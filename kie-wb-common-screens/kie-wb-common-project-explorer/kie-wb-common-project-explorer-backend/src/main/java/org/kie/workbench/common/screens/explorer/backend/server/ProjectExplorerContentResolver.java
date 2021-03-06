/*
 * Copyright 2014 JBoss Inc
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

package org.kie.workbench.common.screens.explorer.backend.server;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;

import org.guvnor.common.services.backend.file.LinkedDotFileFilter;
import org.guvnor.common.services.project.model.Package;
import org.guvnor.common.services.project.model.Project;
import org.guvnor.structure.organizationalunit.OrganizationalUnit;
import org.guvnor.structure.organizationalunit.OrganizationalUnitService;
import org.guvnor.structure.repositories.Repository;
import org.guvnor.structure.repositories.impl.git.GitRepository;
import org.jboss.errai.security.shared.api.identity.User;
import org.kie.workbench.common.screens.explorer.model.FolderItem;
import org.kie.workbench.common.screens.explorer.model.FolderItemType;
import org.kie.workbench.common.screens.explorer.model.FolderListing;
import org.kie.workbench.common.screens.explorer.model.ProjectExplorerContent;
import org.kie.workbench.common.screens.explorer.service.Option;
import org.kie.workbench.common.screens.explorer.service.ProjectExplorerContentQuery;
import org.kie.workbench.common.screens.explorer.utils.Sorters;
import org.kie.workbench.common.services.shared.project.KieProjectService;
import org.uberfire.backend.server.util.Paths;
import org.uberfire.backend.vfs.Path;
import org.uberfire.java.nio.file.Files;
import org.uberfire.security.authz.AuthorizationManager;

import static java.util.Collections.*;
import static org.kie.workbench.common.screens.explorer.backend.server.ExplorerServiceHelper.*;

public class ProjectExplorerContentResolver {

    private LinkedDotFileFilter dotFileFilter = new LinkedDotFileFilter();

    private KieProjectService projectService;

    private ExplorerServiceHelper helper;

    private AuthorizationManager authorizationManager;

    private OrganizationalUnitService organizationalUnitService;

    @Inject
    protected User identity;

    @Inject
    public ProjectExplorerContentResolver( final KieProjectService projectService,
                                           final ExplorerServiceHelper helper,
                                           final AuthorizationManager authorizationManager,
                                           final OrganizationalUnitService organizationalUnitService ) {
        this.projectService = projectService;
        this.helper = helper;
        this.authorizationManager = authorizationManager;
        this.organizationalUnitService = organizationalUnitService;
    }

    public ProjectExplorerContent resolve( final ProjectExplorerContentQuery query ) {

        final Content content = setupSelectedItems( query );

        setSelectedOrganizationalUnit( content );
        setSelectedRepository( content );
        setSelectedProject( content );

        if ( content.getSelectedOrganizationalUnit() == null || content.getSelectedRepository() == null || content.getSelectedProject() == null ) {
            return emptyProjectExplorerContent( content );
        } else {
            return projectExplorerContentWithSelections( content, query.getOptions() );
        }
    }

    private ProjectExplorerContent projectExplorerContentWithSelections( final Content content,
                                                                         final Set<Option> options ) {

        setFolderListing( content, options );

        setSiblings( content );

        helper.store( content.getSelectedOrganizationalUnit(),
                      content.getSelectedRepository(),
                      content.getSelectedProject(),
                      content.getFolderListing(),
                      content.getSelectedPackage(),
                      options );

        return new ProjectExplorerContent(
                new TreeSet<OrganizationalUnit>( Sorters.ORGANIZATIONAL_UNIT_SORTER ) {{
                    addAll( content.getOrganizationalUnits() );
                }},
                content.getSelectedOrganizationalUnit(),
                new TreeSet<Repository>( Sorters.REPOSITORY_SORTER ) {{
                    addAll( content.getRepositories().values() );
                }},
                content.getSelectedRepository(),
                new TreeSet<Project>( Sorters.PROJECT_SORTER ) {{
                    addAll( content.getProjects().values() );
                }},
                content.getSelectedProject(),
                content.getFolderListing(),
                content.getSiblings()
        );
    }

    private void setFolderListing( final Content content,
                                   final Set<Option> options ) {
        content.setFolderListing( helper.getFolderListing( content.getSelectedItem(), content.getSelectedProject(), content.getSelectedPackage(), options ) );
    }

    private void setSiblings( final Content content ) {
        if ( content.getFolderListing().getSegments().size() > 1 ) {
            final ListIterator<FolderItem> li = content.getFolderListing().getSegments().listIterator( content.getFolderListing().getSegments().size() );
            while ( li.hasPrevious() ) {
                final FolderItem currentItem = li.previous();
                final List<FolderItem> result = new ArrayList<FolderItem>();
                result.add( currentItem );

                if ( currentItem.getItem() instanceof Package ) {
                    result.addAll( getSegmentSiblings( (Package) currentItem.getItem() ) );
                } else if ( currentItem.getItem() instanceof Path ) {
                    result.addAll( getSegmentSiblings( (Path) currentItem.getItem() ) );
                }
                content.getSiblings().put( currentItem, result );
            }
        }

        if ( content.getSelectedItem() != null && content.getSelectedItem().getType().equals( FolderItemType.FOLDER ) &&
                !content.getSiblings().containsKey( content.getSelectedItem() ) ) {
            final List<FolderItem> result = new ArrayList<FolderItem>();
            result.add( content.getSelectedItem() );

            if ( content.getSelectedItem().getItem() instanceof Package ) {
                result.addAll( getSegmentSiblings( (Package) content.getSelectedItem().getItem() ) );
            } else if ( content.getSelectedItem().getItem() instanceof Path ) {
                result.addAll( getSegmentSiblings( (Path) content.getSelectedItem().getItem() ) );
            }
            content.getSiblings().put( content.getSelectedItem(),
                                       result );
        }

        if ( content.getFolderListing().getItem().getType().equals( FolderItemType.FOLDER ) &&
                !content.getSiblings().containsKey( content.getFolderListing().getItem() ) ) {
            final List<FolderItem> result = new ArrayList<FolderItem>();
            result.add( content.getFolderListing().getItem() );

            if ( content.getFolderListing().getItem().getItem() instanceof Package ) {
                result.addAll( getSegmentSiblings( (Package) content.getFolderListing().getItem().getItem() ) );
            } else if ( content.getFolderListing().getItem().getItem() instanceof Path ) {
                result.addAll( getSegmentSiblings( (Path) content.getFolderListing().getItem().getItem() ) );
            }
            if ( !result.isEmpty() ) {
                content.getSiblings().put( content.getFolderListing().getItem(),
                                           result );
            }
        }

        //Sort sibling lists before returning to client
        for ( Map.Entry<FolderItem, List<FolderItem>> e : content.getSiblings().entrySet() ) {
            Collections.sort( e.getValue(),
                              Sorters.ITEM_SORTER );
        }
    }

    private ProjectExplorerContent emptyProjectExplorerContent( final Content content ) {
        return new ProjectExplorerContent(
                new TreeSet<OrganizationalUnit>( Sorters.ORGANIZATIONAL_UNIT_SORTER ) {{
                    addAll( content.getOrganizationalUnits() );
                }},
                content.getSelectedOrganizationalUnit(),
                new TreeSet<Repository>( Sorters.REPOSITORY_SORTER ) {{
                    addAll( content.getRepositories().values() );
                }},
                content.getSelectedRepository(),
                new TreeSet<Project>( Sorters.PROJECT_SORTER ) {{
                    addAll( content.getProjects().values() );
                }},
                content.getSelectedProject(),
                new FolderListing( null, Collections.<FolderItem>emptyList(), Collections.<FolderItem>emptyList() ),
                Collections.<FolderItem, List<FolderItem>>emptyMap()
        );
    }

    private void setSelectedProject( final Content content ) {
        content.setProjects( getProjects( content.getSelectedRepository() ) );

        if ( content.getSelectedProject() == null || !content.getProjects().containsKey( content.getSelectedProject().getProjectName() ) ) {
            content.setSelectedProject( ( content.getProjects().isEmpty() ? null : content.getProjects().values().iterator().next() ) );
        } else {
            content.setSelectedProject( content.getProjects().get( content.getSelectedProject().getProjectName() ) );
        }
    }

    private void setSelectedRepository( final Content content ) {
        content.setRepositories( getRepositories( content.getSelectedOrganizationalUnit() ) );
        if ( content.getSelectedRepository() == null || !content.getRepositories().containsKey( content.getSelectedRepository().getAlias() ) ) {
            content.setSelectedRepository( ( content.getRepositories().isEmpty() ? null : content.getRepositories().values().iterator().next() ) );
        } else if ( isCurrentRepositoryUpToDate( content ) ) {
            final String branch = content.getSelectedRepository().getCurrentBranch();
            content.setSelectedRepository( content.getRepositories().get( content.getSelectedRepository().getAlias() ) );
            if ( content.getSelectedRepository() instanceof GitRepository ) {
                ( (GitRepository) content.getSelectedRepository() ).changeBranch( branch );
            }
        }
    }

    private boolean isCurrentRepositoryUpToDate( final Content content ) {
        return !content.getSelectedRepository().equals( content.getRepositories().get( content.getSelectedRepository().getAlias() ) );
    }

    private void setSelectedOrganizationalUnit( final Content content ) {
        content.setOrganizationalUnits( getOrganizationalUnits() );
        if ( !content.getOrganizationalUnits().contains( content.getSelectedOrganizationalUnit() ) ) {
            content.setSelectedOrganizationalUnit( ( content.getOrganizationalUnits().isEmpty() ? null : content.getOrganizationalUnits().iterator().next() ) );
        }
    }

    private Content setupSelectedItems( ProjectExplorerContentQuery query ) {

        final Content content = new Content( query );

        final UserExplorerLastData lastContent = helper.getLastContent();
        final UserExplorerData userContent = helper.loadUserContent();

        if ( !lastContent.isDataEmpty() ) {
            if ( query.getOrganizationalUnit() == null && query.getRepository() == null && query.getProject() == null ) {
                if ( (query.getOptions().contains( Option.BUSINESS_CONTENT )
                        || query.getOptions().contains( Option.GROUPED_CONTENT ))
                        && lastContent.getLastPackage() != null ) {
                    content.setSelectedOrganizationalUnit( lastContent.getLastPackage().getOrganizationalUnit() );
                    content.setSelectedRepository( lastContent.getLastPackage().getRepository() );
                    content.setSelectedProject( lastContent.getLastPackage().getProject() );
                    content.setSelectedPackage( lastContent.getLastPackage().getPkg() );
                    content.setSelectedItem( null );
                } else if ( query.getOptions().contains( Option.TECHNICAL_CONTENT ) && lastContent.getLastFolderItem() != null ) {
                    content.setSelectedOrganizationalUnit( lastContent.getLastFolderItem().getOrganizationalUnit() );
                    content.setSelectedRepository( lastContent.getLastFolderItem().getRepository() );
                    content.setSelectedProject( lastContent.getLastFolderItem().getProject() );
                    content.setSelectedPackage( null );
                }
            } else if ( (query.getOptions().contains( Option.BUSINESS_CONTENT )
                    || query.getOptions().contains( Option.GROUPED_CONTENT ))
                    && lastContent.getLastPackage() != null ) {
                if ( !query.getOrganizationalUnit().equals( lastContent.getLastPackage().getOrganizationalUnit() ) ||
                        query.getRepository() != null && !query.getRepository().equals( lastContent.getLastPackage().getRepository() ) ||
                        query.getProject() != null && !query.getProject().equals( lastContent.getLastPackage().getProject() ) ) {
                    content.setSelectedOrganizationalUnit( loadOrganizationalUnit( query.getOrganizationalUnit(), userContent ) );
                    content.setSelectedRepository( loadRepository( content.getSelectedOrganizationalUnit(), query.getRepository(), userContent ) );
                    content.setSelectedProject( loadProject( content.getSelectedOrganizationalUnit(), content.getSelectedRepository(), query.getProject(), userContent ) );
                    content.setSelectedPackage( loadPackage( content.getSelectedOrganizationalUnit(), content.getSelectedRepository(), content.getSelectedProject(), query.getPkg(), userContent ) );
                    content.setSelectedItem( null );
                }
            } else if ( query.getOptions().contains( Option.TECHNICAL_CONTENT ) && lastContent.getLastFolderItem() != null ) {
                if ( !query.getOrganizationalUnit().equals( lastContent.getLastFolderItem().getOrganizationalUnit() ) ||
                        query.getRepository() != null && !query.getRepository().equals( lastContent.getLastFolderItem().getRepository() ) ||
                        query.getProject() != null && !query.getProject().equals( lastContent.getLastFolderItem().getProject() ) ) {
                    content.setSelectedOrganizationalUnit( loadOrganizationalUnit( query.getOrganizationalUnit(), userContent ) );
                    content.setSelectedRepository( loadRepository( content.getSelectedOrganizationalUnit(), query.getRepository(), userContent ) );
                    content.setSelectedProject( loadProject( content.getSelectedOrganizationalUnit(), content.getSelectedRepository(), query.getProject(), userContent ) );
                    content.setSelectedItem( loadFolderItem( content.getSelectedOrganizationalUnit(), content.getSelectedRepository(), content.getSelectedProject(), query.getItem(), userContent ) );
                    content.setSelectedPackage( null );
                }
            }
        }

        return content;
    }

    private List<FolderItem> getSegmentSiblings( final Path path ) {
        final List<FolderItem> result = new ArrayList<FolderItem>();
        org.uberfire.java.nio.file.Path nioParentPath = Paths.convert( path ).getParent();

        for ( org.uberfire.java.nio.file.Path sibling : Files.newDirectoryStream( nioParentPath, dotFileFilter ) ) {
            result.add( toFolderItem( sibling ) );
        }

        return result;
    }

    private List<FolderItem> getSegmentSiblings( final Package pkg ) {
        final List<FolderItem> result = new ArrayList<FolderItem>();
        final Package parentPkg = projectService.resolveParentPackage( pkg );
        if ( parentPkg == null ) {
            return emptyList();
        }
        final Set<Package> siblings = projectService.resolvePackages( parentPkg );
        if ( siblings != null && !siblings.isEmpty() ) {
            for ( final Package sibling : siblings ) {
                if ( !sibling.equals( pkg ) ) {
                    result.add( toFolderItem( sibling ) );
                }
            }
        }

        return result;
    }

    private OrganizationalUnit loadOrganizationalUnit( final OrganizationalUnit organizationalUnit,
                                                       final UserExplorerData content ) {
        if ( organizationalUnit != null ) {
            return organizationalUnit;
        }

        return content.getOrganizationalUnit();
    }

    private Repository loadRepository( final OrganizationalUnit organizationalUnit,
                                       final Repository repository,
                                       final UserExplorerData content ) {
        if ( organizationalUnit == null ) {
            return null;
        }
        if ( repository != null ) {
            return repository;
        }

        return content.get( organizationalUnit );
    }

    private Project loadProject( final OrganizationalUnit organizationalUnit,
                                 final Repository repository,
                                 final Project project,
                                 final UserExplorerData content ) {
        if ( repository == null ) {
            return null;
        }
        if ( project != null ) {
            return project;
        }

        return content.get( organizationalUnit, repository );
    }

    private Package loadPackage( final OrganizationalUnit organizationalUnit,
                                 final Repository repository,
                                 final Project project,
                                 final Package pkg,
                                 final UserExplorerData content ) {
        if ( project == null ) {
            return null;
        }

        if ( pkg != null ) {
            return pkg;
        }

        return content.getPackage( organizationalUnit, repository, project );
    }

    private FolderItem loadFolderItem( final OrganizationalUnit organizationalUnit,
                                       final Repository repository,
                                       final Project project,
                                       final FolderItem item,
                                       final UserExplorerData content ) {
        if ( project == null ) {
            return null;
        }

        if ( item != null ) {
            return item;
        }

        return content.getFolderItem( organizationalUnit, repository, project );
    }

    private Set<OrganizationalUnit> getOrganizationalUnits() {
        final Collection<OrganizationalUnit> organizationalUnits = organizationalUnitService.getOrganizationalUnits();
        final Set<OrganizationalUnit> authorizedOrganizationalUnits = new HashSet<OrganizationalUnit>();
        for ( OrganizationalUnit organizationalUnit : organizationalUnits ) {
            if ( authorizationManager.authorize( organizationalUnit,
                                                 identity ) ) {
                authorizedOrganizationalUnits.add( organizationalUnit );
            }
        }
        return authorizedOrganizationalUnits;
    }

    private Map<String, Repository> getRepositories( final OrganizationalUnit organizationalUnit ) {
        final Map<String, Repository> authorizedRepositories = new HashMap<String, Repository>();
        if ( organizationalUnit == null ) {
            return authorizedRepositories;
        }
        //Reload OrganizationalUnit as the organizational unit's repository list might have been changed server-side
        final Collection<Repository> repositories = organizationalUnitService.getOrganizationalUnit( organizationalUnit.getName() ).getRepositories();
        for ( Repository repository : repositories ) {
            if ( authorizationManager.authorize( repository,
                                                 identity ) ) {
                authorizedRepositories.put( repository.getAlias(), repository );
            }
        }
        return authorizedRepositories;
    }

    private Map<String, Project> getProjects( final Repository repository ) {
        final Map<String, Project> authorizedProjects = new HashMap<String, Project>();

        if ( repository == null ) {
            return authorizedProjects;
        } else {
            Set<Project> allProjects = projectService.getProjects( repository, repository.getCurrentBranch() );

            for ( Project project : allProjects ) {
                if ( authorizationManager.authorize( project,
                                                     identity ) ) {
                    authorizedProjects.put( project.getProjectName(), project );
                }
            }

            return authorizedProjects;
        }
    }
}
