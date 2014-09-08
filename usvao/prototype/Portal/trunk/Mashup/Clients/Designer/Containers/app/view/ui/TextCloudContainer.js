
Ext.define('Mvpc.view.ui.TextCloudContainer', {
    extend: 'Ext.panel.Panel',

    itemId: 'textCloudContainer',
    layout: {
        type: 'fit',
        width: 300
    },
    bodyCls: 'textCloud',
    animCollapse: false,
    collapsible: true,
    title: 'TextCloud',

    initComponent: function () {
        var me = this;
        me.callParent(arguments);

        this.initCloudPanel();
    },

    initCloudPanel: function () {
        if (!this.cloudPanel) {
            this.cloudPanel = new Ext.Panel({
                layout: 'fit',
                id: 'cloudPanel',
                items: {
                    xtype: 'box',
                    autoEl: {
                        tag: 'canvas'
                    },
                    listeners: {
                        resize: this.onResize
                    }
                }
            });
            this.add(this.cloudPanel);
        }
        this.currentTerms = null;
    },

    setupCanvas: function () {
        if (this.cloudPanel) {
            if (this.cloudPanel.items.items[0].el.dom) {
                var canvas = this.cloudPanel.items.items[0].el.dom;
                canvas.height = this.cloudPanel.el.dom.clientHeight;
                canvas.width = this.cloudPanel.el.dom.clientWidth;

                if (!this.mouseSetup) {
                    canvas.addEventListener("mousemove", on_mousemove, false);
                    canvas.addEventListener("click", on_click, false);
                    this.mouseSetup = true;
                }

                return canvas;
            }
        }

        return null;
    },

    drawCloud: function (weightedTerms) {
        this.currentTerms = weightedTerms;
        var canvas = this.setupCanvas();
        if (canvas) {

            if (!this.textCloudEngine) {
                this.textCloudEngine = new metaaps.nebulos(canvas);
            }
            this.textCloudEngine.draw(this.currentTerms);
        }
    },

    clearCloud: function () {
        if (this.textCloudEngine)
            this.textCloudEngine.clear();
        this.currentTerms = null;
    },

    onResize: function (event) {
        if (TextCloudContainer.currentTerms)
            TextCloudContainer.drawCloud(TextCloudContainer.currentTerms);
    }

});

function on_mousemove(e) {
    var mouseX, mouseY;
    if (TextCloudContainer.textCloudEngine) {
        if (e.offsetX) {
            mouseX = e.offsetX;
            mouseY = e.offsetY;
        }
        else if (e.layerX) {
            mouseX = e.layerX;
            mouseY = e.layerY;
        }

        if (TextCloudContainer.textCloudEngine.getBox(mouseX, mouseY)) {
            document.body.style.cursor = "pointer";
        }
        else {
            document.body.style.cursor = "";
        }
    }
}


function on_click(e) {
    if (TextCloudContainer.textCloudEngine) {

        var mouseX, mouseY;
        if (e.offsetX) {
            mouseX = e.offsetX;
            mouseY = e.offsetY;
        }
        else if (e.layerX) {
            mouseX = e.layerX;
            mouseY = e.layerY;
        }

        TextCloudContainer.textCloudEngine.on_click(mouseX, mouseY);
    }
}