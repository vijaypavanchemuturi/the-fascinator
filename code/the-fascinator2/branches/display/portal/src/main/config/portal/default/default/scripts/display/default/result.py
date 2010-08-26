
class ResultPage:
    def __activate__(self, context):
        self.page = context["page"]
        self.portalId = context["portalId"]
        self.metadata = context["metadata"]
    
    def canManage(self):
        workflowRoles = self.metadata.get("workflow_security")
        if workflowRoles:
            userRoles = self.page.authentication.get_roles_list()
            for role in userRoles:
                if role in workflowRoles:
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
        resource = Services.getPageService().resourceExists(self.portalId, iconPath)
        if resource is not None:
            return iconPath
        elif format.find("/") != -1:
            # check for major type
            return self.getMimeTypeIcon(format.split("/")[0])
        # use default icon
        return "images/icons/mimetype/icon.png"
