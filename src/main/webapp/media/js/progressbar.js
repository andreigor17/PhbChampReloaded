function ajaxOnStartIndicator() {
    document.body.style.cursor = 'wait';
 
    // get the current counter
    var jqDoc = jQuery(document);
    var requestCount = jqDoc.data("ajaxStatus.requestCount");
                
    if (typeof requestCount === 'undefined') {
        requestCount = 0;
    }
    $('.progress').removeClass('xhide');
    $('.progress').css({
        width: 30 + '%'
    });
 
    // increase the counter
    jqDoc.data("ajaxStatus.requestCount", requestCount+1);
}
 
function ajaxOnSuccessIndicator() {
    // get the current counter
    var jqDoc = jQuery(document);
    var requestCount = jqDoc.data("ajaxStatus.requestCount");
 
    // check the counter
    if (typeof requestCount !== 'undefined') {
        if (requestCount == 1) {
            // hide indicators
            document.body.style.cursor = 'auto';
            jqDoc.data("ajaxStatus.requestCount", 0);
        } else if (requestCount > 1) {
            // only decrease the counter
            jqDoc.data("ajaxStatus.requestCount", requestCount-1);
        }
    }
    $('.progress').css({
        width: '60%'
    });
}
            
function ajaxOnCompleteIndicator() {
    $('.progress').css({
        width: '100%'
    });
    $('.progress').addClass('xhide');
}
 
function ajaxOnErrorIndicator() {
    document.body.style.cursor = 'auto';
    // reset counter
    jQuery(document).data("ajaxStatus.requestCount", 0);
    $('.progress').css({
        width: '0%'
    });
    $('.progress').addClass('xhide');
}