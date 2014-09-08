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
    \file       mtbl.h
    
    \author     <a href="mailto:jcg@ipac.caltech.edu">John Good</a>
    \todo       Function documentation 
 */

/**
    \mainpage   libmtbl: Mini Table Parsing Library
    \htmlinclude docs/mtbl.html
 */

#ifndef ISIS_MTBL_LIB
#define ISIS_MTBL_LIB

#define MTBL_MAXLINE 100000
#define MTBL_MAXSTR    1024
#define MTBL_MAXCOL    1024
#define MTBL_KEYLEN     256
#define MTBL_MAXKEY    1024

#define MTBL_OK      0
#define MTBL_NOFILE -2
#define MTBL_COLUMN -3
#define MTBL_RDERR  -4
#define MTBL_MALLOC -5

struct TBL_REC
{
   char  name[MTBL_MAXSTR];
   char  type[MTBL_MAXSTR];
   char  unit[MTBL_MAXSTR];
   char  nuls[MTBL_MAXSTR];
   char *dptr;
   int   endcol;
   int   colwd;
};

extern struct TBL_REC * tbl_rec;

extern char  * tbl_rec_string;
extern char  * tbl_hdr_string;
extern char  * tbl_typ_string;
extern char  * tbl_uni_string;
extern char  * tbl_nul_string;

extern int     haveType;
extern int     haveUnit;
extern int     haveNull;

extern int     tbl_headbytes;
extern int     tbl_reclen;

struct TBL_INFO
{
   struct TBL_REC *tbl_rec;
   int    nhdr;

   int    ncol;
   int    headbytes;
   int    reclen;
   int    nrec;
   int    mtbl_maxline;

   char  *tbl_hdr_string;
   char  *tbl_typ_string;
   char  *tbl_uni_string;
   char  *tbl_nul_string;

   int    haveType;
   int    haveUnit;
   int    haveNull;

   int    nkey;
   char **keystr;
   char **keyword;
   char **value;

   FILE  *tfile;
};

void  tsetlen(int maxstr);
void  tsetdebug(int debug);
int   topen(char *fname);
int   tlen();
int   tcol(char *name);
char *tinfo(int col);
int   tkeycount();
int   thdrcount();
char *tkeyname(int i);
char *tkeyval(int i);
char *tfindkey(char *key);
char *thdrline(int i);
int   tseek(int recno);
int   tread();
char *tval(int col);
int   tnull(int col);
void  tclose();
int   isBlank(char *str);
void  tclear();

struct TBL_INFO *tsave   ();
void             trestore(struct TBL_INFO *tbl_info);
void             tfree   (struct TBL_INFO *tbl_info);

#endif /* ISIS_MTBL_LIB */

