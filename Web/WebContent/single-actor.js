function getParameterByName(target) {
    let url = window.location.href;
    target = target.replace(/[\[\]]/g, "\\$&");

    let regex = new RegExp("[?&]" + target + "(=([^&#]*)|&|#|$)"),
        results = regex.exec(url);
    if (!results) return null;
    if (!results[2]) return '';

    return decodeURIComponent(results[2].replace(/\+/g, " "));
}

function getBaseUrl() {
    var baseUrl = window.location;
    return baseUrl.protocol + "//" + baseUrl.host + "/" + baseUrl.pathname.split('/')[1];
}


function handleActorResult(resultData) {
    console.log("handleResult: populating star info from resultData");
    console.log("handleResult: populating movie table from resultData");

    let singleMovieTableBodyEl = jQuery("#single_actor_body");
    let title = jQuery("#actor_title");
    title.append(resultData[0]["star_name"]);
    let starName = jQuery("#actor_name");
    let starDob = jQuery("#actor_dob");

    if (resultData) {
        let dob = resultData[0]["star_dob"] ? resultData[0]["star_dob"] : "N/A";
        starName.append("Actor: " + resultData[0]["star_name"]);
        starDob.append("Date of Birth: " + dob);

        for (let i = 0; i < resultData.length; i++) {
            console.log(resultData[i]['star_name']);
            let rowHTML = "";
            rowHTML += "<tr> <th>";
            rowHTML += '<a href="single-movie.html?id=' + resultData[i]['movie_id'] + '">'
                + resultData[i]["movie_title"] + '</a></th>';
            rowHTML += "<th>" + resultData[i]['movie_director'] + "</th>";
            rowHTML += "<th>" + resultData[i]['movie_year'] + "</th></tr>";
            singleMovieTableBodyEl.append(rowHTML);
        }


        document.getElementById('moviesPage').onclick = function() {
            location.href= getBaseUrl() + "/" + resultData[0]["redirect"];
        };
    }
}

let actorId = getParameterByName('id');
jQuery.ajax({
    dataType: "json",  // Setting return data type
    method: "GET",// Setting request method
    url: "api/single-actor?id=" + actorId, // Setting request url, which is mapped by StarsServlet in Stars.java
    success: (resultData) => handleActorResult(resultData) // Setting callback function to handle data returned successfully by the SingleStarServlet
});


function handleLogout() {
    jQuery.ajax({
        dataType: "json", // Setting return data type
        method: "GET", // Setting request method
        url: "api/logout", // Setting request url, which is mapped by StarsServlet in Stars.java
    });
    window.reload();
}
