"""
functions that navigate through the login and attribute confirmation pages
for testing.  
"""

import os, sys, pdb, urllib2, cookielib, re
from urllib2 import HTTPError, HTTPRedirectHandler
from vaologin.authenticate import Attributes

testusername = "unittest"
testpassword = "success"
returnurl = "http://example.org/protected/welcome.html"

class SSOSite(object):

    def __init__(self, server, return_url=None, cookiejar=None):
        self.server = server
        self.signin_url = "https://%s/openid/signin" % server
        self.status_url = "https://%s/openid/loginStatus" % server

        if not return_url:
            return_url = returnurl
        self.return_url = return_url
        if not cookiejar:
            policy = \
                cookielib.DefaultCookiePolicy(allowed_domains=[server])
            cookiejar = cookielib.CookieJar(policy)
        self.cj = cookiejar
        self.site = urllib2.build_opener(urllib2.HTTPCookieProcessor(self.cj), 
                                         _TestRedirectHandler(return_url))

    def login(self, login_url, attributes=None, confirm=True, 
              passwd=None, username=None):
        """
        @param login_url   the URL for the login page as returned by the openid
                             authentication reqest operation
        @param attributes  the list of attributes that were requested (and we
                             must provide confirmation for)
        @param confirm     a boolean indicating whether confirmation should be 
                             given to login in to the portal for the first time.
        @param passwd      an incorrect password to use to login
        @param username    the user to login as
        @returns str  the argument-enhanced return URL or None if the login
                           fails
        """
        password = passwd or testpassword
        if not username:  username = testusername
        fd = self.site.open(login_url)
        fd.close()
        logindata = "username=%s&password=%s&interactive=true&logout=false" % \
            (username, password)
        fd = self.site.open(self.signin_url, logindata)
        if passwd:
            # assume the provided password is bogus
            fd.close()
            return None

        if confirm:
            fd.close()
            logindata = "confirm=true&share_username=true"
            if attributes:
                for a in attributes:
                    if a == "username": continue
                    if Attributes.shortname.has_key(a):
                        a = Attributes.shortname[a]
                    if a.startswith('http'): continue
                    if a == "cert" or a == "certificate":
                        logindata += "&share_credentials=true"
                    else:
                        logindata += "&share_%s=true" % a
            fd = self.site.open(self.signin_url, logindata)
            redirect = None
            if 'location' in fd.info():
                redirect = fd.info().getheaders('location')[0]
            fd.close()
            return redirect
        else:
            are = re.compile(r'href="(%s[^"]*)"' % self.return_url)
            for line in fd:
                m = are.search(line)
                if m:
                    fd.close()
                    return m.group(1)
            fd.close()

        return None

    def logout(self, logout_url):
        fd = self.site.open(logout_url)
        redirect = None
        if 'location' in fd.info():
            redirect = fd.info().getheaders('location')[0]
        fd.close()
        return redirect
        
    def login_status(self):
        import json

        fd = self.site.open(self.status_url)
        status = json.load(fd)
        fd.close()

        return status

    def logged_in(self):
        return self.login_status().get("state") == "in"
        
    
class _TestRedirectHandler(HTTPRedirectHandler):

    def __init__(self, return_url):
        self._returl = return_url

    def http_error_302(self, req, fp, code, msg, headers):
        newurl = None
        if 'location' in headers:
            newurl = headers.getheaders('location')[0]
        elif 'uri' in headers:
            newurl = headers.getheaders('uri')[0]

        if newurl and newurl.startswith(self._returl):
            return HTTPError(req.get_full_url(), code, "Okay", headers, fp)

        return HTTPRedirectHandler.http_error_302(self, req, fp, code, msg, 
                                                  headers)
