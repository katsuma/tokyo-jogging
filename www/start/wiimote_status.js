/**
 * wiimote_status.js
 * @Required : jquery.js
 */
jQuery.scope = function(target,func){ return function() { return func.apply(target,arguments);}};  

if(typeof window.WiimoteStatus != "undefined"){
	delete window.WiimoteStatus; 
}
var WiimoteStatus = {};

WiimoteStatus = {
	
	statusPath : "http://localhost:8080/",
	getStatus : function(){
		var self = this;
		$.ajax({
			dataType	: "jsonp",
			url	: this.statusPath,
			success : $.scope(self, self.checkAction)
		});		
	},
	
	initialize : function(){
		this.getStatus();
	},
	

	
	checkAction : function(res){
		var self = window.WiimoteStatus;
		try{
			var status = res;
			var action = status.action;
			_log(action);
			if(!action) return;
			
			var patterns = self.patterns;
			var actions = window.TJ.actions;
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
		} catch(e){
			_log(e);
		}
		
		// access again
		self.getStatus();
	}
};


	
WiimoteStatus.patterns = {
	ACTION_UP : 0x1,
	ACTION_RIGHT : 0x2,
	ACTION_DOWN : 0x3,
	ACTION_LEFT : 0x4,
	ACTION_JOG : 0x5,
	ACTION_ZOOM_IN : 0x6,
	ACTION_ZOOM_OUT : 0x7,
	ACTION_NOT_DETECTED : -0x1		
};
	

function _log(msg){
	var m = $("#debugger").html();
	$("#debugger").html(msg + m);
}

	
