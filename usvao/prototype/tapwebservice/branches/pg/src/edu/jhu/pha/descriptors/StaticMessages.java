/*******************************************************************************
 * Copyright (c) 2011, Johns Hopkins University
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the Johns Hopkins University nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL Johns Hopkins University BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 ******************************************************************************/
package edu.jhu.pha.descriptors;

/**
 *
 * @author deoyani nandrekar-heinis
 */
public class StaticMessages {
 
    public static String msgOk        = "OK";
    public static String msgError     = "ERROR";    
    public static String msgQueue     = "QUEUED";
    public static String msgSuccess   = "SUCCESS";
    public static String msgPend      = "PENDING";    
    public static String msgAborted   = "ABORTED";
    public static String msgExec      = "EXECUTING";
    public static String msgComplete  = "COMPLETED";    
    public static String msgDont      = "DONT PROCESS";    
    public static String notVospace   = " not like 'vospace%'";
    
    //schemas 
    public static String uploadSchema = "TAP_UPLOAD";
    public static String tapSchema    = "TAP_SCHEMA";
    
    //outputformats
    public static String outputVotable= "VOTABLE";
    public static String outputCSV    =  "CSV";
    
    //vospace
    public static String vospace       =  "vospace";
    public static String vospaceuser   =  "vospaceuser"; // specifically made for the direct data transfer
    public static String vospacepublic =  "PUBLIC";
    public static String vospaceprivate=  "PRIVATE";
    public static String vospacegroup =   "GROUP";
    
    //tap commands
    public static String doquery      =  "doQuery";
    public static String lang         =  "ADQL";
    public static String result       =  "VOTABLE";
    
   
}
