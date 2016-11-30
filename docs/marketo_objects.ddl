DROP TABLE work_constituents;
DROP TABLE work_events;
DROP TABLE work_interests;
DROP TABLE work_leads;
DROP TABLE work_order_details;
DROP TABLE work_orders;
DROP TABLE work_visitations;

CREATE TABLE work_leads
(
  email VARCHAR(200) NOT NULL,
  worker VARCHAR(100)
);

CREATE TABLE work_visitations
(
  email VARCHAR(200) NOT NULL,
  secondary VARCHAR(200),
  worker VARCHAR(100)
);

CREATE TABLE work_events
(
  email VARCHAR(200) NOT NULL,
  secondary VARCHAR(200),
  worker VARCHAR(100)
);

CREATE TABLE work_interests
(
  email VARCHAR(200) NOT NULL,
  secondary VARCHAR(200),
  worker VARCHAR(100)
);

CREATE TABLE work_orders
(
  email VARCHAR(200) NOT NULL,
  secondary VARCHAR(200),
  worker VARCHAR(100)
);

CREATE TABLE work_order_details
(
  email VARCHAR(200) NOT NULL,
  secondary VARCHAR(200),
  worker VARCHAR(100)
);

CREATE TABLE work_constituents
(
  email VARCHAR(200) NOT NULL,
  secondary VARCHAR(200),
  worker VARCHAR(100)
);


DROP PROCEDURE sp_buildEvents;
DROP PROCEDURE sp_buildVisitations;
DROP PROCEDURE sp_buildConstituents;
DROP PROCEDURE sp_buildOrders;
DROP PROCEDURE sp_buildOrderDetails;


CREATE PROCEDURE sp_buildEvents
AS
  BEGIN
    TRUNCATE TABLE work_events;

    INSERT INTO work_events (email, secondary)
    SELECT distinct a.EmailAddress, CAST(a.VisualId AS VARCHAR) from Event a
    INNER JOIN MarketoStatus c ON a.EmailAddress = c.key1 AND c.objectType = 'Lead'
	LEFT JOIN MarketoStatus d on a.EmailAddress = d.key1 AND cast(a.VisualId AS VARCHAR) = d.key2 AND d.objectType = 'Event'
    WHERE d.key1 IS NULL AND a.EmailAddress IS NOT NULL AND a.EmailAddress <> ''

    INSERT INTO MarketoStatus (objectType, status, error, key1, key2, lastUpdated)
		SELECT 'Admin', 'SYNC', null, 'Event', count(*), getdate() FROM work_events;
  END

CREATE PROCEDURE sp_buildVisitations
AS
BEGIN
    TRUNCATE TABLE work_visitations;
    INSERT INTO work_visitations (email, secondary)
      SELECT DISTINCT a.email, cast(a.usageId as varchar) FROM GalaxyScans a
        INNER JOIN MarketoStatus c ON a.email = c.key1 AND c.objectType = 'Lead'
        LEFT JOIN MarketoStatus d on a.email = d.key1 AND cast(a.usageId as varchar) = d.key2 AND d.objectType = 'Visitation'
      WHERE a.email is not null and d.key1 is NULL and LEN(a.email) > 0
      AND email NOT LIKE 'galaxy%calacademy.org';

    INSERT INTO MarketoStatus (objectType, status, error, key1, key2, lastUpdated)
		SELECT 'Admin', 'SYNC', null, 'Visitation', count(*), getdate() FROM work_visitations;
END;

CREATE PROCEDURE sp_buildConstituents
AS
BEGIN
	TRUNCATE TABLE work_constituents;
	INSERT INTO work_constituents (email, secondary)
	  SELECT DISTINCT a.constituentEmailAddress, a.visualId from constituents a
	  INNER JOIN MarketoStatus c on c.objectType = 'Lead' AND a.constituentEmailAddress = c.key1
	  LEFT JOIN MarketoStatus b on a.constituentEmailAddress = b.key1 AND a.visualId = b.key2 AND b.objectType = 'Constituent'
	  WHERE b.key1 IS NULL
	  AND a.constituentEmailAddress IS NOT NULL AND LEN(a.constituentEmailAddress) > 0;

	INSERT INTO MarketoStatus (objectType, status, error, key1, key2, lastUpdated)
		SELECT 'Admin', 'SYNC', null, 'Constituent', count(*), getdate() FROM work_constituents;
END;

CREATE PROCEDURE sp_buildOrders
AS
BEGIN
  TRUNCATE TABLE work_orders;
  INSERT INTO work_orders (email, secondary)
	SELECT DISTINCT a.EmailAddress, cast(a.orderId as VARCHAR) from GalaxyOrders a
	  INNER JOIN MarketoStatus c on a.EmailAddress = c.key1 AND objectType = 'Lead'
	  LEFT JOIN MarketoStatus b on a.EmailAddress = b.key1 AND b.objectType = 'Order' AND CAST(a.OrderId AS VARCHAR) = b.key2
	WHERE b.key1 IS NULL AND a.EmailAddress IS NOT NULL AND LEN(a.EmailAddress) > 0;

  EXECUTE sp_buildOrderDetails;
  INSERT INTO MarketoStatus (objectType, status, error, key1, key2, lastUpdated)
	SELECT 'Admin', 'SYNC', null, 'Order', count(*), getdate() FROM work_orders;
END;


CREATE PROCEDURE sp_buildOrderDetails
AS
BEGIN
  TRUNCATE TABLE work_order_details;
  INSERT INTO work_order_details (email, secondary)
	  SELECT DISTINCT a.emailAddress, a.visualID from GalaxyOrderDetail a
		INNER JOIN MarketoStatus c on a.emailAddress = c.key1 AND objectType = 'Lead'
		LEFT JOIN MarketoStatus b on a.emailAddress = b.key1 AND b.objectType = 'OrderDetail' AND a.visualId = b.key2
	  WHERE b.key1 IS NULL AND a.EmailAddress IS NOT NULL AND LEN(a.EmailAddress) > 0;

  INSERT INTO MarketoStatus (objectType, status, error, key1, key2, lastUpdated)
	SELECT 'Admin', 'SYNC', null, 'OrderDetail', count(*), getdate() FROM work_order_details;
END;
