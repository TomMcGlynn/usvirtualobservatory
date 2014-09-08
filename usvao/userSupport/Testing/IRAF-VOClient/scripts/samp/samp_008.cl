vaotest.descr = "Test samp_008 : broadcast, directed getenv"
samp send test.start name=samp008
samp getenv dude 
samp getenv dude to=noclientexists
samp getenv dude to=testingclient 
samp getenv dude to=echoclient 
samp send test.end
samp send testing.end
