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




/***************************************************************************/
/* svQueue.c
 *
 * Functions for initializing, submitting, and monitoring a set of jobs
 * on a set of machines.
 *
 * ************
 * *** NOTE ***
 * ************
 *
 * Although these routines are part of the svclib and make use of it,
 * this is not currently configured to manage an interactive queue.  Each
 * process receives an instruction, processes, and returns.
 *
 * Structures
 * ----------
 * 
 * SV_QUEUE tracks the processors and jobs and their respective states.
 *
 * Structure members:
 *
 *   // list of available "processors" (= cpu for my purposes) : 
 *   int  nprocAlloc;  // space allocated for procId, procSvr, procStatus 
 *   int  nproc;       // number of servers derived from the list 
 *   char **procId;    // name of each processor in form svrName:cpu{1-ncpu} 
 *   char **procSvr;   // server name to send this to 
 *   int  *procStatus; // status of this process: QUEUED => idle, 
 *                     //    FAILED => don't send more jobs here 
 *   int nprocFree;    // the number of free processors 
 *   int nprocRunning; // the number of running processors 
 *   int nextPidx;     // the index of the next processor that should get
 *                     // a job 
 *
 *   // list of jobs: 
 *   int    qlenAlloc; // space allocated for qsvr, qcmd, qret, qstatus 
 *   int    qlen;      // number of jobs 
 *   int   *qproc;     // index of processor running the job 
 *   int   *qSlIdx;    // svclib index of the job's process 
 *   int   *qSlOutfd;  // svclib output file descriptor of the job's process 
 *   char **qcmd;      // command this job is running 
 *   char **qret;      // string returned by this job 
 *   int   *qstatus;   // status of the job (see SV_QSTAT in svQueue.h) 
 *   int    nextQidx;  // index of next job to submit 
 *
 * 
 * Routines (public)
 * --------
 * int svQueueInit(struct SV_QUEUE *myq, char *configFilename, char *errstr)
 * 
 *   This routine reads from a configuration file name and allocates space 
 *   and sets values for the procId, procSvr and procStatus elements of 
 *   the SV_QUEUE structure.
 * 
 *   The values are read from configFilename, which should contain a line 
 *   for each remote machine in the following format:
 *
 *     <fully qualified machine name> <ncore> <nproc> <ncpu>
 *
 *   An example is 
 *
 *     bacchus.ipac.caltech.edu 2 4 8
 *
 *
 *   On success, the function returns RET_OK (1), on error RET_ERR (-1) is
 *   returned and a message is placed in "errstr".
 *
 * int svQueueFree(struct SV_QUEUE *myq, char *errstr)
 *  
 *   This routine releases memory allocated to SV_QUEUE.  To avoid memory
 *   leaks, call this function after svMonitor completes.
 *
 * int svQueueAdd(struct SV_QUEUE *myq, char *cmd, char *errstr)
 *
 *   This function adds the command "cmd" to the end of the queue.  If needed,
 *   memory will be allocated to qproc, qSlIdx, qSlFdout, qcmd, qret and
 *   qstatus.  The job is not submitted at the time this function is called.
 * 
 *
 * int svQueueMonitor(struct SV_QUEUE *myq, int port, char *errstr)
 *
 *   This function does the bulk of the work: submits, monitors, and
 *   collects results of the jobs in the queue.  The return strings are
 *   stored in "qret".
 *
 *   Jobs will be submitted to remote servers (svServer) presumed to be 
 *   listening on the input "port" on the machines specified in the
 *   configuration file used to initialize SV_QUEUE.
 *
 *   A "select" monitors the "fromexec" file descriptors for each of the
 *   service calls associated with a process.  When we have something to 
 *   read on any fd, we loop over all jobs in the queue and investigate
 *   those with the status "Q_RUNNING".  If FD_ISSET for that job, we
 *   read the return string and determine if the job succeeded or failed.
 *   Otherwise, continue to the next job.
 *
 *   If a job fails (returns a message not in [struct status=....] format,
 *   or with status != OK), the procStatus of the processor to which it was
 *   submitted will be set to Q_FAILED and the number of available processors
 *   will be reduced by one.  In addition, the job will be resubmitted to the
 *   end of the queue.
 *
 *   In the event that all jobs fail and no non-failed processors are 
 *   available, the function will return with an error stating that no
 *   processors are functioning.
 *
 *   Note that there is no timeout set on returns from processes: we
 *   rely on the svClient to return an error if a timeout occurs.  If
 *   the svClient itself dies, the service that owns it will return an error.
 * 
 *   This routine will return with success (RET_OK) when all jobs submitted 
 *   are completed with success.
 *
 */
/***************************************************************************/

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <errno.h>
#include <unistd.h>
#include <ctype.h>

#include <sys/select.h>
#include <utilMacros.h>

#include <svc.h>     /* general svclib header */

int svqdebug = 1;
FILE *svqdebugfp = NULL;

/* Formerly in svQueue.h, but doesn't seem worth it now: */
#define BUFFLEN 4096
#define BLOCK_SIZE 8
#define N_VALS_IN_CONFIG 4 /* number of values we expect on a config file ln */

typedef enum {
    Q_QUEUED, 
    Q_RUNNING, 
    Q_SUCCESS,
    Q_FAILURE,
} SV_QSTAT;


/* routine to append an error to the list */
int svQueueAddError(struct SV_QUEUE *myq, char *msg, char *errstr) {
    myq->nErrors++;
    if (myq->emsgs) {
        UTIL_REALLOC(myq->emsgs, myq->nErrors, errstr);
    }
    else {
        UTIL_CALLOC(myq->emsgs, myq->nErrors, errstr);
    }

    /* make sure we aren't looking at two concatenated messages */
    char *s, *end = NULL;
    UTIL_COPY(s, msg, errstr);
    if ((end = strstr(s, "\"]")) != NULL) {
        end[0] = '\0';
    }
    myq->emsgs[myq->nErrors-1] = s;

    return(RET_OK);
}

/* read available processor list and construct queue structure */
int svQueueInit(struct SV_QUEUE *myq, char *configFilename, char *errstr) {

    if (!myq || !configFilename) NULL_ERROR(errstr);

    int i, j, off, strOff, len;
    FILE *fp;
    char buffer[BUFFLEN];
    char *svr, *server=NULL;
    int ncore, nproc, ncpu;


    if (svqdebug) {
        char fname[500];
        pid_t pid = getpid();
        sprintf(fname, "debugQueue.%d.txt", (int)pid);
        if (!(svqdebugfp = fopen(fname, "w"))) {
            sprintf(errstr, "Can't open debug file");
            return(RET_ERR);
        }
    }

    /* initialize all to 0 */
    memset(myq, 0, sizeof(*myq));

    if (!(fp = fopen(configFilename, "r"))) {
        sprintf(errstr, "Cannot open server config file '%s'", 
                configFilename);
        return(RET_ERR);
    }
    
    /* array to hold pointers to server, ncore, nproc and ncpu */
    char *entries[N_VALS_IN_CONFIG];

    int line = 0;
    while (fgets(buffer, BUFFLEN, fp)) {
        line++;
        off = 0;
        strOff = 0;
        len = strlen(buffer);

        /* strip off the terminating newline: */
        if (buffer[len-1] == '\n') buffer[len-1] = '\0';
        len = strlen(buffer);

        while (isspace(buffer[strOff])) strOff++;
        if (strOff < len) entries[off++] = &(buffer[strOff]);
        for (i = strOff; i < len; i++) {
            if (isspace(buffer[i])) {
                while (isspace(buffer[i])) {
                    buffer[i] = '\0';
                    i++;
                }
                if (i < len) {
                    entries[off++] = &(buffer[i]);
                }
            }
        }
        /* if this was an empty line, skip it */
        if (off == 0) continue;

        /* if there were values, but not enough, return an error: */
        if (off < N_VALS_IN_CONFIG) {
            sprintf(errstr, 
                    "ServerConfigFile: "
                    "Only %d values found on line %d, "
                    "expected %d", off, line, 
                    N_VALS_IN_CONFIG);
            fclose(fp);
            return(RET_ERR);                    
        }

        svr = entries[0];
        STR_TO_I(ncore, (entries[1]), errstr);
        if (ncore <= 0) {
            sprintf(errstr, 
                    "ServerConfigFile, line %d: "
                    "invalid value '%d' for NumberOfCores",
                    line, ncore);
            fclose(fp);
            return(RET_ERR);
        }
        STR_TO_I(nproc, (entries[2]), errstr);
        if (nproc <= 0 || nproc < ncore) {
            sprintf(errstr, 
                    "ServerConfigFile, line %d: "
                    "invalid value '%d' for NumberOfProcessors",
                    line, nproc);
            fclose(fp);
            return(RET_ERR);
        }
        STR_TO_I(ncpu, (entries[3]), errstr);
        if (ncpu <= 0 || ncpu < nproc) {
            sprintf(errstr, 
                    "ServerConfigFile, line %d: "
                    "invalid value '%d' for NumberOfCPUs",
                    line, ncpu);
            fclose(fp);
            return(RET_ERR);
        }
            
        /* now move server string to a less variable place: */
        UTIL_COPY(server, svr, errstr);

        for (i = 0; i < ncpu; i++) {
            sprintf(buffer, "%s:%d", server, i);

            if (myq->nproc >= myq->nprocAlloc) {
                myq->nprocAlloc += BLOCK_SIZE;
                if (myq->nproc) {
                    UTIL_REALLOC(myq->procId, myq->nprocAlloc, errstr);
                    UTIL_REALLOC(myq->procSvr, myq->nprocAlloc, errstr);
                    UTIL_REALLOC(myq->procStatus, myq->nprocAlloc, errstr);
                    for (j = myq->nproc; j < myq->nprocAlloc; j++) {
                        myq->procId[j] = NULL;
                        myq->procSvr[j] = NULL;
                        myq->procStatus[j] = Q_QUEUED;
                    }
                }
                else {
                    UTIL_CALLOC(myq->procId, myq->nprocAlloc, errstr);
                    UTIL_CALLOC(myq->procSvr, myq->nprocAlloc, errstr);
                    UTIL_CALLOC(myq->procStatus, myq->nprocAlloc, errstr);
                }
            }

            UTIL_COPY(myq->procId[myq->nproc], buffer, errstr);
            UTIL_COPY(myq->procSvr[myq->nproc], server, errstr);
            myq->procStatus[myq->nproc] = Q_QUEUED;
            myq->nproc++;
        }
        if (server) {
            free(server); 
            server = NULL;
        }
    }
    fclose(fp);

    myq->nprocFree = myq->nproc;

    if (myq->nproc <= 0) {
        sprintf(errstr, "ServerConfigFile: No processors found in config file");
        return(RET_ERR);
    }

    int tmp = RET_OK;
    return(tmp);

}

int svQueueFree(struct SV_QUEUE *myq, char *errstr) {

    if (!myq) return(RET_OK);

    int i;
    for (i = 0; i < myq->nproc; i++) {
        if (myq->procId && myq->procId[i]) free(myq->procId[i]);
        if (myq->procSvr && myq->procSvr[i]) free(myq->procSvr[i]);
    }
    if (myq->procId) free(myq->procId);
    if (myq->procSvr) free(myq->procSvr);
    if (myq->procStatus) free(myq->procStatus);

    for (i = 0; i < myq->qlen; i++) {
        if (myq->qcmd && myq->qcmd[i]) free(myq->qcmd[i]);
        if (myq->qret && myq->qret[i]) free(myq->qret[i]);
    }
    if (myq->qproc) free(myq->qproc);
    if (myq->qSlIdx) free(myq->qSlIdx);
    if (myq->qSlOutfd) free(myq->qSlOutfd);
    if (myq->qcmd) free(myq->qcmd);
    if (myq->qret) free(myq->qret);
    if (myq->qstatus) free(myq->qstatus);

    if (myq->emsgs) {
        for (i = 0; i < myq->nErrors; i++) {
            free(myq->emsgs[i]);
        }
        free(myq->emsgs);
    }

    memset(myq, 0, sizeof(struct SV_QUEUE));

    //svc_closeall();

    return(RET_OK);
}

/* append a command to the end of the queue. */
int svQueueAdd(struct SV_QUEUE *myq, char *cmd, char *errstr) {

    int j;

    if (!myq || !cmd) NULL_ERROR(errstr);

    if (myq->qlen >= myq->qlenAlloc) {
        myq->qlenAlloc += BLOCK_SIZE;
        if (myq->qlen) {
            UTIL_REALLOC(myq->qproc, myq->qlenAlloc, errstr);
            UTIL_REALLOC(myq->qSlIdx, myq->qlenAlloc, errstr);
            UTIL_REALLOC(myq->qSlOutfd, myq->qlenAlloc, errstr);
            UTIL_REALLOC(myq->qcmd, myq->qlenAlloc, errstr);
            UTIL_REALLOC(myq->qret, myq->qlenAlloc, errstr);
            UTIL_REALLOC(myq->qstatus, myq->qlenAlloc, errstr);
            for (j = myq->qlen; j < myq->qlenAlloc; j++) {
                myq->qproc[j] = 0;
                myq->qSlIdx[j] = 0;
                myq->qSlOutfd[j] = 0;
                myq->qcmd[j] = NULL;
                myq->qret[j] = NULL;
                myq->qstatus[j] = Q_QUEUED;
            }
        }
        else {
            UTIL_CALLOC(myq->qproc, myq->qlenAlloc, errstr);
            UTIL_CALLOC(myq->qSlIdx, myq->qlenAlloc, errstr);
            UTIL_CALLOC(myq->qSlOutfd, myq->qlenAlloc, errstr);
            UTIL_CALLOC(myq->qcmd, myq->qlenAlloc, errstr);
            UTIL_CALLOC(myq->qret, myq->qlenAlloc, errstr);
            UTIL_CALLOC(myq->qstatus, myq->qlenAlloc, errstr);
        }
    }

    
    UTIL_COPY(myq->qcmd[myq->qlen], cmd, errstr);
    myq->qstatus[myq->qlen] = Q_QUEUED;

    myq->qlen++;
    
    return(RET_OK);

}


/* submit job qidx to processor pidx: */
int svQueueSubmitJob(struct SV_QUEUE *myq, int port, 
                     int qidx, int pidx, 
                     char *errstr) {

    if (svqdebugfp) {
        svc_debug(svqdebugfp);
    }

    int j;
    int failed = 0;

    /* initialize and send command to svclib: */
    myq->qSlIdx[qidx] = svc_remote_init(myq->procSvr[pidx], port);

    if ((myq->qSlIdx[qidx] < 0) ||
        !svc_getRunning(myq->qSlIdx[qidx]) ||
        (svc_send(myq->qSlIdx[qidx], myq->qcmd[qidx]) < 0)) {

        failed = 1;

        /* 
        sprintf(errstr, 
                "ServerConfigFile: error submitting process to %s on port %d",
                myq->procSvr[pidx], port);

        if (svQueueAddError(myq, errstr, errstr) != RET_OK) return(RET_ERR);
        */
        /*
        sprintf(errstr, 
                "ServerConfigFile: error submitting process to %s on port %d",
                myq->procSvr[pidx], port);
        svQueueFree(myq, errstr);
        svc_remote_closeall();
        return(RET_ERR);
        */
    }

    if (!failed) {
        if (svqdebugfp) {
            fprintf(svqdebugfp, "SENT '%s' to %d (queue item %d, proc %d)\n", 
                    myq->qcmd[qidx], myq->qSlIdx[qidx], qidx, pidx);
            fflush(svqdebugfp);
        }
        
        
        /* retrieve file descriptor to monitor: */
        myq->qSlOutfd[qidx] = svc_getOutFd(myq->qSlIdx[qidx]);
        if (myq->qSlOutfd[qidx] < 0) {
            sprintf(errstr, "Error in file descriptor from svclib");
            svQueueFree(myq, errstr);
            svc_remote_closeall();
            return(RET_ERR);
        }

        /* track index of processor: */
        myq->qproc[qidx] = pidx;

        /* set status: */
        myq->procStatus[pidx] = Q_RUNNING;
        myq->qstatus[qidx]    = Q_RUNNING;
        myq->nprocRunning++;
    }
    else {
        if (svqdebugfp) {
            fprintf(svqdebugfp, 
                    "Unable to send job %d to processor %s.  "
                    "Appending to queue.\n",
                    qidx, myq->procSvr[pidx]);
        }
        myq->procStatus[pidx] = Q_FAILURE;
        myq->qstatus[qidx] = Q_FAILURE;
        myq->nComplete++;

        if (svQueueAdd(myq, myq->qcmd[qidx], errstr) == 
            RET_ERR) {
            svQueueFree(myq, errstr);
            svc_remote_closeall();
            return(RET_ERR);
        }
    }
        
    /* next process to submit will always be the one after this: there's
     * no back-tracking and no skipping, since anything that doesn't work
     * gets added to the end of the queue. */
    myq->nextQidx++;

    /* is there an available processor? */
    for (j = 0; j < myq->nproc; j++) {
        if (myq->procStatus[j] == Q_QUEUED) {
            myq->nextPidx = j; 
            break;
        }
    }

    if (j == myq->nproc) {
        myq->nextPidx = -1;
    }

    myq->nprocFree--;

    return(RET_OK);
}

int svQueueStart(struct SV_QUEUE *myq, int port, char *errstr) {
    if (!myq) NULL_ERROR(errstr);

    int i, j;
    while ((myq->nextQidx < myq->qlen) && 
           (myq->nprocFree > 0)) {
        i = myq->nextQidx;
        j = myq->nextPidx;

        /* if "nextPidx" was updated correctly, this should be fine: */
        if ((j >= 0) && (myq->procStatus[j] == Q_QUEUED)) {
            
            if (svQueueSubmitJob(myq, port, i, j, errstr) != RET_OK) {
                svQueueFree(myq, errstr);
                svc_remote_closeall();
                return(RET_ERR);
            }
            
        }
        else {
            sprintf(errstr, "Mismatch between nextIdx and nprocFree");
            svQueueFree(myq, errstr);
            svc_remote_closeall();
            return(RET_ERR);
        }
    }

    if ((myq->nprocRunning + myq->nprocFree) == 0) {
        sprintf(errstr, "No processors are functioning at this time");
        svQueueFree(myq, errstr);
        svc_remote_closeall();
        return(RET_ERR);
    }

    return(RET_OK);
}

/* This routine will submit the first batch, then monitor them: */
int svQueueMonitor(struct SV_QUEUE *myq, int port, 
                   char *errstr) {

    int i, retval, myfd, myproc, myidx;

    char *retString=NULL, *rstat=NULL;

    int stillOpen = 0;
    fd_set readfds;

    /* do we want to pass this function a timeout? */
    /*
    struct timeval timeout;
    timeout.tv_sec = timeout_sec;
    */

    if (svQueueStart(myq, port, errstr) == RET_ERR) {
        svQueueFree(myq, errstr);
        svc_remote_closeall();
        return(RET_ERR);
    }


    /* loop until all jobs are completed.  note that myq->qlen may increase
     * as we go along if jobs fail and are re-submitted */
    while (((myq->nprocFree + myq->nprocRunning) > 0) && 
           (myq->nComplete < myq->qlen)) {
        
        FD_ZERO(&readfds);
        for (i = 0; i < myq->qlen; i++) {
            if (myq->qstatus[i] == Q_RUNNING) {
                FD_SET(myq->qSlOutfd[i], &readfds);
            }
        }
        
        retval = select(FD_SETSIZE, &readfds, NULL, NULL, NULL);
        if (svqdebugfp) {
            fprintf(svqdebugfp, "Select returned %d\n", retval);
            fflush(svqdebugfp);
        }
        if (retval > 0) {

            /*
            for (i = 0; i < myq->qlen; i++) {
                myfd = myq->qSlOutfd[i];
                if (svqdebugfp) {
                fprintf(svqdebugfp, "result for %d? %d\n", 
                        i, FD_ISSET(myfd, &readfds));
                }
            }
            */

            /* go through each item in the queue:
             * - if it's completed with errors, note it and add to the end
             * - if it's completed with success, note and increment nCompleted
             * - if it's not running yet, see if there's a processor free 
             */

            for (i = 0; i < myq->qlen; i++) {

                myproc = myq->qproc[i];
                myfd = myq->qSlOutfd[i];
                myidx = myq->qSlIdx[i];
                stillOpen = 0;
                if (svqdebugfp) {
                    fprintf(svqdebugfp, "JOB %d [svidx = %d] STATUS: ", i, myidx);
                    fflush(svqdebugfp);
                }

                if (myq->qstatus[i] == Q_RUNNING) {

                    stillOpen = (svc_getPid(myidx) > 0 ? 1:0);
                    if (!FD_ISSET(myfd, &readfds) && stillOpen) {
                        if (svqdebugfp) {
                            fprintf(svqdebugfp, "STILL RUNNING\n");
                            fflush(svqdebugfp);
                        }
                    }
                    else {
                        retString = NULL;
                        /* we're only here if fd_isset or !stillOpen: */
                        if (FD_ISSET(myfd, &readfds)) {
                            if (svqdebugfp) {
                                fprintf(svqdebugfp, "RESULT: ");
                                fflush(svqdebugfp);
                            }

                            if (stillOpen) {
                                /* read return string */
                                retString = svc_receive(myidx);
                            }
                        }
                        if (!stillOpen || 
                            (retString && 
                             (!(rstat = svc_value("stat")) || 
                              strcmp(rstat, "OK")))) {
                            if (svqdebugfp) {
                                fprintf(svqdebugfp, "FAILURE\n");
                                if (retString) {
                                    fprintf(svqdebugfp, 
                                            "Command line %d returned "
                                            "unexpected result "
                                            "on proc %d: %s\n",
                                            i, myproc, retString);
                                    if (svQueueAddError(myq, 
                                                        svc_value("msg"), 
                                                        errstr) 
                                        != RET_OK) {
                                        svQueueFree(myq, errstr);
                                        svc_remote_closeall();
                                        if (svqdebugfp) {
                                            fprintf(svqdebugfp, "ERROR: %s\n",
                                                    errstr);
                                            fclose(svqdebugfp);
                                            svc_debug(NULL);
                                        }
                                        return(RET_ERR);
                                    }
        
                                }
                                else {
                                    fprintf(svqdebugfp,
                                            "Process no longer running\n");
                                }
                                fflush(svqdebugfp);
                            }
                            sprintf(errstr, 
                                    "Command line returned unexpected result");
                            
                            /* remove this server from the available list: */
                            myq->qstatus[i] = Q_FAILURE;
                            myq->procStatus[myproc] = Q_FAILURE;

                            /* nprocFree + nprocRunning = total good 
                             * processors, now down by 1: */
                            myq->nprocRunning--;


                            /* append this job to the queue: */
                            if (svQueueAdd(myq, myq->qcmd[i], errstr) == 
                                RET_ERR) {
                                svQueueFree(myq, errstr);
                                svc_remote_closeall();
                                if (svqdebugfp) {
                                    fprintf(svqdebugfp, "ERROR: %s\n",
                                            errstr);
                                    fclose(svqdebugfp);
                                    svc_debug(NULL);
                                }
                                return(RET_ERR);
                            }

                            /* increment ncompleted -- the whole 
                             * queue just got longer and this one isn't 
                             * active anymore */
                            myq->nComplete++;

                            if (svqdebugfp){ 
                                fprintf(svqdebugfp, 
                                        "qlen: %d, ncomplete: %d\n", 
                                        myq->qlen, myq->nComplete);
                                fflush(svqdebugfp);
                            }

                        }
                        else {

                            if (svqdebugfp) {
                                fprintf(svqdebugfp, 
                                        "SUCCESS: %s\n", retString);
                                fflush(svqdebugfp);
                            }

                            UTIL_COPY(myq->qret[i], retString, errstr);
                            myq->qstatus[i] = Q_SUCCESS;

                            /* update number of completed jobs: */
                            myq->nComplete++;

                            /* processor is ready to go again: */
                            myq->procStatus[myproc] = Q_QUEUED;
                            myq->nprocFree++;
                            myq->nprocRunning--;

                            myq->nextPidx = myproc;

                        }
                    }
                }
                else if (myq->qstatus[i] == Q_QUEUED) {
                    if (svqdebugfp) {
                        fprintf(svqdebugfp, "QUEUED\n");
                        fflush(svqdebugfp);
                    }
                    
                    if (myq->nextPidx >= 0) {

                        if (svqdebugfp) {
                            fprintf(svqdebugfp, "\tSUBMITTING JOB %d to %d\n",
                                    i, myq->nextPidx);
                            fflush(svqdebugfp);
                        }
                        if (svQueueSubmitJob(myq, port, i, myq->nextPidx, 
                                             errstr) 
                            != RET_OK) {
                            svQueueFree(myq, errstr);
                            svc_remote_closeall();
                            if (svqdebugfp) {
                                fprintf(svqdebugfp, "ERROR: %s\n",
                                        errstr);
                                fclose(svqdebugfp);
                                svc_debug(NULL);
                            }
                            return(RET_ERR);
                        }
                    }
                }
                else {
                    if (svqdebugfp) {
                        fprintf(svqdebugfp, "DONE\n");
                        fflush(svqdebugfp);
                    }
                }
            }
        }
        else {
            sprintf(errstr, "Error communicating with server: ");
            if (errno == EBADF) {
                strcat(errstr, "EBADF (invalid fd).");
            }
            else if (errno == EINTR) {
                strcat(errstr, "EINTR (signal caught).");
            }
            else if (errno == EINVAL) {
                strcat(errstr, "EINVAL (nfds or timeout invalid).");
            }
            else if (errno == ENOMEM) {
                strcat(errstr, "ENOMEM (out of memory).");
            }
            else {
                strcat(errstr, "Unrecognized error.");
            }
            strcat(errstr, "Please try again.");

            svQueueFree(myq, errstr);
            svc_remote_closeall();
            if (svqdebugfp) {
                fprintf(svqdebugfp, "ERROR: %s\n", errstr);
                fclose(svqdebugfp);
                svc_debug(NULL);
            }
            return(RET_ERR);
        }

        /* before looping, be sure we have a non-0 # of non-failed
         * processors: */
        if (myq->nprocRunning + myq->nprocFree <= 0) {
            if (myq->nErrors) {
                sprintf(errstr, 
                        "Remote Processing Error: %s.  Please try again.", 
                        myq->emsgs[0]);
            }
            else {
                sprintf(errstr, "No processors are functioning at this time.");
            }
            svQueueFree(myq, errstr);
            svc_remote_closeall();
            if (svqdebugfp) {
                fprintf(svqdebugfp, "ERROR: %s\n", errstr);
                fclose(svqdebugfp);
                svc_debug(NULL);
            }
            return(RET_ERR);
        }


        if (svqdebugfp) {
            fprintf(svqdebugfp, "LOOPING: %d completed of %d jobs; "
                    "%d free proc, %d running\n", myq->nComplete,
                    myq->qlen, myq->nprocFree, myq->nprocRunning);
            fflush(svqdebugfp);
        }

    }

    if (myq->nComplete < myq->qlen) {
        sprintf(errstr, "Only completed %d of %d jobs", 
                myq->nComplete, myq->qlen);
        svQueueFree(myq, errstr);
        svc_remote_closeall();
        if (svqdebugfp) {
            fprintf(svqdebugfp, "Completed %d of %d jobs.  Returning.\n",
                    myq->nComplete, myq->qlen);
            if (svqdebugfp) {
                fclose(svqdebugfp);
                svc_debug(NULL);
            }
        }
        return(RET_ERR);
    }
    else {
        if (svqdebugfp) {
            fprintf(svqdebugfp, "Completed all jobs.  Returning.\n");
        }
    }
    if (svqdebugfp) {
        fclose(svqdebugfp);
        svc_debug(NULL);
    }

    svQueueFree(myq, errstr);

    return(RET_OK);
}
