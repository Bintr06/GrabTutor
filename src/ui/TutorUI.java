package ui;

import dao.BookingDAO;
import dao.TutorDAO;
import dao.UserDAO;
import model.Booking;
import model.Tutor;
import model.User;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class TutorUI extends JFrame {
    private final User currentUser;
    private final BookingDAO bookingDAO = new BookingDAO();
    private final UserDAO userDAO = new UserDAO();
    private final TutorDAO tutorDAO = new TutorDAO();
    private DefaultTableModel newReqModel;
    private JTable newReqTable;
    private DefaultTableModel scheduleModel;
    private JTable scheduleTable;
    private DefaultTableModel earningsModel;
    private JTable earningsTable;
    private JLabel earningsLabel;

    public TutorUI(User user) {
        this.currentUser = user;
        setupUI();
    }

    private void setupUI() {
        setTitle("GrabTutor - Bảng điều khiển (Gia sư: " + currentUser.getFullName() + ")");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(900, 700);
        setLocationRelativeTo(null);
        setResizable(true);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Quản lý hồ sơ", createProfileTab());
        tabs.addTab("Yêu cầu mới", createNewRequestsTab());
        tabs.addTab("Lịch dạy / Lịch sử", createScheduleTab());
        tabs.addTab("Thống kê thu nhập", createEarningsTab());

        JPanel header = new JPanel(new BorderLayout());
            JPanel spacer = new JPanel();
            header.add(spacer, BorderLayout.CENTER);
            JButton logoutBtn = new JButton("Đăng xuất");
            logoutBtn.addActionListener(e -> {
                new AuthFrame().setVisible(true);
                dispose();
            });
            JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            right.setOpaque(false);
            right.add(logoutBtn);
            header.add(right, BorderLayout.EAST);

        JPanel container = new JPanel(new BorderLayout());
        container.add(header, BorderLayout.NORTH);
        container.add(tabs, BorderLayout.CENTER);
        add(container);
        setVisible(true);
    }

    private JPanel createProfileTab() {
        User latestUser = userDAO.getUserById(currentUser.getUserId());
        Tutor tutor = tutorDAO.getTutorById(currentUser.getUserId());

        JPanel p = new JPanel(new GridBagLayout());
        p.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0; p.add(new JLabel("Họ và tên:"), gbc);
        JTextField fullNameField = new JTextField(latestUser != null && latestUser.getFullName() != null ? latestUser.getFullName() : "", 24);
        gbc.gridx = 1; p.add(fullNameField, gbc);

        gbc.gridx = 0; gbc.gridy = 1; p.add(new JLabel("Số điện thoại:"), gbc);
        JTextField phoneField = new JTextField(latestUser != null && latestUser.getPhone() != null ? latestUser.getPhone() : "", 24);
        gbc.gridx = 1; p.add(phoneField, gbc);

        gbc.gridx = 0; gbc.gridy = 2; p.add(new JLabel("Học phí/giờ:"), gbc);
        JTextField priceField = new JTextField(tutor != null && tutor.getPricePerHour() != null ? tutor.getPricePerHour().toPlainString() : "", 12);
        gbc.gridx = 1; p.add(priceField, gbc);

        gbc.gridx = 0; gbc.gridy = 3; gbc.anchor = GridBagConstraints.WEST; p.add(new JLabel("Tỉnh/Thành:"), gbc);
        JComboBox<String> provinceBox = new JComboBox<>();
        provinceBox.addItem("");
        tutorDAO.getAllProvinceNames().forEach(provinceBox::addItem);
        if (tutor != null && tutor.getProvinceName() != null) provinceBox.setSelectedItem(tutor.getProvinceName());
        gbc.gridx = 1; p.add(provinceBox, gbc);

        gbc.gridx = 0; gbc.gridy = 4; gbc.anchor = GridBagConstraints.NORTHWEST; p.add(new JLabel("Lớp dạy:"), gbc);
        JPanel gradesPanel = new JPanel();
        gradesPanel.setLayout(new GridLayout(0, 2, 5, 5));
        List<String> allGrades = tutorDAO.getAllGradeNames();
        if (allGrades.isEmpty()) {
            allGrades = new ArrayList<>();
            for (int i = 1; i <= 12; i++) allGrades.add("Lớp " + i);
        }

List<String> selectedGrades = tutor != null ? tutorDAO.getGradeNamesByTutorId(tutor.getTutorId()) : new ArrayList<>();
System.out.println("DEBUG - Danh sach lop tu DB: " + selectedGrades); 

List<JCheckBox> gradeChecks = new ArrayList<>();
for (String g : allGrades) {
    JCheckBox cb = new JCheckBox(g);
    boolean isSelected = false;

    for (String s : selectedGrades) {
        if (s != null && s.trim().equalsIgnoreCase(g.trim())) {
            isSelected = true;
            break;
        }
    }
    
    cb.setSelected(isSelected);
    gradeChecks.add(cb); 
    gradesPanel.add(cb);
}
        JScrollPane gradeScroll = new JScrollPane(gradesPanel);
        gradeScroll.setPreferredSize(new Dimension(320, 250));
        gbc.gridx = 1; p.add(gradeScroll, gbc);

        gbc.gridx = 0; gbc.gridy = 5; gbc.anchor = GridBagConstraints.NORTHWEST; p.add(new JLabel("Môn dạy:"), gbc);
        JPanel subjectsPanel = new JPanel();
        subjectsPanel.setLayout(new BoxLayout(subjectsPanel, BoxLayout.Y_AXIS));
        List<String> allSubjects = tutorDAO.getAllSubjectNames();
        List<String> selected = tutor != null ? tutorDAO.getSubjectNamesByTutorId(tutor.getTutorId()) : List.of();
        List<JCheckBox> subjectChecks = new ArrayList<>();
        for (String s : allSubjects) {
            JCheckBox cb = new JCheckBox(s);
            boolean isSelected = false;
            for (String selectedSubject : selected) {
                if (selectedSubject != null && selectedSubject.trim().equalsIgnoreCase(s.trim())) {
                    isSelected = true;
                    break;
                }
            }
            cb.setSelected(isSelected);
            
            subjectChecks.add(cb);
            subjectsPanel.add(cb);
        }
        JScrollPane subjScroll = new JScrollPane(subjectsPanel);
        subjScroll.setPreferredSize(new Dimension(320, 250));
        gbc.gridx = 1; p.add(subjScroll, gbc);

        gbc.gridx = 0; gbc.gridy = 6; gbc.anchor = GridBagConstraints.NORTHWEST; p.add(new JLabel("Kinh nghiệm:"), gbc);
        JTextArea expArea = new JTextArea(tutor != null && tutor.getExperience() != null ? tutor.getExperience() : "", 6, 32);
        expArea.setLineWrap(true);
        expArea.setWrapStyleWord(true);
        gbc.gridx = 1; p.add(new JScrollPane(expArea), gbc);

        gbc.gridx = 0; gbc.gridy = 7; gbc.anchor = GridBagConstraints.WEST; p.add(new JLabel("Trạng thái:"), gbc);
        JRadioButton avail = new JRadioButton("AVAILABLE");
        JRadioButton busy = new JRadioButton("BUSY");
        ButtonGroup bg = new ButtonGroup(); bg.add(avail); bg.add(busy);
        String curStatus = tutor != null && tutor.getStatus() != null ? tutor.getStatus() : "AVAILABLE";
        if ("BUSY".equalsIgnoreCase(curStatus)) busy.setSelected(true); else avail.setSelected(true);
        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        statusPanel.add(avail);
        statusPanel.add(busy);
        gbc.gridx = 1; p.add(statusPanel, gbc);

        JButton saveBtn = new JButton("Lưu thay đổi");
        gbc.gridx = 1; gbc.gridy = 8; gbc.anchor = GridBagConstraints.WEST; p.add(saveBtn, gbc);

        saveBtn.addActionListener(e -> {
            try {
                String newFull = fullNameField.getText().trim();
                String newPhone = phoneField.getText().trim();
                BigDecimal newPrice = new BigDecimal(priceField.getText().trim());
                String provinceName = (String) provinceBox.getSelectedItem();
                Integer provinceId = provinceName == null || provinceName.isBlank() ? null : tutorDAO.getProvinceIdByName(provinceName);

                List<String> newGrades = gradeChecks.stream().filter(AbstractButton::isSelected).map(AbstractButton::getText).toList();
                List<String> newSubjects = subjectChecks.stream().filter(AbstractButton::isSelected).map(AbstractButton::getText).toList();
                String newExp = expArea.getText().trim();
                String newStatus = avail.isSelected() ? "AVAILABLE" : "BUSY";

                if (newFull.isBlank()) { JOptionPane.showMessageDialog(this, "Họ tên không được để trống.", "Lỗi", JOptionPane.ERROR_MESSAGE); return; }

                User u = userDAO.getUserById(currentUser.getUserId());
                u.setFullName(newFull); u.setPhone(newPhone);
                Tutor tu = tutorDAO.getTutorById(currentUser.getUserId());
                tu.setPricePerHour(newPrice); tu.setExperience(newExp); tu.setStatus(newStatus); tu.setProvinceId(provinceId); tu.setProvinceName(provinceName);

                boolean ok1 = userDAO.updateUser(u);
                boolean ok2 = tutorDAO.updateTutor(tu);
                boolean ok3 = tutorDAO.replaceTutorGrades(currentUser.getUserId(), newGrades);
                boolean ok4 = tutorDAO.replaceTutorSubjects(currentUser.getUserId(), newSubjects);
                if (ok1 && ok2 && ok3 && ok4) {
                    JOptionPane.showMessageDialog(this, "Cập nhật hồ sơ thành công.", "Thành công", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this, "Không thể lưu đầy đủ thay đổi.", "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Lỗi khi lưu: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        });

        return p;
    }
    private JPanel createNewRequestsTab() {
        JPanel p = new JPanel(new BorderLayout(8,8));
        p.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

        String[] cols = new String[]{"ID","Học sinh","Ngày đặt","Ghi chú"};
        newReqModel = new DefaultTableModel(cols,0) { @Override public boolean isCellEditable(int r,int c){return false;} };
        newReqTable = new JTable(newReqModel);
        TableColumn idc = newReqTable.getColumnModel().getColumn(0); idc.setMinWidth(0); idc.setMaxWidth(0); idc.setPreferredWidth(0); idc.setResizable(false);
        newReqTable.addMouseListener(new MouseListener() {
            @Override public void mouseClicked(MouseEvent e) {
                int row = newReqTable.rowAtPoint(e.getPoint());
                int col = newReqTable.columnAtPoint(e.getPoint());
                if (row >= 0 && col == 3) { // notes column (index 3)
                    int modelRow = newReqTable.convertRowIndexToModel(row);
                    Object notesObj = newReqModel.getValueAt(modelRow, 3);
                    String notes = notesObj != null ? notesObj.toString() : "(không có ghi chú)";
                    JTextArea textarea = new JTextArea(notes, 10, 40);
                    textarea.setLineWrap(true);
                    textarea.setWrapStyleWord(true);
                    textarea.setEditable(false);
                    JScrollPane scroll = new JScrollPane(textarea);
                    JOptionPane.showMessageDialog(TutorUI.this, scroll, "Ghi chú đầy đủ", JOptionPane.INFORMATION_MESSAGE);
                }
            }
            @Override public void mousePressed(MouseEvent e) {}
            @Override public void mouseReleased(MouseEvent e) {}
            @Override public void mouseEntered(MouseEvent e) {}
            @Override public void mouseExited(MouseEvent e) {}
        });
        
        p.add(new JScrollPane(newReqTable), BorderLayout.CENTER);

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton accept = new JButton("Chấp nhận");
        JButton reject = new JButton("Từ chối");
        accept.setEnabled(false);
        reject.setEnabled(false);
        btns.add(accept); btns.add(reject);
        p.add(btns, BorderLayout.SOUTH);
        newReqTable.getSelectionModel().addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) return;
            int sel = newReqTable.getSelectedRow();
            if (sel < 0) { accept.setEnabled(false); reject.setEnabled(false); return; }
            int modelRow = newReqTable.convertRowIndexToModel(sel);
            Object idObj = newReqModel.getValueAt(modelRow, 0);
            if (idObj == null) { accept.setEnabled(false); reject.setEnabled(false); return; }
            Booking b = bookingDAO.getBookingById((int) idObj);
            boolean isPending = b != null && "PENDING".equalsIgnoreCase(b.getStatus());
            Tutor tutor = tutorDAO.getTutorById(currentUser.getUserId());
            boolean tutorBusy = tutor != null && "BUSY".equalsIgnoreCase(tutor.getStatus());
            accept.setEnabled(isPending && !tutorBusy);
            reject.setEnabled(true);
        });

        accept.addActionListener(e -> {
            int sel = newReqTable.getSelectedRow(); if (sel<0) return;
            int modelRow = newReqTable.convertRowIndexToModel(sel);
            int bookingId = (int)newReqModel.getValueAt(modelRow,0);
            Booking b = bookingDAO.getBookingById(bookingId);
            if (b==null) return;
            Tutor tcheck = tutorDAO.getTutorById(currentUser.getUserId());
            if (tcheck != null && "BUSY".equalsIgnoreCase(tcheck.getStatus())) {
                JOptionPane.showMessageDialog(this, "Bạn đang bận và không thể nhận thêm booking.", "Không thể chấp nhận", JOptionPane.WARNING_MESSAGE);
                refreshAll();
                return;
            }
            b.setStatus("ACCEPTED");
            if (bookingDAO.updateBooking(b)) {
                Tutor t = tutorDAO.getTutorById(currentUser.getUserId()); if (t!=null){ t.setStatus("BUSY"); tutorDAO.updateTutor(t);}                
                JOptionPane.showMessageDialog(this,"Đã chấp nhận yêu cầu.","OK",JOptionPane.INFORMATION_MESSAGE);
                refreshAll();
            }
        });

        reject.addActionListener(e -> {
            int sel = newReqTable.getSelectedRow(); if (sel<0) return;
            int modelRow = newReqTable.convertRowIndexToModel(sel);
            int bookingId = (int)newReqModel.getValueAt(modelRow,0);
            Booking b = bookingDAO.getBookingById(bookingId);
            if (b==null) return;
            b.setStatus("REJECTED");
            if (bookingDAO.updateBooking(b)) { JOptionPane.showMessageDialog(this,"Đã từ chối.","OK",JOptionPane.INFORMATION_MESSAGE); refreshAll(); }
        });

        refreshNewRequests();
        return p;
    }

    private void refreshNewRequests(){
        newReqModel.setRowCount(0);
        List<Booking> bookings = bookingDAO.getBookingsByTutorId(currentUser.getUserId());
        for (Booking b: bookings) if ("PENDING".equalsIgnoreCase(b.getStatus())){
            User s = userDAO.getUserById(b.getStudentId()); String student = s!=null? s.getFullName():"#"+b.getStudentId();
            newReqModel.addRow(new Object[]{b.getBookingId(), student, b.getBookingDate(), b.getNotes()});
        }
    }
    private JPanel createScheduleTab(){
        JPanel p = new JPanel(new BorderLayout(8,8)); p.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        String[] cols = new String[]{"ID","Học sinh","Ngày","Ghi chú","Trạng thái"};
        scheduleModel = new DefaultTableModel(cols,0){@Override public boolean isCellEditable(int r,int c){return false;}};
        scheduleTable = new JTable(scheduleModel);
        TableColumn idc = scheduleTable.getColumnModel().getColumn(0); idc.setMinWidth(0); idc.setMaxWidth(0); idc.setPreferredWidth(0); idc.setResizable(false);
        scheduleTable.addMouseListener(new MouseListener() {
            @Override public void mouseClicked(MouseEvent e) {
                int row = scheduleTable.rowAtPoint(e.getPoint());
                int col = scheduleTable.columnAtPoint(e.getPoint());
                if (row >= 0 && col == 3) { 
                    int modelRow = scheduleTable.convertRowIndexToModel(row);
                    Object notesObj = scheduleModel.getValueAt(modelRow, 3);
                    String notes = notesObj != null ? notesObj.toString() : "(không có ghi chú)";
                    JTextArea textarea = new JTextArea(notes, 10, 40);
                    textarea.setLineWrap(true);
                    textarea.setWrapStyleWord(true);
                    textarea.setEditable(false);
                    JScrollPane scroll = new JScrollPane(textarea);
                    JOptionPane.showMessageDialog(TutorUI.this, scroll, "Ghi chú đầy đủ", JOptionPane.INFORMATION_MESSAGE);
                }
            }
            @Override public void mousePressed(MouseEvent e) {}
            @Override public void mouseReleased(MouseEvent e) {}
            @Override public void mouseEntered(MouseEvent e) {}
            @Override public void mouseExited(MouseEvent e) {}
        });
        p.add(new JScrollPane(scheduleTable), BorderLayout.CENTER);
        JPanel btns = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton startBtn = new JButton("Bắt đầu dạy");
        JButton complete = new JButton("Hoàn thành");
        startBtn.setEnabled(false);
        complete.setEnabled(false);
        btns.add(startBtn);
        btns.add(complete);
        p.add(btns, BorderLayout.SOUTH);
        scheduleTable.getSelectionModel().addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) return;
            int sel = scheduleTable.getSelectedRow();
            if (sel < 0) {
                startBtn.setEnabled(false);
                complete.setEnabled(false);
                return;
            }
            int modelRow = scheduleTable.convertRowIndexToModel(sel);
            Object statusObj = scheduleModel.getValueAt(modelRow, 4);
            String status = statusObj != null ? statusObj.toString() : "";
            startBtn.setEnabled("ACCEPTED".equalsIgnoreCase(status));
            complete.setEnabled("IN_PROGRESS".equalsIgnoreCase(status));
        });

        startBtn.addActionListener(e -> {
            int sel = scheduleTable.getSelectedRow(); if (sel<0) return; int modelRow = scheduleTable.convertRowIndexToModel(sel);
            int bookingId = (int)scheduleModel.getValueAt(modelRow,0); Booking b = bookingDAO.getBookingById(bookingId); if (b==null) return;
            if (!"ACCEPTED".equalsIgnoreCase(b.getStatus())) { JOptionPane.showMessageDialog(this,"Chỉ có thể bắt đầu booking đã được chấp nhận.","Lỗi",JOptionPane.WARNING_MESSAGE); return; }
            if (JOptionPane.showConfirmDialog(this, "Xác nhận bắt đầu buổi học? Học sinh sẽ được yêu cầu xác nhận.","Xác nhận",JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                if (bookingDAO.markTutorStartRequested(bookingId)) {
                    JOptionPane.showMessageDialog(this, "Đã gửi yêu cầu bắt đầu. Đang chờ học sinh xác nhận.","OK",JOptionPane.INFORMATION_MESSAGE);
                    refreshAll();
                } else {
                    JOptionPane.showMessageDialog(this, "Lỗi khi gửi yêu cầu.","Lỗi",JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        complete.addActionListener(e->{
            int sel = scheduleTable.getSelectedRow(); if (sel<0) return; int modelRow = scheduleTable.convertRowIndexToModel(sel);
            int bookingId = (int)scheduleModel.getValueAt(modelRow,0); Booking b = bookingDAO.getBookingById(bookingId); if (b==null) return;
            if (!"IN_PROGRESS".equalsIgnoreCase(b.getStatus())) { JOptionPane.showMessageDialog(this,"Chỉ hoàn thành booking đang diễn ra.","Lỗi",JOptionPane.WARNING_MESSAGE); return; }
            String notes = b.getNotes()==null? "" : b.getNotes();
            double hrs = 0;
            try {
                for (String line : notes.split("\n")) {
                    if (line.startsWith("IN_PROGRESS_START=")) {
                        String startTimeStr = line.substring("IN_PROGRESS_START=".length()).trim();
                        java.time.LocalDateTime startTime = java.time.LocalDateTime.parse(startTimeStr);
                        java.time.LocalDateTime endTime = java.time.LocalDateTime.now();
                        java.time.Duration duration = java.time.Duration.between(startTime, endTime);
                        hrs = duration.toMinutes() / 60.0;
                        break;
                    }
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,"Lỗi khi tính giờ.","Lỗi",JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (hrs <= 0) { JOptionPane.showMessageDialog(this,"Không tìm thấy thời gian bắt đầu.","Lỗi",JOptionPane.ERROR_MESSAGE); return; }
            Tutor t = tutorDAO.getTutorById(currentUser.getUserId()); double price = t!=null && t.getPricePerHour()!=null ? t.getPricePerHour().doubleValue() : 0.0; double earned = price * hrs;
            String newNotes = notes; if (!newNotes.isEmpty()) { newNotes += "\n"; } newNotes += "EARNED="+earned+";HOURS="+hrs; b.setNotes(newNotes); b.setStatus("COMPLETED");
            if (bookingDAO.updateBooking(b)){
                if (t!=null){ t.setStatus("AVAILABLE"); tutorDAO.updateTutor(t);} JOptionPane.showMessageDialog(this,"Đã hoàn thành. Thời gian: "+String.format("%.2f", hrs)+" giờ. Thu nhập: "+String.format("%,.0f", earned)+" VNĐ","OK",JOptionPane.INFORMATION_MESSAGE);
                refreshAll();
            }
        });

        refreshSchedule();
        return p;
    }

    private void refreshSchedule(){
        scheduleModel.setRowCount(0);
        List<Booking> bs = bookingDAO.getBookingsByTutorId(currentUser.getUserId());
        for(Booking b:bs) if("ACCEPTED".equalsIgnoreCase(b.getStatus())||"IN_PROGRESS".equalsIgnoreCase(b.getStatus())||"COMPLETED".equalsIgnoreCase(b.getStatus())){
            User s = userDAO.getUserById(b.getStudentId()); String student = s!=null? s.getFullName():"#"+b.getStudentId();
            scheduleModel.addRow(new Object[]{b.getBookingId(), student, b.getBookingDate(), b.getNotes(), b.getStatus()});
        }
    }

    // Earnings tab
    private JPanel createEarningsTab(){
        JPanel p = new JPanel(new BorderLayout(8,8)); p.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        earningsLabel = new JLabel("Tổng thu nhập: 0 VNĐ"); p.add(earningsLabel, BorderLayout.NORTH);
        String[] cols = new String[]{"Booking ID","Học sinh","Ngày","Giờ","Thu nhập"};
        earningsModel = new DefaultTableModel(cols,0){@Override public boolean isCellEditable(int r,int c){return false;}};
        earningsTable = new JTable(earningsModel);
        p.add(new JScrollPane(earningsTable), BorderLayout.CENTER);
        refreshEarnings();
        return p;
    }

    private void refreshEarnings(){
        earningsModel.setRowCount(0);
        double total=0;
        List<Booking> bs = bookingDAO.getBookingsByTutorId(currentUser.getUserId());
        for(Booking b:bs) if("COMPLETED".equalsIgnoreCase(b.getStatus())){
            double earned=0,hours=0;
            String notes=b.getNotes()==null?"":b.getNotes();
            try{
                for(String part: notes.split(";|\\n")){
                    if(part.startsWith("EARNED=")){ earned = Double.parseDouble(part.substring("EARNED=".length())); }
                    if(part.startsWith("HOURS=")){ hours = Double.parseDouble(part.substring("HOURS=".length())); }
                }
            }catch(Exception ignored){}
            User s = userDAO.getUserById(b.getStudentId()); String student = s!=null? s.getFullName():"#"+b.getStudentId();
            earningsModel.addRow(new Object[]{b.getBookingId(), student, b.getBookingDate(), hours, earned}); total+=earned;
        }
        earningsLabel.setText("Tổng thu nhập: " + String.format("%,.0f", total) + " VNĐ");
    }

    private void refreshAll(){
        refreshNewRequests(); refreshSchedule(); refreshEarnings();
    }
}
