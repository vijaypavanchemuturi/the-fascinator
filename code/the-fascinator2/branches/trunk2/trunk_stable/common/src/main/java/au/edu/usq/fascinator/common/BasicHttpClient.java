/* 
 * The Fascinator - Common Library
 * Copyright (C) 2008 University of Southern Queensland
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
package au.edu.usq.fascinator.common;

import java.io.IOException;
import java.net.URL;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.GetMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Commons HttpClient wrapper that makes it easier to work with proxies and
 * authentication
 * 
 * @author Oliver Lucido
 */
public class BasicHttpClient {

    private Logger log = LoggerFactory.getLogger(BasicHttpClient.class);

    private String baseUrl;

    private UsernamePasswordCredentials credentials;

    public BasicHttpClient(String baseUrl) {
        if (baseUrl.endsWith("/")) {
            baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
        }
        this.baseUrl = baseUrl;
    }

    public void authenticate(String username, String password) {
        credentials = new UsernamePasswordCredentials(username, password);
    }

    public HttpClient getHttpClient(boolean auth) {
        HttpClient client = new HttpClient();
        try {
            URL url = new URL(baseUrl);
            if (!isNonProxyHost(url.getHost())) {
                String proxyHost = System.getProperty("http.proxyHost");
                String proxyPort = System.getProperty("http.proxyPort");
                if (proxyHost != null && !"".equals(proxyHost)) {
                    client.getHostConfiguration().setProxy(proxyHost,
                            Integer.parseInt(proxyPort));
                    log.debug("Using proxy {}:{}", proxyHost, proxyPort);
                }
            }
        } catch (Exception e) {
            log.warn("Failed to get proxy settings: " + e.getMessage());
        }
        if (auth && credentials != null) {
            client.getParams().setAuthenticationPreemptive(true);
            client.getState().setCredentials(AuthScope.ANY, credentials);
            log.debug("Credentials: username={}", credentials.getUserName());
        }
        return client;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public int executeMethod(HttpMethodBase method) throws IOException {
        boolean auth = !(method instanceof GetMethod);
        return executeMethod(method, auth);
    }

    public int executeMethod(HttpMethodBase method, boolean auth)
            throws IOException {
        int status = getHttpClient(auth).executeMethod(method);
        log.debug("{} {} returned {}: {}", new Object[] { method.getName(),
                method.getURI(), status, HttpStatus.getStatusText(status) });
        return status;
    }

    private boolean isNonProxyHost(String host) {
        String httpNonProxyHosts = System.getProperty("http.nonProxyHosts",
                "localhost|127.0.0.1");
        String[] nonProxyHosts = httpNonProxyHosts.split("\\|");
        for (int i = 0; i < nonProxyHosts.length; ++i) {
            if (nonProxyHosts[i].startsWith("*")) {
                if (host.endsWith(nonProxyHosts[i].substring(1))) {
                    return true;
                }
            } else if (host.equals(nonProxyHosts[i])) {
                return true;
            }
        }
        return false;
    }
}
