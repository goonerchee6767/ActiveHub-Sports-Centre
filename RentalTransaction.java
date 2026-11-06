import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;


public class RentalTransaction implements Storable {

    public static final double SERVICE_CHARGE_RATE = 0.10;
    private static final int WIDTH = 74;

    private final String transactionId;
    private final Customer customer;
    private final Booking booking;          // null for equipment-only walk-in
    private final LocalDate serviceDate;
    private final LocalTime startTime;
    private final int participants;
    private final List<TransactionItem> items = new ArrayList<>();
    private TransactionStatus status;
    private PromotionResult promotionResult;
    private Payment payment;

    public RentalTransaction(String transactionId, Customer customer, Booking booking,
                             LocalDate serviceDate, LocalTime startTime, int participants) {
        if (customer == null || serviceDate == null || startTime == null) {
            throw new IllegalArgumentException("Transaction details are incomplete.");
        }
        this.transactionId = transactionId;
        this.customer = customer;
        this.booking = booking;
        this.serviceDate = serviceDate;
        this.startTime = startTime;
        this.participants = participants;
        this.status = TransactionStatus.OPEN;
    }

    // ------------------------------------------------------------------ item handling

    
    public void addItem(RentalItem item, int quantity) {
        ensureOpen();
        if (item == null) {
            throw new IllegalArgumentException("Item cannot be null.");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be at least 1.");
        }
        TransactionItem existing = findLine(item.getItemCode());
        if (existing != null) {
            existing.increaseQuantity(quantity);
        } else {
            items.add(new TransactionItem(item, quantity, item.getRentalPrice()));
        }
        promotionResult = null; // items changed -> promotions must be re-evaluated
    }

   
    void restoreItem(RentalItem item, int quantity, double unitPrice) {
        items.add(new TransactionItem(item, quantity, unitPrice));
    }

    
    public void updateQuantity(String itemCode, int newQuantity) {
        ensureOpen();
        TransactionItem line = findLine(itemCode);
        if (line == null) {
            throw new IllegalArgumentException("Item " + itemCode + " is not in this transaction.");
        }
        if (newQuantity <= 0) {
            items.remove(line);
        } else {
            line.setQuantity(newQuantity);
        }
        promotionResult = null;
    }

    public TransactionItem findLine(String itemCode) {
        for (TransactionItem line : items) {
            if (line.getItem().getItemCode().equalsIgnoreCase(itemCode.trim())) {
                return line;
            }
        }
        return null;
    }

    public List<TransactionItem> getItems() {
        return Collections.unmodifiableList(items);
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }

    // ------------------------------------------------------------------ calculations

    public double getSubtotal() {
        double total = 0;
        for (TransactionItem line : items) {
            total += line.getSubtotal();
        }
        return Money.round(total);
    }

    public double getChargeByCategory(ItemCategory category) {
        double total = 0;
        for (TransactionItem line : items) {
            if (line.getItem().getCategory() == category) {
                total += line.getSubtotal();
            }
        }
        return Money.round(total);
    }

    
    public double getFacilityCharge() {
        return getChargeByCategory(ItemCategory.FACILITY);
    }

    
    public int getEquipmentQuantity() {
        int count = 0;
        for (TransactionItem line : items) {
            if (line.getItem().isEquipment()) {
                count += line.getQuantity();
            }
        }
        return count;
    }

    public boolean hasFacility() {
        for (TransactionItem line : items) {
            if (line.getItem().isFacility()) {
                return true;
            }
        }
        return false;
    }

    public double getDiscount() {
        return promotionResult == null ? 0.0 : promotionResult.getSelectedSaving();
    }

    public double getAmountAfterDiscount() {
        return Money.round(Math.max(0, getSubtotal() - getDiscount()));
    }

    /** 10% service charge */
    public double getServiceCharge() {
        return Money.round(getAmountAfterDiscount() * SERVICE_CHARGE_RATE);
    }

    public double getFinalAmount() {
        return Money.round(getAmountAfterDiscount() + getServiceCharge());
    }

    // ------------------------------------------------------------------ state changes

    public void applyPromotion(PromotionResult result) {
        ensureOpen();
        this.promotionResult = result;
    }

    public void markPaid(Payment payment) {
        ensureOpen();
        if (isEmpty()) {
            throw new IllegalStateException("Cannot pay for an empty transaction.");
        }
        if (promotionResult == null) {
            throw new IllegalStateException("Promotions must be evaluated before payment.");
        }
        this.payment = payment;
        this.status = TransactionStatus.PAID;
    }

    
    void restorePayment(Payment savedPayment) {
        this.payment = savedPayment;
        this.status = TransactionStatus.PAID;
    }

    public void cancel() {
        ensureOpen();
        this.status = TransactionStatus.CANCELLED;
    }

    private void ensureOpen() {
        if (status != TransactionStatus.OPEN) {
            throw new IllegalStateException("Transaction " + transactionId + " is " + status
                    + " and can no longer be changed.");
        }
    }

    // ------------------------------------------------------------------ getters

    public String getTransactionId() {
        return transactionId;
    }

    public Customer getCustomer() {
        return customer;
    }

    public Booking getBooking() {
        return booking;
    }

    public LocalDate getServiceDate() {
        return serviceDate;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public int getParticipants() {
        return participants;
    }

    public TransactionStatus getStatus() {
        return status;
    }

    public PromotionResult getPromotionResult() {
        return promotionResult;
    }

    public Payment getPayment() {
        return payment;
    }

    // ------------------------------------------------------------------ output

   
    public String generateBill() {
        String thick = InputHelper.repeat('=', WIDTH);
        String thin = InputHelper.repeat('-', WIDTH);
        StringBuilder sb = new StringBuilder();

        sb.append(thick).append('\n');
        sb.append(center("ACTIVEHUB SPORTS CENTRE")).append('\n');
        sb.append(center(status == TransactionStatus.PAID ? "OFFICIAL RECEIPT" : "CUSTOMER BILL")).append('\n');
        sb.append(thick).append('\n');
        sb.append(String.format("Transaction ID : %-22s Status : %s%n", transactionId, status));
        sb.append(String.format("Customer       : %s%n", customer));
        if (booking != null) {
            sb.append(String.format("Booking        : %s - %s%n", booking.getBookingId(), booking.getFacility()));
            sb.append(String.format("Session        : %s (%s) %s - %s%n", serviceDate, serviceDate.getDayOfWeek(),
                    booking.getStartTime(), booking.getEndTime()));
        } else {
            sb.append("Booking        : Walk-in (no facility booking)\n");
            sb.append(String.format("Session        : %s (%s) %s%n", serviceDate, serviceDate.getDayOfWeek(), startTime));
        }
        sb.append(String.format("Participants   : %d%n", participants));
        sb.append(thin).append('\n');
        sb.append(String.format("%-6s %-28s %-10s %4s %10s %11s%n",
                "Code", "Item", "Category", "Qty", "Unit(RM)", "Amount(RM)"));
        sb.append(thin).append('\n');
        if (items.isEmpty()) {
            sb.append("  (no items)\n");
        }
        for (TransactionItem line : items) {
            RentalItem item = line.getItem();
            String name = item.getItemName();
            if (name.length() > 28) {
                name = name.substring(0, 27) + ".";
            }
            sb.append(String.format("%-6s %-28s %-10s %4d %10.2f %11.2f%n", item.getItemCode(), name,
                    item.getCategory().getShortLabel(), line.getQuantity(), line.getUnitPrice(), line.getSubtotal()));
        }
        sb.append(thin).append('\n');
        sb.append(amountLine("Subtotal", getSubtotal()));
        sb.append(String.format("  (Facility RM%.2f | Equipment RM%.2f [%d unit(s)] | Accessory/Service RM%.2f)%n",
                getFacilityCharge(), getChargeByCategory(ItemCategory.EQUIPMENT), getEquipmentQuantity(),
                getChargeByCategory(ItemCategory.ACCESSORY) + getChargeByCategory(ItemCategory.SERVICE)));
        sb.append(thin).append('\n');

        if (promotionResult == null) {
            sb.append("Promotions     : Not evaluated yet\n");
        } else {
            sb.append("Eligible promotions:\n");
            if (promotionResult.getEligibleSavings().isEmpty()) {
                sb.append("   None - this transaction does not qualify for any promotion.\n");
            }
            for (Map.Entry<Promotion, Double> entry : promotionResult.getEligibleSavings().entrySet()) {
                Promotion p = entry.getKey();
                sb.append(String.format("   [%s] %-34s saving RM%8.2f%n", p.getCode(), p.getName(), entry.getValue()));
            }
            Promotion selected = promotionResult.getSelectedPromotion();
            sb.append("Selected promotion : ")
                    .append(selected == null ? "None" : "[" + selected.getCode() + "] " + selected.getName()
                            + " (greatest saving)")
                    .append('\n');
        }
        sb.append(String.format("%-58s -RM%10.2f%n", "Discount", getDiscount()));
        sb.append(amountLine("Amount after discount", getAmountAfterDiscount()));
        sb.append(amountLine("Service charge (10%)", getServiceCharge()));
        sb.append(thick).append('\n');
        sb.append(amountLine("FINAL PAYABLE AMOUNT", getFinalAmount()));
        sb.append(thick).append('\n');

        if (payment != null) {
            sb.append("Payment ID     : ").append(payment.getPaymentId()).append('\n');
            sb.append("Payment method : ").append(payment.getMethodName()).append('\n');
            sb.append(payment.getPaymentDetails());
            sb.append("Paid on        : ").append(payment.getFormattedDateTime()).append('\n');
            sb.append(thick).append('\n');
            sb.append(center("Thank you and have a great game!")).append('\n');
        }
        return sb.toString();
    }

    private static String amountLine(String label, double amount) {
        return String.format("%-58s  RM%10.2f%n", label, amount);
    }

    private static String center(String text) {
        int pad = Math.max(0, (WIDTH - text.length()) / 2);
        return InputHelper.repeat(' ', pad) + text;
    }

    
    public String toSummaryRow() {
        return String.format("%-6s %-18s %-7s %-10s %5d %10.2f %10.2f  %-9s %s",
                transactionId,
                customer.getName().length() > 18 ? customer.getName().substring(0, 17) + "." : customer.getName(),
                booking == null ? "WALK-IN" : booking.getBookingId(),
                serviceDate, items.size(), getSubtotal(), getFinalAmount(), status,
                payment == null ? "-" : payment.getMethodName());
    }

    @Override
    public String toFileLine() {
        return transactionId + "|" + customer.getName() + "|" + customer.getContactNumber() + "|"
                + (booking == null ? "-" : booking.getBookingId()) + "|" + serviceDate + "|" + startTime + "|"
                + participants + "|" + status.name();
    }
}
