import htmlentitydefs

from au.edu.usq.fascinator.api.storage import PayloadType, StorageException
from au.edu.usq.fascinator.api.indexer import SearchRequest
from au.edu.usq.fascinator.common import FascinatorHome, Manifest, JsonSimple, JsonSimpleConfig
from au.edu.usq.fascinator.common.solr import SolrResult

from com.sun.syndication.feed.atom import Content
from com.sun.syndication.propono.atom.client import AtomClientFactory, BasicAuthStrategy

from java.io import ByteArrayInputStream, ByteArrayOutputStream
from java.net import Proxy, ProxySelector, URL, URLDecoder
from java.lang import Exception
from java.util import TreeMap

from org.apache.commons.io import FileUtils, IOUtils
from org.w3c.tidy import Tidy

from org.apache.velocity import VelocityContext


class ProxyBasicAuthStrategy(BasicAuthStrategy):
    def __init__(self, username, password, baseUrl):
        BasicAuthStrategy.__init__(self, username, password)
        self.__baseUrl = baseUrl

    def addAuthentication(self, httpClient, method):
        BasicAuthStrategy.addAuthentication(self, httpClient, method)
        url = URL(self.__baseUrl)
        proxy = ProxySelector.getDefault().select(url.toURI()).get(0)
        httpClient.getParams().setAuthenticationPreemptive(False);
        if not proxy.type().equals(Proxy.Type.DIRECT):
            address = proxy.address()
            proxyHost = address.getHostName()
            proxyPort = address.getPort()
            httpClient.getHostConfiguration().setProxy(proxyHost, proxyPort)
            #self.vc("log").debug("Using proxy '{}:{}'", proxyHost, proxyPort)

class BlogData:
    def __init__(self):
        pass

    def __activate__(self, context):
        self.velocityContext = context
        self.page = context["page"]
        self.services = context["Services"]
        self.log = context["log"]

        self.buildTOC = False
        self.separator = None

        responseType = "text/html; charset=UTF-8"
        responseMsg = ""

        func = self.vc("formData").get("func")

        ## AJAX query for URL history
        if func == "url-history":
            responseType = "text/plain; charset=UTF-8"
            responseMsg = "\n".join(self.getUrls())

        ## The proper form submission
        else:
            try:
                ## Get all the form data
                oid = self.vc("formData").get("oid")
                url = self.vc("formData").get("url")
                title = self.vc("formData").get("title")
                username = self.vc("formData").get("username")
                password = self.vc("formData").get("password")
                self.separator = self.vc("formData").get("separator")
                if self.separator is None:
                    self.separator = ""
                toc = self.vc("formData").get("toc")
                if toc is not None and toc == "true":
                    self.buildTOC = True
                preamble = self.vc("formData").get("preamble")

                ## Setup our Atom client with proxied and authenticated HTTP
                auth = ProxyBasicAuthStrategy(username, password, url)
                self.__service = AtomClientFactory.getAtomService(url, auth)

                try:
                    ## Get our object from storage and the index
                    self.__object = self.__getObject(oid)
                    self.__readMetadata(oid)
                    sourceId = self.__object.getSourceId()
                    sourcePayload = self.__object.getPayload(sourceId)

                    ## Packages are special
                    if sourcePayload and sourcePayload.getContentType() == "application/x-fascinator-package":
                        manifest = Manifest(sourcePayload.open())
                        content = self.__getManifestContent(manifest)
                        toc = ""
                        if self.buildTOC:
                            toc = self.__buildTableOfContents(manifest)
                        if preamble != "":
                            preamble = "<div class='tf-preamble'>\n%s\n</div>" % preamble
                            content = toc + preamble + content
                        else:
                            content = toc + content
                        sourcePayload.close()

                    ## Normal objects
                    else:
                        content = self.services.pageService.renderObject(self.__convertToVelocityContext(), "detail", self.__metadata)
                        content = "<div class='tf-content-block'>\n%s\n</div>" % self.__escapeUnicode(content)
                        if preamble != "":
                            content = "<div class='tf-content-holder'>\n%s\n</div>" % content
                            preamble = "<div class='tf-preamble'>\n%s\n</div>" % preamble
                            content = preamble + content

                    # Tidy our HTML and fix relative links before sending
                    content, doc = self.__tidy(content)
                    content = self.__processMedia(oid, doc, content)
                    # Now POST to server
                    success, value = self.__post(title, content)

                except Exception, e:
                    self.vc("log").error("Error retrieving object: ", e)
                    success = False
                    value = e.getMessage()

                # Build a response for the front end
                if success:
                    altLinks = value.getAlternateLinks()
                    if altLinks is not None:
                        self.saveUrl(url)
                        responseMsg = "<p>Success! Visit the <a href='%s' target='_blank'>blog post</a>.</p>" % altLinks[0].href
                    else:
                        responseMsg = "<p class='warning'>The server did not return a valid link!</p>"
                else:
                    responseMsg = "<p class='error'>%s</p>" % value

            ## Failed to connect to AtomPub server?
            except Exception, e:
                self.vc("log").error("Failed to post: ", e)
                responseMsg = "<p class='error'>%s</p>"  % e.getMessage()

        # Send the response back
        writer = self.vc("response").getPrintWriter(responseType)
        writer.println(responseMsg)
        writer.close()

    # Get from velocity context
    def vc(self, index):
        if self.velocityContext[index] is not None:
            return self.velocityContext[index]
        else:
            self.log.error("ERROR: Requested context entry '" + index + "' doesn't exist")
            return None

    def __convertToVelocityContext(self):
        vc = VelocityContext()
        for key in self.velocityContext.keySet():
            vc.put(key, bindings.get(key));
        vc.put("velocityContext", vc);
        return vc


    def __loadSolrData(self, oid):
        portal = self.page.getPortal()
        query = 'id:"%s"' % oid
        if portal.getSearchQuery():
            query += " AND " + portal.getSearchQuery()
        req = SearchRequest(query)
        req.addParam("fq", 'item_type:"object"')
        req.addParam("fq", portal.getQuery())
        out = ByteArrayOutputStream()
        self.services.getIndexer().search(req, out)
        self.__solrResult = SolrResult(ByteArrayInputStream(out.toByteArray()))

    def __readMetadata(self, oid):
        self.__loadSolrData(oid)
        if self.__solrResult.getNumFound() == 1:
            self.__metadata = self.__solrResult.getResults().get(0)
            if self.__object is None:
                # Try again, indexed records might have a special storage_id
                self.__object = self.__getObject(oid)
        else:
            self.__metadata = SolrResult('{"id":"%s"}' % oid)

    def getUrls(self):
        return FileUtils.readLines(self.__getHistoryFile())

    def saveUrl(self, url):
        historyFile = self.__getHistoryFile()
        if historyFile.exists():
            urls = FileUtils.readLines(historyFile)
            if not url in urls:
                urls.add(url)
        FileUtils.writeLines(historyFile, urls)

    def __getHistoryFile(self):
        f = FascinatorHome.getPathFile("blog/history.txt")
        if not f.exists():
            f.getParentFile().mkdirs()
            f.createNewFile()
        return f

    def __buildTableOfContents(self, manifest):
        entries = []
        for node in manifest.getTopNodes():
            if not node.getHidden():
                title = node.getTitle()
                key = node.getKey()
                entries.append("<li><a href='#%s'>%s</a></li>" % (key, title))
        return "<div id='tf-toc'>\n<ul>\n%s\n</ul>\n</div>" % "\n".join(entries)

    def __getManifestContent(self, manifest):
        entries = []
        for node in manifest.getTopNodes():
            if not node.getHidden():
                title = node.getTitle()
                if self.buildTOC:
                    key = node.getKey()
                    title = "<h2 class='content-heading' id='%s'>%s <span>(<a href='#tf-toc'>TOC</a>)</span></h2>" % (key, title)
                else:
                    title = "<h2 class='content-heading'>%s</h2>" % title
                content = self.__getContent(node.getId())
                entries.append("<div class='tf-content-block'>\n%s\n%s\n</div>" % (title, content))
        return "<div class='tf-content-holder'>\n%s\n</div>" % self.separator.join(entries)

    def __getContent(self, oid):
        #self.vc("log").debug("Getting content for '{}'", oid)
        content = "<div>Content not found!</div>"
        payload = self.__getPreviewPayload(oid)
        #self.vc("log").debug("Preview payload '{}'", pid)
        if payload is None:
            self.vc("log").error("Failed to get content: '{}'" , oid)
            return ""
        else:
            pid = payload.getId()
            mimeType = payload.getContentType()
            if mimeType.startswith("image/"):
                content = '<img alt="%s" title="%s" src="%s" />' % (pid, pid, pid)
            elif mimeType in ["text/html", "text/xml", "application/xhtml+xml"]:
                content = self.__getPayloadAsString(payload)
            elif mimeType.startswith("text/"):
                content = "<html><body><pre>%s</pre></body></html>" % \
                    self.__getPayloadAsString(payload)
            else:
                content = "<div>unsupported content type: %s</div>" % mimeType
        content, doc = self.__tidy(content)
        content = self.__processMedia(oid, doc, content)
        return content

    def __getPreviewPayload(self, oid):
        object = self.__getObject(oid)
        payloadIdList = object.getPayloadIdList()
        for payloadId in payloadIdList:
            try:
                payload = object.getPayload(payloadId)
                #self.vc("log").debug("{}: {}", payloadId, payload.getType())
                if PayloadType.Preview == payload.getType():
                    return payload
            except Exception, e:
                pass
        return None

    def __getPayloadAsString(self, payload):
        out = ByteArrayOutputStream()
        IOUtils.copy(payload.open(), out)
        payload.close()
        return self.__escapeUnicode(out.toString("UTF-8"))

    def __escapeUnicode(self, unicode):
        result = list()
        for char in unicode:
            try:
                if ord(char) < 128:
                    result.append(char)
                else:
                    result.append('&%s;' % htmlentitydefs.codepoint2name[ord(char)])
            except:
                result.append(char)
        return ''.join(result)

    def __tidy(self, content):
        tidy = Tidy()
        tidy.setIndentAttributes(False)
        tidy.setIndentContent(False)
        tidy.setPrintBodyOnly(True)
        tidy.setSmartIndent(False)
        tidy.setWraplen(0)
        tidy.setXHTML(False)
        tidy.setNumEntities(True)
        tidy.setShowWarnings(False)
        tidy.setQuiet(True)
        out = ByteArrayOutputStream()
        doc = tidy.parseDOM(IOUtils.toInputStream(content, "UTF-8"), out)
        content = out.toString("UTF-8")
        return content, doc

    def __processMedia(self, oid, doc, content):
        content = self.__uploadMedia(oid, doc, content, "a", "href")
        content = self.__uploadMedia(oid, doc, content, "img", "src")
        return content

    def __uploadMedia(self, oid, doc, content, elem, attr):
        links = doc.getElementsByTagName(elem)
        for i in range(0, links.getLength()):
            elem = links.item(i)
            attrValue = elem.getAttribute(attr)
            pid = attrValue
            internalSrc = attrValue
            if attrValue == '' or attrValue.startswith("#") \
                or attrValue.startswith("mailto:") \
                or attrValue.find("://") != -1:
                continue
            else:
                split = attrValue.split("/")
                pid = split[len(split)-1]
                if len(split)>1:
                    # Normally files returned by ice rendition
                    internalSrc = "%s/%s" % (split[len(split)-2], split[len(split)-1])

            #self.vc("log").debug("Uploading '{}' ({})", pid, elem.tagName + ", " + attr)
            found = False
            try:
                payload = self.__getObject(oid).getPayload(pid)
                found = True
            except Exception, e:
                pid = URLDecoder.decode(pid, "UTF-8")
                try:
                    payload = self.__getObject(oid).getPayload(pid)
                    found = True
                except Exception, e:
                    try:
                        #self.vc("log").debug("Trying to get: '{}' payload", internalSrc)
                        payload = self.__getObject(oid).getPayload(internalSrc)
                        found = True
                    except Exception, e:
                        self.vc("log").error("Payload not found '{}'", pid)
            if found:
                #HACK to upload PDFs
                contentType = payload.getContentType().replace("application/", "image/")
                entry = self.__postMedia(payload.getLabel(), contentType, payload.open())
                payload.close()
                if entry is not None:
                    id = entry.getId()
                    #self.vc("log").debug("Replacing '{}' with '{}'", attrValue, id)
                    content = content.replace('%s="%s"' % (attr, attrValue),
                                              '%s="%s"' % (attr, id))
                    content = content.replace("%s='%s'" % (attr, attrValue),
                                              "%s='%s'" % (attr, id))
                else:
                    self.vc("log").error("Failed to upload '{}'", pid)
        return content

    def __getCollection(self, type):
        workspaces = self.__service.getWorkspaces()
        if len(workspaces) > 0:
            return workspaces[0].findCollection(None, type)
        return None

    def __postMedia(self, slug, type, media):
        #self.vc("log").debug("Uploading media '{}', '{}'", slug, type)
        collection = self.__getCollection(type)
        if collection is not None:
            entry = collection.createMediaEntry(slug, slug, type, media)
            collection.addEntry(entry)
            return entry
        return None

    def __post(self, title, content):
        collection = self.__getCollection("application/atom+xml;type=entry")
        if collection is not None:
            entry = collection.createEntry()
            entry.setTitle(title)
            entry.setContent(content, Content.HTML)
            collection.addEntry(entry)
            return True, entry
        else:
            return False, "No valid collection found"
        return False, "An unknown error occured"

    def __getObject(self, oid):
        obj = None
        try:
            storage = self.services.getStorage()
            try:
                obj = storage.getObject(oid)
            except StorageException:
                sid = self.__getStorageId(oid)
                if sid is not None:
                    obj = storage.getObject(sid)
                    self.vc("log").error("Failed to upload '{}'", pid)
        except StorageException:
            self.vc("log").error("Object not found '{}'", pid)
        return obj
