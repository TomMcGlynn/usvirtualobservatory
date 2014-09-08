from trac.wiki.macros import WikiMacroBase

class VaoDocAuthor(WikiMacroBase):
    """
    Format the author string for this document
    """
    def expand_macro(self, formatter, name, args):
        return '<b>%s</b><br />' % args



