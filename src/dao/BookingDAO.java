package dao;

import model.Booking;
import database.DBConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDateTime;

public class BookingDAO {
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
    public boolean hasPendingBooking(int studentId, int tutorId) {
        String sql = "SELECT 1 FROM Bookings WHERE student_id = ? AND tutor_id = ? AND status = 'PENDING' LIMIT 1";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, studentId);
            pstmt.setInt(2, tutorId);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }

        } catch (SQLException e) {
            System.err.println("Error checking pending booking: " + e.getMessage());
            return false;
        }
    }
    public boolean cancelBookingIfPending(int bookingId, int studentId) {
        String sql = "UPDATE Bookings SET status = 'REJECTED' WHERE booking_id = ? AND student_id = ? AND status = 'PENDING'";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, bookingId);
            pstmt.setInt(2, studentId);
            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error cancelling booking: " + e.getMessage());
            return false;
        }
    }
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
    public boolean canCancelBooking(int bookingId, int studentId) {
        String sql = "SELECT 1 FROM Bookings WHERE booking_id = ? AND student_id = ? AND status IN ('PENDING', 'ACCEPTED') LIMIT 1";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, bookingId);
            pstmt.setInt(2, studentId);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            System.err.println("Error checking if booking can be cancelled: " + e.getMessage());
            return false;
        }
    }
    public boolean cancelBookingIfPendingOrAccepted(int bookingId, int studentId) {
        String sql = "UPDATE Bookings SET status = 'REJECTED' WHERE booking_id = ? AND student_id = ? AND status IN ('PENDING', 'ACCEPTED')";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, bookingId);
            pstmt.setInt(2, studentId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error cancelling booking: " + e.getMessage());
            return false;
        }
    }
    public boolean isAwaitingStudentStartConfirmation(int bookingId) {
        Booking b = getBookingById(bookingId);
        if (b == null) return false;
        String notes = b.getNotes() == null ? "" : b.getNotes();
        return "ACCEPTED".equalsIgnoreCase(b.getStatus()) && notes.contains("TUTOR_START_REQUESTED");
    }
    public boolean markTutorStartRequested(int bookingId) {
        Booking b = getBookingById(bookingId);
        if (b == null) return false;
        String notes = b.getNotes() == null ? "" : b.getNotes();
        if (!notes.contains("TUTOR_START_REQUESTED")) {
            if (!notes.isEmpty()) notes += "\n";
            notes += "TUTOR_START_REQUESTED";
            b.setNotes(notes);
            return updateBooking(b);
        }
        return true;
    }
    public boolean confirmStudentStartAndTransition(int bookingId, int studentId) {
        Booking b = getBookingById(bookingId);
        if (b == null || studentId != b.getStudentId()) return false;
        if (!"ACCEPTED".equalsIgnoreCase(b.getStatus())) return false;
        String notes = b.getNotes() == null ? "" : b.getNotes();
        if (!notes.contains("TUTOR_START_REQUESTED")) return false;
        if (!notes.contains("STUDENT_START_CONFIRMED")) {
            if (!notes.isEmpty()) notes += "\n";
            notes += "STUDENT_START_CONFIRMED";
        }
        if (!notes.contains("IN_PROGRESS_START")) {
            if (!notes.isEmpty()) notes += "\n";
            notes += "IN_PROGRESS_START=" + LocalDateTime.now();
        }
        b.setStatus("IN_PROGRESS");
        b.setNotes(notes);
        return updateBooking(b);
    }
}
