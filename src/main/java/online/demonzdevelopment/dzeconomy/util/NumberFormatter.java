package online.demonzdevelopment.dzeconomy.util;

import java.text.DecimalFormat;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Thread-safe number formatting utility.
 * Uses ThreadLocal for DecimalFormat to ensure thread safety.
 */
public class NumberFormatter {
    
    private static final ThreadLocal<DecimalFormat> DECIMAL_FORMAT = 
        ThreadLocal.withInitial(() -> new DecimalFormat("#,##0.00"));
    
    private static final ThreadLocal<DecimalFormat> INTEGER_FORMAT =
        ThreadLocal.withInitial(() -> new DecimalFormat("#,##0"));
    
    private static final LinkedHashMap<String, Double> SUFFIXES = new LinkedHashMap<>();
    static {
        SUFFIXES.put("K", 1_000.0);
        SUFFIXES.put("M", 1_000_000.0);
        SUFFIXES.put("B", 1_000_000_000.0);
        SUFFIXES.put("T", 1_000_000_000_000.0);
        SUFFIXES.put("Qa", 1e15);
        SUFFIXES.put("Qi", 1e18);
    }
    
    public static String formatFull(double amount) {
        return DECIMAL_FORMAT.get().format(amount);
    }
    
    public static String formatInteger(double amount) {
        return INTEGER_FORMAT.get().format(amount);
    }
    
    public static String formatShort(double amount) {
        if (amount < 0) return "-" + formatShort(-amount);
        if (amount < 1000) return INTEGER_FORMAT.get().format(amount);
        
        for (Map.Entry<String, Double> entry : SUFFIXES.entrySet()) {
            if (amount >= entry.getValue()) {
                double truncated = amount / entry.getValue();
                double roundedTruncated = Math.round(truncated * 100.0) / 100.0;
                if (roundedTruncated >= 1000) continue;
                return DECIMAL_FORMAT.get().format(truncated) + entry.getKey();
            }
        }
        return DECIMAL_FORMAT.get().format(amount);
    }
    
    public static double parse(String input) throws NumberFormatException {
        if (input == null || input.trim().isEmpty()) {
            throw new NumberFormatException("Empty input");
        }
        
        input = input.trim().replace(",", "");
        
        // Check for suffix
        for (Map.Entry<String, Double> entry : SUFFIXES.entrySet()) {
            String suffix = entry.getKey();
            if (input.toLowerCase().endsWith(suffix.toLowerCase())) {
                String numberPart = input.substring(0, input.length() - suffix.length());
                return Double.parseDouble(numberPart) * entry.getValue();
            }
        }
        
        return Double.parseDouble(input);
    }
}
