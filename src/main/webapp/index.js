const ALPHABET = ["*", "0", "1", "2", "3", "4", "5", "6", "7", "8", "9",
    "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N",
    "O", "P", "Q", "R", "S", "T", "U", "V", "X", "Y", "Z"];
// const GENRES = ["Action", "Adult", "Adventure", "Animation", "Biography",
//     "Comedy", "Crime", "Documentary", "Drama", "Family", "Fantasy", "History",
//     "Horror", "Music", "Musical", "Mystery", "Reality-TV", "Romance", "Sci-Fi",
//     "Sport", "Thriller", "War", "Western"];
let search_queries = $("#search-queries");

function handleSearchQueries(searchEvent) {
    searchEvent.preventDefault();

    let query = "";
    search_queries.serializeArray().forEach(({name, value}) => {
        if (value !== "") {
            query += query !== "" ? "&" : "";
            query += name + "=" + value;
        }
    });

    if (query !== "") {
        window.location.href = "movie-list.html?" + query + "&page=1&limit=25&sort";
    }
}

// function handleBrowseQueries(browseEvent) {
//     browseEvent.preventDefault();
// }

function fillBrowseLinks(resultData) {
    let alphabetElement = jQuery("#browse-queries-alphabet");
    let alphabetString = "";
    let genreElement = jQuery("#browse-queries-genre");
    let genreString = "";

    ALPHABET.forEach(character => {
        alphabetString += "<a class='badge badge-warning' href='movie-list.html?startsWith=" + character + "&page=1&limit=25&sort'>" + character + "</a>\t";
    })
    alphabetElement.append(alphabetString);

    for (let i = 0; i < resultData.length; i++) {
        let genre = resultData[i]["genre_name"];
        genreString += "<a class='badge badge-warning' href='movie-list.html?genre=" + genre + "&page=1&limit=25&sort'>" + genre + "</a>\t"
    }
    genreElement.append(genreString);
}

// window.onload = fillBrowseLinks();
search_queries.submit(handleSearchQueries);
// browse_queries.submit(handleBrowseQueries);

$.ajax({
    dataType: "json",
    method: "GET",
    url: "api/index",
    success: (resultData) => fillBrowseLinks(resultData)
})

// Full-text Search and Autocomplete

/*
 * This function is called by the library when it needs to lookup a query.
 *
 * The parameter query is the query string.
 * The doneCallback is a callback function provided by the library, after you get the
 *   suggestion list from AJAX, you need to call this function to let the library know.
 */
function handleLookup(query, doneCallback) {
    console.log("autocomplete initiated")

    // TODO: if you want to check past query results first, you can do it here
    if (window.sessionStorage.getItem(query)) {
        console.log("using cached results")
        var jsonData = JSON.parse(window.sessionStorage.getItem(query));
        console.log(jsonData.slice(0,10))
        doneCallback( { suggestions: jsonData.slice(0,10) } );
        return;
    }

    console.log("sending AJAX request to backend Java Servlet")
    // sending the HTTP GET request to the Java Servlet endpoint hero-suggestion
    // with the query data
    jQuery.ajax({
        "method": "GET",
        // generate the request url from the query.
        // escape the query string to avoid errors caused by special characters
        "url": "movie-suggestion?query=" + query,
        "success": function(data) {
            // pass the data, query, and doneCallback function into the success handler
            handleLookupAjaxSuccess(data, query, doneCallback)
        },
        "error": function(errorData) {
            console.log(errorData)
        }
    })
}


/*
 * This function is used to handle the ajax success callback function.
 * It is called by our own code upon the success of the AJAX request
 *
 * data is the JSON data string you get from your Java Servlet
 *
 */
function handleLookupAjaxSuccess(data, query, doneCallback) {
    const result = JSON.stringify(data)
    // console.log(result)
    // parse the string into JSON
    var jsonData = JSON.parse(result);
    console.log(jsonData.slice(0,10))

    // TODO: if you want to cache the result into a global variable you can do it here
    window.sessionStorage.setItem(query,JSON.stringify(data));

    // call the callback function provided by the autocomplete library
    // add "{suggestions: jsonData}" to satisfy the library response format according to
    //   the "Response Format" section in documentation
    doneCallback( { suggestions: jsonData.slice(0,10) } );
}


/*
 * This function is the select suggestion handler function.
 * When a suggestion is selected, this function is called by the library.
 *
 * You can redirect to the page you want using the suggestion data.
 */
function handleSelectSuggestion(suggestion) {
    // TODO: jump to the specific result page based on the selected suggestion
    window.location.href = "single-movie.html?id=" + suggestion["data"]["movieID"];
}

/*
 * This statement binds the autocomplete library with the input box element and
 *   sets necessary parameters of the library.
 *
 * The library documentation can be find here:
 *   https://github.com/devbridge/jQuery-Autocomplete
 *   https://www.devbridge.com/sourcery/components/jquery-autocomplete/
 *
 */
// $('#autocomplete') is to find element by the ID "autocomplete"
$('#autocomplete').autocomplete({
    // documentation of the lookup function can be found under the "Custom lookup function" section
    lookup: function (query, doneCallback) {
        handleLookup(query, doneCallback)
    },
    onSelect: function(suggestion) {
        handleSelectSuggestion(suggestion)
    },
    // set delay time
    deferRequestBy: 300,
    // there are some other parameters that you might want to use to satisfy all the requirements
    // TODO: add other parameters, such as minimum characters
    minChars: 3
});

/*
 * do normal full text search if no suggestion is selected
 */
function handleNormalSearch(query) {
    if (query !== "") {
        window.location.href = "movie-list.html?title=" + query + "&page=1&limit=25&sort";
    }
}

$('#autocomplete').keypress(function(event) {
    if (event.keyCode == 13) {  // enter key
        handleNormalSearch($('#autocomplete').val())
    }
})

$(document).ready(function() {
    $('#submit-autocomplete').click(function() {
        handleNormalSearch($('#autocomplete').val());
    });
});
