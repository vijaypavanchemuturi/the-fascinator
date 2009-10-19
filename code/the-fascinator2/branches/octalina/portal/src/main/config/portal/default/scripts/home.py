
class HomeData:
    def __init__(self):
        action = formData.get("verb")
        portalName = formData.get("value")
        if action == "delete-portal":
            print " * home.py: delete portal %s" % portalName
            Services.portalManager.remove(portalName)
        if action == "backup-portal":
            print "&&&&&&&& calling backup: ", portalId
            portal = Services.portalManager.get(portalId)
            backupPath = ""
            for key in portal.backupPaths:
                if key=="default":
                    backupPath = portal.backupPaths[key]
            description = ""
            if portal.getDescription() != "Everything":
                description = portal.getDescription()
            Services.portalManager.backup(description, portal.email, backupPath)
    
scriptObject = HomeData()
