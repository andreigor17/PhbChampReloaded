CKEDITOR.plugins.add( 'macro', {
    icons: 'macro',
    init: function( editor ) {
        editor.addCommand( 'macro', new CKEDITOR.dialogCommand( 'macroDialog' ) );
        editor.ui.addButton( 'Macro', {
            label: 'Macros disponíveis',
            command: 'macro',
            toolbar: 'insert',
            icon: this.path + 'images/macro.png'
        });

        CKEDITOR.dialog.add( 'macroDialog', this.path + 'dialogs/macro.js' );
    }
});