package com.polimi.PPP.CodeKataBattle.Utilities;

public class URLTrimmer {
    public static String trimUrl(String url) {
        // Split the URL by '/'
        String[] parts = url.split("/");

        // StringBuilder to construct the final string
        StringBuilder result = new StringBuilder();

        // Check if the URL has more than two '/'
        if (parts.length > 2) {
            for (int i = 2; i < parts.length; i++) {
                result.append(parts[i]);
                // Append '/' to reconstruct URL, except for the last part
                if (i < parts.length - 1) {
                    result.append("/");
                }
            }
            return result.toString();
        } else {
            // Return the original URL if it doesn't have two '/'
            return url;
        }
    }
}
