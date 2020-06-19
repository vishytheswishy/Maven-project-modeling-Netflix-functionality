let payment_form = $("#paymentInfo");

/**
 * Handle the data returned by LoginServlet
 * @param resultDataString jsonObject
 */
function handlePaymentResult(resultDataString) {
    let resultDataJson = JSON.parse(resultDataString);
    console.log(resultDataString);
    console.log(resultDataJson["status"]);
    // If payment succeeds, it will redirect the user to index.html
    if (resultDataJson["status"] === "success") {
        console.log("IN THIS SUCCESS ");
        window.location.replace("confirmation.html");
    }
    else {
        // If payment fails, the web page will display
        console.log("show error message");
        console.log(resultDataJson["message"]);
        $("#payment_error_message").text(resultDataJson["message"]);
    }
}



function handleCost(resultData) {
    console.log("IN THIS FUNCTION")
    let costDiv = document.getElementById("total_cost");
    let num = resultData[0]['cost']
    costDiv.innerHTML = "Total Cost: " + num.toFixed(2);
}

jQuery.ajax({
    dataType: "json", // Setting return data type
    method: "GET", // Setting request method
    url: "api/payment", // Setting request url
    success: (resultData) => handleCost(resultData)
});


/**
 * Submit the form content with POST method
 * @param formSubmitEvent
 */
function submitPaymentForm(formSubmitEvent) {
    console.log("submit login form");
    /**
     * When users click the submit button, the browser will not direct
     * users to the url defined in HTML form. Instead, it will call this
     * event handler when the event is triggered.
     */
    formSubmitEvent.preventDefault();

    $.ajax(
        "api/payment", {
            method: "POST",
            // Serialize the login form to the data sent by POST request
            data: payment_form.serialize(),
            success: handlePaymentResult
        }
    );
}

// Bind the submit action of the form to a handler function
payment_form.submit(submitPaymentForm);
