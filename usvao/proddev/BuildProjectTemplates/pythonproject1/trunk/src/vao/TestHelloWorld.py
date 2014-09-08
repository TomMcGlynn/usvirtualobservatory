'''
Created on Jan 27, 2011

@author: thomas
'''
import unittest
from vao.HelloWorld import HelloWorld
import xmlrunner


class TestHelloWorld (unittest.TestCase):

    '''
    A Test Class for HelloWorld
    '''

    def setUp (self):
        self.helloworld = HelloWorld()

#    def tearDown (self):
#      pass

    def testMessage (self):
#        print HelloWorld().getMessage();        
        print "Running Test 1"
        self.assertEqual(self.helloworld.getMessage(), "Hello World", "Check message is right")


    def suite (self):

        suite = unittest.TestSuite()
        suite.addTest(unittest.makeSuite(TestHelloWorld))

        return suite


if __name__ == "__main__":
#    import sys
    #;sys.argv = ['', 'Test.test1']
    #unittest.main()
    
    #unittest.TextTestRunner(verbosity=2).run(TestHelloWorld().suite())

    runner = xmlrunner.XmlTestRunner() #sys.stdout)
    runner.run(TestHelloWorld().suite())

