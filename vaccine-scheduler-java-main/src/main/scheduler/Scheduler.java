package scheduler;

import scheduler.db.ConnectionManager;
import scheduler.model.Caregiver;
import scheduler.model.Patient;
import scheduler.model.Vaccine;
import scheduler.model.Appointment;
import scheduler.util.Util;

import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Locale;

public class Scheduler {

    // objects to keep track of the currently logged-in user
    // Note: it is always true that at most one of currentCaregiver and currentPatient is not null
    //       since only one user can be logged-in at a time
    private static Caregiver currentCaregiver = null;
    private static Patient currentPatient = null;

    public static void main(String[] args) {
        // printing greetings text
        System.out.println();
        System.out.println("Welcome to the COVID-19 Vaccine Reservation Scheduling Application!");
        System.out.println("*** Please enter one of the following commands ***");
        System.out.println("> create_patient <username> <password>");  //TODO: implement create_patient (Part 1)
        System.out.println("> create_caregiver <username> <password>");
        System.out.println("> login_patient <username> <password>");  // TODO: implement login_patient (Part 1)
        System.out.println("> login_caregiver <username> <password>");
        System.out.println("> search_caregiver_schedule <date>");  // TODO: implement search_caregiver_schedule (Part 2)
        System.out.println("> reserve <date> <vaccine>");  // TODO: implement reserve (Part 2)
        System.out.println("> upload_availability <date>");
        System.out.println("> cancel <appointment_id>");  // TODO: implement cancel (extra credit)
        System.out.println("> add_doses <vaccine> <number>");
        System.out.println("> show_appointments");  // TODO: implement show_appointments (Part 2)
        System.out.println("> logout");  // TODO: implement logout (Part 2)
        System.out.println("> quit");
        System.out.println();

        // read input from user
        BufferedReader r = new BufferedReader(new InputStreamReader(System.in));
        while (true) {
            System.out.print("> ");
            String response = "";
            try {
                response = r.readLine();
            } catch (IOException e) {
                System.out.println("Please try again!");
            }
            // split the user input by spaces
            String[] tokens = response.split(" ");
            // check if input exists
            if (tokens.length == 0) {
                System.out.println("Please try again!");
                continue;
            }
            // determine which operation to perform
            String operation = tokens[0];
            if (operation.equals("create_patient")) {
                createPatient(tokens);
            } else if (operation.equals("create_caregiver")) {
                createCaregiver(tokens);
            } else if (operation.equals("login_patient")) {
                loginPatient(tokens);
            } else if (operation.equals("login_caregiver")) {
                loginCaregiver(tokens);
            } else if (operation.equals("search_caregiver_schedule")) {
                searchCaregiverSchedule(tokens);
            } else if (operation.equals("reserve")) {
                reserve(tokens);
            } else if (operation.equals("upload_availability")) {
                uploadAvailability(tokens);
            } else if (operation.equals("cancel")) {
                cancel(tokens);
            } else if (operation.equals("add_doses")) {
                addDoses(tokens);
            } else if (operation.equals("show_appointments")) {
                showAppointments(tokens);
            } else if (operation.equals("logout")) {
                logout(tokens);
            } else if (operation.equals("quit")) {
                System.out.println("Bye!");
                return;
            } else {
                System.out.println("Invalid operation name!");
            }
        }
    }

    private static void createPatient(String[] tokens) {
        if(tokens.length != 3) {
            System.out.println("Failed to create user.");
            return;
        }
        String username = tokens[1];
        String password = tokens[2];


        if (usernameExistsPatient(username)) {
            System.out.println("Username taken, try again!");
            return;
        }
        if(!checkpassword(password)) {
            return;
        }
        byte[] salt = Util.generateSalt();
        byte[] hash = Util.generateHash(password, salt);

            try {
                currentPatient = new Patient.PatientBuilder(username, salt, hash).build();
                // save to Patient information to our database
                currentPatient.saveToDB();
                System.out.println("Created user " + username);
            } catch (SQLException e) {
                System.out.println("Failed to create user.");
                e.printStackTrace();
            }
    }

    private static boolean checkpassword(String password) {
        if(password.length() < 8) {
            System.out.println("password length requires 8");
            return false;
        } else if(password.toLowerCase().equals(password)) {
            System.out.println("password should include upper case or lower case");
            return false;
        } else if(!password.contains("!") && !password.contains("@") && !password.contains("#") && !password.contains("?")) {
            System.out.println("Password should include !, @, #, or ?");
            return false;
        } else {
            return true;
        }
    }


    private static boolean usernameExistsPatient(String username) {
        ConnectionManager cm = new ConnectionManager();
        Connection con = cm.createConnection();

        String selectUsername = "SELECT * FROM Patients WHERE Username = ?";
        try {
            PreparedStatement statement = con.prepareStatement(selectUsername);
            statement.setString(1, username);
            ResultSet resultSet = statement.executeQuery();
            // returns false if the cursor is not before the first record or if there are no rows in the ResultSet.
            return resultSet.isBeforeFirst();
        } catch (SQLException e) {
            System.out.println("Error occurred when checking username");
            e.printStackTrace();
        } finally {
            cm.closeConnection();
        }
        return true;
    }

    private static void createCaregiver(String[] tokens) {
        // create_caregiver <username> <password>
        // check 1: the length for tokens need to be exactly 3 to include all information (with the operation name)
        if (tokens.length != 3) {
            System.out.println("Failed to create user.");
            return;
        }
        String username = tokens[1];
        String password = tokens[2];
        // check 2: check if the username has been taken already
        if (usernameExistsCaregiver(username)) {
            System.out.println("Username taken, try again!");
            return;
        }

        if(!checkpassword(password)) {
            return;
        }

        byte[] salt = Util.generateSalt();
        byte[] hash = Util.generateHash(password, salt);
        // create the caregiver
        try {
            currentCaregiver = new Caregiver.CaregiverBuilder(username, salt, hash).build();
            // save to caregiver information to our database
            currentCaregiver.saveToDB();
            System.out.println("Created user " + username);
        } catch (SQLException e) {
            System.out.println("Failed to create user.");
            e.printStackTrace();
        }
    }

    private static boolean usernameExistsCaregiver(String username) {
        ConnectionManager cm = new ConnectionManager();
        Connection con = cm.createConnection();

        String selectUsername = "SELECT * FROM Caregivers WHERE Username = ?";
        try {
            PreparedStatement statement = con.prepareStatement(selectUsername);
            statement.setString(1, username);
            ResultSet resultSet = statement.executeQuery();
            // returns false if the cursor is not before the first record or if there are no rows in the ResultSet.
            return resultSet.isBeforeFirst();
        } catch (SQLException e) {
            System.out.println("Error occurred when checking username");
            e.printStackTrace();
        } finally {
            cm.closeConnection();
        }
        return true;
    }

    private static void loginPatient(String[] tokens) {
        if (currentCaregiver != null || currentPatient != null) {
            System.out.println("User already logged in.");
            return;
        }
        if(tokens.length != 3) {
            System.out.println("login failed");
            return;
        }
        String username = tokens[1];
        String password = tokens[2];

        Patient patient = null;
        try {
            patient = new Patient.PatientGetter(username, password).get();
        } catch(SQLException e) {
            System.out.println("Login failed.");
            e.printStackTrace();
        }

        if (patient == null) {
            System.out.println("Login failed.");
        } else {
            System.out.println("Logged in as: " + username);
            currentPatient = patient;
        }
    }

    private static void loginCaregiver(String[] tokens) {
        // login_caregiver <username> <password>
        // check 1: if someone's already logged-in, they need to log out first
        if (currentCaregiver != null || currentPatient != null) {
            System.out.println("User already logged in.");
            return;
        }
        // check 2: the length for tokens need to be exactly 3 to include all information (with the operation name)
        if (tokens.length != 3) {
            System.out.println("Login failed.");
            return;
        }
        String username = tokens[1];
        String password = tokens[2];

        Caregiver caregiver = null;
        try {
            caregiver = new Caregiver.CaregiverGetter(username, password).get();
        } catch (SQLException e) {
            System.out.println("Login failed.");
            e.printStackTrace();
        }
        // check if the login was successful
        if (caregiver == null) {
            System.out.println("Login failed.");
        } else {
            System.out.println("Logged in as: " + username);
            currentCaregiver = caregiver;
        }
    }

    private static void searchCaregiverSchedule(String[] tokens) {
        if(currentCaregiver == null && currentPatient == null) {
            System.out.println("Please login first!");
            return;
        }

        if(tokens.length != 2) {
            System.out.println("Please try again!");
            return;
        }

        String date = tokens[1];
        Vaccine.VaccineGetter vaccine = new Vaccine.VaccineGetter();
        Availability caregiver = new Availability();
        ArrayList<Vaccine> availableVaccine = new ArrayList<>();
        ArrayList<String> caregiversName = new ArrayList<>();
        try {
            Date d = Date.valueOf(date);
            availableVaccine =  vaccine.availability();
            caregiversName = caregiver.getAvailability(d);
            if(caregiversName.size() == 0 || availableVaccine.size() ==0 || availableVaccine == null || caregiversName == null) {
                System.out.println("No available schedule");
                return;
            } else{
                System.out.println("Available caregiver in " + date + ":" );
                for(String c: caregiversName) {
                    System.out.println(c);
                }
                System.out.println("Available vaccines:");
                for(Vaccine c: availableVaccine) {
                    System.out.println(c);
                }
            }
        } catch (IllegalArgumentException e) {
            System.out.println("Please enter a valid date!");
        } catch (SQLException e) {
            System.out.println("Error occurred when searching");
            e.printStackTrace();
        }
    }

    private static void reserve(String[] tokens) {
        if(currentPatient == null) {
            System.out.println("Please login as a patient first!");
            return;
        }
        if(tokens.length != 3) {
            System.out.println("Please try again!");
            return;
        }

        String date = tokens[1];
        String vaccineName = tokens[2];

        Boolean availableVaccine = null;
        ArrayList<String> caregiversName = null;
        //Vaccine vaccine = null;
        Availability caregiver = new Availability();

        try {
            Date d = Date.valueOf(date);
            Vaccine.VaccineGetter vaccine = new Vaccine.VaccineGetter(vaccineName);
            caregiversName = caregiver.getAvailability(d);
            availableVaccine =  vaccine.checkavailability();
            if(!availableVaccine) {
                System.out.println("No specified vaccine, please try again");
                return;
            }
            Vaccine get = vaccine.get();
            if(get.getAvailableDoses() == 0) {
                System.out.println("Sorry, we do not have enough available does, please try again");
                return;
            }
            if(caregiversName.size() == 0) {
                System.out.println("No caregiver available");
                return;
            }
            String assigned_caregiver = caregiversName.get(0);
            currentPatient.reserved(get, d, assigned_caregiver);
            //vaccine = new Vaccine.VaccineGetter(vaccineName).get();
        } catch (SQLException e) {
            System.out.println("Error occurred when searching");
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            System.out.println("Wrong date input try again");
            return;
        }
    }

    private static void uploadAvailability(String[] tokens) {
        // upload_availability <date>
        // check 1: check if the current logged-in user is a caregiver
        if (currentCaregiver == null) {
            System.out.println("Please login as a caregiver first!");
            return;
        }
        // check 2: the length for tokens need to be exactly 2 to include all information (with the operation name)
        if (tokens.length != 2) {
            System.out.println("Please try again!");
            return;
        }
        String date = tokens[1];
        try {
            Date d = Date.valueOf(date);
            currentCaregiver.uploadAvailability(d);
            System.out.println("Availability uploaded!");
        } catch (IllegalArgumentException e) {
            System.out.println("Please enter a valid date!");
        } catch (SQLException e) {
            System.out.println("Error occurred when uploading availability");
            e.printStackTrace();
        }
    }

    private static void cancel(String[] tokens) {
        // TODO: Extra credit
    }

    private static void addDoses(String[] tokens) {
        // add_doses <vaccine> <number>
        // check 1: check if the current logged-in user is a caregiver
        if (currentCaregiver == null) {
            System.out.println("Please login as a caregiver first!");
            return;
        }
        // check 2: the length for tokens need to be exactly 3 to include all information (with the operation name)
        if (tokens.length != 3) {
            System.out.println("Please try again!");
            return;
        }

        String vaccineName = tokens[1];
        int doses = Integer.parseInt(tokens[2]);
        Vaccine vaccine = null;
        try {
            vaccine = new Vaccine.VaccineGetter(vaccineName).get();
        } catch (SQLException e) {
            System.out.println("Error occurred when adding doses");
            e.printStackTrace();
        }
        // check 3: if getter returns null, it means that we need to create the vaccine and insert it into the Vaccines
        //          table
        if (vaccine == null) {
            try {
                vaccine = new Vaccine.VaccineBuilder(vaccineName, doses).build();
                vaccine.saveToDB();
            } catch (SQLException e) {
                System.out.println("Error occurred when adding doses");
                e.printStackTrace();
            }
        } else {
            // if the vaccine is not null, meaning that the vaccine already exists in our table
            try {
                vaccine.increaseAvailableDoses(doses);
            } catch (SQLException e) {
                System.out.println("Error occurred when adding doses");
                e.printStackTrace();
            }
        }
        System.out.println("Doses updated!");
    }

    private static void showAppointments(String[] tokens) {
        if(currentCaregiver== null && currentPatient== null) {
            System.out.println("Please login first!");
            return;
        }

        if(tokens.length != 1) {
            System.out.println("Please try again!");
            return;
        }

        ArrayList<Appointment> appointments = new ArrayList<>();
        //Patient appointment = new Patient();

        try {
            if(currentPatient != null) {
                System.out.println("Appointment for "+ currentPatient.getUsername());
                currentPatient.appointment();
            }
            if(currentCaregiver != null) {
                System.out.println("Appointment for "+ currentCaregiver.getUsername());
                currentCaregiver.appointment();
            }

        } catch (IllegalArgumentException e) {
            System.out.println("Please enter a valid input!");
        } catch (SQLException e) {
            System.out.println("Error occurred when showing appointment");
            e.printStackTrace();
        }



    }

    private static void logout(String[] tokens) {
        if(currentCaregiver == null && currentPatient == null) {
            System.out.println("Already logged out");
        } else {
            currentPatient = null;
            currentCaregiver = null;
            System.out.println("logout out successfully!");
        }

    }
}
