/////////////////////////
// DiamondMesh
/////////////////////////
ASTROVIEW.DiamondMesh = function( geometry, material )
{
    if (!material)
    {
        material = ASTROVIEW.DefaultMaterial;
    }

	THREE.Mesh.call( this, geometry, material );
    
    // Inherited Properties
    this.name = geometry.tid;
    this.flipSided = true;
    this.doubleSided = false;
    
    // Additional Properties
    this.baseurl = "";      // Base URL Template that must be encoded to become imageurl
    this.imageurl = "";     // Actual URL to load remote image
    this.defaulturl = ASTROVIEW.DefaultTextureUrl;
	
	// Image Texture Properties
	this.texture = null;
};

ASTROVIEW.DiamondMesh.prototype = new THREE.Mesh();
ASTROVIEW.DiamondMesh.prototype.constructor = ASTROVIEW.DiamondMesh;

/////////////////////////
// Constants
/////////////////////////
ASTROVIEW.DefaultMaterialUrl = "../AstroView/textures/Diamond.png";
ASTROVIEW.DefaultMaterial = new THREE.MeshBasicMaterial( { map: THREE.ImageUtils.loadTexture(ASTROVIEW.DefaultMaterialUrl) } );

/////////////////////////
// render()
/////////////////////////
ASTROVIEW.DiamondMesh.prototype.render = function( av )
{   
	var frustum = av.controller.frustum;
	var vizlevel = av.controller.getVisibleZoom();
		
	// Update our Texture Visibility, based on Active Zoom Level
	this.setVisible(vizlevel === this.geometry.zlevel);
		
	// Check if we are in the Viewing Frustum
    if (this.inFrustum(frustum))
    {	
        if (vizlevel === this.geometry.zlevel)
        {	
			// CASE I: Inside the Frustum, my level is visible, Load my image, Remove any Children (for Zoom Out)
					
			// If survey url has changed, load new image
			if (av.baseurl !== this.baseurl)
			{
				var url = new String(av.baseurl);
				var dg = this.geometry; // DiamondGeometry	
				this.imageurl = url.replace("[LEVEL]", dg.zlevel).replace("[TX]", dg.tx).replace("[TY]", dg.ty).replace("[LEVEL]", dg.zlevel).replace("[TX]", dg.tx).replace("[TY]", dg.ty);      
				this.cleanTexture(av);
				this.loadTexture(this.imageurl);
				this.baseurl = av.baseurl;
			}
		
			// Remove all Children, when View State is IDLE
			if (av.viewState === ASTROVIEW.VIEW_STATE_IDLE)
			{
				if (this.children.length > 0)
				{
					this.removeDiamondChildren(av);
				}
			}
        }
        else if (vizlevel > this.geometry.zlevel)
		{
			// CASE II: In the Frustum, Zoom In Occured, Expand my Children, Render Children
	
			// Expand Children (If necessary)
			if (this.children.length == 0)
			{
				this.expandDiamondChildren();
			}
						
			// Render Children
			if (this.children.length > 0)
			{
				this.renderDiamondChildren(av);
			}
		}
    }
	else	
	{
		// CASE III: Not in the Viewing Frustum,
		// Remove all Children, when View State is IDLE
		if (av.viewState === ASTROVIEW.VIEW_STATE_IDLE)
		{
			if (this.children.length > 0)
			{
				this.removeDiamondChildren(av);
			}
		}
	}
};

ASTROVIEW.DiamondMesh.prototype.loadTexture = function(url)
{
	// Create Texture from Image
    this.texture = new THREE.Texture( new Image() );

    this.texture.image.onload = bind(this, this.onLoad);
    this.texture.image.onerror = bind(this, this.onError);
    
    this.texture.image.crossOrigin = '';
    this.texture.image.src = url;
}

ASTROVIEW.DiamondMesh.prototype.onLoad = function(event)
{
    console.debug(this.name + " onLoad() url: " + this.texture.image.src);
    this.texture.needsUpdate = true;
	
	// Create Material from Texture
    this.material = new THREE.MeshBasicMaterial( { map: this.texture } );
}

ASTROVIEW.DiamondMesh.prototype.setVisible = function(visible)
{
	if (this.material) this.material.opacity = (visible ? 1.0 : 0.0);
}

ASTROVIEW.DiamondMesh.prototype.onError = function(event)
{
    console.debug(this.name + " *** onError() url: " + this.texture.image.src);
    this.loadTexture(this.defaulturl);
}

ASTROVIEW.DiamondMesh.prototype.updateOpacity = function()
{
    if (this.material)
    {
        this.material.opacity = this.opacity;

        /*** Gradual Opacity ***
        this.material.opacity += (this.opacity - this.material.opacity) * 0.5;
        if (Math.abs(this.material.opacity - this.opacity) < 0.1)
        {
            this.material.opacity = this.opacity;
        }
        */
    }
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

ASTROVIEW.DiamondMesh.prototype.renderDiamondChildren = function( av )
{
    for (var i=0; i<this.children.length; i++)
	{
		var dm = this.children[i];
		dm.render(av);
	}
};

ASTROVIEW.DiamondMesh.prototype.removeDiamondChildren = function( av )
{
	var names = "";
    while (this.children.length > 0)
	{
		var dm = this.children[0];
		if (dm.children.length > 0) dm.removeDiamondChildren(av);
		names += dm.name + " ";
		dm.clean(av);
		this.remove(dm);	// NOTE: Decrements this.children.length
	}
	this.children = [];
	console.debug(this.name + " Removed Children Diamonds : " + names);
};

ASTROVIEW.DiamondMesh.prototype.clean = function( av )
{
	// Remove the Texture, Image and Material
	this.cleanTexture(av);
	
	// Remove geometry and mesh from WebGL Graphics Memory 
	if (av.renderer && av.renderer instanceof THREE.WebGLRenderer)
	{
		if (this.geometry) av.renderer.deallocateObject(this.geometry);		
		av.renderer.deallocateObject(this);
	}
	
	// Remove all Object References
	if (this.geometry)
	{
		this.geometry.clean();
		this.geometry = undefined;
	}
}

ASTROVIEW.DiamondMesh.prototype.cleanTexture = function( av )
{		
	// Texture is created from Image
	if (this.texture)
	{
		// Remove texture from WebGL Graphics Memory 
		if (av.renderer && av.renderer instanceof THREE.WebGLRenderer)
		{
			av.renderer.deallocateTexture(this.texture);
		}
		
		if (this.texture.image)
		{
			if (this.texture.image.onload) this.texture.image.onload = undefined;
			if (this.texture.image.onerror) this.texture.image.onerror = undefined;
			this.texture.image = undefined;
		}
		this.texture = undefined;
	}
}

ASTROVIEW.DiamondMesh.prototype.inFrustum = function( frustum )
{
    return this.geometry.inFrustum(frustum);
};
