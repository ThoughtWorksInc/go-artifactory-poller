package com.tw.go.plugin.maven.artifactory;

import com.thoughtworks.go.plugin.api.logging.Logger;
import maven.MavenVersion;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ArtifactoryResponseHandler {
    private static final Logger LOGGER = Logger.getLoggerFor(ArtifactoryResponseHandler.class);
    private final RepoResponse repoResponse;
    private JSONArray jsonResponse;

    public ArtifactoryResponseHandler(RepoResponse repoResponse) {
        this.repoResponse = repoResponse;
    }

    public boolean canUnderstandVersions() {
        if(!repoResponse.isVersionsResult()){
            LOGGER.warn("ArtifactoryResponseHandler can't handle: "+repoResponse.getMimeType());
            return false;
        }
        jsonResponse = new JSONObject(repoResponse.getResponseBody()).getJSONArray("results");
        return jsonResponse != null;
    }
    public boolean canUnderstandFiles() {
        if(!repoResponse.isGavcResult()){
            LOGGER.warn("ArtifactoryResponseHandler can't handle: "+repoResponse.getMimeType());
            return false;
        }
        jsonResponse = new JSONObject(repoResponse.getResponseBody()).getJSONArray("results");
        return jsonResponse != null;
    }

    public boolean canUnderstandFileInfo() {
        if(!repoResponse.isFileInfoResult()){
            LOGGER.warn("ArtifactoryResponseHandler can't handle: "+repoResponse.getMimeType());
            return false;
        }
        String downloadUri = new JSONObject(repoResponse.getResponseBody()).getString("downloadUri");
        return downloadUri != null;
    }

    public List<MavenVersion> getAllVersions() {
        if(jsonResponse == null && !canUnderstandVersions()){
            LOGGER.warn("ArtifactoryResponseHandler getAllVersions invalidContent");
            throw new RuntimeException("getAllVersions: Invalid response");
        }
        List<MavenVersion> versions = new ArrayList<MavenVersion>();
        for(int i=0; i< jsonResponse.length(); i++)
            versions.add(new MavenVersion(((JSONObject) jsonResponse.get(i)).getString("version")));
        return versions;
    }

    public String getPOMurl() {
        if(jsonResponse == null && !canUnderstandFiles())
            throw new RuntimeException("getArtifactURL: Invalid response");
        if(jsonResponse.length() !=2) LOGGER.warn("expected 2 uri but was\n "+jsonResponse.toString(2));
        return ((JSONObject) jsonResponse.get(1)).getString("uri");
    }

    public String getArtifactURL() {
        if(jsonResponse == null && !canUnderstandFiles())
            throw new RuntimeException("getArtifactURL: Invalid response");
        if(jsonResponse.length() !=2) LOGGER.warn("expected 2 uri but was\n "+jsonResponse.toString(2));
        return ((JSONObject) jsonResponse.get(0)).getString("uri");
    }

    public String getPOMDownloadURL() {
        if(repoResponse.isFileInfoResult())
            return new JSONObject(repoResponse.getResponseBody()).getString("downloadUri");
        throw new RuntimeException("Unable to getPOMDownloadURL. mimetype is "+ repoResponse.getMimeType());
    }
    public String getModifiedBy() {
        if(repoResponse.isFileInfoResult())
            return new JSONObject(repoResponse.getResponseBody()).getString("modifiedBy");
        throw new RuntimeException("Unable to getModifiedBy. mimetype is "+ repoResponse.getMimeType());
    }
    public Date getLastModified() {
        if(repoResponse.isFileInfoResult())
            return javax.xml.bind.DatatypeConverter.parseDateTime((new JSONObject(repoResponse.getResponseBody()).getString("lastModified"))).getTime();
        throw new RuntimeException("Unable to lastModified. mimetype is "+ repoResponse.getMimeType());
    }

    public String getArtifactDownloadURL() {
        if(repoResponse.isFileInfoResult())
            return new JSONObject(repoResponse.getResponseBody()).getString("downloadUri");
        throw new RuntimeException("Unable to getArtifactDownloadURL. mimetype is "+ repoResponse.getMimeType());
    }
}
