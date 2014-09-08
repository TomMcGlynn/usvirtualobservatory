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

#include <stdio.h>

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
int svc_init(char const *svcstr);
int svc_remote_init(char *server, int port);
int svc_register(int index, char const *name, char const *sig, char const *quit);
int svc_close(int index);
int svc_closeall();
int svc_remote_closeall();
int svc_run(char const *svcstr);
int svc_send(int index, char const *cmd);
char *svc_receive(int index);
char *svc_fgets(int index);
int svc_command(int svc, char const *cmdstr);
char *svc_value(char const *ref);
int svc_getargs (char *cmd, char **cmdv);
void svc_sigset();
void svc_sighandler();
SVC *svc_struct(char const *instr);
char *svc_stripblanks(char *ptr, int len, int quotes);
int svc_free(SVC *svc);
char *svc_val(char const *structstr, char const *key, char *val);
int svc_count();
int svc_getInFd(int index);
int svc_getOutFd(int index);
int svc_getRunning(int index);
int svc_getPid(int index);


/***************************************************************************/
/* Structure and functions related to maintaining queue of remote 
 * jobs */
/***************************************************************************/
struct SV_QUEUE {

    /* list of available "processors" (= cpu for my purposes) : */
    int  nprocAlloc;  /* space allocated for procId, procSvr, procStatus */
    int  nproc;       /* number of servers derived from the list */
    char **procId;    /* name of each processor in form svrName:cpu{1-ncpu} */
    char **procSvr;   /* server name to send this to */
    int  *procStatus; /* status of this process: QUEUED => idle, 
                       * FAILED => don't send more jobs here */
    int nprocFree;    /* the number of free processors */
    int nprocRunning; /* the number of running processors */
    int nextPidx;     /* the index of the next processor that should get
                       * a job */

    /* list of jobs: */
    int    qlenAlloc; /* space allocated for qsvr, qcmd, qret, qstatus */
    int    qlen;      /* number of jobs */
    int   *qproc;     /* index of processor running the job */
    int   *qSlIdx;    /* svclib index of the job's process */
    int   *qSlOutfd;  /* svclib output file descriptor of the job's process */
    char **qcmd;      /* command this job is running */
    char **qret;      /* string returned by this job */
    int   *qstatus;   /* status of the job (see SV_QSTAT in svQueue.h) */
    int    nextQidx;  /* index of next job to submit */
    int    nComplete; /* number of jobs completed */

    /* error messages */
    int nErrors;      /* number of errors/failures we've seen */
    char **emsgs;     /* list of error messages */

};


int svQueueInit(struct SV_QUEUE *myq, char *configFilename, char *errstr);
int svQueueFree(struct SV_QUEUE *myq, char *errstr);
int svQueueAdd(struct SV_QUEUE *myq, char *cmd, char *errstr);
int svQueueMonitor(struct SV_QUEUE *myq, int port, char *errstr);


#endif /* ISIS_SVC */
