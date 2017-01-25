DROP PROCEDURE sp_buildConstituents;
CREATE PROCEDURE sp_buildConstituents
AS
  BEGIN
    TRUNCATE TABLE work_constituents;
    INSERT INTO work_constituents (email, secondary)
      SELECT DISTINCT a.constituentEmailAddress, a.visualId from constituents a
        INNER JOIN MarketoStatus c on c.objectType = 'Lead' AND a.constituentEmailAddress = c.key1 AND c.status = 'CREATED'
        LEFT JOIN MarketoStatus b on a.constituentEmailAddress = b.key1 AND a.visualId = b.key2 AND b.objectType = 'Constituent' AND b.status = 'CREATED'
      WHERE b.key1 IS NULL             AND a.constituentEmailAddress IS NOT NULL AND LEN(a.constituentEmailAddress) > 0;

    INSERT INTO MarketoStatus (objectType, status, error, key1, key2, lastUpdated)
      SELECT 'Admin', 'SYNC', null, 'Constituent', count(*), getdate() FROM work_constituents;
  END;
