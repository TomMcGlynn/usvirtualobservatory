#! /bin/bash
#
prog=`basename $0` 
########## include this from external file? ###################
export VAO_SW_REPOS=http://dev.usvao.org/repos/vao

antver=1.8.2
ivyver=2.2.0
antcontribver=1.0b3

antpkg=etc/apache-ant-$antver-bin.zip
ivyjar=etc/ivy-$ivyver.jar
antcontribjar=etc/ant-contrib-$antcontribver.jar
newstack_build_file=lib/ant/cmd/newstack.xml
#newstack_build_file=self-install.xml
tmpdir=/tmp
###############################################################

javaexe=java
vaohome=
cmderr=$tmpdir/${prog}-err.txt
cmdout=$tmpdir/${prog}-out.txt

javahelp() {
    echo "Please make sure Java is installed; then do one of the following:"
    echo "  o  put the Java bin directory in your command search path ($PATH),"
    echo "  o  set the JAVA_HOME environment variable to the Java's root directory, or "
    echo "  o  provide Java's root directory to the -j option to this script"
}

usage() {
    echo "${prog} [-h] [-j java_home] vao_home"
}

help() {
    echo "Arguments:"
    echo " vao_home  the root directory where to install all VAO software"
    echo "Options:"
    echo "  -j java_home   assume java is located in java_home/bin"
    echo "  -f             force overwrite of existing stack"
    echo "  -X             do not fetch and install Ant extensions"
    echo "  -h             print this help and exit"
}

init_vaohome_dir() {
    local vaohome=$1
    mkdir -p $vaohome || return $?
    mkdir -p $vaohome/{bin,lib,doc,products,cache,build} || return $?
    productsDir=$vaohome/products
}

# process command line arguments
#
overwrite_stack=
install_ant_ext=1
while [ $# -gt 0 ]; do
    case "$1" in 
        -H) vaohome="$2"; shift;;
        -j) javahome="$2"; shift;;
        -f) overwrite_stack=1;;
        -X) install_ant_ext=;;
        -h) help; exit 0;;
        *)  break;;
    esac
    shift
done

# check availability of JAVA
#
java=`/usr/bin/which java`
[ -n "$javahome" ] && {
    [ ! -d "$javahome" ] && {
        echo "${javahome}: JAVA_HOME not found"
        echo 
        exit 1
    }
    java="$javahome/bin/java"
}
[ -z "$java" -o ! -x "$java" ] && {
    echo "Executable java not found"
    javahelp
    exit 1
}

# set the location of VAO_HOME, the stack directory
#
[ -n "$1" ] && vaohome=$1
[ -z "$vaohome" ] && {
    read -a vaohome_in -p "Enter directory to install VAO software into [\$PWD]: "
    vaohome="${vaohome_in[*]}"
    [ -z "$vaohome" ] && vaohome=$PWD
}

echo "JAVA_HOME=$javahome"
echo "VAO_HOME=$vaohome"
export VAO_HOME="$vaohome"

# sanity-check the stack directory
#
[ -e "$VAO_HOME" ] && {
    [ ! -d "$VAO_HOME" ] && {
        echo "${vaohome}: VAO_HOME not a directory"
        exit 1
    }
    files=`ls $VAO_HOME`
    if [ -n "$files" ]; then
        if [ -z "$overwrite_stack" ]; then
            echo "Non-empty VAO_HOME already exists; use -f to overwrite"
            exit 1
        else
            echo "Warning: overwriting existing VAO_HOME"
        fi
    fi
}

# initialize the stack directory
#
init_vaohome_dir $VAO_HOME

# install ant into stack
#
export ANT_HOME=$productsDir/apache/ant/$antver
antHomeParent=`dirname $ANT_HOME`
mkdir -p $antHomeParent || exit 1
unzip -q $antpkg -d $antHomeParent > /dev/null 2> $cmderr || {
    echo "Failed to unzip Ant zipfile: $antpkg"
    cat $cmderr
    exit 2
}
mv $antHomeParent/apache-ant-$antver $ANT_HOME

# install ivy into Ant
#
cp $ivyjar $ANT_HOME/lib || {
    echo "Failed to install Ivy into Ant"
    exit 2
}

# install ant-contrib into Ant
#
cp $antcontribjar $ANT_HOME/lib || {
    echo "Failed to install Ant-Contrib into Ant"
    exit 2
}

# install Ant extensions (this include junit and ant-contrib?)
#
[ -n "$install_ant_ext" ] && {
    olddir=$PWD
    cd $ANT_HOME
    echo "Downloading Ant extenstions..."
    $ANT_HOME/bin/ant -f fetch.xml -Ddest=system > $cmdout 2> $cmderr 
    if [ $? -gt 0 ]; then
        echo "Warning: Trouble install Ant extensions"
        cat $cmderr
        echo 
        echo "Is networking enabled?"
        echo "To try again with a network connection, type, "
        echo "  cd $ANT_HOME && ant -f fetch.xml -Ddest=system"
    else
        echo "...extension installation complete"
    fi
    cd $olddir
}

# Now build vaosoft
# 
$ANT_HOME/bin/ant -f $newstack_build_file install > $cmdout 2> $cmderr || {
    cat $cmderr
    exit 3
}

[ -f "$cmdout" ] && rm -f $cmdout
[ -f "$cmderr" ] && rm -f $cmderr

echo 
echo Base tools have been installed.
exit 0






