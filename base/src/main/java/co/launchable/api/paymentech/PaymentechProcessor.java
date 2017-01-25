package co.launchable.api.paymentech;

import com.paymentech.orbital.sdk.configurator.Configurator;
import com.paymentech.orbital.sdk.engine.EngineIF;
import com.paymentech.orbital.sdk.engine.pool.EnginePool;
import com.paymentech.orbital.sdk.engine.pool.EnginePoolIF;
import com.paymentech.orbital.sdk.interfaces.RequestIF;
import com.paymentech.orbital.sdk.interfaces.ResponseIF;
import com.paymentech.orbital.sdk.request.Request;
import com.paymentech.orbital.sdk.util.exceptions.InitializationException;
import org.apache.log4j.Logger;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

//import org.apache.commons.logging.Log;
//import org.apache.commons.logging.LogFactory;

// Referenced classes of package com.softslate.commerce.businessobjects.payment:
//            BasePaymentProcessor, Payment, PaymentProcessor

public class PaymentechProcessor
{
    Logger log = Logger.getLogger(PaymentechProcessor.class);

    private Map responses = new HashMap();
    private Map orderNumbersByKey = new HashMap();

    public static String ORBITAL_USERNAME = "CALIACASC1113";
    public static String ORBITAL_PASSWORD = "R1RDZX3G4PA55";
    public static String ORBITAL_MERCHANT_ID = "700000005646";
    public static String ORBITAL_TERMINAL_ID = "001";
    public static String ORBITAL_BIN = "000002";
    private EnginePoolIF enginePool;
    private Configurator configurator;
    public static int SERVER_ID = 1;
    private long timeout = 90000;
    private boolean testRetryLogic = true;
    private RequestIF lastRequest;
    private Map mapTraces = new HashMap();
    private long currentOrderNumber = 10000000;

    public PaymentechProcessor() {
        try {
            enginePool = EnginePool.getInstance();
            configurator = Configurator.getInstance();
            configurator.load();
        } catch (InitializationException ie) {
            log.error(ie.getMessage());
            //ie.printStackTrace();
        } catch (ClassNotFoundException cnfe) {
            log.error(cnfe.getMessage());
        }
    }

    public String getConfigFileName() {
        return configurator.getConfigFileName();
    }

    public boolean testReachability() {
        log.info("Testing reachability of payment processor");
        try {
            Exchange exchange = authTransaction("TESTAUTH", 10.00, "4321432143214321", "111", "0929", "John Doe", "123 Main Street", null, null, "New York", "NY", "11111");
            return exchange.getResponse() != null;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean empty(String field) {
        return field == null || field.length() == 0;
    }

    private RequestIF addNonCaptureFields(RequestIF request) throws Exception {
        request.setFieldValue("IndustryType", "EC");
        request.setFieldValue("Comments", "Java SDK 6.8.0");
        return request;
    }

    private Request generateBasicRequest(String type) throws Exception {
        Request request = new Request(type);
        request.setFieldValue("OrbitalConnectionUsername", ORBITAL_USERNAME);
        request.setFieldValue("OrbitalConnectionPassword", ORBITAL_PASSWORD);
        request.setFieldValue("BIN", ORBITAL_BIN);
        request.setFieldValue("MerchantID", ORBITAL_MERCHANT_ID);
        request.setFieldValue("TerminalID", ORBITAL_TERMINAL_ID);

        String trace = SERVER_ID + "" + System.currentTimeMillis();
        request.setTraceNumber(trace);
        return request;
    }

    private Request generateGiftCardRequest() throws Exception {
        Request request = generateBasicRequest("FlexCache");
        request.setFieldValue("FlexPartialRedemptionInd", "N");
        return request;
    }

    protected Map processTransaction(Map parameters)
        throws Exception
    {
        String giftCardNumber = ((String)parameters.get("giftCardNumber")).replaceAll("[^0-9]+", "");;
        String giftCardSecurityValue = ((String)parameters.get("giftCardSecurityValue")).replaceAll("[^0-9]+", "");

        String organizationLocalOrderId = "";
        Double transactionAmount = 5.0;
        Exchange exchange = redemptionTransaction(parameters,
                organizationLocalOrderId,
                transactionAmount,
                giftCardNumber,
                giftCardSecurityValue);
        printResponseCodes("Order", exchange.getResponse());
        return processResponse(exchange.getResponse(), parameters);
    }

    protected Exchange processGiftCardTransaction(String transactionType, String orderId, Double amount, String accountNumber, String cardSecurityValue, String transactionReferenceNumber) throws Exception {

        Exchange exchange = new Exchange();
        Request request = generateGiftCardRequest();
        request.setFieldValue("AccountNum", accountNumber);
        request.setFieldValue("OrderID", orderId);
        if (amount != null)
            request.setFieldValue("Amount", ((Double)(amount * 100D)).intValue() + "");
        request.setFieldValue("CardSecVal", cardSecurityValue);
        request.setFieldValue("FlexAction", transactionType);
        if (transactionReferenceNumber != null)
            request.setFieldValue("TxRefNum", transactionReferenceNumber);

        exchange.setRequest(request);
        processTx(exchange);
        return exchange;
    }

    private void printResponseCodes(String orderId, ResponseIF response, boolean printTraceNumber) {
        String separationCharacter = ",";
        String trace = (String)mapTraces.get(orderId);

        if (response != null) {
            responses.put(orderId, response);
            //System.out.print(orderId + ",");
            System.out.print(orderId + "[" + trace + "]" + separationCharacter);
            System.out.println(response.getValue("AuthCode") + separationCharacter  + response.getValue("RespCode") + separationCharacter + response.getValue("AVSRespCode") + separationCharacter + response.getCVV2RespCode() + separationCharacter +  response.getValue("TxRefNum"));
        } else {
            System.out.println(orderId + ": No response.");
        }
    }

    private void printResponseCodes(String orderId, ResponseIF response) {
        String separationCharacter = ",";
        String trace = (String)mapTraces.get(orderId);

        if (response != null) {
            responses.put(orderId, response);
            System.out.print(orderId + ",");
            //System.out.print(orderId + "[" + trace + "]" + separationCharacter);
            System.out.println(response.getValue("AuthCode") + separationCharacter  + response.getValue("RespCode") + separationCharacter + response.getValue("AVSRespCode") + separationCharacter + response.getCVV2RespCode() + separationCharacter +  response.getValue("TxRefNum"));
        } else {
            System.out.println(orderId + ": No response.");
        }
    }

    protected Exchange addValueTransaction(Map parameters, String orderId, Double amount, String accountNumber, String cardSecurityValue) throws Exception {
        Exchange exchange = processGiftCardTransaction("AddValue", orderId, amount, accountNumber, cardSecurityValue, null);
        exchange.printResponseCodes();
        return exchange;
    }

    protected Exchange redemptionTransaction(Map parameters, String orderId, Double amount, String accountNumber, String cardSecurityValue) throws Exception {
        Exchange exchange = processGiftCardTransaction("Redemption", orderId, amount, accountNumber, cardSecurityValue, null);
        return exchange;
    }

    protected Exchange balanceInquiryTransaction(Map parameters, String orderId, String accountNumber, String cardSecurityValue) throws Exception {
        Exchange exchange = processGiftCardTransaction("BalanceInquiry", orderId, null, accountNumber, cardSecurityValue, null);
        return exchange;
    }

    protected Exchange giftCardVoidTransaction(Map parameters, String orderId, String accountNumber, String cardSecurityValue, String transactionReferenceNumber) throws Exception {
        Exchange exchange = processGiftCardTransaction("Void", orderId, null, accountNumber, cardSecurityValue, transactionReferenceNumber);
        return exchange;
    }

    protected RequestIF captureRequest(String orderId, String transactionReferenceNumber, Double amount) throws Exception {
        RequestIF request = generateBasicRequest(RequestIF.MARK_FOR_CAPTURE_TRANSACTION);

        request.setFieldValue("TxRefNum", transactionReferenceNumber);
        request.setFieldValue("OrderID", orderId);
        request.setFieldValue("Amount", ((Double)(amount * 100D)).intValue() + "");

        return request;
    }

    protected RequestIF reverseRequest(String orderId, String transactionReferenceNumber, boolean isVoidOfCapturedTransaction) throws Exception {
        log.info("reversing transaction with reference number " + transactionReferenceNumber);

        RequestIF request = generateBasicRequest(RequestIF.REVERSE_TRANSACTION);
        request.setFieldValue("TxRefNum", transactionReferenceNumber);
        request.setFieldValue("OrderID", orderId);
        if (isVoidOfCapturedTransaction)
            request.setFieldValue("OnlineReversalInd", "Y");
        else
            request.setFieldValue("OnlineReversalInd", "N");
        return request;
    }

    protected RequestIF authXRequest(
            String orderId,
            Double amount,
            String accountNumber,
            String cardSecurityValue,
            String expiration,
            String name,
            String address1,
            String address2,
            String address3,
            String city,
            String state,
            String zip) throws Exception {

        RequestIF request = generateBasicRequest(RequestIF.NEW_ORDER_TRANSACTION);
        addNonCaptureFields(request);

        request.setFieldValue("OrderID", orderId);
        request.setFieldValue("AccountNum", accountNumber);

        //if we're using discover or visa we need to include the card security value indicator
        String cardSecurityValInd = null;
        if (accountNumber.startsWith("4") || accountNumber.startsWith("6"))
            cardSecurityValInd = cardSecurityValue == null ? "9" : "1";
        if (cardSecurityValInd != null)
            request.setFieldValue("CardSecValInd", cardSecurityValInd);

        request.setFieldValue("CardSecVal", cardSecurityValue);
        request.setFieldValue("Amount", ((Double)(amount * 100D)).intValue() + "");
        request.setFieldValue("Exp", expiration);

        //AVS Information
        request.setFieldValue("AVSname", name);
        request.setFieldValue("AVSaddress1", address1);
        request.setFieldValue("AVSaddress2", address2);
        request.setFieldValue("AVScity", city);
        request.setFieldValue("AVSstate", state);
        request.setFieldValue("AVSzip", zip);

        return request;
    }

    private ResponseIF retryTx(Exchange exchange) throws Exception {
        return processTxWithRetryLogic(exchange);
    }

    private void processTx(Exchange exchange) throws Exception {
        //String requestXML = request.getXML();
        //System.out.println(requestXML);

        if (testRetryLogic)
            exchange.setResponse(processTxWithRetryLogic(exchange));
        else {
            EngineIF engine = enginePool.acquire();
            ResponseIF response = engine.execute(exchange.getRequest());
            exchange.setResponse(response);
        }
    }

    private ResponseIF processTxWithRetryLogic(Exchange exchange) throws Exception {
        PaymentechExecutor executor = new PaymentechExecutor();
        RequestIF request = exchange.getRequest();

        executor.setEngine(enginePool.acquire());
        executor.setRequest(request);

        mapTraces.put(request.getField("OrderID"), request.getTraceNumber());

        Thread t = new Thread(executor);
        t.start();

        ResponseIF response = null;
        long start = System.currentTimeMillis();

        //System.out.print(request.getField("OrderID"));
        while (executor.isRunning()) {
            //System.out.print(".");
            Thread.sleep(50);

            if (!executor.isRunning())
                response = executor.getResponse();

            //if we don't have a response yet check to see if we're past the timeout.. if we are, break out of the loop,
            //we didn't get a good response
            if (response == null) {
                long waitUntil = start + timeout;
                long now = System.currentTimeMillis();
                if (now > waitUntil) {
                    System.out.println(request.getField("OrderId") +  ": Abandoning");
                    break;

                }
            }
        }
        return response;
    }

    public Exchange voidTransaction(Exchange original, boolean isVoidOfCapturedTransaction) throws Exception {
        Exchange exchange = new Exchange();
        exchange.setRequest(reverseRequest(original.getRequest().getField("OrderID"), original.getResponse().getTxRefNum(), isVoidOfCapturedTransaction));
        processTx(exchange);
        return exchange;
   }

    public Exchange authCaptureTransaction
            (String orderId, Double amount, String accountNumber, String cardSecurityValue, String expiration, String name, String address1, String address2, String address3, String city, String state, String zip) throws Exception {

        Exchange exchange = new Exchange();
        RequestIF request = authXRequest(orderId, amount, accountNumber, cardSecurityValue, expiration, name, address1, address2, address3, city, state, zip);
        request.setFieldValue("MessageType", "AC");

        exchange.setRequest(request);
        processTx(exchange);
        return exchange;
    }

    public Exchange authTransaction
            (String orderId, Double amount, String accountNumber, String cardSecurityValue, String expiration, String name, String address1, String address2, String address3, String city, String state, String zip) throws Exception {
        Exchange exchange = new Exchange();
        RequestIF request = authXRequest(orderId, amount, accountNumber, cardSecurityValue, expiration, name, address1, address2, address3, city, state, zip);
        request.setFieldValue("MessageType", "A");

        exchange.setRequest(request);
        processTx(exchange);
        return exchange;
    }

    public Exchange captureTransaction(Exchange exchange) throws Exception {
        Double amount = Double.parseDouble(exchange.getRequest().getField("Amount")) / 100;
        RequestIF request = captureRequest(exchange.getRequest().getField("OrderID"), exchange.getResponse().getTxRefNum(), amount);
        exchange.setRequest(request);
        processTx(exchange);
        return exchange;
    }


    public Exchange captureTransaction
            (Exchange exchange, Double amount) throws Exception {

        RequestIF request = captureRequest(exchange.getRequest().getField("OrderID"), exchange.getResponse().getTxRefNum(), amount);
        exchange.setRequest(request);
        processTx(exchange);
        return exchange;
    }

    public Exchange captureTransaction(String orderId, Double amount, String transactionReferenceNumber) throws Exception {
        Exchange exchange = new Exchange();
        RequestIF request = captureRequest(orderId, transactionReferenceNumber, amount);
        exchange.setRequest(request);
        processTx(exchange);
        return exchange;
    }

    protected Map processResponse(ResponseIF response, Map parameters)
        throws Exception
    {
        if(response.isError())
            return processErrorResponse(response, parameters);
        if(response.isApproved())
            return processSuccessResponse(response, parameters);
        else
            return processDeclinedResponse(response, parameters);
    }

    protected Map processSuccessResponse(ResponseIF response, Map parameters)
        throws Exception
    {
        Map result = new HashMap();
        result.put("resultCode", "0");
        result.put("resultMessage", response.getMessage());

        //do something with a success response from Chase..
        /*
        Payment payment = (Payment)getBusinessObjectFactory().createObject("paymentImplementer");
        BeanUtils.copyProperties(payment, parameters);
        payment.setStatus("Received");
        try
        {
            String amountS = response.getValue("FlexRedeemedAmount");
            BigDecimal bdAmount = new BigDecimal(amountS);
            Double amountD = Double.valueOf(bdAmount.divide(new BigDecimal(100)).doubleValue());
            payment.setAmount(amountD);
        }
        catch(Exception e)
        {
            if(log.isWarnEnabled())
                log.warn("Error parsing payment amount: ", e);
        }
        payment.setCreated(formatDateTime(new Date()));
        payment.setProcessorClassName(getClass().getName());
        payment.setCreditCardNumberDisplay(response.getValue("AccountNum"));
        payment.setValueFieldLabels("TRANSACTION_TYPE,AMOUNT,CARD_CODE,RESPONSE_CODE,AUTHCODE,TRANSACTION_ID,NEW_BALANCE");
        payment.setValue1(response.getValue("CardBrand"));
        payment.setValue2(response.getValue("FlexRedeemedAmount"));
        payment.setValue3((String)parameters.get("cardSecurityValue"));
        payment.setValue4(response.getValue("RespCode"));
        payment.setValue5(response.getValue("AuthCode"));
        payment.setValue6(response.getValue("TxRefNum"));
        payment.setValue7(response.getValue("FlexAcctBalance"));
        payment.setOrder(getUser().getOrder());
        if(getUser().getOrder().getPayments() == null)
            getUser().getOrder().setPayments(new LinkedHashSet());
        getUser().getOrder().getPayments().add(payment);
        PaymentDAO paymentDAO = (PaymentDAO)getDaoFactory().createDAO("paymentDAOImplementer");
        paymentDAO.setPayment(payment);
        paymentDAO.insertPayment(false);
        result.put("newPayment", payment);
        return result;
        */
        return null;
    }

    protected Map processDeclinedResponse(ResponseIF response, Map parameters)
        throws Exception
    {
        Map result = new HashMap();
        result.put("resultCode", response.getValue("RespCode"));
        result.put("resultMessage", response.getValue("StatusMsg"));
        return result;
    }

    protected Map processErrorResponse(ResponseIF response, Map parameters)
        throws Exception
    {
        Map result = new HashMap();
        result.put("resultCode", response.getValue("RespCode"));
        result.put("resultMessage", response.getValue("StatusMsg"));
        return result;
    }

    private String generateOrderNumber(String input) {
        String generatedOrderNumber = (String)orderNumbersByKey.get(input);
        if (generatedOrderNumber == null) {
            generatedOrderNumber = (currentOrderNumber + "") + input;
            currentOrderNumber++;
            orderNumbersByKey.put(input, generatedOrderNumber);
        }
        return generatedOrderNumber;
    }

    private void doProductionTestScript() throws Exception {
        String name = "Mike McElligott";
        String address1 = "260 Chattanooga";
        String city = "San Francisco";
        String state = "CA";

        Exchange exchange;

        //visa
        exchange = authTransaction(generateOrderNumber("A1"), 30.00, "4788250000028291", "111", "0913", name, address1, null, null, city, state, "11111");
        printResponseCodes(generateOrderNumber("A1"), exchange.getResponse());
        exchange = authTransaction(generateOrderNumber("A2"), 38.01, "4788250000028291", null, "0913", name, address1, null, null, city, state, "L6L2X9");
        printResponseCodes(generateOrderNumber("A2"), exchange.getResponse());
        exchange = authTransaction(generateOrderNumber("A3"), 00.00, "4788250000028291", null, "0913", name, address1, null, null, city, state, "666666");
        printResponseCodes(generateOrderNumber("A3"), exchange.getResponse());
    }

    private void doRetryLogicScript() throws Exception {
        String name = "Mike McElligott";
        String address1 = "628 Indiana Avenue";
        String city = "Venice";
        String state = "CA";

        Exchange exchange;

        //visa
        exchange = authCaptureTransaction(generateOrderNumber("F1a"), 10.00, "4788250000028291", null, "0913", name, address1, null, null, city, state, "L6L2X9");
        printResponseCodes(generateOrderNumber("F1a"), exchange.getResponse(), true);
        retryTx(exchange);
        exchange.printResponseCodes();

        //mc
        exchange = authCaptureTransaction(generateOrderNumber("F2a"), 15.00, "5454545454545454", null, "0913", name, address1, null, null, city, state, "L6L2X9");
        exchange.printResponseCodes();
        retryTx(exchange);
        exchange.printResponseCodes();

        //amex
        exchange = authCaptureTransaction(generateOrderNumber("F3a"), 15.00, "371449635398431", null, "0913", name, address1, null, null, city, state, "L6L2X9");
        exchange.printResponseCodes();
        retryTx(exchange);
        exchange.printResponseCodes();

        //discover
        exchange = authCaptureTransaction(generateOrderNumber("F4a"), 10.00, "6011000995500000", null, "0913", name, address1, null, null, city, state, "L6L2X9");
        exchange.printResponseCodes();
        retryTx(exchange);
        exchange.printResponseCodes();

        //jcb
        exchange = authCaptureTransaction(generateOrderNumber("F5a"), 10.00, "3566002020140006", null, "0913", name, address1, null, null, city, state, "L6L2X9");
        exchange.printResponseCodes();
        retryTx(exchange);
        exchange.printResponseCodes();
    }

    private void doAuthOnlyScript() throws Exception {
        String name = "Mike McElligott";
        String address1 = "628 Indiana Avenue";
        String city = "Venice";
        String state = "CA";

        Exchange exchange;

        //visa
        exchange = authTransaction(generateOrderNumber("A1a"), 30.00, "4788250000028291", "111", "0913", name, address1, null, null, city, state, "11111");
        exchange.printResponseCodes();
        if (exchange.getResponse() != null) {
            voidTransaction(exchange, true);
            exchange.printResponseCodes();
        }

        exchange = authTransaction(generateOrderNumber("A2"), 38.01, "4788250000028291", null, "0913", name, address1, null, null, city, state, "L6L2X9");
        exchange.printResponseCodes();

        exchange = authTransaction(generateOrderNumber("A3a"), 85.00, "4788250000028291", "222", "0913", name, address1, null, null, city, state, "22222");
        exchange.printResponseCodes();
        if (exchange.getResponse() != null) {
            //System.out.println("A3b");
            exchange = captureTransaction(exchange, 85.00);
            exchange.printResponseCodes();
        }

        exchange = authTransaction(generateOrderNumber("A4"), 0.00, "4788250000028291", null, "0913", name, address1, null, null, city, state, "66666");
        exchange.printResponseCodes();

        exchange = authTransaction(generateOrderNumber("A5a"), 125.00, "4788250000028291", "555", "0913", name, address1, null, null, city, state, "11111");
        exchange.printResponseCodes();
        exchange = captureTransaction(exchange, 75.00);
        exchange.printResponseCodes();

        //mc
        exchange = authTransaction(generateOrderNumber("A6a"), 41.00, "5454545454545454", null, "0913", name, address1, null, null, city, state, "L6L2X9");
        exchange.printResponseCodes();
        if (exchange.getResponse() != null) {
            exchange = voidTransaction(exchange, true);
            exchange.printResponseCodes();
        }

        exchange = authTransaction(generateOrderNumber("A7"), 11.02, "5454545454545454", "666", "0913", name, address1, null, null, city, state, "88888");
        exchange.printResponseCodes();


        exchange = authTransaction(generateOrderNumber("A8a"), 70.00, "5454545454545454", "666", "0913", name, address1, null, null, city, state, "L6L2X9");
        exchange.printResponseCodes();
        if (exchange.getResponse() != null) {
            exchange = captureTransaction(exchange, 70.00);
            exchange.printResponseCodes();
        }

        exchange = authTransaction(generateOrderNumber("A9a"), 100.00, "5454545454545454", "222", "0913", name, address1, null, null, city, state, "55555");
        exchange.printResponseCodes();
        exchange = captureTransaction(exchange, 60.00);
        exchange.printResponseCodes();

        exchange = authTransaction(generateOrderNumber("A10"), 0.00, "5454545454545454", null, "0913", name, address1, null, null, city, state, "88888");
        exchange.printResponseCodes();

        //amex
        exchange = authTransaction(generateOrderNumber("A11a"), 1055.00, "371449635398431", null, "0913", name, address1, null, null, city, state, "L6L2X9");
        exchange.printResponseCodes();
        exchange = captureTransaction(exchange,  500.00);
        exchange.printResponseCodes();

        exchange = authTransaction(generateOrderNumber("A12a"), 55.00, "371449635398431", null, "0913", name, address1, null, null, city, state, "44444");
        exchange.printResponseCodes();
        exchange = voidTransaction(exchange, true);
        exchange.printResponseCodes();

        exchange = authTransaction(generateOrderNumber("A13a"), 75.00, "371449635398431", "2222", "0913", name, address1, null, null, city, state, "66666");
        exchange.printResponseCodes();
        exchange = captureTransaction(exchange);
        exchange.printResponseCodes();

        exchange = authTransaction(generateOrderNumber("A14"), 0.00, "371449635398431", null, "0913", name, address1, null, null, city, state, "22222");
        exchange.printResponseCodes();


        //discover
        exchange = authTransaction(generateOrderNumber("A15a"), 10.00, "6011000995500000", null, "0913", name, address1, null, null, city, state, "77777");
        exchange.printResponseCodes();
        exchange = captureTransaction(exchange);
        exchange.printResponseCodes();

        exchange = authTransaction(generateOrderNumber("A16a"), 15.00, "6011000995500000", null, "0913", name, address1, null, null, city, state, "77777");
        exchange.printResponseCodes();
        exchange = voidTransaction(exchange, true);
        exchange.printResponseCodes();

        exchange = authTransaction(generateOrderNumber("A17"), 63.03, "6011000995500000", "444", "0913", name, address1, null, null, city, state, "L6L2X9");
        exchange.printResponseCodes();
        exchange = authTransaction(generateOrderNumber("A18"), 00.00, "6011000995500000", null, "0913", name, address1, null, null, city, state, "11111");
        exchange.printResponseCodes();
        exchange = authTransaction(generateOrderNumber("A19"), 29.00, "3566002020140006", null, "0913", name, address1, null, null, city, state, "33333");
        exchange.printResponseCodes();
    }

    private void doAuthCaptureScript() throws Exception {
        String name = "Mike McElligott";
        String address1 = "260 Chattanooga";
        String city = "San Francisco";
        String state = "CA";

        Exchange exchange;
        exchange = authCaptureTransaction(generateOrderNumber("B1a"), 30.00, "4788250000028291", "111", "0913",
                name, address1, null, null, city, state, "11111");
        exchange.printResponseCodes();
        exchange = voidTransaction(exchange, false);
        exchange.printResponseCodes();

        exchange = authCaptureTransaction(generateOrderNumber("B2"), 38.01, "4788250000028291", null, "0913",
                name, address1, null, null, city, state, "L6L2X9");
        exchange.printResponseCodes();

        exchange = authCaptureTransaction(generateOrderNumber("B3"), 85.00, "4788250000028291", "222", "0913",
                name, address1, null, null, city, state, "22222");
        exchange.printResponseCodes();

        exchange = authCaptureTransaction(generateOrderNumber("B4"), 38.01, "4788250000028291", "555", "0913",
                name, address1, null, null, city, state, "11111");
        exchange.printResponseCodes();

        exchange = authCaptureTransaction(generateOrderNumber("B5a"), 41.00, "5454545454545454", null, "0913",
                name, address1, null, null, city, state, "L6L2X9");
        exchange.printResponseCodes();
        exchange = voidTransaction(exchange, false);
        exchange.printResponseCodes();

        exchange = authCaptureTransaction(generateOrderNumber("B6"), 11.02, "5454545454545454", "666", "0913",
                name, address1, null, null, city, state, "88888");
        exchange.printResponseCodes();

        exchange = authCaptureTransaction(generateOrderNumber("B7"), 70.00, "5454545454545454", "666", "0913",
                name, address1, null, null, city, state, "L6L2X9");
        exchange.printResponseCodes();

        exchange = authCaptureTransaction(generateOrderNumber("B8"), 100.00, "5454545454545454", "222", "0913",
                name, address1, null, null, city, state, "55555");
        exchange.printResponseCodes();

        exchange = authCaptureTransaction(generateOrderNumber("B9"), 1055.00, "371449635398431", null, "0913",
                name, address1, null, null, city, state, "L6L2X9");
        exchange.printResponseCodes();

        exchange = authCaptureTransaction(generateOrderNumber("B10a"), 55.00, "371449635398431", null, "0913",
                name, address1, null, null, city, state, "44444");
        exchange.printResponseCodes();
        exchange = voidTransaction(exchange, false);
        exchange.printResponseCodes();


        exchange = authCaptureTransaction(generateOrderNumber("B11"), 75.00, "371449635398431", "2222", "0913",
                name, address1, null, null, city, state, "66666");
        exchange.printResponseCodes();

        exchange = authCaptureTransaction(generateOrderNumber("B12"), 10.00, "6011000995500000", null, "0913",
                name, address1, null, null, city, state, "77777");
        exchange.printResponseCodes();

        exchange = authCaptureTransaction(generateOrderNumber("B13a"), 15.00, "6011000995500000", null, "0913",
                name, address1, null, null, city, state, "77777");
        exchange.printResponseCodes();

        exchange = voidTransaction(exchange, false);
        exchange.printResponseCodes();

        exchange = authCaptureTransaction(generateOrderNumber("B14"), 63.03, "6011000995500000", "444", "0913",
                name, address1, null, null, city, state, "L6L2X9");
        exchange.printResponseCodes();

        exchange = authCaptureTransaction(generateOrderNumber("B15"), 29.00, "6011000995500000", null, "0913",
                name, address1, null, null, city, state, "33333");
        exchange.printResponseCodes();
    }

    private void doCaptureScript() throws Exception {
        Exchange exchange;
        exchange = captureTransaction(generateOrderNumber("A1"), 30.00, ((ResponseIF)responses.get("A1")).getTxRefNum());
        exchange.printResponseCodes();
        exchange = captureTransaction(generateOrderNumber("A6"), 41.00, ((ResponseIF)responses.get("A6")).getTxRefNum());
        exchange.printResponseCodes();
        exchange = captureTransaction(generateOrderNumber("A11"), 1055.00, ((ResponseIF)responses.get("A11")).getTxRefNum());
        exchange.printResponseCodes();
        exchange = captureTransaction(generateOrderNumber("A14"), 10.00, ((ResponseIF)responses.get("A14")).getTxRefNum());
        exchange.printResponseCodes();
    }

    private void doGiftCardScript(boolean addValue) throws Exception {
        Map map = new HashMap();
        if (addValue)
            addValueTransaction(map, "1", 1000.00, "6035718888921004777", "4414");

        Exchange exchange;

        exchange = redemptionTransaction(map, generateOrderNumber("K6"), 98.00, "6035718888921004777", "4414");
        exchange.printResponseCodes();
        exchange = redemptionTransaction(map, generateOrderNumber("K7"), 40.00,  "6035718888921004777", "4414");
        exchange.printResponseCodes();
        exchange = redemptionTransaction(map, generateOrderNumber("K8"), 56.00,  "6035718888921004777", "4414");
        exchange.printResponseCodes();
        exchange = balanceInquiryTransaction(map, generateOrderNumber("K9"), "6035718888921004777", "4414");
        exchange = balanceInquiryTransaction(map, generateOrderNumber("K9"), "6035718888921004819", "9117");
        exchange.printResponseCodes();
    }

    public void doScripts() throws Exception {
        doAuthOnlyScript();
        System.out.println();
        doAuthCaptureScript();
        System.out.println();
        doRetryLogicScript();

        //doCaptureScript();
        //doGiftCardScript(false);
        //doProductionTestScript();
    }

    public static void main(String args[]) {

        try {
            if (true) {
                PaymentechProcessor processor = new PaymentechProcessor();
                processor.doScripts();
            } else if (false) {
                BigDecimal start = new BigDecimal(1999 - 500);
                BigDecimal result = start.divide(new BigDecimal(100));
                System.out.println(result);
                PaymentechProcessor processor = new PaymentechProcessor();
                Exchange exchange = processor.giftCardVoidTransaction(null,"3302", "6035710216840000155", "4990", "4D83FCC241E978DD365229569EA56F21EAE15305");
                System.out.println(exchange.getResponse().getResponseCode());
            } else {
                PaymentechProcessor processor = new PaymentechProcessor();
                Exchange exchange = processor.balanceInquiryTransaction(null, processor.generateOrderNumber("PROD1"), "6035710216840000155", "4990");
                System.out.println("got response");
                exchange = processor.redemptionTransaction(null, processor.generateOrderNumber("PROD1"), 1.00, "6035710216840000155", "4990");
                String txRefNum = exchange.getResponse().getValue("TxRefNum");
                exchange = processor.giftCardVoidTransaction(null, processor.generateOrderNumber("PROD1"), "6035710216840000155", "4990", txRefNum);
                System.out.println("got response");
            }
            //processor.doGiftCardScript(false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}