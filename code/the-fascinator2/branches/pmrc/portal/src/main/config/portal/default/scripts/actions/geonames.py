
class GeoNames:
    def __init__(self):
        responseType = "text/html"
        responseMsg = ""
        func = formData.get("func")
        if func.startswith("placeName"):
            value = placeName.split("?")[1];
            print "--- value: ", value;
            responseType = "text/plain"
            responseMsg = "\nToowoomba, Australia\nToowoomba, Africa";
        writer = response.getPrintWriter(responseType)
        writer.println(responseMsg)
        writer.close()

scriptObject = GeoNames()