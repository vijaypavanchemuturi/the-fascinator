import sys

sys.path.append("../common")
sys.path.append("../atom")
sys.path.append("../config")

from filesystem import FileSystem
from config import Config
from atomfeed import AtomFeed

from paste import httpserver


class PasteServer(object):
    def __init__(self):
        self.__fs = FileSystem(".")
        self.__config = Config(fileSystem=self.__fs)
        self.host = self.__config.host
        self.port = self.__config.port
        self.atomUrl = self.__config.atomUrl
        
        self.db = None
        if self.__config.db:
            dbPath = "../db/%s" % self.__config.db
            sys.path.append(dbPath)
            dbLib = "%sdb" % self.__config.db
            __import__(dbLib)
            self.__db = sys.modules[dbLib].Database
        self.db = self.__db()
        
    def getFeed(self, environ, start_response):
        self.atom = AtomFeed(self.db, atomUrl=self.atomUrl)
        start_response('200 OK', [('content-type', 'text/xml')])
        return self.atom.getFeed()
        

if __name__ == "__main__":
    ps = PasteServer()
    httpserver.serve(ps.getFeed, host=ps.host, port=ps.port)
    