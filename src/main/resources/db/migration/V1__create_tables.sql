CREATE TABLE IF NOT EXISTS Tables (
                                      id INT AUTO_INCREMENT PRIMARY KEY,
                                      table_number INT NOT NULL,
                                      capacity INT NOT NULL,
                                      status ENUM('AVAILABLE', 'RESERVED', 'OCCUPIED') NOT NULL
    );