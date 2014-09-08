ASTROVIEW = ASTROVIEW || {};
/////////////////////////
// Timer
/////////////////////////
ASTROVIEW.Timer = function ( )
{
    this.ticks = undefined;      
    this.timerID = undefined;
};

ASTROVIEW.Timer.prototype = new Object();
ASTROVIEW.Timer.prototype.constructor = ASTROVIEW.Timer;

ASTROVIEW.Timer.prototype.start = function (callback, ticks)
{    
    this.stop();
    this.timerID = window.setInterval(callback, ticks);
    this.ticks = ticks;
};

ASTROVIEW.Timer.prototype.stop = function ()
{    
    if (this.timerID) window.clearTimeout(this.timerID); 
    this.timerID = undefined;
    this.ticks = undefined;  
};

ASTROVIEW.Timer.prototype.isActive = function()
{
    return (this.timerID !== undefined);
};