(function($) {
var maxZ = 0;
$.fn.easyDrag = function(params) {
if(params == "kill"){
this.each(function(){ var self = $(this); 
var handle = self.data('handle');
handle.off('mousedown', easyDrag_onMouseDown);
handle.off('touchstart', easyDrag_onTouchStart);
handle.css('cursor', '');
self.removeClass('easydrag_enabled');
}); 
} else if(params == 'killall'){ 
$('.easydrag_enabled').easyDrag('kill'); 
} else {
params = $.extend({
handle: '.handle', 
axis: false, 
container: false, 
start: function(){},
drag: function(){},
stop: function(){},
cursor: 'move', 
ontop: true,
clickable: true
}, params);
this.each(function(){ var self = $(this);
if(!self.hasClass('easydrag_enabled')){ 
if(params.handle == 'this' || self.find(params.handle).length==0){
var handle = self;
} else {
var handle = self.find(params.handle);
}
if(params.cursor != ''){ handle.css('cursor', params.cursor); } 
handle.data(params);
var boulet = self;
boulet.addClass('easydrag_enabled'); 
boulet.data('handle', handle); 
handle.data('boulet', boulet);
if(self.css('z-index')!='auto' && params.ontop){
maxZ = Math.max(maxZ, self.css('z-index'));
};
if(self.css('position') != 'absolute' && self.css('position') != 'fixed'){
if(self.css('left') == 'auto'){ self.css('left', '0'); } 
if(self.css('top') == 'auto'){ self.css('top', '0'); }
self.css('position', 'relative');
}
handle.on('mousedown', easyDrag_onMouseDown);
handle.on('touchstart', easyDrag_onTouchStart);
}
});
}
return this;
};
var self, t, boulet, initItemX, initItemY, initEventX, initEventY, axis, container, refX, refY; 
function easyDrag_onMouseDown(event){ event.preventDefault();
t = Date.now();
self = $(this); 
boulet = self.data('boulet');
initItemX = parseInt(boulet.css('left'));
initItemY = parseInt(boulet.css('top'));
axis = self.data('axis');
container = self.data('container');
initEventX = event.pageX;
initEventY = event.pageY;
if(container.length){
refX = self.offset().left;
refY = self.offset().top;
}
self.data('start').call(boulet);
$(document).on('mousemove', easyDrag_onMouseMove);
$(document).on('click', easyDrag_onMouseUp);
if(self.data('ontop')){ 
maxZ++;
boulet.css('z-index', maxZ);
}
}
function easyDrag_onMouseMove(e){ e.preventDefault();
self.data('drag').call(boulet); 
var nextX = initItemX + e.pageX-initEventX;
var nextY = initItemY + e.pageY-initEventY;
if(!axis || axis=='x'){ boulet.css({'left': nextX+'px'}); }
if(!axis || axis=='y'){ boulet.css({'top': nextY+'px'}); }
easyDrag_contain();
}
function easyDrag_onMouseUp(e){ 
$(document).off('mousemove', easyDrag_onMouseMove);
$(document).off('click', easyDrag_onMouseUp);
self.data('stop').call(boulet); 
var d = Date.now() - t;
if(d>300 || !self.data('clickable')){
e.preventDefault(); 
e.stopPropagation();
} 
}
function easyDrag_onTouchStart(event){ event.preventDefault(); 
t = Date.now();
self = $(this); 
boulet = self.data('boulet');
initItemX = parseInt(boulet.css('left'));
initItemY = parseInt(boulet.css('top'));
axis = self.data('axis');
container = self.data('container');
if(container.length){
refX = self.offset().left;
refY = self.offset().top;
}
var touch = event.originalEvent.changedTouches[0];
initEventX = touch.pageX;
initEventY = touch.pageY;
self.data('start').call(boulet);
$(document).on('touchmove', easyDrag_onTouchMove);
$(document).on('touchend', easyDrag_onTouchEnd);
if(self.data('ontop')){ 
maxZ++;
boulet.css('z-index', maxZ);
}
}
function easyDrag_onTouchMove(e){ e.preventDefault();
self.data('drag').call(boulet); 
var touch = e.originalEvent.changedTouches[0];
var nextX = initItemX + touch.pageX-initEventX;
var nextY = initItemY + touch.pageY-initEventY;
if(!axis || axis=='x'){ boulet.css({'left': nextX+'px'}); }
if(!axis || axis=='y'){ boulet.css({'top': nextY+'px'}); }
easyDrag_contain();
}
function easyDrag_onTouchEnd(e){
$(document).off('touchmove', easyDrag_onTouchMove);
$(document).off('touchend', easyDrag_onTouchEnd);
self.data('stop').call(boulet); 
var d = Date.now() - t;
if(d>300 || !self.data('clickable')){
e.preventDefault(); 
e.stopPropagation();
} 
}
function easyDrag_contain(){
if(container.length){
var cur_offset = boulet.offset();
var container_offset = container.offset();
var limite1 = container_offset.left;
var limite2 = limite1+container.width()-boulet.innerWidth();
limite1 += parseInt(boulet.css('margin-left'));
if(cur_offset.left<limite1){
boulet.offset({left: limite1});
} else if(cur_offset.left>limite2){
boulet.offset({left: limite2});
}
var limite1 = container_offset.top;
var limite2 = limite1+container.height()-boulet.innerHeight();
limite1 += parseInt(boulet.css('margin-top'));
if(cur_offset.top<limite1){
boulet.offset({top: limite1});
} else if(cur_offset.top>limite2){
boulet.offset({top: limite2});
}
}
};
})(jQuery);
jQuery.extend({
highlight: function (node, re, nodeName, className) {
if (node.nodeType === 3) {
var match = node.data.match(re);
if (match) {
var highlight = document.createElement(nodeName || 'span');
highlight.className = className || 'highlight';
var wordNode = node.splitText(match.index);
wordNode.splitText(match[0].length);
var wordClone = wordNode.cloneNode(true);
highlight.appendChild(wordClone);
wordNode.parentNode.replaceChild(highlight, wordNode);
return 1; 
}
} else if ((node.nodeType === 1 && node.childNodes) && 
!/^(script|style|text|tspan|textpath)$|(^svg:)/i.test(node.tagName) && 
!(node.tagName === nodeName.toUpperCase() && node.className === className)) { 
for (var i = 0; i < node.childNodes.length; i++) {
i += jQuery.highlight(node.childNodes[i], re, nodeName, className);
}
}
return 0;
}
});
jQuery.fn.unhighlight = function (options) {
var settings = { className: 'highlight', element: 'span' };
jQuery.extend(settings, options);
return this.find(settings.element + "." + settings.className).each(function () {
var parent = this.parentNode;
parent.replaceChild(this.firstChild, this);
parent.normalize();
}).end();
};
jQuery.fn.highlight = function (words, options) {
var settings = { className: 'highlight', element: 'span', caseSensitive: false, wordsOnly: false };
jQuery.extend(settings, options);
if (words.constructor === String) {
words = [words];
}
words = jQuery.grep(words, function(word, i){
return word != '';
});
words = jQuery.map(words, function(word, i) {
return word.replace(/[-[\]{}()*+?.,\\^$|#\s]/g, "\\$&");
});
if (words.length == 0) { return this; };
var flag = settings.caseSensitive ? "" : "i";
var pattern = "(" + words.join("|") + ")";
if (settings.wordsOnly) {
pattern = "\\b" + pattern + "\\b";
}
var re = new RegExp(pattern, flag);
return this.each(function () {
jQuery.highlight(this, re, settings.element, settings.className);
});
};
;(function ($) {
var methods = {
init: function (options) { 
var settings = $.extend({ checked: false, ontoggle: null }, 
options);
var toggle = this.first();
toggle.addClass("toggle-toggle");
toggle.removeData("toggleState"); 
toggle.click(function (event) {
event.preventDefault();
methods.check.call(toggle, "toggle");
});
if (typeof settings.ontoggle === "function") {
toggle.data("onChangeState", settings.ontoggle);
}
methods.check.call(toggle, settings.checked);
return toggle;
},
check: function (checked) { 
var toggle = this.first();
var isChecked = toggle.data("toggleState");
if (checked === "toggle") {
if (typeof isChecked === "boolean") {
checked = !isChecked;
} else {
checked = false;
}
}
if (typeof checked === "boolean") {
var toggle = this.first();
if ((typeof isChecked === "undefined") ||
checked !== isChecked) {
if (checked) {
toggle.addClass("toggle-checked");
} else {
toggle.removeClass("toggle-checked");
}
toggle.data("toggleState", checked);
if (toggle.data("onChangeState")) {
toggle.data("onChangeState").call(toggle, checked);
}
}
return toggle;
} else {
return isChecked;
}
},
};
$.fn.toggle = function (method) {
if (methods[method]) {
return methods[method].apply(
this, 
Array.prototype.slice.call(arguments, 1));
} else if ((typeof method === "object") || !method) {
return methods.init.apply(this, arguments);
} else {
$.error("Method '" + method + "' does not exist in jQuery.toggle");
return this;
} 
};
})(jQuery);
;(function ($) {
var methods = {
init: function (options) { 
var settings = $.extend({ selected: 0, onselect: null }, 
options);
var tabs = this.first();
tabs.addClass("tabs-tabs");
tabs.children("li").each(function (itemIndex) {
$(this).addClass("tabs-tab");
var links = $(this).children("a[href]");
$(this).add(links).click(function (event) {
event.preventDefault();
event.stopImmediatePropagation();
methods.select.call(tabs, itemIndex);
});
});
if (typeof settings.onselect === "function") {
tabs.data("onSelectTab", settings.onselect);
}
methods.select.call(tabs, settings.selected);
return tabs;
},
select: function (index) { 
var tabs = this.first();
if (typeof index === "number") {
var items = tabs.children("li");
if (index < 0) {
index = 0;
} else if (index >= items.length) {
index = items.length - 1;
}
tabs.removeData("selectedTab");
var selected = false;
items.each(function (itemIndex) {
var panel = methods.getPanel.call(tabs, $(this));
if (itemIndex === index) {
$(this).addClass("tabs-selected");
panel.show();
selected = true;
} else {
$(this).removeClass("tabs-selected");
panel.hide();
}
});
if (selected) {
tabs.data("selectedTab", index);
if (tabs.data("onSelectTab")) {
tabs.data("onSelectTab").call(tabs, index);
}
}
return tabs;
} else {
return tabs.data("selectedTab");
}
},
getPanel: function (item) {
var href = item.children("a[href]").first().attr("href");
if (href && href.indexOf("#") === 0) {
return $(href);
} else {
return $();
}
},
};
$.fn.tabs = function (method) {
if (methods[method]) {
return methods[method].apply(
this, 
Array.prototype.slice.call(arguments, 1));
} else if ((typeof method === "object") || !method) {
return methods.init.apply(this, arguments);
} else {
$.error("Method '" + method + "' does not exist in jQuery.tabs");
return this;
} 
};
})(jQuery);
;(function ($) {
var methods = {
init: function (options) { 
var settings = $.extend({ initiallyCollapsed: false }, options);
var toc = this.first();
toc.addClass("toc-toc");
toc.data("toc", settings);
var collapsible = methods.getCollapsibleEntries.call(toc);
if (collapsible.length > 0) {
var hasSingleRoot = (toc.children("li").length === 1);
methods.restoreCollapsibleEntries.call(toc, collapsible, 
hasSingleRoot);
var clickEndX = NaN;
var paddingLeft = collapsible.css("padding-left");
if (paddingLeft.substr(-2) === "px") {
clickEndX = 
parseInt(paddingLeft.substring(0, paddingLeft.length-2));
}
if (isNaN(clickEndX)) {
clickEndX = 16;
}
collapsible.click(function (event) {
var entry = $(this);
var x = event.pageX - entry.offset().left;
if (x >= 0 && x < clickEndX) {
event.stopImmediatePropagation();
var contents = entry.children("ul");
if (entry.hasClass("toc-collapsed")) {
entry.removeClass("toc-collapsed")
.addClass("toc-expanded");
contents.show();
} else {
entry.removeClass("toc-expanded")
.addClass("toc-collapsed");
contents.hide();
}
methods.saveCollapsibleEntries.call(toc, collapsible);
}
});
}
return toc;
},
expandCollapseAll: function (expand) {
var toc = this.first();
var collapsible = methods.getCollapsibleEntries.call(toc);
collapsible.each(function () { 
var entry = $(this);
if (expand && entry.hasClass("toc-collapsed")) {
entry.removeClass("toc-collapsed")
.addClass("toc-expanded");
entry.children("ul").show();
} else if (!expand && entry.hasClass("toc-expanded")) {
entry.removeClass("toc-expanded")
.addClass("toc-collapsed");
entry.children("ul").hide();
}
});
methods.saveCollapsibleEntries.call(toc, collapsible);
return toc;
},
showEntry: function (entry, scroll) {
var toc = this.first();
entry.parents(toc, "li").each(function () {
var e = $(this);
if (e.hasClass("toc-collapsed")) {
e.removeClass("toc-collapsed").addClass("toc-expanded");
e.children("ul").show();
}
});
if (scroll && toc.is(":visible")) {
var scrollable = methods.getScrollParent.call(toc);
scrollable.scrollTop(entry.offset().top - 
scrollable.offset().top);
}
return toc;
},
getScrollParent: function() {
var position = this.css("position");
var excludeStaticParent = (position === "absolute");
var scrollParent = this.parents().filter(function() {
var parent = $(this);
if (excludeStaticParent && 
parent.css("position") === "static") {
return false;
}
return (/(auto|scroll)/).test(parent.css("overflow") + 
parent.css("overflow-y") + 
parent.css("overflow-x"));
}).eq(0);
return (position === "fixed" || scrollParent.length === 0)? 
$(this[0].ownerDocument || document) : scrollParent;
},
getCollapsibleEntries: function () {
return $("li", this).filter(function () {
return $(this).children("ul").length > 0;
});
},
saveCollapsibleEntries: function (collapsible) {
var settings = this.data("toc");
if (settings.storageKey) {
var state = [];
collapsible.each(function () {
state.push($(this).hasClass("toc-collapsed")? 0 : 1);
});
window.sessionStorage.setItem(settings.storageKey,
state.join(""));
}
},
restoreCollapsibleEntries: function (collapsible, hasSingleRoot) {
var fallback = true;
var settings = this.data("toc");
if (settings.storageKey) {
var storedValue = 
window.sessionStorage.getItem(settings.storageKey);
if (storedValue) {
var state = storedValue.split("");
if (state.length === collapsible.length) {
fallback = false;
collapsible.each(function (index) {
var entry = $(this);
var contents = entry.children("ul");
if (parseInt(state[index], 10) === 0) {
entry.addClass("toc-collapsed");
contents.hide();
} else {
entry.addClass("toc-expanded");
contents.show();
}
});
}
}
}
if (fallback) {
if (settings.initiallyCollapsed) {
collapsible.each(function (index) {
var entry = $(this);
if (hasSingleRoot && index === 0) {
entry.addClass("toc-expanded");
} else {
entry.addClass("toc-collapsed");
entry.children("ul").hide();
}
});
} else {
collapsible.each(function (index) {
$(this).addClass("toc-expanded");
});
}
}
}
};
$.fn.toc = function (method) {
if (methods[method]) {
return methods[method].apply(
this, 
Array.prototype.slice.call(arguments, 1));
} else if ((typeof method === "object") || !method) {
return methods.init.apply(this, arguments);
} else {
$.error("Method '" + method + "' does not exist in jQuery.toc");
return this;
} 
};
})(jQuery);
var wh = (function () {
var toc_entries = [
["Instalace a provoz programu OmegaT\n                     ","chapter.installing.and.running.html",[
["Uživatelé Windows","chapter.installing.and.running.html#d0e17",null],
["Uživatelé Linuxu (Intel)","chapter.installing.and.running.html#Linux.Intel.users",null],
["Uživatelé macOS","chapter.installing.and.running.html#MAC.OSX.users",null],
["Ostatní systémy","chapter.installing.and.running.html#other.systems",null],
["Přetáhnout","chapter.installing.and.running.html#drag.and.drop",null],
["Použití Java Web Start","chapter.installing.and.running.html#using.Java.Web.Start",null],
["Spouštění z příkazového řádku","chapter.installing.and.running.html#d0e593",null],
["Sestavení OmegaT ze zdrojového kódu","chapter.installing.and.running.html#building.OmegaT.from.source",null]
]],
["Nabídka","menus.html",[
["Projekt","menus.html#menus.project",null],
["Upravit","menus.html#menus.edit",null],
["Přejít","menus.html#menus.goto",null],
["Zobrazit","menus.html#menus.view",null],
["Nástroje","menus.html#menus.tools",null],
["Možnosti","menus.html#menus.options",null],
["Nápověda","menus.html#menus.help",null]
]],
["Okna a dialogová okna","windows.and.dialogs.html",[
["Vlastnosti projektu","windows.and.dialogs.html#dialogs.projectproperties",null],
["Soubory projektu","windows.and.dialogs.html#windows.projectfiles",null],
["Hledat v textu","windows.and.dialogs.html#windows.textsearch",null],
["Nahradit text","windows.and.dialogs.html#windows.textreplace",null],
["Párovací nástroj","windows.and.dialogs.html#windows.aligner",null],
["Skriptování","windows.and.dialogs.html#windows.scripts",null],
["Obecné předvolby","windows.and.dialogs.html#dialogs.preferences",null]
]],
["Podokna","panes.html",[
["V Editoru","panes.html#editor.pane",null],
["Přibližné překlady","panes.html#d0e4346",null],
["Glosář","panes.html#d0e4373",null],
["Slovník","panes.html#d0e4386",null],
["Strojový překlad","panes.html#d0e4394",null],
["Překlad s více významy","panes.html#d0e4402",null],
["Poznámky","panes.html#d0e4412",null],
["Komentáře","panes.html#d0e4422",null],
["Stavová lišta","panes.html#d0e4427",null]
]],
["Adresář projektu","project.folder.html",[
["source","project.folder.html#d0e4467",null],
["target","project.folder.html#d0e4472",null],
["tm","project.folder.html#d0e4495",null],
["dictionary","project.folder.html#d0e4609",null],
["glossary","project.folder.html#d0e4617",null],
["O aplikaci OmegaT","project.folder.html#folder.omegat",null],
["soubor omegat.project","project.folder.html#d0e4684",null],
[".repositories","project.folder.html#d0e4692",null]
]],
["Jak na to...","howtos.html",[
["Nastavit týmový projekt","howtos.html#howto.setupteamproject",null],
["Využití týmového projektu","howtos.html#howto.useteamproject",null],
["Opětovné používání překladových pamětí","howtos.html#howto.reusetm",null],
["Prevence ztráty dat","howtos.html#howto.preventdataloss",null],
["Překlad souboru PDF","howtos.html#howto.translatepdf",null],
["Spravovat jazyky se zápisem zprava doleva","howtos.html#howto.managerighttoleft",null]
]],
["A. Slovníky","appendix.dictionaries.html",[
["Jak stáhnout a nainstalovat slovníky","appendix.dictionaries.html#d0e5429",null],
["Problémy se slovníky","appendix.dictionaries.html#d0e5498",null]
]],
["B. Glosáře","appendix.glossaries.html",[
["Používání","appendix.glossaries.html#d0e5550",null],
["Formát souboru","appendix.glossaries.html#d0e5589",null],
["Jak glosáře vytvořit","appendix.glossaries.html#d0e5624",null],
["Prioritní glosář","appendix.glossaries.html#d0e5660",null],
["Obecné problémy glosáře","appendix.glossaries.html#d0e5667",null]
]],
["C. Kontrola pravopisu","appendix.spellchecker.html",[
["Instalace slovníků kontroly pravopisu","appendix.spellchecker.html#d0e5717",null],
["Použití slovníků kontroly pravopisu","appendix.spellchecker.html#d0e5766",null],
["Tipy","appendix.spellchecker.html#hints",null]
]],
["D. Regulární výrazy","appendix.regexp.html",[
["Nástroje regex a příklady použití","appendix.regexp.html#regex.tools.and.examples.of.use",null]
]],
["E. Přizpůsobení klávesových zkratek","appendix.shortcut.custom.html",[
["Přizpůsobení klávesových zkratek","appendix.shortcut.custom.html#ch04.shortcuts.customization",null],
["Nabídka ‚Projekt‛","appendix.shortcut.custom.html#ch04.shortcuts.ProjectMenu",null],
["Nabídka ‚Upravit‛","appendix.shortcut.custom.html#ch04.shortcuts.EditMenu",null],
["Nabídka ‚Přejít‛","appendix.shortcut.custom.html#ch04.shortcuts.GoToMenu",null],
["Nabídka ‚Zobrazit‛","appendix.shortcut.custom.html#ch04.shortcuts.ViewMenu",null],
["Nabídka ‚Nástroje‛","appendix.shortcut.custom.html#ch04.shortcuts.ToolsMenu",null],
["Nabídka ‚Možnosti‛","appendix.shortcut.custom.html#ch04.shortcuts.OptionsMenu",null],
["Nabídka ‚Nápověda‛","appendix.shortcut.custom.html#ch04.shortcuts.HelpMenu",null]
]]];
var toc_initiallyCollapsed = false;
var messages = [
"Contents",
"Index",
"Search",
"Collapse All",
"Expand All",
"Previous Page",
"Next Page",
"Print Page",
"Toggle search result highlighting",
"No results found for %W%.",
"1 result found for %W%.",
"%N% results found for %W%.",
"Stop searching",
"Open navigation pane",
"Close navigation pane",
"term",
"word",
"Go",
"See",
"See also" 
];
var messageTranslations = {
"de": [
"Inhalt",
"Index",
"Suchen",
"Alle ausblenden",
"Alle einblenden",
"Vorherige Seite",
"Nächste Seite",
"Print Page",
"Hervorhebung von Suchergebnissen ein-/ausschalten",
"Keine Ergebnisse für %W% gefunden.",
"1 Ergebnis für %W% gefunden.",
"%N% Ergebnisse für %W% gefunden.",
"Stop searching",
"Open navigation pane",
"Close navigation pane",
"term",
"word",
"Go",
"Siehe",
"Siehe auch"
],
"es": [
"Contenido",
"Índice",
"Buscar",
"Contraer todo",
"Expandir todo",
"Página anterior",
"Página siguiente",
"Print Page",
"Alternar el resaltado de los resultados de la búsqueda",
"No se ha encontrado ningún resultado para %W%.",
"Se ha encontrado un resultado para %W%.",
"Se han encontrado %N% resultados para %W%.",
"Stop searching",
"Open navigation pane",
"Close navigation pane",
"term",
"word",
"Go",
"Ver",
"Ver también"
],
"fr": [
"Sommaire",
"Index",
"Rechercher",
"Replier Tout",
"Déplier Tout",
"Page Précédente",
"Page Suivante",
"Imprimer Page",
"Basculer la mise en surbrillance",
"Aucun résultat trouvé pour %W%.",
"1 résultat trouvé pour %W%.",
"%N% résultats trouvés pour %W%.",
"Arrêter de rechercher",
"Ouvrir le panneau de navigation",
"Fermer le panneau de navigation",
"terme",
"mot",
"Atteindre",
"Voir",
"Voir aussi"
],
"it": [
"Sommario",
"Indice",
"Ricerca",
"Comprimi tutto",
"Espandi tutto",
"Pagina precedente",
"Pagina successiva",
"Print Page",
"Attiva/Disattiva evidenziazione risultati ricerca",
"Nessun risultato trovato per %W%.",
"1 risultato trovato per %W%.",
"%N% risultati trovati per %W%.",
"Stop searching",
"Open navigation pane",
"Close navigation pane",
"term",
"word",
"Go",
"Vedere",
"Vedere anche"
],
"ja": [
"目次",
"索引",
"検索",
"すべて折りたたむ",
"すべて展開",
"前のページ",
"次のページ",
"Print Page",
"検索キーワードをハイライト表示",
"%W% の検索結果は見つかりませんでした。",
"%W% の検索結果が 1 件見つかりました。",
"%W% の検索結果が%N% 件見つかりました。%N%",
"Stop searching",
"Open navigation pane",
"Close navigation pane",
"term",
"word",
"Go",
"参照：",
"その他参照："
],
"pl": [
"Spis treści",
"Indeks",
"Wyszukaj",
"Zwiń wszystko",
"Rozwiń wszystko",
"Poprzednia strona",
"Następna strona",
"Print Page",
"Przełącz wyróżnianie wyników wyszukiwania",
"Brak wyników dla %W%.",
"Znaleziono 1 wynik dla %W%.",
"Znaleziono następującą liczbę wyników dla %W%: %N%",
"Stop searching",
"Open navigation pane",
"Close navigation pane",
"term",
"word",
"Go",
"Zobacz",
"Zobacz również"
],
"ru": [
"Содержание",
"Указатель",
"Поиск",
"Свернуть все",
"Развернуть все",
"Предыдущая",
"Следующая",
"Print Page",
"Выделение результатов поиска",
"Ничего не найдено по запросу \"%W%\".",
"Найдено результатов по запросу \"%W%\": 1.",
"Найдено результатов по запросу \"%W%\": %N%.",
"Stop searching",
"Open navigation pane",
"Close navigation pane",
"term",
"word",
"Go",
"См.",
"См. также"
],
"zh-cn": [
"目录",
"索引",
"搜索",
"全部折叠",
"全部展开",
"上一页",
"下一页",
"Print Page",
"切换搜索结果高亮",
"未找到有关 %W% 的结果。",
"找到 1 条有关 %W% 的结果。",
"找到 %N% 条有关 %W% 的结果。",
"Stop searching",
"Open navigation pane",
"Close navigation pane",
"term",
"word",
"Go",
"See",
"See also"
],
"zh-tw": [
"目錄",
"索引",
"搜尋",
"收合全部",
"展開全部",
"上一頁",
"下一頁",
"Print Page",
"反白顯示切換搜尋結果",
"找不到 %W% 的結果。",
"找到 １ 個 %W% 的結果。",
"找到 %N% 個 %W% 的結果。",
"Stop searching",
"Open navigation pane",
"Close navigation pane",
"term",
"word",
"Go",
"See",
"See also"
]
};
var preferredUserLanguage = null; 
function getUserLanguage(lang) {
if (lang === null) {
lang = window.navigator.userLanguage || window.navigator.language;
}
if (lang) {
lang = lang.toLowerCase();
if (lang.length > 5) {
lang = lang.substring(0, 5);
}
if (lang.indexOf("_") >= 0) {
lang = lang.replace(/_/g, "-");
}
if (lang in messageTranslations) {
return lang;
} else {
var pos = lang.indexOf("-");
if (pos > 0) {
lang = lang.substring(0, pos);
}
if (lang in messageTranslations) {
return lang;
} else {
return null;
}
}
} else {
return null;
}
}
var userLanguage = getUserLanguage(preferredUserLanguage);
function msg(message) {
if (userLanguage !== null) {
var translation = messageTranslations[userLanguage];
if (translation !== undefined) {
var index = -1;
var count = messages.length;
for (var i = 0; i < count; ++i) {
if (messages[i] === message) {
index = i;
break;
}
}
if (index >= 0) {
message = translation[index];
}
}
}
return message;
}
var storageId = "-18jokhlyp3r0l-1nkgmq17475d4";
function storageSet(key, value) {
window.sessionStorage.setItem(key + storageId, String(value));
}
function storageGet(key) {
return window.sessionStorage.getItem(key + storageId);
}
function storageDelete(key) {
window.sessionStorage.removeItem(key + storageId);
}
function initMenu() {
var menu = $("#wh-menu");
menu.attr("title", msg("Open navigation pane"));
menu.click(function () {
if (menu.hasClass("wh-icon-menu")) {
openNavigation();
} else {
closeNavigation();
}
});
}
function openNavigation() {
var menu = $("#wh-menu");
menu.removeClass("wh-icon-menu").addClass("wh-icon-close");
menu.attr("title", msg("Close navigation pane"));
var glass = $('<div id="wh-body-glass"></div>');
glass.css({ "position": "absolute",
"top": "0px",
"left": "0px",
"z-index": "50",
"width": "100%",
"height": "100%",
"background-color": "#808080",
"opacity": "0.5" });
$("body").append(glass);
glass.click(closeNavigation);
var top = menu.position().top;
top += menu.outerHeight( false);
var height = $("#wh-body").height() - top;
var nav = $("#wh-navigation");
nav.css({ "position": "absolute",
"top": top + "px",
"right": "0px",
"z-index": "100",
"width": "66%",
"height": height + "px",
"border-style": "solid",
"display": "flex" }); 
}
function closeNavigation() {
var menu = $("#wh-menu");
menu.removeClass("wh-icon-close").addClass("wh-icon-menu");
menu.attr("title", msg("Open navigation pane"));
$("#wh-body-glass").remove();
var nav = $("#wh-navigation");
nav.css({ "position": "",
"top": "",
"right": "",
"z-index": "",
"width": "",
"height": "",
"border-style": "",
"display": "" });
var position = parseInt(storageGet("whSeparatorPosition"), 10);
if (!isNaN(position)) {
nav.width(position);
}
}
function initSeparator() {
var navigation = $("#wh-navigation");
var separator = $("#wh-separator");
var content = $("#wh-content");
separator.easyDrag({
axis: "x",
container: $("#wh-body"),
clickable: false,
cursor: "", 
start: function() { 
$(this).data("startDragLeftOffset", $(this).offset().left);
},
stop: function() {
var delta = 
$(this).offset().left - $(this).data("startDragLeftOffset");
if (delta !== 0) {
var availableW = $("#wh-body").width();
var reservedW = 1 + getPad(navigation,  false)/2 +
separator.outerWidth( true) +
getPad(content,  false)/2;
var maxW = availableW - reservedW;
var w = navigation.width() + delta;
if (w < reservedW) {
w = reservedW; 
} else if (w > maxW) {
w = maxW;
}
saveSeparatorPosition(separator, w);
navigation.width(w);
}
}
});
var position = parseInt(storageGet("whSeparatorPosition"), 10);
if (isNaN(position)) {
position = navigation.width();
}
saveSeparatorPosition(separator, position);
navigation.width(position);
}
function getPad(pane, vertical) {
if (vertical) {
return pane.outerHeight( true) - pane.height();
} else {
return pane.outerWidth( true) - pane.width();
}
}
function saveSeparatorPosition(separator, position) {
separator.css("left", "0px");
storageSet("whSeparatorPosition", position.toString());
}
function populateTOC() {
var tocPane = $("#wh-toc-pane");
var list = $("<ul id='wh-toc'></ul>");
tocPane.append(list);
if (typeof toc_entries !== "undefined") {
var count = toc_entries.length;
for (var i = 0; i < count; ++i) {
addTOCEntry(toc_entries[i], list);
}
toc_entries = undefined; 
}
}
function addTOCEntry(entry, list) {
var text = entry[0];
var href = entry[1];
var children = entry[2];
var count = (children !== null)? children.length : 0;
var item = $("<li></li>");
list.append(item);
if (href !== null) {
var link = $("<a></a>");
link.attr("href", href);
link.html(text);
item.append(link);
} else {
item.html(text);
}
if (count > 0) {
var sublist = $("<ul></ul>");
item.append(sublist);
for (var i = 0; i < count; ++i) {
addTOCEntry(children[i], sublist);
}
}
}
function doInitTOC() {
populateTOC();
var toc = $("#wh-toc");
var tocOptions = { storageKey: ("whTOCState" + storageId) };
if ((typeof toc_initiallyCollapsed !== "undefined") &&
toc_initiallyCollapsed) {
tocOptions.initiallyCollapsed = true;
}
toc.toc(tocOptions);
}
var fieldKeys = {
ENTER: 13,
ESCAPE: 27,
UP: 38,
DOWN: 40
};
function startSearch(field) {
stopSearch(field);
var query = $.trim(field.val());
if (query.length === 0) {
field.val("");
return null;
}
var words = splitWords(query);
if (words === null) {
field.val("");
return null;
}
return [query, words];
}
function splitWords(query) {
var split = query.split(/\s+/);
var words = [];
for (var i = 0; i < split.length; ++i) {
var segment = split[i];
if (stringStartsWith(segment, '"') || stringStartsWith(segment, "'")) {
segment = segment.substring(1);
}
if (stringEndsWith(segment, '"') || stringEndsWith(segment, "'")) {
segment = segment.substring(0, segment.length-1);
}
if (segment.length > 0) {
words.push(segment.toLowerCase());
}
}
if (words.length === 0) {
words = null;
}
return words;
}
function stringStartsWith(text, prefix) {
return (text.indexOf(prefix) === 0);
}
function stringEndsWith(text, suffix) {
return (text.substr(-suffix.length) === suffix);
}
function stopSearch(field) {
$("#wh-search-results").empty();
var pane = $("#wh-search-pane");
pane.scrollTop(0);
var words = pane.removeData("whSearchedWords2");
if (words !== null) {
unhighlightSearchedWords();
}
clearSearchState();
}
function highlightSearchedWords(words) {
$("#wh-content").highlight(words, 
{ caseSensitive: false, 
className: "wh-highlighted" });
}
function unhighlightSearchedWords() {
$("#wh-content").unhighlight({ className: "wh-highlighted" });
}
function doSearch(query, words) {
var searchResults = $("#wh-search-results");
var searchedWords = [];
var resultIndices = findWords(words, searchedWords);
displaySearchResults(query, words, searchedWords, 
resultIndices, searchResults);
saveSearchState(query, words, searchedWords, resultIndices);
}
function displaySearchResults(query, words, searchedWords, 
resultIndices, searchResults) {
searchResults.empty();
if (resultIndices === null || resultIndices.length === 0) {
searchResults.append(searchResultHeader(0, words));
return;
}
searchResults.append(searchResultHeader(resultIndices.length, words));
searchResults.append(searchResultList(resultIndices));
var resultLinks = $("#wh-search-result-list a");
highlightSearchedWordsImmediately(searchedWords, resultLinks);
var currentPage = trimFragment(window.location.href);
resultLinks.click(function (event) {
if (this.href === currentPage) {
event.preventDefault();
} 
});
}
function findWords(words, searchedWords) {
var pageCount = wh.search_baseNameList.length;
var hits = new Array(pageCount);
var i, j, k;
for (i = 0; i < pageCount; ++i) {
hits[i] = 0;
}
var wordCount = words.length;
for (i = 0; i < wordCount; ++i) {
var indices;
var fallback = true;
var word = words[i];
if (wh.search_stemmer !== null && 
word.search(/^[-+]?\d/) < 0) { 
var stem = wh.search_stemmer.stemWord(word);
if (stem != word) {
indices = wh.search_wordMap[stem];
if (indices !== undefined) {
fallback = false;
searchedWords.push(stem);
if (word.indexOf(stem) < 0) {
searchedWords.push(word);
}
}
}
}
if (fallback) {
indices = wh.search_wordMap[word];
searchedWords.push(word);
}
if (indices !== undefined) {
var hitPageCount = 0;
var indexCount = indices.length;
for (j = 0; j < indexCount; ++j) {
var index = indices[j];
if ($.isArray(index)) {
hitPageCount += index.length;
} else {
++hitPageCount;
}
}
var unit = 100.0 * ((pageCount - hitPageCount + 1)/pageCount);
for (j = 0; j < indexCount; ++j) {
var index = indices[j];
if ($.isArray(index)) {
var hitIncr = 
10000.0 + (((indexCount - j)/indexCount) * unit);
for (k = 0; k < index.length; ++k) {
hits[index[k]] += hitIncr;
}
} else {
hits[index] += 
10000.0 + (((indexCount - j)/indexCount) * unit);
}
}
} else {
return null;
}
}
var resultIndices = [];
var minHitValue = 10000.0 * wordCount; 
for (i = 0; i < pageCount; ++i) {
if (hits[i] > minHitValue) {
resultIndices.push(i);
}
}
if (resultIndices.length === 0) {
resultIndices = null;
} else if (resultIndices.length > 1) {
function comparePageIndices(i, j) {
var delta = hits[j] - hits[i];
if (delta !== 0) {
return delta;
} else {
return (i - j);
}
};
resultIndices.sort(comparePageIndices);
}
return resultIndices;
}
function searchResultHeader(resultCount, words) {
var header = $("<div id='wh-search-result-header'></div>");
var message;
switch (resultCount) {
case 0:
message = msg("No results found for %W%.");
break;
case 1:
message = msg("1 result found for %W%.");
break;
default:
message = 
msg("%N% results found for %W%.").replace(new RegExp("%N%", "g"),
resultCount.toString());
}
message = escapeHTML(message);
var spans = "";
for (var i = 0; i < words.length; ++i) {
if (i > 0) {
spans += " ";
}
spans += "<span class='wh-highlighted'>";
spans += escapeHTML(words[i]);
spans += "</span>";
}
header.html(message.replace(new RegExp("%W%", "g"), spans));
return header;
}
function escapeHTML(text) {
return text.replace(/&/g, "&amp;")
.replace(/</g, "&lt;")
.replace(/>/g, "&gt;")
.replace(/"/g, "&quot;");
}
function searchResultList(resultIndices) {
var list = $("<ul id='wh-search-result-list'></ul>");
var resultCount = resultIndices.length;
for (var i = 0; i < resultCount; ++i) {
var index = resultIndices[i];
var item = $("<li class='wh-search-result-item'></li>");
if ((i % 2) === 1) {
item.addClass("wh-odd-item");
}
list.append(item);
var link = $("<a></a>");
link.attr("href", wh.search_baseNameList[index]);
link.html(wh.search_titleList[index]);
item.append(link);
}
return list;
}
function highlightSearchedWordsImmediately(searchedWords, resultLinks) {
var currentPage = trimFragment(window.location.href);
var resultLink = resultLinks.filter(function () {
return this.href === currentPage;
});
if (resultLink.length === 1) {
$("#wh-search-pane").data("whSearchedWords2", searchedWords);
var highlightToggle = $("#wh-search-highlight");
if (highlightToggle.length === 0 || highlightToggle.toggle("check")) {
highlightSearchedWords(searchedWords);
}
}
}
function saveSearchState(query, words, searchedWords, resultIndices) {
storageSet("whSearchQuery", query);
storageSet("whSearchedWords", words.join(" "));
storageSet("whSearchedWords2", searchedWords.join(" "));
storageSet("whSearchResults", 
((resultIndices === null || resultIndices.length === 0)? 
"" : resultIndices.join(",")));
}
function clearSearchState() {
storageDelete("whSearchQuery");
storageDelete("whSearchedWords");
storageDelete("whSearchedWords2");
storageDelete("whSearchResults");
}
function restoreSearchState(field) {
var query = storageGet("whSearchQuery");
if (query) {
var words = storageGet("whSearchedWords");
var searchedWords = storageGet("whSearchedWords2");
var list = storageGet("whSearchResults");
if (query.length > 0 && 
words !== undefined && 
searchedWords !== undefined && 
list !== undefined) {
words = words.split(" ");
if (words.length > 0) {
searchedWords = searchedWords.split(" ");
if (searchedWords.length > 0) {
var resultIndices = [];
if (list.length > 0) {
var items = list.split(",");
var count = items.length;
for (var i = 0; i < count; ++i) {
var index = parseInt(items[i], 10);
if (index >= 0) {
resultIndices.push(index);
} else {
return;
}
}
}
field.val(query);
displaySearchResults(query, words, searchedWords,
resultIndices, $("#wh-search-results"));
}
}
}
}
}
function initContent() {
selectTOCEntry(window.location.href);
$("#wh-toc a[href], #wh-content a[href]").click(function () {
if (trimFragment(this.href) === trimFragment(window.location.href)) {
selectTOCEntry(this.href);
}
});
}
function trimFragment(href) {
var hash = href.lastIndexOf("#");
if (hash >= 0) {
return href.substring(0, hash);
} else {
return href;
}
}
function selectTOCEntry(url) {
var links = $("#wh-toc a");
links.removeClass("wh-toc-selected");
var selectable = links.filter(function () {
return (this.href === url);
});
var hash;
if (selectable.length === 0 && (hash = url.lastIndexOf("#")) >= 0) {
url = url.substring(0, hash);
selectable = links.filter(function () {
return (this.href === url);
});
}
if (selectable.length === 0) {
selectable = links.filter(function () {
return (trimFragment(this.href) === url);
});
}
if (selectable.length > 0) {
selectable = selectable.first();
selectable.addClass("wh-toc-selected");
var entry = selectable.parent("li");
$("#wh-toc").toc("showEntry", entry,  false);
var pane = $("#wh-toc-pane");
if (pane.is(":visible")) {
pane.removeData("whPendingScroll");
pane.scrollTop(entry.offset().top - pane.offset().top);
} else {
pane.data("whPendingScroll", { container: pane, component: entry });
}
}
}
function processPendingScroll(pane) {
var scroll = pane.data("whPendingScroll");
if (scroll !== undefined) {
pane.removeData("whPendingScroll");
scroll.container.scrollTop(scroll.component.offset().top - 
scroll.container.offset().top);
}
}
function layout(resizeEvent) {
var menu = $("#wh-menu");
if (menu.hasClass("wh-icon-close")) {
if (resizeEvent === null) {
closeNavigation();
} else if (window.matchMedia("(max-width: 575.98px)").matches) {
var top = menu.position().top;
top += menu.outerHeight( false);
var height = $("#wh-body").height() - top;
$("#wh-navigation").css("height", height + "px");
} else {
closeNavigation();
}
}
var h = $(window).height();
var pane = $("#wh-header");
if (pane.length > 0 && pane.is(":visible")) {
h -= pane.outerHeight( true);
}
pane = $("#wh-footer");
if (pane.length > 0 && pane.is(":visible")) {
h -= pane.outerHeight( true);
}
var body = $("#wh-body");
body.outerHeight(h,  true);
}
function scrollToFragment() {
var fragment = getFragment(window.location.href);
if (fragment !== null) {
fragment = fragment.replace(/\./g, "\\.");
var anchor = $(fragment);
if (anchor) {
var content = $("#wh-content");
content.scrollTop(anchor.offset().top - content.offset().top + 
content.scrollTop());
}
}
}
function getFragment(href) {
var hash = href.lastIndexOf("#");
if (hash >= 0) {
return href.substring(hash); 
} else {
return null;
}
}
 function initPage() {
initMenu();
initSeparator();
initNavigation();
initTOC();
var searchField = $("#wh-search-field");
initSearch(searchField);
initContent();
$(window).resize(layout);
layout( null);
restoreSearchState(searchField);
scrollToFragment();
}
function initNavigation() {
$("#wh-do-search").attr("title", msg("Search"));
$("#wh-search-field").attr("placeholder", msg("Search"));
$("#wh-cancel-search").attr("title", msg("Stop searching"));
var selectedPane = 0;
var storedValue = storageGet("whSelectedPane");
if (storedValue) {
selectedPane = parseInt(storedValue);
}
selectPane(selectedPane);
}
function selectPane(index) {
storageSet("whSelectedPane", index);
var tocPane = $("#wh-toc-pane");
var searchPane = $("#wh-search-pane");
var cancelSearchButton = $("#wh-cancel-search");
var searchForm = $("#wh-navigation-form");
if (index === 0) {
if (searchPane.is(":visible")) {
searchPane.hide();
cancelSearchButton.css({ "visibility": "hidden", "opacity": "0" });
tocPane.show(); 
processPendingScroll(tocPane);
}
} else {
if (tocPane.is(":visible")) {
tocPane.hide();
cancelSearchButton.css({ "visibility": "visible", "opacity": "1" });
$("#wh-search-field").focus();
searchPane.show(); 
processPendingScroll(searchPane);
}
}
}
function initTOC() {
doInitTOC();
}
function initSearch(field) {
field.attr("autocomplete", "off").attr("spellcheck", "false");
field.keyup(function (event) {
switch (event.which) {
case fieldKeys.ENTER:
search(field);
break;
case fieldKeys.ESCAPE:
cancelSearch(field);
break;
}
});
$("#wh-do-search").click(function (event) {
search(field);
});
$("#wh-cancel-search").click(function (event) { 
cancelSearch(field);
});
}
function search(field) {
var pair = startSearch(field);
if (pair === null) {
return;
}
if (!$("#wh-search-pane").is(":visible")) {
selectPane(1);
}
doSearch(pair[0], pair[1]);
}
function cancelSearch(field) {
field.val("");
stopSearch(field);
if (!$("#wh-toc-pane").is(":visible")) {
selectPane(0);
}
}
return {
initPage: initPage,
}
})();
$(document).ready(function() {
wh.initPage();
$("#wh-body").css({ "visibility": "visible", "opacity": "1" }); 
});
