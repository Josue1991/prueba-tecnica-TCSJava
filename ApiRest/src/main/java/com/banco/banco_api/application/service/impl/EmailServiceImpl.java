package com.banco.banco_api.application.service.impl;

import com.banco.banco_api.application.service.IEmailService;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailServiceImpl implements IEmailService {

    private final JavaMailSender mailSender;
    
    @Value("${spring.mail.username:noreply@banco.com}")
    private String remitente;

    public EmailServiceImpl(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    @SuppressWarnings({"null"})
    public void enviarCorreoConAdjunto(String destinatario, String asunto, String cuerpo,
                                      byte[] archivoAdjunto, String nombreArchivo, String tipoMime) {
        try {
            MimeMessage mensaje = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mensaje, true, "UTF-8");
            
            helper.setFrom(remitente);
            helper.setTo(destinatario);
            helper.setSubject(asunto);
            helper.setText(cuerpo, true); // true = HTML
            
            // Adjuntar archivo
            helper.addAttachment(nombreArchivo, 
                () -> new java.io.ByteArrayInputStream(archivoAdjunto), 
                tipoMime);
            
            mailSender.send(mensaje);
        } catch (Exception e) { // Manejo genérico de excepciones de JavaMail
            throw new RuntimeException("Error al enviar correo electrónico: " + e.getMessage(), e);
        }
    }
}
