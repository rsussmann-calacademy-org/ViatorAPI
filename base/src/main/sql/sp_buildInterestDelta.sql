CREATE PROCEDURE sp_buildInterestDelta
AS BEGIN
  UPDATE work_interest_history SET old = new;
  EXECUTE sp_updateInterests;

  INSERT INTO work_interest_history
  (email, new)
    SELECT
      a.email,
      substring(coalesce(optInEnews, ' '), 1, 1)
      + substring(coalesce(optInNightlife, ' '), 1, 1)
      + substring(coalesce(optInDonor, ' '), 1, 1)
      + substring(coalesce(optInMembership, ' '), 1, 1)
      + substring(coalesce(optInLectures, ' '), 1, 1)
      + substring(coalesce(optInHomeSchool, ' '), 1, 1)
      + substring(coalesce(optInRockFamily, ' '), 1, 1)
    FROM contacts a LEFT JOIN work_interest_history b ON a.email = b.email WHERE b.email IS NULL;

  UPDATE work_interest_history set new = '       ';

  UPDATE work_interest_history SET new =
  substring(coalesce(optInEnews, ' '), 1, 1)
  + substring(coalesce(optInNightlife, ' '), 1, 1)
  + substring(coalesce(optInDonor, ' '), 1, 1)
  + substring(coalesce(optInMembership, ' '), 1, 1)
  + substring(coalesce(optInLectures, ' '), 1, 1)
  + substring(coalesce(optInHomeSchool, ' '), 1, 1)
  + substring(coalesce(optInRockFamily, ' '), 1, 1)
  FROM contacts WHERE contacts.email = work_interest_history.email;

  UPDATE contacts set lastUploaded = null WHERE email in (SELECT email FROM work_interest_history WHERE new <> old);
END