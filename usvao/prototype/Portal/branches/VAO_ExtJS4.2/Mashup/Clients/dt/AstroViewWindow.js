/*!
 * Ext JS Library 4.0
 * Copyright(c) 2006-2011 Sencha Inc.
 * licensing@sencha.com
 * http://www.sencha.com/license
 */

Ext.define('MyDesktop.AstroViewWindow', {
    extend: 'Ext.ux.desktop.Module',

    //requires: [
    //    'Ext.data.ArrayStore',
    //    'Ext.util.Format',
    //    'Ext.grid.Panel',
    //    'Ext.grid.RowNumberer'
    //],

    id:'astroview-win',


    constructor: function (config) {
        this.portal = config.portal;
        delete config.portal;
		this.callParent(arguments);
    },

    init : function(){
        this.launcher = {
            text: 'AstroView',
            iconCls:'icon-astroview'
        };
    },

    createWindow : function(){
        var desktop = this.app.getDesktop();
        var win = desktop.getWindow('astroview-win');
        if(!win){
			var el = desktop.el;
			var w = el.getWidth();
			var h = el.getHeight();
			Ext.log('el.w = ' + w + ', el.h = ' + h);
			var x = w * 0.6;
			var width = (w - x) - 20;
			var y = 140;
			height = h - y - 50;
			Ext.log('x = ' + x + ', y = ' + y);
			Ext.log('width = ' + width + ', height = ' + height);
			
			var avRenderType = AppConfig.avRenderType || 'canvas';
            var avSurveyType = AppConfig.avSurveyType || 'DSS';
			var avPlacement = AppConfig.avPlacement || 'east';
            var avPanel = Ext.create('Mvpc.view.AstroViewContainer', {
                //width: 340,
                rendertype: avRenderType,
                surveytype: avSurveyType,
                //region: avPlacement,
                //split: false,
                collapsible: false,
				preventHeader: true,
                animCollapse: false,
                app: this.portal.app

            });
			
            win = desktop.createWindow({
                id: 'astroview-win',
                title:'AstroView',
                width: width,
                height: height,
				x: x,
				y: y,
                iconCls: 'icon-astroview',
                animCollapse:false,
                constrainHeader:true,
                layout: 'fit',
				closable: false,
                items: [
                    avPanel
                ]
            });
        }
        return win;
    }
    
});

