package org.simconsole.simconsole;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Client for the Audius Search API.
 * Free, no API key required, provides FULL tracks.
 * */
public class MusicApiService {

    private static final String HOST = "https://discoveryprovider.audius.co";
    private static final String APP_NAME = "simconsole";
    private static final HttpClient CLIENT = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();

    public static List<Tracks> search(String query, int limit) {
        List<Tracks> results = new ArrayList<>();
        try {
            String encoded = URLEncoder.encode(query, StandardCharsets.UTF_8);
            String url = HOST + "/v1/tracks/search?query=" + encoded + "&app_name=" + APP_NAME;

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Accept", "application/json")
                    .GET()
                    .build();

            HttpResponse<String> response = CLIENT.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                results = parseAudiusResults(response.body());
            } else {
                System.err.println("Audius API error: HTTP " + response.statusCode());
            }
        } catch (Exception e) {
            System.err.println("Audius API request failed: " + e.getMessage());
        }
        return results;
    }

    private static List<Tracks> parseAudiusResults(String json) {
        List<Tracks> tracks = new ArrayList<>();
        String[] blocks = json.split("\"id\":\"");
        
        for (int i = 1; i < blocks.length; i++) {
            String block = blocks[i];
            
            // Extract ID
            int quoteIdx = block.indexOf('"');
            if (quoteIdx < 0) continue;
            String id = block.substring(0, quoteIdx);

            String title = extractField(block, "title");
            
            // Artist name is inside user object
            String artist = "Unknown Artist";
            int userIdx = block.indexOf("\"user\":{");
            if (userIdx > 0) {
                artist = extractField(block.substring(userIdx), "name");
            }
            
            long durationSec = extractLong(block, "duration");
            long durationMs = durationSec * 1000L;
            
            String streamUrl = HOST + "/v1/tracks/" + id + "/stream?app_name=" + APP_NAME;
            
            if (title == null || title.isBlank()) continue;

            Tracks t = Tracks.fromApi(
                    artist,
                    title,
                    streamUrl,
                    durationMs
            );
            tracks.add(t);
        }
        return tracks;
    }

    /**
     * Downloads the full MP3 track from Audius to a temporary file.
     * Returns the absolute path of the downloaded file.
     */
    public static String downloadTrack(Tracks track) throws Exception {
        if (!track.isFromApi() || track.getPreviewUrl() == null) {
            return track.getFilePath(); // Already local
        }
        
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(track.getPreviewUrl()))
                .GET()
                .build();
                
        java.nio.file.Path tempFile = java.nio.file.Files.createTempFile("audius_", ".mp3");
        HttpResponse<java.nio.file.Path> response = CLIENT.send(request, HttpResponse.BodyHandlers.ofFile(tempFile));
        
        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            return tempFile.toAbsolutePath().toString();
        } else {
            throw new Exception("Download failed with HTTP " + response.statusCode());
        }
    }

    private static String extractField(String json, String key) {
        String marker = "\"" + key + "\":\"";
        int start = json.indexOf(marker);
        if (start < 0) return null;
        start += marker.length();
        int end = json.indexOf('"', start);
        while (end > 0 && json.charAt(end - 1) == '\\') {
            end = json.indexOf('"', end + 1);
        }
        if (end < 0) return null;
        return unescapeJsonString(json.substring(start, end));
    }

    private static String unescapeJsonString(String text) {
        if (text == null) return null;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == '\\' && i + 1 < text.length()) {
                char next = text.charAt(i + 1);
                if (next == 'u' && i + 5 < text.length()) {
                    String hex = text.substring(i + 2, i + 6);
                    try {
                        sb.append((char) Integer.parseInt(hex, 16));
                        i += 5;
                    } catch (NumberFormatException e) {
                        sb.append(c).append(next);
                        i++;
                    }
                } else {
                    if (next == '"') sb.append('"');
                    else if (next == '\\') sb.append('\\');
                    else if (next == '/') sb.append('/');
                    else if (next == 'b') sb.append('\b');
                    else if (next == 'f') sb.append('\f');
                    else if (next == 'n') sb.append('\n');
                    else if (next == 'r') sb.append('\r');
                    else if (next == 't') sb.append('\t');
                    else sb.append(c).append(next);
                    i++;
                }
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    private static long extractLong(String json, String key) {
        String marker = "\"" + key + "\":";
        int start = json.indexOf(marker);
        if (start < 0) return 0;
        start += marker.length();
        int end = start;
        while (end < json.length() && Character.isDigit(json.charAt(end))) end++;
        if (end == start) return 0;
        try { return Long.parseLong(json.substring(start, end)); }
        catch (NumberFormatException e) { return 0; }
    }
}
