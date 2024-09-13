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
    INSERT INTO tb_role (name) VALUES ('ADMIN');
    INSERT INTO tb_role (name) VALUES ('ITOPS');
    INSERT INTO tb_role (name) VALUES ('OBSERVER');
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
    WHERE
        name = 'ADMIN'

    INSERT INTO tb_user (id_department, name, email, gender, username, password, enabled) VALUES
    (1, 'Deodoro da Fonseca', 'deodoro.fonseca@example.com', 'MALE', 'deodoro.fonseca', '{bcrypt}$2a$10$e0MYzXyjpJS7Pd0RVvHwHeFx4r1eH1B2d9uTQ9z5e5G8y5FzFQF92', 1),
    (2, 'Floriano Peixoto', 'floriano.peixoto@example.com', 'MALE', 'floriano.peixoto', '{bcrypt}$2a$10$e0MYzXyjpJS7Pd0RVvHwHeFx4r1eH1B2d9uTQ9z5e5G8y5FzFQF92', 0),
    (3, 'Prudente de Morais', 'prudente.morais@example.com', 'MALE', 'prudente.morais', '{bcrypt}$2a$10$e0MYzXyjpJS7Pd0RVvHwHeFx4r1eH1B2d9uTQ9z5e5G8y5FzFQF92', 1),
    (4, 'Campos Sales', 'campos.sales@example.com', 'MALE', 'campos.sales', '{bcrypt}$2a$10$e0MYzXyjpJS7Pd0RVvHwHeFx4r1eH1B2d9uTQ9z5e5G8y5FzFQF92', 0),
    (5, 'Rodrigues Alves', 'rodrigues.alves@example.com', 'MALE', 'rodrigues.alves', '{bcrypt}$2a$10$e0MYzXyjpJS7Pd0RVvHwHeFx4r1eH1B2d9uTQ9z5e5G8y5FzFQF92', 1),
    (6, 'Afonso Pena', 'afonso.pena@example.com', 'MALE', 'afonso.pena', '{bcrypt}$2a$10$e0MYzXyjpJS7Pd0RVvHwHeFx4r1eH1B2d9uTQ9z5e5G8y5FzFQF92', 0),
    (7, 'Nilo Peçanha', 'nilo.pecanha@example.com', 'MALE', 'nilo.pecanha', '{bcrypt}$2a$10$e0MYzXyjpJS7Pd0RVvHwHeFx4r1eH1B2d9uTQ9z5e5G8y5FzFQF92', 1),
    (8, 'Hermes da Fonseca', 'hermes.fonseca@example.com', 'MALE', 'hermes.fonseca', '{bcrypt}$2a$10$e0MYzXyjpJS7Pd0RVvHwHeFx4r1eH1B2d9uTQ9z5e5G8y5FzFQF92', 0),
    (9, 'Venceslau Brás', 'venceslau.bras@example.com', 'MALE', 'venceslau.bras', '{bcrypt}$2a$10$e0MYzXyjpJS7Pd0RVvHwHeFx4r1eH1B2d9uTQ9z5e5G8y5FzFQF92', 1),
    (10, 'Delfim Moreira', 'delfim.moreira@example.com', 'MALE', 'delfim.moreira', '{bcrypt}$2a$10$e0MYzXyjpJS7Pd0RVvHwHeFx4r1eH1B2d9uTQ9z5e5G8y5FzFQF92', 0),
    (11, 'Epitácio Pessoa', 'epitacio.pessoa@example.com', 'MALE', 'epitacio.pessoa', '{bcrypt}$2a$10$e0MYzXyjpJS7Pd0RVvHwHeFx4r1eH1B2d9uTQ9z5e5G8y5FzFQF92', 1),
    (1, 'Artur Bernardes', 'artur.bernardes@example.com', 'MALE', 'artur.bernardes', '{bcrypt}$2a$10$e0MYzXyjpJS7Pd0RVvHwHeFx4r1eH1B2d9uTQ9z5e5G8y5FzFQF92', 0),
    (2, 'Washington Luís', 'washington.luis@example.com', 'MALE', 'washington.luis', '{bcrypt}$2a$10$e0MYzXyjpJS7Pd0RVvHwHeFx4r1eH1B2d9uTQ9z5e5G8y5FzFQF92', 1),
    (3, 'Getúlio Vargas', 'getulio.vargas@example.com', 'MALE', 'getulio.vargas', '{bcrypt}$2a$10$e0MYzXyjpJS7Pd0RVvHwHeFx4r1eH1B2d9uTQ9z5e5G8y5FzFQF92', 0),
    (4, 'José Linhares', 'jose.linhares@example.com', 'MALE', 'jose.linhares', '{bcrypt}$2a$10$e0MYzXyjpJS7Pd0RVvHwHeFx4r1eH1B2d9uTQ9z5e5G8y5FzFQF92', 1),
    (5, 'Eurico Gaspar Dutra', 'eurico.dutra@example.com', 'MALE', 'eurico.dutra', '{bcrypt}$2a$10$e0MYzXyjpJS7Pd0RVvHwHeFx4r1eH1B2d9uTQ9z5e5G8y5FzFQF92', 0),
    (6, 'Getúlio Vargas', 'getulio.vargas2@example.com', 'MALE', 'getulio.vargas2', '{bcrypt}$2a$10$e0MYzXyjpJS7Pd0RVvHwHeFx4r1eH1B2d9uTQ9z5e5G8y5FzFQF92', 1),
    (7, 'Café Filho', 'cafe.filho@example.com', 'MALE', 'cafe.filho', '{bcrypt}$2a$10$e0MYzXyjpJS7Pd0RVvHwHeFx4r1eH1B2d9uTQ9z5e5G8y5FzFQF92', 0),
    (8, 'Carlos Luz', 'carlos.luz@example.com', 'MALE', 'carlos.luz', '{bcrypt}$2a$10$e0MYzXyjpJS7Pd0RVvHwHeFx4r1eH1B2d9uTQ9z5e5G8y5FzFQF92', 1),
    (9, 'Nereu Ramos', 'nereu.ramos@example.com', 'MALE', 'nereu.ramos', '{bcrypt}$2a$10$e0MYzXyjpJS7Pd0RVvHwHeFx4r1eH1B2d9uTQ9z5e5G8y5FzFQF92', 0),
    (10, 'Juscelino Kubitschek', 'juscelino.kubitschek@example.com', 'MALE', 'juscelino.kubitschek', '{bcrypt}$2a$10$e0MYzXyjpJS7Pd0RVvHwHeFx4r1eH1B2d9uTQ9z5e5G8y5FzFQF92', 1),
    (11, 'Jânio Quadros', 'janio.quadros@example.com', 'MALE', 'janio.quadros', '{bcrypt}$2a$10$e0MYzXyjpJS7Pd0RVvHwHeFx4r1eH1B2d9uTQ9z5e5G8y5FzFQF92', 0),
    (1, 'Ranieri Mazzilli', 'ranieri.mazzilli@example.com', 'MALE', 'ranieri.mazzilli', '{bcrypt}$2a$10$e0MYzXyjpJS7Pd0RVvHwHeFx4r1eH1B2d9uTQ9z5e5G8y5FzFQF92', 1),
    (2, 'João Goulart', 'joao.goulart@example.com', 'MALE', 'joao.goulart', '{bcrypt}$2a$10$e0MYzXyjpJS7Pd0RVvHwHeFx4r1eH1B2d9uTQ9z5e5G8y5FzFQF92', 0),
    (3, 'Ranieri Mazzilli', 'ranieri.mazzilli2@example.com', 'MALE', 'ranieri.mazzilli2', '{bcrypt}$2a$10$e0MYzXyjpJS7Pd0RVvHwHeFx4r1eH1B2d9uTQ9z5e5G8y5FzFQF92', 1),
    (4, 'Humberto Castelo Branco', 'humberto.brancho@example.com', 'MALE', 'humberto.brancho', '{bcrypt}$2a$10$e0MYzXyjpJS7Pd0RVvHwHeFx4r1eH1B2d9uTQ9z5e5G8y5FzFQF92', 0),
    (5, 'Artur da Costa e Silva', 'artur.silva@example.com', 'MALE', 'artur.silva', '{bcrypt}$2a$10$e0MYzXyjpJS7Pd0RVvHwHeFx4r1eH1B2d9uTQ9z5e5G8y5FzFQF92', 1),
    (6, 'Emílio Médici', 'emilio.medici@example.com', 'MALE', 'emilio.medici', '{bcrypt}$2a$10$e0MYzXyjpJS7Pd0RVvHwHeFx4r1eH1B2d9uTQ9z5e5G8y5FzFQF92', 0),
    (7, 'Ernesto Geisel', 'ernesto.geisel@example.com', 'MALE', 'ernesto.geisel', '{bcrypt}$2a$10$e0MYzXyjpJS7Pd0RVvHwHeFx4r1eH1B2d9uTQ9z5e5G8y5FzFQF92', 1),
    (8, 'João Figueiredo', 'joao.figueiredo@example.com', 'MALE', 'joao.figueiredo', '{bcrypt}$2a$10$e0MYzXyjpJS7Pd0RVvHwHeFx4r1eH1B2d9uTQ9z5e5G8y5FzFQF92', 0),
    (9, 'José Sarney', 'jose.sarney@example.com', 'MALE', 'jose.sarney', '{bcrypt}$2a$10$e0MYzXyjpJS7Pd0RVvHwHeFx4r1eH1B2d9uTQ9z5e5G8y5FzFQF92', 1),
    (10, 'Fernando Collor de Mello', 'fernando.mello@example.com', 'MALE', 'fernando.mello', '{bcrypt}$2a$10$e0MYzXyjpJS7Pd0RVvHwHeFx4r1eH1B2d9uTQ9z5e5G8y5FzFQF92', 0),
    (11, 'Itamar Franco', 'itamar.franco@example.com', 'MALE', 'itamar.franco', '{bcrypt}$2a$10$e0MYzXyjpJS7Pd0RVvHwHeFx4r1eH1B2d9uTQ9z5e5G8y5FzFQF92', 1),
    (1, 'Fernando Henrique Cardoso', 'fernando.cardoso@example.com', 'MALE', 'fernando.cardoso', '{bcrypt}$2a$10$e0MYzXyjpJS7Pd0RVvHwHeFx4r1eH1B2d9uTQ9z5e5G8y5FzFQF92', 0),
    (2, 'Luiz Inácio Lula da Silva', 'luiz.silva@example.com', 'MALE', 'luiz.silva', '{bcrypt}$2a$10$e0MYzXyjpJS7Pd0RVvHwHeFx4r1eH1B2d9uTQ9z5e5G8y5FzFQF92', 1),
    (3, 'Dilma Rousseff', 'dilma.rousseff@example.com', 'FEMALE', 'dilma.rousseff', '{bcrypt}$2a$10$e0MYzXyjpJS7Pd0RVvHwHeFx4r1eH1B2d9uTQ9z5e5G8y5FzFQF92', 0),
    (4, 'Michel Temer', 'michel.temer@example.com', 'MALE', 'michel.temer', '{bcrypt}$2a$10$e0MYzXyjpJS7Pd0RVvHwHeFx4r1eH1B2d9uTQ9z5e5G8y5FzFQF92', 1),
    (5, 'Jair Bolsonaro', 'jair.bolsonaro@example.com', 'MALE', 'jair.bolsonaro', '{bcrypt}$2a$10$e0MYzXyjpJS7Pd0RVvHwHeFx4r1eH1B2d9uTQ9z5e5G8y5FzFQF92', 0),
    (6, 'Luiz Inácio Lula da Silva', 'luiz.silva2@example.com', 'MALE', 'luiz.silva2', '{bcrypt}$2a$10$e0MYzXyjpJS7Pd0RVvHwHeFx4r1eH1B2d9uTQ9z5e5G8y5FzFQF92', 1);

    INSERT INTO tb_user_role (id_user, id_role)
    SELECT
        id_user,
        (SELECT id_role FROM tb_role WHERE name = 'OBSERVER')
    FROM
        tb_user
    WHERE
        username <> 'admin'
END

IF NOT EXISTS(SELECT 1 FROM tb_stakeholder)
BEGIN
    INSERT INTO tb_stakeholder (id_department, name, email, gender) VALUES (1,  'George Washington'      , 'george.washington@fakedomain.com', 'MALE');
    INSERT INTO tb_stakeholder (id_department, name, email, gender) VALUES (2,  'John Adams'             , 'john.adams@fakedomain.com', 'MALE');
    INSERT INTO tb_stakeholder (id_department, name, email, gender) VALUES (3,  'Thomas Jefferson'       , 'thomas.jefferson@fakedomain.com', 'MALE');
    INSERT INTO tb_stakeholder (id_department, name, email, gender) VALUES (4,  'James Madison'          , 'james.madison@fakedomain.com', 'MALE');
    INSERT INTO tb_stakeholder (id_department, name, email, gender) VALUES (5,  'James Monroe'           , 'james.monroe@fakedomain.com', 'MALE');
    INSERT INTO tb_stakeholder (id_department, name, email, gender) VALUES (6,  'John Quincy Adams'      , 'john.quincy.adams@fakedomain.com', 'MALE');
    INSERT INTO tb_stakeholder (id_department, name, email, gender) VALUES (7,  'Andrew Jackson'         , 'andrew.jackson@fakedomain.com', 'MALE');
    INSERT INTO tb_stakeholder (id_department, name, email, gender) VALUES (8,  'Martin Van Buren'       , 'martin.van.buren@fakedomain.com', 'MALE');
    INSERT INTO tb_stakeholder (id_department, name, email, gender) VALUES (9,  'William Henry Harrison' , 'william.henry.harrison@fakedomain.com', 'MALE');
    INSERT INTO tb_stakeholder (id_department, name, email, gender) VALUES (10, 'John Tyler'             , 'john.tyler@fakedomain.com', 'MALE');
    INSERT INTO tb_stakeholder (id_department, name, email, gender) VALUES (11, 'James Polk'             , 'james.polk@fakedomain.com', 'MALE');
    INSERT INTO tb_stakeholder (id_department, name, email, gender) VALUES (1,  'Zachary Taylor'         , 'zachary.taylor@fakedomain.com', 'MALE');
    INSERT INTO tb_stakeholder (id_department, name, email, gender) VALUES (2,  'Millard Fillmore'       , 'millard.fillmore@fakedomain.com', 'MALE');
    INSERT INTO tb_stakeholder (id_department, name, email, gender) VALUES (3,  'Franklin Pierce'        , 'franklin.pierce@fakedomain.com', 'MALE');
    INSERT INTO tb_stakeholder (id_department, name, email, gender) VALUES (4,  'James Buchanan'         , 'james.buchanan@fakedomain.com', 'MALE');
    INSERT INTO tb_stakeholder (id_department, name, email, gender) VALUES (5,  'Abraham Lincoln'        , 'abraham.lincoln@fakedomain.com', 'MALE');
    INSERT INTO tb_stakeholder (id_department, name, email, gender) VALUES (6,  'Andrew Johnson'         , 'andrew.johnson@fakedomain.com', 'MALE');
    INSERT INTO tb_stakeholder (id_department, name, email, gender) VALUES (7,  'Ulysses Grant'          , 'ulysses.grant@fakedomain.com', 'MALE');
    INSERT INTO tb_stakeholder (id_department, name, email, gender) VALUES (8,  'Rutherford Hayes'       , 'rutherford.hayes@fakedomain.com', 'MALE');
    INSERT INTO tb_stakeholder (id_department, name, email, gender) VALUES (9,  'James Garfield'         , 'james.garfield@fakedomain.com', 'MALE');
    INSERT INTO tb_stakeholder (id_department, name, email, gender) VALUES (10, 'Chester Arthur'         , 'chester.arthur@fakedomain.com', 'MALE');
    INSERT INTO tb_stakeholder (id_department, name, email, gender) VALUES (11, 'Grover Cleveland'       , 'grover.cleveland@fakedomain.com', 'MALE');
    INSERT INTO tb_stakeholder (id_department, name, email, gender) VALUES (1,  'Benjamin Harrison'      , 'benjamin.harrison@fakedomain.com', 'MALE');
    INSERT INTO tb_stakeholder (id_department, name, email, gender) VALUES (2,  'Grover Cleveland'       , 'grover.cleveland@fakedomain.com', 'MALE');
    INSERT INTO tb_stakeholder (id_department, name, email, gender) VALUES (3,  'William McKinley'       , 'william.mckinley@fakedomain.com', 'MALE');
    INSERT INTO tb_stakeholder (id_department, name, email, gender) VALUES (4,  'Theodore Roosevelt'     , 'theodore.roosevelt@fakedomain.com', 'MALE');
    INSERT INTO tb_stakeholder (id_department, name, email, gender) VALUES (5,  'William Howard Taft'    , 'william.howard.taft@fakedomain.com', 'MALE');
    INSERT INTO tb_stakeholder (id_department, name, email, gender) VALUES (6,  'Woodrow Wilson'         , 'woodrow.wilson@fakedomain.com', 'MALE');
    INSERT INTO tb_stakeholder (id_department, name, email, gender) VALUES (7,  'Warren Harding'         , 'warren.harding@fakedomain.com', 'MALE');
    INSERT INTO tb_stakeholder (id_department, name, email, gender) VALUES (8,  'Calvin Coolidge'        , 'calvin.coolidge@fakedomain.com', 'MALE');
    INSERT INTO tb_stakeholder (id_department, name, email, gender) VALUES (9,  'Herbert Hoover'         , 'herbert.hoover@fakedomain.com', 'MALE');
    INSERT INTO tb_stakeholder (id_department, name, email, gender) VALUES (10, 'Franklin Roosevelt'     , 'franklin.roosevelt@fakedomain.com', 'MALE');
    INSERT INTO tb_stakeholder (id_department, name, email, gender) VALUES (11, 'Harry Truman'           , 'harry.truman@fakedomain.com', 'MALE');
    INSERT INTO tb_stakeholder (id_department, name, email, gender) VALUES (1,  'Dwight Eisenhower'      , 'dwight.eisenhower@fakedomain.com', 'MALE');
    INSERT INTO tb_stakeholder (id_department, name, email, gender) VALUES (2,  'John Kennedy'           , 'john.kennedy@fakedomain.com', 'MALE');
    INSERT INTO tb_stakeholder (id_department, name, email, gender) VALUES (3,  'Lyndon Johnson'         , 'lyndon.johnson@fakedomain.com', 'MALE');
    INSERT INTO tb_stakeholder (id_department, name, email, gender) VALUES (4,  'Richard Nixon'          , 'richard.nixon@fakedomain.com', 'MALE');
    INSERT INTO tb_stakeholder (id_department, name, email, gender) VALUES (5,  'Gerald Ford'            , 'gerald.ford@fakedomain.com', 'MALE');
    INSERT INTO tb_stakeholder (id_department, name, email, gender) VALUES (6,  'Jimmy Carter'           , 'jimmy.carter@fakedomain.com', 'MALE');
    INSERT INTO tb_stakeholder (id_department, name, email, gender) VALUES (7,  'Ronald Reagan'          , 'ronald.reagan@fakedomain.com', 'MALE');
    INSERT INTO tb_stakeholder (id_department, name, email, gender) VALUES (8,  'George Bush'            , 'george.bush@fakedomain.com', 'MALE');
    INSERT INTO tb_stakeholder (id_department, name, email, gender) VALUES (9,  'Bill Clinton'           , 'bill.clinton@fakedomain.com', 'MALE');
    INSERT INTO tb_stakeholder (id_department, name, email, gender) VALUES (10, 'George Bush'            , 'george.bush@fakedomain.com', 'MALE');
    INSERT INTO tb_stakeholder (id_department, name, email, gender) VALUES (11, 'Barack Obama'           , 'barack.obama@fakedomain.com', 'MALE');
    INSERT INTO tb_stakeholder (id_department, name, email, gender) VALUES (1,  'Donald Trump'           , 'donald.trump@fakedomain.com', 'MALE');
    INSERT INTO tb_stakeholder (id_department, name, email, gender) VALUES (2,  'Joe Biden'              , 'joe.biden@fakedomain.com', 'MALE');
    INSERT INTO tb_stakeholder (id_department, name, email, gender) VALUES (3,  'Kamala Devi Harris'     , 'kamala.devi.harris', 'FEMALE');
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