/**
 * TransactionStatus - life-cycle states of a rental transaction.
 * OPEN      : items can still be added / changed, not yet paid
 * PAID      : payment completed (counted in the daily report)
 * CANCELLED : booking was cancelled before payment
 */
public enum TransactionStatus {
    OPEN,
    PAID,
    CANCELLED
}
