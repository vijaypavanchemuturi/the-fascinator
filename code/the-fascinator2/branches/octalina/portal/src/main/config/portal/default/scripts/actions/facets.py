from au.edu.usq.fascinator.portal import Portal

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
        writer = response.getPrintWriter("text/plain")
        writer.println(result)
        writer.close()

scriptObject = FacetActions()
