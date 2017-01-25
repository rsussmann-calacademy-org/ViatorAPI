--all updates created to eliminate secondary objects from being transmitted when the parent object has been manually
--deleted by staff
DROP PROCEDURE sp_buildInterests;
CREATE PROCEDURE sp_buildInterests
AS
  BEGIN
    TRUNCATE TABLE work_interests;
    INSERT INTO work_interests (email)
      SELECT DISTINCT a.email from contacts a
        INNER JOIN ContactsInterests b on a.id = b.contactId INNER JOIN Interests c on b.interestId = c.id
        INNER JOIN MarketoStatus e on a.email = e.key1 AND e.objectType = 'Lead'
        LEFT JOIN MarketoStatus d ON a.email = d.key1 AND c.name = d.key2 AND d.objectType = 'Interest_In'
        LEFT JOIN MarketoStatus f on a.email = f.key1 AND f.objectType = 'Interest_In' AND f.error LIKE '%not found with%'
      WHERE d.key1 IS NULL AND f.key1 IS NULL AND a.email IS NOT NULL and a.email <> '';

    INSERT INTO MarketoStatus (objectType, status, error, key1, key2, lastUpdated) SELECT 'Admin', 'SYNC', null, 'Interest_In', count(*), getdate() FROM work_interests;
  END;

DROP PROCEDURE sp_buildVisitations;
CREATE PROCEDURE sp_buildVisitations
AS
  BEGIN
    TRUNCATE TABLE work_visitations;
    INSERT INTO work_visitations (email, secondary)
      SELECT DISTINCT a.email, cast(a.usageId as varchar) FROM GalaxyScans a
        INNER JOIN MarketoStatus c ON a.email = c.key1 AND c.objectType = 'Lead' AND c.status = 'CREATED'
        LEFT JOIN MarketoStatus d on a.email = d.key1 AND cast(a.usageId as varchar) = d.key2 AND d.objectType = 'Visitation' AND d.status IN ('CREATED', 'UPDATED')
        LEFT JOIN MarketoStatus e on a.email = e.key1 AND e.objectType = 'Visitation' AND e.error LIKE '%not found with%'
      WHERE a.email is not null and d.key1 is NULL AND e.key1 IS NULL and LEN(a.email) > 0
            AND email NOT LIKE 'galaxy%calacademy.org';

    INSERT INTO MarketoStatus (objectType, status, error, key1, key2, lastUpdated)
      SELECT 'Admin', 'SYNC', null, 'Visitation', count(*), getdate() FROM work_visitations;
  END;

DROP PROCEDURE sp_buildConstituents;
CREATE PROCEDURE sp_buildConstituents
AS
  BEGIN
    TRUNCATE TABLE work_constituents;
    INSERT INTO work_constituents (email, secondary)
      SELECT DISTINCT a.constituentEmailAddress, a.visualId from constituents a
        INNER JOIN MarketoStatus c on c.objectType = 'Lead' AND a.constituentEmailAddress = c.key1 AND c.status = 'CREATED'
        LEFT JOIN MarketoStatus b on a.constituentEmailAddress = b.key1 AND a.visualId = b.key2 AND b.objectType = 'Constituent' AND b.status = 'CREATED'
        LEFT JOIN MarketoStatus e on a.constituentEmailAddress = e.key1 AND e.objectType = 'Constituent' AND e.error LIKE '%not found with%'
      WHERE b.key1 IS NULL AND e.key1 IS NULL AND a.constituentEmailAddress IS NOT NULL AND LEN(a.constituentEmailAddress) > 0;

    INSERT INTO MarketoStatus (objectType, status, error, key1, key2, lastUpdated)
      SELECT 'Admin', 'SYNC', null, 'Constituent', count(*), getdate() FROM work_constituents;
  END;

DROP PROCEDURE sp_buildEvents;
CREATE PROCEDURE sp_buildEvents
AS
  BEGIN
    TRUNCATE TABLE work_events;

    INSERT INTO work_events (email, secondary)
      SELECT distinct a.EmailAddress, CAST(a.VisualId AS VARCHAR) from Event a
        INNER JOIN MarketoStatus c ON a.EmailAddress = c.key1 AND c.objectType = 'Lead' and c.status = 'CREATED'
        LEFT JOIN MarketoStatus d on a.EmailAddress = d.key1 AND cast(a.VisualId AS VARCHAR) = d.key2 AND d.objectType = 'Event' and d.status = 'CREATED'
        LEFT JOIN MarketoStatus e on a.EmailAddress = e.key1 AND e.objectType = 'Event' AND e.error LIKE '%not found with%'
      WHERE d.key1 IS NULL AND e.key1 IS NULL AND a.EmailAddress IS NOT NULL AND a.EmailAddress <> '';

    INSERT INTO MarketoStatus (objectType, status, error, key1, key2, lastUpdated)
      SELECT 'Admin', 'SYNC', null, 'Event', count(*), getdate() FROM work_events;
  END

  DROP PROCEDURE sp_buildOrderDetails;
  CREATE PROCEDURE sp_buildOrderDetails
  AS
    BEGIN
      TRUNCATE TABLE work_order_details;
      INSERT INTO work_order_details (email, secondary)
        SELECT DISTINCT a.emailAddress, a.visualID from GalaxyOrderDetail a
          INNER JOIN MarketoStatus c on a.emailAddress = c.key1 AND objectType = 'Lead' AND c.status = 'CREATED'
          LEFT JOIN MarketoStatus b on a.emailAddress = b.key1 AND b.objectType = 'OrderDetail' AND a.visualId = b.key2 AND b.status = 'CREATED'
          LEFT JOIN MarketoStatus d on a.EmailAddress = d.key1 AND d.objectType = 'OrderDetail' AND d.error LIKE '%not found with%'
        WHERE b.key1 IS NULL AND a.EmailAddress IS NOT NULL AND LEN(a.EmailAddress) > 0;

      INSERT INTO MarketoStatus (objectType, status, error, key1, key2, lastUpdated)
        SELECT 'Admin', 'SYNC', null, 'OrderDetail', count(*), getdate() FROM work_order_details;
    END;

    DROP PROCEDURE sp_buildOrders;
    CREATE PROCEDURE sp_buildOrders
    AS
      BEGIN
        TRUNCATE TABLE work_orders;
        INSERT INTO work_orders (email, secondary)
          SELECT DISTINCT a.EmailAddress, cast(a.orderId as VARCHAR) from GalaxyOrders a
            INNER JOIN MarketoStatus c on a.EmailAddress = c.key1 AND objectType = 'Lead' AND c.status = 'CREATED'
            LEFT JOIN MarketoStatus b on a.EmailAddress = b.key1 AND b.objectType = 'Order' AND CAST(a.OrderId AS VARCHAR) = b.key2 AND b.status = 'CREATED'
            LEFT JOIN MarketoStatus d on a.EmailAddress = d.key1 AND b.objectType = 'Order' AND d.error LIKE '%not found with%'
          WHERE b.key1 IS NULL AND d.key1 IS NULL AND a.EmailAddress IS NOT NULL AND LEN(a.EmailAddress) > 0;

        EXECUTE sp_buildOrderDetails;
        INSERT INTO MarketoStatus (objectType, status, error, key1, key2, lastUpdated)
          SELECT 'Admin', 'SYNC', null, 'Order', count(*), getdate() FROM work_orders;
      END;