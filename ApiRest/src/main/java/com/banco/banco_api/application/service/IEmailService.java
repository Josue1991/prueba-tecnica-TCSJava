package com.banco.banco_api.application.service;

public interface IEmailService {
    
    /**
     * Envía un correo electrónico con un archivo adjunto
     * 
     * @param destinatario Correo del destinatario
     * @param asunto Asunto del correo
     * @param cuerpo Cuerpo del mensaje
     * @param archivoAdjunto Contenido del archivo como bytes
     * @param nombreArchivo Nombre del archivo adjunto
     * @param tipoMime Tipo MIME del archivo
     */
    void enviarCorreoConAdjunto(String destinatario, String asunto, String cuerpo, 
                                byte[] archivoAdjunto, String nombreArchivo, String tipoMime);
}
