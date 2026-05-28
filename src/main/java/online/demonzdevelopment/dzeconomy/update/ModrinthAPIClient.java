package online.demonzdevelopment.dzeconomy.update;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;

/**
 * Client for the Modrinth API v2.
 * Documentation: https://docs.modrinth.com/api
 */
public class ModrinthAPIClient {
    
    private static final String API_BASE = "https://api.modrinth.com/v2";
    private static final String USER_AGENT = "DZEconomy/2.1.1 (https://github.com/DemonZ-Development/DZEconomy)";
    
    private final String projectId;
    
    public ModrinthAPIClient(String projectId) {
        this.projectId = projectId;
    }
    
    public ModrinthVersion fetchLatestVersion() throws Exception {
        // Get project versions
        String url = API_BASE + "/project/" + projectId + "/version?game_versions=%5B%22any%22%5D&loaders=%5B%22paper%22,%22spigot%22,%22bukkit%22%5D";
        
        HttpURLConnection conn = (HttpURLConnection) URI.create(url).toURL().openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("User-Agent", USER_AGENT);
        conn.setConnectTimeout(10000);
        conn.setReadTimeout(10000);
        
        try {
            int responseCode = conn.getResponseCode();
            if (responseCode != 200) {
                try (java.io.InputStream err = conn.getErrorStream()) {
                    if (err != null) {
                        byte[] buffer = new byte[1024];
                        while (err.read(buffer) != -1) {}
                    }
                }
                throw new RuntimeException("Modrinth API returned status " + responseCode);
            }
            
            StringBuilder response = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
            }
            
            JsonArray versions = JsonParser.parseString(response.toString()).getAsJsonArray();
            if (versions.size() == 0) return null;
            
            // Get the first (latest) version
            JsonObject latest = versions.get(0).getAsJsonObject();
            
            String versionNumber = latest.get("version_number").getAsString();
            String versionId = latest.get("id").getAsString();
            String changelog = latest.has("changelog") ? latest.get("changelog").getAsString() : "";
            
            // Get download URL from first file
            String downloadUrl = "";
            String fileName = "";
            if (latest.has("files")) {
                JsonArray files = latest.getAsJsonArray("files");
                if (files.size() > 0) {
                    JsonObject file = files.get(0).getAsJsonObject();
                    downloadUrl = file.has("url") ? file.get("url").getAsString() : "";
                    fileName = file.has("filename") ? file.get("filename").getAsString() : "";
                }
            }
            
            return new ModrinthVersion(versionId, versionNumber, changelog, downloadUrl, fileName);
        } finally {
            conn.disconnect();
        }
    }
}
