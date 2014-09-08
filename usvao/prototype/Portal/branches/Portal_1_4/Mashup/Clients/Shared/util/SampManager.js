Ext.define('Mvp.util.SampManager', {

    singleton: true,
        
    config: {
        connector: null,
        sendOptions: null
    },
    
    constructor: function(config) {
        this.initConfig(config);
        this.connector = this.applyConnector();

        return this;
    },
    
    applyConnector: function () {
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
        subs = {"samp.app.ping": {}};
        var connector = new samp.Connector("Portal", meta, logCc, subs);
            
        connector.onunreg = function() {
            Ext.log("SAMP: connection closed");
        };
                        
        return connector;
    },
    
    connect: function(callback, mtype){
        var me = this;
        this.callback = callback;
        this.mtype = mtype;

        var regErrHandler = function(err) {
            Ext.log("SAMP: connection error: " + err.toString() );
        };
                
        var regSuccessHandler = function(conn) {
            me.connector.setConnection(conn);
            Ext.log("SAMP: connection established: " + (conn ? "Yes" : "No"));
            me.checkForClient();
        };

        var connection = me.connector.connection;
        if( connection ){
            me.checkForClient();
        }else{
            Ext.log("SAMP: No connection, try and register");
            samp.register(me.connector.name, regSuccessHandler, regErrHandler);
        }
        
    },
    
    checkForClient: function(){
        var me = this;
        
        var checkErrHandler = function(err) {
            Ext.log("SAMP: error getting subscribed clients: " + err.toString() );
        };
                
        var checkSuccessHandler = function(clients) {
            var foundClients = false;
            for(client in clients){
                foundClients = true;
                Ext.log("SAMP: clients found accepting mtype " + me.mtype);
                me.callback();
                break;
            }
            
            if(!foundClients){
                Ext.log("SAMP: No clients found accepting mtype " + me.mtype);
            }
        };

        this.connector.connection.getSubscribedClients([this.mtype], checkSuccessHandler, checkErrHandler);
    },
    
    sendTable: function (options){
        this.sendOptions = options;
        this.connect(this.sendTableCallback, "table.load.votable");
    },
    
    sendTableCallback: function(){
        var me = this;
        var data = null;
        
        var sendTable = function(responseObject, requestOptions, queryScope, complete, updated) {
            if (complete) {
                // Get the URL and start the download.
                this.complete = complete;
                data = responseObject.data;
                var url = data.url;
                url = url.replace(/\n/m, "");
                var connection = me.connector.connection;
                if (data) {
                    var tableMessage = new samp.Message("table.load.votable", {url: url, name: me.sendOptions.filename});
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
        url = url.replace(/\n/mg, "");
        this.sendOptions = url;
        this.connect(this.sendTableByUrlCallback, "table.load.votable");
    },
    
    sendTableByUrlCallback: function (){
        var tableMessage = new samp.Message("table.load.votable", {url: this.sendOptions, name: this.sendOptions.filename});
        var ids = this.connector.connection.notifyAll([tableMessage], null, null);        
    },
    
    sendImage: function(url, name){
        url = url.replace(/\n/mg, "");
        this.sendOptions = {url: url, name: name};
        this.connect(this.sendImageCallback, "image.load.fits");        
    },
    
    sendImageCallback: function (){
        var imageMessage = new samp.Message("image.load.fits", {url: this.sendOptions.url, name: this.sendOptions.name});
        var ids = this.connector.connection.notifyAll([imageMessage], null, null);        
    },
    
    sendSpectra: function(url, name, metaData){
        url = url.replace(/\n/mg, "");
        this.sendOptions = {url: url, name: name, meta: metaData};
        this.connect(this.sendSpectraCallback, "spectrum.load.ssa-generic");        
    },
    
    sendSpectraCallback: function (){
        var message = new samp.Message("spectrum.load.ssa-generic", 
                {url: this.sendOptions.url, name: this.sendOptions.name, meta: this.sendOptions.meta});
        var ids = this.connector.connection.notifyAll([message], null, null);        
    }
});