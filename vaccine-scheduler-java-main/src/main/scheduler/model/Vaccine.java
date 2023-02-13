package scheduler.model;

import scheduler.db.ConnectionManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class Vaccine {
    private final String vaccineName;
    private int availableDoses;

    private Vaccine(VaccineBuilder builder) {
        this.vaccineName = builder.vaccineName;
        this.availableDoses = builder.availableDoses;
    }

    private Vaccine(VaccineGetter getter) {
        this.vaccineName = getter.vaccineName;
        this.availableDoses = getter.availableDoses;
    }

    // Getters
    public String getVaccineName() {
        return vaccineName;
    }

    public int getAvailableDoses() {
        return availableDoses;
    }

    public void saveToDB() throws SQLException {
        ConnectionManager cm = new ConnectionManager();
        Connection con = cm.createConnection();

        String addDoses = "INSERT INTO vaccines VALUES (?, ?)";
        try {
            PreparedStatement statement = con.prepareStatement(addDoses);
            statement.setString(1, this.vaccineName);
            statement.setInt(2, this.availableDoses);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new SQLException();
        } finally {
            cm.closeConnection();
        }
    }

    // Increment the available doses
    public void increaseAvailableDoses(int num) throws SQLException {
        if (num <= 0) {
            throw new IllegalArgumentException("Argument cannot be negative!");
        }
        this.availableDoses += num;

        ConnectionManager cm = new ConnectionManager();
        Connection con = cm.createConnection();

        String removeAvailability  = "UPDATE vaccines SET Doses = ? WHERE name = ?;";
        try {
            PreparedStatement statement = con.prepareStatement(removeAvailability);
            statement.setInt(1, this.availableDoses);
            statement.setString(2, this.vaccineName);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new SQLException();
        } finally {
            cm.closeConnection();
        }
    }

    // Decrement the available doses
    public void decreaseAvailableDoses(int num) throws SQLException {
        if (this.availableDoses - num < 0) {
            throw new IllegalArgumentException("Not enough available doses!");
        }
        this.availableDoses -= num;
        ConnectionManager cm = new ConnectionManager();
        Connection con = cm.createConnection();

        String removeAvailability  = "UPDATE vaccines SET Doses = ? WHERE name = ?;";
        try {
            PreparedStatement statement = con.prepareStatement(removeAvailability);
            statement.setInt(1, this.availableDoses);
            statement.setString(2, this.vaccineName);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new SQLException();
        } finally {
            cm.closeConnection();
        }
    }


    @Override
    public String toString() {
        return "Vaccine{" +
                "vaccineName='" + vaccineName + '\'' +
                ", availableDoses=" + availableDoses +
                '}';
    }

    public static class VaccineBuilder {
        private final String vaccineName;
        private int availableDoses;

        public VaccineBuilder(String vaccineName, int availableDoses) {
            this.vaccineName = vaccineName;
            this.availableDoses = availableDoses;
        }

        public Vaccine build() throws SQLException {
            return new Vaccine(this);
        }
    }

    public static class VaccineGetter {
        private final String vaccineName;
        private int availableDoses;

        public VaccineGetter(String vaccineName) {
            this.vaccineName = vaccineName;
        }

        public Vaccine get() throws SQLException {
            ConnectionManager cm = new ConnectionManager();
            Connection con = cm.createConnection();

            String getVaccine = "SELECT Name, Doses FROM Vaccines WHERE Name = ?";
            try {
                PreparedStatement statement = con.prepareStatement(getVaccine);
                statement.setString(1, this.vaccineName);
                ResultSet resultSet = statement.executeQuery();
                while (resultSet.next()) {
                    this.availableDoses = resultSet.getInt("Doses");
                    return new Vaccine(this);
                }
                return null;
            } catch (SQLException e) {
                throw new SQLException();
            } finally {
                cm.closeConnection();
            }
        }

        public VaccineGetter(){
            vaccineName = "";
            availableDoses = 0;
        }

        public ArrayList<Vaccine> availability() throws SQLException {
            ConnectionManager cm = new ConnectionManager();
            Connection con = cm.createConnection();

            String availability = "SELECT Name, Doses FROM Vaccines";
            ArrayList<Vaccine> availableDoes = new ArrayList<>();
            try {
                PreparedStatement statement = con.prepareStatement(availability);
                ResultSet resultSet = statement.executeQuery();
                if(resultSet.wasNull()) return null;
                while(resultSet.next()) {
                    String vaccine = resultSet.getString("Name");
                    int availableDoses = resultSet.getInt("Doses");
                    Vaccine vaccines = new Vaccine.VaccineBuilder(vaccine, availableDoses).build();
                    availableDoes.add(vaccines);
                }
                return availableDoes;
            } catch (SQLException e) {
                throw new SQLException();
            } finally {
                cm.closeConnection();
            }
        }

        public boolean checkavailability() throws SQLException {
            ConnectionManager cm = new ConnectionManager();
            Connection con = cm.createConnection();

            String availability = "SELECT Doses FROM Vaccines WHERE Name=?";
            try {
                PreparedStatement statement = con.prepareStatement(availability);
                statement.setString(1, this.vaccineName);
                ResultSet resultSet = statement.executeQuery();
                if(!resultSet.next()) {
                    return false;
                }
//                resultSet.next();
//                int does = resultSet.getInt("Dose");
//                if(does == 0) {
//                    return false;
//                }
            } catch (SQLException e) {
                throw new SQLException();
            } finally {
                cm.closeConnection();
            }
            return true;
        }


    }
}

