
/**
 * This js contains two method 
 * 1)generateReport(reportName) : Requesting to load the report content in div.
 * 2)downloadReport(format): Downloading the current report in given format.
 */


//this will hold the currently loaded report name.
currentReportName="";
	/**
	 * This method is responsible for loading the reports in the report div.
	 * @param localReportName
	 */
	function generateReport(reportName) {
	
		//	here relative url is given if relative url is not working try giving full url
		var reporturl ="/BirtIntegration/loadReport?ReportName="+reportName+"&ReportFormat=html";
		
		$("#reportData").html("Loading...<br><img src='/resources/loading.gif' align='middle' >");
		
        $('#reportData').load(reporturl ,function(response, status, xhr) {
        	
          if (status == "error") {
		    var msg = "Sorry but there was an error getting details ! ";
			$("#reportData").html(msg + xhr.status + " " + xhr.statusText);
		  }
	    });
        
        currentReportName=reportName;
	}
	
	/**
	 * Download report function
	 * 
	 * @param format
	 */
	function downloadReport(format){
		
		if(currentReportName==""){
			alert("Please Select the report.");
			return;
		}
		//here relative url is given if relative url is not working try giving full url
		var reporturl ="/BirtIntegration/loadReport?ReportName="+currentReportName+"&ReportFormat="+format;
		window.location.href = reporturl;
		
	}
