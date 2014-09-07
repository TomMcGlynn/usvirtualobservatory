import os, sys, re, time
from datetime import datetime

class TimestampMatcher(object):
    """
    a utility class for matching and interpreting a date string in a line of 
    text.
    """

    def __init__(self, format, pattern=None):
        self._fmt = format
        if not pattern:
            pattern = "(%D)"
        if isinstance(pattern, str):
            if "%D" in pattern:
                pat = self.date_format_to_repat(self._fmt)
                pattern = pattern.replace("%D", pat)
            pattern = re.compile(pattern)
        if not hasattr(pattern, "search"):
            raise ValueError("TimestampMatcher(): pattern arg not a re or str")

        self._pat = pattern

    fmt2pat = { "%a": "[A-Z][a-z]{2}",
                "%A": "\w+",
                "%b": "[A-Z][a-z]{2}",
                "%B": "\w+",
                "%d": "\d\d?",
                "%H": "\d\d",
                "%I": "\d\d?",
                "%j": "\d{1,3}",
                "%m": "\d\d?",
                "%M": "\d\d",
                "%p": "[AP]M",
                "%S": "\d\d",
                "%U": "\d\d",
                "%w": "\d",
                "%W": "\d\d?",
                "%y": "\d\d",
                "%Y": "\d{4}",
                "%z": "[\+\-]\d{4}",
                "%Z": "\w+",
                "%%": "%",
                }

    @classmethod
    def date_format_to_repat(cls, format):
        """
        convert the given datetime format string into a regular expression
        pattern string.  
        """
        out = format
        for directive in cls.fmt2pat.keys():
            out = out.replace(directive, cls.fmt2pat[directive])
        return out

    def _match(self, line):
        """
        return an re.Match instance if the line contains the matching 
        timestamp or None if it does not. 
        """
        return self._pat.search(line)

    def matches(self, line):
        """
        return True if the line contains a timestamp
        """
        return self._match(line) is not None

    def get_time(self, line):
        """
        return a datetime instance representing the timestamp in the line
        or None if no timestamp is found
        """
        m = self._match(line)
        if not m:
            return None
        timestr = self._get_time_str(m)
        try: 
            return datetime.strptime(timestr, self._fmt)
        except ValueError, ex:
            raise RuntimeError("TimestampMatcher: incompatible format and "+
                               "pattern: \n  " + self._fmt + " vs. \n  " +
                               self._pat.pattern)

    def _get_time_str(self, m):
        # extract the actual timestamp string from an re.Match object.
        # If there are groups present, it will be assumed to be the first 
        # group; otherwise, the entire match will be returned.
        out = m.group()
        g = m.groups()
        if len(g) > 0:
            out = m.group(1)
        return out

tomcat_timestamp_format = "%b %d, %Y %I:%M:%S %p"
vaosso_timestamp_format = "%Y-%m-%d %H:%M:%S"
apache_timestamp_format = "%d/%b/%Y:%H:%M:%S"

tomcat_timestamp_matcher = TimestampMatcher(tomcat_timestamp_format, "^(%D)")
vaosso_timestamp_matcher = TimestampMatcher(vaosso_timestamp_format, "^(%D),")
apache_timestamp_matcher = TimestampMatcher(apache_timestamp_format, " \[(%D) [\-\+](\d{4})\] ")

class LogScanner(object):
    """
    a class for scanning a log file and scraping information from it.
    """
    default_timestamp_matchers = [ vaosso_timestamp_matcher, 
                                   tomcat_timestamp_matcher  ]

    def __init__(self, logfile, proc=None, tsmatchers=None):
        """
        create a scanner to scrape the given log file.  

        Parameters
        ----------
        logfile : str or file object
            the log file to scrape given either as an open file object
            or a file path.  
        proc : LogRecordProcessor
            a record processor to attach that will examine each line for 
            information of interest.
        tsmatchers : list of TimestampMatcher objects
            an ordered list of TimestampMatcher objects used for extracting 
            a timestamp from a log record.  The first matcher to return a 
            datetime will be used as the timestamp for the record.  
        """
        self._buf = []
        self._procs = []
        self._ts = datetime.fromtimestamp(0)

        self._fname = "(file stream)"
        self._strm = logfile
        if isinstance(logfile, str):
            self._fname = logfile
            self._strm = open(logfile)

        self._tsm = self.default_timestamp_matchers[:]
        if tsmatchers:
            if not isinstance(datere, list):
                tsmatchers = [ tsmatchers ]
            bad = filter(lambda e: not isinstance(e, TimestampMatcher), 
                         tsmatchers)
            if len(bad) > 0:
                raise ValueError("LogScanner: tsmatchers not a TimestampMatcher "
                                 + "or a list thereof")
            self._tsm = tsmatchers[:]

        if proc:
            self.add_processor(proc)

    def seek_past_date(self, datetime):
        """
        consume without processing all lines up until a line that is 
        marked with a date after the given one.  
        """
        if self._ts > datetime:
            return

        while True:
            line, ts = self._read_line_time()
            if not line:
                return 
            if ts > datetime:
                self._pushback(line)
                return
            self._ts = ts

    def add_processor(self, processor):
        """
        add a log record processor to this scanner.  Records will be processed 
        by each attached processor in the order that they were added.  

        Parameters
        ----------
        processor : function or function object
           a function that will, with each call, process a record from the 
           log being scanned.  The function must take two arguments: a string 
           containing the log record and a datetime object representing the 
           timestamp (or most recent one) for the record.  A LogRecordProcessor 
           object typically serves this role.  
        """
        if not hasattr(processor, '__call__'):
            raise ValueError("LogScanner: add_processor(): processor not a " +
                             "function or function object")
        self._procs.append(processor)

    @property
    def processors(self):
        """
        a copy of the list of processors currently attached to this scanner
        """
        return self._procs[:]

    def scan(self, until=None):
        """
        scan the log file from its current position applying the filters 
        to each line.

        Parameters
        ----------
        until : date
           stop scanning when the given date has been reached or to the 
           end of the file, which ever comes first.  If not provided, 
           scanning continues to the end of the file.
        """
        if until is not None and self._ts > until:
            return

        for proc in self._procs:
            if hasattr(proc, "start"):
                proc.start(self._ts)

        while True:
            line, ts = self._read_line_time()
            if not line:
                return 
            if until is not None and ts > until:
                self._pushback(line)
                return
            self._ts = ts

            for proc in self._procs:
                proc(line, self._ts)

        for proc in self._procs:
            if hasattr(proc, "finish"):
                proc.finish(self._ts)

    def _readline(self):
        # return the next line
        if len(self._buf) > 0:
            return self._buf.pop(0)
        return self._strm.readline()

    def _read_line_time(self):
        line = self._readline()
        out = [line, self._get_timestamp(line)]
        if not out[1]:
            out[1] = self._ts
        return out

    def _get_timestamp(self, line):
        for matcher in self._tsm:
            out = matcher.get_time(line)
            if out:
                return out
        return None

    def _pushback(self, line):
        self._buf.append(line)

class LogRecordProcessor(object):
    """
    an abstract function class that can process records within a LogScanner
    """

    def matches(self, line):
        """
        return True if the line is of interest to this processor
        """
        raise NotImplementedError("LogRecordProcessor.matches() is abstract")

    def process(self, line, ts):
        """
        the implementation that will actually process a record assuming that 
        matches(line) returns True.
        """
        raise NotImplementedError("LogRecordProcessor.process() is abstract")

    def start(self, startts):
        """
        Prepare to receive records with timestamps after the given one.
        """
        pass

    def finish(self, endts):
        """
        Finish any further processing now that all records with a 
        timestamp before or equal to the given have been consumed.  
        """
        pass

    def __call__(self, line, ts):
        """
        test to see if the given line is of interest by sending it to 
        self.matches(line); if it returns True, process it via self.process().
        """
        if self.matches(line):
            return self.process(line, ts)

class REMatchedLogRecordProcessor(LogRecordProcessor):

    def __init__(self, pattern):
        """
        initialize an instance by setting the pattern that indicates a 
        record of interest.  
        """
        if isinstance(pattern, str):
            pattern = re.compile(pattern)
        self._pat = pattern

    def matches(self, line):
        """
        return True if the line is of interest to this processor.  This 
        implementation returns True in the form of an re.MatchedObject (and 
        False in the form of None).  
        """
        return self._pat.search(line)

class NullProcessor(LogRecordProcessor):
    """
    A do-nothing LogRecordProcessor (used for testing).  
    """
    def matches(self, line):
        return True

    def process(self, line, ts):
        pass
    
class CountAll(LogRecordProcessor):
    """
    A LogRecordProcessor that counts all records consumed (used for testing)
    """
    def __init__(self):
        super(CountAll, self).__init__()
        self.count = 0

    def matches(self, line):
        return True

    def process(self, line, ts):
        self.count += 1
    
