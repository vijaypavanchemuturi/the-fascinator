from au.edu.usq.fascinator.api.authentication import AuthenticationException;
from au.edu.usq.fascinator.api.authentication import User;

class Authentication:
    current_user = None
    error_message = None

    def __init__(self, layout):
        global getVar
        getVar = layout
        self.auth = getVar('Services').getAuthManager()
        self.check_login()

    def session_init(self):
        # Debugging purpose
        if getVar('formData').get("verb") == "clear-session":
            getVar('sessionState').clear()

    def check_login(self):
        action = getVar('formData').get("verb")

        # User is logging in
        if (action == "login"):
            username = getVar('formData').get("username")
            if username is not None:
                password = getVar('formData').get("password")
                self.login(username, password)

        # Normal page render, or logout
        else:
            username = getVar('sessionState').get("username")
            source   = getVar('sessionState').get("source")
            if username is not None:
                print "****** Already logged in"
                self.current_user = self.get_user(username, source)

        # User is logging out, make sure we ran get_user() first
        if (action == "logout"):
            self.logout()

    def is_logged_in(self):
        if self.current_user is not None:
            return True
        else:
            return False

    def login(self, username, password):
        try:
            self.current_user = self.auth.logIn(username, password)
            self.error_message = None
            getVar('sessionState').set("username", username)
            getVar('sessionState').set("source",   self.current_user.getSource())
        except AuthenticationException, e:
            self.current_user  = None
            self.error_message = self.parse_error(e)

    def logout(self):
        if self.current_user is not None:
            try:
                self.auth.logOut(self.current_user)
                self.current_user  = None
                self.error_message = None
                getVar('sessionState').set("username", None)
                getVar('sessionState').set("source",   None)
            except AuthenticationException, e:
                self.error_message = self.parse_error(e)

    def get_user(self, username, source):
        try:
            print "Username : " + username
            print "Source : " + source
            print dir(self.auth)
            self.auth.setActivePlugin(source);
            return self.auth.getUser(username)
        except AuthenticationException, e:
            self.error_message = self.parse_error(e)

    def get_name(self):
        if self.current_user is not None:
            return self.current_user.realName();
        else:
            return "Guest";

    def change_password(self, username, password):
        try:
            self.auth.changePassword(username, password)
            return "Password changed."
        except AuthenticationException, e:
            return self.parse_error(e)

    def create_user(self, username, password):
        try:
            self.current_user = self.auth.createUser(username, password)
            return self.current_user.name();
        except AuthenticationException, e:
            return self.parse_error(e)

    def describe_user(self):
        return self.auth.describeUser()

    def get_error(self):
        return self.error_message

    # Strip out java package names from
    # error strings.
    def parse_error(self, error):
        message = error.getMessage()
        i = message.find(":")
        return message[i+1:].strip()
