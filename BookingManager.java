import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * BookingManager - manages facilities and bookings and prevents double booking.
 *
 * AGGREGATION: holds a list of Facility objects and a list of Booking objects.
 */
public class BookingManager {

    public static final LocalTime OPENING_TIME = LocalTime.of(8, 0);
    public static final LocalTime CLOSING_TIME = LocalTime.of(22, 0);
    public static final int MAX_DURATION_HOURS = 4;
    public static final int MAX_ADVANCE_DAYS = 60;

    private final List<Facility> facilities = new ArrayList<>();
    private final List<Booking> bookings = new ArrayList<>();

    // ------------------------------------------------------------------ facilities

    public void addFacility(Facility facility) {
        if (findFacility(facility.getFacilityId()) != null) {
            throw new IllegalArgumentException("Duplicate facility ID: " + facility.getFacilityId());
        }
        facilities.add(facility);
    }

    public Facility findFacility(String facilityId) {
        if (facilityId == null) {
            return null;
        }
        for (Facility f : facilities) {
            if (f.getFacilityId().equalsIgnoreCase(facilityId.trim())) {
                return f;
            }
        }
        return null;
    }

    public List<Facility> getFacilities() {
        return Collections.unmodifiableList(facilities);
    }

    public List<Facility> getFacilitiesByType(FacilityType type) {
        List<Facility> result = new ArrayList<>();
        for (Facility f : facilities) {
            if (f.getType() == type) {
                result.add(f);
            }
        }
        return result;
    }

    // ------------------------------------------------------------------ validation

    /**
     * Validates a requested time slot using java.time comparisons.
     * @return null if valid, otherwise an error message.
     */
    public String validateSlot(LocalDate date, LocalTime start, int durationHours) {
        LocalDate today = LocalDate.now();
        if (date.isBefore(today)) {
            return "Booking date cannot be in the past.";
        }
        if (date.isAfter(today.plusDays(MAX_ADVANCE_DAYS))) {
            return "Bookings can only be made up to " + MAX_ADVANCE_DAYS + " days in advance.";
        }
        if (durationHours < 1 || durationHours > MAX_DURATION_HOURS) {
            return "Duration must be between 1 and " + MAX_DURATION_HOURS + " hours.";
        }
        if (start.isBefore(OPENING_TIME)) {
            return "The centre opens at " + OPENING_TIME + ". Please choose a later start time.";
        }
        LocalTime latestStart = CLOSING_TIME.minusHours(durationHours);
        if (start.isAfter(latestStart)) {
            return "The centre closes at " + CLOSING_TIME + ". Latest start time for "
                    + durationHours + " hour(s) is " + latestStart + ".";
        }
        if (date.equals(today) && start.isBefore(LocalTime.now().minusMinutes(15))) {
            return "This start time has already passed today.";
        }
        return null;
    }

    /** Returns the existing booking that clashes with the requested slot, or null if free. */
    public Booking findConflict(Facility facility, LocalDate date, LocalTime start, int durationHours) {
        LocalTime end = start.plusHours(durationHours);
        for (Booking b : bookings) {
            if (b.overlapsWith(facility, date, start, end)) {
                return b;
            }
        }
        return null;
    }

    public boolean isAvailable(Facility facility, LocalDate date, LocalTime start, int durationHours) {
        return findConflict(facility, date, start, durationHours) == null;
    }

    public List<Facility> findAvailableFacilities(FacilityType type, LocalDate date, LocalTime start,
                                                  int durationHours) {
        List<Facility> result = new ArrayList<>();
        for (Facility f : getFacilitiesByType(type)) {
            if (isAvailable(f, date, start, durationHours)) {
                result.add(f);
            }
        }
        return result;
    }

    // ------------------------------------------------------------------ bookings

    /**
     * Creates and stores a new booking after validating the slot and
     * checking that the facility is not already booked (duplicate check).
     */
    public Booking createBooking(Customer customer, Facility facility, LocalDate date, LocalTime start,
                                 int durationHours, int participants) {
        String error = validateSlot(date, start, durationHours);
        if (error != null) {
            throw new IllegalArgumentException(error);
        }
        if (participants > facility.getType().getMaxParticipants()) {
            throw new IllegalArgumentException("Maximum participants for a "
                    + facility.getType().getDisplayName() + " is " + facility.getType().getMaxParticipants() + ".");
        }
        Booking conflict = findConflict(facility, date, start, durationHours);
        if (conflict != null) {
            throw new IllegalStateException("Duplicate booking! " + facility + " is already booked on "
                    + date + " from " + conflict.getStartTime() + " to " + conflict.getEndTime()
                    + " (" + conflict.getBookingId() + ").");
        }
        Booking booking = new Booking(generateBookingId(), customer, facility, date, start,
                durationHours, participants);
        bookings.add(booking);
        return booking;
    }

    /** Adds a booking that was loaded from the text file (no validation of past dates). */
    void addLoadedBooking(Booking booking) {
        bookings.add(booking);
    }

    public Booking findBooking(String bookingId) {
        if (bookingId == null) {
            return null;
        }
        for (Booking b : bookings) {
            if (b.getBookingId().equalsIgnoreCase(bookingId.trim())) {
                return b;
            }
        }
        return null;
    }

    /** All bookings sorted by date then start time. */
    public List<Booking> getBookings() {
        List<Booking> sorted = new ArrayList<>(bookings);
        sorted.sort(Comparator.comparing(Booking::getBookingDate).thenComparing(Booking::getStartTime));
        return sorted;
    }

    public List<Booking> getBookingsOn(LocalDate date) {
        List<Booking> result = new ArrayList<>();
        for (Booking b : getBookings()) {
            if (b.getBookingDate().equals(date)) {
                result.add(b);
            }
        }
        return result;
    }

    public List<Booking> getActiveBookings() {
        List<Booking> result = new ArrayList<>();
        for (Booking b : getBookings()) {
            if (b.isActive()) {
                result.add(b);
            }
        }
        return result;
    }

    private String generateBookingId() {
        int max = 0;
        for (Booking b : bookings) {
            try {
                max = Math.max(max, Integer.parseInt(b.getBookingId().substring(2)));
            } catch (RuntimeException ignored) {
                // ignore IDs that do not follow the BKnnnn pattern
            }
        }
        return String.format("BK%04d", max + 1);
    }
}
