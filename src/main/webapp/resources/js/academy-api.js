var AcademyApi = {
	urls: {
		registerNewsletterInterests: 'http://japp1.prod.calacademy.org:8080/api/academy/registerNewsletterInterests',
		registerContact: 'http://japp1.prod.calacademy.org:8080/api/academy/registerContact',
        getEventsForDateRange: 'http://japp1.prod.calacademy.org:8080/api/academy/getEventsForDateRange'
        //registerNewsletterInterests: 'http://localhost:8080/academy/registerNewsletterInterests',
        //registerContact: 'http://localhost:8080/academy/registerContact',
        //getEventsForDateRange: 'http://localhost:8080/academy/getEventsForDateRange'
	},
	initialize: function() {
		this.results = {};
        this.results[this.urls.registerNewsletterInterests] = [];
        this.results[this.urls.registerContact] = [];
	},
    registerContact: function(config, callback) {
        this.ajaxRegisterContact(config.firstname, config.lastname, config.email, config.phone, config.zip, config.source, callback);
    },
    ajaxRegisterContact: function(firstname, lastname, email, phone, zip, source, funcCallback) {
    	var data = {firstname: firstname, lastname:lastname, email:email, phone:phone, zip:zip, source: source};
    	$.ajax({
    		url: AcademyApi.urls.registerContact,
    		data: data,
    		dataType: 'jsonp',
    		success: function(data, status, jqXHR) {
                AcademyApi.lastAjaxResult = {};
                AcademyApi.lastAjaxResult.data = data;
                AcademyApi.lastAjaxResult.status = status;
                AcademyApi.lastAjaxResult.jqXHR = jqXHR;

    			AcademyApi.results[AcademyApi.urls.registerContact].push(data);
                if (data.result == "success" && funcCallback)
                    funcCallback(data);
                else if (data.result == "failure" && funcCallback)
                    funcCallback(data);
    		},
    		error: function(jqXHR, textStatus, errorThrown) {
                AcademyApi.lastAjaxResult = {};
                AcademyApi.lastAjaxResult.jqXHR = jqXHR;
                AcademyApi.lastAjaxResult.textStatus = textStatus;
                AcademyApi.lastAjaxResult.errorThrown = errorThrown;

                if (funcCallback)
                    funcCallback({result: "error", message: errorThrown});
    		}
    	});
    },
    registerNewsletterInterests: function(config, callback) {
        this.ajaxRegisterNewsletterInterests(config.firstname, config.lastname, config.email, config.phone, config.zip, config.source, config.interests, callback);
    },
    ajaxRegisterNewsletterInterests: function(firstname, lastname, email, phone, zip, source, interests, funcCallback) {
        //trim off any leading or trailing spaces
        for (var i = 0; i < interests.length; i++)
            interests[i] = interests[i].trim();

        var data = {firstname: firstname, lastname:lastname, email:email, phone:phone, zip:zip, source: source, interests: interests.join()};
        $.ajax({
            url: AcademyApi.urls.registerNewsletterInterests,
            data: data,
            dataType: 'jsonp',
            success: function(data, status, jqXHR) {
                AcademyApi.lastAjaxResult = {};
                AcademyApi.lastAjaxResult.data = data;
                AcademyApi.lastAjaxResult.status = status;
                AcademyApi.lastAjaxResult.jqXHR = jqXHR;

                AcademyApi.results[AcademyApi.urls.registerNewsletterInterests].push(data);
                if (data.result == "success" && funcCallback)
                  funcCallback(data);
                else if (data.result == "failure" && funcCallback)
                  funcCallback(data);
            },
            error: function(jqXHR, textStatus, errorThrown) {
                AcademyApi.lastAjaxResult = {};
                AcademyApi.lastAjaxResult.jqXHR = jqXHR;
                AcademyApi.lastAjaxResult.textStatus = textStatus;
                AcademyApi.lastAjaxResult.errorThrown = errorThrown;

                if (funcCallback)
                    funcCallback({result: "error", message: errorThrown});
            }
        });
    },
    getEventsForDateRange: function(config, callback) {
        var start, end;

        if (config.start != null && config.start instanceof Date)
            start = this.formatDate(config.start);
        else
            start = config.start;

        if (config.end != null && config.end instanceof Date)
            end = this.formatDate(config.end);
        else
            end = config.end;

        var data = {};
        data.start = start;
        data.end = end;

        if (config.resourceId)
            data.resourceId = config.resourceId;
        if (config.eventTypeName)
            data.eventTypeName = config.eventTypeName;
        if (config.minimumAvailability)
            data.minimumAvailability = config.minimumAvailability;

        $.ajax({
            url: AcademyApi.urls.getEventsForDateRange,
            data: data,
            dataType: 'jsonp',
            success: function(data, status, jqXHR) {
                AcademyApi.lastAjaxResult = {};
                AcademyApi.lastAjaxResult.data = data;
                AcademyApi.lastAjaxResult.status = status;
                AcademyApi.lastAjaxResult.jqXHR = jqXHR;

                if (data.result == "success" && funcCallback)
                  funcCallback(data);
                else if (data.result == "failure" && funcCallback)
                  funcCallback(data);
            },
            error: function(jqXHR, textStatus, errorThrown) {
                AcademyApi.lastAjaxResult = {};
                AcademyApi.lastAjaxResult.jqXHR = jqXHR;
                AcademyApi.lastAjaxResult.textStatus = textStatus;
                AcademyApi.lastAjaxResult.errorThrown = errorThrown;

                if (funcCallback)
                    funcCallback({result: "error", message: errorThrown});
            }
        });
    },
    ajaxGetEventsForDateRange: function(aStart, aEnd, resourceId, funcCallback) {
        var start;
        var end;

        if (aStart instanceof Date)
            start = this.formatDate(aStart);
        else
            start = aStart;

        if (aEnd instanceof Date)
            end = this.formatDate(aEnd);
        else
            end = aEnd;

        var data = {start: start, end:end, resourceId: resourceId};
        $.ajax({
            url: AcademyApi.urls.getEventsForDateRange,
            data: data,
            dataType: 'jsonp',
            success: function(data, status, jqXHR) {
                AcademyApi.lastAjaxResult = {};
                AcademyApi.lastAjaxResult.data = data;
                AcademyApi.lastAjaxResult.status = status;
                AcademyApi.lastAjaxResult.jqXHR = jqXHR;

                if (data.result == "success" && funcCallback)
                  funcCallback(data);
                else if (data.result == "failure" && funcCallback)
                  funcCallback(data);
            },
            error: function(jqXHR, textStatus, errorThrown) {
                AcademyApi.lastAjaxResult = {};
                AcademyApi.lastAjaxResult.jqXHR = jqXHR;
                AcademyApi.lastAjaxResult.textStatus = textStatus;
                AcademyApi.lastAjaxResult.errorThrown = errorThrown;

                if (funcCallback)
                    funcCallback({result: "error", message: errorThrown});
            }
        });
    },
    formatDate: function(aDate) {
        var dateString;
        if (aDate instanceof Date) {
            dateString = aDate.getYear();
            dateString += "-";
            dateString += aDate.getMonth();
            dateString += "-";
            dateString += aDate.getDay();
            dateString += " ";
            dateString += aDate.getHours();
            dateString += ":";
            dateString += aDate.getMinutes();
            dateString += ":";
            dateString += aDate.getSeconds();
        }
        return dateString;
    },
    registerNewsletterInterests: function() {
        var form = $(document.forms[1]);
        var firstname 	= form.find('input[name=firstname]').val();
        var lastname 	= form.find('input[name=lastname]').val();
        var email 		= form.find('input[name=email]').val();
        var phone 		= form.find('input[name=phone_mobile]').val();
        var zip 		= form.find('input[name=zipcode]').val();
        var source 		= form.find('input[name=source]').val();

        var chk_enews 	= form.find('input[name=category-id_0]').attr('checked');
        var chk_night 	= form.find('input[name=category-id_1]').attr('checked');
        var chk_teach 	= form.find('input[name=category-id_2]').attr('checked');
        var chk_lectures = form.find('input[name=category-id_3]').attr('checked');

        //NOTE:  it's very important that the values pushed onto the interests array here match the CAS contacts
        //database values (in 'interests.code') EXACTLY.  These values are used to register a given contact with
        //an interest in that database, thus if additional interests are added in the db they can be additionally
        //added here without changing API code.

        //NOTE:  It should be possible to add a data-code element to each of the checkboxes and maintain the interest
        //name in the HTML instead of having these values hard-coded in script
        var interests = [];
        if (chk_enews)
            var bUnused = true;
        if (chk_night)
            interests.push("Nightlife");
        if (chk_teach)
            interests.push("Teachers");
        if (chk_lectures)
            interests.push("Lectures");

        AcademyApi.ajaxRegisterNewsletterInterests(firstname, lastname, email, phone, zip, interests);
    }
};

//$(function() {
//    AcademyApi.initialize();
//    $("#modal-enews #submit").addEventListener('click', AcademyApi.registerNewsletterInterests);
//})