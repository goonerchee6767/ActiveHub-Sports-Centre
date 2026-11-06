/**
 * Promotion B - Team Booking Reward.

 */
public class TeamBookingPromotion extends Promotion {

    private static final int MIN_PARTICIPANTS = 8;
    private static final double RATE_PER_PARTICIPANT = 5.0;
    private static final double CAP = 60.0;

    public TeamBookingPromotion() {
        super("B", "Team Booking Reward");
    }

    @Override
    public boolean isEligible(RentalTransaction tx) {
        return tx.getBooking() != null
                && tx.hasFacility()
                && tx.getParticipants() >= MIN_PARTICIPANTS;
    }

    @Override
    public double calculateSaving(RentalTransaction tx) {
        if (!isEligible(tx)) {
            return 0;
        }
        double reward = tx.getParticipants() * RATE_PER_PARTICIPANT;
        reward = Math.min(reward, CAP);
        reward = Math.min(reward, tx.getFacilityCharge());
        return Money.round(reward);
    }

    @Override
    public String getDescription() {
        return "RM5 per participant for 8+ participants (max RM60, not below RM0 facility charge)";
    }
}
