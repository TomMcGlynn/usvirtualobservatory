#! /usr/bin/env python
#
from domimpl import Node
import re

class XPathError(Exception):
    def __init__(self, msg):
        self.message = msg
    def __str__(self):
        return repr(self.message)

class XPathSyntaxError(XPathError):
    """
    an exception indicating that a syntax error was found while parsing 
    an XPath.
    """
    def __init__(self, msg):
        XPathError.__init__(self, msg)

class UnsupportedSyntaxError(XPathError):
    """
    an exception indicating that not-yet supported syntax was encountered in
    an XPath.
    """
    def __init__(self, seq, msg=None):
        self.seq = seq
        if msg is None:  
            msg = 'Unsupported syntax: %s (sorry!)' % seq
        XPathError.__init__(self, msg)

def match(node, xpath):
    """
    return a list of nodes that matches the given XPath relative to a 
    given node.
    """
    return XPath(xpath).match(node)

def getvalues(node, xpath):
    """
    return string values of nodes matching the given XPath relative to a 
    given node.
    """
    return map(lambda x: x.getvalue(), match(node, xpath))

def thevalue(node, xpath):
    """
    return the string value of the first node matching the given XPath 
    relative to a given node.
    """
    m = match(node, xpath)
    if len(m) > 0:
        return m[0].getvalue()
    else:
        return ''

stepre = re.compile(r'[^\s\[\/]+')
fullstepre = re.compile(r'^(\w+)::([\w\(\)]+)$')

class XPath:
    """an (incomplete) implementation of XPath v1.0"""

    def __init__(self, xpath=None):
        self.steps = []
        self.xpath = xpath
        if xpath is not None:
            self.parse(xpath)

    def parse(self, xpath):
        self.steps = []
        if xpath is None:  return 
        xpath = xpath.strip()

        if xpath.startswith('///'):
            raise XPathSyntaxError("Illegal xpath char. sequence at '///'")
        if xpath.startswith('/'):
            self.steps.append(LocationStep(RootAxis, 'node()'))
            xpath = xpath[1:]

        while len(xpath) > 0:
            if xpath.startswith('/'):
                self.steps.append(LocationStep(DescendantOrSelfAxis, 'node()'))
                                              
                xpath = xpath[1:]
                if len(xpath) == 0:  break

            if xpath.startswith('/'):
                raise XPathSyntaxError("Illegal xpath char. sequence at '///'")

            stepmch = stepre.match(xpath)
            if stepmch is None:
                raise XPathSyntaxError("Illegal xpath char. sequence at '%s'" %
                                       xpath)
            stepstr = stepmch.group(0)
            xpath = xpath[len(stepstr):].strip()

            # get predicates
            predicates = []
            while xpath.startswith('['):
                predicates.append(self.bitePredicate(xpath))
                xpath[len(predicates[-1]):]

            self.steps.append(self.parseStep(stepstr, predicates))

            if len(xpath) > 0 and xpath[0] != '/':
                raise XPathSyntaxError("Illegal xpath char. sequence at '%s'" %
                                       xpath)
            xpath = xpath[1:]

    def parseStep(self, step, predicates=None):
        axiscl = None
        fullstepmch = fullstepre.match(step)
        if fullstepmch is not None:
            nodeTest = fullstepmch.group(2)
            axis = fullstepmch.group(1)
            if axis == 'following-sibling':
                axiscl = FollowingSiblingAxis
            elif axis == 'previous-sibling':
                axiscl = PreviousSiblingAxis
            elif axis == 'ancestor':
                axiscl = AncestorAxis
            elif axis == 'ancestor-or-self':
                axiscl = AncestorOrSelfAxis
            elif axis == 'descendant':
                axiscl = DescendantAxis
            elif axis == 'descendant-or-self':
                axiscl = DescendantOrSelfAxis
            elif axis == 'self':
                axiscl = SelfAxis
            elif axis == 'child':
                axiscl = ChildAxis
            elif axis == 'parent':
                axiscl = ParentAxis
            elif axis == 'attribute':
                axiscl = AttributeAxis
            elif axis == 'namespace':
                raise UnsupportedSyntaxError(axis)
            else:
                raise XPathSyntaxError(axis)
        else:
            if step.startswith('@'):
                axiscl = AttributeAxis
                nodeTest = step[1:]
            elif step == '*':
                axiscl = ChildAxis
                nodeTest = 'node()'
            elif step == '.':
                axiscl = SelfAxis
                nodeTest = 'node()'
            elif step == '..':
                axiscl = ParentAxis
                nodeTest = 'node()'
            else:
                axiscl = ChildAxis
                nodeTest = step

        return LocationStep(axiscl, nodeTest, predicates)

    def bitePred(self, xpath):
        if not xpath.startswith('['): return None
        lev = 1
        out = '['
        xpath = xpath[1:]
        quote = None

        while lev > 0 and len(xpath) > 0:
            parts = predcharre.split(xpath, 1)
            if len(parts) == 1:
                raise XPathSyntaxError("Illegal predicate syntax: %s" % parts[0])

            if quote is None:
                if parts[1] == '[':
                    lev += 1
                elif parts[1] == ']':
                    lev -= 1
                else:
                    quote = parts[1]
            elif quote == parts[1]:
                quote = None

            out += parts[0] + parts[1]

            if len(parts) > 2:
                xpath = parts[2]
            else:
                xpath = ''

        return out

    def match(self, node):
        """
        return a list of nodes that matches the given XPath relative to a 
        given node
        """
        nset = [node]
        out = []
        for step in self.steps:
            out = []
            if len(nset) == 0:  return out
            for nd in nset:
                out.extend(step.match(nd))
            nset = out
        return out

    def toString(self, abbrev=True):
        out = ''
        for i in xrange(0, len(self.steps)):
            if i > 0:  out += '/'
            out += self.steps[i].toString(abbrev)

        if len(out) == 0:
            out = self.xpath

        return out

    def __repr__(self):
        return "'%s'" % self.toString()

    def __str__(self):
        return self.toString()

class LocationStep:
    """a representation of a single location step"""

    def __init__(self, axisClass, nodeTest, predicates=None):
        self.axis = axisClass
        self.nodeTest = NodeTest(nodeTest)
        if predicates is None:  predicates = []
        self.predicates = map(Predicate, predicates)

    def match(self, node):
        out = []
        iter = self.axis(node, self.nodeTest)
        node = iter.next()
        while node is not None:
            out.append(node)
            node = iter.next()

        for pred in self.predicates:
            items = []
            items.extend(out)
            length = len(items)
            for i in xrange(0, length):
                if self.predicate.select(items[i], i, length):
                    out.append(items[i])

        return out

    def toString(self, abbrev=False):
        out = self.axis.toString(self.nodeTest.type, abbrev)
        for p in self.predicates:
            out += p
        return out

    def __repr__(self):
        return "'%s'" % self.toString()

class Predicate:
    def __init(self, predicate):
        self.pred = predicate

    def select(contextNode, pod, setLength):
        return True

class AxisIterator:
    def __init__(self, root, nodeTest):
        self.root = root
        self.nodeTest = nodeTest

    def select(self, node):
        if not self.nodeTest.select(node):
            return False
        return True

    def next(self):
        return None

    def toString(cls, nodeTest, abbrev=False):
        if abbrev:
            return cls.abbreviate(nodeTest)
        else:
            return "%s::%s" % (cls.axis, nodeTest)
    toString = classmethod(toString)

    def abbreviate(cls, nodeTest):
        return cls.toString(nodeTest, False)
    abbreviate = classmethod(abbreviate)


class ChildAxis(AxisIterator):
    """a representation of a child axis step"""

    axis = "child"

    def __init__(self, root, nodeTest):
        AxisIterator.__init__(self, root, nodeTest)
        self.nxt = root.firstChild

    def next(self):
        while self.nxt is not None and not self.select(self.nxt):
            self.nxt = self.nxt.nextSibling
        out = self.nxt
        if self.nxt is not None:
            self.nxt = self.nxt.nextSibling
        return out

    def abbreviate(cls, nodeTest):
        if nodeTest != "node()" and nodeTest != "text()":
            return nodeTest
        else:
            return "parent::%s" % nodeTest
    abbreviate = classmethod(abbreviate)

class DescendantAxis(AxisIterator):
    """a representation of a descendant axis step"""

    axis = "descendant"

    def __init__(self, root, nodeTest):
        AxisIterator.__init__(self, root, nodeTest)
        self.nxt = [ root.firstChild ]

    def nextInAxis(self):
        if len(self.nxt) == 0:  return None
        out = self.nxt[-1]

        if out.nextSibling is None:
            self.nxt.pop()
        else:
            self.nxt[-1] = out.nextSibling

        if out.hasChildNodes() and \
           (out.nodeType == Node.ELEMENT_NODE or 
            out.nodeType == Node.DOCUMENT_NODE):
            self.nxt.append(out.firstChild)

        return out

    def next(self):
        out = self.nextInAxis()
        while out is not None and not self.select(out):
            out = self.nextInAxis()
        return out

class ParentAxis(AxisIterator):
    """a representation of a parent axis step"""

    axis = "parent"

    def __init__(self, root, nodeTest):
        AxisIterator.__init__(self, root, nodeTest)
        self.nxt = root.parentNode

    def next(self):
        out = self.nxt
        self.nxt = None
        return out

    def abbreviate(cls, nodeTest):
        if nodeTest == "node()":
            return ".."
        else:
            return "parent::%s" % nodeTest
    abbreviate = classmethod(abbreviate)

class AncestorAxis(AxisIterator):
    """a representation of an ancestor axis step"""

    axis = "ancestor"

    def __init__(self, root, nodeTest):
        AxisIterator.__init__(self, root, nodeTest)
        self.nxt = root.parentNode

    def next(self):
        out = self.nxt
        while out is not None and not self.select(out):
            out = out.parentNode
        return out

class AncestorOrSelfAxis(AncestorAxis):
    """a representation of an ancestor-or-self axis step"""

    axis = "ancestor-or-self"

    def __init__(self, root, nodeTest):
        Ancestor.__init__(self, root, nodeTest)
        self.nxt = root

class DescendantOrSelfAxis(DescendantAxis):
    """a representation of an descendant-or-self axis step"""

    axis = "descendant-or-self"

    def __init__(self, root, nodeTest):
        DescendantAxis.__init__(self, root, nodeTest)
        self.nxt = root

    def abbreviate(cls, nodeTest):
        if nodeTest == "node()":
            return ""
        else:
            return "descendant-or-self::%s" % nodeTest
    abbreviate = classmethod(abbreviate)

class FollowingSiblingAxis(ChildAxis):
    """a representation of an following-sibling axis step"""

    axis = "following-sibling"

    def __init__(self, root, nodeTest):
        AxisIterator.__init__(self, root, nodeTest)
        self.nxt = root.nextSibling

class PrecedingSiblingAxis(AxisIterator):
    """a representation of a preceing-sibling axis step"""

    axis = "preceding-sibling"

    def __init__(self, root, nodeTest):
        AxisIterator.__init__(self, root, nodeTest)
        self.nxt = root.previousSibling

    def next(self):
        out = self.nxt
        while out is not None and not self.select(out):
            out = out.previousSibling
        return out

class FollowingAxis(AxisIterator):
    """a representation of a following axis step"""

    axis = "following"

    def __init__(self, root, nodeTest):
        AxisIterator.__init__(self, root, nodeTest)
        self.nxt = root.nextSibling
        self.nodeTest = nodeTest
        self.desc = DescendantOrSelfAxis(self.nxt, nodeTest)

    def nextInAxis(self):
        out = self.desc.next()
        if out is not None:  return out

        self.nxt = self.nxt.nextSibling
        if self.nxt is None:  return None
        self.desc = DescendantOrSelfAxis(self.nxt, self.nodeTest)
        return self.nextInAxis()

    def next(self):
        out = self.nextInAxis()
        while out is not None and not self.select(out):
            out = self.nextInAxis()
        return out


class PrecedingAxis(AxisIterator):
    """a representation of a preceing axis step"""

    axis = "preceding"

    def __init__(self, root, nodeTest):
        AxisIterator.__init__(self, root, nodeTest)
        self.nxt = [ ]
        if root.previousSibling is not None:
            self.nxt.append(root.previousSibling)
        self.appendLasts()

    def appendLasts(self):
        while self.nxt[-1].hasChildNodes():
            self.nxt.append(self.nxt[-1].lastChild)

    def nextInAxis(self):
        if len(self.nxt) == 0: return None
        out = self.nxt[-1]
        if out.previousSibling is None:
            self.nxt.pop()
        else:
            self.nxt[-1] = out.previousSibling
            self.appendLasts()
        return out

    def next(self):
        out = self.nextInAxis()
        while out is not None and not self.select(out):
            out = self.nextInAxis()
        return out


class AttributeAxis(AxisIterator):
    """a representation of an attribute axis step"""

    axis = "attribute"

    def __init__(self, root, nodeTest):
        AxisIterator.__init__(self, root, nodeTest)
        self.nxt = 0
        self.attrs = None
        if root.nodeType == Node.ELEMENT_NODE:
            self.attrs = root.attributes

    def next(self):
        i = self.nxt
        while i < self.attrs.length and not self.select(self.attrs.item(i)):
            i += 1
        self.nxt = i + 1
        return self.attrs.item(i)

    def abbreviate(cls, nodeTest):
        if nodeTest == "node()":
            return "@*"
        elif nodeTest == "text()":
            return "attribute::text()"
        else:
            return "@%s" % nodeTest
    abbreviate = classmethod(abbreviate)

class SelfAxis(AxisIterator):
    """a representation of a self axis step"""

    axis = "self"

    def __init__(self, root, nodeTest):
        AxisIterator.__init__(self, root, nodeTest)
        self.nxt = root

    def next(self):
        out = self.nxt
        self.nxt = None
        return out

    def abbreviate(cls, nodeTest):
        if nodeTest == "node()":
            return "."
        else:
            return "self::%s" % nodeTest
    abbreviate = classmethod(abbreviate)

class RootAxis(SelfAxis):
    """a representation of the root axis step"""

    axis = '/'

    def __init__(self, root, nodeTest):
        AxisIterator.__init__(self, root, nodeTest)
        self.nxt = root.ownerDocument

    def toString(cls, nodeTest, abbrev=False):
        return ''
    toString = classmethod(toString)

class NodeTest:
    """an abstract function class representing of a node test.  The 
select() function will return whether a given node should be selected."""

    def __init__(self, nodeTest):
        self.type = nodeTest
        if nodeTest == "*" or nodeTest == "node()":
            self.select = self.selectAny
        elif nodeTest == "text()":
            self.select = self.selectText
        else:
            self.select = self.selectElement

    def selectAny(self, node):
        """This implementation returns any child (implements "*")"""
        return True

    def selectText(self, node):
        return node.nodeType == Node.TEXT_NODE

    def selectElement(self, node):
        return node.nodeName == self.type

    def __repr__(self):
        return "'%s'" % self.type

    def __str__(self):
        return self.type

