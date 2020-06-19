
function parse(resultData, idx) {
    let stars = resultData[idx]['star'];

}


let cache = {};


function getBaseUrl() {
    var baseUrl = window.location;
    return baseUrl.protocol + "//" + baseUrl.host + "/" + baseUrl.pathname.split('/')[1];
}

function handleMovieResult(resultData) {
    console.log("handleStarResult: populating star table from resultData!!");
    let movieTableBodyElement = jQuery("#movie_table_body");
    let usernameElement = jQuery("#username");
    usernameElement.append(resultData[0]["username"]);
    for (let i = 0; i < resultData.length; i++) {
        let rowHTML = "";
        rowHTML += "<tr id='HM'>";
        rowHTML +=
            "<th>" + '<a href="single-movie.html?id=' + resultData[i]['titleID'] + '">'
            + resultData[i]["title"] +     // display star_name for the link text
            '</a>' + "</th>";
        rowHTML += "<th>" + resultData[i]["year"] + "</th>";
        rowHTML += "<th>" + resultData[i]["director"] + "</th>";
        rowHTML += "<th>" + resultData[i]["genre"] + "</th>";
        rowHTML += "<th>" + resultData[i]["star"] + "</th>";
        rowHTML += "<th>" + resultData[i]["rating"] + "</th>";
        rowHTML += "</tr>";
        movieTableBodyElement.append(rowHTML);
    }
}




function handleLogout() {
    jQuery.ajax({
        dataType: "json", // Setting return data type
        method: "GET", // Setting request method
        url: "api/logout", // Setting request url, which is mapped by StarsServlet in Stars.java
    });
    window.reload();
}

jQuery.ajax({
    dataType: "json", // Setting return data type
    method: "GET", // Setting request method
    url: "api/movies", // Setting request url, which is mapped by StarsServlet in Stars.java
    success: (resultData) => handleMovieResult(resultData) // Setting callback function to handle data returned successfully by the StarsServlet
});



// autocomplete


/*
 * This function is called by the library when it needs to lookup a query.
 *
 * The parameter query is the query string.
 * The doneCallback is a callback function provided by the library, after you get the
 *   suggestion list from AJAX, you need to call this function to let the library know.
 */
function handleLookup(query, doneCallback) {
    if (query.length >= 3) {
        console.log("autocomplete initiated");

        if (query in cache) {
            console.log("using cache for autocomplete");
            console.log(cache[query]);
            doneCallback({suggestions: cache[query]});
        } else {
            console.log("sending AJAX request to backend Java Servlet");

            // TODO: if you want to check past query results first, you can do it here

            // sending the HTTP GET request to the Java Servlet endpoint hero-suggestion
            // with the query data
            jQuery.ajax({
                "method": "GET",
                // generate the request url from the query.
                // escape the query string to avoid errors caused by special characters
                "url": "auto_complete?query=" + escape(query),
                "success": function (data) {
                    // pass the data, query, and doneCallback function into the success handler
                    handleLookupAjaxSuccess(data, query, doneCallback)
                },
                "error": function (errorData) {
                    console.log("lookup ajax error")
                    console.log(errorData)
                }
            })

        }
    }
}


/*
 * This function is used to handle the ajax success callback function.
 * It is called by our own code upon the success of the AJAX request
 *
 * data is the JSON data string you get from your Java Servlet
 *
 */
function handleLookupAjaxSuccess(data, query, doneCallback) {
    console.log("lookup ajax successful");
    var myJSON = JSON.stringify(data);
    var jsonData = JSON.parse(myJSON);
    cache[query] = jsonData;
    console.log(jsonData);


    // TODO: if you want to cache the result into a global variable you can do it here

    // call the callback function provided by the autocomplete library
    // add "{suggestions: jsonData}" to satisfy the library response format according to
    //   the "Response Format" section in documentation
    doneCallback( { suggestions: jsonData } );
}


/*
 * This function is the select suggestion handler function.
 * When a suggestion is selected, this function is called by the library.
 *
 * You can redirect to the page you want using the suggestion data.
 */
function handleSelectSuggestion(suggestion) {
    // TODO: jump to the specific result page based on the selected suggestion

    // console.log("you select " + suggestion["value"] + " with ID " + suggestion["data"]["movieId"])
    console.log("IN SUGGESTION FUNCTION");
    window.location = getBaseUrl() + "/single-movie.html?id=" + suggestion["data"]["movieId"];

}


/*
 * This statement binds the autocomplete library with the input box element and
 *   sets necessary parameters of the library.
 *
 * The library documentation can be find here:
 *   https://github.com/devbridge/jQuery-Autocomplete
 *   https://www.devbridge.com/sourcery/components/jquery-autocomplete/
 *
 */
// $('#autocomplete') is to find element by the ID "autocomplete"
$('#autocomplete').autocomplete({

    // documentation of the lookup function can be found under the "Custom lookup function" section
    lookup: function (query, doneCallback) {
        handleLookup(query, doneCallback)
    },
    onSelect: function(suggestion) {
        handleSelectSuggestion(suggestion)
    },
    // set delay time
    deferRequestBy: 300,
    // there are some other parameters that you might want to use to satisfy all the requirements
    // TODO: add other parameters, such as minimum characters
});


/*
 * do normal full text search if no suggestion is selected
 */
function handleNormalSearch(query) {
    console.log("doing normal search with query: " + query);
    // TODO: you should do normal search here
    window.location = getBaseUrl() + "/search_results.html?title=" + query + "&year=&director=&actor=&pageNum=1&results=10&titleOrder=&rankOrder=";


}

// bind pressing enter key to a handler function
$('#autocomplete').keypress(function(event) {
    // keyCode 13 is the enter key
    if (event.keyCode == 13) {
        // pass the value of the input box to the handler function
        handleNormalSearch($('#autocomplete').val())
    }
})