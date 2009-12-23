from au.edu.usq.fascinator.ims import ImsPackage
import traceback

class Ims:
    def __init__(self):
        print "Packaging view... "
        try:
            self.__exportIms()
        except:
            traceback.print_exc()
            
    def __exportIms(self):
        #imsPackage = ImsPackage()
        #Ims Package...
        imsPackage = ImsPackage("/home/octalina/Desktop/", self.__getPortal(), self.__getPortalManifest())
    
    def __getPortalManifest(self):
        return self.__getPortal().getJsonMap("manifest")
    
    def __getPortal(self):
        return Services.portalManager.get(portalId)
                
scriptObject = Ims()