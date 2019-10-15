package org.thevlad.web.esignature.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.thevlad.web.esignature.service.TemplateProviderType;

@Entity
@Table(uniqueConstraints = { @UniqueConstraint(columnNames = { "type", "name" }) })
public class EdocTemplate {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private TemplateProviderType type;
	private String name;
	private String fileName;
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public TemplateProviderType getType() {
		return type;
	}

	public void setType(TemplateProviderType type) {
		this.type = type;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String displayName() {
		return type.getProviderName() + "[" + name + "]";
	}
}
