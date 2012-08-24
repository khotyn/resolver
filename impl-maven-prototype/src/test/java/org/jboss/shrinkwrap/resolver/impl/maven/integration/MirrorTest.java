package org.jboss.shrinkwrap.resolver.impl.maven.integration;

import java.io.File;

import junit.framework.Assert;

import org.jboss.shrinkwrap.resolver.api.Resolvers;
import org.jboss.shrinkwrap.resolver.api.maven.MavenResolverSystem;
import org.junit.Test;

/**
 * Tests mirror setting in Maven
 *
 * @author <a href="mailto:kpiwko@redhat.com">Karel Piwko</a>
 *
 */
public class MirrorTest {

    /**
     * Tests a resolution of an artifact a repository specified in settings.xml within activeProfiles mirrored
     *
     */
    @Test
    public void enabledMirror() {
        File file = Resolvers.use(MavenResolverSystem.class)
            .configureSettings("target/settings/profiles/settings-mirror.xml")
            .resolve("org.jboss.shrinkwrap.test:test-deps-c:1.0.0").withoutTransitivity().asSingle(File.class);

        Assert.assertEquals("The file is packaged as test-deps-c-1.0.0.jar", "test-deps-c-1.0.0.jar", file.getName());
    }

}
