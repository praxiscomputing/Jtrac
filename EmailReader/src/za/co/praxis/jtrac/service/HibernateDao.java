package za.co.praxis.jtrac.service;

import java.util.List;

import org.springframework.orm.hibernate3.support.HibernateDaoSupport;
import za.co.praxis.jtrac.domain.Spaces;
import za.co.praxis.jtrac.domain.Users;
import za.co.praxis.jtrac.domain.ZSpaceSeverityPeriod;

public class HibernateDao extends HibernateDaoSupport implements JtracDao {
	
	@SuppressWarnings("unchecked")
	public List<Users> findUserByEmailAddress(String email) {
		return getHibernateTemplate().find("from users where emal = ?", email);
	}

	@SuppressWarnings("unchecked")
	public List<Users> findDefaultUserForEmailAssignmentByEmail(String email) {
		return getHibernateTemplate().find("from users where emal = ?", email);
	}

	@SuppressWarnings("unchecked")
	public List<Spaces> findNISSpace() {
		return getHibernateTemplate().find("from spaces where prefix_code = 'DEV'");
	}
	
	@SuppressWarnings("unchecked")
	public List<ZSpaceSeverityPeriod> findSpaceSeverityPeriodBySpaceId(long spaceId) {
    	return getHibernateTemplate().find("from ZSpaceSeverityPeriod where space.id = ?", spaceId);
    }

}
