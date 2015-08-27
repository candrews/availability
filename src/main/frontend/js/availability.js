require(["jquery", "fullcalendar", "fullcalendar-scheduler"], function($) {

	// var Availability = {
	// 	$resList: {},
	// 	events: [],
	// 	allEvents: function(evs){
	// 		this.events = evs;
	// 		console.log(this.events);
	// 	}
	// };

	$(document).ready(function() {
		var $resList = $('div.resource-list');
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

		// Availability.$resList = $resList;
		// expose all this publically
		// must be another way
		// window.Availability = Availability;

		var config = {
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
			// eventAfterAllRender: function(view){
			// 	Availability.allEvents(view.calendar.clientEvents());
			// },
			// eventClick: function(ev, jsEv, view){},
			// eventRender: function (ev, el, view){},
			resources: resources
		};
		var thing = $('#calendar').fullCalendar(config);
	});
});
