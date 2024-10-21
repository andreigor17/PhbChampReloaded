/* 
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

function fixDatePattern(currDate) {
    var currentDate = currDate;
    var currentLength = currentDate.length;
    var lastNumberEntered = currentDate[currentLength - 1];

    if (!isNumber(lastNumberEntered)) {
        return currentDate.substring(0, currentLength - 1);
    }

    if (currentLength > 10) {
        return currentDate.substring(0, 10);
    }

    if (currentLength == 1 && currentDate > 1) {
        var transformedDate = "0" + currentDate + '/';
        dateCountTracker = 2;
        currentLength = transformedDate.length;
        return transformedDate;
    } else if (currentLength == 4 && currentDate[3] > 3) {
        var transformedDate = currentDate.substring(0, 3) + "0" + currentDate[3] + '/';
        dateCountTracker = 5;
        currentLength = transformedDate.length;
        return transformedDate;
    } else if (currentLength == 2 && (dateCountTracker != 2 && dateCountTracker != 3)) {
        dateCountTracker = currentLength;
        return currentDate + '/';
    } else if (currentLength == 5 && (dateCountTracker != 5 && dateCountTracker != 6)) {
        dateCountTracker = currentLength;
        return currentDate + '/';
    }
    dateCountTracker = currentLength;
    return currentDate;
}

function isNumber(n) {
    return !isNaN(parseFloat(n)) && isFinite(n);
}

