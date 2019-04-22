package com.yourbigday.api;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yourbigday.api.entity.FormMappingConfig;

@WebServlet(name = "HelloAppEngine", urlPatterns = { "/hello" })
public class HelloAppEngine extends HttpServlet {

	/**
	 * Logger instance for the servlet
	 */
	private static final Logger LOG = LoggerFactory.getLogger(HelloAppEngine.class);

	private final Map<String, FormMappingConfig> configMap = new HashMap<>();

	/**
	 * 
	 */
	private static final long serialVersionUID = 7410553724L;

	@Override
	public void init() throws ServletException {

		parseCSVInFormMapping();
	}

	private void parseCSVInFormMapping() {

		try {
			final InputStream csvToRead = HelloAppEngine.class.getResourceAsStream("allFormsMapping2.csv");
			final Reader csvReader = new InputStreamReader(csvToRead);
			final CSVParser parser = CSVFormat.DEFAULT.withHeader("HTML NAME", "ELEMENT", "INDEX", "Template Name")
					.parse(csvReader);
			final List<CSVRecord> records = parser.getRecords();
			boolean headerProcessing = true;
			for (final CSVRecord currentRow : records) {

				if (headerProcessing) {
					headerProcessing = false;
					continue;
				}

				final FormMappingConfig currentConfigElement = new FormMappingConfig();

				currentConfigElement.setHtmlElementName(currentRow.get("HTML NAME"));
				currentConfigElement.setArrayIndex(Integer.parseInt(currentRow.get("INDEX")));
				currentConfigElement.setArrayName(currentRow.get("Template Name"));
				configMap.put(currentConfigElement.getHtmlElementName(), currentConfigElement);
			}
		} catch (Exception e) {

			LOG.error("Exception occurred while application initialization, no requests will be processed");
			LOG.error("Exception details are {} ", e);
		}
	}

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

		response.setContentType("text/plain");
		response.setCharacterEncoding("UTF-8");

		response.getWriter().print("Hello App Engine!\r\n");

	}

	private static final String UPLOAD_DIRECTORY = "upload";
	private static final int THRESHOLD_SIZE = 1024 * 1024 * 3; // 3MB
	private static final int MAX_FILE_SIZE = 1024 * 1024 * 40; // 40MB
	private static final int MAX_REQUEST_SIZE = 1024 * 1024 * 50; // 50MB

	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

		final Set<String> configuredElements = configMap.keySet();
		final Map<String, String[]> jsonToSave = new HashMap<>();

		if (ServletFileUpload.isMultipartContent(request)) {
			
			DiskFileItemFactory factory = new DiskFileItemFactory();
			factory.setSizeThreshold(THRESHOLD_SIZE);
			factory.setRepository(new File(System.getProperty("java.io.tmpdir")));

			ServletFileUpload upload = new ServletFileUpload(factory);
			upload.setFileSizeMax(MAX_FILE_SIZE);
			upload.setSizeMax(MAX_REQUEST_SIZE);

			// constructs the directory path to store upload file
			String uploadPath = getServletContext().getRealPath("") + File.separator + UPLOAD_DIRECTORY;
			// creates the directory if it does not exist
			File uploadDir = new File(uploadPath);
			if (!uploadDir.exists()) {
				uploadDir.mkdir();
			}

			List formItems;
			try {
				formItems = upload.parseRequest(request);
				Iterator iter = formItems.iterator();

				// iterates over form's fields
				while (iter.hasNext()) {
					FileItem item = (FileItem) iter.next();
					// processes only fields that are not form fields
					if (!item.isFormField()) {
						String name = item.getName();
						if (name != null && !name.trim().equals("") ) {
							processFIle(configuredElements, jsonToSave, item, name);
						}
					} else {

						final FormMappingConfig configForCurrentElement = configMap.get(item.getFieldName());
						String elementValue = item.getString();

						if (elementValue == null) {
							elementValue = "";
						}

						if ( configForCurrentElement == null ) {
							System.out.println("No config for element name : " + item.getFieldName());
							continue;
						}
						if (jsonToSave.get(configForCurrentElement.getArrayName()) == null) {
							jsonToSave.put(configForCurrentElement.getArrayName(),
									createDefaultArray(configuredElements));
						}
						jsonToSave.get(configForCurrentElement.getArrayName())[configForCurrentElement
								.getArrayIndex()] = elementValue;
					}
				}
				
			} catch ( Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		for (final String currentHTMLElement : configuredElements) {/*

			final FormMappingConfig configForCurrentElement = configMap.get(currentHTMLElement);
			String elementValue = request.getParameter(currentHTMLElement);

			if (elementValue == null) {
				elementValue = "";
			}

			if (jsonToSave.get(configForCurrentElement.getArrayName()) == null) {
				jsonToSave.put(configForCurrentElement.getArrayName(), createDefaultArray(configuredElements));
			}
			jsonToSave.get(configForCurrentElement.getArrayName())[configForCurrentElement
					.getArrayIndex()] = elementValue;
		*/}
//**************
		final ObjectMapper mapper = new ObjectMapper();
		final String stringyfiedJson = mapper.writeValueAsString(jsonToSave);

		final String uri = "http://35.244.38.246/manager/saveFinalJson";
		final RestTemplate restTemplate = new RestTemplate();
		final String result = restTemplate.postForObject(uri, stringyfiedJson, String.class);

		response.setContentType("text/plain");
		response.setCharacterEncoding("UTF-8");
		if ("Done".equalsIgnoreCase(result)) {
			response.getWriter().print("Form submitted successfully! We will send you the sample shortly! Delivery time ranges between 30 minutes to 24 hours depending upon the demand. \r\n");
			final String mailUri = "http://35.244.38.246/manager/sendFormEmail";
			final RestTemplate mailRestTemplate = new RestTemplate();
			final String mailResult = mailRestTemplate.postForObject(mailUri, stringyfiedJson, String.class);
			//response.sendRedirect("https://yourbigday.in");
			//emailSetupAndSend(stringyfiedJson);
			//sendSimpleMail();
		} else {
			response.getWriter()
					.print("Input is not submitted, please retry or connect with support team!\r\n" + stringyfiedJson);
		}
		
	}

	private void processFIle(final Set<String> configuredElements, final Map<String, String[]> jsonToSave,
			FileItem item, String name) throws IOException, Exception {
		String extension = name.substring(name.lastIndexOf('.'));
		File fileToSend = File.createTempFile(UUID.randomUUID().toString(), extension);
		item.write(fileToSend);
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.MULTIPART_FORM_DATA);
		
		MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
		body.add("file", new FileSystemResource(fileToSend));
		
		String serverUrl = "http://35.244.38.246/manager/uploadFile";
		HttpEntity<MultiValueMap<String, Object>> requestEntity
		 = new HttpEntity<>(body, headers);
		
		RestTemplate restTemplate = new RestTemplate();
		ResponseEntity<String> responseEntity = restTemplate.postForEntity(serverUrl, requestEntity, String.class);
		String filePath = responseEntity.getBody();
		FormMappingConfig formMappingConfig = configMap.get(item.getFieldName());
		
		if ( !filePath.equals("") ) {
			
			if (jsonToSave.get(formMappingConfig.getArrayName()) == null) {
				jsonToSave.put(formMappingConfig.getArrayName(),
						createDefaultArray(configuredElements));
			}
			jsonToSave.get(formMappingConfig.getArrayName())[formMappingConfig
					.getArrayIndex()] = filePath;
		}
	}

	
	
	/**
	 * Array initializer
	 * 
	 * @param configuredElements
	 * @return
	 */
	private String[] createDefaultArray(final Set<String> configuredElements) {

		int totalElements = configuredElements.size();
		final String[] arrayToFill = new String[totalElements];

		for (int index = 0; index < totalElements; index++) {
			arrayToFill[index] = "";
		}
		return arrayToFill;
	}
}