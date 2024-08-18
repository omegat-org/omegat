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
 "Anhänge",
 "Einstellungen",
 "Anleitungen",
 "Einführung in OmegaT",
 "Menüs",
 "Fensterbereiche",
 "Projektordner",
 "Fenster und Dialogfenster",
 "OmegaT 6.0.0 - Bedienungsanleitung"
];
wh.search_wordMap= {
"tmx-datei": [2,6,7],
"konvertiert": [2,[4,7]],
"administratoren": [2],
"trennen": [0,2,[1,7]],
"teamprojekt-repositori": [0],
"selben": [[1,3,7]],
"xliff-zielstatus": [0],
"wörterbuchapplik": [1],
"zwar": [7,0],
"zudem": [3],
"ten": [4],
"automatisch": [6,7,[1,2],0,4,3,5],
"halb": [3],
"erfolgen": [0],
"info.plist": [2],
"trotzdem": [0],
"projekt-url": [2],
"inhaltsverzeichnis": [8],
"formatierungen": [0],
"termbase-exchange-format": [0],
"direktionalität": [0],
"brückensprachpaaren": [2],
"einige": [8],
"dialogfenst": [[1,7],3,4,0,2,6],
"fuzzi": [1,4,5,7,[2,3],6],
"irgendwo": [6,5],
"size": [2],
"left": [0],
"einfügen": [0,4,[1,5],[3,7],2,6],
"einfärbt": [0],
"rechner": [1],
"menüpunkt": [4,0,7,3,1,6,2],
"weil": [0],
"verlaufsbasiert": [1],
"gearbeitet": [3],
"speziell": [0],
"weit": [[2,3]],
"weis": [0,[1,2,3,5,6,7]],
"violett": [4],
"edittagnextmissedmenuitem": [0],
"same": [7],
"vermeiden": [2,[0,3]],
"quiet": [2],
"mindestinhalt": [6,8],
"bewirken": [6,0],
"formatanforderungen": [2],
"bedienend": [3],
"xhmtl-filter": [0],
"laden": [2,7,0,3,[1,4,6]],
"implementiert": [1],
"beispiele": [8],
"ausführlichsten": [0],
"belässt": [5],
"bezeichnet": [0,2],
"satzübersetzungen": [7],
"the": [0,[2,7],5],
"projectimportmenuitem": [0],
"ausrichtungsmodus": [7],
"bearbeitung": [3],
"imag": [0],
"monolingu": [0],
"linux-distributionen": [2],
"richtigkeit": [[0,5]],
"microsoft-ziel-local": [0],
"quelltexten": [[6,7],0],
"omegat.project.lock": [2],
"prioritätsstufen": [3],
"entwicklungsseit": [2],
"moodlephp": [2],
"zwei": [2,0,7,[3,4],[1,5,6]],
"currsegment.getsrctext": [7],
"dieselben": [[0,2,4]],
"kategorien": [0,8],
"gelernt": [0],
"export": [6,2],
"überflüssig": [[0,7]],
"freuen": [0],
"projektdaten-speicherinterval": [[1,2,4,6]],
"äquival": [0],
"practic": [7],
"ereignissen": [7],
"unabhängig": [7],
"check": [7],
"zieldateien": [0,4,2,7],
"benachrichtigungseinstellungen": [5],
"tm-datei": [1,2],
"minimieren": [5],
"doppelt": [2,[0,7],4],
"konten": [2],
"bidirektional": [4],
"gotonotespanelmenuitem": [0],
"fr-fr": [3,1],
"ordner": [2,7,6,0,4,3,1,5],
"zeichensätz": [0],
"neuladen": [[4,6]],
"ordnen": [0],
"gefüllt": [[4,6],0,2,1],
"zusammenführen": [[1,7]],
"standardwert": [[0,7]],
"muster": [[0,1],7,2],
"verarbeitenden": [2],
"eckig": [0],
"gefundenen": [7,2],
"root": [0],
"zugeordneten": [4],
"hosting-serv": [2],
"einzig": [2],
"bestehend": [2,7],
"scannt": [7],
"ersetzten": [0],
"welt": [2],
"verbleibenden": [7],
"omegat-ordn": [2],
"monospace-schriftart": [1],
"lateinisch": [0],
"konvertieren": [2,3],
"neues": [8],
"geringfügig": [4],
"neuer": [[0,2,5]],
"systemprogramm": [2],
"zeichenkett": [[0,7],1],
"grünem": [5],
"translation": [8],
"syntax": [0,2],
"neuen": [2,7,0,3,4,[1,6]],
"mechanismen": [[0,2]],
"anzeig": [7],
"inline-formatierung": [7],
"konfigurationsordnern": [2],
"po-dateien": [0,2,[1,6]],
"empti": [2],
"sätzen": [0],
"originaltext": [7,2],
"einzubeziehen": [1],
"könnten": [0,6],
"installationsordn": [2],
"block": [0],
"tms": [2,[4,6],[0,3],[1,7,8]],
"suchzeichenkett": [7],
"tmx": [7,[2,3,5]],
"html-ähnlich": [1],
"repo_for_all_omegat_team_project": [2],
"cli": [2],
"application_startup": [7],
"lediglich": [[3,7]],
"festgelegt": [[1,2],[0,5,6,7]],
"eventtyp": [7],
"zerstören": [1],
"url-protokol": [2],
"sanktion": [6,1],
"fr-ca": [1],
"interferiert": [4],
"mainmenushortcuts.properti": [0],
"gelangen": [3],
"teilt": [[1,7]],
"werkzeugen": [2],
"auflistung": [0],
"glossardateien": [0,[5,7],2],
"standardinhalt": [0],
"anmerkung": [2,7,0,4,1,[3,5],6],
"statistiken": [4,[1,2,7],[0,6]],
"wenn": [2,7,0,4,1,5,3,6],
"mehrere": [8],
"negieren": [0],
"segmentmodifikationsinfo": [1,[4,5]],
"subtitl": [2],
"gespeichert": [[0,2],1,4,5,7,3],
"gotohistorybackmenuitem": [0],
"schwellenwert": [1,[2,5]],
"omegat.project-datei": [2],
"save": [7],
"v1.0": [2],
"dateinamen": [7,0,[2,3,6]],
"fensterbereich-widgets": [8],
"tm-suchoptionen": [7],
"teilen": [7,[2,3,6],[0,1,5]],
"kennzeichen": [4],
"top": [5],
"ergebnissen": [0],
"have": [0],
"powerpc": [2],
"kommerziel": [3],
"juristischen": [0],
"satz": [0,7,3,[1,4],2],
"komplex": [0,2],
"exportieren": [0,[1,4]],
"menüs": [8],
"question": [0],
"kontextmenü-symbol": [4],
"dateiendung": [0,2],
"hervorhebung": [7,4],
"agieren": [0],
"editselectsourcemenuitem": [0],
"eine": [8],
"aufgenommen": [2],
"regelsatz": [1],
"absatzmarken": [0],
"com": [0],
"projekt": [2,7,[3,6],4,0,5,1,8],
"instal": [2,[0,1]],
"übernommen": [6,[0,1]],
"wert-text": [2],
"cot": [0],
"standardort": [2],
"remot": [6],
"liegen": [1],
"font-fallback": [[0,4]],
"sollt": [2,7,[0,4]],
"dafür": [0,[2,3]],
"guter": [1],
"dokument.xx.docx": [0],
"alleinstehend": [1],
"birnen": [0],
"zwischenzeitlich": [2],
"jetzt": [2],
"pipe": [0],
"bereit": [2,[0,7],6,3,1,4],
"überschreibt": [[0,5],2],
"großbuchstab": [0],
"wert": [0,1,[2,7]],
"erzwingen": [0],
"dateifiltereinstellungen": [[0,7],4],
"omegat-projektdatei": [5],
"nutzen": [2,0,3,[1,4,5,6,7]],
"changeid": [1],
"translat": [2,3,7,6,[0,5],[1,4]],
"erinnerung": [0],
"html-kommentar": [0],
"université": [1],
"verschoben": [[3,5]],
"einhergehen": [4],
"verbessern": [[0,1,3,5]],
"suchfunkt": [[0,2]],
"schreibweisen": [0],
"umbenennung": [2,3],
"suchbereich": [7],
"cqt": [0],
"pseudoübersetzt": [2],
"verbleibend": [5],
"trefferanzahl": [7],
"respons": [5],
"zuverlässigkeit": [3],
"gemeißelt": [6],
"übersetzungsspeich": [3],
"scripting": [8],
"docs_devel": [2],
"zusammengesetzt": [1],
"zeitspann": [2],
"lck": [5],
"tsv": [0],
"paar": [2,3],
"verwalten": [3,8,[1,2,4]],
"auszuführen": [7,2,3],
"löst": [0],
"gnome": [1],
"vervollständigungen": [1],
"verwaltet": [[1,2]],
"maximal": [0,2],
"kategori": [0],
"mitgezählt": [4],
"anstatt": [[3,7],[2,4]],
"treffern": [3],
"zeilenläng": [0],
"doctor": [0],
"kollegen": [2,5],
"lre-zeichen": [0],
"durchzugehen": [5],
"appdata": [0],
"sichere": [8],
"mächtige": [7],
"struktureinheiten": [0],
"csv": [0,2],
"download-seit": [1],
"tun": [[0,2,3],[1,4]],
"skripteditor": [[0,7]],
"angeklickt": [5],
"umbenennen": [[2,7]],
"fällig": [3],
"unübersetzbar": [0],
"caractèr": [2],
"tm-dateien": [6,7],
"navigationsmöglichkeiten": [3],
"les": [5],
"press": [0],
"dock": [2],
"standardmäßig": [0,1,7,2,4,6,5,3],
"unsichtbar": [0],
"element": [[0,3],7],
"dasselb": [[0,2,3,7]],
"wortwiederholungen": [0],
"suchfenster": [8],
"speicher": [2,0],
"schleif": [7],
"night": [2],
"genutzt": [[2,7],0],
"statt": [0],
"aneinand": [[4,7],3],
"markierten": [[1,4],7,[0,5]],
"kästchen": [[1,7]],
"herangezogen": [4],
"markiertem": [[0,4,5]],
"weiter": [0,7,2,[1,3],[5,6],4,8],
"abschnitt": [2,3,0,7,1,4],
"verweisen": [7,5],
"suchergebni": [7],
"erfüllt": [0],
"tarball-archiv": [6],
"absatzumbruch": [0],
"filenameon": [1,0],
"cut": [0],
"ctrl": [0,4],
"editorinsertlinebreak": [0],
"darstellt": [0],
"jumptoentryineditor": [0],
"document": [0],
"ausgeschlossen": [7],
"einmalig": [7,5,[0,4]],
"neustart": [2],
"unveränderlich": [6],
"page_up": [0],
"glossaryroot": [0],
"derselben": [5,[1,2,7]],
"selbst": [0,2,[3,7],6],
"beendet": [[2,4],[0,1]],
"eingabeaufforderung": [2],
"genau": [0,7,[3,4]],
"vorkommen": [7,2,[0,1]],
"beenden": [4,[0,7],[1,2]],
"resourc": [2],
"geschrieben": [[0,2,4]],
"während": [7,0,[3,5],2,4,[1,6]],
"moodl": [0],
"demselben": [2,3],
"team": [2,1,8],
"xx_yy": [0],
"docx": [[0,2,4]],
"project_stats_match_per_file.txt": [[4,6]],
"txt": [2,0,5],
"legacy-filt": [0],
"akzeptieren": [7],
"projektdatei": [4],
"abzüglich": [7],
"meldet": [2],
"sicherzustellen": [2,[1,3]],
"projektdaten": [[1,2],7],
"benötigen": [2,0,3],
"definit": [0],
"ermittlung": [0],
"projectmedopenmenuitem": [0],
"lib": [0],
"jeweil": [4,1],
"segmentnavig": [4],
"typ": [[5,7]],
"source": [8],
"repository-zuordnung": [[2,7]],
"mechanismus": [4],
"begriffen": [5,3,7],
"zustand": [[2,6],[1,4]],
"viewdisplaymodificationinfoselectedradiobuttonmenuitem": [0],
"index.html": [0,2],
"kapazität": [2],
"anordnen": [1],
"wörter": [6,0,4,[1,5]],
"anwendungsbeispiel": [2],
"suchvorgänge": [8],
"mehrmaligen": [1,7],
"erleichtern": [[3,7],[0,2,4,6]],
"quellseg": [2,[1,4]],
"erleichtert": [[2,7]],
"fällen": [4,[0,2]],
"develop": [2],
"diffrevers": [1],
"verfahren": [0],
"einprägen": [3],
"erzielen": [7],
"diejenig": [7],
"felder": [[0,4],[3,7]],
"regelsatzdaten": [1],
"konfigurationsdateien": [[1,4]],
"sprachregelsätz": [1],
"hinzugefügten": [[0,2]],
"glossaren": [5,[2,3]],
"texteinheiten": [0],
"zeichengruppen": [0],
"konfigurationsparamet": [2],
"zeilenumbruchzeichen": [0],
"e-mail-adressform": [0],
"hauptfensterbereich": [1],
"übrigen": [7],
"rechtfertigen": [7],
"rutsch": [1],
"adoptium-projekt": [2],
"manchmal": [0,2],
"anwendungsmenü": [2],
"darauf": [0,[4,5,7]],
"stammformredukt": [1,5],
"projektressourcen": [7,[3,6]],
"omegat-instanzen": [4],
"wörterbuch-match": [4],
"unbedeutend": [0],
"projektspezifischen": [[6,7],0],
"stark": [1],
"distributionslizenz": [0],
"automatisieren": [[2,3]],
"internetadressen": [5],
"project.gettranslationinfo": [7],
"czt": [0],
"empfehlen": [2],
"doctorat": [1],
"abbildung": [[3,5]],
"konventionen": [3,8],
"start": [2,[0,7],1],
"mymemori": [1],
"angedockt": [[3,5]],
"grundsätzlich": [0,[2,7]],
"merkt": [4,2],
"omegat-wiki": [[2,6]],
"regex101": [0],
"editiert": [7],
"equal": [0,2],
"fällt": [3],
"libreoffice-dateien": [3],
"aussieht": [3],
"konfigurationsordner": [8],
"watson": [1],
"filtermust": [0],
"segmenteigenschaften": [5,[3,8]],
"java-vers": [2],
"zusammenzuführen": [3,7],
"inklus": [[0,7]],
"tastendarstellungen": [0],
"rahmen": [0],
"pdf-zeichen": [0],
"komplett": [[2,5]],
"project_save.tmx.yyyymmddhhmm.bak": [2],
"zweisprachigen": [2,[6,7]],
"viewmarkglossarymatchescheckboxmenuitem": [0],
"steigern": [0],
"verbleiben": [7],
"positioniert": [1],
"einträgt": [4],
"enter": [0,7,4,1],
"programmstart": [2],
"rechtschreibwörterbüchern": [1,3],
"tbx-glossar": [1],
"angrenzen": [5],
"applic": [[0,2,5]],
"projectteamnewmenuitem": [0],
"gotoprevxenforcedmenuitem": [0],
"auflistet": [5],
"auflisten": [0],
"konfigurationseinstellung": [7],
"html-abschnitten": [0],
"applik": [2,[3,4],0,7],
"daten": [2,[1,4],7],
"vervollständigung": [1],
"mich": [5],
"memori": [2,3,[6,7],5,4,[0,1]],
"autocompletertablelast": [0],
"wagenrücklauf": [0],
"no-match": [4],
"importiert": [5],
"markierungen": [0,7],
"tools-menü": [[0,8]],
"daraus": [2,0],
"indefinit": [0],
"anschließend": [2,[0,3]],
"vorbehalten": [2],
"grundregeln": [0],
"schwacher": [0],
"log": [0,4],
"tagfreien": [7],
"korrekt": [[0,2],[1,4],5,[6,7]],
"jedoch": [[0,7],[2,3]],
"entwicklung": [2,0],
"tagfreier": [[3,7]],
"meinung": [5],
"sternchen": [0],
"hosting-dienst": [2],
"openjdk": [1],
"永住権": [[1,7]],
"einzigen": [0,7],
"führenden": [0],
"toolscheckissuesmenuitem": [0],
"genutzten": [2,7,[5,6]],
"direktionale": [8],
"websuch": [7],
"teil": [0,4,7,2,[3,5]],
"initial": [2],
"leerraumzeichen": [0],
"tutori": [0],
"zeichen": [0,7,[4,5],[1,2],8],
"passieren": [[2,3]],
"entgegengesetzten": [7],
"unterschiedlich": [4,[1,7],[0,3,5]],
"aggressiven": [[0,4]],
"autocompletertablepageup": [0],
"wählt": [7,4],
"sprachmust": [1,0],
"www.deepl.com": [1],
"standard-branch": [2],
"schicken": [2],
"solch": [2,1,3,[0,6],7],
"ausgefallen": [0],
"config-fil": [2],
"interessant": [0],
"quick": [0],
"zusammenfassend": [0],
"kleinbuchstaben": [0],
"vordefinierten": [1],
"scripting-engin": [7],
"falschschreibungen": [0],
"protokollauswahl": [2],
"klass": [0],
"eventuel": [0],
"das": [2,0,7,4,5,1,3,6],
"day": [0],
"dateitypen": [2],
"lre": [0,4],
"hellgrau": [4],
"minuten": [2,[1,3,4,6]],
"system-user-nam": [0],
"lrm": [0,4],
"möglicherweis": [[0,7],4],
"format": [2,0,3,7,6,[4,5,8]],
"seltsam": [2],
"bestimmen": [[0,2],1],
"console.println": [7],
"rainbow": [2],
"besseren": [3],
"textfragment": [0],
"herunterzuladen": [2],
"fetter": [[0,5,7]],
"autocompleterlistdown": [0],
"omegat-tastenkürzel": [0,8],
"writer-datei": [0],
"verlangen": [1],
"pars": [5],
"zerlegt": [0],
"scheint": [2],
"part": [7],
"vorangestellt": [0],
"dateifilter": [8],
"suchdialogfenst": [3],
"bild": [0],
"datei": [2,0,7,6,4,3,1,5],
"regex-beispielen": [0],
"zurückkehren": [3],
"activefilenam": [7],
"ausreichend": [2],
"fuzzy": [8],
"erst": [0,[5,7],4,[2,3]],
"segmentierungsparamet": [0],
"project_files_show_on_load": [0],
"heruntergeladenen": [2],
"speicherfunkt": [2],
"benutzt": [3],
"deutet": [5],
"sammlung": [[2,3,6]],
"identifizieren": [[0,1,2]],
"konzentrieren": [3],
"apostroph": [0],
"originaldoku": [0],
"suchen-und-ersetzen-skript": [7],
"build": [2],
"benutzerdefinierten": [0,[2,3]],
"tag-kürzel": [0],
"plugin-installationen": [0],
"notizen": [[3,5],[0,4,7]],
"heißen": [[0,2]],
"nicht-umbruch-markierung": [1],
"ident": [7],
"entries.s": [7],
"vorab": [0],
"den": [7,0,2,4,3,1,5,6,8],
"bevorzugt": [[0,2]],
"dem": [2,0,7,1,5,4,3,6],
"gotonextuntranslatedmenuitem": [0],
"targetlocal": [0],
"der": [0,2,7,5,1,4,3,6,8],
"fehlenden": [[4,5]],
"path": [0],
"des": [0,2,[1,7],5,4,3,6],
"bind": [7],
"entwed": [0,2,3,[1,6],4],
"verlauf": [4,0,[1,2]],
"autovervollständigung": [0,4,[1,3],[5,8]],
"mehreren": [0,5],
"gleichbreiten": [1],
"sondern": [3,[0,7]],
"betritt": [1],
"vergleich": [3],
"verschiedenen": [[3,4],7,[0,1,2],6],
"dort": [7,[0,2,3]],
"match-prozentsätzen": [6],
"zwingen": [[1,7]],
"eignet": [0,2],
"segmentgenerierung": [0],
"unbedingt": [7],
"öffnet": [4,[1,3,5,7],2],
"gesichert": [2],
"öffnen": [[2,7],0,[3,4],[5,6],1],
"helpcontentsmenuitem": [0],
"dateiformaten": [1],
"omegat-org": [2],
"remote-project": [2],
"beschaffen": [0],
"auszuschalten": [7],
"einfache": [8],
"deaktiviert": [[1,4],0,7],
"initialcreationid": [1],
"ignore.txt": [6],
"trennzeichenmust": [0],
"projectaccessdictionarymenuitem": [0],
"meisten": [0,4,1],
"ressourcenordn": [7,3],
"veröffentlichen": [2],
"beschreibung": [[0,7]],
"maximieren": [5],
"punkt": [0,2,1,[4,7]],
"navigationskürzel": [3],
"ermöglicht": [4,[2,3],7,[0,1]],
"ausnahm": [2,0],
"bekommen": [0],
"files_order.txt": [6],
"projectrestartmenuitem": [0],
"tastenkürzelbeschreibungsbeispiel": [4],
"editorskipnexttoken": [0],
"europäischen": [4],
"trans-unit": [0],
"verteilt": [[0,8]],
"right": [0],
"aussehen": [3,8],
"qigong": [0],
"masterpasswort": [1],
"docx-dateien": [[2,7]],
"projekterstellung": [3],
"omegat.app-paket": [2],
"maximum": [0],
"paus": [3],
"selten": [2],
"ländercod": [2],
"erledigt": [3],
"die": [2,0,7,1,4,3,6,5,8],
"imper": [7],
"standardapplik": [4],
"informiert": [1],
"dir": [2],
"down": [0],
"gehostet": [1],
"terminalfenst": [2],
"projekteigenschaft": [0,2,3],
"sonderklassenzeichen": [0],
"viewfilelistmenuitem": [0],
"projektparamet": [6],
"hinzufügt": [2],
"journey": [0],
"test": [2],
"laufzeitumgebungen": [2],
"jemand": [2],
"omegat": [2,0,3,7,1,4,6,8,5],
"einstellung": [4,5,0,2,[1,3,6,7]],
"allemand": [1,7],
"deepl": [1],
"notizblock": [5,3,4,[0,8]],
"bedienungsanleitung": [4,3,0,8,2],
"drücken": [7,3,4,5,1],
"angenommen": [0],
"durchzuführen": [[2,3,4]],
"durchschalten": [[0,4]],
"virtual": [7,2],
"denen": [0,7,[2,4],[3,5]],
"console-align": [[2,7]],
"dissimul": [5],
"ansatz": [2],
"back": [0],
"authentifizierungsfehl": [[2,5]],
"besteht": [0,[3,6,7,8]],
"projectopenrecentmenuitem": [0],
"fr_fr": [3],
"kommentarfeld": [5],
"standardeinstellung": [[2,4]],
"thèse": [1],
"load": [7],
"zutreffen": [7],
"umfangreichen": [[2,7]],
"darzustellen": [1],
"verwendung": [7,2,0,[1,5]],
"fortschrittsanzeig": [5],
"zusammenhängend": [0],
"issue_provider_sample.groovi": [7],
"und": [0,2,7,3,4,5,1,6,8],
"platzhalt": [1,[0,2]],
"herkunft": [[1,5]],
"unl": [5],
"verwandten": [6],
"tag-einstellungen": [3],
"eingestellten": [4],
"modell-id": [1],
"editoverwritemachinetranslationmenuitem": [0],
"relat": [1],
"console-stat": [2],
"bezug": [4],
"ingreek": [0],
"bearbeiten-menü": [[0,8]],
"lunch": [0],
"löschungen": [3],
"dienstnamen": [5],
"adressbuch": [6],
"id-cod": [0],
"f12": [7],
"convert": [2],
"orten": [2],
"voneinand": [0,[2,7]],
"positionell": [0],
"projectexitmenuitem": [0],
"repository-zuordnungen": [2,7],
"wirklich": [4],
"haben": [2,0,[3,7],4,1,5,6],
"legen": [1],
"text": [0,4,7,[1,5],2,3],
"editregisteruntranslatedmenuitem": [0],
"konsol": [2],
"init": [2],
"remote-projekt": [2],
"analysieren": [8],
"sitzung": [[3,5,7]],
"zusammengeführt": [7,[0,3]],
"segmentierungseinstellungen": [[0,2]],
"bitt": [[2,3]],
"haupt-memori": [7],
"manag": [2],
"manifest.mf": [2],
"mitgliedern": [2],
"anführungszeichen": [0,7],
"schaltet": [4],
"maco": [0,2,4,5,3,1],
"tm-root-ordn": [0],
"standardausgab": [2],
"erzwingt": [0],
"gelesen": [[0,2]],
"doc": [7,0],
"vergessen": [1],
"interval": [2],
"freiheiten": [8],
"standard-omegat-dateien": [0],
"mitg": [2],
"unverändert": [[1,7]],
"output-fil": [2],
"mittel": [7],
"status": [7,[2,5]],
"verarbeitet": [0,2],
"server": [2,1,5],
"darum": [2],
"identifiziert": [1,2],
"paramet": [2,7,[0,1]],
"systemweit": [2],
"run-on": [0],
"verlinkten": [3],
"mal": [2,0,[4,6],3,[5,7]],
"semikolon": [2],
"man": [[0,2]],
"freigegeben": [2],
"stand": [[2,3]],
"map": [2,6],
"ausführbaren": [2],
"url": [2,1,6,[0,3,4,7]],
"megabyt": [2],
"uppercasemenuitem": [0],
"viewmarkuntranslatedsegmentscheckboxmenuitem": [0],
"fußnoten": [0],
"entscheidung": [6],
"relev": [[0,1]],
"needs-review-transl": [0],
"tagwip": [7,3],
"mt-ordner": [6],
"use": [2],
"usd": [7],
"sicherheitsgründen": [[1,7],0],
"korrektur": [7],
"standardstruktur": [6,[7,8]],
"omegat.jar": [2,0],
"omegat.app": [2,0],
"usr": [[0,1,2]],
"usw": [0,1,[4,5,7],3],
"ersatztext": [7],
"alten": [2],
"vollwertig": [2],
"credit": [4],
"linux-system": [0],
"formatendungen": [2],
"halbbreiten": [7],
"abweichend": [1],
"modifikationsinfo": [0,1,[4,5]],
"utf": [0,6],
"ausgabeformat": [2],
"doppelklicken": [2,7,[0,5]],
"ermitteln": [1],
"html-tag": [0],
"java-messageformat-platzhalt": [1],
"folgendem": [2],
"servic": [5],
"regelmäßig": [2,6],
"null": [[0,7],2],
"übereinstimmen": [7,[0,1,5,6],[2,3,4]],
"gesamtzahl": [5,7],
"cleanup": [7],
"macos-tastenkürzel": [0],
"quelldatei": [0,4,7,3],
"unterordnern": [2,[0,4]],
"herunterladen": [2,0,4],
"loszulegen": [3],
"auszublenden": [7],
"entdecken": [7],
"folgenden": [0,7,2,[3,5],[4,8]],
"wiederherstellen": [5,[0,1,2],4],
"einigen": [1,[2,4],[0,3,6]],
"omegat-konfigurationsordn": [[0,2]],
"dtd": [[0,2]],
"rechtschreibprüfung": [[6,7],[1,3],[0,4,8]],
"ermittelt": [1],
"anhand": [[3,5,7]],
"plattform": [0],
"identisch": [4,[2,7],1,[0,5,6]],
"languagetool-problem": [4,0],
"meist": [7],
"irgendeinem": [[0,3]],
"projectcompilemenuitem": [0],
"console-transl": [2],
"java-konfigur": [2],
"normalerweis": [[0,1,2,7]],
"unicode-literal": [0],
"erlauben": [[0,1,7]],
"anspruch": [2],
"vorgenommen": [0,[1,2,3,4]],
"macos-äquival": [2],
"unicode-skript": [0],
"geschlossen": [5,[2,4,7]],
"optionsautocompletehistorycompletionmenuitem": [0],
"gotonextuniquemenuitem": [0],
"wordart": [0],
"objektattribut": [0],
"kürzere": [7],
"leerzeichen": [0,7,3,[1,4],5],
"einstellungen": [4,7,0,2,[1,3],[5,6],8],
"about": [0],
"commit": [2],
"targetlocalelcid": [0],
"gesamten": [0,3,[2,4,7]],
"monat": [[0,2]],
"wörterbuch": [1,[3,6],[4,5]],
"gleichbedeutend": [1],
"project_stats_match.txt": [[4,6]],
"tab-separ": [0],
"benötigt": [[0,2],7],
"bekommt": [0],
"japanischen": [2,1],
"verkettung": [0],
"tmx-dateien": [2,7,6,1],
"ersten": [7,0,2,[4,5],[1,3,6]],
"standardauswahl": [7],
"alphanumerischen": [0],
"systemzeit": [0],
"fehl": [7],
"erster": [[2,4]],
"applikationen": [0],
"textverarbeitungsprogramm": [7],
"zurückzusetzen": [0],
"fraglich": [7],
"terminologiedateien": [0],
"reibungslos": [3],
"libreoffic": [[0,3]],
"autocompleterclos": [0],
"qualiti": [7],
"anzuwenden": [7],
"kurzschreibweis": [0],
"dürfen": [1],
"long": [0],
"automatischen": [7,4],
"nicht-gui-modus-optionen": [2],
"warnungen": [2],
"dienst": [5,1,2],
"eigentlichen": [0],
"geändert": [0,1,7,2,3,4,6],
"feld": [[4,7],2,[0,5]],
"griechischen": [0],
"mit": [0,2,7,1,4,3,5,6,8],
"spracheinstellungen": [1],
"dateinam": [[2,5,7]],
"zählweis": [4],
"sammeln": [2],
"gebunden": [7],
"projektmanag": [2],
"fortgeschritten": [0],
"vergewissern": [[1,3,5,7]],
"zeichenklassen": [0],
"variablen": [[0,1],8],
"ausblenden": [5,1],
"ausführen": [7,2,[1,6],[3,4,8]],
"vorgang": [3,2],
"löscht": [[1,4]],
"generierten": [0],
"enthält": [0,6,2,7,3,5,1,4],
"viewdisplaysegmentsourcecheckboxmenuitem": [0],
"editregisteremptymenuitem": [0],
"erwartete": [8],
"vks-hosting-serv": [2],
"mismatch": [7],
"teamprojekt-konfigur": [2],
"open": [7,0,[1,2]],
"vorrang": [[4,7],2],
"skript": [7,[0,4],2,1],
"project": [7,[2,5,6]],
"取得": [[1,7]],
"xmx1024m": [2],
"exkludierten": [2],
"autotext": [1],
"verarbeitung": [1],
"grundlegend": [[2,6]],
"regex-definiert": [0],
"interpunktionszeichen": [0],
"ausschließlich": [0],
"arbeiten": [2,0,3,7,[1,6]],
"penalty-xxx": [[2,6]],
"gotonextsegmentmenuitem": [0],
"niemal": [6],
"sinnvol": [7,1],
"projekteigenschaften-dialogfenst": [2],
"abbrechen": [2],
"große": [0],
"kde-benutz": [2],
"textformatierungen": [7],
"omegat-entwicklungsseit": [[0,1]],
"errechnet": [7],
"ausnahmeregel": [0,[1,3]],
"excel-zellen": [0],
"dropbox": [2],
"abort": [2],
"aufruf": [4,0],
"left-to-right": [0],
"internet": [[0,1,4]],
"comma-separ": [0],
"wiederhergestellt": [2],
"wenig": [2,[4,7]],
"professionell": [3],
"internen": [2,[1,4,5]],
"bitten": [2],
"beschriebenen": [[0,2,7]],
"nummer": [0,4,7,[1,5,6]],
"filtereinstellungen": [0],
"unübersetzbaren": [0],
"einzuholen": [5],
"anbieterlist": [1],
"visualisiert": [1],
"omegat-gui": [2],
"html-dokument": [0],
"interess": [0],
"ihrer": [2,3,7,4,1,[0,5]],
"erstklassig": [7],
"textbereich": [[5,7]],
"ihrem": [2,4,7,[0,1],[3,5,6]],
"separaten": [7,[3,5]],
"prototypbasiert": [7],
"ihren": [2,5,[3,7],0,6,[1,4,8]],
"kaskadierend": [1],
"schlüsselbasiert": [7],
"navigationsverlauf": [3],
"externe": [8],
"ausgerichteten": [0],
"layout": [1,3,[0,5,7]],
"registri": [0],
"fortzufahren": [3,[4,7]],
"bash": [0],
"basi": [[0,5]],
"tmroot": [0],
"maus": [7,3],
"omegat-filt": [0],
"schaltflächen": [7,4,[0,3]],
"vergleicht": [7],
"titel": [[4,5]],
"大学": [1],
"suchbefehl": [1,[4,7]],
"durchführung": [2],
"einzelnen": [0,[3,5,7]],
"freigeben": [2],
"insertcharslr": [0],
"kopien": [2,8,1],
"zielkodierung": [0],
"o.ä": [1],
"work": [0],
"editierbaren": [0],
"wort": [[0,7],[4,6],[1,5]],
"baut": [7],
"cloud-dienst": [2],
"klon": [2],
"objekt": [7,2],
"word": [[3,7]],
"lingue": [1],
"zusammenarbeit": [3],
"sortierung": [5],
"dabei": [[0,3,7]],
"auto-propag": [[2,7]],
"auto-ordn": [6],
"bearbeiten-menü-tabell": [0],
"fest": [7,1],
"fett": [1,[5,7],[0,3]],
"mithilf": [[0,6]],
"exportierend": [7],
"lautet": [[2,3]],
"developer.ibm.com": [2],
"projektverzeichniss": [0],
"mrs": [1],
"kanadischem": [1],
"jederzeit": [3,7,6],
"funktional": [7],
"zieldokument": [2],
"fehlermeldungen": [5],
"speicherort": [2,0,7,[3,6],4,1],
"verschieden": [0,[2,3],[4,7],5,1],
"allen": [2,7,1,[0,4]],
"klick": [5],
"aller": [7,[1,2,3]],
"dokumenten": [[0,7],2],
"gleich": [7,1,[0,3,6]],
"änderungsverlauf": [4],
"suchmethoden": [7],
"eck": [5],
"maustast": [[4,5,7]],
"vielen": [0,2],
"entpacken": [2,6],
"detailliert": [2],
"html": [[0,2]],
"spell": [0],
"zielsprachcod": [3],
"insertcharsrl": [0],
"zusammenfassung": [8],
"sofort": [3,2,7,[0,5]],
"tagtäglich": [0],
"bisher": [2],
"finit": [1],
"tm-auto-ordn": [0],
"sprachabhängig": [7],
"markierung": [0,[1,4,7]],
"tag-verarbeitung": [1,[3,4,8]],
"beschädigt": [2],
"exakten": [4,7],
"www.ibm.com": [1],
"jres": [2],
"nützlich": [0,2,7,[1,4],[3,5]],
"quellcod": [2],
"nehmen": [[2,3,6],7],
"angebracht": [7],
"kommiss": [4],
"langsam": [2],
"zutrifft": [0],
"definieren": [0,1,7,3,[2,5]],
"hinwei": [3],
"toolsalignfilesmenuitem": [0],
"entkommentieren": [2],
"übertragen": [7],
"usb-stick": [2],
"andere": [8],
"benutzernam": [2],
"font-ersetzungen": [4],
"recht": [[5,7],0,[3,4]],
"unterstrich": [0],
"command": [4,0,[2,3]],
"rechts-nach-links-text": [0],
"scripting-fenst": [[4,7]],
"wirkt": [7,[0,1]],
"zuzuweisen": [7,[3,6]],
"unicode-blöcke": [8],
"funktionen": [0,3,6,[4,5,7]],
"tag-fre": [[3,7]],
"betriebssystemen": [6],
"zugewiesen": [4,[0,1,5,7]],
"bestätigen": [[0,1],7],
"onecloud": [2],
"viewmarkbidicheckboxmenuitem": [0],
"filterparameterdatei": [2],
"aktualisiert": [6,3],
"dateinamenvariablen": [0],
"geöffnet": [[2,7],[4,5],[1,3],0],
"qa-validierung": [4],
"med-format": [4],
"kleinschreibung": [0,[4,7],1],
"branch": [2],
"respektiert": [2],
"via": [2,5,7],
"springen": [0,7,3,5],
"fileshortpath": [[0,1]],
"zuzugreifen": [[0,7],[2,4,6]],
"zielspracheneintrag": [4],
"zentral": [6],
"begrüßt": [3],
"日本語": [7],
"steuerzeichen": [0],
"themen": [2],
"ausrichtung": [7,[0,2]],
"sinnlos": [0],
"entwickeln": [2],
"änderung": [7,[1,6],[0,2,3,4]],
"ausgegraut": [[4,7]],
"dienstprogramm": [2],
"zurückgesetzt": [0],
"vergeben": [0],
"entwickelt": [[3,7]],
"med-projekt": [[0,4]],
"außerdem": [2,[3,5,6,7]],
"buchstabenbereich": [0],
"sprach": [2,[3,7],[0,1,6]],
"version": [2,4,[3,8]],
"statusleist": [5,[0,3]],
"konfigurationsdatei": [2,7],
"effizient": [7],
"grundsätze": [8],
"übersetzungsdienst": [[4,5],1],
"folder": [5,7],
"durchführen": [2],
"bindestrich": [0],
"klassen": [0],
"abzulegen": [2],
"projektadministr": [2],
"gebündelt": [2],
"detail": [6],
"kleingeschrieben": [0],
"vereinfachen": [[1,3]],
"anweisungen": [2,[3,7]],
"weist": [[0,2,3,7]],
"belegten": [4],
"geschützt": [1,[0,4],7,3,5],
"standardinterval": [1],
"projecteditmenuitem": [0],
"ein": [0,2,7,1,3,4,5,6,8],
"festgestellt": [3],
"begonnen": [[3,7]],
"einfügt": [5],
"new_word": [7],
"konvert": [2],
"generaldirekt": [4],
"run\'n\'gun": [0],
"nashorn": [7],
"machin": [7,2],
"unsung": [0],
"vks": [2],
"strg": [4,[0,3]],
"sprachwörterbuch": [1],
"java-laufzeitumgebung": [2],
"stund": [3],
"last_entry.properti": [6],
"gegen": [2],
"odf-dateien": [0],
"bildern": [0],
"übersetzern": [2],
"bestimmt": [2,0,1,[5,6,7],[3,4]],
"zuordnen": [3],
"komprimiert": [[1,6]],
"resname-attribut": [0],
"präfix": [[1,2,6]],
"internetverbindung": [1],
"ursprünglichem": [3],
"gibt": [7,2,0,5,4,[1,3],8],
"brauchen": [3],
"rechts-nach-links-seg": [0],
"autocompleternextview": [0],
"ursprünglichen": [2,3,1],
"absatzblöck": [3],
"specif": [7],
"spring": [5],
"kund": [0],
"printf-variablen": [[0,1]],
"zulassen": [1],
"dsun.java2d.noddraw": [2],
"paarweis": [0],
"gültigen": [[0,5]],
"stellt": [2,[1,7],[0,3,4,5]],
"benutzerdefinierbaren": [7],
"omegat-menü": [2],
"ell": [1],
"bezeichnung": [7],
"xml-basiert": [0],
"tastendarstellung": [[0,4]],
"editorfirstseg": [0],
"x0b": [2],
"konform": [2,[3,7]],
"möglich": [2,[0,1],[4,5],[3,8]],
"effekt": [0],
"jahr": [[0,2,3]],
"identifizierten": [1],
"angelegt": [[2,3,6,7]],
"altern": [4,0,[5,7],1,[2,3]],
"http": [2,1],
"keinen": [0,[1,3,4,6]],
"äpfel": [0],
"nächster": [0],
"willkommen": [3],
"vorhanden": [7,[0,2],[1,4,5,6]],
"nächstes": [0,4,3,7,[1,2,5]],
"nächsten": [4,0,7,2,[1,3]],
"könnte": [3],
"komma": [0],
"liest": [[1,7]],
"lisenc": [0],
"magischen": [0],
"anfertigen": [8],
"hervorzuheben": [7,1,6],
"von": [0,2,7,1,4,6,3,5,8],
"softwar": [0],
"vom": [2,0,4,7,5,[1,6]],
"segmentpaar": [7],
"vor": [[0,2],7,4,3,1,[5,6]],
"projectsinglecompilemenuitem": [0],
"end": [0,7,1,3],
"schließenden": [0],
"übersetzungsdateien": [3],
"lisens": [0],
"einfachsten": [0],
"früheren": [0,[2,3,5]],
"erhält": [6],
"beschränkt": [5,2,[0,6]],
"myfil": [2],
"visuel": [4],
"überschrieben": [2,3,4,[0,1,7]],
"machen": [0,2,4,[3,5,6]],
"kommt": [2,0],
"aufgelistet": [0,2,[4,7]],
"aufrufen": [0,[4,5]],
"env": [0],
"dokument.xx": [0],
"shell-skripten": [0],
"okapi": [2],
"übersetzend": [5],
"page_down": [0],
"mitgeliefert": [7],
"umbenannt": [3],
"entsprechen": [7,2,[0,3]],
"kurz": [[3,7],[0,1,2,5]],
"skriptordn": [7,1],
"copyright": [4],
"springt": [4],
"omegat-übersetzungsprojekt": [7],
"kehrseit": [0],
"notiz": [4,5,3,0],
"bedingungen": [8],
"verwaltung": [2],
"system-os-nam": [0],
"kommentaren": [7,5],
"omegat-benutzeroberfläch": [2,[3,4]],
"insertcharspdf": [0],
"einzulesen": [7],
"folglich": [7],
"kenntlich": [4],
"damit": [0,[2,3,7],1,[4,6,8]],
"heapwis": [7],
"befehlszeilenoptionen": [2],
"standardregeln": [1],
"hervorheben": [[0,4],5,[1,7],[3,6]],
"zugeordnet": [2],
"programmen": [0],
"festzulegen": [7,[1,2,5]],
"tar.bz2": [6],
"bereitgestellten": [2],
"kürzlich": [5],
"editor-tastenkürzel-tabell": [0],
"textersetzung": [7,[0,3,4,8]],
"sicherungskopien": [2,[1,6]],
"kleingeschriebenen": [0],
"segmentierungsregeln": [7,0,4,1,2,[6,8]],
"bundle.properti": [2],
"contributors.txt": [0],
"dritten": [5],
"mühsam": [3],
"www.regular-expressions.info": [0],
"path-einstellungen": [2],
"fensterbereich": [5,3,7,4,1,6,2],
"einbezogen": [1],
"merkmal": [0],
"garant": [0],
"tastenkürzel": [0,7,4,3,1],
"sourcelang": [0],
"schritt": [7,2,0,6],
"verfälschen": [2],
"apache-ant-stil": [2],
"fensterbereiche": [8],
"systemüblichen": [3],
"empfohlen": [[0,2]],
"festlegen": [2,[0,4],[1,5,7]],
"isn\'t": [0],
"ander": [2,0,3,7,1,5,6],
"optionsdictionaryfuzzymatchingcheckboxmenuitem": [0],
"welcher": [0,[2,5,7]],
"aufklapplisten": [7],
"zeiten": [3],
"schreibzugriff": [2],
"überblick": [5],
"spiegelt": [[3,4,7]],
"assur": [7],
"interfac": [2],
"spiegeln": [6],
"zweiten": [4],
"projet": [5],
"betriebssystem": [0,4,[1,2,5,7]],
"notfal": [[2,6]],
"auto-synchronisieren": [7],
"vertriebslizenz": [0],
"umbruch": [1,0],
"sourcelanguag": [1],
"ausdrücken": [0,2,[1,7,8]],
"segmentweis": [7],
"omegat-instanz": [2],
"helpupdatecheckmenuitem": [0],
"stellen": [2,1,[3,7],[0,5]],
"grundlag": [2],
"vorgegeben": [0],
"esc": [5],
"exampl": [7],
"ähnelt": [2],
"einzeln": [0,7,3,[1,4]],
"nostemscor": [1],
"laufenden": [7,[2,4],0],
"project_chang": [7],
"grupp": [7,0,[1,5]],
"ersetzen": [7,[0,2],[1,4],[3,5],6],
"console-createpseudotranslatetmx": [2],
"gemeinsamen": [2],
"generisch": [1],
"vorbereitung": [0],
"schriftart": [1,5,3],
"fuzzyflag": [1],
"omegat-konfigur": [2],
"neu": [[4,7],0,2,3,[1,6],5],
"überlässt": [0],
"proxy-ip": [2],
"escap": [0],
"dateifiltern": [2,7],
"ignoriert": [0,[4,7],6,[1,2]],
"poisson": [7],
"runway": [0],
"protokol": [[1,2]],
"müssen": [2,0,[3,7],1,[5,6]],
"tool": [[1,2],7,[4,6],3,[0,8]],
"gedrückt": [[0,3,7]],
"ll-cc.tmx": [2],
"unterordn": [2,7,[0,6]],
"geänderten": [0,[7,8]],
"intervall": [1],
"gzip-format": [6],
"zielsegmenten": [7],
"aktiv": [4,[0,5],1],
"wiederholen": [3,[0,2,4]],
"grund": [2,[0,3,4]],
"allgegenwärtig": [4],
"datumsangaben": [0],
"seien": [2,7],
"umzuschalten": [5],
"standardtastenkürzel": [5],
"schreibweis": [0],
"freiheit": [8],
"titelschreibung": [[0,4]],
"slot": [4],
"möglichen": [2,[0,7]],
"editorfenst": [7],
"grunt": [0],
"tabellarisch": [1],
"wissen": [3],
"ungültig": [2],
"täglich": [2],
"berechnungsart": [7],
"gilt": [[5,7]],
"anpassungen": [[5,7]],
"magento": [2],
"ll.tmx": [2],
"zeilenvorschubzeichen": [0],
"offlin": [2],
"vordefiniert": [1,[0,2,3]],
"ll_cc.tmx": [2],
"u00a": [7],
"sonst": [[2,6]],
"offizielle": [8],
"match-variablen": [[1,8]],
"editorbereich": [[1,3,4,5],2],
"vorschlag": [1],
"shift": [0,4],
"nie": [0],
"erneut": [7,2,[1,3,4]],
"vorgehensweis": [2],
"java": [2,0,7],
"standardfarben": [1],
"rechtschreibung": [1],
"skriptdatei": [2],
"leistungsfähigkeit": [[1,7]],
"xmxsize": [2],
"alphabetisch": [[5,7]],
"validierungszweck": [0],
"ziehen": [5,[2,6]],
"halber": [7,2],
"project_save.tmx": [2,6,[3,7],4],
"abrufen": [4,[0,1,2]],
"fortschritt": [2],
"dictionari": [6,[1,5,7]],
"wichtigst": [6],
"beschränken": [0],
"plötzlich": [2],
"java-bundles-filt": [1],
"powershel": [[0,2]],
"eye": [0],
"beispiel": [0,7,1,[2,3,5],4,6],
"echtzeit": [5],
"dictionary": [8],
"umbruchregeln": [1],
"projektlayout": [2],
"übersetzungstool": [3],
"eingeladen": [3],
"erforderliche": [8],
"blattnamen": [0],
"rückgängig": [4,0],
"standard-xml-not": [1],
"korrekturen": [7],
"appl": [0],
"bericht": [[2,3]],
"anklicken": [[2,4],[1,5,7]],
"alphanumerisch": [0],
"kleinschreibung-sensitivität": [0],
"fortschrittsinformationen": [5],
"anfänglich": [0],
"heruntergefahren": [7],
"sudo": [2],
"verlieren": [[2,3]],
"verwaist": [7,5],
"timestamp": [0],
"bequemeren": [2],
"projectaccessrootmenuitem": [0],
"ungefärbt": [5],
"über": [2,0,[4,7],1,5,[3,6]],
"absatzbegrenzungen": [5,[0,1,4]],
"beheben": [2,[3,4,6,8]],
"grau": [[3,4]],
"such": [7,0,3,6],
"plugin": [2,1,0,3],
"glossarbegriff": [[1,3,6,7]],
"autocompletertableup": [0],
"igen": [6],
"erforderlichen": [6],
"java-messageformat-must": [0],
"abstürzt": [2],
"iger": [6],
"üben": [2],
"übersetzenden": [2,3,[4,6]],
"erkannt": [[0,4],7,[1,2,5,6]],
"projektglossar": [0,[2,4]],
"erweiterung": [7],
"glossar": [0,[5,7],4,6,3,1,2],
"stilistisch": [0],
"projectcommitsourcefil": [0],
"editinsertsourcemenuitem": [0],
"omegat-glossardateien": [0],
"standardspeicherort": [0],
"referenz-memori": [[2,4],[6,7]],
"verfeinern": [3],
"greifen": [3],
"viterbi": [7],
"microsoft": [0,[3,7]],
"projekteinstellungen": [[2,4],[3,6]],
"match-statistiken": [[4,6],0],
"projektdateien": [[1,2,3]],
"projectnewmenuitem": [0],
"ecmascript": [7],
"davor": [0,1],
"segment": [7,4,5,0,1,6,3,2],
"davon": [0,2,4],
"beteiligten": [2,7],
"auslösen": [2],
"changes.txt": [[0,2]],
"vollständigen": [0,5],
"benutzeroberfläch": [2,0,1,5],
"indexeinträg": [0],
"referenzdokument": [6],
"glossari": [0,7,6,[4,5]],
"ignored_words.txt": [6],
"kursiv": [[0,3,7]],
"glossare": [8],
"github.com": [2],
"configuration.properti": [2],
"fixiertem": [5],
"schriftsystem": [1],
"autocompleterlistpageup": [0],
"tragen": [7,[1,3]],
"nachfolgend": [0],
"glossary": [8],
"neue": [[2,7],3,4,0,6,1,5],
"vielzahl": [2,0],
"sequenzen": [0],
"roten": [[1,6]],
"projektbezogenen": [2],
"weiterverarbeitung": [4],
"zugangsdaten": [1,2,[0,5,8]],
"darstellung": [5,1,8],
"string": [2],
"hidden": [5],
"kanonisch": [0],
"indikatoren": [0],
"eigenschaften": [4,[3,5],[6,7],[0,2]],
"hälfte": [1],
"standard-editor-arbeitsbereich": [5],
"besond": [0,7,4],
"repräsentieren": [0,[1,2]],
"geschweiften": [0],
"irgendein": [0],
"hochgeladen": [2],
"fuzzy-match-präfix": [6],
"ausprobieren": [7],
"eingabedateien": [2],
"wechseln": [[4,7]],
"was": [7,[0,2],[1,3,4,5]],
"nachbearbeitungsskript": [0],
"war": [[1,3]],
"viewrestoreguimenuitem": [0],
"unterstreicht": [4],
"selection.txt": [[0,4]],
"wechselt": [4,7],
"xhtml": [0],
"itoken": [2],
"finder.xml": [[0,6,7]],
"refer": [0],
"ausgewählt": [7,4,5,2],
"dateisuch": [7],
"umschalt": [[4,7],0],
"herunt": [2,[3,6,7]],
"window": [0,2,4,5,3],
"call-out": [4],
"suchtreff": [7],
"positionelle": [8],
"disable-project-lock": [2],
"unterscheiden": [0],
"omegat.pref": [[0,1,7]],
"weiterschalten": [4,1],
"unterscheidet": [4],
"when": [5],
"durchsuchen": [[0,1,7],[3,4,5]],
"gui-start-kompatiblen": [2],
"übersicht": [2,0],
"schrift": [[0,1,5,7]],
"aufgerufen": [7,[0,3]],
"embed": [0],
"plan": [1],
"aktionen": [7,[4,5],2],
"klassennam": [2],
"howto": [3],
"rainbow-unterstützt": [2],
"stats-ausgabedatei": [2],
"sollten": [1,[0,2],7,6],
"widget": [5],
"tastenkürzel-definitionsdateien": [0],
"zuerst": [[1,2,7]],
"lässt": [2,3],
"abgekürzt": [0],
"direct": [0],
"gewünscht": [[1,2,4]],
"jeweiligen": [6],
"erfolgt": [2,7],
"veränderbar": [0],
"web": [2],
"klammern": [0,1],
"anleitungen": [2,[3,5,8]],
"en-us_de_project": [2],
"liegt": [3],
"zeilen": [7,[0,5]],
"weg": [2],
"symlink": [2],
"memories": [8],
"wem": [1],
"editselectfuzzy4menuitem": [0],
"editregisteridenticalmenuitem": [0],
"standardlayout": [5,8,3],
"rle-zeichen": [0],
"wer": [7],
"url-suchvorgäng": [1],
"menschlich": [1],
"ordentlich": [2],
"hanja": [0],
"beschreibbar": [0,[4,6],[2,7],5],
"erneuten": [2],
"java-bundles-format": [1],
"satzfragmenten": [3],
"advanc": [1],
"textcursor": [5,4,3,[0,7]],
"aufheben": [0],
"desselben": [1],
"fensterlayout": [[4,5]],
"fenstertitel": [7],
"ausgab": [2,1],
"nun": [[0,2],3],
"angegeben": [2,0,7,1],
"nur": [0,7,2,1,4,3,5,6],
"inhalt": [[0,2,7],1,3,6,4,5],
"müsste": [6],
"etwa": [3,[0,1,2,4,5]],
"ip-adress": [2],
"dict": [1],
"absätz": [0,7],
"nennt": [2],
"standardfensterlayout": [4],
"arbeit": [2,3,7,[0,4]],
"quelldateiformat": [7],
"älter": [2],
"innen": [5],
"marker": [2],
"translation-memory-match": [0],
"extrahiert": [[0,7]],
"weich": [0],
"allerd": [2,[0,7]],
"fallback": [0],
"schließlich": [[1,2]],
"erscheint": [[1,5,7]],
"option": [1,7,0,[2,4],6,3],
"spalten": [7,[0,1]],
"omegat-optionen": [2,0],
"verändert": [2,[1,3]],
"schnell": [7,3,[2,8]],
"erschwert": [0],
"txml-dateien": [2],
"auftritt": [5],
"grundsyntax": [0],
"voller": [7,3],
"kein": [0,1,7,2,4,3,6],
"wie": [0,2,3,[1,7],5,[4,6],8],
"steuern": [2],
"abgebrochen": [7],
"parallel": [[2,7]],
"dies": [0,2,1,7,4,6,5,3],
"verändern": [[2,4,8]],
"wirksam": [0,7],
"wir": [0,[2,3]],
"dazugehörigen": [2],
"visuell": [3],
"suchen": [7,0,4,3,1,5],
"cjk-sprachen": [0],
"ersetzung": [7],
"rechtschreibproblem": [4],
"kleiner": [5],
"netzwerk": [2],
"kleinen": [0],
"xliff-filt": [0],
"archiv": [6],
"genannten": [7],
"vertrauenswürdig": [2],
"user": [1],
"matching-prozentsätz": [[1,5]],
"extens": [0],
"back_spac": [0],
"fortsetzen": [0],
"tooltip": [[1,5]],
"prozentwert": [1],
"dateiform": [[0,2],[3,4,5,7]],
"robot": [0],
"rand": [7,5],
"erstellt": [2,6,4,[0,3,7],5,1],
"hintergrundfarb": [6],
"ganz": [[0,3],1,[5,7]],
"zeichenbereich": [0],
"nahezu": [2],
"zurückgreifen": [5],
"ausgehend": [6],
"textblöck": [7],
"ambitioniert": [3],
"direktional": [[0,4]],
"entsprechend": [0,7,[1,2,4,6]],
"änderungsbeispiel": [0],
"dezimalstell": [0],
"omegat-paket": [2],
"po-head": [0],
"anzulegen": [2],
"findet": [0,7],
"ab": [[0,4],[1,2,7]],
"eclips": [2],
"konfigur": [2,7],
"zurückzukonvertieren": [2],
"panik": [3],
"behebt": [2],
"diff": [1],
"finden": [0,2,7,4,[1,3],6,5],
"al": [0,2,7,1,6,3,4,5],
"am": [[0,7],[1,2],5,3,4],
"an": [0,2,[5,7],4,1,3,6,8],
"editmultiplealtern": [0],
"installationsort": [2],
"ausgeben": [3],
"ziel": [7],
"elementen": [[1,3]],
"formen": [7],
"pseudoübersetzten": [2],
"modifizieren": [[6,7]],
"hauptbereich": [7],
"hierarchi": [6],
"absatzeben": [0],
"genug": [2],
"wenigen": [4],
"globalen": [7,0,1,2],
"grundlagen": [0],
"remote-omegat-projekt": [4],
"technisch": [2],
"direkt": [2,0,4,[6,7],1],
"omegat-distribut": [2],
"zurückzukehren": [[3,5]],
"suchvorgäng": [7,1,4,[0,5],6],
"filters.xml": [0,[1,2,6,7]],
"übersetz": [2,3,0,1,5],
"groß": [0,7,4,1],
"mindesten": [6,[1,2,5,7]],
"br": [0],
"prozentsatz": [5,1],
"gebeten": [2],
"segmentation.conf": [[0,2,6,7]],
"wollen": [2,[4,7],0],
"openxliff-filt": [2],
"ca": [2],
"bewertung": [7],
"cc": [2],
"wendet": [4],
"hilfe": [8],
"ce": [2],
"nützlichsten": [0],
"gefragt": [[4,7]],
"umgekehrt": [0,2,7],
"cr": [0],
"flexibilität": [3],
"ähnlichsten": [5],
"cs": [0],
"hilft": [3],
"applikationsordner": [8],
"apach": [2,7],
"da": [2,[0,7],3,1],
"adjustedscor": [1],
"wenden": [2,5],
"omegat-layout": [0],
"dd": [2],
"de": [1,5],
"extern": [7,1,[0,4],2,[3,5]],
"f1": [[0,4,7]],
"f2": [[3,5],[0,7]],
"f3": [[0,4],5],
"zielsprachenstandard": [0],
"f5": [[0,3,4]],
"veröffentlicht": [2],
"dz": [6],
"außer": [2,0],
"editundomenuitem": [0],
"oberen": [[1,3,5]],
"außen": [5],
"abgezogen": [6],
"abgelegt": [[0,6],[2,3,4,5,7]],
"dateifilter-optionen": [4],
"insbesonder": [[2,5]],
"standardnamen": [[2,6]],
"andocken": [5],
"belazar": [1],
"sowohl": [7,0,2,[1,5]],
"en": [0],
"verwenden": [2,7,0,[1,3],5,4,6,8],
"umbrüch": [1],
"dateiauswahl": [2],
"verwendet": [0,2,7,1,3,4,6,5],
"er": [[6,7],0,5,2],
"es": [2,[0,7],[1,4],3,5,8,6],
"gehosteten": [2],
"ausnahmeregeln": [0,1],
"listenansicht": [5],
"auswahlopt": [0],
"möglichkeit": [[0,2,4],7],
"vertikal": [0],
"registrierung": [1],
"auszurichtenden": [7],
"übersetzung": [4,[0,1,2],3,7,5,[6,8]],
"origin": [2],
"for": [[0,7],2],
"exclud": [2],
"erfassen": [0],
"ausmacht": [0],
"fr": [2,[1,3]],
"konsolenmodusnam": [2],
"printf-funkt": [0,1],
"gegenstück": [0],
"content": [0,2,7,1],
"umgehen": [4],
"duckduckgo": [1],
"sobald": [[0,2,3,7],[4,6]],
"dateityp": [4],
"applescript": [2],
"wären": [2],
"json": [2],
"chinesisch": [1],
"gb": [2],
"vorbereitungen": [2],
"helplogmenuitem": [0],
"methoden": [2],
"testzwecken": [2],
"verlaufsvorhersagen": [3],
"gültige": [[2,6]],
"editoverwritetranslationmenuitem": [0],
"entstanden": [2],
"gründen": [2],
"aeiou": [0],
"gebildet": [0],
"standardverfahren": [2],
"groovy-code-snippet-beispiel": [7],
"form": [0,7],
"somit": [3,0],
"kollabor": [2],
"versucht": [7,0,[1,2]],
"fort": [[2,3]],
"paketinhalt": [2],
"hh": [2],
"setzen": [7,[0,2],[1,4,6]],
"duser.languag": [2],
"viewmarkparagraphstartcheckboxmenuitem": [0],
"anfallen": [3],
"stützt": [0],
"teammitglied": [[2,3]],
"darüber": [[3,4]],
"fragt": [[1,7]],
"file-target-encod": [0],
"gängiger": [2],
"unveränderlichkeit": [6],
"mainmenushortcuts.mac.properti": [0],
"sicher": [2,1,[0,7]],
"id": [1,0,7],
"https": [2,1,0,[5,6]],
"quelldateien-list": [0],
"darstellen": [1,[0,2,3]],
"if": [7],
"sofern": [2,[0,7],[1,4],5],
"abweichen": [0],
"project_stats.txt": [6,4],
"projectaccesscurrenttargetdocumentmenuitem": [0],
"konfigurationen": [1],
"klassenbeispiel": [0],
"im": [7,[0,2],[1,4],3,5,6],
"in": [0,2,7,4,1,[3,5],6,8],
"regelmäßigen": [2],
"drittanbietern": [2,3],
"individuellen": [2],
"termin": [2],
"index": [0,2],
"is": [0],
"omegat-applik": [2],
"absatzsegmentierung": [[0,7]],
"projectaccesstmmenuitem": [0],
"odf": [0],
"geladenen": [6],
"ja": [7,[1,2]],
"sperrigen": [3],
"mehrmal": [[2,3]],
"je": [0,[2,5],[1,7],[3,4]],
"indem": [7,[2,3],0,[4,6]],
"hingegen": [0],
"odt": [[0,7]],
"gotonexttranslatedmenuitem": [0],
"xliff-dateien": [[0,2]],
"charset": [0],
"wider": [[3,4,6,7]],
"librari": [0],
"sicherstellen": [1],
"hochladen": [2],
"leerraum": [0],
"logogramm": [0],
"unterteilt": [5],
"toolscheckissuescurrentfilemenuitem": [0],
"libraries.txt": [0],
"zuvor": [4,3],
"learned_words.txt": [6],
"omegat-java-datei": [2],
"blockelementen": [0],
"ftl": [[0,2]],
"vollständig": [0,2,[1,3,6]],
"erheblich": [0],
"benutzeroberflächen": [[2,4]],
"bidi-markierungen": [7],
"viewdisplaymodificationinfoallradiobuttonmenuitem": [0],
"zuverlässigen": [8],
"java-regex-dokument": [[0,3]],
"ausnahmemuster-dialogfenst": [7],
"la": [1],
"mitglied": [2],
"systemvariablen": [[1,7]],
"darunt": [[4,7]],
"lf": [0],
"versteckt": [7,[0,6]],
"oft": [3,[0,1]],
"li": [0],
"ll": [2],
"rlm-zeichen": [0],
"weiterzuverarbeiten": [0],
"projektspezifisch": [7,[1,2]],
"lu": [0],
"bestätigt": [6],
"skripte": [8],
"bash-skript": [2],
"wahrscheinlichst": [5],
"ja-jp.tmx": [2],
"glossareinträg": [[4,5]],
"that": [0],
"cycleswitchcasemenuitem": [0],
"allgemeinen": [7],
"mb": [2],
"führen": [0,4,[1,7]],
"me": [2],
"drei": [0,2,[1,4,6],[3,5,7]],
"mm": [2],
"entri": [7],
"nachdem": [[2,3],[0,7],1],
"ms": [0],
"dunkl": [1],
"mt": [6],
"bestehen": [0,7],
"xliff-inhalten": [0],
"arbeitsschritt": [3],
"my": [[0,2]],
"wichtig": [0,2,3],
"berechnet": [[1,5],7],
"license": [8],
"aktuel": [4,7,[0,5],[1,2,6]],
"ohn": [2,[0,7],5,[1,6],[3,4]],
"formulierung": [3],
"funktionalität": [2],
"technischen": [0],
"updat": [[1,2],[0,4,7]],
"matchingzweck": [2],
"freiwilligen": [7],
"basierend": [3,[0,4,7]],
"licenss": [0],
"lokalen": [2,[1,4],0,7],
"datum": [1,[2,3,6]],
"genießen": [3],
"no": [0],
"code": [0,2,[3,7],1],
"erfasst": [0],
"omegat-team-projekt": [2],
"gotohistoryforwardmenuitem": [0],
"gesuchten": [2,0],
"head": [0],
"blau": [[5,7]],
"ob": [1,[0,7],[4,5],2,6],
"rekursiv": [7],
"aufgrund": [2],
"noch": [[3,7],2,1,[0,4],5],
"of": [[0,2,7]],
"ok": [7,4,3],
"umwandeln": [1],
"aufgelöst": [0],
"auswahl": [7],
"or": [0],
"aktuellsten": [2],
"möglichkeiten": [2,[3,4]],
"qualitätssicherungstool": [4],
"zieltexten": [7],
"derzeit": [0,1],
"editinserttranslationmenuitem": [0],
"pc": [2],
"fileextens": [0],
"umwandelt": [0],
"sehen": [[3,6],[0,5,7]],
"paragraph-tag": [0],
"merken": [[4,7]],
"po": [2],
"entsperren": [3],
"erkennbaren": [7],
"folienkommentar": [0],
"mitwirkenden": [0],
"verschieben": [7,6,2,[1,3,5]],
"ausschließen": [6],
"einträg": [[5,7],0,1],
"geteilt": [[0,7],3],
"qa": [7],
"autocompletertablefirst": [0],
"häkchen": [[0,7]],
"verfügt": [7],
"notwendigen": [[2,3]],
"they": [0],
"github": [2],
"qs": [2],
"fehlen": [5],
"edit": [2],
"zellen": [7],
"editselectfuzzy5menuitem": [0],
"angeben": [[0,1,2]],
"fehlerbehebungen": [[0,4]],
"rechts-nach-links-einbettung": [0,4],
"exakt": [7,2,3],
"rc": [2],
"protokollieren": [[0,2]],
"auswirkungen": [7],
"includ": [2],
"eintragen": [3,[4,7]],
"befehlssyntax": [2],
"segmentbegriffen": [4],
"zurücksetzen": [7,1],
"t0": [3],
"t1": [3],
"aufgezeichnet": [3],
"schließend": [0],
"fehler": [2,[4,7],[0,1,3,5,6]],
"t2": [3],
"hebt": [[0,7]],
"websuchvorgäng": [1],
"t3": [3],
"womit": [2],
"nachbearbeitungsbefehle": [8],
"sa": [1],
"unterstreichen": [5],
"sc": [0],
"formatierung": [3],
"zahl": [[0,2,6],[1,3]],
"durchschnittsbewertung": [7],
"versuchen": [[1,2,7]],
"so": [0,3,7,[4,6,8],[1,2]],
"gedacht": [6,[0,2]],
"starten": [2,0,7,[1,3,4,5]],
"exported": [8],
"referenzglossar": [0],
"intern": [[0,2,4]],
"editoverwritesourcemenuitem": [0],
"omegat.autotext": [0],
"kilobyt": [2],
"ausgeliefert": [[1,7]],
"projektordnerhierarchi": [2],
"enforc": [6,4,[0,2],[1,3]],
"remov": [2],
"tm": [[2,6],4,[0,7],3,[1,5,8]],
"startet": [2,4],
"to": [2,[0,5,7]],
"möchten": [7,0,2,3,[1,6],4],
"v2": [2,1],
"omegat-projekt": [2,[6,7],3],
"können": [2,0,7,3,1,4,5,6,8],
"stammen": [[2,6],[0,5]],
"berücksichtigen": [4,7],
"windows-logo": [0],
"file-sharing-system": [2],
"viewmarkautopopulatedcheckboxmenuitem": [0],
"eckigen": [0],
"projectwikiimportmenuitem": [0],
"countri": [2],
"glossarbereich": [5],
"um": [7,0,2,3,1,5,6,4,8],
"kanadisch": [1],
"sicherung": [2],
"un": [2],
"up": [0],
"geschützter": [4],
"newword": [7],
"anfang": [0,6,[2,3],[1,4,5,7]],
"this": [0],
"kennzeichnen": [[0,4,7]],
"wörtern": [0,[4,7],6],
"unsegmentiert": [3],
"standarddateien": [0],
"geschützten": [[3,7]],
"aufgefordert": [3],
"kontrollkästchen": [1],
"verbesserungen": [[0,4]],
"opt": [2,0],
"kennzeichnet": [4,1],
"beizubehalten": [4],
"extract": [7,1],
"kaptain-skript": [2],
"textsuche": [8],
"know": [0],
"prüfregeln": [1],
"region": [0],
"standardglossar": [[0,6]],
"vs": [1],
"support": [2],
"changed": [1],
"endgültigen": [3],
"bestehenden": [3,2],
"sein": [[0,7],2,[1,3],6,5,4],
"ändert": [4,[0,2,6,7]],
"regex-online-tool": [0],
"schließen": [7,0,2,4,3],
"jar-paket": [2],
"seit": [[0,4,5,7]],
"ändern": [0,7,2,4,[1,3],5,6],
"segmentieren": [[0,3]],
"konflikten": [3],
"anzupassen": [[0,1],3],
"bedarf": [7,2,1],
"we": [0],
"gängigsten": [[0,2]],
"vokal": [0],
"befehlsbeispiel": [1],
"dann": [7,3,[0,1,2],5,6],
"dank": [[0,3]],
"autocompleterlistup": [0],
"zugangsschlüssel": [1],
"tm-ordner": [7],
"licenc": [0],
"zuständ": [[2,3]],
"wo": [3,7,[0,6]],
"gar": [0],
"entsprechenden": [[0,6,7],[2,3,4]],
"fehlerhaft": [[0,2]],
"abgeschlossen": [7],
"omegat.project.bak": [2,6],
"repo_for_omegat_team_project": [2],
"exe-datei": [2],
"längeren": [0],
"linux-benutz": [7],
"aufnehmen": [[2,3]],
"projectaccessexporttmmenuitem": [0],
"absatzbegrenzung": [5,[1,4]],
"licens": [2,0],
"org": [2],
"umfasst": [6],
"filtern": [[2,3],[4,7]],
"überal": [7],
"sehr": [[3,7],[0,4]],
"distribut": [2,7],
"filtert": [7],
"po-format": [2,[1,5]],
"ort": [2,0],
"endeffekt": [0],
"jeder": [0,[1,4,7],6],
"jedem": [7,[0,1]],
"jeden": [[1,2,3,5,8]],
"farbschema": [[1,7]],
"passender": [6],
"alphabetischen": [0],
"dauern": [[0,1,3]],
"speziellen": [[2,6]],
"entspricht": [7,[0,1,2],[4,5,8]],
"passenden": [[0,1]],
"xx": [0],
"sourc": [2,7,6,4,5,[0,3]],
"software": [8],
"enden": [0],
"möglichst": [8],
"endet": [0,[2,4]],
"segmentierung": [0,7,[1,3],[4,6,8]],
"fußzeilen": [0],
"type": [2,[0,6]],
"formatspezifisch": [[1,2]],
"suchbegriff": [7,0],
"problem": [1,[2,4],3,0,[5,6,7]],
"vorliegen": [1],
"optionsautocompletehistorypredictionmenuitem": [0],
"stattdessen": [[0,1],[2,5]],
"projectaccesssourcemenuitem": [0],
"beliebigen": [[0,4],7,[2,5]],
"yy": [0],
"phase": [2],
"method": [2,7],
"einig": [0,2,1,[3,5,6,7],4],
"leichter": [[3,4]],
"abgeschnitten": [0],
"ausdrücke": [8],
"anfügen": [0],
"push": [2],
"versehentlich": [[0,1]],
"readme_tr.txt": [2],
"penalti": [6],
"eingeschaltet": [[1,4]],
"zs": [2],
"anwendungsfäll": [0],
"zu": [2,0,7,3,1,5,4,6,8],
"farbschemen": [1],
"oracle-dokument": [0],
"verlust": [2],
"geladen": [2,6,[4,7]],
"utf8": [0,[4,7]],
"aktiven": [4,7,[1,5],6],
"einfg": [5],
"tools": [8],
"umschlossen": [0],
"betreffen": [2],
"einhergeht": [7],
"vermieden": [7],
"power": [0],
"bezieht": [0],
"beigefügt": [2],
"überprüfen": [3,2],
"übersetzungsfeld": [5],
"darf": [[1,4,5]],
"context_menu": [0],
"limitieren": [7],
"speicherplatz": [2],
"quellen": [[0,1,2]],
"editsearchdictionarymenuitem": [0],
"tag-valid": [2],
"ovr": [5],
"anzeigen": [0,4,1,[2,5],7,6],
"help": [2,0],
"täglichen": [2],
"unterstützen": [2,[1,3,8]],
"kompatibilitätsproblemen": [0],
"speicherzuweisung": [2],
"projektbezogen": [2,5],
"typografisch": [[4,7]],
"danach": [0,1,3],
"repositori": [2,6,4,5],
"minimum": [0],
"optimieren": [[0,5,7]],
"lowercasemenuitem": [0],
"tabell": [0,1,4,5],
"ggf": [2,[1,5]],
"unverschlüsselt": [0],
"standard-font-fallback": [4],
"autocompleterconfirmwithoutclos": [0],
"gui-standardmodus": [2],
"separ": [2,[0,1,7],5],
"registriert": [2,[0,1]],
"beibehalten": [0,2],
"filepath": [1,0],
"gewünschten": [2,0],
"solcher": [[2,6]],
"aufbewahrung": [7],
"angepasst": [[0,2,6]],
"ja-jp": [2],
"wiederholungen": [4,[0,7]],
"ihre": [8],
"office-dateien": [3],
"solchen": [2,[1,3,4]],
"hinzufügen": [0,7,3,4,[1,2,6],5],
"ereigni": [0],
"einem": [0,2,[3,6,7],[1,4],5],
"einen": [[0,2],7,1,4,3,5,6,8],
"dass": [0,2,7,3,[5,6],4,1,8],
"konflikt": [2,0],
"ohnehin": [7],
"zähler": [7],
"einer": [0,7,2,1,[3,4],5,6],
"gestartet": [7,1,2,[0,4]],
"zählen": [0],
"regulär": [0,7,1,2],
"protokolliert": [6],
"referenzdateien": [6,[3,7]],
"liefert": [7],
"line": [2],
"link": [0,5,[1,3]],
"servern": [1],
"hero": [0],
"lini": [2],
"liefern": [7],
"rechtschreibprüfungsdateien": [2,1],
"mitteilen": [[2,3]],
"git": [2,6],
"projektsprachen": [4],
"buch": [3],
"anhänge": [8],
"dollarzeichen": [0],
"fließen": [0],
"stammverzeichni": [[0,2]],
"xx-yy": [0],
"passend": [6,[1,4]],
"eingetragen": [4,7,[1,3]],
"will": [2],
"herzlich": [3],
"englisch-japanisch": [2],
"bereich": [0,5,3],
"mauszeig": [[4,5],1],
"follow": [0],
"durchsucht": [1],
"gewährt": [2],
"z.b": [0,7,2,1,4,[5,6],3],
"restlichen": [[0,2,4]],
"targetlang": [0],
"diskrepanzen": [4],
"verwendeten": [[0,7],4],
"optionssetupfilefiltersmenuitem": [0],
"öffnend": [0],
"altgraph": [0],
"vorhandenen": [5,2,1],
"einfachheit": [2],
"stats-typ": [2],
"erwägen": [3],
"wichtigsten": [3],
"sollen": [7,6,1,2,[0,3]],
"verbrachten": [3],
"entfernt": [7,[0,1]],
"englischen": [2,0],
"erinnern": [6,[2,3]],
"xml": [2,0],
"seiner": [0,2,[5,6]],
"abdocken": [5,1],
"darstellungsweis": [1,[4,5]],
"seinen": [[6,7]],
"beginn": [0,1],
"seinem": [6],
"höchste": [5],
"lassen": [2,1,[3,5],[0,7,8]],
"übernehmen": [1],
"entfernen": [7,0,2,[1,6],[3,4]],
"neutral": [0],
"optional": [0],
"proxyserv": [2],
"korrigieren": [[1,2]],
"sekunden": [1],
"geleert": [4],
"starker": [0],
"teamfunktionalität": [3],
"xdg-open": [0],
"senden": [2,1],
"erkannten": [0],
"omegat-fenst": [5,[0,4]],
"befor": [2],
"ausdrück": [0,7,1,2],
"aufweisen": [7,0],
"sendet": [2,1],
"gtk-look-and-feel": [1],
"wörterbuchbegriff": [6],
"tar.bz": [6],
"klicken": [7,3,1,[0,4],5,2],
"generischen": [1,[0,2]],
"späteren": [[2,6]],
"fixiert": [5],
"shebang": [0],
"manipulieren": [4],
"ignorieren": [0,1,[4,6]],
"eintreten": [2],
"translation-memory-ordn": [6],
"folienmast": [0],
"editorskipprevtoken": [0],
"hinterlegt": [4],
"hinzugefügt": [7,[0,1,2],[4,6]],
"dazu": [2,0,7,[4,5]],
"angezeigten": [4,7],
"unterstrichenen": [4],
"großen": [[0,3,7]],
"kontext": [5,4],
"füllen": [[1,2,6]],
"bearbeitet": [[0,7],5],
"zeitstempel": [[0,2,6]],
"gnu": [2,8],
"währungen": [7],
"kunden": [2],
"einrichtung": [2],
"bearbeiten": [7,5,0,3,[1,2,4],[6,8]],
"suzum": [1],
"zieltext": [4,[1,7],[0,5]],
"target.txt": [[0,1]],
"temurin": [2],
"omegat-teamprojekt": [2],
"nummeriert": [5,0],
"vertraut": [2],
"standard": [0,[1,2,3,7]],
"d\'espac": [2],
"anfänglichen": [7],
"stdout": [0,2],
"traduct": [5],
"tastenkombin": [[0,4]],
"tastatur": [[0,5]],
"java-design": [1],
"aufgaben": [2,7],
"runden": [[0,1]],
"zeichenkombinationen": [0],
"sätze": [0,3],
"installieren": [2,[1,3],[0,6,8]],
"optionen": [7,[0,2],[3,4],[1,5,8]],
"abgerufen": [[1,4]],
"nameon": [0],
"standard-darstellungsweis": [1],
"committen": [2,[0,4]],
"kontroll": [2],
"normalen": [0,2],
"kaffe": [3],
"gotonextnotemenuitem": [0],
"verlassen": [4,1,[3,6]],
"erfahren": [3,0],
"newentri": [7],
"list": [0,1,2,7,3,4,6],
"autocompleterprevview": [0],
"wird": [2,0,7,1,6,4,5,3,8],
"quelldateinamensmust": [0],
"letzt": [0,[4,7]],
"installationsskript": [2],
"synchronis": [2],
"java-eigenschaften": [0],
"wiederherzustellen": [[5,6]],
"regional": [2],
"verknüpfung": [2,0],
"erzeugt": [[0,2],[1,3,5,7]],
"html-dateien": [[0,2,3]],
"projectcommittargetfil": [0],
"formate": [8],
"erscheinen": [[1,2,3,7]],
"docx-dokumenten": [7],
"po4a": [2],
"japonai": [7],
"omegat.org": [2],
"unicode-block": [0],
"geschweift": [0],
"beschädigen": [2],
"zuordnungsfunkt": [2],
"reservetext": [0],
"verzeichnisstruktur": [6],
"verschiebt": [7],
"gekennzeichnet": [1,0],
"mitzuteilen": [2],
"niedrig": [[5,7]],
"maxprogram": [2],
"definiert": [0,2,1,[4,6,7]],
"maschinelle": [8],
"pdf": [0,[2,4]],
"losgelassen": [0],
"hoffentlich": [3],
"weichem": [0],
"aufklappmenü": [0,1],
"autocompletertabledown": [0],
"ordnerstruktur": [7],
"wiederzuverwenden": [[2,7]],
"editornextsegmentnottab": [0],
"toolsshowstatisticsmatchesmenuitem": [0],
"viewdisplaymodificationinfononeradiobuttonmenuitem": [0],
"festplatt": [2,7],
"einrichten": [2,6,[0,7,8]],
"tabellenverzeichnis": [8],
"erzwungenen": [6],
"leerräum": [0,4],
"per": [6],
"write": [0],
"großschreibung": [[0,4]],
"bringen": [2],
"gpl-version": [0],
"folgt": [2,0,3,[4,5]],
"project_save.tmx.bak": [[2,6]],
"period": [0],
"ausgegeben": [2],
"zunicht": [3],
"dieselb": [0,[2,7]],
"obwohl": [0,2],
"standardalgorithmus": [7],
"typisch": [2],
"projectaccesswriteableglossarymenuitem": [0],
"prozedur": [2],
"vergleichenden": [7],
"befinden": [[0,1,2,5,7],[4,6]],
"application_shutdown": [7],
"befindet": [[4,7],2,0,[1,3]],
"autocompletertablelastinrow": [0],
"gui": [2,7],
"definierten": [[0,1,2,5,7]],
"regexp": [0],
"grundsätz": [[3,5]],
"startoptionen": [[1,2,4]],
"glossardateiendung": [5],
"sentencecasemenuitem": [0],
"gut": [[0,2,3],8],
"instanz": [2,0],
"datenverlust": [[2,3]],
"url-suchbeispiel": [1],
"projektmitarbeitern": [2],
"xhtml-dateien": [0],
"articl": [0],
"vorhersag": [1],
"eingabefeld": [[1,3,7]],
"rechts-nach-links-markierung": [[0,4]],
"verworfen": [7],
"editorcontextmenu": [0],
"einzurichten": [2],
"parameterdateien": [2],
"wohlgemerkt": [2],
"ungefähren": [7],
"optionssentsegmenuitem": [0],
"robust": [2],
"bought": [0],
"zeitpunkt": [2,[1,4,6,7]],
"einmaligen": [4],
"endnoten": [0],
"ereignisgesteuert": [7],
"auswirkt": [7],
"optionsaccessconfigdirmenuitem": [0],
"charact": [2],
"framework": [2],
"bleiben": [6,[2,3,7]],
"test.html": [2],
"namen": [0,2,1,[3,6],7,5],
"wahl": [[0,2]],
"php": [0],
"xxx": [6],
"übersprungen": [6],
"smalltalk": [7],
"satzsegmentierung": [7,0],
"wörterbuchordn": [0],
"oben": [7,[0,2],1,[4,5],3],
"explizit": [2],
"registrierten": [[2,7]],
"notfallmaßnahmen": [2],
"regex-such": [[0,3]],
"pseudotranslatetmx": [2],
"unabl": [5],
"gegebenenfal": [[3,7]],
"flüssigeren": [3],
"löschen": [2,6,[0,7],4],
"aktion": [2,[5,7],[1,3,4]],
"abzugleichen": [0],
"fensterbereichen": [[4,5]],
"wortzeichen": [0],
"fungiert": [0],
"ähnlicher": [3],
"targetlanguagecod": [0],
"funktioniert": [2,[0,4,5,7]],
"ähnlichem": [2],
"vorteil": [7],
"editorprevsegmentnottab": [0],
"downloadseit": [2],
"schwer": [2],
"bidirect": [4],
"auftreten": [7],
"zugreifen": [0,3,7,6,[1,4]],
"eindeutigen": [7],
"schlecht": [7],
"manuell": [[2,7],[0,4]],
"schreibmodus": [5],
"kontextmenüpriorität": [1],
"textinhalt": [[1,7]],
"websit": [2],
"inspiriert": [7],
"grobe": [0],
"voll": [7,2],
"abwechseln": [2],
"autotext-optionen": [1],
"gemäß": [7,1],
"tmx-konform": [2],
"design": [1],
"land": [2],
"lang": [0,1],
"ihnen": [2,[4,7],3,0,[1,8],6],
"untermenü": [[2,7]],
"begriff": [[3,5],1,4,0,7],
"dateisystem": [2],
"beachten": [7,[0,2],3],
"empfiehlt": [2],
"einfach": [0,3,2,[1,7],6],
"optionalen": [0,2],
"projektereigniss": [2],
"projectnam": [0],
"sprachen": [7,1,2,6,3,0],
"fensterbereich-widget": [5],
"unübersetzt": [0,4,[1,5,7],[3,6],2],
"versieht": [2],
"omegat.project.yyyymmddhhmm.bak": [2],
"obig": [2],
"zieltextstatus": [0],
"autovervollständigungsoptionen": [5],
"hinter": [0,5],
"installdist": [2],
"projekte": [8],
"a-z": [0],
"reihenfolg": [[0,7],6],
"markiert": [4,7,0,1,[5,6]],
"gegeben": [1],
"gotonextxenforcedmenuitem": [0],
"editordeleteprevtoken": [0],
"onlin": [2],
"statusleiste": [8],
"passwort": [2,1],
"verbindung": [2,5],
"unicode-basiert": [0],
"anhäng": [0,[3,6]],
"fuzzy-match": [4,0,1],
"angesprochen": [7],
"verbreitet": [2],
"kompatibilität": [0],
"unicode-kategori": [0],
"office-form": [2],
"umgekehrten": [0],
"javascript": [7],
"ersetzungsvorgang": [7],
"toolkit": [2],
"join.html": [0],
"muss": [0,2,[1,4,6],[3,7]],
"wann": [7,[1,4]],
"anmeldenam": [0],
"aufbewahrt": [[0,3,7]],
"segmentierungsregel": [3],
"erläuterungen": [7,1],
"unicode-zeichensatz": [0],
"proxy-serv": [1],
"beeinflussen": [2],
"omegat.kaptn": [2],
"zufrieden": [[2,7]],
"projekt-menü": [[0,8]],
"pop": [0],
"referenzen": [0,7],
"sonderbedeutung": [0,7],
"wieder": [[2,7],[0,1,4,6]],
"rechtschreibfehl": [7,[1,4]],
"omegat-verknüpfungen": [2],
"generel": [5,[0,1]],
"zwölf": [0],
"schlagen": [7],
"würden": [0],
"computerentwicklung": [2],
"omegat-objektmodel": [7],
"konfiguriert": [[1,4,5]],
"kostenlosen": [2],
"kopi": [2,[4,6]],
"strukturiert": [2],
"außerhalb": [0,[3,5,6]],
"zieldoku": [[0,2,4]],
"googl": [1],
"dokumentiert": [2],
"kostenloses": [8],
"beginnt": [0,4],
"xhtml-filter": [0],
"gotoeditorpanelmenuitem": [0],
"vorn": [3],
"angeboten": [[0,1,3]],
"viewmarkfontfallbackcheckboxmenuitem": [0],
"had": [0],
"align": [7,4,3],
"insertcharsrlm": [0],
"ausschlussmust": [2],
"textkombinationen": [1],
"sourceforg": [2,0],
"exportiert": [[2,4],7,[0,3,6]],
"han": [0],
"äußerst": [[0,3]],
"statusmeldung": [5],
"semeru-runtim": [2],
"hat": [0,7,4,[1,2,5],3],
"einmal": [2,4],
"hintergrund": [[5,6]],
"pfad-zur-omegat-projektdatei": [2],
"unterschiedlichen": [3],
"beschreibbaren": [[2,4,5]],
"last": [7],
"zugehörig": [7,[0,4]],
"editmultipledefault": [0],
"adapt": [[3,7]],
"segmentbegrenzung": [3],
"mozilla": [[0,2]],
"abschnitten": [3],
"editfindinprojectmenuitem": [0],
"pro": [6,1,4,[0,7]],
"benachbart": [7],
"warn": [2],
"diensten": [1],
"literarisch": [0],
"aktuellst": [[2,7]],
"ersetzungsfenst": [7],
"tsv-datei": [0],
"updates": [8],
"quellcontain": [0],
"umbruchregel": [1,0],
"bedingungslo": [6],
"regeltyp": [0],
"projekten": [0,2,[5,7],1],
"kenntniss": [2],
"dekor": [3],
"bedeutet": [0,5,2],
"duckduckgo.com": [1],
"kompatibel": [2,[0,1,7]],
"fließkommazahlen": [1],
"zeigt": [4,7,[1,5],2,3,0],
"schreiben": [[2,7],4],
"vertrauen": [1,7],
"aufgab": [2],
"kombiniert": [[0,7]],
"angab": [2,3],
"colour": [[1,7]],
"lauf": [2],
"probieren": [7],
"umbruchstellen": [1],
"größer": [[1,5]],
"dateiinhalten": [7],
"kurzzeitig": [5],
"benutzerdefiniert": [0,[1,2],4,3,6],
"referenzübersetzungen": [2],
"tipp": [2],
"tastenbindungsereigniss": [7],
"kanji": [0],
"benannt": [3],
"suchfeld": [7,1],
"anpassung": [0,7,[2,4,5]],
"program": [[0,2]],
"hostnam": [0],
"python3": [0],
"beziehen": [[1,7]],
"kontextmenü": [5,1,[0,3,6,7]],
"präsentationsnotizen": [0],
"her": [2,[1,4,5]],
"innerhalb": [0,3,[2,4,5]],
"berücksichtigt": [1,[0,2],[4,6,7]],
"schritten": [0,7],
"erstellung": [2,[4,6],3],
"zugang": [4],
"remote-ordn": [2],
"iraq": [0],
"dossier": [5],
"gefärbt": [[5,7]],
"nicht": [0,2,7,3,1,4,[5,6]],
"ascii-fremden": [0],
"gefüllten": [6,2],
"angeordnet": [[1,7],5],
"brunt": [0],
"prozentualen": [5],
"aufzuheben": [0],
"überschreibmodus": [[0,4]],
"denken": [[2,3]],
"rechtschreibwörterbüch": [3,[0,1]],
"inhalten": [2,[0,3]],
"anhaltspunkt": [1],
"ordnungsgemäß": [2],
"doc-license.txt": [0],
"gemacht": [3,4,[2,5]],
"thema": [0],
"escapezeichen": [2],
"theme": [[1,7]],
"チューリッヒ": [1],
"markennamen": [5],
"einsprachigen": [7],
"sprachpaar": [[0,1,2]],
"standardmust": [0],
"editor": [7,5,[0,3],1,4,[6,8]],
"engl": [3,[2,7]],
"pseudotranslatetyp": [2],
"kodierungsdeklar": [0],
"visuellen": [0],
"properties-datei": [2],
"kommentare": [8],
"decken": [0],
"befehlszeil": [2,0,1,7],
"startseit": [2],
"gesetzt": [0,7,4],
"listet": [[1,2,5]],
"gewechselt": [5],
"projectclosemenuitem": [0],
"gelten": [2,7,1],
"hin": [2,5,7],
"viewmarknonuniquesegmentscheckboxmenuitem": [0],
"listen": [0,2],
"plattformabhängig": [2],
"vergleichsmodus": [7],
"radiobutton": [7],
"adaptieren": [7,3],
"nachbearbeitungsbefehl": [7,[0,1]],
"maschinellen": [[1,5],4],
"hoch": [4],
"freien": [2],
"zweistellig": [[3,7]],
"durch": [[0,3],2,[4,5,7],1],
"zweimal": [[3,4]],
"remote-vers": [2],
"findinprojectreuselastwindow": [0],
"links-nach-rechts-einbettung": [0,4],
"readme.txt": [2,0],
"gerad": [7,5,2],
"früher": [3],
"languagetool": [4,1,[7,8]],
"aufzurufen": [4,0],
"source.txt": [[0,1]],
"falschmeldungen": [1],
"files.s": [7],
"synchronisierung": [[2,3]],
"linux-systemen": [1],
"ini-datei-beispiel": [2],
"svn-client": [6],
"readme-dateien": [0],
"vorstellen": [0],
"konfigurieren": [1],
"desto": [7],
"segmentmark": [5],
"prüfer": [2],
"sicherungskopi": [2,6,[1,7]],
"kodierung": [0,[7,8]],
"currseg": [7],
"immer": [[0,2],1,7,[4,6],[3,5]],
"prüfen": [1,3,0,7,[2,5,8]],
"unterschied": [1],
"point": [0],
"fuzzy-match-zahlenkonvertierung": [1],
"general": [2,8],
"sprachcod": [7,2,3,0],
"xhtml-dateien-filt": [0],
"kehren": [3,[2,7]],
"vertrauenswürdigen": [0],
"vermittelt": [7],
"stärken": [7],
"absicht": [0],
"autocompletertrigg": [0],
"komplexen": [7],
"repository-url": [2],
"attribut": [0],
"korrekturarbeiten": [2],
"fortfahren": [[2,6,7]],
"projektkonfigurationsdateien": [2],
"kapitel": [2,[3,7],0,4],
"folgen": [2,[3,7]],
"proxy-host-portnumm": [2],
"acquiert": [1],
"freigestellt": [0],
"auszurichten": [7],
"teilweis": [[2,3]],
"wörterbüchern": [1,[3,4,5,7]],
"beschreibungen": [4],
"dhttp.proxyhost": [2],
"aktuellen": [7,2,[0,5],4,[3,6],1],
"aktuellem": [5],
"plugins": [8],
"eingestellt": [3],
"teamprojektmanag": [4],
"vermutlich": [0],
"variiert": [[0,5]],
"angegebenen": [2,[0,7],[1,4]],
"vergangenheit": [2],
"editorprevseg": [0],
"szenario": [2],
"java-paramet": [2],
"texthervorhebung": [0],
"konsonanten": [0],
"a-za-z0": [0],
"unterscheidung": [3],
"you": [0],
"dokumenteigenschaften": [0],
"bevor": [2,[0,1,6,7],3],
"modifik": [0,7],
"werkzeug": [2],
"www.apertium.org": [1],
"jetzigen": [7],
"wirkung": [0],
"modifikatortasten": [0,4],
"hinzuzufügen": [2,6,[3,5],0,1],
"kompatibilitätsproblem": [0],
"höchsten": [[1,5]],
"stell": [0,[3,5,6,7]],
"einstellen": [[0,1,4]],
"project_save.tmx.tmp": [2],
"tags": [8],
"nativen": [1],
"configur": [5,2],
"nativ": [2],
"angehängt": [7],
"gui-prozedur": [2],
"befehl": [7,1,2,4,0],
"omegat-javadoc": [7],
"beibehaltung": [0],
"mega": [0],
"zurich": [1],
"空白文字": [2],
"ergebni": [7,0,3],
"quellordn": [2,6,0],
"optionsworkflowmenuitem": [0],
"omegat-team": [[0,2]],
"how": [2],
"editierbar": [5],
"releas": [2,0],
"po-filt": [1],
"zielsatz": [3],
"leerzeilen": [0],
"translation-memory-tool": [0],
"ziffern": [0],
"zeilenumbrüchen": [0],
"öffnenden": [0],
"wandelt": [[0,3]],
"segmentnumm": [[0,4]],
"identifizierung": [0],
"strich": [0,1],
"relevanten": [2],
"wodurch": [[0,7]],
"grundlegenden": [2],
"standard-tag": [7,3],
"dictroot": [0],
"tag-problem": [2,1,[3,4]],
"sogar": [[0,2,4,5]],
"ausrichtungen": [0],
"vorsichtig": [2,7],
"autovervollständiger-ansicht": [0],
"synchronisier": [5],
"gängige": [2],
"zeichensatz": [0],
"fenster": [7,4,1,5,[0,3],[6,8]],
"notwendig": [7],
"fuzzy-matches-bereich": [5],
"beim": [7,4,1,2,0,3,5],
"attributwert": [0],
"resultierenden": [7],
"besten": [1,[5,7],[2,3,6]],
"mehr": [[0,2,7],3,[1,5]],
"subdir": [2],
"programmereigniss": [0],
"herunterladbaren": [[1,3]],
"pfeilschaltfläch": [7],
"handelt": [[1,2,7],5],
"ebenfal": [2,0,1],
"irgendetwa": [[0,2]],
"zeicheneingabesystem": [4,1],
"bereitstellt": [1],
"benutzen": [[0,1,4]],
"gleichen": [2,[0,7]],
"zeichentabell": [1],
"konnten": [3],
"ausnahmen": [7,1,2],
"vorwärt": [4],
"autocompletertableleft": [0],
"aber": [0,7,2,3,[1,6],4,5],
"allein": [0],
"statistikberichten": [7],
"spalt": [7,0,4],
"hatten": [3],
"behalten": [7],
"dahint": [0],
"gehen": [2,0,[3,5]],
"absteigend": [0],
"proxy-host-ip-adress": [2],
"forward-backward": [7],
"beschrieben": [0,5],
"pfad": [2,0,[1,5],7],
"aufgehoben": [0],
"türkisfarben": [4],
"beid": [7,[0,1],2],
"hinausgehen": [3],
"ausgabe": [8],
"steht": [0,[2,3,4]],
"editorlastseg": [0],
"file-source-encod": [0],
"großbuchstaben": [0,[2,4]],
"some": [2],
"kennung": [3],
"widerspiegeln": [[2,3]],
"stein": [6],
"vorgeschrieben": [7],
"generiert": [0],
"konnt": [2],
"übereinstimmenden": [1],
"remote-serv": [[2,6]],
"anderen": [2,0,1,7,6,5,[3,4],8],
"textsuch": [[3,7],[0,4]],
"häufig": [0,[3,7],[2,4]],
"linux-tastenkürzel": [0],
"passen": [7,5],
"erinnerungen": [3],
"umgebungsvariablen": [0],
"alpha": [2],
"links-nach-rechts-text": [0],
"大学院博士課程修了": [1],
"getrennt": [[0,3],[2,7]],
"just": [0],
"gemeinsam": [2,7,[3,6],[0,5]],
"zielsprach": [2,[0,7],6,1,3],
"doppelpunkt": [0],
"verfügung": [2,[1,5,7]],
"editexportselectionmenuitem": [0],
"soll": [0,2,1,7,[3,4,5]],
"omegat-upgrad": [1],
"höher": [1,[0,2,7]],
"pluszeichen": [0],
"editorfunkt": [0],
"home": [[0,2]],
"macos-dienst": [2],
"bewerkstelligen": [2],
"disable-location-sav": [2],
"mehrsprachigen": [0],
"varianten": [0],
"bildschirm": [[0,3]],
"aktuell": [4,[2,7],0,5,6],
"projectaccesstargetmenuitem": [0],
"segmentidentifik": [0],
"iana": [0],
"schreibrecht": [7],
"synchronisieren": [2,6],
"leeren": [2,[1,3,4]],
"dokumentelement": [0],
"aufgeführten": [[2,6]],
"zuordnungsparametern": [2],
"glossar-einstellungen": [5],
"strikt": [2],
"remote-ort": [2],
"aligndir": [2,7],
"system-host-nam": [0],
"suchparametern": [1],
"mymemory.translated.net": [1],
"vorkehrungen": [2],
"zweit": [[0,2,4,5,7]],
"glyphen": [4],
"creat": [[2,7]],
"omegat-erweiterungs-plugin": [0],
"python": [7],
"aufzunehmen": [[2,5]],
"segmentiert": [0,[3,7]],
"suchausdruck": [0],
"office-applikationen": [7],
"zu-menü": [[0,8]],
"tabulatorzeichen": [0],
"benutzereinstellungen": [0],
"getestet": [2],
"omegat-projekt-quellordn": [2],
"üblichen": [5],
"hervorgehoben": [4,[1,7],5],
"projektpaket": [4],
"benutzergruppen": [3],
"offiziell": [0],
"häufigsten": [[0,7]],
"match-prozentsätz": [6],
"algorithmen": [7],
"tag-tooltip": [1],
"vielfach": [2],
"zirkumflex": [0],
"läuft": [0],
"file": [2,7,[0,5]],
"wortgrupp": [7],
"mehrzellig": [7],
"beispielzuordnungen": [2],
"klein": [4,6,0],
"hauptfensterbereichen": [1],
"meng": [2],
"erwähnt": [0],
"zuordnungen": [2],
"bestätigungstast": [1],
"instanzen": [0,2],
"ersetzt": [7,[0,4],1],
"überbrücken": [2,[0,6]],
"zweibuchstabig": [2],
"umschalten": [7,0,1],
"invoke-item": [0],
"med-paket": [4],
"tausendertrennzeichen": [0],
"farben": [[1,4]],
"projektstruktur": [[2,3]],
"konto": [2],
"cjk-zeichen": [7],
"source-pattern": [2],
"übersetzungsprojekt": [3,6,[1,7]],
"variablenlist": [[0,1,7]],
"ausgeführt": [[2,7],[1,4],0],
"benutzernamen": [2],
"obigen": [[0,1],[4,8]],
"geachtet": [0],
"sorg": [[2,3]],
"workflow": [3,5,8],
"autocompletertablepagedown": [0],
"befehlszeilenbasierten": [7],
"lesbar": [0],
"bearbeiteten": [5],
"benutz": [0,1,[2,4,5,7]],
"ursprünglich": [[0,1,2,7]],
"problematisch": [3],
"probleme": [8],
"sichtbar": [0,6],
"angezeigt": [7,5,1,0,[2,4],[3,6]],
"dateimanag": [4,[2,6,7]],
"task": [2],
"endgültig": [6],
"xliff": [2,0],
"tast": [0,5,[1,4]],
"true": [0],
"header": [0],
"tag-attributwert": [0],
"menüpunkten": [[0,4]],
"groovi": [7],
"suchergebniss": [7],
"identifik": [4,0],
"deren": [0,[2,7]],
"best": [7,2],
"suchtext": [7],
"wörterbücher": [1,6,[3,5],[0,4],[7,8],2],
"tastenkürzel-definitionsdatei": [0],
"befehlsparametern": [1],
"pdf-dokument": [2],
"laufwerken": [7],
"menü": [4,5,7,3,[0,1]],
"autovervollständiger": [8],
"abhängig": [0,4],
"bestätigungsfenst": [7],
"bewirkt": [0,2,5],
"zweck": [[0,2,8]],
"verfügbaren": [[2,7],[0,5]],
"zeilenvorschub": [0],
"segmenten": [7,4,1,3,6],
"letzten": [0,4,[2,7],5],
"eigen": [2,[0,5,7]],
"en-nach-fr-projekt": [2],
"würde": [[0,6],[1,2]],
"master": [2],
"tmx-level": [7],
"logdatei": [4],
"schaltfläch": [7,3,0,1],
"eindruck": [7],
"informieren": [2],
"pdf-datei": [2],
"tastenbindung": [7],
"beispielsweis": [2],
"dalloway": [1],
"rubi": [7],
"resource-bundl": [2],
"eigenen": [7,2,0],
"yyyi": [2],
"external_command": [6],
"websuchvorgängen": [1],
"stoßen": [3],
"editorselectal": [0],
"globale": [8],
"existiert": [2],
"sowi": [0,2,[3,4]],
"ersetzungen": [[4,7]],
"runner": [7,0],
"verlaufsvervollständigung": [[0,1]],
"pfeilnavig": [3],
"hierbei": [7],
"benennen": [2,3],
"anzeigt": [[1,4]],
"beendigung": [7],
"omegat-default": [2],
"aktiviert": [4,0,1,5,7,2],
"dekorierten": [3],
"anweisungstext": [0],
"user.languag": [2],
"regex": [0],
"meta": [0],
"suchvorgängen": [7],
"programm": [2,3,[0,1,4,5,7]],
"frei": [[3,5]],
"regex-beispiele": [8],
"ressourcen": [3,7,6,0],
"systemweiten": [0],
"referenz-tm": [7],
"global": [7,0,1,4,[3,5]],
"später": [3,[0,2,5,6,7]],
"racin": [5],
"fensterrand": [7],
"regel": [0,2,7,4],
"neustarten": [6],
"zeichnungen": [0],
"gebrauch": [[2,7]],
"sprengen": [0],
"hinzu": [[3,7],[0,2],6],
"lesen": [2,[0,7],3],
"dateien": [2,7,3,6,0,4,5,1,8],
"ruft": [2],
"brückensprach": [2],
"gehandhabt": [3],
"ibm": [[1,2]],
"tm-match": [1,5],
"einbeziehen": [7,1,[2,4,6]],
"geführt": [2],
"unterstützten": [2,6,[0,7]],
"parsewis": [7],
"remote-desktop-sitzungen": [2],
"zusammenfassen": [0],
"nebensätz": [0],
"ergebniss": [7,[3,4],[0,1,2,5]],
"bedeutung": [[0,2,5]],
"benachrichtigungen": [5],
"hervor": [7],
"variieren": [2],
"erklärung": [0],
"worttrennung": [7],
"omegat-cod": [2],
"befehlszeilenschnittstell": [2],
"office-suite-dateien": [2],
"währenddessen": [2],
"idx": [6],
"tm-matches": [8],
"befindlichen": [[2,5]],
"erhalten": [2,[1,5,7],[0,3,4]],
"jede": [0,2,6,4,3,1,[5,7]],
"erstellten": [[3,4,6,7]],
"faustregel": [7],
"autocompleterconfirmandclos": [0],
"interakt": [[2,7]],
"symbolen": [0],
"hauptdatei": [2],
"projectaccesscurrentsourcedocumentmenuitem": [0],
"linux": [0,2,4,5,3],
"ausschneiden": [5],
"linux-install.sh": [2],
"projektstatistiken": [2],
"leertast": [0],
"file.txt": [2],
"openxliff": [2],
"rechtsklick": [5],
"variablenmust": [1],
"ifo": [6],
"aktivierungsstatus": [0],
"excit": [0],
"modifikatoren": [[0,3]],
"wiederkehrenden": [7],
"erzeugen": [6,[0,2,4]],
"optionsmtautofetchcheckboxmenuitem": [0],
"zweistelligen": [2,[3,7]],
"kehrt": [7],
"zeilenumbrüch": [0],
"xx.docx": [0],
"erkennen": [3,6],
"ige": [6],
"wert-dateien": [7],
"kommunikationsproblem": [5],
"textdatei": [[0,4,7],1],
"rund": [0,1],
"dokument": [0,2,7,3,5,[4,8]],
"negativ": [7],
"applikationsordn": [0,[1,2,7]],
"modernen": [2],
"textdaten": [7],
"editorshortcuts.properti": [0],
"überschreiben": [5,6,[2,4]],
"aufzuteilen": [7],
"französisch": [[1,2]],
"dekorativ": [3],
"maschinel": [[1,4],[0,5]],
"ihn": [0,[2,5],3,[4,7],6],
"ihm": [0],
"externen": [6,[1,3],[4,5,7]],
"ihr": [2,0,3,7,[1,6],5,4],
"tatsächlichen": [2],
"dateifilt": [0,7,1,2,4,3,6],
"projekteigenen": [4],
"verloren": [[2,4],5],
"diesem": [7,2,6,5,[0,3],[1,4]],
"tastenkürzeln": [3,0,[4,5,8]],
"diesen": [7,2,[3,6],0,[1,4]],
"zielsprachencod": [1],
"tmotherlangroot": [0],
"stoppt": [1],
"geeigneten": [2],
"fall": [2,0,[1,5,7]],
"viewmarknotedsegmentscheckboxmenuitem": [0],
"ältere": [2],
"dreistellig": [[3,7]],
"bietet": [0,[2,7],[3,5],1],
"https-protokol": [2],
"algorithmus": [7],
"projektcharakteristik": [1],
"aufgeführt": [[2,3]],
"gruppen": [[0,1,7]],
"editorfunktionen": [0],
"diese": [8],
"behandeln": [[1,7],2],
"dessen": [[3,6],[2,4]],
"exkludiert": [2],
"übersetzbaren": [7,0],
"ausführbar": [0],
"näher": [1],
"gesucht": [0],
"gotomatchsourceseg": [0],
"bieten": [0,[1,2]],
"unmittelbar": [0],
"behandelt": [0,[1,5],[6,7]],
"excel": [0],
"häufigst": [[2,5]],
"comma": [0],
"omegat-tag": [1,4],
"runn": [7],
"zieldateinamensmust": [0],
"wirken": [4],
"lesbarkeit": [1],
"dieses": [8],
"dieser": [0,[4,7],2,6,1,3,[5,8]],
"klassenbeispiele": [8],
"runt": [0],
"projektinstanzen": [2],
"stardict": [6],
"omegat.l4j.ini": [2],
"first": [5],
"span": [0],
"resultierend": [2],
"prefer": [0],
"fremdsprachig": [5],
"weiterzumachen": [3],
"space": [0,7,5],
"wurden": [2,[4,7],[1,5],[0,3]],
"struktur": [7,[0,6]],
"reguläre": [8],
"lesezeichen": [0],
"ドイツ": [7,1],
"modifikatortastendarstellung": [8,[0,4]],
"unveränderten": [3],
"ausstehend": [7],
"nachfolgenden": [0],
"ersetzungstext": [0],
"dateiendungen": [[0,2]],
"editselectfuzzy3menuitem": [0],
"zeichenweis": [0],
"ordnungszahlen": [0],
"übersetzungen": [5,6,1,3,4,[0,2],[7,8]],
"halten": [7,3],
"fals": [0,2],
"besucht": [4,6],
"project.projectfil": [7],
"aufzubewahren": [6],
"übersetzten": [7,3,0,4,[1,2],[5,6],8],
"macos-dateien": [0],
"parameterdatei": [0],
"empfunden": [0],
"variablennam": [0],
"zuletzt": [4,1,5,[0,2,6,7]],
"referenzglossaren": [0],
"reduzieren": [7],
"einschließlich": [0,[1,2],[4,5,6]],
"rechten": [[5,7],4],
"dienstprogrammen": [2],
"manuel": [7,[0,2,3,6],4],
"überspringen": [0],
"zugänglich": [7,[0,2,4,6]],
"shortcut": [7,2],
"public": [2,8],
"anzahl": [7,5,4,0,1,3],
"konfigurationsordn": [0,2,1,[3,4],7],
"tmx2sourc": [2,[0,6]],
"hilfe-menü": [[0,8]],
"erteilen": [2],
"zieldateinam": [0],
"orangen": [0],
"kanada": [2],
"eingegeben": [[0,1,3,5]],
"anstell": [7,[0,2]],
"synchronisiert": [2,7,[1,3,4,5]],
"gute": [8],
"plattformübergreifend": [2],
"dient": [[1,2],[0,6]],
"dhttp.proxyport": [2],
"installierten": [[1,3,5]],
"einführung": [[3,8],2],
"hauptteil": [7],
"zeichenklass": [0],
"computerproblem": [3],
"n-te": [7],
"originaldatei": [3],
"subrip": [2],
"authentifizierten": [1],
"genauer": [7],
"häufigkeit": [2],
"tastaturen": [0],
"übersetzen": [2,7,[0,3],[4,5,8]],
"tabulatorgetrennten": [0],
"textfilt": [0],
"sicherungsmedium": [2],
"vereinfacht": [2],
"aktivieren": [1,4,7,[0,6],[2,5]],
"versehen": [[0,2,4,6]],
"score": [1],
"umgebrochen": [0],
"geeignet": [7,2],
"gefolgt": [0,1,[2,3]],
"initialisieren": [2],
"wiederkehrend": [[2,7]],
"raw": [2],
"physisch": [2],
"schließt": [7,4],
"diagramm": [0],
"laufen": [0,2],
"kombin": [0,2],
"wünschen": [8],
"unten": [7,0,2,[1,6],[3,5]],
"spätere": [6],
"unter": [2,4,0,5,7,1,3,6,8],
"statusmeldungen": [0],
"beschließen": [3],
"führend": [0],
"unbeliev": [0],
"fachgebieten": [2],
"prioritäten": [1],
"überprüfung": [3],
"close": [7],
"egal": [0,[2,6]],
"abc": [0],
"linken": [7,[4,5]],
"bewegen": [[4,5],[0,1,7]],
"lizenzrechtlichen": [2],
"startmenü": [2],
"pos1": [0],
"betreten": [5,[0,1,3]],
"eingab": [2,4,1],
"gruppieren": [[1,5]],
"wobei": [[0,2],5,[1,3,6,7]],
"toolbar.groovi": [7],
"bequem": [7],
"quellsegmenten": [7],
"invertiert": [1],
"pluralspezifik": [0],
"zuverlässig": [6,2],
"vorgestellt": [3],
"glossareinträgen": [4],
"acht": [0],
"zugriff": [2,4,1,7,[0,3,8]],
"folienlayout": [0],
"iso": [[0,2]],
"ordnern": [7,[2,3],6],
"glossardatei": [5,0,7],
"satzend": [1],
"ist": [0,2,7,5,4,1,6,3,8],
"optionspreferencesmenuitem": [0],
"sprachordn": [0],
"zum": [0,4,2,7,1,5,6,3],
"post": [0],
"scripts-ordn": [7],
"zur": [2,[0,7],1,[3,4],5,6],
"glossary.txt": [[2,6],[0,4]],
"prozentsätz": [5],
"gegensatz": [0,1],
"versionskontrollsystem": [2],
"ländern": [1],
"projektdateien-fenst": [0],
"ausführung": [2,0],
"paket": [2],
"referenz": [[2,7],[0,3]],
"konvertierung": [2],
"add": [2],
"stehen": [1,2,[0,7]],
"endung": [0,2,[1,4,6,7]],
"jeweilig": [[0,7]],
"datensicherung": [2],
"übersetzungsarbeiten": [2],
"parsen": [7],
"rfe": [7],
"entry_activ": [7],
"optionsautocompleteshowautomaticallyitem": [0],
"gotoprevxautomenuitem": [0],
"deaktivieren": [7,4,[0,5],[1,2]],
"benutzersprach": [2],
"zugehörigen": [[0,1],[4,5,7]],
"platzhalterzeichen": [7,0],
"ermöglichen": [[2,8]],
"besser": [3,7,[0,2]],
"ishan": [0],
"pasta": [0],
"passt": [3],
"zusammen": [0],
"modifi": [7],
"zieldaten": [6],
"existierenden": [3],
"font-fallback-mechanismus": [4],
"terminologie-unterordn": [0],
"voraus": [2],
"projektkonfigur": [2],
"zieldatei": [0,4,7],
"überschüssig": [[0,1]],
"allgemein": [1,7,[0,2,8]],
"standardmethod": [5],
"absoluten": [0],
"proxy-login": [1,8],
"remote-dateien": [6],
"punkten": [0],
"umfassend": [2],
"targetlanguag": [[0,1]],
"zielordn": [0],
"falsch": [[2,4,7]],
"ssh-authentifizierung": [2],
"backup": [6],
"problemlo": [3],
"kodierungen": [0],
"folgend": [0,2,7,5,[1,3,4,6]],
"editselectfuzzyprevmenuitem": [0],
"stunden": [2],
"gui-applik": [2],
"üblich": [[2,4]],
"regulärem": [3],
"regulären": [0,7,[1,2]],
"tag-problemen": [4,2,1],
"git-client": [2],
"aktualisieren": [[2,7],6],
"reellen": [2],
"auszulösen": [7],
"algorithm": [4],
"ausnahmestell": [1],
"remote-ordnern": [7],
"autovervollständig": [0,1],
"einleitend": [0],
"abzurufen": [1],
"script": [7,0,1,[2,4]],
"japanisch": [2,1,0],
"system": [2,0,7,[4,5,6]],
"deklar": [0],
"spellcheck": [7],
"unnötig": [1],
"richtung": [0],
"zuweisen": [0,[1,4,7]],
"zeichenketten": [7,[0,3],2],
"neben": [7,0,5,4],
"zunächst": [2,[0,3]],
"local": [2,6],
"vorgegebenen": [7],
"einzelheiten": [[2,4],7,0,[1,3],6,5],
"wiederzugeben": [0],
"aspekt": [3],
"rle": [[0,4]],
"nach": [0,7,[1,2],[3,4,5],6],
"glossar-match": [5,[3,4],0],
"repo_for_all_omegat_team_project_sourc": [2],
"repräsentiert": [0,[1,2]],
"login-id": [1],
"oder": [0,7,2,[3,4],5,1,6],
"rlm": [0,4],
"java-resource-bundl": [0],
"effizi": [[0,3]],
"segmente": [8],
"weiterzugeben": [8],
"führt": [[0,2,4]],
"kombinationen": [0,7],
"angewiesen": [[2,6]],
"sucht": [1,2],
"wortgrenz": [0],
"einbettungen": [0],
"c-x": [0],
"gestaltung": [0],
"mode": [2,7],
"entwickl": [0],
"modi": [5],
"schlüssel": [7,2,1],
"seiten": [0,3],
"toolsshowstatisticsstandardmenuitem": [0],
"geöffneten": [4],
"zielbegriff": [4,[0,3,5]],
"navigationsbefehl": [5],
"bestimmung": [7],
"all": [2,7,0,1,4,[3,6],5],
"kompositionen": [0],
"statusleisteninformationen": [5],
"read": [0],
"eingefügt": [6,1,[2,4],[3,7],[0,5]],
"c.t": [0],
"alt": [0,2,4,[1,7]],
"beschreibt": [0,2],
"gewöhnt": [4],
"quellsprach": [0,1,[2,5,6,7]],
"match-prozentsatz": [[1,5]],
"schauen": [0],
"auffordert": [4],
"navigiert": [4],
"lesezeichenreferenzen": [0],
"wiederverwenden": [2,3,[1,4]],
"übereinstimmt": [0,3],
"wiederverwendet": [0],
"zusammengefasst": [0],
"grammatikalisch": [[4,7]],
"verteilung": [7],
"erwartet": [2,[0,1]],
"omegat-inhalt": [0],
"tkit": [2],
"rot": [[0,1,6,7]],
"and": [7],
"erwarten": [0],
"wahrscheinlich": [2],
"match-bericht": [4],
"fehlerbehebung": [2],
"zahlen": [1,5,0,7],
"benachrichtigen": [5],
"projektinhalt": [0,3,6,[2,4,7]],
"wellenlinien": [1],
"sortiert": [[5,7]],
"alternativ": [7,0,[2,5]],
"teamprojekt": [2,4,6,[0,7,8],[1,5]],
"herabgestuft": [6],
"kostenlo": [2],
"fehlend": [4,0,3,2],
"helplastchangesmenuitem": [0],
"standardbrows": [1,[4,5]],
"komprimieren": [0],
"erkennt": [4,[0,3],[1,2,7]],
"zeilenumbruch": [0,7],
"omegat.ex": [2],
"e-mail-adress": [0],
"exitcod": [0],
"sourcetext": [1],
"quelldokument": [[0,7]],
"erreichen": [0,[2,3,7]],
"gezeigt": [0,4],
"gui-einstellungen": [6],
"content-part": [0],
"erlaubt": [7],
"apache-ant-syntax": [7],
"jar": [2],
"api": [7],
"bleibt": [7,[0,2,5]],
"editselectfuzzy2menuitem": [0],
"startparamet": [2],
"dateinamensmust": [0],
"anfangsseg": [7],
"beliebig": [0,2,7,6,5],
"funktionsweise": [8],
"umgeschaltet": [[0,5]],
"umschlossenen": [0],
"extrahieren": [[1,7]],
"alternativen": [[0,1,4],5],
"projektspeich": [2],
"editornextseg": [0],
"standardkodierung": [0],
"angewendet": [1,[0,2],6],
"standarddarstellung": [1],
"übung": [0],
"trennbalken": [5],
"untersuchen": [[1,2],4,[0,3]],
"ähnlich": [[0,2],[1,7]],
"belegen": [7],
"editselectfuzzynextmenuitem": [0],
"belieben": [[0,1],[2,6]],
"gotonextxautomenuitem": [0],
"proportionalen": [1],
"java-startprogramm": [2],
"gelöst": [5],
"read.m": [0],
"begrenzt": [7],
"unsegmentierten": [0],
"cloud.google.com": [1],
"are": [5],
"readme.bak": [2],
"arg": [2],
"erweitern": [2],
"geht": [4],
"unicode-literale-kompatibilität": [0],
"rückkonvertierung": [2],
"fähigkeiten": [2],
"einbettung": [0,4],
"art": [7,[0,3,6]],
"translation-memory-dateien": [[2,7],3],
"wörterbuchtreib": [1],
"sonderzeichen": [0],
"gehe": [5,3,4,[0,2,6,8],1],
"navigieren": [[3,4,5,6],2],
"kontrollieren": [5],
"call": [0],
"buchstab": [0,4],
"remote-eigenschaften": [4],
"einheit": [0],
"übergeben": [[0,2]],
"html-kommentaren": [0],
"aufgehört": [3],
"tabul": [0],
"segmentierungsregelsätz": [1],
"mustern": [7],
"fragezeichen": [0],
"bidi-steuerzeichen": [0,4],
"toolsshowstatisticsmatchesperfilemenuitem": [0],
"spezifisch": [0],
"run": [7,0,2],
"nächste": [0,4,5,[3,7]],
"sprachregionen": [1],
"punktuellen": [7],
"maximierten": [5],
"leicht": [3,[0,4,6]],
"editorshortcuts.mac.properti": [0],
"vier": [0,[2,4,7]],
"vornehmen": [6,1,[0,2,3,7]],
"fragen": [0,1],
"titlecasemenuitem": [0],
"reduziert": [2],
"lizenzinformationen": [4],
"prüfungen": [[3,4]],
"editcreateglossaryentrymenuitem": [0],
"reih": [[0,2],1,[3,6,7],4],
"regelsätz": [1],
"auszufüllen": [4],
"tsv-dateien": [0],
"umschließen": [0,3],
"statistik": [1,4],
"lohnen": [3],
"sdlxliff-dateien": [2],
"viel": [[0,2],3,6],
"rein": [[0,7]],
"vorbereiten": [0],
"auf": [7,0,2,3,[1,4],5,6,8],
"introduc": [7],
"多和田葉子": [7],
"verknüpfen": [0,2],
"hauptfenst": [5,1],
"name": [0,1,[3,5],2,7],
"aus": [2,0,7,6,1,5,[3,4]],
"rechtschreibwörterbücher": [8],
"okapi-framework-dateifilt": [2],
"book": [0],
"show": [7],
"lernen": [[0,3]],
"bearbeitungsdialogfenst": [0],
"systemen": [2],
"comput": [2,[1,4]],
"computergestützte": [8],
"neuesten": [6,3],
"darin": [[1,8]],
"hauptzweck": [8],
"übereinstimmungen": [7,1],
"zielregion": [0],
"editortogglecursorlock": [0],
"enabl": [5],
"enthaltenen": [[1,5,7]],
"dadurch": [[2,7],0],
"gelb": [4],
"vorschläg": [5,[0,1]],
"menüfunktionen": [0],
"svn-cleanup": [7],
"verhalten": [2,4],
"wertet": [1],
"new_fil": [7],
"werten": [1],
"offline-dienstprogrammen": [2],
"hilfreich": [[3,7]],
"gebräuchlichsten": [0],
"target": [1,[4,7],6,3,[0,8]],
"software-dokument": [2],
"gelegentlich": [3],
"spaltenüberschrift": [[0,4,7]],
"namensmust": [0],
"project_save.tmx-datei": [7],
"arabischen": [0],
"config-dir": [2],
"editorskipprevtokenwithselect": [0],
"omegat-fremden": [1],
"konfigordn": [2],
"anwenden": [1,[0,7]],
"verknüpft": [2,0,[4,6]],
"ebenso": [[1,4,5]],
"durchgeführt": [2],
"vorgibt": [2],
"einfachen": [[1,3]],
"platziert": [4],
"kleinschreibung-sensitiv": [0,7],
"modus": [2,7],
"angrenzenden": [4],
"anbiet": [[1,5]],
"vorgesehen": [4],
"speichert": [4,7,2],
"sortierreihenfolg": [4],
"koreanisch": [1],
"daran": [6,2],
"schriftarten": [4],
"matches": [8],
"ablegen": [5,2,0],
"properties-format": [2],
"übereinstimmung": [0,1],
"targettext": [1],
"akzeptiert": [7,2,6,1],
"zusätzlichen": [7,3],
"kombinieren": [[2,6]],
"denselben": [1,[0,2,7]],
"schutz": [1],
"orang": [[5,7]],
"compil": [7],
"lokal": [2,7,1,0,4,5],
"edittagpaintermenuitem": [0],
"fuzzy-match-zahlen": [1],
"dateifilter-plugin": [2],
"speichern": [7,[2,6],[0,1,4],[3,5,8]],
"nähern": [3],
"ausschließt": [2],
"wörterbuchdateien": [[5,7]],
"unicod": [4,0],
"viewmarknbspcheckboxmenuitem": [0],
"zwischen": [[2,5],0,[1,4,7],3,6],
"projectmedcreatemenuitem": [0],
"korrekturles": [6],
"lizenz": [2,8],
"computer-assist": [3],
"webinterfac": [2],
"klammer": [0],
"lingvo-dsl-format": [6],
"tag-paar": [1],
"automatisierung": [7],
"whitespac": [2],
"hochgehen": [0],
"ausdruck": [0,7,1,3],
"anordnung": [[1,4,5,7]],
"msgstr": [0],
"inhaltlich": [3],
"übersetzt": [7,2,0,4,5,3,[1,6]],
"separat": [1],
"mehrfach": [[3,5,7]],
"dargestellt": [1,3,[5,7],4],
"nationalité": [1],
"daili": [0],
"anzuzeigen": [7,3,[1,4]],
"identischen": [4],
"schnellen": [4],
"übersetzbar": [0],
"zwischenablag": [4],
"buchstaben": [0,4,3,[1,2]],
"teamprojekten": [2],
"quadrat": [1],
"auch": [2,7,0,[1,3],6,5,4],
"installiert": [2,1,0,4,3],
"omegat.project": [2,6,3,[1,5,7]],
"hinterlegen": [3],
"editionen": [2],
"excludedfold": [2],
"targetcountrycod": [0],
"durchläuft": [7],
"insert": [0],
"verlangt": [[2,7]],
"fertig": [[2,3,7]],
"vierstellig": [2],
"arabisch": [0],
"quelldoku": [[0,1,2]],
"andernfal": [[2,7]],
"zieldateinamen": [0],
"skriptsprach": [7],
"gesamt": [1,[2,4],[0,3,6]],
"abbildungsverzeichnis": [8],
"geeigneteren": [2],
"rest": [[0,3]],
"original": [0,1],
"begriffsgruppen": [1],
"direkten": [2],
"splitten": [4],
"also": [0,2,[1,3]],
"auswählen": [0,4,7,[1,5],[2,3]],
"tag-informationen": [7],
"project_save.tmx.zeitstempel.bak": [6],
"größe": [7],
"interpretiert": [0],
"installierbaren": [1],
"ehesten": [2],
"itokenizertarget": [2],
"viewmarkwhitespacecheckboxmenuitem": [0],
"verfügbar": [1,0,2,[4,7],5,3],
"asterisk": [0],
"selekt": [2],
"zielsprachen": [1],
"bak": [2,6],
"verstecken": [7,0,[3,4]],
"typisiert": [7],
"ergebnisanzeig": [7],
"standen": [3],
"teamdateien": [2],
"rechtschreibwörterbuch": [1,[3,4]],
"jre": [2],
"projektmitgliedern": [2],
"heruntergeladen": [2],
"posit": [0,4,[5,7],3],
"versioniert": [6],
"fügen": [0,2,3,6],
"grafisch": [2],
"anzugeben": [[0,2],7],
"support-kanälen": [0],
"registrieren": [2,[1,5]],
"scrollen": [[1,3,5]],
"angst": [3],
"dezimalpunkt": [0],
"mitgelieferten": [7],
"projektordner": [8],
"weder": [[2,3]],
"alllemand": [7],
"enthalten": [0,[2,6],7,[3,5],[1,4]],
"auslöst": [0],
"relativ": [2,0],
"omegat-such": [0],
"geklont": [2],
"schrägstrich": [0,2],
"delet": [0],
"nicht-groß": [0],
"eingespeist": [2],
"bcp": [[3,7]],
"weiteren": [[0,1,2]],
"projectaccessglossarymenuitem": [0],
"fuzzy-markierung": [1],
"wiederum": [1],
"sei": [2,[0,3]],
"plattformen": [2,[0,1]],
"auftrag": [3],
"set": [1],
"prüfprozess": [1],
"meldungen": [5],
"balis": [5],
"numerisch": [0],
"projektnam": [7],
"bestimmten": [2,1,0],
"regionscod": [0],
"standardvers": [2],
"lösen": [5,7],
"links-nach-rechts-seg": [0],
"standard-termin": [2],
"kriterien": [7,[0,3]],
"änderungen": [2,0,6,[3,7],4,[1,5]],
"quellsegment": [5],
"project.sav": [2],
"featur": [7,[1,4,5]],
"offic": [0],
"bei": [7,2,0,1,3,[4,6],5],
"repositories.properti": [[0,2]],
"beginnen": [0,2,[3,4]],
"kodiert": [6],
"skriptsprachen": [7],
"prozentu": [5],
"textmust": [0],
"benutzerordn": [[0,2]],
"standardressourcen": [3],
"projektordn": [4,[2,6,7],0,3,1],
"unbegrenzt": [0,3],
"repositories": [8],
"verhindern": [0,2,[4,7]],
"mindestschwell": [1],
"projectsavemenuitem": [0],
"xmx6g": [2],
"übersetzungsressourcen": [8],
"verhindert": [[2,4]],
"autocompletertablefirstinrow": [0],
"versuch": [2,0],
"einzutragen": [[0,3]],
"niedrigst": [5],
"blockieren": [4,1],
"gleichzeitig": [[3,7],[1,2,4]],
"tmx-aktualisierungen": [2],
"sogenannt": [[0,7]],
"tabellenansicht": [5],
"tmautoroot": [0],
"macht": [[0,3],[1,2,4]],
"übersetzungsprozess": [[2,3]],
"startfähig": [2],
"zurück": [7,[3,4],0,2,[1,5,6]],
"angesehen": [6,0],
"suchfenst": [7,0,[3,4]],
"lädt": [4,7,1,3],
"eher": [0],
"sperren": [[2,3]],
"insertcharslrm": [0],
"unerwünschten": [4],
"teammitgliedern": [[2,3]],
"autotext-paramet": [0],
"übersetzerteam": [2],
"einblenden": [5],
"sie": [2,7,0,3,1,4,6,5,8],
"aufgeteilt": [7],
"personen": [[2,4]],
"daher": [2,7,0],
"dialogfenster": [8],
"zugewiesenen": [0,6],
"standardeinstellungen": [[0,2]],
"bestätigung": [4],
"belassen": [6,3],
"dateiformat": [0],
"unwahrscheinlich": [3],
"texteditor": [0,[2,6,7]],
"objektorientiert": [7],
"dritt": [6,[0,2]],
"gesperrt": [2],
"sortieren": [0,1],
"fügt": [7,4,6,[0,1]],
"jemanden": [2],
"standardordn": [7],
"foundat": [2],
"statistisch": [7],
"hängt": [4,[0,1],2],
"targetroot": [0],
"tag-bearbeitung": [1],
"bin": [0,[1,2]],
"nimmt": [4],
"gehören": [0,5,[1,2,3]],
"apertium": [1],
"anpassen": [[0,1,2],[3,4]],
"bis": [0,7,2,1,[3,6]],
"meta-inf": [2],
"sich": [2,0,7,3,4,1,5,6],
"regex-beispiel": [0],
"autovervollständigungsmenü": [1,[3,5]],
"projectopenmenuitem": [0],
"autom": [2],
"dark-theme-erkennung": [1],
"omegat-spezifischen": [2],
"multi-paradigma-sprach": [7],
"helfen": [2,[3,7,8]],
"eingefügten": [6,2],
"bereichen": [1],
"beizutreten": [3],
"funktion": [4,0,3,1,7,2],
"unicode-blöck": [0],
"whitelist": [2],
"inkludiert": [2],
"grün": [7,[4,5]],
"autor": [[3,4]],
"beteiligt": [2],
"unerwartet": [2],
"textcursormodus": [5,0],
"befehlen": [0,[1,7],2],
"beiden": [[0,7],1],
"begin": [0],
"formatierungszeichen": [[0,4],8],
"viewmarktranslatedsegmentscheckboxmenuitem": [0],
"auszug": [0],
"valu": [0],
"zehn": [[2,7],4],
"ausgelegt": [2],
"ilia": [2],
"sieh": [2,5,7,[1,4],[0,3,6]],
"unterstrichen": [5],
"reserviert": [2],
"schreibt": [[2,7]],
"veranschaulichen": [3],
"potenziell": [[0,1]],
"leistung": [3],
"unicode-formatierungszeichen": [4],
"verwandt": [6],
"erforderlich": [2,1,7,3],
"ersatz": [7],
"einfachst": [2,0],
"datenspeicherung": [1,8],
"übersetzungsdaten": [2],
"uxxxx": [0],
"basierender": [8],
"hier": [1,0,5,7,[2,6],4],
"rechtsklicken": [7,[2,4,5,6]],
"macos": [8],
"d.h": [6,0],
"richtig": [3],
"erstellen": [2,7,3,0,4,[1,6],5,8],
"editselectfuzzy1menuitem": [0],
"standort": [[1,2]],
"numerischen": [5],
"bibliotheken": [0],
"hide": [5],
"auszuwählen": [7,[0,5]],
"autocompleterlistpagedown": [0],
"auto": [4,[0,6],2,[1,7]],
"eigenschaft": [5],
"document.xx.docx": [0],
"editorskipnexttokenwithselect": [0],
"quelldateien": [2,[3,4],7,0,6,[5,8]],
"ratsam": [2,7],
"download": [2],
"editortoggleovertyp": [0],
"setzt": [7,[0,1,2,3]],
"wagenrücklaufzeichen": [0],
"optionen-menü": [[0,8]],
"erfordern": [[0,4,5]],
"differenz": [7],
"funktionscod": [0],
"erfordert": [2,1,[0,7]],
"administr": [2],
"gradlew": [2],
"kann": [2,0,7,5,3,[1,4],6],
"angibt": [[2,6]],
"level": [7],
"wurd": [[1,2],4,7,[0,3,6]],
"statistikdatei": [6],
"standardanordnung": [1],
"remote-datei": [2],
"fehlerbericht": [0],
"zeit": [3,[1,2]],
"eigentlich": [3],
"relevant": [[0,1]],
"einzustellen": [1],
"zeig": [7],
"ocred-pdf-dateien": [7],
"zeil": [0,7,2,5,[1,4],6],
"viewmarklanguagecheckercheckboxmenuitem": [0],
"abschreckend": [0],
"kaputt": [3],
"fünf": [1],
"quellbegriff": [[0,4],5],
"ausgerichtet": [[1,2,4,7]],
"zweifel": [6],
"vorherigen": [0,4,[3,7],[2,5]],
"tasten": [0,5],
"quelltext": [1,5,7,[0,4],[3,6]],
"geltungsbereich": [0],
"ungeeignet": [5],
"währungsübersetz": [7],
"switch": [[1,7]],
"zell": [7],
"bundl": [2],
"für": [2,0,7,1,3,4,6,5,8],
"einträgen": [7],
"technic": [0],
"senkrecht": [0,1],
"src": [2],
"gigabyt": [2],
"control": [[0,4]],
"kopiert": [[4,5,7],2,1,[0,3]],
"glossareintrag": [7,1,[0,5],3,[4,6]],
"no-team": [2],
"kommentarzeil": [0],
"api-schlüssel": [1],
"extrem": [2],
"kopieren": [2,6,7,0,[1,3,5]],
"lissens": [0],
"paketmanag": [2],
"authentifizierung": [2],
"kreativ": [0],
"ausführlich": [[0,2]],
"eingabetext": [5],
"unübersetzten": [7,[1,2,3]],
"ssh": [2],
"maschinell": [1,4,5,[0,3]],
"hilf": [[0,4],[2,3]],
"csv-dateien": [0],
"environ": [2],
"endungen": [[0,6,8]],
"unteren": [7,[1,5],[2,4]],
"zuordnung": [2,7],
"stichwortsuch": [7,3],
"qualitätssicherung": [7],
"friend": [0],
"zeigen": [0,[1,2]],
"referenzsprach": [2],
"zusätzlich": [0,2,7,[4,6],5],
"dokumentationsordn": [0],
"individuel": [[0,1]],
"eintrag": [5,[0,4]],
"vorbereiteten": [6],
"textdateien": [0,6],
"benutzergrupp": [2],
"variablenvarianten": [0],
"mehrmalig": [[1,4],[0,5,7]],
"denn": [2,[0,3]],
"unhandlich": [4],
"dynamisch": [7],
"gezogen": [5],
"übersetzungsstand": [2,[3,6,7]],
"currenc": [7],
"languag": [7,2],
"berücksichtigung": [5],
"gelöstem": [5],
"gültig": [7],
"current": [7],
"projekteigenschaften": [2,[1,6,7],[3,4],8],
"teamprojekt-funktionalität": [2],
"skripten": [[0,7],[2,3,6]],
"optionsglossaryfuzzymatchingcheckboxmenuitem": [0],
"kaum": [1],
"freigab": [2],
"key": [7],
"positionieren": [5],
"hören": [0],
"heißt": [0],
"omegat-konfigurationsdateien": [2],
"msgid": [0],
"dateistruktur": [0],
"svn": [2,7],
"omegat-license.txt": [0],
"quell": [7,[0,1],2,4,6,5],
"stori": [0],
"programmierstil": [7],
"nutzung": [2,[0,1],[6,7]],
"blauer": [7],
"artikel": [0],
"längere": [1],
"omegat-entwicklern": [7],
"stoppen": [1],
"wäre": [0],
"stammt": [2],
"unterstützt": [2,7,3,[0,1,4,6]],
"editreplaceinprojectmenuitem": [0],
"but": [0],
"symbol": [5,2,[4,7]],
"editordeletenexttoken": [0],
"problemdialogfenst": [1],
"entscheiden": [2],
"express": [0],
"html-datei": [0],
"richtet": [[0,2]],
"währungssymbol": [0],
"horizontal": [0],
"tastenmodifikatoren": [3],
"richten": [3],
"variant": [2],
"rückreferenzen": [7],
"anzuordnen": [[0,3]],
"textabschnitt": [0,3],
"glossaransicht": [1],
"gotoprevioussegmentmenuitem": [0],
"formaten": [2],
"problemprüfung": [[1,4]],
"log-dateien": [0],
"java-funktionalität": [1],
"gotopreviousnotemenuitem": [0],
"stderr": [0],
"editredomenuitem": [0],
"uilayout.xml": [[0,6]],
"sourceroot": [0],
"wählen": [[1,7],2,[0,4,5],3,6],
"syntaxhervorhebung": [0],
"punktuell": [7],
"größeren": [0],
"überarbeiteten": [3],
"schlägt": [7],
"französischen": [7],
"sind": [0,2,7,1,4,5,6,3],
"ausrichten": [7,4,2,[0,3,8]],
"versionierten": [2],
"gelöscht": [2,0,1,3],
"vorzunehmen": [3,[0,2,7]],
"üblicherweis": [0],
"priorität": [[1,2]],
"entgegengesetzt": [0],
"trennung": [7,[0,1,2]],
"einfluss": [0,3],
"repository-zugangsdaten": [2,1],
"verbunden": [3],
"textcursorstatus": [5],
"verhält": [2,0],
"besonder": [0],
"cat-tool": [7],
"portnumm": [2],
"normal": [0,7,2,[1,3,5,6]],
"zuzuordnen": [2],
"beschleunigen": [[2,3]],
"verglichen": [7,0],
"übersetzungsdiensten": [[1,5]],
"problemen": [[2,3,4]],
"grafiken": [0],
"kontextbeschreibung": [1],
"entwicklern": [2],
"example.email.org": [0],
"supportseit": [0],
"sprachreferenz": [6],
"werden": [0,2,7,1,4,5,3,6],
"mediawiki-seit": [[4,7],[0,3]],
"strukturel": [2],
"welch": [0,[4,7],[2,3]],
"integriert": [1],
"runtim": [2,0],
"treffen": [[2,6]],
"testen": [0],
"aligner": [8],
"treffer": [1,3,7],
"mehrer": [0,[2,7],5,4,1,[3,6]],
"faktoren": [4],
"gui-script": [7],
"produktivität": [0],
"review": [7],
"filenam": [0],
"fehlermeldung": [2],
"zahlenbereich": [0],
"roam": [0],
"fehlt": [0],
"bzw": [0,[2,7]],
"zugriffsrecht": [2],
"besitz": [1],
"nbsp": [7],
"funktionieren": [2,0],
"verdoppelt": [0],
"gotosegmentmenuitem": [0],
"definitionen": [1],
"geben": [0,[2,7],[1,3,4]],
"geordnet": [0],
"eingeben": [2,[5,7],[1,3]],
"eingerichtet": [2,6],
"omegat-funktionen": [[0,3,7]],
"originalsprach": [5],
"ziffer": [0,7],
"dekorationen": [3],
"initialcreationd": [1],
"bereitgestellt": [[2,4]],
"schriftauszeichnungen": [0],
"computergestützten": [3],
"helpaboutmenuitem": [0],
"verzicht": [0],
"verlaufsvorhersag": [[0,1]],
"projektmitglied": [2],
"standardübersetzung": [[4,5],[0,7]],
"informationen": [2,[4,5],[0,3,7],6],
"ansicht-menü": [[0,8]],
"xml-dateien": [0],
"regular": [0],
"aufrufbar": [[0,2]],
"satzeben": [0,[3,7]],
"fließt": [0],
"verstehen": [0],
"einzufügen": [5,4,[1,3],[0,6]],
"omegat-dateifiltern": [7],
"runtergehen": [0],
"omegat-vers": [2],
"token": [0,[1,2,7],[5,6]],
"filter": [0,[2,7]],
"site": [1],
"projectroot": [0],
"standardprojektzuordnung": [2],
"right-to-left": [0],
"editieren": [[2,3,7]],
"omegat.log": [0],
"angehend": [3],
"autocompletertableright": [0],
"argument": [0],
"pfeiltasten": [7,5,0],
"terminologieproblem": [4],
"sprachprüfdienst": [1],
"vorkommt": [[0,5],7],
"abgelehnt": [1],
"gesicherten": [2],
"links-nach-rechts-markierung": [[0,4]],
"grundeinheit": [7],
"tab": [0,[4,5],1],
"remote-repositori": [2,[5,6]],
"satzschreibung": [[0,4]],
"exportierten": [7],
"breit": [7],
"tag": [0,1,3,7,4,2,5],
"versionen": [2,[7,8]],
"ssh-konfigur": [2],
"vorangestelltem": [0],
"tabellenzeil": [0],
"glossarordn": [[0,2,5,6]],
"vorherig": [0,4,[3,5]],
"standardsprach": [2],
"warnung": [7,0,1,[2,3],4,6,5],
"gefunden": [0,7,1,5],
"projectreloadmenuitem": [0],
"richtigen": [7,[1,3]],
"originalformat": [2],
"person": [2],
"schreibrichtung": [5],
"navig": [[4,5]],
"wartungsarbeiten": [2],
"arten": [0,2,4],
"variationen": [0],
"zweisprachig": [[2,6,7]],
"prozent": [1],
"tbx": [0,1],
"can": [0],
"leer": [0,[2,5,6],[4,7],1,3],
"absatz": [0,[1,7],[3,4,5]],
"cat": [[0,3]],
"genauso": [6],
"tabellen": [0],
"angrenzend": [[4,5]],
"ausgewählten": [4,7,0,[2,3,5]],
"regeln": [7,1,0,4],
"markieren": [4,7,[3,5],0],
"duser.countri": [2],
"kommentar": [0,5,3,4],
"cursortasten": [3],
"readm": [0],
"betriebsmodus": [1],
"match": [4,1,5,0,6,2,7,3],
"prüft": [0,[1,4]],
"weisen": [4],
"behebung": [3],
"schriftgröß": [1],
"align.tmx": [2],
"englisch": [[0,2]],
"file2": [2],
"dennoch": [[2,3]],
"ersichtlichen": [2],
"makro": [7],
"ansicht": [5,1,[3,4],[7,8],[0,6]],
"translation-memory-datei": [2,[3,7]],
"eigennamen": [5],
"bewegt": [[0,4]]
};
