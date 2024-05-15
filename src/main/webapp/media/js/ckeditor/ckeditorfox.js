function getHeightWidth(){
    var devicePixelRatio = window.devicePixelRatio || 1;
    dpi_x = document.getElementById('testdiv').offsetWidth * devicePixelRatio;
    var width = 0;
    var height = 0;
    if(dpi_x< 200){
        width = 592;
    //                                height = 842;
    }
    else if(dpi_x < 300){
        width = 1654;
    //                                height = 2339;
    } else if(dpi_x < 400){
        width = 2480;
    //                                height = 3508;
    } else if(dpi_x < 600){
        width = 3307;
    //                                height = 4677;
    } else if(dpi_x < 1200){
        width = 4961;
    //                                height = 7016;
    }
    else {
        width = 9921;
    //                                height = 14031;
    }
    var result = new Array();
    result[0] = width+400;
    //result[1] = height;
    result[1] = 450;
    return result;
}
function setTextAreaValue(){
    adjustMargins();
    document.getElementById(idPrefix +editorName).value = CKEDITOR.instances[editorName].getData();
}

function setarValorEditor(){
    CKEDITOR.instances[editorName].setData(document.getElementById(idPrefix +editorName).value);
    adjustMargins();
}

function corrigeBugPrimefaces(){
    var margemCima = document.getElementById(idPrefix + 'margemCimaInputText').value;
    var margemBaixo = document.getElementById(idPrefix + 'margemBaixoInputText').value;
    var margemEsquerda = document.getElementById(idPrefix + 'margemEsquerdaInputText').value;
    var margemDireita = document.getElementById(idPrefix + 'margemDireitaInputText').value;
    alterarMargens([{
        name:'topo', 
        value: margemCima
    },{
        name:'baixo', 
        value: margemBaixo
    }, {
        name:'esquerda', 
        value: margemEsquerda
    }, {
        name:'direita', 
        value: margemDireita
    }]);
}

function texto(){
    console.log(document.getElementById(idPrefix +editorName).value);
    CKEDITOR.instances[editorName].setData(document.getElementById(idPrefix +editorName).value);
    adjustMargins();
}

function adjustMargins() {
    var margemCima = 50;
    var margemBaixo = 50;
    var margemEsquerda = 50;
    var margemDireita = 50;
    var fonte = 'times';
    var tamanhoFonte = 20;
    var espacamento = null;
    var variancia = 30;

    if(document.getElementById(idPrefix + 'margemCimaInputText') != null){
        margemCima = document.getElementById(idPrefix + 'margemCimaInputText').value;
        margemBaixo = document.getElementById(idPrefix + 'margemBaixoInputText').value;
        margemEsquerda = document.getElementById(idPrefix + 'margemEsquerdaInputText').value;
        margemDireita = document.getElementById(idPrefix + 'margemDireitaInputText').value;
    } else if (document.getElementById(idPrefix + 'margemCimaText')!=null){
        margemCima = document.getElementById(idPrefix + 'margemCimaText').textContent;
        margemBaixo = document.getElementById(idPrefix + 'margemBaixoText').textContent;
        margemEsquerda = document.getElementById(idPrefix + 'margemEsquerdaText').textContent;
        margemDireita = document.getElementById(idPrefix + 'margemDireitaText').textContent;
    }
    margemCima = parseInt(margemCima, 0) + variancia;
    margemBaixo = parseInt(margemBaixo,0) + variancia;
    margemEsquerda = parseInt(margemEsquerda,0) + variancia;
    margemDireita = parseInt(margemDireita,0) + variancia;

    if(document.getElementById(idPrefix + 'fonteSelectOneMenu_label') != null){
        fonte = document.getElementById(idPrefix + 'fonteSelectOneMenu_label').textContent;
    }else if(document.getElementById(idPrefix + 'fontText') != null){
        fonte = document.getElementById(idPrefix + 'fontText').textContent;
    }

    if(document.getElementById(idPrefix + 'tamanhoFonteSelectOneMenu_label') != null){
        tamanhoFonte = document.getElementById(idPrefix + 'tamanhoFonteSelectOneMenu_label').textContent;
    }else if(document.getElementById(idPrefix + 'tamanhoFontText') != null){
        tamanhoFonte = document.getElementById(idPrefix + 'tamanhoFontText').textContent;
    }

    if(document.getElementById(idPrefix + 'espacamentoSelectOneMenu_label')!=null){
        espacamento = document.getElementById(idPrefix + 'espacamentoSelectOneMenu_label').textContent;
    }else if(document.getElementById(idPrefix + 'espacamentoText') != null){
        espacamento = document.getElementById(idPrefix + 'espacamentoText').textContent;
    }

    var html = $(CKEDITOR.instances[editorName].document.$.children);
    var body = $(CKEDITOR.instances[editorName].document.$.body);
    var wh = getHeightWidth();

    html.css('border', '1px solid #ddd');
    html.css('margin', '20px');

    html.css('height', 'auto !important');
    html.css('min-height',wh[1] + 'px');

    html.css('width', wh[0]-100 + 'px');
    html.css('min-width', wh[0]-100 + 'px');
    html.css('max-width', wh[0]-100 + 'px');

    body.css('margin-left',margemEsquerda  + 'px');
    body.css('margin-right',margemDireita+ 'px');
    body.css('margin-top',margemCima  + 'px');
    body.css('margin-bottom',margemBaixo + 'px');

    //    body.children().css('margin', '-1px');
    //    body.children().css('width', '100%');
    //    body.css('font-size','20px');
    body.css('letter-spacing','-1px');

    if(fonte == 'Courier Prime'){
        fonte = 'curier';
        body.removeClass('times');
        body.removeClass('arial');
        body.removeClass('Sans Serif');
    } else if(fonte == 'Arial') {
        fonte = 'arial';
        body.removeClass('curier');
        body.removeClass('times');
        body.removeClass('Sans Serif');
    } else if(fonte == 'Sans Serif'){
        fonte = 'Sans Serif';
        body.removeClass('curier');
        body.removeClass('arial');
        body.removeClass('times');
    } else {
        fonte = 'times';
        body.removeClass('curier');
        body.removeClass('arial');
        body.removeClass('Sans Serif');
    }

    if(tamanhoFonte == 11){
        tamanhoFonte = 18;
    } else if(tamanhoFonte == 11.5){
        tamanhoFonte = 19;
    } else if(tamanhoFonte == 12){ // Padrão
        tamanhoFonte = 20;
    } else if(tamanhoFonte == 12.5){
        tamanhoFonte = 21;
    } else if(tamanhoFonte == 13){
        tamanhoFonte = 22;
    } else if(tamanhoFonte == 13.5){
        tamanhoFonte = 23;
    } else if(tamanhoFonte == 14){
        tamanhoFonte = 24;
    } else if(tamanhoFonte == 14.5){
        tamanhoFonte = 25;
    } else if(tamanhoFonte == 15){
        tamanhoFonte = 26;
    } else if(tamanhoFonte == 15.5){
        tamanhoFonte = 27;
    } else if(tamanhoFonte == 16){
        tamanhoFonte = 28;
    } else {
        tamanhoFonte = 20;
    };

    if (espacamento == null) {
        espacamento = 1.5;
    };

    body.addClass(fonte);
    body.css('font-size', tamanhoFonte + 'px');
    body.css('line-height',espacamento);
}

function ajustarTamanhoEditor(){
    var editor = document.getElementById('cke_textoTextArea');
    editor.classList.add("col-12");
}

function fontButton(){
    var select = $('.font-foxeditor')[0];
    var tamanhoFont = $('.tamanhofont-foxeditor') != null ? $('.tamanhofont-foxeditor')[0] : null;
    var espacamento = $('.espacamento-foxeditor') != null ? $('.espacamento-foxeditor')[0] : null;
    var espacamentoIcon = $('.lineSpacing') != null ? $('.lineSpacing')[0] : null;
    var macros = $('.macros-foxeditor') != null ? $('.macros-foxeditor')[0] : null;
    var macrosDialog = $('.macrosdialogfoxeditor') != null ? $('.macrosdialogfoxeditor')[0] : null;
    var editor = document.getElementById('cke_1_toolbox');
    var content = document.getElementById('cke_1_contents');

    if (editor == null) {
        editor = document.getElementById('cke_textoTextArea').childNodes.item(1).childNodes.item(0);
    }

    if (espacamentoIcon != null && espacamento != null) {
        espacamento.insertBefore(espacamentoIcon,espacamento.firstChild);
    }

    if (macros != null) {
        editor.appendChild(macros);
    }
    editor.appendChild(select);
    if (tamanhoFont != null) {
        editor.appendChild(tamanhoFont);
    }
    if (espacamento != null) {
        editor.appendChild(espacamento);
    }
    if (macrosDialog != null) {
        content.appendChild(macrosDialog);
    }
}

$('.marginInput').on('blur', function(e) {                        
    adjustMargins();
});

$(document).ready(function() {
  
    var wh = getHeightWidth();

    CKEDITOR.on('instanceReady', function(evt){
        adjustMargins();
        fontButton();
        ajustarTamanhoEditor();
    });
    CKEDITOR.replace(editorName,{
        'height' : (wh[1] + 100),
        'width' : (wh[0] + 50)
    });
    CKEDITOR.instances[editorName].setData(document.getElementById(idPrefix+ editorName).value);
});