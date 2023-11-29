CREATE TABLE boards (
  board_name varchar(50)
);

CREATE TABLE registration (
  username varchar(50),
  last_name varchar(50),
  first_name varchar(50),
  password varchar(100),
  board_name varchar(50)
);

ALTER TABLE boards
  ADD PRIMARY KEY (board_name);

ALTER TABLE registration
  ADD PRIMARY KEY (username);