function radio(name, text, action,id) {
   return "<input type='radio' name='"+name+"' onclick='return "+action+"' id='"+id+"' />"+
      "<label for='"+id+"'>"+text+"</label><br/>\n";
}

var inputText;

function starts() {
   var req  = getRequest();
   req.open("GET", "options.txt", false)
   req.send(null)
   var text  = req.responseText;
   inputText = text.split("\n");

   text = "";
   options = haves(inputText);
   for (i=0; i<options.length; i += 1) {
      action = 'choices("' + options[i] + '")';
      text  +=  radio("have", options[i], action, "have_"+i);
   }
   document.getElementById("start").innerHTML = text;
}

function choices(choice) {
   var results = wants(choice);
   var text    = "<b>What do you want to do?</b><br/>";
   for (i=0; i<results.length; i += 1) {
      action  = 'doText("' + choice + '","' + results[i] + '")';
      text   += radio("want", results[i], action, "want_"+i);
   }
   text += "<hr/>";
   document.getElementById("need").innerHTML = text;
   document.getElementById("advice").innerHTML = "";
}


function doText(choice, want) {
   var myText = act(choice, want);
   var div = document.getElementById("advice");
   div.innerHTML = "Try...<br/>"+myText.join("\n");
}

function haves() {
   var results = new Array();
   for (var i=0; i<inputText.length; i += 1) {
      if (inputText[i].substring(0,5) == "Have:") {
         results.push(inputText[i].substring(5));
      }
   }
   return results;
}

function wants(have) {
   var results = new Array();
   var inhave = false;

   for (var i=0; i<inputText.length; i += 1) {
      if (inputText[i].substring(0,5) == "Have:") {
         if (inputText[i].substring(5) == have) {
            inhave = true;
         } else {
            inhave = false;
         }
      }
      if (inhave && inputText[i].substring(0,5) == "Want:") {
         results.push(inputText[i].substring(5));
      }
   }
   return results;
}

function act(have, want) {
   var results = new Array();
   var inhave = false;
   var inwant = false;

   for (var i=0; i<inputText.length; i += 1) {
      if (inputText[i].substring(0,5) == "Have:") {
         if (inputText[i].substring(5) == have) {
            inhave = true;
         } else {
            inhave = false;
         }
      } else if (inputText[i].substring(0,5) == "Want:") {
         if (inhave && inputText[i].substring(5) == want) {
            inwant = true;
         } else {
            inwant = false;
         }
      } else if (inhave && inwant) {
         results.push(inputText[i]);
      }
   }
   return results;
}
// Try to support IE.
function getRequest() {
   var req = false
   try {
      req = new ActiveXObject("Msxml2.XMLHTTP")
   } catch (e) {
      req = false
      try {
         req = new ActiveXObject("Microsoft.XMLHTTP")
      } catch (ex) {
         req = new XMLHttpRequest()
      }
   }
   return req
}
