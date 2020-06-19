function getParameterByName(target) {
    // Get request URL
    let url = window.location.href;
    // Encode target parameter name to url encoding
    target = target.replace(/[\[\]]/g, "\\$&");

    // Ues regular expression to find matched parameter value
    let regex = new RegExp("[?&]" + target + "(=([^&#]*)|&|#|$)"),
        results = regex.exec(url);
    if (!results) return null;
    if (!results[2]) return '';

    // Return the decoded parameter value
    return decodeURIComponent(results[2].replace(/\+/g, " "));
}


function getBaseUrl() {
    var baseUrl = window.location;
    return baseUrl.protocol + "//" + baseUrl.host + "/" + baseUrl.pathname.split('/')[1];
}

function handleResult(resultData) {

    console.log("handleResult: populating star info from resultData");
    console.log("handleResult: populating movie table from resultData");

    let movieTitle= jQuery("#movie_title");

    let singleMovieTableBodyEl = jQuery("#single_movie_table_body");

    let movie_title = jQuery("#movie_title_header");
    let movie_year = jQuery("#movie_year");
    let movie_director = jQuery("#movie_director");
    let movie_genres = jQuery("#movie_genres");
    let movie_rating = jQuery("#movie_ratings");
    let orderLink = jQuery("#orderMovie");
    let rowHTML = '<a href=' + getBaseUrl() + '/cart.html?movie=' + getParameterByName("id") + '&quantity=1&add=True'
        + '>' + "Add to Cart" +  '</a>';   // display star_name for the link text
        // orderLink.append(rowHTML);
    orderLink.append(rowHTML);
    movieTitle.append(resultData[0]["title"]);
    if (resultData) {
        let rating = resultData[0]["rating"] ? resultData[0]["rating"] : "N/A";
        if (rating === '0') {
            rating = "N/A"
        }
        movie_title.append("Title: " + resultData[0]["title"]);
        movie_year.append("Year: " + resultData[0]["year"]);
        movie_director.append("Director: " + resultData[0]["director"]);
        movie_genres.append("Genres: " + resultData[0]["genre"]);
        movie_rating.append("Rating: " + rating);


        for (let i = 0; i < resultData.length; i++) {
            let rowHTML = "";
            rowHTML += "<tr> <th>";
            rowHTML += '<a href="single-actor.html?id=' + resultData[i]['starId'] + '">'
            + resultData[i]["star"] + '</a></th></tr>';
            singleMovieTableBodyEl.append(rowHTML);
        }

        document.getElementById('moviesPage').onclick = function() {
            location.href= getBaseUrl() + "/" + resultData[0]["redirect"];
        };
    }
}


let movieId = getParameterByName('id');
jQuery.ajax({
    dataType: "json",  // Setting return data type
    method: "GET",// Setting request method
    url: "api/single-movie?id=" + movieId, // Setting request url, which is mapped by StarsServlet in Stars.java
    success: (resultData) => handleResult(resultData) // Setting callback function to handle data returned successfully by the SingleStarServlet
});


function handleLogout() {
    jQuery.ajax({
        dataType: "json", // Setting return data type
        method: "GET", // Setting request method
        url: "api/logout", // Setting request url, which is mapped by StarsServlet in Stars.java
    });
    window.reload();
}
