import md5
from org.apache.commons.lang import StringEscapeUtils
# 
# Script for Template layout
# 

from java.net import URLEncoder

class TemplateData:
    
    def __init__(self):
        self.__checkLogin()
        if formData.get("verb") == "clear-session":
            sessionState.clear()
    
    def __checkLogin(self):
        action = formData.get("verb")
        if (action == "logout"):
            sessionState.set("username", None)
        else:
            username = formData.get("username")
            if username is not None:
                #TODO actual login procedure
                sessionState.set("username", username)
    
    def getPortals(self):
        return Services.portalManager.portals
    
    def getPortalName(self):
        return Services.portalManager.get(portalId).description
    
    def encodeURL(self, url):
        #return URLEncoder.encode(url, "UTF-8")
        return url
    
    def escapeText(self, text):
        return StringEscapeUtils.escapeXml(text)
    
    def md5Hash(self, data):
        return md5.new(data).hexdigest()
    
    def getTemplate(self, templateName):
        portalName = portalId
        if not Services.pageService.resourceExists(portalId, templateName, False):
            portalName = Services.portalManager.DEFAULT_PORTAL_NAME
        return "%s/%s" % (portalName, templateName)
    
    def resolve(self, uri):
        print " * layout.py: resolve uri=%s" % uri
        return uri

scriptObject = TemplateData()
