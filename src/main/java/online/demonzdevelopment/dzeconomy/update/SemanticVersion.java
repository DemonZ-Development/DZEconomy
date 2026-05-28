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
            return this.suffix.compareTo(other.suffix);
        }
        return 0;
    }
    
    @Override
    public String toString() {
        return major + "." + minor + "." + patch + (suffix != null ? "-" + suffix : "");
    }
}
