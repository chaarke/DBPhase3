import java.util.*;
import java.sql.*;
import java.io.*;

class Reporting {

    /**
     * USE CASES:
     * 	1. users supply login via command line
     * 	2. Action choices
     * 		a. User selects no choice, so the program shows the options and terminates
     * 	3. Report Patient Basic Info
     * 		a. User is prompted to enter the patient's SSN
     * 		b. Program displays the patient info
     * 	4. Report Doctor Basic Info
     * 		a. User is prompted to enter the doctor's ID
     * 		b. Program displays the patient info
     * 	5. Report Admission Info
     * 		a. User is prompted to enter the admission number
     * 		b. Program displays the admission info
     * 	6. Update Admission Payment
     * 		a. User is prompted to enter the admission number
     * 		b. User is prompted to enter the new total payment amount
     * 		c. Program updates the database with the new payment amount
     * @param args
     */

    public static void main(String[] args) {
        Access access = new Access();

        // Process the user login

        String username = args[0];
        String password = args[1];

        try {

            access.login(username, password);

        } catch ( Exception e ) {
            System.out.println("Failed to log in to Oracle. Please try again.");
            return;
        }

        // Process the mode selection

        if (args[2] == null) {
            System.out.print(
                    "1. Report Patient Basic Info\n"
                            + "2. Report Doctor Basic Info\n"
                            + "3. Report Admission Info\n"
                            + "4. Update Admission Payment\n");
            return;
        }

        switch (Integer.parseInt(args[2])) {
            case 1:
                access.patientMode();
                break;
            case 2:
                access.doctorMode();
                break;
            case 3:
                access.admissionMode();
                break;
            case 4:
                access.updateAdmission();
                break;
            default:
                System.out.println("Invalid mode selected. Please enter a number 1-4.");
        }

    }
}

class Access {

    public void login(String username, String password) throws Exception {

    }

    public void patientMode() {
        Scanner input = new Scanner(System.in);
        System.out.println("Enter Patient SSN: ");
        String ssn =  input.nextLine();

    }

    public void doctorMode() {
        Scanner input = new Scanner(System.in);
        System.out.println("Enter Doctor ID: ");
        String id =  input.nextLine();

    }

    public void admissionMode() {
        Scanner input = new Scanner(System.in);
        System.out.println("Enter Admission Number: ");
        String num =  input.nextLine();

    }

    public void updateAdmission() {
        Scanner input = new Scanner(System.in);
        System.out.println("Enter Admission Number: ");
        String num =  input.nextLine();
        System.out.println("Enter new total payment: ");
        String update = input.nextLine();

    }
}

class DatabaseConnection {
    Connection connection;
    PreparedStatement patientInfo, doctorInfo, admissionInfo, roomInfo, examInfo, paymentUpdate;

    public boolean connect(String userid, String password) {
        boolean success = false;
        try (Connection conn = DriverManager.getConnection  ("jdbc:oracle:thin:@csorcl.cs.wpi.edu:1521:orcl",userid, password)) {

            if (conn != null) {
                success = true;
                connection = conn;
                System.out.println("Connected to the database!");
                patientInfo = connection.prepareStatement("SELECT SSN, fName, lName, address FROM Patient WHERE SSN = ?;");
                doctorInfo = connection.prepareStatement("SELECT ID, fName, lName, gender, graduatedFrom, specialty FROM Employee, Doctor WHERE Employee.ID = Doctor.docID AND Doctor.docID = ?;");
                admissionInfo = connection.prepareStatement("SELECT Admid, SSN, StartDate as AdmissionDate, Payment FROM Admission WHERE Admid = ?;");
                roomInfo = connection.prepareStatement("SELECT RoomNum, StartDate as FromDate, EndDate as ToDate FROM RoomStay WHERE Admid = ?;");
                examInfo = connection.prepareStatement("SELECT DoctorID FROM Examinations WHERE Admid = ?;");
                paymentUpdate = connection.prepareStatement("UPDATE Admission SET Payment = ? WHERE Admid = ?;");
            } else {
                success = false;
                System.out.println("Failed to make connection!");
            }

        } catch (SQLException e) {
            System.err.format("SQL State: %s\n%s", e.getSQLState(), e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return success;
    }

    public ResultSet getPatientInfo(String ssn) throws SQLException {
        try {
            if (patientInfo != null) {
                patientInfo.setString(1, ssn);
                return patientInfo.executeQuery();
            } else {
                throw new Exception("No valid Connections open");
            }
        } catch (SQLException e) {
            System.err.format("SQL State: %s\n%s", e.getSQLState(), e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public ResultSet getDoctorInfo(String docID) throws SQLException {
        try {
            if (doctorInfo != null) {
                doctorInfo.setString(1, docID;
                return doctorInfo.executeQuery();
            } else {
                throw new Exception("No valid Connections open");
            }
        } catch (SQLException e) {
            System.err.format("SQL State: %s\n%s", e.getSQLState(), e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public ResultSet getAdmissionInfo(String AdmID) throws SQLException {
        try {
            if (admissionInfo != null) {
                admissionInfo.setString(1, AdmID);
                return admissionInfo.executeQuery();
            } else {
                throw new Exception("No valid Connections open");
            }
        } catch (SQLException e) {
            System.err.format("SQL State: %s\n%s", e.getSQLState(), e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public ResultSet updatePayment(String AdmID, float newPayment) throws SQLException {
        try {
            if (paymentUpdate != null) {
                paymentUpdate.setString(1, AdmID);
                paymentUpdate.setFloat(2,newPayment);
                return paymentUpdate.executeQuery();
            } else {
                throw new Exception("No valid Connections open");
            }
        } catch (SQLException e) {
            System.err.format("SQL State: %s\n%s", e.getSQLState(), e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}

