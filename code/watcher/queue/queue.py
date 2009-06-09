import traceback, sys
from threading import Thread
from time import sleep
from Queue import Queue

q = Queue()

#receive the event... put it in database
class Queue(Thread):
    def __init__(self, handler, db=None):
        self.__db = db
        self.__listener = []
        
        self.handler = handler
        self.queue = q
        self.running = 1
        
        #Thread.__init__(self)
        
    def run(self):
        print 'in server run'
        while self.running:
            try:
                pass
#                item = self.queue.get()
#                if item == 0:
#                    break
#                else:
#                    #... get from the watcher and call self.processingQueue to process the queue
#                    self.processingQueue(item)
            except:
                traceback.print_exc()
            
    def stop(self):
        self.queue.put(0)
        self.join()
    
    def put(self, *args, **kwargs):
        print 'putting item: ', item
        self.queue.put(item)
        
    def processingQueue(self, event):
        print 'in processingQueue: ', event
        
    def addListenter(self, listener):
        #create new thread here
        print 'start listender'
        self.__listener.append(listener)
        
if __name__=='__main__':
    def printer(self):
        print 'printer(%r)' % item
        
    q = Queue(printer)
    
    q.start()
    for i in range(10):
        q.put(i)
    q.stop()

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

        