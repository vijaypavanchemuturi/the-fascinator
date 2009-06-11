import traceback, sys
from threading import Thread
from time import sleep

class Queue(Thread):
    def __init__(self, db):
        Thread.__init__(self)
        
        self.db = db
        
        
        
    def put(self, *args, **kwargs):
        #self.queue.put(*args, **kwargs) #kwargs not working yet
        self.db.processEvent(*args)
        #self.db.insertFile(*args)
        
        
        

#class QueueHandler(EventHandler):
#    def process_event(self, event):
#        print "event: ", event
#        return 1
    
    
#class ExamineEventHandler(EventHandler):
#    def process_event(self, event):
#        print "------------------"
#        print "Creation Time:    ", time.ctime(event.time)
#        print "Originator's Name:", event.originator
#        print "Original Line:    ", event.data.line,
#        print "Match Object:     ", event.data.match.groups()
#        return 1

        