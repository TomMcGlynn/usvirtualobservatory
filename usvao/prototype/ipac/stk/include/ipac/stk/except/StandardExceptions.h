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



/** \file
    \brief  Standard exception classes.
    \author Serge Monkewitz
  */
#ifndef IPAC_STK_EXCEPT_STANDARDEXCEPTIONS_H_
#define IPAC_STK_EXCEPT_STANDARDEXCEPTIONS_H_

#include "Exception.h"
#include "SystemError.h"


namespace ipac { namespace stk { namespace except {

/// Exception thrown when a format specification is violated.
IPAC_STK_EXCEPTION_TYPE(Format, Exception, ipac::stk::except::Format)

/// Exception thrown for invalid function parameters.
IPAC_STK_EXCEPTION_TYPE(InvalidParameter, Exception, ipac::stk::except::InvalidParameter)

/// Exception thrown when an invalid state is produced or encountered.
IPAC_STK_EXCEPTION_TYPE(IllegalState, Exception, ipac::stk::except::IllegalState)

/// Exception thrown for unexpectedly missing entities.
IPAC_STK_EXCEPTION_TYPE(NotFound, Exception, ipac::stk::except::NotFound)

/// Exception thrown for unsupported operations.
IPAC_STK_EXCEPTION_TYPE(NotSupported, Exception, ipac::stk::except::NotSupported)

/// Exception thrown when a quantity is out of bounds.
IPAC_STK_EXCEPTION_TYPE(OutOfBounds, Exception, ipac::stk::except::NotFound)

/// Exception thrown when a runtime error occurs.
IPAC_STK_EXCEPTION_TYPE(RuntimeError, Exception, ipac::stk::except::RuntimeError)

/// Exception thrown for illegal casts.
IPAC_STK_EXCEPTION_TYPE(TypeError, Exception, ipac::stk::except::IllegalState)

/// Exception thrown when an I/O operation fails.
IPAC_STK_SYSTEM_ERROR_TYPE(IOError, SystemError, ipac::stk::except::IOError)

/// Exception thrown when an OS call fails.
IPAC_STK_SYSTEM_ERROR_TYPE(OSError, SystemError, ipac::stk::except::OSError)

}}} // namespace ipac::stk::except

#endif // IPAC_STK_EXCEPT_STANDARDEXCEPTIONS_H_
