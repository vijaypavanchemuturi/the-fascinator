# 
# Script for Template layout
# 
# 
# 
class TemplateData:
    
    def __init__(self):
        self.__portalManager = Services.getPortalManager()
    
    def getPortals(self):
        return self.__portalManager.getPortals()
    
scriptObject = TemplateData()
