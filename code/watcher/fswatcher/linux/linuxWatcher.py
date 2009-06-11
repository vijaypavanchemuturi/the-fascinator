import pynotify, datetime, sys
from pyinotify import EventsCodes, ProcessEvent, WatchManager, WatchManagerError, Notifier, ThreadedNotifier, ExcludeFilter

class EventWatcherClass(object):
    FLAGS = EventsCodes.ALL_FLAGS
    mask  = FLAGS['IN_DELETE'] | FLAGS['IN_CREATE'] | \
            FLAGS['IN_MOVED_FROM'] | FLAGS['IN_MODIFY'] | \
            FLAGS['IN_MOVED_TO'] | FLAGS['IN_ATTRIB'] | FLAGS['IN_IGNORED'] | FLAGS['IN_MOVE_SELF']
     
    def __init__(self, fs, db, config, daemonize=False, forcekill=False, exclFileName="", exclFile=None):
        self.__listeners = []
        self.__dirToBeWatched = []
        
        self.__fs = fs
        self.__db = db
        self.__config = config
        
        self.__getDirToBeWatched()
                
        #Main variable for notifier and watcher
        self.__daemonize = daemonize
        self.__forcekill = forcekill
        self.notifier = None
        self.__watchManager = WatchManager()
        self.__cp = CustomProcess(self, self.__fs, self.__listener, self.configFile)
        self.__exclFileName = exclFileName
        self.__exclFile = exclFile
        self.wm = None
        
        self.excl = None
        if self.__exclFile and self.__exclFileName:
            self.excl = ExcludeFilter({self.__exclFileName: self.__exclFile})
        
        #preprocess directory structure to database
        self.__preprocessDirFileStructure()
        self.__startWatcher()


    def __getDirToBeWatched(self):
        self.__dirToBeWatched = self.__config.watchDirs
        if self.__dirToBeWatched == []:
            self.__dirToBeWatched.append(os.path.expanduser("~"))  #watching home directory by default
            
        #also watch the config file
        self.configFile = self.__config.configFilePath
        if self.configFile not in self.__dirToBeWatched:
            self.__dirToBeWatched.append(self.configFile)
        

    def __preprocessDirFileStructure(self):
        def callback(path, dirs, files):
            for dir in dirs:
                rDir.append(path + dir)
            for file in files:
                rFiles.append(path + file)
        
        rFiles = []    
        rDir = []
        for dir in self.__dirToBeWatched:
            if self.__fs.isDirectory(dir):
                self.__fs.walker(dir, callback)
                for rdir in rDir:
                    self.__db.insertDir(rdir)
                for rfile in rFiles:
                    self.__db.insertFile(("mod", rfile, datetime.datetime.now()), update=False)

    def addListener(self, listener):
        self.__listeners.append(listener)

    def removeListener(self, listener):
        if self.__listeners.count(listener)>0:
            self.__listeners.remove(listener)
            return True
        return False

    def __listener(self, *args, **kwargs):
        for listener in self.__listeners:
            try:
                listener(*args, **kwargs)
            except Exception, e:
                print str(e)
                pass
        
    def restartWatcher(self):
        try:
            #reload the config file
            self.__config.reload()
            self.__getDirToBeWatched()
            if self.wm is not None:
                self.__watchManager.rm_watch(self.wm.values())
            self.wm = self.__watchManager.add_watch(self.__dirToBeWatched, self.mask, rec=True, 
                                          auto_add=True, quiet=False, exclude_filter=self.excl)
        except WatchManagerError, err:
            print "error: ", err, err.wmd
        
    def __startWatcher(self):
        if self.__daemonize:
            self.notifier = Notifier(self.__watchManager, self.__cp)
            self.notifier.loop(daemonize=self.__daemonize, pid_file='/tmp/pyinotify.pid', \
                  force_kill=True, stdout='/tmp/stdout.txt')
        else:        
            self.notifier = ThreadedNotifier(self.__watchManager, self.__cp)
            self.notifier.start()
        self.restartWatcher()    
        
    
class CustomProcess(ProcessEvent):
    def __init__(self, eventWatcher, fs, listener, configPath):
        pynotify.init("change watcher")
        self.eventWatcher = eventWatcher
        self.__fs = fs
        self.__listener = listener
        self.__dirRename = False
        self.configPath = configPath
    
    def process_DEFAULT(self, event):
        print "called with: ", event
        
    def process_IN_IGNORED(self, event):
        pass

    def __getTimeStamp(self):
        return datetime.datetime.now()  #format: 2009-06-01 13:23:20.618997
    
    def __getFilePath(self, event):
        return self.__fs.join(event.path, event.name)
    
    def __callingModEvent(self, event):
        self.__processEachEvents("mod", event, self.__getTimeStamp())
        
    def __callingDelEvent(self, event):
        #can't walk when directory is deleted'
        self.__processEachEvents("del", event, self.__getTimeStamp())

    def process_IN_CREATE(self, event):
        self.__callingModEvent(event)

    def process_IN_DELETE(self, event):
        self.__callingDelEvent(event)

    def process_IN_MOVED_FROM(self, event):
        self.__callingDelEvent(event)

    def process_IN_MOVED_TO(self, event):
        if self.__fs.isDirectory(self.__getFilePath(event)):
            self.__dirRename = True
        self.__callingModEvent(event)

    #not used yet
    def process_IN_ISDIR(self, event):
        self.__processEachEvents("Action on directory", event, self.__getTimeStamp())
    def process_IN_MODIFY(self, event):
        #print '---modify event---'
        self.__callingModEvent(event)
    def process_IN_ATTRIB(self, event): #NOTE: touch file from command line only invoke attribute event
        #print '---in attrib event---'
        self.__callingModEvent(event)
    def process_IN_CLOSE_WRITE(self, event): 
        self.__processEachEvents("in close write", event, self.__getTimeStamp())
    def process_IN_CLOSE_NOWRITE(self, event):
        self.__processEachEvents("in close no write", event, self.__getTimeStamp())

    def __processEachEvents(self, eventName, event, timeStamp):
        filePath = self.__fs.absolutePath(self.__getFilePath(event))
        ref = pynotify.Notification("%s: " % eventName, filePath)
        ref.show()
        print "In Process Event: %s, %s, %s" % (eventName, filePath, timeStamp)

        try:
            eventList = []
            if self.__getFilePath(event).rstrip("/") == self.configPath.rstrip("/"):
                #restart watcher
                self.eventWatcher.restartWatcher()
            elif self.__fs.isDirectory(self.__getFilePath(event)): #if it's a directory...
                if self.__dirRename:
                    self.__dirRename = False
                    #self.eventWatcher.restartWatcher()
                    #start to add the events to the files/dirs under this directory
                    def callback(path, dirs, files):
                        for dir in dirs:
                            rDir.append(path + dir)
                        for file in files:
                            rFiles.append(path + file)
                    
                    rFiles = []    
                    rDir = []
                    self.__fs.walker(filePath, callback)
                    for rdir in rDir:
                        eventList.append(("dir", filePath, 0))
                    for rfile in rFiles:
                        eventList.append((eventName, rfile, timeStamp))
                else:
                    eventList.append(("dir", filePath, 0))
                    self.__listener(eventList)
            else:
                eventList.append((eventName, filePath, timeStamp))
                self.__listener(eventList)
        except Exception, e:
            print str(e)
            print "fail to event to listener"
            pass