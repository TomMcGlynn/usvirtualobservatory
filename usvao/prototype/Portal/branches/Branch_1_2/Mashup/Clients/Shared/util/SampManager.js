Ext.define('Mvp.util.SampManager', {

    singleton: true,
        
    config: {
        connector: null,
        sendOptions: null
    },
    
    constructor: function(config) {
        this.initConfig(config);

        return this;
    },
    
    applyConnector: function (connector) {
        var me = this;
        /*
         * Make a connection to the hup and register mtypes.
         */
        me.clientTracker = new samp.ClientTracker();
        me.clientTracker.callHandler["samp.app.ping"] = function(senderId, message, isCall) {
            if (isCall) {
                return {text: "ping to you, " + me.clientTracker.getName(senderId)};
            }
        };
        
        logCc = {
                receiveNotification: function(senderId, message) {
                    var handled = me.clientTracker.receiveNotification(senderId, message);
                    Ext.log("SAMP: notification: " + message["samp.mtype"] +
                                " from " + me.clientTracker.getName(senderId));
                    
                    if( message["samp.mtype"] == 'samp.hub.event.unregister' ){
                        me.connector.unregister();
                    }
                },
                receiveCall: function(senderId, msgId, message) {
                    var handled = me.clientTracker.receiveCall(senderId, msgId, message);
                    Ext.log("SAMP: call: " + message["samp.mtype"] +
                                " from " + me.clientTracker.getName(senderId));
                },
                receiveResponse: function(responderId, msgTag, response) {
                    var handled = me.clientTracker.receiveResponse(responderId, msgTag, response);
                    Ext.log("SAMP: response: " + msgTag +
                                " from " + me.clientTracker.getName(responderId));
                },
                init: function(connection) {
                    me.clientTracker.init(connection);
                }
            };

            var meta = {
                    "samp.name": "Portal",
                    "samp.description": "Web Profile interface for VAO Portal application"
                    //, "samp.icon.url": baseUrl + "clientIcon.gif"
            };
            
            var subs = me.clientTracker.calculateSubscriptions();
            subs = {"*": {}};
            var connector = new samp.Connector("Portal", meta, logCc, subs);
            
            connector.onunreg = function() {
                Ext.log("SAMP: connection closed");
            };
                        
            return connector;
    },
    
    connect: function(callback){
        var me = this;
        this.callback = callback;

        var regErrHandler = function(err) {
            Ext.log("SAMP: connection error: " + err.toString() );
        };
                
        var regSuccessHandler = function(conn) {
            me.connector.setConnection(conn);
            Ext.log("SAMP: connection established: " + conn ? "Yes" : "No");
            me.callback();
        };

        var connection = me.connector.connection;
        if( connection ){
            me.callback();
        }else{
            Ext.log("SAMP: No connection, try and register");
            samp.register(me.connector.name, regSuccessHandler, regErrHandler);
        }
        
    },
    
    sendTable: function (options){
        this.sendOptions = options;
        this.connect(this.sendTableCallback);
    },
    
    sendTableCallback: function(){
        var me = this;
        var data = null;
        
        var sendTable = function(responseObject, requestOptions, queryScope, complete, updated) {
            if (complete) {
                // Get the URL and start the download.
                this.complete = complete;
                var url = null;
                data = responseObject.data;
                var connection = me.connector.connection;
                if (data) {
                    var tableMessage = new samp.Message("table.load.votable", {url: data.url});
                    var ids = connection.notifyAll([tableMessage], null, null);
                    // if( ids.length == 0 ){
                    //     Ext.log("SAMP: No clients sent notification.");
                    // }            
                }
            }
        };
        /*
         * Use the Exporter class to get a votable url to point to. 
         */
        var exporterOptions = {
                filename: me.sendOptions.filename + ".xml",
                filetype: "votable",
                filtercolumns: true,
                data: me.sendOptions.data,
                attachement: false,
                responseFunction: sendTable};
        
        Mvp.util.Exporter.activate(exporterOptions);
    },
    
    sendTableByUrl: function (url){
        this.sendOptions = url;
        this.connect(this.sendTableByUrlCallback);
    },
    
    sendTableByUrlCallback: function (){
        var tableMessage = new samp.Message("table.load.votable", {url: this.sendOptions});
        var ids = this.connector.connection.notifyAll([tableMessage], null, null);        
    },
    
    sendImage: function(url){
        this.sendOptions = url;
        this.connect(this.sendImageCallback);        
    },
    
    sendImageCallback: function (){
        var imageMessage = new samp.Message("image.load.fits", {url: this.sendOptions});
        var ids = this.connector.connection.notifyAll([imageMessage], null, null);        
    }
});