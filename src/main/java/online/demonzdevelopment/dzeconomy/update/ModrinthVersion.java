package online.demonzdevelopment.dzeconomy.update;

/**
 * Represents a version from the Modrinth API.
 */
public class ModrinthVersion {
    
    private final String id;
    private final String versionNumber;
    
    public ModrinthVersion(String id, String versionNumber) {
        this.id = id;
        this.versionNumber = versionNumber;
    }
    
    public String getId() { return id; }
    public String getVersionNumber() { return versionNumber; }
}
