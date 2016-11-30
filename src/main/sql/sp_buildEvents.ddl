DROP PROCEDURE sp_buildEvents;
CREATE PROCEDURE sp_buildEvents
AS
  BEGIN
    TRUNCATE TABLE work_events;

    INSERT INTO work_events (email, secondary)
      SELECT distinct a.EmailAddress, CAST(a.VisualId AS VARCHAR) from Event a
        INNER JOIN MarketoStatus c ON a.EmailAddress = c.key1 AND c.objectType = 'Lead' and c.status = 'CREATED'
        LEFT JOIN MarketoStatus d on a.EmailAddress = d.key1 AND cast(a.VisualId AS VARCHAR) = d.key2 AND d.objectType = 'Event' and d.status = 'CREATED'
      WHERE d.key1 IS NULL AND a.EmailAddress IS NOT NULL AND a.EmailAddress <> '';

    INSERT INTO MarketoStatus (objectType, status, error, key1, key2, lastUpdated)
      SELECT 'Admin', 'SYNC', null, 'Event', count(*), getdate() FROM work_events;
  END
