DROP DATABASE IF EXISTS GrabTutor;
CREATE DATABASE GrabTutor;
USE GrabTutor;

CREATE TABLE Users (
user_id INT PRIMARY KEY AUTO_INCREMENT,
username VARCHAR(50) UNIQUE NOT NULL,
password VARCHAR(50) NOT NULL,
full_name VARCHAR(100) NOT NULL,
phone VARCHAR(15),
role ENUM('STUDENT', 'TUTOR') NOT NULL
);

CREATE TABLE Subjects (
subject_id INT PRIMARY KEY AUTO_INCREMENT,
subject_name VARCHAR(50) UNIQUE NOT NULL
);

CREATE TABLE Tutors (
tutor_id INT PRIMARY KEY,
price_per_hour DECIMAL(10, 2) NOT NULL,
experience TEXT,
status ENUM('AVAILABLE', 'BUSY') DEFAULT 'AVAILABLE',
FOREIGN KEY (tutor_id) REFERENCES Users(user_id) ON DELETE CASCADE
);

CREATE TABLE Tutor_Subjects (
tutor_id INT,
subject_id INT,
PRIMARY KEY (tutor_id, subject_id),
FOREIGN KEY (tutor_id) REFERENCES Tutors(tutor_id) ON DELETE CASCADE,
FOREIGN KEY (subject_id) REFERENCES Subjects(subject_id) ON DELETE CASCADE
);

CREATE TABLE Bookings (
booking_id INT PRIMARY KEY AUTO_INCREMENT,
student_id INT NOT NULL,
tutor_id INT NOT NULL,
booking_date DATETIME DEFAULT CURRENT_TIMESTAMP,
status ENUM('PENDING', 'ACCEPTED', 'IN_PROGRESS', 'COMPLETED', 'REJECTED') DEFAULT 'PENDING',
FOREIGN KEY (student_id) REFERENCES Users(user_id) ON DELETE CASCADE,
FOREIGN KEY (tutor_id) REFERENCES Tutors(tutor_id) ON DELETE CASCADE
);

INSERT INTO Subjects (subject_name) VALUES ('Toán'), ('Vật Lý'), ('Hóa Học'), ('Tiếng Anh'), ('Tiếng Việt'), ('Sinh học');

ALTER TABLE Bookings ADD COLUMN notes TEXT;
CREATE TABLE Provinces (
province_id INT PRIMARY KEY AUTO_INCREMENT,
province_name VARCHAR(100) UNIQUE NOT NULL
);

ALTER TABLE Tutors ADD COLUMN province_id INT;
ALTER TABLE Tutors ADD FOREIGN KEY (province_id) REFERENCES Provinces(province_id);

INSERT INTO Provinces (province_name) VALUES ('Đà Nẵng'), ('Hà Nội'), ('TP Hồ Chí Minh'), ('Quảng Nam'), ('Huế');
ALTER TABLE Tutors ADD COLUMN capacity INT DEFAULT 1;

CREATE TABLE IF NOT EXISTS grades (
id INT PRIMARY KEY AUTO_INCREMENT,
name VARCHAR(50) UNIQUE NOT NULL
);

INSERT INTO grades (name) VALUES
('Lớp 1'),('Lớp 2'),('Lớp 3'),('Lớp 4'),('Lớp 5'),('Lớp 6'),
('Lớp 7'),('Lớp 8'),('Lớp 9'),('Lớp 10'),('Lớp 11'),('Lớp 12')
ON DUPLICATE KEY UPDATE name = name;

CREATE TABLE IF NOT EXISTS tutor_grades (
tutor_id INT NOT NULL,
grade_id INT NOT NULL,
PRIMARY KEY (tutor_id, grade_id),
CONSTRAINT fk_tutor_grades_tutor FOREIGN KEY (tutor_id) REFERENCES Tutors(tutor_id) ON DELETE CASCADE,
CONSTRAINT fk_tutor_grades_grade FOREIGN KEY (grade_id) REFERENCES grades(id) ON DELETE CASCADE
);

ALTER TABLE Users MODIFY COLUMN role ENUM('STUDENT', 'TUTOR', 'ADMIN') NOT NULL;
ALTER TABLE Tutors ADD COLUMN is_approved BOOLEAN DEFAULT FALSE;
INSERT INTO Users (username, password, full_name, role)
VALUES ('admin', 'admin123', 'Hệ thống Admin', 'ADMIN');
