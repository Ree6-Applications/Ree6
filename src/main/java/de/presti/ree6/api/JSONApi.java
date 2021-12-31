package de.presti.ree6.api;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

import de.presti.ree6.bot.BotInfo;
import de.presti.ree6.main.Main;
import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

@Deprecated(forRemoval = true, since = "1.5.0")
public class JSONApi {

	@SuppressWarnings("unused")
	public static JSONObject getData(Requests type, String url) {
		return getData(type, url, "", "");
	}
	@SuppressWarnings("unused")
	public static JSONArray getData2(Requests type, String url) {
		return getData2(type, url, "", "");
	}

	public static JSONArray getData2(Requests type, String url, String post, String authKey) {

		JSONArray j = new JSONArray("[]");
		BufferedInputStream i = null;

		try {

			URL u = new URL(url);
			HttpsURLConnection c = (HttpsURLConnection) u.openConnection();

			setInput(c);

			c.setRequestMethod(type.name());

			c.setUseCaches(false);
			c.setDefaultUseCaches(false);
			c.setConnectTimeout(5000);
			c.setReadTimeout(5000);
			c.setRequestProperty("User-Agent",
					"Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/44.0.2403.52 Safari/537.36 Ree6/" + BotInfo.build);
			if(!authKey.isEmpty()) {
				c.setRequestProperty("Authorization", authKey);
			}
			c.setRequestProperty("Content-Type", "application/json-rpc");
			c.setRequestProperty("Content-length", "0");

			if (!post.isEmpty()) {
				c.setDoOutput(true);
			}

			c.connect();

			if (!post.isEmpty()) {
				try (BufferedOutputStream o = new BufferedOutputStream(c.getOutputStream())) {
					IOUtils.write(post, o);
				}
			}

			String content;

			if (c.getResponseCode() == 200) {
				i = new BufferedInputStream(c.getInputStream());
			} else {
				i = new BufferedInputStream(c.getErrorStream());
			}

			content = IOUtils.toString(i, c.getContentEncoding());
			j = new JSONArray(content);
		} catch (Exception ignore) {
		}

		if (i != null) {
			try {
				i.close();
			} catch (IOException ignore) {
			}
		}
		closeInput();
		return j;
	}

	@SuppressWarnings({ "null", "deprecation" })
    public static JSONObject getData(Requests type, String url, String post, String authKey) {

		JSONObject j = new JSONObject("{}");
		BufferedInputStream i = null;
		String rawContent = "";
		int available = 0;
		int responseCode = 0;

		try {

			URL u = new URL(url);
			HttpsURLConnection c = (HttpsURLConnection) u.openConnection();

			setInput(c);

			c.setRequestMethod(type.name());

			c.setUseCaches(false);
			c.setDefaultUseCaches(false);
			c.setConnectTimeout(5000);
			c.setReadTimeout(5000);
			c.setRequestProperty("User-Agent",
					"Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/44.0.2403.52 Safari/537.36 Ree6/" + BotInfo.build);
			if(!authKey.isEmpty()) {
				c.setRequestProperty("Authorization", authKey);
			}
			c.setRequestProperty("Content-Type", "application/json-rpc");
			c.setRequestProperty("Content-length", "0");

			if (!post.isEmpty()) {
				c.setDoOutput(true);
			}

			c.connect();

			if (!post.isEmpty()) {
				try (BufferedOutputStream o = new BufferedOutputStream(c.getOutputStream())) {
					IOUtils.write(post, o);
				}
			}

			String content;
			responseCode = c.getResponseCode();

			if (c.getResponseCode() == 200) {
				i = new BufferedInputStream(c.getInputStream());
			} else {
				i = new BufferedInputStream(c.getErrorStream());
			}

			content = IOUtils.toString(i, c.getContentEncoding());
			rawContent = content;
			j = new JSONObject(content);
			j.put("_success", true);
			j.put("_type", type.name());
			j.put("_url", url);
			j.put("_post", post);
			j.put("_http", c.getResponseCode());
			j.put("_available", available);
			j.put("_exception", "");
			j.put("_exceptionMessage", "");
			j.put("_content", content);
		} catch (JSONException ex) {
			if (ex.getMessage().contains("A JSONObject text must begin with")) {
				j = new JSONObject("{}");
				j.put("_success", true);
				j.put("_type", type.name());
				j.put("_url", url);
				j.put("_post", post);
				j.put("_http", 0);
				j.put("_available", available);
				j.put("_exception", "MalformedJSONData (HTTP " + responseCode + ")");
				j.put("_exceptionMessage", ex.getMessage());
				j.put("_content", rawContent);
			}
		} catch (NullPointerException ex) {
			Main.getInstance().getLogger().error("Error", ex.getCause());
		} catch (MalformedURLException ex) {
			j.put("_success", false);
			j.put("_type", type.name());
			j.put("_url", url);
			j.put("_post", post);
			j.put("_http", 0);
			j.put("_available", available);
			j.put("_exception", "MalformedURLException");
			j.put("_exceptionMessage", ex.getMessage());
			j.put("_content", "");
		} catch (SocketTimeoutException ex) {
			j.put("_success", false);
			j.put("_type", type.name());
			j.put("_url", url);
			j.put("_post", post);
			j.put("_http", 0);
			j.put("_available", available);
			j.put("_exception", "SocketTimeoutException");
			j.put("_exceptionMessage", ex.getMessage());
			j.put("_content", "");
		} catch (IOException ex) {
			j.put("_success", false);
			j.put("_type", type.name());
			j.put("_url", url);
			j.put("_post", post);
			j.put("_http", 0);
			j.put("_available", available);
			j.put("_exception", "IOException");
			j.put("_exceptionMessage", ex.getMessage());
			j.put("_content", "");
		} catch (Exception ex) {
			j.put("_success", false);
			j.put("_type", type.name());
			j.put("_url", url);
			j.put("_post", post);
			j.put("_http", 0);
			j.put("_available", available);
			j.put("_exception", "Exception [" + ex.getClass().getName() + "]");
			j.put("_exceptionMessage", ex.getMessage());
			j.put("_content", "");
		}

		if (i != null) {
			try {
				i.close();
			} catch (IOException ex) {
				j.put("_success", false);
				j.put("_type", type.name());
				j.put("_url", url);
				j.put("_post", post);
				j.put("_http", 0);
				j.put("_available", available);
				j.put("_exception", "IOException");
				j.put("_exceptionMessage", ex.getMessage());
				j.put("_content", "");
			}
		}
		closeInput();
		return j;
	}

	public static void setInput(HttpsURLConnection input) {
		JSONApi.input = input;
	}

	private static HttpsURLConnection input;

	public static HttpsURLConnection getInput() {
		return input;
	}

	public static void closeInput() {
		getInput().disconnect();
	}

	public enum Requests {

		GET

	}

}
