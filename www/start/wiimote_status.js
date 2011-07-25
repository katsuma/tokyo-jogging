if(typeof window.WiimoteStatus != "undefined"){
	delete window.WiimoteStatus; 
}
var WiimoteStatus = {
  initialize: function(){
	var server_path = "ws://localhost:8080/ws/wii";
	var patterns = {
	  ACTION_UP : 0x01,
	  ACTION_RIGHT : 0x02,
	  ACTION_DOWN : 0x03,
	  ACTION_LEFT : 0x04,
	  ACTION_JOG : 0x05,
	  ACTION_ZOOM_IN : 0x06,
	  ACTION_ZOOM_OUT : 0x07,
	  ACTION_NOT_DETECTED : -0x01		
	};

	var ws = new WebSocket(server_path);
	ws.addEventListener("message", function(message){
	  try{
		var data = JSON.parse(message.data);
		var action = data.action;
		var actions = TJ.actions;
		if(!action) return;
		
		switch(action){
		case patterns.ACTION_JOG:
		  actions.jog();
		  break;
		case patterns.ACTION_RIGHT:
		  actions.right();
		  break;
		case patterns.ACTION_LEFT:
		  actions.left();
		  break;
		case patterns.ACTION_UP:
		  actions.up();
		  break;
		case patterns.ACTION_DOWN:
		  actions.down();
		  break;
		case patterns.ACTION_ZOOM_IN:
		  actions.zoomIn();
		  break;
		case patterns.ACTION_ZOOM_OUT:
		  actions.zoomOut();
		  break;
		default:
		  break;							
		}
	  } catch(e) {}
	}, false);  

	ws.addEventListener("open", function(){ console.log('open') }, false);
	ws.addEventListener("close", function(){ ws = null;	}, false);
	ws.send("connect to device");

  }
};