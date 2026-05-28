package online.demonzdevelopment.dzeconomy.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests for {@link MoneyUtil}.
 * Verifies that BigDecimal-based arithmetic avoids floating-point
 * precision errors (e.g. 0.1 + 0.2 == 0.3).
 */
class MoneyUtilTest {

    // ====================================================================
    // Precision — the core purpose of MoneyUtil
    // ====================================================================

    @Nested
    @DisplayName("Floating-point precision")
    class PrecisionTests {

        @Test
        @DisplayName("0.1 + 0.2 == 0.30 (not 0.30000000000000004)")
        void testAdditionPrecision() {
            double nativeResult = 0.1 + 0.2;
            // Native double: 0.30000000000000004
            assertThat(nativeResult).isNotEqualTo(0.3);

            double moneyResult = MoneyUtil.add(0.1, 0.2);
            assertThat(moneyResult).isEqualTo(0.30);
        }

        @Test
        @DisplayName("0.3 - 0.1 == 0.20 (not 0.19999999999999998)")
        void testSubtractionPrecision() {
            double moneyResult = MoneyUtil.subtract(0.3, 0.1);
            assertThat(moneyResult).isEqualTo(0.20);
        }

        @Test
        @DisplayName("0.1 * 3 == 0.30 (not 0.30000000000000004)")
        void testMultiplicationPrecision() {
            double nativeResult = 0.1 * 3;
            assertThat(nativeResult).isNotEqualTo(0.3);

            double moneyResult = MoneyUtil.multiply(0.1, 3.0);
            assertThat(moneyResult).isEqualTo(0.30);
        }

        @Test
        @DisplayName("1.0 / 3.0 == 0.33 (rounded to 2 decimal places)")
        void testDivisionPrecision() {
            double moneyResult = MoneyUtil.divide(1.0, 3.0);
            assertThat(moneyResult).isEqualTo(0.33);
        }
    }

    // ====================================================================
    // Rounding
    // ====================================================================

    @Nested
    @DisplayName("Rounding")
    class RoundingTests {

        @Test
        @DisplayName("round(1.005, 2) == 1.01 (HALF_UP)")
        void testRoundingHalfUp() {
            assertThat(MoneyUtil.round(1.005, 2)).isEqualTo(1.01);
        }

        @Test
        @DisplayName("round(1.004, 2) == 1.00")
        void testRoundingDown() {
            assertThat(MoneyUtil.round(1.004, 2)).isEqualTo(1.0);
        }

        @Test
        @DisplayName("round(999.999, 2) == 1000.00")
        void testRoundingCarry() {
            assertThat(MoneyUtil.round(999.999, 2)).isEqualTo(1000.0);
        }
    }

    // ====================================================================
    // Comparison
    // ====================================================================

    @Nested
    @DisplayName("Comparison")
    class ComparisonTests {

        @Test
        @DisplayName("0.1 + 0.2 equals 0.3 after rounding")
        void testEqualsPrecision() {
            assertThat(MoneyUtil.equals(0.1 + 0.2, 0.3)).isTrue();
        }

        @Test
        @DisplayName("compare(10.0, 5.0) > 0")
        void testCompareGreater() {
            assertThat(MoneyUtil.compare(10.0, 5.0)).isGreaterThan(0);
        }

        @Test
        @DisplayName("compare(5.0, 10.0) < 0")
        void testCompareLess() {
            assertThat(MoneyUtil.compare(5.0, 10.0)).isLessThan(0);
        }

        @Test
        @DisplayName("compare(5.0, 5.0) == 0")
        void testCompareEqual() {
            assertThat(MoneyUtil.compare(5.0, 5.0)).isEqualTo(0);
        }
    }

    // ====================================================================
    // Utility
    // ====================================================================

    @Nested
    @DisplayName("Utility operations")
    class UtilityTests {

        @Test
        @DisplayName("clampToZero(-5.0) == 0.0")
        void testClampNegative() {
            assertThat(MoneyUtil.clampToZero(-5.0)).isEqualTo(0.0);
        }

        @Test
        @DisplayName("clampToZero(10.0) == 10.0")
        void testClampPositive() {
            assertThat(MoneyUtil.clampToZero(10.0)).isEqualTo(10.0);
        }

        @Test
        @DisplayName("max(3.0, 7.0) == 7.0")
        void testMax() {
            assertThat(MoneyUtil.max(3.0, 7.0)).isEqualTo(7.0);
        }

        @Test
        @DisplayName("min(3.0, 7.0) == 3.0")
        void testMin() {
            assertThat(MoneyUtil.min(3.0, 7.0)).isEqualTo(3.0);
        }

        @Test
        @DisplayName("format(1234.5) == '1,234.50'")
        void testFormat() {
            assertThat(MoneyUtil.format(1234.5)).isEqualTo("1,234.50");
        }

        @Test
        @DisplayName("format(0.0) == '0.00'")
        void testFormatZero() {
            assertThat(MoneyUtil.format(0.0)).isEqualTo("0.00");
        }

        @Test
        @DisplayName("Division by zero throws ArithmeticException")
        void testDivisionByZero() {
            assertThatThrownBy(() -> MoneyUtil.divide(1.0, 0.0))
                    .isInstanceOf(ArithmeticException.class)
                    .hasMessageContaining("Division by zero");
        }
    }
}
