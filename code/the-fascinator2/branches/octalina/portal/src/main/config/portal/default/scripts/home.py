
class HomeData:
    def __init__(self):
        action = formData.get("verb")
        portalName = formData.get("value")
        if action == "delete-portal":
            print " * home.py: delete portal %s" % portalName
            Services.portalManager.remove(portalName)
        if action == "backup-portal":
            print "Backing up: ", portalName
            backupPathList = []
            email = ""
            portal = Services.portalManager.get(portalName)
            if portal:
                email = portal.email
                if portal.backupPaths:
                    for key in portal.backupPaths:
                        if portal.backupPaths[key]=="on":
                            backupPathList.append(key)
                portalQuery = portal.getQuery()
                #print " ***** portalQuery: ", portalQuery
                #print " ***** backupPath: ", backupPath
                #print " ***** email: ", email
                #print " ***** description: ", description
            if backupPathList == []:
                " ** Default backup path configured in system-config.json will be used "
            Services.portalManager.backup(email, backupPathList, portalQuery)
    
scriptObject = HomeData()
