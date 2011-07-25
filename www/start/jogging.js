var TJ = {
  panorama : {},
  currentYaw : -90,
  currentPitch : 0,
  currentZoom : 0,
  zoomingIn : true,
  balanceBoardStatus : {},
  position : {},

  firstJog : true,
  currentPoint : null,
  prevPoint : null,
  distanceHistory : 0,

  map : null,
  marker : null,
  mapZoomLevel : 17,

  actions : {
    jog : function(){
      TJ.panorama.followLink(TJ.currentYaw);
    },
    step : function(){
      TJ.panorama.followLink(TJ.currentYaw);
    },
    stop : function(){
      TJ.panorama.followLink(TJ.currentYaw+180);
    },

    left : function(v){
      TJ.currentYaw -= v || 15;
      TJ.panorama.panTo({yaw:TJ.currentYaw, pitch:TJ.currentPitch, zoom:TJ.currentZoom});
    },

    right : function(v){
      TJ.currentYaw += v || 15;
      TJ.panorama.panTo({yaw:TJ.currentYaw, pitch:TJ.currentPitch, zoom:TJ.currentZoom});
    },

    zoomIn : function(){
      TJ.currentZoom += 1;
      if(TJ.currentZoom>=2){
        TJ.currentZoom = 2;
      }
      TJ.panorama.panTo({yaw:TJ.currentYaw, pitch:TJ.currentPitch, zoom: TJ.currentZoom});
    },

    zoomOut : function(){
      TJ.currentZoom -= 1;
      if(TJ.currentZoom<=0){
        TJ.currentZoom = 0;
      }
      TJ.panorama.panTo({yaw:TJ.currentYaw, pitch:TJ.currentPitch, zoom: TJ.currentZoom});
    },

    up : function(){
      TJ.currentPitch -= 10;
      if(TJ.currentPitch<=-90){
        TJ.currentPitch = -90;
      }
      TJ.panorama.panTo({yaw:TJ.currentYaw, pitch:TJ.currentPitch, zoom: TJ.currentZoom});
    },

    down : function(){
      TJ.currentPitch += 10;
      if(TJ.currentPitch>=90){
        TJ.currentPitch = 90;
      }
      TJ.panorama.panTo({yaw:TJ.currentYaw, pitch:TJ.currentPitch, zoom: TJ.currentZoom});
    }
  }
};

var initMap = function(){
  var mapElement = document.getElementById("map");
  TJ.map = new GMap2(mapElement);
  TJ.map.setCenter(TJ.position, TJ.mapZoomLevel);

  var guyIcon = new GIcon(G_DEFAULT_ICON);
  guyIcon.image = "http://maps.gstatic.com/mapfiles/cb/man_arrow-0.png";
  guyIcon.transparent = "http://maps.gstatic.com/intl/en_us/mapfiles/cb/man-pick.png";
  guyIcon.imageMap = [
    26,13, 30,14, 32,28, 27,28, 28,36, 18,35, 18,27, 16,26,
    16,20, 16,14, 19,13, 22,8
  ];
  guyIcon.iconSize = new GSize(49, 52);
  guyIcon.iconAnchor = new GPoint(25, 35);

  TJ.marker = new GMarker(TJ.position, {icon:guyIcon, draggable:false});
  TJ.map.addOverlay(TJ.marker);
  GEvent.addListener(TJ.marker, "click", function(){
    TJ.marker.openInfoWindowHtml("<img src=\"http://tokyo-jogging.com/images/marker-katsuma.png\" /><p>katsuma</p>");
  });

  TJ.currentPoint = TJ.prevPoint = TJ.marker.getPoint();

  // init panorama
  TJ.panorama = new GStreetviewPanorama(document.getElementById("street"));
  TJ.panorama.setLocationAndPOV(
    TJ.position,
    { yaw:TJ.currentYaw, pitch: TJ.currentPitch, zoom: TJ.currentZoom }
  );

  // add event listener
  GEvent.addListener(TJ.panorama, "initialized", function(loc){
    TJ.map.panTo(loc.latlng);
    TJ.marker.setLatLng(loc.latlng);

    TJ.prevPoint = TJ.currentPoint;
    TJ.currentPoint = TJ.marker.getPoint();
    TJ.distanceHistory += TJ.currentPoint.distanceFrom(TJ.prevPoint) /1000;
    if(!TJ.firstJog) {
	  $("#distance-value").html(TJ.distanceHistory.toFixed(2));
	  var line = new GPolyline([TJ.prevPoint, TJ.currentPoint]);
	  TJ.map.addOverlay(line);
    }
    TJ.firstJog = false;
  });

  // move marler to start position
  var gsClient = new GStreetviewClient();
  gsClient.getNearestPanorama(TJ.position, function(data){
    if(data.code!=200) return;
    var newPoint = data.location.latlng;
    TJ.map.panTo(newPoint);
    TJ.marker.setLatLng(newPoint);
  });

};

$(function(){
  navigator.geolocation.getCurrentPosition(
    function (pos) {
	  TJ.position = new GLatLng(pos.coords.latitude, pos.coords.longitude);
	  initMap();
    },
    function (error) {
	  TJ.position = new GLatLng(35.659461,139.700512); /* Shibuya  */
	  initMap();
    }
  );
  
  // Start jogging
  $('a#navi-jogging').click(function(){
    $('a#navi-jogging').hide();
    $('a#navi-walking').hide();
    WiimoteStatus.initialize();
  });

  // Start walking
  $('a#navi-walking').click(function(){
    $('a#navi-jogging').hide();
    $('a#navi-walking').hide();
    BalanceBoardStatus.initialize();
  });

  var clientWidth= document.body.clientWidth || window.innerWidth;
  $('div#navi').css('left', (clientWidth/2)-188 + 'px').css('display','block');
})
$('body').unload(GUnload);

