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
import org.apache.wicket.extensions.yui.calendar.DatePicker;
import org.apache.wicket.extensions.yui.calendar.DateTimeField;
import info.jtrac.domain.Category;
import info.jtrac.domain.Item;
import info.jtrac.domain.ItemSearch;
import info.jtrac.domain.ItemUser;
import info.jtrac.domain.Severity;
import info.jtrac.domain.Space;
import info.jtrac.domain.SpaceCategory;
import info.jtrac.domain.SpaceSeverityPeriod;
import info.jtrac.domain.State;
import info.jtrac.domain.User;
import info.jtrac.domain.UserSpaceRole;
import info.jtrac.util.UserUtils;
import info.jtrac.wicket.yui.YuiCalendar;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.commons.codec.binary.Base64;
import org.apache.wicket.PageParameters;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.behavior.HeaderContributor;
import org.apache.wicket.datetime.StyleDateConverter;
import org.apache.wicket.extensions.markup.html.form.DateTextField;
import org.apache.wicket.markup.html.IHeaderContributor;
import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.markup.html.form.ListMultipleChoice;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.apache.wicket.markup.html.form.upload.FileUploadField;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.BoundCompoundPropertyModel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.util.lang.Bytes;

import com.google.code.jqwicket.ui.datepicker.DatePickerTextField;
//import com.googlecode.wicket.jquery.ui.form.button.Button;


/**
 * Create / Edit item form page
 */
public class ItemFormPage extends BasePage {
    /**
     * Default constructor
     */
    public ItemFormPage() {
        Item item = new Item();
        item.setSpace(getCurrentSpace());
        item.setStatus(State.NEW);
        add(new ItemForm("form", item));
    }
    
    /**
     * Constructor with provided item id.
     * 
     * @param itemId The 
     */
    public ItemFormPage(long itemId) {
        Item item = getJtrac().loadItem(itemId);
        add(new ItemForm("form", item));
    }
    
    /**
     * Wicket form
     */
    private class ItemForm extends Form {
    	
    	private SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
    	private Calendar c;
    	private Model severityModel, datemodel;
    	private int itemEscalationPeriod;
    	private Severity selectedSeverity;
        private JtracFeedbackMessageFilter filter;
        private FileUploadField fileUploadField = new FileUploadField("file");
        private boolean editMode;
        private int version;
        private List<Severity> severities = new ArrayList<Severity>();
        private Severity severity = new Severity();
        private List<SpaceSeverityPeriod> spaceSeverityPeriods = new ArrayList<SpaceSeverityPeriod>();
        private List<Category> categories = new ArrayList<Category>();
        private Category category = new Category();
        private int numberOfDays;
        private String dueDateStatement = "The due date is on : ";
        private String escalationStatement = "Automatic escalation in: ";
        private Severity itemSeverity;
        
        private TextField originator;
        private TextArea detailsTextArea;
        private TextField originator_contact;
        private DateTextField dateTextField;
        private Date itemDateAdded;
        private DropDownChoice dropDownSeverities;

		public Date getItemDateAdded() {
			return itemDateAdded;
		}

		public void setItemDateAdded(Date itemDateAdded) {
			this.itemDateAdded = itemDateAdded;
		}

		//private  YuiCalendar calendar;
		//private DateTextField calendar;
		//private DatePickerTextField calendar;
		private DateTextField calendar;
        
        public Category getCategory() {
			return category;
		}

		public void setCategory(Category category) {
			this.category = category;
		}

		Label severityPeriod;
        	
        private String period = "0";
        private String duedate = "0"; 
        
        public Severity getSeverity() {
			return severity;
		}

		public void setSeverity(Severity severity) {
			this.severity = severity;
		}

		//private Label severityDays;
        
        /**
         * Constructor
         * 
         * @param id
         * @param item
         */
        @SuppressWarnings({ "serial", "deprecation" })
		public ItemForm(String id, final Item item) {
            super(id);
            
            setMultiPart(true);
            FeedbackPanel feedback = new FeedbackPanel("feedback");
            filter = new JtracFeedbackMessageFilter();
            feedback.setFilter(filter);
            add(feedback);
            version = item.getVersion();
            
            c = Calendar.getInstance();
            itemDateAdded = new Date();

            //calendar = new YuiCalendar("dateAdded", new PropertyModel(item, "dateAdded"), true);
            
            
            //calendar = new DatePickerTextField("dateAdded", new PropertyModel(item, "dateAdded"));
            //calendar.setType(Date.class);
            //calendar.setRequired(true);
            //add(calendar);
            calendar = new DateTextField("dateTextField", new PropertyModel(item, "dateAdded"));
            DatePicker datePicker = new DatePicker();
            //datePicker.setShowOnFieldClick(true);
            //datePicker.setAutoHide(true);
            calendar.add(datePicker);
            calendar.setRequired(true);
            calendar.add(new ErrorHighlighter());
            add(calendar);
             
            /*//Date and time fields
            DateTimeField dateTimeField = new DateTimeField("dateTimeField", new PropertyModel(item, "dateAdded")){
                @Override
                protected boolean use12HourFormat() {
                    //this will force to use 24 hours format
                    return false;
                }
            };
            add(dateTimeField);*/
            
            itemSeverity = new Severity();
            
            selectedSeverity = new Severity();
            severityPeriod = new Label( "auto_escalation_period", new PropertyModel(this,"period") ){
          	  { setOutputMarkupId( true ); }
          	};
            
            if (item.getId() > 0) {
                editMode = true;
            }
            
            BoundCompoundPropertyModel model = null;
            if (editMode) {
                /*
                 * This ensures that the model object is re-loaded as part
                 * of the form submission workflow before form binding and
                 * avoids hibernate lazy loading issues during the whole
                 * update transaction.
                 */
            	
                LoadableDetachableModel itemModel = new LoadableDetachableModel() {
                    protected Object load() {
                        logger.debug("attaching existing item " + item.getId());
                        return getJtrac().loadItem(item.getId());
                    }
                };
                model = new BoundCompoundPropertyModel(itemModel);
                byte[] decodedDetail = new Base64().encode(item.getDetail().getBytes());
                
                //item.setDetail(new String(decodedDetail));
                item.setDetail(item.getDetail());
                
                itemSeverity = item.getSpaceSeverityPeriod().getSeverity();
            } else {
                model = new BoundCompoundPropertyModel(item);
            }
            setModel(model);
            
            /*
             * ===================================================
             * Summary
             * ===================================================
             */
            final TextField summaryField = new TextField("summary");
            summaryField.setRequired(true);
            summaryField.add(new ErrorHighlighter());
            summaryField.setOutputMarkupId(true);
            add(summaryField);
            add(new HeaderContributor(new IHeaderContributor() {
                public void renderHead(IHeaderResponse response) {
                    response.renderOnLoadJavascript("document.getElementById('" + 
                            summaryField.getMarkupId() + "').focus()");
                }
            }));
            
            /*
             * ===================================================
             * Delete button
             * ===================================================
             */
            Button delete = new Button("delete") {
            	
            	@Override
            	protected String getOnClickScript() {
            		
            		return "alert('Delete Clicked')";
            		//return super.getOnClickScript();
            	}
            	
                @Override
                public void onSubmit() {
                    String heading = localize("item_delete.confirm");
                    String warning = localize("item_delete.line2");
                    String line1 = localize("item_delete.line1");
                    ConfirmPage confirm = new ConfirmPage(ItemFormPage.this, heading, warning, new String[] { line1 }) {
                        public void onConfirm() {
                            // avoid lazy init problem
                            getJtrac().removeItem(getJtrac().loadItem(item.getId()));
                            ItemSearch itemSearch = JtracSession.get().getItemSearch();
                            if (itemSearch != null) {
                                setResponsePage(new ItemListPage(itemSearch));
                            } else {
                                setResponsePage(DashboardPage.class);
                            }
                        }
                    };
                    setResponsePage(confirm);
                }
            };
            
            delete.setDefaultFormProcessing(false);
            add(delete);
            if (!editMode) {
                delete.setVisible(false);
            }
            
            /*
             * ===================================================
             * Detail
             * ===================================================
             */detailsTextArea = new TextArea("detail");
            
            detailsTextArea = new TextArea("detail");
            detailsTextArea.setRequired(true).add(new ErrorHighlighter());
            add(detailsTextArea);
            
            
            /*
             * ===================================================
             * Custom fields
             * ===================================================
             */
            if (editMode) {
                add(new CustomFieldsFormPanel("fields", model, item.getSpace()).setRenderBodyOnly(true));
            } else {
                add(new CustomFieldsFormPanel("fields", model, item, getPrincipal()).setRenderBodyOnly(true));
            }
            
            /*
             * Originator
             */
            
            originator = new TextField("originator");
            originator.setRequired(true);
            originator.add(new ErrorHighlighter());
            add(originator);
            
            originator_contact = new TextField("originator_contact");
            originator_contact.setRequired(true);
            originator_contact.add(new ErrorHighlighter());
            add(originator_contact);
            
            
            /*
             * Hide some components if editing item
             */
            WebMarkupContainer hideAssignedTo = new WebMarkupContainer("hideAssignedTo");
            WebMarkupContainer hideNotifyList = new WebMarkupContainer("hideNotifyList");
            WebMarkupContainer hideEditReason = new WebMarkupContainer("hideEditReason");
            add(hideAssignedTo);
            add(hideNotifyList);
            add(hideEditReason);
            if (editMode) {
                hideAssignedTo.setVisible(false);
                hideNotifyList.setVisible(false);
                hideEditReason.add(new TextArea("editReason").setRequired(true).add(new ErrorHighlighter()));
            } else {
                hideEditReason.setVisible(false);
                
                /*
                 * ===================================================
                 * Assigned to
                 * ===================================================
                 */
                Space space = item.getSpace();
                List<UserSpaceRole> userSpaceRoles = getJtrac().findUserRolesForSpace(space.getId());
                List<User> assignable = UserUtils.filterUsersAbleToTransitionFrom(userSpaceRoles, space, State.OPEN);
                DropDownChoice choice = new DropDownChoice("assignedTo", assignable, new IChoiceRenderer() {
                    public Object getDisplayValue(Object o) {
                        return ((User) o).getName();
                    }
                    
                    public String getIdValue(Object o, int i) {
                        return ((User) o).getId() + "";
                    }
                });
                
                choice.setNullValid(true);
                choice.setRequired(true);
                WebMarkupContainer border = new WebMarkupContainer("border");
                border.add(choice);
                border.add(new ErrorHighlighter(choice));
                hideAssignedTo.add(border);
                
                /*
                 * ===================================================
                 * Notify list
                 * ===================================================
                 */
                List<ItemUser> choices = UserUtils.convertToItemUserList(userSpaceRoles);
                //itemUsersSMS
                ListMultipleChoice itemUsers = new JtracCheckBoxMultipleChoice("itemUsers", choices,
                        new IChoiceRenderer() {
                            public Object getDisplayValue(Object o) {
                                return ((ItemUser) o).getUser().getName();
                            }
                            
                            public String getIdValue(Object o, int i) {
                                return ((ItemUser) o).getUser().getId() + "";
                            }
                        }, true);
                hideNotifyList.add(itemUsers);
                ListMultipleChoice itemUsersSMS = new JtracCheckBoxMultipleChoice("itemUsersSMS", choices,
                        new IChoiceRenderer() {
                            public Object getDisplayValue(Object o) {
                                return ((ItemUser) o).getUser().getName();
                            }
                            
                            public String getIdValue(Object o, int i) {
                                return ((ItemUser) o).getUser().getId() + "";
                            }
                        }, true);
                hideNotifyList.add(itemUsersSMS);
                /*
                 * ===================================================
                 * Attachment
                 * ===================================================
                 */
                hideNotifyList.add(fileUploadField);
                setMaxSize(Bytes.megabytes(getJtrac().getAttachmentMaxSizeInMb()));
            }
            
            severities = getJtrac().findAllSeverities();            
            
            /*final DropDownChoice dropDownSeverities = new DropDownChoice("severities", new PropertyModel(this, "severity"), severities){
            };*/
            
            dropDownSeverities = new DropDownChoice("severities", new CompoundPropertyModel(itemSeverity), severities, new IChoiceRenderer() {
				
				public String getIdValue(Object object, int index) {
					return ((Severity) object).getId() + "";
				}
				
				public Object getDisplayValue(Object object) {
					return ((Severity) object).getDescription();
				}
			}){
            };
            
            if(!severities.isEmpty()) {
            	//selectedSeverity = severities.get(0);
            	
            	if(editMode)
            		selectedSeverity = ((Severity)dropDownSeverities.getModelObject());//itemSeverity;
            	else
            		selectedSeverity = severities.get(0);
            	
            	spaceSeverityPeriods = getJtrac().findSpaceSeverityPeriodBySpaceAndSeverity(item.getSpace(), selectedSeverity);
            	
            	severityModel = new Model();
            	severityModel.setObject(0);
            	
            	if(!spaceSeverityPeriods.isEmpty()) {
	            	severityModel = new Model() {
	    				
	    				@Override
	    				public void setObject(Object object) {
	    					super.setObject(object);
	    				}
	    				
	    				@Override
	    				 public Integer getObject() {
	    		                return spaceSeverityPeriods.get(0).getPeriod();
	    		         }
	    			};
            	}
            	int duration = Integer.parseInt(severityModel.getObject().toString()) + 1;
				period = escalationStatement + duration + " hour(s)";
    			numberOfDays = Integer.parseInt(String.valueOf(severityModel.getObject()));
    			itemEscalationPeriod = Integer.parseInt(String.valueOf(severityModel.getObject()));
    			
                /*c.setTime(itemDateAdded);
                ItemForm.this.c.add(Calendar.DATE, Integer.parseInt(String.valueOf(severityModel.getObject())));
				duedate = dueDateStatement + sdf.format(ItemForm.this.c.getTime());*/
                
            }
               
            dropDownSeverities.add(new AjaxFormComponentUpdatingBehavior("onchange") {
               
				@Override
				protected void onUpdate(AjaxRequestTarget target) {
					
					selectedSeverity = ((Severity)dropDownSeverities.getModelObject());
		            if(selectedSeverity != null)
		            	spaceSeverityPeriods = getJtrac().findSpaceSeverityPeriodBySpaceAndSeverity(item.getSpace(), selectedSeverity);
		            
	                if(!spaceSeverityPeriods.isEmpty()) {
						
	                	severityModel = new Model() {
	        				
	        				@Override
	        				public void setObject(Object object) {
	        					super.setObject(object);
	        				}
	        				
	        				@Override
	        				 public Integer getObject() {
	        		                return spaceSeverityPeriods.get(0).getPeriod();
	        		         }
	        			};
						
						logger.error(String.valueOf(severityModel.getObject()));	
						int duration = Integer.parseInt(severityModel.getObject().toString()) + 1;
						period = escalationStatement + duration + " hour(s)";
						target.addComponent(severityPeriod);
						itemEscalationPeriod = Integer.parseInt(String.valueOf(severityModel.getObject()));
						
						/*ItemForm.this.c.setTime(itemDateAdded);
						ItemForm.this.c.add(Calendar.DATE, Integer.parseInt(String.valueOf(severityModel.getObject())));
						duedate = dueDateStatement + sdf.format(ItemForm.this.c.getTime());*/
			            
	                }          
					
				}
            });
            
            add(severityPeriod);
            
           /* IChoiceRenderer severitiesChoiceRenderer = new IChoiceRenderer() {
                public Object getDisplayValue(Object o) {
                    return ((Severity) o).getDescription();
                }
                
                public String getIdValue(Object o, int i) {
                    return ((Severity) o).getId() + "";
                }
                
            };*/
           // dropDownSeverities.setChoiceRenderer(severitiesChoiceRenderer);
           
            
            
            dropDownSeverities.setRequired(true);
            
            add(dropDownSeverities);
            
            //==========================Category==================================================
            List<SpaceCategory> spaceCategories = new ArrayList<SpaceCategory>();
            spaceCategories = getJtrac().findSpaceCategoriesBySpace(getCurrentSpace());
            for(SpaceCategory spaceCategory : spaceCategories) {
            	categories.add(spaceCategory.getCategory());
            }
            final DropDownChoice dropdownCategories = new DropDownChoice("categories", new PropertyModel(item, "category"), categories, new IChoiceRenderer() {
				
				public String getIdValue(Object object, int index) {
					return ((Category)object).getId() + "";
				}
				
				public Object getDisplayValue(Object object) {
					return ((Category)object).getDescription();
				}
			}){
            };
            dropdownCategories.setRequired(true);
            dropdownCategories.add(new ErrorHighlighter());
            add(dropdownCategories);
            /*
             * ===================================================
             * Send notifications
             * ===================================================
             */
            add(new CheckBox("sendNotifications"));
            add(new CheckBox("smsNotifications").setEnabled(false));
            
            /*
             * ===================================================
             * Cancel
             * ===================================================
             */
            add(new Link("cancel") {
                public void onClick() {
                    setResponsePage(ItemViewPage.class, new PageParameters("0=" + item.getRefId()));
                }
            }.setVisible(editMode && JtracSession.get().getItemSearch() != null));
        }
        
        /* (non-Javadoc)
         * @see org.apache.wicket.markup.html.form.Form#validate()
         */
        @Override
        protected void validate() {
            filter.reset();
            Item item = (Item) getModelObject();
            if (editMode && item.getVersion() != version) {
                /*
                 * User must have used back button after edit
                 */
                error(localize("item_form.error.version"));
            }
            super.validate();
        }
        
        /* (non-Javadoc)
         * @see org.apache.wicket.markup.html.form.Form#onSubmit()
         */
        @SuppressWarnings("deprecation")
		@Override
        protected void onSubmit() {
            final FileUpload fileUpload = fileUploadField.getFileUpload();
            Item item = (Item) getModelObject();
            User user = getPrincipal();
            
            item.setDetail(item.getDetail());
            
            SpaceSeverityPeriod spaceSeverityPeriod = new SpaceSeverityPeriod();
            
            if(!spaceSeverityPeriods.isEmpty()) {
            	spaceSeverityPeriod = spaceSeverityPeriods.get(0);
            	List<SpaceSeverityPeriod> spaceSevPerds = new ArrayList<SpaceSeverityPeriod>();
            	spaceSevPerds = getJtrac().findSpaceSeverityPeriodBySpaceAndSeverity(getCurrentSpace(), (Severity)dropDownSeverities.getModelObject());
            
            	if(!spaceSevPerds.isEmpty())
            		item.setSpaceSeverityPeriod(spaceSevPerds.get(0));
            }
            
            if (editMode) {
            	//String formattedText = Jsoup.parse(item.getEditReason()).text();
            	item.setEditReason(item.getEditReason());
            	
            	/*Calendar editCalendar = Calendar.getInstance();
            	editCalendar.setTime(item.getDateAdded());
            	
            	editCalendar.set(Calendar.HOUR, new Date().getHours());
            	editCalendar.set(Calendar.MINUTE, new Date().getMinutes());
            	editCalendar.set(Calendar.SECOND, new Date().getSeconds());
            	
            	item.setDateAdded(c.getTime());*/
            	           	
            	/*System.out.println("^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^ " +calendar.getValue() + " ^^^^^^^^^^^^^^^^^^^^^^^^");*/
            	Date newDateAdded = (Date) calendar.getModelObject();
            	c.setTime(newDateAdded);
            	c.set(Calendar.HOUR, new Date().getHours());
            	c.set(Calendar.MINUTE, new Date().getMinutes());
            	c.set(Calendar.SECOND, new Date().getSeconds());
            	
            	item.setDateAdded(c.getTime());
            	
            	// Increment calendar hours based on priority level chosen
                c.add(Calendar.HOUR, itemEscalationPeriod);
                
                item.setDueDate(c.getTime());
                //item.setDueDate(new Date(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH), c.get(Calendar.HOUR), c.get(Calendar.MINUTE), c.get(Calendar.SECOND)));
            	
                getJtrac().updateItem(item, user);
            } else {            	
            	
            	c.setTime(new Date(calendar.getModelObjectAsString()));
            	//c.setTime(new Date(calendar.getValue()));
                
            	c.set(Calendar.HOUR, new Date().getHours());
            	c.set(Calendar.MINUTE, new Date().getMinutes());
            	c.set(Calendar.SECOND, new Date().getSeconds());
            	
            	item.setDateAdded(c.getTime());
            	
            	// Increment calendar hours based on priority level chosen
                c.add(Calendar.HOUR, itemEscalationPeriod);
                
                item.setDueDate(c.getTime());
                //item.setDueDate(new Date(c.get(Calendar.YEAR) - 1900, c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH), c.get(Calendar.HOUR), c.get(Calendar.MINUTE), c.get(Calendar.SECOND)));
            	
            	System.out.println(item.getDueDate());
                
                item.setLoggedBy(user);
                item.setStatus(State.OPEN);
                getJtrac().storeItem(item, fileUpload);
            }            
            
            /*
             * On creating an item, clear any search filter (especially
             * the related item) from session.
             */
            JtracSession.get().setItemSearch(null);
            setResponsePage(ItemViewPage.class, new PageParameters("0=" + item.getRefId()));
        }
        
    } // end inner class ItemForm
}