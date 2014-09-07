var sep = '&nbsp;'

function sexagesimal(value, precision) {

   var str = ""
   var deg, frac, min, sec, fmin, fsec;

   if (value < 0) {
      value = Math.abs(value);
      str = "-";
   }

   if (precision > 13) {
      precision = 13;
   }

   if (precision <= 2) {
      str += numb(Math.floor(value + 0.49999999));

   } else if (precision == 3) {

      deg  = Math.floor(value);
      frac = Math.floor(10*( value-Math.floor(value)) + 0.5);
      if (frac == 10) {
         deg += 1;
         frac = 0;
      }

      str += numb(deg)+"."+frac;

   } else if (precision == 4) {

      deg = Math.floor(value);
      min = Math.floor(60*(value - deg) + 0.5);
      if (min == 60) {
         deg += 1;
         min = 0;
      }
      str += numb(deg)+sep+numb(min);

   } else if (precision == 5) {

      deg  = Math.floor(value);
      fmin = 60.*(value-deg);

      min  = Math.floor(fmin);
      frac = Math.floor(10.*(fmin-min) + .5);

      if (frac == 10) {
         min += 1;
         frac = 0;
      }
      if (min == 60) {
         deg += 1;
         min = 0;
      }

      str += numb(deg) + sep + numb(min) + "." + frac;

   } else if (precision == 6) {

      deg  = Math.floor(value); 
      fmin = 60.*(value-deg); 
      min  = Math.floor(fmin);
      sec  = Math.floor(60.*(fmin-min) + .5); 
      if (sec == 60) { 
         min += 1;
         sec = 0; 
      } 
      if (min == 60) { 
         deg += 1; 
         min = 0; 
      } 

      str += numb(deg) + sep + numb(min) + sep + numb(sec);

   } else {
      var i
      var maxval = 1

      deg   = Math.floor(value);
      fmin  = 60.*(value-deg);
      min   = Math.floor(fmin);
      fsec  = 60.*(fmin-min);
      sec   = Math.floor(fsec);
      var ffrac = fsec - sec;

      for (i=6; i<precision; i += 1) {
         ffrac  *= 10.;
         maxval *= 10;
      }
      frac = Math.floor(ffrac + 0.5);
      if (frac == maxval) {
         sec += 1;
         frac = 0;
      }

      if (sec == 60) {
         min += 1;
         sec = 0;
      }
      if (min == 60) {
         deg += 1;
         min = 0;
      }

      // need to format this properly
      str += numb(deg)+sep+numb(min)+sep+numb(sec)+"."+frac;
   }
   return str;
}

function numb(input) {
   if (input < 10) {
      return "0"+input;
   } else {
      return input;
   }
}

