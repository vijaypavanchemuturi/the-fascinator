package au.edu.usq.fascinator.util;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.openid4java.consumer.ConsumerException;
import org.openid4java.consumer.ConsumerManager;
import org.openid4java.consumer.VerificationResult;
import org.openid4java.discovery.DiscoveryInformation;
import org.openid4java.discovery.Identifier;
import org.openid4java.message.AuthRequest;
import org.openid4java.message.AuthSuccess;
import org.openid4java.message.ParameterList;
import org.openid4java.message.ax.AxMessage;
import org.openid4java.message.ax.FetchResponse;
import org.openid4java.util.HttpClientFactory;
import org.openid4java.util.ProxyProperties;

public class OpenIdAuthentication {

    private Logger log = Logger.getLogger(OpenIdAuthentication.class);

    private static ConsumerManager manager;

    private static ConsumerManager getManager() throws ConsumerException {
        if (manager == null) {
            manager = new ConsumerManager();
        }
        return manager;
    }

    public String request(HttpServletRequest req, String openId)
        throws Exception {
        String proxyHost = System.getProperty("http.proxyHost");
        String proxyPort = System.getProperty("http.proxyPort");
        if (proxyHost != null) {
            ProxyProperties proxy = new ProxyProperties();
            proxy.setProxyHostName(proxyHost);
            proxy.setProxyPort(Integer.parseInt(proxyPort));
            HttpClientFactory.setProxyProperties(proxy);
        }
        String returnToUrl = req.getScheme() + "://" + req.getServerName()
            + ":" + req.getServerPort() + req.getContextPath() + "/openid";
        List discoveries = getManager().discover(openId);
        DiscoveryInformation discovered = manager.associate(discoveries);
        AuthRequest authReq = getManager().authenticate(discovered, returnToUrl);
        req.getSession().setAttribute("openid-disc", discovered);
        return authReq.getDestinationUrl(true);
    }

    public boolean verify(HttpServletRequest request) throws Exception {
        ParameterList params = new ParameterList(request.getParameterMap());
        DiscoveryInformation discovered = (DiscoveryInformation) request.getSession()
            .getAttribute("openid-disc");

        // extract the receiving URL from the HTTP request
        StringBuffer receivingURL = request.getRequestURL();
        String queryString = request.getQueryString();
        if (queryString != null && queryString.length() > 0) {
            receivingURL.append("?").append(request.getQueryString());
        }

        // verify the response; ConsumerManager needs to be the same
        // (static) instance used to place the authentication request
        VerificationResult verification = getManager().verify(
            receivingURL.toString(), params, discovered);

        // examine the verification result and extract the verified identifier
        Identifier verified = verification.getVerifiedId();
        if (verified != null) {
            AuthSuccess authSuccess = (AuthSuccess) verification.getAuthResponse();
            if (authSuccess.hasExtension(AxMessage.OPENID_NS_AX)) {
                FetchResponse fetchResp = (FetchResponse) authSuccess.getExtension(AxMessage.OPENID_NS_AX);
                Map<String, String> attrs = (Map<String, String>) fetchResp.getAttributes();
                List emails = fetchResp.getAttributeValues("email");
                String email = (String) emails.get(0);
                for (String key : attrs.keySet()) {
                    log.debug(key + ": "
                        + fetchResp.getAttributeValues(key).get(0));
                }
            }
            return authSuccess.isValid();
        }
        return false;
    }
}
