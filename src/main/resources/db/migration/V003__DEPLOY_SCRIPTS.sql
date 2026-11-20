-- Version packages are no longer uploaded: they are read from a folder per application inside the packages directory.
-- Each application may have a deploy script, run on the server for every deploy; the deploy records its outcome.

ALTER TABLE tb_software ADD COLUMN deploy_script TEXT NULL;
ALTER TABLE tb_software_aud ADD COLUMN deploy_script TEXT NULL;

-- Deploys recorded before this version had no script: they are considered successful.
ALTER TABLE tb_deploy ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT 'SUCCEEDED';
ALTER TABLE tb_deploy ADD COLUMN exit_code INT NULL;
ALTER TABLE tb_deploy ADD COLUMN started_at TIMESTAMP NULL;
ALTER TABLE tb_deploy ADD COLUMN finished_at TIMESTAMP NULL;
ALTER TABLE tb_deploy ADD COLUMN script_output TEXT NULL;
ALTER TABLE tb_deploy ADD COLUMN executor_node VARCHAR(100) NULL;
ALTER TABLE tb_deploy ADD CONSTRAINT ck_deploy_status
    CHECK (status IN ('PENDING', 'RUNNING', 'SUCCEEDED', 'FAILED', 'CANCELLED'));

CREATE INDEX ix_deploy_status ON tb_deploy (status);

-- script_output is not audited (it changes while the script runs)
ALTER TABLE tb_deploy_aud ADD COLUMN status VARCHAR(20) NULL;
ALTER TABLE tb_deploy_aud ADD COLUMN exit_code INT NULL;
ALTER TABLE tb_deploy_aud ADD COLUMN started_at TIMESTAMP NULL;
ALTER TABLE tb_deploy_aud ADD COLUMN finished_at TIMESTAMP NULL;
ALTER TABLE tb_deploy_aud ADD COLUMN executor_node VARCHAR(100) NULL;

-- Editing a deploy script means running commands on the server: it has its own role, granted to the existing
-- administrators (users who manage both applications and users).
INSERT INTO tb_role (name) VALUES ('ROLE_DEPLOY_SCRIPT_MANAGE');

INSERT INTO tb_user_role (id_user, id_role)
SELECT usr.id_user, script_role.id_role
FROM tb_user AS usr
    CROSS JOIN tb_role AS script_role
WHERE script_role.name = 'ROLE_DEPLOY_SCRIPT_MANAGE'
  AND EXISTS (SELECT 1 FROM tb_user_role ur INNER JOIN tb_role r ON r.id_role = ur.id_role
              WHERE ur.id_user = usr.id_user AND r.name = 'ROLE_USER_MANAGE')
  AND EXISTS (SELECT 1 FROM tb_user_role ur INNER JOIN tb_role r ON r.id_role = ur.id_role
              WHERE ur.id_user = usr.id_user AND r.name = 'ROLE_APPLICATION_MANAGE');
