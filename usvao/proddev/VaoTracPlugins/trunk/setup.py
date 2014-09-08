from setuptools import setup


setup(name='VaoTracPlugin',
      version='0.1',
      packages=['vaotracplugin'],
      author='Ray Plante',
      author_email='rplante@ncsa.uiuc.edu',
      description='A custom plugin for the VAO Trac, including exporting pages as OpenDocument (ODT) files',
      license='Trac license',
      url='http://dev.usvao.org/vao/sw/VaoTracPlugin',
      entry_points={'trac.plugins': ['vaotracplugin.odtexport=vaotracplugin.odtexport',
                                     'vaotracplugin.OdtTemplate=vaotracplugin.OdtTemplate',
                                     'vaotracplugin.VaoDocVersion=vaotracplugin.VaoDocVersion',
                                     'vaotracplugin.VaoDocDate=vaotracplugin.VaoDocDate',
                                     'vaotracplugin.VaoDocAuthor=vaotracplugin.VaoDocAuthor',
                                     'vaotracplugin.Cite=vaotracplugin.Cite',
                                     'vaotracplugin.Ref=vaotracplugin.Ref',
                                     'vaotracplugin.VaoLongDocument=vaotracplugin.VaoLongDocument']},
      package_data={'vaotracplugin': ['xsl/*.xsl',
                                      'xsl/document-content/*.xsl',
                                      'xsl/specific/*.xsl',
                                      'xsl/styles/*.xsl',
                                      'templates/*.odt',
                                      'templates/*.txt',
                                 ]},
     # install_requires = [ 'Trac', 'uTidylib', 'lxml', 'PIL', ],
     install_requires = [ 'Trac', 'lxml' ],
     )



