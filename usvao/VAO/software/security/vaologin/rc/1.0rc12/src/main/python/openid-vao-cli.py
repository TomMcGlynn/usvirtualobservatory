#!/usr/bin/env python
"""
Command line implementation of OpenID Relying Party.

Based on consumer.py in Janrain, Inc., OpenID implementation examples.
"""
__copyright__ = 'Copyright 2005-2008, Janrain, Inc.'

# LIST OF VALID ATTRIBUTES; ADD AS NEEDED.
attribute_uris = {
'username'     : 'http://axschema.org/namePerson/friendly',
'name'         : 'http://axschema.org/namePerson',
'email'        : 'http://axschema.org/contact/email',
'phone'        : 'http://axschema.org/contact/phone',
'credential'   : 'http://sso.usvao.org/schema/credential/x509',
'institution'  : 'http://sso.usvao.org/schema/institution',
'country'      : 'http://sso.usvao.org/schema/country',
}

import cgi
import urlparse
import urllib
import cgitb
import sys
import os
import optparse
import pickle

def quoteattr(s):
    qs = cgi.escape(s, 1)
    return '"%s"' % (qs,)

try:
    import openid
except ImportError:
    sys.stderr.write("""
Failed to import the OpenID library. In order to use this command line tool, you
must either install the python-openid library (see INSTALL in the root of the
distribution) or else add the library to python's import path (the
PYTHONPATH environment variable).

For more information, see the README in the root of the library
distribution.""")
    sys.exit(1)

from openid.store import memstore
from openid.store import filestore
from openid.consumer import consumer
from openid.cryptutil import randomString
from openid.fetchers import setDefaultFetcher, Urllib2Fetcher
from openid.extensions import ax, pape, sreg

class OpenIDRequestHandler(object):
    """Request handler that knows how to verify an OpenID identity."""
    session = None

    def __init__(self, store_path, store, initiate=True, sid=None,
                           keep_session_file=False):
        self.store_path = store_path
        self.store = store
	self.sid = sid
        self.session_file = None
        self.keep_session_file = keep_session_file
        self.initiate = initiate

    def getConsumer(self, stateless=False):
        if stateless:
            store = None
        else:
            store = self.store
        return consumer.Consumer(self.getSession(), store)

    def getSession(self):
        """Return the existing session or a new session"""
        if self.session is None:
            if self.sid is None:
                while True:
                    self.sid = randomString(16, '0123456789abcdef')

                    session_dir = self.store_path + '/VAO_Sessions/'
                    try:
                        os.mkdir(session_dir)
                    except OSError:
                        pass

                    self.session_file = session_dir + self.sid
                    try:
                        fh = os.open(self.session_file, os.O_CREAT | os.O_EXCL)
                    except:
                        print 'CONTINURING' + self.session_file + '\n'
                        continue
                    else:
                        os.close(fh)
                        break
            else:
                self.session_file = self.store_path + '/VAO_Sessions/' \
                                         + self.sid

            if os.path.exists(self.session_file) and \
                       os.path.getsize(self.session_file):
                if self.initiate:
                    sys.stderr.write('ERROR: Non-empty session file already'
                        ' exists while the operation is initiate\n')
                    sys.exit(1)
                session_file_handle = open(self.session_file, 'r')
                self.session = pickle.load(session_file_handle)
                print 'LOADED SESSION FROM ' + self.session_file + '\n'
                session_file_handle.close()
                if self.keep_session_file is False:
                    os.remove(self.session_file)
            else:
                session = {}
                session['id'] = self.sid
                self.session = session

        return self.session

    def doInitiate(self, openid_url, return_to, attributes = None):
        """initiates OpenID authentication.
        """

        use_stateless = False

        oidconsumer = self.getConsumer(stateless = use_stateless)
        try:
            print 'BEGINNING CONSUMER FOR'
            print openid_url
            print 'RETURN TO'
            print return_to
            request = oidconsumer.begin(openid_url)
        except consumer.DiscoveryFailure, exc:
            sys.stderr.write('Error in discovery: %s' % (
                cgi.escape(str(exc[0]))))
            sys.exit(1)
        else:
            if request is None:
                sys.stderr.write('No OpenID services found for <code>%s</code>' % (
                    cgi.escape(openid_url),))
                sys.exit(1)
            else:
                # Then, ask the library to begin the authorization.
                # Here we find out the identity server that will verify the
                # user's identity, and get a token that allows us to
                # communicate securely with the identity server.

                self.requestAxData(request, attributes)

                trust_root = return_to
                if True: # request.shouldSendRedirect():
#TODO: look into "realm" parameter to redirectURL; discovery of RPs
                    redirect_url = request.redirectURL(
                        trust_root, return_to, immediate=False)
                    sys.stdout.write("REDIRECT=%s\n" % redirect_url)
                    print 'SAVING SESSION STATE TO ' + self.session_file + '\n'
                    session_file_handle = open(self.session_file, 'w')
                    pickle.dump(self.session, session_file_handle)
                    session_file_handle.close()
                    sys.stdout.write("SESSION_ID=%s\n" % self.sid)

    def requestAxData(self, request, attributes):
        if attributes and len(attributes):
            ax_request = ax.FetchRequest()

            for i in attributes:
                ax_request.add(ax.AttrInfo(i, required=True))

            request.addExtension(ax_request)

    def doVerify(self, url, credfile):
        """Handle the redirect from the OpenID server.
        """
        oidconsumer = self.getConsumer()

        self.parsed_uri = urlparse.urlparse(url)
        self.query = {}
        for k, v in cgi.parse_qsl(self.parsed_uri[4]):
            print "VALUE OF KEY: " + k + " IS: " + v
            self.query[k] = v.decode('utf-8')

        # Ask the library to check the response that the server sent
        # us.  Status is a code indicating the response type. info is
        # either None or a string containing more information about
        # the return type.
        
        info = oidconsumer.complete(self.query, url)

        display_identifier = info.getDisplayIdentifier()

        if info.status == consumer.FAILURE and display_identifier:
            # In the case of failure, if info is non-None, it is the
            # URL that we were verifying. We include it in the error
            # message to help the user figure out what happened.
            fmt = "Verification of %s failed: %s"
            message = fmt % (cgi.escape(display_identifier),
                             info.message)
        elif info.status == consumer.SUCCESS:
            # Success means that the transaction completed without
            # error. If info is None, it means that the user cancelled
            # the verification.

            # This is a successful verification attempt. If this
            # was a real application, we would do our login,
            # comment posting, etc. here.
            fmt = "You have successfully verified %s as your identity.\n"
            message = fmt % (cgi.escape(display_identifier),)
            ax_response = ax.FetchResponse.fromSuccessResponse(info, False)
            if ax_response:
                for i in attribute_uris:
                    value = ax_response.getSingle(attribute_uris[i])
                    if value:
                        if i is 'credential':
                            urllib.urlretrieve(value, credfile)
                            message += ("\nATTRIBUTE:credential=%s\n" % credfile)
                        else:
                            message += ('\nATTRIBUTE:%s=%s\n' % (i, value))

            if info.endpoint.canonicalID:
                # You should authorize i-name users by their canonicalID,
                # rather than their more human-friendly identifiers.  That
                # way their account with you is not compromised if their
                # i-name registration expires and is bought by someone else.
                message += ("  This is an i-name, and its persistent ID is %s"
                            % (cgi.escape(info.endpoint.canonicalID),))
        elif info.status == consumer.CANCEL:
            # cancelled
            message = 'Verification cancelled'
        elif info.status == consumer.SETUP_NEEDED:
            if info.setup_url:
                message = '<a href=%s>Setup needed</a>' % (
                    quoteattr(info.setup_url),)
            else:
                # This means auth didn't succeed, but you're welcome to try
                # non-immediate mode.
                message = 'Setup needed'
        else:
            # Either we don't understand the code or there is no
            # openid_url included with the error. Give a generic
            # failure message. The library should supply debug
            # information in a log.
            message = 'Verification failed.'

        sys.stdout.write(message)

def main(operation, data_path, attributes, return_url, parse_url, cred_file, userid, weak_ssl, session_id, keep_session_file):
    # Instantiate OpenID consumer store and OpenID consumer.  If you
    # were connecting to a database, you would create the database
    # connection and instantiate an appropriate store here.
    if data_path:
        store = filestore.FileOpenIDStore(data_path)
    else:
        # TODO: raise error here. can't do memstore in cmd line util.
        store = memstore.MemoryStore()

    if weak_ssl:
        setDefaultFetcher(Urllib2Fetcher())

    request_handler = OpenIDRequestHandler(data_path, store,
                 operation == 'initiate', session_id, keep_session_file)
    if operation == 'initiate':
        request_handler.doInitiate(userid, return_url, attributes)
        sys.exit(0);
    elif operation == 'verify':
        request_handler.doVerify(parse_url, cred_file)
        sys.exit(0);
    else:
        sys.stderr.write('Invalid operation specified. Operation can only be'
              ' one of "initiate" or "verify".')
        sys.exit(1);

if __name__ == '__main__':
    weak_ssl = False

    if True:
        parser = optparse.OptionParser('Usage:\n %prog [options]')
        parser.add_option(
            '-o', '--operation', dest='operation',
            help='One of "initiate", "verify" must be specified.')
        parser.add_option(
            '-d', '--data-path', dest='data_path',
            help='Data directory for storing OpenID consumer state. '
            'Setting this option implies using a "FileStore".')
        parser.add_option(
            '-a', '--attributes', dest='attributes',
            help='Required attributes for the user. '
            'These should be specified comma-separated.')
        parser.add_option(
            '-r', '--return-url', dest='return_url',
            help='Return URL. '
            'The URL the user-agent will be redirected to by OpenID Provider after '
            'authentication of user.')
        parser.add_option(
            '-p', '--parse-url', dest='parse_url',
            help='URL the user-agent was redirected to by OpenID provider. '
            'The URL the user-agent was redirected to by OpenID Provider after '
            'authentication of user.')
        parser.add_option(
            '-c', '--cred-file', dest='cred_file',
            help='Credential file to store credential to. ')
        parser.add_option(
            '-u', '--userid', dest='userid',
            help='User supplied identifier. ' )
        parser.add_option(
            '-w', '--weakssl', dest='weakssl', default=False,
            action='store_true', help='Skip ssl cert verification')
        parser.add_option(
            '-s', '--session_id', dest='session_id', default=None,
            help='Session ID to be used during -o initiate; '
                 'needed when -o verify is specified')
        parser.add_option(
            '-k', '--keep_session_file', dest='keep_session_file', default=False,
            help='Leave session file around; '
                 'applicable only when -o verify is specified')

        options, args = parser.parse_args()
        if args:
            parser.error('Expected no arguments. Got %r' % args)
        
        operation = options.operation
        data_path = options.data_path
        attributes = options.attributes
        return_url = options.return_url
        parse_url = options.parse_url
        cred_file = options.cred_file
        userid = options.userid
        weak_ssl = options.weakssl
        session_id = options.session_id
        keep_session_file = options.keep_session_file

        print 'KEEP SESSION FILE IS: ' + str(keep_session_file) + '\n'

#TODO: option checks
        attributes_req = [];
        if attributes:
            for i in attributes.split(","):
                if i in attribute_uris:
                     attributes_req.append(attribute_uris[i])
                else:
                     sys.stderr.write('\nERROR: Unknown attribute "' + i +
                                           '" specified!\n\n')
                     sys.stderr.write('The only known attributes are: \n')
                     for key in attribute_uris:
                         sys.stderr.write(key + '\n')
                     sys.stderr.write('\nYou may add additional attributes '
                         'to the attribute_uris dictionary defined at the '
                         'top of this script. Please refer to '
                         'http://www.axschema.org for additional attributes '
                         'their URIs. Please note that an OpenID '
                         'Provider may or may not support certain attributes.\n')
                     sys.exit(1)

    main(operation, data_path, attributes_req, return_url, parse_url, cred_file, userid, weak_ssl, session_id, keep_session_file)
