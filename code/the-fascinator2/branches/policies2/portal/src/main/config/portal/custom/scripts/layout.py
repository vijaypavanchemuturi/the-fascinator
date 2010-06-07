import md5

from java.net import URLEncoder
from org.apache.commons.lang import StringEscapeUtils

class LayoutData:
    
    def __init__(self):
        pass
    
    def getPortals(self):
        return Services.portalManager.portals
    
    def getPortalName(self):
        return Services.portalManager.get(portalId).description
    
    def escapeText(self, text):
        return StringEscapeUtils.escapeXml(text)
    
    def urlencode(self, url):
        print " ***** %s (%s)" % (url, URLEncoder.encode(url, "UTF-8"))
        return URLEncoder.encode(url, "UTF-8")
    
    def md5Hash(self, data):
        return md5.new(data).hexdigest()
    
    def getTemplate(self, templateName):
        portalName = portalId
        if not Services.pageService.resourceExists(portalId, templateName, False):
            portalName = Services.portalManager.DEFAULT_PORTAL_NAME
        return "%s/%s" % (portalName, templateName)

scriptObject = LayoutData()
