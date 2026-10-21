package com.freelance.ui;

/**
 * Session — stores the currently logged-in user's data and runtime configurations in memory.
 * Shared across all Swing panels via static fields.
 */
public final class Session {

    private Session() {}

    public static long   userId   = -1;
    public static String userName = "";
    public static String userEmail= "";
    public static String role     = "";   // "CLIENT" or "FREELANCER"
    public static int    profileCompletion = 0;
    public static String geminiApiKey = "";

    public static boolean isLoggedIn()   { return userId > 0; }
    public static boolean isClient()     { return "CLIENT".equals(role); }
    public static boolean isFreelancer() { return "FREELANCER".equals(role); }

    public static void set(String json) {
        userId            = ApiClient.jsonLong(json, "id");
        userName          = ApiClient.jsonStr(json, "name");
        userEmail         = ApiClient.jsonStr(json, "email");
        role              = ApiClient.jsonStr(json, "role");
        String pc         = ApiClient.jsonStr(json, "profileCompletion");
        profileCompletion = pc != null ? Integer.parseInt(pc) : 0;
    }

    public static void clear() {
        userId = -1; userName = ""; userEmail = ""; role = ""; profileCompletion = 0;
    }

    public static String firstName() {
        if (userName == null || userName.isBlank()) return "";
        return userName.split(" ")[0];
    }
}
