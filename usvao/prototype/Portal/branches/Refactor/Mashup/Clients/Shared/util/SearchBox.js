Ext.define('Mvp.util.SearchBox', {
    extend: 'Ext.form.field.Trigger',
    
    alias: 'widget.searchbox',
    
    trigger1Cls: Ext.baseCSSPrefix + 'form-clear-trigger',
    
    trigger2Cls: Ext.baseCSSPrefix + 'form-search-trigger',
    
    hasSearch : false,
    paramName : 'query',
    
    constructor: function(config) {
        var me = this;

        this.callParent([config]);
    },
    
    initComponent: function(){
        this.callParent(arguments);
        this.on('specialkey', function(f, e){
            if(e.getKey() == e.ENTER){
                this.onTrigger2Click();
            }
        }, this);
        this.enableBubble('searchInitiated');
    },
    
    afterRender: function(){
        this.callParent();
        this.triggerEl.item(0).setVisibilityMode(Ext.core.Element.DISPLAY);
    },
    
    onTrigger1Click : function(){
        var me = this;
            
        if (me.hasSearch) {
            me.setValue('');
            me.hasSearch = false;
            me.doComponentLayout();
        }

        me.reset();
        me.fireEvent('searchReset');
    },

    onTrigger2Click : function(){
        var me = this,
            value = me.getValue();
            
        if (value.length < 1) {
            me.onTrigger1Click();
            return;
        }
        me.hasSearch = true;
        me.doComponentLayout();
        
        me.fireEvent('searchInitiated', value);
    },
    
    setHint: function(hint){
        var me = this;
        me.emptyText = hint;
    },

    getHint: function(){
        var me = this;
        return me.emptyText;
    }
});
