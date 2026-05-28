package online.demonzdevelopment.dzeconomy.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Utility class for precise monetary calculations.
 * Uses {@link BigDecimal} internally to avoid floating-point precision errors
 * that can occur with {@code double} arithmetic (e.g. 0.1 + 0.2 != 0.3).
 * <p>
 * All balance operations in DZEconomy should route through this class
 * to ensure consistent rounding and precision.
 *
 * @since 2.0.0
 */
public final class MoneyUtil {

    /** Default number of decimal places for currency. */
    public static final int DEFAULT_SCALE = 2;

    /** Rounding mode used for all monetary calculations. */
    public static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_UP;

    private MoneyUtil() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * Rounds a double value to the specified number of decimal places.
     *
     * @param value the value to round
     * @param scale the number of decimal places
     * @return the rounded value
     */
    public static double round(double value, int scale) {
        if (!Double.isFinite(value)) {
            return value;
        }
        return BigDecimal.valueOf(value).setScale(scale, ROUNDING_MODE).doubleValue();
    }

    /**
     * Rounds a double value to the default number of decimal places (2).
     *
     * @param value the value to round
     * @return the rounded value
     */
    public static double round(double value) {
        return round(value, DEFAULT_SCALE);
    }

    /**
     * Adds two monetary values with proper precision.
     *
     * @param a the first value
     * @param b the second value
     * @return the sum, rounded to default scale
     */
    public static double add(double a, double b) {
        if (!Double.isFinite(a) || !Double.isFinite(b)) {
            return a + b;
        }
        return BigDecimal.valueOf(a)
                .add(BigDecimal.valueOf(b))
                .setScale(DEFAULT_SCALE, ROUNDING_MODE)
                .doubleValue();
    }

    /**
     * Subtracts two monetary values with proper precision.
     *
     * @param a the value to subtract from
     * @param b the value to subtract
     * @return the difference, rounded to default scale
     */
    public static double subtract(double a, double b) {
        if (!Double.isFinite(a) || !Double.isFinite(b)) {
            return a - b;
        }
        return BigDecimal.valueOf(a)
                .subtract(BigDecimal.valueOf(b))
                .setScale(DEFAULT_SCALE, ROUNDING_MODE)
                .doubleValue();
    }

    /**
     * Multiplies two monetary values with proper precision.
     *
     * @param a the first value
     * @param b the second value
     * @return the product, rounded to default scale
     */
    public static double multiply(double a, double b) {
        if (!Double.isFinite(a) || !Double.isFinite(b)) {
            return a * b;
        }
        return BigDecimal.valueOf(a)
                .multiply(BigDecimal.valueOf(b))
                .setScale(DEFAULT_SCALE, ROUNDING_MODE)
                .doubleValue();
    }

    /**
     * Divides two monetary values with proper precision.
     *
     * @param a the dividend
     * @param b the divisor
     * @return the quotient, rounded to default scale
     * @throws ArithmeticException if divisor is zero
     */
    public static double divide(double a, double b) {
        if (b == 0) throw new ArithmeticException("Division by zero");
        if (!Double.isFinite(a) || !Double.isFinite(b)) {
            return a / b;
        }
        return BigDecimal.valueOf(a)
                .divide(BigDecimal.valueOf(b), DEFAULT_SCALE, ROUNDING_MODE)
                .doubleValue();
    }

    /**
     * Checks if two monetary values are equal within floating-point tolerance.
     *
     * @param a the first value
     * @param b the second value
     * @return true if the values are equal after rounding
     */
    public static boolean equals(double a, double b) {
        return compare(a, b) == 0;
    }

    /**
     * Compares two monetary values after rounding.
     *
     * @param a the first value
     * @param b the second value
     * @return -1 if a &lt; b, 0 if equal, 1 if a &gt; b
     */
    public static int compare(double a, double b) {
        if (!Double.isFinite(a) || !Double.isFinite(b)) {
            return Double.compare(a, b);
        }
        return Double.compare(round(a), round(b));
    }

    /**
     * Returns the greater of two monetary values.
     *
     * @param a the first value
     * @param b the second value
     * @return the greater value
     */
    public static double max(double a, double b) {
        int cmp = compare(a, b);
        if (cmp != 0) {
            return cmp > 0 ? a : b;
        }
        return Double.compare(a, b) >= 0 ? a : b;
    }

    /**
     * Returns the lesser of two monetary values.
     *
     * @param a the first value
     * @param b the second value
     * @return the lesser value
     */
    public static double min(double a, double b) {
        int cmp = compare(a, b);
        if (cmp != 0) {
            return cmp < 0 ? a : b;
        }
        return Double.compare(a, b) <= 0 ? a : b;
    }

    /**
     * Ensures a value is not negative (clamps to zero).
     *
     * @param value the value to clamp
     * @return the value if positive, 0.0 otherwise
     */
    public static double clampToZero(double value) {
        return value < 0 ? 0.0 : round(value);
    }

    /**
     * Formats a monetary value for display.
     *
     * @param value the value to format
     * @param decimalPlaces the number of decimal places
     * @return formatted string (e.g. "1,000.00")
     */
    public static String format(double value, int decimalPlaces) {
        if (decimalPlaces < 0) decimalPlaces = 0;
        return String.format("%,." + decimalPlaces + "f", round(value, decimalPlaces));
    }

    /**
     * Formats a monetary value with default decimal places.
     *
     * @param value the value to format
     * @return formatted string (e.g. "1,000.00")
     */
    public static String format(double value) {
        return format(value, DEFAULT_SCALE);
    }
}
