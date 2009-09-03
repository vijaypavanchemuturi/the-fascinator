from au.edu.usq.fascinator.api import PluginManager

class SettingsData:

    def __init__(self):
        self.__portal = Services.portalManager.get(portalId)
        if formData.get("portalAction") == "Update":
            self.__updatePortal()
    
    def __updatePortal(self):
        print " * settings.py: updatePortal %s" % formData
        self.__portal.name = formData.get("portalName")
        self.__portal.description = formData.get("portalDescription")
        self.__portal.query = formData.get("portalQuery")
        self.__portal.recordsPerPage = int(formData.get("portalRecordsPerPage"))
        self.__portal.facetCount = int(formData.get("portalFacetLimit"))
        self.__portal.facetSort = formData.get("portalFacetSort") is not None
    
    def getPortal(self):
        return self.__portal
    
    def getIndexerPlugins(self):
        return PluginManager.getIndexerPlugins()

    def getStoragePlugins(self):
        return PluginManager.getStoragePlugins()

    def getHarvesterPlugins(self):
        return PluginManager.getHarvesterPlugins()

    def getTransformerPlugins(self):
        return PluginManager.getTransformerPlugins()

scriptObject = SettingsData()
