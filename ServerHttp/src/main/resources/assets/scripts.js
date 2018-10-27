function refreshNetworks() {
    $.getJSON("/api/networks", function(data){
        $("#ssid").find('option').remove();
        $.each(data.networks, function(){
            var opt = $("<option>").attr("value",this).html(this);
            $("#ssid").append(opt);
        });
     });
}
