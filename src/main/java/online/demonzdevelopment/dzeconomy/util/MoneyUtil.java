package online.demonzdevelopment.dzeconomy.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class MoneyUtil {

    public static final int DEFAULT_SCALE = 2;

    public static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_UP;

    private MoneyUtil() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static double round(double value, int scale) {
        if (!Double.isFinite(value)) {
            return value;
        }
        return BigDecimal.valueOf(value).setScale(scale, ROUNDING_MODE).doubleValue();
    }

    public static double round(double value) {
        return round(value, DEFAULT_SCALE);
    }

    public static double add(double a, double b) {
        if (!Double.isFinite(a) || !Double.isFinite(b)) {
            return a + b;
        }
        return BigDecimal.valueOf(a)
                .add(BigDecimal.valueOf(b))
                .setScale(DEFAULT_SCALE, ROUNDING_MODE)
                .doubleValue();
    }

    public static double subtract(double a, double b) {
        if (!Double.isFinite(a) || !Double.isFinite(b)) {
            return a - b;
        }
        return BigDecimal.valueOf(a)
                .subtract(BigDecimal.valueOf(b))
                .setScale(DEFAULT_SCALE, ROUNDING_MODE)
                .doubleValue();
    }

    public static double multiply(double a, double b) {
        if (!Double.isFinite(a) || !Double.isFinite(b)) {
            return a * b;
        }
        return BigDecimal.valueOf(a)
                .multiply(BigDecimal.valueOf(b))
                .setScale(DEFAULT_SCALE, ROUNDING_MODE)
                .doubleValue();
    }

    public static int compare(double a, double b) {
        if (!Double.isFinite(a) || !Double.isFinite(b)) {
            return Double.compare(a, b);
        }
        return Double.compare(round(a), round(b));
    }
}
