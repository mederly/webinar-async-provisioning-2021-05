package dorm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.Collection;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Represents an operation on the student list (add/update or delete).
 *
 * Responsibilities:
 *
 * - parsing from the message body
 * - application on the students list
 */
abstract class StudentOperation {

    private static final Logger logger = LoggerFactory.getLogger(StudentOperation.class);

    static final String PREFIX_GIVEN_NAME = "gn: ";
    static final String PREFIX_FAMILY_NAME = "fn: ";
    static final String PREFIX_EMAIL = "m: ";
    static final String PREFIX_FACULTY = "f: ";

    abstract void execute(Collection<Student> students);

    static class AddOrUpdate extends StudentOperation {
        static final String OP_NAME = "addOrUpdate";
        private final Student student;

        AddOrUpdate(Student student) {
            this.student = student;
        }

        @Override
        void execute(Collection<Student> students) {
            if (student.getLogin() == null) {
                throw new IllegalArgumentException("Student without login: " + student);
            }
            Optional<Student> existing = students.stream()
                    .filter(s -> s.getLogin().equals(student.getLogin()))
                    .findFirst();
            if (existing.isPresent()) {
                existing.get().update(student);
            } else {
                students.add(student);
            }
        }

        static AddOrUpdate parse(BufferedReader reader) throws IOException {
            String login = parseLogin(reader);
            Student student = new Student(login);
            parseData(student, reader);
            return new AddOrUpdate(student);
        }

        private static void parseData(Student student, BufferedReader reader) throws IOException {
            for (;;) {
                String line = reader.readLine();
                if (line == null) {
                    return;
                }
                if (!tryParse(line, PREFIX_GIVEN_NAME, student::setGivenName) &&
                        !tryParse(line, PREFIX_FAMILY_NAME, student::setFamilyName) &&
                        !tryParse(line, PREFIX_EMAIL, student::setEmail) &&
                        !tryParse(line, PREFIX_FACULTY, student::setFaculty)) {
                    logger.warn("Ignoring unparseable line: {}", line);
                }
            }
        }

        @SuppressWarnings("BooleanMethodIsAlwaysInverted")
        private static boolean tryParse(String line, String prefix, Consumer<String> setter) {
            if (line.startsWith(prefix)) {
                setter.accept(line.substring(prefix.length()));
                return true;
            } else {
                return false;
            }
        }

        @Override
        public String toString() {
            return "AddOrUpdate: " + student;
        }
    }

    static class Delete extends StudentOperation {
        static final String OP_NAME = "delete";
        private final String login;

        Delete(String login) {
            this.login = login;
        }

        @Override
        void execute(Collection<Student> students) {
            students.removeIf(s -> s.getLogin().equals(login));
        }

        static Delete parse(BufferedReader reader) throws IOException {
            String login = parseLogin(reader);
            return new Delete(login);
        }

        @Override
        public String toString() {
            return "Delete: " + login;
        }
    }

    static StudentOperation parse(String messageBody) throws IOException {
        try (BufferedReader reader = new BufferedReader(new StringReader(messageBody))) {
            String opName = reader.readLine();
            if (AddOrUpdate.OP_NAME.equals(opName)) {
                return AddOrUpdate.parse(reader);
            } else if (Delete.OP_NAME.equals(opName)) {
                return Delete.parse(reader);
            } else {
                throw new IllegalArgumentException("Unsupported operation name: " + opName);
            }
        }
    }

    private static String parseLogin(BufferedReader reader) throws IOException {
        String login = reader.readLine();
        if (login == null || login.isEmpty()) {
            throw new IllegalArgumentException("No login specified in the message");
        }
        return login;
    }
}
