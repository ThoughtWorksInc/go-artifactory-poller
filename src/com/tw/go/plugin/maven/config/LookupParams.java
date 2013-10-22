package com.tw.go.plugin.maven.config;

import com.thoughtworks.go.plugin.api.material.packagerepository.PackageRevision;
import com.tw.go.plugin.util.HttpRepoURL;
import maven.Version;


public class LookupParams {
    public static final String PACKAGE_LOCATION = "LOCATION";
    public static final String PACKAGE_VERSION = "VERSION";
    public static final String ANY = "ANY";
    private final String groupId;
    private final String repoId;
    private final HttpRepoURL serverUrl;
    private final String artifactId;

    private String pollVersionFrom = ANY;
    private String pollVersionTo = ANY;
    private PackageRevision lastKnownVersion = null;

    public LookupParams(HttpRepoURL serverUrl, String repoId, String groupId, String artifactId, String pollVersionFrom, String pollVersionTo, PackageRevision previouslyKnownRevision) {
        this.serverUrl = serverUrl;
        this.repoId = repoId;
        this.groupId = groupId;
        this.artifactId = artifactId;
        if (pollVersionFrom != null && !pollVersionFrom.trim().isEmpty()) this.pollVersionFrom = pollVersionFrom;
        if (pollVersionTo != null && !pollVersionTo.trim().isEmpty()) this.pollVersionTo = pollVersionTo;
        this.lastKnownVersion = previouslyKnownRevision;
    }

    public String getGroupId() {
        return groupId;
    }

    public String getRepoId() {
        return repoId;
    }

    public String getArtifactId() {
        return artifactId;
    }

    public String getRepoUrlStr() {
        return serverUrl.getUrlStr();
    }

    public boolean isLastVersionKnown() {
        return lastKnownVersion != null;
    }

    public String getLastKnownVersion() {
        if (lastKnownVersion == null) return null;
        return lastKnownVersion.getDataFor(PACKAGE_VERSION);
    }

    public boolean lowerBoundGiven() {
        return !ANY.equals(pollVersionFrom);
    }

    public boolean upperBoundGiven() {
        return !ANY.equals(pollVersionTo);
    }

    public String getPassword() {
        return serverUrl.getCredentials().getPassword();
    }

    public String getUsername() {
        return serverUrl.getCredentials().getUser();
    }

    public Version getUpperBound() {
        return new Version(pollVersionTo);
    }

    public Version lowerBound() {
        return new Version(pollVersionFrom);
    }

    public String getRepoUrlStrWithBasicAuth() {
        return serverUrl.getUrlWithBasicAuth();
    }

//    http://localhost:8081/artifactory/api/search/latestVersion?g=tw&a=deb-repo-poller&repos=libs-release-local
//    GET /api/search/versions?g=org.acme&a=artifact&repos=libs-release-local
//    {
//        "results": [
//        {
//            "version": "1.2",
//                "integration": false
//        },{
//        "version": "1.0-SNAPSHOT",
//                "integration": true
//    },{
//        "version": "1.0",
//                "integration": false
//    }
//        ]
//    }
    public String getAllVersionsRequestUrl() {
        StringBuilder sb = new StringBuilder();
        sb.append(serverUrl.getUrlStr());
        if(! sb.toString().endsWith("/")) sb.append("/");
        sb.append("api/search/versions?g=");
        sb.append(groupId);
        sb.append("&a=");
        sb.append(artifactId);
        sb.append("&repos=");
        sb.append(repoId);
        return sb.toString();
    }

//GET /api/search/gavc?g=org.acme&a=artifact&v=1.0&c=sources&repos=libs-release-local
//    {
//        "results": [
//        {
//            "uri": "http://localhost:8080/artifactory/api/storage/libs-release-local/org/acme/artifact/1.0/artifact-1.0-sources.jar"
//        },{
//        "uri": "http://localhost:8080/artifactory/api/storage/libs-release-local/org/acme/artifactB/1.0/artifactB-1.0-sources.jar"
//    }
//        ]
//    }
    public String getGAVCurl(String revision) {
        StringBuilder sb = new StringBuilder();
        sb.append(serverUrl.getUrlStr());
        if(! sb.toString().endsWith("/")) sb.append("/");
        sb.append("api/search/gavc?g=");
        sb.append(groupId);
        sb.append("&a=");
        sb.append(artifactId);
        sb.append("&repos=");
        sb.append(repoId);
        sb.append("&v=");
        sb.append(revision);
        return sb.toString();
    }

    public String getFilesUrlWithBasicAuth(String revision) {
        return getGAVCurl(revision);
    }
}
