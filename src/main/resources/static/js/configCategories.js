$("#SICategoriesButton").click(function () {
    selectElement($(this));
    $("#SICategories").show();
    $("#FactorsCategories").hide();
});

$("#FactorsCategoriesButton").click(function () {
    selectElement($(this));
    $("#SICategories").hide();
    $("#FactorsCategories").show();
});

function selectElement (selectedElement) {
    selectedElement.addClass("active");
    $(".category-element").each(function () {
        if (selectedElement.attr("id") !== $(this).attr("id"))
            $(this).removeClass("active");
    });
}