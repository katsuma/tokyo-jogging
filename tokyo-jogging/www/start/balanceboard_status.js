/**
 * status.js
 * @Required : jquery.js
 */
jQuery.scope = function(target,func){ return function() { return func.apply(target,arguments);}};  

if(typeof window.BalanceBoardStatus != "undefined"){
	delete window.BalanceBoardStatus; 
}
var BalanceBoardStatus = {};

BalanceBoardStatus = {
	
	history : [],
	history_size : 3,

	statusPath : "http://localhost:8080/",
	getStatus : function(){
		var self = this;
		$.ajax({
			dataType	: "jsonp",
			url	: this.statusPath,
			success : $.scope(self, self.storeHistory)
		});		
	},
	
	initialize : function(){
		_log("BalanceBoard init");
		this.getStatus();
	},
	
	historyLock : false,
	storeHistory : function(res){
		var self = window.BalanceBoardStatus;
		try{
			var status = res;
			var direction = status.Direction;
			_log("JS:direction:"+direction);
			if(!direction) {
				return self.getStatus();
			};
			
			var patterns = self.patterns;
			var history = self.history;
			var history_size = self.history_size;
			var historyLock = self.historyLock;
			
			if(self.history.length>self.history_size) self.history = [];
			
			self.history.push(direction);
			
			if(!self.historyLock && history.length>=history_size){
				self.historyLock = true;
				var historyPattern = history.join("");
				var actions = window.TJ.actions;
				
				//_log("historyPattern:" + historyPattern, historyPattern.indexOf(patterns.STEP.join("")));
				if(historyPattern.indexOf(patterns.ZOOM_IN.join(""))!=-1){
					_log("Start to ZoomIn");
					actions.zoomIn();
				
				} else if(historyPattern.indexOf(patterns.ZOOM_OUT.join(""))!=-1){
					_log("Start to ZoomOut");
					actions.zoomOut();
				
				} else if(historyPattern.indexOf(patterns.STEP.join(""))!=-1){
					_log("Start to Step");
					actions.step();
				
				} else if(historyPattern.indexOf(patterns.RIGHT.join(""))!=-1){
					//_log("Start to Right");
					actions.right(2);
				
				} else if(historyPattern.indexOf(patterns.LEFT.join(""))!=-1){
					//_log("Start to Left");
					actions.left(2);
				
				} else 
				
				// clear histories
				self.history = [];
				self.historyLock = false;
			}
			
		} catch(e){
			_log(e);
			return self.getStatus();
		}
		
		// access again
		self.getStatus();
	}
};

function _log(msg){
	var m = $("#debugger").html();
	$("#debugger").html(msg + "<br />" + m);
	//console.log(msg);
}

	
BalanceBoardStatus.directions = {
	BALANCE_UP : 0x1,
	BALANCE_RIGHT : 0x2,
	BALANCE_DOWN : 0x3,
	BALANCE_LEFT : 0x4,
	BALANCE_CENTER : 0x5,
	BALANCE_ZOOM_IN : 0x6,
	BALANCE_ZOOM_OUT : 0x7	,
	BALANCE_NOT_DETECTED : -0x1		
};
	
BalanceBoardStatus.patterns = {
		
	ZOOM_IN : [
		BalanceBoardStatus.directions.BALANCE_ZOOM_IN, 
		BalanceBoardStatus.directions.BALANCE_ZOOM_IN, 
		BalanceBoardStatus.directions.BALANCE_ZOOM_IN,
	],
	
	ZOOM_OUT : [
	          BalanceBoardStatus.directions.BALANCE_ZOOM_OUT, 
	          BalanceBoardStatus.directions.BALANCE_ZOOM_OUT, 
	          BalanceBoardStatus.directions.BALANCE_ZOOM_OUT
	 ],
	
	STEP : [
		BalanceBoardStatus.directions.BALANCE_CENTER, 
		BalanceBoardStatus.directions.BALANCE_CENTER, 
		BalanceBoardStatus.directions.BALANCE_CENTER
	],
	
	RIGHT : [
	 		BalanceBoardStatus.directions.BALANCE_RIGHT, 
			BalanceBoardStatus.directions.BALANCE_RIGHT, 
	 		BalanceBoardStatus.directions.BALANCE_RIGHT 
	],
	
	LEFT : [
			BalanceBoardStatus.directions.BALANCE_LEFT,
			BalanceBoardStatus.directions.BALANCE_LEFT,
			BalanceBoardStatus.directions.BALANCE_LEFT
	]
};
	
