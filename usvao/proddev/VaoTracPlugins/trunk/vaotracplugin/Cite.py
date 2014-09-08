from trac.wiki.macros import WikiMacroBase

class Cite(WikiMacroBase):
    """
    Format the version string for this document
    """
    def expand_macro(self, formatter, name, content):
        return '<a href="#ref%s">[%s]</a>' % (content, content)





