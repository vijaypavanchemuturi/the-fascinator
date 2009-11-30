from au.edu.usq.fascinator import HarvestClient
from java.io import File

class UpdateData:
    def __init__(self):
        if formData.get("forced") == "true":
            filename = "usq-policies-forced.json"
        else:
            filename = "usq-policies.json"
        jsonFile = File("/opt/the-fascinator/rules/%s" % filename)
        client = HarvestClient(jsonFile)
        client.run()
    
    def getLog(self):
        log = open("/opt/the-fascinator/work/harvest.log")
        return log.read()

scriptObject = UpdateData()
