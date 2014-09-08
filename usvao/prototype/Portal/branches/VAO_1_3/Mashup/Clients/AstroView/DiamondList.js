		 
//////////////////////////
// DiamondList()
//////////////////////////
ASTROVIEW.DiamondList = function(size)
{
    THREE.Object.call( this );
	if (!size) size = 50;
	this.size = size;
	this.count = 0;
};

ASTROVIEW.DiamondList.prototype = new Object();
ASTROVIEW.DiamondList.prototype.constructor = ASTROVIEW.DiamondList;

////////////////////////
// pop()
////////////////////////
ASTROVIEW.DiamondList.prototype.pop = function( )
{
	var key;
    for (key in this)
    {
        if (this.hasOwnProperty(key))
        {
            var value = this[key];
            delete this[key];
			this.count--;
            return value;
        }
    }
    return null;
};

////////////////////////
// remove()
////////////////////////
ASTROVIEW.DiamondList.prototype.remove = function(key)
{
    if (this.hasOwnProperty(key))
    {
        var value = this[key];
		delete this[key];
		this.count--;
		return value;
    }
    return null;
};

////////////////////////
// put()
////////////////////////
function put(key, value)
{
    this[key] = value;
	this.count++;
	if (this.count > this.size)
	{
		this.pop();
	}
};

////////////////////////
// count()
////////////////////////
function count()
{
    var key, count=0;;
    for (key in freelist)
    {
        if (freelist.hasOwnProperty(key))
        {
            count++;
        }
    }
    return count;
};

