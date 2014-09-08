Ext.define('Mvpc.view.FilterContainer', {
    extend: 'Mvpc.view.ui.FilterContainer',
    requires: ['Mvp.util.Util',
        'Mvpc.view.FacetChooserContainer',
        'Mvpc.view.NumericFacetContainer',
        'Ext.form.Panel',
        'Ext.form.FieldSet',
        'Ext.form.Checkbox',
        'Ext.layout.container.Column',
        'Mvpc.view.TextFacetContainer'
    ],
    initComponent: function () {
        var me = this;
        me.callParent(arguments);
    },

    doFilter: function (store, searchFn) {    // applies an array of filters to a store or MixedCollection
        var filterTable = [];
        if (this.appliedNumericFilters.length) {
            var i = this.appliedNumericFilters.length;
            var filterDef = { type: 'numeric', filters: [] };
            while (i--) {
                var filter = this.appliedNumericFilters[i];
                filterDef.filters.push({ property: filter.column.dataIndex, ignoreValue: filter.ignoreValue, range: filter.range, abideIgnore: filter.abideIgnore });
            }
            this.numericFilterFn.filterDef = filterDef;
            filterTable.push(this.numericFilterFn);
        }
        if (searchFn && this.regex) {
            searchFn.filterDef = { type: 'global', regex: this.regex };
            filterTable.push(searchFn);
        }
        var f = this.filters.length;
        while (f--) {       // regular expression searches are the slowest - do them last
            var regex = '',
                filter = this.filters[f];
            i = filter.values.length;
            while (i--) {  // build a /(val1|val2|...)/ type RegExp for each column
                var val = filter.values[i];
                (regex === '') ? regex += val : regex += '|' + val;
            }
            if (regex !== '') {     // skip blank searches
                regex = '(' + regex + ')';  // complete RegExp and add to filter list
                filterTable.push({ property: filter.column,
                    value: new RegExp(regex, ''),
                    filterDef: { type: 'discrete',
                        property: filter.column,
                        values: filter.values
                    },
                    root: 'data'
                });
                filter.regExp = regex;
            }
        }

        var filteredCollection = store;
        var isStore = (store instanceof Ext.data.Store);
        if (isStore) {
            if (filterTable.length > 0) {
                // Since we know this store uses remoteFilter===true, if we just clear the list of
                // filters, the remote query to refilter will only use the new filters
                // in filterTable.
                store.filters.clear();
                store.filter(filterTable);
            } else {
                store.clearFilter();
            }
        } else {
            // store is a MixedCollection
            if (filterTable.length > 0) {
                var filterObjects = [];
                for (i = 0; i < filterTable.length; i++) {
                    var o = Ext.create('Ext.util.Filter', filterTable[i]);
                    filterObjects.push(o);
                }
                filteredCollection = store.filter(filterObjects);   // Result only defined when store is mixed collection
            }
        }

        Ext.Msg.close();
        return filteredCollection;
    },

    manageFilter: function (column, value, store, filters, addFilter, separator, separatorType) {   // add/remove a (key: value) pair filter to a set of filters
        // (column, value) = property:value pair for which to add a filter
        // filters = list of filters as {property, [val1, val2,...], regexAsString} objects, stored with the calling class
        // store = the data store for the calling class
        // addFilter = add filter, false = remove filter
        // separator = separator character for columns that can have multiple values
        // separatorType = bookend or anything else is treated as a normal separator

        if (separator) {
            var sepValue = Mvp.util.Util.escapeRegExp(separator);   // have to catch separators like |
            if (separatorType == 'bookend') {
                value = sepValue + value + sepValue;
            }
            else {
                value = '(^|' + sepValue + '\s?)' + value + '(' + sepValue + '|$)';
            }
        } else {
            value = '^' + value + '$';
        }

        if (addFilter) {    // adding a filter
            var added = false;
            var f = filters.length;
            while (f--) {
                var filter = filters[f];
                if (filter.column === column) { // seek existing column first and add values to its filter array
                    filter.values.push(value);
                    added = true;
                }
            }
            if (!added) filters.push({ column: column, values: [value] });      // add new property filter
        }
        else {      // removing a filter
            f = filters.length;
            while (f--) {
                filter = filters[f];
                if (filter.column === column) {
                    var i = filter.values.length;
                    while (i--) { // must iterate backwards because of how splice works
                        var val = filter.values[i];
                        if (val === value) filter.values.splice(i, 1);
                    }
                    break;  // don't have to search the rest of the property filters
                }
            }
        }
    },

    constructor: function (config) {
        this.callParent(arguments);
        this.facets = [];                   // specific columns of the store upon which to filter
        this.automaticFacets = [];          // stores facets created by autoCreateFacets to reuse later, if necessary
        this.discreteFacets = [];                // all facet names and their separators
        this.store = config.store;          // data store to be filtered
        this.filters = [];                  // used by filter function, an array of {property, [val1, val2,...], RegExpString} objects
        this.originalCounts = [];           // original count totals for each facet
        this.regex = '';                    // persist the value of the regex from the search box
        this.isResetting = false,           // flag to allow the updater to ignore part of its work
        this.appliedNumericFilters = [];    // exactly the numeric filters applied to the store - if a numeric range is changed to being [min, max] it is removed
        this.availableNumericFilters = [];  // the numeric filters available to be faceted
        this.chartableFacets = { decimalFacets: [], integerFacets: [], categoryFacets: [], allFacets: {} };    // used for charting
        this.staticCounts = this.store.staticCounts || false;   // flag on the store that indicates to only display the total number of entries for a given facet value
        this.allColumns = this.store.getAllColumns();   // contains all columns, column info, columns config, and histograms

        this.passThrough = {};       // copy is passed to each NumericFacetContainer
        //this.controller = config.controller

        var searchField = Ext.create('Mvp.util.SearchBox', {
            itemId: 'searchField',
            fieldLabel: '',
            hideLabel: true,
            emptyText: 'Filter All Record Fields',
            width: 250
        }),
            dockPanel = this.getComponent('dockPanel'),
            toolbar = dockPanel.getComponent('toolbar');
        searchField.on('searchInitiated', this.setSearchTerm, this);
        searchField.on('searchReset', this.searchReset, this);

        toolbar.getComponent('clearButton').addListener('click', this.reset, this);
        toolbar.getComponent('changeFiltersButton').addListener('click', this.changeFilters, this);
        toolbar.getComponent('helpButton').addListener('click', this.help, this);
        this.getComponent('dockPanel').addDocked(searchField);
        this.app = window.app;
        var em = this.app.getEventManager();
        em.addListener('APP.context.records.selected', this.prepareUpdateFromSelection, this);
        this.selectedFacets = 0;
        this.autoCreateFacets();
        this.populateFacets();
    },

    autoCreateFacets: function () {
        var isURL = /(^(http|https|ftp|ftps|ivo):|<\s*a\s*href|<\s*img\s*src|<\s*font)/,
            maxLen = 50;    // hardcoded rules

        for (var i = 0, fhtLength = this.allColumns.length; i < fhtLength; i++) {
            var column = this.allColumns[i];
            var type = column.datatype || 'string';

            var cc = Mvp.util.Util.extractByPrefix(this.allColumns[i].ExtendedProperties, 'cc'),
                rule = cc.autoFacetRule,
                vot = Mvp.util.Util.extractByPrefix(this.allColumns[i].ExtendedProperties, 'vot');
            if (rule == 'never') continue;
            var doNotAutoFacet = doNotFacet = (type == 'double') || (type == 'float'),  // rule - don't autofacet on real numbers
                hist = undefined,
                histType = undefined;
            if (column.ExtendedProperties.histObj) {
                histType = column.ExtendedProperties.histObj.type;
                hist = (histType == 'discrete') ? column.ExtendedProperties.histObj.hist : column.ExtendedProperties.histObj.hist.hist;
            }
            var len = hist ? hist.length : 1,
                separator = '',
                allCount = this.store.getCount(),
                valueCount = 0;
            if (!hist || (histType == 'discrete') && (len < 2)) continue;

            if (len >= maxLen) doNotAutoFacet = true;           // rule - number of values per facet is [2, 50]
            var j = len;
            var url = false;
            while (j--) {
                valueCount += hist[j].count;    // the histogram count size isn't necessarily the number of values
                if (hist[j].key.match(isURL)) {
                    url = true;
                    break;
                }
            }
            if (url && !rule) continue; // if we're automatically determining facets, don't allow things that look like URLs

            var min = Math.log(valueCount) / Math.LN10;
            if ((valueCount / len) < 4 * min) doNotAutoFacet = true;    // rule - the average number of values per facet has to be significant
            if ((valueCount / len) < 2 * min) doNotFacet = true;        // completely ignore very insigificant average bucket sizes

            switch (rule) {     // rule - ColumnsConfig may override the other rules - calculate automatic rule first before overriding
                case 'always': doNotAutoFacet = doNotFacet = false; break;
                case 'never': continue;
                case 'hide': doNotAutoFacet = true; break;                              // hides the facet but doesn't prevent it from being excluded
                case 'hideRequire': doNotAutoFacet = true; doNotFacet = false; break;   // hides the facet but forces it to be available
                default:
            }
            if ((histType == 'discrete') && doNotFacet) continue;   // skip rest of loop if calculated to not be a facet and not overridden by CC

            if (histType == 'numeric') this.chartableFacets.decimalFacets.push({
                column: this.allColumns[i].dataIndex,
                index: i
            });
            else if (type == 'int') this.chartableFacets.integerFacets.push({
                column: this.allColumns[i].dataIndex,
                index: i
            });
            else this.chartableFacets.categoryFacets.push({
                column: this.allColumns[i].dataIndex,
                index: i
            });

            var displayFn, readFn;
            if (cc.isMjd) {
                displayFn = Mvp.custom.Generic.mjdRenderer;
                readFn = Mvp.custom.Generic.mjdReader;
            } else if (cc.isDate) {
                displayFn = Mvp.custom.Generic.dateRenderer;
                readFn = Mvp.custom.Generic.dateReader;
            }
            else {
                displayFn = column.getDisplayValue;
                readFn = column.readDisplayValue;
            }
            
            if (histType == 'numeric') {
                var ep = column.ExtendedProperties,
                    max = ep.histObj.max,
                    min = ep.histObj.min;
                this.chartableFacets.allFacets[this.allColumns[i].dataIndex] = {
                    niceName: cc.text || this.allColumns[i].dataIndex,
                    index: i,
                    chartType: ((histType == 'numeric') || (type == 'int')) ? 'Numeric' : 'Category',
                    unit: (vot && vot.unit) || "",
                    displayFn: displayFn,
                    readFn: readFn,
                    isDate: ep.isDate || cc.isDate,
                    maxValue: max,
                    minValue: min,
                    increment: (type == 'int') ? 1: (max - min) / 1000
                };
            }

            separator = cc.separator || '';     // rule - multiply-categorized values are separated by some mark in one of two ways
            separatorType = cc.separatorType;

            if (histType == 'discrete') this.discreteFacets.push({
                column: this.allColumns[i].dataIndex,
                separator: separator,
                count: len,
                dataType: type,
                index: i
            });

            if (!doNotAutoFacet) {
                this.facets.push({
                    column: this.allColumns[i].dataIndex,
                    separator: separator,
                    separatorType: separatorType,
                    count: len,
                    dataType: type,
                    autoFacetRule: rule,
                    index: i
                });
                this.automaticFacets.push({
                    column: this.allColumns[i].dataIndex,
                    separator: separator,
                    separatorType: separatorType,
                    count: len,
                    dataType: type,
                    autoFacetRule: rule,
                    index: i
                });     // force a backup copy of the auto facets so we don't have to do this again later
            }
        }
        this.defaultDecimalFacets = (this.chartableFacets.decimalFacets.length < 21) ? this.chartableFacets.decimalFacets : [];
        this.usingDefaultFacets = true;
    },

    filter: function (caller, callerToVal, callerFromVal, options) {        // maintain a list of filters for the class
        if (options.column == '_selected_') this.selectedFacets += callerToVal ? 1 : -1;
        this.manageFilter(options.column, options.value, this.store, this.filters, callerToVal, options.separator, options.separatorType);
        this.prepareUpdate();
    },

    searchStore: function (value, store) {
        var filterAllColumns = null;
        this.regex = value;

        if (value !== '') {
            var regex = new RegExp(value, 'i');

            // Original filter.  Only checks faceted columns.
            var filterFacetedColumns = {              // universal column search function
                filterFn: function (item) {
                    var match = false,
                        i = this.facets.length;
                    while (i--) {     // check all faceted columns for the regex
                        var filterValue = this.facets[i].column;
                        var itemValue = item.get(filterValue);
                        if (itemValue.match(regex) && (itemValue !== undefined) || (itemValue !== null)) match = true;
                    }
                    return match;
                },
                scope: this,
                regex: regex
            };

            // New filter.  Filter all columns
            filterAllColumns = {              // universal column search function
                filterFn: function (item) {
                    var match = false;

                    for (i in item.data) {
                        var itemValue = item.data[i];
                        if ((itemValue !== undefined) && (itemValue !== null)) {
                            if (!Ext.isString(itemValue)) {
                                itemValue = itemValue + '';  // Force it to be a string.
                            }
                            if (itemValue.match(regex)) match = true;
                        }
                    }
                    return match;
                },
                scope: this,
                regex: regex
            };

        }
        var filteredCollection = this.doFilter(store, filterAllColumns);
        return filteredCollection;
    },

    setSearchTerm: function (value) {
        this.regex = value;
        this.prepareUpdate();
    },

    searchReset: function () {
        if (this.regex !== '') {
            this.regex = '';
            this.prepareUpdate();
        }
        var box = this.getComponent('dockPanel').getComponent('searchField');
        box.reset();
        box.focus();        // fixes minor problem of having the default text sometimes be black and sometimes gray
    },

    reset: function () {                // reset all facet value checkboxes and the search box
        this.isResetting = true;        // allows increased performance by ignoring filtering/updates triggered by checkbox changes
        this.regex = '';
        this.filters = [];
        this.appliedNumericFilters = [];
        this.selectedFacets = 0;

        var i = this.availableNumericFilters.length;
        while (i--) {
            var f = this.availableNumericFilters[i];
            f.handle.reset();
            f.range = [f.handle.getMin(), f.handle.getMax()];   // reset our known filters based on what the SliderContainer knows about its range
        }
        i = this.columnGroups.length;
        while (i--) {      // enable everything blindly
            var cg = this.columnGroups[i],
            j = cg.checkboxes.length;
            while (j--) {
                var item = cg.checkboxes[j];
                if (item.isCheckbox && item.getValue()) {
                    item.reset();
                    item.enable();
                }
            }
        }
        if (this.trueBox) {
            this.trueBox.reset();
            this.falseBox.reset();
        }
        this.getComponent('dockPanel').getComponent('searchField').reset();
        this.isResetting = false;   // allow checkbox changes again
        this.prepareUpdate();
    },

    prepareUpdate: function () {
        if (!this.isResetting) {
            Ext.Msg.wait('Filtering...');
            this.start = (new Date()).getTime();    // can uncomment the other half of this for performance testing
            var task = new Ext.util.DelayedTask(function () {
                if (this.staticCounts) {
                    this.store.clearFilter();
                    this.searchStore(this.regex, this.store);
                    this.fireEvent('filterApplied', this.filters, this.store);        // Fire an event indicating that the filters have been applied.
                } else {
                    this.setupFacets();
                }
            }, this);
            task.delay(8);      // allows the wait modal to fire before the filtering thread spinlocks the CPU
        }
    },

    prepareUpdateFromSelection: function (event) {
        if ((event.context === this.controller) && (this.selectedFacets > 0)) {
            // The event applies to us.
            this.prepareUpdate();
        }
    },

    setupFacets: function () {
        // This function performs the recomputation for the X of Y counts
        var histograms = [],
        finalHistogramTable = [];

        var f = this.filters.length;
        while (f--) {         // create a histogram for all facets that have filters applied
            var filter = this.filters[f];     // this lets us acknowledge "potential" facets and not disable them in update
            var filterName = filter.column,
            separator;
            var i = this.facets.length;
            while (i--) {     // find this facet's separator
                if (this.facets[i].column === filterName) {
                    separator = this.facets[i].separator;
                    break;
                }
            }
            i = this.filters.length;
            while (i--) {
                if (filterName === this.filters[i].column) {
                    var vals = this.filters[i].values;
                    var els = vals.splice(0, vals.length);  // split out this set of values to set up a histogram of "the other searches"
                    var filteredCollection = this.searchStore(this.regex, this.store.getUnfilteredRecords());
                    var hist = Mvp.util.Util.histogramToArray(Mvp.util.Util.histogram(filteredCollection.items, filterName, separator));
                    histograms.push({ histogram: hist, column: filterName });
                    this.filters[i].values = els;    // return the elements
                    break;
                }
            }
        }       // all potential histograms generated for text facets

        f = this.appliedNumericFilters.length;
        this.passThrough.potentialStores = [];
        while (f--) {
            var filter = this.appliedNumericFilters.splice(f, 1)[0];     // this lets us acknowledge "potential" facets and not disable them in update
            var filterName = filter.column.dataIndex;
            var filteredCollection = this.searchStore(this.regex, this.store.getUnfilteredRecords());
            this.passThrough.potentialStores.push({ store: filteredCollection, column: filterName });
            this.appliedNumericFilters.splice(f, 0, filter);
        }

        this.searchStore(this.regex, this.store);
        f = this.facets.length;
        while (f--) {   // create the "real" histogram of the filtered data
            hist = Mvp.util.Util.histogramToArray(Mvp.util.Util.histogram(this.store.getFilteredRecords(), this.facets[f].column, this.facets[f].separator));
            finalHistogramTable.push({ histogram: hist, separator: this.facets[f].separator, columnName: this.facets[f].column });
        }
        this.fireEvent('filterApplied', this.filters, this.store);        // Fire an event indicating that the filters have been applied.

        Ext.suspendLayouts();
        this.update(histograms, finalHistogramTable);
        Ext.resumeLayouts(true);
    },

    update: function (histograms, finalHistogramTable) {
        // This function enables/disables checkboxes and updates the X of Y labels
        var filtersContainer = this.getComponent('dockPanel').getComponent('filtersContainer');

        var f = this.facets.length;
        while (f--) {
            var facet = this.facets[f];
            var column = filtersContainer.getComponent(facet.column);
            var hist;
            var j = histograms.length;
            while (j--) {
                if (histograms[j].column == facet.column) {
                    hist = histograms[j];
                    break;
                }
            }
            var i = column && column.checkboxes && column.checkboxes.length;
            if (!i) continue;   // I don't know what the hell could cause this, but it came up, so abort this facet

            while (i--) {         // disable all checkboxes and count labels
                var item = column.checkboxes[i];
                (this.isResetting) ? item.enable() : item.disable();
                if (item.itemId && item.itemId.match('Count$')) {      // set count labels to 0 of original count
                    var key = item.itemId.substring(0, item.itemId.search('Count$'));
                    j = this.originalCounts.length;
                    while (j--) {
                        var obj = this.originalCounts[j];
                        if ((obj.column === facet.column) && (obj.key === key)) break;
                    }
                    var text = ((this.isResetting) ? '(' + this.originalCounts[j].count : '(0') + ' of ' + this.originalCounts[j].count + ')';
                    item.setText(text);
                }
            }

            if (hist) {     // a "potential count" histogram was generated
                j = hist.histogram.length;
                while (j--) {     // enable all checkboxes/counts for facet values with a "potential" count
                    if (hist.column !== facet.column) continue;     // not the right facet
                    key = hist.histogram[j].key;
                    var extra = column.getComponent('extraCheckboxes'),
                        box = column.getComponent(key) || (extra && extra.getComponent(key)),
                        label = column.getComponent(key + 'Count') || (extra && extra.getComponent(key + 'Count'));
                    if (box) box.enable();
                    if (label) label.enable();
                }
            }

            i = finalHistogramTable.length;
            while (i--) {        // update everything else for actual counts
                hist = finalHistogramTable[i];
                if (hist.histogram.length > 0) {
                    if (facet.column === hist.columnName) {     // get the histogram for each column, if it exists
                        j = hist.histogram.length;
                        while (j--) {         // repopulate the counts and enable checkable boxes
                            var record = hist.histogram[j],
                                extra = column.getComponent('extraCheckboxes'),
                                checkbox = column.getComponent(record.key) || (extra && extra.getComponent(record.key));
                            if (!checkbox) continue;    // skip values that are hidden to the user
                            var countbox = column.getComponent(record.key + 'Count') || (extra && extra.getComponent(record.key + 'Count'));
                            checkbox.enable();
                            var k = this.originalCounts.length, obj;
                            while (k--) {
                                obj = this.originalCounts[k];
                                if ((obj.column === hist.columnName) && (obj.key === record.key)) break;
                            }
                            countbox.setText('(' + record.count + ' of ' + obj.count + ')');
                            countbox.enable();
                        }
                    }
                }
            }       // finished updating actual counts
        }   // finished checking this facet
        //alert((new Date()).getTime() - this.start);       // see prepareUpdate
        Ext.Msg.close();
    },

    help: function () {
        var htmlText = 'The software attempts to determine which record columns (the "facets") are useful for filtering. It will allow text faceting on non-URL text and discrete collections of numbers, and it will allow numeric faceting on non-discrete collections of decimal numbers. By default it will present a facet where the average count of filterable values is proportionate to the number of records, and will disallow a facet entirely if the average count is too low. Sometimes, especially when the record count is less than 100, a facet that is available for some searches might not be present for another - this is to be expected depending on the data. <br>&nbsp;<br>The search box allows case-insensitive regular expressions in Javascript format. The data searched are all record data, including hidden record fields. <br />&nbsp;<br />  1.  "ngc" (w/o quotes) will find all records containing "ngc"<br />  2.  "ngc|m101" (w/o quotes) will find "ngc" or "m101"<br>&nbsp;<p />For numeric facets, a general histogram of the numeric distribution is presented along with a slider and bounding text boxes. Either the sliders or text boxes may be used to filter using that facet. When a facet has an alternate presentation from numeric format, the bounding boxes can be changed using either the alternate presentation or using numbers (e.g. RA/Dec are in sexagesimal; the software accepts 211 as 14:04:00.000) <p />There is also a special facet for some grids, displayed as the first facet, which allows filtering by whether a row\'s checkbox is selected in the grid.</html>';
        var win = Ext.create('Ext.window.Window', {
            width: 300, height: 350,
            minWidth: 220, minHeight: 200,
            autoScroll: true, x: 140, y: 175,
            modal: true, constrainHeader: true,
            title: 'Facet Filtering Help',
            layout: 'fit',
            padding: 3,
            items: [{
                xtype: 'panel',
                autoScroll: true,
                items: [{
                    xtype: 'container',
                    html: htmlText,
                    padding: 7
                }]
            }]
        });
        win.show();
    },

    changeFilters: function () {
        var win = Ext.create('Ext.window.Window', {
            width: 250, height: 300,
            x: 140, y: 175,
            constrainHeader: true, modal: true,
            title: 'Change Selected Facets',
            layout: 'fit'
        }),
            chooser = Ext.create('Mvpc.view.FacetChooserContainer', {
                selectedFacets: this.facets,
                defaultFacets: this.automaticFacets,
                allFacets: this.discreteFacets,
                decimalFacets: this.chartableFacets.decimalFacets,
                selectedDecimalFacets: this.availableNumericFilters,
                defaultDecimalFacets: this.defaultDecimalFacets,
                columns: this.allColumns
            });
        chooser.addListener('filtersChanged', changeFilters, this);
        win.add(chooser);
        chooser.on('filtersChanged', closeWin);
        win.show();

        function changeFilters(config) {
            this.isResetting = true;
            this.populateFacets(config);
        }

        function closeWin() {
            win.close();
            Ext.Msg.close();
        }
    },

    populateFacets: function (config) {           // broken out of constructor to allow changing facets
        if (config && config.newFacets) {
            this.facets = config.newFacets;
            this.filters = [];
            this.appliedNumericFilters = [];
            this.getComponent('dockPanel').getComponent('filtersContainer').removeAll();
            this.store.clearFilter();
            this.usingDefaultFacets = false;
        }

        function logicalSort(a, b) {
            // adapted from Mvp.util.Util
            var na = Number(a.key), nb = Number(b.key);
            if ((na == a.key) && (nb == b.key)) return na - nb;
            var sa = a.key.toString().toLowerCase(),
                sb = b.key.toString().toLowerCase();
            return sa.localeCompare(sb);
        }

        function numericSort(a, b) {
            return b.count - a.count;
        }

        this.columnGroups = [];
        this.originalCounts = [];
        for (var i in this.allColumns) {
            var c = this.allColumns[i];
            if (c.dataIndex == '_selected_') {
                this.trueBox = Ext.create('Ext.form.field.Checkbox', {
                    xtype: 'checkboxfield',
                    width: 170,
                    boxLabel: 'true',
                    itemId: 'true'
                });
                this.falseBox = Ext.create('Ext.form.field.Checkbox', {
                    xtype: 'checkboxfield',
                    width: 170,
                    boxLabel: 'false',
                    itemId: 'false'
                });
                this.trueBox.on('change', this.filter, this, { column: '_selected_', value: 'true' });
                this.falseBox.on('change', this.filter, this, { column: '_selected_', value: 'false' });
                var fs = {
                    xtype: 'fieldset',
                    collapsible: true,
                    layout: 'column',
                    itemId: 'truefalseBox',
                    width: 275,
                    title: 'Selected',
                    items: [this.trueBox, this.falseBox]
                };
                this.getComponent('dockPanel').getComponent('filtersContainer').add(fs);
            }
        }
        var maxFacets = 10;
        if (this.usingDefaultFacets && (this.facets.length > maxFacets)) {
            // write a message to the filter container
            var fs = Ext.create('Ext.form.FieldSet', {
                title: 'Additional Default Discrete Facets',
                width: 275,
                layout: 'column',
                collapsible: true,
                html: 'Only the first ' + maxFacets + ' text facets were displayed by default. Use the "Edit Facets..." button to customize the available facets.'
            });
            this.getComponent('dockPanel').getComponent('filtersContainer').add(fs);
        }

        if (!this.usingDefaultFacets || (this.facets.length != 0)) {
            var histogramTable = [];
            for (var f = 0, facetsLen = this.facets.length; f < facetsLen; f++) {
                var index = this.facets[f].index;
                var hist = this.allColumns[index].ExtendedProperties.histObj.hist;
                histogramTable.push({ histogram: hist, separator: this.facets[f].separator });
            }
            for (var i = 0, htLen = histogramTable.length; (i < htLen) && (!this.usingDefaultFacets || (i < maxFacets)); i++) {
                index = this.facets[i].index;    // index in the full array
                var fullColumnGroup = this.allColumns[index];
                var columnName = this.facets[i].column;
                var cc = Mvp.util.Util.extractByPrefix(fullColumnGroup.ExtendedProperties, 'cc');
                var columnGroup = histogramTable[i].histogram.sort(cc.numericSort ? numericSort : logicalSort);

                this.columnGroups.push(Ext.create('Mvpc.view.TextFacetContainer', {
                    itemId: columnName,
                    cc: cc,
                    parent: this,
                    columnGroup: columnGroup,
                    index: i,
                    originalCounts: this.originalCounts,
                    tip: fullColumnGroup.tip
                }));
            }

            this.getComponent('dockPanel').getComponent('filtersContainer').add(this.columnGroups);
        }

        (config && config.newNumericFacets) ? this.populateNumericFacets(config.newNumericFacets) : this.populateNumericFacets(this.chartableFacets.decimalFacets);
        if (config && (config.newFacets || config.newNumericFacets)) this.reset();
    },

    populateNumericFacets: function (selectedFacets) {
        var maxFacets = 20;     // placeholder for later customization
        var filtersContainer = this.getComponent('dockPanel').getComponent('filtersContainer');

        if (!this.usingDefaultFacets || (selectedFacets != 0)) {
            this.numericFacets = [];
            if (this.availableNumericFilters) delete this.availableNumericFilters;
            this.availableNumericFilters = [];
            for (var i = 0, len = selectedFacets.length; (i < len) && (!this.usingDefaultFacets || (i < maxFacets)); i++) {
                var index = selectedFacets[i].index,
                    column = this.allColumns[index],
                    ep = column.ExtendedProperties,
                    cc = Mvp.util.Util.extractByPrefix(ep, 'cc'),
                    vot = Mvp.util.Util.extractByPrefix(ep, 'vot'),
                    min = ep.histObj.min,
                    max = ep.histObj.max,
                    niceName = column.text,
                    displayFn = column.getDisplayValue,
                    readFn = column.readDisplayValue,
                    discrete = (cc.treatNumeric),
                    diff = max - min,
                    isDate = cc.isDate || cc.isMjd,
                    units = (vot && vot.unit) ? ' (' + vot.unit + ')' : '';

                if (!(min == NaN) && !(max == NaN)) {
                    var cfg = {
                        //MinMax config
                        facetName: column.dataIndex,
                        minValue: min,
                        maxValue: max,
                        displayFn: displayFn,
                        readFn: readFn,
                        hist: ep.histObj.hist,
                        ignoreValue: cc.ignoreValue,
                        discrete: cc.treatNumeric,
                        isDate: isDate,
                        originalStore: this.store,
                        passThrough: this.passThrough,
                        increment: isDate ? 86400000 : diff / 1000,
                        // FieldSet Config
                        itemId: column.dataIndex,
                        title: niceName + units,
                        tip: column.tip
                    };
                    var c = Ext.create('Mvpc.view.NumericFacetContainer', cfg);
                    c.addListener('render', function (fs) {
                        html = fs.tip;
                        if (html) {
                            fs.legend.on("render", function (leg) {
                                Ext.create('Ext.tip.ToolTip', {
                                    target: leg.getEl(),
                                    //title: 'Column Details',
                                    html: html,
                                    width: 200,
                                    dismissDelay: 0
                                });
                                return leg;
                            });
                        }
                        return fs;
                    }, this);

                    c.on('filterRemoved', this.filterRemoved, this);
                    c.on('rangeChanged', this.rangeChanged, this);
                    this.numericFacets.push(c);
                }
                this.availableNumericFilters.push({ column: column, range: [min, max], ignoreValue: cc.ignoreValue, handle: c });
            }
        }

        if (this.usingDefaultFacets && (selectedFacets.length > maxFacets)) {
            // write a message to the filter container
            fs = Ext.create('Ext.form.FieldSet', {
                title: 'Additional Default Numeric Facets',
                width: 275,
                layout: 'column',
                collapsible: true,
                html: 'Only the first ' + maxFacets + ' numeric facets were displayed by default. Use the "Edit Facets..." button to customize the available facets.'
            });
            this.getComponent('dockPanel').getComponent('filtersContainer').add(fs);
        }
        filtersContainer.add(this.numericFacets);
        this.paddingContainer = Ext.create('Ext.container.Container', {
            height: 5
        });

        if (this.numericFacets && this.numericFacets.length) {
            this.numericFacets[this.numericFacets.length - 1].on({
                'collapse': this.keepFocus,
                'expand': this.keepFocus,
                scope: this
            });
        }
        filtersContainer.add(this.paddingContainer);

        if (!this.numericFilterFn) this.numericFilterFn = {
            // this is defined globally to the class instance once - this allows it to work with the static filter method like the search box does
            filterFn: function fn(object, key) {
                var i = this.appliedNumericFilters ? this.appliedNumericFilters.length : 0,
                    filter;
                while (i--) {
                    filter = this.appliedNumericFilters[i];
                    var val = object.get(filter.column.dataIndex);
                    if ((filter.abideIgnore) && (val === filter.ignoreValue)) return false;
                    if ((val !== filter.ignoreValue) && ((val < filter.range[0]) || (val > filter.range[1]))) return false;
                }
                return true;
            },
            scope: this
        };
    },

    keepFocus: function () {
        this.paddingContainer.focus();
    },

    rangeChanged: function (property, vals, abideIgnore, skipAdd) {
        if (this.isResetting) return;
        if (skipAdd !== true) {
            // when a NumericFacetContainer fires a numeric facet change that is only telling filters to abide by 
            // the ignore value, it has to skip the step of adding the filter it just removed
            var i = this.appliedNumericFilters.length,
                filter,
                found = false;

            while (i--) {
                filter = this.appliedNumericFilters[i];
                if (filter.column.dataIndex == property) {
                    filter.range = [vals[0], vals[1]];
                    filter.abideIgnore = abideIgnore;
                    found = true;
                    break;
                }
            }
            if (!found) {
                i = this.availableNumericFilters.length;
                while (i--) {
                    filter = this.availableNumericFilters[i];
                    if (filter.column.dataIndex == property) {
                        var f = Ext.clone(filter);
                        f.range = [vals[0], vals[1]];
                        f.abideIgnore = abideIgnore;
                        this.appliedNumericFilters.push(f);
                        break;
                    }
                }
            }
        }
        if (!this.isResetting) {
            this.prepareUpdate(this);    // pushes filter changes to all other filters, including updating counts
        }
    },

    filterRemoved: function (property) {    // splice numeric filter from the array of applied filters
        var i = this.appliedNumericFilters.length,
            filter;
        while (i--) {
            filter = this.appliedNumericFilters[i];
            if (filter.column.dataIndex == property) {
                this.appliedNumericFilters.splice(i);
                break;
            }
        }
    },

    getChartableFacets: function () {
        return this.chartableFacets;
    }
});