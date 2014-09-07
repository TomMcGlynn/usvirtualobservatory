#! /bin/bash

# We could build with Make, but that would take more effort.

platform="unknown"
unamestr=`uname`
if [[ "$unamestr" == 'Linux' ]]; then
    platform='linux'
elif [[ "$unamestr" == 'Darwin' ]]; then
    platform='mac'
else
    echo "Unrecognized uname: $unamestr"
    exit 1;
fi

libs='-lpam'
if [[ "$platform" == 'linux' ]]; then
    libs="$libs -lpam_misc"
fi

targets='pam_test pam_cmd'

exit_code=0
for target in $targets; do
    source=${target}.c
    cmd="gcc -o $target $libs $source"
    echo $cmd
    $cmd
    if [ ! $? ]; then
	echo "Warning: failed to compile $source"
	exit_code=1
    fi
done

if [ $exit_code ]; then
    echo "successfully built [ $targets ]"
fi

exit $exit_code
