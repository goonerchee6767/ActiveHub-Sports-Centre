/**
 * Money - small utility class for rounding and formatting Ringgit amounts.
 * The constructor is private because this class only has static helpers.
 */
public final class Money {

    private Money() {
    }

    /** Rounds a value to 2 decimal places (sen). */
    public static double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    /** Formats a value as e.g. "RM12.50". */
    public static String format(double value) {
        return String.format("RM%.2f", value);
    }

    /** Converts an amount to whole sen so two amounts can be compared exactly. */
    public static long toSen(double value) {
        return Math.round(value * 100.0);
    }
}
