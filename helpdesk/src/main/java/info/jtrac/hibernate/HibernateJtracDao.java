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

package info.jtrac.hibernate;

import info.jtrac.JtracDao;
import info.jtrac.domain.AlertLog;
import info.jtrac.domain.Attachment;
import info.jtrac.domain.Category;
import info.jtrac.domain.Config;
import info.jtrac.domain.Counts;
import info.jtrac.domain.CountsHolder;
import info.jtrac.domain.EscalationLog;
import info.jtrac.domain.Field;
import info.jtrac.domain.History;
import info.jtrac.domain.Item;
import info.jtrac.domain.ItemItem;
import info.jtrac.domain.ItemSearch;
import info.jtrac.domain.ItemUser;
import info.jtrac.domain.Level;
import info.jtrac.domain.Metadata;
import info.jtrac.domain.ReasonOutstanding;
import info.jtrac.domain.Reports;
import info.jtrac.domain.Role;
import info.jtrac.domain.ServerSettings;
import info.jtrac.domain.Severity;
import info.jtrac.domain.Space;
import info.jtrac.domain.SpaceCategory;
import info.jtrac.domain.SpaceSequence;
import info.jtrac.domain.SpaceSeverityPeriod;
import info.jtrac.domain.SpaceUserLevel;
import info.jtrac.domain.State;
import info.jtrac.domain.Status;
import info.jtrac.domain.User;
import info.jtrac.domain.UserSpaceRole;
import info.jtrac.domain.ZSpaceEmail;
import info.jtrac.wicket.SpaceEmailPage;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import java.util.Map;
import org.apache.commons.collections.ComparatorUtils;
import org.apache.commons.lang.WordUtils;
import org.hibernate.CacheMode;
import org.hibernate.Criteria;
import org.hibernate.FetchMode;
import org.hibernate.Session;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

/**
 * DAO Implementation using Spring Hibernate template
 * note usage of the Spring "init-method" and "destroy-method" options
 */
public class HibernateJtracDao extends HibernateDaoSupport implements JtracDao {
    
    private SchemaHelper schemaHelper;
    
    public void setSchemaHelper(SchemaHelper schemaHelper) {
        this.schemaHelper = schemaHelper;
    }
    
    @SuppressWarnings("unchecked")
	public List<Item> findItemBySpaceSeverityPeriod(
			SpaceSeverityPeriod spaceSeverityPeriod) {
		return getHibernateTemplate().find("from Item where spaceSeverityPeriod.id = ?", spaceSeverityPeriod.getId());
	}
    
    public void storeItem(Item item) {
        getHibernateTemplate().merge(item);
    }
    
    public Item loadItem(long id) {
        return (Item) getHibernateTemplate().get(Item.class, id);
    }
    
    public void storeHistory(History history) {
        getHibernateTemplate().merge(history);
    }    
    
    public History loadHistory(long id) {
        return (History) getHibernateTemplate().get(History.class, id);
    }
    
    @SuppressWarnings("unchecked")
	public List<Item> findItems(long sequenceNum, String prefixCode) {
        Object[] params = new Object[] {sequenceNum, prefixCode};
        return getHibernateTemplate().find("from Item item where item.sequenceNum = ? and item.space.prefixCode = ?", params);
    }
    
    @SuppressWarnings("unchecked")
	public List<Item> findItems(ItemSearch itemSearch) {
        int pageSize = itemSearch.getPageSize();
        // TODO: if we are ordering by a custom column, we must load the whole
        // list to do an in-memory sort. we need to find a better way
        Field.Name sortFieldName = Field.isValidName(itemSearch.getSortFieldName()) ? Field.convertToName(itemSearch.getSortFieldName()) : null;
        // only trigger the in-memory sort for drop-down fields and when querying within a space
        // UI currently does not allow you to sort by custom field when querying across spaces, but check again
        boolean doInMemorySort = sortFieldName != null && sortFieldName.isDropDownType() && itemSearch.getSpace() != null;
        if (pageSize == -1 || doInMemorySort) {
            List<Item> list = getHibernateTemplate().findByCriteria(itemSearch.getCriteria());
            if(!list.isEmpty() && doInMemorySort) {
                doInMemorySort(list, itemSearch);
            }
            itemSearch.setResultCount(list.size());
            if (pageSize != -1) {
                // order-by was requested on custom field, so we loaded all results, but only need one page
                int start = pageSize * itemSearch.getCurrentPage();
                int end = Math.min(start + itemSearch.getPageSize(), list.size());
                return list.subList(start, end);
            }
            return list;
        } else {
            // pagination
            if(itemSearch.isBatchMode()) {
                getHibernateTemplate().execute(new HibernateCallback() {
                    public Object doInHibernate(Session session) {
                        session.clear();
                        return null;
                    }                    
                });
            }
            int firstResult = pageSize * itemSearch.getCurrentPage();
            List<Item> list = getHibernateTemplate().findByCriteria(itemSearch.getCriteria(), firstResult, pageSize);
            if(!itemSearch.isBatchMode()) {
                DetachedCriteria criteria = itemSearch.getCriteriaForCount();
                criteria.setProjection(Projections.rowCount());
                Integer count = (Integer) getHibernateTemplate().findByCriteria(criteria).get(0);
                itemSearch.setResultCount(count);
            }
            return list;
        }
    }
    
    @SuppressWarnings("unchecked")
	private void doInMemorySort(List<Item> list, ItemSearch itemSearch) { 
        // we should never come here if search is across multiple spaces
        final Field field = itemSearch.getSpace().getMetadata().getField(itemSearch.getSortFieldName());
        final ArrayList<String> valueList = new ArrayList<String>(field.getOptions().keySet());
        Comparator<Item> comp = new Comparator<Item>() {
            public int compare(Item left, Item right) {
                Object leftVal = left.getValue(field.getName());
                String leftValString = leftVal == null ? null : leftVal.toString();
                int leftInd = valueList.indexOf(leftValString);
                Object rightVal = right.getValue(field.getName());
                String rightValString = rightVal == null ? null : rightVal.toString();
                int rightInd = valueList.indexOf(rightValString);
                return leftInd - rightInd;
            }
        };
        Collections.sort(list, itemSearch.isSortDescending() ? ComparatorUtils.reversedComparator(comp) : comp);
    }
    
    public int loadCountOfAllItems() {
        return (Integer) getHibernateTemplate().execute(new HibernateCallback() {
            public Object doInHibernate(Session session) {
                Criteria criteria = session.createCriteria(Item.class);
                criteria.setProjection(Projections.rowCount());
                return criteria.list().get(0);
            }
        });
    }
    
    @SuppressWarnings("unchecked")
	public List<Item> findAllItems(final int firstResult, final int batchSize) {
        return getHibernateTemplate().executeFind(new HibernateCallback() {
            public Object doInHibernate(Session session) {
                session.clear();
                Criteria criteria = session.createCriteria(Item.class);
                criteria.setCacheMode(CacheMode.IGNORE);
                criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
                criteria.setFetchMode("history", FetchMode.JOIN);
                criteria.add(Restrictions.ge("id", (long) firstResult));
                criteria.add(Restrictions.lt("id", (long) firstResult + batchSize));
                return criteria.list();
            }
        });
    }
    
    public void removeItem(Item item) {
        getHibernateTemplate().delete(item);
    }
    
    public void removeItemItem(ItemItem itemItem) {
        getHibernateTemplate().delete(itemItem);
    }
    
    @SuppressWarnings("unchecked")
	public List<ItemUser> findItemUsersByUser(User user) {
        return getHibernateTemplate().find("from ItemUser iu where iu.user = ?", user);
    }
    
    public void removeItemUser(ItemUser itemUser) {
        getHibernateTemplate().delete(itemUser);
    }
    
    public void storeAttachment(Attachment attachment) {
        getHibernateTemplate().merge(attachment);
    }
    
    public void storeMetadata(Metadata metadata) {
        getHibernateTemplate().merge(metadata);
    }
    public void storeSpaceUserLevel(SpaceUserLevel spaceUserLevel){
    	getHibernateTemplate().merge(spaceUserLevel);
    }
    public Metadata loadMetadata(long id) {
        return (Metadata) getHibernateTemplate().get(Metadata.class, id);
    }
    
    public void storeSpace(Space space) {
        getHibernateTemplate().merge(space);
    }
    
    public void storeSeverity(Severity severity) {
    	getHibernateTemplate().merge(severity);
    }
    
    public void storeSpaceUselevel(SpaceUserLevel spaceUserLevel) {
    	getHibernateTemplate().merge(spaceUserLevel);
    }
    public Space loadSpace(long id) {
        return (Space) getHibernateTemplate().get(Space.class, id);
    }
    
    public UserSpaceRole loadUserSpaceRole(long id) {
        return (UserSpaceRole) getHibernateTemplate().get(UserSpaceRole.class, id);
    }
    
    public long loadNextSequenceNum(final long spaceSequenceId) {
        return (Long) getHibernateTemplate().execute(new HibernateCallback() {
            public Object doInHibernate(Session session) {  
                session.flush();
                session.setCacheMode(CacheMode.IGNORE);
                SpaceSequence ss = (SpaceSequence) session.get(SpaceSequence.class, spaceSequenceId);
                long next = ss.getAndIncrement();
                session.update(ss);
                session.flush();
                return next;
            }
        });
    }
    
    public void storeSpaceSequence(SpaceSequence spaceSequence) {
        getHibernateTemplate().save(spaceSequence);
    }
    
    
    @SuppressWarnings("unchecked")
	public List<Space> findSpacesByPrefixCode(String prefixCode) {
        return getHibernateTemplate().find("from Space space where space.prefixCode = ?", prefixCode);
    }
    
    @SuppressWarnings("unchecked")
	public List<Space> findAllSpaces() {
        return getHibernateTemplate().find("from Space space order by space.prefixCode");
    }
    
    @SuppressWarnings("unchecked")
	public List<Space> findSpacesNotAllocatedToUser(long userId) {
        return getHibernateTemplate().find("from Space space where space not in"
                + " (select usr.space from UserSpaceRole usr where usr.user.id = ?) order by space.name", userId);
    }
    
    @SuppressWarnings("unchecked")
	public List<Space> findSpacesWhereIdIn(List<Long> ids) {
        return getHibernateTemplate().findByNamedParam("from Space space where space.id in (:ids)", "ids", ids);
    }    
    
    @SuppressWarnings("unchecked")
	public List<Space> findSpacesWhereGuestAllowed() {        
        return getHibernateTemplate().find("from Space space join fetch space.metadata where space.guestAllowed = true");
    }
    
    public void removeSpace(Space space) {
        getHibernateTemplate().delete(space);
    }
    
    public void storeUser(User user) {
        getHibernateTemplate().merge(user);
    }
    
    public void storeLevel(Level level) {
    	getHibernateTemplate().merge(level);
    }
    public void removeLevel(Level level) {
        getHibernateTemplate().delete(level);
    }
    
    public void removeAllLevels() {
		
	}

    public void storeCategory(Category category) {
    	getHibernateTemplate().merge(category);
    }
    
    public User loadUser(long id) {
        return (User) getHibernateTemplate().get(User.class, id);
    }
    
    public void removeUser(User user) {
        getHibernateTemplate().delete(user);
    }
    
    @SuppressWarnings("unchecked")
	public List<User> findAllUsers() {
        return getHibernateTemplate().find("from User user order by user.name");
    }
    
    @SuppressWarnings("unchecked")
	public List<User> findUsersWhereIdIn(List<Long> ids) {
        return getHibernateTemplate().findByNamedParam("from User user where user.id in (:ids)", "ids", ids);
    }    
    
    @SuppressWarnings("unchecked")
	public List<User> findUsersMatching(final String searchText, final String searchOn) {
        return (List<User>) getHibernateTemplate().execute(new HibernateCallback() {
            public Object doInHibernate(Session session) {
                Criteria criteria = session.createCriteria(User.class);
                criteria.add(Restrictions.ilike(searchOn, searchText, MatchMode.ANYWHERE));
                criteria.addOrder(Order.asc("name"));
                return criteria.list();
            }
        });
    }
    
    @SuppressWarnings("unchecked")
	public List<User> findUsersByLoginName(String loginName) {
        return getHibernateTemplate().find("from User user where user.loginName = ?", loginName);
    }
    
    @SuppressWarnings("unchecked")
	public List<User> findUsersByEmail(String email) {
        return getHibernateTemplate().find("from User user where user.email = ?", email);
    }
    
    @SuppressWarnings("unchecked")
	public List<User> findUsersNotAllocatedToSpace(long spaceId) {
        return getHibernateTemplate().find("from User user where user not in"
                + " (select usr.user from UserSpaceRole usr where usr.space.id = ?) order by user.name", spaceId);
    }
    
    @SuppressWarnings("unchecked")
	public List<UserSpaceRole> findUserRolesForSpace(long spaceId) {
        // join fetch for user object
        return getHibernateTemplate().find("select usr from UserSpaceRole usr join fetch usr.user"
                 + " where usr.space.id = ? order by usr.user.name", spaceId);
    }
    
    @SuppressWarnings("unchecked")
	public List<User> findUsersWithRoleForSpace(long spaceId, String roleKey) {
        return getHibernateTemplate().find("from User user"
                + " join user.userSpaceRoles as usr where usr.space.id = ?"
                + " and usr.roleKey = ? order by user.name", new Object[] {spaceId, roleKey});
    }    
    
    @SuppressWarnings("unchecked")
    public List<Severity> findAllSeverities() {
		return getHibernateTemplate().find("from Severity");
	}
    
    public void removeSeverity(Severity severity) {
		getHibernateTemplate().delete(severity);
	}
    
    @SuppressWarnings("unchecked")
	public List<SpaceSeverityPeriod> findSpaceSeverityPeriodBySpaceId(long spaceId) {
    	return getHibernateTemplate().find("from SpaceSeverityPeriod where space.id = ?", spaceId);
    }
    
    @SuppressWarnings("unchecked")
	public List<SpaceSeverityPeriod> findSpaceSeverityPeriodBySpaceAndSeverity(
			Space space, Severity severity) {
    	List<Object> params = new ArrayList<Object>();
    	params.add(space.getId());
    	params.add(severity.getId());
    	
		return getHibernateTemplate().find("from SpaceSeverityPeriod where space.id = ? and severity.id = ?", params.toArray());
	}
    
    @SuppressWarnings("unchecked")
    public void removeSpaceSeverityPeriodBySeverity(Severity severity) {
    	
    	List<SpaceSeverityPeriod> spaceSeverityPeriods = getHibernateTemplate().find("from SpaceSeverityPeriod where severity.id = ?", severity.getId());
    	
    	for(int i=0; i<spaceSeverityPeriods.size(); i++) {
    		getHibernateTemplate().delete(spaceSeverityPeriods.get(i));
    	}
	}
    
    public void storeSpaceSeverityPeriod(SpaceSeverityPeriod spaceSeverityPeriod) {
    	getHibernateTemplate().merge(spaceSeverityPeriod);
	}
    
    @SuppressWarnings("unchecked")
    public List<Level> findAllLevels() {
		return getHibernateTemplate().find("from Level");
	}
    @SuppressWarnings("unchecked")
	public List<UserSpaceRole> findSpaceRolesForUser(long userId) {
        return getHibernateTemplate().find("select usr from UserSpaceRole usr" 
                + " left join fetch usr.space as space"
                + " left join fetch space.metadata"
                + " where usr.user.id = ? order by usr.space.name", userId);
    }
    
    @SuppressWarnings("unchecked")
	public List<User> findSuperUsers() {
        return getHibernateTemplate().find("select usr.user from UserSpaceRole usr"
                + " where usr.space is null and usr.roleKey = ?", Role.ROLE_ADMIN);
    }
    
    public int loadCountOfHistoryInvolvingUser(User user) {
        Long count = (Long) getHibernateTemplate().find("select count(history) from History history where "
                + " history.loggedBy = ? or history.assignedTo = ?", new Object[] {user, user}).get(0);
        return count.intValue();
    }
    
    //==========================================================================
    
    @SuppressWarnings("unchecked")
	public CountsHolder loadCountsForUser(User user) {
        Collection<Space> spaces = user.getSpaces();
        if (spaces.size() == 0) {
            return null;
        }
        CountsHolder ch = new CountsHolder();
        HibernateTemplate ht = getHibernateTemplate();
        List<Object[]> loggedByList = ht.find("select item.space.id, count(item) from Item item" 
                + " where item.loggedBy.id = ? group by item.space.id", user.getId());
        List<Object[]> assignedToList = ht.find("select item.space.id, count(item) from Item item" 
                + " where item.assignedTo.id = ? group by item.space.id", user.getId());
        List<Object[]> statusList = ht.findByNamedParam("select item.space.id, count(item) from Item item" 
                + " where item.space in (:spaces) group by item.space.id", "spaces", spaces);
        for(Object[] oa : loggedByList) {
            ch.addLoggedByMe((Long) oa[0], (Long) oa[1]);
        }
        for(Object[] oa : assignedToList) {
            ch.addAssignedToMe((Long) oa[0], (Long) oa[1]);
        }
        for(Object[] oa : statusList) {
            ch.addTotal((Long) oa[0], (Long) oa[1]);
        }
        return ch;
    }
    
    @SuppressWarnings("unchecked")
	public Counts loadCountsForUserSpace(User user, Space space) {
        HibernateTemplate ht = getHibernateTemplate();
        List<Object[]> loggedByList = ht.find("select status, count(item) from Item item" 
                + " where item.loggedBy.id = ? and item.space.id = ? group by item.status", new Object[] {user.getId(), space.getId()});
        List<Object[]> assignedToList = ht.find("select status, count(item) from Item item" 
                + " where item.assignedTo.id = ? and item.space.id = ? group by item.status", new Object[] {user.getId(), space.getId()});
        List<Object[]> statusList = ht.find("select status, count(item) from Item item" 
                + " where item.space.id = ? group by item.status", space.getId());
        Counts c = new Counts(true);
        for(Object[] oa : loggedByList) {
            c.addLoggedByMe((Integer) oa[0], (Long) oa[1]);
        }
        for(Object[] oa : assignedToList) {
            c.addAssignedToMe((Integer) oa[0], (Long) oa[1]);
        }
        for(Object[] oa : statusList) {
            c.addTotal((Integer) oa[0], (Long) oa[1]);
        }
        return c;
    }
    
    //==========================================================================
    
    @SuppressWarnings("unchecked")
	public List<User> findUsersForSpace(long spaceId) {
        return getHibernateTemplate().find("select distinct u from User u join u.userSpaceRoles usr" 
                + " where usr.space.id = ? order by u.name", spaceId);
    }
    
    @SuppressWarnings("unchecked")
	public List<User> findUsersForSpaceSet(Collection<Space> spaces) {
        return getHibernateTemplate().findByNamedParam("select u from User u join u.userSpaceRoles usr" 
                + " where usr.space in (:spaces) order by u.name", "spaces", spaces);
    }
    
    public void removeUserSpaceRole(UserSpaceRole userSpaceRole) {
        getHibernateTemplate().delete(userSpaceRole);
    }
    
    @SuppressWarnings("unchecked")
	public List<Config> findAllConfig() {
        return getHibernateTemplate().loadAll(Config.class);
    }
    
    public void storeConfig(Config config) {
        getHibernateTemplate().merge(config);
    }
    
    public Config loadConfig(String param) {
        return (Config) getHibernateTemplate().get(Config.class, param);
    }
    
    public int loadCountOfRecordsHavingFieldNotNull(Space space, Field field) {
        Criteria criteria = getSession().createCriteria(Item.class);
        criteria.add(Restrictions.eq("space", space));
        criteria.add(Restrictions.isNotNull(field.getName().toString()));
        criteria.setProjection(Projections.rowCount());
        int itemCount = (Integer) criteria.list().get(0);
        // even when no item has this field not null currently, items may have history with this field not null
        // because of the "parent" difference, cannot use AbstractItem and have to do a separate Criteria query
        criteria = getSession().createCriteria(History.class);
        criteria.createCriteria("parent").add(Restrictions.eq("space", space));
        criteria.add(Restrictions.isNotNull(field.getName().toString()));
        criteria.setProjection(Projections.rowCount());
        return itemCount + (Integer) criteria.list().get(0);
    }
    
    public int bulkUpdateFieldToNull(Space space, Field field) {
        int itemCount = getHibernateTemplate().bulkUpdate("update Item item set item." + field.getName() + " = null" 
                + " where item.space.id = ?", space.getId());
        logger.info("no of Item rows where " + field.getName() + " set to null = " + itemCount);
        int historyCount = getHibernateTemplate().bulkUpdate("update History history set history." + field.getName() + " = null"
                + " where history.parent in ( from Item item where item.space.id = ? )", space.getId());
        logger.info("no of History rows where " + field.getName() + " set to null = " + historyCount);
        return itemCount;
    }
    
    public int loadCountOfRecordsHavingFieldWithValue(Space space, Field field, int optionKey) {
        Criteria criteria = getSession().createCriteria(Item.class);
        criteria.add(Restrictions.eq("space", space));
        criteria.add(Restrictions.eq(field.getName().toString(), optionKey));
        criteria.setProjection(Projections.rowCount());
        int itemCount = (Integer) criteria.list().get(0);
        // even when no item has this field value currently, items may have history with this field value
        // because of the "parent" difference, cannot use AbstractItem and have to do a separate Criteria query
        criteria = getSession().createCriteria(History.class);
        criteria.createCriteria("parent").add(Restrictions.eq("space", space));
        criteria.add(Restrictions.eq(field.getName().toString(), optionKey));
        criteria.setProjection(Projections.rowCount());
        return itemCount + (Integer) criteria.list().get(0);
    }
    
    public int bulkUpdateFieldToNullForValue(Space space, Field field, int optionKey) {
        int itemCount = getHibernateTemplate().bulkUpdate("update Item item set item." + field.getName() + " = null" 
                + " where item.space.id = ?"
                + " and item." + field.getName() + " = ?", new Object[] {space.getId(), optionKey});
        logger.info("no of Item rows where " + field.getName() + " value '" + optionKey + "' replaced with null = " + itemCount);
        int historyCount = getHibernateTemplate().bulkUpdate("update History history set history." + field.getName() + " = null"
                + " where history." + field.getName() + " = ?"
                + " and history.parent in ( from Item item where item.space.id = ? )", new Object[] {optionKey, space.getId()});
        logger.info("no of History rows where " + field.getName() + " value '" + optionKey + "' replaced with null = " + historyCount);
        return itemCount;
    }
    
    public int loadCountOfRecordsHavingStatus(Space space, int status) {
        Criteria criteria = getSession().createCriteria(Item.class);
        criteria.add(Restrictions.eq("space", space));
        criteria.add(Restrictions.eq("status", status));
        criteria.setProjection(Projections.rowCount());
        int itemCount = (Integer) criteria.list().get(0);
        // even when no item has this status currently, items may have history with this status
        // because of the "parent" difference, cannot use AbstractItem and have to do a separate Criteria query
        criteria = getSession().createCriteria(History.class);
        criteria.createCriteria("parent").add(Restrictions.eq("space", space));
        criteria.add(Restrictions.eq("status", status));
        criteria.setProjection(Projections.rowCount());
        return itemCount + (Integer) criteria.list().get(0);
    }
    
    public int bulkUpdateStatusToOpen(Space space, int status) {
        int itemCount = getHibernateTemplate().bulkUpdate("update Item item set item.status = " + State.OPEN 
                + " where item.status = ? and item.space.id = ?", new Object[] {status, space.getId()});
        logger.info("no of Item rows where status changed from " + status + " to " + State.OPEN + " = " + itemCount);
        int historyCount = getHibernateTemplate().bulkUpdate("update History history set history.status = " + State.OPEN 
                + " where history.status = ?"
                + " and history.parent in ( from Item item where item.space.id = ? )", new Object[] {status, space.getId()});
        logger.info("no of History rows where status changed from " + status + " to " + State.OPEN + " = " + historyCount);
        return itemCount;
    }
    
    public int bulkUpdateRenameSpaceRole(Space space, String oldRoleKey, String newRoleKey) {
        return getHibernateTemplate().bulkUpdate("update UserSpaceRole usr set usr.roleKey = ?"
                + " where usr.roleKey = ? and usr.space.id = ?", new Object[] {newRoleKey, oldRoleKey, space.getId()});
    }
    
    public int bulkUpdateDeleteSpaceRole(Space space, String roleKey) {
        if (roleKey == null) {
            return getHibernateTemplate().bulkUpdate("delete UserSpaceRole usr where usr.space.id = ?", space.getId());
        } else {
            return getHibernateTemplate().bulkUpdate("delete UserSpaceRole usr"
                    + " where usr.space.id = ? and usr.roleKey = ?", new Object[] {space.getId(), roleKey});
        }
    }
    
    public int bulkUpdateDeleteItemsForSpace(Space space) {
        int historyCount = getHibernateTemplate().bulkUpdate("delete History history where history.parent in"
                + " ( from Item item where item.space.id = ? )", space.getId());
        logger.debug("deleted " + historyCount + " records from history");
        int itemItemCount = getHibernateTemplate().bulkUpdate("delete ItemItem itemItem where itemItem.item in"
                + " ( from Item item where item.space.id = ? )", space.getId());
        logger.debug("deleted " + itemItemCount + " records from item_items");
        
        int itemUserCount = getHibernateTemplate().bulkUpdate("delete ItemUser itemUser where itemUser.item in"
                + " ( from Item item where item.space.id = ? )", space.getId());
        
        logger.debug("deleted " + itemUserCount + " records from item user");
        
        int itemCount = getHibernateTemplate().bulkUpdate("delete Item item where item.space.id = ?", space.getId());
        logger.debug("deleted " + itemCount + " records from items");
        return historyCount + itemItemCount + itemCount;
    }
    
    //==========================================================================
    
    /**
     * note that this is automatically configured to run on startup 
     * as a spring bean "init-method"
     */
    @SuppressWarnings("unchecked")
	public void createSchema() {
        try {
            getHibernateTemplate().find("from Item item where item.id = 1");
            logger.info("database schema exists, normal startup");
        } catch (Exception e) {
            logger.warn("expected database schema does not exist, will create. Error is: " + e.getMessage());
            schemaHelper.createSchema();
            User admin = new User();
            admin.setLoginName("admin");
            admin.setName("Admin");
            admin.setEmail("admin");
            admin.setPassword("21232f297a57a5a743894a0e4a801fc3");
            admin.addSpaceWithRole(null, Role.ROLE_ADMIN);
            logger.info("inserting default admin user into database");
            storeUser(admin);

            /*
             * Create and store priority Levels high, medium, and low
             * 
             */
            String[] descriptioins = {"High", "Medium", "Low"};
            for(String desc : descriptioins) {
            	Severity severity = new Severity();
            	severity.setDescription(desc);
            	storeSeverity(severity);
            }
            
            /*
             * Create and store 10 user levels
             */
            int[] levels = {1,2,3,4,5,6,7,8,9,10};
            for(int l : levels) {
            	Level level = new Level();
            	level.setLevel(l);
            	storeLevel(level);
            }
            
            String[] categories = {"Support", "Development", "Training", "Upgrade", "Installation"};
            for(String s : categories) {
            	Category category = new Category();
            	
            	category.setDescription(s);
            	if(s.equals("Development"))
            		category.setAlert_escalate_yn(false);
            	else
            		category.setAlert_escalate_yn(true);
            	
            	storeCategory(category);            		
            }
            
            
            logger.info("schema creation complete");
        }
        List<SpaceSequence> ssList = getHibernateTemplate().loadAll(SpaceSequence.class);
        Map<Long, SpaceSequence> ssMap = new HashMap<Long, SpaceSequence>(ssList.size());
        for(SpaceSequence ss : ssList) {
            ssMap.put(ss.getId(), ss);
        }
        List<Object[]> list = getHibernateTemplate().find("select item.space.id, max(item.sequenceNum) from Item item group by item.space.id");
        for(Object[] oa : list) {
            Long spaceId = (Long) oa[0];
            Long maxSeqNum = (Long) oa[1];
            SpaceSequence ss = ssMap.get(spaceId);
            logger.info("checking space sequence id: " + spaceId + ", max: " + maxSeqNum + ", next: " + ss.getNextSeqNum());
            if(ss.getNextSeqNum() <= maxSeqNum) {
                logger.warn("fixing sequence number for space id: " + spaceId 
                        + ", was: " + ss.getNextSeqNum() + ", should be: " + (maxSeqNum + 1));
                ss.setNextSeqNum(maxSeqNum + 1);
                getHibernateTemplate().update(ss);
            }
        }
    }

	@SuppressWarnings("unchecked")
	public void removeSpaceSeverityPeriodBySpace(Space space) {
		List<SpaceSeverityPeriod> spaceSeverityPeriods = new ArrayList<SpaceSeverityPeriod>();
		spaceSeverityPeriods = getHibernateTemplate().find("from SpaceSeverityPeriod where space.id = ?", space.getId());
		for(SpaceSeverityPeriod s : spaceSeverityPeriods) {
			getHibernateTemplate().delete(s);
		}
	}

	@SuppressWarnings("unchecked")
	public void removeSpaceUserLevelBySpace(Space space) {

		List<SpaceUserLevel> spaceUserLevels = new ArrayList<SpaceUserLevel>();
		spaceUserLevels = getHibernateTemplate().find("from SpaceUserLevel where space.id = ?", space.getId());
		
		for(SpaceUserLevel spaceUserLevel : spaceUserLevels) {
			getHibernateTemplate().delete(spaceUserLevel);
		}
	}

	@SuppressWarnings("unchecked")
	public List<Reports> getAllReports() {
		return getHibernateTemplate().find("from Reports");
	}

	@SuppressWarnings("unchecked")
	public List<ServerSettings> getServerSettings() {
		return getHibernateTemplate().find("from ServerSettings");
	}

	@SuppressWarnings("unchecked")
	public List<Status> getAllStatus() {
		return getHibernateTemplate().find("from Status");
	}

	@SuppressWarnings("unchecked")
	public List<Category> findAllCategories() {
		return getHibernateTemplate().find("from Category");
	}

	@SuppressWarnings("unchecked")
	public List<ReasonOutstanding> findAllReasons() {
		return getHibernateTemplate().find("from ReasonOutstanding");
	}

	@SuppressWarnings("unchecked")
	public List<Severity> findSeverityByName(String name) {
		return getHibernateTemplate().find("from Severity where description = ?", WordUtils.capitalize(name));
	}

	@SuppressWarnings("unchecked")
	public List<Space> findDefaultSpace() {
		return getHibernateTemplate().find("from Space where prefix_code = 'DEFAULT'");
	}

	@SuppressWarnings("unchecked")
	public List<User> findUserByEmailAddress(String email) {
		return getHibernateTemplate().find("from users where emal = ?", email);
	}

	@SuppressWarnings("unchecked")
	public List<User> findDefaultUserForEmailAssignmentByEmail(String email) {
		return getHibernateTemplate().find("from users where emal = ?", email);
	}

	@SuppressWarnings("unchecked")
	public List<Space> findNISSpace() {
		return getHibernateTemplate().find("from spaces where prefix_code = 'DEV'");
	}

	@SuppressWarnings("unchecked")
	public List<SpaceCategory> findSpaceCategoriesBySpace(Space space) {
		return getHibernateTemplate().find("from SpaceCategory where space_id = ?", space.getId());
	}
	@SuppressWarnings("unchecked")
	public List<ZSpaceEmail> findAllMails() {
		return getHibernateTemplate().find("from ZSpaceEmail");
	}

	@SuppressWarnings("unchecked")
	public List<Category> findCategoryByDescription(String description) {
		return getHibernateTemplate().find("from Category where description = ?", description);
	}
	
	public void storeSpaceCategory(SpaceCategory spaceCategory) {
		getHibernateTemplate().merge(spaceCategory);
	}

	@SuppressWarnings("unchecked")
	public List<Level> findLevels(int level) {
		return getHibernateTemplate().find("from Level where level=?", level);
	}

	@SuppressWarnings("unchecked")
	public List<UserSpaceRole> loadUserSpaceRole(User user, Space space) {
        return getHibernateTemplate().find("from UserSpaceRole where user_id=? AND space_id=?", new Object[] {user.getId(), space.getId()});
    }

	@SuppressWarnings("unchecked")
	public List<SpaceUserLevel> getSpaceUserLevel(Space space, User user) {
		return getHibernateTemplate().find("from SpaceUserLevel where space_id=? and user_id=?", new Object[] {space.getId(), user.getId()});
	}
	
	public void removeSpaceUserLevel(Space space, User user) {
		List<SpaceUserLevel> spaceUserLevels = getSpaceUserLevel(space, user);
		if(!spaceUserLevels.isEmpty() && spaceUserLevels != null) {
			getHibernateTemplate().delete(spaceUserLevels.get(0));
		}
	}

	public void removeSpaceCategory(Space space) {
		List<SpaceCategory> spaceCategories = new ArrayList<SpaceCategory>();		
		spaceCategories = findSpaceCategoriesBySpace(space);		
		getHibernateTemplate().deleteAll(spaceCategories);
	}

	public void deleteCategory(Category category) {
		removeItems(category);
		removeSpaceCategories(category);
		getHibernateTemplate().delete(category);
	}

	public void saveCategory(Category category) {
		getHibernateTemplate().merge(category);
	}

	@SuppressWarnings("unchecked")
	public void removeItems(Category category) {
		List<Item> items = new ArrayList<Item>();
		List<History> histories = new ArrayList<History>();
		List<Long> ids = new ArrayList<Long>(); 
		
		items = getHibernateTemplate().find("from Item where category.id = ?", category.getId());
		
		for(Item item : items) {
			ids.add(item.getId());
		}
		
		
		if(!ids.isEmpty() && ids != null) {
			histories = getHibernateTemplate().findByNamedParam("from History where parent.id in (:ids)", "ids", ids);
			getHibernateTemplate().deleteAll(histories);
		}
		
		removeAlertLogs(items);
		removeEscalationLogs(items);
		removeItemUsers(items);
		
		getHibernateTemplate().deleteAll(items);
	}

	public void removeSpaceCategories(Category category) {
		List<SpaceCategory> spaceCategories = new ArrayList<SpaceCategory>();
		spaceCategories = findSpaceCategories(category);
		getHibernateTemplate().deleteAll(spaceCategories);
	}

	@SuppressWarnings("unchecked")
	public List<Item> findItems(Category category) {
		return getHibernateTemplate().find("from Item where category.id = ?", category.getId());
	}

	@SuppressWarnings("unchecked")
	public List<SpaceCategory> findSpaceCategories(Category category) {
		return getHibernateTemplate().find("from SpaceCategory where category.id = ?", category.getId());
	}

	@SuppressWarnings("unchecked")
	public void removeAlertLogs(List<Item> items) {
		List<AlertLog> alertLogs = new ArrayList<AlertLog>();
		List<Long> ids = new ArrayList<Long>();
		
		for(Item item : items) {
			ids.add(item.getId());
		}
		
		if(!ids.isEmpty() && ids != null) {
			alertLogs = getHibernateTemplate().findByNamedParam("from AlertLog where item.id in (:ids)", "ids", ids);
			getHibernateTemplate().deleteAll(alertLogs);
		}
	}

	@SuppressWarnings("unchecked")
	public void removeEscalationLogs(List<Item> items) {
		List<EscalationLog> escalationLogs = new ArrayList<EscalationLog>();
		List<Long> ids = new ArrayList<Long>();
		
		for(Item item : items) {
			ids.add(item.getId());
		}
	
		if(!ids.isEmpty() && ids != null) {
			escalationLogs = getHibernateTemplate().findByNamedParam("from EscalationLog where item.id in (:ids)", "ids", ids);		
			getHibernateTemplate().deleteAll(escalationLogs);
		}
	}

	@SuppressWarnings("unchecked")
	public List<Space> findSpaces(User user) {
		List<UserSpaceRole> userSpaceRoles = new ArrayList<UserSpaceRole>();
		userSpaceRoles = getHibernateTemplate().find("from UserSpaceRole where user.id = ? order by space.name ASC", user.getId());
		
		Map<Long, Space> spaceMap = new HashMap<Long, Space>();
		
		for(UserSpaceRole userSpaceRole : userSpaceRoles) {
			spaceMap.put(userSpaceRole.getSpace().getId(), userSpaceRole.getSpace());
		}
		
		return new ArrayList<Space>(spaceMap.values());
	}

	@SuppressWarnings("unchecked")
	public List<ZSpaceEmail> findSpaceEmail(Space space) {
		return getHibernateTemplate().find("from ZSpaceEmail where space_id = ?", space.getId());
	}

	public void storeSpaceEmail(ZSpaceEmail spaceEmail) {
		getHibernateTemplate().merge(spaceEmail);
	}

	@SuppressWarnings("unchecked")
	public List<User> findUsers(long id) {
		return getHibernateTemplate().find("from User where id = ?", id);
	}

	@SuppressWarnings("unchecked")
	public List<Item> findAllItems() {
		return getHibernateTemplate().find("from Item");
	}

	public void removeSpaceEmail(Space space) {
		List<ZSpaceEmail> spaceEmails = new ArrayList<ZSpaceEmail>();
		spaceEmails = findSpaceEmail(space);
		
		getHibernateTemplate().deleteAll(spaceEmails);
	}
	
	@SuppressWarnings("unchecked")
	public List<Item> findItems(Space space) {
		return getHibernateTemplate().find("from Item where space.id = ?", space.getId());
	}

	
	@SuppressWarnings("unchecked")
	public void removeItemUsers(List<Item> items) {
		List<Long> ids = new ArrayList<Long>();
		
		for(Item item : items) {
			ids.add(item.getId());
		}

		if(!ids.isEmpty() && ids != null) {
			List<ItemUser> itemUsers = new ArrayList<ItemUser>();
			itemUsers = getHibernateTemplate().findByNamedParam("from ItemUser where item.id in (:ids)", "ids", ids);
			
			getHibernateTemplate().deleteAll(itemUsers);
		}
	}

}