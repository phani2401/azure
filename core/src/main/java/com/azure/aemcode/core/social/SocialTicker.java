package com.azure.aemcode.core.social;

import javax.inject.Inject;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.Optional;

@Model(adaptables = Resource.class)
public class SocialTicker {
	@Inject
	@Optional
	public String title;

	@Inject
	@Optional
	public String isfacebook;

	@Inject
	@Optional
	public String fbaccpath;

	@Inject
	@Optional
	public String fbicon;

	@Inject
	@Optional
	public String istwitter;

	@Inject
	@Optional
	public String twittscreenname;

	@Inject
	@Optional
	public String twitterlink;

	@Inject
	@Optional
	public String twittericon;
	
	@Inject
	@Optional
	public String isrss;

	@Inject
	@Optional
	public String feedurl;
	
	@Inject
	@Optional
	public String rssicon;
	
	@Inject
	@Optional
	public String feedcount;
	
	/**
	 * Getting and setting Social Ticker Component properties
	 *
	 */
	public String getTwittericon() {
		return twittericon;
	}

	public void setTwittericon(String twittericon) {
		this.twittericon = twittericon;
	}

	public String getFeedCount() {
		return feedcount;
	}

	public void setFeedCount(String feedcount) {
		this.feedcount = feedcount;
	}
	
	public String getFbicon() {
		return fbicon;
	}

	public void setFbicon(String fbicon) {
		this.fbicon = fbicon;
	}
	
	public String getRssicon() {
		return rssicon;
	}

	public void setRssicon(String rssicon) {
		this.rssicon = rssicon;
	}

	public String getTwitterlink() {
		return twitterlink;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public void setTwitterlink(String twitterlink) {
		this.twitterlink = twitterlink;
	}

	public String getFbaccpath() {
		return fbaccpath;
	}

	public void setFbaccpath(String fbaccpath) {
		this.fbaccpath = fbaccpath;
	}

	public String getTwittscreenname() {
		return twittscreenname;
	}

	public void setTwittscreenname(String twittscreenname) {
		this.twittscreenname = twittscreenname;
	}
	
	public String getRssFeedName() {
		return feedurl;
	}

	public void setRssFeedName(String feedurl) {
		this.feedurl = feedurl;
	}

	public String getIsfacebook() {
		return isfacebook;
	}

	public void setIsfacebook(String isfacebook) {
		this.isfacebook = isfacebook;
	}

	public String getIstwitter() {
		return istwitter;
	}

	public void setIstwitter(String istwitter) {
		this.istwitter = istwitter;
	}
	
	public void setIsrss(String isrss) {
		this.isrss = isrss;
	}

	public String getIsrss() {
		return isrss;
	}

}
