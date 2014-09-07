import os, sys, re

from .scan import LogRecordProcessor, REMatchedLogRecordProcessor

class CountMatched(REMatchedLogRecordProcessor):
    """
    a LogRecordProcessor that counts the number of records matching a 
    particular pattern.
    """

    def __init__(self, pattern):
        super(CountMatched, self).__init__(pattern)
        self.count = 0

    def process(self, line, ts):
        self.count += 1

