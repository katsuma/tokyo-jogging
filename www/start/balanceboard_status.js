if(typeof window.BalanceBoardStatus != "undefined"){
	delete window.BalanceBoardStatus; 
}
var BalanceBoardStatus = {
  initialize: function(){
	var directions = {
	  BALANCE_UP : 0x01,
	  BALANCE_RIGHT : 0x02,
	  BALANCE_DOWN : 0x03,
	  BALANCE_LEFT : 0x04,
	  BALANCE_CENTER : 0x05,
	  BALANCE_ZOOM_IN : 0x06,
	  BALANCE_ZOOM_OUT : 0x07,
	  BALANCE_NOT_DETECTED : -0x01
	};
	
	var patterns = {	
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

	var history = [];
	var history_size = 3;

	var server_path = "ws://localhost:8080/";
	var historyLock = false;
	var self = this;
	var storeHistory = function(res){
	  try{
		var status = res;
		var direction = status.Direction;
		if(!direction) {
		  return self.getStatus();
		};
		
		var patterns = self.patterns;
		var history = self.history;
		var history_size = self.history_size;
		var historyLock = self.historyLock;
		
		if(history.length > history_size) history = [];
		
		history.push(direction);
		
		if(!historyLock && length >= history_size){
		  historyLock = true;
		  var historyPattern = history.join("");
		  
		  if(historyPattern.indexOf(patterns.ZOOM_IN.join(""))!=-1){
			actions.zoomIn();
			
		  } else if(historyPattern.indexOf(patterns.ZOOM_OUT.join(""))!=-1){
			actions.zoomOut();
			
		  } else if(historyPattern.indexOf(patterns.STEP.join(""))!=-1){
			actions.step();
			
		  } else if(historyPattern.indexOf(patterns.RIGHT.join(""))!=-1){
			actions.right(2);
			
		  } else if(historyPattern.indexOf(patterns.LEFT.join(""))!=-1){
			actions.left(2);
			
		  }

		  // clear histories
		  history = [];
		  historyLock = false;
		}	  
	  } catch(e){ }
	}
  }
};
