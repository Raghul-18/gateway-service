-- V2__insert_admin_user.sql

INSERT INTO users (username, role, password_hash, enabled, created_at)
VALUES (
           'admin',
           'ADMIN',
           '$2a$10$ZgoOcMQyP8nMPCXXIodD.OQnCBtfa0tfP6Th5gHfjI.AwXY/5p8SG', -- password: admin123
           1,
           CURRENT_TIMESTAMP
       );
