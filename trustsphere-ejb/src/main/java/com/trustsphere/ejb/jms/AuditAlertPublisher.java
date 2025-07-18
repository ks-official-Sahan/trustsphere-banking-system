package com.trustsphere.ejb.jms;

import com.trustsphere.core.entity.AuditLog;

import jakarta.annotation.Resource;
import jakarta.ejb.EJBException;
import jakarta.ejb.Stateless;
import jakarta.jms.*;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Stateless
@JMSDestinationDefinition(
        name = "java:global/jms/audit/alert/high",
        interfaceName = "jakarta.jms.Topic",
        destinationName = "audit.alert.high"
)
public class AuditAlertPublisher {

    private static final Logger logger = LoggerFactory.getLogger(AuditAlertPublisher.class);

    @Inject
    private JMSContext context;

    @Resource(lookup = "java:global/jms/audit/alert/high")
    private Topic topic;

    public void publish(AuditLog log) {
        // context.createProducer().send(topic, log);
        try {
            ObjectMessage msg = context.createObjectMessage(log);
            context.createProducer().send(topic, msg);
        } catch (Exception e) {
            //throw new EJBException("JMS publish failed", e);
            logger.error("JMS publish failed :{}", e.getMessage(), e);
        }
    }
}
