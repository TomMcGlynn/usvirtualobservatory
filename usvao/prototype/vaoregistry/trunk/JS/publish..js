// Registry publishing helper functions
// T. Dower, 2008 Feb 4

function helppop( string helptext)
{
my_window= window.open ("",
  "mywindow1","status=1,width=350,height=150");
my_window.document.write(helptext); 
}