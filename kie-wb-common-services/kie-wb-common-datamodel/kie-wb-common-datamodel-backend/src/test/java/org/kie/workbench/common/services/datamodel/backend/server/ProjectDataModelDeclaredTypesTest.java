/*
 * Copyright 2015 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package org.kie.workbench.common.services.datamodel.backend.server;

import java.net.URL;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;

import org.drools.workbench.models.datamodel.oracle.ProjectDataModelOracle;
import org.drools.workbench.models.datamodel.oracle.TypeSource;
import org.jboss.weld.environment.se.StartMain;
import org.junit.Before;
import org.junit.Test;
import org.kie.workbench.common.services.datamodel.backend.server.service.DataModelService;
import org.uberfire.backend.server.util.Paths;
import org.uberfire.backend.vfs.Path;
import org.uberfire.java.nio.fs.file.SimpleFileSystemProvider;

import static org.junit.Assert.*;
import static org.kie.workbench.common.services.datamodel.backend.server.ProjectDataModelOracleTestUtils.*;

/**
 * Tests for DataModelService
 */
public class ProjectDataModelDeclaredTypesTest {

    private final SimpleFileSystemProvider fs = new SimpleFileSystemProvider();
    private BeanManager beanManager;
    private Paths paths;

    @Before
    public void setUp() throws Exception {
        //Bootstrap WELD container
        StartMain startMain = new StartMain( new String[ 0 ] );
        beanManager = startMain.go().getBeanManager();

        //Instantiate Paths used in tests for Path conversion
        final Bean pathsBean = (Bean) beanManager.getBeans( Paths.class ).iterator().next();
        final CreationalContext cc = beanManager.createCreationalContext( pathsBean );
        paths = (Paths) beanManager.getReference( pathsBean,
                                                  Paths.class,
                                                  cc );

        //Ensure URLs use the default:// scheme
        fs.forceAsDefault();
    }

    @Test
    public void testProjectDeclaredTypes() throws Exception {
        final Bean dataModelServiceBean = (Bean) beanManager.getBeans( DataModelService.class ).iterator().next();
        final CreationalContext cc = beanManager.createCreationalContext( dataModelServiceBean );
        final DataModelService dataModelService = (DataModelService) beanManager.getReference( dataModelServiceBean,
                                                                                               DataModelService.class,
                                                                                               cc );

        final URL packageUrl = this.getClass().getResource( "/DataModelBackendDeclaredTypesTest1/src/main/java/t1p1" );
        final org.uberfire.java.nio.file.Path nioPackagePath = fs.getPath( packageUrl.toURI() );
        final Path packagePath = paths.convert( nioPackagePath );

        final ProjectDataModelOracle oracle = dataModelService.getProjectDataModel( packagePath );

        assertNotNull( oracle );

        assertEquals( 4,
                      oracle.getProjectModelFields().size() );
        assertContains( "t1p1.Bean1",
                        oracle.getProjectModelFields().keySet() );
        assertContains( "t1p1.DRLBean",
                        oracle.getProjectModelFields().keySet() );
        assertContains( "t1p2.Bean2",
                        oracle.getProjectModelFields().keySet() );
        assertContains( "java.lang.String",
                        oracle.getProjectModelFields().keySet() );

        assertEquals( TypeSource.JAVA_PROJECT,
                      oracle.getProjectTypeSources().get( "t1p1.Bean1" ) );
        assertEquals( TypeSource.DECLARED,
                      oracle.getProjectTypeSources().get( "t1p1.DRLBean" ) );
        assertEquals( TypeSource.JAVA_PROJECT,
                      oracle.getProjectTypeSources().get( "t1p2.Bean2" ) );
        assertEquals( TypeSource.JAVA_PROJECT,
                      oracle.getProjectTypeSources().get( "java.lang.String" ) );
    }

}
