package com.andonova.singit.helpers;

import android.util.Log;

import com.google.firebase.crashlytics.buildtools.reloc.org.apache.http.HttpResponse;
import com.google.firebase.crashlytics.buildtools.reloc.org.apache.http.client.methods.HttpGet;
import com.google.firebase.crashlytics.buildtools.reloc.org.apache.http.impl.client.CloseableHttpClient;
import com.google.firebase.crashlytics.buildtools.reloc.org.apache.http.impl.client.HttpClientBuilder;
import com.google.firebase.crashlytics.buildtools.reloc.org.apache.http.util.EntityUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;


/**
 * Utility class for downloading lyrics for a song from web.
 */
public class LyricsDownloader {

    private static final String TAG = "LyricsDownloader";

    private static String lyricsType = "lrc";

    /***
     * Getting the lyrics of a song.
     * @param songFileName - name of the song file for which the lyrics should be downloaded.
     * @return if found returns the lyric in String, otherwise returns empty String ("")
     */
    public static HashMap<String, String> findLyrics(String songFileName) {

        String lyrics;

        String url = null;
        try {
            url = "https://www.lyricsify.com/search?q=" + URLEncoder.encode(songFileName, "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        String html = "";
        if (url != null) {
            try (CloseableHttpClient httpClient = HttpClientBuilder.create().build()) {
                HttpGet request = new HttpGet(url);
                HttpResponse response = null;
                response = httpClient.execute(request);
                InputStream in = response.getEntity().getContent();
                BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                StringBuilder str = new StringBuilder();
                String line = null;
                while ((line = reader.readLine()) != null) {
                    str.append(line);
                }
                in.close();
                html = str.toString();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        Element titleLinkElement = null;
        if (!html.equals("")) {
            Document doc = Jsoup.parse(html);
            Elements elements = doc.getElementsByClass("title");

            for (Element e : elements) {
                if (e.tagName().equals("a")) {
                    titleLinkElement = e;
                    break;
                }
            }
        }

        if (titleLinkElement == null) {
            // 0 finds for a lrc files
            lyrics = textLyrics(songFileName);
        } else {
            // there is at least 1 result
            lyrics = lrcLyrics(titleLinkElement);
        }

        HashMap<String, String> result = new HashMap<>();
        result.put("lyrics", lyrics);
        result.put("type", lyricsType);

        return result;
    }


    private static String lrcLyrics(Element titleLinkElement) {

        lyricsType = "lrc";

        String secondPageHTML = "";
        String urlSongLyrics = titleLinkElement.attr("href");

        if (!urlSongLyrics.equals("")) {
            Log.d(TAG, urlSongLyrics);
            try (CloseableHttpClient httpClient = HttpClientBuilder.create().build()) {
                HttpGet request = new HttpGet("https://www.lyricsify.com" + urlSongLyrics);
                HttpResponse response;
                response = httpClient.execute(request);
                InputStream in = response.getEntity().getContent();
                BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                StringBuilder str = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    str.append(line);
                }
                in.close();
                secondPageHTML = str.toString();
            } catch (IOException e) {
                e.printStackTrace();
                Log.d(TAG, e.getMessage());
            }
        }

        if (!secondPageHTML.equals("")) {
            Document songLyricsPageDoc = Jsoup.parse(secondPageHTML);

            Element divElement = songLyricsPageDoc.getElementById("entry");
            if (divElement != null) {
                String lyricsHtml = divElement.html();
                return lyricsHtml.replace("<br>", System.lineSeparator());
            }
        }

        return "";
    }


    public static String textLyrics(String songFileName) {

        lyricsType = "txt";

        String songName = "";
        String songArtist = "";
        String resultLyrics = "";

        String url = null;
        try {
            url = "https://api.lyrics.ovh/suggest/" + URLEncoder.encode(songFileName, "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        if (url != null) {
            Log.d(TAG, url);

            try (CloseableHttpClient httpClient = HttpClientBuilder.create().build()) {
                HttpGet request = new HttpGet(url);
                HttpResponse response;
                response = httpClient.execute(request);
                String json_string = EntityUtils.toString(response.getEntity());
                JSONObject jsonLyrics = new JSONObject(json_string);
                try {
                    JSONArray data = jsonLyrics.getJSONArray("data");
                    if (data.length() > 0) {
                        // if data array (array of songs info) is not empty, get the first song
                        JSONObject song = data.getJSONObject(0);
                        songName = song.getString("title_short");
                        songArtist = song.getJSONObject("artist").getString("name");
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }

            if (!songName.equals("") && !songArtist.equals("")) {

                String urlLyrics = null;
                try {
                    urlLyrics = "https://api.lyrics.ovh/v1/"
                            + URLEncoder.encode(songArtist, "utf-8") + "/"
                            + URLEncoder.encode(songName, "utf-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }

                if (urlLyrics != null) {
                    Log.d(TAG, "Getting lyrics from " + urlLyrics);

                    try (CloseableHttpClient httpClient = HttpClientBuilder.create().build()) {
                        HttpGet request = new HttpGet(urlLyrics);
                        HttpResponse response;
                        response = httpClient.execute(request);
                        String jsonString = EntityUtils.toString(response.getEntity());
                        JSONObject jsonObject = new JSONObject(jsonString);
                        try {
                            resultLyrics = jsonObject.getString("lyrics");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    } catch (IOException | JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        return resultLyrics;
    }

}
