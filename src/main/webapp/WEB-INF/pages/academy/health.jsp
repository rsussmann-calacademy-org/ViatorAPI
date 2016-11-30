<%@ page contentType="application/json;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
{"result" : "ok",
    "webServiceReachable": "${webServiceReachable}",
    "galaxyWebServiceReachable": "${galaxyWebServiceReachable}",
    "galaxyDatabaseReachable": "${galaxyDatabaseReachable}",
    "paymentWebServiceReachable" : "${paymentWebServiceReachable}",
    "contactsDatabaseReachable" : "${contactsDatabaseReachable}",
    "numConnections": "${numConnections}",
    "numConnectionsIdle": "${numConnectionsIdle}",
    "numConnectionsBusy": "${numConnectionsBusy}",
    "numConnectionsOrphaned": "${numConnectionsOrphaned}",
    "galaxyWebServiceUrl": "${galaxyWebServiceUrl}",
    "galaxyDataSourceUrl": "${galaxyDataSourceUrl}",
    "contactsDataSourceUrl": "${contactsDataSourceUrl}"
}