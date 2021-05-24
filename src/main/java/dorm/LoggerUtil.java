package dorm;

class LoggerUtil {

    static String prefix(String message) {
        return prefix(message, ">> ", ">> ");
    }

    static String prefix(String message, String firstLinePrefix, String nextLinePrefix) {
        StringBuilder sb = new StringBuilder();
        String[] lines = message.split("\n");
        String prefix = firstLinePrefix;
        for (String line : lines) {
            sb.append(prefix).append(line).append("\n");
            prefix = nextLinePrefix;
        }
        return sb.toString();
    }
}
