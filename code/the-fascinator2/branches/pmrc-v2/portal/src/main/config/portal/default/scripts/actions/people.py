import au.edu.usq.fascinator.common.BasicHttpClient as BasicHttpClient
import org.apache.commons.httpclient.methods.PostMethod as PostMethod
from java.io import ByteArrayInputStream, ByteArrayOutputStream

from java.lang import Boolean, String
from org.dom4j.io import OutputFormat, XMLWriter, SAXReader

from au.edu.usq.fascinator.common import JsonConfigHelper

from org.w3c.tidy import Tidy

class PeopleNames:
    def __init__(self):
        self.url = "http://www.vietnamroll.gov.au/VeteranSearch.aspx"
        responseType = "text/html"
        responseMsg = ""
        func = formData.get("func")
        if func == "searchName":
            firstName = formData.get("firstName")
            surname = formData.get("surname")
            
            viewStateValue = self.__getViewStateValue()
            if viewStateValue:
                #Process the search
                client = BasicHttpClient(self.url)
                post = PostMethod(self.url)
                post.addParameter("txtSimpleLastname", surname)
                post.addParameter("txtSimpleFirstname", firstName)
                post.removeParameter("__VIEWSTATE")
                post.addParameter("__VIEWSTATE", viewStateValue)
                post.addParameter("btnSearchSimple", "Search")
                statusInt = client.executeMethod(post)
                
                if statusInt==200:
                    searchResponse = post.getResponseBodyAsString()
                    searchResultDoc = self.__tidyResponse(searchResponse)
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
        website = "http://www.vietnamroll.gov.au/"
        allVeteran = document.selectNodes("//tr[@class='dgItem'] | //tr[@class='dgAlternatingItem']")
        
        colRows = ''
        for veteran in allVeteran:
            veteranList = veteran.selectNodes("./td/a")
            veteranName = ''
            veteranLink = ''
            rowStr = ""
            for veteranInfo in veteranList:
                if veteranInfo.valueOf("./@id").find("VeteranName") > -1:
                    veteranName = veteranInfo.getText()
                veteranLink = '%s%s' % (website, veteranInfo.valueOf("./@href"))
                rowStr += "<td><a href='%s' class='%s' target='_blank'>%s</a></td>" % \
                           (veteranLink, veteranInfo.valueOf("./@class"), veteranInfo.getText())
            rowStr = "<tr><td><input type='radio' id='selected_people' name='selected_people' value='%s' rel='%s'/></td>%s</tr>" % \
                        (veteranName, veteranLink, rowStr)
            colRows += rowStr
        
        tableStr = "<table><thead><tr><th></th><th>Service No</th><th>Name</th><th>Service Name</th></thead><tbody></tbody>%s</table>" \
                   % colRows
        
        if int(numOfRecord) > 20:
            tableStr = "<p><strong>There are more results available (total: %s records), please refine your search</strong></p>%s" % (numOfRecord, tableStr)
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
        

            
            
            
            
#            post = PostMethod(url)
#            post.addParameter("txtSimpleLastname", "surname")
#            post.addParameter("txtSimpleFirstname", "firstName")
#            statusInt = client.executeMethod(post)
#            
#            r = str(statusInt)
#            print " * people.py: ", r
            #print post.getResponseBodyAsString()
#        responseType = "text/html"
#        responseMsg = ""
#        func = formData.get("func")
#        if func == "placeName":
#            try:
#                placeName = formData.get("q")
#                url = "http://ws.geonames.org/searchJSON?fuzzy=0.7&q=" + placeName
#                client = BasicHttpClient(url)
#                get = GetMethod(url)
#                statusInt = client.executeMethod(get)
#                r = str(statusInt)
#                jsonConfigHelper = JsonConfigHelper(get.getResponseBodyAsString().strip())
#                for geoName in jsonConfigHelper.getJsonList("geonames"):
#                    responseMsg += "%s, %s|%s \n" % (geoName.get("name"), geoName.get("countryName"), geoName.get("geonameId"))
#            except Exception, e:
#                print "exception: ", str(e)
#                r = str(e), None
#            responseType = "text/plain"
#            #responseMsg = "\nToowoomba, Australia\nToowoomba, Africa";
#        writer = response.getPrintWriter(responseType)
#        writer.println(responseMsg)
#        writer.close()

scriptObject = PeopleNames()

