from au.edu.usq.fascinator.common import JsonConfig
from au.edu.usq.fascinator.common import JsonConfigHelper

class QueuesPage:
    def __init__(self):
        pass

    def __activate__(self, context):
        self.velocityContext = context

        self.config = JsonConfigHelper(JsonConfig.getSystemFile())
        self.threads = self.config.getJsonList("messaging/threads")

    def getDescription(self, queue):
        for thread in self.threads:
            name = thread.get("config/name")
            if name == queue:
                return thread.get("description")
