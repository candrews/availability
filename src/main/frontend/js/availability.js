require(["jquery","fullcalendar", "fullcalendar-scheduler"], function($) {
	$(document).ready(function() {
		var emailAddresses = $('body').data('emailAddresses').split(' ');
		var eventSources = [];
		var resources = [];
		emailAddresses.forEach(function(emailAddress, i){
			var color = "hsl(" + (i * 50)%360 + ", 50%, 50%)";
			eventSources.push({
				color: color,
				url: '/user/' + emailAddress + '/availability/fullcalendar.json',
				cache: true, // the server returns appropriate caching headers so cache busting is not desirable
				timezoneOffset: new Date().getTimezoneOffset()
			});
			resources.push({
				id: emailAddress,
				title: emailAddress
			});
		});
		
		$('#calendar').fullCalendar({
			schedulerLicenseKey: 'GPL-My-Project-Is-Open-Source',
			defaultView: 'agendaDay',
			header: {
				left: 'prev,next today',
				center: 'title',
				right: 'month,agendaWeek,agendaDay'
			},
			timezone: 'local',
			editable: false,
			eventLimit: true,
			eventSources: eventSources,
			resources: resources
		});
		
	});
});

