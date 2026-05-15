package ui;

import dao.TutorDAO;
import dao.UserDAO;
import database.DBConnection;
import model.Tutor;
import model.User;
// import dao.AdminDAO;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JRadioButton;
import javax.swing.JTabbedPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JComboBox;
import javax.swing.ButtonGroup;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;

public class AuthFrame extends JFrame {
    private final UserDAO userDAO = new UserDAO();
    private final TutorDAO tutorDAO = new TutorDAO();

    private final JTabbedPane tabs = new JTabbedPane();

    private JTextField loginUsernameField;
    private JPasswordField loginPasswordField;

    private JTextField registerFullNameField;
    private JTextField tutorPriceField;
    private JTextField registerUsernameField;
    private JTextField registerPhoneField;
    private JRadioButton registerStudentRadio;
    private JRadioButton registerTutorRadio;
    private JPasswordField registerPasswordField;
    private JPasswordField registerConfirmPasswordField;
    private List<JCheckBox> gradeCheckboxes = new ArrayList<>();
    private JButton gradeSelectButton;
    private List<JCheckBox> subjectCheckboxes = new ArrayList<>();
    private JButton subjectSelectButton;
    private JComboBox<String> tutorProvinceCombo;
    private JTextArea tutorExperienceArea;
    private JPanel tutorExtraPanel;

    public AuthFrame() {
        setupLookAndFeel();
        setTitle("GrabTutor - Đăng nhập & Đăng ký");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setSize(1080, 720);
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(980, 660));

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(new Color(245, 247, 250));

        root.add(createHeroPanel(), BorderLayout.WEST);
        root.add(createFormPanel(), BorderLayout.CENTER);

        setContentPane(root);
    }

    private void setupLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");
    }

    private JPanel createHeroPanel() {
        JPanel hero = new GradientPanel(new Color(23, 37, 84), new Color(14, 116, 144));
        hero.setPreferredSize(new Dimension(370, 0));
        hero.setLayout(new BorderLayout());
        hero.setBorder(new EmptyBorder(40, 32, 40, 32));

        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.insets = new Insets(0, 0, 18, 0);

        JLabel title = new JLabel("GrabTutor");
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Segoe UI", Font.BOLD, 34));

        JLabel subtitle = new JLabel("Kết nối học sinh với gia sư phù hợp");
        subtitle.setForeground(new Color(224, 242, 254));
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 17));

        JLabel description = new JLabel("Đăng nhập để tiếp tục hoặc tạo tài khoản mới.");
        description.setForeground(new Color(191, 219, 254));
        description.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        JLabel badge = createBadge("Học sinh • Gia sư");

        gbc.gridy = 0;
        content.add(title, gbc);
        gbc.gridy = 1;
        content.add(subtitle, gbc);
        gbc.gridy = 2;
        content.add(description, gbc);
        gbc.gridy = 3;
        gbc.insets = new Insets(8, 0, 0, 0);
        content.add(badge, gbc);

        hero.add(content, BorderLayout.NORTH);
        return hero;
    }

    private JPanel createFormPanel() {
        JPanel container = new JPanel(new BorderLayout());
        container.setBackground(new Color(245, 247, 250));
        container.setBorder(new EmptyBorder(28, 28, 28, 28));

        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240)),
                new EmptyBorder(24, 24, 24, 24)
        ));

        JLabel header = new JLabel("Đăng nhập hoặc đăng ký");
        header.setFont(new Font("Segoe UI", Font.BOLD, 24));
        header.setForeground(new Color(15, 23, 42));
        header.setBorder(new EmptyBorder(0, 0, 18, 0));

        tabs.setFont(new Font("Segoe UI", Font.BOLD, 14));
        tabs.setBackground(Color.WHITE);
        tabs.setForeground(new Color(15, 23, 42));
        tabs.addTab("Đăng nhập", createLoginTab());
        tabs.addTab("Đăng ký", createRegisterTab());

        card.add(header, BorderLayout.NORTH);
        card.add(tabs, BorderLayout.CENTER);
        container.add(card, BorderLayout.CENTER);
        return container;
    }

    private JComponent createLoginTab() {
        JPanel panel = formCanvas();

        loginUsernameField = new JTextField();
        loginPasswordField = new JPasswordField();

        addField(panel, 0, "Tên đăng nhập", loginUsernameField);
        addField(panel, 1, "Mật khẩu", loginPasswordField);

        JButton loginButton = createPrimaryButton("Đăng nhập");
        loginButton.setForeground(Color.BLACK);
        loginButton.addActionListener(e -> handleLogin());

        panel.add(buttonRow(loginButton), rowConstraints(2));
        panel.add(spacer(), rowConstraints(3));
        return panel;
    }

    private JComponent createRegisterTab() {
        JPanel panel = formCanvas();

        registerFullNameField = new JTextField();
        registerUsernameField = new JTextField();
        registerPhoneField = new JTextField();
        registerPasswordField = new JPasswordField();
        registerConfirmPasswordField = new JPasswordField();

        registerStudentRadio = new JRadioButton("Học sinh", true);
        registerTutorRadio = new JRadioButton("Gia sư");
        registerStudentRadio.setOpaque(false);
        registerTutorRadio.setOpaque(false);
        registerStudentRadio.setFont(formFont());
        registerTutorRadio.setFont(formFont());
        ButtonGroup roleGroup = new ButtonGroup();
        roleGroup.add(registerStudentRadio);
        roleGroup.add(registerTutorRadio);

        tutorPriceField = new JTextField();
        tutorExperienceArea = new JTextArea(4, 18);
        tutorExperienceArea.setLineWrap(true);
        tutorExperienceArea.setWrapStyleWord(true);
        tutorExperienceArea.setFont(formFont());

        tutorExtraPanel = new JPanel(new GridBagLayout());
        tutorExtraPanel.setOpaque(false);
        addField(tutorExtraPanel, 0, "Lớp dạy (chọn nhiều lớp)", createGradePanel());
        addField(tutorExtraPanel, 1, "Môn dạy (chọn nhiều môn)", createSubjectPanel());
        addField(tutorExtraPanel, 2, "Tỉnh/Thành", createProvinceCombo());
        addField(tutorExtraPanel, 3, "Học phí/giờ", tutorPriceField);
        addField(tutorExtraPanel, 4, "Mô tả kinh nghiệm", wrapArea(tutorExperienceArea));

        addField(panel, 0, "Họ và tên", registerFullNameField);
        addField(panel, 1, "Tên đăng nhập", registerUsernameField);
        addField(panel, 2, "Số điện thoại", registerPhoneField);
    addField(panel, 3, "Vai trò", createRolePanel());
        addField(panel, 4, "Mật khẩu", registerPasswordField);
        addField(panel, 5, "Nhập lại mật khẩu", registerConfirmPasswordField);

        GridBagConstraints extra = rowConstraints(6);
        extra.insets = new Insets(0, 0, 12, 0);
        extra.gridwidth = 2;
        extra.fill = GridBagConstraints.HORIZONTAL;
        panel.add(tutorExtraPanel, extra);

        registerStudentRadio.addActionListener(e -> updateTutorFieldsVisibility());
        registerTutorRadio.addActionListener(e -> updateTutorFieldsVisibility());
        updateTutorFieldsVisibility();

        JButton registerButton = createPrimaryButton("Đăng ký");
        registerButton.setForeground(Color.BLACK);
        registerButton.addActionListener(e -> handleRegister());

        panel.add(buttonRow(registerButton), rowConstraints(7));

        JScrollPane scrollPane = new JScrollPane(panel,
            JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
            JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        return scrollPane;
    }

    private JPanel formCanvas() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(8, 2, 2, 2));
        return panel;
    }

    private void updateTutorFieldsVisibility() {
        boolean isTutor = registerTutorRadio.isSelected();
        tutorExtraPanel.setVisible(isTutor);
        if (gradeSelectButton != null) {
            gradeSelectButton.setEnabled(isTutor);
        }

        if (subjectSelectButton != null) {
            subjectSelectButton.setEnabled(isTutor && hasAnyGradeSelected());
        }
        tutorProvinceCombo.setEnabled(isTutor);
        tutorPriceField.setEnabled(isTutor);
        tutorExperienceArea.setEnabled(isTutor);
        tutorExtraPanel.revalidate();
        tutorExtraPanel.repaint();
    }

    private void handleLogin() {
        String username = loginUsernameField.getText().trim();
        String password = new String(loginPasswordField.getPassword());

        
        if (username.isEmpty() || password.isEmpty()) {
            showError("Vui lòng nhập đầy đủ tên đăng nhập và mật khẩu.");
            return;
        }

        User user = userDAO.getUserByUsername(username);
        if (user == null) {
            showError("Tài khoản không tồn tại.");
            return;
        }

        if (!password.equals(user.getPassword())) {
            showError("Mật khẩu không đúng.");
            return;
        }

        JOptionPane.showMessageDialog(this,
                "Đăng nhập thành công. Xin chào " + user.getFullName() + "!",
                "Thành công",
                JOptionPane.INFORMATION_MESSAGE);
        if ("ADMIN".equals(user.getRole())) {
        new AdminUI().setVisible(true);
    } 
    else if ("TUTOR".equals(user.getRole())) {
        model.Tutor tutor = tutorDAO.getTutorById(user.getUserId());
        if (tutor != null && !tutor.isApproved()) {
            showError("Tài khoản gia sư của bạn đang chờ Admin phê duyệt.");
            return; 
        }
        new TutorUI(user).setVisible(true);
    } 
    else if ("STUDENT".equals(user.getRole())) {
        new StudentUI(user).setVisible(true);
    }
        dispose();
    }

    private void handleRegister() {
        String fullName = registerFullNameField.getText().trim();
        String username = registerUsernameField.getText().trim();
        String phone = registerPhoneField.getText().trim();
        String password = new String(registerPasswordField.getPassword());
        String confirmPassword = new String(registerConfirmPasswordField.getPassword());

        if (fullName.isEmpty() || username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            showError("Vui lòng nhập đầy đủ các trường bắt buộc.");
            return;
        }

        if (!password.equals(confirmPassword)) {
            showError("Mật khẩu nhập lại không khớp.");
            return;
        }

        if (password.length() < 3) {
            showError("Mật khẩu phải có ít nhất 3 ký tự.");
            return;
        }

        if (userDAO.getUserByUsername(username) != null) {
            showError("Tên đăng nhập đã tồn tại.");
            return;
        }

        String role = mapRole();
        User user = new User(username, password, fullName, phone, role);

        try {
            Connection conn = DBConnection.getConnection();
            boolean oldAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);

            int userId;
            try {
                userId = userDAO.insertUser(conn, user);
                if (userId <= 0) {
                    throw new SQLException("Không thể tạo tài khoản người dùng.");
                }

                if ("TUTOR".equals(role)) {
                    List<String> selectedGrades = collectSelectedGrades();
                    List<String> selectedSubjects = collectSelectedSubjects();
                    String provinceName = tutorProvinceCombo.getSelectedItem() == null
                            ? ""
                            : tutorProvinceCombo.getSelectedItem().toString().trim();
                    String priceText = tutorPriceField.getText().trim();
                    String experience = tutorExperienceArea.getText().trim();

                    if (selectedGrades.isEmpty()) {
                        throw new SQLException("Gia sư cần chọn ít nhất 1 lớp dạy trước khi chọn môn.");
                    }

                    if (selectedSubjects.isEmpty()) {
                        throw new SQLException("Gia sư cần chọn ít nhất 1 môn dạy.");
                    }

                    if (priceText.isEmpty() || provinceName.isEmpty()) {
                        throw new SQLException("Gia sư cần chọn 1 tỉnh/thành và nhập học phí/giờ.");
                    }

                    Integer provinceId = tutorDAO.getProvinceIdByName(conn, provinceName);
                    if (provinceId == null) {
                        throw new SQLException("Không tìm thấy tỉnh/thành: " + provinceName);
                    }

                    BigDecimal pricePerHour = new BigDecimal(priceText);
                    Tutor tutor = new Tutor();
                    tutor.setTutorId(userId);
                    tutor.setPricePerHour(pricePerHour);
                    tutor.setExperience(experience);
                    tutor.setStatus("AVAILABLE");
                    tutor.setProvinceId(provinceId);

                    if (!tutorDAO.insertTutor(conn, tutor)) {
                        throw new SQLException("Không thể tạo hồ sơ gia sư.");
                    }
                    if (!tutorDAO.insertTutorGrades(conn, userId, selectedGrades)) {
                        throw new SQLException("Không thể lưu danh sách lớp dạy.");
                    }

                    for (String subjectName : selectedSubjects) {
                        Integer subjectId = tutorDAO.getSubjectIdByName(conn, subjectName);
                        if (subjectId == null) {
                            throw new SQLException("Không tìm thấy môn học: " + subjectName);
                        }

                        if (!tutorDAO.insertTutorSubject(conn, userId, subjectId)) {
                            throw new SQLException("Không thể liên kết môn học: " + subjectName);
                        }
                    }
                }

                conn.commit();
            } catch (Exception ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(oldAutoCommit);
            }

            showInfo("Đăng ký thành công. Bạn có thể đăng nhập ngay bây giờ.");
            clearRegisterForm();
            tabs.setSelectedIndex(0);
        } catch (Exception e) {
            showError("Đăng ký thất bại: " + e.getMessage());
        }
    }

    private String mapRole() {
        if (registerTutorRadio.isSelected()) {
            return "TUTOR";
        }
        return "STUDENT";
    }

    private List<String> collectSelectedSubjects() {
        return subjectCheckboxes.stream()
                .filter(JCheckBox::isSelected)
                .map(JCheckBox::getText)
                .toList();
    }

    private List<String> collectSelectedGrades() {
        return gradeCheckboxes.stream()
                .filter(JCheckBox::isSelected)
                .map(JCheckBox::getText)
                .toList();
    }

    private boolean hasAnyGradeSelected() {
        for (JCheckBox cb : gradeCheckboxes) {
            if (cb.isSelected()) return true;
        }
        return false;
    }

    private void clearRegisterForm() {
        registerFullNameField.setText("");
        registerUsernameField.setText("");
        registerPhoneField.setText("");
        registerStudentRadio.setSelected(true);
        registerPasswordField.setText("");
        registerConfirmPasswordField.setText("");
        gradeCheckboxes.forEach(cb -> cb.setSelected(false));
        updateGradeButtonLabel();
        subjectCheckboxes.forEach(cb -> cb.setSelected(false));
        updateSubjectButtonLabel();
        updateSubjectEnabledState();
        tutorProvinceCombo.setSelectedIndex(-1);
        tutorPriceField.setText("");
        tutorExperienceArea.setText("");
        updateTutorFieldsVisibility();
    }

    private JPanel buttonRow(JButton button) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.add(button, BorderLayout.CENTER);
        row.setBorder(new EmptyBorder(8, 0, 0, 0));
        return row;
    }

    private JComponent createRolePanel() {
        JPanel rolePanel = new JPanel(new BorderLayout(12, 0));
        rolePanel.setOpaque(false);

        JPanel options = new JPanel(new FlowLayout(FlowLayout.LEFT, 18, 0));
        options.setOpaque(false);
        options.add(registerStudentRadio);
        options.add(registerTutorRadio);

        rolePanel.add(options, BorderLayout.CENTER);
        return rolePanel;
    }

    private JComponent createGradePanel() {
        gradeCheckboxes.clear();
        List<String> grades = tutorDAO.getAllGradeNames();
        if (grades.isEmpty()) {
            List<String> fallback = new ArrayList<>();
            for (int i = 1; i <= 12; i++) fallback.add("Lớp " + i);
            grades = fallback;
        }

        for (String grade : grades) {
            JCheckBox checkbox = new JCheckBox(grade);
            checkbox.setFont(formFont());
            checkbox.setOpaque(false);
            checkbox.addItemListener(e -> {
                updateGradeButtonLabel();
                updateSubjectEnabledState();
            });
            gradeCheckboxes.add(checkbox);
        }

        gradeSelectButton = new JButton("Chọn lớp");
        gradeSelectButton.setFont(formFont());
        gradeSelectButton.addActionListener(e -> showGradeDialog());
        updateGradeButtonLabel();

        return gradeSelectButton;
    }

    private JComponent createSubjectPanel() {
        subjectCheckboxes.clear();
        List<String> subjects = tutorDAO.getAllSubjectNames();
        if (subjects.isEmpty()) {
            subjects = List.of("Toán", "Lý", "Hóa", "Tiếng Anh", "Tin Học");
        }

        for (String subject : subjects) {
            JCheckBox checkbox = new JCheckBox(subject);
            checkbox.setFont(formFont());
            checkbox.setOpaque(false);
            checkbox.addItemListener(e -> updateSubjectButtonLabel());
            subjectCheckboxes.add(checkbox);
        }

        subjectSelectButton = new JButton("Chọn môn");
        subjectSelectButton.setFont(formFont());
        subjectSelectButton.addActionListener(e -> showSubjectDialog());
        updateSubjectButtonLabel();

        // Default: disabled until user selected at least one grade
        subjectSelectButton.setEnabled(false);

        return subjectSelectButton;
    }

    private JComponent createProvinceCombo() {
        List<String> provinces = tutorDAO.getAllProvinceNames();
        if (provinces.isEmpty()) {
            provinces = List.of("Đà Nẵng", "Hà Nội", "TP Hồ Chí Minh", "Quảng Nam", "Huế");
        }

        tutorProvinceCombo = new JComboBox<>(provinces.toArray(new String[0]));
        tutorProvinceCombo.setSelectedIndex(-1);
        tutorProvinceCombo.setFont(formFont());
        return tutorProvinceCombo;
    }

    private void updateGradeButtonLabel() {
        List<String> selected = collectSelectedGrades();
        if (gradeSelectButton == null) return;
        if (selected.isEmpty()) {
            gradeSelectButton.setText("Chọn lớp");
        } else {
            gradeSelectButton.setText(String.join(", ", selected));
        }
    }

    private void updateSubjectEnabledState() {
        boolean isTutor = registerTutorRadio != null && registerTutorRadio.isSelected();
        if (subjectSelectButton != null) {
            subjectSelectButton.setEnabled(isTutor && hasAnyGradeSelected());
        }
    }

    private void updateSubjectButtonLabel() {
        List<String> selected = collectSelectedSubjects();
        if (selected.isEmpty()) {
            subjectSelectButton.setText("Chọn môn");
        } else {
            subjectSelectButton.setText(String.join(", ", selected));
        }
    }

    private void showGradeDialog() {
        JPanel checkboxPanel = new JPanel();
        checkboxPanel.setLayout(new BoxLayout(checkboxPanel, BoxLayout.Y_AXIS));
        checkboxPanel.setBackground(Color.WHITE);

        for (JCheckBox checkbox : gradeCheckboxes) {
            checkboxPanel.add(checkbox);
        }

        JScrollPane scrollPane = new JScrollPane(checkboxPanel);
        scrollPane.setPreferredSize(new Dimension(300, 200));

        JOptionPane.showConfirmDialog(this, scrollPane, "Chọn lớp dạy",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        // Refresh enablement in case user changed selection.
        updateGradeButtonLabel();
        updateSubjectEnabledState();
    }

    private void showSubjectDialog() {
        JPanel checkboxPanel = new JPanel();
        checkboxPanel.setLayout(new BoxLayout(checkboxPanel, BoxLayout.Y_AXIS));
        checkboxPanel.setBackground(Color.WHITE);

        for (JCheckBox checkbox : subjectCheckboxes) {
            checkboxPanel.add(checkbox);
        }

        JScrollPane scrollPane = new JScrollPane(checkboxPanel);
        scrollPane.setPreferredSize(new Dimension(300, 200));

        JOptionPane.showConfirmDialog(this, scrollPane, "Chọn môn dạy", 
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Thông báo", JOptionPane.ERROR_MESSAGE);
    }

    private void showInfo(String message) {
        JOptionPane.showMessageDialog(this, message, "Thông báo", JOptionPane.INFORMATION_MESSAGE);
    }

    private JLabel createBadge(String text) {
        JLabel badge = new JLabel(text);
        badge.setOpaque(true);
        badge.setBackground(new Color(59, 130, 246));
        badge.setForeground(Color.WHITE);
        badge.setFont(new Font("Segoe UI", Font.BOLD, 12));
        badge.setBorder(new EmptyBorder(8, 12, 8, 12));
        return badge;
    }

    private JButton createPrimaryButton(String text) {
        JButton button = new JButton(text);
        button.setFocusPainted(false);
        button.setBackground(new Color(15, 118, 110));
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setBorder(new EmptyBorder(12, 16, 12, 16));
        button.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        return button;
    }

    private void addField(JPanel panel, int row, String labelText, Component field) {
        GridBagConstraints labelConstraints = rowConstraints(row);
        labelConstraints.insets = new Insets(0, 0, 6, 0);
        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Segoe UI", Font.BOLD, 13));
        label.setForeground(new Color(51, 65, 85));
        panel.add(label, labelConstraints);

        GridBagConstraints fieldConstraints = rowConstraints(row);
        fieldConstraints.gridy = row;
        fieldConstraints.gridx = 1;
        fieldConstraints.weightx = 1.0;
        fieldConstraints.insets = new Insets(0, 14, 12, 0);
        fieldConstraints.fill = GridBagConstraints.HORIZONTAL;
        JComponent wrapped = field instanceof JComponent ? (JComponent) field : wrapComponent(field);
        styleField(wrapped);
        panel.add(wrapped, fieldConstraints);
    }

    private GridBagConstraints rowConstraints(int row) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.anchor = GridBagConstraints.WEST;
        return gbc;
    }

    private JComponent wrapArea(JTextArea area) {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(Color.WHITE);
        wrapper.setBorder(BorderFactory.createLineBorder(new Color(203, 213, 225)));
        wrapper.add(area, BorderLayout.CENTER);
        return wrapper;
    }

    private JComponent wrapComponent(Component component) {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(Color.WHITE);
        wrapper.add(component, BorderLayout.CENTER);
        return wrapper;
    }

    private void styleField(JComponent component) {
        component.setFont(formFont());
        if (component instanceof JTextField) {
            ((JTextField) component).setBorder(new EmptyBorder(10, 10, 10, 10));
        } else if (component instanceof JPasswordField) {
            ((JPasswordField) component).setBorder(new EmptyBorder(10, 10, 10, 10));
        }
    }

    private Font formFont() {
        return new Font("Segoe UI", Font.PLAIN, 13);
    }

    private Component spacer() {
        JPanel spacer = new JPanel();
        spacer.setOpaque(false);
        spacer.setPreferredSize(new Dimension(1, 12));
        return spacer;
    }

    private static class GradientPanel extends JPanel {
        private final Color start;
        private final Color end;

        private GradientPanel(Color start, Color end) {
            this.start = start;
            this.end = end;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g2.setPaint(new GradientPaint(0, 0, start, getWidth(), getHeight(), end));
            g2.fillRect(0, 0, getWidth(), getHeight());
            g2.dispose();
        }
    }
}