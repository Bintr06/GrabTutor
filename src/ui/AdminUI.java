package ui;

import dao.AdminDAO;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.Map;

public class AdminUI extends JFrame {
    private AdminDAO adminDAO = new AdminDAO();
    private DefaultTableModel tutorModel;
    private JLabel statsLabel;
    private DefaultTableModel tutorListModel;
    private JTable tutorTable;  
    private DefaultTableModel studentListModel;

    public AdminUI() {
        setTitle("GrabTutor - Quản Trị Hệ Thống");
        setSize(1200, 800); 
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(52, 73, 94)); 
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        
        headerPanel.setBackground(new Color(52, 73, 94)); 
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        JLabel titleLabel = new JLabel("HỆ THỐNG QUẢN TRỊ GRABTUTOR");
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        headerPanel.add(titleLabel, BorderLayout.WEST);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0)); // Thêm khoảng cách 10px giữa các nút
        btnPanel.setOpaque(false);
            
        JButton btnUpdate = new JButton("Cập nhật thông tin");
        btnUpdate.setBackground(new Color(52, 152, 219)); 
        btnUpdate.setForeground(Color.BLACK);
        btnUpdate.setFocusable(false);

    btnUpdate.addActionListener(e -> {
    int selectedRow = tutorTable.getSelectedRow(); 
    if (selectedRow != -1) {
        String username = (String) tutorTable.getValueAt(selectedRow, 1);
        String currentName = (String) tutorTable.getValueAt(selectedRow, 0);

        String[] options = {"Sửa Họ Tên", "Đổi Mật Khẩu"};
        int choice = JOptionPane.showOptionDialog(this, 
                "Bạn muốn cập nhật thông tin gì cho " + username + "?",
                "Tùy chọn cập nhật",
                JOptionPane.DEFAULT_OPTION, 
                JOptionPane.QUESTION_MESSAGE, 
                null, options, options[0]);

        if (choice == 0) {
            String newName = JOptionPane.showInputDialog(this, "Nhập tên mới:", currentName);
            if (newName != null && !newName.trim().isEmpty()) {
                if (adminDAO.updateUserInfoByUsername(username, newName.trim())) {
                    JOptionPane.showMessageDialog(this, "Cập nhật tên thành công!");
                    refreshData(); 
                }
            }
        } else if (choice == 1) { 
            String newPass = JOptionPane.showInputDialog(this, "Nhập mật khẩu mới cho " + username + ":");
            if (newPass != null && !newPass.trim().isEmpty()) {
                if (adminDAO.updatePasswordByUsername(username, newPass.trim())) {
                    JOptionPane.showMessageDialog(this, "Đổi mật khẩu thành công!");
                }
            }
        }
    } else {
        JOptionPane.showMessageDialog(this, "Vui lòng chọn một gia sư từ bảng Thống kê!");
    }
});

    JButton btnLogout = new JButton("Đăng xuất");
    btnLogout.setFocusable(false);
    btnLogout.setBackground(new Color(231, 76, 60));
    btnLogout.setForeground(Color.BLACK);
    btnLogout.addActionListener(e -> handleLogout());

    btnPanel.add(btnUpdate);
    btnPanel.add(btnLogout);
    headerPanel.add(btnPanel, BorderLayout.EAST);

    JTabbedPane tabs = new JTabbedPane();
    tabs.addTab("Duyệt Gia Sư", createApprovalTab());
    tabs.addTab("Thống Kê Hệ Thống", createStatsTab());
    
    setLayout(new BorderLayout());
    add(headerPanel, BorderLayout.NORTH);
    add(tabs, BorderLayout.CENTER);

    refreshData();
    setVisible(true);
    }

    private void handleLogout() {
    int confirm = JOptionPane.showConfirmDialog(this, 
        "Bạn có chắc chắn muốn đăng xuất không?", 
        "Xác nhận", 
        JOptionPane.YES_NO_OPTION);
        
    if (confirm == JOptionPane.YES_OPTION) {
        this.dispose(); 
        new AuthFrame().setVisible(true); 
    }
}
    private JPanel createApprovalTab() {
        JPanel p = new JPanel(new BorderLayout(15, 15));
        p.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        String[] cols = {"ID", "Tên Gia Sư", "Kinh Nghiệm", "Học Phí/Giờ"};
        tutorModel = new DefaultTableModel(cols, 0) { @Override public boolean isCellEditable(int r, int c) { return false; } };
        JTable table = new JTable(tutorModel);
        table.setRowHeight(35);
        table.setFont(new Font("Arial", Font.PLAIN, 14));

        JButton btnApprove = new JButton("PHÊ DUYỆT GIA SƯ ĐÃ CHỌN");
        btnApprove.setFont(new Font("Arial", Font.BOLD, 16));
        btnApprove.setBackground(new Color(46, 204, 113));
        btnApprove.setForeground(Color.BLACK);
        btnApprove.setPreferredSize(new Dimension(300, 50));

        btnApprove.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row >= 0) {
                int id = (int) tutorModel.getValueAt(row, 0);
                if (adminDAO.approveTutor(id)) {
                    JOptionPane.showMessageDialog(this, "Đã duyệt gia sư thành công!");
                    refreshData();
                }
            } else {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn một gia sư để duyệt.");
            }
        });

        p.add(new JScrollPane(table), BorderLayout.CENTER);
        p.add(btnApprove, BorderLayout.SOUTH);
        return p;
    }

    private JPanel createStatsTab() {
    JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
    mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    
    statsLabel = new JLabel("Thống kê hệ thống", SwingConstants.CENTER);
    statsLabel.setFont(new Font("Arial", Font.BOLD, 16));
    mainPanel.add(statsLabel, BorderLayout.NORTH);
    
    JTabbedPane filterTabs = new JTabbedPane();
    
    tutorListModel = new DefaultTableModel(new String[]{"Họ Tên", "Username", "Mật khẩu", "SĐT", "Trạng thái"}, 0);
    tutorTable = new JTable(tutorListModel); 
    tutorTable.setRowHeight(30); 
    filterTabs.addTab("Danh sách Gia sư", new JScrollPane(tutorTable));
    studentListModel = new DefaultTableModel(new String[]{"Họ Tên", "Username", "Mật khẩu", "SĐT"}, 0);
    filterTabs.addTab("Danh sách Học sinh", new JScrollPane(new JTable(studentListModel)));

    mainPanel.add(filterTabs, BorderLayout.CENTER);
    return mainPanel;
}


private void refreshData() {
        tutorModel.setRowCount(0);
        List<Map<String, Object>> pending = adminDAO.getPendingTutors();
        for (Map<String, Object> t : pending) {
            tutorModel.addRow(new Object[]{t.get("id"), t.get("name"), t.get("exp"), t.get("price")});
        }

        Map<String, Integer> stats = adminDAO.getSystemStats();
        String summary = String.format("<html>Học Sinh: <font color='blue'>%d</font> | Gia Sư: <font color='green'>%d</font></html>",
                stats.getOrDefault("STUDENT", 0),
                stats.getOrDefault("TUTOR", 0));
        statsLabel.setText(summary);

        tutorListModel.setRowCount(0);
        List<Map<String, String>> tutors = adminDAO.getAllTutors();
        for (Map<String, String> tutorItem : tutors) {
            tutorListModel.addRow(new Object[]{
                tutorItem.get("name"), 
                tutorItem.get("username"), 
                tutorItem.get("password"),
                tutorItem.get("phone"), 
                tutorItem.get("status")
            });
        }

        studentListModel.setRowCount(0);
        List<Map<String, String>> students = adminDAO.getAllStudents();
        for (Map<String, String> studentItem : students) {
            studentListModel.addRow(new Object[]{
                studentItem.get("name"), 
                studentItem.get("username"),
                studentItem.get("password"),
                studentItem.get("phone")
            });
        }
    }
}
