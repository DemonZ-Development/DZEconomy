package online.demonzdevelopment.dzeconomy.update;

/**
 * Represents a version from the Modrinth API.
 */
public class ModrinthVersion {
    
    private final String id;
    private final String versionNumber;
    private final String changelog;
    private final String downloadUrl;
    private final String fileName;
    
    public ModrinthVersion(String id, String versionNumber, String changelog, String downloadUrl, String fileName) {
        this.id = id;
        this.versionNumber = versionNumber;
        this.changelog = changelog;
        this.downloadUrl = downloadUrl;
        this.fileName = fileName;
    }
    
    public String getId() { return id; }
    public String getVersionNumber() { return versionNumber; }
    public String getChangelog() { return changelog; }
    public String getDownloadUrl() { return downloadUrl; }
    public String getFileName() { return fileName; }
}
