package net.ivoa.util;

import net.ivoa.util.Settings;

import java.io.StringWriter;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.File;

public class SettingsFilter  {

   public static void filter(String file) throws java.io.IOException {
      filter(file, false);
   }

   public static void filter(String file, boolean skip) throws java.io.IOException {

      if (file != null) {

         BufferedReader bf = new java.io.BufferedReader(
            new java.io.InputStreamReader(
               getResourceOrFile(file)
            )
         );
         if (bf == null) {
            throw new java.io.FileNotFoundException("File/Resource not found:"+file);
         }
         String line;
         while ( (line =bf.readLine()) != null) {
	    if (line.startsWith("!$")) {
		int sp = line.indexOf(" ");
		
		// Line is: !$key Rest of line.
		// We want to check if key is set.
		String key = line.substring(2, sp);
		
		// Skip if setting is found.
		if (Settings.has(key)) {
		    continue;
		}
		// Now just process: Rest of line. 
		line = line.substring(sp+1);
	    }
            line = SettingsMatcher.replaceSettings(line, skip);
            if (line != null) {
               System.out.println(line);
            }
         }
      } else {
         new ShowError().fail("Null form template");
      }
   }

   /** Open a stream from a resource or a file.
   *  If a file with the given name exists then return
   *  a stream from that file, otherwise try to open it as a system resource.
   *
   *  @param name  The name of the resource or file.
   */
   static java.io.InputStream getResourceOrFile(String name) throws java.io.IOException {

      if (new java.io.File(name).exists()) {
         return new java.io.FileInputStream(name);
      } else {
         return ClassLoader.getSystemClassLoader().getResourceAsStream(name);
      }
   }
    
   public static void main(String[] args) throws Exception {
       filter(args[0]);
   }
}
