/*
 * Copyright 2002-2005 the original author or authors.
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

package info.jtrac.wicket;

import info.jtrac.domain.User;
import org.apache.wicket.markup.html.link.Link;

/**
 * options menu page
 */
public class OptionsPage extends BasePage {
      
    public OptionsPage() {                  
        
        setVersioned(false);                      
        
        final User user = getPrincipal();
        
        add(new Link("profile") {
            public void onClick() {
                UserFormPage page = new UserFormPage(user);
                page.setPrevious(OptionsPage.this);
                setResponsePage(page);
            }            
        });
        
        user.isSuperUser();
        
        boolean isSuperUser = user.isSuperUser();
        boolean isSpaceAdmin = user.getSpacesWhereRoleIsAdmin().size() > 0;
        
        add(new Link("users") {
            public void onClick() {
                setResponsePage(new UserListPage());
            }            
        }.setVisible(isSuperUser || isSpaceAdmin)); 
        
        add(new Link("spaces") {
            public void onClick() {
                setResponsePage(new SpaceListPage());//SpaceFormPage
            }            
        }.setVisible(isSuperUser || isSpaceAdmin));      
        
        add(new Link("settings") {
            public void onClick() {
                setResponsePage(new ConfigListPage(null));
            }            
        }.setVisible(isSuperUser));        
        
        add(new Link("severities") {
            public void onClick() {
                setResponsePage(new SeveritiesListPage());
            }            
        }.setVisible(false));
        add(new Link("levels") {
            public void onClick() {
                setResponsePage(new LevelPage());
            }            
        }.setVisible(false));
        add(new Link("categories") {
            public void onClick() {
                setResponsePage(new CategoryManagementForm());
            }            
        }.setVisible(isSuperUser));
        add(new Link("reports") {
            public void onClick() {
            	setResponsePage(new ReportPage());
            }            
        }.setVisible(isSpaceAdmin || isSuperUser));
        
        add(new Link("reporting") {
            public void onClick() {
            	setResponsePage(new ReportPage());
            }            
        }.setVisible(isSpaceAdmin || isSuperUser));
        
        add(new Link("indexes") {
            public void onClick() {
                setResponsePage(new IndexRebuildPage(false));
            }            
        }.setVisible(isSuperUser));        
        
        // for the future
        add(new Link("import") {
            public void onClick() {
                setResponsePage(new ExcelImportPage());
            }            
        }.setVisible(isSuperUser));                 
        
    }
    
}
