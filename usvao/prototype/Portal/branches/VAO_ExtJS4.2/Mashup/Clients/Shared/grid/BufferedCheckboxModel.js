// Using this class assumes that the store is an Mvp.data.BufferedStore.

Ext.define('Mvp.grid.BufferedCheckboxModel', {
    extend: 'Ext.selection.CheckboxModel',
    
    constructor: function(config) {
        Ext.apply(config, {
            checkOnly: true,
            showHeaderCheckbox: true
        });
        this.context = config.context;
        this.callParent(arguments);
        
        this.addEvents('APP.context.records.selected');
        this.addListener({
            selectionchange: this.selectionChangeHandler,
            buffer: 5
        });
        
        // If we have an application, listen to records selected events.
        this.app = window.app;
        if (this.app) {
            var em = this.app.getEventManager();
            em.addListener('APP.context.records.selected', this.handleAppRecordsSelected, this);
            em.addListener('APP.context.records.filtered', this.handleAppRecordsFiltered, this);
        }
        
    },

    restoreSelection: function () {
        if (!this.store) return;
        this.store.backingStore.each(function (record) {
            if (record.get('_selected_') == true) {
                this.select(record, true, true);
            } else {
                this.doDeselect(record, true, true);
            }
        }, this);
        //this.page = this.store.currentPage;
    },
    
    selectionChangeHandler: function (selectionModel, selectedRecords, options) {
        
        if (this.markSelectionChanged) {
            // The selections have changed since the last time we were here.
            // This also fires when the grid is scrolled to a new page, since
            // the selection model really only knows about one page at a time.
             this.syncHeaderCheckbox();
            
            this.fireEvent('APP.context.records.selected', {
                type: 'APP.context.records.selected',
                context: this.context
            });
        }
        this.markSelectionChanged = false;
    },
    
    handleAppRecordsSelected: function(event) {
        if ((event.sender !== this) && (event.context === this.context)) {
            // The event applies to us.
            
            // First sort the records to get the selected ones at the top, the refresh the checkbox displays.
            var store = this.context.getStore();
            if (store) {
                store.sort('_selected_', 'DESC');
            }
            
            this.restoreSelection();
            this.syncHeaderCheckbox();
        }
    },
    
    handleAppRecordsFiltered: function(event) {
        if (event.context === this.context) {
            // The event applies to us.
            this.syncHeaderCheckbox();
        }
    },
    
    syncHeaderCheckbox: function() {
        // Make sure the header checkbox is set iff all the filtered records are set.
        var store = this.context.getStore();
        if (store) {
            var allSelected = store.allSelected();
            this.toggleUiHeader(allSelected);
        }
    },
    
    // Override behavior of selectAll() and deselectAll() so that *all* the records are affected, not
    // just those in the current view.
    
    /**
     * Selects all records in the view.
     * @param {Boolean} suppressEvent True to suppress any select events
     */
    selectAll: function(suppressEvent) {
        var me = this,
            selections = me.store.getFilteredRecords(),
            i = 0,
            len = selections.length,
            start = me.getSelection().length;

        me.bulkChange = true;
        for (; i < len; i++) {
            me.doSelect(selections[i], true, suppressEvent);
        }
        delete me.bulkChange;
        // fire selection change only if the number of selections differs
        me.maybeFireSelectionChange(true); //me.getSelection().length !== start);
    },

    /**
     * Deselects all records in the view.
     * @param {Boolean} suppressEvent True to suppress any deselect events
     */
    deselectAll: function(suppressEvent) {
        var me = this,
            selections = me.store.getSelectedRecords(),
            i = 0,
            len = selections.length,
            start = selections.length;

        me.bulkChange = true;
        for (; i < len; i++) {
            me.doDeselect(selections[i], suppressEvent);
        }
        delete me.bulkChange;
        // fire selection change only if the number of selections differs
        me.maybeFireSelectionChange(start !== 0);
    },

    /**
     * Returns true if the specified row is selected.
     * @param {Ext.data.Model/Number} record The record or index of the record to check
     * @return {Boolean}
     */
    isReallySelected: function(record) {
        record = Ext.isNumber(record) ? this.store.getAt(record) : record;
        //return this.selected.indexOf(record) !== -1;
        return (record.get('_selected_') == true);
    },
    
        // records can be an index, a record or an array of records
    doDeselect: function(records, suppressEvent, forceDeselect) {
        var me = this,
            selected = me.selected,
            i = 0,
            len, record,
            attempted = 0,
            accepted = 0;

        if (me.locked || !me.store) {
            return false;
        }

        if (typeof records === "number") {
            records = [me.store.getAt(records)];
        } else if (!Ext.isArray(records)) {
            records = [records];
        }

        function commit () {
            ++accepted;
            selected.remove(record);
        }

        len = records.length;

        for (; i < len; i++) {
            record = records[i];
            if (me.isReallySelected(record) || forceDeselect) {
                if (me.lastSelected == record) {
                    me.lastSelected = selected.last();
                }
                ++attempted;
                me.onSelectChange(record, false, suppressEvent, commit);
            }
        }

        // fire selchange if there was a change and there is no suppressEvent flag
        me.maybeFireSelectionChange(accepted > 0 && !suppressEvent);
        return accepted === attempted;
    },

    /**
     * Synchronize header checker value as selection changes.
     * @private
     *
     * This is handled differently since we're buffered.  This override just
     * prevents the CheckboxModel code from executing.  That code tries to
     * keep the header checkbox in sync, but doesn't know about the whole buffered store.
     *
     * This is a copy of the RowModel code, which does need to happen.
     *
     * WARNING:  This needs to be updated whenever we change Ext JS versions.
     */
    
    onSelectChange: function(record, isSelected, suppressEvent, commitFn) {
        var me      = this,
            views   = me.views,
            viewsLn = views.length,
            store   = me.store,
            rowIdx  = store.indexOf(record),
            eventName = isSelected ? 'select' : 'deselect',
            i = 0;

        if ((suppressEvent || me.fireEvent('before' + eventName, me, record, rowIdx)) !== false &&
                commitFn() !== false) {

            for (; i < viewsLn; i++) {
                if (isSelected) {
                    views[i].onRowSelect(rowIdx, suppressEvent);
                } else {
                    views[i].onRowDeselect(rowIdx, suppressEvent);
                }
            }

            if (!suppressEvent) {
                me.fireEvent(eventName, me, record, rowIdx);
            }
        }
    },
    
    /**
     * Toggle between selecting all and deselecting all when clicking on
     * a checkbox header.
     */
    onHeaderClick: function(headerCt, header, e) {
        if (header.isCheckerHd) {
            e.stopEvent();
            var me = this,
                isChecked = header.el.hasCls(Ext.baseCSSPrefix + 'grid-hd-checker-on');
                
            me.toggleUiHeader(!isChecked);
                
            // Prevent focus changes on the view, since we're selecting/deselecting all records
            me.preventFocus = true;
            if (isChecked) {
                me.deselectAll();
            } else {
                me.selectAll();
            }
            delete me.preventFocus;
        }
    },

    /**
     * Retrieve a configuration to be used in a HeaderContainer.
     * This should be used when injectCheckbox is set to false.
     */
    getHeaderConfig: function() {
        var me = this,
            showCheck = me.showHeaderCheckbox !== false;

        return {
            isCheckerHd: showCheck,
            text : '&#160;',
            width: me.headerWidth,
            sortable: true,
            draggable: true,
            resizable: true,
            hideable: false,
            menuDisabled: false,
            width: 40,
            dataIndex: '_selected_',
            cls: showCheck ? Ext.baseCSSPrefix + 'column-header-checkbox ' : '',
            renderer: Ext.Function.bind(me.renderer, me),
            editRenderer: me.editRenderer || me.renderEmpty,
            locked: me.hasLockedHeader()
        };
    },
    
    destroy: function() {
        this.callParent(arguments);
        
        // Make sure we remove listeners (at least global ones) for this and any
        // objects this thing created.  I couldn't get Ext's "ManagedListeners" to work,
        // so we can do it manually here.
        Ext.log('Destroying a BufferedCheckboxModel');
        var em = this.app.getEventManager();
        em.removeListener('APP.context.records.selected', this.handleAppRecordsSelected, this);
        em.removeListener('APP.context.records.filtered', this.handleAppRecordsFiltered, this);
        
    }


})