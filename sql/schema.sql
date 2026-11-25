CREATE DATABASE IF NOT EXISTS loan_db;
USE loan_db;
SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS ng_core_banking;
DROP TABLE IF EXISTS utility_run;
DROP TABLE IF EXISTS system_alerts;
DROP TABLE IF EXISTS application_history;
DROP TABLE IF EXISTS disbursement;
DROP TABLE IF EXISTS loan_application;
DROP TABLE IF EXISTS customer;
SET FOREIGN_KEY_CHECKS = 1;

CREATE TABLE customer (
    customer_id   INT AUTO_INCREMENT PRIMARY KEY,
    name          VARCHAR(100) NOT NULL,
    date_of_birth DATE         NOT NULL,
    mobile        CHAR(10)     NOT NULL UNIQUE,
    address       VARCHAR(255),
    kyc_details   VARCHAR(255)
);

CREATE TABLE loan_application (
    application_id               INT AUTO_INCREMENT PRIMARY KEY,
    application_number           VARCHAR(30)   NOT NULL UNIQUE,
    customer_id                  INT           NOT NULL,
    product                      VARCHAR(20)   NOT NULL,
    branch_code                  VARCHAR(10),
    branch_name                  VARCHAR(100),
    requested_amount             DECIMAL(15,2) NOT NULL,
    tenure_months                INT           NOT NULL,
    monthly_income               DECIMAL(15,2) NOT NULL,
    existing_monthly_obligations DECIMAL(15,2) NOT NULL DEFAULT 0,
    annual_interest_rate         DECIMAL(5,2),
    proposed_emi                 DECIMAL(15,2),
    foir_percentage              DECIMAL(6,2),
    eligible                     BOOLEAN,
    eligibility_reasons          VARCHAR(1000),
    status                       VARCHAR(20)   NOT NULL DEFAULT 'DRAFT',
    created_at                   DATETIME      NOT NULL,
    submitted_at                 DATETIME,
    decided_at                   DATETIME,
    reviewer_code                VARCHAR(20),
    decision_reason              VARCHAR(500),
    CONSTRAINT fk_app_customer FOREIGN KEY (customer_id)
        REFERENCES customer(customer_id)
);
CREATE TABLE disbursement (
    disbursement_id INT AUTO_INCREMENT PRIMARY KEY,
    application_id  INT           NOT NULL UNIQUE,
    reference       VARCHAR(40)   NOT NULL UNIQUE,
    amount          DECIMAL(15,2) NOT NULL,
    disbursed_at    DATETIME      NOT NULL,
    CONSTRAINT fk_disb_app FOREIGN KEY (application_id)
        REFERENCES loan_application(application_id)
);

CREATE TABLE application_history (
    history_id     INT AUTO_INCREMENT PRIMARY KEY,
    application_id INT         NOT NULL,
    action         VARCHAR(30) NOT NULL,
    old_status     VARCHAR(20),
    new_status     VARCHAR(20),
    performed_by   VARCHAR(20) NOT NULL,
    performed_at   DATETIME    NOT NULL,
    remarks        VARCHAR(500),
    CONSTRAINT fk_hist_app FOREIGN KEY (application_id)
        REFERENCES loan_application(application_id)
);

CREATE TABLE system_alerts (
    alert_id       INT AUTO_INCREMENT PRIMARY KEY,
    application_id INT         NOT NULL,
    alert_type     VARCHAR(30) NOT NULL,
    created_at     DATETIME    NOT NULL,
    CONSTRAINT uq_alert UNIQUE (application_id, alert_type),
    CONSTRAINT fk_alert_app FOREIGN KEY (application_id)
        REFERENCES loan_application(application_id)
);

CREATE TABLE utility_run (
    run_id                 INT AUTO_INCREMENT PRIMARY KEY,
    started_at             DATETIME    NOT NULL,
    completed_at           DATETIME,
    result                 VARCHAR(20),
    applications_processed INT DEFAULT 0,
    alerts_created         INT DEFAULT 0,
    error_message          VARCHAR(500)
);

CREATE TABLE ng_core_banking (
    core_id          INT AUTO_INCREMENT PRIMARY KEY,
    application_id   INT           NOT NULL UNIQUE,
    customer_id      INT           NOT NULL,
    customer_name    VARCHAR(100)  NOT NULL,
    address          VARCHAR(255),
    dob              DATE,
    kyc_details      VARCHAR(255),
    approved_amount  DECIMAL(15,2) NOT NULL,
    disbursed_amount DECIMAL(15,2) NOT NULL,
    disbursed_date   DATETIME      NOT NULL
);