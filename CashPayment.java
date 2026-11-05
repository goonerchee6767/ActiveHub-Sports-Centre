/**
 * CashPayment - cash payment with amount tendered and change.
 */
public class CashPayment extends Payment {

    private double amountTendered;
    private double change;

    public CashPayment(String paymentId, String transactionId, double amount) {
        super(paymentId, transactionId, amount);
    }

    /** Constructor used when loading from file. */
    CashPayment(String paymentId, String transactionId, double amount, double amountTendered, double change) {
        super(paymentId, transactionId, amount);
        this.amountTendered = amountTendered;
        this.change = change;
    }

    @Override
    public String getMethodName() {
        return "Cash";
    }

    @Override
    public String getMethodCode() {
        return "CASH";
    }

    @Override
    public boolean processPayment(InputHelper input) {
        System.out.println("Amount due: " + Money.format(getAmount()));
        while (true) {
            double tendered = input.readDouble("Cash received from customer (RM, 0 = cancel): ", 0, 100000);
            if (tendered == 0) {
                return false;
            }
            if (Money.toSen(tendered) < Money.toSen(getAmount())) {
                System.out.println("  >> Insufficient cash. Short by "
                        + Money.format(getAmount() - tendered) + ".");
                continue;
            }
            amountTendered = Money.round(tendered);
            change = Money.round(tendered - getAmount());
            System.out.println("  Change to return: " + Money.format(change));
            return true;
        }
    }

    @Override
    public String getPaymentDetails() {
        return String.format("Cash received  : RM%.2f%nChange         : RM%.2f%n", amountTendered, change);
    }

    @Override
    protected String getExtraFileFields() {
        return String.format("%.2f|%.2f", amountTendered, change);
    }
}
