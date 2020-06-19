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


function handleCartResult(resultData) {
    console.log("handleCartResult: populating cart table from resultData!!");
    let movieTableBodyElement = jQuery("#shopping_cart_table_body");
    let usernameElement = jQuery("#username");
    usernameElement.append(resultData[0]["username"]);
    for (let i = 0; i < resultData.length; i++) {
        let rowHTML = "";
        rowHTML += "<tr id='HM'>";
        rowHTML +=
            "<th>" + '<a href="single-movie.html?id=' + resultData[i]['movieID'] + '">'
            + resultData[i]["title"] +   // display star_name for the link text
            '</a>' + "</th>";
        rowHTML += "<th>" + "$15.99" + "</th>";
        rowHTML += "<th>" + resultData[i]["quantity"] + "</th>";
        rowHTML += "<th>" + '<form id="quantity_form" method="post" action="#">  <label>';
        rowHTML += '<input min="1" name="quantityDesired" placeholder="1" type="number" class="quantityInput" id= Q' + resultData[i]['movieID'] + '>  </label> ' + "</th>";
        rowHTML += "<th>" + '<button id = ' + resultData[i]["movieID"] + ' type="button" class="btn btn-dark" onclick="Update(this.id)">Update</button>' + "</th>";
        rowHTML += "<th>" + '<button id = ' + resultData[i]["movieID"] + ' type="button" class="btn btn-dark" onclick="Remove(this.id)">Remove</button>' + "</th>";
        rowHTML += "</tr>";
        movieTableBodyElement.append(rowHTML);
        // let quantity = document.getElementById("quantity_form").value;
    }
}


let mid = getParameterByName('movie');
let quantity = getParameterByName('quantity');
let newAdd = getParameterByName('add');
let removeV= getParameterByName('remove');
let updateC = getParameterByName('update');
let cartStat = getParameterByName('cartStatus');


function EmptyCart() {
    let endPoint = "/cart.html?movie="+ "&quantity="+ "&add=" + "&remove=" + "&update=" + "&cartStatus=empty";
    window.location = getBaseUrl() + endPoint;
}

function Remove(movieID) {
    let endPoint = "/cart.html?movie=" + movieID + "&quantity=" + quantity + "&add="  + 'False' + "&remove=" + 'True' + "&update=" + "False" + "&cartStatus=empty";
    window.location = getBaseUrl() + endPoint;
}

function Update(movieID) {
    let quantity = document.getElementById('Q' + movieID).value;
    console.log(quantity);
    console.log(movieID);

    let endPoint = "/cart.html?movie=" + movieID + "&quantity=" + quantity + "&add="  + 'False' + "&remove=" + 'False' + "&update=" + "True";
    window.location = getBaseUrl() + endPoint;

}
function ProceedToPayment() {
    // let remove = getParameterByName("remove").value;
    // console.log(remove);
    // let url = window.location.href
    // console.log(url);
    // let movieID = getParameterByName("movie").value;
    // let quantity = document.getElementById('Q' + movieID);
    // if ((quantity  === null && movieID === null)) {
    //     console.log("the cart is empty!");
    //     alert("Cart is currently empty you cannot proceed to the payment page!");
    // } else {
    // var x = document.getElementById("#shopping_cart_table_body");
    // console.log(x.rows.length);
    if ($("#shopping_cart_table tr").length === 1 ) {
        console.log("in here cart is empty");
    } else {
         let endPoint = "/payment.html";
         window.location = getBaseUrl() + endPoint;
    }
    // if (getParameterByName("cartStatus") !== "empty" )  {
    //     console.log(getParameterByName("cartStatus"));
    //     console.log("in if statement");
    //     // let endPoint = "/payment.html";
    //     // window.location = getBaseUrl() + endPoint;
    // }
    // console.log("not in if statement");

    // window.location = getBaseUrl() + endPoint;


}

jQuery.ajax({
    dataType: "json", // Setting return data type
    method: "GET", // Setting request method
    url: "api/cart?movie=" + mid + "&quantity=" + quantity + "&add="  + newAdd + "&remove=" + removeV + "&update=" + updateC + "&cartStatus=" + cartStat,  // Setting request url, which is mapped by StarsServlet in Stars.java
    success: (resultData) => handleCartResult(resultData) // Setting callback function to handle data returned successfully by the StarsServlet
});


function handleLogout() {
    jQuery.ajax({
        dataType: "json", // Setting return data type
        method: "GET", // Setting request method
        url: "api/logout", // Setting request url, which is mapped by StarsServlet in Stars.java
    });
    window.reload();
}