$(document).ready(function() {
/*
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
  */  

/*
<div id="os" class="well navbar-nav col-md-12 col-sm-12">
            <h5 class="col-md-12 col-sm-3">Système d'exploitation</h5>
              <ul class="col-md-12 col-sm-9 list-group list-unstyled">
                
                  <li class="list-group-item text-center col-md-4 col-sm-4 col-lg-4 col-xs-4">
                    <a href="index.html">
                      Windows
                    </a>
                  </li>
                  <li class="list-group-item text-center col-md-4 col-sm-4 col-lg-4 col-xs-4">
                    <a href="index.html">
                      Windows
                    </a>
                  </li>
                  <li class="list-group-item text-center col-md-4 col-sm-4 col-lg-4 col-xs-4">
                    <a href="index.html">
                      Windows
                    </a>
                  </li>                
              </ul>

              
            </div>
*/

   var build_ui = function() {
       var crit, val;
       var conditions = {}
       
       $('meta[scheme=user_condition]').each(
	   function(i,m) {
	       crit = $(m).attr('name');
	       val = $(m).attr('content');
	       if (conditions[crit] == undefined)
		   conditions[crit] = [];
	       conditions[crit].push(val); 
	   });
       build_ui_menus(conditions);
   }
    
    var build_ui_buttons = function(conditions) {
	$.each(conditions, function(i,c) {
	    $('<div>',
	      { "class":"well navbar-nav col-md-12 col-sm-12 user-condition",
		"id":"uc_"+i,
		"html":[$('<div>', {
		    "class":"col-md-12 col-sm-3",
		    "html":i}),
			$('<div>', {
			    "class":"btn-group btn-group-justified",
			    "data-toggle":"buttons"
			    
			})
		       ]
	      }).appendTo($('#userconditions'));
	    
      	    $.each(c,function(j,v){
		$('<label>', {
		    "class":"btn btn-default",
		    html:[$('<input>', {
			type:"radio",
			"class":"userselect",
			id:"uc_"+i+"_"+v,
		       name:"uc_"+i
		    }).change(function() {
			console.log('click');
			filter_view();
		    }),v]
		    
		}).appendTo($('#uc_'+i+" .btn-group"));
	    });
	});
    }

    var build_ui_menus = function(conditions) {
	$.each(conditions, function(i,c) {
	    $('<div>',
	      { "class":"col-md-12 col-sm-12 user-condition",
		"id":"uc_"+i,
		"data-code":i,
		"data-codevalue":c[0],
		"html":[$('<div>', {
		    "class":"col-md-12 col-sm-3",
		    "html":[
			$('<div>',{
			    "class":"col-md-8 col-sm-12",
			    "html":i
			}),
			$('<div>', {
			    "class":"btn-group col-md-4 col-sm-12",
			    "html":[
				$("<button>", {
				    "type":"button",
				    "class":"btn btn-default btn-xs dropdown-toggle",
				    "data-toggle":"dropdown",
				    "html":[c[0]+" ",$("<span>", {"class":"caret"})]
				}),
				$('<ul>',{
				    "class":"dropdown-menu",
				    "role":'menu' 
				})]
			})
		    ]
		})]
	      }).appendTo($('#userconditions'));
	      
      	    $.each(c,function(j,v){
		$('<li>', {
		    html:
			$('<a>', {
			    "href":"#",
			    "id":"uc_"+i+"_"+v,
			    "data-code":v,
			    "html":v
			}).click(function() {
			    $('#uc_'+i).data('codevalue',$(this).data('code'));
			    $('#uc_'+i+" button").html(v+" <span class='caret'/>");
			    filter_view();
			})
		}).appendTo($('#uc_' + i + " ul"));
	    });
	});
	
    }
    
    // Filter view with selected criterias
    
    filter_view = function(arg) {
	var s, value, cl;
	var me = this;
	var conditions = new Array();
	$(".user-condition").each(function(i,input) {
	    var cond = $(this).data('code');
	    var value = $(this).data('codevalue');
	    conditions[cond]=value;
	});
	
	
	$("#k-topic").find("*[class*='=']").each(function(e) {
	    cl = this.className.replace(/ /g,'');
	    if (check_condition(cl, conditions))
                this.style.display = "block";
	    else
                this.style.display = "none";
	});
    }

    check_condition = function(expr, crit) {
	var cond = expr.split('=');
	var c = cond[0];

	var expr = expr.substr(cond[0].length+1, expr.length);

	var r1 = !(expr.search(/,/) < 0);
	var r2 = !(expr.search(/;/) < 0);
	var r3 = !(expr.search(/\\/) < 0);
	
	// if criteria not selected
	if(!crit[c] && !r1) {
            return true;
	} else if(!crit[c]) {
            if(cond.length > 2) {
		//NoticePapier,NoticeWeb, ZONE = WestEurope, EastEurope
		var ncond = cond[1].split(',').pop();
		if(ncond.search(/;/) < 0)
                    return check_condition(expr.substr(cond[1].length-ncond.length, expr.length), crit);
		else
                    return false;
            } else {
		return true;
            }
	}
	
	// SIMPLE condition
	if(!r1 && !r2 && !r3) {
            return crit[c] == cond[1];
	}
	// EXCLUDE condition
	else if(!r1 && !r2 && r3) {
            return check_condition_exclude(expr, crit, c, cond[1]).result;
	}
	// AND condition
	else if(!r1 && r2 && !r3) {
            return check_condition_and(expr, crit, c);
	}
	// AND + EXCLUDE conditions
	else if(!r1 && r2 && r3) {
            var pos1 = expr.search(/;/);
            var pos2 = expr.search(/\\/);
            if(pos1 < pos2) {
		return check_condition_and(expr, crit, c) && check_condition(expr.substr(pos2+1, expr.length), crit);
            } else {
		var exclu = check_condition_exclude(expr, crit, c, cond[1]);
		return exclu.result && check_condition(expr.substr(exclu.pos+1, expr.length), crit);
            }
	}
	// OR condition
	else if(r1 && !r2 && !r3) {
            return check_condition_or(expr, crit, c).result;
	} 
	// OR + EXCLUDE conditions
	else if(r1 && !r2 && r3) {
            var pos1 = expr.search(/,/);
            var pos2 = expr.search(/\\/);
            if(pos1 < pos2) {
		var cor = check_condition_or(expr, crit, c)
		return cor.result || check_condition(expr.substr(cor.pos, expr.length), crit);
            } else {
		var exclu = check_condition_exclude(expr, crit, c, cond[1]);
		return exclu.result || check_condition(expr.substr(exclu.pos+1, expr.length), crit);
            }
	}
	// OR + AND conditions
	else if(r1 && r2 && !r3) {
            var pos1 = expr.search(/,/);
            var pos2 = expr.search(/;/);
            if(pos1 < pos2)
		return check_condition_or(expr, crit, c) && check_condition(expr.substr(pos2+1, expr.length), crit);
            else
		return check_condition_and(expr, crit, c) && check_condition(expr.substr(pos1+1, expr.length), crit);
	} 
	// OR + AND + EXCLUDE conditions
	else if(r1 && r2 && r3) {
            var pos1 = expr.search(/,/);
            var pos2 = expr.search(/;/);
            var pos3 = expr.search(/\\/);
            if(pos1 < pos2 && pos1 < pos3) {
		if(pos2 < pos3)
                    return check_condition_or(expr, crit, c) || check_condition(expr.substr(pos2+1, expr.length), crit);
		else
                    return check_condition_or(expr, crit, c).result || check_condition(expr.substr(pos3+1, expr.length), crit);
            } else if(pos2 < pos1 && pos2 < pos3) {
		if(pos1 < pos3)
                    return check_condition_and(expr, crit, c) && check_condition(expr.substr(pos1+1, expr.length), crit);
		else
                    return check_condition_and(expr, crit, c) && check_condition(expr.substr(pos3+1, expr.length), crit);
            } else {
		var exclu = check_condition_exclude(expr, crit, c, cond[1]);
		return exclu.result && check_condition(expr.substr(exclu.pos+1, expr.length), crit);
            }
	}
	return false;
    }


    // Check EXCLUDE condition
    var check_condition_exclude = function(expr, crit, cond, val) {
	var curpos = 0;
	var pos = expr.search(/\\/);
	
	var splitVal = val.substr(pos+1,val.length).split(",");
	if (!(expr.search(new RegExp(splitVal[splitVal.length-1]+"=")) < 0))
            splitVal.pop();
	
	for(var i=0; i<splitVal.length; i++) {
            if(crit[cond] == splitVal[i])
		return {'result': false, 'pos': 0};
            curpos += splitVal[i].length+1;
	}
	return {'result': true, 'pos': curpos};
    }

    // Check OR condition
    var check_condition_or = function(expr, crit, cond) {
	var curval;
	var val = expr.split(/[a-zA-Z0-9]+=/)[0].split(',');
	var curpos = 0;
	for(i=0; i<val.length; i++) {
            curval = val[i];
            if(curval != "") {
		curval = curval.split(';')[0];
		if(crit[cond] == curval)
                    return {'result': true, 'pos': 0};
		curpos+= curval.length+1;
            }
	}
	return {'result': false, 'pos': curpos};
    }

	
		   
    build_ui();
    filter_view();
	


 /*
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
   */ 
 })