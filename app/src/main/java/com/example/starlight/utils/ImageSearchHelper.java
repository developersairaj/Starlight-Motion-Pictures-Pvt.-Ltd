package com.example.starlight.utils;

import android.graphics.Bitmap;
import android.util.Base64;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ImageSearchHelper {

    private static final String TAG = "ImageSearchHelper";

    private static final String API_KEY = "AIzaSyDDhBm11aWXyAaQcw91JO15chF7mWEpvEg";

    // gemini-2.5-flash — confirmed available on this key from the models list
    private static final String URL =
            "https://generativelanguage.googleapis.com/v1beta/models/"
                    + "gemini-2.5-flash:generateContent?key=" + API_KEY;

    private static final String PROMPT =
            "Look at this image. What movie, TV show, anime, manga, drama, or cartoon is this? "
                    + "Reply with ONLY the exact title. Nothing else. No explanation. "
                    + "If unknown, say UNKNOWN.";

    public static volatile String lastError = "";

    private static final OkHttpClient CLIENT = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build();

    public static String detectFromImage(Bitmap bitmap) {
        lastError = "";
        try {
            // Scale to 512px wide
            Bitmap scaled = bitmap;
            if (bitmap.getWidth() > 512) {
                float scale = 512f / bitmap.getWidth();
                scaled = Bitmap.createScaledBitmap(bitmap,
                        512, (int)(bitmap.getHeight() * scale), true);
            }
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            scaled.compress(Bitmap.CompressFormat.JPEG, 85, baos);
            String base64 = Base64.encodeToString(baos.toByteArray(), Base64.NO_WRAP);

            // Build request
            JSONObject body = new JSONObject();
            body.put("contents", new JSONArray().put(
                    new JSONObject().put("parts", new JSONArray()
                            .put(new JSONObject()
                                    .put("inline_data", new JSONObject()
                                            .put("mime_type", "image/jpeg")
                                            .put("data", base64)))
                            .put(new JSONObject()
                                    .put("text", PROMPT))
                    )
            ));

            RequestBody reqBody = RequestBody.create(
                    body.toString(),
                    MediaType.parse("application/json; charset=utf-8"));

            Request request = new Request.Builder()
                    .url(URL)
                    .post(reqBody)
                    .addHeader("Content-Type", "application/json")
                    .build();

            try (Response resp = CLIENT.newCall(request).execute()) {
                int code = resp.code();
                String respStr = resp.body() != null ? resp.body().string() : "";

                Log.d(TAG, "HTTP " + code);
                Log.d(TAG, "Body: " + respStr.substring(0, Math.min(400, respStr.length())));

                if (code != 200) {
                    lastError = parseError(code, respStr);
                    return null;
                }

                JSONObject json = new JSONObject(respStr);
                JSONArray candidates = json.optJSONArray("candidates");
                if (candidates == null || candidates.length() == 0) return null;

                JSONObject content = candidates.getJSONObject(0).optJSONObject("content");
                if (content == null) return null;

                JSONArray parts = content.optJSONArray("parts");
                if (parts == null || parts.length() == 0) return null;

                String text = parts.getJSONObject(0).optString("text", "").trim();
                Log.d(TAG, "Gemini result: " + text);

                if (text.isEmpty() || text.equalsIgnoreCase("UNKNOWN")) {
                    lastError = "Image not recognized";
                    return null;
                }

                return text
                        .replaceAll("(?i)^(title|movie|show|film|series|anime)[:\\s]+", "")
                        .replaceAll("^\"|\"$|^'|'$|\\*+", "")
                        .replaceAll("\\n.*", "")
                        .trim();
            }

        } catch (Exception e) {
            lastError = e.getMessage();
            Log.e(TAG, "Error: " + e.getMessage(), e);
            return null;
        }
    }

    private static String parseError(int code, String body) {
        try {
            JSONObject err = new JSONObject(body).optJSONObject("error");
            if (err != null) {
                String msg = err.optString("message", "");
                if (code == 429) return "Quota exceeded — create new key at aistudio.google.com";
                if (code == 403) return "API key invalid";
                if (code == 404) return "Model not found";
                return "Error " + code + ": " + msg.substring(0, Math.min(80, msg.length()));
            }
        } catch (Exception ignored) {}
        return "HTTP " + code;
    }
}