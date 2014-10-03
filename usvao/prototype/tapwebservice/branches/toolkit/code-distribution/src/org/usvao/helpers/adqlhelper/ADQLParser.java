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
package org.usvao.helpers.adqlhelper;

import java.io.ByteArrayInputStream;
import java.util.Scanner;
import javax.xml.transform.TransformerException;
import org.astrogrid.adql.AdqlException;
import org.astrogrid.adql.AdqlParser;

/**
 *
 * @author deoyani
 */
public class ADQLParser {
    
    
    public  ADQLParser(){}

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        
        try{
       
//                String adqlString = "SELECT * FROM ( "        
//                + "  SELECT top 100 q.name, q.raj2000, q.dej2000, p.alphaFloat, p.deltaFloat, p.vmag FROM ("
//                + "    SELECT TOP 100 raj2000, dej2000, name, z FROM veronqsos.data"
//                + "    WHERE notRadio!='*' AND z BETWEEN 0.5 AND 1 AND dej2000<-40) AS q JOIN"
//                + "    ppmx.data AS p ON (1=CONTAINS( POINT('ICRS', q.raj2000, q.dej2000),"
//                + "  CIRCLE('ICRS', p.alphaFloat, p.deltaFloat, 0.3)))) AS f WHERE vmag BETWEEN 10 and 11";
         System.out.println("Enter adql query:");   
         Scanner scan = new Scanner(System.in);  
         String adqlString = scan.nextLine();
//         String adqlString = "SELECT o.ra, o.dec FROM photoobj o "
//                + " WHERE CONTAINS( POINT('ICRS', o.ra, o.dec), "
//                + " CIRCLE('ICRS', 180, 0, 0.3)) = 1";
        
         AdqlParser testParse = new AdqlParser(new ByteArrayInputStream(adqlString.getBytes()));
         String returnedString = testParse.parseToXmlText();
         AdqlConverter adcon = new AdqlConverter("ADQL20_SQLSERVER-SPATIAL.xsl");        
         String test = adcon.convertToV20(returnedString) ;
         System.out.println("SQL query:\n"+test);        
        
        }catch(Exception e){
            e.printStackTrace();
        }
    }
    
    /**
     * Get Parsed ADQL query
     * @param adqlString
     * @param styleSheet
     * @return
     * @throws AdqlException
     * @throws TransformerException 
     */
    public String getSQL(String adqlString, String styleSheet) throws AdqlException, TransformerException{                
        
         AdqlParser testParse = new AdqlParser(new ByteArrayInputStream(adqlString.getBytes()));
         String returnedString = testParse.parseToXmlText();
         AdqlConverter adcon = new AdqlConverter(styleSheet);       
         String sqlString = adcon.convertToV20(returnedString) ;                 
         return sqlString;        
    }
}
