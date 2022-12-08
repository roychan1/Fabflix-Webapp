/**
 * Retrieve parameter from request URL, matching by parameter name
 * @param target String
 * @returns {*}
 */
function getParameterByName(target) {
    // Get request URL
    let url = window.location.href;
    // Encode target parameter name to url encoding
    target = target.replace(/[\[\]]/g, "\\$&");

    // Use regular expression to find matched parameter value
    let regex = new RegExp("[?&]" + target + "(=([^&#]*)|&|#|$)"),
        results = regex.exec(url);
    if (!results) return null;
    if (!results[2]) return '';

    // Return the decoded parameter value
    return decodeURIComponent(results[2].replace(/\+/g, " "));
}

function getCookie(name) {
    var cookies = "; " + document.cookie;
    var cookie = cookies.split("; " + name + "=");
    if (cookie.length == 2) {
        return cookie.pop().split(";").shift();
    } else {
        return cookie.shift().split(";").shift();
    }
}

/**
 * @param resultData jsonObject
 */
function handleSingleStarResult(resultData) {
    console.log(resultData);
    let starNameElement = jQuery("#single_star_name"),
        starBirthYearElement = jQuery("#single_star_birth_year"),
        starTableBodyElement = jQuery("#single_star_movies_table_body");

    starNameElement.append(resultData["star_name"]);
    starBirthYearElement.append("Year of birth: " + resultData["star_birth_year"]);
    for (let i = 0; i < resultData["star_movies_table"].length; i++) {
        let htmlString = "<tr><td><a class='badge badge-dark' href=\"single-movie.html?id=" +
            resultData["star_movies_table"][i]["movie_id"] + "\">" +
            resultData["star_movies_table"][i]["movie_title"] +
            "</a></td></tr>";

        starTableBodyElement.append(htmlString);
    }
}

function showError(error) {
    // jQuery.ajax 'error' argument -> error: (error) => showError(error)
    let movieTableBodyElement = jQuery("#single_star_name");
    movieTableBodyElement.append(JSON.stringify(error));
}

let starID = getParameterByName("id");

$(document).ready(function() {
    $("#backBtn").attr("href", getCookie("lastUrl"));
})
jQuery.ajax({
    dataType: "json",
    method: "GET",
    url: "api/single-star?id=" + starID,
    success: (resultData) => handleSingleStarResult(resultData),
    error: (error) => showError(error)
})