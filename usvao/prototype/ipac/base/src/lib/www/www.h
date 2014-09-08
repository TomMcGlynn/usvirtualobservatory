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
    \file       www.h
    \author     <a href="mailto:jcg@ipac.caltech.edu">John Good</a>
 */

/**
    \mainpage   libwww: WWW CGI Parameter Handling Library
    \htmlinclude docs/www.html
 */

#ifndef ISIS_WWW_LIB
#define ISIS_WWW_LIB

#include <stdio.h>

#define WWW_OK 0
#define WWW_BADFOUT 1
#define WWW_BADHEAD 2
#define WWW_BADFOOT 3

void keyword_debug(FILE *dbg);

void keyword_workdir(char *workdir);

int keyword_init(int argc, char **argv);

int keyword_count();

void keyword_close();

int keyword_exists(char const *key);

char *keyword_value(char const *key);

char *keyword_value_stripped(char const *key);

char *keyword_instance(char const *key, int count);

char *keyword_filename(char const *key);

int keyword_info(int index, char **keyname, char **keyval, char **fname);

int keyword_safe_system(char const *str);

int initHTTP(FILE *fout, char const *cookiestr);

int wwwHeader(FILE *fout, char const *header, char const *title);

int keylib_initialized(void);

int wwwFooter(FILE *fout, char const *footer);

/* Utility Routines */

void unescape_url(char *url);

int is_blank(char const *s);

char *url_encode(char const *s);

char *url_decode(char const *s);

void encodeOffsetURL(char *out, int start);


#endif /* ISIS_WWW_LIB */

