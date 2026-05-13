package ui;

import dao.BookingDAO;
import dao.TutorDAO;
import dao.UserDAO;
import model.Booking;
import model.Tutor;
import model.User;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class TutorUI extends JFrame {
    private final User currentUser;
    private final BookingDAO bookingDAO = new BookingDAO();
    private final UserDAO userDAO = new UserDAO();
    private final TutorDAO tutorDAO = new TutorDAO();

    private final DefaultListModel<Booking> bookingModel = new DefaultListModel<>();
    private final List<Booking> allBookings = new ArrayList<>();
    private JList<Booking> bookingList;
    private JButton acceptButton;
    private JButton rejectButton;
    private JComboBox<String> statusFilter;

    public TutorUI(User user) {
        this.currentUser = user;
        setupUI();
    }

    private void setupUI() {
        setTitle("GrabTutor - Yêu cầu booking (Gia sư: " + currentUser.getUsername() + ")");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(600, 500);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel main = new JPanel(new BorderLayout(10, 10));
        main.setBorder(BorderFactory.createEmptyBorder(12,12,12,12));

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        topPanel.add(new JLabel("Lọc trạng thái:"));

        statusFilter = new JComboBox<>(new String[]{"Tất cả", "PENDING", "ACCEPTED", "REJECTED"});
        statusFilter.addActionListener(e -> applyStatusFilter());
        topPanel.add(statusFilter);

        JButton editProfileButton = new JButton("Chỉnh sửa thông tin");
        editProfileButton.addActionListener(this::onEditProfile);
        topPanel.add(editProfileButton);

        bookingList = new JList<>(bookingModel);
        bookingList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        bookingList.setCellRenderer((list, booking, index, isSelected, cellHasFocus) -> {
            String text = formatBooking(booking);
            JLabel lbl = new JLabel("<html>" + text.replaceAll("\n", "<br>") + "</html>");
            lbl.setOpaque(true);
            lbl.setBorder(BorderFactory.createEmptyBorder(6,6,6,6));
            if (isSelected) {
                lbl.setBackground(list.getSelectionBackground());
                lbl.setForeground(list.getSelectionForeground());
            } else {
                lbl.setBackground(list.getBackground());
                lbl.setForeground(list.getForeground());
            }
            return lbl;
        });

        JScrollPane scroll = new JScrollPane(bookingList);
        scroll.setPreferredSize(new Dimension(560, 340));

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        acceptButton = new JButton("Chấp nhận");
        rejectButton = new JButton("Từ chối");
        acceptButton.setEnabled(false);
        rejectButton.setEnabled(false);
        acceptButton.addActionListener(this::onAccept);
        rejectButton.addActionListener(this::onReject);

        bookingList.addListSelectionListener(e -> {
            boolean ok = bookingList.getSelectedValue() != null && "PENDING".equals(bookingList.getSelectedValue().getStatus());
            acceptButton.setEnabled(ok);
            rejectButton.setEnabled(ok);
        });

        btnPanel.add(acceptButton);
        btnPanel.add(rejectButton);

        main.add(topPanel, BorderLayout.NORTH);
        main.add(scroll, BorderLayout.CENTER);
        main.add(btnPanel, BorderLayout.SOUTH);

        add(main);

        loadBookings();
        setVisible(true);
    }

    private void loadBookings() {
        allBookings.clear();
        int tutorId = currentUser.getUserId();
        List<Booking> bookings = bookingDAO.getBookingsByTutorId(tutorId);
        allBookings.addAll(bookings);
        applyStatusFilter();
    }

    private void applyStatusFilter() {
        bookingModel.clear();
        String selected = (String) statusFilter.getSelectedItem();

        for (Booking b : allBookings) {
            if ("Tất cả".equals(selected) || selected == null || selected.equalsIgnoreCase(b.getStatus())) {
                bookingModel.addElement(b);
            }
        }
    }

    private String formatBooking(Booking b) {
        User student = userDAO.getUserById(b.getStudentId());
        String studentName = student != null ? student.getFullName() : ("#" + b.getStudentId());
        String date = b.getBookingDate() != null ? b.getBookingDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) : "(không rõ)";
        String notes = b.getNotes() == null || b.getNotes().isBlank() ? "(không có)" : b.getNotes();
        return "Booking ID: " + b.getBookingId()
                + "\nHọc sinh: " + studentName
                + "\nNgày: " + date
                + "\nTrạng thái: " + b.getStatus()
                + "\nLời nhắn: " + notes;
    }

    private void onAccept(ActionEvent ev) {
        Booking b = bookingList.getSelectedValue();
        if (b == null) return;
        if (!confirmAction("Xác nhận chấp nhận yêu cầu booking này?")) return;
        b.setStatus("ACCEPTED");
        if (bookingDAO.updateBooking(b)) {
            JOptionPane.showMessageDialog(this, "Đã chấp nhận booking.", "Thành công", JOptionPane.INFORMATION_MESSAGE);
            loadBookings();
        } else {
            JOptionPane.showMessageDialog(this, "Không thể cập nhật booking.", "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onReject(ActionEvent ev) {
        Booking b = bookingList.getSelectedValue();
        if (b == null) return;
        if (!confirmAction("Bạn có chắc muốn từ chối yêu cầu này?")) return;
        b.setStatus("REJECTED");
        if (bookingDAO.updateBooking(b)) {
            JOptionPane.showMessageDialog(this, "Đã từ chối booking.", "Thành công", JOptionPane.INFORMATION_MESSAGE);
            loadBookings();
        } else {
            JOptionPane.showMessageDialog(this, "Không thể cập nhật booking.", "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onEditProfile(ActionEvent ev) {
        User latestUser = userDAO.getUserById(currentUser.getUserId());
        Tutor tutor = tutorDAO.getTutorById(currentUser.getUserId());
        if (latestUser == null || tutor == null) {
            JOptionPane.showMessageDialog(this, "Không tìm thấy hồ sơ gia sư.", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JTextField fullNameField = new JTextField(latestUser.getFullName() != null ? latestUser.getFullName() : "", 20);
        JTextField phoneField = new JTextField(latestUser.getPhone() != null ? latestUser.getPhone() : "", 20);
        JTextField priceField = new JTextField(tutor.getPricePerHour() != null ? tutor.getPricePerHour().toPlainString() : "", 20);
        JTextArea expArea = new JTextArea(tutor.getExperience() != null ? tutor.getExperience() : "", 4, 20);
        expArea.setLineWrap(true);
        expArea.setWrapStyleWord(true);
        JScrollPane expScroll = new JScrollPane(expArea);

        JComboBox<String> tutorStatus = new JComboBox<>(new String[]{"AVAILABLE", "BUSY", "INACTIVE"});
        if (tutor.getStatus() != null) {
            tutorStatus.setSelectedItem(tutor.getStatus());
        }

        List<String> provinces = new ArrayList<>(tutorDAO.getAllProvinceNames());
        provinces.add(0, "");
        JComboBox<String> provinceCombo = new JComboBox<>(provinces.toArray(new String[0]));
        if (tutor.getProvinceName() != null) {
            provinceCombo.setSelectedItem(tutor.getProvinceName());
        }

        List<String> allSubjectNames = tutorDAO.getAllSubjectNames();
        List<String> selectedSubjectNames = tutorDAO.getSubjectNamesByTutorId(tutor.getTutorId());
        List<JCheckBox> subjectChecks = new ArrayList<>();
        JPanel subjectPanel = new JPanel();
        subjectPanel.setLayout(new BoxLayout(subjectPanel, BoxLayout.Y_AXIS));
        for (String subjectName : allSubjectNames) {
            JCheckBox cb = new JCheckBox(subjectName);
            cb.setSelected(selectedSubjectNames.contains(subjectName));
            cb.setOpaque(false);
            subjectChecks.add(cb);
            subjectPanel.add(cb);
        }
        JScrollPane subjectScroll = new JScrollPane(subjectPanel);
        subjectScroll.setPreferredSize(new Dimension(260, 120));

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;
        form.add(new JLabel("Họ và tên:"), gbc);
        gbc.gridx = 1;
        form.add(fullNameField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        form.add(new JLabel("Số điện thoại:"), gbc);
        gbc.gridx = 1;
        form.add(phoneField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        form.add(new JLabel("Học phí/giờ:"), gbc);
        gbc.gridx = 1;
        form.add(priceField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        form.add(new JLabel("Trạng thái:"), gbc);
        gbc.gridx = 1;
        form.add(tutorStatus, gbc);

        gbc.gridx = 0;
        gbc.gridy = 4;
        form.add(new JLabel("Tỉnh thành:"), gbc);
        gbc.gridx = 1;
        form.add(provinceCombo, gbc);

        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        form.add(new JLabel("Kinh nghiệm:"), gbc);
        gbc.gridx = 1;
        form.add(expScroll, gbc);

        gbc.gridx = 0;
        gbc.gridy = 6;
        form.add(new JLabel("Môn dạy:"), gbc);
        gbc.gridx = 1;
        form.add(subjectScroll, gbc);

        int result = JOptionPane.showConfirmDialog(this, form, "Chỉnh sửa thông tin gia sư", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result != JOptionPane.OK_OPTION) {
            return;
        }

        try {
            String newFullName = fullNameField.getText().trim();
            String newPhone = phoneField.getText().trim();
            BigDecimal newPrice = new BigDecimal(priceField.getText().trim());

            if (newFullName.isBlank() || newPhone.isBlank()) {
                JOptionPane.showMessageDialog(this, "Họ tên và số điện thoại không được để trống.", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }

            List<String> newSubjects = subjectChecks.stream()
                    .filter(AbstractButton::isSelected)
                    .map(AbstractButton::getText)
                    .toList();

            if (newSubjects.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Gia sư cần chọn ít nhất 1 môn dạy.", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String selectedProvinceName = (String) provinceCombo.getSelectedItem();
            Integer provinceId = null;
            if (selectedProvinceName != null && !selectedProvinceName.isBlank()) {
                provinceId = tutorDAO.getProvinceIdByName(selectedProvinceName);
            }

            latestUser.setFullName(newFullName);
            latestUser.setPhone(newPhone);

            tutor.setTutorId(currentUser.getUserId());
            tutor.setPricePerHour(newPrice);
            tutor.setStatus((String) tutorStatus.getSelectedItem());
            tutor.setExperience(expArea.getText().trim());
            tutor.setProvinceId(provinceId);
            tutor.setProvinceName(selectedProvinceName);

            boolean userUpdated = userDAO.updateUser(latestUser);
            boolean tutorUpdated = tutorDAO.updateTutor(tutor);
            boolean subjectsUpdated = tutorDAO.replaceTutorSubjects(currentUser.getUserId(), newSubjects);

            if (userUpdated && tutorUpdated && subjectsUpdated) {
                currentUser.setFullName(newFullName);
                currentUser.setPhone(newPhone);
                JOptionPane.showMessageDialog(this, "Cập nhật thông tin thành công.", "Thành công", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Không thể cập nhật đầy đủ thông tin. Vui lòng thử lại.", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Học phí không hợp lệ.", "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private boolean confirmAction(String msg) {
        int r = JOptionPane.showConfirmDialog(this, msg, "Xác nhận", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        return r == JOptionPane.YES_OPTION;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            User test = new User("tutor1", "pwd", "Nguyen Tutor", "0900000000", "TUTOR");
            test.setUserId(2);
            new TutorUI(test);
        });
    }
}
