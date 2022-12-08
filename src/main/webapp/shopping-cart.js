let cart = jQuery("#cart_form");
let checkout = jQuery("#checkout_button");

function removeItem(id) {
    $.ajax({
        method: "POST",
        url: "api/shopping-cart",
        data: {"id": id, "remove": "true"},
        success: () => {
            window.location.replace("./shopping-cart.html");
        },
        error: showError
    })
}

function handleShoppingCartArray(resultArray) {
    let shoppingCartTableElement = jQuery("#cart_items_list");
    let emptyCartLabelElement = jQuery("#empty_cart_label");
    let totalPriceElement = jQuery("#total_price");

    shoppingCartTableElement.html("");
    if (resultArray.length === 0) {
        emptyCartLabelElement.show();
        return;
    }

    let res = "<ul class='list-group list-group-flush'>";
    let totalPrice = 0.0;
    for (let i = 0; i < resultArray.length; i++) {
        let itemsPrice = (Math.round(parseFloat(resultArray[i]['priceForEach']) * resultArray[i]['count'] * 100) / 100);
        res += "<li class='list-group-item d-flex justify-content-between align-items-center'>" +
            resultArray[i]["title"] +
            "<input class='form-control' name='" + resultArray[i]['id'] + "' id='" + i + "' type='number' min='1' style='max-width: 100px;' value='" + resultArray[i]["count"] + "'>" +
            "$<div id='p" + i + "'>" + itemsPrice + "</div>" +
            "<button class='btn btn-danger my-2 my-sm-0' onclick='removeItem(\"" + resultArray[i]['id'] + "\")'>Delete</button>";

        totalPrice += itemsPrice;
    }
    res += "</ul>";

    emptyCartLabelElement.hide();
    shoppingCartTableElement.append(res);
    totalPriceElement.html("<h4>$" + (Math.round(totalPrice * 100) / 100).toString() + "</h4>");
}

function handleShoppingCartGet(resultDataJson) {
    handleShoppingCartArray(resultDataJson["cart_items"]);
}


function handleShoppingCartPost(event) {
    event.preventDefault();
    let m = cart.serializeArray();

    for (let i = 0; i < m.length; i++) {
        jQuery.ajax({
            method: "POST",
            url: "api/shopping-cart",
            data: {"id": m[i]['name'], "quantity": m[i]['value']},
            success: resultDataString => {
                $("#success_save").show();
                $("#fail_save").hide();
                let resultDataJson = JSON.parse(resultDataString);
                handleShoppingCartArray(resultDataJson["cart_items"]);
            },
            error: (error) => {
                console.log(error);
                $("#success_save").hide();
                $("#fail_save").show();
            }
        })
    }
}

function showError(error) {
    // jQuery.ajax 'error' argument -> error: (error) => showError(error)
    let errorStringElement = jQuery("#error_string");
    errorStringElement.append(JSON.stringify(error));
}

jQuery.ajax({
    dataType: "json",
    method: "GET",
    url: "api/shopping-cart",
    success: handleShoppingCartGet,
    error: showError
})

$("#cart_items_save").on("click", handleShoppingCartPost);
checkout.on("click", function() {
    window.location.href = "payment.html";
})