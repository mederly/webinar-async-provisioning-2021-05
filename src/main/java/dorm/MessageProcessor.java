package dorm;

/**
 * Processes incoming messages.
 */
interface MessageProcessor {
    void process(String messageBody);
}
