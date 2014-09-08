Ext.define('Mvp.util.Wcs', {
    requires: ['Mvp.util.TableUtils',
               'Mvp.util.Util'],
    
    //CTYPE1  = 'RA---TAN'
    //CTYPE2  = 'DEC--TAN'
    //CUNIT1  = 'deg     '
    //CUNIT2  = 'deg     '
    //CRPIX1  = 1.02450000000000E+03 / Column Pixel Coordinate of Ref. Pixel
    //CRPIX2  = 7.44500000000000E+02 / Row Pixel Coordinate of Ref. Pixel
    //CRVAL1  = 5.86305204900000E+01 / RA at Reference Pixel
    //CRVAL2  = -3.1363144000000E-01 / DEC at Reference Pixel
    //CD1_1   = 1.92282275464897E-08 / RA  degrees per column pixel (cdMatrix[0])
    //CD1_2   = 1.09997827820690E-04 / RA  degrees per row pixel (cdMatrix[1])
    //CD2_1   = 1.09973486328125E-04 / DEC degrees per column pixel (cdMatrix[2])
    //CD2_2   = -9.5161290322482E-09 / DEC degrees per row pixel (cdMatrix[3])

    // Must specify at a minimum: naxis, cdMatrix, coordRefValue, coordRefPixel,
    // which are the string values for those entities.
    constructor: function (config) {
        
        var tu = Mvp.util.TableUtils;
        var u = Mvp.util.Util;
        
        this.naxis = tu.parseArray(config.naxis);
        this.cdMatrix = tu.parseArray(config.cdMatrix);
        this.coordRefValue = tu.parseArray(config.coordRefValue);
        this.coordRefPixel = tu.parseArray(config.coordRefPixel);
        
        this.valid = true;
        this.valid = this.valid && u.isValidNumericArray(this.naxis, 2);
        this.valid = this.valid && u.isValidNumericArray(this.cdMatrix, 4);
        this.valid = this.valid && u.isValidNumericArray(this.coordRefValue, 2);
        this.valid = this.valid && u.isValidNumericArray(this.coordRefPixel, 2);
        
        if (this.valid) {
            var wcsInput = {
                crpix: this.coordRefPixel,
                crval: this.coordRefValue,
                cdmatrix: this.cdMatrix,
                proj: this.proj  // TBD - where to get this??
            };
            this.setWcs(wcsInput);
            if (!this.mapWCS) {
                this.valid = false;
            } else {
                this.computeCorners();
            }
        }
    },
    
    pixel2RaDec: function(px, py) {
        var ret = null;
        
        if (this.valid) {
            var xdif = px - this.coordRefPixel[0];
            var ydif = py - this.coordRefPixel[1];
            var xsi = this.cdMatrix[0] * xdif + this.cdMatrix[1] * ydif;
            var eta = this.cdMatrix[2] * xdif + this.cdMatrix[3] * ydif;
            var cosDec = Math.cos(this.coordRefValue[1]);
            var a = this.coordRefValue[0] + (xsi / cosDec);
            var d = this.coordRefValue[1] + eta
            ret = {ra: a, dec: d};
        }
        return ret;
    },
    
    computeCorners: function() {
        if (this.valid) {
            this.ll = this.mapWCS(0, 0);
            this.lr = this.mapWCS(this.naxis[0], 0);
            this.ul = this.mapWCS(0, this.naxis[1]);
            this.ur = this.mapWCS(this.naxis[0], this.naxis[1]);
        }
    },
    
    /**
     * This function sets up functions that convert between sky and pixel coordinates.  It takes two parameters:
     * wcs is an object with attributes from the WCS (crpix, crval, cdmatrix, proj, refframe).  This code handles
     *      a lot of different options including different projections, galactic and ecliptic coordinates.
     *      refframe defaults to 'TAN', and proj defaults to 'ICRS'.
     * wcslabel (optional) is just a string that only gets used for a linear projection.  It gets used for things like 2-D spectra,
     *      where one axis in in degrees (or arcsec or something like that) and the other one is in wavelength.  It's used in the fits2web display and may be irrelevant for you.
     *
     * What this does is examine the WCS parameters and add two coordinate mapping methods to the object.
     * This actually adds these methods to 'this', but it would be easy to pull this out and pass in another object to use,
     * so that this would be a kind of decorate that adds the methods to another objects.
     *
     * The methods are:
     *      mapWCS(x,y) maps from pixels (using 1-based FITS pixel coordinates) to sky coordinates (degrees)
     *      mapWCSinv(ra,dec) maps from RA/Dec (or longitude/latitude, degrees) to pixel coordinates X,Y
     *
     * Both of these take two input parameters and return a two-element array with the results.
     *
     * This also assigns a wcsformat parameter that is used by the code that displays positions.  The format may
     * be either "sexagesimal" (h:m:s +d:m:s, wcsformat = -1) or degrees (wcsformat = 5).  I don't remember why 5.
     * I suppose there could be other options.  The default is to use sexagesimal for RA/Dec and degrees for other
     * kinds of coordinates.
     */

    setWcs: function(wcs,wcslabel) {
		this.mapWCS = undefined;
		if (!(wcs.crpix && wcs.crval && wcs.cdmatrix)) {
			// WCS is not defined
			return;
		}
		if (wcs.proj == undefined) wcs.proj = 'TAN';
		if (wcs.refframe == undefined) wcs.refframe = 'ICRS';
		if (wcs.crpix.length != 2 || wcs.crval.length != 2 || wcs.cdmatrix.length != 4) {
			Ext.Msg.alert("WCS error: some parameters have wrong length\n" +
				"crpix " + wcs.crpix.length +
				"crval " + wcs.crval.length +
				"cdmatrix " + wcs.cdmatrix.length);
			return;
		}
		// put all intermediate quantities in local variables
		var crpix0 = wcs.crpix[0];
		var crpix1 = wcs.crpix[1];
		if (wcs.proj == 'Linear') {
			// leave linear coordinates in natural units
			var radeg = 1.0;
		} else {
			// convert units from degrees to radians
			radeg = 180.0/Math.PI;
		}
		var crval0 = wcs.crval[0] / radeg;
		var crval1 = wcs.crval[1] / radeg;
		var cos_crval1 = Math.cos(crval1);
		var sin_crval1 = Math.sin(crval1);
		var cd00 = wcs.cdmatrix[0] / radeg;
		var cd01 = wcs.cdmatrix[1] / radeg;  // TSD Spitzer has this one negated.
		var cd10 = wcs.cdmatrix[2] / radeg;
		var cd11 = wcs.cdmatrix[3] / radeg;
		// inverted CD matrix
		var determ = cd00*cd11-cd01*cd10;
		var cdinv00 =  cd11/determ;
		var cdinv01 = -cd01/determ;
		var cdinv10 = -cd10/determ;
		var cdinv11 =  cd00/determ;

		if (wcs.refframe == 'ECL') {
			this.wcsformat = 5;
			this.wcslabel = 'Ecliptic Long Lat';
		} else if (wcs.refframe == 'GAL') {
			this.wcsformat = 5;
			this.wcslabel = 'Galactic l b';
		} else if (wcs.proj == 'Linear') {
			this.wcsformat = 5;
			this.wcslabel = wcslabel || 'WCS';
		} else {
			this.wcsformat = -1;
			this.wcslabel = wcs.refframe+' RA Dec';
		}
		if (wcs.proj == 'Linear') {
			this.mapWCS = function(x,y) {
				// first pixel is (1,1)
				var xdif = x - crpix0;
				var ydif = y - crpix1;
				var xsi = cd00*xdif + cd01*ydif;
				var eta = cd10*xdif + cd11*ydif;
				return [crval0+xsi, crval1+eta];
			};
			this.mapWCSinv = function(ra,dec) {
				var xsi = ra - crval0;
				var eta = dec - crval1;
				var xdif = cdinv00*xi + cdinv01*eta;
				var ydif = cdinv10*xi + cdinv11*eta;
				return [xdif+crpix0, ydif+crpix1];
			};
		} else if (wcs.proj == 'TAN' || wcs.proj == 'TAN-SIP') {
			this.mapWCS = function(x,y) {
				// first pixel is (1,1)
				var xdif = x - crpix0;
				var ydif = y - crpix1;
				var xsi = cd00*xdif + cd01*ydif;
				var eta = cd10*xdif + cd11*ydif;
				var beta = cos_crval1 - eta*sin_crval1;
				var ra = Math.atan2(xsi, beta) + crval0;
				var gamma = Math.sqrt(xsi*xsi + beta*beta);
				var dec = Math.atan2(eta*cos_crval1+sin_crval1, gamma);
				ra = ra*radeg;
				dec = dec*radeg;
				return [ra, dec];
			};
			this.mapWCSinv = function(ra,dec) {
				var radif = ra/radeg - crval0;
				// guard against zero wraparound
				if (radif > Math.PI) {
					radif = radif - 2*Math.PI;
				} else if (radif < -Math.PI) {
					radif = radif + 2*Math.PI;
				}
				dec = dec/radeg;
				var cos_dec = Math.cos(dec);
				var sin_dec = Math.sin(dec);
				var cos_radif = Math.cos(radif);
				var sin_radif = Math.sin(radif);
				var h = sin_dec*sin_crval1 + cos_dec*cos_crval1*cos_radif;
				var xsi = cos_dec*sin_radif/h;
				var eta = (sin_dec*cos_crval1 - cos_dec*sin_crval1*cos_radif)/h;
				var xdif = cdinv00*xsi + cdinv01*eta;
				var ydif = cdinv10*xsi + cdinv11*eta;
				return [xdif+crpix0,ydif+crpix1];
			};
		} else if (wcs.proj == 'SIN') {
			this.mapWCS = function(x,y) {
				// first pixel is (1,1)
				var xdif = x - crpix0;
				var ydif = y - crpix1;
				var xsi = cd00*xdif + cd01*ydif;
				var eta = cd10*xdif + cd11*ydif;
				var zt = Math.sqrt(1 - xsi*xsi - eta*eta);
				var dec = Math.asin(eta*cos_crval1+zt*sin_crval1);
				var ra = Math.atan2(xsi, zt*cos_crval1-eta*sin_crval1) + crval0;
				ra = ra*radeg;
				dec = dec*radeg;
				return [ra, dec];
			};
			this.mapWCSinv = function(ra,dec) {
				var radif = ra/radeg - crval0;
				// guard against zero wraparound
				if (radif > Math.PI) {
					radif = radif - 2*Math.PI;
				} else if (radif < -Math.PI) {
					radif = radif + 2*Math.PI;
				}
				dec = dec/radeg;
				var cos_dec = Math.cos(dec);
				var sin_dec = Math.sin(dec);
				var cos_radif = Math.cos(radif);
				var sin_radif = Math.sin(radif);
				var xsi = cos_dec*sin_radif;
				var sint = sin_dec*sin_crval1 + cos_dec*cos_crval1*cos_radif;
				var eta = sin_dec*cos_crval1 - cos_dec*sin_crval1*cos_radif;
				var xdif = cdinv00*xsi + cdinv01*eta;
				var ydif = cdinv10*xsi + cdinv11*eta;
				return [xdif+crpix0,ydif+crpix1];
			};
		}
		// leave mapWCS undefined for unknown projections
	}

    
    
})