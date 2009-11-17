
class Reindex:
    def __init__(self):
        if formData.get("func") == "reindex":
            file = formData.get("file")
            print " * Reindexing: formData=%s" % file
            portalManager = Services.getPortalManager()
            
            portalManager.indexObject(file)
        
scriptObject = Reindex()
