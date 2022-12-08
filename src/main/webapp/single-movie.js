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

    // Ues regular expression to find matched parameter value
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
function handleSingleMovieResult(resultData) {
    let movieTitleElement = jQuery("#single_movie_title");
    let movieTableBodyElement = jQuery("#single_movie_list_table_body");

    movieTitleElement.append(resultData['movie_title']);

    let rowHTML = "<tr>" +
        "<td>" +
        "<a class='badge badge-dark' href=''>" +
        resultData['movie_title'] +
        "</a>" +
        "</td>" +
        "<td>" +
        resultData['movie_year'] +
        "</td>" +
        "<td>" +
        resultData['movie_director'] +
        "</td><td>";

    for (let j = 0; j < resultData['movie_genres'].length; j++) {
        rowHTML += resultData['movie_genres'][j]['genre'] + "<br>";
    }

    rowHTML += "</td><td>";

    for (let i = 0; i < resultData['movie_stars'].length; i++) {
        rowHTML += "<a class='badge badge-warning' href='single-star.html?id=" +
            resultData['movie_stars'][i]['star_id'] + "'>" +
            resultData['movie_stars'][i]['star_name'] + "</a><br>";
    }

    rowHTML += "</td><td>" +
        resultData['movie_rating'] +
        "</td>" +
        "</tr>";

    movieTableBodyElement.append(rowHTML);
}

function showError(error) {
    // jQuery.ajax 'error' argument -> error: (error) => showError(error)
    let movieTableBodyElement = jQuery("#single_movie_list_table_body");
    movieTableBodyElement.append("<tr><th>" + JSON.stringify(error) + "</th></tr>");
}

let movieId = getParameterByName('id');

$("#add_button").on("click", function() {
    let successString = "#success";
    let failString = "#fail";
    jQuery.ajax({
        method: "POST",
        data: {"id": movieId},
        url: 'api/shopping-cart',
        success: () => {
            $(successString).show();
            $(failString).hide();
        },
        error: () => {
            $(successString).hide();
            $(failString).show();
        }
    })
})

$(document).ready(function() {
    $("#backBtn").attr("href", getCookie("lastUrl"));
})

jQuery.ajax({
    dataType: "json",
    method: "GET",
    url: "api/single-movie-list?id=" + movieId,
    success: (resultData) => handleSingleMovieResult(resultData),
    error: (error) => showError(error)
});