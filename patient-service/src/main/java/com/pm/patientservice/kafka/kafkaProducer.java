package com.pm.patientservice.kafka;

import com.pm.patientservice.model.Patient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import patient.events.PatientEvent;

@Component
public class kafkaProducer {

    private static final Logger log = LoggerFactory.getLogger(kafkaProducer.class);
    private final KafkaTemplate<String, byte[]> kafkaTemplate;

    public kafkaProducer(KafkaTemplate<String, byte[]> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendEvent(Patient patient) {
        log.info("===== SENDING EVENT TO KAFKA =====");
        PatientEvent event = PatientEvent.newBuilder()
                .setPatientId(patient.getId().toString())
                .setName(patient.getName())
                .setEmail(patient.getEmail())
                .setEventType("Patient_Created")
                .build();
        log.info("Sending new event to Kafka: {}",patient.getId());
        try{
            kafkaTemplate.send("patient", patient.getId().toString(), event.toByteArray());
        } catch (Exception e) {
            log.error("Error sending Patient created event: {}",event);
        }
    }
}
