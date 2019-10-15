package org.thevlad.web.esignature.model;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;

@Entity
public class Edocument {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	private String name;
	private String fileName;
	private Status status;
	@ManyToOne
	@JoinColumn(name = "user_id")
	private User owner;
	@ManyToOne
	@JoinColumn(name = "template_id")
	private EdocTemplate template;
	@OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "document")
	private EsignInfo esignInfo;

	@Lob
	private byte[] content;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
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

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	public User getOwner() {
		return owner;
	}

	public void setOwner(User owner) {
		this.owner = owner;
	}

	public byte[] getContent() {
		return content;
	}

	public EdocTemplate getTemplate() {
		return template;
	}

	public void setTemplate(EdocTemplate template) {
		this.template = template;
	}

	public EsignInfo getEsignInfo() {
		return esignInfo;
	}

	public void setEsignInfo(EsignInfo esignInfo) {
		this.esignInfo = esignInfo;
	}

	public void setContent(byte[] content) {
		this.content = content;
	}

	@Override
	public String toString() {
		return "Edocument [id=" + id + ", name=" + name + ", fileName=" + fileName + ", status=" + status + ", owner="
				+ owner + ", template=" + template.displayName() + "]";
	}

	public static enum Status {
		NEW, INPROGRESS, SIGNED
	}
}
