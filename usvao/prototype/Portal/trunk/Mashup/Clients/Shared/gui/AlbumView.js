Ext.define('Mvp.gui.AlbumView', {
    extend: 'Ext.panel.Panel',
    requires: [
        'Mvp.grid.MvpGrid',
        'Ext.view.View',
        'Ext.panel.Panel'
    ],

    /**
     * @cfg {Number} pagesize
     * The number of items to have on one page in the grid display.
     */

    constructor: function(config) {
        this.controller = config.controller;
        this.controller.on('storeupdated', this.updateView, this);
        delete config.controller;
        
        this.imagePanelTemplate = config.imagePanelTemplate;
        
        config.layout = 'fit';
        config.closable = false;
        this.callParent(arguments);
    },
    
    // Public methods
     
    updateView: function(updateObject) {
        // If there's a store, create the Grid
        if (updateObject.store) {
            if (updateObject.store !== this.lastStore) {
                // We only need to refresh the grid if this is a different store than last time.
                
                // Remove the old grid, if any.
                if (this.albumPanel) {
                    this.remove(this.albumPanel);
                }

                this.albumPanel = this.createAlbumPanel(updateObject);
                this.add(this.albumPanel);
            }
            this.lastStore = updateObject.store;
        }
    },

    // Private methods
    
    createAlbumPanel: function(updateObject) {
        var pagingbar = new Ext.toolbar.Paging({
             style: 'border:1px solid #99BBE8;',
             store: updateObject.store,
             pageSize: 5,
             displayInfo: true
         });

         var datav = new Ext.view.View({
             store: updateObject.store,
             tpl: this.imagePanelTemplate,
             multiSelect: true,
             overItemCls: 'x-view-over',
             itemSelector: 'div.thumb-wrap',
             emptyText: 'No images to display',
             style: 'border:1px solid #99BBE8; border-top-width: 0;'
         });

         var albumPanel = new Ext.panel.Panel({
             itemId: 'images-view',
             frame: false,
             autoHeight: true,
             autoScroll: true,
             layout: 'auto',
             items: [pagingbar, datav]
         });
         
         return albumPanel;
    }
    
    
});