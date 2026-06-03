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
-- 4. APPOINTMENT / SUPPORT CONVERSATION / ONLINE CONSULTATION SCHEDULE
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
    CONSTRAINT unique_appointment UNIQUE(start_time, end_time, patient_id, doctor_id),
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

CREATE TABLE support_conversation (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    patient_id BIGINT NOT NULL,
    staff_id BIGINT NULL,
    status VARCHAR(30) DEFAULT 'OPEN',
    subject VARCHAR(255),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    closed_at DATETIME NULL,
    active BOOLEAN DEFAULT TRUE,
    CONSTRAINT chk_support_conversation_status CHECK (status IN ('OPEN','ASSIGNED','IN_PROGRESS','CLOSED')),
    CONSTRAINT fk_support_conversation_patient
        FOREIGN KEY (patient_id) REFERENCES patient(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_support_conversation_staff
        FOREIGN KEY (staff_id) REFERENCES employee(id)
        ON DELETE SET NULL
) ENGINE=InnoDB;


CREATE TABLE support_message (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    conversation_id BIGINT NOT NULL,
    sender_id BIGINT NOT NULL,
    content TEXT NOT NULL,
    message_type VARCHAR(30) DEFAULT 'TEXT',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    is_read BOOLEAN DEFAULT FALSE,
    active BOOLEAN DEFAULT TRUE,
    CONSTRAINT chk_support_message_type CHECK (message_type IN ('TEXT','MEET_SCHEDULE')),
    CONSTRAINT fk_support_message_conversation
        FOREIGN KEY (conversation_id) REFERENCES support_conversation(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_support_message_sender
        FOREIGN KEY (sender_id) REFERENCES user(id)
        ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE online_consultation_schedule (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    conversation_id BIGINT NULL,
    patient_id BIGINT NOT NULL,
    doctor_id BIGINT NOT NULL,
    staff_id BIGINT NULL,
    scheduled_start DATETIME NOT NULL,
    scheduled_end DATETIME NOT NULL,
    meet_link VARCHAR(255),
    status VARCHAR(30) DEFAULT 'SCHEDULED',
    note TEXT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    active BOOLEAN DEFAULT TRUE,
    CONSTRAINT chk_online_consultation_schedule_time CHECK (scheduled_start < scheduled_end),
    CONSTRAINT chk_online_consultation_schedule_status CHECK (status IN ('SCHEDULED','COMPLETED','CANCELLED')),
    CONSTRAINT uq_online_consultation_doctor_slot UNIQUE (doctor_id, scheduled_start),
    CONSTRAINT fk_online_consultation_schedule_conversation
        FOREIGN KEY (conversation_id) REFERENCES support_conversation(id)
        ON DELETE SET NULL,
    CONSTRAINT fk_online_consultation_schedule_patient
        FOREIGN KEY (patient_id) REFERENCES patient(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_online_consultation_schedule_doctor
        FOREIGN KEY (doctor_id) REFERENCES doctor(id)
        ON DELETE RESTRICT,
    CONSTRAINT fk_online_consultation_schedule_staff
        FOREIGN KEY (staff_id) REFERENCES employee(id)
        ON DELETE SET NULL
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
    CONSTRAINT chk_medical_record_payment_status CHECK (payment_status IN ('UNPAID','PAID','REFUNDED')),
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
    CONSTRAINT chk_invoice_payment_status CHECK (payment_status IN ('UNPAID','PAID','REFUNDED')),
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
('ROLE_LAB_TECH', 'Kỹ thuật viên xét nghiệm', 'Phụ trách tiếp nhận mẫu, thực hiện và cập nhật kết quả xét nghiệm'),
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

-- =============================================================
-- 10. CONSISTENT MOCK DATA - GENERATED FOR DEVELOPMENT / DEMO
-- Quy uoc ma du lieu:
--   Patient code: PAT-2026-0001 ... PAT-2026-0250
--   Appointment code: APT-YYYYMMDD-001 ...
--   Medical record: MR-2026-0001 ...
--   Invoice: INV-2026-0001 ...
--   10 hoa don da thanh toan nhung don thuoc van PRESCRIBED, chua DISPENSED.
--   Da bo bang online_consultation cu; them support_conversation, support_message va online_consultation_schedule.
-- =============================================================

SET FOREIGN_KEY_CHECKS = 0;
SET SQL_SAFE_UPDATES = 0;

DELETE FROM notification;
DELETE FROM payment;
DELETE FROM invoice;
DELETE FROM inventory_transaction;
DELETE FROM prescription_item;
DELETE FROM prescription;
DELETE FROM test_result;
DELETE FROM medical_record_service;
DELETE FROM medical_record;
DELETE FROM online_consultation_schedule;
DELETE FROM support_message;
DELETE FROM support_conversation;
DELETE FROM appointment;
DELETE FROM doctor_schedule;
DELETE FROM medicine_batch;
DELETE FROM medicine;
DELETE FROM doctor;
DELETE FROM patient;
DELETE FROM employee;
DELETE FROM user_role;
DELETE FROM user WHERE id <> 1;
SET FOREIGN_KEY_CHECKS = 1;

-- Bo sung phong ban va dich vu mau
INSERT IGNORE INTO department(code, name, description) VALUES
('DA_LIEU', 'Khoa Da lieu', 'Kham va dieu tri benh ly da lieu'),
('SAN', 'San phu khoa', 'Kham va tu van san phu khoa'),
('MAT', 'Khoa Mat', 'Kham va dieu tri benh ly mat'),
('DUOC', 'Nha thuoc', 'Quan ly va cap phat thuoc');

INSERT IGNORE INTO medical_service(department_id, code, name, description, price, service_type) VALUES
((SELECT id FROM department WHERE code='NOI'), 'SVC-NOI-001', 'Kham noi tong quat', 'Kham va tu van suc khoe noi khoa', 150000, 'EXAMINATION'),
((SELECT id FROM department WHERE code='NHI'), 'SVC-NHI-001', 'Kham nhi', 'Kham va tu van suc khoe tre em', 150000, 'EXAMINATION'),
((SELECT id FROM department WHERE code='TMH'), 'SVC-TMH-001', 'Kham tai mui hong', 'Kham tai mui hong co ban', 180000, 'EXAMINATION'),
((SELECT id FROM department WHERE code='RHM'), 'SVC-RHM-001', 'Kham rang ham mat', 'Kham rang ham mat co ban', 200000, 'EXAMINATION'),
((SELECT id FROM department WHERE code='XN'), 'SVC-XN-001', 'Xet nghiem mau', 'Cong thuc mau co ban', 120000, 'TEST'),
((SELECT id FROM department WHERE code='XN'), 'SVC-XN-002', 'Xet nghiem duong huyet', 'Kiem tra duong huyet', 90000, 'TEST'),
((SELECT id FROM department WHERE code='CDHA'), 'SVC-CDHA-001', 'Sieu am tong quat', 'Sieu am o bung tong quat', 250000, 'IMAGING'),
((SELECT id FROM department WHERE code='CDHA'), 'SVC-CDHA-002', 'X quang nguc', 'Chup X quang nguc thang', 220000, 'IMAGING'),
((SELECT id FROM department WHERE code='DA_LIEU'), 'SVC-DL-001', 'Kham da lieu', 'Kham va tu van benh ly da', 170000, 'EXAMINATION'),
((SELECT id FROM department WHERE code='SAN'), 'SVC-SAN-001', 'Kham san phu khoa', 'Kham va tu van san phu khoa', 220000, 'EXAMINATION'),
((SELECT id FROM department WHERE code='MAT'), 'SVC-MAT-001', 'Kham mat', 'Kham va do thi luc co ban', 160000, 'EXAMINATION');

INSERT INTO user (id, username, email, password, full_name, phone, avatar_url, enabled, account_non_locked, active) VALUES
(101, 'doctor01', 'doctor01@evercare.test', '$2a$10$l4UA2SJnGJU5fRkR/bo4lenImEOHrUjw4e3wS8Ih14TUGuBPeqW9O', 'Bac Si 01', '0901000001', NULL, TRUE, TRUE, TRUE),
(102, 'doctor02', 'doctor02@evercare.test', '$2a$10$l4UA2SJnGJU5fRkR/bo4lenImEOHrUjw4e3wS8Ih14TUGuBPeqW9O', 'Bac Si 02', '0901000002', NULL, TRUE, TRUE, TRUE),
(103, 'doctor03', 'doctor03@evercare.test', '$2a$10$l4UA2SJnGJU5fRkR/bo4lenImEOHrUjw4e3wS8Ih14TUGuBPeqW9O', 'Bac Si 03', '0901000003', NULL, TRUE, TRUE, TRUE),
(104, 'doctor04', 'doctor04@evercare.test', '$2a$10$l4UA2SJnGJU5fRkR/bo4lenImEOHrUjw4e3wS8Ih14TUGuBPeqW9O', 'Bac Si 04', '0901000004', NULL, TRUE, TRUE, TRUE),
(105, 'doctor05', 'doctor05@evercare.test', '$2a$10$l4UA2SJnGJU5fRkR/bo4lenImEOHrUjw4e3wS8Ih14TUGuBPeqW9O', 'Bac Si 05', '0901000005', NULL, TRUE, TRUE, TRUE),
(106, 'doctor06', 'doctor06@evercare.test', '$2a$10$l4UA2SJnGJU5fRkR/bo4lenImEOHrUjw4e3wS8Ih14TUGuBPeqW9O', 'Bac Si 06', '0901000006', NULL, TRUE, TRUE, TRUE),
(107, 'doctor07', 'doctor07@evercare.test', '$2a$10$l4UA2SJnGJU5fRkR/bo4lenImEOHrUjw4e3wS8Ih14TUGuBPeqW9O', 'Bac Si 07', '0901000007', NULL, TRUE, TRUE, TRUE),
(108, 'doctor08', 'doctor08@evercare.test', '$2a$10$l4UA2SJnGJU5fRkR/bo4lenImEOHrUjw4e3wS8Ih14TUGuBPeqW9O', 'Bac Si 08', '0901000008', NULL, TRUE, TRUE, TRUE),
(109, 'doctor09', 'doctor09@evercare.test', '$2a$10$l4UA2SJnGJU5fRkR/bo4lenImEOHrUjw4e3wS8Ih14TUGuBPeqW9O', 'Bac Si 09', '0901000009', NULL, TRUE, TRUE, TRUE),
(110, 'doctor10', 'doctor10@evercare.test', '$2a$10$l4UA2SJnGJU5fRkR/bo4lenImEOHrUjw4e3wS8Ih14TUGuBPeqW9O', 'Bac Si 10', '0901000010', NULL, TRUE, TRUE, TRUE),
(201, 'receptionist01', 'receptionist01@evercare.test', '$2a$10$l4UA2SJnGJU5fRkR/bo4lenImEOHrUjw4e3wS8Ih14TUGuBPeqW9O', 'Nhan Vien 01', '0902000001', NULL, TRUE, TRUE, TRUE),
(202, 'cashier02', 'cashier02@evercare.test', '$2a$10$l4UA2SJnGJU5fRkR/bo4lenImEOHrUjw4e3wS8Ih14TUGuBPeqW9O', 'Nhan Vien 02', '0902000002', NULL, TRUE, TRUE, TRUE),
(203, 'pharmacist03', 'pharmacist03@evercare.test', '$2a$10$l4UA2SJnGJU5fRkR/bo4lenImEOHrUjw4e3wS8Ih14TUGuBPeqW9O', 'Nhan Vien 03', '0902000003', NULL, TRUE, TRUE, TRUE),
(204, 'labtech04', 'labtech04@evercare.test', '$2a$10$l4UA2SJnGJU5fRkR/bo4lenImEOHrUjw4e3wS8Ih14TUGuBPeqW9O', 'Nhan Vien 04', '0902000004', NULL, TRUE, TRUE, TRUE),
(205, 'manager05', 'manager05@evercare.test', '$2a$10$l4UA2SJnGJU5fRkR/bo4lenImEOHrUjw4e3wS8Ih14TUGuBPeqW9O', 'Nhan Vien 05', '0902000005', NULL, TRUE, TRUE, TRUE),
(206, 'cashier06', 'cashier06@evercare.test', '$2a$10$l4UA2SJnGJU5fRkR/bo4lenImEOHrUjw4e3wS8Ih14TUGuBPeqW9O', 'Nhan Vien 06', '0902000006', NULL, TRUE, TRUE, TRUE),
(207, 'pharmacist07', 'pharmacist07@evercare.test', '$2a$10$l4UA2SJnGJU5fRkR/bo4lenImEOHrUjw4e3wS8Ih14TUGuBPeqW9O', 'Nhan Vien 07', '0902000007', NULL, TRUE, TRUE, TRUE),
(208, 'receptionist08', 'receptionist08@evercare.test', '$2a$10$l4UA2SJnGJU5fRkR/bo4lenImEOHrUjw4e3wS8Ih14TUGuBPeqW9O', 'Nhan Vien 08', '0902000008', NULL, TRUE, TRUE, TRUE),
(209, 'labtech09', 'labtech09@evercare.test', '$2a$10$l4UA2SJnGJU5fRkR/bo4lenImEOHrUjw4e3wS8Ih14TUGuBPeqW9O', 'Nhan Vien 09', '0902000009', NULL, TRUE, TRUE, TRUE),
(210, 'manager10', 'manager10@evercare.test', '$2a$10$l4UA2SJnGJU5fRkR/bo4lenImEOHrUjw4e3wS8Ih14TUGuBPeqW9O', 'Nhan Vien 10', '0902000010', NULL, TRUE, TRUE, TRUE),
(1001, 'patient0001', 'patient0001@evercare.test', '$2a$10$l4UA2SJnGJU5fRkR/bo4lenImEOHrUjw4e3wS8Ih14TUGuBPeqW9O', 'Benh Nhan 0001', '0910000001', NULL, TRUE, TRUE, TRUE),
(1002, 'patient0002', 'patient0002@evercare.test', '$2a$10$l4UA2SJnGJU5fRkR/bo4lenImEOHrUjw4e3wS8Ih14TUGuBPeqW9O', 'Benh Nhan 0002', '0910000002', NULL, TRUE, TRUE, TRUE),
(1003, 'patient0003', 'patient0003@evercare.test', '$2a$10$l4UA2SJnGJU5fRkR/bo4lenImEOHrUjw4e3wS8Ih14TUGuBPeqW9O', 'Benh Nhan 0003', '0910000003', NULL, TRUE, TRUE, TRUE),
(1004, 'patient0004', 'patient0004@evercare.test', '$2a$10$l4UA2SJnGJU5fRkR/bo4lenImEOHrUjw4e3wS8Ih14TUGuBPeqW9O', 'Benh Nhan 0004', '0910000004', NULL, TRUE, TRUE, TRUE),
(1005, 'patient0005', 'patient0005@evercare.test', '$2a$10$l4UA2SJnGJU5fRkR/bo4lenImEOHrUjw4e3wS8Ih14TUGuBPeqW9O', 'Benh Nhan 0005', '0910000005', NULL, TRUE, TRUE, TRUE),
(1006, 'patient0006', 'patient0006@evercare.test', '$2a$10$l4UA2SJnGJU5fRkR/bo4lenImEOHrUjw4e3wS8Ih14TUGuBPeqW9O', 'Benh Nhan 0006', '0910000006', NULL, TRUE, TRUE, TRUE),
(1007, 'patient0007', 'patient0007@evercare.test', '$2a$10$l4UA2SJnGJU5fRkR/bo4lenImEOHrUjw4e3wS8Ih14TUGuBPeqW9O', 'Benh Nhan 0007', '0910000007', NULL, TRUE, TRUE, TRUE),
(1008, 'patient0008', 'patient0008@evercare.test', '$2a$10$l4UA2SJnGJU5fRkR/bo4lenImEOHrUjw4e3wS8Ih14TUGuBPeqW9O', 'Benh Nhan 0008', '0910000008', NULL, TRUE, TRUE, TRUE),
(1009, 'patient0009', 'patient0009@evercare.test', '$2a$10$l4UA2SJnGJU5fRkR/bo4lenImEOHrUjw4e3wS8Ih14TUGuBPeqW9O', 'Benh Nhan 0009', '0910000009', NULL, TRUE, TRUE, TRUE),
(1010, 'patient0010', 'patient0010@evercare.test', '$2a$10$l4UA2SJnGJU5fRkR/bo4lenImEOHrUjw4e3wS8Ih14TUGuBPeqW9O', 'Benh Nhan 0010', '0910000010', NULL, TRUE, TRUE, TRUE),
(1011, 'patient0011', 'patient0011@evercare.test', '$2a$10$l4UA2SJnGJU5fRkR/bo4lenImEOHrUjw4e3wS8Ih14TUGuBPeqW9O', 'Benh Nhan 0011', '0910000011', NULL, TRUE, TRUE, TRUE),
(1012, 'patient0012', 'patient0012@evercare.test', '$2a$10$l4UA2SJnGJU5fRkR/bo4lenImEOHrUjw4e3wS8Ih14TUGuBPeqW9O', 'Benh Nhan 0012', '0910000012', NULL, TRUE, TRUE, TRUE),
(1013, 'patient0013', 'patient0013@evercare.test', '$2a$10$l4UA2SJnGJU5fRkR/bo4lenImEOHrUjw4e3wS8Ih14TUGuBPeqW9O', 'Benh Nhan 0013', '0910000013', NULL, TRUE, TRUE, TRUE),
(1014, 'patient0014', 'patient0014@evercare.test', '$2a$10$l4UA2SJnGJU5fRkR/bo4lenImEOHrUjw4e3wS8Ih14TUGuBPeqW9O', 'Benh Nhan 0014', '0910000014', NULL, TRUE, TRUE, TRUE),
(1015, 'patient0015', 'patient0015@evercare.test', '$2a$10$l4UA2SJnGJU5fRkR/bo4lenImEOHrUjw4e3wS8Ih14TUGuBPeqW9O', 'Benh Nhan 0015', '0910000015', NULL, TRUE, TRUE, TRUE),
(1016, 'patient0016', 'patient0016@evercare.test', '$2a$10$l4UA2SJnGJU5fRkR/bo4lenImEOHrUjw4e3wS8Ih14TUGuBPeqW9O', 'Benh Nhan 0016', '0910000016', NULL, TRUE, TRUE, TRUE),
(1017, 'patient0017', 'patient0017@evercare.test', '$2a$10$l4UA2SJnGJU5fRkR/bo4lenImEOHrUjw4e3wS8Ih14TUGuBPeqW9O', 'Benh Nhan 0017', '0910000017', NULL, TRUE, TRUE, TRUE),
(1018, 'patient0018', 'patient0018@evercare.test', '$2a$10$l4UA2SJnGJU5fRkR/bo4lenImEOHrUjw4e3wS8Ih14TUGuBPeqW9O', 'Benh Nhan 0018', '0910000018', NULL, TRUE, TRUE, TRUE),
(1019, 'patient0019', 'patient0019@evercare.test', '$2a$10$l4UA2SJnGJU5fRkR/bo4lenImEOHrUjw4e3wS8Ih14TUGuBPeqW9O', 'Benh Nhan 0019', '0910000019', NULL, TRUE, TRUE, TRUE),
(1020, 'patient0020', 'patient0020@evercare.test', '$2a$10$l4UA2SJnGJU5fRkR/bo4lenImEOHrUjw4e3wS8Ih14TUGuBPeqW9O', 'Benh Nhan 0020', '0910000020', NULL, TRUE, TRUE, TRUE),
(1021, 'patient0021', 'patient0021@evercare.test', '$2a$10$l4UA2SJnGJU5fRkR/bo4lenImEOHrUjw4e3wS8Ih14TUGuBPeqW9O', 'Benh Nhan 0021', '0910000021', NULL, TRUE, TRUE, TRUE),
(1022, 'patient0022', 'patient0022@evercare.test', '$2a$10$l4UA2SJnGJU5fRkR/bo4lenImEOHrUjw4e3wS8Ih14TUGuBPeqW9O', 'Benh Nhan 0022', '0910000022', NULL, TRUE, TRUE, TRUE),
(1023, 'patient0023', 'patient0023@evercare.test', '$2a$10$l4UA2SJnGJU5fRkR/bo4lenImEOHrUjw4e3wS8Ih14TUGuBPeqW9O', 'Benh Nhan 0023', '0910000023', NULL, TRUE, TRUE, TRUE),
(1024, 'patient0024', 'patient0024@evercare.test', '$2a$10$l4UA2SJnGJU5fRkR/bo4lenImEOHrUjw4e3wS8Ih14TUGuBPeqW9O', 'Benh Nhan 0024', '0910000024', NULL, TRUE, TRUE, TRUE),
(1025, 'patient0025', 'patient0025@evercare.test', '$2a$10$l4UA2SJnGJU5fRkR/bo4lenImEOHrUjw4e3wS8Ih14TUGuBPeqW9O', 'Benh Nhan 0025', '0910000025', NULL, TRUE, TRUE, TRUE),
(1026, 'patient0026', 'patient0026@evercare.test', '$2a$10$l4UA2SJnGJU5fRkR/bo4lenImEOHrUjw4e3wS8Ih14TUGuBPeqW9O', 'Benh Nhan 0026', '0910000026', NULL, TRUE, TRUE, TRUE),
(1027, 'patient0027', 'patient0027@evercare.test', '$2a$10$l4UA2SJnGJU5fRkR/bo4lenImEOHrUjw4e3wS8Ih14TUGuBPeqW9O', 'Benh Nhan 0027', '0910000027', NULL, TRUE, TRUE, TRUE),
(1028, 'patient0028', 'patient0028@evercare.test', '$2a$10$l4UA2SJnGJU5fRkR/bo4lenImEOHrUjw4e3wS8Ih14TUGuBPeqW9O', 'Benh Nhan 0028', '0910000028', NULL, TRUE, TRUE, TRUE),
(1029, 'patient0029', 'patient0029@evercare.test', '$2a$10$l4UA2SJnGJU5fRkR/bo4lenImEOHrUjw4e3wS8Ih14TUGuBPeqW9O', 'Benh Nhan 0029', '0910000029', NULL, TRUE, TRUE, TRUE),
(1030, 'patient0030', 'patient0030@evercare.test', '$2a$10$l4UA2SJnGJU5fRkR/bo4lenImEOHrUjw4e3wS8Ih14TUGuBPeqW9O', 'Benh Nhan 0030', '0910000030', NULL, TRUE, TRUE, TRUE),
(1031, 'patient0031', 'patient0031@evercare.test', '$2a$10$l4UA2SJnGJU5fRkR/bo4lenImEOHrUjw4e3wS8Ih14TUGuBPeqW9O', 'Benh Nhan 0031', '0910000031', NULL, TRUE, TRUE, TRUE),
(1032, 'patient0032', 'patient0032@evercare.test', '$2a$10$l4UA2SJnGJU5fRkR/bo4lenImEOHrUjw4e3wS8Ih14TUGuBPeqW9O', 'Benh Nhan 0032', '0910000032', NULL, TRUE, TRUE, TRUE),
(1033, 'patient0033', 'patient0033@evercare.test', '$2a$10$l4UA2SJnGJU5fRkR/bo4lenImEOHrUjw4e3wS8Ih14TUGuBPeqW9O', 'Benh Nhan 0033', '0910000033', NULL, TRUE, TRUE, TRUE),
(1034, 'patient0034', 'patient0034@evercare.test', '$2a$10$l4UA2SJnGJU5fRkR/bo4lenImEOHrUjw4e3wS8Ih14TUGuBPeqW9O', 'Benh Nhan 0034', '0910000034', NULL, TRUE, TRUE, TRUE),
(1035, 'patient0035', 'patient0035@evercare.test', '$2a$10$l4UA2SJnGJU5fRkR/bo4lenImEOHrUjw4e3wS8Ih14TUGuBPeqW9O', 'Benh Nhan 0035', '0910000035', NULL, TRUE, TRUE, TRUE),
(1036, 'patient0036', 'patient0036@evercare.test', '$2a$10$l4UA2SJnGJU5fRkR/bo4lenImEOHrUjw4e3wS8Ih14TUGuBPeqW9O', 'Benh Nhan 0036', '0910000036', NULL, TRUE, TRUE, TRUE),
(1037, 'patient0037', 'patient0037@evercare.test', '$2a$10$l4UA2SJnGJU5fRkR/bo4lenImEOHrUjw4e3wS8Ih14TUGuBPeqW9O', 'Benh Nhan 0037', '0910000037', NULL, TRUE, TRUE, TRUE),
(1038, 'patient0038', 'patient0038@evercare.test', '$2a$10$l4UA2SJnGJU5fRkR/bo4lenImEOHrUjw4e3wS8Ih14TUGuBPeqW9O', 'Benh Nhan 0038', '0910000038', NULL, TRUE, TRUE, TRUE),
(1039, 'patient0039', 'patient0039@evercare.test', '$2a$10$l4UA2SJnGJU5fRkR/bo4lenImEOHrUjw4e3wS8Ih14TUGuBPeqW9O', 'Benh Nhan 0039', '0910000039', NULL, TRUE, TRUE, TRUE),
(1040, 'patient0040', 'patient0040@evercare.test', '$2a$10$l4UA2SJnGJU5fRkR/bo4lenImEOHrUjw4e3wS8Ih14TUGuBPeqW9O', 'Benh Nhan 0040', '0910000040', NULL, TRUE, TRUE, TRUE),
(1041, 'patient0041', 'patient0041@evercare.test', '$2a$10$l4UA2SJnGJU5fRkR/bo4lenImEOHrUjw4e3wS8Ih14TUGuBPeqW9O', 'Benh Nhan 0041', '0910000041', NULL, TRUE, TRUE, TRUE),
(1042, 'patient0042', 'patient0042@evercare.test', '$2a$10$l4UA2SJnGJU5fRkR/bo4lenImEOHrUjw4e3wS8Ih14TUGuBPeqW9O', 'Benh Nhan 0042', '0910000042', NULL, TRUE, TRUE, TRUE),
(1043, 'patient0043', 'patient0043@evercare.test', '$2a$10$l4UA2SJnGJU5fRkR/bo4lenImEOHrUjw4e3wS8Ih14TUGuBPeqW9O', 'Benh Nhan 0043', '0910000043', NULL, TRUE, TRUE, TRUE),
(1044, 'patient0044', 'patient0044@evercare.test', '$2a$10$l4UA2SJnGJU5fRkR/bo4lenImEOHrUjw4e3wS8Ih14TUGuBPeqW9O', 'Benh Nhan 0044', '0910000044', NULL, TRUE, TRUE, TRUE),
(1045, 'patient0045', 'patient0045@evercare.test', '$2a$10$l4UA2SJnGJU5fRkR/bo4lenImEOHrUjw4e3wS8Ih14TUGuBPeqW9O', 'Benh Nhan 0045', '0910000045', NULL, TRUE, TRUE, TRUE),
(1046, 'patient0046', 'patient0046@evercare.test', '$2a$10$l4UA2SJnGJU5fRkR/bo4lenImEOHrUjw4e3wS8Ih14TUGuBPeqW9O', 'Benh Nhan 0046', '0910000046', NULL, TRUE, TRUE, TRUE),
(1047, 'patient0047', 'patient0047@evercare.test', '$2a$10$l4UA2SJnGJU5fRkR/bo4lenImEOHrUjw4e3wS8Ih14TUGuBPeqW9O', 'Benh Nhan 0047', '0910000047', NULL, TRUE, TRUE, TRUE),
(1048, 'patient0048', 'patient0048@evercare.test', '$2a$10$l4UA2SJnGJU5fRkR/bo4lenImEOHrUjw4e3wS8Ih14TUGuBPeqW9O', 'Benh Nhan 0048', '0910000048', NULL, TRUE, TRUE, TRUE),
(1049, 'patient0049', 'patient0049@evercare.test', '$2a$10$l4UA2SJnGJU5fRkR/bo4lenImEOHrUjw4e3wS8Ih14TUGuBPeqW9O', 'Benh Nhan 0049', '0910000049', NULL, TRUE, TRUE, TRUE),
(1050, 'patient0050', 'patient0050@evercare.test', '$2a$10$l4UA2SJnGJU5fRkR/bo4lenImEOHrUjw4e3wS8Ih14TUGuBPeqW9O', 'Benh Nhan 0050', '0910000050', NULL, TRUE, TRUE, TRUE),
(1051, 'patient0051', 'patient0051@evercare.test', '$2a$10$l4UA2SJnGJU5fRkR/bo4lenImEOHrUjw4e3wS8Ih14TUGuBPeqW9O', 'Benh Nhan 0051', '0910000051', NULL, TRUE, TRUE, TRUE),
(1052, 'patient0052', 'patient0052@evercare.test', '$2a$10$l4UA2SJnGJU5fRkR/bo4lenImEOHrUjw4e3wS8Ih14TUGuBPeqW9O', 'Benh Nhan 0052', '0910000052', NULL, TRUE, TRUE, TRUE),
(1053, 'patient0053', 'patient0053@evercare.test', '$2a$10$l4UA2SJnGJU5fRkR/bo4lenImEOHrUjw4e3wS8Ih14TUGuBPeqW9O', 'Benh Nhan 0053', '0910000053', NULL, TRUE, TRUE, TRUE),
(1054, 'patient0054', 'patient0054@evercare.test', '$2a$10$l4UA2SJnGJU5fRkR/bo4lenImEOHrUjw4e3wS8Ih14TUGuBPeqW9O', 'Benh Nhan 0054', '0910000054', NULL, TRUE, TRUE, TRUE),
(1055, 'patient0055', 'patient0055@evercare.test', '$2a$10$l4UA2SJnGJU5fRkR/bo4lenImEOHrUjw4e3wS8Ih14TUGuBPeqW9O', 'Benh Nhan 0055', '0910000055', NULL, TRUE, TRUE, TRUE),
(1056, 'patient0056', 'patient0056@evercare.test', '$2a$10$l4UA2SJnGJU5fRkR/bo4lenImEOHrUjw4e3wS8Ih14TUGuBPeqW9O', 'Benh Nhan 0056', '0910000056', NULL, TRUE, TRUE, TRUE),
(1057, 'patient0057', 'patient0057@evercare.test', '$2a$10$l4UA2SJnGJU5fRkR/bo4lenImEOHrUjw4e3wS8Ih14TUGuBPeqW9O', 'Benh Nhan 0057', '0910000057', NULL, TRUE, TRUE, TRUE),
(1058, 'patient0058', 'patient0058@evercare.test', '$2a$10$l4UA2SJnGJU5fRkR/bo4lenImEOHrUjw4e3wS8Ih14TUGuBPeqW9O', 'Benh Nhan 0058', '0910000058', NULL, TRUE, TRUE, TRUE),
(1059, 'patient0059', 'patient0059@evercare.test', '$2a$10$l4UA2SJnGJU5fRkR/bo4lenImEOHrUjw4e3wS8Ih14TUGuBPeqW9O', 'Benh Nhan 0059', '0910000059', NULL, TRUE, TRUE, TRUE),
(1060, 'patient0060', 'patient0060@evercare.test', '$2a$10$l4UA2SJnGJU5fRkR/bo4lenImEOHrUjw4e3wS8Ih14TUGuBPeqW9O', 'Benh Nhan 0060', '0910000060', NULL, TRUE, TRUE, TRUE),
(1061, 'patient0061', 'patient0061@evercare.test', '$2a$10$l4UA2SJnGJU5fRkR/bo4lenImEOHrUjw4e3wS8Ih14TUGuBPeqW9O', 'Benh Nhan 0061', '0910000061', NULL, TRUE, TRUE, TRUE),
(1062, 'patient0062', 'patient0062@evercare.test', '$2a$10$l4UA2SJnGJU5fRkR/bo4lenImEOHrUjw4e3wS8Ih14TUGuBPeqW9O', 'Benh Nhan 0062', '0910000062', NULL, TRUE, TRUE, TRUE),
(1063, 'patient0063', 'patient0063@evercare.test', '$2a$10$l4UA2SJnGJU5fRkR/bo4lenImEOHrUjw4e3wS8Ih14TUGuBPeqW9O', 'Benh Nhan 0063', '0910000063', NULL, TRUE, TRUE, TRUE),
(1064, 'patient0064', 'patient0064@evercare.test', '$2a$10$l4UA2SJnGJU5fRkR/bo4lenImEOHrUjw4e3wS8Ih14TUGuBPeqW9O', 'Benh Nhan 0064', '0910000064', NULL, TRUE, TRUE, TRUE),
(1065, 'patient0065', 'patient0065@evercare.test', '$2a$10$l4UA2SJnGJU5fRkR/bo4lenImEOHrUjw4e3wS8Ih14TUGuBPeqW9O', 'Benh Nhan 0065', '0910000065', NULL, TRUE, TRUE, TRUE),
(1066, 'patient0066', 'patient0066@evercare.test', '$2a$10$l4UA2SJnGJU5fRkR/bo4lenImEOHrUjw4e3wS8Ih14TUGuBPeqW9O', 'Benh Nhan 0066', '0910000066', NULL, TRUE, TRUE, TRUE),
(1067, 'patient0067', 'patient0067@evercare.test', '$2a$10$l4UA2SJnGJU5fRkR/bo4lenImEOHrUjw4e3wS8Ih14TUGuBPeqW9O', 'Benh Nhan 0067', '0910000067', NULL, TRUE, TRUE, TRUE),
(1068, 'patient0068', 'patient0068@evercare.test', '$2a$10$l4UA2SJnGJU5fRkR/bo4lenImEOHrUjw4e3wS8Ih14TUGuBPeqW9O', 'Benh Nhan 0068', '0910000068', NULL, TRUE, TRUE, TRUE),
(1069, 'patient0069', 'patient0069@evercare.test', '$2a$10$l4UA2SJnGJU5fRkR/bo4lenImEOHrUjw4e3wS8Ih14TUGuBPeqW9O', 'Benh Nhan 0069', '0910000069', NULL, TRUE, TRUE, TRUE),
(1070, 'patient0070', 'patient0070@evercare.test', '$2a$10$l4UA2SJnGJU5fRkR/bo4lenImEOHrUjw4e3wS8Ih14TUGuBPeqW9O', 'Benh Nhan 0070', '0910000070', NULL, TRUE, TRUE, TRUE),
(1071, 'patient0071', 'patient0071@evercare.test', '$2a$10$l4UA2SJnGJU5fRkR/bo4lenImEOHrUjw4e3wS8Ih14TUGuBPeqW9O', 'Benh Nhan 0071', '0910000071', NULL, TRUE, TRUE, TRUE),
(1072, 'patient0072', 'patient0072@evercare.test', '$2a$10$l4UA2SJnGJU5fRkR/bo4lenImEOHrUjw4e3wS8Ih14TUGuBPeqW9O', 'Benh Nhan 0072', '0910000072', NULL, TRUE, TRUE, TRUE),
(1073, 'patient0073', 'patient0073@evercare.test', '$2a$10$l4UA2SJnGJU5fRkR/bo4lenImEOHrUjw4e3wS8Ih14TUGuBPeqW9O', 'Benh Nhan 0073', '0910000073', NULL, TRUE, TRUE, TRUE),
(1074, 'patient0074', 'patient0074@evercare.test', '$2a$10$l4UA2SJnGJU5fRkR/bo4lenImEOHrUjw4e3wS8Ih14TUGuBPeqW9O', 'Benh Nhan 0074', '0910000074', NULL, TRUE, TRUE, TRUE),
(1075, 'patient0075', 'patient0075@evercare.test', '$2a$10$l4UA2SJnGJU5fRkR/bo4lenImEOHrUjw4e3wS8Ih14TUGuBPeqW9O', 'Benh Nhan 0075', '0910000075', NULL, TRUE, TRUE, TRUE),
(1076, 'patient0076', 'patient0076@evercare.test', '$2a$10$l4UA2SJnGJU5fRkR/bo4lenImEOHrUjw4e3wS8Ih14TUGuBPeqW9O', 'Benh Nhan 0076', '0910000076', NULL, TRUE, TRUE, TRUE),
(1077, 'patient0077', 'patient0077@evercare.test', '$2a$10$l4UA2SJnGJU5fRkR/bo4lenImEOHrUjw4e3wS8Ih14TUGuBPeqW9O', 'Benh Nhan 0077', '0910000077', NULL, TRUE, TRUE, TRUE),
(1078, 'patient0078', 'patient0078@evercare.test', '$2a$10$l4UA2SJnGJU5fRkR/bo4lenImEOHrUjw4e3wS8Ih14TUGuBPeqW9O', 'Benh Nhan 0078', '0910000078', NULL, TRUE, TRUE, TRUE),
(1079, 'patient0079', 'patient0079@evercare.test', '$2a$10$l4UA2SJnGJU5fRkR/bo4lenImEOHrUjw4e3wS8Ih14TUGuBPeqW9O', 'Benh Nhan 0079', '0910000079', NULL, TRUE, TRUE, TRUE),
(1080, 'patient0080', 'patient0080@evercare.test', '$2a$10$l4UA2SJnGJU5fRkR/bo4lenImEOHrUjw4e3wS8Ih14TUGuBPeqW9O', 'Benh Nhan 0080', '0910000080', NULL, TRUE, TRUE, TRUE),
(1081, 'patient0081', 'patient0081@evercare.test', '$2a$10$l4UA2SJnGJU5fRkR/bo4lenImEOHrUjw4e3wS8Ih14TUGuBPeqW9O', 'Benh Nhan 0081', '0910000081', NULL, TRUE, TRUE, TRUE),
(1082, 'patient0082', 'patient0082@evercare.test', '$2a$10$l4UA2SJnGJU5fRkR/bo4lenImEOHrUjw4e3wS8Ih14TUGuBPeqW9O', 'Benh Nhan 0082', '0910000082', NULL, TRUE, TRUE, TRUE),
(1083, 'patient0083', 'patient0083@evercare.test', '$2a$10$l4UA2SJnGJU5fRkR/bo4lenImEOHrUjw4e3wS8Ih14TUGuBPeqW9O', 'Benh Nhan 0083', '0910000083', NULL, TRUE, TRUE, TRUE),
(1084, 'patient0084', 'patient0084@evercare.test', '$2a$10$l4UA2SJnGJU5fRkR/bo4lenImEOHrUjw4e3wS8Ih14TUGuBPeqW9O', 'Benh Nhan 0084', '0910000084', NULL, TRUE, TRUE, TRUE),
(1085, 'patient0085', 'patient0085@evercare.test', '$2a$10$l4UA2SJnGJU5fRkR/bo4lenImEOHrUjw4e3wS8Ih14TUGuBPeqW9O', 'Benh Nhan 0085', '0910000085', NULL, TRUE, TRUE, TRUE),
(1086, 'patient0086', 'patient0086@evercare.test', '$2a$10$l4UA2SJnGJU5fRkR/bo4lenImEOHrUjw4e3wS8Ih14TUGuBPeqW9O', 'Benh Nhan 0086', '0910000086', NULL, TRUE, TRUE, TRUE),
(1087, 'patient0087', 'patient0087@evercare.test', '$2a$10$l4UA2SJnGJU5fRkR/bo4lenImEOHrUjw4e3wS8Ih14TUGuBPeqW9O', 'Benh Nhan 0087', '0910000087', NULL, TRUE, TRUE, TRUE),
(1088, 'patient0088', 'patient0088@evercare.test', '$2a$10$l4UA2SJnGJU5fRkR/bo4lenImEOHrUjw4e3wS8Ih14TUGuBPeqW9O', 'Benh Nhan 0088', '0910000088', NULL, TRUE, TRUE, TRUE),
(1089, 'patient0089', 'patient0089@evercare.test', '$2a$10$l4UA2SJnGJU5fRkR/bo4lenImEOHrUjw4e3wS8Ih14TUGuBPeqW9O', 'Benh Nhan 0089', '0910000089', NULL, TRUE, TRUE, TRUE),
(1090, 'patient0090', 'patient0090@evercare.test', '$2a$10$l4UA2SJnGJU5fRkR/bo4lenImEOHrUjw4e3wS8Ih14TUGuBPeqW9O', 'Benh Nhan 0090', '0910000090', NULL, TRUE, TRUE, TRUE),
(1091, 'patient0091', 'patient0091@evercare.test', '$2a$10$l4UA2SJnGJU5fRkR/bo4lenImEOHrUjw4e3wS8Ih14TUGuBPeqW9O', 'Benh Nhan 0091', '0910000091', NULL, TRUE, TRUE, TRUE),
(1092, 'patient0092', 'patient0092@evercare.test', '$2a$10$l4UA2SJnGJU5fRkR/bo4lenImEOHrUjw4e3wS8Ih14TUGuBPeqW9O', 'Benh Nhan 0092', '0910000092', NULL, TRUE, TRUE, TRUE),
(1093, 'patient0093', 'patient0093@evercare.test', '$2a$10$l4UA2SJnGJU5fRkR/bo4lenImEOHrUjw4e3wS8Ih14TUGuBPeqW9O', 'Benh Nhan 0093', '0910000093', NULL, TRUE, TRUE, TRUE),
(1094, 'patient0094', 'patient0094@evercare.test', '$2a$10$l4UA2SJnGJU5fRkR/bo4lenImEOHrUjw4e3wS8Ih14TUGuBPeqW9O', 'Benh Nhan 0094', '0910000094', NULL, TRUE, TRUE, TRUE),
(1095, 'patient0095', 'patient0095@evercare.test', '$2a$10$l4UA2SJnGJU5fRkR/bo4lenImEOHrUjw4e3wS8Ih14TUGuBPeqW9O', 'Benh Nhan 0095', '0910000095', NULL, TRUE, TRUE, TRUE),
(1096, 'patient0096', 'patient0096@evercare.test', '$2a$10$l4UA2SJnGJU5fRkR/bo4lenImEOHrUjw4e3wS8Ih14TUGuBPeqW9O', 'Benh Nhan 0096', '0910000096', NULL, TRUE, TRUE, TRUE),
(1097, 'patient0097', 'patient0097@evercare.test', '$2a$10$l4UA2SJnGJU5fRkR/bo4lenImEOHrUjw4e3wS8Ih14TUGuBPeqW9O', 'Benh Nhan 0097', '0910000097', NULL, TRUE, TRUE, TRUE),
(1098, 'patient0098', 'patient0098@evercare.test', '$2a$10$l4UA2SJnGJU5fRkR/bo4lenImEOHrUjw4e3wS8Ih14TUGuBPeqW9O', 'Benh Nhan 0098', '0910000098', NULL, TRUE, TRUE, TRUE),
(1099, 'patient0099', 'patient0099@evercare.test', '$2a$10$l4UA2SJnGJU5fRkR/bo4lenImEOHrUjw4e3wS8Ih14TUGuBPeqW9O', 'Benh Nhan 0099', '0910000099', NULL, TRUE, TRUE, TRUE),
(1100, 'patient0100', 'patient0100@evercare.test', '$2a$10$l4UA2SJnGJU5fRkR/bo4lenImEOHrUjw4e3wS8Ih14TUGuBPeqW9O', 'Benh Nhan 0100', '0910000100', NULL, TRUE, TRUE, TRUE),
(1101, 'patient0101', 'patient0101@evercare.test', '$2a$10$l4UA2SJnGJU5fRkR/bo4lenImEOHrUjw4e3wS8Ih14TUGuBPeqW9O', 'Benh Nhan 0101', '0910000101', NULL, TRUE, TRUE, TRUE),
(1102, 'patient0102', 'patient0102@evercare.test', '$2a$10$l4UA2SJnGJU5fRkR/bo4lenImEOHrUjw4e3wS8Ih14TUGuBPeqW9O', 'Benh Nhan 0102', '0910000102', NULL, TRUE, TRUE, TRUE),
(1103, 'patient0103', 'patient0103@evercare.test', '$2a$10$l4UA2SJnGJU5fRkR/bo4lenImEOHrUjw4e3wS8Ih14TUGuBPeqW9O', 'Benh Nhan 0103', '0910000103', NULL, TRUE, TRUE, TRUE),
(1104, 'patient0104', 'patient0104@evercare.test', '$2a$10$l4UA2SJnGJU5fRkR/bo4lenImEOHrUjw4e3wS8Ih14TUGuBPeqW9O', 'Benh Nhan 0104', '0910000104', NULL, TRUE, TRUE, TRUE),
(1105, 'patient0105', 'patient0105@evercare.test', '$2a$10$l4UA2SJnGJU5fRkR/bo4lenImEOHrUjw4e3wS8Ih14TUGuBPeqW9O', 'Benh Nhan 0105', '0910000105', NULL, TRUE, TRUE, TRUE),
(1106, 'patient0106', 'patient0106@evercare.test', '$2a$10$l4UA2SJnGJU5fRkR/bo4lenImEOHrUjw4e3wS8Ih14TUGuBPeqW9O', 'Benh Nhan 0106', '0910000106', NULL, TRUE, TRUE, TRUE),
(1107, 'patient0107', 'patient0107@evercare.test', '$2a$10$l4UA2SJnGJU5fRkR/bo4lenImEOHrUjw4e3wS8Ih14TUGuBPeqW9O', 'Benh Nhan 0107', '0910000107', NULL, TRUE, TRUE, TRUE),
(1108, 'patient0108', 'patient0108@evercare.test', '$2a$10$l4UA2SJnGJU5fRkR/bo4lenImEOHrUjw4e3wS8Ih14TUGuBPeqW9O', 'Benh Nhan 0108', '0910000108', NULL, TRUE, TRUE, TRUE),
(1109, 'patient0109', 'patient0109@evercare.test', '$2a$10$l4UA2SJnGJU5fRkR/bo4lenImEOHrUjw4e3wS8Ih14TUGuBPeqW9O', 'Benh Nhan 0109', '0910000109', NULL, TRUE, TRUE, TRUE),
(1110, 'patient0110', 'patient0110@evercare.test', '$2a$10$l4UA2SJnGJU5fRkR/bo4lenImEOHrUjw4e3wS8Ih14TUGuBPeqW9O', 'Benh Nhan 0110', '0910000110', NULL, TRUE, TRUE, TRUE),
(1111, 'patient0111', 'patient0111@evercare.test', '$2a$10$l4UA2SJnGJU5fRkR/bo4lenImEOHrUjw4e3wS8Ih14TUGuBPeqW9O', 'Benh Nhan 0111', '0910000111', NULL, TRUE, TRUE, TRUE),
(1112, 'patient0112', 'patient0112@evercare.test', '$2a$10$l4UA2SJnGJU5fRkR/bo4lenImEOHrUjw4e3wS8Ih14TUGuBPeqW9O', 'Benh Nhan 0112', '0910000112', NULL, TRUE, TRUE, TRUE),
(1113, 'patient0113', 'patient0113@evercare.test', '$2a$10$l4UA2SJnGJU5fRkR/bo4lenImEOHrUjw4e3wS8Ih14TUGuBPeqW9O', 'Benh Nhan 0113', '0910000113', NULL, TRUE, TRUE, TRUE),
(1114, 'patient0114', 'patient0114@evercare.test', '$2a$10$l4UA2SJnGJU5fRkR/bo4lenImEOHrUjw4e3wS8Ih14TUGuBPeqW9O', 'Benh Nhan 0114', '0910000114', NULL, TRUE, TRUE, TRUE),
(1115, 'patient0115', 'patient0115@evercare.test', '$2a$10$l4UA2SJnGJU5fRkR/bo4lenImEOHrUjw4e3wS8Ih14TUGuBPeqW9O', 'Benh Nhan 0115', '0910000115', NULL, TRUE, TRUE, TRUE),
(1116, 'patient0116', 'patient0116@evercare.test', '$2a$10$l4UA2SJnGJU5fRkR/bo4lenImEOHrUjw4e3wS8Ih14TUGuBPeqW9O', 'Benh Nhan 0116', '0910000116', NULL, TRUE, TRUE, TRUE),
(1117, 'patient0117', 'patient0117@evercare.test', '$2a$10$l4UA2SJnGJU5fRkR/bo4lenImEOHrUjw4e3wS8Ih14TUGuBPeqW9O', 'Benh Nhan 0117', '0910000117', NULL, TRUE, TRUE, TRUE),
(1118, 'patient0118', 'patient0118@evercare.test', '$2a$10$l4UA2SJnGJU5fRkR/bo4lenImEOHrUjw4e3wS8Ih14TUGuBPeqW9O', 'Benh Nhan 0118', '0910000118', NULL, TRUE, TRUE, TRUE),
(1119, 'patient0119', 'patient0119@evercare.test', '$2a$10$l4UA2SJnGJU5fRkR/bo4lenImEOHrUjw4e3wS8Ih14TUGuBPeqW9O', 'Benh Nhan 0119', '0910000119', NULL, TRUE, TRUE, TRUE),
(1120, 'patient0120', 'patient0120@evercare.test', '$2a$10$l4UA2SJnGJU5fRkR/bo4lenImEOHrUjw4e3wS8Ih14TUGuBPeqW9O', 'Benh Nhan 0120', '0910000120', NULL, TRUE, TRUE, TRUE),
(1121, 'patient0121', 'patient0121@evercare.test', '$2a$10$l4UA2SJnGJU5fRkR/bo4lenImEOHrUjw4e3wS8Ih14TUGuBPeqW9O', 'Benh Nhan 0121', '0910000121', NULL, TRUE, TRUE, TRUE),
(1122, 'patient0122', 'patient0122@evercare.test', '$2a$10$l4UA2SJnGJU5fRkR/bo4lenImEOHrUjw4e3wS8Ih14TUGuBPeqW9O', 'Benh Nhan 0122', '0910000122', NULL, TRUE, TRUE, TRUE),
(1123, 'patient0123', 'patient0123@evercare.test', '$2a$10$l4UA2SJnGJU5fRkR/bo4lenImEOHrUjw4e3wS8Ih14TUGuBPeqW9O', 'Benh Nhan 0123', '0910000123', NULL, TRUE, TRUE, TRUE),
(1124, 'patient0124', 'patient0124@evercare.test', '$2a$10$l4UA2SJnGJU5fRkR/bo4lenImEOHrUjw4e3wS8Ih14TUGuBPeqW9O', 'Benh Nhan 0124', '0910000124', NULL, TRUE, TRUE, TRUE),
(1125, 'patient0125', 'patient0125@evercare.test', '$2a$10$l4UA2SJnGJU5fRkR/bo4lenImEOHrUjw4e3wS8Ih14TUGuBPeqW9O', 'Benh Nhan 0125', '0910000125', NULL, TRUE, TRUE, TRUE),
(1126, 'patient0126', 'patient0126@evercare.test', '$2a$10$l4UA2SJnGJU5fRkR/bo4lenImEOHrUjw4e3wS8Ih14TUGuBPeqW9O', 'Benh Nhan 0126', '0910000126', NULL, TRUE, TRUE, TRUE),
(1127, 'patient0127', 'patient0127@evercare.test', '$2a$10$l4UA2SJnGJU5fRkR/bo4lenImEOHrUjw4e3wS8Ih14TUGuBPeqW9O', 'Benh Nhan 0127', '0910000127', NULL, TRUE, TRUE, TRUE),
(1128, 'patient0128', 'patient0128@evercare.test', '$2a$10$l4UA2SJnGJU5fRkR/bo4lenImEOHrUjw4e3wS8Ih14TUGuBPeqW9O', 'Benh Nhan 0128', '0910000128', NULL, TRUE, TRUE, TRUE),
(1129, 'patient0129', 'patient0129@evercare.test', '$2a$10$l4UA2SJnGJU5fRkR/bo4lenImEOHrUjw4e3wS8Ih14TUGuBPeqW9O', 'Benh Nhan 0129', '0910000129', NULL, TRUE, TRUE, TRUE),
(1130, 'patient0130', 'patient0130@evercare.test', '$2a$10$l4UA2SJnGJU5fRkR/bo4lenImEOHrUjw4e3wS8Ih14TUGuBPeqW9O', 'Benh Nhan 0130', '0910000130', NULL, TRUE, TRUE, TRUE),
(1131, 'patient0131', 'patient0131@evercare.test', '$2a$10$l4UA2SJnGJU5fRkR/bo4lenImEOHrUjw4e3wS8Ih14TUGuBPeqW9O', 'Benh Nhan 0131', '0910000131', NULL, TRUE, TRUE, TRUE),
(1132, 'patient0132', 'patient0132@evercare.test', '$2a$10$l4UA2SJnGJU5fRkR/bo4lenImEOHrUjw4e3wS8Ih14TUGuBPeqW9O', 'Benh Nhan 0132', '0910000132', NULL, TRUE, TRUE, TRUE),
(1133, 'patient0133', 'patient0133@evercare.test', '$2a$10$l4UA2SJnGJU5fRkR/bo4lenImEOHrUjw4e3wS8Ih14TUGuBPeqW9O', 'Benh Nhan 0133', '0910000133', NULL, TRUE, TRUE, TRUE),
(1134, 'patient0134', 'patient0134@evercare.test', '$2a$10$l4UA2SJnGJU5fRkR/bo4lenImEOHrUjw4e3wS8Ih14TUGuBPeqW9O', 'Benh Nhan 0134', '0910000134', NULL, TRUE, TRUE, TRUE),
(1135, 'patient0135', 'patient0135@evercare.test', '$2a$10$l4UA2SJnGJU5fRkR/bo4lenImEOHrUjw4e3wS8Ih14TUGuBPeqW9O', 'Benh Nhan 0135', '0910000135', NULL, TRUE, TRUE, TRUE),
(1136, 'patient0136', 'patient0136@evercare.test', '$2a$10$l4UA2SJnGJU5fRkR/bo4lenImEOHrUjw4e3wS8Ih14TUGuBPeqW9O', 'Benh Nhan 0136', '0910000136', NULL, TRUE, TRUE, TRUE),
(1137, 'patient0137', 'patient0137@evercare.test', '$2a$10$l4UA2SJnGJU5fRkR/bo4lenImEOHrUjw4e3wS8Ih14TUGuBPeqW9O', 'Benh Nhan 0137', '0910000137', NULL, TRUE, TRUE, TRUE),
(1138, 'patient0138', 'patient0138@evercare.test', '$2a$10$l4UA2SJnGJU5fRkR/bo4lenImEOHrUjw4e3wS8Ih14TUGuBPeqW9O', 'Benh Nhan 0138', '0910000138', NULL, TRUE, TRUE, TRUE),
(1139, 'patient0139', 'patient0139@evercare.test', '$2a$10$l4UA2SJnGJU5fRkR/bo4lenImEOHrUjw4e3wS8Ih14TUGuBPeqW9O', 'Benh Nhan 0139', '0910000139', NULL, TRUE, TRUE, TRUE),
(1140, 'patient0140', 'patient0140@evercare.test', '$2a$10$l4UA2SJnGJU5fRkR/bo4lenImEOHrUjw4e3wS8Ih14TUGuBPeqW9O', 'Benh Nhan 0140', '0910000140', NULL, TRUE, TRUE, TRUE),
(1141, 'patient0141', 'patient0141@evercare.test', '$2a$10$l4UA2SJnGJU5fRkR/bo4lenImEOHrUjw4e3wS8Ih14TUGuBPeqW9O', 'Benh Nhan 0141', '0910000141', NULL, TRUE, TRUE, TRUE),
(1142, 'patient0142', 'patient0142@evercare.test', '$2a$10$l4UA2SJnGJU5fRkR/bo4lenImEOHrUjw4e3wS8Ih14TUGuBPeqW9O', 'Benh Nhan 0142', '0910000142', NULL, TRUE, TRUE, TRUE),
(1143, 'patient0143', 'patient0143@evercare.test', '$2a$10$l4UA2SJnGJU5fRkR/bo4lenImEOHrUjw4e3wS8Ih14TUGuBPeqW9O', 'Benh Nhan 0143', '0910000143', NULL, TRUE, TRUE, TRUE),
(1144, 'patient0144', 'patient0144@evercare.test', '$2a$10$l4UA2SJnGJU5fRkR/bo4lenImEOHrUjw4e3wS8Ih14TUGuBPeqW9O', 'Benh Nhan 0144', '0910000144', NULL, TRUE, TRUE, TRUE),
(1145, 'patient0145', 'patient0145@evercare.test', '$2a$10$l4UA2SJnGJU5fRkR/bo4lenImEOHrUjw4e3wS8Ih14TUGuBPeqW9O', 'Benh Nhan 0145', '0910000145', NULL, TRUE, TRUE, TRUE),
(1146, 'patient0146', 'patient0146@evercare.test', '$2a$10$l4UA2SJnGJU5fRkR/bo4lenImEOHrUjw4e3wS8Ih14TUGuBPeqW9O', 'Benh Nhan 0146', '0910000146', NULL, TRUE, TRUE, TRUE),
(1147, 'patient0147', 'patient0147@evercare.test', '$2a$10$l4UA2SJnGJU5fRkR/bo4lenImEOHrUjw4e3wS8Ih14TUGuBPeqW9O', 'Benh Nhan 0147', '0910000147', NULL, TRUE, TRUE, TRUE),
(1148, 'patient0148', 'patient0148@evercare.test', '$2a$10$l4UA2SJnGJU5fRkR/bo4lenImEOHrUjw4e3wS8Ih14TUGuBPeqW9O', 'Benh Nhan 0148', '0910000148', NULL, TRUE, TRUE, TRUE),
(1149, 'patient0149', 'patient0149@evercare.test', '$2a$10$l4UA2SJnGJU5fRkR/bo4lenImEOHrUjw4e3wS8Ih14TUGuBPeqW9O', 'Benh Nhan 0149', '0910000149', NULL, TRUE, TRUE, TRUE),
(1150, 'patient0150', 'patient0150@evercare.test', '$2a$10$l4UA2SJnGJU5fRkR/bo4lenImEOHrUjw4e3wS8Ih14TUGuBPeqW9O', 'Benh Nhan 0150', '0910000150', NULL, TRUE, TRUE, TRUE),
(1151, 'patient0151', 'patient0151@evercare.test', '$2a$10$l4UA2SJnGJU5fRkR/bo4lenImEOHrUjw4e3wS8Ih14TUGuBPeqW9O', 'Benh Nhan 0151', '0910000151', NULL, TRUE, TRUE, TRUE),
(1152, 'patient0152', 'patient0152@evercare.test', '$2a$10$l4UA2SJnGJU5fRkR/bo4lenImEOHrUjw4e3wS8Ih14TUGuBPeqW9O', 'Benh Nhan 0152', '0910000152', NULL, TRUE, TRUE, TRUE),
(1153, 'patient0153', 'patient0153@evercare.test', '$2a$10$l4UA2SJnGJU5fRkR/bo4lenImEOHrUjw4e3wS8Ih14TUGuBPeqW9O', 'Benh Nhan 0153', '0910000153', NULL, TRUE, TRUE, TRUE),
(1154, 'patient0154', 'patient0154@evercare.test', '$2a$10$l4UA2SJnGJU5fRkR/bo4lenImEOHrUjw4e3wS8Ih14TUGuBPeqW9O', 'Benh Nhan 0154', '0910000154', NULL, TRUE, TRUE, TRUE),
(1155, 'patient0155', 'patient0155@evercare.test', '$2a$10$l4UA2SJnGJU5fRkR/bo4lenImEOHrUjw4e3wS8Ih14TUGuBPeqW9O', 'Benh Nhan 0155', '0910000155', NULL, TRUE, TRUE, TRUE),
(1156, 'patient0156', 'patient0156@evercare.test', '$2a$10$l4UA2SJnGJU5fRkR/bo4lenImEOHrUjw4e3wS8Ih14TUGuBPeqW9O', 'Benh Nhan 0156', '0910000156', NULL, TRUE, TRUE, TRUE),
(1157, 'patient0157', 'patient0157@evercare.test', '$2a$10$l4UA2SJnGJU5fRkR/bo4lenImEOHrUjw4e3wS8Ih14TUGuBPeqW9O', 'Benh Nhan 0157', '0910000157', NULL, TRUE, TRUE, TRUE),
(1158, 'patient0158', 'patient0158@evercare.test', '$2a$10$l4UA2SJnGJU5fRkR/bo4lenImEOHrUjw4e3wS8Ih14TUGuBPeqW9O', 'Benh Nhan 0158', '0910000158', NULL, TRUE, TRUE, TRUE),
(1159, 'patient0159', 'patient0159@evercare.test', '$2a$10$l4UA2SJnGJU5fRkR/bo4lenImEOHrUjw4e3wS8Ih14TUGuBPeqW9O', 'Benh Nhan 0159', '0910000159', NULL, TRUE, TRUE, TRUE),
(1160, 'patient0160', 'patient0160@evercare.test', '$2a$10$l4UA2SJnGJU5fRkR/bo4lenImEOHrUjw4e3wS8Ih14TUGuBPeqW9O', 'Benh Nhan 0160', '0910000160', NULL, TRUE, TRUE, TRUE),
(1161, 'patient0161', 'patient0161@evercare.test', '$2a$10$l4UA2SJnGJU5fRkR/bo4lenImEOHrUjw4e3wS8Ih14TUGuBPeqW9O', 'Benh Nhan 0161', '0910000161', NULL, TRUE, TRUE, TRUE),
(1162, 'patient0162', 'patient0162@evercare.test', '$2a$10$l4UA2SJnGJU5fRkR/bo4lenImEOHrUjw4e3wS8Ih14TUGuBPeqW9O', 'Benh Nhan 0162', '0910000162', NULL, TRUE, TRUE, TRUE),
(1163, 'patient0163', 'patient0163@evercare.test', '$2a$10$l4UA2SJnGJU5fRkR/bo4lenImEOHrUjw4e3wS8Ih14TUGuBPeqW9O', 'Benh Nhan 0163', '0910000163', NULL, TRUE, TRUE, TRUE),
(1164, 'patient0164', 'patient0164@evercare.test', '$2a$10$l4UA2SJnGJU5fRkR/bo4lenImEOHrUjw4e3wS8Ih14TUGuBPeqW9O', 'Benh Nhan 0164', '0910000164', NULL, TRUE, TRUE, TRUE),
(1165, 'patient0165', 'patient0165@evercare.test', '$2a$10$l4UA2SJnGJU5fRkR/bo4lenImEOHrUjw4e3wS8Ih14TUGuBPeqW9O', 'Benh Nhan 0165', '0910000165', NULL, TRUE, TRUE, TRUE),
(1166, 'patient0166', 'patient0166@evercare.test', '$2a$10$l4UA2SJnGJU5fRkR/bo4lenImEOHrUjw4e3wS8Ih14TUGuBPeqW9O', 'Benh Nhan 0166', '0910000166', NULL, TRUE, TRUE, TRUE),
(1167, 'patient0167', 'patient0167@evercare.test', '$2a$10$l4UA2SJnGJU5fRkR/bo4lenImEOHrUjw4e3wS8Ih14TUGuBPeqW9O', 'Benh Nhan 0167', '0910000167', NULL, TRUE, TRUE, TRUE),
(1168, 'patient0168', 'patient0168@evercare.test', '$2a$10$l4UA2SJnGJU5fRkR/bo4lenImEOHrUjw4e3wS8Ih14TUGuBPeqW9O', 'Benh Nhan 0168', '0910000168', NULL, TRUE, TRUE, TRUE),
(1169, 'patient0169', 'patient0169@evercare.test', '$2a$10$l4UA2SJnGJU5fRkR/bo4lenImEOHrUjw4e3wS8Ih14TUGuBPeqW9O', 'Benh Nhan 0169', '0910000169', NULL, TRUE, TRUE, TRUE),
(1170, 'patient0170', 'patient0170@evercare.test', '$2a$10$l4UA2SJnGJU5fRkR/bo4lenImEOHrUjw4e3wS8Ih14TUGuBPeqW9O', 'Benh Nhan 0170', '0910000170', NULL, TRUE, TRUE, TRUE),
(1171, 'patient0171', 'patient0171@evercare.test', '$2a$10$l4UA2SJnGJU5fRkR/bo4lenImEOHrUjw4e3wS8Ih14TUGuBPeqW9O', 'Benh Nhan 0171', '0910000171', NULL, TRUE, TRUE, TRUE),
(1172, 'patient0172', 'patient0172@evercare.test', '$2a$10$l4UA2SJnGJU5fRkR/bo4lenImEOHrUjw4e3wS8Ih14TUGuBPeqW9O', 'Benh Nhan 0172', '0910000172', NULL, TRUE, TRUE, TRUE),
(1173, 'patient0173', 'patient0173@evercare.test', '$2a$10$l4UA2SJnGJU5fRkR/bo4lenImEOHrUjw4e3wS8Ih14TUGuBPeqW9O', 'Benh Nhan 0173', '0910000173', NULL, TRUE, TRUE, TRUE),
(1174, 'patient0174', 'patient0174@evercare.test', '$2a$10$l4UA2SJnGJU5fRkR/bo4lenImEOHrUjw4e3wS8Ih14TUGuBPeqW9O', 'Benh Nhan 0174', '0910000174', NULL, TRUE, TRUE, TRUE),
(1175, 'patient0175', 'patient0175@evercare.test', '$2a$10$l4UA2SJnGJU5fRkR/bo4lenImEOHrUjw4e3wS8Ih14TUGuBPeqW9O', 'Benh Nhan 0175', '0910000175', NULL, TRUE, TRUE, TRUE),
(1176, 'patient0176', 'patient0176@evercare.test', '$2a$10$l4UA2SJnGJU5fRkR/bo4lenImEOHrUjw4e3wS8Ih14TUGuBPeqW9O', 'Benh Nhan 0176', '0910000176', NULL, TRUE, TRUE, TRUE),
(1177, 'patient0177', 'patient0177@evercare.test', '$2a$10$l4UA2SJnGJU5fRkR/bo4lenImEOHrUjw4e3wS8Ih14TUGuBPeqW9O', 'Benh Nhan 0177', '0910000177', NULL, TRUE, TRUE, TRUE),
(1178, 'patient0178', 'patient0178@evercare.test', '$2a$10$l4UA2SJnGJU5fRkR/bo4lenImEOHrUjw4e3wS8Ih14TUGuBPeqW9O', 'Benh Nhan 0178', '0910000178', NULL, TRUE, TRUE, TRUE),
(1179, 'patient0179', 'patient0179@evercare.test', '$2a$10$l4UA2SJnGJU5fRkR/bo4lenImEOHrUjw4e3wS8Ih14TUGuBPeqW9O', 'Benh Nhan 0179', '0910000179', NULL, TRUE, TRUE, TRUE),
(1180, 'patient0180', 'patient0180@evercare.test', '$2a$10$l4UA2SJnGJU5fRkR/bo4lenImEOHrUjw4e3wS8Ih14TUGuBPeqW9O', 'Benh Nhan 0180', '0910000180', NULL, TRUE, TRUE, TRUE),
(1181, 'patient0181', 'patient0181@evercare.test', '$2a$10$l4UA2SJnGJU5fRkR/bo4lenImEOHrUjw4e3wS8Ih14TUGuBPeqW9O', 'Benh Nhan 0181', '0910000181', NULL, TRUE, TRUE, TRUE),
(1182, 'patient0182', 'patient0182@evercare.test', '$2a$10$l4UA2SJnGJU5fRkR/bo4lenImEOHrUjw4e3wS8Ih14TUGuBPeqW9O', 'Benh Nhan 0182', '0910000182', NULL, TRUE, TRUE, TRUE),
(1183, 'patient0183', 'patient0183@evercare.test', '$2a$10$l4UA2SJnGJU5fRkR/bo4lenImEOHrUjw4e3wS8Ih14TUGuBPeqW9O', 'Benh Nhan 0183', '0910000183', NULL, TRUE, TRUE, TRUE),
(1184, 'patient0184', 'patient0184@evercare.test', '$2a$10$l4UA2SJnGJU5fRkR/bo4lenImEOHrUjw4e3wS8Ih14TUGuBPeqW9O', 'Benh Nhan 0184', '0910000184', NULL, TRUE, TRUE, TRUE),
(1185, 'patient0185', 'patient0185@evercare.test', '$2a$10$l4UA2SJnGJU5fRkR/bo4lenImEOHrUjw4e3wS8Ih14TUGuBPeqW9O', 'Benh Nhan 0185', '0910000185', NULL, TRUE, TRUE, TRUE),
(1186, 'patient0186', 'patient0186@evercare.test', '$2a$10$l4UA2SJnGJU5fRkR/bo4lenImEOHrUjw4e3wS8Ih14TUGuBPeqW9O', 'Benh Nhan 0186', '0910000186', NULL, TRUE, TRUE, TRUE),
(1187, 'patient0187', 'patient0187@evercare.test', '$2a$10$l4UA2SJnGJU5fRkR/bo4lenImEOHrUjw4e3wS8Ih14TUGuBPeqW9O', 'Benh Nhan 0187', '0910000187', NULL, TRUE, TRUE, TRUE),
(1188, 'patient0188', 'patient0188@evercare.test', '$2a$10$l4UA2SJnGJU5fRkR/bo4lenImEOHrUjw4e3wS8Ih14TUGuBPeqW9O', 'Benh Nhan 0188', '0910000188', NULL, TRUE, TRUE, TRUE),
(1189, 'patient0189', 'patient0189@evercare.test', '$2a$10$l4UA2SJnGJU5fRkR/bo4lenImEOHrUjw4e3wS8Ih14TUGuBPeqW9O', 'Benh Nhan 0189', '0910000189', NULL, TRUE, TRUE, TRUE),
(1190, 'patient0190', 'patient0190@evercare.test', '$2a$10$l4UA2SJnGJU5fRkR/bo4lenImEOHrUjw4e3wS8Ih14TUGuBPeqW9O', 'Benh Nhan 0190', '0910000190', NULL, TRUE, TRUE, TRUE),
(1191, 'patient0191', 'patient0191@evercare.test', '$2a$10$l4UA2SJnGJU5fRkR/bo4lenImEOHrUjw4e3wS8Ih14TUGuBPeqW9O', 'Benh Nhan 0191', '0910000191', NULL, TRUE, TRUE, TRUE),
(1192, 'patient0192', 'patient0192@evercare.test', '$2a$10$l4UA2SJnGJU5fRkR/bo4lenImEOHrUjw4e3wS8Ih14TUGuBPeqW9O', 'Benh Nhan 0192', '0910000192', NULL, TRUE, TRUE, TRUE),
(1193, 'patient0193', 'patient0193@evercare.test', '$2a$10$l4UA2SJnGJU5fRkR/bo4lenImEOHrUjw4e3wS8Ih14TUGuBPeqW9O', 'Benh Nhan 0193', '0910000193', NULL, TRUE, TRUE, TRUE),
(1194, 'patient0194', 'patient0194@evercare.test', '$2a$10$l4UA2SJnGJU5fRkR/bo4lenImEOHrUjw4e3wS8Ih14TUGuBPeqW9O', 'Benh Nhan 0194', '0910000194', NULL, TRUE, TRUE, TRUE),
(1195, 'patient0195', 'patient0195@evercare.test', '$2a$10$l4UA2SJnGJU5fRkR/bo4lenImEOHrUjw4e3wS8Ih14TUGuBPeqW9O', 'Benh Nhan 0195', '0910000195', NULL, TRUE, TRUE, TRUE),
(1196, 'patient0196', 'patient0196@evercare.test', '$2a$10$l4UA2SJnGJU5fRkR/bo4lenImEOHrUjw4e3wS8Ih14TUGuBPeqW9O', 'Benh Nhan 0196', '0910000196', NULL, TRUE, TRUE, TRUE),
(1197, 'patient0197', 'patient0197@evercare.test', '$2a$10$l4UA2SJnGJU5fRkR/bo4lenImEOHrUjw4e3wS8Ih14TUGuBPeqW9O', 'Benh Nhan 0197', '0910000197', NULL, TRUE, TRUE, TRUE),
(1198, 'patient0198', 'patient0198@evercare.test', '$2a$10$l4UA2SJnGJU5fRkR/bo4lenImEOHrUjw4e3wS8Ih14TUGuBPeqW9O', 'Benh Nhan 0198', '0910000198', NULL, TRUE, TRUE, TRUE),
(1199, 'patient0199', 'patient0199@evercare.test', '$2a$10$l4UA2SJnGJU5fRkR/bo4lenImEOHrUjw4e3wS8Ih14TUGuBPeqW9O', 'Benh Nhan 0199', '0910000199', NULL, TRUE, TRUE, TRUE),
(1200, 'patient0200', 'patient0200@evercare.test', '$2a$10$l4UA2SJnGJU5fRkR/bo4lenImEOHrUjw4e3wS8Ih14TUGuBPeqW9O', 'Benh Nhan 0200', '0910000200', NULL, TRUE, TRUE, TRUE),
(1201, 'patient0201', 'patient0201@evercare.test', '$2a$10$l4UA2SJnGJU5fRkR/bo4lenImEOHrUjw4e3wS8Ih14TUGuBPeqW9O', 'Benh Nhan 0201', '0910000201', NULL, TRUE, TRUE, TRUE),
(1202, 'patient0202', 'patient0202@evercare.test', '$2a$10$l4UA2SJnGJU5fRkR/bo4lenImEOHrUjw4e3wS8Ih14TUGuBPeqW9O', 'Benh Nhan 0202', '0910000202', NULL, TRUE, TRUE, TRUE),
(1203, 'patient0203', 'patient0203@evercare.test', '$2a$10$l4UA2SJnGJU5fRkR/bo4lenImEOHrUjw4e3wS8Ih14TUGuBPeqW9O', 'Benh Nhan 0203', '0910000203', NULL, TRUE, TRUE, TRUE),
(1204, 'patient0204', 'patient0204@evercare.test', '$2a$10$l4UA2SJnGJU5fRkR/bo4lenImEOHrUjw4e3wS8Ih14TUGuBPeqW9O', 'Benh Nhan 0204', '0910000204', NULL, TRUE, TRUE, TRUE),
(1205, 'patient0205', 'patient0205@evercare.test', '$2a$10$l4UA2SJnGJU5fRkR/bo4lenImEOHrUjw4e3wS8Ih14TUGuBPeqW9O', 'Benh Nhan 0205', '0910000205', NULL, TRUE, TRUE, TRUE),
(1206, 'patient0206', 'patient0206@evercare.test', '$2a$10$l4UA2SJnGJU5fRkR/bo4lenImEOHrUjw4e3wS8Ih14TUGuBPeqW9O', 'Benh Nhan 0206', '0910000206', NULL, TRUE, TRUE, TRUE),
(1207, 'patient0207', 'patient0207@evercare.test', '$2a$10$l4UA2SJnGJU5fRkR/bo4lenImEOHrUjw4e3wS8Ih14TUGuBPeqW9O', 'Benh Nhan 0207', '0910000207', NULL, TRUE, TRUE, TRUE),
(1208, 'patient0208', 'patient0208@evercare.test', '$2a$10$l4UA2SJnGJU5fRkR/bo4lenImEOHrUjw4e3wS8Ih14TUGuBPeqW9O', 'Benh Nhan 0208', '0910000208', NULL, TRUE, TRUE, TRUE),
(1209, 'patient0209', 'patient0209@evercare.test', '$2a$10$l4UA2SJnGJU5fRkR/bo4lenImEOHrUjw4e3wS8Ih14TUGuBPeqW9O', 'Benh Nhan 0209', '0910000209', NULL, TRUE, TRUE, TRUE),
(1210, 'patient0210', 'patient0210@evercare.test', '$2a$10$l4UA2SJnGJU5fRkR/bo4lenImEOHrUjw4e3wS8Ih14TUGuBPeqW9O', 'Benh Nhan 0210', '0910000210', NULL, TRUE, TRUE, TRUE),
(1211, 'patient0211', 'patient0211@evercare.test', '$2a$10$l4UA2SJnGJU5fRkR/bo4lenImEOHrUjw4e3wS8Ih14TUGuBPeqW9O', 'Benh Nhan 0211', '0910000211', NULL, TRUE, TRUE, TRUE),
(1212, 'patient0212', 'patient0212@evercare.test', '$2a$10$l4UA2SJnGJU5fRkR/bo4lenImEOHrUjw4e3wS8Ih14TUGuBPeqW9O', 'Benh Nhan 0212', '0910000212', NULL, TRUE, TRUE, TRUE),
(1213, 'patient0213', 'patient0213@evercare.test', '$2a$10$l4UA2SJnGJU5fRkR/bo4lenImEOHrUjw4e3wS8Ih14TUGuBPeqW9O', 'Benh Nhan 0213', '0910000213', NULL, TRUE, TRUE, TRUE),
(1214, 'patient0214', 'patient0214@evercare.test', '$2a$10$l4UA2SJnGJU5fRkR/bo4lenImEOHrUjw4e3wS8Ih14TUGuBPeqW9O', 'Benh Nhan 0214', '0910000214', NULL, TRUE, TRUE, TRUE),
(1215, 'patient0215', 'patient0215@evercare.test', '$2a$10$l4UA2SJnGJU5fRkR/bo4lenImEOHrUjw4e3wS8Ih14TUGuBPeqW9O', 'Benh Nhan 0215', '0910000215', NULL, TRUE, TRUE, TRUE),
(1216, 'patient0216', 'patient0216@evercare.test', '$2a$10$l4UA2SJnGJU5fRkR/bo4lenImEOHrUjw4e3wS8Ih14TUGuBPeqW9O', 'Benh Nhan 0216', '0910000216', NULL, TRUE, TRUE, TRUE),
(1217, 'patient0217', 'patient0217@evercare.test', '$2a$10$l4UA2SJnGJU5fRkR/bo4lenImEOHrUjw4e3wS8Ih14TUGuBPeqW9O', 'Benh Nhan 0217', '0910000217', NULL, TRUE, TRUE, TRUE),
(1218, 'patient0218', 'patient0218@evercare.test', '$2a$10$l4UA2SJnGJU5fRkR/bo4lenImEOHrUjw4e3wS8Ih14TUGuBPeqW9O', 'Benh Nhan 0218', '0910000218', NULL, TRUE, TRUE, TRUE),
(1219, 'patient0219', 'patient0219@evercare.test', '$2a$10$l4UA2SJnGJU5fRkR/bo4lenImEOHrUjw4e3wS8Ih14TUGuBPeqW9O', 'Benh Nhan 0219', '0910000219', NULL, TRUE, TRUE, TRUE),
(1220, 'patient0220', 'patient0220@evercare.test', '$2a$10$l4UA2SJnGJU5fRkR/bo4lenImEOHrUjw4e3wS8Ih14TUGuBPeqW9O', 'Benh Nhan 0220', '0910000220', NULL, TRUE, TRUE, TRUE),
(1221, 'patient0221', 'patient0221@evercare.test', '$2a$10$l4UA2SJnGJU5fRkR/bo4lenImEOHrUjw4e3wS8Ih14TUGuBPeqW9O', 'Benh Nhan 0221', '0910000221', NULL, TRUE, TRUE, TRUE),
(1222, 'patient0222', 'patient0222@evercare.test', '$2a$10$l4UA2SJnGJU5fRkR/bo4lenImEOHrUjw4e3wS8Ih14TUGuBPeqW9O', 'Benh Nhan 0222', '0910000222', NULL, TRUE, TRUE, TRUE),
(1223, 'patient0223', 'patient0223@evercare.test', '$2a$10$l4UA2SJnGJU5fRkR/bo4lenImEOHrUjw4e3wS8Ih14TUGuBPeqW9O', 'Benh Nhan 0223', '0910000223', NULL, TRUE, TRUE, TRUE),
(1224, 'patient0224', 'patient0224@evercare.test', '$2a$10$l4UA2SJnGJU5fRkR/bo4lenImEOHrUjw4e3wS8Ih14TUGuBPeqW9O', 'Benh Nhan 0224', '0910000224', NULL, TRUE, TRUE, TRUE),
(1225, 'patient0225', 'patient0225@evercare.test', '$2a$10$l4UA2SJnGJU5fRkR/bo4lenImEOHrUjw4e3wS8Ih14TUGuBPeqW9O', 'Benh Nhan 0225', '0910000225', NULL, TRUE, TRUE, TRUE),
(1226, 'patient0226', 'patient0226@evercare.test', '$2a$10$l4UA2SJnGJU5fRkR/bo4lenImEOHrUjw4e3wS8Ih14TUGuBPeqW9O', 'Benh Nhan 0226', '0910000226', NULL, TRUE, TRUE, TRUE),
(1227, 'patient0227', 'patient0227@evercare.test', '$2a$10$l4UA2SJnGJU5fRkR/bo4lenImEOHrUjw4e3wS8Ih14TUGuBPeqW9O', 'Benh Nhan 0227', '0910000227', NULL, TRUE, TRUE, TRUE),
(1228, 'patient0228', 'patient0228@evercare.test', '$2a$10$l4UA2SJnGJU5fRkR/bo4lenImEOHrUjw4e3wS8Ih14TUGuBPeqW9O', 'Benh Nhan 0228', '0910000228', NULL, TRUE, TRUE, TRUE),
(1229, 'patient0229', 'patient0229@evercare.test', '$2a$10$l4UA2SJnGJU5fRkR/bo4lenImEOHrUjw4e3wS8Ih14TUGuBPeqW9O', 'Benh Nhan 0229', '0910000229', NULL, TRUE, TRUE, TRUE),
(1230, 'patient0230', 'patient0230@evercare.test', '$2a$10$l4UA2SJnGJU5fRkR/bo4lenImEOHrUjw4e3wS8Ih14TUGuBPeqW9O', 'Benh Nhan 0230', '0910000230', NULL, TRUE, TRUE, TRUE),
(1231, 'patient0231', 'patient0231@evercare.test', '$2a$10$l4UA2SJnGJU5fRkR/bo4lenImEOHrUjw4e3wS8Ih14TUGuBPeqW9O', 'Benh Nhan 0231', '0910000231', NULL, TRUE, TRUE, TRUE),
(1232, 'patient0232', 'patient0232@evercare.test', '$2a$10$l4UA2SJnGJU5fRkR/bo4lenImEOHrUjw4e3wS8Ih14TUGuBPeqW9O', 'Benh Nhan 0232', '0910000232', NULL, TRUE, TRUE, TRUE),
(1233, 'patient0233', 'patient0233@evercare.test', '$2a$10$l4UA2SJnGJU5fRkR/bo4lenImEOHrUjw4e3wS8Ih14TUGuBPeqW9O', 'Benh Nhan 0233', '0910000233', NULL, TRUE, TRUE, TRUE),
(1234, 'patient0234', 'patient0234@evercare.test', '$2a$10$l4UA2SJnGJU5fRkR/bo4lenImEOHrUjw4e3wS8Ih14TUGuBPeqW9O', 'Benh Nhan 0234', '0910000234', NULL, TRUE, TRUE, TRUE),
(1235, 'patient0235', 'patient0235@evercare.test', '$2a$10$l4UA2SJnGJU5fRkR/bo4lenImEOHrUjw4e3wS8Ih14TUGuBPeqW9O', 'Benh Nhan 0235', '0910000235', NULL, TRUE, TRUE, TRUE),
(1236, 'patient0236', 'patient0236@evercare.test', '$2a$10$l4UA2SJnGJU5fRkR/bo4lenImEOHrUjw4e3wS8Ih14TUGuBPeqW9O', 'Benh Nhan 0236', '0910000236', NULL, TRUE, TRUE, TRUE),
(1237, 'patient0237', 'patient0237@evercare.test', '$2a$10$l4UA2SJnGJU5fRkR/bo4lenImEOHrUjw4e3wS8Ih14TUGuBPeqW9O', 'Benh Nhan 0237', '0910000237', NULL, TRUE, TRUE, TRUE),
(1238, 'patient0238', 'patient0238@evercare.test', '$2a$10$l4UA2SJnGJU5fRkR/bo4lenImEOHrUjw4e3wS8Ih14TUGuBPeqW9O', 'Benh Nhan 0238', '0910000238', NULL, TRUE, TRUE, TRUE),
(1239, 'patient0239', 'patient0239@evercare.test', '$2a$10$l4UA2SJnGJU5fRkR/bo4lenImEOHrUjw4e3wS8Ih14TUGuBPeqW9O', 'Benh Nhan 0239', '0910000239', NULL, TRUE, TRUE, TRUE),
(1240, 'patient0240', 'patient0240@evercare.test', '$2a$10$l4UA2SJnGJU5fRkR/bo4lenImEOHrUjw4e3wS8Ih14TUGuBPeqW9O', 'Benh Nhan 0240', '0910000240', NULL, TRUE, TRUE, TRUE),
(1241, 'patient0241', 'patient0241@evercare.test', '$2a$10$l4UA2SJnGJU5fRkR/bo4lenImEOHrUjw4e3wS8Ih14TUGuBPeqW9O', 'Benh Nhan 0241', '0910000241', NULL, TRUE, TRUE, TRUE),
(1242, 'patient0242', 'patient0242@evercare.test', '$2a$10$l4UA2SJnGJU5fRkR/bo4lenImEOHrUjw4e3wS8Ih14TUGuBPeqW9O', 'Benh Nhan 0242', '0910000242', NULL, TRUE, TRUE, TRUE),
(1243, 'patient0243', 'patient0243@evercare.test', '$2a$10$l4UA2SJnGJU5fRkR/bo4lenImEOHrUjw4e3wS8Ih14TUGuBPeqW9O', 'Benh Nhan 0243', '0910000243', NULL, TRUE, TRUE, TRUE),
(1244, 'patient0244', 'patient0244@evercare.test', '$2a$10$l4UA2SJnGJU5fRkR/bo4lenImEOHrUjw4e3wS8Ih14TUGuBPeqW9O', 'Benh Nhan 0244', '0910000244', NULL, TRUE, TRUE, TRUE),
(1245, 'patient0245', 'patient0245@evercare.test', '$2a$10$l4UA2SJnGJU5fRkR/bo4lenImEOHrUjw4e3wS8Ih14TUGuBPeqW9O', 'Benh Nhan 0245', '0910000245', NULL, TRUE, TRUE, TRUE),
(1246, 'patient0246', 'patient0246@evercare.test', '$2a$10$l4UA2SJnGJU5fRkR/bo4lenImEOHrUjw4e3wS8Ih14TUGuBPeqW9O', 'Benh Nhan 0246', '0910000246', NULL, TRUE, TRUE, TRUE),
(1247, 'patient0247', 'patient0247@evercare.test', '$2a$10$l4UA2SJnGJU5fRkR/bo4lenImEOHrUjw4e3wS8Ih14TUGuBPeqW9O', 'Benh Nhan 0247', '0910000247', NULL, TRUE, TRUE, TRUE),
(1248, 'patient0248', 'patient0248@evercare.test', '$2a$10$l4UA2SJnGJU5fRkR/bo4lenImEOHrUjw4e3wS8Ih14TUGuBPeqW9O', 'Benh Nhan 0248', '0910000248', NULL, TRUE, TRUE, TRUE),
(1249, 'patient0249', 'patient0249@evercare.test', '$2a$10$l4UA2SJnGJU5fRkR/bo4lenImEOHrUjw4e3wS8Ih14TUGuBPeqW9O', 'Benh Nhan 0249', '0910000249', NULL, TRUE, TRUE, TRUE),
(1250, 'patient0250', 'patient0250@evercare.test', '$2a$10$l4UA2SJnGJU5fRkR/bo4lenImEOHrUjw4e3wS8Ih14TUGuBPeqW9O', 'Benh Nhan 0250', '0910000250', NULL, TRUE, TRUE, TRUE);
INSERT IGNORE INTO user_role(user_id, role_id) VALUES
(1,1),
(101,3),
(102,3),
(103,3),
(104,3),
(105,3),
(106,3),
(107,3),
(108,3),
(109,3),
(110,3);
INSERT IGNORE INTO user_role(user_id, role_id) SELECT 201, id FROM role WHERE code = 'ROLE_RECEPTIONIST';
INSERT IGNORE INTO user_role(user_id, role_id) SELECT 202, id FROM role WHERE code = 'ROLE_CASHIER';
INSERT IGNORE INTO user_role(user_id, role_id) SELECT 203, id FROM role WHERE code = 'ROLE_PHARMACIST';
INSERT IGNORE INTO user_role(user_id, role_id) SELECT 204, id FROM role WHERE code = 'ROLE_LAB_TECH';
INSERT IGNORE INTO user_role(user_id, role_id) SELECT 205, id FROM role WHERE code = 'ROLE_MANAGER';
INSERT IGNORE INTO user_role(user_id, role_id) SELECT 206, id FROM role WHERE code = 'ROLE_CASHIER';
INSERT IGNORE INTO user_role(user_id, role_id) SELECT 207, id FROM role WHERE code = 'ROLE_PHARMACIST';
INSERT IGNORE INTO user_role(user_id, role_id) SELECT 208, id FROM role WHERE code = 'ROLE_RECEPTIONIST';
INSERT IGNORE INTO user_role(user_id, role_id) SELECT 209, id FROM role WHERE code = 'ROLE_LAB_TECH';
INSERT IGNORE INTO user_role(user_id, role_id) SELECT 210, id FROM role WHERE code = 'ROLE_MANAGER';
INSERT IGNORE INTO user_role(user_id, role_id) SELECT 1001, id FROM role WHERE code = 'ROLE_PATIENT';
INSERT IGNORE INTO user_role(user_id, role_id) SELECT 1002, id FROM role WHERE code = 'ROLE_PATIENT';
INSERT IGNORE INTO user_role(user_id, role_id) SELECT 1003, id FROM role WHERE code = 'ROLE_PATIENT';
INSERT IGNORE INTO user_role(user_id, role_id) SELECT 1004, id FROM role WHERE code = 'ROLE_PATIENT';
INSERT IGNORE INTO user_role(user_id, role_id) SELECT 1005, id FROM role WHERE code = 'ROLE_PATIENT';
INSERT IGNORE INTO user_role(user_id, role_id) SELECT 1006, id FROM role WHERE code = 'ROLE_PATIENT';
INSERT IGNORE INTO user_role(user_id, role_id) SELECT 1007, id FROM role WHERE code = 'ROLE_PATIENT';
INSERT IGNORE INTO user_role(user_id, role_id) SELECT 1008, id FROM role WHERE code = 'ROLE_PATIENT';
INSERT IGNORE INTO user_role(user_id, role_id) SELECT 1009, id FROM role WHERE code = 'ROLE_PATIENT';
INSERT IGNORE INTO user_role(user_id, role_id) SELECT 1010, id FROM role WHERE code = 'ROLE_PATIENT';
INSERT IGNORE INTO user_role(user_id, role_id) SELECT 1011, id FROM role WHERE code = 'ROLE_PATIENT';
INSERT IGNORE INTO user_role(user_id, role_id) SELECT 1012, id FROM role WHERE code = 'ROLE_PATIENT';
INSERT IGNORE INTO user_role(user_id, role_id) SELECT 1013, id FROM role WHERE code = 'ROLE_PATIENT';
INSERT IGNORE INTO user_role(user_id, role_id) SELECT 1014, id FROM role WHERE code = 'ROLE_PATIENT';
INSERT IGNORE INTO user_role(user_id, role_id) SELECT 1015, id FROM role WHERE code = 'ROLE_PATIENT';
INSERT IGNORE INTO user_role(user_id, role_id) SELECT 1016, id FROM role WHERE code = 'ROLE_PATIENT';
INSERT IGNORE INTO user_role(user_id, role_id) SELECT 1017, id FROM role WHERE code = 'ROLE_PATIENT';
INSERT IGNORE INTO user_role(user_id, role_id) SELECT 1018, id FROM role WHERE code = 'ROLE_PATIENT';
INSERT IGNORE INTO user_role(user_id, role_id) SELECT 1019, id FROM role WHERE code = 'ROLE_PATIENT';
INSERT IGNORE INTO user_role(user_id, role_id) SELECT 1020, id FROM role WHERE code = 'ROLE_PATIENT';
INSERT IGNORE INTO user_role(user_id, role_id) SELECT 1021, id FROM role WHERE code = 'ROLE_PATIENT';
INSERT IGNORE INTO user_role(user_id, role_id) SELECT 1022, id FROM role WHERE code = 'ROLE_PATIENT';
INSERT IGNORE INTO user_role(user_id, role_id) SELECT 1023, id FROM role WHERE code = 'ROLE_PATIENT';
INSERT IGNORE INTO user_role(user_id, role_id) SELECT 1024, id FROM role WHERE code = 'ROLE_PATIENT';
INSERT IGNORE INTO user_role(user_id, role_id) SELECT 1025, id FROM role WHERE code = 'ROLE_PATIENT';
INSERT IGNORE INTO user_role(user_id, role_id) SELECT 1026, id FROM role WHERE code = 'ROLE_PATIENT';
INSERT IGNORE INTO user_role(user_id, role_id) SELECT 1027, id FROM role WHERE code = 'ROLE_PATIENT';
INSERT IGNORE INTO user_role(user_id, role_id) SELECT 1028, id FROM role WHERE code = 'ROLE_PATIENT';
INSERT IGNORE INTO user_role(user_id, role_id) SELECT 1029, id FROM role WHERE code = 'ROLE_PATIENT';
INSERT IGNORE INTO user_role(user_id, role_id) SELECT 1030, id FROM role WHERE code = 'ROLE_PATIENT';
INSERT IGNORE INTO user_role(user_id, role_id) SELECT 1031, id FROM role WHERE code = 'ROLE_PATIENT';
INSERT IGNORE INTO user_role(user_id, role_id) SELECT 1032, id FROM role WHERE code = 'ROLE_PATIENT';
INSERT IGNORE INTO user_role(user_id, role_id) SELECT 1033, id FROM role WHERE code = 'ROLE_PATIENT';
INSERT IGNORE INTO user_role(user_id, role_id) SELECT 1034, id FROM role WHERE code = 'ROLE_PATIENT';
INSERT IGNORE INTO user_role(user_id, role_id) SELECT 1035, id FROM role WHERE code = 'ROLE_PATIENT';
INSERT IGNORE INTO user_role(user_id, role_id) SELECT 1036, id FROM role WHERE code = 'ROLE_PATIENT';
INSERT IGNORE INTO user_role(user_id, role_id) SELECT 1037, id FROM role WHERE code = 'ROLE_PATIENT';
INSERT IGNORE INTO user_role(user_id, role_id) SELECT 1038, id FROM role WHERE code = 'ROLE_PATIENT';
INSERT IGNORE INTO user_role(user_id, role_id) SELECT 1039, id FROM role WHERE code = 'ROLE_PATIENT';
INSERT IGNORE INTO user_role(user_id, role_id) SELECT 1040, id FROM role WHERE code = 'ROLE_PATIENT';
INSERT IGNORE INTO user_role(user_id, role_id) SELECT 1041, id FROM role WHERE code = 'ROLE_PATIENT';
INSERT IGNORE INTO user_role(user_id, role_id) SELECT 1042, id FROM role WHERE code = 'ROLE_PATIENT';
INSERT IGNORE INTO user_role(user_id, role_id) SELECT 1043, id FROM role WHERE code = 'ROLE_PATIENT';
INSERT IGNORE INTO user_role(user_id, role_id) SELECT 1044, id FROM role WHERE code = 'ROLE_PATIENT';
INSERT IGNORE INTO user_role(user_id, role_id) SELECT 1045, id FROM role WHERE code = 'ROLE_PATIENT';
INSERT IGNORE INTO user_role(user_id, role_id) SELECT 1046, id FROM role WHERE code = 'ROLE_PATIENT';
INSERT IGNORE INTO user_role(user_id, role_id) SELECT 1047, id FROM role WHERE code = 'ROLE_PATIENT';
INSERT IGNORE INTO user_role(user_id, role_id) SELECT 1048, id FROM role WHERE code = 'ROLE_PATIENT';
INSERT IGNORE INTO user_role(user_id, role_id) SELECT 1049, id FROM role WHERE code = 'ROLE_PATIENT';
INSERT IGNORE INTO user_role(user_id, role_id) SELECT 1050, id FROM role WHERE code = 'ROLE_PATIENT';
INSERT IGNORE INTO user_role(user_id, role_id) SELECT 1051, id FROM role WHERE code = 'ROLE_PATIENT';
INSERT IGNORE INTO user_role(user_id, role_id) SELECT 1052, id FROM role WHERE code = 'ROLE_PATIENT';
INSERT IGNORE INTO user_role(user_id, role_id) SELECT 1053, id FROM role WHERE code = 'ROLE_PATIENT';
INSERT IGNORE INTO user_role(user_id, role_id) SELECT 1054, id FROM role WHERE code = 'ROLE_PATIENT';
INSERT IGNORE INTO user_role(user_id, role_id) SELECT 1055, id FROM role WHERE code = 'ROLE_PATIENT';
INSERT IGNORE INTO user_role(user_id, role_id) SELECT 1056, id FROM role WHERE code = 'ROLE_PATIENT';
INSERT IGNORE INTO user_role(user_id, role_id) SELECT 1057, id FROM role WHERE code = 'ROLE_PATIENT';
INSERT IGNORE INTO user_role(user_id, role_id) SELECT 1058, id FROM role WHERE code = 'ROLE_PATIENT';
INSERT IGNORE INTO user_role(user_id, role_id) SELECT 1059, id FROM role WHERE code = 'ROLE_PATIENT';
INSERT IGNORE INTO user_role(user_id, role_id) SELECT 1060, id FROM role WHERE code = 'ROLE_PATIENT';
INSERT IGNORE INTO user_role(user_id, role_id) SELECT 1061, id FROM role WHERE code = 'ROLE_PATIENT';
INSERT IGNORE INTO user_role(user_id, role_id) SELECT 1062, id FROM role WHERE code = 'ROLE_PATIENT';
INSERT IGNORE INTO user_role(user_id, role_id) SELECT 1063, id FROM role WHERE code = 'ROLE_PATIENT';
INSERT IGNORE INTO user_role(user_id, role_id) SELECT 1064, id FROM role WHERE code = 'ROLE_PATIENT';
INSERT IGNORE INTO user_role(user_id, role_id) SELECT 1065, id FROM role WHERE code = 'ROLE_PATIENT';
INSERT IGNORE INTO user_role(user_id, role_id) SELECT 1066, id FROM role WHERE code = 'ROLE_PATIENT';
INSERT IGNORE INTO user_role(user_id, role_id) SELECT 1067, id FROM role WHERE code = 'ROLE_PATIENT';
INSERT IGNORE INTO user_role(user_id, role_id) SELECT 1068, id FROM role WHERE code = 'ROLE_PATIENT';
INSERT IGNORE INTO user_role(user_id, role_id) SELECT 1069, id FROM role WHERE code = 'ROLE_PATIENT';
INSERT IGNORE INTO user_role(user_id, role_id) SELECT 1070, id FROM role WHERE code = 'ROLE_PATIENT';
INSERT IGNORE INTO user_role(user_id, role_id) SELECT 1071, id FROM role WHERE code = 'ROLE_PATIENT';
INSERT IGNORE INTO user_role(user_id, role_id) SELECT 1072, id FROM role WHERE code = 'ROLE_PATIENT';
INSERT IGNORE INTO user_role(user_id, role_id) SELECT 1073, id FROM role WHERE code = 'ROLE_PATIENT';
INSERT IGNORE INTO user_role(user_id, role_id) SELECT 1074, id FROM role WHERE code = 'ROLE_PATIENT';
INSERT IGNORE INTO user_role(user_id, role_id) SELECT 1075, id FROM role WHERE code = 'ROLE_PATIENT';
INSERT IGNORE INTO user_role(user_id, role_id) SELECT 1076, id FROM role WHERE code = 'ROLE_PATIENT';
INSERT IGNORE INTO user_role(user_id, role_id) SELECT 1077, id FROM role WHERE code = 'ROLE_PATIENT';
INSERT IGNORE INTO user_role(user_id, role_id) SELECT 1078, id FROM role WHERE code = 'ROLE_PATIENT';
INSERT IGNORE INTO user_role(user_id, role_id) SELECT 1079, id FROM role WHERE code = 'ROLE_PATIENT';
INSERT IGNORE INTO user_role(user_id, role_id) SELECT 1080, id FROM role WHERE code = 'ROLE_PATIENT';
INSERT IGNORE INTO user_role(user_id, role_id) SELECT 1081, id FROM role WHERE code = 'ROLE_PATIENT';
INSERT IGNORE INTO user_role(user_id, role_id) SELECT 1082, id FROM role WHERE code = 'ROLE_PATIENT';
INSERT IGNORE INTO user_role(user_id, role_id) SELECT 1083, id FROM role WHERE code = 'ROLE_PATIENT';
INSERT IGNORE INTO user_role(user_id, role_id) SELECT 1084, id FROM role WHERE code = 'ROLE_PATIENT';
INSERT IGNORE INTO user_role(user_id, role_id) SELECT 1085, id FROM role WHERE code = 'ROLE_PATIENT';
INSERT IGNORE INTO user_role(user_id, role_id) SELECT 1086, id FROM role WHERE code = 'ROLE_PATIENT';
INSERT IGNORE INTO user_role(user_id, role_id) SELECT 1087, id FROM role WHERE code = 'ROLE_PATIENT';
INSERT IGNORE INTO user_role(user_id, role_id) SELECT 1088, id FROM role WHERE code = 'ROLE_PATIENT';
INSERT IGNORE INTO user_role(user_id, role_id) SELECT 1089, id FROM role WHERE code = 'ROLE_PATIENT';
INSERT IGNORE INTO user_role(user_id, role_id) SELECT 1090, id FROM role WHERE code = 'ROLE_PATIENT';
INSERT IGNORE INTO user_role(user_id, role_id) SELECT 1091, id FROM role WHERE code = 'ROLE_PATIENT';
INSERT IGNORE INTO user_role(user_id, role_id) SELECT 1092, id FROM role WHERE code = 'ROLE_PATIENT';
INSERT IGNORE INTO user_role(user_id, role_id) SELECT 1093, id FROM role WHERE code = 'ROLE_PATIENT';
INSERT IGNORE INTO user_role(user_id, role_id) SELECT 1094, id FROM role WHERE code = 'ROLE_PATIENT';
INSERT IGNORE INTO user_role(user_id, role_id) SELECT 1095, id FROM role WHERE code = 'ROLE_PATIENT';
INSERT IGNORE INTO user_role(user_id, role_id) SELECT 1096, id FROM role WHERE code = 'ROLE_PATIENT';
INSERT IGNORE INTO user_role(user_id, role_id) SELECT 1097, id FROM role WHERE code = 'ROLE_PATIENT';
INSERT IGNORE INTO user_role(user_id, role_id) SELECT 1098, id FROM role WHERE code = 'ROLE_PATIENT';
INSERT IGNORE INTO user_role(user_id, role_id) SELECT 1099, id FROM role WHERE code = 'ROLE_PATIENT';
INSERT IGNORE INTO user_role(user_id, role_id) SELECT 1100, id FROM role WHERE code = 'ROLE_PATIENT';
INSERT IGNORE INTO user_role(user_id, role_id) SELECT 1101, id FROM role WHERE code = 'ROLE_PATIENT';
INSERT IGNORE INTO user_role(user_id, role_id) SELECT 1102, id FROM role WHERE code = 'ROLE_PATIENT';
INSERT IGNORE INTO user_role(user_id, role_id) SELECT 1103, id FROM role WHERE code = 'ROLE_PATIENT';
INSERT IGNORE INTO user_role(user_id, role_id) SELECT 1104, id FROM role WHERE code = 'ROLE_PATIENT';
INSERT IGNORE INTO user_role(user_id, role_id) SELECT 1105, id FROM role WHERE code = 'ROLE_PATIENT';
INSERT IGNORE INTO user_role(user_id, role_id) SELECT 1106, id FROM role WHERE code = 'ROLE_PATIENT';
INSERT IGNORE INTO user_role(user_id, role_id) SELECT 1107, id FROM role WHERE code = 'ROLE_PATIENT';
INSERT IGNORE INTO user_role(user_id, role_id) SELECT 1108, id FROM role WHERE code = 'ROLE_PATIENT';
INSERT IGNORE INTO user_role(user_id, role_id) SELECT 1109, id FROM role WHERE code = 'ROLE_PATIENT';
INSERT IGNORE INTO user_role(user_id, role_id) SELECT 1110, id FROM role WHERE code = 'ROLE_PATIENT';
INSERT IGNORE INTO user_role(user_id, role_id) SELECT 1111, id FROM role WHERE code = 'ROLE_PATIENT';
INSERT IGNORE INTO user_role(user_id, role_id) SELECT 1112, id FROM role WHERE code = 'ROLE_PATIENT';
INSERT IGNORE INTO user_role(user_id, role_id) SELECT 1113, id FROM role WHERE code = 'ROLE_PATIENT';
INSERT IGNORE INTO user_role(user_id, role_id) SELECT 1114, id FROM role WHERE code = 'ROLE_PATIENT';
INSERT IGNORE INTO user_role(user_id, role_id) SELECT 1115, id FROM role WHERE code = 'ROLE_PATIENT';
INSERT IGNORE INTO user_role(user_id, role_id) SELECT 1116, id FROM role WHERE code = 'ROLE_PATIENT';
INSERT IGNORE INTO user_role(user_id, role_id) SELECT 1117, id FROM role WHERE code = 'ROLE_PATIENT';
INSERT IGNORE INTO user_role(user_id, role_id) SELECT 1118, id FROM role WHERE code = 'ROLE_PATIENT';
INSERT IGNORE INTO user_role(user_id, role_id) SELECT 1119, id FROM role WHERE code = 'ROLE_PATIENT';
INSERT IGNORE INTO user_role(user_id, role_id) SELECT 1120, id FROM role WHERE code = 'ROLE_PATIENT';
INSERT IGNORE INTO user_role(user_id, role_id) SELECT 1121, id FROM role WHERE code = 'ROLE_PATIENT';
INSERT IGNORE INTO user_role(user_id, role_id) SELECT 1122, id FROM role WHERE code = 'ROLE_PATIENT';
INSERT IGNORE INTO user_role(user_id, role_id) SELECT 1123, id FROM role WHERE code = 'ROLE_PATIENT';
INSERT IGNORE INTO user_role(user_id, role_id) SELECT 1124, id FROM role WHERE code = 'ROLE_PATIENT';
INSERT IGNORE INTO user_role(user_id, role_id) SELECT 1125, id FROM role WHERE code = 'ROLE_PATIENT';
INSERT IGNORE INTO user_role(user_id, role_id) SELECT 1126, id FROM role WHERE code = 'ROLE_PATIENT';
INSERT IGNORE INTO user_role(user_id, role_id) SELECT 1127, id FROM role WHERE code = 'ROLE_PATIENT';
INSERT IGNORE INTO user_role(user_id, role_id) SELECT 1128, id FROM role WHERE code = 'ROLE_PATIENT';
INSERT IGNORE INTO user_role(user_id, role_id) SELECT 1129, id FROM role WHERE code = 'ROLE_PATIENT';
INSERT IGNORE INTO user_role(user_id, role_id) SELECT 1130, id FROM role WHERE code = 'ROLE_PATIENT';
INSERT IGNORE INTO user_role(user_id, role_id) SELECT 1131, id FROM role WHERE code = 'ROLE_PATIENT';
INSERT IGNORE INTO user_role(user_id, role_id) SELECT 1132, id FROM role WHERE code = 'ROLE_PATIENT';
INSERT IGNORE INTO user_role(user_id, role_id) SELECT 1133, id FROM role WHERE code = 'ROLE_PATIENT';
INSERT IGNORE INTO user_role(user_id, role_id) SELECT 1134, id FROM role WHERE code = 'ROLE_PATIENT';
INSERT IGNORE INTO user_role(user_id, role_id) SELECT 1135, id FROM role WHERE code = 'ROLE_PATIENT';
INSERT IGNORE INTO user_role(user_id, role_id) SELECT 1136, id FROM role WHERE code = 'ROLE_PATIENT';
INSERT IGNORE INTO user_role(user_id, role_id) SELECT 1137, id FROM role WHERE code = 'ROLE_PATIENT';
INSERT IGNORE INTO user_role(user_id, role_id) SELECT 1138, id FROM role WHERE code = 'ROLE_PATIENT';
INSERT IGNORE INTO user_role(user_id, role_id) SELECT 1139, id FROM role WHERE code = 'ROLE_PATIENT';
INSERT IGNORE INTO user_role(user_id, role_id) SELECT 1140, id FROM role WHERE code = 'ROLE_PATIENT';
INSERT IGNORE INTO user_role(user_id, role_id) SELECT 1141, id FROM role WHERE code = 'ROLE_PATIENT';
INSERT IGNORE INTO user_role(user_id, role_id) SELECT 1142, id FROM role WHERE code = 'ROLE_PATIENT';
INSERT IGNORE INTO user_role(user_id, role_id) SELECT 1143, id FROM role WHERE code = 'ROLE_PATIENT';
INSERT IGNORE INTO user_role(user_id, role_id) SELECT 1144, id FROM role WHERE code = 'ROLE_PATIENT';
INSERT IGNORE INTO user_role(user_id, role_id) SELECT 1145, id FROM role WHERE code = 'ROLE_PATIENT';
INSERT IGNORE INTO user_role(user_id, role_id) SELECT 1146, id FROM role WHERE code = 'ROLE_PATIENT';
INSERT IGNORE INTO user_role(user_id, role_id) SELECT 1147, id FROM role WHERE code = 'ROLE_PATIENT';
INSERT IGNORE INTO user_role(user_id, role_id) SELECT 1148, id FROM role WHERE code = 'ROLE_PATIENT';
INSERT IGNORE INTO user_role(user_id, role_id) SELECT 1149, id FROM role WHERE code = 'ROLE_PATIENT';
INSERT IGNORE INTO user_role(user_id, role_id) SELECT 1150, id FROM role WHERE code = 'ROLE_PATIENT';
INSERT IGNORE INTO user_role(user_id, role_id) SELECT 1151, id FROM role WHERE code = 'ROLE_PATIENT';
INSERT IGNORE INTO user_role(user_id, role_id) SELECT 1152, id FROM role WHERE code = 'ROLE_PATIENT';
INSERT IGNORE INTO user_role(user_id, role_id) SELECT 1153, id FROM role WHERE code = 'ROLE_PATIENT';
INSERT IGNORE INTO user_role(user_id, role_id) SELECT 1154, id FROM role WHERE code = 'ROLE_PATIENT';
INSERT IGNORE INTO user_role(user_id, role_id) SELECT 1155, id FROM role WHERE code = 'ROLE_PATIENT';
INSERT IGNORE INTO user_role(user_id, role_id) SELECT 1156, id FROM role WHERE code = 'ROLE_PATIENT';
INSERT IGNORE INTO user_role(user_id, role_id) SELECT 1157, id FROM role WHERE code = 'ROLE_PATIENT';
INSERT IGNORE INTO user_role(user_id, role_id) SELECT 1158, id FROM role WHERE code = 'ROLE_PATIENT';
INSERT IGNORE INTO user_role(user_id, role_id) SELECT 1159, id FROM role WHERE code = 'ROLE_PATIENT';
INSERT IGNORE INTO user_role(user_id, role_id) SELECT 1160, id FROM role WHERE code = 'ROLE_PATIENT';
INSERT IGNORE INTO user_role(user_id, role_id) SELECT 1161, id FROM role WHERE code = 'ROLE_PATIENT';
INSERT IGNORE INTO user_role(user_id, role_id) SELECT 1162, id FROM role WHERE code = 'ROLE_PATIENT';
INSERT IGNORE INTO user_role(user_id, role_id) SELECT 1163, id FROM role WHERE code = 'ROLE_PATIENT';
INSERT IGNORE INTO user_role(user_id, role_id) SELECT 1164, id FROM role WHERE code = 'ROLE_PATIENT';
INSERT IGNORE INTO user_role(user_id, role_id) SELECT 1165, id FROM role WHERE code = 'ROLE_PATIENT';
INSERT IGNORE INTO user_role(user_id, role_id) SELECT 1166, id FROM role WHERE code = 'ROLE_PATIENT';
INSERT IGNORE INTO user_role(user_id, role_id) SELECT 1167, id FROM role WHERE code = 'ROLE_PATIENT';
INSERT IGNORE INTO user_role(user_id, role_id) SELECT 1168, id FROM role WHERE code = 'ROLE_PATIENT';
INSERT IGNORE INTO user_role(user_id, role_id) SELECT 1169, id FROM role WHERE code = 'ROLE_PATIENT';
INSERT IGNORE INTO user_role(user_id, role_id) SELECT 1170, id FROM role WHERE code = 'ROLE_PATIENT';
INSERT IGNORE INTO user_role(user_id, role_id) SELECT 1171, id FROM role WHERE code = 'ROLE_PATIENT';
INSERT IGNORE INTO user_role(user_id, role_id) SELECT 1172, id FROM role WHERE code = 'ROLE_PATIENT';
INSERT IGNORE INTO user_role(user_id, role_id) SELECT 1173, id FROM role WHERE code = 'ROLE_PATIENT';
INSERT IGNORE INTO user_role(user_id, role_id) SELECT 1174, id FROM role WHERE code = 'ROLE_PATIENT';
INSERT IGNORE INTO user_role(user_id, role_id) SELECT 1175, id FROM role WHERE code = 'ROLE_PATIENT';
INSERT IGNORE INTO user_role(user_id, role_id) SELECT 1176, id FROM role WHERE code = 'ROLE_PATIENT';
INSERT IGNORE INTO user_role(user_id, role_id) SELECT 1177, id FROM role WHERE code = 'ROLE_PATIENT';
INSERT IGNORE INTO user_role(user_id, role_id) SELECT 1178, id FROM role WHERE code = 'ROLE_PATIENT';
INSERT IGNORE INTO user_role(user_id, role_id) SELECT 1179, id FROM role WHERE code = 'ROLE_PATIENT';
INSERT IGNORE INTO user_role(user_id, role_id) SELECT 1180, id FROM role WHERE code = 'ROLE_PATIENT';
INSERT IGNORE INTO user_role(user_id, role_id) SELECT 1181, id FROM role WHERE code = 'ROLE_PATIENT';
INSERT IGNORE INTO user_role(user_id, role_id) SELECT 1182, id FROM role WHERE code = 'ROLE_PATIENT';
INSERT IGNORE INTO user_role(user_id, role_id) SELECT 1183, id FROM role WHERE code = 'ROLE_PATIENT';
INSERT IGNORE INTO user_role(user_id, role_id) SELECT 1184, id FROM role WHERE code = 'ROLE_PATIENT';
INSERT IGNORE INTO user_role(user_id, role_id) SELECT 1185, id FROM role WHERE code = 'ROLE_PATIENT';
INSERT IGNORE INTO user_role(user_id, role_id) SELECT 1186, id FROM role WHERE code = 'ROLE_PATIENT';
INSERT IGNORE INTO user_role(user_id, role_id) SELECT 1187, id FROM role WHERE code = 'ROLE_PATIENT';
INSERT IGNORE INTO user_role(user_id, role_id) SELECT 1188, id FROM role WHERE code = 'ROLE_PATIENT';
INSERT IGNORE INTO user_role(user_id, role_id) SELECT 1189, id FROM role WHERE code = 'ROLE_PATIENT';
INSERT IGNORE INTO user_role(user_id, role_id) SELECT 1190, id FROM role WHERE code = 'ROLE_PATIENT';
INSERT IGNORE INTO user_role(user_id, role_id) SELECT 1191, id FROM role WHERE code = 'ROLE_PATIENT';
INSERT IGNORE INTO user_role(user_id, role_id) SELECT 1192, id FROM role WHERE code = 'ROLE_PATIENT';
INSERT IGNORE INTO user_role(user_id, role_id) SELECT 1193, id FROM role WHERE code = 'ROLE_PATIENT';
INSERT IGNORE INTO user_role(user_id, role_id) SELECT 1194, id FROM role WHERE code = 'ROLE_PATIENT';
INSERT IGNORE INTO user_role(user_id, role_id) SELECT 1195, id FROM role WHERE code = 'ROLE_PATIENT';
INSERT IGNORE INTO user_role(user_id, role_id) SELECT 1196, id FROM role WHERE code = 'ROLE_PATIENT';
INSERT IGNORE INTO user_role(user_id, role_id) SELECT 1197, id FROM role WHERE code = 'ROLE_PATIENT';
INSERT IGNORE INTO user_role(user_id, role_id) SELECT 1198, id FROM role WHERE code = 'ROLE_PATIENT';
INSERT IGNORE INTO user_role(user_id, role_id) SELECT 1199, id FROM role WHERE code = 'ROLE_PATIENT';
INSERT IGNORE INTO user_role(user_id, role_id) SELECT 1200, id FROM role WHERE code = 'ROLE_PATIENT';
INSERT IGNORE INTO user_role(user_id, role_id) SELECT 1201, id FROM role WHERE code = 'ROLE_PATIENT';
INSERT IGNORE INTO user_role(user_id, role_id) SELECT 1202, id FROM role WHERE code = 'ROLE_PATIENT';
INSERT IGNORE INTO user_role(user_id, role_id) SELECT 1203, id FROM role WHERE code = 'ROLE_PATIENT';
INSERT IGNORE INTO user_role(user_id, role_id) SELECT 1204, id FROM role WHERE code = 'ROLE_PATIENT';
INSERT IGNORE INTO user_role(user_id, role_id) SELECT 1205, id FROM role WHERE code = 'ROLE_PATIENT';
INSERT IGNORE INTO user_role(user_id, role_id) SELECT 1206, id FROM role WHERE code = 'ROLE_PATIENT';
INSERT IGNORE INTO user_role(user_id, role_id) SELECT 1207, id FROM role WHERE code = 'ROLE_PATIENT';
INSERT IGNORE INTO user_role(user_id, role_id) SELECT 1208, id FROM role WHERE code = 'ROLE_PATIENT';
INSERT IGNORE INTO user_role(user_id, role_id) SELECT 1209, id FROM role WHERE code = 'ROLE_PATIENT';
INSERT IGNORE INTO user_role(user_id, role_id) SELECT 1210, id FROM role WHERE code = 'ROLE_PATIENT';
INSERT IGNORE INTO user_role(user_id, role_id) SELECT 1211, id FROM role WHERE code = 'ROLE_PATIENT';
INSERT IGNORE INTO user_role(user_id, role_id) SELECT 1212, id FROM role WHERE code = 'ROLE_PATIENT';
INSERT IGNORE INTO user_role(user_id, role_id) SELECT 1213, id FROM role WHERE code = 'ROLE_PATIENT';
INSERT IGNORE INTO user_role(user_id, role_id) SELECT 1214, id FROM role WHERE code = 'ROLE_PATIENT';
INSERT IGNORE INTO user_role(user_id, role_id) SELECT 1215, id FROM role WHERE code = 'ROLE_PATIENT';
INSERT IGNORE INTO user_role(user_id, role_id) SELECT 1216, id FROM role WHERE code = 'ROLE_PATIENT';
INSERT IGNORE INTO user_role(user_id, role_id) SELECT 1217, id FROM role WHERE code = 'ROLE_PATIENT';
INSERT IGNORE INTO user_role(user_id, role_id) SELECT 1218, id FROM role WHERE code = 'ROLE_PATIENT';
INSERT IGNORE INTO user_role(user_id, role_id) SELECT 1219, id FROM role WHERE code = 'ROLE_PATIENT';
INSERT IGNORE INTO user_role(user_id, role_id) SELECT 1220, id FROM role WHERE code = 'ROLE_PATIENT';
INSERT IGNORE INTO user_role(user_id, role_id) SELECT 1221, id FROM role WHERE code = 'ROLE_PATIENT';
INSERT IGNORE INTO user_role(user_id, role_id) SELECT 1222, id FROM role WHERE code = 'ROLE_PATIENT';
INSERT IGNORE INTO user_role(user_id, role_id) SELECT 1223, id FROM role WHERE code = 'ROLE_PATIENT';
INSERT IGNORE INTO user_role(user_id, role_id) SELECT 1224, id FROM role WHERE code = 'ROLE_PATIENT';
INSERT IGNORE INTO user_role(user_id, role_id) SELECT 1225, id FROM role WHERE code = 'ROLE_PATIENT';
INSERT IGNORE INTO user_role(user_id, role_id) SELECT 1226, id FROM role WHERE code = 'ROLE_PATIENT';
INSERT IGNORE INTO user_role(user_id, role_id) SELECT 1227, id FROM role WHERE code = 'ROLE_PATIENT';
INSERT IGNORE INTO user_role(user_id, role_id) SELECT 1228, id FROM role WHERE code = 'ROLE_PATIENT';
INSERT IGNORE INTO user_role(user_id, role_id) SELECT 1229, id FROM role WHERE code = 'ROLE_PATIENT';
INSERT IGNORE INTO user_role(user_id, role_id) SELECT 1230, id FROM role WHERE code = 'ROLE_PATIENT';
INSERT IGNORE INTO user_role(user_id, role_id) SELECT 1231, id FROM role WHERE code = 'ROLE_PATIENT';
INSERT IGNORE INTO user_role(user_id, role_id) SELECT 1232, id FROM role WHERE code = 'ROLE_PATIENT';
INSERT IGNORE INTO user_role(user_id, role_id) SELECT 1233, id FROM role WHERE code = 'ROLE_PATIENT';
INSERT IGNORE INTO user_role(user_id, role_id) SELECT 1234, id FROM role WHERE code = 'ROLE_PATIENT';
INSERT IGNORE INTO user_role(user_id, role_id) SELECT 1235, id FROM role WHERE code = 'ROLE_PATIENT';
INSERT IGNORE INTO user_role(user_id, role_id) SELECT 1236, id FROM role WHERE code = 'ROLE_PATIENT';
INSERT IGNORE INTO user_role(user_id, role_id) SELECT 1237, id FROM role WHERE code = 'ROLE_PATIENT';
INSERT IGNORE INTO user_role(user_id, role_id) SELECT 1238, id FROM role WHERE code = 'ROLE_PATIENT';
INSERT IGNORE INTO user_role(user_id, role_id) SELECT 1239, id FROM role WHERE code = 'ROLE_PATIENT';
INSERT IGNORE INTO user_role(user_id, role_id) SELECT 1240, id FROM role WHERE code = 'ROLE_PATIENT';
INSERT IGNORE INTO user_role(user_id, role_id) SELECT 1241, id FROM role WHERE code = 'ROLE_PATIENT';
INSERT IGNORE INTO user_role(user_id, role_id) SELECT 1242, id FROM role WHERE code = 'ROLE_PATIENT';
INSERT IGNORE INTO user_role(user_id, role_id) SELECT 1243, id FROM role WHERE code = 'ROLE_PATIENT';
INSERT IGNORE INTO user_role(user_id, role_id) SELECT 1244, id FROM role WHERE code = 'ROLE_PATIENT';
INSERT IGNORE INTO user_role(user_id, role_id) SELECT 1245, id FROM role WHERE code = 'ROLE_PATIENT';
INSERT IGNORE INTO user_role(user_id, role_id) SELECT 1246, id FROM role WHERE code = 'ROLE_PATIENT';
INSERT IGNORE INTO user_role(user_id, role_id) SELECT 1247, id FROM role WHERE code = 'ROLE_PATIENT';
INSERT IGNORE INTO user_role(user_id, role_id) SELECT 1248, id FROM role WHERE code = 'ROLE_PATIENT';
INSERT IGNORE INTO user_role(user_id, role_id) SELECT 1249, id FROM role WHERE code = 'ROLE_PATIENT';
INSERT IGNORE INTO user_role(user_id, role_id) SELECT 1250, id FROM role WHERE code = 'ROLE_PATIENT';

INSERT INTO employee (id, user_id, employee_code, full_name, gender, date_of_birth, phone, email, address, position, salary, hired_date, active) VALUES
(1, 201, 'EMP-2026-0001', 'Nhan Vien 01', 'MALE', '1991-02-15', '0902000001', 'employee01@evercare.test', '11 Duong Noi Bo, Quan 2, TP HCM', 'Le tan', 8500000, '2026-01-05', TRUE),
(2, 202, 'EMP-2026-0002', 'Nhan Vien 02', 'FEMALE', '1992-03-15', '0902000002', 'employee02@evercare.test', '12 Duong Noi Bo, Quan 3, TP HCM', 'Thu ngan', 9000000, '2026-01-05', TRUE),
(3, 203, 'EMP-2026-0003', 'Nhan Vien 03', 'MALE', '1993-04-15', '0902000003', 'employee03@evercare.test', '13 Duong Noi Bo, Quan 4, TP HCM', 'Duoc si', 9500000, '2026-01-05', TRUE),
(4, 204, 'EMP-2026-0004', 'Nhan Vien 04', 'FEMALE', '1994-05-15', '0902000004', 'employee04@evercare.test', '14 Duong Noi Bo, Quan 5, TP HCM', 'Ky thuat vien xet nghiem', 10000000, '2026-01-05', TRUE),
(5, 205, 'EMP-2026-0005', 'Nhan Vien 05', 'MALE', '1995-06-15', '0902000005', 'employee05@evercare.test', '15 Duong Noi Bo, Quan 6, TP HCM', 'Quan ly van hanh', 10500000, '2026-01-05', TRUE),
(6, 206, 'EMP-2026-0006', 'Nhan Vien 06', 'FEMALE', '1996-07-15', '0902000006', 'employee06@evercare.test', '16 Duong Noi Bo, Quan 7, TP HCM', 'Thu ngan', 11000000, '2026-01-05', TRUE),
(7, 207, 'EMP-2026-0007', 'Nhan Vien 07', 'MALE', '1997-08-15', '0902000007', 'employee07@evercare.test', '17 Duong Noi Bo, Quan 8, TP HCM', 'Duoc si', 11500000, '2026-01-05', TRUE),
(8, 208, 'EMP-2026-0008', 'Nhan Vien 08', 'FEMALE', '1998-09-15', '0902000008', 'employee08@evercare.test', '18 Duong Noi Bo, Quan 9, TP HCM', 'Le tan', 12000000, '2026-01-05', TRUE),
(9, 209, 'EMP-2026-0009', 'Nhan Vien 09', 'MALE', '1999-01-15', '0902000009', 'employee09@evercare.test', '19 Duong Noi Bo, Quan 10, TP HCM', 'Ky thuat vien xet nghiem', 12500000, '2026-01-05', TRUE),
(10, 210, 'EMP-2026-0010', 'Nhan Vien 10', 'FEMALE', '1990-02-15', '0902000010', 'employee10@evercare.test', '20 Duong Noi Bo, Quan 1, TP HCM', 'Quan ly phong kham', 13000000, '2026-01-05', TRUE);
INSERT INTO doctor (id, user_id, department_id, doctor_code, full_name, phone, email, avatar_url, qualification, specialization, doctor_type, work_status, base_salary, hourly_rate, bio, active) VALUES
(1, 101, (SELECT id FROM department WHERE code='NOI'), 'DOC-2026-0001', 'Bac Si 01', '0901000001', 'doctor01@evercare.test', NULL, 'Bac si chuyen khoa I', 'Noi tong quat', 'FULL_TIME', 'AVAILABLE', 19000000, 300000, 'Bac si phu trach noi tong quat', TRUE),
(2, 102, (SELECT id FROM department WHERE code='NHI'), 'DOC-2026-0002', 'Bac Si 02', '0901000002', 'doctor02@evercare.test', NULL, 'Bac si chuyen khoa I', 'Nhi khoa', 'FULL_TIME', 'AVAILABLE', 20000000, 300000, 'Bac si phu trach nhi khoa', TRUE),
(3, 103, (SELECT id FROM department WHERE code='TMH'), 'DOC-2026-0003', 'Bac Si 03', '0901000003', 'doctor03@evercare.test', NULL, 'Bac si chuyen khoa I', 'Tai mui hong', 'FULL_TIME', 'AVAILABLE', 21000000, 300000, 'Bac si phu trach tai mui hong', TRUE),
(4, 104, (SELECT id FROM department WHERE code='RHM'), 'DOC-2026-0004', 'Bac Si 04', '0901000004', 'doctor04@evercare.test', NULL, 'Bac si chuyen khoa I', 'Rang ham mat', 'FULL_TIME', 'AVAILABLE', 22000000, 300000, 'Bac si phu trach rang ham mat', TRUE),
(5, 105, (SELECT id FROM department WHERE code='XN'), 'DOC-2026-0005', 'Bac Si 05', '0901000005', 'doctor05@evercare.test', NULL, 'Bac si chuyen khoa I', 'Xet nghiem', 'FULL_TIME', 'AVAILABLE', 23000000, 300000, 'Bac si phu trach xet nghiem', TRUE),
(6, 106, (SELECT id FROM department WHERE code='CDHA'), 'DOC-2026-0006', 'Bac Si 06', '0901000006', 'doctor06@evercare.test', NULL, 'Bac si chuyen khoa I', 'Chan doan hinh anh', 'FULL_TIME', 'AVAILABLE', 24000000, 300000, 'Bac si phu trach chan doan hinh anh', TRUE),
(7, 107, (SELECT id FROM department WHERE code='DA_LIEU'), 'DOC-2026-0007', 'Bac Si 07', '0901000007', 'doctor07@evercare.test', NULL, 'Bac si chuyen khoa I', 'Da lieu', 'FULL_TIME', 'AVAILABLE', 25000000, 300000, 'Bac si phu trach da lieu', TRUE),
(8, 108, (SELECT id FROM department WHERE code='SAN'), 'DOC-2026-0008', 'Bac Si 08', '0901000008', 'doctor08@evercare.test', NULL, 'Bac si chuyen khoa I', 'San phu khoa', 'FULL_TIME', 'AVAILABLE', 26000000, 300000, 'Bac si phu trach san phu khoa', TRUE),
(9, 109, (SELECT id FROM department WHERE code='MAT'), 'DOC-2026-0009', 'Bac Si 09', '0901000009', 'doctor09@evercare.test', NULL, 'Bac si chuyen khoa I', 'Mat', 'FULL_TIME', 'AVAILABLE', 27000000, 300000, 'Bac si phu trach mat', TRUE),
(10, 110, (SELECT id FROM department WHERE code='NOI'), 'DOC-2026-0010', 'Bac Si 10', '0901000010', 'doctor10@evercare.test', NULL, 'Bac si chuyen khoa I', 'Noi tiet', 'FULL_TIME', 'AVAILABLE', 28000000, 300000, 'Bac si phu trach noi tiet', TRUE);

INSERT INTO patient (id, user_id, patient_code, full_name, gender, date_of_birth, phone, email, citizen_id, health_insurance_no, address, emergency_contact_name, emergency_contact_phone, blood_type, allergy_note, medical_history_note, active) VALUES
(1, 1001, 'PAT-2026-0001', 'Tran Thi Binh 001', 'MALE', '1971-02-02', '0910000001', 'patient0001@evercare.test', '079000000001', 'BH-2026-000001', '101 Duong Mau 2, Phuong 2, TP HCM', 'Nguoi Than 001', '0980000001', 'B+', 'Khong ghi nhan', 'Khong co tien su dac biet', TRUE),
(2, 1002, 'PAT-2026-0002', 'Le Minh Chi 002', 'FEMALE', '1972-03-03', '0910000002', 'patient0002@evercare.test', '079000000002', 'BH-2026-000002', '102 Duong Mau 3, Phuong 3, TP HCM', 'Nguoi Than 002', '0980000002', 'O+', 'Khong ghi nhan', 'Khong co tien su dac biet', TRUE),
(3, 1003, 'PAT-2026-0003', 'Pham Quoc Dung 003', 'MALE', '1973-04-04', '0910000003', 'patient0003@evercare.test', '079000000003', 'BH-2026-000003', '103 Duong Mau 4, Phuong 4, TP HCM', 'Nguoi Than 003', '0980000003', 'AB+', 'Khong ghi nhan', 'Khong co tien su dac biet', TRUE),
(4, 1004, 'PAT-2026-0004', 'Hoang Thanh Giang 004', 'FEMALE', '1974-05-05', '0910000004', 'patient0004@evercare.test', '079000000004', 'BH-2026-000004', '104 Duong Mau 5, Phuong 5, TP HCM', 'Nguoi Than 004', '0980000004', 'A-', 'Khong ghi nhan', 'Tien su viem mui di ung', TRUE),
(5, 1005, 'PAT-2026-0005', 'Huynh Ngoc Ha 005', 'MALE', '1975-06-06', '0910000005', 'patient0005@evercare.test', '079000000005', 'BH-2026-000005', '105 Duong Mau 6, Phuong 6, TP HCM', 'Nguoi Than 005', '0980000005', 'B-', 'Di ung hai san', 'Khong co tien su dac biet', TRUE),
(6, 1006, 'PAT-2026-0006', 'Phan Anh Hung 006', 'FEMALE', '1976-07-07', '0910000006', 'patient0006@evercare.test', '079000000006', 'BH-2026-000006', '106 Duong Mau 7, Phuong 7, TP HCM', 'Nguoi Than 006', '0980000006', 'O-', 'Khong ghi nhan', 'Khong co tien su dac biet', TRUE),
(7, 1007, 'PAT-2026-0007', 'Vu Gia Khanh 007', 'MALE', '1977-08-08', '0910000007', 'patient0007@evercare.test', '079000000007', 'BH-2026-000007', '107 Duong Mau 8, Phuong 8, TP HCM', 'Nguoi Than 007', '0980000007', 'AB-', 'Khong ghi nhan', 'Khong co tien su dac biet', TRUE),
(8, 1008, 'PAT-2026-0008', 'Vo Duc Lan 008', 'FEMALE', '1978-09-09', '0910000008', 'patient0008@evercare.test', '079000000008', 'BH-2026-000008', '108 Duong Mau 9, Phuong 9, TP HCM', 'Nguoi Than 008', '0980000008', 'A+', 'Khong ghi nhan', 'Tien su viem mui di ung', TRUE),
(9, 1009, 'PAT-2026-0009', 'Dang Bao Long 009', 'MALE', '1979-10-10', '0910000009', 'patient0009@evercare.test', '079000000009', 'BH-2026-000009', '109 Duong Mau 10, Phuong 10, TP HCM', 'Nguoi Than 009', '0980000009', 'B+', 'Khong ghi nhan', 'Khong co tien su dac biet', TRUE),
(10, 1010, 'PAT-2026-0010', 'Bui Van Mai 010', 'FEMALE', '1980-11-11', '0910000010', 'patient0010@evercare.test', '079000000010', 'BH-2026-000010', '110 Duong Mau 11, Phuong 11, TP HCM', 'Nguoi Than 010', '0980000010', 'O+', 'Di ung hai san', 'Khong co tien su dac biet', TRUE),
(11, 1011, 'PAT-2026-0011', 'Do Thi Nam 011', 'MALE', '1981-12-12', '0910000011', 'patient0011@evercare.test', '079000000011', 'BH-2026-000011', '111 Duong Mau 12, Phuong 12, TP HCM', 'Nguoi Than 011', '0980000011', 'AB+', 'Khong ghi nhan', 'Khong co tien su dac biet', TRUE),
(12, 1012, 'PAT-2026-0012', 'Ho Minh Phuc 012', 'FEMALE', '1982-01-13', '0910000012', 'patient0012@evercare.test', '079000000012', 'BH-2026-000012', '112 Duong Mau 13, Phuong 13, TP HCM', 'Nguoi Than 012', '0980000012', 'A-', 'Khong ghi nhan', 'Tien su viem mui di ung', TRUE),
(13, 1013, 'PAT-2026-0013', 'Ngo Quoc Quyen 013', 'MALE', '1983-02-14', '0910000013', 'patient0013@evercare.test', '079000000013', 'BH-2026-000013', '113 Duong Mau 14, Phuong 14, TP HCM', 'Nguoi Than 013', '0980000013', 'B-', 'Khong ghi nhan', 'Khong co tien su dac biet', TRUE),
(14, 1014, 'PAT-2026-0014', 'Duong Thanh Son 014', 'FEMALE', '1984-03-15', '0910000014', 'patient0014@evercare.test', '079000000014', 'BH-2026-000014', '114 Duong Mau 15, Phuong 15, TP HCM', 'Nguoi Than 014', '0980000014', 'O-', 'Khong ghi nhan', 'Khong co tien su dac biet', TRUE),
(15, 1015, 'PAT-2026-0015', 'Ly Ngoc Trang 015', 'MALE', '1985-04-16', '0910000015', 'patient0015@evercare.test', '079000000015', 'BH-2026-000015', '115 Duong Mau 16, Phuong 1, TP HCM', 'Nguoi Than 015', '0980000015', 'AB-', 'Di ung hai san', 'Khong co tien su dac biet', TRUE),
(16, 1016, 'PAT-2026-0016', 'Nguyen Anh Tuan 016', 'FEMALE', '1986-05-17', '0910000016', 'patient0016@evercare.test', '079000000016', 'BH-2026-000016', '116 Duong Mau 17, Phuong 2, TP HCM', 'Nguoi Than 016', '0980000016', 'A+', 'Khong ghi nhan', 'Tien su viem mui di ung', TRUE),
(17, 1017, 'PAT-2026-0017', 'Tran Gia Vy 017', 'MALE', '1987-06-18', '0910000017', 'patient0017@evercare.test', '079000000017', 'BH-2026-000017', '117 Duong Mau 18, Phuong 3, TP HCM', 'Nguoi Than 017', '0980000017', 'B+', 'Khong ghi nhan', 'Khong co tien su dac biet', TRUE),
(18, 1018, 'PAT-2026-0018', 'Le Duc Yen 018', 'FEMALE', '1988-07-19', '0910000018', 'patient0018@evercare.test', '079000000018', 'BH-2026-000018', '118 Duong Mau 19, Phuong 4, TP HCM', 'Nguoi Than 018', '0980000018', 'O+', 'Khong ghi nhan', 'Khong co tien su dac biet', TRUE),
(19, 1019, 'PAT-2026-0019', 'Pham Bao Nhi 019', 'MALE', '1989-08-20', '0910000019', 'patient0019@evercare.test', '079000000019', 'BH-2026-000019', '119 Duong Mau 20, Phuong 5, TP HCM', 'Nguoi Than 019', '0980000019', 'AB+', 'Khong ghi nhan', 'Khong co tien su dac biet', TRUE),
(20, 1020, 'PAT-2026-0020', 'Hoang Van An 020', 'FEMALE', '1990-09-21', '0910000020', 'patient0020@evercare.test', '079000000020', 'BH-2026-000020', '120 Duong Mau 1, Phuong 6, TP HCM', 'Nguoi Than 020', '0980000020', 'A-', 'Di ung hai san', 'Tien su viem mui di ung', TRUE),
(21, 1021, 'PAT-2026-0021', 'Huynh Thi Binh 021', 'MALE', '1991-10-22', '0910000021', 'patient0021@evercare.test', '079000000021', 'BH-2026-000021', '121 Duong Mau 2, Phuong 7, TP HCM', 'Nguoi Than 021', '0980000021', 'B-', 'Khong ghi nhan', 'Khong co tien su dac biet', TRUE),
(22, 1022, 'PAT-2026-0022', 'Phan Minh Chi 022', 'FEMALE', '1992-11-23', '0910000022', 'patient0022@evercare.test', '079000000022', 'BH-2026-000022', '122 Duong Mau 3, Phuong 8, TP HCM', 'Nguoi Than 022', '0980000022', 'O-', 'Khong ghi nhan', 'Khong co tien su dac biet', TRUE),
(23, 1023, 'PAT-2026-0023', 'Vu Quoc Dung 023', 'MALE', '1993-12-24', '0910000023', 'patient0023@evercare.test', '079000000023', 'BH-2026-000023', '123 Duong Mau 4, Phuong 9, TP HCM', 'Nguoi Than 023', '0980000023', 'AB-', 'Khong ghi nhan', 'Khong co tien su dac biet', TRUE),
(24, 1024, 'PAT-2026-0024', 'Vo Thanh Giang 024', 'FEMALE', '1994-01-25', '0910000024', 'patient0024@evercare.test', '079000000024', 'BH-2026-000024', '124 Duong Mau 5, Phuong 10, TP HCM', 'Nguoi Than 024', '0980000024', 'A+', 'Khong ghi nhan', 'Tien su viem mui di ung', TRUE),
(25, 1025, 'PAT-2026-0025', 'Dang Ngoc Ha 025', 'MALE', '1995-02-26', '0910000025', 'patient0025@evercare.test', '079000000025', 'BH-2026-000025', '125 Duong Mau 6, Phuong 11, TP HCM', 'Nguoi Than 025', '0980000025', 'B+', 'Di ung hai san', 'Khong co tien su dac biet', TRUE),
(26, 1026, 'PAT-2026-0026', 'Bui Anh Hung 026', 'FEMALE', '1996-03-27', '0910000026', 'patient0026@evercare.test', '079000000026', 'BH-2026-000026', '126 Duong Mau 7, Phuong 12, TP HCM', 'Nguoi Than 026', '0980000026', 'O+', 'Khong ghi nhan', 'Khong co tien su dac biet', TRUE),
(27, 1027, 'PAT-2026-0027', 'Do Gia Khanh 027', 'MALE', '1997-04-01', '0910000027', 'patient0027@evercare.test', '079000000027', 'BH-2026-000027', '127 Duong Mau 8, Phuong 13, TP HCM', 'Nguoi Than 027', '0980000027', 'AB+', 'Khong ghi nhan', 'Khong co tien su dac biet', TRUE),
(28, 1028, 'PAT-2026-0028', 'Ho Duc Lan 028', 'FEMALE', '1998-05-02', '0910000028', 'patient0028@evercare.test', '079000000028', 'BH-2026-000028', '128 Duong Mau 9, Phuong 14, TP HCM', 'Nguoi Than 028', '0980000028', 'A-', 'Khong ghi nhan', 'Tien su viem mui di ung', TRUE),
(29, 1029, 'PAT-2026-0029', 'Ngo Bao Long 029', 'MALE', '1999-06-03', '0910000029', 'patient0029@evercare.test', '079000000029', 'BH-2026-000029', '129 Duong Mau 10, Phuong 15, TP HCM', 'Nguoi Than 029', '0980000029', 'B-', 'Khong ghi nhan', 'Khong co tien su dac biet', TRUE),
(30, 1030, 'PAT-2026-0030', 'Duong Van Mai 030', 'FEMALE', '2000-07-04', '0910000030', 'patient0030@evercare.test', '079000000030', 'BH-2026-000030', '130 Duong Mau 11, Phuong 1, TP HCM', 'Nguoi Than 030', '0980000030', 'O-', 'Di ung hai san', 'Khong co tien su dac biet', TRUE),
(31, 1031, 'PAT-2026-0031', 'Ly Thi Nam 031', 'MALE', '2001-08-05', '0910000031', 'patient0031@evercare.test', '079000000031', 'BH-2026-000031', '131 Duong Mau 12, Phuong 2, TP HCM', 'Nguoi Than 031', '0980000031', 'AB-', 'Khong ghi nhan', 'Khong co tien su dac biet', TRUE),
(32, 1032, 'PAT-2026-0032', 'Nguyen Minh Phuc 032', 'FEMALE', '2002-09-06', '0910000032', 'patient0032@evercare.test', '079000000032', 'BH-2026-000032', '132 Duong Mau 13, Phuong 3, TP HCM', 'Nguoi Than 032', '0980000032', 'A+', 'Khong ghi nhan', 'Tien su viem mui di ung', TRUE),
(33, 1033, 'PAT-2026-0033', 'Tran Quoc Quyen 033', 'MALE', '2003-10-07', '0910000033', 'patient0033@evercare.test', '079000000033', 'BH-2026-000033', '133 Duong Mau 14, Phuong 4, TP HCM', 'Nguoi Than 033', '0980000033', 'B+', 'Khong ghi nhan', 'Khong co tien su dac biet', TRUE),
(34, 1034, 'PAT-2026-0034', 'Le Thanh Son 034', 'FEMALE', '2004-11-08', '0910000034', 'patient0034@evercare.test', '079000000034', 'BH-2026-000034', '134 Duong Mau 15, Phuong 5, TP HCM', 'Nguoi Than 034', '0980000034', 'O+', 'Khong ghi nhan', 'Khong co tien su dac biet', TRUE),
(35, 1035, 'PAT-2026-0035', 'Pham Ngoc Trang 035', 'MALE', '2005-12-09', '0910000035', 'patient0035@evercare.test', '079000000035', 'BH-2026-000035', '135 Duong Mau 16, Phuong 6, TP HCM', 'Nguoi Than 035', '0980000035', 'AB+', 'Di ung hai san', 'Khong co tien su dac biet', TRUE),
(36, 1036, 'PAT-2026-0036', 'Hoang Anh Tuan 036', 'FEMALE', '1970-01-10', '0910000036', 'patient0036@evercare.test', '079000000036', 'BH-2026-000036', '136 Duong Mau 17, Phuong 7, TP HCM', 'Nguoi Than 036', '0980000036', 'A-', 'Khong ghi nhan', 'Tien su viem mui di ung', TRUE),
(37, 1037, 'PAT-2026-0037', 'Huynh Gia Vy 037', 'MALE', '1971-02-11', '0910000037', 'patient0037@evercare.test', '079000000037', 'BH-2026-000037', '137 Duong Mau 18, Phuong 8, TP HCM', 'Nguoi Than 037', '0980000037', 'B-', 'Khong ghi nhan', 'Khong co tien su dac biet', TRUE),
(38, 1038, 'PAT-2026-0038', 'Phan Duc Yen 038', 'FEMALE', '1972-03-12', '0910000038', 'patient0038@evercare.test', '079000000038', 'BH-2026-000038', '138 Duong Mau 19, Phuong 9, TP HCM', 'Nguoi Than 038', '0980000038', 'O-', 'Khong ghi nhan', 'Khong co tien su dac biet', TRUE),
(39, 1039, 'PAT-2026-0039', 'Vu Bao Nhi 039', 'MALE', '1973-04-13', '0910000039', 'patient0039@evercare.test', '079000000039', 'BH-2026-000039', '139 Duong Mau 20, Phuong 10, TP HCM', 'Nguoi Than 039', '0980000039', 'AB-', 'Khong ghi nhan', 'Khong co tien su dac biet', TRUE),
(40, 1040, 'PAT-2026-0040', 'Vo Van An 040', 'FEMALE', '1974-05-14', '0910000040', 'patient0040@evercare.test', '079000000040', 'BH-2026-000040', '140 Duong Mau 1, Phuong 11, TP HCM', 'Nguoi Than 040', '0980000040', 'A+', 'Di ung hai san', 'Tien su viem mui di ung', TRUE),
(41, 1041, 'PAT-2026-0041', 'Dang Thi Binh 041', 'MALE', '1975-06-15', '0910000041', 'patient0041@evercare.test', '079000000041', 'BH-2026-000041', '141 Duong Mau 2, Phuong 12, TP HCM', 'Nguoi Than 041', '0980000041', 'B+', 'Khong ghi nhan', 'Khong co tien su dac biet', TRUE),
(42, 1042, 'PAT-2026-0042', 'Bui Minh Chi 042', 'FEMALE', '1976-07-16', '0910000042', 'patient0042@evercare.test', '079000000042', 'BH-2026-000042', '142 Duong Mau 3, Phuong 13, TP HCM', 'Nguoi Than 042', '0980000042', 'O+', 'Khong ghi nhan', 'Khong co tien su dac biet', TRUE),
(43, 1043, 'PAT-2026-0043', 'Do Quoc Dung 043', 'MALE', '1977-08-17', '0910000043', 'patient0043@evercare.test', '079000000043', 'BH-2026-000043', '143 Duong Mau 4, Phuong 14, TP HCM', 'Nguoi Than 043', '0980000043', 'AB+', 'Khong ghi nhan', 'Khong co tien su dac biet', TRUE),
(44, 1044, 'PAT-2026-0044', 'Ho Thanh Giang 044', 'FEMALE', '1978-09-18', '0910000044', 'patient0044@evercare.test', '079000000044', 'BH-2026-000044', '144 Duong Mau 5, Phuong 15, TP HCM', 'Nguoi Than 044', '0980000044', 'A-', 'Khong ghi nhan', 'Tien su viem mui di ung', TRUE),
(45, 1045, 'PAT-2026-0045', 'Ngo Ngoc Ha 045', 'MALE', '1979-10-19', '0910000045', 'patient0045@evercare.test', '079000000045', 'BH-2026-000045', '145 Duong Mau 6, Phuong 1, TP HCM', 'Nguoi Than 045', '0980000045', 'B-', 'Di ung hai san', 'Khong co tien su dac biet', TRUE),
(46, 1046, 'PAT-2026-0046', 'Duong Anh Hung 046', 'FEMALE', '1980-11-20', '0910000046', 'patient0046@evercare.test', '079000000046', 'BH-2026-000046', '146 Duong Mau 7, Phuong 2, TP HCM', 'Nguoi Than 046', '0980000046', 'O-', 'Khong ghi nhan', 'Khong co tien su dac biet', TRUE),
(47, 1047, 'PAT-2026-0047', 'Ly Gia Khanh 047', 'MALE', '1981-12-21', '0910000047', 'patient0047@evercare.test', '079000000047', 'BH-2026-000047', '147 Duong Mau 8, Phuong 3, TP HCM', 'Nguoi Than 047', '0980000047', 'AB-', 'Khong ghi nhan', 'Khong co tien su dac biet', TRUE),
(48, 1048, 'PAT-2026-0048', 'Nguyen Duc Lan 048', 'FEMALE', '1982-01-22', '0910000048', 'patient0048@evercare.test', '079000000048', 'BH-2026-000048', '148 Duong Mau 9, Phuong 4, TP HCM', 'Nguoi Than 048', '0980000048', 'A+', 'Khong ghi nhan', 'Tien su viem mui di ung', TRUE),
(49, 1049, 'PAT-2026-0049', 'Tran Bao Long 049', 'MALE', '1983-02-23', '0910000049', 'patient0049@evercare.test', '079000000049', 'BH-2026-000049', '149 Duong Mau 10, Phuong 5, TP HCM', 'Nguoi Than 049', '0980000049', 'B+', 'Khong ghi nhan', 'Khong co tien su dac biet', TRUE),
(50, 1050, 'PAT-2026-0050', 'Le Van Mai 050', 'FEMALE', '1984-03-24', '0910000050', 'patient0050@evercare.test', '079000000050', 'BH-2026-000050', '150 Duong Mau 11, Phuong 6, TP HCM', 'Nguoi Than 050', '0980000050', 'O+', 'Di ung hai san', 'Khong co tien su dac biet', TRUE),
(51, 1051, 'PAT-2026-0051', 'Pham Thi Nam 051', 'MALE', '1985-04-25', '0910000051', 'patient0051@evercare.test', '079000000051', 'BH-2026-000051', '151 Duong Mau 12, Phuong 7, TP HCM', 'Nguoi Than 051', '0980000051', 'AB+', 'Khong ghi nhan', 'Khong co tien su dac biet', TRUE),
(52, 1052, 'PAT-2026-0052', 'Hoang Minh Phuc 052', 'FEMALE', '1986-05-26', '0910000052', 'patient0052@evercare.test', '079000000052', 'BH-2026-000052', '152 Duong Mau 13, Phuong 8, TP HCM', 'Nguoi Than 052', '0980000052', 'A-', 'Khong ghi nhan', 'Tien su viem mui di ung', TRUE),
(53, 1053, 'PAT-2026-0053', 'Huynh Quoc Quyen 053', 'MALE', '1987-06-27', '0910000053', 'patient0053@evercare.test', '079000000053', 'BH-2026-000053', '153 Duong Mau 14, Phuong 9, TP HCM', 'Nguoi Than 053', '0980000053', 'B-', 'Khong ghi nhan', 'Khong co tien su dac biet', TRUE),
(54, 1054, 'PAT-2026-0054', 'Phan Thanh Son 054', 'FEMALE', '1988-07-01', '0910000054', 'patient0054@evercare.test', '079000000054', 'BH-2026-000054', '154 Duong Mau 15, Phuong 10, TP HCM', 'Nguoi Than 054', '0980000054', 'O-', 'Khong ghi nhan', 'Khong co tien su dac biet', TRUE),
(55, 1055, 'PAT-2026-0055', 'Vu Ngoc Trang 055', 'MALE', '1989-08-02', '0910000055', 'patient0055@evercare.test', '079000000055', 'BH-2026-000055', '155 Duong Mau 16, Phuong 11, TP HCM', 'Nguoi Than 055', '0980000055', 'AB-', 'Di ung hai san', 'Khong co tien su dac biet', TRUE),
(56, 1056, 'PAT-2026-0056', 'Vo Anh Tuan 056', 'FEMALE', '1990-09-03', '0910000056', 'patient0056@evercare.test', '079000000056', 'BH-2026-000056', '156 Duong Mau 17, Phuong 12, TP HCM', 'Nguoi Than 056', '0980000056', 'A+', 'Khong ghi nhan', 'Tien su viem mui di ung', TRUE),
(57, 1057, 'PAT-2026-0057', 'Dang Gia Vy 057', 'MALE', '1991-10-04', '0910000057', 'patient0057@evercare.test', '079000000057', 'BH-2026-000057', '157 Duong Mau 18, Phuong 13, TP HCM', 'Nguoi Than 057', '0980000057', 'B+', 'Khong ghi nhan', 'Khong co tien su dac biet', TRUE),
(58, 1058, 'PAT-2026-0058', 'Bui Duc Yen 058', 'FEMALE', '1992-11-05', '0910000058', 'patient0058@evercare.test', '079000000058', 'BH-2026-000058', '158 Duong Mau 19, Phuong 14, TP HCM', 'Nguoi Than 058', '0980000058', 'O+', 'Khong ghi nhan', 'Khong co tien su dac biet', TRUE),
(59, 1059, 'PAT-2026-0059', 'Do Bao Nhi 059', 'MALE', '1993-12-06', '0910000059', 'patient0059@evercare.test', '079000000059', 'BH-2026-000059', '159 Duong Mau 20, Phuong 15, TP HCM', 'Nguoi Than 059', '0980000059', 'AB+', 'Khong ghi nhan', 'Khong co tien su dac biet', TRUE),
(60, 1060, 'PAT-2026-0060', 'Ho Van An 060', 'FEMALE', '1994-01-07', '0910000060', 'patient0060@evercare.test', '079000000060', 'BH-2026-000060', '160 Duong Mau 1, Phuong 1, TP HCM', 'Nguoi Than 060', '0980000060', 'A-', 'Di ung hai san', 'Tien su viem mui di ung', TRUE),
(61, 1061, 'PAT-2026-0061', 'Ngo Thi Binh 061', 'MALE', '1995-02-08', '0910000061', 'patient0061@evercare.test', '079000000061', 'BH-2026-000061', '161 Duong Mau 2, Phuong 2, TP HCM', 'Nguoi Than 061', '0980000061', 'B-', 'Khong ghi nhan', 'Khong co tien su dac biet', TRUE),
(62, 1062, 'PAT-2026-0062', 'Duong Minh Chi 062', 'FEMALE', '1996-03-09', '0910000062', 'patient0062@evercare.test', '079000000062', 'BH-2026-000062', '162 Duong Mau 3, Phuong 3, TP HCM', 'Nguoi Than 062', '0980000062', 'O-', 'Khong ghi nhan', 'Khong co tien su dac biet', TRUE),
(63, 1063, 'PAT-2026-0063', 'Ly Quoc Dung 063', 'MALE', '1997-04-10', '0910000063', 'patient0063@evercare.test', '079000000063', 'BH-2026-000063', '163 Duong Mau 4, Phuong 4, TP HCM', 'Nguoi Than 063', '0980000063', 'AB-', 'Khong ghi nhan', 'Khong co tien su dac biet', TRUE),
(64, 1064, 'PAT-2026-0064', 'Nguyen Thanh Giang 064', 'FEMALE', '1998-05-11', '0910000064', 'patient0064@evercare.test', '079000000064', 'BH-2026-000064', '164 Duong Mau 5, Phuong 5, TP HCM', 'Nguoi Than 064', '0980000064', 'A+', 'Khong ghi nhan', 'Tien su viem mui di ung', TRUE),
(65, 1065, 'PAT-2026-0065', 'Tran Ngoc Ha 065', 'MALE', '1999-06-12', '0910000065', 'patient0065@evercare.test', '079000000065', 'BH-2026-000065', '165 Duong Mau 6, Phuong 6, TP HCM', 'Nguoi Than 065', '0980000065', 'B+', 'Di ung hai san', 'Khong co tien su dac biet', TRUE),
(66, 1066, 'PAT-2026-0066', 'Le Anh Hung 066', 'FEMALE', '2000-07-13', '0910000066', 'patient0066@evercare.test', '079000000066', 'BH-2026-000066', '166 Duong Mau 7, Phuong 7, TP HCM', 'Nguoi Than 066', '0980000066', 'O+', 'Khong ghi nhan', 'Khong co tien su dac biet', TRUE),
(67, 1067, 'PAT-2026-0067', 'Pham Gia Khanh 067', 'MALE', '2001-08-14', '0910000067', 'patient0067@evercare.test', '079000000067', 'BH-2026-000067', '167 Duong Mau 8, Phuong 8, TP HCM', 'Nguoi Than 067', '0980000067', 'AB+', 'Khong ghi nhan', 'Khong co tien su dac biet', TRUE),
(68, 1068, 'PAT-2026-0068', 'Hoang Duc Lan 068', 'FEMALE', '2002-09-15', '0910000068', 'patient0068@evercare.test', '079000000068', 'BH-2026-000068', '168 Duong Mau 9, Phuong 9, TP HCM', 'Nguoi Than 068', '0980000068', 'A-', 'Khong ghi nhan', 'Tien su viem mui di ung', TRUE),
(69, 1069, 'PAT-2026-0069', 'Huynh Bao Long 069', 'MALE', '2003-10-16', '0910000069', 'patient0069@evercare.test', '079000000069', 'BH-2026-000069', '169 Duong Mau 10, Phuong 10, TP HCM', 'Nguoi Than 069', '0980000069', 'B-', 'Khong ghi nhan', 'Khong co tien su dac biet', TRUE),
(70, 1070, 'PAT-2026-0070', 'Phan Van Mai 070', 'FEMALE', '2004-11-17', '0910000070', 'patient0070@evercare.test', '079000000070', 'BH-2026-000070', '170 Duong Mau 11, Phuong 11, TP HCM', 'Nguoi Than 070', '0980000070', 'O-', 'Di ung hai san', 'Khong co tien su dac biet', TRUE),
(71, 1071, 'PAT-2026-0071', 'Vu Thi Nam 071', 'MALE', '2005-12-18', '0910000071', 'patient0071@evercare.test', '079000000071', 'BH-2026-000071', '171 Duong Mau 12, Phuong 12, TP HCM', 'Nguoi Than 071', '0980000071', 'AB-', 'Khong ghi nhan', 'Khong co tien su dac biet', TRUE),
(72, 1072, 'PAT-2026-0072', 'Vo Minh Phuc 072', 'FEMALE', '1970-01-19', '0910000072', 'patient0072@evercare.test', '079000000072', 'BH-2026-000072', '172 Duong Mau 13, Phuong 13, TP HCM', 'Nguoi Than 072', '0980000072', 'A+', 'Khong ghi nhan', 'Tien su viem mui di ung', TRUE),
(73, 1073, 'PAT-2026-0073', 'Dang Quoc Quyen 073', 'MALE', '1971-02-20', '0910000073', 'patient0073@evercare.test', '079000000073', 'BH-2026-000073', '173 Duong Mau 14, Phuong 14, TP HCM', 'Nguoi Than 073', '0980000073', 'B+', 'Khong ghi nhan', 'Khong co tien su dac biet', TRUE),
(74, 1074, 'PAT-2026-0074', 'Bui Thanh Son 074', 'FEMALE', '1972-03-21', '0910000074', 'patient0074@evercare.test', '079000000074', 'BH-2026-000074', '174 Duong Mau 15, Phuong 15, TP HCM', 'Nguoi Than 074', '0980000074', 'O+', 'Khong ghi nhan', 'Khong co tien su dac biet', TRUE),
(75, 1075, 'PAT-2026-0075', 'Do Ngoc Trang 075', 'MALE', '1973-04-22', '0910000075', 'patient0075@evercare.test', '079000000075', 'BH-2026-000075', '175 Duong Mau 16, Phuong 1, TP HCM', 'Nguoi Than 075', '0980000075', 'AB+', 'Di ung hai san', 'Khong co tien su dac biet', TRUE),
(76, 1076, 'PAT-2026-0076', 'Ho Anh Tuan 076', 'FEMALE', '1974-05-23', '0910000076', 'patient0076@evercare.test', '079000000076', 'BH-2026-000076', '176 Duong Mau 17, Phuong 2, TP HCM', 'Nguoi Than 076', '0980000076', 'A-', 'Khong ghi nhan', 'Tien su viem mui di ung', TRUE),
(77, 1077, 'PAT-2026-0077', 'Ngo Gia Vy 077', 'MALE', '1975-06-24', '0910000077', 'patient0077@evercare.test', '079000000077', 'BH-2026-000077', '177 Duong Mau 18, Phuong 3, TP HCM', 'Nguoi Than 077', '0980000077', 'B-', 'Khong ghi nhan', 'Khong co tien su dac biet', TRUE),
(78, 1078, 'PAT-2026-0078', 'Duong Duc Yen 078', 'FEMALE', '1976-07-25', '0910000078', 'patient0078@evercare.test', '079000000078', 'BH-2026-000078', '178 Duong Mau 19, Phuong 4, TP HCM', 'Nguoi Than 078', '0980000078', 'O-', 'Khong ghi nhan', 'Khong co tien su dac biet', TRUE),
(79, 1079, 'PAT-2026-0079', 'Ly Bao Nhi 079', 'MALE', '1977-08-26', '0910000079', 'patient0079@evercare.test', '079000000079', 'BH-2026-000079', '179 Duong Mau 20, Phuong 5, TP HCM', 'Nguoi Than 079', '0980000079', 'AB-', 'Khong ghi nhan', 'Khong co tien su dac biet', TRUE),
(80, 1080, 'PAT-2026-0080', 'Nguyen Van An 080', 'FEMALE', '1978-09-27', '0910000080', 'patient0080@evercare.test', '079000000080', 'BH-2026-000080', '180 Duong Mau 1, Phuong 6, TP HCM', 'Nguoi Than 080', '0980000080', 'A+', 'Di ung hai san', 'Tien su viem mui di ung', TRUE),
(81, 1081, 'PAT-2026-0081', 'Tran Thi Binh 081', 'MALE', '1979-10-01', '0910000081', 'patient0081@evercare.test', '079000000081', 'BH-2026-000081', '181 Duong Mau 2, Phuong 7, TP HCM', 'Nguoi Than 081', '0980000081', 'B+', 'Khong ghi nhan', 'Khong co tien su dac biet', TRUE),
(82, 1082, 'PAT-2026-0082', 'Le Minh Chi 082', 'FEMALE', '1980-11-02', '0910000082', 'patient0082@evercare.test', '079000000082', 'BH-2026-000082', '182 Duong Mau 3, Phuong 8, TP HCM', 'Nguoi Than 082', '0980000082', 'O+', 'Khong ghi nhan', 'Khong co tien su dac biet', TRUE),
(83, 1083, 'PAT-2026-0083', 'Pham Quoc Dung 083', 'MALE', '1981-12-03', '0910000083', 'patient0083@evercare.test', '079000000083', 'BH-2026-000083', '183 Duong Mau 4, Phuong 9, TP HCM', 'Nguoi Than 083', '0980000083', 'AB+', 'Khong ghi nhan', 'Khong co tien su dac biet', TRUE),
(84, 1084, 'PAT-2026-0084', 'Hoang Thanh Giang 084', 'FEMALE', '1982-01-04', '0910000084', 'patient0084@evercare.test', '079000000084', 'BH-2026-000084', '184 Duong Mau 5, Phuong 10, TP HCM', 'Nguoi Than 084', '0980000084', 'A-', 'Khong ghi nhan', 'Tien su viem mui di ung', TRUE),
(85, 1085, 'PAT-2026-0085', 'Huynh Ngoc Ha 085', 'MALE', '1983-02-05', '0910000085', 'patient0085@evercare.test', '079000000085', 'BH-2026-000085', '185 Duong Mau 6, Phuong 11, TP HCM', 'Nguoi Than 085', '0980000085', 'B-', 'Di ung hai san', 'Khong co tien su dac biet', TRUE),
(86, 1086, 'PAT-2026-0086', 'Phan Anh Hung 086', 'FEMALE', '1984-03-06', '0910000086', 'patient0086@evercare.test', '079000000086', 'BH-2026-000086', '186 Duong Mau 7, Phuong 12, TP HCM', 'Nguoi Than 086', '0980000086', 'O-', 'Khong ghi nhan', 'Khong co tien su dac biet', TRUE),
(87, 1087, 'PAT-2026-0087', 'Vu Gia Khanh 087', 'MALE', '1985-04-07', '0910000087', 'patient0087@evercare.test', '079000000087', 'BH-2026-000087', '187 Duong Mau 8, Phuong 13, TP HCM', 'Nguoi Than 087', '0980000087', 'AB-', 'Khong ghi nhan', 'Khong co tien su dac biet', TRUE),
(88, 1088, 'PAT-2026-0088', 'Vo Duc Lan 088', 'FEMALE', '1986-05-08', '0910000088', 'patient0088@evercare.test', '079000000088', 'BH-2026-000088', '188 Duong Mau 9, Phuong 14, TP HCM', 'Nguoi Than 088', '0980000088', 'A+', 'Khong ghi nhan', 'Tien su viem mui di ung', TRUE),
(89, 1089, 'PAT-2026-0089', 'Dang Bao Long 089', 'MALE', '1987-06-09', '0910000089', 'patient0089@evercare.test', '079000000089', 'BH-2026-000089', '189 Duong Mau 10, Phuong 15, TP HCM', 'Nguoi Than 089', '0980000089', 'B+', 'Khong ghi nhan', 'Khong co tien su dac biet', TRUE),
(90, 1090, 'PAT-2026-0090', 'Bui Van Mai 090', 'FEMALE', '1988-07-10', '0910000090', 'patient0090@evercare.test', '079000000090', 'BH-2026-000090', '190 Duong Mau 11, Phuong 1, TP HCM', 'Nguoi Than 090', '0980000090', 'O+', 'Di ung hai san', 'Khong co tien su dac biet', TRUE),
(91, 1091, 'PAT-2026-0091', 'Do Thi Nam 091', 'MALE', '1989-08-11', '0910000091', 'patient0091@evercare.test', '079000000091', 'BH-2026-000091', '191 Duong Mau 12, Phuong 2, TP HCM', 'Nguoi Than 091', '0980000091', 'AB+', 'Khong ghi nhan', 'Khong co tien su dac biet', TRUE),
(92, 1092, 'PAT-2026-0092', 'Ho Minh Phuc 092', 'FEMALE', '1990-09-12', '0910000092', 'patient0092@evercare.test', '079000000092', 'BH-2026-000092', '192 Duong Mau 13, Phuong 3, TP HCM', 'Nguoi Than 092', '0980000092', 'A-', 'Khong ghi nhan', 'Tien su viem mui di ung', TRUE),
(93, 1093, 'PAT-2026-0093', 'Ngo Quoc Quyen 093', 'MALE', '1991-10-13', '0910000093', 'patient0093@evercare.test', '079000000093', 'BH-2026-000093', '193 Duong Mau 14, Phuong 4, TP HCM', 'Nguoi Than 093', '0980000093', 'B-', 'Khong ghi nhan', 'Khong co tien su dac biet', TRUE),
(94, 1094, 'PAT-2026-0094', 'Duong Thanh Son 094', 'FEMALE', '1992-11-14', '0910000094', 'patient0094@evercare.test', '079000000094', 'BH-2026-000094', '194 Duong Mau 15, Phuong 5, TP HCM', 'Nguoi Than 094', '0980000094', 'O-', 'Khong ghi nhan', 'Khong co tien su dac biet', TRUE),
(95, 1095, 'PAT-2026-0095', 'Ly Ngoc Trang 095', 'MALE', '1993-12-15', '0910000095', 'patient0095@evercare.test', '079000000095', 'BH-2026-000095', '195 Duong Mau 16, Phuong 6, TP HCM', 'Nguoi Than 095', '0980000095', 'AB-', 'Di ung hai san', 'Khong co tien su dac biet', TRUE),
(96, 1096, 'PAT-2026-0096', 'Nguyen Anh Tuan 096', 'FEMALE', '1994-01-16', '0910000096', 'patient0096@evercare.test', '079000000096', 'BH-2026-000096', '196 Duong Mau 17, Phuong 7, TP HCM', 'Nguoi Than 096', '0980000096', 'A+', 'Khong ghi nhan', 'Tien su viem mui di ung', TRUE),
(97, 1097, 'PAT-2026-0097', 'Tran Gia Vy 097', 'MALE', '1995-02-17', '0910000097', 'patient0097@evercare.test', '079000000097', 'BH-2026-000097', '197 Duong Mau 18, Phuong 8, TP HCM', 'Nguoi Than 097', '0980000097', 'B+', 'Khong ghi nhan', 'Khong co tien su dac biet', TRUE),
(98, 1098, 'PAT-2026-0098', 'Le Duc Yen 098', 'FEMALE', '1996-03-18', '0910000098', 'patient0098@evercare.test', '079000000098', 'BH-2026-000098', '198 Duong Mau 19, Phuong 9, TP HCM', 'Nguoi Than 098', '0980000098', 'O+', 'Khong ghi nhan', 'Khong co tien su dac biet', TRUE),
(99, 1099, 'PAT-2026-0099', 'Pham Bao Nhi 099', 'MALE', '1997-04-19', '0910000099', 'patient0099@evercare.test', '079000000099', 'BH-2026-000099', '199 Duong Mau 20, Phuong 10, TP HCM', 'Nguoi Than 099', '0980000099', 'AB+', 'Khong ghi nhan', 'Khong co tien su dac biet', TRUE),
(100, 1100, 'PAT-2026-0100', 'Hoang Van An 100', 'FEMALE', '1998-05-20', '0910000100', 'patient0100@evercare.test', '079000000100', 'BH-2026-000100', '200 Duong Mau 1, Phuong 11, TP HCM', 'Nguoi Than 100', '0980000100', 'A-', 'Di ung hai san', 'Tien su viem mui di ung', TRUE),
(101, 1101, 'PAT-2026-0101', 'Huynh Thi Binh 101', 'MALE', '1999-06-21', '0910000101', 'patient0101@evercare.test', '079000000101', 'BH-2026-000101', '201 Duong Mau 2, Phuong 12, TP HCM', 'Nguoi Than 101', '0980000101', 'B-', 'Khong ghi nhan', 'Khong co tien su dac biet', TRUE),
(102, 1102, 'PAT-2026-0102', 'Phan Minh Chi 102', 'FEMALE', '2000-07-22', '0910000102', 'patient0102@evercare.test', '079000000102', 'BH-2026-000102', '202 Duong Mau 3, Phuong 13, TP HCM', 'Nguoi Than 102', '0980000102', 'O-', 'Khong ghi nhan', 'Khong co tien su dac biet', TRUE),
(103, 1103, 'PAT-2026-0103', 'Vu Quoc Dung 103', 'MALE', '2001-08-23', '0910000103', 'patient0103@evercare.test', '079000000103', 'BH-2026-000103', '203 Duong Mau 4, Phuong 14, TP HCM', 'Nguoi Than 103', '0980000103', 'AB-', 'Khong ghi nhan', 'Khong co tien su dac biet', TRUE),
(104, 1104, 'PAT-2026-0104', 'Vo Thanh Giang 104', 'FEMALE', '2002-09-24', '0910000104', 'patient0104@evercare.test', '079000000104', 'BH-2026-000104', '204 Duong Mau 5, Phuong 15, TP HCM', 'Nguoi Than 104', '0980000104', 'A+', 'Khong ghi nhan', 'Tien su viem mui di ung', TRUE),
(105, 1105, 'PAT-2026-0105', 'Dang Ngoc Ha 105', 'MALE', '2003-10-25', '0910000105', 'patient0105@evercare.test', '079000000105', 'BH-2026-000105', '205 Duong Mau 6, Phuong 1, TP HCM', 'Nguoi Than 105', '0980000105', 'B+', 'Di ung hai san', 'Khong co tien su dac biet', TRUE),
(106, 1106, 'PAT-2026-0106', 'Bui Anh Hung 106', 'FEMALE', '2004-11-26', '0910000106', 'patient0106@evercare.test', '079000000106', 'BH-2026-000106', '206 Duong Mau 7, Phuong 2, TP HCM', 'Nguoi Than 106', '0980000106', 'O+', 'Khong ghi nhan', 'Khong co tien su dac biet', TRUE),
(107, 1107, 'PAT-2026-0107', 'Do Gia Khanh 107', 'MALE', '2005-12-27', '0910000107', 'patient0107@evercare.test', '079000000107', 'BH-2026-000107', '207 Duong Mau 8, Phuong 3, TP HCM', 'Nguoi Than 107', '0980000107', 'AB+', 'Khong ghi nhan', 'Khong co tien su dac biet', TRUE),
(108, 1108, 'PAT-2026-0108', 'Ho Duc Lan 108', 'FEMALE', '1970-01-01', '0910000108', 'patient0108@evercare.test', '079000000108', 'BH-2026-000108', '208 Duong Mau 9, Phuong 4, TP HCM', 'Nguoi Than 108', '0980000108', 'A-', 'Khong ghi nhan', 'Tien su viem mui di ung', TRUE),
(109, 1109, 'PAT-2026-0109', 'Ngo Bao Long 109', 'MALE', '1971-02-02', '0910000109', 'patient0109@evercare.test', '079000000109', 'BH-2026-000109', '209 Duong Mau 10, Phuong 5, TP HCM', 'Nguoi Than 109', '0980000109', 'B-', 'Khong ghi nhan', 'Khong co tien su dac biet', TRUE),
(110, 1110, 'PAT-2026-0110', 'Duong Van Mai 110', 'FEMALE', '1972-03-03', '0910000110', 'patient0110@evercare.test', '079000000110', 'BH-2026-000110', '210 Duong Mau 11, Phuong 6, TP HCM', 'Nguoi Than 110', '0980000110', 'O-', 'Di ung hai san', 'Khong co tien su dac biet', TRUE),
(111, 1111, 'PAT-2026-0111', 'Ly Thi Nam 111', 'MALE', '1973-04-04', '0910000111', 'patient0111@evercare.test', '079000000111', 'BH-2026-000111', '211 Duong Mau 12, Phuong 7, TP HCM', 'Nguoi Than 111', '0980000111', 'AB-', 'Khong ghi nhan', 'Khong co tien su dac biet', TRUE),
(112, 1112, 'PAT-2026-0112', 'Nguyen Minh Phuc 112', 'FEMALE', '1974-05-05', '0910000112', 'patient0112@evercare.test', '079000000112', 'BH-2026-000112', '212 Duong Mau 13, Phuong 8, TP HCM', 'Nguoi Than 112', '0980000112', 'A+', 'Khong ghi nhan', 'Tien su viem mui di ung', TRUE),
(113, 1113, 'PAT-2026-0113', 'Tran Quoc Quyen 113', 'MALE', '1975-06-06', '0910000113', 'patient0113@evercare.test', '079000000113', 'BH-2026-000113', '213 Duong Mau 14, Phuong 9, TP HCM', 'Nguoi Than 113', '0980000113', 'B+', 'Khong ghi nhan', 'Khong co tien su dac biet', TRUE),
(114, 1114, 'PAT-2026-0114', 'Le Thanh Son 114', 'FEMALE', '1976-07-07', '0910000114', 'patient0114@evercare.test', '079000000114', 'BH-2026-000114', '214 Duong Mau 15, Phuong 10, TP HCM', 'Nguoi Than 114', '0980000114', 'O+', 'Khong ghi nhan', 'Khong co tien su dac biet', TRUE),
(115, 1115, 'PAT-2026-0115', 'Pham Ngoc Trang 115', 'MALE', '1977-08-08', '0910000115', 'patient0115@evercare.test', '079000000115', 'BH-2026-000115', '215 Duong Mau 16, Phuong 11, TP HCM', 'Nguoi Than 115', '0980000115', 'AB+', 'Di ung hai san', 'Khong co tien su dac biet', TRUE),
(116, 1116, 'PAT-2026-0116', 'Hoang Anh Tuan 116', 'FEMALE', '1978-09-09', '0910000116', 'patient0116@evercare.test', '079000000116', 'BH-2026-000116', '216 Duong Mau 17, Phuong 12, TP HCM', 'Nguoi Than 116', '0980000116', 'A-', 'Khong ghi nhan', 'Tien su viem mui di ung', TRUE),
(117, 1117, 'PAT-2026-0117', 'Huynh Gia Vy 117', 'MALE', '1979-10-10', '0910000117', 'patient0117@evercare.test', '079000000117', 'BH-2026-000117', '217 Duong Mau 18, Phuong 13, TP HCM', 'Nguoi Than 117', '0980000117', 'B-', 'Khong ghi nhan', 'Khong co tien su dac biet', TRUE),
(118, 1118, 'PAT-2026-0118', 'Phan Duc Yen 118', 'FEMALE', '1980-11-11', '0910000118', 'patient0118@evercare.test', '079000000118', 'BH-2026-000118', '218 Duong Mau 19, Phuong 14, TP HCM', 'Nguoi Than 118', '0980000118', 'O-', 'Khong ghi nhan', 'Khong co tien su dac biet', TRUE),
(119, 1119, 'PAT-2026-0119', 'Vu Bao Nhi 119', 'MALE', '1981-12-12', '0910000119', 'patient0119@evercare.test', '079000000119', 'BH-2026-000119', '219 Duong Mau 20, Phuong 15, TP HCM', 'Nguoi Than 119', '0980000119', 'AB-', 'Khong ghi nhan', 'Khong co tien su dac biet', TRUE),
(120, 1120, 'PAT-2026-0120', 'Vo Van An 120', 'FEMALE', '1982-01-13', '0910000120', 'patient0120@evercare.test', '079000000120', 'BH-2026-000120', '220 Duong Mau 1, Phuong 1, TP HCM', 'Nguoi Than 120', '0980000120', 'A+', 'Di ung hai san', 'Tien su viem mui di ung', TRUE),
(121, 1121, 'PAT-2026-0121', 'Dang Thi Binh 121', 'MALE', '1983-02-14', '0910000121', 'patient0121@evercare.test', '079000000121', 'BH-2026-000121', '221 Duong Mau 2, Phuong 2, TP HCM', 'Nguoi Than 121', '0980000121', 'B+', 'Khong ghi nhan', 'Khong co tien su dac biet', TRUE),
(122, 1122, 'PAT-2026-0122', 'Bui Minh Chi 122', 'FEMALE', '1984-03-15', '0910000122', 'patient0122@evercare.test', '079000000122', 'BH-2026-000122', '222 Duong Mau 3, Phuong 3, TP HCM', 'Nguoi Than 122', '0980000122', 'O+', 'Khong ghi nhan', 'Khong co tien su dac biet', TRUE),
(123, 1123, 'PAT-2026-0123', 'Do Quoc Dung 123', 'MALE', '1985-04-16', '0910000123', 'patient0123@evercare.test', '079000000123', 'BH-2026-000123', '223 Duong Mau 4, Phuong 4, TP HCM', 'Nguoi Than 123', '0980000123', 'AB+', 'Khong ghi nhan', 'Khong co tien su dac biet', TRUE),
(124, 1124, 'PAT-2026-0124', 'Ho Thanh Giang 124', 'FEMALE', '1986-05-17', '0910000124', 'patient0124@evercare.test', '079000000124', 'BH-2026-000124', '224 Duong Mau 5, Phuong 5, TP HCM', 'Nguoi Than 124', '0980000124', 'A-', 'Khong ghi nhan', 'Tien su viem mui di ung', TRUE),
(125, 1125, 'PAT-2026-0125', 'Ngo Ngoc Ha 125', 'MALE', '1987-06-18', '0910000125', 'patient0125@evercare.test', '079000000125', 'BH-2026-000125', '225 Duong Mau 6, Phuong 6, TP HCM', 'Nguoi Than 125', '0980000125', 'B-', 'Di ung hai san', 'Khong co tien su dac biet', TRUE),
(126, 1126, 'PAT-2026-0126', 'Duong Anh Hung 126', 'FEMALE', '1988-07-19', '0910000126', 'patient0126@evercare.test', '079000000126', 'BH-2026-000126', '226 Duong Mau 7, Phuong 7, TP HCM', 'Nguoi Than 126', '0980000126', 'O-', 'Khong ghi nhan', 'Khong co tien su dac biet', TRUE),
(127, 1127, 'PAT-2026-0127', 'Ly Gia Khanh 127', 'MALE', '1989-08-20', '0910000127', 'patient0127@evercare.test', '079000000127', 'BH-2026-000127', '227 Duong Mau 8, Phuong 8, TP HCM', 'Nguoi Than 127', '0980000127', 'AB-', 'Khong ghi nhan', 'Khong co tien su dac biet', TRUE),
(128, 1128, 'PAT-2026-0128', 'Nguyen Duc Lan 128', 'FEMALE', '1990-09-21', '0910000128', 'patient0128@evercare.test', '079000000128', 'BH-2026-000128', '228 Duong Mau 9, Phuong 9, TP HCM', 'Nguoi Than 128', '0980000128', 'A+', 'Khong ghi nhan', 'Tien su viem mui di ung', TRUE),
(129, 1129, 'PAT-2026-0129', 'Tran Bao Long 129', 'MALE', '1991-10-22', '0910000129', 'patient0129@evercare.test', '079000000129', 'BH-2026-000129', '229 Duong Mau 10, Phuong 10, TP HCM', 'Nguoi Than 129', '0980000129', 'B+', 'Khong ghi nhan', 'Khong co tien su dac biet', TRUE),
(130, 1130, 'PAT-2026-0130', 'Le Van Mai 130', 'FEMALE', '1992-11-23', '0910000130', 'patient0130@evercare.test', '079000000130', 'BH-2026-000130', '230 Duong Mau 11, Phuong 11, TP HCM', 'Nguoi Than 130', '0980000130', 'O+', 'Di ung hai san', 'Khong co tien su dac biet', TRUE),
(131, 1131, 'PAT-2026-0131', 'Pham Thi Nam 131', 'MALE', '1993-12-24', '0910000131', 'patient0131@evercare.test', '079000000131', 'BH-2026-000131', '231 Duong Mau 12, Phuong 12, TP HCM', 'Nguoi Than 131', '0980000131', 'AB+', 'Khong ghi nhan', 'Khong co tien su dac biet', TRUE),
(132, 1132, 'PAT-2026-0132', 'Hoang Minh Phuc 132', 'FEMALE', '1994-01-25', '0910000132', 'patient0132@evercare.test', '079000000132', 'BH-2026-000132', '232 Duong Mau 13, Phuong 13, TP HCM', 'Nguoi Than 132', '0980000132', 'A-', 'Khong ghi nhan', 'Tien su viem mui di ung', TRUE),
(133, 1133, 'PAT-2026-0133', 'Huynh Quoc Quyen 133', 'MALE', '1995-02-26', '0910000133', 'patient0133@evercare.test', '079000000133', 'BH-2026-000133', '233 Duong Mau 14, Phuong 14, TP HCM', 'Nguoi Than 133', '0980000133', 'B-', 'Khong ghi nhan', 'Khong co tien su dac biet', TRUE),
(134, 1134, 'PAT-2026-0134', 'Phan Thanh Son 134', 'FEMALE', '1996-03-27', '0910000134', 'patient0134@evercare.test', '079000000134', 'BH-2026-000134', '234 Duong Mau 15, Phuong 15, TP HCM', 'Nguoi Than 134', '0980000134', 'O-', 'Khong ghi nhan', 'Khong co tien su dac biet', TRUE),
(135, 1135, 'PAT-2026-0135', 'Vu Ngoc Trang 135', 'MALE', '1997-04-01', '0910000135', 'patient0135@evercare.test', '079000000135', 'BH-2026-000135', '235 Duong Mau 16, Phuong 1, TP HCM', 'Nguoi Than 135', '0980000135', 'AB-', 'Di ung hai san', 'Khong co tien su dac biet', TRUE),
(136, 1136, 'PAT-2026-0136', 'Vo Anh Tuan 136', 'FEMALE', '1998-05-02', '0910000136', 'patient0136@evercare.test', '079000000136', 'BH-2026-000136', '236 Duong Mau 17, Phuong 2, TP HCM', 'Nguoi Than 136', '0980000136', 'A+', 'Khong ghi nhan', 'Tien su viem mui di ung', TRUE),
(137, 1137, 'PAT-2026-0137', 'Dang Gia Vy 137', 'MALE', '1999-06-03', '0910000137', 'patient0137@evercare.test', '079000000137', 'BH-2026-000137', '237 Duong Mau 18, Phuong 3, TP HCM', 'Nguoi Than 137', '0980000137', 'B+', 'Khong ghi nhan', 'Khong co tien su dac biet', TRUE),
(138, 1138, 'PAT-2026-0138', 'Bui Duc Yen 138', 'FEMALE', '2000-07-04', '0910000138', 'patient0138@evercare.test', '079000000138', 'BH-2026-000138', '238 Duong Mau 19, Phuong 4, TP HCM', 'Nguoi Than 138', '0980000138', 'O+', 'Khong ghi nhan', 'Khong co tien su dac biet', TRUE),
(139, 1139, 'PAT-2026-0139', 'Do Bao Nhi 139', 'MALE', '2001-08-05', '0910000139', 'patient0139@evercare.test', '079000000139', 'BH-2026-000139', '239 Duong Mau 20, Phuong 5, TP HCM', 'Nguoi Than 139', '0980000139', 'AB+', 'Khong ghi nhan', 'Khong co tien su dac biet', TRUE),
(140, 1140, 'PAT-2026-0140', 'Ho Van An 140', 'FEMALE', '2002-09-06', '0910000140', 'patient0140@evercare.test', '079000000140', 'BH-2026-000140', '240 Duong Mau 1, Phuong 6, TP HCM', 'Nguoi Than 140', '0980000140', 'A-', 'Di ung hai san', 'Tien su viem mui di ung', TRUE),
(141, 1141, 'PAT-2026-0141', 'Ngo Thi Binh 141', 'MALE', '2003-10-07', '0910000141', 'patient0141@evercare.test', '079000000141', 'BH-2026-000141', '241 Duong Mau 2, Phuong 7, TP HCM', 'Nguoi Than 141', '0980000141', 'B-', 'Khong ghi nhan', 'Khong co tien su dac biet', TRUE),
(142, 1142, 'PAT-2026-0142', 'Duong Minh Chi 142', 'FEMALE', '2004-11-08', '0910000142', 'patient0142@evercare.test', '079000000142', 'BH-2026-000142', '242 Duong Mau 3, Phuong 8, TP HCM', 'Nguoi Than 142', '0980000142', 'O-', 'Khong ghi nhan', 'Khong co tien su dac biet', TRUE),
(143, 1143, 'PAT-2026-0143', 'Ly Quoc Dung 143', 'MALE', '2005-12-09', '0910000143', 'patient0143@evercare.test', '079000000143', 'BH-2026-000143', '243 Duong Mau 4, Phuong 9, TP HCM', 'Nguoi Than 143', '0980000143', 'AB-', 'Khong ghi nhan', 'Khong co tien su dac biet', TRUE),
(144, 1144, 'PAT-2026-0144', 'Nguyen Thanh Giang 144', 'FEMALE', '1970-01-10', '0910000144', 'patient0144@evercare.test', '079000000144', 'BH-2026-000144', '244 Duong Mau 5, Phuong 10, TP HCM', 'Nguoi Than 144', '0980000144', 'A+', 'Khong ghi nhan', 'Tien su viem mui di ung', TRUE),
(145, 1145, 'PAT-2026-0145', 'Tran Ngoc Ha 145', 'MALE', '1971-02-11', '0910000145', 'patient0145@evercare.test', '079000000145', 'BH-2026-000145', '245 Duong Mau 6, Phuong 11, TP HCM', 'Nguoi Than 145', '0980000145', 'B+', 'Di ung hai san', 'Khong co tien su dac biet', TRUE),
(146, 1146, 'PAT-2026-0146', 'Le Anh Hung 146', 'FEMALE', '1972-03-12', '0910000146', 'patient0146@evercare.test', '079000000146', 'BH-2026-000146', '246 Duong Mau 7, Phuong 12, TP HCM', 'Nguoi Than 146', '0980000146', 'O+', 'Khong ghi nhan', 'Khong co tien su dac biet', TRUE),
(147, 1147, 'PAT-2026-0147', 'Pham Gia Khanh 147', 'MALE', '1973-04-13', '0910000147', 'patient0147@evercare.test', '079000000147', 'BH-2026-000147', '247 Duong Mau 8, Phuong 13, TP HCM', 'Nguoi Than 147', '0980000147', 'AB+', 'Khong ghi nhan', 'Khong co tien su dac biet', TRUE),
(148, 1148, 'PAT-2026-0148', 'Hoang Duc Lan 148', 'FEMALE', '1974-05-14', '0910000148', 'patient0148@evercare.test', '079000000148', 'BH-2026-000148', '248 Duong Mau 9, Phuong 14, TP HCM', 'Nguoi Than 148', '0980000148', 'A-', 'Khong ghi nhan', 'Tien su viem mui di ung', TRUE),
(149, 1149, 'PAT-2026-0149', 'Huynh Bao Long 149', 'MALE', '1975-06-15', '0910000149', 'patient0149@evercare.test', '079000000149', 'BH-2026-000149', '249 Duong Mau 10, Phuong 15, TP HCM', 'Nguoi Than 149', '0980000149', 'B-', 'Khong ghi nhan', 'Khong co tien su dac biet', TRUE),
(150, 1150, 'PAT-2026-0150', 'Phan Van Mai 150', 'FEMALE', '1976-07-16', '0910000150', 'patient0150@evercare.test', '079000000150', 'BH-2026-000150', '250 Duong Mau 11, Phuong 1, TP HCM', 'Nguoi Than 150', '0980000150', 'O-', 'Di ung hai san', 'Khong co tien su dac biet', TRUE),
(151, 1151, 'PAT-2026-0151', 'Vu Thi Nam 151', 'MALE', '1977-08-17', '0910000151', 'patient0151@evercare.test', '079000000151', 'BH-2026-000151', '251 Duong Mau 12, Phuong 2, TP HCM', 'Nguoi Than 151', '0980000151', 'AB-', 'Khong ghi nhan', 'Khong co tien su dac biet', TRUE),
(152, 1152, 'PAT-2026-0152', 'Vo Minh Phuc 152', 'FEMALE', '1978-09-18', '0910000152', 'patient0152@evercare.test', '079000000152', 'BH-2026-000152', '252 Duong Mau 13, Phuong 3, TP HCM', 'Nguoi Than 152', '0980000152', 'A+', 'Khong ghi nhan', 'Tien su viem mui di ung', TRUE),
(153, 1153, 'PAT-2026-0153', 'Dang Quoc Quyen 153', 'MALE', '1979-10-19', '0910000153', 'patient0153@evercare.test', '079000000153', 'BH-2026-000153', '253 Duong Mau 14, Phuong 4, TP HCM', 'Nguoi Than 153', '0980000153', 'B+', 'Khong ghi nhan', 'Khong co tien su dac biet', TRUE),
(154, 1154, 'PAT-2026-0154', 'Bui Thanh Son 154', 'FEMALE', '1980-11-20', '0910000154', 'patient0154@evercare.test', '079000000154', 'BH-2026-000154', '254 Duong Mau 15, Phuong 5, TP HCM', 'Nguoi Than 154', '0980000154', 'O+', 'Khong ghi nhan', 'Khong co tien su dac biet', TRUE),
(155, 1155, 'PAT-2026-0155', 'Do Ngoc Trang 155', 'MALE', '1981-12-21', '0910000155', 'patient0155@evercare.test', '079000000155', 'BH-2026-000155', '255 Duong Mau 16, Phuong 6, TP HCM', 'Nguoi Than 155', '0980000155', 'AB+', 'Di ung hai san', 'Khong co tien su dac biet', TRUE),
(156, 1156, 'PAT-2026-0156', 'Ho Anh Tuan 156', 'FEMALE', '1982-01-22', '0910000156', 'patient0156@evercare.test', '079000000156', 'BH-2026-000156', '256 Duong Mau 17, Phuong 7, TP HCM', 'Nguoi Than 156', '0980000156', 'A-', 'Khong ghi nhan', 'Tien su viem mui di ung', TRUE),
(157, 1157, 'PAT-2026-0157', 'Ngo Gia Vy 157', 'MALE', '1983-02-23', '0910000157', 'patient0157@evercare.test', '079000000157', 'BH-2026-000157', '257 Duong Mau 18, Phuong 8, TP HCM', 'Nguoi Than 157', '0980000157', 'B-', 'Khong ghi nhan', 'Khong co tien su dac biet', TRUE),
(158, 1158, 'PAT-2026-0158', 'Duong Duc Yen 158', 'FEMALE', '1984-03-24', '0910000158', 'patient0158@evercare.test', '079000000158', 'BH-2026-000158', '258 Duong Mau 19, Phuong 9, TP HCM', 'Nguoi Than 158', '0980000158', 'O-', 'Khong ghi nhan', 'Khong co tien su dac biet', TRUE),
(159, 1159, 'PAT-2026-0159', 'Ly Bao Nhi 159', 'MALE', '1985-04-25', '0910000159', 'patient0159@evercare.test', '079000000159', 'BH-2026-000159', '259 Duong Mau 20, Phuong 10, TP HCM', 'Nguoi Than 159', '0980000159', 'AB-', 'Khong ghi nhan', 'Khong co tien su dac biet', TRUE),
(160, 1160, 'PAT-2026-0160', 'Nguyen Van An 160', 'FEMALE', '1986-05-26', '0910000160', 'patient0160@evercare.test', '079000000160', 'BH-2026-000160', '260 Duong Mau 1, Phuong 11, TP HCM', 'Nguoi Than 160', '0980000160', 'A+', 'Di ung hai san', 'Tien su viem mui di ung', TRUE),
(161, 1161, 'PAT-2026-0161', 'Tran Thi Binh 161', 'MALE', '1987-06-27', '0910000161', 'patient0161@evercare.test', '079000000161', 'BH-2026-000161', '261 Duong Mau 2, Phuong 12, TP HCM', 'Nguoi Than 161', '0980000161', 'B+', 'Khong ghi nhan', 'Khong co tien su dac biet', TRUE),
(162, 1162, 'PAT-2026-0162', 'Le Minh Chi 162', 'FEMALE', '1988-07-01', '0910000162', 'patient0162@evercare.test', '079000000162', 'BH-2026-000162', '262 Duong Mau 3, Phuong 13, TP HCM', 'Nguoi Than 162', '0980000162', 'O+', 'Khong ghi nhan', 'Khong co tien su dac biet', TRUE),
(163, 1163, 'PAT-2026-0163', 'Pham Quoc Dung 163', 'MALE', '1989-08-02', '0910000163', 'patient0163@evercare.test', '079000000163', 'BH-2026-000163', '263 Duong Mau 4, Phuong 14, TP HCM', 'Nguoi Than 163', '0980000163', 'AB+', 'Khong ghi nhan', 'Khong co tien su dac biet', TRUE),
(164, 1164, 'PAT-2026-0164', 'Hoang Thanh Giang 164', 'FEMALE', '1990-09-03', '0910000164', 'patient0164@evercare.test', '079000000164', 'BH-2026-000164', '264 Duong Mau 5, Phuong 15, TP HCM', 'Nguoi Than 164', '0980000164', 'A-', 'Khong ghi nhan', 'Tien su viem mui di ung', TRUE),
(165, 1165, 'PAT-2026-0165', 'Huynh Ngoc Ha 165', 'MALE', '1991-10-04', '0910000165', 'patient0165@evercare.test', '079000000165', 'BH-2026-000165', '265 Duong Mau 6, Phuong 1, TP HCM', 'Nguoi Than 165', '0980000165', 'B-', 'Di ung hai san', 'Khong co tien su dac biet', TRUE),
(166, 1166, 'PAT-2026-0166', 'Phan Anh Hung 166', 'FEMALE', '1992-11-05', '0910000166', 'patient0166@evercare.test', '079000000166', 'BH-2026-000166', '266 Duong Mau 7, Phuong 2, TP HCM', 'Nguoi Than 166', '0980000166', 'O-', 'Khong ghi nhan', 'Khong co tien su dac biet', TRUE),
(167, 1167, 'PAT-2026-0167', 'Vu Gia Khanh 167', 'MALE', '1993-12-06', '0910000167', 'patient0167@evercare.test', '079000000167', 'BH-2026-000167', '267 Duong Mau 8, Phuong 3, TP HCM', 'Nguoi Than 167', '0980000167', 'AB-', 'Khong ghi nhan', 'Khong co tien su dac biet', TRUE),
(168, 1168, 'PAT-2026-0168', 'Vo Duc Lan 168', 'FEMALE', '1994-01-07', '0910000168', 'patient0168@evercare.test', '079000000168', 'BH-2026-000168', '268 Duong Mau 9, Phuong 4, TP HCM', 'Nguoi Than 168', '0980000168', 'A+', 'Khong ghi nhan', 'Tien su viem mui di ung', TRUE),
(169, 1169, 'PAT-2026-0169', 'Dang Bao Long 169', 'MALE', '1995-02-08', '0910000169', 'patient0169@evercare.test', '079000000169', 'BH-2026-000169', '269 Duong Mau 10, Phuong 5, TP HCM', 'Nguoi Than 169', '0980000169', 'B+', 'Khong ghi nhan', 'Khong co tien su dac biet', TRUE),
(170, 1170, 'PAT-2026-0170', 'Bui Van Mai 170', 'FEMALE', '1996-03-09', '0910000170', 'patient0170@evercare.test', '079000000170', 'BH-2026-000170', '270 Duong Mau 11, Phuong 6, TP HCM', 'Nguoi Than 170', '0980000170', 'O+', 'Di ung hai san', 'Khong co tien su dac biet', TRUE),
(171, 1171, 'PAT-2026-0171', 'Do Thi Nam 171', 'MALE', '1997-04-10', '0910000171', 'patient0171@evercare.test', '079000000171', 'BH-2026-000171', '271 Duong Mau 12, Phuong 7, TP HCM', 'Nguoi Than 171', '0980000171', 'AB+', 'Khong ghi nhan', 'Khong co tien su dac biet', TRUE),
(172, 1172, 'PAT-2026-0172', 'Ho Minh Phuc 172', 'FEMALE', '1998-05-11', '0910000172', 'patient0172@evercare.test', '079000000172', 'BH-2026-000172', '272 Duong Mau 13, Phuong 8, TP HCM', 'Nguoi Than 172', '0980000172', 'A-', 'Khong ghi nhan', 'Tien su viem mui di ung', TRUE),
(173, 1173, 'PAT-2026-0173', 'Ngo Quoc Quyen 173', 'MALE', '1999-06-12', '0910000173', 'patient0173@evercare.test', '079000000173', 'BH-2026-000173', '273 Duong Mau 14, Phuong 9, TP HCM', 'Nguoi Than 173', '0980000173', 'B-', 'Khong ghi nhan', 'Khong co tien su dac biet', TRUE),
(174, 1174, 'PAT-2026-0174', 'Duong Thanh Son 174', 'FEMALE', '2000-07-13', '0910000174', 'patient0174@evercare.test', '079000000174', 'BH-2026-000174', '274 Duong Mau 15, Phuong 10, TP HCM', 'Nguoi Than 174', '0980000174', 'O-', 'Khong ghi nhan', 'Khong co tien su dac biet', TRUE),
(175, 1175, 'PAT-2026-0175', 'Ly Ngoc Trang 175', 'MALE', '2001-08-14', '0910000175', 'patient0175@evercare.test', '079000000175', 'BH-2026-000175', '275 Duong Mau 16, Phuong 11, TP HCM', 'Nguoi Than 175', '0980000175', 'AB-', 'Di ung hai san', 'Khong co tien su dac biet', TRUE),
(176, 1176, 'PAT-2026-0176', 'Nguyen Anh Tuan 176', 'FEMALE', '2002-09-15', '0910000176', 'patient0176@evercare.test', '079000000176', 'BH-2026-000176', '276 Duong Mau 17, Phuong 12, TP HCM', 'Nguoi Than 176', '0980000176', 'A+', 'Khong ghi nhan', 'Tien su viem mui di ung', TRUE),
(177, 1177, 'PAT-2026-0177', 'Tran Gia Vy 177', 'MALE', '2003-10-16', '0910000177', 'patient0177@evercare.test', '079000000177', 'BH-2026-000177', '277 Duong Mau 18, Phuong 13, TP HCM', 'Nguoi Than 177', '0980000177', 'B+', 'Khong ghi nhan', 'Khong co tien su dac biet', TRUE),
(178, 1178, 'PAT-2026-0178', 'Le Duc Yen 178', 'FEMALE', '2004-11-17', '0910000178', 'patient0178@evercare.test', '079000000178', 'BH-2026-000178', '278 Duong Mau 19, Phuong 14, TP HCM', 'Nguoi Than 178', '0980000178', 'O+', 'Khong ghi nhan', 'Khong co tien su dac biet', TRUE),
(179, 1179, 'PAT-2026-0179', 'Pham Bao Nhi 179', 'MALE', '2005-12-18', '0910000179', 'patient0179@evercare.test', '079000000179', 'BH-2026-000179', '279 Duong Mau 20, Phuong 15, TP HCM', 'Nguoi Than 179', '0980000179', 'AB+', 'Khong ghi nhan', 'Khong co tien su dac biet', TRUE),
(180, 1180, 'PAT-2026-0180', 'Hoang Van An 180', 'FEMALE', '1970-01-19', '0910000180', 'patient0180@evercare.test', '079000000180', 'BH-2026-000180', '280 Duong Mau 1, Phuong 1, TP HCM', 'Nguoi Than 180', '0980000180', 'A-', 'Di ung hai san', 'Tien su viem mui di ung', TRUE),
(181, 1181, 'PAT-2026-0181', 'Huynh Thi Binh 181', 'MALE', '1971-02-20', '0910000181', 'patient0181@evercare.test', '079000000181', 'BH-2026-000181', '281 Duong Mau 2, Phuong 2, TP HCM', 'Nguoi Than 181', '0980000181', 'B-', 'Khong ghi nhan', 'Khong co tien su dac biet', TRUE),
(182, 1182, 'PAT-2026-0182', 'Phan Minh Chi 182', 'FEMALE', '1972-03-21', '0910000182', 'patient0182@evercare.test', '079000000182', 'BH-2026-000182', '282 Duong Mau 3, Phuong 3, TP HCM', 'Nguoi Than 182', '0980000182', 'O-', 'Khong ghi nhan', 'Khong co tien su dac biet', TRUE),
(183, 1183, 'PAT-2026-0183', 'Vu Quoc Dung 183', 'MALE', '1973-04-22', '0910000183', 'patient0183@evercare.test', '079000000183', 'BH-2026-000183', '283 Duong Mau 4, Phuong 4, TP HCM', 'Nguoi Than 183', '0980000183', 'AB-', 'Khong ghi nhan', 'Khong co tien su dac biet', TRUE),
(184, 1184, 'PAT-2026-0184', 'Vo Thanh Giang 184', 'FEMALE', '1974-05-23', '0910000184', 'patient0184@evercare.test', '079000000184', 'BH-2026-000184', '284 Duong Mau 5, Phuong 5, TP HCM', 'Nguoi Than 184', '0980000184', 'A+', 'Khong ghi nhan', 'Tien su viem mui di ung', TRUE),
(185, 1185, 'PAT-2026-0185', 'Dang Ngoc Ha 185', 'MALE', '1975-06-24', '0910000185', 'patient0185@evercare.test', '079000000185', 'BH-2026-000185', '285 Duong Mau 6, Phuong 6, TP HCM', 'Nguoi Than 185', '0980000185', 'B+', 'Di ung hai san', 'Khong co tien su dac biet', TRUE),
(186, 1186, 'PAT-2026-0186', 'Bui Anh Hung 186', 'FEMALE', '1976-07-25', '0910000186', 'patient0186@evercare.test', '079000000186', 'BH-2026-000186', '286 Duong Mau 7, Phuong 7, TP HCM', 'Nguoi Than 186', '0980000186', 'O+', 'Khong ghi nhan', 'Khong co tien su dac biet', TRUE),
(187, 1187, 'PAT-2026-0187', 'Do Gia Khanh 187', 'MALE', '1977-08-26', '0910000187', 'patient0187@evercare.test', '079000000187', 'BH-2026-000187', '287 Duong Mau 8, Phuong 8, TP HCM', 'Nguoi Than 187', '0980000187', 'AB+', 'Khong ghi nhan', 'Khong co tien su dac biet', TRUE),
(188, 1188, 'PAT-2026-0188', 'Ho Duc Lan 188', 'FEMALE', '1978-09-27', '0910000188', 'patient0188@evercare.test', '079000000188', 'BH-2026-000188', '288 Duong Mau 9, Phuong 9, TP HCM', 'Nguoi Than 188', '0980000188', 'A-', 'Khong ghi nhan', 'Tien su viem mui di ung', TRUE),
(189, 1189, 'PAT-2026-0189', 'Ngo Bao Long 189', 'MALE', '1979-10-01', '0910000189', 'patient0189@evercare.test', '079000000189', 'BH-2026-000189', '289 Duong Mau 10, Phuong 10, TP HCM', 'Nguoi Than 189', '0980000189', 'B-', 'Khong ghi nhan', 'Khong co tien su dac biet', TRUE),
(190, 1190, 'PAT-2026-0190', 'Duong Van Mai 190', 'FEMALE', '1980-11-02', '0910000190', 'patient0190@evercare.test', '079000000190', 'BH-2026-000190', '290 Duong Mau 11, Phuong 11, TP HCM', 'Nguoi Than 190', '0980000190', 'O-', 'Di ung hai san', 'Khong co tien su dac biet', TRUE),
(191, 1191, 'PAT-2026-0191', 'Ly Thi Nam 191', 'MALE', '1981-12-03', '0910000191', 'patient0191@evercare.test', '079000000191', 'BH-2026-000191', '291 Duong Mau 12, Phuong 12, TP HCM', 'Nguoi Than 191', '0980000191', 'AB-', 'Khong ghi nhan', 'Khong co tien su dac biet', TRUE),
(192, 1192, 'PAT-2026-0192', 'Nguyen Minh Phuc 192', 'FEMALE', '1982-01-04', '0910000192', 'patient0192@evercare.test', '079000000192', 'BH-2026-000192', '292 Duong Mau 13, Phuong 13, TP HCM', 'Nguoi Than 192', '0980000192', 'A+', 'Khong ghi nhan', 'Tien su viem mui di ung', TRUE),
(193, 1193, 'PAT-2026-0193', 'Tran Quoc Quyen 193', 'MALE', '1983-02-05', '0910000193', 'patient0193@evercare.test', '079000000193', 'BH-2026-000193', '293 Duong Mau 14, Phuong 14, TP HCM', 'Nguoi Than 193', '0980000193', 'B+', 'Khong ghi nhan', 'Khong co tien su dac biet', TRUE),
(194, 1194, 'PAT-2026-0194', 'Le Thanh Son 194', 'FEMALE', '1984-03-06', '0910000194', 'patient0194@evercare.test', '079000000194', 'BH-2026-000194', '294 Duong Mau 15, Phuong 15, TP HCM', 'Nguoi Than 194', '0980000194', 'O+', 'Khong ghi nhan', 'Khong co tien su dac biet', TRUE),
(195, 1195, 'PAT-2026-0195', 'Pham Ngoc Trang 195', 'MALE', '1985-04-07', '0910000195', 'patient0195@evercare.test', '079000000195', 'BH-2026-000195', '295 Duong Mau 16, Phuong 1, TP HCM', 'Nguoi Than 195', '0980000195', 'AB+', 'Di ung hai san', 'Khong co tien su dac biet', TRUE),
(196, 1196, 'PAT-2026-0196', 'Hoang Anh Tuan 196', 'FEMALE', '1986-05-08', '0910000196', 'patient0196@evercare.test', '079000000196', 'BH-2026-000196', '296 Duong Mau 17, Phuong 2, TP HCM', 'Nguoi Than 196', '0980000196', 'A-', 'Khong ghi nhan', 'Tien su viem mui di ung', TRUE),
(197, 1197, 'PAT-2026-0197', 'Huynh Gia Vy 197', 'MALE', '1987-06-09', '0910000197', 'patient0197@evercare.test', '079000000197', 'BH-2026-000197', '297 Duong Mau 18, Phuong 3, TP HCM', 'Nguoi Than 197', '0980000197', 'B-', 'Khong ghi nhan', 'Khong co tien su dac biet', TRUE),
(198, 1198, 'PAT-2026-0198', 'Phan Duc Yen 198', 'FEMALE', '1988-07-10', '0910000198', 'patient0198@evercare.test', '079000000198', 'BH-2026-000198', '298 Duong Mau 19, Phuong 4, TP HCM', 'Nguoi Than 198', '0980000198', 'O-', 'Khong ghi nhan', 'Khong co tien su dac biet', TRUE),
(199, 1199, 'PAT-2026-0199', 'Vu Bao Nhi 199', 'MALE', '1989-08-11', '0910000199', 'patient0199@evercare.test', '079000000199', 'BH-2026-000199', '299 Duong Mau 20, Phuong 5, TP HCM', 'Nguoi Than 199', '0980000199', 'AB-', 'Khong ghi nhan', 'Khong co tien su dac biet', TRUE),
(200, 1200, 'PAT-2026-0200', 'Vo Van An 200', 'FEMALE', '1990-09-12', '0910000200', 'patient0200@evercare.test', '079000000200', 'BH-2026-000200', '300 Duong Mau 1, Phuong 6, TP HCM', 'Nguoi Than 200', '0980000200', 'A+', 'Di ung hai san', 'Tien su viem mui di ung', TRUE),
(201, 1201, 'PAT-2026-0201', 'Dang Thi Binh 201', 'MALE', '1991-10-13', '0910000201', 'patient0201@evercare.test', '079000000201', 'BH-2026-000201', '301 Duong Mau 2, Phuong 7, TP HCM', 'Nguoi Than 201', '0980000201', 'B+', 'Khong ghi nhan', 'Khong co tien su dac biet', TRUE),
(202, 1202, 'PAT-2026-0202', 'Bui Minh Chi 202', 'FEMALE', '1992-11-14', '0910000202', 'patient0202@evercare.test', '079000000202', 'BH-2026-000202', '302 Duong Mau 3, Phuong 8, TP HCM', 'Nguoi Than 202', '0980000202', 'O+', 'Khong ghi nhan', 'Khong co tien su dac biet', TRUE),
(203, 1203, 'PAT-2026-0203', 'Do Quoc Dung 203', 'MALE', '1993-12-15', '0910000203', 'patient0203@evercare.test', '079000000203', 'BH-2026-000203', '303 Duong Mau 4, Phuong 9, TP HCM', 'Nguoi Than 203', '0980000203', 'AB+', 'Khong ghi nhan', 'Khong co tien su dac biet', TRUE),
(204, 1204, 'PAT-2026-0204', 'Ho Thanh Giang 204', 'FEMALE', '1994-01-16', '0910000204', 'patient0204@evercare.test', '079000000204', 'BH-2026-000204', '304 Duong Mau 5, Phuong 10, TP HCM', 'Nguoi Than 204', '0980000204', 'A-', 'Khong ghi nhan', 'Tien su viem mui di ung', TRUE),
(205, 1205, 'PAT-2026-0205', 'Ngo Ngoc Ha 205', 'MALE', '1995-02-17', '0910000205', 'patient0205@evercare.test', '079000000205', 'BH-2026-000205', '305 Duong Mau 6, Phuong 11, TP HCM', 'Nguoi Than 205', '0980000205', 'B-', 'Di ung hai san', 'Khong co tien su dac biet', TRUE),
(206, 1206, 'PAT-2026-0206', 'Duong Anh Hung 206', 'FEMALE', '1996-03-18', '0910000206', 'patient0206@evercare.test', '079000000206', 'BH-2026-000206', '306 Duong Mau 7, Phuong 12, TP HCM', 'Nguoi Than 206', '0980000206', 'O-', 'Khong ghi nhan', 'Khong co tien su dac biet', TRUE),
(207, 1207, 'PAT-2026-0207', 'Ly Gia Khanh 207', 'MALE', '1997-04-19', '0910000207', 'patient0207@evercare.test', '079000000207', 'BH-2026-000207', '307 Duong Mau 8, Phuong 13, TP HCM', 'Nguoi Than 207', '0980000207', 'AB-', 'Khong ghi nhan', 'Khong co tien su dac biet', TRUE),
(208, 1208, 'PAT-2026-0208', 'Nguyen Duc Lan 208', 'FEMALE', '1998-05-20', '0910000208', 'patient0208@evercare.test', '079000000208', 'BH-2026-000208', '308 Duong Mau 9, Phuong 14, TP HCM', 'Nguoi Than 208', '0980000208', 'A+', 'Khong ghi nhan', 'Tien su viem mui di ung', TRUE),
(209, 1209, 'PAT-2026-0209', 'Tran Bao Long 209', 'MALE', '1999-06-21', '0910000209', 'patient0209@evercare.test', '079000000209', 'BH-2026-000209', '309 Duong Mau 10, Phuong 15, TP HCM', 'Nguoi Than 209', '0980000209', 'B+', 'Khong ghi nhan', 'Khong co tien su dac biet', TRUE),
(210, 1210, 'PAT-2026-0210', 'Le Van Mai 210', 'FEMALE', '2000-07-22', '0910000210', 'patient0210@evercare.test', '079000000210', 'BH-2026-000210', '310 Duong Mau 11, Phuong 1, TP HCM', 'Nguoi Than 210', '0980000210', 'O+', 'Di ung hai san', 'Khong co tien su dac biet', TRUE),
(211, 1211, 'PAT-2026-0211', 'Pham Thi Nam 211', 'MALE', '2001-08-23', '0910000211', 'patient0211@evercare.test', '079000000211', 'BH-2026-000211', '311 Duong Mau 12, Phuong 2, TP HCM', 'Nguoi Than 211', '0980000211', 'AB+', 'Khong ghi nhan', 'Khong co tien su dac biet', TRUE),
(212, 1212, 'PAT-2026-0212', 'Hoang Minh Phuc 212', 'FEMALE', '2002-09-24', '0910000212', 'patient0212@evercare.test', '079000000212', 'BH-2026-000212', '312 Duong Mau 13, Phuong 3, TP HCM', 'Nguoi Than 212', '0980000212', 'A-', 'Khong ghi nhan', 'Tien su viem mui di ung', TRUE),
(213, 1213, 'PAT-2026-0213', 'Huynh Quoc Quyen 213', 'MALE', '2003-10-25', '0910000213', 'patient0213@evercare.test', '079000000213', 'BH-2026-000213', '313 Duong Mau 14, Phuong 4, TP HCM', 'Nguoi Than 213', '0980000213', 'B-', 'Khong ghi nhan', 'Khong co tien su dac biet', TRUE),
(214, 1214, 'PAT-2026-0214', 'Phan Thanh Son 214', 'FEMALE', '2004-11-26', '0910000214', 'patient0214@evercare.test', '079000000214', 'BH-2026-000214', '314 Duong Mau 15, Phuong 5, TP HCM', 'Nguoi Than 214', '0980000214', 'O-', 'Khong ghi nhan', 'Khong co tien su dac biet', TRUE),
(215, 1215, 'PAT-2026-0215', 'Vu Ngoc Trang 215', 'MALE', '2005-12-27', '0910000215', 'patient0215@evercare.test', '079000000215', 'BH-2026-000215', '315 Duong Mau 16, Phuong 6, TP HCM', 'Nguoi Than 215', '0980000215', 'AB-', 'Di ung hai san', 'Khong co tien su dac biet', TRUE),
(216, 1216, 'PAT-2026-0216', 'Vo Anh Tuan 216', 'FEMALE', '1970-01-01', '0910000216', 'patient0216@evercare.test', '079000000216', 'BH-2026-000216', '316 Duong Mau 17, Phuong 7, TP HCM', 'Nguoi Than 216', '0980000216', 'A+', 'Khong ghi nhan', 'Tien su viem mui di ung', TRUE),
(217, 1217, 'PAT-2026-0217', 'Dang Gia Vy 217', 'MALE', '1971-02-02', '0910000217', 'patient0217@evercare.test', '079000000217', 'BH-2026-000217', '317 Duong Mau 18, Phuong 8, TP HCM', 'Nguoi Than 217', '0980000217', 'B+', 'Khong ghi nhan', 'Khong co tien su dac biet', TRUE),
(218, 1218, 'PAT-2026-0218', 'Bui Duc Yen 218', 'FEMALE', '1972-03-03', '0910000218', 'patient0218@evercare.test', '079000000218', 'BH-2026-000218', '318 Duong Mau 19, Phuong 9, TP HCM', 'Nguoi Than 218', '0980000218', 'O+', 'Khong ghi nhan', 'Khong co tien su dac biet', TRUE),
(219, 1219, 'PAT-2026-0219', 'Do Bao Nhi 219', 'MALE', '1973-04-04', '0910000219', 'patient0219@evercare.test', '079000000219', 'BH-2026-000219', '319 Duong Mau 20, Phuong 10, TP HCM', 'Nguoi Than 219', '0980000219', 'AB+', 'Khong ghi nhan', 'Khong co tien su dac biet', TRUE),
(220, 1220, 'PAT-2026-0220', 'Ho Van An 220', 'FEMALE', '1974-05-05', '0910000220', 'patient0220@evercare.test', '079000000220', 'BH-2026-000220', '320 Duong Mau 1, Phuong 11, TP HCM', 'Nguoi Than 220', '0980000220', 'A-', 'Di ung hai san', 'Tien su viem mui di ung', TRUE),
(221, 1221, 'PAT-2026-0221', 'Ngo Thi Binh 221', 'MALE', '1975-06-06', '0910000221', 'patient0221@evercare.test', '079000000221', 'BH-2026-000221', '321 Duong Mau 2, Phuong 12, TP HCM', 'Nguoi Than 221', '0980000221', 'B-', 'Khong ghi nhan', 'Khong co tien su dac biet', TRUE),
(222, 1222, 'PAT-2026-0222', 'Duong Minh Chi 222', 'FEMALE', '1976-07-07', '0910000222', 'patient0222@evercare.test', '079000000222', 'BH-2026-000222', '322 Duong Mau 3, Phuong 13, TP HCM', 'Nguoi Than 222', '0980000222', 'O-', 'Khong ghi nhan', 'Khong co tien su dac biet', TRUE),
(223, 1223, 'PAT-2026-0223', 'Ly Quoc Dung 223', 'MALE', '1977-08-08', '0910000223', 'patient0223@evercare.test', '079000000223', 'BH-2026-000223', '323 Duong Mau 4, Phuong 14, TP HCM', 'Nguoi Than 223', '0980000223', 'AB-', 'Khong ghi nhan', 'Khong co tien su dac biet', TRUE),
(224, 1224, 'PAT-2026-0224', 'Nguyen Thanh Giang 224', 'FEMALE', '1978-09-09', '0910000224', 'patient0224@evercare.test', '079000000224', 'BH-2026-000224', '324 Duong Mau 5, Phuong 15, TP HCM', 'Nguoi Than 224', '0980000224', 'A+', 'Khong ghi nhan', 'Tien su viem mui di ung', TRUE),
(225, 1225, 'PAT-2026-0225', 'Tran Ngoc Ha 225', 'MALE', '1979-10-10', '0910000225', 'patient0225@evercare.test', '079000000225', 'BH-2026-000225', '325 Duong Mau 6, Phuong 1, TP HCM', 'Nguoi Than 225', '0980000225', 'B+', 'Di ung hai san', 'Khong co tien su dac biet', TRUE),
(226, 1226, 'PAT-2026-0226', 'Le Anh Hung 226', 'FEMALE', '1980-11-11', '0910000226', 'patient0226@evercare.test', '079000000226', 'BH-2026-000226', '326 Duong Mau 7, Phuong 2, TP HCM', 'Nguoi Than 226', '0980000226', 'O+', 'Khong ghi nhan', 'Khong co tien su dac biet', TRUE),
(227, 1227, 'PAT-2026-0227', 'Pham Gia Khanh 227', 'MALE', '1981-12-12', '0910000227', 'patient0227@evercare.test', '079000000227', 'BH-2026-000227', '327 Duong Mau 8, Phuong 3, TP HCM', 'Nguoi Than 227', '0980000227', 'AB+', 'Khong ghi nhan', 'Khong co tien su dac biet', TRUE),
(228, 1228, 'PAT-2026-0228', 'Hoang Duc Lan 228', 'FEMALE', '1982-01-13', '0910000228', 'patient0228@evercare.test', '079000000228', 'BH-2026-000228', '328 Duong Mau 9, Phuong 4, TP HCM', 'Nguoi Than 228', '0980000228', 'A-', 'Khong ghi nhan', 'Tien su viem mui di ung', TRUE),
(229, 1229, 'PAT-2026-0229', 'Huynh Bao Long 229', 'MALE', '1983-02-14', '0910000229', 'patient0229@evercare.test', '079000000229', 'BH-2026-000229', '329 Duong Mau 10, Phuong 5, TP HCM', 'Nguoi Than 229', '0980000229', 'B-', 'Khong ghi nhan', 'Khong co tien su dac biet', TRUE),
(230, 1230, 'PAT-2026-0230', 'Phan Van Mai 230', 'FEMALE', '1984-03-15', '0910000230', 'patient0230@evercare.test', '079000000230', 'BH-2026-000230', '330 Duong Mau 11, Phuong 6, TP HCM', 'Nguoi Than 230', '0980000230', 'O-', 'Di ung hai san', 'Khong co tien su dac biet', TRUE),
(231, 1231, 'PAT-2026-0231', 'Vu Thi Nam 231', 'MALE', '1985-04-16', '0910000231', 'patient0231@evercare.test', '079000000231', 'BH-2026-000231', '331 Duong Mau 12, Phuong 7, TP HCM', 'Nguoi Than 231', '0980000231', 'AB-', 'Khong ghi nhan', 'Khong co tien su dac biet', TRUE),
(232, 1232, 'PAT-2026-0232', 'Vo Minh Phuc 232', 'FEMALE', '1986-05-17', '0910000232', 'patient0232@evercare.test', '079000000232', 'BH-2026-000232', '332 Duong Mau 13, Phuong 8, TP HCM', 'Nguoi Than 232', '0980000232', 'A+', 'Khong ghi nhan', 'Tien su viem mui di ung', TRUE),
(233, 1233, 'PAT-2026-0233', 'Dang Quoc Quyen 233', 'MALE', '1987-06-18', '0910000233', 'patient0233@evercare.test', '079000000233', 'BH-2026-000233', '333 Duong Mau 14, Phuong 9, TP HCM', 'Nguoi Than 233', '0980000233', 'B+', 'Khong ghi nhan', 'Khong co tien su dac biet', TRUE),
(234, 1234, 'PAT-2026-0234', 'Bui Thanh Son 234', 'FEMALE', '1988-07-19', '0910000234', 'patient0234@evercare.test', '079000000234', 'BH-2026-000234', '334 Duong Mau 15, Phuong 10, TP HCM', 'Nguoi Than 234', '0980000234', 'O+', 'Khong ghi nhan', 'Khong co tien su dac biet', TRUE),
(235, 1235, 'PAT-2026-0235', 'Do Ngoc Trang 235', 'MALE', '1989-08-20', '0910000235', 'patient0235@evercare.test', '079000000235', 'BH-2026-000235', '335 Duong Mau 16, Phuong 11, TP HCM', 'Nguoi Than 235', '0980000235', 'AB+', 'Di ung hai san', 'Khong co tien su dac biet', TRUE),
(236, 1236, 'PAT-2026-0236', 'Ho Anh Tuan 236', 'FEMALE', '1990-09-21', '0910000236', 'patient0236@evercare.test', '079000000236', 'BH-2026-000236', '336 Duong Mau 17, Phuong 12, TP HCM', 'Nguoi Than 236', '0980000236', 'A-', 'Khong ghi nhan', 'Tien su viem mui di ung', TRUE),
(237, 1237, 'PAT-2026-0237', 'Ngo Gia Vy 237', 'MALE', '1991-10-22', '0910000237', 'patient0237@evercare.test', '079000000237', 'BH-2026-000237', '337 Duong Mau 18, Phuong 13, TP HCM', 'Nguoi Than 237', '0980000237', 'B-', 'Khong ghi nhan', 'Khong co tien su dac biet', TRUE),
(238, 1238, 'PAT-2026-0238', 'Duong Duc Yen 238', 'FEMALE', '1992-11-23', '0910000238', 'patient0238@evercare.test', '079000000238', 'BH-2026-000238', '338 Duong Mau 19, Phuong 14, TP HCM', 'Nguoi Than 238', '0980000238', 'O-', 'Khong ghi nhan', 'Khong co tien su dac biet', TRUE),
(239, 1239, 'PAT-2026-0239', 'Ly Bao Nhi 239', 'MALE', '1993-12-24', '0910000239', 'patient0239@evercare.test', '079000000239', 'BH-2026-000239', '339 Duong Mau 20, Phuong 15, TP HCM', 'Nguoi Than 239', '0980000239', 'AB-', 'Khong ghi nhan', 'Khong co tien su dac biet', TRUE),
(240, 1240, 'PAT-2026-0240', 'Nguyen Van An 240', 'FEMALE', '1994-01-25', '0910000240', 'patient0240@evercare.test', '079000000240', 'BH-2026-000240', '340 Duong Mau 1, Phuong 1, TP HCM', 'Nguoi Than 240', '0980000240', 'A+', 'Di ung hai san', 'Tien su viem mui di ung', TRUE),
(241, 1241, 'PAT-2026-0241', 'Tran Thi Binh 241', 'MALE', '1995-02-26', '0910000241', 'patient0241@evercare.test', '079000000241', 'BH-2026-000241', '341 Duong Mau 2, Phuong 2, TP HCM', 'Nguoi Than 241', '0980000241', 'B+', 'Khong ghi nhan', 'Khong co tien su dac biet', TRUE),
(242, 1242, 'PAT-2026-0242', 'Le Minh Chi 242', 'FEMALE', '1996-03-27', '0910000242', 'patient0242@evercare.test', '079000000242', 'BH-2026-000242', '342 Duong Mau 3, Phuong 3, TP HCM', 'Nguoi Than 242', '0980000242', 'O+', 'Khong ghi nhan', 'Khong co tien su dac biet', TRUE),
(243, 1243, 'PAT-2026-0243', 'Pham Quoc Dung 243', 'MALE', '1997-04-01', '0910000243', 'patient0243@evercare.test', '079000000243', 'BH-2026-000243', '343 Duong Mau 4, Phuong 4, TP HCM', 'Nguoi Than 243', '0980000243', 'AB+', 'Khong ghi nhan', 'Khong co tien su dac biet', TRUE),
(244, 1244, 'PAT-2026-0244', 'Hoang Thanh Giang 244', 'FEMALE', '1998-05-02', '0910000244', 'patient0244@evercare.test', '079000000244', 'BH-2026-000244', '344 Duong Mau 5, Phuong 5, TP HCM', 'Nguoi Than 244', '0980000244', 'A-', 'Khong ghi nhan', 'Tien su viem mui di ung', TRUE),
(245, 1245, 'PAT-2026-0245', 'Huynh Ngoc Ha 245', 'MALE', '1999-06-03', '0910000245', 'patient0245@evercare.test', '079000000245', 'BH-2026-000245', '345 Duong Mau 6, Phuong 6, TP HCM', 'Nguoi Than 245', '0980000245', 'B-', 'Di ung hai san', 'Khong co tien su dac biet', TRUE),
(246, 1246, 'PAT-2026-0246', 'Phan Anh Hung 246', 'FEMALE', '2000-07-04', '0910000246', 'patient0246@evercare.test', '079000000246', 'BH-2026-000246', '346 Duong Mau 7, Phuong 7, TP HCM', 'Nguoi Than 246', '0980000246', 'O-', 'Khong ghi nhan', 'Khong co tien su dac biet', TRUE),
(247, 1247, 'PAT-2026-0247', 'Vu Gia Khanh 247', 'MALE', '2001-08-05', '0910000247', 'patient0247@evercare.test', '079000000247', 'BH-2026-000247', '347 Duong Mau 8, Phuong 8, TP HCM', 'Nguoi Than 247', '0980000247', 'AB-', 'Khong ghi nhan', 'Khong co tien su dac biet', TRUE),
(248, 1248, 'PAT-2026-0248', 'Vo Duc Lan 248', 'FEMALE', '2002-09-06', '0910000248', 'patient0248@evercare.test', '079000000248', 'BH-2026-000248', '348 Duong Mau 9, Phuong 9, TP HCM', 'Nguoi Than 248', '0980000248', 'A+', 'Khong ghi nhan', 'Tien su viem mui di ung', TRUE),
(249, 1249, 'PAT-2026-0249', 'Dang Bao Long 249', 'MALE', '2003-10-07', '0910000249', 'patient0249@evercare.test', '079000000249', 'BH-2026-000249', '349 Duong Mau 10, Phuong 10, TP HCM', 'Nguoi Than 249', '0980000249', 'B+', 'Khong ghi nhan', 'Khong co tien su dac biet', TRUE),
(250, 1250, 'PAT-2026-0250', 'Bui Van Mai 250', 'FEMALE', '2004-11-08', '0910000250', 'patient0250@evercare.test', '079000000250', 'BH-2026-000250', '350 Duong Mau 11, Phuong 11, TP HCM', 'Nguoi Than 250', '0980000250', 'O+', 'Di ung hai san', 'Khong co tien su dac biet', TRUE);
INSERT INTO medicine (medicine_code, name, unit, description, usage_note, unit_price, min_stock_quantity) VALUES
('MED-2026-0001', 'Paracetamol 500mg', 'VIEN', 'Giam dau ha sot', 'Uong sau an', 1500, 50),
('MED-2026-0002', 'Amoxicillin 500mg', 'VIEN', 'Khang sinh', 'Dung theo chi dinh bac si', 3500, 50),
('MED-2026-0003', 'Cetirizine 10mg', 'VIEN', 'Khang histamin', 'Uong buoi toi', 1800, 40),
('MED-2026-0004', 'Omeprazole 20mg', 'VIEN', 'Giam acid da day', 'Uong truoc an sang', 2200, 40),
('MED-2026-0005', 'ORESOL', 'GOI', 'Bu nuoc dien giai', 'Pha voi nuoc theo huong dan', 2500, 30),
('MED-2026-0006', 'Vitamin C 500mg', 'VIEN', 'Bo sung vitamin C', 'Uong sau an', 1200, 50),
('MED-2026-0007', 'Ibuprofen 400mg', 'VIEN', 'Giam dau khang viem', 'Uong sau an', 2800, 40),
('MED-2026-0008', 'Salbutamol syrup', 'CHAI', 'Ho tro hen phe quan', 'Dung theo don', 45000, 20),
('MED-2026-0009', 'Natri Clorid 0.9%', 'CHAI', 'Rua mui rua vet thuong', 'Dung ngoai', 9000, 30),
('MED-2026-0010', 'Povidone Iodine', 'CHAI', 'Sat khuẩn ngoai da', 'Dung ngoai', 28000, 20);
INSERT INTO medicine_batch (medicine_id, batch_code, import_date, expiry_date, quantity, remaining_quantity, import_price, supplier_name) VALUES
(1, 'BATCH-2026-0001', '2026-01-10', '2028-01-10', 500, 500, 1050.0, 'EverCare Pharma Supplier'),
(2, 'BATCH-2026-0002', '2026-01-10', '2028-01-10', 500, 500, 2450.0, 'EverCare Pharma Supplier'),
(3, 'BATCH-2026-0003', '2026-01-10', '2028-01-10', 500, 500, 1260.0, 'EverCare Pharma Supplier'),
(4, 'BATCH-2026-0004', '2026-01-10', '2028-01-10', 500, 500, 1540.0, 'EverCare Pharma Supplier'),
(5, 'BATCH-2026-0005', '2026-01-10', '2028-01-10', 500, 500, 1750.0, 'EverCare Pharma Supplier'),
(6, 'BATCH-2026-0006', '2026-01-10', '2028-01-10', 500, 500, 840.0, 'EverCare Pharma Supplier'),
(7, 'BATCH-2026-0007', '2026-01-10', '2028-01-10', 500, 500, 1959.9999999999998, 'EverCare Pharma Supplier'),
(8, 'BATCH-2026-0008', '2026-01-10', '2028-01-10', 500, 500, 31499.999999999996, 'EverCare Pharma Supplier'),
(9, 'BATCH-2026-0009', '2026-01-10', '2028-01-10', 500, 500, 6300.0, 'EverCare Pharma Supplier'),
(10, 'BATCH-2026-0010', '2026-01-10', '2028-01-10', 500, 500, 19600.0, 'EverCare Pharma Supplier');
INSERT INTO doctor_schedule (id, doctor_id, work_date, start_time, end_time, max_patients, status, note, active) VALUES
(1, 1, '2026-05-27', '08:00:00', '12:00:00', 40, 'AVAILABLE', 'Lich kham sang', TRUE),
(2, 1, '2026-05-27', '13:00:00', '17:00:00', 40, 'AVAILABLE', 'Lich kham chieu', TRUE),
(3, 2, '2026-05-27', '08:00:00', '12:00:00', 40, 'AVAILABLE', 'Lich kham sang', TRUE),
(4, 2, '2026-05-27', '13:00:00', '17:00:00', 40, 'AVAILABLE', 'Lich kham chieu', TRUE),
(5, 3, '2026-05-27', '08:00:00', '12:00:00', 40, 'AVAILABLE', 'Lich kham sang', TRUE),
(6, 3, '2026-05-27', '13:00:00', '17:00:00', 40, 'AVAILABLE', 'Lich kham chieu', TRUE),
(7, 4, '2026-05-27', '08:00:00', '12:00:00', 40, 'AVAILABLE', 'Lich kham sang', TRUE),
(8, 4, '2026-05-27', '13:00:00', '17:00:00', 40, 'AVAILABLE', 'Lich kham chieu', TRUE),
(9, 5, '2026-05-27', '08:00:00', '12:00:00', 40, 'AVAILABLE', 'Lich kham sang', TRUE),
(10, 5, '2026-05-27', '13:00:00', '17:00:00', 40, 'AVAILABLE', 'Lich kham chieu', TRUE),
(11, 6, '2026-05-27', '08:00:00', '12:00:00', 40, 'AVAILABLE', 'Lich kham sang', TRUE),
(12, 6, '2026-05-27', '13:00:00', '17:00:00', 40, 'AVAILABLE', 'Lich kham chieu', TRUE),
(13, 7, '2026-05-27', '08:00:00', '12:00:00', 40, 'AVAILABLE', 'Lich kham sang', TRUE),
(14, 7, '2026-05-27', '13:00:00', '17:00:00', 40, 'AVAILABLE', 'Lich kham chieu', TRUE),
(15, 8, '2026-05-27', '08:00:00', '12:00:00', 40, 'AVAILABLE', 'Lich kham sang', TRUE),
(16, 8, '2026-05-27', '13:00:00', '17:00:00', 40, 'AVAILABLE', 'Lich kham chieu', TRUE),
(17, 9, '2026-05-27', '08:00:00', '12:00:00', 40, 'AVAILABLE', 'Lich kham sang', TRUE),
(18, 9, '2026-05-27', '13:00:00', '17:00:00', 40, 'AVAILABLE', 'Lich kham chieu', TRUE),
(19, 10, '2026-05-27', '08:00:00', '12:00:00', 40, 'AVAILABLE', 'Lich kham sang', TRUE),
(20, 10, '2026-05-27', '13:00:00', '17:00:00', 40, 'AVAILABLE', 'Lich kham chieu', TRUE),
(21, 1, '2026-05-28', '08:00:00', '12:00:00', 40, 'AVAILABLE', 'Lich kham sang', TRUE),
(22, 1, '2026-05-28', '13:00:00', '17:00:00', 40, 'AVAILABLE', 'Lich kham chieu', TRUE),
(23, 2, '2026-05-28', '08:00:00', '12:00:00', 40, 'AVAILABLE', 'Lich kham sang', TRUE),
(24, 2, '2026-05-28', '13:00:00', '17:00:00', 40, 'AVAILABLE', 'Lich kham chieu', TRUE),
(25, 3, '2026-05-28', '08:00:00', '12:00:00', 40, 'AVAILABLE', 'Lich kham sang', TRUE),
(26, 3, '2026-05-28', '13:00:00', '17:00:00', 40, 'AVAILABLE', 'Lich kham chieu', TRUE),
(27, 4, '2026-05-28', '08:00:00', '12:00:00', 40, 'AVAILABLE', 'Lich kham sang', TRUE),
(28, 4, '2026-05-28', '13:00:00', '17:00:00', 40, 'AVAILABLE', 'Lich kham chieu', TRUE),
(29, 5, '2026-05-28', '08:00:00', '12:00:00', 40, 'AVAILABLE', 'Lich kham sang', TRUE),
(30, 5, '2026-05-28', '13:00:00', '17:00:00', 40, 'AVAILABLE', 'Lich kham chieu', TRUE),
(31, 6, '2026-05-28', '08:00:00', '12:00:00', 40, 'AVAILABLE', 'Lich kham sang', TRUE),
(32, 6, '2026-05-28', '13:00:00', '17:00:00', 40, 'AVAILABLE', 'Lich kham chieu', TRUE),
(33, 7, '2026-05-28', '08:00:00', '12:00:00', 40, 'AVAILABLE', 'Lich kham sang', TRUE),
(34, 7, '2026-05-28', '13:00:00', '17:00:00', 40, 'AVAILABLE', 'Lich kham chieu', TRUE),
(35, 8, '2026-05-28', '08:00:00', '12:00:00', 40, 'AVAILABLE', 'Lich kham sang', TRUE),
(36, 8, '2026-05-28', '13:00:00', '17:00:00', 40, 'AVAILABLE', 'Lich kham chieu', TRUE),
(37, 9, '2026-05-28', '08:00:00', '12:00:00', 40, 'AVAILABLE', 'Lich kham sang', TRUE),
(38, 9, '2026-05-28', '13:00:00', '17:00:00', 40, 'AVAILABLE', 'Lich kham chieu', TRUE),
(39, 10, '2026-05-28', '08:00:00', '12:00:00', 40, 'AVAILABLE', 'Lich kham sang', TRUE),
(40, 10, '2026-05-28', '13:00:00', '17:00:00', 40, 'AVAILABLE', 'Lich kham chieu', TRUE),
(41, 1, '2026-05-29', '08:00:00', '12:00:00', 40, 'AVAILABLE', 'Lich kham sang', TRUE),
(42, 1, '2026-05-29', '13:00:00', '17:00:00', 40, 'AVAILABLE', 'Lich kham chieu', TRUE),
(43, 2, '2026-05-29', '08:00:00', '12:00:00', 40, 'AVAILABLE', 'Lich kham sang', TRUE),
(44, 2, '2026-05-29', '13:00:00', '17:00:00', 40, 'AVAILABLE', 'Lich kham chieu', TRUE),
(45, 3, '2026-05-29', '08:00:00', '12:00:00', 40, 'AVAILABLE', 'Lich kham sang', TRUE),
(46, 3, '2026-05-29', '13:00:00', '17:00:00', 40, 'AVAILABLE', 'Lich kham chieu', TRUE),
(47, 4, '2026-05-29', '08:00:00', '12:00:00', 40, 'AVAILABLE', 'Lich kham sang', TRUE),
(48, 4, '2026-05-29', '13:00:00', '17:00:00', 40, 'AVAILABLE', 'Lich kham chieu', TRUE),
(49, 5, '2026-05-29', '08:00:00', '12:00:00', 40, 'AVAILABLE', 'Lich kham sang', TRUE),
(50, 5, '2026-05-29', '13:00:00', '17:00:00', 40, 'AVAILABLE', 'Lich kham chieu', TRUE),
(51, 6, '2026-05-29', '08:00:00', '12:00:00', 40, 'AVAILABLE', 'Lich kham sang', TRUE),
(52, 6, '2026-05-29', '13:00:00', '17:00:00', 40, 'AVAILABLE', 'Lich kham chieu', TRUE),
(53, 7, '2026-05-29', '08:00:00', '12:00:00', 40, 'AVAILABLE', 'Lich kham sang', TRUE),
(54, 7, '2026-05-29', '13:00:00', '17:00:00', 40, 'AVAILABLE', 'Lich kham chieu', TRUE),
(55, 8, '2026-05-29', '08:00:00', '12:00:00', 40, 'AVAILABLE', 'Lich kham sang', TRUE),
(56, 8, '2026-05-29', '13:00:00', '17:00:00', 40, 'AVAILABLE', 'Lich kham chieu', TRUE),
(57, 9, '2026-05-29', '08:00:00', '12:00:00', 40, 'AVAILABLE', 'Lich kham sang', TRUE),
(58, 9, '2026-05-29', '13:00:00', '17:00:00', 40, 'AVAILABLE', 'Lich kham chieu', TRUE),
(59, 10, '2026-05-29', '08:00:00', '12:00:00', 40, 'AVAILABLE', 'Lich kham sang', TRUE),
(60, 10, '2026-05-29', '13:00:00', '17:00:00', 40, 'AVAILABLE', 'Lich kham chieu', TRUE),
(61, 1, '2026-05-30', '08:00:00', '12:00:00', 40, 'AVAILABLE', 'Lich kham sang', TRUE),
(62, 1, '2026-05-30', '13:00:00', '17:00:00', 40, 'AVAILABLE', 'Lich kham chieu', TRUE),
(63, 2, '2026-05-30', '08:00:00', '12:00:00', 40, 'AVAILABLE', 'Lich kham sang', TRUE),
(64, 2, '2026-05-30', '13:00:00', '17:00:00', 40, 'AVAILABLE', 'Lich kham chieu', TRUE),
(65, 3, '2026-05-30', '08:00:00', '12:00:00', 40, 'AVAILABLE', 'Lich kham sang', TRUE),
(66, 3, '2026-05-30', '13:00:00', '17:00:00', 40, 'AVAILABLE', 'Lich kham chieu', TRUE),
(67, 4, '2026-05-30', '08:00:00', '12:00:00', 40, 'AVAILABLE', 'Lich kham sang', TRUE),
(68, 4, '2026-05-30', '13:00:00', '17:00:00', 40, 'AVAILABLE', 'Lich kham chieu', TRUE),
(69, 5, '2026-05-30', '08:00:00', '12:00:00', 40, 'AVAILABLE', 'Lich kham sang', TRUE),
(70, 5, '2026-05-30', '13:00:00', '17:00:00', 40, 'AVAILABLE', 'Lich kham chieu', TRUE),
(71, 6, '2026-05-30', '08:00:00', '12:00:00', 40, 'AVAILABLE', 'Lich kham sang', TRUE),
(72, 6, '2026-05-30', '13:00:00', '17:00:00', 40, 'AVAILABLE', 'Lich kham chieu', TRUE),
(73, 7, '2026-05-30', '08:00:00', '12:00:00', 40, 'AVAILABLE', 'Lich kham sang', TRUE),
(74, 7, '2026-05-30', '13:00:00', '17:00:00', 40, 'AVAILABLE', 'Lich kham chieu', TRUE),
(75, 8, '2026-05-30', '08:00:00', '12:00:00', 40, 'AVAILABLE', 'Lich kham sang', TRUE),
(76, 8, '2026-05-30', '13:00:00', '17:00:00', 40, 'AVAILABLE', 'Lich kham chieu', TRUE),
(77, 9, '2026-05-30', '08:00:00', '12:00:00', 40, 'AVAILABLE', 'Lich kham sang', TRUE),
(78, 9, '2026-05-30', '13:00:00', '17:00:00', 40, 'AVAILABLE', 'Lich kham chieu', TRUE),
(79, 10, '2026-05-30', '08:00:00', '12:00:00', 40, 'AVAILABLE', 'Lich kham sang', TRUE),
(80, 10, '2026-05-30', '13:00:00', '17:00:00', 40, 'AVAILABLE', 'Lich kham chieu', TRUE),
(81, 1, '2026-05-31', '08:00:00', '12:00:00', 40, 'AVAILABLE', 'Lich kham sang', TRUE),
(82, 1, '2026-05-31', '13:00:00', '17:00:00', 40, 'AVAILABLE', 'Lich kham chieu', TRUE),
(83, 2, '2026-05-31', '08:00:00', '12:00:00', 40, 'AVAILABLE', 'Lich kham sang', TRUE),
(84, 2, '2026-05-31', '13:00:00', '17:00:00', 40, 'AVAILABLE', 'Lich kham chieu', TRUE),
(85, 3, '2026-05-31', '08:00:00', '12:00:00', 40, 'AVAILABLE', 'Lich kham sang', TRUE),
(86, 3, '2026-05-31', '13:00:00', '17:00:00', 40, 'AVAILABLE', 'Lich kham chieu', TRUE),
(87, 4, '2026-05-31', '08:00:00', '12:00:00', 40, 'AVAILABLE', 'Lich kham sang', TRUE),
(88, 4, '2026-05-31', '13:00:00', '17:00:00', 40, 'AVAILABLE', 'Lich kham chieu', TRUE),
(89, 5, '2026-05-31', '08:00:00', '12:00:00', 40, 'AVAILABLE', 'Lich kham sang', TRUE),
(90, 5, '2026-05-31', '13:00:00', '17:00:00', 40, 'AVAILABLE', 'Lich kham chieu', TRUE),
(91, 6, '2026-05-31', '08:00:00', '12:00:00', 40, 'AVAILABLE', 'Lich kham sang', TRUE),
(92, 6, '2026-05-31', '13:00:00', '17:00:00', 40, 'AVAILABLE', 'Lich kham chieu', TRUE),
(93, 7, '2026-05-31', '08:00:00', '12:00:00', 40, 'AVAILABLE', 'Lich kham sang', TRUE),
(94, 7, '2026-05-31', '13:00:00', '17:00:00', 40, 'AVAILABLE', 'Lich kham chieu', TRUE),
(95, 8, '2026-05-31', '08:00:00', '12:00:00', 40, 'AVAILABLE', 'Lich kham sang', TRUE),
(96, 8, '2026-05-31', '13:00:00', '17:00:00', 40, 'AVAILABLE', 'Lich kham chieu', TRUE),
(97, 9, '2026-05-31', '08:00:00', '12:00:00', 40, 'AVAILABLE', 'Lich kham sang', TRUE),
(98, 9, '2026-05-31', '13:00:00', '17:00:00', 40, 'AVAILABLE', 'Lich kham chieu', TRUE),
(99, 10, '2026-05-31', '08:00:00', '12:00:00', 40, 'AVAILABLE', 'Lich kham sang', TRUE),
(100, 10, '2026-05-31', '13:00:00', '17:00:00', 40, 'AVAILABLE', 'Lich kham chieu', TRUE),
(101, 1, '2026-06-01', '08:00:00', '12:00:00', 40, 'AVAILABLE', 'Lich kham sang', TRUE),
(102, 1, '2026-06-01', '13:00:00', '17:00:00', 40, 'AVAILABLE', 'Lich kham chieu', TRUE),
(103, 2, '2026-06-01', '08:00:00', '12:00:00', 40, 'AVAILABLE', 'Lich kham sang', TRUE),
(104, 2, '2026-06-01', '13:00:00', '17:00:00', 40, 'AVAILABLE', 'Lich kham chieu', TRUE),
(105, 3, '2026-06-01', '08:00:00', '12:00:00', 40, 'AVAILABLE', 'Lich kham sang', TRUE),
(106, 3, '2026-06-01', '13:00:00', '17:00:00', 40, 'AVAILABLE', 'Lich kham chieu', TRUE),
(107, 4, '2026-06-01', '08:00:00', '12:00:00', 40, 'AVAILABLE', 'Lich kham sang', TRUE),
(108, 4, '2026-06-01', '13:00:00', '17:00:00', 40, 'AVAILABLE', 'Lich kham chieu', TRUE),
(109, 5, '2026-06-01', '08:00:00', '12:00:00', 40, 'AVAILABLE', 'Lich kham sang', TRUE),
(110, 5, '2026-06-01', '13:00:00', '17:00:00', 40, 'AVAILABLE', 'Lich kham chieu', TRUE),
(111, 6, '2026-06-01', '08:00:00', '12:00:00', 40, 'AVAILABLE', 'Lich kham sang', TRUE),
(112, 6, '2026-06-01', '13:00:00', '17:00:00', 40, 'AVAILABLE', 'Lich kham chieu', TRUE),
(113, 7, '2026-06-01', '08:00:00', '12:00:00', 40, 'AVAILABLE', 'Lich kham sang', TRUE),
(114, 7, '2026-06-01', '13:00:00', '17:00:00', 40, 'AVAILABLE', 'Lich kham chieu', TRUE),
(115, 8, '2026-06-01', '08:00:00', '12:00:00', 40, 'AVAILABLE', 'Lich kham sang', TRUE),
(116, 8, '2026-06-01', '13:00:00', '17:00:00', 40, 'AVAILABLE', 'Lich kham chieu', TRUE),
(117, 9, '2026-06-01', '08:00:00', '12:00:00', 40, 'AVAILABLE', 'Lich kham sang', TRUE),
(118, 9, '2026-06-01', '13:00:00', '17:00:00', 40, 'AVAILABLE', 'Lich kham chieu', TRUE),
(119, 10, '2026-06-01', '08:00:00', '12:00:00', 40, 'AVAILABLE', 'Lich kham sang', TRUE),
(120, 10, '2026-06-01', '13:00:00', '17:00:00', 40, 'AVAILABLE', 'Lich kham chieu', TRUE),
(121, 1, '2026-06-02', '08:00:00', '12:00:00', 40, 'AVAILABLE', 'Lich kham sang', TRUE),
(122, 1, '2026-06-02', '13:00:00', '17:00:00', 40, 'AVAILABLE', 'Lich kham chieu', TRUE),
(123, 2, '2026-06-02', '08:00:00', '12:00:00', 40, 'AVAILABLE', 'Lich kham sang', TRUE),
(124, 2, '2026-06-02', '13:00:00', '17:00:00', 40, 'AVAILABLE', 'Lich kham chieu', TRUE),
(125, 3, '2026-06-02', '08:00:00', '12:00:00', 40, 'AVAILABLE', 'Lich kham sang', TRUE),
(126, 3, '2026-06-02', '13:00:00', '17:00:00', 40, 'AVAILABLE', 'Lich kham chieu', TRUE),
(127, 4, '2026-06-02', '08:00:00', '12:00:00', 40, 'AVAILABLE', 'Lich kham sang', TRUE),
(128, 4, '2026-06-02', '13:00:00', '17:00:00', 40, 'AVAILABLE', 'Lich kham chieu', TRUE),
(129, 5, '2026-06-02', '08:00:00', '12:00:00', 40, 'AVAILABLE', 'Lich kham sang', TRUE),
(130, 5, '2026-06-02', '13:00:00', '17:00:00', 40, 'AVAILABLE', 'Lich kham chieu', TRUE),
(131, 6, '2026-06-02', '08:00:00', '12:00:00', 40, 'AVAILABLE', 'Lich kham sang', TRUE),
(132, 6, '2026-06-02', '13:00:00', '17:00:00', 40, 'AVAILABLE', 'Lich kham chieu', TRUE),
(133, 7, '2026-06-02', '08:00:00', '12:00:00', 40, 'AVAILABLE', 'Lich kham sang', TRUE),
(134, 7, '2026-06-02', '13:00:00', '17:00:00', 40, 'AVAILABLE', 'Lich kham chieu', TRUE),
(135, 8, '2026-06-02', '08:00:00', '12:00:00', 40, 'AVAILABLE', 'Lich kham sang', TRUE),
(136, 8, '2026-06-02', '13:00:00', '17:00:00', 40, 'AVAILABLE', 'Lich kham chieu', TRUE),
(137, 9, '2026-06-02', '08:00:00', '12:00:00', 40, 'AVAILABLE', 'Lich kham sang', TRUE),
(138, 9, '2026-06-02', '13:00:00', '17:00:00', 40, 'AVAILABLE', 'Lich kham chieu', TRUE),
(139, 10, '2026-06-02', '08:00:00', '12:00:00', 40, 'AVAILABLE', 'Lich kham sang', TRUE),
(140, 10, '2026-06-02', '13:00:00', '17:00:00', 40, 'AVAILABLE', 'Lich kham chieu', TRUE),
(141, 1, '2026-06-03', '08:00:00', '12:00:00', 40, 'AVAILABLE', 'Lich kham sang', TRUE),
(142, 1, '2026-06-03', '13:00:00', '17:00:00', 40, 'AVAILABLE', 'Lich kham chieu', TRUE),
(143, 2, '2026-06-03', '08:00:00', '12:00:00', 40, 'AVAILABLE', 'Lich kham sang', TRUE),
(144, 2, '2026-06-03', '13:00:00', '17:00:00', 40, 'AVAILABLE', 'Lich kham chieu', TRUE),
(145, 3, '2026-06-03', '08:00:00', '12:00:00', 40, 'AVAILABLE', 'Lich kham sang', TRUE),
(146, 3, '2026-06-03', '13:00:00', '17:00:00', 40, 'AVAILABLE', 'Lich kham chieu', TRUE),
(147, 4, '2026-06-03', '08:00:00', '12:00:00', 40, 'AVAILABLE', 'Lich kham sang', TRUE),
(148, 4, '2026-06-03', '13:00:00', '17:00:00', 40, 'AVAILABLE', 'Lich kham chieu', TRUE),
(149, 5, '2026-06-03', '08:00:00', '12:00:00', 40, 'AVAILABLE', 'Lich kham sang', TRUE),
(150, 5, '2026-06-03', '13:00:00', '17:00:00', 40, 'AVAILABLE', 'Lich kham chieu', TRUE),
(151, 6, '2026-06-03', '08:00:00', '12:00:00', 40, 'AVAILABLE', 'Lich kham sang', TRUE),
(152, 6, '2026-06-03', '13:00:00', '17:00:00', 40, 'AVAILABLE', 'Lich kham chieu', TRUE),
(153, 7, '2026-06-03', '08:00:00', '12:00:00', 40, 'AVAILABLE', 'Lich kham sang', TRUE),
(154, 7, '2026-06-03', '13:00:00', '17:00:00', 40, 'AVAILABLE', 'Lich kham chieu', TRUE),
(155, 8, '2026-06-03', '08:00:00', '12:00:00', 40, 'AVAILABLE', 'Lich kham sang', TRUE),
(156, 8, '2026-06-03', '13:00:00', '17:00:00', 40, 'AVAILABLE', 'Lich kham chieu', TRUE),
(157, 9, '2026-06-03', '08:00:00', '12:00:00', 40, 'AVAILABLE', 'Lich kham sang', TRUE),
(158, 9, '2026-06-03', '13:00:00', '17:00:00', 40, 'AVAILABLE', 'Lich kham chieu', TRUE),
(159, 10, '2026-06-03', '08:00:00', '12:00:00', 40, 'AVAILABLE', 'Lich kham sang', TRUE),
(160, 10, '2026-06-03', '13:00:00', '17:00:00', 40, 'AVAILABLE', 'Lich kham chieu', TRUE),
(161, 1, '2026-06-04', '08:00:00', '12:00:00', 40, 'AVAILABLE', 'Lich kham sang', TRUE),
(162, 1, '2026-06-04', '13:00:00', '17:00:00', 40, 'AVAILABLE', 'Lich kham chieu', TRUE),
(163, 2, '2026-06-04', '08:00:00', '12:00:00', 40, 'AVAILABLE', 'Lich kham sang', TRUE),
(164, 2, '2026-06-04', '13:00:00', '17:00:00', 40, 'AVAILABLE', 'Lich kham chieu', TRUE),
(165, 3, '2026-06-04', '08:00:00', '12:00:00', 40, 'AVAILABLE', 'Lich kham sang', TRUE),
(166, 3, '2026-06-04', '13:00:00', '17:00:00', 40, 'AVAILABLE', 'Lich kham chieu', TRUE),
(167, 4, '2026-06-04', '08:00:00', '12:00:00', 40, 'AVAILABLE', 'Lich kham sang', TRUE),
(168, 4, '2026-06-04', '13:00:00', '17:00:00', 40, 'AVAILABLE', 'Lich kham chieu', TRUE),
(169, 5, '2026-06-04', '08:00:00', '12:00:00', 40, 'AVAILABLE', 'Lich kham sang', TRUE),
(170, 5, '2026-06-04', '13:00:00', '17:00:00', 40, 'AVAILABLE', 'Lich kham chieu', TRUE),
(171, 6, '2026-06-04', '08:00:00', '12:00:00', 40, 'AVAILABLE', 'Lich kham sang', TRUE),
(172, 6, '2026-06-04', '13:00:00', '17:00:00', 40, 'AVAILABLE', 'Lich kham chieu', TRUE),
(173, 7, '2026-06-04', '08:00:00', '12:00:00', 40, 'AVAILABLE', 'Lich kham sang', TRUE),
(174, 7, '2026-06-04', '13:00:00', '17:00:00', 40, 'AVAILABLE', 'Lich kham chieu', TRUE),
(175, 8, '2026-06-04', '08:00:00', '12:00:00', 40, 'AVAILABLE', 'Lich kham sang', TRUE),
(176, 8, '2026-06-04', '13:00:00', '17:00:00', 40, 'AVAILABLE', 'Lich kham chieu', TRUE),
(177, 9, '2026-06-04', '08:00:00', '12:00:00', 40, 'AVAILABLE', 'Lich kham sang', TRUE),
(178, 9, '2026-06-04', '13:00:00', '17:00:00', 40, 'AVAILABLE', 'Lich kham chieu', TRUE),
(179, 10, '2026-06-04', '08:00:00', '12:00:00', 40, 'AVAILABLE', 'Lich kham sang', TRUE),
(180, 10, '2026-06-04', '13:00:00', '17:00:00', 40, 'AVAILABLE', 'Lich kham chieu', TRUE),
(181, 1, '2026-06-05', '08:00:00', '12:00:00', 40, 'AVAILABLE', 'Lich kham sang', TRUE),
(182, 1, '2026-06-05', '13:00:00', '17:00:00', 40, 'AVAILABLE', 'Lich kham chieu', TRUE),
(183, 2, '2026-06-05', '08:00:00', '12:00:00', 40, 'AVAILABLE', 'Lich kham sang', TRUE),
(184, 2, '2026-06-05', '13:00:00', '17:00:00', 40, 'AVAILABLE', 'Lich kham chieu', TRUE),
(185, 3, '2026-06-05', '08:00:00', '12:00:00', 40, 'AVAILABLE', 'Lich kham sang', TRUE),
(186, 3, '2026-06-05', '13:00:00', '17:00:00', 40, 'AVAILABLE', 'Lich kham chieu', TRUE),
(187, 4, '2026-06-05', '08:00:00', '12:00:00', 40, 'AVAILABLE', 'Lich kham sang', TRUE),
(188, 4, '2026-06-05', '13:00:00', '17:00:00', 40, 'AVAILABLE', 'Lich kham chieu', TRUE),
(189, 5, '2026-06-05', '08:00:00', '12:00:00', 40, 'AVAILABLE', 'Lich kham sang', TRUE),
(190, 5, '2026-06-05', '13:00:00', '17:00:00', 40, 'AVAILABLE', 'Lich kham chieu', TRUE),
(191, 6, '2026-06-05', '08:00:00', '12:00:00', 40, 'AVAILABLE', 'Lich kham sang', TRUE),
(192, 6, '2026-06-05', '13:00:00', '17:00:00', 40, 'AVAILABLE', 'Lich kham chieu', TRUE),
(193, 7, '2026-06-05', '08:00:00', '12:00:00', 40, 'AVAILABLE', 'Lich kham sang', TRUE),
(194, 7, '2026-06-05', '13:00:00', '17:00:00', 40, 'AVAILABLE', 'Lich kham chieu', TRUE),
(195, 8, '2026-06-05', '08:00:00', '12:00:00', 40, 'AVAILABLE', 'Lich kham sang', TRUE),
(196, 8, '2026-06-05', '13:00:00', '17:00:00', 40, 'AVAILABLE', 'Lich kham chieu', TRUE),
(197, 9, '2026-06-05', '08:00:00', '12:00:00', 40, 'AVAILABLE', 'Lich kham sang', TRUE),
(198, 9, '2026-06-05', '13:00:00', '17:00:00', 40, 'AVAILABLE', 'Lich kham chieu', TRUE),
(199, 10, '2026-06-05', '08:00:00', '12:00:00', 40, 'AVAILABLE', 'Lich kham sang', TRUE),
(200, 10, '2026-06-05', '13:00:00', '17:00:00', 40, 'AVAILABLE', 'Lich kham chieu', TRUE);
INSERT INTO appointment (id, appointment_code, patient_id, doctor_id, service_id, appointment_date, start_time, end_time, status, reason, symptom_note, cancel_reason, created_by, active) VALUES
(1, 'APT-20260527-001', 1, 1, 2, '2026-05-27', '08:00:00', '08:30:00', 'COMPLETED', 'Kham tong quat', 'Trieu chung nhe, can tham kham ban dau', NULL, 201, TRUE),
(2, 'APT-20260527-002', 2, 2, 3, '2026-05-27', '08:00:00', '08:30:00', 'COMPLETED', 'Tu van suc khoe', 'Can kiem tra dinh ky', NULL, 201, TRUE),
(3, 'APT-20260527-003', 3, 3, 4, '2026-05-27', '08:00:00', '08:30:00', 'COMPLETED', 'Kiem tra huyet ap', 'Dau hieu xuat hien 2-3 ngay', NULL, 201, TRUE),
(4, 'APT-20260527-004', 4, 4, 5, '2026-05-27', '08:00:00', '08:30:00', 'COMPLETED', 'Dau dau', 'Da dung thuoc nhung chua giam', NULL, 201, TRUE),
(5, 'APT-20260527-005', 5, 5, 6, '2026-05-27', '08:00:00', '08:30:00', 'COMPLETED', 'Ho sot', 'Mong muon bac si tu van chi tiet', NULL, 201, TRUE),
(6, 'APT-20260527-006', 6, 6, 7, '2026-05-27', '08:00:00', '08:30:00', 'COMPLETED', 'Dau bung', 'Trieu chung nhe, can tham kham ban dau', NULL, 201, TRUE),
(7, 'APT-20260527-007', 7, 7, 8, '2026-05-27', '08:00:00', '08:30:00', 'COMPLETED', 'Kham da lieu', 'Can kiem tra dinh ky', NULL, 201, TRUE),
(8, 'APT-20260527-008', 8, 8, 9, '2026-05-27', '08:00:00', '08:30:00', 'COMPLETED', 'Kham mat', 'Dau hieu xuat hien 2-3 ngay', NULL, 201, TRUE),
(9, 'APT-20260527-009', 9, 9, 10, '2026-05-27', '08:00:00', '08:30:00', 'COMPLETED', 'Dau rang', 'Da dung thuoc nhung chua giam', NULL, 201, TRUE),
(10, 'APT-20260527-010', 10, 10, 1, '2026-05-27', '08:00:00', '08:30:00', 'COMPLETED', 'Kham nhi', 'Mong muon bac si tu van chi tiet', NULL, 201, TRUE),
(11, 'APT-20260527-011', 11, 1, 2, '2026-05-27', '08:30:00', '09:00:00', 'COMPLETED', 'Kham tong quat', 'Trieu chung nhe, can tham kham ban dau', NULL, 201, TRUE),
(12, 'APT-20260527-012', 12, 2, 3, '2026-05-27', '08:30:00', '09:00:00', 'COMPLETED', 'Tu van suc khoe', 'Can kiem tra dinh ky', NULL, 201, TRUE),
(13, 'APT-20260527-013', 13, 3, 4, '2026-05-27', '08:30:00', '09:00:00', 'COMPLETED', 'Kiem tra huyet ap', 'Dau hieu xuat hien 2-3 ngay', NULL, 201, TRUE),
(14, 'APT-20260527-014', 14, 4, 5, '2026-05-27', '08:30:00', '09:00:00', 'COMPLETED', 'Dau dau', 'Da dung thuoc nhung chua giam', NULL, 201, TRUE),
(15, 'APT-20260527-015', 15, 5, 6, '2026-05-27', '08:30:00', '09:00:00', 'COMPLETED', 'Ho sot', 'Mong muon bac si tu van chi tiet', NULL, 201, TRUE),
(16, 'APT-20260527-016', 16, 6, 7, '2026-05-27', '08:30:00', '09:00:00', 'COMPLETED', 'Dau bung', 'Trieu chung nhe, can tham kham ban dau', NULL, 201, TRUE),
(17, 'APT-20260527-017', 17, 7, 8, '2026-05-27', '08:30:00', '09:00:00', 'COMPLETED', 'Kham da lieu', 'Can kiem tra dinh ky', NULL, 201, TRUE),
(18, 'APT-20260527-018', 18, 8, 9, '2026-05-27', '08:30:00', '09:00:00', 'COMPLETED', 'Kham mat', 'Dau hieu xuat hien 2-3 ngay', NULL, 201, TRUE),
(19, 'APT-20260527-019', 19, 9, 10, '2026-05-27', '08:30:00', '09:00:00', 'COMPLETED', 'Dau rang', 'Da dung thuoc nhung chua giam', NULL, 201, TRUE),
(20, 'APT-20260527-020', 20, 10, 1, '2026-05-27', '08:30:00', '09:00:00', 'COMPLETED', 'Kham nhi', 'Mong muon bac si tu van chi tiet', NULL, 201, TRUE),
(21, 'APT-20260528-001', 21, 1, 2, '2026-05-28', '08:00:00', '08:30:00', 'COMPLETED', 'Kham tong quat', 'Trieu chung nhe, can tham kham ban dau', NULL, 201, TRUE),
(22, 'APT-20260528-002', 22, 2, 3, '2026-05-28', '08:00:00', '08:30:00', 'COMPLETED', 'Tu van suc khoe', 'Can kiem tra dinh ky', NULL, 201, TRUE),
(23, 'APT-20260528-003', 23, 3, 4, '2026-05-28', '08:00:00', '08:30:00', 'COMPLETED', 'Kiem tra huyet ap', 'Dau hieu xuat hien 2-3 ngay', NULL, 201, TRUE),
(24, 'APT-20260528-004', 24, 4, 5, '2026-05-28', '08:00:00', '08:30:00', 'COMPLETED', 'Dau dau', 'Da dung thuoc nhung chua giam', NULL, 201, TRUE),
(25, 'APT-20260528-005', 25, 5, 6, '2026-05-28', '08:00:00', '08:30:00', 'COMPLETED', 'Ho sot', 'Mong muon bac si tu van chi tiet', NULL, 201, TRUE),
(26, 'APT-20260528-006', 26, 6, 7, '2026-05-28', '08:00:00', '08:30:00', 'COMPLETED', 'Dau bung', 'Trieu chung nhe, can tham kham ban dau', NULL, 201, TRUE),
(27, 'APT-20260528-007', 27, 7, 8, '2026-05-28', '08:00:00', '08:30:00', 'COMPLETED', 'Kham da lieu', 'Can kiem tra dinh ky', NULL, 201, TRUE),
(28, 'APT-20260528-008', 28, 8, 9, '2026-05-28', '08:00:00', '08:30:00', 'COMPLETED', 'Kham mat', 'Dau hieu xuat hien 2-3 ngay', NULL, 201, TRUE),
(29, 'APT-20260528-009', 29, 9, 10, '2026-05-28', '08:00:00', '08:30:00', 'COMPLETED', 'Dau rang', 'Da dung thuoc nhung chua giam', NULL, 201, TRUE),
(30, 'APT-20260528-010', 30, 10, 1, '2026-05-28', '08:00:00', '08:30:00', 'COMPLETED', 'Kham nhi', 'Mong muon bac si tu van chi tiet', NULL, 201, TRUE),
(31, 'APT-20260528-011', 31, 1, 2, '2026-05-28', '08:30:00', '09:00:00', 'WAITING', 'Kham tong quat', 'Trieu chung nhe, can tham kham ban dau', NULL, 201, TRUE),
(32, 'APT-20260528-012', 32, 2, 3, '2026-05-28', '08:30:00', '09:00:00', 'WAITING', 'Tu van suc khoe', 'Can kiem tra dinh ky', NULL, 201, TRUE),
(33, 'APT-20260528-013', 33, 3, 4, '2026-05-28', '08:30:00', '09:00:00', 'WAITING', 'Kiem tra huyet ap', 'Dau hieu xuat hien 2-3 ngay', NULL, 201, TRUE),
(34, 'APT-20260528-014', 34, 4, 5, '2026-05-28', '08:30:00', '09:00:00', 'WAITING', 'Dau dau', 'Da dung thuoc nhung chua giam', NULL, 201, TRUE),
(35, 'APT-20260528-015', 35, 5, 6, '2026-05-28', '08:30:00', '09:00:00', 'WAITING', 'Ho sot', 'Mong muon bac si tu van chi tiet', NULL, 201, TRUE),
(36, 'APT-20260528-016', 36, 6, 7, '2026-05-28', '08:30:00', '09:00:00', 'WAITING', 'Dau bung', 'Trieu chung nhe, can tham kham ban dau', NULL, 201, TRUE),
(37, 'APT-20260528-017', 37, 7, 8, '2026-05-28', '08:30:00', '09:00:00', 'WAITING', 'Kham da lieu', 'Can kiem tra dinh ky', NULL, 201, TRUE),
(38, 'APT-20260528-018', 38, 8, 9, '2026-05-28', '08:30:00', '09:00:00', 'WAITING', 'Kham mat', 'Dau hieu xuat hien 2-3 ngay', NULL, 201, TRUE),
(39, 'APT-20260528-019', 39, 9, 10, '2026-05-28', '08:30:00', '09:00:00', 'WAITING', 'Dau rang', 'Da dung thuoc nhung chua giam', NULL, 201, TRUE),
(40, 'APT-20260528-020', 40, 10, 1, '2026-05-28', '08:30:00', '09:00:00', 'WAITING', 'Kham nhi', 'Mong muon bac si tu van chi tiet', NULL, 201, TRUE),
(41, 'APT-20260528-021', 41, 1, 2, '2026-05-28', '09:00:00', '09:30:00', 'BOOKED', 'Kham tong quat', 'Trieu chung nhe, can tham kham ban dau', NULL, 201, TRUE),
(42, 'APT-20260528-022', 42, 2, 3, '2026-05-28', '09:00:00', '09:30:00', 'BOOKED', 'Tu van suc khoe', 'Can kiem tra dinh ky', NULL, 201, TRUE),
(43, 'APT-20260528-023', 43, 3, 4, '2026-05-28', '09:00:00', '09:30:00', 'BOOKED', 'Kiem tra huyet ap', 'Dau hieu xuat hien 2-3 ngay', NULL, 201, TRUE),
(44, 'APT-20260528-024', 44, 4, 5, '2026-05-28', '09:00:00', '09:30:00', 'BOOKED', 'Dau dau', 'Da dung thuoc nhung chua giam', NULL, 201, TRUE),
(45, 'APT-20260528-025', 45, 5, 6, '2026-05-28', '09:00:00', '09:30:00', 'BOOKED', 'Ho sot', 'Mong muon bac si tu van chi tiet', NULL, 201, TRUE),
(46, 'APT-20260528-026', 46, 6, 7, '2026-05-28', '09:00:00', '09:30:00', 'BOOKED', 'Dau bung', 'Trieu chung nhe, can tham kham ban dau', NULL, 201, TRUE),
(47, 'APT-20260528-027', 47, 7, 8, '2026-05-28', '09:00:00', '09:30:00', 'BOOKED', 'Kham da lieu', 'Can kiem tra dinh ky', NULL, 201, TRUE),
(48, 'APT-20260528-028', 48, 8, 9, '2026-05-28', '09:00:00', '09:30:00', 'BOOKED', 'Kham mat', 'Dau hieu xuat hien 2-3 ngay', NULL, 201, TRUE),
(49, 'APT-20260528-029', 49, 9, 10, '2026-05-28', '09:00:00', '09:30:00', 'BOOKED', 'Dau rang', 'Da dung thuoc nhung chua giam', NULL, 201, TRUE),
(50, 'APT-20260528-030', 50, 10, 1, '2026-05-28', '09:00:00', '09:30:00', 'BOOKED', 'Kham nhi', 'Mong muon bac si tu van chi tiet', NULL, 201, TRUE),
(51, 'APT-20260528-031', 51, 1, 2, '2026-05-28', '09:30:00', '10:00:00', 'BOOKED', 'Kham tong quat', 'Trieu chung nhe, can tham kham ban dau', NULL, 201, TRUE),
(52, 'APT-20260528-032', 52, 2, 3, '2026-05-28', '09:30:00', '10:00:00', 'BOOKED', 'Tu van suc khoe', 'Can kiem tra dinh ky', NULL, 201, TRUE),
(53, 'APT-20260528-033', 53, 3, 4, '2026-05-28', '09:30:00', '10:00:00', 'BOOKED', 'Kiem tra huyet ap', 'Dau hieu xuat hien 2-3 ngay', NULL, 201, TRUE),
(54, 'APT-20260528-034', 54, 4, 5, '2026-05-28', '09:30:00', '10:00:00', 'BOOKED', 'Dau dau', 'Da dung thuoc nhung chua giam', NULL, 201, TRUE),
(55, 'APT-20260528-035', 55, 5, 6, '2026-05-28', '09:30:00', '10:00:00', 'BOOKED', 'Ho sot', 'Mong muon bac si tu van chi tiet', NULL, 201, TRUE),
(56, 'APT-20260528-036', 56, 6, 7, '2026-05-28', '09:30:00', '10:00:00', 'BOOKED', 'Dau bung', 'Trieu chung nhe, can tham kham ban dau', NULL, 201, TRUE),
(57, 'APT-20260528-037', 57, 7, 8, '2026-05-28', '09:30:00', '10:00:00', 'BOOKED', 'Kham da lieu', 'Can kiem tra dinh ky', NULL, 201, TRUE),
(58, 'APT-20260528-038', 58, 8, 9, '2026-05-28', '09:30:00', '10:00:00', 'BOOKED', 'Kham mat', 'Dau hieu xuat hien 2-3 ngay', NULL, 201, TRUE),
(59, 'APT-20260528-039', 59, 9, 10, '2026-05-28', '09:30:00', '10:00:00', 'BOOKED', 'Dau rang', 'Da dung thuoc nhung chua giam', NULL, 201, TRUE),
(60, 'APT-20260528-040', 60, 10, 1, '2026-05-28', '09:30:00', '10:00:00', 'BOOKED', 'Kham nhi', 'Mong muon bac si tu van chi tiet', NULL, 201, TRUE),
(61, 'APT-20260528-041', 61, 1, 2, '2026-05-28', '10:00:00', '10:30:00', 'BOOKED', 'Kham tong quat', 'Trieu chung nhe, can tham kham ban dau', NULL, 201, TRUE),
(62, 'APT-20260528-042', 62, 2, 3, '2026-05-28', '10:00:00', '10:30:00', 'BOOKED', 'Tu van suc khoe', 'Can kiem tra dinh ky', NULL, 201, TRUE),
(63, 'APT-20260528-043', 63, 3, 4, '2026-05-28', '10:00:00', '10:30:00', 'BOOKED', 'Kiem tra huyet ap', 'Dau hieu xuat hien 2-3 ngay', NULL, 201, TRUE),
(64, 'APT-20260528-044', 64, 4, 5, '2026-05-28', '10:00:00', '10:30:00', 'BOOKED', 'Dau dau', 'Da dung thuoc nhung chua giam', NULL, 201, TRUE),
(65, 'APT-20260528-045', 65, 5, 6, '2026-05-28', '10:00:00', '10:30:00', 'BOOKED', 'Ho sot', 'Mong muon bac si tu van chi tiet', NULL, 201, TRUE),
(66, 'APT-20260528-046', 66, 6, 7, '2026-05-28', '10:00:00', '10:30:00', 'BOOKED', 'Dau bung', 'Trieu chung nhe, can tham kham ban dau', NULL, 201, TRUE),
(67, 'APT-20260528-047', 67, 7, 8, '2026-05-28', '10:00:00', '10:30:00', 'BOOKED', 'Kham da lieu', 'Can kiem tra dinh ky', NULL, 201, TRUE),
(68, 'APT-20260528-048', 68, 8, 9, '2026-05-28', '10:00:00', '10:30:00', 'BOOKED', 'Kham mat', 'Dau hieu xuat hien 2-3 ngay', NULL, 201, TRUE),
(69, 'APT-20260528-049', 69, 9, 10, '2026-05-28', '10:00:00', '10:30:00', 'BOOKED', 'Dau rang', 'Da dung thuoc nhung chua giam', NULL, 201, TRUE),
(70, 'APT-20260528-050', 70, 10, 1, '2026-05-28', '10:00:00', '10:30:00', 'BOOKED', 'Kham nhi', 'Mong muon bac si tu van chi tiet', NULL, 201, TRUE),
(71, 'APT-20260528-051', 71, 1, 2, '2026-05-28', '10:30:00', '11:00:00', 'BOOKED', 'Kham tong quat', 'Trieu chung nhe, can tham kham ban dau', NULL, 201, TRUE),
(72, 'APT-20260528-052', 72, 2, 3, '2026-05-28', '10:30:00', '11:00:00', 'BOOKED', 'Tu van suc khoe', 'Can kiem tra dinh ky', NULL, 201, TRUE),
(73, 'APT-20260528-053', 73, 3, 4, '2026-05-28', '10:30:00', '11:00:00', 'BOOKED', 'Kiem tra huyet ap', 'Dau hieu xuat hien 2-3 ngay', NULL, 201, TRUE),
(74, 'APT-20260528-054', 74, 4, 5, '2026-05-28', '10:30:00', '11:00:00', 'BOOKED', 'Dau dau', 'Da dung thuoc nhung chua giam', NULL, 201, TRUE),
(75, 'APT-20260528-055', 75, 5, 6, '2026-05-28', '10:30:00', '11:00:00', 'BOOKED', 'Ho sot', 'Mong muon bac si tu van chi tiet', NULL, 201, TRUE),
(76, 'APT-20260528-056', 76, 6, 7, '2026-05-28', '10:30:00', '11:00:00', 'BOOKED', 'Dau bung', 'Trieu chung nhe, can tham kham ban dau', NULL, 201, TRUE),
(77, 'APT-20260528-057', 77, 7, 8, '2026-05-28', '10:30:00', '11:00:00', 'BOOKED', 'Kham da lieu', 'Can kiem tra dinh ky', NULL, 201, TRUE),
(78, 'APT-20260528-058', 78, 8, 9, '2026-05-28', '10:30:00', '11:00:00', 'BOOKED', 'Kham mat', 'Dau hieu xuat hien 2-3 ngay', NULL, 201, TRUE),
(79, 'APT-20260528-059', 79, 9, 10, '2026-05-28', '10:30:00', '11:00:00', 'BOOKED', 'Dau rang', 'Da dung thuoc nhung chua giam', NULL, 201, TRUE),
(80, 'APT-20260528-060', 80, 10, 1, '2026-05-28', '10:30:00', '11:00:00', 'BOOKED', 'Kham nhi', 'Mong muon bac si tu van chi tiet', NULL, 201, TRUE),
(81, 'APT-20260528-061', 81, 1, 2, '2026-05-28', '11:00:00', '11:30:00', 'BOOKED', 'Kham tong quat', 'Trieu chung nhe, can tham kham ban dau', NULL, 201, TRUE),
(82, 'APT-20260528-062', 82, 2, 3, '2026-05-28', '11:00:00', '11:30:00', 'BOOKED', 'Tu van suc khoe', 'Can kiem tra dinh ky', NULL, 201, TRUE),
(83, 'APT-20260528-063', 83, 3, 4, '2026-05-28', '11:00:00', '11:30:00', 'BOOKED', 'Kiem tra huyet ap', 'Dau hieu xuat hien 2-3 ngay', NULL, 201, TRUE),
(84, 'APT-20260528-064', 84, 4, 5, '2026-05-28', '11:00:00', '11:30:00', 'BOOKED', 'Dau dau', 'Da dung thuoc nhung chua giam', NULL, 201, TRUE),
(85, 'APT-20260528-065', 85, 5, 6, '2026-05-28', '11:00:00', '11:30:00', 'BOOKED', 'Ho sot', 'Mong muon bac si tu van chi tiet', NULL, 201, TRUE),
(86, 'APT-20260528-066', 86, 6, 7, '2026-05-28', '11:00:00', '11:30:00', 'BOOKED', 'Dau bung', 'Trieu chung nhe, can tham kham ban dau', NULL, 201, TRUE),
(87, 'APT-20260528-067', 87, 7, 8, '2026-05-28', '11:00:00', '11:30:00', 'BOOKED', 'Kham da lieu', 'Can kiem tra dinh ky', NULL, 201, TRUE),
(88, 'APT-20260528-068', 88, 8, 9, '2026-05-28', '11:00:00', '11:30:00', 'BOOKED', 'Kham mat', 'Dau hieu xuat hien 2-3 ngay', NULL, 201, TRUE),
(89, 'APT-20260528-069', 89, 9, 10, '2026-05-28', '11:00:00', '11:30:00', 'BOOKED', 'Dau rang', 'Da dung thuoc nhung chua giam', NULL, 201, TRUE),
(90, 'APT-20260528-070', 90, 10, 1, '2026-05-28', '11:00:00', '11:30:00', 'BOOKED', 'Kham nhi', 'Mong muon bac si tu van chi tiet', NULL, 201, TRUE),
(91, 'APT-20260529-001', 91, 1, 2, '2026-05-29', '08:00:00', '08:30:00', 'BOOKED', 'Kham tong quat', 'Trieu chung nhe, can tham kham ban dau', NULL, 201, TRUE),
(92, 'APT-20260529-002', 92, 2, 3, '2026-05-29', '08:00:00', '08:30:00', 'BOOKED', 'Tu van suc khoe', 'Can kiem tra dinh ky', NULL, 201, TRUE),
(93, 'APT-20260529-003', 93, 3, 4, '2026-05-29', '08:00:00', '08:30:00', 'BOOKED', 'Kiem tra huyet ap', 'Dau hieu xuat hien 2-3 ngay', NULL, 201, TRUE),
(94, 'APT-20260529-004', 94, 4, 5, '2026-05-29', '08:00:00', '08:30:00', 'BOOKED', 'Dau dau', 'Da dung thuoc nhung chua giam', NULL, 201, TRUE),
(95, 'APT-20260529-005', 95, 5, 6, '2026-05-29', '08:00:00', '08:30:00', 'BOOKED', 'Ho sot', 'Mong muon bac si tu van chi tiet', NULL, 201, TRUE),
(96, 'APT-20260529-006', 96, 6, 7, '2026-05-29', '08:00:00', '08:30:00', 'BOOKED', 'Dau bung', 'Trieu chung nhe, can tham kham ban dau', NULL, 201, TRUE),
(97, 'APT-20260529-007', 97, 7, 8, '2026-05-29', '08:00:00', '08:30:00', 'BOOKED', 'Kham da lieu', 'Can kiem tra dinh ky', NULL, 201, TRUE),
(98, 'APT-20260529-008', 98, 8, 9, '2026-05-29', '08:00:00', '08:30:00', 'BOOKED', 'Kham mat', 'Dau hieu xuat hien 2-3 ngay', NULL, 201, TRUE),
(99, 'APT-20260529-009', 99, 9, 10, '2026-05-29', '08:00:00', '08:30:00', 'BOOKED', 'Dau rang', 'Da dung thuoc nhung chua giam', NULL, 201, TRUE),
(100, 'APT-20260529-010', 100, 10, 1, '2026-05-29', '08:00:00', '08:30:00', 'BOOKED', 'Kham nhi', 'Mong muon bac si tu van chi tiet', NULL, 201, TRUE),
(101, 'APT-20260529-011', 101, 1, 2, '2026-05-29', '08:30:00', '09:00:00', 'BOOKED', 'Kham tong quat', 'Trieu chung nhe, can tham kham ban dau', NULL, 201, TRUE),
(102, 'APT-20260529-012', 102, 2, 3, '2026-05-29', '08:30:00', '09:00:00', 'BOOKED', 'Tu van suc khoe', 'Can kiem tra dinh ky', NULL, 201, TRUE),
(103, 'APT-20260529-013', 103, 3, 4, '2026-05-29', '08:30:00', '09:00:00', 'BOOKED', 'Kiem tra huyet ap', 'Dau hieu xuat hien 2-3 ngay', NULL, 201, TRUE),
(104, 'APT-20260529-014', 104, 4, 5, '2026-05-29', '08:30:00', '09:00:00', 'BOOKED', 'Dau dau', 'Da dung thuoc nhung chua giam', NULL, 201, TRUE),
(105, 'APT-20260529-015', 105, 5, 6, '2026-05-29', '08:30:00', '09:00:00', 'BOOKED', 'Ho sot', 'Mong muon bac si tu van chi tiet', NULL, 201, TRUE),
(106, 'APT-20260529-016', 106, 6, 7, '2026-05-29', '08:30:00', '09:00:00', 'BOOKED', 'Dau bung', 'Trieu chung nhe, can tham kham ban dau', NULL, 201, TRUE),
(107, 'APT-20260529-017', 107, 7, 8, '2026-05-29', '08:30:00', '09:00:00', 'BOOKED', 'Kham da lieu', 'Can kiem tra dinh ky', NULL, 201, TRUE),
(108, 'APT-20260529-018', 108, 8, 9, '2026-05-29', '08:30:00', '09:00:00', 'BOOKED', 'Kham mat', 'Dau hieu xuat hien 2-3 ngay', NULL, 201, TRUE),
(109, 'APT-20260529-019', 109, 9, 10, '2026-05-29', '08:30:00', '09:00:00', 'BOOKED', 'Dau rang', 'Da dung thuoc nhung chua giam', NULL, 201, TRUE),
(110, 'APT-20260529-020', 110, 10, 1, '2026-05-29', '08:30:00', '09:00:00', 'BOOKED', 'Kham nhi', 'Mong muon bac si tu van chi tiet', NULL, 201, TRUE),
(111, 'APT-20260530-001', 111, 1, 2, '2026-05-30', '08:00:00', '08:30:00', 'BOOKED', 'Kham tong quat', 'Trieu chung nhe, can tham kham ban dau', NULL, 201, TRUE),
(112, 'APT-20260530-002', 112, 2, 3, '2026-05-30', '08:00:00', '08:30:00', 'BOOKED', 'Tu van suc khoe', 'Can kiem tra dinh ky', NULL, 201, TRUE),
(113, 'APT-20260530-003', 113, 3, 4, '2026-05-30', '08:00:00', '08:30:00', 'BOOKED', 'Kiem tra huyet ap', 'Dau hieu xuat hien 2-3 ngay', NULL, 201, TRUE),
(114, 'APT-20260530-004', 114, 4, 5, '2026-05-30', '08:00:00', '08:30:00', 'BOOKED', 'Dau dau', 'Da dung thuoc nhung chua giam', NULL, 201, TRUE),
(115, 'APT-20260530-005', 115, 5, 6, '2026-05-30', '08:00:00', '08:30:00', 'BOOKED', 'Ho sot', 'Mong muon bac si tu van chi tiet', NULL, 201, TRUE),
(116, 'APT-20260530-006', 116, 6, 7, '2026-05-30', '08:00:00', '08:30:00', 'BOOKED', 'Dau bung', 'Trieu chung nhe, can tham kham ban dau', NULL, 201, TRUE),
(117, 'APT-20260530-007', 117, 7, 8, '2026-05-30', '08:00:00', '08:30:00', 'BOOKED', 'Kham da lieu', 'Can kiem tra dinh ky', NULL, 201, TRUE),
(118, 'APT-20260530-008', 118, 8, 9, '2026-05-30', '08:00:00', '08:30:00', 'BOOKED', 'Kham mat', 'Dau hieu xuat hien 2-3 ngay', NULL, 201, TRUE),
(119, 'APT-20260530-009', 119, 9, 10, '2026-05-30', '08:00:00', '08:30:00', 'BOOKED', 'Dau rang', 'Da dung thuoc nhung chua giam', NULL, 201, TRUE),
(120, 'APT-20260530-010', 120, 10, 1, '2026-05-30', '08:00:00', '08:30:00', 'BOOKED', 'Kham nhi', 'Mong muon bac si tu van chi tiet', NULL, 201, TRUE),
(121, 'APT-20260530-011', 121, 1, 2, '2026-05-30', '08:30:00', '09:00:00', 'BOOKED', 'Kham tong quat', 'Trieu chung nhe, can tham kham ban dau', NULL, 201, TRUE),
(122, 'APT-20260530-012', 122, 2, 3, '2026-05-30', '08:30:00', '09:00:00', 'BOOKED', 'Tu van suc khoe', 'Can kiem tra dinh ky', NULL, 201, TRUE),
(123, 'APT-20260530-013', 123, 3, 4, '2026-05-30', '08:30:00', '09:00:00', 'BOOKED', 'Kiem tra huyet ap', 'Dau hieu xuat hien 2-3 ngay', NULL, 201, TRUE),
(124, 'APT-20260530-014', 124, 4, 5, '2026-05-30', '08:30:00', '09:00:00', 'BOOKED', 'Dau dau', 'Da dung thuoc nhung chua giam', NULL, 201, TRUE),
(125, 'APT-20260530-015', 125, 5, 6, '2026-05-30', '08:30:00', '09:00:00', 'BOOKED', 'Ho sot', 'Mong muon bac si tu van chi tiet', NULL, 201, TRUE),
(126, 'APT-20260530-016', 126, 6, 7, '2026-05-30', '08:30:00', '09:00:00', 'BOOKED', 'Dau bung', 'Trieu chung nhe, can tham kham ban dau', NULL, 201, TRUE),
(127, 'APT-20260530-017', 127, 7, 8, '2026-05-30', '08:30:00', '09:00:00', 'BOOKED', 'Kham da lieu', 'Can kiem tra dinh ky', NULL, 201, TRUE),
(128, 'APT-20260530-018', 128, 8, 9, '2026-05-30', '08:30:00', '09:00:00', 'BOOKED', 'Kham mat', 'Dau hieu xuat hien 2-3 ngay', NULL, 201, TRUE),
(129, 'APT-20260530-019', 129, 9, 10, '2026-05-30', '08:30:00', '09:00:00', 'BOOKED', 'Dau rang', 'Da dung thuoc nhung chua giam', NULL, 201, TRUE),
(130, 'APT-20260530-020', 130, 10, 1, '2026-05-30', '08:30:00', '09:00:00', 'BOOKED', 'Kham nhi', 'Mong muon bac si tu van chi tiet', NULL, 201, TRUE),
(131, 'APT-20260531-001', 131, 1, 2, '2026-05-31', '08:00:00', '08:30:00', 'BOOKED', 'Kham tong quat', 'Trieu chung nhe, can tham kham ban dau', NULL, 201, TRUE),
(132, 'APT-20260531-002', 132, 2, 3, '2026-05-31', '08:00:00', '08:30:00', 'BOOKED', 'Tu van suc khoe', 'Can kiem tra dinh ky', NULL, 201, TRUE),
(133, 'APT-20260531-003', 133, 3, 4, '2026-05-31', '08:00:00', '08:30:00', 'BOOKED', 'Kiem tra huyet ap', 'Dau hieu xuat hien 2-3 ngay', NULL, 201, TRUE),
(134, 'APT-20260531-004', 134, 4, 5, '2026-05-31', '08:00:00', '08:30:00', 'BOOKED', 'Dau dau', 'Da dung thuoc nhung chua giam', NULL, 201, TRUE),
(135, 'APT-20260531-005', 135, 5, 6, '2026-05-31', '08:00:00', '08:30:00', 'BOOKED', 'Ho sot', 'Mong muon bac si tu van chi tiet', NULL, 201, TRUE),
(136, 'APT-20260531-006', 136, 6, 7, '2026-05-31', '08:00:00', '08:30:00', 'BOOKED', 'Dau bung', 'Trieu chung nhe, can tham kham ban dau', NULL, 201, TRUE),
(137, 'APT-20260531-007', 137, 7, 8, '2026-05-31', '08:00:00', '08:30:00', 'BOOKED', 'Kham da lieu', 'Can kiem tra dinh ky', NULL, 201, TRUE),
(138, 'APT-20260531-008', 138, 8, 9, '2026-05-31', '08:00:00', '08:30:00', 'BOOKED', 'Kham mat', 'Dau hieu xuat hien 2-3 ngay', NULL, 201, TRUE),
(139, 'APT-20260531-009', 139, 9, 10, '2026-05-31', '08:00:00', '08:30:00', 'BOOKED', 'Dau rang', 'Da dung thuoc nhung chua giam', NULL, 201, TRUE),
(140, 'APT-20260531-010', 140, 10, 1, '2026-05-31', '08:00:00', '08:30:00', 'BOOKED', 'Kham nhi', 'Mong muon bac si tu van chi tiet', NULL, 201, TRUE),
(141, 'APT-20260531-011', 141, 1, 2, '2026-05-31', '08:30:00', '09:00:00', 'BOOKED', 'Kham tong quat', 'Trieu chung nhe, can tham kham ban dau', NULL, 201, TRUE),
(142, 'APT-20260531-012', 142, 2, 3, '2026-05-31', '08:30:00', '09:00:00', 'BOOKED', 'Tu van suc khoe', 'Can kiem tra dinh ky', NULL, 201, TRUE),
(143, 'APT-20260531-013', 143, 3, 4, '2026-05-31', '08:30:00', '09:00:00', 'BOOKED', 'Kiem tra huyet ap', 'Dau hieu xuat hien 2-3 ngay', NULL, 201, TRUE),
(144, 'APT-20260531-014', 144, 4, 5, '2026-05-31', '08:30:00', '09:00:00', 'BOOKED', 'Dau dau', 'Da dung thuoc nhung chua giam', NULL, 201, TRUE),
(145, 'APT-20260531-015', 145, 5, 6, '2026-05-31', '08:30:00', '09:00:00', 'BOOKED', 'Ho sot', 'Mong muon bac si tu van chi tiet', NULL, 201, TRUE),
(146, 'APT-20260531-016', 146, 6, 7, '2026-05-31', '08:30:00', '09:00:00', 'BOOKED', 'Dau bung', 'Trieu chung nhe, can tham kham ban dau', NULL, 201, TRUE),
(147, 'APT-20260531-017', 147, 7, 8, '2026-05-31', '08:30:00', '09:00:00', 'BOOKED', 'Kham da lieu', 'Can kiem tra dinh ky', NULL, 201, TRUE),
(148, 'APT-20260531-018', 148, 8, 9, '2026-05-31', '08:30:00', '09:00:00', 'BOOKED', 'Kham mat', 'Dau hieu xuat hien 2-3 ngay', NULL, 201, TRUE),
(149, 'APT-20260531-019', 149, 9, 10, '2026-05-31', '08:30:00', '09:00:00', 'BOOKED', 'Dau rang', 'Da dung thuoc nhung chua giam', NULL, 201, TRUE),
(150, 'APT-20260531-020', 150, 10, 1, '2026-05-31', '08:30:00', '09:00:00', 'BOOKED', 'Kham nhi', 'Mong muon bac si tu van chi tiet', NULL, 201, TRUE),
(151, 'APT-20260601-001', 151, 1, 2, '2026-06-01', '08:00:00', '08:30:00', 'BOOKED', 'Kham tong quat', 'Trieu chung nhe, can tham kham ban dau', NULL, 201, TRUE),
(152, 'APT-20260601-002', 152, 2, 3, '2026-06-01', '08:00:00', '08:30:00', 'BOOKED', 'Tu van suc khoe', 'Can kiem tra dinh ky', NULL, 201, TRUE),
(153, 'APT-20260601-003', 153, 3, 4, '2026-06-01', '08:00:00', '08:30:00', 'BOOKED', 'Kiem tra huyet ap', 'Dau hieu xuat hien 2-3 ngay', NULL, 201, TRUE),
(154, 'APT-20260601-004', 154, 4, 5, '2026-06-01', '08:00:00', '08:30:00', 'BOOKED', 'Dau dau', 'Da dung thuoc nhung chua giam', NULL, 201, TRUE),
(155, 'APT-20260601-005', 155, 5, 6, '2026-06-01', '08:00:00', '08:30:00', 'BOOKED', 'Ho sot', 'Mong muon bac si tu van chi tiet', NULL, 201, TRUE),
(156, 'APT-20260601-006', 156, 6, 7, '2026-06-01', '08:00:00', '08:30:00', 'BOOKED', 'Dau bung', 'Trieu chung nhe, can tham kham ban dau', NULL, 201, TRUE),
(157, 'APT-20260601-007', 157, 7, 8, '2026-06-01', '08:00:00', '08:30:00', 'BOOKED', 'Kham da lieu', 'Can kiem tra dinh ky', NULL, 201, TRUE),
(158, 'APT-20260601-008', 158, 8, 9, '2026-06-01', '08:00:00', '08:30:00', 'BOOKED', 'Kham mat', 'Dau hieu xuat hien 2-3 ngay', NULL, 201, TRUE),
(159, 'APT-20260601-009', 159, 9, 10, '2026-06-01', '08:00:00', '08:30:00', 'BOOKED', 'Dau rang', 'Da dung thuoc nhung chua giam', NULL, 201, TRUE),
(160, 'APT-20260601-010', 160, 10, 1, '2026-06-01', '08:00:00', '08:30:00', 'BOOKED', 'Kham nhi', 'Mong muon bac si tu van chi tiet', NULL, 201, TRUE),
(161, 'APT-20260601-011', 161, 1, 2, '2026-06-01', '08:30:00', '09:00:00', 'BOOKED', 'Kham tong quat', 'Trieu chung nhe, can tham kham ban dau', NULL, 201, TRUE),
(162, 'APT-20260601-012', 162, 2, 3, '2026-06-01', '08:30:00', '09:00:00', 'BOOKED', 'Tu van suc khoe', 'Can kiem tra dinh ky', NULL, 201, TRUE),
(163, 'APT-20260601-013', 163, 3, 4, '2026-06-01', '08:30:00', '09:00:00', 'BOOKED', 'Kiem tra huyet ap', 'Dau hieu xuat hien 2-3 ngay', NULL, 201, TRUE),
(164, 'APT-20260601-014', 164, 4, 5, '2026-06-01', '08:30:00', '09:00:00', 'BOOKED', 'Dau dau', 'Da dung thuoc nhung chua giam', NULL, 201, TRUE),
(165, 'APT-20260601-015', 165, 5, 6, '2026-06-01', '08:30:00', '09:00:00', 'BOOKED', 'Ho sot', 'Mong muon bac si tu van chi tiet', NULL, 201, TRUE),
(166, 'APT-20260601-016', 166, 6, 7, '2026-06-01', '08:30:00', '09:00:00', 'BOOKED', 'Dau bung', 'Trieu chung nhe, can tham kham ban dau', NULL, 201, TRUE),
(167, 'APT-20260601-017', 167, 7, 8, '2026-06-01', '08:30:00', '09:00:00', 'BOOKED', 'Kham da lieu', 'Can kiem tra dinh ky', NULL, 201, TRUE),
(168, 'APT-20260601-018', 168, 8, 9, '2026-06-01', '08:30:00', '09:00:00', 'BOOKED', 'Kham mat', 'Dau hieu xuat hien 2-3 ngay', NULL, 201, TRUE),
(169, 'APT-20260601-019', 169, 9, 10, '2026-06-01', '08:30:00', '09:00:00', 'BOOKED', 'Dau rang', 'Da dung thuoc nhung chua giam', NULL, 201, TRUE),
(170, 'APT-20260601-020', 170, 10, 1, '2026-06-01', '08:30:00', '09:00:00', 'BOOKED', 'Kham nhi', 'Mong muon bac si tu van chi tiet', NULL, 201, TRUE),
(171, 'APT-20260602-001', 171, 1, 2, '2026-06-02', '08:00:00', '08:30:00', 'BOOKED', 'Kham tong quat', 'Trieu chung nhe, can tham kham ban dau', NULL, 201, TRUE),
(172, 'APT-20260602-002', 172, 2, 3, '2026-06-02', '08:00:00', '08:30:00', 'BOOKED', 'Tu van suc khoe', 'Can kiem tra dinh ky', NULL, 201, TRUE),
(173, 'APT-20260602-003', 173, 3, 4, '2026-06-02', '08:00:00', '08:30:00', 'BOOKED', 'Kiem tra huyet ap', 'Dau hieu xuat hien 2-3 ngay', NULL, 201, TRUE),
(174, 'APT-20260602-004', 174, 4, 5, '2026-06-02', '08:00:00', '08:30:00', 'BOOKED', 'Dau dau', 'Da dung thuoc nhung chua giam', NULL, 201, TRUE),
(175, 'APT-20260602-005', 175, 5, 6, '2026-06-02', '08:00:00', '08:30:00', 'BOOKED', 'Ho sot', 'Mong muon bac si tu van chi tiet', NULL, 201, TRUE),
(176, 'APT-20260602-006', 176, 6, 7, '2026-06-02', '08:00:00', '08:30:00', 'BOOKED', 'Dau bung', 'Trieu chung nhe, can tham kham ban dau', NULL, 201, TRUE),
(177, 'APT-20260602-007', 177, 7, 8, '2026-06-02', '08:00:00', '08:30:00', 'BOOKED', 'Kham da lieu', 'Can kiem tra dinh ky', NULL, 201, TRUE),
(178, 'APT-20260602-008', 178, 8, 9, '2026-06-02', '08:00:00', '08:30:00', 'BOOKED', 'Kham mat', 'Dau hieu xuat hien 2-3 ngay', NULL, 201, TRUE),
(179, 'APT-20260602-009', 179, 9, 10, '2026-06-02', '08:00:00', '08:30:00', 'BOOKED', 'Dau rang', 'Da dung thuoc nhung chua giam', NULL, 201, TRUE),
(180, 'APT-20260602-010', 180, 10, 1, '2026-06-02', '08:00:00', '08:30:00', 'BOOKED', 'Kham nhi', 'Mong muon bac si tu van chi tiet', NULL, 201, TRUE),
(181, 'APT-20260602-011', 181, 1, 2, '2026-06-02', '08:30:00', '09:00:00', 'BOOKED', 'Kham tong quat', 'Trieu chung nhe, can tham kham ban dau', NULL, 201, TRUE),
(182, 'APT-20260602-012', 182, 2, 3, '2026-06-02', '08:30:00', '09:00:00', 'BOOKED', 'Tu van suc khoe', 'Can kiem tra dinh ky', NULL, 201, TRUE),
(183, 'APT-20260602-013', 183, 3, 4, '2026-06-02', '08:30:00', '09:00:00', 'BOOKED', 'Kiem tra huyet ap', 'Dau hieu xuat hien 2-3 ngay', NULL, 201, TRUE),
(184, 'APT-20260602-014', 184, 4, 5, '2026-06-02', '08:30:00', '09:00:00', 'BOOKED', 'Dau dau', 'Da dung thuoc nhung chua giam', NULL, 201, TRUE),
(185, 'APT-20260602-015', 185, 5, 6, '2026-06-02', '08:30:00', '09:00:00', 'BOOKED', 'Ho sot', 'Mong muon bac si tu van chi tiet', NULL, 201, TRUE),
(186, 'APT-20260602-016', 186, 6, 7, '2026-06-02', '08:30:00', '09:00:00', 'BOOKED', 'Dau bung', 'Trieu chung nhe, can tham kham ban dau', NULL, 201, TRUE),
(187, 'APT-20260602-017', 187, 7, 8, '2026-06-02', '08:30:00', '09:00:00', 'BOOKED', 'Kham da lieu', 'Can kiem tra dinh ky', NULL, 201, TRUE),
(188, 'APT-20260602-018', 188, 8, 9, '2026-06-02', '08:30:00', '09:00:00', 'BOOKED', 'Kham mat', 'Dau hieu xuat hien 2-3 ngay', NULL, 201, TRUE),
(189, 'APT-20260602-019', 189, 9, 10, '2026-06-02', '08:30:00', '09:00:00', 'BOOKED', 'Dau rang', 'Da dung thuoc nhung chua giam', NULL, 201, TRUE),
(190, 'APT-20260602-020', 190, 10, 1, '2026-06-02', '08:30:00', '09:00:00', 'BOOKED', 'Kham nhi', 'Mong muon bac si tu van chi tiet', NULL, 201, TRUE),
(191, 'APT-20260603-001', 191, 1, 2, '2026-06-03', '08:00:00', '08:30:00', 'BOOKED', 'Kham tong quat', 'Trieu chung nhe, can tham kham ban dau', NULL, 201, TRUE),
(192, 'APT-20260603-002', 192, 2, 3, '2026-06-03', '08:00:00', '08:30:00', 'BOOKED', 'Tu van suc khoe', 'Can kiem tra dinh ky', NULL, 201, TRUE),
(193, 'APT-20260603-003', 193, 3, 4, '2026-06-03', '08:00:00', '08:30:00', 'BOOKED', 'Kiem tra huyet ap', 'Dau hieu xuat hien 2-3 ngay', NULL, 201, TRUE),
(194, 'APT-20260603-004', 194, 4, 5, '2026-06-03', '08:00:00', '08:30:00', 'BOOKED', 'Dau dau', 'Da dung thuoc nhung chua giam', NULL, 201, TRUE),
(195, 'APT-20260603-005', 195, 5, 6, '2026-06-03', '08:00:00', '08:30:00', 'BOOKED', 'Ho sot', 'Mong muon bac si tu van chi tiet', NULL, 201, TRUE),
(196, 'APT-20260603-006', 196, 6, 7, '2026-06-03', '08:00:00', '08:30:00', 'BOOKED', 'Dau bung', 'Trieu chung nhe, can tham kham ban dau', NULL, 201, TRUE),
(197, 'APT-20260603-007', 197, 7, 8, '2026-06-03', '08:00:00', '08:30:00', 'BOOKED', 'Kham da lieu', 'Can kiem tra dinh ky', NULL, 201, TRUE),
(198, 'APT-20260603-008', 198, 8, 9, '2026-06-03', '08:00:00', '08:30:00', 'BOOKED', 'Kham mat', 'Dau hieu xuat hien 2-3 ngay', NULL, 201, TRUE),
(199, 'APT-20260603-009', 199, 9, 10, '2026-06-03', '08:00:00', '08:30:00', 'BOOKED', 'Dau rang', 'Da dung thuoc nhung chua giam', NULL, 201, TRUE),
(200, 'APT-20260603-010', 200, 10, 1, '2026-06-03', '08:00:00', '08:30:00', 'BOOKED', 'Kham nhi', 'Mong muon bac si tu van chi tiet', NULL, 201, TRUE),
(201, 'APT-20260603-011', 201, 1, 2, '2026-06-03', '08:30:00', '09:00:00', 'BOOKED', 'Kham tong quat', 'Trieu chung nhe, can tham kham ban dau', NULL, 201, TRUE),
(202, 'APT-20260603-012', 202, 2, 3, '2026-06-03', '08:30:00', '09:00:00', 'BOOKED', 'Tu van suc khoe', 'Can kiem tra dinh ky', NULL, 201, TRUE),
(203, 'APT-20260603-013', 203, 3, 4, '2026-06-03', '08:30:00', '09:00:00', 'BOOKED', 'Kiem tra huyet ap', 'Dau hieu xuat hien 2-3 ngay', NULL, 201, TRUE),
(204, 'APT-20260603-014', 204, 4, 5, '2026-06-03', '08:30:00', '09:00:00', 'BOOKED', 'Dau dau', 'Da dung thuoc nhung chua giam', NULL, 201, TRUE),
(205, 'APT-20260603-015', 205, 5, 6, '2026-06-03', '08:30:00', '09:00:00', 'BOOKED', 'Ho sot', 'Mong muon bac si tu van chi tiet', NULL, 201, TRUE),
(206, 'APT-20260603-016', 206, 6, 7, '2026-06-03', '08:30:00', '09:00:00', 'BOOKED', 'Dau bung', 'Trieu chung nhe, can tham kham ban dau', NULL, 201, TRUE),
(207, 'APT-20260603-017', 207, 7, 8, '2026-06-03', '08:30:00', '09:00:00', 'BOOKED', 'Kham da lieu', 'Can kiem tra dinh ky', NULL, 201, TRUE),
(208, 'APT-20260603-018', 208, 8, 9, '2026-06-03', '08:30:00', '09:00:00', 'BOOKED', 'Kham mat', 'Dau hieu xuat hien 2-3 ngay', NULL, 201, TRUE),
(209, 'APT-20260603-019', 209, 9, 10, '2026-06-03', '08:30:00', '09:00:00', 'BOOKED', 'Dau rang', 'Da dung thuoc nhung chua giam', NULL, 201, TRUE),
(210, 'APT-20260603-020', 210, 10, 1, '2026-06-03', '08:30:00', '09:00:00', 'BOOKED', 'Kham nhi', 'Mong muon bac si tu van chi tiet', NULL, 201, TRUE),
(211, 'APT-20260604-001', 211, 1, 2, '2026-06-04', '08:00:00', '08:30:00', 'BOOKED', 'Kham tong quat', 'Trieu chung nhe, can tham kham ban dau', NULL, 201, TRUE),
(212, 'APT-20260604-002', 212, 2, 3, '2026-06-04', '08:00:00', '08:30:00', 'BOOKED', 'Tu van suc khoe', 'Can kiem tra dinh ky', NULL, 201, TRUE),
(213, 'APT-20260604-003', 213, 3, 4, '2026-06-04', '08:00:00', '08:30:00', 'BOOKED', 'Kiem tra huyet ap', 'Dau hieu xuat hien 2-3 ngay', NULL, 201, TRUE),
(214, 'APT-20260604-004', 214, 4, 5, '2026-06-04', '08:00:00', '08:30:00', 'BOOKED', 'Dau dau', 'Da dung thuoc nhung chua giam', NULL, 201, TRUE),
(215, 'APT-20260604-005', 215, 5, 6, '2026-06-04', '08:00:00', '08:30:00', 'BOOKED', 'Ho sot', 'Mong muon bac si tu van chi tiet', NULL, 201, TRUE),
(216, 'APT-20260604-006', 216, 6, 7, '2026-06-04', '08:00:00', '08:30:00', 'BOOKED', 'Dau bung', 'Trieu chung nhe, can tham kham ban dau', NULL, 201, TRUE),
(217, 'APT-20260604-007', 217, 7, 8, '2026-06-04', '08:00:00', '08:30:00', 'BOOKED', 'Kham da lieu', 'Can kiem tra dinh ky', NULL, 201, TRUE),
(218, 'APT-20260604-008', 218, 8, 9, '2026-06-04', '08:00:00', '08:30:00', 'BOOKED', 'Kham mat', 'Dau hieu xuat hien 2-3 ngay', NULL, 201, TRUE),
(219, 'APT-20260604-009', 219, 9, 10, '2026-06-04', '08:00:00', '08:30:00', 'BOOKED', 'Dau rang', 'Da dung thuoc nhung chua giam', NULL, 201, TRUE),
(220, 'APT-20260604-010', 220, 10, 1, '2026-06-04', '08:00:00', '08:30:00', 'BOOKED', 'Kham nhi', 'Mong muon bac si tu van chi tiet', NULL, 201, TRUE),
(221, 'APT-20260604-011', 221, 1, 2, '2026-06-04', '08:30:00', '09:00:00', 'BOOKED', 'Kham tong quat', 'Trieu chung nhe, can tham kham ban dau', NULL, 201, TRUE),
(222, 'APT-20260604-012', 222, 2, 3, '2026-06-04', '08:30:00', '09:00:00', 'BOOKED', 'Tu van suc khoe', 'Can kiem tra dinh ky', NULL, 201, TRUE),
(223, 'APT-20260604-013', 223, 3, 4, '2026-06-04', '08:30:00', '09:00:00', 'BOOKED', 'Kiem tra huyet ap', 'Dau hieu xuat hien 2-3 ngay', NULL, 201, TRUE),
(224, 'APT-20260604-014', 224, 4, 5, '2026-06-04', '08:30:00', '09:00:00', 'BOOKED', 'Dau dau', 'Da dung thuoc nhung chua giam', NULL, 201, TRUE),
(225, 'APT-20260604-015', 225, 5, 6, '2026-06-04', '08:30:00', '09:00:00', 'BOOKED', 'Ho sot', 'Mong muon bac si tu van chi tiet', NULL, 201, TRUE),
(226, 'APT-20260604-016', 226, 6, 7, '2026-06-04', '08:30:00', '09:00:00', 'BOOKED', 'Dau bung', 'Trieu chung nhe, can tham kham ban dau', NULL, 201, TRUE),
(227, 'APT-20260604-017', 227, 7, 8, '2026-06-04', '08:30:00', '09:00:00', 'BOOKED', 'Kham da lieu', 'Can kiem tra dinh ky', NULL, 201, TRUE),
(228, 'APT-20260604-018', 228, 8, 9, '2026-06-04', '08:30:00', '09:00:00', 'BOOKED', 'Kham mat', 'Dau hieu xuat hien 2-3 ngay', NULL, 201, TRUE),
(229, 'APT-20260604-019', 229, 9, 10, '2026-06-04', '08:30:00', '09:00:00', 'BOOKED', 'Dau rang', 'Da dung thuoc nhung chua giam', NULL, 201, TRUE),
(230, 'APT-20260604-020', 230, 10, 1, '2026-06-04', '08:30:00', '09:00:00', 'BOOKED', 'Kham nhi', 'Mong muon bac si tu van chi tiet', NULL, 201, TRUE),
(231, 'APT-20260605-001', 231, 1, 2, '2026-06-05', '08:00:00', '08:30:00', 'BOOKED', 'Kham tong quat', 'Trieu chung nhe, can tham kham ban dau', NULL, 201, TRUE),
(232, 'APT-20260605-002', 232, 2, 3, '2026-06-05', '08:00:00', '08:30:00', 'BOOKED', 'Tu van suc khoe', 'Can kiem tra dinh ky', NULL, 201, TRUE),
(233, 'APT-20260605-003', 233, 3, 4, '2026-06-05', '08:00:00', '08:30:00', 'BOOKED', 'Kiem tra huyet ap', 'Dau hieu xuat hien 2-3 ngay', NULL, 201, TRUE),
(234, 'APT-20260605-004', 234, 4, 5, '2026-06-05', '08:00:00', '08:30:00', 'BOOKED', 'Dau dau', 'Da dung thuoc nhung chua giam', NULL, 201, TRUE),
(235, 'APT-20260605-005', 235, 5, 6, '2026-06-05', '08:00:00', '08:30:00', 'BOOKED', 'Ho sot', 'Mong muon bac si tu van chi tiet', NULL, 201, TRUE),
(236, 'APT-20260605-006', 236, 6, 7, '2026-06-05', '08:00:00', '08:30:00', 'BOOKED', 'Dau bung', 'Trieu chung nhe, can tham kham ban dau', NULL, 201, TRUE),
(237, 'APT-20260605-007', 237, 7, 8, '2026-06-05', '08:00:00', '08:30:00', 'BOOKED', 'Kham da lieu', 'Can kiem tra dinh ky', NULL, 201, TRUE),
(238, 'APT-20260605-008', 238, 8, 9, '2026-06-05', '08:00:00', '08:30:00', 'BOOKED', 'Kham mat', 'Dau hieu xuat hien 2-3 ngay', NULL, 201, TRUE),
(239, 'APT-20260605-009', 239, 9, 10, '2026-06-05', '08:00:00', '08:30:00', 'BOOKED', 'Dau rang', 'Da dung thuoc nhung chua giam', NULL, 201, TRUE),
(240, 'APT-20260605-010', 240, 10, 1, '2026-06-05', '08:00:00', '08:30:00', 'BOOKED', 'Kham nhi', 'Mong muon bac si tu van chi tiet', NULL, 201, TRUE),
(241, 'APT-20260605-011', 241, 1, 2, '2026-06-05', '08:30:00', '09:00:00', 'BOOKED', 'Kham tong quat', 'Trieu chung nhe, can tham kham ban dau', NULL, 201, TRUE),
(242, 'APT-20260605-012', 242, 2, 3, '2026-06-05', '08:30:00', '09:00:00', 'BOOKED', 'Tu van suc khoe', 'Can kiem tra dinh ky', NULL, 201, TRUE),
(243, 'APT-20260605-013', 243, 3, 4, '2026-06-05', '08:30:00', '09:00:00', 'BOOKED', 'Kiem tra huyet ap', 'Dau hieu xuat hien 2-3 ngay', NULL, 201, TRUE),
(244, 'APT-20260605-014', 244, 4, 5, '2026-06-05', '08:30:00', '09:00:00', 'BOOKED', 'Dau dau', 'Da dung thuoc nhung chua giam', NULL, 201, TRUE),
(245, 'APT-20260605-015', 245, 5, 6, '2026-06-05', '08:30:00', '09:00:00', 'BOOKED', 'Ho sot', 'Mong muon bac si tu van chi tiet', NULL, 201, TRUE),
(246, 'APT-20260605-016', 246, 6, 7, '2026-06-05', '08:30:00', '09:00:00', 'BOOKED', 'Dau bung', 'Trieu chung nhe, can tham kham ban dau', NULL, 201, TRUE),
(247, 'APT-20260605-017', 247, 7, 8, '2026-06-05', '08:30:00', '09:00:00', 'BOOKED', 'Kham da lieu', 'Can kiem tra dinh ky', NULL, 201, TRUE),
(248, 'APT-20260605-018', 248, 8, 9, '2026-06-05', '08:30:00', '09:00:00', 'BOOKED', 'Kham mat', 'Dau hieu xuat hien 2-3 ngay', NULL, 201, TRUE),
(249, 'APT-20260605-019', 249, 9, 10, '2026-06-05', '08:30:00', '09:00:00', 'BOOKED', 'Dau rang', 'Da dung thuoc nhung chua giam', NULL, 201, TRUE),
(250, 'APT-20260605-020', 250, 10, 1, '2026-06-05', '08:30:00', '09:00:00', 'BOOKED', 'Kham nhi', 'Mong muon bac si tu van chi tiet', NULL, 201, TRUE);
INSERT INTO support_conversation (id, patient_id, staff_id, status, subject, created_at, updated_at, closed_at, active) VALUES
(1, 1, 8, 'OPEN', 'Tu van ket qua xet nghiem', '2026-05-27 08:30:00', '2026-05-27 09:30:00', NULL, TRUE),
(2, 2, 1, 'ASSIGNED', 'Hoi ve lich kham', '2026-05-27 09:00:00', '2026-05-27 10:00:00', NULL, TRUE),
(3, 3, 8, 'IN_PROGRESS', 'Can ho tro thanh toan', '2026-05-27 10:30:00', '2026-05-27 11:30:00', NULL, TRUE),
(4, 4, 4, 'CLOSED', 'Tu van trieu chung ban dau', '2026-05-27 11:00:00', '2026-05-27 12:00:00', '2026-05-27 13:00:00', TRUE),
(5, 5, 9, 'OPEN', 'Yeu cau doi lich hen', '2026-05-27 12:30:00', '2026-05-27 13:30:00', NULL, TRUE),
(6, 6, 1, 'ASSIGNED', 'Hoi ve don thuoc', '2026-05-28 13:00:00', '2026-05-28 14:00:00', NULL, TRUE),
(7, 7, 8, 'IN_PROGRESS', 'Can gap le tan', '2026-05-28 14:30:00', '2026-05-28 15:30:00', NULL, TRUE),
(8, 8, 1, 'CLOSED', 'Tu van goi kham tong quat', '2026-05-28 15:00:00', '2026-05-28 16:00:00', '2026-05-28 17:00:00', TRUE),
(9, 9, 8, 'OPEN', 'Can gui link meet', '2026-05-28 08:30:00', '2026-05-28 09:30:00', NULL, TRUE),
(10, 10, 4, 'ASSIGNED', 'Hoi ve quy trinh cap phat thuoc', '2026-05-28 09:00:00', '2026-05-28 10:00:00', NULL, TRUE),
(11, 11, 9, 'IN_PROGRESS', 'Tu van ket qua xet nghiem', '2026-05-29 10:30:00', '2026-05-29 11:30:00', NULL, TRUE),
(12, 12, 1, 'CLOSED', 'Hoi ve lich kham', '2026-05-29 11:00:00', '2026-05-29 12:00:00', '2026-05-29 13:00:00', TRUE),
(13, 13, 8, 'OPEN', 'Can ho tro thanh toan', '2026-05-29 12:30:00', '2026-05-29 13:30:00', NULL, TRUE),
(14, 14, 1, 'ASSIGNED', 'Tu van trieu chung ban dau', '2026-05-29 13:00:00', '2026-05-29 14:00:00', NULL, TRUE),
(15, 15, 8, 'IN_PROGRESS', 'Yeu cau doi lich hen', '2026-05-29 14:30:00', '2026-05-29 15:30:00', NULL, TRUE),
(16, 16, 4, 'CLOSED', 'Hoi ve don thuoc', '2026-05-30 15:00:00', '2026-05-30 16:00:00', '2026-05-30 17:00:00', TRUE),
(17, 17, 9, 'OPEN', 'Can gap le tan', '2026-05-30 08:30:00', '2026-05-30 09:30:00', NULL, TRUE),
(18, 18, 1, 'ASSIGNED', 'Tu van goi kham tong quat', '2026-05-30 09:00:00', '2026-05-30 10:00:00', NULL, TRUE),
(19, 19, 8, 'IN_PROGRESS', 'Can gui link meet', '2026-05-30 10:30:00', '2026-05-30 11:30:00', NULL, TRUE),
(20, 20, 1, 'CLOSED', 'Hoi ve quy trinh cap phat thuoc', '2026-05-30 11:00:00', '2026-05-30 12:00:00', '2026-05-30 13:00:00', TRUE),
(21, 21, 8, 'OPEN', 'Tu van ket qua xet nghiem', '2026-05-31 12:30:00', '2026-05-31 13:30:00', NULL, TRUE),
(22, 22, 4, 'ASSIGNED', 'Hoi ve lich kham', '2026-05-31 13:00:00', '2026-05-31 14:00:00', NULL, TRUE),
(23, 23, 9, 'IN_PROGRESS', 'Can ho tro thanh toan', '2026-05-31 14:30:00', '2026-05-31 15:30:00', NULL, TRUE),
(24, 24, 1, 'CLOSED', 'Tu van trieu chung ban dau', '2026-05-31 15:00:00', '2026-05-31 16:00:00', '2026-05-31 17:00:00', TRUE),
(25, 25, 8, 'OPEN', 'Yeu cau doi lich hen', '2026-05-31 08:30:00', '2026-05-31 09:30:00', NULL, TRUE),
(26, 26, 1, 'ASSIGNED', 'Hoi ve don thuoc', '2026-06-01 09:00:00', '2026-06-01 10:00:00', NULL, TRUE),
(27, 27, 8, 'IN_PROGRESS', 'Can gap le tan', '2026-06-01 10:30:00', '2026-06-01 11:30:00', NULL, TRUE),
(28, 28, 4, 'CLOSED', 'Tu van goi kham tong quat', '2026-06-01 11:00:00', '2026-06-01 12:00:00', '2026-06-01 13:00:00', TRUE),
(29, 29, 9, 'OPEN', 'Can gui link meet', '2026-06-01 12:30:00', '2026-06-01 13:30:00', NULL, TRUE),
(30, 30, 1, 'ASSIGNED', 'Hoi ve quy trinh cap phat thuoc', '2026-06-01 13:00:00', '2026-06-01 14:00:00', NULL, TRUE);
INSERT INTO support_message (id, conversation_id, sender_id, content, message_type, created_at, is_read, active) VALUES
(1, 1, 1001, 'Xin chao, toi can ho tro ve: tu van ket qua xet nghiem.', 'TEXT', '2026-05-27 08:30:00', TRUE, TRUE),
(2, 1, 208, 'EverCare da nhan cau hoi, nhan vien dang cho tiep nhan va phan hoi som.', 'TEXT', '2026-05-27 08:35:00', TRUE, TRUE),
(3, 1, 208, 'Da tao lich tu van online cho cuoc tro chuyen #1. Vui long xem link Google Meet trong muc lich tu van.', 'MEET_SCHEDULE', '2026-05-27 08:42:00', TRUE, TRUE),
(4, 2, 1002, 'Xin chao, toi can ho tro ve: hoi ve lich kham.', 'TEXT', '2026-05-27 09:00:00', TRUE, TRUE),
(5, 2, 201, 'Nhan vien phu trach da tiep nhan, anh/chi vui long cung cap them thong tin can ho tro.', 'TEXT', '2026-05-27 09:05:00', TRUE, TRUE),
(6, 2, 201, 'Da tao lich tu van online cho cuoc tro chuyen #2. Vui long xem link Google Meet trong muc lich tu van.', 'MEET_SCHEDULE', '2026-05-27 09:12:00', TRUE, TRUE),
(7, 3, 1003, 'Xin chao, toi can ho tro ve: can ho tro thanh toan.', 'TEXT', '2026-05-27 10:30:00', TRUE, TRUE),
(8, 3, 208, 'Nhan vien dang kiem tra thong tin va se phan hoi ket qua trong it phut nua.', 'TEXT', '2026-05-27 10:35:00', TRUE, TRUE),
(9, 3, 208, 'Da tao lich tu van online cho cuoc tro chuyen #3. Vui long xem link Google Meet trong muc lich tu van.', 'MEET_SCHEDULE', '2026-05-27 10:42:00', TRUE, TRUE),
(10, 4, 1004, 'Xin chao, toi can ho tro ve: tu van trieu chung ban dau.', 'TEXT', '2026-05-27 11:00:00', TRUE, TRUE),
(11, 4, 204, 'Yeu cau da duoc ho tro xong, cam on anh/chi da lien he EverCare.', 'TEXT', '2026-05-27 11:05:00', TRUE, TRUE),
(12, 4, 204, 'Da tao lich tu van online cho cuoc tro chuyen #4. Vui long xem link Google Meet trong muc lich tu van.', 'MEET_SCHEDULE', '2026-05-27 11:12:00', TRUE, TRUE),
(13, 5, 1005, 'Xin chao, toi can ho tro ve: yeu cau doi lich hen.', 'TEXT', '2026-05-27 12:30:00', TRUE, TRUE),
(14, 5, 209, 'EverCare da nhan cau hoi, nhan vien dang cho tiep nhan va phan hoi som.', 'TEXT', '2026-05-27 12:35:00', TRUE, TRUE),
(15, 5, 209, 'Da tao lich tu van online cho cuoc tro chuyen #5. Vui long xem link Google Meet trong muc lich tu van.', 'MEET_SCHEDULE', '2026-05-27 12:42:00', TRUE, TRUE),
(16, 6, 1006, 'Xin chao, toi can ho tro ve: hoi ve don thuoc.', 'TEXT', '2026-05-28 13:00:00', TRUE, TRUE),
(17, 6, 201, 'Nhan vien phu trach da tiep nhan, anh/chi vui long cung cap them thong tin can ho tro.', 'TEXT', '2026-05-28 13:05:00', TRUE, TRUE),
(18, 6, 201, 'Da tao lich tu van online cho cuoc tro chuyen #6. Vui long xem link Google Meet trong muc lich tu van.', 'MEET_SCHEDULE', '2026-05-28 13:12:00', TRUE, TRUE),
(19, 7, 1007, 'Xin chao, toi can ho tro ve: can gap le tan.', 'TEXT', '2026-05-28 14:30:00', TRUE, TRUE),
(20, 7, 208, 'Nhan vien dang kiem tra thong tin va se phan hoi ket qua trong it phut nua.', 'TEXT', '2026-05-28 14:35:00', TRUE, TRUE),
(21, 7, 208, 'Da tao lich tu van online cho cuoc tro chuyen #7. Vui long xem link Google Meet trong muc lich tu van.', 'MEET_SCHEDULE', '2026-05-28 14:42:00', TRUE, TRUE),
(22, 8, 1008, 'Xin chao, toi can ho tro ve: tu van goi kham tong quat.', 'TEXT', '2026-05-28 15:00:00', TRUE, TRUE),
(23, 8, 201, 'Yeu cau da duoc ho tro xong, cam on anh/chi da lien he EverCare.', 'TEXT', '2026-05-28 15:05:00', TRUE, TRUE),
(24, 8, 201, 'Da tao lich tu van online cho cuoc tro chuyen #8. Vui long xem link Google Meet trong muc lich tu van.', 'MEET_SCHEDULE', '2026-05-28 15:12:00', TRUE, TRUE),
(25, 9, 1009, 'Xin chao, toi can ho tro ve: can gui link meet.', 'TEXT', '2026-05-28 08:30:00', TRUE, TRUE),
(26, 9, 208, 'EverCare da nhan cau hoi, nhan vien dang cho tiep nhan va phan hoi som.', 'TEXT', '2026-05-28 08:35:00', TRUE, TRUE),
(27, 9, 208, 'Da tao lich tu van online cho cuoc tro chuyen #9. Vui long xem link Google Meet trong muc lich tu van.', 'MEET_SCHEDULE', '2026-05-28 08:42:00', TRUE, TRUE),
(28, 10, 1010, 'Xin chao, toi can ho tro ve: hoi ve quy trinh cap phat thuoc.', 'TEXT', '2026-05-28 09:00:00', TRUE, TRUE),
(29, 10, 204, 'Nhan vien phu trach da tiep nhan, anh/chi vui long cung cap them thong tin can ho tro.', 'TEXT', '2026-05-28 09:05:00', TRUE, TRUE),
(30, 10, 204, 'Da tao lich tu van online cho cuoc tro chuyen #10. Vui long xem link Google Meet trong muc lich tu van.', 'MEET_SCHEDULE', '2026-05-28 09:12:00', TRUE, TRUE),
(31, 11, 1011, 'Xin chao, toi can ho tro ve: tu van ket qua xet nghiem.', 'TEXT', '2026-05-29 10:30:00', TRUE, TRUE),
(32, 11, 209, 'Nhan vien dang kiem tra thong tin va se phan hoi ket qua trong it phut nua.', 'TEXT', '2026-05-29 10:35:00', TRUE, TRUE),
(33, 11, 1011, 'Toi da nam thong tin, cam on phong kham da ho tro.', 'TEXT', '2026-05-29 10:42:00', TRUE, TRUE),
(34, 12, 1012, 'Xin chao, toi can ho tro ve: hoi ve lich kham.', 'TEXT', '2026-05-29 11:00:00', TRUE, TRUE),
(35, 12, 201, 'Yeu cau da duoc ho tro xong, cam on anh/chi da lien he EverCare.', 'TEXT', '2026-05-29 11:05:00', TRUE, TRUE),
(36, 12, 1012, 'Toi da nam thong tin, cam on phong kham da ho tro.', 'TEXT', '2026-05-29 11:12:00', TRUE, TRUE),
(37, 13, 1013, 'Xin chao, toi can ho tro ve: can ho tro thanh toan.', 'TEXT', '2026-05-29 12:30:00', TRUE, TRUE),
(38, 13, 208, 'EverCare da nhan cau hoi, nhan vien dang cho tiep nhan va phan hoi som.', 'TEXT', '2026-05-29 12:35:00', TRUE, TRUE),
(39, 13, 1013, 'Toi da nam thong tin, cam on phong kham da ho tro.', 'TEXT', '2026-05-29 12:42:00', FALSE, TRUE),
(40, 14, 1014, 'Xin chao, toi can ho tro ve: tu van trieu chung ban dau.', 'TEXT', '2026-05-29 13:00:00', TRUE, TRUE),
(41, 14, 201, 'Nhan vien phu trach da tiep nhan, anh/chi vui long cung cap them thong tin can ho tro.', 'TEXT', '2026-05-29 13:05:00', TRUE, TRUE),
(42, 14, 1014, 'Toi da nam thong tin, cam on phong kham da ho tro.', 'TEXT', '2026-05-29 13:12:00', TRUE, TRUE),
(43, 15, 1015, 'Xin chao, toi can ho tro ve: yeu cau doi lich hen.', 'TEXT', '2026-05-29 14:30:00', TRUE, TRUE),
(44, 15, 208, 'Nhan vien dang kiem tra thong tin va se phan hoi ket qua trong it phut nua.', 'TEXT', '2026-05-29 14:35:00', TRUE, TRUE),
(45, 15, 1015, 'Toi da nam thong tin, cam on phong kham da ho tro.', 'TEXT', '2026-05-29 14:42:00', TRUE, TRUE),
(46, 16, 1016, 'Xin chao, toi can ho tro ve: hoi ve don thuoc.', 'TEXT', '2026-05-30 15:00:00', TRUE, TRUE),
(47, 16, 204, 'Yeu cau da duoc ho tro xong, cam on anh/chi da lien he EverCare.', 'TEXT', '2026-05-30 15:05:00', TRUE, TRUE),
(48, 16, 1016, 'Toi da nam thong tin, cam on phong kham da ho tro.', 'TEXT', '2026-05-30 15:12:00', TRUE, TRUE),
(49, 17, 1017, 'Xin chao, toi can ho tro ve: can gap le tan.', 'TEXT', '2026-05-30 08:30:00', TRUE, TRUE),
(50, 17, 209, 'EverCare da nhan cau hoi, nhan vien dang cho tiep nhan va phan hoi som.', 'TEXT', '2026-05-30 08:35:00', TRUE, TRUE),
(51, 17, 1017, 'Toi da nam thong tin, cam on phong kham da ho tro.', 'TEXT', '2026-05-30 08:42:00', FALSE, TRUE),
(52, 18, 1018, 'Xin chao, toi can ho tro ve: tu van goi kham tong quat.', 'TEXT', '2026-05-30 09:00:00', TRUE, TRUE),
(53, 18, 201, 'Nhan vien phu trach da tiep nhan, anh/chi vui long cung cap them thong tin can ho tro.', 'TEXT', '2026-05-30 09:05:00', TRUE, TRUE),
(54, 18, 1018, 'Toi da nam thong tin, cam on phong kham da ho tro.', 'TEXT', '2026-05-30 09:12:00', TRUE, TRUE),
(55, 19, 1019, 'Xin chao, toi can ho tro ve: can gui link meet.', 'TEXT', '2026-05-30 10:30:00', TRUE, TRUE),
(56, 19, 208, 'Nhan vien dang kiem tra thong tin va se phan hoi ket qua trong it phut nua.', 'TEXT', '2026-05-30 10:35:00', TRUE, TRUE),
(57, 19, 1019, 'Toi da nam thong tin, cam on phong kham da ho tro.', 'TEXT', '2026-05-30 10:42:00', TRUE, TRUE),
(58, 20, 1020, 'Xin chao, toi can ho tro ve: hoi ve quy trinh cap phat thuoc.', 'TEXT', '2026-05-30 11:00:00', TRUE, TRUE),
(59, 20, 201, 'Yeu cau da duoc ho tro xong, cam on anh/chi da lien he EverCare.', 'TEXT', '2026-05-30 11:05:00', TRUE, TRUE),
(60, 20, 1020, 'Toi da nam thong tin, cam on phong kham da ho tro.', 'TEXT', '2026-05-30 11:12:00', TRUE, TRUE),
(61, 21, 1021, 'Xin chao, toi can ho tro ve: tu van ket qua xet nghiem.', 'TEXT', '2026-05-31 12:30:00', TRUE, TRUE),
(62, 21, 208, 'EverCare da nhan cau hoi, nhan vien dang cho tiep nhan va phan hoi som.', 'TEXT', '2026-05-31 12:35:00', TRUE, TRUE),
(63, 21, 1021, 'Toi da nam thong tin, cam on phong kham da ho tro.', 'TEXT', '2026-05-31 12:42:00', FALSE, TRUE),
(64, 22, 1022, 'Xin chao, toi can ho tro ve: hoi ve lich kham.', 'TEXT', '2026-05-31 13:00:00', TRUE, TRUE),
(65, 22, 204, 'Nhan vien phu trach da tiep nhan, anh/chi vui long cung cap them thong tin can ho tro.', 'TEXT', '2026-05-31 13:05:00', TRUE, TRUE),
(66, 22, 1022, 'Toi da nam thong tin, cam on phong kham da ho tro.', 'TEXT', '2026-05-31 13:12:00', TRUE, TRUE),
(67, 23, 1023, 'Xin chao, toi can ho tro ve: can ho tro thanh toan.', 'TEXT', '2026-05-31 14:30:00', TRUE, TRUE),
(68, 23, 209, 'Nhan vien dang kiem tra thong tin va se phan hoi ket qua trong it phut nua.', 'TEXT', '2026-05-31 14:35:00', TRUE, TRUE),
(69, 23, 1023, 'Toi da nam thong tin, cam on phong kham da ho tro.', 'TEXT', '2026-05-31 14:42:00', TRUE, TRUE),
(70, 24, 1024, 'Xin chao, toi can ho tro ve: tu van trieu chung ban dau.', 'TEXT', '2026-05-31 15:00:00', TRUE, TRUE),
(71, 24, 201, 'Yeu cau da duoc ho tro xong, cam on anh/chi da lien he EverCare.', 'TEXT', '2026-05-31 15:05:00', TRUE, TRUE),
(72, 24, 1024, 'Toi da nam thong tin, cam on phong kham da ho tro.', 'TEXT', '2026-05-31 15:12:00', TRUE, TRUE),
(73, 25, 1025, 'Xin chao, toi can ho tro ve: yeu cau doi lich hen.', 'TEXT', '2026-05-31 08:30:00', TRUE, TRUE),
(74, 25, 208, 'EverCare da nhan cau hoi, nhan vien dang cho tiep nhan va phan hoi som.', 'TEXT', '2026-05-31 08:35:00', TRUE, TRUE),
(75, 25, 1025, 'Toi da nam thong tin, cam on phong kham da ho tro.', 'TEXT', '2026-05-31 08:42:00', FALSE, TRUE),
(76, 26, 1026, 'Xin chao, toi can ho tro ve: hoi ve don thuoc.', 'TEXT', '2026-06-01 09:00:00', TRUE, TRUE),
(77, 26, 201, 'Nhan vien phu trach da tiep nhan, anh/chi vui long cung cap them thong tin can ho tro.', 'TEXT', '2026-06-01 09:05:00', TRUE, TRUE),
(78, 26, 1026, 'Toi da nam thong tin, cam on phong kham da ho tro.', 'TEXT', '2026-06-01 09:12:00', TRUE, TRUE),
(79, 27, 1027, 'Xin chao, toi can ho tro ve: can gap le tan.', 'TEXT', '2026-06-01 10:30:00', TRUE, TRUE),
(80, 27, 208, 'Nhan vien dang kiem tra thong tin va se phan hoi ket qua trong it phut nua.', 'TEXT', '2026-06-01 10:35:00', TRUE, TRUE),
(81, 27, 1027, 'Toi da nam thong tin, cam on phong kham da ho tro.', 'TEXT', '2026-06-01 10:42:00', TRUE, TRUE),
(82, 28, 1028, 'Xin chao, toi can ho tro ve: tu van goi kham tong quat.', 'TEXT', '2026-06-01 11:00:00', TRUE, TRUE),
(83, 28, 204, 'Yeu cau da duoc ho tro xong, cam on anh/chi da lien he EverCare.', 'TEXT', '2026-06-01 11:05:00', TRUE, TRUE),
(84, 28, 1028, 'Toi da nam thong tin, cam on phong kham da ho tro.', 'TEXT', '2026-06-01 11:12:00', TRUE, TRUE),
(85, 29, 1029, 'Xin chao, toi can ho tro ve: can gui link meet.', 'TEXT', '2026-06-01 12:30:00', TRUE, TRUE),
(86, 29, 209, 'EverCare da nhan cau hoi, nhan vien dang cho tiep nhan va phan hoi som.', 'TEXT', '2026-06-01 12:35:00', TRUE, TRUE),
(87, 29, 1029, 'Toi da nam thong tin, cam on phong kham da ho tro.', 'TEXT', '2026-06-01 12:42:00', FALSE, TRUE),
(88, 30, 1030, 'Xin chao, toi can ho tro ve: hoi ve quy trinh cap phat thuoc.', 'TEXT', '2026-06-01 13:00:00', TRUE, TRUE),
(89, 30, 201, 'Nhan vien phu trach da tiep nhan, anh/chi vui long cung cap them thong tin can ho tro.', 'TEXT', '2026-06-01 13:05:00', TRUE, TRUE),
(90, 30, 1030, 'Toi da nam thong tin, cam on phong kham da ho tro.', 'TEXT', '2026-06-01 13:12:00', TRUE, TRUE);
INSERT INTO online_consultation_schedule (id, conversation_id, patient_id, doctor_id, staff_id, scheduled_start, scheduled_end, meet_link, status, note, active) VALUES
(1, 1, 1, 1, 1, '2026-05-28 13:00:00', '2026-05-28 13:30:00', 'https://meet.google.com/evercare-tu-van-0001', 'SCHEDULED', 'Le tan hen bac si tu van truc tiep qua Google Meet', TRUE),
(2, 2, 2, 2, 1, '2026-05-28 14:00:00', '2026-05-28 14:30:00', 'https://meet.google.com/evercare-tu-van-0002', 'SCHEDULED', 'Le tan hen bac si tu van truc tiep qua Google Meet', TRUE),
(3, 3, 3, 3, 1, '2026-05-28 15:00:00', '2026-05-28 15:30:00', 'https://meet.google.com/evercare-tu-van-0003', 'SCHEDULED', 'Le tan hen bac si tu van truc tiep qua Google Meet', TRUE),
(4, 4, 4, 4, 1, '2026-05-28 16:00:00', '2026-05-28 16:30:00', 'https://meet.google.com/evercare-tu-van-0004', 'SCHEDULED', 'Le tan hen bac si tu van truc tiep qua Google Meet', TRUE),
(5, 5, 5, 5, 1, '2026-05-28 17:00:00', '2026-05-28 17:30:00', 'https://meet.google.com/evercare-tu-van-0005', 'SCHEDULED', 'Le tan hen bac si tu van truc tiep qua Google Meet', TRUE),
(6, 6, 6, 6, 8, '2026-05-29 13:00:00', '2026-05-29 13:30:00', 'https://meet.google.com/evercare-tu-van-0006', 'SCHEDULED', 'Le tan hen bac si tu van truc tiep qua Google Meet', TRUE),
(7, 7, 7, 7, 8, '2026-05-29 14:00:00', '2026-05-29 14:30:00', 'https://meet.google.com/evercare-tu-van-0007', 'SCHEDULED', 'Le tan hen bac si tu van truc tiep qua Google Meet', TRUE),
(8, 8, 8, 8, 8, '2026-05-29 15:00:00', '2026-05-29 15:30:00', 'https://meet.google.com/evercare-tu-van-0008', 'COMPLETED', 'Le tan hen bac si tu van truc tiep qua Google Meet', TRUE),
(9, 9, 9, 9, 8, '2026-05-29 16:00:00', '2026-05-29 16:30:00', 'https://meet.google.com/evercare-tu-van-0009', 'COMPLETED', 'Le tan hen bac si tu van truc tiep qua Google Meet', TRUE),
(10, 10, 10, 10, 8, '2026-05-29 17:00:00', '2026-05-29 17:30:00', NULL, 'CANCELLED', 'Benh nhan huy lich tu van online', TRUE);
INSERT INTO medical_record (id, record_code, appointment_id, patient_id, doctor_id, visit_date, chief_complaint, diagnosis, treatment_plan, doctor_note, payment_status, active) VALUES
(1, 'MR-2026-0001', 1, 1, 1, '2026-05-27 08:00:00', 'Kham tong quat', 'Chan doan mau 01', 'Ke don va theo doi sau 3-5 ngay', 'Ghi chu bac si mau', 'PAID', TRUE),
(2, 'MR-2026-0002', 2, 2, 2, '2026-05-27 08:00:00', 'Tu van suc khoe', 'Chan doan mau 02', 'Ke don va theo doi sau 3-5 ngay', 'Ghi chu bac si mau', 'PAID', TRUE),
(3, 'MR-2026-0003', 3, 3, 3, '2026-05-27 08:00:00', 'Kiem tra huyet ap', 'Chan doan mau 03', 'Ke don va theo doi sau 3-5 ngay', 'Ghi chu bac si mau', 'PAID', TRUE),
(4, 'MR-2026-0004', 4, 4, 4, '2026-05-27 08:00:00', 'Dau dau', 'Chan doan mau 04', 'Ke don va theo doi sau 3-5 ngay', 'Ghi chu bac si mau', 'PAID', TRUE),
(5, 'MR-2026-0005', 5, 5, 5, '2026-05-27 08:00:00', 'Ho sot', 'Chan doan mau 05', 'Ke don va theo doi sau 3-5 ngay', 'Ghi chu bac si mau', 'PAID', TRUE),
(6, 'MR-2026-0006', 6, 6, 6, '2026-05-27 08:00:00', 'Dau bung', 'Chan doan mau 06', 'Ke don va theo doi sau 3-5 ngay', 'Ghi chu bac si mau', 'PAID', TRUE),
(7, 'MR-2026-0007', 7, 7, 7, '2026-05-27 08:00:00', 'Kham da lieu', 'Chan doan mau 07', 'Ke don va theo doi sau 3-5 ngay', 'Ghi chu bac si mau', 'PAID', TRUE),
(8, 'MR-2026-0008', 8, 8, 8, '2026-05-27 08:00:00', 'Kham mat', 'Chan doan mau 08', 'Ke don va theo doi sau 3-5 ngay', 'Ghi chu bac si mau', 'PAID', TRUE),
(9, 'MR-2026-0009', 9, 9, 9, '2026-05-27 08:00:00', 'Dau rang', 'Chan doan mau 09', 'Ke don va theo doi sau 3-5 ngay', 'Ghi chu bac si mau', 'PAID', TRUE),
(10, 'MR-2026-0010', 10, 10, 10, '2026-05-27 08:00:00', 'Kham nhi', 'Chan doan mau 10', 'Ke don va theo doi sau 3-5 ngay', 'Ghi chu bac si mau', 'PAID', TRUE),
(11, 'MR-2026-0011', 11, 11, 1, '2026-05-27 08:30:00', 'Kham tong quat', 'Chan doan mau 11', 'Ke don va theo doi sau 3-5 ngay', 'Ghi chu bac si mau', 'UNPAID', TRUE),
(12, 'MR-2026-0012', 12, 12, 2, '2026-05-27 08:30:00', 'Tu van suc khoe', 'Chan doan mau 12', 'Ke don va theo doi sau 3-5 ngay', 'Ghi chu bac si mau', 'UNPAID', TRUE),
(13, 'MR-2026-0013', 13, 13, 3, '2026-05-27 08:30:00', 'Kiem tra huyet ap', 'Chan doan mau 13', 'Ke don va theo doi sau 3-5 ngay', 'Ghi chu bac si mau', 'UNPAID', TRUE),
(14, 'MR-2026-0014', 14, 14, 4, '2026-05-27 08:30:00', 'Dau dau', 'Chan doan mau 14', 'Ke don va theo doi sau 3-5 ngay', 'Ghi chu bac si mau', 'UNPAID', TRUE),
(15, 'MR-2026-0015', 15, 15, 5, '2026-05-27 08:30:00', 'Ho sot', 'Chan doan mau 15', 'Ke don va theo doi sau 3-5 ngay', 'Ghi chu bac si mau', 'UNPAID', TRUE),
(16, 'MR-2026-0016', 16, 16, 6, '2026-05-27 08:30:00', 'Dau bung', 'Chan doan mau 16', 'Ke don va theo doi sau 3-5 ngay', 'Ghi chu bac si mau', 'UNPAID', TRUE),
(17, 'MR-2026-0017', 17, 17, 7, '2026-05-27 08:30:00', 'Kham da lieu', 'Chan doan mau 17', 'Ke don va theo doi sau 3-5 ngay', 'Ghi chu bac si mau', 'UNPAID', TRUE),
(18, 'MR-2026-0018', 18, 18, 8, '2026-05-27 08:30:00', 'Kham mat', 'Chan doan mau 18', 'Ke don va theo doi sau 3-5 ngay', 'Ghi chu bac si mau', 'UNPAID', TRUE),
(19, 'MR-2026-0019', 19, 19, 9, '2026-05-27 08:30:00', 'Dau rang', 'Chan doan mau 19', 'Ke don va theo doi sau 3-5 ngay', 'Ghi chu bac si mau', 'UNPAID', TRUE),
(20, 'MR-2026-0020', 20, 20, 10, '2026-05-27 08:30:00', 'Kham nhi', 'Chan doan mau 20', 'Ke don va theo doi sau 3-5 ngay', 'Ghi chu bac si mau', 'UNPAID', TRUE),
(21, 'MR-2026-0021', 21, 21, 1, '2026-05-28 08:00:00', 'Kham tong quat', 'Chan doan mau 21', 'Ke don va theo doi sau 3-5 ngay', 'Ghi chu bac si mau', 'UNPAID', TRUE),
(22, 'MR-2026-0022', 22, 22, 2, '2026-05-28 08:00:00', 'Tu van suc khoe', 'Chan doan mau 22', 'Ke don va theo doi sau 3-5 ngay', 'Ghi chu bac si mau', 'UNPAID', TRUE),
(23, 'MR-2026-0023', 23, 23, 3, '2026-05-28 08:00:00', 'Kiem tra huyet ap', 'Chan doan mau 23', 'Ke don va theo doi sau 3-5 ngay', 'Ghi chu bac si mau', 'UNPAID', TRUE),
(24, 'MR-2026-0024', 24, 24, 4, '2026-05-28 08:00:00', 'Dau dau', 'Chan doan mau 24', 'Ke don va theo doi sau 3-5 ngay', 'Ghi chu bac si mau', 'UNPAID', TRUE),
(25, 'MR-2026-0025', 25, 25, 5, '2026-05-28 08:00:00', 'Ho sot', 'Chan doan mau 25', 'Ke don va theo doi sau 3-5 ngay', 'Ghi chu bac si mau', 'UNPAID', TRUE),
(26, 'MR-2026-0026', 26, 26, 6, '2026-05-28 08:00:00', 'Dau bung', 'Chan doan mau 26', 'Ke don va theo doi sau 3-5 ngay', 'Ghi chu bac si mau', 'UNPAID', TRUE),
(27, 'MR-2026-0027', 27, 27, 7, '2026-05-28 08:00:00', 'Kham da lieu', 'Chan doan mau 27', 'Ke don va theo doi sau 3-5 ngay', 'Ghi chu bac si mau', 'UNPAID', TRUE),
(28, 'MR-2026-0028', 28, 28, 8, '2026-05-28 08:00:00', 'Kham mat', 'Chan doan mau 28', 'Ke don va theo doi sau 3-5 ngay', 'Ghi chu bac si mau', 'UNPAID', TRUE),
(29, 'MR-2026-0029', 29, 29, 9, '2026-05-28 08:00:00', 'Dau rang', 'Chan doan mau 29', 'Ke don va theo doi sau 3-5 ngay', 'Ghi chu bac si mau', 'UNPAID', TRUE),
(30, 'MR-2026-0030', 30, 30, 10, '2026-05-28 08:00:00', 'Kham nhi', 'Chan doan mau 30', 'Ke don va theo doi sau 3-5 ngay', 'Ghi chu bac si mau', 'UNPAID', TRUE);
INSERT INTO medical_record_service (id, medical_record_id, service_id, quantity, unit_price, result_summary, active) VALUES
(1, 1, 2, 1, 190000, 'Da thuc hien dich vu kham', TRUE),
(2, 2, 3, 1, 210000, 'Da thuc hien dich vu kham', TRUE),
(3, 3, 4, 1, 230000, 'Da thuc hien dich vu kham', TRUE),
(4, 4, 5, 1, 150000, 'Da thuc hien dich vu kham', TRUE),
(5, 5, 6, 1, 170000, 'Da thuc hien dich vu kham', TRUE),
(6, 6, 7, 1, 190000, 'Da thuc hien dich vu kham', TRUE),
(7, 7, 8, 1, 210000, 'Da thuc hien dich vu kham', TRUE),
(8, 8, 9, 1, 230000, 'Da thuc hien dich vu kham', TRUE),
(9, 9, 10, 1, 150000, 'Da thuc hien dich vu kham', TRUE),
(10, 10, 1, 1, 170000, 'Da thuc hien dich vu kham', TRUE),
(11, 11, 2, 1, 190000, 'Da thuc hien dich vu kham', TRUE),
(12, 12, 3, 1, 210000, 'Da thuc hien dich vu kham', TRUE),
(13, 13, 4, 1, 230000, 'Da thuc hien dich vu kham', TRUE),
(14, 14, 5, 1, 150000, 'Da thuc hien dich vu kham', TRUE),
(15, 15, 6, 1, 170000, 'Da thuc hien dich vu kham', TRUE),
(16, 16, 7, 1, 190000, 'Da thuc hien dich vu kham', TRUE),
(17, 17, 8, 1, 210000, 'Da thuc hien dich vu kham', TRUE),
(18, 18, 9, 1, 230000, 'Da thuc hien dich vu kham', TRUE),
(19, 19, 10, 1, 150000, 'Da thuc hien dich vu kham', TRUE),
(20, 20, 1, 1, 170000, 'Da thuc hien dich vu kham', TRUE),
(21, 21, 2, 1, 190000, 'Da thuc hien dich vu kham', TRUE),
(22, 22, 3, 1, 210000, 'Da thuc hien dich vu kham', TRUE),
(23, 23, 4, 1, 230000, 'Da thuc hien dich vu kham', TRUE),
(24, 24, 5, 1, 150000, 'Da thuc hien dich vu kham', TRUE),
(25, 25, 6, 1, 170000, 'Da thuc hien dich vu kham', TRUE),
(26, 26, 7, 1, 190000, 'Da thuc hien dich vu kham', TRUE),
(27, 27, 8, 1, 210000, 'Da thuc hien dich vu kham', TRUE),
(28, 28, 9, 1, 230000, 'Da thuc hien dich vu kham', TRUE),
(29, 29, 10, 1, 150000, 'Da thuc hien dich vu kham', TRUE),
(30, 30, 1, 1, 170000, 'Da thuc hien dich vu kham', TRUE);
INSERT INTO test_result (id, medical_record_id, service_id, result_code, result_title, result_content, file_url, conclusion, performed_by, result_date, active) VALUES
(3, 3, 5, 'TR-2026-0003', 'Ket qua xet nghiem mau', 'Chi so trong gioi han tham chieu', '/files/test-results/TR-2026-0003.pdf', 'Binh thuong', 4, '2026-05-27 08:00:00', TRUE),
(6, 6, 5, 'TR-2026-0006', 'Ket qua xet nghiem mau', 'Chi so trong gioi han tham chieu', '/files/test-results/TR-2026-0006.pdf', 'Binh thuong', 4, '2026-05-27 08:00:00', TRUE),
(9, 9, 5, 'TR-2026-0009', 'Ket qua xet nghiem mau', 'Chi so trong gioi han tham chieu', '/files/test-results/TR-2026-0009.pdf', 'Binh thuong', 4, '2026-05-27 08:00:00', TRUE),
(12, 12, 5, 'TR-2026-0012', 'Ket qua xet nghiem mau', 'Chi so trong gioi han tham chieu', '/files/test-results/TR-2026-0012.pdf', 'Binh thuong', 4, '2026-05-27 08:30:00', TRUE),
(15, 15, 5, 'TR-2026-0015', 'Ket qua xet nghiem mau', 'Chi so trong gioi han tham chieu', '/files/test-results/TR-2026-0015.pdf', 'Binh thuong', 4, '2026-05-27 08:30:00', TRUE),
(18, 18, 5, 'TR-2026-0018', 'Ket qua xet nghiem mau', 'Chi so trong gioi han tham chieu', '/files/test-results/TR-2026-0018.pdf', 'Binh thuong', 4, '2026-05-27 08:30:00', TRUE),
(21, 21, 5, 'TR-2026-0021', 'Ket qua xet nghiem mau', 'Chi so trong gioi han tham chieu', '/files/test-results/TR-2026-0021.pdf', 'Binh thuong', 4, '2026-05-28 08:00:00', TRUE),
(24, 24, 5, 'TR-2026-0024', 'Ket qua xet nghiem mau', 'Chi so trong gioi han tham chieu', '/files/test-results/TR-2026-0024.pdf', 'Binh thuong', 4, '2026-05-28 08:00:00', TRUE),
(27, 27, 5, 'TR-2026-0027', 'Ket qua xet nghiem mau', 'Chi so trong gioi han tham chieu', '/files/test-results/TR-2026-0027.pdf', 'Binh thuong', 4, '2026-05-28 08:00:00', TRUE),
(30, 30, 5, 'TR-2026-0030', 'Ket qua xet nghiem mau', 'Chi so trong gioi han tham chieu', '/files/test-results/TR-2026-0030.pdf', 'Binh thuong', 4, '2026-05-28 08:00:00', TRUE);
INSERT INTO prescription (id, prescription_code, medical_record_id, doctor_id, patient_id, prescribed_at, status, note, active) VALUES
(1, 'RX-2026-0001', 1, 1, 1, '2026-05-27 08:00:00', 'PRESCRIBED', 'Don thuoc mau cho benh nhan', TRUE),
(2, 'RX-2026-0002', 2, 2, 2, '2026-05-27 08:00:00', 'PRESCRIBED', 'Don thuoc mau cho benh nhan', TRUE),
(3, 'RX-2026-0003', 3, 3, 3, '2026-05-27 08:00:00', 'PRESCRIBED', 'Don thuoc mau cho benh nhan', TRUE),
(4, 'RX-2026-0004', 4, 4, 4, '2026-05-27 08:00:00', 'PRESCRIBED', 'Don thuoc mau cho benh nhan', TRUE),
(5, 'RX-2026-0005', 5, 5, 5, '2026-05-27 08:00:00', 'PRESCRIBED', 'Don thuoc mau cho benh nhan', TRUE),
(6, 'RX-2026-0006', 6, 6, 6, '2026-05-27 08:00:00', 'PRESCRIBED', 'Don thuoc mau cho benh nhan', TRUE),
(7, 'RX-2026-0007', 7, 7, 7, '2026-05-27 08:00:00', 'PRESCRIBED', 'Don thuoc mau cho benh nhan', TRUE),
(8, 'RX-2026-0008', 8, 8, 8, '2026-05-27 08:00:00', 'PRESCRIBED', 'Don thuoc mau cho benh nhan', TRUE),
(9, 'RX-2026-0009', 9, 9, 9, '2026-05-27 08:00:00', 'PRESCRIBED', 'Don thuoc mau cho benh nhan', TRUE),
(10, 'RX-2026-0010', 10, 10, 10, '2026-05-27 08:00:00', 'PRESCRIBED', 'Don thuoc mau cho benh nhan', TRUE),
(11, 'RX-2026-0011', 11, 1, 11, '2026-05-27 08:30:00', 'DISPENSED', 'Don thuoc mau cho benh nhan', TRUE),
(12, 'RX-2026-0012', 12, 2, 12, '2026-05-27 08:30:00', 'DISPENSED', 'Don thuoc mau cho benh nhan', TRUE),
(13, 'RX-2026-0013', 13, 3, 13, '2026-05-27 08:30:00', 'DISPENSED', 'Don thuoc mau cho benh nhan', TRUE),
(14, 'RX-2026-0014', 14, 4, 14, '2026-05-27 08:30:00', 'DISPENSED', 'Don thuoc mau cho benh nhan', TRUE),
(15, 'RX-2026-0015', 15, 5, 15, '2026-05-27 08:30:00', 'DISPENSED', 'Don thuoc mau cho benh nhan', TRUE),
(16, 'RX-2026-0016', 16, 6, 16, '2026-05-27 08:30:00', 'DISPENSED', 'Don thuoc mau cho benh nhan', TRUE),
(17, 'RX-2026-0017', 17, 7, 17, '2026-05-27 08:30:00', 'DISPENSED', 'Don thuoc mau cho benh nhan', TRUE),
(18, 'RX-2026-0018', 18, 8, 18, '2026-05-27 08:30:00', 'DISPENSED', 'Don thuoc mau cho benh nhan', TRUE),
(19, 'RX-2026-0019', 19, 9, 19, '2026-05-27 08:30:00', 'DISPENSED', 'Don thuoc mau cho benh nhan', TRUE),
(20, 'RX-2026-0020', 20, 10, 20, '2026-05-27 08:30:00', 'DISPENSED', 'Don thuoc mau cho benh nhan', TRUE),
(21, 'RX-2026-0021', 21, 1, 21, '2026-05-28 08:00:00', 'PRESCRIBED', 'Don thuoc mau cho benh nhan', TRUE),
(22, 'RX-2026-0022', 22, 2, 22, '2026-05-28 08:00:00', 'PRESCRIBED', 'Don thuoc mau cho benh nhan', TRUE),
(23, 'RX-2026-0023', 23, 3, 23, '2026-05-28 08:00:00', 'PRESCRIBED', 'Don thuoc mau cho benh nhan', TRUE),
(24, 'RX-2026-0024', 24, 4, 24, '2026-05-28 08:00:00', 'PRESCRIBED', 'Don thuoc mau cho benh nhan', TRUE),
(25, 'RX-2026-0025', 25, 5, 25, '2026-05-28 08:00:00', 'PRESCRIBED', 'Don thuoc mau cho benh nhan', TRUE),
(26, 'RX-2026-0026', 26, 6, 26, '2026-05-28 08:00:00', 'PRESCRIBED', 'Don thuoc mau cho benh nhan', TRUE),
(27, 'RX-2026-0027', 27, 7, 27, '2026-05-28 08:00:00', 'PRESCRIBED', 'Don thuoc mau cho benh nhan', TRUE),
(28, 'RX-2026-0028', 28, 8, 28, '2026-05-28 08:00:00', 'PRESCRIBED', 'Don thuoc mau cho benh nhan', TRUE),
(29, 'RX-2026-0029', 29, 9, 29, '2026-05-28 08:00:00', 'PRESCRIBED', 'Don thuoc mau cho benh nhan', TRUE),
(30, 'RX-2026-0030', 30, 10, 30, '2026-05-28 08:00:00', 'PRESCRIBED', 'Don thuoc mau cho benh nhan', TRUE);
INSERT INTO prescription_item (id, prescription_id, medicine_id, quantity, unit_price, dosage, frequency, duration, instruction, active) VALUES
(1, 1, 1, 10, 1500, '1 vien/lần', '2 lan/ngay', '5 ngay', 'Uong sau an', TRUE),
(2, 1, 2, 5, 3500, '1 vien/lần', '1 lan/ngay', '5 ngay', 'Dung theo huong dan', TRUE),
(3, 2, 2, 10, 3500, '1 vien/lần', '2 lan/ngay', '5 ngay', 'Uong sau an', TRUE),
(4, 2, 3, 5, 1800, '1 vien/lần', '1 lan/ngay', '5 ngay', 'Dung theo huong dan', TRUE),
(5, 3, 3, 10, 1800, '1 vien/lần', '2 lan/ngay', '5 ngay', 'Uong sau an', TRUE),
(6, 3, 4, 5, 2200, '1 vien/lần', '1 lan/ngay', '5 ngay', 'Dung theo huong dan', TRUE),
(7, 4, 4, 10, 2200, '1 vien/lần', '2 lan/ngay', '5 ngay', 'Uong sau an', TRUE),
(8, 4, 5, 5, 2500, '1 vien/lần', '1 lan/ngay', '5 ngay', 'Dung theo huong dan', TRUE),
(9, 5, 5, 10, 2500, '1 vien/lần', '2 lan/ngay', '5 ngay', 'Uong sau an', TRUE),
(10, 5, 6, 5, 1200, '1 vien/lần', '1 lan/ngay', '5 ngay', 'Dung theo huong dan', TRUE),
(11, 6, 6, 10, 1200, '1 vien/lần', '2 lan/ngay', '5 ngay', 'Uong sau an', TRUE),
(12, 6, 7, 5, 2800, '1 vien/lần', '1 lan/ngay', '5 ngay', 'Dung theo huong dan', TRUE),
(13, 7, 7, 10, 2800, '1 vien/lần', '2 lan/ngay', '5 ngay', 'Uong sau an', TRUE),
(14, 7, 8, 5, 45000, '1 vien/lần', '1 lan/ngay', '5 ngay', 'Dung theo huong dan', TRUE),
(15, 8, 8, 10, 45000, '1 vien/lần', '2 lan/ngay', '5 ngay', 'Uong sau an', TRUE),
(16, 8, 9, 5, 9000, '1 vien/lần', '1 lan/ngay', '5 ngay', 'Dung theo huong dan', TRUE),
(17, 9, 9, 10, 9000, '1 vien/lần', '2 lan/ngay', '5 ngay', 'Uong sau an', TRUE),
(18, 9, 10, 5, 28000, '1 vien/lần', '1 lan/ngay', '5 ngay', 'Dung theo huong dan', TRUE),
(19, 10, 10, 10, 28000, '1 vien/lần', '2 lan/ngay', '5 ngay', 'Uong sau an', TRUE),
(20, 10, 1, 5, 1500, '1 vien/lần', '1 lan/ngay', '5 ngay', 'Dung theo huong dan', TRUE),
(21, 11, 1, 10, 1500, '1 vien/lần', '2 lan/ngay', '5 ngay', 'Uong sau an', TRUE),
(22, 11, 2, 5, 3500, '1 vien/lần', '1 lan/ngay', '5 ngay', 'Dung theo huong dan', TRUE),
(23, 12, 2, 10, 3500, '1 vien/lần', '2 lan/ngay', '5 ngay', 'Uong sau an', TRUE),
(24, 12, 3, 5, 1800, '1 vien/lần', '1 lan/ngay', '5 ngay', 'Dung theo huong dan', TRUE),
(25, 13, 3, 10, 1800, '1 vien/lần', '2 lan/ngay', '5 ngay', 'Uong sau an', TRUE),
(26, 13, 4, 5, 2200, '1 vien/lần', '1 lan/ngay', '5 ngay', 'Dung theo huong dan', TRUE),
(27, 14, 4, 10, 2200, '1 vien/lần', '2 lan/ngay', '5 ngay', 'Uong sau an', TRUE),
(28, 14, 5, 5, 2500, '1 vien/lần', '1 lan/ngay', '5 ngay', 'Dung theo huong dan', TRUE),
(29, 15, 5, 10, 2500, '1 vien/lần', '2 lan/ngay', '5 ngay', 'Uong sau an', TRUE),
(30, 15, 6, 5, 1200, '1 vien/lần', '1 lan/ngay', '5 ngay', 'Dung theo huong dan', TRUE),
(31, 16, 6, 10, 1200, '1 vien/lần', '2 lan/ngay', '5 ngay', 'Uong sau an', TRUE),
(32, 16, 7, 5, 2800, '1 vien/lần', '1 lan/ngay', '5 ngay', 'Dung theo huong dan', TRUE),
(33, 17, 7, 10, 2800, '1 vien/lần', '2 lan/ngay', '5 ngay', 'Uong sau an', TRUE),
(34, 17, 8, 5, 45000, '1 vien/lần', '1 lan/ngay', '5 ngay', 'Dung theo huong dan', TRUE),
(35, 18, 8, 10, 45000, '1 vien/lần', '2 lan/ngay', '5 ngay', 'Uong sau an', TRUE),
(36, 18, 9, 5, 9000, '1 vien/lần', '1 lan/ngay', '5 ngay', 'Dung theo huong dan', TRUE),
(37, 19, 9, 10, 9000, '1 vien/lần', '2 lan/ngay', '5 ngay', 'Uong sau an', TRUE),
(38, 19, 10, 5, 28000, '1 vien/lần', '1 lan/ngay', '5 ngay', 'Dung theo huong dan', TRUE),
(39, 20, 10, 10, 28000, '1 vien/lần', '2 lan/ngay', '5 ngay', 'Uong sau an', TRUE),
(40, 20, 1, 5, 1500, '1 vien/lần', '1 lan/ngay', '5 ngay', 'Dung theo huong dan', TRUE),
(41, 21, 1, 10, 1500, '1 vien/lần', '2 lan/ngay', '5 ngay', 'Uong sau an', TRUE),
(42, 21, 2, 5, 3500, '1 vien/lần', '1 lan/ngay', '5 ngay', 'Dung theo huong dan', TRUE),
(43, 22, 2, 10, 3500, '1 vien/lần', '2 lan/ngay', '5 ngay', 'Uong sau an', TRUE),
(44, 22, 3, 5, 1800, '1 vien/lần', '1 lan/ngay', '5 ngay', 'Dung theo huong dan', TRUE),
(45, 23, 3, 10, 1800, '1 vien/lần', '2 lan/ngay', '5 ngay', 'Uong sau an', TRUE),
(46, 23, 4, 5, 2200, '1 vien/lần', '1 lan/ngay', '5 ngay', 'Dung theo huong dan', TRUE),
(47, 24, 4, 10, 2200, '1 vien/lần', '2 lan/ngay', '5 ngay', 'Uong sau an', TRUE),
(48, 24, 5, 5, 2500, '1 vien/lần', '1 lan/ngay', '5 ngay', 'Dung theo huong dan', TRUE),
(49, 25, 5, 10, 2500, '1 vien/lần', '2 lan/ngay', '5 ngay', 'Uong sau an', TRUE),
(50, 25, 6, 5, 1200, '1 vien/lần', '1 lan/ngay', '5 ngay', 'Dung theo huong dan', TRUE),
(51, 26, 6, 10, 1200, '1 vien/lần', '2 lan/ngay', '5 ngay', 'Uong sau an', TRUE),
(52, 26, 7, 5, 2800, '1 vien/lần', '1 lan/ngay', '5 ngay', 'Dung theo huong dan', TRUE),
(53, 27, 7, 10, 2800, '1 vien/lần', '2 lan/ngay', '5 ngay', 'Uong sau an', TRUE),
(54, 27, 8, 5, 45000, '1 vien/lần', '1 lan/ngay', '5 ngay', 'Dung theo huong dan', TRUE),
(55, 28, 8, 10, 45000, '1 vien/lần', '2 lan/ngay', '5 ngay', 'Uong sau an', TRUE),
(56, 28, 9, 5, 9000, '1 vien/lần', '1 lan/ngay', '5 ngay', 'Dung theo huong dan', TRUE),
(57, 29, 9, 10, 9000, '1 vien/lần', '2 lan/ngay', '5 ngay', 'Uong sau an', TRUE),
(58, 29, 10, 5, 28000, '1 vien/lần', '1 lan/ngay', '5 ngay', 'Dung theo huong dan', TRUE),
(59, 30, 10, 10, 28000, '1 vien/lần', '2 lan/ngay', '5 ngay', 'Uong sau an', TRUE),
(60, 30, 1, 5, 1500, '1 vien/lần', '1 lan/ngay', '5 ngay', 'Dung theo huong dan', TRUE);
INSERT INTO inventory_transaction (id, medicine_id, batch_id, prescription_item_id, transaction_type, quantity, transaction_date, note, created_by) VALUES
(1, 1, 1, 21, 'PRESCRIPTION_EXPORT', -10, '2026-05-30 17:01:00', 'Da cap phat theo don thuoc', 207),
(2, 2, 2, 22, 'PRESCRIPTION_EXPORT', -5, '2026-05-30 17:02:00', 'Da cap phat theo don thuoc', 207),
(3, 2, 2, 23, 'PRESCRIPTION_EXPORT', -10, '2026-05-30 17:03:00', 'Da cap phat theo don thuoc', 207),
(4, 3, 3, 24, 'PRESCRIPTION_EXPORT', -5, '2026-05-30 17:04:00', 'Da cap phat theo don thuoc', 207),
(5, 3, 3, 25, 'PRESCRIPTION_EXPORT', -10, '2026-05-30 17:05:00', 'Da cap phat theo don thuoc', 207),
(6, 4, 4, 26, 'PRESCRIPTION_EXPORT', -5, '2026-05-30 17:06:00', 'Da cap phat theo don thuoc', 207),
(7, 4, 4, 27, 'PRESCRIPTION_EXPORT', -10, '2026-05-30 17:07:00', 'Da cap phat theo don thuoc', 207),
(8, 5, 5, 28, 'PRESCRIPTION_EXPORT', -5, '2026-05-30 17:08:00', 'Da cap phat theo don thuoc', 207),
(9, 5, 5, 29, 'PRESCRIPTION_EXPORT', -10, '2026-05-30 17:09:00', 'Da cap phat theo don thuoc', 207),
(10, 6, 6, 30, 'PRESCRIPTION_EXPORT', -5, '2026-05-30 17:10:00', 'Da cap phat theo don thuoc', 207),
(11, 6, 6, 31, 'PRESCRIPTION_EXPORT', -10, '2026-05-30 17:11:00', 'Da cap phat theo don thuoc', 207),
(12, 7, 7, 32, 'PRESCRIPTION_EXPORT', -5, '2026-05-30 17:12:00', 'Da cap phat theo don thuoc', 207),
(13, 7, 7, 33, 'PRESCRIPTION_EXPORT', -10, '2026-05-30 17:13:00', 'Da cap phat theo don thuoc', 207),
(14, 8, 8, 34, 'PRESCRIPTION_EXPORT', -5, '2026-05-30 17:14:00', 'Da cap phat theo don thuoc', 207),
(15, 8, 8, 35, 'PRESCRIPTION_EXPORT', -10, '2026-05-30 17:15:00', 'Da cap phat theo don thuoc', 207),
(16, 9, 9, 36, 'PRESCRIPTION_EXPORT', -5, '2026-05-30 17:16:00', 'Da cap phat theo don thuoc', 207),
(17, 9, 9, 37, 'PRESCRIPTION_EXPORT', -10, '2026-05-30 17:17:00', 'Da cap phat theo don thuoc', 207),
(18, 10, 10, 38, 'PRESCRIPTION_EXPORT', -5, '2026-05-30 17:18:00', 'Da cap phat theo don thuoc', 207),
(19, 10, 10, 39, 'PRESCRIPTION_EXPORT', -10, '2026-05-30 17:19:00', 'Da cap phat theo don thuoc', 207),
(20, 1, 1, 40, 'PRESCRIPTION_EXPORT', -5, '2026-05-30 17:20:00', 'Da cap phat theo don thuoc', 207);
INSERT INTO invoice (id, invoice_code, medical_record_id, patient_id, cashier_id, total_service_amount, total_medicine_amount, discount_amount, total_amount, payment_method, payment_status, paid_at, note, active) VALUES
(1, 'INV-2026-0001', 1, 1, 202, 190000, 32500, 0, 222500, 'CASH', 'PAID', '2026-05-27 16:00:00', 'DA THANH TOAN - CHUA CAP PHAT THUOC', TRUE),
(2, 'INV-2026-0002', 2, 2, 202, 210000, 44000, 0, 254000, 'CASH', 'PAID', '2026-05-27 16:00:00', 'DA THANH TOAN - CHUA CAP PHAT THUOC', TRUE),
(3, 'INV-2026-0003', 3, 3, 202, 230000, 29000, 0, 259000, 'CASH', 'PAID', '2026-05-27 16:00:00', 'DA THANH TOAN - CHUA CAP PHAT THUOC', TRUE),
(4, 'INV-2026-0004', 4, 4, 202, 150000, 34500, 0, 184500, 'CASH', 'PAID', '2026-05-27 16:00:00', 'DA THANH TOAN - CHUA CAP PHAT THUOC', TRUE),
(5, 'INV-2026-0005', 5, 5, 202, 170000, 31000, 0, 201000, 'CASH', 'PAID', '2026-05-27 16:00:00', 'DA THANH TOAN - CHUA CAP PHAT THUOC', TRUE),
(6, 'INV-2026-0006', 6, 6, 202, 190000, 26000, 0, 216000, 'CASH', 'PAID', '2026-05-27 16:00:00', 'DA THANH TOAN - CHUA CAP PHAT THUOC', TRUE),
(7, 'INV-2026-0007', 7, 7, 202, 210000, 253000, 0, 463000, 'CASH', 'PAID', '2026-05-27 16:00:00', 'DA THANH TOAN - CHUA CAP PHAT THUOC', TRUE),
(8, 'INV-2026-0008', 8, 8, 202, 230000, 495000, 0, 725000, 'CASH', 'PAID', '2026-05-27 16:00:00', 'DA THANH TOAN - CHUA CAP PHAT THUOC', TRUE),
(9, 'INV-2026-0009', 9, 9, 202, 150000, 230000, 0, 380000, 'CASH', 'PAID', '2026-05-27 16:00:00', 'DA THANH TOAN - CHUA CAP PHAT THUOC', TRUE),
(10, 'INV-2026-0010', 10, 10, 202, 170000, 287500, 0, 457500, 'CASH', 'PAID', '2026-05-27 16:00:00', 'DA THANH TOAN - CHUA CAP PHAT THUOC', TRUE),
(11, 'INV-2026-0011', 11, 11, 202, 190000, 32500, 0, 222500, NULL, 'UNPAID', NULL, 'Chua thanh toan', TRUE),
(12, 'INV-2026-0012', 12, 12, 202, 210000, 44000, 0, 254000, NULL, 'UNPAID', NULL, 'Chua thanh toan', TRUE),
(13, 'INV-2026-0013', 13, 13, 202, 230000, 29000, 0, 259000, NULL, 'UNPAID', NULL, 'Chua thanh toan', TRUE),
(14, 'INV-2026-0014', 14, 14, 202, 150000, 34500, 0, 184500, NULL, 'UNPAID', NULL, 'Chua thanh toan', TRUE),
(15, 'INV-2026-0015', 15, 15, 202, 170000, 31000, 0, 201000, NULL, 'UNPAID', NULL, 'Chua thanh toan', TRUE),
(16, 'INV-2026-0016', 16, 16, 202, 190000, 26000, 0, 216000, NULL, 'UNPAID', NULL, 'Chua thanh toan', TRUE),
(17, 'INV-2026-0017', 17, 17, 202, 210000, 253000, 0, 463000, NULL, 'UNPAID', NULL, 'Chua thanh toan', TRUE),
(18, 'INV-2026-0018', 18, 18, 202, 230000, 495000, 0, 725000, NULL, 'UNPAID', NULL, 'Chua thanh toan', TRUE),
(19, 'INV-2026-0019', 19, 19, 202, 150000, 230000, 0, 380000, NULL, 'UNPAID', NULL, 'Chua thanh toan', TRUE),
(20, 'INV-2026-0020', 20, 20, 202, 170000, 287500, 0, 457500, NULL, 'UNPAID', NULL, 'Chua thanh toan', TRUE),
(21, 'INV-2026-0021', 21, 21, 206, 190000, 32500, 0, 222500, NULL, 'UNPAID', NULL, 'Chua thanh toan', TRUE),
(22, 'INV-2026-0022', 22, 22, 206, 210000, 44000, 0, 254000, NULL, 'UNPAID', NULL, 'Chua thanh toan', TRUE),
(23, 'INV-2026-0023', 23, 23, 206, 230000, 29000, 0, 259000, NULL, 'UNPAID', NULL, 'Chua thanh toan', TRUE),
(24, 'INV-2026-0024', 24, 24, 206, 150000, 34500, 0, 184500, NULL, 'UNPAID', NULL, 'Chua thanh toan', TRUE),
(25, 'INV-2026-0025', 25, 25, 206, 170000, 31000, 0, 201000, NULL, 'UNPAID', NULL, 'Chua thanh toan', TRUE),
(26, 'INV-2026-0026', 26, 26, 206, 190000, 26000, 0, 216000, NULL, 'UNPAID', NULL, 'Chua thanh toan', TRUE),
(27, 'INV-2026-0027', 27, 27, 206, 210000, 253000, 0, 463000, NULL, 'UNPAID', NULL, 'Chua thanh toan', TRUE),
(28, 'INV-2026-0028', 28, 28, 206, 230000, 495000, 0, 725000, NULL, 'UNPAID', NULL, 'Chua thanh toan', TRUE),
(29, 'INV-2026-0029', 29, 29, 206, 150000, 230000, 0, 380000, NULL, 'UNPAID', NULL, 'Chua thanh toan', TRUE),
(30, 'INV-2026-0030', 30, 30, 206, 170000, 287500, 0, 457500, NULL, 'UNPAID', NULL, 'Chua thanh toan', TRUE);
INSERT INTO payment (id, invoice_id, amount, payment_method, transaction_code, payment_provider, payment_status, paid_at, active) VALUES
(1, 1, 222500, 'CASH', 'PAY-2026-0001', 'COUNTER', 'SUCCESS', '2026-05-27 16:00:00', TRUE),
(2, 2, 254000, 'CASH', 'PAY-2026-0002', 'COUNTER', 'SUCCESS', '2026-05-27 16:00:00', TRUE),
(3, 3, 259000, 'CASH', 'PAY-2026-0003', 'COUNTER', 'SUCCESS', '2026-05-27 16:00:00', TRUE),
(4, 4, 184500, 'CASH', 'PAY-2026-0004', 'COUNTER', 'SUCCESS', '2026-05-27 16:00:00', TRUE),
(5, 5, 201000, 'CASH', 'PAY-2026-0005', 'COUNTER', 'SUCCESS', '2026-05-27 16:00:00', TRUE),
(6, 6, 216000, 'CASH', 'PAY-2026-0006', 'COUNTER', 'SUCCESS', '2026-05-27 16:00:00', TRUE),
(7, 7, 463000, 'CASH', 'PAY-2026-0007', 'COUNTER', 'SUCCESS', '2026-05-27 16:00:00', TRUE),
(8, 8, 725000, 'CASH', 'PAY-2026-0008', 'COUNTER', 'SUCCESS', '2026-05-27 16:00:00', TRUE),
(9, 9, 380000, 'CASH', 'PAY-2026-0009', 'COUNTER', 'SUCCESS', '2026-05-27 16:00:00', TRUE),
(10, 10, 457500, 'CASH', 'PAY-2026-0010', 'COUNTER', 'SUCCESS', '2026-05-27 16:00:00', TRUE);
INSERT INTO notification (id, user_id, title, content, notification_type, related_id, read_at, active) VALUES
(1, 1001, 'Nhac lich hen', 'Ban co lich hen APT-20260527-001 vao ngay 2026-05-27 luc 08:00:00', 'APPOINTMENT_REMINDER', 1, NULL, TRUE),
(2, 1002, 'Nhac lich hen', 'Ban co lich hen APT-20260527-002 vao ngay 2026-05-27 luc 08:00:00', 'APPOINTMENT_REMINDER', 2, NULL, TRUE),
(3, 1003, 'Nhac lich hen', 'Ban co lich hen APT-20260527-003 vao ngay 2026-05-27 luc 08:00:00', 'APPOINTMENT_REMINDER', 3, NULL, TRUE),
(4, 1004, 'Nhac lich hen', 'Ban co lich hen APT-20260527-004 vao ngay 2026-05-27 luc 08:00:00', 'APPOINTMENT_REMINDER', 4, NULL, TRUE),
(5, 1005, 'Nhac lich hen', 'Ban co lich hen APT-20260527-005 vao ngay 2026-05-27 luc 08:00:00', 'APPOINTMENT_REMINDER', 5, NULL, TRUE),
(6, 1006, 'Nhac lich hen', 'Ban co lich hen APT-20260527-006 vao ngay 2026-05-27 luc 08:00:00', 'APPOINTMENT_REMINDER', 6, NULL, TRUE),
(7, 1007, 'Nhac lich hen', 'Ban co lich hen APT-20260527-007 vao ngay 2026-05-27 luc 08:00:00', 'APPOINTMENT_REMINDER', 7, NULL, TRUE),
(8, 1008, 'Nhac lich hen', 'Ban co lich hen APT-20260527-008 vao ngay 2026-05-27 luc 08:00:00', 'APPOINTMENT_REMINDER', 8, NULL, TRUE),
(9, 1009, 'Nhac lich hen', 'Ban co lich hen APT-20260527-009 vao ngay 2026-05-27 luc 08:00:00', 'APPOINTMENT_REMINDER', 9, NULL, TRUE),
(10, 1010, 'Nhac lich hen', 'Ban co lich hen APT-20260527-010 vao ngay 2026-05-27 luc 08:00:00', 'APPOINTMENT_REMINDER', 10, NULL, TRUE),
(11, 1011, 'Nhac lich hen', 'Ban co lich hen APT-20260527-011 vao ngay 2026-05-27 luc 08:30:00', 'APPOINTMENT_REMINDER', 11, NULL, TRUE),
(12, 1012, 'Nhac lich hen', 'Ban co lich hen APT-20260527-012 vao ngay 2026-05-27 luc 08:30:00', 'APPOINTMENT_REMINDER', 12, NULL, TRUE),
(13, 1013, 'Nhac lich hen', 'Ban co lich hen APT-20260527-013 vao ngay 2026-05-27 luc 08:30:00', 'APPOINTMENT_REMINDER', 13, NULL, TRUE),
(14, 1014, 'Nhac lich hen', 'Ban co lich hen APT-20260527-014 vao ngay 2026-05-27 luc 08:30:00', 'APPOINTMENT_REMINDER', 14, NULL, TRUE),
(15, 1015, 'Nhac lich hen', 'Ban co lich hen APT-20260527-015 vao ngay 2026-05-27 luc 08:30:00', 'APPOINTMENT_REMINDER', 15, NULL, TRUE),
(16, 1016, 'Nhac lich hen', 'Ban co lich hen APT-20260527-016 vao ngay 2026-05-27 luc 08:30:00', 'APPOINTMENT_REMINDER', 16, NULL, TRUE),
(17, 1017, 'Nhac lich hen', 'Ban co lich hen APT-20260527-017 vao ngay 2026-05-27 luc 08:30:00', 'APPOINTMENT_REMINDER', 17, NULL, TRUE),
(18, 1018, 'Nhac lich hen', 'Ban co lich hen APT-20260527-018 vao ngay 2026-05-27 luc 08:30:00', 'APPOINTMENT_REMINDER', 18, NULL, TRUE),
(19, 1019, 'Nhac lich hen', 'Ban co lich hen APT-20260527-019 vao ngay 2026-05-27 luc 08:30:00', 'APPOINTMENT_REMINDER', 19, NULL, TRUE),
(20, 1020, 'Nhac lich hen', 'Ban co lich hen APT-20260527-020 vao ngay 2026-05-27 luc 08:30:00', 'APPOINTMENT_REMINDER', 20, NULL, TRUE),
(21, 1021, 'Nhac lich hen', 'Ban co lich hen APT-20260528-001 vao ngay 2026-05-28 luc 08:00:00', 'APPOINTMENT_REMINDER', 21, NULL, TRUE),
(22, 1022, 'Nhac lich hen', 'Ban co lich hen APT-20260528-002 vao ngay 2026-05-28 luc 08:00:00', 'APPOINTMENT_REMINDER', 22, NULL, TRUE),
(23, 1023, 'Nhac lich hen', 'Ban co lich hen APT-20260528-003 vao ngay 2026-05-28 luc 08:00:00', 'APPOINTMENT_REMINDER', 23, NULL, TRUE),
(24, 1024, 'Nhac lich hen', 'Ban co lich hen APT-20260528-004 vao ngay 2026-05-28 luc 08:00:00', 'APPOINTMENT_REMINDER', 24, NULL, TRUE),
(25, 1025, 'Nhac lich hen', 'Ban co lich hen APT-20260528-005 vao ngay 2026-05-28 luc 08:00:00', 'APPOINTMENT_REMINDER', 25, NULL, TRUE),
(26, 1026, 'Nhac lich hen', 'Ban co lich hen APT-20260528-006 vao ngay 2026-05-28 luc 08:00:00', 'APPOINTMENT_REMINDER', 26, NULL, TRUE),
(27, 1027, 'Nhac lich hen', 'Ban co lich hen APT-20260528-007 vao ngay 2026-05-28 luc 08:00:00', 'APPOINTMENT_REMINDER', 27, NULL, TRUE),
(28, 1028, 'Nhac lich hen', 'Ban co lich hen APT-20260528-008 vao ngay 2026-05-28 luc 08:00:00', 'APPOINTMENT_REMINDER', 28, NULL, TRUE),
(29, 1029, 'Nhac lich hen', 'Ban co lich hen APT-20260528-009 vao ngay 2026-05-28 luc 08:00:00', 'APPOINTMENT_REMINDER', 29, NULL, TRUE),
(30, 1030, 'Nhac lich hen', 'Ban co lich hen APT-20260528-010 vao ngay 2026-05-28 luc 08:00:00', 'APPOINTMENT_REMINDER', 30, NULL, TRUE),
(31, 1031, 'Nhac lich hen', 'Ban co lich hen APT-20260528-011 vao ngay 2026-05-28 luc 08:30:00', 'APPOINTMENT_REMINDER', 31, NULL, TRUE),
(32, 1032, 'Nhac lich hen', 'Ban co lich hen APT-20260528-012 vao ngay 2026-05-28 luc 08:30:00', 'APPOINTMENT_REMINDER', 32, NULL, TRUE),
(33, 1033, 'Nhac lich hen', 'Ban co lich hen APT-20260528-013 vao ngay 2026-05-28 luc 08:30:00', 'APPOINTMENT_REMINDER', 33, NULL, TRUE),
(34, 1034, 'Nhac lich hen', 'Ban co lich hen APT-20260528-014 vao ngay 2026-05-28 luc 08:30:00', 'APPOINTMENT_REMINDER', 34, NULL, TRUE),
(35, 1035, 'Nhac lich hen', 'Ban co lich hen APT-20260528-015 vao ngay 2026-05-28 luc 08:30:00', 'APPOINTMENT_REMINDER', 35, NULL, TRUE),
(36, 1036, 'Nhac lich hen', 'Ban co lich hen APT-20260528-016 vao ngay 2026-05-28 luc 08:30:00', 'APPOINTMENT_REMINDER', 36, NULL, TRUE),
(37, 1037, 'Nhac lich hen', 'Ban co lich hen APT-20260528-017 vao ngay 2026-05-28 luc 08:30:00', 'APPOINTMENT_REMINDER', 37, NULL, TRUE),
(38, 1038, 'Nhac lich hen', 'Ban co lich hen APT-20260528-018 vao ngay 2026-05-28 luc 08:30:00', 'APPOINTMENT_REMINDER', 38, NULL, TRUE),
(39, 1039, 'Nhac lich hen', 'Ban co lich hen APT-20260528-019 vao ngay 2026-05-28 luc 08:30:00', 'APPOINTMENT_REMINDER', 39, NULL, TRUE),
(40, 1040, 'Nhac lich hen', 'Ban co lich hen APT-20260528-020 vao ngay 2026-05-28 luc 08:30:00', 'APPOINTMENT_REMINDER', 40, NULL, TRUE),
(41, 1041, 'Nhac lich hen', 'Ban co lich hen APT-20260528-021 vao ngay 2026-05-28 luc 09:00:00', 'APPOINTMENT_REMINDER', 41, NULL, TRUE),
(42, 1042, 'Nhac lich hen', 'Ban co lich hen APT-20260528-022 vao ngay 2026-05-28 luc 09:00:00', 'APPOINTMENT_REMINDER', 42, NULL, TRUE),
(43, 1043, 'Nhac lich hen', 'Ban co lich hen APT-20260528-023 vao ngay 2026-05-28 luc 09:00:00', 'APPOINTMENT_REMINDER', 43, NULL, TRUE),
(44, 1044, 'Nhac lich hen', 'Ban co lich hen APT-20260528-024 vao ngay 2026-05-28 luc 09:00:00', 'APPOINTMENT_REMINDER', 44, NULL, TRUE),
(45, 1045, 'Nhac lich hen', 'Ban co lich hen APT-20260528-025 vao ngay 2026-05-28 luc 09:00:00', 'APPOINTMENT_REMINDER', 45, NULL, TRUE),
(46, 1046, 'Nhac lich hen', 'Ban co lich hen APT-20260528-026 vao ngay 2026-05-28 luc 09:00:00', 'APPOINTMENT_REMINDER', 46, NULL, TRUE),
(47, 1047, 'Nhac lich hen', 'Ban co lich hen APT-20260528-027 vao ngay 2026-05-28 luc 09:00:00', 'APPOINTMENT_REMINDER', 47, NULL, TRUE),
(48, 1048, 'Nhac lich hen', 'Ban co lich hen APT-20260528-028 vao ngay 2026-05-28 luc 09:00:00', 'APPOINTMENT_REMINDER', 48, NULL, TRUE),
(49, 1049, 'Nhac lich hen', 'Ban co lich hen APT-20260528-029 vao ngay 2026-05-28 luc 09:00:00', 'APPOINTMENT_REMINDER', 49, NULL, TRUE),
(50, 1050, 'Nhac lich hen', 'Ban co lich hen APT-20260528-030 vao ngay 2026-05-28 luc 09:00:00', 'APPOINTMENT_REMINDER', 50, NULL, TRUE),
(51, 1051, 'Nhac lich hen', 'Ban co lich hen APT-20260528-031 vao ngay 2026-05-28 luc 09:30:00', 'APPOINTMENT_REMINDER', 51, NULL, TRUE),
(52, 1052, 'Nhac lich hen', 'Ban co lich hen APT-20260528-032 vao ngay 2026-05-28 luc 09:30:00', 'APPOINTMENT_REMINDER', 52, NULL, TRUE),
(53, 1053, 'Nhac lich hen', 'Ban co lich hen APT-20260528-033 vao ngay 2026-05-28 luc 09:30:00', 'APPOINTMENT_REMINDER', 53, NULL, TRUE),
(54, 1054, 'Nhac lich hen', 'Ban co lich hen APT-20260528-034 vao ngay 2026-05-28 luc 09:30:00', 'APPOINTMENT_REMINDER', 54, NULL, TRUE),
(55, 1055, 'Nhac lich hen', 'Ban co lich hen APT-20260528-035 vao ngay 2026-05-28 luc 09:30:00', 'APPOINTMENT_REMINDER', 55, NULL, TRUE),
(56, 1056, 'Nhac lich hen', 'Ban co lich hen APT-20260528-036 vao ngay 2026-05-28 luc 09:30:00', 'APPOINTMENT_REMINDER', 56, NULL, TRUE),
(57, 1057, 'Nhac lich hen', 'Ban co lich hen APT-20260528-037 vao ngay 2026-05-28 luc 09:30:00', 'APPOINTMENT_REMINDER', 57, NULL, TRUE),
(58, 1058, 'Nhac lich hen', 'Ban co lich hen APT-20260528-038 vao ngay 2026-05-28 luc 09:30:00', 'APPOINTMENT_REMINDER', 58, NULL, TRUE),
(59, 1059, 'Nhac lich hen', 'Ban co lich hen APT-20260528-039 vao ngay 2026-05-28 luc 09:30:00', 'APPOINTMENT_REMINDER', 59, NULL, TRUE),
(60, 1060, 'Nhac lich hen', 'Ban co lich hen APT-20260528-040 vao ngay 2026-05-28 luc 09:30:00', 'APPOINTMENT_REMINDER', 60, NULL, TRUE),
(61, 1061, 'Nhac lich hen', 'Ban co lich hen APT-20260528-041 vao ngay 2026-05-28 luc 10:00:00', 'APPOINTMENT_REMINDER', 61, NULL, TRUE),
(62, 1062, 'Nhac lich hen', 'Ban co lich hen APT-20260528-042 vao ngay 2026-05-28 luc 10:00:00', 'APPOINTMENT_REMINDER', 62, NULL, TRUE),
(63, 1063, 'Nhac lich hen', 'Ban co lich hen APT-20260528-043 vao ngay 2026-05-28 luc 10:00:00', 'APPOINTMENT_REMINDER', 63, NULL, TRUE),
(64, 1064, 'Nhac lich hen', 'Ban co lich hen APT-20260528-044 vao ngay 2026-05-28 luc 10:00:00', 'APPOINTMENT_REMINDER', 64, NULL, TRUE),
(65, 1065, 'Nhac lich hen', 'Ban co lich hen APT-20260528-045 vao ngay 2026-05-28 luc 10:00:00', 'APPOINTMENT_REMINDER', 65, NULL, TRUE),
(66, 1066, 'Nhac lich hen', 'Ban co lich hen APT-20260528-046 vao ngay 2026-05-28 luc 10:00:00', 'APPOINTMENT_REMINDER', 66, NULL, TRUE),
(67, 1067, 'Nhac lich hen', 'Ban co lich hen APT-20260528-047 vao ngay 2026-05-28 luc 10:00:00', 'APPOINTMENT_REMINDER', 67, NULL, TRUE),
(68, 1068, 'Nhac lich hen', 'Ban co lich hen APT-20260528-048 vao ngay 2026-05-28 luc 10:00:00', 'APPOINTMENT_REMINDER', 68, NULL, TRUE),
(69, 1069, 'Nhac lich hen', 'Ban co lich hen APT-20260528-049 vao ngay 2026-05-28 luc 10:00:00', 'APPOINTMENT_REMINDER', 69, NULL, TRUE),
(70, 1070, 'Nhac lich hen', 'Ban co lich hen APT-20260528-050 vao ngay 2026-05-28 luc 10:00:00', 'APPOINTMENT_REMINDER', 70, NULL, TRUE),
(71, 1071, 'Nhac lich hen', 'Ban co lich hen APT-20260528-051 vao ngay 2026-05-28 luc 10:30:00', 'APPOINTMENT_REMINDER', 71, NULL, TRUE),
(72, 1072, 'Nhac lich hen', 'Ban co lich hen APT-20260528-052 vao ngay 2026-05-28 luc 10:30:00', 'APPOINTMENT_REMINDER', 72, NULL, TRUE),
(73, 1073, 'Nhac lich hen', 'Ban co lich hen APT-20260528-053 vao ngay 2026-05-28 luc 10:30:00', 'APPOINTMENT_REMINDER', 73, NULL, TRUE),
(74, 1074, 'Nhac lich hen', 'Ban co lich hen APT-20260528-054 vao ngay 2026-05-28 luc 10:30:00', 'APPOINTMENT_REMINDER', 74, NULL, TRUE),
(75, 1075, 'Nhac lich hen', 'Ban co lich hen APT-20260528-055 vao ngay 2026-05-28 luc 10:30:00', 'APPOINTMENT_REMINDER', 75, NULL, TRUE),
(76, 1076, 'Nhac lich hen', 'Ban co lich hen APT-20260528-056 vao ngay 2026-05-28 luc 10:30:00', 'APPOINTMENT_REMINDER', 76, NULL, TRUE),
(77, 1077, 'Nhac lich hen', 'Ban co lich hen APT-20260528-057 vao ngay 2026-05-28 luc 10:30:00', 'APPOINTMENT_REMINDER', 77, NULL, TRUE),
(78, 1078, 'Nhac lich hen', 'Ban co lich hen APT-20260528-058 vao ngay 2026-05-28 luc 10:30:00', 'APPOINTMENT_REMINDER', 78, NULL, TRUE),
(79, 1079, 'Nhac lich hen', 'Ban co lich hen APT-20260528-059 vao ngay 2026-05-28 luc 10:30:00', 'APPOINTMENT_REMINDER', 79, NULL, TRUE),
(80, 1080, 'Nhac lich hen', 'Ban co lich hen APT-20260528-060 vao ngay 2026-05-28 luc 10:30:00', 'APPOINTMENT_REMINDER', 80, NULL, TRUE),
(81, 1081, 'Nhac lich hen', 'Ban co lich hen APT-20260528-061 vao ngay 2026-05-28 luc 11:00:00', 'APPOINTMENT_REMINDER', 81, NULL, TRUE),
(82, 1082, 'Nhac lich hen', 'Ban co lich hen APT-20260528-062 vao ngay 2026-05-28 luc 11:00:00', 'APPOINTMENT_REMINDER', 82, NULL, TRUE),
(83, 1083, 'Nhac lich hen', 'Ban co lich hen APT-20260528-063 vao ngay 2026-05-28 luc 11:00:00', 'APPOINTMENT_REMINDER', 83, NULL, TRUE),
(84, 1084, 'Nhac lich hen', 'Ban co lich hen APT-20260528-064 vao ngay 2026-05-28 luc 11:00:00', 'APPOINTMENT_REMINDER', 84, NULL, TRUE),
(85, 1085, 'Nhac lich hen', 'Ban co lich hen APT-20260528-065 vao ngay 2026-05-28 luc 11:00:00', 'APPOINTMENT_REMINDER', 85, NULL, TRUE),
(86, 1086, 'Nhac lich hen', 'Ban co lich hen APT-20260528-066 vao ngay 2026-05-28 luc 11:00:00', 'APPOINTMENT_REMINDER', 86, NULL, TRUE),
(87, 1087, 'Nhac lich hen', 'Ban co lich hen APT-20260528-067 vao ngay 2026-05-28 luc 11:00:00', 'APPOINTMENT_REMINDER', 87, NULL, TRUE),
(88, 1088, 'Nhac lich hen', 'Ban co lich hen APT-20260528-068 vao ngay 2026-05-28 luc 11:00:00', 'APPOINTMENT_REMINDER', 88, NULL, TRUE),
(89, 1089, 'Nhac lich hen', 'Ban co lich hen APT-20260528-069 vao ngay 2026-05-28 luc 11:00:00', 'APPOINTMENT_REMINDER', 89, NULL, TRUE),
(90, 1090, 'Nhac lich hen', 'Ban co lich hen APT-20260528-070 vao ngay 2026-05-28 luc 11:00:00', 'APPOINTMENT_REMINDER', 90, NULL, TRUE),
(91, 1091, 'Nhac lich hen', 'Ban co lich hen APT-20260529-001 vao ngay 2026-05-29 luc 08:00:00', 'APPOINTMENT_REMINDER', 91, NULL, TRUE),
(92, 1092, 'Nhac lich hen', 'Ban co lich hen APT-20260529-002 vao ngay 2026-05-29 luc 08:00:00', 'APPOINTMENT_REMINDER', 92, NULL, TRUE),
(93, 1093, 'Nhac lich hen', 'Ban co lich hen APT-20260529-003 vao ngay 2026-05-29 luc 08:00:00', 'APPOINTMENT_REMINDER', 93, NULL, TRUE),
(94, 1094, 'Nhac lich hen', 'Ban co lich hen APT-20260529-004 vao ngay 2026-05-29 luc 08:00:00', 'APPOINTMENT_REMINDER', 94, NULL, TRUE),
(95, 1095, 'Nhac lich hen', 'Ban co lich hen APT-20260529-005 vao ngay 2026-05-29 luc 08:00:00', 'APPOINTMENT_REMINDER', 95, NULL, TRUE),
(96, 1096, 'Nhac lich hen', 'Ban co lich hen APT-20260529-006 vao ngay 2026-05-29 luc 08:00:00', 'APPOINTMENT_REMINDER', 96, NULL, TRUE),
(97, 1097, 'Nhac lich hen', 'Ban co lich hen APT-20260529-007 vao ngay 2026-05-29 luc 08:00:00', 'APPOINTMENT_REMINDER', 97, NULL, TRUE),
(98, 1098, 'Nhac lich hen', 'Ban co lich hen APT-20260529-008 vao ngay 2026-05-29 luc 08:00:00', 'APPOINTMENT_REMINDER', 98, NULL, TRUE),
(99, 1099, 'Nhac lich hen', 'Ban co lich hen APT-20260529-009 vao ngay 2026-05-29 luc 08:00:00', 'APPOINTMENT_REMINDER', 99, NULL, TRUE),
(100, 1100, 'Nhac lich hen', 'Ban co lich hen APT-20260529-010 vao ngay 2026-05-29 luc 08:00:00', 'APPOINTMENT_REMINDER', 100, NULL, TRUE),
(101, 1101, 'Nhac lich hen', 'Ban co lich hen APT-20260529-011 vao ngay 2026-05-29 luc 08:30:00', 'APPOINTMENT_REMINDER', 101, NULL, TRUE),
(102, 1102, 'Nhac lich hen', 'Ban co lich hen APT-20260529-012 vao ngay 2026-05-29 luc 08:30:00', 'APPOINTMENT_REMINDER', 102, NULL, TRUE),
(103, 1103, 'Nhac lich hen', 'Ban co lich hen APT-20260529-013 vao ngay 2026-05-29 luc 08:30:00', 'APPOINTMENT_REMINDER', 103, NULL, TRUE),
(104, 1104, 'Nhac lich hen', 'Ban co lich hen APT-20260529-014 vao ngay 2026-05-29 luc 08:30:00', 'APPOINTMENT_REMINDER', 104, NULL, TRUE),
(105, 1105, 'Nhac lich hen', 'Ban co lich hen APT-20260529-015 vao ngay 2026-05-29 luc 08:30:00', 'APPOINTMENT_REMINDER', 105, NULL, TRUE),
(106, 1106, 'Nhac lich hen', 'Ban co lich hen APT-20260529-016 vao ngay 2026-05-29 luc 08:30:00', 'APPOINTMENT_REMINDER', 106, NULL, TRUE),
(107, 1107, 'Nhac lich hen', 'Ban co lich hen APT-20260529-017 vao ngay 2026-05-29 luc 08:30:00', 'APPOINTMENT_REMINDER', 107, NULL, TRUE),
(108, 1108, 'Nhac lich hen', 'Ban co lich hen APT-20260529-018 vao ngay 2026-05-29 luc 08:30:00', 'APPOINTMENT_REMINDER', 108, NULL, TRUE),
(109, 1109, 'Nhac lich hen', 'Ban co lich hen APT-20260529-019 vao ngay 2026-05-29 luc 08:30:00', 'APPOINTMENT_REMINDER', 109, NULL, TRUE),
(110, 1110, 'Nhac lich hen', 'Ban co lich hen APT-20260529-020 vao ngay 2026-05-29 luc 08:30:00', 'APPOINTMENT_REMINDER', 110, NULL, TRUE),
(111, 1111, 'Nhac lich hen', 'Ban co lich hen APT-20260530-001 vao ngay 2026-05-30 luc 08:00:00', 'APPOINTMENT_REMINDER', 111, NULL, TRUE),
(112, 1112, 'Nhac lich hen', 'Ban co lich hen APT-20260530-002 vao ngay 2026-05-30 luc 08:00:00', 'APPOINTMENT_REMINDER', 112, NULL, TRUE),
(113, 1113, 'Nhac lich hen', 'Ban co lich hen APT-20260530-003 vao ngay 2026-05-30 luc 08:00:00', 'APPOINTMENT_REMINDER', 113, NULL, TRUE),
(114, 1114, 'Nhac lich hen', 'Ban co lich hen APT-20260530-004 vao ngay 2026-05-30 luc 08:00:00', 'APPOINTMENT_REMINDER', 114, NULL, TRUE),
(115, 1115, 'Nhac lich hen', 'Ban co lich hen APT-20260530-005 vao ngay 2026-05-30 luc 08:00:00', 'APPOINTMENT_REMINDER', 115, NULL, TRUE),
(116, 1116, 'Nhac lich hen', 'Ban co lich hen APT-20260530-006 vao ngay 2026-05-30 luc 08:00:00', 'APPOINTMENT_REMINDER', 116, NULL, TRUE),
(117, 1117, 'Nhac lich hen', 'Ban co lich hen APT-20260530-007 vao ngay 2026-05-30 luc 08:00:00', 'APPOINTMENT_REMINDER', 117, NULL, TRUE),
(118, 1118, 'Nhac lich hen', 'Ban co lich hen APT-20260530-008 vao ngay 2026-05-30 luc 08:00:00', 'APPOINTMENT_REMINDER', 118, NULL, TRUE),
(119, 1119, 'Nhac lich hen', 'Ban co lich hen APT-20260530-009 vao ngay 2026-05-30 luc 08:00:00', 'APPOINTMENT_REMINDER', 119, NULL, TRUE),
(120, 1120, 'Nhac lich hen', 'Ban co lich hen APT-20260530-010 vao ngay 2026-05-30 luc 08:00:00', 'APPOINTMENT_REMINDER', 120, NULL, TRUE),
(121, 203, 'Don thuoc cho cap phat', 'Hoa don INV-2026-0001 da thanh toan, don thuoc RX-2026-0001 chua cap phat', 'PRESCRIPTION', 1, NULL, TRUE),
(122, 203, 'Don thuoc cho cap phat', 'Hoa don INV-2026-0002 da thanh toan, don thuoc RX-2026-0002 chua cap phat', 'PRESCRIPTION', 2, NULL, TRUE),
(123, 203, 'Don thuoc cho cap phat', 'Hoa don INV-2026-0003 da thanh toan, don thuoc RX-2026-0003 chua cap phat', 'PRESCRIPTION', 3, NULL, TRUE),
(124, 203, 'Don thuoc cho cap phat', 'Hoa don INV-2026-0004 da thanh toan, don thuoc RX-2026-0004 chua cap phat', 'PRESCRIPTION', 4, NULL, TRUE),
(125, 203, 'Don thuoc cho cap phat', 'Hoa don INV-2026-0005 da thanh toan, don thuoc RX-2026-0005 chua cap phat', 'PRESCRIPTION', 5, NULL, TRUE),
(126, 203, 'Don thuoc cho cap phat', 'Hoa don INV-2026-0006 da thanh toan, don thuoc RX-2026-0006 chua cap phat', 'PRESCRIPTION', 6, NULL, TRUE),
(127, 203, 'Don thuoc cho cap phat', 'Hoa don INV-2026-0007 da thanh toan, don thuoc RX-2026-0007 chua cap phat', 'PRESCRIPTION', 7, NULL, TRUE),
(128, 203, 'Don thuoc cho cap phat', 'Hoa don INV-2026-0008 da thanh toan, don thuoc RX-2026-0008 chua cap phat', 'PRESCRIPTION', 8, NULL, TRUE),
(129, 203, 'Don thuoc cho cap phat', 'Hoa don INV-2026-0009 da thanh toan, don thuoc RX-2026-0009 chua cap phat', 'PRESCRIPTION', 9, NULL, TRUE),
(130, 203, 'Don thuoc cho cap phat', 'Hoa don INV-2026-0010 da thanh toan, don thuoc RX-2026-0010 chua cap phat', 'PRESCRIPTION', 10, NULL, TRUE);

-- 11. EDGE CASE DATA FOR TESTING
-- Tat ca dong duoi day hop le ve rang buoc DB, dung de test cac nhanh nghiep vu kho.

-- 11.1. Thuoc va lo thuoc: het ton, chi co lo het han, ton vua du, FIFO nhieu lo.
INSERT INTO medicine (id, medicine_code, name, unit, description, usage_note, unit_price, min_stock_quantity, active) VALUES
(9001, 'MED-EDGE-NOSTOCK', 'Thuoc edge khong co ton', 'VIEN', 'Dung test cap phat khi khong co ton kha dung.', 'Dung theo don bac si.', 1000, 10, TRUE),
(9002, 'MED-EDGE-EXPIRED', 'Thuoc edge chi co lo het han', 'VIEN', 'Dung test bo qua lo het han khi tinh ton.', 'Dung theo don bac si.', 2000, 10, TRUE),
(9003, 'MED-EDGE-EXACT', 'Thuoc edge ton vua du', 'VIEN', 'Dung test cap phat dung bang so ton kha dung.', 'Dung theo don bac si.', 3000, 5, TRUE),
(9004, 'MED-EDGE-FIFO', 'Thuoc edge FIFO nhieu lo', 'VIEN', 'Dung test cap phat theo han dung gan nhat truoc.', 'Dung theo don bac si.', 4000, 20, TRUE);

INSERT INTO medicine_batch (id, medicine_id, batch_code, import_date, expiry_date, quantity, remaining_quantity, import_price, supplier_name, active) VALUES
(9011, 9001, 'EDGE-NOSTOCK-ZERO', '2026-01-01', '2027-01-01', 10, 0, 700, 'Edge Pharma', TRUE),
(9012, 9002, 'EDGE-EXPIRED-ONLY', '2025-01-01', '2026-02-01', 30, 30, 1400, 'Edge Pharma', TRUE),
(9013, 9003, 'EDGE-EXACT-A', '2026-01-01', '2027-06-01', 5, 5, 2100, 'Edge Pharma', TRUE),
(9014, 9003, 'EDGE-EXACT-B', '2026-01-01', '2027-06-02', 7, 7, 2100, 'Edge Pharma', TRUE),
(9015, 9004, 'EDGE-FIFO-OLD', '2026-01-01', '2026-06-15', 20, 8, 2800, 'Edge Pharma', TRUE),
(9016, 9004, 'EDGE-FIFO-NEW', '2026-01-01', '2027-12-31', 50, 50, 2800, 'Edge Pharma', TRUE),
(9017, 9004, 'EDGE-FIFO-ZERO', '2026-01-01', '2027-01-01', 5, 0, 2800, 'Edge Pharma', TRUE),
(9018, 9004, 'EDGE-FIFO-EXPIRED', '2025-01-01', '2026-03-01', 10, 10, 2800, 'Edge Pharma', TRUE);

-- 11.2. Lich hen edge case cho module bac si.
INSERT INTO appointment (id, appointment_code, patient_id, doctor_id, service_id, appointment_date, start_time, end_time, status, reason, symptom_note, cancel_reason, created_by, active) VALUES
(9001, 'APT-EDGE-BOOKED-001', 201, 1, 1, '2026-06-06', '08:00:00', '08:30:00', 'BOOKED', 'Test khong duoc bat dau kham neu chua check-in.', 'Edge appointment: booked only.', NULL, 201, TRUE),
(9002, 'APT-EDGE-WAITING-001', 202, 1, 1, '2026-06-06', '08:30:00', '09:00:00', 'WAITING', 'Test duoc bat dau kham sau check-in.', 'Edge appointment: waiting queue.', NULL, 201, TRUE),
(9003, 'APT-EDGE-INPROGRESS-001', 203, 1, 1, '2026-06-06', '09:00:00', '09:30:00', 'IN_PROGRESS', 'Test workspace dang kham.', 'Edge appointment: in progress.', NULL, 201, TRUE),
(9004, 'APT-EDGE-CANCELLED-001', 204, 1, 1, '2026-06-06', '09:30:00', '10:00:00', 'CANCELLED', 'Test lich da huy.', 'Edge appointment: cancelled.', 'Benh nhan yeu cau huy.', 201, TRUE),
(9005, 'APT-EDGE-NOSHOW-001', 205, 1, 1, '2026-06-06', '10:00:00', '10:30:00', 'NO_SHOW', 'Test benh nhan khong den.', 'Edge appointment: no show.', NULL, 201, TRUE),
(9006, 'APT-EDGE-RX-EXACT-001', 206, 2, 1, '2026-06-06', '08:00:00', '08:30:00', 'COMPLETED', 'Test don thuoc ton vua du.', 'Edge prescription exact stock.', NULL, 201, TRUE),
(9007, 'APT-EDGE-RX-UNPAID-001', 207, 2, 1, '2026-06-06', '08:30:00', '09:00:00', 'COMPLETED', 'Test don thuoc chua thanh toan.', 'Edge prescription unpaid.', NULL, 201, TRUE),
(9008, 'APT-EDGE-RX-NOSTOCK-001', 208, 2, 1, '2026-06-06', '09:00:00', '09:30:00', 'COMPLETED', 'Test don thuoc het ton.', 'Edge prescription no stock.', NULL, 201, TRUE),
(9009, 'APT-EDGE-RX-EXPIRED-001', 209, 2, 1, '2026-06-06', '09:30:00', '10:00:00', 'COMPLETED', 'Test don thuoc chi co lo het han.', 'Edge prescription expired stock only.', NULL, 201, TRUE),
(9010, 'APT-EDGE-LAB-MISSING-001', 210, 3, 1, '2026-06-06', '08:00:00', '08:30:00', 'IN_PROGRESS', 'Test khong hoan tat khi chi dinh chua co ket qua.', 'Edge medical record missing lab result.', NULL, 201, TRUE),
(9011, 'APT-EDGE-RX-FIFO-001', 211, 3, 1, '2026-06-06', '08:30:00', '09:00:00', 'COMPLETED', 'Test cap phat FIFO nhieu lo.', 'Edge prescription FIFO.', NULL, 201, TRUE),
(9012, 'APT-EDGE-RX-DUPMED-001', 212, 3, 1, '2026-06-06', '09:00:00', '09:30:00', 'COMPLETED', 'Test don co 2 dong cung mot thuoc.', 'Edge prescription duplicate medicine lines.', NULL, 201, TRUE),
(9013, 'APT-EDGE-RX-CANCELLED-001', 213, 3, 1, '2026-06-06', '09:30:00', '10:00:00', 'COMPLETED', 'Test don da huy khong duoc cap phat.', 'Edge prescription cancelled.', NULL, 201, TRUE),
(9014, 'APT-EDGE-RX-DISPENSED-001', 214, 3, 1, '2026-06-06', '10:00:00', '10:30:00', 'COMPLETED', 'Test don da cap phat khong cap lai.', 'Edge prescription already dispensed.', NULL, 201, TRUE),
(9015, 'APT-EDGE-REFUND-001', 215, 4, 1, '2026-06-06', '08:00:00', '08:30:00', 'COMPLETED', 'Test hoa don hoan tien.', 'Edge refunded invoice.', NULL, 201, TRUE);

-- 11.3. Ho so va chi dinh can chan hoan tat khi thieu ket qua active.
INSERT INTO medical_record (id, record_code, appointment_id, patient_id, doctor_id, visit_date, chief_complaint, diagnosis, treatment_plan, doctor_note, payment_status, active) VALUES
(9001, 'MR-EDGE-INPROGRESS-001', 9003, 203, 1, '2026-06-06 09:05:00', 'Dang kham edge case.', NULL, NULL, 'Ho so dang kham, chua co chan doan.', 'UNPAID', TRUE),
(9002, 'MR-EDGE-LAB-MISSING-001', 9010, 210, 3, '2026-06-06 08:05:00', 'Can xet nghiem truoc khi ket luan.', 'Theo doi bat thuong cong thuc mau.', 'Cho ket qua xet nghiem va chan doan hinh anh.', 'Co chi dinh active nhung chua co ket qua active.', 'UNPAID', TRUE),
(9003, 'MR-EDGE-RX-EXACT-001', 9006, 206, 2, '2026-06-06 08:05:00', 'Dau dau nhe.', 'Cam lanh.', 'Uong thuoc va theo doi.', 'Ho so da thanh toan, ton vua du.', 'PAID', TRUE),
(9004, 'MR-EDGE-RX-UNPAID-001', 9007, 207, 2, '2026-06-06 08:35:00', 'Ho va sot.', 'Viem hong nhe.', 'Uong thuoc theo don.', 'Ho so chua thanh toan.', 'UNPAID', TRUE),
(9005, 'MR-EDGE-RX-NOSTOCK-001', 9008, 208, 2, '2026-06-06 09:05:00', 'Dau bung.', 'Roi loan tieu hoa.', 'Uong thuoc theo don.', 'Thuoc trong don khong co ton kha dung.', 'PAID', TRUE),
(9006, 'MR-EDGE-RX-EXPIRED-001', 9009, 209, 2, '2026-06-06 09:35:00', 'Di ung nhe.', 'Di ung thoi tiet.', 'Uong thuoc theo don.', 'Thuoc trong don chi co lo het han.', 'PAID', TRUE),
(9007, 'MR-EDGE-RX-FIFO-001', 9011, 211, 3, '2026-06-06 08:35:00', 'Dau co vai.', 'Cang co.', 'Uong thuoc theo don.', 'Don can lay tu nhieu lo theo FIFO.', 'PAID', TRUE),
(9008, 'MR-EDGE-RX-DUPMED-001', 9012, 212, 3, '2026-06-06 09:05:00', 'Dau lung.', 'Co that co lung.', 'Uong thuoc theo don.', 'Don co 2 dong cung mot thuoc de test gom so luong.', 'PAID', TRUE),
(9009, 'MR-EDGE-RX-CANCELLED-001', 9013, 213, 3, '2026-06-06 09:35:00', 'Can huy don.', 'Khong cap phat.', 'Khong dung thuoc.', 'Don thuoc da huy.', 'PAID', TRUE),
(9010, 'MR-EDGE-RX-DISPENSED-001', 9014, 214, 3, '2026-06-06 10:05:00', 'Da cap thuoc.', 'Cam lanh.', 'Da cap phat thuoc.', 'Don da cap phat truoc do.', 'PAID', TRUE),
(9011, 'MR-EDGE-REFUND-001', 9015, 215, 4, '2026-06-06 08:05:00', 'Hoan tien dich vu.', 'Khong thuc hien dich vu.', 'Hoan tien.', 'Ho so co hoa don refunded.', 'REFUNDED', TRUE);

INSERT INTO medical_record_service (id, medical_record_id, service_id, quantity, unit_price, result_summary, active) VALUES
(9001, 9002, 4, 1, 120000, NULL, TRUE),
(9002, 9002, 5, 1, 250000, NULL, TRUE),
(9003, 9001, 4, 1, 120000, NULL, FALSE),
(9004, 9011, 4, 1, 120000, 'Ket qua binh thuong nhung ho so da refund.', TRUE);

INSERT INTO test_result (id, medical_record_id, service_id, result_code, result_title, result_content, file_url, conclusion, performed_by, result_date, active) VALUES
(9001, 9002, 4, 'TR-EDGE-INACTIVE-001', 'Ket qua inactive', 'Ket qua bi huy, khong duoc tinh la hop le.', NULL, 'Inactive result.', 4, '2026-06-06 08:45:00', FALSE),
(9002, 9011, 4, 'TR-EDGE-REFUND-001', 'Ket qua ho so refund', 'Chi so trong gioi han binh thuong.', NULL, 'Binh thuong.', 4, '2026-06-06 08:20:00', TRUE);

-- 11.4. Don thuoc: chua thanh toan, thieu ton, lo het han, FIFO, trung thuoc, da huy, da cap.
INSERT INTO prescription (id, prescription_code, medical_record_id, doctor_id, patient_id, prescribed_at, status, note, active) VALUES
(9001, 'RX-EDGE-EXACT-001', 9003, 2, 206, '2026-06-06 08:25:00', 'PRESCRIBED', 'Ton kha dung vua du 12 vien.', TRUE),
(9002, 'RX-EDGE-UNPAID-001', 9004, 2, 207, '2026-06-06 08:55:00', 'PRESCRIBED', 'Ho so chua thanh toan, khong duoc cap phat.', TRUE),
(9003, 'RX-EDGE-NOSTOCK-001', 9005, 2, 208, '2026-06-06 09:25:00', 'PRESCRIBED', 'Thuoc khong co ton kha dung.', TRUE),
(9004, 'RX-EDGE-EXPIRED-001', 9006, 2, 209, '2026-06-06 09:55:00', 'PRESCRIBED', 'Thuoc chi co lo het han.', TRUE),
(9005, 'RX-EDGE-FIFO-001', 9007, 3, 211, '2026-06-06 08:55:00', 'PRESCRIBED', 'Can cap 12 vien tu lo cu truoc, lo moi sau.', TRUE),
(9006, 'RX-EDGE-DUPMED-001', 9008, 3, 212, '2026-06-06 09:25:00', 'PRESCRIBED', 'Hai dong cung thuoc, can gom tong 11 vien khi validate ton.', TRUE),
(9007, 'RX-EDGE-CANCELLED-001', 9009, 3, 213, '2026-06-06 09:55:00', 'CANCELLED', 'Don da huy.', TRUE),
(9008, 'RX-EDGE-DISPENSED-001', 9010, 3, 214, '2026-06-06 10:25:00', 'DISPENSED', 'Don da cap phat, khong duoc cap lai.', TRUE);

INSERT INTO prescription_item (id, prescription_id, medicine_id, quantity, unit_price, dosage, frequency, duration, instruction, active) VALUES
(9001, 9001, 9003, 12, 3000, '1 vien', '2 lan/ngay', '6 ngay', 'Uong sau an.', TRUE),
(9002, 9002, 9004, 5, 4000, '1 vien', '1 lan/ngay', '5 ngay', 'Uong sau an.', TRUE),
(9003, 9003, 9001, 1, 1000, '1 vien', '1 lan/ngay', '1 ngay', 'Test het ton.', TRUE),
(9004, 9004, 9002, 5, 2000, '1 vien', '1 lan/ngay', '5 ngay', 'Test lo het han.', TRUE),
(9005, 9005, 9004, 12, 4000, '1 vien', '2 lan/ngay', '6 ngay', 'Test FIFO.', TRUE),
(9006, 9006, 9004, 5, 4000, '1 vien', '1 lan/ngay', '5 ngay', 'Dong thu nhat cung thuoc.', TRUE),
(9007, 9006, 9004, 6, 4000, '1 vien', '1 lan/ngay', '6 ngay', 'Dong thu hai cung thuoc.', TRUE),
(9008, 9007, 9004, 4, 4000, '1 vien', '1 lan/ngay', '4 ngay', 'Don da huy.', TRUE),
(9009, 9008, 9004, 3, 4000, '1 vien', '1 lan/ngay', '3 ngay', 'Don da cap.', TRUE);

INSERT INTO inventory_transaction (id, medicine_id, batch_id, prescription_item_id, transaction_type, quantity, transaction_date, note, created_by) VALUES
(9001, 9004, 9015, 9009, 'PRESCRIPTION_EXPORT', -3, '2026-06-06 10:40:00', 'Edge baseline: don da cap phat truoc do.', 207);

-- 11.5. Hoa don va thanh toan: paid, unpaid, failed payment, refunded.
INSERT INTO invoice (id, invoice_code, medical_record_id, patient_id, cashier_id, total_service_amount, total_medicine_amount, discount_amount, total_amount, payment_method, payment_status, paid_at, note, active) VALUES
(9001, 'INV-EDGE-EXACT-001', 9003, 206, 202, 150000, 36000, 0, 186000, 'CASH', 'PAID', '2026-06-06 08:35:00', 'Hoa don da thanh toan, ton vua du.', TRUE),
(9002, 'INV-EDGE-UNPAID-001', 9004, 207, 202, 150000, 20000, 0, 170000, NULL, 'UNPAID', NULL, 'Hoa don chua thanh toan.', TRUE),
(9003, 'INV-EDGE-NOSTOCK-001', 9005, 208, 202, 150000, 1000, 0, 151000, 'BANK_TRANSFER', 'PAID', '2026-06-06 09:35:00', 'Da thanh toan nhung thuoc het ton.', TRUE),
(9004, 'INV-EDGE-EXPIRED-001', 9006, 209, 202, 150000, 10000, 0, 160000, 'VIETQR', 'PAID', '2026-06-06 10:05:00', 'Da thanh toan nhung chi co lo het han.', TRUE),
(9005, 'INV-EDGE-FIFO-001', 9007, 211, 202, 150000, 48000, 0, 198000, 'CASH', 'PAID', '2026-06-06 09:05:00', 'Hoa don test FIFO.', TRUE),
(9006, 'INV-EDGE-DUPMED-001', 9008, 212, 202, 150000, 44000, 0, 194000, 'CASH', 'PAID', '2026-06-06 09:35:00', 'Hoa don test gom so luong trung thuoc.', TRUE),
(9007, 'INV-EDGE-CANCELLED-001', 9009, 213, 202, 150000, 16000, 0, 166000, 'CASH', 'PAID', '2026-06-06 10:05:00', 'Hoa don da thanh toan nhung don thuoc da huy.', TRUE),
(9008, 'INV-EDGE-DISPENSED-001', 9010, 214, 202, 150000, 12000, 0, 162000, 'CASH', 'PAID', '2026-06-06 10:35:00', 'Hoa don cua don da cap.', TRUE),
(9009, 'INV-EDGE-REFUND-001', 9011, 215, 202, 150000, 0, 0, 150000, 'CASH', 'REFUNDED', '2026-06-06 08:35:00', 'Hoa don da hoan tien.', TRUE);

INSERT INTO payment (id, invoice_id, amount, payment_method, transaction_code, payment_provider, payment_status, paid_at, active) VALUES
(9001, 9001, 186000, 'CASH', 'PAY-EDGE-EXACT-001', 'COUNTER', 'SUCCESS', '2026-06-06 08:35:00', TRUE),
(9002, 9002, 170000, 'MOMO', 'PAY-EDGE-UNPAID-FAILED-001', 'MOMO', 'FAILED', NULL, TRUE),
(9003, 9003, 151000, 'BANK_TRANSFER', 'PAY-EDGE-NOSTOCK-001', 'BANK', 'SUCCESS', '2026-06-06 09:35:00', TRUE),
(9004, 9004, 160000, 'VIETQR', 'PAY-EDGE-EXPIRED-001', 'VIETQR', 'SUCCESS', '2026-06-06 10:05:00', TRUE),
(9005, 9005, 198000, 'CASH', 'PAY-EDGE-FIFO-001', 'COUNTER', 'SUCCESS', '2026-06-06 09:05:00', TRUE),
(9006, 9006, 194000, 'CASH', 'PAY-EDGE-DUPMED-001', 'COUNTER', 'SUCCESS', '2026-06-06 09:35:00', TRUE),
(9007, 9007, 166000, 'CASH', 'PAY-EDGE-CANCELLED-001', 'COUNTER', 'SUCCESS', '2026-06-06 10:05:00', TRUE),
(9008, 9008, 162000, 'CASH', 'PAY-EDGE-DISPENSED-001', 'COUNTER', 'SUCCESS', '2026-06-06 10:35:00', TRUE),
(9009, 9009, 150000, 'CASH', 'PAY-EDGE-REFUND-001', 'COUNTER', 'REFUNDED', '2026-06-06 08:35:00', TRUE);

-- 11.6. Chat edge case: chua gan nhan vien, da gan, dang ho tro, da dong, khong co tin nhan, nhieu tin nhan.
INSERT INTO support_conversation (id, patient_id, staff_id, status, subject, created_at, updated_at, closed_at, active) VALUES
(9001, 201, NULL, 'OPEN', 'Edge chat chua gan nhan vien', '2026-06-06 07:30:00', '2026-06-06 07:30:00', NULL, TRUE),
(9002, 202, 8, 'ASSIGNED', 'Edge chat da gan va co lich Meet', '2026-06-06 07:40:00', '2026-06-06 07:50:00', NULL, TRUE),
(9003, 203, 8, 'IN_PROGRESS', 'Edge chat nhieu tin nhan', '2026-06-06 08:00:00', '2026-06-06 08:16:00', NULL, TRUE),
(9004, 204, 8, 'CLOSED', 'Edge chat da dong', '2026-06-06 08:30:00', '2026-06-06 08:45:00', '2026-06-06 08:45:00', TRUE),
(9005, 205, NULL, 'OPEN', 'Edge chat chua co tin nhan', '2026-06-06 08:50:00', '2026-06-06 08:50:00', NULL, TRUE);

INSERT INTO support_message (id, conversation_id, sender_id, content, message_type, created_at, is_read, active) VALUES
(9001, 9001, 1201, 'Toi can ho tro gap nhung chua co nhan vien tiep nhan.', 'TEXT', '2026-06-06 07:30:00', FALSE, TRUE),
(9002, 9002, 1202, 'Toi muon hen tu van truc tuyen voi bac si.', 'TEXT', '2026-06-06 07:40:00', TRUE, TRUE),
(9003, 9002, 208, 'Le tan da tiep nhan va se gui lich Google Meet.', 'TEXT', '2026-06-06 07:50:00', FALSE, TRUE),
(9004, 9002, 208, 'Da tao lich tu van online. Vui long xem link Google Meet trong muc lich tu van.', 'MEET_SCHEDULE', '2026-06-06 07:55:00', FALSE, TRUE),
(9005, 9003, 1203, 'Tin nhan edge 01 tu benh nhan.', 'TEXT', '2026-06-06 08:01:00', TRUE, TRUE),
(9006, 9003, 208, 'Tin nhan edge 02 tu nhan vien.', 'TEXT', '2026-06-06 08:02:00', TRUE, TRUE),
(9007, 9003, 1203, 'Tin nhan edge 03 tu benh nhan.', 'TEXT', '2026-06-06 08:03:00', TRUE, TRUE),
(9008, 9003, 208, 'Tin nhan edge 04 tu nhan vien.', 'TEXT', '2026-06-06 08:04:00', TRUE, TRUE),
(9009, 9003, 1203, 'Tin nhan edge 05 tu benh nhan.', 'TEXT', '2026-06-06 08:05:00', TRUE, TRUE),
(9010, 9003, 208, 'Tin nhan edge 06 tu nhan vien.', 'TEXT', '2026-06-06 08:06:00', TRUE, TRUE),
(9011, 9003, 1203, 'Tin nhan edge 07 tu benh nhan.', 'TEXT', '2026-06-06 08:07:00', TRUE, TRUE),
(9012, 9003, 208, 'Tin nhan edge 08 tu nhan vien.', 'TEXT', '2026-06-06 08:08:00', TRUE, TRUE),
(9013, 9003, 1203, 'Tin nhan edge 09 tu benh nhan.', 'TEXT', '2026-06-06 08:09:00', TRUE, TRUE),
(9014, 9003, 208, 'Tin nhan edge 10 tu nhan vien.', 'TEXT', '2026-06-06 08:10:00', TRUE, TRUE),
(9015, 9003, 1203, 'Tin nhan edge 11 moi nhat chua doc.', 'TEXT', '2026-06-06 08:15:00', FALSE, TRUE),
(9016, 9003, 208, 'Tin nhan edge 12 phan hoi moi nhat.', 'TEXT', '2026-06-06 08:16:00', FALSE, TRUE),
(9017, 9004, 1204, 'Cam on phong kham da ho tro.', 'TEXT', '2026-06-06 08:40:00', TRUE, TRUE);

INSERT INTO online_consultation_schedule (id, conversation_id, patient_id, doctor_id, staff_id, scheduled_start, scheduled_end, meet_link, status, note, active) VALUES
(9001, 9002, 202, 2, 8, '2026-06-07 09:00:00', '2026-06-07 09:30:00', 'https://meet.google.com/edge-assigned-001', 'SCHEDULED', 'Lich Meet tu chat da gan nhan vien.', TRUE),
(9002, 9003, 203, 3, 8, '2026-06-07 10:00:00', '2026-06-07 10:30:00', 'https://meet.google.com/edge-cancelled-001', 'CANCELLED', 'Benh nhan huy lich tu van.', TRUE),
(9003, 9004, 204, 4, 8, '2026-06-07 11:00:00', '2026-06-07 11:30:00', 'https://meet.google.com/edge-completed-001', 'COMPLETED', 'Da tu van xong va dong chat.', TRUE);

-- 11.7. Thong bao edge case.
INSERT INTO notification (id, user_id, title, content, notification_type, related_id, read_at, active) VALUES
(9001, 1201, 'Yeu cau ho tro dang cho', 'Yeu cau ho tro cua ban dang cho nhan vien tiep nhan.', 'GENERAL', 9001, NULL, TRUE),
(9002, 1202, 'Lich tu van Google Meet', 'Le tan da tao lich tu van truc tuyen cho ban.', 'APPOINTMENT_REMINDER', 9001, NULL, TRUE),
(9003, 1207, 'Thanh toan that bai', 'Hoa don cua ban chua duoc thanh toan thanh cong.', 'PAYMENT', 9002, NULL, TRUE),
(9004, 1215, 'Hoa don da hoan tien', 'Hoa don cua ban da duoc hoan tien.', 'PAYMENT', 9009, '2026-06-06 08:40:00', TRUE);

-- KIEM TRA NHANH SAU KHI IMPORT:
-- SELECT COUNT(*) FROM patient; -- 250
-- SELECT appointment_date, COUNT(*) FROM appointment GROUP BY appointment_date ORDER BY appointment_date;
-- SELECT i.invoice_code, i.payment_status, p.prescription_code, p.status FROM invoice i JOIN prescription p ON p.medical_record_id = i.medical_record_id WHERE i.payment_status='PAID' AND p.status='PRESCRIBED'; -- gom data goc va edge case
