vaotest.descr = "Test samp_009 : broadcast, directed setenv"
samp send test.start name=samp009
samp getenv dude 
samp setenv dude abides 
samp getenv dude 
samp setenv dude abides to=noclientexists
samp getenv dude 
samp setenv dude abides to=testingclient
samp getenv dude 
samp setenv dude abides to=echoclient
samp getenv dude 
samp send test.end
samp send testing.end
