-- =============================================================
-- Database schema: EverCare Online Clinic Management System
-- Source model: Flask SQLAlchemy models, customized for Java Spring MVC/Spring Security app
-- DBMS: MySQL 8+
-- Charset: utf8mb4
-- =============================================================

DROP DATABASE IF EXISTS evercaredb;
CREATE DATABASE evercaredb
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE evercaredb;

-- =============================================================
-- 1. AUTHENTICATION / AUTHORIZATION
-- =============================================================

CREATE TABLE role (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(255),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    active BOOLEAN DEFAULT TRUE
) ENGINE=InnoDB;

CREATE TABLE user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) UNIQUE,
    password VARCHAR(255) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    phone VARCHAR(20),
    avatar_url VARCHAR(255),
    enabled BOOLEAN DEFAULT TRUE,
    account_non_locked BOOLEAN DEFAULT TRUE,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    active BOOLEAN DEFAULT TRUE,
    CONSTRAINT chk_user_email CHECK (email IS NULL OR email LIKE '%@%')
) ENGINE=InnoDB;

CREATE TABLE user_role (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_user_role_user
        FOREIGN KEY (user_id) REFERENCES user(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_user_role_role
        FOREIGN KEY (role_id) REFERENCES role(id)
        ON DELETE RESTRICT
) ENGINE=InnoDB;

-- =============================================================
-- 2. MASTER DATA
-- =============================================================

CREATE TABLE department (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(30) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    active BOOLEAN DEFAULT TRUE
) ENGINE=InnoDB;

CREATE TABLE medical_service (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    department_id BIGINT,
    code VARCHAR(30) NOT NULL UNIQUE,
    name VARCHAR(150) NOT NULL,
    description TEXT,
    price DECIMAL(15,2) DEFAULT 0,
    service_type VARCHAR(50) DEFAULT 'EXAMINATION',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    active BOOLEAN DEFAULT TRUE,
    CONSTRAINT chk_medical_service_price CHECK (price >= 0),
    CONSTRAINT chk_medical_service_type CHECK (service_type IN ('EXAMINATION','TEST','IMAGING','PROCEDURE','CONSULTATION','OTHER')),
    CONSTRAINT fk_medical_service_department
        FOREIGN KEY (department_id) REFERENCES department(id)
        ON DELETE SET NULL
) ENGINE=InnoDB;

-- =============================================================
-- 3. HUMAN RESOURCES / PATIENT / DOCTOR
-- =============================================================

CREATE TABLE employee (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT UNIQUE,
    employee_code VARCHAR(30) NOT NULL UNIQUE,
    full_name VARCHAR(100) NOT NULL,
    gender VARCHAR(20),
    date_of_birth DATE,
    phone VARCHAR(20) NOT NULL,
    email VARCHAR(100),
    address VARCHAR(255),
    position VARCHAR(80) NOT NULL,
    salary DECIMAL(15,2),
    hired_date DATE DEFAULT (CURRENT_DATE),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    active BOOLEAN DEFAULT TRUE,
    CONSTRAINT chk_employee_gender CHECK (gender IS NULL OR gender IN ('MALE','FEMALE','OTHER')),
    CONSTRAINT chk_employee_salary CHECK (salary IS NULL OR salary >= 0),
    CONSTRAINT fk_employee_user
        FOREIGN KEY (user_id) REFERENCES user(id)
        ON DELETE SET NULL
) ENGINE=InnoDB;

CREATE TABLE patient (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT UNIQUE,
    patient_code VARCHAR(30) NOT NULL UNIQUE,
    full_name VARCHAR(100) NOT NULL,
    gender VARCHAR(20),
    date_of_birth DATE,
    phone VARCHAR(20) NOT NULL,
    email VARCHAR(100),
    citizen_id VARCHAR(20),
    health_insurance_no VARCHAR(50),
    address VARCHAR(255),
    emergency_contact_name VARCHAR(100),
    emergency_contact_phone VARCHAR(20),
    blood_type VARCHAR(5),
    allergy_note TEXT,
    medical_history_note TEXT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    active BOOLEAN DEFAULT TRUE,
    CONSTRAINT chk_patient_gender CHECK (gender IS NULL OR gender IN ('MALE','FEMALE','OTHER')),
    CONSTRAINT chk_patient_blood_type CHECK (blood_type IS NULL OR blood_type IN ('A+','A-','B+','B-','AB+','AB-','O+','O-')),
    CONSTRAINT fk_patient_user
        FOREIGN KEY (user_id) REFERENCES user(id)
        ON DELETE SET NULL
) ENGINE=InnoDB;

CREATE TABLE doctor (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT UNIQUE,
    department_id BIGINT,
    doctor_code VARCHAR(30) NOT NULL UNIQUE,
    full_name VARCHAR(100) NOT NULL,
    phone VARCHAR(20),
    email VARCHAR(100),
    avatar_url VARCHAR(255),
    qualification VARCHAR(150),
    specialization VARCHAR(150),
    doctor_type VARCHAR(30) DEFAULT 'FULL_TIME',
    work_status VARCHAR(30) DEFAULT 'AVAILABLE',
    base_salary DECIMAL(15,2),
    hourly_rate DECIMAL(15,2),
    bio TEXT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    active BOOLEAN DEFAULT TRUE,
    CONSTRAINT chk_doctor_type CHECK (doctor_type IN ('FULL_TIME','PART_TIME')),
    CONSTRAINT chk_doctor_status CHECK (work_status IN ('AVAILABLE','BUSY','OFF')),
    CONSTRAINT chk_doctor_salary CHECK (base_salary IS NULL OR base_salary >= 0),
    CONSTRAINT chk_doctor_hourly_rate CHECK (hourly_rate IS NULL OR hourly_rate >= 0),
    CONSTRAINT fk_doctor_user
        FOREIGN KEY (user_id) REFERENCES user(id)
        ON DELETE SET NULL,
    CONSTRAINT fk_doctor_department
        FOREIGN KEY (department_id) REFERENCES department(id)
        ON DELETE SET NULL
) ENGINE=InnoDB;

CREATE TABLE doctor_schedule (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    doctor_id BIGINT NOT NULL,
    work_date DATE NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    max_patients INT DEFAULT 20,
    status VARCHAR(30) DEFAULT 'AVAILABLE',
    note VARCHAR(255),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    active BOOLEAN DEFAULT TRUE,
    CONSTRAINT chk_doctor_schedule_time CHECK (start_time < end_time),
    CONSTRAINT chk_doctor_schedule_status CHECK (status IN ('AVAILABLE','BUSY','OFF')),
    CONSTRAINT chk_doctor_schedule_max CHECK (max_patients > 0),
    CONSTRAINT uq_doctor_schedule UNIQUE (doctor_id, work_date, start_time, end_time),
    CONSTRAINT fk_doctor_schedule_doctor
        FOREIGN KEY (doctor_id) REFERENCES doctor(id)
        ON DELETE CASCADE
) ENGINE=InnoDB;

-- =============================================================
-- 4. APPOINTMENT / ONLINE CONSULTATION
-- =============================================================

CREATE TABLE appointment (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    appointment_code VARCHAR(30) NOT NULL UNIQUE,
    patient_id BIGINT NOT NULL,
    doctor_id BIGINT NOT NULL,
    service_id BIGINT,
    appointment_date DATE NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME,
    status VARCHAR(40) DEFAULT 'BOOKED',
    reason VARCHAR(255),
    symptom_note TEXT,
    cancel_reason VARCHAR(255),
    created_by BIGINT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    active BOOLEAN DEFAULT TRUE,
    CONSTRAINT chk_appointment_status CHECK (status IN ('BOOKED','WAITING','IN_PROGRESS','COMPLETED','CANCELLED','NO_SHOW')),
    CONSTRAINT chk_appointment_time CHECK (end_time IS NULL OR start_time < end_time),
    CONSTRAINT uq_doctor_appointment_slot UNIQUE (doctor_id, appointment_date, start_time),
    CONSTRAINT fk_appointment_patient
        FOREIGN KEY (patient_id) REFERENCES patient(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_appointment_doctor
        FOREIGN KEY (doctor_id) REFERENCES doctor(id)
        ON DELETE RESTRICT,
    CONSTRAINT fk_appointment_service
        FOREIGN KEY (service_id) REFERENCES medical_service(id)
        ON DELETE SET NULL,
    CONSTRAINT fk_appointment_created_by
        FOREIGN KEY (created_by) REFERENCES user(id)
        ON DELETE SET NULL
) ENGINE=InnoDB;

CREATE TABLE online_consultation (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    appointment_id BIGINT NOT NULL UNIQUE,
    consultation_type VARCHAR(20) DEFAULT 'CHAT',
    room_url VARCHAR(255),
    started_at DATETIME,
    ended_at DATETIME,
    summary TEXT,
    status VARCHAR(30) DEFAULT 'SCHEDULED',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    active BOOLEAN DEFAULT TRUE,
    CONSTRAINT chk_online_consultation_type CHECK (consultation_type IN ('CHAT','VIDEO_CALL')),
    CONSTRAINT chk_online_consultation_status CHECK (status IN ('SCHEDULED','ONGOING','ENDED','CANCELLED')),
    CONSTRAINT fk_online_consultation_appointment
        FOREIGN KEY (appointment_id) REFERENCES appointment(id)
        ON DELETE CASCADE
) ENGINE=InnoDB;

-- =============================================================
-- 5. ELECTRONIC MEDICAL RECORD / TEST RESULT / PRESCRIPTION
-- =============================================================

CREATE TABLE medical_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    record_code VARCHAR(30) NOT NULL UNIQUE,
    appointment_id BIGINT UNIQUE,
    patient_id BIGINT NOT NULL,
    doctor_id BIGINT NOT NULL,
    visit_date DATETIME DEFAULT CURRENT_TIMESTAMP,
    chief_complaint TEXT,
    diagnosis TEXT,
    treatment_plan TEXT,
    doctor_note TEXT,
    payment_status VARCHAR(30) DEFAULT 'UNPAID',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    active BOOLEAN DEFAULT TRUE,
    CONSTRAINT chk_medical_record_payment_status CHECK (payment_status IN ('UNPAID','PAID','REFUNDED','PARTIALLY_PAID')),
    CONSTRAINT fk_medical_record_appointment
        FOREIGN KEY (appointment_id) REFERENCES appointment(id)
        ON DELETE SET NULL,
    CONSTRAINT fk_medical_record_patient
        FOREIGN KEY (patient_id) REFERENCES patient(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_medical_record_doctor
        FOREIGN KEY (doctor_id) REFERENCES doctor(id)
        ON DELETE RESTRICT
) ENGINE=InnoDB;

CREATE TABLE medical_record_service (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    medical_record_id BIGINT NOT NULL,
    service_id BIGINT NOT NULL,
    quantity INT DEFAULT 1,
    unit_price DECIMAL(15,2) DEFAULT 0,
    result_summary TEXT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    active BOOLEAN DEFAULT TRUE,
    CONSTRAINT chk_mrs_quantity CHECK (quantity > 0),
    CONSTRAINT chk_mrs_unit_price CHECK (unit_price >= 0),
    CONSTRAINT fk_mrs_medical_record
        FOREIGN KEY (medical_record_id) REFERENCES medical_record(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_mrs_service
        FOREIGN KEY (service_id) REFERENCES medical_service(id)
        ON DELETE RESTRICT
) ENGINE=InnoDB;

CREATE TABLE test_result (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    medical_record_id BIGINT NOT NULL,
    service_id BIGINT,
    result_code VARCHAR(30) NOT NULL UNIQUE,
    result_title VARCHAR(150) NOT NULL,
    result_content TEXT,
    file_url VARCHAR(255),
    conclusion TEXT,
    performed_by BIGINT,
    result_date DATETIME DEFAULT CURRENT_TIMESTAMP,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    active BOOLEAN DEFAULT TRUE,
    CONSTRAINT fk_test_result_medical_record
        FOREIGN KEY (medical_record_id) REFERENCES medical_record(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_test_result_service
        FOREIGN KEY (service_id) REFERENCES medical_service(id)
        ON DELETE SET NULL,
    CONSTRAINT fk_test_result_performed_by
        FOREIGN KEY (performed_by) REFERENCES employee(id)
        ON DELETE SET NULL
) ENGINE=InnoDB;

CREATE TABLE medicine (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    medicine_code VARCHAR(30) NOT NULL UNIQUE,
    name VARCHAR(150) NOT NULL,
    unit VARCHAR(30) DEFAULT 'VIEN',
    description TEXT,
    usage_note TEXT,
    unit_price DECIMAL(15,2) DEFAULT 0,
    min_stock_quantity INT DEFAULT 10,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    active BOOLEAN DEFAULT TRUE,
    CONSTRAINT chk_medicine_unit CHECK (unit IN ('VIEN','VI','ONG','CHAI','GOI','HOP','TUYP','LO')),
    CONSTRAINT chk_medicine_price CHECK (unit_price >= 0),
    CONSTRAINT chk_medicine_min_stock CHECK (min_stock_quantity >= 0)
) ENGINE=InnoDB;

CREATE TABLE medicine_batch (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    medicine_id BIGINT NOT NULL,
    batch_code VARCHAR(50) NOT NULL,
    import_date DATE DEFAULT (CURRENT_DATE),
    expiry_date DATE NOT NULL,
    quantity INT DEFAULT 0,
    remaining_quantity INT DEFAULT 0,
    import_price DECIMAL(15,2) DEFAULT 0,
    supplier_name VARCHAR(150),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    active BOOLEAN DEFAULT TRUE,
    CONSTRAINT uq_medicine_batch UNIQUE (medicine_id, batch_code),
    CONSTRAINT chk_medicine_batch_quantity CHECK (quantity >= 0 AND remaining_quantity >= 0 AND remaining_quantity <= quantity),
    CONSTRAINT chk_medicine_batch_expiry CHECK (expiry_date > import_date),
    CONSTRAINT fk_medicine_batch_medicine
        FOREIGN KEY (medicine_id) REFERENCES medicine(id)
        ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE prescription (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    prescription_code VARCHAR(30) NOT NULL UNIQUE,
    medical_record_id BIGINT NOT NULL UNIQUE,
    doctor_id BIGINT NOT NULL,
    patient_id BIGINT NOT NULL,
    prescribed_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(30) DEFAULT 'PRESCRIBED',
    note TEXT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    active BOOLEAN DEFAULT TRUE,
    CONSTRAINT chk_prescription_status CHECK (status IN ('PRESCRIBED','DISPENSED','CANCELLED')),
    CONSTRAINT fk_prescription_medical_record
        FOREIGN KEY (medical_record_id) REFERENCES medical_record(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_prescription_doctor
        FOREIGN KEY (doctor_id) REFERENCES doctor(id)
        ON DELETE RESTRICT,
    CONSTRAINT fk_prescription_patient
        FOREIGN KEY (patient_id) REFERENCES patient(id)
        ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE prescription_item (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    prescription_id BIGINT NOT NULL,
    medicine_id BIGINT NOT NULL,
    quantity INT DEFAULT 1,
    unit_price DECIMAL(15,2) DEFAULT 0,
    dosage VARCHAR(100),
    frequency VARCHAR(100),
    duration VARCHAR(100),
    instruction VARCHAR(255),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    active BOOLEAN DEFAULT TRUE,
    CONSTRAINT chk_prescription_item_quantity CHECK (quantity > 0),
    CONSTRAINT chk_prescription_item_price CHECK (unit_price >= 0),
    CONSTRAINT fk_prescription_item_prescription
        FOREIGN KEY (prescription_id) REFERENCES prescription(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_prescription_item_medicine
        FOREIGN KEY (medicine_id) REFERENCES medicine(id)
        ON DELETE RESTRICT
) ENGINE=InnoDB;

CREATE TABLE inventory_transaction (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    medicine_id BIGINT NOT NULL,
    batch_id BIGINT,
    prescription_item_id BIGINT,
    transaction_type VARCHAR(30) NOT NULL,
    quantity INT NOT NULL,
    transaction_date DATETIME DEFAULT CURRENT_TIMESTAMP,
    note VARCHAR(255),
    created_by BIGINT,
    CONSTRAINT chk_inventory_transaction_type CHECK (transaction_type IN ('IMPORT','EXPORT','ADJUSTMENT','PRESCRIPTION_EXPORT')),
    CONSTRAINT chk_inventory_transaction_quantity CHECK (quantity <> 0),
    CONSTRAINT fk_inventory_transaction_medicine
        FOREIGN KEY (medicine_id) REFERENCES medicine(id)
        ON DELETE RESTRICT,
    CONSTRAINT fk_inventory_transaction_batch
        FOREIGN KEY (batch_id) REFERENCES medicine_batch(id)
        ON DELETE SET NULL,
    CONSTRAINT fk_inventory_transaction_prescription_item
        FOREIGN KEY (prescription_item_id) REFERENCES prescription_item(id)
        ON DELETE SET NULL,
    CONSTRAINT fk_inventory_transaction_created_by
        FOREIGN KEY (created_by) REFERENCES user(id)
        ON DELETE SET NULL
) ENGINE=InnoDB;

-- =============================================================
-- 6. INVOICE / PAYMENT / NOTIFICATION
-- =============================================================

CREATE TABLE invoice (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    invoice_code VARCHAR(30) NOT NULL UNIQUE,
    medical_record_id BIGINT NOT NULL UNIQUE,
    patient_id BIGINT NOT NULL,
    cashier_id BIGINT,
    total_service_amount DECIMAL(15,2) DEFAULT 0,
    total_medicine_amount DECIMAL(15,2) DEFAULT 0,
    discount_amount DECIMAL(15,2) DEFAULT 0,
    total_amount DECIMAL(15,2) DEFAULT 0,
    payment_method VARCHAR(40),
    payment_status VARCHAR(30) DEFAULT 'UNPAID',
    paid_at DATETIME,
    note VARCHAR(255),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    active BOOLEAN DEFAULT TRUE,
    CONSTRAINT chk_invoice_amount CHECK (total_service_amount >= 0 AND total_medicine_amount >= 0 AND discount_amount >= 0 AND total_amount >= 0),
    CONSTRAINT chk_invoice_payment_method CHECK (payment_method IS NULL OR payment_method IN ('CASH','BANK_TRANSFER','VIETQR','MOMO','VNPAY','PAYPAL')),
    CONSTRAINT chk_invoice_payment_status CHECK (payment_status IN ('UNPAID','PAID','REFUNDED','PARTIALLY_PAID')),
    CONSTRAINT fk_invoice_medical_record
        FOREIGN KEY (medical_record_id) REFERENCES medical_record(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_invoice_patient
        FOREIGN KEY (patient_id) REFERENCES patient(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_invoice_cashier
        FOREIGN KEY (cashier_id) REFERENCES user(id)
        ON DELETE SET NULL
) ENGINE=InnoDB;

CREATE TABLE payment (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    invoice_id BIGINT NOT NULL,
    amount DECIMAL(15,2) NOT NULL,
    payment_method VARCHAR(40) DEFAULT 'CASH',
    transaction_code VARCHAR(100),
    payment_provider VARCHAR(80),
    payment_status VARCHAR(30) DEFAULT 'PENDING',
    paid_at DATETIME,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    active BOOLEAN DEFAULT TRUE,
    CONSTRAINT chk_payment_amount CHECK (amount > 0),
    CONSTRAINT chk_payment_method CHECK (payment_method IN ('CASH','BANK_TRANSFER','VIETQR','MOMO','VNPAY','PAYPAL')),
    CONSTRAINT chk_payment_status CHECK (payment_status IN ('PENDING','SUCCESS','FAILED','REFUNDED')),
    CONSTRAINT fk_payment_invoice
        FOREIGN KEY (invoice_id) REFERENCES invoice(id)
        ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE notification (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    title VARCHAR(150) NOT NULL,
    content TEXT NOT NULL,
    notification_type VARCHAR(50) DEFAULT 'GENERAL',
    related_id BIGINT,
    read_at DATETIME,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    active BOOLEAN DEFAULT TRUE,
    CONSTRAINT chk_notification_type CHECK (notification_type IN ('GENERAL','APPOINTMENT_REMINDER','PRESCRIPTION','PAYMENT','TEST_RESULT')),
    CONSTRAINT fk_notification_user
        FOREIGN KEY (user_id) REFERENCES user(id)
        ON DELETE CASCADE
) ENGINE=InnoDB;

-- =============================================================
-- 7. REPORTING SUPPORT VIEWS
-- =============================================================




-- =============================================================
-- 8. INDEXES
-- =============================================================

CREATE INDEX idx_user_username ON user(username);
CREATE INDEX idx_user_email ON user(email);
CREATE INDEX idx_patient_phone ON patient(phone);
CREATE INDEX idx_patient_citizen_id ON patient(citizen_id);
CREATE INDEX idx_doctor_department ON doctor(department_id);
CREATE INDEX idx_appointment_date ON appointment(appointment_date);
CREATE INDEX idx_appointment_patient ON appointment(patient_id);
CREATE INDEX idx_appointment_doctor ON appointment(doctor_id);
CREATE INDEX idx_medical_record_patient ON medical_record(patient_id);
CREATE INDEX idx_medical_record_doctor ON medical_record(doctor_id);
CREATE INDEX idx_medicine_batch_expiry ON medicine_batch(expiry_date);
CREATE INDEX idx_medicine_batch_remaining ON medicine_batch(remaining_quantity);
CREATE INDEX idx_invoice_status ON invoice(payment_status);
CREATE INDEX idx_notification_user_read ON notification(user_id, read_at);


-- =============================================================
-- 8.1. DEFAULT CODE GENERATION TRIGGERS
-- These triggers generate business codes only when the app does
-- not provide a code. They avoid unsafe static DEFAULT values on
-- UNIQUE columns such as department.code or appointment_code.
-- =============================================================

DELIMITER //

CREATE TRIGGER trg_role_code_bi
BEFORE INSERT ON role
FOR EACH ROW
BEGIN
    IF NEW.code IS NULL OR TRIM(NEW.code) = '' THEN
        SET NEW.code = CONCAT('ROLE_', DATE_FORMAT(NOW(6), '%y%m%d%H%i%s'), '_', SUBSTRING(REPLACE(UUID(), '-', ''), 1, 6));
    END IF;
END//

CREATE TRIGGER trg_department_code_bi
BEFORE INSERT ON department
FOR EACH ROW
BEGIN
    IF NEW.code IS NULL OR TRIM(NEW.code) = '' THEN
        SET NEW.code = CONCAT('DEP_', DATE_FORMAT(NOW(6), '%y%m%d%H%i%s'), '_', SUBSTRING(REPLACE(UUID(), '-', ''), 1, 6));
    END IF;
END//

CREATE TRIGGER trg_medical_service_code_bi
BEFORE INSERT ON medical_service
FOR EACH ROW
BEGIN
    IF NEW.code IS NULL OR TRIM(NEW.code) = '' THEN
        SET NEW.code = CONCAT('SVC_', DATE_FORMAT(NOW(6), '%y%m%d%H%i%s'), '_', SUBSTRING(REPLACE(UUID(), '-', ''), 1, 6));
    END IF;
END//

CREATE TRIGGER trg_employee_code_bi
BEFORE INSERT ON employee
FOR EACH ROW
BEGIN
    IF NEW.employee_code IS NULL OR TRIM(NEW.employee_code) = '' THEN
        SET NEW.employee_code = CONCAT('EMP_', DATE_FORMAT(NOW(6), '%y%m%d%H%i%s'), '_', SUBSTRING(REPLACE(UUID(), '-', ''), 1, 6));
    END IF;
END//

CREATE TRIGGER trg_patient_code_bi
BEFORE INSERT ON patient
FOR EACH ROW
BEGIN
    IF NEW.patient_code IS NULL OR TRIM(NEW.patient_code) = '' THEN
        SET NEW.patient_code = CONCAT('PAT_', DATE_FORMAT(NOW(6), '%y%m%d%H%i%s'), '_', SUBSTRING(REPLACE(UUID(), '-', ''), 1, 6));
    END IF;
END//

CREATE TRIGGER trg_doctor_code_bi
BEFORE INSERT ON doctor
FOR EACH ROW
BEGIN
    IF NEW.doctor_code IS NULL OR TRIM(NEW.doctor_code) = '' THEN
        SET NEW.doctor_code = CONCAT('DOC_', DATE_FORMAT(NOW(6), '%y%m%d%H%i%s'), '_', SUBSTRING(REPLACE(UUID(), '-', ''), 1, 6));
    END IF;
END//

CREATE TRIGGER trg_appointment_code_bi
BEFORE INSERT ON appointment
FOR EACH ROW
BEGIN
    IF NEW.appointment_code IS NULL OR TRIM(NEW.appointment_code) = '' THEN
        SET NEW.appointment_code = CONCAT('APT_', DATE_FORMAT(NOW(6), '%y%m%d%H%i%s'), '_', SUBSTRING(REPLACE(UUID(), '-', ''), 1, 6));
    END IF;
END//

CREATE TRIGGER trg_medical_record_code_bi
BEFORE INSERT ON medical_record
FOR EACH ROW
BEGIN
    IF NEW.record_code IS NULL OR TRIM(NEW.record_code) = '' THEN
        SET NEW.record_code = CONCAT('MR_', DATE_FORMAT(NOW(6), '%y%m%d%H%i%s'), '_', SUBSTRING(REPLACE(UUID(), '-', ''), 1, 6));
    END IF;
END//

CREATE TRIGGER trg_test_result_code_bi
BEFORE INSERT ON test_result
FOR EACH ROW
BEGIN
    IF NEW.result_code IS NULL OR TRIM(NEW.result_code) = '' THEN
        SET NEW.result_code = CONCAT('TR_', DATE_FORMAT(NOW(6), '%y%m%d%H%i%s'), '_', SUBSTRING(REPLACE(UUID(), '-', ''), 1, 6));
    END IF;
END//

CREATE TRIGGER trg_medicine_code_bi
BEFORE INSERT ON medicine
FOR EACH ROW
BEGIN
    IF NEW.medicine_code IS NULL OR TRIM(NEW.medicine_code) = '' THEN
        SET NEW.medicine_code = CONCAT('MED_', DATE_FORMAT(NOW(6), '%y%m%d%H%i%s'), '_', SUBSTRING(REPLACE(UUID(), '-', ''), 1, 6));
    END IF;
END//

CREATE TRIGGER trg_medicine_batch_code_bi
BEFORE INSERT ON medicine_batch
FOR EACH ROW
BEGIN
    IF NEW.batch_code IS NULL OR TRIM(NEW.batch_code) = '' THEN
        SET NEW.batch_code = CONCAT('BAT_', DATE_FORMAT(NOW(6), '%y%m%d%H%i%s'), '_', SUBSTRING(REPLACE(UUID(), '-', ''), 1, 6));
    END IF;
END//

CREATE TRIGGER trg_prescription_code_bi
BEFORE INSERT ON prescription
FOR EACH ROW
BEGIN
    IF NEW.prescription_code IS NULL OR TRIM(NEW.prescription_code) = '' THEN
        SET NEW.prescription_code = CONCAT('RX_', DATE_FORMAT(NOW(6), '%y%m%d%H%i%s'), '_', SUBSTRING(REPLACE(UUID(), '-', ''), 1, 6));
    END IF;
END//

CREATE TRIGGER trg_invoice_code_bi
BEFORE INSERT ON invoice
FOR EACH ROW
BEGIN
    IF NEW.invoice_code IS NULL OR TRIM(NEW.invoice_code) = '' THEN
        SET NEW.invoice_code = CONCAT('INV_', DATE_FORMAT(NOW(6), '%y%m%d%H%i%s'), '_', SUBSTRING(REPLACE(UUID(), '-', ''), 1, 6));
    END IF;
END//

DELIMITER ;


-- =============================================================
-- 9. SEED DATA
-- =============================================================

INSERT INTO role(code, name, description) VALUES
('ROLE_ADMIN', 'Quản trị viên', 'Quản lý toàn bộ hệ thống'),
('ROLE_PATIENT', 'Bệnh nhân', 'Người dùng đặt lịch, xem bệnh án, thanh toán'),
('ROLE_DOCTOR', 'Bác sĩ', 'Quản lý lịch khám, bệnh án, kê đơn thuốc'),
('ROLE_RECEPTIONIST', 'Lễ tân', 'Tiếp nhận đặt lịch và hỗ trợ bệnh nhân'),
('ROLE_CASHIER', 'Thu ngân', 'Xử lý hóa đơn và thanh toán'),
('ROLE_PHARMACIST', 'Dược sĩ', 'Quản lý thuốc, kho thuốc và cấp phát thuốc'),
('ROLE_MANAGER', 'Quản lý', 'Xem thống kê, báo cáo và vận hành phòng khám');

INSERT INTO department(code, name, description) VALUES
('NOI', 'Khoa Nội', 'Khám và điều trị nội khoa'),
('NHI', 'Khoa Nhi', 'Khám và điều trị cho trẻ em'),
('TMH', 'Tai Mũi Họng', 'Khám tai, mũi, họng'),
('RHM', 'Răng Hàm Mặt', 'Khám và điều trị răng hàm mặt'),
('XN', 'Xét nghiệm', 'Dịch vụ xét nghiệm'),
('CDHA', 'Chẩn đoán hình ảnh', 'Siêu âm, X-quang, hình ảnh y khoa');

INSERT INTO medical_service(department_id, code, name, description, price, service_type) VALUES
((SELECT id FROM department WHERE code='NOI'), 'DV_KHAM_NOI', 'Khám nội tổng quát', 'Dịch vụ khám nội tổng quát', 150000, 'EXAMINATION'),
((SELECT id FROM department WHERE code='NHI'), 'DV_KHAM_NHI', 'Khám nhi', 'Dịch vụ khám nhi', 150000, 'EXAMINATION'),
((SELECT id FROM department WHERE code='TMH'), 'DV_KHAM_TMH', 'Khám tai mũi họng', 'Dịch vụ khám tai mũi họng', 180000, 'EXAMINATION'),
((SELECT id FROM department WHERE code='XN'), 'DV_XN_MAU', 'Xét nghiệm máu', 'Xét nghiệm công thức máu cơ bản', 120000, 'TEST'),
((SELECT id FROM department WHERE code='CDHA'), 'DV_SIEU_AM', 'Siêu âm tổng quát', 'Siêu âm ổ bụng tổng quát', 250000, 'IMAGING');

INSERT INTO evercaredb.user (
    id,
    username,
    email,
    password,
    full_name,
    phone,
    enabled,
    account_non_locked,
    active
)
VALUES (
    1,
    'admin',
    'admin@gmail.com',
    '$2a$10$l4UA2SJnGJU5fRkR/bo4lenImEOHrUjw4e3wS8Ih14TUGuBPeqW9O',
    'Admin',
    '0909009009',
    true,
    true,
    true
)
ON DUPLICATE KEY UPDATE
    email = VALUES(email),
    password = VALUES(password),
    full_name = VALUES(full_name),
    phone = VALUES(phone),
    enabled = VALUES(enabled),
    account_non_locked = VALUES(account_non_locked),
    active = VALUES(active);

INSERT IGNORE INTO evercaredb.user_role (user_id, role_id)
VALUES (1, 1);