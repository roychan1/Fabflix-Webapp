let salesId = getParameterByName('salesId');

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



function handleConfirmationGet(resultDataJson) {
    let titleArray = resultDataJson["movie_titles"];
    let countArray = resultDataJson["counts"];
    let res = "";
    for (let i = 0; i < titleArray.length; i++) {
        res += "<tr><td>" + titleArray[i] + " x " + countArray[i] + "</td></tr>"
    }

    $("#purchases_table_body").append(res);
    $("#total_price").append("Total: $" + resultDataJson["total_price"]);
    $("#sale_number").append("Order #" + salesId + " has been placed!");
}

function showError(error) {
    // jQuery.ajax 'error' argument -> error: (error) => showError(error)
    let errorStringElement = jQuery("#error_string");
    errorStringElement.append(JSON.stringify(error));
}


$.ajax({
    dataType: "json",
    url: "api/confirmation?salesId=" + salesId,
    method: "GET",
    success: handleConfirmationGet,
    error: showError
})