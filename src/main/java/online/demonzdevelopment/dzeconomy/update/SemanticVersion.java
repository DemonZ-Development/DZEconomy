package online.demonzdevelopment.dzeconomy.update;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SemanticVersion implements Comparable<SemanticVersion> {
    
    private static final Pattern VERSION_PATTERN = Pattern.compile("(\\d+)\\.(\\d+)\\.(\\d+)(?:-([a-zA-Z0-9._-]+))?");
    
    private final int major;
    private final int minor;
    private final int patch;
    private final String suffix;
    
    public SemanticVersion(String version) {
        Matcher matcher = VERSION_PATTERN.matcher(version.trim());
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Invalid semantic version: " + version);
        }
        this.major = Integer.parseInt(matcher.group(1));
        this.minor = Integer.parseInt(matcher.group(2));
        this.patch = Integer.parseInt(matcher.group(3));
        this.suffix = matcher.group(4); // may be null
    }
    
    public boolean isNewerThan(SemanticVersion other) {
        return this.compareTo(other) > 0;
    }
    
    @Override
    public int compareTo(SemanticVersion other) {
        int result = Integer.compare(this.major, other.major);
        if (result != 0) return result;
        result = Integer.compare(this.minor, other.minor);
        if (result != 0) return result;
        result = Integer.compare(this.patch, other.patch);
        if (result != 0) return result;
        
        // No suffix (release) > with suffix (pre-release)
        if (this.suffix == null && other.suffix != null) return 1;
        if (this.suffix != null && other.suffix == null) return -1;
        if (this.suffix != null && other.suffix != null) {
            return comparePrerelease(this.suffix, other.suffix);
        }
        return 0;
    }

    /**
     * Compare two semver pre-release identifiers (e.g. "beta.2" vs "rc.1").
     * Identifiers are compared dot-separated: numeric identifiers compare
     * numerically, alphanumeric identifiers compare lexically, and a set with
     * more identifiers is greater if all preceding identifiers are equal.
     */
    private int comparePrerelease(String a, String b) {
        String[] aParts = a.split("\\.");
        String[] bParts = b.split("\\.");
        int max = Math.min(aParts.length, bParts.length);
        for (int i = 0; i < max; i++) {
            int result = comparePrereleasePart(aParts[i], bParts[i]);
            if (result != 0) return result;
        }
        return Integer.compare(aParts.length, bParts.length);
    }

    private int comparePrereleasePart(String a, String b) {
        boolean aNumeric = isNumeric(a);
        boolean bNumeric = isNumeric(b);
        if (aNumeric && bNumeric) {
            // Compare as numbers, avoiding overflow on long identifier values
            java.math.BigInteger aNum = new java.math.BigInteger(a);
            java.math.BigInteger bNum = new java.math.BigInteger(b);
            return aNum.compareTo(bNum);
        }
        // Numeric identifiers always have lower precedence than alphanumeric ones
        if (aNumeric) return -1;
        if (bNumeric) return 1;
        return a.compareTo(b);
    }

    private boolean isNumeric(String s) {
        if (s.isEmpty()) return false;
        for (int i = 0; i < s.length(); i++) {
            if (!Character.isDigit(s.charAt(i))) return false;
        }
        return true;
    }
    
    @Override
    public String toString() {
        return major + "." + minor + "." + patch + (suffix != null ? "-" + suffix : "");
    }
}
