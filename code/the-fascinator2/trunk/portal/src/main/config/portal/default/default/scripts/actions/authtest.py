from au.edu.usq.fascinator.common import JsonConfigHelper

class AuthtestData:

    def __init__(self):
        pass

    def __activate__(self, context):
        response = context["response"]
        writer = response.getPrintWriter("application/json; charset=UTF-8")
        result = JsonConfigHelper()

        if context["page"].authentication.is_logged_in():
            result.set("isAuthenticated", "true")
        else:
            result.set("isAuthenticated", "false")

        writer.println(result.toString())
        writer.close()
