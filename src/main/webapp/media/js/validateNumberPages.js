jQuery.fn.validateNumberPages = function(value) {
    $(this).keyup(function(event) {
        $value = $(this).val();
        var expReg;

        expReg = /[^0-9*V]|^V|V[0-9]+|VV+/g;
                        
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
