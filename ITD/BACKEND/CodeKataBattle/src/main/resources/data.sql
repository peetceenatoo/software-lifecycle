-- INSERT INTO roles (name) VALUES ('Student');
-- INSERT INTO roles (name) VALUES ('Educator');

-- INSERT INTO users (username, password, email, role_id) VALUES ('testuser', 'testpassword', 'testuser@example.com', (SELECT id FROM roles WHERE name = 'Student'));
