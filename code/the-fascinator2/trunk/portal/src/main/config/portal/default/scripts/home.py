
class HomeData:
    def __init__(self):
        action = formData.get("verb")
        portalName = formData.get("value")
        if action == "delete-portal":
            print " * home.py: delete portal %s" % portalName
            Services.portalManager.remove(portalName)
        if action == "backup-portal":
            print "Backing up: ", portalName
            backupPath = ""
            email = ""
            portal = Services.portalManager.get(portalName)
            if portal:
                email = portal.email
                if portal.backupPaths:
                    for key in portal.backupPaths:
                        if key=="default":
                            backupPath = portal.backupPaths[key]
                portalQuery = portal.getQuery()
                #print " ***** portalQuery: ", portalQuery
                #print " ***** backupPath: ", backupPath
                #print " ***** email: ", email
                #print " ***** description: ", description
            Services.portalManager.backup(email, backupPath, portalQuery)
    
scriptObject = HomeData()
