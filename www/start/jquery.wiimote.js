(function($){
  $.wiimote = function(options){
	var self = this;
	var defaults = {
	  gateway : ""
	};
	
	var setting = $.extend(defaults, options);

	var actions = {
	  UP : 0x01,
	  RIGHT : 0x02,
	  DOWN : 0x03,
	  LEFT : 0x04,
	  JOG : 0x05,
	  ZOOM_IN : 0x06,
	  ZOOM_OUT : 0x07,
	  NOT_DETECTED : -0x01		
	};

	var callbacks = {
	  connectHandler : function(){},
	  closeHandler : function(){},
	  messageHandler : function(message){
		try{
		  var data = JSON.parse(message.data);
		  if(data.message){
			$(window).trigger('tj.flush', data.message);
			return;
		  }
		  var action = data.action;
		  switch(action){
		  case actions.JOG:
			$(window).trigger('tj.jog'); break;
		  case actions.RIGHT:
			$(window).trigger('tj.right'); break;
		  case actions.LEFT:
			$(window).trigger('tj.left'); break;
		  case actions.UP:
			$(window).trigger('tj.up'); break;
		  case actions.DOWN:
			$(window).trigger('tj.down'); break;
		  case actions.ZOOM_IN:
			$(window).trigger('tj.zoomIn'); break;
		  case actions.ZOOM_OUT:
			$(window).trigger('tj.zoomOut'); break;
		  default:
			break;							
		  }
		} catch(e) { }
	  }
	};

	
	if(setting.gateway == "") {
	  throw "Gateway address is not set."
	  return;
	}
	if(typeof window.WebSocket == "undefined" && setting.gateway.indexOf('ws://')) {
	  setting.gateway = setting.gateway.reaplce('ws://', 'http://')
	}

	if(typeof window.WebSocket == "undefined") {
	  var connector = $.ajax({
		dataType : 'jsonp',
		url : setting.gateway,
		success : function(response){
		  callbacks.messageHandler(response);
		  connector.call(self);
		}
	  });

	} else {

	  var ws = new WebSocket(setting.gateway);
	  ws.addEventListener("open", callbacks.connectHandler, false);
	  ws.addEventListener("close", callbacks.closeHandler, false);
	  ws.addEventListener("message", callbacks.messageHandler, false);  
	  try { ws.send("connect to device"); } catch(e) {};
	}
  };
})(jQuery);
