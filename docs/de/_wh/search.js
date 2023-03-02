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
 "OmegaT 5.8.0 - Bedienungsanleitung"
];
wh.search_wordMap= {
"tmx-datei": [2,6,7],
"konvertiert": [2,[4,7]],
"administratoren": [2],
"trennen": [0,2,[1,7]],
"teamprojekt-repositori": [4,0],
"selben": [[1,7]],
"xliff-zielstatus": [0],
"wörterbuchapplik": [1],
"zwar": [7,[0,3,4]],
"überzugehen": [7],
"zudem": [[2,3]],
"ten": [4],
"automatisch": [[6,7],[1,2],4,0,3,5],
"erfolgen": [0,[2,5]],
"irgendwa": [2],
"info.plist": [2],
"trotzdem": [0,2],
"projekt-url": [2],
"inhaltsverzeichnis": [8],
"formatierungen": [0],
"termbase-exchange-format": [0],
"rückwärts": [4],
"infolgedessen": [2],
"direktionalität": [0],
"einige": [8],
"dialogfenst": [[1,7],3,4,0,2,6],
"fuzzi": [1,4,5,7,[2,3],6],
"vorher": [2],
"irgendwo": [6,5],
"size": [2],
"left": [0],
"einfügen": [0,4,5,1,7,3,[2,6]],
"einfärbt": [0],
"rechner": [1,4],
"menüpunkt": [0,4,7,3],
"weil": [0,1],
"verlaufsbasiert": [1],
"sprachenpaar": [2],
"gearbeitet": [3],
"speziell": [0,[2,6]],
"weit": [2],
"weis": [3,0,[5,6]],
"violett": [4],
"ausgeschlossenen": [2],
"edittagnextmissedmenuitem": [0],
"same": [7],
"vermeiden": [2,[0,3]],
"quiet": [2],
"mindestinhalt": [6,8],
"bewirken": [[0,7]],
"formatanforderungen": [2],
"bedienend": [3],
"xhmtl-filter": [0],
"laden": [2,[0,7],[3,4],[1,6]],
"implementiert": [1],
"beispiele": [8],
"ausführlichsten": [0],
"belässt": [5],
"bezeichnet": [0,2],
"tag-bezogenen": [3],
"satzübersetzungen": [7],
"the": [0,[2,7],5],
"projectimportmenuitem": [0],
"ausrichtungsmodus": [7],
"bearbeitung": [3],
"imag": [0],
"monolingu": [0],
"linux-distributionen": [2],
"richtigkeit": [0],
"microsoft-ziel-local": [0],
"quelltexten": [[6,7],0],
"omegat.project.lock": [2],
"prioritätsstufen": [3],
"entwicklungsseit": [2],
"moodlephp": [2],
"zwei": [2,0,7,4,[1,3,6]],
"currsegment.getsrctext": [7],
"dieselben": [[0,2]],
"kategorien": [0,8],
"gelernt": [0],
"export": [6,2],
"überflüssig": [7],
"freuen": [0],
"projektdaten-speicherinterval": [[1,2,4,6]],
"äquival": [[0,5]],
"practic": [7],
"ereignissen": [7],
"unabhängig": [7],
"check": [7],
"zieldateien": [0,4,2,7],
"benachrichtigungseinstellungen": [5],
"tm-datei": [1],
"minimieren": [5],
"doppelt": [[0,7],2],
"konten": [2],
"bidirektional": [4],
"gotonotespanelmenuitem": [0],
"fr-fr": [3,1],
"hochzuladen": [2],
"ordner": [2,7,6,0,4,3,[1,5]],
"zeichensätz": [0],
"neuladen": [[4,6]],
"ordnen": [0],
"gefüllt": [[2,6],4],
"zusammenführen": [[1,7]],
"standardwert": [[0,7],4],
"muster": [1,[0,7],2],
"verarbeitenden": [2],
"eckig": [0],
"gefundenen": [7,4],
"root": [0],
"zugeordneten": [4],
"hosting-serv": [2],
"einzig": [2],
"bestehend": [2,[6,7]],
"scannt": [7],
"ersetzten": [0],
"welt": [2],
"omegat-ordn": [2],
"ausgangstext": [1],
"monospace-schriftart": [1],
"lateinisch": [0],
"konvertieren": [2,3],
"neues": [8],
"neuer": [2,[0,4]],
"systemprogramm": [2],
"zeichenkett": [[0,7],1],
"grünem": [5],
"translation": [8],
"syntax": [0,2],
"neuen": [2,7,[0,3],4,[1,6]],
"mechanismen": [0],
"anzeig": [7,5],
"inline-formatierung": [7],
"konfigurationsordnern": [2],
"po-dateien": [0,2,[1,6]],
"empti": [2],
"sätzen": [0],
"originaltext": [7,2],
"einzubeziehen": [[0,1,2,4,7]],
"könnten": [[0,2],6],
"installationsordn": [2],
"block": [7,0],
"tms": [2,[4,6],[0,3,7],[1,8]],
"suchzeichenkett": [7],
"tmx": [7,2,[3,5]],
"html-ähnlich": [1],
"repo_for_all_omegat_team_project": [2],
"cli": [2],
"application_startup": [7],
"lediglich": [3,7],
"festgelegt": [[1,2],7,[0,5,6]],
"eventtyp": [7],
"url-protokol": [2],
"sanktion": [6,1],
"fr-ca": [1],
"interferiert": [4],
"mainmenushortcuts.properti": [0],
"gelangen": [3],
"teilt": [7,[0,1]],
"werkzeugen": [2],
"auflistung": [0],
"glossardateien": [0,[5,7],2],
"standardinhalt": [0],
"besitzt": [0],
"anmerkung": [2,7,0,4,1,[3,5],6],
"statistiken": [4,1,[2,7],[0,6]],
"wenn": [2,7,0,4,1,5,3,6],
"mehrere": [8],
"negieren": [0],
"segmentmodifikationsinfo": [1,[4,5]],
"subtitl": [2],
"gespeichert": [[1,2],[0,4],[5,7],3],
"gotohistorybackmenuitem": [0],
"schwellenwert": [1,[2,5]],
"omegat.project-datei": [2],
"save": [7],
"v1.0": [2],
"dateinamen": [7,0,[2,3,6]],
"fensterbereich-widgets": [8],
"teilen": [2,[3,6,7],[0,5]],
"kennzeichen": [4],
"abgeglichen": [0],
"projektereigni": [2],
"top": [5],
"ergebnissen": [[0,7]],
"have": [0],
"powerpc": [2],
"juristischen": [0],
"satz": [0,7,3,[1,4],2],
"komplex": [0,2],
"exportieren": [0,[1,4]],
"menüs": [8],
"question": [0],
"kontextmenü-symbol": [4],
"dateiendung": [0,2,1],
"hervorhebung": [7,4],
"editselectsourcemenuitem": [0],
"eine": [8],
"aufgenommen": [6],
"regelsatz": [1],
"com": [0],
"projekt": [2,7,3,6,[0,4],[1,5],8],
"instal": [2,[0,1]],
"übernommen": [6,1],
"wert-text": [2],
"cot": [0],
"remot": [6],
"liegen": [1],
"font-fallback": [[0,4]],
"sollt": [2,7,[0,1,4]],
"dafür": [0,[2,7]],
"guter": [1],
"dokument.xx.docx": [0],
"alleinstehend": [[0,1]],
"birnen": [0],
"jetzt": [2],
"neuanordnung": [[4,5]],
"pipe": [0],
"kde-nutz": [2],
"bereit": [2,0,[6,7],1,[3,4,5]],
"überschreibt": [5,[0,2]],
"großbuchstab": [0],
"nutzer": [0,[1,2,4,5,7]],
"hängen": [[2,4]],
"wert": [0,1,[2,7]],
"erzwingen": [0,4],
"dateifiltereinstellungen": [7,[0,4]],
"omegat-projektdatei": [5],
"nutzen": [[0,4],3,2,[1,5,8]],
"changeid": [1],
"translat": [2,3,7,6,[0,5],[1,4]],
"erinnerung": [[0,4]],
"html-kommentar": [0],
"verwendete": [8],
"université": [1],
"krasser": [0],
"verschoben": [[1,3,5]],
"einhergehen": [4],
"verbessern": [0],
"suchfunkt": [[0,2]],
"schreibweisen": [0],
"umbenennung": [2],
"suchbereich": [7],
"cqt": [0],
"pseudoübersetzt": [2],
"verbleibend": [5],
"trefferanzahl": [7],
"respons": [5],
"zuverlässigkeit": [3],
"gemeißelt": [6],
"scripting": [8],
"docs_devel": [2],
"zusammengesetzt": [1],
"zeitspann": [2],
"passgenau": [6],
"lck": [5],
"tsv": [0],
"paar": [[2,3],7],
"verwalten": [3,8,[1,2,4]],
"auszuführen": [7,2,3],
"löst": [[0,2]],
"gnome": [1],
"vervollständigungen": [1],
"verwaltet": [[1,2],3],
"maximal": [0,2],
"kategori": [0],
"drittanbieter-plugin": [2],
"anstatt": [[2,3,4]],
"treffern": [3],
"zeilenläng": [0],
"doctor": [0],
"kollegen": [5],
"durchzugehen": [5],
"appdata": [0],
"sichere": [8],
"mächtige": [7],
"struktureinheiten": [0],
"csv": [0,2],
"download-seit": [[1,2]],
"tun": [[0,2],3,[4,7]],
"skripteditor": [[0,7]],
"angeklickt": [5],
"umbenennen": [2],
"fällig": [3],
"unübersetzbar": [0],
"caractèr": [2],
"tm-dateien": [[6,7]],
"fr-zb": [2],
"navigationsmöglichkeiten": [3],
"les": [5],
"press": [0],
"dock": [2],
"standardmäßig": [0,7,1,2,4,6,5,3],
"unsichtbar": [0],
"element": [0,[3,7],4],
"doch": [3],
"dasselb": [[0,1,2,4,7]],
"wortwiederholungen": [0],
"suchfenster": [8],
"speicher": [2,[0,7]],
"schleif": [7],
"night": [2],
"genutzt": [7,2],
"statt": [[0,7],3],
"aneinand": [7,[3,4]],
"markierten": [7,4,1,[0,5]],
"kästchen": [7,1],
"herangezogen": [4],
"weiter": [0,4,2,7,[1,6],5,3,8],
"abschnitt": [3,2,0,7,1,4],
"arbeitsweis": [[3,5]],
"suchergebni": [7],
"verweisen": [7],
"erfüllt": [0],
"tarball-archiv": [6],
"absatzumbruch": [0],
"filenameon": [1,0],
"cut": [0],
"ctrl": [0,4],
"editorinsertlinebreak": [0],
"jumptoentryineditor": [0],
"document": [0],
"ausgeschlossen": [7,[2,5]],
"einmalig": [[5,7],[0,4]],
"neustart": [2],
"unveränderlich": [6],
"page_up": [0],
"glossaryroot": [0],
"derselben": [5,[1,7]],
"selbst": [0,2,3,7,6],
"beendet": [4],
"bedenken": [2],
"eingabeaufforderung": [2],
"einschließen": [2],
"genau": [[0,7],[3,4]],
"vorkommen": [[1,7],[0,2,3]],
"beenden": [4,7,[0,1,2]],
"resourc": [2],
"geschrieben": [[0,4]],
"während": [7,[0,2,3,5],4,1],
"moodl": [0],
"demselben": [2,[0,3,7]],
"team": [2,1,8],
"xx_yy": [0],
"docx": [[0,2,4]],
"project_stats_match_per_file.txt": [[4,6]],
"txt": [2,0,5],
"legacy-filt": [0],
"akzeptieren": [[6,7]],
"projektdatei": [4],
"meldet": [2],
"löschung": [3],
"sicherzustellen": [2,[1,3]],
"projektdaten": [[1,2],7],
"benötigen": [[0,2],3],
"ermittlung": [0],
"jeweil": [[0,1,2]],
"lib": [0],
"segmentnavig": [4],
"typ": [[5,7]],
"source": [8],
"repository-zuordnung": [[2,7]],
"mechanismus": [4],
"begriffen": [5,3,7],
"zustand": [[2,6],[0,1,4]],
"viewdisplaymodificationinfoselectedradiobuttonmenuitem": [0],
"index.html": [0,2],
"teamprojektleit": [4],
"kapazität": [2],
"wörter": [0,[4,6],1,[5,7]],
"anwendungsbeispiel": [2],
"suchvorgänge": [8],
"mehrmaligen": [1,[0,7]],
"erleichtern": [7,[0,2,6]],
"quellseg": [1],
"erleichtert": [2],
"fällen": [2,[0,4,7]],
"develop": [2],
"diffrevers": [1],
"erzielen": [7],
"felder": [[0,4]],
"regelsatzdaten": [1],
"sprachregelsätz": [1],
"hinzugefügten": [[0,2]],
"glossaren": [5,[2,3]],
"texteinheiten": [0],
"konfigurationsparamet": [2],
"zeilenumbruchzeichen": [0],
"e-mail-adressform": [0],
"hauptfensterbereich": [7,1],
"rechtfertigen": [7],
"übrigen": [7],
"rutsch": [[1,4]],
"adoptium-projekt": [2],
"manchmal": [0,2],
"anwendungsmenü": [2],
"darauf": [0,[2,5],[1,3,7]],
"stammformredukt": [1,5],
"projektressourcen": [7,[3,6]],
"omegat-instanzen": [4],
"wörterbuch-match": [4],
"unbedeutend": [0],
"projektspezifischen": [[6,7]],
"stark": [1],
"befolgen": [3],
"automatisieren": [[2,3]],
"project.gettranslationinfo": [7],
"czt": [0],
"doctorat": [1],
"abbildung": [[3,5]],
"gängigst": [2],
"konventionen": [3,8],
"start": [2,[0,7],1],
"mymemori": [1],
"angedockt": [[3,5]],
"grundsätzlich": [[0,1,2,7]],
"merkt": [4,2],
"omegat-wiki": [[2,6]],
"regex101": [0],
"editiert": [7],
"equal": [0,2],
"fällt": [3],
"koreanischen": [1],
"libreoffice-dateien": [3],
"aussieht": [3],
"konfigurationsordner": [8],
"watson": [1],
"filtermust": [0],
"segmenteigenschaften": [5,[3,8]],
"wörterbuchdateinamen": [3],
"java-vers": [2],
"zusammenzuführen": [[3,7]],
"inklus": [[0,7]],
"unterschi": [7],
"tastendarstellungen": [0],
"rahmen": [0],
"komplett": [[2,5]],
"project_save.tmx.yyyymmddhhmm.bak": [2],
"zweisprachigen": [6,[2,7]],
"viewmarkglossarymatchescheckboxmenuitem": [0],
"steigern": [0],
"einträgt": [[3,4]],
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
"sonstigem": [0],
"eigenschaftsnam": [5],
"konfigurationseinstellung": [7],
"html-abschnitten": [0],
"applik": [2,[3,4],0,7],
"daten": [2,[1,4],7],
"vervollständigung": [[1,5]],
"mich": [5],
"memori": [2,3,6,7,5,4,[0,1]],
"autocompletertablelast": [0],
"wagenrücklauf": [0],
"no-match": [4],
"importiert": [5],
"markierungen": [0,[1,7]],
"tools-menü": [[0,8]],
"daraus": [2,0],
"indefinit": [0],
"wörterbucheinträg": [0],
"anschließend": [[0,2]],
"vorbehalten": [2],
"verschmelzungen": [0],
"grundregeln": [0],
"schwacher": [0],
"log": [0,4],
"tagfreien": [7],
"korrekt": [0,[1,5],[2,4,6,7]],
"jedoch": [[0,7],3],
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
"websuch": [7,4],
"teil": [0,4,7,2,[1,3]],
"initial": [[0,2]],
"leerraumzeichen": [0],
"tutori": [0],
"zeichen": [0,7,5,4,[1,2],8],
"passieren": [2],
"erste-dritt": [2],
"unterschiedlich": [[4,7],[0,1,2,3,5]],
"aggressiven": [4],
"autocompletertablepageup": [0],
"wählt": [7,4],
"sprachmust": [1,0],
"www.deepl.com": [1],
"standard-branch": [2],
"schicken": [2],
"umgang": [7],
"solch": [2,1,[3,6,7],[0,5]],
"ausgefallen": [0],
"config-fil": [2],
"interessant": [0],
"quick": [0],
"zusammenfassend": [0],
"kleinbuchstaben": [0],
"vordefinierten": [1],
"scripting-engin": [7],
"falschschreibungen": [0],
"abgedockt": [1],
"protokollauswahl": [2],
"klass": [0],
"eventuel": [0],
"das": [2,0,7,4,5,1,3,6],
"day": [0],
"dateitypen": [2],
"lre": [0,4],
"hellgrau": [4],
"unstimmigkeiten": [7],
"minuten": [2,[1,3,4,6]],
"system-user-nam": [0],
"lrm": [0,4],
"möglicherweis": [0,7,3],
"format": [2,0,3,7,[4,5,6],[1,8]],
"bestimmen": [7,[0,1]],
"console.println": [7],
"rainbow": [2],
"befindlich": [2],
"besseren": [[1,3,7]],
"textfragment": [0],
"herunterzuladen": [2],
"fetter": [7,[0,5]],
"autocompleterlistdown": [0],
"omegat-tastenkürzel": [0,8],
"writer-datei": [0],
"verlangen": [1],
"pars": [5],
"scheint": [2],
"part": [7],
"vorangestellt": [0],
"dateifilter": [8],
"suchdialogfenst": [3],
"auftauchen": [2],
"bild": [0],
"datei": [2,0,7,6,4,3,1,5],
"existierend": [7],
"regex-beispielen": [0],
"zurückkehren": [3],
"activefilenam": [7],
"ausreichend": [2],
"fuzzy": [8],
"erst": [0,[4,5,7],[2,3]],
"segmentierungsparamet": [0],
"project_files_show_on_load": [0],
"heruntergeladenen": [2],
"speicherfunkt": [2],
"benutzt": [3],
"deutet": [5],
"sammlung": [[2,3,6]],
"identifizieren": [[1,2]],
"konzentrieren": [3],
"apostroph": [0],
"originaldoku": [0],
"suchen-und-ersetzen-skript": [7],
"build": [2],
"benutzerdefinierten": [0,[2,3]],
"tag-kürzel": [0],
"plugin-installationen": [0],
"omegat-dateifilt": [7],
"notizen": [[3,5],[0,4,7]],
"heißen": [[0,2]],
"ident": [7],
"entries.s": [7],
"vorab": [0],
"den": [7,0,2,3,4,1,5,6,8],
"bevorzugt": [[0,2]],
"dem": [2,0,1,7,[4,5],3,6,8],
"gotonextuntranslatedmenuitem": [0],
"targetlocal": [0],
"der": [0,2,7,5,1,4,3,6,8],
"fehlenden": [[4,5]],
"path": [0],
"des": [0,2,[5,7],1,4,3,6],
"bind": [7],
"entwed": [0,2,3,[1,6],7],
"verlauf": [4,0,[1,2]],
"autovervollständigung": [0,[1,3,4],[5,8]],
"mehreren": [0,5],
"gleichbreiten": [1],
"sondern": [[1,3],[0,7]],
"betritt": [1],
"verschiedenen": [[3,4],[2,7],0,[1,5,6]],
"dort": [[2,7],[0,1,3,6]],
"gleitkommazahlen": [1],
"match-prozentsätzen": [6],
"zwingen": [[1,7]],
"eignet": [[0,2,7]],
"umständen": [2],
"unbedingt": [7],
"öffnet": [4,[1,7],5,3],
"spezifizieren": [0],
"öffnen": [2,7,0,[3,4],[5,6],1],
"helpcontentsmenuitem": [0],
"unlimitiert": [0],
"dateiformaten": [1],
"omegat-org": [2],
"remote-project": [2],
"beschaffen": [0],
"auszuschalten": [7],
"einfache": [8],
"deaktiviert": [4,1,[0,7]],
"initialcreationid": [1],
"ignore.txt": [6],
"trennzeichenmust": [0],
"projectaccessdictionarymenuitem": [0],
"meisten": [0,4,[1,2]],
"ressourcenordn": [7,3],
"veröffentlichen": [2],
"beschreibung": [[0,7],4],
"maximieren": [5],
"punkt": [0,2,1,[4,7]],
"hergeschaltet": [5],
"ermöglicht": [4,[3,7],[0,1],[2,5]],
"ausnahm": [2,0],
"bekommen": [[0,4]],
"files_order.txt": [6],
"projectrestartmenuitem": [0],
"tastenkürzelbeschreibungsbeispiel": [4],
"editorskipnexttoken": [0],
"trennzeichen": [1],
"europäischen": [4],
"trans-unit": [0],
"verteilt": [[0,8]],
"right": [0],
"aussehen": [3,8],
"ausgefüllt": [6,4,[1,2],0],
"qigong": [0],
"masterpasswort": [1],
"docx-dateien": [[2,7]],
"projekterstellung": [3],
"omegat.app-paket": [2],
"maximum": [0],
"paus": [3],
"ländercod": [2],
"erledigt": [3],
"die": [2,0,7,4,1,3,6,5,8],
"imper": [7],
"standardapplik": [4],
"teilmeng": [2],
"informiert": [1],
"dir": [2],
"down": [0],
"gehostet": [1],
"terminalfenst": [2],
"projekteigenschaft": [0,2,[1,3]],
"viewfilelistmenuitem": [0],
"projektparamet": [6],
"hinzufügt": [[0,2]],
"journey": [0],
"test": [2],
"laufzeitumgebungen": [2],
"omegat": [2,0,3,7,1,4,6,8,5],
"einstellung": [4,0,5,2,1,6,[3,7]],
"allemand": [1,7],
"deepl": [1],
"notizblock": [5,3,4,8],
"bedienungsanleitung": [[3,4],0,8,2],
"drücken": [7,3,4,5,1],
"durchzuführen": [[2,3,7]],
"durchschalten": [[0,4]],
"virtual": [7,2],
"denen": [[0,7],[2,4]],
"console-align": [[2,7]],
"dissimul": [5],
"ansatz": [2],
"back": [0],
"authentifizierungsfehl": [[2,5]],
"besteht": [0,[3,6,7,8]],
"projectopenrecentmenuitem": [0],
"fr_fr": [3],
"kommentarfeld": [5],
"standardeinstellung": [2],
"thèse": [1],
"load": [7],
"umfangreichen": [7],
"darzustellen": [1],
"verwendung": [2,7],
"zusammenhängend": [0],
"issue_provider_sample.groovi": [7],
"und": [0,2,7,3,4,5,1,6,8],
"platzhalt": [1,[0,2]],
"herkunft": [[1,5]],
"unl": [5],
"verwandten": [6],
"modell-id": [1],
"editoverwritemachinetranslationmenuitem": [0],
"relat": [1],
"veranlassen": [4],
"console-stat": [2],
"ingreek": [0],
"bearbeiten-menü": [[0,8]],
"lunch": [0],
"adressbuch": [6],
"id-cod": [0],
"f12": [7],
"convert": [2],
"voneinand": [0,7,2],
"validierung": [4],
"projectexitmenuitem": [0],
"repository-zuordnungen": [2,7],
"wirklich": [4],
"haben": [2,0,[3,7],4,1,5,6],
"legen": [[2,6],1],
"text": [0,7,4,5,1,[2,3],6],
"editregisteruntranslatedmenuitem": [0],
"konsol": [2],
"init": [2],
"remote-projekt": [2],
"analysieren": [8],
"sitzung": [[3,5,7]],
"zusammengeführt": [7,[0,3]],
"segmentierungseinstellungen": [[0,2]],
"bitt": [2],
"manag": [2],
"mussten": [7],
"manifest.mf": [2],
"mitgliedern": [2],
"anführungszeichen": [0,7],
"schaltet": [4],
"maco": [0,2,4,5,3,1],
"tm-root-ordn": [0],
"standardausgab": [2],
"erzwingt": [0],
"doc": [7,0],
"gelesen": [2],
"vergessen": [1],
"interval": [2],
"hin-und-her-konvertierungen": [2],
"mitten": [7],
"freiheiten": [8],
"standard-omegat-dateien": [0],
"mitg": [2],
"mittel": [[2,7],[1,3]],
"unverändert": [[1,7]],
"output-fil": [2],
"status": [[2,5,7]],
"verarbeitet": [[2,4]],
"server": [2,1,5],
"darum": [[3,4]],
"identifiziert": [1,2],
"paramet": [2,7,[0,1]],
"systemweit": [2],
"run-on": [0],
"verlinkten": [3],
"mal": [2,0,6,[3,4,7],5],
"semikolon": [2],
"man": [[0,3],[2,7],1],
"freigegeben": [2],
"stand": [[2,3]],
"map": [2,6],
"ausführbaren": [2],
"url": [2,1,[3,6],[0,4,7]],
"projektzielsprach": [3],
"sodass": [6],
"megabyt": [2],
"uppercasemenuitem": [0],
"viewmarkuntranslatedsegmentscheckboxmenuitem": [0],
"fußnoten": [0],
"entscheidung": [6],
"relev": [[0,1]],
"needs-review-transl": [0],
"tagwip": [7,3],
"use": [2],
"usd": [7],
"sicherheitsgründen": [[1,7],0],
"korrektur": [7],
"dienen": [[0,6]],
"standardstruktur": [6,[7,8]],
"omegat.jar": [2,0],
"omegat.app": [2,0],
"usr": [[0,1,2]],
"usw": [1,[5,7],[0,3,4,6]],
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
"doppelklicken": [2,7,[0,4,5]],
"ermitteln": [1],
"html-tag": [0],
"java-messageformat-platzhalt": [1],
"folgendem": [0],
"servic": [5],
"regelmäßig": [2,6],
"null": [[0,7],2],
"übereinstimmen": [7,[0,1,3,5,6],[2,4]],
"gesamtzahl": [5,7,4],
"cleanup": [7],
"macos-tastenkürzel": [0],
"quelldatei": [0,[4,7],3],
"quelldaten": [4],
"unterordnern": [2,[0,4,6]],
"herunterladen": [2,0,4],
"loszulegen": [3],
"auszublenden": [7],
"entdecken": [7],
"folgenden": [0,7,2,[3,5],[4,8]],
"wiederherstellen": [5,[0,1,4]],
"einigen": [1,0,[2,6]],
"omegat-konfigurationsordn": [[0,2]],
"dtd": [[0,2]],
"rechtschreibprüfung": [[3,6,7],1,[0,4,8]],
"ermittelt": [[1,2]],
"anhand": [3,2],
"plattform": [0,2],
"identisch": [7,2,[1,4],[0,5,6]],
"languagetool-problem": [4],
"meist": [7],
"zwecke": [8],
"abzudecken": [2],
"irgendeinem": [[0,3]],
"projectcompilemenuitem": [0],
"console-transl": [2],
"java-konfigur": [2],
"unicode-literal": [0],
"normalerweis": [2,7],
"erlauben": [[1,7],[0,4]],
"anspruch": [[1,2,3]],
"günstigen": [2],
"vorgenommen": [[0,2],[1,3,4]],
"macos-äquival": [2],
"unicode-skript": [0],
"geschlossen": [5,[2,4,7]],
"gezählt": [4],
"optionsautocompletehistorycompletionmenuitem": [0],
"gotonextuniquemenuitem": [0],
"fahren": [2],
"aktualisierten": [3],
"wordart": [0],
"objektattribut": [0],
"kürzere": [7],
"inform": [2],
"leerzeichen": [7,0,3,[1,4],5],
"einstellungen": [7,4,0,2,5,3,1,6,8],
"about": [0],
"commit": [2],
"targetlocalelcid": [0],
"gesamten": [0,3,[2,4,7]],
"monat": [[0,2]],
"wörterbuch": [1,6,[0,3,4,5]],
"gleichbedeutend": [1],
"project_stats_match.txt": [[4,6]],
"benötigt": [2,0,7,1],
"omegat-bereich": [4],
"japanischen": [1],
"verkettung": [0],
"tmx-dateien": [2,7,6,1],
"ersten": [[0,7],2,5,[1,3,4,6]],
"standardauswahl": [7],
"alphanumerischen": [0],
"systemzeit": [0],
"fehl": [7],
"erster": [[2,4]],
"applikationen": [0],
"textverarbeitungsprogramm": [7],
"zurückzusetzen": [0],
"terminologiedateien": [0],
"reibungslos": [3],
"libreoffic": [[0,3]],
"male": [2],
"autocompleterclos": [0],
"qualiti": [7],
"anzuwenden": [7,1],
"holen": [4],
"block-elementen": [0],
"dürfen": [1],
"long": [0],
"automatischen": [7,4],
"nicht-gui-modus-optionen": [2],
"warnungen": [2],
"dienst": [5,1,2],
"eigentlichen": [0],
"geändert": [0,1,2,3,[4,7],6],
"feld": [7,4,[2,3],[0,5]],
"griechischen": [0],
"mit": [0,2,7,1,4,3,5,6],
"spracheinstellungen": [1],
"dateinam": [[2,5,7]],
"zählweis": [4],
"sammeln": [2],
"gebunden": [7],
"projektmanag": [2],
"fortgeschritten": [0],
"vergewissern": [2,[1,5]],
"zeichenklassen": [0],
"sämtlichen": [2],
"variablen": [[0,1],8],
"ausblenden": [5],
"ausführen": [7,2,[1,6],[3,4,8]],
"vorgang": [3,[0,2]],
"löscht": [0,[1,4]],
"enthält": [0,6,2,7,3,5,1,4],
"viewdisplaysegmentsourcecheckboxmenuitem": [0],
"editregisteremptymenuitem": [0],
"erwartete": [8],
"vks-hosting-serv": [2],
"teamprojekt-konfigur": [2],
"open": [7,0,[1,2]],
"fehlern": [3],
"generieren": [6],
"vorrang": [[4,7],2],
"skript": [7,4,0,2,1],
"project": [7,[2,5,6]],
"取得": [[1,7]],
"xmx1024m": [2],
"autotext": [1],
"grundlegend": [[0,2,6]],
"regex-definiert": [0],
"interpunktionszeichen": [0],
"arbeitet": [2],
"unterteilung": [0],
"arbeiten": [2,0,[3,7],[1,6]],
"penalty-xxx": [[2,6]],
"niemal": [0,6],
"gotonextsegmentmenuitem": [0],
"sinnvol": [7],
"projekteigenschaften-dialogfenst": [2],
"große": [[1,3]],
"textformatierungen": [7],
"omegat-entwicklungsseit": [[0,1]],
"errechnet": [7],
"ausnahmeregel": [0,[1,3]],
"excel-zellen": [0],
"dropbox": [2],
"abort": [2],
"woandershin": [2],
"internet": [[0,1]],
"wiederhergestellt": [2],
"wenig": [[2,4],7],
"professionell": [3],
"internen": [2,1],
"bitten": [2],
"beschriebenen": [[0,2]],
"nummer": [0,4,7,[1,5,6]],
"filtereinstellungen": [0],
"anbieterlist": [1],
"visualisiert": [1],
"omegat-gui": [2],
"html-dokument": [0],
"interess": [0],
"ihrer": [[2,3],4,1,[0,5,7]],
"erstklassig": [7],
"einheiten": [0],
"textbereich": [[5,7]],
"ihrem": [4,2,[0,7],[1,3,5,6]],
"separaten": [7,[2,3,5]],
"prototypbasiert": [7],
"ihren": [2,3,[0,5],8,[1,4,6,7]],
"kaskadierend": [1],
"schlüsselbasiert": [7],
"navigationsverlauf": [3],
"externe": [8],
"ausgerichteten": [0],
"layout": [1,3,5,[0,4,7]],
"registri": [0],
"fortzufahren": [3],
"bash": [0],
"basi": [[0,5,7]],
"tmroot": [0],
"zusammenzufügen": [3],
"maus": [7,3],
"omegat-filt": [0],
"schaltflächen": [7,4,[0,3]],
"vergleicht": [7],
"titel": [[4,5]],
"大学": [1],
"suchbefehl": [1,[4,7]],
"einzelnen": [[0,5],[3,7]],
"insertcharslr": [0],
"kopien": [[2,8],1],
"zielkodierung": [0],
"work": [0],
"japanischsprachigen": [2],
"editierbaren": [0],
"wort": [7,0,[4,6],[1,5]],
"baut": [7],
"cloud-dienst": [2],
"klon": [2],
"objekt": [7,2],
"word": [[0,3,7]],
"lingue": [1],
"zusammenarbeit": [3],
"sortierung": [5],
"dabei": [7,[0,2,3]],
"auto-propag": [[2,7]],
"auto-ordn": [6],
"bearbeiten-menü-tabell": [0],
"fest": [7,1],
"fett": [1,5,[0,3,7]],
"mithilf": [0,[4,6]],
"exportierend": [7],
"lautet": [[0,2]],
"entsteht": [0],
"developer.ibm.com": [2],
"projektverzeichniss": [0],
"mrs": [1],
"kanadischen": [1],
"jederzeit": [3,7,[2,6]],
"funktional": [7],
"zieldokument": [2],
"fehlermeldungen": [5],
"speicherort": [2,0,7,[3,6],4,1],
"verschieden": [[0,2],7,[1,3,4,5]],
"allem": [7],
"allen": [2,[1,7],4],
"klick": [5],
"aller": [7,[1,2,3]],
"dokumenten": [0,7,2],
"gleich": [1,7,[3,6]],
"änderungsverlauf": [4],
"suchmethoden": [7],
"eck": [5],
"maustast": [[4,5,7]],
"vielen": [0],
"entpacken": [2,6],
"computergestützt": [3],
"detailliert": [2],
"html": [0,2],
"spell": [0],
"zielsprachcod": [3],
"insertcharsrl": [0],
"zusammenfassung": [8],
"sofort": [3,[2,7],[0,1,5]],
"tagtäglich": [0],
"bisher": [2],
"finit": [1],
"tm-auto-ordn": [0],
"dienstanbiet": [5],
"sprachabhängig": [7],
"markierung": [0,[1,4,7]],
"tag-verarbeitung": [1,[3,4,8]],
"beschädigt": [2],
"exakten": [4,7],
"www.ibm.com": [1],
"jres": [2],
"nützlich": [[0,2],7,4,[1,3,5]],
"quellcod": [2],
"nehmen": [[3,7],6,[0,1,2]],
"angebracht": [7],
"kommiss": [4],
"langsam": [2],
"zutrifft": [[0,4]],
"definieren": [0,1,7,3,[2,5]],
"verschaffen": [8],
"hinwei": [3],
"toolsalignfilesmenuitem": [0],
"übertragen": [7],
"usb-stick": [2],
"andere": [8],
"benutzernam": [2],
"original-englisch": [2],
"recht": [[5,7],0,[3,4]],
"unterstrich": [0],
"command": [4,0,3],
"rechts-nach-links-text": [0],
"scripting-fenst": [7,4],
"wirkt": [7,[0,4]],
"zuzuweisen": [7,[2,3,6]],
"unicode-blöcke": [8],
"funktionen": [0,3,6,[4,5,7]],
"tag-fre": [[3,7]],
"betriebssystemen": [6],
"zugewiesen": [4,0,[1,5,7]],
"bestätigen": [[0,1],7],
"onecloud": [2],
"viewmarkbidicheckboxmenuitem": [0],
"filterparameterdatei": [2],
"aktualisiert": [6],
"kümmert": [3],
"dateinamenvariablen": [0],
"geöffnet": [2,[4,5],[1,7],3],
"qa-validierung": [4],
"med-format": [4],
"kleinschreibung": [0,[4,7],1],
"branch": [2],
"via": [2,5,[0,3,7]],
"springen": [0,3,5],
"fileshortpath": [[0,1]],
"zuzugreifen": [7,0,[2,4,6],3],
"zielspracheneintrag": [4],
"begrüßt": [3],
"日本語": [7],
"steuerzeichen": [0],
"themen": [2],
"ausrichtung": [7,[0,2]],
"entwickeln": [2],
"wozu": [0],
"änderung": [[1,3,6],[2,4,7]],
"ausgegraut": [[4,7]],
"dienstprogramm": [2],
"zurückgesetzt": [0],
"vergeben": [0],
"entwickelt": [[3,7]],
"med-projekt": [4],
"außerdem": [2,7,[5,6]],
"sprach": [2,7,[0,1,3,6]],
"version": [2,4,[3,8]],
"statusleist": [5,[0,3]],
"konfigurationsdatei": [2,7],
"log-datei": [4],
"effizient": [7],
"grundsätze": [8],
"übersetzungsdienst": [4,5,1],
"folder": [5,7],
"durchführen": [[2,7]],
"effizienz": [0],
"bindestrich": [0],
"klassen": [0],
"projektadministr": [2],
"gebündelt": [2],
"detail": [6],
"kleingeschrieben": [0],
"vereinfachen": [[1,3]],
"anweisungen": [2,[3,7]],
"weist": [[0,3]],
"belegten": [4],
"geschützt": [[1,4,7],[0,3]],
"standardinterval": [1],
"projecteditmenuitem": [0],
"ein": [0,2,7,1,3,4,5,6,8],
"festgestellt": [[3,4]],
"einfügt": [2,5],
"begonnen": [[3,7]],
"new_word": [7],
"worden": [[1,7]],
"weise": [8],
"konvert": [2],
"generaldirekt": [4],
"run\'n\'gun": [0],
"nashorn": [7],
"wichtigen": [0],
"machin": [7,2],
"unsung": [0],
"vks": [2],
"strg": [4,0,3],
"sprachwörterbuch": [1],
"java-laufzeitumgebung": [[0,2]],
"stund": [3],
"last_entry.properti": [6],
"gegen": [3],
"odf-dateien": [0],
"bildern": [0],
"übersetzern": [2],
"bestimmt": [0,2,[1,5,7],[3,4,6]],
"zuordnen": [3],
"komprimiert": [[1,6]],
"resname-attribut": [0],
"präfix": [[1,2,6]],
"internetverbindung": [1],
"ursprünglichem": [3],
"gibt": [2,0,[4,5,7],[1,3]],
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
"stellt": [2,1,[0,5]],
"benutzerdefinierbaren": [7],
"omegat-menü": [2],
"ell": [1],
"bezeichnung": [1],
"aktualisierung": [2,[1,7]],
"xml-basiert": [0],
"tastendarstellung": [[0,4]],
"rechts-nach-links-einbettungszeichen": [0],
"aufträgen": [2],
"editorfirstseg": [0],
"x0b": [2],
"konform": [2,[3,7]],
"möglich": [2,[1,5],0,[4,8]],
"effekt": [0],
"jahr": [[0,2]],
"keinem": [1],
"editorinhalt": [3],
"identifizierten": [1],
"angelegt": [[3,7]],
"altern": [[0,4],5,7,1,3],
"http": [2,1],
"keinen": [0,[4,6]],
"äpfel": [0],
"nächster": [0],
"willkommen": [3],
"vorhanden": [7,4,[2,5],[0,1],[3,6]],
"nächstes": [0,4,3,7,[1,2,5]],
"nächsten": [4,0,7,2,[1,3]],
"könnte": [[2,3,7]],
"komma": [0],
"liest": [[1,7]],
"lisenc": [0],
"magischen": [0],
"root-verzeichni": [2],
"angebot": [3],
"hervorzuheben": [[1,7],6],
"von": [2,0,7,[1,4],6,3,5,8],
"softwar": [0],
"vom": [2,0,4,7,5,1],
"segmentpaar": [7],
"vor": [2,0,7,4,[1,3],[5,6]],
"projectsinglecompilemenuitem": [0],
"end": [0,7,1,[3,5]],
"schließenden": [0],
"übersetzungsdateien": [3],
"lisens": [0],
"einfachsten": [0],
"früheren": [0,[2,3,5]],
"erhält": [6],
"beschränkt": [2,5,[0,6]],
"myfil": [2],
"visuel": [4],
"überschrieben": [2,3,4,[0,1,7]],
"machen": [0,1,[2,4],[3,5,6]],
"kommt": [2,[0,7]],
"aufgelistet": [2,[0,4]],
"aufrufen": [0,[4,7],[3,5,6]],
"env": [0],
"dokument.xx": [0],
"shell-skripten": [0],
"abgrenzung": [7],
"okapi": [2],
"übersetzend": [5,[2,3]],
"page_down": [0],
"versetzt": [3],
"künftig": [4],
"mitgeliefert": [7],
"umbenannt": [3],
"entsprechen": [[2,7],[0,6]],
"kurz": [[0,7],3,[1,5]],
"skriptordn": [7,1],
"copyright": [4],
"omegat-übersetzungsprojekt": [[3,7]],
"springt": [4,7],
"kehrseit": [0],
"notiz": [4,5,3,0],
"bedingungen": [8],
"verwaltung": [[2,3]],
"system-os-nam": [0],
"kommentaren": [7,5],
"omegat-benutzeroberfläch": [2,[3,4]],
"insertcharspdf": [0],
"folglich": [[3,7]],
"einzulesen": [7],
"kenntlich": [4],
"damit": [0,2,3,[4,7,8],[1,6]],
"besonderen": [0],
"heapwis": [7],
"befehlszeilenoptionen": [2],
"standardregeln": [1],
"hervorheben": [0,4,5,[1,7],[3,6]],
"zugeordnet": [2,1],
"programmen": [0],
"festzulegen": [7,[1,2,5]],
"tar.bz2": [6],
"bereitgestellten": [1,2],
"kürzlich": [5],
"editor-tastenkürzel-tabell": [0],
"textersetzung": [7,[0,3,4,8]],
"sicherungskopien": [2,6],
"kleingeschriebenen": [0],
"segmentierungsregeln": [7,0,4,1,2,[6,8]],
"bundle.properti": [2],
"zuverlässige": [8],
"contributors.txt": [0],
"dritten": [[0,5]],
"wörterbuchbereich": [1],
"mühsam": [3],
"www.regular-expressions.info": [0],
"path-einstellungen": [2],
"fensterbereich": [5,4,[3,7],1,2],
"einbezogen": [[1,4]],
"merkmal": [0],
"garant": [0],
"tastenkürzel": [0,4,3,7,1],
"betracht": [3],
"sourcelang": [0],
"suchbegriffen": [7],
"schritt": [7,2,0,6,3],
"verfälschen": [2],
"apache-ant-stil": [2],
"menschen": [4],
"fensterbereiche": [8],
"empfohlen": [[0,2]],
"festlegen": [[0,1,2,4,7]],
"isn\'t": [0],
"verfügen": [1],
"ander": [2,0,7,3,5,1,[4,6]],
"optionsdictionaryfuzzymatchingcheckboxmenuitem": [0],
"abzug": [7],
"welcher": [[0,2],7],
"zeiten": [3],
"schreibzugriff": [2],
"überblick": [5],
"spiegelt": [[4,7]],
"assur": [7],
"spiegeln": [[3,6]],
"zweiten": [0],
"projet": [5],
"betriebssystem": [0,4,2,[1,5,7]],
"notfal": [[2,6]],
"auto-synchronisieren": [7],
"vertriebslizenz": [0],
"umbruch": [1,0],
"sourcelanguag": [1],
"unternehmen": [2],
"ausdrücken": [0,2,[1,7]],
"segmentweis": [4,7],
"omegat-instanz": [2],
"helpupdatecheckmenuitem": [0],
"stellen": [2,1,[0,3,7],5],
"grundlag": [2],
"markennam": [5],
"vorgegeben": [0],
"esc": [5],
"java-referenz": [0],
"exampl": [7],
"ähnelt": [2],
"einzeln": [[0,7],3,[1,4]],
"nostemscor": [1],
"laufenden": [7,[0,2,4]],
"project_chang": [7],
"grupp": [0,[1,5,7]],
"ersetzen": [7,[0,2],[1,4],[3,5],6],
"console-createpseudotranslatetmx": [2],
"gemeinsamen": [2],
"generisch": [1],
"vorbereitung": [0],
"schriftart": [1,5,3],
"etc": [2],
"fuzzyflag": [1],
"omegat-konfigur": [2],
"neu": [7,[0,4],2,3,[1,6]],
"überlässt": [0],
"proxy-ip": [2],
"escap": [0],
"dateifiltern": [2,7],
"ignoriert": [0,7,[1,2,4,6]],
"poisson": [7],
"runway": [0],
"protokol": [[1,2]],
"müssen": [[0,2],7,[3,6],[1,5]],
"tool": [2,1,7,4,6,3,[0,8]],
"gedrückt": [[0,3,7]],
"ll-cc.tmx": [2],
"unterordn": [[2,7],0,6],
"geänderten": [0,[7,8]],
"intervall": [1],
"gzip-format": [6],
"zielsegmenten": [7],
"aktiv": [[4,5],0,1],
"wiederholen": [[0,2,3,4]],
"grund": [2,[0,3,4]],
"allgegenwärtig": [4],
"datumsangaben": [0],
"seien": [2,7],
"umzuschalten": [4],
"standardtastenkürzel": [5],
"schreibweis": [0],
"freiheit": [8],
"titelschreibung": [[0,4]],
"slot": [4],
"möglichen": [[0,2]],
"editorfenst": [7],
"möglichem": [2],
"grunt": [0],
"tabellarisch": [1],
"wissen": [3],
"ungültig": [2],
"berechnungsart": [7],
"gilt": [[2,5,7]],
"anpassungen": [[2,5,7]],
"magento": [2],
"zeilenvorschubzeichen": [0],
"offlin": [2],
"vordefiniert": [1,[0,2,3]],
"ll_cc.tmx": [2],
"u00a": [7],
"raten": [2],
"sonst": [2,6],
"offizielle": [8],
"match-variablen": [[1,8]],
"editorbereich": [[3,4,5],[0,1,2]],
"vorschlag": [1],
"shift": [0,4],
"nie": [[0,4]],
"erneut": [[2,7],[1,3,4]],
"java": [2,0,7],
"standardfarben": [1],
"rechtschreibung": [1],
"skriptdatei": [2],
"xmxsize": [2],
"alphabetisch": [[5,7]],
"ziehen": [5,[2,3,6]],
"halber": [7,2],
"project_save.tmx": [2,6,[3,7],4],
"abrufen": [4,[0,1,2]],
"fortschritt": [5,2],
"dictionari": [6,[1,5,7]],
"wichtigst": [6],
"beschränken": [0],
"plötzlich": [2],
"literarischen": [0],
"java-bundles-filt": [1],
"powershel": [[0,2]],
"eye": [0],
"po-unterstützung": [2],
"beispiel": [0,7,[1,3],5,2,4,6],
"echtzeit": [5],
"dictionary": [8],
"umbruchregeln": [1],
"projektlayout": [2],
"erforderliche": [8],
"blattnamen": [0],
"rückgängig": [4,0],
"standard-xml-not": [1],
"korrekturen": [7],
"appl": [0],
"bericht": [[2,3]],
"anklicken": [7,5,1],
"gekoppelt": [4],
"alphanumerisch": [0],
"kleinschreibung-sensitivität": [0],
"heruntergefahren": [7],
"sudo": [2],
"verlieren": [[2,3]],
"verwaist": [7,5],
"timestamp": [[0,8]],
"projectaccessrootmenuitem": [0],
"ungefärbt": [5],
"über": [2,0,7,4,1,5,[3,6]],
"umzustellen": [0],
"absatzbegrenzungen": [5,[0,1,4]],
"beheben": [2,[3,4,6,8]],
"grau": [3],
"such": [7,0,[3,4],1],
"plugin": [2,1,0,3],
"glossarbegriff": [[1,3,6,7]],
"autocompletertableup": [0],
"igen": [6],
"erforderlichen": [6],
"java-messageformat-must": [0],
"iger": [6],
"übersetzenden": [2,3],
"erkannt": [[0,4],7,[1,2,5,6]],
"projektglossar": [[0,2,4]],
"erweiterung": [[2,7]],
"glossar": [0,[5,7],[4,6],3,[1,2]],
"stilistisch": [0],
"projectcommitsourcefil": [0],
"editinsertsourcemenuitem": [0],
"omegat-glossardateien": [0],
"standardspeicherort": [[0,2]],
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
"davon": [0,[1,2]],
"beteiligten": [2],
"auslösen": [7],
"changes.txt": [[0,2]],
"vollständigen": [0,5],
"benutzeroberfläch": [2,0,1,5],
"indexeinträg": [0],
"glossari": [0,7,6,[4,5]],
"ignored_words.txt": [6],
"kursiv": [[0,3,7]],
"standard-url": [1],
"glossare": [8],
"github.com": [2],
"configuration.properti": [2],
"schriftsystem": [1],
"autocompleterlistpageup": [0],
"nachfolgend": [0,2],
"glossary": [8],
"neue": [7,[0,2,3],4,5,[1,6]],
"vielzahl": [2,0],
"sequenzen": [0],
"roten": [[1,6]],
"projektbezogenen": [2],
"weiterverarbeitung": [1],
"zugangsdaten": [1,[0,2,5,8]],
"darstellung": [5,1,[3,8]],
"string": [2],
"hidden": [5],
"kanonisch": [0],
"indikatoren": [0],
"eigenschaften": [[3,4,5],[6,7],[0,2]],
"hälfte": [1],
"standard-editor-arbeitsbereich": [5],
"besond": [0,7],
"repräsentieren": [0,[1,2]],
"geschweiften": [0],
"irgendein": [[0,1]],
"hochgeladen": [2],
"fuzzy-match-präfix": [6],
"ausprobieren": [7],
"eingabedateien": [2],
"wechseln": [[5,7]],
"was": [7,2,0,[1,4,5]],
"nachbearbeitungsskript": [0],
"war": [[1,3]],
"unterstreicht": [4],
"selection.txt": [[0,4]],
"wechselt": [4,7],
"xhtml": [0],
"itoken": [2],
"finder.xml": [[0,6,7]],
"ausgewählt": [7,4,5,2],
"dateisuch": [7],
"nicht-umbruchs-markierung": [1],
"umschalt": [4,[0,7]],
"herunt": [[2,3,6,7]],
"window": [0,2,4,5,3],
"call-out": [4],
"suchtreff": [7],
"disable-project-lock": [2],
"unterscheiden": [0],
"omegat.pref": [[0,1,7]],
"weiterschalten": [4,1],
"when": [5],
"durchsuchen": [0,[1,7],[3,4,5]],
"übersicht": [2,0],
"schrift": [7,[0,1,5]],
"aufgerufen": [7,0],
"plan": [1],
"aktionen": [7,[4,5],2],
"klassennam": [2],
"howto": [3],
"rainbow-unterstützt": [2],
"stats-ausgabedatei": [2],
"sollten": [2,[0,1],7,6],
"widget": [5],
"tastenkürzel-definitionsdateien": [0],
"zuerst": [[1,2]],
"lässt": [2,3,7],
"abgekürzt": [0],
"direct": [0],
"gewünscht": [1,[2,4]],
"jeweiligen": [[2,6]],
"erfolgt": [2,7],
"web": [2],
"klammern": [0,1],
"anleitungen": [2,[3,5,8]],
"en-us_de_project": [2],
"liegt": [[3,5]],
"weg": [2],
"symlink": [2],
"zeilen": [7,5],
"memories": [8],
"wem": [1],
"editselectfuzzy4menuitem": [0],
"editregisteridenticalmenuitem": [0],
"standardlayout": [5,8],
"vorzuziehen": [0],
"url-suchvorgäng": [1],
"menschlich": [1],
"ordentlich": [2],
"hanja": [0],
"beschreibbar": [0,[4,6],[2,7],5],
"sorgen": [2],
"erneuten": [[2,4]],
"gepaarten": [1],
"java-bundles-format": [1],
"satzfragmenten": [3],
"advanc": [1],
"textcursor": [5,4,3,[0,7]],
"aufheben": [0],
"desselben": [[1,3]],
"fensterlayout": [[4,5]],
"fenstertitel": [7],
"palett": [2],
"ausgab": [2,1],
"nun": [0,[2,3]],
"angegeben": [2,0,7,[1,3,4]],
"nur": [0,7,2,1,4,3,5,6],
"inhalt": [[0,2,7],[1,3],6,[4,5]],
"notizenbereich": [0],
"müsste": [6],
"etwa": [[0,1,2,3,5]],
"ip-adress": [2],
"dict": [1],
"absätz": [0,7],
"standard-omegat-layout": [8,[3,5]],
"synchronisationsmechanismen": [2],
"nennt": [2],
"arbeit": [2,3,[0,7]],
"quelldateiformat": [7],
"älter": [2],
"marker": [2],
"translation-memory-match": [0],
"extrahiert": [[0,7]],
"weich": [0],
"allerd": [2,[0,3,7]],
"fallback": [0],
"erscheint": [1],
"schließlich": [2],
"option": [1,0,7,[2,4],[3,6]],
"spalten": [7,[0,1]],
"omegat-optionen": [2,0],
"schnell": [7,3,[2,8]],
"verändert": [[2,7]],
"erschwert": [0],
"auftritt": [2],
"sicherungsdatei": [2],
"voller": [7,3],
"kein": [0,1,7,2,4,3,6],
"wie": [0,2,7,3,1,5,[4,6],8],
"steuern": [2],
"abgebrochen": [7],
"parallel": [[2,7]],
"dies": [0,2,7,1,4,6,5,3],
"verändern": [[0,2,3,8]],
"wirksam": [0,7],
"wir": [0,[2,3]],
"präzise": [0],
"suchen": [7,0,[3,4],1,5],
"cjk-sprachen": [0],
"ersetzung": [7],
"kleiner": [[4,5]],
"rechtschreibproblem": [4],
"skriptlist": [7],
"netzwerk": [2],
"kleinen": [0],
"anmeldedaten": [2],
"xliff-filt": [0],
"genannten": [[0,7],[1,8]],
"spezialdateien": [2],
"vertrauenswürdig": [2],
"user": [1],
"matching-prozentsätz": [[1,5]],
"extens": [0],
"back_spac": [0],
"fortsetzen": [[0,6]],
"tooltip": [[1,5]],
"prozentwert": [1],
"dateiform": [0,[2,3,4,5,7]],
"robot": [0],
"rand": [[5,7]],
"erstellt": [2,6,[3,4,7],0,5,1],
"hintergrundfarb": [6],
"ganz": [0,[1,3],5],
"zeichenbereich": [0],
"nahezu": [2],
"zurückgreifen": [5],
"textblöck": [7],
"ambitioniert": [3],
"direktional": [[0,4]],
"entsprechend": [7,0,[1,3,4]],
"dezimalstell": [0],
"omegat-paket": [2],
"po-head": [0],
"findet": [0,7],
"ab": [2,4,[0,1,6]],
"eclips": [2],
"konfigur": [2,7],
"panik": [3],
"behebt": [2],
"diff": [1],
"finden": [0,7,2,3,[1,4],6,5],
"al": [0,2,7,1,6,[3,4],5],
"am": [0,7,[2,5],1,3,4],
"an": [2,0,5,[4,7],1,3,6,8],
"editmultiplealtern": [0],
"ausgeben": [3],
"ziel": [7],
"font-substitutionen": [4],
"elementen": [1],
"formen": [7],
"pseudoübersetzten": [2],
"modifizieren": [[6,7]],
"folg": [7],
"hierarchi": [6],
"absatzeben": [0],
"zeicheneingabesystemen": [1],
"genug": [2],
"wenigen": [4],
"globalen": [7,1,[0,2]],
"grundlagen": [0],
"remote-omegat-projekt": [4],
"technisch": [[0,2]],
"direkt": [2,0,4,[6,7]],
"omegat-distribut": [2],
"hauptordn": [2],
"zurückzukehren": [[3,5]],
"entlang": [0],
"suchvorgäng": [7,[1,4],[0,5],6],
"filters.xml": [0,[1,2,6,7]],
"übersetz": [2,3,0,1,5],
"groß": [0,7,4,1],
"mindesten": [[2,6],[1,7]],
"br": [0],
"prozentsatz": [5,1],
"gebeten": [4],
"tmx-standardkonform": [2],
"segmentation.conf": [[0,2,6,7]],
"wollen": [2,0,7,[3,4]],
"openxliff-filt": [2],
"ca": [2],
"bewertung": [7],
"cc": [2],
"wendet": [4],
"hilfe": [8],
"ce": [2],
"nützlichsten": [0],
"gefragt": [[4,7]],
"cr": [0],
"umgekehrt": [0,[2,7]],
"flexibilität": [3],
"ähnlichsten": [5],
"cs": [0],
"hilft": [3],
"partner": [2],
"oberhalb": [7,[1,3,5]],
"applikationsordner": [8],
"apach": [2,7],
"da": [2,7,[0,3],1],
"adjustedscor": [1],
"wenden": [[2,5]],
"omegat-layout": [0],
"dd": [2],
"de": [1,5],
"extern": [7,[1,4],0,2,[3,5,6]],
"f1": [[0,4,7]],
"f2": [[3,5],[0,7]],
"f3": [[0,4],5],
"sprachmustern": [1],
"zielsprachenstandard": [0],
"f5": [[0,3,4]],
"veröffentlicht": [2],
"dz": [6],
"außer": [[0,2]],
"editundomenuitem": [0],
"oberen": [1],
"ja-rv": [2],
"abgezogen": [6],
"abgelegt": [[0,6,7],[2,3,4,5]],
"standardtrennzeichen": [1],
"insbesonder": [[2,5]],
"standardnamen": [[2,6]],
"andocken": [5],
"belazar": [1],
"sowohl": [7,[0,2],[1,5]],
"en": [0],
"verwenden": [7,2,0,1,3,5,4,6,8],
"umbrüch": [1],
"dateiauswahl": [2],
"verwendet": [0,2,7,1,3,4,6,5],
"er": [7,0,6,5,[1,2]],
"es": [2,0,7,1,3,4,6,5,8],
"gehosteten": [2],
"ausnahmeregeln": [0,1],
"listenansicht": [5],
"auswahlopt": [0],
"möglichkeit": [2,[0,4],7],
"vertikal": [0],
"registrierung": [[1,2]],
"auszurichtenden": [7],
"übersetzung": [4,1,[0,3],2,7,5,6,8],
"dienstnam": [5],
"for": [[0,7],2],
"exclud": [2],
"erfassen": [0],
"ausmacht": [0],
"fr": [2,[1,3]],
"konsolenmodusnam": [2],
"printf-funkt": [0,1],
"gegenstück": [0],
"content": [0,2,7,1],
"duckduckgo": [1],
"ding": [3],
"sobald": [2,[3,7],[0,4,6]],
"remote-speicherort": [2],
"applescript": [2],
"wären": [2],
"json": [2],
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
"form": [[0,7],4],
"kollabor": [2],
"versucht": [7,[0,1,2]],
"vorkommend": [7],
"fort": [2,3],
"paketinhalt": [2],
"hh": [2],
"färbung": [0],
"setzen": [7,[0,1,2],[4,6]],
"duser.languag": [2],
"viewmarkparagraphstartcheckboxmenuitem": [0],
"stützt": [0],
"teammitglied": [[2,3]],
"darüber": [[3,7]],
"java-launch": [2],
"fragt": [[1,7]],
"file-target-encod": [0],
"unveränderlichkeit": [6],
"mainmenushortcuts.mac.properti": [0],
"sicher": [2,1,[0,4,7]],
"id": [1,0,7],
"https": [2,1,0,[5,6]],
"quelldateien-list": [0],
"darstellen": [1,0,[2,3]],
"if": [7],
"sofern": [2,[0,4,5,7],1],
"project_stats.txt": [6,4],
"projectaccesscurrenttargetdocumentmenuitem": [0],
"konfigurationen": [1],
"klassenbeispiel": [0],
"im": [7,2,0,1,4,3,5,6],
"in": [0,2,7,4,1,3,5,6,8],
"regelmäßigen": [2],
"drittanbietern": [2,3],
"individuellen": [2],
"termin": [2],
"index": [0,2],
"is": [0],
"omegat-applik": [2],
"gängigen": [2],
"absatzsegmentierung": [0,7],
"projectaccesstmmenuitem": [0],
"odf": [0],
"kurzform": [0],
"geladenen": [6],
"ja": [7,[1,2]],
"sperrigen": [3],
"mehrmal": [3],
"je": [0,[2,5],[1,7],[3,4]],
"indem": [[3,7],2,0,6,4],
"hingegen": [0],
"odt": [[0,7]],
"gotonexttranslatedmenuitem": [0],
"xliff-dateien": [[0,2]],
"charset": [0],
"wider": [[3,4,6,7]],
"librari": [0],
"sicherstellen": [[1,2]],
"hochladen": [2],
"leerraum": [0,4],
"logogramm": [0],
"unterteilt": [5,7],
"toolscheckissuescurrentfilemenuitem": [0],
"zuvor": [[3,4],[0,7]],
"libraries.txt": [0],
"learned_words.txt": [6],
"omegat-java-datei": [2],
"ftl": [[0,2]],
"vollständig": [0,2,[1,3,6]],
"erheblich": [0],
"benutzeroberflächen": [[2,4]],
"bidi-markierungen": [7],
"viewdisplaymodificationinfoallradiobuttonmenuitem": [0],
"java-regex-dokument": [[0,3]],
"ausnahmemuster-dialogfenst": [7],
"la": [1],
"mitglied": [2],
"systemvariablen": [[1,7]],
"darunt": [[2,6],[4,7]],
"lf": [0],
"oft": [3,[0,1,2]],
"versteckt": [[6,7]],
"li": [0],
"grauen": [4],
"ll": [2],
"rlm-zeichen": [0],
"weiterzuverarbeiten": [0],
"projektspezifisch": [7,[0,1,2,4]],
"lu": [0],
"bestätigt": [6],
"skripte": [8],
"bash-skript": [2],
"wahrscheinlichst": [5],
"nachzuvollziehen": [2],
"glossareinträg": [[0,3,4,5]],
"that": [0],
"cycleswitchcasemenuitem": [0],
"allgemeinen": [7],
"mb": [2],
"gesamtwortzahl": [4],
"führen": [0,2,4,1],
"me": [2],
"benutzeroberflächensprachparamet": [2],
"drei": [0,2,[1,4,6],[3,5,7]],
"mm": [2],
"entri": [7],
"nachdem": [[2,3],7,1,0],
"ms": [0],
"dunkl": [1],
"mt": [6],
"vorangehenden": [0],
"bestehen": [0,7],
"xliff-inhalten": [0],
"arbeitsschritt": [3],
"my": [[0,2]],
"wichtig": [2,0,3],
"berechnet": [[1,5],7],
"license": [8],
"aktuel": [4,7,[0,5],[1,2,6]],
"ohn": [[0,2],7,5,[1,6],3],
"einbettung-beenden-zeichen": [0],
"formulierung": [3],
"überhaupt": [0],
"funktionalität": [2],
"technischen": [0],
"updat": [1,[0,2,4]],
"freiwilligen": [7],
"basierend": [3,0,[4,7]],
"licenss": [0],
"lokalen": [2,[1,4],[0,5,7]],
"datum": [1,7,[3,6]],
"genießen": [3],
"no": [0],
"ungesetzt": [1],
"nutzt": [1],
"code": [0,2,7,3],
"erfasst": [0],
"lokalem": [2],
"omegat-team-projekt": [2],
"gotohistoryforwardmenuitem": [0],
"gesuchten": [2,0],
"head": [0],
"blau": [5],
"ob": [1,[0,5,7],4,[2,6]],
"rekursiv": [7],
"aufgrund": [2],
"noch": [[3,7],2,[0,1],4,[5,6]],
"of": [[0,2,7]],
"ok": [7,4,3],
"aufgelöst": [0],
"manchen": [[2,4]],
"auswahl": [4,0,5,7],
"or": [0],
"aktuellsten": [2],
"möglichkeiten": [2,4],
"zieltexten": [7],
"derzeit": [[0,1,7]],
"editinserttranslationmenuitem": [0],
"fileextens": [0],
"verknüpften": [0],
"sehen": [[3,5,6],[0,2,7]],
"paragraph-tag": [0],
"merken": [[3,4,7]],
"po": [2],
"entsperren": [3],
"erkennbaren": [7],
"sech": [3],
"folienkommentar": [0],
"mitwirkenden": [0],
"verschieben": [7,5,3],
"ausschließen": [6],
"einträg": [5,7,0,1],
"geteilt": [0,7],
"qa": [7],
"autocompletertablefirst": [0],
"damal": [3],
"häkchen": [7,0,1],
"verfügt": [7],
"notwendigen": [2,3],
"they": [0],
"spezifischen": [[0,2]],
"github": [2],
"qs": [2],
"fehlen": [5],
"zellen": [7],
"editselectfuzzy5menuitem": [0],
"angeben": [[0,2],1],
"fehlerbehebungen": [[0,4]],
"rechts-nach-links-einbettung": [0,4],
"exakt": [7,[1,2,3]],
"rc": [2],
"auswirkungen": [0,[1,3]],
"protokollieren": [0],
"includ": [2],
"eintragen": [[2,3,4]],
"kommagetrennten": [0],
"befehlssyntax": [2],
"segmentbegriffen": [4],
"zurücksetzen": [7,1],
"t0": [3],
"t1": [3],
"aufgezeichnet": [3],
"schließend": [0],
"fehler": [2,[4,7],[0,1,3,5,6]],
"t2": [3],
"hebt": [0,7],
"websuchvorgäng": [1],
"t3": [3],
"womit": [[0,2]],
"nachbearbeitungsbefehle": [8],
"sa": [1],
"befähigen": [5],
"unterstreichen": [[1,5]],
"sc": [0],
"formatierung": [3],
"zahl": [[0,2,6],[1,3]],
"durchschnittsbewertung": [7],
"versuchen": [[1,2,7]],
"so": [0,7,[1,2,3,6,8],[4,5]],
"gedacht": [6,[0,2]],
"starten": [2,0,7,[1,4,5]],
"exported": [8],
"referenzglossar": [0],
"intern": [[0,2,4]],
"schreibfehl": [1],
"editoverwritesourcemenuitem": [0],
"omegat.autotext": [0],
"kilobyt": [2],
"ausgeliefert": [1],
"projektordnerhierarchi": [2],
"enforc": [6,4,[0,2],[1,3]],
"remov": [2],
"tm": [6,2,4,[0,7],[1,3,5,8]],
"startet": [[2,3,4]],
"to": [2,[0,5,7]],
"möchten": [7,0,2,[1,6],[3,4]],
"v2": [2,1],
"omegat-projekt": [2,[6,7],3],
"können": [2,0,7,3,1,4,5,6,8],
"stammen": [6,[0,2]],
"berücksichtigen": [4],
"windows-logo": [0],
"viewmarkautopopulatedcheckboxmenuitem": [0],
"eckigen": [0],
"projectwikiimportmenuitem": [0],
"countri": [2],
"glossarbereich": [5,1],
"um": [7,[0,2],3,1,5,4,6],
"kanadisch": [1],
"zwischenzeit": [2],
"un": [2],
"up": [0],
"geschützter": [[0,1],[3,4,5]],
"newword": [7],
"anfang": [0,6,[2,3],[1,4,5,7]],
"kennzeichnen": [0,4,[7,8]],
"this": [0],
"wörtern": [[0,7],[5,6]],
"unsegmentiert": [3],
"geschütztem": [4],
"standarddateien": [0],
"geschützten": [7],
"aufgefordert": [2],
"omegat-segmentierungsregeln": [7],
"verbesserungen": [[0,4]],
"opt": [2,0],
"kennzeichnet": [[4,5],1],
"extract": [7,1],
"kaptain-skript": [2],
"textsuche": [8],
"know": [0],
"prüfregeln": [1],
"region": [0],
"standardglossar": [0],
"vs": [1],
"support": [2],
"changed": [1],
"endgültigen": [3],
"bestehenden": [[2,3]],
"sein": [0,7,2,1,6,[3,4,5]],
"ändert": [7,[0,2,4,6]],
"regex-online-tool": [0],
"schließen": [7,[0,2],4,3],
"jar-paket": [2],
"seit": [7,[0,2,4],3],
"ändern": [[0,7],2,4,[1,3],5,6],
"segmentieren": [[0,3]],
"konflikten": [3],
"anzupassen": [[0,1],3],
"bedarf": [2,4],
"we": [0],
"gängigsten": [[0,7]],
"vokal": [0],
"befehlsbeispiel": [1],
"dann": [3,7,[0,1,2],5,[4,6]],
"dank": [3,0],
"autocompleterlistup": [0],
"zugangsschlüssel": [1],
"tm-ordner": [7],
"licenc": [0],
"zuständ": [[2,3]],
"wo": [3,7,[0,2,6]],
"entsprechenden": [[0,7],[2,3],[4,6]],
"fehlerhaft": [[0,2]],
"abgeschlossen": [7],
"omegat.project.bak": [2,6],
"repo_for_omegat_team_project": [2],
"exe-datei": [2],
"längeren": [0],
"aufnehmen": [[2,3]],
"projectaccessexporttmmenuitem": [0],
"absatzbegrenzung": [4,[1,5]],
"licens": [2,0],
"org": [2],
"umfasst": [6],
"filtern": [2,3],
"überal": [7],
"sehr": [3,2,[0,4,7]],
"distribut": [2,7],
"filtert": [7],
"po-format": [2,[1,5]],
"beauftragt": [3],
"ort": [[0,2,3,7]],
"jeder": [0,[1,4,7],[2,6]],
"jedem": [7,[0,1,5]],
"jeden": [7,[2,3],[1,5]],
"farbschema": [[1,7]],
"alphabetischen": [0],
"dauern": [[0,1]],
"entspricht": [[0,7],4,[1,3,5]],
"passenden": [0],
"xx": [0],
"sourc": [2,7,6,4,5,[0,3]],
"software": [8],
"enden": [0],
"möglichst": [8],
"endet": [0,2],
"segmentierung": [0,7,[1,3],[4,6,8]],
"fußzeilen": [0],
"type": [2,[0,6]],
"formatspezifisch": [[1,2]],
"suchbegriff": [7,0],
"problem": [[1,2],4,3,0,[5,6,7]],
"vorliegen": [1],
"optionsautocompletehistorypredictionmenuitem": [0],
"stattdessen": [0,[1,2,5]],
"projectaccesssourcemenuitem": [0],
"beliebigen": [0,4,2,7,5],
"yy": [0],
"method": [[2,7]],
"einig": [0,2,[3,4,5,7],[1,6]],
"leichter": [3,4],
"abgeschnitten": [0],
"ausdrücke": [8],
"push": [2],
"versehentlich": [[0,1]],
"readme_tr.txt": [2],
"penalti": [6],
"wies": [0],
"zs": [2],
"anwendungsfäll": [0],
"zu": [2,0,7,3,5,1,4,6,8],
"farbschemen": [1],
"oracle-dokument": [0],
"verlust": [2],
"geladen": [2,6,7],
"abgedeckt": [2],
"utf8": [0,[4,7]],
"aktiven": [4,7,[1,5],6],
"einfg": [5],
"tools": [8],
"umschlossen": [0],
"betreffen": [2],
"einhergeht": [7],
"vermieden": [7],
"power": [0],
"bezieht": [0,6],
"überprüfen": [[1,3],0,7,[5,8]],
"übersetzungsfeld": [5],
"darf": [5,[1,4]],
"context_menu": [0],
"limitieren": [7],
"speicherplatz": [2],
"quellen": [[0,1,2]],
"editsearchdictionarymenuitem": [0],
"chinesischen": [1],
"tag-valid": [2],
"uhrzeit": [7,4],
"ovr": [5],
"anzeigen": [4,[0,1],7,5,2,[3,6]],
"help": [2,0],
"quelldokumenten": [7],
"täglichen": [2],
"unterstützen": [2,[1,3,8]],
"kompatibilitätsproblemen": [0],
"speicherzuweisung": [2],
"projektbezogen": [2,5],
"typografisch": [[4,7]],
"danach": [0,1,[2,3]],
"repositori": [2,6,5],
"minimum": [0],
"optimieren": [[0,3,5]],
"überprüfungsverfahren": [3],
"lowercasemenuitem": [0],
"tabell": [0,1,4,5],
"ggf": [[1,2]],
"standard-font-fallback": [4],
"autocompleterconfirmwithoutclos": [0],
"gui-standardmodus": [2],
"separ": [2,[0,7],5],
"registriert": [2,[0,1]],
"beibehalten": [0,6,2],
"filepath": [1,0],
"gewünschten": [2,6,0],
"aufbewahrung": [7],
"angepasst": [6],
"wiederholungen": [4,[0,7]],
"office-dateien": [3],
"solchen": [2,[1,7],3],
"hinzufügen": [0,7,6,3,4,[1,2],5],
"ereigni": [0],
"einem": [0,2,4,[3,7],6,1,5],
"einen": [0,2,7,1,[3,4],5,6],
"dass": [0,2,7,5,[1,3],6,4,8],
"konflikt": [2,0],
"ohnehin": [7],
"zähler": [7],
"einer": [0,7,2,3,1,[4,5],6],
"gestartet": [7,1,2,[0,4]],
"zählen": [[0,4]],
"brückensprachpaar": [2],
"regulär": [0,7,1,2],
"protokolliert": [6],
"referenzdateien": [[3,6,7]],
"liefert": [7],
"link": [0,5,[1,3]],
"servern": [1],
"praktisch": [0],
"hero": [0],
"lini": [2],
"liefern": [7],
"basieren": [7],
"rechtschreibprüfungsdateien": [2,1],
"mitteilen": [7],
"git": [2,6],
"projektsprachen": [4],
"buch": [3],
"freisteht": [0],
"anhänge": [8],
"dollarzeichen": [0],
"fließen": [0],
"sicherheitskopien": [2,1],
"stammverzeichni": [0],
"xx-yy": [0],
"passend": [[0,1]],
"eingetragen": [4,7,1],
"will": [2],
"bereich": [7,5,3,4,[0,1,6],2],
"mauszeig": [[4,5],1],
"follow": [0],
"durchsucht": [[0,1]],
"gewährt": [4,[2,8]],
"restlichen": [[0,2,4]],
"targetlang": [0],
"diskrepanzen": [4],
"verwendeten": [7,0,4],
"optionssetupfilefiltersmenuitem": [0],
"öffnend": [0],
"altgraph": [0],
"einfachheit": [2],
"stats-typ": [2],
"vorhandenen": [5],
"wichtigsten": [3],
"sollen": [7,1,[0,2,6],3,4],
"verbrachten": [3],
"entfernt": [7,[0,1]],
"englischen": [0],
"erinnern": [6,[2,3]],
"reicht": [2],
"xml": [2,0],
"seiner": [0,[2,3,5]],
"abdocken": [5],
"darstellungsweis": [1],
"beginn": [1,0],
"seinem": [6],
"höchste": [5],
"lassen": [0,[1,2,3],5,[6,7,8]],
"entfernen": [7,0,2,6,1,[3,4]],
"starken": [0],
"neutral": [0],
"optional": [0,2],
"proxyserv": [2],
"korrigieren": [[1,2]],
"sekunden": [1],
"geleert": [4],
"teamfunktionalität": [3],
"xdg-open": [0],
"senden": [2,1],
"erkannten": [0],
"befor": [2],
"kommerziellen": [3],
"ausdrück": [0,7,1,2],
"aufweisen": [7,0],
"sendet": [2,1],
"gtk-look-and-feel": [1],
"wörterbuchbegriff": [6],
"tar.bz": [6],
"klicken": [7,3,[1,4],0,[2,5],6],
"generischen": [1,[0,2]],
"späteren": [[2,6]],
"online-mediawiki-seit": [7,[3,4]],
"fixiert": [5],
"shebang": [0],
"manipulieren": [4],
"ignorieren": [0,[1,6],4],
"systemneustart": [2],
"translation-memory-ordn": [6],
"folienmast": [0],
"editorskipprevtoken": [0],
"hinzugefügt": [7,[0,1,2],[4,6]],
"dazu": [2,0,[6,7]],
"angezeigten": [[4,7]],
"unterstrichenen": [4],
"großen": [[2,3,7]],
"kontext": [5,4],
"füllen": [[2,6]],
"bearbeitet": [0,7,5],
"zeitstempel": [[0,2,6]],
"gnu": [2,8],
"währungen": [7],
"kunden": [2],
"bearbeiten": [7,[0,5],3,2,[1,4],8],
"suzum": [1],
"zieltext": [4,[1,7],[0,5]],
"target.txt": [[0,1]],
"temurin": [2],
"omegat-teamprojekt": [2],
"nummeriert": [5,0],
"vertraut": [2],
"standard": [[0,1,7],[2,3]],
"d\'espac": [2],
"anfänglichen": [7],
"stdout": [0],
"traduct": [5],
"tastenkombin": [[0,4]],
"monaten": [3],
"tastatur": [[0,5]],
"java-design": [1],
"aufgaben": [2,7],
"runden": [[0,1]],
"zeichenkombinationen": [0],
"sätze": [0,3],
"installieren": [2,[1,3],0,[6,8]],
"optionen": [7,2,0,4,3,5,[1,8]],
"abgerufen": [1],
"nameon": [0],
"standard-darstellungsweis": [1],
"committen": [2,[0,4]],
"kontroll": [2],
"normalen": [0],
"kaffe": [3],
"gotonextnotemenuitem": [0],
"verlassen": [4,1,[3,6]],
"erfahren": [[0,3]],
"newentri": [7],
"speicherorten": [2],
"list": [0,1,2,7,3,4,6],
"autocompleterprevview": [0],
"wird": [2,0,7,1,6,5,4,3,8],
"quelldateinamensmust": [0],
"letzt": [0,4,7],
"installationsskript": [2],
"synchronis": [2],
"sagen": [4],
"java-eigenschaften": [0],
"wiederherzustellen": [[1,5,6]],
"regional": [2],
"verknüpfung": [2,0],
"erzeugt": [0,2,1],
"html-dateien": [[0,2,3]],
"projectcommittargetfil": [0],
"formate": [8],
"erscheinen": [1,[3,7]],
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
"perform": [[3,7]],
"niedrig": [[5,7]],
"maxprogram": [2],
"definiert": [0,2,1,[4,7],5,6],
"maschinelle": [8],
"pdf": [0,[2,4]],
"losgelassen": [0],
"hoffentlich": [3],
"weichem": [0],
"aufklappmenü": [0,1],
"autocompletertabledown": [0],
"trefferquot": [[1,5]],
"ordnerstruktur": [7],
"wiederzuverwenden": [[2,7]],
"editornextsegmentnottab": [0],
"toolsshowstatisticsmatchesmenuitem": [0],
"viewdisplaymodificationinfononeradiobuttonmenuitem": [0],
"überprüfungen": [4],
"festplatt": [2,7],
"einrichten": [2,6,[0,7,8]],
"tabellenverzeichnis": [8],
"erzwungenen": [6],
"leerräum": [0,4],
"per": [6,2],
"write": [0],
"großschreibung": [[0,4]],
"bringen": [2],
"gpl-version": [0],
"folgt": [2,0,3,5],
"lage": [3,[1,4]],
"project_save.tmx.bak": [[2,6]],
"period": [0],
"ausgegeben": [2],
"zunicht": [3],
"dieselb": [[0,7],2],
"obwohl": [0,2],
"texten": [0],
"standardalgorithmus": [7],
"typisch": [2],
"projectaccesswriteableglossarymenuitem": [0],
"prozedur": [2],
"genannt": [2],
"befinden": [4,[0,1,5,7],6],
"application_shutdown": [7],
"befindet": [2,[0,4,7],3,1],
"autocompletertablelastinrow": [0],
"gui": [2,7],
"definierten": [[2,5],[6,7]],
"regexp": [0],
"grundsätz": [[3,5]],
"hoher": [0],
"startoptionen": [[1,2,4]],
"glossardateiendung": [5],
"sentencecasemenuitem": [0],
"gut": [[0,2,3],8],
"segmenteigenschaften-bereich": [5],
"instanz": [[0,2]],
"datenverlust": [[2,3]],
"url-suchbeispiel": [1],
"projektmitarbeitern": [2],
"xhtml-dateien": [0],
"articl": [0],
"vorhersag": [1],
"rechts-nach-links-markierung": [[0,4]],
"verworfen": [7],
"editorcontextmenu": [0],
"parameterdateien": [2],
"wohlgemerkt": [2],
"ungefähren": [7],
"optionssentsegmenuitem": [0],
"robust": [2],
"bought": [0],
"zeitpunkt": [2,[1,6,7]],
"einmaligen": [7,4],
"endnoten": [0],
"optionsaccessconfigdirmenuitem": [0],
"charact": [2],
"framework": [2],
"bleiben": [[6,7],[2,3,4]],
"test.html": [2],
"namen": [[0,2],[1,7],6,3,5],
"wahl": [2,0],
"php": [0],
"xxx": [6],
"übersprungen": [6],
"smalltalk": [7],
"satzsegmentierung": [7,0],
"wörterbuchordn": [0],
"oben": [0,[2,7],1,5,3,[4,8]],
"explizit": [2],
"registrierten": [[2,7]],
"notfallmaßnahmen": [2],
"regex-such": [[0,3]],
"pseudotranslatetmx": [2],
"unabl": [5],
"gegebenenfal": [[3,7]],
"flüssigeren": [3],
"löschen": [2,0,7,[4,6]],
"absatzmarkierungen": [0],
"aktion": [[2,4],[0,5,7],[1,3]],
"abzugleichen": [0],
"spezifiziert": [0,2],
"fensterbereichen": [[4,5]],
"wortzeichen": [0],
"einschließt": [2],
"fungiert": [0],
"targetlanguagecod": [0],
"funktioniert": [2,[0,4,5,7]],
"ähnlichem": [2],
"vorteil": [7],
"ähnlichen": [3],
"editorprevsegmentnottab": [0],
"schwer": [2],
"reichen": [0],
"bidirect": [4],
"auftreten": [7],
"zugreifen": [0,3,7,6,[2,4]],
"eindeutigen": [7],
"schlecht": [7],
"manuell": [[2,7],[0,4]],
"links-nach-rechts-einbettungszeichen": [0],
"schreibmodus": [5],
"linux-nutz": [7],
"kontextmenüpriorität": [1],
"textinhalt": [[1,7]],
"websit": [2],
"inspiriert": [7],
"voll": [7,2],
"abwechseln": [2],
"autotext-optionen": [1],
"gemäß": [1],
"design": [1],
"land": [2],
"lang": [0,[1,2]],
"wiederherstellung": [2],
"ihnen": [2,7,0,4,3,[1,8],6],
"untermenü": [[2,7]],
"begriff": [1,[3,5],4,0,[6,7]],
"dateisystem": [2],
"beachten": [7,0],
"empfiehlt": [[2,7]],
"einfach": [0,3,2,7,1,6],
"ober": [5],
"projectnam": [0],
"sprachen": [7,1,2,3,6,0],
"fensterbereich-widget": [5],
"unübersetzt": [0,4,[1,5,7],6,[2,3]],
"versieht": [2],
"omegat.project.yyyymmddhhmm.bak": [2],
"zieltextstatus": [0],
"hinter": [5],
"installdist": [2],
"projekte": [8],
"a-z": [0],
"reihenfolg": [0,7,[1,6]],
"markiert": [4,7,[0,1,5,6]],
"absätzen": [4],
"gegeben": [[1,2]],
"gotonextxenforcedmenuitem": [0],
"editordeleteprevtoken": [0],
"onlin": [2],
"statusleiste": [8],
"passwort": [2,1],
"verbindung": [2,5],
"unicode-basiert": [0],
"anhäng": [0,[3,6]],
"fuzzy-match": [4,[0,1],5],
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
"muss": [0,2,[1,6,7],3],
"wann": [[1,6]],
"anmeldenam": [0],
"aufbewahrt": [[0,7],[2,3]],
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
"wieder": [2,[3,7],[0,1,4,6]],
"rechtschreibfehl": [7,4],
"omegat-verknüpfungen": [2],
"generel": [5,[0,1]],
"zwölf": [0],
"schlagen": [7],
"würden": [0],
"computerentwicklung": [2],
"qa-skript": [7],
"omegat-objektmodel": [7],
"kostenlosen": [[2,8]],
"kopi": [2,[4,6]],
"strukturiert": [2],
"außerhalb": [0,5,[3,6]],
"zieldoku": [[0,2,4]],
"googl": [1],
"dokumentiert": [2],
"beginnt": [[0,4]],
"xhtml-filter": [0],
"gotoeditorpanelmenuitem": [0],
"vorn": [3],
"angeboten": [[0,1]],
"viewmarkfontfallbackcheckboxmenuitem": [0],
"had": [0],
"align": [7,4],
"insertcharsrlm": [0],
"ausschlussmust": [2],
"textkombinationen": [1],
"sourceforg": [2,0],
"exportiert": [2,[4,7],[0,3,6]],
"han": [0],
"äußerst": [7,[0,3]],
"statusmeldung": [5],
"semeru-runtim": [2],
"sicherheitskopi": [2],
"hat": [0,7,4,5,2,1,3],
"einmal": [2,4],
"hintergrund": [[4,5,6]],
"unterschiedlichen": [[1,3,4]],
"pfad-zur-omegat-projektdatei": [2],
"beschreibbaren": [[2,4,5]],
"last": [7],
"zugehörig": [7,4,[0,1]],
"editmultipledefault": [0],
"adapt": [[3,7]],
"segmentbegrenzung": [3],
"mozilla": [[0,2]],
"editfindinprojectmenuitem": [0],
"pro": [6,1,[0,4,5,7]],
"warn": [2],
"aktiviertem": [0],
"aktivierten": [5],
"aktuellst": [[2,7]],
"ersetzungsfenst": [7],
"tsv-datei": [0],
"wechsel": [7],
"referenzdokumenten": [6],
"updates": [8],
"quellcontain": [0],
"umbruchregel": [1,0],
"gegenüb": [0],
"bedingungslo": [6],
"regeltyp": [0],
"nachträglich": [[2,7]],
"projekten": [0,2,5,[1,7]],
"kenntniss": [2],
"dekor": [3],
"bedeutet": [0,[1,2]],
"duckduckgo.com": [1],
"kompatibel": [2,[0,1,7]],
"zeigt": [5,4,7,1,2,3],
"schreiben": [[2,7],4],
"vertrauen": [1,7],
"aufgab": [2],
"kombiniert": [0,[2,7]],
"angab": [2],
"colour": [[1,7]],
"lauf": [[2,6]],
"probieren": [7],
"umbruchstellen": [1],
"größer": [[1,5]],
"dateiinhalten": [7],
"kurzzeitig": [5],
"benutzerdefiniert": [0,[1,2],4,3,6],
"tipp": [2],
"tastenbindungsereigniss": [7],
"kanji": [0],
"benannt": [3],
"suchfeld": [7,1],
"anpassung": [0,7,[4,5]],
"program": [[0,2]],
"hostnam": [0],
"python3": [0],
"beziehen": [[1,5]],
"kontextmenü": [5,1,[0,3,6,7]],
"präsentationsnotizen": [0],
"her": [2,[1,5]],
"innerhalb": [0,5,3,[2,4,6]],
"berücksichtigt": [1,0,[2,7]],
"schritten": [0,7],
"erstellung": [2,4,[0,6]],
"zugang": [[2,8]],
"ausgeblendet": [1],
"remote-ordn": [2],
"iraq": [0],
"dossier": [5],
"gefärbt": [5,3],
"nicht": [0,2,7,3,1,4,6,5],
"ascii-fremden": [0],
"angeordnet": [1,5],
"brunt": [0],
"aufzuheben": [0],
"überschreibmodus": [[0,4]],
"denken": [2,[0,3]],
"rechtschreibwörterbüch": [3,1,0],
"inhalten": [2,[0,3]],
"anhaltspunkt": [1],
"ordnungsgemäß": [2,0],
"doc-license.txt": [0],
"gemacht": [3,4,[2,5]],
"thema": [0],
"escapezeichen": [2],
"theme": [[1,7]],
"チューリッヒ": [1],
"einsprachigen": [7],
"sprachpaar": [[0,1]],
"standardmust": [0],
"editor": [7,5,1,[0,3],4,[6,8]],
"pseudotranslatetyp": [2],
"kodierungsdeklar": [0],
"visuellen": [[0,3]],
"properties-datei": [2],
"kommentare": [8],
"decken": [0],
"befehlszeil": [2,0,1,7],
"startseit": [2],
"gesetzt": [0,7,4],
"listet": [[1,5]],
"gelten": [7,[2,6],[0,1]],
"projectclosemenuitem": [0],
"hin": [5,2],
"viewmarknonuniquesegmentscheckboxmenuitem": [0],
"listen": [0,2],
"liegend": [1],
"vergleichsmodus": [7],
"radiobutton": [7],
"adaptieren": [7,3],
"nachbearbeitungsbefehl": [7,[0,1]],
"maschinellen": [[1,4],5],
"hoch": [4,1],
"zweistellig": [2],
"freien": [2],
"durch": [7,[0,2],[3,5],4,1],
"zweimal": [3],
"remote-vers": [2],
"findinprojectreuselastwindow": [0],
"links-nach-rechts-einbettung": [0,4],
"readme.txt": [2,0],
"gerad": [7,5,4],
"früher": [3],
"languagetool": [4,1,[0,7,8]],
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
"konfigurieren": [1,2],
"desto": [7],
"segmentmark": [5],
"prüfer": [2],
"sicherungskopi": [2,6,[1,7]],
"kodierung": [0,[7,8]],
"currseg": [7],
"immer": [[0,2],1,[6,7],4,[3,5]],
"prüfen": [1],
"unterschied": [1,3],
"installierbar": [1],
"point": [0],
"fuzzy-match-zahlenkonvertierung": [1],
"general": [2,8],
"unterstützung": [2],
"sprachcod": [7,2,3,0],
"klappen": [2],
"xhtml-dateien-filt": [0],
"kehren": [3,[2,7]],
"vertrauenswürdigen": [0],
"vermittelt": [7],
"vermitteln": [3],
"bedürfniss": [3],
"stärken": [7],
"absicht": [0],
"autocompletertrigg": [0],
"komplexen": [7],
"repository-url": [2],
"attribut": [0],
"gepaart": [1],
"korrekturarbeiten": [2],
"fortfahren": [2],
"projektkonfigurationsdateien": [2],
"klassenzeichen": [0],
"folgen": [2,[3,7]],
"kapitel": [3,2],
"proxy-host-portnumm": [2],
"acquiert": [1],
"projektinformationen": [[3,5]],
"auszurichten": [7],
"teilweis": [[2,3]],
"wörterbüchern": [[1,3,4,5,7]],
"beschreibungen": [4],
"dhttp.proxyhost": [2],
"aktuellen": [7,2,0,5,[3,6],1],
"aktuellem": [5],
"plugins": [8],
"teamprojektmanag": [4],
"vermutlich": [0],
"variiert": [[0,5]],
"angegebenen": [2,[0,1,7],4],
"vergangenheit": [2],
"editorprevseg": [0],
"szenario": [2],
"java-paramet": [2],
"texthervorhebung": [0],
"konsonanten": [0],
"a-za-z0": [0],
"vorübergehend": [1],
"unterscheidung": [3],
"you": [0],
"dokumenteigenschaften": [0],
"bevor": [2,1,[0,4,6,7],3],
"modifik": [0,7],
"werkzeug": [2],
"ersetzungsfeld": [7],
"www.apertium.org": [1],
"wirkung": [0],
"modifikatortasten": [0],
"hinzuzufügen": [2,[3,5],0,[1,6,7]],
"kompatibilitätsproblem": [0],
"höchsten": [[1,5]],
"einstellen": [0],
"stell": [0,[3,5,7]],
"project_save.tmx.tmp": [2],
"tags": [8],
"configur": [5,2],
"nativ": [2,1],
"angehängt": [7],
"gui-prozedur": [2],
"befehl": [7,1,[2,4],0],
"omegat-javadoc": [7],
"tage": [2],
"beibehaltung": [0],
"mega": [0],
"zurich": [1],
"空白文字": [2],
"omegat-benutzergruppen": [3],
"validierungszwecken": [0],
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
"omegat-hauptfenst": [4],
"wandelt": [[0,1,3]],
"segmentnumm": [[0,4]],
"identifizierung": [0],
"strich": [0],
"relevanten": [2],
"wodurch": [0,7],
"grundlegenden": [2],
"standard-tag": [7,3],
"dictroot": [0],
"tag-problem": [2,4,[1,3]],
"sogar": [[0,2,4]],
"ausrichtungen": [0],
"vorsichtig": [2,7],
"autovervollständiger-ansicht": [0],
"synchronisier": [5],
"gängige": [2],
"zeichensatz": [0],
"fenster": [7,4,1,5,3,[0,6],[2,8]],
"notwendig": [[0,7]],
"fuzzy-matches-bereich": [[4,5]],
"beim": [7,[0,1],4,[2,3],[5,6]],
"attributwert": [0],
"besten": [[1,5,7],3,[2,6]],
"drag-and-drop": [2],
"mehr": [0,[2,7],[1,3,5]],
"subdir": [2],
"programmereigniss": [0],
"absurd": [0],
"herunterladbaren": [[1,3]],
"pfeilschaltfläch": [7],
"handelt": [2,[0,1,7]],
"ebenfal": [[0,2]],
"ausnahmefäll": [2],
"irgendetwa": [0],
"zeicheneingabesystem": [4],
"bereitstellt": [[0,1]],
"benutzen": [0,[1,4],7],
"gleichen": [2,[0,3,7]],
"zeichentabell": [1],
"konnten": [3],
"ausnahmen": [7,1,2],
"vorwärt": [4],
"autocompletertableleft": [0],
"aber": [0,7,2,[1,3,6],4,5],
"allein": [0],
"statistikberichten": [7],
"spalt": [7,0,4],
"behalten": [7],
"dahint": [0],
"gehen": [2,0,5],
"absteigend": [0],
"proxy-host-ip-adress": [2],
"forward-backward": [7],
"beschrieben": [[0,5],2],
"pfad": [2,0,1,[5,7]],
"aufgehoben": [0],
"türkisfarben": [4],
"beid": [7,[0,1],2],
"hinausgehen": [[0,3]],
"ausgabe": [8],
"steht": [0,[1,3]],
"drittsprachig": [5],
"editorlastseg": [0],
"file-source-encod": [0],
"praktizieren": [2],
"großbuchstaben": [0,4],
"some": [2],
"kennung": [3],
"widerspiegeln": [2],
"stein": [6],
"vorgeschrieben": [7],
"konnt": [2],
"übereinstimmenden": [1],
"remote-serv": [[2,6]],
"anderen": [2,0,1,7,[5,6],[3,4],8],
"textsuch": [[3,7],[0,4]],
"häufig": [0,[3,7],[2,4]],
"linux-tastenkürzel": [0],
"passen": [7,[2,3,5]],
"erinnerungen": [3],
"umgebungsvariablen": [0],
"alpha": [2],
"links-nach-rechts-text": [0],
"大学院博士課程修了": [1],
"getrennt": [0,[3,7]],
"just": [0],
"gemeinsam": [2,7,3,6,5],
"zielsprach": [2,[0,7],6,1],
"doppelpunkt": [0],
"verfügung": [2,[0,7],1],
"editexportselectionmenuitem": [0],
"soll": [0,2,1,[3,4,5,6,7]],
"omegat-upgrad": [1],
"höher": [1,[0,2,7]],
"pluszeichen": [0],
"editorfunkt": [0],
"home": [[0,2]],
"macos-dienst": [2],
"bewerkstelligen": [2],
"disable-location-sav": [2],
"mehrsprachigen": [0],
"varianten": [[0,2]],
"bildschirm": [[0,3]],
"aktuell": [4,[2,7],0,[5,6]],
"projectaccesstargetmenuitem": [0],
"segmentidentifik": [0],
"webadressen": [5],
"iana": [0],
"schreibrecht": [7],
"synchronisieren": [2,6],
"leeren": [2,[1,3,4]],
"teamprojektadministr": [2],
"dokumentelement": [0],
"aufgeführten": [[2,6]],
"zuordnungsparametern": [2],
"glossar-einstellungen": [5],
"aligndir": [2,7],
"system-host-nam": [0],
"stadium": [2],
"suchparametern": [1],
"mymemory.translated.net": [1],
"vorkehrungen": [2],
"zweit": [4,[2,5,7]],
"glyphen": [4],
"creat": [[2,7]],
"omegat-erweiterungs-plugin": [0],
"python": [7],
"aufzunehmen": [[2,5]],
"segmentiert": [0,3],
"suchausdruck": [0],
"office-applikationen": [7],
"tabulatorzeichen": [0],
"benutzereinstellungen": [0],
"getestet": [[0,2]],
"omegat-projekt-quellordn": [2],
"üblichen": [[4,5]],
"hervorgehoben": [4,7,1,5],
"projektpaket": [4],
"ansonsten": [7],
"offiziell": [0],
"match-prozentsätz": [6],
"algorithmen": [7],
"tag-tooltip": [1],
"vielfach": [2],
"zirkumflex": [0],
"läuft": [[0,8]],
"file": [2,7,[0,5]],
"wortgrupp": [7],
"mehrzellig": [7],
"beispielzuordnungen": [2],
"klein": [[4,6]],
"hauptfensterbereichen": [1],
"meng": [2],
"erwähnt": [0],
"zuordnungen": [2],
"instanzen": [0,2],
"ersetzt": [7,[0,4],1],
"überbrücken": [2,[0,6]],
"umschalten": [7,0,1],
"hindern": [2],
"invoke-item": [0],
"med-paket": [4],
"tausendertrennzeichen": [0],
"farben": [[1,4]],
"projektstruktur": [2],
"konto": [2],
"cjk-zeichen": [7],
"braucht": [2],
"source-pattern": [2],
"übersetzungsprojekt": [3,6,[1,7]],
"variablenlist": [[0,1,7]],
"ausgeführt": [[2,7],[1,4]],
"benutzernamen": [2],
"obigen": [4],
"workflow": [3,[0,8]],
"sorg": [3],
"autocompletertablepagedown": [0],
"befehlszeilenbasierten": [7],
"lesbar": [0],
"bearbeiteten": [5],
"benutz": [[0,1,4]],
"ursprünglich": [2,[1,3]],
"problematisch": [3],
"probleme": [8],
"sichtbar": [0,6],
"angezeigt": [7,[1,5],0,4,6,2,3],
"dateimanag": [[2,6]],
"task": [2],
"endgültig": [6],
"dorthin": [2],
"xliff": [2,0],
"tast": [0,[1,5],[4,7]],
"true": [0],
"header": [0],
"position": [8],
"tag-attributwert": [0],
"menüpunkten": [0],
"beitreten": [3],
"groovi": [7],
"suchergebniss": [7],
"identifik": [[0,4]],
"deren": [[0,2],1],
"best": [2],
"wörterbücher": [1,6,[3,5],4,[0,7,8],2],
"memory-datei": [7],
"texteingab": [5],
"tastenkürzel-definitionsdatei": [0],
"befehlsparametern": [1],
"pdf-dokument": [2],
"laufwerken": [7],
"menü": [4,5,7,3,0,1,6,[2,8]],
"autovervollständiger": [8],
"abhängig": [[0,4]],
"bestätigungsfenst": [7],
"bewirkt": [0,[2,5]],
"zweck": [[0,2,4]],
"verfügbaren": [7,2,[0,1,4]],
"zeilenvorschub": [0],
"segmenten": [7,4,1,[3,6],5],
"letzten": [0,4,7,[2,5]],
"eigen": [2,[0,5]],
"en-nach-fr-projekt": [2],
"würde": [[0,1,6],[2,7]],
"master": [2],
"tmx-level": [7],
"abgetrennt": [3],
"schaltfläch": [7,3,0,1],
"eindruck": [7],
"informieren": [0,2],
"pdf-datei": [2],
"tastenbindung": [7],
"beispielsweis": [7,2],
"dalloway": [1],
"rubi": [7],
"resource-bundl": [2],
"eigenen": [7,2,0],
"yyyi": [2],
"external_command": [6],
"websuchvorgängen": [1],
"stoßen": [[3,4,5]],
"editorselectal": [0],
"optimiert": [7],
"globale": [8],
"existiert": [2],
"sowi": [0,2,7,[3,4]],
"ersetzungen": [[4,7]],
"runner": [7,0],
"verlaufsvervollständigung": [[0,1]],
"pfeilnavig": [3],
"hierbei": [7],
"benennen": [2,3],
"anzeigt": [[1,3,4]],
"beendigung": [7],
"konsist": [4],
"omegat-default": [2],
"aktiviert": [4,0,1,5,2],
"dekorierten": [3],
"anweisungstext": [0],
"user.languag": [2],
"regex": [0],
"meta": [0],
"suchvorgängen": [7],
"programm": [2,3,[0,1,4,5,7]],
"frei": [[3,5]],
"regex-beispiele": [8],
"einholen": [1],
"ressourcen": [3,7,6,0],
"systemweiten": [0],
"global": [7,0,1,4,[3,5]],
"später": [3,[0,2,5,6,7]],
"racin": [5],
"regel": [0,2,[1,7]],
"neustarten": [6],
"zeichnungen": [0],
"gebrauch": [[2,7]],
"hinzu": [[2,3,7],0,6],
"lesen": [2,[0,7]],
"dateien": [2,7,3,[0,6],4,5,1,8],
"ruft": [2],
"ibm": [[1,2]],
"tm-match": [1,5],
"versionskontrollierten": [2],
"einbeziehen": [7,1,[2,4,6]],
"geführt": [2],
"unterstützten": [2,6,[0,3,4,7]],
"parsewis": [7],
"remote-desktop-sitzungen": [2],
"zusammenfassen": [0],
"nebensätz": [0],
"ergebniss": [7,[3,4],[0,1,2,5]],
"bedeutung": [0,[5,7]],
"benachrichtigungen": [5],
"hervor": [7],
"variieren": [2],
"erklärung": [0],
"omegat-cod": [2],
"befehlszeilenschnittstell": [2],
"office-suite-dateien": [2],
"arbeitsablauf": [[3,5]],
"idx": [6],
"abgrenzt": [7],
"tm-matches": [8],
"befindlichen": [[1,2,5]],
"erhalten": [[2,5,7],[0,1],[3,4]],
"jede": [0,2,6,[3,4,7],1],
"erstellten": [2,[3,4,6,7]],
"faustregel": [7],
"autocompleterconfirmandclos": [0],
"interakt": [2],
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
"rechtsklick": [5,[4,6]],
"variablenmust": [1],
"ifo": [6],
"excit": [0],
"modifikatoren": [[0,3]],
"erzeugen": [[0,2,4,6]],
"optionsmtautofetchcheckboxmenuitem": [0],
"zweistelligen": [2,[3,7]],
"kehrt": [7],
"zeilenumbrüch": [0],
"xx.docx": [0],
"erkennen": [3,6],
"ige": [6],
"wert-dateien": [7],
"kommunikationsproblem": [5],
"kommentierung": [2],
"textdatei": [[0,4,7],1],
"rund": [0,1],
"dokument": [0,[2,7],3,5,[4,8]],
"negativ": [7],
"applikationsordn": [0,[1,7]],
"hin-und-her-konvertierung": [2],
"modernen": [2],
"textdaten": [7],
"überschreiben": [5,6,[0,2,4]],
"editorshortcuts.properti": [0],
"standardverteilung": [7],
"aufzuteilen": [7],
"französisch": [2,1,7],
"dekorativ": [3],
"maschinel": [[1,4],[0,5]],
"ihn": [0,[2,5],3,[4,6,7]],
"ihm": [[0,6]],
"externen": [6,[1,3,7],[2,5]],
"ihr": [2,0,7,3,6,1,5,4],
"tatsächlichen": [2],
"dateifilt": [0,7,1,2,4,3,6],
"projekteigenen": [4,[0,2,5,6,7]],
"verloren": [[2,4],5],
"diesem": [7,2,6,[0,1,3,4],5],
"zielsprachen-token": [2],
"tastenkürzeln": [3,4,0,[5,8]],
"diesen": [7,2,6,3,[0,4],1],
"zielsprachencod": [1],
"tmotherlangroot": [0],
"stoppt": [1],
"geeigneten": [2],
"fall": [0,[2,5,7],1,[3,4]],
"viewmarknotedsegmentscheckboxmenuitem": [0],
"ältere": [2],
"vielleicht": [2],
"bietet": [7,[0,2,3],[1,5]],
"https-protokol": [2],
"algorithmus": [7],
"aufgeführt": [0,[3,7]],
"gruppen": [[0,1,7]],
"editorfunktionen": [0],
"behandeln": [[1,7],2],
"hinaus": [7],
"dessen": [2,[1,3,6],4],
"exkludiert": [0],
"übersetzbaren": [7,0],
"ausführbar": [0],
"näher": [1],
"gesucht": [0],
"gotomatchsourceseg": [0],
"bieten": [0,2,1],
"unmittelbar": [[0,4]],
"behandelt": [0,5,[1,6,7]],
"excel": [0],
"häufigst": [5],
"comma": [0],
"omegat-tag": [1,4,3],
"runn": [7],
"zieldateinamensmust": [0],
"lesbarkeit": [1],
"dieses": [8],
"dieser": [7,0,2,6,4,[1,3],5,8],
"klassenbeispiele": [8],
"runt": [0],
"stardict": [6],
"omegat.l4j.ini": [2],
"first": [5],
"span": [0],
"resultierend": [2],
"prefer": [0],
"heben": [2],
"weiterzumachen": [3],
"deshalb": [[0,2]],
"space": [0,7,5],
"wurden": [2,7,[3,5],[0,1,4],6],
"struktur": [7,[0,3,6]],
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
"übersetzungen": [5,6,3,1,4,[0,2],[7,8]],
"halten": [7,3],
"fals": [0,2],
"besucht": [4,6],
"project.projectfil": [7],
"aufzubewahren": [6],
"übersetzten": [7,3,0,[4,5],[1,2,6],8],
"macos-dateien": [0],
"parameterdatei": [0],
"variablennam": [0],
"zuletzt": [[1,4],[2,5],[0,6,7]],
"referenzglossaren": [0],
"reduzieren": [[2,7]],
"einschließlich": [0,1,[2,3,4,5,6,7]],
"rechten": [[5,7],4],
"dienstprogrammen": [2],
"manuel": [7,[0,6],[2,3],4],
"überspringen": [0],
"zugänglich": [7,[0,2,4,6]],
"frequenz": [2],
"glossarbegriffen": [4],
"shortcut": [7,2],
"public": [2,8],
"anzahl": [7,5,0,4,[1,6],[2,3]],
"konfigurationsordn": [0,2,1,[3,4],7],
"dreistelligen": [[3,7]],
"tmx2sourc": [[0,2,6]],
"hilfe-menü": [[0,8]],
"zieldateinam": [0],
"orangen": [0],
"gesetzten": [1],
"kanada": [2],
"eingegeben": [3,[0,1,5],7],
"remote-standort": [2],
"fixieren": [5],
"anstell": [7,[0,2]],
"synchronisiert": [2,7,[1,3,5]],
"begrenzungen": [4],
"gute": [8],
"plattformübergreifend": [2],
"dient": [2,[0,1,6]],
"dhttp.proxyport": [2],
"installierten": [[1,3,5]],
"einführung": [[3,8],2],
"hauptteil": [7],
"zeichenklass": [0],
"benutzerunfreundlich": [4],
"computerproblem": [3],
"n-te": [7],
"subrip": [2],
"authentifizierten": [1],
"genauer": [[6,7]],
"tastaturen": [0],
"übersetzen": [7,2,[0,3],5,[1,4,8]],
"tabulatorgetrennten": [0],
"textfilt": [0],
"sicherungsmedium": [2],
"vereinfacht": [[2,7]],
"aktivieren": [[1,4],[0,6,7],[2,5]],
"versehen": [2,[0,1,4,6]],
"score": [1],
"umgebrochen": [0],
"geeignet": [7,3],
"gefolgt": [0,1,[2,3]],
"initialisieren": [2],
"wiederkehrend": [2],
"rat": [2],
"raw": [2],
"physisch": [2],
"schließt": [7,4,[0,2]],
"diagramm": [0],
"kombin": [0],
"respektieren": [2],
"wünschen": [8],
"unten": [7,0,2,6,[1,3,5]],
"aufsteigend": [0],
"unter": [7,2,4,0,[1,3],[5,6],8],
"statusmeldungen": [0],
"beschließen": [3],
"führend": [0],
"unbeliev": [0],
"prioritäten": [1],
"überprüfung": [2],
"close": [7],
"egal": [0,2],
"abc": [0],
"glossareintragsdialogfenst": [3],
"linken": [7,[4,5]],
"bewegen": [4,5,1],
"lizenzrechtlichen": [2],
"startmenü": [2],
"pos1": [0],
"betreten": [5,[0,1,3]],
"eingab": [2,4,[1,7]],
"gruppieren": [[1,5]],
"wobei": [0,2,5,[1,3,6,7]],
"toolbar.groovi": [7],
"bequem": [[2,7]],
"quellsegmenten": [[5,7]],
"invertiert": [1],
"pluralspezifik": [0],
"zuverlässig": [6,2],
"vorgestellt": [3],
"glossareinträgen": [4],
"acht": [0],
"zugriff": [4,2,1,7,[0,3]],
"folienlayout": [0],
"iso": [[0,2]],
"ordnern": [7,[2,3],6],
"glossardatei": [5,0,7],
"satzend": [1],
"ist": [0,2,7,5,1,4,6,3,8],
"optionspreferencesmenuitem": [0],
"sprachordn": [0],
"zum": [0,[4,7],2,1,5,6,3],
"verlaufen": [0],
"post": [0],
"scripts-ordn": [7],
"zur": [2,[0,7],1,[3,4],[5,6]],
"glossary.txt": [[2,6],[0,4]],
"prozentsätz": [5],
"gegensatz": [[0,1]],
"versionskontrollsystem": [2],
"interagieren": [7],
"ländern": [1],
"projektdateien-fenst": [[0,7]],
"ausführung": [2,0],
"paket": [2],
"referenz": [7,[0,3],2],
"umwandlung": [1],
"konvertierung": [2],
"add": [2],
"stehen": [1,[0,7],[2,3]],
"endung": [0,2,[4,6,7]],
"jeweilig": [[0,7]],
"datensicherung": [2],
"aufbewahren": [2],
"übersetzungsarbeiten": [2],
"parsen": [7],
"rfe": [7],
"entry_activ": [7],
"optionsautocompleteshowautomaticallyitem": [0],
"gotoprevxautomenuitem": [0],
"deaktivieren": [4,7,[1,5],[0,2]],
"benutzersprach": [2],
"zugehörigen": [[0,1],[2,5,7]],
"platzhalterzeichen": [7,0],
"ermöglichen": [[0,2]],
"besser": [3,7,[0,2]],
"ishan": [0],
"pasta": [0],
"zusammen": [0],
"espac": [2],
"modifi": [7],
"zieldaten": [6],
"existierenden": [3,1],
"font-fallback-mechanismus": [4],
"terminologie-unterordn": [0],
"ausprägung": [1],
"voraus": [2],
"projektkonfigur": [2],
"zieldatei": [0,4,7],
"überschüssig": [0,1],
"allgemein": [1,7,[2,8]],
"standardmethod": [5],
"absoluten": [0],
"proxy-login": [1,8],
"web-interfac": [2],
"remote-dateien": [6],
"verschlüsselt": [0],
"umfassend": [2],
"targetlanguag": [[0,1]],
"zielordn": [0],
"falsch": [[4,7]],
"ssh-authentifizierung": [2],
"backup": [2,6],
"problemlo": [3],
"kodierungen": [0],
"folgend": [0,2,[5,7],1,[3,4,6]],
"editselectfuzzyprevmenuitem": [0],
"stunden": [2],
"sämtlich": [[2,7]],
"gui-applik": [2],
"üblich": [2],
"regulärem": [3],
"regulären": [0,7,2,[1,3]],
"tag-problemen": [[2,4],1],
"git-client": [2],
"aktualisieren": [2,7,6],
"reellen": [2],
"weitermachen": [7],
"auszulösen": [7],
"algorithm": [4],
"zeilenblock": [7],
"ausnahmestell": [1],
"remote-ordnern": [7],
"deaktivierung": [0],
"autovervollständig": [0,1],
"einleitend": [0],
"merkwürdig": [2],
"script": [7,0,1,[2,4]],
"schützen": [2],
"japanisch": [2,[0,1]],
"system": [2,0,7,[3,5,6]],
"deklar": [0],
"spellcheck": [7],
"projekteigen": [6,0,2],
"warum": [0],
"unnötig": [1],
"richtung": [0],
"zuweisen": [0,[2,4,7]],
"zeichenketten": [7,[0,3],2],
"originalübersetzung": [[0,7]],
"neben": [7,0,5,4],
"zunächst": [2,[0,3]],
"local": [2,6],
"vorgegebenen": [[4,7]],
"einzelheiten": [7,2,3,[0,4],1,6,5],
"wiederzugeben": [0],
"aspekt": [3],
"zunichtemachen": [1],
"rle": [0,4],
"nach": [0,7,[1,2],[4,5],[3,6],8],
"prozentanzeig": [5],
"glossar-match": [5,4,[0,3]],
"repo_for_all_omegat_team_project_sourc": [2],
"repräsentiert": [0,[1,2,6]],
"login-id": [1],
"oder": [0,7,2,4,3,5,1,6],
"rlm": [0,4],
"java-resource-bundl": [0],
"effizi": [3],
"wörterbuchnam": [3],
"segmente": [8],
"weiterzugeben": [8],
"führt": [[0,4]],
"kombinationen": [0,7],
"angewiesen": [[2,6]],
"sucht": [1,[2,7]],
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
"navigationsbefehl": [6,[2,5]],
"all": [7,2,0,1,4,6,3,5],
"statusleisteninformationen": [5],
"read": [0],
"eingefügt": [4,6,1,[2,3],7,5],
"c.t": [0],
"alt": [0,2,4,[1,7]],
"beschreibt": [0,2],
"gewöhnt": [4],
"quellsprach": [0,1,[2,5,6,7]],
"schauen": [[0,3]],
"auffordern": [3],
"navigiert": [4],
"lesezeichenreferenzen": [0],
"wiederverwenden": [2,3,[1,4]],
"übereinstimmt": [0,3,1],
"wiederverwendet": [[0,2]],
"zusammengefasst": [[0,7]],
"grammatikalisch": [[4,7]],
"verteilung": [7],
"erwartet": [2,[0,1]],
"omegat-inhalt": [0],
"tkit": [2],
"rot": [[0,1,6,7]],
"and": [[1,7]],
"erwarten": [0],
"wahrscheinlich": [2],
"match-bericht": [4],
"fehlerbehebung": [2],
"zahlen": [1,5,0,7],
"benachrichtigen": [5],
"projektinhalt": [0,3,6,[2,4,7]],
"wellenlinien": [1],
"sortiert": [[5,7]],
"alternativ": [7,0,2],
"teamprojekt": [2,4,6,[0,7,8],[1,5]],
"herabgestuft": [6],
"kostenlo": [2],
"eingeschlossen": [2,1],
"fehlend": [4,0,3,2],
"helplastchangesmenuitem": [0],
"standardbrows": [1,[4,5]],
"komprimieren": [0],
"erkennt": [4,0,[1,2,3,7]],
"zeilenumbruch": [0,7],
"omegat.ex": [2],
"e-mail-adress": [0],
"exitcod": [0],
"sourcetext": [1],
"quelldokument": [0],
"erreichen": [[0,2,3,7]],
"gezeigt": [0],
"gui-einstellungen": [6],
"content-part": [0],
"erlaubt": [[0,7]],
"apache-ant-syntax": [7],
"jar": [2],
"api": [7],
"bleibt": [2,7,[0,5]],
"editselectfuzzy2menuitem": [0],
"leitet": [0],
"startparamet": [2],
"dateinamensmust": [0],
"anfangsseg": [7],
"wortzahl": [4],
"beliebig": [0,2,7,6,5],
"funktionsweise": [8],
"umschlossenen": [0],
"extrahieren": [[1,7]],
"unterhalb": [5],
"alternativen": [1,[0,5]],
"projektspeich": [2],
"editornextseg": [0],
"standardkodierung": [0],
"angewendet": [[0,2],1],
"standarddarstellung": [1],
"übung": [0],
"trennbalken": [5],
"untersuchen": [1,2,[3,4],0],
"ähnlich": [2,0,[1,3,7]],
"belegen": [7],
"editselectfuzzynextmenuitem": [0],
"standard-projektzuordnung": [2],
"belieben": [[0,7],1,[2,6]],
"gotonextxautomenuitem": [0],
"proportionalen": [1],
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
"fähigkeiten": [2],
"xliff-unterstützung": [2],
"einbettung": [[0,4]],
"art": [7,[0,1,6]],
"translation-memory-dateien": [[2,6,7],3],
"wörterbuchtreib": [1],
"sonderzeichen": [0],
"gehe": [5,3,4,[0,2,6,8],1],
"navigieren": [[3,5,6],[2,4]],
"call": [0],
"buchstab": [0,4],
"remote-eigenschaften": [4],
"einheit": [0],
"übergeben": [[0,2]],
"html-kommentaren": [0],
"aufgehört": [3],
"tabul": [[0,2]],
"segmentierungsregelsätz": [1],
"platzieren": [0],
"mustern": [7],
"fragezeichen": [0],
"bidi-steuerzeichen": [0,4],
"toolsshowstatisticsmatchesperfilemenuitem": [0],
"spezifisch": [0],
"run": [7,0,2],
"nächste": [0,[4,5],[2,3,7]],
"sprachregionen": [1],
"maximierten": [5],
"leicht": [[0,3,6]],
"editorshortcuts.mac.properti": [0],
"vier": [0,[2,4,7]],
"vornehmen": [6,[0,2,7]],
"fragen": [0,[2,5]],
"titlecasemenuitem": [0],
"lizenzinformationen": [4],
"prüfungen": [3],
"editcreateglossaryentrymenuitem": [0],
"jeglich": [6],
"reih": [0,2,1,[3,6,7],4],
"regelsätz": [1],
"auszufüllen": [4],
"umschließen": [0,3],
"lohnen": [3],
"viel": [[0,2],3],
"rein": [[0,7]],
"vorbereiten": [0],
"auf": [7,0,2,[3,4],1,5,6,8],
"introduc": [7],
"多和田葉子": [7],
"verknüpfen": [0,2],
"hauptfenst": [5,[1,4]],
"name": [0,1,[2,3,5],7],
"aus": [2,0,7,6,1,4,[3,5]],
"rechtschreibwörterbücher": [8],
"okapi-framework-dateifilt": [2],
"book": [0],
"show": [7],
"lernen": [[0,3]],
"bearbeitungsdialogfenst": [0],
"systemen": [2],
"comput": [2,1],
"computergestützte": [8],
"zwischendurch": [2],
"neuesten": [6,3],
"darin": [[1,6,8]],
"hauptzweck": [8],
"verlässt": [0],
"übereinstimmungen": [7,1],
"zielregion": [0],
"editortogglecursorlock": [0],
"enabl": [5],
"enthaltenen": [7],
"dadurch": [6,[0,2,5,7]],
"gelb": [4],
"vorschläg": [5,[0,1]],
"menüfunktionen": [0],
"svn-cleanup": [7],
"verhalten": [2,4],
"wertet": [1],
"new_fil": [7],
"werten": [0,1],
"offline-dienstprogrammen": [2],
"hilfreich": [[3,7]],
"gesamtzahlen": [4],
"target": [1,[4,7],6,3,[0,8]],
"software-dokument": [2],
"gelegentlich": [3],
"spaltenüberschrift": [[0,4,7]],
"namensmust": [0],
"project_save.tmx-datei": [7],
"arabischen": [0],
"config-dir": [2],
"achten": [[0,1,2,3,7]],
"editorskipprevtokenwithselect": [0],
"omegat-fremden": [1],
"konfigordn": [2],
"anwenden": [1,[0,7]],
"verknüpft": [2,[4,6]],
"ebenso": [[1,2,4,5]],
"durchgeführt": [2,4],
"vorgibt": [[2,7]],
"einfachen": [[1,3]],
"platziert": [[2,4]],
"kleinschreibung-sensitiv": [0,7],
"modus": [2,7],
"support-seit": [0],
"anbiet": [1,5],
"umzubenennen": [2],
"vorgesehen": [4],
"aufklapplist": [7],
"speichert": [4,7,2],
"sortierreihenfolg": [4],
"daran": [2,6,0],
"schriftarten": [4],
"matches": [8],
"ablegen": [5,2,0,7],
"properties-format": [2],
"übereinstimmung": [0,1],
"targettext": [1],
"akzeptiert": [7,2,[1,6]],
"schreibrechten": [2],
"zusätzlichen": [7,[3,5]],
"kombinieren": [[2,6]],
"denselben": [[1,2,7]],
"schutz": [1],
"meistern": [4],
"orang": [[5,7]],
"compil": [7],
"lokal": [2,7,1,0,4,5],
"edittagpaintermenuitem": [0],
"voranstellen": [0],
"fuzzy-match-zahlen": [1],
"dateifilter-plugin": [2],
"speichern": [2,[6,7],[0,1],4,[3,5,8]],
"nähern": [3],
"ausschließt": [2],
"wörterbuchdateien": [[5,7]],
"kreativen": [0],
"unicod": [4,0],
"viewmarknbspcheckboxmenuitem": [0],
"zwischen": [[2,5],0,[4,7],[1,3],6],
"korrekturles": [6],
"lizenz": [2,8],
"klammer": [0],
"lingvo-dsl-format": [6],
"automatisierung": [7],
"whitespac": [2],
"sorgt": [7],
"hochgehen": [0],
"derzeitig": [4],
"ausdruck": [0,7,1,3],
"ausgefüllten": [[2,6]],
"anordnung": [[1,7]],
"msgstr": [0],
"java-laufzeitausführung": [0],
"übersetzt": [7,2,0,4,5,[3,6],1],
"separat": [1],
"mehrfach": [3],
"dargestellt": [1,[3,5,7],4],
"erweitert": [7],
"nationalité": [1],
"daili": [0],
"anzuzeigen": [7,3,[1,4]],
"schnellen": [4],
"gern": [[2,3]],
"übersetzbar": [0],
"zwischenablag": [4],
"buchstaben": [0,4,3,[1,2]],
"teamprojekten": [2],
"quadrat": [1],
"zweibuchstabigen": [2],
"auch": [2,7,0,1,3,6,5,4],
"installiert": [2,1,0,4,[3,7]],
"omegat.project": [2,6,3,[1,5,7]],
"hinterlegen": [3],
"excludedfold": [2],
"targetcountrycod": [0],
"durchläuft": [7],
"insert": [0],
"verlangt": [[2,7]],
"fertig": [7],
"vierstellig": [2],
"arabisch": [0],
"quelldoku": [[0,1,4]],
"zieldateinamen": [0],
"streng": [2],
"skriptsprach": [7],
"gesamt": [1,2,[0,3,4,6]],
"abbildungsverzeichnis": [8],
"rest": [[0,3]],
"original": [0,1],
"direkter": [1],
"begriffsgruppen": [1],
"direkten": [2],
"splitten": [7,4],
"also": [0,2,[1,3]],
"auswählen": [[0,4],7,5,[1,2,6]],
"tag-informationen": [7],
"project_save.tmx.zeitstempel.bak": [6],
"größe": [7],
"interpretiert": [0],
"ehesten": [2],
"itokenizertarget": [2],
"viewmarkwhitespacecheckboxmenuitem": [0],
"verfügbar": [1,[0,2],7,5,[3,4]],
"überprüft": [0,1,4],
"asterisk": [0],
"selekt": [2],
"zielsprachen": [1],
"bak": [2,6],
"verstecken": [7,0,[3,4]],
"typisiert": [7],
"ergebnisanzeig": [7],
"offen": [7,0],
"standen": [3],
"teamdateien": [2],
"rechtschreibwörterbuch": [1,[3,4]],
"jre": [2],
"projektmitgliedern": [2],
"heruntergeladen": [2],
"posit": [0,4,5,7,3],
"versioniert": [6],
"fügen": [0,2,3,6],
"grafisch": [2],
"anzugeben": [2,0,7],
"support-kanälen": [0],
"angefertigt": [3],
"registrieren": [[1,2],5],
"scrollen": [[1,3,5]],
"angst": [3],
"dezimalpunkt": [0],
"mitgelieferten": [7],
"projektordner": [8],
"weder": [3],
"alllemand": [7],
"enthalten": [0,2,[6,7],3,5,[1,4]],
"standard-dateimanag": [4,7],
"verwandelt": [0],
"auslöst": [0],
"relativ": [2,0],
"omegat-such": [0],
"geklont": [2],
"schrägstrich": [0,2],
"delet": [0],
"nicht-groß": [0],
"eingespeist": [2],
"bcp": [[3,7]],
"weiteren": [[0,2]],
"projectaccessglossarymenuitem": [0],
"wiederum": [1],
"prüfung": [1],
"sei": [2,[0,1,3]],
"beeinträchtigt": [7],
"beliebige": [8],
"plattformen": [2,[0,1]],
"auftrag": [3],
"set": [1],
"meldungen": [5],
"balis": [5],
"numerisch": [0],
"projektnam": [7],
"bestimmten": [2,1],
"regionscod": [0],
"standardvers": [2],
"lösen": [5,7],
"modifiziert": [7],
"links-nach-rechts-seg": [0],
"standard-termin": [2],
"kriterien": [7,[0,3]],
"änderungen": [2,0,6,7,4,3,[1,5]],
"project.sav": [2],
"featur": [[1,7],[4,5]],
"offic": [0],
"bei": [7,0,2,1,[3,5],[4,6]],
"repositories.properti": [[0,2]],
"beginnen": [0,2,[3,4]],
"kodiert": [[0,6]],
"skriptsprachen": [7],
"ausgeschaltet": [4],
"prozentu": [5],
"textmust": [0],
"benutzerordn": [[0,2]],
"standardressourcen": [3],
"projektordn": [4,[6,7],0,2,3,1],
"unbegrenzt": [3],
"repositories": [8],
"verhindern": [0,4,[2,3,7]],
"mindestschwell": [1],
"projectsavemenuitem": [0],
"ausfüllen": [1],
"runter": [1],
"xmx6g": [2],
"übersetzungsressourcen": [8],
"verhindert": [2],
"mausklick": [7],
"autocompletertablefirstinrow": [0],
"versuch": [2,0],
"einzutragen": [0,[3,5]],
"niedrigst": [5],
"ausdrücklich": [[2,4]],
"blockieren": [4,1],
"gleichzeitig": [[3,7],[1,2,4]],
"tmx-aktualisierungen": [2],
"sogenannt": [[0,7]],
"tabellenansicht": [5],
"tmautoroot": [0],
"macht": [3,[0,2],[1,4]],
"übersetzungsprozess": [[2,3]],
"umgestellt": [0],
"startfähig": [2],
"zurück": [7,3,4,0,[1,2],[5,6]],
"angesehen": [0],
"suchfenst": [7,0,[3,4]],
"lädt": [4,7,1,3],
"eher": [0],
"sperren": [[2,3]],
"insertcharslrm": [0],
"unerwünschten": [4],
"teammitgliedern": [[2,3]],
"autotext-paramet": [0],
"einblenden": [[5,6]],
"sie": [2,7,0,3,1,4,6,5,8],
"aufgeteilt": [[1,3,7]],
"personen": [2],
"daher": [[2,7]],
"dialogfenster": [8],
"zugewiesenen": [6],
"standardeinstellungen": [[0,2]],
"bestätigung": [1],
"belassen": [6],
"dateiformat": [0],
"unwahrscheinlich": [3],
"texteditor": [0,6,[2,7]],
"objektorientiert": [7],
"dritt": [2,6],
"gesperrt": [2],
"sortieren": [0,1],
"fügt": [7,4,[0,1,6]],
"jemanden": [2],
"standardordn": [7],
"foundat": [2],
"statistisch": [7],
"hängt": [[1,2],[0,4]],
"targetroot": [0],
"tag-bearbeitung": [1],
"bin": [0,[1,2]],
"gehören": [0,[2,3,5],1],
"apertium": [1],
"anpassen": [0,1,4],
"bis": [0,7,[1,2],3,6],
"meta-inf": [2],
"sich": [2,0,3,7,4,5,1,6],
"regex-beispiel": [0],
"autovervollständigungsmenü": [1,[3,5]],
"projectopenmenuitem": [0],
"autom": [2],
"dark-theme-erkennung": [1],
"multi-paradigma-sprach": [7],
"helfen": [[2,3,4,7,8]],
"eingefügten": [6,2],
"funktion": [4,0,3,1,7,2],
"unicode-blöck": [0],
"dateifreigabesystem": [2],
"whitelist": [2],
"grün": [7,[4,5]],
"autor": [[3,4]],
"beteiligt": [2],
"unerwartet": [2],
"textcursormodus": [5,0],
"befehlen": [[0,1,7],2],
"fachgebiet": [2],
"beiden": [7,0,[1,2,5]],
"begin": [0],
"formatierungszeichen": [[0,4],8],
"viewmarktranslatedsegmentscheckboxmenuitem": [0],
"auszug": [0],
"zehn": [[2,7],4],
"ausgelegt": [2],
"ilia": [2],
"sieh": [[2,4],0,5,[1,7],6,3],
"unterstrichen": [5],
"reserviert": [2],
"schreibt": [[2,7]],
"veranschaulichen": [3],
"potenziell": [[0,1,6]],
"leistung": [1],
"unicode-formatierungszeichen": [4],
"verwandt": [6],
"erforderlich": [2,1,[0,7]],
"ersatz": [7],
"sperrig": [4],
"einfachst": [2,0],
"datenspeicherung": [1,8],
"übersetzungsdaten": [2],
"uxxxx": [0],
"basierender": [8],
"hier": [1,[0,5],7,[2,4,6],3],
"rechtsklicken": [7,[2,5]],
"macos": [8],
"bearbeitungsbeispiel": [0],
"richtig": [[2,3,4]],
"erstellen": [2,7,[0,3],4,6,1,5,8],
"editselectfuzzy1menuitem": [0],
"standort": [[1,2]],
"bibliotheken": [0],
"hide": [5],
"auszuwählen": [7,0,[3,5]],
"autocompleterlistpagedown": [0],
"auto": [4,[0,6],2,[1,7]],
"eigenschaft": [5],
"document.xx.docx": [0],
"editorskipnexttokenwithselect": [0],
"standard-xliff-filt": [2],
"quelldateien": [2,3,4,7,0,6,[5,8]],
"anregungen": [0],
"ratsam": [2],
"download": [2],
"editortoggleovertyp": [0],
"setzt": [7,1,[0,2,3,4]],
"wagenrücklaufzeichen": [0],
"optionen-menü": [[0,8]],
"erfordern": [[0,1,3,4,5,7]],
"funktionscod": [0],
"zusammenhang": [1],
"erfordert": [2,1,[0,7]],
"administr": [2],
"gradlew": [2],
"kann": [2,0,7,4,[3,5,6],1],
"angibt": [[2,6]],
"level": [7],
"wurd": [4,[1,2],[0,7],6,3],
"statistikdatei": [6],
"standardanordnung": [1],
"remote-datei": [2],
"fehlerbericht": [0],
"zeit": [3,2],
"eigentlich": [3],
"angaben": [3],
"relevant": [[0,1]],
"einzustellen": [1,0],
"zeig": [7],
"ocred-pdf-dateien": [7],
"zeil": [0,7,[4,5],[1,2],6],
"viewmarklanguagecheckercheckboxmenuitem": [0],
"abschreckend": [0],
"kaputt": [3],
"fünf": [1],
"quellbegriff": [[0,4],[3,5]],
"ausgerichtet": [4,[2,7]],
"zweifel": [6],
"vorherigen": [[0,4],[3,7],[2,5]],
"tasten": [0,5],
"quelltext": [1,5,4,7,0,[3,6],2],
"geltungsbereich": [0],
"ungeeignet": [5],
"währungsübersetz": [7],
"switch": [[1,7]],
"zell": [7],
"bundl": [2],
"für": [2,0,7,4,1,3,6,5,8],
"einträgen": [7],
"erreicht": [2],
"präsentiert": [2],
"senkrecht": [0],
"src": [2],
"gigabyt": [2],
"kopiert": [[4,5,7],2,[0,1],3],
"control": [[0,4]],
"glossareintrag": [7,1,5,0,[3,4,6]],
"no-team": [2],
"kommentarzeil": [0],
"nützlichen": [4],
"api-schlüssel": [1],
"fragwürdig": [7],
"extrem": [2],
"kopieren": [2,[0,6,7],[1,3,5]],
"lissens": [0],
"paketmanag": [2],
"authentifizierung": [2],
"keinerlei": [1],
"ausführlich": [[0,2]],
"eingabetext": [5],
"unübersetzten": [7,[1,2,3]],
"ssh": [2],
"maschinell": [1,[4,5],[0,3,6]],
"hilf": [[0,4],[2,3,5]],
"environ": [2],
"endungen": [[0,6,8]],
"unteren": [7,[1,5],[2,4]],
"zuordnung": [2,7],
"stichwortsuch": [7,3],
"qualitätssicherung": [[4,7]],
"friend": [0],
"modifizierten": [7],
"zeigen": [0,[1,2]],
"derselb": [0],
"zusätzlich": [0,[2,7],[1,4],6],
"dokumentationsordn": [0],
"eintrag": [[0,5],4],
"individuel": [[0,1]],
"vorbereiteten": [6],
"textdateien": [0,6],
"benutzergrupp": [2],
"variablenvarianten": [0],
"mehrmalig": [[1,4,5,7]],
"denn": [2,[0,1,3]],
"dynamisch": [7],
"gezogen": [5],
"übersetzungsstand": [2,[3,6,7]],
"currenc": [7],
"languag": [7,2],
"großteil": [0],
"berücksichtigung": [5],
"current": [7],
"projekteigenschaften": [2,[6,7],[1,3,4],8],
"teamprojekt-funktionalität": [2],
"skripten": [[0,7],[2,3,6]],
"optionsglossaryfuzzymatchingcheckboxmenuitem": [0],
"freigab": [2],
"key": [7],
"hören": [0],
"heißt": [0],
"omegat-konfigurationsdateien": [[1,2,4]],
"msgid": [0],
"dateistruktur": [0],
"svn": [2,7],
"omegat-license.txt": [0],
"quell": [7,[0,1,4],2,6,[3,5]],
"stori": [0],
"programmierstil": [7],
"nutzung": [0,2,7,[1,6],3],
"blauer": [7],
"artikel": [0],
"längere": [1],
"omegat-entwicklern": [7],
"stoppen": [1],
"wäre": [0],
"stammt": [2],
"unterstützt": [2,7,3,[0,1,6]],
"editreplaceinprojectmenuitem": [0],
"but": [0],
"symbol": [5,2,[4,7]],
"editordeletenexttoken": [0],
"problemdialogfenst": [1],
"entscheiden": [2],
"express": [0],
"html-datei": [0],
"richtet": [0],
"währungssymbol": [0],
"horizontal": [0],
"tastenmodifikatoren": [3],
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
"projektbasiert": [7],
"gotopreviousnotemenuitem": [0],
"stderr": [0],
"editredomenuitem": [0],
"uilayout.xml": [[0,6]],
"ausrichtet": [[1,3]],
"sourceroot": [0],
"wählen": [1,7,4,2,[0,3,5]],
"punktuell": [7],
"größeren": [0],
"überarbeiteten": [3],
"schlägt": [7],
"sind": [0,7,2,1,4,5,6,3],
"ausrichten": [7,[2,4],[0,3,8]],
"gelöscht": [2,1,3],
"vorzunehmen": [[0,2,7]],
"priorität": [[1,2]],
"übermitteln": [2],
"entgegengesetzt": [0],
"trennung": [[0,2]],
"repository-zugangsdaten": [2,1],
"verbunden": [0,3],
"textcursorstatus": [5],
"verhält": [2,0],
"besonder": [0],
"cat-tool": [[2,7]],
"portnumm": [2],
"normal": [0,7,[1,2,3,5,6]],
"zuzuordnen": [2],
"befassen": [2],
"beschleunigen": [3,2],
"übersetzungsdiensten": [1],
"verglichen": [7,3],
"problemen": [4],
"grafiken": [0],
"kontextbeschreibung": [1],
"entwicklern": [2],
"example.email.org": [0],
"aggressiv": [0],
"sprachreferenz": [[2,6]],
"werden": [0,2,7,1,4,5,6,3],
"mediawiki-seit": [[0,4]],
"strukturel": [2],
"welch": [0,7,[2,3,5]],
"integriert": [1],
"runtim": [2],
"treffen": [[2,6]],
"treffer": [1,3,[6,7]],
"mehrer": [0,[2,7],5,[1,4,6],3],
"faktoren": [4],
"gui-script": [7],
"produktivität": [0],
"review": [7],
"filenam": [0],
"fehlermeldung": [2],
"zahlenbereich": [0],
"roam": [0],
"fehlt": [0],
"bzw": [[0,7],[2,4]],
"zugriffsrecht": [2],
"nbsp": [7],
"funktionieren": [0,2],
"übereinstimmend": [6,1],
"verdoppelt": [0],
"gotosegmentmenuitem": [0],
"definitionen": [1],
"geben": [7,2,0,1,3,[4,5]],
"geordnet": [0],
"eingeben": [[2,7],3,[1,5]],
"eingerichtet": [2,6],
"omegat-funktionen": [[0,3,7]],
"originalsprach": [5],
"ziffer": [0,7],
"dekorationen": [3],
"initialcreationd": [1],
"bereitgestellt": [2,4],
"schriftauszeichnungen": [0],
"systemkurzbefehl": [3],
"helpaboutmenuitem": [0],
"verzicht": [0],
"verlaufsvorhersag": [[0,1]],
"projektmitglied": [2],
"standardübersetzung": [[4,5],[0,7]],
"informationen": [4,2,0,5,7,[1,6],3],
"ansicht-menü": [[0,8]],
"xml-dateien": [0],
"regular": [0],
"aufrufbar": [[0,2]],
"viert": [2],
"satzeben": [0,[3,7]],
"fließt": [0],
"verstehen": [0],
"einzufügen": [[4,5],1,[0,3],6],
"omegat-dateifiltern": [7],
"runtergehen": [0],
"omegat-vers": [2],
"token": [0,[1,7],[2,5,6]],
"filter": [0,7,2,4],
"site": [1],
"projectroot": [0],
"editieren": [7],
"manuelle-korrektur-schritt": [7],
"omegat.log": [0],
"angehend": [3],
"autocompletertableright": [0],
"argument": [0],
"ausgelöst": [2],
"pfeiltasten": [7,5,0],
"terminologieproblem": [4],
"sprachprüfdienst": [1],
"vorkommt": [5,0],
"abgelehnt": [1],
"links-nach-rechts-markierung": [[0,4]],
"grundeinheit": [7],
"tab": [0,[4,5],1],
"remote-repositori": [2,[5,6]],
"divers": [3],
"satzschreibung": [[0,4]],
"exportierten": [[4,7]],
"breit": [7,2],
"tag": [0,1,3,7,4,2,5],
"versionen": [2,[7,8]],
"ssh-konfigur": [2],
"tabellenzeil": [0],
"glossarordn": [[0,2,5,6]],
"vorherig": [0,4,[3,5]],
"standardsprach": [2],
"warnung": [7,0,1,[2,3],4,6,5],
"gefunden": [0,7,[1,5],[2,3,4]],
"projectreloadmenuitem": [0],
"richtigen": [7,[1,3]],
"person": [7,2],
"schreibrichtung": [5],
"navig": [[3,5]],
"wartungsarbeiten": [2],
"arten": [0,2,4],
"variationen": [0],
"zweisprachig": [7],
"prozent": [1],
"tbx": [0,1],
"can": [0],
"leer": [0,6,[2,5],4,[1,7],3],
"absatz": [0,7,1,[3,5]],
"cat": [[0,3]],
"genauso": [6],
"tabellen": [0],
"angrenzend": [7,5],
"ausgewählten": [[4,7],0,[2,3,5]],
"regeln": [[1,7],0,4],
"markieren": [4,7,[0,5]],
"duser.countri": [2],
"kommentar": [0,5,3,4],
"cursortasten": [3],
"readm": [0],
"betriebsmodus": [1],
"match": [4,1,5,6,2,0,7,3],
"prüft": [0,1],
"angewandt": [1],
"weisen": [4],
"behebung": [3],
"einschränken": [3],
"schriftgröß": [1],
"fortzusetzen": [[3,4]],
"auto-vervollständig": [1],
"align.tmx": [2],
"englisch": [[0,2]],
"file2": [2],
"ersichtlichen": [2],
"dennoch": [3],
"makro": [7],
"ansicht": [5,1,[3,4],[7,8],[0,6]],
"translation-memory-datei": [2,3,7],
"eigennamen": [5],
"bewegt": [0,4]
};
