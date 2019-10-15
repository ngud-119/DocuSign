package org.thevlad.web.esignature.web;

import java.io.IOException;
import java.util.Arrays;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.servlet.view.RedirectView;
import org.thevlad.web.esignature.docusign.DSConfig;
import org.thevlad.web.esignature.docusign.DocuSignBase;
import org.thevlad.web.esignature.model.Edocument;
import org.thevlad.web.esignature.model.EsignInfo;
import org.thevlad.web.esignature.model.User;
import org.thevlad.web.esignature.repository.EdocumentRepository;
import org.thevlad.web.esignature.repository.EsignInfoRepository;

import com.docusign.esign.api.EnvelopesApi;
import com.docusign.esign.client.ApiClient;
import com.docusign.esign.client.ApiException;
import com.docusign.esign.model.Document;
import com.docusign.esign.model.EnvelopeDefinition;
import com.docusign.esign.model.EnvelopeSummary;
import com.docusign.esign.model.RecipientViewRequest;
import com.docusign.esign.model.Recipients;
import com.docusign.esign.model.SignHere;
import com.docusign.esign.model.Signer;
import com.docusign.esign.model.Tabs;
import com.docusign.esign.model.ViewUrl;
import com.sun.jersey.core.util.Base64;

@Controller
public class DocuSignProcessController {

    private String dsPingUrl; //= config.appUrl + "/";
    private String dsReturnUrl;// = config.appUrl + "/ds-return";


	@Autowired
	private DocuSignBase docuSignBase;

	@Autowired
	private EdocumentRepository edocumentRepository;

	@Autowired
	private EsignInfoRepository eSignInfoRepository;
	
//    @Autowired
//    protected HttpSession session;

    
    @PostConstruct
    public void init(){
        dsPingUrl = DSConfig.DS_APP_URL + "/";
        dsReturnUrl = DSConfig.DS_APP_URL + "/";
    }
    
    @PostMapping("/docuSignProvider/sign")
	public Object sign(ModelMap model, @RequestBody MultiValueMap<String, String> formParams,
			HttpServletResponse response) throws IOException, ApiException {
		String docIdStr = formParams.getFirst("docId");
		
		try {
			Long docId = Long.parseLong(docIdStr);
			Edocument eDocument = edocumentRepository.findById(docId).orElse(null);
			docuSignBase.checkToken();
			String basePath = docuSignBase.getBasePath();
			String redirectUrl = doWork(model, docuSignBase.getToken(), basePath, eDocument);
//			Boolean externalRedirect = redirectUrl != null && redirectUrl.indexOf("redirect:") == 0;
//			String url = redirectUrl.substring(9); // strip 'redirect:'
			RedirectView redirect = new RedirectView(redirectUrl);
			redirect.setExposeModelAttributes(false);
			return redirect;

		} catch (Exception e) {
			populateErrorModel(model, e);
			throw new RuntimeException(e);
		}
	}

	protected String doWork(ModelMap model, String accessToken, String basePath, Edocument edocument)
			throws ApiException, IOException {
// Step 1. Create the envelope definition
		User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		EnvelopeDefinition envelope = makeEnvelope(edocument, currentUser);

// Step 2. Call DocuSign to create the envelope
		ApiClient apiClient = new ApiClient(basePath);
		apiClient.addDefaultHeader("Authorization", "Bearer " + accessToken);
		EnvelopesApi envelopesApi = new EnvelopesApi(apiClient);
		EnvelopeSummary results = envelopesApi.createEnvelope(docuSignBase.getAccountId(), envelope);

		String envelopeId = results.getEnvelopeId();
		
		EsignInfo esignInfo = new EsignInfo();
		esignInfo.setDocument(edocument);
		esignInfo.setDocName(edocument.getName());
		esignInfo.setEnvelopeId(envelopeId);
		esignInfo = eSignInfoRepository.save(esignInfo);
		edocument.setEsignInfo(esignInfo);
		edocumentRepository.save(edocument);
		
//		session.setAttribute("envelopeId", envelopeId);

// Step 3. create the recipient view, the Signing Ceremony
		RecipientViewRequest viewRequest = makeRecipientViewRequest(currentUser, edocument);
// call the CreateRecipientView API
		ViewUrl results1 = envelopesApi.createRecipientView(docuSignBase.getAccountId(), envelopeId, viewRequest);

// Step 4. Redirect the user to the Signing Ceremony
// Don't use an iFrame!
// State can be stored/recovered using the framework's session or a
// query parameter on the returnUrl (see the makeRecipientViewRequest method)
		String redirectUrl = results1.getUrl();
		return redirectUrl;
	}

	private EnvelopeDefinition makeEnvelope(Edocument eDocument, User user) throws IOException {

		EnvelopeDefinition envelopeDefinition = new EnvelopeDefinition();
		envelopeDefinition.setEmailSubject("Please sign this document");
		Document doc1 = new Document();

		String doc1b64 = new String(Base64.encode(eDocument.getContent()));

		doc1.setDocumentBase64(doc1b64);
		doc1.setName(eDocument.getName()); // can be different from actual file name
		doc1.setFileExtension(getFileExtension(eDocument.getFileName()));
		doc1.setDocumentId(String.valueOf(eDocument.getId()));

		// The order in the docs array determines the order in the envelope
		envelopeDefinition.setDocuments(Arrays.asList(doc1));

		// Create a signer recipient to sign the document, identified by name and email
		// We set the clientUserId to enable embedded signing for the recipient
		// We're setting the parameters via the object creation
		Signer signer1 = new Signer();
		signer1.setEmail(user.getEmail());
		signer1.setName(user.getFullName());
		signer1.clientUserId(String.valueOf(user.getId()));
		signer1.recipientId("1");

		// Create signHere fields (also known as tabs) on the documents,
		// We're using anchor (autoPlace) positioning
		//
		// The DocuSign platform seaches throughout your envelope's
		// documents for matching anchor strings.
		SignHere signHere1 = DSConfig.getSignHereForTemplate(eDocument.getTemplate().getName()); 
//		= new SignHere();
//		signHere1.setPageNumber(pageNumber);
//		signHere1.setAnchorString("/sn1/");
//		signHere1.setAnchorUnits("pixels");
//		signHere1.setAnchorYOffset("20");
//		signHere1.setAnchorXOffset("10");

		// Tabs are set per recipient / signer
		Tabs signer1Tabs = new Tabs();
		signer1Tabs.setSignHereTabs(Arrays.asList(signHere1));
		signer1.setTabs(signer1Tabs);

		// Add the recipient to the envelope object
		Recipients recipients = new Recipients();
		recipients.setSigners(Arrays.asList(signer1));
		envelopeDefinition.setRecipients(recipients);

		// Request that the envelope be sent by setting |status| to "sent".
		// To request that the envelope be created as a draft, set to "created"
		envelopeDefinition.setStatus("sent");

		return envelopeDefinition;
	}

	private String getFileExtension(String fileName) {
		int lastDotPos = fileName.lastIndexOf('.');
		if (lastDotPos >= 0) {
			return fileName.substring(lastDotPos);
		}
		return null;
	}

	private RecipientViewRequest makeRecipientViewRequest(User user, Edocument eDocument) {
		// Data for this method
		// signerEmail (argument)
		// signerName (argument)
		// dsReturnUrl (class constant) url on this app that DocuSign will redirect to
		// signerClientId (class constant) the id of the signer in this app
		// dsPingUrl (class constant) optional url in this app that DocuSign signing
		// ceremony should ping

		RecipientViewRequest viewRequest = new RecipientViewRequest();
		// Set the url where you want the recipient to go once they are done signing
		// should typically be a callback route somewhere in your app.
		// The query parameter is included as an example of how
		// to save/recover state information during the redirect to
		// the DocuSign signing ceremony. It's usually better to use
		// the session mechanism of your web framework. Query parameters
		// can be changed/spoofed very easily.
		viewRequest.setReturnUrl(dsReturnUrl + "?docId=" + String.valueOf(eDocument.getId()));

		// How has your app authenticated the user? In addition to your app's
		// authentication, you can include authenticate steps from DocuSign.
		// Eg, SMS authentication
		viewRequest.setAuthenticationMethod("none");

		// Recipient information must match embedded recipient info
		// we used to create the envelope.
		viewRequest.setEmail(user.getEmail());
		viewRequest.setUserName(user.getFullName());
		viewRequest.setClientUserId(String.valueOf(user.getId()));

		// DocuSign recommends that you redirect to DocuSign for the
		// Signing Ceremony. There are multiple ways to save state.
		// To maintain your application's session, use the pingUrl
		// parameter. It causes the DocuSign Signing Ceremony web page
		// (not the DocuSign server) to send pings via AJAX to your
		// app,
		viewRequest.setPingFrequency("600"); // seconds
		// NOTE: The pings will only be sent if the pingUrl is an https address
        viewRequest.setPingUrl(dsPingUrl); // optional setting

		return viewRequest;
	}

	protected void populateErrorModel(ModelMap model, Exception e) {
		model.addAttribute("err", e);
		model.addAttribute("errorCode", e.getCause());
		model.addAttribute("errorMessage", e.getMessage());
	}

}
