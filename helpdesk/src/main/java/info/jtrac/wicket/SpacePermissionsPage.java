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

import info.jtrac.domain.Category;
import info.jtrac.domain.Field;
import info.jtrac.domain.Role;
import info.jtrac.domain.Severity;
import info.jtrac.domain.Space;
import info.jtrac.domain.SpaceCategory;
import info.jtrac.domain.SpaceSeverityPeriod;
import info.jtrac.domain.State;
import info.jtrac.domain.WorkflowRenderer;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.behavior.SimpleAttributeModifier;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.PropertyModel;

/**
 * space roles and workflow form
 */
public class SpacePermissionsPage extends BasePage {
      
    private WebPage previous;
    private Space space;     
    
    public SpacePermissionsPage(Space space, WebPage previous) {
        this.space = space;
        this.previous = previous;
        add(new SpacePermissionsForm("form"));
    }
    
    /**
     * wicket form
     */     
    @SuppressWarnings("serial")
	private class SpacePermissionsForm extends Form {               
        
    	private List<TextField> textFieldsPeriods;
    	private List<Severity> severities;
    	private List<SpaceSeverityPeriod> spaceSeverityPeriods;
    	private SpaceSeverityPeriod spaceSeverityPeriod;
    	private int spaceSeverityPeriodIndex;
    	private TextField textFieldPeriod;
    	
    	private List<Category> selectedCategories = new ArrayList<Category>();
    	
    	// Space Category vars ======================
    	
    	private CheckBox assign_category_yn;
		private List<Category> categories = new ArrayList<Category>();
		private List<SpaceCategory> spaceCategories = new ArrayList<SpaceCategory>();
		
		private HashMap<Category, Boolean> selection = new HashMap<Category, Boolean>(); 
    	// ==========================================
    	

		public SpacePermissionsForm(String id) {
            super(id);
            
            textFieldsPeriods = new ArrayList<TextField>();
            severities = new ArrayList<Severity>();
            spaceSeverityPeriods = new ArrayList<SpaceSeverityPeriod>();
            spaceSeverityPeriod = new SpaceSeverityPeriod();
            spaceSeverityPeriodIndex = 0;
            
            // label / heading =================================================
            add(new Label("label", space.getName() + " (" + space.getPrefixCode() + ")"));
            // states colspan ==================================================
            final Map<Integer, String> statesMap = space.getMetadata().getStatesMap();
            SimpleAttributeModifier statesColspan = new SimpleAttributeModifier("colspan", (statesMap.size() - 1) + "");
            add(new WebMarkupContainer("statesColspan").add(statesColspan));
            // fields colspan ==================================================
            final List<Field> fields = space.getMetadata().getFieldList();
            SimpleAttributeModifier fieldsColspan = new SimpleAttributeModifier("colspan", fields.size() + "");
            add(new WebMarkupContainer("fieldsColspan").add(fieldsColspan));
            // add state =======================================================
            add(new Button("addState") {
                @Override
                public void onSubmit() {
                    setResponsePage(new SpaceStatePage(space, -1, previous));
                }
            }); 
            // add state =======================================================
            add(new Button("addRole") {
                @Override
                public void onSubmit() {
                    setResponsePage(new SpaceRolePage(space, null, previous));
                }
            });
            // states col headings =============================================
            final List<Integer> stateKeysNoNew = new ArrayList(statesMap.keySet());
            stateKeysNoNew.remove(State.NEW);
            add(new ListView("stateHeads", stateKeysNoNew) {
                protected void populateItem(ListItem listItem) {
                    Integer stateKey = (Integer) listItem.getModelObject();
                    listItem.add(new Label("state", statesMap.get(stateKey)));
                }
            });
            // fields col headings =============================================
            add(new ListView("fieldHeads", fields) {
                protected void populateItem(ListItem listItem) {
                    Field f = (Field) listItem.getModelObject();
                    listItem.add(new Label("field", f.getLabel()));                    
                }
            });
            
            severities = getJtrac().findAllSeverities();
            spaceSeverityPeriods = getJtrac().findSpaceSeverityPeriodBySpaceId(space.getId());
            
            if(spaceSeverityPeriods.isEmpty() == false) {
            	spaceSeverityPeriod = spaceSeverityPeriods.get(0);
            }            
            
            
            add(new ListView("severities", severities) {
				
				@Override
				protected void populateItem(ListItem listItem) {
					
					Severity severity = (Severity) listItem.getModelObject();
                    listItem.add(new Label("description", new PropertyModel(severity, "description")));
                    
                    if(spaceSeverityPeriodIndex >= spaceSeverityPeriods.size()) {
                    	textFieldPeriod = new TextField("period", new PropertyModel(new SpaceSeverityPeriod(), "period"));
                    } else {
                    	textFieldPeriod = new TextField("period", new PropertyModel(spaceSeverityPeriods.get(spaceSeverityPeriodIndex), "period"));
                    }
                    
                    textFieldPeriod.setRequired(true);
                    textFieldPeriod.add(new ErrorHighlighter());
                    textFieldPeriod.setType(Integer.class);
                    
                    listItem.add(textFieldPeriod);
                    
                    textFieldsPeriods.add(textFieldPeriod);
                    spaceSeverityPeriodIndex++;
				}
			});
            
            // Space Categories ===============================================
            
            
			spaceCategories = getJtrac().findSpaceCategoriesBySpace(space);
			
			categories = getJtrac().findAllCategories();
			
			/*List<String> columnHeadings = new ArrayList<String>();
			columnHeadings.add("Category");
			columnHeadings.add("Category Applicable to Space?");

			ListView headings = new ListView("headings", columnHeadings) {
	            protected void populateItem(ListItem listItem) {
	                listItem.add(new Label("heading", listItem.getModelObjectAsString()));
	            }
	        };
	        
	        add(headings);*/
	        
	        class CheckBoxModel implements Serializable {
	        	
	        	private boolean isChecked;
	        	private Category category;
	        	
				public CheckBoxModel(Space space, Category category) {
	        		List<SpaceCategory> spaceCategories = getJtrac().findSpaceCategoriesBySpace(space);
	        		
	        		for(SpaceCategory spaceCategory : spaceCategories) {
	        			if(spaceCategory.getCategory().getId() == category.getId()) {
	        				setChecked(true);
	        				setCategory(spaceCategory.getCategory());
	        				break;
			        	} else {
			        		setChecked(false);
			        	}
	        		}
	        	}
	        	
				public boolean isChecked() {
					return isChecked;
				}

				public void setChecked(boolean isChecked) {
					this.isChecked = isChecked;
				}

				public Category getCategory() {
					return category;
				}

				public void setCategory(Category category) {
					this.category = category;
				}

	        }
	        
	        add(new ListView("listview", categories) {
	        	
			    protected void populateItem(ListItem item) {
			        final Category category = (Category) item.getModelObject();
			        
			        item.add(new Label("category", new PropertyModel(category, "description")));
			        
			        final CheckBoxModel checkBoxModel = new CheckBoxModel(space, category);
			        assign_category_yn = new CheckBox("assign_category_yn", new PropertyModel(checkBoxModel, "isChecked"));
			        
			        assign_category_yn.add(new AjaxFormComponentUpdatingBehavior("onchange") { 
                        
			        	protected void onUpdate(AjaxRequestTarget target) { 
			        		selection.put(category, checkBoxModel.isChecked());
	                    } 
			        }); 
			        
			        item.add(assign_category_yn);
			    }
			});
	        
	        // =================================================================
            
            // rows init =======================================================
            List<Integer> stateKeys = new ArrayList(statesMap.keySet());
            final List<Role> roles = new ArrayList(space.getMetadata().getRoleList());
            final SimpleAttributeModifier rowspan = new SimpleAttributeModifier("rowspan", roles.size() + "");
            final SimpleAttributeModifier yes = new SimpleAttributeModifier("src", "../resources/status-green.gif");
            final SimpleAttributeModifier no = new SimpleAttributeModifier("src", "../resources/status-grey.gif");            
            final SimpleAttributeModifier readonly = new SimpleAttributeModifier("src", "../resources/field-readonly.gif");
            final SimpleAttributeModifier mandatory = new SimpleAttributeModifier("src", "../resources/field-mandatory.gif");            
            final SimpleAttributeModifier optional = new SimpleAttributeModifier("src", "../resources/field-optional.gif");
            final SimpleAttributeModifier hidden = new SimpleAttributeModifier("src", "../resources/field-hidden.gif");
            final SimpleAttributeModifier altClass = new SimpleAttributeModifier("class", "alt");
            //==================================================================
            
            add(new ListView("states", stateKeys) {               
                protected void populateItem(ListItem listItem) {
                    final boolean firstState = listItem.getIndex() == 0;
                    final String stateClass = listItem.getIndex() % 2 == 1 ? "bdr-bottom alt" : "bdr-bottom";
                    final Integer stateKeyRow = (Integer) listItem.getModelObject();
                    listItem.add(new ListView("roles", roles) {
                        protected void populateItem(ListItem listItem) {
                            String roleClass = listItem.getIndex() % 2 == 1 ? " alt" : "";
                            String lastRole = listItem.getIndex() == roles.size() - 1 ? " bdr-bottom" : "";
                            listItem.add(new SimpleAttributeModifier("class", "center" + roleClass + lastRole));                            
                            final Role role = (Role) listItem.getModelObject();
                            if(listItem.getIndex() == 0) {
                                SimpleAttributeModifier rowClass = new SimpleAttributeModifier("class", stateClass);
                                listItem.add(new Label("state", statesMap.get(stateKeyRow)).add(rowspan).add(rowClass));
                                WebMarkupContainer editState = new WebMarkupContainer("editState");
                                editState.add(rowspan).add(rowClass);
                                Button editStateButton = new Button("editState") {
                                    @Override
                                    public void onSubmit() {
                                        setResponsePage(new SpaceStatePage(space, stateKeyRow, previous));
                                    }                                    
                                };
                                editState.add(editStateButton);
                                if(stateKeyRow == State.NEW) { // user can customize state names, even for Closed
                                    editStateButton.setVisible(false);
                                }
                                listItem.add(editState);
                            } else {
                                listItem.add(new WebMarkupContainer("state").setVisible(false));
                                listItem.add(new WebMarkupContainer("editState").setVisible(false));
                            }
                            listItem.add(new Label("role", role.getName()));
                            Button editRoleButton = new Button("editRole") {
                                @Override
                                public void onSubmit() {
                                    setResponsePage(new SpaceRolePage(space, role.getName(), previous));
                                }                                
                            };
                            listItem.add(editRoleButton);
                            if(!firstState) {
                                editRoleButton.setVisible(false);
                            }
                            listItem.add(new ListView("stateHeads", stateKeysNoNew) {
                                protected void populateItem(ListItem listItem) {
                                    final Integer stateKeyCol = (Integer) listItem.getModelObject();
                                    Button stateButton = new Button("state") {
                                        @Override
                                        public void onSubmit() {
                                            space.getMetadata().toggleTransition(role.getName(), stateKeyRow, stateKeyCol);
                                            setResponsePage(new SpacePermissionsPage(space, previous));
                                        }                                          
                                    };
                                    if(stateKeyRow == State.NEW && stateKeyCol != State.OPEN) {
                                        stateButton.setVisible(false);
                                    }
                                    State state = role.getStates().get(stateKeyRow);
                                    if(state != null && state.getTransitions().contains(stateKeyCol)) {
                                        stateButton.add(yes);                                        
                                    } else {
                                        stateButton.add(no);
                                    }
                                    listItem.add(stateButton);                                    
                                }                                
                            });
                            listItem.add(new ListView("fieldHeads", fields) {
                                protected void populateItem(ListItem listItem) {
                                    if(roles.size() == 1 && listItem.getIndex() % 2 == 0) {
                                        listItem.add(altClass);
                                    }
                                    final Field field = (Field) listItem.getModelObject();
                                    Button fieldButton = new Button("field") {
                                        @Override
                                        public void onSubmit() {
                                            space.getMetadata().switchMask(stateKeyRow, role.getName(), field.getName().getText());
                                            setResponsePage(new SpacePermissionsPage(space, previous));
                                        }                                          
                                    };
                                   /* State state = role.getStates().get(stateKeyRow);                                    
                                    int mask = state.getFields().get(field.getName());
                                    switch(mask) {
                                        case State.MASK_MANDATORY : fieldButton.add(mandatory); break;
                                        case State.MASK_OPTIONAL : fieldButton.add(optional); break;
                                        case State.MASK_READONLY : fieldButton.add(readonly); break;
                                        case State.MASK_HIDDEN : fieldButton.add(hidden); break;
                                        default: // should never happen
                                    }
                                    listItem.add(fieldButton);       */                          
                                }                                
                            });                            
                        }                        
                    });
                }                
            });
            // back ============================================================
            add(new Button("back") {
                @Override
                public void onSubmit() {
                    setResponsePage(new SpaceFormPage(space));
                }
            });            
            // save ============================================================
            add(new Button("save") {
                @Override
                public void onSubmit() {
                	
                    boolean isNewSpace = space.getId() == 0;
                    getJtrac().storeSpace(space);
                    
	                for(int i=0; i<severities.size(); i++) {
	                	SpaceSeverityPeriod spaceSevPeriod = new SpaceSeverityPeriod();
	                	spaceSevPeriod.setPeriod(Integer.parseInt(textFieldsPeriods.get(i).getModelObjectAsString()));
	                	spaceSevPeriod.setSeverity(severities.get(i));
	                	spaceSevPeriod.setSpace(space);
	                	
	                    if(spaceSeverityPeriods.isEmpty() == false && i < spaceSeverityPeriods.size()) {
	                    	spaceSevPeriod.setId(spaceSeverityPeriods.get(i).getId());
	                    }
	                   	
	                   	getJtrac().storeSpaceSeverityPeriod(spaceSevPeriod);
	                }
	                
	             // Remove all SpaceCategories and Re-Save them
	                getJtrac().removeSpaceCategory(space);
	                for(Category category : selection.keySet()) {
	                	if(selection.get(category)) {
	                		SpaceCategory spaceCategory = new SpaceCategory();
	                		spaceCategory.setCategory(category);
	                		spaceCategory.setSpace(space);
		                	getJtrac().storeSpaceCategory(spaceCategory);
	                	}
	                }
	                
	                
	                /*if(isNewSpace) {
		                
		                List<Category> categories = new ArrayList<Category>();
		                
		                categories = getJtrac().findAllCategories();
		                
		                for(Category category : categories) {
		                	SpaceCategory spaceCategory = new SpaceCategory();
			                spaceCategory.setCategory(category);
			                spaceCategory.setSpace(space);
			                	
			                getJtrac().storeSpaceCategory(spaceCategory);
		                }
		            }*/
	                
                    // current user may be allocated to this space, and e.g. name could have changed
                    JtracSession.get().refreshPrincipal();
                    if(isNewSpace) {
                        setResponsePage(new SpaceAllocatePage(space.getId(), previous));
                    } else if(previous == null) {
                        setResponsePage(new OptionsPage());
                    } else {                        
                        if (previous instanceof SpaceListPage) {
                            ((SpaceListPage) previous).setSelectedSpaceId(space.getId());
                        }                      
                        setResponsePage(previous);
                    }                    
                }
            });
            // cancel ==========================================================
            add(new Link("cancel") {
                public void onClick() {
                    setResponsePage(previous);
                }                
            });
            //WorkflowRenderer workflow = new WorkflowRenderer(space.getMetadata().getRolesMap(), space.getMetadata().getStatesMap());
            //add(new Label("workflow", workflow.getAsHtml()).setEscapeModelStrings(false));
        }  
		
		@Override
        protected void validate() {
            super.validate();          
        }  
                
    }                
    
}
