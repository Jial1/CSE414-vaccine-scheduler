package scheduler.model;
import scheduler.Availability;
import scheduler.db.ConnectionManager;
import scheduler.util.Util;

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;

public class Patient {
    private final String username;
    private final byte[] salt;
    private final byte[] hash;

    private Patient(PatientBuilder builder) {
        this.username = builder.username;
        this.salt = builder.salt;
        this.hash = builder.hash;
    }

    private Patient(PatientGetter getter) {
        this.username = getter.username;
        this.salt = getter.salt;
        this.hash = getter.hash;
    }


    // Getters
    public String getUsername() {
        return username;
    }

    public byte[] getSalt() {
        return salt;
    }

    public byte[] getHash() {
        return hash;
    }

    public void saveToDB() throws SQLException {
        ConnectionManager cm = new ConnectionManager();
        Connection con = cm.createConnection();

        String addPatient = "INSERT INTO Patients VALUES (? , ?, ?)";
        try {
            PreparedStatement statement = con.prepareStatement(addPatient);
            statement.setString(1, this.username);
            statement.setBytes(2, this.salt);
            statement.setBytes(3, this.hash);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new SQLException();
        } finally {
            cm.closeConnection();
        }
    }

    public void reserved(Vaccine vaccine, Date time, String s) throws SQLException {
        String id = s + time;
        Appointment appointment = new Appointment.AppointmentBuilder(id, time, s, this.username, vaccine.getVaccineName()).build();
        appointment.saveToDB();
        Availability caregiver = new Availability();
        caregiver.decreaseAvailableCaregiver(time, s);
        vaccine.decreaseAvailableDoses(1);
        System.out.println("Reservation successful");
        System.out.println("CaregiverName: "+ s);
        System.out.println("AppointmentID: "+ id);
    }

    public void appointment() throws SQLException {
        ConnectionManager cm = new ConnectionManager();
        Connection con = cm.createConnection();

        String showAppointment = "SELECT Appointment_id, Caregivers_name, Vaccine_name, Time FROM Appointment WHERE Patient_name=? ORDER BY Appointment_id";


        try{
            PreparedStatement statement = con.prepareStatement(showAppointment);
            statement.setString(1, this.username);
            ResultSet resultSet = statement.executeQuery();
            if (!resultSet.isBeforeFirst()) {
                System.out.println("You have no appointment");
                return;
            }
            System.out.println("id "+ " Caregiver" + " Vaccine" + " Time");
            while(resultSet.next()) {
                String id = resultSet.getString("Appointment_id");
                String caregiver_name = resultSet.getString("Caregivers_name");
                String vaccine_name = resultSet.getString("Vaccine_name");
                Date d = resultSet.getDate("Time");
                System.out.println(id + " " + caregiver_name + " " + vaccine_name + " " + d);
            }
        } catch (SQLException e) {
            throw new SQLException();
        } finally {
            cm.closeConnection();
        }
    }


    public static class PatientBuilder {
        private final String username;
        private final byte[] salt;
        private final byte[] hash;

        public PatientBuilder(String username, byte[] salt, byte[] hash) {
            this.username = username;
            this.salt = salt;
            this.hash = hash;
        }

        public Patient build() {
            return new Patient(this);
        }
    }

    public static class PatientGetter {
        private final String username;
        private final String password;
        private byte[] salt;
        private byte[] hash;

        public PatientGetter(String username, String password) {
            this.username = username;
            this.password = password;
        }
        public Patient get() throws SQLException {
            ConnectionManager cm = new ConnectionManager();
            Connection con = cm.createConnection();

            String getPatient = "SELECT Salt, Hash FROM Patients WHERE Username = ?";
            try {
                PreparedStatement statement = con.prepareStatement(getPatient);
                statement.setString(1, this.username);
                ResultSet resultSet = statement.executeQuery();
                while (resultSet.next()) {
                    byte[] salt = resultSet.getBytes("Salt");
                    // we need to call Util.trim() to get rid of the paddings,
                    // try to remove the use of Util.trim() and you'll see :)
                    byte[] hash = Util.trim(resultSet.getBytes("Hash"));
                    // check if the password matches
                    byte[] calculatedHash = Util.generateHash(password, salt);
                    if (!Arrays.equals(hash, calculatedHash)) {
                        return null;
                    } else {
                        this.salt = salt;
                        this.hash = hash;
                        return new Patient(this);
                    }
                }
                return null;
            } catch (SQLException e) {
                throw new SQLException();
            } finally {
                cm.closeConnection();
            }
        }
    }




}
