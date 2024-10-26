/*
-- DROP TABLES --
DROP TABLE flyway_schema_history;
DROP TABLE tb_deploy;
DROP TABLE tb_version;
DROP TABLE tb_software;
DROP TABLE tb_stakeholder;
DROP TABLE tb_user_role;
DROP TABLE tb_role;
DROP TABLE tb_user;
DROP TABLE tb_department;
DROP TABLE tb_deploy_aud;
DROP TABLE tb_version_aud;
DROP TABLE tb_software_aud;
DROP TABLE tb_stakeholder_aud;
DROP TABLE tb_user_role_aud;
DROP TABLE tb_role_aud;
DROP TABLE tb_user_aud;
DROP TABLE tb_department_aud;
DROP TABLE SPRING_SESSION_ATTRIBUTES;
DROP TABLE SPRING_SESSION;
DROP TABLE tb_revision;
DROP TABLE tb_revision_type;
DROP VIEW vw_last_version_deployed;
*/

IF OBJECT_ID('SPRING_SESSION', 'U') IS NULL
BEGIN
    CREATE TABLE SPRING_SESSION (
        PRIMARY_ID            CHAR(36)     NOT NULL,
        SESSION_ID            CHAR(36)     NOT NULL,
        CREATION_TIME         BIGINT       NOT NULL,
        LAST_ACCESS_TIME      BIGINT       NOT NULL,
        MAX_INACTIVE_INTERVAL INT          NOT NULL,
        EXPIRY_TIME           BIGINT       NOT NULL,
        PRINCIPAL_NAME        VARCHAR(100) NULL,

        CONSTRAINT SPRING_SESSION_PK PRIMARY KEY CLUSTERED (PRIMARY_ID)
    );

    CREATE INDEX SPRING_SESSION_IX2 ON SPRING_SESSION (EXPIRY_TIME);
    CREATE INDEX SPRING_SESSION_IX3 ON SPRING_SESSION (PRINCIPAL_NAME);
END;

IF OBJECT_ID('SPRING_SESSION_ATTRIBUTES', 'U') IS NULL
CREATE TABLE SPRING_SESSION_ATTRIBUTES (
    SESSION_PRIMARY_ID CHAR(36)       NOT NULL,
    ATTRIBUTE_NAME     VARCHAR(200)   NOT NULL,
    ATTRIBUTE_BYTES    VARBINARY(MAX) NOT NULL,

    CONSTRAINT SPRING_SESSION_ATTRIBUTES_PK PRIMARY KEY CLUSTERED (SESSION_PRIMARY_ID, ATTRIBUTE_NAME),
    CONSTRAINT SPRING_SESSION_ATTRIBUTES_FK FOREIGN KEY(SESSION_PRIMARY_ID) REFERENCES SPRING_SESSION (PRIMARY_ID)
        ON DELETE CASCADE
);

IF OBJECT_ID('tb_revision', 'U') IS NULL
BEGIN
    CREATE TABLE tb_revision (
        id_revision     INT       NOT NULL IDENTITY(1, 1),
        id_user         INT       NULL,
        event_timestamp DATETIME2 NOT NULL,
        CONSTRAINT pk_revision PRIMARY KEY (id_revision)
    );
END

IF OBJECT_ID('tb_revision_type', 'U') IS NULL
BEGIN
    CREATE TABLE tb_revision_type (
        id_revision_type TINYINT     NOT NULL,
        description      VARCHAR(50) NOT NULL,
        CONSTRAINT pk_revision_type PRIMARY KEY (id_revision_type)
    );

    INSERT INTO tb_revision_type (id_revision_type, description) VALUES
    (0, 'ADD'),
    (1, 'MOD'),
    (2, 'DEL');
END

IF OBJECT_ID('tb_department', 'U') IS NULL
BEGIN
    CREATE TABLE tb_department (
        id_department INT          NOT NULL IDENTITY(1, 1),
        name          VARCHAR(255) NOT NULL,
        email         VARCHAR(255) NULL,
        created_at    DATETIME2    NOT NULL DEFAULT(SYSDATETIME()),
        updated_at    DATETIME2    NULL,

        CONSTRAINT pk_department PRIMARY KEY CLUSTERED (id_department),
        CONSTRAINT qa_department_name UNIQUE (name)
    );

    CREATE TABLE tb_department_aud (
        id_department    INT           NOT NULL,
        id_revision      INT           NOT NULL,
        id_revision_type TINYINT       NULL,
        name             VARCHAR(255)  NULL,
        email            VARCHAR(255)  NULL,
        created_at       DATETIME2     NULL,
        updated_at       DATETIME2     NULL,
        PRIMARY KEY (id_department, id_revision),
        CONSTRAINT fk_department_aud_revision FOREIGN KEY (id_revision) REFERENCES tb_revision (id_revision),
        CONSTRAINT fk_department_aud_revision_type FOREIGN KEY (id_revision_type) REFERENCES tb_revision_type (id_revision_type)
    );
END

IF OBJECT_ID('tb_user', 'U') IS NULL
BEGIN
    CREATE TABLE tb_user (
        id_user       INT          NOT NULL IDENTITY(1, 1),
        id_department INT          NOT NULL,
        name          VARCHAR(255) NOT NULL,
        email         VARCHAR(255) NULL,
        gender        VARCHAR(10)  NOT NULL,
        username      VARCHAR(50)  NOT NULL,
        password      VARCHAR(255) NOT NULL,
        enabled       BIT          NOT NULL DEFAULT(1),
        created_at    DATETIME2    NOT NULL DEFAULT(SYSDATETIME()),
        updated_at    DATETIME2    NULL,

        CONSTRAINT pk_user            PRIMARY KEY NONCLUSTERED (id_user),
        CONSTRAINT uq_user_username   UNIQUE CLUSTERED (username),
        CONSTRAINT fk_user_department FOREIGN KEY (id_department) REFERENCES tb_department (id_department)
    );

    CREATE TABLE tb_user_aud (
        id_user          INT          NOT NULL,
        id_revision      INT          NOT NULL,
        id_revision_type TINYINT      NULL,
        id_department    INT          NULL,
        name             VARCHAR(255) NULL,
        email            VARCHAR(255) NULL,
        gender           VARCHAR(10)  NULL,
        username         VARCHAR(50)  NULL,
        password         VARCHAR(255) NULL,
        enabled          BIT          NULL,
        created_at       DATETIME2    NULL,
        updated_at       DATETIME2    NULL,
        PRIMARY KEY (id_user, id_revision),
        CONSTRAINT fk_user_aud_revision FOREIGN KEY (id_revision) REFERENCES tb_revision (id_revision),
        CONSTRAINT fk_user_aud_revision_type FOREIGN KEY (id_revision_type) REFERENCES tb_revision_type (id_revision_type)
    );
END

IF OBJECT_ID('tb_role', 'U') IS NULL
BEGIN
    CREATE TABLE tb_role (
        id_role    INT          NOT NULL IDENTITY(1, 1),
        name       VARCHAR(255) NOT NULL,
        created_at DATETIME2    NOT NULL DEFAULT(SYSDATETIME()),
        updated_at DATETIME2    NULL,

        CONSTRAINT pk_role PRIMARY KEY CLUSTERED (id_role),
        CONSTRAINT uq_role_name UNIQUE (name)
    );

    CREATE TABLE tb_role_aud (
        id_role          INT          NOT NULL,
        id_revision      INT          NOT NULL,
        id_revision_type TINYINT      NULL,
        name             VARCHAR(255) NULL,
        created_at       DATETIME2    NULL,
        updated_at       DATETIME2    NULL,
        PRIMARY KEY (id_role, id_revision),
        CONSTRAINT fk_role_aud_revision FOREIGN KEY (id_revision) REFERENCES tb_revision (id_revision),
        CONSTRAINT fk_role_aud_revision_type FOREIGN KEY (id_revision_type) REFERENCES tb_revision_type (id_revision_type)
    );
END

IF OBJECT_ID('tb_user_role', 'U') IS NULL
BEGIN
    CREATE TABLE tb_user_role (
        id_user INT NOT NULL,
        id_role INT NOT NULL,

        CONSTRAINT pk_user_role PRIMARY KEY (id_user, id_role),
        CONSTRAINT fk_user_role_user FOREIGN KEY (id_user) REFERENCES tb_user (id_user)
            ON UPDATE CASCADE
            ON DELETE CASCADE,
        CONSTRAINT fk_user_role_role FOREIGN KEY (id_role) REFERENCES tb_role (id_role)
            ON UPDATE CASCADE
            ON DELETE CASCADE
    );

    CREATE TABLE tb_user_role_aud (
        id_user          INT     NOT NULL,
        id_role          INT     NOT NULL,
        id_revision      INT     NOT NULL,
        id_revision_type TINYINT NULL,
        PRIMARY KEY (id_user, id_role, id_revision),
        CONSTRAINT fk_user_role_aud_revision FOREIGN KEY (id_revision) REFERENCES tb_revision (id_revision),
        CONSTRAINT fk_user_role_aud_revision_type FOREIGN KEY (id_revision_type) REFERENCES tb_revision_type (id_revision_type)
    );
END

IF OBJECT_ID('tb_stakeholder', 'U') IS NULL
BEGIN
    CREATE TABLE tb_stakeholder (
        id_stakeholder INT          NOT NULL IDENTITY(1, 1),
        id_department  INT          NOT NULL,
        name           VARCHAR(255) NOT NULL,
        email          VARCHAR(255) NULL,
        gender         VARCHAR(10)  NOT NULL,
        created_at     DATETIME2    NOT NULL DEFAULT(SYSDATETIME()),
        updated_at     DATETIME2    NULL,

        CONSTRAINT pk_stakeholder PRIMARY KEY (id_stakeholder),
        CONSTRAINT fk_stakeholder_department FOREIGN KEY (id_department) REFERENCES tb_department (id_department)
    );

    CREATE TABLE tb_stakeholder_aud (
        id_stakeholder   INT          NOT NULL,
        id_revision      INT          NOT NULL,
        id_revision_type TINYINT      NULL,
        id_department    INT          NULL,
        name             VARCHAR(255) NULL,
        email            VARCHAR(255) NULL,
        gender           VARCHAR(10)  NULL,
        created_at       DATETIME2    NULL,
        updated_at       DATETIME2    NULL,
        PRIMARY KEY (id_stakeholder, id_revision),
        CONSTRAINT fk_stakeholder_aud_revision FOREIGN KEY (id_revision) REFERENCES tb_revision (id_revision),
        CONSTRAINT fk_stakeholder_aud_revision_type FOREIGN KEY (id_revision_type) REFERENCES tb_revision_type (id_revision_type)
    );
END

IF OBJECT_ID('tb_software', 'U') IS NULL
BEGIN
    CREATE TABLE tb_software (
        id_software INT          NOT NULL IDENTITY(1, 1),
        id_owner    INT          NULL,
        code        VARCHAR(20)  NOT NULL,
        name        VARCHAR(255) NOT NULL,
        created_at  DATETIME2    NOT NULL DEFAULT(SYSDATETIME()),
        updated_at  DATETIME2    NULL,

        CONSTRAINT pk_software       PRIMARY KEY CLUSTERED (id_software),
        CONSTRAINT uq_software_code  UNIQUE (code),
        CONSTRAINT uq_software_name  UNIQUE (name),
        CONSTRAINT fk_software_owner FOREIGN KEY (id_owner) REFERENCES tb_stakeholder (id_stakeholder),
    );

    CREATE TABLE tb_software_aud (
        id_software      INT          NOT NULL,
        id_revision      INT          NOT NULL,
        id_revision_type TINYINT      NULL,
        id_owner         INT          NULL,
        code             VARCHAR(20)  NULL,
        name             VARCHAR(255) NULL,
        created_at       DATETIME2    NULL,
        updated_at       DATETIME2    NULL,
        PRIMARY KEY (id_software, id_revision),
        CONSTRAINT fk_software_aud_revision FOREIGN KEY (id_revision) REFERENCES tb_revision (id_revision),
        CONSTRAINT fk_software_aud_revision_type FOREIGN KEY (id_revision_type) REFERENCES tb_revision_type (id_revision_type)
    );
END

IF OBJECT_ID('tb_version', 'U') IS NULL
BEGIN
    CREATE TABLE tb_version (
        id_version   INT          NOT NULL IDENTITY(1, 1),
        id_software  INT          NOT NULL,
        name         VARCHAR(255) NOT NULL,
        file_name    VARCHAR(255) NOT NULL,
        release_date DATE         NOT NULL DEFAULT(GETDATE()),
        changelog    VARCHAR(MAX) NULL,
        comments     VARCHAR(MAX) NULL,
        created_at   DATETIME2    NOT NULL DEFAULT(SYSDATETIME()),
        updated_at   DATETIME2    NULL,

        CONSTRAINT pk_version PRIMARY KEY (id_version),
        CONSTRAINT fk_version_software FOREIGN KEY (id_software) REFERENCES tb_software (id_software)
    );

    CREATE TABLE tb_version_aud (
        id_version       INT           NOT NULL,
        id_revision      INT           NOT NULL,
        id_revision_type TINYINT       NULL,
        id_software      INT           NULL,
        name             VARCHAR(255)  NULL,
        file_name        VARCHAR(255)  NULL,
        release_date     DATE          NULL,
        changelog        VARCHAR(MAX)  NULL,
        comments         VARCHAR(MAX)  NULL,
        created_at       DATETIME2     NULL,
        updated_at       DATETIME2     NULL,
        PRIMARY KEY (id_version, id_revision),
        CONSTRAINT fk_version_aud_revision FOREIGN KEY (id_revision) REFERENCES tb_revision (id_revision),
        CONSTRAINT fk_version_aud_revision_type FOREIGN KEY (id_revision_type) REFERENCES tb_revision_type (id_revision_type)
    );
END

IF OBJECT_ID('tb_deploy', 'U') IS NULL
BEGIN
    CREATE TABLE tb_deploy (
        id_deploy      INT          NOT NULL IDENTITY(1, 1),
        id_version     INT          NOT NULL,
        id_operator    INT          NOT NULL,
        id_authorizer  INT          NOT NULL,
        is_active      BIT          NOT NULL DEFAULT(0),
        environment    VARCHAR(30)  NOT NULL,
        rfc            VARCHAR(30)  NOT NULL,
        notes          VARCHAR(max) NULL,
        execution_date DATE         NOT NULL DEFAULT (GETDATE()),
        created_at     DATETIME2    NOT NULL DEFAULT(SYSDATETIME()),
        updated_at     DATETIME2    NULL,

        CONSTRAINT pk_deploy            PRIMARY KEY (id_deploy),
        CONSTRAINT uq_deploy_rfc        UNIQUE (rfc),
        CONSTRAINT fk_deploy_operator   FOREIGN KEY (id_operator)   REFERENCES tb_user        (id_user),
        CONSTRAINT fk_deploy_version    FOREIGN KEY (id_version)    REFERENCES tb_version     (id_version),
        CONSTRAINT fk_deploy_authorizer FOREIGN KEY (id_authorizer) REFERENCES tb_stakeholder (id_stakeholder),
        CONSTRAINT ck_deploy_environment CHECK (environment IN ('DEVELOPMENT', 'QA', 'UAT', 'PRODUCTION', 'DR'))
    );
    CREATE UNIQUE INDEX uq_deploy_environment_active ON tb_deploy (id_version, environment, is_active) WHERE is_active = 1;

    CREATE TABLE tb_deploy_aud (
        id_deploy        INT         NOT NULL,
        id_revision      INT         NOT NULL,
        id_revision_type TINYINT     NULL,
        id_version       INT         NULL,
        id_operator      INT         NULL,
        id_authorizer    INT         NULL,
        is_active        BIT         NULL,
        environment      VARCHAR(30) NULL,
        rfc              VARCHAR(30) NULL,
        notes           VARCHAR(max) NULL,
        execution_date   DATE        NULL,
        created_at       DATETIME2   NULL,
        updated_at       DATETIME2   NULL,
        PRIMARY KEY (id_deploy, id_revision),
        CONSTRAINT fk_deploy_aud_revision FOREIGN KEY (id_revision) REFERENCES tb_revision (id_revision),
        CONSTRAINT fk_deploy_aud_revision_type FOREIGN KEY (id_revision_type) REFERENCES tb_revision_type (id_revision_type)
    );
END