package scheduler.model;
import scheduler.db.ConnectionManager;

import java.sql.*;

public class Appointment {
    private final String Appointment_id;
    private final Date Time;
    private final String Caregivers_name;
    private final String Patient_name;
    private final String Vaccine_name;

    private Appointment(AppointmentBuilder builder) {
        this.Caregivers_name = builder.Caregivers_name;
        this.Appointment_id = builder.Appointment_id;
        this.Patient_name = builder.Patient_name;
        this.Vaccine_name = builder.Vaccine_name;
        this.Time = builder.Time;
    }

    private Appointment(AppointmentGetter getter) {
        this.Caregivers_name = getter.Caregivers_name;
        this.Appointment_id = getter.Appointment_id;
        this.Patient_name = getter.Patient_name;
        this.Vaccine_name = getter.Vaccine_name;
        this.Time = getter.Time;
    }

    public String getAppointment_id() {
        return Appointment_id;
    }
    public String getVaccine_name() {
        return Vaccine_name;
    }
    public String getPatient_name() {
        return Patient_name;
    }
    public String getCaregivers_name() {
        return Caregivers_name;
    }
    public Date getTime() {
        return Time;
    }


    public void saveToDB() throws SQLException {
        ConnectionManager cm = new ConnectionManager();
        Connection con = cm.createConnection();

        String addAppointment = "INSERT INTO Appointment VALUES (?, ?, ?, ?, ?)";
        try {
            PreparedStatement statement = con.prepareStatement(addAppointment);
            statement.setString(1, this.Appointment_id);
            statement.setString(2, this.Caregivers_name );
            statement.setString(3, this.Patient_name);
            statement.setString(4, this.Vaccine_name);
            statement.setDate(5, this.Time);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new SQLException();
        } finally {
            cm.closeConnection();
        }
    }

    public static class AppointmentBuilder {
        private final String Appointment_id;
        private final Date Time;
        private final String Caregivers_name;
        private final String Patient_name;
        private final String Vaccine_name;

        public AppointmentBuilder(String appointment_id,Date Time, String Caregivers_name, String Patient_name, String Vaccine_name) {
            this.Appointment_id = appointment_id;
            this.Time = Time;
            this.Caregivers_name = Caregivers_name;
            this.Patient_name = Patient_name;
            this.Vaccine_name = Vaccine_name;
        }

        public Appointment build() throws  SQLException{
            return new Appointment(this);
        }
    }
    public static class AppointmentGetter {
        private final String Appointment_id;
        private final Date Time;
        private final String Caregivers_name;
        private final String Patient_name;
        private final String Vaccine_name;

        public AppointmentGetter(String appointment_id, Date time, String caregivers_name, String patient_name, String vaccine_name) {
            this.Appointment_id = appointment_id;
            this.Time = time;
            this.Caregivers_name = caregivers_name;
            this.Patient_name = patient_name;
            this.Vaccine_name = vaccine_name;
        }

//        public Appointment get() throws SQLException{
//            ConnectionManager cm = new ConnectionManager();
//            Connection con = cm.createConnection();
//
//            String getVaccine = "SELECT * FROM Appointment WHERE Appointment_id = ?";
//            try {
//                PreparedStatement statement = con.prepareStatement(getVaccine);
//                statement.setInt(1, this.Appointment_id);
//                ResultSet resultSet = statement.executeQuery();
//                while (resultSet.next()) {
//                    this.availableDoses = resultSet.getInt("Doses");
//                    return new Appointment(this);
//                }
//                return null;
//            } catch (SQLException e) {
//                throw new SQLException();
//            } finally {
//                cm.closeConnection();
//            }
//        }
    }


}
