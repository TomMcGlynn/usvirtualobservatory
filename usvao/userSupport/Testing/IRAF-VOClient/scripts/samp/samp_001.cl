vao
vaotest.descr = "Test samp_001 ; set up client for testing"
samp send testing.start
samp send test.start name=samp001
samp name testingclient
fcache init
fcache purge
samp send test.stop
