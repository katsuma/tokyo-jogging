(function($){
  $.tj = function(options){
	var self = this;
	var defaults = {
	  gateway : {
		wiimote : "ws://localhost:8080/ws/wii" ,
		balance_board : "ws://localhost:8080/ws/balance"
	  },
	  coords : { /* Shibuya */
		latitude : 35.659461,
		longtitude : 139.700512 
	  },
	  dom : {
		announce : '#announce',
		map : '#map',
		distance : '#distance-value',
		navigation : {
		  jog : 'a#navi-jogging',
		  walk : 'a#navi-walking',
		  navi : 'div#navi'
		}
	  }
	};
	var setting = $.extend(defaults, options);
	var gmap = {
	  panorama : {},
	  currentYaw : -90,
	  currentPitch : 0,
	  currentZoom : 0,
	  zoomingIn : true,
	  position : {},
	  firstJog : true,
	  currentPoint : null,
	  prevPoint : null,
	  distanceHistory : 0,
	  map : null,
	  marker : null,
	  mapZoomLevel : 17,

	  initialize: function(pos){
		gmap.position = pos;
		var mapElement = $(setting.dom.map)[0];
		gmap.map = new GMap2(mapElement);
		gmap.map.setCenter(gmap.position, gmap.mapZoomLevel);

		var guyIcon = new GIcon(G_DEFAULT_ICON);
		guyIcon.image = "http://maps.gstatic.com/mapfiles/cb/man_arrow-0.png";
		guyIcon.transparent = "http://maps.gstatic.com/intl/en_us/mapfiles/cb/man-pick.png";
		guyIcon.imageMap = [
		  26,13, 30,14, 32,28, 27,28, 28,36, 18,35, 18,27, 16,26,
		  16,20, 16,14, 19,13, 22,8
		];
		guyIcon.iconSize = new GSize(49, 52);
		guyIcon.iconAnchor = new GPoint(25, 35);

		gmap.marker = new GMarker(gmap.position, { icon:guyIcon, draggable:false });
		gmap.map.addOverlay(gmap.marker);
		gmap.currentPoint = gmap.prevPoint = gmap.marker.getPoint();

		// init panorama
		gmap.panorama = new GStreetviewPanorama(document.getElementById("street"));
		gmap.panorama.setLocationAndPOV(
		  gmap.position,
		  { yaw:gmap.currentYaw, pitch: gmap.currentPitch, zoom: gmap.currentZoom }
		);

		// add event listener
		GEvent.addListener(gmap.panorama, "initialized", function(loc){
		  gmap.map.panTo(loc.latlng);
		  gmap.marker.setLatLng(loc.latlng);

		  gmap.prevPoint = gmap.currentPoint;
		  gmap.currentPoint = gmap.marker.getPoint();
		  gmap.distanceHistory += gmap.currentPoint.distanceFrom(gmap.prevPoint) /1000;
		  if(!gmap.firstJog) {
			$(setting.dom.distance).html(gmap.distanceHistory.toFixed(2));
			var line = new GPolyline([gmap.prevPoint, gmap.currentPoint]);
			gmap.map.addOverlay(line);
		  }
		  gmap.firstJog = false;
		});

		// move marler to start position
		var gsClient = new GStreetviewClient();
		gsClient.getNearestPanorama(gmap.position, function(data){
		  if(data.code!=200) return;
		  var newPoint = data.location.latlng;
		  gmap.map.panTo(newPoint);
		  gmap.marker.setLatLng(newPoint);
		});

	  }
	};

	var actions = {
      jog : function(){
		gmap.panorama.followLink(gmap.currentYaw);
      },
      step : function(){
		gmap.panorama.followLink(gmap.currentYaw);
      },
      stop : function(){
		gmap.panorama.followLink(gmap.currentYaw+180);
      },

      left : function(v){
		gmap.currentYaw -= v || 15;
		gmap.panorama.panTo({yaw:gmap.currentYaw, pitch:gmap.currentPitch, zoom:gmap.currentZoom});
      },

      right : function(v){
		gmap.currentYaw += v || 15;
		gmap.panorama.panTo({yaw:gmap.currentYaw, pitch:gmap.currentPitch, zoom:gmap.currentZoom});
      },

      zoomIn : function(){
		gmap.currentZoom += 1;
		if(gmap.currentZoom>=2){
          gmap.currentZoom = 2;
		}
		gmap.panorama.panTo({yaw:gmap.currentYaw, pitch:gmap.currentPitch, zoom: gmap.currentZoom});
      },

      zoomOut : function(){
		gmap.currentZoom -= 1;
		if(gmap.currentZoom<=0){
          gmap.currentZoom = 0;
		}
		gmap.panorama.panTo({yaw:gmap.currentYaw, pitch:gmap.currentPitch, zoom: gmap.currentZoom});
      },

      up : function(){
		gmap.currentPitch -= 10;
		if(gmap.currentPitch<=-90){
          gmap.currentPitch = -90;
		}
		gmap.panorama.panTo({yaw:gmap.currentYaw, pitch:gmap.currentPitch, zoom: gmap.currentZoom});
      },

      down : function(){
		gmap.currentPitch += 10;
		if(gmap.currentPitch>=90){
          gmap.currentPitch = 90;
		}
		gmap.panorama.panTo({yaw:gmap.currentYaw, pitch:gmap.currentPitch, zoom: gmap.currentZoom});
      },

	  flush : function(message){
		$(setting.dom.announce).find('p').html(message).parent().slideDown('fast', function(){
		  var self = this;
		  setTimeout(function(){ $(this).slideUp(); }, 2000);
		});
	  }
	};

	var registerHandlers = function(){
	  var w = window;
	  $(w).bind('tj.jog', function(e){ actions.jog() } );
	  $(w).bind('tj.step', function(e){ actions.step() } );
	  $(w).bind('tj.stop', function(e){ actions.stop() } );
	  $(w).bind('tj.left', function(e, v){ actions.left(v) } );
	  $(w).bind('tj.right', function(e, v){ actions.right(v) } );
	  $(w).bind('tj.zoomIn', function(e){ actions.zoomIn() } );
	  $(w).bind('tj.zoomOut', function(e){ actions.zoomOut() } );
	  $(w).bind('tj.up', function(e){ actions.up() } );
	  $(w).bind('tj.down', function(e){ actions.down() } );
	  $('body').unload(GUnload);
	};

	$(document).ready(function(){
	  registerHandlers();

	  navigator.geolocation.getCurrentPosition(
		function (pos) {
		  gmap.initialize(new GLatLng(pos.coords.latitude, pos.coords.longitude));
		},
		function (error) {
		  var coords = setting.coords;
		  gmap.initialize(new GLatLng(coords.latitude, coords.longtitude)); 
		}
	  );


	  var navigation = setting.dom.navigation;

	  // Start jogging
	  $(navigation.jog).click(function(){
		$(navigation.jog).hide();
		$(navigation.walk).hide();
		$.wiimote({gateway:setting.gateway.wiimote});
	  });

	  // Start walking
	  $(navigation.walk).click(function(){
		$(navigation.jog).hide();
		$(navigation.walk).hide();
		$.balance_board({gateway:setting.gateway.balance_board});
	  });

	  var clientWidth = document.body.clientWidth || window.innerWidth;
	  $(navigation.navi).css('left', (clientWidth/2)-188 + 'px').css('display','block');
	});
  };
})(jQuery);
