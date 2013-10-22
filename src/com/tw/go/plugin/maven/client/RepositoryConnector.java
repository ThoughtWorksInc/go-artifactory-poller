package com.tw.go.plugin.maven.client;

import com.thoughtworks.go.plugin.api.logging.Logger;
import com.tw.go.plugin.maven.artifactory.RepoResponse;
import com.tw.go.plugin.maven.config.LookupParams;
import com.tw.go.plugin.util.HttpRepoURL;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.util.EntityUtils;

public class RepositoryConnector {
    private static final Logger LOGGER = Logger.getLoggerFor(RepositoryConnector.class);

    public RepositoryConnector() {
    }

    RepoResponse doHttpRequest(String username, String password,
                               String url) {
        HttpClient client = createHttpClient(username, password);

        String responseBody;
        HttpGet method = null;
        try {
            method = createGetMethod(url);
            HttpResponse response = client.execute(method);
            if(response.getStatusLine().getStatusCode() != HttpStatus.SC_OK){
                throw new RuntimeException(String.format("HTTP %s, %s",
                        response.getStatusLine().getStatusCode(), response.getStatusLine().getReasonPhrase()));
            }
            HttpEntity entity = response.getEntity();
            responseBody = EntityUtils.toString(entity);
            String mimeType = ContentType.get(entity).getMimeType();
            return new RepoResponse(responseBody, mimeType);
        } catch (Exception e) {
            String message = String.format("Exception while connecting to %s\n%s", url, e);
            LOGGER.error(message);
            throw new RuntimeException(message, e);
        } finally {
            if (method != null) {
                method.releaseConnection();
            }
        }
    }

    HttpGet createGetMethod(String url) {
        HttpGet method = new HttpGet(url);
        method.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, 10 * 1000);
        return method;
    }

    HttpClient createHttpClient(String username, String password) {
        DefaultHttpClient client = HttpRepoURL.getHttpClient();
        if (username != null) {
            Credentials creds = new UsernamePasswordCredentials(username, password);
            CredentialsProvider credsProvider = new BasicCredentialsProvider();
            credsProvider.setCredentials(AuthScope.ANY, creds);
            client.setCredentialsProvider(credsProvider);
        }
        return client;
    }

    public boolean testConnection(String url, String username,
                                  String password) {

        boolean success = false;
        HttpClient client = createHttpClient(username, password);

        HttpGet method = null;
        try {
            method = createGetMethod(url);

            HttpResponse response = client.execute(method);
            success = (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK);
            if(!success)
                LOGGER.warn("testConnection failed with status code "+response.getStatusLine().getStatusCode()+response.getStatusLine().getReasonPhrase());
        } catch (Exception e) {
            String message = String.format("Exception while connecting to %s\n%s", url, e.getMessage());
            LOGGER.error(message);
            throw new RuntimeException(message, e);
        } finally {
            if (method != null) {
                method.releaseConnection();
            }
        }
        return success;
    }

    public RepoResponse makeAllVersionsRequest(LookupParams lookupParams) {
        String url = lookupParams.getAllVersionsRequestUrl();
        LOGGER.info("Getting versions from " + url);
        return doHttpRequest(lookupParams.getUsername(), lookupParams.getPassword(), url);
    }

    public RepoResponse makeGAVCRequest(LookupParams lookupParams, String revision) {
        String baseurl = lookupParams.getGAVCurl(revision);
        LOGGER.debug("GAVC " + baseurl);
        return doHttpRequest(lookupParams.getUsername(), lookupParams.getPassword(), baseurl);
    }

}