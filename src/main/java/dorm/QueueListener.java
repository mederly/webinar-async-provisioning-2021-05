package dorm;

import org.apache.activemq.artemis.api.core.ActiveMQInterruptedException;
import org.apache.activemq.artemis.api.core.ActiveMQObjectClosedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.*;
import javax.naming.InitialContext;
import java.lang.IllegalStateException;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static dorm.LoggerUtil.prefix;

/**
 * Listens for messages in the queue.
 *
 * Responsibilities:
 *
 * - Connects to the broker and the queue.
 * - Receives messages and passes them to the message processor.
 */
class QueueListener {

    private static final Logger logger = LoggerFactory.getLogger(QueueListener.class);

    static final String CONNECTION_FACTORY = "localhostConnectionFactory";
    static final String BROKER_USERNAME = "admin";
    static final String BROKER_PASSWORD = "admin123";
    static final String QUEUE_NAME = "DormitoryRequestsQueue";

    private static final Set<QueueListener> LISTENERS = ConcurrentHashMap.newKeySet();

    private final MessageProcessor messageProcessor;
    private String name;
    private Connection connection;

    private QueueListener(MessageProcessor messageProcessor) {
        this.messageProcessor = messageProcessor;
        LISTENERS.add(this);
    }

    static void start(MessageProcessor messageProcessor) {
        QueueListener listener = new QueueListener(messageProcessor);
        Thread listenerThread = new Thread(listener::listen);
        listenerThread.setDaemon(true);
        listenerThread.start();
        logger.info("Listening thread {} started", listenerThread.getName());
    }

    private void listen() {
        name = "Listener in " + Thread.currentThread().getName();
        MessageConsumer consumer;
        try {
            InitialContext ic = new InitialContext();
            ConnectionFactory cf = (ConnectionFactory) ic.lookup(CONNECTION_FACTORY);
            Queue provisioningQueue = (Queue) ic.lookup(QUEUE_NAME);
            connection = cf.createConnection(BROKER_USERNAME, BROKER_PASSWORD);
            connection.start();

            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            consumer = session.createConsumer(provisioningQueue);
        } catch (Exception e) {
            throw new IllegalStateException("Couldn't connect to the broker: " + e.getMessage(), e);
        }

        try {
            for (;;) {
                Message message = consumer.receive();
                if (message == null) {
                    continue;
                }
                if (!(message instanceof TextMessage)) {
                    logger.warn("Ignoring unknown message type: {}", message);
                    continue;
                }
                String body = ((TextMessage) message).getText();
                logger.info("Received a message:\n{}", prefix(body));
                messageProcessor.process(body);
            }
        } catch (javax.jms.IllegalStateException e) {
            if (e.getCause() instanceof ActiveMQInterruptedException) {
                logger.info("Listening was interrupted, exiting.");
            } else if (e.getCause() instanceof ActiveMQObjectClosedException) {
                logger.info("The connection was closed, exiting.");
            } else {
                throw new IllegalStateException("Couldn't receive message from the queue: " + e.getMessage(), e);
            }
        } catch (JMSException e) {
            throw new IllegalStateException("Couldn't receive message from the queue: " + e.getMessage(), e);
        }
    }

    @Override
    public String toString() {
        return "QueueListener{" +
                "name='" + name + '\'' +
                '}';
    }

    private void stop() {
        try {
            logger.info("Stopping {}", this);
            connection.stop();
            connection.close();
        } catch (JMSException jmsException) {
            jmsException.printStackTrace();
        }
    }

    static void stopAll() {
        for (Iterator<QueueListener> iterator = LISTENERS.iterator(); iterator.hasNext(); ) {
            QueueListener listener = iterator.next();
            listener.stop();
            iterator.remove();
        }
    }
}
