package org.thevlad.web.esignature.service;

import java.io.IOException;

import org.thevlad.web.esignature.model.EdocTemplate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

public interface EdocTemplatesService {

	EdocTemplateContent getEdocTemplate(EdocTemplate edocTemplate) throws IOException;
	
	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	public static class EdocTemplateContent {
		private String fileName;
		private byte[] content;
	}
	
}
