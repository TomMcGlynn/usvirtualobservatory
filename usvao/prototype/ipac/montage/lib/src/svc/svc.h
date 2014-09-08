/*************************************************************************

   Copyright (c) 2014 California Institute of Technology, Pasadena,
   California.    Based on Cooperative Agreement Number NCC5-626 between
   NASA and the California Institute of Technology. All rights reserved.

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

   This software was developed at the Infrared Processing and Analysis
   Center (IPAC) and the Jet Propulsion Laboratory (JPL) by Bruce
   Berriman, John Good, Joseph Jacob, Daniel S. Katz, and Anastasia
   Laity.

*************************************************************************/



/**
    \file       svc.h
    \author     <a href="mailto:jcg@ipac.caltech.edu">John Good</a>
 */

/**
    \mainpage   libsvc: Service I/O and Return Structure Handling Library
    \htmlinclude docs/svc.html
 */

/**
    \page       Structure Format
    \htmlinclude docs/struct.html
 */

#ifndef ISIS_SVC
#define ISIS_SVC

#define SVC_OK     0
#define SVC_ERROR -1

#define SVC_MAXSVC    32
#define SVC_STRLEN  4096
#define SVC_SVCCNT   128

typedef struct
{
   int  nalloc;
   int  count;
   char **key;
   char **val;
}
   SVC;

void set_apputil_debug(int flag);
void svc_debug(FILE *stream);
void svc_check();
int svc_init(char *svcstr);
int svc_register(int index, char *name, char *sig, char *quit);
int svc_close(int index);
int svc_closeall();
int svc_run(char * svcstr);
int svc_send(int index, char *cmd);
char *svc_receive(int index);
char *svc_fgets(int index);
int svc_command(int svc, char *cmdstr);
char *svc_value(char *ref);
int svc_getargs (char *cmd, char **cmdv);
void svc_sigset();
void svc_sighandler();
SVC *svc_struct(char *instr);
char *svc_stripblanks(char *ptr, int len, int quotes);
int svc_free(SVC *svc);
char *svc_val(char *structstr, char *key, char *val);

#endif /* ISIS_SVC */
