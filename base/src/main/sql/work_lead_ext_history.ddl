USE [AcademyContacts]
GO

/****** Object:  Table [dbo].[work_lead_ext_history]    Script Date: 12/31/2014 14:43:32 ******/
SET ANSI_NULLS ON
GO

SET QUOTED_IDENTIFIER ON
GO

SET ANSI_PADDING ON
GO

CREATE TABLE [dbo].[work_lead_ext_history](
	[email] [varchar](200) NULL,
	[constituentRecordId] [int] NULL,
	[constituentId] [varchar](20) NULL,
	[visualId] [varchar](50) NULL,
	[category] [varchar](50) NULL,
	[membershipStanding] [varchar](50) NULL,
	[expirationDate] [datetime] NULL,
	[timesRenewed] [int] NULL,
	[constituentJoinDate] [datetime] NULL,
	[membershipProgram] [varchar](50) NULL,
	[dropDate] [datetime] NULL,
	[wealthScoreCategory] [varchar](50) NULL,
	[wealthScore] [int] NULL,
	[lastGiftAmount] [decimal](20, 4) NULL,
	[highestGiftAmount] [decimal](20, 4) NULL,
	[lastGiftDate] [datetime] NULL,
	[lastGiftFundDescription] [varchar](100) NULL,
	[majorGiftLikelihood] [varchar](50) NULL,
	[midLevelGiftLikelihood] [varchar](50) NULL,
	[plannedGiftLikelihood] [varchar](50) NULL,
	[targetGiftRange] [varchar](50) NULL,
	[lastEventStartDate] [datetime] NULL,
	[eventName] [varchar](50) NULL,
	[attended] [varchar](5) NULL,
	[amount] [decimal](20, 4) NULL,
	[ticketType] [varchar](100) NULL,
	[eventCategory] [varchar](100) NULL,
	[eventDonation] [decimal](20, 4) NULL,
	[eventCapacity] [int] NULL,
	[unitQuantity] [int] NULL,
	[totalGiftAmount] [decimal](19, 4) NULL,
	[cs]  AS (checksum([email],[constituentId],[visualId],[category],[membershipStanding],[expirationDate],[timesRenewed],[constituentJoinDate],[membershipProgram],[dropDate],[wealthScoreCategory],[wealthScore],[lastGiftAmount],[highestGiftAmount],[lastGiftDate],[lastGiftFundDescription],[majorGiftLikelihood],[midLevelGiftLikelihood],[plannedGiftLikelihood],[targetGiftRange],[lastEventStartDate],[eventName],[attended],[amount],[ticketType],[eventCategory],[eventDonation],[unitQuantity],[totalGiftAmount]))
) ON [PRIMARY]

GO

SET ANSI_PADDING OFF
GO

