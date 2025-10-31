CREATE DATABASE IF NOT EXISTS cbs CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE cbs;

CREATE TABLE IF NOT EXISTS users (
	id BIGINT PRIMARY KEY AUTO_INCREMENT,
	name VARCHAR(100) NOT NULL,
	email VARCHAR(120) NOT NULL UNIQUE,
	password_hash VARCHAR(64) NOT NULL
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS cycles (
	id BIGINT PRIMARY KEY AUTO_INCREMENT,
	model VARCHAR(120) NOT NULL,
	owner_email VARCHAR(120) NOT NULL,
	lat DOUBLE NULL,
	lon DOUBLE NULL,
	borrowed_by_user_id BIGINT NULL,
	INDEX (owner_email)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS borrow_requests (
	id BIGINT PRIMARY KEY AUTO_INCREMENT,
	cycle_id BIGINT NOT NULL,
	owner_email VARCHAR(120) NOT NULL,
	borrower_user_id BIGINT NOT NULL,
	status ENUM('PENDING','ACCEPTED','DECLINED') NOT NULL DEFAULT 'PENDING',
	created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
	INDEX (owner_email),
	INDEX (borrower_user_id),
	INDEX (cycle_id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS user_location_tokens (
	user_id BIGINT PRIMARY KEY,
	location_token VARCHAR(64) NOT NULL,
	created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
	FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS borrow_tracking (
	cycle_id BIGINT PRIMARY KEY,
	borrower_user_id BIGINT NOT NULL,
	borrower_token VARCHAR(64) NULL,
	lat DOUBLE NULL,
	lon DOUBLE NULL,
	updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- seed a few cycles
INSERT INTO cycles(model, owner_email, lat, lon, borrowed_by_user_id)
VALUES
	('Red-Servant', 'alice@campus.edu', 17.38714, 78.491684, NULL),
	('Blue-Rider', 'bob@campus.edu', 17.395, 78.492, NULL),
	('Green-Spin', 'carol@campus.edu', 17.38, 78.48, NULL)
ON DUPLICATE KEY UPDATE model = VALUES(model);

-- create dedicated app user (adjust password as needed)
CREATE USER IF NOT EXISTS 'cbs_user'@'localhost' IDENTIFIED BY 'StrongPassword123!';
ALTER USER 'cbs_user'@'localhost' IDENTIFIED WITH mysql_native_password BY 'StrongPassword123!';
GRANT ALL PRIVILEGES ON cbs.* TO 'cbs_user'@'localhost';
FLUSH PRIVILEGES;
