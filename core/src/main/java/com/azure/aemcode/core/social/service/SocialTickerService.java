package com.azure.aemcode.core.social.service;

import org.apache.sling.commons.json.JSONArray;

public interface SocialTickerService {
	public JSONArray getFacebookDetails(String fbId,String fbIcon, String feedcount);
	public JSONArray getTwitterResponse(String twId, String twIcon, String feedcount);
	public JSONArray getRSSFeedsResponse(String feedurl, String rssicon, String feedcount);
	public String getStatusResponse(JSONArray jsonArray, int i);
}