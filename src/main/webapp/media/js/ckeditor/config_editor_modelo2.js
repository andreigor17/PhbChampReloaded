CKEDITOR.editorConfig = function( config ) {
    config.language = 'pt-br';
    config.width = 820;
    config.uiColor = '#CB2424';

    config.toolbarGroups = [
        { name: 'clipboard', groups: [ 'clipboard', 'undo' ]},
        {name: 'basicstyles', groups: [ 'basicstyles', 'cleanup']},
        { name: 'insert' },
        { name: 'styles', groups : ['font' ] },
        {name: 'paragraph', groups: [ 'list', 'indent', 'blocks', 'align', 'bidi', 'paragraph' ]},
        {name: 'tools', groups: [ 'tools' ]},
        {name: 'others', groups: [ 'others']},
    ];
    
    config.fontSize_style = {
        element      : 'span',
        styles      : { 'font-size' : '12px' },
        overrides   : [ { element : 'font', attributes : { 'size' : '12px' } } ]
    };
    config.fontSize_defaultLabel = '12px';
    config.resize_enabled = false;
    config.removeButtons = 'Styles,Font,Format, Cut,Copy,Paste,Source,NewPage,Save,Preview,Print,Templates,Scayt,Form,Checkbox,Radio,TextField,Textarea,Select,ImageButton,HiddenField,Button,Image,Flash,Anchor,Unlink,Link,HorizontalRule,PageBreak,Iframe,BGColor,ShowBlocks,About,BidiRtl,Language,BidiLtr,CreateDiv,Blockquote,Smiley';
    config.removePlugins = 'elementspath';
    config.disableNativeSpellChecker = false;
};