package dao;

import model.Tutor;
import database.DBConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TutorDAO {
    
    /**
     * Insert a new tutor
     */
    public boolean insertTutor(Tutor tutor) {
        String sql = "INSERT INTO Tutors (tutor_id, price_per_hour, experience, status, province_id) VALUES (?, ?, ?, ?, ?)";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, tutor.getTutorId());
            pstmt.setBigDecimal(2, tutor.getPricePerHour());
            pstmt.setString(3, tutor.getExperience());
            pstmt.setString(4, tutor.getStatus());
            if (tutor.getProvinceId() != null) {
                pstmt.setInt(5, tutor.getProvinceId());
            } else {
                pstmt.setNull(5, Types.INTEGER);
            }
            
            return pstmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            System.err.println("Error inserting tutor: " + e.getMessage());
            return false;
        }
    }

    /**
     * Insert a new tutor inside an existing transaction.
     */
    public boolean insertTutor(Connection conn, Tutor tutor) throws SQLException {
        String sql = "INSERT INTO Tutors (tutor_id, price_per_hour, experience, status, province_id) VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, tutor.getTutorId());
            pstmt.setBigDecimal(2, tutor.getPricePerHour());
            pstmt.setString(3, tutor.getExperience());
            pstmt.setString(4, tutor.getStatus());
            if (tutor.getProvinceId() != null) {
                pstmt.setInt(5, tutor.getProvinceId());
            } else {
                pstmt.setNull(5, Types.INTEGER);
            }
            return pstmt.executeUpdate() > 0;
        }
    }

    public List<String> getAllProvinceNames() {
        List<String> provinceNames = new ArrayList<>();
        String sql = "SELECT province_name FROM Provinces ORDER BY province_name ASC";

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                provinceNames.add(rs.getString("province_name"));
            }
        } catch (SQLException e) {
            System.err.println("Error getting province names: " + e.getMessage());
        }

        return provinceNames;
    }

    public Integer getProvinceIdByName(Connection conn, String provinceName) throws SQLException {
        String sql = "SELECT province_id FROM Provinces WHERE province_name = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, provinceName);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("province_id");
                }
            }
        }

        return null;
    }

    public Integer getProvinceIdByName(String provinceName) {
        if (provinceName == null || provinceName.isBlank()) {
            return null;
        }

        try (Connection conn = DBConnection.getConnection()) {
            return getProvinceIdByName(conn, provinceName);
        } catch (SQLException e) {
            System.err.println("Error getting province id by name: " + e.getMessage());
            return null;
        }
    }

    /**
     * Get all subject names from the Subjects table.
     */
    public List<String> getAllSubjectNames() {
        List<String> subjectNames = new ArrayList<>();
        String sql = "SELECT subject_name FROM Subjects ORDER BY subject_name ASC";

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                subjectNames.add(rs.getString("subject_name"));
            }
        } catch (SQLException e) {
            System.err.println("Error getting subject names: " + e.getMessage());
        }

        return subjectNames;
    }

    /**
     * Link a tutor to one subject inside an existing transaction.
     */
    public boolean insertTutorSubject(Connection conn, int tutorId, int subjectId) throws SQLException {
        String sql = "INSERT INTO Tutor_Subjects (tutor_id, subject_id) VALUES (?, ?)";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, tutorId);
            pstmt.setInt(2, subjectId);
            return pstmt.executeUpdate() > 0;
        }
    }

    /**
     * Resolve a subject name to its ID.
     */
    public Integer getSubjectIdByName(Connection conn, String subjectName) {
    String sql = "SELECT subject_id FROM Subjects WHERE LOWER(subject_name) = LOWER(?)";
    
    // Đảm bảo subjectName không bị null trước khi truy vấn
    if (subjectName == null || subjectName.trim().isEmpty()) return null;

    try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
        pstmt.setString(1, subjectName);
        try (ResultSet rs = pstmt.executeQuery()) {
            if (rs.next()) {
                return rs.getInt("subject_id");
            }
        }
    } catch (SQLException e) {
        // Thay vì quăng lỗi ra ngoài, bạn có thể log lại để dễ debug
        System.err.println("Lỗi khi lấy Subject ID: " + e.getMessage());
    }

    return null; // Không tìm thấy hoặc có lỗi xảy ra
}

    public List<String> getSubjectNamesByTutorId(int tutorId) {
        List<String> subjects = new ArrayList<>();
        String sql = "SELECT s.subject_name FROM Tutor_Subjects ts " +
                "INNER JOIN Subjects s ON ts.subject_id = s.subject_id " +
                "WHERE ts.tutor_id = ? ORDER BY s.subject_name ASC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, tutorId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    subjects.add(rs.getString("subject_name"));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting tutor subjects: " + e.getMessage());
        }

        return subjects;
    }

    public boolean replaceTutorSubjects(int tutorId, List<String> subjectNames) {
        String deleteSql = "DELETE FROM Tutor_Subjects WHERE tutor_id = ?";

        try (Connection conn = DBConnection.getConnection()) {
            boolean oldAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);

            try {
                try (PreparedStatement deleteStmt = conn.prepareStatement(deleteSql)) {
                    deleteStmt.setInt(1, tutorId);
                    deleteStmt.executeUpdate();
                }

                for (String subjectName : subjectNames) {
                    Integer subjectId = getSubjectIdByName(conn, subjectName);
                    if (subjectId == null) {
                        throw new SQLException("Subject not found: " + subjectName);
                    }

                    if (!insertTutorSubject(conn, tutorId, subjectId)) {
                        throw new SQLException("Cannot insert tutor-subject: " + subjectName);
                    }
                }

                conn.commit();
                return true;
            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(oldAutoCommit);
            }
        } catch (SQLException e) {
            System.err.println("Error replacing tutor subjects: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Get tutor by ID
     */
    public Tutor getTutorById(int tutorId) {
        String sql = "SELECT t.*, p.province_name FROM Tutors t " +
            "LEFT JOIN Provinces p ON t.province_id = p.province_id " +
            "WHERE t.tutor_id = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, tutorId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return mapResultSetToTutor(rs);
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting tutor: " + e.getMessage());
        }
        return null;
    }
    
    /**
     * Get tutors by subject
     */
    public List<Tutor> getTutorsBySubject(String subject) {
        return getTutorsBySubjectAndProvince(subject, null);
    }

    public List<Tutor> getTutorsBySubjectAndProvince(String subject, String provinceName) {
        List<Tutor> tutors = new ArrayList<>();
        String sql = "SELECT t.*, p.province_name FROM Tutors t " +
            "INNER JOIN Tutor_Subjects ts ON t.tutor_id = ts.tutor_id " +
            "INNER JOIN Subjects s ON ts.subject_id = s.subject_id " +
            "LEFT JOIN Provinces p ON t.province_id = p.province_id " +
            "WHERE s.subject_name = ? " +
            (provinceName != null && !provinceName.isBlank() ? "AND p.province_name = ? " : "") +
            "ORDER BY t.price_per_hour ASC";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, subject);
            if (provinceName != null && !provinceName.isBlank()) {
                pstmt.setString(2, provinceName);
            }
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                tutors.add(mapResultSetToTutor(rs));
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting tutors by subject and province: " + e.getMessage());
        }
        return tutors;
    }
    
    /**
     * Get tutors by status
     */
    public List<Tutor> getTutorsByStatus(String status) {
        List<Tutor> tutors = new ArrayList<>();
        String sql = "SELECT t.*, p.province_name FROM Tutors t " +
            "LEFT JOIN Provinces p ON t.province_id = p.province_id " +
            "WHERE t.status = ? ORDER BY t.price_per_hour ASC";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, status);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                tutors.add(mapResultSetToTutor(rs));
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting tutors by status: " + e.getMessage());
        }
        return tutors;
    }
    
    /**
     * Get all tutors
     */
    public List<Tutor> getAllTutors() {
        List<Tutor> tutors = new ArrayList<>();
        String sql = "SELECT t.*, p.province_name FROM Tutors t " +
            "LEFT JOIN Provinces p ON t.province_id = p.province_id";
        
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                tutors.add(mapResultSetToTutor(rs));
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting all tutors: " + e.getMessage());
        }
        return tutors;
    }
    
    /**
     * Update tutor
     */
    public boolean updateTutor(Tutor tutor) {
        String sql = "UPDATE Tutors SET price_per_hour = ?, experience = ?, status = ?, province_id = ? WHERE tutor_id = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setBigDecimal(1, tutor.getPricePerHour());
            pstmt.setString(2, tutor.getExperience());
            pstmt.setString(3, tutor.getStatus());
            if (tutor.getProvinceId() != null) {
                pstmt.setInt(4, tutor.getProvinceId());
            } else {
                pstmt.setNull(4, Types.INTEGER);
            }
            pstmt.setInt(5, tutor.getTutorId());
            
            return pstmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            System.err.println("Error updating tutor: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Delete tutor
     */
    public boolean deleteTutor(int tutorId) {
        String sql = "DELETE FROM Tutors WHERE tutor_id = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, tutorId);
            return pstmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            System.err.println("Error deleting tutor: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Map ResultSet to Tutor object
     */
    private Tutor mapResultSetToTutor(ResultSet rs) throws SQLException {
        Tutor tutor = new Tutor();
        tutor.setTutorId(rs.getInt("tutor_id"));
        tutor.setPricePerHour(rs.getBigDecimal("price_per_hour"));
        tutor.setExperience(rs.getString("experience"));
        tutor.setStatus(rs.getString("status"));
        int provinceId = rs.getInt("province_id");
        tutor.setProvinceId(rs.wasNull() ? null : provinceId);
        try {
            tutor.setProvinceName(rs.getString("province_name"));
        } catch (SQLException ignored) {
            tutor.setProvinceName(null);
        }
        
        return tutor;
    }
}
