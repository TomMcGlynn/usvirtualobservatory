
package edu.caltech.vao.vospace;

import edu.caltech.vao.vospace.storage.StorageManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.IOException;

import java.net.URI;

import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Consumes;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("data")
public class DataResource extends VOSpaceResource {

    private final String ROOTNODE = "vos://nvo.caltech!vospace";
    private StorageManager backend;

    public DataResource() throws VOSpaceException {
	super();
	backend = manager.getStorageManager();
    }

    /**
     * This method retrieves the specified data.
     * 
     * @param fileid The identifier for the data to return.
     * @return the specified data
     */
    @Path("{fileid}")
    @GET
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public InputStream getData(@PathParam("fileid") String fileid) throws VOSpaceException {
	try {
	    String location = manager.resolveLocation(fileid);
	    manager.invalidateLocation(fileid);
	    //	    return new File(new URI(location));
	    return backend.getBytes(location);
	} catch (Exception e) {
	    e.printStackTrace(System.err);
	    throw new VOSpaceException(VOSpaceException.INTERNAL_SERVER_ERROR, e);
	}
    }

    /**
     * This method deals with the uplaoded data.
     * 
     * @param fileid The endpoint for the uploaded data (contents of HTTP PUT).
     */
    @Path("{dataid}")
    @PUT
    @Consumes(MediaType.APPLICATION_OCTET_STREAM)
    public void putNode(@PathParam("dataid") String fileid, File file) throws VOSpaceException {	
	FileInputStream in = null;
        FileOutputStream out = null;
	try {
	    String location = manager.resolveLocation(fileid);
            in = new FileInputStream(file);
	    backend.putBytes(location, in);
	    /*
	    out = new FileOutputStream(new File(new URI(location)));
	    byte[] buffer = new byte[4096]; // To hold file contents
	    int bytes_read; 
	    while ((bytes_read = in.read(buffer)) != -1)
		// Read until EOF
		out.write(buffer, 0, bytes_read); // write
	    */
	} catch (Exception e) {
	    throw new VOSpaceException(VOSpaceException.INTERNAL_SERVER_ERROR, e);
	} finally {
	    /*
	    if (in != null)
		try {
		    in.close();
		} catch (IOException e) {
		    ;
		}
	    if (out != null)
		try {
		    out.close();
		} catch (IOException e) {
		    ;
		}
	    */
	    manager.invalidateLocation(fileid);
	}
       
    }
}
