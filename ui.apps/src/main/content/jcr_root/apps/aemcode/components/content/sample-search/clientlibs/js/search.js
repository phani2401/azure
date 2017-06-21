//creating Namespace
var AemCode = {};

AemCode.SearchResults = AemCode.SearchResults || {};
AemCode.SearchResults.currentPage;
AemCode.SearchResults.searchData;
AemCode.SearchResults.previousSearchData;
AemCode.SearchResults.offsetValue;

var getSearchPagination = function(pageNumber, data) {
	var prevResultsObj = '';
	if (AemCode.SearchResults.previousSearchData) {
		prevResultsObj = JSON
				.stringify(AemCode.SearchResults.previousSearchData);
		prevResultsObj = prevResultsObj.substring(1, prevResultsObj.length - 1);
		var assetObj = '';
		if (AemCode.SearchResults.searchData) {
			assetObj = JSON.stringify(AemCode.SearchResults.searchData);
			assetObj = assetObj.substring(1, assetObj.length - 1);
		}
		var concatedArray = ''; 
		if (prevResultsObj) {
			if (assetObj) {
				concatedArray = prevResultsObj + ',' + assetObj;
			} else {
				concatedArray = prevResultsObj;
			}
		}
		if ('' != concatedArray)
			AemCode.SearchResults.searchData.assets = JSON.parse('['
					+ concatedArray + ']');
		AemCode.SearchResults.previousSearchData = undefined;
	}

	if ('' != prevResultsObj) {
		AemCode.SearchResults.currentPage = pageNumber - 1;
		prevResultsObj = '';
	} else {
		AemCode.SearchResults.currentPage = pageNumber;
	}
	var numerofrecordsperpage = $('#resultsPerPage').val();
	var results = {};
	var flag = 0;
	var noofpages;
	if (AemCode.SearchResults.searchData != null
			&& AemCode.SearchResults.searchData
			&& AemCode.SearchResults.searchData.length > 0) {
		if (AemCode.SearchResults.searchData.length < numerofrecordsperpage) {
			noofpages = 0;
			flag = 1;
		} else {
			noofpages = Math
					.ceil(AemCode.SearchResults.searchData.length
							/ numerofrecordsperpage);
		}
	} else {
		noofpages = 0;
	}
	alert("Search Data Length: " + AemCode.SearchResults.searchData.length);

    alert("No Of Pages: " + noofpages);

	var s = '';
	if (AemCode.SearchResults.currentPage > 1) {
		s = s + "<span><a href='#' onclick='getSearchPagination("
				+ (AemCode.SearchResults.currentPage - 1)
				+ ")' title='Previous' class='prevPage'>Previous</a></span>";
	}

	if (noofpages < 5) {
		for (var i = 1; i <= noofpages; i++) {
			if (AemCode.SearchResults.currentPage === i) {
				s = s + "<span>" + i + "</span>";
			} else {
				s = s + "<span><a href='#'  onclick='getSearchPagination(" + i
						+ ")'>" + i + "</a></span>";
			}
		}
	}

	if (noofpages >= 5 && (AemCode.SearchResults.currentPage < 4)) {
		for (var i = 1; i < 4; i++) {
			if (AemCode.SearchResults.currentPage === i) {
				s = s + "<span>" + i + "</span>";
			} else {
				s = s + "<span><a href='#'  onclick='getSearchPagination(" + i
						+ ")'>" + i + "</a></span>";
			}
		}
		s = s + " ... <span><a href='#'  onclick='getSearchPagination("
				+ noofpages + ")'>" + noofpages + "</a></span>";
	}

	if (AemCode.SearchResults.currentPage >= 4 && noofpages > 5
			&& AemCode.SearchResults.currentPage != noofpages
			&& (!(AemCode.SearchResults.currentPage >= noofpages - 3))) {
		s = s + "<span><a href='#'  onclick='getSearchPagination(" + 1 + ")'>"
				+ 1 + "</a></span>" + "..." + "<span>"
				+ AemCode.SearchResults.currentPage + "</span>" + "..."
				+ "<span><a href='#'  onclick='getSearchPagination("
				+ noofpages + ")'>" + noofpages + "</a></span>";
	}
	if (AemCode.SearchResults.currentPage >= 4 && noofpages > 5
			&& AemCode.SearchResults.currentPage != noofpages
			&& (AemCode.SearchResults.currentPage == noofpages - 3)) {

		s = s + "<span><a href='#'  onclick='getSearchPagination(" + 1 + ")'>"
				+ 1 + "</a></span>" + "..." + "<span>"
				+ AemCode.SearchResults.currentPage + "</span>" + "..."
				+ "<span><a href='#'  onclick='getSearchPagination("
				+ noofpages + ")'>" + noofpages + "</a></span>";
	}

	if (AemCode.SearchResults.currentPage >= 4
			&& noofpages > 5
			&& (AemCode.SearchResults.currentPage == noofpages || (AemCode.SearchResults.currentPage > noofpages - 3))) {

		s = s + "<span><a href='#'  onclick='getSearchPagination(" + 1 + ")'>"
				+ 1 + "</a></span>" + "...";
		for (var i = noofpages - 3; i <= noofpages; i++) {
			if (AemCode.SearchResults.currentPage === i) {
				s = s + "<span>" + i + "</span>";
			} else {
				s = s + "<span><a href='#'  onclick='getSearchPagination(" + i
						+ ")'>" + i + "</a></span>";
			}
		}
	}

	if (AemCode.SearchResults.currentPage == noofpages
			&& AemCode.SearchResults.searchData.hasMore
			&& AemCode.SearchResults.searchData.hasMore == 'true') {
		AemCode.SearchResults.previousSearchData = AemCode.SearchResults.searchData;
		var searchStringValue = $("#search-field").val();
		alert("Search String Value: {}", searchStringValue);
		var assetsLength = '';
		if (AemCode.SearchResults.searchData)
			assetsLength = AemCode.SearchResults.searchData.length;

		if (searchStringValue === '')
			searchStringValue = AemCode.SearchResults.searchData.searchtext;
		$.ajax({
			type : 'GET',
			dataType : 'json',
			url : "/bin/search",
			success : function(data, prevResults) {
				AemCode.SearchResults.searchData = data;
				context = data;
				getSearchPagination((AemCode.SearchResults.currentPage + 1),
						data);
			}
		});

	}

	if (AemCode.SearchResults.currentPage < noofpages) {
		AemCode.SearchResults.currentPage + 1;
		s = s + "<span><a href='#' onclick='getSearchPagination("
				+ (AemCode.SearchResults.currentPage + 1)
				+ ")' title='Next' class='nextPage'>Next</a></span>";
	}

	$('#paginationsearchresults').html(s);
    alert("Flag: " + flag);
	var assets = [];
	if (noofpages >= 1 || flag == 1) {
		for (var j = ((AemCode.SearchResults.currentPage - 1) * numerofrecordsperpage); j < ((AemCode.SearchResults.currentPage * numerofrecordsperpage)); j++) {
			if ((AemCode.SearchResults.searchData && AemCode.SearchResults.searchData.length - 1) < j) {
				break;
			}
			assets.push(AemCode.SearchResults.searchData[j]);
			results.assets = assets;
		}
	} else {
		results = AemCode.SearchResults.searchData;
	}
    
    //handle bars code
	//var source = $("#searchresults-template").html();
	//var template = Handlebars.compile(source);
	//var context;
	//context = results;
	//var theCompiledHtml = template(context);
	//$('#search-results').html(theCompiledHtml);
	if (assets && assets.length == 0) {
		$("#noresults")
				.html(
						"No search results were found. Please refine your search and try again.");
		$(".search-nav").hide();

        $("#search-results").hide();
	} else {
		$("#noresults").html("");
	}
};

var searchRecordsPerPage = function() {
	getSearchPagination(1, AemCode.SearchResults.searchData);
};


//From Here actual Code

var getResults = function() {

	var searchText = '';
    
    // setting Offset to '0'
    AemCode.SearchResults.offsetValue = '0';
	var numerofrecordsperpage = '';
	if ($("#resultsPerPage"))
		numerofrecordsperpage = $("#resultsPerPage").val();
	if ($("#search-field"))
		searchText = $("#search-field").val();
    
	if (searchText && searchText != '') {
		$("#search-field").val(unescape(searchText));
		$("#displayResults").html(
				'Displaying results for \"' + unescape(searchText)
						+ '\" within \"Everything\".');

		//var searchFieldValue = $("#search-field").val();
		$.ajax({
			url : "/bin/search",
			type : "POST",
            dataType : 'json',
			data : {
				"search-field" : searchText,
				"noofpages" : (numerofrecordsperpage * 10),
				"offset": AemCode.SearchResults.offsetValue
			},
			success : function(result) {
				displayResults(result);
			}
		});
	}
};

var displayResults = function(resultData) {
	var resultsJSON = resultData;
	var results = [];
	var facets = [];

	if (!resultData || resultData == 'undefined') {
		$("#facets").html("No Results found.");
		return;
	}
    for (var i = 0; i < resultsJSON.length; i++) {
        var jsonObj = resultsJSON[i];
        if (jsonObj.facets) {
          $.each(jsonObj.facets, function(key, val) {
			 facets.push(key + "(" + val + ")");
		  });
            resultsJSON.splice(i,1);
        } else {
            results.push("<a href='" + jsonObj.path + "'>"
					+ jsonObj.title + "</a>");
        }
    }    
    AemCode.SearchResults.searchData = resultsJSON;
	getSearchPagination(1, resultsJSON);

	$("#facets").html(facets.join("&emsp;"));
	$("#results").html(results.join("<br/>"));

};