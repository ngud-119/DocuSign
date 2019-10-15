package org.thevlad.web.esignature.service;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.interactive.action.PDAction;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotation;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationLink;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.thevlad.web.esignature.service.EdocTemplatesService.EdocTemplateContent;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import lombok.extern.slf4j.Slf4j;

@Component(TemplateProviderType.TypeConstants.DEALHUB_TYPE_ALIAS)
@Slf4j
public class DealHubEdocTemplatesServiceImpl  implements TemplateProviderType.TemplateContentProvider {

	private static String USER_AGENT = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Ubuntu Chromium/74.0.3729.169 Chrome/74.0.3729.169 Safari/537.36";
    private static int TIMEOUT = 10000;
    private static int MAX_BODY_SIZE = 4096*1024;
	private static final Map<String, String> DEFAULT_HEADERS = new HashMap<String, String>();
    private ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
            .enable(SerializationFeature.INDENT_OUTPUT);

	static {

		DEFAULT_HEADERS.put("User-Agent", USER_AGENT);
		DEFAULT_HEADERS.put("Accept-Language", "en-US,en;q=0.9");
		DEFAULT_HEADERS.put("Accept-Encoding", "gzip, deflate, br");
		DEFAULT_HEADERS.put("DNT", "1");
		DEFAULT_HEADERS.put("Connection", "keep-alive");
		DEFAULT_HEADERS.put("Upgrade-Insecure-Requests", "1");
	}

	@Value("${dealhub.edoctemplate.repository.url}")
	private String templateRepositoryUrl;
	
	@Value("${dealhub.edoctemplate.repository.templateuid}")
	private String templateUid;
	
	@Override
	public EdocTemplateContent getEdocTemplate(String templateName) throws IOException {
		Map<String,String> cookies = new LinkedHashMap<String, String>();
		String url = templateRepositoryUrl + "#!/hub?dealGUID=" + templateName;
		Connection.Response res = Jsoup.connect(url)
				.method(Connection.Method.GET)
				.headers(DEFAULT_HEADERS)
				.header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
				.cookies(cookies)
				.followRedirects(false)
				.timeout(TIMEOUT)
				.execute();
		if (res != null && !res.cookies().isEmpty()) {
			cookies.putAll(res.cookies());
		}
		url = templateRepositoryUrl + "/init";
		res = getJson(cookies, url);
		if (res != null && !res.cookies().isEmpty()) {
			cookies.putAll(res.cookies());
		}
		
		
		url = templateRepositoryUrl + "/dealDocument?dealGUID=" + templateName;
		res = getJson(cookies, url);
		JsonNode rootNode = objectMapper.readTree(res.body());
		
		String docGuid = rootNode.at("/dealDocument/guid").asText();
		url = templateRepositoryUrl + "/getPrintableDocumentToken?dealGUID="+ templateName + "&docGUID=" + docGuid;
		res = getJson(cookies, url);
		
		
		String token = res.body();
		if (token != null && token.startsWith("\"")) {
			token = token.substring(1);
			if (token.endsWith("\"")) {
				token = token.substring(0, token.length()-1);
			}
		}
		url = templateRepositoryUrl + "/getPrintableDocument?token=" + token;
		res = getJson(cookies, url);
		String fileName = null;
		if (res.hasHeader("Content-Disposition")) {
			fileName = extractFileNameFromHeader(res.header("Content-Disposition"));
		}
		byte[] content = IOUtils.toByteArray(res.bodyStream());
		
		EdocTemplateContent edocTemplateContent = new EdocTemplateContent(fileName, content);
		return edocTemplateContent;
	}


	private void saveTemplateContent(String fileName, byte[] content) throws IOException {
		String path = "/storage/tmp/pdf2html/upwork/docusign-dealhub-pdf";
		org.apache.commons.io.FileUtils.writeByteArrayToFile(new File(path, fileName), content);
	}

	private byte[] removeAcivePdfContent(byte[] content) throws IOException {
		byte[] cleaned;
		try (PDDocument doc = PDDocument.load(content)) {
			int pageNum = 0;
			for (PDPage page : doc.getPages()) {
				pageNum++;
				List<PDAnnotation> annotations = page.getAnnotations();
				for (Iterator<PDAnnotation> iterator = annotations.iterator(); iterator.hasNext();) {
					PDAnnotation pdAnnotation = iterator.next();
					if (pdAnnotation instanceof PDAnnotationLink) {
						log.debug("found link: " + pdAnnotation.getContents());
						PDAnnotationLink link = (PDAnnotationLink)pdAnnotation;
//						PDAction action = ((PDAnnotationLink)pdAnnotation).getAction();
//						System.out.println(action.toString());
						link.setAction(null);
					}
				}
				page.setAnnotations(annotations);
			}
			
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			doc.save(baos);
			doc.close();
			cleaned =  baos.toByteArray(); 
		}
		return cleaned;
	}


	private String extractFileNameFromHeader(String header) {
		int pos = header.indexOf("filename=");
		if (pos >= 0) {
			String fileNameAssign = header.substring(pos, header.length());
			String[] keyAndValue = fileNameAssign.split("=");
			String fileName = keyAndValue[1];
			if (fileName != null && fileName.startsWith("\"")) {
				fileName = fileName.substring(1);
				if (fileName.endsWith("\"")) {
					fileName = fileName.substring(0, fileName.length()-1);
				}
			}
			return fileName;
		}
		return null;
	}

	private static Connection.Response getJson(Map<String, String> cookies, String url) throws IOException {
			Connection.Response res = Jsoup.connect(url)
					.method(Connection.Method.GET)
					.headers(DEFAULT_HEADERS)
					.header("X-Requested-With", "XMLHttpRequest")
					.header("Accept", "application/json, text/plain, */*")
					.cookies(cookies)
					.ignoreContentType(true)
					.maxBodySize(MAX_BODY_SIZE)
					.timeout(TIMEOUT).execute();
			return res;
	}
	
}
