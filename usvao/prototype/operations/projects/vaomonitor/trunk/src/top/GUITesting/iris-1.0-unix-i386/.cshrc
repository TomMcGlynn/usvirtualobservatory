#ident "@(#) Standard EUD .cshrc template 14 March 2005"
#
setenv GNUBINDIR /usr1/local/gnu/bin

set path=( /usr/ucb /usr/bin/X11 $GNUBINDIR /usr/etc /usr/bin )
#set path=($path  /usr1/local/javasdk/bin/jar)
set path=( $path /usr/local/bin /usr1/local/bin /bin ~/bin . )
set path = (/Home/eud/preciado/nvo/personal_installs/Python-2.4.1 $path)
#setenv MYSQL_UNIX_PORT /skyview/temp/mysql.sock 
set path = (/heasarc/bin $path)
set path = (/usr/local/web_chroot/heasarc/sybase/OCS/bin $path)
set path = (/usr1/local/javasdk/bin  $path)
set path = (~/xerces-1_4_4/samples/sax/ $path)
set path = (~/SW/ $path)
set path = (~/oairecords/ $path)
set path = (/software/jira/ant/ant/bin/ $path)
set path = (/www/htdocs/selenium/selenium-grid-1.0.3 $path)
set path = (/www/htdocs/vo/vaomonitor/java $path)




if ($?LANG) unsetenv LANG

setenv NVOSS_HOME /Home/eud/preciado/nvo/nvoss2005
#setenv CLASSPATH  /skyview3/java/mysql-connector-java-3.0.16-ga/mysql-connector-java-3.0.16-ga-bin.jar:.
setenv CLASSPATH /www/server/vo/mysql-connector-java-5.1.7/mysql-connector-java-5.1.7-bin.jar:.:../javalib:/software/jira/software/class/:/www/htdocs/vo/vaomonitor/java/:/software/jira/software/ljar/fits.jar:/software/jira/software/fjar/bzip2.jar




setenv MANPATH /usr/share/man:/usr/man:/usr1/local/man:/usr/X11R6/man
setenv LD_LIBRARY_PATH /usr/lib:/usr/lib/X11:/usr1/local/lib


setenv SYBASE /heasarc/sybase
setenv SYBASE_OCS OCS
setenv LD_LIBRARY_PATH ${SYBASE}/OCS/lib:${LD_LIBRARY_PATH}

#setenv DSQUERY SYBASE_DBMS1

###setenv QUERY_STRING 'GnET cgi-bin/OAI2test/XMLFile/nvo/oai.pl?verb=Identify'


setenv DSQUERY SYBASE_DBMS2






switch (`uname`)	#Operating system-specific path additions
	case OSF1:
		set path = ($path /sbin /usr/sbin); breaksw
	case IRIX:
		set path = ($path /usr/bsd); breaksw
	case SunOS:    #only want to add to path for Solaris
		#for both SunOS & Solaris
		setenv OPENWINHOME /usr/openwin
		setenv MANPATH ${MANPATH}:/usr/lang/man:$OPENWINHOME/man
		setenv LD_LIBRARY_PATH ${LD_LIBRARY_PATH}:/usr/lang/SC1.0:/usr/openwin/lib
		#SunOS only
		if (-e /vmunix) then
		 set path =($path $OPENWINHOME/bin /usr/games /usr/lang)
		endif
		#Solaris only
		if (-x /sbin/sync) then
	   	 set path =(/opt/bin /usr/ccs/bin /usr/lib/nis $path )
	   	 set path =(/opt/SUNWspro/bin $OPENWINHOME/bin $path )
		 setenv MANPATH ${MANPATH}:/opt1/man
		endif
		breaksw
	case Darwin:
		set path =(/usr/X11R6/bin $path)
		#Fink setup
		if (-e /sw/bin/init.csh) source  /sw/bin/init.csh
	endsw


#         skip remaining setup if not an interactive shell
if ($?USER == 0 || $?prompt == 0) exit

##
## Aliases and shortcuts
##
set noclobber
limit   coredumpsize 0   # turn off core dumps
#
set notify   = on
set history  = 30	#history commands to remember this session
set savehist = 40	#history commands to remember between sessions
set voprod = "/usr/local/web_chroot.prod/.www_mountpnt/www/htdocs/vo/logs/"
set oai = "/usr/local/web_chroot.prod/www/htdocs/cgi-bin/OAI/XMLFile/nvo/data"
set oaitest = "/usr/local/web_chroot.prod/www/htdocs/cgi-bin/OAItest/XMLFile/nvo/data"
set oainewt = "/usr/local/web_chroot.prod/www/htdocs/cgi-bin/OAI2test/XMLFile/nvo/data"
set oainew  = "/usr/local/web_chroot.prod/www/htdocs/cgi-bin/OAI2/XMLFile/nvo/data"
set mycgi   = "/usr/local/web_chroot/.www_mountpnt/www/htdocs/cgi-bin/preciado"

set customvo  = "/usr/local/web_chroot/.www_mountpnt/www/htdocs/cgi-bin/vo/monitor"
set customvop = "/usr/local/web_chroot.prod/.www_mountpnt/www/htdocs/cgi-bin/vo/monitor"
set docsvo      = "/usr/local/web_chroot/.www_mountpnt/www/htdocs/vo/"
set docsvop     = "/usr/local/web_chroot.prod/.www_mountpnt/www/htdocs/vo"
set vd         = "/usr/local/web_chroot/.www_mountpnt/www/htdocs/cgi-bin/vo/validation"


#
alias	dir	ls -aF
alias   ll      ls -alF
alias   show    env
alias   comp    cd /usr/local/web_chroot/.www_mountpnt/www/htdocs/preciado
alias   rm      "rm -i"
alias   cp      "cp -i"
alias   mv      "mv -i"
alias	xlock	'\xlock -allowroot'
alias  nw      xterm -fg black
alias browse  cd /usr/local/web_chroot/www/htdocs/cgi-bin/W3Browse/lib/Browse
alias oai    cd /usr/local/web_chroot.prod/www/htdocs/cgi-bin/OAItest/XMLFile/nvo/data
alias sia      cd /Home/home1/tam/java/xsk/surveys/xml
alias apach   cd /usr/local/web_chroot/usr/wwwserver/apache/logs
alias vo      cd /usr/local/web_chroot/www/htdocs/vo/logs
alias ndi     mkdir 
alias ne     vi


#alias pd           dirs	#These three commands handle the 
#alias po           popd	#directory stack (very useful)
#alias pu           pushd




#
# set default printer to your printer of choice
setenv PRINTER b22-r119a-color

#
# Set a search path for application default files 
# ("%T"="app-defaults",  "%N"=name of program, e.g., XTerm )
setenv XFILESEARCHPATH /usr/lib/X11/%T/%N:/usr1/local/lib/%T/%N


setenv LESS '-M'   #sets 'less' pager to show line numbers and percentages
#setenv MORE -c    # set 'more' for page clearing mode


alias cd        'cd \!*;echo $cwd'   #show dir after change; superseded below
#If one is using tcsh, one does not want the prompt clobbered here:
if ( $shell == /bin/csh )  then
	set prompt = "`hostname`:$cwd(\!) "
	alias cd chdir \!\* \; set prompt = `hostname`:\"\$cwd\(\\\!\) \"
endif


################# Scientific software #####################################
#
# IDL pointers; device pointer under X, Sun 
# uncomment if you will use IDL
#
### For EUD cluster users, uncomment the next line:
#if (-e /software/IDL/idl.6.1/idl_setup) source /software/IDL/idl.6.1/idl_setup
# For most recent version and license server info, see
# http://universe.gsfc.nasa.gov/eudcomp/IDL/index.html


# LHEASOFT software:
#   FTOOLS, XANADU (XIMAGE, XRONOS, XSPEC), and XSTAR initialization
# 
# Choose from the following:
# uncomment the next line to use the latest release version of these packages:
#if (-r /software/lheasoft/release/lhea.csh) source /software/lheasoft/release/lhea.csh
# 
# uncomment the next line to use the latest develop version of these packages:
#if (-r /software/lheasoft/develop/lhea.csh) source /software/lheasoft/develop/lhea.csh

# IRAF use
# Please contact "system@milkyway" so IRAF can be configured on your
# desktop computer. Then run the command "mkiraf" (once only) in the
# directory in which you want to run IRAF to configure your personal
# settings. The command "cl" starts IRAF.
#
# mkiraf
# (to terminal type question, answer gterm if you use openwindows)
# (to terminal type question, answer xterm if you use Xwindows)

# CIAO use
# Uncomment the following line to set up CIAO. If you need the CALDB,
# you'll need to contact "system@milkyway" and ask that /FTP be mounted
# on your machine if it isn't already.
#
# alias ciao "source /usr1/local/Chandra/ciao/bin/ciao.csh"  

###################### End scientific software setup section ##############
