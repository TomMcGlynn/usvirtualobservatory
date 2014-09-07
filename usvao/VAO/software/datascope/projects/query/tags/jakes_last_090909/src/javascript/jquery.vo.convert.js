/*
	referenced some code for time conversion on ...
		http://home.online.no/~pjacklam/matlab/software/util/timeutil/jd2date.m
		http://home.online.no/~pjacklam/matlab/software/util/timeutil/mjd2date.m
*/

var mytest = 0;
var vo_ready = function() {
	$("select.vo_converter").each(function(){
		if ( $(this).attr("func") == "position" ) {
			$(this).change( function() {
				$("td."+$(this).attr("fn")).convert_position({
					format: $(this).val(), 
					scale: $(this).attr("scale") 
				}) 
			}).val('hhmmss').change();
		} else if ( $(this).attr("func") == "time" ) {
			$(this).change( function() {
				$("td."+$(this).attr("fn")).convert_time({
					iformat: 'mjd', 
					oformat: $(this).val() 
				}) 
			}).val('utc').change();
		}
	});
};

jQuery.fn.convert_position = function(options) {
	var settings = jQuery.extend({
		format: 'degree',
		scale: 1
	}, options);

	return this.each( function() {
		me = $(this);
		
	        if (mytest < 3) {
		    mytest += 1;
		}
		
		
		if ( settings.format == 'degree' ) {
			me.text( me.attr('degree') );
			me.removeClass('hhmmss').addClass('degree');
		} else {
			if ( ! me.attr('hhmmss') ) {
				if ( ! me.attr('degree') ) 
					me.attr('degree', me.text());
				if ( me.text() ) {
					sign = ( me.text() < 0 ) ? "-" : "" ;
					deg = Math.abs(me.text());
					hh = Math.floor(deg / settings.scale);
					xx = ((deg/settings.scale)-hh)*60;
					mm = Math.floor(xx);
					if (mm<10) mm = '0'+mm;
					ss = Math.floor((xx-mm)*60);
					if (ss<10) ss = '0'+ss;
					me.attr('hhmmss', sign+hh+":"+mm+":"+ss );
				} else
					me.attr('hhmmss', '' );
			}
			me.text( me.attr('hhmmss') );
			me.removeClass('degree').addClass('hhmmss');
		}
	});
};

jQuery.fn.convert_time = function(options) {
	var settings = jQuery.extend({
		iformat: 'mjd',
		oformat: 'utc'
	}, options);

	return this.each( function() {
		me = $(this);
		if ( settings.oformat == 'mjd' ) {
			me.text( me.attr('mjd') );
			me.removeClass('utc').addClass('mjd');
		} else {
			if ( ! me.attr('utc') ) {
				me.attr('mjd', me.text());
				if ( me.text() ) 
					me.attr('utc', voDate.mjd2utc(me.text()) );
				else
					me.attr('utc', '' );
			}
			me.text( me.attr('utc') );
			me.removeClass('mjd').addClass('utc');
		}
	});
};

var voDate = {
	jd2mjd  : function  (jd){ return parseFloat(jd)-2400000.5; },
	mjd2jd  : function (mjd){ return parseFloat(mjd)+2400000.5; },
	fday2hms : function (fday) {
		sign = (fday<0) ? "-" : "";
		fday = Math.abs(fday);
		h  = Math.floor(fday*24);
		dh = (fday*24)-h;
		if (h<10) h="0"+h;
		m  = Math.floor(dh*60);
		dm = (dh*60)-m;
		if (m<10) m="0"+m;
		s  = Math.floor(100*dm*60)/100;
		if (s<10) s="0"+s;
		return ( sign+h+":"+m+":"+s );
	},
	mjd2utc : function(mjd) {
		if ( mjd = parseFloat(mjd) ) {
			fmjd = mjd - Math.floor(mjd);
			jd = voDate.mjd2jd(mjd);
			return (voDate.jd2utc(jd)+"T"+voDate.fday2hms(fmjd));
		} else {
			return;
		}
	},
	jd2utc : function(jd) {
		ijd = Math.floor(parseFloat(jd) + 0.5);
		a = ijd + 32044;
		b = Math.floor((4 * a + 3) / 146097);
		c = a - Math.floor((b * 146097) / 4);

		d = Math.floor((4 * c + 3) / 1461);
		e = c - Math.floor((1461 * d) / 4);
		m = Math.floor((5 * e + 2) / 153);

		day   = e - Math.floor((153 * m + 2) / 5) + 1;
		if ( day < 10 ) day = "0"+day;
		month = m + 3 - 12 * Math.floor(m / 10);
		if ( month < 10 ) month = "0"+month;
		year  = b * 100 + d - 4800 + Math.floor(m / 10);
		return ( year+"-"+month+"-"+day );
	}
};
