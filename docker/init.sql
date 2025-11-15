-- Table Department
CREATE TABLE department (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    code VARCHAR(20) UNIQUE NOT NULL,
    description TEXT
);

-- Table Position
CREATE TABLE position (
      id SERIAL PRIMARY KEY,
      name VARCHAR(100) NOT NULL,
      description TEXT
);

-- Table User
CREATE TABLE user_account (
      id SERIAL PRIMARY KEY,
      registration_number VARCHAR(20) UNIQUE NOT NULL,
      last_name VARCHAR(50) NOT NULL,
      first_name VARCHAR(50) NOT NULL,
      email VARCHAR(100) UNIQUE NOT NULL,
      phone VARCHAR(20),
      password VARCHAR(255) NOT NULL,
      image BYTEA,
      address VARCHAR(255),
      grade VARCHAR(20),
      role VARCHAR(20),
      department_id INT,
      position_id INT,
      FOREIGN KEY (department_id) REFERENCES department(id),
      FOREIGN KEY (position_id) REFERENCES position(id)
);

-- Table Project
CREATE TABLE project (
     id SERIAL PRIMARY KEY,
     name VARCHAR(100) NOT NULL,
     description TEXT,
     project_manager_id INT,
     status VARCHAR(20) DEFAULT 'IN_PROGRESS',
     FOREIGN KEY (project_manager_id) REFERENCES user_account(id)
);

-- N-N Relationship Table User-Project, can't use "user" as table name because it's a reserved word in SQL
CREATE TABLE user_project (
      user_id INT NOT NULL,
      project_id INT NOT NULL,
      PRIMARY KEY (user_id, project_id),
      FOREIGN KEY (user_id) REFERENCES user_account(id) ON DELETE CASCADE,
      FOREIGN KEY (project_id) REFERENCES project(id) ON DELETE CASCADE
);

-- Table Payslip
CREATE TABLE payslip (
     id SERIAL PRIMARY KEY,
     registration_number VARCHAR(20),
     year INT NOT NULL,
     month INT NOT NULL CHECK (month BETWEEN 1 AND 12),
     base_salary DECIMAL(10,2),
     bonuses DECIMAL(10,2),
     deductions DECIMAL(10,2),
     net_pay DECIMAL(10,2),
     generation_date DATE NOT NULL DEFAULT CURRENT_DATE,
     FOREIGN KEY (registration_number) REFERENCES user_account(registration_number) ON DELETE CASCADE,
     CONSTRAINT unique_payslip_per_period UNIQUE (registration_number, year, month)
);