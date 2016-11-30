Single Date Response
The below sample is the response provided by the supplier system for a single date availability request. In this response the option (9am) of the tour (BON_PRI) is available on October 31 2014. The response provides a reference number (optional) for the availability hold that was requested in the call (see availability request sample), the reference will be used in the subsequent booking request (optional). The availability response also provides availability information for all age bands for the date and product of the request - allowing Viator systems to track availability for other age bands and reduce the number of calls to the supplier.


<AvailabilityResponse xmlns="http://toursgds.com/api/01">
         <ApiKey>cdqu60CykKeca1Qc000VXwgchV000L2fNOOf0bv9gPp</ApiKey>
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
         <SupplierProductCode>BON_PRI</SupplierProductCode>
         <TourAvailability>
                 <Date>2014-10-31</Date>
                   <TourOptions>
                 <SupplierOptionCode>9AM</SupplierOptionCode>
                 <SupplierOptionName>Bondi Beach Private Lesson 9AM</SupplierOptionName>
                 <TourDepartureTime>09:00:00</TourDepartureTime>
                 <TourDuration>PT1H</TourDuration>
                 <Language>
                         <LanguageCode>EN</LanguageCode>
                         <LanguageOption>GUIDE</LanguageOption>
                 </Language>
                             <Option>
                               <Name></Name>
                               <Value></Value>
                             </Option>
         </TourOptions>
                   <AvailabilityStatus>
                             <Status>AVAILABLE</Status>
                   </AvailabilityStatus>
                   <AvailabilityHold>
                             <Expiry>PT300S</Expiry>
                           <Reference>1K883383K2S12K883383K2S57K883383K2</Reference>
                   </AvailabilityHold>
 </TourAvailability>
 <TravellerMixAvailability>
         <Adult>TRUE</Adult>
         <Child>TRUE</Child>
         <Youth>TRUE</Youth>
         <Infant>FALSE</Infant>
         <Senior>TRUE</Senior>
 </TravellerMixAvailability>
</AvailabilityResponse>

Date Range Response
The below sample is the response provided by the supplier system for a date range availability request. In this response the product is sold out, therefore not available.


<AvailabilityResponse xmlns="http://toursgds.com/api/01">
         <ApiKey>cdqu60CykKeca1Qc000VXwgchV000L2fNOOf0bv9gPp</ApiKey>
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
         <SupplierProductCode>BLUE</SupplierProductCode>
         <TourAvailability>
                 <Date>2014-10-30</Date>
                   <TourOptions>
                 <SupplierOptionCode>BASIC</SupplierOptionCode>
                 <SupplierOptionName>Basic Shared Accommodation</SupplierOptionName>
                 <TourDepartureTime></TourDepartureTime>
                 <TourDuration></TourDuration>
                 <Language>
                         <LanguageCode>EN</LanguageCode>
                         <LanguageOption>GUIDE</LanguageOption>
                 </Language>
                             <Option>
                               <Name>room</Name>
                        <Value>dualocc</Value>
                      </Option>
         </TourOptions>
            <AvailabilityStatus>
                             <Status>AVAILABLE</Status>
            </AvailabilityStatus>
                   <AvailabilityHold>
                             <Expiry></Expiry>
                    <Reference></Reference>
                   </AvailabilityHold>
 </TourAvailability>
  <TourAvailability>
                  <Date>2014-10-31</Date>
            <TourOptions>
                 <SupplierOptionCode>BASIC</SupplierOptionCode>
                 <SupplierOptionName>Basic Shared Accommodation</SupplierOptionName>
                 <TourDepartureTime></TourDepartureTime>
                 <TourDuration></TourDuration>
                 <Language>
                         <LanguageCode>EN</LanguageCode>
                         <LanguageOption>GUIDE</LanguageOption>
                 </Language>
                      <Option>
                               <Name>room</Name>
                               <Value>dualocc</Value>
                      </Option>
         </TourOptions>
            <AvailabilityStatus>
                 <Status>UNAVAILABLE</Status>
                 <UnavailabilityReason>SOLD_OUT</UnavailabilityReason>
         </AvailabilityStatus>
                   <AvailabilityHold>
                             <Expiry></Expiry>
                           <Reference></Reference>
                   </AvailabilityHold>
 </TourAvailability>
</AvailabilityResponse>