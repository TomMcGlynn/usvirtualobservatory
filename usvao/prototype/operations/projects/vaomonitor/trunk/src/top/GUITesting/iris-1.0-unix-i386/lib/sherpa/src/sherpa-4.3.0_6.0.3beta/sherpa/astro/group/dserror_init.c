/*                                                                
**  Copyright (C) 2010 Smithsonian Astrophysical Observatory 
*/                                                                

/*                                                                          */
/*  This program is free software; you can redistribute it and/or modify    */
/*  it under the terms of the GNU General Public License as published by    */
/*  the Free Software Foundation; either version 2 of the License, or       */
/*  (at your option) any later version.                                     */
/*                                                                          */
/*  This program is distributed in the hope that it will be useful,         */
/*  but WITHOUT ANY WARRANTY; without even the implied warranty of          */
/*  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the           */
/*  GNU General Public License for more details.                            */
/*                                                                          */
/*  You should have received a copy of the GNU General Public License along */
/*  with this program; if not, write to the Free Software Foundation, Inc., */
/*  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.             */
/*                                                                          */


#ifndef __dserror_funcs_priv_h
#include    "dserror_funcs_priv.h"      /* for ASC error library */
#endif

/* helper function, it initializes the generic error elements */
dsErrCode dsErrInitGeneralErr(long *current_error, long *num_errors)
{
  dsErrCode group_err_stat_e = dsNOERR;

  /* Add miscellaneous errors */
  *num_errors +=
    dsErrInitHashMapElement(dsNOERR, dsNOERRSEV, dsNOERRSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsGENERICERR, dsGENERICSEV, dsGENERICSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsUNDEFERR, dsUNDEFSEV, dsUNDEFSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsERRNOTFOUNDERR, dsERRNOTFOUNDSEV, dsERRNOTFOUNDSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsDIVIDEBYZEROERR, dsDIVIDEBYZEROSEV, dsDIVIDEBYZEROSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsALLOCERR, dsALLOCSEV, dsALLOCSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsALLOC2ERR, dsALLOC2SEV, dsALLOC2STDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsALLOCOBJERR, dsALLOCOBJSEV, dsALLOCOBJSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsINITLIBERR, dsINITLIBSEV, dsINITLIBSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsNULLPTRERR, dsNULLPTRSEV, dsNULLPTRSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsNULLSTRINGERR, dsNULLSTRINGSEV, dsNULLSTRINGSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsNOTIMPLMERR, dsNOTIMPLMSEV, dsNOTIMPLMSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsBOUNDSERR, dsBOUNDSSEV, dsBOUNDSSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsAUTONAMEERR, dsAUTONAMESEV, dsAUTONAMESTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsOBJSELFASSIGNERR, dsOBJSELFASSIGNSEV, dsOBJSELFASSIGNSTDMSG,
                            current_error, &group_err_stat_e);

/* Column and keyword errors in data files - mostly FITS, StSCI tables, or QPOES
 */

  *num_errors +=
    dsErrInitHashMapElement(dsUNSUPORTTYPEERR, dsUNSUPORTTYPESEV, dsUNSUPORTTYPESTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsCOLUMNTYPEERR, dsCOLUMNTYPESEV, dsCOLUMNTYPESTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsKEYWORDTYPEERR, dsKEYWORDTYPESEV, dsKEYWORDTYPESTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsFINDCOLUMNERR, dsFINDCOLUMNSEV, dsFINDCOLUMNSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsFINDKEYWORDFERR, dsFINDKEYWORDFSEV, dsFINDKEYWORDFSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsFINDKEYWORDWERR, dsFINDKEYWORDWSEV, dsFINDKEYWORDWSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsCREATECOLUMNERR, dsCREATECOLUMNSEV, dsCREATECOLUMNSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsCREATEKEYWORDERR, dsCREATEKEYWORDSEV, dsCREATEKEYWORDSTDMSG,
                            current_error, &group_err_stat_e);

/* file I/O errors */

  *num_errors +=
    dsErrInitHashMapElement(dsOPENFILEFERR, dsOPENFILEFSEV, dsOPENFILEFSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsOPENFILEWERR, dsOPENFILEWSEV, dsOPENFILEWSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsINPUTEXISTSFERR, dsINPUTEXISTSFSEV, dsINPUTEXISTSFSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsINPUTEXISTSWERR, dsINPUTEXISTSWSEV, dsINPUTEXISTSWSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsREADFILEFERR, dsREADFILEFSEV, dsREADFILEFSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsREADFILEWERR, dsREADFILEWSEV, dsREADFILEWSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsCLOBBERFILEERR, dsCLOBBERFILESEV, dsCLOBBERFILESTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsCREATEFILEERR, dsCREATEFILESEV, dsCREATEFILESTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsOUTPUTEXISTSERR, dsOUTPUTEXISTSSEV, dsOUTPUTEXISTSSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsGETSTATUSERR, dsGETSTATUSSEV, dsGETSTATUSSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsTMPFILEERR, dsTMPFILESEV, dsTMPFILESTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsNOROWSERR, dsNOROWSSEV, dsNOROWSSTDMSG,
                            current_error, &group_err_stat_e);

/* Data Model related errors */

  *num_errors +=
    dsErrInitHashMapElement(dsDMGENERICERR, dsDMGENERICSEV, dsDMGENERICSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsDMBLOCKCREATEERR, dsDMBLOCKCREATESEV, dsDMBLOCKCREATESTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsDMBLOCKOPENERR, dsDMBLOCKOPENSEV, dsDMBLOCKOPENSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsDMTABLEOPENERR, dsDMTABLEOPENSEV, dsDMTABLEOPENSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsDMSETVALUEERR, dsDMSETVALUESEV, dsDMSETVALUESTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsDMGETVALUEERR, dsDMGETVALUESEV, dsDMGETVALUESTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsDMOPENIMAGEERR, dsDMOPENIMAGESEV, dsDMOPENIMAGESTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsDMCREATEIMAGEERR, dsDMCREATEIMAGESEV, dsDMCREATEIMAGESTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsDMREADIMAGEERR, dsDMREADIMAGESEV, dsDMREADIMAGESTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsDMWRITEIMAGEERR, dsDMWRITEIMAGESEV, dsDMWRITEIMAGESTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsDMCLOSEIMAGEERR, dsDMCLOSEIMAGESEV, dsDMCLOSEIMAGESTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsDMSUBSPCREATEERR, dsDMSUBSPCREATESEV, dsDMSUBSPCREATESTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsDMSSCREATECPTERR, dsDMSSCREATECPTSEV, dsDMSSCREATECPTSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsDMSSSETELEMENTERR, dsDMSSSETELEMENTSEV, dsDMSSSETELEMENTSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsDMSETOPENERR, dsDMSETOPENSEV, dsDMSETOPENSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsDMSETKERNELERR, dsDMSETKERNELSEV, dsDMSETKERNELSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsDMSETCREATEERR, dsDMSETCREATESEV, dsDMSETCREATESTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsDMSUBSPREADERR, dsDMSUBSPREADSEV, dsDMSUBSPREADSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsDMSUBSPOPENERR, dsDMSUBSPOPENSEV, dsDMSUBSPOPENSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsDMDESCRIPOPENERR, dsDMDESCRIPOPENSEV, dsDMDESCRIPOPENSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsDMPUTROWERR, dsDMPUTROWSEV, dsDMPUTROWSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsDMREADCOLERR, dsDMREADCOLSEV, dsDMREADCOLSTDMSG,
                            current_error, &group_err_stat_e);
/* Parameter file errors */
  *num_errors +=
    dsErrInitHashMapElement(dsOPENPARAMFERR, dsOPENPARAMFSEV, dsOPENPARAMFSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsOPENPARAMWERR, dsOPENPARAMWSEV, dsOPENPARAMWSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsFINDPARAMFERR, dsFINDPARAMFSEV, dsFINDPARAMFSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsFINDPARAMWERR, dsFINDPARAMWSEV, dsFINDPARAMWSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsPARAMVALUEFERR, dsPARAMVALUEFSEV, dsPARAMVALUEFSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsPARAMVALUEWERR, dsPARAMVALUEWSEV, dsPARAMVALUEWSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsPARAMFORMATERR, dsPARAMFORMATSEV, dsPARAMFORMATSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsPARAMNULLERR, dsPARAMNULLSEV, dsPARAMNULLSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsPARAMTOOLERR, dsPARAMTOOLSEV, dsPARAMTOOLSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsPARAMMISSMERR, dsPARAMMISSMSEV, dsPARAMMISSMSTDMSG,
                            current_error, &group_err_stat_e);
/* Errors in using the asc fitting engine */

  *num_errors +=
    dsErrInitHashMapElement(dsAFFITERR, dsAFFITSEV, dsAFFITSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsAFMODELVALERR, dsAFMODELVALSEV, dsAFMODELVALSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsAFPARAMERR, dsAFPARAMSEV, dsAFPARAMSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsAFSETCONFERR, dsAFSETCONFSEV, dsAFSETCONFSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsAFSETDATA1DERR, dsAFSETDATA1DSEV, dsAFSETDATA1DSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsAFSETMETHODERR, dsAFSETMETHODSEV, dsAFSETMETHODSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsAFSETMODELERR, dsAFSETMODELSEV, dsAFSETMODELSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsAFSETPARAMERR, dsAFSETPARAMSEV, dsAFSETPARAMSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsAFSETRANGEERR, dsAFSETRANGESEV, dsAFSETRANGESTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsAFSETSTATERR, dsAFSETSTATSEV, dsAFSETSTATSTDMSG,
                            current_error, &group_err_stat_e);
/* Timing errors */

  *num_errors +=
    dsErrInitHashMapElement(dsGETGTIERR, dsGETGTISEV, dsGETGTISTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsGETTIMEERR, dsGETTIMESEV, dsGETTIMESTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsTIMESORTERR, dsTIMESORTSEV, dsTIMESORTSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsTIMESORTWERR, dsTIMESORTWSEV, dsTIMESORTWSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsFILEORDERERR, dsFILEORDERSEV, dsFILEORDERSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsTIMERNGERR, dsTIMERNGSEV, dsTIMERNGSTDMSG,
                            current_error, &group_err_stat_e);
/* stack lib errors */
  *num_errors +=
    dsErrInitHashMapElement(dsSTKGENERICERR, dsSTKGENERICSEV, dsSTKGENERICSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsSTKBLDERR, dsSTKBLDSEV, dsSTKBLDSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsSTKEMPTYERR, dsSTKEMPTYSEV, dsSTKEMPTYSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsSTKREADERR, dsSTKREADSEV, dsSTKREADSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsSTKCLOSEERR, dsSTKCLOSESEV, dsSTKCLOSESTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsSTKADDERR, dsSTKADDSEV, dsSTKADDSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsSTKDELERR, dsSTKDELSEV, dsSTKDELSTDMSG,
                            current_error, &group_err_stat_e);


  /* CALDB errors */

  *num_errors +=
    dsErrInitHashMapElement( dsCALDBGENFERR, dsCALDBGENFSEV, dsCALDBGENFMSG,
                            current_error, &group_err_stat_e);

  *num_errors +=
    dsErrInitHashMapElement( dsCALDBGENWERR, dsCALDBGENWSEV, dsCALDBGENWMSG,
                            current_error, &group_err_stat_e);


  return group_err_stat_e;
}

/* helper function, it initializes the Pipe/Tools instrument errors */
dsErrCode dsErrInitPTInstrumentErr(long *current_error, long *num_errors)
{
  dsErrCode group_err_stat_e = dsNOERR;

  *num_errors +=
    dsErrInitHashMapElement(dsAPEEMPTYSTACKERR, dsAPEEMPTYSTACKSEV, dsAPEEMPTYSTACKSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsAPEGRADEFILEERR, dsAPEGRADEFILESEV, dsAPEGRADEFILESTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsAPEGRADEFILESZERR, dsAPEGRADEFILESZSEV, dsAPEGRADEFILESZSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsAPEFOAFPDNEERR, dsAPEFOAFPDNESEV, dsAPEFOAFPDNESTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsAPEWINSIZEERR, dsAPEWINSIZESEV, dsAPEWINSIZESTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsAPEGAINFILEERR, dsAPEGAINFILESEV, dsAPEGAINFILESTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsAPEDOPINOGAINERR, dsAPEDOPINOGAINSEV, dsAPEDOPINOGAINSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsAPEBADGAINROWERR, dsAPEBADGAINROWSEV, dsAPEBADGAINROWSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsAPEGAINMISSERR, dsAPEGAINMISSSEV, dsAPEGAINMISSSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsAPEGAINCCDRNGERR, dsAPEGAINCCDRNGSEV, dsAPEGAINCCDRNGSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsAPEGAINNODERNGERR, dsAPEGAINNODERNGSEV, dsAPEGAINNODERNGSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsAPETSTARTERR, dsAPETSTARTSEV, dsAPETSTARTSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsAPETSTOPERR, dsAPETSTOPSEV, dsAPETSTOPSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsAPEOBSTIMERNGERR, dsAPEOBSTIMERNGSEV, dsAPEOBSTIMERNGSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsAPEDMDATATYPEERR, dsAPEDMDATATYPESEV, dsAPEDMDATATYPESTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsAPEUNKNOWNSYSERR, dsAPEUNKNOWNSYSSEV, dsAPEUNKNOWNSYSSTDMSG,
                            current_error, &group_err_stat_e);
                            
/********************************                            
Errors added 28 Feb 2007 for acis_process_events                            
********************************/
  *num_errors +=
      dsErrInitHashMapElement(dsAPEPULSEHEIGHTERR, dsAPEPULSEHEIGHTSEV, dsAPEPULSEHEIGHTSTDMSG,
                            current_error, &group_err_stat_e);

  *num_errors +=
      dsErrInitHashMapElement(dsAPEREADERR, dsAPEREADSEV, dsAPEREADSTDMSG,
                            current_error, &group_err_stat_e);

  *num_errors +=
      dsErrInitHashMapElement(dsAPEDXYERR, dsAPEDXYSEV, dsAPEDXYSTDMSG,
                            current_error, &group_err_stat_e);

  *num_errors +=
      dsErrInitHashMapElement(dsAPEDIVZEROERR, dsAPEDIVZEROSEV, dsAPEDIVZEROSTDMSG,
                            current_error, &group_err_stat_e);

  *num_errors +=
      dsErrInitHashMapElement(dsAPETARGERR, dsAPETARGSEV, dsAPETARGSTDMSG,
                            current_error, &group_err_stat_e);

  *num_errors +=
      dsErrInitHashMapElement(dsAPENUMCOLSERR, dsAPENUMCOLSSEV, dsAPENUMCOLSSTDMSG,
                            current_error, &group_err_stat_e);

  *num_errors +=
      dsErrInitHashMapElement(dsAPEREADCTIFILEERR, dsAPEREADCTIFILESEV, dsAPeREADCTIFILESTDMSG,
                            current_error, &group_err_stat_e);

  *num_errors +=
      dsErrInitHashMapElement(dsAPEREADTGAINFILEERR, dsAPEREADTGAINFILESEV, dsAPEREADTGAINFILESTDMSG,
                            current_error, &group_err_stat_e);

  *num_errors +=
      dsErrInitHashMapElement(dsAPEPHAZEROERR, dsAPEPHAZEROSEV, dsAPEPHAZEROSTDMSG,
                            current_error, &group_err_stat_e);
                            
*num_errors +=
    dsErrInitHashMapElement(dsAPESETTRAPDENSERR, dsAPESETTRAPDENSSEV, dsAPESETTRAPDENSSTDMSG,
                            current_error, &group_err_stat_e);

  *num_errors +=
    dsErrInitHashMapElement(dsAPEMEMERR, dsAPEMEMSEV, dsAPEMEMSTDMSG,
                            current_error, &group_err_stat_e);

  *num_errors +=
    dsErrInitHashMapElement(dsAPEREADIMERR, dsAPEREADIMSEV, dsAPEREADIMSTDMSG, 
                            current_error, &group_err_stat_e);

  *num_errors +=
    dsErrInitHashMapElement(dsAPEIMTYPEERR, dsAPEIMTYPESEV, dsAPEIMTYPESTDMSG,
                            current_error, &group_err_stat_e);

  *num_errors +=
    dsErrInitHashMapElement(dsAPEIMDIMERR, dsAPEIMDIMSEV, dsAPEIMDIMSTDMSG,
                            current_error, &group_err_stat_e);

  *num_errors +=
    dsErrInitHashMapElement(dsAPEMALLOCERR, dsAPEMALLOCSEV, dsAPEMALLOCSTDMSG,
                            current_error, &group_err_stat_e);

  *num_errors +=
    dsErrInitHashMapElement(dsAPEREADTRAPDENSERR, dsAPEREADTRAPDENSSEV, dsAPEREADTRAPDENSSTDMSG, 
                            current_error, &group_err_stat_e);

  *num_errors +=
    dsErrInitHashMapElement(dsAPECCDNODEERR, dsAPECCDNODESEV, dsAPECCDNODESTDMSG, 
                            current_error, &group_err_stat_e);
/********************************                            
End errors added 28 Feb 2007                          
********************************/
                            
  *num_errors +=
    dsErrInitHashMapElement(dsAFESTACKSIZEERR, dsAFESTACKSIZESEV, dsAFESTACKSIZESTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsAFEEXPSTATSERR, dsAFEEXPSTATSSEV, dsAFEEXPSTATSSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsAFEBIASLOADERR, dsAFEBIASLOADSEV, dsAFEBIASLOADSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsAFENOBIASDATAERR, dsAFENOBIASDATASEV, dsAFENOBIASDATASTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsAFEGRDMDBIASERR, dsAFEGRDMDBIASSEV, dsAFEGRDMDBIASSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsAFEUNKNOWNMODEERR, dsAFEUNKNOWNMODESEV, dsAFEUNKNOWNMODESTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsAFEMIXEDMODEERR, dsAFEMIXEDMODESEV, dsAFEMIXEDMODESTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsAFEBIASDIFFERERR, dsAFEBIASDIFFERSEV, dsAFEBIASDIFFERSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsAFENOBIASCORRERR, dsAFENOBIASCORRSEV, dsAFENOBIASCORRSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsAFEOCCHIPRANGEERR, dsAFEOCCHIPRANGESEV, dsAFEOCCHIPRANGESTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsAFEBSCHIPRANGEERR, dsAFEBSCHIPRANGESEV, dsAFEBSCHIPRANGESTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsAFECCDRANGEERR, dsAFECCDRANGESEV, dsAFECCDRANGESTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsAFEBADPMODEERR, dsAFEBADPMODESEV, dsAFEBADPMODESTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsAFEBPCHIPRANGEERR, dsAFEBPCHIPRANGESEV, dsAFEBPCHIPRANGESTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsAFEBADPCNTERR, dsAFEBADPCNTSEV, dsAFEBADPCNTSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsAFEBADPPOSERR, dsAFEBADPPOSSEV, dsAFEBADPPOSSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsACEDTYCYCLE0ERR, dsACEDTYCYCLE0SEV, dsACEDTYCYCLE0STDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsACEDTYCYCRNGERR, dsACEDTYCYCRNGSEV, dsACEDTYCYCRNGSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsACEEVTEXPSTKSZERR, dsACEEVTEXPSTKSZSEV, dsACEEVTEXPSTKSZSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsACEINOUTSTKSZERR, dsACEINOUTSTKSZSEV, dsACEINOUTSTKSZSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsACECOLUMNAMEERR, dsACECOLUMNAMESEV, dsACECOLUMNAMESTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsACEEXPCOLUMNAMEERR, dsACEEXPCOLUMNAMESEV, dsACEEXPCOLUMNAMESTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsACEWRITEEVTERR, dsACEWRITEEVTSEV, dsACEWRITEEVTSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsACEWRITEEXPERR, dsACEWRITEEXPSEV, dsACEWRITEEXPSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsACEDTYCYCMISSERR, dsACEDTYCYCMISSSEV, dsACEDTYCYCMISSSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsACEEXPTIMEMISSERR, dsACEEXPTIMEMISSSEV, dsACEEXPTIMEMISSSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsACEEXPTIMERNGERR, dsACEEXPTIMERNGSEV, dsACEEXPTIMERNGSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsACEPBKEXTRAERR, dsACEPBKEXTRASEV, dsACEPBKEXTRASTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsACEPBKNONEERR, dsACEPBKNONESEV, dsACEPBKNONESTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsACISGTINAMESERR, dsACISGTINAMESSEV, dsACISGTINAMESSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsACISGTIDTCERR, dsACISGTIDTCSEV, dsACISGTIDTCSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsACISGTIROWERR, dsACISGTIROWSEV, dsACISGTIROWSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsAMGNOBLOCKERR, dsAMGNOBLOCKSEV, dsAMGNOBLOCKSTDMSG,
                            current_error, &group_err_stat_e);
  
  /* DESTREAK related errors (dsDS__) */

  *num_errors +=
    dsErrInitHashMapElement(dsDSINVALIDMAXERR, dsDSINVALIDMAXSEV, dsDSINVALIDMAXSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsDSEVENTQEMPTYERR, dsDSEVENTQEMPTYSEV, dsDSEVENTQEMPTYSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsDSNOQORMAPFUNERR, dsDSNOQORMAPFUNSEV, dsDSNOQORMAPFUNSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsDSNOMOREROWSERR, dsDSNOMOREROWSSEV, dsDSNOMOREROWSSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsDSFILEOPENERR, dsDSFILEOPENSEV, dsDSFILEOPENSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsDSNOINPUTERR, dsDSNOINPUTSEV, dsDSNOINPUTSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsDSNOOUTPUTERR, dsDSNOOUTPUTSEV, dsDSNOOUTPUTSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsDSMISSINGCOLSERR, dsDSMISSINGCOLSSEV, dsDSMISSINGCOLSSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsDSDOMALLOCERR, dsDSDOMALLOCSEV, dsDSDOMALLOCSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsDSQREINITERR, dsDSQREINITSEV, dsDSQREINITSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsDSDOREALLOCERR, dsDSDOREALLOCSEV, dsDSDOREALLOCSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsDSNOFITERR, dsDSNOFITSEV, dsDSNOFITSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsDSQOVERFLOWERR, dsDSQOVERFLOWSEV, dsDSQOVERFLOWSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsDSQNEWSIZEERR, dsDSQNEWSIZESEV, dsDSQNEWSIZESTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsDSNOPROCFRAMEERR, dsDSNOPROCFRAMESEV, dsDSNOPROCFRAMESTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsDSNOSKYCOLERR, dsDSNOSKYCOLSEV, dsDSNOSKYCOLSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsDSNOCCDCOLERR, dsDSNOCCDCOLSEV, dsDSNOCCDCOLSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsDSTOOMANYCHIPSERR, dsDSTOOMANYCHIPSSEV, dsDSTOOMANYCHIPSSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsDSTOOMANYNODESERR, dsDSTOOMANYNODESSEV, dsDSTOOMANYNODESSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsDSNONNEGSLOPEERR, dsDSNONNEGSLOPESEV, dsDSNONNEGSLOPESTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsDSNONODEIDCOLERR, dsDSNONODEIDCOLSEV, dsDSNONODEIDCOLSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsDSNOEXPTIMEERR, dsDSNOEXPTIMESEV, dsDSNOEXPTIMESTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsDSBADCCDIDERR, dsDSBADCCDIDSEV, dsDSBADCCDIDSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsDSNODETNAMERR, dsDSNODETNAMSEV, dsDSNODETNAMSTDMSG,
                            current_error, &group_err_stat_e);

  

  
  *num_errors +=
    dsErrInitHashMapElement(dsHPEBADCOORDRNGERR, dsHPEBADCOORDRNGSEV, dsHPEBADCOORDRNGSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsHPETIMEORDERERR, dsHPETIMEORDERSEV, dsHPETIMEORDERSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsHPESTARTSTOPERR, dsHPESTARTSTOPSEV, dsHPESTARTSTOPSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsHPEDEGAPALLOCERR, dsHPEDEGAPALLOCSEV, dsHPEDEGAPALLOCSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsHPEDEGAPLOADERR, dsHPEDEGAPLOADSEV, dsHPEDEGAPLOADSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsHPEADCALLOCERR, dsHPEADCALLOCSEV, dsHPEADCALLOCSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsHPEADCLOADERR, dsHPEADCLOADSEV, dsHPEADCLOADSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsHPEOUTCOLUMNERR, dsHPEOUTCOLUMNSEV, dsHPEOUTCOLUMNSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsHPEALIGNMENTERR, dsHPEALIGNMENTSEV, dsHPEALIGNMENTSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsHPEASPECTERR, dsHPEASPECTSEV, dsHPEASPECTSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsHPEEVENTSEQERR, dsHPEEVENTSEQSEV, dsHPEEVENTSEQSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsHPEALLBADFILESERR, dsHPEALLBADFILESSEV, dsHPEALLBADFILESSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsHPEFILEEXISTSERR, dsHPEFILEEXISTSSEV, dsHPEFILEEXISTSSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsHPEWRITEEVTERR, dsHPEWRITEEVTSEV, dsHPEWRITEEVTSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsHPEINSTRUMEPARERR, dsHPEINSTRUMEPARSEV, dsHPEINSTRUMEPARSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsHPEBADEVTFILEERR, dsHPEBADEVTFILESEV, dsHPEBADEVTFILESTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsHPELOGOPENERR, dsHPELOGOPENSEV, dsHPELOGOPENSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsHPESTAGEDNEERR, dsHPESTAGEDNESEV, dsHPESTAGEDNESTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsHPESTAGEANGDNEERR, dsHPESTAGEANGDNESEV, dsHPESTAGEANGDNESTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsHPEHPYDNEERR, dsHPEHPYDNESEV, dsHPEHPYDNESTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsHPESETAIMPOINTERR, dsHPESETAIMPOINTSEV, dsHPESETAIMPOINTSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsHPEDEPENDENCYERR, dsHPEDEPENDENCYSEV, dsHPEDEPENDENCYSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsHPEBADOUTCOLERR, dsHPEBADOUTCOLSEV, dsHPEBADOUTCOLSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsHPEADCOPENERR, dsHPEADCOPENSEV, dsHPEADCOPENSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsHPEADCOPENMEMERR, dsHPEADCOPENMEMSEV, dsHPEADCOPENMEMSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsHPEADCROWCNTERR, dsHPEADCROWCNTSEV, dsHPEADCROWCNTSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsHPEADCMISSROWERR, dsHPEADCMISSROWSEV, dsHPEADCMISSROWSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsHPEADCBADSYSERR, dsHPEADCBADSYSSEV, dsHPEADCBADSYSSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsHCDTSSPSOVERLAPERR, dsHCDTSSPSOVERLAPSEV, dsHCDTSSPSOVERLAPSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsHCDEVENNUMERR, dsHCDEVENNUMSEV, dsHCDEVENNUMSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsHBBDESCNOTSETERR, dsHBBDESCNOTSETSEV, dsHBBDESCNOTSETSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsHBBINVALIDSHAPEERR, dsHBBINVALIDSHAPESEV, dsHBBINVALIDSHAPESTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsHBBEMPTYINFILEERR, dsHBBEMPTYINFILESEV, dsHBBEMPTYINFILESTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsHBBOBSREREADERR, dsHBBOBSREREADSEV, dsHBBOBSREREADSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsHBBNOMEMALLOCERR, dsHBBNOMEMALLOCSEV, dsHBBNOMEMALLOCSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsHBBDEGAPREREADERR, dsHBBDEGAPREREADSEV, dsHBBDEGAPREREADSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsHBBSETTLMINERR, dsHBBSETTLMINSEV, dsHBBSETTLMINSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsHBBSETTLMAXERR, dsHBBSETTLMAXSEV, dsHBBSETTLMAXSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsHMTSSOBSTIMEERR, dsHMTSSOBSTIMESEV, dsHMTSSOBSTIMESTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsHMTHRCHKOBSTIMEERR, dsHMTHRCHKOBSTIMESEV, dsHMTHRCHKOBSTIMESTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsTREREGLOADERR, dsTREREGLOADSEV, dsTREREGLOADSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsTREINDATATYPEERR, dsTREINDATATYPESEV, dsTREINDATATYPESTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsTREOUTDATATYPEERR, dsTREOUTDATATYPESEV, dsTREOUTDATATYPESTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsTRECASTFLOWERR, dsTRECASTFLOWSEV, dsTRECASTFLOWSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsTREUNKNOWNINCOLERR, dsTREUNKNOWNINCOLSEV, dsTREUNKNOWNINCOLSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsTREUNKNOWNOUTCOLERR, dsTREUNKNOWNOUTCOLSEV, dsTREUNKNOWNOUTCOLSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsTREZOFPC2CHIPERR, dsTREZOFPC2CHIPSEV, dsTREZOFPC2CHIPSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsTRERMENERGYFINDERR, dsTRERMENERGYFINDSEV, dsTRERMENERGYFINDSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsTREBADTELESCOPERR, dsTREBADTELESCOPSEV, dsTREBADTELESCOPSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsTREMISSTELESCOPERR, dsTREMISSTELESCOPSEV, dsTREMISSTELESCOPSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsTGMUNEQUALSTACKERR, dsTGMUNEQUALSTACKSEV, dsTGMUNEQUALSTACKSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsTGMINTERPOLATIONERR, dsTGMINTERPOLATIONSEV, dsTGMINTERPOLATIONSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsTGMHZEROORDERERR, dsTGMHZEROORDERSEV, dsTGMHZEROORDERSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsTGMMZEROORDERERR, dsTGMMZEROORDERSEV, dsTGMMZEROORDERSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsTGMLZEROORDERERR, dsTGMLZEROORDERSEV, dsTGMLZEROORDERSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsTGMHWIDTHERR, dsTGMHWIDTHSEV, dsTGMHWIDTHSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsTGMMWIDTHERR, dsTGMMWIDTHSEV, dsTGMMWIDTHSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsTGMLWIDTHERR, dsTGMLWIDTHSEV, dsTGMLWIDTHSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsTGMPIXLIBERR, dsTGMPIXLIBSEV, dsTGMPIXLIBSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsTGMLENGTHCALCERR, dsTGMLENGTHCALCSEV, dsTGMLENGTHCALCSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsTGMGRATINGMATCHERR, dsTGMGRATINGMATCHSEV, dsTGMGRATINGMATCHSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsTGMZEROYVALERR, dsTGMZEROYVALSEV, dsTGMZEROYVALSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsTGMZERORADIUSERR, dsTGMZERORADIUSSEV, dsTGMZERORADIUSSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsTGMHCALCWIDERR, dsTGMHCALCWIDSEV, dsTGMHCALCWIDSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsTGMMCALCWIDERR, dsTGMMCALCWIDSEV, dsTGMMCALCWIDSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsTGMLCALCWIDERR, dsTGMLCALCWIDSEV, dsTGMLCALCWIDSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsTGMUSERMASKPARSERR, dsTGMUSERMASKPARSSEV, dsTGMUSERMASKPARSSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsTGMREGIONSTRINGERR, dsTGMREGIONSTRINGSEV, dsTGMREGIONSTRINGSTDMSG,
                            current_error, &group_err_stat_e);

  return group_err_stat_e;
}

/* helper function, intializes Pipe/Tools analysis errors */
dsErrCode dsErrInitPTAnalysisErr(long *current_error, long *num_errors)
{
  dsErrCode group_err_stat_e = dsNOERR;

  *num_errors +=
    dsErrInitHashMapElement(dsWRFLUXAFFTERR, dsWRFLUXAFFTSEV, dsWRFLUXAFFTSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsWRFLUXBFFTERR, dsWRFLUXBFFTSEV, dsWRFLUXBFFTSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsWRBADEMAPAXESERR, dsWRBADEMAPAXESSEV, dsWRBADEMAPAXESSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsWRBADEMAPAXLENSERR, dsWRBADEMAPAXLENSSEV, dsWRBADEMAPAXLENSSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsWRBADFMAPAXESERR, dsWRBADFMAPAXESSEV, dsWRBADFMAPAXESSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsWRBADFMAPAXLENSERR, dsWRBADFMAPAXLENSSEV, dsWRBADFMAPAXLENSSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsWRBADDATAAXESERR, dsWRBADDATAAXESSEV, dsWRBADDATAAXESSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsWRBADBKGAXESERR, dsWRBADBKGAXESSEV, dsWRBADBKGAXESSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsWRBADBKGAXLENSERR, dsWRBADBKGAXLENSSEV, dsWRBADBKGAXLENSSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsWRBADBKGEAXESERR, dsWRBADBKGEAXESSEV, dsWRBADBKGEAXESSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsWRBADBKGEAXLENSERR, dsWRBADBKGEAXLENSSEV, dsWRBADBKGEAXLENSSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsWRBADCOREAXESERR, dsWRBADCOREAXESSEV, dsWRBADCOREAXESSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsWRBADCOREAXLENSERR, dsWRBADCOREAXLENSSEV, dsWRBADCOREAXLENSSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsWRBADPARAMXSCALESERR, dsWRBADPARAMXSCALESSEV, dsWRBADPARAMXSCALESSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsWRBADPARAMYSCALESERR, dsWRBADPARAMYSCALESSEV, dsWRBADPARAMYSCALESSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsWRBADPARAMXYSCALESERR, dsWRBADPARAMXYSCALESSEV, dsWRBADPARAMXYSCALESSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsWROFFSETERR, dsWROFFSETSEV, dsWROFFSETSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsWRCELLTABLEERR, dsWRCELLTABLESEV, dsWRCELLTABLESTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsWRPSFLIMITERR, dsWRPSFLIMITSEV, dsWRPSFLIMITSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsWRSTKBADNAMEERR, dsWRSTKBADNAMESEV, dsWRSTKBADNAMESTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsWRNOGRATERR, dsWRNOGRATSEV, dsWRNOGRATSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsWRSRCFILECANTERR, dsWRSRCFILECANTSEV, dsWRSRCFILECANTSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsWRCOORDCREATEERR, dsWRCOORDCREATESEV, dsWRCOORDCREATESTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsWRCOORDCALCERR, dsWRCOORDCALCSEV, dsWRCOORDCALCSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsWRCOORDERRCALCERR, dsWRCOORDERRCALCSEV, dsWRCOORDERRCALCSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsWRNOBKGERR, dsWRNOBKGSEV, dsWRNOBKGSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsWTBGFFTERR, dsWTBGFFTSEV, dsWTBGFFTSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsWTCORAFFTERR, dsWTCORAFFTSEV, dsWTCORAFFTSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsWTCORBFFTERR, dsWTCORBFFTSEV, dsWTCORBFFTSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsWTCORBGFFTERR, dsWTCORBGFFTSEV, dsWTCORBGFFTSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsWTCORERRFFTERR, dsWTCORERRFFTSEV, dsWTCORERRFFTSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsWTCORERRCORAFFTERR, dsWTCORERRCORAFFTSEV, dsWTCORERRCORAFFTSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsWTCORERRCORBFFTERR, dsWTCORERRCORBFFTSEV, dsWTCORERRCORBFFTSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsWTCORERRBKGERRFFTERR, dsWTCORERRBKGERRFFTSEV, dsWTCORERRBKGERRFFTSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsWTEXPCORAFFTERR, dsWTEXPCORAFFTSEV, dsWTEXPCORAFFTSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsWTAREACORFFTERR, dsWTAREACORFFTSEV, dsWTAREACORFFTSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsWTBADPARAMITERERR, dsWTBADPARAMITERSEV, dsWTBADPARAMITERSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsWTBADPARAMSTOPERR, dsWTBADPARAMSTOPSEV, dsWTBADPARAMSTOPSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsWTBADPARAMSIGERR, dsWTBADPARAMSIGSEV, dsWTBADPARAMSIGSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsWTBADPARAMSIGBNDSERR, dsWTBADPARAMSIGBNDSSEV, dsWTBADPARAMSIGBNDSSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsWTBADPARAMBKGSIGERR, dsWTBADPARAMBKGSIGSEV, dsWTBADPARAMBKGSIGSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsWTBADPARAMBKGSIGBNDSERR, dsWTBADPARAMBKGSIGBNDSSEV, dsWTBADPARAMBKGSIGBNDSSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsWTCREATECMERR, dsWTCREATECMSEV, dsWTCREATECMSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsWTEXPCORBFFTERR, dsWTEXPCORBFFTSEV, dsWTEXPCORAFFTSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsWTBADPARAMXSCALESERR, dsWTBADPARAMXSCALESSEV, dsWTBADPARAMXSCALESSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsWTBADPARAMYSCALESERR, dsWTBADPARAMYSCALESSEV, dsWTBADPARAMYSCALESSTDMSG,
                            current_error, &group_err_stat_e);

  *num_errors +=
    dsErrInitHashMapElement(dsWTBADPARAMXYSCALESERR, dsWTBADPARAMXYSCALESSEV, dsWTBADPARAMXYSCALESSTDMSG,
                            current_error, &group_err_stat_e);

  *num_errors +=
    dsErrInitHashMapElement(dsWTBADEMAPAXESERR, dsWTBADEMAPAXESSEV, dsWTBADEMAPAXESSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsWTBADEMAPAXLENSERR, dsWTBADEMAPAXLENSSEV, dsWTBADEMAPAXLENSSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsWTBADPARAMWSCALESIZEERR, dsWTBADPARAMWSCALESIZESEV, dsWTBADPARAMWSCALESIZESTDMSG,
                            current_error, &group_err_stat_e);

  *num_errors +=
    dsErrInitHashMapElement(dsCDBADBGAXESERR, dsCDBADBGAXESSEV, dsCDBADBGAXESSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsCDBADBGAXLENSERR, dsCDBADBGAXLENSSEV, dsCDBADBGAXLENSSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsCDWCSINSTRERR, dsCDWCSINSTRSEV, dsCDWCSINSTRSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsCDPSFCANTERR, dsCDPSFCANTSEV, dsCDPSFCANTSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsCDCELLSIZEERR, dsCDCELLSIZESEV, dsCDCELLSIZESTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsCDOFFSETERR, dsCDOFFSETSEV, dsCDOFFSETSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsCDCELLTABLEERR, dsCDCELLTABLESEV, dsCDCELLTABLESTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsCDPSFLIMITERR, dsCDPSFLIMITSEV, dsCDPSFLIMITSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsCDBADINAXESERR, dsCDBADINAXESSEV, dsCDBADINAXESSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsCDCOORDCREATEERR, dsCDCOORDCREATESEV, dsCDCOORDCREATESTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsCDCOORDCALCERR, dsCDCOORDCALCSEV, dsCDCOORDCALCSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsCDCOORDERRCALCERR, dsCDCOORDERRCALCSEV, dsCDCOORDERRCALCSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsCDNOEXPMAPERR, dsCDNOEXPMAPSEV, dsCDNOEXPMAPSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsCDFEWEXPMAPERR, dsCDFEWEXPMAPSEV, dsCDFEWEXPMAPSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsCDSIZEEXPMAPERR, dsCDSIZEEXPMAPSEV, dsCDSIZEEXPMAPSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsSTKREADNUMLOWNUMERR, dsSTKREADNUMLOWNUMSEV, dsSTKREADNUMLOWNUMSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsSTKREADNUMHIGHNUMERR, dsSTKREADNUMHIGHNUMSEV, dsSTKREADNUMHIGHNUMSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsSTKREADNUMEMPTYERR, dsSTKREADNUMEMPTYSEV, dsSTKREADNUMEMPTYSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsFITPARAMERR, dsFITPARAMSEV, dsFITPARAMSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsHISTLISTERR, dsHISTLISTSEV, dsHISTLISTSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsWRITEBACKGDERR, dsWRITEBACKGDSEV, dsWRITEBACKGDSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsTCDNAXESERR, dsTCDNAXESSEV, dsTCDNAXESSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsTCDPADLTOLDERR, dsTCDPADLTOLDSEV, dsTCDPADLTOLDSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsTCDUNKWNPADERR, dsTCDUNKWNPADSEV, dsTCDUNKWNPADSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsTCDLAXES0ERR, dsTCDLAXES0SEV, dsTCDLAXES0STDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsTCDUNSUPPORTNAXESERR, dsTCDUNSUPPORTNAXESSEV, dsTCDUNSUPPORTNAXESSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsTCDNAXESMISMATCHERR, dsTCDNAXESMISMATCHSEV, dsTCDNAXESMISMATCHSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsTCDINCONSISTENTERR, dsTCDINCONSISTENTSEV, dsTCDINCONSISTENTSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsTCDUNKWNKERNELERR, dsTCDUNKWNKERNELSEV, dsTCDUNKWNKERNELSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsSIZEMISMATCHERR, dsSIZEMISMATCHSEV, dsSIZEMISMATCHSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsBKGMODEERR, dsBKGMODESEV, dsBKGMODESTDMSG,
                            current_error, &group_err_stat_e);

  /* MKRMF ERRORS */
  *num_errors +=
    dsErrInitHashMapElement(dsMKRMFDIMENSIONERR, dsMKRMFDIMENSIONSEV, dsMKRMFDIMENSIONMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsMKRMFINFILESYNTXERR, dsMKRMFINFILESYNTXSEV, dsMKRMFINFILESYNTXMSG,
                            current_error, &group_err_stat_e);

  *num_errors +=
    dsErrInitHashMapElement(dsMKRMFUSRGRIDSYNTXERR, dsMKRMFUSRGRIDSYNTXSEV,dsMKRMFUSRGRIDSYNTXMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsMKRMFMISMTCHNMESERR, dsMKRMFMISMTCHNMESSEV, dsMKRMFMISMTCHNMESMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsMKRMFOUTPUTFMTERR, dsMKRMFOUTPUTFMTSEV, dsMKRMFOUTPUTFMTMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsMKRMFNOTLOGBINERR, dsMKRMFNOTLOGBINSEV, dsMKRMFNOTLOGBINMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsMKRMFZEROBINBUMERR,dsMKRMFZEROBINBUMSEV,dsMKRMFZEROBINBUMMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsMKRMFDECBINSTPERR, dsMKRMFDECBINSTPSEV, dsMKRMFDECBINSTPMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsMKRMFNEGVALUEERR,dsMKRMFNEGVALUESEV,dsMKRMFNEGVALUEMSG,
                            current_error, &group_err_stat_e);

  
  *num_errors +=
    dsErrInitHashMapElement(dsMKRMFLONGKWDERR, dsMKRMFLONGKWDSEV, dsMKRMFLONGKWDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsMKRMFGENERICERR, dsMKRMFGENERICSEV, dsMKRMFGENERICMSG,
                            current_error, &group_err_stat_e);

  *num_errors +=
    dsErrInitHashMapElement(dsMKRMFINFILEREGNUMERR, dsMKRMFINFILEREGNUMSEV, dsMKRMFINFILEREGNUMMSG,
                            current_error, &group_err_stat_e);

  *num_errors +=
    dsErrInitHashMapElement(dsBKGGDBINERR, dsBKGGDBINSEV, dsBKGGDBINSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsSETTIMEBINSERR, dsSETTIMEBINSSEV, dsSETTIMEBINSSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsSOURCEBINERR, dsSOURCEBINSEV, dsSOURCEBINSTDMSG,
                            current_error, &group_err_stat_e);

  return group_err_stat_e;
}

/* helper function, it initializes the ASCFit error elements */
dsErrCode dsErrInitAFErr(long *current_error, long *num_errors)
{
  dsErrCode group_err_stat_e = dsNOERR;
  return group_err_stat_e;
}

/* helper function, initializes Pipe/Tools DM tools */
dsErrCode dsErrInitPTDMErr(long *current_error, long *num_errors)
{
  dsErrCode group_err_stat_e = dsNOERR;

  *num_errors +=
    dsErrInitHashMapElement(dsDMHEDITBADLINEERR, dsDMHEDITBADLINESEV, dsDMHEDITBADLINESTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsDMHEDITNOKEYWRITEERR, dsDMHEDITNOKEYWRITESEV, dsDMHEDITNOKEYWRITESTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsDMHEDITNOKEYADDERR, dsDMHEDITNOKEYADDSEV, dsDMHEDITNOKEYADDSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsDMHEDITKEYDELETEERR, dsDMHEDITKEYDELETESEV, dsDMHEDITKEYDELETESTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsDMHEDITKEYMOVEAFTERERR, dsDMHEDITKEYMOVEAFTERSEV, dsDMHEDITKEYMOVEAFTERSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsDMHEDITUNKNOWNOPPERR, dsDMHEDITUNKNOWNOPPSEV, dsDMHEDITUNKNOWNOPPSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsDMSMISSTABLENAMEERR, dsDMSMISSTABLENAMESEV, dsDMSMISSTABLENAMESTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsDMSINPUTFILEERR, dsDMSINPUTFILESEV, dsDMSINPUTFILESTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsDMWFEFNOSCRPTFILNAMERR, dsDMWFEFNOSCRPTFILNAMSEV, dsDMWFEFNOSCRPTFILNAMSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsDMWFEFNOXFILNAMERR, dsDMWFEFNOXFILNAMSEV, dsDMWFEFNOXFILNAMSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsDMWFEFNOLSTFILNAMERR, dsDMWFEFNOLSTFILNAMSEV, dsDMWFEFNOLSTFILNAMSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsDMWFEFFXNAMMISMATERR, dsDMWFEFFXNAMMISMATSEV, dsDMWFEFFXNAMMISMATSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsDMWFEFCANTSETXPROPERR, dsDMWFEFCANTSETXPROPSEV, dsDMWFEFCANTSETXPROPSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsDMWFEFXNOMISMATERR, dsDMWFEFXNOMISMATSEV, dsDMWFEFXNOMISMATSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsDMWFEFCANTREADCONSTERR, dsDMWFEFCANTREADCONSTSEV, dsDMWFEFCANTREADCONSTSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsDMWFEFMOREPARAMCOLNERR, dsDMWFEFMOREPARAMCOLNSEV, dsDMWFEFMOREPARAMCOLNSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsDMWFEFXNOMISMATGVNERR, dsDMWFEFXNOMISMATGVNSEV, dsDMWFEFXNOMISMATGVNSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsDMWFEFCANTSETPARAMERR, dsDMWFEFCANTSETPARAMSEV, dsDMWFEFCANTSETPARAMSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsDMWFEFMISCNTPARAMERR, dsDMWFEFMISCNTPARAMSEV, dsDMWFEFMISCNTPARAMSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsDMWFEFCANTSETCOLNAMERR, dsDMWFEFCANTSETCOLNAMSEV, dsDMWFEFCANTSETCOLNAMSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsDMWFEFFORKERRERR, dsDMWFEFFORKERRSEV, dsDMWFEFFORKERRSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsDMEXTRACTIGNOREPARERR, dsDMEXTRACTIGNOREPARSEV, 
			    dsDMEXTRACTIGNOREPARSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsDMEXTRACTBINSPECMISSINGERR, 
			    dsDMEXTRACTBINSPECMISSINGSEV, 
			    dsDMEXTRACTBINSPECMISSINGSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsDMEXTRACTBINSPECMISSINGEQERR, 
			    dsDMEXTRACTBINSPECMISSINGEQSEV, 
			    dsDMEXTRACTBINSPECMISSINGEQSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsDMEXTRACTBINPARMISSINGERR, 
			    dsDMEXTRACTBINPARMISSINGSEV, 
			    dsDMEXTRACTBINPARMISSINGSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsDMEXTRACTFINDBINPARERR, dsDMEXTRACTFINDBINPARSEV,
			    dsDMEXTRACTFINDBINPARSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsDMEXTRACTMINGTMAXERR, dsDMEXTRACTMINGTMAXSEV, 
			    dsDMEXTRACTMINGTMAXSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsDMEXTRACTBINSIZENEGERR, dsDMEXTRACTBINSIZENEGSEV,
			    dsDMEXTRACTBINSIZENEGSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsDMEXTRACTBADOPTERR, dsDMEXTRACTBADOPTSEV , 
			    dsDMEXTRACTBADOPTSTDMSG,
			    current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsDMEXTRACTBKGNUMERR ,dsDMEXTRACTBKGNUMSEV,
			    dsDMEXTRACTBKGNUMSTDMSG  ,
			    current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement( dsDMEXTRACTDATAREADERR, dsDMEXTRACTDATAREADSEV,
			     dsDMEXTRACTDATAREADSTDMSG ,
			     current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement( dsDMEXTRACTPROFILEERR, dsDMEXTRACTPROFILESEV ,
			     dsDMEXTRACTPROFILESTDMSG  ,
			     current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement( dsDMEXTRACTREGPARSEERR, dsDMEXTRACTREGPARSESEV,
			     dsDMEXTRACTREGPARSESTDMSG,
			     current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement( dsDMEXTRACTSTEPNEGERR, dsDMEXTRACTSTEPNEGSEV, 
			     dsDMEXTRACTSTEPNEGSTDMSG  ,
			     current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement( dsDMEXTRACTSTEPZEROERR, dsDMEXTRACTSTEPZEROSEV, 
			     dsDMEXTRACTSTEPZEROSTDMSG ,
			     current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement( dsDMEXTRACTNOSKYWERR, dsDMEXTRACTNOSKYWSEV, 
			     dsDMEXTRACTNOSKYWSTDMSG,
			     current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsDMEXTRACTREGWCSWERR, dsDMEXTRACTREGWCSWSEV, 
			    dsDMEXTRACTREGWCSWSTDMSG,
			    current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement( dsDMEXTRACTLOADKEYWERR, dsDMEXTRACTLOADKEYWSEV,
			     dsDMEXTRACTLOADKEYWSTDMSG,
			     current_error, &group_err_stat_e);
 
  *num_errors +=
    dsErrInitHashMapElement( dsDMEXTRACTDEFAULTEXWERR, 
			     dsDMEXTRACTDEFAULTEXWSEV, 
			     dsDMEXTRACTDEFAULTEXWSTDMSG,
			     current_error, &group_err_stat_e);

  *num_errors +=
    dsErrInitHashMapElement( dsDMEXTRACTHEADERREADERR, 
			     dsDMEXTRACTHEADERREADSEV, 
			     dsDMEXTRACTHEADERREADSTDMSG,
			     current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement( dsDMEXTRACTEXPSTACKERR, dsDMEXTRACTEXPSTACKSEV,
			     dsDMEXTRACTEXPSTACKSTDMSG,
			     current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement( dsDMEXTRACTBKGSTACKERR, dsDMEXTRACTBKGSTACKSEV,
			     dsDMEXTRACTBKGSTACKSTDMSG,
			     current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement( dsDMEXTRACTBKGEXPSTACKERR, 
			     dsDMEXTRACTBKGEXPSTACKSEV, 
			     dsDMEXTRACTBKGEXPSTACKSTDMSG,
			     current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement( dsDMEXTRACTREGCOMPWERR, dsDMEXTRACTREGCOMPWSEV,
			     dsDMEXTRACTREGCOMPWSTDMSG,
			     current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement( dsDMEXTRACTNOREGERR, dsDMEXTRACTNOREGSEV, 
			     dsDMEXTRACTNOREGSTDMSG,
			     current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement( dsDMEXTRACTVARIMGERR, dsDMEXTRACTVARIMGSEV,
			     dsDMEXTRACTVARIMGSTDMSG,
			     current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement( dsDMEXTRACTBKGNOREGERR, dsDMEXTRACTBKGNOREGSEV, 
			     dsDMEXTRACTBKGNOREGSTDMSG,
			     current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement( dsDMEXTRACTSUBARRAYERR,
			     dsDMEXTRACTSUBARRAYSEV,
			     dsDMEXTRACTSUBARRAYSTDMSG  ,
			     current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement( dsDMEXTRACTNOBKGERR, dsDMEXTRACTNOBKGSEV,
			     dsDMEXTRACTNOBKGSTDMSG,
			     current_error, &group_err_stat_e);

  *num_errors +=
    dsErrInitHashMapElement( dsDMEXTRACTVARPHAERR, dsDMEXTRACTVARPHASEV,
			     dsDMEXTRACTVARPHASTDMSG,
			     current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsDMREGRIDINCOMPLETEBINSPECERR, dsDMREGRIDINCOMPLETEBINSPECSEV, dsDMREGRIDINCOMPLETEBINSPECSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsDMREGRIDBADNUMBINSPECERR, dsDMREGRIDBADNUMBINSPECSEV, dsDMREGRIDBADNUMBINSPECSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsDMREGRIDIMAGEEXTENTDIFFERERR, dsDMREGRIDIMAGEEXTENTDIFFERSEV, dsDMREGRIDIMAGEEXTENTDIFFERSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsDMREGRIDMINGTMAXERR, dsDMREGRIDMINGTMAXSEV, dsDMREGRIDMINGTMAXSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsDMREGRIDBINSIZENEGERR, dsDMREGRIDBINSIZENEGSEV, dsDMREGRIDBINSIZENEGSTDMSG,
                            current_error, &group_err_stat_e);

 *num_errors +=
    dsErrInitHashMapElement(dsDMGROUPUNSUPPGTYPEERR, dsDMGROUPUNSUPPGTYPESEV, dsDMGROUPUNSUPPGTYPESTDMSG, current_error, &group_err_stat_e);

 *num_errors +=
    dsErrInitHashMapElement( dsDMGROUPMINBINGTMAXBINERR, dsDMGROUPMINBINGTMAXBINSEV, dsDMGROUPMINBINGTMAXBINSTDMSG, current_error, &group_err_stat_e);

 *num_errors +=
    dsErrInitHashMapElement( dsDMGROUPSTEPNEGERR, dsDMGROUPSTEPNEGSEV, dsDMGROUPSTEPNEGSTDMSG, current_error, &group_err_stat_e);

 *num_errors +=
    dsErrInitHashMapElement( dsDMGROUPSTEPZEROERR, dsDMGROUPSTEPZEROSEV, dsDMGROUPSTEPZEROSTDMSG, current_error, &group_err_stat_e);

 *num_errors +=
    dsErrInitHashMapElement( dsDMGROUPOVERLAPBINSPECERR, dsDMGROUPOVERLAPBINSPECSEV, dsDMGROUPOVERLAPBINSPECSTDMSG, current_error, &group_err_stat_e);

 *num_errors +=
    dsErrInitHashMapElement( dsDMGROUPMINBINERR, dsDMGROUPMINBINSEV, dsDMGROUPMINBINSTDMSG, current_error, &group_err_stat_e);

 *num_errors +=
    dsErrInitHashMapElement( dsDMGROUPMAXBINERR, dsDMGROUPMAXBINSEV,  dsDMGROUPMAXBINSTDMSG, current_error, &group_err_stat_e);

 *num_errors +=
    dsErrInitHashMapElement( dsDMGROUPNOBINSPECERR, dsDMGROUPNOBINSPECSEV, dsDMGROUPNOBINSPECSTDMSG, current_error, &group_err_stat_e);

 *num_errors +=
    dsErrInitHashMapElement( dsDMGROUPBINSPECMAXERR, dsDMGROUPBINSPECMAXSEV,  dsDMGROUPBINSPECMAXSTDMSG, current_error, &group_err_stat_e);

 *num_errors +=
    dsErrInitHashMapElement( dsDMGROUPSTEPMAXERR, dsDMGROUPSTEPMAXSEV, dsDMGROUPSTEPMAXSTDMSG, current_error, &group_err_stat_e);

 *num_errors +=
    dsErrInitHashMapElement( dsDMGROUPBADPARAMERR, dsDMGROUPBADPARAMSEV, dsDMGROUPBADPARAMSTDMSG, current_error, &group_err_stat_e);

 *num_errors +=
    dsErrInitHashMapElement( dsDMGROUPBADDATAORDERERR, dsDMGROUPBADDATAORDERSEV, dsDMGROUPBADDATAORDERSTDMSG, current_error, &group_err_stat_e);

 *num_errors +=
    dsErrInitHashMapElement( dsDMGROUPZEROWIDTHERR, dsDMGROUPZEROWIDTHSEV, dsDMGROUPZEROWIDTHSTDMSG, current_error, &group_err_stat_e);

 *num_errors +=
    dsErrInitHashMapElement( dsDMGROUPINVALIDBINERR, dsDMGROUPINVALIDBINSEV, dsDMGROUPINVALIDBINSTDMSG, current_error, &group_err_stat_e);

 *num_errors +=
    dsErrInitHashMapElement( dsDMGROUPLOWERBOUNDERR, dsDMGROUPLOWERBOUNDSEV, dsDMGROUPLOWERBOUNDSTDMSG, current_error, &group_err_stat_e);

 *num_errors +=
    dsErrInitHashMapElement( dsDMGROUPUPPERBOUNDERR, dsDMGROUPUPPERBOUNDSEV, dsDMGROUPUPPERBOUNDSTDMSG, current_error, &group_err_stat_e);

 *num_errors +=
    dsErrInitHashMapElement( dsDMGROUPBINONROWSERR, dsDMGROUPBINONROWSSEV, dsDMGROUPBINONROWSSTDMSG, current_error, &group_err_stat_e);

 *num_errors +=
    dsErrInitHashMapElement( dsDMGROUPMISSINGPARAMERR, dsDMGROUPMISSINGPARAMSEV, dsDMGROUPMISSINGPARAMSTDMSG, current_error, &group_err_stat_e);

 *num_errors +=
    dsErrInitHashMapElement( dsDMGROUPEXTRAGROUPSERR, dsDMGROUPEXTRAGROUPSSEV, dsDMGROUPEXTRAGROUPSSTDMSG, current_error, &group_err_stat_e);

 *num_errors +=
    dsErrInitHashMapElement( dsDMGROUPTOOFEWGROUPSERR, dsDMGROUPTOOFEWGROUPSSEV, dsDMGROUPTOOFEWGROUPSSTDMSG, current_error, &group_err_stat_e);

 *num_errors +=
    dsErrInitHashMapElement( dsDMGROUPZEROERRORERR, dsDMGROUPZEROERRORSEV, dsDMGROUPZEROERRORSTDMSG, current_error, &group_err_stat_e);
 
 *num_errors +=
   dsErrInitHashMapElement( dsDMTYPE2SPLITBADNUMOFROWSERR, dsDMTYPE2SPLITBADNUMOFROWSSEV, dsDMTYPE2SPLITBADNUMOFROWSSTDMSG, current_error, &group_err_stat_e);

 *num_errors +=
   dsErrInitHashMapElement( dsDMTYPE2SPLITBADROWNUMERR, dsDMTYPE2SPLITBADROWNUMSEV, dsDMTYPE2SPLITBADROWNUMSTDMSG, current_error, &group_err_stat_e);

 *num_errors +=
   dsErrInitHashMapElement( dsDMTYPE2SPLITROWNANERR, dsDMTYPE2SPLITROWNANSEV, dsDMTYPE2SPLITROWNANSTDMSG, current_error, &group_err_stat_e);

 *num_errors +=
   dsErrInitHashMapElement( dsDMTYPE2SPLITBADROWERR, dsDMTYPE2SPLITBADROWSEV, dsDMTYPE2SPLITBADROWSTDMSG, current_error, &group_err_stat_e);
 
 /* DMDIFF ERRORS */

 *num_errors +=
   dsErrInitHashMapElement( dsDMDIFFLINEDIFFERR, dsDMDIFFLINEDIFFSEV, dsDMDIFFLINEDIFFSTDMSG, current_error, &group_err_stat_e);

 *num_errors +=
   dsErrInitHashMapElement( dsDMDIFFUNITDIFFERR, dsDMDIFFUNITDIFFSEV, dsDMDIFFUNITDIFFSTDMSG, current_error, &group_err_stat_e);

 *num_errors +=
   dsErrInitHashMapElement( dsDMDIFFUNITNOTEQERR, dsDMDIFFUNITNOTEQSEV, dsDMDIFFUNITNOTEQSTDMSG, current_error, &group_err_stat_e);

 *num_errors +=
   dsErrInitHashMapElement( dsDMDIFFCOMMENTDIFFERR, dsDMDIFFCOMMENTDIFFSEV, dsDMDIFFCOMMENTDIFFSTDMSG, current_error, &group_err_stat_e);

 *num_errors +=
   dsErrInitHashMapElement( dsDMDIFFCOMMENTNOTEQERR, dsDMDIFFCOMMENTNOTEQSEV, dsDMDIFFCOMMENTNOTEQSTDMSG, current_error, &group_err_stat_e);

 *num_errors +=
   dsErrInitHashMapElement( dsDMDIFFLOWPARAMSERR, dsDMDIFFLOWPARAMSSEV, dsDMDIFFLOWPARAMSSTDMSG, current_error, &group_err_stat_e);

 *num_errors +=
   dsErrInitHashMapElement( dsDMDIFFDATATYPEDIFFERR, dsDMDIFFDATATYPEDIFFSEV, dsDMDIFFDATATYPEDIFFSTDMSG, current_error, &group_err_stat_e);

 *num_errors +=
   dsErrInitHashMapElement( dsDMDIFFVECTORDIFFERR, dsDMDIFFVECTORDIFFSEV, dsDMDIFFVECTORDIFFSTDMSG, current_error, &group_err_stat_e);

 *num_errors +=
   dsErrInitHashMapElement( dsDMDIFFROWSDIFFERR, dsDMDIFFROWSDIFFSEV, dsDMDIFFROWSDIFFSTDMSG, current_error, &group_err_stat_e);

 *num_errors +=
   dsErrInitHashMapElement( dsDMDIFFSSVALSDIFFERR, dsDMDIFFSSVALSDIFFSEV, dsDMDIFFSSVALSDIFFSTDMSG, current_error, &group_err_stat_e);

 *num_errors +=
   dsErrInitHashMapElement( dsDMDIFFVALSDIFFERR, dsDMDIFFVALSDIFFSEV, dsDMDIFFVALSDIFFSTDMSG, current_error, &group_err_stat_e);

 *num_errors +=
   dsErrInitHashMapElement( dsDMDIFFARRAYSIZEDIFFERR, dsDMDIFFARRAYSIZEDIFFSEV, dsDMDIFFARRAYSIZEDIFFSTDMSG, current_error, &group_err_stat_e);

 *num_errors +=
   dsErrInitHashMapElement( dsDMDIFFDATADIFFERR, dsDMDIFFDATADIFFSEV, dsDMDIFFDATADIFFSTDMSG, current_error, &group_err_stat_e);

 *num_errors +=
   dsErrInitHashMapElement( dsDMDIFFDATANOTEQERR, dsDMDIFFDATANOTEQSEV, dsDMDIFFDATANOTEQSTDMSG, current_error, &group_err_stat_e);

 *num_errors +=
   dsErrInitHashMapElement( dsDMDIFFSSDATATYPEDIFFERR, dsDMDIFFSSDATATYPEDIFFSEV, dsDMDIFFSSDATATYPEDIFFSTDMSG, current_error, &group_err_stat_e);

 *num_errors +=
   dsErrInitHashMapElement( dsDMDIFFBADNVALSERR, dsDMDIFFBADNVALSSEV, dsDMDIFFBADNVALSSTDMSG, current_error, &group_err_stat_e);

  return group_err_stat_e;
}

/* helper function, initializes Pipe/Tools Non SI errors */
dsErrCode dsErrInitPTNonSIErr(long *current_error, long *num_errors)
{
  dsErrCode group_err_stat_e = dsNOERR;

  *num_errors +=
    dsErrInitHashMapElement(dsMTLLOOKUPCOLERR, dsMTLLOOKUPCOLSEV, dsMTLLOOKUPCOLSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsMTLSEEDFILEERR, dsMTLSEEDFILESEV, dsMTLSEEDFILESTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsMTLDATALOOKUPERR, dsMTLDATALOOKUPSEV, dsMTLDATALOOKUPSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsMTLDATALIMERR, dsMTLDATALIMSEV, dsMTLDATALIMSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsMBGSMOOTHERR, dsMBGSMOOTHSEV, dsMBGSMOOTHSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsMBGLIMITCONFLICTERR, dsMBGLIMITCONFLICTSEV, dsMBGLIMITCONFLICTSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsMBGLIMITSERR, dsMBGLIMITSSEV, dsMBGLIMITSSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsACOGAPWERR, dsACOGAPWSEV, dsACOGAPWSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsACODETNAMEERR, dsACODETNAMESEV, dsACODETNAMESTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsACONODELERR, dsACONODELSEV, dsACONODELSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsACONOPARAMERR, dsACONOPARAMSEV, dsACONOPARAMSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsASPHISTNEGERR, dsASPHISTNEGSEV, dsASPHISTNEGSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsASPHISTLTFERR, dsASPHISTLTFSEV, dsASPHISTLTFSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsECVACOSERR, dsECVACOSSEV, dsECVACOSSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsECVARUNOUTWERR, dsECVARUNOUTWSEV, dsECVARUNOUTWSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsTDCINPUTSTKERR, dsTDCINPUTSTKSEV, dsTDCINPUTSTKSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsSIM2UPDATEDERR, dsSIM2UPDATEDSEV, dsSIM2UPDATEDSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsSIMNOUPDATEDERR, dsSIMNOUPDATEDSEV, dsSIMNOUPDATEDSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsSDSNOROWSERR, dsSDSNOROWSSEV, dsSDSNOROWSSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsSDSNOINERR, dsSDSNOINSEV, dsSDSNOINSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsSDSNEEDROWERR, dsSDSNEEDROWSEV, dsSDSNEEDROWSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsSDSBADMATCHERR, dsSDSBADMATCHSEV, dsSDSBADMATCHSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsSDSPREVMJFERR, dsSDSPREVMJFSEV, dsSDSPREVMJFSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsSDSPREVTIMEERR, dsSDSPREVTIMESEV, dsSDSPREVTIMESTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsSCPTIMESERR, dsSCPTIMESSEV, dsSCPTIMESSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsSCPSTATUSERR, dsSCPSTATUSSEV, dsSCPSTATUSSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsSCPBADCOLERR, dsSCPBADCOLSEV, dsSCPBADCOLSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsOMTBLOBJCOLERR, dsOMTBLOBJCOLSEV, dsOMTBLOBJCOLSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsOMTNULLSSERR, dsOMTNULLSSSEV, dsOMTNULLSSSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsOSLBADRNGERR, dsOSLBADRNGSEV, dsOSLBADRNGSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsOELNOIDERR, dsOELNOIDSEV, dsOELNOIDSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsOELNOALTERR, dsOELNOALTSEV, dsOELNOALTSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsOELFAULTWERR, dsOELFAULTWSEV, dsOELFAULTWSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsOELUKNWERR, dsOELUKNWSEV, dsOELUKNWSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsOASPSKIPWERR, dsOASPSKIPWSEV, dsOASPSKIPWSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsOASPNOIDERR, dsOASPNOIDSEV, dsOASPNOIDSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsOASPNOKALWERR, dsOASPNOKALWSEV, dsOASPNOKALWSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsOASPNOROWSERR, dsOASPNOROWSSEV, dsOASPNOROWSSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsOASPEARLYWERR, dsOASPEARLYWSEV, dsOASPEARLYWSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsOASPLATEWERR, dsOASPLATEWSEV, dsOASPLATEWSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsOCTTOLVALERR, dsOCTTOLVALSEV, dsOCTTOLVALSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsOCTTOLTYPEERR, dsOCTTOLTYPESEV, dsOCTTOLTYPESTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsOCTDURATIONERR, dsOCTDURATIONSEV, dsOCTDURATIONSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsOCTOUTPARERR, dsOCTOUTPARSEV, dsOCTOUTPARSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsOCTNOMMATCHERR, dsOCTNOMMATCHSEV, dsOCTNOMMATCHSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsOCTBADTOLERR, dsOCTBADTOLSEV, dsOCTBADTOLSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsOCTTOLSUPPORTERR, dsOCTTOLSUPPORTSEV, dsOCTTOLSUPPORTSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsOCTFINDTOLERR, dsOCTFINDTOLSEV, dsOCTFINDTOLSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsOCTCHECKERR, dsOCTCHECKSEV, dsOCTCHECKSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsCMPNOSTARTERR, dsCMPNOSTARTSEV, dsCMPNOSTARTSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsCMPRUNOUTERR, dsCMPRUNOUTSEV, dsCMPRUNOUTSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsCMPINKALERR, dsCMPINKALSEV, dsCMPINKALSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsAHLINSTERR, dsAHLINSTSEV, dsAHLINSTSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsAHLRDMODEERR, dsAHLRDMODESEV, dsAHLRDMODESTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsAHLNOTIMESWERR, dsAHLNOTIMESWSEV, dsAHLNOTIMESWSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsOHLINSTERR, dsOHLINSTSEV, dsOHLINSTSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsOHLREADCOLHERR, dsOHLREADCOLHSEV, dsOHLREADCOLHSTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsOHLREADCOLEERR, dsOHLREADCOLESEV, dsOHLREADCOLESTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsOHLDETNAMEERR, dsOHLDETNAMESEV, dsOHLDETNAMESTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsOHLNOTIMEERR, dsOHLNOTIMESEV, dsOHLNOTIMESTDMSG,
                            current_error, &group_err_stat_e);
  *num_errors +=
    dsErrInitHashMapElement(dsUPDNODETERR, dsUPDNODETSEV, dsUPDNODETSTDMSG,
                            current_error, &group_err_stat_e);

  return group_err_stat_e;
}

/* helper function, it initializes the DataBase error elements */
dsErrCode dsErrInitDBErr(long *current_error, long *num_errors)
{
  dsErrCode group_err_stat_e = dsNOERR;

  *num_errors += 
    dsErrInitHashMapElement(dsDATABASEEXISTSERR, dsDATABASEEXISTSSEV, 
                            dsDATABASEEXISTSSTDMSG, current_error, 
                            &group_err_stat_e);
  *num_errors += 
    dsErrInitHashMapElement(dsATTNOTFOUNDERR, dsATTNOTFOUNDSEV, 
                            dsATTNOTFOUNDSTDMSG, current_error, 
                            &group_err_stat_e);
  return group_err_stat_e;
}
