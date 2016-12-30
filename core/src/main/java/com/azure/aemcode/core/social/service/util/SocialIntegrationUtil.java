package com.azure.aemcode.core.social.service.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.Format;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.sling.commons.json.JSONArray;
import org.apache.sling.commons.json.JSONException;
import org.apache.sling.commons.json.JSONObject;
import org.slf4j.Logger;

public class SocialIntegrationUtil {

	private final String USER_AGENT = "Mozilla/5.0";
	private static final String TWITTER_AUTH_URL = "https://api.twitter.com/oauth2/token";
	private static final SimpleDateFormat fb_dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZZZZZ");
	private static final SimpleDateFormat rss_feed_format = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz");
	private static final SimpleDateFormat tw_dateFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss ZZZZZ yyyy");

	/**
	 * This method returns the access token for the facebook.
	 * 
	 * @param fb_app_secret
	 * @param fb_app_id
	 * @param log
	 * 
	 * @param request
	 * @throws IOException
	 * 
	 */
	public String getFacebookAccessToken(String fb_app_id, String fb_app_secret, Logger log) throws IOException {
		String facebookToken = "";
		if (StringUtils.isNotBlank(fb_app_id) && StringUtils.isNotBlank(fb_app_secret)) {
			String fbURL = "https://graph.facebook.com/oauth/access_token?client_id=" + fb_app_id + "&client_secret="
					+ fb_app_secret + "&grant_type=client_credentials";
			facebookToken = getConnectionResponseMethodGet(fbURL, log);
		}
		return facebookToken;
	}

	/**
	 * 
	 * @param token
	 * @param fb_icon_path
	 * @param log
	 * @param responseArray
	 * @param facebookPostsUrl
	 * @return
	 * @throws IOException
	 * @throws JSONException
	 */
	public JSONArray getFeedResponse(String userId, String fbIcon, String token, String postsUrl, int noOfResults,
			String fb_icon_path, Logger log) throws IOException, JSONException {
		String url = postsUrl.replace("{page}", userId) + token;
		String feedsJSON = getConnectionResponseMethodGet(url, log);
		return getFinalJSON(new JSONObject(feedsJSON), noOfResults, userId, fbIcon, fb_icon_path);
	}

	/**
	 * 
	 * @param jsonObject
	 * @param noOfResults
	 * @param fb_icon_path
	 * @return
	 * @throws JSONException
	 */
	private JSONArray getFinalJSON(JSONObject jsonObject, int noOfResults, String userId, String fbIcon,
			String fb_icon_path) throws JSONException {
		JSONArray responseArray = new JSONArray();
		if (StringUtils.isNotBlank(jsonObject.toString())) {
			JSONArray jsonArray = jsonObject.getJSONArray("data");
			for (int i = 0; i < noOfResults; i++) {
				JSONObject fbFeedObject = new JSONObject();
				JSONObject jsObject = jsonArray.getJSONObject(i);
				fbFeedObject.put("title", userId);
				if (jsObject.has("message")) {
					fbFeedObject.put("description", jsObject.get("message"));
				} else {
					fbFeedObject.put("description", "");
				}

				fbFeedObject.put("channelName", "Facebook");
				// obj.put("img", "");
				fbFeedObject.put("url", "http://www.facebook.com/" + userId);
				fbFeedObject.put("date", jsObject.get("created_time"));
				if (StringUtils.isNotBlank(fbIcon)) {
					fbFeedObject.put("channelIcon", fbIcon);
				} else {

					// setting default image path
					fbFeedObject.put("channelIcon", fb_icon_path);
				}
				responseArray.put(fbFeedObject);
			}
		}
		return responseArray;
	}

	/**
	 * This method returns the access token for twitter.
	 * 
	 * @param log
	 * @param tw_consumer_secret
	 * @param tw_consumer_id
	 * 
	 * @return
	 * @throws IOException
	 * @throws ClientProtocolException
	 * @throws JSONException
	 */
	public String getTwitterAccessToken(String tw_consumer_id, String tw_consumer_secret, Logger log)
			throws ClientProtocolException, IOException, JSONException {

		String access_token = "";
		// encoding the consumer key and secret using base 64
		String final_Consumer_key = tw_consumer_id + ":" + tw_consumer_secret;
		log.debug("Before Encoding: " + final_Consumer_key);
		final_Consumer_key = "Basic " + Base64.encodeBase64String(final_Consumer_key.getBytes());
		log.debug("Concatinated Key: " + final_Consumer_key);

		HttpClient client = HttpClientBuilder.create().build();
		HttpPost post = new HttpPost(TWITTER_AUTH_URL);

		// post request headers
		post.addHeader("Authorization", final_Consumer_key);
		post.addHeader("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");
		post.addHeader("Accept-Encoding", "gzip");
		post.addHeader("user-agent", USER_AGENT);

		// post request body values
		List<NameValuePair> parameters = new ArrayList<NameValuePair>();
		parameters.add(new BasicNameValuePair("grant_type", "client_credentials"));
		post.setEntity(new UrlEncodedFormEntity(parameters, "UTF-8"));

		ResponseHandler<String> responseHandler = new BasicResponseHandler();
		String execute = client.execute(post, responseHandler);

		JSONObject responseObj = new JSONObject(execute);
		if (responseObj.has("token_type") && responseObj.has("access_token")) {
			access_token = responseObj.getString("token_type") + " " + responseObj.getString("access_token");
		}
		return access_token;
	}

	/**
	 * 
	 * @param responseArray2
	 * @param feedCount
	 * @param log
	 * @return
	 * @throws JSONException
	 */
	public JSONArray getSortedResponseMap(JSONArray responseArray2, int feedCount, Logger log) throws JSONException {
		Map<Long, JSONObject> sortedMap = new TreeMap<Long, JSONObject>(Collections.reverseOrder());
		JSONArray response = new JSONArray();
		for (int i = 0; i < responseArray2.length(); i++) {
			JSONObject json = (JSONObject) responseArray2.get(i);
			if (json.has("date")) {
				Long dateValue = getDateValue((JSONObject) responseArray2.get(i), log);
				JSONObject setDateText = setDateText((JSONObject) responseArray2.get(i), log);
				sortedMap.put(dateValue, setDateText);
			}
		}
		Iterator<Long> iterator = sortedMap.keySet().iterator();
		int i = 0;
		// setting map values
		while (iterator.hasNext() && i < feedCount) {
			response.put(sortedMap.get(iterator.next()));
			i++;
		}
		return response;
	}

	/**
	 * 
	 * @param jsonObject
	 * @param log
	 * @return
	 * @throws JSONException
	 */
	private JSONObject setDateText(JSONObject jsonObject, Logger log) throws JSONException {
		JSONObject obj = new JSONObject();
		Long dateValue = getDateValue(jsonObject, log);
		obj = jsonObject;
		obj.put("date", timeAgoInWords(dateValue));
		return obj;
	}

	/**
	 * 
	 * @param from
	 * @return
	 */
	public static String timeAgoInWords(Long from) {
		Date now = new Date();
		long difference = now.getTime() - from;
		long distanceInMin = difference / 60000;

		if (0 <= distanceInMin && distanceInMin <= 1) {
			return "Less than 1 minute ago";
		} else if (1 <= distanceInMin && distanceInMin <= 45) {
			return distanceInMin + " minutes ago";
		} else if (45 <= distanceInMin && distanceInMin <= 89) {
			return "About 1 hour";
		} else if (90 <= distanceInMin && distanceInMin <= 1439) {
			return "About " + (distanceInMin / 60) + " hours ago";
		} else if (1440 <= distanceInMin && distanceInMin <= 2529) {
			return "1 day";
		} else if (2530 <= distanceInMin && distanceInMin <= 43199) {
			return (distanceInMin / 1440) + "days ago";
		} else if (43200 <= distanceInMin && distanceInMin <= 86399) {
			return "About 1 month ago";
		} else if (86400 <= distanceInMin && distanceInMin <= 525599) {
			return "About " + (distanceInMin / 43200) + " months ago";
		} else {
			long distanceInYears = distanceInMin / 525600;
			return "About " + distanceInYears + " years ago";
		}
	}

	/**
	 * 
	 * @param object
	 * @param log
	 * @return
	 * @throws JSONException
	 */
	private Long getDateValue(JSONObject object, Logger log) throws JSONException {
		Long date = 0L;
		String dateString = object.getString("date");
		try {
			if (object.has("channelName")) {
				if (object.getString("channelName").equals("Twitter")) {
					date = tw_dateFormat.parse(dateString).getTime();
				} else if (object.getString("channelName").equals("Facebook")) {
					date = fb_dateFormat.parse(dateString).getTime();
				} else if (object.getString("channelName").equals("RSS Feed")) {
					date = rss_feed_format.parse(dateString).getTime();
				}
			}
		} catch (ParseException e) {
			log.error("Exception Occured while parsing date: ", e);
		}
		return date;
	}

	/**
	 * 
	 * @param number
	 * @param numberFormat
	 * @return
	 */
	public String formatNumber(String number, String numberFormat) {
		String formatedNumber = number;
		Long num = Long.parseLong(number, 10);
		if (numberFormat.equals("formatone")) {
			formatedNumber = NumberFormat.getNumberInstance(Locale.JAPANESE).format(num);
		} else if (numberFormat.equals("formattwo")) {
			formatedNumber = NumberFormat.getNumberInstance(Locale.FRANCE).format(num);
		} else if (numberFormat.equals("formatthree")) {
			formatedNumber = NumberFormat.getNumberInstance(Locale.ITALY).format(num);
		} else if (numberFormat.equals("formatfour")) {
			Format format = NumberFormat.getCurrencyInstance(new Locale("en", "in"));
			String formNum = format.format(new BigDecimal(num));
			formatedNumber = formNum.split(" ")[1].split("\\.")[0];

		}
		return formatedNumber;
	}

	/**
	 * 
	 * @param fbURL
	 * @param log
	 * @return
	 * @throws IOException
	 */
	public String getConnectionResponseMethodGet(String fbURL, Logger log) throws IOException {
		StringBuffer response = new StringBuffer();
		URL obj = new URL(fbURL);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();

		// optional default is GET
		con.setRequestMethod("GET");

		// add request header
		con.setRequestProperty("User-Agent", USER_AGENT);

		int responseCode = con.getResponseCode();
		log.debug("\nSending 'GET' request to URL : " + fbURL);
		log.debug("Response Code : " + responseCode);

		BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
		String inputLine;
		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		in.close();
		return response.toString();
	}
}
