+ (function($) {

    $('#sidebar h3').click(function() {
        $(this).next('.toggle').slideToggle('fast');
    });

})(jQuery);