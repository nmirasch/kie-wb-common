/*
 * Copyright 2005 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.workbench.common.widgets.metadata.client.widget;

import com.github.gwtbootstrap.client.ui.Button;
import com.github.gwtbootstrap.client.ui.ButtonGroup;
import com.github.gwtbootstrap.client.ui.TextBox;
import com.github.gwtbootstrap.client.ui.constants.ButtonType;
import com.github.gwtbootstrap.client.ui.constants.IconType;
import com.github.gwtbootstrap.client.ui.resources.ButtonSize;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Widget;
import org.guvnor.common.services.shared.metadata.model.Metadata;
import org.kie.workbench.common.widgets.metadata.client.resources.Images;
import org.kie.workbench.common.widgets.metadata.client.resources.i18n.MetadataConstants;
import org.uberfire.ext.widgets.common.client.common.SmallLabel;
import javax.inject.Inject;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import org.guvnor.common.services.shared.metadata.model.Metadata;
import org.jboss.errai.ioc.client.container.IOC;
import org.jboss.errai.ioc.client.container.IOCBeanDef;

/**
 * This is a viewer/selector for tags.
 * It will show a list of tags currently applicable, and allow you to
 * remove/add to them.
 * <p/>
 * It is intended to work with the meta data form.
 */
public class TagWidget implements IsWidget {

    private Metadata data;

    private TagWidgetView view;

    private boolean readOnly;

    @Inject
    public void setView( TagWidgetView view ) {
        this.view = view;
        view.setPresenter( this );
    }

    /**
     * @param d The meta data.
     * @param readOnly If it is to be non editable.
     */
    public void setContent( Metadata d,
                            boolean readOnly ) {
        this.data = d;

        this.readOnly = readOnly;

        view.setReadOnly(readOnly);

        loadData();
    }

    public void onAddTags( String text ) {
        if (text != null) {
            String[] tags = text.split( " " );
            for (String tag : tags) {
                if (!data.getTags().contains( tag )) {
                    data.addTag( tag );
                    view.addTag( tag, readOnly );
                }
            }
        }
    }

    public void onRemoveTag( String tag ) {
        data.getTags().remove( tag );
        loadData();
    }

    public void loadData( ) {
        view.clear();
        for (String tag : data.getTags()) {
            view.addTag( tag, readOnly );
        }
    }

    @Override
    public Widget asWidget() {
        if (view == null) initView();
        return view.asWidget();
    }

    // TODO: remove this method when the MetaDataWidget is moved to MVP
    private void initView() {
        setView( new TagWidgetViewImpl() );
    }
}
