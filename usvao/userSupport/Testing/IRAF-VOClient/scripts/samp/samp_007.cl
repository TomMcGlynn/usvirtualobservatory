vaotest.descr = "Test samp_007 : pointAt tests"
samp send test.start name=samp007
samp pointAt (15 * 10:23:01) 34:12:45
samp pointAt 10.2345 -20.1232
samp send test.end
