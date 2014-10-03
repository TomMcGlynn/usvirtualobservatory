#! /usr/bin/env python
#
"""
an (incomplete) implementation of XPath v1.0.  This implementation allows but
ignores predicates.
"""
import re, sys, os
from cStringIO import StringIO
from xml.sax import saxutils

dmxlen = 79
dadon = '  '
spre = re.compile('\s+')

class NodeList:
    
    def __init__(self, node=None):
        self.head = node
        (self.tail, self.length) = self._pace(self.head)
        self._lu = None

    def _pace(self, node, length=0):
        if node is None:
            return (node, length)
        elif node.nextSibling is None:
            return (node, length+1)
        else:
            return self._pace(node.nextSibling, length+1)

    def _setlookup(self):
        self._lu = []
        node = self.head
        while node is not None:
            self._lu.append(node._getself())
            node = node.nextSibling

    def appendChain(self, node):
        if node is None: return node
        (tail, length) = self._pace(node)

        if self.head is None:
            self.head = node
            node.previousSibling = None
        else:
            self.tail.nextSibling = node
            node.previousSibling = self.tail

        self.tail = tail
        self.length += length
        
        if self._lu is not None:
            n = node
            while n is not None:
                self._lu.append(n)
                n = n.nextSibling

        return node

    def append(self, node):
        node.nextSibling = None
        return NodeList.appendChain(self, node)

    def _is(self, nodeA, nodeB):
        return nodeA is nodeB

    def insert(self, node, before=None):
        if before is None:
            return self.append(node)

        nd = self.head
        while nd is not None and not self._is(nd, before):
            nd = nd.nextSibling

        if nd is None:
            raise ValueError, "insert node not found"

        if self._lu is not None:
            self._lu = None

        node.previousSibling = nd.previousSibling
        if nd.previousSibling is None:
            self.head = node
        else:
            nd.previousSibling.nextSibling = node
        node.nextSibling = nd
        nd.previousSibling = node
        self.length += 1

    def replace(self, newnode, oldnode):
        if oldnode is None:
            raise ValueError, "old node not found"
        
        nd = self.head
        while nd is not None and not self._is(nd, oldnode):
            nd = nd.nextSibling

        if nd is None:
            raise ValueError, "old node has not been found"

        if self._lu is not None:
            self._lu = None

        if nd.previousSibling is None:
            # nd is the head
            self.head = newnode
        else:
            nd.previousSibling.nextSibling = newnode
        newnode.previousSibling = nd.previousSibling
        newnode.nextSibling = nd.nextSibling
        newnode.parentNode = nd.parentNode
        if self.tail == nd:
            self.tail = newnode

        nd.nextSibling = None
        nd.previousSibling = None
        nd.parentNode = None
        return nd

    def remove(self, oldnode):
        if oldnode is None:
            raise ValueError, "old node not found"
        
        nd = self.head
        while nd is not None and not self._is(nd, oldnode):
            nd = nd.nextSibling

        if nd is None:
            raise ValueError, "old node has not been found"
        if self._lu is not None:
            self._lu = None

        if nd.previousSibling is None:
            self.head = nd.nextSibling
        else:
            nd.previousSibling.nextSibling = nd.nextSibling
        if nd.nextSibling is None:
            self.tail = nd.previousSibling
        else:
            nd.nextSibling.previousSibling  = nd.previousSibling
        nd.nextSibling = None
        nd.previousSibling = None

        self.length -= 1
        return nd

    def item(self, index):
        if self._lu is None or len(self._lu) != self.length:
            self._setlookup()

        if index >= self.length or index < 0:
            return None
        else:
            return self._lu[index]

    def clone(self, parent=None):
        out = NodeList()
        self._copyInto(out, parent)
        return out

    def _copyInto(self, out, parent):
        node = self.head
        while node is not None:
            cnode = node.cloneNode(true)
            if parent is not None:
                cnode.parentNode = parent
            out.append(cnode)

class _WrappedNode:
    def __init__(self, node):
        self.nextSibling = None
        self.previousSibling = None
        self.self = node
    def _getself(self):
        return self.self

class WrappedNodeList(NodeList):
    """This NodeList implementation allows its members to belong to  multiple 
    lists"""

    def __init__(self, node=None):
        NodeList.__init__(self)
        if node is not None: 
            self.append(node)

    def _is(self, nodeA, nodeB):
        if (hasattr(nodeA, "self")): nodeA = nodeA.self
        if (hasattr(nodeB, "self")): nodeB = nodeB.self
        return nodeA is nodeB

    def append(self, node):
        return NodeList.append(self, _WrappedNode(node))
        
    def appendChain(self, node):
        return self.appendChain(WrappedNode(node))

    def insert(self, node, before):
        return NodeList.insert(self, _WrappedNode(node), _WrappedNode(before))

    def replace(self, node, before):
        return NodeList.replace(self, _WrappedNode(node), _WrappedNode(before))

    def remove(self, node):
        return NodeList.remove(self, _WrappedNode(node))

class NamedNodeMap(NodeList):
    def __init__(self, node=None):
        NodeList.__init__(self, node)
        self._map = None

    def _setmap(self):
        self._map = {}
        node = self.head
        while node is not None:
            if node.nodeName is not None:
                self._map[node.nodeName] = node._getself()
            node = node.nextSibling

    def getNamedItem(self, name):
        if self._map is None:
            self._setmap()
        if self._map.has_key(name):
            return self._map[name]
        else:
            return None

    def removeNamedItem(self, name):
        return self.remove(self.getNamedItem(name))

    def setNamedItem(self, node):
        out = None
        old = self.getNamedItem(node.nodeName)
        if old is not None:
            out = self.replace(node, old)
        else:
            self.append(node)

        if self._map is not None:
            self._map[node.nodeName] = node
        return out

    def clone(self, parent=None):
        out = NamedNodeMap()
        self._copyInto(out, parent)

class WrappedNamedNodeMap(WrappedNodeList):
    """This NamedNodeMap implementation allows its members to belong to
    multiple maps"""

    def __init__(self, node):
        NodeList.__init__(self, node)
        self._map = None

    def _setmap(self):
        NamedNodeMap._setmap(self)

    def _deref(self, node):
        if node is not None:
           node = node.self
        return node

    def getNamedItem(self, name):
        return self._deref(NamedNodeMap.getNameItem(self, name))

    def removeNamedItem(self, name):
        return self._deref(NamedNodeMap.removeNamedItem(self, name))

    def setNamedItem(self, node):
        return self._deref(NamedNodeMap.setNamedItem(self, node))

class Node:
    """an implementation of a DOM Node"""

    ELEMENT_NODE                = 1;
    ATTRIBUTE_NODE              = 2;
    TEXT_NODE                   = 3;
    CDATA_SECTION_NODE          = 4;     
    ENTITY_REFERENCE_NODE       = 5;
    ENTITY_NODE                 = 6;  
    PROCESSING_INSTRUCTION_NODE = 7;
    COMMENT_NODE                = 8;
    DOCUMENT_NODE               = 9;
    DOCUMENT_TYPE_NODE          = 10;    
    DOCUMENT_FRAGMENT_NODE      = 11;
    NOTATION_NODE               = 12;

    def __init__(self, type, owner=None):
        self.nodeType  = type
        self.nodeName  = None
        self.nodeValue = None

        self.parentNode = None
        self.childNodes = NodeList()
        self._updateHeadTail()
        self.nextSibling = None
        self.previousSibling = None
        self.attributes = None
        self.ownerDocument = owner

    def _getself(self):
        return self

    def insertBefore(self, newChild, refChild):
        if newChild.ownerDocument is not self.ownerDocument:
            raise ValueError, "new node is from wrong doucment"
        if newChild.parentNode is not None:
            raise ValueError, "new node is already inside a document"

        try:
            self.removeChild(newChild)
        except:
            pass

        newChild.parentNode = self
        out = self.childNodes.insert(newChild, refChild)
        self._updateHeadTail()
        return out

    def _updateHeadTail(self):
        self.firstChild = self.childNodes.head
        self.lastChild = self.childNodes.tail

    def replaceChild(self, newChild, oldChild):
        if newChild.ownerDocument is not self.ownerDocument:
            raise ValueError, "new node is from wrong doucment"
        if newChild.parentNode is not None:
            raise ValueError, "new node is already inside a document"

        out = self.childNodes.replace(newChild, oldChild)
        self._updateHeadTail()
        return out

    def removeChild(self, oldChild):
        out = self.childNodes.remove(oldChild)
        out.parentNode = None
        self._updateHeadTail()
        return out

    def appendChild(self, newChild):
        if newChild.ownerDocument is not self.ownerDocument:
            raise ValueError, "new node is from wrong doucment"
        if newChild.parentNode is not None:
            raise ValueError, "new node is already inside a document"

        newChild.parentNode = self
        out = self.childNodes.append(newChild);
        self._updateHeadTail()
        return out

    def hasChildNodes(self):
        return self.childNodes.length > 0;

    def cloneNode(self, deep):
        out = Node(type)
        self._copyInto(out, deep)
        return out

    def _copyInto(self, out, deep):
        out.nodeName = self.nodeName
        out.nodeValue = self.nodeValue
        out.ownerDocument = self.ownerDocument

        if self.attributes is not None:
            out.attributes = self.attributes.clone()
        if deep:
            out.childNodes = self.childNodes.clone()

    def toxml(self):
        out = StringIO()
        self.writexml(out)
        return out.getvalue()

    def __repr__(self):
        return self.toxml()

    def encode(self, text):
        return saxutils.escape(text.encode('utf_8','xmlcharrefreplace'))

class Text(Node):

    def __init__(self, owner=None, text=''):
        Node.__init__(self, Node.TEXT_NODE, owner)
        self.nodeValue = text
        self.nodeName = "#text"

    def cloneNode(self, deep):
        out = Text(self)
        self._copyInto(out)
        return out

    def getvalue(self):
        return self.nodeValue

    def writexml(self, strm, prefix='', addon=dadon, maxlen=dmxlen):
        strm.write(prefix)
        strm.write(self.encode(self.nodeValue))

class Comment(Node):

    def __init__(self, owner=None, text=''):
        Node.__init__(self, Node.COMMENT_NODE, owner)
        self.nodeValue = text
        self.nodeName = "#comment"

    def cloneNode(self, deep):
        out = Comment(self)
        self._copyInto(out)
        return out

    def writexml(self, strm, prefix='', addon=dadon, maxlen=dmxlen):
        strm.write(prefix)
        strm.write('<!-- ')
        strm.write(self.nodeValue)
        strm.write('-->\n')

    def getvalue(self):
        return ''

class ProcessingInstruction(Node):

    def __init__(self, target, owner=None, text=''):
        Node.__init__(self, Node.PROCESSING_INSTRUCTION_NODE, owner)
        self.nodeValue = text
        self.nodeName = target

    def cloneNode(self, deep):
        out = ProcessingInstruction(self, self.target)
        self._copyInto(out)
        return out

    def writexml(self, strm, prefix='', addon=dadon, maxlen=dmxlen):
        strm.write(prefix)
        strm.write('<?')
        strm.write(self.nodeName)
        strm.write(' ')
        strm.write(self.nodeValue)
        strm.write('?>\n')

    def getvalue(self):
        return ''

class Attr(Node):
    
    def __init__(self, name, value=None, owner=None):
        Node.__init__(self, Node.ATTRIBUTE_NODE, owner)
        self.nodeName = name
        self.nodeValue = value
        self.specified = value is not None
        self.name = name
        self.value = value

    def cloneNode(self, deep):
        out = Attr(self, self.name, self.value)
        self._copyInto(out)
        return out

    def _copyInto(self, out, deep):
        Node._copyInto(self, out, deep)
        out.specified = self.specified

    def getvalue(self):
        if self.value is not None:
            return self.value
        else:
            return ''

    def writexml(self, strm, prefix='', addon=dadon, maxlen=dmxlen):
        strm.write(self.name)
        strm.write('="')
        if self.value is not None:
            strm.write(self.encode(self.value))
        strm.write('"')

class Element(Node):

    def __init__(self, tag, owner=None):
        Node.__init__(self, Node.ELEMENT_NODE, owner)
        self.nodeName = tag
        self.tagName = tag
        self.nodeValue = None
        self.attributes = NamedNodeMap()
        self.oneAttPerLine = False

    def cloneNode(self, deep):
        out = Text(self)
        self._copyInto(out)
        return out

    def getElementsByTagName(self, name):
        raise RuntimeError, "not yet supported"

    def getChildrenByTagName(self, name, out=None):
        """
        return all child elements that match a given name
        @param name        the tag name to match
        @param out         the NodeList object to add the nodes to; if None,
                               one will be created
        @return NodeList   the list of matching nodes
        """
        out = WrappedNodeList()
        child = self.firstChild
        while child is not None:
            if child.nodeType == Node.ELEMENT_NODE and \
               child.nodeName == name:
                out.append(child)
            child = child.nextSibling

        return out

    def getAttributeNode(self, name):
        return self.attributes.getNamedItem(name)

    def setAttributeNode(self, newAttr):
        if newAttr.ownerDocument is not self.ownerDocument:
            raise ValueError, "new attribute is from wrong doucment"
        return self.attributes.setNamedItem(newAttr)

    def removeAttributeNode(self, name):
        return self.attributes.removeNamedItem(name)

    def getAttribute(self, name):
        out = self.getAttributeNode(name)
        if out is None: return ''
        return out.nodeValue

    def setAttribute(self, name, value):
        self.attributes.setNamedItem(
            self.ownerDocument.createAttribute(name, value))

    def removeAttribute(self, name):
        try:
            self.removeAttributeNode(name)
        except:
            pass

    def normalize():
        raise RuntimeError, "normalize not yet supported"

    def writexml(self, strm, prefix='', addon=dadon, maxlen=dmxlen):
        self.writeOpenTag(strm, prefix, maxlen)
        if self.childNodes.length > 0:
            strm.write('>')
            strm.write('\n')
            node = self.firstChild
            while node is not None:
                node.writexml(strm, prefix+addon, addon, maxlen)
                node = node.nextSibling
            self.writeCloseTag(strm, prefix)
        else:
            strm.write('/>')
        strm.write('\n')
        
    def writeOpenTag(self, strm, prefix='', maxlen=dmxlen):
        buf = StringIO();
        buf.write(prefix)
        buf.write('<')
        buf.write(self.tagName)
        attindent = len(buf.getvalue())
        first = True

        if self.attributes.length > 0:
            for i in xrange(0, self.attributes.length):
                att = self.attributes.item(i)
                if not att.specified: 
                    continue
                att = att.toxml()

                if not first and \
                   (self.oneAttPerLine or 
                    len(att)+len(buf.getvalue())+1 > maxlen):

                    strm.write(buf.getvalue())
                    strm.write('\n')
                    buf.close()
                    buf = StringIO()
                    buf.write(attindent*' ')

                buf.write(' ')
                buf.write(att)
                if first: first = False

        strm.write(buf.getvalue());
        return attindent

    def writeCloseTag(self, strm, prefix=''):
        strm.write(prefix)
        strm.write('</')
        strm.write(self.nodeName)
        strm.write('>')

    def getvalue(self):
        out = ''
        child = self.firstChild
        while child is not None:
            out += child.getvalue()
            child = child.nextSibling
        return out

class TextElement(Element):

    def __init__(self, tagname, text='', owner=None):
        Element.__init__(self, tagname, owner)
        self.wrapLines = False
        if owner is not None:
            textNode = owner.createTextNode(text)
            self.appendChild(textNode)
            self.nodeValue = text

    def writexml(self, strm, prefix='', addon=dadon, maxlen=dmxlen):
        self.writeOpenTag(strm, prefix, maxlen)
        strm.write('>')

        if self.wrapLines:
            strm.write('\n')
            self.wraplines(strm, self.encode(self.nodeValue), prefix+addon, 
                           maxlen)
            strm.write(prefix)
        else:
            strm.write(self.encode(self.nodeValue))

        self.writeCloseTag(strm)
        strm.write('\n')

    def wraplines(self, strm, text, prefix=dadon, maxlen=dmxlen):
        maxlen -= len(prefix)-1
        p = 0
        e1 = 0
        e2 = 0
        while p < len(text):
            if len(text)-p <= maxlen:
                e1 = len(text)
            else:
                e2 = text[p+maxlen:].find(' ')
                if e2 < 0:  e2 = len(text)
                e1 = text[p:p+maxlen+e2].rfind(' ')
                if e1 < 0:  e1 = e2

            strm.write(prefix)
            strm.write(text[p:p+e1])
            strm.write('\n')

            p += e1+1
            while p < len(text) and text[p] == ' ': p += 1


class Document(Node):

    def __init__(self, tag=None):
        Node.__init__(self, Node.DOCUMENT_NODE, self)
        self.nodeName = "#document"
        self.nodeValue = None
        self.documentElement = None
        if tag is not None:
            self.appendChild(self.createElement(tag))

    def appendChild(self, newChild):
        if self.documentElement is not None and \
                newChild.nodeType == Node.ELEMENT_NODE:
            raise RuntimeError, "Document already has root node"
        out = Node.appendChild(self, newChild)
        if newChild.nodeType == Node.ELEMENT_NODE:
            self.documentElement = newChild
        return out

    def insertBefore(self, newChild, refChild):
        if self.documentElement is not None and \
                newChild.nodeType == Node.ELEMENT_NODE:
            raise RuntimeError, "Document already has root node"
        if newChild.nodeType == Node.ELEMENT_NODE:
            self.documentElement = newChild
        return Node.insertBefore(self, newChild, refChild)

    def replaceChild(self, newChild, oldChild):
        if self.documentElement is not None and \
                oldChild.nodeType != Node.ELEMENT_NODE and \
                newChild.nodeType == Node.ELEMENT_NODE:
            raise RuntimeError, "Document already has root node"
        out = Node.replaceChild(self, newChild, oldChild)
        if out.nodeType == Node.ELEMENT_NODE:
            self.documentElement = out
        return out

    def removeChild(self, oldChild):
        out = Node.removeChild(self, oldChild)
        if oldChild.nodeType == Node.ELEMENT_NODE:
            self.documentElement = None
        return out

    def createComment(self, data):
        return Comment(self, data)

    def createTextNode(self, data):
        return Text(self, data)

    def createElement(self, tagname):
        return Element(tagname, self)

    def createTextElement(self, tagname, text):
        return TextElement(tagname, text, self)

    def createAttribute(self, name, value=None):
        return Attr(name, value, self)

    def createProcessingInstruction(self, target, data):
        return ProcessingInstruction(target, self, data)

    def getElementsByTagName(self, name):
        raise RuntimeError, "not yet supported"

    def importNode(self, node, remove=True):
        if remove and node.parentNode is not Node:
            node.parentNode.removeChild(node)
        node.ownerDocument = self

        if node.attributes is not None:
            for i in xrange(0, node.attributes.length):
                self.importNode(node.attributes.item(i), False)

        node = node.firstChild
        while node is not None:
            self.importNode(node, False)
            node = node.nextSibling

    def writexml(self, strm, prefix='', addon=dadon, maxlen=dmxlen):
        strm.write(prefix)
        strm.write('<?xml version="1.0" encoding="UTF-8"?>\n')
        node = self.firstChild
        while node is not None:
            node.writexml(strm, prefix, addon, maxlen)
            node = node.nextSibling

    def getvalue(self):
        out = ''
        child = firstChild
        while child is not None:
            out += child.getvalue
        return out

