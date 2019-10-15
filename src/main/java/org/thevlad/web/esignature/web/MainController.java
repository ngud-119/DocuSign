package org.thevlad.web.esignature.web;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.thevlad.web.esignature.docusign.DocuSignBase;
import org.thevlad.web.esignature.model.EdocTemplate;
import org.thevlad.web.esignature.model.Edocument;
import org.thevlad.web.esignature.model.Edocument.Status;
import org.thevlad.web.esignature.model.EsignInfo;
import org.thevlad.web.esignature.model.User;
import org.thevlad.web.esignature.repository.EdocTemplateRepository;
import org.thevlad.web.esignature.repository.EdocumentRepository;
import org.thevlad.web.esignature.repository.EsignInfoRepository;
import org.thevlad.web.esignature.service.EdocTemplatesService;
import org.thevlad.web.esignature.service.EdocTemplatesService.EdocTemplateContent;
import org.thevlad.web.esignature.service.EsignProviderType;

import com.docusign.esign.api.EnvelopesApi;
import com.docusign.esign.client.ApiException;

import lombok.extern.slf4j.Slf4j;

@Controller
@Slf4j
public class MainController {

	@Autowired
	private EdocumentRepository edocumentRepository;

	@Autowired
	private EdocTemplatesService edocTemplatesService;
	
	@Autowired
	private EdocTemplateRepository edocTemplateRepository;

	@Autowired
	private EsignInfoRepository eSignInfoRepository;

	@Autowired
	private DocuSignBase docuSignBase;

	@GetMapping("/")
	public String root(Model model, @RequestParam MultiValueMap<String, String> requestParams) {

		if (requestParams.containsKey("event") && requestParams.getFirst("event").equals("signing_complete")
				&& requestParams.containsKey("docId")) {
			downloadSignedDoc(requestParams.getFirst("docId"));
		}

		fillMyDocs(model, requestParams);
		return "index";
	}

	private void downloadSignedDoc(String docId) {
		Edocument doc = edocumentRepository.findById(Long.parseLong(docId)).get();
		EsignInfo esignInfo = doc.getEsignInfo();
		String envelopeId = esignInfo.getEnvelopeId();
        EnvelopesApi envelopesApi = new EnvelopesApi(docuSignBase.getApiClient());

        // Step 1. EnvelopeDocuments::get.
        // Exceptions will be caught by the calling function
        try {
			byte[] results = envelopesApi.getDocument(docuSignBase.getAccountId(), envelopeId, docId);
			
			doc.setContent(results);
			doc.setStatus(Status.SIGNED);
			
			esignInfo.setSignedAt(LocalDateTime.now());
			
			eSignInfoRepository.save(esignInfo);
			edocumentRepository.save(doc);
			
		} catch (ApiException e) {
			log.error("Error download signed document!",e);
		}
		
	}

	@GetMapping("/login")
	public String login(Model model) {
		return "login";
	}

	@GetMapping("/doclist")
	public String myDocuments(Model model, @RequestParam MultiValueMap<String, String> requestParams) {
		fillMyDocs(model,requestParams);
		return "/doctable";
	}

	@PostMapping("/createdocument")
	public String createDocument(Model model, @RequestParam MultiValueMap<String, String> requestParams) {
		String templateIdStr = requestParams.getFirst("templateId");
		if (templateIdStr != null) {
			Long templateId = Long.valueOf(templateIdStr);
			Edocument eDocument = new Edocument();
			EdocTemplate tmpl = edocTemplateRepository.findById(templateId).orElse(null);
			eDocument.setTemplate(tmpl);
			eDocument.setFileName(tmpl.getFileName());
			eDocument.setName(tmpl.getName());
			eDocument.setStatus(Status.NEW);
			Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
			if (principal instanceof User) {
				eDocument.setOwner((User) principal);
			}
			try {
				EdocTemplateContent edocTemplateContent = edocTemplatesService.getEdocTemplate(tmpl);
				if (tmpl.getFileName()==null && edocTemplateContent.getFileName()!=null) {
					tmpl.setFileName(edocTemplateContent.getFileName());
					edocTemplateRepository.save(tmpl);
				}
				eDocument.setContent(edocTemplateContent.getContent());
			} catch (IOException e) {
				log.error("Error creating document.",e);
			}
			edocumentRepository.save(eDocument);
		}
		fillMyDocs(model, requestParams);
		return "index";
	}
	
	@GetMapping(path = "/downloaddoc")
	public ResponseEntity<Resource> downloadDoc(@RequestParam("docId") Long docId) throws IOException {
		Edocument doc = edocumentRepository.findById(docId).get();
		Edocument.Status status = doc.getStatus();
		if (status != Edocument.Status.SIGNED) {
			throw new RuntimeException("Document # " + doc.getId() + " is not ready yet");
		}
		String filename = doc.getFileName();
		byte[] contents = null;
		contents = doc.getContent();
		Resource resource = new ByteArrayResource(contents);
		return ResponseEntity.ok().contentType(MediaType.parseMediaType("application/pdf"))
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"").body(resource);
	}
	
	private void fillMyDocs(Model model, @RequestParam MultiValueMap<String, String> requestParams) {
		Page<Edocument> docs = Page.empty();
		
        int page = 0; 
        int size = 5;
        
        if (requestParams.getFirst("page") != null && !requestParams.getFirst("page").isEmpty()) {
            page = Integer.parseInt(requestParams.getFirst("page")) - 1;
        }

        if (requestParams.getFirst("size") != null && !requestParams.getFirst("size").isEmpty()) {
            size = Integer.parseInt(requestParams.getFirst("size"));
        }
		
		
		
		Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		if (principal instanceof User) {
			docs = edocumentRepository.findByOwner((User) principal,PageRequest.of(page, size));
		}
		model.addAttribute("docs", docs);
		fillTemplates(model);
		fillEsignProviders(model);
	}
	
	private void fillTemplates(Model model) {
		List<EdocTemplate> templates = edocTemplateRepository.findAll();
		model.addAttribute("templates", templates);
		model.addAttribute("template", templates.get(0));
	}
	
	private void fillEsignProviders(Model model) {
		model.addAttribute("eSignProviders", EsignProviderType.values());
	}
	
}
