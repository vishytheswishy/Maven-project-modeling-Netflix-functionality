function getBaseUrl() {
    var baseUrl = window.location;
    return baseUrl.protocol + "//" + baseUrl.host + "/" + baseUrl.pathname.split('/')[1];
}

function changePage(advPage) {
    let url = new URL(window.location.href);
    let search_params = url.searchParams;
    search_params.set('pageNum', advPage.toString());
    url.search = search_params.toString();
    let new_url = url.toString();
    return new_url;
}
function changeRank(details, type) {
    let url = new URL(window.location.href);
    let search_params = url.searchParams;
    search_params.set(details, type);
    // if (details === "titleOrder")
    //     search_params.set("rankOrder", "");
    // if (details === "rankOrder")
    //     search_params.set("titleOrder", "");
    url.search = search_params.toString();
    let new_url = url.toString();
    return new_url;
}

function changeResults(results) {
    let url = new URL(window.location.href);
    let search_params = url.searchParams;
    search_params.set('results', results.toString());
    url.search = search_params.toString();
    let new_url = url.toString();
    return new_url;
}
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


function handleSearchResult(resultData) {
    console.log("handleSearchResult: populating star table from resultData!!");
    let movieTableBodyElement = jQuery("#search_table_body");
    let pageNumber = getParameterByName("pageNum");

    if (!(resultData.length === 0)) {
        for (let i = 0; i < resultData.length; i++) {
            let rating = resultData[i]["rating"];

            if (rating === '0') {
                rating = "N/A";
            }
            let rowHTML = "";
            rowHTML += "<tr id='HM'>";
            rowHTML +=
                "<th>" + '<a href="single-movie.html?id=' + resultData[i]['movieID'] + '">'
                + resultData[i]["title"] +     // display star_name for the link text
                '</a>' + "</th>";
            rowHTML += "<th>" + resultData[i]["year"] + "</th>";
            rowHTML += "<th>" + resultData[i]["director"] + "</ th>";
            rowHTML += "<th>" + resultData[i]["genre"] + "</th>";
            rowHTML += "<th>" + resultData[i]["actors"] + "</th>";
            rowHTML += "<th>" + rating + "</th>";
            rowHTML += "<th>" + '<a class="boxed" href=' + getBaseUrl() + '/cart.html?movie=' + resultData[i]['movieID'] + '&quantity=1&add=True'  + '>' + "Add To Shopping Cart" +     // display star_name for the link text
                '</a>' + "</th>";
            rowHTML += "</tr>";
            movieTableBodyElement.append(rowHTML);
        }

        let numElement = document.getElementById("page");
        let pageNumber = getParameterByName("pageNum");
        numElement.append(pageNumber);
    } else {
        let noResEl = document.getElementById("noResults");
        if (pageNumber === "1") {noResEl.innerHTML += "NO RESULTS FOUND";}
        else { noResEl.innerHTML += "NO MORE PAGES TO SHOW";
        beforePage()}
    }
    document.getElementById('SearchPage').onclick = function() {
        location.href= getBaseUrl() + "/search.html";
    };
}

function rankASC() {
    location.href = changeRank("rankOrder", "asc");
}
function rankDESC() {
    location.href = changeRank("rankOrder", "desc");
}
function titleASC() {
    location.href = changeRank("titleOrder", "asc");
}
function titleDESC() {
    location.href = changeRank("titleOrder", "desc");
}


function beforePage() {
    let currPage = getParameterByName("pageNum");
    if (currPage > 1) {
        let beforePage = parseInt(currPage) - 1;
        location.href = changePage(beforePage);
    }
}

function advPage() {
    let currPage = getParameterByName("pageNum");
    if (currPage > 0) {
        let advPage = parseInt(currPage) + 1;
        location.href = changePage(advPage);
    }
}

window.onload = function() {
    let choose_n = document.getElementById("choose_N");
    choose_n.value = 10;
    N = 10;
};

function onChange() {
    // console.log("IN ON CHANGE");
    N = document.getElementById("choose_N").value;
    console.log(N);
    location.href = changeResults(N);
}

let title = getParameterByName('title');
let directors = getParameterByName('director');
let years = getParameterByName('year');
let actor_name = getParameterByName('actor');
let pageNum = getParameterByName('pageNum');
let results = getParameterByName('results');
let titleOrder = getParameterByName('titleOrder');
let rankOrder = getParameterByName('rankOrder');
jQuery.ajax({
    dataType: "json",  // Setting return data type
    method: "GET",// Setting request method
    url: "api/search_results?title=" + title + "&year=" + years + "&director=" + directors + "&actor=" +
        actor_name + "&pageNum=" + pageNum + "&results=" + results + '&titleOrder=' + titleOrder + '&rankOrder=' + rankOrder ,//
    success: (resultData) => handleSearchResult(resultData) //
});


function handleLogout() {
    jQuery.ajax({
        dataType: "json", // Setting return data type
        method: "GET", // Setting request method
        url: "api/logout", // Setting request url, which is mapped by StarsServlet in Stars.java
    });
    window.reload();
}
