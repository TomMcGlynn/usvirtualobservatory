*********************************************************************************************************
 * TAP service toolkit 
 * Copyright (c) 2011, Johns Hopkins University
 * All rights reserved.
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
*******************************************************************************************************

Details of TAP service toolkit

TAP is a Table Access Protocol by IVOA, International Virtual Observatory Alliance. This toolkit contains the source code for the TAP webservice, supported SQL scripts, properties file to edit depending on the database type selected and detail documentation.

Following are important parts of TAP service toolkit documentation, written in simple format and also divided in major steps required to implement TAP service on your database.

1. Requirements Overview 
	All the system requirements are listed in the "requirements-doc.txt". 
2. Installation Details
        Installation related details are in the "installation-doc.txt"  
3. Application Properties
	There is "tapwebservice.properties" file in the "code-distribution/src" folder, which one has to edit according 
	to the required system properties.
4. Build and Deploy
        Compiling code, building the executable and deployment procedure is given in "build-doc.txt"

       

Optional

Developers corner:
For developers who want to contribute to the code or edit according to your requirement read following document.
developers-help.txt
API-distribution:
All the api is available under "java-documentation" folder in the "documents-distribution" 

optional-docs

There is a folder "optional-docs" in the "documents-distribution".  
This contains "user-doc.txt" for all the different usage options , explained briefly

VOSpace Integration and Usage:

in the same "optional-docs" there is VOSpace-Tapservice.txt which explains how the given tapservice code works 
with US-VAO VOSpace. 

