package scheduler;

import scheduler.db.ConnectionManager;

import java.sql.*;
import java.util.ArrayList;

public class Availability {

    public static ArrayList<String> getAvailability(Date d) throws SQLException {
        ConnectionManager cm = new ConnectionManager();
        Connection con = cm.createConnection();

        String availability = "SELECT Username FROM Availabilities WHERE Time = ? ORDER BY Username";
        ArrayList<String> availableCaregiver = new ArrayList<>();
        try {
            PreparedStatement statement = con.prepareStatement(availability);
            statement.setDate(1, d);
            ResultSet resultSet = statement.executeQuery();
            while(resultSet.next()) {
                String caregiver = resultSet.getString("Username");
                availableCaregiver.add(caregiver);
            }
            return availableCaregiver;
        } catch (SQLException e) {
            throw new SQLException();
        } finally {
            cm.closeConnection();
        }
    }

    public static void decreaseAvailableCaregiver(Date d, String username) throws SQLException {
        ConnectionManager cm = new ConnectionManager();
        Connection con = cm.createConnection();

        String addAvailability = "DELETE FROM Availabilities WHERE TIME=? AND USERNAME=?";
        try {
            PreparedStatement statement = con.prepareStatement(addAvailability);
            statement.setDate(1, d);
            statement.setString(2, username);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new SQLException();
        } finally {
            cm.closeConnection();
        }
    }
}
