let actor_form = $("#insert_actor");

function getBaseUrl() {
    var baseUrl = window.location;
    return baseUrl.protocol + "//" + baseUrl.host + "/" + baseUrl.pathname.split('/')[1];
}


document.getElementById('metaData').onclick = function() {
    location.href= getBaseUrl() + "/" + "metadata.html";
};



function handleActorInsertResult(resultDataString) {
    console.log(resultDataString);
    console.log("successfully added actor into database!");
    let resultDataJson = JSON.parse(resultDataString);
    document.getElementById("movie_insert").reset();
    if (resultDataJson["status"] === "success") {
        // If payment fails, the web page will display
        console.log(resultDataJson["message"]);
        let errorElement = document.getElementById("movie_insert_error_message");
        console.log(errorElement);
        alert(resultDataJson["message"]);
    }
    document.getElementById("insert_actor").reset();
}

/**
 * Submit the form content with POST method
 * @param formSubmitEvent
 */
function submitActorInsert(formSubmitEvent) {
    console.log("submit insert actor form!");
    /**
     * When users click the submit button, the browser will not direct
     * users to the url defined in HTML form. Instead, it will call this
     * event handler when the event is triggered.
     */
    formSubmitEvent.preventDefault();

    $.ajax(
        "api/addActor", {
            method: "POST",
            // Serialize the login form to the data sent by POST request
            data: actor_form.serialize(),
            success: handleActorInsertResult
        }
    );
}

actor_form.submit(submitActorInsert);



// MOVIE INSERTION CODE
let movie_form = $("#movie_insert");


function handleMovieResult(resultDataString) {
    let resultDataJson = JSON.parse(resultDataString);
    console.log("INSERTED MOVIE");
    document.getElementById("movie_insert").reset();
    if (resultDataJson["status"] === "success") {
        // If payment fails, the web page will display
        console.log(resultDataJson["message"]);
        let errorElement = document.getElementById("movie_insert_error_message");
        console.log(errorElement);
        alert(resultDataJson["message"]);
    }
    document.getElementById("movie_insert").reset();
}


/**
 * Submit the form content with POST method
 * @param formSubmitEvent
 */
function submitMovieInsert(formSubmitEvent) {
    console.log("submit insert actor form!");
    /**
     * When users click the submit button, the browser will not direct
     * users to the url defined in HTML form. Instead, it will call this
     * event handler when the event is triggered.
     */
    formSubmitEvent.preventDefault();

    $.ajax(
        "api/addMovie", {
            method: "POST",
            // Serialize the login form to the data sent by POST request
            data: movie_form.serialize(),
            success: handleMovieResult
        }
    );
}

movie_form.submit(submitMovieInsert);