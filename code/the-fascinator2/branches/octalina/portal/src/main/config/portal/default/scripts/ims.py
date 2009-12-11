
import traceback

class Ims:
    def __init__(self):
        print "Packaging view... "
        try:
            self.__exportIms()
        except:
            traceback.print_exc()
            
    def __exportIms(self):
        
        #Ims Package...
        
        pass