from au.edu.usq.fascinator.api import PluginManager

class HomeData:
    def __init__(self):
        print " * home.py: formData=%s" % formData
        action = formData.get("verb")
        if action == "delete-portal":
            portalName = formData.get("value")
            print " * home.py: delete portal %s" % portalName
            Services.portalManager.remove(portalName)
    
scriptObject = HomeData()
