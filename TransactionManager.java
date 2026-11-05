import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * TransactionManager - creates, finds and lists rental transactions.
 * AGGREGATION: holds many RentalTransaction objects.
 */
public class TransactionManager {

    private final List<RentalTransaction> transactions = new ArrayList<>();

    public RentalTransaction createTransaction(Customer customer, Booking booking, LocalDate serviceDate,
                                               LocalTime startTime, int participants) {
        RentalTransaction tx = new RentalTransaction(generateTransactionId(), customer, booking,
                serviceDate, startTime, participants);
        transactions.add(tx);
        return tx;
    }

    void addLoadedTransaction(RentalTransaction tx) {
        transactions.add(tx);
    }

    public boolean removeTransaction(RentalTransaction tx) {
        return transactions.remove(tx);
    }

    public RentalTransaction findTransaction(String transactionId) {
        if (transactionId == null) {
            return null;
        }
        for (RentalTransaction tx : transactions) {
            if (tx.getTransactionId().equalsIgnoreCase(transactionId.trim())) {
                return tx;
            }
        }
        return null;
    }

    /** Returns the (not cancelled) transaction linked to a booking, or null. */
    public RentalTransaction findByBooking(String bookingId) {
        for (RentalTransaction tx : transactions) {
            if (tx.getBooking() != null && tx.getStatus() != TransactionStatus.CANCELLED
                    && tx.getBooking().getBookingId().equalsIgnoreCase(bookingId)) {
                return tx;
            }
        }
        return null;
    }

    public List<RentalTransaction> getTransactions() {
        return Collections.unmodifiableList(transactions);
    }

    public List<RentalTransaction> getOpenTransactions() {
        List<RentalTransaction> result = new ArrayList<>();
        for (RentalTransaction tx : transactions) {
            if (tx.getStatus() == TransactionStatus.OPEN) {
                result.add(tx);
            }
        }
        return result;
    }

    /** Transactions that were PAID on the given date. */
    public List<RentalTransaction> getCompletedTransactionsOn(LocalDate date) {
        List<RentalTransaction> result = new ArrayList<>();
        for (RentalTransaction tx : transactions) {
            if (tx.getStatus() == TransactionStatus.PAID && tx.getPayment() != null
                    && tx.getPayment().getPaymentDateTime().toLocalDate().equals(date)) {
                result.add(tx);
            }
        }
        return result;
    }

    /** OPEN transactions whose service date is the given date. */
    public List<RentalTransaction> getOpenTransactionsFor(LocalDate date) {
        List<RentalTransaction> result = new ArrayList<>();
        for (RentalTransaction tx : getOpenTransactions()) {
            if (tx.getServiceDate().equals(date)) {
                result.add(tx);
            }
        }
        return result;
    }

    public String generateTransactionId() {
        int max = 0;
        for (RentalTransaction tx : transactions) {
            max = Math.max(max, numberPart(tx.getTransactionId()));
        }
        return String.format("T%04d", max + 1);
    }

    public String generatePaymentId() {
        int max = 0;
        for (RentalTransaction tx : transactions) {
            if (tx.getPayment() != null) {
                max = Math.max(max, numberPart(tx.getPayment().getPaymentId()));
            }
        }
        return String.format("P%04d", max + 1);
    }

    private static int numberPart(String id) {
        try {
            return Integer.parseInt(id.substring(1));
        } catch (RuntimeException e) {
            return 0;
        }
    }
}
