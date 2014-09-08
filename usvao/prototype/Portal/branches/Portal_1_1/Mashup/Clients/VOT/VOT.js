Ext.require([
    'Ext.grid.*',
    'Ext.data.*',
    'Ext.util.*',
    'Ext.state.*'
]);

Ext.onReady(function() {
    
    //var mainPanel = new MainPanel({
    //    renderTo: 'vot-example'
    //});
    //mainPanel.show();
    
    //var mainPanel = new TestPanel({
    //    renderTo: 'vot-example'
    //});
    //mainPanel.show();

    //var mainPanel = new TestPanelPage({
    //    renderTo: 'vot-example'
    //});
    
    //var mainPanel = new TestIS({
    //    renderTo: 'vot-example'
    //});
    //mainPanel.show();
    
    //var mainPanel = new TestFilters({
    //    renderTo: 'vot-example'
    //});
    //mainPanel.show();
    
    var mainPanel = Ext.create('TestLayout', {
        renderTo: 'vot-example'
    });
    mainPanel.show();
    
});

