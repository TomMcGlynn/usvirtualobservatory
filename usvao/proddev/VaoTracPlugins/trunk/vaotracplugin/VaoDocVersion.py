from trac.wiki.macros import WikiMacroBase

class VaoDocVersion(WikiMacroBase):
    """
    Format the version string for this document
    """
    def expand_macro(self, formatter, name, args):
        return '<b>Version %s</b><br />' % args



