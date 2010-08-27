
class NavigationData:
    def __activate__(self, context):
        self.page = context["page"]
        self.metadata = context["metadata"]
    
    def canOrganise(self):
        userRoles = self.page.authentication.get_roles_list()
        workflowSecurity = self.metadata.getList("workflow_security")
        for userRole in userRoles:
            if userRole in workflowSecurity:
                return True
        return False
