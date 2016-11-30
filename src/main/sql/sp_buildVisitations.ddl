DROP PROCEDURE sp_buildVisitations;
CREATE PROCEDURE sp_buildVisitations
AS
  BEGIN
    TRUNCATE TABLE work_visitations;
    INSERT INTO work_visitations (email, secondary)
      SELECT DISTINCT a.email, cast(a.usageId as varchar) FROM GalaxyScans a
        INNER JOIN MarketoStatus c ON a.email = c.key1 AND c.objectType = 'Lead' AND c.status = 'CREATED'
        LEFT JOIN MarketoStatus d on a.email = d.key1 AND cast(a.usageId as varchar) = d.key2 AND d.objectType = 'Visitation' AND d.status IN ('CREATED', 'UPDATED')
      WHERE a.email is not null and d.key1 is NULL and LEN(a.email) > 0
            AND email NOT LIKE 'galaxy%calacademy.org';

    INSERT INTO MarketoStatus (objectType, status, error, key1, key2, lastUpdated)
      SELECT 'Admin', 'SYNC', null, 'Visitation', count(*), getdate() FROM work_visitations;
  END;
