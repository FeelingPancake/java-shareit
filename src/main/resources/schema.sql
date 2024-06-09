DROP TABLE IF EXISTS comments;
DROP TABLE IF EXISTS bookings;
DROP TABLE IF EXISTS items;
DROP TABLE IF EXISTS users;

CREATE TABLE IF NOT EXISTS users (
  id BIGINT GENERATED ALWAYS AS IDENTITY NOT NULL,
  name VARCHAR(255) NOT NULL,
  email VARCHAR(512) NOT NULL,
  CONSTRAINT pk_user PRIMARY KEY (id),
  CONSTRAINT UQ_USER_EMAIL UNIQUE (email)
);

CREATE TABLE IF NOT EXISTS items (
  id BIGINT GENERATED ALWAYS AS IDENTITY NOT NULL,
  owner_id BIGINT references users(id) NOT NULL,
  name VARCHAR(255) NOT NULL,
  description VARCHAR(512) NOT NULL,
  is_available boolean NOT NULL,
  CONSTRAINT pk_item PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS bookings (
   id BIGINT GENERATED ALWAYS AS IDENTITY NOT NULL,
   item_id BIGINT REFERENCES items(id) NOT NULL,
   user_id BIGINT REFERENCES users(id) NOT NULL,
   start_date TIMESTAMP NOT NULL,
   end_date TIMESTAMP NOT NULL,
   status VARCHAR(20) NOT NULL CHECK (status IN ('WAITING', 'APPROVED', 'REJECTED')),
   created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
   CONSTRAINT pk_booking PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS comments (
    id BIGINT GENERATED ALWAYS AS IDENTITY NOT NULL,
    item_id BIGINT REFERENCES items(id) NOT NULL,
    user_id BIGINT REFERENCES users(id) NOT NULL,
    text VARCHAR(600),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_comments PRIMARY KEY (id)
);