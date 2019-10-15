package org.thevlad.web.esignature.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.thevlad.web.esignature.model.Edocument;
import org.thevlad.web.esignature.model.User;

public interface EdocumentRepository extends JpaRepository<Edocument, Long> {

	Page<Edocument> findByOwner(User owner, Pageable pageable);
	
//	List<Edocument> findByStatus(Status status);
	
}
