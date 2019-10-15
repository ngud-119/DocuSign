package org.thevlad.web.esignature.service;

import java.io.IOException;

import org.thevlad.web.esignature.service.EdocTemplatesService.EdocTemplateContent;

public enum TemplateProviderType {
	DEALHUB(TypeConstants.DEALHUB_TYPE_ALIAS, TypeConstants.DEALHUB_TYPE_NAME),
	LOCAL(TypeConstants.LOCAL_TYPER_ALIAS, TypeConstants.LOCAL_TYPER_NAME);

	private final String providerName;
	private final String providerAlias;

	TemplateProviderType(String providerAlias, String providerName) {
		this.providerAlias = providerAlias;
		this.providerName = providerName;
	}

	@Override
	public String toString() {
		return providerAlias;
	}

	public String getProviderName() {
		return providerName;
	}

	public String getProviderAlias() {
		return providerAlias;
	}

	public interface TemplateProviderFactory {

		TemplateContentProvider getTemplateProvider(TemplateProviderType templateProviderType);

	}

	public interface TemplateContentProvider {

		EdocTemplateContent getEdocTemplate(String templateName) throws IOException;

	}

	public interface TypeConstants {

		String DEALHUB_TYPE_ALIAS = "dealHubTemplateProvider";
		String LOCAL_TYPER_ALIAS = "localFileTemplateProvider";

		String DEALHUB_TYPE_NAME = "Deal Hub";
		String LOCAL_TYPER_NAME = "Local file";

	}

}
