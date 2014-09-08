//This quite elegant solution for tooltips taken directly from the Sencha forums
// http://www.sencha.com/forum/showthread.php?19301-SOLVED-Extended-form-in-window-gt-Shadow-problem

 Ext.override(Ext.form.Field, {
   afterRender: function() {
         if(this.helpText){
             var label = findLabel(this);
             if(label){                
                  var helpImage = label.createChild({
                          tag: 'img', 
                          src: 'images/information.png',
                          cls: 'info-tooltip',
                          style: 'margin-bottom: 0px; margin-left: 5px; padding: 0px;',
                          width: 18,
             			  height: 18
                      });                        
                 Ext.QuickTips.register({
                     target:  helpImage,
                     title: '',
                     hideDelay: 8000,
                     minWidth: 400,
                     width: 200,
                     text: this.helpText,
                     enabled: true
                 });
             }
           }
           Ext.form.Field.superclass.afterRender.call(this);
           this.initEvents(); 
           this.initValue();
   }
 });

 var findLabel = function (field) {
     var label = null
     //var wrapDiv = null
     //wrapDiv = field.getEl().up('div.x-form-item');    
     var wrapDiv = field.getEl();
     if (wrapDiv) {
         label = wrapDiv.child('label');
     }
     if (label) {
         return label;
     }
 }

 