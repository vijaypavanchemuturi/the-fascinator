from java.io import ByteArrayOutputStream
from org.apache.commons.io import IOUtils

class ResultData:
    def __init__(self):
        pass
    
    def __activate__(self, context):
        self.services = context["Services"]
        self.page = context["page"]
        self.metadata = context["metadata"]
        self.portalId = context["portalId"]
    
    def canManage(self):
        wfSecurity = self.metadata.get("workflow_security")
        if wfSecurity:
            user_roles = self.page.authentication.get_roles_list()
            for role in user_roles:
                if role in wfSecurity:
                    return True
        return False

    def get(self, name):
        valueList = self.metadata.getList(name)
        if valueList.size() > 0:
            return valueList.get(0)
        return ""

    def getMimeTypeIcon(self, format):
        # check for specific icon
        iconPath = "images/icons/mimetype/%s/icon.png" % format
        resource = self.services.getPageService().resourceExists(self.portalId, iconPath)
        if resource is not None:
            return iconPath
        elif format.find("/") != -1:
            # check for major type
            return self.getMimeTypeIcon(format[:format.find("/")])
        # use default icon
        return "images/icons/mimetype/icon.png"

    def getSourceSample(self, id, limit):
        # Get source payload
        object = self.services.getStorage().getObject(id)
        if object is not None:
            payload = object.getPayload(object.getSourceId())

        # Read to a string
        if payload is not None:
            out = ByteArrayOutputStream()
            IOUtils.copy(payload.open(), out)
            payload.close()
            string = out.toString("UTF-8")

        # Return response
        if string is not None:
            if (len(string)) > limit:
                return  string[0:limit] + "..."
            else:
                return  string
        else:
            return ""
