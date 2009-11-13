from au.edu.usq.fascinator.common import JsonConfigHelper
from au.edu.usq.fascinator.portal import Portal
from java.util import HashMap

class FacetActions:
    def __init__(self):
        print " * facets.py: formData=%s" % formData
        result = "{}"
        portalManager = Services.getPortalManager()
        portal = portalManager.get(portalId)
        func = formData.get("func")
        if func == "view-update":
            portal.setDescription(formData.get("view-description"))
            portal.setQuery(formData.get("view-query"))
            portal.setRecordsPerPage(int(formData.get("view-records-per-page")))
            portal.setFacetCount(int(formData.get("view-facet-count")))
            portal.setFacetSort(formData.get("view-facet-sort") is not None)
            portalManager.save(portal)
        elif func == "facets-update":
            portal.removePath("portal/facet-fields")
            fields = formData.getValues("field")
            labels = formData.getValues("label")
            displays = formData.getValues("display")
            deletes = formData.getValues("delete")
            for i in range(0, len(fields)):
                field = fields[i]
                if deletes[i] == "false":
                    portal.set("portal/facet-fields/%s/label" % field, labels[i])
                    portal.set("portal/facet-fields/%s/display" % field, displays[i])
            portalManager.save(portal)
        elif func == "backup-update":
            portal.set("portal/backup/email", formData.get("email"))
            pathIds = formData.get("pathIds").split(",")
            actives = formData.getValues("backup-active")
            renditions = formData.getValues("backup-rendition")
            views = formData.getValues("backup-view")
            paths = HashMap()
            for pathId in pathIds:
                path = formData.get("%s-path" % pathId)
                active = str(pathId in actives).lower()
                rendition = str(pathId in renditions).lower()
                view = str(pathId in views).lower()
                ignoreFilter = formData.get("%s-ignore" % pathId)
                json = JsonConfigHelper()
                json.set("active", active)
                json.set("include-rendition-meta", rendition)
                json.set("include-portal-view", view)
                json.set("ignoreFilter", ignoreFilter)
                paths.put(path, json.getMap("/"))
            print " *** paths=%s" % paths
            portal.setMap("portal/backup/paths", paths)
            portalManager.save(portal)
        writer = response.getPrintWriter("text/plain")
        writer.println(result)
        writer.close()

scriptObject = FacetActions()
