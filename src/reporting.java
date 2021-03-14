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
                try {
                    access.patientMode();
                } catch (SQLException e) {
                    System.out.println("Failed to retrieve patient information.");
                }
                break;
            case 2:
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

    DatabaseConnection db = new DatabaseConnection();

    public void login(String username, String password) throws Exception {

    }

    public void patientMode() throws SQLException {
        Scanner input = new Scanner(System.in);
        System.out.println("Enter Patient SSN: ");
        String ssn =  input.nextLine();

        String output = new String("Patient SSN: " + rs.getString("ssn")
                                    + "Patient First Name: " + rs.getString("fname")
                                    + "Patient Last Name: " + rs.getString("lname")
                                    + "Patient Address: " + rs.getString("address"));
        rs.close();
        System.out.println(output);


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

}

