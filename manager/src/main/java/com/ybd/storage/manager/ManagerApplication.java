/**
 * 
 */
package com.ybd.storage.manager;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Properties;
import java.util.UUID;

import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author Dell
 */
@RestController
@EnableAutoConfiguration
public class ManagerApplication {

	final String rootPath = "C:\\Apache24\\htdocs\\recieved_json\\";
	final String rootPathImages = "C:\\Apache24\\htdocs\\recieved_images\\";

	private static void emailSetupAndSend(final String emailBody) {
		final String fromEmail = "helpline.ybd@gmail.com"; //requires valid gmail id
		final String password = "Spring@2018"; // correct password for gmail id
		final String toEmail = "technical.ybd@gmail.com"; // can be any email id 
		final String ccEmail = "management.ybd@gmail.com";
		
		Properties props = new Properties();
		props.put("mail.smtp.host", "smtp.gmail.com"); //SMTP Host
		props.put("mail.smtp.port", "587"); //TLS Port
		props.put("mail.smtp.auth", "true"); //enable authentication
		props.put("mail.smtp.starttls.enable", "true"); //enable STARTTLS
		
		        //create Authenticator object to pass in Session.getInstance argument
		Authenticator auth = new Authenticator() {
			//override the getPasswordAuthentication method
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(fromEmail, password);
			}
		};
		Session session = Session.getInstance(props, auth);
		
		EmailUtil.sendEmail(session, toEmail,ccEmail,"New Form Submitted", "The submitted form is \n" + emailBody);
	}
	
	@RequestMapping(path = "/sendFormEmail", method = RequestMethod.POST)
	public String sendEmail(final @RequestBody String jsonValue) {
		emailSetupAndSend(jsonValue);
		return "Done";
	}
	
	@RequestMapping(path = "/saveFinalJson", method = RequestMethod.POST)
	public String saveFinalJson(final @RequestBody String jsonValue) {

		try {
			final File jsonFile = new File(rootPath + UUID.randomUUID().toString() + ".json");
			FileWriter fileWriter = new FileWriter(jsonFile, false);
			fileWriter.write(jsonValue);
			fileWriter.close();

		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
		return "Done";
	}

	@PostMapping("/uploadFile")
	public String uploadFile(@RequestParam("file") MultipartFile file) {
		
		String uploadFileName = file.getOriginalFilename();
		String extension = uploadFileName.substring(uploadFileName.lastIndexOf('.'));
		String filePath = rootPathImages+ UUID.randomUUID().toString() + "." + extension;
		try {
			Files.copy(file.getInputStream(),Paths.get(filePath), StandardCopyOption.REPLACE_EXISTING);
			return filePath;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static void main(String[] args) {
		SpringApplication.run(ManagerApplication.class, args);
	}
}
