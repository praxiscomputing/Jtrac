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

package info.jtrac;

import info.jtrac.domain.BatchInfo;
import info.jtrac.domain.Category;
import info.jtrac.domain.Config;
import info.jtrac.domain.Counts;
import info.jtrac.domain.CountsHolder;
import info.jtrac.domain.Field;
import info.jtrac.domain.History;
import info.jtrac.domain.Item;
import info.jtrac.domain.ItemItem;
import info.jtrac.domain.ItemSearch;
import info.jtrac.domain.Level;
import info.jtrac.domain.Metadata;
import info.jtrac.domain.ReasonOutstanding;
import info.jtrac.domain.Reports;
import info.jtrac.domain.ServerSettings;
import info.jtrac.domain.Severity;
import info.jtrac.domain.Space;
import info.jtrac.domain.SpaceCategory;
import info.jtrac.domain.SpaceSeverityPeriod;
import info.jtrac.domain.SpaceUserLevel;
import info.jtrac.domain.Status;
import info.jtrac.domain.User;
import info.jtrac.domain.UserSpaceRole;
import info.jtrac.domain.ZSpaceEmail;
import info.jtrac.sms.SMSSender;

import java.util.List;
import java.util.Map;

import org.acegisecurity.userdetails.UserDetailsService;
import org.apache.wicket.markup.html.form.upload.FileUpload;

/**
 * Jtrac main business interface (Service Layer)
 */
public interface Jtrac extends UserDetailsService {
    
    // TODO remove Wicket dep with FileUpload
    void storeItem(Item item, FileUpload fileUpload);
    void storeItems(List<Item> items);
    void setSmsSender(SMSSender smsSender);
    void updateItem(Item item, User user);
    void storeHistoryForItem(long itemId, History history, FileUpload fileUpload);
    Item loadItem(long id);
    Item loadItemByRefId(String refId);
    History loadHistory(long id);
    List<Item> findItems(ItemSearch itemSearch);
    List<Item> findItems(Category category);
    int loadCountOfAllItems();
    List<Item> findAllItems(int firstResult, int batchSize);
    List<Item> findAllItems();
    void removeItem(Item item);
    void removeItems(Category category);
    void removeItemItem(ItemItem itemItem);
    List<Item> findItemBySpaceSeverityPeriod(SpaceSeverityPeriod spaceSeverityPeriod);
    //========================================================
    List<ReasonOutstanding> findAllReasons();
    //========================================================
    int loadCountOfRecordsHavingFieldNotNull(Space space, Field field);
    int bulkUpdateFieldToNull(Space space, Field field);
    int loadCountOfRecordsHavingFieldWithValue(Space space, Field field, int optionKey);
    int bulkUpdateFieldToNullForValue(Space space, Field field, int optionKey);
    int loadCountOfRecordsHavingStatus(Space space, int status);
    int bulkUpdateStatusToOpen(Space space, int status);
    int bulkUpdateRenameSpaceRole(Space space, String oldRoleKey, String newRoleKey);
    int bulkUpdateDeleteSpaceRole(Space space, String roleKey);
    //========================================================
    void storeUser(User user);
    void storeUser(User user, String password, boolean sendNotifications);
    void removeUser(User user);    
    User loadUser(long id);
    User loadUser(String loginName);
    List<User> findAllUsers();
    List<User> findUsers(long id);
    List<User> findUsersWhereIdIn(List<Long> ids);
    List<User> findUsersMatching(String searchText, String searchOn);
    List<User> findUsersForSpace(long spaceId);
    List<UserSpaceRole> findUserRolesForSpace(long spaceId);
    Map<Long, List<UserSpaceRole>> loadUserRolesMapForSpace(long spaceId);
    Map<Long, List<UserSpaceRole>> loadSpaceRolesMapForUser(long userId);
    List<User> findUsersWithRoleForSpace(long spaceId, String roleKey);
    List<User> findUsersForUser(User user);
    List<User> findUsersNotFullyAllocatedToSpace(long spaceId);
    int loadCountOfHistoryInvolvingUser(User user);
    //========================================================
    CountsHolder loadCountsForUser(User user);
    Counts loadCountsForUserSpace(User user, Space space);
    //========================================================
    void storeSpace(Space space);
    Space loadSpace(long id);
    Space loadSpace(String prefixCode);
    List<Space> findAllSpaces();
    List<Space> findSpaces(User user);
    List<Space> findSpacesWhereIdIn(List<Long> ids);
    List<Space> findSpacesWhereGuestAllowed();
    List<Space> findSpacesNotFullyAllocatedToUser(long userId);
    void removeSpace(Space space);
    //========================================================
    void storeSeverity(Severity severity);
    List<Severity> findAllSeverities();
    void removeSeverity(Severity severity);
    //========================================================
    void storeSpaceSeverityPeriod(SpaceSeverityPeriod spaceSeverityPeriod);
    void editSpaceSeverityPeriod(SpaceSeverityPeriod spaceSeverityPeriod);
    List<SpaceSeverityPeriod> findSpaceSeverityPeriodBySpaceId(long spaceId);
    List<SpaceSeverityPeriod> findSpaceSeverityPeriodBySpaceAndSeverity(Space space, Severity severity);
    void removeSpaceSeverityPeriodBySeverity(Severity severity);
    void removeSpaceSeverityPeriodBySpace(Space space);
    //========================================================
    void storeLevel(Level level);
    void removeLevel(Level level);
    List<Level> findLevels(int level);
    List<Level> findAllLevels();
    void removeAllLevels();
    //=======================================================
    //========================================================
    void storeUserSpaceRole(User user, Space space, String roleKey);
    UserSpaceRole loadUserSpaceRole(long id);
    void removeUserSpaceRole(UserSpaceRole userSpaceRole);
    //========================================================
    void storeMetadata(Metadata metadata);
    Metadata loadMetadata(long id);
    //========================================================
    String generatePassword();
    String encodeClearText(String clearText);
    Map<String, String> getLocales();
    String getDefaultLocale();
    String getJtracHome();
    int getAttachmentMaxSizeInMb();
    int getSessionTimeoutInMinutes();
    //========================================================
    Map<String, String> loadAllConfig();
    void storeConfig(Config config);
    String loadConfig(String param);
    //========================================================
    void rebuildIndexes(BatchInfo batchInfo);
    boolean validateTextSearchQuery(String text);
    //========================================================
    void executeHourlyTask();
    void executePollingTask();
    //========================================================
    String getReleaseVersion();
    String getReleaseTimestamp();
    void storeSpaceUserlevel(SpaceUserLevel spaceUserLevel);
    void removeSpaceUserLevelBySpace(Space space);
    List<SpaceUserLevel> getSpaceUserLevel(Space space, User user);
    //========================================================
    List<Reports> getAllReports();
    //========================================================
    List<ServerSettings> getServerSettings();
    //========================================================
    List<Status> getAllStatus();
	List<Category> findAllCategories();
	void saveCategory(Category category);
	void deleteCategory(Category category);
	List<SpaceCategory> findSpaceCategoriesBySpace(Space space);
	List<SpaceCategory> findSpaceCategories(Category category);
	void removeSpaceCategories(Category category);
	List<Category> findCategoryByDescription(String description);
	void removeSpaceCategory(Space space);
	//================ Mail Reader ===========================
	List<Severity> findSeverityByName(String name);
	List<Space> findDefaultSpace();
	
	List<User> findUserByEmailAddress(String email);
	List<User> findDefaultUserForEmailAssignmentByEmail(String email);
	List<Space> findNISSpace();
	List<ZSpaceEmail> findAllMails();
	List<ZSpaceEmail> findSpaceEmail(Space space);
	void storeSpaceEmail(ZSpaceEmail spaceEmail);
	void removeSpaceEmail(Space space);
	
	
	void storeSpaceCategory(SpaceCategory spaceCategory);
	List<UserSpaceRole> loadUserSpaceRole(User user, Space space);
	void removeSpaceUserLevel(Space space, User user);
	
	void removeAlertLogs(List<Item> items);
	
	void removeEscalationLogs(List<Item> items);
	
	void removeItemUsers(List<Item> items);
	
}
