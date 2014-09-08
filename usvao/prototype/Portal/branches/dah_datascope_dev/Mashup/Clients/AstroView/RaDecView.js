var ASTROVIEW = ASTROVIEW || {};
/////////////////////////////
// RaDecView
/////////////////////////////
ASTROVIEW.RaDecView = function ( div, mobile )
{ 
     // Create div element if not specified
    if (!div)
    {
        div = document.createElement('div');
        document.body.appendChild(div);
    }
    this.div = div;
    
    this.divRaDec = null;
    this.divLabel = null;
    this.divLive = null;
    this.divCross = null;
      
    this.createRaDecView(mobile);
};

ASTROVIEW.RaDecView.prototype = new Object();
ASTROVIEW.RaDecView.prototype.constructor = ASTROVIEW.RaDecView;

/////////////////////////////
// createRaDecView()
/////////////////////////////
ASTROVIEW.RaDecView.prototype.createRaDecView = function(mobile)
{    
    this.divRaDec = document.createElement('div');
    this.divRaDec.id = 'divRaDec';
    this.divRaDec.style.left = '10px';
    this.divRaDec.style.position = 'absolute';
    this.divRaDec.style.top = '4px';
    this.divRaDec.style.width = '220';
    this.divRaDec.style.zIndex = 1000;
    this.div.appendChild(this.divRaDec);
    
    this.divLabel = document.createElement('div');
    this.divLabel.id = 'divLabel'; 
    this.divLabel.style.position = 'absolute';
    this.divLabel.style.top = '0px';
    this.divLabel.style.left = '0px';
    this.divLabel.style.width = '80px';
    this.divLabel.style["font-family"] = 'Arial,Helvetica,sans-serif'; 
    this.divLabel.style["font-size"] = '12px'; 
    this.divLabel.style["color"] = (mobile ? 'rgb(200, 50, 200)' : 'rgb(248, 176, 72)'); 
    this.divLabel.style["font-weight"] = 'bold';
    this.divLabel.innerHTML = "[RA, DEC] ";
    this.divRaDec.appendChild(this.divLabel);
    
    if (!mobile)
    {
        this.divLive = document.createElement('div');
        this.divLive.id = 'divLive'; 
        this.divLive.style.position = 'absolute';
        this.divLive.style.left = '80px';
        this.divLive.style.top = '0px';
        this.divLive.style.width = '240px';
        this.divLive.style["font-family"] = 'Arial,Helvetica,sans-serif'; 
        this.divLive.style["font-size"] = '12px'; 
        this.divLive.style["color"] = 'rgb(248, 176, 72)'; 
        this.divLive.style["font-weight"] = 'bold';
        this.divLive.innerHTML = " 0.0 , 0.0 ";
        this.divRaDec.appendChild(this.divLive);
    }

    this.divCross = document.createElement('div');
    this.divCross.id = 'divCross'; 
    this.divCross.style.position = 'absolute';
    this.divCross.style.top = (mobile ? '0px' : '20px');
    this.divCross.style.left = '80px';
    this.divCross.style.width = '240px';
    this.divCross.style["font-family"] = 'Arial,Helvetica,sans-serif'; 
    this.divCross.style["font-size"] = '12px'; 
    this.divCross.style["color"] = 'rgb(200, 50, 200)';
    this.divCross.style["font-weight"] = 'bold';
    this.divCross.innerHTML = " 0.0 , 0.0";
    this.divRaDec.appendChild(this.divCross);  
}

/////////////////////////////
// update()
/////////////////////////////
ASTROVIEW.RaDecView.prototype.update = function(coord)
{
    this.updateLive(coord);
    this.updateCross(coord);
}

/////////////////////////////
// udpateCross()
/////////////////////////////
ASTROVIEW.RaDecView.prototype.updateCross = function(coord)
{
    if (this.divCross && coord && coord.sra && coord.sdec)
    {
        this.divCross.innerHTML = "  " + coord.sra + "  " + coord.sdec + "  ";
    }
}

/////////////////////////////
// udpateLive()
/////////////////////////////
ASTROVIEW.RaDecView.prototype.updateLive = function(coord)
{
    if (this.divLive && coord && coord.sra && coord.sdec)
    {
        this.divLive.innerHTML = "  " + coord.sra + "  " + coord.sdec + "  ";
    }
}