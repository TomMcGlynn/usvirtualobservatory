vaotest.descr = "Test samp_005 : loadImage"
samp send test.start name=samp005
samp loadImage  /home/iraftest/data/dpix.fits
samp loadImage http://iraf.noao.edu/vao/dpix.fits
samp send test.end
