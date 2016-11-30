Single Date  / Multiple Products Response
The sample below represents the supplier response to a single date / multiple product batch availability request (see Batch Availability request sample).
Each product has one option (9am private / 9am group), therefore availability is shown for each option of the product.
In this example one tour has been sold out and is therefor unavailable and the other is available.
 
<?xml version="1.0" encoding="UTF-8"?>
<BatchAvailabilityResponse xmlns="http://toursgds.com/api/01">
 <ApiKey>ZDNycnlSdWwzcw</ApiKey>
 <ResellerId>1000</ResellerId>
 <SupplierId>1004</SupplierId>
 <ExternalReference>10051374722992616</ExternalReference>
 <Timestamp>2013-07-25T13:29:52.616+10:00</Timestamp>
 <Extension>
         <any/>
 </Extension>
 <Parameter>
                  <Name></Name>
                  <Value></Value>
 </Parameter>
 <RequestStatus>
         <Status>SUCCESS</Status>
 </RequestStatus>
 <BatchTourAvailability>
         <Date>2014-10-1</Date>
         <TourOptions>
                 <SupplierOptionCode>9AM</SupplierOptionCode>
                 <SupplierOptionName>Bondi Beach Private Lesson 9AM</SupplierOptionName>
                 <TourDepartureTime>09:00:00</TourDepartureTime> 
                 <TourDuration>PT1Hn</TourDuration>
                 <Option>
                         <Name></Name>
                         <Value></Value>
                 </Option>
                 <Language>
                         <LanguageCode></LanguageCode>
                         <LanguageOption></LanguageOption>
                 </Language>
                 <SupplierProductCode>BON_PRI</SupplierProductCode>
          </TourOptions>
         <AvailabilityStatus>
                 <Status>UNAVAILABLE</Status>
                              <UnavailabilityReason>SOLD_OUT</UnavailabilityReason>
         </AvailabilityStatus>
 </BatchTourAvailability>
 <BatchTourAvailability>
         <Date>2014-10-1</Date>
         <TourOptions>
                 <SupplierOptionCode>9AM</SupplierOptionCode>
                 <SupplierOptionName>Bondi Beach Private Lesson 9AM</SupplierOptionName>
                 <TourDepartureTime>09:00:00</TourDepartureTime>
                 <TourDuration>PT1Hn</TourDuration>
                 <Option>
                         <Name></Name>
                         <Value></Value>
                 </Option>
                 <Language>
                         <LanguageCode></LanguageCode>
                         <LanguageOption></LanguageOption>
                 </Language>
                 <SupplierProductCode>BON_GRO</SupplierProductCode>
                 </TourOptions>
         <AvailabilityStatus>
                 <Status>AVAILABLE</Status>
         </AvailabilityStatus>
 </BatchTourAvailability>
</BatchAvailabilityResponse>
 
Date Range  / Single Product Response
The sample below represents the supplier response to a batch availability request for unavailable dates (Blockouts) of a single product (BON_PRI) for a date range (2014-10-30 to 2014-10-31) - see Batch Availability request sample.
In this example tours that are unavailable for the entire date range are displayed.
 
<?xml version="1.0" encoding="UTF-8"?>
<BatchAvailabilityResponse xmlns="http://toursgds.com/api/01">
 <ApiKey>ZDNycnlSdWwzcw</ApiKey>
 <ResellerId>1000</ResellerId>
 <SupplierId>1004</SupplierId>
 <ExternalReference>10051374722992616</ExternalReference>
 <Timestamp>2013-07-25T13:29:52.616+10:00</Timestamp>
 <Extension>
         <any/>
 </Extension>
 <Parameter>
                  <Name></Name>
                  <Value></Value>
 </Parameter>
 <RequestStatus>
         <Status>SUCCESS</Status>
 </RequestStatus>
 <BatchTourAvailability>
         <Date>2014-10-30</Date>
         <TourOptions>
                 <SupplierOptionCode>9AM</SupplierOptionCode>
                 <SupplierOptionName>Bondi Beach Private Lesson 9AM</SupplierOptionName>
                 <TourDepartureTime>09:00:00</TourDepartureTime> 
                 <TourDuration>PT1Hn</TourDuration>
                 <Option>
                         <Name></Name>
                         <Value></Value>
                 </Option>
                 <Language>
                         <LanguageCode></LanguageCode>
                         <LanguageOption></LanguageOption>
                 </Language>
                 <SupplierProductCode>BON_PRI</SupplierProductCode>
                 </TourOptions>
         <AvailabilityStatus>
                 <Status>UNAVAILABLE</Status>
                             <UnavailabilityReason>SOLD_OUT</UnavailabilityReason>
         </AvailabilityStatus>
 </BatchTourAvailability>
 <BatchTourAvailability>
         <Date>2014-10-31</Date>
         <TourOptions>
                 <SupplierOptionCode>9AM</SupplierOptionCode>
                 <SupplierOptionName>Bondi Beach Group Lesson 9AM</SupplierOptionName>
                 <TourDepartureTime>09:00:00</TourDepartureTime>
                 <TourDuration>PT1Hn</TourDuration>
                 <Option>
                         <Name></Name>
                         <Value></Value>
                 </Option>
                 <Language>
                         <LanguageCode></LanguageCode>
                         <LanguageOption></LanguageOption>
                 </Language>
                 <SupplierProductCode>BON_PRI</SupplierProductCode>
                 </TourOptions>
         <AvailabilityStatus>
                 <Status>UNAVAILABLE</Status>
                             <UnavailabilityReason>SOLD_OUT</UnavailabilityReason>
         </AvailabilityStatus>
 </BatchTourAvailability>
</BatchAvailabilityResponse>