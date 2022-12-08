const PARAMETERS = ["title", "year", "director", "star", "startsWith", "genre", "page", "limit", "sort"];
const GENRE_LIMIT = 3;
const STAR_LIMIT = 3;

function addToCart(elem) {
    let id = $(elem).attr("id");
    let successString = "#success" + id;
    let failString = "#fail" + id;
    console.log(successString);
    jQuery.ajax({
        method: "POST",
        data: {"id": id},
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
}

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

function setButtons(){
    $("#currentBtn").html("View " + getParameterByName("limit"));
    var sort = getParameterByName("sort");
    var title = "";
    var rating = "";

    if (sort.includes("titleAsc")) {
        title = "titleAsc";
        $("#sortTitleBtn").html("Title ↑");
    } else if (sort.includes("titleDes")) {
        title = "titleDes";
        $("#sortTitleBtn").html("Title ↓");
    }
    if (sort.includes("ratingAsc")) {
        rating = "ratingAsc";
        $("#sortRatingBtn").html("Rating ↑");
    } else if (sort.includes("ratingDes")) {
        rating = "ratingDes";
        $("#sortRatingBtn").html("Rating ↓");
    }

    var current_url = window.location.toString();
    var pageIndex = current_url.indexOf("&page");
    var sortIndex = current_url.indexOf("&sort");
    var noPage = current_url.substring(0,pageIndex);
    var saveSort = current_url.substring(sortIndex);
    var noSort = current_url.substring(0,sortIndex);
    var page = getParameterByName("page");
    var nextPage = (parseInt(page)+1).toString();
    var prevPage = (parseInt(page)-1).toString();
    var nextUrl = window.location.toString().replace("&page="+page, "&page="+nextPage);
    var prevUrl = window.location.toString().replace("&page="+page, "&page="+prevPage);

    if (sort == "") {
        var titleAsc_url = noSort + "&sort=titleAsc";
        var titleDes_url = noSort + "&sort=titleDes";
        var ratingAsc_url = noSort + "&sort=ratingAsc";
        var ratingDes_url = noSort + "&sort=ratingDes";
    } else {
        var titleAsc_url = noSort + "&sort=" + "titleAsc" + rating;
        var titleDes_url = noSort + "&sort="  + "titleDes" + rating;
        var ratingAsc_url = noSort + "&sort=" + title + "ratingAsc";
        var ratingDes_url = noSort + "&sort=" + title + "ratingDes";
    }

    var view1_url = noPage + "&page=1&limit=10" + saveSort;
    var view2_url = noPage + "&page=1&limit=25" + saveSort;
    var view3_url = noPage + "&page=1&limit=50" + saveSort;
    var view4_url = noPage + "&page=1&limit=100" + saveSort;
    $("#titleAsc").attr("href", titleAsc_url);
    $("#titleDes").attr("href", titleDes_url);
    $("#ratingAsc").attr("href", ratingAsc_url);
    $("#ratingDes").attr("href", ratingDes_url);
    $("#view1Btn").attr("href", view1_url);
    $("#view2Btn").attr("href", view2_url);
    $("#view3Btn").attr("href", view3_url);
    $("#view4Btn").attr("href", view4_url);
    $("#nextBtn").attr("href", nextUrl);
    if (parseInt(page) != 1) {
        $("#prevBtn").attr("href", prevUrl);
    }
}

/**
 * @param resultData jsonObject
 */
function handleMovieResult(resultData) {
    let movieTableBodyElement = jQuery("#movie_list_table_body");
    var i = 0;

    if (resultData.length === 0) {
        movieTableBodyElement.append("<tr><td>No results found!</td><td>" +
            "</td><td></td><td></td><td></td><td></td></tr>");
        return;
    }

    for (i = 0; i < resultData.length; i++) {
        let rowHTML = "<tr>" +
            "<td>" +
            '<a class="badge badge-dark" href="single-movie.html?id=' + resultData[i]['movie_id'] + '">' +
            resultData[i]['movie_title'] +
            "</a>" +
            "</td>" +
            "<td>" +
            resultData[i]['movie_year'] +
            "</td>" +
            "<td>" +
            resultData[i]['movie_director'] +
            "</td><td>";

        let genre_array = resultData[i]['movie_genres'].split(",");
        for (let i = 0; i < Math.min(genre_array.length, GENRE_LIMIT); i++) {
            rowHTML += genre_array[i] + "<br>";
        }
        rowHTML += "</td><td>";

        let star_array = resultData[i]['movie_stars'].split(",");
        let starid_array = resultData[i]['movie_starsId'].split(",");
        for (let j = 0; j < Math.min(star_array.length, STAR_LIMIT); j++) {
            rowHTML += "<a class='badge badge-warning' href='single-star.html?id=" +
                starid_array[j] + "'>" + star_array[j] + "</a><br>";
        }

        rowHTML +=
            "</td><td>" + resultData[i]['movie_rating'] + "</td>" +
            "<td><button name='add_button' id='" + resultData[i]['movie_id'] +
            "' type='button' class='btn btn-dark btn-sm my-2 my-sm-0' onClick='addToCart(this)'>Add to cart</button>" +
            "<div class='mt-2 text-success' id='success" + resultData[i]["movie_id"] + "' style='display: none'>Successfully added.</div>" +
            "<div class='mt-2 text-danger' id='fail" + resultData[i]["movie_id"] + "' style='display: none'>Failed to add.</div></td></tr>";

        movieTableBodyElement.append(rowHTML);
    }
    var limit = getParameterByName("limit");
    if (i < parseInt(limit) - 1) {
        $("#nextBtn").attr("href", "#");
    }
}

function showError(error) {
    // jQuery.ajax 'error' argument -> error: (error) => showError(error)
    let movieTableBodyElement = jQuery("#movie_list_table_body");
    movieTableBodyElement.append("<tr><th>" + JSON.stringify(error) + "</th></tr>");
}

function getURL() {
    let url = "api/movie-list?";
    let query = "";
    PARAMETERS.forEach((param) => {
        if (getParameterByName(param) !== null) {
            query += query !== "" ? "&" : "";
            query += param + "=" + getParameterByName(param);
        }
    })
    return url + query;
}

$(document).ready(function() {
    document.cookie = "lastUrl=" + window.location.href;
})

jQuery.ajax({
    dataType: "json",
    method: "GET",
    url: getURL(),
    success: (resultData) => handleMovieResult(resultData),
    error: (error) => showError(error)
});
