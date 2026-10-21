package com.freelance.ui;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * ApiClient — HTTP client that talks to the Spring Boot REST backend.
 * Uses only JDK built-ins (HttpURLConnection) so no extra dependencies are needed.
 * Base URL: http://localhost:8080
 */
public final class ApiClient {

    public static final String BASE = "http://localhost:8080";

    private ApiClient() {}

    // ── Generic helpers ────────────────────────────────────────────────────────

    public static String get(String path) throws IOException {
        URL url = toUrl(BASE + path);
        HttpURLConnection c = (HttpURLConnection) url.openConnection();
        c.setRequestMethod("GET");
        c.setRequestProperty("Accept", "application/json");
        c.setConnectTimeout(6000);
        c.setReadTimeout(15000);
        return readResponse(c);
    }

    public static String post(String path, String json) throws IOException {
        return send("POST", path, json);
    }

    public static String put(String path, String json) throws IOException {
        return send("PUT", path, json);
    }

    public static String delete(String path) throws IOException {
        URL url = toUrl(BASE + path);
        HttpURLConnection c = (HttpURLConnection) url.openConnection();
        c.setRequestMethod("DELETE");
        c.setRequestProperty("Accept", "application/json");
        c.setConnectTimeout(6000);
        c.setReadTimeout(10000);
        return readResponse(c);
    }

    /**
     * Upload a resume text file or multipart data to /api/resume/compatibility
     */
    public static String postMultipartResume(String path, long jobId, String filename, byte[] content) throws IOException {
        return postMultipartResume(path, jobId, filename, content, null);
    }

    public static String postMultipartResume(String path, long jobId, String filename, byte[] content, String geminiKey) throws IOException {
        String boundary = "===" + System.currentTimeMillis() + "===";
        String query = (path.contains("?") ? "&" : "?") + "jobId=" + jobId;
        if (geminiKey != null && !geminiKey.isBlank()) {
            query += "&geminiKey=" + URLEncoder.encode(geminiKey, StandardCharsets.UTF_8);
        }
        URL url = toUrl(BASE + path + query);
        HttpURLConnection c = (HttpURLConnection) url.openConnection();
        c.setRequestMethod("POST");
        c.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
        c.setRequestProperty("Accept", "application/json");
        c.setDoOutput(true);
        c.setConnectTimeout(8000);
        c.setReadTimeout(25000);

        try (OutputStream os = c.getOutputStream();
             PrintWriter writer = new PrintWriter(new OutputStreamWriter(os, StandardCharsets.UTF_8), true)) {
            
            // Resume file part
            writer.append("--").append(boundary).append("\r\n");
            writer.append("Content-Disposition: form-data; name=\"resume\"; filename=\"").append(filename).append("\"\r\n");
            writer.append("Content-Type: text/plain; charset=UTF-8\r\n\r\n").flush();
            os.write(content);
            os.flush();
            writer.append("\r\n").flush();

            // End boundary
            writer.append("--").append(boundary).append("--\r\n").flush();
        }

        return readResponse(c);
    }

    // ── Private helpers ────────────────────────────────────────────────────────

    private static URL toUrl(String fullUrl) throws IOException {
        try {
            return new URI(fullUrl).toURL();
        } catch (URISyntaxException | IllegalArgumentException e) {
            throw new IOException("Invalid URL: " + fullUrl, e);
        }
    }

    private static String send(String method, String path, String json) throws IOException {
        URL url = toUrl(BASE + path);
        HttpURLConnection c = (HttpURLConnection) url.openConnection();
        c.setRequestMethod(method);
        c.setRequestProperty("Content-Type", "application/json");
        c.setRequestProperty("Accept", "application/json");
        c.setDoOutput(true);
        c.setConnectTimeout(6000);
        c.setReadTimeout(15000);
        if (json != null && !json.isEmpty()) {
            try (OutputStream os = c.getOutputStream()) {
                os.write(json.getBytes(StandardCharsets.UTF_8));
            }
        }
        return readResponse(c);
    }

    private static String readResponse(HttpURLConnection c) throws IOException {
        int code = c.getResponseCode();
        InputStream is = (code >= 400) ? c.getErrorStream() : c.getInputStream();
        if (is == null) return "";
        try (BufferedReader r = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = r.readLine()) != null) sb.append(line);
            String body = sb.toString();
            if (code >= 400) throw new IOException("HTTP " + code + ": " + body);
            return body;
        }
    }

    // ── JSON parser helpers ────────────────────────────────────────────────────

    public static String esc(String s) {
        if (s == null) return "null";
        StringBuilder sb = new StringBuilder("\"");
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '"' -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '\b' -> sb.append("\\b");
                case '\f' -> sb.append("\\f");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                default -> {
                    if (c < 32 || c == 0x7f) {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
                }
            }
        }
        sb.append("\"");
        return sb.toString();
    }

    public static String jsonStr(String json, String key) {
        if (json == null) return null;
        String needle = "\"" + key + "\"";
        int idx = json.indexOf(needle);
        if (idx < 0) return null;
        int colon = json.indexOf(':', idx + needle.length());
        if (colon < 0) return null;
        int start = colon + 1;
        while (start < json.length() && (json.charAt(start) == ' ' || json.charAt(start) == '\t')) start++;
        if (start >= json.length()) return null;
        if (json.charAt(start) == '"') {
            int end = start + 1;
            while (end < json.length()) {
                char ch = json.charAt(end);
                if (ch == '"' && json.charAt(end - 1) != '\\') break;
                end++;
            }
            return json.substring(start + 1, end)
                       .replace("\\\"", "\"").replace("\\n", "\n").replace("\\\\", "\\");
        }
        int end = start;
        while (end < json.length() && ",}]".indexOf(json.charAt(end)) < 0) end++;
        String val = json.substring(start, end).trim();
        if ("null".equals(val)) return null;
        return val;
    }

    public static long jsonLong(String json, String key) {
        try { 
            String val = jsonStr(json, key);
            return val != null ? Long.parseLong(val) : -1; 
        } catch (Exception e) { return -1; }
    }

    public static double jsonDouble(String json, String key) {
        try { 
            String val = jsonStr(json, key);
            return val != null ? Double.parseDouble(val) : 0; 
        } catch (Exception e) { return 0; }
    }

    public static boolean jsonBool(String json, String key) {
        try {
            String val = jsonStr(json, key);
            return val != null && Boolean.parseBoolean(val);
        } catch (Exception e) { return false; }
    }

    public static List<String> jsonStrList(String json, String key) {
        List<String> list = new ArrayList<>();
        if (json == null) return list;
        String needle = "\"" + key + "\"";
        int idx = json.indexOf(needle);
        if (idx < 0) return list;
        int openBracket = json.indexOf('[', idx + needle.length());
        if (openBracket < 0) return list;
        int closeBracket = json.indexOf(']', openBracket);
        if (closeBracket < 0) return list;
        String arrContent = json.substring(openBracket + 1, closeBracket).trim();
        if (arrContent.isEmpty()) return list;

        boolean inStr = false;
        StringBuilder cur = new StringBuilder();
        for (int i = 0; i < arrContent.length(); i++) {
            char c = arrContent.charAt(i);
            if (c == '"' && (i == 0 || arrContent.charAt(i - 1) != '\\')) {
                if (inStr) {
                    list.add(cur.toString().replace("\\\"", "\"").replace("\\n", "\n").replace("\\\\", "\\"));
                    cur.setLength(0);
                    inStr = false;
                } else {
                    inStr = true;
                }
            } else if (inStr) {
                cur.append(c);
            }
        }
        return list;
    }

    public static String[] jsonArray(String json) {
        if (json == null || !json.trim().startsWith("[")) return new String[0];
        json = json.trim();
        List<String> items = new ArrayList<>();
        int depth = 0, start = -1;
        for (int i = 0; i < json.length(); i++) {
            char c = json.charAt(i);
            if (c == '{') {
                if (depth == 1) start = i;
                depth++;
            } else if (c == '}') {
                depth--;
                if (depth == 1 && start != -1) {
                    items.add(json.substring(start, i + 1));
                    start = -1;
                }
            } else if (c == '[') {
                depth++;
            } else if (c == ']') {
                depth--;
            }
        }
        return items.toArray(new String[0]);
    }
}
