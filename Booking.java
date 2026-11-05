import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Booking - a reservation of one Facility by one Customer for a time slot.
 *
 * Dates and times are stored using java.time (LocalDate / LocalTime / DayOfWeek),
 * NOT strings, so they can be compared correctly for double-booking checks
 * and promotion eligibility.
 *
 * ASSOCIATION: Booking -> Customer (1) and Booking -> Facility (1).
 */
public class Booking implements Storable {

    private final String bookingId;
    private final Customer customer;
    private final Facility facility;
    private final LocalDate bookingDate;
    private final LocalTime startTime;
    private final int durationHours;
    private final int participants;
    private BookingStatus status;

    public Booking(String bookingId, Customer customer, Facility facility, LocalDate bookingDate,
                   LocalTime startTime, int durationHours, int participants) {
        if (customer == null || facility == null || bookingDate == null || startTime == null) {
            throw new IllegalArgumentException("Booking details are incomplete.");
        }
        if (durationHours <= 0) {
            throw new IllegalArgumentException("Duration must be at least 1 hour.");
        }
        if (participants <= 0) {
            throw new IllegalArgumentException("Number of participants must be at least 1.");
        }
        this.bookingId = bookingId;
        this.customer = customer;
        this.facility = facility;
        this.bookingDate = bookingDate;
        this.startTime = startTime;
        this.durationHours = durationHours;
        this.participants = participants;
        this.status = BookingStatus.CONFIRMED;
    }

    public String getBookingId() {
        return bookingId;
    }

    public Customer getCustomer() {
        return customer;
    }

    public Facility getFacility() {
        return facility;
    }

    public LocalDate getBookingDate() {
        return bookingDate;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public LocalTime getEndTime() {
        return startTime.plusHours(durationHours);
    }

    public DayOfWeek getDayOfWeek() {
        return bookingDate.getDayOfWeek();
    }

    public int getDurationHours() {
        return durationHours;
    }

    public int getParticipants() {
        return participants;
    }

    public BookingStatus getStatus() {
        return status;
    }

    public boolean isActive() {
        return status == BookingStatus.CONFIRMED;
    }

    /**
     * Returns true if this (active) booking uses the same facility on the same date
     * and its time range overlaps [start, end).
     * Two ranges overlap when: newStart < existingEnd AND existingStart < newEnd.
     */
    public boolean overlapsWith(Facility otherFacility, LocalDate date, LocalTime start, LocalTime end) {
        return isActive()
                && facility.getFacilityId().equals(otherFacility.getFacilityId())
                && bookingDate.equals(date)
                && start.isBefore(getEndTime())
                && startTime.isBefore(end);
    }

    public void cancel() {
        if (!isActive()) {
            throw new IllegalStateException("Booking " + bookingId + " is already cancelled.");
        }
        status = BookingStatus.CANCELLED;
    }

    /** Used only when loading saved data from the text file. */
    void restoreStatus(BookingStatus savedStatus) {
        this.status = savedStatus;
    }

    @Override
    public String toFileLine() {
        return bookingId + "|" + customer.getName() + "|" + customer.getContactNumber() + "|"
                + facility.getFacilityId() + "|" + bookingDate + "|" + startTime + "|"
                + durationHours + "|" + participants + "|" + status.name();
    }

    @Override
    public String toString() {
        return String.format("%-7s %-18s %-12s %-4s %-10s %-9s %s-%s %3d %4d  %s",
                bookingId, shorten(customer.getName(), 18), customer.getContactNumber(),
                facility.getFacilityId(), bookingDate, getDayOfWeek(), startTime, getEndTime(),
                durationHours, participants, status);
    }

    private static String shorten(String text, int max) {
        return text.length() <= max ? text : text.substring(0, max - 1) + ".";
    }
}
