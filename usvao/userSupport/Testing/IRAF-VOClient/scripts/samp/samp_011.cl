vaotest.descr = "Test samp_011 : broadcast and directed samp setparam"
samp send test.start name=samp011
samp setparam vodata.verbose yes
samp getparam vodata.verbose
samp setparam vodata.verbose yes to=noclient 
samp getparam vodata.verbose to=noclient 
samp setparam vodata.verbose no to=testingclient
samp getparam vodata.verbose to=testingclient 
samp setparam vodata.verbose yes to=echoclient
samp getparam vodata.verbose to=echoclient
samp getparam vodata.verbose
samp send test.end
samp send testing.end
