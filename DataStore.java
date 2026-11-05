import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * DataStore - saves and loads all data using text files in a simple
 * pipe-delimited line format (one record per line).
 *
 *   catalogue.txt         CATEGORY|code|name|price[|facilityType]
 *   facilities.txt        id|name|TYPE
 *   bookings.txt          id|name|contact|facilityId|date|time|hours|participants|status
 *   transactions.txt      id|name|contact|bookingId|date|time|participants|status
 *   transaction_items.txt transactionId|itemCode|quantity|unitPrice
 *   payments.txt          paymentId|transactionId|METHOD|amount|dateTime|details...
 */
public class DataStore {

    private static final String CATALOGUE_FILE = "catalogue.txt";
    private static final String FACILITY_FILE = "facilities.txt";
    private static final String BOOKING_FILE = "bookings.txt";
    private static final String TRANSACTION_FILE = "transactions.txt";
    private static final String ITEM_FILE = "transaction_items.txt";
    private static final String PAYMENT_FILE = "payments.txt";

    private final Path dataFolder;

    public DataStore(String folder) {
        this.dataFolder = Paths.get(folder);
        try {
            Files.createDirectories(dataFolder);
        } catch (IOException e) {
            System.out.println("Warning: cannot create data folder - " + e.getMessage());
        }
    }

    // ------------------------------------------------------------------ low-level file helpers

    private boolean exists(String fileName) {
        return Files.exists(dataFolder.resolve(fileName));
    }

    private List<String> readLines(String fileName) {
        List<String> result = new ArrayList<>();
        Path path = dataFolder.resolve(fileName);
        if (!Files.exists(path)) {
            return result;
        }
        try {
            for (String line : Files.readAllLines(path, StandardCharsets.UTF_8)) {
                String trimmed = line.trim();
                if (!trimmed.isEmpty() && !trimmed.startsWith("#")) {
                    result.add(trimmed);
                }
            }
        } catch (IOException e) {
            System.out.println("Warning: cannot read " + fileName + " - " + e.getMessage());
        }
        return result;
    }

    private void writeLines(String fileName, List<String> lines) {
        try {
            Files.write(dataFolder.resolve(fileName), lines, StandardCharsets.UTF_8);
        } catch (IOException e) {
            System.out.println("Warning: cannot save " + fileName + " - " + e.getMessage());
        }
    }

    private static void warnBadLine(String fileName, String line, Exception e) {
        System.out.println("Warning: skipped invalid line in " + fileName + ": " + line + " (" + e.getMessage() + ")");
    }

    // ------------------------------------------------------------------ catalogue

    public Catalogue loadCatalogue() {
        Catalogue catalogue = new Catalogue();
        for (String line : readLines(CATALOGUE_FILE)) {
            try {
                String[] p = line.split("\\|", -1);
                ItemCategory category = ItemCategory.valueOf(p[0].trim());
                String code = p[1].trim();
                String name = p[2].trim();
                double price = Double.parseDouble(p[3].trim());
                RentalItem item;
                switch (category) {
                    case EQUIPMENT:
                        item = new EquipmentItem(code, name, price);
                        break;
                    case FACILITY:
                        item = new FacilityPackage(code, name, price, FacilityType.valueOf(p[4].trim()));
                        break;
                    default:
                        item = new AddOnItem(code, name, price, category);
                        break;
                }
                catalogue.addItem(item);
            } catch (RuntimeException e) {
                warnBadLine(CATALOGUE_FILE, line, e);
            }
        }
        if (catalogue.size() == 0) {
            createDefaultCatalogue(catalogue);
            saveCatalogue(catalogue);
        }
        return catalogue;
    }

    /** The group's 8 rental items: 4 equipment, 2 facility packages, 2 accessories / add-on services. */
    private void createDefaultCatalogue(Catalogue catalogue) {
        catalogue.addItem(new EquipmentItem("E001", "Badminton Racquet", 5.00));
        catalogue.addItem(new EquipmentItem("E002", "Shuttlecock Set (6 pcs)", 8.00));
        catalogue.addItem(new EquipmentItem("E003", "Futsal Ball", 6.00));
        catalogue.addItem(new EquipmentItem("E004", "Goalkeeper Gloves (pair)", 4.00));
        catalogue.addItem(new FacilityPackage("F001", "Badminton Court (per hour)", 20.00, FacilityType.BADMINTON));
        catalogue.addItem(new FacilityPackage("F002", "Futsal Court (per hour)", 90.00, FacilityType.FUTSAL));
        catalogue.addItem(new AddOnItem("A001", "Sports Towel", 3.00, ItemCategory.ACCESSORY));
        catalogue.addItem(new AddOnItem("S001", "Locker & Shower Access", 5.00, ItemCategory.SERVICE));
    }

    public void saveCatalogue(Catalogue catalogue) {
        List<String> lines = new ArrayList<>();
        lines.add("# CATEGORY|code|name|price|facilityType(for FACILITY only)");
        for (RentalItem item : catalogue.getItems()) {
            lines.add(item.toFileLine());
        }
        writeLines(CATALOGUE_FILE, lines);
    }

    // ------------------------------------------------------------------ facilities

    public void loadFacilities(BookingManager manager) {
        for (String line : readLines(FACILITY_FILE)) {
            try {
                String[] p = line.split("\\|", -1);
                manager.addFacility(new Facility(p[0], p[1], FacilityType.valueOf(p[2].trim())));
            } catch (RuntimeException e) {
                warnBadLine(FACILITY_FILE, line, e);
            }
        }
        if (manager.getFacilities().isEmpty()) {
            for (int i = 1; i <= 4; i++) {
                manager.addFacility(new Facility("B" + i, "Badminton Court " + i, FacilityType.BADMINTON));
            }
            for (int i = 1; i <= 2; i++) {
                manager.addFacility(new Facility("F" + i, "Futsal Court " + i, FacilityType.FUTSAL));
            }
            List<String> lines = new ArrayList<>();
            lines.add("# facilityId|facilityName|TYPE");
            for (Facility f : manager.getFacilities()) {
                lines.add(f.toFileLine());
            }
            writeLines(FACILITY_FILE, lines);
        }
    }

    // ------------------------------------------------------------------ bookings

    public void loadBookings(BookingManager manager) {
        for (String line : readLines(BOOKING_FILE)) {
            try {
                String[] p = line.split("\\|", -1);
                Facility facility = manager.findFacility(p[3]);
                if (facility == null) {
                    throw new IllegalArgumentException("unknown facility " + p[3]);
                }
                Booking booking = new Booking(p[0], new Customer(p[1], p[2]), facility,
                        LocalDate.parse(p[4]), LocalTime.parse(p[5]),
                        Integer.parseInt(p[6]), Integer.parseInt(p[7]));
                booking.restoreStatus(BookingStatus.valueOf(p[8]));
                manager.addLoadedBooking(booking);
            } catch (RuntimeException e) {
                warnBadLine(BOOKING_FILE, line, e);
            }
        }
    }

    public void saveBookings(BookingManager manager) {
        List<String> lines = new ArrayList<>();
        for (Booking b : manager.getBookings()) {
            lines.add(b.toFileLine());
        }
        writeLines(BOOKING_FILE, lines);
    }

    // ------------------------------------------------------------------ transactions

    public void loadTransactions(TransactionManager txManager, Catalogue catalogue,
                                 BookingManager bookingManager, PromotionEngine engine) {
        Map<String, RentalTransaction> map = new HashMap<>();
        Map<String, TransactionStatus> savedStatus = new HashMap<>();

        // 1. transaction headers (all start as OPEN so items can be restored)
        for (String line : readLines(TRANSACTION_FILE)) {
            try {
                String[] p = line.split("\\|", -1);
                Booking booking = p[3].equals("-") ? null : bookingManager.findBooking(p[3]);
                RentalTransaction tx = new RentalTransaction(p[0], new Customer(p[1], p[2]), booking,
                        LocalDate.parse(p[4]), LocalTime.parse(p[5]), Integer.parseInt(p[6]));
                map.put(tx.getTransactionId(), tx);
                savedStatus.put(tx.getTransactionId(), TransactionStatus.valueOf(p[7]));
                txManager.addLoadedTransaction(tx);
            } catch (RuntimeException e) {
                warnBadLine(TRANSACTION_FILE, line, e);
            }
        }

        // 2. transaction items
        for (String line : readLines(ITEM_FILE)) {
            try {
                String[] p = line.split("\\|", -1);
                RentalTransaction tx = map.get(p[0]);
                RentalItem item = catalogue.findItem(p[1]);
                if (tx == null || item == null) {
                    throw new IllegalArgumentException("unknown transaction or item");
                }
                tx.restoreItem(item, Integer.parseInt(p[2]), Double.parseDouble(p[3]));
            } catch (RuntimeException e) {
                warnBadLine(ITEM_FILE, line, e);
            }
        }

        // 3. re-evaluate promotions (rules are deterministic, so the result is the same as before)
        for (RentalTransaction tx : map.values()) {
            if (!tx.isEmpty()) {
                engine.applyBestPromotion(tx);
            }
        }

        // 4. payments -> mark as PAID
        for (String line : readLines(PAYMENT_FILE)) {
            try {
                Payment payment = Payment.fromFileLine(line);
                RentalTransaction tx = map.get(payment.getTransactionId());
                if (tx == null) {
                    throw new IllegalArgumentException("unknown transaction");
                }
                tx.restorePayment(payment);
            } catch (RuntimeException e) {
                warnBadLine(PAYMENT_FILE, line, e);
            }
        }

        // 5. cancelled transactions
        for (RentalTransaction tx : map.values()) {
            if (savedStatus.get(tx.getTransactionId()) == TransactionStatus.CANCELLED
                    && tx.getStatus() == TransactionStatus.OPEN) {
                tx.cancel();
            }
        }
    }

    public void saveTransactions(TransactionManager txManager) {
        List<String> txLines = new ArrayList<>();
        List<String> itemLines = new ArrayList<>();
        List<String> paymentLines = new ArrayList<>();
        for (RentalTransaction tx : txManager.getTransactions()) {
            txLines.add(tx.toFileLine());
            for (TransactionItem line : tx.getItems()) {
                itemLines.add(tx.getTransactionId() + "|" + line.getItem().getItemCode() + "|"
                        + line.getQuantity() + "|" + String.format("%.2f", line.getUnitPrice()));
            }
            if (tx.getPayment() != null) {
                paymentLines.add(tx.getPayment().toFileLine());
            }
        }
        writeLines(TRANSACTION_FILE, txLines);
        writeLines(ITEM_FILE, itemLines);
        writeLines(PAYMENT_FILE, paymentLines);
    }
}
