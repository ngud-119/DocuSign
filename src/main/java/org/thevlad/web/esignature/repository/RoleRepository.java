package org.thevlad.web.esignature.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.thevlad.web.esignature.model.Role;

public interface RoleRepository extends JpaRepository<Role, Long> {
	
	Role findByName(String name);
	
}
