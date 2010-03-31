import au.edu.usq.fascinator.common.BasicHttpClient as BasicHttpClient
import org.apache.commons.httpclient.methods.PostMethod as PostMethod
import org.apache.commons.httpclient.methods.GetMethod as GetMethod

from java.io import ByteArrayInputStream, ByteArrayOutputStream

from java.lang import Boolean, String
from org.dom4j.io import OutputFormat, XMLWriter, SAXReader

from au.edu.usq.fascinator.common import JsonConfigHelper

from org.w3c.tidy import Tidy

class PeopleNames:
    def __init__(self):
        self.url = "http://www.vietnamroll.gov.au/VeteranSearch.aspx"
        self.website = "http://www.vietnamroll.gov.au/"
        responseType = "text/html"
        responseMsg = ""
        func = formData.get("func")
        if func == "searchName":
            firstName = formData.get("firstName")
            surname = formData.get("surname")
            htmlStr = ""
            viewStateValue = self.__getViewStateValue()
            if viewStateValue == 404:
                htmlStr = "<p><strong>Could not connect to <a href='http://www.vietnamroll.gov.au' target='_blank'>http://www.vietnamroll.gov.au</a> to retrieve the data</strong></p>"
            else:
                #Process the search
                client = BasicHttpClient(self.url)
                post = PostMethod(self.url)
                post.addParameter("txtSimpleLastname", surname)
                post.addParameter("txtSimpleFirstname", firstName)
                post.removeParameter("__VIEWSTATE")
                post.addParameter("__VIEWSTATE", viewStateValue)
                post.addParameter("btnSearchSimple", "Search")
                statusInt = client.executeMethod(post)
                searchResponse = post.getResponseBodyAsString()
                searchResultDoc = self.__tidyResponse(searchResponse)
                
                foundMoved = False
                if searchResponse.find("Object moved")>-1:
                    movedLinkNode = searchResultDoc.selectSingleNode("//a")
                    moveLink = ""
                    if movedLinkNode:
                        moveLink = "%s%s" % (self.website, movedLinkNode.valueOf("./@href"))
                        #get website
                        getClient = BasicHttpClient(moveLink)
                        get = GetMethod(moveLink)
                        statusInt = getClient.executeMethod(get)
                        searchResponse = get.getResponseBodyAsString()
                        searchResultDoc = self.__tidyResponse(searchResponse)
                        foundMoved = True
                    
                if statusInt==200 or foundMoved:
                    numOfRecord, noRecordFoundStr = self.__recordFound(searchResultDoc)
                    #return noRecordFoundStr message if it's not empty
                    if noRecordFoundStr:
                        htmlStr = "<p><strong>%s</strong></p>" % noRecordFoundStr
                    else:
                        #process the veteran
                        htmlStr = self.__processPeople(searchResultDoc, numOfRecord)
            writer = response.getPrintWriter(responseType)
            writer.println(htmlStr)
            writer.close()
    
    def __processPeople(self, document, numOfRecord):
        colRows = ''
        #Sometime search result will return the full detail of the veteran 
        divFullDetails = document.selectSingleNode("//div[@id='divFullDetails']")
        if divFullDetails != None:
            serviceNumber = ""
            veteranName = ""
            veteranLink = ""
            service = ""
            serviceNumberNode = document.selectSingleNode("//input[@name='txtServiceNo']")
            if serviceNumberNode:
                serviceNumber = serviceNumberNode.valueOf("./@value")
            fullNameNode = document.selectSingleNode("//input[@name='txtFullName']")
            if fullNameNode:
                veteranName = fullNameNode.valueOf("./@value")
            veteranLinkNode = document.selectSingleNode("//form[@name='frmSearch']")
            if veteranLinkNode:
                veteranLink = veteranLinkNode.valueOf("./@action")
            serviceNode = document.selectSingleNode("//input[@name='txtServiceName']")
            if serviceNode:
                service = serviceNode.valueOf("./@value")
            veteranLink = '%s%s' % (self.website, veteranLink)
            colRows = "<tr><td><input type='radio' id='selected_people' name='selected_people' value='%s' rel='%s'/></td>" \
                          "<td><a href='%s' target='_blank'>%s</a></td>" \
                          "<td><a href='%s' target='_blank'>%s</a></td>" \
                          "<td><a href='%s' target='_blank'>%s</a></td>" \
                     "</tr>" % (veteranName, veteranLink, veteranLink, serviceNumber, veteranLink, veteranName, veteranLink, service)
        else:
            #process list of veterans
            allVeteran = document.selectNodes("//tr[@class='dgItem'] | //tr[@class='dgAlternatingItem']")
            for veteran in allVeteran:
                veteranList = veteran.selectNodes("./td/a")
                veteranName = ''
                veteranLink = ''
                rowStr = ""
                for veteranInfo in veteranList:
                    if veteranInfo.valueOf("./@id").find("VeteranName") > -1:
                        veteranName = veteranInfo.getText()
                    veteranLink = '%s%s' % (self.website, veteranInfo.valueOf("./@href"))
                    rowStr += "<td><a href='%s' class='%s' target='_blank'>%s</a></td>" % \
                               (veteranLink, veteranInfo.valueOf("./@class"), veteranInfo.getText())
                rowStr = "<tr><td><input type='radio' id='selected_people' name='selected_people' value='%s' rel='%s'/></td>%s</tr>" % \
                            (veteranName, veteranLink, rowStr)
                colRows += rowStr
            
        tableStr = "<table><thead><tr><th></th><th>Service No</th><th>Name</th><th>Service Name</th></tr></thead><tbody>%s</tbody></table>" \
                   % colRows
        moreResult="<p></p>"
        if int(numOfRecord) > 20:
            moreResult = "<p><strong>There are more results available (total: %s records), please refine your search</strong></p>" % (numOfRecord)
        
        tableStr = "%s%s" % (moreResult, tableStr)
        return tableStr
    
    def __getViewStateValue(self):
        #Execute once to get the __VIEWSTATE value
        client = BasicHttpClient(self.url)
        post = PostMethod(self.url)
        statusInt = client.executeMethod(post)
        
        viewStateValue = ''
        if statusInt==200: 
            response = post.getResponseBodyAsString()
            document = self.__tidyResponse(response)
            viewStateNode = document.selectSingleNode("//input[@name='__VIEWSTATE']")
            viewStateValue = ""
            if viewStateNode:
                viewStateValue = viewStateNode.valueOf("./@value")
        else:
            viewStateValue = 404
        return viewStateValue
    
    def __recordFound(self, document):
        numOfRecord = 0
        noRecordFoundStr = ''
        divNoRecordFound = document.selectSingleNode("//div[@id='divNoRecordFound']")
        if divNoRecordFound:
            noRecordFoundStr = divNoRecordFound.getText()
        if noRecordFoundStr == '':
            btnSearchRefine = document.selectSingleNode("//input[@name='btnSearchRefine']")
            if btnSearchRefine:
                numOfRecordStr = btnSearchRefine.valueOf("./@value")
                numOfRecord = numOfRecordStr[15:numOfRecordStr.find(" Records Found")]
        return numOfRecord, noRecordFoundStr

    def __tidyResponse(self, response):
        tidy = Tidy()
        tidy.setIndentAttributes(False)
        tidy.setIndentContent(False)
        tidy.setPrintBodyOnly(True)
        tidy.setSmartIndent(False)
        tidy.setWraplen(0)
        tidy.setXHTML(True)
        tidy.setNumEntities(True)
        
        out = ByteArrayOutputStream()
        doc = tidy.parseDOM(ByteArrayInputStream(String(response).getBytes()), out)
        content = "<body>%s</body>" % String(out.toString("UTF-8"), "UTF-8")
        try:
            saxReader = SAXReader(Boolean.parseBoolean("false"))
            return saxReader.read(ByteArrayInputStream(String(content).getBytes("UTF-8")))
        except:
            raise "Error in parsing to saxReader" 
        

scriptObject = PeopleNames()

