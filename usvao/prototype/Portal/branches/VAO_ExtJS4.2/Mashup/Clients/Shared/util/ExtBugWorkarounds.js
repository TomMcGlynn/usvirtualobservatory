
Ext.define('Mvp.util.ExtBugWorkarounds', {
    // Require every class that gets overridden!
    requires: ['Ext.data.Store', /*'Ext.grid.PagingScroller',*/ 'Ext.view.AbstractView', 'Ext.layout.ContextItem'],
    statics: {
        closeTabBug: function () {
            // Workaround for bug in ext that prevents us from closing a tab that contains a sub-tab that has
            // never been visualized:  EXTJSIV-3294, see:
            // http://www.sencha.com/forum/showthread.php?136528-4.0.2-Store.bindStore-assumes-me.loadMask-has-bindStore-function/page2
            //
            //put at the root of your script, but inside .onReady 
            //I also happened to change 'me' to 'this' , in my override , if you compare 
            //to original source, but its not required at all 
            Ext.override(Ext.view.AbstractView, {

                bindStore: function (store, initial) {
                    //var me = this;
                    if (!initial && this.store) {
                        if (store !== this.store && this.store.autoDestroy) {
                            this.store.destroy();
                        } else {
                            this.mun(this.store, {
                                scope: this,
                                datachanged: this.onDataChanged,
                                add: this.onAdd,
                                remove: this.onRemove,
                                update: this.onUpdate,
                                clear: this.refresh
                            });
                        }
                        if (!store) {
                            if (this.loadMask && typeof this.loadMask.bindStore == 'function') {
                                this.loadMask.bindStore(null);
                            }
                            this.store = null;
                        }
                    }
                    if (store) {
                        store = Ext.data.StoreManager.lookup(store);
                        this.mon(store, {
                            scope: this,
                            datachanged: this.onDataChanged,
                            add: this.onAdd,
                            remove: this.onRemove,
                            update: this.onUpdate,
                            clear: this.refresh
                        });
                        if (this.loadMask && typeof this.loadMask.bindStore == 'function') {
                            this.loadMask.bindStore(store);
                        }
                    }
                    this.store = store;
                    this.getSelectionModel().bind(store);
                    if (store && (!initial || store.getCount())) {
                        this.refresh(true);
                    }
                }
            });
        },

        // This bug, present in ExtJS 4.1.0 RC1, makes the scroll bar handle not resize
        // when the amount of data is less than a certain threshold.
        // This was appraently fixed by RC3, so we don't need it anymore.
        scrollBarSizeBug: function () {
            Ext.override(Ext.grid.PagingScroller, {
                // Used for variable row heights. Try to find the offset from scrollTop of a common row
                // Ensure, upon each refresh, that the stretcher element is the correct height
                onViewRefresh: function () {
                    var me = this,
                        store = me.store,
                        newScrollHeight,
                        view = me.view,
                        viewEl = view.el.dom,
                        rows,
                        newScrollOffset,
                        scrollDelta,
                        table,
                        tableTop;

                    if (!store.getCount()) {
                        return;
                    }

                    // All data is in view: no buffered scrolling needed
                    if (store.getCount() === store.getTotalCount()) {
                        // TSD - Only change from original, comment out this return:
                        // return (me.disabled = true);
                    } else {
                        me.disabled = false;
                    }

                    me.stretcher.setHeight(newScrollHeight = me.getScrollHeight());

                    // If we have had to calculate the store position from the pure scroll bar position,
                    // then we must calculate the table's vertical position from the scrollProportion 
                    if (me.scrollProportion !== undefined) {
                        table = me.view.el.child('table', true);
                        me.scrollProportion = view.el.dom.scrollTop / (newScrollHeight - table.offsetHeight);
                        table = me.view.el.child('table', true);
                        table.style.position = 'absolute';
                        table.style.top = (me.scrollProportion ? (newScrollHeight * me.scrollProportion) - (table.offsetHeight * me.scrollProportion) : 0) + 'px';
                    }
                    else {
                        table = me.view.el.child('table', true);
                        table.style.position = 'absolute';
                        table.style.top = (tableTop = (me.tableStart || 0) * me.rowHeight) + 'px';

                        // ScrollOffset to a common row was calculated in beforeViewRefresh, so we can synch table position with how it was before
                        if (me.scrollOffset) {
                            rows = view.getNodes();
                            newScrollOffset = -view.el.getOffsetsTo(rows[me.commonRecordIndex])[1];
                            scrollDelta = newScrollOffset - me.scrollOffset;
                            me.position = (view.el.dom.scrollTop += scrollDelta);
                        }

                            // If the table is not fully in view view, scroll to where it is in view.
                            // This will happen when the page goes out of view undepectedly, outside the
                            // control of the PagingScroller. For example, a refresh caused by a remote sort reverting
                            // back to page 1.
                            // Note that with buffered Stores, only remote paging is allowed, otherwise the locally
                            // sorted page will be out of order with the whole dataset.
                        else if ((tableTop > viewEl.scrollTop) || ((tableTop + table.offsetHeight) < viewEl.scrollTop + viewEl.clientHeight)) {
                            me.position = viewEl.scrollTop = tableTop;
                        }
                    }
                }
            })
        },

        // I filed a bug report on this with Ext.  Animal doesn't think it's a bug.
        // http://www.sencha.com/forum/showthread.php?190791-4.1-RC1-Remote-sort-from-a-buffered-store-fails-on-small-data-sets.&p=764334
        // For now, the fix is to not return when getCount() == total.
        sortSmallDataSetBug: function () {
            Ext.override(Ext.data.Store, {
                prefetchPage: function (page, options) {
                    var me = this,
                        pageSize = me.pageSize || me.defaultPageSize,
                        start = (page - 1) * me.pageSize,
                        end = start + pageSize,
                        total = me.getTotalCount();

                    if (total) {
                        end = Math.min(end, total);
                    }

                    // No more data to prefetch.
                    //if (me.getCount() === total) {
                    //    return;
                    //}

                    // Copy options into a new object so as not to mutate passed in objects
                    me.prefetch(Ext.apply({
                        page: page,
                        start: start,
                        limit: pageSize
                    }, options));
                }
            });
        },


        // With Ext JS 4.1.0 first release, we get an exception applying a filter.  See:
        // http://help.usvao.org:8080/browse/VAOPD-357?focusedCommentId=14749&page=com.atlassian.jira.plugin.system.issuetabpanels:comment-tabpanel#comment-14749
        //
        filterBug_4_1_0: function () {
            Ext.override(Ext.data.Store, {
                cancelAllPrefetches: function () {
                    var me = this,
                        reqs = me.pageRequests,
                        req, page;
                    // If any requests return, we no longer respond to them.
                    if (me.pageMap.events.pageadded) {
                        me.pageMap.events.pageadded.clearListeners();
                    }
                    // Cancel all outstanding requests
                    for (page in reqs) {
                        if (reqs.hasOwnProperty(page)) {
                            req = reqs[page];
                            delete reqs[page];
                            if (req && req.callback) {
                                delete req.callback;
                            }
                        }
                    }
                }
            });
        },

        googleChrome18Bug: function () {
            Ext.override(Ext.layout.ContextItem, {
                setHeight: function (height, dirty) {
                    var me = this,
            comp = me.target,
            frameBody, frameInfo, padding;

                    if (isNaN(height)) {
                        return;
                    }
                    if (height < 0) {
                        height = 0;
                    }
                    if (!me.wrapsComponent) {
                        if (!me.setProp('height', height, dirty)) {
                            return NaN;
                        }
                    } else {
                        height = Ext.Number.constrain(height, comp.minHeight || 0, comp.maxHeight);
                        if (!me.setProp('height', height, dirty)) {
                            return NaN;
                        }
                        frameBody = me.frameBodyContext;
                        if (frameBody) {
                            frameInfo = me.getFrameInfo();
                            frameBody.setHeight(height - frameInfo.height, dirty);
                        }
                    }
                    return height;
                },

                setWidth: function (width, dirty) {
                    var me = this,
            comp = me.target,
            frameBody, frameInfo, padding;
                    if (isNaN(width)) {
                        return;
                    }
                    if (width < 0) {
                        width = 0;
                    }
                    if (!me.wrapsComponent) {
                        if (!me.setProp('width', width, dirty)) {
                            return NaN;
                        }
                    } else {
                        width = Ext.Number.constrain(width, comp.minWidth || 0, comp.maxWidth);
                        if (!me.setProp('width', width, dirty)) {
                            return NaN;
                        }


                        frameBody = me.frameBodyContext;
                        if (frameBody) {
                            frameInfo = me.getFrameInfo();
                            frameBody.setWidth(width - frameInfo.width, dirty);
                        }


                    }

                    return width;
                }
            });
        },


        // Grid doesn't appear when loading data sets that are smaller than the view size:
        // http://help.usvao.org:8080/browse/VAOPD-357?focusedCommentId=14753&page=com.atlassian.jira.plugin.system.issuetabpanels:comment-tabpanel#comment-14753
        noGridScroller: function () {
            Ext.override(Ext.grid.PagingScroller, {
                handleViewScroll: function (direction) {
                    var me = this,
                        store = me.store,
                        view = me.view,
                        viewSize = me.viewSize,
                        totalCount = store.getTotalCount(),
                        highestStartPoint = totalCount - viewSize,
                        visibleStart = me.getFirstVisibleRowIndex(),
                        visibleEnd = me.getLastVisibleRowIndex(),
                        requestStart, requestEnd;
                    // Only process if the total rows is larger than the visible page size
                    // TSD: this is prevent the initial showing of short data sets:  if (totalCount >= viewSize) {
                    // This is only set if we are using variable row height, and the thumb is dragged so that
                    // There are no remaining visible rows to vertically anchor the new table to.
                    // In this case we use the scrollProprtion to anchor the table to the correct relative
                    // position on the vertical axis.
                    me.scrollProportion = undefined;
                    // We're scrolling up
                    if (direction == -1) {
                        if (visibleStart !== undefined) {
                            if (visibleStart < (me.tableStart + me.numFromEdge)) {
                                requestStart = Math.max(0, visibleEnd + me.trailingBufferZone - viewSize);
                            }
                        }
                            // The only way we can end up without a visible start is if, in variableRowHeight mode, the user drags
                            // the thumb up out of the visible range. In this case, we have to estimate the start row index
                        else {
                            // If we have no visible rows to orientate with, then use the scroll proportion
                            me.scrollProportion = view.el.dom.scrollTop / (view.el.dom.scrollHeight - view.el.dom.clientHeight);
                            requestStart = Math.max(0, totalCount * me.scrollProportion - (viewSize / 2) - me.numFromEdge - ((me.leadingBufferZone + me.trailingBufferZone) / 2));
                        }
                    }
                        // We're scrolling down
                    else {
                        if (visibleStart !== undefined) {
                            if (visibleEnd > (me.tableEnd - me.numFromEdge)) {
                                requestStart = Math.max(0, visibleStart - me.trailingBufferZone);
                            }
                        }
                            // The only way we can end up without a visible end is if, in variableRowHeight mode, the user drags
                            // the thumb down out of the visible range. In this case, we have to estimate the start row index
                        else {
                            // If we have no visible rows to orientate with, then use the scroll proportion
                            me.scrollProportion = view.el.dom.scrollTop / (view.el.dom.scrollHeight - view.el.dom.clientHeight);
                            requestStart = totalCount * me.scrollProportion - (viewSize / 2) - me.numFromEdge - ((me.leadingBufferZone + me.trailingBufferZone) / 2);
                        }
                    }
                    // We scrolled close to the edge and the Store needs reloading
                    if (requestStart !== undefined) {
                        // The calculation walked off the end; Request the highest possible chunk which starts on an even row count (Because of row striping)
                        if (requestStart > highestStartPoint) {
                            requestStart = highestStartPoint & ~1;
                            requestEnd = totalCount - 1;
                        }
                            // Make sure first row is even to ensure correct even/odd row striping
                        else {
                            requestStart = requestStart & ~1;
                            requestEnd = requestStart + viewSize - 1;
                        }
                        // TSD Make sure requestStart is non-negative
                        if (requestStart < 0) {
                            requestStart = 0;
                        }
                        // If range is satsfied within the prefetch buffer, then just draw it from the prefetch buffer
                        if (store.rangeCached(requestStart, requestEnd)) {
                            me.cancelLoad();
                            store.guaranteeRange(requestStart, requestEnd);
                            if (Ext.tomCount === undefined) Ext.tomCount = 0;
                            Ext.tomCount++;
                        }
                            // Required range is not in the prefetch buffer. Ask the store to prefetch it.
                            // We will recieve a guaranteedrange event when that is done.
                        else {
                            me.attemptLoad(requestStart, requestEnd);
                        }
                    }
                    // TSD }
                }
            });
        },

        deletePageMapError: function () {
            Ext.override(Ext.data.Store, {
                onPageMapClear: function() {
                    var me = this,
                        loadingFlag = me.wasLoading,
                        reqs = me.pageRequests,
                        req,
                        page;

                    // If any requests return, we no longer respond to them.
                    if (me.data.events.pageadded) {
                        me.data.events.pageadded.clearListeners();
                    }

                    // If the page cache gets cleared it's because a full reload is in progress.
                    // Setting the loading flag prevents linked Views from displaying the empty text
                    // during a load... we don't know whether ther dataset is empty or not.
                    me.loading = true;
                    me.totalCount = 0;

                    // Cancel all outstanding requests
                    for (page in reqs) {
                        if (reqs.hasOwnProperty(page)) {
                            req = reqs[page];
                            delete reqs[page];
                            if (req) delete req.callback;
                        }
                    }

                    // This will update any views. 
                    me.fireEvent('clear', me);

                    // Restore loading flag. The beforeload event could still veto the process.
                    // The flag does not get set for real until we pass the beforeload event.
                    me.loading = loadingFlag;
                }
            });
        },

        selModelError: function () {
            Ext.override(Ext.selection.Model, {
                storeHasSelected: function (record) {
                    var store = this.store,
                        records,
                        len, id, i;

                    if (record.hasId() && store.getById(record)) {
                        return true;
                    } else {
                        records = store.data.items;
                        len = records ? records.length : 0;
                        id = record.internalId;

                        for (i = 0; i < len; ++i) {
                            if (id === records[i].internalId) {
                                return true;
                            }
                        }
                    }
                    return false;
                }
            });
        },

        refreshError: function () {
            Ext.override(Ext.view.AbstractView, {
                refresh: function () {
                    var me = this,
                        targetEl,
                        targetParent,
                        oldDisplay,
                        nextSibling,
                        dom,
                        records;

                    if (!me.rendered || me.isDestroyed) {
                        return;
                    }

                    if (!me.hasListeners.beforerefresh || me.fireEvent('beforerefresh', me) !== false) {
                        targetEl = me.getTargetEl();
                        records = me.getViewRange();
                        dom = targetEl.dom;

                        // Updating is much quicker if done when the targetEl is detached from the document, and not displayed.
                        // But this resets the scroll position, so when preserving scroll position, this cannot be done.
                        if (!me.preserveScrollOnRefresh) {
                            targetParent = dom.parentNode;
                            oldDisplay = dom.style.display;
                            dom.style.display = 'none';
                            nextSibling = dom.nextSibling;
                            targetParent.removeChild(dom);
                        }

                        if (me.refreshCounter) {
                            me.clearViewEl();
                        } else {
                            me.fixedNodes = targetEl.dom.childNodes.length;
                            me.refreshCounter = 1;
                        }

                        // Always attempt to create the required markup after the fixedNodes.
                        // Usually, for an empty record set, this would be blank, but when the Template
                        // Creates markup outside of the record loop, this must still be honoured even if there are no
                        // records.
                        me.tpl.append(targetEl, me.collectData(records || [], me.all.startIndex));

                        // The emptyText is now appended to the View's element
                        // after any fixedNodes.
                        if (records && records.length < 1) {
                            // Process empty text unless the store is being cleared.
                            if (!this.store.loading && (!me.deferEmptyText || me.hasFirstRefresh)) {
                                Ext.core.DomHelper.insertHtml('beforeEnd', targetEl.dom, me.emptyText);
                            }
                            me.all.clear();
                        } else {
                            me.collectNodes(targetEl.dom);
                            me.updateIndexes(0);
                        }

                        // Don't need to do this on the first refresh
                        if (me.hasFirstRefresh) {
                            // Some subclasses do not need to do this. TableView does not need to do this.
                            if (me.refreshSelmodelOnRefresh !== false) {
                                me.selModel.refresh();
                            } else {
                                // However, even if that is not needed, pruning if pruneRemoved is true (the default) still needs doing.
                                me.selModel.pruneIf();
                            }
                        }

                        me.hasFirstRefresh = true;

                        if (!me.preserveScrollOnRefresh) {
                            targetParent.insertBefore(dom, nextSibling);
                            dom.style.display = oldDisplay;
                        }

                        // Ensure layout system knows about new content size
                        this.refreshSize();

                        me.fireEvent('refresh', me);

                        // Upon first refresh, fire the viewready event.
                        // Reconfiguring the grid "renews" this event.
                        if (!me.viewReady) {
                            // Fire an event when deferred content becomes available.
                            // This supports grid Panel's deferRowRender capability
                            me.viewReady = true;
                            me.fireEvent('viewready', me);
                        }
                    }
                }
            })
        },

        abstractViewError: function () {
            Ext.override(Ext.view.AbstractView, {
                refresh: function() {
                    var me = this,
                        targetEl,
                        targetParent,
                        oldDisplay,
                        nextSibling,
                        dom,
                        records;

                    if (!me.rendered || me.isDestroyed) {
                        return;
                    }

                    if (!me.hasListeners.beforerefresh || me.fireEvent('beforerefresh', me) !== false) {
                        targetEl = me.getTargetEl();
                        records = me.getViewRange();
                        dom = targetEl.dom;

                        // Updating is much quicker if done when the targetEl is detached from the document, and not displayed.
                        // But this resets the scroll position, so when preserving scroll position, this cannot be done.
                        if (!me.preserveScrollOnRefresh) {
                            targetParent = dom.parentNode;
                            oldDisplay = dom.style.display;
                            dom.style.display = 'none';
                            nextSibling = dom.nextSibling;
                            targetParent.removeChild(dom);
                        }

                        if (me.refreshCounter) {
                            me.clearViewEl();
                        } else {
                            me.fixedNodes = targetEl.dom.childNodes.length;
                            me.refreshCounter = 1;
                        }

                        // Always attempt to create the required markup after the fixedNodes.
                        // Usually, for an empty record set, this would be blank, but when the Template
                        // Creates markup outside of the record loop, this must still be honoured even if there are no
                        // records.
                        me.tpl.append(targetEl, me.collectData(records, me.all.startIndex));

                        // The emptyText is now appended to the View's element
                        // after any fixedNodes.
                        if (records && (records.length < 1)) {
                            // Process empty text unless the store is being cleared.
                            if (!this.store.loading && (!me.deferEmptyText || me.hasFirstRefresh)) {
                                Ext.core.DomHelper.insertHtml('beforeEnd', targetEl.dom, me.emptyText);
                            }
                            me.all.clear();
                        } else {
                            me.collectNodes(targetEl.dom);
                            me.updateIndexes(0);
                        }

                        // Don't need to do this on the first refresh
                        if (me.hasFirstRefresh) {
                            // Some subclasses do not need to do this. TableView does not need to do this.
                            if (me.refreshSelmodelOnRefresh !== false) {
                                me.selModel.refresh();
                            } else {
                                // However, even if that is not needed, pruning if pruneRemoved is true (the default) still needs doing.
                                me.selModel.pruneIf();
                            }
                        }

                        me.hasFirstRefresh = true;

                        if (!me.preserveScrollOnRefresh) {
                            targetParent.insertBefore(dom, nextSibling);
                            dom.style.display = oldDisplay;
                        }

                        // Ensure layout system knows about new content size
                        this.refreshSize();

                        me.fireEvent('refresh', me);

                        // Upon first refresh, fire the viewready event.
                        // Reconfiguring the grid "renews" this event.
                        if (!me.viewReady) {
                            // Fire an event when deferred content becomes available.
                            // This supports grid Panel's deferRowRender capability
                            me.viewReady = true;
                            me.fireEvent('viewready', me);
                        }
                    }
                }
            });
        },

        allowHeaderCheckbox: function () {
            Ext.override(Ext.selection.CheckboxModel, {
                addCheckbox: function (view, initial) {
                    var me = this,
                        checkbox = me.injectCheckbox,
                        headerCt = view.headerCt;

                    // Preserve behaviour of false, but not clear why that would ever be done.
                    if (checkbox !== false) {
                        if (checkbox == 'first') {
                            checkbox = 0;
                        } else if (checkbox == 'last') {
                            checkbox = headerCt.getColumnCount();
                        }
                        Ext.suspendLayouts();
                        /*
                        if (view.getStore().buffered) {
                            me.showHeaderCheckbox = false;
                        }
                        */
                        headerCt.add(checkbox, me.getHeaderConfig());
                        Ext.resumeLayouts();
                    }

                    if (initial !== true) {
                        view.refresh();
                    }
                }
            });
        },

        controlLabelLayout: function () {
            Ext.override(Ext.form.Label, {
                setText: function (text, encode, skipLayout) {
                    var me = this;

                    encode = encode !== false;
                    if (encode) {
                        me.text = text;
                        delete me.html;
                    } else {
                        me.html = text;
                        delete me.text;
                    }

                    if (me.rendered && !skipLayout) {
                        me.el.dom.innerHTML = encode !== false ? Ext.util.Format.htmlEncode(text) : text;
                        me.updateLayout();
                    }
                    return me;
                }
            });
        }
    }
});