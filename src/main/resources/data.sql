INSERT INTO roles (name) VALUES ('ROLE_ADMIN');
INSERT INTO roles (name) VALUES ('ROLE_CANDIDATE');
INSERT INTO roles (name) VALUES ('ROLE_RECRUITER');

-- Admin User: admin@smarthire.com / admin123
INSERT INTO users (name, email, password, created_at) VALUES ('System Admin', 'admin@smarthire.com', '$2a$10$wZk0xX0T1E1y/T5f1t0V6O5i7JzI6fQ6E7y9a0lD/kM.G3R1L8gBq', NOW());

INSERT INTO user_roles (user_id, role_id) VALUES (
    (SELECT id FROM users WHERE email = 'admin@smarthire.com'),
    (SELECT id FROM roles WHERE name = 'ROLE_ADMIN')
);
