vaotest.descr = "Test samp_003 : local send"
samp send test.start name=samp003 
!(sleep 1; send -r testingclient client.cmd.exec imstat /home/iraftest/data/dpix.fits &)
samp send test.end
