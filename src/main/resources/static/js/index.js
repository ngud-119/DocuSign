function fillSignDocForm(elem) {
	console.log(elem);
	var docId = elem.attributes.getNamedItem("data-docid").value;
	$('#docId').val(docId);
	console.log('set doc id = ' + docId);
}

function loadDocsPage(elem) {
	var pageId = elem.attributes.getNamedItem("data-page").value;
	var url = "/doclist?"+ "page=" + pageId;
    $.get(url, function(fragment) { // get from controller
        $("#docsTable").replaceWith(fragment); // update snippet of page
    });
}

$(document).ready(function() {
    $("#submitSignForm").click(function(){
    	var eSignProvider = $( "#eSignProvider" ).val();
    	$('#signDocForm').attr('action','/'+ eSignProvider + '/sign');
    	$('#signDocForm').submit();
    }); 
    
    $("#submitNewDocBtn").click(function(){
    	$(this).text('Sending…');
    	$('#newDocForm').submit();
    	$("#basicModal").find('button, select').attr('disabled','disabled');
//    	$("#basicModal").attr('data-backdrop','static');
//    	$("#basicModal").attr('data-keyboard','false');
//    	$("#basicModal").modal('hide');
    }); 
    
});