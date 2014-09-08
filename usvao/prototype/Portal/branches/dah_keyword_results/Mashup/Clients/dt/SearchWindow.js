/*!
 * Ext JS Library 4.0
 * Copyright(c) 2006-2011 Sencha Inc.
 * licensing@sencha.com
 * http://www.sencha.com/license
 */

Ext.define('MyDesktop.SearchWindow', {
    extend: 'Ext.ux.desktop.Module',

    //requires: [
    //    'Ext.data.ArrayStore',
    //    'Ext.util.Format',
    //    'Ext.grid.Panel',
    //    'Ext.grid.RowNumberer'
    //],

    id:'search-win',


    constructor: function (config) {
        this.portal = config.portal;
        delete config.portal;
		this.callParent(arguments);
    },

    init : function(){
        this.launcher = {
            text: 'Search Window',
            iconCls:'icon-search'
        };
    },

    createWindow : function(){
        var desktop = this.app.getDesktop();
        var win = desktop.getWindow('search-win');
        if(!win){
            var searchPanel = Ext.create('Mast.view.MastTopBar', {
                searchParams: this.portal.getSearchParams(),
                defaultSearch: 'CAOM',
                versionString: Mvp.util.Version.versionString()
            });
            searchPanel.addListener('newsearch', this.portal.searchCallback, this.portal);
            
            win = desktop.createWindow({
                id: 'search-win',
                title:'Search',
                width:870,
                y: 10,
                //height:480,
                iconCls: 'icon-search',
                animCollapse:false,
                constrainHeader:true,
                layout: 'fit',
                items: [
                    searchPanel
                ]
            });
        }
        return win;
    }
    
});

