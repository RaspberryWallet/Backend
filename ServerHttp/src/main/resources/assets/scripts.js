function refreshNetworks() {
    $.getJSON("/api/networks", function(data){
        $("#ssid").find('option').remove();
        $.each(data.networks, function(){
            var opt = $("<option>").attr("value",this).html(this);
            $("#ssid").append(opt);
        });
     });
}

function validate_form() {

    var email = $("#register_email").removeClass("error");
    var pass1 = $("#register_password").removeClass("error"), pass2 = $("#register_password2").removeClass("error");
    $("#register_button").prop("disabled", true);

    var fine = true;

    if(email.val().empty) {
        email.addClass("error");
        fine = false;
    }

    if(pass1.val().length < 6) {
        pass1.addClass("error");
        fine = false;
    }

    if(pass2.val() != pass1.val()) {
        pass2.addClass("error");
        fine = false;
    }

    if(fine) {
        $("#register_button").prop("disabled", false);
    }

    return true;
}

$(function() {
    $("#register_email").on('keyup',validate_form);
    $("#register_password").on('keyup',validate_form);
    $("#register_password2").on('keyup',validate_form);
});
