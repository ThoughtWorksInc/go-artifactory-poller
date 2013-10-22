package com.tw.go.plugin.maven.config;

import com.tw.go.plugin.util.HttpRepoURL;

public class ServerURL extends HttpRepoURL {
    public ServerURL(String url, String user, String password) {
        super(url, user, password);
    }

    public ServerURL(HttpRepoURL httpRepoURL) {
        this(httpRepoURL.getUrlStrWithTrailingSlash(),
                httpRepoURL.getCredentials() == null ? null : httpRepoURL.getCredentials().getUser(),
                httpRepoURL.getCredentials() == null ? null : httpRepoURL.getCredentials().getPassword());
    }

    public String getCheckConnectionURL() {
        return getUrlWithBasicAuth() + "api/application.wadl";
    }

    public String getUser() {
        return  getCredentials() == null ? null : getCredentials().getUser();
    }

    public String getPassword() {
        return  getCredentials() == null ? null : getCredentials().getPassword();
    }
}
