package org.thevlad.web.esignature.service;

import java.io.IOException;

import org.thevlad.web.esignature.model.Edocument;

public interface EsignDocService {

	Edocument sign(Edocument document, EsignProviderType eSignProviderType) throws IOException;
	
}
