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
    \file       xmlinfo.h
    \author     <a href="mailto:jcg@ipac.caltech.edu">John Good</a>
 */

/**
    \mainpage   libxmlinfo: XML File Extraction Library 

    <hr>
    <center><h2>XML File Extraction Library</h2></center>
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
    XML files (or strings) are often used to transport a data structure.
    When used in this context, the user needs simple methods to extract
    the information contained in the structure, often in a arbitrary
    sequence (<i>i.e.</i> using a DOM-like model).

    <p>
    This only really works well when the XML structure is reasonably
    compact, since it has to be stored in memory, and where user already
    knows the basic layout and is just trying to get specific values.

    <p>
    This functionality is layered on top of the <em>scew</em> library
    (which is itself layered on <em>expat</em>).  The user parses a 
    file and then asks the library to evaluate strings of the form
    <tt>abc.def</tt> where the dot notation is used to denote parent/child
    relationships. Multiple instance XML elements can be addressed
    using an array notation (e.g. <tt>abc.def[4].ghi</tt>)
    and attribute values can be obtained using parentheses (e.g.
    <tt>abc.def[3].ghi(jkl)</tt>, <tt>abc.def[3](jkl)</tt>, etc...).

    <p>
    The return is always a string (null pointer if there is no data);
    the attribute string for an attribute and the non-tagged text
    for XML elements (CDATA constructs are handled transparently;
    the user doesn't have to worry about them).

    <p>
    The library also allows the user to ask for a count for a group
    of elements having the same name.  If there are five <tt>def</tt>
    tags inside <tt>abc</tt>, asking for a count of <tt>abc.def</tt>
    will return 5.

    <p><hr><p>

    <a name=control><h2>Library Routines</h2></a>

    <p>
    The calls in the <tt><b>xmlinfo</b></tt> library are as follows :

    <p>
    <ul>
    <li>xmlinfo_init(char *)</li>
    <li>xmlinfo_close()</li>
    <li>xmlinfo_value(char *)</li>
    <li>xmlinfo_count(char *)</li>
    <li>xmlinfo_error()</li>
    </ul>

    <p><hr></p>

    <a name=example><h2>Usage Example</h2></a>

    <p>
    The following is a the code for a simple investigative XML file reader.

    <p>

    \code

    #include <stdio.h>
    #include <string.h>
    #include <xmlinfo.h>

    int main(int argc, char **argv)
    {
        int  count;
        char str[4096], *value;

        if (argc < 2)
        {
            printf("[struct stat=\"ERROR\", msg=\"Usage: %s file.xml\"]\n", argv[0]);
            fflush(stdout);
            exit(0);
        }

        if(xmlinfo_init(argv[1]) == 1)
        {
            printf("[struct stat=\"ERROR\", msg=\"%s\"]\n",
                xmlinfo_error());
            fflush(stdout);
            exit(0);
        }

        while(1)
        {
            if(gets(str) == (char *)NULL)
                break;
            if(strcmp(str, "quit") == 0)
            {
                printf("[struct stat=\"OK\", msg=\"Quitting.\"]\n");
                fflush(stdout);
                xmlinfo_close();
                exit(0);
            }
            else if(strncmp(str, "count ", 6) == 0)
            {
                count = xmlinfo_count(str+6);
                if(count < 0)
                    printf("[struct stat=\"ERROR\", msg=\"No such element.\"]\n");
                else
                    printf("[struct stat=\"OK\", count=%d]\n", count);
                fflush(stdout);
            }
            else
            {
                value = xmlinfo_value(str);
                if(value == (char *)NULL)
                {
                    printf("[struct stat=\"ERROR\", msg=\"%s\"]\n",
                    xmlinfo_error());
                    fflush(stdout);
                }
                else
                {
                    printf("[struct stat=\"OK\", value=\"%s\"]\n",
                        value);
                    fflush(stdout);
                }
            }
        }

        return 0;
    }

    \endcode

    <p><hr><p>

    \author     <a href="mailto:jcg@ipac.caltech.edu">John Good</a>
 */

#ifndef XMLINFO_H
#define XMLINFO_H

/** 
    Reads and parses the given XML file, storing the results in an internal
    DOM-like structure (queriable by xmlinfo_value(char*) and xmlinfo_count(char*)).

    \param filename     the name of the XML file to read in and parse.
    \return             <tt>0</tt> on success and <tt>1</tt> if an error occurred.
 */
int xmlinfo_init (char const *filename);

/**  
    Frees all library resources. 
 */
void  xmlinfo_close();

/**
    Returns the textual content of the XML tag with the given name. 

    \param str          the name of the tag to return the textual content of.
    \return             the textual content of the tag with name equal to
                        <tt>str</tt>, or <tt>(char *)NULL</tt> if an error 
                        occurred.
 */
char *xmlinfo_value(char const *str);

/**
    Returns the number of XML tags read in by xmlinfo_init(char *) which
    have name equal to <tt>str</tt>.

    \param str          the name of the tag to count the number of
                        instances of.
    \return	        the number of tags with name eqaul to <tt>str</tt>,
                        or <tt>-1</tt> if an error occurred.
 */
int xmlinfo_count(char const *str);

/**
    Returns a message describing the last error which occurred.

    \return 	        a message describing the last error which occurred.
 */
char *xmlinfo_error();

#endif
