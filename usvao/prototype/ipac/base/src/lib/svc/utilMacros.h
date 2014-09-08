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



#ifndef UTILITY_MACROS_H
#define UTILITY_MACROS_H

#include <limits.h>

#define RET_OK 1
#define RET_ERR (-1)


/* In all the macros below _ERRSTR is assumed to have space
 * allocated to hold an error string, if needed. */

/* Macros defined in this file:
 * DUMP_RETURN
 * NULL_ERROR
 * ERROR_IF_NULL
 * UTIL_MALLOC
 * UTIL_CALLOC
 * UTIL_REALLOC
 * STR_TO_D
 * STR_TO_I
 * UTIL_COPY
 */

/****************************************************************************
 * Macro to save the string _COMMENT to the _ERRSTR and return RET_ERR
 * 
 * * _COMMENT should be non-null (usually a quoted string)
 * * _ERRSTR should have space allocated to hold an error return string.
 ****************************************************************************/
#define DUMP_RETURN(_ERRSTR, _COMMENT) {        \
        strcpy(_ERRSTR, _COMMENT);              \
        return(RET_ERR);                        \
    }


/*****************************************************************************
 * Macros for NULL pointer errors.  NULL_ERROR prints the name of the 
 * function to an error string, UTIL_ERROR_IF_NULL checks to see if the 
 * passed _PTR is null and returns an error if it is.
 *
 * * _ERRSTR should have space allocated to hold an error return string.
 *****************************************************************************/
#define NULL_ERROR(_ERRSTR) {                                           \
        sprintf(_ERRSTR, "Null pointer passed to %s", __FUNCTION__);    \
        return(RET_ERR);                                                \
    }

#define UTIL_ERROR_IF_NULL(_PTR, _ERRSTR) {                     \
        if (!(_PTR)) {                                          \
            sprintf(_ERRSTR, "Null pointer '%s' passed to %s",  \
                    #_PTR, __FUNCTION__);                       \
            return(RET_ERR);                                    \
        }                                                       \
    }


/**************************************************************************** 
 * Macros to allocate _LEN elements of size _NAME[0] to the 
 * array _NAME.  Three forms: malloc, calloc and realloc.
 * 
 * * _NAME should be a NULL pointer
 * * _LEN should be > 0
 * * _ERRSTR should have space allocated to hold an error return string.
 ***************************************************************************/
/* to debut, insert this line: printf("%s %d\n", __FILE__, __LINE__); */

#define UTIL_MALLOC(_NAME, _LEN, _ERRSTR) {                             \
        int _len = _LEN;                                                \
        if (_len < 0) {                                                 \
            sprintf(_ERRSTR, "'%d' is an invalid array length", _len);    \
            return(RET_ERR);                                            \
        }                                                               \
        if (!((_NAME) = malloc(_len*sizeof(*(_NAME))))) {               \
            sprintf(_ERRSTR, "Cannot allocate array of length %d", _len); \
            return(RET_ERR);                                            \
        }                                                               \
    }

#define UTIL_CALLOC(_NAME, _LEN, _ERRSTR) {                             \
        int _len = _LEN;                                                \
        if (_len < 0) {                                                 \
            sprintf(_ERRSTR, "'%d' is an invalid array length", _len);    \
            return(RET_ERR);                                            \
        }                                                               \
        if (!((_NAME) = calloc(_len, sizeof(*(_NAME))))) {              \
            sprintf(_ERRSTR, "Cannot allocate array of length %d", _len); \
            return(RET_ERR);                                            \
        }                                                               \
    }

#define UTIL_REALLOC(_NAME, _LEN, _ERRSTR) {                            \
        int _len = _LEN;                                                \
        if (_len < 0) {                                                 \
            sprintf(_ERRSTR, "'%d' is an invalid array length", _len);    \
            return(RET_ERR);                                            \
        }                                                               \
        if (!((_NAME) = realloc((_NAME), _len*sizeof(*(_NAME))))) {     \
            sprintf(_ERRSTR, "Cannot allocate array of length %d", _len); \
            return(RET_ERR);                                            \
        }                                                               \
    }


/*****************************************************************************
 * Macros to do conversion from string to double (STR_TO_D) and string to 
 * integer (STR_TO_I) with checking for any inappropriate characters.
 * 
 * * _TGT is a variable of the appropriate type
 * * _INSTR is a non-null input string to convert
 * * _ERRSTR should have space allocated to hold an error return string.
 *****************************************************************************/
#define STR_TO_D(_TGT, _INSTR, _ERRSTR) {                               \
        UTIL_ERROR_IF_NULL(_INSTR, _ERRSTR);                            \
        char *_ptr;                                                     \
        double _dv = strtod(_INSTR, &_ptr);                             \
        if ((_ptr - _INSTR) < strlen(_INSTR)) {                         \
            sprintf(_ERRSTR,                                            \
                    "%s: '%s' is not a valid value (should be a float).", \
                    #_TGT, _INSTR);                                     \
            return(RET_ERR);                                            \
        }                                                               \
        else _TGT = _dv;                                                \
    }

#define STR_TO_I(_TGT, _INSTR, _ERRSTR) {                               \
        char *_ptr;                                                     \
        long int _iv = strtol(_INSTR, &(_ptr), 10);                     \
        if (_iv > INT_MAX) {                                            \
            sprintf(_ERRSTR,                                            \
                    "%s: '%s' is too large a value (should be less than %d).", \
                    #_TGT, _INSTR, INT_MAX);                            \
            return(RET_ERR);                                            \
        }                                                               \
        if ((_ptr - _INSTR) < strlen(_INSTR)) {                         \
            sprintf(_ERRSTR,                                            \
                    "%s: '%s' is not a valid value (should be an integer).", \
                    #_TGT, _INSTR);                                     \
            return(RET_ERR);                                            \
        }                                                               \
        else _TGT = _iv;                                                \
    }

/*****************************************************************************
 * Macro to allocate space to the char *_TGT and copy non-null _INSTR to it
 *
 * * _TGT is a char *
 * * _INSTR is a non-null input string (char *) to copy
 * * _ERRSTR should have space allocated to hold an error return string.
 *****************************************************************************/
#define UTIL_COPY(_TGT, _INSTR, _ERRSTR) {                      \
        /*UTIL_ERROR_IF_NULL(_INSTR, _ERRSTR);                  */      \
        UTIL_MALLOC((_TGT), (strlen(_INSTR) + 1), _ERRSTR);     \
        strcpy(_TGT, _INSTR);                                   \
    }


#endif
