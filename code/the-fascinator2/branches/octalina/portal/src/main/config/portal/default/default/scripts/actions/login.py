class LoginData:

    def __init__(self):
        if page.authentication.is_logged_in():
            if page.authentication.is_admin():
                responseMsg = page.authentication.get_name() + ":admin"
            else:
                responseMsg = page.authentication.get_name() + ":notadmin"
        else:
            responseMsg = page.authentication.get_error()
            response.setStatus(500)
        writer = response.getPrintWriter("text/html; charset=UTF-8")
        writer.println(responseMsg)
        writer.close()

scriptObject = LoginData()
