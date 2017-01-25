<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
<head>
    <meta http-equiv="refresh" content="5;url=../jobs/jobStatus?uuid=${uuid}">
    <title></title>
    <style>
        * {font-family:Arial,sans-serif;font-size:.9em}
        .worker {margin:10px;padding:10px;display:inline-block;width:260px;height:80px;border-radius:5px;border-width:2px;border-style:solid;border-color:#aaa}
        .property {width:100%}
        .label {display:inline-block;width: 80px;text-align:right}
        .propValue {display:inline-block;width:150px}
        #progress {margin:10px}
        #complete {border-radius:5px;border-color:#555;border-width:2px;border-style:solid;text-align:center;padding-top:10px;padding-bottom:10px;color:white;font-size:14px;font-weight:bold}
    </style>
</head>
<body>

<div id='progress' style='width:100%'>
    <div id='complete' style='width:${percentageComplete}%;background-color:lightgreen'>${rowsProcessed} / ${rowsLeft + rowsProcessed} (${percentageComplete}%, ${secondsToGo} seconds to go)</div>
</div>

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
        <div class='property'>
            <div class='label'>Pending:</div>
            <div class='propValue status'><b>${worker.rowsToProcess}</b></div>
        </div>
    </div>
</c:forEach>
</body>
</html>