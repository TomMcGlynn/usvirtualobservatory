#! /usr/bin/env python
#
from domimpl import Document, Node
from xml.sax.handler import ContentHandler
from xml.sax.xmlreader import Locator
import xml.sax
import re

ignorable = re.compile(r'^\s*$')

class DOMLoader(ContentHandler):

    def __init__(self, root=None):
        if root is None:
            self.document = None
            self.root = None
        else:
            self.root = root
            self.document = root.ownerDocument

        self.locator = None
        self.parent = None
        self.text = None

    def setDocumentLocator(self, locator):
        self.locator = locator

    def startDocument(self):
        if self.root is None:
            self.document = Document()
            self.root = self.document

        if self.parent is None:
            self.parent = self.root

    def endDocument(self):
        self.text = None

    def appendText(self, parent, text):
        newchild = self.document.createTextNode(text)
        parent.appendChild(newchild)

    def startElement(self, name, attrs):
        if self.parent is not None:
            # dispense with any pending text
            if self.text is not None:
                if not ignorable.match(self.text):
                    self.appendText(self.parent, self.text)
                self.text = None

            newchild = self.document.createElement(name)
            self.parent.appendChild(newchild)
            self.parent = newchild

            if attrs is not None:
                for attr in attrs.getNames():
                    newchild.setAttribute(attr, attrs.getValue(attr))

    def endElement(self, name):
        if self.parent is not None:
            # dispense with any pending text
            if self.text is not None:
                if not ignorable.match(self.text):
                    self.appendText(self.parent, self.text)
                self.text = None

            el = self.parent
            self.parent = self.parent.parentNode

            # see if we need to convert this element to a TextElement type:
            # if this element contains only Text children, replace it
            if el.attributes.length == 0:
                text = ''
                child = el.firstChild
                while child is not None:
                    if child.nodeType != Node.TEXT_NODE:
                        text = ''
                        break
                    text += child.nodeValue
                    child = child.nextSibling

                if text != '':
                    # replacing ...
                    child = self.document.createTextElement(el.nodeName, text)
                    self.parent.replaceChild(child, el)
            
    def characters(self, content):
        if self.text is None:
            self.text = content
        else:
            self.text += content
#        if self.parent is not None and not ignorable.search(content):
#            newchild = self.document.createTextNode(content)
#            self.parent.appendChild(newchild)

    def ignorableWhitespace(self, whitespace):
#        self.characters(whitespace)
        pass

    def getRoot(self):
        return self.parent

    def getDocument(self):
        return self.document

def parse(istrm, addTo=None):
    ch = DOMLoader(addTo)
    xml.sax.parse(istrm, ch)
    return ch.getRoot()


    
