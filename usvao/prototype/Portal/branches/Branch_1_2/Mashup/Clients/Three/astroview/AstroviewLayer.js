/////////////////////////////
// AstroviewLayer()
/////////////////////////////
ASTROVIEW.AstroviewLayer = function ( layer )
{
    THREE.Object3D.call( this );
    if (layer)
    {
        //
        // NOTE: We must ensure the Layer ID matches everwhere: 
        // layer: The object contains that properties of a layer.
        // AstroviewLayer: The actual 3D Object added to the Scene.
        // Object3D.name: main key for extracting Objects from the Scene using scene.getObjectByName()
        //
        this.layer = layer;
        this.lid = ASTROVIEW.lid++;
        this.layer.lid = this.lid;
        this.name = this.lid;
        
        ASTROVIEW.lid++;
    }
};

ASTROVIEW.AstroviewLayer.prototype = new THREE.Object3D();
ASTROVIEW.AstroviewLayer.prototype.constructor = ASTROVIEW.AstroviewLayer;
		
/////////////////////////////
// raDecToVertex()
/////////////////////////////
ASTROVIEW.AstroviewLayer.prototype.raDecToVertex = function(ra, dec, radius)
{
    var decRadians = dec*TO_RADIANS;
    var raRadians = ra*TO_RADIANS;
    var r = Math.cos(decRadians)*radius;
    
    var y = Math.sin(decRadians)*radius;
    var x = -Math.sin(raRadians)*r;
    var z = -Math.cos(raRadians)*r;

    var v = new THREE.Vertex(new THREE.Vector3(x, y, z));
    return v;
};

ASTROVIEW.lid = 0;