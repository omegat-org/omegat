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
 "Appendices",
 "Preferences",
 "How To...",
 "Introduction to OmegaT",
 "Menus",
 "Panes",
 "Project Folder",
 "Windows and Dialogs",
 "OmegaT 5.8.0 - User Manual"
];
wh.search_wordMap= {
"cancel": [0,4,7],
"altgraph": [0],
"stats-typ": [2],
"half": [1],
"upload": [2,4],
"don\'t": [6],
"your": [2,3,0,7,1,4,5,[6,8]],
"without": [2,7,0,1,6,5],
"these": [[0,7],6,2,[3,5]],
"would": [[0,6],[2,7]],
"xml": [0,2,1],
"ten": [[2,7],4],
"sake": [2],
"info.plist": [2],
"i.e": [0],
"sometim": [0,2],
"serv": [2],
"thus": [[1,2,3]],
"neutral": [0],
"noun": [5],
"scratch": [2],
"click": [7,[3,5],1,[0,4],2],
"insensit": [0,7],
"fuzzi": [1,4,5,7,[2,3],6,0,8],
"xdg-open": [0],
"befor": [2,0,1,7,[3,4,6],5],
"size": [1,7,2],
"util": [2],
"left": [[0,5],[3,4,7]],
"seri": [0],
"tar.bz": [6],
"much": [[1,2,3]],
"object": [7,[0,2]],
"arab": [0],
"chapter": [2,[3,7],0,[4,5]],
"ahead": [0],
"yellow": [4],
"shebang": [0],
"turn": [0,2,[3,6,7]],
"suffici": [[0,2]],
"result": [7,[0,2],[3,4],[1,5]],
"edittagnextmissedmenuitem": [0],
"same": [7,2,0,1,3,5,[4,6]],
"editorskipprevtoken": [0],
"checkbox": [7],
"after": [0,2,7,1,3,5,[4,6]],
"quiet": [2],
"flip": [0],
"connect": [[1,2],5],
"hand": [3],
"address": [0,2,[5,6]],
"gnu": [2,8],
"the": [0,7,2,4,1,5,3,6,8],
"wipe": [3],
"straight": [3],
"blue": [7,5],
"projectimportmenuitem": [0],
"obvious": [2],
"imag": [0],
"suzum": [1],
"monolingu": [[0,7]],
"target.txt": [[0,1]],
"goe": [7],
"demonstr": [0],
"temurin": [2],
"standard": [2,3,[0,1,4,5,7]],
"d\'espac": [2],
"correct": [7,[0,1,5],[2,3,4,6]],
"stdout": [0],
"traduct": [5],
"project-bas": [7],
"advic": [2],
"good": [[2,3,8],1],
"omegat.project.lock": [2],
"wish": [[7,8],[2,6]],
"nameon": [0],
"moodlephp": [2],
"currsegment.getsrctext": [7],
"implement": [1],
"alphanumer": [0],
"uncheck": [7,0,1],
"export": [0,[2,4,7],6,[1,3,8]],
"gotonextnotemenuitem": [0],
"area": [5,[2,7]],
"practic": [7,[0,2,6]],
"gpl": [0],
"newentri": [7],
"reduc": [7],
"check": [[1,7],4,0,[2,3],5],
"list": [0,1,2,7,3,4,5,8,6],
"onto": [2],
"autocompleterprevview": [0],
"rainbow-support": [2],
"resolut": [3],
"vowel": [0],
"gotonotespanelmenuitem": [0],
"fr-fr": [3,1],
"ascend": [0],
"ensur": [2,[0,3,7]],
"minim": [6,[1,2,5,8]],
"medium": [2],
"projectcommittargetfil": [0],
"pear": [0],
"determin": [1,7,0],
"root": [0,2],
"combin": [0,[1,2,7],[4,6]],
"po4a": [2],
"japonai": [7],
"omegat.org": [2],
"menus": [[0,5],[4,7],[2,3,6],8],
"hard": [2],
"realign": [7],
"object-ori": [7],
"cjk": [7,0],
"perform": [2,[3,7],[0,1]],
"prewritten": [6],
"alternatives—th": [5],
"maxprogram": [2],
"better": [[3,7],1],
"with": [0,2,7,[3,4],1,5,6],
"pdf": [2,0,4,7],
"there": [2,7,0,[4,5],[1,3,6]],
"syntax": [0,2,7],
"well": [0,2,7,3,[1,4]],
"empti": [2,6,[0,4,5],1,[3,7]],
"autocompletertabledown": [0],
"editornextsegmentnottab": [0],
"toolsshowstatisticsmatchesmenuitem": [0],
"channel": [0],
"focus": [3,2],
"viewdisplaymodificationinfononeradiobuttonmenuitem": [0],
"desir": [2,7,[1,5],0],
"approach": [2],
"variabl": [1,0,[7,8]],
"block": [7,0,4,[1,3,8]],
"tms": [2,[4,6],[0,3,7],[1,8]],
"per": [6,[0,1,4,5,7]],
"write": [7,2,[4,5],[0,1]],
"flow": [0],
"tmx": [2,7,6,[1,3,5]],
"propos": [7],
"order": [1,[0,7],[4,5,6]],
"e.g": [[0,1],[2,7]],
"gtk": [1],
"project_save.tmx.bak": [[2,6]],
"repo_for_all_omegat_team_project": [2],
"cli": [2],
"period": [0,1,2],
"colleagu": [5],
"application_startup": [7],
"proceed": [2,[4,7]],
"eventtyp": [7],
"understand": [[0,2]],
"integ": [1],
"fr-ca": [1],
"mainmenushortcuts.properti": [0],
"ever": [[1,3]],
"projectaccesswriteableglossarymenuitem": [0],
"even": [2,[0,7],1,[3,4],[5,6]],
"aris": [2],
"application_shutdown": [7],
"autocompletertablelastinrow": [0],
"gui": [2,7,6],
"tmx-standard": [2],
"proport": [1],
"regexp": [0],
"subtitl": [2],
"sentencecasemenuitem": [0],
"gotohistorybackmenuitem": [0],
"save": [4,2,7,1,[0,6],[3,5,8]],
"v1.0": [2],
"articl": [0],
"relaunch": [6],
"goto": [[0,8]],
"editorcontextmenu": [0],
"top": [5,1,7,[2,3]],
"too": [2,1],
"have": [2,0,7,3,1,4,6,5],
"powerpc": [2],
"mandatori": [0],
"optionssentsegmenuitem": [0],
"slowli": [2],
"avail": [7,2,[0,1],4,5,3],
"product": [0],
"robust": [[2,3]],
"question": [0],
"bought": [0],
"hyphen": [0],
"optionsaccessconfigdirmenuitem": [0],
"editselectsourcemenuitem": [0],
"charact": [0,7,4,5,1,2,[3,8]],
"framework": [2],
"test.html": [2],
"php": [0],
"xxx": [6],
"instanc": [[0,2],7,[4,5]],
"thousand": [0],
"smalltalk": [7],
"com": [0],
"instal": [2,1,[0,3],4,[5,6,7,8]],
"minor": [6],
"arrow": [7,5,[0,3]],
"almost": [[2,4]],
"cot": [0],
"remot": [2,6,4,[5,7]],
"manner": [3],
"upon": [[3,4],7],
"whenev": [[3,6]],
"lag": [2],
"earlier": [3],
"pseudotranslatetmx": [2],
"whether": [1,[0,5],7,[2,6]],
"unabl": [[1,5]],
"function": [0,4,3,7,[1,2],[5,6]],
"pipe": [0],
"start-up": [2],
"comparison": [7],
"targetlanguagecod": [0],
"undock": [5,1],
"revert": [0],
"tri": [7,[1,2],0],
"editorprevsegmentnottab": [0],
"changeid": [1],
"less": [2],
"absolut": [[0,1]],
"translat": [2,3,7,0,5,4,1,6,8],
"eras": [[0,1]],
"uniqu": [7,[4,5],0],
"welcom": [3],
"bidirect": [4,0],
"université": [1],
"were": [3,4,[2,5]],
"basic": [2,7,0],
"disabl": [7,1,0,[4,5],2],
"footer": [0],
"cqt": [0],
"shorthand": [0],
"respons": [5],
"docs_devel": [2],
"lck": [5],
"twelv": [0],
"tsv": [0],
"extra": [[0,2,4,5]],
"design": [3,2],
"convey": [3],
"command-lin": [2],
"unpack": [2],
"semicolon": [2],
"gnome": [1],
"accord": [5,[1,7]],
"horizont": [0],
"doctor": [0],
"conduct": [7],
"projectnam": [0],
"omegat.project.yyyymmddhhmm.bak": [2],
"appdata": [0],
"gotten": [3],
"configdir": [2],
"prev": [[0,1,2,3,4,5,6,7]],
"csv": [0,2],
"installdist": [2],
"a-z": [0],
"enhanc": [4],
"password": [2,1],
"ambigu": [0],
"caractèr": [2],
"fr-zb": [2],
"let": [7,[0,3,5]],
"state": [2,0,7,[3,6]],
"gotonextxenforcedmenuitem": [0],
"editordeleteprevtoken": [0],
"les": [5],
"press": [7,[3,4],[0,5],1],
"eventu": [[2,3]],
"dock": [2,[3,5]],
"onlin": [7,[0,2,3,4]],
"coffe": [3],
"element": [0,[3,7]],
"caret": [0],
"want": [7,2,0,4,[1,3,6],5],
"night": [2],
"processor": [7,3],
"each": [0,7,[1,2],[4,5],3,6],
"javascript": [7],
"mediawiki": [[4,7],[0,3]],
"input": [4,[1,2]],
"cue": [0],
"toolkit": [2],
"creativ": [0],
"must": [0,2,1,6],
"join.html": [0],
"suppli": [1],
"non-omegat": [1],
"cur": [0],
"filenameon": [1,0],
"cut": [0,5],
"ctrl": [0,4],
"editorinsertlinebreak": [0],
"jumptoentryineditor": [0],
"document": [0,2,7,3,[4,5],[1,6,8]],
"omegat.kaptn": [2],
"misplac": [4],
"multi-cel": [7],
"mainten": [2],
"two": [2,0,7,[1,4],[3,5,6]],
"accident": [1],
"user-defin": [7],
"pop": [0,4],
"page_up": [0],
"found": [7,4,2,0,[1,6],[3,5]],
"usernam": [2],
"glossaryroot": [0],
"scenario": [2],
"encrypt": [0],
"larg": [7,[2,3]],
"attach": [7,1],
"anoth": [2,[0,4,7]],
"freez": [2],
"advantag": [0],
"graphic": [2],
"creation": [4,[1,3]],
"resourc": [[3,7],6,2,0],
"latest": [6],
"pend": [7],
"moodl": [0],
"team": [2,4,[0,6],[1,3,7,8],5],
"xx_yy": [0],
"side-by-sid": [2],
"docx": [[2,7],[0,4]],
"project_stats_match_per_file.txt": [[4,6]],
"diagram": [0],
"txt": [2,0,5],
"googl": [1],
"quit": [4,[0,1]],
"re-ent": [1],
"chart": [0],
"thing": [[2,3]],
"gotoeditorpanelmenuitem": [0],
"fashion": [3],
"definit": [0,1],
"lib": [0],
"viewmarkfontfallbackcheckboxmenuitem": [0],
"tedious": [3],
"had": [3,[0,7]],
"prepar": [[0,2]],
"align": [7,4,[0,2],3,[1,8]],
"adjac": [5],
"endnot": [0],
"insertcharsrlm": [0],
"redistribut": [8],
"sourceforg": [2,0],
"structur": [[6,7],0,2,[3,8]],
"han": [0],
"viewdisplaymodificationinfoselectedradiobuttonmenuitem": [0],
"entir": [0,[2,3,4,5]],
"index.html": [0,2],
"semeru-runtim": [2],
"has": [[2,4],0,[5,7],1,[3,6]],
"keyword": [7,3],
"given": [2,[0,1],6],
"doubl": [0,2],
"actual": [3,[0,2]],
"unlock": [5,3],
"autosav": [5],
"last": [0,4,[1,7],5,2,[3,6]],
"editmultipledefault": [0],
"adapt": [[1,7],3],
"batch": [2],
"mozilla": [[0,2]],
"doubt": [[6,7]],
"editfindinprojectmenuitem": [0],
"develop": [2,[0,7],1],
"reproduc": [[0,7]],
"pro": [1],
"diffrevers": [1],
"weak-direct": [0],
"warn": [7,2,[0,1],3,4,6,5],
"bookmark": [0],
"easiest": [0,2],
"inlin": [7],
"page": [0,[4,7],[2,3],1],
"full": [0,7,[1,2],5],
"plural": [0],
"away": [[1,2,3]],
"becaus": [0,1],
"three-column": [0],
"parenthesi": [0],
"project.gettranslationinfo": [7],
"czt": [0],
"doctorat": [1],
"overview": [2],
"yes": [7],
"duckduckgo.com": [1],
"start": [0,7,3,2,[1,4,5]],
"mymemori": [1],
"yet": [1,[3,4],[2,6,7]],
"stylist": [0],
"generic": [1,[0,2]],
"pair": [0,[1,2],7],
"regex101": [0],
"equal": [[0,2],[1,4,5]],
"colour": [1,4,[0,6,7]],
"chang": [7,[0,1],6,2,[3,4],5,8],
"watson": [1],
"anywher": [[5,6],[0,7]],
"short": [7,3,[1,4]],
"pop-up": [5],
"time": [3,2,0,7,6,4,[1,5]],
"tmxs": [1],
"kanji": [0],
"program": [2,0,[3,7],[1,4,5]],
"three": [0,2,[1,4,6],[3,5,7]],
"cyan": [4],
"put": [2,0,6,[1,3]],
"python3": [0],
"project_save.tmx.yyyymmddhhmm.bak": [2],
"viewmarkglossarymatchescheckboxmenuitem": [0],
"enter": [7,[0,4],3,5,1,2],
"prioriti": [[1,4],[2,3]],
"tran": [0],
"pale": [4],
"applic": [2,0,7,4,3,[1,6],[5,8]],
"bidi": [0,4,7],
"projectteamnewmenuitem": [0],
"gotoprevxenforcedmenuitem": [0],
"iraq": [0],
"dossier": [5],
"preced": [0,7,2],
"right-click": [7,[4,5],[2,6]],
"directorate-gener": [4],
"non-seg": [0],
"brunt": [0],
"memori": [2,3,7,6,5,0,4,1,8],
"autocompletertablelast": [0],
"authent": [2,1,5],
"no-match": [4],
"retransl": [2],
"indefinit": [0],
"quot": [0],
"recogn": [0,7,3,[2,5,6],[1,4]],
"post-process": [[0,7],1,8],
"tabl": [0,1,[4,5,8]],
"engin": [5,[1,4],7],
"log": [0,4],
"four-step": [7],
"smart": [2],
"lot": [0,3],
"doc-license.txt": [0],
"openjdk": [1],
"永住権": [[1,7]],
"consult": [[0,2]],
"theme": [1,7],
"toolscheckissuesmenuitem": [0],
"チューリッヒ": [1],
"pane": [5,[3,7],4,1,[0,6],2,8],
"undesir": [4],
"meant": [6],
"editor": [7,0,5,3,1,4,6,8,2],
"pseudotranslatetyp": [2],
"chain": [0],
"tutori": [0],
"orphan": [7,5],
"cycl": [4,0],
"autocompletertablepageup": [0],
"fetch": [4,1,0],
"www.deepl.com": [1],
"char": [7],
"small": [[0,4]],
"config-fil": [2],
"quick": [7,[0,3],[2,4]],
"tell": [2,[0,3,7]],
"unavail": [0],
"projectclosemenuitem": [0],
"checker": [1,[0,4]],
"viewmarknonuniquesegmentscheckboxmenuitem": [0],
"hit": [7,4],
"shown": [5,7,[0,4],[1,2]],
"major": [0,2],
"titl": [4,[0,7]],
"consider": [2],
"inspir": [7],
"day": [[0,2],4],
"lre": [0,4],
"group": [0,1,[5,7],[2,3]],
"obtain": [7,[1,2,3,5]],
"suppos": [0],
"system-user-nam": [0],
"findinprojectreuselastwindow": [0],
"lrm": [0,4],
"liter": [0,1],
"format": [2,0,4,3,7,[1,5],6,8],
"tree": [6],
"particular": [[2,7]],
"readme.txt": [2,0],
"done": [2,3],
"languagetool": [4,1,[7,8]],
"savour": [3],
"console.println": [7],
"rainbow": [2],
"source.txt": [[0,1]],
"twice": [3],
"files.s": [7],
"autocompleterlistdown": [0],
"histori": [4,[0,1],3],
"exchang": [0],
"auto-sync": [7],
"achiev": [[0,2,7]],
"launcher": [2],
"request": [2],
"procedur": [2],
"pars": [[0,5,7]],
"part": [0,7,[1,4],[3,5],[2,6]],
"currseg": [7],
"their": [0,7,2,3,1,[5,6]],
"generat": [6,2,[0,1,3,4]],
"unexpect": [2],
"right-end": [5],
"point": [[0,2],1,7],
"general": [[0,7],[1,2],5,8,3],
"browser": [1,[4,5]],
"activefilenam": [7],
"easi": [3,2],
"process": [3,[1,2],[0,7],4,8],
"project_files_show_on_load": [0],
"autocompletertrigg": [0],
"instance—a": [5],
"attribut": [0],
"clear": [[4,7]],
"apostroph": [0],
"third": [2,[5,6],0],
"acquiert": [1],
"build": [2,7],
"mean": [0,[1,2,5,7]],
"neither": [0],
"further": [3,[0,6],[5,7]],
"account": [2,[1,4],[6,7]],
"snippet": [7],
"been": [2,4,7,[1,3,6],0],
"stack": [7],
"dhttp.proxyhost": [2],
"japanes": [[1,2],0],
"ident": [7,2,[1,4,6],[0,5]],
"entries.s": [7],
"addit": [0,7,1,[2,3,4]],
"alphabet": [0,[5,7]],
"simplifi": [[1,2]],
"gotonextuntranslatedmenuitem": [0],
"targetlocal": [0],
"systemwid": [2],
"editorprevseg": [0],
"path": [2,0,1,[5,7]],
"trip": [2],
"bind": [7],
"overwritten": [2,3,4,[0,1,7]],
"abbrevi": [0],
"record": [0,6],
"monospac": [1],
"a-za-z0": [0],
"strict": [2],
"you": [2,7,0,3,1,4,6,5,8],
"jump": [[3,5,7],[0,4]],
"reinsert": [3],
"happen": [2],
"www.apertium.org": [1],
"pass": [2,0],
"past": [4,[2,5]],
"impact": [3],
"mainstream": [0],
"percentag": [5,1,6],
"especi": [[2,7]],
"whose": [[2,4]],
"cours": [[4,6,7]],
"project_save.tmx.tmp": [2],
"configur": [2,0,1,7,4,3,5,8],
"nativ": [2,[0,1]],
"helpcontentsmenuitem": [0],
"resnam": [0],
"omegat-org": [2],
"descript": [4,[0,1,7],3],
"remote-project": [2],
"preserv": [0,2,1],
"initialcreationid": [1],
"ignore.txt": [6],
"organ": [0],
"mega": [0],
"projectaccessdictionarymenuitem": [0],
"zurich": [1],
"mirror": [7],
"空白文字": [2],
"sentenc": [0,[3,7],1,[2,4]],
"alongsid": [2],
"optionsworkflowmenuitem": [0],
"consecut": [7,0],
"how": [3,[0,1,2],5,8,7],
"releas": [2,[0,4]],
"term": [7,5,[3,4],1,0,6,8],
"backslash": [0,2],
"files_order.txt": [6],
"mind": [[0,2,7]],
"projectrestartmenuitem": [0],
"editorskipnexttoken": [0],
"trans-unit": [0],
"right": [0,5,7,2,3,[1,4,6]],
"opposit": [0],
"insid": [[0,2],5],
"qigong": [0],
"dictroot": [0],
"stage": [[2,7]],
"keybind": [7],
"maximum": [0,[2,7]],
"under": [2,4,[0,1,5,6,7,8]],
"xhmtl": [0],
"submenus": [[2,7]],
"did": [7],
"represent": [7],
"imper": [7],
"reserv": [2],
"dir": [2],
"down": [0,7,[1,3]],
"hold": [7,3,5],
"linebreak": [0],
"trail": [0],
"subdir": [2],
"later": [2,3,[0,6,7],5],
"legal": [0],
"bracket": [0],
"unrespons": [7],
"viewfilelistmenuitem": [0],
"info": [0,[1,4,5]],
"hyperlink": [5],
"brows": [7,5],
"autocompletertableleft": [0],
"non-break": [7,0,4,[1,3]],
"journey": [0],
"test": [2,0],
"count": [4,1],
"omegat": [2,0,3,7,1,4,6,8,5],
"forward-backward": [7],
"allemand": [1,7],
"deepl": [1],
"take": [0,[2,7],[3,4],1],
"month": [[0,2],3],
"thereof": [0],
"final": [0,3,[2,6]],
"excerpt": [0],
"editorlastseg": [0],
"file-source-encod": [0],
"occasion": [3],
"some": [2,0,1,6,[3,4,5,7]],
"virtual": [7,2],
"blank": [0],
"rather": [0,7,[2,3,4]],
"session": [[2,3,5,7]],
"console-align": [[2,7]],
"dissimul": [5],
"back": [7,[3,5],2,0,4,6],
"mach": [4],
"projectopenrecentmenuitem": [0],
"fr_fr": [3],
"miss": [4,0,3,[2,5]],
"thèse": [1],
"load": [[2,7],1,[0,6]],
"alpha": [2],
"大学院博士課程修了": [1],
"just": [0,3,2,[6,7]],
"human": [1],
"divid": [0,7],
"primarili": [2],
"collabor": [3,2],
"custom": [0,2,1,4,3,7],
"editexportselectionmenuitem": [0],
"length": [0],
"issue_provider_sample.groovi": [7],
"home": [0,2,[1,3,4,5,6,7]],
"disable-location-sav": [2],
"print": [[0,2,7]],
"condit": [2],
"glyph": [4],
"unl": [5],
"although": [0,2,7],
"projectaccesstargetmenuitem": [0],
"interpret": [0],
"editoverwritemachinetranslationmenuitem": [0],
"iana": [0],
"relat": [2,[1,6],[0,3,5]],
"grant": [[2,8]],
"console-stat": [2],
"ingreek": [0],
"lunch": [0],
"f12": [7],
"visibl": [[0,6]],
"convers": [2,1],
"ignor": [0,6,[1,4,5,7],2],
"convert": [2,3,[0,1,4,7]],
"hope": [3],
"elsewher": [4],
"attempt": [2,7,[0,1]],
"soon": [[4,6,7]],
"influenc": [4],
"projectexitmenuitem": [0],
"aligndir": [2,7],
"system-host-nam": [0],
"action": [7,4,2,5,[0,3],1],
"lock": [5,2,[0,3]],
"adoptium": [2],
"text": [0,7,4,[1,5],3,2,6,8],
"latin": [0],
"mymemory.translated.net": [1],
"en-to-fr": [2],
"fear": [2],
"editregisteruntranslatedmenuitem": [0],
"creat": [2,7,0,3,6,4,5,1,8],
"init": [2],
"python": [7],
"misspel": [0],
"made": [3,[0,2],4,[1,5,6,7]],
"block-level": [0],
"manag": [3,4,2,8,[1,6,7]],
"manifest.mf": [2],
"maco": [0,2,4,5,[3,8],1],
"field": [7,5,4,0,[2,3],1],
"tarbal": [6],
"singl": [0,7,3],
"invalid": [2],
"doc": [7,0],
"doe": [0,2,7,3,[1,4],8],
"output-fil": [2],
"notifi": [5,1],
"status": [5,[0,2,3,6],[1,4,8]],
"server": [2,1,6,5],
"don": [3],
"dot": [0,4],
"paramet": [2,0,7,1,6],
"stamp": [[0,6]],
"run-on": [0],
"skip": [0,6],
"overrid": [[0,2]],
"file": [2,0,7,4,6,3,1,5,8],
"known": [[0,4]],
"member": [2,3],
"map": [2,7,6],
"may": [2,0,7,5,[1,3,4,6]],
"within": [0,[5,6],[2,3]],
"forward": [[0,4]],
"could": [[0,2,6,7]],
"trigger": [7,[0,2]],
"menu": [0,4,5,1,[3,7],8,2,6],
"url": [2,1,[3,6],[0,4,7]],
"exercis": [2],
"megabyt": [2],
"system-wid": [0],
"uppercasemenuitem": [0],
"explan": [7,[0,1]],
"viewmarkuntranslatedsegmentscheckboxmenuitem": [0],
"discrep": [4],
"probabl": [2],
"relev": [0,[1,2],[3,4]],
"needs-review-transl": [0],
"tagwip": [7,3],
"return": [7,0,3,5],
"nonsens": [0],
"invoke-item": [0],
"usb": [2],
"use": [2,7,0,3,1,[4,5],6,8],
"usd": [7],
"feel": [[2,3],[1,6]],
"main": [5,7,[1,2],4,[3,8]],
"newlin": [0],
"convent": [3,[0,8]],
"radio": [7],
"omegat.jar": [2,0],
"source-pattern": [2],
"strip": [7,3],
"conveni": [7,[0,2,3]],
"omegat.app": [2,0],
"fine": [0],
"usr": [[0,1,2]],
"find": [0,2,7,3,1,6],
"host": [2,[0,1]],
"logo": [0],
"backward": [4],
"errat": [0],
"credit": [4],
"regardless": [2],
"alter": [7],
"workflow": [3,5,[0,8]],
"utf": [0,6],
"occur": [0],
"autocompletertablepagedown": [0],
"difficult": [2],
"sort": [[0,5],[1,4,7]],
"fill": [6,2,4],
"feed": [0],
"servic": [2,1,5,4],
"forget": [1],
"task": [2,[3,7]],
"cleanup": [7],
"background": [6,[4,5]],
"xliff": [2,0],
"true": [0],
"header": [0,[4,7]],
"nonetheless": [0],
"present": [0,2,7,[1,3,4,5]],
"dsl": [6],
"mid-transl": [7],
"belong": [[0,1]],
"groovi": [7],
"pre-defin": [3],
"multi-paradigm": [7],
"best": [5,[1,3],[2,4,6,7]],
"med": [4],
"transform": [1],
"fundament": [0],
"execut": [2,0,7],
"hour": [[2,3]],
"dtd": [[0,2]],
"repeat": [[1,7],4,[0,2,3]],
"make": [2,0,3,6,7,1,5,[4,8]],
"abov": [[0,2],[1,7],[3,4,5],8],
"sentence-level": [[0,7],3],
"projectcompilemenuitem": [0],
"classnam": [2],
"messageformat": [1,0],
"console-transl": [2],
"stern": [4],
"compound": [1],
"master": [2,1,0],
"optionsautocompletehistorycompletionmenuitem": [0],
"gotonextuniquemenuitem": [0],
"due": [[2,3]],
"conform": [[3,7]],
"underlin": [4,5,1],
"writer": [0],
"wordart": [0],
"merg": [7,3,[0,1]],
"dalloway": [1],
"rubi": [7],
"resource-bundl": [2],
"inform": [5,2,4,[0,1],7,3,6],
"depend": [0,1,[2,4],5,7,3],
"about": [0,[3,7],[2,4,5,6]],
"commit": [2,[0,4]],
"yyyi": [2],
"external_command": [6],
"targetlocalelcid": [0],
"annot": [3],
"project_stats_match.txt": [[4,6]],
"cover": [0,2],
"editorselectal": [0],
"character": [0],
"tab-separ": [0],
"reflect": [[2,3],[4,6]],
"flexibl": [3],
"runner": [7,0],
"immedi": [0,[3,7],[4,5]],
"pointer": [5],
"distinguish": [3],
"condens": [1],
"omegat-default": [2],
"benefit": [7],
"user.languag": [2],
"regex": [0,3],
"highest": [5,1],
"meta": [0],
"declar": [0],
"except": [1,0,2,3],
"boost": [0],
"libreoffic": [3,0],
"autocompleterclos": [0],
"qualiti": [4],
"nevertheless": [3],
"scan": [7],
"global": [7,0,1,4,8,[2,3,5]],
"racin": [5],
"long": [0,1],
"into": [2,7,0,4,[1,5],3,6],
"unless": [2,[0,1,3]],
"defin": [0,1,[2,7],5,4,[3,6]],
"industri": [3],
"free": [2,3,[0,7,8]],
"evolut": [0],
"star": [0],
"though": [1],
"disappar": [2],
"thorough": [0,2],
"everyday": [2],
"viewdisplaysegmentsourcecheckboxmenuitem": [0],
"appear": [1,0,7,5,[2,3,8]],
"editregisteremptymenuitem": [0],
"face": [1],
"ibm": [[1,2]],
"stats-output-fil": [2],
"mismatch": [7],
"french—if": [1],
"progress": [[2,5],1],
"reliabl": [6,[2,8]],
"oper": [2,[0,1,4,7],[3,6]],
"mani": [0,2,3],
"open": [4,7,0,2,5,[1,3],6],
"treat": [0,[5,7],1],
"parsewis": [7],
"project": [2,7,6,3,0,4,1,5,8],
"seven": [0],
"user-cr": [0],
"取得": [[1,7]],
"trustworthi": [2],
"xmx1024m": [2],
"whatev": [2],
"autotext": [1,0],
"sever": [7,[0,4,5],[2,3,6]],
"loop": [7],
"autocomplet": [0,8],
"five": [1],
"enclos": [0,7,[1,3]],
"penalty-xxx": [[2,6]],
"gotonextsegmentmenuitem": [0],
"invert": [1],
"omegat-cod": [2],
"look": [3,[0,1],2,[4,7,8]],
"repres": [0,1,2],
"dropbox": [2],
"abort": [2],
"left-to-right": [0,4],
"idx": [6],
"internet": [1],
"conflict": [2,[0,3]],
"allow": [[0,4,7],1,[2,3],5],
"comma-separ": [0],
"squar": [0,1],
"commerci": [3],
"rule": [7,0,1,4,[2,3],[6,8]],
"proper": [2,[0,1,4],[3,5,7]],
"detect": [4,1,2],
"everi": [2,[0,6],7,4,[1,3]],
"speed": [2],
"printf": [0,1],
"peopl": [[2,4]],
"summari": [2],
"outsid": [0,[3,5,6]],
"autocompleterconfirmandclos": [0],
"how-to": [3,6,[0,4],[2,7],[1,5]],
"common": [2,0,5,[3,7]],
"projectaccesscurrentsourcedocumentmenuitem": [0],
"interest": [0],
"appli": [7,1,2,0,[4,5,6]],
"linux": [0,2,4,5,[1,3,7]],
"linux-install.sh": [2],
"again": [3,[1,2,7]],
"file.txt": [2],
"openxliff": [2],
"uncom": [2],
"writabl": [0,4,[2,6,7],5],
"layout": [5,[1,3],[0,8],4,[2,7]],
"registri": [0],
"popup": [7],
"format-specif": [1],
"ifo": [6],
"popul": [[1,4,6]],
"comment": [0,5,7,[3,4,8]],
"step": [7,[0,2],3,6],
"bash": [[0,2]],
"basi": [0],
"excit": [0],
"tmroot": [0],
"mark": [0,7,1,4,5,[3,6,8]],
"base": [3,[0,7],1,[2,4,5,8]],
"stem": [1,5],
"uncolor": [5],
"registr": [1],
"intermedi": [2],
"disconnect": [2],
"compulsori": [7],
"optionsmtautofetchcheckboxmenuitem": [0],
"xx.docx": [0],
"prefix": [6,1,2],
"whole": [2,[0,1,4,6,7]],
"consist": [0,4,[2,7],[3,5,6]],
"loss": [2],
"大学": [1],
"lost": [2,4,3],
"editorshortcuts.properti": [0],
"insertcharslr": [0],
"grammat": [[4,7]],
"still": [[0,7],[4,5]],
"compress": [[0,6]],
"work": [2,0,[3,7],6,[1,4,8]],
"lose": [2,[3,5]],
"suitabl": [[2,3,5]],
"fail": [[2,3],7],
"itself": [0,2,7,[3,6]],
"among": [3],
"word": [0,7,[4,6],1,[3,5]],
"variat": [0],
"love": [0],
"lingue": [1],
"thumb": [7],
"verifi": [0],
"auto-propag": [[2,7]],
"requir": [2,[0,1],7,3,[4,5],[6,8]],
"across": [0,3],
"tmotherlangroot": [0],
"viewmarknotedsegmentscheckboxmenuitem": [0],
"non-vis": [0],
"event": [[0,7],2],
"simplest": [0,2],
"vcs": [2],
"lingvo": [6],
"gotomatchsourceseg": [0],
"abstract": [8],
"appropri": [2,7,[0,3]],
"developer.ibm.com": [2],
"mrs": [1],
"opinion": [5],
"excel": [0],
"comma": [0],
"runn": [7],
"literari": [0],
"cannot": [2,0,5],
"runt": [0],
"averag": [7],
"stardict": [6],
"first": [0,7,5,[2,4],[1,3],6],
"omegat.l4j.ini": [2],
"span": [0],
"prefer": [4,0,7,[2,5],1,6,3,8],
"quotat": [[0,7]],
"threshold": [1,[2,5]],
"overridden": [2],
"float": [1],
"space": [0,7,[3,5],4,1],
"hard-and-fast": [7],
"ドイツ": [7,1],
"manipul": [[4,5]],
"simpl": [0,1,3,2,[7,8]],
"from": [2,7,0,6,5,4,3,1],
"html": [0,2,[1,3]],
"spell": [[0,1],[4,7]],
"editselectfuzzy3menuitem": [0],
"bottom": [7,1,5,4],
"insertcharsrl": [0],
"templat": [1,0,8,7],
"fals": [0,[1,2]],
"project.projectfil": [7],
"uncondit": [6],
"finit": [1],
"frequenc": [2],
"www.ibm.com": [1],
"jres": [2],
"frequent": [0,7,[2,3,4]],
"interact": [[2,7]],
"outright": [4],
"error": [2,5,4,[0,3,7]],
"egregi": [0],
"platform": [2,0,1],
"network": [2],
"shortcut": [0,[3,4],7,5,[2,8]],
"public": [2,8],
"briefli": [[0,5]],
"track": [[2,5]],
"tmx2sourc": [[0,2,6]],
"toolsalignfilesmenuitem": [0],
"ini": [2],
"overal": [[0,4]],
"spent": [3],
"instead": [0,[1,2,3,4,7]],
"improv": [0,7],
"command": [7,2,1,0,4,5,[3,8]],
"project-specif": [6,7,[1,2]],
"unlik": [[0,1,3]],
"round": [2],
"dhttp.proxyport": [2],
"detach": [7],
"slash": [0],
"tag-fre": [7,3],
"negat": [0],
"onecloud": [2],
"notat": [0,1],
"viewmarkbidicheckboxmenuitem": [0],
"refus": [[2,3]],
"year": [[0,2]],
"subrip": [2],
"branch": [2],
"via": [2],
"describ": [0,2,5,[3,7]],
"score": [1,7,6],
"fileshortpath": [[0,1]],
"permiss": [7],
"poor": [7],
"double-click": [[2,7],[0,4,5]],
"visual": [0,[3,4]],
"absent": [2],
"near": [3],
"approxim": [7],
"日本語": [7],
"instruct": [2,0,[6,7]],
"appendix": [7,0,1,4,2,[3,5,6]],
"illustr": [3],
"raw": [2],
"version": [2,[0,8],[3,4,6,7]],
"unassign": [4],
"folder": [2,7,0,6,4,1,3,5,8],
"stop": [[1,7]],
"handl": [0,[1,2],7],
"detail": [2,4,7,0,1,3,6,5],
"retriev": [2],
"projecteditmenuitem": [0],
"least": [[2,7]],
"manual": [0,[4,7],[2,3],8,6],
"dollar": [0],
"new_word": [7],
"recycl": [2],
"run\'n\'gun": [0],
"aspect": [3],
"appendic": [0,[3,6,8]],
"unbeliev": [0],
"measur": [2],
"nashorn": [7],
"machin": [1,4,5,[0,7,8],[2,3,6]],
"close": [0,7,2,4,5,[1,3]],
"unsung": [0],
"descend": [0],
"abc": [0],
"learn": [0,3],
"last_entry.properti": [6],
"abl": [2,[0,3,4,7]],
"textual": [7,0],
"toolbar.groovi": [7],
"newer": [4],
"uppercas": [0],
"invok": [7],
"iso": [[0,2]],
"eager": [3],
"isn": [[0,2]],
"optionspreferencesmenuitem": [0],
"thorni": [3],
"autocompleternextview": [0],
"specif": [[0,7],2,1,4,3],
"red": [[1,6],[0,7]],
"aggreg": [0],
"act": [6,0],
"soft-return": [0],
"post": [0],
"glossary.txt": [[2,6],[0,4]],
"finish": [7,0],
"dsun.java2d.noddraw": [2],
"placehold": [1,0],
"add": [2,0,7,3,6,5,[1,4]],
"initi": [7,2,[0,6],1],
"multi-word": [0],
"chines": [1],
"ell": [1],
"need": [2,0,3,6,7,1,5],
"equival": [7,1,[0,2],5],
"often": [3,[0,2,7]],
"editorfirstseg": [0],
"x0b": [2],
"gather": [2],
"els": [5],
"respect": [[0,2,6]],
"rfe": [7],
"canada": [2],
"shell": [0],
"pre-configur": [1],
"port": [2],
"altern": [0,7,4,5,1,2,3],
"entry_activ": [7],
"http": [2,1],
"optionsautocompleteshowautomaticallyitem": [0],
"gotoprevxautomenuitem": [0],
"exec": [0],
"trust": [1,7],
"untar": [2],
"interfer": [4],
"lisenc": [0],
"consequ": [[3,7]],
"prevent": [2,[3,4,7],0],
"undo": [[0,4]],
"glitch": [3],
"softwar": [2,0],
"ishan": [0],
"scope": [0,[3,7]],
"pasta": [0],
"untrust": [0],
"projectsinglecompilemenuitem": [0],
"end": [0,7,[1,2,3]],
"lisens": [0],
"footnot": [0],
"modifi": [0,7,2,3,4,1,[5,6,8]],
"espac": [2],
"otherwis": [2,[6,7]],
"myfil": [2],
"particip": [2],
"anyth": [[0,2]],
"label": [[1,4]],
"howev": [7,0,[2,3]],
"env": [0],
"fledg": [2],
"special": [0,[2,6,7]],
"okapi": [2],
"togeth": [3],
"page_down": [0],
"key-bas": [7],
"numer": [0],
"clone": [2],
"fine-tun": [5],
"targetlanguag": [[0,1]],
"directori": [0],
"sensit": [0,7],
"backup": [2,6,1,7],
"copyright": [4],
"properti": [[2,5],0,3,4,6,7,1,8],
"project_nam": [7],
"system-os-nam": [0],
"occurr": [7],
"insertcharspdf": [0],
"editselectfuzzyprevmenuitem": [0],
"number": [0,7,5,1,2,4,6,3],
"identifi": [0,1,3,4,[2,8],7],
"specifi": [2,0,1,7,[3,5,6]],
"heapwis": [7],
"narrow": [3],
"faulti": [2],
"algorithm": [7,4,0],
"shorter": [7],
"invis": [0],
"troubleshoot": [2,[3,6,8]],
"newli": [[6,7]],
"similar": [[0,2],3,1,[5,7],4],
"tar.bz2": [6],
"paragraph-level": [0],
"forth": [[2,5]],
"bundle.properti": [2],
"script": [7,0,2,4,1,8,[3,6]],
"contributors.txt": [0],
"exit": [[2,7],[0,4]],
"system": [2,0,1,[4,7],[3,6],5],
"driver": [1],
"spellcheck": [1,3,6,7,[2,8],[0,4]],
"www.regular-expressions.info": [0],
"characterist": [[0,1,7]],
"issu": [4,[1,2],0,3,[6,8]],
"partial": [2],
"other": [2,0,7,3,1,[5,6],8,4],
"sourcelang": [0],
"against": [[2,3,5]],
"retain": [[0,2]],
"parenthes": [[0,1]],
"login": [1,0,8],
"cell": [7,0],
"local": [2,7,1,0,4,5,6],
"optionsdictionaryfuzzymatchingcheckboxmenuitem": [0],
"resum": [[0,3]],
"remind": [[0,3,4,6]],
"valid": [0,4,1,[2,6]],
"pictur": [3],
"assur": [4],
"interfac": [2,0,[1,4],[3,5]],
"projet": [5,0],
"locat": [2,0,7,4,[1,3],6,5],
"yield": [7],
"share": [2,7,6,[0,3],5],
"sourcelanguag": [1],
"rle": [0,4],
"gzip": [6],
"helpupdatecheckmenuitem": [0],
"duplic": [[0,2,3,7]],
"repo_for_all_omegat_team_project_sourc": [2],
"notic": [7],
"rlm": [0,4],
"notif": [5],
"esc": [5],
"exampl": [0,2,7,1,4,[3,5],[6,8]],
"nostemscor": [1],
"first-third": [2],
"project_chang": [7],
"round-trip": [2],
"screen": [[0,3]],
"correspond": [7,0,[4,5],[1,2,6]],
"c-x": [0],
"console-createpseudotranslatetmx": [2],
"mode": [2,7,5,4],
"etc": [1,[0,2,5,6]],
"fuzzyflag": [1],
"toolsshowstatisticsstandardmenuitem": [0],
"all": [7,2,1,0,4,3,6,5],
"precaut": [2],
"border": [5],
"new": [2,7,3,0,4,1,6,5,8],
"escap": [0,2],
"read": [7,[0,2],1],
"simplic": [2],
"sequenti": [4],
"below": [0,2,5,6,[1,3,4,7]],
"c.t": [0],
"alt": [0,4],
"poisson": [7],
"runway": [0],
"choos": [0,[1,2,7]],
"rememb": [2,[3,4],6,[0,7]],
"half-width": [7],
"real": [[2,5]],
"tool": [2,[1,7],4,[0,6,8],3],
"ll-cc.tmx": [2],
"unit": [0,7],
"alreadi": [2,[6,7],[0,1,3],4],
"therefor": [0,7,2],
"collect": [[0,6]],
"two-lett": [2,[3,7]],
"redo": [[0,4]],
"slot": [4],
"around": [[1,3,5]],
"grunt": [0],
"reload": [4,7,[0,2,3,6]],
"tkit": [2],
"calcul": [[1,5],7],
"and": [0,2,7,3,4,5,1,6,8],
"synchron": [2,[3,5,7],[1,6]],
"predict": [1,[0,3]],
"row": [7,[0,4]],
"ani": [0,2,7,3,6,4,5,[1,8]],
"render": [7],
"magento": [2],
"backs-up": [6],
"ant": [[2,7]],
"korean": [1],
"boundari": [0,3],
"dispar": [2],
"offlin": [2],
"ll_cc.tmx": [2],
"unnecessari": [6,1],
"u00a": [7],
"helplastchangesmenuitem": [0],
"until": [2,[1,3,7]],
"omegat.ex": [2],
"reason": [0,1,[2,4,7]],
"thought": [2],
"shift": [0,4,7],
"sourcetext": [1],
"simultan": [[1,2]],
"java": [2,0,1,7,3],
"exe": [2],
"english": [2,0,1],
"xmxsize": [2],
"mistak": [7,1,[0,3,4]],
"jar": [2],
"api": [[1,7]],
"editselectfuzzy2menuitem": [0],
"project_save.tmx": [2,6,[3,7],4],
"encapsul": [7],
"dictionari": [1,[3,6],5,4,0,7,8,2],
"remain": [7,6,4,2],
"powershel": [[0,2]],
"eye": [0],
"letter": [0,4,[3,7],[1,2]],
"grade": [3],
"editornextseg": [0],
"appl": [0],
"editselectfuzzynextmenuitem": [0],
"recommend": [2,[0,7]],
"gotonextxautomenuitem": [0],
"worth": [[3,7]],
"read.m": [0],
"default": [0,7,1,4,2,5,6,3,8],
"gray": [4,7,3],
"are": [0,7,2,1,4,5,6,3],
"cloud.google.com": [1],
"taken": [1,[4,6],[2,5,7]],
"readme.bak": [2],
"arg": [2],
"came": [6,2],
"where": [0,7,2,5,4,[1,3],6],
"sudo": [2],
"drop-down": [7,0,1],
"timestamp": [[0,2,8]],
"logogram": [0],
"broken": [3],
"vice": [7],
"projectaccessrootmenuitem": [0],
"nest": [0],
"fulli": [6],
"call": [0,[2,7],4,[1,3,5,6]],
"facilit": [3],
"such": [0,2,7,1,3,[5,6],4],
"plugin": [2,0,1,[3,8]],
"autocompletertableup": [0],
"essenti": [2],
"ask": [2,[4,7],1,[0,3,5]],
"principl": [[3,5,8]],
"tabul": [2],
"understood": [7],
"through": [2,7,5,[0,1,3,4]],
"toolsshowstatisticsmatchesperfilemenuitem": [0],
"strength": [7],
"projectcommitsourcefil": [0],
"editinsertsourcemenuitem": [0],
"run": [7,2,0,1,4,8,[3,5]],
"viterbi": [7],
"microsoft": [0,[3,7]],
"reorgan": [0],
"projectnewmenuitem": [0],
"ecmascript": [7],
"worri": [3],
"either": [0,2,[3,6,7],1],
"view": [5,1,0,[3,4,8],7,6],
"lowercas": [0],
"white": [0,4],
"editorshortcuts.mac.properti": [0],
"segment": [7,0,4,5,1,3,6,2,8],
"changes.txt": [[0,2]],
"titlecasemenuitem": [0],
"yourself": [3,2],
"those": [2,[0,3,7],6,[1,5]],
"glossari": [0,5,7,4,6,[1,3],2,8],
"recurs": [7],
"editcreateglossaryentrymenuitem": [0],
"ignored_words.txt": [6],
"might": [2,3,0],
"github.com": [2],
"configuration.properti": [2],
"ital": [[0,3,7]],
"bold": [1,[5,7],0,3],
"autocompleterlistpageup": [0],
"dure": [[2,3,7],6],
"effici": [3],
"longer": [[0,2],[1,5,7]],
"introduc": [7],
"supersed": [7],
"多和田葉子": [7],
"occupi": [7],
"reopen": [2],
"name": [0,2,[1,3],5,7,6],
"physic": [2],
"recreat": [[0,2,3,6]],
"notabl": [[2,5]],
"next": [0,4,7,[2,3],5,1,[6,8]],
"string": [7,0,4,1,2,3],
"import": [[0,2],[3,5,6]],
"hidden": [6,[5,7]],
"reli": [0,2],
"book": [[0,3,6]],
"show": [7,5,1,2,0,[3,6]],
"cautious": [2],
"target-languag": [2],
"non": [0],
"button": [7,3,0,1,4],
"nor": [0],
"comput": [2,[1,3,8]],
"not": [0,2,7,1,3,4,6,5],
"now": [[0,2],3],
"introduct": [[3,8],2],
"trademark": [5],
"factor": [4],
"editortogglecursorlock": [0],
"enabl": [1,[0,5],4,2,[3,6,7]],
"greek": [0],
"green": [7,5,4],
"associ": [0,2,[1,4],3,[5,7]],
"pseudotransl": [2],
"was": [1,[0,2],7,[3,6]],
"subfold": [2,[0,7],6,4],
"greet": [3],
"new_fil": [7],
"selection.txt": [[0,4]],
"way": [0,2,3,[4,7],8],
"target": [0,4,7,1,2,3,6,5,8],
"xhtml": [0],
"what": [0,[2,3,7],1],
"knowledg": [2],
"itoken": [2],
"finder.xml": [[0,6,7]],
"refer": [0,[6,7],2,3,5],
"workfow": [3],
"colon": [0],
"window": [7,0,4,5,2,1,3,8,6],
"call-out": [4],
"config-dir": [2],
"editorskipprevtokenwithselect": [0],
"discard": [7],
"any—wil": [1],
"criteria": [7,[0,3]],
"disable-project-lock": [2],
"omegat.pref": [[0,1,7]],
"when": [7,[0,1],2,5,3,6,4],
"termbas": [0],
"sequenc": [0,3],
"auto-popul": [6,4,1,2,0],
"carriage-return": [0],
"far": [5,2],
"embed": [0,4],
"plan": [1],
"case": [0,4,7,2,1,[3,6]],
"give": [4,[0,2],[1,3,6,7,8]],
"item": [0,4,7,3,[1,5]],
"multipl": [[0,2,5],[1,3,8]],
"violet": [4],
"unfriend": [4],
"matcher": [0],
"lowest": [5],
"explicit": [2],
"targettext": [1],
"studi": [8],
"consid": [0,[1,6],[2,3,4]],
"slide": [0],
"reset": [7,0,1],
"everyth": [[0,2]],
"style": [[2,7]],
"explor": [0],
"suit": [0,[2,3,5]],
"card": [7,0],
"care": [3],
"widget": [5,8],
"orang": [[0,5,7]],
"portion": [0,7],
"guard": [2],
"direct": [0,2,4,[5,6,7,8]],
"pattern": [0,1,7,2],
"compil": [7],
"caus": [[0,4]],
"mechan": [[0,2,4]],
"modern": [2],
"freedom": [8],
"web": [1,7,2,[0,4,5]],
"edittagpaintermenuitem": [0],
"en-us_de_project": [2],
"temporarili": [[1,5]],
"you\'r": [3],
"symlink": [2],
"older": [2],
"protect": [1,3],
"nth": [7],
"editselectfuzzy4menuitem": [0],
"editregisteridenticalmenuitem": [0],
"more": [0,7,3,[1,6],[2,5],4],
"display": [1,7,4,5,0,3,6,2],
"hanja": [0],
"great": [3],
"unicod": [0,4,8],
"viewmarknbspcheckboxmenuitem": [0],
"fanci": [0],
"usag": [7,2],
"computer-assist": [[3,8]],
"left-hand": [7],
"certain": [2,5,[0,4]],
"advanc": [7,0,[1,4]],
"shut": [7],
"overwrit": [5,[4,6],[0,2]],
"path-to-omegat-project-fil": [2],
"fed": [2],
"whitespac": [0,[2,4]],
"credenti": [1,2,[5,8]],
"section": [3,2,0,7,1,[4,5]],
"auto-complet": [1,0,[3,5],4,8],
"simpli": [0,7,[2,3],6],
"cloud": [2],
"protocol": [2,1],
"msgstr": [0],
"few": [0,[3,4],2,[7,8]],
"dict": [1],
"untransl": [0,7,4,[1,5],[2,3],6],
"nationalité": [1],
"kind": [0,1],
"daili": [0],
"resiz": [5],
"both": [7,0,2,1,5],
"most": [0,2,4,[5,7],[1,6,8]],
"delimit": [1,5,[4,7],0],
"phrase": [0,7,3],
"omegat.project": [2,6,3,[1,5,7]],
"button-bas": [4],
"marker": [[0,1,5],2],
"keep": [7,2,0,6,[1,3,5]],
"effect": [0,7,1],
"whi": [0],
"topic": [[0,2]],
"excludedfold": [2],
"targetcountrycod": [0],
"job": [3,2],
"fallback": [[0,4]],
"option": [2,0,7,1,4,3,6,[5,8]],
"who": [1,7,[2,4]],
"overtyp": [0],
"continu": [3,[0,2,7]],
"insert": [0,4,1,6,5,2,7,3],
"remark": [3],
"everyon": [2],
"resid": [2],
"highlight": [7,4,5,0,[1,6]],
"along": [0,7],
"reject": [1],
"arrang": [1],
"sheet": [0],
"messag": [5,[0,2]],
"rest": [0,[2,3]],
"move": [4,7,5,3,[0,1]],
"amount": [2],
"also": [2,7,0,[1,3],6,5,4],
"enough": [2],
"differ": [1,2,7,0,[3,4,5]],
"conson": [0],
"situat": [[0,2]],
"consol": [2],
"mous": [7,[3,4,5]],
"various": [3,0,[4,7],[2,5],1,6],
"archiv": [6],
"front": [2],
"visit": [4,6],
"user": [[0,2],1,[3,4,8],[5,7]],
"itokenizertarget": [2],
"viewmarkwhitespacecheckboxmenuitem": [0],
"proxi": [2,1,8],
"extens": [0,2,6,[1,4,5,7,8]],
"back_spac": [0],
"potenti": [0,[1,4,6]],
"asterisk": [0],
"bring": [7,[0,2,5]],
"tooltip": [1,5],
"complet": [0,1,2,7],
"recalcul": [7],
"avoir": [0],
"bak": [2,6],
"canon": [0],
"offer": [2,[0,3],5,7],
"robot": [0],
"fit": [[3,5]],
"bar": [5,0,[3,8]],
"claus": [0],
"fix": [2,[0,4,6]],
"built-in": [1],
"complex": [0,7,2],
"jre": [2],
"rang": [0,2],
"despit": [7],
"posit": [0,4,[5,7],[1,8]],
"eclips": [2],
"sure": [2,1,7,[0,3,5]],
"ad": [[0,7],2,1,[3,4,6]],
"reus": [2,[3,4],[0,1,7]],
"diff": [1],
"automat": [7,2,1,[4,6],3,0,5],
"an": [0,2,[3,7],1,4,5,6],
"secur": [1,7,[0,8]],
"editmultiplealtern": [0],
"panic": [3],
"extend": [[2,7]],
"as": [0,2,7,1,4,6,5,3,8],
"day-to-day": [0],
"at": [7,[0,1],[2,5],3,4,6],
"predefin": [1,[0,2]],
"constitut": [0,[2,6]],
"hierarchi": [6,2],
"ordinarili": [0],
"drive": [2],
"file-shar": [2],
"alllemand": [7],
"non-gui": [2],
"deal": [[0,7]],
"strong": [0],
"be": [0,2,7,1,4,6,3,5],
"affect": [2],
"icon": [4,5],
"filters.xml": [0,[1,2,6,7]],
"delet": [2,0,[1,3,4],[6,7]],
"proven": [0],
"version-control": [2],
"bcp": [[3,7]],
"br": [0],
"projectaccessglossarymenuitem": [0],
"javadoc": [7],
"see": [2,[4,7],0,3,1,6,5],
"search": [7,0,1,4,3,5,8,[2,6]],
"by": [0,7,2,1,6,3,4,5,8],
"segmentation.conf": [[0,2,6,7]],
"panel": [7,1],
"ca": [2],
"cc": [2],
"ce": [2],
"set": [1,[2,7],0,6,[3,4],5,8],
"contain": [0,6,[2,7],5,3,1,4],
"incorrect": [[6,7]],
"balis": [5],
"fastest": [8],
"column": [7,[0,4],1],
"freeli": [[0,5]],
"figur": [5,[3,8]],
"cs": [0],
"renam": [2,3,0],
"instantan": [2],
"partner": [2],
"project.sav": [2],
"somewhat": [3],
"apach": [2,7],
"config": [2],
"adjustedscor": [1],
"font": [1,4,5,[0,3]],
"dd": [2],
"de": [[1,5]],
"featur": [1,7,[2,3,5]],
"offic": [[0,2],[3,7]],
"terminolog": [0,4],
"repositories.properti": [[0,2]],
"extern": [7,1,4,0,[3,5],[2,6,8]],
"forc": [0,[1,4,7]],
"do": [0,2,[1,7],[3,4],5,6],
"f1": [[0,4,7]],
"f2": [[3,5],[0,7]],
"f3": [[0,4],5],
"parti": [[2,5]],
"f5": [[0,3,4]],
"dz": [6],
"projectsavemenuitem": [0],
"editundomenuitem": [0],
"won": [2],
"rare": [2],
"contact": [5],
"ja-rv": [2],
"xmx6g": [2],
"autocompletertablefirstinrow": [0],
"digit": [0,[2,7]],
"which": [0,[2,7],4,[3,5],1],
"signific": [0],
"belazar": [1],
"en": [0,1],
"carri": [[4,7]],
"eu": [4],
"never": [0,[3,4,6]],
"tmautoroot": [0],
"aggress": [[0,4]],
"adjust": [7,[2,6]],
"activ": [4,1,0,7,[5,6]],
"first-class": [7],
"compat": [0,2,[1,7]],
"compar": [7,3],
"cursor": [5,4,3,0,7],
"prototype-bas": [7],
"indic": [5,[0,4],2,1],
"insertcharslrm": [0],
"origin": [2,0,7,3,1,5],
"for": [0,2,7,1,4,3,6,5,8],
"exclud": [2,7,0],
"fr": [2,[1,3]],
"content": [0,2,7,3,1,6,4,[5,8]],
"duckduckgo": [1],
"hover": [[1,4,5]],
"desktop": [2],
"decor": [3,[0,7]],
"applescript": [2],
"skill": [2],
"client": [2,[0,6]],
"exclus": [7,2],
"json": [2],
"gb": [2],
"class": [0,8],
"helplogmenuitem": [0],
"over": [4,2,[1,5,6]],
"six": [3],
"someth": [[1,2]],
"easy-to-us": [3],
"editoverwritetranslationmenuitem": [0],
"bound": [7],
"go": [0,5,3,4,2,6,[1,7,8]],
"counter": [7],
"kept": [6,[0,7]],
"aeiou": [0],
"form": [0,7],
"publish": [2],
"setup": [2],
"restor": [[1,2,5],[4,6],[0,7]],
"avoid": [[0,2],[3,7]],
"foundat": [2],
"targetroot": [0],
"prompt": [2],
"subset": [[0,2]],
"assign": [0,[4,7],[2,3,6],[1,5]],
"typograph": [[4,7]],
"hh": [2],
"select": [4,7,0,5,1,2,3,6],
"duser.languag": [2],
"viewmarkparagraphstartcheckboxmenuitem": [0],
"bin": [0,[1,2]],
"canadian": [1],
"degre": [0],
"easili": [3,6],
"apertium": [1],
"bit": [4],
"bis": [0],
"kaptain": [2],
"meta-inf": [2],
"clipboard": [4],
"repetit": [[4,5,7],0],
"output": [2,1,8],
"veri": [3,7,[0,1,4],2],
"file-target-encod": [0],
"projectopenmenuitem": [0],
"autom": [2,[1,3,7]],
"corner": [5],
"four": [0,[2,4]],
"decim": [0],
"mainmenushortcuts.mac.properti": [0],
"context": [1,5,4,[0,2,3,6]],
"ordinari": [0],
"model": [[1,7]],
"id": [1,0,7],
"https": [2,1,0,[5,6]],
"drag": [5,2],
"join": [3],
"decis": [6],
"if": [2,7,4,0,1,3,5,6],
"french": [2,1,7],
"project_stats.txt": [6,4],
"non-ascii": [0],
"ocr": [7],
"oct": [1],
"projectaccesscurrenttargetdocumentmenuitem": [0],
"in": [0,7,2,4,1,5,3,6,8],
"lower": [0,4,[5,6,7]],
"termin": [2],
"ip": [2],
"index": [0],
"is": [0,2,7,5,[1,4],6,3,8],
"it": [0,2,7,6,5,3,1,4,8],
"vertic": [0],
"whitelist": [2],
"decid": [3,[0,2]],
"projectaccesstmmenuitem": [0],
"odf": [0],
"smoother": [[3,7]],
"contrast": [0],
"ja": [[1,2]],
"becam": [2],
"begin": [0,3],
"odt": [[0,7]],
"gotonexttranslatedmenuitem": [0],
"viewmarktranslatedsegmentscheckboxmenuitem": [0],
"paragraph": [0,7,5,[1,4],3],
"charset": [0],
"viewer": [5],
"valu": [0,7,[1,2],4],
"librari": [0],
"standalon": [1],
"ilia": [2],
"toolscheckissuescurrentfilemenuitem": [0],
"libraries.txt": [0],
"learned_words.txt": [6],
"language—th": [0],
"world": [2],
"meantim": [2],
"uxxxx": [0],
"ftl": [[0,2]],
"reappli": [4],
"side": [[2,5],[0,7]],
"break": [0,1,[3,7]],
"editselectfuzzy1menuitem": [0],
"themselv": [0,2,1],
"upgrad": [2,1,7],
"viewdisplaymodificationinfoallradiobuttonmenuitem": [0],
"tabular": [1],
"draw": [0],
"characters—known": [0],
"non-default": [2],
"comfort": [[2,7]],
"off": [[3,4,7]],
"hide": [7,5,0,[1,3,4]],
"extran": [[0,1,7]],
"la": [1],
"report": [2,[0,3,4,7]],
"li": [0],
"autocompleterlistpagedown": [0],
"ll": [2],
"auto": [4,0,6,2,[1,7]],
"receiv": [[1,5]],
"sign": [0,5],
"notepad": [5,3,4,8],
"document.xx.docx": [0],
"lu": [0],
"editorskipnexttokenwithselect": [0],
"while": [[0,4],[3,5],7,[1,2]],
"second": [[1,4],[0,2,5,7]],
"that": [0,2,7,1,3,6,4,5,8],
"cycleswitchcasemenuitem": [0],
"download": [2,1,[0,3],[4,6,7]],
"split": [7,0,3,[1,4,5]],
"mb": [2],
"oracl": [0],
"editortoggleovertyp": [0],
"than": [1,0,[2,6,7],3,[4,5]],
"limit": [0,2,[3,6]],
"me": [[2,5]],
"non-translat": [0],
"picker": [2],
"mm": [2],
"administr": [2],
"gradlew": [2],
"entri": [7,0,4,5,1,3,[2,6]],
"level": [7,3],
"ms": [0],
"author": [7,[3,4]],
"toggl": [[0,5],7],
"mt": [6],
"modif": [0,1,4,[2,5],[3,7],6],
"my": [[0,2]],
"cascad": [1],
"plus": [0],
"expand": [0],
"disk": [7,2],
"legaci": [0],
"viewmarklanguagecheckercheckboxmenuitem": [0],
"unseg": [3],
"updat": [[1,2],6,7,[0,3,4,8]],
"ubiquit": [4],
"produc": [2,0,[5,7]],
"three-lett": [[3,7]],
"licenss": [0],
"no": [0,7,2,1,4,3,[5,6]],
"code": [0,2,7,3,1],
"bridg": [2,[0,6]],
"underscor": [0],
"gotohistoryforwardmenuitem": [0],
"box": [7,0,1],
"switch": [7,0,4,[1,3]],
"head": [0],
"dialog": [[1,7],3,[0,4],2,[6,8]],
"project_save.tmx.timestamp.bak": [6],
"total": [5,[4,7]],
"immut": [6],
"of": [0,7,2,1,3,5,4,6,8],
"bundl": [[1,2],0],
"possibl": [2,0,1,[5,8]],
"involv": [[2,7],6],
"ok": [7,4,3],
"dynam": [7],
"hear": [0],
"on": [2,0,7,1,5,[3,4],6,8],
"keyboard": [0,[4,5]],
"macro": [7],
"purpos": [0,[2,8],[1,7]],
"technic": [0,2],
"or": [0,7,2,4,3,5,1,6],
"os": [0,[4,5]],
"src": [2],
"gigabyt": [2],
"control": [0,4,2,3],
"encod": [0,[6,7,8]],
"no-team": [2],
"editinserttranslationmenuitem": [0],
"extrem": [0,[2,3]],
"lissens": [0],
"fileextens": [0],
"offici": [[0,8]],
"easier": [2,[0,3],4],
"compliant": [2],
"pm": [[1,5]],
"po": [2,0,1,[5,6]],
"closest": [1],
"upper": [4,0],
"ssh": [2],
"environ": [2,0],
"qa": [7,4],
"autocompletertablefirst": [0],
"specialti": [2],
"necessari": [2,[0,1,3,7]],
"vari": [5,[0,2]],
"friend": [0],
"concurr": [4],
"recent": [[2,4],[0,5,7]],
"they": [0,7,2,[3,5],[1,6],4],
"pinpoint": [7,2],
"streamlin": [3],
"github": [2],
"edit": [7,5,0,3,2,4,1,8],
"old": [2,[1,7]],
"subtract": [6],
"editselectfuzzy5menuitem": [0],
"them": [0,3,2,7,4,[5,6],1],
"bilingu": [[6,7],2],
"then": [2,3,0,[5,7],[1,4,6]],
"kde": [2],
"accept": [7,[2,6],1],
"third-parti": [2,3],
"rc": [2],
"includ": [2,0,7,6,[1,3,5],4],
"readili": [2],
"adopt": [0],
"t0": [3],
"t1": [3],
"t2": [3],
"t3": [3],
"minut": [2,[1,3,4,6]],
"access": [0,7,[2,4],3,1,6,8],
"currenc": [[0,7]],
"languag": [2,7,1,0,3,6,[4,5]],
"seen": [0],
"sa": [1],
"seem": [2],
"sc": [0],
"exept": [2],
"current": [4,7,0,[2,5],[1,6],3],
"sl": [2],
"optionsglossaryfuzzymatchingcheckboxmenuitem": [0],
"so": [2,0,[3,7],5,[4,6,8]],
"caution": [[0,2,7]],
"email": [0],
"key": [0,7,[1,5],4,[2,3],8],
"apart": [7],
"communic": [5],
"intern": [2,[0,1,4,5]],
"starter": [0],
"onc": [2,7,3,4,[0,1]],
"one": [0,7,4,2,[3,5],1,6],
"anymor": [2],
"msgid": [0],
"launch": [2,7,1,0,4,3],
"svn": [2,7,6],
"store": [0,2,7,[1,3],[5,6],4,8],
"interv": [1,2,[4,6]],
"omegat-license.txt": [0],
"editoverwritesourcemenuitem": [0],
"stori": [0],
"closer": [1],
"confirm": [[0,1,4,6,7],2],
"omegat.autotext": [0],
"emerg": [2],
"kilobyt": [2],
"enforc": [6,4,[0,2],[1,3]],
"problemat": [3],
"th": [4],
"bug": [[0,4]],
"remov": [7,0,2,6,1,4],
"tl": [2],
"tm": [6,2,4,[0,1],7,[5,8],3],
"assist": [[3,5]],
"to": [2,0,7,3,1,4,5,6,8],
"v2": [2,1],
"typic": [[0,2]],
"editreplaceinprojectmenuitem": [0],
"but": [0,2,7,3,[1,6],4,5],
"document.xx": [0],
"symbol": [0,5,[2,7]],
"editordeletenexttoken": [0],
"express": [0,7,1,8,2,3],
"multilingu": [0],
"viewmarkautopopulatedcheckboxmenuitem": [0],
"zero": [0,7],
"projectwikiimportmenuitem": [0],
"deactiv": [4],
"countri": [2,1],
"variant": [[0,2]],
"subsequ": [[0,1]],
"un": [2],
"up": [0,2,7,[3,6],1,[5,8]],
"written": [[0,2,4,7]],
"us": [0],
"gotoprevioussegmentmenuitem": [0],
"partway": [[2,7]],
"newword": [7],
"usual": [[2,4]],
"this": [7,0,2,[1,4],6,5,3,8],
"gotopreviousnotemenuitem": [0],
"stderr": [0],
"editredomenuitem": [0],
"uilayout.xml": [[0,6]],
"verif": [1],
"substitut": [4],
"opt": [2,0],
"extract": [7,[0,1,6]],
"sourceroot": [0],
"hint": [3],
"know": [0],
"projectʼ": [3],
"region": [0,[1,2]],
"support": [2,7,[0,3],6,1,[4,8]],
"vs": [1],
"sinc": [[0,2,3],7],
"higher": [1,0],
"changed": [1],
"drop": [5,[2,6]],
"idea": [[2,7]],
"we": [0,[2,3]],
"unchang": [3],
"rearrang": [[4,5]],
"wavy-lin": [1],
"autocompleterlistup": [0],
"licenc": [0],
"omegat.project.bak": [2,6],
"repo_for_omegat_team_project": [2],
"choic": [7,[0,2]],
"normal": [[0,7],2,[1,6]],
"gradual": [[0,2,6]],
"slight": [[0,5]],
"corrupt": [2],
"previous": [0,4,3,5,2,[6,7]],
"projectaccessexporttmmenuitem": [0],
"wide": [2],
"licens": [2,0,8,4],
"org": [2],
"distribut": [7,2,0,8],
"behav": [2,0],
"daunt": [0],
"punctuat": [0],
"example.email.org": [0],
"xx": [0],
"runtim": [2,0],
"sourc": [0,7,2,4,1,5,6,3,8],
"individu": [0,7,[2,4]],
"reach": [3,2],
"realiz": [3],
"none": [1,7,[0,4,6]],
"ressourc": [8],
"type": [2,7,0,[1,5],3,6,4],
"beyond": [[0,3]],
"problem": [[2,5],4],
"review": [3,[2,7],[0,5,6,8]],
"filenam": [0,7,[1,2,5]],
"optionsautocompletehistorypredictionmenuitem": [0],
"projectaccesssourcemenuitem": [0],
"roam": [0],
"between": [[2,5],0,[1,7],4,3],
"yy": [0],
"nbsp": [7],
"method": [2,[5,7]],
"contract": [0],
"gotosegmentmenuitem": [0],
"scroll": [[1,3,5]],
"come": [[0,2],3,6,7,5],
"push": [2],
"exist": [2,[3,7],[4,5],0,[1,6]],
"readme_tr.txt": [2],
"penalti": [6,1],
"exact": [7,0,4,[2,3,6]],
"regist": [2,1,[0,4,5],[3,7]],
"initialcreationd": [1],
"references—in": [0],
"flag": [1,4,0,[3,5]],
"spacebar": [0],
"utf8": [0,[4,7]],
"helpaboutmenuitem": [0],
"copi": [2,7,[1,4,5],0,6,[3,8]],
"out": [7,[0,4],[2,3,5]],
"weak": [0,7],
"get": [2,[0,3,7]],
"dark": [1],
"statist": [4,6,7,1,[0,2]],
"power": [0,7],
"place": [0,[2,7],[4,6],1],
"packag": [2,4],
"accur": [[6,7]],
"leav": [1,[3,4],5,[0,6],2],
"regular": [0,7,2,1,[6,8],[3,5]],
"context_menu": [0],
"editsearchdictionarymenuitem": [0],
"restart": [2,0,4,1],
"tag-valid": [2],
"ovr": [5],
"suggest": [5,1,0],
"alway": [[0,2],[1,6],4,[3,7]],
"lead": [0],
"token": [0,[1,2,7],[5,6]],
"filter": [0,[2,7],1,4,3,8,6],
"readabl": [1],
"expect": [0,2,[1,8]],
"help": [0,[2,3,4,8],7],
"site": [0,2,1],
"projectroot": [0],
"right-to-left": [0,4],
"omegat.log": [0],
"behaviour": [2,4],
"carriag": [0],
"revis": [3],
"repositori": [2,6,[4,5,7],[0,1,8]],
"minimum": [6,[0,1]],
"autocompletertableright": [0],
"date": [1,[0,3,7],6],
"magic": [0],
"argument": [0],
"data": [2,1,4,7,6,3],
"lowercasemenuitem": [0],
"own": [2,0,7,5],
"wiki": [[2,6]],
"autocompleterconfirmwithoutclos": [0],
"separ": [0,[2,7],1,5,3,[4,6]],
"tab": [0,[4,5],1],
"filepath": [1,0],
"plain": [[0,7]],
"should": [0,[1,2],7,[3,4,6]],
"tag": [1,0,3,4,7,2,5,8],
"replac": [7,0,4,1,2,3,5,[6,8]],
"versa": [7],
"tap": [3],
"like": [2,[0,3,7],6,5],
"maxim": [5],
"onli": [0,7,[1,2],4,3,5],
"brace": [0],
"sent": [1],
"projectreloadmenuitem": [0],
"core": [6],
"person": [7,2],
"safe": [2],
"navig": [3,[4,5,6],2],
"send": [2,1],
"here": [1,[0,5],2,7,6,[3,4]],
"note": [0,2,4,7,5,1,3,6],
"cross-platform": [2],
"line": [0,2,7,[1,5],6],
"noth": [[3,4]],
"link": [[0,3],1,[5,6]],
"hero": [0],
"provis": [2],
"becom": [[3,6]],
"tbx": [0,1],
"wildcard": [2],
"can": [2,0,7,3,1,5,4,6,8],
"everybodi": [2],
"contributor": [[0,7]],
"git": [2,6],
"satisfi": [[2,7]],
"cat": [[0,2,3,7]],
"duser.countri": [2],
"provid": [2,0,1,7,5,3,4],
"realli": [4],
"xx-yy": [0],
"smooth": [3],
"reboot": [2],
"readm": [0],
"will": [0,2,1,7,6,3,5,4],
"readi": [2],
"self-host": [2],
"match": [0,1,4,7,6,5,3,2,8],
"follow": [0,2,7,[3,5],1,[4,6,8]],
"categori": [0,8],
"intent": [0],
"fragment": [[0,3]],
"targetlang": [0],
"align.tmx": [2],
"file2": [2],
"arbitrari": [2],
"optionssetupfilefiltersmenuitem": [0],
"wild": [7,0],
"intend": [0,[2,6]]
};
