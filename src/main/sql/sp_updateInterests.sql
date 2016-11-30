CREATE PROCEDURE sp_updateInterests
AS BEGIN
  UPDATE contacts set optInEnews = null, optInNightlife = null, optInDonor = null, optInMembership = null, optInLectures = null, optInRockFamily = null;

  UPDATE contacts SET optInEnews = 'yes' WHERE email in (
    SELECT
      a.email
    FROM contacts a INNER JOIN contactsInterests b ON a.id = b.contactId
      INNER JOIN interests c ON b.interestId = c.id
    WHERE
      c.code = 'NonMember'
  );

  UPDATE contacts SET optInNightlife = 'yes' WHERE email in (
    SELECT
      a.email
    FROM contacts a INNER JOIN contactsInterests b ON a.id = b.contactId
      INNER JOIN interests c ON b.interestId = c.id
    WHERE
      c.code = 'Nightlife'
  );

  UPDATE contacts SET optInDonor = 'yes' WHERE email in (
    SELECT
      a.email
    FROM contacts a INNER JOIN contactsInterests b ON a.id = b.contactId
      INNER JOIN interests c ON b.interestId = c.id
    WHERE
      c.code IN ('DonorChase', 'DonorFriends', 'DonorLapsed', 'DonorPipeline')
  );

  UPDATE contacts SET optInMembership = 'yes' WHERE email in (
    SELECT
      a.email
    FROM contacts a INNER JOIN contactsInterests b ON a.id = b.contactId
      INNER JOIN interests c ON b.interestId = c.id
    WHERE
      c.code IN ('MembershipActive', 'MembershipEnews', 'MembershipLapsed')
  );

  UPDATE contacts SET optInLectures = 'yes' WHERE email in (
    SELECT
      a.email
    FROM contacts a INNER JOIN contactsInterests b ON a.id = b.contactId
      INNER JOIN interests c ON b.interestId = c.id
    WHERE
      c.code = 'Lectures'
  );

  UPDATE contacts SET optInHomeschool = 'yes' WHERE email in (
    SELECT
      a.email
    FROM contacts a INNER JOIN contactsInterests b ON a.id = b.contactId
      INNER JOIN interests c ON b.interestId = c.id
    WHERE
      c.code = 'HomeSchool'
  );

  UPDATE contacts SET optInRockFamily = 'yes' WHERE email in (
    SELECT
      a.email
    FROM contacts a INNER JOIN contactsInterests b ON a.id = b.contactId
      INNER JOIN interests c ON b.interestId = c.id
    WHERE
      c.code = 'RockFamily'
  );
END