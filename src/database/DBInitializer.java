package database;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DBInitializer {
    private static boolean initialized = false;

    public static synchronized void initializeTables() {
        if (initialized) {
            return;
        }

        try (Connection conn = DBConnection.getConnection()) {
            ensureGradesTable(conn);
            seedGrades(conn);
            ensureTutorGradesTableAndConstraints(conn);

            initialized = true;
            System.out.println("\u2713 DBInitializer: ensured grades + tutor_grades tables and seeded grades.");
        } catch (SQLException e) {
            System.err.println("DBInitializer error: " + e.getMessage());
        }
    }

    private static void ensureGradesTable(Connection conn) throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS grades (" +
                "id INT PRIMARY KEY AUTO_INCREMENT, " +
                "name VARCHAR(50) UNIQUE NOT NULL" +
                ")";

        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        }
    }

    private static void seedGrades(Connection conn) throws SQLException {
        String sql = "INSERT INTO grades (name) VALUES (?) ON DUPLICATE KEY UPDATE name = name";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int i = 1; i <= 12; i++) {
                ps.setString(1, "L\u1edbp " + i);
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private static void ensureTutorGradesTableAndConstraints(Connection conn) throws SQLException {
        String tutorsTableName = findExistingTableName(conn, "Tutors", "tutors");
        if (tutorsTableName == null) {
            System.err.println("DBInitializer warning: Tutors table not found; cannot add tutor_grades FK constraints.");
            return;
        }

        try (Statement stmt = conn.createStatement()) {
            // Create table structure first (no FK). This makes it possible to add constraints later.
            stmt.execute("CREATE TABLE IF NOT EXISTS tutor_grades (" +
                    "tutor_id INT NOT NULL, " +
                    "grade_id INT NOT NULL, " +
                    "PRIMARY KEY (tutor_id, grade_id)" +
                    ")");

            // Add FK constraints if not present (best-effort; ignore duplicates).
            String fkTutor = "ALTER TABLE tutor_grades " +
                    "ADD CONSTRAINT fk_tutor_grades_tutor " +
                    "FOREIGN KEY (tutor_id) REFERENCES `" + tutorsTableName + "`(tutor_id) ON DELETE CASCADE";

            String fkGrade = "ALTER TABLE tutor_grades " +
                    "ADD CONSTRAINT fk_tutor_grades_grade " +
                    "FOREIGN KEY (grade_id) REFERENCES grades(id) ON DELETE CASCADE";

            executeAlterIgnoreDuplicate(stmt, fkTutor, "fk_tutor_grades_tutor");
            executeAlterIgnoreDuplicate(stmt, fkGrade, "fk_tutor_grades_grade");
        }
    }

    private static void executeAlterIgnoreDuplicate(Statement stmt, String sql, String constraintName) throws SQLException {
        try {
            stmt.execute(sql);
        } catch (SQLException ex) {
            // MySQL common duplicate errors:
            // - 1061: Duplicate key name
            // - 1826: Duplicate foreign key constraint name
            int code = ex.getErrorCode();
            String msg = ex.getMessage() == null ? "" : ex.getMessage().toLowerCase();
            boolean isDuplicate = code == 1061 || code == 1826 || msg.contains("duplicate") || msg.contains("already exists");
            if (isDuplicate) {
                return;
            }

            System.err.println("DBInitializer error: cannot create FK constraint '" + constraintName + "': " + ex.getMessage());
            throw ex;
        }
    }

    private static String findExistingTableName(Connection conn, String... candidates) throws SQLException {
        DatabaseMetaData meta = conn.getMetaData();
        String catalog = conn.getCatalog();

        for (String name : candidates) {
            try (ResultSet rs = meta.getTables(catalog, null, name, new String[]{"TABLE"})) {
                if (rs.next()) {
                    return name;
                }
            }
        }

        // Fallback: scan all table names with case-insensitive match.
        try (ResultSet rs = meta.getTables(catalog, null, "%", new String[]{"TABLE"})) {
            while (rs.next()) {
                String existing = rs.getString("TABLE_NAME");
                for (String c : candidates) {
                    if (existing != null && existing.equalsIgnoreCase(c)) {
                        return existing;
                    }
                }
            }
        }
        return null;
    }
}
