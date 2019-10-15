package org.thevlad.web.esignature.service;

import java.util.LinkedHashMap;
import java.util.Map;

public enum EsignProviderType {
	DOCUSIGN(TypeConstants.DOCUSIGN_TYPE_ALIAS, TypeConstants.DOCUSIGN_TYPE_NAME);

	private static final Map<String,EsignProviderType> byAlias = new LinkedHashMap<String, EsignProviderType>();
	private final String providerName;
	private final String providerAlias;

	static {
		for (EsignProviderType eSignProviderType : EsignProviderType.values()) {
			byAlias.put(eSignProviderType.providerAlias, eSignProviderType);
		}
	}
	
	EsignProviderType(String providerAlias, String providerName) {
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

	public interface TypeConstants {

		String DOCUSIGN_TYPE_ALIAS = "docuSignProvider";

		String DOCUSIGN_TYPE_NAME = "DocuSign";

	}

	public static EsignProviderType getByAlias(String alias) {
		return byAlias.get(alias);
	}
}
