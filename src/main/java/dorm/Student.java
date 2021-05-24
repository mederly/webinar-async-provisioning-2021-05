package dorm;

import javafx.beans.property.SimpleStringProperty;

/**
 * Maintains data about particular student.
 */
@SuppressWarnings("WeakerAccess") // accessed from the framework
public class Student {

    private final SimpleStringProperty login;
    private final SimpleStringProperty givenName;
    private final SimpleStringProperty familyName;
    private final SimpleStringProperty email;
    private final SimpleStringProperty faculty;

    Student(String login, String givenName, String familyName, String email, String faculty) {
        this.login = new SimpleStringProperty(login);
        this.givenName = new SimpleStringProperty(givenName);
        this.familyName = new SimpleStringProperty(familyName);
        this.email = new SimpleStringProperty(email);
        this.faculty = new SimpleStringProperty(faculty);
    }

    Student(String login) {
        this(login, null, null, null, null);
    }

    public String getLogin() {
        return login.get();
    }

    public void setLogin(String login) {
        this.login.set(login);
    }

    public String getGivenName() {
        return givenName.get();
    }

    public void setGivenName(String value) {
        givenName.set(value);
    }

    public String getFamilyName() {
        return familyName.get();
    }

    public void setFamilyName(String value) {
        familyName.set(value);
    }

    public String getEmail() {
        return email.get();
    }

    public void setEmail(String value) {
        email.set(value);
    }

    public String getFaculty() {
        return faculty.get();
    }

    public void setFaculty(String faculty) {
        this.faculty.set(faculty);
    }

    @Override
    public String toString() {
        return "Student{" +
                "login=" + getLogin() +
                ", givenName=" + getGivenName() +
                ", familyName=" + getFamilyName() +
                ", email=" + getEmail() +
                ", faculty=" + getFaculty() +
                '}';
    }

    void update(Student newData) {
        setLogin(newData.getLogin());
        setGivenName(newData.getGivenName());
        setFamilyName(newData.getFamilyName());
        setEmail(newData.getEmail());
        setFaculty(newData.getFaculty());
    }
}
