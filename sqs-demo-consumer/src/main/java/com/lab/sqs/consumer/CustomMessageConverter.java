//package com.lab.sqs.consumer;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import org.springframework.messaging.Message;
//import org.springframework.messaging.converter.MappingJackson2MessageConverter;
//import org.springframework.messaging.converter.MessageConversionException;
//
//public class CustomMessageConverter extends MappingJackson2MessageConverter {
//
//    public CustomMessageConverter(ObjectMapper objectMapper) {
//        super(objectMapper);
//    }
//
//    @Override
//    protected Object convertFromInternal(Message<?> message, Class<?> targetClass, Object conversionHint) {
//        try {
//            // Obter o payload da mensagem como JSON
//            String payload = (String) message.getPayload();
//
//            // Usar ObjectMapper para deserializar, ignorando atributos como "JavaType"
//            return getObjectMapper().readValue(payload, targetClass);
//        } catch (Exception e) {
//            throw new MessageConversionException("Erro ao converter mensagem SQS", e);
//        }
//    }
//
//
//}