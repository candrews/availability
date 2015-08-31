require(["jquery", "fullcalendar", "fullcalendar-scheduler"], function($) {

	var Availability = {
		'$': $, // save some jQuery for use later, even globally
		// colorStart: '#f23109', // a nice shade of @orange
		colorStart: "hsl(10, 93%, 49%)", // a nice shade of @orange

		// internally used for queries, etc.
		$resList: {},
		emailAddresses: [], // used for query
		eventSources: [], // used for query
		resources: [], // pushed into calendar

		sources: [], // internal store
		events: [], // array of events from calendar
		els: [], // elements used for events in the view(s)
		views: [], //
		isHasData: false, // toggled during load event to tell others state of data
		evsFetched: 0, // keep track of number of events fetched last
		persist: false, // for the bright future where we may save filtered states
		startRender: false, // status
		/**
		 * init will be called when we load new data.
		 * @param {boolean} resetSources is true only on the first operation from document.ready()
		 *                               after that, it is undefined which means the sources are
		 *                               the same, but we are fetching new events.
		 * @returns {void}
		 */
		init: function(resetSources) {
			// console.count('init()');
			// this.eventSources = [];
			if (typeof resetSources !== 'undefined' && resetSources === true) {
				// console.debug('do we need to reset the resources or sources?'); // yes
				this.resources = [];
				this.sources = [];
			}
			// this.resources = [];
			// this.sources = [];
			this.events = [];
			this.els = [];
			this.views = [];
		},

		/**
		 * checkState is called in two cases: first, when the loading operation starts;
		 * 		and second, when the eventAfterAllRender() runs, but then checkState gets an
		 * 		undefined 'isLoad' argument so we rely on the 'startRender' value.
		 * @param {boolean} isLoad - is this being called from the load data event?
		 * @returns {void}
		 *
		 * states
		 * 	- isLoad = true --- fullCalendar has started to load the page data
		 *  - isLoad = false --- fullCalendar has finished loading (and rendering) the page data
		 *  - hasData = true --- we have fetched data
		 *  - hasData = false --- we have no fetched data
		 *  - startRender = true --- rendering has begun
		 *  - startRender = false --- rendering either: (a) hasn't started or (b) is done
		 */
		checkState: function(isLoad){

			// console.info('checkState:', isLoad);

			// first condition
			if (typeof isLoad !== 'undefined' && isLoad === true) { // starting rendering
				this.startRender = true;
				this.views = [];
				this.els = []; // this.resetResources(); // this might as well be done, it will be needed
			} else if (this.isHasData && this.startRender && typeof isLoad !== 'undefined' && isLoad === false) {
				// finished the load and build
				this.startRender = false;
				this.resetResources();
			} else if (this.isHasData && !this.startRender && typeof isLoad === 'undefined') {
				// we have data, we are done building, and need to .... do something .... ????
				// console.trace('checkState');
				// console.warn('what now ... ?');
			} else if (this.startRender === true) {
				// console.warn('startRender === true, setting false')
				this.startRender = false;
				if (!this.persist) {
					this.resetResources();
				}
				// nothing
			} else if (this.startRender === false) { // this means the above, with isLoad = true was called, and startRender was also toggled
				this.resetResources();
			} else { // default
				this.startRender = false;
			}
		},

		resetResources: function(){
			// console.count('resetResources');
			this.$resList.find('li').removeClass('m-dull');
		},

		colored: false,
		colorKey: function(){
			if (this.colored) {
				return;
			}
			var select = '';
			var rule = '';
			$.each(this.sources, function(index, el) {
				el.$el.css('border-bottom-color', el.color);
				select = '.resource-list li:nth-child(' + (index + 1) + '):before';
				rule = '{border-bottom-color: ' + el.color + ';}';
				document.styleSheets[1].insertRule(select + rule, 0);
			});
			$('.resource-list li').on('click', function(){
				var $that = $(this);
				$that.toggleClass('m-dull');
				Availability.relatedByResource(this, !$that.hasClass('m-dull'));
			});
			this.colored = true;
		},

		setAllEvents: function(evs){
			// console.debug('number of events: ', evs.length);
			// reconcile rendering an event vs. "having" an event from the fetched list
			if (this.events.length === evs.length) { // strictly not even necessary
				this.events = [];
				this.events = evs;
			}
			this.evsFetched = evs.length;
			this.colorKey();
		},
		relatedByResource: function(item, onOff){
			var data = $(item).data('val');
				$.each(this.events, function(i, el){
					if (data === el.resourceId && !onOff) {
						Availability.views[i].hide();
					}
					if (data === el.resourceId && onOff) {
						Availability.views[i].show();
					}
				});
		},
		fadeRelatedByEvent: function(item, view){
			// console.log(item, view.name);
			// var data = $(item).data('val');
		}
	};

	$(document).ready(function() {

		var av = Availability;
			av.$resList = $('.resource-list');
			av.emailAddresses = $('body').data('emailAddresses').split(' ');
			// set up
			av.init(true);

		av.emailAddresses.forEach(function(emailAddress, i){
			var color = (i === 0) ? av.colorStart : "hsl(" + (i * 50)%360 + ", 50%, 50%)";
			av.eventSources.push({
				color: color,
				url: '/user/' + emailAddress + '/availability/fullcalendar.json',
				cache: true, // the server returns appropriate caching headers so cache busting is not desirable
				timezoneOffset: new Date().getTimezoneOffset()
			});
			av.resources.push({
				id: emailAddress,
				title: emailAddress
			});
			// for reference later
			av.sources.push({
				id: emailAddress,
				color: color,
				$el: av.$resList.find('li:eq(' + i + ')')
			});
		});

		// expose all this publically
		// must be another way
		window.Availability = av;

		var config = {
			schedulerLicenseKey: 'GPL-My-Project-Is-Open-Source',
			defaultView: 'agendaDay',
			// defaultView: 'month',
			// defaultView: 'agendaWeek',
			header: {
				left: 'prev,next today',
				center: 'title',
				right: 'month,agendaWeek,agendaDay'
			},
			aspectRatio: 2,
			businessHours: true,
			minTime: '06:00:00',
			maxTime: '21:00:00',
			timezone: 'local',
			editable: false,
			eventLimit: true,
			eventSources: av.eventSources,
			loading: function(isLoading, view){
				if (isLoading) { // when a load event is triggered, we need to invalidate the data we have
					av.isHasData = false;
					av.init();
				} else {
					av.isHasData = true;
				}
				av.checkState(isLoading);
			},
			eventRender: function(ev, el, view){
				if (av.isHasData && av.startRender === false) {
					// rendering events are being called and we have data so ... init()?
					av.startRender = true;
					// this should be done elsewhere, like a better init();
					av.els = [];
					av.views = [];
					av.events = []; // reset because we are using a sub-set of what we already have
				}
				av.els.push(el);
				av.events.push(ev);
			},
			eventAfterRender: function(ev, el, view){ // fundamentally no difference between eventRender() and eventAfterRender() for us here, though in the API it is before/after
				av.views.push(el);
			},
			eventAfterAllRender: function(view){
				av.checkState();
				av.setAllEvents(av.$('#calendar').fullCalendar('clientEvents'));
			// },
			// eventClick: function(ev, jsEv, view){
			// 	// console.log('click:', ev, view);
			// 	Availability.fadeRelatedByEvent(ev, view);
			},
			// eventRender: function (ev, el, view){},
			resources: av.resources
		};
		$('#calendar').fullCalendar(config);
	});
});
