vaotest.descr = "Test samp_010 : broadcast and directed samp getparam "
samp send test.start name=samp010
samp getparam fcache.cmd 
samp getparam fcache.cmd to=noclient 
samp getparam fcache.cmd to=testingclient
samp getparam fcache.cmd to=echoclient
samp send test.end
samp send testing.end
