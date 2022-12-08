let new_star = $("#new-star");
let new_movie = $("#new-movie");

function handleAddStar(addEvent) {
    addEvent.preventDefault();

    starData = {};
    starData["add"] = "star";

    new_star.serializeArray().forEach(({name, value}) => {
        starData[name] = value;
    });

    if (starData["name"] !== "") {
        jQuery.ajax({
            method: "POST",
            data: starData,
            url: 'api/dashboard',
            success: (result) => {
                $("#success-star").show();
                $("#fail-star").hide();
                $("#name-fail-star").hide();
                $("#star-processed").html("Star ID: " + result['star_id']);
                $("#star-processed").show();
            },
            error: (error) => {
                $("#success-star").hide();
                $("#fail-star").show();
                $("#name-fail-star").hide();
                $("#star-processed").hide();
            }
        })
    } else {
        $("#success-star").hide();
        $("#fail-star").hide();
        $("#name-fail-star").show();
        $("#star-processed").hide();
    }
}

function handleAddMovie(addEvent) {
    addEvent.preventDefault();

    movieData = {};
    movieData["add"] = "movie";

    new_movie.serializeArray().forEach(({name, value}) => {
        movieData[name] = value;
    });

    console.log(movieData)

    if (movieData["title"] !== "" && movieData["year"] !== "" && movieData["director"] !== "" && movieData["price"] !== "") {
        jQuery.ajax({
            method: "POST",
            data: movieData,
            url: 'api/dashboard',
            success: (result) => {
                if (result['duplicate'] == true) {
                    $("#success-movie").hide();
                    $("#fail-movie").hide();
                    $("#requirement-fail-movie").hide();
                    $("#movie-processed").html("Duplicate Movie. Not added");
                    $("#movie-processed").show();
                } else {
                    $("#success-movie").show();
                    $("#fail-movie").hide();
                    $("#requirement-fail-movie").hide();
                    console.log("Movie ID: " + result['movie_id'] + ", Star ID: " + result['star_id'] + ", Genre ID: " + result['genre_id']);
                    $("#movie-processed").html("Movie ID: " + result['movie_id'] + "\nStar ID: " + result['star_id'] + "\nGenre ID: " + result['genre_id']);
                    $("#movie-processed").show();
                }
            },
            error: (error) => {
                $("#success-movie").hide();
                $("#fail-movie").show();
                $("#requirement-fail-movie").hide();
                $("#movie-processed").hide();
            }
        })
    } else {
        $("#success-movie").hide();
        $("#fail-movie").hide();
        $("#requirement-fail-movie").show();
        $("#movie-processed").hide();
    }
}

/**
 * @param resultData jsonObject
 */
function handleMetadataResult(resultData) {
    let dbTableBodyElement = jQuery("#db_list_table_body");
    var i = 0;

    if (resultData.length === 0) {
        dbTableBodyElement.append("<tr><td>No results found!</td><td></td></tr>");
        return;
    }

    for (i = 0; i < resultData.length; i++) {
        let rowHTML = "<tr>" +
            "<td>" + resultData[i]['table'] + "</td>" +
            "<td>" + resultData[i]['colType'] + "</td></tr>";

        dbTableBodyElement.append(rowHTML);
    }
}

function showError(error) {
    // jQuery.ajax 'error' argument -> error: (error) => showError(error)
    let movieTableBodyElement = jQuery("#db_list_table_body");
    movieTableBodyElement.append("<tr><th>" + JSON.stringify(error) + "</th></tr>");
}

jQuery.ajax({
    dataType: "json",
    method: "GET",
    url: 'api/dashboard',
    success: (resultData) => handleMetadataResult(resultData),
    error: (error) => showError(error)
});

new_star.submit(handleAddStar);
new_movie.submit(handleAddMovie);
