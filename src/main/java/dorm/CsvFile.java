package dorm;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static java.util.Collections.emptyList;

/**
 * Maintains CSV data file.
 *
 * Responsibilities:
 *
 * - parses the content of the CSV file (at application start)
 * - updates the content of the CSV file (when update messages come)
 *
 * Uses OpenCSV.
 */
class CsvFile {

    private static final Logger logger = LoggerFactory.getLogger(CsvFile.class);

    static CsvFile INSTANCE = new CsvFile();

    private static final File FILE = new File("accounts.csv");

    private static final List<String> HEADER = Arrays.asList("login", "givenName", "familyName", "email", "faculty");

    /**
     * @return true if the file exists and has a valid header
     */
    boolean readInto(List<Student> students) throws IOException, CsvValidationException {
        if (!FILE.exists()) {
            logger.debug("No CSV file found, will create one.");
            return false;
        }

        try (CSVReader csvReader = new CSVReader(new FileReader(FILE))) {
            String[] header = csvReader.readNext();
            if (header == null) {
                logger.debug("CSV file is empty, will create one.");
                return false;
            }

            checkHeader(header);

            int records = 0;
            String[] line;
            while ((line = csvReader.readNext()) != null) {
                if (line.length == 5) {
                    Student student = new Student(line[0], line[1], line[2], line[3], line[4]);
                    students.add(student);
                    records++;
                } else {
                    logger.warn("Skipping malformed CSV line: {}", Arrays.asList(line));
                }
            }

            logger.info("{} record(s) from CSV file have been read", records);
            return true;
        }
    }

    private void checkHeader(String[] header) {
        List<String> headerAsList = Arrays.asList(header);
        if (!HEADER.equals(headerAsList)) {
            throw new IllegalStateException("Mismatching header line: " + headerAsList);
        }
    }

    void writeFrom(List<Student> students) throws IOException {
        try (CSVWriter writer = new CSVWriter(new FileWriter(FILE))) {
            writer.writeNext(HEADER.toArray(new String[0]));
            for (Student student : students) {
                writer.writeNext(new String[] {
                        student.getLogin(),
                        student.getGivenName(),
                        student.getFamilyName(),
                        student.getEmail(),
                        student.getFaculty() });
            }
        }
    }
}
