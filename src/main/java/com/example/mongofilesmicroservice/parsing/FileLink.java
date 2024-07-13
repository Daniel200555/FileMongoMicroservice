package com.example.mongofilesmicroservice.parsing;

import java.util.ArrayList;
import java.util.List;

public class FileLink {

    public static List<String> parseLink(String dir) {
        System.out.println(dir);
        int index = 0;
        List<String> ids = new ArrayList<>();
        String temp = "";
        for (int i = 0; i < dir.length(); i++) {
            if (index != 0) {
                if ((dir.charAt(i) == '/' || dir.charAt(i) == '.')) {
                    ids.add(temp);
                    System.out.println(temp);
                    temp = "";
                } else {
                    System.out.println(temp);
                    temp = temp + dir.charAt(i);
                }
            } else {
                if (dir.charAt(i) == '/')
                    index++;
                temp = "";
            }
        }
        System.out.println(ids.size());
        return ids;
    }

}
