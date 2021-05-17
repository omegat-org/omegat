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
 "Appendix A. Slovníky",
 "Appendix B. Glosáře",
 "Appendix D. Regulární výrazy",
 "Appendix E. Přizpůsobení klávesových zkratek",
 "Appendix C. Kontrola pravopisu",
 "Instalace a provoz programu OmegaT",
 "Jak na to...",
 "OmegaT 4.2 - Uživatelská příručka",
 "Nabídka",
 "Podokna",
 "Adresář projektu",
 "Okna a dialogová okna"
];
wh.search_wordMap= {
"spravovat": [6,7],
"stisknutí": [[8,11]],
"zadáním": [[5,11]],
"instalačním": [11],
"souborem": [[8,11]],
"bloki": [[2,7,11]],
"obzvláště": [11,[5,6,8]],
"oktalovou": [2],
"velikostí": [11],
"ten": [9,[8,11],5],
"doleva": [6,7],
"info.plist": [5],
"pár": [[5,11],[6,9]],
"postupi": [11],
"byla": [6,8,[4,5,10]],
"prohledává": [11],
"fuzzi": [11],
"statusu": [11],
"sadě": [2],
"validován": [9],
"zůstává": [11],
"spolu": [11],
"vytvořít": [11,[5,6],10],
"specifikovat": [5,11],
"bylo": [11,[5,6,8]],
"postupu": [6],
"spustit": [5,11],
"sledují": [9],
"některých": [[6,10,11]],
"byli": [8,11,[6,9,10]],
"rohu": [9],
"samo": [2],
"robustní": [6],
"kódu": [[4,5,11],7],
"dgoogle.api.key": [5],
"roli": [6],
"projeví": [11],
"jednoduchého": [11],
"nedocílít": [9],
"edittagnextmissedmenuitem": [3],
"poskytnout": [9],
"hlavním": [[6,9]],
"spouštěcímu": [5],
"sama": [11],
"zakázat": [11],
"quiet": [5],
"sami": [[1,2,5]],
"kořenovému": [6],
"kolegů": [9],
"způsobů": [[6,10]],
"mělo": [11,0],
"es_es.d": [4],
"následují": [8],
"základních": [11],
"the": [5,[0,2]],
"ovládat": [[5,11]],
"strukturi": [10,11],
"stiskem": [11,[8,9],[1,5]],
"komprimovat": [11],
"účinnější": [2],
"projectimportmenuitem": [3],
"měla": [11,3],
"vyřešit": [6],
"alternativními": [11],
"opouštít": [11],
"pravidel": [11,[2,4]],
"struktura": [10,11],
"novém": [[1,6]],
"imag": [5],
"spojí": [11],
"měli": [[6,11],[0,5]],
"textovou": [6],
"nezlomitelné": [8,3],
"tuto": [11,8,[5,6,10]],
"spravováni": [6],
"kontrol": [4,11],
"ruském": [5],
"obsahující": [11,[6,8],[5,9,10]],
"nainstalován": [4],
"alternativního": [[9,11]],
"ruční": [11,4],
"komplexnější": [2],
"plochu": [5],
"currsegment.getsrctext": [11],
"tip": [11,[5,6]],
"zrdoje": [9],
"naposledi": [6],
"export": [6,[1,11]],
"záležet": [6],
"dbát": [6],
"velikosti": [11,9],
"aktuálním": [9,11,[1,8],10],
"akceptováno": [10],
"nahoř": [11,[2,9],[1,5,6]],
"zpětnou": [9],
"událostí": [3],
"transtip": [[3,9],1],
"akceptováni": [3],
"preventivní": [6],
"vygenerován": [8],
"obvykl": [[5,10]],
"xxxx9xxxx9xxxxxxxx9xxx99xxxxx9xx9xxxxxxxxxx": [5],
"poskytují": [11,6],
"klíčů": [11],
"tlačítek": [11],
"nerozumí": [6],
"měni": [2],
"fr-fr": [4],
"naopak": [11,6],
"teď": [[3,11]],
"zmíněným": [11],
"druhý": [[1,5,9]],
"prostým": [6,[5,11]],
"samý": [8],
"nejméně": [6],
"upřednostňovat": [6],
"primari": [5],
"aktivní": [8,[4,9,11]],
"byst": [[6,11],[2,3,5]],
"druhá": [3],
"root": [3],
"xmxzzm": [5],
"webster": [0,[7,9]],
"nezměněné": [11],
"děleno": [9],
"samé": [1],
"aktualizacích": [11],
"nacházejí": [11,6],
"chovat": [11],
"cjk": [11],
"platformě": [5],
"napsat": [1],
"nejvíc": [9],
"vyřešen": [1],
"následujt": [[4,6]],
"stisknuté": [3],
"nezahltila": [11],
"spouštěcího": [5],
"kořenového": [6],
"syntax": [11,3],
"či": [11,[5,6,9],[1,10]],
"empti": [[5,8,11]],
"vůbec": [11,2,5],
"přesunout": [11,8],
"systémového": [5],
"glosářích": [[9,11]],
"odpovídá": [11,8,[3,4,10]],
"docku": [5],
"pravidelných": [6],
"tmx": [[6,10],5,11,8,[3,9]],
"repo_for_all_omegat_team_project": [6],
"barevné": [11],
"události": [3],
"překládán": [[5,10]],
"nl-en": [6],
"importujt": [6],
"otevřeno": [11,8],
"grafického": [10],
"začátek": [[1,2,11]],
"integ": [11],
"nazývat": [5],
"intel": [5,7],
"fr-ca": [11],
"mainmenushortcuts.properti": [3],
"uvolnění": [3],
"ačkoliv": [11],
"zaktualizujet": [11],
"alternativních": [[8,9]],
"mění": [[6,11]],
"překládát": [11,6,9],
"navrhování": [8],
"cmd": [[5,6,11]],
"ignorován": [11],
"coach": [2],
"vyskakovací": [[4,8,9,11]],
"různými": [[6,11]],
"překladádat": [6],
"gotohistorybackmenuitem": [3],
"opravu": [8],
"opravi": [11,6],
"každého": [11],
"parametri": [5,6,11],
"stisknutím": [[6,11]],
"project-save.tmx": [6],
"tom": [11,9,[8,10]],
"přístupu": [11],
"přeložili": [4],
"otevřeni": [8],
"powerpc": [5],
"vpravo": [[6,11],5],
"minutách": [11],
"tou": [5],
"způsobi": [6,5,[1,4,11]],
"zpravidla": [11],
"otevřena": [9],
"dostanet": [11],
"avail": [5],
"instalovat": [4,5],
"požádat": [6],
"ručně": [[6,11],1,[4,8]],
"otevřenému": [9],
"aktivuj": [[8,11]],
"nezasahuj": [1],
"barevně": [8],
"tématu": [[6,10],[9,11]],
"kompletní": [[5,11],[3,6,9]],
"vzájemně": [6],
"kterých": [11,1],
"projekt": [6,11,8,5,10,[1,3],7,9,[0,4]],
"zapnuta": [11],
"remot": [5],
"reprezentují": [[5,11]],
"upravuj": [[6,9,11]],
"navrhovaná": [8],
"vašem": [5,[4,8],[1,6,10]],
"nenahraj": [5],
"proce": [6,[9,11]],
"dokument.xx.docx": [11],
"omegat.sourceforge.io": [5],
"pipe": [11],
"pozadí": [10,[8,9]],
"vytvářením": [11],
"paměťmi": [11],
"otevření": [6,[3,5,11]],
"translat": [11,5,[4,8]],
"platformi": [5],
"čínštini": [[5,6]],
"otevřený": [[1,9,11],[3,5]],
"možnsti": [11],
"alignovat": [8],
"místní": [[6,8],5],
"typických": [5],
"nejmíň": [2],
"udělá": [11],
"lokálním": [6],
"kontrolovat": [4],
"neformátovaným": [[6,11]],
"přeloženým": [11],
"vědomí": [5],
"základního": [11],
"vypadat": [5],
"docs_devel": [5],
"otevřené": [8],
"běží": [5],
"vymezuj": [2],
"gnome": [5],
"způsobí": [[5,11]],
"nahrazuj": [9],
"kategori": [2,7],
"bezplatný": [5],
"světle": [8],
"místně": [8],
"vašich": [9],
"vzdálenými": [11],
"vynechá": [[6,11]],
"nemají": [9],
"pravopisem": [8],
"každou": [11],
"tvar": [[1,11]],
"prev": [[0,1,2,3,4,5,6,8,9,10,11]],
"csv": [1],
"skryli": [11],
"podává": [9],
"tvari": [1],
"souborů": [11,6,[8,10],[1,4,5],3],
"naplnít": [10],
"vyžadovat": [8],
"cti": [11],
"dock": [5],
"press": [9],
"přejd": [8],
"zvýrazněn": [9],
"lepší": [11,4],
"příčini": [5],
"jakým": [11],
"výstupní": [6],
"rámci": [6,11,[5,8,9]],
"dmicrosoft.api.client_secret": [5],
"času": [6],
"špouštěč": [5],
"prováděni": [11],
"skrpiti": [11],
"tvaru": [11,1],
"pracovní": [[5,10]],
"samozřejmě": [[4,10],[5,11]],
"vidím": [[1,9]],
"malých": [5],
"synchronizován": [6],
"ctrl": [3,11,9,6,8,1,[0,10]],
"stará": [5],
"zaškrtávacího": [11],
"document": [[2,11]],
"parametrů": [5,6],
"zůstane": [11,10],
"nadál": [11],
"vidít": [9],
"chybným": [11],
"moment": [8],
"častých": [11],
"domovské": [5],
"stavu": [6,9,[10,11]],
"kontextové": [11],
"napsáním": [11],
"staré": [[5,11]],
"resourc": [5],
"lokalizac": [6],
"zadaných": [[5,11]],
"zvukové": [2],
"součástí": [10],
"přehl": [[6,9,11]],
"procházít": [11],
"velkých": [[2,5,11]],
"team": [6],
"jiných": [5,[6,9]],
"xx_yy": [[6,11]],
"docx": [[6,11],8],
"txt": [1,[9,11]],
"dialogové": [11,8,1],
"dialogová": [11,[7,8,9]],
"výměně": [1],
"snažit": [2],
"tedi": [5,11,[1,6,10]],
"tohoto": [11,8,10,5,[1,4,6,9]],
"segmentační": [[6,10,11]],
"příkazi": [11,[5,8]],
"hodně": [11],
"typ": [11,6],
"platforem": [5],
"trnsl": [5],
"což": [11,[1,5,6]],
"definic": [[3,11]],
"stále": [11],
"viewdisplaymodificationinfoselectedradiobuttonmenuitem": [3],
"uživatelského": [[5,11]],
"index.html": [5],
"omegat.tmx": [6],
"zaškrtno": [11],
"příkazu": [5,8,11],
"dvojím": [5],
"kontrolov": [11],
"přímý": [5],
"startu": [5,6],
"začínat": [3],
"adresář": [6,5,10,11,[3,4,8],9,[0,1],7],
"poskytovatel": [11],
"zveřejnět": [6],
"směr": [6],
"část": [9,[5,8,11],4],
"správou": [6],
"tzn": [[6,9,11]],
"diffrevers": [11],
"zablokování": [5],
"nového": [11,6,[2,5,10]],
"tzv": [11],
"listů": [11],
"vyberet": [11,[5,8],9],
"spojit": [6],
"přechod": [1],
"zautomatizují": [5],
"změníte": [11,6],
"přepsán": [11],
"přidat": [11,6,5,3,[1,4,8]],
"překládaným": [10],
"přibližných": [[8,9],[3,6,11]],
"technických": [8],
"otevřeného": [[3,8]],
"nejlepší": [10],
"posláním": [6],
"nahrává": [11],
"nejbližší": [11],
"standardní": [[4,11],[5,6]],
"kterýkoliv": [9],
"evropských": [11],
"další": [8,11,3,9,[5,6],2],
"čínského": [6],
"project.gettranslationinfo": [11],
"považován": [11],
"správné": [[1,5,10]],
"opatření": [6],
"nejedinečné": [[8,11],3],
"importované": [6],
"pomůž": [6],
"správná": [5],
"různých": [11,6,8],
"vyzkoušet": [[2,11]],
"start": [5,7],
"nejedinečný": [11],
"neukládá": [5],
"počátečních": [11],
"době": [[1,11]],
"srolovat": [11],
"asociováni": [5,8],
"nezahrn": [11],
"projektového": [0],
"způsob": [11,5],
"jazykovou": [[0,4]],
"equal": [5],
"spolehlivá": [11],
"dolní": [11],
"kliknet": [11,9,[1,4]],
"spolehlivé": [10],
"optionsalwaysconfirmquitcheckboxmenuitem": [3],
"jazycích": [[1,6]],
"priorita": [11],
"tvarů": [11],
"orientované": [11],
"pozdější": [[10,11]],
"věnujt": [5],
"začnet": [6],
"cílovém": [11,8,[5,9]],
"skopírováni": [11],
"upravit": [11,[3,6,7,8],[5,9]],
"enter": [11,[3,5,8]],
"viterbiho": [11],
"doménu": [11],
"souboru": [11,[5,6],8,1,[3,9],[7,10],4],
"prioriti": [[5,11]],
"bloků": [11],
"rozlišování": [11],
"projectteamnewmenuitem": [3],
"soubori": [11,6,5,10,8,4,[3,9],1,0,7],
"prioritu": [6],
"dojd": [11],
"directorate-gener": [8],
"dobř": [6],
"takovémuto": [11],
"barvou": [11],
"memori": [5],
"submenu": [5],
"deklarac": [11],
"správce": [[4,6]],
"jinými": [[5,6]],
"účet": [5,11],
"přejdet": [9],
"uváděné": [2],
"upravujet": [9],
"vnitřní": [11],
"uloží": [8,11],
"identická": [5],
"pravidla": [11,6,10],
"zkráceně": [2],
"identické": [6,[2,11]],
"rozpracované": [11],
"zatrhnět": [8],
"procesoru": [11],
"omegat.jnlp": [5],
"počítá": [11],
"klíčová": [11],
"pravidlo": [11],
"tabulkového": [11],
"identický": [8,[3,9,10,11]],
"datumem": [11],
"nezapamatuj": [11],
"n.n_windows_without_jre.ex": [5],
"vymezit": [5],
"importování": [6],
"příkazového": [5,[6,7]],
"netýmového": [6],
"příliš": [11],
"dole": [11],
"prof": [11],
"nějakou": [11],
"bidirekcionálního": [[3,8]],
"následovně": [[5,11],3],
"příčině": [5],
"obchodních": [11],
"dmicrosoft.api.client_id": [5],
"pokaždé": [11,6],
"populárním": [11],
"příkazů": [11,5],
"config-fil": [5],
"segmetnu": [8],
"skryté": [10],
"ponechat": [11],
"vytváří": [6],
"zkopírovat": [[4,6]],
"oblasti": [11],
"čísti": [9],
"komentářem": [[1,3]],
"přizpůsobit": [11,3],
"zvolt": [5],
"dat": [6,[1,11],[5,7]],
"způsobuj": [8],
"třetí": [1],
"zěmnám": [10],
"upraven": [5],
"nazývaný": [11],
"system-user-nam": [11],
"dvěma": [6,[3,5]],
"format": [11],
"uživatelskou": [7],
"console.println": [11],
"nazýváno": [5],
"zkontrolujt": [6,0,4],
"výběru": [5],
"klíče": [5,11],
"odstraní": [[4,11]],
"hodit": [11,[2,5]],
"dokončít": [[6,8]],
"zakázek": [9],
"disku": [6],
"uložt": [6,[1,3,5]],
"editoru": [11,9,8,6,[1,5,7,10]],
"aktualizována": [5],
"polovině": [11],
"tabulkový": [1],
"jazykům": [11],
"aktualizované": [1],
"existují": [11],
"určitého": [11],
"project_files_show_on_load": [11],
"definuj": [[8,11]],
"ltr": [6],
"optionsexttmxmenuitem": [3],
"přeloženého": [11,[6,8,9]],
"build": [5],
"párovací": [11,7],
"strojový": [11,9,8,7],
"naznačil": [6],
"marketplac": [5],
"kvůli": [6],
"nepotřebný": [11],
"archív": [0],
"entries.s": [11],
"textovými": [11],
"stisk": [[1,3]],
"del": [[9,11]],
"kastilštinu": [4],
"den": [6],
"mimo": [2,[5,6]],
"gotonextuntranslatedmenuitem": [3],
"zbarvena": [9],
"targetlocal": [11],
"path": [5],
"získají": [5],
"zkontroluj": [5],
"nakopírováni": [9],
"vyžádá": [11],
"posledního": [[8,10]],
"samotné": [5],
"nastavenými": [11],
"mezer": [11],
"deseti": [8],
"allsegments.tmx": [5],
"odpovíd": [5],
"tichý": [5],
"spojeni": [11],
"shoda": [11,9,1],
"proč": [5],
"helpcontentsmenuitem": [3],
"omegat-org": [6],
"zkratek": [3,7,2],
"shodi": [8,11,10,9,[2,3],6],
"jakéhokoli": [6],
"šedou": [11,8],
"descript": [5],
"třemi": [6],
"nejdřív": [6],
"stisků": [11],
"přeloženém": [11],
"písmeno": [2,[3,6,8]],
"projectaccessdictionarymenuitem": [3],
"zaškrtnuta": [11],
"klávesová": [3],
"páru": [6],
"klávesové": [3,1,[6,11]],
"domnívát": [6],
"tehdi": [8,11,5,[4,9]],
"použijet": [11,5],
"shodu": [11,9],
"písmena": [8,3,2],
"německém": [4],
"zaškrtnuto": [11],
"tichém": [5],
"překládaný": [[10,11]],
"term": [1],
"pári": [11],
"dolů": [11],
"dosud": [11,8],
"duden": [9],
"obsahuj": [10,[5,11],6,[1,9],0],
"distribuci": [5],
"nahradí": [[8,11]],
"průběžně": [10],
"samotný": [[1,6]],
"řecki": [2],
"spotlight": [5],
"did": [11],
"podržení": [3],
"zdroj": [8,3,[6,9,11]],
"podob": [[3,11]],
"nemůžet": [9],
"starších": [[9,11]],
"číslo": [11,8,5,[3,9]],
"dir": [5],
"slovníkům": [4],
"čísle": [1],
"div": [11],
"výše": [6,11,[5,9],10,[0,1,4,8]],
"čísla": [11,[6,9]],
"velké": [[2,3,8,11]],
"lze": [6,8,[5,11],1],
"překladovým": [11],
"poslouží": [11],
"okolo": [11],
"specifika": [11],
"viewfilelistmenuitem": [3],
"zdrojem": [[8,11],6,[3,9]],
"velká": [8,[2,3]],
"navrženi": [9],
"rozeznat": [11],
"brows": [5],
"podporován": [1],
"glošář": [11],
"test": [5],
"reportovat": [11],
"terminálu": [5],
"omegat": [5,6,11,8,10,3,7,4,1,0,[2,9]],
"nepřeloženého": [11],
"rule-bas": [11],
"manuální": [8],
"zálohovou": [6],
"přijmet": [5],
"koncovkou": [6,[9,10]],
"balíčku": [5],
"popisujem": [11],
"virtual": [11],
"widnow": [5],
"validátor": [11],
"console-align": [5],
"webová": [10],
"koncové": [11],
"ms-dos": [5],
"překládání": [[9,10,11]],
"projectopenrecentmenuitem": [3],
"získali": [11],
"dle": [6,[4,5],[2,8,9,10,11]],
"prakticki": [[1,11]],
"zástupné": [11,5,6],
"přecházení": [11],
"nakopírovali": [[6,10]],
"und": [4],
"une": [1],
"upřednostněnou": [9],
"přepnout": [[6,11]],
"buďt": [6],
"desetinnými": [11],
"řecké": [2],
"editoverwritemachinetranslationmenuitem": [3],
"výsledcích": [11],
"specifikovala": [3],
"ruským": [5],
"objeví": [11,[4,5,10]],
"es_es.aff": [4],
"přímo": [5,11,[2,6,8,9,10]],
"tučným": [[9,11]],
"pojavnem": [1],
"řídicí": [[3,8]],
"projectexitmenuitem": [3],
"získání": [[5,11]],
"běžnými": [6],
"text": [11,9,8,6,10,[2,3,7]],
"nerozbaleném": [5],
"odstranit": [11,[3,4,5,6,8]],
"editregisteruntranslatedmenuitem": [3],
"init": [6],
"problémi": [8,6,[0,1,7]],
"otevřen": [8,6],
"vzhled": [11],
"poškození": [[1,11]],
"schopna": [11],
"ruskými": [5],
"stalo": [11],
"využívaný": [11],
"manag": [6],
"správně": [6,[1,8],[10,11]],
"pokusí": [11,[5,6]],
"zablokováno": [5],
"právech": [8],
"opětovně": [6],
"maco": [5,7],
"field": [5],
"provedena": [[6,8]],
"macu": [5],
"přece": [9],
"různé": [11,[5,8,9],[6,10]],
"problému": [5],
"doc": [6],
"provedeno": [5],
"konkrétně": [5],
"například": [11,6,[2,4],[1,5,9],[0,3,8,10]],
"provedeni": [5],
"otevřet": [11,[5,6],[8,10],4],
"status": [[8,10,11]],
"server": [[10,11]],
"podívejm": [3],
"zkontrolov": [9],
"přesouvat": [9],
"paramet": [5],
"získáte": [5],
"znova": [11],
"prvki": [6],
"mac": [3,[2,6]],
"nula": [[2,11]],
"znovu": [6,[8,11],[1,3]],
"umí": [6,1,[9,10,11]],
"párů": [11],
"začíná": [11],
"man": [5],
"map": [6],
"účtu": [5],
"opětovné": [6,7],
"may": [[9,11]],
"zkratka": [3,[8,11]],
"konkrétní": [11,6,9],
"megabytech": [5],
"konkrétních": [6],
"smazán": [8],
"url": [6,11,[4,5,8]],
"vazbu": [9],
"přes": [5,11,3,[1,4,8,10]],
"cyklicki": [8],
"před": [11,8,6,[5,10],[1,4,9]],
"měnění": [6],
"zkratki": [3,11,[1,6],8],
"uppercasemenuitem": [3],
"viewmarkuntranslatedsegmentscheckboxmenuitem": [3],
"operační": [[5,11]],
"specifickou": [11],
"zkratku": [[3,6,9,11]],
"formátováním": [11],
"způsobem": [[5,6],[4,9,11]],
"klienta": [6,10,[5,9,11]],
"zajistít": [11],
"use": [5],
"výchozí": [3,11,[6,9],[5,8,10]],
"přeskočit": [11],
"svn.code.sf.net": [5],
"omegat.jar": [5,[6,11]],
"omegat.app": [5],
"neviditelné": [11],
"usr": [5],
"zobrazeného": [11],
"nezobrazují": [1,11],
"obsahem": [10],
"bezpečnost": [11],
"párování": [11,8],
"podstatě": [4],
"odkaz": [6],
"význam": [11],
"necht": [11],
"utf": [1],
"uživatelé": [5,7,11],
"načten": [6,[1,11]],
"první": [11,[5,8],[1,3,9,10]],
"servic": [5],
"vyplněn": [[8,11]],
"ověřeného": [11],
"překládané": [3],
"spojení": [11,5],
"služeb": [5],
"takového": [6,[9,11]],
"dsl": [0],
"vztahují": [[5,6]],
"méně": [5],
"možných": [[5,11]],
"posledních": [11,6],
"nenalezn": [1],
"dokumentaci": [3,11],
"kořen": [[3,8,11]],
"rozbalení": [5],
"n.n_windows_without_jre.zip": [5],
"openoffice.org": [6,11],
"med": [8],
"mějte": [11,[5,6]],
"průběh": [11],
"instalační": [5],
"žlutě": [9],
"tomto": [5,6,[8,10,11],[1,9]],
"make": [11],
"rozbalit": [[0,5]],
"službi": [[5,11]],
"neplatí": [5],
"nachází": [5,[1,2,6,8,10,11]],
"průběžným": [11],
"čísel": [11],
"projectcompilemenuitem": [3],
"console-transl": [5],
"vrátí": [[5,6,9]],
"služba": [8],
"navigační": [5],
"kreativní": [11],
"nahradít": [9],
"současnosti": [11],
"jednoho": [11,8],
"gotonextuniquemenuitem": [3],
"nasbírali": [10],
"grafi": [11],
"wordart": [11],
"optionsviewoptionsmenuitem": [3],
"glosář": [1,11,9,3,[7,8],[6,10],[0,4]],
"commit": [6],
"překládaného": [11],
"targetlocalelcid": [11],
"project_stats_match.txt": [10],
"dva": [11,5,[4,6,8]],
"dvd": [6],
"zadání": [11,[2,8]],
"systémem": [8],
"xmx2048m": [5],
"vše": [[1,9,10,11]],
"meniju": [1],
"vyhledávači": [11],
"ne-slova": [2],
"xxxxxxxxxxxxxxxx.xxxxxxxxxxxxxxxx.xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx": [5],
"událost": [3],
"mexickou": [4],
"koncovek": [11],
"šipku": [11],
"poznačit": [11],
"neobsahují": [[6,11]],
"jména": [11,[6,9]],
"proměnnou": [11],
"mezerník": [11],
"dostačující": [6],
"problémů": [[1,8]],
"krunner": [5],
"libreoffic": [6,4,11],
"postupuj": [11],
"využijet": [[4,11]],
"kliknutí": [11,8,5],
"zavření": [[6,8]],
"nezalomitelné": [11],
"ovlivní": [5],
"parsovacím": [11],
"často": [6,11,5],
"podtržené": [1],
"nastavovat": [5],
"konceptu": [11],
"specifikované": [11],
"jméno": [11,[5,6]],
"texti": [11,6,[8,10]],
"vůči": [10],
"nejsou": [11,[1,6,10],8],
"implementacemi": [5],
"nepodporuj": [6],
"existuj": [10,[2,8,11]],
"začátku": [10,[6,11],[3,5,8]],
"narazít": [6],
"typickým": [6],
"defin": [3],
"vyžaduj": [[4,6,11]],
"představuj": [[5,9,11],[6,7,10]],
"meta-tagů": [11],
"důvěryhodných": [11],
"stav": [8],
"maskováno": [5],
"mají": [11,5,[4,8,10]],
"úplnou": [6],
"řešení": [6],
"pořadí": [11,8],
"některý": [11],
"viewdisplaysegmentsourcecheckboxmenuitem": [3],
"zavřít": [11,[3,8]],
"klik": [9],
"editregisteremptymenuitem": [3],
"přizpůsobení": [3,7,[2,11]],
"potvrzujícího": [11],
"některé": [[0,11],[1,6,8,9]],
"textu": [11,6,4,10,[1,8,9],[3,5,7]],
"dokud": [[6,11]],
"open": [11,6],
"wordovskými": [6],
"otevřenou": [6],
"nesmí": [11],
"www.oracle.com": [5],
"některou": [11],
"skript": [11,[5,8]],
"nespárované": [[9,11]],
"existov": [5],
"zadaný": [11,5],
"otestujt": [6],
"project": [5,11],
"xmx1024m": [5],
"předefinovat": [11],
"zvykem": [8],
"normálně": [11],
"zadáno": [5],
"zdrojovým": [[10,11],1],
"rozlišují": [2],
"máme": [5],
"ztrátě": [6],
"nechcet": [11],
"volitelně": [8,5],
"penalty-xxx": [10],
"určena": [6],
"zadána": [8],
"gotonextsegmentmenuitem": [3],
"zadaná": [11],
"málo": [11],
"zadané": [11],
"nnn.nnn.nnn.nnn": [5],
"zpracovat": [[5,6,11]],
"rozložení": [11],
"překládanému": [8],
"kontrolujt": [6],
"abort": [5],
"hodnot": [9],
"určeni": [11],
"malá": [2,[3,8]],
"internet": [11],
"určeno": [5],
"kvantifikátori": [2,7],
"kompon": [11],
"doplňt": [5],
"dvě": [11,9,5],
"počítači": [5,[8,11]],
"firefoxu": [4],
"žádacího": [5],
"lišta": [9,7],
"vývoj": [2],
"standardního": [9],
"slučovat": [11],
"nastaveních": [11,8],
"třeba": [[4,11]],
"lišti": [9],
"nedávný": [[3,8]],
"operátori": [[2,7]],
"vybráním": [[10,11]],
"snímků": [11],
"umožnili": [11],
"přeložít": [11],
"es-mx": [4],
"zpracovávat": [11,[5,6]],
"souborové": [[6,9]],
"slovníčků": [1],
"zahrn": [6],
"base": [1],
"extermí": [8],
"přednastaveného": [11],
"zajistí": [11],
"jind": [9],
"kurzívou": [11],
"zajisté": [1],
"moh": [[6,10,11]],
"neplatným": [5],
"ztratít": [9],
"určený": [1],
"zástupců": [5],
"kolekc": [11],
"zadají": [5],
"navrhovaných": [9],
"okolností": [5],
"podaří": [6],
"soubor": [5,6,11,1,8,10,3,0,[7,9]],
"nepotřebujet": [[5,11]],
"pozici": [8,9,11,[4,6]],
"listopad": [1],
"stisku": [[3,9]],
"repozitářem": [6],
"ještě": [9],
"existující": [11,5,6,10],
"ukázaný": [9],
"gedit": [1],
"písmo": [3,11],
"jinou": [[4,6,11]],
"sloupcích": [11],
"objekt": [11],
"určení": [[4,6]],
"jejich": [11,6,9],
"word": [11,6],
"písma": [11,8],
"cílového": [11,6,[4,9],[1,8]],
"ověřovat": [1],
"spuštěn": [5,11],
"uvedenými": [5],
"brazilská": [4],
"uživatelova": [5],
"podstatná": [11],
"zprávi": [[6,9]],
"oddělt": [6],
"posloužit": [6],
"pbsahuj": [10],
"příkladem": [6],
"neodpovídají": [[1,11]],
"koncovki": [1],
"operačním": [8,5],
"meta-tagi": [11],
"kopírovat": [[3,8,9]],
"velmi": [11,6],
"posunu": [2],
"koncovka": [[1,11],0],
"zmíněných": [1],
"nedaří": [11],
"nakliknet": [9],
"tomuto": [10,[9,11]],
"externí": [11,[3,8]],
"fyzicki": [4],
"normální": [11],
"prostém": [1],
"koncovku": [[0,1]],
"nebud": [8,[5,11],[1,6],4],
"lingvo": [0],
"spuštěna": [5],
"mrs": [11],
"textů": [11],
"proměnná": [11],
"prostřednictvím": [5,6,[0,9,11]],
"úpravám": [3,8],
"objemu": [11],
"vztahujících": [11],
"proměnné": [11],
"spuštěno": [8,11],
"táhnout": [9],
"multiplatformní": [11],
"jiné": [6,5,[1,4,10]],
"liště": [5],
"dokumentem": [[9,11]],
"osmičkovou": [2],
"jiní": [6],
"pt_pt.aff": [4],
"html": [11,5],
"nakopírování": [6],
"upřednostňovanou": [11],
"ctime.txt": [11],
"opatrní": [6],
"čínské": [11],
"třída": [2],
"nemusít": [[4,5]],
"zastaralou": [6],
"nepřesunet": [11],
"artund": [4],
"třídi": [[2,7]],
"ukončené": [9],
"zapříčiní": [11],
"máte": [5,11,6,4,[1,9]],
"přidávat": [11,[9,10]],
"ukončení": [8,[3,11]],
"konfigurační": [5,8],
"vyskytovat": [11],
"jakém": [11],
"potáhnet": [9],
"stávající": [[5,6,11]],
"cti.m": [11],
"touto": [5],
"defektním": [6],
"postupně": [[6,11]],
"www.ibm.com": [5],
"vypadá": [5],
"pracovat": [6,[1,5]],
"dotčené": [6],
"vytváření": [11,1],
"počítačů": [11],
"cíle": [[4,8,11]],
"zadali": [[4,8]],
"ověřit": [[3,6,11]],
"aktualizovat": [[5,11]],
"zprava": [6,[7,8]],
"spuštění": [5],
"mažou": [6],
"nebyl": [[1,8]],
"vybranou": [8],
"doporučujem": [[1,11]],
"command": [[3,5,9]],
"flexibilitě": [11],
"n.n_without_jr": [5],
"vztahuj": [6],
"používát": [5,[1,3,6,10]],
"přičemž": [9],
"jiný": [[6,9,11]],
"objevovat": [9],
"viewmarkbidicheckboxmenuitem": [3],
"počítačích": [3],
"year": [6],
"povolují": [9],
"taková": [[6,11]],
"určen": [5],
"upgradu": [11],
"organizací": [1],
"detailní": [5],
"poskytn": [[6,11]],
"položki": [3,11,[1,6],5],
"využívá": [10],
"vykonávat": [11],
"položka": [3,[1,5],11],
"údaj": [5],
"červenou": [10],
"ptevř": [8],
"zobrazených": [11],
"viz": [[6,11],[1,4,5,8,10]],
"konverz": [6],
"položku": [5,[8,9,11]],
"nastaveními": [6],
"version": [5],
"mapování": [6,11],
"opakujících": [9],
"volně": [0],
"folder": [5],
"španělštinou": [4],
"spusťt": [5],
"potřeb": [6],
"probíhají": [9],
"nalzenet": [3],
"zobrazít": [11,5],
"nastaven": [6],
"znaků": [11,2,[5,6,7],[3,8,9]],
"požadavkem": [6],
"statistiku": [8,10],
"nakládá": [11],
"vytvářejí": [6],
"průměrná": [11],
"užívaného": [11],
"nijak": [4],
"potřebným": [5],
"přesně": [11,1],
"projecteditmenuitem": [3],
"britannica": [0],
"statistiki": [[6,10]],
"umožňují": [8],
"segmentaci": [11],
"wikipedii": [8],
"statistika": [3,8,10,6],
"zapnout": [11],
"jednotkami": [11],
"jakýkoliv": [2,11,10],
"vybrali": [4],
"wikipedia": [8],
"machin": [11],
"načetlo": [3],
"přesto": [11],
"informacím": [[0,5]],
"použit": [[5,11],1],
"druhou": [11],
"skutečné": [11],
"straně": [6],
"normálních": [5],
"skupině": [6],
"nálezů": [11],
"vlastních": [11,[6,8]],
"anglické": [6],
"iceni": [6],
"nebo": [11,6,5,2,8,9,1,3,4,10,0],
"jedinečné": [[9,11]],
"exportuj": [[6,8,11]],
"krokem": [9,10],
"strukturální": [11],
"použij": [[5,11],[4,6]],
"přesun": [8],
"zvládli": [9],
"zvýraznít": [9],
"děje": [11],
"docela": [1],
"aktivaci": [8],
"sled": [11],
"kombinaci": [0],
"zvýrazněného": [8],
"přibližnému": [8],
"naleznou": [11],
"náhradní": [11],
"dsun.java2d.noddraw": [5],
"pamětech": [[10,11]],
"skupina": [2],
"x0b": [2],
"komentářích": [11],
"samotném": [[10,11]],
"http": [6,5,11],
"skupini": [[9,11]],
"ukazovat": [[8,11]],
"stranu": [11],
"snížení": [10],
"volbami": [8],
"terminálové": [5],
"předělat": [9],
"syntaxi": [11],
"vrátit": [9,[8,11]],
"volný": [9],
"tabulátorem": [1,11],
"softwar": [11],
"ovšem": [[3,6,11]],
"spouští": [[5,8]],
"upozornění": [11,5],
"odpojit": [9],
"projectsinglecompilemenuitem": [3],
"zadaného": [[5,11]],
"vytvářejt": [6],
"párovat": [11,8],
"nejnovější": [6],
"zpřístupnit": [3,[8,11]],
"zastoupená": [11],
"imperativní": [11],
"velkým": [[2,11]],
"takový": [11],
"kupa": [11],
"jako": [11,6,[5,9],8,[1,2],[4,10],0,3],
"dokument.xx": [11],
"výrazech": [11],
"aktualizac": [5,11,[1,8]],
"chová": [11],
"toho": [5],
"ukládá": [[6,8]],
"definovat": [11,[2,5]],
"sctr": [11],
"volné": [8],
"zvážit": [11],
"takové": [11,6,[1,9,10]],
"internetovém": [11],
"skupin": [11],
"nejd": [5],
"módu": [6],
"formě": [[2,6]],
"pravém": [9],
"system-os-nam": [11],
"vybraný": [8,[5,9,11]],
"optionstabadvancecheckboxmenuitem": [3],
"nad": [[6,11]],
"částečné": [11],
"vícekrát": [2],
"vybraná": [8,11],
"níže": [[2,5,6,9],[3,4,11]],
"kterou": [[3,9,11]],
"činnosti": [5],
"vybrána": [8],
"pracovním": [5],
"zpracovávané": [8],
"čtimě": [11],
"příhodném": [5],
"optionsviewoptionsmenuloginitem": [3],
"sbírce": [9],
"vybrané": [8,[4,6,11]],
"zpracováváni": [[6,11]],
"vybráno": [[8,11]],
"zeleném": [9],
"módi": [6],
"tar.bz2": [0],
"vozíku": [2],
"nových": [8,[3,6]],
"francouzštini": [11],
"nejjednodušším": [5],
"bundle.properti": [6],
"přidělili": [5],
"systém": [5,[1,11]],
"x64": [5],
"běžně": [[5,6,8,11]],
"sestavení": [5,7],
"dvakrát": [5],
"pravidlům": [11],
"francouzštinu": [11,5],
"hledacího": [11],
"hodini": [6],
"vyhrazeného": [6],
"řádků": [11],
"běžné": [[6,8]],
"udělejt": [10],
"isn\'t": [2],
"řádně": [5,6],
"login": [11],
"blížících": [11],
"stejnou": [[5,6,10]],
"šířili": [6],
"vybraných": [[3,6,8]],
"tomu": [[6,11],[5,8,9]],
"dělá": [5],
"běžný": [6],
"kliknout": [[5,11]],
"správnost": [[9,11]],
"jaké": [11],
"optionsteammenuitem": [3],
"objektového": [11],
"klávesovou": [[3,9,11]],
"gzip": [10],
"nepřeložený": [11,8,[3,9]],
"rozhraní": [5],
"tvoření": [6],
"směri": [6],
"hledá": [11],
"viděli": [11],
"esc": [11],
"výraz": [11,1,[2,5],9],
"x86": [5],
"nepřeložené": [11,8,[3,6,10]],
"nastaví": [11],
"stazeny_soubor.tar.gz": [5],
"tvořené": [1],
"automatickým": [[6,11]],
"nostemscor": [11],
"nalezeném": [9],
"znovunačtení": [6],
"odpovídali": [2],
"šestnáctkovou": [2],
"tečka": [2,[5,11]],
"odpovídalo": [11],
"linki": [11],
"několika": [11,[5,9]],
"tečki": [11],
"console-createpseudotranslatetmx": [5],
"bráni": [11],
"vyšší": [[5,11]],
"důležitá": [[6,11]],
"člen": [2],
"určit": [[5,10,11]],
"longman": [0],
"fuzzyflag": [11],
"ukazuj": [9,[0,2,11]],
"obnovit": [11,[3,9],8],
"merriam": [[0,7,9]],
"merrian": [0],
"fungovat": [[4,8],[6,11]],
"novější": [[5,8]],
"protokol": [[3,8]],
"důležité": [5,6,11],
"soukromé": [[9,11]],
"stáhnout": [[0,3,8],[4,5,6,7]],
"skutečně": [5],
"tečku": [11],
"řetězců": [6],
"intervali": [11],
"sestaveni": [6],
"skončení": [11],
"originálního": [6],
"nastavit": [11,6,[1,5,7,8]],
"exportní": [6],
"ničeho": [9],
"vhodném": [6],
"odpovídají": [11],
"netisknutelné": [11,8,[1,3]],
"operačních": [10],
"n.n_without_jre.zip": [5],
"obvyklé": [6],
"zvětšit": [11],
"netisknutelný": [11,2],
"logické": [[2,7]],
"celý": [[8,11],6,5],
"chybějící": [8,3],
"obrázku": [9],
"více": [11,5,6,[9,10],1,7,[3,8]],
"formu": [[10,11]],
"nepřeloženo": [11],
"potvrdit": [[3,8,11]],
"známé": [6],
"zobrazenými": [1],
"brazílii": [5],
"běžného": [[5,6]],
"obchodní": [[9,11]],
"anglicki": [6],
"zároveň": [7],
"známý": [11],
"ruštině": [5],
"zadat": [5,11,[1,4]],
"jiném": [5],
"formi": [10],
"vybrat": [11,[4,9],5,8],
"offlin": [6,5],
"holadski": [6],
"odstraňt": [11],
"u00a": [11],
"celá": [11],
"nepřeloženi": [11],
"přetáhnout": [5,[7,9]],
"selhat": [5],
"toto": [11,5,6,[8,9,10],[1,2,4]],
"voleb": [11],
"není": [5,8,[9,11],[1,6],10],
"shift": [3,11,[6,8],1],
"příkazový": [5],
"nic": [2,[8,10]],
"ploše": [5],
"java": [5,3,[2,11],[6,7]],
"příkazové": [5],
"exe": [5],
"adresářích": [10],
"javi": [[5,11]],
"lang2": [6],
"lang1": [6],
"ukl8daj9": [11],
"ověření": [[3,5,6]],
"project_save.tmx": [6,10,11],
"paměti": [6,11,[5,10],9,8,[1,2]],
"dictionari": [0,10,7],
"modelu": [11],
"importuj": [9],
"horním": [9],
"označt": [11,8,[5,6],4],
"opouštění": [11],
"slov": [11,9,[2,8]],
"operacích": [9],
"nemá": [[1,5],[8,11]],
"posléz": [[4,5,6]],
"vynucených": [10],
"řádku": [5,2,6,11,[3,7,9]],
"vygenerování": [6],
"předběžnému": [6],
"parametrem": [5],
"převést": [11,6],
"pravého": [11],
"cizích": [11],
"rozestavění": [[9,11]],
"účeli": [11,[1,4]],
"zaplňující": [11],
"chyba": [[5,6]],
"popsané": [5],
"zvoleném": [5],
"čtime": [5],
"chybi": [6,[5,8]],
"popsáni": [11],
"timestamp": [11],
"projectaccessrootmenuitem": [3],
"upřednostnění": [11],
"dyandex.api.key": [5],
"popsáno": [[8,11]],
"vypsáno": [[5,9]],
"doprava": [6],
"nezapomeňt": [11,[4,6,10]],
"konfigurovat": [11],
"ušetří": [11],
"jazyc": [[5,9],[1,6,10]],
"přiřazení": [5],
"vedlejší": [10],
"předchozích": [9],
"přiřazené": [3],
"oficiální": [7],
"provozu": [5],
"plugin": [11],
"prezentaci": [11],
"vždi": [11,[1,3,8,9]],
"jazyk": [5,11,6,4,1],
"činili": [6],
"nějakého": [[2,11]],
"libovolné": [10],
"možností": [11,5,[6,9]],
"řádki": [5,3,11],
"microsoff": [11],
"neprojeví": [5],
"vteřin": [11],
"editinsertsourcemenuitem": [3],
"ukončí": [8],
"označí": [8],
"microsoft": [11,[1,5,6,9]],
"projectnewmenuitem": [3],
"spoustu": [11],
"operačního": [[5,11]],
"nabízejí": [6],
"pravým": [[5,9,11],1,[4,8]],
"dispozici": [5,4,[8,11],[1,3]],
"optionstranstipsenablemenuitem": [3],
"segment": [8,11,9,3,10,6],
"nabízeno": [9],
"ciferně": [6],
"spousti": [[10,11]],
"glossari": [1,[6,10],[7,9,11]],
"uložit": [11,8,[3,5,6,9]],
"ignored_words.txt": [10],
"navrátí": [11],
"než": [11,6,[2,4,5,8,10]],
"configuration.properti": [5],
"github.com": [6],
"ukládáním": [11],
"přebírají": [6],
"nnn": [9],
"next": [[0,1,2,4,5,6,7,8,9,10,11]],
"vygenerováni": [11],
"rozlišovat": [[10,11]],
"částečně": [9],
"string": [5],
"import": [6,11],
"sloužit": [4],
"vizuálně": [8],
"místními": [11],
"určováno": [1],
"obousměrného": [8],
"týmové": [[6,8],[5,11]],
"přepínání": [[6,8]],
"not": [11,5],
"volbu": [[8,11]],
"jednorázový": [3],
"nepřekládat": [11],
"žádný": [8,11,[1,5,9]],
"úvodní": [11],
"týmový": [6,8,[3,7]],
"volba": [[8,11],4],
"kurzoru": [8,[9,11]],
"klosář": [11],
"spousta": [6],
"volbi": [[5,9,11]],
"was": [11],
"selection.txt": [11,8],
"editovat": [11,9],
"jednoduchým": [6],
"xhtml": [11],
"deklaraci": [11],
"žádné": [11,5,[1,10],[3,8]],
"refer": [9],
"možnosti": [11,8,[3,5,9],6,[4,7],[2,10]],
"žádná": [11,[8,10]],
"window": [5,[0,1,2,7,8]],
"docílit": [[6,9]],
"médium": [6],
"zpětného": [[2,5]],
"umožňuj": [11,[5,6],[1,2,8,9]],
"přejmenujt": [6],
"aktivního": [[8,10,11]],
"řádek": [5,[3,8,11],2],
"vstupní": [[6,11]],
"kombinac": [3],
"disable-project-lock": [5],
"spodním": [11],
"když": [11,[5,6,8],9,1,[4,10]],
"omegat.pref": [11],
"udělít": [5],
"silné": [11],
"narazí": [11],
"označít": [8,[1,5]],
"nabízené": [8],
"exportovatelný": [6],
"znaki": [11,8,1,[2,3,5,6,9]],
"zdrojovém": [11,9,[1,3,5,8,10]],
"předchozími": [11],
"podmínek": [[0,6,10]],
"předchozímu": [[2,6]],
"licencí": [[0,6]],
"celých": [11],
"pt_pt.dic": [4],
"stisknet": [11,[8,9]],
"thunderbirdu": [4],
"netisknutelných": [11],
"radu": [6],
"level1": [6],
"znaku": [11,[2,5]],
"uložen": [6,[9,11]],
"instrukc": [5,11],
"level2": [6],
"vedl": [11,5,[1,3,4]],
"přidejt": [[3,5,6,11]],
"pokyni": [[7,11]],
"sousedící": [9],
"nahrát": [11],
"zaškrtnět": [11],
"vzhledem": [11],
"web": [5,7],
"en-us_de_project": [6],
"nahrán": [1],
"sám": [11],
"kativujt": [10],
"levelu": [6],
"příponě": [11],
"editselectfuzzy4menuitem": [3],
"editregisteridenticalmenuitem": [3],
"jakákoliv": [3],
"rozbaleném": [5],
"dialogovém": [[8,11],6,[1,10]],
"procesor": [1],
"schránki": [8],
"bílého": [2],
"programech": [[4,11]],
"kódem": [4],
"téměř": [1],
"nespolehlivá": [1],
"pt_br.dic": [4],
"nastavování": [1],
"mexická": [4],
"tabulc": [11],
"unabridg": [0],
"chybět": [1],
"přesná": [1],
"předchozího": [[6,8]],
"automatickém": [11],
"muset": [11,6],
"zápis": [11],
"pamětí": [6,10,11,[7,9],5],
"optionsglossaryexactmatchcheckboxmenuitem": [3],
"ohraničeni": [1],
"vylepšení": [[8,11]],
"přesné": [11,8],
"komponenti": [6],
"pořizujt": [6],
"rozbalovacího": [11],
"zeleným": [8],
"jezdec": [11],
"příponi": [11],
"užívá": [[2,4]],
"chyběli": [8],
"znázornit": [11],
"chtít": [11,[5,6]],
"nnnn": [9,5],
"project_save.tmx.yearmmddhhnn.bak": [6],
"formátech": [11],
"oknech": [5],
"doslovných": [11],
"dávkách": [5],
"importovat": [11,[6,10]],
"slovníkové": [4],
"hotovu": [11],
"levým": [11],
"víte": [11],
"br.aff": [4],
"užíváním": [6],
"zh_cn.tmx": [6],
"zkracuj": [11],
"objektově": [11],
"význami": [9,7],
"přiřazeno": [5],
"využít": [11],
"zhruba": [9],
"doporučeno": [11],
"funguj": [11,8],
"archiv": [5],
"balíček": [8,5],
"přiřazena": [3],
"repo_for_omegat_team_project.git": [6],
"podobnosti": [11],
"výstup": [[6,11],[3,8]],
"překladatelem": [6],
"nabídki": [3,11,9,5,[6,8]],
"proxi": [5,11,3],
"překrývat": [9],
"synchronizac": [11],
"extens": [11],
"odznačením": [11],
"jedno": [6],
"uplatní": [11],
"jednu": [11,3,[5,6]],
"nabídku": [11],
"započetí": [11],
"rozhodnutí": [10],
"ctime": [11],
"pochybnosti": [10],
"zůstávají": [5],
"exportních": [6],
"aplikac": [5,6,4,11],
"nebudou": [11,[3,5,6,8,10]],
"sure": [11],
"parametr": [5],
"přepíš": [8],
"zvýrazněna": [8],
"kolem": [11],
"součást": [[4,5]],
"diff": [11],
"mazání": [11],
"an": [2],
"editmultiplealtern": [3],
"optické": [6],
"zvýrazněno": [8],
"nemůž": [[6,11]],
"vidět": [9,[6,10,11]],
"třídit": [11],
"be": [11],
"výskyt": [[2,9]],
"poznámkou": [8],
"filters.xml": [6,[10,11]],
"nutno": [6,11,[3,5],[1,4]],
"instalaci": [5,[4,7,9]],
"terminologii": [[1,6,11]],
"sloužícímu": [6],
"br": [11,5],
"pojmenován": [3],
"patřičného": [5],
"aktuálního": [11,9,[1,3]],
"by": [11,6,[0,5],[3,9]],
"segmentation.conf": [6,[5,10,11]],
"kladen": [10],
"panel": [5],
"veškerého": [9],
"ca": [5],
"související": [9],
"cd": [5,6],
"öäüqwß": [11],
"přebytečných": [11],
"případ": [6],
"sezeními": [11],
"cn": [5],
"co": [6,11,5,[2,10]],
"zmíněný": [6],
"figur": [[1,4],[0,2,7]],
"návrh": [[9,11]],
"představují": [6,[1,9,11]],
"cx": [2],
"ne-mezera": [2],
"zmíněné": [[0,6,11]],
"nabídka": [3,7,8,[9,11],5],
"balík": [5],
"jedná": [5,[6,11]],
"poskytkuj": [5],
"apach": [4,[6,11]],
"starých": [11],
"adjustedscor": [11],
"platný": [[5,11]],
"dd": [6],
"překladatel": [6,9,11,10],
"jedné": [11,10],
"ohrožení": [6],
"vkládání": [11,6],
"cílových": [11,6,5],
"do": [6,11,[5,8],9,10,1,4,3,0],
"f1": [3],
"modrým": [11],
"f2": [9,[5,11]],
"f3": [[3,8]],
"dr": [11],
"tlačítkem": [11,[5,9],1,[4,8]],
"f5": [3],
"obdobu": [5],
"pravidelně": [6],
"jsme": [1],
"fungují": [6],
"úplně": [6],
"platné": [11],
"dz": [0],
"editundomenuitem": [3],
"převedeno": [11],
"startuj": [5],
"zkratkami": [3],
"vyhledávat": [[4,11]],
"u000a": [2],
"indikováni": [8],
"koliv": [9],
"zaktivována": [8],
"protž": [11],
"en": [5],
"typické": [10],
"u000d": [2],
"správě": [6,1],
"u000c": [2],
"eu": [8],
"značkami": [11],
"používá": [11,6,5,[1,4,8,9]],
"detailnější": [11],
"zvýrazněné": [[4,9]],
"kolik": [[8,9,11]],
"pozvánku": [6],
"u001b": [2],
"stats.txt": [10],
"terminologií": [8],
"terminologi": [11,[1,8,9]],
"schéma": [11],
"foo": [11],
"exclud": [6],
"for": [11,8],
"zvýrazněný": [11],
"pamatuj": [8],
"testování": [2,7],
"doporučováno": [1],
"fr": [5,[4,11]],
"nutné": [[5,6,11],4],
"content": [5,7],
"desetini": [11],
"specializované": [9],
"desktop": [5],
"applescript": [5],
"zapotřebí": [6],
"gb": [5],
"identifikac": [[8,11]],
"class": [11],
"jsou": [11,5,6,1,10,[0,8],3,9,4,[2,7]],
"helplogmenuitem": [3],
"tímto": [[5,8,9,11],4],
"imunní": [10],
"svého": [5,[1,3,4]],
"editoverwritetranslationmenuitem": [3],
"dříve": [6,[8,11]],
"sestává": [9],
"zdrojového": [11,6,5,[8,9,10],[1,3,7]],
"aeiou": [2],
"oba": [6,[10,11],[4,5]],
"prioritní": [1,7],
"zmíněno": [6],
"nenalezen": [5],
"těchto": [11,[5,10]],
"form": [5],
"nechat": [[5,11]],
"dá": [9],
"zaznačena": [11],
"verz": [5,8,[6,10]],
"hh": [6],
"hlavička": [11],
"poznámki": [11,9,7],
"jednom": [[1,3,5,8,11]],
"posílat": [11],
"algoritmus": [3],
"duser.languag": [5],
"vestavěnou": [[4,11]],
"plnou": [11],
"programovací": [11],
"ho": [5],
"poznámka": [11,8,[1,3,9,10],[5,6]],
"našem": [6],
"nastavena": [11],
"jednou": [2,6],
"plní": [6],
"nastaveni": [6],
"file-target-encod": [11],
"legislativa": [6],
"překládaném": [9,1],
"přibližný": [3,8,9,[10,11]],
"context": [9],
"vzdáleným": [6],
"počítejt": [6],
"https": [6,[5,11]],
"nastaveno": [[10,11]],
"id": [11],
"if": [11],
"project_stats.txt": [11],
"detailního": [11],
"ocr": [[6,11]],
"projectaccesscurrenttargetdocumentmenuitem": [3],
"ať": [[9,10]],
"přibližná": [11],
"in": [11],
"ip": [5],
"načítá": [1],
"is": [2],
"identifikaci": [5],
"it": [[1,11]],
"encyklopedi": [0],
"přibližné": [11,8,9,[6,7],10],
"prvnímu": [[2,11]],
"zapsán": [6,8],
"odf": [[6,11]],
"slovinština": [1],
"pohybuj": [11],
"odg": [6],
"hlavičku": [11,8],
"ja": [5],
"multiterm": [1],
"sdílí": [6],
"je": [11,5,6,8,9,10,1,4,3,2,0],
"až": [11,2,[1,4,5]],
"odt": [6,11],
"nepoužívali": [11],
"ji": [[5,6],[8,9,11]],
"gotonexttranslatedmenuitem": [3],
"nplural": [11],
"důvod": [5],
"js": [11],
"jste": [5,[4,6],[8,9],10,[0,1,3,11]],
"segmentovat": [11],
"předdefinovaná": [11],
"zobrazovat": [3,[8,11],[5,6]],
"překladům": [11],
"learned_words.txt": [10],
"vpisujet": [9],
"předdefinované": [[2,7,11]],
"mohou": [11,[1,5,6,10],[2,8,9]],
"vyskytn": [11],
"ke": [[5,6],11,[1,10]],
"abyst": [[6,11],[5,10]],
"rozdíki": [6],
"překladač": [5],
"místního": [6,11],
"čtěte": [5],
"ftp": [11],
"rozlišnou": [11],
"viewdisplaymodificationinfoallradiobuttonmenuitem": [3],
"ku": [11],
"draw": [6],
"poutřebujet": [6],
"specifikujt": [11],
"zrušení": [11],
"starou": [5],
"nepatrný": [11],
"žádané": [11],
"překladem": [[9,11],8,[1,3]],
"le": [1],
"přibližně": [10,11],
"rozdíli": [11],
"dswing.aatext": [5],
"zvolena": [5],
"projektových": [6,8],
"spárováni": [11],
"vyobrazen": [4],
"pohledu": [9],
"prahem": [11],
"slovníků": [4,7,[0,9,11]],
"lu": [2],
"ne-číslic": [2],
"přetáhnět": [5],
"povolí": [5],
"skripti": [11,8],
"cycleswitchcasemenuitem": [3],
"mb": [5],
"načíst": [[3,8,11]],
"omegat.png": [5],
"pokládána": [11],
"editac": [9,[8,11],[3,10]],
"mm": [6],
"levém": [11],
"entri": [11],
"uvozování": [[2,7]],
"platit": [5],
"francouzským": [5],
"linuxové": [9],
"mr": [11],
"ms": [11],
"mt": [10],
"mu": [6],
"obě": [[5,11]],
"my": [5],
"uskutečněna": [11],
"disk": [[5,8]],
"na": [11,5,6,8,9,10,4,3,1,0,2,7],
"uvedeni": [[3,8]],
"převzal": [11],
"provádění": [[6,11]],
"uvedeno": [6],
"ne": [11,[5,6],2,[4,10]],
"nastavením": [[5,11]],
"vypočítané": [11],
"uvozuj": [2],
"předem": [11],
"nl": [6],
"situacím": [10],
"nn": [6],
"datum": [11],
"řadě": [10],
"nespárovaných": [11],
"no": [11],
"umistění": [5],
"instalac": [5,4,7,8],
"zvolené": [6],
"měsíce": [6],
"gotohistoryforwardmenuitem": [3],
"určující": [5],
"načt": [8],
"síť": [5],
"prvního": [11,1],
"nemusí": [[1,11]],
"dialog": [[1,11]],
"znalosti": [6],
"od": [11,5,6,[1,3],9],
"of": [7,5,0],
"umožní": [11,8,[1,6]],
"liší": [5],
"ok": [[5,8]],
"zaškrtávací": [11,4],
"jednoduchých": [11],
"provádět": [6,11],
"or": [9],
"změnám": [[10,11]],
"os": [[6,11]],
"hodnotou": [2,10],
"akceptován": [10],
"tudíž": [6],
"zástupných": [11,6],
"editinserttranslationmenuitem": [3],
"reagovat": [9],
"spárování": [11],
"pc": [5],
"formou": [8],
"překladačům": [11],
"uveden": [[5,9]],
"hranic": [2,7],
"po": [11,[2,5,6,9],1,[8,10]],
"popsán": [5],
"optionsglossarystemmingcheckboxmenuitem": [3],
"neumí": [6],
"pt": [[4,5]],
"sbírka": [[1,2,9]],
"uvedená": [5],
"sžijet": [5],
"definování": [11],
"souhrnu": [11],
"má": [11,5,[6,10],[4,8]],
"vezm": [5],
"manuálech": [6],
"mé": [5],
"sebe": [1,[5,11]],
"restartujt": [3],
"formátem": [6],
"edit": [[5,8]],
"uvedený": [11],
"editselectfuzzy5menuitem": [3],
"konfiguracnim-souborum": [5],
"chtěli": [[2,3]],
"přistupovat": [[0,5]],
"příkazovou": [5],
"vším": [5],
"spouštěcím": [5],
"includ": [6],
"prohledávat": [11],
"generováni": [10],
"následuj": [11],
"ní": [1],
"minut": [6,8],
"několikrát": [11],
"použitím": [11,8,[9,10]],
"takovémto": [9],
"svůj": [5,[6,8]],
"povolit": [11,[3,5,8]],
"nepřidá": [[6,10]],
"doporučit": [6],
"daného": [[5,11]],
"sc": [2],
"frázi": [11],
"se": [11,[5,6],8,9,1,10,4,2,[0,3],7],
"načtení": [[1,6,8,11]],
"fráze": [11],
"si": [5,11,6,4,1,0,[9,10],[2,3,8]],
"zadá": [5],
"interv": [11,[6,8]],
"dodané": [11],
"ta": [11],
"editoverwritesourcemenuitem": [3],
"odpojili": [11],
"běžnou": [[5,10,11]],
"zdarma": [5],
"enforc": [10],
"tj": [11,[1,4,6],[0,5,8,9]],
"remov": [5],
"rezervuj": [5],
"skriptu": [[5,11],8],
"tm": [10,6,11,8,[7,9]],
"to": [11,5,6,9,8,[1,4,10],[0,2],7],
"v2": [5],
"párují": [11],
"tu": [11,[0,1]],
"tw": [5],
"zálohuj": [6],
"nalezeni": [[9,11]],
"definována": [[10,11]],
"adresáři": [5,11,10,8,6,1,0],
"ty": [11,[5,6,8]],
"skriptů": [[8,11]],
"dialogu": [10],
"nalezeno": [1],
"umístěných": [11],
"definováni": [[8,11]],
"viewmarkautopopulatedcheckboxmenuitem": [3],
"krýt": [8],
"údajů": [11],
"slovníku": [4,[1,11]],
"projectwikiimportmenuitem": [3],
"editační": [9],
"klávesnic": [9],
"countri": [5],
"definováno": [[6,11]],
"regulárnímu": [11],
"dokončili": [6],
"dodaná": [10],
"dali": [11],
"libovolnou": [10],
"nekorespondující": [11],
"takovém": [[6,11],[5,9,10]],
"un": [1],
"napíšou": [8],
"nalezena": [[1,6,11]],
"up": [11],
"nastavení": [11,5,8,4,[3,6],[9,10],7,[1,2]],
"stávajíchího": [8],
"ut": [11],
"při": [11,[5,6],8,[2,4],[1,10]],
"definovaný": [5],
"najdet": [[6,11],9,8],
"zazipované": [5],
"že": [11,5,6,[1,9,10],4,[0,3]],
"this": [[2,5]],
"neváhejt": [6],
"každému": [11],
"ve": [11,5,8,6,9,1,4,10,2,[0,3]],
"obsahovalo": [11],
"zakládající": [5],
"vi": [5],
"uvozovkami": [1],
"prohlížeči": [[5,8]],
"uvědomili": [4],
"slovníki": [0,4,7,[6,10,11],[1,8,9]],
"generování": [[10,11]],
"definovaná": [3],
"vs": [9,11],
"upravili": [3],
"logických": [11],
"vy": [[4,11]],
"pojmem": [1],
"zrušena": [8],
"rozhodnet": [11],
"čárkou": [11,[1,2]],
"pamatovat": [6],
"zobrazili": [5],
"licenc": [8],
"groovy.codehaus.org": [11],
"stránkách": [11],
"repo_for_omegat_team_project": [6],
"zrušt": [11],
"řáká": [6],
"backspac": [11],
"detaili": [[5,8]],
"slovního": [11],
"řetězec": [11,8,9],
"stromové": [10],
"emac": [5],
"org": [6],
"distribut": [5],
"čím": [11],
"xf": [5],
"rozbalovací": [11],
"produktivitu": [11],
"glosářů": [1],
"dovoluj": [5],
"odmítla": [6],
"zrušeno": [11],
"té": [6,5],
"poznámku": [11],
"jeden": [11,8,9,1,[0,6,10]],
"být": [11,1,5,6,3,10,[0,4,9]],
"stromová": [10],
"xx": [5,11],
"výběr": [8,11,3,[0,5]],
"xy": [2],
"vašeho": [6,[4,5,9],11],
"sourc": [6,11,10,[5,8],[7,9]],
"nalezené": [[1,11]],
"riziko": [1],
"nezmění": [11],
"zůstanou": [11,[5,10]],
"dále": [5,11],
"nalezení": [5,3],
"typi": [11,[6,8,10]],
"odstavců": [11,8],
"type": [6],
"speciální": [11],
"sloužícího": [6],
"danému": [11],
"hledej": [2],
"toolssinglevalidatetagsmenuitem": [3],
"pustit": [6],
"správu": [6,1],
"páruje": [5],
"vypočítávána": [9],
"nalezená": [[9,11]],
"projectaccesssourcemenuitem": [3],
"psaní": [11,[5,8]],
"yy": [9,11],
"zatrhnutá": [11],
"stavové": [[5,9]],
"obsahi": [[5,10]],
"za": [11,5,[6,9],4,[1,2,3]],
"jedinečných": [11,9],
"stavová": [9,7],
"obsahu": [[1,6,8,11]],
"umožnít": [9],
"správném": [[0,5]],
"ze": [11,6,5,[7,8,9,10]],
"japonštinu": [11],
"push": [6],
"zh": [6],
"readme_tr.txt": [6],
"penalti": [10],
"japonská": [11],
"nalezený": [[1,11]],
"stáhněte": [5,0],
"těmi": [2],
"japonské": [11],
"exportujet": [11],
"zpracovávaného": [10],
"repozitáři": [6],
"navíc": [[1,2,5,8,9]],
"rychlého": [5],
"utf8": [1,[8,11]],
"sníženi": [10],
"postupují": [2],
"vytvořni": [8],
"jednak": [9],
"snížena": [10],
"průběhu": [[1,10,11]],
"ujistět": [5,[1,4]],
"dané": [[8,10],9],
"jednat": [0],
"dark": [11],
"naleznem": [3],
"kompletaci": [11],
"prochází": [11],
"power": [11],
"daná": [11,[1,6]],
"naleznet": [[5,10,11],8],
"nápovědě": [6],
"vyhledávacího": [11],
"tag-valid": [5],
"jednotlivými": [1],
"včetně": [11,[2,8,9]],
"spustí": [5,11],
"nalézt": [6],
"jakého": [9],
"raději": [1],
"kdekoliv": [10,[4,5,11]],
"zpracuj": [[5,11]],
"aplikaci": [5,6,11,[8,10],[0,1,2,3,4,7]],
"u0009": [2],
"xhh": [2],
"revis": [[0,6]],
"u0007": [2],
"typu": [11,6,0,[5,8]],
"repositori": [6,10,7],
"kód": [3,11,4,5],
"minimum": [11],
"následný": [11],
"nabídc": [11,3,8],
"zase": [11],
"údaje": [11,6],
"výkonný": [11],
"nápověda": [[3,7],8],
"zpřístupní": [[5,11]],
"data": [11,6],
"lowercasemenuitem": [3],
"snadno": [11],
"wiki": [0],
"firefox": [11,[2,4]],
"zpomalením": [6],
"adresářů": [11,8],
"importovaných": [6],
"nápovědi": [6],
"přetažený": [9],
"jedinečný": [[3,9,11]],
"vložil": [11],
"hledat": [11,8,[3,7]],
"obsahujících": [11],
"vložit": [11,[3,8],9,10,[1,6]],
"číst": [6],
"daný": [11,10,8,[1,5,6,9]],
"sens": [11],
"vašemu": [5],
"už": [11,9,[1,5,6,10]],
"kurzor": [11,[8,9]],
"chybové": [5,6],
"taktéž": [6],
"nepocházejících": [11],
"sítě": [6],
"proto": [[1,6,10]],
"openoffic": [4,11],
"proti": [6],
"svou": [[6,11]],
"volbou": [11],
"prostředki": [1],
"note": [2,9],
"japonštině": [5],
"formátovaného": [6],
"optionsautocompletechartablemenuitem": [3],
"line": [5],
"minimalizována": [9],
"vhodné": [5,4,[6,11]],
"potvrzování": [10],
"serveru": [6,5,11],
"exportována": [11],
"regulárních": [2,11,7,5],
"přepínat": [6],
"kapitálki": [3],
"zobrazí": [11,8,[4,5,9]],
"git": [6,10],
"vzhledu": [9],
"přeneseni": [6],
"reprezentuj": [11],
"zato": [1],
"prospěšné": [11],
"načteni": [5],
"vhodný": [[4,11]],
"xx-yy": [11],
"zvolít": [[1,11]],
"tlačítko": [11,5],
"will": [5],
"povolena": [11],
"detailů": [[6,8]],
"virgul": [1],
"venku": [9],
"glosáři": [1,9,[7,11]],
"tlačítka": [11,[7,9]],
"neovlivní": [6],
"follow": [6],
"chtějí": [2],
"povoleni": [11],
"efektu": [11],
"optionsspellcheckmenuitem": [3],
"sloužících": [6],
"nehodí": [[8,9]],
"postupujt": [[0,5]],
"exportovaný": [8],
"nabídn": [5],
"aktuálnímu": [[8,10,11]],
"proměnných": [11],
"tyto": [11,6,4,5,[8,9,10],1],
"volných": [[4,11]],
"jedna": [11,[2,5,6]],
"optionssetupfilefiltersmenuitem": [3],
"aplikací": [6,4],
"rozsah": [2],
"altgraph": [3],
"účelům": [11],
"především": [[2,6]],
"budu": [4],
"vložen": [8,9],
"podporovaná": [11],
"hledát": [11],
"zahrnuti": [11],
"nainstalovat": [0,7],
"aktivovali": [9],
"hned": [[1,8,11]],
"xml": [11,1],
"zkopírován": [[8,9,11]],
"adres": [5],
"zahrnuta": [[6,11]],
"klávesnici": [[3,11]],
"úpravu": [[8,11]],
"úpravi": [11,6],
"úprava": [11],
"přejmenování": [11],
"projdět": [2],
"podporováni": [11],
"spustít": [5],
"befor": [5],
"podporované": [[6,8]],
"vztahující": [[9,11]],
"bude": [11,5,6,[8,9],[1,10],4,2,3],
"tar.bz": [0],
"kolidovat": [5,3],
"vytvořit": [11,8,3,[1,5],[6,9],[7,10]],
"požadavki": [8],
"opětovnému": [6],
"chybějícími": [9],
"zdvojený": [2],
"linuxu": [5,[1,7]],
"začlenění": [5],
"odpojí": [9],
"finder.xm": [11],
"zapsaného": [4],
"pozadím": [8],
"termíni": [1],
"odpovídat": [2,[4,11]],
"kontext": [[9,11]],
"xlsx": [11],
"duplicitních": [11],
"práva": [5],
"spouštěcí": [5],
"vysvětlení": [5],
"assembledist": [5],
"vyhodnocují": [11],
"formátování": [6,11,10],
"jednotki": [11],
"prohledávání": [2],
"formáti": [6,[8,11],9],
"typů": [6],
"právi": [5],
"jednotka": [10],
"formulář": [5],
"pravý": [9],
"míře": [10],
"odstupech": [6],
"sloupci": [11,8],
"target.txt": [11],
"formátu": [[6,11],1,0,[5,8]],
"standard": [1],
"nahoru": [11],
"odlišný": [11],
"tučně": [11,[1,9]],
"aktuální": [8,11,[5,6,9],[3,10]],
"překladovými": [[10,11]],
"zobrazovacích": [6],
"nové": [8,[5,11],[1,3],2],
"přidáním": [[1,10]],
"nanejvýš": [3],
"kontrolu": [11,4,[6,8]],
"zahrnutí": [6],
"otevř": [8,11,9,[1,4]],
"zůstat": [11,10,[5,6]],
"detekuj": [[1,5]],
"tím": [6,11,8,[4,9]],
"odlišné": [11],
"nameon": [11],
"optionsglossarytbxdisplaycontextcheckboxmenuitem": [3],
"kontroli": [4,7,11,8],
"pak": [11,8,6,10,[5,9],4,3,1],
"rychlejší": [1],
"odlišná": [9],
"nová": [5,[3,11]],
"gotonextnotemenuitem": [3],
"tar.gz": [5],
"gpl": [0],
"kontrola": [4,[2,10,11],[1,3,6,7]],
"omega.project": [[5,9,11]],
"odlišně": [11],
"sloupec": [1,8],
"právě": [8,10,[9,11],5],
"specifikován": [6],
"stranách": [6],
"smaže": [8],
"ukončením": [11],
"pokusit": [11],
"pravopisu": [4,11,7,10,[1,2,3,8]],
"list": [7],
"deaktivováno": [8],
"běžném": [5],
"upravovaným": [8],
"lisa": [1],
"přejmenována": [6],
"poněkud": [5],
"smažt": [[6,9]],
"azur": [5],
"každém": [6,11],
"klávesových": [3,7,2],
"dynamický": [11],
"odstraněním": [[9,11]],
"cíl": [[8,11]],
"potřebujt": [6],
"otevírá": [[1,8]],
"nabízí": [11,[1,8,9]],
"souborovém": [11],
"hledání": [11,8,2,[1,5]],
"portugalština": [4],
"rozpoznání": [6],
"nově": [[1,11],8],
"odstavec": [11],
"portugalštini": [5],
"stručného": [5],
"složc": [[5,11]],
"vkládat": [6,[1,11]],
"slova": [11,9,8,2,1,[4,5,10]],
"slovo": [11,[1,8],[4,5,9]],
"návrat": [9],
"slovi": [[6,11]],
"mrtvá": [6],
"with": [6,5],
"slovu": [1],
"ovládát": [6],
"pdf": [6,[7,8,11]],
"smazáním": [11,10],
"přidělt": [6],
"zaznačít": [5],
"nový": [[6,11],8,[3,4],1,[5,9]],
"vybírat": [11],
"uživatel": [11,5,8,[2,3,9]],
"zachována": [[5,10]],
"lokalizována": [5],
"takovými": [10],
"vlastnostech": [11],
"toolsshowstatisticsmatchesmenuitem": [3],
"spouštění": [5,7],
"neuložili": [8],
"čtyři": [8],
"uživatelské": [[5,6],[9,11]],
"stisknět": [11,9,1],
"viewdisplaymodificationinfononeradiobuttonmenuitem": [3],
"zachované": [10],
"desir": [5],
"zachováni": [11],
"protož": [11,5,[1,6,9]],
"většinu": [11],
"maximalizac": [9],
"širokou": [[4,11]],
"repozitář": [6,8,5],
"popisovat": [6],
"uživatelský": [[5,11]],
"změn": [6,11],
"dosáhnout": [5],
"přepsat": [11,[3,8]],
"též": [[1,5]],
"začínající": [11],
"stačí": [5,[9,10,11]],
"neodpovídá": [8],
"textem": [11,10,6,[1,9]],
"uživatelská": [7,[3,5,8]],
"zavř": [8,11],
"projectaccesswriteableglossarymenuitem": [3],
"převed": [8],
"synchronizují": [6],
"novou": [11,5],
"vhledem": [9],
"gui": [5],
"návrhi": [11,[3,8,9,10]],
"všimnět": [5,[0,10,11]],
"hledaná": [6],
"pravopi": [4],
"entitu": [11],
"ochraně": [11],
"rovnající": [[8,11]],
"doplnit": [[6,11]],
"sentencecasemenuitem": [3],
"strojového": [8,11,9],
"stejné": [11,[1,5,6,9]],
"neexistují": [11],
"uhhhh": [2],
"minimalizuj": [9],
"možnost": [11,8,[5,6],9,10],
"nezbytné": [6],
"mujprojekt": [6],
"stejný": [11,[5,6],[4,8]],
"substituci": [8],
"uživatelským": [5],
"optionssentsegmenuitem": [3],
"nezáleží": [11],
"popisek": [9],
"samohlásku": [2],
"zásadní": [11],
"přihlašovacích": [11],
"vystavěn": [11],
"užitečných": [2],
"optionsaccessconfigdirmenuitem": [3],
"zahájít": [5],
"odstraňuj": [11],
"charact": [6],
"úpravě": [6],
"mezerou": [2,11],
"vlastnost": [[6,11]],
"test.html": [5],
"xxx": [10],
"stejně": [11,[6,9],[0,5,8,10]],
"hledaný": [[2,11]],
"posledním": [8],
"smalltalk": [11],
"opakovaným": [8],
"budou": [11,8,6,9,5,[1,10],[2,4],3],
"úrovni": [11,8,1,5],
"zpětným": [5],
"zájmu": [1],
"objem": [5],
"pseudotranslatetmx": [5],
"připojení": [[4,5,6]],
"neurčitý": [2],
"revizi": [10],
"slově": [11,8],
"nefunguj": [4],
"parsovat": [6],
"přesuňt": [10],
"dopad": [11],
"přednastavených": [11],
"změnách": [9],
"výpadek": [8],
"dodatečný": [5],
"prostor": [11],
"přístupových": [11],
"vyexportujt": [6],
"targetlanguagecod": [11],
"změnami": [5],
"ignorovat": [11,[4,10]],
"nazývám": [11],
"stejná": [[2,6,11]],
"pokračovat": [11],
"kořenovém": [6],
"překládat": [11,[6,10]],
"zabránit": [11],
"vytvořen": [6,[1,5,11]],
"vykazovat": [9],
"dodatečná": [11,10],
"odpojt": [6],
"správného": [5],
"zadaném": [5],
"názvem": [5,[0,10]],
"současného": [6],
"dodatečné": [11,[1,2,5]],
"které": [11,6,9,5,8,10,1,[2,4],3],
"zaškrtávající": [5],
"polích": [6],
"většině": [11,[3,5,6]],
"zvýrazněním": [8],
"náhrada": [11],
"která": [11,5,[6,10],[1,4,8,9]],
"aktivováno": [11],
"mírou": [9],
"dokonc": [11,[5,9]],
"vlevo": [[6,11],8],
"náhradu": [11],
"množství": [11,[2,5,10]],
"dolním": [9],
"instalován": [8],
"název": [11,9,[1,5,6,8],4],
"encyclopedia": [0],
"nemát": [[4,11]],
"sdílet": [6],
"týče": [6],
"temný": [11],
"který": [11,[5,8],9,6,4,10,[1,3]],
"prostředník": [6],
"projekti": [8,11,6],
"optionstagvalidationmenuitem": [3],
"projektům": [8],
"vyplněné": [[3,8,11]],
"slovník": [4,0,[7,8,9,11]],
"nalezenými": [5],
"číslem": [8],
"nějakým": [1],
"pt_br": [4,5],
"shodných": [11],
"aplikovat": [11],
"a-z": [2],
"nabízející": [11],
"zastaveno": [5],
"poměrně": [11],
"dostupného": [11],
"zobrazování": [6,[8,11]],
"všemi": [[5,11]],
"konzoli": [5],
"revizí": [6],
"vyplněný": [[8,11]],
"můžete": [11,5,[4,6,9],8,1,10,[0,2,3]],
"vložený": [1],
"onlin": [4,6],
"současné": [6],
"stažení": [5,1],
"nalezen": [11],
"měsíčně": [5],
"vložená": [11],
"png": [5],
"plyne": [5],
"vložení": [11,[8,9]],
"konc": [2],
"nějaký": [[8,11],1],
"vložené": [[1,6,8,11]],
"současně": [11,1],
"menší": [5],
"šablona": [11],
"javascript": [11],
"kromě": [11,[0,2,6]],
"mediawiki": [11,[3,8]],
"input": [11],
"stažený": [5],
"nějaké": [[5,6,8]],
"návrhů": [[4,10,11]],
"přidát": [11,3],
"chvíli": [4,11],
"nepřeložených": [11],
"několikátý": [8],
"nějaká": [11],
"pod": [5,[0,6],1],
"potlačit": [11],
"přihlašovací": [11],
"dlouhý": [11],
"úložiště": [6,11],
"překladových": [6,10,11,[7,9],5],
"volí": [6],
"dvousměrný": [6],
"zveřejněné": [0],
"pop": [11],
"metaznaků": [2],
"registrujet": [8],
"svých": [[9,10]],
"found": [5],
"šabloni": [11],
"kroků": [11],
"záznami": [1,[8,11],[3,5,7]],
"panelu": [5,11],
"změně": [11],
"záznamu": [1,[8,11]],
"službám": [11],
"slovní": [11],
"formátovací": [11],
"imunitě": [10],
"aplikováno": [10],
"formách": [11],
"změní": [8,[5,10,11]],
"překladovou": [6,[8,10,11]],
"potřebi": [11,6],
"prvních": [11,8],
"kopi": [[4,11]],
"komprimováni": [10],
"jazykového": [6],
"rozdílů": [11],
"googl": [5,11],
"podívát": [11],
"aplikována": [11],
"opendocu": [11],
"existujících": [11],
"omylem": [11],
"download.html": [5],
"režimu": [5,11,9],
"volání": [11],
"klíčových": [11],
"založený": [[1,11]],
"úložišti": [11],
"tým": [11,[3,7]],
"chybějících": [8],
"ovlivnit": [11],
"align": [11],
"běh": [5],
"během": [11,6,[5,10],[8,9]],
"sourceforg": [3,5],
"založenou": [4,11],
"goodi": [5],
"začátkem": [2],
"vymezení": [[2,11]],
"prohledat": [11,8],
"dlouhé": [11],
"editmultipledefault": [3],
"editfindinprojectmenuitem": [3],
"pracuj": [5,11],
"setmentu": [9],
"pro": [11,5,6,8,2,1,3,[4,9],10,7],
"založené": [11,8],
"vyhledat": [11],
"musí": [[5,6],1,[3,11],4,10],
"warn": [5],
"technetwork": [5],
"zajištění": [8],
"exportu": [11],
"synchronizovat": [5],
"můžeme": [6],
"funkc": [11,8,9,[1,4]],
"plural": [11],
"obou": [6],
"jazykovému": [4],
"překlad": [8,11,9,6,3,[1,7],10,5],
"dokumentů": [[6,11],9],
"projektem": [11,6],
"přetažením": [5],
"změnu": [11,10,[5,6,9]],
"otevíráním": [11],
"aplikování": [11],
"uživatelském": [[9,11]],
"pozic": [11,4],
"změni": [11,5,[8,10],[2,3,6]],
"chcete": [11,5,[6,9],[3,4,8]],
"colour": [11],
"n.n_windows.ex": [5],
"zaškrtnout": [11],
"chang": [5],
"pop-up": [1],
"obsahují": [11,[6,10],[1,5,9]],
"projektu": [6,11,[3,8,10],9,[4,5],[1,7]],
"shodují": [9],
"hodnoceni": [10],
"projektů": [11,6],
"program": [5,6,[1,4,11]],
"dál": [5],
"týmovém": [[10,11]],
"výpadkem": [8],
"smazání": [6,11],
"existujícího": [1],
"formát": [1,[6,7]],
"tipi": [4,[6,7,11]],
"začali": [6],
"nalezli": [5],
"upravít": [[3,11]],
"pozor": [6,[4,8],9],
"nabídkami": [5],
"jednotek": [[10,11]],
"založeni": [0],
"učiněná": [5],
"založeno": [11],
"n.n_mac.zip": [5],
"jistě": [1],
"podobnost": [11],
"tabl": [2,3,[7,9],11],
"záložní": [[6,8]],
"nalezených": [9],
"stahovat": [11],
"povšimnět": [5,11],
"aktuálně": [8,3,[9,10,11]],
"přechodu": [11],
"theme": [11],
"záznamů": [11,[8,9]],
"řádkem": [5],
"nebudem": [6],
"editor": [11,[5,6,8]],
"pseudotranslatetyp": [5],
"přesunut": [9],
"výrazem": [9],
"dostatečně": [[6,11]],
"nebudet": [11],
"popisuj": [6],
"zašeděn": [8],
"téma": [6],
"zapisuj": [11],
"skutečnosti": [6],
"systémový": [11],
"smysluplnější": [[10,11]],
"tadi": [5],
"projectclosemenuitem": [3],
"project_save.tmx.nahradni": [6],
"viewmarknonuniquesegmentscheckboxmenuitem": [3],
"vyhnuli": [6],
"praxi": [5],
"smazáno": [11],
"dostatečné": [10],
"hodnotám": [5],
"překladech": [[2,7]],
"smazáni": [[6,11]],
"vloženo": [11],
"zobrazováni": [8],
"zobrazované": [11],
"buňkách": [11],
"naplnění": [6],
"extrahovat": [11],
"findinprojectreuselastwindow": [3],
"chce": [[6,9]],
"dostatečná": [11],
"readme.txt": [6],
"dokumentu": [11,[6,8],[1,3,9]],
"něco": [6],
"languagetool": [11,8],
"oken": [11,[5,8]],
"úvodního": [5],
"source.txt": [11],
"files.s": [11],
"rovná": [11],
"zleva": [6],
"společných": [9],
"histori": [8],
"exchang": [1],
"request": [5],
"strojovým": [[3,8]],
"zkopíruj": [11,[8,10]],
"dokončit": [11],
"currseg": [11],
"takových": [[6,11]],
"operaci": [5],
"point": [11],
"jmenuj": [5,11],
"zveřejněni": [6],
"přepsána": [[5,10]],
"prázného": [8],
"zástupc": [5],
"dokumenti": [6,8,11,[3,5],10],
"licenční": [5],
"prvotřídní": [11],
"procházet": [3,[8,11],[5,9]],
"dvojitého": [5],
"poslání": [11],
"přecházet": [9],
"nezobrazovat": [[3,8]],
"alternativa": [[5,11]],
"nalézat": [11],
"ikonu": [5,8],
"ikoni": [5],
"alternativu": [[8,11]],
"odporovat": [11],
"procesu": [11],
"account": [[5,11]],
"identifikátoru": [11],
"dhttp.proxyhost": [5],
"pluginu": [2],
"kliknět": [11,5,8,[4,9]],
"výchozím": [11,8,6,[5,10],[1,2,9]],
"této": [11,5,[1,4,6,9]],
"opakovaně": [4],
"zorientujet": [11],
"výskyti": [11,4],
"autorských": [8],
"načítáni": [10,11],
"případech": [[6,11]],
"každý": [[8,11],6],
"upřednostnit": [11],
"zápatí": [11],
"vyhodnocuj": [11],
"výskytu": [11],
"nalezeným": [9],
"prototypech": [11],
"uvedeném": [9,[1,6]],
"you": [9],
"nezobrazí": [[8,11]],
"kanadské": [11],
"strukturovaných": [1],
"contient": [1],
"kritériím": [11],
"tagu": [11,8],
"přeloeno": [11],
"plugini": [11],
"týkají": [6],
"configur": [5],
"poděkování": [8],
"tagi": [11,6,8,3,5],
"obrazovc": [5],
"reprodukovat": [6],
"nepoužitelné": [11],
"vyhledejt": [0],
"produktů": [6],
"nejvyšší": [9,10],
"potřebné": [[4,5],0],
"zobrazovací": [6],
"prostředí": [5,9,[6,10,11]],
"nakliknutím": [11],
"hostované": [11],
"optionsworkflowmenuitem": [3],
"pozornost": [5],
"releas": [6],
"jiným": [[5,6,11]],
"vyber": [8,11],
"sparc": [5],
"odečítání": [2],
"sobě": [2,11],
"hodí": [11],
"hexadecimální": [2],
"míri": [10],
"aktualizován": [[1,11]],
"téže": [11],
"zobrazeným": [11],
"dalšímu": [[8,11]],
"procentní": [10],
"podadresáři": [[5,10],[0,6,11]],
"segmentac": [11,[2,6,8],3],
"kopírování": [4],
"shodující": [11],
"podpůrná": [6],
"obchodu": [5],
"míra": [9],
"opakované": [11],
"urychlil": [6],
"signalizac": [2],
"vydána": [8],
"zadejt": [5,[1,6,11]],
"oddíli": [6],
"slovinský": [9],
"obrácenými": [11],
"výsledků": [11],
"prostě": [8],
"zaměří": [11],
"načítání": [[6,11]],
"danou": [[9,11]],
"všeobecné": [11],
"jiného": [[4,8,9]],
"neseznámít": [6],
"přiloženo": [11],
"doplnění": [11,3,8,5],
"otevřít": [5,8,[3,6],11],
"instalátor": [4],
"rozeznatelné": [1],
"obrací": [2],
"okno": [11,8,9,5,[1,7],[3,4]],
"překontrolujt": [8],
"nikdi": [11],
"forward-backward": [11],
"přípustná": [6],
"definici": [3],
"nehledě": [11],
"zapisovat": [8],
"dvojklikem": [5],
"základní": [5,11],
"poskytuj": [11,[5,8]],
"přepínacích": [11],
"přejít": [9,[3,7,8,11]],
"okna": [11,9,8,5,7,[0,6]],
"taki": [5,11],
"file-source-encod": [11],
"dokumentech": [11],
"psát": [11],
"kritérii": [11],
"session": [5],
"kritéria": [11],
"uprostř": [2],
"zobrazovalo": [6],
"chod": [5],
"textový": [8,[6,11]],
"dosáhnet": [[6,11]],
"zobrazovali": [11],
"systémovými": [3],
"terminologických": [1],
"všech": [11,[6,8]],
"potřebuj": [6],
"celkovém": [11],
"zařazeni": [11],
"editexportselectionmenuitem": [3],
"zalomení": [11],
"speciálních": [11],
"textové": [11,6,2],
"home": [5,[0,1,2,3,4,6,8,9,10,11]],
"neplatné": [[5,6]],
"dvojté": [8],
"projectaccesstargetmenuitem": [3],
"oprav": [[4,8]],
"vypnuta": [[8,11]],
"zvlášť": [11],
"porozumění": [6],
"opakování": [11,8],
"pochází": [11,9],
"nejpřibližnější": [[9,11]],
"varianti": [[2,11]],
"původní": [11,9],
"prozradí": [5],
"ponechá": [11],
"vydání": [8],
"uzavřen": [6],
"naskenovaných": [6],
"administrátorem": [11],
"výrazů": [2,11,7,[5,9]],
"výskytů": [11,9],
"variantu": [9],
"aligndir": [5],
"změna": [11],
"system-host-nam": [11],
"action": [8],
"restartovat": [[3,11]],
"odstavc": [[6,11]],
"původním": [[9,11]],
"užitečný": [6],
"ukázaných": [9],
"jmen": [11],
"obecně": [11,2],
"užitečné": [5,[9,10]],
"creat": [11],
"python": [11],
"tagů": [11,6,5,3,8],
"es_mx.dic": [4],
"odstranít": [11],
"zpracován": [11],
"infix": [6],
"projektová": [11],
"přihlášení": [11,[3,5]],
"zálohi": [10],
"operac": [6],
"projektové": [[6,10,11]],
"tarbal": [0],
"očekává": [5],
"účastníci": [6],
"rozhodnout": [11],
"užitečná": [11,6],
"měsíc": [6],
"špatně": [11],
"také": [11,5,9,6,1,[8,10],[2,3,4]],
"kapitol": [11,[6,9]],
"úpravou": [[8,11]],
"projektový": [6,[8,10]],
"výsledná": [5],
"prázdný": [11,6,8,10,[1,3]],
"samostatných": [11],
"kroku": [8,[1,6,10,11]],
"pohyb": [11,[8,9]],
"obecné": [11,[1,7]],
"file": [11,5],
"přidá": [[6,11]],
"užívání": [6],
"adresa": [5],
"prázdné": [11,[3,5,9]],
"automatickému": [11],
"vyskytují": [[1,11]],
"kroki": [[4,6,11]],
"normálním": [11],
"vyhledávané": [11],
"škálu": [[4,11]],
"projektově": [11],
"pomoci": [5,11],
"nahrazeno": [9],
"menu": [5,[1,11],9,[4,8]],
"vkopírovat": [4],
"dvojitým": [5],
"nahrazeni": [6],
"přesunet": [9],
"vezmet": [3],
"prostý": [[1,6]],
"a-za-z": [2,11],
"nyní": [6],
"okně": [11,8,1,4,6,5,[9,10]],
"týmového": [6,7],
"odpovídajícího": [9],
"klientem": [5],
"specifikuj": [11],
"chovají": [11],
"rozdělen": [[9,11]],
"pracujet": [11,6],
"řídící": [2],
"source-pattern": [5],
"celého": [11],
"takž": [11,6,[1,10],[4,5]],
"barev": [[8,11]],
"nazevprojektu-omegat.tmx": [6],
"zajistit": [11],
"tato": [11,8,5,9,[2,3,6]],
"práce": [6,10,[5,9,11]],
"prázdná": [6],
"jednodušší": [6,4],
"nikterak": [6],
"nabízeného": [11],
"práci": [6,11,[4,5,9]],
"segmentům": [11],
"vzdáleného": [6,[5,8]],
"skriptovacím": [5],
"poznámkách": [11],
"výsledki": [[2,8],[1,11]],
"představující": [11],
"vedlejších": [10],
"základ": [1],
"anebo": [11,[2,5]],
"přišli": [6],
"seznamu": [11,8],
"true": [5],
"prioritního": [1],
"startovací": [5],
"groovi": [11],
"přístupový": [11],
"těmto": [[1,6,11]],
"předvolbách": [8],
"kteří": [2],
"protiklad": [11],
"kmenueditor": [5],
"výběrem": [11,8,3],
"chybí": [1,[2,9]],
"nepřekládejt": [11],
"nahradit": [11,8,3,7],
"segmentem": [[5,11]],
"stát": [11,[6,10]],
"pomocí": [11,6,9,[1,2,5]],
"výjimki": [11],
"vyhledávání": [11,2,6],
"bert": [5],
"rozbalít": [5],
"smazat": [11,6,5],
"master": [6],
"kmenuedit": [5],
"výjimku": [11],
"relevantní": [[3,8,11]],
"deklinaci": [1],
"nezohledňovat": [11],
"příp": [9],
"varianta": [5],
"xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx": [5],
"následující": [11,2,[3,5,6],[0,8,9]],
"writer": [6],
"automatického": [11],
"ekvivalentním": [0],
"skrolovací": [11],
"dalloway": [11],
"rubi": [11],
"chyb": [8],
"španělština": [4],
"objevit": [[3,5]],
"dvoujazyčné": [11],
"určuj": [[5,11]],
"výrazu": [11,5],
"neměnitelný": [11],
"přeloženi": [11,10,[5,9]],
"metoda": [5],
"zobrazením": [6],
"přeloženo": [11],
"červeně": [11],
"prioritních": [1],
"výrazi": [2,7,11,1,[3,4]],
"zkopírujet": [[9,11]],
"metodi": [11,5],
"výjimka": [11],
"zkopírováni": [9],
"minimální": [6],
"varovat": [5],
"metodu": [11],
"prostého": [6,[1,11]],
"takovýto": [11],
"programu": [5,6,[7,8,9],10],
"řečeno": [11],
"těm": [11],
"španělštinu": [4],
"user.languag": [5],
"programi": [[5,6]],
"regex": [2,7],
"meta": [3],
"mujglosar.txt": [1],
"řetězc": [11,1,6],
"dvoujazyčný": [6],
"delším": [9],
"akci": [[0,5,8]],
"úprávám": [1],
"specifická": [[8,11],10],
"vrstvu": [6],
"zahrno": [11],
"mnoho": [11],
"odpovídajícím": [[6,11]],
"interním": [11],
"minimálně": [[10,11]],
"mnoha": [6],
"mít": [11,5,[1,3,4,6,8,10]],
"vytvořili": [[4,9]],
"dádávaná": [11],
"specifické": [11,6,10],
"přeložen": [[5,11]],
"políčka": [11,8,4],
"přeložena": [6],
"hledáním": [11],
"políčku": [11],
"segmentů": [11,9,[6,8],10],
"políčko": [11,4,[5,8]],
"ibm": [5],
"zda": [[4,5,6],[0,11]],
"používat": [11,5,[1,6],8],
"zde": [11,[5,6],[1,4,7,9]],
"spustitelné": [5],
"poznámkami": [8,[3,9]],
"beze": [11],
"zobrazuj": [9,11,5],
"chování": [5,11],
"nápisu": [11],
"dobrým": [11],
"přeložený": [[6,9],[8,11],[3,10]],
"směru": [6],
"upravovat": [11,5,6],
"drobnou": [10],
"přeložené": [8,11,6,3,[9,10]],
"přeložení": [11],
"založit": [11],
"zobrazeném": [11],
"zeleně": [9],
"vám": [11,[5,8],[1,6]],
"jakýchkoliv": [[5,6,11]],
"vás": [[5,11]],
"změněna": [11],
"uživ": [[5,8,9]],
"výjimek": [11],
"děláte": [11],
"věci": [1],
"změněno": [1],
"následován": [3],
"zeptá": [5],
"tabulátor": [[1,11]],
"ignoruj": [8],
"idx": [0],
"ruštini": [5],
"technický": [11],
"obsahovat": [11,5,[6,10],[3,4,9]],
"používání": [6,[1,5,7]],
"qui": [1],
"změňte": [11,4],
"jazyková": [4],
"začínají": [5],
"klíč": [11,5],
"potřebujet": [6,[2,5]],
"projectaccesscurrentsourcedocumentmenuitem": [3],
"výsledek": [11],
"linux": [5,2],
"roluj": [11],
"synchronizované": [[6,11]],
"synchronizováni": [6],
"jazykový": [4,[6,11]],
"hesla": [11],
"převod": [6],
"přeložit": [11,[5,6]],
"neprioritních": [1],
"heslo": [11,6],
"výchozímu": [11],
"ifo": [0],
"podl": [11,8,[3,5,6,10]],
"zavřen": [[6,9]],
"holandským": [6],
"mezi": [11,6,[1,10],9,[2,5,8]],
"zdá": [1],
"identickým": [10],
"zavřet": [11,6],
"comment": [5],
"takovýmto": [10],
"abecedi": [11],
"mód": [6],
"segmentováni": [11],
"části": [11,9,5,[6,8,10]],
"stech": [1],
"poslední": [8,[3,5,10]],
"xx.docx": [11],
"technicki": [11],
"dokument": [[3,6,8,11],7],
"tečc": [11],
"optionsautocompleteautotextmenuitem": [3],
"segmentovaný": [11],
"bezpečnostních": [[5,11]],
"přesnější": [10],
"nazývá": [5],
"jednojazyčných": [11],
"požadovanou": [0],
"hlavičc": [11],
"určitě": [6],
"využívejt": [6],
"dalších": [10,[0,6]],
"libovolného": [11],
"příslušný": [1,3],
"modrozeleně": [8],
"neomezený": [5],
"příslušné": [11,6],
"nahrejt": [11],
"rozpoznané": [11,1],
"concis": [0],
"customer-id": [5],
"nacházející": [11],
"vydá": [11],
"pomocných": [6],
"písmenem": [[2,11]],
"term.tilde.com": [11],
"budem": [5],
"otevřeném": [[9,11]],
"určitý": [[8,9]],
"potřebují": [2],
"segmentech": [[6,11]],
"rozpoznáni": [8],
"nálezi": [6],
"nich": [5,[0,11]],
"jasné": [10],
"viewmarknotedsegmentscheckboxmenuitem": [3],
"stahují": [11],
"hierarchií": [10],
"zastaví": [11],
"přihlíží": [11],
"konkrétním": [6],
"konci": [[2,3,10,11]],
"gotomatchsourceseg": [3],
"abstract": [7],
"oběma": [1],
"optionssaveoptionsmenuitem": [3],
"excel": [11],
"pohn": [11],
"runn": [11],
"zvaná": [11],
"přibližného": [11,8,[9,10]],
"prohledávají": [11],
"vhodného": [5],
"budet": [11,6,5,4,[8,10]],
"dodatečnému": [11],
"stardict": [0],
"jestliž": [[0,5,11]],
"omegat.l4j.ini": [5],
"pamětem": [11],
"span": [11],
"barvu": [10],
"poli": [5,[1,2]],
"měnit": [11,[5,9,10]],
"krátký": [11],
"pole": [11,9,[1,2,4,5,8]],
"vyvolá": [6],
"negac": [2],
"nahrazování": [11],
"struktur": [11],
"výchozího": [11,6],
"barvi": [11,[3,8]],
"předponu": [11],
"váš": [5,6,11],
"pozvat": [6],
"thunderbird": [[4,11]],
"editselectfuzzy3menuitem": [3],
"termínů": [9],
"fals": [[5,11]],
"project.projectfil": [11],
"jeho": [11,[0,1,8,9],[5,6,10]],
"směrnicemi": [8],
"krocích": [11],
"konec": [[2,3,8,11]],
"spuštěním": [10],
"množném": [1],
"zahrnují": [5],
"dalšího": [11,8],
"celém": [11],
"značkou": [[9,11]],
"zeptejt": [6],
"velikost": [[8,11],2],
"kopírován": [11],
"pt_br.aff": [4],
"tmx2sourc": [6],
"otevřením": [6],
"milionů": [5],
"spolupracuj": [6],
"předpona": [[10,11]],
"ini": [[5,6]],
"třetím": [9],
"přesném": [11],
"vykoná": [5],
"maximalizuj": [9],
"žádaného": [6],
"zabraňt": [11],
"položek": [9,3],
"ověřování": [5,11],
"ukončujících": [2],
"dhttp.proxyport": [5],
"každé": [8,[6,11]],
"ověřujt": [6],
"kanadu": [5],
"krátké": [11],
"udělal": [6],
"slovnících": [8,9],
"musít": [5,11,[6,10]],
"funkcí": [8],
"přibližném": [11],
"takto": [[5,11],8,[0,10]],
"silných": [11],
"popi": [[3,11],6],
"víc": [[2,8,9,11]],
"původního": [6],
"znamená": [2,[5,11],[6,9]],
"její": [5],
"score": [11],
"celkovému": [9],
"udělat": [10],
"výstupních": [11],
"písmem": [[9,11]],
"appendix": [[1,2,4],[0,3],6],
"zapisovatelný": [[1,3,8]],
"raw": [6],
"spodní": [9,11,8],
"fonti": [8],
"překladový": [9],
"vypn": [8],
"zobrazit": [11,3,8,7,[5,10],1],
"diagrami": [11],
"odpovídající": [11,6,[2,5,8,9]],
"překladové": [6,10,11,9,5,8,2],
"aaa": [2],
"instrukcí": [5],
"naprosto": [4],
"indikuj": [6],
"contemporari": [0],
"solari": [5],
"příponou": [11],
"zkopírujt": [6,[5,8]],
"úprav": [6],
"upravt": [[3,4,11]],
"kterým": [11],
"využití": [6,[7,11]],
"textového": [11,6,[1,8]],
"nicméně": [6,[4,5,11],9],
"evropského": [6],
"nezaměňovat": [11],
"otevírat": [6],
"libovolném": [8,11],
"deaktivaci": [11],
"přednosti": [11],
"algoritmu": [[3,8]],
"klíč-hodnota": [11],
"abc": [2],
"dalším": [10,6],
"nulové": [8],
"funkční": [11],
"pomalá": [11],
"stejným": [11,[0,6]],
"vytvořený": [[6,8]],
"abi": [11,[5,6],[3,9],[2,4,8,10]],
"potvrďt": [[5,11]],
"terminál": [5],
"zadán": [5],
"pamětmi": [10],
"postupného": [11],
"zadát": [[5,11]],
"oknem": [11],
"algoritmi": [11],
"použít": [11,6,[3,8],5,1,[4,9],10],
"omegat.ap": [5],
"funkci": [4],
"předvolbi": [8,11,[5,6,7]],
"pravid": [[5,6]],
"zdrojovými": [11],
"iso": [1],
"uveďm": [6],
"segmentování": [11],
"titulek": [11],
"vstup": [6],
"zvýraznit": [9],
"shodovat": [[1,11]],
"položc": [[3,11]],
"prázdných": [11],
"zpracování": [11,6,[3,8]],
"následujících": [[2,3,5,6,11]],
"částmi": [9],
"pokoušít": [5],
"glossary.txt": [[1,6]],
"stejného": [6,[2,11]],
"vybraným": [[6,8,11]],
"externího": [8],
"věti": [[2,3,8,11]],
"nimi": [[6,8,9,11]],
"tolik": [10,2],
"ověřilo": [11],
"asociovat": [8],
"zlom": [11],
"implicitně": [11],
"věta": [6],
"potom": [[9,11]],
"interní": [[8,9]],
"můžou": [11,9],
"zobrazen": [11,8,9],
"překladů": [11,9,8,[3,6]],
"optionsautocompleteshowautomaticallyitem": [3],
"šipkami": [9],
"půlce": [11],
"zadávání": [6],
"larouss": [9],
"končící": [[5,8]],
"korejské": [11],
"instrukci": [11],
"upraví": [10],
"untar": [0],
"kolečkem": [[1,9]],
"alternativním": [6,[9,11]],
"referenční": [6],
"následováno": [11],
"prevenc": [6,7],
"pouz": [11,6,5,1],
"následované": [2],
"opravdu": [4],
"jazyku": [4],
"filters.conf": [5],
"následovaná": [2],
"projít": [10],
"písmen": [11,[2,5,8]],
"přidává": [1],
"souborového": [4],
"situaci": [9],
"jazyki": [11,6,[1,4,7]],
"možno": [11,9,5,[1,4,6]],
"nástrojů": [6,[2,8,9]],
"ujednání": [5],
"neřek": [1],
"metodám": [11],
"přeskočena": [11],
"jest": [[5,11],6,[8,10]],
"vykonávání": [8],
"kláves": [3,11,9],
"předvoleb": [8],
"neměl": [11],
"targetlanguag": [11],
"jazyka": [11,4,5,[1,6]],
"obsaženými": [1],
"zpřístupnít": [11],
"použitelnosti": [11],
"zpracováni": [5],
"hladové": [2,7],
"definován": [[4,8,9]],
"filtru": [11,6],
"properti": [[5,11]],
"filtri": [11,8,[3,6,10]],
"znehodnocení": [6],
"objektů": [11],
"editselectfuzzyprevmenuitem": [3],
"vytvořeni": [10],
"podruhé": [11],
"následujícím": [5],
"potřebovat": [5,11,4],
"vytvořeno": [11],
"uživatelem": [11],
"vyhnout": [[1,6,10,11]],
"procento": [11,9],
"počáteční": [5],
"script": [11],
"nevykonává": [11],
"kompatibilní": [5],
"system": [[6,11]],
"spellcheck": [4],
"rozeznatelnou": [9],
"týkající": [11],
"používané": [11,[2,5]],
"počítač": [11,5],
"other": [5],
"cokoliv": [3],
"odkazi": [[0,2,11]],
"používáni": [4],
"obnoveno": [10],
"editaci": [11],
"segmentu": [11,9,8,1,[3,10]],
"local": [6,[5,11]],
"překladatelské": [11,9],
"dřívější": [11],
"zdrojových": [11,6,[5,8]],
"locat": [5],
"ztráti": [6,7],
"zrušením": [11],
"kódovací": [11],
"zastupuj": [11],
"rozdíl": [11],
"serverech": [[6,11]],
"editorem": [11],
"specifikaci": [11],
"spárovat": [[8,11]],
"celkové": [8],
"negativně": [10],
"duplic": [11],
"možné": [11,[5,6],2,[1,3,4,9,10]],
"využívat": [5],
"přidáni": [[1,5,8]],
"trvat": [4],
"vybrán": [8,5],
"překladatelský": [5],
"celkový": [9,11],
"přijmout": [11],
"nezapisujt": [11],
"zobrazov": [11],
"akc": [8],
"segmenti": [11,8,3,9,10,5,6],
"přidána": [[1,10]],
"rlt": [6],
"zobrazují": [1,[8,9,11]],
"přeložených": [11,[6,9],5],
"nerozhodn": [11],
"převezm": [8],
"předchozím": [8],
"es_mx.aff": [4],
"filtr": [11,6],
"vytvořená": [6],
"mode": [5],
"vytvořené": [6],
"označením": [11],
"vytvoření": [6,11,[1,5]],
"obnovít": [[9,11]],
"ale": [6,11,[5,10],1,4,[2,9],8],
"toolsshowstatisticsstandardmenuitem": [3],
"fázi": [10],
"určít": [5],
"napsaných": [11],
"alt": [[3,5,11]],
"překladu": [11,8,9,6,10,4,1],
"překladi": [11,6,8,[9,10],7,1],
"pojmenované": [4],
"pojmenováni": [4],
"zabránili": [6],
"obsažených": [11],
"seznam": [11,4,[2,5,6,8]],
"nelíbí": [11],
"collect": [1],
"napojené": [10],
"drobné": [11],
"externích": [11,6],
"velkou": [4],
"instruovat": [4],
"cílové": [11,[6,8],[3,5,9]],
"předdefinovaných": [5],
"rok": [6],
"podrobností": [9],
"modifikátor": [3],
"jakmil": [[5,8],[6,11],[3,9]],
"and": [5,[4,11]],
"uživatelův": [5],
"používají": [11,3],
"cílový": [6,11,8,1,4,3],
"nástroji": [[6,11]],
"ano": [5],
"minuti": [6,[8,11]],
"uložením": [6],
"dostupných": [11,[2,4,5,9]],
"ant": [[6,11]],
"konfiguracnimu-souboru": [5],
"potvrdít": [8],
"jazyků": [11,8],
"přesněji": [11],
"pomalu": [5],
"proveďt": [11],
"přidání": [[5,6],11],
"čtení": [[1,6]],
"sezení": [[10,11]],
"helplastchangesmenuitem": [3],
"nástroje": [11,[3,7],[8,10],2,5],
"omegat.ex": [5],
"ukládáni": [10],
"filtrů": [11],
"rovnítka": [1],
"jakýmkoliv": [[10,11]],
"sourcetext": [11],
"linuxem": [5],
"ním": [11],
"vyhledávací": [11],
"stejném": [11,5],
"stylové": [11],
"jak": [6,11,[1,5,8,10],[0,7],[2,4]],
"english": [0],
"jar": [5,6],
"api": [5,11],
"najd": [[1,2,11]],
"editselectfuzzy2menuitem": [3],
"přístroji": [5],
"narozdíl": [5],
"zkratkou": [11],
"zobrazujícího": [4],
"zkust": [[6,11]],
"vyprázdněn": [11],
"navrženým": [9],
"společenství": [6],
"znakové": [11,8],
"někdi": [11,6,10],
"znački": [11,9],
"zcela": [[1,11]],
"pomocní": [6],
"odděleno": [11,1],
"značka": [[1,9]],
"odděleni": [1],
"budoucí": [6],
"operátor": [11],
"samém": [11],
"stáhli": [5],
"rsc": [6],
"najít": [[2,5,8,11],3],
"javou": [5,11],
"editselectfuzzynextmenuitem": [3],
"některého": [11],
"hromadném": [11],
"postup": [6,8,[3,5,11]],
"vedoucí": [6],
"záleží": [8],
"zvýšit": [11],
"nominativu": [1],
"upravovaný": [8],
"kresbi": [[6,11]],
"zahrnut": [11],
"blíže": [5],
"vyprázdnět": [9],
"podporuj": [[2,6,11]],
"zahrnul": [9],
"jdoucích": [11],
"daném": [11,9],
"nepárových": [9],
"zrušít": [11],
"dvouznakový": [5],
"jistotu": [6],
"jednoduš": [11,[1,4,5,9]],
"vložt": [11,8,[5,10],4],
"zahrnuj": [[2,5,11]],
"napišt": [5],
"změnit": [11,3,6,5,8],
"ucelené": [[6,11]],
"art": [4],
"místo": [8,[6,11],[1,3,5]],
"ostatní": [[5,6],[7,8,9,11]],
"rozdělený": [9],
"místi": [11],
"předcházet": [10],
"využívající": [7],
"nezobrazuj": [[8,10]],
"jde": [9],
"rtl": [6],
"zahrnout": [[6,9]],
"nabízet": [1],
"místa": [9],
"jdk": [5],
"nejdůležitější": [[5,9]],
"ohledu": [2],
"číselný": [9],
"asi": [[5,11]],
"šíři": [11],
"počet": [11,9,8],
"toolsshowstatisticsmatchesperfilemenuitem": [3],
"ukládání": [[4,6]],
"zápisu": [6,8],
"bližší": [[2,6]],
"zdrojové": [6,[8,11],3,9],
"exportovat": [11,[3,8,10]],
"run": [11,5],
"zatržena": [[8,11]],
"jej": [5,11,6,8,9,10,[0,1,3,4]],
"atd": [11,[5,6,9],[0,1,2,10]],
"jen": [11,8,6,[2,5],[1,4],[0,9]],
"nazvaném": [10],
"titlecasemenuitem": [3],
"atributů": [11],
"přesunuli": [11],
"editcreateglossaryentrymenuitem": [3],
"šlo": [5],
"překladatelských": [6],
"automatický": [11,3],
"jakékoli": [10],
"modř": [9],
"zdrojový": [11,[1,6,9],8,[3,10]],
"kontextu": [11,[3,8,9]],
"nežádoucí": [8],
"statistik": [[6,11]],
"projevili": [11],
"nespecifikují": [11],
"tokenizeri": [11],
"ikon": [5],
"znakovou": [1],
"přichází": [11],
"bodů": [6],
"automatické": [11,[3,8],6],
"instalování": [5],
"name": [5],
"říci": [9],
"automatická": [11],
"instalovanou": [5],
"příručku": [[7,8]],
"zdrpjů": [6],
"konkrétního": [11,[4,6]],
"vidí": [6],
"zprostředkované": [9],
"takovéto": [6,10],
"obnovení": [6],
"jazyk-země": [11],
"fontů": [8],
"vychází": [11,6],
"pomlčki": [5],
"nejpravděpodobnější": [[6,9]],
"nejlép": [[6,11]],
"podokně": [8,[1,11],9,[6,10]],
"místě": [[1,5,8]],
"ukažm": [6],
"nepotřebují": [10],
"kocovku": [1],
"provozovat": [5],
"shod": [6],
"případné": [6],
"kombinovat": [[5,11]],
"příručka": [7,[3,5,8]],
"target": [11,[8,10],7],
"paramteri": [10],
"config-dir": [5],
"nástrojích": [6],
"jakéhokoliv": [[4,6,9]],
"ignorovaný": [11],
"jim": [8],
"potaz": [11],
"ignorováno": [3],
"zapsáni": [[6,11]],
"malou": [8],
"načtením": [11],
"uloženi": [11,[5,8]],
"ignorováni": [11,5],
"orientační": [11],
"označit": [3,8,11,[5,6]],
"zkontrolovat": [8,11,[6,9,10]],
"záhlaví": [11],
"uložena": [11,[5,6],10],
"paměť": [6,[10,11],[5,8]],
"globálně": [11],
"zkonvertované": [11],
"cesti": [5],
"probíhat": [11],
"stisknout": [6],
"zamčen": [9],
"trunk": [5],
"targettext": [11],
"výpočtu": [11],
"instalováno": [5],
"doplňovánícki": [[8,11]],
"automaticki": [11,8,5,[1,6],[3,4],9],
"opravit": [[6,11]],
"zatímco": [9],
"vaše": [5,[3,11],0],
"nebyli": [11],
"pluginů": [11],
"vybert": [11,[5,8],[4,9]],
"instalováni": [8],
"skrptovací": [11],
"změnou": [5],
"styli": [11],
"identifikační": [[6,11]],
"nejen": [6],
"instalována": [5],
"stylu": [6],
"aaabbb": [2],
"příkladů": [[5,11]],
"šedým": [8],
"definujet": [11],
"přístup": [5,11,[6,8,9]],
"edittagpaintermenuitem": [3],
"optionscolorsselectionmenuitem": [3],
"zaregistrovali": [5],
"startování": [5],
"ukončuj": [2],
"unicod": [[1,2,7]],
"viewmarknbspcheckboxmenuitem": [3],
"navštivt": [2],
"koncovkami": [0],
"učiněn": [8],
"holandštině": [6],
"zprostředkovává": [8],
"lokativu": [1],
"sdílení": [6],
"vyskytnou": [6],
"minimalizovaného": [9],
"situac": [11],
"počti": [[8,11]],
"pokud": [11,8,5,6,10,9,4,1,3,2,0],
"podokni": [9],
"ukládají": [[6,10]],
"podokna": [9,11,7,[1,10]],
"závislosti": [[5,9,11],8],
"odděleně": [[3,6,11]],
"počtu": [[9,11]],
"výhodná": [11],
"zpracovává": [11],
"vzor": [11],
"validátoru": [11],
"uložení": [11,[3,6,8]],
"adresářich": [5],
"atributi": [11],
"samostatné": [11,[5,9]],
"hledanému": [11],
"uloženým": [11],
"msgstr": [11],
"žlutým": [8],
"samostatná": [2],
"ignorování": [8],
"začínajících": [2],
"žádaném": [6],
"vaší": [5,3],
"uložený": [10],
"alternativní": [8,11,9,3],
"používanému": [8],
"odlišnými": [10],
"označni": [11],
"několik": [11,[5,6,8]],
"přerušit": [[4,5]],
"hlavní": [11,9,[3,6],[5,7]],
"rozbalt": [[0,5]],
"omegat.project": [[5,10],[6,7]],
"zapomněli": [0],
"targetcountrycod": [11],
"načtět": [5],
"poslat": [6,11],
"podokno": [9],
"oddělené": [1],
"grafické": [5],
"nevýhodné": [5],
"požadovaného": [5],
"přejmenovat": [[4,6],11],
"účinek": [11],
"čísl.a": [11],
"užití": [11],
"překladová": [6,[10,11]],
"webstart": [5],
"primární": [5],
"umístěn": [[1,6]],
"umístět": [6],
"zdroj-překlad": [0],
"zaškrtnutí": [11],
"totiž": [11,5],
"počtem": [11,9],
"systému": [5,11,4,[6,8]],
"vloží": [8,11,10],
"osiřelé": [9],
"nahraj": [8],
"cesta": [5],
"instalují": [11],
"ukáží": [6],
"měl": [6],
"amount": [5],
"výjimkou": [2],
"přesunuté": [9],
"kterého": [[6,11]],
"hlavního": [9,11],
"systémi": [5,[7,11]],
"systémech": [[6,10]],
"differ": [9],
"např": [[6,11],5,[1,9,10],[2,4],[0,3,8]],
"rozděleno": [11],
"odstraňovat": [[4,11]],
"softwarové": [5],
"yandex": [5],
"rozděleni": [11],
"neměli": [11],
"přístupni": [8],
"a123456789b123456789c123456789d12345678": [5],
"viewmarkwhitespacecheckboxmenuitem": [3],
"prostředím": [5],
"závorkách": [11],
"souhláski": [2],
"přiřadí": [5],
"kapitola": [2],
"vpřed": [8,3],
"zobrazený": [[9,11]],
"bak": [10],
"nástroj": [[2,7,11],5],
"reakc": [3],
"bat": [5],
"složku": [5],
"poskytovatelem": [11],
"jazykem": [6,11],
"personalizovat": [11],
"zobrazené": [11,1],
"jre": [5],
"zobrazení": [11,6,[5,8,9]],
"pracují": [11],
"již": [11,6,5,9,8,[1,3,4,10]],
"rozeznání": [6],
"vzorů": [11],
"optionsfontselectionmenuitem": [3],
"zobrazená": [[9,11]],
"tabulku": [11],
"povinné": [11],
"kopií": [10],
"štěstí": [11],
"poloviční": [11],
"podobě": [[5,11]],
"jazykovém": [[4,6]],
"tabulka": [[3,11]],
"provádí": [8],
"dvojklik": [11],
"zlomů": [11],
"verzí": [6,5],
"zjevně": [9],
"závisí": [[6,8]],
"tabulki": [11,[6,8]],
"deaktivovat": [8],
"překladového": [[5,10,11]],
"příkladu": [2],
"validaci": [11],
"jednoduché": [[1,6,11]],
"vyžadují": [11],
"freebsd": [2],
"jedním": [[6,11]],
"použijí": [11],
"icon": [5],
"delet": [11],
"nižší": [1,[6,11]],
"jednoduchá": [2,[1,11]],
"příkladi": [2,11,7,[5,6]],
"přístupný": [[1,6,11]],
"jednotlivé": [[1,5]],
"projectaccessglossarymenuitem": [3],
"proved": [[5,11]],
"zpět": [8,3,[6,9,11]],
"nahrávají": [8],
"postupovat": [[4,9]],
"neshodují": [11],
"sem": [9,11,[5,10]],
"protokolem": [8],
"samostatně": [11],
"čárku": [1],
"developerwork": [5],
"uvést": [6],
"obecným": [11],
"čárka": [2],
"řádcích": [[10,11]],
"nejedinečných": [11,9],
"záložki": [11,9],
"akceptovat": [10],
"správnou": [4,1],
"záložku": [8],
"programem": [11,[5,6]],
"optionsrestoreguimenuitem": [3],
"opět": [[5,9,11]],
"proceduri": [6],
"určitých": [6,11],
"správných": [[6,10]],
"generován": [10],
"rovnat": [5],
"obsažena": [11],
"shodou": [[3,8]],
"přístupné": [11],
"rovnal": [9],
"zápisem": [6,7],
"samostatný": [2],
"nazvaný": [5],
"obsaženo": [11],
"terminolog": [1],
"offic": [11],
"umístěné": [8,11],
"začít": [11],
"obsaženi": [6,[5,7]],
"umístění": [5,11,1,6,8],
"požadované": [[5,8]],
"podoken": [9,7],
"shodný": [9],
"skriptovací": [11],
"kdybi": [11],
"aktivujt": [11],
"bez": [11,5,6,[0,1,2,8,10]],
"provést": [[6,11],4],
"projectsavemenuitem": [3],
"hlášení": [5,11],
"xmx6g": [5],
"později": [[5,11],[9,10]],
"podobně": [11,[5,6,9]],
"umístěný": [5,11],
"microsoftu": [5],
"seřadit": [11],
"tisknutím": [8],
"požadovaný": [8],
"takovýchto": [[6,11]],
"zapsat": [8,6,3],
"hraně": [9],
"standardním": [8],
"extistuj": [10],
"sloučeni": [11],
"internetu": [4],
"ukázat": [3,[5,8]],
"klonovat": [6],
"nazývanými": [11],
"umístěná": [5],
"vlastní": [11,9,[2,6]],
"verzi": [5,6,4],
"vybraného": [[6,8]],
"cursor": [9],
"nevím": [5],
"oddíl": [5],
"příklad": [[6,11],5,[0,1,2,4,7,8,9]],
"slovem": [[1,2]],
"podobné": [11,9],
"terminologická": [9],
"aktivováním": [11],
"interaktivních": [2],
"počitadla": [9,7],
"podobná": [9],
"shoduj": [11,1],
"lomítkem": [5],
"jmenovaných": [[6,10]],
"názvů": [11,10],
"systémů": [5],
"historii": [8,3],
"přidávají": [5],
"mezinárodní": [[1,6]],
"zadávát": [6],
"omezít": [11],
"ostatních": [11],
"osvědčil": [1],
"ponechána": [11],
"pokročilí": [5],
"přístroj": [8],
"důraz": [10],
"vyjmout": [9],
"určí": [[5,11]],
"pokročilé": [[2,11]],
"použita": [6,[5,11]],
"vést": [11],
"kapitoli": [1],
"číslice": [2],
"použiti": [11],
"číslici": [6],
"vyžadovala": [11],
"snazší": [11],
"nevyžadují": [9],
"může": [11,5,6,9,[1,3,10],8,4],
"příkladě": [9,[1,4,6,11]],
"podobný": [[2,6,10,11]],
"použito": [5],
"důvodů": [11],
"select": [9],
"uchováni": [10],
"bodem": [11],
"vzdálené": [[6,10]],
"dostupném": [5],
"nezměnít": [11],
"sice": [1],
"započaté": [2],
"bis": [2],
"hodnota": [11],
"platformách": [5],
"projectopenmenuitem": [3],
"autom": [5],
"hodnoti": [11,8],
"vyloučení": [[6,11]],
"informuj": [9],
"anglického": [2],
"zadávanou": [5],
"hodnotu": [[5,11]],
"vizt": [[2,5,11]],
"porovnání": [11],
"požádáni": [8],
"kvalita": [6],
"toolsvalidatetagsmenuitem": [3],
"začnět": [[1,6]],
"pravděpodobně": [[1,2,5,11]],
"archivujt": [6],
"kvaliti": [8],
"autor": [[8,9,11]],
"komunikuj": [6],
"vyžadováno": [2],
"kvalitu": [10],
"varování": [[9,11]],
"nejprv": [11,[4,6,9,10]],
"maskovat": [5],
"potvrzení": [11],
"správnými": [0],
"termín": [1,8],
"přejdět": [5,[4,11]],
"neoznačeno": [11],
"viewmarktranslatedsegmentscheckboxmenuitem": [3],
"váhu": [10],
"valu": [11],
"běžným": [5,8],
"nazevsouboruprovystup": [5],
"nahrávání": [11],
"dotýká": [8],
"neznamená": [2],
"časových": [6],
"vypnout": [11],
"pojavni": [1],
"optic": [6],
"nakonec": [5,11],
"sekundách": [11],
"editselectfuzzy1menuitem": [3],
"pevný": [[5,8]],
"budoucích": [6],
"hide": [11],
"chováním": [8],
"tabulátoru": [[1,2]],
"vzdálený": [6,10],
"příkazovém": [5,6],
"posunout": [11],
"segmentačních": [[2,11]],
"návratu": [2],
"auto": [10,6,8,11],
"souvislejší": [11],
"un-com": [5],
"notepad": [1],
"průvodc": [5],
"importujet": [8],
"zobrazeno": [8],
"oracl": [5,3,11],
"zobrazeni": [11,8,[6,10],[1,9]],
"jednoduchou": [6],
"vysvětlen": [6],
"kvalitě": [6],
"účinný": [11],
"věc": [10],
"zobrazena": [[1,5]],
"gradlew": [5],
"nesegmentovaných": [11],
"tři": [6,[0,10],1],
"level": [6],
"vět": [11],
"upravením": [5],
"krok": [6,11],
"oddělovač": [8,9],
"konfiguraci": [11],
"fialovým": [8],
"uvádí": [11],
"klávesa": [3],
"platí": [8,11,5],
"zobrazován": [6],
"nástrojem": [0],
"použijt": [6,5,11,4],
"tento": [5,8,11,[6,10],9,[0,1,3,7]],
"značek": [11],
"nakopírovat": [5],
"switch": [11],
"doplňování": [11,[3,8]],
"kam": [[9,11]],
"dvou": [11,6],
"kořenem": [11],
"zapisovatelného": [1],
"bundl": [5],
"cestou": [5],
"interaguj": [11],
"src": [6],
"tabulek": [3],
"přiřadit": [11],
"myši": [11,9,[1,5],[4,8]],
"oblast": [[8,11]],
"rozšíření": [[8,11]],
"control": [[3,6]],
"kdykoliv": [[6,9]],
"znakem": [[1,5]],
"no-team": [[5,6]],
"klávesu": [11],
"nimiž": [2],
"zatržení": [11],
"klávesi": [3,11],
"čas": [[8,11]],
"použili": [6],
"objekti": [6],
"podporujících": [6],
"obnoví": [8,9],
"rozdělí": [11],
"neklad": [10],
"environ": [5],
"informaci": [6],
"předchozí": [8,3,[9,11]],
"optionsautocompleteglossarymenuitem": [3],
"jakékoliv": [1,[2,5,11]],
"vlastností": [11],
"spouštěč": [5],
"přístu": [8],
"důvodu": [[4,5,8]],
"skloňované": [1],
"existovat": [1],
"obrazovka": [5],
"přítomni": [11],
"manažeru": [4],
"horní": [[9,11]],
"nesouhlasí": [4],
"tří": [6],
"elektronický": [9],
"kde": [5,[6,11],8,[3,4,9,10],1],
"perfektně": [6],
"sto": [6],
"aktivovat": [8],
"nucené": [[8,10]],
"kdi": [[6,11],[1,5]],
"řídit": [11],
"vytvářet": [11],
"filtrování": [11],
"navigaci": [11],
"zobrazující": [11],
"specifických": [[9,11]],
"zemi": [5],
"vámi": [6],
"následujícími": [11],
"sub": [1],
"languag": [5],
"týmu": [6],
"portu": [5],
"oranžově": [8],
"záznam": [8,[1,11],3,[2,9]],
"sloupc": [11,[1,8]],
"vypíš": [5],
"stažen": [5],
"spoluprác": [6,5],
"souborech": [11,6,[1,5,8,9]],
"pravidlech": [2],
"délku": [9],
"key": [5,11],
"skládá": [11],
"zažádat": [5],
"přiřazovat": [8],
"přibližným": [11,8],
"svg": [5],
"opustít": [10],
"opětovným": [11],
"umístít": [1],
"znak": [2,11,[3,5,7,8]],
"zacházet": [[9,11]],
"launch": [5],
"svn": [6,5,10],
"myší": [[8,9]],
"zachová": [10],
"pojem": [2,1,8],
"tradiční": [5],
"komentář": [11,1,9,[7,8]],
"dialogového": [11,8],
"svém": [5,11,[4,6,9]],
"parametrech": [5],
"lokálně": [6],
"nechejt": [10],
"editreplaceinprojectmenuitem": [3],
"symbol": [2],
"země": [11,5],
"ukládat": [[4,6]],
"detekovat": [8],
"vytvořeného": [9],
"zdrojů": [11,6],
"express": [2,11],
"uskutečňuj": [6],
"stránce": [[0,5],[8,11]],
"provoz": [[5,7],8],
"německé": [11],
"vlastnosti": [11,6,8,[1,4],[0,3,5,7,10]],
"dostupná": [[3,5,6,11]],
"větší": [10],
"gotoprevioussegmentmenuitem": [3],
"lokální": [6],
"týká": [[5,9,11]],
"tečkou": [[2,8,11]],
"regulární": [2,7,11,5,[3,4]],
"gotopreviousnotemenuitem": [3],
"editredomenuitem": [3],
"stole": [9],
"uilayout.xml": [10],
"poskytnuté": [11],
"ekvival": [[8,9]],
"kryje": [11],
"tištěných": [9],
"názor": [9],
"případě": [11,6,5,9,1,[8,10]],
"přísné": [6],
"spárovaných": [9],
"označený": [9],
"segmentován": [11],
"příkaz": [5,[8,11]],
"stránek": [[6,11]],
"zbytečným": [6],
"základě": [11],
"případů": [6,[10,11]],
"chcete-li": [0],
"hostitelského": [5,11],
"uvnitř": [[1,8]],
"angličtina": [6],
"projektech": [11],
"zvláštní": [6,[9,11]],
"dostupné": [11,[3,5],[6,10]],
"skriptování": [11,8,7],
"jakémkoliv": [11,5],
"recognit": [6],
"své": [5,11,6],
"oddělenými": [1],
"proveditelné": [11],
"omezení": [11],
"stistku": [11],
"učiňt": [10],
"omezené": [11],
"označené": [11,4,[8,10]],
"byl": [[6,11],[1,5,8,9,10]],
"dostupný": [[1,9]],
"označení": [11,[2,7],[5,8]],
"stojí": [11],
"jakoukoliv": [[1,6]],
"opustili": [8],
"runtim": [5],
"čili": [10,6],
"rovnítkem": [1],
"tester": [2,7],
"použitý": [11],
"podadresář": [10,11,6,4],
"příkazovým": [5],
"obsažené": [[5,10]],
"slouží": [[1,11],[6,10]],
"upraveni": [8],
"zeptat": [[6,9]],
"vzori": [11,6],
"problém": [1,[8,11]],
"přeložitelných": [11],
"filenam": [11],
"vzoru": [11,2],
"technologi": [[5,7]],
"zajištěn": [5],
"buď": [11,[2,4,6]],
"přeložt": [6],
"nbsp": [11],
"nepraktický": [5],
"odstraněna": [10],
"gotosegmentmenuitem": [3],
"kryjí": [1],
"balíku": [5],
"veškerý": [[6,10,11]],
"odstraněni": [11,4],
"nevhodných": [11],
"editačního": [9],
"umístěna": [[5,10]],
"praktického": [9],
"angličtinu": [2,5],
"umožnit": [11],
"veškeré": [6],
"xx_yy.tmx": [6],
"kódování": [11,1],
"key-valu": [11],
"internetových": [11],
"prvním": [11,[1,2,5,8]],
"jakožto": [[1,11]],
"helpaboutmenuitem": [3],
"trenaž": [2],
"zachovat": [11,5],
"okamžiku": [11],
"výstrah": [5],
"nesegmentovat": [11],
"nezměněni": [11],
"překladatelů": [6],
"regular": [2],
"stránkám": [6],
"c\'est": [1],
"sadi": [11,[2,8]],
"sadu": [11,1],
"nainstaluj": [5],
"označena": [11],
"zkoumá": [2],
"obsah": [11,3,[6,10],[1,5,8],[0,9]],
"elementi": [11],
"použitá": [11],
"omezeno": [[1,11]],
"x_linux.tar.bz2": [5],
"stránki": [8,3],
"znázorňuj": [6],
"informací": [[5,11],6,10,9],
"označeni": [8],
"označeno": [[4,8,11]],
"použité": [11],
"vytvořt": [6,4],
"formátů": [11],
"použití": [11,6,5,4,7,[0,2],[1,9]],
"pojmi": [[1,3,11]],
"stránka": [11,10],
"záznamem": [[1,11]],
"sada": [11,2],
"slovech": [[1,7]],
"zdroji": [11,9,[5,8]],
"stránku": [2,[3,8,11]],
"zdroje": [8,11,6,[3,5,9]],
"vložít": [[9,11],1],
"vytvoří": [8,[5,11],10],
"předcházející": [6],
"tab": [[1,3],[8,11],9],
"taa": [11,8],
"webu": [6],
"zarovnání": [6],
"ukázka": [11],
"rozeznáni": [11],
"tag": [11,8,3],
"něj": [5],
"nalezn": [[1,5,11]],
"tak": [11,[5,6],9,10,8,[1,3],[0,2,4]],
"lokalizačních": [6],
"tam": [[0,11]],
"vybranému": [8],
"tar": [5],
"nainstalováni": [5,4],
"kanadskou": [11],
"projectreloadmenuitem": [3],
"příkazem": [8],
"nainstalována": [5],
"odstranění": [[6,11]],
"navrhovaným": [9],
"dostupným": [4],
"odstavcích": [11],
"safe": [11],
"přeloží": [5],
"kopii": [6,8],
"všechni": [11,6,5,8,[2,3,10],[4,9]],
"překladatelé": [6],
"líné": [2,7],
"všechna": [11,9],
"winrar": [0],
"tbx": [1,11,3],
"získat": [9,[2,5,11]],
"informování": [11],
"názvu": [11,10],
"kterém": [5,11,[4,8]],
"agenturi": [9],
"cat": [10],
"duser.countri": [5],
"tcl": [11],
"tck": [11],
"readm": [5],
"informac": [6,11,[5,8],3,[1,2]],
"zvolit": [6],
"zarovnat": [8],
"match": [9],
"kliknutím": [11,5,[1,4,8]],
"makra": [11],
"názvi": [11,[4,6,9]],
"lomítko": [2],
"následujícího": [5],
"byť": [1],
"jediný": [11],
"spouštět": [5,[8,11]],
"mezeri": [11,8,[2,3]],
"přiloženou": [8],
"align.tmx": [5],
"běhu": [9],
"lomítki": [3],
"mezera": [[2,11]],
"šedě": [8],
"chybně": [[8,11]],
"poklikem": [5],
"přednost": [8],
"výsledkem": [11],
"lomítka": [[2,5]]
};
