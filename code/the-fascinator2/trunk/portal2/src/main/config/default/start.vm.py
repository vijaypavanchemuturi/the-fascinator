
class StartPage:
    
    def __init__(self):
        self.__portalManager = Services.getPortalManager()
    
    def getPortals(self):
        return self.__portalManager.getPortals()
    
page = StartPage()
