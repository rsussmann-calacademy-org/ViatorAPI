CREATE PROCEDURE sp_queueOpportunities
AS BEGIN
  INSERT INTO MarketoLeads(email, marketoLeadId)
    SELECT distinct key1, max(cast(key2 as int)) FROM MarketoStatus a LEFT JOIN MarketoLeads b on a.key1 = b.email  where b.email is NULL and a.status IN ('CREATED', 'UPDATED') and a.objectType = 'Lead' group by key1;

  INSERT INTO MarketoOpportunities(marketoLeadId, orderDate, orderTotal, orderId)
    SELECT marketoLeadId, orderDate, pluPrice, orderId from GalaxyOrders a inner join MarketoLeads b ON a.emailAddress = b.email
    WHERE orderId NOT IN (select orderId FROM MarketoOpportunities);
END
  