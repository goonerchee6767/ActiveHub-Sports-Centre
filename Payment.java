import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

/**
 * Payment - ABSTRACT base class for all payment methods.
 *
 * POLYMORPHISM: the system creates a CashPayment, CardPayment or EWalletPayment
 * but stores it in a Payment variable and calls payment.processPayment(...).
 * Each subclass collects and validates its own details.
 */
public abstract class Payment implements Storable {

    private static final DateTimeFormatter DISPLAY_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final String paymentId;
    private final String transactionId;
    private final double amount;
    private LocalDateTime paymentDateTime;

    protected Payment(String paymentId, String transactionId, double amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Payment amount cannot be negative.");
        }
        this.paymentId = paymentId;
        this.transactionId = transactionId;
        this.amount = Money.round(amount);
        this.paymentDateTime = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
    }

    public String getPaymentId() {
        return paymentId;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public double getAmount() {
        return amount;
    }

    public LocalDateTime getPaymentDateTime() {
        return paymentDateTime;
    }

    public String getFormattedDateTime() {
        return paymentDateTime.format(DISPLAY_FORMAT);
    }

    void setPaymentDateTime(LocalDateTime dateTime) {
        this.paymentDateTime = dateTime;
    }

    /** Display name e.g. "Cash". */
    public abstract String getMethodName();

    /** Code used in the text file e.g. "CASH". */
    public abstract String getMethodCode();

    /**
     * Collects and validates the details needed for this payment method.
     * @return true if payment succeeded, false if the staff cancelled it.
     */
    public abstract boolean processPayment(InputHelper input);

    /** Extra lines printed on the receipt (each line ends with a newline). */
    public abstract String getPaymentDetails();

    /** Method-specific fields for the text file (pipe-delimited). */
    protected abstract String getExtraFileFields();

    @Override
    public String toFileLine() {
        return paymentId + "|" + transactionId + "|" + getMethodCode() + "|"
                + String.format("%.2f", amount) + "|" + paymentDateTime + "|" + getExtraFileFields();
    }

    /** Factory method - rebuilds the correct Payment subclass from a saved file line. */
    public static Payment fromFileLine(String line) {
        String[] p = line.split("\\|", -1);
        String id = p[0];
        String txId = p[1];
        String method = p[2];
        double amount = Double.parseDouble(p[3]);
        LocalDateTime dateTime = LocalDateTime.parse(p[4]);

        Payment payment;
        switch (method) {
            case "CASH":
                payment = new CashPayment(id, txId, amount, Double.parseDouble(p[5]), Double.parseDouble(p[6]));
                break;
            case "CARD":
                payment = new CardPayment(id, txId, amount, p[5], p[6], p[7]);
                break;
            case "EWALLET":
                payment = new EWalletPayment(id, txId, amount, p[5], p[6], p[7]);
                break;
            default:
                throw new IllegalArgumentException("Unknown payment method: " + method);
        }
        payment.setPaymentDateTime(dateTime);
        return payment;
    }
}
