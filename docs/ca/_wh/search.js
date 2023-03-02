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
 "chapter.appendices.html",
 "chapter.dialogs.preferences.html",
 "chapter.how.to.html",
 "chapter.instant.start.guide.html",
 "chapter.menus.html",
 "chapter.panes.html",
 "chapter.project.folder.html",
 "chapter.windows.and.dialogs.html",
 "index.html"
];
wh.search_titleList = [
 "Annexos",
 "Preferències",
 "Guies",
 "Introducció a l&#39;OmegaT",
 "Menús",
 "Subfinestres",
 "Carpeta del projecte",
 "Finestres i quadres de diàleg",
 "OmegaT 5.8.0 - Manual d&#39;usuari"
];
wh.search_wordMap= {
"cancel": [4,[0,7]],
"descripció": [4,[0,7],1],
"n\'hi": [1,2,[5,7]],
"predefinid": [[0,2,3]],
"d\'intentar": [2],
"característica": [1,[2,5]],
"lingüístic": [1,[0,4]],
"predefinit": [1],
"ressaltad": [7],
"llarga": [[0,1]],
"info.plist": [2],
"gestió": [3,8,2,4],
"processador": [7,3],
"algorism": [7,4],
"ressaltar": [7,5],
"coincidiran": [7,0],
"taronja": [7],
"produir": [[0,2]],
"ressaltat": [[0,7]],
"llicènci": [[2,4]],
"pestanya": [5,1],
"click": [3,7,5],
"fuzzi": [1,5],
"size": [1],
"left": [0,5],
"cantem": [[0,7]],
"mostrar": [1,7,5,2,6,[0,3,4]],
"s\'invoca": [7],
"reflectiran": [6],
"object": [7,2],
"preferènci": [4,0,7,5,[1,2],6,3],
"algoritm": [7],
"llevat": [2,1],
"cel": [7,0],
"combinació": [0,[1,4]],
"especialitat": [2],
"result": [2,[3,4,7]],
"considerar-s": [[0,7]],
"edittagnextmissedmenuitem": [0],
"necessària": [2],
"same": [2,1],
"modificar": [0,7,2,3,[1,4,5]],
"substituït": [0],
"substituïu": [2,0],
"after": [2],
"quiet": [2],
"modificat": [[0,1,7],4],
"salt": [0,7],
"llegeix-m": [0],
"modificacion": [[0,2,4],1,3,[5,6,7]],
"gestor": [4,2,[6,7]],
"modificad": [0,7],
"the": [3,[1,2,7],5,4,0,6,8],
"preparar": [0],
"projectimportmenuitem": [0],
"obvious": [2],
"imag": [0],
"relacionat": [6,3],
"intermèdia": [2],
"ratolí": [7,5,[1,3,4]],
"sortir-n": [1],
"generaran": [4],
"laboratius": [2],
"monolingü": [[0,7]],
"modificadores": [8],
"d\'estil": [0],
"good": [8],
"blocatg": [5,0],
"omegat.project.lock": [2],
"convertint": [2],
"moodlephp": [2],
"currsegment.getsrctext": [7],
"uncheck": [0],
"s\'afegiran": [6],
"practic": [7],
"reduc": [7],
"check": [4],
"enllaço": [[0,3,5],2],
"s\'utilitzen": [0,[1,2,6,7]],
"plataforma": [[0,2]],
"gotonotespanelmenuitem": [0],
"fr-fr": [3,1],
"conservar": [0,2],
"ascend": [0],
"personalitzada": [2],
"lent": [0],
"varia": [5,0],
"l\'assignació": [2,[3,7]],
"darrer": [0,7,[4,5],[3,6]],
"termini": [3],
"n\'heu": [[3,7]],
"descansar": [3],
"lematitzador": [[1,2,7],6],
"cjk": [7,0],
"validar": [4,1],
"buida": [2,6,[4,7],[0,1,3]],
"duplicat": [[0,5,7]],
"translation": [8],
"well": [2],
"duplicad": [0],
"obert": [7,[0,2,5]],
"cíclicament": [4],
"empti": [4,[2,5]],
"valida": [0],
"s\'havia": [2],
"blocs": [8],
"execució": [2,[0,3,4,8]],
"longitud": [0],
"variabl": [1,0,7],
"actualitza": [6],
"tmx": [2,7,6,1,[3,5]],
"s\'indiquen": [4,[0,2]],
"menús": [8],
"cli": [2],
"s\'estan": [7],
"application_startup": [7],
"actuen": [0],
"eventtyp": [7],
"proposa": [7],
"alineacion": [0],
"l\'atribut": [0],
"fr-ca": [1],
"clonat": [2],
"tanca": [7,0,4,2],
"blocar": [5],
"mainmenushortcuts.properti": [0],
"blocat": [5,2],
"anar": [5,[2,6]],
"convertiu": [[2,3]],
"convertit": [7],
"convertir": [2],
"inhabilitar-lo": [0],
"conserveu": [0],
"gotohistorybackmenuitem": [0],
"conserven": [2,1],
"save": [7],
"ometr": [6],
"lluna": [0],
"esquerr": [7],
"recursiva": [7],
"top": [[3,5]],
"podreu": [7,0],
"quina": [0,7],
"tot": [7,2,0,1,3,[4,6],5],
"have": [[0,2,4],[5,7]],
"powerpc": [2],
"avail": [1,[0,3]],
"product": [3],
"quan": [7,2,0,1,3,5,6,4],
"maximitzada": [5],
"comparació": [[3,7]],
"editselectsourcemenuitem": [0],
"substituirà": [1],
"l\'autor": [3],
"eina": [[0,3,4,7]],
"l\'autom": [2],
"romanen": [7],
"qual": [[0,2],[1,7],[3,4,5]],
"com": [0,[2,7],1,6,[4,5],3,8],
"col": [0,[1,3,4,7],2],
"instal": [2,1,3,0,4,[5,6,7,8]],
"propagada": [1],
"núvol": [2],
"diferènci": [1,[3,7]],
"ordena": [1],
"remot": [2,6,4,[5,7]],
"modificar-l": [0,7,3],
"cantaires-dansair": [0],
"modificar-n": [7],
"function": [7,0,[1,2,4]],
"innecessària": [1],
"lar": [2,0,1,6],
"separador": [[0,5]],
"lat": [1,[0,2,4],[3,5,7]],
"mínim": [6,1,[0,2,8]],
"afegit": [[0,2,4,7]],
"afegiu": [0,3,[2,6,7]],
"afegir": [2,0,3,6,[1,5,7],4],
"avui": [0],
"avisi": [3],
"maximitza": [5],
"traduïd": [[1,7]],
"changeid": [1],
"translat": [0,2,[3,5,7],[1,4]],
"aneu": [[2,3,5,7]],
"université": [1],
"traduït": [7,0,3,2,4,5,6,1],
"traduïu": [1,3,[5,6]],
"punter": [5],
"aviso": [2],
"uneix": [7],
"l\'anglè": [2],
"l\'ordinador": [[1,2],[0,3,4]],
"segmentació": [7,0,1,4,[2,3],[6,8]],
"ordenar-lo": [0],
"podria": [6],
"correcta": [2,0,[1,4,7],[3,5]],
"actualitzar": [7,2],
"docs_devel": [2],
"correccion": [7,[0,2,4]],
"tsv": [0],
"d\'unicode": [8],
"flux": [3,5,[0,8]],
"navegació": [3,[4,5],6,2],
"redueixen": [6],
"gnome": [1],
"monoespaiada": [1],
"s\'actualitzi": [7],
"endavant-endarrer": [7],
"tornar-hi": [6],
"s\'actualitza": [6],
"doctor": [0],
"lingüístiqu": [1],
"jerarquia": [6,2],
"significat": [0,[5,7]],
"requereixen": [0,[1,2,7]],
"versió": [2,0,4,3],
"resolució": [2,[3,6,8]],
"appdata": [0],
"csv": [0,2],
"concert": [0],
"directa": [2,4,[0,6],[3,7]],
"requereix": [2,0,1,7],
"comprat": [0],
"seguiu": [2,[0,3,7]],
"seguir": [[0,3],2],
"descomprimiu": [2],
"seguit": [0,1,2],
"caractèr": [2],
"fr-zb": [2],
"assegura": [7],
"les": [0,7,1,2,4,3,5,6,8],
"press": [0],
"dock": [2],
"leu": [1],
"prem": [0],
"element": [0,4,7,3,1,5],
"ballem": [0],
"desconnecteu-vo": [2],
"seguid": [1],
"each": [[1,5]],
"dobl": [2,7,[0,4,5]],
"característiqu": [7,[0,1,3]],
"filenameon": [1,0],
"l\'edició": [1],
"ctrl": [0,4],
"editorinsertlinebreak": [0],
"jumptoentryineditor": [0],
"document": [0,7,2,3,5,[4,8]],
"two": [5],
"preparació": [2,0],
"moment": [[2,3],[5,7]],
"page_up": [0],
"glossaryroot": [0],
"d\'altr": [[1,2],[0,5]],
"resourc": [[2,3]],
"assegurar-s": [2],
"moodl": [0],
"rendiment": [[1,3]],
"s\'havien": [[0,7]],
"team": [2,4,8],
"xx_yy": [0],
"docx": [[2,7],[0,4]],
"project_stats_match_per_file.txt": [[4,6]],
"txt": [2,0,5],
"clau": [0,7,[1,2],3],
"l\'editor": [7,0,5,1,[3,4]],
"comerci": [3],
"quin": [7,[0,3]],
"suprimir-l": [3],
"definiu": [[0,1]],
"definit": [0,[1,4,6,7],[2,5]],
"definir": [[0,1],7,[2,3,5],4],
"lib": [0],
"lid": [4],
"s\'escriu": [4],
"source": [8],
"l\'opció": [1,[2,7],[3,4,6],0],
"d\'incrustacion": [0],
"definid": [[2,5],[0,6,7]],
"viewdisplaymodificationinfoselectedradiobuttonmenuitem": [0],
"index.html": [0,2],
"entir": [5],
"actual": [7,4,[0,2],5,1,6,3],
"encantaria": [0],
"d\'escriur": [0,3],
"màgic": [0],
"tecl": [0,7,[3,5],4],
"diffrevers": [1],
"ofereix": [2,[3,5,7],0],
"sincronitzar-lo": [[2,6]],
"full": [0],
"away": [3],
"afegir-hi": [0],
"mica": [[0,4]],
"màxim": [0,[2,4,6,7]],
"principis": [8],
"d\'allà": [2],
"delimita": [7],
"d\'exist": [7],
"project.gettranslationinfo": [7],
"tediosa": [3],
"doctorat": [1],
"precis": [6],
"tallar": [0],
"d\'esquerra": [0,4],
"desplaçar-lo": [5],
"mymemori": [1],
"interaccionaven": [2],
"cursiva": [[0,3]],
"regex101": [0],
"equal": [2],
"watson": [1],
"també": [2,7,0,3,[1,6],5,4],
"recupera": [2],
"short": [[3,7]],
"referènci": [0,7],
"defineix": [[1,4],[0,2],[5,7]],
"inclouen": [0,3],
"d\'enviar-s": [2],
"three": [1],
"coherència": [2],
"incrustació": [0,4],
"viewmarkglossarymatchescheckboxmenuitem": [0],
"ofici": [0],
"d\'acabar": [7],
"imagineu": [3],
"enter": [0,3,[1,4,7]],
"creatius": [0],
"desenvolupador": [0],
"applic": [2,[1,5]],
"desplaçar-vo": [3,[4,5]],
"credencials": [8],
"projectteamnewmenuitem": [0],
"gotoprevxenforcedmenuitem": [0],
"tancar-s": [2],
"distribuir": [8],
"iniciar-la": [2],
"troba": [[0,2],4,7],
"mida": [7,[1,2,5]],
"preced": [0],
"anomenada": [2,6],
"autocompletertablelast": [0],
"iniciar-lo": [2],
"indefinit": [0],
"recogn": [[3,5]],
"log": [0],
"xifr": [1,[5,7]],
"l\'inici": [0],
"afegir-la": [0],
"d\'inclour": [2],
"mostrar-ho": [3],
"adequad": [1],
"lot": [2],
"dona": [4,[1,7]],
"openjdk": [1],
"永住権": [[1,7]],
"gràfiqu": [2],
"conservaran": [[0,2]],
"comporta": [[0,2]],
"demanat": [[2,4]],
"toolscheckissuesmenuitem": [0],
"pane": [5,1,3,[0,2]],
"demanar": [2,5],
"prou": [2],
"adequat": [2],
"comença": [0,4],
"tutori": [0],
"orphan": [5],
"clic": [7,5,4,2,[0,1],3,6],
"divideixen": [0],
"autocompletertablepageup": [0],
"desactivar-l": [4],
"www.deepl.com": [1],
"logotip": [0],
"treballar-hi": [2],
"identificador": [0,4,3],
"l\'expressió": [0,7],
"config-fil": [2],
"convertirà": [[1,3]],
"shown": [5],
"d\'estar": [[0,1]],
"interessi": [2],
"d\'estat": [5,[0,3,8]],
"opcions": [8],
"lre": [0,4],
"system-user-nam": [0],
"lrm": [0,4],
"liter": [0,1],
"format": [2,0,7,4,3,1,[5,6],8],
"anul": [0],
"doni": [7],
"formar": [0],
"console.println": [7],
"rainbow": [2],
"l\'eina": [2,8],
"l\'ajuda": [4],
"subconjunt": [[0,2]],
"autocompleterlistdown": [0],
"afegir-lo": [2,6],
"llunyan": [0],
"mostren": [1,[5,7],3,4,6,0,2],
"d\'haver": [6],
"fundació": [2],
"part": [7,0,5,1,4,[2,3,6]],
"lectiva": [0],
"instruccion": [2,0,7],
"principal": [2],
"reconeguda": [5],
"regulars": [8],
"llistat": [8],
"siguin": [6,2,[1,5]],
"activefilenam": [7],
"temp": [2,[0,3],[4,5,6]],
"manté": [1],
"project_files_show_on_load": [0],
"coincideix": [0,7,[1,3]],
"rodona": [5],
"tema": [1,7,0],
"build": [2],
"d\'utilitzar": [[0,3],[1,2],4,[6,7]],
"circumflex": [0],
"meitat": [1],
"further": [3],
"teme": [1,2],
"stack": [7],
"connectors": [8],
"ident": [2],
"entries.s": [7],
"haurien": [0],
"tothom": [2],
"del": [0,2,7,4,1,6,[3,5],8],
"gotonextuntranslatedmenuitem": [0],
"targetlocal": [0],
"altra": [[0,2],[3,6]],
"path": [[0,2]],
"project.save.tmx": [7,2],
"des": [2,7,0,4,5,3,1,6],
"conèix": [2,4,7,0,3,1,6,5],
"deu": [7,[2,4]],
"seleccionant-lo": [7],
"treur": [4],
"començàveu": [3],
"finalitat": [2,[0,7,8]],
"impact": [3],
"establerta": [4],
"l\'execució": [0],
"especi": [0,[2,6,7]],
"percentag": [5,1],
"converteixen": [[0,2]],
"apareguin": [1,2],
"helpcontentsmenuitem": [0],
"aplica-ho": [1],
"resnam": [0],
"situacion": [2,3],
"omegat-org": [2],
"descript": [[1,4]],
"remote-project": [2],
"mostrar-s": [0],
"dibuixo": [0],
"initialcreationid": [1],
"ignore.txt": [6],
"habilita": [0,1,[3,7]],
"projectaccessdictionarymenuitem": [0],
"descrita": [5],
"sobreescrigui": [0],
"compara": [7],
"suprimiu-lo": [2],
"visualitzar": [0],
"term": [7,4,5,3,1,0,6,2],
"files_order.txt": [6],
"mind": [7],
"projectrestartmenuitem": [0],
"editorskipnexttoken": [0],
"s\'insereixi": [[1,6]],
"tancada": [5],
"right": [0,[3,5]],
"possible": [8],
"dèbil": [0],
"revisar": [[2,3,5]],
"propagar": [1],
"insid": [2],
"propagat": [[4,6],[0,1,2]],
"under": [[1,8]],
"dia": [0,2,[3,4]],
"paraula": [0,7,[4,6],[1,5]],
"dotz": [0],
"die": [2],
"acabi": [[0,2]],
"comenc": [0],
"seguretat": [2,6,1,7,0],
"seguint": [[2,6]],
"existeix": [[2,4],7],
"din": [0,6,[2,3,5]],
"coincidència": [0,4,1,5,6,7,3,2],
"n\'afegiu": [2],
"dir": [2,0],
"down": [0],
"submenú": [[2,7]],
"later": [[3,7]],
"assignar": [7,[0,4],[2,6]],
"exemples": [8],
"assignat": [2,[4,7]],
"legal": [0],
"gràfica": [2],
"viewfilelistmenuitem": [0],
"mantenint": [7],
"varien": [[0,2]],
"test": [2],
"assignad": [0],
"esborrareu": [0],
"probabilitat": [2],
"etiquet": [1,0,3,7,4,2],
"omegat": [2,0,3,7,4,6,[1,8],5],
"omegat.project.aaaammddhhmm.bak": [2],
"comprovarà": [1],
"allemand": [1,7],
"deepl": [1],
"final": [0,3,[1,7],[2,6]],
"desmarcada": [7],
"virtual": [7,2],
"electrònica": [0],
"ignora": [0,1],
"qüestion": [2],
"console-align": [[2,7]],
"dissimul": [5],
"back": [3],
"assegureu": [0],
"projectopenrecentmenuitem": [0],
"fr_fr": [3],
"miss": [5],
"codificació": [0,[7,8]],
"thèse": [1],
"load": [7],
"d\'algun": [3],
"ignori": [[6,7]],
"restaura": [[5,7]],
"coincid": [[6,7],1],
"l\'ssh": [2],
"issue_provider_sample.groovi": [7],
"una": [0,2,7,4,1,3,6,5,8],
"d\'entrad": [[2,7]],
"une": [2,[4,7]],
"partir": [0,[1,2,7],3,4],
"estàndard": [[2,3],[0,1,5,7],4],
"editoverwritemachinetranslationmenuitem": [0],
"lematització": [1],
"relat": [2],
"patró": [0,1,7,2],
"console-stat": [2],
"ingreek": [0],
"millor": [3,7,5,[0,4]],
"xmxmida": [2],
"f12": [7],
"allotjat": [1],
"convers": [2],
"ignor": [5,4],
"projectexitmenuitem": [0],
"segura": [2],
"senzills": [8],
"loca": [0,1],
"lock": [2,3],
"adoptium": [2],
"text": [0,7,1,4,5,3,2,6,8],
"vegada": [2,0,[4,6],3,1,[5,7]],
"estricta": [2],
"editregisteruntranslatedmenuitem": [0],
"init": [2],
"cadena": [7,4,1,0],
"productivitat": [0],
"s\'utilitzarà": [[0,3,7]],
"d\'emplena": [1,5,3],
"enganxar": [4],
"s\'emmagatzema": [0,[3,5]],
"manag": [2],
"útil": [7,0,2,4,3,[1,5]],
"manifest.mf": [2],
"capítol": [2,[3,7],0,[4,5]],
"associació": [2],
"maco": [0,2,4,5,3,1],
"perdeu": [[2,3]],
"field": [3,7],
"servei": [2,1,5,4],
"doc": [7,0],
"autenticat": [1],
"doe": [3,[2,7]],
"s\'hagi": [2,[0,4]],
"output-fil": [2],
"publicar": [2],
"server": [2],
"interactuar-hi": [7],
"dos": [0,7,2,1,4],
"assegurar": [2,3],
"atractiu": [3,8],
"tornarà": [7],
"distinció": [0,7],
"reutilitzar": [2,[0,3,7]],
"mai": [0,[1,3,4,6]],
"l\'svn": [2,7],
"funcionalitat": [2,[3,4,7]],
"maj": [4,0,7],
"dobla": [0],
"mal": [[4,7]],
"especifica": [0,2,[5,6]],
"map": [2,6],
"retornarà": [0],
"may": [[2,3]],
"febl": [0],
"títol": [[4,7]],
"url": [2,1,[3,6],0],
"megabyt": [2],
"uppercasemenuitem": [0],
"viewmarkuntranslatedsegmentscheckboxmenuitem": [0],
"acaba": [0],
"needs-review-transl": [0],
"usb": [2],
"use": [3,2,4,[1,7],[0,8]],
"incloguin": [0],
"usd": [7],
"feel": [1],
"main": [[1,8]],
"taula": [0,1,[4,5]],
"uso": [0],
"separació": [0],
"omegat.jar": [2,0],
"omegat.app": [2,0],
"conveni": [[3,7]],
"usr": [[0,1,2]],
"descriur": [2],
"d\'internet": [2],
"combina": [[1,2]],
"recopilar": [2],
"utf": [0,6],
"lació": [2,[0,1,3,8]],
"havíeu": [3],
"maner": [2,[4,7]],
"suprimiran": [2],
"obtenir": [7,5,[0,1,2,3,4]],
"d\'incrustació": [0],
"background": [6],
"llindar": [[1,2,5]],
"s\'imprimeixen": [0],
"l\'espai": [[0,3]],
"dsl": [6],
"operatiu": [4,0,[1,2,5,7]],
"baixarà": [2],
"med": [4],
"aquesta": [7,[0,2],1,4,6,3,5],
"dtd": [[0,2]],
"repeat": [3],
"mes": [[0,2]],
"automàtiqu": [[1,2],4],
"meu": [0],
"make": [2,[3,7]],
"mateixo": [0,2],
"d\'error": [1,4,[0,2,5]],
"projectcompilemenuitem": [0],
"console-transl": [2],
"senzilla": [0,[2,7],3],
"podrà": [[0,2]],
"mateixa": [[2,7],0,[1,5],[3,4],6],
"optionsautocompletehistorycompletionmenuitem": [0],
"due": [2,0,[1,3,7],4],
"gotonextuniquemenuitem": [0],
"conform": [[3,7]],
"wordart": [0],
"explícita": [2],
"princip": [[5,7],2,[1,4],[0,3,6]],
"copiaran": [5,4],
"dur": [[2,7]],
"dipòsit": [2,6,[4,5,7],[0,1]],
"diari": [2],
"inform": [[2,4,7]],
"s\'il": [3],
"depend": [[1,7]],
"duu": [[2,4]],
"progré": [5,2],
"commit": [2],
"targetlocalelcid": [0],
"fàcilment": [3,6,[2,4]],
"project_stats_match.txt": [[4,6]],
"exclogui": [2],
"reorganitzar": [0],
"tab-separ": [0],
"d\'ús": [2,7],
"curt": [[1,3,7]],
"destinada": [[2,6]],
"revisor": [[2,6]],
"executi": [[0,2]],
"s\'hi": [2],
"executa": [7,1],
"d\'assignacion": [2],
"highest": [5],
"s\'ha": [0,4,7,[1,2],5],
"controlar": [0],
"libreoffic": [3,0],
"autocompleterclos": [0],
"emmagatzematge": [8],
"adapta": [7,3],
"convertir-vo": [3],
"into": [[0,1,3,5,7]],
"intimiden": [0],
"l\'origen": [7],
"reutilitzar-la": [2],
"industri": [3],
"exacta": [7,0,6],
"variables": [8],
"texto": [7,[0,3]],
"viewdisplaysegmentsourcecheckboxmenuitem": [0],
"editregisteremptymenuitem": [0],
"mismatch": [7],
"oper": [[2,3,7]],
"requereixin": [[1,4]],
"desbloc": [5],
"mani": [3],
"open": [2,0,[1,5,7]],
"començar": [3,[0,2]],
"project": [2,7,6,[0,3],4,1,5,8],
"取得": [[1,7]],
"xmx1024m": [2],
"s\'envia": [0],
"recomanad": [0],
"s\'introdueix": [4,7,0],
"vinculada": [7],
"començat": [7],
"penalty-xxx": [[2,6]],
"gotonextsegmentmenuitem": [0],
"única": [7],
"finestra": [7,5,4,[0,1],3,[2,6,8]],
"pràctic": [[6,7]],
"look": [1],
"quinoa": [0],
"pèrdua": [2],
"dropbox": [2],
"segmentar-lo": [[0,3]],
"selecciona-ho": [0],
"abort": [2],
"tancarà": [4],
"internet": [1,0],
"navega": [7,4],
"comma-separ": [0],
"allow": [2,4],
"saltar": [[3,5]],
"excepció": [1,0],
"copiar-la": [2],
"s\'adjuntarà": [1],
"proper": [1,2],
"se\'n": [1],
"printf": [0,1],
"ocorr": [4],
"aquest": [0,2,7,4,6,1,3,5,8],
"appli": [[1,7]],
"interess": [0],
"patron": [0,1,7,2],
"basa": [0,7],
"revisió": [3,7,[2,8]],
"fent": [7,6,5],
"externa": [[0,1],[4,7]],
"ressaltarà": [0],
"layout": [[1,2]],
"registri": [1],
"lletra": [0,1,[3,5,7],4],
"l\'eficiència": [3],
"registra": [[0,1,2,4]],
"bash": [[0,2]],
"tmroot": [0],
"base": [[2,5]],
"stem": [5],
"registr": [0,4,[1,2,6]],
"enllaçada": [6],
"hauran": [2],
"whole": [[2,4]],
"reliable": [8],
"mou": [7,1],
"ignora-ho": [[1,6]],
"automàtica": [7,[1,4],6,2,5,0,3,8],
"大学": [1],
"insertcharslr": [0],
"permeso": [2],
"inseriu": [0],
"val": [7],
"indica": [5,2,[0,1,7]],
"inserit": [4,6,2],
"van": [0],
"still": [5],
"inserir": [1,4,[3,5],[0,2,6,7]],
"perdut": [2],
"activació": [2],
"d\'ani": [0],
"desbloca": [5],
"d\'instruccion": [0],
"subcarpeta": [2,7],
"manipular-lo": [4],
"variar": [2],
"aprofitar-ho": [0],
"estructura": [[6,7],[2,8]],
"word": [[0,3,4,5,7]],
"interfíci": [2,0,4,[1,3,5]],
"lingue": [1],
"traduiran": [2,0],
"senzil": [1,[0,3],7],
"commutació": [4],
"ocupen": [7],
"adreça": [0,2],
"d\'escapar": [2],
"possibilitat": [2],
"acceptada": [7],
"patiu": [3],
"admes": [0],
"admet": [[2,7],6],
"fete": [3],
"servidor": [2,1,6,[5,8]],
"simplificar-ho": [2],
"vcs": [2],
"lingvo": [6],
"contenen": [0,7,3,2,[5,6]],
"developer.ibm.com": [2],
"mrs": [1],
"l\'aspect": [3,0],
"entrada": [7,5,[0,1,4],[3,6]],
"literari": [0],
"permet": [7,1,[0,4],5,2,3],
"requisit": [2],
"incloure\'l": [1],
"haureu": [2,7,[0,1]],
"evita": [2],
"formular": [3],
"aviat": [4],
"threshold": [1],
"caracteritzen": [0],
"consulteu-n": [6],
"amagueu": [0],
"baixada": [2,1],
"html": [0,2,[1,3]],
"d\'entrada": [4,[1,2,3]],
"spell": [0],
"recordareu": [4],
"cascada": [1],
"ves": [0,5,3,4,[2,6,8],1],
"insertcharsrl": [0],
"permissius": [7],
"d\'accedir": [0],
"comprovar-n": [3],
"màquina": [7,2],
"finit": [1],
"d\'accé": [[1,2]],
"suprimiu": [2,[0,6,7],1],
"accediu": [[3,5],[1,2]],
"suprimit": [1],
"accedir": [7,1,4,[2,3,5],[0,6]],
"suprimir": [2,7,6,1,[0,3,4]],
"www.ibm.com": [1],
"vermell": [1],
"platform": [1],
"inserid": [6,[1,2]],
"indiqueu": [7,2,[1,4]],
"essencial": [2],
"seleccionada": [4,[1,2,5],0],
"indiquen": [0,2],
"toolsalignfilesmenuitem": [0],
"diàleg": [[1,7],[0,3,4],2,[6,8]],
"podeu": [2,0,7,3,5,6,1,4],
"hauria": [[2,6],[0,4]],
"contenir": [2,[5,6],[0,1]],
"poder": [0],
"reviseu": [3,7],
"calculadora": [7],
"command": [2],
"poden": [2,0,7,3,1,[4,5],6],
"recomanem": [2],
"compatibilitat": [[0,2],[3,8]],
"addició": [0],
"resideix": [2],
"desaran": [0],
"traduir-la": [2],
"onecloud": [2],
"viewmarkbidicheckboxmenuitem": [0],
"refus": [2],
"preocupar-s": [3],
"revisada": [3],
"d\'etiquet": [1,2,4,[0,3]],
"preferit": [2],
"actualitzar-lo": [6],
"preferiu": [[0,7]],
"suposarà": [0],
"estè": [2],
"desenvolupa": [2,[0,1,7]],
"l\'alternativa": [4],
"subtítol": [2],
"fileshortpath": [[0,1]],
"agrega": [0],
"traduir-lo": [[2,7]],
"visual": [0,[3,4]],
"permiso": [7],
"日本語": [7],
"esperàveu": [2,1],
"veureu": [6],
"conservarà": [6,[0,2,7]],
"descartaran": [7],
"verificar": [0],
"version": [2,[0,6,7,8]],
"folder": [5,2,[1,3],6],
"branca": [2],
"editar-lo": [5],
"està": [7,2,[0,5,6],[1,4],3],
"referència": [0,2,6,7,3],
"handl": [7],
"detail": [4],
"laborador": [[0,7]],
"conjunta": [3],
"s\'utilitzi": [7],
"utilitz": [2,0,[1,3,6,7]],
"projecteditmenuitem": [0],
"ein": [2,1,7,[0,6],4,3],
"configurat": [[2,6]],
"configurar": [1,[2,5,6]],
"gestionaran": [3],
"new_word": [7],
"d\'una": [0,2,4,1,3,[5,6,7]],
"obligatòria": [[0,7]],
"nashorn": [7],
"japonè": [2,1,0],
"s\'utilitza": [2,0,6,[1,3,7]],
"projecte_en-us_d": [2],
"descend": [0],
"last_entry.properti": [6],
"inhabilitat": [4],
"inhabilitar": [7,5],
"publicar-lo": [2],
"detectar": [4],
"prémer": [7,5,[1,3]],
"notes": [8],
"alinea": [[0,2,4,7]],
"autocompleternextview": [0],
"specif": [7],
"d\'això": [0],
"definició": [0],
"inhabilitad": [[1,7],0],
"potser": [7,[0,5]],
"dsun.java2d.noddraw": [2],
"substitueixen": [[2,7]],
"s\'ajusti": [[3,5]],
"compartida": [[2,6]],
"donem": [3],
"ell": [0,1,5],
"assigni": [2],
"need": [3,7],
"editorfirstseg": [0],
"x0b": [2],
"assigna": [[0,3]],
"inspirad": [7],
"l\'evolució": [0],
"determinad": [[2,5]],
"altern": [0,1,5,4],
"automatitzin": [2],
"http": [2,1],
"pogut": [[2,3,5]],
"prototip": [7],
"entorn": [2],
"accion": [7,[4,5],2],
"l\'annex": [7,[0,1],4,2,[3,5,6]],
"copiïn": [2],
"amaga": [7,0],
"significa": [0,[1,7]],
"lisenc": [0],
"millorar": [0,7],
"contribueix": [0],
"vol": [[2,6]],
"projectsinglecompilemenuitem": [0],
"end": [0,7],
"lisens": [0],
"l\'error": [[1,4]],
"iniciar": [2,[0,7],3],
"mantindrà": [5],
"iniciat": [1],
"tindran": [[1,2,6]],
"particip": [2],
"detecteu": [7],
"ubicació": [2,0,7,1,[3,6],4],
"env": [0],
"estil": [[0,4,7]],
"inicial": [6,1],
"okapi": [2],
"page_down": [0],
"progressiva": [1],
"ràpida": [7],
"crear-lo": [[2,3]],
"detecten": [4],
"minúscula": [0],
"gràfic": [0],
"recomana": [2,7],
"decoratiu": [3],
"desament": [1,2,[4,6,8]],
"donar": [[0,1,2,3,5]],
"reiniciar": [[0,2]],
"trieu": [0,2],
"donat": [2],
"l\'ús": [2,0,1],
"s\'obtindran": [1],
"majoria": [0,4,[1,2]],
"system-os-nam": [0],
"insertcharspdf": [0],
"specifi": [3],
"s\'uneix": [7],
"invis": [0],
"similar": [2,0,[1,3],[5,7],6],
"tar.bz2": [6],
"forta": [0],
"restaurar": [2],
"d\'adrec": [[0,6]],
"reprodueix": [0],
"dedicàveu": [3],
"genèriqu": [1],
"bundle.properti": [2],
"contributors.txt": [0],
"traduirà": [[0,7]],
"l\'excepció": [2],
"interfereix": [4],
"connector": [2,1,0,3],
"www.regular-expressions.info": [0],
"bàsica": [7,[0,2]],
"canadà": [2],
"combinar-lo": [3],
"s\'acceptin": [1],
"l\'altr": [2],
"ajudar": [3,[0,8]],
"separacion": [4],
"sourcelang": [0],
"d\'extensió": [0],
"minimitza": [[2,5]],
"omegat\'s": [8],
"penalització": [6,1],
"direccionalitat": [0],
"optionsdictionaryfuzzymatchingcheckboxmenuitem": [0],
"interfac": [1],
"simbòlic": [2],
"projet": [5],
"share": [2],
"s\'emplenarà": [6],
"protegir-s": [2],
"sourcelanguag": [1],
"inicieu": [0,[2,5]],
"decidiu": [3,2],
"decidir": [0],
"explicació": [0],
"gzip": [6],
"helpupdatecheckmenuitem": [0],
"visitat": [4,6],
"estarà": [4],
"trebal": [3,[2,5],[0,6]],
"notif": [5],
"plataform": [2,0],
"esteu": [[2,7],5],
"esc": [5],
"fitxer": [2,0,7,4,6,3,1,5],
"exampl": [2],
"processar-lo": [0],
"trobarà": [0],
"hàgiu": [[2,3],[5,7],[0,1,6]],
"constitueix": [[0,6]],
"nostemscor": [1],
"trobava": [1],
"triar": [[1,7],[0,2]],
"representacion": [0],
"enfoca": [2],
"project_chang": [7],
"d\'etiquetes": [8],
"configuracion": [[1,7]],
"reinicieu": [2,1],
"console-createpseudotranslatetmx": [2],
"programació": [7],
"etc": [[1,7],[0,2,3,4,5]],
"notificació": [5],
"fuzzyflag": [1],
"veuran": [3],
"enumera": [[1,5]],
"d\'exclusió": [[2,7]],
"escap": [0],
"païso": [1],
"below": [1],
"inhabilit": [5],
"suposem": [0],
"poisson": [7],
"autenticació": [[2,5]],
"ll-cc.tmx": [2],
"automatitzar": [[3,7]],
"torn": [[2,3]],
"assignada": [[0,4,6]],
"forma": [0,[2,7]],
"premeu": [7,4,3],
"calcul": [5],
"respectiva": [[0,2,6]],
"introduïu": [1],
"introduït": [7,5],
"magento": [2],
"introduïd": [[1,3]],
"haver": [2],
"inhabiliteu": [7,[0,1]],
"intenta": [7,1,[0,2]],
"quarta": [2],
"pseudotraduït": [2],
"estar": [[0,3]],
"estan": [7,[0,1,2],[3,4]],
"ll_cc.tmx": [2],
"u00a": [7],
"estat": [5,[0,3]],
"carpeta": [2,0,7,6,4,1,3,5,8],
"xineso": [1],
"lletr": [0,4,2,3],
"shift": [0],
"cert": [2,0],
"representen": [0],
"taules": [8],
"java": [2,0,1,7,3],
"s\'escriuen": [[0,2]],
"exe": [2],
"invertid": [1],
"demostren": [0],
"tote": [[0,7],[2,4],1,[3,6]],
"comodin": [2],
"tota": [[0,7]],
"project_save.tmx": [2,6,3,4,7],
"dictionari": [3,1,6,[4,5],7],
"marcad": [[0,1]],
"remain": [4],
"suprimirà": [1],
"gini": [5],
"powershel": [[0,2]],
"d0aquesta": [2],
"intentar": [[2,7]],
"dictionary": [8],
"marcat": [1,4,[0,7],[3,5]],
"treballar": [2,0,[3,6]],
"traduint": [7,[2,4,5]],
"marcar": [1,[0,7]],
"locant-lo": [0],
"treballat": [3],
"heroi": [0],
"implicat": [2],
"default": [[1,3],[4,5]],
"sudo": [2],
"dividirà": [[0,7]],
"fusionarà": [[0,7]],
"drop-down": [7],
"timestamp": [[0,8]],
"concedeix": [8],
"despleg": [[0,7],1],
"projectaccessrootmenuitem": [0],
"mecanism": [[0,2,4]],
"gran": [7,[2,3]],
"índex": [0],
"alineació": [7,[3,4,8]],
"prendr": [2],
"grau": [3],
"voleu": [7,2,0,4,1,6,[3,5]],
"such": [2,7],
"plugin": [0,[1,2]],
"transmeti": [3],
"principi": [3,5,0],
"autocompletertableup": [0],
"s\'activi": [2],
"implicad": [7],
"s\'activa": [4],
"bàsic": [2],
"projectcommitsourcefil": [0],
"editinsertsourcemenuitem": [0],
"viterbi": [7],
"microsoft": [0,[3,7]],
"projectnewmenuitem": [0],
"ecmascript": [7],
"apliqueu": [2,3],
"segment": [7,4,5,0,1,3,6,2,8],
"changes.txt": [[0,2]],
"porta-retal": [4],
"restringir": [3],
"glossari": [0,5,7,4,6,[1,3],2],
"s\'acaben": [7],
"ignored_words.txt": [6],
"github.com": [2],
"configuration.properti": [2],
"autocompleterlistpageup": [0],
"expressions": [8],
"demostrat": [0],
"glossary": [8],
"assignar-lo": [4],
"operació": [2],
"personalització": [0,4],
"dividint": [3],
"reopen": [2],
"recrear": [[0,2]],
"s\'està": [[4,5]],
"next": [7,[3,4,5]],
"modificareu": [0],
"import": [[0,2],[3,5,6]],
"color": [4,7,1,[0,3,5]],
"string": [2],
"hidden": [[5,7]],
"l\'autoria": [4],
"classes": [8],
"nom": [0,2,7,1,5,3,6],
"button": [3,7],
"not": [[2,4],3,[0,1,7]],
"necessàri": [1,0],
"pantalla": [[0,3]],
"nou": [2,[0,7],3,1,[4,5,6]],
"desplaçar": [0],
"d\'assistència": [0],
"factor": [4],
"ascii": [0],
"l\'accé": [2],
"greek": [0],
"publica": [2,[0,4]],
"preconfigurad": [1],
"green": [5],
"límit": [0,5,4,[1,3]],
"atura\'t": [1],
"resultat": [7,0,[2,4],[1,3,5]],
"selection.txt": [[0,4]],
"way": [8],
"xhtml": [0],
"itoken": [2],
"finder.xml": [[0,6,7]],
"volat": [4],
"window": [0,2,4,5,[1,3]],
"desempaquetar": [2],
"gramat": [[4,7]],
"depenen": [2,[0,4]],
"suprimint": [[2,7]],
"envia": [2,1],
"disable-project-lock": [2],
"omegat.pref": [[0,1,7]],
"contrària": [0],
"when": [[5,6],[1,2,4,7]],
"identificadors": [8],
"fan": [0],
"comparteixen": [7,0],
"sufici": [[0,2]],
"vegad": [0,[2,3],7],
"auto-popul": [4],
"inesperada": [2],
"estrani": [0],
"clonar-lo": [2],
"estigui": [[2,3],[0,5]],
"presentat": [2],
"multipl": [2],
"pàgina": [0,[2,4,7],[1,3]],
"l\'excel": [0],
"treballin": [2],
"pàgine": [3],
"obtén": [4,[0,1]],
"lowest": [5],
"tercer": [2,[3,5]],
"alinead": [1],
"suma": [0],
"freqüència": [[0,2,7]],
"determinat": [1,[0,4,5,6]],
"determinar": [7,1],
"alinear": [7,4,2],
"direct": [2],
"alineat": [[0,7]],
"modificaran": [2],
"àrab": [0],
"modern": [2],
"web": [1,7,2,[4,5]],
"l\'arrel": [2],
"d\'atribut": [0],
"categories": [8],
"editselectfuzzy4menuitem": [0],
"editregisteridenticalmenuitem": [0],
"gris": [4,3],
"apareixerà": [1],
"estrany": [0],
"hanja": [0],
"modifiqui": [2],
"prediccion": [3],
"desempaqueteu": [2],
"advanc": [1],
"presentació": [0],
"l\'estàndard": [2],
"indicació": [0],
"section": [3],
"actualització": [1,2],
"fer": [2,[4,5,7],[0,6],3],
"protocol": [2,1],
"fet": [[2,6,7]],
"feu": [7,2,1,0,4,5,3,6],
"dict": [1],
"còpies": [8],
"dispon": [7,2,0,1,4,5,3],
"direccion": [[0,4]],
"presenten": [0,5,[2,4,7]],
"llibertat": [8,0],
"codificacion": [0],
"gair": [[0,1,7]],
"treballeu": [7,[1,2]],
"d\'element": [0],
"keep": [2,7],
"pròxima": [2,[0,1]],
"option": [1,[2,3]],
"who": [1],
"processa": [[1,4],[3,7]],
"àrea": [5],
"remark": [3],
"d\'obertura": [0],
"adonat": [3],
"groc": [4],
"respectarà": [2],
"distingir": [3],
"disposició": [5,1,3,8,[0,4],2],
"d\'url": [2],
"ocasional": [3],
"premut": [5],
"reconeix": [[0,1,2,4,7]],
"paràgraf": [0,7,5,[1,4],3],
"paquet": [2,[1,4],0],
"enllaçat": [3],
"various": [[1,2,3,4]],
"emmagatzemar-lo": [0],
"useu": [3],
"desacoblar": [1],
"user": [1,4],
"s\'adrecen": [0],
"desmarqueu": [7,1],
"afegint": [7],
"extens": [0,6],
"back_spac": [0],
"canviar-la": [2],
"fin": [7,[0,2],3,[1,4,5,6]],
"dispositiu": [2],
"robot": [0],
"cantant": [[0,7]],
"surt": [4,[0,2]],
"calculen": [1],
"claus": [7,1],
"enregistrat": [3],
"enregistrar": [5],
"nocturna": [2],
"eclips": [2],
"sure": [2,[3,7]],
"gratuïta": [2],
"l\'adreça": [2],
"posterior": [0,1,2],
"diff": [1],
"al": [2,0,7,4,3,1,6,5],
"automat": [1,[2,3]],
"editmultiplealtern": [0],
"an": [5,[2,3]],
"llicència": [2,0,8],
"posició": [0,4,[1,5,7],[3,8]],
"formen": [0,7],
"produirà": [0],
"as": [2,[1,3],[0,5]],
"perdr": [[2,3]],
"predefin": [1],
"at": [3,7],
"substituir-lo": [6],
"copieu-hi": [2],
"tancar-lo": [[0,7]],
"be": [2,[0,1,4,5,7]],
"prove": [2],
"coincidènci": [[1,4],6,7,5,[2,3],0],
"prova": [2],
"d\'emmagatzemar": [2],
"salta": [7,4],
"d\'autor": [4],
"filters.xml": [0,[1,2,6,7]],
"penseu": [2],
"importat": [5],
"bo": [2],
"laboració": [3],
"anterior": [0,4,2,3,1,7,5,6],
"br": [0],
"l\'url": [2,[1,4,7]],
"search": [1,4,2],
"necessita": [7,6],
"by": [[1,2,5],[3,4,8]],
"alfanumèr": [0],
"d\'indicar": [[0,1]],
"segmentation.conf": [[0,2,6,7]],
"ca": [2],
"iniciarà": [2],
"arxiu": [6],
"cc": [2],
"exclou": [[0,2]],
"ce": [2],
"acció": [4,2,[0,1,5],7],
"figur": [5],
"cs": [0],
"renam": [2],
"ella": [0],
"recompt": [[4,7]],
"terminologia": [0,4],
"raon": [[1,7],0],
"apach": [2,7],
"intermediari": [2,1,8],
"adjustedscor": [1],
"font": [1,[0,2],[4,7]],
"dd": [2],
"de": [0,2,7,1,4,3,5,6,8],
"explicacion": [7,1],
"d\'executar": [4,[2,8]],
"fora": [2,0,[5,6]],
"intenció": [0],
"extern": [7,1,4,[0,3],5,[2,6]],
"d\'ordr": [2,0,1,7],
"f1": [[0,4,7]],
"do": [4],
"f2": [[3,5],[0,7]],
"bés": [0],
"f3": [[0,4],5],
"f5": [[0,3,4]],
"d\'instànci": [4],
"utilitzarà": [1,[2,7]],
"resposta": [5],
"dz": [6],
"editundomenuitem": [0],
"rare": [2],
"especifiquen": [0],
"ja-rv": [2],
"comparteixin": [2],
"especifiqueu": [[1,2],7],
"selecciona": [4,0,7,5,1],
"avisar": [2],
"el": [2,7,0,4,1,5,3,6,8],
"belazar": [1],
"en": [0,7,2,4,1,5,3,6,8],
"esperada": [[0,8]],
"copiant": [6],
"es": [0,7,2,4,1,5,6,3],
"carro": [0],
"ex": [1,0,2],
"pensada": [6],
"formin": [2],
"l\'oblideu": [1],
"duplicada": [2],
"activ": [[5,6,7]],
"fa": [0,6,[1,2,3,7]],
"actiu": [4,1],
"lentament": [2],
"indic": [0,4,7],
"confirmi": [2],
"fi": [0,4],
"fon": [[4,5,6]],
"bé": [2,3,0,4,[1,7]],
"origin": [2,0,7,3,1,[4,5]],
"vocal": [0],
"for": [2,1,4,7,5],
"exclud": [2],
"pensar": [0],
"confirma": [0,1],
"antic": [0],
"fos": [0],
"fr": [2,[1,3]],
"guionet": [0],
"content": [[0,2],[1,3,7]],
"duckduckgo": [1],
"documentació": [[0,2],[3,7]],
"periòdica": [2,6],
"applescript": [2],
"accid": [1],
"json": [2],
"exclus": [7],
"gb": [2],
"class": [0,2,7],
"helplogmenuitem": [0],
"presenta": [0,2,[3,7]],
"dipòsit_d\'un_projecte_en_equip_de_l\'omegat": [2],
"direccional": [8],
"l\'aplicació": [0,2,4,[3,7],[1,8]],
"discrepànci": [4],
"editoverwritetranslationmenuitem": [0],
"conserva": [7,0,6,2],
"provenien": [6,2],
"tanqueu": [7,2,[0,4]],
"fosc": [1],
"aeiou": [0],
"contrari": [2,[0,7]],
"existeixen": [[0,4]],
"conservi": [7],
"form": [[0,7]],
"generar-lo": [6],
"cantair": [0],
"grup": [0,1,7,[2,3,5]],
"ranura": [4],
"ha": [2,0,7,4,1,3,5,6],
"ajusteu": [7],
"afegirà": [6],
"restor": [6],
"fort": [7],
"he": [0],
"obr": [4,7,0,[1,3],[2,6]],
"assign": [[0,1,3,4,7]],
"prové": [2],
"genèric": [1,[0,2]],
"hh": [2],
"hi": [2,[0,4],7,1,3,5,6],
"duser.languag": [2],
"viewmarkparagraphstartcheckboxmenuitem": [0],
"ho": [[0,4],[2,3,6,7]],
"vàlida": [0],
"repetir": [2],
"repetit": [[1,7],4,0],
"anomenad": [0],
"l\'unirà": [7],
"file-target-encod": [0],
"insereix": [[0,4],1,[5,7],[2,3],6],
"periòdiqu": [2,6],
"verd": [7,[4,5]],
"mainmenushortcuts.mac.properti": [0],
"context": [[1,5],[2,4]],
"id": [1,[0,7]],
"https": [2,1,0,[5,6]],
"impedir": [[2,4,6],[0,3,7]],
"if": [7,[2,3,4],[0,1,5]],
"project_stats.txt": [6,4],
"defineixen": [0,[1,2,7]],
"ocr": [7],
"projectaccesscurrenttargetdocumentmenuitem": [0],
"supressió": [2],
"in": [5,7,1,[0,2,4],3,[6,8]],
"vinculat": [7],
"termin": [2],
"ip": [2],
"lower": [5],
"amplieu": [2],
"index": [0],
"is": [1,5,[2,3],[4,6,8],[0,7]],
"it": [5,1,[2,3]],
"projectaccesstmmenuitem": [0],
"odf": [0],
"impedeix": [4,1],
"desitjad": [4],
"ja": [2,7,0,6,1,[3,4,5]],
"s\'atura": [1],
"adaptar-l": [3],
"odt": [[0,7]],
"anomenat": [2,[0,1,7]],
"glossaris": [8],
"gotonexttranslatedmenuitem": [0],
"senzill": [0,[1,2]],
"desitjat": [[0,1]],
"origen": [[5,7]],
"toolscheckissuescurrentfilemenuitem": [0],
"libraries.txt": [0],
"learned_words.txt": [6],
"robusto": [2],
"d\'oració": [[0,4]],
"escrit": [2],
"finestres": [8],
"escriu": [[0,7]],
"pregunt": [0],
"arribeu": [3],
"proporcionen": [2,0],
"correspon": [7,0,[4,6],1,[2,3,5]],
"robusta": [2],
"ftl": [[0,2]],
"tècnic": [[0,2]],
"viewdisplaymodificationinfoallradiobuttonmenuitem": [0],
"subfinestra": [5,7,3,4,[1,6],0,2],
"reflecteixen": [2],
"funcionen": [0],
"ressaltar-lo": [6],
"supèrflu": [[0,7]],
"símbol": [0,[2,5]],
"completa": [7,0,2,6],
"logograma": [0],
"la": [0,2,7,4,1,5,3,6,8],
"forçada": [[0,4]],
"esdeveni": [7,0,2],
"l\'apartat": [2,[0,3]],
"d\'introduir": [2],
"li": [0],
"comprova": [1,0,4,2,7,3],
"sincronitza": [2,7,5],
"ll": [2],
"genera": [2],
"somni": [0],
"ordr": [7,1,0,4,2,5,3],
"lu": [0],
"while": [[1,5]],
"acoblad": [[3,5]],
"són": [0,7,2,1,5,3,[4,6]],
"dade": [2,[1,4],7,[3,6]],
"cycleswitchcasemenuitem": [0],
"that": [[2,3],7,[1,4,5]],
"dret": [5,[4,7],2,6],
"dada": [7],
"mb": [2],
"desinst": [2],
"incloeu": [0],
"estableix": [7],
"calculat": [1],
"calcular": [7],
"llibertats": [8],
"mm": [2],
"permè": [3],
"entri": [7],
"permí": [7],
"mt": [6],
"copieu-la": [2],
"ampliar": [7],
"ampliat": [0],
"interessat": [0],
"viatg": [0],
"editat": [[5,7],4],
"license": [8],
"intenteu": [[0,1]],
"editar": [7,[0,2,4]],
"tornareu": [7],
"disc": [[2,7]],
"ni": [0,2,1,3],
"veur": [3,7,0,[1,2,4,5]],
"marcada": [[0,7],[1,2,6]],
"despré": [0,2,7,3,1,5,[4,6]],
"desblocat": [5],
"three-lett": [[3,7]],
"desblocar": [5],
"licenss": [0],
"dreceres": [8],
"navegador": [1,[4,5]],
"no": [0,2,7,1,3,4,6,5],
"desmarcar": [[0,7]],
"code": [3,7,0],
"l\'esborrarà": [1],
"biblioteca": [0],
"puja": [4],
"gotohistoryforwardmenuitem": [0],
"comparteixi": [2],
"head": [0],
"blau": [7],
"dialog": [3],
"suggereix": [[3,5]],
"of": [[1,2,3],7,0,[6,8]],
"enganxa": [5],
"possibl": [0,2,[1,5],[3,4],7],
"reserva": [2],
"ok": [3],
"llegeix": [[1,7]],
"codi": [0,2,7,1],
"on": [0,7,1,3,[2,5],4,6],
"or": [0,7,[2,3],[1,4],[5,6]],
"documentat": [2],
"opcion": [7,2,0,4,3,1,5],
"crear-n": [7],
"l\'últim": [4],
"notificacion": [5,1],
"pendent": [7],
"n\'acoloreix": [0],
"equiv": [0],
"editinserttranslationmenuitem": [0],
"equip": [2,4,6,[0,1],[7,8],[3,5]],
"fileextens": [0],
"d\'enviar": [2],
"determina": [[0,1]],
"complexo": [7,0],
"vàlid": [2,0],
"conversió": [2,1],
"traduccion": [5,6,[2,3],[0,1,4],7],
"po": [2,0,1,[5,6]],
"subratllat": [[0,4]],
"procé": [3,[0,2,7],1],
"subratllar": [5],
"autocompletertablefirst": [0],
"mà": [3],
"necessari": [2,[3,6,7]],
"aplicat": [[2,5]],
"corren": [7],
"informàt": [2],
"correm": [7],
"aplicar": [7,0,1,[2,3,4]],
"actualitzada": [3],
"recent": [[2,4],[0,6],[5,7]],
"bloqui": [2],
"github": [2],
"aplicad": [[0,1,4]],
"edit": [[2,5],[0,3]],
"editselectfuzzy5menuitem": [0],
"them": [[3,7],2],
"then": [3],
"facilitar-vo": [7],
"fer-lo": [0],
"plantill": [0],
"l\'script": [7,2,4,0,1],
"rc": [2],
"re": [0],
"includ": [2,[3,5,7]],
"estructur": [0],
"traduccions": [8],
"t0": [3],
"precedida": [0],
"t1": [3],
"t2": [3],
"t3": [3],
"minut": [2,[1,3,4,6]],
"drecer": [0,3,4,7,2,5],
"quedar": [2],
"excepcion": [1,2],
"l\'hauríeu": [0],
"sa": [1],
"sc": [0],
"se": [2,7,4,[0,1],3,5],
"fer-ho": [0,2,[3,5,7]],
"si": [2,0,7,4,1,3,5,6],
"sl": [2],
"pleca": [0],
"crearà": [2,0],
"so": [3],
"lectura": [[0,2]],
"sr": [0],
"següents": [8],
"intern": [2,[0,1,4]],
"quadr": [7,1,[0,3,4],2,6],
"executeu": [[0,7]],
"lar-lo": [1],
"onc": [2],
"one": [[4,7]],
"vostr": [2,7,5,1,[0,3],6],
"interv": [[0,1],[2,4,6]],
"editoverwritesourcemenuitem": [0],
"sencera": [0],
"segon": [0,2,[1,3],7],
"omegat.autotext": [0],
"s\'identifiquen": [3],
"kilobyt": [2],
"enforc": [6,4,[0,2],[1,3]],
"gruix": [1],
"tl": [2],
"tm": [6,2,4,0,7,1,[3,5,8]],
"to": [3,2,7,[4,5],1,[0,6,8]],
"v2": [1],
"destinació": [0,4,7,2,1,6,5],
"document.xx": [0],
"segl": [0],
"sincronitzi": [2],
"viewmarkautopopulatedcheckboxmenuitem": [0],
"seleccion": [[6,7]],
"accedir-hi": [[0,2],[6,7]],
"projectwikiimportmenuitem": [0],
"ue": [4],
"formatació": [7],
"còmodament": [7],
"coincidir": [0,7],
"un": [0,2,7,3,1,4,6,5],
"calcula": [7],
"up": [0,2],
"us": [[4,7],3,2,0,1,5,8],
"newword": [7],
"usual": [4],
"this": [5,7,4,[1,2,3,6]],
"aplicacion": [0,2,[1,7]],
"s\'afegeixen": [2],
"va": [0,2,3],
"sincronitzaran": [2],
"bilingü": [[6,7],2],
"tardar": [1],
"s\'executarà": [7],
"ve": [0],
"opt": [2,0],
"extract": [7],
"establert": [7,[1,2,3]],
"automàtic": [8],
"region": [[0,1,2,7]],
"únic": [7,0,[4,5]],
"operador": [0],
"punt": [0,[1,2],[4,7]],
"changed": [1],
"propagació": [6,[1,2,4]],
"d\'obrir": [2],
"centrar-vo": [3],
"utilitzada": [0,7,2,1],
"identificar": [0,[1,2,3,7]],
"autocompleterlistup": [0],
"licenc": [0],
"identificat": [2],
"desenvolupat": [7],
"sí": [7,0],
"omegat.project.bak": [2,6],
"utilitzades": [8],
"l\'execut": [[0,2]],
"queden": [7],
"orf": [7],
"succeeix": [2],
"slight": [5],
"retorn": [7,4,0,1],
"executar": [2,7,[0,1,4]],
"projectaccessexporttmmenuitem": [0],
"previous": [[3,4,5]],
"licens": [0],
"org": [2],
"lar-hi": [0],
"efect": [0,7,1],
"instànci": [2],
"d\'allotja": [2],
"d\'execució": [2,0],
"prioritat": [[1,4],2,3],
"superior": [[1,5],[2,7]],
"funcionar": [2],
"té": [0,2,[4,7],1,5],
"recuperar-lo": [2],
"declaració": [0],
"especifiqui": [0],
"xx": [0],
"plena": [[0,3]],
"suport": [7],
"sourc": [2,7,6,4,5,3,[0,1]],
"propietats": [8],
"panxacont": [0],
"incloent-hi": [0,[4,6],[1,2]],
"estaran": [0],
"s\'excloguin": [2],
"imatg": [0],
"type": [2,[0,1,3,6,7]],
"problem": [2,0,4,[1,3,5,6]],
"connexió": [[1,2],5],
"administrar": [2],
"problemàt": [[2,3]],
"terms": [8],
"detallad": [2],
"optionsautocompletehistorypredictionmenuitem": [0],
"l\'inform": [[0,3]],
"d\'acord": [7,4],
"yy": [0],
"alfabètica": [7],
"ortogràf": [1,7,4,6,0,3],
"d\'obrir-n": [4],
"come": [[0,3]],
"seqüència": [0,3],
"qualitat": [7,4],
"quant": [0,[3,4,7]],
"abandoneu": [3],
"coma": [0,2],
"push": [2],
"contactar": [5],
"exist": [2,3,5,7,[0,1,6]],
"l\'equival": [2],
"readme_tr.txt": [2],
"minúscul": [0,[4,7],1],
"creant": [2],
"segur": [1,[3,4,8]],
"penalti": [6],
"exact": [7,4,[0,2,3,6]],
"propi": [7,5,[0,2,3]],
"l\'emplena": [0,1],
"seleccioneu-n": [5],
"utf8": [0,[4,7]],
"copi": [[0,3]],
"columna": [7,[0,4]],
"desenvolupeu": [2],
"precisa": [7],
"dark": [1],
"continueu": [2],
"funcion": [0,3,7,[4,5,6]],
"packag": [2],
"pròpiament": [0],
"instància": [2],
"context_menu": [0],
"editsearchdictionarymenuitem": [0],
"aquí": [5,0,1,2,[6,7],4],
"tag-valid": [2],
"porció": [0],
"alway": [2],
"help": [2,8],
"organitzar": [0],
"pugui": [[2,7],1],
"qualsevol": [0,2,7,6,3,4,5,8],
"ajustar": [5,3],
"organitzad": [0],
"repositori": [2,6],
"date": [0],
"retorna": [7,5],
"corr": [7],
"data": [[1,2],7,[3,6]],
"lowercasemenuitem": [0],
"wiki": [[2,6]],
"autocompleterconfirmwithoutclos": [0],
"separ": [7,[0,4],[1,2,3]],
"norm": [0],
"endav": [0,2,[3,4],[5,6]],
"quatr": [0,[2,4,7]],
"cosa": [[2,7],[0,1,4]],
"filepath": [1,0],
"enteneu": [2],
"create": [8],
"replac": [7],
"sens": [0,7,2,1,4,3,[5,6]],
"like": [2],
"divisió": [1,[0,7]],
"haurà": [2],
"nomé": [0,7,[1,2],3,4,5,6],
"bloc": [7,0,5,3],
"mestr": [0],
"reutilització": [2,[1,3,4]],
"comú": [3],
"eines": [8],
"s\'emmagatzemaran": [[0,1]],
"here": [[1,3,7]],
"note": [5,3,[0,4],7],
"cantonada": [5],
"l\'actualització": [2],
"concreta": [2],
"purpose": [8],
"line": [2],
"s\'inicia": [2,[1,4]],
"avanceu": [2],
"git": [2,6],
"exportat": [7,[4,6]],
"enllà": [0],
"còpia": [2,6,[1,4,7]],
"continuar": [[2,3]],
"nota": [2,0,7,4,1,5,3,6],
"inclou": [7,2,0,1,[5,6]],
"xx-yy": [0],
"exportad": [2,6,[0,3,4,7]],
"will": [2,3,7,5],
"follow": [0,7],
"l\'obrin": [2],
"considera": [0],
"dreta": [0,7,5,4,3],
"targetlang": [0],
"frase": [0,7,3,1,2],
"abreujada": [0],
"arbitrari": [2],
"optionssetupfilefiltersmenuitem": [0],
"inversa": [0,2],
"altgraph": [0],
"stats-typ": [2],
"nove": [0,[3,4],6],
"consisteix": [[0,3]],
"idèntic": [7,[2,6],[1,4]],
"conec": [0],
"alinearà": [2],
"converteix": [0,4],
"remota": [2],
"omegatl": [2],
"adrec": [[0,5]],
"your": [2,3,5,[4,8]],
"s\'obr": [2,7,3],
"without": [5],
"s\'agregaran": [0],
"xml": [0,2,1],
"menor": [6],
"d\'evitar-ho": [7],
"nova": [2,4,7,[0,3],6,[1,5]],
"obriu": [2,6,[0,4,7]],
"obrir": [2,7,3,[4,5],0],
"sometim": [0],
"ocult": [6],
"sistem": [2,0,[1,4],6],
"introduint-l": [3],
"neutral": [0],
"nous": [[2,7],[0,3],4],
"l\'extensió": [0,2,[1,4,7]],
"xdg-open": [0],
"ambigua": [0],
"befor": [2],
"puntuacion": [6],
"caràcter": [0,7,4,5,1,[2,3]],
"creeu-n": [3],
"tar.bz": [6],
"analitza": [0],
"registrar": [2,[1,4,5]],
"independent": [2],
"seus": [7],
"registrat": [[0,2,7]],
"seva": [[0,3,5]],
"shebang": [0],
"dividid": [0],
"editorskipprevtoken": [0],
"utilitzad": [3,[0,1]],
"negreta": [1,[0,5,7],3],
"utilitzar": [2,7,0,[1,3,4,5],6],
"dividir": [7,[0,1,4]],
"dividit": [3],
"d\'excepció": [[0,1],3],
"utilitzat": [7,3,2,[0,1]],
"d\'oct": [1],
"aaaa": [2],
"creada": [5],
"produït": [2],
"gnu": [2,8],
"desassignar": [7],
"d\'estadístiqu": [6,[2,7]],
"blue": [5],
"modificar-lo": [[0,3],[6,8]],
"registrad": [5],
"suzum": [1],
"target.txt": [[0,1]],
"temurin": [2],
"boton": [7,4,[0,3]],
"buid": [0,5],
"standard": [2],
"d\'espac": [2],
"descripcion": [3],
"l\'interior": [2],
"apareixeran": [0,[1,3,7]],
"correct": [[5,7],[0,2,3,6]],
"stdout": [0],
"lade": [4],
"traduct": [5],
"d\'espai": [0,7,3],
"substituir": [[1,3,5],[4,7]],
"idèntica": [4,[0,1,7]],
"canadenc": [1],
"buit": [0,[1,2,5,6],3],
"nameon": [0],
"diferència": [[0,1,7]],
"encadenar": [0],
"trencarà": [1],
"gotonextnotemenuitem": [0],
"gpl": [0],
"pas": [7,2,[0,6]],
"l\'acció": [3],
"l\'apach": [[2,7]],
"s\'insereixen": [6,2],
"newentri": [7],
"especial": [[2,7]],
"list": [3,7,2],
"coneix": [2],
"s\'enumeren": [2],
"autocompleterprevview": [0],
"segmentar-s": [[0,3]],
"seve": [0],
"serà": [[0,3,7]],
"decisió": [6],
"combinar-s": [3],
"formats": [8],
"reinicia": [4,0],
"utilitzen": [0,2,7,[1,4],[3,8]],
"projectcommittargetfil": [0],
"determin": [1],
"utilitzem": [3],
"navegar": [[3,5,6],2],
"po4a": [2],
"combin": [7],
"japonai": [7],
"omegat.org": [2],
"utilitzeu": [7,0,3,1,2,5,6,4],
"menus": [0],
"claudàtor": [0],
"incrustacion": [0],
"maxprogram": [2],
"with": [3,[2,4],[1,5]],
"pdf": [2,0,4,7],
"ordenar": [[0,7]],
"there": [7],
"mentr": [5,[0,3,7],[1,2,4]],
"autocompletertabledown": [0],
"editornextsegmentnottab": [0],
"toolsshowstatisticsmatchesmenuitem": [0],
"viewdisplaymodificationinfononeradiobuttonmenuitem": [0],
"alinear-lo": [7],
"viceversa": [7],
"assaboriu": [3],
"sincronitzat": [[1,2]],
"pel": [4,2,7,5,0,[1,3]],
"sincronitzar": [3],
"procedi": [2],
"per": [0,2,7,1,4,3,5,6,8],
"paí": [2],
"write": [4],
"gtk": [1],
"canviar": [[5,7],[0,2],4,[1,6]],
"peu": [0],
"project_save.tmx.bak": [[2,6]],
"period": [0],
"canviat": [3,2],
"proceed": [4],
"tancament": [0],
"carrega": [[1,2,7],6],
"incorrecta": [6],
"projectaccesswriteableglossarymenuitem": [0],
"even": [2],
"reordenar": [[4,5]],
"considereu": [7],
"application_shutdown": [7],
"lliurament": [3],
"autocompletertablelastinrow": [0],
"gui": [2,[3,7],[5,6]],
"consideren": [6],
"regexp": [0],
"sentencecasemenuitem": [0],
"registrin": [2],
"abreujad": [0],
"concret": [0],
"nativa": [2,1],
"corrent": [7],
"editorcontextmenu": [0],
"utilitzin": [2],
"imperativa": [7],
"acostumeu": [2],
"difer": [2,1,0,[4,5],7,3,6],
"optionssentsegmenuitem": [0],
"pública": [2],
"robust": [3],
"desfé": [[0,4]],
"optionsaccessconfigdirmenuitem": [0],
"distribueix": [2],
"charact": [2],
"seqüènci": [0],
"framework": [2],
"test.html": [2],
"s\'utilitzaran": [0],
"php": [0],
"xxx": [6],
"smalltalk": [7],
"desen": [1,[4,7],0],
"això": [2,0,7,5,1,[3,4,6]],
"pseudotranslatetmx": [2],
"whether": [1],
"xarxa": [2],
"sincronitzen": [2],
"coincideixi": [0],
"l\'obri": [2],
"representa": [0,1,2],
"targetlanguagecod": [0],
"repetició": [5,7],
"editorprevsegmentnottab": [0],
"absolut": [0],
"així": [0,2,[1,3],7],
"continuació": [0,2,7,[3,5],6],
"fletxa": [7],
"valdrà": [3],
"començaran": [3],
"disabl": [[1,4]],
"massa": [[1,2]],
"ordres": [8],
"decoracion": [3],
"causa": [3],
"acostumad": [4],
"s\'obri": [[0,7]],
"diccionaris": [8],
"diaposit": [0],
"registren": [0],
"d\'express": [0,[2,3]],
"primera": [[0,2,4,5,7],[1,6]],
"entrad": [0,[1,5,7],[3,4]],
"design": [2],
"representi": [0],
"alfabèt": [[0,5]],
"consecutius": [7],
"perquè": [0,6,[1,2,3],7,8,[4,5]],
"pla": [1],
"combinar": [[0,2,3,6]],
"contingui": [2,[0,3,6,7]],
"canònica": [0],
"seccion": [[3,5]],
"deseu": [[0,2,6],[3,7]],
"l\'entrada": [4,[0,5]],
"substitueix": [7,0,4,1,5,[2,3]],
"projectnam": [0],
"l\'heu": [2],
"contingut": [0,7,2,3,[1,6],4,5,8],
"simplifica": [1],
"traduir": [7,0,[2,3],[4,5],1,6],
"installdist": [2],
"postprocessament": [8],
"a-z": [0],
"recuperar": [[2,6]],
"utilitzeu-la": [2],
"malgrat": [[0,7]],
"d\'estudiar": [8],
"gotonextxenforcedmenuitem": [0],
"editordeleteprevtoken": [0],
"modificació": [[0,1],[2,4,7],5,6],
"baixeu": [2,6],
"modifiqueu": [7,[0,1,3]],
"assegureu-vo": [2,1,[0,5,7]],
"requerir": [5],
"creieu": [7],
"want": [[3,7]],
"utilitat": [2],
"javascript": [7],
"marqu": [7,0,5],
"manera": [[0,2],3,7,[1,6],4,5],
"mediawiki": [[4,7],[0,3]],
"toolkit": [2],
"editeu": [5],
"join.html": [0],
"limita": [2],
"s\'identifica": [1],
"poc": [4,3,[2,7]],
"omegat.kaptn": [2],
"giving": [8],
"conté": [6,0,7,2,5,[1,3,4]],
"etiqueta": [1,0],
"pot": [2,7,0,[4,5],3,[1,6]],
"numeren": [5],
"canal": [0],
"freez": [2],
"pena": [[3,7]],
"d\'afegir-lo": [2],
"avisa\'m": [5],
"nombr": [7,5,0,[1,2,4],6,3],
"generen": [2],
"d\'assignació": [2,7],
"agrupar": [5],
"coincidirà": [0,1],
"contrasenya": [2,1],
"d\'escriptura": [[0,5],2,[1,4,7,8]],
"mesura": [[0,1,2]],
"parcials": [8],
"diagram": [0],
"d\'aplicacion": [2],
"s\'intenta": [2],
"googl": [1],
"opendocu": [0],
"traduïts": [8],
"convenció": [0],
"procediu": [2],
"l\'estableix": [2],
"definirà": [[6,7]],
"s\'ignorarà": [[0,2]],
"gotoeditorpanelmenuitem": [0],
"deixar-lo": [5],
"caràcters": [8],
"travé": [2],
"viewmarkfontfallbackcheckboxmenuitem": [0],
"requerirà": [2],
"adjac": [5],
"insertcharsrlm": [0],
"d\'inici": [1,[0,2],4],
"l\'habiliteu": [0],
"sourceforg": [2,0],
"escrivint": [2],
"continua": [[3,7]],
"han": [0,1,3,2],
"semeru-runtim": [2],
"baixar": [2,[0,1]],
"vora": [5],
"given": [2],
"baixat": [2],
"canvieu": [1,2,7,[0,3,6]],
"editmultipledefault": [0],
"mozilla": [[0,2]],
"editfindinprojectmenuitem": [0],
"consulteu-la": [0],
"pro": [1],
"implica": [7],
"l\'estat": [2,6,[0,5,7],[1,3,4]],
"warn": [2],
"canvien": [0],
"conserva\'l": [7],
"plural": [0],
"projectaccessourcemenuitem": [0],
"detecta": [4,[1,2]],
"formada": [0,5],
"defineixi": [0],
"detal": [2,4,7,0,1,3,6,5],
"proporcionar": [[0,5]],
"larà": [1],
"proporcionat": [2,1],
"oberta": [[2,4,7]],
"perd": [4],
"exporta": [0,4,1],
"tinc": [0],
"pere": [0],
"preocupeu": [3],
"l\'etiqueta": [[0,1,4],3],
"dinàmic": [7],
"numerat": [[0,5]],
"confirmar": [6,7],
"duckduckgo.com": [1],
"yet": [1],
"agrupeu": [1],
"tipus": [7,0,2,1,[4,5,6]],
"configura": [1],
"colour": [[1,4,6]],
"ràpidament": [7,3,2],
"chang": [3,[1,6]],
"reflecteix": [4],
"s\'inicien": [[1,7]],
"time": [2,3],
"kanji": [0],
"actuarà": [6],
"program": [[0,2,7]],
"desacoblar-la": [5],
"put": [2],
"python3": [0],
"hem": [0],
"tinguin": [0,1],
"carregarà": [2],
"heu": [2,3,0,[4,5],7,[1,6]],
"sortida": [2,1,[0,8]],
"precedid": [0],
"convertidor": [2],
"tran": [0],
"iraq": [0],
"dossier": [5],
"precedit": [0],
"descriuen": [2,[0,3,5]],
"cercar": [0,7,1,[2,3,5]],
"separa": [1],
"ocultar": [5],
"però": [0,2,7,6,[1,3],4,5],
"engin": [5],
"tractarà": [[0,5]],
"pròpi": [2],
"aprendr": [0,3],
"personalitza": [[1,4]],
"alien": [0],
"esperat": [2],
"doc-license.txt": [0],
"facilita": [3],
"theme": [1],
"チューリッヒ": [1],
"annexo": [0,[3,6]],
"sessió": [1,0,[3,5,7,8]],
"temeu": [2],
"editor": [7,3,[0,5],4,1,6,8,2],
"pseudotranslatetyp": [2],
"confirmeu": [4],
"l\'iniciador": [2],
"veieu": [[3,7]],
"peus": [0],
"edició": [[0,7]],
"anteriors": [8],
"distribuït": [7,0],
"intentarà": [7],
"d\'amplada": [7],
"obri": [0],
"accedeix": [0,1],
"costat": [0,[5,7]],
"prement": [7],
"tingueu": [7,[0,1,2,4]],
"projectclosemenuitem": [0],
"admeti": [7],
"viewmarknonuniquesegmentscheckboxmenuitem": [0],
"bibliotequ": [0],
"emplenament": [8],
"sobr": [[2,7],5,4,1,0,6,3],
"tres": [0,[2,4,6],[3,5,7]],
"tret": [2],
"davant": [0],
"proporcion": [1],
"s\'ignoraran": [0],
"obren": [4],
"aplicació": [2,3],
"findinprojectreuselastwindow": [0],
"d\'opció": [7],
"d\'oracl": [0],
"readme.txt": [2,0],
"admeso": [[1,4]],
"comprèn": [6],
"languagetool": [4,1,[7,8]],
"lliure": [8],
"source.txt": [[0,1]],
"files.s": [7],
"associada": [4,[0,2,3,7]],
"histori": [1],
"exchang": [0],
"produeix": [[2,4,5]],
"varietat": [2],
"quantitat": [2],
"substituiran": [7,1],
"currseg": [7],
"generat": [[0,1]],
"point": [7],
"general": [[0,2],7,1,5,8,[3,4]],
"identifica": [1],
"generar": [[2,3]],
"generad": [2],
"torna": [7,4,0,[2,3,6]],
"facil": [3],
"process": [3],
"autocompletertrigg": [0],
"marcarà": [0],
"alternativa": [[0,7],4,5,2,[1,3]],
"confú": [4],
"desblocar-lo": [5],
"conegut": [0],
"esperen": [0],
"acquiert": [1],
"inclour": [2,5,[0,3,6]],
"visualització": [5,1,7,0,[3,4,8],6],
"esperem": [3],
"account": [1],
"been": [4],
"dhttp.proxyhost": [2],
"especificat": [2,1],
"especificar": [[0,2]],
"cercarà": [1,7],
"oculta": [6],
"diverso": [0,2,7,4,3],
"barra": [0,5,[2,3,8]],
"editorprevseg": [0],
"acabat": [[3,7]],
"marca": [[0,4],7,1,5,[2,3,6]],
"sobreescriguin": [[2,3,6]],
"ignorat": [0,7],
"ignorar": [0,4],
"a-za-z0": [0],
"antigu": [[2,7]],
"you": [[2,3],7,5,[1,6,8],4],
"s\'emplena": [6],
"protegid": [3],
"aban": [2,0,1,7,3,[4,6],5],
"www.apertium.org": [1],
"ignorad": [0],
"activi": [0],
"correcció": [1,[2,7],6],
"d\'aparèix": [0],
"passeu": [4],
"corrector": [1,[6,7],[0,4],[3,8]],
"carregad": [6],
"activa": [0],
"selectiu": [2],
"project_save.tmx.tmp": [2],
"protegit": [1],
"protegir": [1],
"configur": [[2,3,5],1],
"nativ": [0],
"d\'adreça": [0],
"convencions": [8],
"segueix": [2,0],
"carregat": [1],
"carregar": [7,4,0,2,[3,6]],
"dòlar": [0],
"traductor": [2,3,[0,1],[5,7]],
"mega": [0],
"obrir-lo": [7,[0,2,3,5,6]],
"subratllada": [5],
"zurich": [1],
"空白文字": [2],
"eviteu": [2],
"filtrant-lo": [3],
"premuda": [7],
"evitem": [0],
"optionsworkflowmenuitem": [0],
"integrat": [1],
"how": [2],
"apareix": [[5,7],3],
"releas": [2,0],
"comet": [0,7],
"qaf": [0],
"encapsulad": [7],
"segmentar": [0,[3,7]],
"segmentat": [0],
"processar": [[0,2,5,7]],
"d\'unicod": [0,4],
"s\'indiqui": [0],
"l\'element": [0,[3,5,7]],
"indicador": [[1,5],0],
"dictroot": [0],
"serien": [2],
"extensa": [3],
"antiga": [2],
"xhmtl": [0],
"l\'associació": [0],
"confirmació": [[1,7]],
"d\'exclour": [7],
"nom_del_project": [7],
"reutilitza": [4],
"postprocessa": [[0,7],1],
"acaben": [0],
"manteniu": [7],
"hold": [3],
"mantenir": [[5,7]],
"subdir": [2],
"acabeu": [0],
"delimitar": [7],
"mostra": [4,7,5,1,0,2,3],
"autocompletertableleft": [0],
"demanarà": [7,[1,3]],
"simplement": [0,[2,7]],
"processar-l": [1],
"definida": [[0,2]],
"mostri": [0,4],
"extreu-n": [6],
"immediata": [0,[3,7],[1,2,4]],
"passar": [[1,3,4,5,7]],
"creeu": [2,[0,3,6],[5,7]],
"passat": [2],
"d\'id": [0],
"take": [3],
"d\'ein": [4],
"s\'assigna": [2,5],
"plantilla": [1,0,8,7],
"traducció": [2,3,5,7,4,1,0,6,8],
"algun": [0,2,[1,5],6,[4,7]],
"editorlastseg": [0],
"inhabilita": [0,2],
"file-source-encod": [0],
"dictionaries": [8],
"some": [3,2],
"ubicacion": [2,7,[0,1,3,4,5,6]],
"tant": [2,[0,7],[3,5]],
"session": [2],
"entr": [0,7,2,5,1,4,3],
"divis": [7,1],
"criteri": [7,[0,3]],
"selecció": [7,4,0,5,[1,2]],
"petit": [[0,7]],
"alpha": [2],
"s\'insereix": [7,[1,5]],
"selector": [2],
"blanc": [0,4],
"大学院博士課程修了": [1],
"just": [0,[2,3]],
"alterar": [7],
"sola": [0,3],
"editexportselectionmenuitem": [0],
"solucionar-ho": [2],
"home": [[0,2]],
"hauríeu": [[2,3,7]],
"disable-location-sav": [2],
"although": [7],
"projectaccesstargetmenuitem": [0],
"increïbl": [0],
"iana": [0],
"assegurar-vo": [1,2],
"fiabl": [6,[0,2]],
"visibl": [0,6],
"cercar-la": [3],
"desarà": [4],
"aligndir": [2,7],
"system-host-nam": [0],
"action": [2],
"mymemory.translated.net": [1],
"creat": [2,0,7,3,[4,5],[1,6]],
"python": [7],
"cercar-lo": [[0,3]],
"segueixin": [2],
"ignorin": [0],
"vacant": [0],
"crear": [2,0,7,3,6,[1,4]],
"canviant": [7],
"tarbal": [6],
"ortogràfic": [8],
"operatius": [6],
"afectin": [2],
"atenció": [0],
"deixa": [[1,5]],
"llengua": [2,0,1,[6,7],5,[3,4]],
"hora": [3,0],
"d\'índex": [0],
"deixi": [7],
"constitueixi": [0],
"file": [[2,7],5,0,3,4],
"creen": [6,[0,3,4,7]],
"fila": [7,[0,4]],
"preferireu": [2],
"moneda": [0],
"múltipl": [2],
"independ": [7,2,[0,5],[1,3,6]],
"meni": [2],
"atenuada": [4],
"tard": [7],
"menu": [3,4],
"elimineu": [2],
"mena": [1,[2,3]],
"positius": [1],
"probabl": [[2,3,5]],
"aconseguir": [2,[0,3,7]],
"return": [3],
"prenen": [0],
"forçad": [6],
"invoke-item": [0],
"comenceu": [[0,2]],
"comencen": [0],
"forçar": [[0,1,4,7]],
"desitjada": [5,[1,2]],
"preneu": [6],
"forçat": [6],
"source-pattern": [2],
"obrint": [5],
"find": [7,2],
"ondulad": [1],
"següent": [0,2,4,7,5,3,[1,6],8],
"estrella": [0],
"freqüent": [0,[2,3,4,7]],
"l\'openxliff": [2],
"centreu-vo": [2],
"autocompletertablepagedown": [0],
"problema": [2,5],
"accepti": [[2,6]],
"s.l": [0],
"task": [2],
"física": [2],
"associar-n": [3],
"xliff": [2,0],
"true": [0],
"s\'executa": [2],
"present": [1,[0,7]],
"majúscul": [0,4,7,1],
"l\'estil": [2],
"groovi": [7],
"evitar": [[0,2],3],
"meso": [3],
"desactivar": [4,7],
"best": [1],
"personalitzar": [0,[1,7]],
"cread": [7],
"personalitzat": [0,2],
"execut": [0,2],
"més": [0,2,7,4,3,1,6,5],
"menú": [0,4,5,[1,7],3,8,2,6],
"d\'aquest": [6,0,2,[3,7]],
"modific": [0,[2,4,6,7],5],
"actius": [1],
"especificada": [[0,2]],
"compacta": [1],
"s\'inseriran": [2],
"baixa": [2,[0,7],4],
"impliquin": [2],
"personalitzad": [0,[1,2],4,3],
"extensió": [0,[2,5,6]],
"l\'exempl": [0,2],
"abov": [[1,3,5]],
"messageformat": [1,0],
"controlador": [1],
"ajudarà": [[2,4]],
"tracta": [[1,7]],
"tingui": [5,1,[0,2,3,7]],
"master": [2],
"taul": [0,1],
"tracti": [0],
"distribucion": [2],
"desactivat": [7],
"especificad": [[1,7]],
"writer": [0],
"introduir-l": [3],
"sota": [5,[2,4,6]],
"dalloway": [1],
"rubi": [7],
"resource-bundl": [2],
"subratlla": [4],
"meus": [0],
"representació": [[0,7]],
"globals": [8],
"external_command": [6],
"programa": [2,3,0,[1,4,5,8]],
"seran": [1],
"editorselectal": [0],
"corregir": [[1,2,4]],
"dipòsit_de_tots_els_fitxers_d\'origen_del_projecte_en_equip_de_l\'omegat": [2],
"llenguatg": [7],
"coincidències": [8],
"annexos": [8],
"corregiu": [2],
"carabassa": [5],
"temporal": [[1,5]],
"immedi": [5],
"emplenar": [[4,6]],
"descarten": [7],
"edita": [7,0,5,3,1,4,8],
"omegat-default": [2],
"mètode": [2,5],
"mestra": [1],
"basen": [[0,7]],
"user.languag": [2],
"d\'un": [2,0,[3,7],4,1,6,5,8],
"regex": [0],
"l\'especificació": [0],
"meta": [0],
"línia": [0,2,7,1,5,[3,4,6]],
"except": [2,0,3],
"comprovació": [1,7,4],
"proporcionada": [4],
"global": [7,0,1,4,[2,3,5]],
"afectar": [2],
"racin": [5],
"de10": [6],
"ressources": [8],
"trencat": [[2,3]],
"trencar": [0],
"màxima": [[0,2]],
"arrossegueu": [2],
"alineador": [4],
"free": [7],
"d\'usuari": [2,0,[3,8],[1,4,5]],
"emplenat": [6],
"modificador": [0,3,4],
"valor": [0,[1,2,7],4],
"aparença": [1,8],
"esborra": [4],
"diccionari": [1,6,5,0,4,7,[2,3]],
"addicion": [0,[1,7],2,[3,4,5]],
"desar": [7,2,0],
"desat": [[4,5],[0,1]],
"ibm": [[1,2]],
"aplic": [0],
"comun": [0,2],
"sovint": [3,[0,2,7]],
"precaucion": [2],
"validació": [[0,1]],
"subratllarà": [1],
"afegir-n": [7],
"multiparadigma": [7],
"exhaustiva": [[0,2]],
"five": [1],
"d\'antiguitat": [2],
"esborrar": [3],
"reconegui": [0,[2,3]],
"basad": [4],
"ajusta": [[6,7]],
"d\'afegir": [[0,3,7]],
"necessitat": [1,3],
"omegat-cod": [2],
"reconegut": [0],
"amagar": [1],
"guia": [3,6,[0,4],[2,7],[1,5]],
"idx": [6],
"conflict": [2,[0,3]],
"que": [0,2,7,1,3,6,4,5,8],
"decoratius": [3],
"canonad": [0],
"emplen": [2],
"fixa": [6],
"qui": [1],
"detect": [1],
"membr": [2,3,5],
"xifrat": [0],
"ajusto": [7],
"autocompleterconfirmandclos": [0],
"projectaccesscurrentsourcedocumentmenuitem": [0],
"compartit": [2,7,[3,5,6]],
"basat": [3,7,[0,8]],
"compartir": [2,[3,6,7]],
"linux": [0,2,4,5,[1,3,7]],
"repeticion": [[0,4],7],
"linux-install.sh": [2],
"compt": [2,7,0,[1,4],6,3],
"sembla": [[3,4]],
"inclogui": [[2,7]],
"inferior": [7,1,5,4],
"again": [3],
"s\'habilita": [[1,5]],
"openxliff": [2],
"ifo": [6],
"comment": [0,5],
"substitueix-ho": [7],
"substitució": [7,0,4,[3,8]],
"memòria": [2,[3,7],6,[0,1],[4,5]],
"icona": [4],
"uncolor": [5],
"ignorarà": [6],
"accelerar": [2],
"optionsmtautofetchcheckboxmenuitem": [0],
"lliurement": [[0,5]],
"abreviatura": [0],
"sistema": [2,0,7,4,[1,5],[3,6]],
"xx.docx": [0],
"prefix": [6,1,2],
"transformació": [1],
"món": [2],
"consist": [4],
"utilitzi": [[0,2]],
"reconèix": [[0,3,7]],
"habiliteu": [0,[4,6],[2,5,7]],
"editorshortcuts.properti": [0],
"utilitza": [0,[1,7],4,2,[3,5,6]],
"juntament": [0,7,2],
"reflexioneu": [3],
"diverses": [8],
"necessiteu": [0,2,3],
"fail": [7],
"versions": [8],
"necessiten": [2],
"externes": [8],
"obtindreu": [2],
"inicia": [2,0,7],
"cian": [4],
"insereixi": [6],
"arrel": [0,2],
"què": [0,2,7,[3,4],5],
"l\'ha": [0],
"spellchecking": [8],
"fàcil": [3,[0,2],[1,7]],
"regió": [0],
"d\'object": [[0,7]],
"requir": [3,7],
"caden": [0,7,3,2],
"fonament": [0],
"tmotherlangroot": [0],
"d\'inserir": [1],
"modificades": [8],
"usuari": [2,[1,7],0],
"l\'abast": [0],
"viewmarknotedsegmentscheckboxmenuitem": [0],
"esquerra": [0,[4,5,7]],
"event": [0],
"obriu-la": [2],
"gestionar": [3,1],
"gotomatchsourceseg": [0],
"s\'inclou": [[2,5]],
"appropri": [[3,7]],
"excel": [0],
"memòri": [2,[3,6,7],0,[1,4,5]],
"comma": [0],
"l\'aspecte": [8],
"trobareu": [[0,2]],
"regla": [0,1,[3,7]],
"stardict": [6],
"omegat.l4j.ini": [2],
"first": [5],
"span": [0],
"prefer": [4,[2,3]],
"número": [0,[2,4],[1,7],[3,5,6]],
"space": [0,5],
"l\'hora": [7,[2,4]],
"ドイツ": [7,1],
"tecla": [0,7,[1,5],4],
"l\'interv": [[0,1],2],
"pujar": [2],
"habilitat": [4,[0,1,3,5]],
"pujat": [2],
"from": [[5,7],[2,6]],
"habilitar": [5],
"assignació": [2,7],
"l\'okapi": [2],
"activar-l": [4],
"blaus": [7],
"editselectfuzzy3menuitem": [0],
"emmagatzema": [[0,3],2],
"l\'id": [0],
"comprimir": [6],
"lustra": [3],
"fals": [0,2],
"project.projectfil": [7],
"fossin": [2],
"sobreescriu": [5,[3,4],[0,2]],
"error": [4,2,[1,3],[0,7],5],
"marcador": [1,0,5,2],
"canviar-n": [[0,6,7]],
"adoptat": [0],
"shortcut": [3,[0,4]],
"immutabilitat": [6],
"public": [8],
"disposicion": [[1,7]],
"s\'enganxarà": [4],
"track": [2],
"annex": [0],
"tmx2sourc": [[0,2,6]],
"contra": [2],
"l\'obr": [7],
"pome": [0],
"sobreescriuen": [[0,1,2]],
"ini": [2],
"poma": [0],
"dipòsit_de_tots_els_projectes_en_equip_de_l\'omegat": [2],
"proced": [6],
"visualitzador": [5],
"dhttp.proxyport": [2],
"negar": [0],
"distingeixen": [0],
"esdevé": [6],
"subrip": [2],
"l\'altra": [7,2],
"unir-vo": [3],
"s\'obren": [1,5,4],
"score": [1],
"recordar": [[3,7]],
"persona": [7,2],
"descriu": [0,[2,5]],
"passo": [2,3,0,7],
"descrit": [[0,2,5,7]],
"raw": [2],
"actualitzacion": [1,2,[0,4]],
"tornar": [[2,5],[3,7],[1,4],0],
"emmagatzemarà": [7],
"titulat": [0],
"extensions": [8],
"mapatg": [2],
"l\'esquerra": [[0,4]],
"crèdit": [4],
"copia": [7,[1,5],4],
"introducció": [[3,8],[2,4]],
"donar-li": [0],
"canvia": [0,[4,7],1],
"primer": [0,7,[2,3],[1,4],5],
"preferida": [0],
"carpetaexclosa": [2],
"dubtosa": [7],
"manual": [0,[4,7],[2,3],8,6],
"registraran": [0],
"almeni": [[2,7]],
"funciona": [2,0,[1,4,7]],
"aspect": [3,0],
"distribució": [7,2,0],
"poqu": [[2,4]],
"oficial": [8],
"close": [[2,3,7]],
"empaquetad": [2],
"exclosa": [2],
"fase": [[2,7]],
"abc": [0],
"abl": [2],
"textual": [7,0],
"toolbar.groovi": [7],
"seleccionat": [4,7,5,0,2],
"excloso": [7],
"funcioni": [8],
"seleccionar": [7,[0,1,4],5,2],
"vermel": [[0,1,6,7]],
"llatin": [0],
"seleccionad": [7],
"d\'autenticació": [2,1],
"iso": [[0,2]],
"definicion": [[0,1]],
"n\'utilitza": [2],
"farà": [0],
"optionspreferencesmenuitem": [0],
"dubt": [[0,6]],
"bidireccion": [0,4,[2,7]],
"concord": [4],
"red": [6],
"necessàries": [8],
"desapareguin": [2],
"glossary.txt": [[2,6],[0,4]],
"finish": [7],
"rep": [6],
"tracten": [0],
"recordeu": [2,3,[0,5]],
"gestioni": [2],
"add": [[2,3,5]],
"incondicional": [6],
"res": [[2,3,4]],
"casella": [7,0,1],
"torneu": [3,2,[0,6,7]],
"raó": [0,4],
"accé": [4,2,[0,1,3,7]],
"sèrie": [0,3,[1,4,7]],
"llegir": [7,[0,2],1],
"gestiona": [2],
"equival": [7,1,0,5],
"apareixen": [[5,7]],
"convencion": [3],
"l\'anterior": [2],
"rfe": [7],
"shell": [0],
"port": [2],
"entry_activ": [7],
"s\'inclouen": [2,7],
"optionsautocompleteshowautomaticallyitem": [0],
"gotoprevxautomenuitem": [0],
"l\'ani": [2],
"indicad": [[2,7]],
"miler": [0],
"basada": [0],
"puntuació": [7,0],
"prevent": [7],
"enviar": [2,1],
"calgui": [2,7],
"situada": [[0,2]],
"enllaç": [1,0],
"duen": [4],
"ishan": [0],
"vós": [6],
"benefici": [7],
"constituirà": [0],
"espac": [2],
"modifi": [[2,4],6],
"executar-s": [2],
"tractar": [2,[0,1],7],
"l\'so": [0],
"tractat": [0],
"d\'alineació": [7],
"espai": [0,7,4,[1,5],3],
"activeu": [1,[6,7]],
"seleccioneu": [4,7,1,2,5,[0,3]],
"benvinguda": [3],
"filtra": [7],
"exempl": [0,7,2,1,[4,5],3,6],
"s\'adapti": [[0,5]],
"emmagatzematg": [1],
"llista": [0,1,2,7,4,3,6,5],
"indicat": [0,[2,4]],
"seleccionen": [7,4],
"l\'api": [1],
"reinicialitza": [7,[1,4,5],0],
"targetlanguag": [[0,1]],
"indicar": [0,[3,5,7]],
"defecte": [8],
"backup": [2],
"d\'aquesta": [6,1,7,[0,2]],
"tabulació": [0],
"prèviament": [6],
"properti": [2],
"habilitada": [0,1],
"durant": [2,[3,7],0,[4,6]],
"editselectfuzzyprevmenuitem": [0],
"subjac": [7],
"number": [5,[0,7]],
"defect": [0,7,1,[2,4],5,6,3],
"identifi": [[1,2]],
"copiar": [[0,1,2,3,7]],
"desacobla": [5],
"copiat": [[0,2,4]],
"sempr": [0,6,[1,2],[3,4],7],
"rebut": [5],
"multiplataforma": [2],
"dissenyat": [3],
"reconeixerà": [6],
"comptarà": [1],
"distingeixi": [[0,7]],
"distributed": [8],
"emocion": [0],
"script": [7,0,[2,4],1,[3,6]],
"exit": [4],
"etiquetes": [8],
"system": [2,[1,3]],
"tindrà": [0,[1,7],[3,6]],
"pertanyen": [[0,1]],
"spellcheck": [3],
"opinió": [5],
"issu": [0],
"analitzar": [7],
"substituïu-la": [2],
"other": [7,[1,3]],
"darrera": [0,[1,4],2,5],
"locad": [4],
"tabulacion": [0],
"numèriqu": [0],
"local": [2,7,1,0,4,5,6],
"deixaran": [[0,7]],
"resum": [2,[0,3,8]],
"regles": [8],
"segments": [8],
"unitat": [0,7,2],
"robustesa": [3],
"locar": [[1,7]],
"locat": [3,[2,7]],
"memòries": [8],
"d\'interv": [0],
"crea": [7,2,0,4,6,3,5,1],
"mostrada": [2],
"rle": [0,4],
"partida": [[0,1]],
"rlm": [0,4],
"cada": [0,2,4,7,[1,6],3,5],
"proveïdor": [1,5],
"mateix": [[0,2],1,[3,7]],
"l\'estructura": [[0,6,7],[2,3]],
"resta": [0,2,[3,7]],
"precisió": [0],
"s\'hagin": [[2,6,7]],
"entusiast": [3],
"l\'omegat": [2,0,7,3,1,4,6,5,8],
"l\'histori": [4,0,1,3],
"filtr": [0,7,2,1,4,3,6],
"clonació": [2],
"copien": [[2,4,7]],
"correspond": [5],
"c-x": [0],
"mode": [2,7,5,4],
"copieu": [2,6,7],
"substitucion": [4],
"duplicació": [3],
"toolsshowstatisticsstandardmenuitem": [0],
"majúscula": [0,4],
"encara": [7,0,[1,4],2,3,6],
"alac": [0],
"all": [1,3,[4,5,7]],
"read": [0],
"alt": [0,4,[1,5]],
"real": [2,[0,5]],
"tradueix": [[0,2],7],
"pregunta": [0],
"l\'àrea": [7],
"unit": [0],
"etiquetar": [4],
"unir": [7],
"s\'edita": [7],
"amb": [0,2,7,[1,3],4,5,6,8],
"exportades": [8],
"credenci": [1,2,5],
"two-lett": [[3,7]],
"adaptar": [1],
"completat": [2],
"n\'encarrega": [3],
"sortiu": [4,7],
"combinacion": [0,[1,7]],
"sortir": [4,1,[2,7]],
"respondr": [7],
"substituïu-lo": [3],
"dubteu": [3,2],
"tkit": [2],
"and": [3,5,7,2,0,1],
"synchron": [3],
"modifica": [2,[0,5,6,7]],
"sincronització": [2,5],
"desplaceu": [[1,3,5]],
"ani": [[3,5,7]],
"alternatiu": [[0,7]],
"ant": [[2,7]],
"comentaris": [8],
"project_save.tmx.aaaammddhhmm.bak": [2],
"d\'interè": [0],
"rebr": [1],
"creï": [[2,7]],
"voltant": [3],
"pruna": [0],
"altres": [8],
"helplastchangesmenuitem": [0],
"sobreescriurà": [2,7],
"omegat.ex": [2],
"contraccion": [0],
"reason": [2],
"cometeu": [3],
"preescrit": [6],
"sourcetext": [1],
"compten": [4,1],
"cafè": [3],
"compon": [2],
"potenci": [[0,6]],
"jar": [2],
"millora": [3,8],
"api": [7],
"editselectfuzzy2menuitem": [0],
"introduir": [[1,3],7],
"l\'indicador": [1],
"autoallotjat": [2],
"comproveu": [3],
"marquen": [[0,7,8]],
"comproven": [1],
"sintaxi": [0,2,7],
"demana": [4,7],
"iniciïn": [0],
"l\'àmbit": [[0,3,7]],
"oblidat": [0],
"demani": [1],
"marqueu": [[1,7],4],
"nivel": [7,3],
"editornextseg": [0],
"d\'algorism": [0],
"d\'origen": [0,7,4,2,1,5,[3,6],8],
"vida": [0],
"preguntar-ho": [0],
"editselectfuzzynextmenuitem": [0],
"gotonextxautomenuitem": [0],
"seguida": [0,3],
"read.m": [0],
"ara": [0,7,2,6,3,5],
"are": [[1,5],[0,2],[4,7]],
"cloud.google.com": [1],
"taken": [1],
"readme.bak": [2],
"arg": [2],
"where": [[2,5]],
"romandran": [[6,7]],
"retard": [2],
"s\'han": [[1,2],6,[0,3,4,7],5],
"acut": [3],
"regl": [7,1,0,4,2,6],
"delimitador": [1],
"llegiran": [2],
"l\'alfabet": [0],
"retalla": [5],
"d\'edició": [[3,4]],
"encarreguen": [3],
"ask": [2],
"tabul": [2],
"crida": [0],
"restaurarà": [2],
"treballa": [[0,2]],
"refé": [[0,4]],
"compta": [[1,4]],
"toolsshowstatisticsmatchesperfilemenuitem": [0],
"emplena": [0,1,[3,4],[2,5]],
"l\'autenticació": [2],
"cant": [0,7],
"run": [[1,2]],
"assignacion": [2,7],
"d\'instal": [2,[0,1]],
"simultània": [[1,4]],
"either": [[3,7]],
"tercera": [[2,6],[0,5]],
"enrer": [4,0],
"mostrava": [7],
"editorshortcuts.mac.properti": [0],
"molt": [0,[2,3],7,1,4],
"titlecasemenuitem": [0],
"yourself": [3],
"canvieu-n": [2],
"arrossega": [5],
"editcreateglossaryentrymenuitem": [0],
"comprovar": [[1,2,3,5]],
"assistida": [[0,3,8]],
"simplificar": [3],
"ital": [7],
"suprimeix": [[0,7],4,3],
"propietat": [5,2,0,[3,4],6,7,1],
"mostrarà": [1,0,[3,5,6]],
"bold": [[5,7]],
"s\'instal": [2],
"camp": [7,5,4,0,2,1],
"d\'anul": [0],
"ràpid": [4],
"s\'assignen": [2],
"escriur": [7,2,[4,5]],
"s\'obrirà": [[4,5,7]],
"introduc": [7],
"多和田葉子": [7],
"name": [3,0],
"alin": [[3,7]],
"notabl": [2],
"pertin": [3],
"recurso": [7,3,6,[0,2]],
"explorar": [0],
"s\'ordenen": [5],
"show": [5,7],
"cautious": [2],
"camí": [2,0,1,[5,7]],
"comput": [2],
"s\'anomena": [0],
"processament": [8],
"tasqu": [2,7],
"divideix": [7,[0,5]],
"editortogglecursorlock": [0],
"enabl": [5,[1,4]],
"d\'opcion": [2,7],
"associ": [4],
"new_fil": [7],
"fletx": [[3,7]],
"s\'anomeni": [2],
"bona": [2],
"target": [3,1,7,4,6,[0,8]],
"llarg": [0,2,[1,4,6]],
"sobreescriptura": [5,[0,4]],
"compti": [[1,4]],
"config-dir": [2],
"editorskipprevtokenwithselect": [0],
"subfinestres": [8],
"tipogràf": [[4,7]],
"fitxer.txt": [2],
"termbas": [0],
"reben": [[1,7]],
"finestr": [7,[1,3,4]],
"caso": [2,[0,4,7]],
"atè": [3],
"còpi": [2,[0,1,6]],
"l\'administrador": [2],
"case": [2],
"parèntesi": [0,1],
"d\'extens": [2],
"item": [3,4],
"propaga": [[2,7]],
"japoneso": [1],
"corregirà": [2],
"atribut": [0],
"targettext": [1],
"s\'aplicarà": [[0,2]],
"quadres": [8],
"omet": [0],
"cadascun": [[0,2],[5,7]],
"coincideixin": [[0,1]],
"compil": [7],
"conèixer-l": [0],
"fent-hi": [7],
"edittagpaintermenuitem": [0],
"relacionada": [5],
"satisfet": [7],
"aví": [7,[0,1],2,3,4,6,5],
"preferències": [8],
"s\'uniran": [7],
"lloc": [0,2,1,[3,4,5,7]],
"more": [5],
"display": [1,3,[0,5]],
"separad": [[0,3]],
"unicod": [0],
"viewmarknbspcheckboxmenuitem": [0],
"activar": [4,[0,7]],
"suprimir-lo": [[0,6]],
"feina": [3,7,2],
"allà": [1],
"activat": [4],
"tecles": [8],
"consulteu": [2,7,[0,4],3,1,[5,6]],
"habitu": [2,[0,5],7],
"especificació": [7],
"overwrit": [4],
"whitespac": [2],
"credenti": [2],
"l\'abreviatura": [0],
"mour": [7,1],
"simpli": [7,3],
"omnipres": [4],
"msgstr": [0],
"separat": [[0,1,7]],
"difícil": [[2,3]],
"separar": [0,2,7],
"inicialitza": [2],
"untransl": [0],
"núm": [4,0,7],
"nationalité": [1],
"seleccioneu-lo": [0],
"missatg": [5,[0,2]],
"traduíeu": [3],
"most": [[0,2,8]],
"quadrat": [1],
"l\'entorn": [[0,2]],
"establir": [1,4],
"omegat.project": [2,6,3,[1,5,7]],
"preferència": [4,[0,5],2,1,6,7],
"decorat": [3],
"l\'alineador": [7],
"targetcountrycod": [0],
"mitja": [7],
"joc": [0],
"mitjanç": [2,3,[1,5]],
"altr": [2,0,5,[3,7],6,4,1],
"cobreixi": [0],
"insert": [[0,5],6],
"continu": [3],
"plantegeu": [3],
"s\'afegeix": [[0,2,7]],
"highlight": [[4,5,7]],
"manipulació": [5],
"específ": [2,[0,7],[1,3,6]],
"reject": [1],
"d\'ofimàtica": [2,7],
"funció": [0,1,4,[3,5],2,7,6],
"scripts": [8],
"tarong": [0],
"profit": [4],
"executar-lo": [7],
"sal": [0],
"separeu": [0,1],
"d\'escriptori": [2],
"original": [[1,2]],
"concedirà": [2],
"sap": [4],
"recordarà": [2],
"also": [1,0],
"differ": [1],
"conson": [0],
"tancar": [7,[0,5]],
"comentari": [0,5,7,[2,3,4]],
"consol": [2],
"consultar": [0],
"teclat": [0,[4,7],5],
"itokenizertarget": [2],
"viewmarkwhitespacecheckboxmenuitem": [0],
"copiant-lo": [7],
"sbr": [5],
"potenti": [1],
"situació": [2],
"complet": [0,1,[2,5]],
"demanar-li": [2],
"bak": [2,6],
"segona": [4,[0,2,5,7]],
"tasca": [2,3],
"indicarà": [[2,7]],
"projecte": [8],
"complex": [0,2],
"francè": [2,1,7],
"jre": [2],
"asterisc": [0],
"botó": [7,0,1,[2,3,4]],
"s\'especifica": [2],
"destinat": [0],
"l\'ortografia": [1],
"capçalera": [0,[4,7]],
"file-shar": [2],
"alllemand": [7],
"proveu": [2],
"relatiu": [2,[0,1]],
"presentarà": [2],
"icon": [5],
"delet": [0],
"publiqueu": [2],
"mitjana": [7],
"traduïbl": [0,7],
"subcarpet": [0,2,[6,7],4],
"bcp": [[3,7]],
"associad": [1,[0,2,4]],
"projectaccessglossarymenuitem": [0],
"javadoc": [7],
"see": [4],
"l\'articl": [0],
"associat": [0,1,2,5],
"ser": [0,2,7,[3,5,6]],
"conjunt": [1,0,7,[2,6]],
"seu": [[0,2],3],
"associar": [0,2],
"set": [2,3,[0,1]],
"pugueu": [[1,7]],
"contain": [[5,6]],
"incorrect": [0,7],
"balis": [5],
"fastest": [8],
"afegeix": [7,3,[1,4,6],[0,2],5],
"categoria": [0],
"corresponen": [7,1],
"column": [7,[0,1]],
"sistemes": [8],
"obstant": [[0,7]],
"somnio": [0],
"incorporar": [4],
"traduïda": [[1,3]],
"continguin": [0],
"notació": [1],
"igual": [[1,2,4,5],6],
"correspondr": [2],
"fiabilitat": [2],
"featur": [1],
"habilitar-lo": [2],
"offic": [0,3],
"repositories.properti": [[0,2]],
"ranur": [4],
"flexibilitat": [3],
"reflectid": [3],
"expressió": [0,7,1,3],
"qüestió": [[0,4]],
"d\'actualitzar": [2],
"tenir": [[0,4,7],[1,3,6]],
"simplificada": [2],
"repositories": [8],
"l\'haureu": [[0,1]],
"projectsavemenuitem": [0],
"xmx6g": [2],
"teniu": [2,[0,3,4,6,7],5],
"permetrà": [[0,2,3]],
"escriviu": [[0,5],[1,2,3,7]],
"autocompletertablefirstinrow": [0],
"s\'executen": [7,1],
"powerpoint": [0],
"emmagatzemat": [7,[0,3,6]],
"emmagatzemar": [6,[0,2,5]],
"programari": [[0,2]],
"consel": [2],
"trobat": [[3,4]],
"persones": [8],
"és": [0,2,7,5,1,4,6,3,8],
"trobar": [0,2,[1,6,7]],
"emmagatzemad": [2],
"tmautoroot": [0],
"reflectir": [2],
"compat": [2,7,[0,3],[1,6]],
"fitxer2": [2],
"treball": [8],
"cursor": [5,4,[0,3],7],
"insertcharslrm": [0],
"sentit": [0],
"provar": [[0,7]],
"molta": [2],
"amunt": [0,1,7,3],
"humana": [1],
"habilitar-l": [7],
"variacion": [0],
"decor": [7],
"client": [2,[0,6]],
"provenen": [6,5],
"coincideixen": [0,7,6,[2,3,5]],
"anglè": [[0,2],1],
"sis": [3],
"mantenir-la": [3],
"apartat": [2],
"codificat": [6],
"associeu": [[0,2]],
"neteja": [7,3],
"falso": [1],
"pràctica": [0],
"ortogràfica": [1,2,[6,7]],
"practicar": [0],
"reinicialitzar": [1,0],
"desbloqueu": [3],
"falten": [4,0,[2,3]],
"falta": [[0,4],3],
"restant": [7,5,2],
"vagi": [0],
"fitxers": [8],
"targetroot": [0],
"multilingü": [0],
"select": [3,[1,5],[4,7]],
"bin": [0,[1,2]],
"cíclic": [[0,4]],
"apertium": [1],
"cerca": [7,0,1,[3,4],8,5],
"kaptain": [2],
"meta-inf": [2],
"clipboard": [4],
"d\'expressions": [8],
"projectopenmenuitem": [0],
"aquàt": [0],
"decim": [0,1],
"model": [[1,7]],
"l\'utilitzarà": [6],
"llibreta": [6],
"restada": [6],
"percentatg": [5,1,6],
"vostra": [2],
"direcció": [[0,7],5,3,4],
"l\'object": [7],
"vertic": [0],
"trobeu": [3,2,0],
"llist": [0],
"pràctiqu": [2],
"actualitzacions": [8],
"ofereixen": [[0,2]],
"viewmarktranslatedsegmentscheckboxmenuitem": [0],
"valu": [0],
"ordinador": [[0,2,3,8]],
"desplaça": [4,7,[0,5]],
"arrossegar": [5],
"ilia": [2],
"arrossegat": [5],
"s\'aplicaran": [[1,2]],
"àmplia": [2],
"exemple.org": [0],
"troben": [7,0],
"inserir-la": [4,5],
"uxxxx": [0],
"macos": [8],
"editar-n": [7],
"lliur": [2],
"inserir-lo": [5],
"depèn": [[0,2,4],[1,5]],
"editselectfuzzy1menuitem": [0],
"restauraran": [2],
"creació": [3,[2,4],[1,8],[0,6,7]],
"comprovacion": [[3,4]],
"hide": [7,[3,4,5]],
"s\'apliquen": [7,1,2],
"reinicialitzeu": [1],
"semblant": [[2,3]],
"reinicialitzen": [0],
"d\'iniciar": [7,2],
"comprimeix": [0],
"autocompleterlistpagedown": [0],
"d\'incloure-l": [0],
"auto": [4,[0,6],2,1],
"constant": [2],
"arrosseg": [5],
"d\'entorn": [0],
"confieu": [1,7],
"sign": [0,5],
"notepad": [[3,5],4],
"document.xx.docx": [0],
"editorskipnexttokenwithselect": [0],
"sol": [0,2],
"adequada": [2,[3,4,5]],
"download": [3,2],
"editortoggleovertyp": [0],
"desplaçar-s": [5],
"manteni": [2],
"desplaçar-l": [3],
"relació": [1],
"estadístiqu": [4,6,1,[0,2],7],
"opció": [0,1,2,7,4],
"sigui": [0,[3,7],6,[1,2],[4,5,8]],
"gradlew": [2],
"contenidor": [0],
"modif": [[2,3,5]],
"gairebé": [[2,4]],
"viewmarklanguagecheckercheckboxmenuitem": [0],
"recrea": [6],
"produc": [[5,7]],
"cerquen": [0],
"bon": [1],
"deixar": [0,[5,7],3],
"deixat": [[2,3]],
"automàt": [0,1,[3,5],4,[2,6]],
"extreu": [7,1],
"adjunt": [7],
"horitzont": [0],
"específiqu": [7,1,[0,2,6]],
"switch": [3],
"total": [5,[4,7]],
"immut": [6],
"trobin": [2],
"macro": [7],
"src": [2],
"consola": [2],
"gigabyt": [2],
"dels": [8],
"control": [[0,4],2,7,3],
"srl": [0],
"no-team": [2],
"incorpora": [2,7],
"ressalta": [[4,7],[1,5],[0,6]],
"lissens": [0],
"estona": [1],
"cerqueu": [[0,2],3],
"cerques": [8],
"d\'aparició": [7],
"ssh": [2],
"extremada": [[0,2,3]],
"environ": [2,0],
"licita": [2],
"tècnica": [0],
"específica": [[2,4],[0,1,6,7]],
"l\'usuari": [0,[1,2,4,5,7]],
"porpra": [4],
"drecera": [0,4,7,5,3],
"deseu-lo": [6],
"paràmetr": [2,7,0,3,1,[5,6],4],
"filtres": [8],
"indicada": [5],
"habiliteu-ho": [1],
"kde": [2],
"ginys": [8],
"motor": [5,4,1,7],
"d\'aplicar": [0],
"compartiran": [2],
"access": [[3,7],[6,8]],
"deixeu": [5,[2,6],[1,3]],
"compilació": [2],
"suprimeixen": [2],
"languag": [7,3,[2,5]],
"sul": [0],
"interpretat": [0],
"estadística": [7],
"current": [5,4],
"comptar-l": [4],
"generarà": [6],
"optionsglossaryfuzzymatchingcheckboxmenuitem": [0],
"inhabilitada": [4],
"key": [4],
"alguna": [[1,2]],
"amig": [4],
"msgid": [0],
"subfinestr": [5,[1,3],4,2],
"svn": [2,[6,7]],
"amic": [0],
"reduir": [7],
"condicion": [[0,2]],
"d\'interrogació": [0],
"d\'aparellar": [0],
"omegat-license.txt": [0],
"caoba": [0],
"emerg": [[5,7]],
"mostraran": [7,1,[0,5]],
"alguns": [8],
"l\'identificador": [4],
"editreplaceinprojectmenuitem": [0],
"recordatori": [[0,3,4]],
"but": [3,[1,7]],
"symbol": [5],
"recordar-vo": [6],
"editordeletenexttoken": [0],
"declara": [0],
"contextu": [1,5,[0,3,4,6]],
"express": [0,7,1,2,3],
"zero": [0,7,2],
"supèrflua": [1],
"variant": [[0,2]],
"permeten": [0,7,[2,3,4]],
"gotoprevioussegmentmenuitem": [0],
"desa": [2,4,[6,7],1,0],
"project_save.tmx.marcatemporal.bak": [6],
"resoldr": [3],
"composta": [1],
"facilitat": [3],
"secció": [2,3,0,7,1,4],
"l\'equip": [2,3,[0,7]],
"facilitar": [[0,2,3]],
"gotopreviousnotemenuitem": [0],
"decidirà": [0],
"stderr": [0],
"editredomenuitem": [0],
"rellev": [0,[1,2]],
"uilayout.xml": [[0,6]],
"sobreescriur": [2,[5,6]],
"inici": [0,2,7,1,4,[3,5,6,8]],
"sourceroot": [0],
"desempaquetar-lo": [2],
"dedicar": [3],
"carpet": [7,6,2,3,4,1],
"ofereixin": [2],
"recorda": [4],
"ús": [2,7,3,[0,4,5,6,8]],
"desi": [2],
"publicació": [2],
"disseni": [[0,3]],
"recordi": [[2,6]],
"vulgueu": [2,7,0,6,[1,3,4,8]],
"s\'exportaran": [7],
"figures": [8],
"idea": [2,7],
"orientada": [7],
"provocar": [[2,7]],
"idèntiqu": [5],
"saltant": [5],
"guies": [8],
"avançad": [7,[0,3]],
"sumari": [8],
"comunicació": [5],
"avançar": [4,1],
"avançat": [0],
"proveu-lo": [7],
"paraul": [0,7,[4,6],1,3],
"llibr": [0,3],
"normal": [0,7,2,1,[4,6]],
"gradual": [[0,2,6]],
"corrupt": [2],
"szl": [0],
"figura": [[3,5]],
"problemes": [8],
"configuració": [2,0,1,[4,7],6,8,3],
"implementa": [1],
"s\'accedeix": [0,2],
"distingeix": [7,0],
"restaur": [0],
"runtim": [2,0],
"individu": [0,[2,3,4,7]],
"potent": [[0,7]],
"aval": [0,7,1],
"tenen": [2,0,7,5,4,[1,3,6]],
"d\'anar": [0],
"filenam": [0],
"arrossegar-la": [5],
"coincidirien": [0],
"roam": [0],
"between": [[0,7]],
"amor": [0],
"canvi": [6,[0,2,7],3,4,[1,5]],
"gotosegmentmenuitem": [0],
"sinó": [[0,3]],
"projectes": [8],
"força": [0],
"llengü": [1,7,2,6,0,[3,4]],
"acceptat": [7,2],
"assignareu": [2],
"acceptar": [6],
"reutilitzar-l": [2],
"refereix": [2],
"interna": [5],
"funcionament": [8],
"initialcreationd": [1],
"s\'aplica": [[2,5,6,7]],
"líni": [0,[1,5]],
"helpaboutmenuitem": [0],
"corean": [1],
"seleccionar-lo": [3],
"parel": [[1,2],[0,7]],
"limitar": [0],
"leav": [[3,6]],
"regular": [0,7,1,2,3],
"identificació": [[0,3]],
"ajuda": [0,[2,4,8],[3,5]],
"suggest": [5],
"estigueu": [7],
"recorri": [7],
"s\'emmagatzemen": [[0,2],[4,5],[1,3,7]],
"token": [5],
"filter": [2,7],
"site": [1],
"projectroot": [0],
"ajudi": [3],
"omegat.log": [0],
"behaviour": [2,4],
"utilitzar-la": [[1,2]],
"agradi": [2],
"exportació": [2],
"autocompletertableright": [0],
"argument": [0],
"extreur": [0],
"proporciona": [0,2,[1,7],[3,4]],
"parcial": [[1,4],5,[2,7],3,6,0],
"alhora": [7,[1,2,3],[4,6]],
"proporcioni": [7],
"garantir": [2],
"tab": [0,4,1,5],
"divers": [5,[0,7],[2,3],[1,4],6],
"suaus": [0],
"should": [2],
"tag": [7,5,[0,3,4]],
"estiguin": [7,[1,2]],
"ressalten": [4],
"tal": [7],
"tan": [[0,4,6]],
"administrador": [2],
"tao": [[2,3,7]],
"individual": [7],
"mostra-la": [0],
"comodí": [[0,7]],
"onli": [2,[3,5]],
"filtrar": [3],
"hagi": [2,[0,1,3]],
"introdueixi": [4],
"projectreloadmenuitem": [0],
"aproximada": [7],
"suggeri": [1,[0,5]],
"person": [2,4],
"l\'alineació": [7,[0,2]],
"accepten": [1],
"l\'inrevé": [7],
"dígit": [0,7,[1,2]],
"l\'ordr": [[2,7],1,[0,4],6],
"reproduir": [7],
"tbx": [0,1],
"cal": [0,[2,3,6],7,1,[4,5]],
"can": [[1,2,3],[0,5,7],4],
"cap": [0,7,1,4,2,6,[3,5]],
"cas": [2,0,[1,3,7]],
"predicció": [1,0],
"cerqu": [7,[0,1],4,5,[3,6]],
"puguin": [3],
"limiten": [[0,2,6]],
"posar": [[0,2]],
"duser.countri": [2],
"provid": [3,[1,2,7]],
"consulta": [7],
"reboot": [2],
"readm": [0],
"breument": [[0,5]],
"match": [1,[3,5],[6,7]],
"inclinada": [0,2],
"categori": [0],
"fragment": [0,[3,7]],
"align.tmx": [2],
"pàg": [0],
"d\'emergència": [2],
"atractius": [0],
"reconeixen": [7,0],
"influït": [4],
"informació": [5,[0,2],4,1,7,3,6],
"pàl": [4]
};
