package net.ivoa.registry;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import java.io.File;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import java.net.URL;

import net.ivoa.registry.XSLTrans;

import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

/** Query the JHU Registry using the GET protocols. */
public class RegistryQuery {

	public static void main(String[] args) throws Exception {
		ArrayList<String> urls = new ArrayList<String>(0);
		//urls.add("http://nvo.stsci.edu/vor10/ristandardservice.asmx/TestKeywords?keywords=SimpleImageAccess&from=&max=");
		// "http://nvo.stsci.edu/vor10/ristandardservice.asmx/TestKeywords?keywords=ConeSearch&from=&max="
		urls.add("file:///C:/Documents%20and%20Settings/dahinshaw/Desktop/sia.xml");
		urls.add("file:///C:/Documents%20and%20Settings/dahinshaw/Desktop/cone.xml");

		ArrayList<String> xsls = new ArrayList<String>(0);
		xsls.add("sia.xsl");
		xsls.add("cone.xsl");

		Map<String, Map<String, List<String>>> res = query(urls, xsls);
		net.ivoa.datascope.MetaDump.dump(res);
	}

	/**
	 * Query the registry at the given URLs and simplify the results.
	 * 
	 * @param urls
	 *            An array of URLs to query the registry with. These are full
	 *            URLs and expected to use the JHU GET port to the registry. The
	 *            results from all of the queries will be concatenated.
	 * @param xsls
	 *            An array of file names for XSL files that are to be used to
	 *            filter the XML returned by the URLs. These should transform
	 *            the output of the URLs into a fiducial format like
	 * 
	 *            <pre>
	 *     &lt;ResourceList&gt;
	 *        &lt;Resource&gt;
	 *          &lt;Key&gt;Value&lt;/Key&gt;
	 *          ...
	 *        &lt;/Resource&gt;
	 *        ...
	 *     &lt;/ResourceList&gt;
	 * </pre>
	 * @return A Map where the keys are the IVORNs of the resources and the
	 *         values are a map of the metadata for that resource. Each of these
	 *         resource maps has a key which is the key of the metadata field
	 *         and a value which is a list of String values.
	 */
	public static Map<String, Map<String, List<String>>> query(List<String> urls,
			List<String> xsls) throws Exception {

		Map<String, Map<String, List<String>>> metadata = null;

		for (int i = 0; i < urls.size(); i += 1) {

			// We need two threads to process the information:
			// Thread 1 reads the URL, transforms the XML, and writes it to
			// pipe1
			// Thread 2 (main) reads pipe1, parses the XML, and returns metadata
			// to the user.

			PipedInputStream i1 = new PipedInputStream();
			PipedOutputStream o1 = new PipedOutputStream(i1);

			URL RegURL = new URL(urls.get(i));
			InputStream is = RegURL.openStream();
			Transformer r1 = new Transformer(xsls.get(i), is, o1);

			// Start processing the other threads.
			new Thread(r1).start();

			// The other thread should be sending data our way.
			// Set up the parser to interpret it.
			MetaParser mp;

			if (metadata == null) {
				mp = new MetaParser();
			} else {
				mp = new MetaParser(metadata);
			}
			metadata = mp.extract(i1);
		}

		return metadata;
	}

	/**
	 * This class reads the input XML and writes the transformed XML.
	 */
	private static class Transformer implements Runnable {

		String file;
		StreamSource input;
		StreamResult output;
		OutputStream outsave;

		Transformer(String xslFile, InputStream in, OutputStream out) {
			output = new StreamResult(out);
			input = new StreamSource(in);
			file = xslFile;
			outsave = out;
		}

		public void run() {

			try {

				XSLTrans tr = new XSLTrans();

				StreamSource xslsrc = new StreamSource(new File(file));
				tr.transform(xslsrc, input, output);
				outsave.close();

			} catch (Exception e) {
				System.err.println("Exception:" + e);
				e.printStackTrace(System.err);
			}
		}
	}
}
