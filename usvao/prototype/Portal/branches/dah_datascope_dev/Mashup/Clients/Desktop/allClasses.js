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
 Ext.define("MyDesktop.SearchWindow", {
     extend: "Ext.ux.desktop.Module",
     id: 'search-win',
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
        var a = b.getWindow("search-win");
        if (!a) {
         
            var portal = Ext.create('DemoApp.Portal');
            var searchPanel = portal.createSearchPanel();
            
            b.add(searchPanel);
         
//	var win = Ext.create('Ext.window.Window', {
//            width: 450,
//            minWidth: 435,
//            height: 600,
//            minHeight: 450,
//            title: 'test',
//            layout: 'fit',
//            constrainHeader: true
//        });
//        win.add(searchPanel);
//        win.show();
    
           a = b.createWindow({
                id: "search-win",
                title: "Search Window",
                width: 740,
                height: 380,
                iconCls: "icon-grid",
                animCollapse: false,
                constrainHeader: true,
                layout: "fit",
                items: []
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
    requires: ["Ext.window.MessageBox", "Ext.ux.desktop.ShortcutModel", "MyDesktop.SystemStatus", "MyDesktop.VideoWindow", "MyDesktop.GridWindow", "MyDesktop.SearchWindow", "MyDesktop.TabWindow", "MyDesktop.AccordionWindow", "MyDesktop.Notepad", "MyDesktop.BogusMenuModule", "MyDesktop.BogusModule", "MyDesktop.Settings"],
    init: function () {
        this.callParent()
    },
    getModules: function () {
        return [new MyDesktop.VideoWindow(), new MyDesktop.SystemStatus(), new MyDesktop.GridWindow(), new MyDesktop.SearchWindow(), new MyDesktop.TabWindow(), new MyDesktop.AccordionWindow(), new MyDesktop.Notepad(), new MyDesktop.BogusMenuModule(), new MyDesktop.BogusModule()]
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
                    name: "Grid Window",
                    iconCls: "grid-shortcut",
                    module: "grid-win"
                }, {
                    name: "Search Window",
                    iconCls: "grid-shortcut",
                    module: "search-win"
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
/*

This file is part of Ext JS 4

Copyright (c) 2011 Sencha Inc

Contact:  http://www.sencha.com/contact

GNU General Public License Usage
This file may be used under the terms of the GNU General Public License version 3.0 as published by the Free Software Foundation and appearing in the file LICENSE included in the packaging of this file.  Please review the following information to ensure the GNU General Public License version 3.0 requirements will be met: http://www.gnu.org/copyleft/gpl.html.

If you are unsure which license is appropriate for your use, please contact the sales department at http://www.sencha.com/contact.

*/
/*!
 * Ext JS Library 4.0
 * Copyright(c) 2006-2011 Sencha Inc.
 * licensing@sencha.com
 * http://www.sencha.com/license
 */

Ext.define('Ext.ux.desktop.App', {
    mixins: {
        observable: 'Ext.util.Observable'
    },

    requires: [
        'Ext.container.Viewport',

        'Ext.ux.desktop.Desktop'
    ],

    isReady: false,
    modules: null,
    useQuickTips: true,

    constructor: function (config) {
        var me = this;
        me.addEvents(
            'ready',
            'beforeunload'
        );

        me.mixins.observable.constructor.call(this, config);

        if (Ext.isReady) {
            Ext.Function.defer(me.init, 10, me);
        } else {
            Ext.onReady(me.init, me);
        }
    },

    init: function() {
        var me = this, desktopCfg;

        if (me.useQuickTips) {
            Ext.QuickTips.init();
        }

        me.modules = me.getModules();
        if (me.modules) {
            me.initModules(me.modules);
        }

        desktopCfg = me.getDesktopConfig();
        me.desktop = new Ext.ux.desktop.Desktop(desktopCfg);

        me.viewport = new Ext.container.Viewport({
            layout: 'fit',
            items: [ me.desktop ]
        });

        Ext.EventManager.on(window, 'beforeunload', me.onUnload, me);

        me.isReady = true;
        me.fireEvent('ready', me);
    },

    /**
     * This method returns the configuration object for the Desktop object. A derived
     * class can override this method, call the base version to build the config and
     * then modify the returned object before returning it.
     */
    getDesktopConfig: function () {
        var me = this, cfg = {
            app: me,
            taskbarConfig: me.getTaskbarConfig()
        };

        Ext.apply(cfg, me.desktopConfig);
        return cfg;
    },

    getModules: Ext.emptyFn,

    /**
     * This method returns the configuration object for the Start Button. A derived
     * class can override this method, call the base version to build the config and
     * then modify the returned object before returning it.
     */
    getStartConfig: function () {
        var me = this, cfg = {
            app: me,
            menu: []
        };

        Ext.apply(cfg, me.startConfig);

        Ext.each(me.modules, function (module) {
            if (module.launcher) {
                cfg.menu.push(module.launcher);
            }
        });

        return cfg;
    },

    /**
     * This method returns the configuration object for the TaskBar. A derived class
     * can override this method, call the base version to build the config and then
     * modify the returned object before returning it.
     */
    getTaskbarConfig: function () {
        var me = this, cfg = {
            app: me,
            startConfig: me.getStartConfig()
        };

        Ext.apply(cfg, me.taskbarConfig);
        return cfg;
    },

    initModules : function(modules) {
        var me = this;
        Ext.each(modules, function (module) {
            module.app = me;
        });
    },

    getModule : function(name) {
    	var ms = this.modules;
        for (var i = 0, len = ms.length; i < len; i++) {
            var m = ms[i];
            if (m.id == name || m.appType == name) {
                return m;
            }
        }
        return null;
    },

    onReady : function(fn, scope) {
        if (this.isReady) {
            fn.call(scope, this);
        } else {
            this.on({
                ready: fn,
                scope: scope,
                single: true
            });
        }
    },

    getDesktop : function() {
        return this.desktop;
    },

    onUnload : function(e) {
        if (this.fireEvent('beforeunload', this) === false) {
            e.stopEvent();
        }
    }
});

/*

This file is part of Ext JS 4

Copyright (c) 2011 Sencha Inc

Contact:  http://www.sencha.com/contact

GNU General Public License Usage
This file may be used under the terms of the GNU General Public License version 3.0 as published by the Free Software Foundation and appearing in the file LICENSE included in the packaging of this file.  Please review the following information to ensure the GNU General Public License version 3.0 requirements will be met: http://www.gnu.org/copyleft/gpl.html.

If you are unsure which license is appropriate for your use, please contact the sales department at http://www.sencha.com/contact.

*/
/*!
 * Ext JS Library 4.0
 * Copyright(c) 2006-2011 Sencha Inc.
 * licensing@sencha.com
 * http://www.sencha.com/license
 */

/**
 * @class Ext.ux.desktop.Desktop
 * @extends Ext.panel.Panel
 * <p>This class manages the wallpaper, shortcuts and taskbar.</p>
 */
Ext.define('Ext.ux.desktop.Desktop', {
    extend: 'Ext.panel.Panel',

    alias: 'widget.desktop',

    uses: [
        'Ext.util.MixedCollection',
        'Ext.menu.Menu',
        'Ext.view.View', // dataview
        'Ext.window.Window',

        'Ext.ux.desktop.TaskBar',
        'Ext.ux.desktop.Wallpaper',
        'Ext.ux.desktop.FitAllLayout'
    ],

    activeWindowCls: 'ux-desktop-active-win',
    inactiveWindowCls: 'ux-desktop-inactive-win',
    lastActiveWindow: null,

    border: false,
    html: '&#160;',
    layout: 'fitall',

    xTickSize: 1,
    yTickSize: 1,

    app: null,

    /**
     * @cfg {Array|Store} shortcuts
     * The items to add to the DataView. This can be a {@link Ext.data.Store Store} or a
     * simple array. Items should minimally provide the fields in the
     * {@link Ext.ux.desktop.ShorcutModel ShortcutModel}.
     */
    shortcuts: null,

    /**
     * @cfg {String} shortcutItemSelector
     * This property is passed to the DataView for the desktop to select shortcut items.
     * If the {@link #shortcutTpl} is modified, this will probably need to be modified as
     * well.
     */
    shortcutItemSelector: 'div.ux-desktop-shortcut',

    /**
     * @cfg {String} shortcutTpl
     * This XTemplate is used to render items in the DataView. If this is changed, the
     * {@link shortcutItemSelect} will probably also need to changed.
     */
    shortcutTpl: [
        '<tpl for=".">',
            '<div class="ux-desktop-shortcut" id="{name}-shortcut">',
                '<div class="ux-desktop-shortcut-icon {iconCls}">',
                    '<img src="',Ext.BLANK_IMAGE_URL,'" title="{name}">',
                '</div>',
                '<span class="ux-desktop-shortcut-text">{name}</span>',
            '</div>',
        '</tpl>',
        '<div class="x-clear"></div>'
    ],

    /**
     * @cfg {Object} taskbarConfig
     * The config object for the TaskBar.
     */
    taskbarConfig: null,

    windowMenu: null,

    initComponent: function () {
        var me = this;

        me.windowMenu = new Ext.menu.Menu(me.createWindowMenu());

        me.bbar = me.taskbar = new Ext.ux.desktop.TaskBar(me.taskbarConfig);
        me.taskbar.windowMenu = me.windowMenu;

        me.windows = new Ext.util.MixedCollection();

        me.contextMenu = new Ext.menu.Menu(me.createDesktopMenu());

        me.items = [
            { xtype: 'wallpaper', id: me.id+'_wallpaper' },
            me.createDataView()
        ];

        me.callParent();

        me.shortcutsView = me.items.getAt(1);
        me.shortcutsView.on('itemclick', me.onShortcutItemClick, me);

        var wallpaper = me.wallpaper;
        me.wallpaper = me.items.getAt(0);
        if (wallpaper) {
            me.setWallpaper(wallpaper, me.wallpaperStretch);
        }
    },

    afterRender: function () {
        var me = this;
        me.callParent();
        me.el.on('contextmenu', me.onDesktopMenu, me);
    },

    //------------------------------------------------------
    // Overrideable configuration creation methods

    createDataView: function () {
        var me = this;
        return {
            xtype: 'dataview',
            overItemCls: 'x-view-over',
            trackOver: true,
            itemSelector: me.shortcutItemSelector,
            store: me.shortcuts,
            tpl: new Ext.XTemplate(me.shortcutTpl)
        };
    },

    createDesktopMenu: function () {
        var me = this, ret = {
            items: me.contextMenuItems || []
        };

        if (ret.items.length) {
            ret.items.push('-');
        }

        ret.items.push(
                { text: 'Tile', handler: me.tileWindows, scope: me, minWindows: 1 },
                { text: 'Cascade', handler: me.cascadeWindows, scope: me, minWindows: 1 })

        return ret;
    },

    createWindowMenu: function () {
        var me = this;
        return {
            defaultAlign: 'br-tr',
            items: [
                { text: 'Restore', handler: me.onWindowMenuRestore, scope: me },
                { text: 'Minimize', handler: me.onWindowMenuMinimize, scope: me },
                { text: 'Maximize', handler: me.onWindowMenuMaximize, scope: me },
                '-',
                { text: 'Close', handler: me.onWindowMenuClose, scope: me }
            ],
            listeners: {
                beforeshow: me.onWindowMenuBeforeShow,
                hide: me.onWindowMenuHide,
                scope: me
            }
        };
    },

    //------------------------------------------------------
    // Event handler methods

    onDesktopMenu: function (e) {
        var me = this, menu = me.contextMenu;
        e.stopEvent();
        if (!menu.rendered) {
            menu.on('beforeshow', me.onDesktopMenuBeforeShow, me);
        }
        menu.showAt(e.getXY());
        menu.doConstrain();
    },

    onDesktopMenuBeforeShow: function (menu) {
        var me = this, count = me.windows.getCount();

        menu.items.each(function (item) {
            var min = item.minWindows || 0;
            item.setDisabled(count < min);
        });
    },

    onShortcutItemClick: function (dataView, record) {
        var me = this, module = me.app.getModule(record.data.module),
            win = module && module.createWindow();

        if (win) {
            me.restoreWindow(win);
        }
    },

    onWindowClose: function(win) {
        var me = this;
        me.windows.remove(win);
        me.taskbar.removeTaskButton(win.taskButton);
        me.updateActiveWindow();
    },

    //------------------------------------------------------
    // Window context menu handlers

    onWindowMenuBeforeShow: function (menu) {
        var items = menu.items.items, win = menu.theWin;
        items[0].setDisabled(win.maximized !== true && win.hidden !== true); // Restore
        items[1].setDisabled(win.minimized === true); // Minimize
        items[2].setDisabled(win.maximized === true || win.hidden === true); // Maximize
    },

    onWindowMenuClose: function () {
        var me = this, win = me.windowMenu.theWin;

        win.close();
    },

    onWindowMenuHide: function (menu) {
        menu.theWin = null;
    },

    onWindowMenuMaximize: function () {
        var me = this, win = me.windowMenu.theWin;

        win.maximize();
    },

    onWindowMenuMinimize: function () {
        var me = this, win = me.windowMenu.theWin;

        win.minimize();
    },

    onWindowMenuRestore: function () {
        var me = this, win = me.windowMenu.theWin;

        me.restoreWindow(win);
    },

    //------------------------------------------------------
    // Dynamic (re)configuration methods

    getWallpaper: function () {
        return this.wallpaper.wallpaper;
    },

    setTickSize: function(xTickSize, yTickSize) {
        var me = this,
            xt = me.xTickSize = xTickSize,
            yt = me.yTickSize = (arguments.length > 1) ? yTickSize : xt;

        me.windows.each(function(win) {
            var dd = win.dd, resizer = win.resizer;
            dd.xTickSize = xt;
            dd.yTickSize = yt;
            resizer.widthIncrement = xt;
            resizer.heightIncrement = yt;
        });
    },

    setWallpaper: function (wallpaper, stretch) {
        this.wallpaper.setWallpaper(wallpaper, stretch);
        return this;
    },

    //------------------------------------------------------
    // Window management methods

    cascadeWindows: function() {
        var x = 0, y = 0,
            zmgr = this.getDesktopZIndexManager();

        zmgr.eachBottomUp(function(win) {
            if (win.isWindow && win.isVisible() && !win.maximized) {
                win.setPosition(x, y);
                x += 20;
                y += 20;
            }
        });
    },

    createWindow: function(config, cls) {
        var me = this, win, cfg = Ext.applyIf(config || {}, {
                stateful: false,
                isWindow: true,
                constrainHeader: true,
                minimizable: true,
                maximizable: true
            });

        cls = cls || Ext.window.Window;
        win = me.add(new cls(cfg));

        me.windows.add(win);

        win.taskButton = me.taskbar.addTaskButton(win);
        win.animateTarget = win.taskButton.el;

        win.on({
            activate: me.updateActiveWindow,
            beforeshow: me.updateActiveWindow,
            deactivate: me.updateActiveWindow,
            minimize: me.minimizeWindow,
            destroy: me.onWindowClose,
            scope: me
        });

        win.on({
            afterrender: function () {
                win.dd.xTickSize = me.xTickSize;
                win.dd.yTickSize = me.yTickSize;

                if (win.resizer) {
                    win.resizer.widthIncrement = me.xTickSize;
                    win.resizer.heightIncrement = me.yTickSize;
                }
            },
            single: true
        });

        // replace normal window close w/fadeOut animation:
        win.doClose = function ()  {
            win.doClose = Ext.emptyFn; // dblclick can call again...
            win.el.disableShadow();
            win.el.fadeOut({
                listeners: {
                    afteranimate: function () {
                        win.destroy();
                    }
                }
            });
        };

        return win;
    },

    getActiveWindow: function () {
        var win = null,
            zmgr = this.getDesktopZIndexManager();

        if (zmgr) {
            // We cannot rely on activate/deactive because that fires against non-Window
            // components in the stack.

            zmgr.eachTopDown(function (comp) {
                if (comp.isWindow && !comp.hidden) {
                    win = comp;
                    return false;
                }
                return true;
            });
        }

        return win;
    },

    getDesktopZIndexManager: function () {
        var windows = this.windows;
        // TODO - there has to be a better way to get this...
        return (windows.getCount() && windows.getAt(0).zIndexManager) || null;
    },

    getWindow: function(id) {
        return this.windows.get(id);
    },

    minimizeWindow: function(win) {
        win.minimized = true;
        win.hide();
    },

    restoreWindow: function (win) {
        if (win.isVisible()) {
            win.restore();
            win.toFront();
        } else {
            win.show();
        }
        return win;
    },

    tileWindows: function() {
        var me = this, availWidth = me.body.getWidth(true);
        var x = me.xTickSize, y = me.yTickSize, nextY = y;

        me.windows.each(function(win) {
            if (win.isVisible() && !win.maximized) {
                var w = win.el.getWidth();

                // Wrap to next row if we are not at the line start and this Window will
                // go off the end
                if (x > me.xTickSize && x + w > availWidth) {
                    x = me.xTickSize;
                    y = nextY;
                }

                win.setPosition(x, y);
                x += w + me.xTickSize;
                nextY = Math.max(nextY, y + win.el.getHeight() + me.yTickSize);
            }
        });
    },

    updateActiveWindow: function () {
        var me = this, activeWindow = me.getActiveWindow(), last = me.lastActiveWindow;
        if (activeWindow === last) {
            return;
        }

        if (last) {
            if (last.el.dom) {
                last.addCls(me.inactiveWindowCls);
                last.removeCls(me.activeWindowCls);
            }
            last.active = false;
        }

        me.lastActiveWindow = activeWindow;

        if (activeWindow) {
            activeWindow.addCls(me.activeWindowCls);
            activeWindow.removeCls(me.inactiveWindowCls);
            activeWindow.minimized = false;
            activeWindow.active = true;
        }

        me.taskbar.setActiveButton(activeWindow && activeWindow.taskButton);
    }
});

/*

This file is part of Ext JS 4

Copyright (c) 2011 Sencha Inc

Contact:  http://www.sencha.com/contact

GNU General Public License Usage
This file may be used under the terms of the GNU General Public License version 3.0 as published by the Free Software Foundation and appearing in the file LICENSE included in the packaging of this file.  Please review the following information to ensure the GNU General Public License version 3.0 requirements will be met: http://www.gnu.org/copyleft/gpl.html.

If you are unsure which license is appropriate for your use, please contact the sales department at http://www.sencha.com/contact.

*/
/*!
 * Ext JS Library 4.0
 * Copyright(c) 2006-2011 Sencha Inc.
 * licensing@sencha.com
 * http://www.sencha.com/license
 */

/**
 * @class Ext.ux.desktop.FitAllLayout
 * @extends Ext.layout.container.AbstractFit
 * <p>This layout applies a "fit" layout to all items, overlaying them on top of each
 * other.</p>
 */
Ext.define('Ext.ux.desktop.FitAllLayout', {
    extend: 'Ext.layout.container.AbstractFit',
    alias: 'layout.fitall',

    // @private
    onLayout : function() {
        var me = this;
        me.callParent();

        var size = me.getLayoutTargetSize();

        me.owner.items.each(function (item) {
            me.setItemBox(item, size);
        });
    },

    getTargetBox : function() {
        return this.getLayoutTargetSize();
    },

    setItemBox : function(item, box) {
        var me = this;
        if (item && box.height > 0) {
            if (item.layoutManagedWidth == 2) {
               box.width = undefined;
            }
            if (item.layoutManagedHeight == 2) {
               box.height = undefined;
            }

            item.getEl()('absolute', null, 0, 0);
            me.setItemSize(item, box.width, box.height);
        }
    }
});

/*

This file is part of Ext JS 4

Copyright (c) 2011 Sencha Inc

Contact:  http://www.sencha.com/contact

GNU General Public License Usage
This file may be used under the terms of the GNU General Public License version 3.0 as published by the Free Software Foundation and appearing in the file LICENSE included in the packaging of this file.  Please review the following information to ensure the GNU General Public License version 3.0 requirements will be met: http://www.gnu.org/copyleft/gpl.html.

If you are unsure which license is appropriate for your use, please contact the sales department at http://www.sencha.com/contact.

*/
/*!
 * Ext JS Library 4.0
 * Copyright(c) 2006-2011 Sencha Inc.
 * licensing@sencha.com
 * http://www.sencha.com/license
 */

Ext.define('Ext.ux.desktop.Module', {
    mixins: {
        observable: 'Ext.util.Observable'
    },

    constructor: function (config) {
        this.mixins.observable.constructor.call(this, config);
        this.init();
    },

    init: Ext.emptyFn
});

/*

This file is part of Ext JS 4

Copyright (c) 2011 Sencha Inc

Contact:  http://www.sencha.com/contact

GNU General Public License Usage
This file may be used under the terms of the GNU General Public License version 3.0 as published by the Free Software Foundation and appearing in the file LICENSE included in the packaging of this file.  Please review the following information to ensure the GNU General Public License version 3.0 requirements will be met: http://www.gnu.org/copyleft/gpl.html.

If you are unsure which license is appropriate for your use, please contact the sales department at http://www.sencha.com/contact.

*/
/*!
 * Ext JS Library 4.0
 * Copyright(c) 2006-2011 Sencha Inc.
 * licensing@sencha.com
 * http://www.sencha.com/license
 */

/**
 * @class Ext.ux.desktop.ShortcutModel
 * @extends Ext.data.Model
 * This model defines the minimal set of fields for desktop shortcuts.
 */
Ext.define('Ext.ux.desktop.ShortcutModel', {
    extend: 'Ext.data.Model',
    fields: [
       { name: 'name' },
       { name: 'iconCls' },
       { name: 'module' }
    ]
});

/*

This file is part of Ext JS 4

Copyright (c) 2011 Sencha Inc

Contact:  http://www.sencha.com/contact

GNU General Public License Usage
This file may be used under the terms of the GNU General Public License version 3.0 as published by the Free Software Foundation and appearing in the file LICENSE included in the packaging of this file.  Please review the following information to ensure the GNU General Public License version 3.0 requirements will be met: http://www.gnu.org/copyleft/gpl.html.

If you are unsure which license is appropriate for your use, please contact the sales department at http://www.sencha.com/contact.

*/
/*!
 * Ext JS Library 4.0
 * Copyright(c) 2006-2011 Sencha Inc.
 * licensing@sencha.com
 * http://www.sencha.com/license
 */

Ext.define('Ext.ux.desktop.StartMenu', {
    extend: 'Ext.panel.Panel',

    requires: [
        'Ext.menu.Menu',
        'Ext.toolbar.Toolbar'
    ],

    ariaRole: 'menu',

    cls: 'x-menu ux-start-menu',

    defaultAlign: 'bl-tl',

    iconCls: 'user',

    floating: true,

    shadow: true,

    // We have to hardcode a width because the internal Menu cannot drive our width.
    // This is combined with changing the align property of the menu's layout from the
    // typical 'stretchmax' to 'stretch' which allows the the items to fill the menu
    // area.
    width: 300,

    initComponent: function() {
        var me = this, menu = me.menu;

        me.menu = new Ext.menu.Menu({
            cls: 'ux-start-menu-body',
            border: false,
            floating: false,
            items: menu
        });
        me.menu.layout.align = 'stretch';

        me.items = [me.menu];
        me.layout = 'fit';

        Ext.menu.Manager.register(me);
        me.callParent();
        // TODO - relay menu events

        me.toolbar = new Ext.toolbar.Toolbar(Ext.apply({
            dock: 'right',
            cls: 'ux-start-menu-toolbar',
            vertical: true,
            width: 100
        }, me.toolConfig));

        me.toolbar.layout.align = 'stretch';
        me.addDocked(me.toolbar);

        delete me.toolItems;

        me.on('deactivate', function () {
            me.hide();
        });
    },

    addMenuItem: function() {
        var cmp = this.menu;
        cmp.add.apply(cmp, arguments);
    },

    addToolItem: function() {
        var cmp = this.toolbar;
        cmp.add.apply(cmp, arguments);
    },

    showBy: function(cmp, pos, off) {
        var me = this;

        if (me.floating && cmp) {
            me.layout.autoSize = true;
            me.show();

            // Component or Element
            cmp = cmp.el || cmp;

            // Convert absolute to floatParent-relative coordinates if necessary.
            var xy = me.el.getAlignToXY(cmp, pos || me.defaultAlign, off);
            if (me.floatParent) {
                var r = me.floatParent.getTargetEl().getViewRegion();
                xy[0] -= r.x;
                xy[1] -= r.y;
            }
            me.showAt(xy);
            me.doConstrain();
        }
        return me;
    }
}); // StartMenu

/*

This file is part of Ext JS 4

Copyright (c) 2011 Sencha Inc

Contact:  http://www.sencha.com/contact

GNU General Public License Usage
This file may be used under the terms of the GNU General Public License version 3.0 as published by the Free Software Foundation and appearing in the file LICENSE included in the packaging of this file.  Please review the following information to ensure the GNU General Public License version 3.0 requirements will be met: http://www.gnu.org/copyleft/gpl.html.

If you are unsure which license is appropriate for your use, please contact the sales department at http://www.sencha.com/contact.

*/
/*!
 * Ext JS Library 4.0
 * Copyright(c) 2006-2011 Sencha Inc.
 * licensing@sencha.com
 * http://www.sencha.com/license
 */

/**
 * @class Ext.ux.desktop.TaskBar
 * @extends Ext.toolbar.Toolbar
 */
Ext.define('Ext.ux.desktop.TaskBar', {
    extend: 'Ext.toolbar.Toolbar', // TODO - make this a basic hbox panel...

    requires: [
        'Ext.button.Button',
        'Ext.resizer.Splitter',
        'Ext.menu.Menu',

        'Ext.ux.desktop.StartMenu'
    ],

    alias: 'widget.taskbar',

    cls: 'ux-taskbar',

    /**
     * @cfg {String} startBtnText
     * The text for the Start Button.
     */
    startBtnText: 'Start',

    initComponent: function () {
        var me = this;

        me.startMenu = new Ext.ux.desktop.StartMenu(me.startConfig);

        me.quickStart = new Ext.toolbar.Toolbar(me.getQuickStart());

        me.windowBar = new Ext.toolbar.Toolbar(me.getWindowBarConfig());

        me.tray = new Ext.toolbar.Toolbar(me.getTrayConfig());

        me.items = [
            {
                xtype: 'button',
                cls: 'ux-start-button',
                iconCls: 'ux-start-button-icon',
                menu: me.startMenu,
                menuAlign: 'bl-tl',
                text: me.startBtnText
            },
            me.quickStart,
            {
                xtype: 'splitter', html: '&#160;',
                height: 14, width: 2, // TODO - there should be a CSS way here
                cls: 'x-toolbar-separator x-toolbar-separator-horizontal'
            },
            //'-',
            me.windowBar,
            '-',
            me.tray
        ];

        me.callParent();
    },

    afterLayout: function () {
        var me = this;
        me.callParent();
        me.windowBar.el.on('contextmenu', me.onButtonContextMenu, me);
    },

    /**
     * This method returns the configuration object for the Quick Start toolbar. A derived
     * class can override this method, call the base version to build the config and
     * then modify the returned object before returning it.
     */
    getQuickStart: function () {
        var me = this, ret = {
            minWidth: 20,
            width: 60,
            items: [],
            enableOverflow: true
        };

        Ext.each(this.quickStart, function (item) {
            ret.items.push({
                tooltip: { text: item.name, align: 'bl-tl' },
                //tooltip: item.name,
                overflowText: item.name,
                iconCls: item.iconCls,
                module: item.module,
                handler: me.onQuickStartClick,
                scope: me
            });
        });

        return ret;
    },

    /**
     * This method returns the configuration object for the Tray toolbar. A derived
     * class can override this method, call the base version to build the config and
     * then modify the returned object before returning it.
     */
    getTrayConfig: function () {
        var ret = {
            width: 80,
            items: this.trayItems
        };
        delete this.trayItems;
        return ret;
    },

    getWindowBarConfig: function () {
        return {
            flex: 1,
            cls: 'ux-desktop-windowbar',
            items: [ '&#160;' ],
            layout: { overflowHandler: 'Scroller' }
        };
    },

    getWindowBtnFromEl: function (el) {
        var c = this.windowBar.getChildByElement(el);
        return c || null;
    },

    onQuickStartClick: function (btn) {
        var module = this.app.getModule(btn.module);
        if (module) {
            module.createWindow();
        }
    },
    
    onButtonContextMenu: function (e) {
        var me = this, t = e.getTarget(), btn = me.getWindowBtnFromEl(t);
        if (btn) {
            e.stopEvent();
            me.windowMenu.theWin = btn.win;
            me.windowMenu.showBy(t);
        }
    },

    onWindowBtnClick: function (btn) {
        var win = btn.win;

        if (win.minimized || win.hidden) {
            win.show();
        } else if (win.active) {
            win.minimize();
        } else {
            win.toFront();
        }
    },

    addTaskButton: function(win) {
        var config = {
            iconCls: win.iconCls,
            enableToggle: true,
            toggleGroup: 'all',
            width: 140,
            text: Ext.util.Format.ellipsis(win.title, 20),
            listeners: {
                click: this.onWindowBtnClick,
                scope: this
            },
            win: win
        };

        var cmp = this.windowBar.add(config);
        cmp.toggle(true);
        return cmp;
    },

    removeTaskButton: function (btn) {
        var found, me = this;
        me.windowBar.items.each(function (item) {
            if (item === btn) {
                found = item;
            }
            return !found;
        });
        if (found) {
            me.windowBar.remove(found);
        }
        return found;
    },

    setActiveButton: function(btn) {
        if (btn) {
            btn.toggle(true);
        } else {
            this.windowBar.items.each(function (item) {
                if (item.isButton) {
                    item.toggle(false);
                }
            });
        }
    }
});

/**
 * @class Ext.ux.desktop.TrayClock
 * @extends Ext.toolbar.TextItem
 * This class displays a clock on the toolbar.
 */
Ext.define('Ext.ux.desktop.TrayClock', {
    extend: 'Ext.toolbar.TextItem',

    alias: 'widget.trayclock',

    cls: 'ux-desktop-trayclock',

    html: '&#160;',

    timeFormat: 'g:i A',

    tpl: '{time}',

    initComponent: function () {
        var me = this;

        me.callParent();

        if (typeof(me.tpl) == 'string') {
            me.tpl = new Ext.XTemplate(me.tpl);
        }
    },

    afterRender: function () {
        var me = this;
        Ext.Function.defer(me.updateTime, 100, me);
        me.callParent();
    },

    onDestroy: function () {
        var me = this;

        if (me.timer) {
            window.clearTimeout(me.timer);
            me.timer = null;
        }

        me.callParent();
    },

    updateTime: function () {
        var me = this, time = Ext.Date.format(new Date(), me.timeFormat),
            text = me.tpl.apply({ time: time });
        if (me.lastText != text) {
            me.setText(text);
            me.lastText = text;
        }
        me.timer = Ext.Function.defer(me.updateTime, 10000, me);
    }
});

/*

This file is part of Ext JS 4

Copyright (c) 2011 Sencha Inc

Contact:  http://www.sencha.com/contact

GNU General Public License Usage
This file may be used under the terms of the GNU General Public License version 3.0 as published by the Free Software Foundation and appearing in the file LICENSE included in the packaging of this file.  Please review the following information to ensure the GNU General Public License version 3.0 requirements will be met: http://www.gnu.org/copyleft/gpl.html.

If you are unsure which license is appropriate for your use, please contact the sales department at http://www.sencha.com/contact.

*/
/*!
* Ext JS Library 4.0
* Copyright(c) 2006-2011 Sencha Inc.
* licensing@sencha.com
* http://www.sencha.com/license
*/

// From code originally written by David Davis (http://www.sencha.com/blog/html5-video-canvas-and-ext-js/)

/* -NOTICE-
 * For HTML5 video to work, your server must
 * send the right content type, for more info see:
 * http://developer.mozilla.org/En/HTML/Element/Video
 */

Ext.define('Ext.ux.desktop.Video', {
    extend: 'Ext.panel.Panel',

    alias: 'widget.video',

    width: '100%',
    height: '100%',
    autoplay: false,
    controls: true,
    bodyStyle: 'background-color:#000;color:#fff',
    html: '',

    initComponent: function () {
        this.callParent();
    },

    afterRender: function () {
        var fallback;

        if (this.fallbackHTML) {
            fallback = this.fallbackHTML;
        } else {
            fallback = "Your browser does not support HTML5 Video. ";

            if (Ext.isChrome) {
                fallback += 'Upgrade Chrome.';
            } else if (Ext.isGecko) {
                fallback += 'Upgrade to Firefox 3.5 or newer.';
            } else {
                var chrome = '<a href="http://www.google.com/chrome">Chrome</a>';
                fallback += 'Please try <a href="http://www.mozilla.com">Firefox</a>';

                if (Ext.isIE) {
                    fallback += ', ' + chrome +
                        ' or <a href="http://www.google.com/chromeframe">Chrome Frame for IE</a>.';
                } else {
                    fallback += ' or ' + chrome + '.';
                }
            }
        }

        // match the video size to the panel dimensions
        var size = this.getSize();

        var cfg = Ext.copyTo({
            tag   : 'video',
            width : size.width,
            height: size.height
        },
        this, 'poster,start,loopstart,loopend,playcount,autobuffer,loop');

        // just having the params exist enables them
        if (this.autoplay) {
            cfg.autoplay = 1;
        }
        if (this.controls) {
            cfg.controls = 1;
        }

        // handle multiple sources
        if (Ext.isArray(this.src)) {
            cfg.children = [];

            for (var i = 0, len = this.src.length; i < len; i++) {
                if (!Ext.isObject(this.src[i])) {
                    Ext.Error.raise('The src list passed to "video" must be an array of objects');
                }

                cfg.children.push(
                    Ext.applyIf({tag: 'source'}, this.src[i])
                );
            }

            cfg.children.push({
                html: fallback
            });

        } else {
            cfg.src  = this.src;
            cfg.html = fallback;
        }

        this.video = this.body.createChild(cfg);
        var el = this.video.dom;
        this.supported = (el && el.tagName.toLowerCase() == 'video');
    },

    doComponentLayout : function() {
        var me = this;

        me.callParent(arguments);

        if (me.video)
            me.video.setSize(me.body.getSize());
    },

    onDestroy: function () {
        var video = this.video;
        if (video) {
            var videoDom = video.dom;
            if (videoDom && videoDom.pause) {
                videoDom.pause();
            }
            video.remove();
            this.video = null;
        }

        this.callParent();
    }
});

/*

This file is part of Ext JS 4

Copyright (c) 2011 Sencha Inc

Contact:  http://www.sencha.com/contact

GNU General Public License Usage
This file may be used under the terms of the GNU General Public License version 3.0 as published by the Free Software Foundation and appearing in the file LICENSE included in the packaging of this file.  Please review the following information to ensure the GNU General Public License version 3.0 requirements will be met: http://www.gnu.org/copyleft/gpl.html.

If you are unsure which license is appropriate for your use, please contact the sales department at http://www.sencha.com/contact.

*/
/*!
 * Ext JS Library 4.0
 * Copyright(c) 2006-2011 Sencha Inc.
 * licensing@sencha.com
 * http://www.sencha.com/license
 */

/**
 * @class Ext.ux.desktop.Wallpaper
 * @extends Ext.Component
 * <p>This component renders an image that stretches to fill the component.</p>
 */
Ext.define('Ext.ux.desktop.Wallpaper', {
    extend: 'Ext.Component',

    alias: 'widget.wallpaper',

    cls: 'ux-wallpaper',
    html: '<img src="'+Ext.BLANK_IMAGE_URL+'">',

    stretch: false,
    wallpaper: null,

    afterRender: function () {
        var me = this;
        me.callParent();
        me.setWallpaper(me.wallpaper, me.stretch);
    },

    applyState: function () {
        var me = this, old = me.wallpaper;
        me.callParent(arguments);
        if (old != me.wallpaper) {
            me.setWallpaper(me.wallpaper);
        }
    },

    getState: function () {
        return this.wallpaper && { wallpaper: this.wallpaper };
    },

    setWallpaper: function (wallpaper, stretch) {
        var me = this, imgEl, bkgnd;

        me.stretch = (stretch !== false);
        me.wallpaper = wallpaper;

        if (me.rendered) {
            imgEl = me.el.dom.firstChild;

            if (!wallpaper || wallpaper == Ext.BLANK_IMAGE_URL) {
                Ext.fly(imgEl).hide();
            } else if (me.stretch) {
                imgEl.src = wallpaper;

                me.el.removeCls('ux-wallpaper-tiled');
                Ext.fly(imgEl).setStyle({
                    width: '100%',
                    height: '100%'
                }).show();
            } else {
                Ext.fly(imgEl).hide();

                bkgnd = 'url('+wallpaper+')';
                me.el.addCls('ux-wallpaper-tiled');
            }

            me.el.setStyle({
                backgroundImage: bkgnd || ''
            });
        }
        return me;
    }
});

