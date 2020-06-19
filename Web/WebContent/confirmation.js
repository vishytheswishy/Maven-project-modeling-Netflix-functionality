

function getBaseUrl() {
    var baseUrl = window.location;
    return baseUrl.protocol + "//" + baseUrl.host + "/" + baseUrl.pathname.split('/')[1];
}

function handleClickConfirm(resultData) {
    let firstName = resultData[0]["firstName"];
    let lastName = resultData[0]["lastName"];
    let confirmDiv = document.getElementById("confirmDiv");
    console.log(resultData[0]["ccID"]);
    confirmDiv.innerHTML = "Hello, " + firstName + " " + lastName + "!" + "<br>" + "You have successfully purchased your movies!";
    confirmDiv.innerHTML += "<br>" + "You will be redirected to HomePage in 3 seconds";
    setTimeout(function(){ window.location = getBaseUrl() + "/index.html"; }, 3000);

}

function handleConfirm() {
    console.log('HA');

    jQuery.ajax({
        dataType: "json", // Setting return data type
        method: "GET", // Setting request method
        url: "api/confirm", // Setting request url
        success: (resultData) => handleClickConfirm(resultData)
    });


}