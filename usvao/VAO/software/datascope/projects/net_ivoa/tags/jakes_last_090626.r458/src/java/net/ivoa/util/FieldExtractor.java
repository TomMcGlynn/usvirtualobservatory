package net.ivoa.util;

import javax.xml.parsers.SAXParserFactory;
import javax.xml.parsers.SAXParser;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;

import java.io.FileInputStream;

public class FieldExtractor {

   public static void main(String[] args) throws Exception {
      FieldExtractor fe = new FieldExtractor();
      System.out.println(
         fe.find(args[0], Integer.parseInt(args[1]),Integer.parseInt(args[2])));
   }

   private class FieldCallBack extends DefaultHandler {

      int row;
      int col;
      int currentRow = 0;
      int currentCol = 0;
      String value;
      StringBuffer buf;
      boolean active = false;

      FieldCallBack(int row, int col) {
         this.row = row;
         this.col = col;
      }

      String getValue() {
         return value;
      }

      public void startElement(String uri, String localName, String qName, Attributes attrib) {
         if (qName.equals("TD")) {
            if (currentRow == row && currentCol == col) {
               active = true;
               buf = new StringBuffer();
            }
         }
      }

      public void endElement(String uri, String localName, String qName) {
         if (qName.equals("TR")) {
            currentCol = 0;
            currentRow += 1;
         } else if (qName.equals("TD")) {
            if (active) {
               value = new String(buf).trim();
               // We just wanted this one value.
               throw new Error("");
            }
            currentCol += 1;
         }
      }

      public void characters(char[] arr, int start, int len) {
         if (active) {
            buf.append(arr, start, len);
         }
      }
   }

   public String find(String file, int row, int col) throws Exception {

      SAXParser sp        = SAXParserFactory.newInstance().newSAXParser();
      FileInputStream is  = new FileInputStream(file);
      FieldCallBack   dft = new FieldCallBack(row, col);
      try {
         sp.parse(new InputSource(is), dft);
      } catch (Error e) {
         // Thrown to terminate parsing.  And if it's not
         // we should have a null value which is fine too
      }
      is.close();
      return dft.getValue();
   }
}
