/////////////////////////////
// CatalogLayer()
/////////////////////////////
ASTROVIEW.CatalogLayer = function ( av, layerData )
{ 
    ASTROVIEW.GraphicsLayer.call( this, av, layerData );
    
    if (layerData && layerData.rows && layerData.rows.length > 0)
    {
		this.createParticles(layerData.rows);
	}
	
	// NOTE: 
    // If any Particles were created (for WebGL Only),
    // we need to add them to the scene as a Particle System, 
    if (this.hasParticleSystem())
    {
        this.createParticleSystem();
    }
};

ASTROVIEW.CatalogLayer.prototype = new ASTROVIEW.GraphicsLayer();
ASTROVIEW.CatalogLayer.prototype.constructor = ASTROVIEW.CatalogLayer;

/////////////////////////////
// createParticles()
/////////////////////////////
ASTROVIEW.CatalogLayer.prototype.createParticles = function(rows)
{
	//
	// NOTE: The Particle API is different for WebGLRenderer vs. CanvasRenderer
	//       so we separate the code accordingly.
	//
	if (rows && rows.length > 0)
	{
		for (var i=0; i<rows.length; i++)
		{
			// Check all mixed case variations for RA and DEC values
			var row = rows[i];
			var ra = row.ra != null ? row.ra : (row.RA != null ? row.RA : (row.Ra != null ? row.Ra : null));
			var dec = row.dec != null ? row.dec : (row.DEC != null ? row.DEC : (row.Dec != null ? row.Dec : null));
			
			// Add the particle at specified location
			this.createParticle(ra, dec, row);
		}
	}
}