/**
 * TransactionStatus - life-cycle states of a rental transaction.
 * OPEN      : items can still be added / changed, not yet paid
 * PAID      : payment completed 
 * CANCELLED : booking was cancelled before payment
 */
public enum TransactionStatus {
    OPEN,
    PAID,
    CANCELLED
}
