/*
 *    This file is part of the Map Link mod
 *    licensed under the GNU GPL v3 License.
 *    (some parts of this file are originally from "RemotePlayers" by ewpratten)
 *
 *    Copyright (C) 2024 - 2025  Leander Knüttel and contributors
 *
 *    This program is free software: you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation, either version 3 of the License, or
 *    (at your option) any later version.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package de.the_build_craft.maplink.common;

import com.google.gson.Gson;
import com.mojang.blaze3d.platform.NativeImage;

import javax.net.ssl.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;

import static de.the_build_craft.maplink.common.CommonModConfig.config;

/**
 * HTTP utils
 *
 * @author ewpratten
 * @author Leander Knüttel
 * @author eatmyvenom
 * @version 26.08.2025
 */
public class HTTP {

    private static Charset getResponseCharset(HttpURLConnection request) {
        String contentType = request.getContentType();
        if (contentType != null) {
            // e.g. "application/json; charset=utf-8"
            String[] parts = contentType.split(";");
            for (String part : parts) {
                String trimmed = part.trim();
                if (trimmed.regionMatches(true, 0, "charset=", 0, "charset=".length())) {
                    String charsetName = trimmed.substring("charset=".length()).trim();
                    // strip optional quotes
                    if (charsetName.length() >= 2 && charsetName.startsWith("\"") && charsetName.endsWith("\"")) {
                        charsetName = charsetName.substring(1, charsetName.length() - 1);
                    }
                    try {
                        return Charset.forName(charsetName);
                    } catch (Exception ignored) {
                        return StandardCharsets.UTF_8;
                    }
                }
            }
        }
        // Bluemap JSON is UTF-8; never fall back to platform default (GBK on many Windows installs)
        return StandardCharsets.UTF_8;
    }

    /**
     * Make an HTTP request, and deserialize
     *
     * @param <T>      Type
     * @param endpoint URL to request from
     * @param clazz    Type class
     * @return Deserialized object
     * @throws IOException
     */
    public static <T> T makeJSONHTTPRequest(URL endpoint, Class<T> clazz) throws IOException {
        // Turn to a Java object
        Gson gson = new Gson();
        return gson.fromJson(makeTextHttpRequest(endpoint), clazz);
    }

    /**
     * Make an HTTP request, and deserialize
     *
     * @param <T>      Type
     * @param endpoint URL to request from
     * @param apiResponseType Type class
     * @return Deserialized object
     * @throws IOException
     */
    public static <T> T makeJSONHTTPRequest(URL endpoint, Type apiResponseType) throws IOException {
        // Turn to a Java object
        Gson gson = new Gson();
        return gson.fromJson(makeTextHttpRequest(endpoint), apiResponseType);
    }

    public static String makeTextHttpRequest(URL url) throws IOException {
        return makeTextHttpRequest(url, false);
    }

    public static HttpURLConnection openHTTPConnection(URL url, String contentType) throws IOException {
        // Open an HTTP request
        HttpURLConnection request = (HttpURLConnection) url.openConnection();
        request.setRequestMethod("GET");
        request.setRequestProperty("Content-Type", contentType);
        request.setRequestProperty("User-Agent", AbstractModInitializer.MOD_NAME);
        request.setInstanceFollowRedirects(true);
        request.setConnectTimeout(10_000);
        request.setReadTimeout(10_000);
        // completely insecure and should normally not be used!
        if (config.general.ignoreCertificatesUseAtYourOwnRisk && request instanceof HttpsURLConnection) {
            try {
                SSLContext sc = SSLContext.getInstance("SSL");
                sc.init(null, new TrustManager[]{new TrustAnyTrustManager()}, new SecureRandom());
                HostnameVerifier allHostsValid = (hostname, session) -> true;
                ((HttpsURLConnection) request).setSSLSocketFactory(sc.getSocketFactory());
                ((HttpsURLConnection) request).setHostnameVerifier(allHostsValid);
            } catch (Exception ignored) {}
        }
        return request;
    }

    public static String makeTextHttpRequest(URL url, boolean includeNewLine) throws IOException{
        // Open an HTTP request
        HttpURLConnection request = openHTTPConnection(url, "application/json");

        // Get the content
        Charset charset = getResponseCharset(request);
        StringBuilder response = new StringBuilder();
        try (BufferedReader responseReader = new BufferedReader(new InputStreamReader(request.getInputStream(), charset))) {
            String output;
            while ((output = responseReader.readLine()) != null) {
                response.append(output);
                if (includeNewLine) response.append("\n");
            }
        }

        return response.toString();
    }

    public static NativeImage makeImageHttpRequest(URL url) throws IOException{
        // Open an HTTP request
        HttpURLConnection request = openHTTPConnection(url, "image/png");

        // Return the content
        return NativeImage.read(request.getInputStream());
    }

    /**
     * completely insecure and should normally not be used!
     */
    static class TrustAnyTrustManager implements X509TrustManager {

        public void checkClientTrusted(X509Certificate[] chain, String authType) {
        }

        public void checkServerTrusted(X509Certificate[] chain, String authType) {
        }

        public X509Certificate[] getAcceptedIssuers() {
            return null;
        }
    }
}