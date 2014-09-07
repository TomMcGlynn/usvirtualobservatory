#! /bin/bash
#
# Check the system configuration to ensure it has been set up correctly
# to run vaosso
#
prog=`basename $0 | sed -e 's/\.sh$//'`
verbosity=2
tmperr=/tmp/${prog}.err
# set -x

function getinstalldir {
    home=`grep dir.install= $1 | sed -e 's/^[^=]*=//'` || return 1
    home_bin=$home/bin
    return 0
}

function loadprops {
    for pfile in $@; do
        [ -f "$pfile" ] || {
            err ${pfile}: properties file not found
            return 1
        }
    done
    cat $@ | $home_bin/prop2var.py > /tmp/${prog}.var
    source /tmp/${prog}.var
    # rm /tmp/${prog}.var
}

function runtest {
    func=$1; shift
    desctest "${@}" '	'
    $func > $tmperr 2>&1
    reporttest $? $tmperr
    # [ -e "$tmperr" ] && rm -f $tmperr
}

function havecmd {
    cmd=$1; shift
    desctest Checking for $cmd command # "${@}" '	'
    checkcmd $cmd > $tmperr 2>&1
    reporttest $? $tmperr
    [ -e "$tmperr" ] && rm -f $tmperr
}

function havepath {
    pth=$1; shift
    desctest Checking for $pth
    [ -e "$pth" ] || { 
        echo ${pth}: path not found > $tmperr
        false
    }
    reporttest $? $tmperr
    [ -e "$tmperr" ] && rm -f $tmperr
}

function desctest {
    echo -n "${@}" '	'
}
function reporttest {
    status=$1
    outfile=$2
    if [ $status -ne 0 ]; then
        echo "[FAILED]"
        sed -Ee 's/^/  /' $outfile
    else
        echo "[OK]"
    fi
}

function checkcmd {
    cmdpath=`which $1` 
    [ -z "$cmdpath" ] && {
        echo "${1}: Command not found" 1>&2
        return 1
    }
    [ -x "$cmdpath" ] || return 1
    return 0
}

function hostcerts {
    [ -f /etc/grid-security/hostcert.pem ] 
}

function sslconf {
    local sslconf=${dir_sys_httpd_conf}/conf.d/ssl.conf
    [ -f $sslconf ] || {
        echo Missing mod_ssl config file: $sslconf
        return 1
    }
    prob=0

    cfile=`grep '^SSLCertificateFile ' $sslconf | head -1 | awk '{print $2}'`
    echo cfile: $cfile
    [ -n "$cfile" -a -f "$cfile" ] || {
        echo Host Cert file not found: $cfile
        prob=1
    }
    echo $cfile | grep -qs "^${dir_sys_grid_security}/" || {
        echo Host Cert file not installed in grid security area:
        echo "  expected dir: ${dir_sys_grid_security}"
        echo "  cert: $cfile"
        prob=1
    }


    cfile=`grep '^SSLCertificateKeyFile ' $sslconf | head -1 | awk '{print $2}'`
    [ -n "$cfile" -a -f "$cfile" ] || {
        echo Host key file not found: $cfile
        prob=1
    }
    echo $cfile | grep -qs "^${dir_sys_grid_security}/" || {
        echo Host key file not installed in grid security area:
        echo "  expected dir: ${dir_sys_grid_security}"
        echo "  cert: $cfile"
        prob=1
    }

    return $prob
}

function proxyajp {
    local conf="${dir_sys_httpd_conf}/conf.d/proxy_ajp.conf"
    [ -e "$conf" ] || {
        echo Missing proxy_ajp.conf in ${dir_sys_httpd_conf}/conf.d/
        return 1
    }

    needed=`grep '^ProxyPass ' $conf | egrep '(openid|register)' | wc -l`
    [ $needed -ge 2 ] || {
        echo proxy_ajp.conf is apparently missing openid configuration
        echo "   path=$conf"
        return 1
    }
    return 0
}

function pamconf {
    local moddir=${dir_sys_pam_lib}
    local confdir=${dir_sys_pam_lib}

    prob=0
    [ -e "$moddir/pam_mysql.so" ] || {
        echo Missing pam mysql module: $moddir/pam_mysql.so
        prob=1
    }

    [ -e "$confdir/vaosso_openidip" ] || {
        echo Missing pam openidip config file: $confdir/vaosso_openidip
        prob=1
    }
    [ -e "$confdir/vaosso_geteec" ] || {
        echo Missing pam config file for geteec: $confdir/vaosso_geteec
        prob=1
    }

    return $prob
}

# havecmd goob Checking for goob
# runtest checkopenssl Checking for openssl

[ -n "$1" ] && {
    getinstalldir $1
    loadprops build-default.properties $1
}

havecmd openssl 
havecmd mysql
havepath /usr/libexec/mysqld
havecmd myproxy-logon
havecmd myproxy-server

havepath ${dir_sys_grid_security}

runtest pamconf   Checking pam configuration...
runtest proxyajp  Checking mod_proxy_ajp configuration...
runtest sslconf   Checking mod_ssl configuration...

