/*
Copyright(c) 2012 Space Telescope Science Institute
*/
Ext.define("Mvp.util.ExtBugWorkarounds", { requires: ["Ext.data.Store", "Ext.grid.PagingScroller", "Ext.view.AbstractView", "Ext.layout.ContextItem"], statics: { closeTabBug: function () { Ext.override(Ext.view.AbstractView, { bindStore: function (a, b) { if (!b && this.store) { if (a !== this.store && this.store.autoDestroy) { this.store.destroy() } else { this.mun(this.store, { scope: this, datachanged: this.onDataChanged, add: this.onAdd, remove: this.onRemove, update: this.onUpdate, clear: this.refresh }) } if (!a) { if (this.loadMask && typeof this.loadMask.bindStore == "function") { this.loadMask.bindStore(null) } this.store = null } } if (a) { a = Ext.data.StoreManager.lookup(a); this.mon(a, { scope: this, datachanged: this.onDataChanged, add: this.onAdd, remove: this.onRemove, update: this.onUpdate, clear: this.refresh }); if (this.loadMask && typeof this.loadMask.bindStore == "function") { this.loadMask.bindStore(a) } } this.store = a; this.getSelectionModel().bind(a); if (a && (!b || a.getCount())) { this.refresh(true) } } }) }, scrollBarSizeBug: function () { Ext.override(Ext.grid.PagingScroller, { onViewRefresh: function () { var c = this, e = c.store, b, d = c.view, h = d.el.dom, j, g, a, i, f; if (!e.getCount()) { return } if (e.getCount() === e.getTotalCount()) { } else { c.disabled = false } c.stretcher.setHeight(b = c.getScrollHeight()); if (c.scrollProportion !== undefined) { i = c.view.el.child("table", true); c.scrollProportion = d.el.dom.scrollTop / (b - i.offsetHeight); i = c.view.el.child("table", true); i.style.position = "absolute"; i.style.top = (c.scrollProportion ? (b * c.scrollProportion) - (i.offsetHeight * c.scrollProportion) : 0) + "px" } else { i = c.view.el.child("table", true); i.style.position = "absolute"; i.style.top = (f = (c.tableStart || 0) * c.rowHeight) + "px"; if (c.scrollOffset) { j = d.getNodes(); g = -d.el.getOffsetsTo(j[c.commonRecordIndex])[1]; a = g - c.scrollOffset; c.position = (d.el.dom.scrollTop += a) } else { if ((f > h.scrollTop) || ((f + i.offsetHeight) < h.scrollTop + h.clientHeight)) { c.position = h.scrollTop = f } } } } }) }, sortSmallDataSetBug: function () { Ext.override(Ext.data.Store, { prefetchPage: function (f, c) { var e = this, b = e.pageSize || e.defaultPageSize, g = (f - 1) * e.pageSize, a = g + b, d = e.getTotalCount(); if (d) { a = Math.min(a, d) } e.prefetch(Ext.apply({ page: f, start: g, limit: b }, c)) } }) }, filterBug_4_1_0: function () { Ext.override(Ext.data.Store, { cancelAllPrefetches: function () { var c = this, a = c.pageRequests, b, d; if (c.pageMap.events.pageadded) { c.pageMap.events.pageadded.clearListeners() } for (d in a) { if (a.hasOwnProperty(d)) { b = a[d]; delete a[d]; if (b && b.callback) { delete b.callback } } } } }) }, googleChrome18Bug: function () { Ext.override(Ext.layout.ContextItem, { setHeight: function (a, c) { var e = this, b = e.target, g, d, f; if (isNaN(a)) { return } if (a < 0) { a = 0 } if (!e.wrapsComponent) { if (!e.setProp("height", a, c)) { return NaN } } else { a = Ext.Number.constrain(a, b.minHeight || 0, b.maxHeight); if (!e.setProp("height", a, c)) { return NaN } g = e.frameBodyContext; if (g) { d = e.getFrameInfo(); g.setHeight(a - d.height, c) } } return a }, setWidth: function (c, b) { var e = this, a = e.target, g, d, f; if (isNaN(c)) { return } if (c < 0) { c = 0 } if (!e.wrapsComponent) { if (!e.setProp("width", c, b)) { return NaN } } else { c = Ext.Number.constrain(c, a.minWidth || 0, a.maxWidth); if (!e.setProp("width", c, b)) { return NaN } g = e.frameBodyContext; if (g) { d = e.getFrameInfo(); g.setWidth(c - d.width, b) } } return c } }) }, noGridScroller: function () { Ext.override(Ext.grid.PagingScroller, { handleViewScroll: function (g) { var d = this, i = d.store, f = d.view, e = d.viewSize, j = i.getTotalCount(), c = j - e, b = d.getFirstVisibleRowIndex(), h = d.getLastVisibleRowIndex(), a, k; d.scrollProportion = undefined; if (g == -1) { if (b !== undefined) { if (b < (d.tableStart + d.numFromEdge)) { a = Math.max(0, h + d.trailingBufferZone - e) } } else { d.scrollProportion = f.el.dom.scrollTop / (f.el.dom.scrollHeight - f.el.dom.clientHeight); a = Math.max(0, j * d.scrollProportion - (e / 2) - d.numFromEdge - ((d.leadingBufferZone + d.trailingBufferZone) / 2)) } } else { if (b !== undefined) { if (h > (d.tableEnd - d.numFromEdge)) { a = Math.max(0, b - d.trailingBufferZone) } } else { d.scrollProportion = f.el.dom.scrollTop / (f.el.dom.scrollHeight - f.el.dom.clientHeight); a = j * d.scrollProportion - (e / 2) - d.numFromEdge - ((d.leadingBufferZone + d.trailingBufferZone) / 2) } } if (a !== undefined) { if (a > c) { a = c & ~1; k = j - 1 } else { a = a & ~1; k = a + e - 1 } if (a < 0) { a = 0 } if (i.rangeCached(a, k)) { d.cancelLoad(); i.guaranteeRange(a, k); if (Ext.tomCount === undefined) { Ext.tomCount = 0 } Ext.tomCount++ } else { d.attemptLoad(a, k) } } } }) } } }); Ext.define("Ext.tip.QuickTipManager", (function () { var b, a = false; return { requires: ["Ext.tip.QuickTip"], singleton: true, alternateClassName: "Ext.QuickTips", init: function (f, d) { if (!b) { if (!Ext.isReady) { Ext.onReady(function () { Ext.tip.QuickTipManager.init(f, d) }); return } var c = Ext.apply({ disabled: a, id: "ext-quicktips-tip" }, d), e = c.className, g = c.xtype; if (e) { delete c.className } else { if (g) { e = "widget." + g; delete c.xtype } } if (f !== false) { c.renderTo = document.body; if (c.renderTo.tagName.toUpperCase() != "BODY") { Ext.Error.raise({ sourceClass: "Ext.tip.QuickTipManager", sourceMethod: "init", msg: "Cannot init QuickTipManager: no document body" }) } } b = Ext.create(e || "Ext.tip.QuickTip", c) } }, destroy: function () { if (b) { var c; b.destroy(); b = c } }, ddDisable: function () { if (b && !a) { b.disable() } }, ddEnable: function () { if (b && !a) { b.enable() } }, enable: function () { if (b) { b.enable() } a = false }, disable: function () { if (b) { b.disable() } a = true }, isEnabled: function () { return b !== undefined && !b.disabled }, getQuickTip: function () { return b }, register: function () { b.register.apply(b, arguments) }, unregister: function () { b.unregister.apply(b, arguments) }, tips: function () { b.register.apply(b, arguments) } } }())); Ext.define("Mast.Portal", { requires: ["Mast.view.MastTopBar", "Ext.util.Cookies", "Mvp.util.Version", "Ext.ux.IFrame", "Ext.layout.container.Border", "Ext.container.Viewport", "Mvp.search.SearchParams"], extend: "Mvp.util.BasePortal", constructor: function (a) { this.callParent(arguments) }, run: function () { this.mainPanel = Ext.create("Mvp.gui.PortalBorderContainer", { region: "center", avPanel: this.avPanel }); var e = Mvp.search.SearchParams; var g = [e.getSearch("CAOMDB"), e.getSearch("DataScope")]; var h = [e.getSearch("CAOM"), e.getSearch("WhatIs"), e.getSearch("ADS"), e.getSearch("CSV"), e.getSearch("CAOMDownload"), e.getSearch("GalexObjects"), e.getSearch("GalexSdss"), e.getSearch("GalexTiles"), e.getSearch("HLA"), e.getSearch("HLSP"), e.getSearch("STPR"), e.getSearch("HSTPR"), e.getSearch("RP"), e.getSearch("SANTA"), e.getSearch("SedAvailability"), e.getSearch("SedRetrieval"), e.getSearch("SID"), e.getSearch("STP"), e.getSearch("STPR"), e.getSearch("VOTable")]; if (AppConfig.isDevelopment) { g = g.concat(h) } var b = Ext.create("Mast.view.MastTopBar", { searchParams: g, defaultSearch: "CAOMDB", versionString: Mvp.util.Version.versionString() }); b.addListener("newsearch", this.searchCallback, this); this.viewport = Ext.create("Ext.container.Viewport", { renderTo: this.mainDiv, margin: 0, layout: "border", items: this.mainPanel }); this.mainPanel.getNorth().add(b); this.searchPanel = b; this.resultPanel = this.mainPanel.getResultPanel(); this.resultPanel.on("tabchange", this.onTabChange, this); var c = this.app.getEventManager(); c.addListener("APP.context.added", this.contextAdded, this); c.addListener("APP.context.removed", this.contextRemoved, this); c.addListener("APP.AstroView.Search.Request", this.searchWhatIs, this); var f = AppConfig.startPage || "data/html/Start Page.html"; var a = Ext.create("Ext.ux.IFrame", { src: f, width: 600 }); var d = Ext.create("Ext.panel.Panel", { title: "Start Page", layout: "fit", padding: 20, width: 600, items: [a], closable: true }); this.resultPanel.add(d) }, contextAdded: function () { }, contextRemoved: function () { }, searchWhatIs: function (b) { var c = Mvp.search.SearchParams.getSearch("WhatIs"); c.coords = b.coords; var a = { inputText: b.coords, title: "What Is", description: "XXX" } } });