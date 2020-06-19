
function handleMetaDataResult(resultData) {
    let metaDataTableBodyElement = jQuery("#metaData_table_body");
    let usernameElement = jQuery("#usernameM");
    usernameElement.append(resultData[0]["username"]);
    console.log(resultData[0]["tableNames"]);
    console.log("here");
    for (let i = 0; i < resultData.length; i++) {
        let rowHTML = "";
        rowHTML += "<tr id='HM'>";
        rowHTML += "<th>" + resultData[i]["tableNames"] + "</th>";
        console.log(resultData[i]["tableNames"]);
        rowHTML += "<th>" + resultData[i]["variables"] + "</th>";
        rowHTML += "</tr>";
        metaDataTableBodyElement.append(rowHTML);
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
    url: "api/metadata", // Setting request url, which is mapped by StarsServlet in Stars.java
    success: (resultData) => handleMetaDataResult(resultData) // Setting callback function to handle data returned successfully by the StarsServlet
});