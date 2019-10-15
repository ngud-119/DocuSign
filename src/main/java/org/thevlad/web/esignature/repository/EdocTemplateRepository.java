package org.thevlad.web.esignature.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.thevlad.web.esignature.model.EdocTemplate;
import org.thevlad.web.esignature.service.TemplateProviderType;

public interface EdocTemplateRepository extends JpaRepository<EdocTemplate, Long> {

	List<EdocTemplate> findByType(TemplateProviderType type);

	List<EdocTemplate> findByName(String name);

	EdocTemplate findByTypeAndName(TemplateProviderType type, String name);

}
