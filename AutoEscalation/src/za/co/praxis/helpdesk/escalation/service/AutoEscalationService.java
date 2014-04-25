package za.co.praxis.helpdesk.escalation.service;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.hibernate.FlushMode;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;

import za.co.praxis.helpdesk.escalation.domain.Config;
import za.co.praxis.helpdesk.escalation.domain.Items;
import za.co.praxis.helpdesk.escalation.domain.SpaceUserLevel;
import za.co.praxis.helpdesk.escalation.domain.Spaces;
import za.co.praxis.helpdesk.escalation.domain.Users;
import za.co.praxis.helpdesk.escalation.domain.ZEscalationLogs;
import za.co.praxis.helpdesk.escalation.domain.ZLevels;


public class AutoEscalationService {
	
	private Connection con = null;
	private static SessionFactory sessionFactory;
	private SQLQuery query = null;
	private static Configuration configuration = new Configuration();
	private static Transaction transaction = null;
	private static Session session = null;
	public static String EMAIL_SUBJECT = "Escalation Notification";
	public static String EMAIL_BODY_DESC = "Due date for this support call has elapsed. It has been escalated for your attention.";
	
	
	static {
		configuration.configure("hibernate.cfg.xml");
		sessionFactory = configuration.configure().buildSessionFactory();
		//session = sessionFactory.openSession();
		getCurrentSession().beginTransaction();
	}
	
	public void establishConnection() {
		//sessionFactory = configuration.configure().buildSessionFactory();
		//session = sessionFactory.openSession();
		sessionFactory.openSession();
		getCurrentSession().beginTransaction();
	}
	
	/*public static Session getSession() throws HibernateException {
		return sessionFactory.openSession();
	}*/

	public static Session getCurrentSession() throws HibernateException {
		return sessionFactory.getCurrentSession();
	}
	
	public AutoEscalationService() { }
	
	@SuppressWarnings("unchecked")
	public List<Config> getJTracConfiguration() {
		List<Config> configs = new ArrayList<Config>();
		query = getCurrentSession().createSQLQuery("select * from config");
		query.addEntity(Config.class);
		configs = query.list();
		
		return configs;
	}
	
	
	@SuppressWarnings("unchecked")
	public List<Items> getAllOverDueItems() {
		List<Items> items = new ArrayList<Items>();
		// due_date >= NOW() && status != 99
		query = getCurrentSession().createSQLQuery("select * from items i " +
										"inner join z_categories c on c.id = i.category_id " +
										"inner join spaces s on s.id = i.space_id " +
										"where i.status = 1 AND i.due_date <= GETDATE() " +
										"and c.alert_escalate_yn = 1 and s.alert_escalate_yn = 1");
		
		query.addEntity(Items.class);
		items = query.list();
		
		return items;
	}
	
	
	@SuppressWarnings("unchecked")
	public List<Users> getCurrentAssignedUserByOutStandingItem(Items item) {
		List<Users> users = new ArrayList<Users>();
		query = getCurrentSession().createSQLQuery("select * from SpaceUserLevel where user_id = ? ");
		query.setParameter(0, item.getUsersByAssignedTo());
		query.addEntity(Users.class);
		users = query.list();
		
		return users;
	}
	
	@SuppressWarnings("unchecked")
	public List<ZEscalationLogs> getLastEscalationLogForItem(Items item) {
		List<ZEscalationLogs> escalationLogs = new ArrayList<ZEscalationLogs>();
		query = getCurrentSession().createSQLQuery("select * from z_escalation_logs where id = ( select MAX(id) from z_escalation_logs where item_id = ? )");
		query.setParameter(0, item.getId());
		query.addEntity(ZEscalationLogs.class);
		escalationLogs = query.list();
		
		return escalationLogs;
	}
	
	
	public int getHoursBetweenDates(Date startDate, Date endDate) {
		
		long secs = (startDate.getTime() - endDate.getTime()) / 1000;
		int hours = (int)(secs / 3600);    
		/*secs = secs % 3600;
		int mins = secs / 60;
		secs = secs % 60;*/
		
		return hours;
	}
	
	@SuppressWarnings("unchecked")
	public List<Config> getAllConfigurations() {
		List<Config> configs = new ArrayList<Config>();
		query = getCurrentSession().createSQLQuery("select * from config");
		query.addEntity(Config.class);
		configs = query.list();
		return configs;
	}
	
	@SuppressWarnings("unchecked")
	public List<ZLevels> getLevelBySpaceAndUser(Spaces space, Users user) {
		List<ZLevels> levels = new ArrayList<ZLevels>();
		List<SpaceUserLevel> spaceUserLevels = new ArrayList<SpaceUserLevel>();
		query = getCurrentSession().createSQLQuery("select * from SpaceUserLevel where space_id = ? AND user_id = ?");
		query.setParameter(0, space.getId());
		query.setParameter(1, user.getId());
		query.addEntity(SpaceUserLevel.class);
		spaceUserLevels = query.list();
		

		if(!spaceUserLevels.isEmpty() && spaceUserLevels != null) {
			System.out.println(spaceUserLevels.get(0).getUsers().getName());
			query = getCurrentSession().createSQLQuery("select * from z_levels where id = ?");
			query.setParameter(0, spaceUserLevels.get(0).getZLevels().getId());
			query.addEntity(ZLevels.class);
			levels = query.list();
		}
		return levels;
	}
	
	@SuppressWarnings("unchecked")
	public List<Users> getUserToEscalateToBySpaceAndCurrentLevel(Spaces space, ZLevels level) {
		List<Users> users = new ArrayList<Users>();
		
		List<SpaceUserLevel> spaceUserLevels = new ArrayList<SpaceUserLevel>();
		query = getCurrentSession().createSQLQuery("select sul.* from SpaceUserLevel sul inner join z_levels l on l.id = sul.level_id  where sul.space_id = ? and l.level > ? order by l.level asc");
		query.setParameter(0, space.getId());
		query.setParameter(1, level.getLevel());
		query.addEntity(SpaceUserLevel.class);
		spaceUserLevels = query.list();
		
		if(!spaceUserLevels.isEmpty() && spaceUserLevels != null) {
			
			//query = getCurrentSession().createSQLQuery("select * from users where id IN (:ids)");
			//query = getCurrentSession().createQuery("select * from users where id IN (:ids)");
			Query secondQuery = getCurrentSession().createQuery("from Users u where u.id IN (:ids)");
			//secondQuery.addEntity(Users.class);
			List<BigDecimal> ids = new ArrayList<BigDecimal>();
			
			for(SpaceUserLevel spaceUserLevel : spaceUserLevels) {
				ids.add(spaceUserLevel.getUsers().getId());
			}
			secondQuery.setParameterList("ids", ids);
			
			users = secondQuery.list();
		}
		
		return users;
	}
	
	public void insertEscalationHistory(ZEscalationLogs escalationLog) {
		getCurrentSession().merge(escalationLog);
		//getCurrentSession().getTransaction().commit();
	}
	
}
