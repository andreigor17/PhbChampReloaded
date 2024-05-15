CKEDITOR.dialog.add( 'macroDialog', function( editor ) {
    return {
        title: 'Selecionar macro',
        minWidth: 400,
        minHeight: 200,
        height: 260,
        resizable: false,
        contents: [
        {
            id: 'tab-basic',
            label: 'Basic Settings',
            elements: [
            {
                type: 'radio',
                id: 'macro',
                label: '',
                labelLayout: 'vertical',
                items: macrosDefinidas()
            //                        onClick: function() {
            //                            var dialog = this.getDialog()
            //
            //                            var macro = editor.document.createElement( 'macro' );
            //                            macro.setAttribute( 'title', dialog.getValueOf( 'tab-basic', 'macro' ) );
            //                            macro.setText( dialog.getValueOf( 'tab-basic', 'macro' ) );
            //                            editor.insertElement( macro );
            //                        }
            //                        validate: CKEDITOR.dialog.validate.notEmpty( "Abbreviation field cannot be empty." )
            }
            //                    {
            //                        type: 'text',
            //                        id: 'title',
            //                        label: 'Macro(s) selecionada(s)'
            ////                        validate: CKEDITOR.dialog.validate.notEmpty( "Explanation field cannot be empty." )
            //                    }
            ]
        }
        //            ,
        //            {
        //                id: 'tab-adv',mac
        //                label: 'Advanced Settings',
        //                elements: [
        //                    {
        //                        type: 'text',
        //                        id: 'id',
        //                        label: 'Id'
        //                    }
        //                ]
        //            }
        ],
        onOk: function() {
            var dialog = this;

            var macro = editor.document.createElement( 'macro' );
            macro.setAttribute( 'title', dialog.getValueOf( 'tab-basic', 'macro' ) );
            macro.setText( dialog.getValueOf( 'tab-basic', 'macro' ) );

            //            var id = dialog.getValueOf( 'tab-adv', 'id' );
            //            if ( id )
            //                macro.setAttribute( 'id', id );

            editor.insertElement( macro );
        }
    };
});