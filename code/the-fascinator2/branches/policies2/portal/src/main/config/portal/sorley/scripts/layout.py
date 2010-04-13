from org.apache.commons.lang import StringEscapeUtils

class LayoutData:

    def escapeText(self, text):
        return StringEscapeUtils.escapeXml(text)

    def getTemplate(self, templateName):
        portalName = portalId
        if not Services.pageService.resourceExists(portalId, templateName, False):
            portalName = Services.portalManager.DEFAULT_PORTAL_NAME
        return "%s/%s" % (portalName, templateName)

scriptObject = LayoutData()
