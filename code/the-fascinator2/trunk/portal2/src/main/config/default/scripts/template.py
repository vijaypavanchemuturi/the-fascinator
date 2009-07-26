# 
# Script for Template layout
# 
# 
# 
class TemplateData:
    
    def __init__(self):
        self.__portalManager = Services.getPortalManager()
    
    def getPortals(self):
        portals = self.__portalManager.getPortals()
        return portals
    
scriptObject = TemplateData()
