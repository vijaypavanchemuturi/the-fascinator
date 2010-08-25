import md5
from authentication import AuthenticationData
from org.apache.commons.lang import StringEscapeUtils

class LayoutPage:
    def __init__(self):
        self.authentication = AuthenticationData()
    
    def __activate__(self, context):
        self.services = context["Services"]
        self.security = context["security"]
        self.portalId = context["portalId"]
        self.authentication.__activate__(context)
    
    def getPortal(self):
        return self.services.getPortalManager().get(self.portalId)
    
    def getPortals(self):
        return self.services.getPortalManager().portals
    
    def getPortalName(self):
        return self.getPortal().getDescription()
    
    def escapeXml(self, text):
        return StringEscapeUtils.escapeXml(text)
    
    def escapeHtml(self, text):
        return StringEscapeUtils.escapeHtml(text)
    
    def unescapeHtml(self, text):
        return StringEscapeUtils.unescapeHtml(text)
    
    def md5Hash(self, data):
        return md5.new(data).hexdigest()
    
    def capitalise(self, text):
        return text[0].upper() + text[1:]
    
    def getTemplate(self, templateName):
        return self.services.pageService.resourceExists(self.portalId, templateName)
    
    def getQueueStats(self):
        return self.services.getHouseKeepingManager().getQueueStats()
    
    def getSsoProviders(self):
        return self.security.ssoBuildLogonInterface()

