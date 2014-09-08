var ASTROVIEW = ASTROVIEW || {};
/////////////////////////////
// RaDecView
/////////////////////////////
ASTROVIEW.RaDecView = function ( av )
{ 
    this.av = av;
     // Create div element if not specified   
    this.div = null;
    this.radecIcon = null;
    this.divLabel = null;
    this.divLive = null;
    this.divCross = null;
    this.format = "sexagesimal";
    
    this.coord = new ASTROVIEW.Coord();
    this.createRaDecView();
};

ASTROVIEW.RaDecView.prototype = new Object();
ASTROVIEW.RaDecView.prototype.constructor = ASTROVIEW.RaDecView;

/////////////////////////////
// createRaDecView()
/////////////////////////////
ASTROVIEW.RaDecView.prototype.createRaDecView = function()
{    
    this.div = document.createElement('div');
    this.div.id = 'divRaDec';
    this.div.style.right = '4px';
    this.div.style.position = 'absolute';
    this.div.style.top = '0px';
    this.div.style.textAlign = 'right';
    this.div.style.fontFamily = 'Verdana'; 
    this.div.style.fontSize = '10pt'; 
    this.div.style.fontWight = 'bold';

    if (!ASTROVIEW.MOBILE)
    {
        this.divLive = document.createElement('div');
        this.divLive.id = 'divLive'; 
        this.divLive.style.position = 'absolute';
        this.divLive.style.right = '84px';
        this.divLive.style.top = '4px';
        this.divLive.style.width = '300px'; 
        this.divLive.style.color = 'rgb(248, 176, 72)'; 
        this.divLive.innerHTML = " 0.0 , 0.0 ";
        this.div.appendChild(this.divLive);
    }

    this.divCross = document.createElement('div');
    this.divCross.id = 'divCross'; 
    this.divCross.style.position = 'absolute';
    this.divCross.style.top = (ASTROVIEW.MOBILE ? '4px' : '20px');
    this.divCross.style.right = '84px';
    this.divCross.style.width = '300px';
    this.divCross.style.color = 'rgb(200, 50, 200)'; 
    this.divCross.innerHTML = " 0.0 , 0.0";
    this.div.appendChild(this.divCross); 
         
    // Create Settings Icon 
    this.radecIcon = new Image();
    this.radecIcon.id = 'imgRaDec';
    this.radecIcon.src = '../../Clients/AstroView/radec.png';
    this.radecIcon.style.position = 'absolute';
    this.radecIcon.style.top = '4px';
    this.radecIcon.style.right = '4px';
    this.radecIcon.addEventListener( 'click',  ASTROVIEW.bind(this, this.onMouseDown),  false );
    this.radecIcon.title = "Click to change [RA,DEC] format to sexagesimal or degrees";
    this.div.appendChild( this.radecIcon ); 
    
    return this.div;
}

/////////////////////////
// Mouse Events
/////////////////////////
ASTROVIEW.RaDecView.prototype.onMouseDown = function(event)
{
    event.preventDefault();
    this.format = (this.format == "degrees" ? "sexagesimal" : "degrees");
    this.updateLive(this.coord);      // Update Live Div
    this.updateCross(this.coord);     // Update Cross Div
    this.av.render("CROSS");
}

/////////////////////////////
// update()
/////////////////////////////
ASTROVIEW.RaDecView.prototype.update = function(coord)
{
    this.updateLive(coord);     // Update Live Div
    this.updateCross(coord);    // Update Cross Div
    this.coord.copy(coord);     // Save the Coord for later use in case the 'format' is switched (see onMouseDown)
}

/////////////////////////////
// udpateCross()
/////////////////////////////
ASTROVIEW.RaDecView.prototype.updateCross = function(coord)
{
    if (this.divCross)
    {
        this.divCross.innerHTML = coord.radecToString(this.format);
    }
}

/////////////////////////////
// udpateLive()
/////////////////////////////
ASTROVIEW.RaDecView.prototype.updateLive = function(coord)
{
    if (this.divLive)
    {
        this.divLive.innerHTML = coord.radecToString(this.format);
    }
}