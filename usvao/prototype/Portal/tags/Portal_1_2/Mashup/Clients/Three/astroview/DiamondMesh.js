   
ASTROVIEW.DiamondMesh = function( geometry, material )
{
    if (!material)
    {
        material = new THREE.MeshBasicMaterial( { map: THREE.ImageUtils.loadTexture("textures/Diamond.png") } );
    }

	THREE.Mesh.call( this, geometry, material );
    
    // Inherited Properties
    this.name = "DiamondMesh:" + geometry.tid;
    this.flipSided = true;
    this.doubleSided = false;
    
    // Additional Properties
    this.baseurl = "";      // Base URL Template that must be encoded to become imageurl
    this.imageurl = "";     // Actual URL to load remote image
    this.defaulturl = "textures/Diamond.png";
    
    // Initial opacity of texture
    this.opacity = 1.0;
};

ASTROVIEW.DiamondMesh.prototype = new THREE.Mesh();
ASTROVIEW.DiamondMesh.prototype.constructor = ASTROVIEW.DiamondMesh;

ASTROVIEW.DiamondMesh.prototype.render = function( baseurl, zlevel, frustum )
{
    // If we not in the frustum, do nothing
    if (this.inFrustum(frustum))
    {
        // If this is my zoom level, make myself visible and load image
        if (zlevel === this.geometry.zlevel)
        {
            // My level is now visible, start fading in
            this.opacity = 1.0;

            // Don't turn on opacity until image is loaded
            if (this.baseurl !== baseurl)
            {               
                var url = new String(baseurl);
                var dg = this.geometry; // DiamondGeometry
                
                this.imageurl = url.replace("[LEVEL]", dg.zlevel).replace("[TX]", dg.tx).replace("[TY]", dg.ty).replace("[LEVEL]", dg.zlevel).replace("[TX]", dg.tx).replace("[TY]", dg.ty);      
                this.loadTexture(this.imageurl);                
                this.baseurl = baseurl;
            }
        }
        else 
        {
            // My level is no longer visible, start fading out
            this.opacity = 0.0;         
            
            // If current zoom level is greater than me, ensure I have expanded my children 
            if (zlevel > this.geometry.zlevel)
            {
                if (this.children.length == 0)
                {
                    this.expandDiamondChildren();
                }
            }
        }
        
        // Render all my diamond children
        if (this.children.length > 0)
        {
            for (var i=0; i<this.children.length; i++)
            {
                var dm = this.children[i];
                dm.render(baseurl, zlevel, frustum);
            }
        }
        
        this.updateOpacity();
    }
};

ASTROVIEW.DiamondMesh.prototype.loadTexture = function(url)
{
    this.opacity = 0.0; // Loading new image, start fading out old texture
    
    this.image = new Image();
    this.texture = new THREE.Texture( this.image );

    this.image.onload = bind(this, this.onLoad);
    this.image.onerror = bind(this, this.onError);
    
    this.image.crossOrigin = '';
    this.image.src = url;
}

ASTROVIEW.DiamondMesh.prototype.onLoad = function(event)
{
    console.debug(this.name + " onload url: " + this.imageurl);
    this.texture.needsUpdate = true;
    this.material = new THREE.MeshBasicMaterial( { map: this.texture } );
    
    this.opacity = 1.0; // New image is loaded, start fading in new texture
}

ASTROVIEW.DiamondMesh.prototype.updateOpacity = function()
{
    if (this.material)
    {
        this.material.opacity = this.opacity;
        
        //
        // Gradually change Image Opacity
        //
        /*
        this.material.opacity += (this.opacity - this.material.opacity) * 0.5;
        if (Math.abs(this.material.opacity - this.opacity) < 0.1)
        {
            this.material.opacity = this.opacity;
        }
        */
    }
}

ASTROVIEW.DiamondMesh.prototype.onError = function(event)
{
    console.debug(this.name + " *** onerror url: " + this.imageurl);
    this.loadTexture(this.defaulturl);
}

ASTROVIEW.DiamondMesh.prototype.expandDiamondChildren = function()
{
    var dga = this.geometry.expandDiamond();
    for (i=0; i<dga.length; i++)
    {
        var dm = new ASTROVIEW.DiamondMesh( dga[i] );
        this.add(dm);
    }
};

ASTROVIEW.DiamondMesh.prototype.inFrustum = function( frustum )
{
    return this.geometry.inFrustum(frustum);
};
