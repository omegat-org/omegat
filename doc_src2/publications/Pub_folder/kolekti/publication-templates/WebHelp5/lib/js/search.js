
var charsin ='àâäéèêëïîôöùûç';
var charsout='aaaeeeeiioouuc';
var charexcl='!#%&\',-/:;=>@_`{}~';
var charescxcl='()*+.?[\\]$^|';
var stopwords=[]

var nbsnippets=1;
var maxres = 8;
var hllimit=50;

var IE=false;
if(navigator.userAgent.indexOf("MSIE") != -1)
    IE=true;

var do_highlight=true;

var options={
    mot_entier:false,
    multi_ou:false
}

function chchr(str) {
    var re=new RegExp('');
    var chr=str;
    var cin;
    var cout;
    var i;
    for (i=0; i<charsin.length; i++) {
	cin=charsin.substr(i,1);
	cout=charsout.substr(i,1);
	re.compile(cin,"g");
	chr=chr.replace(re,cout);
    }
    for (i=0; i<charexcl.length; i++) {
	cin=charexcl.substr(i,1);
	re.compile(cin,"g");
	chr=chr.replace(re,'');
    }
    for (i=0; i<charescxcl.length; i++) {
	cin=charescxcl.substr(i,1);
	re.compile("\\"+cin,"g");
	chr=chr.replace(re,'');
   }
    return chr;
}

function chchr2(str) {
    var re=new RegExp('');
    var chr=str;
    var cin;
    var cout;
    var i;
    for (i=0; i<charsin.length; i++) {
	cin=charsin.substr(i,1);
	cout=charsout.substr(i,1);
	re.compile(cin,"g");
	chr=chr.replace(re,cout);
    }
    for (i=0; i<charexcl.length; i++) {
	cin=charexcl.substr(i,1);
	re.compile(cin,"g");
	chr=chr.replace(re,' ');
    }
    for (i=0; i<charescxcl.length; i++) {
	cin=charescxcl.substr(i,1);
	re.compile("\\"+cin,"g");
	chr=chr.replace(re,' ');
   }
    return chr;
}

function lemmatise(words){
    var res=[];
    var word;
    var lc=words.toLowerCase()
    lc=lc.replace(/ +/g," ");
    lc=lc.replace(/^ /,"");
    lc=lc.replace(/ $/,"");

    var nb;
    lc=chchr2(lc);
    if (lc.substr(0,1)=='"') {
	nb=lc.substr(2).search('"');
	if (nb==-1) return [];
        res.push(lc.substr(0,nb+3));
        lc=lc.substr(nb+4);
    }
    var nb=lc.search(' ');
    while (nb!=-1) {
	if(lc.substr(0,1)=='"'){
	    nb=lc.substr(1).search('"');
	    if (nb==-1) return [];
	    nb+=2;
	}
	word = lc.substr(0,nb);
	if (stopwords.indexOf(word)==-1) {
            res.push(word);
	}
        lc=lc.substr(nb+1);
        nb=lc.search(' ');
    }
    if (stopwords.indexOf(lc)==-1) {
	res.push(lc);
    }
    return res
        
}

function a_search(words) {
    var restot={};
    var rescount={};
    var restab={};
    var wordtab=lemmatise(words);
    var i, res;
    lastsearchedwords=[];
    for (i in wordtab) {
        if (wordtab[i]=="") continue;
        if (wordtab[i].substr(0,1)=='"') {
            var w=wordtab[i].substr(1,wordtab[i].length-2);
            restab=a_search_fulltext(w);
            lastsearchedwords.push(w);
        } else {
            restab=a_searchword(wordtab[i]);
            lastsearchedwords.push(wordtab[i]);
        }
        for (res in restab) {
            if (restot[res]) {
                restot[res]+=restab[res];
                rescount[res]+=1;
            } else {
                restot[res]=restab[res];
                rescount[res]=1;
            }
        }
    }
    var rescore={};
    for (res in restot) {
  	rescore[res]=Math.floor((restot[res]/wordcount[res])*1000);
    }
    var ressort=sort_score(rescore);
    
    
    
    var count = 0;
    var htmlbuf = '';
    var searchl = $("ul#ksearchmenu");
    searchl.html("<li>Aucun résultat</li>");
    var searchdata=[ lastsearchedwords, words];
    var str_words = encodeURIComponent(JSON.stringify(searchdata));
    for (r in ressort) {
        res=ressort[r];
	if (res) {
            htmlbuf += '<li><a href="'+res+'.html?s='+str_words+'"><strong>'+modcodes[res+".html"]+'</strong><br /><em>'+show_search_words(res,false)+'</em></a></li>';
            if(count > maxres)
		break;
            count++;
	}
    }
    if(htmlbuf != '') {
        searchl.html(htmlbuf);
    }
    return
    if(htmlbuf != '') {
	searchl[0].style.display = "block";
    } else {
        searchl[0].style.display = "none";
    }
}

function search_get_text(doc,topic) {
    var topicdiv=doc.getElementById(topic+'.html');
    text=topicdiv.innerText || topicdiv.textContent;
    return chchr2(text);
}


function sort_score(obj) {
  sor=[];
  for(i in obj) {
    v=obj[i];
    for(j=0;j<sor.length && v<obj[sor[j]];j++);
    end=sor.slice(j,sor.length);
    begin=sor.slice(0,j);
    begin.push(i);
    sor=begin.concat(end)    
  }
  return sor;
}


function a_search_fulltext(word){ 
    var res={};
    var re=new RegExp(word);
    var sif=document.getElementById('searchresframe').contentWindow.document;
    var topics=sif.getElementsByTagName('DIV');
    var segs=[]
    for (i=0;i<topics.length;i++) {
	text=topics[i].innerText || topics[i].textContent;
	text=chchr2(text.toLowerCase());
	segs=text.split(re);
	if (segs.length > 1) {
	    res[topics[i].getAttribute('id').replace(".html","")]=segs.length-1;
	}
    }
    return res;
}


function a_searchword(word) {
    var cind=1;
    var clett=0;
    var stop=false;
    var wordlen=word.length;
    var nextcand=0;
    var letterfound;
    var curlet;
    var result={};
    while (clett < wordlen && !stop) {
	curlet=word.substr(clett,1);
	nextcand=cind;
	letterfound=false;
	while(nextcand!=0 && !letterfound){
	    if (curlet==nodes[nextcand]) {
		letterfound=true;
	    } else {
		nextcand=nexts[nextcand];
	    }
	}
	
	if (letterfound) {
	    clett++;
	    cind=child[nextcand];
	} else {
	    cind=0;
	}
	stop=(cind==0);
    }

    if (clett==wordlen) {
	a_concatres(result,terms[nextcand]);
	if (!options['mot_entier']) {
	    s=a_expandsearch(nextcand);
	    a_concatres(result,s);
	}
    }

    return result;
}

function a_concatres(resobj,tabres) {
    for (res in tabres) {
	
	if (resobj[res]) {
	    resobj[res]+=tabres[res];
	} else {
	    resobj[res]=tabres[res];
	}
    }
}


function a_expandsearch(cind) {
    var res=[];
    var cnode=child[cind];
    while (cnode) {
	a_concatres(res,terms[cnode])

	//res.concat(a_expandsearch(cnode));
	a_concatres(res,a_expandsearch(cnode));
	cnode=nexts[cnode];
    }
    return res;
}

function show_search_words(topic,full) {
    var strings=extraits(modtexts[topic+'.html']);
    var res="";
    for (s in strings) {
	if (full || s <= (nbsnippets - 1)) {	
	    res += '... '+strings[s]+'...';
	}
    }
    
    if (!full && s > (nbsnippets - 1)) {
        res + label_moreres;
    }
    return res;
}

function extraits(text) {
    if(!text)
       return;
    var textl=chchr2(text.toLowerCase());
	var texto=text;
    var delend='';
    var segs;
    var len;
    var indices=[];
    var re= new RegExp();
    if (options.mot_entier) {
	delend='\\b';
    }
    for (wsi in lastsearchedwords) {
	re.compile('\\b'+lastsearchedwords[wsi]+delend);
	segs=textl.split(re);
	len=0;
	for (s=0; s<segs.length-1; s++) {
	    len+=segs[s].length
	    indices[indices.length]=[len,lastsearchedwords[wsi].length];
	    len+=lastsearchedwords[wsi].length;
	}
    }
    indices.sort(sortIndices);
    var curst=0;
    var curend=0;
    var restab=[];
    var ext;
    for (i=0; i<indices.length; i++) {
	if (indices[i][0]-hllimit <0) {
	    ext=texto.substr(0,indices[i][0]);
	} else {
	    ext=texto.substr(indices[i][0]-hllimit,hllimit);
	}
	ext+='<span style="border-bottom: 1px dotted #0088CC;">';
	ext+=texto.substr(indices[i][0],indices[i][1]);
	ext+="</span>";
	while (i<(indices.length-1) && indices[i+1][0]-indices[i][0]<hllimit) {
	    ext+=texto.substring(indices[i][0]+indices[i][1], indices[i+1][0]);
	    ext+='<span class="border-bottom dotted #0088CC;">';
	    ext+=texto.substr(indices[i+1][0],indices[i+1][1]);
	    ext+="</span>";
	    i++;
	}
	if (indices[i][0]+indices[i][1]+hllimit > texto.length)
	    ext+=texto.substr(indices[i][0]+indices[i][1],texto.length-(indices[i][0]+indices[i][1]+1));
	else
	    ext+=texto.substr(indices[i][0]+indices[i][1],hllimit);
	ext=ext.replace(/^[^\s]*\s+/,'');
	ext=ext.replace(/\s+[^\s]*$/,'');
	restab[restab.length]=ext;
    }
    return restab;
}

function sortIndices(a,b) {
  return a[0] - b[0];
}

function loadfromurl() {
    var u = window.location.href.split('?');
    if (u.length > 1) {
	search_data =  JSON.parse( decodeURIComponent(u[1].substr(2)));
	lastsearchedwords = search_data[0]
	$("#ksearchinput").val(search_data[1]);
	return true;
    }
    return false;
}

function highlight() {
	var elt = document.getElementById('k-topiccontent');
    first_in_topic=true;
    parcours(elt,highlight_text);
}

function unhighlight() {
   var elt = document.getElementById('k-topiccontent');
   var splist=elt.getElementsByTagName('SPAN');
   var i,span;
   for (i=0; i< splist.length;i++) {
     span=splist[i];
     if (span.getAttribute('class')=='hl' || span.className=="hl") {
	 
       txt=span.firstChild;
       span.parentNode.replaceChild(txt,span);
     }
   }
}

function parcours (elt,func) {
    var c=elt.firstChild;
    var next;
    while (c) {
	next=c.nextSibling;
	if (c.nodeType==3) {
	    func(c);
	}
	if (c.nodeType==1) {
	    parcours(c,func);
	}
	c=next;
    }	
}

function highlight_text(elt) {
    var textl=chchr2(elt.nodeValue.toLowerCase());
    var texto=elt.nodeValue;
    var re= new RegExp('');
    var delend='';
    var segs;
    var len;
    var indices=[];
    var re= new RegExp();
    if (options.mot_entier) {
	delend='\\b';
    }
    if (lastsearchedwords) {
	for (wsi in lastsearchedwords) {
	    re.compile('\\b'+lastsearchedwords[wsi]+delend);
	    segs=textl.split(re);
	    len=0;
	    for (s=0; s<segs.length-1; s++) {
		len+=segs[s].length
		indices[indices.length]=[len,lastsearchedwords[wsi].length];
		len+=lastsearchedwords[wsi].length;
	    }
	}
	if (indices.length==0)
	    return;

	indices.sort(sortIndices);
	var cc=0;
	var ext='';
	for (i in indices) {
	    ext+=texto.substring(cc,indices[i][0]);
	    if (first_in_topic) {
		ext+="<a id='firstsearchresult' name='firstsearchresult'></a>";
	    }
	    ext+="<span class='hl'>";
	    ext+=texto.substr(indices[i][0],indices[i][1]);
	    ext+="</span>";
	    if (first_in_topic) {
		first_in_topic=false;
		ext+="</a>";
	    }
	    cc=indices[i][0]+indices[i][1];
	}
	ext+=texto.substr(cc);
	
	span=elt.ownerDocument.createElement('SPAN');
	span.innerHTML=ext;
	elt.parentNode.replaceChild(span,elt);
    }
}