import java.util.*;
import java.sql.*;

class Reporting {

    /**
     * USE CASES:
     * 1. users supply login via command line
     * 2. Action choices
     * a. User selects no choice, so the program shows the options and terminates
     * 3. Report Patient Basic Info
     * a. User is prompted to enter the patient's SSN
     * b. Program displays the patient info
     * 4. Report Doctor Basic Info
     * a. User is prompted to enter the doctor's ID
     * b. Program displays the patient info
     * 5. Report Admission Info
     * a. User is prompted to enter the admission number
     * b. Program displays the admission info
     * 6. Update Admission Payment
     * a. User is prompted to enter the admission number
     * b. User is prompted to enter the new total payment amount
     * c. Program updates the database with the new payment amount
     *
     * @param args
     */

    static Connection connection;

    public static void main(String[] args) throws SQLException {
        try {
            // Process the user login
            int argC = args.length;
            if (argC < 2)
                return;
            String username = args[0];
            String password = args[1];

            if (login(username, password)) {
                System.out.println("Welcome, " + username);
                System.out.println("Connected to the database!");

            } else {
                System.out.println("Failed to make connection!");
                System.out.println("Error signing on. Username or password is incorrect.");
            }

            // Process the mode selection

            if (argC == 2) {
                System.out.print(
                        "1. Report Patient Basic Info\n"
                                + "2. Report Doctor Info\n"
                                + "3. Report Admission Info\n"
                                + "4. Update Admission Payment\n");
                return;
            }

            switch (Integer.parseInt(args[2])) {
                case 1:
                    try {
                        patientMode();
                    } catch (SQLException e) {
                        System.out.println("SQL EXCEPTION: Failed to retrieve patient data.");
                        e.printStackTrace();
                        break;
                    }
                    break;
                case 2:
                    try {
                        doctorMode();
                    } catch (SQLException e) {
                        System.out.println("SQL EXCEPTION: Failed to retrieve doctor data.");
                        e.printStackTrace();
                        break;
                    }
                    break;
                case 3:
                    try {
                        admissionMode();
                    } catch (SQLException e) {
                        System.out.println("SQL EXCEPTION: Failed to retrieve admission data.");
                        e.printStackTrace();
                        break;
                    }
                    break;
                case 4:
                    try {
                        updateAdmission();
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

        } catch (Exception e) {
            e.printStackTrace();
        }

        if(!connection.isClosed())
            connection.close();

    }

    public static boolean login(String userid, String password) throws SQLException {
        try  {
            Class.forName("oracle.jdbc.driver.OracleDriver");
            connection = DriverManager.getConnection("jdbc:oracle:thin:@oracle.wpi.edu:1521:orcl", userid, password);
        } catch (SQLException e) {
            System.err.format("SQL State: %s\n%s", e.getSQLState(), e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return !connection.isClosed();
    }


    public static void patientMode() throws Exception {
        try{
            Scanner input = new Scanner(System.in);
            System.out.println("Enter Patient SSN: ");
            String ssn = input.nextLine();
            Statement patientInfo = connection.createStatement();
            if (patientInfo != null) {
                ResultSet ptResult = patientInfo.executeQuery("SELECT SSN, fName, lName, address FROM Patient WHERE SSN = '"+ssn+"'");
                if (ptResult != null) {
                    System.out.println("Reading Patient Info\n");
                    while (ptResult.next()) {
                        System.out.print("Patient SSN: " + ptResult.getString("SSN") + "\n"
                                + "Patient First Name: " + ptResult.getString("fName") + "\n"
                                + "Patient Last Name: " + ptResult.getString("lName") + "\n"
                                + "Patient Address: " + ptResult.getString("address") + "\n\n");
                    }
                }
                ptResult.close();
                patientInfo.close();
            } else {
                throw new Exception("No valid Connections open");
            }
        } catch (SQLException e) {
            System.err.format("SQL State: %s\n%s", e.getSQLState(), e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }
        connection.close();
    }

    public static void doctorMode() throws Exception {
        try{
            Scanner input = new Scanner(System.in);
            System.out.println("Enter Doctor ID: ");
            String id = input.nextLine();
            Statement doctorInfo = connection.createStatement();
            if (doctorInfo != null) {
                ResultSet docSet = doctorInfo.executeQuery("SELECT ID, fName, lName, gender, graduatedFrom, specialty FROM Employee, Doctor WHERE Employee.ID = Doctor.docID AND Doctor.docID = "+id);
                if (docSet != null) {
                    while(docSet.next()) {
                        System.out.print("Doctor ID: " + docSet.getString("id") + "\n"
                                + "Doctor First Name: " + docSet.getString("fName") + "\n"
                                + "Doctor Last Name: " + docSet.getString("lName") + "\n"
                                + "Doctor Gender: " + docSet.getString("gender") + "\n"
                                + "Graduated From: " + docSet.getString("graduatedFrom") + "\n"
                                + "Specialty: " + docSet.getString("specialty") + "\n");
                    }
                    docSet.close();
                }
                doctorInfo.close();
            } else {
                throw new Exception("No valid Connections open");
            }
        } catch (SQLException e) {
            System.err.format("SQL State: %s\n%s", e.getSQLState(), e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }
        connection.close();
    }

    public static void admissionMode() throws Exception {
        try{
            Scanner input = new Scanner(System.in);
            System.out.println("Enter Admission Number: ");
            String num =  input.nextLine();
            Statement statement = connection.createStatement();
            if (statement != null) {
                ResultSet admSet = statement.executeQuery("SELECT Admid, SSN, AdmDate, Payment FROM Admission WHERE Admid = "+num);
                if (admSet != null) {
                    while (admSet.next()){
                        System.out.print("Admission ID: " + admSet.getString("AdmID") + "\n"
                                + "Patient SSN: " + admSet.getString("SSN") + "\n"
                                + "Admission Date: " + admSet.getDate("admDate") + "\n"
                                + "Total Payment: " + admSet.getString("payment") + "\n");
                    }
                    admSet.close();
                }
                System.out.println("Rooms: ");
                ResultSet roomSet = statement.executeQuery("SELECT RoomNum, StartDate as FromDate, EndDate as ToDate FROM RoomStay WHERE Admid = "+num);
                if (roomSet != null) {
                    while (roomSet.next()){
                        System.out.println (roomSet.getString("RoomNum") + "\t"
                                + roomSet.getDate("FromDate") + "\t"
                                + roomSet.getDate("ToDate") + "\n");
                    }
                    roomSet.close();
                }
                System.out.println("Doctors who examined the patient in this admission: ");
                ResultSet examSet = statement.executeQuery("SELECT DoctorID FROM Examinations WHERE Admid = "+num);
                if (examSet != null) {
                    while (examSet.next()){
                        System.out.println("Doctor ID: " + examSet.getString("DoctorID"));
                    }
                    examSet.close();
                }
                statement.close();
            } else {
                throw new Exception("No valid Connections open");
            }
        } catch (SQLException e) {
            System.err.format("SQL State: %s\n%s", e.getSQLState(), e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }
        connection.close();
    }

    public static void updateAdmission() throws SQLException {
        Scanner input = new Scanner(System.in);
        System.out.println("Enter Admission Number: ");
        String num =  input.nextLine();
        System.out.println("Enter new total payment: ");
        float update = input.nextFloat();
        try {
            Statement paymentUpdate = connection.createStatement();
            if (paymentUpdate != null) {
               int rowsUpdated = paymentUpdate.executeUpdate("UPDATE Admission SET Payment = "+update+" WHERE Admid = "+num);
               System.out.println(rowsUpdated +" Rows Updated");
               paymentUpdate.close();
            } else {
                throw new Exception("No valid Connections open");
            }
        } catch (SQLException e) {
            System.err.format("SQL State: %s\n%s", e.getSQLState(), e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }
        connection.close();
    }


}