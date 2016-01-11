$(document).ready(function() {
    $('a[data-section]').on('click', function(ev) {
	var content = $("ul[data-section-content='"+$(this).attr("data-section")+"']"); 
	var icon = $(this).find('i'); 
	content.toggleClass('hidden');
	if (content.hasClass('hidden'))
	{
	    icon.attr('class','glyphicon glyphicon-folder-close');
	} else {
	    icon.attr('class','glyphicon glyphicon-folder-open');
	}  
	ev.preventDefault();
	ev.stopPropagation();
    })
    $('#ksearchinput').on('keyup', function() {
	search();
    });

    $('#search_btn').on('click', function() {
	search();
 	highlight();
   });

    $('#searchclose').on('click', function() {
	$('#search_results').hide();
 	unhighlight();
   });
    
    var search = function() {
	var words = $("#ksearchinput").val();
	if (words.length > 2) {
	    a_search(words);
	    $("#search_results").show();
	} else {
	    $("#search_results").hide();
 	    unhighlight();
	}
    }

    $('#indexinner').each(function(e) {
	var row = $(this);
	var pos = 0;
	$(this).find('.span3').each(function() {
	    if (pos == 3) {
		pos = 0;
		newrow = $('<div class="row-fluid">');
		row.after(newrow);
		row = newrow;
	    }
	    row.append($(this));
	    pos ++;
	});
    });
    
    qs = loadfromurl(); 
    if (loadfromurl()){
	search();
	highlight();
    }
})