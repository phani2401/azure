package com.azure.aemcode.core.social.service.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Dictionary;

import org.apache.commons.lang3.StringUtils;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Service;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.sling.commons.json.JSONArray;
import org.apache.sling.commons.json.JSONException;
import org.apache.sling.commons.json.JSONObject;
import org.apache.sling.commons.json.xml.XML;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.azure.aemcode.core.social.service.SocialTickerService;
import com.azure.aemcode.core.social.service.util.SocialIntegrationUtil;


@Component(name = "com.adobe.gdc.ccr.services.impl.SocialTickerServiceImpl", label = "CCR - Social Ticker", metatype = true, immediate = true)
@Service(value = SocialTickerService.class)
@Properties({
		@Property(label = "Enter the app ID of the Facebook app", description = "This field takes the app id of the facebook app.", name = "property.fb_app_id", value = ""),
		@Property(label = "Enter the app secret of the Facebook app", description = "This field takes the app secret of the facebook app.", name = "property.fb_app_secret", value = ""),
		@Property(label = "Enter the facebook app image url", description = "This field takes the image for the facebook app.", name = "property.facebook_icon_path", value = ""),
		@Property(label = "Enter the app ID of the Twitter app", description = "This field takes the app id of the twitter app.", name = "property.twitter_app_id", value = ""),
		@Property(label = "Enter the app Secret of the Twitter app", description = "This field takes the app secret of the twitter app..", name = "property.twitter_app_secret", value = ""),
		@Property(label = "Enter the twitter app image path", description = "This field takes the image for the twitter feeds.", name = "property.twitter_icon_path", value = ""),
		@Property(label = "Enter the rss feed app image path", description = "This field takes the icon path for the rss feeds.", name = "property.rss_image_url", value = "") 
})

public class SocialTickerServiceImpl implements SocialTickerService {

	private Logger log = LoggerFactory.getLogger(SocialTickerServiceImpl.class);

	// constants
	private static final String FACEBOOK_APP_ID = "property.fb_app_id";
	private static final String FACEBOOK_APP_SECRET = "property.fb_app_secret";
	private static final String FACEBOOK_ICON_PATH = "property.facebook_icon_path";
	private static final String TWITTER_APP_ID = "property.twitter_app_id";
	private static final String TWITTER_APP_SECRET = "property.twitter_app_secret";
	private static final String TWITTER_ICON_PATH = "property.twitter_icon_path";
	private static final String RSS_ICON_PATH = "property.rss_image_url";
	private static final String FACEBOOK_POSTS_URL = "https://graph.facebook.com/v2.5/{page}/posts?";
	private static final String TWITTER_POSTS_URL = "https://api.twitter.com/1.1/statuses/user_timeline.json?exclude_replies=true&screen_name=";

	// strings
	private String fb_app_id = "";
	private String fb_app_secret = "";
	private String fb_icon_path = "";
	private String tw_consumer_id = "";
	private String tw_consumer_secret = "";
	private String tw_icon_path = "";
	private String rss_icon_path = "";
	
	SocialIntegrationUtil util = new SocialIntegrationUtil();

	/**
	 * This method calls the required method getFeedResponse for getting the
	 * facebook detais.
	 * 
	 * @param userId
	 * @param fbIcon
	 */
	public JSONArray getFacebookDetails(String userId, String fbIcon, String feedCount) {
		log.debug("Starting - get Facebook Details");
		if (StringUtils.isBlank(feedCount)) {
			feedCount = "4";
		}
		JSONArray facebookResponse = new JSONArray();
		try {
			String access_token_facebook = util.getFacebookAccessToken(fb_app_id, fb_app_secret, log);

			if (StringUtils.isNotBlank(access_token_facebook)) {
				log.debug("access token: {}", access_token_facebook);
				facebookResponse = util.getFeedResponse(userId, fbIcon, access_token_facebook, FACEBOOK_POSTS_URL,
						Integer.parseInt(feedCount), fb_icon_path, log);
			}
		} catch (JSONException e) {
			log.error("Exception Occured due to JSON: "+ e);
		} catch (IOException e) {
			log.error("Exception in IO: "+ e);
		}
		return facebookResponse;
	}

	/**
	 * 
	 * 
	 */
	public JSONArray getTwitterResponse(String twId, String twIcon, String feedCount) {
		log.debug("Starting - get twitter details");
		if (StringUtils.isBlank(feedCount)) {
			feedCount = "4";
		}
		JSONArray responseArray = new JSONArray();
		int feedsCount = Integer.parseInt(feedCount);
		try {
			String access_token_twitter = util.getTwitterAccessToken(tw_consumer_id, tw_consumer_secret, log);
			if (StringUtils.isNotBlank(access_token_twitter)) {
				log.debug("access token: {}", access_token_twitter);

				// After getting authorization bearer code the api requesr is given.
				HttpClient client1 = HttpClientBuilder.create().build();
				HttpGet get = new HttpGet(TWITTER_POSTS_URL + twId);
				get.addHeader("Authorization", access_token_twitter);
				ResponseHandler<String> responseHandler1 = new BasicResponseHandler();
				String twitterResponse = client1.execute(get, responseHandler1);
				JSONArray resp = new JSONArray(twitterResponse);
				for (int i = 0; i < feedsCount; i++) {
					JSONObject twFeedResponse = new JSONObject();
					JSONObject subObj = resp.getJSONObject(i);
					if (subObj.has("user")) {
						JSONObject arr = subObj.getJSONObject("user");
						twFeedResponse.put("title", arr.get("name"));
						if (subObj.has("text")) {
							twFeedResponse.put("description", subObj.getString("text"));
						} else {
							twFeedResponse.put("description", "");
						}
						twFeedResponse.put("url", "http://twitter.com/" + arr.getString("screen_name"));
						//obj.put("img", "");
						twFeedResponse.put("channelName", "Twitter");
						twFeedResponse.put("date", subObj.getString("created_at"));
						if (StringUtils.isNotBlank(twIcon)) {
							twFeedResponse.put("channelIcon", twIcon);
						} else {

							// setting default image path
							twFeedResponse.put("channelIcon", this.tw_icon_path);
						}
					}
					responseArray.put(twFeedResponse);
				}
			}
		} catch (ClientProtocolException e) {
			log.error("ClientProtocolException Occured while getting twitter details: "+ e);
		} catch (IOException e) {
			log.error("IOException occured while getting twitter details: "+ e);
		} catch (JSONException e) {
			log.error("JSONException occured while getting twitter details: "+ e);
		}
		return responseArray;
	}

	/**
	 * This method returns response of the rss feeds url given in the social
	 * ticker dialog.
	 */
	public JSONArray getRSSFeedsResponse(String feedUrl, String feedIcon, String feedCount) {
		log.debug("Starting - get RSS Feeds details");
		JSONArray responseArray = new JSONArray();
		if (StringUtils.isBlank(feedCount)) {
			feedCount = "4";
		}
		int feedsCount = Integer.parseInt(feedCount);
		BufferedReader buffer_reader = null;
		InputStream openStream = null;
		try {
			URL url = new URL(feedUrl);
			openStream = url.openStream();
			buffer_reader = new BufferedReader(new InputStreamReader(openStream));
			String read;
			StringBuffer buf = new StringBuffer();
			while ((read = buffer_reader.readLine()) != null) {
				buf.append(read);
			}
			JSONObject jsonObject = XML.toJSONObject(buf.toString());
			JSONArray jsonArray = jsonObject.getJSONObject("rss").getJSONObject("channel").getJSONArray("item");
			for (int i = 0; i < feedsCount && jsonArray.length() > 4; i++) {
				JSONObject rssFeedResonse = new JSONObject();
				JSONObject object = (JSONObject) jsonArray.get(i);
				rssFeedResonse.put("title", object.get("title"));
				if (StringUtils.isBlank(object.get("description").toString())) {
					rssFeedResonse.put("description", "");
				} else {
					rssFeedResonse.put("description", object.get("description").toString().replaceAll("\\<.*?\\>", ""));
				}
				rssFeedResonse.put("channelName", "RSS Feed");
				rssFeedResonse.put("url", object.get("link"));
				rssFeedResonse.put("date", object.get("pubDate"));

				if (StringUtils.isNotBlank(feedIcon)) {
					rssFeedResonse.put("channelIcon", feedIcon);
				} else {
					rssFeedResonse.put("channelIcon", rss_icon_path);
				}
				responseArray.put(rssFeedResonse);
			}
		} catch (IOException e) {
			log.error("IOException while getting RSS feeds: "+ e);
		} catch (JSONException e) {
			log.error("JSONException while getting RSS feeds: "+ e);
		} finally {
			try {
				if (null != buffer_reader) {
					buffer_reader.close();
				}
				if (null != openStream) {
					openStream.close();
				}
			} catch (IOException e) {
				log.error("Exception while closing the streams: "+ e);
			}
		}
		return responseArray;
	}

	/**
	 * 
	 * This method returns the response json string which has combined responses
	 * of the facebook, twitter and rss feeds.
	 * 
	 * @param responseJSON
	 * @throws IOException
	 * @throws JSONException
	 */
	public String getStatusResponse(JSONArray responseArray, int feedCount) {
		log.debug("inside the get Status response method.");
		if (feedCount == 0) {
			feedCount = responseArray.length();
		}
		JSONObject responseJSON = new JSONObject();
		try {
			if (StringUtils.isBlank(responseArray.toString())) {
				responseJSON.put("list",
						new JSONArray(new JSONObject().put("title", "Please add configurations..").toString()));
				log.debug("JSON is Empty...");
				return responseJSON.toString();
			}
			responseArray = util.getSortedResponseMap(responseArray, feedCount, log);
			responseJSON.put("list", responseArray);
		} catch (JSONException e) {
			log.error("JSONException in sending status response: "+ e);
		}
		return responseJSON.toString();
	}

	
	/**
	 * Activate method get's all the OSGI configuration parameters and map them
	 * to local variables.
	 * 
	 * @param context
	 */
	@SuppressWarnings("rawtypes")
	@Activate
	public void activate(ComponentContext context) {
		log.debug("inside social ticker service impl activate method");
		Dictionary properties = context.getProperties();
		this.fb_app_id = properties.get(FACEBOOK_APP_ID).toString();
		this.fb_app_secret = properties.get(FACEBOOK_APP_SECRET).toString();
		this.fb_icon_path = properties.get(FACEBOOK_ICON_PATH).toString();
		this.tw_consumer_id = properties.get(TWITTER_APP_ID).toString();
		this.tw_consumer_secret = properties.get(TWITTER_APP_SECRET).toString();
		this.tw_icon_path = properties.get(TWITTER_ICON_PATH).toString();
		this.rss_icon_path = properties.get(RSS_ICON_PATH).toString();
	}
}