let form = $("#payment_form");

function handlePaymentGet(resultDataJson) {
    $("#total_price").text("$" + resultDataJson["total_price"]);
}

function handlePaymentSuccess(resultDataJson) {
    window.location.replace("./confirmation.html?salesId=" + resultDataJson["sales_id"]);
}

function handlePaymentPost(event) {
    event.preventDefault();
    if (parseFloat($("#total_price").text().split("$")[1]) > 0.0) {
        $.ajax({
            method: "POST",
            url: "api/payment",
            data: form.serialize(),
            success: handlePaymentSuccess,
            error: () => {
                $("#fail_payment").show();
            }
        })
    }
}

function showError(error) {
    // jQuery.ajax 'error' argument -> error: (error) => showError(error)
    let movieTableBodyElement = jQuery("#movie_list_table_body");
    movieTableBodyElement.append("<tr><th>" + JSON.stringify(error) + "</th></tr>");
}

$.ajax({
    dataType: "json",
    url: "api/payment",
    method: "GET",
    success: handlePaymentGet,
    error: showError
})

form.submit(handlePaymentPost);

