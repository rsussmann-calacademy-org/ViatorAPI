USE [AcademyContacts]
GO

/****** Object:  Table [dbo].[work_lead_ext]    Script Date: 12/31/2014 14:43:06 ******/
SET ANSI_NULLS ON
GO

SET QUOTED_IDENTIFIER ON
GO

SET ANSI_PADDING ON
GO

CREATE TABLE [dbo].[work_interest_history](
	[email] [varchar](200) NULL,
	[interests] [varchar](10) NULL,
	[new] [varchar](50) NULL,
  [old] [varchar](50) NULL
)

SELECT email FROM contacts a INNER JOIN work_interest_history b on a.email = b.email
WHERE
(a.optInEnews IS NOT NULL AND b.optInEnews IS NULL)
OR (a.optInEnews IS NULL AND b.optInEnews IS NOT NULL)
OR (a.optInEnews IS NOT NULL AND b.optInEnews IS NOT NULL AND a.optInEnews <> b.optInEnews)


INSERT INTO work_interest_history
(email, new, old)
  SELECT
    email,
    substring(coalesce(optInEnews, ' '), 1, 1)
    + substring(coalesce(optInNightlife, ' '), 1, 1)
    + substring(coalesce(optInDonor, ' '), 1, 1)
    + substring(coalesce(optInMembership, ' '), 1, 1)
    + substring(coalesce(optInLectures, ' '), 1, 1)
    + substring(coalesce(optInHomeSchool, ' '), 1, 1)
    + substring(coalesce(optInRockFamily, ' '), 1, 1)
  FROM contacts where optInEnews is not null;
