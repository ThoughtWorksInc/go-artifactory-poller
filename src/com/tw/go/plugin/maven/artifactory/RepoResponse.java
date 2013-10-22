package com.tw.go.plugin.maven.artifactory;

public class RepoResponse {
    final String responseBody;
    final String mimeType;
    public final static String ARTIFACTORY_VERSIONS_RESULT = "application/vnd.org.jfrog.artifactory.search.ArtifactVersionsResult+json";
    private static final String ARTIFACTORY_POM_RESULT = "application/x-maven-pom+xml";
    public final static String ARTIFACTORY_GAVC_RESULT = "application/vnd.org.jfrog.artifactory.search.GavcSearchResult+json";
    public final static String ARTIFACTORY_FILEINFO_RESULT = "application/vnd.org.jfrog.artifactory.storage.FileInfo+json";

    public RepoResponse(String responseBody, String mimeType) {
        this.responseBody = responseBody;
        this.mimeType = mimeType;
    }

    public String getMimeType() {
        return mimeType;
    }

    public String getResponseBody() {
        return responseBody;
    }

    public boolean isVersionsResult() {
        return ARTIFACTORY_VERSIONS_RESULT.equalsIgnoreCase(mimeType);
    }

    public boolean isGavcResult() {
        return ARTIFACTORY_GAVC_RESULT.equalsIgnoreCase(mimeType);
    }

    public boolean isFileInfoResult() {
        return ARTIFACTORY_FILEINFO_RESULT.equalsIgnoreCase(mimeType);
    }

    public boolean isPOMresult() {
        return ARTIFACTORY_POM_RESULT.equalsIgnoreCase(mimeType);
    }
}
