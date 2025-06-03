
drop database billing;
create database billing;

use billing;

-- 1. Rate Plan Table
CREATE TABLE rateplan (
                          plan_id INT PRIMARY KEY AUTO_INCREMENT,
                          rate_plan_name VARCHAR(100) NOT NULL
);

-- 2. Services Table
CREATE TABLE services (
                          id INT PRIMARY KEY AUTO_INCREMENT,
                          service_type ENUM('voice', 'data', 'sms') NOT NULL,
                          rate FLOAT NOT NULL
);

-- 3. Plan-Service Join Table
CREATE TABLE plan_service (
                              rate_plan_id INT,
                              service_id INT,
                              PRIMARY KEY (rate_plan_id, service_id),
                              FOREIGN KEY (rate_plan_id) REFERENCES rateplan(plan_id),
                              FOREIGN KEY (service_id) REFERENCES services(id)
);

-- Trigger: Ensure only one service of a type is allowed per rate plan
DELIMITER //
CREATE TRIGGER trg_check_service_type_per_plan
    BEFORE INSERT ON plan_service
    FOR EACH ROW
BEGIN
    DECLARE existing_count INT;
    SELECT COUNT(*) INTO existing_count
    FROM plan_service ps
             JOIN services s ON ps.service_id = s.id
    WHERE ps.rate_plan_id = NEW.rate_plan_id
      AND s.service_type = (SELECT service_type FROM services WHERE id = NEW.service_id);

    IF existing_count >= 1 THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Only one service of each type is allowed per rate plan';
END IF;
END;
//
DELIMITER ;

-- 4. Customer Table
CREATE TABLE customer (
                          msisdn VARCHAR(15) PRIMARY KEY,
                          name VARCHAR(100) NOT NULL,
                          email VARCHAR(100),
                          rateplan_id INT,
                          FOREIGN KEY (rateplan_id) REFERENCES rateplan(plan_id)
);


drop table rated_cdrs;
-- 5. Input CDRs Table
CREATE TABLE input_cdrs (
                            id INT AUTO_INCREMENT PRIMARY KEY,
                            dial_a VARCHAR(15) NOT NULL,
                            dial_b VARCHAR(15) NOT NULL,
                            service_type ENUM('voice', 'data', 'sms') NOT NULL,
                            volume INT NOT NULL,
                            start_time DATETIME NOT NULL,
                            external_charges DECIMAL(10,2) DEFAULT 0
);

-- 6. Rated CDRs Table
CREATE TABLE rated_cdrs (
                            id INT AUTO_INCREMENT PRIMARY KEY,
                            dial_a VARCHAR(15) NOT NULL,
                            dial_b VARCHAR(15) NOT NULL,
                            service_type ENUM('voice', 'data', 'sms') NOT NULL,
                            volume INT NOT NULL,
                            start_time DATETIME NOT NULL,
                            total DECIMAL(10,2) NOT NULL
);

-- 7. Customer Invoice Table
CREATE TABLE customer_invoice (
                                  id INT AUTO_INCREMENT PRIMARY KEY,
                                  customer_msisdn VARCHAR(15) NOT NULL,
                                  service_type ENUM('voice', 'data', 'sms') NOT NULL,
                                  total_volume INT NOT NULL,
                                  total_charges DECIMAL(10,2) NOT NULL,
                                  invoice_date DATE NOT NULL DEFAULT (CURRENT_DATE),
                                  FOREIGN KEY (customer_msisdn) REFERENCES customer(msisdn)
);

drop table	invoice;

create table invoice(
                        id INT AUTO_INCREMENT PRIMARY KEY,
                        customer_msisdn VARCHAR(15) NOT NULL,
                        total_charges DECIMAL(10,2) NOT NULL,
                        invoice_date DATE NOT NULL DEFAULT (CURRENT_DATE)
);


-- 1. Insert Rate Plans
INSERT INTO rateplan (rate_plan_name) VALUES
                                          ('Basic Plan'),
                                          ('Premium Plan');

-- 2. Insert Services
INSERT INTO services (service_type, rate) VALUES
                                              ('voice', 0.5),
                                              ('data', 0.05),
                                              ('sms', 0.1);

-- 3. Insert Plan-Service (Make sure trigger rules are respected)
INSERT INTO plan_service (rate_plan_id, service_id) VALUES
                                                        (1, 1),  -- Basic: voice
                                                        (1, 2),  -- Basic: data
                                                        (2, 3);  -- Premium: sms

-- 4. Insert Customers
INSERT INTO customer (msisdn, name, email, rateplan_id) VALUES
                                                            ('201111111111', 'Alice', 'alice@example.com', 1),
                                                            ('201222222222', 'Bob', 'bob@example.com', 1),
                                                            ('201333333333', 'Charlie', 'charlie@example.com', 2);

-- 5. Insert Input CDRs
INSERT INTO input_cdrs (dial_a, dial_b, service_type, volume, start_time, external_charges) VALUES
                                                                                                ('201111111111', '201000000001', 'voice', 120, '2025-05-01 09:00:00', 0.00),
                                                                                                ('201222222222', '201000000002', 'data', 500, '2025-05-01 10:00:00', 0.00),
                                                                                                ('201333333333', '201000000003', 'sms', 1, '2025-05-01 11:00:00', 0.00);

-- 6. Insert Rated CDRs (without foreign key now)
INSERT INTO rated_cdrs (dial_a, dial_b, service_type, volume, start_time, total) VALUES
                                                                                     ('201111111111', '201000000001', 'voice', 120, '2025-05-01 09:00:00', 60.00),
                                                                                     ('201222222222', '201000000002', 'data', 500, '2025-05-01 10:00:00', 25.00),
                                                                                     ('201333333333', '201000000003', 'sms', 1, '2025-05-01 11:00:00', 0.10);

-- 7. Insert Customer Invoices (per service)
-- INSERT INTO customer_invoice (customer_msisdn, service_type, total_volume, total_charges) VALUES 
-- ('201111111111', 'voice', 120, 60.00),
-- ('201222222222', 'data', 500, 25.00),
-- ('201333333333', 'sms', 1, 0.10);

truncate table customer_invoice;
truncate table invoice;

select * from customer_invoice;

select * from invoice;
-- -- 8. Insert into General Invoice Table (total charges per customer)
-- INSERT INTO invoice (id, customer_msisdn, total_charges) VALUES 
-- (1, '201111111111', 60.00),
-- (2, '201222222222', 25.00),
-- (3, '201333333333', 0.10);







