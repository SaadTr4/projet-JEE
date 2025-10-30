-- Table Department
CREATE TABLE department (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT
);

-- Table Position
CREATE TABLE position (
      id SERIAL PRIMARY KEY,
      name VARCHAR(100) NOT NULL,
      description TEXT
);

-- Table Project
CREATE TABLE project (
     id SERIAL PRIMARY KEY,
     name VARCHAR(100) NOT NULL,
     description TEXT,
     status VARCHAR(20) DEFAULT 'In progress'
);

-- Table User
CREATE TABLE user_account (
      registration_number VARCHAR(20) PRIMARY KEY,
      last_name VARCHAR(50) NOT NULL,
      first_name VARCHAR(50) NOT NULL,
      email VARCHAR(100) UNIQUE NOT NULL,
      phone VARCHAR(20),
      image BYTEA,
      address VARCHAR(255),
      grade VARCHAR(20),
      role VARCHAR(20),
      department_id INT,
      position_id INT,
      FOREIGN KEY (department_id) REFERENCES department(id),
      FOREIGN KEY (position_id) REFERENCES position(id)
);

-- N-N Relationship Table User-Project, can't use "user" as table name because it's a reserved word in SQL
CREATE TABLE user_project (
      registration_number VARCHAR(20),
      project_id INT,
      PRIMARY KEY (registration_number, project_id),
      FOREIGN KEY (registration_number) REFERENCES user_account(registration_number) ON DELETE CASCADE,
      FOREIGN KEY (project_id) REFERENCES project(id) ON DELETE CASCADE
);

-- Table Payslip
CREATE TABLE payslip (
     id SERIAL PRIMARY KEY,
     employee_number VARCHAR(20),
     date DATE NOT NULL,
     base_salary DECIMAL(10,2),
     bonuses DECIMAL(10,2),
     deductions DECIMAL(10,2),
     net_pay DECIMAL(10,2),
     FOREIGN KEY (employee_number) REFERENCES user_account(employee_number) ON DELETE CASCADE
);
