package org.thevlad.web.esignature.model;

import java.time.LocalDateTime;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;

import org.thevlad.web.esignature.service.EsignProviderType;

@Entity
public class EsignInfo {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	private EsignProviderType esignProviderType;
	private String docName;
	private String envelopeId;
	private String signHereJson;
	private LocalDateTime signedAt;
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "doc_id", nullable = false)
    private Edocument document;
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public EsignProviderType getEsignProviderType() {
		return esignProviderType;
	}

	public void setEsignProviderType(EsignProviderType esignProviderType) {
		this.esignProviderType = esignProviderType;
	}

	public String getDocName() {
		return docName;
	}

	public void setDocName(String docName) {
		this.docName = docName;
	}

	public String getEnvelopeId() {
		return envelopeId;
	}

	public void setEnvelopeId(String envelopeId) {
		this.envelopeId = envelopeId;
	}

	public String getSignHereJson() {
		return signHereJson;
	}

	public void setSignHereJson(String signHereJson) {
		this.signHereJson = signHereJson;
	}

	public LocalDateTime getSignedAt() {
		return signedAt;
	}

	public void setSignedAt(LocalDateTime signedAt) {
		this.signedAt = signedAt;
	}

	public Edocument getDocument() {
		return document;
	}

	public void setDocument(Edocument document) {
		this.document = document;
	}

}
