function getHeightWidth(){
    var devicePixelRatio = window.devicePixelRatio || 1;
    dpi_x = document.getElementById('testdiv').offsetWidth * devicePixelRatio;
    var width = 0;
    var height = 0;
    if(dpi_x< 200){
        width = 592;
    //                                height = 842;
    } else if(dpi_x < 300){
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
    }else {
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

function adjustMargins() {


    var margemCima = 50;
    var margemBaixo = 50;
    var margemEsquerda = 50;
    var margemDireita = 50;
    var fonte = 'times';
    var tamanhoFonte = 20;
    var espacamento = 1;
    
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
    var variancia = 30;
    margemCima = parseInt(margemCima, 0) + variancia;
    margemBaixo = parseInt(margemBaixo,0) + variancia;
    margemEsquerda = parseInt(margemEsquerda,0) + variancia;
    margemDireita = parseInt(margemDireita,0) + variancia;
    
    if(document.getElementById(idPrefix + 'fonteSelectOneMenu_label')!=null){
        fonte = document.getElementById(idPrefix + 'fonteSelectOneMenu_label').textContent;
    }else{
        fonte = document.getElementById(idPrefix + 'fontText').textContent;
    }
    
    if(document.getElementById(idPrefix + 'tamanhoFonteSelectOneMenu_label')!=null){
        tamanhoFonte = document.getElementById(idPrefix + 'tamanhoFonteSelectOneMenu_label').textContent;
    }else{
        tamanhoFonte = document.getElementById(idPrefix + 'tamanhoFontText').textContent;
    }
    
    if(document.getElementById(idPrefix + 'espacamentoSelectOneMenu_label')!=null){
        espacamento = document.getElementById(idPrefix + 'espacamentoSelectOneMenu_label').textContent;
    }else{
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


    body.children().css('margin', '-1px');
    body.children().css('width', '100%');
    body.css('letter-spacing','-1px');
    
    if(fonte == 'Curier'){
        fonte = 'curier';
        body.removeClass('times');
        body.removeClass('arial');
    } else if(fonte == 'Arial') {
        fonte = 'arial';
        body.removeClass('curier');
        body.removeClass('times');
    } else {
        fonte = 'times';
        body.removeClass('curier');
        body.removeClass('arial');
    }
    body.addClass(fonte);
    body.css('font-size',tamanhoFonte + 'px');
    body.css('line-height',espacamento);
    
}

function componentesAdicionais(){
    var font = $('.font-foxeditor')[0];
    var tamanhoFont = $('.tamanhofont-foxeditor')[0];
    var espacamento = $('.espacamento-foxeditor')[0];
    var espacamentoIcon = $('.lineSpacing')[0];
    espacamento.insertBefore(espacamentoIcon,espacamento.firstChild);
    var editor = document.getElementById('cke_1_toolbox');
    editor.appendChild(font);
    editor.appendChild(tamanhoFont);
    editor.appendChild(espacamento);
}

$('.marginInput').on('blur', function(e) {                        
    adjustMargins();
});

$(document).ready(function() {
                        
    var wh = getHeightWidth();
                            
    CKEDITOR.on('instanceReady', function(evt){
        adjustMargins();
        componentesAdicionais();
    });
    
    CKEDITOR.replace(editorName,{
        'height' : (wh[1] + 100),
        'width' : (wh[0] + 50)
    });
    console.log(idPrefix+ editorName);
    CKEDITOR.instances[editorName].setData(document.getElementById(idPrefix+ editorName).value);

});
