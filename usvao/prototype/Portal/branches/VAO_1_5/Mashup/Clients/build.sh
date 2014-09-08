#!/bin/sh
sencha create jsb -a 'Mast/Portal.html?isBuild' -p Mast/portal.jsb3
sencha build jsb -p Mast/portal.jsb3 -d Mast

sencha create jsb -a 'Portal/DataDiscovery.html?isBuild' -p Portal/portal.jsb3
sencha build jsb -p Portal/portal.jsb3 -d Portal 

sencha create jsb -a 'dt/desktop.html?isBuild' -p dt/portal.jsb3
sencha build jsb -p dt/portal.jsb3 -d dt