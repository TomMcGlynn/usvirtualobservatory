import VOClient
def object2pos( target) :
    """
    Returns a doublet in decimal degrees of the target object
    """
    if(type(target) == str) :
       a = VOClient.nameResolver(target)
       pos =(VOClient.resolverRA(a), VOClient.resolverDEC(a));
    else :
       if(type(target) == list ):
	   pos = []
           for thetarget in target :
              a = VOClient.nameResolver(thetarget)
	      pos.append((VOClient.resolverRA(a), VOClient.resolverDEC(a)))
       else :
	       print "oops: ", type(target)
               pos = None
    return pos
def object2sexapos(target) :
    """
    Returns a string in sexadecimal of the target object
    """
    if(type(target) == str) :
        a = VOClient.nameResolver(target)
        pos = VOClient.resolverPos(a)
    else :
       if(type(target) == list ):
	   pos = []
           for thetarget in target :
              a = VOClient.nameResolver(thetarget)
	      pos.append(VOClient.resolverPos(a))
       else :
	       print "oops: ", type(target)
	       pos = None
    return pos

