/***********************************************
* com.bydust.array - (c) byDust code library (www.bydust.com)
* Extended Javascript array class.
* This notice MUST stay intact for legal use
* Author: Nick Van der Vreken
* Version: 1.1
* use:

<script type="text/javascript" src="js/com.bydust.array.js"></script>

standard array functions:
	- array.concat(another_array, ...);
	- array.join("separator");
	- array.pop();
	- array.push(obj, ...);
	- array.reverse();
	- array.shift();
	- array.slice(start[, end]);
	- array.sort();
	- array.splice(index, howmany[, elementstoadd]);
	- array.toSource();
	- array.toString();
	- array.unshift(elements);
	- array.valueOf();
		
functions in com.bydust.array 1.2:
	- array.contains(obj);
		Returns true if the array contains the given value, 
		false if it doesn't.
	- array.count();
		Counts the number of keys in the array.
	- array.countValues();
		Returns an array using the values of the  
		array as keys and their frequency in input as values. 
	- array.dump();
		Returns a string containing a table with each key 
		and its respective value in HTML-code.
	- array.fillKeys(keys, value);
		Fills an array with the value of the value parameter, 
		using the values of the keys array as keys. 
	- array.get(key);
		Returns the value attached to the specified key.
	- array.getKeys();
		Returns an array containing all the keys.
	- array.indexOfKey(key);
		Returns the index of the specified key in the array.
		Returns -1 if the key doesn't exist.
	- array.indexOf(obj);
		Returns the index of the specified value in the array.
		Returns -1 if the value doesn't exist.
	- array.keyOnIndex(index);
		Returns the key on the specified index.
	- array.makeUnique();
		Removes duplicate values from an array.
	- array.product();
		Calculate the product of values in an array.
	- array.shuffle([preserve_keys]);
		Shuffle the array. Optional parameter "preserve_keys", 
		default false.
	- array.set(key, value);
		Add a key with a value.
	- array.sum();
		Calculate the sum of values in an array.
	- array.swap(key1,key2);
		Swaps two elements in an array. Keys are not preserved.
	- array.rand();
		Pick a random entry out of an array.
	- array.remove(key);
		Removes the specified key and its value from the array.

***********************************************/

Array.prototype.set = function(key,value){
	this[key] = value;
}

Array.prototype.get = function(key){
	if(this.getKeys().contains(key)) return this[key];
	return null;
}

Array.prototype.count = function(){
	var s = 0;
	for(var i in this) if(!this.isFunction(i)) s++;
	return s;
}

Array.prototype.countValues = function(){
	var output = new Array();
	for(var i in this){
		if(output.indexOfKey(this.get(i)) == -1) output.set(this.get(i), 1);
		else output.set(this.get(i), output.get(this.get(i)) + 1);
	}
	return output;
}

Array.prototype.fillKeys = function(keys, value){
	for(var i in keys) this.set(keys.get(i),value);
}

Array.prototype.getKeys = function(){
	var keys = '';
	for(var i in this) keys += ',' + i;
	return keys.substr(1).split(',');
}

Array.prototype.indexOfKey = function(key){
	if(this.isFunction(key)) return -1;
	var index = 0;
	for(var i in this){
		if(i == key) return index;
		index++;
	}
	return -1;
}

Array.prototype.isFunction = function(i){
	if(!(typeof i == 'function' || typeof this.get(i) == 'function' || (typeof i == 'string' && i.substr(0,8) == 'function') )) return false;
	else return true
}

Array.prototype.keyOnIndex = function (index){
	var i = 0;
	for(var k in this){
		if(i == index) return k;
		i++;
	}
	return null;
}

Array.prototype.makeUnique = function(){
	var output = new Array();
	for(var i in this){
		if(!output.contains(this.get(i)) && !this.isFunction(i)) output.push(this.get(i));
		else if(!this.isFunction(i)) this.remove(i);
	}
}

Array.prototype.product = function(){
	var output = 1;
	for(var i in this) if(this.get(i)*1 == this.get(i)) output *= this.get(i);
	return output;
}

Array.prototype.rand = function(){
	var i = Math.round(Math.random()*(this.count() - 1));
	return this.get(this.keyOnIndex(i));
}

Array.prototype.remove = function(key){
	if(this.isFunction(key)) return;
	if(1*key != key) delete this[key];
	else{
		var i = this.indexOfKey(key);
		this.splice(i,1);
	}
}

Array.prototype.shuffle = function(preserve_keys){
	if(!preserve_keys) preserve_keys = false;
	if(preserve_keys){
		var shuffled = new Array();
		while(shuffled.count() < this.count()){
			var i = Math.round(Math.random()*(this.count()-1));
			var key = this.keyOnIndex(i);
			if(shuffled.indexOfKey(key) == -1) shuffled.set(key,this.get(key));
		}
		while(this.count() > 0) this.remove(this.keyOnIndex(0));
		for(var i in shuffled) if(!this.isFunction(i)) this.set(i,shuffled.get(i));
	}else{
		for(var i = 0; i < this.count()*3; i++){
			var key1 = this.keyOnIndex(Math.round(Math.random()*(this.count()-1)));
			var key2 = this.keyOnIndex(Math.round(Math.random()*(this.count()-1)));
			this.swap(key1, key2);
		}
	}
}

Array.prototype.sum = function(){
	var output = 0;
	for(var i in this) if(this.get(i)*1 == this.get(i)) output += this.get(i);
	return output;
}

Array.prototype.swap = function(key1,key2){
	if(this.indexOfKey(key1) == -1 || this.indexOfKey(key2) == -1) return;
	var t = this.get(key1);
	this.set(key1,this.get(key2));
	this.set(key2,t);
}

Array.prototype.contains = function(obj){
	return (this.indexOf(obj) == -1)?false:true;
}

// fixing Array.indexOf for the fucking IE engine..
if(!Array.indexOf){
	Array.prototype.indexOf = function(obj){
		for(var i=0; i<this.length; i++){
			if(this[i]==obj) return i;
		}
		return -1;
	}
}

Array.prototype.dump = function(){
	var output = new Array();
	var number = 0;
	output.push('<tr><th>#</th><th>Key</th><th>Value</th></tr>');
	for(var i in this){
		if(!this.isFunction(i)){
			output.push('<tr><td>' + number + '</td><td>' + i + '</td><td>' + this.get(i) + '</td></tr>');
			number++;
		}
	}
	return '<table>' + String.fromCharCode(10) + output.join(String.fromCharCode(10)) + String.fromCharCode(10) + '</table>';
}