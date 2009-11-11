from au.edu.usq.fascinator.portal import Portal

class FacetActions:
    def __init__(self):
        print " * facets.py: formData=%s" % formData
        result = "{}"
        portalManager = Services.getPortalManager()
        portal = portalManager.get(portalId)
        func = formData.get("func")
        if func == "facets-update":
            portal.removePath("portal/facet-fields")
            fields = formData.getValues("field")
            labels = formData.getValues("label")
            displays = formData.getValues("display")
            deletes = formData.getValues("delete")
            for i in range(0, len(fields)):
                if deletes[i] == "false":
                    portal.set("portal/facet-fields/%s/label" % fields[i], labels[i])
                    portal.set("portal/facet-fields/%s/display" % fields[i], displays[i])
            portalManager.save(portal)
        writer = response.getPrintWriter("text/plain")
        writer.println(result)
        writer.close()

scriptObject = FacetActions()
