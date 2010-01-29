from ch.qos.logback.classic.html import  HTMLLayout
from org.slf4j import Logger, LoggerFactory

class Reharvest:
    def __init__(self):
        func = formData.get("func")
        result = "{}"
        resultType = "text/plain"
        if func == "reharvest":
            file = formData.get("file")
            portalId = formData.get("portalId")
            portalManager = Services.getPortalManager()
            if file:
                print " * Re-harvesting: formData=%s" % file
                portalManager.reHarvestObject(file)
                sessionState.set("reharvest/lastResult", "success")
                result = '{ status: "ok" }'
            elif portalId:
                portal = portalManager.get(portalId)
                print " * Re-harvesting: Portal=%s" % portal.name
                portalManager.reHarvestPortal(portal)
                sessionState.set("reharvest/lastResult", "success")
                result = '{ status: "ok" }'
            else:
                sessionState.set("reharvest/lastResult", "failed")
                result = '{ status: "failed" }'
        elif func == "get-state":
            result = '{ running: "%s", lastResult: "%s" }' % \
                (sessionState.get("reharvest/running"),
                 sessionState.get("reharvest/lastResult"))
        elif func == "get-log":
            context = LoggerFactory.getILoggerFactory()
            logger = context.getLogger("au.edu.usq.fascinator.HarvestClient")
            it = logger.iteratorForAppenders()
            appender = logger.getAppender("reharvest")
            layout = HTMLLayout()
            layout.setContext(context)
            layout.setPattern("%d%level%msg")
            layout.setTitle("Reharvest log")
            layout.start()
            result = "<table>"
            count = appender.getLength()
            if count == -1:
                result += "<tr><td>Failed</td></tr>"
            elif count == 0:
                result += "<tr><td>No logging events</td></tr>"
            else:
                for i in range(0, count):
                    event = appender.get(i)
                    result += layout.doLayout(event)
            result += "</table>"
            resultType = "text/html"
        writer = response.getPrintWriter(resultType)
        writer.println(result)
        writer.close()
                
scriptObject = Reharvest()
