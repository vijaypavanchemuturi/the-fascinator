from java.io import ByteArrayOutputStream
from org.apache.commons.io import IOUtils

class ObjectResultData:
    def __init__(self):
        pass

    def canManage(self, wfSecurity):
        user_roles = page.authentication.get_roles_list()
        for role in user_roles:
            if role in wfSecurity:
                return True
        return False

    def get(self, name):
        valueList = metadata.getList(name)
        if valueList.size() > 0:
            return valueList.get(0)
        return ""

    def getMimeTypeIcon(self, format):
        # check for specific icon
        iconPath = "images/icons/mimetype/%s/icon.png" % format
        resource = Services.getPageService().resourceExists(portalId, iconPath)
        if resource is not None:
            return iconPath
        elif format.find("/") != -1:
            # check for major type
            return self.getMimeTypeIcon(format[:format.find("/")])
        # use default icon
        return "images/icons/mimetype/icon.png"

    def getSourceSample(self, id, limit):
        # Get source payload
        object = Services.getStorage().getObject(id)
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

if __name__ == "__main__":
    scriptObject = ObjectResultData()
