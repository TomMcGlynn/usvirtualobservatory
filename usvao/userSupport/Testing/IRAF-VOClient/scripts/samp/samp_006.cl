vaotest.descr = "Test samp_006 : loadVOTable"
samp send test.start name=samp006
samp loadVOTable /home/iraftest/data/usno-b.xml
samp loadVOTable http://iraf.noao.edu/vao/usno-b.xml
samp send test.end
