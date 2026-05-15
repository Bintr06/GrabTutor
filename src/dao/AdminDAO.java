package dao;

import database.DBConnection;
import java.sql.*;
import java.util.*;

public class AdminDAO {
    public List<Map<String, Object>> getPendingTutors() {
        List<Map<String, Object>> list = new ArrayList<>();
        String sql = "SELECT u.user_id, u.full_name, t.experience, t.price_per_hour " +
                     "FROM Tutors t JOIN Users u ON t.tutor_id = u.user_id " +
                     "WHERE t.is_approved = FALSE";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Map<String, Object> map = new HashMap<>();
                map.put("id", rs.getInt("user_id"));
                map.put("name", rs.getString("full_name"));
                map.put("exp", rs.getString("experience"));
                map.put("price", rs.getBigDecimal("price_per_hour"));
                list.add(map);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public boolean approveTutor(int tutorId) {
        String sql = "UPDATE Tutors SET is_approved = TRUE WHERE tutor_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, tutorId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { return false; }
    }

    public Map<String, Integer> getSystemStats() {
        Map<String, Integer> stats = new HashMap<>();
        String sql = "SELECT role, COUNT(*) as count FROM Users GROUP BY role";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                stats.put(rs.getString("role"), rs.getInt("count"));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return stats;
    }

    public List<Map<String, String>> getAllUsersWithRole() {
    List<Map<String, String>> list = new ArrayList<>();
    String sql = "SELECT full_name, username, role, phone FROM Users ORDER BY role DESC";
    try (Connection conn = DBConnection.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            Map<String, String> user = new HashMap<>();
            user.put("name", rs.getString("full_name"));
            user.put("username", rs.getString("username"));
            user.put("role", rs.getString("role"));
            user.put("phone", rs.getString("phone"));
            list.add(user);
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }
    return list;
}
public List<Map<String, String>> getAllTutors() {
    List<Map<String, String>> list = new ArrayList<>();
    String sql = "SELECT u.full_name, u.username, u.password, u.phone, t.status FROM Users u " + 
                 "JOIN Tutors t ON u.user_id = t.tutor_id WHERE u.role = 'TUTOR'";
    try (Connection conn = DBConnection.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            Map<String, String> m = new HashMap<>();
            m.put("name", rs.getString("full_name"));
            m.put("username", rs.getString("username"));
            m.put("password", rs.getString("password"));
            m.put("phone", rs.getString("phone"));
            m.put("status", rs.getString("status"));
            list.add(m);
        }
    } catch (SQLException e) { e.printStackTrace(); }
    return list;
}
public boolean updateUserInfoByUsername(String fullName, String newName) {
    String sql = "UPDATE Users SET full_name = ? WHERE username = ?";
    
    try (Connection conn = DBConnection.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {
        
        ps.setString(1, newName); 
        ps.setString(2, fullName);    
        
        int rowsAffected = ps.executeUpdate();
        return rowsAffected > 0;
        
    } catch (SQLException e) {
        e.printStackTrace();
        return false;
    }
}
public List<Map<String, String>> getAllStudents() {
    List<Map<String, String>> list = new ArrayList<>();
    String sql = "SELECT full_name, username, password, phone FROM Users WHERE role = 'STUDENT'";
    try (Connection conn = DBConnection.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            Map<String, String> m = new HashMap<>();
            m.put("name", rs.getString("full_name"));
            m.put("username", rs.getString("username"));
            m.put("password", rs.getString("password"));
            m.put("phone", rs.getString("phone"));
            list.add(m);
        }
    } catch (SQLException e) { e.printStackTrace(); }
    return list;
}

public boolean updatePasswordByUsername(String username, String newPassword) {
    String sql = "UPDATE Users SET password = ? WHERE username = ?";
    
    try (Connection conn = DBConnection.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {
        
        ps.setString(1, newPassword);
        ps.setString(2, username);   
        
        return ps.executeUpdate() > 0;
        
    } catch (SQLException e) {
        System.err.println("Lỗi cập nhật mật khẩu: " + e.getMessage());
        return false;
    }
}
}

