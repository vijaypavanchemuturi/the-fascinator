import au.edu.usq.fascinator.common.BasicHttpClient as BasicHttpClient
import org.apache.commons.httpclient.methods.PostMethod as PostMethod
from java.lang import Boolean
from org.dom4j.io import OutputFormat, XMLWriter, SAXReader

from au.edu.usq.fascinator.common import JsonConfigHelper

class PeopleNames:
    def __init__(self):
        responseType = "text/html"
        responseMsg = ""
        func = formData.get("func")
        if func=="searchName":
            url = "http://www.vietnamroll.gov.au/VeteranSearch.aspx"
            firstName = formData.get("firstName")
            surname = formData.get("surname")
            client = BasicHttpClient(url)
            
            #Execute once to get the __VIEWSTATE value
            post = PostMethod(url)
            statusInt = client.executeMethod(post)
            response = post.getResponseBodyAsString()
            #remove last div ouside the html tag
            response = response[:response.find("</html>") + 7]
            saxReader = SAXReader(Boolean.parseBoolean("false"))
            document = saxReader.read(response)
            print "document: ", document
            
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
