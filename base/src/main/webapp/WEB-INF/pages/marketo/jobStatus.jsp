<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
<head>
    <meta http-equiv="refresh" content="1;url=jobStatus?uuid=${uuid}">
    <title></title>
    <style>
        * {font-family:Arial,sans-serif;font-size:.9em}
        .worker {margin:10px;padding:10px;display:inline-block;width:260px;height:80px;border-radius:5px;border-width:2px;border-style:solid;border-color:#aaa}
        .property {width:100%}
        .label {display:inline-block;width: 100px;text-align:right}
        .propValue {display:inline-block;width:100px}
    </style>
</head>
<body>
<c:forEach var="worker" items="${workers}" varStatus="status">
    <div class='worker'>
        <div class='property'>
            <div class='label'>Worker:</div>
            <div class='propValue name'><b>${worker.workerName}</b></div>
        </div>
        <div class='property'>
            <div class='label'>Processing:</div>
            <div class='propValue status'><b>${worker.rowsMarked}</b></div>
        </div>
        <div class='property'>
            <div class='label'>Processed:</div>
            <div class='propValue name'><b>${worker.fullRowsProcessed}</b></div>
        </div>
        <div class='property'>
            <div class='label'>Running:</div>
            <div class='propValue running'><b>${worker.running}</b></div>
        </div>
        <div class='property'>
            <div class='label'>Status:</div>
            <div class='propValue status'><b>${worker.status}</b></div>
        </div>
    </div>
</c:forEach>
</body>
</html>