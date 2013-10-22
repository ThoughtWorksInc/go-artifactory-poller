package com.tw.go.plugin.maven.apimpl;


import com.thoughtworks.go.plugin.api.material.packagerepository.PackageMaterialConfiguration;
import com.thoughtworks.go.plugin.api.material.packagerepository.PackageMaterialPoller;
import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;

public class MavenProviderTest {
    @Test
    public void shouldGetMavenRepositoryConfig() {
        MavenProvider repositoryMaterial = new MavenProvider();
        PackageMaterialConfiguration repositoryConfiguration = repositoryMaterial.getConfig();
        assertThat(repositoryConfiguration, is(notNullValue()));
        assertThat(repositoryConfiguration, instanceOf(PluginConfig.class));
    }

    @Test
    public void shouldGetMavenRepositoryPoller() {
        MavenProvider repositoryMaterial = new MavenProvider();
        PackageMaterialPoller poller = repositoryMaterial.getPoller();
        assertThat(poller, is(notNullValue()));
        assertThat(poller, instanceOf(ArtifactoryPoller.class));
    }
}
