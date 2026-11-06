
public final class Money {

    private Money() {
    }

   
    public static double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    
    public static String format(double value) {
        return String.format("RM%.2f", value);
    }

  
    public static long toSen(double value) {
        return Math.round(value * 100.0);
    }
}
