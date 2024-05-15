jQuery.fn.validateNumberOnly = function(value) {
    $(this).keyup(function(event) {
        $value = $(this).val();
        var expReg;

        if (value) {
            expReg = /(\D)|^0./g;
        } else {
            expReg = /(\D)/g;
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
};
