package net.ivoa.datascope;

import net.ivoa.util.CGI;
import net.ivoa.util.FieldExtractor;

import com.ice.tar.TarOutputStream;
import com.ice.tar.TarEntry;

import java.io.File;
import java.io.FileInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.zip.GZIPOutputStream;

import java.net.URL;
import java.net.URLConnection;

public class Tar {

	static String cache;
	static TarOutputStream ts;

	public static void main(String[] args) throws Exception {

		CGI cgi = new CGI();

		String selectString = cgi.value("selections");
		cache = cgi.value("cache");
		cache = DS.baseToHome(cache);

		String[] selections = selectString.split(";");

		for (String selection : selections) {
			process(selection);
		}
		if (ts != null) {
			ts.close();
		}
	}

	static void process(String info) throws Exception {

		String[] fields = info.split(",");
		if (fields.length == 2) {
			processXML(fields[0], fields[1]);
		} else if (fields.length == 3) {
			processFits(fields[0], fields[1], fields[2]);
		}
	}

	static void processXML(String sn, String id) throws Exception {

		String snx = DS.validFileName(sn);

		String file = cache + snx + "." + id + ".xml";
		processFile(file);
	}

	static void processFits(String sn, String id, String column) throws Exception {
		String snx = DS.validFileName(sn);
		String file = cache + snx + "." + id.replace("-", ".")
				+ ".fits";

		String f = DS.checkFile(file);
		if (f != null) {
			processFile(f);

		} else {
			String[] flds = id.split("-");
			
			snx = DS.validFileName(snx);
			
			int row = Integer.parseInt(flds[1]) - 1;
			int col = Integer.parseInt(column) - 1;
			String url = new FieldExtractor().find(cache + snx + "." + flds[0]
					+ ".xml", row, col);
			if (url == null) {
				throw new Exception("Unable to find requested URL");
			}

			processURL(url, file);
		}
	}

	static void processFile(String name) throws Exception {

		File f = new File(name);
		TarEntry tar = new TarEntry(f);
		String nn = name.substring(name.lastIndexOf("/") + 1);
		tar.setName(nn);
		if (ts == null) {
			openTs();
		}
		ts.putNextEntry(tar);
		FileInputStream fi = new FileInputStream(f);
		byte[] buffer = new byte[16384];
		int len;

		while ((len = fi.read(buffer)) > 0) {
			ts.write(buffer, 0, len);
		}

		ts.closeEntry();
	}

	static void processURL(String urlName, String fileName) throws Exception {

		byte[] data;
		String suffix = "";
		try {
			// First read the URL.
			InputStream is = new URL(urlName).openStream();
			ByteArrayOutputStream bo = new ByteArrayOutputStream();
			byte[] buffer = new byte[16384];
			int maxlen = DS.getMaxTarSize();
			int len;

			while ((len = is.read(buffer)) > 0) {
				bo.write(buffer, 0, len);
				if(bo.size()>maxlen){
					throw new Exception("File exceeded maximum file size of " +
							maxlen + " bytes for tar files. " + 
							"Try using the direct link in the URL column of the Data Table.");
				}
			}
			bo.close();
			data = bo.toByteArray();
			if (data[0] == FileTee.gzipMagic[0]
					&& data[1] == FileTee.gzipMagic[1]) {
				suffix = ".gz";
			} else if (data[0] == FileTee.zMagic[0]
					&& data[1] == FileTee.zMagic[1]) {
				suffix = ".Z";
			}
		} catch (Exception e) {
			String error = "Error accessing url:" + urlName + "\n" + e;
			data = error.getBytes();
			suffix = ".ERROR.txt";
		}

		String nn = fileName.substring(fileName.lastIndexOf("/") + 1);
		TarEntry te = new TarEntry(nn + suffix);
		if (ts == null) {
			openTs();
		}
		te.setSize(data.length);
		ts.putNextEntry(te);
		ts.write(data);
		ts.closeEntry();
		
		File f = new File(fileName + ".temp");
		FileOutputStream fo = new FileOutputStream(f);
		fo.write(data);
		fo.close();
		File to = new File(fileName + suffix);
		f.renameTo(to);

	}

	public static void openTs() throws Exception {
		System.out.println("Content-type: application/tar\n"
				+ "Content-encoding: gzip\n"
				+ "Content-disposition: inline; filename=\"datascope.tar\"\n");
		ts = new TarOutputStream(new GZIPOutputStream(System.out));
	}
}
