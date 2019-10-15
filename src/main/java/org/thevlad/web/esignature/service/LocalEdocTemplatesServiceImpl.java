package org.thevlad.web.esignature.service;

import java.io.IOException;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.thevlad.web.esignature.docusign.DSHelper;
import org.thevlad.web.esignature.service.EdocTemplatesService.EdocTemplateContent;
import org.thevlad.web.esignature.service.TemplateProviderType.TemplateContentProvider;

@Component(TemplateProviderType.TypeConstants.LOCAL_TYPER_ALIAS)
public class LocalEdocTemplatesServiceImpl implements TemplateContentProvider {

	@Value("#{${local.edoctemplate.map}}")
	private Map<String, String> localTemplatesConfig;

	@Override
	public EdocTemplateContent getEdocTemplate(String templateName) throws IOException {
		String fileName = localTemplatesConfig.get(templateName);
		if (fileName != null) {
			byte[] content = DSHelper.readContent(fileName);
			EdocTemplateContent edocTemplateContent = new EdocTemplateContent(fileName, content);
			return edocTemplateContent;
		} else {
			return null;
		}
	}

}
