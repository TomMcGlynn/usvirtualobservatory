vaotest.descr = "Test samp_004 : loadFITS"
samp send test.start name=samp004
samp loadFITS  /home/iraftest/data/h_n5194-1_f555_wf3.fits
samp loadFITS  http://iraf.noao.edu/vao/dpix.fits
samp send test.end
