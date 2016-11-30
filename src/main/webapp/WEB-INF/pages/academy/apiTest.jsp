<!DOCTYPE html>
<html>
<head>
    <title></title>
    <style>
        label.ui-input-text {text-align:right}
    </style>
    <link rel="stylesheet" href="https://code.jquery.com/mobile/1.3.2/jquery.mobile-1.3.2.min.css" />
    <script src="https://code.jquery.com/jquery-1.9.1.min.js"></script>
    <script src="https://code.jquery.com/mobile/1.3.2/jquery.mobile-1.3.2.min.js"></script>
    <script src="https://ajax.aspnetcdn.com/ajax/jquery.validate/1.11.1/jquery.validate.min.js"></script>
    <script src="../resources/js/academy-api.js"></script>
</head>
<body>
    <div data-role="page" data-control-title="Test Harness" id="pageHarness">
        <div data-theme="b" data-role="header">
            <h3>
                California Academy Test Harness
            </h3>
        </div>
        <div data-role="content">
            <form id='formRegisterContact' action="">
            <div data-controltype="textblock">
                <p>
                    <b>
                        Fill out the form below to submit a contact.  If you wish to associate interests
                        with the submitted contact, click the 'Register Contact And Interests' button.  If you
                        wish to only submit the contact without any interests click the 'Register Contact Only' button.
                    </b>
                </p>
            </div>

            <div class="ui-bar ui-bar-b">Registration Tests</div>
            <div data-role="fieldcontain" data-controltype="textinput">
                <label for="firstname">First Name</label>
                <input name="firstname" id="firstname" placeholder="Diane" value=""
                type="text" class="required">
            </div>
            <div data-role="fieldcontain" data-controltype="textinput">
                <label for="lastname">Last Name</label>
                <input name="lastname" id="lastname" placeholder="Gregorio" value=""
                type="text" class="required">
            </div>
            <div data-role="fieldcontain" data-controltype="textinput">
                <label for="email">Email</label>
                <input name="email" id="email" placeholder="Email" value="" type="text" class="required">
            </div>
            <div data-role="fieldcontain" data-controltype="textinput">
                <label for="zip">Zip</label>
                <input name="zip" id="zip" placeholder="Zip" placeholder="90210" value="" type="text">
            </div>
            <div data-role="fieldcontain" data-controltype="textinput">
                <label for="source">Source</label>
                <input name="source" id="source" placeholder="Source" placeholder="penguins" value="" type="text">
            </div>
            <div data-role="fieldcontain" data-controltype="textinput">
                <label for="interests">Interests</label>
                <input name="interests" id="interests" placeholder="Nightlife,Newsletter" value="" type="text" class="required">
            </div>
            <input id='btnSubmitContact' type="submit" value="Register Contact Only"/>
            <p/>
            <input id='btnSubmitInterests' type="submit" value="Register Contact And Interests"/>
            <p/>
            <div class="ui-bar ui-bar-b">Events Retrieval Test</div>
            <div data-role="fieldcontain" data-controltype="textinput">
                <label for="startDate">Start</label>
                <input name="startDate" id="startDate" placeholder="2014-02-28 00:00:00" value="" type="text" class="required">
            </div>
            <div data-role="fieldcontain" data-controltype="textinput">
                <label for="endDate">End</label>
                <input name="endDate" id="endDate" placeholder="2014-03-02 00:00:00" value="" type="text" class="required">
            </div>
            <div data-role="fieldcontain" data-controltype="textinput">
                <label for="resourceId">Resource ID</label>
                <input name="resourceId" id="resourceId" placeholder="73" value="" type="text" class="required">
            </div>
            <input id='btnGetEventsForDateRange' type="submit" value="Get Events For Date Range"/>
            </form>
        </div>
    </div>

    <div data-role="page" id="pageEventDetail" data-add-back-btn="true">
        <div data-theme="b" data-role="header">
            <h3>
                Test Harness - Event Detail
            </h3>
        </div>
        <div data-role="fieldcontain" data-controltype="textinput">
            <label for="event-id">Event ID</label>
            <input name="eventId" id="event-id" value="" type="text" class="required">
        </div>
        <div data-role="fieldcontain" data-controltype="textinput">
            <label for="event-name">Event Name</label>
            <input name="eventName" id="event-name" value="" type="text" class="required">
        </div>
        <div data-role="fieldcontain" data-controltype="textinput">
            <label for="event-startDateTime">Start</label>
            <input name="startDateTime" id="event-startDateTime" value="" type="text" class="required">
        </div>
        <div data-role="fieldcontain" data-controltype="textinput">
            <label for="event-endDateTime">End</label>
            <input name="endDateTime" id="event-endDateTime" value="" type="text" class="required">
        </div>
        <div data-role="fieldcontain" data-controltype="textinput">
            <label for="event-resourceId">Resource ID</label>
            <input name="resourceId" id="event-resourceId" value="" type="text" class="required">
        </div>
        <div data-role="fieldcontain" data-controltype="textinput">
            <label for="event-onSaleDateTime">Start</label>
            <input name="onSaleDateTime" id="event-onSaleDateTime" value="" type="text" class="required">
        </div>
        <div data-role="fieldcontain" data-controltype="textinput">
            <label for="event-offSaleDateTime">End</label>
            <input name="offSaleDateTime" id="event-offSaleDateTime" value="" type="text" class="required">
        </div>
        <div data-role="fieldcontain" data-controltype="textinput">
            <label for="event-userEventNumber">User Event #</label>
            <input name="userEventNumber" id="event-userEventNumber" value="" type="text" class="required">
        </div>
        <div data-role="fieldcontain" data-controltype="textinput">
            <label for="event-available">Available</label>
            <input name="available" id="event-available" value="" type="text" class="required">
        </div>
        <div data-role="fieldcontain" data-controltype="textinput">
            <label for="event-status">Status</label>
            <input name="status" id="event-status" value="" type="text" class="required">
        </div>
        <div data-role="fieldcontain" data-controltype="textinput">
            <label for="event-roster">Roster</label>
            <input name="roster" id="event-roster" value="" type="text" class="required">
        </div>
        <div data-role="fieldcontain" data-controltype="textinput">
            <label for="event-private">Private</label>
            <input name="private" id="event-private" value="" type="text" class="required">
        </div>

    </div>
    <div data-role="page" data-control-title="Success" id="pageSuccess" data-add-back-btn="true">
         <div data-theme="b" data-role="header">
             <h3>
                 Test Harness - Registration Success
             </h3>
         </div>
         <div data-role="content">
             Your registration was successful.
             <p/>
             The contact id is <span id="contactId"></span>.<br/>
             The corresponding message was '<span class="successMessage"></span>'.
             <p/>
             <input class='buttonShowHarness' type="button" value="Show Test Harness">
         </div>
     </div>

    <div data-role="page" data-control-title="Error" id="pageError" data-add-back-btn="true">
        <div data-theme="b" data-role="header">
            <h3>
                Test Harness - Registration Error
            </h3>
        </div>
        <div data-role="content">
            Sorry, there was an error processing your submission.
            <p/>
            The error message was '<span class="errorMessage"></span>'.
            <p/>
            <input class='buttonShowHarness' type="button" value="Show Test Harness">
        </div>
    </div>

    <div data-role="page" data-control-title="Failure" id="pageFailure" data-add-back-btn="true">
        <div data-theme="b" data-role="header">
            <h3>
                Test Harness - Registration Failure
            </h3>
        </div>
        <div data-role="content">
            Sorry, the contact registration failed.
            <p/>
            The associated failure message was '<span id="failureMessage"></span>'.
            <p/>
            <input class='buttonShowHarness' type="button" value="Show Test Harness">
        </div>
    </div>

    <div data-role="page" data-control-title="Events Retrieved" id="pageEventsSuccess" data-add-back-btn="true">
        <div data-theme="b" data-role="header">
            <h3>
                Test Harness - Events Retrieved
            </h3>
        </div>
        <div data-role="content">
            <ul data-role="listview" id="events" data-filter="true" data-filter placeholder="Search Records..."></ul>
        </div>
    </div>

    <div data-role="page" data-control-title="Error" id="pageEventsFailure" data-add-back-btn="true">
        <div data-theme="b" data-role="header">
            <h3>
                Test Harness - Events Failure
            </h3>
        </div>
        <div data-role="content">
            Sorry, the event load process failed.
            <p/>
            The associated failure message was '<span class="failureMessage"></span>'.
            <p/>
        </div>
    </div>

    <div data-role="page" data-control-title="Error" id="pageEventsError" data-add-back-btn="true">
        <div data-theme="b" data-role="header">
            <h3>
                Test Harness - Events Error
            </h3>
        </div>
        <div data-role="content">
            Sorry, there was an error loading events.
            <p/>
            The error message was '<span class="errorMessage"></span>'.
            <p/>
        </div>
    </div>

</body>
<script>

    function registerContactOnly(evt) {
        evt.preventDefault();

        //you can get these any way you see fit... using the input[name=???] style, or using $("#firstname").val(), etc.
        var form = $("#formRegisterContact");
        var firstname 	= form.find('input[name=firstname]').val();
        var lastname 	= form.find('input[name=lastname]').val();
        var email 		= form.find('input[name=email]').val();
        var phone 		= form.find('input[name=phone_mobile]').val();
        var zip 		= form.find('input[name=zipcode]').val();
        var source 		= form.find('input[name=source]').val();

        AcademyApi.ajaxRegisterContact(
            firstname, lastname, email, phone, zip, source,
                function(apiCall) {
                    if (apiCall.result == "success")
                        $.mobile.changePage( "#pageSuccess", { transition: "slide"});
                    else if (apiCall.result == "failure") {
                        $.mobile.changePage( "#pageFailure", { transition: "slide"});
                        $('#pageFailure .failureMessage').html(apiCall.message);
                    }
                    else if (apiCall.result == "error") {
                        $.mobile.changePage( "#pageError", { transition: "slide"});
                        $('#pageError .errorMessage').html(apiCall.message);
                    }
                }
            );
    }

    function registerContactAndInterests(evt) {
        evt.preventDefault();

        var form = $("#formRegisterContact");
        var firstname 	= form.find('input[name=firstname]').val();
        var lastname 	= form.find('input[name=lastname]').val();
        var email 		= form.find('input[name=email]').val();
        var phone 		= form.find('input[name=phone_mobile]').val();
        var zip 		= form.find('input[name=zipcode]').val();
        var source 		= form.find('input[name=source]').val();

        var interests   = form.find('input[name=interests]').val().split(",");

        AcademyApi.ajaxRegisterNewsletterInterests(
                firstname, lastname, email, phone, zip, source, interests,
                function(apiCall) {
                    if (apiCall.result == "success") {
                        $.mobile.changePage( "#pageSuccess", { transition: "slide"});
                        $("#contactId").html(apiCall.contactId);
                        $("#successMessage").html(apiCall.message);
                    }
                    else if (apiCall.result == "failure") {
                        $.mobile.changePage( "#pageFailure", { transition: "slide"});
                        $('#pageFailure .failureMessage').html(apiCall.message);
                    }
                    else if (apiCall.result == "error") {
                        $.mobile.changePage( "#pageError", { transition: "slide"});
                        $('#pageError .errorMessage').html(apiCall.message);
                    }
                }
        );
    }

    function getEventsForDateRange(evt) {
        evt.preventDefault();

        var form = $("#formRegisterContact");
        var start 	= form.find('input[name=startDate]').val();
        var end 	= form.find('input[name=endDate]').val();
        var resourceId = form.find('input[name=resourceId]').val()

        AcademyApi.ajaxGetEventsForDateRange(start, end, resourceId,
            function(apiCall) {
                if (apiCall.result == "success") {

                    $.mobile.changePage( "#pageEventsSuccess", { transition: "slide"});
                    //@todo we need to display a list of events and allow the user to drill down
                    var listHtml = "";
                    for (var i = 0; i < apiCall.events.length; i++) {
                        var event = apiCall.events[i];
                        listHtml += "<li>[" + event.startDateTime + "] - " + event.name + "</li>";
                    }

                    $("#pageEventsSuccess #events").html(listHtml);
                    $("#events").listview("refresh");

                    $('#events').delegate('li', 'tap', function (event) {
                        event.preventDefault();

                        //show the event detail form, then just update the fields on the form directly from the
                        //properties sent back by the API (fields have been named to match with the prefix "event-")
                        $.mobile.changePage("#pageEventDetail",{ transition: "slide"});
                        var index = $(this).closest('li').index();
                        var evt = apiCall.events[index];

                        for (var prop in evt) {
                            $('#event-' + prop).val(evt[prop]);
                        }
                    });

                } else if (apiCall.result == "failure") {
                    $.mobile.changePage( "#pageEventsFailure", { transition: "slide"});
                    $('#failureMessage').html(apiCall.message);
                }
                else if (apiCall.result == "error") {
                    $.mobile.changePage( "#pageEventsError", { transition: "slide"});
                    $('#errorMessage').html(apiCall.message);
                }
            }
        );
    }

    $(function() {
        AcademyApi.initialize();
        $("#btnGetEventsForDateRange").click(getEventsForDateRange);
        $("#btnSubmitContact").click(registerContactOnly);
        $("#btnSubmitInterests").click(registerContactAndInterests);
        $(".buttonShowHarness").click(function() {
            $.mobile.changePage( "#pageHarness", { transition: "slide", reverse: "true"});
        });

        $.mobile.changePage( "#pageHarness", { transition: "slide"});
    })
</script>

</html>