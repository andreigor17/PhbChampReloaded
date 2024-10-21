function widthFix() {
    
    var autocompleteWidth;
    var buttonWidth;
    var finalWidth;

    $('.ui-selectonemenu').each(function() {
        $(this).css({'display': 'inline-block', 'width': '100%'});
        autocompleteWidth = $(this).outerWidth();
        buttonWidth = $(this).children('.ui-selectonemenu-trigger').outerWidth();
        finalWidth = autocompleteWidth - buttonWidth;
        $(this).css({'width': autocompleteWidth});
        $(this).children('label').css({'width': autocompleteWidth});
        $('.ui-selectonemenu-panel').css({'width': autocompleteWidth});
    });

    $('.ui-autocomplete').each(function() {
        $(this).css({'display': 'inline-block', 'width': '100%'});
        autocompleteWidth = $(this).outerWidth();
        buttonWidth = $(this).children('button').outerWidth();
        finalWidth = autocompleteWidth - buttonWidth;
        $(this).children('input').css({'width': finalWidth});
    });

    $('.hasDatepicker').each(function() {
        $(this).css({'display': 'inline-block', 'width': '100%'});
        autocompleteWidth = $(this).outerWidth();
        buttonWidth = $(this).next('button').outerWidth();
        finalWidth = autocompleteWidth - buttonWidth;
        $(this).parent().css({'display': 'inline-block', 'width': finalWidth + 30});
        $(this).css({'width': finalWidth});
    });
    
    $("[id*=numeroTelefoneInputText], [id*=numeroCelularInputText]").focusout(function(){
            var phone, element;
            element = $(this);
            element.unmask();
            phone = element.val().replace(/\D/g, '');
            if (phone.length > 8) {
                element.mask("99999-9999");
            } else {
                element.mask("9999-9999?9");
            }
    }).trigger('focusout');    

}
