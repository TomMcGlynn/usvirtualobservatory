/**
 * 
 */
package org.usvao.service.testing;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

/** An Adapter class for HTTP services which accept GET and POST calls.
 * 
 * @author thomas
 *
 */
public class HttpServiceAdapter 
{

	private static final Logger log = Logger.getLogger(HttpServiceAdapter.class);

	private static final String CRLF = "\r\n"; // Line separator required by multipart/form-data.

	// the schema to check for response validation (XML)
	private String responseSchema;
	private URL responseURL;
	private String baseUrl;
	private String charset = "UTF-8";
	private String userAgent = null;
	
	public final String getCharset() { return charset; }
	public final void setCharset(String cs) { this.charset = cs; }
	
	public final String getUserAgent() { return userAgent; }
	public final void setUserAgent(String userAgent) { this.userAgent = userAgent; }
	
	public final String getBaseUrl() { return baseUrl; }
	public final void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }
	
	public final URL getResponseSchemaURL() {
		if (responseURL == null)
		   responseURL = this.getClass().getClassLoader().getResource(getResponseSchema());
		return responseURL;
	}
	
	private String getResponseSchema() { return responseSchema; }
	public final void setResponseSchema(String schema) {
		responseSchema = null;
		this.responseSchema = schema;
	}

	public final String makePostCall (String urlPath, Map<String,String> data, File file, PostFileType type ) 
	throws HttpServiceAdapterException
	{
		return makeServiceCall (new StringBuffer("Make POST call to "), urlPath, data, file, type);
	}

	public final String makeGetCall ( String urlPath ) 
	throws HttpServiceAdapterException
	{
		return makeServiceCall (new StringBuffer("Make GET call to "), urlPath, null, null, PostFileType.TEXT);
	}

	private String makeServiceCall ( StringBuffer msg, String urlPath, Map<String,String> data, 
			File file , PostFileType ftype
	) 
	throws HttpServiceAdapterException
	{

		StringBuffer strUrl = new StringBuffer(getBaseUrl()).append(urlPath);

		log.debug(msg.append(strUrl).append(" data:").append(data).toString());

		URL url = null;
		try {
			url = new URL(strUrl.toString());
		} 
		catch (MalformedURLException murlex)
		{
			throw new HttpServiceAdapterException(murlex);
		}

		//make connection, use post mode, and send query
		URLConnection urlc = null;
		StringBuffer sb = new StringBuffer();

		try {

			urlc = url.openConnection();

			if (getUserAgent() != null)
			{
				urlc.setRequestProperty( "User-Agent", getUserAgent() );
			}

			// IF we have some data, then we assume this is a POST operation rather than GET
			// We need to do some extra things in this case
			if ((data != null && !data.isEmpty()) || file != null)
			{

				String boundary = Long.toHexString(System.currentTimeMillis()); // Just generate some unique random value.

				urlc.setDoOutput(true);
				urlc.setRequestProperty("Accept-Charset", getCharset());
				urlc.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
				//				urlc.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=" + getCharset());

				OutputStream output = urlc.getOutputStream();
				PrintWriter writer = null;
				try {
					writer = new PrintWriter(new OutputStreamWriter(output, getCharset()), true); // true = autoFlush, important!

					if (data != null)
					{
						for (String param : data.keySet())
						{
							sendParam(writer, boundary, param, data.get(param));
						}
					}

					if (file != null)
					{
						if (ftype.equals(PostFileType.TEXT)) {
							sendTextFile(writer, boundary, file);
						} else {
							sendBinaryFile(writer, output, boundary, file);
						}
					}

					// End of multi-part/form-data.
					writer.append("--" + boundary + "--").append(CRLF);

				} finally {
					if (writer != null) writer.close();
				}

			}

			int status = ((HttpURLConnection) urlc).getResponseCode();
			log.info("HttpServiceAdapter returns status:"+status);

			// header info
			boolean responseContentTypeIsText = false;
			for (Entry<String, List<String>> header : urlc.getHeaderFields().entrySet()) {
				String key = header.getKey();
				List<String> values = header.getValue();
				log.info(key + "=" + values);
				if (key != null && key.equals("Content-type"))
				{
					for (String ctype : values)
					{
						if (ctype.contains("text") || ctype.contains("xml"))
						{
							responseContentTypeIsText = true;
							break;
						}
					}
				}
			}

			String contentType = urlc.getHeaderField("Content-Type");
			String response_charset = null;
			for (String param : contentType.replace(" ", "").split(";")) {
				if (param.startsWith("charset=")) {
					response_charset = param.split("=", 2)[1];
					responseContentTypeIsText = true;
					break;
				}
			}
			
			//retrieve result
			if (responseContentTypeIsText)
			{

				// safety
				if (response_charset == null)
				{
					response_charset = getCharset();
				}
				
				BufferedReader br = null;
				try {
					br = new BufferedReader(new InputStreamReader(urlc.getInputStream(), response_charset));
					String str;
					while ((str = br.readLine()) != null) { sb.append(str).append("\n"); }
				} finally {
					if (br != null) 
					{
						try { br.close(); }  catch (IOException rioex) { throw new HttpServiceAdapterException(rioex); }
					}
				}

			} else {
				// TODO 
				// It's likely binary content, use InputStream/OutputStream write to response string
				throw new HttpServiceAdapterException("Missing charset in response indicates binary response which we cannot currently handle");
			}

		} catch (IOException ioex) { 
			throw new HttpServiceAdapterException(ioex); 
		}

		return sb.toString();

	}

	private void  sendParam(Writer writer, String boundary, String param, String value)
	throws IOException
	{
		writer.append("--" + boundary).append(CRLF);
		writer.append("Content-Disposition: form-data; name=\"").append(param).append("\"").append(CRLF);
		writer.append("Content-Type: text/plain; charset=" + getCharset()).append(CRLF);
		writer.append(CRLF).append(value).append(CRLF).flush();
	}

	private void sendTextFile (Writer writer, String boundary, File textFile) 
	throws IOException
	{

		// Send text file.
		writer.append("--" + boundary).append(CRLF);
		writer.append("Content-Disposition: form-data; name=\"textFile\"; filename=\"" + textFile.getName() + "\"").append(CRLF);
		writer.append("Content-Type: text/plain; charset=" + getCharset()).append(CRLF);
		writer.append(CRLF).flush();

		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new InputStreamReader(new FileInputStream(textFile), getCharset()));
			for (String line; (line = reader.readLine()) != null;) {
				writer.append(line).append(CRLF);
			}
		} finally {
			if (reader != null) try { reader.close(); } catch (IOException logOrIgnore) {}
		}
		writer.flush();

	}

	private void sendBinaryFile (Writer writer, OutputStream output, String boundary, File binaryFile) 
	throws IOException
	{

		writer.append("--" + boundary).append(CRLF);
		writer.append("Content-Disposition: form-data; name=\"binaryFile\"; filename=\"").append(binaryFile.getName()).append("\"").append(CRLF);
		writer.append("Content-Type: ").append(URLConnection.guessContentTypeFromName(binaryFile.getName())).append(CRLF);
		writer.append("Content-Transfer-Encoding: binary").append(CRLF);
		writer.append(CRLF).flush();
		InputStream input = null;
		try {
			input = new FileInputStream(binaryFile);
			byte[] buffer = new byte[1024];
			for (int length = 0; (length = input.read(buffer)) > 0;) {
				output.write(buffer, 0, length);
			}
			output.flush(); // Important! Output cannot be closed. Close of writer will close output as well.
		} finally {
			if (input != null) try { input.close(); } catch (IOException logOrIgnore) {}
		}
		writer.append(CRLF).flush(); // CRLF is important! It indicates end of binary boundary.

	}


}