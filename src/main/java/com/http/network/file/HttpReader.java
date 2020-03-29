package com.http.network.file;

import java.io.File;

public class HttpReader {

   public static int readContentLength(File file) {
        int length = (int) file.length();
        return length;
    }

   public static String readContentType(String file) {
        if (file.endsWith(".html"))
            return "text/html";
        else if (file.endsWith(".xml"))
            return "text/xml";
        else if (file.endsWith(".json"))
            return "application/json";
        else if (file.endsWith(".plain"))
            return "text/plain";
        else if (file.endsWith(".txt"))
            return "text/plain";
        return null;
    }
}
