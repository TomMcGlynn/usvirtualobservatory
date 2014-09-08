from trac.wiki.macros import WikiMacroBase

class VaoDocDate(WikiMacroBase):
    """
    Format the date string for this document
    """
    def expand_macro(self, formatter, name, args):
        return '<b>%s</b><br />' % args



