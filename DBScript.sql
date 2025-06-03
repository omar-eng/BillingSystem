create table invoice(
    id INT AUTO_INCREMENT PRIMARY KEY,
	customer_msisdn VARCHAR(15) NOT NULL,
    total_charges DECIMAL(10,2) NOT NULL,
    invoice_date DATE NOT NULL DEFAULT (CURRENT_DATE)
);

CREATE TABLE customer_invoice (
    id INT AUTO_INCREMENT PRIMARY KEY,
    customer_msisdn VARCHAR(15) NOT NULL,
    service_type ENUM('voice', 'data', 'sms') NOT NULL,
    total_volume INT NOT NULL,
    total_charges DECIMAL(10,2) NOT NULL,
    invoice_date DATE NOT NULL DEFAULT (CURRENT_DATE)
);

CREATE TABLE rated_cdrs (
    id INT AUTO_INCREMENT PRIMARY KEY,
    dial_a VARCHAR(15) NOT NULL,
    dial_b VARCHAR(15) NOT NULL,
	service_type ENUM('voice', 'data', 'sms') NOT NULL,
    volume INT NOT NULL,
    start_time DATETIME NOT NULL,
    total DECIMAL(10,2) NOT NULL
);

CREATE TABLE IF NOT EXISTS cdrs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    dial_a VARCHAR(20) NOT NULL,
    dial_b VARCHAR(255),
    service_type VARCHAR(20) NOT NULL,
    `usage` BIGINT NOT NULL,
    start_time DATETIME NOT NULL,
    external_charges DOUBLE NOT NULL,
    customer_id BIGINT,
    invoice_id BIGINT,
    FOREIGN KEY (customer_id) REFERENCES customers(id)
);

CREATE TABLE IF NOT EXISTS service_subscriptions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    customer_id BIGINT NOT NULL,
    service_package_id BIGINT NOT NULL,
    start_date DATETIME NOT NULL,
    end_date DATETIME,
    active BOOLEAN NOT NULL,
    remaining_free_units INT NOT NULL,
    FOREIGN KEY (customer_id) REFERENCES customers(id),
    FOREIGN KEY (service_package_id) REFERENCES service_packages(id)
);

CREATE TABLE IF NOT EXISTS customers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    phone_number VARCHAR(20) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL,
    address TEXT NOT NULL,
    profile_id BIGINT,
    FOREIGN KEY (profile_id) REFERENCES rate_plans(id)
);

CREATE TABLE IF NOT EXISTS services (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description TEXT NOT NULL,
    type VARCHAR(20) NOT NULL,
    unit_price DECIMAL(10,2) NOT NULL,
    unit_type VARCHAR(20) NOT NULL,
    service_package_id BIGINT,
    FOREIGN KEY (service_package_id) REFERENCES service_packages(id)
);
CREATE TABLE IF NOT EXISTS service_packages (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description TEXT NOT NULL,
    price DECIMAL(10,2) NOT NULL,
    profile_id BIGINT NOT NULL,
    is_recurring BOOLEAN NOT NULL,
    free_units INT,
    FOREIGN KEY (profile_id) REFERENCES rate_plans (id)
);

CREATE TABLE IF NOT EXISTS rate_plans (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description TEXT NOT NULL,
    base_price DECIMAL(10,2) NOT NULL
);