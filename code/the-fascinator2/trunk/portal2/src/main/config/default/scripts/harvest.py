from au.edu.usq.fascinator.api import PluginManager

class HarvestData:
    
    def __init__(self):
        for key in formData.keySet():
            print key, "=", self.getFormData(key)
    
    def getHarvesters(self):
        return PluginManager.getHarvesterPlugins()
    
    def getFormData(self, key):
        values = formData.get(key)
        if values is not None and len(values) == 1:
            return values[0]
        else:
            return values
    
scriptObject = HarvestData()

if request.method == "POST":
    response.sendRedirect("harvest")
