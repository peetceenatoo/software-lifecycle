package com.polimi.PPP.CodeKataBattle.Utilities;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

public class URLTrimmer {

    public static String extractRepoPath(String urlString) {
        URI url;
        try{
            url = new URI(urlString);
        } catch (Exception e) {
            throw new IllegalArgumentException("URL is malformed.");
        }

        String path = url.getPath();

        // Remove leading "/" if present
        if (path.startsWith("/")) {
            path = path.substring(1);
        }

        String[] parts = path.split("/");
        if (parts.length < 2) {
            throw new IllegalArgumentException("URL does not contain enough parts to extract the desired information.");
        }

        // Concatenate the second-to-last and last parts
        return parts[parts.length - 2] + "/" + parts[parts.length - 1];
    }
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
