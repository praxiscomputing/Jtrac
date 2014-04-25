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

import info.jtrac.domain.Attachment;
import info.jtrac.domain.Category;
import info.jtrac.domain.Config;
import info.jtrac.domain.Counts;
import info.jtrac.domain.CountsHolder;
import info.jtrac.domain.Item;
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
import info.jtrac.domain.Field;
import info.jtrac.domain.History;
import info.jtrac.domain.ItemItem;
import info.jtrac.domain.ItemUser;
import info.jtrac.domain.SpaceSequence;
import info.jtrac.domain.UserSpaceRole;
import info.jtrac.domain.ZSpaceEmail;

import java.util.Collection;

import java.util.List;

/**
 * Jtrac DAO Interface all database access operations
 */
public interface JtracDao {

	void storeItem(Item item);

	Item loadItem(long id);

	History loadHistory(long id);

	void storeHistory(History history);

	List<Item> findItems(long sequenceNum, String prefixCode);

	List<Item> findItems(ItemSearch itemSearch);
	
	List<Item> findItems(Category category);

	int loadCountOfAllItems();

	List<Item> findAllItems(int firstResult, int batchSize);
	
	List<Item> findAllItems();
	
	List<Item> findItems(Space space);

	void removeItem(Item item);
	
	void removeItems(Category category);
	
	void removeItemItem(ItemItem itemItem);

	List<ItemUser> findItemUsersByUser(User user);

	void removeItemUser(ItemUser itemUser);

	List<Item> findItemBySpaceSeverityPeriod(
			SpaceSeverityPeriod spaceSeverityPeriod);

	// ===========================================
	int loadCountOfRecordsHavingFieldNotNull(Space space, Field field);

	int bulkUpdateFieldToNull(Space space, Field field);

	int loadCountOfRecordsHavingFieldWithValue(Space space, Field field,
			int optionKey);

	int bulkUpdateFieldToNullForValue(Space space, Field field, int optionKey);

	int loadCountOfRecordsHavingStatus(Space space, int status);

	int bulkUpdateStatusToOpen(Space space, int status);

	int bulkUpdateRenameSpaceRole(Space space, String oldRoleKey,
			String newRoleKey);

	int bulkUpdateDeleteSpaceRole(Space space, String roleKey);

	int bulkUpdateDeleteItemsForSpace(Space space);

	// ========================================================
	void storeAttachment(Attachment attachment);

	// ===========================================
	void storeMetadata(Metadata metadata);

	Metadata loadMetadata(long id);

	// ===========================================
	void storeSpace(Space space);

	Space loadSpace(long id);

	List<Space> findSpacesByPrefixCode(String prefixCode);

	List<Space> findAllSpaces();
	
	List<Space> findSpaces(User user);

	List<Space> findSpacesNotAllocatedToUser(long userId);

	List<ReasonOutstanding> findAllReasons();

	List<Space> findSpacesWhereIdIn(List<Long> ids);

	List<Space> findSpacesWhereGuestAllowed();

	void removeSpace(Space space);

	// ===========================================
	void storeSeverity(Severity severity);

	List<Severity> findAllSeverities();

	// ===========================================
	void removeSeverity(Severity severity);

	// ===========================================
	List<SpaceSeverityPeriod> findSpaceSeverityPeriodBySpaceId(long spaceId);

	void removeSpaceSeverityPeriodBySeverity(Severity severity);

	void removeSpaceSeverityPeriodBySpace(Space space);

	void storeSpaceSeverityPeriod(SpaceSeverityPeriod spaceSeverityPeriod);

	List<SpaceSeverityPeriod> findSpaceSeverityPeriodBySpaceAndSeverity(
			Space space, Severity severity);

	// ===========================================
	void removeLevel(Level level);

	void storeLevel(Level level);
	List<Level> findLevels(int level);
	List<Level> findAllLevels();

	void removeAllLevels();

	// ===========================================
	long loadNextSequenceNum(long spaceSequenceId);

	void storeSpaceSequence(SpaceSequence spaceSequence);

	// ===========================================
	void storeUser(User user);

	User loadUser(long id);

	void removeUser(User user);

	List<User> findAllUsers();
	
	List<User> findUsers(long id);

	List<User> findUsersWhereIdIn(List<Long> ids);

	List<User> findUsersMatching(String searchText, String searchOn);

	List<User> findUsersByLoginName(String loginName);

	List<User> findUsersByEmail(String email);

	List<User> findUsersForSpace(long spaceId);

	List<User> findUsersNotAllocatedToSpace(long spaceId);

	List<UserSpaceRole> findUserRolesForSpace(long spaceId);

	List<UserSpaceRole> findSpaceRolesForUser(long userId);

	List<User> findUsersWithRoleForSpace(long spaceId, String roleKey);

	List<User> findUsersForSpaceSet(Collection<Space> spaces);

	List<User> findSuperUsers();

	int loadCountOfHistoryInvolvingUser(User user);

	// ===========================================
	UserSpaceRole loadUserSpaceRole(long id);

	void removeUserSpaceRole(UserSpaceRole userSpaceRole);

	// ===========================================
	CountsHolder loadCountsForUser(User user);

	Counts loadCountsForUserSpace(User user, Space space);

	// ===========================================
	List<Config> findAllConfig();

	void storeConfig(Config config);

	Config loadConfig(String key);

	void storeSpaceUselevel(SpaceUserLevel spaceUserLevel);

	void removeSpaceUserLevelBySpace(Space space);
	
	List<SpaceUserLevel> getSpaceUserLevel(Space space, User user);
	
	// ===========================================
	List<Reports> getAllReports();

	// ===========================================
	List<ServerSettings> getServerSettings();

	// ===========================================
	List<Status> getAllStatus();

	// ===========================================
	List<Category> findAllCategories();
	void deleteCategory(Category category);
	void saveCategory(Category category);
	List<Category> findCategoryByDescription(String description);
	List<SpaceCategory> findSpaceCategoriesBySpace(Space space);
	List<SpaceCategory> findSpaceCategories(Category category);
	void removeSpaceCategory(Space space);
	void removeSpaceCategories(Category category);
	// ================ Mail Reader ===========================
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
