package org.thevlad.web.esignature.service;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thevlad.web.esignature.model.EdocTemplate;
import org.thevlad.web.esignature.service.TemplateProviderType.TemplateContentProvider;
import org.thevlad.web.esignature.service.TemplateProviderType.TemplateProviderFactory;

@Service
public class EdocTemplatesServiceDefaultImpl implements EdocTemplatesService {

	private TemplateProviderType.TemplateProviderFactory templateProviderFactory;

	@Autowired
	public EdocTemplatesServiceDefaultImpl(TemplateProviderFactory templateProviderFactory) {
		this.templateProviderFactory = templateProviderFactory;
	}

	@Override
	public EdocTemplateContent getEdocTemplate(EdocTemplate edocTemplate) throws IOException {
		TemplateContentProvider templateContentProvider = templateProviderFactory
				.getTemplateProvider(edocTemplate.getType());
		return templateContentProvider.getEdocTemplate(edocTemplate.getName());
	}

}
