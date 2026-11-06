import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


public class ReportGenerator {

    private final BookingManager bookingManager;
    private final TransactionManager transactionManager;

    public ReportGenerator(BookingManager bookingManager, TransactionManager transactionManager) {
        this.bookingManager = bookingManager;
        this.transactionManager = transactionManager;
    }

    public void printDailyReport(LocalDate date) {
        String thick = InputHelper.repeat('=', 74);
        String thin = InputHelper.repeat('-', 74);

        // ---------- bookings
        List<Booking> dayBookings = bookingManager.getBookingsOn(date);
        int confirmed = 0;
        int cancelled = 0;
        Map<Facility, int[]> facilityUsage = new LinkedHashMap<>(); // [bookings, hours]
        for (Facility f : bookingManager.getFacilities()) {
            facilityUsage.put(f, new int[2]);
        }
        for (Booking b : dayBookings) {
            if (b.isActive()) {
                confirmed++;
                int[] usage = facilityUsage.get(b.getFacility());
                if (usage != null) {
                    usage[0]++;
                    usage[1] += b.getDurationHours();
                }
            } else {
                cancelled++;
            }
        }

        // ---------- completed transactions
        List<RentalTransaction> paid = transactionManager.getCompletedTransactionsOn(date);
        double gross = 0;
        double discounts = 0;
        double serviceCharges = 0;
        double revenue = 0;
        Map<String, Integer> itemQuantity = new LinkedHashMap<>();     // equipment / accessories / services
        Map<String, Integer> facilityHours = new LinkedHashMap<>();    // facility packages (hours)
        Map<String, String> itemNames = new LinkedHashMap<>();
        Map<String, Integer> paymentCount = new LinkedHashMap<>();
        Map<String, Double> paymentAmount = new LinkedHashMap<>();
        for (String method : new String[]{"Cash", "Credit/Debit Card", "E-Wallet"}) {
            paymentCount.put(method, 0);
            paymentAmount.put(method, 0.0);
        }
        Map<String, Integer> promotionCount = new LinkedHashMap<>();
        promotionCount.put("A - Off-Peak Saver", 0);
        promotionCount.put("B - Team Booking Reward", 0);
        promotionCount.put("C - Equipment Bundle Discount", 0);
        promotionCount.put("No promotion", 0);

        for (RentalTransaction tx : paid) {
            gross += tx.getSubtotal();
            discounts += tx.getDiscount();
            serviceCharges += tx.getServiceCharge();
            revenue += tx.getPayment().getAmount();

            for (TransactionItem line : tx.getItems()) {
                String code = line.getItem().getItemCode();
                Map<String, Integer> target = line.getItem().isFacility() ? facilityHours : itemQuantity;
                target.put(code, target.getOrDefault(code, 0) + line.getQuantity());
                itemNames.put(code, line.getItem().getItemName());
            }

            String method = tx.getPayment().getMethodName();
            paymentCount.put(method, paymentCount.getOrDefault(method, 0) + 1);
            paymentAmount.put(method, paymentAmount.getOrDefault(method, 0.0) + tx.getPayment().getAmount());

            PromotionResult pr = tx.getPromotionResult();
            String key = "No promotion";
            if (pr != null && pr.hasPromotion()) {
                Promotion p = pr.getSelectedPromotion();
                key = p.getCode() + " - " + p.getName();
            }
            promotionCount.put(key, promotionCount.getOrDefault(key, 0) + 1);
        }

        // ---------- print
        System.out.println();
        System.out.println(thick);
        System.out.println("            ACTIVEHUB SPORTS CENTRE - DAILY SUMMARY REPORT");
        System.out.println("            Date: " + date + " (" + date.getDayOfWeek() + ")");
        System.out.println(thick);

        System.out.println("1. FACILITY BOOKINGS (sessions scheduled on this date)");
        System.out.println("   Total facility bookings (confirmed) : " + confirmed);
        System.out.println("   Cancelled bookings                  : " + cancelled);
        System.out.println("   Facility usage:");
        for (Map.Entry<Facility, int[]> e : facilityUsage.entrySet()) {
            System.out.printf("     %-26s %2d booking(s), %2d hour(s)%n",
                    e.getKey().toString(), e.getValue()[0], e.getValue()[1]);
        }
        System.out.println(thin);

        System.out.println("2. RENTAL TRANSACTIONS (payments received on this date)");
        System.out.println("   Total completed (paid) transactions : " + paid.size());
        System.out.println("   Open (unpaid) transactions for date : "
                + transactionManager.getOpenTransactionsFor(date).size());
        System.out.println(thin);

        System.out.println("3. REVENUE");
        System.out.printf("   Gross rental subtotal               : RM%10.2f%n", gross);
        System.out.printf("   Less: promotion discounts           : RM%10.2f%n", discounts);
        System.out.printf("   Add : service charges (10%%)         : RM%10.2f%n", serviceCharges);
        System.out.printf("   TOTAL DAILY REVENUE                 : RM%10.2f%n", revenue);
        System.out.println(thin);

        System.out.println("4. MOST FREQUENTLY RENTED");
        System.out.println("   Most rented item        : " + topEntries(itemQuantity, itemNames, "unit(s)"));
        System.out.println("   Most rented facility    : " + topEntries(facilityHours, itemNames, "hour(s) paid"));
        Map<String, Integer> facilityCounts = new LinkedHashMap<>();
        Map<String, String> facilityNames = new LinkedHashMap<>();
        for (Map.Entry<Facility, int[]> e : facilityUsage.entrySet()) {
            facilityCounts.put(e.getKey().getFacilityId(), e.getValue()[0]);
            facilityNames.put(e.getKey().getFacilityId(), e.getKey().toString());
        }
        System.out.println("   Most booked court       : " + topEntries(facilityCounts, facilityNames, "booking(s)"));
        System.out.println(thin);

        System.out.println("5. PAYMENT METHOD SUMMARY");
        for (String method : paymentCount.keySet()) {
            System.out.printf("   %-20s : %3d transaction(s)   RM%10.2f%n",
                    method, paymentCount.get(method), paymentAmount.get(method));
        }
        System.out.println(thin);

        System.out.println("6. PROMOTIONS APPLIED");
        for (Map.Entry<String, Integer> e : promotionCount.entrySet()) {
            System.out.printf("   %-32s : %3d%n", e.getKey(), e.getValue());
        }
        System.out.println(thick);
    }

    
    private static String topEntries(Map<String, Integer> counts, Map<String, String> names, String unit) {
        int max = 0;
        for (int value : counts.values()) {
            max = Math.max(max, value);
        }
        if (max == 0) {
            return "No data for this date";
        }
        List<String> top = new ArrayList<>();
        for (Map.Entry<String, Integer> e : counts.entrySet()) {
            if (e.getValue() == max) {
                top.add(names.get(e.getKey()));
            }
        }
        return String.join(", ", top) + " (" + max + " " + unit + ")";
    }
}
