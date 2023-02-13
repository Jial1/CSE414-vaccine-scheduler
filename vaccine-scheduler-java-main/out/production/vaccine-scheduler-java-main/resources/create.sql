CREATE TABLE Caregivers (
    Username varchar(255),
    Salt BINARY(16),
    Hash BINARY(16),
    PRIMARY KEY (Username)
);

CREATE TABLE Patients (
    Username varchar(255),
    Salt BINARY(16),
    Hash BINARY(16),
    PRIMARY KEY (Username)
);

CREATE TABLE Availabilities (
    Time date,
    Username varchar(255) REFERENCES Caregivers,
    PRIMARY KEY (Time, Username)
);

CREATE TABLE Vaccines (
    Name varchar(255),
    Doses int,
    PRIMARY KEY (Name)
);

CREATE TABLE Appointment (
      Appointment_id varchar(255),
      Caregivers_name varchar(255) REFERENCES Caregivers(Username),
      Patient_name varchar(255) REFERENCES Patients(Username),
      Vaccine_name varchar(255) REFERENCES Vaccines(Name),
      Time date,
      PRIMARY KEY(Appointment_id)
)