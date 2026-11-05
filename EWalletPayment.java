import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * EWalletPayment - payment through an e-wallet provider.
 */
public class EWalletPayment extends Payment {

    private static final String[] PROVIDERS = {"Touch 'n Go eWallet", "GrabPay", "Boost", "ShopeePay"};

    private String provider;
    private String walletPhone;
    private String referenceNo;

    public EWalletPayment(String paymentId, String transactionId, double amount) {
        super(paymentId, transactionId, amount);
    }

    /** Constructor used when loading from file. */
    EWalletPayment(String paymentId, String transactionId, double amount, String provider,
                   String walletPhone, String referenceNo) {
        super(paymentId, transactionId, amount);
        this.provider = provider;
        this.walletPhone = walletPhone;
        this.referenceNo = referenceNo;
    }

    @Override
    public String getMethodName() {
        return "E-Wallet";
    }

    @Override
    public String getMethodCode() {
        return "EWALLET";
    }

    @Override
    public boolean processPayment(InputHelper input) {
        System.out.println("E-wallet providers:");
        for (int i = 0; i < PROVIDERS.length; i++) {
            System.out.println("  " + (i + 1) + ". " + PROVIDERS[i]);
        }
        System.out.println("  0. Cancel");
        int choice = input.readInt("Select provider: ", 0, PROVIDERS.length);
        if (choice == 0) {
            return false;
        }
        provider = PROVIDERS[choice - 1];
        walletPhone = input.readContactNumber("E-wallet registered phone number: ");

        System.out.println("  Please ask the customer to scan the ActiveHub QR code and pay "
                + Money.format(getAmount()) + ".");
        if (!input.confirm("Has the customer's payment been received? (Y/N): ")) {
            return false;
        }
        referenceNo = "EW" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyMMddHHmmss"));
        System.out.println("  Payment received via " + provider + ". Reference: " + referenceNo);
        return true;
    }

    @Override
    public String getPaymentDetails() {
        return "E-wallet       : " + provider + " (" + walletPhone + ")\n"
                + "Reference no.  : " + referenceNo + "\n";
    }

    @Override
    protected String getExtraFileFields() {
        return provider + "|" + walletPhone + "|" + referenceNo;
    }
}
