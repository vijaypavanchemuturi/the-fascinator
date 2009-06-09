import pynotify, datetime, sys
from pyinotify import EventsCodes, ProcessEvent, WatchManager, WatchManagerError, Notifier, ThreadedNotifier, ExcludeFilter
from threading import Thread

class WriterClass(object):
    def __init__(self):
        self.__data = ""
        self.__stdout = sys.stdout
        sys.stdout = self

    def write(self, data):
        self.__data += data
        self.__stdout.write(data)
        
    def read(self):
        return self.__data.split("\n")        

    def close(self):
        sys.stdout = self.__stdout


class EventWatcherClass(object):
    FLAGS = EventsCodes.ALL_FLAGS
    mask  = FLAGS['IN_DELETE'] | FLAGS['IN_CREATE'] | \
            FLAGS['IN_MOVED_FROM'] | FLAGS['IN_MODIFY'] | \
            FLAGS['IN_MOVED_TO'] | FLAGS['IN_ATTRIB'] | FLAGS['IN_IGNORED']
     
    def __init__(self, dirToBeWatched=[], queue=None, fs=None, daemonize=False, forcekill=False, exclFileName="", exclFile=None):
        self.__dirToBeWatched = dirToBeWatched
        self.__fs = fs
        self.__queue = queue
        
        #Main variable for notifier and watcher
        self.__daemonize = daemonize
        self.__forcekill = forcekill
        self.notifier = None
        self.__cp = CustomProcess(self.__fs, self.__listener)
        self.__watchManager = WatchManager()
        self.__exclFileName = exclFileName
        self.__exclFile = exclFile
        
        #stout reader
        self.writerClass = WriterClass()
        
        self.__startWatcher()

#        notifier.loop(daemonize=True, pid_file='/tmp/pyinotify.pid', \
#              force_kill=True, stdout='/tmp/stdout.txt')
        
        self.__listeners = []

    def addListener(self, listener):
        self.__listeners.append(listener)

    def removeListener(self, listener):
        if self.__listener.count(listener)>0:
            self.__listener.remove(listener)
            return True
        return False

    def __listener(self, *args, **kwargs):
        for listner in self.__listeners:
            try:
                listner(*args, **kwargs)
            except Exception, e:
                pass
        
    def __startWatcher(self):
        if self.__daemonize:
            self.notifier = Notifier(self.__watchManager, self.__cp)
            self.notifier.loop()
            #self.notifier.loop(daemonize=self.__daemonize)
        else:
            self.notifier = ThreadedNotifier(self.__watchManager, self.__cp)
            #to seat deamon: self.notifier.setDaemon
            self.notifier.start()
            
            
        #auto_add will help to automatically add the watcher to "just-created" child folder
        try:
            excl = None
            if self.__exclFile and self.__exclFileName:
                excl = ExcludeFilter({self.__exclFileName: self.__exclFile})
            self.__watchManager.add_watch(self.__dirToBeWatched, self.mask, rec=True, 
                                          auto_add=True, quiet=False, exclude_filter=excl)
            
        except WatchManagerError, err:
            print "error: ", err, err.wmd    
        
    #DO cleanup here
    #Get last 'mod' event
        #If there 'del' event on the same file after 'mod' event, use the 'del' event
    def eventList(self):
        eventList = []
        self.writerClass.close()
        allEvent = self.writerClass.read()
        for eachEvent in allEvent:
            if eachEvent != '':
                eventList = self.__processEvent(eventList, eachEvent)
        return eventList
             
    def __processEvent(self, eventList, event):
        eventName, filePath, time = event.split(",")       
        found = False
        for eachEvent in eventList:
            eventName0, filePath0, time0 = eachEvent
            if filePath.strip() == filePath0.strip():
                found = True
                if eventName.strip() == 'mod' or (eventName.strip() == 'del' and eventName0.strip()=='mod'):
                    eventList.remove(eachEvent)
                    eventList.append((eventName.strip(), filePath.strip(), time.strip()))
        if not found:
            eventList.append((eventName.strip(), filePath.strip(), time.strip()))
        return eventList

        
    
class CustomProcess(ProcessEvent):
    def __init__(self, fs, listener):
        pynotify.init("change watcher")
        self.__fs = fs
        self.__listener = listener

    def addListener(self, listener):
        self.__listeners.append(listener)

    def removeListener(self, listener):
        if self.__listener.count(listener)>0:
            self.__listener.remove(listener)
            return True
        return False
    
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
        self.__processEachEvents("del", event, self.__getTimeStamp())

    def process_IN_CREATE(self, event):
        #print '---create event---'
        self.__callingModEvent(event)

    def process_IN_DELETE(self, event):
        self.__callingDelEvent(event)

    def process_IN_MOVED_FROM(self, event):
        self.__callingDelEvent(event)

    def process_IN_MOVED_TO(self, event):
        #print '---moved to event---'
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
        #ref.show()
        print "%s, %s, %s" % (eventName, filePath, timeStamp)

        try:
            self.__listener(eventName, filePath, timeStamp)
        except Exception, e:
            pass
        #self.__queue.processingQueue((eventName, filePath, timeStamp))
        #self.__queue.put((eventName, filePath, timeStamp))
        #need to use queue to push to database... ;)



   

#class EventWatcherClass(object):
#    #    EventsCodes.IN_ATTRIB    #when the file is utime/touch
#    #    EventsCodes.IN_ISDIR
#    #    EventsCodes.IN_CLOSE_WRITE 
#    #    EventsCodes.IN_CLOSE_NOWRITE
#    mask = EventsCodes.IN_DELETE | EventsCodes.IN_CREATE | \
#           EventsCodes.IN_MODIFY | EventsCodes.IN_MOVED_FROM | \
#           EventsCodes.IN_MOVED_TO | EventsCodes.IN_ATTRIB
#
#    def __init__(self, dirFileToBeWatched, fs):
#        self.__dirFileToBeWatched = dirFileToBeWatched
#        self.__wm = WatchManager()
#        self.__wm.add_watch(self.__dirFileToBeWatched, self.mask, rec=True, auto_add=True)
#        self.notifier = None
#        self.__fs = fs
#        
#        self.__data = ""
#        self.__stdout = sys.stdout
#        sys.stdout = self
#        
#        self.__startNotifier()
#        
#    def write(self, data):
#        self.__data += data
#        self.__stdout.write(data)
#        
#    def read(self):
#        return self.__data.split("\n")        
#    
#    def close(self):
#        sys.stdout = self.__stdout
#
#    def __startNotifier(self):    
#        # notifier instance and init, process all events with
#        # an instance of PExample
#        #print 'start monitoring %s with mask 0x%08x' % (self.__dirFileToBeWatched, self.mask)
#        self.cp = CustomProcess(self.__fs)
#        self.notifier = ThreadedNotifier(self.__wm, self.cp)
#        self.notifier.start()
    
#    def __eventsHappened(self):
#        # read and process events
#        self.notifier.process_events()
#        while True:
#            try:
#                if self.notifier.check_events():
#                    self.notifier.read_events()
#            except KeyboardInterrupt:
#                # ...until c^c signal
#                print 'stop monitoring...'
#                # stop monitoring
#                self.notifier.stop()
#                break
#            except Exception, err:
#                # otherwise keep on watching
#                print err
#                self.notifier.stop() 