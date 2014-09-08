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


#ifndef DSERROR_STRUCTS_H
#include "dserror_structs.h"
#endif
#define _DSERROR_PIPETOOLS_H

#define dsPIPETOOLERROFFSET     -1000
#define dsPIPETOOLNUMERRORS     152

/* TCD SPECIFIC ERRORS */

#define dsTCDNAXESERR (dsPIPETOOLERROFFSET - 1)
#define dsTCDNAXESSEV dsERRSEVFATAL
#define dsTCDNAXESSTDMSG "ERROR: User passed nmuber of axes <= 0.\n"

#define dsTCDPADLTOLDERR (dsPIPETOOLERROFFSET - 2)
#define dsTCDPADLTOLDSEV dsERRSEVFATAL
#define dsTCDPADLTOLDSTDMSG "ERROR: Required padding less than original image size.\n"

#define dsTCDUNKWNPADERR (dsPIPETOOLERROFFSET - 3)
#define dsTCDUNKWNPADSEV dsERRSEVFATAL
#define dsTCDUNKWNPADSTDMSG "ERROR: Unknown padding specified.\n"

#define dsTCDLAXES0ERR (dsPIPETOOLERROFFSET - 4)
#define dsTCDLAXES0SEV dsERRSEVFATAL
#define dsTCDLAXES0STDMSG "ERROR: User specified lAxes[i] = 0.\n"

#define dsTCDUNSUPPORTNAXESERR (dsPIPETOOLERROFFSET - 5)
#define dsTCDUNSUPPORTNAXESSEV dsERRSEVFATAL
#define dsTCDUNSUPPORTNAXESSTDMSG "ERROR: Kernel lib doesn't support specified nAxes.\n"

#define dsTCDNAXESMISMATCHERR (dsPIPETOOLERROFFSET - 6)
#define dsTCDNAXESMISMATCHSEV dsERRSEVFATAL
#define dsTCDNAXESMISMATCHSTDMSG "ERROR: Kernel and image nAxes don't match.\n"

#define dsTCDINCONSISTENTERR (dsPIPETOOLERROFFSET - 7)
#define dsTCDINCONSISTENTSEV dsERRSEVFATAL
#define dsTCDINCONSISTENTSTDMSG "ERROR: Length of axes specified in string not same.\n"

/* ASP HIST SPECIFIC ERRORS */

#define dsASPHISTNEGERR (dsPIPETOOLERROFFSET - 8)
#define dsASPHISTNEGSEV dsERRSEVWARNING
#define dsASPHISTNEGSTDMSG "WARNING: Negative value illegal for octree: %f\n"

#define dsASPHISTLTFERR (dsPIPETOOLERROFFSET - 9)
#define dsASPHISTLTFSEV dsERRSEVFATAL
#define dsASPHISTLTFSTDMSG "ERROR: Problem with livetime correction file %s\n"

/* TG MASK SPECIFIC ERRORS */

#define dsTGMUNEQUALSTACKERR  (dsPIPETOOLERROFFSET - 10)
#define dsTGMUNEQUALSTACKSEV dsERRSEVFATAL
#define dsTGMUNEQUALSTACKSTDMSG "ERROR: Input/output stacks are of unequal size.\n"

#define dsTGMINTERPOLATIONERR (dsPIPETOOLERROFFSET - 11)
#define dsTGMINTERPOLATIONSEV dsERRSEVFATAL
#define dsTGMINTERPOLATIONSTDMSG "ERROR: Unable to interpolate fwhm for offaxis=%f.\n"

#define dsTGMHZEROORDERERR (dsPIPETOOLERROFFSET - 12)
#define dsTGMHZEROORDERSEV dsERRSEVFATAL
#define dsTGMHZEROORDERSTDMSG "ERROR: Heg zero order radius calculations.\n"

#define dsTGMMZEROORDERERR (dsPIPETOOLERROFFSET - 13)
#define dsTGMMZEROORDERSEV dsERRSEVFATAL
#define dsTGMMZEROORDERSTDMSG "ERROR: Meg zero order radius calculations.\n"

#define dsTGMLZEROORDERERR (dsPIPETOOLERROFFSET - 14)
#define dsTGMLZEROORDERSEV dsERRSEVFATAL
#define dsTGMLZEROORDERSTDMSG "ERROR: Leg zero order radius calculations.\n"

#define dsTGMHWIDTHERR (dsPIPETOOLERROFFSET - 15)
#define dsTGMHWIDTHSEV dsERRSEVFATAL
#define dsTGMHWIDTHSTDMSG "ERROR: Source %d is missing HEG width.\n"

#define dsTGMMWIDTHERR (dsPIPETOOLERROFFSET - 16)
#define dsTGMMWIDTHSEV dsERRSEVFATAL
#define dsTGMMWIDTHSTDMSG "ERROR: Source %d is missing MEG width.\n"

#define dsTGMLWIDTHERR (dsPIPETOOLERROFFSET - 17)
#define dsTGMLWIDTHSEV dsERRSEVFATAL
#define dsTGMLWIDTHSTDMSG "ERROR: Source %d is missing LEG width.\n"

#define dsTGMPIXLIBERR (dsPIPETOOLERROFFSET - 18)
#define dsTGMPIXLIBSEV dsERRSEVFATAL
#define dsTGMPIXLIBSTDMSG "ERROR: PIXLIB value not found for %s\n"

#define dsTGMLENGTHCALCERR (dsPIPETOOLERROFFSET - 19)
#define dsTGMLENGTHCALCSEV dsERRSEVFATAL
#define dsTGMLENGTHCALCSTDMSG "ERROR: Mask length calculation - %s\n"

#define dsTGMGRATINGMATCHERR (dsPIPETOOLERROFFSET - 20)
#define dsTGMGRATINGMATCHSEV dsERRSEVFATAL
#define dsTGMGRATINGMATCHSTDMSG "ERROR: Grating name %s, not matched to known types\n"

#define dsTGMZEROYVALERR (dsPIPETOOLERROFFSET - 21)
#define dsTGMZEROYVALSEV dsERRSEVFATAL
#define dsTGMZEROYVALSTDMSG "ERROR: Source %d is missing zero order y value\n"

#define dsTGMZERORADIUSERR (dsPIPETOOLERROFFSET - 22)
#define dsTGMZERORADIUSSEV dsERRSEVFATAL
#define dsTGMZERORADIUSSTDMSG "ERROR: Source %d is missing zero order radius\n"

#define dsTGMHCALCWIDERR (dsPIPETOOLERROFFSET - 23)
#define dsTGMHCALCWIDSEV dsERRSEVFATAL
#define dsTGMHCALCWIDSTDMSG "ERROR: Heg mask width calculations\n"

#define dsTGMMCALCWIDERR (dsPIPETOOLERROFFSET - 24)
#define dsTGMMCALCWIDSEV dsERRSEVFATAL
#define dsTGMMCALCWIDSTDMSG "ERROR: Meg mask width calculations\n"

#define dsTGMLCALCWIDERR (dsPIPETOOLERROFFSET - 25)
#define dsTGMLCALCWIDSEV dsERRSEVFATAL
#define dsTGMLCALCWIDSTDMSG "ERROR: Leg mask width calculations\n"

#define dsTGMUSERMASKPARSERR (dsPIPETOOLERROFFSET - 26)
#define dsTGMUSERMASKPARSSEV dsERRSEVFATAL
#define dsTGMUSERMASKPARSSTDMSG "ERROR: User mask size params are incorrect for source %d\n"

#define dsTGMREGIONSTRINGERR (dsPIPETOOLERROFFSET - 27)
#define dsTGMREGIONSTRINGSEV dsERRSEVFATAL
#define dsTGMREGIONSTRINGSTDMSG "ERROR: Output region string is empty\n"

/* CALC LIGHT CURVE SPECIFIC ERRORS */

#define dsBKGGDBINERR (dsPIPETOOLERROFFSET - 28)
#define dsBKGGDBINSEV dsERRSEVFATAL
#define dsBKGGDBINSTDMSG "ERROR: Problem binning background counts.\n"

#define dsSETTIMEBINSERR (dsPIPETOOLERROFFSET - 29)
#define dsSETTIMEBINSSEV dsERRSEVFATAL
#define dsSETTIMEBINSSTDMSG "ERROR: Failed to set timing bins.\n"

#define dsSOURCEBINERR (dsPIPETOOLERROFFSET - 30)
#define dsSOURCEBINSEV dsERRSEVFATAL
#define dsSOURCEBINSTDMSG "ERROR: Failed to  bin source counts.\n"

/* BKGD CALC GLOBAL COUNT RATE SPECIFIC ERRORS */

#define dsFITPARAMERR (dsPIPETOOLERROFFSET - 31)
#define dsFITPARAMSEV dsERRSEVFATAL
#define dsFITPARAMSTDMSG "ERROR: Fitting parameter error.\n"

#define dsHISTLISTERR (dsPIPETOOLERROFFSET - 32)
#define dsHISTLISTSEV dsERRSEVFATAL
#define dsHISTLISTSTDMSG "ERROR: Failed to create the histogram linked list.\n"

#define dsWRITEBACKGDERR (dsPIPETOOLERROFFSET - 33)
#define dsWRITEBACKGDSEV dsERRSEVFATAL
#define dsWRITEBACKGDSTDMSG "ERROR: Failed to write the background events.\n"

/* MISSION TIME LINE SPECIFIC ERRORS */

#define dsMTLLOOKUPCOLERR (dsPIPETOOLERROFFSET - 34)
#define dsMTLLOOKUPCOLSEV dsERRSEVFATAL
#define dsMTLLOOKUPCOLSTDMSG "ERROR: Lookup table does not contain required columns.\n"

#define dsMTLSEEDFILEERR (dsPIPETOOLERROFFSET - 35)
#define dsMTLSEEDFILESEV dsERRSEVFATAL
#define dsMTLSEEDFILESTDMSG "ERROR: No seed files specified.\n"

#define dsMTLMTLTABLEERR (dsPIPETOOLERROFFSET - 36)
#define dsMTLMTLTABLESEV dsERRSEVFATAL
#define dsMTLMTLTABLESTDMSG "ERROR: MTL_Table constructor failed.\n"

#define dsMTLDATALOOKUPERR (dsPIPETOOLERROFFSET - 37)
#define dsMTLDATALOOKUPSEV dsERRSEVWARNING
#define dsMTLDATALOOKUPSTDMSG "WARNING: The lookup table specified contains no data for the file %s.\n"

#define dsMTLDATALIMERR (dsPIPETOOLERROFFSET - 38)
#define dsMTLDATALIMSEV dsERRSEVWARNING
#define dsMTLDATALIMSTDMSG "WARNING: Specified obistart and stop are not contained within the data provided.  Using %f to %f.\n"

/* ACIS Collate Events Errors */

#define dsACEDTYCYCLE0ERR (dsPIPETOOLERROFFSET - 39)
#define dsACEDTYCYCLE0SEV dsERRSEVFATAL
#define dsACEDTYCYCLE0STDMSG "ERROR: Dtycycle of parameter block is 0. Event and exposure files are not in interleaved mode."
 
#define dsACEDTYCYCRNGERR (dsPIPETOOLERROFFSET - 40)
#define dsACEDTYCYCRNGSEV dsERRSEVWARNING
#define dsACEDTYCYCRNGSTDMSG "WARNING: Dtycycle value of %d is out of valid range (0..15)."
 
#define dsACEEVTEXPSTKSZERR (dsPIPETOOLERROFFSET - 41)
#define dsACEEVTEXPSTKSZSEV dsERRSEVFATAL
#define dsACEEVTEXPSTKSZSTDMSG "ERROR: If exposure and event stacks both contain elements then they must contain the same number of elements."
 
#define dsACEINOUTSTKSZERR (dsPIPETOOLERROFFSET - 42)
#define dsACEINOUTSTKSZSEV dsERRSEVFATAL
#define dsACEINOUTSTKSZSTDMSG "ERROR: The output stacks must contain the same number of elements as the input stacks."
 
#define dsACECOLUMNAMEERR (dsPIPETOOLERROFFSET - 43)
#define dsACECOLUMNAMESEV dsERRSEVFATAL
#define dsACECOLUMNAMESTDMSG "ERROR: One or more columns in file %s not recognized as a valid level 1 column name."
 
#define dsACEEXPCOLUMNAMEERR (dsPIPETOOLERROFFSET - 44)
#define dsACEEXPCOLUMNAMESEV dsERRSEVFATAL
#define dsACEEXPCOLUMNAMESTDMSG "ERROR: Column %s in file %s not recognized as a valid level 1 exposure file column name."
 
#define dsACEWRITEEVTERR (dsPIPETOOLERROFFSET - 45)
#define dsACEWRITEEVTSEV dsERRSEVFATAL
#define dsACEWRITEEVTSTDMSG "ERROR: Specified output event file column either contains an unrecognized column name or column datatype."
 
#define dsACEWRITEEXPERR (dsPIPETOOLERROFFSET - 46)
#define dsACEWRITEEXPSEV dsERRSEVFATAL
#define dsACEWRITEEXPSTDMSG "ERROR: Specified output column in exposure file %s either contains an unrecognized column name or column datatype."
 
#define dsACEDTYCYCMISSERR (dsPIPETOOLERROFFSET - 47)
#define dsACEDTYCYCMISSSEV dsERRSEVFATAL
#define dsACEDTYCYCMISSSTDMSG "ERROR: Dtycycle keyord is missing from the parameter block file."
 
#define dsACEEXPTIMEMISSERR (dsPIPETOOLERROFFSET - 48)
#define dsACEEXPTIMEMISSSEV dsERRSEVWARNING
#define dsACEEXPTIMEMISSSTDMSG "WARNING: %s keyord is missing from the parameter block file."
 
#define dsACEEXPTIMERNGERR (dsPIPETOOLERROFFSET - 49)
#define dsACEEXPTIMERNGSEV dsERRSEVWARNING
#define dsACEEXPTIMERNGSTDMSG "WARNING: %s value of %f is out of valid range (0.0 - 100.0 in tenths of seconds)."

#define dsACEPBKEXTRAERR (dsPIPETOOLERROFFSET - 50)
#define dsACEPBKEXTRASEV dsERRSEVWARNING
#define dsACEPBKEXTRASTDMSG "WARNING: More than 1 parameter block supplied. Only using the first one."

#define dsACEPBKNONEERR (dsPIPETOOLERROFFSET - 51)
#define dsACEPBKNONESEV dsERRSEVFATAL
#define dsACEPBKNONESTDMSG "Error: No parameter blocks supplied in file %s."

/* HRC_PROCESS_EVENTS errors */

#define dsHPEBADCOORDRNGERR (dsPIPETOOLERROFFSET - 52)
#define dsHPEBADCOORDRNGSEV dsERRSEVFATAL
#define dsHPEBADCOORDRNGSTDMSG "ERROR: The coordinate transformation starting point specified in the .par file is invalid."

#define dsHPETIMEORDERERR (dsPIPETOOLERROFFSET - 53)
#define dsHPETIMEORDERSEV dsERRSEVFATAL
#define dsHPETIMEORDERSTDMSG "ERROR: Event files must be in chronological order."

#define dsHPESTARTSTOPERR (dsPIPETOOLERROFFSET - 54)
#define dsHPESTARTSTOPSEV dsERRSEVFATAL
#define dsHPESTARTSTOPSTDMSG "ERROR: Event file TSTOP time must be greater than TSTART time."

#define dsHPEDEGAPALLOCERR (dsPIPETOOLERROFFSET - 55)
#define dsHPEDEGAPALLOCSEV dsERRSEVFATAL
#define dsHPEDEGAPALLOCSTDMSG "ERROR: Degap table memory allocation failed."

#define dsHPEDEGAPLOADERR (dsPIPETOOLERROFFSET - 56)
#define dsHPEDEGAPLOADSEV dsERRSEVFATAL
#define dsHPEDEGAPLOADSTDMSG "ERROR: Unable to load degap file into memory."

#define dsHPEADCALLOCERR (dsPIPETOOLERROFFSET - 57)
#define dsHPEADCALLOCSEV dsERRSEVFATAL
#define dsHPEADCALLOCSTDMSG "ERROR: ADC Correction table memory allocation failed."

#define dsHPEADCLOADERR (dsPIPETOOLERROFFSET - 58)
#define dsHPEADCLOADSEV dsERRSEVFATAL
#define dsHPEADCLOADSTDMSG "ERROR: Unable to load ADC Correction file into memory."

#define dsHPEOUTCOLUMNERR (dsPIPETOOLERROFFSET - 59) 
#define dsHPEOUTCOLUMNSEV dsERRSEVFATAL
#define dsHPEOUTCOLUMNSTDMSG "ERROR: Coordinate columns specified in the output eventdef do not correspond to requested coordinate transformations."  

#define dsHPEALIGNMENTERR (dsPIPETOOLERROFFSET - 60)
#define dsHPEALIGNMENTSEV dsERRSEVFATAL
#define dsHPEALIGNMENTSTDMSG "ERROR: The alignment file could not be open or is missing required columns." 

#define dsHPEASPECTERR (dsPIPETOOLERROFFSET - 61)
#define dsHPEASPECTSEV dsERRSEVFATAL
#define dsHPEASPECTSTDMSG "ERROR: The aspect file could not be open or is missing required columns." 

#define dsHPEEVENTSEQERR (dsPIPETOOLERROFFSET - 62)
#define dsHPEEVENTSEQSEV dsERRSEVWARNING
#define dsHPEEVENTSEQSTDMSG "WARNING: Out of sequence events discovered in %s."

#define dsHPEALLBADFILESERR (dsPIPETOOLERROFFSET - 63)
#define dsHPEALLBADFILESSEV dsERRSEVFATAL
#define dsHPEALLBADFILESSTDMSG "ERROR: Unable to successfully process any input event files."

#define dsHPEFILEEXISTSERR (dsPIPETOOLERROFFSET - 64)
#define dsHPEFILEEXISTSSEV dsERRSEVFATAL
#define dsHPEFILEEXISTSSTDMSG "ERROR: Could not create output file %s because the file already exists."

#define dsHPEWRITEEVTERR (dsPIPETOOLERROFFSET - 65)
#define dsHPEWRITEEVTSEV dsERRSEVWARNING
#define dsHPEWRITEEVTSTDMSG "WARNING: Could not write event data to unrecognized column of %s." 

#define dsHPEINSTRUMEPARERR  (dsPIPETOOLERROFFSET - 66)
#define dsHPEINSTRUMEPARSEV dsERRSEVFATAL
#define dsHPEINSTRUMEPARSTDMSG "ERROR: Unable to open the file specified by %s parameter in %s."

#define dsHPEBADEVTFILEERR (dsPIPETOOLERROFFSET - 67)
#define dsHPEBADEVTFILESEV dsERRSEVWARNING  
#define dsHPEBADEVTFILESTDMSG "WARNING: Event written to bad event file %s."  

#define dsHPELOGOPENERR (dsPIPETOOLERROFFSET - 68)
#define dsHPELOGOPENSEV dsERRSEVWARNING  
#define dsHPELOGOPENSTDMSG "WARNING: Could not open logfile %s. Writing log to stdout." 

#define dsHPESTAGEDNEERR (dsPIPETOOLERROFFSET - 69)
#define dsHPESTAGEDNESEV dsERRSEVWARNING
#define dsHPESTAGEDNESTDMSG "WARNING: Stage keywords (STF_X/Y/Z) not found in %s. Using zeros as the default values."

#define dsHPESTAGEANGDNEERR (dsPIPETOOLERROFFSET - 70)
#define dsHPESTAGEANGDNESEV dsERRSEVWARNING
#define dsHPESTAGEANGDNESTDMSG "WARNING: Stage angle keywords (STF_ANG1/2/3) not found in %s. Using zeros as the default values."  

#define dsHPEHPYDNEERR (dsPIPETOOLERROFFSET - 71)
#define dsHPEHPYDNESEV dsERRSEVWARNING
#define dsHPEHPYDNESTDMSG "WARNING: HRMA Pitch and Yaw (HRMA_PIT/YAW) keywords not found in %s. Using zeros as the default values." 

#define dsHPESETAIMPOINTERR (dsPIPETOOLERROFFSET - 72)
#define dsHPESETAIMPOINTSEV dsERRSEVWARNING
#define dsHPESETAIMPOINTSTDMSG "WARNING: Sim adjustment keywords (SIM_X/Y/Z) not found in %s. Using the instrument's default aimpoint position." 

#define dsHPEDEPENDENCYERR (dsPIPETOOLERROFFSET - 73)
#define dsHPEDEPENDENCYSEV dsERRSEVFATAL
#define dsHPEDEPENDENCYSTDMSG "ERROR: A data dependency on %s was not met."

#define dsHPEBADOUTCOLERR (dsPIPETOOLERROFFSET - 74)
#define dsHPEBADOUTCOLSEV dsERRSEVFATAL
#define dsHPEBADOUTCOLSTDMSG "ERROR: The column %s is not recognized as a valid level 1 output event column."

#define dsHPEADCOPENERR (dsPIPETOOLERROFFSET - 75) 
#define dsHPEADCOPENSEV dsERRSEVFATAL
#define dsHPEADCOPENSTDMSG "ERROR: The file %s either does not exist or is not accessible."
 
#define dsHPEADCOPENMEMERR (dsPIPETOOLERROFFSET - 76) 
#define dsHPEADCOPENMEMSEV dsERRSEVFATAL
#define dsHPEADCOPENMEMSTDMSG "ERROR: Unable to allocate memory necessary to open file %s."
 
#define dsHPEADCROWCNTERR (dsPIPETOOLERROFFSET - 77) 
#define dsHPEADCROWCNTSEV dsERRSEVWARNING
#define dsHPEADCROWCNTSTDMSG "WARNING: %s does not contain the expected number of rows"
 
#define dsHPEADCMISSROWERR (dsPIPETOOLERROFFSET - 78) 
#define dsHPEADCMISSROWSEV dsERRSEVWARNING
#define dsHPEADCMISSROWSTDMSG "WARNING: %s does not contain values for %c tap %hd. Using 0 for P and 1 for Q."

#define dsHPEADCBADSYSERR (dsPIPETOOLERROFFSET - 79) 
#define dsHPEADCBADSYSSEV dsERRSEVFATAL
#define dsHPEADCBADSYSSTDMSG "ERROR: The specified instrument %s is unrecognized or invalid."
 
/* errors for ACIS GTI tools */

#define dsACISGTINAMESERR (dsPIPETOOLERROFFSET - 80)
#define dsACISGTINAMESSEV dsERRSEVFATAL
#define dsACISGTINAMESSTDMSG "ERROR: The value of keyword %s, %s, in file %s is not an allowed value.  See help file.\n"

#define dsACISGTIDTCERR (dsPIPETOOLERROFFSET - 81)
#define dsACISGTIDTCSEV dsERRSEVWARNING
#define dsACISGTIDTCSTDMSG "WARNING: Calculated Dead Time Correction was %g, setting it to 1.0.\n"

#define dsACISGTIROWERR (dsPIPETOOLERROFFSET - 82)
#define dsACISGTIROWSEV dsERRSEVWARNING
#define dsACISGTIROWSTDMSG "WARNING: The %s extension in file %s does not have enough rows to determine the record length.\nLooking instead for %s keyword to get record length from.\n"

/* errors for ACIS_FORMAT_EVENTS */
 
#define dsAFESTACKSIZEERR (dsPIPETOOLERROFFSET - 83)
#define dsAFESTACKSIZESEV dsERRSEVFATAL
#define dsAFESTACKSIZESTDMSG "ERROR: The input bias and exposure stacks must either be empty (NONE) or contain the same number of elements as the input event stack."
 
#define dsAFEEXPSTATSERR (dsPIPETOOLERROFFSET - 84)
#define dsAFEEXPSTATSSEV dsERRSEVFATAL
#define dsAFEEXPSTATSSTDMSG "ERROR: Can not create exposure statistics file %s because the input exposure file stack is empty."
 
#define dsAFEBIASLOADERR (dsPIPETOOLERROFFSET - 85)
#define dsAFEBIASLOADSEV dsERRSEVFATAL
#define dsAFEBIASLOADSTDMSG "ERROR: Unable to load bias map %s."
 
#define dsAFENOBIASDATAERR (dsPIPETOOLERROFFSET - 86)
#define dsAFENOBIASDATASEV dsERRSEVFATAL
#define dsAFENOBIASDATASTDMSG "ERROR: A bias map must be provided for all modes except FAINT w/ BIAS"
 
#define dsAFEGRDMDBIASERR (dsPIPETOOLERROFFSET - 87)
#define dsAFEGRDMDBIASSEV dsERRSEVFATAL
#define dsAFEGRDMDBIASSTDMSG "ERROR: Unable to perform bias corrections on graded mode data."
 
#define dsAFEUNKNOWNMODEERR (dsPIPETOOLERROFFSET - 88)
#define dsAFEUNKNOWNMODESEV dsERRSEVFATAL
#define dsAFEUNKNOWNMODESTDMSG "ERROR: Unable to determine the data type of %s from the READMODE and DATAMODE keywords."
 
#define dsAFEMIXEDMODEERR (dsPIPETOOLERROFFSET - 89)
#define dsAFEMIXEDMODESEV dsERRSEVFATAL
#define dsAFEMIXEDMODESTDMSG "ERROR: The readmode and datamode values of all input event files in the stack must be identical."
 
#define dsAFEBIASDIFFERERR (dsPIPETOOLERROFFSET - 90)
#define dsAFEBIASDIFFERSEV dsERRSEVWARNING
#define dsAFEBIASDIFFERSTDMSG "WARNING: A bias pixel value in the output bias map has changed value between different events occurring in the same area."
 
#define dsAFENOBIASCORRERR (dsPIPETOOLERROFFSET - 91)
#define dsAFENOBIASCORRSEV dsERRSEVWARNING
#define dsAFENOBIASCORRSTDMSG "WARNING: Not performing bias correction of faint mode data because bias_correct parameter is set to no."
 
#define dsAFEOCCHIPRANGEERR (dsPIPETOOLERROFFSET - 92)
#define dsAFEOCCHIPRANGESEV dsERRSEVWARNING
#define dsAFEOCCHIPRANGESTDMSG "WARNING: Event chip position is outside of valid range. Overclock corrections not applied to this event."

#define dsAFEBSCHIPRANGEERR (dsPIPETOOLERROFFSET - 93)
#define dsAFEBSCHIPRANGESEV dsERRSEVWARNING
#define dsAFEBSCHIPRANGESTDMSG "WARNING: Event chip position is outside of valid range. Bias values for this event were not applied to the output bias map."

#define dsAFECCDRANGEERR (dsPIPETOOLERROFFSET - 94)
#define dsAFECCDRANGESEV dsERRSEVFATAL
#define dsAFECCDRANGESTDMSG "ERROR: The default ccd value of %hd obtained from the input file header is out of valid range (0-9)." 

#define dsAFEBADPMODEERR (dsPIPETOOLERROFFSET - 95)
#define dsAFEBADPMODESEV dsERRSEVWARNING
#define dsAFEBADPMODESTDMSG "WARNING: The specified acis mode is not recognized- can not perform bad pixel check." 

#define dsAFEBPCHIPRANGEERR (dsPIPETOOLERROFFSET - 96)
#define dsAFEBPCHIPRANGESEV dsERRSEVWARNING
#define dsAFEBPCHIPRANGESTDMSG "WARNING: Event chip position is outside of valid range. Bad pixel checking was not performed for this event."
 
#define dsAFEBADPCNTERR (dsPIPETOOLERROFFSET - 97)
#define dsAFEBADPCNTSEV dsERRSEVWARNING
#define dsAFEBADPCNTSTDMSG "WARNING: Event island contains 1 or more bad pixels."

#define dsAFEBADPPOSERR (dsPIPETOOLERROFFSET - 98)
#define dsAFEBADPPOSSEV dsERRSEVWARNING
#define dsAFEBADPPOSSTDMSG "WARNING: A specified bad pixel/column was not used in the bad pixel check because it contains an invalid point."

/* errors for TG_RESOLVE_EVENTS */

#define dsTREREGLOADERR (dsPIPETOOLERROFFSET - 99)
#define dsTREREGLOADSEV dsERRSEVFATAL
#define dsTREREGLOADSTDMSG "ERROR: Unable to parse or load region file %s."

#define dsTREINDATATYPEERR (dsPIPETOOLERROFFSET - 100)
#define dsTREINDATATYPESEV dsERRSEVWARNING
#define dsTREINDATATYPESTDMSG "WARNING: Data value for column %s not loaded due to inability to determine input column data type."

#define dsTREOUTDATATYPEERR (dsPIPETOOLERROFFSET - 101)
#define dsTREOUTDATATYPESEV dsERRSEVWARNING
#define dsTREOUTDATATYPESTDMSG "WARNING: Data value for column %s not written to output file due to inability to determine output data column type."

#define dsTRECASTFLOWERR (dsPIPETOOLERROFFSET - 102)
#define dsTRECASTFLOWSEV dsERRSEVWARNING
#define dsTRECASTFLOWSTDMSG "WARNING: A potential casting overflow has occurred for data in column %s." 

#define dsTREUNKNOWNINCOLERR (dsPIPETOOLERROFFSET - 103)
#define dsTREUNKNOWNINCOLSEV dsERRSEVWARNING
#define dsTREUNKNOWNINCOLSTDMSG "WARNING: Not loading data from unrecognized level 1.5 input column."

#define dsTREUNKNOWNOUTCOLERR (dsPIPETOOLERROFFSET - 104)
#define dsTREUNKNOWNOUTCOLSEV dsERRSEVFATAL
#define dsTREUNKNOWNOUTCOLSTDMSG "ERROR: Could not write out unrecognized level 1.5 output column."

#define dsTREZOFPC2CHIPERR (dsPIPETOOLERROFFSET - 105)
#define dsTREZOFPC2CHIPSEV dsERRSEVWARNING
#define dsTREZOFPC2CHIPSTDMSG "WARNING: Unable to determine the aspect corrected zero order focal plane position of source %hd."

#define dsTRERMENERGYFINDERR (dsPIPETOOLERROFFSET - 106)
#define dsTRERMENERGYFINDSEV dsERRSEVWARNING
#define dsTRERMENERGYFINDSTDMSG "WARNING: Could not find the energy range 
in the rm table that matches the event"

#define dsTREBADTELESCOPERR (dsPIPETOOLERROFFSET - 107)
#define dsTREBADTELESCOPSEV dsERRSEVFATAL
#define dsTREBADTELESCOPSTDMSG "ERROR: The TELESCOP keyword in %s is either invalid or differs from other files in the input event stack ."

#define dsTREMISSTELESCOPERR (dsPIPETOOLERROFFSET - 108)
#define dsTREMISSTELESCOPSEV dsERRSEVWARNING
#define dsTREMISSTELESCOPSTDMSG "WARNING: The TELESCOP keyword is missing from the principal extension of file %s. Using the Axaf flight configuration as the default."

/* errors for ACIS_PROCESS_EVENTS */

#define dsAPEEMPTYSTACKERR (dsPIPETOOLERROFFSET - 109)
#define dsAPEEMPTYSTACKSEV dsERRSEVFATAL
#define dsAPEEMPTYSTACKSTDMSG "ERROR: The input event stack does not contain any files."

#define dsAPEGRADEFILEERR (dsPIPETOOLERROFFSET - 110)
#define dsAPEGRADEFILESEV dsERRSEVFATAL
#define dsAPEGRADEFILESTDMSG "ERROR: The input grade scheme file %s could not be opened."

#define dsAPEGRADEFILESZERR (dsPIPETOOLERROFFSET - 111)
#define dsAPEGRADEFILESZSEV dsERRSEVFATAL
#define dsAPEGRADEFILESZSTDMSG "ERROR: The grade scheme file %s is missing entries or contains unrecognized columns."

#define dsAPEFOAFPDNEERR (dsPIPETOOLERROFFSET - 112)
#define dsAPEFOAFPDNESEV dsERRSEVWARNING
#define dsAPEFOAFPDNESTDMSG "WARNING: foafp_x, foafp_y, or foafp_z data does not exist in file %s ot performing 'winsize' corrections."
 
#define dsAPEWINSIZEERR (dsPIPETOOLERROFFSET - 113)
#define dsAPEWINSIZESEV dsERRSEVFATAL
#define dsAPEWINSIZESTDMSG "ERROR: All files in the stack must have the WINSIZE settings."

#define dsAPEGAINFILEERR (dsPIPETOOLERROFFSET - 114)
#define dsAPEGAINFILESEV dsERRSEVFATAL
#define dsAPEGAINFILESTDMSG "ERROR: The gain file %s does contain all of the necessary columns."

#define dsAPEDOPINOGAINERR  (dsPIPETOOLERROFFSET - 115)
#define dsAPEDOPINOGAINSEV dsERRSEVFATAL
#define dsAPEDOPINOGAINSTDMSG "ERROR: The calculate_pi option is set to yes but a gain file was not provided." 

#define dsAPEBADGAINROWERR (dsPIPETOOLERROFFSET - 116)
#define dsAPEBADGAINROWSEV dsERRSEVFATAL
#define dsAPEBADGAINROWSTDMSG "ERROR: A row in the gain table contains an invalid ccd id or ccd node value."

#define dsAPEGAINMISSERR (dsPIPETOOLERROFFSET - 117)
#define dsAPEGAINMISSSEV dsERRSEVFATAL
#define dsAPEGAINMISSSTDMSG "ERROR: The gain file is missing one or more entries."

#define dsAPEGAINCCDRNGERR (dsPIPETOOLERROFFSET - 118)
#define dsAPEGAINCCDRNGSEV dsERRSEVFATAL
#define dsAPEGAINCCDRNGSTDMSG "ERROR: An event with an out of range ccd id (%hd) was detected."

#define dsAPEGAINNODERNGERR (dsPIPETOOLERROFFSET - 119)
#define dsAPEGAINNODERNGSEV dsERRSEVFATAL
#define dsAPEGAINNODERNGSTDMSG "ERROR: An event with an out of range ccd id (%hd) was detected."

#define dsAPETSTARTERR (dsPIPETOOLERROFFSET - 120)
#define dsAPETSTARTSEV dsERRSEVWARNING 
#define dsAPETSTARTSTDMSG "WARNING: The input event times start at %f but the TSTART specified in %s is %f."

#define dsAPETSTOPERR (dsPIPETOOLERROFFSET - 121)
#define dsAPETSTOPSEV dsERRSEVWARNING 
#define dsAPETSTOPSTDMSG "WARNING: The input event times end at %f but the TSTOP specified in %s is %f."

#define dsAPEOBSTIMERNGERR (dsPIPETOOLERROFFSET - 122)
#define dsAPEOBSTIMERNGSEV dsERRSEVWARNING
#define dsAPEOBSTIMERNGSTDMSG "WARNING: The tstop time (%f) is earlier than the tstart time (%f) in %s."

#define dsAPEDMDATATYPEERR (dsPIPETOOLERROFFSET - 123)
#define dsAPEDMDATATYPESEV dsERRSEVFATAL
#define dsAPEDMDATATYPESTDMSG "ERROR: The specified output column %s is composed of un unknown or unsupported data type."

#define dsAPEUNKNOWNSYSERR (dsPIPETOOLERROFFSET - 124)
#define dsAPEUNKNOWNSYSSEV dsERRSEVWARNING
#define dsAPEUNKNOWNSYSSTDMSG "WARNING: Unable to set the TLMIN/TLMAX values for output event file column %s due to unknown instrument configuration." 

/*Errors added 28 February 2007 for ACIS_PROCESS_EVENTS */
#define dsAPEPULSEHEIGHTERR (dsPIPETOOLERROFFSET - 125)
#define dsAPEPULSEHEIGHTSEV dsERRSEVWARNING
#define dsAPEPULSEHEIGHTSTDMSG "WARNING: pulse height is less than split threshold when performing serial CTI adjustment.\n"

#define dsAPEREADERR (dsPIPETOOLERROFFSET - 126)
#define dsAPEREADSEV dsERRSEVFATAL
#define dsAPEREADSTDMSG "ERROR: Problem reading %s.\n"

#define dsAPEDXYERR (dsPIPETOOLERROFFSET - 127)
#define dsAPEDXYSEV dsERRSEVWARNING
#define dsAPEDXYSTDMSG "WARNING: Problem computing previous diffx/y values in CTI adjustment.\n"

#define dsAPEDIVZEROERR (dsPIPETOOLERROFFSET - 128)
#define dsAPEDIVZEROSEV dsERRSEVWARNING
#define dsAPEDIVZEROSTDMSG "WARNING: Attempted to divide by zero- please verify that the gain table entries are correct.\n"

#define dsAPETARGERR (dsPIPETOOLERROFFSET - 129)
#define dsAPETARGSEV dsERRSEVWARNING
#define dsAPETARGSTDMSG "WARNING: the RA_TARG and DEC_TARG coordinate fall off of the CCD during the observation.  Times may be bad.\n"

#define dsAPENUMCOLSERR (dsPIPETOOLERROFFSET - 130)
#define dsAPENUMCOLSSEV dsERRSEVWARNING
#define dsAPENUMCOLSSTDMSG "WARNING: %s does not contain all required columns.\n"

#define dsAPEREADCTIFILEERR (dsPIPETOOLERROFFSET - 131)
#define dsAPEREADCTIFILESEV dsERRSEVWARNING
#define dsAPeREADCTIFILESTDMSG "WARNING: problem reading ctifile, cti adjustment will not be applied.\n"

#define dsAPEREADTGAINFILEERR (dsPIPETOOLERROFFSET - 132)
#define dsAPEREADTGAINFILESEV dsERRSEVWARNING
#define dsAPEREADTGAINFILESTDMSG "WARNING: problem reading tgainfile, tgain adjustment will not be applied.\n"

#define dsAPEPHAZEROERR (dsPIPETOOLERROFFSET - 133)
#define dsAPEPHAZEROSEV dsERRSEVWARNING
#define dsAPEPHAZEROSTDMSG "WARNING: an event's summed pha is zero or null.\n"

#define dsAPESETTRAPDENSERR (dsPIPETOOLERROFFSET - 134)
#define dsAPESETTRAPDENSSEV dsERRSEVWARNING
#define dsAPESETTRAPDENSSTDMSG "WARNING: Error setting TRAPDENS value.\n"

#define dsAPEMEMERR (dsPIPETOOLERROFFSET - 135)
#define dsAPEMEMSEV dsERRSEVWARNING
#define dsAPEMEMSTDMSG "WARNING: Out of memory.\n"

#define dsAPEREADIMERR (dsPIPETOOLERROFFSET - 136)
#define dsAPEREADIMSEV dsERRSEVWARNING
#define dsAPEREADIMSTDMSG "WARNING: Error reading image data.\n"

#define dsAPEIMTYPEERR (dsPIPETOOLERROFFSET - 137)
#define dsAPEIMTYPESEV dsERRSEVWARNING
#define dsAPEIMTYPESTDMSG "WARNING: Unrecognized image data type.\n"

#define dsAPEIMDIMERR (dsPIPETOOLERROFFSET - 138)
#define dsAPEIMDIMSEV dsERRSEVWARNING
#define dsAPEIMDIMSTDMSG "WARNING: Couldn't get image dimensions.\n"

#define dsAPEMALLOCERR (dsPIPETOOLERROFFSET - 139)
#define dsAPEMALLOCSEV dsERRSEVWARNING
#define dsAPEMALLOCSTDMSG "WARNING: Malloc failed for line buffer: Out of memory.\n" 

#define dsAPEREADTRAPDENSERR (dsPIPETOOLERROFFSET - 140)
#define dsAPEREADTRAPDENSSEV dsERRSEVWARNING
#define dsAPEREADTRAPDENSSTDMSG "WARNING: problem reading from CTI trapdens map.\n"

#define dsAPECCDNODEERR (dsPIPETOOLERROFFSET - 141)
#define dsAPECCDNODESEV dsERRSEVWARNING
#define dsAPECCDNODESTDMSG "WARNING: An invalid ccdnode (valid range is 0-3) was converted to 0 during processing.\n"
/*END 28 Feb 2007 Update  */


/* SIM_TOOLS ERRORS */
#define dsSIM2UPDATEDERR (dsPIPETOOLERROFFSET - 142)
#define dsSIM2UPDATEDSEV dsERRSEVWARNING
#define dsSIM2UPDATEDSTDMSG "ERROR: Both samples are UPDATED. Please check input file."

#define dsSIMNOUPDATEDERR (dsPIPETOOLERROFFSET - 143)
#define dsSIMNOUPDATEDSEV dsERRSEVWARNING
#define dsSIMNOUPDATEDSTDMSG "WARNING: Neither sample was UPDATED. Please check input file."



/* HRC_BUILD_BADPIX ERRORS */
#define dsHBBDESCNOTSETERR (dsPIPETOOLERROFFSET - 144)
#define dsHBBDESCNOTSETSEV dsERRSEVFATAL
#define dsHBBDESCNOTSETSTDMSG "ERROR: Unable to process the %s badpixel file because the file descriptors have not been properly set up."

#define dsHBBINVALIDSHAPEERR (dsPIPETOOLERROFFSET - 145)
#define dsHBBINVALIDSHAPESEV dsERRSEVWARNING
#define dsHBBINVALIDSHAPESTDMSG "WARNING: The shape %s is currently not supported. Ignoring this element"

#define dsHBBEMPTYINFILEERR (dsPIPETOOLERROFFSET - 146)
#define dsHBBEMPTYINFILESEV dsERRSEVWARNING
#define dsHBBEMPTYINFILESTDMSG "WARNING: The input bad pixel file contains no entries."

#define dsHBBOBSREREADERR (dsPIPETOOLERROFFSET - 147)
#define dsHBBOBSREREADSEV dsERRSEVWARNING
#define dsHBBOBSREREADSTDMSG "WARNING: Obs.par file appears to have already been read- using previous data."

#define dsHBBNOMEMALLOCERR (dsPIPETOOLERROFFSET - 148)
#define dsHBBNOMEMALLOCSEV dsERRSEVFATAL
#define dsHBBNOMEMALLOCSTDMSG "ERROR: No memory was allocated for the data structure %s."

#define dsHBBDEGAPREREADERR (dsPIPETOOLERROFFSET - 149)
#define dsHBBDEGAPREREADSEV dsERRSEVWARNING
#define dsHBBDEGAPREREADSTDMSG "WARNING: The degap file appears to have already been loaded- using previous data."

/* STK_READ_NUM ERRORS */

#define dsSTKREADNUMLOWNUMERR (dsPIPETOOLERROFFSET - 150 )
#define dsSTKREADNUMLOWNUMSEV dsERRSEVWARNING
#define dsSTKREADNUMLOWNUMSTDMSG "WARNING: Stack element < 1, reseting num to 1.\n"

#define dsSTKREADNUMHIGHNUMERR (dsPIPETOOLERROFFSET - 151 )
#define dsSTKREADNUMHIGHNUMSEV dsERRSEVFATAL
#define dsSTKREADNUMHIGHNUMSTDMSG "ERROR: Number specified is greater than stack count.\n"

#define dsSTKREADNUMEMPTYERR (dsPIPETOOLERROFFSET - 152 )
#define dsSTKREADNUMEMPTYSEV dsERRSEVWARNING
#define dsSTKREADNUMEMPTYSTDMSG "WARNING: %ith element of stack is empty.\n"
