#main app

import sys

sys.path.append("../common")
sys.path.append("../config")
sys.path.append("../queue")
from filesystem import FileSystem
from config import Config
from queue import *

class Watcher(object):
    def __init__(self):
        self.__fs = FileSystem(".")
        self.__config = Config(fileSystem=self.__fs)
        
        self.__eventWatcherClass = None
        self.__db = None
        self.__watchDirs = self.__config.watchDirs
        
        self.watcher = None
        self.db = None
        self.queue = None
        
        self._setup()
        
    def _setup(self):
        #watcher will publish if there's even....
        #queue do not need to know about the watcher
        
        if self.__config.platform:
            fsWatcherPath = "../fswatcher/%s" % self.__config.platform
            sys.path.append(fsWatcherPath)
            watchLib = "%sWatcher" % self.__config.platform
            __import__(watchLib)
            self.__eventWatcherClass = sys.modules[watchLib].EventWatcherClass
            
        if self.__config.db:
            dbPath = "../db/%s" % self.__config.db
            sys.path.append(dbPath)
            dbLib = "%sdb" % self.__config.db
            __import__(dbLib)
            self.__db = sys.modules[dbLib].Database
        
        if self.__db and self.__eventWatcherClass:
            #setup db
            self.db = self.__db()
            
            #setup queue
            self.queue = Queue(self.db)
            
            #setup Watcher        
            self.watcher = self.__eventWatcherClass(self.__watchDirs, queue=self.queue, fs=self.__fs)

            self.watcher.addListener(self.queue.put)




        
#            try:
#                self.watcher.notifier.process_events()
#                
#                self.queue.processingQueue()
#                while self.watcher.notifier.process_events():
#                    self.watcher.notifier.read_events()
#            except KeyboardInterrupt:
#                # ...until c^c signal
#                print 'stop monitoring...'
#                # stop monitoring
#                self.watcher.notifier.stop()
#    
#            except Exception, err:
#                # otherwise keep on watching
#                print err
#                self.watcher.notifier.stop()
                
        
#        #try to create watcher for each directory (for now only 1 dir... don know how to setup multiple directory to run as deamon)
#        if self.__eventWatcherClass is not None:
#            for dir in self.__watchDirs:
#                eventWatcher = self.__eventWatcherClass(dir, self.__fs)
#                try:
#                    eventWatcher.notifier.process_events()
#                    
#                    
#                    while eventWatcher.notifier.check_events():
#                        eventWatcher.notifier.read_events()
#                    
#                except KeyboardInterrupt:
#                    # ...until c^c signal
#                    print 'stop monitoring...'
#                    # stop monitoring
#                    eventWatcher.notifier.stop()
#        
#                except Exception, err:
#                    # otherwise keep on watching
#                    print err
#                    eventWatcher.notifier.stop()
                    
            
if __name__ == "__main__":
    watcher = Watcher()