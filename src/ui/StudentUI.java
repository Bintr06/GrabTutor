package ui;

import dao.BookingDAO;
import dao.TutorDAO;
import model.User;
import model.Booking;
import model.Tutor;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.ArrayList;
import java.util.Map;
import java.util.List;

public class StudentUI extends JFrame {
    private final User currentUser;
    private final TutorDAO tutorDAO = new TutorDAO();
    private final BookingDAO bookingDAO = new BookingDAO();

    private final List<String> selectedSubjects = new ArrayList<>();
    private JComboBox<String> provinceFilterCombo;
    private JButton subjectSelectButton;
    private JList<Tutor> tutorList;
    private JButton requestButton;
    private JTextArea notesArea;
    private JTextArea bookingStatusArea;

    public StudentUI(User user) {
        this.currentUser = user;
        setupUI();
    }

    private void setupUI() {
        setTitle("GrabTutor - Tìm Gia Sư (Học sinh: " + currentUser.getUsername() + ")");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(600, 500);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        mainPanel.setBackground(Color.WHITE);

        // Top: Subject selection
        JPanel subjectPanel = createSubjectPanel();
        mainPanel.add(subjectPanel, BorderLayout.NORTH);

        // Center: Tutor list
        JPanel tutorPanel = createTutorPanel();
        mainPanel.add(tutorPanel, BorderLayout.CENTER);

        // Bottom: Booking status
        JPanel bottomPanel = createBottomPanel();
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        add(mainPanel);
        setVisible(true);
    }

    private JPanel createSubjectPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 6));
        panel.setBorder(BorderFactory.createTitledBorder("Chọn môn cần học"));
        panel.setBackground(Color.WHITE);

        JPanel filterRow = new JPanel(new BorderLayout(8, 0));
        filterRow.setOpaque(false);

        subjectSelectButton = new JButton("Chọn môn");
        subjectSelectButton.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subjectSelectButton.setFocusPainted(false);
        subjectSelectButton.addActionListener(e -> showSubjectPicker());

        provinceFilterCombo = createProvinceFilterCombo();
        filterRow.add(subjectSelectButton, BorderLayout.CENTER);
        filterRow.add(provinceFilterCombo, BorderLayout.EAST);

        panel.add(filterRow, BorderLayout.CENTER);

        return panel;
    }

    private JComboBox<String> createProvinceFilterCombo() {
        List<String> provinceOptions = new ArrayList<>();
        provinceOptions.add("Tất cả tỉnh thành");

        List<String> provinces = tutorDAO.getAllProvinceNames();
        if (provinces.isEmpty()) {
            provinces = List.of("Đà Nẵng", "Hà Nội", "TP Hồ Chí Minh", "Quảng Nam", "Huế");
        }
        provinceOptions.addAll(provinces);

        JComboBox<String> combo = new JComboBox<>(provinceOptions.toArray(new String[0]));
        combo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        combo.addActionListener(e -> updateTutorList());
        combo.setPreferredSize(new Dimension(190, 30));
        return combo;
    }

    private JPanel createTutorPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createTitledBorder("Danh sách gia sư"));
        panel.setBackground(Color.WHITE);

        tutorList = new JList<>();
        tutorList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tutorList.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tutorList.setCellRenderer((list, tutor, index, isSelected, cellHasFocus) -> {
            JLabel label = new JLabel(formatTutor(tutor));
            label.setOpaque(true);
            label.setBorder(BorderFactory.createEmptyBorder(4, 6, 4, 6));
            if (isSelected) {
                label.setBackground(list.getSelectionBackground());
                label.setForeground(list.getSelectionForeground());
            } else {
                label.setBackground(list.getBackground());
                label.setForeground(list.getForeground());
            }
            return label;
        });

        JScrollPane scrollPane = new JScrollPane(tutorList);
        scrollPane.setPreferredSize(new Dimension(400, 200));

        panel.add(scrollPane, BorderLayout.CENTER);
        updateTutorList();

        return panel;
    }

    private JPanel createBottomPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createTitledBorder("Yêu cầu booking"));
        panel.setBackground(Color.WHITE);

        JPanel notePanel = new JPanel(new BorderLayout(6, 6));
        notePanel.setOpaque(false);
        JLabel noteLabel = new JLabel("Lời nhắn cho gia sư:");
        noteLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        notesArea = new JTextArea(3, 40);
        notesArea.setLineWrap(true);
        notesArea.setWrapStyleWord(true);
        notesArea.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        JScrollPane notesScroll = new JScrollPane(notesArea);
        notesScroll.setPreferredSize(new Dimension(400, 70));
        notePanel.add(noteLabel, BorderLayout.NORTH);
        notePanel.add(notesScroll, BorderLayout.CENTER);
        requestButton = new JButton("Yêu cầu gia sư này");
        requestButton.setFont(new Font("Segoe UI", Font.BOLD, 12));
        requestButton.setPreferredSize(new Dimension(150, 35));
        requestButton.setBackground(new Color(59, 130, 246));
        requestButton.setForeground(Color.WHITE);
        requestButton.setFocusPainted(false);
        requestButton.addActionListener(e -> requestTutor());
        bookingStatusArea = new JTextArea(3, 40);
        bookingStatusArea.setEditable(false);
        bookingStatusArea.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        bookingStatusArea.setLineWrap(true);
        bookingStatusArea.setWrapStyleWord(true);
        bookingStatusArea.setBorder(BorderFactory.createLineBorder(new Color(203, 213, 225)));
        JScrollPane statusScroll = new JScrollPane(bookingStatusArea);

        JPanel topSub = new JPanel(new BorderLayout());
        topSub.setOpaque(false);
        topSub.add(requestButton, BorderLayout.WEST);

        JPanel northPanel = new JPanel(new BorderLayout(0, 8));
        northPanel.setOpaque(false);
        northPanel.add(notePanel, BorderLayout.NORTH);
        northPanel.add(topSub, BorderLayout.CENTER);

        panel.add(northPanel, BorderLayout.NORTH);
        panel.add(statusScroll, BorderLayout.CENTER);

        loadBookingStatus();

        return panel;
    }

    private void updateTutorList() {
        DefaultListModel<Tutor> tutorModel = new DefaultListModel<>();

        if (selectedSubjects.isEmpty()) {
            tutorList.setModel(tutorModel);
            return;
        }

        Map<Integer, Tutor> uniqueTutors = new LinkedHashMap<>();
        String selectedProvince = provinceFilterCombo != null
                ? (String) provinceFilterCombo.getSelectedItem()
                : "Tất cả tỉnh thành";
        String provinceFilter = (selectedProvince == null || "Tất cả tỉnh thành".equals(selectedProvince))
                ? null
                : selectedProvince;

        for (String subject : selectedSubjects) {
            List<Tutor> tutors = tutorDAO.getTutorsBySubjectAndProvince(subject, provinceFilter);
            for (Tutor tutor : tutors) {
                uniqueTutors.putIfAbsent(tutor.getTutorId(), tutor);
            }
        }

        for (Tutor tutor : uniqueTutors.values()) {
            tutorModel.addElement(tutor);
        }

        tutorList.setModel(tutorModel);
    }

    private void requestTutor() {
        Tutor selectedTutor = tutorList.getSelectedValue();
        if (selectedTutor == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một gia sư.", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String notes = notesArea.getText().trim();
        Booking booking = new Booking(currentUser.getUserId(), selectedTutor.getTutorId(), LocalDateTime.now(), notes);
        if (!bookingDAO.insertBooking(booking)) {
            JOptionPane.showMessageDialog(this, "Không thể gửi yêu cầu booking.", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JOptionPane.showMessageDialog(this, "Yêu cầu gửi thành công!\nChờ gia sư xác nhận hoặc từ chối.", "Thành công", JOptionPane.INFORMATION_MESSAGE);
        loadBookingStatus();
    }

    private String formatTutor(Tutor tutor) {
        return "Gia sư #" + tutor.getTutorId()
                + " | Học phí: " + tutor.getPricePerHour()
                + " | Tỉnh: " + (tutor.getProvinceName() == null || tutor.getProvinceName().isBlank() ? "(chưa cập nhật)" : tutor.getProvinceName())
                + " | Trạng thái: " + tutor.getStatus();
    }

    private void showSubjectPicker() {
        List<String> subjects = tutorDAO.getAllSubjectNames();
        if (subjects.isEmpty()) {
            subjects = List.of("Toán", "Lý", "Hóa", "Tiếng Anh", "Tin Học");
        }

        DefaultListModel<String> subjectModel = new DefaultListModel<>();
        for (String subject : subjects) {
            subjectModel.addElement(subject);
        }

        JList<String> pickerList = new JList<>(subjectModel);
        pickerList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        pickerList.setVisibleRowCount(6);
        pickerList.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        if (!selectedSubjects.isEmpty()) {
            int[] indices = selectedSubjects.stream()
                    .mapToInt(subjectModel::indexOf)
                    .filter(index -> index >= 0)
                    .toArray();
            pickerList.setSelectedIndices(indices);
        }

        JScrollPane scrollPane = new JScrollPane(pickerList);
        scrollPane.setPreferredSize(new Dimension(260, 180));

        int result = JOptionPane.showConfirmDialog(
                this,
                scrollPane,
                "Chọn môn học",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (result == JOptionPane.OK_OPTION) {
            selectedSubjects.clear();
            selectedSubjects.addAll(pickerList.getSelectedValuesList());
            updateSubjectButtonLabel();
            updateTutorList();
        }
    }

    private void updateSubjectButtonLabel() {
        if (selectedSubjects.isEmpty()) {
            subjectSelectButton.setText("Chọn môn");
        } else if (selectedSubjects.size() == 1) {
            subjectSelectButton.setText(selectedSubjects.get(0));
        } else {
            subjectSelectButton.setText(selectedSubjects.size() + " môn đã chọn");
        }
    }

    private void loadBookingStatus() {
        List<Booking> bookings = bookingDAO.getBookingsByStudentId(currentUser.getUserId());
        
        StringBuilder status = new StringBuilder();
        for (Booking booking : bookings) {
            status.append("Booking ID: ").append(booking.getBookingId())
                    .append("\nTrạng thái: ").append(booking.getStatus())
                    .append("\nNgày: ").append(booking.getBookingDate())
                    .append("\nLời nhắn: ").append(booking.getNotes() == null || booking.getNotes().isBlank() ? "(không có)" : booking.getNotes())
                    .append("\n---\n");
        }

        if (status.length() == 0) {
            bookingStatusArea.setText("Chưa có yêu cầu booking nào.");
        } else {
            bookingStatusArea.setText(status.toString());
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
