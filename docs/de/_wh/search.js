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
 "Anhang A. Wörterbücher",
 "Anhang B. Glossare",
 "Anhang D. Reguläre Ausdrücke",
 "Anhang E. Anpassung der Tastaturkurzbefehle",
 "Anhang C. Rechtschreibprüfung",
 "Installation und Ausführung von OmegaT",
 "Anleitungen",
 "OmegaT 4.2 - Benutzerhandbuch",
 "Menüs",
 "Fensterbereiche",
 "Projektordner",
 "Fenster und Dialogfenster"
];
wh.search_wordMap= {
"aufklappbar": [11],
"tmx-datei": [10,6,5,8],
"konvertiert": [[6,8,11]],
"trennen": [6],
"selben": [[6,11]],
"legt": [5],
"sinnvoller": [11],
"zwar": [6,10],
"zudem": [7],
"automatisch": [11,8,5,6,4,[1,3],9],
"erfolgen": [[1,6]],
"info.plist": [5],
"gründe": [1],
"inhaltsverzeichnis": [7],
"zeilenendzeichen": [2],
"formatierungen": [11,6],
"dialogfenst": [11,8,10,[1,5,9]],
"fuzzi": [11,9,8,10,6],
"vorher": [[6,11]],
"einfügen": [11,[3,8],9,5],
"suchergebnissen": [11],
"rechner": [5,[8,11]],
"menüpunkt": [3,8,11,9],
"beschreiben": [[6,11]],
"weil": [6,[4,9]],
"roll": [5],
"sprachenpaar": [6,11],
"dgoogle.api.key": [5],
"speziell": [[6,11]],
"weis": [11,[5,9]],
"violett": [8],
"ausgeschlossenen": [6],
"edittagnextmissedmenuitem": [3],
"vermeiden": [6,10,[7,11]],
"durchsetzen": [10],
"quiet": [5],
"bewirken": [[5,11]],
"laden": [[5,11],6,8,[0,3]],
"es_es.d": [4],
"beispiele": [7],
"implementiert": [11],
"bezeichnet": [11,[5,6]],
"satzübersetzungen": [11],
"the": [5,[0,2,11]],
"projectimportmenuitem": [3],
"bearbeitung": [11],
"imag": [5],
"richtigkeit": [[1,9]],
"tmx-datenbanken": [6],
"möchte": [[6,11]],
"wirkungsvoller": [2],
"microsoft-ziel-local": [11],
"quelltexten": [11],
"moodlephp": [5],
"zwei": [11,5,6,[4,8],[9,10]],
"currsegment.getsrctext": [11],
"dieselben": [[1,5]],
"kategorien": [[2,7]],
"export": [6,11],
"unabhängig": [11],
"start-technologi": [5],
"transtip": [[3,9]],
"check": [6],
"zieldateien": [11,6,8,3],
"xxxx9xxxx9xxxxxxxx9xxx99xxxxx9xx9xxxxxxxxxx": [5],
"doppelt": [11],
"bidirektional": [8],
"regelpriorität": [11],
"ordner": [5,10,11,6,8,4,[1,9],0],
"omegat.app-standort": [5],
"gefüllt": [6],
"zusammenführen": [6],
"standardwert": [11,[5,10]],
"muster": [11,6,2],
"verarbeitenden": [5],
"weiß": [5],
"gefundenen": [[5,11],8],
"zurückspringen": [8],
"webster": [0,[7,9]],
"einzig": [[9,11]],
"bestehend": [5,[6,10,11]],
"pluralformen": [1],
"verwendend": [4],
"beliebt": [11],
"konvertieren": [6,11],
"neuer": [8,[5,11]],
"zeichenkett": [11,2],
"translation": [7],
"grünem": [9],
"syntax": [11,3],
"neuen": [11,[3,5,8],[1,6],4],
"anzeig": [11],
"inline-formatierung": [[10,11]],
"empti": [5],
"po-dateien": [11],
"einzubeziehen": [11,2],
"persönlichen": [5],
"dockt": [9],
"elektronischen": [9],
"könnten": [11],
"doc-endung": [6],
"installationsordn": [5],
"block": [2],
"tms": [6,10,[9,11]],
"tmx": [6,11,[5,9]],
"repo_for_all_omegat_team_project": [6],
"festgelegt": [11,[3,8]],
"integ": [11],
"intel": [5,7],
"sanktion": [10],
"interferiert": [5,11],
"fr-ca": [11],
"mainmenushortcuts.properti": [3],
"gelangen": [5],
"teilt": [5],
"glossardateien": [[1,9]],
"verunreinigt": [6],
"anmerkung": [2,[8,9,10]],
"statistiken": [6,[3,8,10],11],
"wenn": [11,8,6,5,10,9,4,1,3,[0,2]],
"mehrere": [7],
"cmd": [3,[5,6,11]],
"coach": [2],
"subtitl": [5],
"gespeichert": [11,[1,5,6,8],10,9],
"gotohistorybackmenuitem": [3],
"dateinamen": [11,4],
"fensterbereich-widgets": [7],
"teilen": [6,9],
"project-save.tmx": [6],
"kennzeichen": [8],
"befehlszeilenfenst": [5],
"powerpc": [5],
"ergebnissen": [11],
"dateierstellung": [11],
"exportieren": [6,11,[3,8,10]],
"satz": [11,8,6],
"menüs": [7],
"dateiendung": [11,1],
"eingabefeldern": [[5,6]],
"ltr-quellsprach": [6],
"hervorhebung": [8],
"trotz": [11],
"standorten": [6],
"ergebnisfeld": [2],
"eine": [7],
"regelsatz": [11],
"projekt": [6,11,8,5,10,9,[1,4],3,[0,7]],
"instal": [5,[4,7,8,9]],
"übernommen": [[10,11]],
"wert-text": [5],
"font-fallback": [8],
"sollt": [11,6],
"dafür": [[2,6,11]],
"omegat.sourceforge.io": [5],
"function": [11],
"pipe": [11],
"bereit": [11,5,6,8,[4,10],[1,3,9]],
"großbuchstab": [2,11],
"wert": [11,2,1],
"omegat-projektdatei": [9],
"dateifiltereinstellungen": [11],
"nutzen": [5,[6,10]],
"translat": [6,11,10,5,9,8,[2,4]],
"html-kommentar": [11],
"nicht-wortgrenz": [2],
"verschoben": [11,9],
"einhergehen": [8],
"umbenennung": [6],
"glossartreff": [[9,11]],
"optisch": [6],
"auswahllist": [11],
"lck": [9],
"tsv": [1],
"paar": [5],
"verwalten": [6,7],
"auszuführen": [5,11],
"gnome": [5],
"verwaltet": [6],
"kategori": [11,8,2],
"anstatt": [6],
"kollegen": [9],
"textexportfunkt": [11],
"struktureinheiten": [11],
"csv": [1,5],
"n.n_linux.tar.bz2": [5],
"satzweisen": [11],
"tun": [[4,11]],
"nutzungsbedingungen": [0],
"umbenennen": [[4,11]],
"enhanc": [8],
"benachrichtigung": [11],
"unübersetzbar": [11],
"press": [3],
"dock": [5],
"standardmäßig": [11,8,6,5,[2,9,10]],
"gesehen": [[6,11]],
"unsichtbar": [11],
"element": [11,5,[1,3,6]],
"wortwiederholungen": [2],
"dasselb": [11,[5,6,8]],
"speicher": [[5,11]],
"root-ordn": [6],
"genutzt": [[4,6]],
"statt": [[3,5,8,9,11]],
"startbefehl": [5],
"dmicrosoft.api.client_secret": [5],
"markierten": [11,8],
"herangezogen": [11,[4,6,9]],
"weiter": [11,5,9,10,2,[0,1,4,6,7,8]],
"abschnitt": [5],
"suchergebni": [11],
"analysiert": [2],
"absatzumbruch": [11],
"angemessenen": [6],
"ctrl": [3],
"document": [11,5],
"ausgeschlossen": [6],
"einmalig": [11,[3,9]],
"übersetzerin": [6],
"quelldateinamen": [11],
"selbst": [11,[2,6]],
"beendet": [[2,8]],
"bedenken": [0],
"eingabeaufforderung": [5],
"vorkommen": [11,2,9,[3,4,10]],
"beenden": [11,8,[3,6]],
"genau": [11,4],
"resourc": [5,11],
"geschrieben": [8,11],
"während": [9,[5,10],[6,11],[0,1,8]],
"ausgewertet": [11],
"team": [11,[3,6]],
"xx_yy": [[6,11]],
"docx": [[6,11],8],
"txt": [[1,6],[9,11]],
"ungreedy-quantoren": [2,7],
"akzeptieren": [10],
"melden": [6],
"einzubetten": [6],
"meldet": [6],
"fertiggestellt": [6],
"benötigen": [5,6,2],
"definit": [11],
"jeweil": [[1,8,10]],
"typ": [11],
"source": [7],
"repository-zuordnung": [[6,11]],
"mechanismus": [8],
"begriffen": [[1,9]],
"glossardateiendungen": [9],
"populären": [11],
"trnsl": [5],
"zustand": [8,[6,9],11],
"viewdisplaymodificationinfoselectedradiobuttonmenuitem": [3],
"index.html": [5],
"omegat.tmx": [6],
"kapazität": [5],
"odg-datei": [6],
"wörter": [11,[8,9],[2,10]],
"anwendungsbeispiel": [2],
"erleichtern": [11],
"quellseg": [11],
"lösung": [6],
"fällen": [11,6],
"diffrevers": [11],
"verfahren": [6],
"bestandteil": [5],
"felder": [11,8],
"zeilenweis": [10],
"seltenen": [11],
"konfigurationsdateien": [5],
"hinzugefügten": [6],
"glossaren": [1,[7,9]],
"sicheren": [6],
"texteinheiten": [11],
"rutsch": [8],
"manchmal": [11,[6,10]],
"stammformredukt": [11,[1,9],3],
"darauf": [11,5],
"omegat-instanzen": [5],
"projektspezifischen": [11],
"stark": [11],
"befolgen": [6],
"automatisieren": [5],
"project.gettranslationinfo": [11],
"quelldateiformaten": [11],
"abbildung": [4,[0,2,9]],
"fälle": [10],
"gängigst": [5],
"sparen": [11],
"ausgabedateinam": [5],
"start": [5,7],
"grundsätzlich": [11],
"merkt": [8],
"omegat-wiki": [0],
"editiert": [11],
"equal": [5],
"omegat-editor": [11],
"modifikationsinformationen": [11],
"optionsalwaysconfirmquitcheckboxmenuitem": [3],
"inklus": [[9,11]],
"unterschi": [11],
"ungültigen": [5],
"komplett": [[6,11]],
"verbleiben": [5],
"steigern": [11],
"enter": [[3,5,8,11]],
"programmstart": [5],
"rechtschreibwörterbüchern": [4,11],
"tbx-glossar": [11,3],
"angrenzen": [9],
"bidi": [6],
"projectteamnewmenuitem": [3],
"daten": [11,[5,6]],
"bekannten": [9],
"analog": [10],
"memori": [6,11,10,9,[5,8],2],
"übersetzungsdatum": [11],
"importiert": [[1,6,9]],
"no-match": [8],
"tools-menü": [3,7],
"daraus": [5,11],
"wörterbucheinträg": [[8,11]],
"anschließend": [11,5,[4,10]],
"log": [[3,8]],
"korrekt": [6,1,10,5,[8,11]],
"jedoch": [11,5,[1,4,6]],
"meinung": [[6,9]],
"omegat.jnlp": [5],
"einzigen": [[5,6,11]],
"detaillierter": [11],
"genutzten": [5],
"n.n_windows_without_jre.ex": [5],
"teil": [8,9,[4,6,10,11]],
"leerraumzeichen": [2,11],
"zeichen": [2,11,1,[5,8],[7,9]],
"prof": [11],
"unterschiedlich": [11,[8,9]],
"wählt": [8],
"dmicrosoft.api.client_id": [5],
"sprachmust": [11],
"solch": [11,6,10],
"config-fil": [5],
"interessant": [2],
"durchaus": [11],
"versionsverwaltungssystem": [6],
"vordefinierten": [3,11],
"schlüsselsuchbegriff": [2],
"derartig": [6],
"abzuarbeiten": [11],
"abgedockt": [11],
"klass": [2],
"eventuel": [11,[2,6,9]],
"das": [11,8,5,6,9,2,1,[4,10],0,3,7],
"rechtsvorschriften": [6],
"bezugnehmend": [11],
"online-wörterbüchern": [4],
"hellgrau": [8],
"minuten": [[6,11],8],
"system-user-nam": [11],
"möglicherweis": [6,11,5,1],
"format": [1,[0,6,11],[7,8]],
"bestimmen": [[4,11]],
"fuzzy-matches-beispiel": [9],
"console.println": [11],
"herunterlädt": [0,7],
"herunterzuladen": [4],
"fetter": [[9,11]],
"launcher": [5],
"abhängt": [1],
"anfangsbuchstaben": [8],
"scheint": [[5,8]],
"betroffen": [11],
"vorangestellt": [10],
"bild": [4],
"datei": [5,6,11,8,10,[1,3],0,[7,9]],
"browser": [5],
"existierend": [11],
"hinweise": [7],
"regex-beispielen": [2],
"zurückkehren": [[9,11]],
"ausreichend": [11],
"fuzzy": [7],
"erst": [11,8,[1,2,3,5,6,9]],
"segmentierungsparamet": [6],
"project_files_show_on_load": [11],
"benutzt": [10],
"sammlung": [[2,9]],
"identifizieren": [11],
"ltr": [6],
"optionsexttmxmenuitem": [3],
"originaldoku": [6],
"zeichensatzkodierung": [11],
"build": [5],
"marketplac": [5],
"spanisch": [4],
"heißen": [[3,11]],
"notizen": [9,11,8,[3,7]],
"entries.s": [11],
"anwendbarkeit": [11],
"behandlung": [2],
"den": [11,6,5,9,8,10,1,[3,4],0,2],
"bevorzugt": [9],
"dem": [11,6,5,8,[2,9],10,[1,4],0,7],
"gotonextuntranslatedmenuitem": [3],
"targetlocal": [11],
"der": [11,6,5,9,8,10,1,4,3,[0,2],7],
"fehlenden": [[8,9]],
"path": [5],
"des": [11,6,[5,8],9,10,1,4,3,[2,7]],
"entwed": [11,[2,4,6,8]],
"verlauf": [8,3],
"enforce-ordn": [10],
"autovervollständigung": [3,11,[1,8]],
"mehreren": [11,[1,5,9,10]],
"sondern": [[6,11]],
"verschiedenen": [11,6],
"dort": [6,[5,11]],
"eignen": [11],
"gleitkommazahlen": [11],
"allsegments.tmx": [5],
"unbedingt": [[1,9]],
"umständen": [6],
"öffnet": [8,[4,5,9,11]],
"öffnen": [5,11,8,[3,6],[9,10],[1,4]],
"helpcontentsmenuitem": [3],
"schrittweis": [11],
"omegat-org": [6],
"descript": [3],
"xml-formaten": [11],
"auszuschalten": [11],
"deaktiviert": [8,11,2],
"projectaccessdictionarymenuitem": [3],
"meisten": [11,[3,5]],
"veröffentlichen": [6],
"beschreibung": [[5,6]],
"dazwischen": [11],
"punkt": [11,2,[5,6,8]],
"zugangsschlüsseln": [11],
"ermöglicht": [11,5,8,9],
"term": [9],
"momentan": [9],
"ausnahm": [6,11],
"stilistischen": [11],
"europäischen": [[6,8,11]],
"duden": [9],
"aussehen": [5],
"kommentarzeilen": [3],
"ausgefüllt": [[8,11],3],
"docx-dateien": [[6,11]],
"word-benutzern": [11],
"vordefinierte": [7],
"befehlszeile": [7],
"vordergrund": [8],
"ähnlichkeitsschwellenwert": [11],
"paus": [4],
"ländercod": [11],
"spotlight": [5],
"erledigt": [11],
"die": [11,5,6,8,9,10,4,1,2,3,0,7],
"dateikodierung": [1],
"imper": [11],
"hauptmenü": [11,[3,9]],
"dir": [5],
"gehostet": [11],
"div": [11],
"viewfilelistmenuitem": [3],
"stattfindet": [11],
"oktalwert": [2],
"kompat": [5],
"projektparamet": [[6,10]],
"vorübersetzung": [6],
"test": [5],
"omegat": [5,11,6,8,[7,10],3,4,1,[0,2,9]],
"bedienungsanleitung": [8,3],
"strengen": [6],
"drücken": [11,9,[5,8],[1,6]],
"durchschalten": [3],
"teilnehmen": [6],
"virtual": [11],
"denen": [11,[6,8]],
"console-align": [5],
"besteht": [6,9],
"projectopenrecentmenuitem": [3],
"standardeinstellung": [11,6],
"google-api-schlüssel": [5],
"darzustellen": [11],
"verwendung": [[1,11],[4,6,7]],
"minütig": [[6,8]],
"umzugehen": [[6,11]],
"standard-spanisch": [4],
"zusammenhängend": [11],
"und": [11,6,5,8,9,4,10,2,1,[3,7],0],
"platzhalt": [11,6],
"project_save.tmx.temporari": [6],
"suchressourcen": [11],
"unl": [9],
"editoverwritemachinetranslationmenuitem": [3],
"bezug": [11,2],
"anzeigeprogramm": [6],
"ingreek": [2],
"bearbeiten-menü": [3,7],
"es_es.aff": [4],
"erfüllen": [11],
"projectexitmenuitem": [3],
"übersichtlich": [11],
"wirklich": [[8,11]],
"haben": [11,5,6,4,[8,9,10],[0,1,3]],
"legen": [11],
"text": [11,8,9,[6,10]],
"editregisteruntranslatedmenuitem": [3],
"konsol": [5],
"init": [6],
"sitzung": [11],
"zusammengeführt": [11],
"segmentierungseinstellungen": [11],
"maco": [5,3,1],
"erzwingt": [11],
"gelesen": [6,[1,5,10]],
"vergessen": [0],
"interval": [[6,8]],
"zielland": [11],
"mitten": [[6,11]],
"mittel": [9],
"unverändert": [11],
"übersetzungsbüro": [9],
"status": [11],
"verarbeitet": [[5,11],[6,8]],
"derzeitigen": [10],
"server": [6,[5,11]],
"darum": [11],
"identifiziert": [11],
"paramet": [5,[6,11]],
"mac": [[3,6]],
"umfassen": [11],
"mal": [6,11,5],
"semikolon": [6],
"verarbeiten": [11],
"man": [11,0,[3,5,6,7,8,10]],
"stand": [10],
"map": [6],
"vorteilhaft": [11],
"klar": [10],
"may": [11],
"proxy-administr": [11],
"klartextvers": [6],
"url": [6,11,[4,5,8]],
"megabyt": [5],
"uppercasemenuitem": [3],
"schon": [[4,6,9]],
"viewmarkuntranslatedsegmentscheckboxmenuitem": [3],
"entscheidung": [10],
"fußnoten": [11],
"haupt-tm": [6],
"relev": [11],
"standardmodus": [9],
"treten": [11],
"projektintern": [9,10],
"umgewandelt": [[6,11]],
"use": [[0,5,11]],
"vorgeschlagenen": [9],
"dienen": [[6,10]],
"omegat-installationsordn": [11],
"omegat.jar": [5,[6,11]],
"omegat.app": [5],
"usr": [5],
"usw": [11,9,6,[0,2,5,10]],
"auftraggeb": [[9,11]],
"alten": [11],
"credit": [8],
"abweichend": [11],
"modifikationsinfo": [3,8],
"utf": [1],
"ausgabeformat": [6],
"annehmen": [10],
"doppelklicken": [5,[8,9,11]],
"hilfetext": [6],
"datenschutzgründen": [11],
"ermitteln": [5],
"html-tag": [11],
"einschränkungen": [11],
"regelmäßig": [[5,6]],
"verwerfen": [11],
"übereinstimmen": [11,[1,4,9]],
"null": [11,2],
"gesamtzahl": [9,11],
"quelldatei": [11,6,8],
"dsl": [0],
"herunterladen": [5,[3,6,8,11]],
"eingabefokus": [11],
"kunden-id": [5],
"produktnamen": [6],
"n.n_windows_without_jre.zip": [5],
"folgenden": [11,[3,5,6],[2,9]],
"wiederherstellen": [11,9,6,3],
"einigen": [[10,11]],
"dtd": [5],
"textbezogen": [6],
"kommentiert": [11],
"rechtschreibprüfung": [4,11,7,10,[1,2,3]],
"ermittelt": [11],
"anhand": [11,5,[4,7]],
"nennen": [11],
"plattform": [5],
"identisch": [11,[6,8],[5,9],[3,10]],
"anhang": [[1,2,4],[0,3],6],
"languagetool-problem": [8],
"altgr": [3],
"projectcompilemenuitem": [3],
"console-transl": [5],
"normalerweis": [5,[6,10]],
"anspruch": [5],
"günstigen": [5],
"erlauben": [[8,9,11]],
"vorgenommen": [5,6,11],
"zugeschnitten": [11],
"macos-äquival": [5],
"geschlossen": [6,[8,9]],
"gotonextuniquemenuitem": [3],
"wordart": [11],
"objektattribut": [11],
"optionsviewoptionsmenuitem": [3],
"inform": [11,5],
"nachschlagen": [8],
"leerzeichen": [11,8,[2,3]],
"einstellungen": [8,5,11,4,[1,6,7,9,10]],
"commit": [6],
"gesamten": [[6,8,11],[9,10]],
"targetlocalelcid": [11],
"wörterbuch": [4,11,9,[7,8]],
"monat": [6,5],
"voraussetzungen": [5],
"project_stats_match.txt": [10],
"blöcke": [11],
"macos-benutz": [5],
"benötigt": [5,[6,11]],
"dvd": [6],
"xmx2048m": [5],
"bekommt": [11],
"auto-propagation-opt": [11],
"einzulegen": [4],
"tmx-dateien": [6,10,11,[3,5,8]],
"runterschieben": [11],
"ersten": [11,[5,6,8],1],
"xxxxxxxxxxxxxxxx.xxxxxxxxxxxxxxxx.xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx": [5],
"erster": [8],
"aufwendig": [6],
"zurückzusetzen": [11],
"terminologiedateien": [1],
"anlegen": [5],
"krunner": [5],
"libreoffic": [4,[6,11]],
"primärer": [5],
"anzuwenden": [11],
"einstieg": [6],
"defekten": [6],
"dürfen": [[5,8,11]],
"block-elementen": [11],
"automatischen": [11,8],
"warnungen": [5],
"dienst": [5,11],
"geändert": [6,8,11,5,3],
"mir": [11],
"griechischen": [2],
"feld": [11,8,4],
"mit": [11,5,6,8,9,0,10,[2,4],3,1,7],
"spracheinstellungen": [4],
"formatierenden": [11],
"standard-tastaturkurzbefehl": [3],
"dateinam": [11,[1,9]],
"gebunden": [0],
"projektmanag": [6],
"fortgeschritten": [[2,5]],
"vergewissern": [5,4],
"zeichenklassen": [[2,7]],
"stößt": [11],
"variablen": [11],
"ausführen": [5],
"vorgang": [11],
"löscht": [8],
"enthält": [10,5,[6,11],9,8,[0,2,7]],
"viewdisplaysegmentsourcecheckboxmenuitem": [3],
"editregisteremptymenuitem": [3],
"erwartete": [7],
"open": [11],
"generieren": [10],
"www.oracle.com": [5],
"vorrang": [8],
"skript": [11,8,5],
"font-substitut": [8],
"project": [11],
"xmx1024m": [5],
"autotext": [[3,11]],
"verarbeitung": [6],
"grundlegend": [5],
"arbeitet": [[5,6]],
"ausschließlich": [11],
"eingesehen": [5],
"arbeiten": [6,11],
"nummern": [11],
"penalty-xxx": [10],
"gotonextsegmentmenuitem": [3],
"changes-datei": [5],
"sinnvol": [11,[4,10],[5,6]],
"absatzweis": [11],
"projekteigenschaften-dialogfenst": [6,4],
"große": [[4,11]],
"kde-benutz": [5],
"nnn.nnn.nnn.nnn": [5],
"textformatierungen": [6],
"übersetzungsvorschlägen": [9],
"ausnahmeregel": [11],
"linux-benutzer": [7],
"abort": [5],
"aufruf": [11],
"left-to-right": [6],
"ergebnisfenst": [8],
"internet": [[4,5,6,11]],
"wenig": [[6,11],[2,5,10]],
"internen": [[8,11]],
"bitten": [6,9],
"beschriebenen": [5],
"nummer": [11,8,[1,9]],
"html-dokument": [11],
"ihrer": [5,11,6,[3,8,9],4],
"erstklassig": [11],
"konvertierungen": [6],
"textbereich": [11],
"ihrem": [5,4,9,[3,8,11]],
"prototypbasiert": [11],
"ihren": [5,[6,11],9,10],
"es-mx": [4],
"layout": [9],
"stem": [9],
"maus": [9],
"schaltflächen": [11],
"quelle-ziel": [0],
"titel": [11,8],
"darstellungsparamet": [6],
"suchbefehl": [8],
"einzelnen": [11,9],
"zielkodierung": [11],
"japanischsprachigen": [5],
"tcl-basiert": [11],
"wort": [11,[4,8],5,9],
"baut": [11],
"beinhalten": [10],
"objekt": [11],
"reservierten": [4],
"word": [[6,11]],
"beinhaltet": [[6,9]],
"reagieren": [9],
"editierfeld": [9,11],
"dabei": [6,[9,10]],
"auto-ordn": [10],
"auto-propag": [11],
"hilfs-tmx-datei": [6],
"fest": [[5,11]],
"fett": [11,[1,9]],
"mithilf": [11,[0,4]],
"nutzbar": [3],
"installation": [7],
"kalkulationsmethoden": [11],
"ausschlüss": [6],
"lingvo": [0],
"mrs": [11],
"kanadischen": [11],
"stil": [6],
"tauschen": [6],
"jederzeit": [9],
"funktional": [11],
"zieldokument": [[6,8],11],
"speicherort": [11,[1,5]],
"verschieden": [11,6,[5,8,9]],
"allem": [11],
"allen": [[5,11],[6,10]],
"klick": [5,11],
"aller": [11,[5,6,8]],
"gleich": [11,6,[2,5]],
"suchmethoden": [11],
"dokumenten": [11],
"eck": [9],
"maustast": [11,9,5],
"vielen": [[6,11]],
"pt_pt.aff": [4],
"entpacken": [5,0],
"detailliert": [5],
"html": [5,11],
"wirkungsvoll": [11],
"konfigurationsanleitungen": [11],
"terminal-fenst": [5],
"artund": [4],
"zusammenfassung": [7],
"sofort": [1],
"bisher": [[9,11]],
"beantragt": [5],
"wörterbuch-funkt": [0],
"tag-verarbeitung": [11,3],
"markierung": [11],
"exakten": [8,11],
"jres": [5],
"www.ibm.com": [5],
"nützlich": [11,5,[2,9]],
"java-software-anwendungen": [5],
"quellcod": [5],
"nehmen": [[5,10],6],
"kommiss": [8],
"langsam": [[5,11]],
"zutrifft": [11,8],
"definieren": [11,[2,3,9]],
"hinwei": [11,6,8,10,9],
"entkommentieren": [5],
"andere": [7],
"benutzernam": [11],
"recht": [[5,6],[8,11]],
"command": [9,3],
"n.n_without_jr": [5],
"spezifik": [11],
"scripting-fenst": [[8,11]],
"zuzuweisen": [5],
"unicode-blöcke": [7],
"fenstergröß": [11],
"betriebssystemen": [[5,10]],
"zugewiesen": [5,11],
"bestätigen": [11,[3,5,8]],
"aktualisiert": [[1,5]],
"viewmarkbidicheckboxmenuitem": [3],
"year": [6],
"kümmert": [10],
"geöffnet": [11,8,[6,9],5,[1,4]],
"med-format": [8],
"kleinschreibung": [3,8,11,2],
"themenbereich": [10],
"via": [[1,11],[2,10]],
"macos-goodi": [5],
"springen": [9],
"zuzugreifen": [[5,11]],
"n-ten": [8],
"steuerzeichen": [[2,8]],
"ausrichtung": [6,11],
"entwickeln": [2],
"ausgegraut": [8],
"änderung": [11,10,8],
"packen": [6],
"dienstprogramm": [5],
"zurückgesetzt": [5],
"übersetzerseit": [6],
"med-projekt": [8],
"übersetzungseinheit": [10],
"außerdem": [6,[5,9]],
"sprach": [5,[6,11],2,4],
"version": [5,8,6],
"konfigurationsdatei": [5],
"statusleist": [9,5],
"log-datei": [8],
"fünften": [9],
"übersetzungsdienst": [8,11,[5,9]],
"durchführen": [8],
"bindestrich": [5],
"klassen": [[2,7]],
"abzulegen": [9],
"gebündelt": [5],
"anweisungen": [5,6],
"weist": [5],
"geschützt": [8,[3,11]],
"standardinterval": [11],
"projecteditmenuitem": [3],
"aufkeimend": [6],
"ein": [11,6,5,8,2,9,10,4,1,3,[0,7]],
"britannica": [0],
"begonnen": [[6,11]],
"worden": [[6,9]],
"gerückt": [8],
"generaldirekt": [8],
"computern": [11],
"wikipedia": [8],
"machin": [11],
"strg": [3,11,9,6,8,1,[0,10]],
"sprachwörterbuch": [4],
"stund": [6],
"gegen": [10,6],
"windows-versionen": [5],
"iceni": [6],
"odf-dateien": [[6,11]],
"bestimmt": [11,6,9,[4,5]],
"komprimiert": [10],
"präfix": [11],
"gibt": [11,[1,4,5,9],[0,6]],
"texteben": [6],
"brauchen": [6,10],
"ursprünglichen": [[6,9]],
"verwaisten": [11],
"gewählt": [4],
"zulassen": [11],
"dsun.java2d.noddraw": [5],
"kommandozeilenmodus": [5],
"gültigen": [11],
"stellt": [5,8,[9,11]],
"aufträgen": [9],
"x0b": [2],
"konform": [6],
"möglich": [11,[6,9],[1,2,5]],
"mittler": [2],
"jahr": [6],
"nicht-leerraumzeichen": [2],
"simpledateformat-must": [11],
"angelegt": [[6,9]],
"altern": [9,8,11,[3,5]],
"scrollbalken": [11],
"http": [6,5,11],
"aufwärt": [11],
"keinen": [11,[5,9,10]],
"bezüglich": [[5,6]],
"vorhanden": [11,5,[1,10],6,[0,8]],
"nächstes": [3,8,11],
"nächsten": [8,11,[5,9],3],
"könnte": [6],
"komma": [[2,11],1],
"sprachkombin": [0],
"root-verzeichni": [[5,6]],
"von": [11,6,5,[2,9],4,8,3,10,1,7],
"vom": [11,6,8,[2,3,9]],
"vor": [[8,11],10,5,[0,1,3,4,6,9]],
"projectsinglecompilemenuitem": [3],
"end": [11,[2,3,10]],
"bereitzustellen": [[5,11]],
"einfachsten": [5],
"früheren": [6,[9,11]],
"beschränkt": [[6,11]],
"myfil": [6],
"visuel": [8],
"überschrieben": [[5,8,11]],
"machen": [11,8,[3,9,10]],
"kommt": [6],
"aufgelistet": [[3,8]],
"aufrufen": [10],
"übersetzend": [6,[5,9,11]],
"versetzt": [11],
"künftig": [8],
"umbenannt": [6],
"entsprechen": [11,4],
"segmentierungsoptionen": [11],
"kurz": [[1,6],[2,4,11]],
"copyright": [8],
"springt": [8],
"notiz": [8,[3,9]],
"reinen": [6],
"zählungen": [8],
"auswirkung": [11],
"kommentaren": [11,9],
"system-os-nam": [11],
"omegat-benutzeroberfläch": [[1,6]],
"optionstabadvancecheckboxmenuitem": [3],
"mischen": [6],
"kenntlich": [8],
"damit": [11,[6,10],8],
"heapwis": [11],
"optionsviewoptionsmenuloginitem": [3],
"standardregeln": [11],
"hervorheben": [8,3,11,1],
"zugeordnet": [6],
"festzulegen": [11],
"programmen": [11],
"tar.bz2": [0],
"halbbreit": [11],
"bereitgestellten": [11,6],
"sicherungskopien": [[6,10]],
"textersetzung": [11,7],
"segmentierungsregeln": [11,[2,6],10],
"bundle.properti": [6],
"entschieden": [11],
"nein": [5],
"dritten": [1],
"odt-format": [6],
"x64": [5],
"tastenereigniss": [3],
"fensterbereich": [9,8,11,10],
"einbezogen": [11],
"keyev": [3],
"schritt": [6,11,10],
"beeinträchtigen": [11],
"fensterbereiche": [7],
"empfohlen": [11],
"isn\'t": [2],
"festlegen": [11],
"verfügen": [5],
"ander": [6,5,11,9,[8,10],0],
"spiegelt": [[8,10]],
"welchem": [[4,5]],
"zweiten": [[1,6,9]],
"betriebssystem": [5,11,8],
"auto-synchronisieren": [11],
"lizenzvereinbarung": [5],
"erstinstal": [5],
"wortpaar": [11],
"optionsteammenuitem": [3],
"ausdrücken": [11,[2,9]],
"segmentweis": [11],
"gzip": [10],
"stellen": [5,[4,11]],
"importieren": [6,[8,10,11]],
"markennam": [[9,11]],
"esc": [11,1],
"vorgegeben": [5],
"xml-basierend": [11],
"x86": [5],
"doc-datei": [6],
"zulässt": [6],
"einzeln": [11,2],
"nostemscor": [11],
"rechtschreibungsproblem": [8],
"grupp": [2],
"ersetzen": [11,8,3,9],
"console-createpseudotranslatetmx": [5],
"arbeitsablaufbezogen": [9],
"schriftart": [11,3],
"etc": [5],
"longman": [0],
"fuzzyflag": [11],
"taas-sammlungen": [11],
"neu": [11,6,[3,8],1],
"drag-und-drop": [5,[7,9]],
"überlässt": [11],
"merriam": [0,[7,9]],
"schnellstart-handbuch": [5],
"dateifiltern": [11],
"iceni-infix-filt": [6],
"ignoriert": [11,[3,5,8]],
"müssen": [11,6,5,[4,10],[1,2,8,9]],
"tool": [6,[2,8,11],10,[3,7]],
"gedrückt": [[3,8,11]],
"bekannt": [[6,11]],
"unterordn": [10,[6,11],5,[0,4]],
"geänderten": [11],
"intervall": [11],
"aktiv": [8,[9,11],3],
"wiederholen": [[3,8,9]],
"grund": [4,8],
"seien": [6],
"umzuschalten": [11],
"satzanfang": [2],
"uneindeutigen": [11],
"titelschreibung": [8,3],
"möglichen": [6,[3,5]],
"n.n_without_jre.zip": [5],
"wissen": [11],
"ungültig": [6],
"gilt": [5,[8,9,11]],
"magento": [5],
"darstellungsmodi": [6],
"opendocument-dateien": [11],
"zeilenvorschubzeichen": [2],
"offlin": [6,5],
"vordefiniert": [11,[2,5]],
"pt_br-wörterbücher": [4],
"u00a": [11],
"sonst": [6],
"untereinand": [6],
"offizielle": [7],
"verhältni": [9],
"editorbereich": [9,11,8,[1,10]],
"vorschlag": [11],
"shift": [3],
"nie": [11],
"erneut": [[5,6,8,11]],
"vorgehensweis": [6],
"java": [5,[3,11],[2,6,7]],
"rechtschreibung": [4],
"alphabetisch": [11],
"tote": [6],
"lang2": [6],
"lang1": [6],
"ziehen": [5,9,6],
"halber": [11],
"project_save.tmx": [6,10,11],
"abrufen": [11],
"seitenvorschubzeichen": [2],
"fortschritt": [[6,9]],
"dictionari": [0,10,4],
"wichtigst": [[5,9,10]],
"beschränken": [11],
"tag-validier": [11,5],
"vorgäng": [[9,11]],
"beispiel": [11,6,2,9,[4,5],0,[3,8]],
"dictionary": [7],
"echtzeit": [9],
"umbruchregeln": [11],
"groovy-code-snippet": [11],
"schreibgeschützt": [6],
"programmiervariablen": [11],
"rückgängig": [[3,6,8,11]],
"blattnamen": [11],
"korrekturen": [11],
"kontoschlüssel": [5],
"anklicken": [[9,11]],
"dateifiltermust": [11],
"leist": [9],
"default": [3],
"anfänglich": [10],
"verlieren": [9],
"verwaist": [[9,11]],
"vorherrschend": [11],
"timestamp": [11],
"po-datei-head": [11],
"tag-validierung": [[5,6,11]],
"vorsichtsmaßnahmen": [6],
"projectaccessrootmenuitem": [3],
"dyandex.api.key": [5],
"über": [11,5,6,9,[3,8,10],[1,4,7]],
"absatzbegrenzungen": [8],
"beheben": [8],
"grau": [11],
"svg-datei": [5],
"such": [11,8,5],
"glossarbegriff": [1],
"plugin": [11],
"erforderlichen": [[5,6]],
"übersetzenden": [10,6,[9,11]],
"erkannt": [1,[8,11]],
"projektglossar": [1],
"glossar": [1,11,3,9,[6,8,10],[0,4,7]],
"editinsertsourcemenuitem": [3],
"omegat-glossardateien": [1],
"skriptnam": [8],
"viterbi": [11],
"microsoft": [11,6,[5,9]],
"projekteinstellungen": [6,8],
"projektdateien": [11,8,[3,7,9]],
"match-statistiken": [8,[3,10]],
"projectnewmenuitem": [3],
"davor": [11],
"optionstranstipsenablemenuitem": [3],
"segment": [11,8,9,3,10,1,5,6],
"davon": [5,8],
"vollständigen": [[9,11]],
"benutzeroberfläch": [5,[9,11]],
"vollständigem": [5],
"indexeinträg": [11],
"glossari": [1,[6,9,10],11],
"ignored_words.txt": [10],
"glossare": [7],
"kursiv": [11],
"configuration.properti": [5],
"github.com": [6],
"nachfolgend": [11,5],
"glossary": [7],
"neue": [11,6,5,8,4,[1,3]],
"rückgängigmachen": [8],
"gnome-benutz": [5],
"nl-en-translation-memori": [6],
"weiterverarbeitung": [11],
"zugangsdaten": [11],
"string": [5],
"darstellung": [11,9],
"eigenschaften": [[6,8,11],[0,3,4,5]],
"hälfte": [11],
"not": [11],
"besond": [6],
"logische": [7],
"repräsentieren": [11],
"öäüß": [11],
"wechseln": [[6,9,11]],
"was": [11,8,[5,6]],
"war": [8,[6,9,11]],
"tiefer": [1],
"selection.txt": [11,8],
"xhtml": [11],
"finder.xml": [11],
"ausgewählt": [5,8,11,[6,9]],
"abgrenzungsplatzhalter": [7],
"umschalt": [3,[6,11],8,1],
"herunt": [5,0],
"window": [5,[0,2,8]],
"bedingt": [6],
"glossarfunkt": [1],
"unterliegen": [6],
"disable-project-lock": [5],
"unterscheiden": [5,10],
"weiterschalten": [8,[3,11]],
"vererbt": [6],
"omegat.pref": [11],
"durchsuchen": [11,[5,8],[2,3,4]],
"übersicht": [9],
"schrift": [[9,11]],
"aufgerufen": [11],
"interferieren": [8],
"aktionen": [[5,8]],
"erzwungen": [10],
"rtl-text": [6],
"howto": [6],
"weiterbewegt": [11],
"weiterhin": [11],
"pt_pt.dic": [4],
"sollten": [11,[3,6],[2,5],[0,9]],
"scripting-featur": [11],
"level1": [6],
"level2": [6],
"po-zieldatei": [11],
"zuerst": [[9,11]],
"lässt": [11],
"flags": [7],
"abgekürzt": [11],
"gewünscht": [11,5,[8,9]],
"jeweiligen": [6],
"erfolgt": [[6,9]],
"ltr-zeichenketten": [6],
"web": [5,7],
"anleitungen": [6,[0,7,10]],
"en-us_de_project": [6],
"klammern": [11],
"liegt": [11],
"weg": [5],
"speicherkapazität": [5],
"memories": [7],
"zeilen": [11],
"maximiert": [9],
"wesentlich": [4],
"editselectfuzzy4menuitem": [3],
"editregisteridenticalmenuitem": [3],
"abgrenzungsplatzhalt": [2],
"sicherheit": [11],
"beschreibbar": [1,3],
"momentanen": [10],
"pt_br.dic": [4],
"textcursor": [8,[9,11],1],
"sprache-land-paar": [11],
"unabridg": [0],
"ausgab": [11,[3,6,8]],
"nun": [[3,6]],
"angegeben": [5,11,[1,3,4,6]],
"validiert": [8,9],
"nur": [11,8,6,5,[1,4,10],[0,3,9]],
"inhalt": [11,6,10,8,[0,5,9]],
"blick": [11],
"müsste": [11,0],
"optionsglossaryexactmatchcheckboxmenuitem": [3],
"etwa": [5,[2,6,9,11]],
"ip-adress": [5],
"absätz": [6],
"maximierungsschritt": [9],
"nennt": [5],
"ltr-sprachen": [6],
"arbeit": [5],
"älter": [6],
"gehört": [11],
"nnnn": [9,5],
"project_save.tmx.yearmmddhhnn.bak": [6],
"marker": [9],
"versionsverwalteten": [6],
"extrahiert": [11],
"allerd": [1],
"editor-bereich": [11],
"erscheint": [11,5],
"option": [11,8,5,4,[6,9,10]],
"spalten": [11,1],
"verändert": [11],
"schnell": [11],
"lokalisierungsdateien": [6],
"voller": [11],
"kein": [11,8,5,1,6,[3,4,9,10]],
"symboldateien": [5],
"wie": [11,6,5,[0,8,9],2,[4,10],[3,7]],
"impliziert": [11],
"steuern": [5],
"parallel": [5],
"zh_cn.tmx": [6],
"dies": [11,5,8,6,[9,10],[2,4],[1,3]],
"win": [5],
"wirksam": [11],
"wir": [6,11],
"suchen": [11,8,[2,5],[0,3,4,6]],
"ersetzung": [11],
"kleiner": [[5,11]],
"skriptlist": [11],
"netzwerk": [6],
"kleinen": [8],
"anmeldedaten": [11],
"archiv": [[0,5]],
"genannten": [[0,1,4,5,10,11]],
"repo_for_omegat_team_project.git": [6],
"user": [[5,9]],
"proxi": [5],
"matching-prozentsätz": [9],
"extens": [11],
"fortsetzen": [11],
"dateiform": [11,6,[8,9]],
"rand": [9,11],
"erstellt": [6,8,[5,10,11],1,[4,9]],
"alternativübersetzungen": [11],
"ganz": [11],
"nahezu": [11],
"gedruckten": [9],
"zugriffsschlüssel": [11],
"entsprechend": [11,[5,9],[1,2,6,8]],
"sicht": [9],
"po-head": [11],
"findet": [2,11,[1,3]],
"ab": [6,5,9],
"beispielprojekt": [9],
"unterbinden": [11],
"inline-tag": [11],
"diff": [11],
"finden": [11,5,2,6,8,[1,9,10]],
"al": [11,6,5,8,9,[2,4],3,10],
"am": [[9,11],6,10,[2,3,5]],
"an": [11,9,8,6,5,2,10,4,[0,1]],
"editmultiplealtern": [3],
"ziel": [6,[9,11]],
"git.code.sf.net": [5],
"formen": [11,10],
"logisch": [2],
"folg": [[10,11]],
"hierarchi": [10],
"wörterbuchlink": [0],
"globalen": [[1,11]],
"remote-omegat-projekt": [8],
"technisch": [11,8],
"direkt": [5,11,[1,8,10]],
"hauptordn": [3],
"omegat-distribut": [5],
"be": [11],
"zurückzukehren": [9,8],
"entlang": [11],
"filters.xml": [6,[10,11]],
"groß": [[3,8],11,2],
"übersetz": [6,[9,11],[8,10]],
"mindesten": [10],
"br": [11,5],
"multinational": [6],
"prozentsatz": [9,11],
"segmentation.conf": [6,[5,10,11]],
"panel": [5],
"word-vers": [6],
"wollen": [[2,4,5,6,11]],
"ca": [5],
"hilfe": [7],
"wendet": [11],
"cd": [5,6],
"ce": [5],
"systeme": [7],
"cn": [5],
"umgekehrt": [11,[2,5,6]],
"ähnlichsten": [9,11],
"flexibilität": [11],
"metazeichen": [2],
"cx": [2],
"vokalen": [2],
"apach": [4,[6,11]],
"da": [11,5,6,9],
"zwischenschritt": [6],
"adjustedscor": [11],
"dd": [6],
"extern": [11,8,[3,6]],
"f1": [3],
"f2": [9,[5,11]],
"progressiven": [11],
"f3": [[3,8]],
"dr": [11],
"f5": [3],
"dz": [0],
"veröffentlicht": [[6,8]],
"außer": [2,[6,11]],
"editundomenuitem": [3],
"oberen": [11,9],
"abgelegt": [9,8],
"insbesonder": [11],
"u000a": [2],
"sowohl": [[6,11],5,9],
"verwenden": [6,11,5,[3,8],1,9,[4,7],10],
"verwendet": [11,5,6,8,4,9,[1,2,3,10]],
"er": [[8,10],[6,9,11]],
"es": [11,5,6,9,4,8,1,10,0,7],
"u000d": [2],
"u000c": [2],
"verschwinden": [4],
"ausnahmeregeln": [11],
"frame": [5],
"u001b": [2],
"möglichkeit": [[5,6,11]],
"stats.txt": [10],
"terminologi": [[6,9,11]],
"übersetzung": [11,8,9,6,3,10,7,5],
"foo": [11],
"exclud": [6],
"for": [11,8],
"erfassen": [6],
"erwägung": [6],
"andererseit": [11],
"fr": [5,[4,11]],
"content": [5],
"ding": [6],
"desktop": [5,11],
"sobald": [8,[1,5,6],[9,11]],
"applescript": [5],
"einsatz": [6],
"gb": [5],
"chinesisch": [11,[5,6]],
"class": [11],
"helplogmenuitem": [3],
"methoden": [5,11],
"gültige": [5],
"editoverwritetranslationmenuitem": [3],
"aeiou": [2],
"form": [11,6,[3,5,8,9,10]],
"somit": [[4,6,11]],
"versucht": [[5,11],6],
"paketinhalt": [5],
"hh": [6],
"setzen": [[8,11],3],
"duser.languag": [5],
"fr-fr-wörterbuch": [4],
"java-launch": [5],
"fragt": [[5,11]],
"setup-fenst": [4],
"file-target-encod": [11],
"context": [9],
"sicher": [[5,11],4],
"https": [6,5,[9,11]],
"id": [11,6],
"bindestrichen": [5],
"darstellen": [6],
"if": [11],
"sofern": [11,[5,6]],
"project_stats.txt": [11],
"projectaccesscurrenttargetdocumentmenuitem": [3],
"geläufigen": [11],
"im": [11,5,8,6,9,1,10,4,3,[0,2]],
"in": [11,6,5,8,9,10,1,[2,3],4,0,7],
"regelmäßigen": [6],
"einstellungsdateien": [5],
"involviert": [6],
"termin": [5],
"is": [2],
"gängigen": [6],
"absatzsegmentierung": [11],
"odf": [6],
"ja": [5],
"mehrmal": [6],
"unveränderlichen": [11],
"je": [[5,11],9,8],
"indem": [11,5,9,6,3],
"odt": [11],
"gotonexttranslatedmenuitem": [3],
"wider": [[8,10]],
"leerraum": [[3,8,11]],
"nplural": [11],
"unterteilt": [[9,11]],
"js": [11],
"macos-benutzer": [7],
"zuvor": [[4,8,11]],
"learned_words.txt": [10],
"traditionellen": [5],
"meldung": [5],
"ftl": [5],
"vollständig": [11,[3,5,6]],
"frage": [6],
"viewdisplaymodificationinfoallradiobuttonmenuitem": [3],
"draw": [6],
"java-regex-dokument": [2],
"omegat-tastaturkurzbefehl": [3],
"ausnahmemuster-dialogfenst": [11],
"darunt": [11],
"ablauf": [[6,11]],
"versteckt": [10],
"oft": [11],
"dswing.aatext": [5],
"grauen": [8],
"quellkodierung": [11],
"speicherung": [6],
"projektspezifisch": [11,10,8],
"lu": [2],
"skripte": [7],
"bestätigt": [10],
"nachzuvollziehen": [6],
"glossareinträg": [[1,3,11]],
"cycleswitchcasemenuitem": [3],
"wortstamm": [11,8],
"allgemeinen": [11],
"mb": [5],
"führen": [11,[5,8]],
"me": [6],
"power-featur": [11],
"drei": [6,10,[0,9],1],
"omegat.png": [5],
"anweisen": [4],
"mm": [6],
"entri": [11],
"matching-prozentsätzen": [9],
"nachdem": [6,5,3],
"schlüssel-wert-paar": [11],
"mr": [11],
"ms": [11],
"mt": [10],
"bestehen": [1],
"regex-tools": [7],
"my": [6,5],
"wichtig": [6,11],
"berechnet": [[9,11]],
"ocr-programm": [6],
"aktuel": [8,11,9,3],
"web-start-versionen": [5],
"ohn": [11,9,5,6],
"kodierungsschema": [11],
"überhaupt": [[10,11]],
"funktionalität": [11],
"updat": [11,8],
"offenkundig": [9],
"basierend": [11,8],
"hilfsprogrammen": [0],
"nl": [6],
"erhöhen": [11],
"lokalen": [6,11,5],
"nn": [6],
"no": [11],
"code": [5,[4,11]],
"gotohistoryforwardmenuitem": [3],
"betrachten": [6],
"gesuchten": [6],
"blau": [9],
"ob": [11,5,6,[4,8,10],9],
"aufgrund": [10],
"noch": [11,[5,8],[6,10]],
"of": [0],
"sammlungen": [11],
"ok": [[5,8]],
"projekt-memori": [11],
"aufweist": [[6,10]],
"auskennen": [6],
"auswahl": [8,11,3,4,[5,9]],
"os": [[6,11]],
"zieldokumenten": [11],
"repariert": [6],
"möglichkeiten": [[6,11],5,4],
"zieltexten": [11],
"derzeit": [11],
"erscheinung": [11],
"editinserttranslationmenuitem": [3],
"pc": [5],
"abkürzung": [11],
"pdfs": [6],
"system-hostnam": [11],
"sehen": [6,11,[3,9]],
"paragraph-tag": [11],
"po": [[5,9]],
"erkennbaren": [11],
"repository-typ": [6],
"auffüllen": [6],
"optionsglossarystemmingcheckboxmenuitem": [3],
"folienkommentar": [11],
"pt": [5],
"verschieben": [[9,11]],
"ausschließen": [11],
"einträg": [[1,5,11]],
"äquivalent": [[8,9]],
"config-datei": [5],
"häkchen": [11],
"verfügt": [11,6,4],
"notwendigen": [0],
"fehlen": [9],
"edit": [8],
"zellen": [11],
"editselectfuzzy5menuitem": [3],
"angeben": [5,11],
"fehlerbehebungen": [8],
"exakt": [11,1],
"batch-modus": [5],
"rc": [5],
"zeilenend": [2],
"includ": [6],
"fehler": [[5,6]],
"minut": [6],
"windows-systemen": [5],
"unterstreichen": [1],
"sc": [2],
"formatierung": [6],
"validieren": [3,[6,11]],
"darstellungsmodus": [6],
"content-attribut": [11],
"zahl": [[5,9,10,11]],
"durchschnittsbewertung": [11],
"versuchen": [11,2,[5,6]],
"projektleit": [6],
"farblich": [8],
"so": [6,11,5,[2,8,10],9],
"gedacht": [6],
"starten": [5,11,[3,6,7]],
"referenzglossar": [1],
"intern": [11],
"word-dateien": [6],
"editoverwritesourcemenuitem": [3],
"nachricht": [6],
"enforc": [10],
"erweist": [6],
"remov": [5],
"tm": [10,6,[8,9],[5,7,11]],
"startet": [5,8],
"to": [[0,5,11]],
"möchten": [11,6,5,3,8],
"v2": [5],
"omegat-projekt": [6,[5,10]],
"können": [11,5,6,9,8,[4,10],1,3,[0,2]],
"document.xx": [11],
"tw": [5],
"berücksichtigen": [8],
"standardübersetzungen": [10],
"viewmarkautopopulatedcheckboxmenuitem": [3],
"projectwikiimportmenuitem": [3],
"countri": [5],
"trivial": [6],
"glossarbereich": [9,1,11],
"um": [11,5,6,[8,9],1,10,4,2,[0,3]],
"kanadisch": [11],
"anfang": [[2,11],[0,1,3,4,5,6,8,9,10]],
"russischen": [5],
"benutzung": [11],
"this": [2],
"kennzeichnen": [8,[3,11]],
"wörtern": [11,[1,9]],
"kontrollkästchen": [11,5],
"verbesserungen": [8],
"kennzeichnet": [8],
"vi": [5],
"umgebung": [5],
"textsuche": [7],
"prüfregeln": [11],
"standardglossar": [1,7],
"vs": [9,11],
"glück": [11],
"übersetzungsprogramm": [5],
"bestehenden": [11],
"sein": [11,6,5,1,9,[0,3],[2,4,8]],
"ändert": [10,[5,8,11]],
"regex-tool": [2],
"schließen": [11,8,[3,6]],
"seit": [11,8,[3,4,5]],
"herauszufinden": [11],
"ändern": [11,5,6,[8,9],3,[1,4]],
"segmentieren": [11],
"rechts-nach-links-sprachen": [6,7],
"bedarf": [6],
"anzupassen": [11],
"dann": [11,5,[4,10],6,[0,3,9]],
"tm-ordner": [6],
"zuständ": [6],
"wo": [5,6],
"groovy.codehaus.org": [11],
"regelbasiert": [11],
"entsprechenden": [11,6,8,[3,4,10]],
"abgeschlossen": [9],
"repo_for_omegat_team_project": [6],
"exe-datei": [5],
"hauptprojektordn": [6],
"linux-benutz": [5],
"und-logik": [11],
"umfasst": [[5,6]],
"emac": [5],
"org": [6],
"filtern": [11,6],
"veraltet": [6],
"distribut": [5],
"sehr": [11,6],
"xf": [5],
"ort": [[4,6]],
"jeder": [[5,10,11],[6,8]],
"jedem": [11],
"jeden": [11],
"dauern": [4],
"speziellen": [11,6],
"entspricht": [11,8,2],
"bezogen": [5],
"xx": [5,11],
"xy": [2],
"sourc": [6,10,11,[5,8],9],
"enden": [1],
"greedy-quantoren": [2,7],
"endet": [[0,5,8,9]],
"segmentierung": [11,8,[2,3]],
"type": [6,3],
"fußzeilen": [11],
"eigenständigen": [2],
"suchbegriff": [11],
"toolssinglevalidatetagsmenuitem": [3],
"problem": [1,[6,8],0],
"vorliegen": [11],
"projectaccesssourcemenuitem": [3],
"stattdessen": [6],
"vollbreiten": [11],
"beliebigen": [[8,11],[9,10]],
"yy": [9,11],
"method": [5,11],
"einig": [[6,11],[0,5],[1,8,9]],
"anlaufstell": [11],
"leichter": [6],
"ausdrücke": [7],
"push": [6],
"zh": [6],
"zuordnungsparamet": [6],
"versehentlich": [11],
"readme_tr.txt": [6],
"penalti": [10],
"personalisieren": [11],
"zu": [11,5,6,9,8,10,4,3,[1,7],2,0],
"oracle-dokument": [11],
"verlust": [6],
"geladen": [6,[1,5,8,11]],
"utf8": [1,[8,11]],
"aktiven": [[9,11],10],
"sozusagen": [6],
"dateikontext": [11],
"tools": [7],
"out": [6],
"suchkriterien": [11],
"filterkonfigur": [11],
"vermieden": [11],
"dark": [11],
"softwarebezogen": [11],
"bezieht": [[10,11]],
"power": [11],
"überprüfen": [[6,11],4,[0,5,9,10]],
"rtl-modus": [6],
"darf": [[3,11]],
"speicherplatz": [5],
"docs_devel-ordn": [5],
"quellen": [11,6],
"tag-valid": [5],
"chinesischen": [6],
"uhrzeit": [[8,11]],
"anzeigen": [[8,11],3,[5,10]],
"quelldokumenten": [[9,11]],
"unterstützen": [6],
"u0009": [2],
"xhh": [2],
"speicherzuweisung": [5],
"stichwörtern": [11],
"revis": [0],
"u0007": [2],
"danach": [11,9],
"repositori": [6,[8,10],5],
"minimum": [11],
"lowercasemenuitem": [3],
"tabell": [2,3,11,9,[1,8]],
"firefox": [[4,11]],
"ggf": [10,[4,11]],
"wiki": [9],
"standard-font-fallback": [8],
"separ": [[1,11],9],
"beibehalten": [11],
"gewünschten": [5,[0,6]],
"angepasst": [6],
"deutschen": [11],
"wiederholungen": [11,8,9],
"maxim": [3],
"solchen": [6,10],
"hinzufügen": [11,5,6,10,[3,8,9]],
"ereigni": [3],
"einem": [6,11,5,8,10,[2,4,9],1,[0,3]],
"dass": [11,5,6,10,4,0,[1,9],8],
"einen": [11,6,5,8,1,[4,9],10],
"konflikt": [3],
"ohnehin": [[5,11]],
"zähler": [9,7],
"einer": [11,8,5,[6,10],[1,9],2,4],
"gestartet": [5,11,[2,3,8]],
"openoffic": [4,11],
"regulär": [11,2,[3,4,5]],
"quasi": [6],
"geschriebenen": [4],
"liefert": [11],
"optionsautocompletechartablemenuitem": [3],
"link": [6,11],
"servern": [11],
"praktisch": [9],
"basieren": [[0,2]],
"mitteilen": [6],
"git": [6,10],
"projektsprachen": [[6,8]],
"xx-yy": [11],
"passend": [11],
"durchführt": [8],
"zip-datei": [5],
"wiederholt": [11],
"handhabt": [11],
"bereich": [11,8,[2,6,9]],
"mauszeig": [8],
"repository-ordn": [6],
"gewährt": [8],
"durchsucht": [11],
"arbeitsordn": [5],
"optionsspellcheckmenuitem": [3],
"suchoptionen": [11],
"optionssetupfilefiltersmenuitem": [3],
"verwendeten": [11,5],
"regex-funktionalität": [2],
"vornherein": [10,11],
"altgraph": [3],
"installationsanweisungen": [[5,7]],
"vorhandenen": [[6,11]],
"sollen": [11,[5,8],6],
"entfernt": [11,10,4],
"without": [5],
"gesplittet": [11],
"anwendungen": [[4,11]],
"seiner": [[0,5,9]],
"abdocken": [9],
"darstellungsweis": [11],
"seinen": [9],
"beginn": [11,[5,10]],
"xmx": [5],
"höchste": [9],
"lassen": [11,6],
"übernehmen": [3],
"entfernen": [11,[5,6],[3,4,8,9,10]],
"optional": [1],
"proxyserv": [5],
"korrigieren": [11],
"geleert": [[8,11]],
"sekunden": [11],
"senden": [6,11],
"befor": [5],
"erkannten": [11],
"standardansicht": [6],
"ausdrück": [11,2,[3,4,5]],
"anwendungsbeispiele": [7],
"tar.bz": [0],
"aufgeschlüsselt": [11],
"benutzerhandbuch": [7,5],
"klicken": [11,5,8,[4,9],6],
"rechtsklickmenü": [11],
"späteren": [[6,10]],
"fixiert": [9],
"ignorieren": [11,[4,8,10]],
"folienmast": [11],
"hinzugefügt": [[1,6],10,8],
"dazu": [[5,6,9,10,11]],
"angezeigten": [[9,11]],
"unterstrichenen": [1],
"großen": [[4,5]],
"kontext": [9,[6,8,11]],
"füllen": [10],
"xlsx": [11],
"anzumelden": [5],
"suchen-und-ersetzen-fenst": [[8,11]],
"assembledist": [5],
"natürlich": [[4,10],[5,6,9,11]],
"kunden": [6,10],
"bearbeiten": [11,9,8,5,[1,4,6,7]],
"zieltext": [11,8,9,[1,5,6,10]],
"target.txt": [11],
"omegat-teamprojekt": [6],
"vertraut": [[5,6,11]],
"standard": [[6,11]],
"tastenkombin": [6,[3,11]],
"tastatur": [[3,9]],
"odt-zieldatei": [6],
"installieren": [5,4,[0,7]],
"sätze": [11],
"optionen": [11,[8,9],[4,5],6,[2,3,7,10]],
"abgerufen": [[5,11]],
"nameon": [11],
"committen": [6,8],
"kontroll": [6],
"optionsglossarytbxdisplaycontextcheckboxmenuitem": [3],
"minimiert": [9],
"gotonextnotemenuitem": [3],
"tar.gz": [5],
"yearmmddhhnn": [6],
"verlassen": [11,[8,10]],
"durchzuschalten": [8],
"bidirekt": [6],
"list": [11,8,[2,5,6,10]],
"speichermeng": [5],
"wird": [11,5,6,9,8,10,1,4,[2,3]],
"quelldateinamensmust": [11],
"letzt": [8,3,[5,10]],
"omegat-spezifisch": [10],
"niederländischen": [6],
"azur": [5],
"zurückversetzt": [6],
"zögern": [6],
"fehlübersetzung": [11],
"java-eigenschaften": [11],
"verknüpfung": [5],
"popup-menü-symbol": [8],
"ausrichtungsparamet": [11],
"html-dateien": [11,5],
"erzeugt": [8,[6,11]],
"archivieren": [6],
"erscheinen": [5,11,[4,6,9]],
"umgeht": [11],
"niederländisch": [6],
"unterschiedlichst": [8],
"gesteuert": [11],
"verzeichnisstruktur": [10],
"git-repositori": [6],
"gekennzeichnet": [8],
"niedrig": [[9,11]],
"with": [5,6],
"maschinelle": [7],
"definiert": [[8,11]],
"pdf": [6,8],
"losgelassen": [3],
"tastaturkurzbefehldefinit": [3],
"toolsshowstatisticsmatchesmenuitem": [3],
"viewdisplaymodificationinfononeradiobuttonmenuitem": [3],
"tags-verwaltung": [6],
"überprüfungen": [8],
"gesendeten": [11],
"gemeinschaft": [6],
"festplatt": [[5,6,8]],
"einrichten": [[6,7,11],[4,8]],
"tabellenverzeichnis": [7],
"erzwungenen": [10],
"spielt": [5],
"leerräum": [11,8],
"per": [11,[5,9]],
"großschreibung": [8,[3,5]],
"folgt": [5,6,11,[3,4,8,9]],
"lage": [11,[6,9]],
"obwohl": [[4,11]],
"dieselb": [11],
"rtl-darstellung": [6],
"texten": [[6,11]],
"typisch": [[5,6]],
"projectaccesswriteableglossarymenuitem": [3],
"genannt": [[5,11]],
"befinden": [[1,11],[5,6,8]],
"befindet": [5,[6,11],[1,8,10]],
"gui": [5],
"definierten": [[9,10]],
"regexp": [5],
"startoptionen": [5],
"sentencecasemenuitem": [3],
"instanz": [[5,9]],
"datenverlust": [6,7],
"xhtml-dateien": [11],
"eingabefeld": [11,5,2],
"verworfen": [6],
"uhhhh": [2],
"ungefähren": [11],
"optionssentsegmenuitem": [3],
"schnittstellen": [6],
"robust": [6],
"zeitpunkt": [6,10],
"einmaligen": [11,9],
"endnoten": [11],
"optionsaccessconfigdirmenuitem": [3],
"charact": [6],
"open-source-lizenz": [6],
"bleiben": [11,[5,10]],
"test.html": [5],
"textlich": [6],
"namen": [11,5,10,6,4,9,[0,1]],
"wahl": [[5,11]],
"xxx": [10],
"übersprungen": [11],
"smalltalk": [11],
"operiert": [11],
"satzsegmentierung": [11],
"brasilianisch": [[4,5]],
"oben": [11,5,[6,9,10],[0,1,2,4,8]],
"explizit": [[8,11]],
"textdateiform": [6],
"pseudotranslatetmx": [5],
"löschen": [11,6,[5,9,10]],
"aktion": [8,3,0],
"abzugleichen": [11],
"fensterbereichen": [9],
"spezifiziert": [11],
"wortzeichen": [2],
"einschließt": [6],
"targetlanguagecod": [11],
"behoben": [1],
"funktioniert": [8,[4,11],[5,6]],
"vorteil": [[5,11]],
"privaten": [5],
"zugreifen": [3,[6,8]],
"schlecht": [11],
"manuell": [11,[4,8]],
"bidi-algorithmus-steuerzeichen": [[3,8]],
"programmierwerkzeug": [11],
"versionsverwaltung": [6],
"kontextmenüpriorität": [11],
"inspiriert": [11],
"voll": [6],
"zusammenfügt": [11],
"autotext-optionen": [11],
"gemäß": [11,10,[4,5]],
"beachtet": [9],
"extra": [11],
"land": [5],
"anzuweisen": [4],
"lang": [11,5],
"versteht": [6],
"ihnen": [11,5,[6,8],9],
"untermenü": [5],
"begriff": [1,11,[3,9]],
"beachten": [5,[6,11],[4,10],1],
"empfiehlt": [[6,11]],
"einfach": [11,2,[4,5,6],[1,9]],
"datenschutz": [5],
"encyclopedia": [0],
"ober": [[2,9]],
"sprachen": [11,6,4],
"fensterbereich-widget": [9],
"unübersetzt": [11,8,3,[9,10]],
"platzhaltersuch": [11],
"optionstagvalidationmenuitem": [3],
"obig": [[6,11],5],
"hinter": [11],
"pt_br": [4,5],
"a-z": [2],
"reihenfolg": [11],
"markiert": [8,11],
"absätzen": [8],
"verwechseln": [11],
"onlin": [4],
"ganzer": [11],
"statusleiste": [7],
"verbindung": [6,4],
"passwort": [11,6],
"fuzzy-match": [11,8],
"omegat-homepag": [6],
"angesprochen": [11],
"freie": [8],
"darstellungsrichtung": [6],
"javascript": [11],
"mediawiki": [11],
"input": [11,6],
"muss": [6,5,3,[1,11],[4,9]],
"vorgehen": [6],
"wann": [6],
"proxy-serv": [[5,11]],
"projekt-menü": [3,7],
"yandex-translate-api-schlüssel": [5],
"neuest": [6],
"hexadezimalen": [2],
"referenzen": [2],
"wieder": [11,8,[6,9],10],
"generel": [9,[2,11]],
"quellcodes": [7],
"verlangsamen": [6],
"würden": [5],
"omegat-objektmodel": [11],
"kontoseit": [5],
"kostenlosen": [[4,7,11]],
"kopi": [6,[8,10,11]],
"außerhalb": [[5,6,9]],
"zieldoku": [8,[3,6]],
"googl": [5,11],
"xhtml-filter": [11],
"wiederfinden": [[10,11]],
"sichergestellt": [11],
"download.html": [5],
"vorn": [11],
"angeboten": [9],
"align": [11],
"vorgeschlagen": [9],
"sourceforg": [3,5],
"exportiert": [11,6,8],
"hat": [11,[1,4,5,6,8],[9,10]],
"hintergrund": [[8,10],9],
"struktursegmentierung": [11],
"betroffenen": [4],
"unterschiedlichen": [8],
"beschreibbaren": [8,1],
"editmultipledefault": [3],
"mozilla": [5],
"editfindinprojectmenuitem": [3],
"pro": [[3,8],5],
"warn": [5],
"technetwork": [5],
"aktiviertem": [11],
"aktuellst": [5],
"plural": [11],
"tsv-datei": [1],
"zahlreich": [6],
"umbruchregel": [11],
"bedingungslo": [10],
"rtl-dokument": [6],
"nachträglich": [10],
"projekten": [11,[1,6]],
"kenntniss": [6],
"oberst": [9],
"kompatibel": [5],
"zeigt": [8,11,9,[0,2]],
"schreiben": [6],
"kombiniert": [[5,11]],
"n.n_windows.ex": [5],
"benutzerdefiniert": [11,[3,8]],
"tipp": [[5,6]],
"referenzübersetzungen": [6],
"suchfeld": [11],
"anpassung": [3,7,2],
"program": [5],
"beziehen": [[0,9,11]],
"quellsprachen": [6],
"kontextmenü": [11,9,[1,4]],
"her": [[6,8],9],
"präsentationsnotizen": [11],
"innerhalb": [10,[6,8,11],9],
"berücksichtigt": [11],
"windows-benutz": [5],
"erstellung": [6,11],
"ausgeblendet": [11],
"ergreifen": [6],
"remote-ordn": [6],
"stimmen": [1],
"gefärbt": [9,10],
"nicht": [11,6,5,8,1,9,4,10,2,[0,3]],
"angeordnet": [9],
"ausführen-dialogfenst": [5],
"n.n_mac.zip": [5],
"maschinelle-übersetzung-bereich": [9],
"prioritätsreihenfolg": [11],
"bak-dateien": [6],
"standardverhalten": [[5,11]],
"system-benutzernam": [11],
"aufgebaut": [8],
"denken": [11],
"engin": [6],
"rechtschreibwörterbüch": [4],
"inhalten": [5],
"ordnungsgemäß": [5],
"gemacht": [[5,6,8]],
"thema": [6,10,11],
"escapezeichen": [5,2],
"begrenzen": [11],
"theme": [11],
"einsprachigen": [11],
"markennamen": [11],
"pseudotranslatetyp": [5],
"editor": [11,[8,9],[6,7]],
"kodierungsdeklar": [11],
"definitionsdatei": [3],
"kommentare": [7],
"layoutgründen": [11],
"befehlszeil": [5,6],
"vonstattengeht": [11],
"hinweis": [4,6],
"gesetzt": [[6,11]],
"listet": [11],
"fertigstellung": [11],
"gewechselt": [6],
"hauptfenster": [7],
"projectclosemenuitem": [3],
"gelten": [8,[5,6]],
"nicht-teamprojekt": [6],
"hin": [6],
"viewmarknonuniquesegmentscheckboxmenuitem": [3],
"listen": [11],
"praxi": [5],
"tonsignalsteuerzeichen": [2],
"liegend": [11],
"config-dateien": [5],
"vergleichsmodus": [11],
"radiobutton": [11],
"maschinellen": [11,[8,9]],
"nachbearbeitungsbefehl": [11],
"hoch": [8,11],
"zweistellig": [6],
"durch": [11,9,[5,6,10]],
"group": [9],
"demzufolg": [11],
"findinprojectreuselastwindow": [3],
"readme.txt": [6,11],
"gerad": [[8,9]],
"aufzurufen": [11],
"source.txt": [11],
"files.s": [11],
"ini-datei-beispiel": [5],
"readme-dateien": [11],
"vorstellen": [11],
"exchang": [1],
"desto": [11],
"konfigurieren": [11],
"segmentmark": [9],
"zugriffsberechtigung": [6],
"request": [8],
"sicherungskopi": [6],
"kodierung": [11,1,7],
"currseg": [11],
"prüfen": [[0,4],6],
"immer": [11,1,[2,3,4,8]],
"unterschied": [11,6],
"point": [11],
"liegenden": [1],
"omegat-instal": [5],
"sprachcod": [4,11],
"vertrauenswürdigen": [11],
"os-abhängig": [1],
"komplexer": [2],
"vermittelt": [11],
"stärken": [11],
"absicht": [11],
"repository-url": [6],
"fortfahren": [6],
"attribut": [11],
"zukünftig": [6],
"downloaded_file.tar.gz": [5],
"kapitel": [[2,9,11]],
"folgen": [5,[2,6,8]],
"proxy-host-portnumm": [5],
"geparst": [6],
"auszurichten": [11],
"verwei": [6],
"teilweis": [9],
"wörterbüchern": [4,0,8,[7,9]],
"account": [11],
"dhttp.proxyhost": [5],
"translate-dienst": [5],
"aktuellen": [11,8,9,[6,10],[1,5]],
"unbrauchbar": [11],
"angegebenen": [11,5],
"vergangenheit": [6],
"zahlenpaar": [9],
"korrigiert": [11],
"konsonanten": [2],
"gelassen": [11],
"unterscheidung": [2],
"modifik": [3],
"bevor": [6,[8,11],4],
"yandex-konto": [5],
"angemeldet": [5],
"konstrukt": [2],
"wirkung": [11],
"hinzuzufügen": [6,11,4],
"höchsten": [9],
"stell": [[9,10]],
"configur": [5],
"filteroptionen": [11],
"befehl": [5,[8,11]],
"quellordn": [6,11,[3,5,8]],
"optionsworkflowmenuitem": [3],
"omegat-team": [7],
"releas": [6,3],
"po-filt": [11],
"leerzeilen": [11,3],
"translation-memory-tool": [[6,11]],
"tastaturkurzbefehl": [3,8,11,2],
"verzeichni": [8,6],
"sparc": [5],
"ant-syntax": [11],
"auslassen": [6],
"öffnenden": [11],
"zeilenumbrüchen": [11],
"gesammelt": [10],
"omegat-hauptfenst": [11],
"segmentnumm": [8,3],
"identifizierung": [11],
"relevanten": [5],
"wodurch": [11],
"tastenkombinationen": [3],
"vorspringen": [8],
"tag-problem": [8],
"sogar": [[6,8,9]],
"segmentumbruch": [11],
"vorsichtig": [6],
"platzhaltern": [6],
"fenster": [11,8,9,6,[4,5,7]],
"notwendig": [6,[4,5]],
"fuzzy-matches-bereich": [[7,8,11]],
"beim": [11,5,6,2],
"drag-and-drop": [[5,9]],
"besten": [11,[6,10]],
"mehr": [11,2,[5,6,8,10],[3,9]],
"subdir": [6],
"pfeilschaltfläch": [11],
"befehlszeileneditor": [5],
"handelt": [[9,11],[5,6]],
"ebenfal": [5,[1,3,11]],
"zeicheneingabesystem": [11],
"benutzen": [[4,11],[5,7,8],6],
"gleichen": [11,6,[0,2,5,10]],
"sieht": [5],
"zeichentabell": [11,3],
"ausnahmen": [11],
"aber": [6,11,5,[1,9],[2,4,8,10]],
"spalt": [1,[8,11],9],
"behalten": [[10,11]],
"übersetzungsphas": [10],
"gehen": [[0,4,11]],
"ausgabedateien": [[6,11]],
"proxy-host-ip-adress": [5],
"forward-backward": [11],
"pfad": [5,6],
"beschrieben": [11],
"türkisfarben": [8],
"beid": [[5,11]],
"steht": [5,1],
"drittsprachig": [9],
"befehlszeilenmodus": [5],
"file-source-encod": [11],
"großbuchstaben": [[2,8,11]],
"some": [6],
"generiert": [10],
"struktureben": [11],
"übereinstimmenden": [11],
"remote-serv": [10],
"anderem": [11],
"anderen": [6,5,11,9,[1,4,8],10],
"textsuch": [11],
"häufig": [[6,11]],
"matches-bereich": [9],
"passen": [11],
"ziel-locale-cod": [11],
"getrennt": [[1,11],6],
"standardfilt": [6],
"gpl-lizenz": [0],
"gemeinsam": [6],
"zielsprach": [4,6,11,5],
"verfügung": [5,[4,11],2],
"editexportselectionmenuitem": [3],
"soll": [[5,11],[4,9,10]],
"omegat-upgrad": [11],
"höher": [11,5],
"home": [6],
"macos-dienst": [5],
"genügend": [10],
"entf": [11,9],
"varianten": [[2,10,11]],
"bildschirm": [5],
"projectaccesstargetmenuitem": [3],
"aktuell": [8,3,[5,6,9,11]],
"segmentidentifik": [11],
"tar-befehl": [0],
"synchronisieren": [[5,6]],
"leeren": [11,6],
"beispielen": [5],
"kompilieren": [5,7],
"aligndir": [5],
"system-host-nam": [11],
"action": [8],
"zweit": [[3,5,9]],
"glyphen": [8],
"größere": [11],
"fremden": [11],
"creat": [11],
"python": [11],
"es_mx.dic": [4],
"verknüpfungen": [5],
"infix": [6],
"segmentiert": [11],
"tabulatorzeichen": [1,2],
"tarbal": [0],
"omegat-projekt-quellordn": [6],
"üblichen": [[6,8]],
"hervorgehoben": [8,9],
"projektpaket": [8],
"häufigsten": [9],
"match-prozentsätz": [10],
"bevorzugten": [9],
"algorithmen": [11],
"kompilierung": [5],
"läuft": [[5,6]],
"file": [11,6,5],
"ergeben": [11],
"wortgrupp": [11],
"beispielzuordnungen": [6],
"klein": [[8,10]],
"meng": [11],
"erwähnt": [[5,6]],
"zuordnungen": [6],
"instanzen": [[5,8,11]],
"menu": [9],
"ersetzt": [8,[6,11]],
"umschalten": [6,8],
"a-za-z": [2,11],
"unserem": [6],
"med-paket": [8],
"farben": [[8,11],3],
"source-pattern": [5],
"übersetzungsprojekt": [11],
"ausgeführt": [11,[5,8],9],
"obigen": [[6,9],11,[2,4]],
"segmenttext": [11],
"internetseiten": [11],
"benutz": [[5,11],[8,9],[2,3,6]],
"ursprünglich": [[6,11]],
"probleme": [7],
"sichtbar": [[10,11]],
"angezeigt": [11,8,9,[1,10],5,6],
"dateimanag": [4],
"dorthin": [5],
"tast": [3,[1,9]],
"true": [5],
"header": [11],
"ltr-text": [6],
"menüpunkten": [3],
"gespeicherten": [[6,11]],
"groovi": [11],
"suchergebniss": [11],
"doppelklick": [5],
"deren": [[6,11],[1,8,9]],
"identifik": [3],
"suchtext": [2],
"wörterbücher": [0,4,[6,7,8,9,10,11],[1,3]],
"texteingab": [6],
"menü": [5,11,[3,8],7,6],
"kmenueditor": [5],
"abhängig": [[8,11],9],
"bestätigungsfenst": [11],
"bewirkt": [2,5],
"zweck": [11],
"verfügbaren": [11,5,[4,9]],
"segmenten": [11,6,10],
"letzten": [8,11],
"eigen": [[9,11]],
"en-nach-fr-projekt": [5],
"würde": [[5,11]],
"master": [6],
"kmenuedit": [5],
"taas-terminologi": [[8,11]],
"schaltfläch": [11,5,4],
"eindruck": [11],
"xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx": [5],
"pdf-datei": [6,7],
"taas-terminologie-lookup-fachgebiet": [11],
"quiet-modus": [5],
"writer": [6],
"beispielsweis": [[5,11],9],
"dalloway": [11],
"rubi": [11],
"signieren": [11],
"eigenen": [11,[1,2]],
"omegat-benutzereinstellungsordn": [11],
"stoßen": [6],
"existiert": [[6,11]],
"sowi": [6,5],
"verlaufsvervollständigung": [8],
"hierbei": [5,4],
"verfügbarkeitslist": [4],
"benennen": [6],
"anzeigt": [[8,11],5],
"beendigung": [11],
"team-software-versionierung": [6],
"menüpunktid": [3],
"aktiviert": [11,8,[2,4,9]],
"verlässlich": [11],
"user.languag": [5],
"anweisungstext": [11],
"regex": [2,7],
"meta": [3],
"keystrok": [3],
"suchvorgängen": [2],
"programm": [5,6,11],
"txt-endung": [6],
"einholen": [11],
"ressourcen": [6],
"funktionsweis": [6],
"später": [[5,11],[8,9]],
"global": [8,11],
"regel": [11,[2,6]],
"zeichnungen": [[6,11]],
"free": [0],
"hinzu": [6,11,[3,5]],
"lesen": [6],
"dateien": [11,6,5,10,8,4,9,0,[1,3]],
"teamarbeit": [5],
"ibm": [5],
"tm-match": [11],
"einbeziehen": [[6,11]],
"parsewis": [11],
"unterstützten": [11],
"remote-desktop-sitzungen": [5],
"odf-dokument": [11],
"zusammenfassen": [11],
"ergebniss": [11,8],
"perfekt": [6],
"ich": [5,11],
"bedeutung": [11],
"erklärung": [5],
"korrekturvorschlägen": [4],
"idx": [0],
"tastendrück": [11],
"befindlichen": [10,[8,11]],
"erhalten": [5,11,[4,6,9,10]],
"plattformspezifischen": [11],
"jede": [6,11,[8,10]],
"erstellten": [[6,8]],
"interakt": [2],
"projectaccesscurrentsourcedocumentmenuitem": [3],
"anwendung": [[4,5,6],8],
"linux": [5,[0,2,9]],
"existieren": [11,6],
"ausschneiden": [9],
"leertast": [11,1],
"kompletten": [6],
"projektname-omegat.tmx": [6],
"textformatierung": [6],
"konsolenmodus": [5],
"file.txt": [6],
"rechtsklick": [9,8],
"ifo": [0],
"folgendermaßen": [11,0],
"erzeugen": [6,11],
"zweistelligen": [5],
"tastaturkurzbefehlen": [3],
"zeilenumbrüch": [11],
"erkennen": [11,6],
"xx.docx": [11],
"textdatei": [[6,8]],
"rund": [11],
"dokument": [[6,11],8,3,9,[1,5,7,10]],
"konzept": [6],
"zeichenerkennung": [6],
"optionsautocompleteautotextmenuitem": [3],
"überschreiben": [11,[5,6,10]],
"französisch": [11,5],
"maschinel": [8,3],
"ihn": [[5,6],[8,11],[1,4,9,10]],
"ihm": [11],
"externen": [11,1],
"ihr": [5,[6,11],9,4,3,[0,2,8,10]],
"concis": [0],
"dateifilt": [11,8,[3,5,6,10]],
"verloren": [6],
"diesem": [11,10,[5,6],8,9,1],
"term.tilde.com": [11],
"diesen": [6,[8,11],[4,9,10]],
"ftp-server": [11],
"zielsprachencod": [4],
"stoppt": [11],
"geeigneten": [[4,5,6]],
"fall": [11,6,[5,9,10],8],
"viewmarknotedsegmentscheckboxmenuitem": [3],
"missfällt": [11],
"bietet": [11,5],
"algorithmus": [11],
"aufgeführt": [3],
"gruppen": [11],
"behandeln": [[6,11]],
"dessen": [11,6,5],
"ausführbar": [5],
"übersetzbaren": [11],
"näher": [11],
"gesucht": [[5,11]],
"gotomatchsourceseg": [3],
"behandelt": [11,2,6],
"optionssaveoptionsmenuitem": [3],
"excel": [11],
"comma": [1],
"omegat-tag": [6],
"runn": [11],
"wirken": [11],
"dieses": [7],
"dieser": [11,5,10,8,[6,9],4],
"stardict": [0],
"omegat.l4j.ini": [5],
"resultierend": [5,11],
"span": [11],
"deshalb": [5],
"wurden": [8,11,[1,6],9,[5,10]],
"reguläre": [7],
"struktur": [[10,11]],
"lesezeichen": [11],
"ausstehend": [11],
"nachfolgenden": [2],
"voraussetzt": [4],
"top-level-unterordn": [10],
"thunderbird": [4,11],
"editselectfuzzy3menuitem": [3],
"ähnlichkeit": [10],
"zeichenweis": [11],
"abgleichen": [5],
"übersetzungen": [11,6,9,8,[7,10],2],
"halten": [11],
"fals": [[5,11]],
"project.projectfil": [11],
"aufzubewahren": [6],
"übersetzten": [11,6,[8,9],[5,10]],
"uneingeschränkt": [5],
"zuletzt": [8,[3,6,11]],
"referenzglossaren": [1],
"überlappen": [9],
"kastilisch": [4],
"einschließlich": [11,[6,9]],
"rechten": [9,11,5],
"manuel": [[6,11],[1,4,8]],
"überspringen": [11],
"zugänglich": [[3,8,11]],
"java-implementierungen": [5],
"leistungsstarken": [11],
"shortcut": [3],
"anzahl": [11,9,8,[1,10]],
"konfigurationsordn": [3,[8,11]],
"pt_br.aff": [4],
"tmx2sourc": [6],
"hilfe-menü": [3,7],
"kanada": [5],
"zieldateinam": [11],
"eingegeben": [[8,11],[3,9]],
"remote-standort": [6],
"synchronisiert": [6,11],
"begrenzungen": [8],
"plattformübergreifend": [5],
"tritt": [5],
"dient": [5],
"dargestellten": [3],
"dhttp.proxyport": [5],
"installierten": [[4,5]],
"serverseit": [6],
"negat": [2],
"dürfte": [11],
"originaldatei": [11],
"farb": [11],
"subrip": [5],
"genauer": [[2,11]],
"authentifizierten": [11],
"übersetzen": [11,6,5,[7,8,9]],
"tabulatorgetrennten": [1],
"standardausrichtung": [6],
"textfilt": [11],
"komponenten": [11],
"versehen": [8,5,[0,3,6,9]],
"aktivieren": [11,8,[4,5],[3,10]],
"genügt": [[5,11]],
"gesagt": [11],
"score": [11],
"geeignet": [[4,5],6],
"gefolgt": [2,[3,11],6],
"verbesserungswürdig": [11],
"wiederkehrend": [5],
"rat": [6],
"raw": [6],
"physisch": [4],
"source-ordn": [[5,11]],
"schließt": [8,[6,11]],
"diagramm": [11],
"kraft": [11],
"wortvorschläg": [8],
"kombin": [[5,11]],
"kostenlos": [5],
"unten": [5,11,2,[4,6]],
"aaa": [2],
"contemporari": [0],
"solari": [5],
"spätere": [11],
"nochmal": [9],
"unter": [5,[6,8,11],9,10,[2,3,4]],
"rtl-segmenten": [6],
"beschließen": [11],
"führend": [11],
"überprüfung": [2],
"modifikationsinfo-opt": [8],
"egal": [11,[6,9]],
"abc": [2],
"rcs": [6],
"linken": [11,[4,8,9]],
"bewegen": [8],
"startmenü": [5],
"eingab": [11],
"unterverzeichni": [6],
"wobei": [9,[3,6,10]],
"unpraktisch": [5],
"bequem": [6],
"invertiert": [11],
"zuverlässig": [10],
"datenträg": [6],
"zugriff": [11,5,8,[4,6,9]],
"ordnern": [11,[1,5,6,10]],
"folienlayout": [11],
"glossardatei": [1,8],
"ist": [11,5,6,8,9,10,3,[1,4],2,7],
"windows-plattformen": [5],
"vorziehen": [11],
"zum": [11,8,[5,6],4,9,3,[2,10],0,1],
"glossary.txt": [6,1],
"zur": [5,11,6,4,[2,8,9,10]],
"prozentsätz": [9],
"interagieren": [11],
"gegensatz": [11],
"ausführung": [5,7,8],
"paket": [5],
"referenz": [[6,9]],
"add": [6],
"konvertierung": [6],
"stehen": [11,[2,3,4]],
"endung": [[0,1,11],10],
"webseit": [10],
"jeweilig": [11],
"übersetzungsarbeiten": [[9,11]],
"bedienfeld": [11],
"umlaut": [11],
"optionsautocompleteshowautomaticallyitem": [3],
"deaktivieren": [11,[1,8]],
"escapen": [[2,7]],
"benutzersprach": [5],
"larouss": [9],
"solang": [[3,11]],
"zugehörigen": [6],
"platzhalterzeichen": [11],
"ermöglichen": [[5,11]],
"besser": [11,6],
"filters.conf": [5],
"rückmeldung": [9],
"passt": [[9,11]],
"zusammen": [11],
"zieldaten": [5],
"font-fallback-mechanismus": [8],
"ausprägung": [11],
"zieldatei": [11,6],
"allgemein": [11,1],
"weshalb": [11],
"proxy-login": [11,3],
"clone": [6],
"remote-dateien": [10],
"targetlanguag": [11],
"zielordn": [11],
"falsch": [11,[4,8]],
"konkret": [5],
"kodierungen": [11],
"microsoft-konto": [5],
"folgend": [2,11,5,3,[0,6,8,10]],
"editselectfuzzyprevmenuitem": [3],
"regulären": [11,2],
"aktualisieren": [5,11],
"api-schlüssel-antragsformular": [5],
"remote-ordnern": [11],
"volumen": [11],
"script": [11,8],
"schützen": [11],
"japanisch": [11],
"system": [5,[4,11],3],
"spellcheck": [4],
"unnötig": [[6,11]],
"subtrakt": [2],
"etlich": [11],
"neben": [5,11,[0,3,4,8]],
"zunächst": [6,5,[4,11]],
"local": [6,5],
"vorgegebenen": [[8,11]],
"startskript": [5],
"einzelheiten": [[6,8],[5,11]],
"textdarstellung": [6],
"duplik": [11],
"nach": [11,6,5,8,[1,2],9,[0,3,4,10]],
"glossar-match": [1],
"repo_for_all_omegat_team_project_sourc": [6],
"repräsentiert": [6],
"oder": [11,6,5,2,8,[3,9],1,4,0,10],
"freebsd-versionen": [2],
"tm-repositori": [6],
"notieren": [11],
"ursach": [5],
"benutzerkonto": [11],
"führt": [11,6],
"sucht": [11],
"wortgrenz": [2],
"es_mx.aff": [4],
"mode": [5],
"modi": [6],
"schlüssel": [11,5],
"seiten": [6],
"toolsshowstatisticsstandardmenuitem": [3],
"geöffneten": [8],
"zielbegriff": [1,8],
"arbeitsverzeichni": [5],
"nur-text-glossar": [1],
"all": [11,6,8,5,9,3,10,[2,4]],
"read": [11],
"eingefügt": [8,11,9,[5,10]],
"alt": [[3,5,11]],
"beschreibt": [6],
"konsolenfenst": [5],
"omegat-symbol": [5],
"quellsprach": [[6,9,10,11]],
"schauen": [2],
"collect": [9],
"nahe": [6],
"wiederverwenden": [6,7],
"lesezeichenreferenzen": [11],
"sonderfal": [6],
"übereinstimmt": [[4,11]],
"vorgaben": [6],
"revisionskontrollsystemen": [6],
"wiederverwendet": [6],
"zusammengefasst": [11],
"erwartet": [[1,6,11]],
"and": [[5,6]],
"rot": [11,10],
"erwarten": [5],
"entwurf": [11],
"wahrscheinlich": [[5,11]],
"fehlerbehebung": [6],
"zahlen": [11,9,6],
"ant": [6],
"master-passwort": [11],
"projektinhalt": [3,8],
"sortiert": [[10,11]],
"alternativ": [[6,8,11]],
"teamprojekt": [6,8,[7,11],[3,10]],
"herabgestuft": [10],
"kostenlo": [5],
"eingeschlossen": [6],
"fehlend": [8,3],
"helplastchangesmenuitem": [3],
"standardbrows": [8],
"komprimieren": [11],
"erkennt": [1],
"po-datei": [[9,11]],
"zeilenumbruch": [11],
"omegat.ex": [5],
"sourcetext": [11],
"quelldokument": [11],
"erreichen": [11],
"geraden": [10],
"gui-einstellungen": [10],
"english": [0],
"erlaubt": [[5,11]],
"jar": [5,6],
"bleibt": [10,11],
"editselectfuzzy2menuitem": [3],
"startbefehlsargument": [5],
"startparamet": [5],
"dateinamensmust": [11],
"schnellstartleist": [5],
"internet-suchmaschin": [11],
"beliebig": [2,11,[9,10],[1,3,5,6]],
"alternativen": [11,[8,9]],
"standardkodierung": [[1,11]],
"angewendet": [11,[6,9]],
"rüberkopieren": [5],
"untersuchen": [8],
"ähnlich": [11,[5,6,9]],
"editselectfuzzynextmenuitem": [3],
"standard-projektzuordnung": [6],
"belieben": [5],
"gelöst": [9],
"suchen-schaltfläch": [11],
"begrenzt": [11],
"read.m": [11],
"unsegmentierten": [11],
"readme.bak": [6],
"erweitern": [11],
"basiert": [[4,5,11]],
"art": [11,[4,8]],
"translation-memory-dateien": [6],
"gehe": [[3,7],[8,9,11]],
"rtl": [6],
"setup-programm": [5],
"navigieren": [5,[4,6]],
"kontrollieren": [6],
"jdk": [5],
"buchstab": [[2,6,8]],
"übergeben": [5],
"einheit": [11],
"bestand": [[6,9]],
"tabul": [11],
"segmentierungsregelsätz": [11],
"platzieren": [[1,4,6,11]],
"mustern": [11],
"toolsshowstatisticsmatchesperfilemenuitem": [3],
"spezifisch": [[6,11]],
"run": [11,[5,6]],
"nächste": [11,[2,3,6,8,9]],
"befehlszeilenparamet": [5],
"vornehmen": [[6,11],10],
"vier": [8],
"fragen": [6],
"titlecasemenuitem": [3],
"lizenzinformationen": [8],
"editcreateglossaryentrymenuitem": [3],
"reih": [[6,11]],
"rein": [6,11,1],
"viel": [11,[2,6,8,9,10]],
"anwendungsordn": [5],
"parametern": [11],
"auf": [11,5,6,8,3,[4,9],[1,2,10],0],
"privat": [11],
"textelement": [6],
"allgemeine": [7],
"hauptfenst": [9,[3,11]],
"verknüpfen": [[5,8]],
"kohärenter": [11],
"name": [11,5,6,9],
"aus": [11,5,6,8,10,1,9,4,3],
"rechtschreibwörterbücher": [7],
"meta-tag": [11],
"systemen": [5],
"comput": [5,11],
"computergestützte": [7],
"neuesten": [10],
"darin": [11,5],
"abwärt": [11],
"übereinstimmungen": [1,11],
"enthaltenen": [11,5],
"dadurch": [[6,11]],
"gelb": [[8,9]],
"vorschläg": [[9,11],[3,8,10]],
"verhalten": [[5,11],8],
"werten": [11],
"target": [[8,10,11],7],
"spaltenüberschrift": [[8,11]],
"namensmust": [11],
"bedienungsanleitungen": [6],
"config-dir": [5],
"achten": [[5,11]],
"windows-benutzer": [7],
"omegat-fremden": [11],
"kontrolliert": [11],
"anwenden": [11],
"verknüpft": [[5,8],10],
"durchgeführt": [6,8],
"ebenso": [11],
"termbas": [1],
"platziert": [8],
"modus": [5,9],
"enter-tast": [11],
"fuzzy-matches-view": [[6,9]],
"umzubenennen": [6],
"anbiet": [11],
"vorgesehen": [8,9],
"speichert": [8,[5,6]],
"sortierreihenfolg": [[8,9]],
"schriftarten": [8],
"daran": [[10,11]],
"koreanisch": [11],
"matches": [7],
"ablegen": [5],
"standardkonfigur": [11],
"übereinstimmung": [2,[1,9]],
"targettext": [11],
"akzeptiert": [[3,5]],
"denselben": [11,[6,8,10]],
"schutz": [11],
"projektinternen": [9],
"orang": [8],
"aaabbb": [2],
"einzugrenzen": [5],
"edittagpaintermenuitem": [3],
"lokal": [6,8],
"speichern": [11,6,[3,8],5,[4,9]],
"optionscolorsselectionmenuitem": [3],
"ausschließt": [11],
"wörterbuchdateien": [4,0],
"languagetool-plugin": [11],
"kreativen": [11],
"viewmarknbspcheckboxmenuitem": [3],
"zwischen": [11,6,8,[2,10],[1,5,9]],
"dezimalzeichen": [11],
"fehlersuch": [6],
"sorgt": [11],
"derzeitig": [8],
"ausdruck": [11,2],
"bat-datei": [5],
"anordnung": [11],
"msgstr": [11],
"stichwörter": [11],
"übersetzt": [11,6,8,9,[3,10],5,4],
"separat": [11,[3,6]],
"gere": [11],
"mehrfach": [11],
"dargestellt": [11,9,1,[5,6,8,10]],
"gefahrlo": [6],
"erweitert": [[5,11]],
"lokalisiert": [5],
"anzuzeigen": [[8,11],[1,5]],
"identischen": [[10,11]],
"zwischenablag": [8],
"übersetzbar": [11],
"buchstaben": [8,11],
"iso-standard": [1],
"installiert": [5,[0,4,8],[7,11]],
"auch": [11,5,6,9,4,8,[0,10]],
"omegat.project": [6,5,10,[7,9,11]],
"hinterlegen": [11],
"tatsächlich": [8],
"excludedfold": [6],
"targetcountrycod": [11],
"beschäftigen": [11],
"sicherheitswarnungen": [5],
"webstart": [5],
"verlangt": [5],
"fertig": [8],
"vierstellig": [6],
"nicht-wortzeichen": [2],
"quelldoku": [[3,8]],
"getan": [[9,11]],
"skriptsprach": [11],
"gesamt": [11,5],
"abbildungsverzeichnis": [7],
"original": [11],
"direkter": [11],
"also": [11,5,6,[4,8]],
"auswählen": [11,3,9,5,4,[6,8]],
"millionen": [5],
"größe": [9],
"yandex": [5],
"a123456789b123456789c123456789d12345678": [5],
"ehesten": [6],
"viewmarkwhitespacecheckboxmenuitem": [3],
"verfügbar": [5,[3,8,11],6],
"überprüft": [11,[1,5,8]],
"selekt": [6],
"zieldateinamensmuster-editor": [11],
"bak": [[6,10]],
"zielsprachen": [11],
"typisiert": [11],
"schlüssel-wert-paaren": [11],
"bevorzugen": [11],
"operatoren": [[2,7]],
"bat": [5],
"offen": [11],
"rechtschreibwörterbuch": [[4,8]],
"jre": [5],
"standardzuordnung": [6],
"heruntergeladen": [5,11],
"optionsfontselectionmenuitem": [3],
"posit": [[8,11],9,[1,6]],
"versioniert": [10],
"fügen": [6,8,[3,5,11]],
"grafisch": [5],
"anzugeben": [[6,10]],
"scrollen": [11,9],
"quelldateityp": [11],
"geprüft": [11],
"projektordner": [7],
"weder": [5],
"enthalten": [11,6,[5,10],9,[3,4,8]],
"trennlinien": [9],
"terminologie-problem": [8],
"relativ": [6,11],
"schrägstrich": [[2,5]],
"eingespeist": [5],
"gescannten": [6],
"weiteren": [2],
"editierverhalten": [[3,9,10]],
"projectaccessglossarymenuitem": [3],
"verfälscht": [6],
"entpackten": [5],
"developerwork": [5],
"plattformen": [[1,5]],
"set": [5],
"meldungen": [[5,9]],
"optionsrestoreguimenuitem": [3],
"bestimmten": [11,[5,6,10]],
"kriterien": [11],
"änderungen": [6,10,5,11,8,[1,3,9]],
"quellsegment": [8,[3,6]],
"featur": [11,[6,8]],
"offic": [11],
"bei": [11,6,5,[9,10],1],
"gewöhnlich": [8],
"beginnen": [11,[2,3,5,6,8]],
"skriptsprachen": [11],
"ausgeschaltet": [8],
"benutzerordn": [[3,8,11]],
"projektordn": [[1,6],10,[0,8,9,11]],
"repositories": [7],
"verhindern": [11],
"projectsavemenuitem": [3],
"commit-vorgang": [6],
"xmx6g": [5],
"runter": [11],
"eigenständig": [5],
"verhindert": [5],
"niedrigst": [9],
"nicht-ziff": [2],
"gleichzeitig": [11,8],
"macht": [6],
"übersetzungsprozess": [11],
"qualitativ": [10],
"identifikationsdaten": [11],
"zurück": [8,[3,11],[0,1,2,4,5,6,9,10]],
"suchfenst": [11,8,6],
"lädt": [8,11],
"übersetzungseinheiten": [[10,11]],
"unerwünschten": [8],
"startprogramm": [5],
"mitgeteilt": [9],
"sie": [11,5,6,8,9,4,10,[0,1],3,2],
"aufgeteilt": [11],
"segment-call-out": [8],
"daher": [11,6,5],
"dialogfenster": [7],
"zugewiesenen": [3],
"bestätigung": [11],
"belassen": [[6,10]],
"dateiformat": [1,[7,11]],
"texteditor": [[1,5]],
"objektorientiert": [11],
"absatzbezogen": [11],
"dritt": [1],
"gesperrt": [5],
"fügt": [[8,11],6],
"sortieren": [11],
"hängt": [6,5],
"tag-bearbeitung": [11],
"anpassen": [11],
"bis": [11,2,[5,6]],
"sich": [11,5,6,10,[8,9],1,[2,4]],
"feststellen": [4],
"projectopenmenuitem": [3],
"autom": [5],
"ltr-modus": [6],
"multi-paradigma-sprach": [11],
"helfen": [6],
"funktion": [[4,8]],
"unicode-blöck": [2],
"toolsvalidatetagsmenuitem": [3],
"bearbeitungsakt": [8],
"grün": [[8,9]],
"autor": [[8,9,11]],
"beteiligt": [6],
"abgearbeitet": [5],
"fachgebiet": [11],
"beiden": [11,6,5,[4,9]],
"viewmarktranslatedsegmentscheckboxmenuitem": [3],
"valu": [1],
"zusammentragen": [6],
"zehn": [8],
"ilia": [5],
"ausgelegt": [5],
"sieh": [6,11,5,[2,4,9,10]],
"unterstrichen": [4],
"reserviert": [5],
"schreibt": [5],
"erforderlich": [5,11,2],
"einfachst": [5],
"datenspeicherung": [11],
"übersetzungsdaten": [6],
"optic": [6],
"rechtsklicken": [5,[1,4,11]],
"hier": [11,[5,6],[8,9,10]],
"richtig": [5,4],
"erstellen": [6,11,[3,5,8],1,[4,9,10]],
"editselectfuzzy1menuitem": [3],
"standort": [5,8,11],
"upgrad": [5],
"gleicht": [5],
"hide": [11],
"filternamen": [11],
"auszuwählen": [11,[5,9]],
"auto": [10,[6,8],11],
"document.xx.docx": [11],
"quelldateien": [6,11,8,[3,5]],
"relevanz": [11],
"ratsam": [11,4],
"durchgehen": [9],
"oracl": [5,3],
"wagenrücklaufzeichen": [2],
"optionen-menü": [3,7],
"setzt": [11],
"erfordern": [[8,9]],
"erfordert": [[6,11]],
"gradlew": [5],
"kann": [11,6,5,8,[9,10],[1,4]],
"angibt": [5],
"level": [6],
"wurd": [[8,11],6,[1,5],[2,3]],
"svn-programm": [10],
"remote-datei": [6],
"zeit": [4],
"eigentlich": [6,11],
"relevant": [[3,6,8,11]],
"de.wikipedia.org": [9],
"zeig": [11],
"ocred-pdf-dateien": [11],
"zeil": [[8,11],[2,3],[1,5,9]],
"quellbegriff": [1,8],
"ausgerichtet": [8,11],
"zweifel": [10],
"vorherigen": [8,3],
"tasten": [3],
"quelltext": [11,9,8,10,1,[3,5,6]],
"ursachen": [5],
"anzuschauen": [5],
"bundl": [5,11],
"immun": [10],
"für": [11,6,5,8,3,9,1,4,7,[0,2],10],
"einträgen": [1],
"erreicht": [5],
"neben-tm": [10,9],
"src": [6],
"unformatiert": [11],
"control": [3],
"kopiert": [[9,11],[6,8]],
"glossareintrag": [11,[1,3,8],9],
"speziel": [[5,9,11]],
"no-team": [[5,6]],
"git-programm": [6,5],
"nützlichen": [2],
"api-schlüssel": [[5,11]],
"kopieren": [6,[4,10],8,[5,11],[3,9]],
"keinerlei": [6],
"unübersetzten": [11,[6,9]],
"meinprojekt": [6],
"maschinell": [11,8,9],
"hilf": [8],
"einzugeben": [[6,11]],
"environ": [5],
"endungen": [0,[1,7]],
"unteren": [[9,11],[3,8]],
"optionsautocompleteglossarymenuitem": [3],
"zuordnung": [6,11],
"revisionsstatus": [10],
"stichwortsuch": [11],
"qualitätssicherung": [8],
"pdf-dateien": [6],
"ausgeschrieben": [11],
"nicht-einmalig": [11,[8,9],3],
"zeigen": [11,9],
"zusätzlich": [11,5,10,6,[2,8]],
"eintrag": [[8,11]],
"textdateien": [11,6,1],
"benutzergrupp": [6],
"kde": [5],
"denn": [5],
"tastaturkurzbefehle": [7],
"dynamisch": [11],
"arbeitsplatz": [6],
"gezogen": [9],
"languag": [5],
"berücksichtigung": [9],
"intakt": [10],
"gültig": [11],
"projekteigenschaften": [11,[1,7,8,10]],
"skripten": [11],
"key": [[5,11]],
"heißt": [8,[5,6,11],[0,1]],
"omegat-konfigurationsdateien": [8],
"standard-spanisch-wörterbuch": [4],
"svn": [6],
"quell": [11,8,6,3,9],
"programmierstil": [11],
"nutzung": [[0,5,6,7,11]],
"blauer": [11],
"stoppen": [5],
"wäre": [4],
"empfangenen": [11],
"unterstützt": [6,[5,11],8],
"editreplaceinprojectmenuitem": [3],
"symbol": [5,9],
"abständen": [6],
"switch_colour_them": [11],
"vorfinden": [5],
"eingeleitet": [2],
"entscheiden": [11],
"express": [[2,11]],
"länge": [9],
"richtet": [[2,5]],
"währungssymbol": [2],
"stilinformationen": [6],
"portugiesisch": [[4,5]],
"richten": [11],
"textabschnitt": [4],
"glossaransicht": [11],
"gotoprevioussegmentmenuitem": [3],
"formaten": [5],
"symbol-schaltfläch": [5],
"sinn": [11],
"hochschieben": [11],
"hochwertigen": [10],
"gotopreviousnotemenuitem": [3],
"editredomenuitem": [3],
"uilayout.xml": [10],
"rücktast": [11],
"wählen": [11,5,8,4,6,[1,10]],
"punktuell": [11],
"französischen": [5],
"sind": [11,6,5,[8,9],[0,1],[3,10]],
"worauf": [9],
"ausrichten": [11,8],
"gelöscht": [11,6],
"vorzunehmen": [5],
"üblicherweis": [[6,8]],
"argumenten": [5],
"quiet-opt": [5],
"wikipedia-seit": [8],
"einfluss": [6],
"repository-zugangsdaten": [11],
"normalfal": [11],
"einladung": [6],
"verhält": [10],
"besonder": [11],
"tag-fehlern": [6],
"zielseg": [11,8],
"cat-tool": [10],
"portnumm": [5],
"normal": [2,5,11,[1,10]],
"recognit": [6],
"beschleunigen": [6],
"vermutet": [11],
"einschlüss": [6],
"problemen": [[5,8]],
"grafiken": [11],
"kontextbeschreibung": [11,3],
"überein": [1],
"aggressiv": [8],
"werden": [11,6,5,8,[9,10],1,[3,4],2],
"mediawiki-seit": [[3,8,11]],
"welch": [11,5,9,[2,4]],
"integriert": [[4,11]],
"runtim": [5],
"tester": [2,7],
"treffen": [10],
"testen": [[2,6]],
"qualität": [6],
"aligner": [7],
"treffer": [11,[2,6]],
"mehrer": [11,[6,8,9],5,[1,2,4,10]],
"versionierungssystem": [6],
"produktivität": [11],
"open-source-bereich": [11],
"filenam": [11],
"fehlermeldung": [5,6],
"fehlt": [2],
"systemdatum": [11],
"bzw": [[6,11]],
"projektseit": [11],
"nbsp": [11],
"scripting-umgebung": [5],
"funktionieren": [9],
"übereinstimmend": [1],
"lieferumfang": [11],
"gotosegmentmenuitem": [3],
"russisch": [5],
"geben": [5,11,8],
"eingeben": [11,5,[6,8,9]],
"eingerichtet": [6],
"originalsprach": [9],
"ziffer": [[2,6]],
"erreichbar": [11],
"xx_yy.tmx": [6],
"gefühl": [11],
"bereitgestellt": [[5,8]],
"immunität": [10],
"flag": [2],
"helpaboutmenuitem": [3],
"verlaufsvorhersag": [8],
"segmentinformationen": [11],
"standardübersetzung": [[8,9],[3,11]],
"informationen": [5,6,11,[0,2,8,9]],
"ansicht-menü": [3,7,11],
"manuellen": [11],
"xml-dateien": [11],
"satzsegmentierungsregeln": [11],
"regular": [2],
"einzufügen": [11,1,8,9],
"passiert": [11],
"prozess": [[6,11]],
"token": [11],
"filter": [11,[5,6]],
"hingewiesen": [11],
"glossary-ordn": [11],
"editieren": [[5,11]],
"right-to-left": [6],
"argument": [5],
"pfeiltasten": [[9,11]],
"sprachprüfdienst": [11],
"vorkommt": [11],
"sagt": [9],
"ausschalten": [11],
"standardzustand": [11],
"tab": [[1,3,8],[9,11]],
"taa": [11],
"remote-repositori": [6],
"java-implementierung": [5],
"satzschreibung": [8,3],
"exportierten": [[6,8]],
"breit": [11],
"tag": [11,8,6,3,9,5],
"versionen": [[5,6],4],
"geschehen": [6],
"slovenian": [9],
"tar": [5],
"vorherig": [[3,8],[6,9,11]],
"schriftzeichen": [11],
"gewählten": [11],
"gefunden": [11,9,[1,5,6]],
"warnung": [11,[6,9]],
"projectreloadmenuitem": [3],
"erarbeiten": [2],
"richtigen": [[0,4,10]],
"originalformat": [6],
"safe": [11],
"navig": [11],
"arten": [6,[5,8,11]],
"zweisprachig": [[6,11]],
"trennt": [11],
"prozent": [11],
"winrar": [0],
"tbx": [1],
"berechnung": [11],
"leer": [11,6,[8,10],9,[1,3,5]],
"absatz": [11],
"genauso": [6],
"tabellen": [[3,6]],
"ausgewählten": [8,11,[4,6],[3,9]],
"markieren": [8,9,[5,11]],
"regeln": [11,5],
"duser.countri": [5],
"tck": [11],
"kommentar": [11,[1,9],8],
"readm": [5,11],
"betriebsmodus": [11],
"match": [11,9,8,3,10,6],
"align.tmx": [5],
"auto-vervollständig": [11,8],
"englisch": [6,2,[5,11]],
"mexikanisch": [4],
"file2": [6],
"dennoch": [[6,11]],
"makro": [11],
"ansicht": [[8,11],[1,3,7]],
"eigennamen": [11,9],
"bewegt": [[8,11]]
};
