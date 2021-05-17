function Snowball() {
BaseStemmer = function() {
this.setCurrent = function(value) {
this.current = value;
this.cursor = 0;
this.limit = this.current.length;
this.limit_backward = 0;
this.bra = this.cursor;
this.ket = this.limit;
};
this.getCurrent = function() {
return this.current;
};
this.copy_from = function(other) {
this.current = other.current;
this.cursor = other.cursor;
this.limit = other.limit;
this.limit_backward = other.limit_backward;
this.bra = other.bra;
this.ket = other.ket;
};
this.in_grouping = function(s, min, max) {
if (this.cursor >= this.limit) return false;
var ch = this.current.charCodeAt(this.cursor);
if (ch > max || ch < min) return false;
ch -= min;
if ((s[ch >>> 3] & (0x1 << (ch & 0x7))) == 0) return false;
this.cursor++;
return true;
};
this.in_grouping_b = function(s, min, max) {
if (this.cursor <= this.limit_backward) return false;
var ch = this.current.charCodeAt(this.cursor - 1);
if (ch > max || ch < min) return false;
ch -= min;
if ((s[ch >>> 3] & (0x1 << (ch & 0x7))) == 0) return false;
this.cursor--;
return true;
};
this.out_grouping = function(s, min, max) {
if (this.cursor >= this.limit) return false;
var ch = this.current.charCodeAt(this.cursor);
if (ch > max || ch < min) {
this.cursor++;
return true;
}
ch -= min;
if ((s[ch >>> 3] & (0X1 << (ch & 0x7))) == 0) {
this.cursor++;
return true;
}
return false;
};
this.out_grouping_b = function(s, min, max) {
if (this.cursor <= this.limit_backward) return false;
var ch = this.current.charCodeAt(this.cursor - 1);
if (ch > max || ch < min) {
this.cursor--;
return true;
}
ch -= min;
if ((s[ch >>> 3] & (0x1 << (ch & 0x7))) == 0) {
this.cursor--;
return true;
}
return false;
};
this.eq_s = function(s)
{
if (this.limit - this.cursor < s.length) return false;
if (this.current.slice(this.cursor, this.cursor + s.length) != s)
{
return false;
}
this.cursor += s.length;
return true;
};
this.eq_s_b = function(s)
{
if (this.cursor - this.limit_backward < s.length) return false;
if (this.current.slice(this.cursor - s.length, this.cursor) != s)
{
return false;
}
this.cursor -= s.length;
return true;
};
 this.find_among = function(v)
{
var i = 0;
var j = v.length;
var c = this.cursor;
var l = this.limit;
var common_i = 0;
var common_j = 0;
var first_key_inspected = false;
while (true)
{
var k = i + ((j - i) >>> 1);
var diff = 0;
var common = common_i < common_j ? common_i : common_j; 
var w = v[k];
var i2;
for (i2 = common; i2 < w[0].length; i2++)
{
if (c + common == l)
{
diff = -1;
break;
}
diff = this.current.charCodeAt(c + common) - w[0].charCodeAt(i2);
if (diff != 0) break;
common++;
}
if (diff < 0)
{
j = k;
common_j = common;
}
else
{
i = k;
common_i = common;
}
if (j - i <= 1)
{
if (i > 0) break; 
if (j == i) break; 
if (first_key_inspected) break;
first_key_inspected = true;
}
}
do {
var w = v[i];
if (common_i >= w[0].length)
{
this.cursor = c + w[0].length;
if (w.length < 4) return w[2];
var res = w[3](this);
this.cursor = c + w[0].length;
if (res) return w[2];
}
i = w[1];
} while (i >= 0);
return 0;
};
this.find_among_b = function(v)
{
var i = 0;
var j = v.length
var c = this.cursor;
var lb = this.limit_backward;
var common_i = 0;
var common_j = 0;
var first_key_inspected = false;
while (true)
{
var k = i + ((j - i) >> 1);
var diff = 0;
var common = common_i < common_j ? common_i : common_j;
var w = v[k];
var i2;
for (i2 = w[0].length - 1 - common; i2 >= 0; i2--)
{
if (c - common == lb)
{
diff = -1;
break;
}
diff = this.current.charCodeAt(c - 1 - common) - w[0].charCodeAt(i2);
if (diff != 0) break;
common++;
}
if (diff < 0)
{
j = k;
common_j = common;
}
else
{
i = k;
common_i = common;
}
if (j - i <= 1)
{
if (i > 0) break;
if (j == i) break;
if (first_key_inspected) break;
first_key_inspected = true;
}
}
do {
var w = v[i];
if (common_i >= w[0].length)
{
this.cursor = c - w[0].length;
if (w.length < 4) return w[2];
var res = w[3](this);
this.cursor = c - w[0].length;
if (res) return w[2];
}
i = w[1];
} while (i >= 0);
return 0;
};
this.replace_s = function(c_bra, c_ket, s)
{
var adjustment = s.length - (c_ket - c_bra);
this.current = this.current.slice(0, c_bra) + s + this.current.slice(c_ket);
this.limit += adjustment;
if (this.cursor >= c_ket) this.cursor += adjustment;
else if (this.cursor > c_bra) this.cursor = c_bra;
return adjustment;
};
this.slice_check = function()
{
if (this.bra < 0 ||
this.bra > this.ket ||
this.ket > this.limit ||
this.limit > this.current.length)
{
return false;
}
return true;
};
this.slice_from = function(s)
{
var result = false;
if (this.slice_check())
{
this.replace_s(this.bra, this.ket, s);
result = true;
}
return result;
};
this.slice_del = function()
{
return this.slice_from("");
};
this.insert = function(c_bra, c_ket, s)
{
var adjustment = this.replace_s(c_bra, c_ket, s);
if (c_bra <= this.bra) this.bra += adjustment;
if (c_bra <= this.ket) this.ket += adjustment;
};
this.slice_to = function()
{
var result = '';
if (this.slice_check())
{
result = this.current.slice(this.bra, this.ket);
}
return result;
};
this.assign_to = function()
{
return this.current.slice(0, this.limit);
};
};
EnglishStemmer = function() {
var base = new BaseStemmer();
 var a_0 = [
["arsen", -1, -1],
["commun", -1, -1],
["gener", -1, -1]
];
 var a_1 = [
["'", -1, 1],
["'s'", 0, 1],
["'s", -1, 1]
];
 var a_2 = [
["ied", -1, 2],
["s", -1, 3],
["ies", 1, 2],
["sses", 1, 1],
["ss", 1, -1],
["us", 1, -1]
];
 var a_3 = [
["", -1, 3],
["bb", 0, 2],
["dd", 0, 2],
["ff", 0, 2],
["gg", 0, 2],
["bl", 0, 1],
["mm", 0, 2],
["nn", 0, 2],
["pp", 0, 2],
["rr", 0, 2],
["at", 0, 1],
["tt", 0, 2],
["iz", 0, 1]
];
 var a_4 = [
["ed", -1, 2],
["eed", 0, 1],
["ing", -1, 2],
["edly", -1, 2],
["eedly", 3, 1],
["ingly", -1, 2]
];
 var a_5 = [
["anci", -1, 3],
["enci", -1, 2],
["ogi", -1, 13],
["li", -1, 15],
["bli", 3, 12],
["abli", 4, 4],
["alli", 3, 8],
["fulli", 3, 9],
["lessli", 3, 14],
["ousli", 3, 10],
["entli", 3, 5],
["aliti", -1, 8],
["biliti", -1, 12],
["iviti", -1, 11],
["tional", -1, 1],
["ational", 14, 7],
["alism", -1, 8],
["ation", -1, 7],
["ization", 17, 6],
["izer", -1, 6],
["ator", -1, 7],
["iveness", -1, 11],
["fulness", -1, 9],
["ousness", -1, 10]
];
 var a_6 = [
["icate", -1, 4],
["ative", -1, 6],
["alize", -1, 3],
["iciti", -1, 4],
["ical", -1, 4],
["tional", -1, 1],
["ational", 5, 2],
["ful", -1, 5],
["ness", -1, 5]
];
 var a_7 = [
["ic", -1, 1],
["ance", -1, 1],
["ence", -1, 1],
["able", -1, 1],
["ible", -1, 1],
["ate", -1, 1],
["ive", -1, 1],
["ize", -1, 1],
["iti", -1, 1],
["al", -1, 1],
["ism", -1, 1],
["ion", -1, 2],
["er", -1, 1],
["ous", -1, 1],
["ant", -1, 1],
["ent", -1, 1],
["ment", 15, 1],
["ement", 16, 1]
];
 var a_8 = [
["e", -1, 1],
["l", -1, 2]
];
 var a_9 = [
["succeed", -1, -1],
["proceed", -1, -1],
["exceed", -1, -1],
["canning", -1, -1],
["inning", -1, -1],
["earring", -1, -1],
["herring", -1, -1],
["outing", -1, -1]
];
 var a_10 = [
["andes", -1, -1],
["atlas", -1, -1],
["bias", -1, -1],
["cosmos", -1, -1],
["dying", -1, 3],
["early", -1, 9],
["gently", -1, 7],
["howe", -1, -1],
["idly", -1, 6],
["lying", -1, 4],
["news", -1, -1],
["only", -1, 10],
["singly", -1, 11],
["skies", -1, 2],
["skis", -1, 1],
["sky", -1, -1],
["tying", -1, 5],
["ugly", -1, 8]
];
 var  g_v = [17, 65, 16, 1];
 var  g_v_WXY = [1, 17, 65, 208, 1];
 var  g_valid_LI = [55, 141, 2];
var  B_Y_found = false;
var  I_p2 = 0;
var  I_p1 = 0;
function r_prelude() {
B_Y_found = false;
var  v_1 = base.cursor;
lab0: {
base.bra = base.cursor;
if (!(base.eq_s("'")))
{
break lab0;
}
base.ket = base.cursor;
if (!base.slice_del())
{
return false;
}
}
base.cursor = v_1;
var  v_2 = base.cursor;
lab1: {
base.bra = base.cursor;
if (!(base.eq_s("y")))
{
break lab1;
}
base.ket = base.cursor;
if (!base.slice_from("Y"))
{
return false;
}
B_Y_found = true;
}
base.cursor = v_2;
var  v_3 = base.cursor;
lab2: {
while(true)
{
var  v_4 = base.cursor;
lab3: {
golab4: while(true)
{
var  v_5 = base.cursor;
lab5: {
if (!(base.in_grouping(g_v, 97, 121)))
{
break lab5;
}
base.bra = base.cursor;
if (!(base.eq_s("y")))
{
break lab5;
}
base.ket = base.cursor;
base.cursor = v_5;
break golab4;
}
base.cursor = v_5;
if (base.cursor >= base.limit)
{
break lab3;
}
base.cursor++;
}
if (!base.slice_from("Y"))
{
return false;
}
B_Y_found = true;
continue;
}
base.cursor = v_4;
break;
}
}
base.cursor = v_3;
return true;
};
function r_mark_regions() {
I_p1 = base.limit;
I_p2 = base.limit;
var  v_1 = base.cursor;
lab0: {
lab1: {
var  v_2 = base.cursor;
lab2: {
if (base.find_among(a_0) == 0)
{
break lab2;
}
break lab1;
}
base.cursor = v_2;
golab3: while(true)
{
lab4: {
if (!(base.in_grouping(g_v, 97, 121)))
{
break lab4;
}
break golab3;
}
if (base.cursor >= base.limit)
{
break lab0;
}
base.cursor++;
}
golab5: while(true)
{
lab6: {
if (!(base.out_grouping(g_v, 97, 121)))
{
break lab6;
}
break golab5;
}
if (base.cursor >= base.limit)
{
break lab0;
}
base.cursor++;
}
}
I_p1 = base.cursor;
golab7: while(true)
{
lab8: {
if (!(base.in_grouping(g_v, 97, 121)))
{
break lab8;
}
break golab7;
}
if (base.cursor >= base.limit)
{
break lab0;
}
base.cursor++;
}
golab9: while(true)
{
lab10: {
if (!(base.out_grouping(g_v, 97, 121)))
{
break lab10;
}
break golab9;
}
if (base.cursor >= base.limit)
{
break lab0;
}
base.cursor++;
}
I_p2 = base.cursor;
}
base.cursor = v_1;
return true;
};
function r_shortv() {
lab0: {
var  v_1 = base.limit - base.cursor;
lab1: {
if (!(base.out_grouping_b(g_v_WXY, 89, 121)))
{
break lab1;
}
if (!(base.in_grouping_b(g_v, 97, 121)))
{
break lab1;
}
if (!(base.out_grouping_b(g_v, 97, 121)))
{
break lab1;
}
break lab0;
}
base.cursor = base.limit - v_1;
if (!(base.out_grouping_b(g_v, 97, 121)))
{
return false;
}
if (!(base.in_grouping_b(g_v, 97, 121)))
{
return false;
}
if (base.cursor > base.limit_backward)
{
return false;
}
}
return true;
};
function r_R1() {
if (!(I_p1 <= base.cursor))
{
return false;
}
return true;
};
function r_R2() {
if (!(I_p2 <= base.cursor))
{
return false;
}
return true;
};
function r_Step_1a() {
var  among_var;
var  v_1 = base.limit - base.cursor;
lab0: {
base.ket = base.cursor;
if (base.find_among_b(a_1) == 0)
{
base.cursor = base.limit - v_1;
break lab0;
}
base.bra = base.cursor;
if (!base.slice_del())
{
return false;
}
}
base.ket = base.cursor;
among_var = base.find_among_b(a_2);
if (among_var == 0)
{
return false;
}
base.bra = base.cursor;
switch (among_var) {
case 1:
if (!base.slice_from("ss"))
{
return false;
}
break;
case 2:
lab1: {
var  v_2 = base.limit - base.cursor;
lab2: {
{
var  c1 = base.cursor - 2;
if (base.limit_backward > c1 || c1 > base.limit)
{
break lab2;
}
base.cursor = c1;
}
if (!base.slice_from("i"))
{
return false;
}
break lab1;
}
base.cursor = base.limit - v_2;
if (!base.slice_from("ie"))
{
return false;
}
}
break;
case 3:
if (base.cursor <= base.limit_backward)
{
return false;
}
base.cursor--;
golab3: while(true)
{
lab4: {
if (!(base.in_grouping_b(g_v, 97, 121)))
{
break lab4;
}
break golab3;
}
if (base.cursor <= base.limit_backward)
{
return false;
}
base.cursor--;
}
if (!base.slice_del())
{
return false;
}
break;
}
return true;
};
function r_Step_1b() {
var  among_var;
base.ket = base.cursor;
among_var = base.find_among_b(a_4);
if (among_var == 0)
{
return false;
}
base.bra = base.cursor;
switch (among_var) {
case 1:
if (!r_R1())
{
return false;
}
if (!base.slice_from("ee"))
{
return false;
}
break;
case 2:
var  v_1 = base.limit - base.cursor;
golab0: while(true)
{
lab1: {
if (!(base.in_grouping_b(g_v, 97, 121)))
{
break lab1;
}
break golab0;
}
if (base.cursor <= base.limit_backward)
{
return false;
}
base.cursor--;
}
base.cursor = base.limit - v_1;
if (!base.slice_del())
{
return false;
}
var  v_3 = base.limit - base.cursor;
among_var = base.find_among_b(a_3);
if (among_var == 0)
{
return false;
}
base.cursor = base.limit - v_3;
switch (among_var) {
case 1:
{
var  c1 = base.cursor;
base.insert(base.cursor, base.cursor, "e");
base.cursor = c1;
}
break;
case 2:
base.ket = base.cursor;
if (base.cursor <= base.limit_backward)
{
return false;
}
base.cursor--;
base.bra = base.cursor;
if (!base.slice_del())
{
return false;
}
break;
case 3:
if (base.cursor != I_p1)
{
return false;
}
var  v_4 = base.limit - base.cursor;
if (!r_shortv())
{
return false;
}
base.cursor = base.limit - v_4;
{
var  c2 = base.cursor;
base.insert(base.cursor, base.cursor, "e");
base.cursor = c2;
}
break;
}
break;
}
return true;
};
function r_Step_1c() {
base.ket = base.cursor;
lab0: {
var  v_1 = base.limit - base.cursor;
lab1: {
if (!(base.eq_s_b("y")))
{
break lab1;
}
break lab0;
}
base.cursor = base.limit - v_1;
if (!(base.eq_s_b("Y")))
{
return false;
}
}
base.bra = base.cursor;
if (!(base.out_grouping_b(g_v, 97, 121)))
{
return false;
}
lab2: {
if (base.cursor > base.limit_backward)
{
break lab2;
}
return false;
}
if (!base.slice_from("i"))
{
return false;
}
return true;
};
function r_Step_2() {
var  among_var;
base.ket = base.cursor;
among_var = base.find_among_b(a_5);
if (among_var == 0)
{
return false;
}
base.bra = base.cursor;
if (!r_R1())
{
return false;
}
switch (among_var) {
case 1:
if (!base.slice_from("tion"))
{
return false;
}
break;
case 2:
if (!base.slice_from("ence"))
{
return false;
}
break;
case 3:
if (!base.slice_from("ance"))
{
return false;
}
break;
case 4:
if (!base.slice_from("able"))
{
return false;
}
break;
case 5:
if (!base.slice_from("ent"))
{
return false;
}
break;
case 6:
if (!base.slice_from("ize"))
{
return false;
}
break;
case 7:
if (!base.slice_from("ate"))
{
return false;
}
break;
case 8:
if (!base.slice_from("al"))
{
return false;
}
break;
case 9:
if (!base.slice_from("ful"))
{
return false;
}
break;
case 10:
if (!base.slice_from("ous"))
{
return false;
}
break;
case 11:
if (!base.slice_from("ive"))
{
return false;
}
break;
case 12:
if (!base.slice_from("ble"))
{
return false;
}
break;
case 13:
if (!(base.eq_s_b("l")))
{
return false;
}
if (!base.slice_from("og"))
{
return false;
}
break;
case 14:
if (!base.slice_from("less"))
{
return false;
}
break;
case 15:
if (!(base.in_grouping_b(g_valid_LI, 99, 116)))
{
return false;
}
if (!base.slice_del())
{
return false;
}
break;
}
return true;
};
function r_Step_3() {
var  among_var;
base.ket = base.cursor;
among_var = base.find_among_b(a_6);
if (among_var == 0)
{
return false;
}
base.bra = base.cursor;
if (!r_R1())
{
return false;
}
switch (among_var) {
case 1:
if (!base.slice_from("tion"))
{
return false;
}
break;
case 2:
if (!base.slice_from("ate"))
{
return false;
}
break;
case 3:
if (!base.slice_from("al"))
{
return false;
}
break;
case 4:
if (!base.slice_from("ic"))
{
return false;
}
break;
case 5:
if (!base.slice_del())
{
return false;
}
break;
case 6:
if (!r_R2())
{
return false;
}
if (!base.slice_del())
{
return false;
}
break;
}
return true;
};
function r_Step_4() {
var  among_var;
base.ket = base.cursor;
among_var = base.find_among_b(a_7);
if (among_var == 0)
{
return false;
}
base.bra = base.cursor;
if (!r_R2())
{
return false;
}
switch (among_var) {
case 1:
if (!base.slice_del())
{
return false;
}
break;
case 2:
lab0: {
var  v_1 = base.limit - base.cursor;
lab1: {
if (!(base.eq_s_b("s")))
{
break lab1;
}
break lab0;
}
base.cursor = base.limit - v_1;
if (!(base.eq_s_b("t")))
{
return false;
}
}
if (!base.slice_del())
{
return false;
}
break;
}
return true;
};
function r_Step_5() {
var  among_var;
base.ket = base.cursor;
among_var = base.find_among_b(a_8);
if (among_var == 0)
{
return false;
}
base.bra = base.cursor;
switch (among_var) {
case 1:
lab0: {
var  v_1 = base.limit - base.cursor;
lab1: {
if (!r_R2())
{
break lab1;
}
break lab0;
}
base.cursor = base.limit - v_1;
if (!r_R1())
{
return false;
}
{
var  v_2 = base.limit - base.cursor;
lab2: {
if (!r_shortv())
{
break lab2;
}
return false;
}
base.cursor = base.limit - v_2;
}
}
if (!base.slice_del())
{
return false;
}
break;
case 2:
if (!r_R2())
{
return false;
}
if (!(base.eq_s_b("l")))
{
return false;
}
if (!base.slice_del())
{
return false;
}
break;
}
return true;
};
function r_exception2() {
base.ket = base.cursor;
if (base.find_among_b(a_9) == 0)
{
return false;
}
base.bra = base.cursor;
if (base.cursor > base.limit_backward)
{
return false;
}
return true;
};
function r_exception1() {
var  among_var;
base.bra = base.cursor;
among_var = base.find_among(a_10);
if (among_var == 0)
{
return false;
}
base.ket = base.cursor;
if (base.cursor < base.limit)
{
return false;
}
switch (among_var) {
case 1:
if (!base.slice_from("ski"))
{
return false;
}
break;
case 2:
if (!base.slice_from("sky"))
{
return false;
}
break;
case 3:
if (!base.slice_from("die"))
{
return false;
}
break;
case 4:
if (!base.slice_from("lie"))
{
return false;
}
break;
case 5:
if (!base.slice_from("tie"))
{
return false;
}
break;
case 6:
if (!base.slice_from("idl"))
{
return false;
}
break;
case 7:
if (!base.slice_from("gentl"))
{
return false;
}
break;
case 8:
if (!base.slice_from("ugli"))
{
return false;
}
break;
case 9:
if (!base.slice_from("earli"))
{
return false;
}
break;
case 10:
if (!base.slice_from("onli"))
{
return false;
}
break;
case 11:
if (!base.slice_from("singl"))
{
return false;
}
break;
}
return true;
};
function r_postlude() {
if (!B_Y_found)
{
return false;
}
while(true)
{
var  v_1 = base.cursor;
lab0: {
golab1: while(true)
{
var  v_2 = base.cursor;
lab2: {
base.bra = base.cursor;
if (!(base.eq_s("Y")))
{
break lab2;
}
base.ket = base.cursor;
base.cursor = v_2;
break golab1;
}
base.cursor = v_2;
if (base.cursor >= base.limit)
{
break lab0;
}
base.cursor++;
}
if (!base.slice_from("y"))
{
return false;
}
continue;
}
base.cursor = v_1;
break;
}
return true;
};
this.stem =  function() {
lab0: {
var  v_1 = base.cursor;
lab1: {
if (!r_exception1())
{
break lab1;
}
break lab0;
}
base.cursor = v_1;
lab2: {
{
var  v_2 = base.cursor;
lab3: {
{
var  c1 = base.cursor + 3;
if (0 > c1 || c1 > base.limit)
{
break lab3;
}
base.cursor = c1;
}
break lab2;
}
base.cursor = v_2;
}
break lab0;
}
base.cursor = v_1;
r_prelude();
r_mark_regions();
base.limit_backward = base.cursor; base.cursor = base.limit;
var  v_5 = base.limit - base.cursor;
r_Step_1a();
base.cursor = base.limit - v_5;
lab4: {
var  v_6 = base.limit - base.cursor;
lab5: {
if (!r_exception2())
{
break lab5;
}
break lab4;
}
base.cursor = base.limit - v_6;
var  v_7 = base.limit - base.cursor;
r_Step_1b();
base.cursor = base.limit - v_7;
var  v_8 = base.limit - base.cursor;
r_Step_1c();
base.cursor = base.limit - v_8;
var  v_9 = base.limit - base.cursor;
r_Step_2();
base.cursor = base.limit - v_9;
var  v_10 = base.limit - base.cursor;
r_Step_3();
base.cursor = base.limit - v_10;
var  v_11 = base.limit - base.cursor;
r_Step_4();
base.cursor = base.limit - v_11;
var  v_12 = base.limit - base.cursor;
r_Step_5();
base.cursor = base.limit - v_12;
}
base.cursor = base.limit_backward;
var  v_13 = base.cursor;
r_postlude();
base.cursor = v_13;
}
return true;
};
this['stemWord'] = function(word) {
base.setCurrent(word);
this.stem();
return base.getCurrent();
};
};
return new EnglishStemmer();
}
wh.search_stemmer = Snowball();
wh.search_baseNameList = [
 "appendix.dictionaries.html",
 "appendix.glossaries.html",
 "appendix.regexp.html",
 "appendix.shortcut.custom.html",
 "appendix.spellchecker.html",
 "chapter.installing.and.running.html",
 "howtos.html",
 "index.html",
 "menus.html",
 "panes.html",
 "project.folder.html",
 "windows.and.dialogs.html"
];
wh.search_titleList = [
 "Appendix A. Слоўнікі",
 "Appendix B. Глясары",
 "Appendix D. Рэгулярныя выразы",
 "Appendix E. Наладка спалучэньняў клявішаў",
 "Appendix C. Модуль праверкі правапісу",
 "Усталяваньне і запуск OmegaT",
 "Дапаможнікі…",
 "OmegaT 4.2 — Дапаможнік карыстальніка",
 "Мэню",
 "Панэлі",
 "Каталёг праекта",
 "Вокны і дыялёгавыя вокны"
];
wh.search_wordMap= {
"аддаленага": [6,[5,8]],
"адкрыць": [[9,11]],
"напрыклад": [11,6,5,4,[1,10],9,[2,8],0,3],
"першым": [11,6,5],
"шмат": [11],
"будучым": [6],
"простая": [2],
"частых": [5],
"шлях": [5],
"кантэкстным": [11],
"вызначальнікі": [[2,7]],
"першых": [[1,8]],
"разьдзел": [[2,5,11]],
"простае": [1],
"канкрэтную": [11],
"чырвоным": [11],
"наперад": [8,3],
"info.plist": [5],
"міжнароднага": [1],
"усталяванай": [5],
"атрыманых": [9],
"бяжыць": [11],
"клявішаў": [3,8,[7,9],[1,2,11]],
"праграмаваньня": [11],
"fuzzi": [11],
"пусты": [11,[8,10],[1,3,6]],
"карыстальніцкага": [11],
"францускім": [5],
"ключ-значэньне": [11],
"дынамічная": [11],
"жаданьні": [6],
"віртуальнай": [11],
"усталяваная": [5],
"атрыманыя": [[6,11]],
"першасны": [5],
"куце": [9],
"перагледзець": [10],
"прадухіленьне": [6,7],
"адчыніўшы": [9],
"націскаючы": [[6,11]],
"недакладнага": [11,8],
"dgoogle.api.key": [5],
"баку": [6],
"edittagnextmissedmenuitem": [3],
"тэрміналёгіі": [[1,9]],
"фактычна": [[6,8,11]],
"змяняецца": [8],
"выяўленая": [6],
"тэрміналёгія": [11],
"quiet": [5],
"тэрміналёгію": [[6,11]],
"паўтараюцца": [11],
"моўны": [5],
"інакш": [6],
"першы": [11,[1,5,8]],
"es_es.d": [4],
"дыску": [6],
"ектаў": [11],
"the": [5,[0,2]],
"пісаць": [11],
"ангельскую": [6],
"projectimportmenuitem": [3],
"шрыфту": [11],
"imag": [5],
"шрыфты": [8],
"дыска": [5],
"замяняць": [11],
"якому-небудзь": [11],
"рэгвыры": [2],
"назвавыніковагафайла": [5],
"прасьцей": [4],
"адкрыты": [1],
"url-спасылка": [6],
"тычыцца": [5,6],
"разбору": [11],
"экспартаваных": [6],
"загружаецца": [[1,5]],
"шуканы": [[2,11]],
"moodlephp": [5],
"туды": [[6,11]],
"старонкі": [8,3],
"currsegment.getsrctext": [11],
"выключэньняў": [11],
"файламі": [6,[5,10],11,8],
"іхні": [11],
"лічыльнікі": [9,7],
"перакладаныя": [11],
"часу": [[4,9]],
"transtip": [[3,9],1],
"check": [6],
"неперакладальнымі": [11],
"зноскі": [11],
"xxxx9xxxx9xxxxxxxx9xxx99xxxxx9xx9xxxxxxxxxx": [5],
"стылі": [6,11],
"загружаць": [11,6],
"fr-fr": [4],
"перакладаным": [11],
"перадаць": [5],
"клікайце": [11],
"базе": [5],
"інструмэнт": [[5,8]],
"мінулым": [6],
"ключавыя": [11],
"адной": [5],
"primari": [5],
"падказка": [11],
"сустракаюцца": [[8,11]],
"ўпарадкаваныя": [[1,10]],
"шматпарадыгмавая": [11],
"webster": [0,9],
"часткай": [11],
"пазьнейшым": [10],
"зьявілася": [10],
"мэнэджары": [4],
"частках": [9],
"слоем": [6],
"паскорыць": [6],
"галоўнае": [9,[3,7,11]],
"старонку": [[3,8,11]],
"галоўная": [6],
"галоўнага": [9,11,3],
"старонка": [11],
"копіі": [6,10],
"даступныя": [[3,5,8,11]],
"ігнараваньні": [9,8],
"ключавых": [11],
"копія": [[8,11]],
"копію": [6,10],
"empti": [[5,11]],
"сярод": [10],
"адпаведнікаў": [11,2],
"даступных": [[4,5,11],9],
"наступнага": [[8,11],[2,6,9]],
"потым": [[6,11]],
"даступным": [[4,6]],
"толькі": [11,6,8,5,1,4,[0,9,10]],
"адпаведна": [[1,4,11]],
"супадзеньнямі": [9],
"вядомымі": [9],
"сэрвісу": [8,11],
"tmx": [6,10,5,[8,11],[3,9]],
"сынхранізуецца": [6],
"разархіваваць": [0],
"адпаведнікам": [2],
"пачатковага": [5],
"сэрвісы": [11],
"nl-en": [6],
"склад": [11],
"integ": [11],
"скапіюе": [11],
"ўваходу": [11],
"мага": [2],
"intel": [5,7],
"fr-ca": [11],
"тэкставымі": [11],
"атрымацца": [0],
"mainmenushortcuts.properti": [3],
"адным": [11,[5,6],[8,10]],
"выхад": [[3,8],11],
"канвэеры": [11],
"неразрыўны": [11],
"ведаю": [5],
"cmd": [[6,11]],
"coach": [2],
"лепш": [1],
"спыняецца": [11],
"gotohistorybackmenuitem": [3],
"save": [5],
"пачаць": [11],
"слове": [[2,11],[4,9]],
"слова": [11,8,[1,5],[2,4]],
"пошуку": [11,8,6],
"пакетны": [5],
"ручная": [8],
"словы": [11,9,[1,8],10],
"адзіночныя": [11],
"powerpc": [5],
"адчыненае": [8],
"запусьціцца": [5],
"пачаткам": [[2,6]],
"зьмяняе": [6,5],
"застануцца": [11],
"частцы": [9,11,8],
"калегаў": [9],
"дробнай": [11],
"ўверсе": [11],
"безумоўна": [10],
"скаротаў": [11],
"адпаведнікамі": [11],
"дзесьці": [5],
"моўнымі": [6],
"пазначэньнем": [10],
"сумневу": [10],
"фільтр": [11],
"плятформы": [11],
"выкананьне": [[5,11]],
"множнымі": [11],
"фільтраў": [11,6,[8,10]],
"выхадзе": [6,[8,10,11]],
"інструмэнтаў": [[2,6]],
"павольна": [5],
"падбор": [11,8],
"правілах": [[2,11]],
"omegat.sourceforge.io": [5],
"панэль": [9,7,1],
"панэлі": [9,11,1,8,5,[7,10],6],
"катэгорыя": [2,7],
"дадаць": [[6,11],5,10,[1,3,4,8,9]],
"экспартаваную": [6],
"фільтрам": [5],
"японская": [11],
"ейнаму": [9],
"translat": [11,5,[4,6]],
"вынікам": [11],
"прапановы": [8,11,[3,9]],
"альфабэтным": [11],
"яднаць": [[6,11]],
"правілаў": [11,6,2],
"зьвязу": [6],
"прагартаць": [11],
"існуючую": [6],
"шукаць": [11],
"будуць": [11,5,8,10,6,9,[1,4],3],
"сэгмэнту": [[9,11]],
"набліжэйшыя": [11],
"сэгмэнты": [11,8,9,3,10,5],
"кіруюць": [6],
"пачаты": [[6,11]],
"зьвесткамі": [5],
"вывадам": [8],
"docs_devel": [5],
"вынікаў": [11,8],
"супадаюць": [[1,9]],
"першаму": [11],
"захаваны": [[5,8],[6,10,11]],
"увага": [11],
"так": [11,5,[6,9,10]],
"там": [[5,10,11]],
"gnome": [5],
"перакладаецца": [10],
"астатнія": [[6,11]],
"збор": [[1,2,9]],
"згубіць": [1],
"адрапараваць": [6],
"канкрэтных": [9],
"выкананьня": [5],
"бал": [11],
"прыняцьця": [5],
"тая": [[5,6]],
"клявішай": [[3,11]],
"плятформе": [5],
"сэгмэнта": [11,[8,9],10,[1,3]],
"сэгмэнтуюцца": [11],
"глясарамі": [1,7],
"наладзьце": [11],
"канфідэнцыйнасьці": [11],
"prev": [[0,1,2,3,4,5,6,8,9,10,11]],
"csv": [[1,5]],
"n.n_linux.tar.bz2": [5],
"узяты": [3],
"падлягае": [11],
"вызначэньні": [6,[1,11]],
"press": [3],
"dock": [5],
"падразьдзеле": [5],
"вызначэньня": [[3,6]],
"створыць": [11],
"кампаноўку": [9],
"dmicrosoft.api.client_secret": [5],
"полем": [4],
"падказак": [11],
"ектна-арыентаваны": [11],
"шляхам": [5,6],
"нічога": [[2,8],3],
"актыўнымі": [11],
"ctrl": [3,11,9,6,8,1,[0,10]],
"сэгмэньце": [[9,11],1,8,5],
"document": [11,[2,5]],
"кампаноўкі": [11],
"ўвайсьці": [5],
"будзьце": [6],
"выкарыстоўвае": [[6,11]],
"злучэньне": [11,3],
"японскай": [11,5],
"адчыніў": [6],
"важна": [6],
"resourc": [5],
"старонцы": [[5,11],8],
"уводзіцца": [5],
"эталёна": [9],
"team": [6],
"вырабаў": [6],
"xx_yy": [[6,11]],
"docx": [[6,11],8],
"празь": [8],
"клікам": [[9,11]],
"важны": [10],
"txt": [6,1,[9,11]],
"унутранай": [9],
"асобных": [11],
"злучэньні": [6],
"асобныя": [11],
"яшчэ": [[9,11],[2,8]],
"мэтавае": [11],
"вызначэньне": [6,11],
"злучэньня": [5],
"знайсьці": [11,[6,8],[3,5]],
"без": [11,5,9],
"прымыкаюць": [9],
"бел": [11],
"сынтаксычны": [11],
"здарыцца": [10],
"падрабязнасьці": [6,8],
"каталёгі": [11],
"надзейныя": [10],
"кроку": [10],
"пісьмовасьць": [6],
"trnsl": [5],
"viewdisplaymodificationinfoselectedradiobuttonmenuitem": [3],
"пісьмовасьцю": [6,7],
"створаных": [[6,11]],
"дакладны": [11],
"index.html": [5],
"omegat.tmx": [6],
"каталёгу": [5,11,10,8,1,6,4,[0,3]],
"даступнага": [5],
"сумяшчальны": [5],
"створаным": [11],
"нязьменны": [11],
"пісьмовасьці": [6],
"унутраная": [8],
"параўнаньня": [11],
"адсутны": [[3,8]],
"створаныя": [8,6],
"зьмяняючы": [6],
"слоўніку": [11],
"параўнаньні": [1],
"рэгулярна": [6],
"прагляду": [[6,9]],
"diffrevers": [11],
"дакладна": [11,[4,6]],
"сэрвэрамі": [6],
"разгляду": [[10,11]],
"ліцэнзіяй": [0],
"надзейных": [11],
"рэгулярны": [2,7,11],
"удасканаленьняў": [8],
"канкрэтнай": [11],
"палямі": [1],
"слоўніка": [4,[0,8]],
"лякалізацыйныя": [5],
"каталёгамі": [[6,11]],
"кропка": [2,11],
"пазначацца": [11],
"ўжываць": [5,11,[1,2,3,6]],
"стварэце": [4],
"крытэрыям": [11],
"project.gettranslationinfo": [11],
"слоўнікі": [0,4,[6,7,9,10],[1,11]],
"спыніцца": [5],
"канада": [5],
"зыходны": [11,1,[6,8],9,3,[5,10]],
"захаваць": [[6,11],[3,5,8]],
"зьмесьціва": [[3,11],6,8,[1,10],[0,5]],
"пільныя": [6],
"каталёга": [5,6,3,[1,8,10,11],4],
"start": [5,7],
"сьпіс": [11,[2,4]],
"структураванымі": [1],
"адпаведны": [11,8,[1,2,4,9,10]],
"equal": [5],
"прапушчаны": [2],
"закамэнтаваныя": [11],
"гледзячы": [11],
"ствараюцца": [10,[1,8]],
"optionsalwaysconfirmquitcheckboxmenuitem": [3],
"перазагрузіць": [[3,8]],
"тла": [[9,10]],
"цэтлікаў": [5],
"дзесятковыя": [11],
"захаваньнем": [11],
"дадатковыя": [11,5,10,[2,9]],
"чытаньне": [6],
"некалькіх": [11,5,[1,7,8,9]],
"пустая": [6],
"enter": [11,[3,8],5],
"кропкі": [11,2],
"адпаведнасьці": [6,11,5,[1,4]],
"applic": [5],
"правільна": [6,1,10],
"projectteamnewmenuitem": [3],
"імпартуюцца": [6],
"апошняга": [8,6,10],
"кропку": [[5,11]],
"пераймяноўвае": [11],
"memori": [5],
"дазваляе": [11,8,5,6,[1,9]],
"тло": [[8,10]],
"бачыце": [9],
"сьпісам": [4],
"перакладаць": [11,6],
"права": [[6,8]],
"пакет": [5,8],
"зьяўляцца": [11,[9,10]],
"галоўным": [[6,9]],
"паўплываць": [11],
"малыя": [[3,8,11]],
"магчымасьці": [[5,11],6],
"загрузіць": [[6,11]],
"tmx-файлах": [11],
"дададуцца": [10],
"ужывае": [11,5],
"бачыць": [6,[8,11]],
"стварыў": [6],
"перакладам": [[6,8],[9,11]],
"магчымасьць": [11,5,[1,4,6]],
"omegat.jnlp": [5],
"падзяляць": [11],
"зробленых": [11],
"значыць": [6,11],
"маглі": [6],
"сцэнара": [[5,11],8],
"тмх": [10,6],
"задаваць": [[5,11]],
"зробленыя": [6,[5,8]],
"n.n_windows_without_jre.ex": [5],
"будучай": [6],
"каляровым": [8],
"перакладах": [11,[2,7]],
"дадатковых": [6,[0,11]],
"дзейснымі": [[3,11]],
"тое": [11,9,[0,2,5,6,8,10]],
"той": [[5,6,9,10,11]],
"склон": [1],
"слабой": [11],
"канфігурацыя": [11],
"сцэнары": [11,[5,7,8]],
"прабел": [11,2],
"dmicrosoft.api.client_id": [5],
"разрыва": [11],
"перакладаў": [6,11,10,9,8,5,[2,3,7]],
"сумарнымі": [8],
"праглядаць": [11],
"канфігурацыі": [11,3,[5,8]],
"config-fil": [5],
"выразу": [11],
"вылучае": [8],
"выразы": [2,11,7,[3,4]],
"прапанаваны": [9],
"захаваюцца": [5],
"ангельскай": [2],
"аднолькавай": [0],
"страты": [6,7],
"працоўнай": [[6,10,11]],
"аўтаматычнага": [[5,6,8]],
"паказ": [11,8,10],
"ўключэньня": [8],
"выяўленыя": [5],
"застанецца": [11],
"system-user-nam": [11],
"каб": [11,6,9,5,8,10,4,3,1,[0,2]],
"console.println": [11],
"арыгінал": [[8,9]],
"працоўная": [10],
"аддалены": [6],
"працуюць": [11,[2,6,8]],
"датычная": [5],
"аўтатэксту": [11],
"недаступны": [5],
"launcher": [5],
"тыпаў": [11],
"разьдзяляць": [11],
"запоўненыя": [[3,8,11]],
"ўсталяванага": [5],
"існуючае": [1],
"падстаноўкі": [11,6],
"спраўджваць": [6],
"паспрабаваць": [[6,11]],
"апрацоўваць": [11],
"завецца": [5],
"файлаў-канфігурацыі": [5],
"трымаць": [11],
"тры": [[0,6],[1,9]],
"двузначны": [5],
"сьпісаў": [11],
"аўтатэкст": [[3,11]],
"сховішчы": [6],
"вэб-старонцы": [6],
"project_files_show_on_load": [11],
"прамінаць": [11],
"сынхранізуюцца": [6],
"азначаць": [11],
"белай": [11],
"ltr": [6],
"тыпу": [11,5,[0,4,6,10]],
"падстаноўка": [8],
"невялікую": [10],
"адпаведнымі": [[0,6,11]],
"optionsexttmxmenuitem": [3],
"плагінаў": [11],
"падстаноўку": [8],
"губляць": [6],
"build": [5],
"тыпы": [11,8],
"рахавацца": [11],
"marketplac": [5],
"запоўненых": [[8,11]],
"націсканьні": [[8,11]],
"выпадное": [8],
"entries.s": [11],
"інструкцыямі": [5],
"націсканьня": [11],
"добры": [2],
"del": [[9,11]],
"gotonextuntranslatedmenuitem": [3],
"адчыненых": [11],
"targetlocal": [11],
"path": [5],
"ўжытае": [[6,11]],
"альтэрнатыўнага": [11],
"словаў": [11,8,1,[2,7,9]],
"сумніўныя": [11],
"адчыненым": [11],
"дадатковую": [1],
"побач": [11,[3,4]],
"лепей": [4],
"атрымаць": [[5,11],[3,4,6,9]],
"адасобліваць": [6],
"якасьць": [6],
"працэдура": [6],
"жаданым": [5],
"ставіцца": [[5,11]],
"allsegments.tmx": [5],
"дынамічны": [11],
"правільнасьці": [5],
"уключаць": [[9,11]],
"добра": [11,6],
"пазначаныя": [[5,10]],
"тут": [11,[9,10],[5,6,8]],
"рэалізаванае": [11],
"словам": [11],
"месны": [1],
"дзеля": [11],
"сыстэмамі": [6],
"helpcontentsmenuitem": [3],
"узьнікае": [6],
"шэрым": [8],
"прагляд": [11],
"паставіць": [11],
"omegat-org": [6],
"прымусовага": [10],
"пазначаных": [11],
"descript": [[3,5]],
"правільнасьць": [11],
"вядома": [10,[1,4,5,6,9]],
"projectaccessdictionarymenuitem": [3],
"блякаваньне": [9,5],
"шрыфт": [11,3],
"працэдуры": [[5,6]],
"структуру": [10],
"краіна": [5],
"тлом": [8],
"супадзеньне": [3,9,8,11],
"патрабаваньні": [5],
"прасэгмэнтаваўшы": [11],
"апрацаваныя": [8],
"супадзеньню": [8],
"клясы": [[2,7]],
"даступнае": [5],
"структура": [[10,11]],
"клік": [[5,11]],
"term": [1],
"запісана": [10],
"ўбачыць": [6],
"нумар": [[5,8],[3,9,11]],
"ніжнім": [9],
"кнопка": [11],
"супадзеньні": [11,6,[8,9],[1,7,10]],
"duden": [9],
"супадзеньня": [11,[8,10],[3,9]],
"ейным": [9],
"аркушоў": [11],
"дзень": [6],
"імпэратыўны": [11],
"апэрацыяў": [11],
"уласьцівасьцяў": [5,[6,8]],
"ip-адрас": [5],
"структуры": [10],
"spotlight": [5],
"ніжняй": [11,9,8],
"пэўнага": [11,[4,6,10]],
"іншым": [5,6],
"верхнім": [2],
"выяўляюцца": [8],
"практыцы": [5],
"іншых": [5,[6,11],9],
"dir": [5],
"падрабязна": [9],
"div": [11],
"данімі": [1],
"дыстрыбутыва": [5],
"каталёг-сховішча": [6],
"viewfilelistmenuitem": [3],
"ідэнтычнымі": [6],
"кнопку": [11,4,5],
"некарэктныя": [6],
"brows": [5],
"статыстычныя": [10,6],
"слушнымі": [9],
"больш": [11,5,[2,3,6,9]],
"запісаны": [5],
"test": [5],
"перайменаваньня": [6],
"краіну": [5],
"пароль": [11,6],
"пароля": [11],
"кадаваньня": [11],
"кароткі": [4],
"фіялетавым": [8],
"omegat": [5,6,11,8,10,[3,7],4,1,[0,9],2],
"патрабаваць": [9],
"краіны": [11],
"ніжняя": [9],
"статыстычных": [6],
"кантэксьце": [6],
"вылічаецца": [11],
"іншыя": [6,5,10,[0,1,7,8,9,11]],
"кнопкі": [[5,11]],
"адправіць": [6],
"дакумэнтацыі": [[3,11]],
"стаць": [11],
"клікнуўшы": [5,11,9],
"console-align": [5],
"накіроўвацца": [11],
"першае": [[2,3,8,9]],
"утрымліваюцца": [11],
"ms-dos": [5],
"першай": [[8,11]],
"простых": [[1,11]],
"projectopenrecentmenuitem": [3],
"зьвярнуцца": [6],
"адпаведную": [6],
"якасьці": [11,[1,6],[5,8]],
"першая": [5],
"ўважаецца": [11],
"папярэдняя": [[3,5,8,9]],
"зразумець": [11],
"тэхнічнага": [11],
"трывіяльныя": [6],
"яднаньня": [11],
"nazvaprajekta-omegat.tmx": [6],
"ўводзіць": [11,6],
"скапіюйце": [8,5],
"und": [4],
"project_save.tmx.temporari": [6],
"запіс": [11,[1,3,8],5,9],
"зьдзяйсьняць": [11],
"папярэдняе": [3],
"удрукуйце": [5],
"імпарт": [6],
"праглядзець": [10],
"editoverwritemachinetranslationmenuitem": [3],
"дэталі": [[8,11]],
"перакладзеную": [8],
"ingreek": [2],
"запыт": [8],
"тым": [[5,6,11],[8,10]],
"коскай": [[1,6]],
"тып": [11,6],
"es_es.aff": [4],
"дыск": [8],
"неперакладзеным": [[8,11]],
"тых": [[5,6]],
"магчымым": [11,[5,6,9]],
"кшталту": [5],
"ўвагу": [[0,5]],
"pojavnem": [1],
"выніковага": [2],
"projectexitmenuitem": [3],
"неперакладзеных": [11,8,6],
"магчымых": [6,5],
"магчымыя": [[1,3,5,6,11]],
"хаваньня": [11],
"пунктамі": [3],
"націснутых": [3],
"ккя": [11],
"editregisteruntranslatedmenuitem": [3],
"init": [6],
"выраўнаваць": [[8,11]],
"адпаведных": [6,11],
"ўмове": [8],
"даўжыня": [5],
"інтэрнэта": [[4,6]],
"мінімум": [11],
"адпаведным": [[6,9,11]],
"кожны": [6,11,10],
"падзяліцца": [6],
"кляс": [2],
"даўжыні": [9],
"рэжым": [6,5,11],
"maco": [5,7],
"тыя": [11,[1,5,6]],
"дадзенай": [6],
"вікі": [0],
"doc": [6],
"дыялёгу": [[8,11],4],
"запрасіць": [6],
"пустымі": [5],
"асаблівы": [6],
"выбранага": [8,3],
"стандартнай": [4],
"ключ": [[5,11]],
"француская": [5],
"paramet": [5],
"найбольш": [9,10],
"кнр": [5],
"mac": [3,5,6],
"генэральным": [8],
"абраньня": [11],
"ідэнтычным": [[9,10]],
"тэг": [[8,11],3],
"пазначанага": [[5,11]],
"падразумяванай": [11],
"man": [5],
"пералік": [[5,6,8,11]],
"map": [6],
"бачнымі": [8,9],
"францускай": [11],
"код": [3,11,5,4],
"неперакладзеныя": [11,[8,10],[3,9]],
"may": [11],
"пасьлядоўнымі": [11],
"кіраўнік": [6],
"асабліва": [[5,11],[6,8]],
"url": [6,11,5],
"зачынены": [[6,8]],
"быў": [8,9,[6,11],[1,5]],
"uppercasemenuitem": [3],
"viewmarkuntranslatedsegmentscheckboxmenuitem": [3],
"набору": [11,8],
"напаўненьня": [6],
"разьмяшчэньне": [11,[1,6]],
"дадатковымі": [5],
"павінныя": [[4,6]],
"выбірае": [8],
"ідэнтычныя": [[6,9,11]],
"наборы": [11],
"перапісаць": [10],
"прыпынак": [11],
"гадзіна": [6],
"use": [5],
"запісаць": [3],
"апэрацыях": [9],
"omegat.jar": [5,[6,11]],
"omegat.app": [5],
"сустракаецца": [11],
"usr": [5],
"дублікаты": [11],
"вызначаецца": [[1,5,8]],
"мэнэджарам": [6],
"верхняй": [[9,11]],
"загалоўку": [11,[8,9]],
"праверыць": [6,[9,10,11]],
"апэрацыяй": [11],
"utf": [1,11],
"бяз": [11,5],
"інтэрнэце": [[4,11]],
"вам": [11,[4,5]],
"гісторыі": [8,3],
"сыстэмай": [5],
"servic": [5],
"тайвань": [5],
"юнікод": [1],
"пералікам": [5],
"запусьціць": [5],
"dsl": [0],
"простую": [11],
"сыстэмах": [10],
"перацягнеце": [5],
"верхняя": [9],
"ствараецца": [8],
"med": [8],
"прапановамі": [8],
"en.wikipedia.org": [9],
"вас": [5,[1,6]],
"dtd": [5],
"дазваляць": [11],
"інтэрактыўных": [2],
"камбінаваньнем": [11],
"выдзеленай": [5],
"сыстэмаў": [5,11],
"ваш": [[6,11]],
"сказаў": [11],
"інжынерам": [6],
"адпаведныя": [11,[4,10],[3,5,6,8]],
"стану": [9,[5,6,7,10,11]],
"адлюстроўвацца": [[1,11]],
"projectcompilemenuitem": [3],
"перакладзеным": [6],
"console-transl": [5],
"папярэдніх": [[9,11]],
"перакладзеных": [11,[6,9],8],
"адтуль": [5],
"gotonextuniquemenuitem": [3],
"паўторныя": [11],
"сярэдзіне": [2],
"аднойчы": [6],
"стане": [9],
"скланеньні": [1],
"самым": [6,11],
"даюцца": [5],
"абараняць": [10],
"патрэбы": [11],
"рэпазыторый_усіх_камандных_праектаў_omegat": [6],
"рэпазыторыем": [6],
"перакладзеныя": [11,[8,9,10],[3,5,6]],
"wordart": [11],
"запрапанавана": [5],
"optionsviewoptionsmenuitem": [3],
"аб": [11,[5,6],8],
"прасэгмэнтуе": [11],
"тэксты": [[5,6]],
"ад": [5,11,9,8,6,10,[2,3],4],
"commit": [6],
"targetlocalelcid": [11],
"тэксту": [11,[6,8],[1,3,7,10],[2,4]],
"вылучанага": [8,11],
"паменшыць": [6],
"project_stats_match.txt": [10],
"dvd": [6],
"прыярытэтнага": [1],
"метак": [11],
"самі": [11],
"xmx2048m": [5],
"тэкста": [[9,11]],
"кнопцы": [11,5],
"месяц": [[5,6]],
"meniju": [1],
"сказах": [11,[3,8]],
"самы": [5],
"xxxxxxxxxxxxxxxx.xxxxxxxxxxxxxxxx.xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx": [5],
"аднолькавую": [6],
"падгружаюцца": [[8,11]],
"бо": [5,11,9,[1,6]],
"асацыяваныя": [5],
"рэсурсы": [6,5],
"няпрагныя": [2,7],
"стварэньне": [6,11],
"krunner": [5],
"libreoffic": [6,4,11],
"ва": [11,[0,4,5,6,10]],
"заказчыка": [6],
"зададзеныя": [11],
"стандарт": [1],
"роўна": [11],
"пачняце": [6],
"прабелу": [11],
"карыстальнікаў": [6,[2,9]],
"мэта-сымбаляў": [2],
"акрамя": [[9,11]],
"сынтаксысу": [11],
"проста": [11,5,2,1,[4,9,10]],
"вы": [11,6,[5,9],[3,4],[0,10]],
"ужо": [11,[1,5,6],[3,9,10]],
"невялікія": [11],
"свабоднай": [7],
"зваротам": [1],
"гб": [5],
"перакладацца": [11,10],
"зьмясьціць": [10,6],
"увёўшы": [5],
"згенэраваныя": [10],
"апорных": [10,[6,9]],
"гл": [6,11,2,[5,8,9,10],4,1],
"просты": [2,6],
"канфігурацыйных-файлаў": [5],
"рэдактар": [11,5],
"карыстальнікам": [11,[3,9]],
"уключаючы": [[9,11]],
"перакладаеце": [[9,11]],
"прэфікса": [10],
"viewdisplaysegmentsourcecheckboxmenuitem": [3],
"разьбіваць": [11],
"editregisteremptymenuitem": [3],
"апорныя": [6],
"адпаведнай": [6,11,4],
"вельмі": [11,6],
"уключаюць": [11],
"аднак": [11,[5,6],4],
"уніз": [11],
"да": [11,5,8,6,9,3,10,2,[0,1],4],
"дапускаюцца": [[3,11]],
"open": [11],
"гіпэрспасылкі": [11],
"адпаведнае": [6],
"пэрыяд": [5],
"пакінутыя": [[5,6]],
"www.oracle.com": [5],
"вылучыць": [8,11,9,[1,5,6]],
"цэлыя": [11],
"xmx1024m": [5],
"нямецкай": [11],
"віджэты": [[7,9]],
"файл": [5,6,11,10,8,1,3,[0,9],7],
"прыватныя": [11],
"абразком": [5],
"сэгмэнтаваньне": [11,[2,3,8]],
"двух": [11,[5,6,9]],
"агулам": [9],
"penalty-xxx": [10],
"перавыраўнаваць": [11],
"gotonextsegmentmenuitem": [3],
"слоўнікаў": [4,[0,7,11],[3,8,9]],
"захаваньне": [6,[8,11],3],
"прабелы": [11,[2,8],3],
"nnn.nnn.nnn.nnn": [5],
"паказчыкамі": [8],
"ес": [8],
"адмысловая": [10],
"абразкоў": [5],
"каманднымі": [11],
"abort": [5],
"выконваць": [8],
"абмен": [1],
"жа": [5],
"зададзена": [11],
"сэгмэнтаваньню": [11],
"сэгмэнтаваньня": [11,6,2,[8,10]],
"слоўнікам": [4],
"разьдзяляльнікі": [[8,9]],
"часопіс": [[3,8]],
"вызначаюцца": [[1,11]],
"табліцаў": [[3,6]],
"адмысловае": [11],
"марфалёгіі": [9,11],
"былі": [11,[5,9]],
"слоўніках": [8,11],
"аднолькавым": [[2,10]],
"плата": [5],
"адрозьненьні": [11],
"за": [11,6,[2,5,8],10],
"асобнае": [9],
"зв": [11],
"вызначэньнем": [9],
"васьмічным": [2],
"стрэлцы": [11],
"ясна": [10],
"выберыце": [11,5,8,[4,10]],
"сэсіі": [[10,11]],
"было": [11,6],
"меншай": [2],
"зн": [11,5,[6,8,9]],
"тэрміналёгіяй": [1],
"разьмешчаным": [11],
"es-mx": [4],
"зыходнымі": [[5,6,10,11]],
"літараў": [[5,8,11]],
"падтрымкі": [11],
"зь": [6,11,9,[4,5,8],[1,7]],
"напрацоўкі": [6],
"была": [[6,10,11]],
"адрозьненьне": [11],
"base": [1],
"stem": [9],
"пуск": [5],
"мэнэджара": [4],
"інструкцыі": [5,[6,7]],
"кансольны": [5],
"месцы": [[4,5,6,8,11]],
"паўшырынныя": [11],
"асобны": [[6,11]],
"лістапад": [1],
"супадзеньнем": [[9,11]],
"літарамі": [11],
"ужыць": [6,11],
"шрыфтам": [11,9,1],
"стандартную": [8],
"прадказаньні": [8],
"gedit": [1],
"асобна": [6],
"прымаецца": [10],
"старонак": [6],
"пасуе": [[8,9]],
"адключаная": [[8,11]],
"аднолькавыя": [8],
"прачытаць": [11],
"клявішамі": [11],
"імёнаў": [11],
"месца": [9,[4,6]],
"уключаныя": [11],
"сэрвэры": [6,11,5],
"захоўваньне": [11],
"word": [6,11],
"экспартаванае": [8],
"усталяваныя": [[4,5]],
"працоўным": [5,[9,11]],
"захаваньні": [6],
"захоўваньня": [4],
"скарыстаўшыся": [4],
"разьдзеле": [11,[9,10],[6,8]],
"захаваньня": [[6,11],1],
"разьдзела": [1],
"дзейсныя": [11],
"тэгам": [11],
"іншае": [9],
"машынны": [8,[9,11],[3,7]],
"вылучанаму": [8],
"сытуацыяў": [10],
"пераклады": [[6,11],[8,9],10,[5,7]],
"тэгах": [11,6],
"папярэджаньне": [[6,9,11]],
"спампаваньні": [6],
"скасуйце": [11],
"іншай": [[4,5]],
"перад": [8,[5,11],[6,9],[1,2]],
"першую": [11],
"лякальны": [6,8],
"неадназначнасьці": [11],
"некаторыя": [[0,1,6,9]],
"гішпанскай": [4],
"lingvo": [0],
"жаданай": [5],
"некаторых": [[10,11]],
"пазначанае": [5],
"перакладу": [11,6,8,9,4,10,5,1,[3,7]],
"mrs": [11],
"першапачаткова": [6],
"тэгаў": [11,6,[3,5],[8,9]],
"пазначаная": [11],
"мб": [5],
"тэксьце": [11,9,[4,6],3],
"атрыманы": [8],
"усталяваных": [5],
"спампаваньне": [5],
"гішпанская": [4],
"карыстацца": [5,0],
"мм": [6],
"пашырэньнямі": [0,[1,9]],
"лякальна": [8,6],
"усе": [11,6,[3,8],[4,10]],
"імпартаваць": [6,[3,8,10,11]],
"прыярытэт": [11,8],
"мы": [[6,11]],
"уручную": [11,1],
"pt_pt.aff": [4],
"на": [11,5,6,8,1,9,3,4,10,0,2],
"не": [11,6,5,8,1,10,[3,4,9]],
"дзейнічаць": [11],
"html": [11,5],
"перамешвацца": [1],
"апэратар": [11],
"памераў": [11],
"экспарту": [11,1],
"усё": [6,11,[3,8,9]],
"дрэнна": [11],
"пераймяноўваць": [11],
"artund": [4],
"вікіпэдыю": [8],
"ня": [11,5,[2,6],8,9,1,10,[0,4]],
"узроўню": [6,11],
"неперакладзенымі": [11],
"атрымліваюцца": [6],
"рэдагаваць": [11,[5,8,9]],
"папярэджаньні": [[5,11]],
"вікіпэдыі": [8],
"ўказаць": [4],
"кантраляваць": [5],
"умовах": [6],
"прычын": [5],
"прычым": [3],
"створанай": [9],
"зачыняць": [11],
"знаходзяцца": [11,[0,5],[2,8]],
"www.ibm.com": [5],
"разрывах": [11],
"па": [11,5,6,3,9,8,10,1,[2,4]],
"даўней": [6],
"курсора": [8,[9,11],1],
"дырэктаратам": [8],
"выпаднае": [4],
"блытаць": [11],
"пазначце": [11,8],
"умоваў": [0],
"пк": [5],
"прабельны": [11,2],
"несапраўдным": [5],
"вылучэньнем": [8],
"пр": [11],
"іншы": [11,[6,8]],
"абодвух": [6,1],
"ўзьнікае": [5],
"command": [[3,5,9]],
"будучых": [10],
"пераход": [8,[3,7],9,11],
"n.n_without_jr": [5],
"складаюцца": [11],
"кіраваньнем": [6],
"рахаваньня": [11],
"ўнесеныя": [5],
"націснуць": [11,9,8,[5,6]],
"расейскую": [5],
"ўжывае": [11],
"viewmarkbidicheckboxmenuitem": [3],
"пэўны": [6,[4,5,9]],
"зроблена": [[6,9]],
"year": [6],
"захаваліся": [11],
"прадстаўленьні": [6],
"спатрэбіцца": [11,5,6,4],
"са": [11,5,[0,6],[1,3,4,7,8,9]],
"перазапуск": [[3,11]],
"аўтасынхранізацыя": [11],
"грунтоўнага": [1],
"стан": [8,11,[6,10]],
"вылучаны": [11,8],
"вул": [11],
"бяруцца": [11],
"даведачныя": [6],
"рэгістрам": [11],
"пасьлядоўным": [11],
"кампутараў": [11],
"такімі": [5],
"зроблены": [11],
"перазапусьцеце": [3],
"прачытанка": [11],
"version": [5],
"сьпісе": [11,[4,8]],
"вылучана": [8],
"сэрвэрам": [10],
"інтэрфэйсам": [5],
"то": [11,[4,9]],
"вырашыць": [11],
"зьвярнеце": [[0,5]],
"валодаеце": [6],
"новы": [11,6,4,1,[5,8]],
"projecteditmenuitem": [3],
"аднаўляліся": [6],
"кожнай": [11],
"узроўні": [11],
"выразаць": [9],
"britannica": [0],
"улічвае": [[2,11]],
"перакладчыкі": [6],
"лякальную": [6],
"баках": [6],
"адзначаюцца": [11],
"зьявіцца": [[5,11],[1,3,4,8,9]],
"наладзіць": [11],
"множнага": [1],
"тэрмінала": [5],
"iceni": [6],
"заўвага": [6,11,[8,10],1,9],
"ніжэйшай": [6],
"шукаеце": [11],
"неабмежаваны": [5],
"загрузіцца": [6],
"распазнаць": [6],
"перацягнуць": [9],
"коскамі": [11],
"ўніз": [11],
"апошні": [[8,10]],
"вызначыць": [11,5,[3,9]],
"агрэсыўная": [8],
"закрыцьцём": [11],
"ўпісваць": [[1,11]],
"ўзяць": [2],
"dsun.java2d.noddraw": [5],
"ціхім": [5],
"гаворкі": [6],
"эквівалентам": [5],
"ўплываць": [11],
"x0b": [2],
"строгія": [6],
"камплекце": [5],
"інтэрфэйс": [5,6],
"канфліктуюць": [3],
"http": [6,5,11],
"якой": [8,[6,11],[1,3,10]],
"сваіх": [6],
"канадзкай": [11],
"якое": [11,[3,8,9]],
"ці": [5,[6,11],[0,1,9,10],[4,8]],
"выкарыстоўваеце": [6],
"множныя": [9,7],
"навядзеньні": [8],
"projectsinglecompilemenuitem": [3],
"дастаткова": [5,[6,11],10],
"элемэнты": [11,[1,6,8],[5,10]],
"выходзіць": [8],
"myfil": [6],
"паказваць": [3,8,11,[1,5]],
"можаце": [[5,6,9,11]],
"спачатку": [[10,11],6,4],
"урывак": [11],
"улічыць": [8],
"часта": [[6,8,11]],
"завялікія": [11],
"адшукайце": [5],
"перайменаванымі": [6],
"прапануюцца": [[9,11]],
"выканаць": [[5,6,8,11]],
"прыклада": [11],
"выкарыстаньне": [6,7,5,[0,4,11]],
"накіроўваць": [11],
"самая": [[6,9]],
"дзеяньнем": [8],
"выкарыстоўвацца": [5,6],
"падтрыманымі": [11],
"аналягічнае": [5],
"system-os-nam": [11],
"камэнтароў": [1],
"абавязкова": [6,11],
"optionstabadvancecheckboxmenuitem": [3],
"камэнтарох": [11],
"пазнанага": [11],
"прыкладу": [2],
"ўзровень": [6],
"замена": [11,8,[3,7]],
"optionsviewoptionsmenuloginitem": [3],
"ўзяты": [2],
"уласьцівасьці": [11,[6,8],[1,3,4,7]],
"пачатковыя": [11],
"зьмяняюцца": [[1,5,6,11]],
"прыклады": [2,11,[6,7],5],
"трэці": [1],
"радком": [10],
"адносна": [6,[9,11]],
"выкарыстаньні": [6],
"tar.bz2": [0],
"кнопак-пераключальнікаў": [11],
"лягічныя": [[2,7]],
"кадоўкай": [11],
"выкарыстаньня": [5,[1,4]],
"радкох": [11],
"bundle.properti": [6],
"праверцы": [11],
"экспартаваць": [[3,6,8,10,11]],
"выканальны": [5],
"мышы": [9,11],
"рэпазыторыяў": [[6,11]],
"кадоўках": [11],
"выкананым": [8],
"двухмоўнай": [[6,11]],
"вызначаных": [11],
"x64": [5],
"пераўтвораныя": [11],
"вашага": [9],
"самае": [[9,11],[2,8]],
"адмысловы": [[4,11]],
"зьбірацца": [11],
"значна": [[4,6]],
"канец": [2],
"самай": [[6,10]],
"вызначаным": [11,8],
"праверце": [0,[4,6]],
"радкоў": [11,2],
"аднаўленьня": [[6,11]],
"keyev": [3],
"дапамагае": [6],
"шэраг": [11],
"падвойны": [5],
"isn\'t": [2],
"ўнутры": [[1,5,10]],
"галяндзкую": [6],
"адкрыцьцём": [6],
"гэтых": [11,[4,6,10]],
"лякальныя": [6],
"шаблёна": [11],
"магутны": [11],
"ужываецца": [11,[5,6]],
"аўтаномнага": [2],
"правільныя": [[4,6]],
"страцілі": [6],
"гэтыя": [8,[6,11],[4,5]],
"падтрымлівае": [6,11],
"розьніца": [11],
"шаблёну": [11],
"optionsteammenuitem": [3],
"прапанаваным": [9],
"лякальным": [6],
"гэтым": [5,11,[6,8,10],[4,9],1],
"аднаўленьне": [11],
"лякальных": [6],
"самастойна": [[6,11]],
"gzip": [10],
"шаблёны": [11,6],
"адрэдагаваць": [11,5],
"сваёй": [5],
"друкаваных": [9],
"застаецца": [11],
"апрацоўваецца": [5],
"патрабуе": [[5,6,11]],
"разьдзяляльнікам": [11],
"зьвяртаецца": [5],
"якую": [8],
"esc": [11],
"дапаможнікам": [7],
"зьяўляюцца": [11,8,5,[1,3,9]],
"x86": [5],
"быць": [[5,6],11,1,[3,10],0,[4,8,9]],
"небясьпекі": [6],
"пералічаныя": [3],
"сынхранізацыі": [6],
"замены": [11,8],
"разьбівацца": [11],
"nostemscor": [11],
"неабходнасьць": [6],
"паказваны": [9],
"адлюструюцца": [10],
"сказы": [11],
"ўказваць": [4],
"іхніх": [10],
"вышэйшымі": [11],
"нязьменнымі": [11],
"дэфіс": [5],
"сказу": [2],
"адрозьніваць": [[10,11]],
"замест": [[3,5,6,11]],
"console-createpseudotranslatetmx": [5],
"разьдзяляльнікаў": [1],
"ядноўвае": [11],
"кантэксту": [11,[3,8,9]],
"мэтадзе": [5],
"забыліся": [0],
"дасьведчаных": [2],
"неабходнасьці": [11,[4,6],9],
"longman": [0],
"колерамі": [8],
"fuzzyflag": [11],
"ігнараваны": [11],
"нельга": [11],
"вывад": [[8,11],[3,6]],
"дасьведчаныя": [5],
"merriam": [[0,9]],
"new": [5],
"значэньняў": [5],
"шаблён": [11,2],
"патрэбамі": [5],
"ўнутраным": [11],
"ўжытых": [10],
"падтрымліваецца": [1],
"рахуюцца": [11],
"іхнія": [11],
"разьбіўкай": [11],
"плагін": [11],
"уключыць": [[3,4,8]],
"словаформы": [1],
"разьбіваецца": [9],
"яе": [5,11,9,[6,8]],
"як": [11,6,8,9,[0,1,5],[2,3],[4,7,10]],
"вызначаныя": [11,[2,5,7,8,10]],
"сэрвэрах": [11],
"закрыцьця": [6],
"n.n_without_jre.zip": [5],
"сьпісы": [11],
"сьпісу": [11],
"magento": [5],
"выяўленьня": [6],
"тэкставы": [[5,8,11]],
"аўтаномных": [5],
"паказвае": [[5,11],[0,8,9]],
"закрыцьці": [[6,8]],
"актыўнага": [[8,10]],
"рэгістар": [3,[2,8,11],5],
"аўтаномным": [6],
"зацемкамі": [8,3],
"агульны": [6,11],
"u00a": [11],
"порта": [5],
"усталяванымі": [5],
"скажам": [6],
"перагружаецца": [8,11],
"shift": [3,11,[6,8],1],
"ёй": [[1,2,9,11]],
"зразумелі": [4],
"ём": [5,11],
"ён": [11,[1,5,6],10,[7,9]],
"тэгі": [11,6,8,[3,9]],
"перакладаньне": [6,7],
"канфігурацыйны": [5],
"java": [5,11,3,2,[6,7]],
"exe": [5],
"гігабайтаў": [5],
"перавесьці": [9],
"сталі": [[3,11]],
"lang2": [6],
"распакуйце": [5,0],
"lang1": [6],
"кампутары": [5,11],
"project_save.tmx": [6,10,11],
"сказ": [6],
"прыдатнасьць": [11],
"dictionari": [0,10,7],
"спосабам": [[5,6,11]],
"перакладаньня": [[6,8,9]],
"погляд": [5],
"кампанэнтаў": [11],
"апошнім": [8],
"пачатак": [[1,2]],
"мэтаду": [5],
"ўваходжаньні": [4],
"сэрвісамі": [5],
"ўстаўкі": [8,1],
"стала": [[5,11]],
"кампутара": [5,11],
"апошніх": [[8,11]],
"мэтады": [11,5],
"надалей": [6],
"адкрывае": [11],
"стале": [5,[9,11]],
"апошнія": [[3,8,11]],
"часьцей": [6],
"блізкі": [6],
"паўтор": [2],
"устаўляючы": [11],
"уверх": [11],
"ўводзе": [8],
"ўключаная": [11,9],
"інфармацыйных": [1],
"файла-канфігурацыі": [5],
"найлепш": [[6,10]],
"ўсталяванай": [5],
"спосабаў": [6,5],
"паступова": [[6,10]],
"timestamp": [11],
"падзеленыя": [1],
"стол": [5],
"карыстаньня": [5],
"projectaccessrootmenuitem": [3],
"выпадных": [11],
"буфэра": [8],
"dyandex.api.key": [5],
"камандным": [6],
"стрэлкамі": [9],
"ніколі": [2],
"асобнікаў": [[5,8,9,11]],
"выдасьць": [11],
"ніякіх": [0],
"канчаткам": [[5,8]],
"зразумела": [[1,4,11]],
"абзацаў": [[8,11],6],
"макам": [3],
"мяняць": [9],
"адбудзецца": [10],
"зьвязаную": [10],
"абзацах": [11],
"plugin": [11],
"увесь": [[8,11]],
"зьмясьцеце": [10,1],
"прапусьціць": [11],
"выпадныя": [11],
"камандных": [[6,8],[7,11]],
"падкрэсьлены": [1],
"выглядзе": [11,[6,9,10]],
"бразылія": [5],
"разумець": [6],
"editinsertsourcemenuitem": [3],
"камандныя": [6],
"microsoft": [11,[5,6],[1,9]],
"projectnewmenuitem": [3],
"акном": [[5,11]],
"вось": [5,[6,11]],
"асобніках": [5],
"праектамі": [11],
"дадаючы": [11],
"optionstranstipsenablemenuitem": [3],
"адноўленыя": [10],
"уводу": [6,11],
"часам": [11,[2,10]],
"графічны": [[5,6]],
"адпаведнік": [1],
"вярнуцца": [[8,9,11]],
"glossari": [1,[6,10],[7,9,11]],
"дазваляюць": [[5,8,9,11]],
"слоўнік": [4,[0,7,9,11],8],
"пацвярджаць": [10],
"ignored_words.txt": [10],
"іхняй": [11],
"ім": [11,5,[0,2,6,9]],
"configuration.properti": [5],
"github.com": [6],
"забруджваць": [6],
"лякалізацыі": [6,5],
"адсутнічаюць": [9],
"кожную": [9],
"мовах": [1],
"іх": [11,6,[8,9],[5,10],3,4],
"іхняе": [6,9],
"слайдаў": [11],
"шукае": [1],
"ігнараваць": [11,[4,10]],
"улічваюцца": [11],
"загружацца": [11],
"моваў": [11,6,4,[0,1]],
"стандартнага": [9],
"тычацца": [9],
"альтэрнатыўнымі": [[6,11]],
"верхні": [2],
"next": [[0,1,2,4,5,6,7,8,9,10,11]],
"лічбы": [9],
"апошнюю": [11],
"вынаскі": [11],
"string": [5],
"налады": [11,8,[3,5,10],[4,7,9],6,2],
"адчыніцца": [11,[8,9]],
"выкарыстоўваючы": [11],
"вырабляюць": [6],
"апошняй": [[5,8]],
"ўстаўку": [11],
"апошняе": [8],
"сымбалях": [8],
"самую": [5],
"адкрытым": [[6,9]],
"not": [11],
"сымбаляў": [11,[2,5,7],[3,6,9]],
"жадаеце": [6],
"іхняю": [11],
"даданьне": [5,[6,11]],
"падобнае": [[8,9]],
"адкрытых": [11],
"адключыўшы": [11],
"мэта-тэгі": [11],
"сцэнароў": [11,8,5],
"ўстаўка": [11],
"арганізаваць": [6],
"уключанымі": [6],
"лічба": [2,6],
"ўключаць": [6,[5,8]],
"selection.txt": [11,8],
"xhtml": [11],
"уласна": [9],
"сынтаксысам": [11],
"ўдрукаваць": [1],
"падобная": [[2,8]],
"finder.xml": [11],
"сцэнарох": [5],
"вызначаюць": [6],
"перазагрузцы": [6,1],
"window": [5,[0,1,2,7,8]],
"месцаў": [1],
"тэме": [[6,11]],
"збойнага": [6],
"disable-project-lock": [5],
"omegat.pref": [11],
"улічваць": [11],
"тэму": [[6,11]],
"ўсталюйце": [4],
"фраза": [11],
"вярнуць": [[9,11],[3,8]],
"моўная": [11],
"тэмы": [[6,10,11]],
"прывязаных": [8],
"кансольнага": [5],
"зьмяшчаюцца": [11],
"паўторы": [11,9],
"howto": [6],
"аднамоўных": [11],
"адсотак": [9],
"сярэдні": [11],
"якая": [[6,11],5,[1,2,9,10]],
"pt_pt.dic": [4],
"прасторы": [6],
"фразы": [11],
"канца": [11],
"фармаце": [11,8,[0,1,3,6,10]],
"год": [6],
"ключа": [5],
"пазначаецца": [6],
"фразу": [11],
"level1": [6],
"level2": [6],
"модулю": [4],
"пакетным": [5],
"рэцэпт": [6],
"модуля": [[4,11]],
"модуль": [4,[1,2,7]],
"лішнія": [2],
"даданыя": [[8,10]],
"фармату": [11,0],
"парог": [11],
"канцы": [[2,9,10]],
"мяняліся": [6],
"мовай": [[4,5],6],
"рэалізаваныя": [11],
"прататыпаарыентаваная": [11],
"мае": [11,[1,6],[4,8,9],[5,10]],
"фарматы": [6],
"эўрапейскіх": [11],
"пакідае": [11],
"web": [5,7],
"зафіксаваць": [6],
"мяркуеце": [6],
"мовам": [11],
"en-us_de_project": [6],
"бясьпечнае": [11,6],
"галіне": [1],
"няма": [5,[1,11],[4,8,9],[6,10]],
"выбарам": [11],
"цэлай": [11],
"самой": [2],
"editselectfuzzy4menuitem": [3],
"editregisteridenticalmenuitem": [3],
"неабходныя": [[0,5]],
"славенскай": [9],
"славенская": [1],
"пастаўце": [4],
"кампіляваць": [6],
"pt_br.dic": [4],
"незалежна": [11],
"html-тэгаў": [11],
"бягучаму": [[8,11]],
"вылучаць": [[9,11]],
"кожным": [11,6],
"ажыцьцяўляецца": [5],
"unabridg": [0],
"наконт": [[6,7,9,10]],
"папярэджаньняў": [5],
"панэлямі": [9],
"дзякуючы": [11],
"трэцяй": [9],
"глясароў": [1,9,11,3,7],
"ужывацца": [11,10],
"выглядае": [[5,8,11]],
"падобна": [11],
"рэгістры": [2],
"мінімізаванымі": [9],
"optionsglossaryexactmatchcheckboxmenuitem": [3],
"зарэгістраваць": [[3,8]],
"ўключана": [11],
"пашырэньняў": [11],
"узятым": [9],
"завяршэньня": [8,6],
"перакладчыку": [6,9,11],
"падвойным": [[8,9,11]],
"кліента": [6,[5,9,10,11]],
"кожныя": [[6,8]],
"nnnn": [5],
"project_save.tmx.yearmmddhhnn.bak": [6],
"тэкст": [11,[8,9],6,10,[3,5],2],
"глясарох": [1,[7,9]],
"ўжо": [11,[6,9],[4,5,8,10]],
"перакладчыка": [6],
"рэгістрацыі": [8],
"ўнікальны": [[3,11]],
"падобны": [11,[6,10]],
"агульныя": [11,7],
"неўнікальных": [[9,11]],
"пазыцыі": [11],
"тлумачэньне": [5],
"неўнікальныя": [11,8,3],
"найбліжэйшы": [11],
"пазыцыю": [8,11,9,[1,6]],
"застаюцца": [[5,11]],
"выплыўным": [11],
"важная": [9],
"zh_cn.tmx": [6],
"слупок": [1,11],
"хуткага": [5],
"болей": [11],
"прагонаў": [6],
"ніжэйшы": [9],
"праігнараваныя": [5],
"узятыя": [[1,5]],
"прымусова": [10],
"бразыльская": [4],
"унікальны": [11],
"літаральныя": [11],
"даступнымі": [6],
"моўнай": [6],
"прымацаваная": [8],
"выконвайце": [[5,6]],
"працэсам": [11],
"archiv": [5],
"дысплее": [11],
"бачнай": [11],
"важнае": [5],
"рэзэрвовую": [6],
"хвілінах": [11],
"уключаная": [8],
"вікі-старонцы": [11],
"ўмешвацца": [8],
"чытацца": [5],
"extens": [11],
"абмежаваньня": [5],
"абмежаваньні": [11],
"сваё": [11],
"ўкл": [9],
"лік": [[10,11]],
"перацягваньне": [5,7],
"прыярытэтных": [1],
"ўмовах": [6],
"найніжэйшы": [9],
"гэтая": [11,8,5,6,9],
"пераключэньне": [6],
"перамяшчаюцца": [9],
"захоўваюцца": [11,[6,8]],
"падмэню": [5],
"вызначэньнямі": [6],
"гэтай": [11,5,[1,4,8]],
"выключэньнем": [[2,5,6,11]],
"напрамкам": [8,[3,6]],
"ўстаўленая": [9],
"інтэрнэт-злучэньне": [[4,11]],
"запуску": [5],
"паказаць": [[6,11]],
"свае": [6,11],
"пакуль": [[6,8,11]],
"выбар": [11,[4,8]],
"найважнейшыя": [5],
"diff": [11],
"дырэкторыю": [6],
"an": [2],
"editmultiplealtern": [3],
"пераключэньня": [8],
"максымізацыяй": [9],
"git.code.sf.net": [5],
"арганізаваныя": [6],
"уставіць": [[3,8,11],9],
"аднолькавы": [11],
"слупкі": [[1,11]],
"гэтае": [11,8,5],
"ўжыць": [11,6],
"ўручную": [11,[4,6],1],
"тыповы": [5],
"наступны": [3,8,5,2,[0,6,9,11]],
"служыць": [[1,6]],
"абмяжоўваецца": [11],
"be": [11],
"ідэнтыфікаваныя": [8],
"адпаведнага": [11,[8,9]],
"слупка": [8],
"націсканьнем": [8],
"filters.xml": [6,[10,11]],
"блёк": [2],
"неперакладзенага": [11,9],
"зьлева": [11],
"рэлевантнасьці": [11],
"перапісваецца": [11],
"br": [11,5],
"паўзьверх": [5],
"сартаваньня": [[8,9,11]],
"ўсталяваньні": [5],
"слупку": [[8,11],9],
"капіяваць": [[6,9,11]],
"традыцыйных": [5],
"segmentation.conf": [6,[5,10,11]],
"ўсталяваньня": [5,[8,11]],
"ca": [5],
"cd": [5,6],
"новыя": [[1,3],11,8],
"ce": [5],
"öäüqwß": [11],
"асаблівасьцяў": [11],
"двойчы": [5],
"cn": [5],
"калянтытулы": [11],
"перакладамі": [11,[8,10]],
"падачы": [2],
"адлюстроўваецца": [9],
"figur": [[1,4],[0,2,7]],
"адлюстроўвае": [8],
"cx": [2],
"скапіяваны": [9],
"унікальных": [11,9],
"apach": [6,4,11],
"новых": [8,[6,11]],
"частковага": [11],
"adjustedscor": [11],
"dd": [6],
"патрабуюць": [9],
"уваходную": [6],
"f1": [3],
"сынтаксыс": [11,3],
"f2": [9,[5,11]],
"f3": [[3,8]],
"млн": [5],
"f5": [3],
"зрабілі": [[9,11]],
"дапамогай": [5,11,9,[0,4,6,8,10]],
"dz": [0],
"editundomenuitem": [3],
"захоўвацца": [11],
"друкаваць": [11],
"перакладчыцкай": [9],
"сховішчамі": [6],
"which": [1],
"u000a": [2],
"кнопкай": [11,9,5,1,4],
"сыстэмнымі": [3],
"en": [5],
"пашырэньне": [1,0,11],
"паводзіны": [[5,8]],
"u000d": [2],
"u000c": [2],
"бледна-шэрым": [11,8],
"сваімі": [5],
"архіў": [[0,5]],
"зноў": [11,[6,8],[5,9]],
"пашырэньня": [11],
"u001b": [2],
"stats.txt": [10],
"належным": [[5,11],[0,8]],
"выразам": [11,2],
"кораня": [6],
"foo": [11],
"exclud": [6],
"for": [11],
"этапе": [[6,10]],
"падобнага": [9],
"fr": [5,[4,11]],
"наадварот": [11,6],
"паказваюць": [[9,11]],
"вобласьць": [11],
"такога": [0],
"content": [5,7],
"уліковы": [11],
"applescript": [5],
"этапа": [11],
"прызначыць": [3],
"сэрвісам": [5],
"паўтораў": [[8,11]],
"class": [11],
"helplogmenuitem": [3],
"нават": [11,8,[5,6,9]],
"рэдагуйце": [9],
"мог": [10],
"ўсе": [11,6,[5,9],[2,10]],
"выразаў": [[2,11],7,[5,9]],
"створанымі": [10],
"рэзэрвовыя": [6,10],
"editoverwritetranslationmenuitem": [3],
"чырвонае": [10],
"варыянтамі": [8],
"рахуецца": [11],
"значэньнем": [2],
"aeiou": [2],
"адключэце": [11],
"ячэйках": [11],
"тэрмін": [1,[8,11]],
"ўмоваў": [5],
"макету": [11],
"працуе": [[8,11],5,[1,4,6]],
"задаецца": [6,[1,3]],
"зачыніцца": [9],
"датычных": [11],
"сэрвісаў": [[5,11],8],
"макеты": [11],
"выразах": [11],
"месцах": [5],
"hh": [6],
"сэгмэнтамі": [11],
"duser.languag": [5],
"супадалі": [4],
"выхаду": [8],
"кіраўніком": [6],
"file-target-encod": [11],
"моц": [11],
"некалькі": [11,6,2,[1,9],5,[3,8]],
"абновіцца": [1],
"context": [9],
"зялёным": [[8,9]],
"https": [6,5,[9,11]],
"падабенствам": [10],
"id": [11],
"if": [11],
"ўсё": [5,[1,6]],
"кансольным": [5],
"project_stats.txt": [11],
"майстры": [11],
"projectaccesscurrenttargetdocumentmenuitem": [3],
"хоча": [[5,6]],
"усталяваньня": [5],
"аднакарэнныя": [11,8],
"in": [11],
"коды": [4],
"termin": [5],
"дае": [11,4],
"найвышэйшае": [9],
"is": [[1,2]],
"адрозную": [11],
"коду": [[3,4],11],
"odf": [11,6],
"апэратары": [[2,7]],
"odg": [6],
"працэс": [11,6],
"ja": [5],
"multiterm": [1],
"здольная": [11],
"усталяваньне": [5,7,8],
"odt": [6,11],
"gotonexttranslatedmenuitem": [3],
"прыдатным": [4],
"падзякі": [8],
"блякуецца": [5],
"уліковага": [5],
"фокус": [11],
"nplural": [11],
"стаіць": [11],
"js": [11],
"надзейная": [6],
"парамэтры": [11,5,6],
"ўключаюцца": [6],
"learned_words.txt": [10],
"парамэтра": [5,[3,11]],
"згаданых": [[1,5,6]],
"ненадзейны": [1],
"выпадку": [6,11,5,[9,10],[1,8]],
"мэта-тэгаў": [11],
"скапіяваць": [6,4,5,10],
"ftl": [5],
"стандартна": [[5,11]],
"два": [11,[4,5],[6,8],10],
"падайце": [5],
"viewdisplaymodificationinfoallradiobuttonmenuitem": [3],
"выпадкі": [[6,11]],
"draw": [6],
"ўвёўшы": [11],
"ужываным": [8],
"файлавага": [11,4],
"пачатку": [10,5],
"значэньнямі": [11],
"паўторна": [6,[9,11]],
"le": [1],
"утыліты": [0],
"dswing.aatext": [5],
"дрэвападобную": [10],
"адпаведнаму": [11],
"завершаны": [6],
"замяняюцца": [6],
"тыпізацыяй": [11],
"lu": [2],
"звычайным": [5,11],
"доступу": [11,[5,6]],
"звычайных": [6],
"cycleswitchcasemenuitem": [3],
"прэзэнтацыяў": [11],
"me": [6],
"omegat.png": [5],
"рэжымамі": [6],
"зьмяшчаюць": [6,[9,11]],
"entri": [11],
"альтэрнатыўны": [[8,9,11],3],
"паданы": [5,11],
"найлепшым": [11],
"mr": [11],
"звычайныя": [[1,8],[7,11]],
"ms": [11],
"mt": [10],
"непрабельны": [2],
"my": [6],
"назьбіралі": [10],
"перайменаваць": [6,4],
"запытвала": [11],
"выдзяляе": [5],
"пераўтварыць": [[6,11]],
"захавайце": [[1,3,5]],
"nl": [6],
"nn": [6],
"перасоўваньня": [11],
"no": [11],
"некаманднага": [6],
"code": [5],
"з-за": [[10,11]],
"gotohistoryforwardmenuitem": [3],
"адступіць": [8],
"прызначаных": [5],
"разглядае": [11],
"клявіятуры": [[3,9,11]],
"генэратары": [11],
"дробы": [11],
"of": [7,0],
"прызначаныя": [3],
"каментарыяў": [3],
"варыянт": [8,11,5,9],
"ok": [[5,8]],
"перайдзеце": [5,4],
"ведаюць": [11],
"дыяпазон": [2],
"беларус": [11],
"os": [[5,6,11]],
"найпрасьцейшы": [5],
"заўважце": [5,10,[1,4,6,11]],
"editinserttranslationmenuitem": [3],
"адсутнасьці": [8],
"дадаюцца": [[1,5,10]],
"захоўваць": [11,[8,9,10]],
"вэб-сайт": [10],
"неперакладзены": [[3,8]],
"po": [11,9,5],
"супадае": [11],
"разьяднаньня": [11],
"дзе": [11,[5,10],8,[1,3,6,9]],
"камэнтары": [11,9,[1,7]],
"optionsglossarystemmingcheckboxmenuitem": [3],
"кампутар": [11],
"падкаталёг": [10,[5,6,11],4],
"pt": [5],
"клікнеце": [11,5,[1,8]],
"сродкаў": [6],
"часткі": [11,[4,6]],
"запісах": [1],
"падобным": [11,[5,6,9,10]],
"прызначаная": [6],
"зычных": [2],
"мышкі": [[4,5,8,11]],
"выпадак": [6],
"сетцы": [5],
"пункце": [11],
"першага": [[1,5,8,11]],
"перакрываліся": [9],
"зьмяняецца": [[8,10,11]],
"edit": [8],
"editselectfuzzy5menuitem": [3],
"рэпазыторый_праекта_omegat.git": [6],
"пераўтвараецца": [[6,8]],
"дыялёгавыя": [11,[7,8,9]],
"rc": [5],
"includ": [6],
"падобных": [11,[6,10]],
"адымаецца": [10],
"апрацоўваюцца": [6],
"падобныя": [11,6,9],
"гэтую": [11,[5,6,8,9]],
"зарэзэрваваны": [4],
"sc": [2],
"цэтлікі": [5],
"пазьбягаць": [11,10],
"найвышэйшым": [9],
"імітаваць": [8],
"аргумэнтаў": [5],
"націснуўшы": [11,9],
"недакладныя": [[9,11],[6,7]],
"дыялёгавым": [11,1],
"абмежаваная": [6],
"ідэнтыфікатар": [[5,6,11]],
"прызнаць": [11],
"імі": [[0,6,8,11]],
"абмежаванай": [11],
"агульная": [11,9],
"пункты": [[8,9]],
"недакладным": [8,9],
"мэты": [1],
"спасылкі": [[0,2,11]],
"недакладных": [11,8,9,[6,7,10]],
"пункту": [11],
"editoverwritesourcemenuitem": [3],
"расейскай": [5],
"поўнашырынных": [11],
"паказваецца": [8,9,11,[5,10]],
"свой": [[6,11]],
"enforc": [10],
"аргумэнтах": [5],
"remov": [5],
"замяняе": [8],
"tm": [10,6,8,[5,7,9,11]],
"імя": [11],
"to": [[5,11]],
"v2": [5],
"пункта": [[3,5,11]],
"document.xx": [11],
"фарматаваньні": [6],
"tw": [5],
"запісы": [1,11],
"вокнаў": [11],
"тэхналёгія": [5],
"сэкундаў": [11],
"фарматаваньня": [6,[10,11]],
"запісу": [[1,5]],
"viewmarkautopopulatedcheckboxmenuitem": [3],
"projectwikiimportmenuitem": [3],
"countri": [5],
"інш": [[5,11]],
"колькасьці": [11,4],
"сэкундах": [11],
"арыгінальны": [6],
"атрымліваць": [11],
"паведамленьні": [[5,6,9,11]],
"адпраўце": [6],
"максымізаваць": [9],
"запіса": [[8,11]],
"клявіша-мадыфікатар": [3],
"this": [[1,2]],
"карыстайцеся": [6],
"колькасьць": [11,9,8,[6,10]],
"аргумэнт": [5],
"колькасьцю": [11],
"тэрміналягічнымі": [1],
"ход": [9],
"разумее": [6],
"зрушаныя": [8],
"стыляў": [6],
"кліку": [8,11],
"vi": [5],
"усталяваць": [5,4,0],
"азнаёмцеся": [5],
"для": [11,5,6,8,4,3,2,10,9,1,7],
"няслушных": [11],
"паведамленьне": [5,6],
"фільтрацыя": [11],
"скароту": [11],
"захоўваецца": [[8,11],[1,9]],
"тэхналёгіяў": [1],
"варыяньце": [5],
"выпраўленьні": [[6,11],8],
"groovy.codehaus.org": [11],
"захоўваючы": [10],
"скарота": [11],
"закранаецца": [1],
"адмыслова": [[5,11]],
"выпраўленьня": [4],
"backspac": [11],
"нефарматаваны": [11],
"абароны": [11],
"ісо": [1],
"svn-кліента": [10],
"emac": [5],
"org": [6],
"фарматаваньне": [6,11],
"пажаданай": [11],
"distribut": [5],
"падабаецца": [11],
"арганізацыя": [6],
"частка": [8,9,5],
"xf": [5],
"запісам": [1],
"найлепшае": [11],
"значэньне": [11,[3,5]],
"выніковы": [5],
"ужываць": [11,3,[1,5,6,8]],
"робячы": [11],
"канкрэтнага": [11,6],
"большай": [2],
"уключна": [[2,6]],
"мэню": [3,[7,11],8,5,9,6,[4,10]],
"частку": [11],
"xx": [5,11],
"xy": [2],
"sourc": [6,10,11,[5,8],[1,7,9]],
"ўключаныя": [11,6],
"ягонага": [11],
"дрэвападобнай": [10],
"прагортка": [11],
"двукосьсі": [1],
"type": [6,3],
"каманьдзе": [5],
"сьціснутымі": [10],
"нейкі": [11,10],
"над": [6,11,8],
"toolssinglevalidatetagsmenuitem": [3],
"момант": [9],
"сэансах": [5],
"адпраўка": [11],
"называецца": [11,5],
"спалучэньнямі": [3],
"projectaccesssourcemenuitem": [3],
"yy": [9,11],
"перазагружаны": [[1,6]],
"тэкстамі": [11],
"бягучы": [8,9,5,[6,10]],
"выгляд": [[3,11],7,[8,9],6],
"выплыўнае": [9,11],
"ўтрымліваць": [11,[1,3,4,5,6,9,10]],
"push": [6],
"readme_tr.txt": [6],
"парамэтрамі": [5,6],
"асноваў": [9],
"адпраўкі": [11],
"парамэтар": [5,[6,11]],
"penalti": [10],
"сынхранізаваных": [11],
"адчыненымі": [[9,11]],
"сынхранізаваныя": [6],
"каталёг": [5,6,10,11,9,[0,4,8],7],
"клявішы": [[3,11]],
"большасьці": [11,[3,5]],
"utf8": [1,8],
"out": [6],
"хто": [5],
"dark": [11],
"дакумэнта": [[3,8],9],
"каранёвым": [[5,6]],
"многіх": [[6,11]],
"супадзе": [11],
"клявішу": [9,[3,11]],
"packag": [5],
"power": [11],
"tag-valid": [5],
"сеткі": [6],
"файлу": [11],
"файлы": [11,6,5,10,8,[1,4],0,[3,9],7],
"уключае": [[2,6,11]],
"файла": [11,6,1,5,10,8,9,[0,3,7]],
"наяўны": [10],
"файле": [[5,11],6,[8,10],[1,9]],
"u0009": [2],
"xhh": [2],
"чаргу": [11],
"revis": [0],
"u0007": [2],
"дакумэнты": [11,8,[5,6]],
"repositori": [6,10,7],
"lowercasemenuitem": [3],
"firefox": [[4,11]],
"wiki": [9],
"мадэлі": [11],
"ідэнтычны": [[3,8,10,11]],
"ліцэнзійнай": [5],
"ўстаўляецца": [8,11],
"хвіліны": [6,8,11],
"опцыяй": [11],
"скарыстацца": [6,[4,5],[1,2,9,11]],
"сымбалем": [[1,11]],
"url-адрас": [6,8],
"уваходжаньнях": [11],
"пакіньце": [5],
"nl-zh": [6],
"адсартаваць": [11],
"апрацоўваў": [11],
"ўвесьці": [11,5,1],
"лучыва": [5],
"клявіша": [3,[8,11]],
"сэгмэнт": [8,11,9,3,10,6],
"якім": [11,5,9,[1,2,4]],
"пошук": [11,8,3,[2,7]],
"паколькі": [[1,11],6],
"openoffic": [[4,6],11],
"ўласныя": [11],
"стварае": [5],
"увогуле": [[1,10]],
"якіх": [11,[1,6,8]],
"якія": [11,6,[5,9],8,[1,2,10],3],
"note": [2,9],
"абнаўленьняў": [[8,11]],
"якога": [[8,11],[4,6]],
"optionsautocompletechartablemenuitem": [3],
"загружаюцца": [11],
"выявіўшы": [11],
"трэба": [11,6,5,10,9,[1,4],3],
"git": [6,10],
"кампіляцыя": [6],
"шуканым": [11],
"фіксацыі": [6],
"тоесным": [8],
"дакумэнце": [3],
"квантары": [2,7],
"фіксацыя": [[6,8]],
"xx-yy": [11],
"спробе": [5],
"падборка": [0],
"відавочна": [9],
"уваходжаньняў": [11],
"вырашылі": [11],
"кампіляцыі": [[6,11]],
"другога": [9],
"optionsspellcheckmenuitem": [3],
"колькі": [[6,8,9]],
"стандарту": [[1,6]],
"ідуць": [11],
"optionssetupfilefiltersmenuitem": [3],
"паведаміць": [[6,9,11]],
"забесьпячэньня": [11],
"падрабязьней": [6],
"ўгару": [11],
"altgraph": [3],
"прыхільнікі": [11],
"прасэгмэнтаванымі": [11],
"рэгулярным": [11,2],
"канфігурацыйнага-файла": [5],
"разгортваньня": [5],
"ўключыць": [[8,10,11]],
"пашкоджаньня": [11],
"without": [5],
"блёкі": [11],
"вэрсіі": [5,6,10,[2,4,8]],
"знаходзіць": [1],
"xml": [11,1],
"плятформаў": [5],
"марфалягічны": [[3,11]],
"вэрсію": [[5,8]],
"вэрсія": [5,6],
"адсочваць": [11],
"xmx": [5],
"вызначае": [[5,11]],
"назваў": [11,[1,9,10]],
"рэгулярнаму": [11],
"асяродкам": [5],
"распакаваны": [5],
"нейкага": [8],
"befor": [5],
"колерам": [8,11,9],
"util": [5],
"назвах": [6],
"пункт": [3,8,[4,5,11]],
"абмену": [[1,8]],
"патрэбнага": [5,6],
"устаўляе": [11],
"tar.bz": [0],
"ўлічваючы": [1],
"лепшыя": [11,10],
"назвай": [5,10,[0,6,11]],
"адмяніць": [[3,8]],
"перагрузеце": [11],
"дапаможнік": [[7,8],[3,5]],
"камандзе": [5],
"спампаваны": [5],
"аднавіць": [11,9,3,[6,8]],
"лякаль": [11],
"зьмянеце": [11,[3,4]],
"наступных": [11,[5,6]],
"xlsx": [11],
"статыстыкай": [8],
"адміністратар": [11],
"лякалі": [11],
"разгортваньне": [5],
"assembledist": [5],
"даяце": [5],
"схаваных": [10],
"пакінуты": [11,8],
"наступныя": [11,6],
"пашукаць": [5],
"належнай": [5],
"вітэрбі": [11],
"target.txt": [11],
"адключаецца": [8],
"кантроль": [[6,11]],
"мадыфікацыі": [6],
"слоў": [11],
"знакамі": [[6,11]],
"існуючага": [1],
"наладах": [[1,8,11]],
"выніковых": [11],
"ўвесь": [8,9],
"наладаў": [8,11,5,3],
"nameon": [11],
"адначасова": [11,8],
"optionsglossarytbxdisplaycontextcheckboxmenuitem": [3],
"будзем": [6],
"камандай": [5,8],
"gotonextnotemenuitem": [3],
"tar.gz": [5],
"gpl": [0],
"тэставаньня": [2,7],
"тэкставых": [[6,11]],
"тэкставым": [[1,5,6]],
"актываваны": [8],
"становіцца": [8],
"list": [7],
"сэрвіс": [[3,11],[7,8],10],
"прыблізна": [6],
"верагодна": [[5,6]],
"самымі": [5],
"асобніка": [9],
"lisa": [1],
"ўводу": [8],
"azur": [5],
"камандаў": [11],
"тэкставыя": [11],
"граматыкі": [11,8],
"наяўная": [[5,11]],
"малюнкі": [[6,11]],
"дзеяньня": [8],
"выраўноўваньне": [[6,11],5],
"забясьпечвае": [11,[5,6,9]],
"пустога": [6],
"групы": [[9,11]],
"загрузкі": [11,8],
"асобніку": [5],
"паўставаць": [6],
"ужываньні": [6],
"адзначыць": [[6,11]],
"ужываньня": [11],
"захаванага": [6],
"атрыбутам": [11],
"зыходных": [6,11,5,8,[3,7]],
"выраўноўваньня": [11,[5,7,8]],
"падчас": [[5,11],6,[1,8,10]],
"група": [2],
"карыстаецеся": [3],
"зыходныя": [8,11,[3,6],5],
"ужываньне": [11,7,[1,4,5,6],8],
"зыходным": [11,9,[6,10],[1,3,5,8]],
"with": [5,6],
"групу": [6],
"даступны": [11],
"pdf": [6,[7,8,11]],
"дзеяньні": [[0,6,11]],
"другая": [[5,9]],
"праблемы": [[0,1,7]],
"выніковыя": [5],
"праблема": [1],
"літаральна": [2],
"праграме": [6,[8,11]],
"варыянты": [11,6,[2,5]],
"прыдатны": [5],
"аўтаматычна": [11,8,[1,5],3,[6,9]],
"toolsshowstatisticsmatchesmenuitem": [3],
"праграма": [5,[4,6]],
"карыстальніцкія": [[3,8,11]],
"ціхага": [5],
"viewdisplaymodificationinfononeradiobuttonmenuitem": [3],
"выпраўленая": [1],
"выдзяленьне": [5],
"праграму": [5,[3,8]],
"лякальнымі": [11],
"унізе": [11,3],
"значэньня": [11],
"аўтаматычны": [11,8],
"зацемак": [9],
"краінай": [5],
"абярэце": [[4,11],5],
"перайменаваньнем": [6],
"чарнавік": [11],
"прадстаўляе": [11,5],
"варыянта": [11,6],
"адмацаваць": [9],
"праграмы": [5,6,11,[4,7,8,10]],
"забясьпечыць": [[0,11]],
"малюнку": [4],
"ўтрымліваецца": [11],
"адсоткавых": [9],
"адлюстроўваюцца": [11,1],
"projectaccesswriteableglossarymenuitem": [3],
"карыстальніцкіх": [11],
"капіяваньне": [4],
"камандны": [6,5,[3,7,8]],
"напрамку": [6],
"адсоткавыя": [9],
"варыянту": [11],
"перакласьці": [6,[8,9,11]],
"малюнка": [9],
"атрыбутаў": [11],
"парамі": [6],
"націскайце": [[1,6,8,11]],
"regexp": [5],
"перайсьці": [11,9],
"замінаць": [5],
"кліент": [6],
"sentencecasemenuitem": [3],
"фрагмэнце": [11],
"адрынаюцца": [6],
"спампаваць": [5,[0,3,4,8],[1,6,7,11]],
"дакладнай": [1],
"uhhhh": [2],
"уліковыя": [11],
"перакладанаму": [10],
"optionssentsegmenuitem": [3],
"сувязь": [9],
"цыклічна": [3],
"ўверх": [11],
"вольна": [0],
"уключэце": [11,8],
"апублікаваць": [6],
"optionsaccessconfigdirmenuitem": [3],
"клікнуць": [9,[1,4,6]],
"складзе": [4],
"test.html": [5],
"xxx": [10],
"рэгулярнага": [5],
"smalltalk": [11],
"адбывацца": [8],
"пакідаюцца": [11],
"слушныя": [10],
"адчыніць": [5,11,[3,8],6],
"экспартуюцца": [11],
"браўзэры": [[5,8]],
"дапаможныя": [6],
"ствараць": [[6,11]],
"нейкія": [2],
"фарбуюцца": [9],
"усіх": [11,6],
"дапаможных": [6],
"існы": [11,[1,5]],
"pseudotranslatetmx": [5],
"нейкіх": [11],
"зыходную": [[9,11]],
"павінен": [6,[0,3,11]],
"адмаўленьне": [2],
"вынікі": [11,[1,2,8]],
"targetlanguagecod": [11],
"выніку": [8,5],
"ігнараваньнем": [8],
"пачынаць": [11],
"званок": [2],
"хочаце": [[3,6,11],[5,8]],
"разоў": [2,[6,11]],
"наладкі": [8,4],
"перакладныя": [11],
"выводзіцца": [11,[5,6]],
"патрабуецца": [5,[6,11],2,[1,3,4]],
"палічыць": [6],
"сымбалямі": [1],
"ўжывалася": [11],
"вяртаньня": [2],
"лацінская": [6],
"пазнавацца": [1],
"элемэнтамі": [9],
"рэгулярныя": [2,11,7,[3,4,6]],
"адкрыцьці": [6,1,11],
"уважаюцца": [11],
"захоўвае": [[5,8]],
"наладка": [[3,7,11],[2,4]],
"гарантаваць": [11],
"здаецца": [1],
"пакінуць": [11],
"encyclopedia": [0],
"стылістычнага": [11],
"клікаючы": [5],
"сьлед": [[5,10]],
"атрымайце": [5],
"наладку": [11],
"блёку": [2],
"імпарту": [6],
"рэгулярных": [[2,11],7,5],
"трыма": [6],
"мінімізаваць": [9],
"наўпрост": [11],
"optionstagvalidationmenuitem": [3],
"спосаб": [11,5],
"цытаваньне": [[2,7]],
"падбіраліся": [6],
"структурныя": [11],
"заміналі": [11],
"pt_br": [4,5],
"абмежаваць": [11],
"маркі": [11],
"блянка": [2],
"a-z": [2],
"кодаў": [4],
"структурным": [11],
"экранаваньня": [5],
"гэтымі": [8],
"практычнага": [9],
"увесьці": [11,2],
"javascript": [11],
"mediawiki": [11,[3,8]],
"input": [11],
"дакладней": [11],
"колеры": [11,3],
"эўрапейскага": [6],
"электронныя": [9],
"парады": [4,[6,7]],
"сымбаль": [2,11,5,[1,7]],
"сымбаля": [[8,11]],
"вызначаць": [[2,11]],
"ягоным": [5],
"сымбалі": [11,8,1,3,2],
"найпрасьцейшым": [5],
"адсоткам": [9],
"парада": [[5,6]],
"скарочана": [11],
"капіююць": [6],
"цытаваньня": [2],
"полі": [11,5,9,2],
"спосабы": [[1,4,6]],
"выбіраецца": [[8,9]],
"ўсталёўкі": [4,5,[9,11]],
"роўнасьці": [1],
"леваруч": [8],
"адсылаць": [11],
"поля": [4],
"павінна": [[1,5],[2,3,6,11]],
"googl": [5,11],
"урыўкі": [6],
"прадукцыйнасьць": [11],
"зьместам": [11],
"opendocu": [11],
"якімі": [11],
"назьве": [[10,11]],
"аддаленае": [6],
"download.html": [5],
"яднаныя": [5],
"цалкам": [6,[5,11]],
"стаяць": [2],
"поле": [11,[8,9],[1,2]],
"практычны": [5],
"дыялёгавае": [10],
"адчынены": [[6,8],5],
"sourceforg": [3,5],
"ўсталёўка": [5],
"дзейнічае": [[10,11]],
"кіруйцеся": [5],
"павінны": [11,6,[4,5],[0,3]],
"парадак": [9,[8,11]],
"часткова": [9],
"editmultipledefault": [3],
"абнаўленьне": [5,1],
"паказвацца": [8,[5,6,11]],
"mozilla": [5],
"азначаныя": [6],
"editfindinprojectmenuitem": [3],
"характарыстыкаў": [11],
"warn": [5],
"перакладзенага": [11,[6,8,9]],
"technetwork": [5],
"уведзены": [[8,11]],
"фільтар": [11],
"вэрсіямі": [6],
"выраўноваецца": [6],
"plural": [11],
"капіявацца": [11],
"распакаваньне": [5],
"сапраўднага": [5],
"акне": [11,[4,5,9],6],
"выбірайце": [11],
"пільнаваць": [4],
"узровень": [6],
"працягу": [6],
"акно": [11,[5,8],9,[7,10]],
"дадаваць": [11,[1,3,5]],
"злучок": [5],
"несэгмэнтаваных": [11],
"распаўсюджваць": [6],
"кадоўкамі": [11],
"адкрытага": [9],
"назоўны": [1],
"яго": [11,[5,8],6,[9,10],1,4],
"амаль": [2],
"акна": [[8,11]],
"кодам": [[5,6]],
"набор": [11],
"перацягнуўшы": [5],
"colour": [11],
"n.n_windows.ex": [5],
"прыдатнага": [5],
"pop-up": [1],
"выдаляе": [11],
"звонку": [9],
"распакаваньня": [5],
"альгарытмы": [11],
"паказаны": [[9,11]],
"адпавядае": [2,11,4,[6,8]],
"бюро": [9],
"напісаньні": [6],
"мова-краіна": [11],
"статыстыка": [3,8,10],
"вызначана": [4,[10,11]],
"program": [5],
"перацягваць": [9],
"выбару": [5],
"спэцыфікацыю": [11],
"паказваюцца": [11,9,1,[2,5,6,8]],
"межах": [6,10],
"адбылося": [11],
"уласныя": [[2,9,11]],
"дзьве": [5,11],
"скапіяваныя": [9],
"статыстыкі": [8],
"працаваць": [4,3],
"новая": [[5,11]],
"скапіяваных": [6],
"прыняць": [[10,11]],
"абмежаваным": [1],
"разьдзяляе": [11],
"n.n_mac.zip": [5],
"зручнага": [6],
"tabl": [2,3,[7,9],11],
"далейшых": [11],
"ўласьцівасьцях": [[0,4]],
"вакне": [11,8,[1,5,6,9]],
"замяняецца": [8],
"парамэтрам": [6],
"зрабіць": [11,[4,5,6],9],
"абраны": [5],
"графічнага": [[5,10]],
"паказаных": [9],
"вакно": [[8,11],9,[1,3,4,7,10]],
"ажыцьцяўляць": [11],
"зьмяненьня": [5,11],
"ўзроўню": [6,10],
"пераканайцеся": [5,[0,4]],
"ўласьцівасьцяў": [1],
"вакна": [11,9,8],
"theme": [11],
"крэатыўнага": [11],
"маркер": [9],
"пабачыць": [11,8,[5,9,10]],
"pseudotranslatetyp": [5],
"інтэрвала": [11],
"націсьнеце": [11,5,[1,9],[4,8]],
"тыповым": [6],
"азначаюць": [11],
"вялікія": [[3,8,11]],
"ўключае": [5,9],
"выбары": [11,8],
"карыстальнікі": [5,7],
"тэрміны": [1,9,[3,7,11]],
"межаў": [[2,7]],
"вызначаны": [5,[3,11]],
"ягоную": [11],
"тэрміна": [[1,8,11]],
"запускаць": [5,11],
"ўзроўні": [8,11],
"парамэтрах": [11,8],
"няправільнае": [1],
"інтэрвалы": [11,6],
"карыстальніку": [11,8],
"пераходу": [11,8,[1,3]],
"наступную": [11],
"перакладзены": [11,6,[3,5,8,9,10]],
"прыкладна": [[1,5]],
"аналіз": [11],
"зможаце": [5],
"projectclosemenuitem": [3],
"viewmarknonuniquesegmentscheckboxmenuitem": [3],
"дасьць": [11],
"валюты": [2],
"перапынак": [4],
"правераныя": [5],
"наступным": [11,5,[3,6],[1,8]],
"перш": [[6,11]],
"загалоўках": [[3,8]],
"findinprojectreuselastwindow": [3],
"загалоўкам": [11],
"readme.txt": [6,11],
"зьмяненьне": [11,6],
"выцягнутымі": [11],
"languagetool": [11,8],
"хутка": [11],
"зацемкі": [9,11,7],
"абмінаць": [11],
"source.txt": [11],
"працоўнага": [5],
"апрацоўвацца": [5,[6,11]],
"files.s": [11],
"недакладнае": [3,[8,9,10,11]],
"exchang": [1],
"паказаныя": [[9,11],6],
"сьпярша": [5],
"англійскай": [5],
"новае": [8,11],
"мінімальнае": [6],
"яўна": [11],
"кіраваньня": [6,8,[1,3]],
"адказ": [5],
"currseg": [11],
"зацемка": [8,[3,9],11],
"ўжывацца": [11,6,5],
"функцыю": [11],
"новай": [5,11],
"point": [11],
"функцыя": [8,[1,11],[0,6,9]],
"праект": [6,8,11,3,5,[7,10],[1,9],4],
"пераключацца": [6,8],
"функцыі": [[4,11],9],
"адзінкамі": [11],
"пасьля": [11,5,8,1,6,[3,4,9],2],
"пацьвярджаецца": [9],
"перапісваць": [11],
"грэцкага": [2],
"ўласна": [5],
"падбору": [[6,9]],
"парадку": [11],
"вылучэньне": [8,3,11],
"жоўтым": [[8,9]],
"індэксаў": [11],
"пашкоджаньне": [1],
"пераўтварэньня": [6],
"падборы": [2],
"пераўтварэньні": [6],
"мінімальная": [11],
"апісваць": [6],
"марак": [11],
"кропачкай": [8],
"напрамак": [6],
"выніковую": [11],
"прывязваць": [8],
"які": [11,5,8,[6,9],[2,10],1,[3,4]],
"кіраваньне": [11],
"паказана": [11],
"плятформах": [5],
"account": [[5,11]],
"цяпер": [11,[3,6,10]],
"адключыць": [[8,11]],
"мець": [[5,6,11],[1,3,9],[4,8]],
"dhttp.proxyhost": [5],
"паказе": [11],
"падзеі": [3],
"спампаваны_файл.tar.gz": [5],
"ужываюцца": [[6,11]],
"ўплываюць": [11],
"мусіць": [[1,5,11]],
"падзея": [3],
"паказу": [11,[5,8]],
"перастаўляць": [11],
"вылучаецца": [9],
"важныя": [6],
"яно": [[6,11]],
"найімавернейшы": [9],
"доступ": [3,[5,6,8,11],[0,9]],
"пакажа": [6],
"даданае": [1],
"зьмяшчаць": [10],
"ніжэй": [5,[2,6,9],[3,4,11]],
"яны": [11,6,5,[3,4],[2,9]],
"меней": [[5,10]],
"дакумэньце": [6,[8,11],1],
"яна": [9,[5,11]],
"configur": [5],
"падтрыманыя": [6],
"спрабаваць": [11],
"можа": [11,6,5,[1,9],3,10,[0,4,8]],
"зьвяртайцеся": [[2,9]],
"падтрыманых": [5],
"адчыняць": [[5,8,9]],
"насупраць": [11],
"нябачны": [11],
"бязь": [11],
"прыкладзе": [9,6,[1,2,4,5,11]],
"optionsworkflowmenuitem": [3],
"ўлічваць": [[2,10]],
"бягучага": [11,8,[3,9],10,[1,6]],
"releas": [6,3],
"ацэнка": [11],
"нязьменным": [11],
"вернуцца": [6],
"перакладчык": [6,[9,10,11]],
"капіюецца": [[8,11]],
"sparc": [5],
"перазагрузкі": [6],
"адзін": [[2,11],8,6,[5,9],[0,1,10]],
"вызначанай": [11],
"выраз": [2,7,11],
"мяжа": [2],
"такія": [[6,10]],
"сэгмэнтаваць": [11],
"азнаёміцца": [5],
"такім": [6,[5,10,11],[1,9]],
"сэнсу": [5],
"сыстэмная": [11],
"час": [4,[6,8,11]],
"галосных": [2],
"такіх": [11,[4,5,9]],
"пасунуць": [11],
"карыстальніка": [5,11,7,8,[3,6,9,10]],
"subdir": [6],
"робіць": [[6,11]],
"націскаць": [11],
"кароткія": [11],
"фільтра": [11,6],
"запаўняецца": [6],
"непажаданая": [8],
"пазначыць": [11,[5,8,10]],
"мовы": [11,4,6,[2,5],[7,8]],
"дзесяці": [8],
"наступная": [[3,8,9]],
"ейны": [[5,9,11]],
"мову": [5,6,11],
"фільтры": [11,8,3],
"пэўную": [5,11],
"мове": [9,[4,5,10,11]],
"заблыталіся": [9],
"меры": [11,9],
"мова": [[5,6,11],4,1],
"file-source-encod": [11],
"some": [6],
"канфігурацыйныя": [5,8],
"падыходу": [11],
"звычайна": [5,11,9,[2,6]],
"інструментаў": [[6,9]],
"дамовы": [5],
"пашукайце": [0],
"нага": [10],
"наступнай": [5,11,[0,3,6]],
"пашырэньнем": [11,6,10],
"спрабуе": [11,5],
"наступнае": [11,[0,2,3,4,6,8]],
"вызначанаму": [4],
"аўтаматычную": [11],
"асаблівага": [11],
"рэзэрвуецца": [5],
"перагляду": [6],
"editexportselectionmenuitem": [3],
"прыватнасьць": [5],
"знойдзены": [11],
"ўнутраная": [9],
"робіце": [11],
"кіроўны": [2],
"home": [6,[0,1,2,3,4,5,8,9,10,11]],
"знойдзена": [5],
"падтрымліваюцца": [11,[1,2,5,8]],
"фізычна": [4],
"памылак": [8,6],
"projectaccesstargetmenuitem": [3],
"зьмяніліся": [11],
"звычайны": [11],
"памылка": [6,5],
"прыкладах": [5],
"памяць": [6,11,5,[8,9,10]],
"вэрсіяй": [5],
"запамінае": [8],
"опцыю": [11,8],
"кліентам": [6],
"опцыя": [11,8],
"верхняга": [11],
"інтэрвал": [[6,8,11]],
"выдаленьня": [11],
"aligndir": [5],
"фармат": [1,6,[7,11]],
"памяці": [6,11,10,5,9,8,[1,2,7]],
"system-host-nam": [11],
"менш": [5],
"action": [8],
"тэкставае": [[2,6]],
"прыкладаў": [[2,11]],
"поўных": [11],
"creat": [11],
"python": [11],
"es_mx.dic": [4],
"вэрсіяў": [5],
"закладкі": [11],
"памылкі": [[4,5,8]],
"сумяшчальная": [5],
"сэгмэнта-крыніцы": [9],
"infix": [6],
"палегчыць": [11],
"назвамі": [4,6],
"паказьнікі": [9],
"сэнс": [[4,11],10],
"запамінаць": [8],
"памылку": [5,[6,11]],
"поўным": [[5,9]],
"прыкладам": [6],
"пэнальты": [10],
"дыялёгавымі": [11],
"гнуткасьці": [11],
"аналягічна": [11],
"крыніца": [[0,3,8,11]],
"партугальская": [4],
"дадаткова": [11],
"ўвядзеце": [[5,11],1],
"створанага": [4],
"ўстаўцы": [11,10],
"партугальскай": [5],
"file": [11,5,6],
"пашырыць": [11],
"заканадаўства": [6],
"ўстаўце": [8],
"дадатковы": [5],
"перапісаная": [5],
"ажыцьцяўляцца": [6],
"menu": [1,9],
"белы": [11],
"функцыяў": [8],
"выдаленьне": [[6,11]],
"мела": [11],
"азначае": [11,2],
"a-za-z": [2,11],
"тэкставая": [6],
"прыярытэтны": [1,7],
"бела": [11],
"дакладных": [8,11],
"адчынеце": [[5,10,11],[6,8]],
"тэхнічныя": [8],
"source-pattern": [5],
"удрукаваны": [9],
"запісаныя": [5],
"панізіўшы": [11],
"вопцыі": [8],
"дэклярацыю": [11],
"вопцыю": [11,8],
"дэклярацыі": [11],
"крыніцы": [[6,11],8],
"запісаным": [11],
"фразаў": [11],
"левай": [11],
"true": [5],
"выдаліць": [11,[5,6],10,[3,4,8]],
"выклікаецца": [11],
"пацьвярджаць": [[3,11]],
"часопіса": [8],
"аўтаматычным": [[5,6,11]],
"groovi": [11],
"немагчыма": [[5,9]],
"напісаных": [11],
"неўнікальны": [11],
"аўтаматычныя": [[6,11]],
"дакумэнт": [[6,7,9,11]],
"праўкі": [[8,11]],
"kmenueditor": [5],
"безь": [6],
"задоўгае": [11],
"ключоў": [11,5],
"плагіны": [11],
"праўку": [10],
"ўтрымлівае": [6,[7,8,11]],
"асноўная": [5],
"чынам": [11,5,6,[9,10],[1,2,3,8]],
"master": [6],
"дакладныя": [1],
"kmenuedit": [5],
"межы": [[8,9]],
"адпаведнасьцяў": [6],
"праўка": [11,[3,7],[8,9],[6,10]],
"xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx": [5],
"writer": [6],
"функцыянальны": [11],
"rubi": [11],
"аўтазавяршэньне": [[3,11],8],
"пастаўляецца": [5],
"зьмест": [11],
"неабходна": [5,[1,6,9]],
"каманда": [5,[8,11],6,[3,7]],
"проксі-сэрвэрам": [11,3],
"абразок": [5],
"сутыкняцеся": [6],
"элемэнтаў": [11,6],
"лякальная": [8],
"пазьбегнуць": [11],
"знаходзіцца": [5,6,10],
"канцоў": [2],
"каманду": [[5,6],11],
"значную": [11],
"скампіляваць": [6,[3,8]],
"самага": [6,[9,10]],
"каманды": [11,5,8,[3,6]],
"карэткі": [2],
"user.languag": [5],
"regex": [2,7],
"зьяўляецца": [8,11,[5,9],[4,6,7]],
"лякальнае": [6],
"meta": [3],
"keystrok": [3],
"канчатковай": [11],
"абнаўляць": [11],
"лякальнай": [5],
"паўтлустым": [11,9,1],
"запаўненьня": [[8,11]],
"належнага": [6],
"выкарыстоўваюцца": [[4,11]],
"арыгіналу": [9,11],
"сайта": [11],
"асяродкі": [5],
"дублікатаў": [11],
"арыгінала": [[6,8],11],
"выяўляцца": [4],
"спосабамі": [11],
"прызначана": [10],
"пераводы": [11],
"эквіваленту": [[0,8]],
"загрузцы": [11,6],
"ўзаемадзейнічаць": [[6,11]],
"аўтазавяршэньня": [11,8],
"эквіваленты": [9],
"нешта": [[5,10,11]],
"асяродка": [5],
"выкарыстаны": [[5,11]],
"абразкі": [[5,9]],
"інтэрфэйса": [5,[10,11]],
"інтэрфэйсе": [9],
"ibm": [5],
"аўтаматычнае": [6],
"фільтрамі": [6],
"сьціскаць": [11],
"сюды": [9],
"аўтаматычнай": [11],
"аднаўляецца": [8,10],
"вокны": [11,[7,8,9]],
"ўсталяваныя": [8],
"правіла": [11,10],
"даніх": [6,11,[1,5,7]],
"запампоўваюцца": [8],
"варыянтам": [8,[1,5,6,11]],
"канкрэтна": [5],
"функцыямі": [11],
"ўваходзіць": [11],
"утрымліваюць": [6],
"пераходзе": [9,8],
"данія": [6,11,10],
"трапяць": [11],
"называем": [11],
"аўтаматычная": [11,4],
"другое": [3],
"пад": [5,0],
"ягонае": [[1,11]],
"зьмяняць": [11,[3,5,6,10]],
"непрыярытэтных": [1],
"праверкі": [4,11,7,10,[1,2,9]],
"зьмены": [[6,11],[5,8],3,10],
"праграмнага": [11,5],
"пар": [11],
"файлавыя": [11,8,3],
"адыманьне": [2],
"састарэлыя": [9],
"пацьвердзеце": [5],
"праверку": [4,[5,8,11]],
"варыянтаў": [[9,11],[6,10]],
"паасобку": [11,3],
"абразка": [5],
"капіююцца": [[6,8,9,11]],
"машынныя": [11],
"файлавым": [[4,5,11]],
"праверка": [[4,8],3,[5,6],[2,11]],
"idx": [0],
"файлавых": [11,[6,8,10]],
"састарэлых": [11],
"фрагмэнта": [9],
"ўласьцівасьці": [10],
"запускае": [5],
"праграмныя": [11],
"ланцужкі": [11],
"projectaccesscurrentsourcedocumentmenuitem": [3],
"ніякай": [9],
"двухнапрамкавага": [6],
"linux": [5,[1,2,7,9]],
"праграмным": [5],
"менавіта": [[5,11]],
"няправільную": [1],
"працоўны": [5],
"сымбалі-запаўняльнікі": [6],
"разьдзяленьнем": [1],
"форме": [11,6],
"канкрэтны": [[4,11]],
"file.txt": [6],
"поўнымі": [6],
"правілы": [11],
"фрагмэнты": [9],
"рэжымах": [6],
"ifo": [0],
"comment": [5],
"левым": [6,9],
"раскамэнтаваць": [5],
"нядаўні": [[3,8]],
"xx.docx": [11],
"здымеце": [11],
"зыходнай": [[6,10,11]],
"формы": [11],
"тэгамі": [5],
"optionsautocompleteautotextmenuitem": [3],
"форму": [5],
"губляюцца": [6],
"элемэнта": [3],
"выпраўленьняў": [8],
"выкарыстанай": [6],
"зыходная": [6],
"зручных": [5],
"рэагаваць": [9],
"адкрыйце": [[5,8]],
"экспартуе": [6],
"галяндзкай": [6],
"зручным": [6],
"каманднай": [5],
"патрабуюцца": [[2,5,6,9]],
"concis": [0],
"яндэксе": [5],
"распакаваць": [0],
"customer-id": [5],
"экран": [11,5],
"зручныя": [11],
"мэгабайтах": [5],
"вялікімі": [11],
"term.tilde.com": [11],
"зачыняецца": [8],
"разам": [11,5,[6,9]],
"напр": [11],
"зрабеце": [10,[0,8,9]],
"залежыць": [8,[5,6,11]],
"выправіць": [11],
"лепшае": [6],
"сіні": [9],
"праход": [8],
"пацьверджаньня": [11],
"скончана": [9],
"viewmarknotedsegmentscheckboxmenuitem": [3],
"улікам": [[9,11]],
"зьмяшчае": [[1,5,6,10,11]],
"паасобна": [11],
"менск": [11],
"значных": [11],
"спусташаецца": [8],
"бачныя": [8],
"істотна": [11],
"уключайце": [11],
"праблемамі": [6],
"нумароў": [8],
"выкарыстоўваць": [6,5,[3,8],[4,9,11],[1,10]],
"атрымаецца": [6],
"запуск": [5,7,8],
"выкарыстаць": [[6,9,11]],
"раім": [11],
"gotomatchsourceseg": [3],
"abstract": [7],
"паказьнік": [9],
"optionssaveoptionsmenuitem": [3],
"excel": [11],
"comma": [1],
"адлучэцеся": [6],
"stardict": [0],
"omegat.l4j.ini": [5],
"спампаваўшы": [5],
"span": [11],
"спэцыяльнай": [9],
"блёкаў": [[2,7,11]],
"знойдзенага": [11],
"паказчыкаў": [10],
"зьніжаныя": [10],
"атрыбут": [11],
"націску": [[4,6,11]],
"пэўным": [[10,11]],
"вялікай": [11,4],
"разрыў": [11],
"экспарт": [6,1,11],
"вамі": [11],
"thunderbird": [4,11],
"editselectfuzzy3menuitem": [3],
"паміж": [11,6,[1,8],[2,9,10]],
"прычыне": [4],
"пэўных": [[6,11]],
"абодва": [11],
"fals": [[5,11]],
"паказчыкам": [9],
"верне": [11],
"project.projectfil": [11],
"выданьне": [0],
"упера-назад": [11],
"прычыны": [[1,5,8,11]],
"іншага": [9,[4,11]],
"тэкставую": [11],
"выклікаць": [[5,11]],
"пэўныя": [11,6,[5,8]],
"прычыну": [5],
"спраўдзіць": [[6,11]],
"галоўны": [11],
"po-файлаў": [11],
"пацьвярджэньне": [8],
"чый": [8],
"ўтрымліваюць": [11,8],
"аптычнага": [6],
"чым": [[6,10,11]],
"няправільных": [11],
"неразрыўнаму": [11],
"боку": [11],
"чатыры": [8],
"shortcut": [3],
"супастаўляецца": [6],
"канфігураваньне": [11],
"pt_br.aff": [4],
"tmx2sourc": [6],
"адбываўся": [11],
"ini": [5],
"глябальна": [11],
"вызначанага": [5],
"пацьвярджэньня": [11],
"справа-налева": [6,7],
"па-за": [11,8,5],
"колер": [[9,10]],
"працягнуць": [11],
"dhttp.proxyport": [5],
"пераходны": [5],
"вышэй": [[6,9,11],5,[2,4],[0,1,8,10]],
"замяняцца": [11],
"маюць": [11,[6,9],0],
"subrip": [5],
"ідэнтычнасьць": [6],
"носьбіты": [6],
"зачыніць": [11,[3,6,8]],
"дадае": [6],
"апрацоўкі": [11,[6,8]],
"score": [11],
"панэляў": [9,5,[6,7]],
"пакета": [5],
"пазнаюцца": [1],
"найвышэйшы": [9],
"паказчыка": [10],
"appendix": [[1,2,4],[0,3],6],
"прыклад": [[5,6],[0,1,7,11]],
"праекце": [11,9,[6,10],[3,8]],
"raw": [6],
"праектах": [11],
"спаганяецца": [5],
"прапановаў": [[4,9,10]],
"дазволы": [5],
"праектаў": [11,8,6,[5,7,9]],
"папулярных": [11],
"апрацоўку": [11],
"гатова": [11],
"aaa": [2],
"адмацаваньня": [11],
"contemporari": [0],
"solari": [5],
"адбываюцца": [6],
"імёны": [11,9],
"дапамогу": [6],
"спэцыялізаваных": [9],
"дадаў": [9],
"праз": [11,5,6,10,[8,9],[0,1,3,4]],
"ніякага": [9],
"екты": [11],
"мноства": [11],
"патрэбная": [11],
"пачынаюцца": [2],
"элемэнт": [2,3,[5,11]],
"апісанай": [5],
"патрэбнай": [6],
"abc": [2],
"rcs": [6],
"зьмененымі": [3],
"варта": [11,6,10],
"екта": [11],
"перавызначыць": [6],
"паводле": [11,[5,10]],
"непатрэбным": [11],
"патрэбнае": [11],
"пра": [11,[3,5,6,8],9],
"карыснай": [11],
"асяродак": [5],
"карыснае": [11],
"запоўнены": [11],
"разыходжаньняў": [6],
"абзацы": [11],
"рысы": [[2,5]],
"значнай": [4],
"наступнымі": [9],
"хаця": [11],
"glossary.txt": [[1,6]],
"пры": [11,6,5,8,9,[1,10],4,2],
"карысная": [[6,11]],
"любыя": [1],
"add": [[5,6]],
"устаўкай": [10],
"выдзяляцца": [5],
"працэнт": [11],
"паказваліся": [11,8],
"выдалены": [11],
"выдзяляецца": [5],
"зьвестак": [11,[5,8]],
"складаныя": [2],
"optionsautocompleteshowautomaticallyitem": [3],
"змаўчаньні": [11,3,[6,8],9,[5,10],[1,2]],
"ўплывае": [[6,11]],
"бягучае": [8],
"larouss": [9],
"untar": [0],
"курсівам": [11],
"уліку": [9],
"калі": [11,8,6,5,9,10,4,1,3,0,2],
"дужках": [11],
"ёсьць": [11,[1,5],[4,6,8],0],
"апрацоўка": [11,3],
"падпункты": [11],
"якой-небудзь": [[4,6]],
"filters.conf": [5],
"выключае": [6],
"каля": [11],
"іншымі": [6,8],
"аднаму": [11],
"інтэрнэт-старонак": [11],
"навейшай": [[5,8]],
"субтытры": [5],
"створаны": [6,[5,8],1],
"ўліковы": [5],
"выпадках": [6,11],
"ўведзена": [11],
"ўжываецца": [11,[5,8],[4,9,10]],
"паказьнікаў": [9],
"сродак": [11,7,2],
"выпадкаў": [11,6],
"выбранае": [8],
"разьдзелаў": [6],
"спрычыніцца": [11],
"найперш": [11],
"clone": [6],
"перазагрузеце": [11],
"targetlanguag": [11],
"карыстальнік": [[5,11],[8,9]],
"шматнацыянальнае": [6],
"бягучай": [11,[5,10]],
"падкаталёгамі": [10],
"properti": [5],
"дадуць": [11],
"мэтаў": [11,6],
"editselectfuzzyprevmenuitem": [3],
"выключаныя": [6],
"адвольнай": [11],
"мэтах": [11,[5,8]],
"выканаецца": [11],
"simpledateformat": [11],
"паспрабуйце": [11],
"кітайскую": [6],
"мэтад": [[5,11]],
"адсутнічае": [9],
"script": [11],
"базамі": [[1,6]],
"прэфікс": [11,10],
"адрас": [4],
"пасярэдніка": [6],
"system": [11],
"крытэр": [11],
"табліцамі": [1],
"spellcheck": [4],
"выпуску": [8],
"перацягнутыя": [9],
"жорсткі": [8],
"ўважацца": [11],
"утрымліваецца": [11],
"зьмененыя": [8],
"вышэйзгаданых": [10],
"выраўнованьня": [8],
"дыялёг": [11,8,6,1],
"падставе": [[4,11]],
"шукаліся": [11],
"камбінацыю": [0],
"любым": [10,11],
"local": [6,5],
"староньніх": [11],
"вылучаюцца": [11,[1,8]],
"паўторнае": [6,7],
"камбінацыі": [[5,11]],
"абнаўляюцца": [5],
"столькі": [11],
"вызначэньняў": [6,3],
"адмысловыя": [6,5],
"выраўнованьне": [6],
"пошуках": [2,11],
"прадстаўленьнем": [5],
"немагчымасьці": [11],
"шрыфтоў": [8],
"трох": [[6,10],9],
"зрабіўшы": [5],
"літару": [8],
"es_mx.aff": [4],
"рэжыму": [5,[6,9]],
"спалучэньне": [3,8],
"зыходнага": [11,[3,8,9],6],
"напісанага": [4],
"увядзеце": [5,11],
"вашым": [5,11],
"вызначэньнях": [6],
"калекцый": [11],
"mode": [5],
"розныя": [11,[8,9]],
"зьмяніўшы": [11],
"пошукаў": [11,2],
"ўкладкі": [[9,11]],
"поўны": [11,[3,5,6]],
"toolsshowstatisticsstandardmenuitem": [3],
"падрахаваны": [[9,11]],
"адсутнічаць": [1],
"літары": [11,8],
"тарбол": [0],
"створыцца": [5],
"ўстаўлены": [8,1],
"дазволіць": [11,[5,8]],
"read": [11],
"электроннымі": [1],
"праекта": [6,11,[8,10],5,3,9,[1,4],7,0],
"alt": [[3,5,11]],
"клявішаў-мадыфікатараў": [3],
"аўтарскае": [8],
"любую": [10],
"зьменнай": [11],
"выдалеце": [[9,10,11]],
"літара": [2,6],
"прагортваць": [9],
"рэжыме": [5,6,[9,11]],
"старту": [5],
"праекты": [6],
"вашыя": [5],
"запыту": [5],
"назад": [8,6,3],
"гэты": [11,5,10,[6,8],[1,2,7,9]],
"захаваная": [11],
"эфэкт": [11],
"апэрацыі": [6],
"няправільна": [[4,11]],
"запыце": [8],
"паданай": [11],
"пазнака": [11],
"проксі-сэрвэр": [11],
"стварэньня": [[1,11],5],
"сцэнар": [11,5,8],
"выкарыстоўвайце": [6],
"гэта": [11,6,5,1,[4,9],2,[0,8],10],
"стварэньні": [6,[1,11]],
"and": [[5,6,11]],
"заўгодна": [[2,11],[8,10],6,[1,3,9]],
"меркаваньне": [9],
"пераважным": [[10,11]],
"захаванай": [10],
"пазнаку": [11,[4,5]],
"калекцыі": [11],
"назву": [11,[1,3,6,9]],
"розных": [11,[5,6,8]],
"ant": [[6,11]],
"поўна": [11],
"спалучэньні": [3,[9,11]],
"аўтар": [[8,9,11]],
"назвы": [11,[1,4],5,[0,6,9]],
"спалучэньня": [3],
"распазнаваньня": [[6,11]],
"розным": [11],
"назва": [11,6,[1,5,8]],
"генэрацыі": [11],
"залежнасьці": [[5,9,11],[6,8]],
"абагульваньне": [6],
"helplastchangesmenuitem": [3],
"састарэлая": [6],
"omegat.ex": [5],
"уверсе": [11],
"патрэбную": [[0,11]],
"карысны": [5],
"ўласнага": [9],
"sourcetext": [11],
"выдаленыя": [[6,11]],
"ліка": [9],
"зьнікнуць": [4],
"вылучанае": [[8,9],[3,11]],
"пачынаўся": [11],
"маеце": [[4,5]],
"выбраўшы": [9,5],
"схемы": [11],
"абзац": [11],
"english": [0],
"прабелаў": [[2,11]],
"выконвае": [11,[6,8]],
"jar": [5,6],
"api": [5,11],
"зашмат": [11],
"сродкі": [1],
"editselectfuzzy2menuitem": [3],
"зьменаў": [11,10,[1,5]],
"курсор": [11,[8,9]],
"сродка": [11],
"спытаць": [9],
"унесьці": [[5,8]],
"можна": [11,5,6,9,8,4,1,10,[2,3],0],
"карысную": [5],
"павялічыць": [11],
"першапачатковага": [5],
"рознае": [11],
"непасрэдна": [5,[8,11],10],
"ўяўленьне": [11],
"складаецца": [9],
"зьменах": [9],
"ўжываюцца": [11,8,10,[2,6]],
"супадзеньнях": [11],
"увод": [6],
"складацца": [1],
"магчыма": [[1,2,6]],
"editselectfuzzynextmenuitem": [3],
"read.m": [11],
"аргумэнты": [5],
"раз": [[2,6,11],[8,10]],
"readme.bak": [6],
"сэгмэнтаў": [11,8,9,6,[3,5,10]],
"інвэрсіяй": [11],
"даволі": [1],
"супадзеньняў": [8,11,9,10,6,3,7],
"згадвалася": [5],
"сэгмэнтах": [11,6],
"зьвесткі": [11,10,[2,6,9]],
"art": [4],
"пажадана": [11,[1,6]],
"вылучаная": [8],
"rtl": [6],
"стварыць": [6,11,8,[3,5],[1,7,9]],
"прымаць": [10],
"jdk": [5],
"сабраных": [9],
"карысна": [11,[5,6,9,10]],
"сэгмэнтам": [11],
"апісанымі": [11],
"toolsshowstatisticsmatchesperfilemenuitem": [3],
"ўнутрытэкставыя": [11],
"run": [5],
"двума": [[9,11]],
"правым": [9,6],
"устаўляць": [11],
"што": [11,5,6,10,4,1,[0,8],[2,3]],
"нашым": [6],
"ягоны": [1],
"рэдактары": [[1,5]],
"titlecasemenuitem": [3],
"апроч": [2],
"editcreateglossaryentrymenuitem": [3],
"машыннага": [8,11,9],
"выкл": [9],
"дыялёгавага": [11],
"запрацавалі": [11],
"нефарматаванага": [6,1],
"дыяграмы": [11],
"name": [5],
"распазнаныя": [11],
"пераўтварацца": [11],
"тады": [[6,11]],
"аргумэнта": [5],
"бачны": [[9,11]],
"прамінуты": [11],
"экране": [[5,8,11]],
"ужывайце": [[5,11],6],
"падтрымліваюць": [6],
"show": [5],
"аднолькавымі": [11],
"выдатна": [6],
"лёгка": [6],
"правяраць": [11,6],
"канчатковыя": [11],
"хоста": [11],
"пасьлядоўнасьці": [2,11],
"поўная": [11],
"таго": [[9,11],6,[5,8]],
"разьбіць": [11],
"target": [[8,11],10,7],
"адпавядаць": [11,4],
"азнаямленьня": [5],
"кадоўцы": [1,11],
"дадатак": [[0,1,8]],
"аддзяляецца": [1],
"збору": [[6,9]],
"config-dir": [5],
"пасьлядоўнасьць": [11],
"знойдзецца": [2],
"паказчык": [10],
"табліца": [[3,8,11]],
"счытваюцца": [10],
"ўкладцы": [8],
"якую-небудзь": [5],
"пазьней": [[5,11],[6,9,10]],
"найлепшы": [11],
"выключаць": [8],
"адчыняюцца": [[8,11]],
"патрэбных": [4],
"выдзяленьнем": [5],
"асяродзьдзя": [5],
"табліцу": [11],
"адбываецца": [8,[4,6,9]],
"патрэбныя": [6,[4,5,8,11]],
"item": [5],
"магчымасьцяў": [6],
"гіерархіяй": [10],
"карысныя": [2],
"патрэбным": [5],
"проксі-сэрвэра": [5,11],
"кіроўнай": [2],
"лікаў": [[9,11]],
"сябе": [[5,11]],
"targettext": [11],
"merriam-webst": [[0,7]],
"абраная": [5],
"фарматаванага": [6],
"таас": [11],
"абранай": [[6,11]],
"файлах": [11,6,10,8,[1,3,9]],
"aaabbb": [2],
"інструмэнты": [[8,11]],
"старых": [11],
"старыя": [11],
"карысных": [2],
"файлаў": [11,6,5,[4,10],8,[1,9],[3,7]],
"edittagpaintermenuitem": [3],
"зьлева-направа": [6],
"бягучую": [9],
"зыходнаму": [[8,11]],
"рэальнага": [9],
"кнопак": [11],
"пэўнай": [11],
"optionscolorsselectionmenuitem": [3],
"мовамі": [6],
"табліцы": [11],
"выдаляць": [[10,11],6],
"слупках": [11],
"пэўная": [8,11],
"сьцягі": [[2,7]],
"інструмэнта": [2],
"зялёны": [9],
"unicod": [[2,7]],
"viewmarknbspcheckboxmenuitem": [3],
"справа": [[5,8,11]],
"файлам": [6,[5,8]],
"такой": [5],
"знакаў": [11],
"прыблізнае": [11],
"запамогай": [10],
"запрашэньне": [6],
"бясплатны": [5],
"запісвацца": [11],
"выкарыстоўваецца": [[3,6,8,11]],
"знакі": [11,[6,9]],
"ўсім": [11],
"назначаецца": [11],
"ёму": [5],
"знайдзеце": [5],
"папярэдне": [[5,8,11]],
"знакам": [1],
"msgstr": [11],
"незанятым": [8],
"ёме": [11],
"рэдагаваньне": [[9,11]],
"дадайце": [[3,5,11]],
"ўсіх": [11,8,6,[5,10],3],
"патрэбны": [5,[1,8,9]],
"графічным": [5],
"бягучыя": [[8,10]],
"кіраўніка": [6],
"правапісу": [4,7,11,10,[1,2,3,8]],
"табуляцыяй": [1],
"адключайце": [11],
"знаку": [1],
"omegat.project": [6,5,10,[7,9]],
"бягучым": [[1,9,11]],
"excludedfold": [6],
"памер": [9],
"апісаную": [2],
"пашанцавала": [11],
"лякальнага": [6],
"супаставіць": [11],
"агрэгаваньне": [11],
"аднаго": [[3,11]],
"адпавядаюць": [2,[9,11]],
"любяць": [11],
"webstart": [5],
"адлюстроўваць": [10],
"першакляснымі": [11],
"праектам": [11,6,8],
"палёх": [[6,8,11]],
"заўсёды": [11,[1,3,8]],
"даданьнем": [6],
"лякалізаваная": [5],
"утрымлівае": [10,6,[5,11]],
"недатыкальнасьці": [10],
"перамяшчаць": [9],
"двухнапрамкавы": [6],
"папярэдняга": [[6,8]],
"сартуюцца": [10],
"дыстрыбутыў": [5],
"папярэдні": [[3,6,8,11]],
"рэдагаваньня": [11,9,8,[5,6],[3,10],[1,7]],
"yandex": [5],
"кітайскай": [[5,11]],
"ухвалены": [1],
"палёў": [5],
"аналізуюцца": [6],
"a123456789b123456789c123456789d12345678": [5],
"viewmarkwhitespacecheckboxmenuitem": [3],
"жорсткага": [5],
"інструкцыяў": [11],
"захаваныя": [5],
"сумесны": [6],
"коску": [1],
"залежаць": [6],
"найлягчэйшым": [6],
"bak": [[6,10]],
"аснове": [11,[1,4,8]],
"камэнтар": [1,8],
"запускальнікам": [5],
"рашэньне": [[6,10,11]],
"bat": [5],
"дакумэнтах": [11],
"ўвогуле": [11],
"дапаможніка": [5],
"jre": [5],
"ўпэўнены": [11],
"optionsfontselectionmenuitem": [3],
"тэрмінаў": [[1,8,9,11],3],
"url-адраса": [6],
"рабіць": [[6,11],[4,8]],
"дакумэнтаў": [6,[8,9,11]],
"прыведзеным": [9,[1,6]],
"спампуйце": [5,0],
"правіламі": [[4,5,11]],
"ўсталяваць": [0,[4,7]],
"падказках": [1],
"шэрагу": [11,2],
"падабенства": [[9,10],11],
"інфармацыі": [5,[0,8,11]],
"зьнешняя": [11],
"freebsd": [2],
"вашай": [5],
"ігнаравацца": [11,3],
"запісваць": [[8,11]],
"delet": [11],
"выдаляецца": [8],
"паведамленьняў": [6],
"эфэкту": [9],
"кадоўка": [11,1],
"projectaccessglossarymenuitem": [3],
"хвілін": [6],
"правяраюцца": [1],
"зьмяніць": [11,[3,5,6],8,9,1],
"тоесны": [11],
"прабельныя": [11,8,[1,3]],
"экспартуецца": [[8,11]],
"інфармацыю": [6,5,1],
"інфармацыя": [[3,6],[5,8]],
"прабельных": [11],
"developerwork": [5],
"этап": [6,11],
"кадоўкі": [11],
"зьнешнюю": [11],
"contain": [1],
"set": [5],
"дапаможніку": [6],
"каталёгаў": [8,[6,11]],
"нуль": [2],
"раней": [8,[6,11]],
"адчыняецца": [8,11],
"выконваецца": [8],
"optionsrestoreguimenuitem": [3],
"дапаможнікі": [6,[0,7,10]],
"спэцыфічныя": [10,[2,6,11]],
"адкрываецца": [8,4],
"падрабязныя": [[5,11]],
"клапаціцца": [6],
"гандлёвых": [11],
"гандлёвыя": [11],
"кадоўку": [11,1],
"зьніжаецца": [10],
"спампаваным": [5],
"карані": [11],
"terminolog": [1],
"кансолі": [5],
"offic": [11],
"адрозьніваюцца": [5,11],
"устаўляюцца": [8],
"зважайце": [5],
"праца": [[5,6]],
"ачысьціўшы": [11],
"якасныя": [10],
"цэтлік": [5],
"projectsavemenuitem": [3],
"статыстыцы": [11],
"xmx6g": [5],
"абмяжоўвае": [11],
"абсалютна": [11],
"працы": [6,5,11,9,[1,4,10]],
"задаць": [11,[5,8],[1,3]],
"альтэрнатыўныя": [8,[9,11]],
"апісаныя": [5],
"пачатага": [2],
"перакладзе": [11,6,[8,9],1],
"таксама": [11,5,6,9,[1,3,4,8],7],
"экранаваць": [5],
"альтэрнатыўным": [8],
"сэансамі": [11],
"падкрэсьленым": [4],
"працу": [6,11],
"альтэрнатыўных": [9],
"непатрэбнае": [11],
"усталёўкі": [5,7],
"літара-скарот": [8],
"ектнай": [11],
"існыя": [6,11,10],
"адзінкі": [[10,11]],
"існых": [11],
"зыходзячы": [4],
"рэсурсаў": [11],
"заставацца": [11],
"радок": [11,8,[5,6],[3,9,10]],
"найхутчэй": [11],
"зваротнай": [[2,5]],
"усталёўка": [5,4,7],
"ўстаўляць": [11],
"прыярытэту": [11],
"адпаведнікі": [[2,11]],
"коска": [2,1],
"пачынацца": [[3,11]],
"ідэнтыфікацыі": [11],
"мэксыканскай": [4],
"новага": [11,6,2,[5,8]],
"кантэкст": [11,9],
"робіцца": [11,6,[8,9]],
"выключэньні": [[6,11]],
"шляху": [6],
"кіраўніцтвах": [6],
"карэкцыя": [11],
"bis": [2],
"сьцяжок": [11],
"хатнім": [5],
"ліцэнзіі": [6],
"далей": [11,8,[3,5]],
"зьнешніх": [[6,11]],
"любы": [[9,10,11]],
"зьменены": [8,[5,6]],
"звычайнага": [[5,10]],
"projectopenmenuitem": [3],
"autom": [5],
"зробленага": [8],
"прагныя": [2,7],
"ліцэнзію": [8],
"зьнешнія": [[3,11]],
"зацемках": [11],
"прывязаць": [8],
"абнавіць": [[5,11]],
"toolsvalidatetagsmenuitem": [3],
"паданыя": [11],
"апрацаваць": [6],
"рэалізацыям": [5],
"праблемаў": [8,6,[1,5,11]],
"пакідаеце": [11],
"выбіраць": [11,[5,6]],
"альтэрнатывы": [11],
"паданых": [3],
"мэксыканская": [4],
"шляхі": [5],
"viewmarktranslatedsegmentscheckboxmenuitem": [3],
"ўсталяваны": [5],
"абнаўленьня": [11],
"ilia": [5],
"зьменных": [11],
"абнаўленьні": [11,5],
"апорнага": [10],
"адно": [11,5],
"прызначаецца": [11],
"зьменныя": [11],
"карэйскай": [11],
"радку": [[3,5,6,8,11]],
"сказана": [6],
"прызначэньні": [11],
"pojavni": [1],
"выгружаецца": [8],
"адну": [9],
"адна": [[1,2,6]],
"задачаў": [5],
"актуальны": [6],
"editselectfuzzy1menuitem": [3],
"устаўленыя": [8],
"наяўнасьці": [11,5,8],
"выключэньне": [11],
"выключэньня": [11],
"напрамкамі": [6],
"hide": [11],
"унутраны": [11],
"перанесеныя": [11],
"нязьменнасьць": [11],
"вызначце": [5],
"ўставіць": [9,11],
"радкі": [6,3,[1,11]],
"зробіць": [5],
"запаволіць": [6],
"наяўнасьць": [[6,8],[1,5,11]],
"ужываньнем": [11],
"гэтага": [11,5,8,1,3,[6,9,10]],
"auto": [10,[6,8],11],
"таварныя": [9],
"змяняюцца": [11],
"перавызначаць": [11],
"выбраных": [6],
"каталёгам": [6],
"наяўныя": [11],
"notepad": [1],
"шаблёнаў": [11,6],
"document.xx.docx": [11],
"любога": [[9,11]],
"выбраным": [11],
"каталёгах": [[5,10]],
"выбраныя": [11,8],
"адкампіляваць": [[3,8,10,11]],
"oracl": [5,3,11],
"канечныя": [11],
"адзіная": [2],
"gradlew": [5],
"радка": [5,2,[7,8,11]],
"паспрабуе": [11,6],
"касаваньнем": [8],
"слоўнікамі": [0,7],
"неразрыўныя": [8,3],
"падвысіць": [11],
"праграмаў": [[4,11],[1,5,6]],
"вынікамі": [11],
"прачытайце": [6],
"glossary.tbt": [1],
"тэрмінал": [5],
"межамі": [5],
"глясар": [[1,3,9,11],[7,8]],
"таму": [11,[1,6],5],
"косай": [[2,5]],
"каманднага": [5,6,[7,10]],
"switch": [11],
"праграмах": [11,[4,5,6]],
"ftp-сэрвэр": [11],
"кастыльскай": [4],
"адключае": [5],
"пераключыцца": [6],
"існуе": [11,6],
"фрагмэньце": [9],
"праграмай": [[1,4,6]],
"ідэнтыфікацыйныя": [11],
"src": [6],
"знойдзеных": [9],
"control": [3],
"лікі": [11,[6,9]],
"апэрацыйных": [10],
"сьцяжкі": [11],
"no-team": [[5,6]],
"знойдзеныя": [[1,9,11]],
"падабраць": [11],
"сынхранізавала": [5],
"сур\'ёзна": [11],
"такі": [[1,11]],
"шаблёнах": [11],
"ліку": [[1,5,6,8,10,11]],
"уключэньне": [11],
"mojprajekt": [6],
"адсутных": [8],
"рэпазыторыя": [6,[5,11]],
"ўводзьце": [9],
"абраным": [11],
"рэпазыторыі": [6],
"optionsautocompleteglossarymenuitem": [3],
"апрацаваны": [5],
"гледзішча": [[9,11]],
"рэпазыторый": [8,6],
"ўбудаваную": [[4,11]],
"зборка": [5,7],
"прынамсі": [[10,11]],
"памаранчавым": [8],
"агульнага": [9],
"рэалізацыя": [5],
"найвышэйшага": [10],
"крос-плятформенных": [5],
"наступнаму": [2],
"kde": [5],
"пазначаны": [[4,5,6]],
"выбралі": [11],
"досьведу": [6],
"адзначаць": [11],
"глясара": [1,11,3,8,9,6],
"ўдасканаленьня": [8],
"вылучэце": [11],
"кожнага": [11,8,6],
"могуць": [11,6,10,[1,5,8],[0,9]],
"перавышаны": [11],
"sub": [1],
"ўнікальныя": [9],
"сыстэмы": [5,11,[7,8]],
"languag": [5],
"неабходнымі": [5],
"рознымі": [6,[10,11]],
"або": [11,6,5,2,[1,9],[3,8],4,0,10],
"зваротную": [9],
"глясары": [1,11,[7,10],[0,4,6]],
"інтэрнэтам": [5],
"правай": [9,[5,11],1,4],
"дата": [11],
"пазначана": [11,8],
"афіцыйным": [7],
"апісаньня": [3],
"месцазнаходжаньні": [8],
"нейкую": [11],
"сыстэму": [6,11,5],
"key": [[5,11]],
"даведка": [[3,7],8],
"месцазнаходжаньня": [5],
"пары": [[6,11]],
"сыстэма": [[1,5,8,11]],
"svg": [5],
"сыстэме": [5,4,8],
"svn": [6],
"рэдкіх": [11],
"адсканаваных": [6],
"ўнікальным": [9],
"устаўляецца": [8],
"профілю": [5],
"цьвёрдым": [6],
"выдаліўшы": [9],
"месцазнаходжаньне": [5,[1,6,8,11]],
"вокнамі": [11],
"ўнікальных": [11],
"editreplaceinprojectmenuitem": [3],
"пара": [11,9],
"патрабавацца": [[6,11]],
"крыніцай": [[9,11]],
"ставіць": [11],
"зьявяцца": [11,4],
"express": [[2,11]],
"словамі": [[2,6,11]],
"нумара": [8],
"знойдзенае": [9],
"зархіваваць": [6],
"рэпазыторый_каманднага_праекта_omegat": [6],
"перакладчыкаў": [6],
"адключана": [11],
"спытае": [5],
"выконваюцца": [[5,9,11]],
"выканайце": [[4,11]],
"gotoprevioussegmentmenuitem": [3],
"патрэбнымі": [5],
"нумары": [8],
"аналізуецца": [2],
"аддаленым": [[6,10]],
"крыніцаў": [11],
"скончыць": [11],
"дазволены": [1,3],
"мусяць": [[3,5,11]],
"gotopreviousnotemenuitem": [3],
"апэрацыйная": [[5,8,11]],
"падкаталёга": [10],
"editredomenuitem": [3],
"адлюстраваньня": [6],
"uilayout.xml": [10],
"апісаны": [5],
"адключаны": [8],
"вылучаным": [9],
"сінім": [11],
"бегчы": [11],
"апэрацыйнай": [5,11,8],
"пачынаючы": [6],
"насамрэч": [6],
"другі": [[1,6,11]],
"пацьверджаны": [8],
"затым": [11,5,[3,4,6],[1,2,8]],
"пройдзе": [5],
"выбраны": [8,11],
"пунктаў": [[3,9]],
"замяніць": [11,[3,8,9]],
"адзіным": [11],
"запавольнае": [11],
"аддаленыя": [10],
"запускаецца": [5],
"адзіныя": [11],
"устаўкі": [11],
"канструктыўны": [2],
"зьменіцца": [10,11],
"падкаталёгу": [6,[1,10,11],0],
"адрозьненьняў": [11],
"абраную": [5],
"адрозьненьнях": [11],
"задаяце": [11],
"падкаталёгі": [11],
"палі": [11],
"зьмесьцівам": [5],
"любой": [[5,9]],
"атрымаеце": [5],
"runtim": [5],
"спалучэньняў": [3,7,[1,2,8]],
"асаблівыя": [11],
"пераклад": [11,8,9,[3,6],1,5,10,[0,7]],
"tester": [2],
"пазначаць": [8,3,11],
"дзьвюма": [6],
"фіксаванай": [11],
"кантэкстнага": [11],
"тэхнічны": [11],
"блакітным": [8],
"пераходзіць": [11],
"filenam": [11],
"выдаецца": [5],
"спалучэньнях": [3],
"імпартаваных": [9],
"ўжываньне": [1],
"тэкстам": [[1,10,11]],
"рэпазыторый_усіх_зыходных_файлаў_камандных_праектаў_omegat": [6],
"gotosegmentmenuitem": [3],
"працэсу": [9],
"хутчэйшым": [1],
"асаблівых": [11],
"абноўленым": [11],
"утрымліваць": [11,5,[9,10]],
"працэсе": [11,10],
"xx_yy.tmx": [6],
"тэкстаў": [[5,11],[6,7]],
"наладамі": [6],
"загрузеце": [11],
"helpaboutmenuitem": [3],
"чакаць": [5],
"загаловак": [11,[8,9]],
"чытаць": [6],
"атрыбуту": [11],
"дастатковымі": [11],
"вылучыўшы": [11],
"тэкстах": [11],
"усімі": [5,11],
"атрыбуты": [11],
"regular": [2],
"існай": [5],
"git-кліента": [5],
"доўгія": [11],
"скапіяваў": [6],
"ўжываньню": [11],
"нябачнае": [11],
"ўжываньня": [2,[7,10,11]],
"перакладчыкам": [6],
"ўжываньні": [11],
"доўгіх": [11],
"бясьпекі": [5],
"існая": [5],
"вынік": [11],
"выдаляюцца": [11,4],
"вылучацца": [8,11],
"tab": [[1,3],[8,11],9],
"taa": [11,8],
"формаў": [11],
"шаснаццатковым": [2],
"бясьпеку": [11],
"пакеце": [5],
"але": [6,11,9,[1,2,5],[4,8,10]],
"памятайце": [11,6,[4,5]],
"ўтрымоўвае": [0],
"каранёвага": [6,3],
"рухаецца": [11],
"tar": [5],
"кропкай": [[2,6,11]],
"апісаць": [11],
"зборцы": [5],
"зьняць": [11],
"разумееце": [11],
"захоўвайце": [10],
"projectreloadmenuitem": [3],
"машыны": [11],
"будзе": [11,5,6,8,9,[1,10],4,2,3],
"перакладанага": [[8,11]],
"safe": [11],
"targetcoutrycod": [11],
"формах": [[10,11]],
"апісаньне": [11,[3,6]],
"правая": [11],
"html-тэг": [11],
"аўтаматызацыі": [[5,7,10,11]],
"выраўнаваныя": [11],
"winrar": [0],
"tbx": [1,11,3],
"разглядацца": [9],
"пустыя": [11,3],
"фарматах": [[8,11],5],
"няслова": [2],
"duser.countri": [5],
"tcl": [11],
"tck": [11],
"пазначанымі": [5],
"readm": [5,11],
"абраныя": [4,11],
"пустым": [11,9],
"свабоднага": [11],
"пустых": [11],
"пераключыць": [[6,11]],
"падлягаючыя": [11],
"табуляцыя": [11],
"align.tmx": [5],
"навігацыю": [11],
"табуляцыі": [1,[2,11]],
"file2": [6],
"атрыманьня": [[10,11]],
"адсутныя": [8,3],
"ўсяго": [[5,6]],
"выбраць": [11,3,5,[8,9],4,[6,10]],
"навігацыі": [5],
"фарматаў": [11,[5,9]]
};
