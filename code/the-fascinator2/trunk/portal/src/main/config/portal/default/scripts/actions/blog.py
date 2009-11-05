import os

from com.sun.syndication.feed.atom import Content
from com.sun.syndication.propono.atom.client import AtomClientFactory, BasicAuthStrategy

from java.io import ByteArrayInputStream, ByteArrayOutputStream, File, StringWriter
from java.net import Proxy, ProxySelector, URL, URLDecoder
from java.lang import Exception

from org.apache.commons.httpclient.methods import PostMethod
from org.apache.commons.io import IOUtils
from org.dom4j.io import OutputFormat, XMLWriter, SAXReader

class ProxyBasicAuthStrategy(BasicAuthStrategy):
    def __init__(self, username, password, baseUrl):
        BasicAuthStrategy.__init__(self, username, password)
        self.__baseUrl = baseUrl
    
    def addAuthentication(self, httpClient, method):
        BasicAuthStrategy.addAuthentication(self, httpClient, method)
        url = URL(self.__baseUrl)
        proxy = ProxySelector.getDefault().select(url.toURI()).get(0)
        if not proxy.type().equals(Proxy.Type.DIRECT):
            address = proxy.address()
            proxyHost = address.getHostName()
            proxyPort = address.getPort()
            httpClient.getHostConfiguration().setProxy(proxyHost, proxyPort)
            print "Using proxy %s:%s" % (proxyHost, proxyPort)

class AtomEntryPoster:
    def __init__(self):
        responseMsg = ""
        try:
            url = formData.get("url")
            title = formData.get("title")
            username = formData.get("username")
            password = formData.get("password")
            content = self.__getContent(formData.get("oid"))
            success, value = self.__post(username, password, title, content, url)
            if success:
                altLinks = value.getAlternateLinks()
                if altLinks is not None:
                    responseMsg = "<p>Success! Visit the <a href='%s' target='_blank'>blog post</a>.</p>" % altLinks[0].href
                else:
                    responseMsg = "<p class='warning'>The server did not return a valid link!</p>"
            else:
                responseMsg = "<p class='error'>%s</p>" % value
        except Exception, e:
            responseMsg = "<p class='error'>%s</p>"  % e.getMessage()
        writer = response.getPrintWriter("text/html")
        writer.println(responseMsg)
        writer.close()
    
    def __getContent(self, oid):
        slash = oid.rfind("/")
        pid = os.path.splitext(oid[slash+1:])[0] + ".htm"
        print " * detail.py: oid=%s,pid=%s" % (oid, pid)
        payload = Services.storage.getObject(oid).getPayload(pid)
        try:
            saxReader = SAXReader(False)
            document = saxReader.read(payload.getInputStream())
        except Exception, e:
            e.printStackTrace()
        slideNode = document.selectSingleNode("//*[local-name()='body']")
        slideNode.setName("div")
        out = ByteArrayOutputStream()
        format = OutputFormat.createPrettyPrint()
        format.setSuppressDeclaration(True)
        writer = XMLWriter(out, format)
        writer.write(slideNode)
        writer.close()
        return out.toString("UTF-8")
    
    def __post(self, username, password, title, content, url):
        auth = ProxyBasicAuthStrategy(username, password, url)
        try:
            service = AtomClientFactory.getAtomService(url, auth)
        except Exception, e:
            return False, "Failed to connect"
        workspace = None
        workspaces = service.getWorkspaces()
        if len(workspaces) > 0:
            workspace = workspaces[0]
        else:
            return False, "No valid workspace found"
        if workspace is not None:
            for c in workspace.getCollections():
                print c.href, c.accepts("entry")
            collection = workspace.findCollection(None, "application/atom+xml;type=entry")
            if collection is not None:
                entry = collection.createEntry()
                entry.setTitle(title)
                entry.setContent(content, Content.HTML)
                collection.addEntry(entry)
                return True, entry
            else:
                return False, "No valid collection found"
        return False, "An unknown error occured"

scriptObject = AtomEntryPoster()
