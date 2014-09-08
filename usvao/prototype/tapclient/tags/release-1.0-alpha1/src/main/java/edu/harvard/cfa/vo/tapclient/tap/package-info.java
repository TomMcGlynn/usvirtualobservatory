/**
 * Provides the API for accessing IVOA Table Access Protocol(TAP) services.
 * This API includes access to TAP {@link edu.harvard.cfa.vo.tapclient.tap.AsyncJob asynchronous} and {@link edu.harvard.cfa.vo.tapclient.tap.SyncJob synchronous} data queries as well as the VOSI availability, capabilities, and tables metadata.
 * <p>
 * {@link edu.harvard.cfa.vo.tapclient.tap.TapService} provides access to the VOSI metadata of a service via the {@link edu.harvard.cfa.vo.tapclient.vosi.Availability}, {@link edu.harvard.cfa.vo.tapclient.vosi.Capability}, and {@link edu.harvard.cfa.vo.tapclient.vosi.Schema} objects.  Asynchronous data and metadata queries can be executed on a TAP service using the {@link edu.harvard.cfa.vo.tapclient.tap.AsyncJob} object.  The AsyncJob in turn provides access to parameters, {@link edu.harvard.cfa.vo.tapclient.tap.Result}, and {@link edu.harvard.cfa.vo.tapclient.tap.Error} objects.  Finally, synchronous data and metadata queries can be executed on a TAP service using the {@link edu.harvard.cfa.vo.tapclient.tap.SyncJob} object.
 *
 * @see <a href="http://www.ivoa.net/Documents/TAP/20100327/REC-TAP-1.0.html">IVOA Table Access Protocol v1.0 Recommendation</a>
 * @see <a href="http://www.ivoa.net/Documents/UWS/20101010/REC-UWS-1.0-20101010.html">IVOA Universal Worker Service Pattern v1.0 Recommendation</a>
 * @see <a href="http://www.ivoa.net/Documents/VOSI/20101206/PR-VOSI-1.0-20101206.html">IVOA Support Interfaces v1.0 Recommendation</a>
 */
package edu.harvard.cfa.vo.tapclient.tap;
