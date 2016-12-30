package com.azure.aemcode.core.social;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.commons.json.JSONArray;
import org.apache.sling.commons.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adobe.cq.sightly.WCMUsePojo;
import com.azure.aemcode.core.slingmodels.SocialTickerModel;
import com.azure.aemcode.core.social.service.SocialTickerService;

public class SocialTickerUse extends WCMUsePojo {

	public SocialTickerModel socTicker;
	private static final Logger log = LoggerFactory.getLogger(SocialTickerUse.class);
	private String fbPath;
	public String[] pathSplit;
	public String feeds;
	private int feedsCount = 4;

	@Override
	public void activate() throws Exception {
		log.debug("inside SocialTickerUse activate method.");
		JSONArray jsonArray = new JSONArray();
		SocialTickerService ccrSocialLoginService = getSlingScriptHelper().getService(SocialTickerService.class);
		Resource resource = getResource();
		socTicker = resource.adaptTo(SocialTickerModel.class);
		fbPath = socTicker.fbaccpath;
		if (null != socTicker.isfacebook && socTicker.isfacebook.equals("true") && ccrSocialLoginService != null) {
			if (fbPath != null) {
				JSONArray array = ccrSocialLoginService.getFacebookDetails(socTicker.fbaccpath, socTicker.fbicon, socTicker.feedcount);
				jsonArray = addToJSONArray(array, jsonArray);
			}
		}
		if (null != socTicker.istwitter && socTicker.istwitter.equals("true") && ccrSocialLoginService != null) {
			JSONArray array = ccrSocialLoginService.getTwitterResponse(socTicker.twittscreenname, socTicker.twittericon, socTicker.feedcount);
			jsonArray = addToJSONArray(array, jsonArray);
		}
		
		if (null != socTicker.isrss && socTicker.isrss.equals("true") && ccrSocialLoginService != null) {
			JSONArray array = ccrSocialLoginService.getRSSFeedsResponse(socTicker.feedurl, socTicker.rssicon, socTicker.feedcount);
			jsonArray = addToJSONArray(array, jsonArray);
		}
		
		if(StringUtils.isBlank(socTicker.feedcount)){
			feeds = ccrSocialLoginService.getStatusResponse(jsonArray, feedsCount);
		} else {
			feeds = ccrSocialLoginService.getStatusResponse(jsonArray, Integer.parseInt(socTicker.feedcount));
		}
		
		log.debug("Final Feeds " + feeds);
	}

	/**
	 * 
	 * @param array
	 * @param jsonArray2 
	 * @return 
	 * @throws JSONException 
	 */
	public JSONArray addToJSONArray(JSONArray array, JSONArray feedArray) throws JSONException {
		JSONArray jsonArray = feedArray;
		for(int i = 0; i < array.length(); i++){
			jsonArray.put(array.get(i));
		}
		return jsonArray;
	}
}
