CREATE VIEW vw_last_version_deployed
AS
SELECT
    sow.id_software,
    sow.code AS software_code,
    sow.name AS software_name,
    --
    dep.id_deploy,
    dep.environment,
    dep.execution_date,
    dep.rfc,
    --
    ver.name AS [version],
    usr.name AS [operator],
    stk.name AS [authorizer]
FROM
    tb_deploy AS dep

    INNER JOIN tb_version AS ver
               ON ver.id_version = dep.id_version

    INNER JOIN tb_software AS sow
               ON sow.id_software = ver.id_software

    LEFT JOIN tb_user AS usr
              ON usr.id_user = dep.id_operator

    LEFT JOIN tb_stakeholder AS stk
              ON stk.id_stakeholder = dep.id_authorizer
WHERE
    dep.is_active = 1
GO