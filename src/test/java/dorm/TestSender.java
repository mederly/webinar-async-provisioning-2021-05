package dorm;

import javax.jms.*;
import javax.naming.InitialContext;

import static dorm.QueueListener.*;

class TestSender {

    private static final long DELAY = 3000;

    private static Session session;
    private static MessageProducer producer;

    public static void main(String[] args) throws Exception {
        InitialContext ic = new InitialContext();
        ConnectionFactory cf = (ConnectionFactory) ic.lookup(CONNECTION_FACTORY);
        Connection connection = cf.createConnection(BROKER_USERNAME, BROKER_PASSWORD);
        session = connection.createSession();

        Queue queue = (Queue) ic.lookup(QUEUE_NAME);
        producer = session.createProducer(queue);


        Student jack = new Student("jack", "Jack", "Sparrow", null, "Naval Academy");
        Student bill = new Student("bill", "Bill", "Turner", null, "School of Blacksmithing");
        Student jack2 = new Student("jack", "Jack", "Sparrow", null, "Faculty of Engineering");
        Student jack3 = new Student("jack", "Jack", "Sparrow, PhD.", null, "Faculty of Engineering");
        Student jack4 = new Student("jack", "Jack", "Sparrow, PhD.", "jack.sparrow@evolveum.com", "Faculty of Engineering");

        sendUpdateMessage(jack);
        sendUpdateMessage(bill);
        sendUpdateMessage(jack2);
        sendUpdateMessage(jack3);
        sendUpdateMessage(jack4);

        sendDeleteMessage(jack4.getLogin());
        sendDeleteMessage(bill.getLogin());

        session.close();
    }

    private static void sendUpdateMessage(Student student) throws InterruptedException, JMSException {
        String body = createUpdateBody(student);
        sendMessage(body);
    }

    private static String createUpdateBody(Student student) {
        return StudentOperation.AddOrUpdate.OP_NAME + "\n" + student.getLogin() + "\n" +
                addDataLine(StudentOperation.PREFIX_GIVEN_NAME, student.getGivenName()) +
                addDataLine(StudentOperation.PREFIX_FAMILY_NAME, student.getFamilyName()) +
                addDataLine(StudentOperation.PREFIX_EMAIL, student.getEmail()) +
                addDataLine(StudentOperation.PREFIX_FACULTY, student.getFaculty());
    }

    private static String addDataLine(String prefix, String value) {
        if (value != null) {
            return prefix + value + "\n";
        } else {
            return "";
        }
    }

    private static void sendDeleteMessage(String login) throws InterruptedException, JMSException {
        String body = createDeleteBody(login);
        sendMessage(body);
    }

    private static String createDeleteBody(String login) {
        return StudentOperation.Delete.OP_NAME + "\n" + login;
    }

    private static void sendMessage(String body) throws InterruptedException, JMSException {
        TextMessage message = session.createTextMessage(body);
        producer.send(message);
        if (DELAY > 0) {
            Thread.sleep(DELAY);
        }
    }
}
