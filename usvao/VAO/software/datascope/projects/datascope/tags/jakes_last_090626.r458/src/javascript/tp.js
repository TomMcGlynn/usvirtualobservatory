/*
* date:	2002-10-11
* info:	http://inspire.server101.com/js/tp/
*/

var tp = [];
var tpl = [];

function tpSet(i, c) {
   if (document.createElement) {
      var e = document.getElementById(i);
      var l = document.createElement('ul');
      var p = document.createElement('div');
      e.className = l.className = p.className = c;

      var a, j, t;
      for (j = 2; j < arguments.length; j++) {
         c = document.getElementById(t = arguments[j]);
         tp[t] = c.parentNode.removeChild(c);

         a = l.appendChild(document.createElement('li'));
         a.className = c.className;
         var anchor = document.createElement('a')
         anchor.setAttribute("id", "p__"+t)
         tpl[t] = a = a.appendChild(anchor);
         a.setAttribute('href', 'javascript:tpShow(\''+i+'\', \''+t+'\');');
         a.appendChild(document.createTextNode(c.getAttribute('title')));
      }

      p.appendChild(tp[arguments[2]]);
      tpl[arguments[2]].className = 'active';
      // added by jake on 080421 to actually make the whole tab (li AND a) change class
      tpl[arguments[2]].parentNode.className = 'active';

      while (e.firstChild) e.removeChild(e.firstChild);
      e.appendChild(l);
      e.appendChild(p);
   }
}

function tpShow(e, p) {
   e = document.getElementById(e).lastChild;
   // added by jake on 080421 to actually make the whole tab (li AND a) change class
   tpl[e.firstChild.getAttribute('id')].parentNode.className = null;
   tpl[e.replaceChild(tp[p], e.firstChild).getAttribute('id')].className = null;
   tpl[p].className = 'active';
   // added by jake on 080421 to actually make the whole tab (li AND a) change class
   tpl[p].parentNode.className = 'active';
}

function tpGetPane(e) {
   return tp[e]
}
