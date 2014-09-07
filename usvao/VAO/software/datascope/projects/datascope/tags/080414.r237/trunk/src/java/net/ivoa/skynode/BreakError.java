package net.ivoa.skynode;

/** Used for legal breaks in XML scans.
 *  The only way to get out of a SAX scan before the end
 *  of the document is to throw a Throwable.
 *  We use an error since we are contrained by the
 *  signatures in the interfaces for the classes that do the
 *  scans.
 */
public class BreakError extends Error {
    BreakError(String msg) {
	super(msg);
    }
}
