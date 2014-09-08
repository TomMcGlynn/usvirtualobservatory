/*
* File: app/view/MinMaxSliderContainer.js
* Date: Thu Jan 26 2012 11:26:47 GMT-0500 (Eastern Standard Time)
*
* This file was generated by Ext Designer version 1.2.2.
* http://www.sencha.com/products/designer/
*
* This file will be generated the first time you export.
*
* You should implement event handling and custom methods in this
* class.
*/

Ext.define('Mvpc.view.NumericFacetContainer', {
    extend: 'Mvpc.view.ui.NumericFacetContainer',
    alias: 'widget.numericfacetcontainer',
    requires: ['Mvp.util.Coords', 'Mvp.util.Util', 'Ext.data.JsonStore', 'Ext.data.reader.Json', 'Mvp.util.Constants', 'Mvpc.view.MinMaxSliderContainer'],

    initComponent: function () {
        var me = this;
        me.callParent(arguments);
    },

    constructor: function (config) {
        Ext.apply(this, config);
        this.callParent(arguments);

        this.setTitle(this.title);
        this.app = window.app;
        var em = this.app.getEventManager();
        em.addListener('APP.context.records.filtered', this.configHistogram, this);
        this.store = Mvp.util.Util.potentialHistogramToStore(this.hist.hist);
        this.zoomStore = undefined;

        this.histogramContainer = this.getComponent('histogramContainer');
        this.guideline = Ext.create('Ext.container.Container', {
            height: 50,
            width: 1,
            title: false,
            hidden: true,
            shadow: false,
            closable: false,
            floating: true,
            resizable: false,
            style: {
                'background-color': 'orange',
                'border': 'transparent'
            }
        });

        this.minmaxSliderContainer = this.add(Ext.create('Mvpc.view.MinMaxSliderContainer', {
            minValue: this.minValue,
            maxValue: this.maxValue,
            displayFn: this.displayFn,
            readFn: this.readFn,
            ignoreValue: this.ignoreValue,
            discrete: this.discrete,
            isDate: this.isDate,
            increment: this.increment
        }));
        this.minmaxSliderContainer.addListener('rangeChanged', this.rangeChanged, this);
        this.minmaxSliderContainer.valueSlider.on({
            'dragend': this.hideLines,    // changecomplete doesn't fire if there isn't a change
            'drag': this.moveLines,
            'slideFailed': this.slideFailed,
            scope: this
        });
        this.zoomButton = Ext.create('Ext.button.Button', {
            disabled: true,
            itemId: 'zoomButton',
            text: 'Zoom to Range',
            flex: 0,
            margins: '0 0 0 39'
        });
        this.resetButton = Ext.create('Ext.button.Button', {
            itemId: 'resetButton',
            text: 'Reset/Unzoom',
            flex: 0,
            margins: '0 0 0 10',
            disabled: true
        });
        this.buttonContainer = this.add(Ext.create('Ext.container.Container', {
            layout: 'hbox',
            align: 'top',
            width: 255
        }));
        this.buttonContainer.add([this.zoomButton, this.resetButton]);
        this.zoomButton.addListener('click', this.zoom, this);
        this.resetButton.on('click', this.reset, this);

        if (this.ignoreValue !== undefined) {
            var label = {
                xtype: 'label',
                text: '(' + this.ignoreValue + ') is excluded from the histogram'
            };
            this.ignoreCheckbox = Ext.create('Ext.form.field.Checkbox', {
                labelWidth: 200,
                fieldLabel: 'Remove rows with ignored value',
                checked: false,
                handler: this.ignore,
                scope: this
            });
            var c = {
                xtype: 'container',
                items: [label, this.ignoreCheckbox],
                margin: '3 0 0 7'
            }
            this.add(c);
        };

        this.on({
            expand: this.configHistogram,
            scope: this
        });
        this.minmaxSliderContainer.setValues(this.minValue, this.maxValue);
    },

    reset: function () {
        this.doZoom = false;
        this.minmaxSliderContainer.reset();
        this.zoomButton.disable();
        this.buttonContainer.focus();
    },

    rangeChanged: function (values) {
        this.min = values[0];
        this.max = values[1];
        this.zoomButton.disable();
        if ((this.min != (this.doZoom ? this.sliderMin : this.minValue)) ||
            (this.max != (this.doZoom ? this.sliderMax : this.maxValue))) {
            this.resetButton.enable();
            this.zoomButton.enable();
        }
        if ((this.min == this.minValue) && (this.max == this.maxValue)) {
            this.resetButton.disable();
            this.zoomButton.disable();
        }
        this.fireEvent('rangeChanged', this.facetName, values, this.abideIgnore);
    },

    zoom: function (obj, e, config) {
        this.minmaxSliderContainer.valueSlider.setMaxValue(this.max);
        this.minmaxSliderContainer.valueSlider.setMinValue(this.min);
        this.sliderMax = this.max;
        this.sliderMin = this.min;
        this.doZoom = true;
        this.configHistogram(config);
    },

    getValues: function() {
        return [this.min, this.max];
    },

    getMin: function() {
        return this.min;
    },

    getMax: function() {
        return this.max
    },

    ignore: function (el, toVal) {
        if (this.ignoreChange) return;
        this.abideIgnore = toVal;
        var skipAdd = false;
        if ((this.min == this.minValue) && (this.max == this.maxValue) && !toVal) {
            this.fireEvent('filterRemoved', this.facetName);
            skipAdd = true;
        }
        this.fireEvent('rangeChanged', this.facetName, this.getValues(), this.abideIgnore, skipAdd);
    },

    slideFailed: function () {
        this.hideLines();
    },

    configHistogram: function () {
        if (!this.histogramContainer.isVisible(true)) return;
        Ext.log(this.facetName + ' configured');

        var i = this.passThrough.potentialStores ? this.passThrough.potentialStores.length : 0;
        var found = false;
        while (i--) {
            var s = this.passThrough.potentialStores[i];
            if (s.column == this.facetName) {
                this.zoomHist = Mvp.util.Util.potentialHistogram(s.store.items, this.facetName, this.doZoom ? this.sliderMin : this.minValue, this.doZoom ? this.sliderMax : this.maxValue, Mvp.util.Constants.NUMERIC_HISTOGRAM_BUCKETS, this.abideIgnore ? this.ignoreValue : undefined, this.min, this.max);
                found = true;
                break;
            }
        }
        if (!found) this.zoomHist = Mvp.util.Util.potentialHistogram(this.originalStore.backingStore.data.items, this.facetName, this.doZoom ? this.sliderMin : this.minValue, this.doZoom ? this.sliderMax : this.maxValue, Mvp.util.Constants.NUMERIC_HISTOGRAM_BUCKETS, this.abideIgnore ? this.ignoreValue : undefined, this.min, this.max);
        this.zoomStore = Mvp.util.Util.potentialHistogramToStore(this.zoomHist.histArray);

        var zoomArray = new Array(Mvp.util.Constants.NUMERIC_HISTOGRAM_BUCKETS), histArray = new Array(Mvp.util.Constants.NUMERIC_HISTOGRAM_BUCKETS),
            xmin = this.zoomHist.histArray[0].key, xmax = this.zoomHist.histArray[Mvp.util.Constants.NUMERIC_HISTOGRAM_BUCKETS-1].key,
            ymax = 0;

        for (var i = 0; i < 100; i++) {
            zoomArray[i] = this.zoomHist.histArray[i].excluded;
            histArray[i] = this.zoomHist.histArray[i].count;
            if ((zoomArray[i] + histArray[i]) > ymax) ymax = zoomArray[i] + histArray[i];
        }
        var plotData = [histArray, zoomArray];
        
        var options = {
            title: false,
            seriesDefaults:{
                renderer:$.jqplot.BarRenderer,
                rendererOptions: {
                    fillToZero: true,
                    barWidth: 2,
                    shadowOffset: 1,
                    shadowAlpha: 0,
                    barMargin: 0,
                    barPadding: 0
                }
            },
            seriesColors: ['#133987', '#c0c0c0'],
            axes: {
                xaxis: {
                    min: 0, max: 99,
                    showTicks: false,
                    show: false,
                    borderWidth: 0
                },
                yaxis: {
                    min: 0, max: ymax,
                    showTicks: false,
                    show: false,
                    borderWidth: 0
                }
            },
            grid: {
                drawGridlines: false,
                shadow: false,
                drawBorder: false
            }
        }

        if (this.plot) {
            options.data = plotData;
            this.plot.replot(options);
        } else {
            this.plot = $.jqplot(this.histogramContainer.getId(), plotData, options);
        }

        if (this.doZoom) {
            if ((this.sliderMin == this.min) && (this.sliderMax == this.max)) {
                this.zoomButton.disable();
            } else {
                this.zoomButton.enable();
            }
        }
    },

    moveLines: function () {
        if (this.ignoreChange) return;
        this.showLines();
        var thumbs = this.minmaxSliderContainer.valueSlider.thumbs;
        var which = thumbs[0].dragging ? 0 : 1;    // 0 = min thumb, 1 = max thumb
        this.guideline.el = Ext.get(this.guideline.id);
        this.guideline.alignTo(thumbs[which], 'b-t?', [(which ? 1 : 0), -18]);   // slightly offset the line position
    },

    hideLines: function () {
        this.guideline.hide();
    },

    showLines: function () {
        this.guideline.show();
    }
});