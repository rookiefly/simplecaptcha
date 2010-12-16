<html>
    <head>
		<meta name="layout" content="main" />
        <title>SimpleCaptcha - Spring Stress Test</title>
    </head>
    <body>
        <h1 style="margin-left:20px;">SimpleCaptcha - Spring Stress Test</h1>
        <div style="margin-left:20px;margin-bottom:20px;width:90%">Generate a thousand CAPTCHA images all at once. Intended to test the performance of using Spring to generate CAPTCHAs.</div>
        <div class="dialog" style="margin-left:20px;width:60%;">
            <%-- We have to add a bogus parameter to the URL to keep the browser from caching the image. --%>
            <g:each var="i" in="${(0..<1000)}">
                ${i}<img src="${createLink(action:'simple',controller:'spring',params:[nop:i])}"><br />
            </g:each>
        </div>
    </body>
</html>

