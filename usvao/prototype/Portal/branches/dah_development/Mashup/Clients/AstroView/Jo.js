// initialize jo
jo.load();

joCache.set("button", function() {
	var complex = new joButton("Dialog with Form Controls")
	
	complex.selectEvent.subscribe(function() {
		App.scn.showPopup(joCache.get("popup"));
	});
		
	return complex;
});

// a more complex popup with dialog controls
joCache.set("popup", function() {
	var popup = [
		new joTitle("Login Form Example"),
		new joGroup([
			new joHTML("You can load up a popup with form widgets and more."),
			new joDivider(),
			new joCaption("User Name"),
			new joFlexrow(new joInput("Jo")),
			new joLabel("Password"),
			new joFlexrow(new joPasswordInput("password"))
		]),
		new joButton("Login").selectEvent.subscribe(pop)
	];
	
	function pop() {
		console.log("hide popup");
		App.scn.hidePopup();
	}
	
	return popup;
});

var App = {};
var button = joCache.get("button");
view.div.appendChild(button);
//App.scn = new joScreen(button);