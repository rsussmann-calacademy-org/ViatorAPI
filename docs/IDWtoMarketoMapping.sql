---Hey Mike for Lead table update, you can use this SQL. I am not 100% sure on the API names but I think you probably already have that mapped out from previous work
--Contacts table to Lead table

select a.id, a.lastName, a.firstName, a.email,title, jobTitle, company, c.name status, b.name source,  a.dateCreated, a.dateModified,a.RaiserEdgeId, a.galaxyId,
a.PostalCode, a.phone, a.city, a.State, a.Country, a.Birthdate, a.gender, a.school
from contacts a
inner join sources b on a.sourceId = b.id
left outer join status c on c.id=a.statusId


--Contactsinterests to Interest  (marketo custom object name)
--inner join ContactsInterests d on a.id = d.contactId
--You can see from the interest table they have set up the interests as columns on the interest table where I have it as a many to many table 
--between contacts, contactsinterests and interests. here is the SQL for that

select a.email InterestEmailAddress, e.name
from contacts a
inner join ContactsInterests d on a.id = d.contactId
inner join interests e on e.id = d.interestid
--where e.ID = 11 -- photography* missing from the marketo table currently

--*The one that potentially could fail is the above commented out, I do not believe 'photography' was added as a column on the marketo interests table. We should have that added my the next time we run
--this but for now you will need to turn the above rows into columns.


--Constituents to Constituents  (marketo custom object name)
-- pretty straight forward, join back to leads table with email address
select	ConstituentEmailAddress
,	constituentID
,	VisualID
,	MembershipProgram
,	MembershipLevel
,	MembershipStanding
,	ExpirationDate
,	TimesRenewed
,	JoinDate
,	DropDate
,	WeathScoreCategory
,	WeathScore
,	LastGiftAmount
,	HighestGiftAmount
,	LastGiftDate
,	LastGiftFundDescription
,	TotalGiftAmount
from constituents

--GalaxyOrders to Orders (marketo custom object name)
--This one may have some of the columns names change to better map the API names and there are some missing data that I have go get so they are being brought in as null
--I have added aliases to the SQL to match the API names as I have done on the other SQL above

select	email,null VisualID, descr PLUName, description PLUItemGroup, [Purchase Date] OrderDate, [Purchase Amount] OrderTotal,
Quantity, AgencyDescription agency,null	OrderSubtotal,null OrderTax,null DiscountName
from GalaxyOrders

--GalaxyScans to Visitation (marketo custom object name)
--This is pretty straight forward. mapped SQL alias to API names of each custom object

select email EmailAddress, visualID, accessCodeGroupName AccessCodeGroup, null VisitType, usetime VisitDate, qty Quantity
from GalaxyScans

