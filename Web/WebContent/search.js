function getBaseUrl() {
    var baseUrl = window.location;
    return baseUrl.protocol + "//" + baseUrl.host + "/" + baseUrl.pathname.split('/')[1];
}

let search_form = $("#search_form");

/**
 * Handle the data returned by LoginServlet
 * @param resultDataString jsonObject
 */
function handleSearchResult(formSubmitEvent) {
    formSubmitEvent.preventDefault();

    console.log("handling search response");

    let newUrl = getBaseUrl() + '/search_results.html?';
    let inputTitle = document.getElementById("movie_title").value;
    newUrl += "title=" + inputTitle + "&";

    let inputYear = document.getElementById("movie_year").value;
    newUrl += "year=" + inputYear +  "&";
    // console.log(typeof (inputYear));
    // console.log(inputYear);
    let inputDirector = document.getElementById("movie_director").value;
    newUrl += "director=" + inputDirector +  "&";

    let inputActor = document.getElementById("actor_name").value;
    newUrl += "actor=" + inputActor +  "&";

    let pageNumber = 1;
    newUrl += "pageNum="  + pageNumber +  "&";

    let results = 10;
    newUrl += "results="  + results +  "&";

    let titleOrder = "";
    newUrl += 'titleOrder=' + titleOrder + "&";

    let rankOrder = "";
    newUrl += 'rankOrder=' + rankOrder + "&";

    if (!(inputTitle === "" && inputDirector === "" && inputYear === "" && inputActor === "")) {

         location.href = newUrl.substring(0, newUrl.length - 1);

    }

}


function handleGenreResult(resultData) {
    console.log("IN GENRE HANDLE SEARCH RESULT");
    let genreDiv = document.getElementById('genres');
    let alphaDiv = document.getElementById('alphabet');
    let div_info = "";
//        '<a href="guidedSearch.html?genre=', '\">'  , '</a>', "
    for (let i = 0; i < resultData.length; i++) {
        div_info += '<a href="guidedSearch.html?movie=&genre=' + resultData[i]['genre'] + "&pageNum=1&results=10"  + '">'
                    + resultData[i]["genre"] +  '</a>' + " ";
    }
    genreDiv.innerHTML = div_info;
    let alpha_info = "";
    let str = '*0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ';
    for (let i = 0; i < str.length; i++) {
        alpha_info += '<a href="guidedSearch.html?movie='+ str[i] +"&genre=&pageNum=1&results=10"  + '">'
            + str[i] +  '</a>' + " ";
    }
    alphaDiv.innerHTML = alpha_info;
}

/**
 * Submit the form content with POST method
 * @param formSubmitEvent
 */


function handleLogout() {
    jQuery.ajax({
        dataType: "json", // Setting return data type
        method: "GET", // Setting request method
        url: "api/logout", // Setting request url, which is mapped by StarsServlet in Stars.java
    });
}


jQuery.ajax({
    dataType: "json", // Setting return data type
    method: "GET", // Setting request method
    url: "api/genres", // Setting request url, which is mapped by StarsServlet in Stars.java
    success: (resultData) => handleGenreResult(resultData) // Setting callback function to handle data returned successfully by the StarsServlet
});



// Bind the submit action of the form to a handler function
search_form.submit(handleSearchResult);
