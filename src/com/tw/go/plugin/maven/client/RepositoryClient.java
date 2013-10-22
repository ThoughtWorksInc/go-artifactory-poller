package com.tw.go.plugin.maven.client;

import com.thoughtworks.go.plugin.api.logging.Logger;
import com.tw.go.plugin.maven.artifactory.RepoResponse;
import com.tw.go.plugin.maven.config.LookupParams;
import com.tw.go.plugin.maven.artifactory.ArtifactoryResponseHandler;
import maven.MavenVersion;
import maven.Model;
import org.xml.sax.InputSource;

import java.io.StringReader;
import java.util.Collections;
import java.util.List;

public class RepositoryClient {

    private static final Logger LOGGER = Logger.getLoggerFor(RepositoryClient.class);
    private RepositoryConnector repositoryConnector = new RepositoryConnector();
    private LookupParams lookupParams;

    public RepositoryClient(LookupParams lookupParams) {
        this.lookupParams = lookupParams;
    }

    public MavenVersion getLatest() {
        RepoResponse repoResponse = repositoryConnector.makeAllVersionsRequest(lookupParams);
        LOGGER.debug(repoResponse.getResponseBody());
        List<MavenVersion> allVersions = getAllVersions(repoResponse);
        MavenVersion latest = getLatest(allVersions);
        if(latest != null){
            latest.setArtifactId(lookupParams.getArtifactId());
            latest.setGroupId(lookupParams.getGroupId());
            LOGGER.info("Latest is "+latest.getRevisionLabel());
            setVersionInfoWrapper(latest);
        }else{
            LOGGER.warn("getLatest returning null");
        }
        return latest;
    }

    public void setVersionInfoWrapper(MavenVersion version) {
        try{
            setVersionInfo(version);
        }catch(Exception ex){
            LOGGER.error("Error getting version info for " + version.getRevisionLabel());
            if(ex.getMessage() != null)
                LOGGER.error(ex.getMessage());
            if(ex.getCause() != null && ex.getCause().getMessage() != null){
                LOGGER.error(ex.getCause().getMessage());
            }
            version.setErrorMessage("Plugin could not determine location/trackback. Please see plugin log for details.");
        }
    }

    MavenVersion getLatest(List<MavenVersion> allVersions) {
        if(allVersions == null || allVersions.isEmpty()) return null;
        MavenVersion latest = maxSubjectToUpperBound(allVersions);
        if(latest == null) {
            LOGGER.info("maxSubjectToUpperBound is null");
            return null;
        }
        if (lookupParams.isLastVersionKnown()) {
            LOGGER.info("lastKnownVersion is "+ lookupParams.getLastKnownVersion());
            MavenVersion lastKnownVersion = new MavenVersion(lookupParams.getLastKnownVersion());
            if (noNewerVersion(latest, lastKnownVersion)) {
                LOGGER.info("no newer version");
                return null;
            }
        }
        if(!lookupParams.lowerBoundGiven() || latest.greaterOrEqual(lookupParams.lowerBound())){
            return latest;
        }else{
            LOGGER.info("latestSubjectToLowerBound is null");
            return null;
        }
    }

    private MavenVersion maxSubjectToUpperBound(List<MavenVersion> allVersions) {
        MavenVersion absoluteMax = Collections.max(allVersions);
        if(!lookupParams.upperBoundGiven()) return absoluteMax;
        Collections.sort(allVersions);
        for(int i = 0; i < allVersions.size(); i++){
            if(allVersions.get(i).lessThan(lookupParams.getUpperBound()) &&
                    i+1 <= allVersions.size()-1 &&
                    allVersions.get(i+1).greaterOrEqual(lookupParams.getUpperBound()))
                return allVersions.get(i);
            if(allVersions.get(i).lessThan(lookupParams.getUpperBound()) &&
                    i+1 == allVersions.size())
                return allVersions.get(i);
        }
        return null;
    }

    private boolean noNewerVersion(MavenVersion latest, MavenVersion lastKnownVersion) {
        return latest.notNewerThan(lastKnownVersion);
    }

    private List<MavenVersion> getAllVersions(RepoResponse repoResponse) {
        List<MavenVersion> versions;
        ArtifactoryResponseHandler responseHandler = new ArtifactoryResponseHandler(repoResponse);
        if (responseHandler.canUnderstandVersions()) {
            versions = responseHandler.getAllVersions();
        } else {
            throw new RuntimeException("ArtifactoryResponseHandler can't handle response");
        }
        return versions;
    }

    private void setVersionInfo(MavenVersion version) {
        RepoResponse gavcResponse = repositoryConnector.makeGAVCRequest(lookupParams, version.getV_Q());
        ArtifactoryResponseHandler gavcResponseHandler = new ArtifactoryResponseHandler(gavcResponse);
        String pomUrl;
        String artifactURL;
        if (gavcResponseHandler.canUnderstandFiles()) {
            artifactURL = gavcResponseHandler.getArtifactURL();
            LOGGER.info("artifactURL is "+ artifactURL);
            pomUrl = gavcResponseHandler.getPOMurl();
            LOGGER.info("pomUrl is "+ pomUrl);
        } else {
            throw new RuntimeException("ArtifactoryResponseHandler can't handle response");
        }
        ArtifactoryResponseHandler artifactResponseHandler = new ArtifactoryResponseHandler(doHttpRequest(artifactURL));
        if (artifactResponseHandler.canUnderstandFileInfo()) {
            version.setLocation(artifactResponseHandler.getArtifactDownloadURL());
        }
        ArtifactoryResponseHandler responseHandler = new ArtifactoryResponseHandler(doHttpRequest(pomUrl));
        Model model;
        if (responseHandler.canUnderstandFileInfo()) {
            version.setLastModified(responseHandler.getLastModified());
            version.setModifiedBy(responseHandler.getModifiedBy());
            String pomDownloadURL = responseHandler.getPOMDownloadURL();
            LOGGER.info("pomDownloadURL is "+ pomDownloadURL);
            RepoResponse pomContents = doHttpRequest(pomDownloadURL);
            model = Model.unmarshal(new InputSource(new StringReader(pomContents.getResponseBody())));
        }else{
            throw new RuntimeException("ArtifactoryResponseHandler cannot understand FileInfo. mimetype is "+ doHttpRequest(pomUrl).getMimeType());
        }
        version.setTrackBackUrl(model.getUrl());
    }

    private RepoResponse doHttpRequest(String artifactURL) {
        return repositoryConnector.doHttpRequest(lookupParams.getUsername(), lookupParams.getPassword(),
                artifactURL);
    }

    void setRepositoryConnector(RepositoryConnector repositoryConnector) {
        this.repositoryConnector = repositoryConnector;
    }
}
