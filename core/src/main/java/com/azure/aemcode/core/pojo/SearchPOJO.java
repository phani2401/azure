package com.azure.aemcode.core.pojo;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

/**
 * 
 *
 *
 */
public class SearchPOJO {
	
	private String searchTerm = StringUtils.EMPTY;
	private String noOfPages = StringUtils.EMPTY;
	private String offSet = StringUtils.EMPTY;
	private boolean hasMore = false;
	private List<String> filterPaths = new ArrayList<String>();
	private List<String> blockedPaths = new ArrayList<String>();
	
	public void setBlockedPaths(List<String> blockedPaths) {
		this.blockedPaths = blockedPaths;
	}

	public void setSearchTerm(String searchTerm) {
		this.searchTerm = searchTerm;
	}

	public void setFilterPaths(List<String> filterPaths) {
		this.filterPaths = filterPaths;
	}

	public List<String> getBlockedPaths() {
		return blockedPaths;
	}

	public String getSearchTerm() {
		return searchTerm;
	}
	
	public List<String> getFilterPaths() {
		return filterPaths;
	}

	public String getNoOfPages() {
		return noOfPages;
	}

	public void setNoOfPages(String noOfPages) {
		this.noOfPages = noOfPages;
	}

	public String getOffSet() {
		return offSet;
	}

	public void setOffSet(String offSet) {
		this.offSet = offSet;
	}

	public boolean isHasMore() {
		return hasMore;
	}

	public void setHasMore(boolean hasMore) {
		this.hasMore = hasMore;
	}
}
