package com.janne.webserverraspberrypi.util;

import java.util.Arrays;

public class BenqProjectorUtilitys {
    public static String parseStatusString(String status) {
        String[] seperatedLines = status.split("\n");
        String lastLine = seperatedLines[seperatedLines.length - 1];
        return lastLine.contains("*POW=ON#") ? "1" : "0";
    }
}
