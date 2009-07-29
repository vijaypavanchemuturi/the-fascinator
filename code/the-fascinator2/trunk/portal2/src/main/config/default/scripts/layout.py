# 
# Script for Template layout
# 
class TemplateData:
    
    def __init__(self):
        self.__checkLogin()

    def __checkLogin(self):
        action = formData.get("action")
        if (action == "logout"):
            sessionState.set("username", None)
        else:
            username = formData.get("username")
            if username is not None:
                #TODO actual login procedure
                sessionState.set("username", username)

    def getPortals(self):
        return Services.getPortalManager().getPortals()

scriptObject = TemplateData()
