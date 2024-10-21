jQuery.fn.validateMultipleNumber = function(onlyComa) {
    $(this).keyup(function(event) {
        $value = $(this).val();
        var expReg;

        if (onlyComa) {
            expReg = /[^0-9*,;]|,[0-9]*[;][,]|,[,|;]|,[,|;]|^[;|,]\b|;[,]|;[,]|^[,]|^[;]|^[,]|;;|;/g;
        } else {
            expReg = /[^0-9*,;-]|[0-9]*-[0-9]*[,|-]|,[0-9]*[-|;][,|-]|,[,|;|-]|-[-|,|;]|^[;|,|-]\b|;[-]|[0-9]*[,][0-9]*[-]|;[,]|^[-]|^[;]|^[,]|;;/g;
        }

        $aux = $value.match(expReg);
        if ($aux){
            var text = $(this).val();
            text = text.substring(0, text.length-1);
            $(this).val(text);
        }else{
            return $(this);
        }
    });

    $(this).blur(function(event) {
        $value = $(this).val();
        var text = $(this).val();        
        var s = text.substring(text.length-1, text.length);
        if(s == "-" || s == "," || s == ";"){
            $(this).val(text.substring(0, text.length-1));
        }else{
            return $(this);
        }
    });
};
