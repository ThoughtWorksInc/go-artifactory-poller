package com.tw.go.plugin.maven.config;

import com.tw.go.plugin.util.HttpRepoURL;
import com.tw.go.plugin.util.RepoUrl;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class LookupParamsTest {
    //GET /api/search/gavc?g=org.acme&a=artifact&v=1.0&c=sources&repos=libs-release-local
    @Test
    public void shouldProvideFilesQueryURL(){
        LookupParams lookupParams = new LookupParams((HttpRepoURL)RepoUrl.create("http://artifactory",null,null),"repoA","grpB","artifactC", null,null,null);
        assertThat(lookupParams.getGAVCurl("2.1"), is("http://artifactory/api/search/gavc?g=grpB&a=artifactC&repos=repoA&v=2.1"));
    }
}
