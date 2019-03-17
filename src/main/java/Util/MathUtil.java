package Util;

public class MathUtil {

    public static int roundToNearestInteger(double dec) {
        int floor = (int) Math.floor(dec);

        double distanceDown = dec - floor;
        double distanceUp = floor + 1 - dec;

        if (distanceDown < distanceUp) {
            return floor;
        } else {
            return floor + 1;
        }
    }

    public static void main(String[] args) {
        System.out.println(roundToNearestInteger(1.49));
        System.out.println(roundToNearestInteger(2.0));
    }
}
