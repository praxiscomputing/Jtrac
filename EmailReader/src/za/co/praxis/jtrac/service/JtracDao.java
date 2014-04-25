package za.co.praxis.jtrac.service;

import java.math.BigDecimal;
import java.util.List;

import za.co.praxis.jtrac.domain.Spaces;
import za.co.praxis.jtrac.domain.Users;
import za.co.praxis.jtrac.domain.ZSpaceSeverityPeriod;

public interface JtracDao {
	List<Users> findUserByEmailAddress(String email);
	List<Users> findDefaultUserForEmailAssignmentByEmail(String email);
	List<Spaces> findNISSpace();
	List<ZSpaceSeverityPeriod> findSpaceSeverityPeriodBySpaceId(long bigDecimal);
}
