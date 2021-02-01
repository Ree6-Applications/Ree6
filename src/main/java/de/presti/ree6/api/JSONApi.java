package de.presti.ree6.api;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.Date;

import javax.net.ssl.HttpsURLConnection;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class JSONApi {

	@SuppressWarnings("unused")
	public static JSONObject GetData(Requests type, String url) {
		return GetData(type, url, "");
	}

	@SuppressWarnings("deprecation")
	static JSONArray GetData2(Requests type, String url, String post) {
		Date start = new Date();
		Date preconnect = start;
		Date postconnect = start;
		Date prejson = start;
		Date postjson = start;
		JSONArray j = new JSONArray("[]");
		BufferedInputStream i = null;
		String rawcontent = "";
		int available = 0;
		int responsecode = 0;
		long cl = 0;

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
					"Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/44.0.2403.52 Safari/537.36 QuorraBot/2015");
			c.setRequestProperty("Content-Type", "application/json-rpc");
			c.setRequestProperty("Content-length", "0");

			if (!post.isEmpty()) {
				c.setDoOutput(true);
			}

			preconnect = new Date();
			c.connect();
			postconnect = new Date();

			if (!post.isEmpty()) {
				try (BufferedOutputStream o = new BufferedOutputStream(c.getOutputStream())) {
					IOUtils.write(post, o);
				}
			}

			String content;
			cl = c.getContentLengthLong();
			responsecode = c.getResponseCode();

			if (c.getResponseCode() == 200) {
				i = new BufferedInputStream(c.getInputStream());
			} else {
				i = new BufferedInputStream(c.getErrorStream());
			}

			/*
			 * if (i != null) { available = i.available();
			 *
			 * while (available == 0 && (new Date().getTime() - postconnect.getTime()) <
			 * 450) { Thread.sleep(500); available = i.available(); }
			 *
			 * if (available == 0) { i = new BufferedInputStream(c.getErrorStream());
			 *
			 * if (i != null) { available = i.available(); } } }
			 *
			 * if (available == 0) { content = "{}"; } else { content = IOUtils.toString(i,
			 * c.getContentEncoding()); }
			 */
			content = IOUtils.toString(i, c.getContentEncoding());
			rawcontent = content;
			prejson = new Date();
			j = new JSONArray(content);
			postjson = new Date();
		} catch (JSONException ex) {
			ex.printStackTrace();
		} catch (NullPointerException ex) {
			ex.printStackTrace();
		} catch (MalformedURLException ex) {

			ex.printStackTrace();

		} catch (SocketTimeoutException ex) {

			ex.printStackTrace();

		} catch (IOException ex) {

			ex.printStackTrace();

		} catch (Exception ex) {

			ex.printStackTrace();
		}

		if (i != null) {
			try {
				i.close();
			} catch (IOException ex) {

				ex.printStackTrace();
			}
		}
		closeInput();
		return j;
	}

	@SuppressWarnings({ "null", "deprecation" })
	private static JSONObject GetData(Requests type, String url, String post) {
		Date start = new Date();
		Date preconnect = start;
		Date postconnect = start;
		Date prejson = start;
		Date postjson = start;
		JSONObject j = new JSONObject("{}");
		BufferedInputStream i = null;
		String rawcontent = "";
		int available = 0;
		int responsecode = 0;
		long cl = 0;

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
					"Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/44.0.2403.52 Safari/537.36 QuorraBot/2015");
			c.setRequestProperty("Content-Type", "application/json-rpc");
			c.setRequestProperty("Content-length", "0");

			if (!post.isEmpty()) {
				c.setDoOutput(true);
			}

			preconnect = new Date();
			c.connect();
			postconnect = new Date();

			if (!post.isEmpty()) {
				try (BufferedOutputStream o = new BufferedOutputStream(c.getOutputStream())) {
					IOUtils.write(post, o);
				}
			}

			String content;
			cl = c.getContentLengthLong();
			responsecode = c.getResponseCode();

			if (c.getResponseCode() == 200) {
				i = new BufferedInputStream(c.getInputStream());
			} else {
				i = new BufferedInputStream(c.getErrorStream());
			}

			/*
			 * if (i != null) { available = i.available();
			 *
			 * while (available == 0 && (new Date().getTime() - postconnect.getTime()) <
			 * 450) { Thread.sleep(500); available = i.available(); }
			 *
			 * if (available == 0) { i = new BufferedInputStream(c.getErrorStream());
			 *
			 * if (i != null) { available = i.available(); } } }
			 *
			 * if (available == 0) { content = "{}"; } else { content = IOUtils.toString(i,
			 * c.getContentEncoding()); }
			 */
			content = IOUtils.toString(i, c.getContentEncoding());
			rawcontent = content;
			prejson = new Date();
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
			postjson = new Date();
		} catch (JSONException ex) {
			if (ex.getMessage().contains("A JSONObject text must begin with")) {
				j = new JSONObject("{}");
				j.put("_success", true);
				j.put("_type", type.name());
				j.put("_url", url);
				j.put("_post", post);
				j.put("_http", 0);
				j.put("_available", available);
				j.put("_exception", "MalformedJSONData (HTTP " + responsecode + ")");
				j.put("_exceptionMessage", ex.getMessage());
				j.put("_content", rawcontent);
			} else {
				ex.printStackTrace();
			}
		} catch (NullPointerException ex) {
			ex.printStackTrace();
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

			ex.printStackTrace();

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

			ex.printStackTrace();

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

			ex.printStackTrace();

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

			ex.printStackTrace();
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

				ex.printStackTrace();
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

	public static void openinput() {
		try {
			getInput().connect();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void closeInput() {
		getInput().disconnect();
	}

}
