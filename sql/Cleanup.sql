USE loan_db;
SET SQL_SAFE_UPDATES = 0;

DELETE FROM system_alerts WHERE application_id IN
  (SELECT application_id FROM loan_application WHERE customer_id IN
    (SELECT customer_id FROM customer WHERE name LIKE 'Test Customer%'));

DELETE FROM ng_core_banking WHERE application_id IN
  (SELECT application_id FROM loan_application WHERE customer_id IN
    (SELECT customer_id FROM customer WHERE name LIKE 'Test Customer%'));

DELETE FROM disbursement WHERE application_id IN
  (SELECT application_id FROM loan_application WHERE customer_id IN
    (SELECT customer_id FROM customer WHERE name LIKE 'Test Customer%'));

DELETE FROM application_history WHERE application_id IN
  (SELECT application_id FROM loan_application WHERE customer_id IN
    (SELECT customer_id FROM customer WHERE name LIKE 'Test Customer%'));

DELETE FROM loan_application WHERE customer_id IN
  (SELECT customer_id FROM customer WHERE name LIKE 'Test Customer%');

DELETE FROM customer WHERE name LIKE 'Test Customer%';

SET SQL_SAFE_UPDATES = 1;