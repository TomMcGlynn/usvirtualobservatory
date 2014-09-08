var ASTROVIEW = ASTROVIEW || {};
/////////////////////////////
// RaDecView
/////////////////////////////
ASTROVIEW.RaDecView = function ( mobile )
{ 
     // Create div element if not specified   
    this.div = null;
    this.divLabel = null;
    this.divLive = null;
    this.divCross = null;
    this.format = "sexagesimal";
    
    this.coord = {
        "ra": 0.0, "dec": 0.0,
        "sra": "00:00:00", "sdec": "00:00:00",
        "dra": "0.0", "ddec": "0.0"
    }
      
    this.createRaDecView(mobile);
};

ASTROVIEW.RaDecView.prototype = new Object();
ASTROVIEW.RaDecView.prototype.constructor = ASTROVIEW.RaDecView;

/////////////////////////////
// createRaDecView()
/////////////////////////////
ASTROVIEW.RaDecView.prototype.createRaDecView = function(mobile)
{    
    this.div = document.createElement('div');
    this.div.id = 'divRaDec';
    this.div.style.left = '10px';
    this.div.style.position = 'absolute';
    this.div.style.top = '4px';
    this.div.style.width = '220';
    this.div.style.zIndex = 1000;

    this.divLabel = document.createElement('div');
    this.divLabel.id = 'divLabel'; 
    this.divLabel.style.position = 'absolute';
    this.divLabel.style.top = '2px';
    this.divLabel.style.left = '0px';
    this.divLabel.style.width = '80px';
    this.divLabel.style["font-family"] = 'Arial,Helvetica,sans-serif'; 
    this.divLabel.style["font-size"] = '12px'; 
    this.divLabel.style["color"] = (mobile ? 'rgb(200, 50, 200)' : 'rgb(248, 176, 72)'); 
    this.divLabel.style["font-weight"] = 'bold';
    this.divLabel.innerHTML = "[RA, DEC] ";
    this.divLabel.title = "Click to change [RA,DEC] format to sexagesimal or degrees";
    this.div.appendChild(this.divLabel);
    this.divLabel.addEventListener( 'mousedown', bind(this, this.onMouseDown), false );

    if (!mobile)
    {
        this.divLive = document.createElement('div');
        this.divLive.id = 'divLive'; 
        this.divLive.style.position = 'absolute';
        this.divLive.style.left = '72px';
        this.divLive.style.top = '2px';
        this.divLive.style.width = '240px';
        this.divLive.style["font-family"] = 'Arial,Helvetica,sans-serif'; 
        this.divLive.style["font-size"] = '12px'; 
        this.divLive.style["color"] = 'rgb(248, 176, 72)'; 
        this.divLive.style["font-weight"] = 'bold';
        this.divLive.innerHTML = " 0.0 , 0.0 ";
        this.div.appendChild(this.divLive);
    }

    this.divCross = document.createElement('div');
    this.divCross.id = 'divCross'; 
    this.divCross.style.position = 'absolute';
    this.divCross.style.top = (mobile ? '2px' : '18px');
    this.divCross.style.left = '72px';
    this.divCross.style.width = '240px';
    this.divCross.style["font-family"] = 'Arial,Helvetica,sans-serif'; 
    this.divCross.style["font-size"] = '12px'; 
    this.divCross.style["color"] = 'rgb(200, 50, 200)';
    this.divCross.style["font-weight"] = 'bold';
    this.divCross.innerHTML = " 0.0 , 0.0";
    this.div.appendChild(this.divCross);  
    
    return this.div;
}

/////////////////////////
// Mouse Events
/////////////////////////
ASTROVIEW.RaDecView.prototype.onMouseDown = function(event)
{
    event.preventDefault();
    this.format = (this.format == "degrees" ? "sexagesimal" : "degrees");
    this.update(this.coord);
}

/////////////////////////////
// update()
/////////////////////////////
ASTROVIEW.RaDecView.prototype.update = function(coord)
{
    this.updateLive(coord);
    this.updateCross(coord);
    this.copyCoord(coord);
}

/////////////////////////////
// copyCoord()
/////////////////////////////
ASTROVIEW.RaDecView.prototype.copyCoord = function(coord)
{
    this.coord.ra = coord.ra;
    this.coord.dec = coord.dec;
    this.coord.sra = coord.sra;
    this.coord.sdec = coord.sdec;
    this.coord.dra = coord.dra;
    this.coord.ddec = coord.ddec;
}

/////////////////////////////
// udpateCross()
/////////////////////////////
ASTROVIEW.RaDecView.prototype.updateCross = function(coord)
{
    if (this.divCross)
    {
        if (this.format == "sexagesimal")
            this.divCross.innerHTML = " " + coord.sra + "  " + coord.sdec + " ";
        else if (this.format == "degrees")
            this.divCross.innerHTML = " " + coord.dra + "  " + coord.ddec + " ";
    }
}

/////////////////////////////
// udpateLive()
/////////////////////////////
ASTROVIEW.RaDecView.prototype.updateLive = function(coord)
{
    if (this.divLive)
    {
        if (this.format == "sexagesimal")
            this.divLive.innerHTML = " " + coord.sra + "  " + coord.sdec + " ";
        else if (this.format == "degrees")
            this.divLive.innerHTML = " " + coord.dra + "  " + coord.ddec + " ";
    }
}