USE loan_db;
SET SQL_SAFE_UPDATES = 0;

DELETE FROM application_history;
DELETE FROM loan_application;
DELETE FROM customer WHERE name LIKE 'Test Customer%';

SET SQL_SAFE_UPDATES = 1;