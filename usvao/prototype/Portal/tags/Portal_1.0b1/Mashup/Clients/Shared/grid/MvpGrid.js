
Ext.define('Mvp.grid.MvpGrid', {
    extend: 'Ext.grid.Panel',
    
    requires: ['Mvp.data.BufferedStore', 'Mvp.grid.MvpPagingScroller', 'Ext.ux.RowExpander'],
    
    statics: {
        createGrid: function(extjsDataSet, width, height, title, dockedItems) {
            
            // Apply widths from column ExtendedProperties.
            var allColumns = extjsDataSet.Columns;
            var columns = [];
            var sorters = [];
            var hiddenColumns = [];
            for (c in allColumns) {
                var col = allColumns[c];
                var include = true;
                if (col.ExtendedProperties) {
                    // Pull the column config attributes out of the extended properties.
                    var cc = Mvp.util.Util.extractByPrefix(col.ExtendedProperties, 'cc');
                    
                    // Width
                    if (cc.width) {
                        col.width = cc.width;
                    }
                    
                    // Column Header Text
                    if (cc.text) {
                        col.text = cc.text;
                    }
                    
                    // Sorter?
                    if (cc.sort) {
                        sorters.push({property: col.dataIndex, direction: cc.sort});
                    }
                    
                    // Visible - If visible was specified and is false, we mark this column hidden, which
                    // means it won't be shown initially, but will be available for showing in the
                    // Columns menu item that edits column visibility.
                    if (Ext.isDefined(cc.visible) && !cc.visible) {
                        col.hidden = true;
                    }
                    
                    // Hidden - If hidden was specified and is true, it means we never want to show that column to
                    // the user, so it shouldn't even be available for showing in the
                    // Columns menu item that edits column visibility.
                    if (Ext.isDefined(cc.hidden) && cc.hidden) {
                        include = false;
                    }
                    
                    if (include) {
                        columns.push(col);
                    } else {
                        hiddenColumns.push(col);
                    }
                    
                }
            }
        
            // create the Grid
            var grid = Ext.create('Mvp.grid.MvpGrid', {
                fields: extjsDataSet.Fields,
                data: extjsDataSet.Rows,
                columns: columns,
                hiddenColumns: hiddenColumns,
                sorters: sorters,
                width: width,
                height: height,
                title: title,
                dockedItems: dockedItems,
                numberRows: true
            });
            
            return grid;
        }
    },

    constructor: function (config) {
        config = config || {}; // ensure config is defined
        
        // The data and fields are required for the buffered store, but not the parent grid,
        // so save the value and remove it from the config.
        var data = config.data;
        delete config.data;
        var fields = config.fields;
        delete config.fields;
        var sorters = config.sorters || [];
        delete config.sorters;

        var store = Ext.create('Mvp.data.BufferedStore', {
            fields: fields,
            data: data,
            sorters: sorters,
            pageSize: 50
        });
        
        // We'll keep the list of hidden columns around in case we need any of that metadata for those fields.
        this.hiddenColumns = config.hiddenColumns;
        delete config.hiddenColumns;
    
        // Create the selection model.
        var sm = Ext.create('Ext.selection.RowModel');
        
        // Required config for the parent grid
        if (config.numberRows) {
            delete config.numberRows;
            config.columns = [{
                xtype: 'rownumberer',
                width: 40,
                sortable: false
                }].concat(config.columns);
        }
        Ext.apply(config, {
            store: store,
            //disableSelection: true,
            invalidateScrollerOnRefresh: false,
            plugins: [
                     // Ext.create('DemoApp.FilterRow')
                      //,{ptype: 'rowexpander',
                      //rowBodyTpl : [
                      //              '<p><b>Title:</b> {title}</p><br>',
                      //              '<p><button value="AAAAAA" onclick="{[Mvp.doTest(values, parent, xindex, xcount)]}" name="Action1" type="button">Hi There</button></p><br>',
                      //              '<p><b>Summary:</b> {description}</p>'
                      //              ]
                      //  }
                      ],
            verticalScroller: {
                xtype: 'paginggridscroller',
                //xtype: 'mvppaginggridscroller',
                activePrefetch: false
            }
            });
        

        // Default config (can be overriden by caller)
        Ext.applyIf(config, {
            width: 800,
            height: 350,
            //closable: true,
            preventHeader: true,
            loadMask: true,
            viewConfig: {
                trackOver: false
            }
            });
        
        delete config.width;
        delete config.height;

        this.callParent([config]);

        this.on('selectionchange', this.selectionChanged);
    },
    
    selectionChanged: function(selectionModel, selections, options) {
        var record = selections[0];
        if (record) {
            // Have the selection model remember which record is selected in case we scroll away from it.
            var selModel = this.getSelectionModel();
            selModel.selectedIndex = record.index;
            
            var title = record.get('title');
            Ext.log('selection changed with a record: ' + title);
        } else {
            Ext.log('selection changed with no record');
        }
    },
    
    // This is to work around a bug where sometimes the vertical scroller stops responding.  You can drag it up and
    // down, but the callback is never called.  It seems to happen after the scrollbar goes away because it's not
    // needed after a filter is applied, then comes back when the filter is taken away.
    //
    // This override tries to ensure that the callback is always registered when the scrollbar is shown.
    //
    // See the 16 Jun 2011, 2:38 PM comment in:
    // http://www.sencha.com/forum/archive/index.php/t-136263.html?s=cdae2bc4c55d9b9436b67d7a799addee
    //
    showVerticalScroller: function () {
        var me = this;

        me.setHeaderReserveOffset(true);
        if (me.verticalScroller && me.verticalScroller.ownerCt !== me) {
            //ensure scroll listener is attached
            if (me.verticalScroller.scrollEl) {
                me.verticalScroller.mun(me.verticalScroller.scrollEl, 'scroll', me.verticalScroller.onElScroll, me.verticalScroller);
                me.verticalScroller.mon(me.verticalScroller.scrollEl, 'scroll', me.verticalScroller.onElScroll, me.verticalScroller);
            }
            me.addDocked(me.verticalScroller);
            me.addCls(me.verticalScrollerPresentCls);
            me.fireEvent('scrollershow', me.verticalScroller, 'vertical');
        }
    },

    showHorizontalScroller: function () {
        var me = this;

        if (me.verticalScroller) {
            me.verticalScroller.setReservedSpace(Ext.getScrollbarSize().height - 1);
        }
        if (me.horizontalScroller && me.horizontalScroller.ownerCt !== me) {
            //ensure scroll listener is attached
            if (me.horizontalScroller.scrollEl) {
                me.horizontalScroller.mun(me.horizontalScroller.scrollEl, 'scroll', me.horizontalScroller.onElScroll, me.horizontalScroller);
                me.horizontalScroller.mon(me.horizontalScroller.scrollEl, 'scroll', me.horizontalScroller.onElScroll, me.horizontalScroller);
            }
            me.addDocked(me.horizontalScroller);
            me.addCls(me.horizontalScrollerPresentCls);
            me.fireEvent('scrollershow', me.horizontalScroller, 'horizontal');
        }
    },

    showHorizontalScroller: function() {
        var me = this;

        if (me.verticalScroller) {
            me.verticalScroller.setReservedSpace(Ext.getScrollbarSize().height - 1);
        }
        if (me.horizontalScroller && me.horizontalScroller.ownerCt !== me) {
            me.addDocked(me.horizontalScroller);
            me.addCls(me.horizontalScrollerPresentCls);
            me.fireEvent('scrollershow', me.horizontalScroller, 'horizontal');
        }
    },

    updateStatusText: function(filteredRecordCnt) {
    	var statusText = this.getDockedItems()[0].getComponent(0).getEl().dom.innerHTML;

        // Need to strip existing filter msg prior to redisplay
        // FRAGILE!  Format must be kept in sync with FacetedGridHelper.computeStatusText()
        var re = /^Displaying <b>\d+<\/b> of /;
        if (re.test(statusText)) {
            statusText = statusText.replace(re, '');
        }

        // Determine original total record count from status string
        re = /\d+/;
        var matchAry = statusText.match(re);
        var originalRecordCnt =  matchAry[0];

        // Only prepend text when the count is non-negative and does not include every record
        if (statusText && filteredRecordCnt >= 0 && filteredRecordCnt != originalRecordCnt) {
            statusText = 'Displaying <b>' + filteredRecordCnt + '</b> of ' + statusText;
        }

        Ext.log('Updating status text: ' + statusText);
        this.getDockedItems()[0].getComponent(0).setText(statusText);
    }
});

Mvp.doTest = function(values, parent, xindex, xcount) {
//    console.log('Mvp.doTest...');
    return "Mvp.doTest2()";
}

Mvp.doTest2 = function(values) {
 //   console.log('Mvp.doTest2...');
}

    
