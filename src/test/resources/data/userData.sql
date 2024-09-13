INSERT INTO [user] (
    id,
    name,
    email,
    gender,
    username,
    password,
    enabled,
    department_id
) VALUES (
    1300,
    'admin',
    'Sed ut perspiciatis.',
    'MALE',
    'No sea takimata.',
    '{bcrypt}$2a$10$FMzmOkkfbApEWxS.4XzCKOR7EbbiwzkPEyGgYh6uQiPxurkpzRMa6',
    1,
    1100
);

INSERT INTO user_role (
    user_id,
    role_id
) VALUES (
    1300,
    1400
);

INSERT INTO [user] (
    id,
    name,
    email,
    gender,
    username,
    password,
    enabled,
    department_id
) VALUES (
    1301,
    'itops',
    'Lorem ipsum dolor.',
    'MALE',
    'Vel illum dolore.',
    '{bcrypt}$2a$10$FMzmOkkfbApEWxS.4XzCKOR7EbbiwzkPEyGgYh6uQiPxurkpzRMa6',
    0,
    1101
);

INSERT INTO user_role (
    user_id,
    role_id
) VALUES (
    1301,
    1401
);

INSERT INTO [user] (
    id,
    name,
    email,
    gender,
    username,
    password,
    enabled,
    department_id
) VALUES (
    1302,
    'observer',
    'Duis autem vel.',
    'MALE',
    'Consectetuer adipiscing.',
    '{bcrypt}$2a$10$FMzmOkkfbApEWxS.4XzCKOR7EbbiwzkPEyGgYh6uQiPxurkpzRMa6',
    1,
    1102
);

INSERT INTO user_role (
    user_id,
    role_id
) VALUES (
    1302,
    1402
);
