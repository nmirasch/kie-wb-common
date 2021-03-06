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

package org.kie.workbench.common.screens.server.management.client;

import org.junit.Test;
import org.uberfire.client.workbench.panels.impl.SimpleWorkbenchPanelPresenter;
import org.uberfire.mvp.impl.DefaultPlaceRequest;
import org.uberfire.workbench.model.PartDefinition;
import org.uberfire.workbench.model.PerspectiveDefinition;

import static org.junit.Assert.*;

public class ServerManagementPerspectiveTest {

    @Test
    public void checkPerspectiveDefinition() {
        final ServerManagementPerspective perspective = new ServerManagementPerspective();
        final PerspectiveDefinition definition = perspective.buildPerspective();

        assertNotNull( definition );

        assertEquals( "ServerManagementPerspective", definition.getName() );
        assertEquals( SimpleWorkbenchPanelPresenter.class.getName(), definition.getRoot().getPanelType() );

        assertEquals( 1, definition.getRoot().getParts().size() );

        final PartDefinition partDefinition = definition.getRoot().getParts().iterator().next();

        assertTrue( partDefinition.getPlace() instanceof DefaultPlaceRequest );

        assertEquals( "ServerManagementBrowser", partDefinition.getPlace().getIdentifier() );
    }
}
