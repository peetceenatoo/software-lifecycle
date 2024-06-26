package com.polimi.PPP.CodeKataBattle.Utilities;

import com.polimi.PPP.CodeKataBattle.DTOs.MessageDTO;
import com.polimi.PPP.CodeKataBattle.Exceptions.ErrorInNotificationException;
import com.polimi.PPP.CodeKataBattle.Exceptions.MissingEnvironmentVariableExcpetion;
import jakarta.annotation.PostConstruct;
import jakarta.mail.*;
import jakarta.mail.internet.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Properties;

@Component("emailProvider")
public class EmailProvider implements NotificationProvider{

    @Value("${emailprovider.host}")
    private String host;

    @Value("${emailprovider.port}")
    private Integer port;

    @Value("${emailprovider.username}")
    private  String username;

    @Value("${CKB_SMTP_PASSWORD}")
    private String password;

    @Value("${emailprovider.ssl}")
    private Boolean ssl;
    private Session session;

    @PostConstruct
    public void init() {

        if(password == null || password.isEmpty()){
            throw new MissingEnvironmentVariableExcpetion("Missing SMTP password");
        }

        Properties prop = new Properties();
        prop.put("mail.smtp.auth", true);
        prop.put("mail.smtp.ssl.enable", this.ssl);
        prop.put("mail.smtp.host", this.host);
        prop.put("mail.smtp.port", this.port);

        this.session = Session.getInstance(prop, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });
    }

    public void sendNotification(MessageDTO messageDTO, List<String> destinations){
        new Thread(() -> {
            for(String destination : destinations){
            Message message = new MimeMessage(session);
            try{

                message.setFrom(new InternetAddress("noreply@codekatabattle.it", "CodeKataBattle"));

                message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(destination));

                message.setSubject(messageDTO.getTitle());

                MimeBodyPart mimeBodyPart = new MimeBodyPart();
                mimeBodyPart.setContent(messageDTO.getBody(), "text/html; charset=utf-8");

                Multipart multipart = new MimeMultipart();
                multipart.addBodyPart(mimeBodyPart);

                message.setContent(multipart);

                Transport.send(message);

            }catch (AddressException ex){
                throw new ErrorInNotificationException("Error in parsing sender email");
            }catch (MessagingException | UnsupportedEncodingException ex){
                throw new ErrorInNotificationException("Error in sending email");
            }

            }

        }).start();
    }

    public void sendNotification(MessageDTO messageDTO, String destination) {

        new Thread(() -> {

            Message message = new MimeMessage(session);
            try{

                message.setFrom(new InternetAddress("noreply@codekatabattle.it", "CodeKataBattle"));

                message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(destination));

                message.setSubject(messageDTO.getTitle());

                MimeBodyPart mimeBodyPart = new MimeBodyPart();
                mimeBodyPart.setContent(messageDTO.getBody(), "text/html; charset=utf-8");

                Multipart multipart = new MimeMultipart();
                multipart.addBodyPart(mimeBodyPart);

                message.setContent(multipart);

                Transport.send(message);

            }catch (AddressException ex){
                throw new ErrorInNotificationException("Error in parsing sender email");
            }catch (MessagingException | UnsupportedEncodingException ex){
                throw new ErrorInNotificationException("Error in sending email");
            }

        }).start();

    }
}
