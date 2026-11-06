import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * ActiveHubSystem - main class and console menu of the
 * ActiveHub Sports Facility Booking, Equipment Rental & Promotion System.
 *
 * Compile : javac *.java
 * Run     : java ActiveHubSystem
 * Data is stored in text files inside the "data" folder.
 */
public class ActiveHubSystem {

    private static final int MAX_ITEM_QUANTITY = 20;

    private final InputHelper input = new InputHelper(new Scanner(System.in));
    private final DataStore dataStore = new DataStore("data");
    private final BookingManager bookingManager = new BookingManager();
    private final TransactionManager transactionManager = new TransactionManager();
    private final PromotionEngine promotionEngine = new PromotionEngine();
    private final Catalogue catalogue;
    private final ReportGenerator reportGenerator;

    public ActiveHubSystem() {
        
        promotionEngine.addPromotion(new OffPeakSaverPromotion());
        promotionEngine.addPromotion(new TeamBookingPromotion());
        promotionEngine.addPromotion(new EquipmentBundlePromotion());

        catalogue = dataStore.loadCatalogue();
        dataStore.loadFacilities(bookingManager);
        dataStore.loadBookings(bookingManager);
        dataStore.loadTransactions(transactionManager, catalogue, bookingManager, promotionEngine);
        reportGenerator = new ReportGenerator(bookingManager, transactionManager);
    }

    public static void main(String[] args) {
        new ActiveHubSystem().run();
    }

    // =====================================================================
    // MAIN MENU
    // =====================================================================

    public void run() {
        System.out.println("Welcome to ActiveHub Sports Centre Management System");
        System.out.println("Today is " + LocalDate.now() + " (" + LocalDate.now().getDayOfWeek() + ")");
        boolean running = true;
        while (running) {
            System.out.println();
            System.out.println("===== ACTIVEHUB SYSTEM =====");
            System.out.println("1. Facility Booking Module");
            System.out.println("2. View Rental Catalogue");
            System.out.println("3. Create Rental Transaction");
            System.out.println("4. Apply Promotion");
            System.out.println("5. Make Payment");
            System.out.println("6. Daily Report");
            System.out.println("7. Exit");
            int choice = input.readInt("Enter your choice (1-7): ", 1, 7);
            try {
                switch (choice) {
                    case 1:
                        facilityBookingMenu();
                        break;
                    case 2:
                        catalogueMenu();
                        break;
                    case 3:
                        transactionMenu();
                        break;
                    case 4:
                        applyPromotion();
                        input.pause();
                        break;
                    case 5:
                        makePayment();
                        input.pause();
                        break;
                    case 6:
                        dailyReport();
                        input.pause();
                        break;
                    case 7:
                        running = !input.confirm("Are you sure you want to exit? (Y/N): ");
                        break;
                    default:
                        break;
                }
            } catch (IllegalArgumentException | IllegalStateException e) {
                System.out.println("  >> " + e.getMessage());
            }
        }
        System.out.println("All data has been saved. Thank you for using ActiveHub System. Goodbye!");
    }

    // =====================================================================
    // 1. FACILITY BOOKING MODULE
    // =====================================================================

    private void facilityBookingMenu() {
        while (true) {
            InputHelper.printHeader("FACILITY BOOKING MODULE");
            System.out.println("1. Make New Booking");
            System.out.println("2. View All Booking Records");
            System.out.println("3. View Bookings by Date");
            System.out.println("4. Check Facility Availability (Daily Schedule)");
            System.out.println("5. Cancel Booking");
            System.out.println("0. Back to Main Menu");
            int choice = input.readInt("Enter your choice: ", 0, 5);
            switch (choice) {
                case 0:
                    return;
                case 1:
                    makeBooking();
                    break;
                case 2:
                    displayBookings(bookingManager.getBookings(), "ALL BOOKING RECORDS");
                    break;
                case 3:
                    LocalDate date = input.readDate("Enter date (yyyy-MM-dd, Enter = today): ", true);
                    displayBookings(bookingManager.getBookingsOn(date), "BOOKINGS ON " + date + " (" + date.getDayOfWeek() + ")");
                    break;
                case 4:
                    showFacilitySchedule();
                    break;
                case 5:
                    cancelBooking();
                    break;
                default:
                    break;
            }
            input.pause();
        }
    }

    private void makeBooking() {
        InputHelper.printHeader("NEW FACILITY BOOKING");
        Booking booking = createBookingInteractive(null);
        if (booking != null && input.confirm("Create a rental transaction for this booking now? (Y/N): ")) {
            createTransactionForBooking(booking);
        }
    }

    private Booking createBookingInteractive(LocalDate fixedDate) {
        String name = input.readName("Customer name               : ");
        String contact = input.readContactNumber("Contact number              : ");

        LocalDate date;
        LocalTime start;
        int duration;
        while (true) {
            LocalTime defaultTime = null;
            if (fixedDate == null) {
                date = input.readDate("Booking date (yyyy-MM-dd)   : ", false);
            } else {
                date = fixedDate;
                defaultTime = LocalTime.now().truncatedTo(ChronoUnit.MINUTES);
                System.out.println("Booking date                : " + date + " (today, walk-in)");
            }
            String timePrompt = defaultTime == null
                    ? "Start time (24-hour HH:mm)  : "
                    : "Start time (HH:mm, Enter = now " + defaultTime + "): ";
            start = input.readTime(timePrompt, defaultTime);
            duration = input.readInt("Duration in hours (1-" + BookingManager.MAX_DURATION_HOURS + ")    : ",
                    1, BookingManager.MAX_DURATION_HOURS);

            String error = bookingManager.validateSlot(date, start, duration);
            if (error == null) {
                break;
            }
            System.out.println("  >> " + error);
            if (!input.confirm("Try another date/time? (Y/N): ")) {
                return null;
            }
        }
        System.out.println("  Day: " + date.getDayOfWeek() + "  |  Session: " + start + " - " + start.plusHours(duration));

        FacilityType type = selectFacilityType();
        int participants = input.readInt("Number of participants (1-" + type.getMaxParticipants() + "): ",
                1, type.getMaxParticipants());

        List<Facility> available = bookingManager.findAvailableFacilities(type, date, start, duration);
        if (available.isEmpty()) {
            System.out.println("  >> Sorry, all " + type.getDisplayName() + "s are already booked on "
                    + date + " " + start + "-" + start.plusHours(duration) + ".");
            System.out.println("     Use 'Check Facility Availability' to find a free slot.");
            return null;
        }
        System.out.print("  Available courts for this slot: ");
        for (Facility f : available) {
            System.out.print(f.getFacilityId() + " ");
        }
        System.out.println();

        Facility chosen = null;
        while (chosen == null) {
            String id = input.readLine("Preferred court number (e.g. " + available.get(0).getFacilityId()
                    + ", Enter = auto-assign): ");
            if (id.isEmpty()) {
                chosen = available.get(0);
                break;
            }
            Facility facility = bookingManager.findFacility(id);
            if (facility == null || facility.getType() != type) {
                System.out.println("  >> '" + id + "' is not a valid " + type.getDisplayName() + " number.");
                continue;
            }
            Booking conflict = bookingManager.findConflict(facility, date, start, duration);
            if (conflict != null) {
                System.out.println("  >> DUPLICATE BOOKING BLOCKED: " + facility + " is already booked "
                        + conflict.getStartTime() + "-" + conflict.getEndTime() + " (" + conflict.getBookingId() + ").");
                continue;
            }
            chosen = facility;
        }

        System.out.println();
        System.out.println("Booking summary:");
        System.out.println("  Customer     : " + name + " (" + contact + ")");
        System.out.println("  Facility     : " + chosen);
        System.out.println("  Date / time  : " + date + " (" + date.getDayOfWeek() + ") " + start + " - " + start.plusHours(duration));
        System.out.println("  Participants : " + participants);
        if (!input.confirm("Confirm this booking? (Y/N): ")) {
            System.out.println("Booking not saved.");
            return null;
        }
        try {
            Booking booking = bookingManager.createBooking(new Customer(name, contact), chosen, date, start,
                    duration, participants);
            dataStore.saveBookings(bookingManager);
            System.out.println("Booking " + booking.getBookingId() + " confirmed successfully!");
            return booking;
        } catch (IllegalArgumentException | IllegalStateException e) {
            System.out.println("  >> Booking failed: " + e.getMessage());
            return null;
        }
    }

    private FacilityType selectFacilityType() {
        FacilityType[] types = FacilityType.values();
        System.out.println("Facility types:");
        for (int i = 0; i < types.length; i++) {
            System.out.println("  " + (i + 1) + ". " + types[i].getDisplayName()
                    + " (" + bookingManager.getFacilitiesByType(types[i]).size() + " courts, max "
                    + types[i].getMaxParticipants() + " participants)");
        }
        return types[input.readInt("Preferred facility type: ", 1, types.length) - 1];
    }

    private void displayBookings(List<Booking> list, String title) {
        InputHelper.printHeader(title);
        if (list.isEmpty()) {
            System.out.println("No booking records found.");
            return;
        }
        System.out.printf("%-7s %-18s %-12s %-4s %-10s %-9s %-11s %3s %4s  %s%n",
                "ID", "Customer", "Contact", "Crt", "Date", "Day", "Time", "Hrs", "Pax", "Status");
        System.out.println(InputHelper.repeat('-', 96));
        for (Booking b : list) {
            System.out.println(b);
        }
        System.out.println("Total: " + list.size() + " booking(s)");
    }

    private void showFacilitySchedule() {
        LocalDate date = input.readDate("Enter date (yyyy-MM-dd, Enter = today): ", true);
        InputHelper.printHeader("FACILITY SCHEDULE - " + date + " (" + date.getDayOfWeek() + ")");
        StringBuilder header = new StringBuilder(String.format("%-22s", "Court \\ Hour"));
        for (int h = BookingManager.OPENING_TIME.getHour(); h < BookingManager.CLOSING_TIME.getHour(); h++) {
            header.append(String.format("%3d", h));
        }
        System.out.println(header);
        for (Facility f : bookingManager.getFacilities()) {
            StringBuilder row = new StringBuilder(String.format("%-22s", f.toString()));
            for (int h = BookingManager.OPENING_TIME.getHour(); h < BookingManager.CLOSING_TIME.getHour(); h++) {
                boolean booked = !bookingManager.isAvailable(f, date, LocalTime.of(h, 0), 1);
                row.append(booked ? "  X" : "  .");
            }
            System.out.println(row);
        }
        System.out.println("Legend: X = booked / occupied, . = available");

        List<Booking> active = new ArrayList<>();
        for (Booking b : bookingManager.getBookingsOn(date)) {
            if (b.isActive()) {
                active.add(b);
            }
        }
        if (!active.isEmpty()) {
            System.out.println();
            System.out.println("Bookings on this date:");
            for (Booking b : active) {
                System.out.println("  " + b.getBookingId() + "  " + b.getFacility().getFacilityId() + "  "
                        + b.getStartTime() + "-" + b.getEndTime() + "  " + b.getCustomer());
            }
        }
    }

    private void cancelBooking() {
        List<Booking> active = bookingManager.getActiveBookings();
        if (active.isEmpty()) {
            System.out.println("There are no active bookings to cancel.");
            return;
        }
        displayBookings(active, "ACTIVE BOOKINGS");
        String id = input.readLine("Enter Booking ID to cancel (0 = back): ");
        if (id.equals("0")) {
            return;
        }
        Booking booking = bookingManager.findBooking(id);
        if (booking == null || !booking.isActive()) {
            System.out.println("  >> Active booking '" + id + "' not found.");
            return;
        }
        RentalTransaction tx = transactionManager.findByBooking(booking.getBookingId());
        if (tx != null && tx.getStatus() == TransactionStatus.PAID) {
            System.out.println("  >> Booking " + booking.getBookingId() + " has already been paid ("
                    + tx.getTransactionId() + ") and cannot be cancelled here.");
            return;
        }
        System.out.println("Booking: " + booking.getBookingId() + " - " + booking.getCustomer() + ", "
                + booking.getFacility() + ", " + booking.getBookingDate() + " " + booking.getStartTime());
        if (tx != null) {
            System.out.println("Note: unpaid transaction " + tx.getTransactionId() + " will also be cancelled.");
        }
        if (!input.confirm("Confirm cancellation? (Y/N): ")) {
            System.out.println("Cancellation aborted.");
            return;
        }
        booking.cancel();
        if (tx != null) {
            tx.cancel();
            dataStore.saveTransactions(transactionManager);
        }
        dataStore.saveBookings(bookingManager);
        System.out.println("Booking " + booking.getBookingId() + " has been cancelled. The court is now available.");
    }

    // =====================================================================
    // 2. RENTAL CATALOGUE
    // =====================================================================

    private void catalogueMenu() {
        while (true) {
            InputHelper.printHeader("RENTAL CATALOGUE");
            System.out.println("1. View Full Catalogue");
            System.out.println("2. View by Category");
            System.out.println("3. Update Rental Price");
            System.out.println("0. Back to Main Menu");
            int choice = input.readInt("Enter your choice: ", 0, 3);
            if (choice == 0) {
                return;
            }
            if (choice == 1) {
                catalogue.display();
            } else if (choice == 2) {
                ItemCategory[] categories = ItemCategory.values();
                for (int i = 0; i < categories.length; i++) {
                    System.out.println("  " + (i + 1) + ". " + categories[i].getLabel());
                }
                int c = input.readInt("Select category: ", 1, categories.length);
                catalogue.displayCategory(categories[c - 1]);
            } else {
                updatePrice();
            }
            input.pause();
        }
    }

    private void updatePrice() {
        catalogue.display();
        String code = input.readLine("Enter item code to update (0 = back): ");
        if (code.equals("0")) {
            return;
        }
        RentalItem item = catalogue.findItem(code);
        if (item == null) {
            System.out.println("  >> Item '" + code + "' not found.");
            return;
        }
        System.out.println("Current price of " + item.getItemName() + ": " + Money.format(item.getRentalPrice()));
        double price = input.readDouble("New rental price (RM): ", 0, 10000);
        if (input.confirm("Change price to " + Money.format(price) + "? (Y/N): ")) {
            item.setRentalPrice(price);
            dataStore.saveCatalogue(catalogue);
            System.out.println("Price updated. (Existing transactions keep their original price.)");
        }
    }

    // =====================================================================
    // 3. RENTAL TRANSACTION MODULE
    // =====================================================================

    private void transactionMenu() {
        while (true) {
            InputHelper.printHeader("RENTAL TRANSACTION MODULE");
            System.out.println("1. Create New Rental Transaction");
            System.out.println("2. Add Items to Existing Transaction");
            System.out.println("3. Change Item Quantity / Remove Item");
            System.out.println("4. View Transaction Details (Bill)");
            System.out.println("5. View All Transactions");
            System.out.println("0. Back to Main Menu");
            int choice = input.readInt("Enter your choice: ", 0, 5);
            switch (choice) {
                case 0:
                    return;
                case 1:
                    createTransaction();
                    break;
                case 2:
                    addItemsToExistingTransaction();
                    break;
                case 3:
                    changeItemQuantity();
                    break;
                case 4:
                    viewTransactionDetails();
                    break;
                case 5:
                    viewAllTransactions();
                    break;
                default:
                    break;
            }
            input.pause();
        }
    }

    private void createTransaction() {
        InputHelper.printHeader("CREATE RENTAL TRANSACTION");
        System.out.println("1. Customer with an existing facility booking");
        System.out.println("2. Walk-in customer - use a court now (court is booked automatically)");
        System.out.println("3. Walk-in customer - equipment / accessories only");
        System.out.println("0. Back");
        int choice = input.readInt("Enter your choice: ", 0, 3);
        if (choice == 1) {
            Booking booking = selectBookingWithoutTransaction();
            if (booking != null) {
                createTransactionForBooking(booking);
            }
        } else if (choice == 2) {
            Booking booking = createBookingInteractive(LocalDate.now());
            if (booking != null) {
                createTransactionForBooking(booking);
            }
        } else if (choice == 3) {
            createWalkInTransaction();
        }
    }

    private Booking selectBookingWithoutTransaction() {
        List<Booking> list = new ArrayList<>();
        for (Booking b : bookingManager.getActiveBookings()) {
            if (!b.getBookingDate().isBefore(LocalDate.now())
                    && transactionManager.findByBooking(b.getBookingId()) == null) {
                list.add(b);
            }
        }
        if (list.isEmpty()) {
            System.out.println("No bookings are waiting for a transaction. Please make a booking first.");
            return null;
        }
        displayBookings(list, "BOOKINGS WITHOUT A TRANSACTION");
        String id = input.readLine("Enter Booking ID (0 = back): ");
        if (id.equals("0")) {
            return null;
        }
        Booking booking = bookingManager.findBooking(id);
        if (booking == null || !list.contains(booking)) {
            System.out.println("  >> Booking '" + id + "' is not in the list above.");
            return null;
        }
        return booking;
    }

    private void createTransactionForBooking(Booking booking) {
        RentalTransaction existing = transactionManager.findByBooking(booking.getBookingId());
        if (existing != null) {
            System.out.println("  >> Booking " + booking.getBookingId() + " already has transaction "
                    + existing.getTransactionId() + ". Use 'Add Items to Existing Transaction' instead.");
            return;
        }
        RentalTransaction tx = transactionManager.createTransaction(booking.getCustomer(), booking,
                booking.getBookingDate(), booking.getStartTime(), booking.getParticipants());
        FacilityPackage pkg = catalogue.findPackageFor(booking.getFacility().getType());
        if (pkg != null) {
            tx.addItem(pkg, booking.getDurationHours());
            System.out.printf("Transaction %s created. Facility charge added: %d hour(s) x %s = RM%.2f%n",
                    tx.getTransactionId(), booking.getDurationHours(), pkg.getItemName(),
                    pkg.getRentalPrice() * booking.getDurationHours());
        } else {
            System.out.println("Warning: no facility package found in catalogue for "
                    + booking.getFacility().getType().getDisplayName());
        }
        if (input.confirm("Add equipment / accessories to this transaction? (Y/N): ")) {
            addItemsLoop(tx);
        }
        finishTransactionUpdate(tx, true);
    }

    private void createWalkInTransaction() {
        System.out.println("Walk-in rental (equipment / accessories only)");
        String name = input.readName("Customer name          : ");
        String contact = input.readContactNumber("Contact number         : ");
        int participants = input.readInt("Number of participants : ", 1, 50);
        RentalTransaction tx = transactionManager.createTransaction(new Customer(name, contact), null,
                LocalDate.now(), LocalTime.now().truncatedTo(ChronoUnit.MINUTES), participants);
        System.out.println("Transaction " + tx.getTransactionId() + " created.");
        addItemsLoop(tx);
        finishTransactionUpdate(tx, true);
    }

    private void addItemsLoop(RentalTransaction tx) {
        catalogue.display();
        System.out.println("Note: facility/court packages are charged automatically from bookings.");
        while (true) {
            String code = input.readLine("Enter item code to add (0 = finish): ").toUpperCase();
            if (code.equals("0")) {
                break;
            }
            if (code.isEmpty()) {
                continue;
            }
            RentalItem item = catalogue.findItem(code);
            if (item == null) {
                System.out.println("  >> Item code '" + code + "' not found. Please refer to the catalogue.");
                continue;
            }
            if (item.isFacility()) {
                System.out.println("  >> Facility packages cannot be added manually. Please use the Facility "
                        + "Booking Module so the court is reserved without double booking.");
                continue;
            }
            TransactionItem line = tx.findLine(code);
            int current = line == null ? 0 : line.getQuantity();
            if (current >= MAX_ITEM_QUANTITY) {
                System.out.println("  >> Maximum quantity (" + MAX_ITEM_QUANTITY + ") already reached for this item.");
                continue;
            }
            int qty = input.readInt("Quantity of " + item.getItemName() + " (1-" + (MAX_ITEM_QUANTITY - current) + "): ",
                    1, MAX_ITEM_QUANTITY - current);
            tx.addItem(item, qty);
            System.out.printf("  + Added %d x %s @ RM%.2f = RM%.2f   | Current subtotal: RM%.2f%n",
                    qty, item.getItemName(), item.getRentalPrice(), item.getRentalPrice() * qty, tx.getSubtotal());
        }
    }

    private void finishTransactionUpdate(RentalTransaction tx, boolean isNew) {
        if (tx.isEmpty()) {
            if (isNew) {
                transactionManager.removeTransaction(tx);
                System.out.println("No items were added, so transaction " + tx.getTransactionId() + " was discarded.");
            } else {
                tx.cancel();
                System.out.println("All items were removed, so transaction " + tx.getTransactionId() + " was cancelled.");
            }
            dataStore.saveTransactions(transactionManager);
            return;
        }
        promotionEngine.applyBestPromotion(tx); 
        dataStore.saveTransactions(transactionManager);
        System.out.println();
        System.out.println(tx.generateBill());
        System.out.println("Transaction " + tx.getTransactionId() + " saved. Use 'Make Payment' when the customer is ready to pay.");
    }

    private RentalTransaction selectOpenTransaction() {
        List<RentalTransaction> open = transactionManager.getOpenTransactions();
        if (open.isEmpty()) {
            System.out.println("There are no open (unpaid) transactions.");
            return null;
        }
        printTransactionTable(open, "OPEN (UNPAID) TRANSACTIONS");
        String id = input.readLine("Enter Transaction ID (0 = back): ");
        if (id.equals("0")) {
            return null;
        }
        RentalTransaction tx = transactionManager.findTransaction(id);
        if (tx == null || tx.getStatus() != TransactionStatus.OPEN) {
            System.out.println("  >> Open transaction '" + id + "' not found.");
            return null;
        }
        return tx;
    }

    private void addItemsToExistingTransaction() {
        RentalTransaction tx = selectOpenTransaction();
        if (tx == null) {
            return;
        }
        System.out.println(tx.generateBill());
        addItemsLoop(tx);
        finishTransactionUpdate(tx, false);
    }

    private void changeItemQuantity() {
        RentalTransaction tx = selectOpenTransaction();
        if (tx == null) {
            return;
        }
        System.out.println(tx.generateBill());
        String code = input.readLine("Enter item code to change (0 = back): ");
        if (code.equals("0")) {
            return;
        }
        TransactionItem line = tx.findLine(code);
        if (line == null) {
            System.out.println("  >> Item '" + code + "' is not in this transaction.");
            return;
        }
        if (line.getItem().isFacility()) {
            System.out.println("  >> Facility hours follow the booking. Cancel the booking and rebook to change hours.");
            return;
        }
        int qty = input.readInt("New quantity for " + line.getItem().getItemName() + " (0 = remove, max "
                + MAX_ITEM_QUANTITY + "): ", 0, MAX_ITEM_QUANTITY);
        tx.updateQuantity(code, qty);
        System.out.println(qty == 0 ? "Item removed." : "Quantity updated.");
        finishTransactionUpdate(tx, false);
    }

    private void viewTransactionDetails() {
        if (transactionManager.getTransactions().isEmpty()) {
            System.out.println("No transactions recorded yet.");
            return;
        }
        printTransactionTable(transactionManager.getTransactions(), "ALL TRANSACTIONS");
        String id = input.readLine("Enter Transaction ID (0 = back): ");
        if (id.equals("0")) {
            return;
        }
        RentalTransaction tx = transactionManager.findTransaction(id);
        if (tx == null) {
            System.out.println("  >> Transaction '" + id + "' not found.");
            return;
        }
        System.out.println(tx.generateBill());
    }

    private void viewAllTransactions() {
        if (transactionManager.getTransactions().isEmpty()) {
            System.out.println("No transactions recorded yet.");
            return;
        }
        printTransactionTable(transactionManager.getTransactions(), "ALL TRANSACTIONS");
    }

    private void printTransactionTable(List<RentalTransaction> list, String title) {
        InputHelper.printHeader(title);
        System.out.printf("%-6s %-18s %-7s %-10s %5s %10s %10s  %-9s %s%n",
                "ID", "Customer", "Booking", "Date", "Lines", "Subtotal", "Final", "Status", "Payment");
        System.out.println(InputHelper.repeat('-', 96));
        for (RentalTransaction tx : list) {
            System.out.println(tx.toSummaryRow());
        }
    }

    // =====================================================================
    // 4. APPLY PROMOTION
    // =====================================================================

    private void applyPromotion() {
        InputHelper.printHeader("PROMOTION ENGINE");
        for (Promotion p : promotionEngine.getPromotions()) {
            System.out.printf("[%s] %-27s %s%n", p.getCode(), p.getName(), p.getDescription());
        }
        System.out.println("Only ONE promotion (greatest saving) is applied. Ties: A before B before C.");
        RentalTransaction tx = selectOpenTransaction();
        if (tx == null) {
            return;
        }
        if (tx.isEmpty()) {
            System.out.println("  >> This transaction has no items.");
            return;
        }
        PromotionResult result = promotionEngine.applyBestPromotion(tx);
        System.out.println();
        System.out.println("Promotion evaluation for " + tx.getTransactionId() + ":");
        for (Promotion p : promotionEngine.getPromotions()) {
            Double saving = result.getSavingFor(p);
            String status = saving == null ? "Not eligible" : String.format("ELIGIBLE - saving RM%.2f", saving);
            if (p == result.getSelectedPromotion()) {
                status += "   <== APPLIED";
            }
            System.out.printf("  [%s] %-27s : %s%n", p.getCode(), p.getName(), status);
        }
        if (!result.hasPromotion()) {
            System.out.println("  No promotion applies to this transaction.");
        }
        dataStore.saveTransactions(transactionManager);
        System.out.println();
        System.out.println(tx.generateBill());
    }

    // =====================================================================
    // 5. MAKE PAYMENT
    // =====================================================================

    private void makePayment() {
        InputHelper.printHeader("MAKE PAYMENT");
        RentalTransaction tx = selectOpenTransaction();
        if (tx == null) {
            return;
        }
        if (tx.isEmpty()) {
            System.out.println("  >> This transaction has no items to pay for.");
            return;
        }
        promotionEngine.applyBestPromotion(tx); 
        System.out.println(tx.generateBill());
        if (!input.confirm("Proceed to payment of " + Money.format(tx.getFinalAmount()) + "? (Y/N): ")) {
            System.out.println("Payment not made. Transaction remains OPEN.");
            return;
        }

        System.out.println("Payment methods:");
        System.out.println("  1. Cash");
        System.out.println("  2. Credit / Debit Card");
        System.out.println("  3. E-Wallet");
        System.out.println("  0. Cancel");
        int method = input.readInt("Select payment method: ", 0, 3);
        if (method == 0) {
            System.out.println("Payment cancelled. Transaction remains OPEN.");
            return;
        }

        String paymentId = transactionManager.generatePaymentId();
        double amount = tx.getFinalAmount();
        Payment payment; 
        if (method == 1) {
            payment = new CashPayment(paymentId, tx.getTransactionId(), amount);
        } else if (method == 2) {
            payment = new CardPayment(paymentId, tx.getTransactionId(), amount);
        } else {
            payment = new EWalletPayment(paymentId, tx.getTransactionId(), amount);
        }

        if (payment.processPayment(input)) { 
            tx.markPaid(payment);
            dataStore.saveTransactions(transactionManager);
            System.out.println();
            System.out.println("PAYMENT SUCCESSFUL!");
            System.out.println(tx.generateBill());
        } else {
            System.out.println("Payment cancelled. Transaction remains OPEN.");
        }
    }

    // =====================================================================
    // 6. DAILY REPORT
    // =====================================================================

    private void dailyReport() {
        LocalDate date = input.readDate("Report date (yyyy-MM-dd, Enter = today): ", true);
        reportGenerator.printDailyReport(date);
    }
}
