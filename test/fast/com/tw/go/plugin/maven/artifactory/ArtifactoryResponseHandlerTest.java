package com.tw.go.plugin.maven.artifactory;

import maven.MavenVersion;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class ArtifactoryResponseHandlerTest {
    @Test
    public void shouldRetriveAllVersionsInResponse(){
        String body = "{\"results\":[{\"version\":\"1.1.3\",\"integration\":false},{\"version\":\"1.1.1\",\"integration\":false}]}";
        RepoResponse response = new RepoResponse(body, RepoResponse.ARTIFACTORY_VERSIONS_RESULT);
        ArtifactoryResponseHandler responseHandler = new ArtifactoryResponseHandler(response);
        List<MavenVersion> allVersions = responseHandler.getAllVersions();
        assertThat(allVersions.get(0).getVersion(), is("1.1.3"));
        assertThat(allVersions.get(1).getVersion(), is("1.1.1"));
    }

    @Test
    public void shouldRetrieveArtifactandPOMURL(){
        String body = "{\n" +
                "\"results\": [\n" +
                "    {\n" +
                "        \"uri\": \"http://localhost:8080/artifactory/api/storage/libs-release-local/org/acme/artifact/1.0/artifact-1.0.jar\"\n" +
                "    },{\n" +
                "        \"uri\": \"http://localhost:8080/artifactory/api/storage/libs-release-local/org/acme/artifact/1.0/artifact-1.0.pom\"\n" +
                "    }\n" +
                "]\n" +
                "}";
        RepoResponse response = new RepoResponse(body, RepoResponse.ARTIFACTORY_GAVC_RESULT);
        ArtifactoryResponseHandler responseHandler = new ArtifactoryResponseHandler(response);
        assertThat(responseHandler.getArtifactURL(), is("http://localhost:8080/artifactory/api/storage/libs-release-local/org/acme/artifact/1.0/artifact-1.0.jar"));
        assertThat(responseHandler.getPOMurl(), is("http://localhost:8080/artifactory/api/storage/libs-release-local/org/acme/artifact/1.0/artifact-1.0.pom"));
    }
}
