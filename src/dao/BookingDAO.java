package dao;

import model.Booking;
import database.DBConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDateTime;

public class BookingDAO {
    
    /**
     * Insert a new booking
     */
    public boolean insertBooking(Booking booking) {
        String sql = "INSERT INTO Bookings (student_id, tutor_id, booking_date, status, notes) VALUES (?, ?, ?, ?, ?)";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, booking.getStudentId());
            pstmt.setInt(2, booking.getTutorId());
            pstmt.setObject(3, booking.getBookingDate());
            pstmt.setString(4, booking.getStatus());
            pstmt.setString(5, booking.getNotes());
            
            return pstmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            System.err.println("Error inserting booking: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Get booking by ID
     */
    public Booking getBookingById(int bookingId) {
        String sql = "SELECT * FROM Bookings WHERE booking_id = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, bookingId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return mapResultSetToBooking(rs);
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting booking: " + e.getMessage());
        }
        return null;
    }
    
    /**
     * Get bookings by student ID
     */
    public List<Booking> getBookingsByStudentId(int studentId) {
        List<Booking> bookings = new ArrayList<>();
        String sql = "SELECT * FROM Bookings WHERE student_id = ? ORDER BY booking_date DESC";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, studentId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                bookings.add(mapResultSetToBooking(rs));
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting bookings by student: " + e.getMessage());
        }
        return bookings;
    }
    
    /**
     * Get bookings by tutor ID
     */
    public List<Booking> getBookingsByTutorId(int tutorId) {
        List<Booking> bookings = new ArrayList<>();
        String sql = "SELECT * FROM Bookings WHERE tutor_id = ? ORDER BY booking_date DESC";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, tutorId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                bookings.add(mapResultSetToBooking(rs));
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting bookings by tutor: " + e.getMessage());
        }
        return bookings;
    }
    
    /**
     * Get bookings by status
     */
    public List<Booking> getBookingsByStatus(String status) {
        List<Booking> bookings = new ArrayList<>();
        String sql = "SELECT * FROM Bookings WHERE status = ? ORDER BY booking_date DESC";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, status);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                bookings.add(mapResultSetToBooking(rs));
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting bookings by status: " + e.getMessage());
        }
        return bookings;
    }
    
    /**
     * Get all bookings
     */
    public List<Booking> getAllBookings() {
        List<Booking> bookings = new ArrayList<>();
        String sql = "SELECT * FROM Bookings ORDER BY booking_date DESC";
        
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                bookings.add(mapResultSetToBooking(rs));
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting all bookings: " + e.getMessage());
        }
        return bookings;
    }
    
    /**
     * Update booking
     */
    public boolean updateBooking(Booking booking) {
        String sql = "UPDATE Bookings SET student_id = ?, tutor_id = ?, booking_date = ?, status = ?, notes = ? WHERE booking_id = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, booking.getStudentId());
            pstmt.setInt(2, booking.getTutorId());
            pstmt.setObject(3, booking.getBookingDate());
            pstmt.setString(4, booking.getStatus());
            pstmt.setString(5, booking.getNotes());
            pstmt.setInt(6, booking.getBookingId());
            
            return pstmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            System.err.println("Error updating booking: " + e.getMessage());
            return false;
        }
    }
    public boolean deleteBooking(int bookingId) {
        String sql = "DELETE FROM Bookings WHERE booking_id = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, bookingId);
            return pstmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            System.err.println("Error deleting booking: " + e.getMessage());
            return false;
        }
    }
    private Booking mapResultSetToBooking(ResultSet rs) throws SQLException {
        Booking booking = new Booking();
        booking.setBookingId(rs.getInt("booking_id"));
        booking.setStudentId(rs.getInt("student_id"));
        booking.setTutorId(rs.getInt("tutor_id"));
        booking.setBookingDate(rs.getObject("booking_date", LocalDateTime.class));
        booking.setStatus(rs.getString("status"));
        booking.setNotes(rs.getString("notes"));
        
        return booking;
    }
}
