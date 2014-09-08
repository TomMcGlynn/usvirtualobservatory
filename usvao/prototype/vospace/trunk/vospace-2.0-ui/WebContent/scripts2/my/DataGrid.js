define(["dojox/grid/EnhancedGrid", "dojo/_base/declare", "dojo/_base/array", "dojo/_base/lang"], function(EnhancedGrid, declare, array, lang) {
    declare("my.DataGrid", [dojox.grid.EnhancedGrid], {
    	
    	_currentPath : '',
    	pathWidget : null,
    	
    	_fetch: function(start, isRender){
    		start = start || 0;
    		if(this.store && !this._pending_requests[start]){
    			if(!this._isLoaded && !this._isLoading){
    				this._isLoading = true;
    				this.showMessage(this.loadingMessage);
    			}
    			this._pending_requests[start] = true;
    			try{
    				if(this.items){
    					var items = this.items;
    					var store = this.store;
    					this.rowsPerPage = items.length;
    					var req = {
    						start: start,
    						count: this.rowsPerPage,
    						isRender: isRender
    					};
    					this._onFetchBegin(items.length, req);
    					
    					// Load them if we need to
    					var waitCount = 0;
    					array.forEach(items, function(i){
    						if(!store.isItemLoaded(i)){ waitCount++; }
    					});
    					if(waitCount === 0){
    						this._onFetchComplete(items, req);
    					}else{
    						var onItem = function(item){
    							waitCount--;
    							if(waitCount === 0){
    								this._onFetchComplete(items, req);
    							}
    						};
    						array.forEach(items, function(i){
    							if(!store.isItemLoaded(i)){
    								store.loadItem({item: i, onItem: onItem, scope: this});
    							}
    						}, this);
    					}
    				}else{
    					this.store.fetch({
    						start: start,
    						count: this.rowsPerPage,
    						query: this.query,
    						sort: this.getSortProps(),
    						path: this._currentPath,
    						queryOptions: this.queryOptions,
    						isRender: isRender,
    						onBegin: lang.hitch(this, "_onFetchBegin"),
    						onComplete: lang.hitch(this, "_onFetchComplete"),
    						onError: lang.hitch(this, "_onFetchError")
    					});
    				}
    			}catch(e){
        			console.error(e);
    				this._onFetchError(e, {start: start, count: this.rowsPerPage});
    			}
    		}
    	},

    	setCurrentPath: function(path) {
    		this._setCurrentPath(path);
    		this.plugin('selector').clear();
    		this._refresh(true);
    	},
    	
    	_setCurrentPath: function(path) {
    		this._currentPath = path;
    		if(null != this.pathWidget) {
    			this.pathWidget.options[0].label ='Location: '+path;
    			
    			while(this.pathWidget.options.length > 1){
    				this.pathWidget.removeOption(this.pathWidget.options.length-1);
    			}
    		
    			if(path != "/") {
    				var pathTokens = path.split('/');
    				this.pathWidget.addOption({});
    				for(var i = pathTokens.length-1; i > 1 ; i--) {
    					var curPath = pathTokens.slice(0,i).join("/");
    					this.pathWidget.addOption({value:curPath , label:curPath });
    				}
    				this.pathWidget.addOption({value:'/' , label:'/'});
    			}

    		}
    	},
    	
    	setStore: function(store) {
    		var oldStore = this.store;
    		this._setCurrentPath("/");
    		this.inherited("setStore", arguments);
    		if(null != oldStore){
    			oldStore.close();
    		}
    	}
    	
	});

    return my.DataGrid;
});