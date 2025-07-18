package com.trustsphere.ejb.jms;

import com.trustsphere.core.entity.Transaction;

import jakarta.annotation.Resource;
import jakarta.ejb.EJBException;
import jakarta.ejb.Stateless;
import jakarta.jms.*;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Stateless
@JMSDestinationDefinition(name = "java:global/jms/bank/txn/created", interfaceName = "jakarta.jms.Topic", destinationName = "bank.txn.created")
public class TransactionCreatedPublisher {

    private static final Logger logger = LoggerFactory.getLogger(TransactionCreatedPublisher.class);

    @Inject
    private JMSContext context;

    @Resource(lookup = "java:global/jms/bank/txn/created")
    private Topic topic;

    public void publish(Transaction txn) {
        // context.createProducer().send(topic, txn);
        try {
            ObjectMessage msg = context.createObjectMessage(txn);
            context.createProducer().send(topic, msg);
        } catch (Exception e) {
            //throw new EJBException("JMS publish failed", e);
            logger.error("JMS publish failed :{}", e.getMessage(), e);
        }
    }
}
