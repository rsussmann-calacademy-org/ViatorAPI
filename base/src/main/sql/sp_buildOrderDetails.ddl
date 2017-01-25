  DROP PROCEDURE sp_buildOrderDetails;
  CREATE PROCEDURE sp_buildOrderDetails
  AS
    BEGIN
      TRUNCATE TABLE work_order_details;
      INSERT INTO work_order_details (email, secondary)
        SELECT DISTINCT a.emailAddress, a.visualID from GalaxyOrderDetail a
          INNER JOIN MarketoStatus c
            ON a.emailAddress = c.key1
               AND a.EmailAddress IS NOT NULL
               AND LEN(a.EmailAddress) > 0
               AND objectType = 'Lead' AND c.status IN ('CREATED', 'UPDATED')
          LEFT JOIN MarketoStatus b ON a.emailAddress = b.key1
                                       AND b.objectType = 'OrderDetail' AND a.visualId = b.key2 AND b.status = 'CREATED'
          LEFT JOIN MarketoDeletions d on a.EmailAddress = d.email
        WHERE b.key1 IS NULL AND d.email IS NULL;

      INSERT INTO MarketoStatus (objectType, status, error, key1, key2, lastUpdated)
        SELECT 'Admin', 'SYNC', null, 'OrderDetail', count(*), getdate() FROM work_order_details;
    END;
