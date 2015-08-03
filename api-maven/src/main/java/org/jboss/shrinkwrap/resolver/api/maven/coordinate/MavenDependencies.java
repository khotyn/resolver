/*
 * JBoss, Home of Professional Open Source
 * Copyright 2012, Red Hat Middleware LLC, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.shrinkwrap.resolver.api.maven.coordinate;

import java.lang.reflect.Constructor;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

import org.jboss.shrinkwrap.resolver.api.CoordinateParseException;
import org.jboss.shrinkwrap.resolver.api.maven.ScopeType;

/**
 * Factory class for creating new {@link MavenDependency} instances
 *
 * @author <a href="mailto:alr@jboss.org">Andrew Lee Rubinger</a>
 */
public final class MavenDependencies {

    private static final String NAME_IMPL_CLASS_NAME_KEY = "__shrinkwrap_maven_dependency_impl_class_name_key__";
    private static final String DEFAULT_NAME_IMPL_CLASS = "org.jboss.shrinkwrap.resolver.impl.maven.coordinate.MavenDependencyImpl";
    private static final Constructor<MavenDependency> ctor;
    static {
        try {
            ClassLoader classLoader = MavenDependencies.class.getClassLoader();

            if (classLoader == null) {
                classLoader = ClassLoader.getSystemClassLoader();
            }

            String namedImplClassName = System.getProperty(NAME_IMPL_CLASS_NAME_KEY);

            if(namedImplClassName == null || namedImplClassName.trim().length() == 0) {
                namedImplClassName = DEFAULT_NAME_IMPL_CLASS;
            }

            @SuppressWarnings("unchecked")
            final Class<MavenDependency> clazz = (Class<MavenDependency>) classLoader.loadClass(namedImplClassName);
            ctor = clazz.getConstructor(MavenCoordinate.class, ScopeType.class, boolean.class,
                MavenDependencyExclusion[].class);
        } catch (final Exception e) {
            throw new RuntimeException("Could not obtain constructor for " + MavenDependency.class.getSimpleName(), e);
        }
    }

    /**
     * No instances
     */
    private MavenDependencies() {
        throw new UnsupportedOperationException("No instances permitted");
    }

    /**
     * Creates a new {@link MavenDependency} instance from the specified, required canonical form in format
     * <code><groupId>:<artifactId>[:<packagingType>[:<classifier>]][:<version>]</code>, with the additional, optional
     * properties. If no {@link ScopeType} is specified, default will be {@link ScopeType#COMPILE}.
     *
     * @param canonicalForm
     * @param scope
     * @param optional
     * @param exclusions
     * @return
     * @throws IllegalArgumentException
     *             If the canonical form is not supplied
     * @throws CoordinateParseException
     *             If the specified canonical form is not valid
     */
    public static MavenDependency createDependency(final String canonicalForm, final ScopeType scope,
        final boolean optional, final MavenDependencyExclusion... exclusions) throws IllegalArgumentException,
        CoordinateParseException {
        if (canonicalForm == null || canonicalForm.length() == 0) {
            throw new IllegalArgumentException("canonical form is required");
        }
        final MavenCoordinate delegate = MavenCoordinates.createCoordinate(canonicalForm);
        return createDependency(delegate, scope, optional, exclusions);
    }

    /**
     * Creates a new {@link MavenDependency} instance from the specified properties. If no {@link ScopeType} is
     * specified, default will be {@link ScopeType#COMPILE}.
     *
     * @param coordinate
     * @param scope
     * @param optional
     * @param exclusions
     * @return
     * @throws IllegalArgumentException
     *             If the coordinate is not supplied
     * @throws CoordinateParseException
     *             If the specified canonical form is not valid
     */
    public static MavenDependency createDependency(final MavenCoordinate coordinate, final ScopeType scope,
        final boolean optional, final MavenDependencyExclusion... exclusions) throws IllegalArgumentException,
        CoordinateParseException {
        if (coordinate == null) {
            throw new IllegalArgumentException("coordinate form is required");
        }
        final MavenDependency dep = newInstance(coordinate, scope, optional, exclusions);
        return dep;
    }

    /**
     * Creates a new {@link MavenDependency} instance
     *
     * @param coordinate
     * @param scope
     * @param optional
     * @param exclusions
     * @return
     */
    private static MavenDependency newInstance(final MavenCoordinate coordinate, final ScopeType scope,
        final boolean optional, final MavenDependencyExclusion... exclusions) {
        assert coordinate != null : "coordinate must be specified";
        assert exclusions != null : "exclusions must be specified";
        try {
            return ctor.newInstance(coordinate, scope, optional, exclusions);
        } catch (final Exception e) {
            throw new RuntimeException("Could not create new " + MavenDependency.class.getSimpleName() + "instance", e);
        }
    }

    /**
     * Creates a new {@link MavenDependencyExclusion} instance from the specified, required canonical form in format
     * <code><groupId>:<artifactId></code>
     *
     * @param canonicalForm
     * @return
     * @throws IllegalArgumentException
     *             If the canonical form is not supplied
     * @throws CoordinateParseException
     *             If the canonical form is not in the correct format
     */
    public static MavenDependencyExclusion createExclusion(final String canonicalForm) throws IllegalArgumentException,
        CoordinateParseException {
        if (canonicalForm == null || canonicalForm.length() == 0) {
            throw new IllegalArgumentException("canonical form is required");
        }
        final StringTokenizer tokenizer = new StringTokenizer(canonicalForm,
            String.valueOf(MavenGABaseImpl.SEPARATOR_COORDINATE));
        final String groupId;
        final String artifactId;
        try {
            groupId = tokenizer.nextToken();
            artifactId = tokenizer.nextToken();
        } catch (final NoSuchElementException nsee) {
            // Exception translate
            throw new CoordinateParseException("Canonical form must be \"groupId:artifactId\"; got: " + canonicalForm);
        }
        // Ensure there isn't *more* defined than we need
        if (tokenizer.hasMoreTokens()) {
            throw new CoordinateParseException("Canonical form must be \"groupId:artifactId\"; got: " + canonicalForm);
        }
        final MavenDependencyExclusion exclusion = createExclusion(groupId, artifactId);
        return exclusion;
    }

    /**
     * Creates a new {@link MavenDependencyExclusion} instance from the specified, required arguments
     *
     * @param canonicalForm
     * @return
     * @throws IllegalArgumentException
     *             If either argument is not specified
     */
    public static MavenDependencyExclusion createExclusion(final String groupId, final String artifactId)
        throws IllegalArgumentException {
        if (groupId == null || groupId.length() == 0) {
            throw new IllegalArgumentException("groupId must be specified");
        }
        if (artifactId == null || artifactId.length() == 0) {
            throw new IllegalArgumentException("groupId must be specified");
        }
        final MavenDependencyExclusion exclusion = new MavenDependencyExclusionImpl(groupId, artifactId);
        return exclusion;
    }

}
