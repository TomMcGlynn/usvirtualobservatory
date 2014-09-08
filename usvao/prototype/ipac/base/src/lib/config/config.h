/*************************************************************************

   Copyright (c) 2014, California Institute of Technology, Pasadena,
   California, under cooperative agreement 0834235 between the California
   Institute of Technology and the National Science  Foundation/National
   Aeronautics and Space Administration.

   All rights reserved.

   Redistribution and use in source and binary forms, with or without
   modification, are permitted provided that the following conditions
   of this BSD 3-clause license are met:

   1. Redistributions of source code must retain the above copyright
   notice, this list of conditions and the following disclaimer.

   2. Redistributions in binary form must reproduce the above copyright
   notice, this list of conditions and the following disclaimer in the
   documentation and/or other materials provided with the distribution.

   3. Neither the name of the copyright holder nor the names of its
   contributors may be used to endorse or promote products derived from
   this software without specific prior written permission.

   THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
   "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
   LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
   A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
   HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
   SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
   LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
   DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
   THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
   (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
   OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

   This software was developed by the Infrared Processing and Analysis
   Center (IPAC) for the Virtual Astronomical Observatory (VAO), jointly
   funded by NSF and NASA, and managed by the VAO, LLC, a non-profit
   501(c)(3) organization registered in the District of Columbia and a
   collaborative effort of the Association of Universities for Research
   in Astronomy (AURA) and the Associated Universities, Inc. (AUI).

*************************************************************************/



/**
    \file       config.h
    \author     <a href="mailto:jcg@ipac.caltech.edu">John Good</a>
 */

/**
    \mainpage   libconfig: Configuration File Parameter Handling Library

    <hr>
    <center><h2>Configuration File Parameter Handling Library</h2></center>
    <p>

    <center><b>Table of Contents</b></a>
    <table border=1 cellpadding=2 width=75%>
        <tr><td>
            <center><table><tr><td>
                <ul>
                <li><a href=#description>   General Description</a></li>
                <li><a href=#control>       Library Routines</a></li>
                <li><a href=#example>       Usage Example</a></li>
                </ul>
            </td></tr></table></center>
        </td></tr>
    </table>
    </center>

    <a name=description><h2>General Description</h2></a>

    <p>
    There are always a few parameters for a system that change from
    installation to installation (the name of the host machine is an
    obvious example).  In the IRSA system, this information is kept
    in a single file (default: /irsa/isis/ISIS.config with an environment
    variable override using ISIS_CONFIG).

    <p>
    This file contains nothing more than a set of ASCII name/value pairs
    and this library is used to read these values.

    <p><hr><p>

    <a name=control><h2>Library Routines</h2></a>

    <p>
    The calls in the <tt><b>config</b></tt> library are as follows:

    <p>
    <ul>
    <li>config_init(char *)</li>
    <li>config_read(char *)</li>
    <li>config_exists(char *)</li>
    <li>config_value(char *)</li>
    <li>config_info(int, char *, char *)</li>
    <li>set_config_debug(FILE *)</li>
    </ul>

    <p><hr><p>

    <a name=example><h2>Usage Example</h2></a>

    <p>
    The following is a code snippet demonstrating the use
    of the library routines.

    <p>
    \code

    char *name, *val;
    int   i, nkey;

    nkey = config_init();

    printf("The following name/value pairs were read in:\n");

    for(i = 0; i < nkey; ++i)
    {
        config_info(i, &name, &val);
        printf("%s = %s\n", name, val);
    }
    \endcode

    <p><hr><p>

    \author     <a href="mailto:jcg@ipac.caltech.edu">John Good</a>
 */

#ifndef ISIS_CONFIG_LIB 
#define ISIS_CONFIG_LIB 


/**
    Causes debug messages to be written to the given stream. If the
    <tt>debug</tt> stream pointer is <tt>NULL</tt>, debug messages will
    be supressed.

    \param debug        the stream to write debug messages to.
 */
void set_config_debug(FILE *debug);

/**
    Reads the specified configuration file, parses it, and sets up
    an internal strucuture to hold the results. If the given name is
    <tt>NULL</tt> or empty, then the value of the <tt>ISIS_CONFIG</tt> 
    environment variable is used instead. If this environment variable 
    is itself undefined or empty, a hard-coded default filename is used 
    as a last resort.

    \param file		the name of the configuration file to read.
    \return             the number of configuration parameters and values
                        that were read in. A negative return value indicates
                        that an error occurred. 
 */
int config_init(char const * file);

/**
    Reads the specified configuration file, parses it, and sets up
    an internal strucuture to hold the results. 

    \param file         the name of the configuration file to read.
    \return             a non-zero integer if an error occurred and 
                        zero otherwise.
 */
int config_read(char const * file);

/**
    Checks to see if a configuration parameter with the given name exists. 

    \param key          the name of the configuration parameter to test 
                        the existence of.
    \return             a non-zero integer if a configuration parameter with
                        name equal to <tt>key</tt> exists and zero otherwise.
 */
int config_exists(char const * key);

/**
    Returns the value of the configuration parameter with the given name.

    \param key		the name of the configuration parameter to return
                        the value of.
    \return             the value of the configuration parameter with name
                        equal to <tt>key</tt>, or <tt>NULL</tt> if no such
                        parameter exists.
 */
char * config_value(char const * key);

/**
    \todo               Documentation
 */
char * config_expand(char const * str);

/**
    Returns the configuration parameter name and value for the parameter
    with the given index. Valid indexes range from zero up to but not including
    the count returned by the most recent call to config_init().

    \param index        the index of the configuration parameter to return
                        information about. 
    \param keyname      the name of the configuration parameter with the given index. 
    \param keyval       the value of the configuration parameter with the given index.
    \return             <tt>1</tt> if <tt>index</tt> is valid and <tt>-1</tt> otherwise.
 */
int config_info(int index, char * keyname, char * keyval);

#endif /* ISIS_CONFIG_LIB */

