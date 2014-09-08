Ext.define("MyDesktop.VideoWindow", {
    extend: "Ext.ux.desktop.Module",
    uses: ["Ext.ux.desktop.Video"],
    id: "video",
    windowId: "video-window",
    tipWidth: 160,
    tipHeight: 96,
    init: function () {
        this.launcher = {
            text: "About Ext JS",
            iconCls: "video",
            handler: this.createWindow,
            scope: this
        }
    },
    createWindow: function () {
        var a = this,
            c = a.app.getDesktop(),
            b = c.getWindow(a.windowId);
        if (!b) {
            b = c.createWindow({
                id: a.windowId,
                title: "About Ext JS",
                width: 740,
                height: 480,
                iconCls: "video",
                animCollapse: false,
                border: false,
                layout: "fit",
                items: [{
                    xtype: "video",
                    id: "video-player",
                    src: [{
                        src: "http://dev.sencha.com/desktopvideo.mp4",
                        type: "video/mp4"
                    }, {
                        src: "http://dev.sencha.com/desktopvideo.ogv",
                        type: "video/ogg"
                    }, {
                        src: "http://dev.sencha.com/desktopvideo.mov",
                        type: "video/quicktime"
                    }],
                    autobuffer: true,
                    autoplay: true,
                    controls: true,
                    listeners: {
                        afterrender: function (d) {
                            a.videoEl = d.video.dom;
                            if (d.supported) {
                                a.tip = new Ext.tip.ToolTip({
                                    anchor: "bottom",
                                    dismissDelay: 0,
                                    height: a.tipHeight,
                                    width: a.tipWidth,
                                    renderTpl: ['<canvas width="', a.tipWidth, '" height="', a.tipHeight, '">'],
                                    renderSelectors: {
                                        body: "canvas"
                                    },
                                    listeners: {
                                        afterrender: a.onTooltipRender,
                                        show: a.renderPreview,
                                        scope: a
                                    }
                                })
                            }
                        }
                    }
                }],
                listeners: {
                    beforedestroy: function () {
                        a.tip = a.ctx = a.videoEl = null
                    }
                }
            })
        }
        b.show();
        if (a.tip) {
            a.tip.setTarget(b.taskButton.el)
        }
        return b
    },
    onTooltipRender: function (c) {
        var a = c.body.dom,
            b = this;
        b.ctx = a.getContext && a.getContext("2d")
    },
    renderPreview: function () {
        var a = this;
        if ((a.tip && !a.tip.isVisible()) || !a.videoEl) {
            return
        }
        if (a.ctx) {
            try {
                a.ctx.drawImage(a.videoEl, 0, 0, a.tipWidth, a.tipHeight)
            } catch (b) {}
        }
        Ext.Function.defer(a.renderPreview, 20, a)
    }
});
/*
* Ext JS Library 4.0
* Copyright(c) 2006-2011 Sencha Inc.
* licensing@sencha.com
* http://www.sencha.com/license
*/
var windowIndex = 0;
Ext.define("MyDesktop.BogusModule", {
    extend: "Ext.ux.desktop.Module",
    init: function () {
        this.launcher = {
            text: "Window " + (++windowIndex),
            iconCls: "bogus",
            handler: this.createWindow,
            scope: this,
            windowId: windowIndex
        }
    },
    createWindow: function (b) {
        var c = this.app.getDesktop();
        var a = c.getWindow("bogus" + b.windowId);
        if (!a) {
            a = c.createWindow({
                id: "bogus" + b.windowId,
                title: b.text,
                width: 640,
                height: 480,
                html: "<p>Something useful would be in here.</p>",
                iconCls: "bogus",
                animCollapse: false,
                constrainHeader: true
            })
        }
        a.show();
        return a
    }
});
/*
* Ext JS Library 4.0
* Copyright(c) 2006-2011 Sencha Inc.
* licensing@sencha.com
* http://www.sencha.com/license
*/
Ext.define("MyDesktop.BogusMenuModule", {
    extend: "MyDesktop.BogusModule",
    init: function () {
        this.launcher = {
            text: "More items",
            iconCls: "bogus",
            handler: function () {
                return false
            },
            menu: {
                items: []
            }
        };
        for (var a = 0; a < 5; ++a) {
            this.launcher.menu.items.push({
                text: "Window " + (++windowIndex),
                iconCls: "bogus",
                handler: this.createWindow,
                scope: this,
                windowId: windowIndex
            })
        }
    }
});
/*
 * Ext JS Library 4.0
 * Copyright(c) 2006-2011 Sencha Inc.
 * licensing@sencha.com
 * http://www.sencha.com/license
 */
Ext.define("MyDesktop.Settings", {
    extend: "Ext.window.Window",
    uses: ["Ext.tree.Panel", "Ext.tree.View", "Ext.form.field.Checkbox", "Ext.layout.container.Anchor", "Ext.layout.container.Border", "Ext.ux.desktop.Wallpaper", "MyDesktop.WallpaperModel"],
    layout: "anchor",
    title: "Change Settings",
    modal: true,
    width: 640,
    height: 480,
    border: false,
    initComponent: function () {
        var a = this;
        a.selected = a.desktop.getWallpaper();
        a.stretch = a.desktop.wallpaper.stretch;
        a.preview = Ext.create("widget.wallpaper");
        a.preview.setWallpaper(a.selected);
        a.tree = a.createTree();
        a.buttons = [{
            text: "OK",
            handler: a.onOK,
            scope: a
        }, {
            text: "Cancel",
            handler: a.close,
            scope: a
        }];
        a.items = [{
            anchor: "0 -30",
            border: false,
            layout: "border",
            items: [a.tree,
            {
                xtype: "panel",
                title: "Preview",
                region: "center",
                layout: "fit",
                items: [a.preview]
            }]
        }, {
            xtype: "checkbox",
            boxLabel: "Stretch to fit",
            checked: a.stretch,
            listeners: {
                change: function (b) {
                    a.stretch = b.checked
                }
            }
        }];
        a.callParent()
    },
    createTree: function () {
        var b = this;

        function c(d) {
            return {
                img: d,
                text: b.getTextOfWallpaper(d),
                iconCls: "",
                leaf: true
            }
        }
        var a = new Ext.tree.Panel({
            title: "Desktop Background",
            rootVisible: false,
            lines: false,
            autoScroll: true,
            width: 150,
            region: "west",
            split: true,
            minWidth: 100,
            listeners: {
                afterrender: {
                    fn: this.setInitialSelection,
                    delay: 100
                },
                select: this.onSelect,
                scope: this
            },
            store: new Ext.data.TreeStore({
                model: "MyDesktop.WallpaperModel",
                root: {
                    text: "Wallpaper",
                    expanded: true,
                    children: [{
                        text: "None",
                        iconCls: "",
                        leaf: true
                    },
                    c("Blue-Sencha.jpg"), c("Dark-Sencha.jpg"), c("Wood-Sencha.jpg"), c("blue.jpg"), c("desk.jpg"), c("desktop.jpg"), c("desktop2.jpg"), c("sky.jpg")]
                }
            })
        });
        return a
    },
    getTextOfWallpaper: function (c) {
        var d = c,
            b = c.lastIndexOf("/");
        if (b >= 0) {
            d = d.substring(b + 1)
        }
        var a = d.lastIndexOf(".");
        d = Ext.String.capitalize(d.substring(0, a));
        d = d.replace(/[-]/g, " ");
        return d
    },
    onOK: function () {
        var a = this;
        if (a.selected) {
            a.desktop.setWallpaper(a.selected, a.stretch)
        }
        a.destroy()
    },
    onSelect: function (a, b) {
        var c = this;
        if (b.data.img) {
            c.selected = "wallpapers/" + b.data.img
        } else {
            c.selected = Ext.BLANK_IMAGE_URL
        }
        c.preview.setWallpaper(c.selected)
    },
    setInitialSelection: function () {
        var a = this.desktop.getWallpaper();
        if (a) {
            var b = "/Wallpaper/" + this.getTextOfWallpaper(a);
            this.tree.selectPath(b, "text")
        }
    }
});
/*
 * Ext JS Library 4.0
 * Copyright(c) 2006-2011 Sencha Inc.
 * licensing@sencha.com
 * http://www.sencha.com/license
 */
Ext.define("MyDesktop.TabWindow", {
    extend: "Ext.ux.desktop.Module",
    requires: ["Ext.tab.Panel"],
    id: "tab-win",
    init: function () {
        this.launcher = {
            text: "Tab Window",
            iconCls: "tabs",
            handler: this.createWindow,
            scope: this
        }
    },
    createWindow: function () {
        var b = this.app.getDesktop();
        var a = b.getWindow("tab-win");
        if (!a) {
            a = b.createWindow({
                id: "tab-win",
                title: "Tab Window",
                width: 740,
                height: 480,
                iconCls: "tabs",
                animCollapse: false,
                border: false,
                constrainHeader: true,
                layout: "fit",
                items: [{
                    xtype: "tabpanel",
                    activeTab: 0,
                    bodyStyle: "padding: 5px;",
                    items: [{
                        title: "Tab Text 1",
                        header: false,
                        html: "<p>Something useful would be in here.</p>",
                        border: false
                    }, {
                        title: "Tab Text 2",
                        header: false,
                        html: "<p>Something useful would be in here.</p>",
                        border: false
                    }, {
                        title: "Tab Text 3",
                        header: false,
                        html: "<p>Something useful would be in here.</p>",
                        border: false
                    }, {
                        title: "Tab Text 4",
                        header: false,
                        html: "<p>Something useful would be in here.</p>",
                        border: false
                    }]
                }]
            })
        }
        a.show();
        return a
    }
});
/*
 * Ext JS Library 4.0
 * Copyright(c) 2006-2011 Sencha Inc.
 * licensing@sencha.com
 * http://www.sencha.com/license
 */
globalPortal = null;
function globalEnsurePortal() {
    if (!globalPortal) {
        globalPortal = Ext.create('DemoApp.Portal');
    }
    return globalPortal;
};

 Ext.define("MyDesktop.AstroViewWindow", {
     extend: "Ext.ux.desktop.Module",
     id: 'astroview-win',
     init: function () {
        this.launcher = {
            text: "AstroView",
            iconCls: "icon-grid",
            handler: this.createWindow,
            scope: this
        }
    },
    createWindow: function () {
        var b = this.app.getDesktop();
        var a = b.getWindow("astroview-win");
        if (!a) {
         
            var portal = globalEnsurePortal();
            var astroViewPanel = portal.ensureAstroViewPanel();
            Ext.log('portal = ' + portal);
            Ext.log('avp = ' + astroViewPanel);

           a = b.createWindow({
                id: "astroview-win",
                title: "AstroView",
                width: 600,
                height: 400,
                iconCls: "icon-grid",
                animCollapse: false,
                constrainHeader: true,
                layout: "fit",
                items: [astroViewPanel]
            });
           useAV = true;
        }
        a.show();
        return a
    }
 });
 
 
/*
 * Ext JS Library 4.0
 * Copyright(c) 2006-2011 Sencha Inc.
 * licensing@sencha.com
 * http://www.sencha.com/license
 */
 Ext.define("MyDesktop.SearchWindow", {
     extend: "Ext.ux.desktop.Module",
     id: 'search-win',
     init: function () {
        this.launcher = {
            text: "Search Window",
            iconCls: "icon-grid",
            handler: this.createWindow,
            scope: this
        }
    },
    createWindow: function () {
        var b = this.app.getDesktop();
        globalDesk = b;
        var a = b.getWindow("search-win");
        if (!a) {
         
            var portal = globalEnsurePortal();
            var searchPanel = portal.createSearchPanel();

           a = b.createWindow({
                id: "search-win",
                title: "Search Window",
                width: 1000,
                height: 200,
                iconCls: "icon-grid",
                animCollapse: false,
                constrainHeader: true,
                layout: "fit",
                items: [searchPanel]
            });
        }
        a.show();
        return a
    }
 });
 
 
Ext.define("MyDesktop.GridWindow", {
    extend: "Ext.ux.desktop.Module",
    requires: ["Ext.data.ArrayStore", "Ext.util.Format", "Ext.grid.Panel", "Ext.grid.RowNumberer"],
    id: "grid-win",
    init: function () {
        this.launcher = {
            text: "Grid Window",
            iconCls: "icon-grid",
            handler: this.createWindow,
            scope: this
        }
    },
    createWindow: function () {
        var b = this.app.getDesktop();
        var a = b.getWindow("grid-win");
        if (!a) {
            a = b.createWindow({
                id: "grid-win",
                title: "Grid Window",
                width: 740,
                height: 480,
                iconCls: "icon-grid",
                animCollapse: false,
                constrainHeader: true,
                layout: "fit",
                items: [{
                    border: false,
                    xtype: "grid",
                    store: new Ext.data.ArrayStore({
                        fields: [{
                            name: "company"
                        }, {
                            name: "price",
                            type: "float"
                        }, {
                            name: "change",
                            type: "float"
                        }, {
                            name: "pctChange",
                            type: "float"
                        }],
                        data: MyDesktop.GridWindow.getDummyData()
                    }),
                    columns: [new Ext.grid.RowNumberer(),
                    {
                        text: "Company",
                        flex: 1,
                        sortable: true,
                        dataIndex: "company"
                    }, {
                        text: "Price",
                        width: 70,
                        sortable: true,
                        renderer: Ext.util.Format.usMoney,
                        dataIndex: "price"
                    }, {
                        text: "Change",
                        width: 70,
                        sortable: true,
                        dataIndex: "change"
                    }, {
                        text: "% Change",
                        width: 70,
                        sortable: true,
                        dataIndex: "pctChange"
                    }]
                }],
                tbar: [{
                    text: "Add Something",
                    tooltip: "Add a new row",
                    iconCls: "add"
                }, "-",
                {
                    text: "Options",
                    tooltip: "Blah blah blah blaht",
                    iconCls: "option"
                }, "-",
                {
                    text: "Remove Something",
                    tooltip: "Remove the selected item",
                    iconCls: "remove"
                }]
            })
        }
        a.show();
        return a
    },
    statics: {
        getDummyData: function () {
            return [["3m Co", 71.72, 0.02, 0.03, "9/1 12:00am"], ["Alcoa Inc", 29.01, 0.42, 1.47, "9/1 12:00am"], ["American Express Company", 52.55, 0.01, 0.02, "9/1 12:00am"], ["American International Group, Inc.", 64.13, 0.31, 0.49, "9/1 12:00am"], ["AT&T Inc.", 31.61, -0.48, -1.54, "9/1 12:00am"], ["Caterpillar Inc.", 67.27, 0.92, 1.39, "9/1 12:00am"], ["Citigroup, Inc.", 49.37, 0.02, 0.04, "9/1 12:00am"], ["Exxon Mobil Corp", 68.1, -0.43, -0.64, "9/1 12:00am"], ["General Electric Company", 34.14, -0.08, -0.23, "9/1 12:00am"], ["General Motors Corporation", 30.27, 1.09, 3.74, "9/1 12:00am"], ["Hewlett-Packard Co.", 36.53, -0.03, -0.08, "9/1 12:00am"], ["Honeywell Intl Inc", 38.77, 0.05, 0.13, "9/1 12:00am"], ["Intel Corporation", 19.88, 0.31, 1.58, "9/1 12:00am"], ["Johnson & Johnson", 64.72, 0.06, 0.09, "9/1 12:00am"], ["Merck & Co., Inc.", 40.96, 0.41, 1.01, "9/1 12:00am"], ["Microsoft Corporation", 25.84, 0.14, 0.54, "9/1 12:00am"], ["The Coca-Cola Company", 45.07, 0.26, 0.58, "9/1 12:00am"], ["The Procter & Gamble Company", 61.91, 0.01, 0.02, "9/1 12:00am"], ["Wal-Mart Stores, Inc.", 45.45, 0.73, 1.63, "9/1 12:00am"], ["Walt Disney Company (The) (Holding Company)", 29.89, 0.24, 0.81, "9/1 12:00am"]]
        }
    }
});
/*
 * Ext JS Library 4.0
 * Copyright(c) 2006-2011 Sencha Inc.
 * licensing@sencha.com
 * http://www.sencha.com/license
 */
Ext.define("MyDesktop.AccordionWindow", {
    extend: "Ext.ux.desktop.Module",
    requires: ["Ext.data.TreeStore", "Ext.layout.container.Accordion", "Ext.toolbar.Spacer", "Ext.tree.Panel"],
    id: "acc-win",
    init: function () {
        this.launcher = {
            text: "Accordion Window",
            iconCls: "accordion",
            handler: this.createWindow,
            scope: this
        }
    },
    createTree: function () {
        var a = Ext.create("Ext.tree.Panel", {
            id: "im-tree",
            title: "Online Users",
            rootVisible: false,
            lines: false,
            autoScroll: true,
            tools: [{
                type: "refresh",
                handler: function (e, d) {
                    a.setLoading(true, a.body);
                    var b = a.getRootNode();
                    b.collapseChildren(true, false);
                    Ext.Function.defer(function () {
                        a.setLoading(false);
                        b.expand(true, true)
                    }, 1000)
                }
            }],
            store: Ext.create("Ext.data.TreeStore", {
                root: {
                    text: "Online",
                    expanded: true,
                    children: [{
                        text: "Friends",
                        expanded: true,
                        children: [{
                            text: "Brian",
                            iconCls: "user",
                            leaf: true
                        }, {
                            text: "Kevin",
                            iconCls: "user",
                            leaf: true
                        }, {
                            text: "Mark",
                            iconCls: "user",
                            leaf: true
                        }, {
                            text: "Matt",
                            iconCls: "user",
                            leaf: true
                        }, {
                            text: "Michael",
                            iconCls: "user",
                            leaf: true
                        }, {
                            text: "Mike Jr",
                            iconCls: "user",
                            leaf: true
                        }, {
                            text: "Mike Sr",
                            iconCls: "user",
                            leaf: true
                        }, {
                            text: "JR",
                            iconCls: "user",
                            leaf: true
                        }, {
                            text: "Rich",
                            iconCls: "user",
                            leaf: true
                        }, {
                            text: "Nige",
                            iconCls: "user",
                            leaf: true
                        }, {
                            text: "Zac",
                            iconCls: "user",
                            leaf: true
                        }]
                    }, {
                        text: "Family",
                        expanded: true,
                        children: [{
                            text: "Kiana",
                            iconCls: "user-girl",
                            leaf: true
                        }, {
                            text: "Aubrey",
                            iconCls: "user-girl",
                            leaf: true
                        }, {
                            text: "Cale",
                            iconCls: "user-kid",
                            leaf: true
                        }]
                    }]
                }
            })
        });
        return a
    },
    createWindow: function () {
        var b = this.app.getDesktop();
        var a = b.getWindow("acc-win");
        if (!a) {
            a = b.createWindow({
                id: "acc-win",
                title: "Accordion Window",
                width: 250,
                height: 400,
                iconCls: "accordion",
                animCollapse: false,
                constrainHeader: true,
                bodyBorder: true,
                tbar: {
                    xtype: "toolbar",
                    ui: "plain",
                    items: [{
                        tooltip: {
                            title: "Rich Tooltips",
                            text: "Let your users know what they can do!"
                        },
                        iconCls: "connect"
                    }, "-",
                    {
                        tooltip: "Add a new user",
                        iconCls: "user-add"
                    }, " ",
                    {
                        tooltip: "Remove the selected user",
                        iconCls: "user-delete"
                    }]
                },
                layout: "accordion",
                border: false,
                items: [this.createTree(),
                {
                    title: "Settings",
                    html: "<p>Something useful would be in here.</p>",
                    autoScroll: true
                }, {
                    title: "Even More Stuff",
                    html: "<p>Something useful would be in here.</p>"
                }, {
                    title: "My Stuff",
                    html: "<p>Something useful would be in here.</p>"
                }]
            })
        }
        a.show();
        return a
    }
});
/*
* Ext JS Library 4.0
* Copyright(c) 2006-2011 Sencha Inc.
* licensing@sencha.com
* http://www.sencha.com/license
*/
Ext.define("MyDesktop.SystemStatus", {
    extend: "Ext.ux.desktop.Module",
    requires: ["Ext.chart.*"],
    id: "systemstatus",
    init: function () {
        Ext.chart.theme.Memory = Ext.extend(Ext.chart.theme.Base, {
            constructor: function (a) {
                Ext.chart.theme.Memory.superclass.constructor.call(this, Ext.apply({
                    colors: ["rgb(244, 16, 0)", "rgb(248, 130, 1)", "rgb(0, 7, 255)", "rgb(84, 254, 0)"]
                }, a))
            }
        })
    },
    createNewWindow: function () {
        var a = this,
            b = a.app.getDesktop();
        a.cpuLoadData = [];
        a.cpuLoadStore = Ext.create("store.json", {
            fields: ["core1", "core2"]
        });
        a.memoryArray = ["Wired", "Active", "Inactive", "Free"];
        a.memoryStore = Ext.create("store.json", {
            fields: ["name", "memory"],
            data: a.generateData(a.memoryArray)
        });
        a.pass = 0;
        a.processArray = ["explorer", "monitor", "charts", "desktop", "Ext3", "Ext4"];
        a.processesMemoryStore = Ext.create("store.json", {
            fields: ["name", "memory"],
            data: a.generateData(a.processArray)
        });
        a.generateCpuLoad();
        return b.createWindow({
            id: "systemstatus",
            title: "System Status",
            width: 800,
            height: 600,
            animCollapse: false,
            constrainHeader: true,
            border: false,
            layout: "fit",
            listeners: {
                afterrender: {
                    fn: a.updateCharts,
                    delay: 100
                },
                destroy: function () {
                    clearTimeout(a.updateTimer);
                    a.updateTimer = null
                },
                scope: a
            },
            items: [{
                xtype: "panel",
                layout: {
                    type: "hbox",
                    align: "stretch"
                },
                items: [{
                    flex: 1,
                    height: 600,
                    width: 400,
                    xtype: "container",
                    layout: {
                        type: "vbox",
                        align: "stretch"
                    },
                    items: [a.createCpu1LoadChart(), a.createCpu2LoadChart()]
                }, {
                    flex: 1,
                    width: 400,
                    height: 600,
                    xtype: "container",
                    layout: {
                        type: "vbox",
                        align: "stretch"
                    },
                    items: [a.createMemoryPieChart(), a.createProcessChart()]
                }]
            }]
        })
    },
    createWindow: function () {
        var a = this.app.getDesktop().getWindow(this.id);
        if (!a) {
            a = this.createNewWindow()
        }
        a.show();
        return a
    },
    createCpu1LoadChart: function () {
        return {
            flex: 1,
            xtype: "chart",
            theme: "Category1",
            animate: false,
            store: this.cpuLoadStore,
            legend: {
                position: "bottom"
            },
            axes: [{
                type: "Numeric",
                position: "left",
                minimum: 0,
                maximum: 100,
                fields: ["core1"],
                title: "CPU Load",
                grid: true,
                labelTitle: {
                    font: "13px Arial"
                },
                label: {
                    font: "11px Arial"
                }
            }],
            series: [{
                title: "Core 1 (3.4GHz)",
                type: "line",
                lineWidth: 4,
                showMarkers: false,
                fill: true,
                axis: "right",
                xField: "time",
                yField: "core1",
                style: {
                    "stroke-width": 1
                }
            }]
        }
    },
    createCpu2LoadChart: function () {
        return {
            flex: 1,
            xtype: "chart",
            theme: "Category2",
            animate: false,
            store: this.cpuLoadStore,
            legend: {
                position: "bottom"
            },
            axes: [{
                type: "Numeric",
                position: "left",
                minimum: 0,
                maximum: 100,
                grid: true,
                fields: ["core2"],
                title: "CPU Load",
                labelTitle: {
                    font: "13px Arial"
                },
                label: {
                    font: "11px Arial"
                }
            }],
            series: [{
                title: "Core 2 (3.4GHz)",
                type: "line",
                lineWidth: 4,
                showMarkers: false,
                fill: true,
                axis: "right",
                xField: "time",
                yField: "core2",
                style: {
                    "stroke-width": 1
                }
            }]
        }
    },
    createMemoryPieChart: function () {
        var a = this;
        return {
            flex: 1,
            xtype: "chart",
            animate: {
                duration: 250
            },
            store: this.memoryStore,
            shadow: true,
            legend: {
                position: "right"
            },
            insetPadding: 40,
            theme: "Memory:gradients",
            series: [{
                donut: 30,
                type: "pie",
                field: "memory",
                showInLegend: true,
                tips: {
                    trackMouse: true,
                    width: 140,
                    height: 28,
                    renderer: function (d, c) {
                        var b = 0;
                        a.memoryStore.each(function (e) {
                            b += e.get("memory")
                        });
                        this.setTitle(d.get("name") + ": " + Math.round(d.get("memory") / b * 100) + "%")
                    }
                },
                highlight: {
                    segment: {
                        margin: 20
                    }
                },
                labelTitle: {
                    font: "13px Arial"
                },
                label: {
                    field: "name",
                    display: "rotate",
                    contrast: true,
                    font: "12px Arial"
                }
            }]
        }
    },
    createProcessChart: function () {
        return {
            flex: 1,
            xtype: "chart",
            theme: "Category1",
            store: this.processesMemoryStore,
            animate: {
                easing: "ease-in-out",
                duration: 750
            },
            axes: [{
                type: "Numeric",
                position: "left",
                minimum: 0,
                maximum: 10,
                fields: ["memory"],
                title: "Memory",
                labelTitle: {
                    font: "13px Arial"
                },
                label: {
                    font: "11px Arial"
                }
            }, {
                type: "Category",
                position: "bottom",
                fields: ["name"],
                title: "System Processes",
                labelTitle: {
                    font: "bold 14px Arial"
                },
                label: {
                    rotation: {
                        degrees: 45
                    }
                }
            }, {
                type: "Numeric",
                position: "top",
                fields: ["memory"],
                title: "Memory Usage",
                labelTitle: {
                    font: "bold 14px Arial"
                },
                label: {
                    fill: "#FFFFFF",
                    stroke: "#FFFFFF"
                },
                axisStyle: {
                    fill: "#FFFFFF",
                    stroke: "#FFFFFF"
                }
            }],
            series: [{
                title: "Processes",
                type: "column",
                xField: "name",
                yField: "memory",
                renderer: function (g, c, b, f, e) {
                    var a = Ext.draw.Color.fromString("#b1da5a"),
                        h = c.get("memory"),
                        d;
                    if (h > 5) {
                        d = a.getDarker((h - 5) / 15).toString()
                    } else {
                        d = a.getLighter(((5 - h) / 20)).toString()
                    }
                    if (h >= 8) {
                        d = "#CD0000"
                    }
                    return Ext.apply(b, {
                        fill: d
                    })
                }
            }]
        }
    },
    generateCpuLoad: function () {
        var c = this,
            e = c.cpuLoadData;

        function a(f) {
            var g = f + ((Math.floor(Math.random() * 2) % 2) ? -1 : 1) * Math.floor(Math.random() * 9);
            if (g < 0 || g > 100) {
                g = 50
            }
            return g
        }
        if (e.length === 0) {
            e.push({
                core1: 0,
                core2: 0,
                time: 0
            });
            for (var b = 1; b < 100; b++) {
                e.push({
                    core1: a(e[b - 1].core1),
                    core2: a(e[b - 1].core2),
                    time: b
                })
            }
            c.cpuLoadStore.loadData(e)
        } else {
            c.cpuLoadStore.data.removeAt(0);
            c.cpuLoadStore.data.each(function (g, f) {
                g.data.time = f
            });
            var d = c.cpuLoadStore.last().data;
            c.cpuLoadStore.loadData([{
                core1: a(d.core1),
                core2: a(d.core2),
                time: d.time + 1
            }], true)
        }
    },
    generateData: function (e) {
        var d = [],
            b, c = e.length,
            a;
        for (b = 0; b < e.length; b++) {
            a = Math.floor(Math.random() * c * 100) / 100 + 2;
            c = c - (a - 5);
            d.push({
                name: e[b],
                memory: a
            })
        }
        return d
    },
    updateCharts: function () {
        var a = this;
        clearTimeout(a.updateTimer);
        a.updateTimer = setTimeout(function () {
            if (a.pass % 3 === 0) {
                a.memoryStore.loadData(a.generateData(a.memoryArray))
            }
            if (a.pass % 5 === 0) {
                a.processesMemoryStore.loadData(a.generateData(a.processArray))
            }
            a.generateCpuLoad();
            a.updateCharts();
            a.pass++
        }, 500)
    }
});
/*
 * Ext JS Library 4.0
 * Copyright(c) 2006-2011 Sencha Inc.
 * licensing@sencha.com
 * http://www.sencha.com/license
 */
Ext.define("MyDesktop.Notepad", {
    extend: "Ext.ux.desktop.Module",
    requires: ["Ext.form.field.HtmlEditor"],
    id: "notepad",
    init: function () {
        this.launcher = {
            text: "Notepad",
            iconCls: "notepad",
            handler: this.createWindow,
            scope: this
        }
    },
    createWindow: function () {
        var b = this.app.getDesktop();
        var a = b.getWindow("notepad");
        if (!a) {
            a = b.createWindow({
                id: "notepad",
                title: "Notepad",
                width: 600,
                height: 400,
                iconCls: "notepad",
                animCollapse: false,
                border: false,
                hideMode: "offsets",
                layout: "fit",
                items: [{
                    xtype: "htmleditor",
                    id: "notepad-editor",
                    value: ['Some <b>rich</b> <font color="red">text</font> goes <u>here</u><br>', "Give it a try!"].join("")
                }]
            })
        }
        a.show();
        return a
    }
});
/*
 * Ext JS Library 4.0
 * Copyright(c) 2006-2011 Sencha Inc.
 * licensing@sencha.com
 * http://www.sencha.com/license
 */
Ext.define("MyDesktop.App", {
    extend: "Ext.ux.desktop.App",
    requires: ["Ext.window.MessageBox", "Ext.ux.desktop.ShortcutModel", "MyDesktop.SystemStatus", "MyDesktop.VideoWindow", "MyDesktop.GridWindow", "MyDesktop.SearchWindow",  "MyDesktop.AstroViewWindow", "MyDesktop.TabWindow", "MyDesktop.AccordionWindow", "MyDesktop.Notepad", "MyDesktop.BogusMenuModule", "MyDesktop.BogusModule", "MyDesktop.Settings"],
    init: function () {
        this.callParent()
    },
    getModules: function () {
        return [new MyDesktop.VideoWindow(), new MyDesktop.SystemStatus(), new MyDesktop.GridWindow(), new MyDesktop.SearchWindow(), new MyDesktop.AstroViewWindow(), new MyDesktop.TabWindow(), new MyDesktop.AccordionWindow(), new MyDesktop.Notepad(), new MyDesktop.BogusMenuModule(), new MyDesktop.BogusModule()]
    },
    getDesktopConfig: function () {
        var b = this,
            a = b.callParent();
        return Ext.apply(a, {
            contextMenuItems: [{
                text: "Change Settings",
                handler: b.onSettings,
                scope: b
            }],
            shortcuts: Ext.create("Ext.data.Store", {
                model: "Ext.ux.desktop.ShortcutModel",
                data: [{
                //    name: "Grid Window",
                //    iconCls: "grid-shortcut",
                //    module: "grid-win"
                //}, {
                    name: "Search Window",
                    iconCls: "grid-shortcut",
                    module: "search-win"
                }, {
                    name: "AstroView",
                    iconCls: "grid-shortcut",
                    module: "astroview-win"
                }, {
                    name: "Accordion Window",
                    iconCls: "accordion-shortcut",
                    module: "acc-win"
                }, {
                    name: "Notepad",
                    iconCls: "notepad-shortcut",
                    module: "notepad"
                }, {
                    name: "System Status",
                    iconCls: "cpu-shortcut",
                    module: "systemstatus"
                }]
            }),
            wallpaper: "wallpapers/Blue-Sencha.jpg",
            wallpaperStretch: false
        })
    },
    getStartConfig: function () {
        var b = this,
            a = b.callParent();
        return Ext.apply(a, {
            title: "Don Griffin",
            iconCls: "user",
            height: 300,
            toolConfig: {
                width: 100,
                items: [{
                    text: "Settings",
                    iconCls: "settings",
                    handler: b.onSettings,
                    scope: b
                }, "-",
                {
                    text: "Logout",
                    iconCls: "logout",
                    handler: b.onLogout,
                    scope: b
                }]
            }
        })
    },
    getTaskbarConfig: function () {
        var a = this.callParent();
        return Ext.apply(a, {
            quickStart: [{
                name: "Accordion Window",
                iconCls: "accordion",
                module: "acc-win"
            }, {
                name: "Grid Window",
                iconCls: "icon-grid",
                module: "grid-win"
            }],
            trayItems: [{
                xtype: "trayclock",
                flex: 1
            }]
        })
    },
    onLogout: function () {
        Ext.Msg.confirm("Logout", "Are you sure you want to logout?")
    },
    onSettings: function () {
        var a = new MyDesktop.Settings({
            desktop: this.desktop
        });
        a.show()
    }
});
/*
 * Ext JS Library 4.0
 * Copyright(c) 2006-2011 Sencha Inc.
 * licensing@sencha.com
 * http://www.sencha.com/license
 */
Ext.define("MyDesktop.WallpaperModel", {
    extend: "Ext.data.Model",
    fields: [{
        name: "text"
    }, {
        name: "img"
    }]
});
