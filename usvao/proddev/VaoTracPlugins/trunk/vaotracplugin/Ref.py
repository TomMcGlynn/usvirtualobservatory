from trac.wiki.macros import WikiMacroBase

class Ref(WikiMacroBase):
    """
    Format the version string for this document
    """
    def expand_macro(self, formatter, name, content):
        return '<a name="ref%s">[%s]</a>' % (content, content)





