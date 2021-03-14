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
        int argC = args.length;
        if (argC < 2)
            return;
        String username = args[0];
        String password = args[1];

        access.login(username, password);

        // Process the mode selection

        if (argC == 2) {
            System.out.print(
                    "1. Report Patient Basic Info\n"
                            + "2. Report Doctor Basic Info\n"
                            + "3. Report Admission Info\n"
                            + "4. Update Admission Payment\n");
            return;
        }

        switch (Integer.parseInt(args[2])) {
            case 1:
                try {
                    access.patientMode();
                } catch (SQLException e) {
                    System.out.println("SQL EXCEPTION: Failed to retrieve patient data.");
                    e.printStackTrace();
                    break;
                }
                break;
            case 2:
                try {
                    access.doctorMode();
                } catch (SQLException e) {
                    System.out.println("SQL EXCEPTION: Failed to retrieve doctor data.");
                    e.printStackTrace();
                    break;
                }
                break;
            case 3:
                try {
                    access.admissionMode();
                } catch (SQLException e) {
                    System.out.println("SQL EXCEPTION: Failed to retrieve admission data.");
                    e.printStackTrace();
                    break;
                }
                break;
            case 4:
                try {
                    access.updateAdmission();
                } catch (SQLException e) {
                    System.out.println("SQL EXCEPTION: Failed to update payment data.");
                    e.printStackTrace();
                    break;
                }
                break;
            default:
                System.out.println("Invalid mode selected. Please enter a number 1-4.");
                break;
        }

    }
}

class Access {

    DatabaseConnection db = new DatabaseConnection();

    public void login(String username, String password) {
        if (db.connect(username, password)) {
            System.out.println("Welcome, " +  username);
        } else {
            System.out.println("Error signing on. Username or password is incorrect.");
        }
    }

    public void patientMode() throws SQLException {
        Scanner input = new Scanner(System.in);
        System.out.println("Enter Patient SSN: ");
        String ssn =  input.nextLine();
        ResultSet ptInfo = db.getPatientInfo(ssn);
        System.out.print("Patient SSN: " + ptInfo.getString("ssn") + "\n"
                        + "Patient First Name: " + ptInfo.getString("fName") + "\n"
                        + "Patient Last Name: " + ptInfo.getString("lName") + "\n"
                        + "Patient Address: " + ptInfo.getString("address") + "\n");
        ptInfo.close();
        db.wrapUp();
    }

    public void doctorMode() throws SQLException {
        Scanner input = new Scanner(System.in);
        System.out.println("Enter Doctor ID: ");
        String id =  input.nextLine();
        ResultSet drInfo = db.getDoctorInfo(id);
        System.out.print("Doctor ID: " + drInfo.getString("id") + "\n"
                + "Doctor First Name: " + drInfo.getString("fName") + "\n"
                + "Doctor Last Name: " + drInfo.getString("lName") + "\n"
                + "Doctor Gender: " + drInfo.getString("gender") + "\n"
                + "Graduated From: " + drInfo.getString("graduatedFrom") + "\n"
                + "Specialty: " + drInfo.getString("specialty") + "\n");
        drInfo.close();
        db.wrapUp();

    }

    public void admissionMode() throws SQLException {
        Scanner input = new Scanner(System.in);
        System.out.println("Enter Admission Number: ");
        String num =  input.nextLine();
        ResultSet admInfo = db.getAdmissionInfo(num);
        System.out.print("Admission ID: " + admInfo.getString("AdmID") + "\n"
                + "Patient SSN: " + admInfo.getString("SSN") + "\n"
                + "Admission Date: " + admInfo.getString("admissionDate") + "\n"
                + "Total Payment: " + admInfo.getString("payment") + "\n");
        admInfo.close();

        ResultSet roomInfo = db.getRoomInfo(num);

        System.out.println("Rooms: ");
        while (roomInfo.next()) {
            System.out.println (roomInfo.getString("RoomNum") + "\t"
                                + roomInfo.getString("FromDate") + "\t"
                                + roomInfo.getString("ToDate") + "\n");
        }
        roomInfo.close();

        ResultSet examInfo = db.getExamInfo(num);
        System.out.println("Doctors who examined the patient in this admission: ");
        while (examInfo.next()) {
            System.out.println("Doctor ID: " + examInfo.getString("DoctorID"));
        }
        examInfo.close();

        db.wrapUp();

    }

    public void updateAdmission() throws SQLException {
        Scanner input = new Scanner(System.in);
        System.out.println("Enter Admission Number: ");
        String num =  input.nextLine();
        System.out.println("Enter new total payment: ");
        float update = input.nextFloat();
        db.updatePayment(num, update);

    }
}

class DatabaseConnection {
    Connection connection;
    PreparedStatement patientInfo, doctorInfo, admissionInfo, roomInfo, examInfo, paymentUpdate;

    public boolean connect(String userid, String password) {
        boolean success = false;
        try (Connection conn = DriverManager.getConnection  ("jdbc:oracle:thin:@oracle.wpi.edu:1521:orcl",userid, password)) {
            Class.forName("oracle.jdbc.driver.OracleDriver");
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

    public ResultSet getRoomInfo(String AdmID) throws SQLException {
        try {
            if (roomInfo != null) {
                roomInfo.setString(1, AdmID);
                return roomInfo.executeQuery();
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

    public ResultSet getExamInfo(String AdmID) throws SQLException {
        try {
            if (examInfo != null) {
                examInfo.setString(1, AdmID);
                return examInfo.executeQuery();
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
                doctorInfo.setString(1, docID);
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

    public void wrapUp() throws SQLException {
        patientInfo.close();
        doctorInfo.close();
        roomInfo.close();
        admissionInfo.close();
        examInfo.close();
        paymentUpdate.close();
        connection.close();
    }

}

