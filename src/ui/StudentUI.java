package ui;

import dao.BookingDAO;
import dao.TutorDAO;
import dao.UserDAO;
import model.User;
import model.Booking;
import model.Tutor;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.time.LocalDateTime;
import java.util.List;
// import java.util.stream.Collectors;

public class StudentUI extends JFrame {
    private final User currentUser;
    private final TutorDAO tutorDAO = new TutorDAO();
    private final BookingDAO bookingDAO = new BookingDAO();
    private final UserDAO userDAO = new UserDAO();
    private JComboBox<String> subjectCombo;
    private JComboBox<String> gradeCombo;
    private JComboBox<String> provinceCombo;
    private JTextField nameSearchField;
    private JButton searchButton;
    private JTable tutorTable;
    private DefaultTableModel tutorTableModel;
    private JTextArea notesArea;
    private JLabel totalPriceLabel;
    private JButton bookButton;
    private JTable historyTable;
    private DefaultTableModel historyModel;
    public StudentUI(User user) {
        this.currentUser = user;
        setupUI();
    }
    private void setupUI() {
        setTitle("GrabTutor - Tìm Gia Sư (" + currentUser.getFullName() + ")");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 650);
        setLocationRelativeTo(null);
        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Tìm kiếm gia sư", createSearchTab());
        tabs.addTab("Lịch sử đặt chỗ", createHistoryTab());

        add(tabs);
        setVisible(true);
    }

    private JPanel createSearchTab() {
        JPanel main = new JPanel(new BorderLayout(8, 8));
        main.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JPanel top = new JPanel(new GridBagLayout());
        top.setBorder(BorderFactory.createTitledBorder("Tìm kiếm & Bộ lọc"));
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(6, 6, 6, 6);
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.gridx = 0; gc.gridy = 0; gc.weightx = 0;
        top.add(new JLabel("Môn học:"), gc);
        subjectCombo = new JComboBox<>();
        subjectCombo.setPreferredSize(new Dimension(220, 28));
        gc.gridx = 1; gc.weightx = 0.3;
        top.add(subjectCombo, gc);

        gc.gridx = 2; gc.weightx = 0; top.add(new JLabel("Lớp:"), gc);
        gradeCombo = new JComboBox<>();
        gradeCombo.setPreferredSize(new Dimension(140, 28));
        gc.gridx = 3; gc.weightx = 0.2;
        top.add(gradeCombo, gc);

        gc.gridx = 4; gc.weightx = 0; top.add(new JLabel("Tỉnh/Thành:"), gc);
        provinceCombo = new JComboBox<>();
        provinceCombo.setPreferredSize(new Dimension(200, 28));

        gc.gridx = 5; gc.weightx = 0.3; top.add(provinceCombo, gc);

        gc.gridx = 0; gc.gridy = 1; gc.weightx = 0; top.add(new JLabel("Tìm theo tên:"), gc);
        nameSearchField = new JTextField();
        gc.gridx = 1; gc.gridwidth = 4; gc.weightx = 0.8; top.add(nameSearchField, gc);
        searchButton = new JButton("Tìm kiếm");
        gc.gridx = 5; gc.gridwidth = 1; gc.weightx = 0; top.add(searchButton, gc);
        loadFilterData();
        searchButton.addActionListener(e -> searchTutors());
        // logout button
        JButton logoutBtn = new JButton("Đăng xuất");
        logoutBtn.addActionListener(e -> {
            // return to login
            new AuthFrame().setVisible(true);
            dispose();
        });

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.add(top, BorderLayout.WEST);
        JPanel rightWrap = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        rightWrap.setOpaque(false);
        rightWrap.add(logoutBtn);
        header.add(rightWrap, BorderLayout.EAST);
        JPanel center = new JPanel(new BorderLayout());
        center.setBorder(BorderFactory.createTitledBorder("Danh sách gia sư"));

        String[] cols = new String[]{"ID", "Họ tên", "Môn dạy", "Giá/Giờ", "Khu vực", "Kinh nghiệm"};
        tutorTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tutorTable = new JTable(tutorTableModel);
        tutorTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tutorTable.getSelectionModel().addListSelectionListener(e -> onTutorSelected(e));
        TableColumn idCol = tutorTable.getColumnModel().getColumn(0);
        idCol.setMinWidth(0); idCol.setMaxWidth(0); idCol.setPreferredWidth(0); idCol.setResizable(false);

        JScrollPane tableScroll = new JScrollPane(tutorTable);
        center.add(tableScroll, BorderLayout.CENTER);
        JPanel right = new JPanel(new BorderLayout(6, 6));
        right.setPreferredSize(new Dimension(320, 300));
        right.setBorder(BorderFactory.createTitledBorder("Chi tiết & Ghi chú"));
        notesArea = new JTextArea(6, 20);
        notesArea.setLineWrap(true);
        notesArea.setWrapStyleWord(true);
        JScrollPane notesScroll = new JScrollPane(notesArea);
        totalPriceLabel = new JLabel("Giá dự kiến: -");
        bookButton = new JButton("Đặt gia sư");
        bookButton.setEnabled(false);
        JPanel bottomRight = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        bottomRight.add(totalPriceLabel);
        bottomRight.add(bookButton);
        right.add(notesScroll, BorderLayout.CENTER);
        right.add(bottomRight, BorderLayout.SOUTH);
        bookButton.addActionListener(e -> bookSelectedTutor());
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, center, right);
        split.setResizeWeight(0.65);
        main.add(header, BorderLayout.NORTH);
        main.add(split, BorderLayout.CENTER);
        searchTutors();

        return main;
    }

    private JPanel createHistoryTab() {
        JPanel p = new JPanel(new BorderLayout(6,6));
        p.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        String[] cols = new String[]{"ID", "Gia sư", "Ngày đặt", "Ghi chú", "Trạng thái"};
        historyModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        historyTable = new JTable(historyModel);
        // hide ID column
        TableColumn idCol = historyTable.getColumnModel().getColumn(0);
        idCol.setMinWidth(0); idCol.setMaxWidth(0); idCol.setPreferredWidth(0); idCol.setResizable(false);

        // Add mouse listener to view full notes when clicking on notes column
        historyTable.addMouseListener(new MouseListener() {
            @Override public void mouseClicked(MouseEvent e) {
                int row = historyTable.rowAtPoint(e.getPoint());
                int col = historyTable.columnAtPoint(e.getPoint());
                if (row >= 0 && col == 3) { // notes column (index 3)
                    int modelRow = historyTable.convertRowIndexToModel(row);
                    Object notesObj = historyModel.getValueAt(modelRow, 3);
                    String notes = notesObj != null ? notesObj.toString() : "(không có ghi chú)";
                    JTextArea textarea = new JTextArea(notes, 10, 40);
                    textarea.setLineWrap(true);
                    textarea.setWrapStyleWord(true);
                    textarea.setEditable(false);
                    JScrollPane scroll = new JScrollPane(textarea);
                    JOptionPane.showMessageDialog(StudentUI.this, scroll, "Ghi chú đầy đủ", JOptionPane.INFORMATION_MESSAGE);
                }
            }
            @Override public void mousePressed(MouseEvent e) {}
            @Override public void mouseReleased(MouseEvent e) {}
            @Override public void mouseEntered(MouseEvent e) {}
            @Override public void mouseExited(MouseEvent e) {}
        });

        JScrollPane sc = new JScrollPane(historyTable);
        p.add(sc, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton cancelBtn = new JButton("Hủy yêu cầu");
        JButton confirmStartBtn = new JButton("Xác nhận bắt đầu");
        cancelBtn.setEnabled(false);
        confirmStartBtn.setEnabled(false);
        bottom.add(cancelBtn);
        bottom.add(confirmStartBtn);
        p.add(bottom, BorderLayout.SOUTH);
        historyTable.getSelectionModel().addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) return;
            int sel = historyTable.getSelectedRow();
            if (sel < 0) {
                cancelBtn.setEnabled(false);
                confirmStartBtn.setEnabled(false);
                return;
            }
            int modelRow = historyTable.convertRowIndexToModel(sel);
            Object idObj = historyModel.getValueAt(modelRow, 0);
            Object statusObj = historyModel.getValueAt(modelRow, 4);
            if (idObj == null || statusObj == null) {
                cancelBtn.setEnabled(false);
                confirmStartBtn.setEnabled(false);
                return;
            }
            int bookingId = (int) idObj;
            String status = statusObj.toString();
            cancelBtn.setEnabled(bookingDAO.canCancelBooking(bookingId, currentUser.getUserId()));
            confirmStartBtn.setEnabled("ACCEPTED".equalsIgnoreCase(status) && bookingDAO.isAwaitingStudentStartConfirmation(bookingId));
        });

        cancelBtn.addActionListener(ev -> {
            int sel = historyTable.getSelectedRow();
            if (sel < 0) return;
            int modelRow = historyTable.convertRowIndexToModel(sel);
            Object idObj = historyModel.getValueAt(modelRow, 0);
            if (idObj == null) return;
            int bookingId = (int) idObj;
            boolean ok = bookingDAO.cancelBookingIfPendingOrAccepted(bookingId, currentUser.getUserId());
            if (ok) {
                JOptionPane.showMessageDialog(this, "Yêu cầu đã được hủy.", "Đã hủy", JOptionPane.INFORMATION_MESSAGE);
                loadBookingHistory();
            } else {
                JOptionPane.showMessageDialog(this, "Không thể hủy yêu cầu (có thể không còn ở trạng thái PENDING/ACCEPTED).", "Lỗi", JOptionPane.ERROR_MESSAGE);
                loadBookingHistory();
            }
        });

        confirmStartBtn.addActionListener(ev -> {
            int sel = historyTable.getSelectedRow();
            if (sel < 0) return;
            int modelRow = historyTable.convertRowIndexToModel(sel);
            Object idObj = historyModel.getValueAt(modelRow, 0);
            if (idObj == null) return;
            int bookingId = (int) idObj;
            if (JOptionPane.showConfirmDialog(this, "Xác nhận bắt đầu buổi học?", "Xác nhận", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                boolean ok = bookingDAO.confirmStudentStartAndTransition(bookingId, currentUser.getUserId());
                if (ok) {
                    JOptionPane.showMessageDialog(this, "Buổi học bắt đầu.", "Thành công", JOptionPane.INFORMATION_MESSAGE);
                    loadBookingHistory();
                } else {
                    JOptionPane.showMessageDialog(this, "Không thể xác nhận bắt đầu.", "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        loadBookingHistory();
        return p;
    }

    private void loadFilterData() {
        subjectCombo.removeAllItems();
        subjectCombo.addItem("Tất cả");
        List<String> subjects = tutorDAO.getAllSubjectNames();
        for (String s : subjects) subjectCombo.addItem(s);

        gradeCombo.removeAllItems();
        gradeCombo.addItem("Tất cả");
        List<String> grades = tutorDAO.getAllGradeNames();
        if (grades.isEmpty()) {
            for (int i = 1; i <= 12; i++) grades.add("Lớp " + i);
        }
        for (String g : grades) gradeCombo.addItem(g);

        provinceCombo.removeAllItems();
        provinceCombo.addItem("Tất cả tỉnh thành");
        List<String> provinces = tutorDAO.getAllProvinceNames();
        for (String p : provinces) provinceCombo.addItem(p);
    }

    private void searchTutors() {
        String subject = (String) subjectCombo.getSelectedItem();
        String grade = (String) gradeCombo.getSelectedItem();
        String province = (String) provinceCombo.getSelectedItem();
        String name = nameSearchField.getText().trim().toLowerCase();

        String subjectFilter = (subject == null || "Tất cả".equals(subject)) ? null : subject;
        String gradeFilter = (grade == null || "Tất cả".equals(grade)) ? null : grade;
        String provinceFilter = (province == null || "Tất cả tỉnh thành".equals(province)) ? null : province;

        List<Tutor> tutors = tutorDAO.searchTutors(subjectFilter, provinceFilter, gradeFilter);
        tutorTableModel.setRowCount(0);
        for (Tutor t : tutors) {
            if (t.getStatus() != null && "BUSY".equalsIgnoreCase(t.getStatus())) {
                continue;
            }
            
            String tutorName = "(chưa có)";
            if (t.getUserId() != 0) {
                var u = userDAO.getUserById(t.getUserId());
                if (u != null) tutorName = u.getFullName();
            }
            if (!name.isBlank() && !tutorName.toLowerCase().contains(name)) continue;

            List<String> subjects = tutorDAO.getSubjectNamesByTutorId(t.getTutorId());
            String subj = String.join(", ", subjects);
            tutorTableModel.addRow(new Object[]{t.getTutorId(), tutorName, subj, String.valueOf(t.getPricePerHour()), t.getProvinceName(), t.getExperience()});
        }
    }

    private void onTutorSelected(ListSelectionEvent e) {
        if (e.getValueIsAdjusting()) return;
        int sel = tutorTable.getSelectedRow();
        if (sel < 0) {
            bookButton.setEnabled(false);
            totalPriceLabel.setText("Giá dự kiến: -");
            return;
        }
        int modelRow = tutorTable.convertRowIndexToModel(sel);
        Object idObj = tutorTableModel.getValueAt(modelRow, 0);
        if (idObj == null) return;
        int tutorId = (int) idObj;

        Tutor t = tutorDAO.getTutorById(tutorId);
        String priceText = t.getPricePerHour() == null ? "-" : t.getPricePerHour().toString();
        totalPriceLabel.setText("Giá/Giờ: " + priceText);
        bookButton.setEnabled(true);
    }

    private void bookSelectedTutor() {
        int sel = tutorTable.getSelectedRow();
        if (sel < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn gia sư để đặt.", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int modelRow = tutorTable.convertRowIndexToModel(sel);
        int tutorId = (int) tutorTableModel.getValueAt(modelRow, 0);
        Tutor tcheck = tutorDAO.getTutorById(tutorId);
        if (tcheck != null && "BUSY".equalsIgnoreCase(tcheck.getStatus())) {
            JOptionPane.showMessageDialog(this, "Gia sư hiện đang bận và không nhận lớp mới.", "Không thể đặt", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (bookingDAO.hasPendingBooking(currentUser.getUserId(), tutorId)) {
            JOptionPane.showMessageDialog(this, "Bạn đã có yêu cầu đang chờ với gia sư này.", "Không thể đặt", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String notes = notesArea.getText().trim();
        Booking booking = new Booking(currentUser.getUserId(), tutorId, LocalDateTime.now(), notes);
        boolean ok = bookingDAO.insertBooking(booking);
        if (ok) {
            JOptionPane.showMessageDialog(this, "Đặt gia sư thành công.", "Thành công", JOptionPane.INFORMATION_MESSAGE);
            notesArea.setText("");
            loadBookingHistory();
        } else {
            JOptionPane.showMessageDialog(this, "Không thể đặt gia sư.", "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadBookingHistory() {
        historyModel.setRowCount(0);
        List<Booking> bookings = bookingDAO.getBookingsByStudentId(currentUser.getUserId());
        for (Booking b : bookings) {
            String tutorName = "(không xác định)";
            var t = tutorDAO.getTutorById(b.getTutorId());
            if (t != null && t.getUserId() != 0) {
                var u = userDAO.getUserById(t.getUserId());
                if (u != null) tutorName = u.getFullName();
            }
            historyModel.addRow(new Object[]{b.getBookingId(), tutorName, b.getBookingDate(), b.getNotes(), b.getStatus()});
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            User testUser = new User("student1", "password", "Nguyễn Văn A", "0912345678", "STUDENT");
            testUser.setUserId(1);
            new StudentUI(testUser);
        });
    }
}
