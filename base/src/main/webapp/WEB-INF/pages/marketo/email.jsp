<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <style>
        html, body, div, span, applet, object, iframe,
        h1, h2, h3, h4, h5, h6, p, blockquote, pre,
        a, abbr, acronym, address, big, cite, code,
        del, dfn, em, img, ins, kbd, q, s, samp,
        small, strike, strong, sub, sup, tt, var,
        b, u, i, center,
        dl, dt, dd, ol, ul, li,
        fieldset, form, label, legend,
        table, caption, tbody, tfoot, thead, tr, th, td,
        article, aside, canvas, details, embed,
        figure, figcaption, footer, header, hgroup,
        menu, nav, output, ruby, section, summary,
        time, mark, audio, video {
            margin: 0;
            padding: 0;
            border: 0;
            font-size: 100%;
            font: inherit;
            vertical-align: baseline;
        }
        /* HTML5 display-role reset for older browsers */
        article, aside, details, figcaption, figure,
        footer, header, hgroup, menu, nav, section {
            display: block;
        }
        body {
            line-height: 1;
        }
        ol, ul {
            list-style: none;
        }
        blockquote, q {
            quotes: none;
        }
        blockquote:before, blockquote:after,
        q:before, q:after {
            content: '';
            content: none;
        }
        table {
            border-collapse: collapse;
            border-spacing: 0;
            font-size:10pt;
        }

        .title {margin-top:0px;font-size:24px;background-color:black;color:white;padding:20px}
        .search {background-color:gray;padding:20px}

        .title-query {margin-top:20px}
        .query-results {width:90%;overflow:scroll;min-width:50%}
        body {font-family:sans-serif;}
        .content {padding:20px}
        td {padding:15px; border-style:solid; border-color:lightgray;border-width:1px;border-collapse:collapse}
        tr:first-child { font-weight: bold; font-size:10pt; text-align:center;background-color:lightblue; }
        tr:first-child td {border-color:darkgray}
    </style>
    <script>
        function doSearch() {
            var query = document.getElementById('email').value;
            window.document.location = 'email?q=' + query;
        }
    </script>
</head>
<body>
<div class='title'>Cal Academy IDW Email Lookup</div>
<div class='search'><input id='email' name='email' size="50"/><input id='submit' type='button' onclick="doSearch()" value="Search"/></div>

<div class='content'>
    <c:forEach var="result" items="${results}">
        <div class='title-query'>Records: ${result.title}</div>
        <div class='query-results'>
        <table>
        <c:forEach var="record" items="${result.results}">
            <tr><c:forEach var="column" items="${record}"><td>${column}</td></c:forEach>
            </tr>
        </c:forEach>
        </table>
        </div>
    </c:forEach>
</div>
</body>
</html>
