/*
 * Copyright 2015 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.workbench.common.screens.datamodeller.client.widgets.common.domain;

import java.util.List;
import javax.enterprise.event.Event;
import javax.inject.Inject;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.IsWidget;
import org.kie.workbench.common.screens.datamodeller.client.DataModelerContext;
import org.kie.workbench.common.screens.datamodeller.client.command.DataModelCommand;
import org.kie.workbench.common.screens.datamodeller.client.command.DataModelCommandBuilder;
import org.kie.workbench.common.screens.datamodeller.client.handlers.DomainHandler;
import org.kie.workbench.common.screens.datamodeller.client.handlers.DomainHandlerRegistry;
import org.kie.workbench.common.screens.datamodeller.events.DataModelerEvent;
import org.kie.workbench.common.screens.datamodeller.events.DataModelerValueChangeEvent;

public abstract class BaseEditor extends Composite {

    protected DataModelerContext context;

    protected boolean readonly = false;

    @Inject
    protected Event<DataModelerEvent> dataModelerEvent;

    @Inject
    protected DomainHandlerRegistry handlerRegistry;

    @Inject
    protected DataModelCommandBuilder commandBuilder;

    protected BaseEditor() {
    }

    public DataModelerContext getContext() {
        return context;
    }

    public void setContext( DataModelerContext context ) {
        this.context = context;
    }

    public void setReadonly( boolean readonly ) {
        this.readonly = readonly;
    }

    public boolean isReadonly() {
        return readonly;
    }

    public abstract void clean();

    public abstract String getName();

    public abstract String getDomainName();

    protected void executePostCommandProcessing( DataModelCommand command ) {
        List<DomainHandler> handlers = handlerRegistry.getDomainHandlers( getDomainName() );
        for ( DomainHandler handler : handlers ) {
            handler.postCommandProcessing( command );
        }
    }

    protected void notifyChange( DataModelerEvent event ) {
        dataModelerEvent.fire( event );
        //TODO, check if the helper is still needed.
        // Notify helper directly
        getContext().getHelper().dataModelChanged( ( DataModelerValueChangeEvent)event );
    }
}
