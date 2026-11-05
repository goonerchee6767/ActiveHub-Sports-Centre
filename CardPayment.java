import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Random;

/**
 * CardPayment - credit or debit card payment.
 * Only the last 4 digits of the card number are stored (masked).
 */
public class CardPayment extends Payment {

    private static final Random RANDOM = new Random();
    private static final DateTimeFormatter EXPIRY_FORMAT = DateTimeFormatter.ofPattern("MM/yy");

    private String cardType;
    private String maskedCardNumber;
    private String approvalCode;

    public CardPayment(String paymentId, String transactionId, double amount) {
        super(paymentId, transactionId, amount);
    }

    /** Constructor used when loading from file. */
    CardPayment(String paymentId, String transactionId, double amount, String cardType,
                String maskedCardNumber, String approvalCode) {
        super(paymentId, transactionId, amount);
        this.cardType = cardType;
        this.maskedCardNumber = maskedCardNumber;
        this.approvalCode = approvalCode;
    }

    @Override
    public String getMethodName() {
        return "Credit/Debit Card";
    }

    @Override
    public String getMethodCode() {
        return "CARD";
    }

    @Override
    public boolean processPayment(InputHelper input) {
        System.out.println("Card type: 1. Credit Card   2. Debit Card   0. Cancel");
        int type = input.readInt("Select card type: ", 0, 2);
        if (type == 0) {
            return false;
        }
        cardType = type == 1 ? "Credit" : "Debit";

        String number;
        while (true) {
            number = input.readLine("16-digit card number (0 = cancel): ").replaceAll("[\\s-]", "");
            if (number.equals("0")) {
                return false;
            }
            if (number.matches("\\d{16}")) {
                break;
            }
            System.out.println("  >> Card number must contain exactly 16 digits.");
        }

        while (true) {
            String text = input.readLine("Expiry date (MM/yy): ");
            try {
                YearMonth expiry = YearMonth.parse(text, EXPIRY_FORMAT);
                if (expiry.isBefore(YearMonth.now())) {
                    System.out.println("  >> This card has expired. Please use another card or method.");
                    return false;
                }
                break;
            } catch (DateTimeParseException e) {
                System.out.println("  >> Invalid expiry date. Example: 08/28");
            }
        }

        while (true) {
            String cvv = input.readLine("CVV (3 digits): ");
            if (cvv.matches("\\d{3}")) {
                break;
            }
            System.out.println("  >> CVV must be 3 digits.");
        }

        maskedCardNumber = "**** **** **** " + number.substring(12);
        approvalCode = String.format("%06d", RANDOM.nextInt(1000000));
        System.out.println("  Authorising " + Money.format(getAmount()) + " on " + cardType + " card "
                + maskedCardNumber + " ... APPROVED (code " + approvalCode + ")");
        return true;
    }

    @Override
    public String getPaymentDetails() {
        return "Card           : " + cardType + " " + maskedCardNumber + "\n"
                + "Approval code  : " + approvalCode + "\n";
    }

    @Override
    protected String getExtraFileFields() {
        return cardType + "|" + maskedCardNumber + "|" + approvalCode;
    }
}
