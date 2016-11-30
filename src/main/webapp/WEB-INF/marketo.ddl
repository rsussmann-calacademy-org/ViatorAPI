DROP TABLE work_lead_ext;
CREATE TABLE work_lead_ext (
  email varchar(200),
  optInEnews varchar(10),
  optInNightlife varchar(10),
  optInDonor varchar(10),
  optInMembership varchar(10),
  optInLectures varchar(10),
  optInHomeSchool varchar(10),
  optInRockFamily varchar(10),
  constituentRecordId int,
  constituentId varchar(20),
  visualId varchar(50),
  category varchar(50),
  membershipStanding varchar(50),
  expirationDate datetime,
  timesRenewed int,
  constituentJoinDate datetime,
  membershipProgram varchar(50),
  dropDate datetime,
  wealthScoreCategory varchar(50),
  wealthScore int,
  lastGiftAmount DECIMAL(20,4),
  highestGiftAmount DECIMAL(20,4),
  lastGiftDate datetime,
  lastGiftFundDescription varchar(100),
  majorGiftLikelihood varchar(50),
  midLevelGiftLikelihood varchar(50),
  plannedGiftLikelihood varchar(50),
  targetGiftRange varchar(50),
  lastEventStartDate datetime,
  eventName VARCHAR(50),
  attended VARCHAR(5),
  amount DECIMAL(20,4),
  ticketType  VARCHAR(100),
  eventCategory VARCHAR(100),
  eventDonation DECIMAL(20,4),
  eventCapacity INT,
  unitQuantity INT,
  totalGiftAmount DECIMAL(19,4),
  cs AS CHECKSUM(email, optInRockFamily, optInEnews, optInDonor, optInMembership, optInHomeSchool, optInNightLife, optInLectures,constituentId, visualId, category, membershipStanding, expirationDate, timesRenewed, constituentJoinDate, membershipProgram, dropDate, wealthScoreCategory, wealthScore, lastGiftAmount, highestGiftAmount, lastGiftDate, lastGiftFundDescription, majorGiftLikelihood, midLevelGiftLikelihood, plannedGiftLikelihood, targetGiftRange, lastEventStartDate, eventName, attended, amount, ticketType , eventCategory, eventDonation, unitQuantity, totalGiftAmount),
  dirty bit,
  transmitted datetime
);

DROP TABLE work_lead_ext_history;
CREATE TABLE work_lead_ext_history (
  email varchar(200),
  optInEnews varchar(10),
  optInNightlife varchar(10),
  optInDonor varchar(10),
  optInMembership varchar(10),
  optInLectures varchar(10),
  optInHomeSchool varchar(10),
  optInRockFamily varchar(10),
  constituentRecordId int,
  constituentId varchar(20),
  visualId varchar(50),
  category varchar(50),
  membershipStanding varchar(50),
  expirationDate datetime,
  timesRenewed int,
  constituentJoinDate datetime,
  membershipProgram varchar(50),
  dropDate datetime,
  wealthScoreCategory varchar(50),
  wealthScore int,
  lastGiftAmount DECIMAL(20,4),
  highestGiftAmount DECIMAL(20,4),
  lastGiftDate datetime,
  lastGiftFundDescription varchar(100),
  majorGiftLikelihood varchar(50),
  midLevelGiftLikelihood varchar(50),
  plannedGiftLikelihood varchar(50),
  targetGiftRange varchar(50),
  lastEventStartDate datetime,
  eventName VARCHAR(50),
  attended VARCHAR(5),
  amount DECIMAL(20,4),
  ticketType  VARCHAR(100),
  eventCategory VARCHAR(100),
  eventDonation DECIMAL(20,4),
  eventCapacity INT,
  unitQuantity INT,
  totalGiftAmount DECIMAL(19,4),
  cs AS CHECKSUM(email, optInRockFamily, optInEnews, optInDonor, optInMembership, optInHomeSchool, optInNightLife, optInLectures,constituentId, visualId, category, membershipStanding, expirationDate, timesRenewed, constituentJoinDate, membershipProgram, dropDate, wealthScoreCategory, wealthScore, lastGiftAmount, highestGiftAmount, lastGiftDate, lastGiftFundDescription, majorGiftLikelihood, midLevelGiftLikelihood, plannedGiftLikelihood, targetGiftRange, lastEventStartDate, eventName, attended, amount, ticketType , eventCategory, eventDonation, unitQuantity, totalGiftAmount)
);

CREATE INDEX idx_wli_email ON work_lead_ext(email);
CREATE PROCEDURE sp_buildLeadProperties
AS BEGIN
  ALTER INDEX idx_wli_email ON work_lead_ext DISABLE;

--refresh the history table from the current table before rebuilding the current table (we use history to compare / find updated lead records)
  TRUNCATE TABLE work_lead_ext_history;
  INSERT INTO work_lead_ext_history(email, optInRockFamily, optInEnews, optInDonor, optInMembership, optInHomeSchool, optInNightLife, optInLectures,constituentId, visualId, category, membershipStanding, expirationDate, timesRenewed, constituentJoinDate, membershipProgram, dropDate, wealthScoreCategory, wealthScore, lastGiftAmount, highestGiftAmount, lastGiftDate, lastGiftFundDescription, majorGiftLikelihood, midLevelGiftLikelihood, plannedGiftLikelihood, targetGiftRange, lastEventStartDate, eventName, attended, amount, ticketType , eventCategory, eventDonation, unitQuantity, totalGiftAmount) SELECT email, optInRockFamily, optInEnews, optInDonor, optInMembership, optInHomeSchool, optInNightLife, optInLectures,constituentId, visualId, category, membershipStanding, expirationDate, timesRenewed, constituentJoinDate, membershipProgram, dropDate, wealthScoreCategory, wealthScore, lastGiftAmount, highestGiftAmount, lastGiftDate, lastGiftFundDescription, majorGiftLikelihood, midLevelGiftLikelihood, plannedGiftLikelihood, targetGiftRange, lastEventStartDate, eventName, attended, amount, ticketType , eventCategory, eventDonation, unitQuantity, totalGiftAmount FROM work_lead_ext;
  TRUNCATE TABLE work_lead_ext;

--initialize the lead table with addresses from the constituent query
  INSERT INTO work_lead_ext(email)
    SELECT DISTINCT a.email from contacts a INNER JOIN contactsInterests b on a.id = b.contactId INNER JOIN interests c on b.interestid = c.id
    WHERE a.email NOT IN (select email FROM work_lead_ext);

--initialize the lead table with addresses from the constituent query
  INSERT INTO work_lead_ext(email)
    SELECT DISTINCT [Preferred E-mail Number] from RE_EventQuery
    WHERE [Preferred E-mail Number] NOT IN (select email FROM work_lead_ext);

  ALTER INDEX idx_wli_email ON work_lead_ext REBUILD;

--update interests based on the contactsInterests table
  UPDATE work_lead_ext SET optInEnews = 'yes' WHERE email in (
    SELECT
      a.email
    FROM contacts a INNER JOIN contactsInterests b ON a.id = b.contactId
      INNER JOIN interests c ON b.interestId = c.id
    WHERE
      c.code = 'NonMember'
  );

  UPDATE work_lead_ext SET optInNightlife = 'yes' WHERE email in (
    SELECT
      a.email
    FROM contacts a INNER JOIN contactsInterests b ON a.id = b.contactId
      INNER JOIN interests c ON b.interestId = c.id
    WHERE
      c.code = 'Nightlife'
  );

  UPDATE work_lead_ext SET optInDonor = 'yes' WHERE email in (
    SELECT
      a.email
    FROM contacts a INNER JOIN contactsInterests b ON a.id = b.contactId
      INNER JOIN interests c ON b.interestId = c.id
    WHERE
      c.code IN ('DonorChase', 'DonorFriends', 'DonorLapsed', 'DonorPipeline')
  );

  UPDATE work_lead_ext SET optInMembership = 'yes' WHERE email in (
    SELECT
      a.email
    FROM contacts a INNER JOIN contactsInterests b ON a.id = b.contactId
      INNER JOIN interests c ON b.interestId = c.id
    WHERE
      c.code IN ('MembershipActive', 'MembershipEnews', 'MembershipLapsed')
  );

  UPDATE work_lead_ext SET optInLectures = 'yes' WHERE email in (
    SELECT
      a.email
    FROM contacts a INNER JOIN contactsInterests b ON a.id = b.contactId
      INNER JOIN interests c ON b.interestId = c.id
    WHERE
      c.code = 'Lectures'
  );

  UPDATE work_lead_ext SET optInHomeschool = 'yes' WHERE email in (
    SELECT
      a.email
    FROM contacts a INNER JOIN contactsInterests b ON a.id = b.contactId
      INNER JOIN interests c ON b.interestId = c.id
    WHERE
      c.code = 'HomeSchool'
  );

  UPDATE work_lead_ext SET optInRockFamily = 'yes' WHERE email in (
    SELECT
      a.email
    FROM contacts a INNER JOIN contactsInterests b ON a.id = b.contactId
      INNER JOIN interests c ON b.interestId = c.id
    WHERE
      c.code = 'RockFamily'
  );

--find the most constituent record with the latest expiration date and set the id on the corresponding record in work_lead_ext
  UPDATE work_lead_ext SET constituentRecordId =
  (SELECT MAX(id) FROM constituents b WHERE b.constituentEmailAddress = email AND ExpirationDate = (SELECT MAX(ExpirationDate) FROM constituents a WHERE a.constituentEmailAddress = email));

--update associated values from the selected constituent record
  UPDATE w SET w.constituentId = c.constituentId,
    w.visualId = b.VIDAttribute,
    w.category = b.Category,
    w.membershipStanding = b.Standing,
    w.expirationDate = b.Exp,
    w.timesRenewed = b.TimesRenewed,
    w.constituentJoinDate = b.[Date Joined],
    w.membershipProgram = b.[Mem Program],
    w.dropDate = null,
    w.wealthScoreCategory = c.wealthScoreCategory,
    w.wealthScore = c.wealthScore,
    w.lastGiftAmount = b.[Last Gift Amt],
    w.highestGiftAmount = b.[Largest Gift Amt],
    w.lastGiftDate = b.[Last Gift Date],
    w.lastGiftFundDescription = b.[Last Gift Fund 1],
    w.majorGiftLikelihood = b.[CMS Major Gift Likelihood],
    w.midLevelGiftLikelihood = b.[CMS Mid-Level Gift Likelihood],
    w.plannedGiftLikelihood = b.[CMS Planned Gift Likelihood],
    w.targetGiftRange = b.[CMS Target Gift Range]
  FROM work_lead_ext w
    INNER JOIN constituents c ON w.email = c.constituentEmailAddress
    INNER JOIN [AcademyContacts].[dbo].[RE_ConstituentQuery] b ON c.constituentId = b.constituent_id;

--find the event with the latest start date, pull that one in as the denormalized event (note: this is non-deterministic if a
--given constituent has two events that start on the same date associated with their account)
  UPDATE work_lead_ext SET lastEventStartDate = (SELECT MAX([Event Start Date]) FROM RE_EventQuery WHERE [Constituent_ID] = constituentId);

--update work_lead_ext with the selected record
  UPDATE work_lead_ext SET eventName = b.[Event Name],
    attended = b.[Participant Has Attended],
    amount = b.[Registration Fee Gift Amount],
    ticketType = b.[Registration Fee Unit],
    eventCategory = b.[Event Type],
    eventDonation = b.[Other Donation Amount],
    unitQuantity = b.[Registration Fee Number of Units],
    totalGiftAmount = b.[Gift Amount],
    eventCapacity = c.totalCapacity
  FROM work_lead_ext w INNER JOIN RE_EventQuery b ON w.constituentId = b.CONSTITUENT_ID AND w.lastEventStartDate = b.[Event Start Date]
    INNER JOIN RMCapacity c ON b.eventId = c.eventId;

--determine which records have changed since the last run, mark them as dirty
  UPDATE work_lead_ext set dirty = 0;
  UPDATE work_lead_ext set dirty = 1 WHERE email in (SELECT a.email FROM work_lead_ext a LEFT JOIN work_lead_ext_history b ON a.email = b.email WHERE a.cs <> b.cs);
  UPDATE contacts set lastUploaded = null WHERE email IN (SELECT a.email FROM work_lead_ext a LEFT JOIN work_lead_ext_history b ON a.email = b.email WHERE a.cs <> b.cs);
END