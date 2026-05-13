package model;

import java.time.LocalDateTime;

public class Booking {
    private int bookingId;
    private int studentId;
    private int tutorId;
    private LocalDateTime bookingDate;
    private String status;
    private String notes;
    public Booking() {
    }

    public Booking(int studentId, int tutorId, LocalDateTime bookingDate) {
        this.studentId = studentId;
        this.tutorId = tutorId;
        this.bookingDate = bookingDate;
        this.status = "PENDING";
    }

    public Booking(int studentId, int tutorId, LocalDateTime bookingDate, String notes) {
        this(studentId, tutorId, bookingDate);
        this.notes = notes;
    }
    public int getBookingId() {
        return bookingId;
    }

    public void setBookingId(int bookingId) {
        this.bookingId = bookingId;
    }

    public int getStudentId() {
        return studentId;
    }

    public void setStudentId(int studentId) {
        this.studentId = studentId;
    }

    public int getTutorId() {
        return tutorId;
    }

    public void setTutorId(int tutorId) {
        this.tutorId = tutorId;
    }

    public LocalDateTime getBookingDate() {
        return bookingDate;
    }

    public void setBookingDate(LocalDateTime bookingDate) {
        this.bookingDate = bookingDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    @Override
    public String toString() {
        return "Booking{" +
                "bookingId=" + bookingId +
                ", studentId=" + studentId +
                ", tutorId=" + tutorId +
                ", bookingDate=" + bookingDate +
                ", status='" + status + '\'' +
                ", notes='" + notes + '\'' +
                '}';
    }
}
