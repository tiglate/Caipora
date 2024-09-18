IF NOT EXISTS(SELECT 1 FROM tb_department)
BEGIN
    INSERT INTO tb_department (name) VALUES ('IT');
    INSERT INTO tb_department (name) VALUES ('Business Risk Management');
    INSERT INTO tb_department (name) VALUES ('Client Desk');
    INSERT INTO tb_department (name) VALUES ('Compliance');
    INSERT INTO tb_department (name) VALUES ('Enterprise Risk Management');
    INSERT INTO tb_department (name) VALUES ('FLDS');
    INSERT INTO tb_department (name) VALUES ('Finance');
    INSERT INTO tb_department (name) VALUES ('Legal');
    INSERT INTO tb_department (name) VALUES ('Middle Office');
    INSERT INTO tb_department (name) VALUES ('Operations');
    INSERT INTO tb_department (name) VALUES ('Trading Desk');
END

IF NOT EXISTS(SELECT 1 FROM tb_role)
BEGIN
    INSERT INTO tb_role (name) VALUES ('ROLE_DEPARTMENT_VIEW');
    INSERT INTO tb_role (name) VALUES ('ROLE_DEPARTMENT_MANAGE');
    INSERT INTO tb_role (name) VALUES ('ROLE_USER_VIEW');
    INSERT INTO tb_role (name) VALUES ('ROLE_USER_MANAGE');
    INSERT INTO tb_role (name) VALUES ('ROLE_VERSION_VIEW');
    INSERT INTO tb_role (name) VALUES ('ROLE_VERSION_MANAGE');
    INSERT INTO tb_role (name) VALUES ('ROLE_STAKEHOLDER_VIEW');
    INSERT INTO tb_role (name) VALUES ('ROLE_STAKEHOLDER_MANAGE');
    INSERT INTO tb_role (name) VALUES ('ROLE_APPLICATION_VIEW');
    INSERT INTO tb_role (name) VALUES ('ROLE_APPLICATION_MANAGE');
    INSERT INTO tb_role (name) VALUES ('ROLE_DEPLOY_VIEW');
    INSERT INTO tb_role (name) VALUES ('ROLE_DEPLOY_MANAGE');
    INSERT INTO tb_role (name) VALUES ('ROLE_SWAGGER_ACCESS');
    INSERT INTO tb_role (name) VALUES ('ROLE_ACTUATOR_ACCESS');
END

IF NOT EXISTS(SELECT 1 FROM tb_user)
BEGIN
    DECLARE @Id_User INT

    INSERT INTO tb_user (
        id_department,
        name,
        email,
        gender,
        username,
        password,
        enabled)
    SELECT
        id_department,
        name     = 'Admin',
        email    = 'admin@admin.com',
        gender   = 'MALE',
        username = 'admin',
        password = '{bcrypt}$2a$12$NYZurvH.l.vujYDufA6X6uFLBqQ1tDSDxX5VPTAcKSpNxJ3mBiWOW', -- 12345
        enabled  = 1
    FROM
        tb_department
    WHERE
        name = 'IT'

    SET @Id_User = SCOPE_IDENTITY()

    INSERT INTO tb_user_role (id_user, id_role)
    SELECT
        @Id_User,
        id_role
    FROM
        tb_role

    INSERT INTO tb_user (id_department, name, email, gender, username, password, enabled) VALUES
    (1, N'Deodoro da Fonseca', 'deodoro.fonseca@example.com', 'MALE', 'deodoro.fonseca', '{bcrypt}$2a$10$e0MYzXyjpJS7Pd0RVvHwHeFx4r1eH1B2d9uTQ9z5e5G8y5FzFQF92', 1),
    (2, N'Floriano Peixoto', 'floriano.peixoto@example.com', 'MALE', 'floriano.peixoto', '{bcrypt}$2a$10$e0MYzXyjpJS7Pd0RVvHwHeFx4r1eH1B2d9uTQ9z5e5G8y5FzFQF92', 0),
    (3, N'Prudente de Morais', 'prudente.morais@example.com', 'MALE', 'prudente.morais', '{bcrypt}$2a$10$e0MYzXyjpJS7Pd0RVvHwHeFx4r1eH1B2d9uTQ9z5e5G8y5FzFQF92', 1),
    (4, N'Campos Sales', 'campos.sales@example.com', 'MALE', 'campos.sales', '{bcrypt}$2a$10$e0MYzXyjpJS7Pd0RVvHwHeFx4r1eH1B2d9uTQ9z5e5G8y5FzFQF92', 0),
    (5, N'Rodrigues Alves', 'rodrigues.alves@example.com', 'MALE', 'rodrigues.alves', '{bcrypt}$2a$10$e0MYzXyjpJS7Pd0RVvHwHeFx4r1eH1B2d9uTQ9z5e5G8y5FzFQF92', 1),
    (6, N'Afonso Pena', 'afonso.pena@example.com', 'MALE', 'afonso.pena', '{bcrypt}$2a$10$e0MYzXyjpJS7Pd0RVvHwHeFx4r1eH1B2d9uTQ9z5e5G8y5FzFQF92', 0),
    (7, N'Nilo Peçanha', 'nilo.pecanha@example.com', 'MALE', 'nilo.pecanha', '{bcrypt}$2a$10$e0MYzXyjpJS7Pd0RVvHwHeFx4r1eH1B2d9uTQ9z5e5G8y5FzFQF92', 1),
    (8, N'Hermes da Fonseca', 'hermes.fonseca@example.com', 'MALE', 'hermes.fonseca', '{bcrypt}$2a$10$e0MYzXyjpJS7Pd0RVvHwHeFx4r1eH1B2d9uTQ9z5e5G8y5FzFQF92', 0),
    (9, N'Venceslau Brás', 'venceslau.bras@example.com', 'MALE', 'venceslau.bras', '{bcrypt}$2a$10$e0MYzXyjpJS7Pd0RVvHwHeFx4r1eH1B2d9uTQ9z5e5G8y5FzFQF92', 1),
    (10, N'Delfim Moreira', 'delfim.moreira@example.com', 'MALE', 'delfim.moreira', '{bcrypt}$2a$10$e0MYzXyjpJS7Pd0RVvHwHeFx4r1eH1B2d9uTQ9z5e5G8y5FzFQF92', 0),
    (11, N'Epitácio Pessoa', 'epitacio.pessoa@example.com', 'MALE', 'epitacio.pessoa', '{bcrypt}$2a$10$e0MYzXyjpJS7Pd0RVvHwHeFx4r1eH1B2d9uTQ9z5e5G8y5FzFQF92', 1),
    (1, N'Artur Bernardes', 'artur.bernardes@example.com', 'MALE', 'artur.bernardes', '{bcrypt}$2a$10$e0MYzXyjpJS7Pd0RVvHwHeFx4r1eH1B2d9uTQ9z5e5G8y5FzFQF92', 0),
    (2, N'Washington Luís', 'washington.luis@example.com', 'MALE', 'washington.luis', '{bcrypt}$2a$10$e0MYzXyjpJS7Pd0RVvHwHeFx4r1eH1B2d9uTQ9z5e5G8y5FzFQF92', 1),
    (3, N'Getúlio Vargas', 'getulio.vargas@example.com', 'MALE', 'getulio.vargas', '{bcrypt}$2a$10$e0MYzXyjpJS7Pd0RVvHwHeFx4r1eH1B2d9uTQ9z5e5G8y5FzFQF92', 0),
    (4, N'José Linhares', 'jose.linhares@example.com', 'MALE', 'jose.linhares', '{bcrypt}$2a$10$e0MYzXyjpJS7Pd0RVvHwHeFx4r1eH1B2d9uTQ9z5e5G8y5FzFQF92', 1),
    (5, N'Eurico Gaspar Dutra', 'eurico.dutra@example.com', 'MALE', 'eurico.dutra', '{bcrypt}$2a$10$e0MYzXyjpJS7Pd0RVvHwHeFx4r1eH1B2d9uTQ9z5e5G8y5FzFQF92', 0),
    (6, N'Getúlio Vargas', 'getulio.vargas2@example.com', 'MALE', 'getulio.vargas2', '{bcrypt}$2a$10$e0MYzXyjpJS7Pd0RVvHwHeFx4r1eH1B2d9uTQ9z5e5G8y5FzFQF92', 1),
    (7, N'Café Filho', 'cafe.filho@example.com', 'MALE', 'cafe.filho', '{bcrypt}$2a$10$e0MYzXyjpJS7Pd0RVvHwHeFx4r1eH1B2d9uTQ9z5e5G8y5FzFQF92', 0),
    (8, N'Carlos Luz', 'carlos.luz@example.com', 'MALE', 'carlos.luz', '{bcrypt}$2a$10$e0MYzXyjpJS7Pd0RVvHwHeFx4r1eH1B2d9uTQ9z5e5G8y5FzFQF92', 1),
    (9, N'Nereu Ramos', 'nereu.ramos@example.com', 'MALE', 'nereu.ramos', '{bcrypt}$2a$10$e0MYzXyjpJS7Pd0RVvHwHeFx4r1eH1B2d9uTQ9z5e5G8y5FzFQF92', 0),
    (10, N'Juscelino Kubitschek', 'juscelino.kubitschek@example.com', 'MALE', 'juscelino.kubitschek', '{bcrypt}$2a$10$e0MYzXyjpJS7Pd0RVvHwHeFx4r1eH1B2d9uTQ9z5e5G8y5FzFQF92', 1),
    (11, N'Jânio Quadros', 'janio.quadros@example.com', 'MALE', 'janio.quadros', '{bcrypt}$2a$10$e0MYzXyjpJS7Pd0RVvHwHeFx4r1eH1B2d9uTQ9z5e5G8y5FzFQF92', 0),
    (1, N'Ranieri Mazzilli', 'ranieri.mazzilli@example.com', 'MALE', 'ranieri.mazzilli', '{bcrypt}$2a$10$e0MYzXyjpJS7Pd0RVvHwHeFx4r1eH1B2d9uTQ9z5e5G8y5FzFQF92', 1),
    (2, N'João Goulart', 'joao.goulart@example.com', 'MALE', 'joao.goulart', '{bcrypt}$2a$10$e0MYzXyjpJS7Pd0RVvHwHeFx4r1eH1B2d9uTQ9z5e5G8y5FzFQF92', 0),
    (3, N'Ranieri Mazzilli', 'ranieri.mazzilli2@example.com', 'MALE', 'ranieri.mazzilli2', '{bcrypt}$2a$10$e0MYzXyjpJS7Pd0RVvHwHeFx4r1eH1B2d9uTQ9z5e5G8y5FzFQF92', 1),
    (4, N'Humberto Castelo Branco', 'humberto.brancho@example.com', 'MALE', 'humberto.brancho', '{bcrypt}$2a$10$e0MYzXyjpJS7Pd0RVvHwHeFx4r1eH1B2d9uTQ9z5e5G8y5FzFQF92', 0),
    (5, N'Artur da Costa e Silva', 'artur.silva@example.com', 'MALE', 'artur.silva', '{bcrypt}$2a$10$e0MYzXyjpJS7Pd0RVvHwHeFx4r1eH1B2d9uTQ9z5e5G8y5FzFQF92', 1),
    (6, N'Emílio Médici', 'emilio.medici@example.com', 'MALE', 'emilio.medici', '{bcrypt}$2a$10$e0MYzXyjpJS7Pd0RVvHwHeFx4r1eH1B2d9uTQ9z5e5G8y5FzFQF92', 0),
    (7, N'Ernesto Geisel', 'ernesto.geisel@example.com', 'MALE', 'ernesto.geisel', '{bcrypt}$2a$10$e0MYzXyjpJS7Pd0RVvHwHeFx4r1eH1B2d9uTQ9z5e5G8y5FzFQF92', 1),
    (8, N'João Figueiredo', 'joao.figueiredo@example.com', 'MALE', 'joao.figueiredo', '{bcrypt}$2a$10$e0MYzXyjpJS7Pd0RVvHwHeFx4r1eH1B2d9uTQ9z5e5G8y5FzFQF92', 0),
    (9, N'José Sarney', 'jose.sarney@example.com', 'MALE', 'jose.sarney', '{bcrypt}$2a$10$e0MYzXyjpJS7Pd0RVvHwHeFx4r1eH1B2d9uTQ9z5e5G8y5FzFQF92', 1),
    (10, N'Fernando Collor de Mello', 'fernando.mello@example.com', 'MALE', 'fernando.mello', '{bcrypt}$2a$10$e0MYzXyjpJS7Pd0RVvHwHeFx4r1eH1B2d9uTQ9z5e5G8y5FzFQF92', 0),
    (11, N'Itamar Franco', 'itamar.franco@example.com', 'MALE', 'itamar.franco', '{bcrypt}$2a$10$e0MYzXyjpJS7Pd0RVvHwHeFx4r1eH1B2d9uTQ9z5e5G8y5FzFQF92', 1),
    (1, N'Fernando Henrique Cardoso', 'fernando.cardoso@example.com', 'MALE', 'fernando.cardoso', '{bcrypt}$2a$10$e0MYzXyjpJS7Pd0RVvHwHeFx4r1eH1B2d9uTQ9z5e5G8y5FzFQF92', 0),
    (2, N'Luiz Inácio Lula da Silva', 'luiz.silva@example.com', 'MALE', 'luiz.silva', '{bcrypt}$2a$10$e0MYzXyjpJS7Pd0RVvHwHeFx4r1eH1B2d9uTQ9z5e5G8y5FzFQF92', 1),
    (3, N'Dilma Rousseff', 'dilma.rousseff@example.com', 'FEMALE', 'dilma.rousseff', '{bcrypt}$2a$10$e0MYzXyjpJS7Pd0RVvHwHeFx4r1eH1B2d9uTQ9z5e5G8y5FzFQF92', 0),
    (4, N'Michel Temer', 'michel.temer@example.com', 'MALE', 'michel.temer', '{bcrypt}$2a$10$e0MYzXyjpJS7Pd0RVvHwHeFx4r1eH1B2d9uTQ9z5e5G8y5FzFQF92', 1),
    (5, N'Jair Bolsonaro', 'jair.bolsonaro@example.com', 'MALE', 'jair.bolsonaro', '{bcrypt}$2a$10$e0MYzXyjpJS7Pd0RVvHwHeFx4r1eH1B2d9uTQ9z5e5G8y5FzFQF92', 0),
    (6, N'Luiz Inácio Lula da Silva', 'luiz.silva2@example.com', 'MALE', 'luiz.silva2', '{bcrypt}$2a$10$e0MYzXyjpJS7Pd0RVvHwHeFx4r1eH1B2d9uTQ9z5e5G8y5FzFQF92', 1);

    INSERT INTO tb_user_role (id_user, id_role)
    SELECT
        usr.id_user,
        rol.id_role
    FROM
        tb_user AS usr

        CROSS APPLY (SELECT id_role FROM tb_role WHERE name LIKE '%_VIEW') AS rol
    WHERE
        usr.username <> 'admin'
END

IF NOT EXISTS(SELECT 1 FROM tb_stakeholder)
BEGIN
    INSERT INTO tb_stakeholder (id_department, name, email, gender) VALUES (1,  N'George Washington'      , 'george.washington@fakedomain.com', 'MALE');
    INSERT INTO tb_stakeholder (id_department, name, email, gender) VALUES (2,  N'John Adams'             , 'john.adams@fakedomain.com', 'MALE');
    INSERT INTO tb_stakeholder (id_department, name, email, gender) VALUES (3,  N'Thomas Jefferson'       , 'thomas.jefferson@fakedomain.com', 'MALE');
    INSERT INTO tb_stakeholder (id_department, name, email, gender) VALUES (4,  N'James Madison'          , 'james.madison@fakedomain.com', 'MALE');
    INSERT INTO tb_stakeholder (id_department, name, email, gender) VALUES (5,  N'James Monroe'           , 'james.monroe@fakedomain.com', 'MALE');
    INSERT INTO tb_stakeholder (id_department, name, email, gender) VALUES (6,  N'John Quincy Adams'      , 'john.quincy.adams@fakedomain.com', 'MALE');
    INSERT INTO tb_stakeholder (id_department, name, email, gender) VALUES (7,  N'Andrew Jackson'         , 'andrew.jackson@fakedomain.com', 'MALE');
    INSERT INTO tb_stakeholder (id_department, name, email, gender) VALUES (8,  N'Martin Van Buren'       , 'martin.van.buren@fakedomain.com', 'MALE');
    INSERT INTO tb_stakeholder (id_department, name, email, gender) VALUES (9,  N'William Henry Harrison' , 'william.henry.harrison@fakedomain.com', 'MALE');
    INSERT INTO tb_stakeholder (id_department, name, email, gender) VALUES (10, N'John Tyler'             , 'john.tyler@fakedomain.com', 'MALE');
    INSERT INTO tb_stakeholder (id_department, name, email, gender) VALUES (11, N'James Polk'             , 'james.polk@fakedomain.com', 'MALE');
    INSERT INTO tb_stakeholder (id_department, name, email, gender) VALUES (1,  N'Zachary Taylor'         , 'zachary.taylor@fakedomain.com', 'MALE');
    INSERT INTO tb_stakeholder (id_department, name, email, gender) VALUES (2,  N'Millard Fillmore'       , 'millard.fillmore@fakedomain.com', 'MALE');
    INSERT INTO tb_stakeholder (id_department, name, email, gender) VALUES (3,  N'Franklin Pierce'        , 'franklin.pierce@fakedomain.com', 'MALE');
    INSERT INTO tb_stakeholder (id_department, name, email, gender) VALUES (4,  N'James Buchanan'         , 'james.buchanan@fakedomain.com', 'MALE');
    INSERT INTO tb_stakeholder (id_department, name, email, gender) VALUES (5,  N'Abraham Lincoln'        , 'abraham.lincoln@fakedomain.com', 'MALE');
    INSERT INTO tb_stakeholder (id_department, name, email, gender) VALUES (6,  N'Andrew Johnson'         , 'andrew.johnson@fakedomain.com', 'MALE');
    INSERT INTO tb_stakeholder (id_department, name, email, gender) VALUES (7,  N'Ulysses Grant'          , 'ulysses.grant@fakedomain.com', 'MALE');
    INSERT INTO tb_stakeholder (id_department, name, email, gender) VALUES (8,  N'Rutherford Hayes'       , 'rutherford.hayes@fakedomain.com', 'MALE');
    INSERT INTO tb_stakeholder (id_department, name, email, gender) VALUES (9,  N'James Garfield'         , 'james.garfield@fakedomain.com', 'MALE');
    INSERT INTO tb_stakeholder (id_department, name, email, gender) VALUES (10, N'Chester Arthur'         , 'chester.arthur@fakedomain.com', 'MALE');
    INSERT INTO tb_stakeholder (id_department, name, email, gender) VALUES (11, N'Grover Cleveland'       , 'grover.cleveland@fakedomain.com', 'MALE');
    INSERT INTO tb_stakeholder (id_department, name, email, gender) VALUES (1,  N'Benjamin Harrison'      , 'benjamin.harrison@fakedomain.com', 'MALE');
    INSERT INTO tb_stakeholder (id_department, name, email, gender) VALUES (2,  N'Grover Cleveland'       , 'grover.cleveland@fakedomain.com', 'MALE');
    INSERT INTO tb_stakeholder (id_department, name, email, gender) VALUES (3,  N'William McKinley'       , 'william.mckinley@fakedomain.com', 'MALE');
    INSERT INTO tb_stakeholder (id_department, name, email, gender) VALUES (4,  N'Theodore Roosevelt'     , 'theodore.roosevelt@fakedomain.com', 'MALE');
    INSERT INTO tb_stakeholder (id_department, name, email, gender) VALUES (5,  N'William Howard Taft'    , 'william.howard.taft@fakedomain.com', 'MALE');
    INSERT INTO tb_stakeholder (id_department, name, email, gender) VALUES (6,  N'Woodrow Wilson'         , 'woodrow.wilson@fakedomain.com', 'MALE');
    INSERT INTO tb_stakeholder (id_department, name, email, gender) VALUES (7,  N'Warren Harding'         , 'warren.harding@fakedomain.com', 'MALE');
    INSERT INTO tb_stakeholder (id_department, name, email, gender) VALUES (8,  N'Calvin Coolidge'        , 'calvin.coolidge@fakedomain.com', 'MALE');
    INSERT INTO tb_stakeholder (id_department, name, email, gender) VALUES (9,  N'Herbert Hoover'         , 'herbert.hoover@fakedomain.com', 'MALE');
    INSERT INTO tb_stakeholder (id_department, name, email, gender) VALUES (10, N'Franklin Roosevelt'     , 'franklin.roosevelt@fakedomain.com', 'MALE');
    INSERT INTO tb_stakeholder (id_department, name, email, gender) VALUES (11, N'Harry Truman'           , 'harry.truman@fakedomain.com', 'MALE');
    INSERT INTO tb_stakeholder (id_department, name, email, gender) VALUES (1,  N'Dwight Eisenhower'      , 'dwight.eisenhower@fakedomain.com', 'MALE');
    INSERT INTO tb_stakeholder (id_department, name, email, gender) VALUES (2,  N'John Kennedy'           , 'john.kennedy@fakedomain.com', 'MALE');
    INSERT INTO tb_stakeholder (id_department, name, email, gender) VALUES (3,  N'Lyndon Johnson'         , 'lyndon.johnson@fakedomain.com', 'MALE');
    INSERT INTO tb_stakeholder (id_department, name, email, gender) VALUES (4,  N'Richard Nixon'          , 'richard.nixon@fakedomain.com', 'MALE');
    INSERT INTO tb_stakeholder (id_department, name, email, gender) VALUES (5,  N'Gerald Ford'            , 'gerald.ford@fakedomain.com', 'MALE');
    INSERT INTO tb_stakeholder (id_department, name, email, gender) VALUES (6,  N'Jimmy Carter'           , 'jimmy.carter@fakedomain.com', 'MALE');
    INSERT INTO tb_stakeholder (id_department, name, email, gender) VALUES (7,  N'Ronald Reagan'          , 'ronald.reagan@fakedomain.com', 'MALE');
    INSERT INTO tb_stakeholder (id_department, name, email, gender) VALUES (8,  N'George Bush'            , 'george.bush@fakedomain.com', 'MALE');
    INSERT INTO tb_stakeholder (id_department, name, email, gender) VALUES (9,  N'Bill Clinton'           , 'bill.clinton@fakedomain.com', 'MALE');
    INSERT INTO tb_stakeholder (id_department, name, email, gender) VALUES (10, N'George Bush'            , 'george.bush@fakedomain.com', 'MALE');
    INSERT INTO tb_stakeholder (id_department, name, email, gender) VALUES (11, N'Barack Obama'           , 'barack.obama@fakedomain.com', 'MALE');
    INSERT INTO tb_stakeholder (id_department, name, email, gender) VALUES (1,  N'Donald Trump'           , 'donald.trump@fakedomain.com', 'MALE');
    INSERT INTO tb_stakeholder (id_department, name, email, gender) VALUES (2,  N'Joe Biden'              , 'joe.biden@fakedomain.com', 'MALE');
    INSERT INTO tb_stakeholder (id_department, name, email, gender) VALUES (3,  N'Kamala Devi Harris'     , 'kamala.devi.harris', 'FEMALE');
END

IF NOT EXISTS(SELECT 1 FROM tb_software)
BEGIN
    INSERT INTO tb_software (code, name) VALUES ('ABR', 'CONTROLE PATRIMONIAL');
    INSERT INTO tb_software (code, name) VALUES ('BAS', 'CONTROLE FINANCEIRO INTEGRADOR');
    INSERT INTO tb_software (code, name) VALUES ('CDZ', 'CADASTRO DE CLIENTES DO SISTEMA FINANCEIRO');
    INSERT INTO tb_software (code, name) VALUES ('CHG', 'CHANGE CAMBIO');
    INSERT INTO tb_software (code, name) VALUES ('CKB', 'CRK BUSINESS PLATFORM');
    INSERT INTO tb_software (code, name) VALUES ('EGU', 'INFOTREASURY');
    INSERT INTO tb_software (code, name) VALUES ('EIP', 'EASY IRPJ');
    INSERT INTO tb_software (code, name) VALUES ('JDC', 'JD CABINE SPB COMPULSORIOS');
    INSERT INTO tb_software (code, name) VALUES ('MEN', 'MENSAGENS CAMBIO');
    INSERT INTO tb_software (code, name) VALUES ('ORJ', 'ORDENS JUDICIAIS');
    INSERT INTO tb_software (code, name) VALUES ('UNE', 'MD COMUNE');
    INSERT INTO tb_software (code, name) VALUES ('ZCO', 'ZAP CONTABIL');

    UPDATE tb_software SET id_owner = 1 WHERE id_software = 1;
    UPDATE tb_software SET id_owner = 2 WHERE id_software = 2;
    UPDATE tb_software SET id_owner = 3 WHERE id_software = 3;
    UPDATE tb_software SET id_owner = 4 WHERE id_software = 4;
    UPDATE tb_software SET id_owner = 5 WHERE id_software = 5;
    UPDATE tb_software SET id_owner = 6 WHERE id_software = 6;
    UPDATE tb_software SET id_owner = 7 WHERE id_software = 7;
    UPDATE tb_software SET id_owner = 8 WHERE id_software = 8;
    UPDATE tb_software SET id_owner = 9 WHERE id_software = 9;
    UPDATE tb_software SET id_owner = 10 WHERE id_software = 10;
    UPDATE tb_software SET id_owner = 11 WHERE id_software = 11;
    UPDATE tb_software SET id_owner = 12 WHERE id_software = 12;
END

IF NOT EXISTS(SELECT 1 FROM tb_version)
BEGIN
    -- Software 1
    INSERT INTO tb_version (id_software, name, file_name, release_date, changelog, comments)
    VALUES
        (1, 'v1.0', 'software1-v1.0.zip', '2023-01-15', 'Initial release', 'First version!'),
        (1, 'v1.1', 'software1-v1.1.zip', '2023-02-20', 'Bug fixes and performance improvements', ''),
        (1, 'v1.2-beta', 'software1-v1.2-beta.zip', '2023-03-10', 'New features: user profiles, improved search', 'Beta release for testing new features');

    -- Software 2
    INSERT INTO tb_version (id_software, name, file_name, release_date, changelog, comments)
    VALUES
        (2, 'v0.9-alpha', 'software2-v0.9-alpha.zip', '2022-12-05', 'Initial alpha release', 'Early access for feedback'),
        (2, 'v1.0', 'software2-v1.0.zip', '2023-04-01', 'Major bug fixes, UI improvements', 'Officially stable!'),
        (2, 'v1.0.1', 'software2-v1.0.1.zip', '2023-04-10', 'Minor bug fixes', '');

    -- Software 3
    INSERT INTO tb_version (id_software, name, file_name, release_date, changelog, comments)
    VALUES
        (3, 'v2.5', 'software3-v2.5.zip', '2023-05-12', 'Security updates, minor UI tweaks', ''),
        (3, 'v2.6-rc1', 'software3-v2.6-rc1.zip', '2023-06-30', 'New reporting features', 'Release candidate 1');

    -- Software 4
    INSERT INTO tb_version (id_software, name, file_name, release_date, changelog, comments)
    VALUES
        (4, 'v1.0', 'software4-v1.0.zip', '2023-03-08', 'Initial release', 'Ready for production use'),
        (4, 'v1.1', 'software4-v1.1.zip', '2023-07-18', 'Improved compatibility, bug fixes', '');

    -- Software 5
    INSERT INTO tb_version (id_software, name, file_name, release_date, changelog, comments)
    VALUES
        (5, 'v0.5-beta', 'software5-v0.5-beta.zip', '2022-11-20', 'Beta testing phase', 'Seeking user feedback'),
        (5, 'v0.6-beta', 'software5-v0.6-beta.zip', '2023-01-02', 'Performance enhancements', ''),
        (5, 'v1.0', 'software5-v1.0.zip', '2023-05-30', 'Stable release, new features', 'First official version');

    -- Software 6
    INSERT INTO tb_version (id_software, name, file_name, release_date, changelog, comments)
    VALUES
        (6, 'v2.0', 'software6-v2.0.zip', '2023-02-14', 'Complete UI overhaul', 'Fresh new look!'),
        (6, 'v2.1', 'software6-v2.1.zip', '2023-06-10', 'Accessibility improvements', '');

    -- Software 7
    INSERT INTO tb_version (id_software, name, file_name, release_date, changelog, comments)
    VALUES
        (7, 'v1.3', 'software7-v1.3.zip', '2023-03-22', 'Bug fixes, stability enhancements', ''),
        (7, 'v1.4', 'software7-v1.4.zip', '2023-08-05', 'New integration options', '');

    -- Software 8
    INSERT INTO tb_version (id_software, name, file_name, release_date, changelog, comments)
    VALUES
        (8, 'v0.1-alpha', 'software8-v0.1-alpha.zip', '2023-01-08', 'Very early alpha', 'Experimental features'),
        (8, 'v0.2-alpha', 'software8-v0.2-alpha.zip', '2023-05-28', 'Core functionality implemented', '');

    -- Software 9
    INSERT INTO tb_version (id_software, name, file_name, release_date, changelog, comments)
    VALUES
        (9, 'v4.5', 'software9-v4.5.zip', '2023-07-03', 'Cloud storage integration', 'Access your files anywhere'),
        (9, 'v4.6', 'software9-v4.6.zip', '2023-09-05', 'Offline mode added', 'Work without internet connection');

    -- Software 10
    INSERT INTO tb_version (id_software, name, file_name, release_date, changelog, comments)
    VALUES
        (10, 'v1.0', 'software10-v1.0.zip', '2023-04-29', 'Initial release', 'Basic features implemented'),
        (10, 'v1.1', 'software10-v1.1.zip', '2023-05-15', 'UI enhancements, bug fixes', '');

    -- Software 11
    INSERT INTO tb_version (id_software, name, file_name, release_date, changelog, comments)
    VALUES
        (11, 'v2.2', 'software11-v2.2.zip', '2023-02-28', 'Improved data visualization', ''),
        (11, 'v2.3', 'software11-v2.3.zip', '2023-08-19', 'Collaboration tools added', 'Work together seamlessly');

    -- Software 12
    INSERT INTO tb_version (id_software, name, file_name, release_date, changelog, comments)
    VALUES
        (12, 'v3.0', 'software12-v3.0.zip', '2023-08-25', 'Major update, new UI, performance boost', 'Big release!'),
        (12, 'v3.0.1', 'software12-v3.0.1.zip', '2023-09-01', 'Critical bug fixes', 'Addressing issues in v3.0');
END

IF OBJECT_ID('tempdb..#tb_last_version') IS NOT NULL
BEGIN
    DROP TABLE #tb_last_version;
END

CREATE TABLE #tb_last_version (
    id_software INT NULL,
    id_version  INT NULL
);

INSERT INTO #tb_last_version
SELECT
    sow.id_software,
    ver.id_version
FROM
    tb_software AS sow

    INNER JOIN tb_version AS ver
            ON ver.id_software  = sow.id_software
           AND ver.release_date = (SELECT MAX(aux.release_date)
                                   FROM tb_version AS aux
                                   WHERE aux.id_software = sow.id_software)

DECLARE @MaxUsers        INT
DECLARE @MaxStakeholders INT

SELECT @MaxUsers        = COUNT(*) FROM tb_user
SELECT @MaxStakeholders = COUNT(*) FROM tb_stakeholder

INSERT INTO tb_deploy (id_version, id_operator, id_authorizer, is_active, environment, rfc, execution_date)
SELECT
    id_version     = ver.id_version,
    id_operator    = ABS(CHECKSUM(NEWID())) % @MaxUsers + 1,
    id_authorizer  = ABS(CHECKSUM(NEWID())) % @MaxStakeholders + 1,
    is_active      = CASE WHEN lvr.id_version = ver.id_version THEN 1 ELSE 0 END,
    environment    = 'DEVELOPMENT',
    rfc            = 'RFC' + RIGHT('000000' + CAST(ABS(CHECKSUM(NEWID())) % 1000000 AS VARCHAR(6)), 6),
    execution_date = DATEADD(DAY, 1, ver.release_date)
FROM
    tb_version AS ver

    LEFT JOIN #tb_last_version AS lvr
           ON lvr.id_software = ver.id_software
ORDER BY
    ver.id_software,
    ver.release_date

INSERT INTO tb_deploy (id_version, id_operator, id_authorizer, is_active, environment, rfc, execution_date)
SELECT
    id_version     = ver.id_version,
    id_operator    = ABS(CHECKSUM(NEWID())) % @MaxUsers + 1,
    id_authorizer  = ABS(CHECKSUM(NEWID())) % @MaxStakeholders + 1,
    is_active      = CASE WHEN lvr.id_version = ver.id_version THEN 1 ELSE 0 END,
    environment    = 'QA',
    rfc            = 'RFC' + RIGHT('000000' + CAST(ABS(CHECKSUM(NEWID())) % 1000000 AS VARCHAR(6)), 6),
    execution_date = DATEADD(DAY, 2, ver.release_date)
FROM
    tb_version AS ver

    LEFT JOIN #tb_last_version AS lvr
           ON lvr.id_software = ver.id_software
ORDER BY
    ver.id_software,
    ver.release_date

INSERT INTO tb_deploy (id_version, id_operator, id_authorizer, is_active, environment, rfc, execution_date)
SELECT
    id_version     = ver.id_version,
    id_operator    = ABS(CHECKSUM(NEWID())) % @MaxUsers + 1,
    id_authorizer  = ABS(CHECKSUM(NEWID())) % @MaxStakeholders + 1,
    is_active      = CASE WHEN lvr.id_version = ver.id_version THEN 1 ELSE 0 END,
    environment    = 'UAT',
    rfc            = 'RFC' + RIGHT('000000' + CAST(ABS(CHECKSUM(NEWID())) % 1000000 AS VARCHAR(6)), 6),
    execution_date = DATEADD(DAY, 3, ver.release_date)
FROM
    tb_version AS ver

    LEFT JOIN #tb_last_version AS lvr
           ON lvr.id_software = ver.id_software
ORDER BY
    ver.id_software,
    ver.release_date

INSERT INTO tb_deploy (id_version, id_operator, id_authorizer, is_active, environment, rfc, execution_date)
SELECT
    id_version     = ver.id_version,
    id_operator    = ABS(CHECKSUM(NEWID())) % @MaxUsers + 1,
    id_authorizer  = ABS(CHECKSUM(NEWID())) % @MaxStakeholders + 1,
    is_active      = CASE WHEN lvr.id_version = ver.id_version THEN 1 ELSE 0 END,
    environment    = 'PRODUCTION',
    rfc            = 'RFC' + RIGHT('000000' + CAST(ABS(CHECKSUM(NEWID())) % 1000000 AS VARCHAR(6)), 6),
    execution_date = DATEADD(DAY, 4, ver.release_date)
FROM
    tb_version AS ver

    LEFT JOIN #tb_last_version AS lvr
           ON lvr.id_software = ver.id_software
ORDER BY
    ver.id_software,
    ver.release_date

INSERT INTO tb_deploy (id_version, id_operator, id_authorizer, is_active, environment, rfc, execution_date)
SELECT
    id_version     = ver.id_version,
    id_operator    = ABS(CHECKSUM(NEWID())) % @MaxUsers + 1,
    id_authorizer  = ABS(CHECKSUM(NEWID())) % @MaxStakeholders + 1,
    is_active      = CASE WHEN lvr.id_version = ver.id_version THEN 1 ELSE 0 END,
    environment    = 'DR',
    rfc            = 'RFC' + RIGHT('000000' + CAST(ABS(CHECKSUM(NEWID())) % 1000000 AS VARCHAR(6)), 6),
    execution_date = DATEADD(DAY, 5, ver.release_date)
FROM
    tb_version AS ver

    LEFT JOIN #tb_last_version AS lvr
           ON lvr.id_software = ver.id_software
ORDER BY
    ver.id_software,
    ver.release_date

IF OBJECT_ID('tempdb..#tb_last_version') IS NOT NULL
BEGIN
    DROP TABLE #tb_last_version;
END