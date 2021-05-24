package dorm;

import javafx.collections.ObservableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static dorm.LoggerUtil.prefix;

/**
 * Processes incoming messages.
 *
 * Responsibilities:
 *
 * - parsing (delegated)
 * - execution (delegated)
 * - error reporting
 * - refreshing the view
 */
class UpdatingMessageProcessor implements MessageProcessor {

    private static final Logger logger = LoggerFactory.getLogger(UpdatingMessageProcessor.class);

    private final ObservableList<Student> students;
    private final Runnable viewUpdater;

    UpdatingMessageProcessor(ObservableList<Student> students, Runnable viewUpdater) {
        this.students = students;
        this.viewUpdater = viewUpdater;
    }

    @Override
    public void process(String messageBody) {
        StudentOperation operation;
        try {
            operation = StudentOperation.parse(messageBody);
        } catch (Exception e) {
            logger.warn("Couldn't parse message body:\n{}", prefix(messageBody), e);
            e.printStackTrace();
            return;
        }

        operation.execute(students);
        viewUpdater.run();
    }
}
