Ext.define('Mvp.util.TableUtils', {
    statics: {
        
        // Some functions for recognizing Vo Table columns adapted from Aladin...
        
        raQuality: function(colName, ucd) {
            var t = Mvp.util.TableUtils;
            var n = 0;
            
            // Détection du RA et évaluation de la qualité de cette détection
            var qual=9999;
            if( ucd && (ucd.match(/^POS_EQ_RA_MAIN$/) || ucd.match(/^pos.eq.ra;meta.main$/)) ) qual=0; 
            else if( ucd && (ucd.match(/^POS_EQ_RA/) || ucd.match(/^pos.eq.ra/)) ) qual=100;
            else if ((n = t.raName(colName)) < 9999) qual=n+200;
            else if ((n = t.raSubName(colName)) < 9999) qual=n+300;
            
            return qual;
        },
        
        /** Retourne un indice entre 0 (meilleur) et 9 en fonction de la reconnaissance
         * ou non du nom d'une colonne en tant que RA,
         * (-1 s'il ne s'agit a priori pas de cela)
         */
        raName: function(s) {
            if( s.match(/^_RAJ2000$/i) ) return 0;
            if( s.match(/^RAJ2000$/i) )  return 1;
            if( s.match(/^_RA$/i) )      return 2;
            if( s.match(/^RA\(ICRS\)$/i) ) return 3;
            if( s.match(/^RA$/i) )       return 4;
            if( s.match(/^ALPHA_J2000$/i) ) return 5;
            return 9999;
         },
         
        /** Retourne un indice entre 0 (meilleur) et 9 en fonction de la présence
         * d'une sous chaine RA dans le nom de colonne (-1 sinon) */
        raSubName: function(s) {
            if( s.match(/RADIUS/i)) return -1;
            if( s.match(/^_RA/i)) return 0;
            if( s.match(/^RA/i))   return 1;
            return 9999;
         },
         
         decQuality: function(colName, ucd) {
            var t = Mvp.util.TableUtils;
            var n = 0;
            
            // Détection du DE et évaluation de la qualité de cette détection
            var qual=9999;
            if(ucd && (ucd.match(/^POS_EQ_DEC_MAIN$/) || ucd.match(/^pos.eq.dec;meta.main$/)) )qual=0;
            else if(ucd && ( ucd.match(/^POS_EQ_DEC/) || ucd.match(/^pos.eq.dec/)) ) qual=100;
            else if ((n = t.decName(colName)) < 9999) qual=n+200;
            else if ((n = t.decSubName(colName)) < 9999) qual=n+300;
            
            return qual;
         },
         
        /** Retourne un indice entre 0 (meilleur) et 9 en fonction de la reconnaissance
         * ou non du nom d'une colonne en tant que DE,
         * (-1 s'il ne s'agit a priori pas de cela)
         */
        decName: function(s) {
           if( s.match(/^_DEJ2000$/) )  return 0;
           if( s.match(/^_DECJ2000$/) ) return 1;
           if( s.match(/^DEJ2000$/) )   return 2;
           if( s.match(/^DECJ2000$/) )  return 3;
           if( s.match(/^_DE$/) )       return 4;
           if( s.match(/^_DEC$/) )      return 5;
           if( s.match(/^DE(ICRS)$/) )  return 6;
           if( s.match(/^DEC(ICRS)$/) ) return 6;
           if( s.match(/^DE$/) )        return 8;
           if( s.match(/^DEC$/) )       return 9;
           if( s.match(/^DELTA_J2000$/) ) return 9;
           return 9999;
        },
        
        /** Retourne un indice entre 0 (meilleur) et 9 en fonction de la présence
         * d'une sous chaine DE dans le nom de colonne (-1 sinon) */
        decSubName: function(s) {
           if( s.match(/^_DEC/i)) return 0;
           if( s.match(/^_DE/i))   return 1;
           if( s.match(/^DEC/i))   return 2;
           if( s.match(/^DE/i))     return 3;
 //          if( s.match(/DE/i)>0 || s.indexOf(/de/)>0 )       return 4;
           return 9999;
        },
        
        
        /**
         * Find the column that has a certain ucd type, based on column data from
         * the MvpGrid object.  Returns first occurance found.
         */
        getColumnbyUCD: function (columns, ucd) {
            for (c in columns) {
                var col = columns[c];
                
                // Pull the VO Table attributes out of the extended properties.
                var vot = {};
                if (col.ExtendedProperties) {
                    vot = Mvp.util.Util.extractByPrefix(col.ExtendedProperties, 'vot');
                }
                if( vot.ucd && vot.ucd == ucd ){
                    return col.text;
                }
            }
            return null;
        },
        
        
        /**
         * Get the 2d extent, in acrminutes, of an image based on info returned from an SIA query.  columns
         * and record agruments are in the format used in the MvpGrid object.
         */
        getSiaExtent: function (columns, record) {
            // Though the VOTable standard seems to indicate that array elements should be
            // separated by spaces, there are some implementations that use commas.
            
            var axesColumn = Mvp.util.TableUtils.getColumnbyUCD(columns, 'VOX:Image_Naxis');
            var axesValue = record.get(axesColumn);
            axesValue = axesValue.replace(/[ ,]+/g, ' ');
            var axes = axesValue.split(' ');

            var scaleColumn = Mvp.util.TableUtils.getColumnbyUCD(columns, 'VOX:Image_Scale');
            var scaleValue = new String( record.get(scaleColumn) );
            scaleValue = scaleValue.replace(/[\s,]+/g, ' ');
            scaleValue = scaleValue.replace(/^\s*/, '');
            scaleValue = scaleValue.replace(/\s*$/, '');
            
            var scale = scaleValue.split(' ');
            
            if( isNaN(scale[0]) || scale[0]==0 ){
                return ["N/A", "N/A"];
            }
            
            if( scale[1] == null ){
                scale[1] = scale[0];
            }else{
                if( isNaN(scale[1]) || scale[1]==0 ){
                    return ["N/A", "N/A"];
                }
            }
            
            var x = Ext.util.Format.number(Math.abs(scale[0]*axes[0]*60), '0.0000');
            var y = Ext.util.Format.number(Math.abs(scale[1]*axes[1]*60), '0.0000');
            
            return [x,y];
        }
    }
});