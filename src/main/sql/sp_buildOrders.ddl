DROP PROCEDURE sp_buildOrders;
CREATE PROCEDURE sp_buildOrders
    AS
      BEGIN
        TRUNCATE TABLE work_orders;
        INSERT INTO work_orders (email, secondary)
          SELECT DISTINCT a.EmailAddress, cast(a.orderId as VARCHAR) from GalaxyOrders a
          INNER JOIN MarketoStatus c ON a.emailAddress IS NOT NULL
              AND LEN(a.EmailAddress) > 0
              AND a.EmailAddress = c.key1 AND objectType = 'Lead'
              AND c.status IN ('CREATED', 'UPDATED')
          LEFT JOIN MarketoStatus b ON a.EmailAddress = b.key1
              AND b.objectType = 'Order'
              AND a.OrderId = b.key2Number
              AND b.status = 'CREATED'
          LEFT JOIN MarketoDeletions d ON a.EmailAddress = d.email
        WHERE
          b.key1 IS NULL AND d.email IS NULL;

        EXECUTE sp_buildOrderDetails;
        INSERT INTO MarketoStatus(objectType, status, error, key1, key2, lastUpdated)
          SELECT 'Admin', 'SYNC', null, 'Order', count(*), getdate() FROM work_orders;
      END;