#! /bin/bash
#
help() {

cat <<EOF
filter a template

Usage filter_template.sh [NAME=VALUE]... infile
EOF
}

filter_template() {
    out=${1%.in}
    local params=(`grep -oE '@[[:alnum:]]*@' $1 | sed -e 's/@//g' | sort -u`)
    local filt=(sed)
    local v
    for p in ${params[@]}; do
        v=`eval esc_sed_filt \\\$$p`
        filt=("${filt[@]}" -e "s/@$p@/$v/g")
    done
    # echo filt: ${#filt[@]}: ${filt[@]}

    # echo creating $out
    if [ ${#params[@]} -gt 0 ]; then
        grep -v ^## $1 | "${filt[@]}"
    else 
        grep -v ^## $1
    fi
}

esc_sed_filt() {
    echo "$@" | sed -e 's/\//\\\//g'
}

file=

# set -x
# Process any cmdline args.
while [ $# -gt 0 ]
do

    case "$1" in
    -h | --help)                                # print a help summary
        help
        exit 0
        ;;
    [a-zA-Z0-9_]*=*)
        name=`echo $1 | sed -e 's/=.*$//'`
        val=`echo $1 | sed -e 's/^[^=]*=//'`
        eval $name=$val
        shift
        ;;
    *)
        if [ -n "$file" ]; then
            echo Ignoring extra argument: $1
        else 
            file=$1
        fi
        shift
        ;;
    esac
done

filter_template $file
