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

package org.kie.workbench.common.screens.datamodeller.client.widgets.advanceddomain;

import java.util.List;

import com.google.gwt.user.client.ui.IsWidget;
import org.kie.workbench.common.screens.datamodeller.client.widgets.advanceddomain.annotationlisteditor.AdvancedAnnotationListEditorAware;
import org.kie.workbench.common.services.datamodeller.core.Annotation;
import org.kie.workbench.common.services.datamodeller.core.ElementType;
import org.kie.workbench.common.services.shared.project.KieProject;

public interface AdvancedDataObjectEditorView
        extends IsWidget {

    interface Presenter extends AdvancedAnnotationListEditorAware {

    }

    void setPresenter( Presenter presenter );

    void init( KieProject project, ElementType elementType );

    void loadAnnotations( List<Annotation> annotations );

    void removeAnnotation( Annotation annotation );

    void clear();

}
