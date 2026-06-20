package com.tzy.utils;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import java.io.IOException;

public class Spider {
    public static String crawl(String url) {
        try {
            Connection.Response response = Jsoup.connect(url)
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/119.0.0.0 Safari/537.36")
                .ignoreContentType(true)
                .execute();

            return response.body();
        } catch (IOException e) {
            return "Error fetching the page: " + e.getMessage();
        }
    }
}
