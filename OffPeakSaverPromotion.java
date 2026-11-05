import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Promotion A - Off-Peak Saver.
 * 15% off FACILITY / COURT charges only, for bookings starting Monday-Friday,
 * at or after 09:00 and before 16:00 (15:59 eligible, 16:00 NOT eligible).
 * Eligibility uses LocalDate, LocalTime and DayOfWeek from java.time.
 */
public class OffPeakSaverPromotion extends Promotion {

    private static final double RATE = 0.15;
    private static final LocalTime OFF_PEAK_START = LocalTime.of(9, 0);
    private static final LocalTime OFF_PEAK_END = LocalTime.of(16, 0);

    public OffPeakSaverPromotion() {
        super("A", "Off-Peak Saver");
    }

    @Override
    public boolean isEligible(RentalTransaction tx) {
        if (!tx.hasFacility()) {
            return false;
        }
        LocalDate date = tx.getServiceDate();
        LocalTime start = tx.getStartTime();
        DayOfWeek day = date.getDayOfWeek();
        boolean weekday = day != DayOfWeek.SATURDAY && day != DayOfWeek.SUNDAY;
        boolean offPeakTime = !start.isBefore(OFF_PEAK_START) && start.isBefore(OFF_PEAK_END);
        return weekday && offPeakTime;
    }

    @Override
    public double calculateSaving(RentalTransaction tx) {
        if (!isEligible(tx)) {
            return 0;
        }
        return Money.round(tx.getFacilityCharge() * RATE);
    }

    @Override
    public String getDescription() {
        return "15% off facility/court charges, Mon-Fri, start 09:00 to before 16:00";
    }
}
