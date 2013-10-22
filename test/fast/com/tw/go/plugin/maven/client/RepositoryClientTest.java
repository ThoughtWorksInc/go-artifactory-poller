package com.tw.go.plugin.maven.client;

import com.thoughtworks.go.plugin.api.material.packagerepository.PackageRevision;
import com.tw.go.plugin.maven.artifactory.RepoResponse;
import com.tw.go.plugin.maven.config.LookupParams;
import com.tw.go.plugin.maven.artifactory.ArtifactoryResponseHandler;
import com.tw.go.plugin.util.HttpRepoURL;
import maven.MavenVersion;
import org.apache.commons.io.FileUtils;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static junit.framework.Assert.assertNull;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

public class RepositoryClientTest {
    @Test
    public void shouldGetVersionInfo() {
        LookupParams params = new LookupParams(new HttpRepoURL("http://localhost:8081/artifactory", "godev", "cru1s3"), "libs-release-local", "commons-logging", "commons-logging", null, null, null);
        MavenVersion version = new MavenVersion("1.1.3");
        RepositoryClient repositoryClient = new RepositoryClient(params);
        repositoryClient.setVersionInfoWrapper(version);
        PackageRevision packageRevision = version.toPackageRevision();
        assertThat(packageRevision.getTrackbackUrl(), is("http://commons.apache.org/proper/commons-logging/"));
        System.out.println(packageRevision.getTimestamp());
        System.out.println(packageRevision.getRevisionComment());
        System.out.println(packageRevision.getUser());
        System.out.println(packageRevision.getDataFor(LookupParams.PACKAGE_LOCATION));
    }

    @Test
    public void shouldGetLatestVersion() throws IOException {
        LookupParams lookupParams = new LookupParams(
                new HttpRepoURL("http://localhost:8081/artifactory", "godev", "cru1s3"),
                "libs-release-local", "commons-logging", "commons-logging", null, null, null);
        RepositoryClient client = new RepositoryClient(lookupParams);
        String responseBody = FileUtils.readFileToString(new File("test/fast/versions.json"));
        ArtifactoryResponseHandler artifactoryResponseHandler = new ArtifactoryResponseHandler(new RepoResponse(responseBody, RepoResponse.ARTIFACTORY_VERSIONS_RESULT));
        MavenVersion result = client.getLatest(artifactoryResponseHandler.getAllVersions());
        assertThat(result.getVersion(), is("1.2"));
        assertThat(result.getQualifier(), is(nullValue()));
    }

    @Test
    public void shouldReturnNullIfNoNewerVersion() throws IOException {
        PackageRevision previouslyKnownRevision = new PackageRevision("1.2", new Date(), "abc");
        previouslyKnownRevision.addData(LookupParams.PACKAGE_VERSION, "1.2");
        LookupParams lookupParams = new LookupParams(
                new HttpRepoURL("http://localhost:8081/artifactory", "godev", "cru1s3"),
                "libs-release-local", "commons-logging", "commons-logging", null, null, previouslyKnownRevision);
        RepositoryClient client = new RepositoryClient(lookupParams);
        String responseBody = FileUtils.readFileToString(new File("test/fast/versions.json"));
        ArtifactoryResponseHandler artifactoryResponseHandler = new ArtifactoryResponseHandler(new RepoResponse(responseBody, RepoResponse.ARTIFACTORY_VERSIONS_RESULT));
        assertNull(client.getLatest(artifactoryResponseHandler.getAllVersions()));
    }

    @Test
    public void shouldReturnNewerVersion() throws IOException {
        PackageRevision previouslyKnownRevision = new PackageRevision("1.1", new Date(), "abc");
        previouslyKnownRevision.addData(LookupParams.PACKAGE_VERSION, "1.1");
        LookupParams lookupParams = new LookupParams(
                new HttpRepoURL("http://localhost:8081/artifactory", "godev", "cru1s3"),
                "libs-release-local", "commons-logging", "commons-logging", null, null, previouslyKnownRevision);
        RepositoryClient client = new RepositoryClient(lookupParams);
        String responseBody = FileUtils.readFileToString(new File("test/fast/versions.json"));
        ArtifactoryResponseHandler artifactoryResponseHandler = new ArtifactoryResponseHandler(new RepoResponse(responseBody, RepoResponse.ARTIFACTORY_VERSIONS_RESULT));
        MavenVersion result = client.getLatest(artifactoryResponseHandler.getAllVersions());
        assertThat(result.getVersion(), is("1.2"));
    }

    @Test
    public void shouldHonorUpperBound() {
        PackageRevision previouslyKnownRevision = new PackageRevision("1.0.14", new Date(), "abc");
        previouslyKnownRevision.addData(LookupParams.PACKAGE_VERSION, "1.0.14");
        String upperBound = "1.0.17";
        LookupParams lookupParams = new LookupParams(
                new HttpRepoURL("http://localhost:8081/artifactory", "godev", "cru1s3"),
                "libs-release-local", "commons-logging", "commons-logging", null, upperBound, previouslyKnownRevision);
        RepositoryClient client = new RepositoryClient(lookupParams);
        List<MavenVersion> allVersions = new ArrayList<MavenVersion>();
        allVersions.add(new MavenVersion("1.0.18"));
        allVersions.add(new MavenVersion("1.0.16"));
        assertThat(client.getLatest(allVersions).getV_Q(), is("1.0.16"));
        allVersions.clear();
        allVersions.add(new MavenVersion("1.0.18"));
        assertNull(client.getLatest(allVersions));
    }

    @Test
    public void shouldHonorLowerBoundWithKnownPreviousVersion() {
        PackageRevision previouslyKnownRevision = new PackageRevision("1.0.14", new Date(), "abc");
        previouslyKnownRevision.addData(LookupParams.PACKAGE_VERSION, "1.0.14");
        String lowerBound = "0.1";
        LookupParams lookupParams = new LookupParams(
                new HttpRepoURL("http://localhost:8081/artifactory", "godev", "cru1s3"),
                "libs-release-local", "commons-logging", "commons-logging", lowerBound, null, previouslyKnownRevision);
        RepositoryClient client = new RepositoryClient(lookupParams);
        List<MavenVersion> allVersions = new ArrayList<MavenVersion>();
        allVersions.add(new MavenVersion("1.0.18"));
        allVersions.add(new MavenVersion("1.0.16"));
        assertThat(client.getLatest(allVersions).getV_Q(), is("1.0.18"));
        allVersions.clear();
        allVersions.add(new MavenVersion("1.0.12"));
        assertNull(client.getLatest(allVersions));
        allVersions.clear();
        allVersions.add(new MavenVersion("0.0.12"));
        assertNull(client.getLatest(allVersions));
    }

    @Test
    public void shouldHonorLowerBound() {
        String lowerBound = "0.1";
        LookupParams lookupParams = new LookupParams(
                new HttpRepoURL("http://localhost:8081/artifactory", "godev", "cru1s3"),
                "libs-release-local", "commons-logging", "commons-logging", lowerBound, null, null);
        RepositoryClient client = new RepositoryClient(lookupParams);
        List<MavenVersion> allVersions = new ArrayList<MavenVersion>();
        allVersions.add(new MavenVersion("1.0.18"));
        allVersions.add(new MavenVersion("1.0.16"));
        assertThat(client.getLatest(allVersions).getV_Q(), is("1.0.18"));
        allVersions.clear();
        allVersions.add(new MavenVersion("1.0.12"));
        assertThat(client.getLatest(allVersions).getV_Q(), is("1.0.12"));
        allVersions.clear();
        allVersions.add(new MavenVersion("0.0.12"));
        assertNull(client.getLatest(allVersions));
    }

    @Test
    public void shouldHonorUpperBoundAtQualifierLevel() {
        PackageRevision previouslyKnownRevision = new PackageRevision("1.0.0-14", new Date(), "abc");
        previouslyKnownRevision.addData(LookupParams.PACKAGE_VERSION, "1.0.0-14");
        String upperBound = "1.0.0-17";
        LookupParams lookupParams = new LookupParams(
                new HttpRepoURL("http://localhost:8081/artifactory", "godev", "cru1s3"),
                "libs-release-local", "commons-logging", "commons-logging", null, upperBound, previouslyKnownRevision);
        RepositoryClient client = new RepositoryClient(lookupParams);
        List<MavenVersion> allVersions = new ArrayList<MavenVersion>();
        allVersions.add(new MavenVersion("1.0.0-18"));
        allVersions.add(new MavenVersion("1.0.0-16"));
        assertThat(client.getLatest(allVersions).getV_Q(), is("1.0.0-16"));
        allVersions.clear();
        allVersions.add(new MavenVersion("1.0.0-18"));
        assertNull(client.getLatest(allVersions));
        allVersions.clear();
        allVersions.add(new MavenVersion("1.0.0-17"));
        assertNull(client.getLatest(allVersions));
        allVersions.clear();
        allVersions.add(new MavenVersion("1.0.0-16"));
        assertThat(client.getLatest(allVersions).getV_Q(), is("1.0.0-16"));
        allVersions.clear();
        allVersions.add(new MavenVersion("1.0.0-16"));
        allVersions.add(new MavenVersion("1.0.0-17"));
        assertThat(client.getLatest(allVersions).getV_Q(), is("1.0.0-16"));
        allVersions.clear();
        allVersions.add(new MavenVersion("1.0.0-15"));
        allVersions.add(new MavenVersion("1.0.0-16"));
        allVersions.add(new MavenVersion("1.0.0-17"));
        assertThat(client.getLatest(allVersions).getV_Q(), is("1.0.0-16"));
        allVersions.clear();
        allVersions.add(new MavenVersion("1.0.0-14"));
        allVersions.add(new MavenVersion("1.0.0-16"));
        allVersions.add(new MavenVersion("1.0.0-17"));
        assertThat(client.getLatest(allVersions).getV_Q(), is("1.0.0-16"));
        allVersions.clear();
        allVersions.add(new MavenVersion("1.0.0-14"));
        allVersions.add(new MavenVersion("1.0.0-17"));
        assertNull(client.getLatest(allVersions));
    }
}
