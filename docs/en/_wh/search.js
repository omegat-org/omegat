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
 "appendices.html",
 "chapter.instant.start.guide.html",
 "dialogs.preferences.html",
 "how.to.html",
 "index.html",
 "menus.html",
 "panes.html",
 "project.folder.html",
 "windows.and.dialogs.html"
];
wh.search_titleList = [
 "Appendices",
 "Introduction to OmegaT",
 "Preferences",
 "How To...",
 "OmegaT 5.8.0 - User Guide",
 "Menus",
 "Panes",
 "Project Folder",
 "Windows and Dialogs"
];
wh.search_wordMap= {
"cancel": [0,[5,8]],
"altgraph": [0],
"stats-typ": [3],
"half": [2],
"upload": [3,5],
"don\'t": [7],
"your": [3,1,0,8,2,5,6,4,7],
"elimin": [3],
"without": [3,8,2,0,7,6],
"these": [[0,7],3,[1,8]],
"would": [[0,7],[3,8]],
"xml": [[0,3],2],
"ten": [8,[3,5]],
"sake": [3],
"beginn": [1],
"info.plist": [3],
"i.e": [0],
"sometim": [0,3],
"thus": [[1,2]],
"noun": [6],
"scratch": [3],
"click": [8,[0,2],5,[3,6],1],
"insensit": [0,8],
"fuzzi": [2,5,6,8,[0,3],7,[1,4]],
"xdg-open": [[2,8]],
"befor": [3,0,2,8,5,[1,7],6],
"size": [2,8,3],
"util": [3],
"left": [0,6,[3,5],8],
"seri": [0],
"tar.bz": [7],
"much": [[0,2,3]],
"object": [8,[0,3]],
"chapter": [3],
"ahead": [0],
"yellow": [5],
"turn": [0,[3,5,7,8]],
"suffici": [[0,3]],
"result": [8,3,0,5,[2,4,6],1],
"edittagnextmissedmenuitem": [0],
"same": [8,3,0,2,6,1,[5,7]],
"editorskipprevtoken": [0],
"checkbox": [8],
"after": [0,3,[2,8],1,6,5,7],
"quiet": [3],
"flip": [0],
"connect": [[2,3],6],
"hand": [[1,8]],
"address": [0,3,[6,7]],
"union": [3],
"statmt.org": [2],
"gnu": [3],
"the": [0,3,8,2,5,6,7,1,4],
"straight": [0],
"wipe": [1],
"blue": [8,6],
"projectimportmenuitem": [0],
"imag": [0],
"suzum": [2],
"monolingu": [0,8],
"target.txt": [[0,2]],
"goe": [8],
"demonstr": [0],
"temurin": [3],
"standard": [1,[2,3],[0,8],[5,6]],
"d\'espac": [3],
"correct": [8,3,[2,6],[0,4,5,7]],
"traduct": [6],
"project-bas": [8],
"advic": [3],
"good": [[1,3],[2,4]],
"wish": [8,[3,7]],
"nameon": [0],
"moodlephp": [3],
"currsegment.getsrctext": [8],
"implement": [2],
"alphanumer": [0],
"uncheck": [8,0,2],
"export": [0,[5,8],[3,7],[1,2,4]],
"gotonextnotemenuitem": [0],
"area": [6,8],
"practic": [8,[0,3,7]],
"gpl": [0],
"european": [3],
"newentri": [8],
"reduc": [8],
"check": [[2,5,8],0,3,1,[6,7]],
"list": [0,[2,3],8,5,4,[1,6],7],
"onto": [3],
"autocompleterprevview": [0],
"resolut": [1],
"vowel": [0],
"fr-fr": [2],
"ensur": [3,[0,1,8]],
"minim": [[2,3,5,6]],
"locali": [3],
"projectcommittargetfil": [0],
"pear": [0],
"determin": [[2,8],0],
"root": [3],
"combin": [0,[2,8],[3,7]],
"po4a": [3],
"japonai": [8],
"omegat.org": [3],
"menus": [0,8,[5,6],[1,3,4]],
"hard": [3],
"realign": [8],
"object-ori": [8],
"cjk": [8,0],
"perform": [3,8,1,[0,2]],
"prewritten": [7],
"alternatives—th": [6],
"maxprogram": [3],
"better": [8,0,1],
"with": [0,3,8,2,5,[1,6],7,4],
"pdf": [3,5,[0,8]],
"there": [3,8,0,5,[1,6],[2,7]],
"syntax": [0,3,[2,4,8]],
"well": [0,3,8,1,[2,5]],
"empti": [3,7,[0,5],[2,6],[1,8]],
"autocompletertabledown": [0],
"editornextsegmentnottab": [0],
"toolsshowstatisticsmatchesmenuitem": [0],
"channel": [0],
"focus": [1,[0,3]],
"viewdisplaymodificationinfononeradiobuttonmenuitem": [0],
"desir": [3,8,2,0],
"approach": [3],
"variabl": [2,0,8,4],
"block": [8,0,5,[1,2,4]],
"tms": [3,4,7,[0,5,8],1],
"per": [7,[0,2,5,6,8]],
"write": [[3,8],6,[0,2,5]],
"tmx": [3,8,7,[1,2,5,6]],
"propos": [8],
"e.g": [2,[0,3,8]],
"order": [2,8,[0,6],[5,7]],
"project_save.tmx.bak": [[3,7]],
"cli": [3],
"repo_for_all_omegat_team_project": [3],
"period": [0,2,3],
"colleagu": [6],
"application_startup": [8],
"proceed": [3,8],
"eventtyp": [8],
"byte": [3],
"understand": [[0,3]],
"integ": [2],
"fr-ca": [2],
"mainmenushortcuts.properti": [0],
"ever": [[1,2]],
"projectaccesswriteableglossarymenuitem": [0],
"even": [[3,8],0,2,[1,5],[6,7]],
"aris": [3],
"application_shutdown": [8],
"autocompletertablelastinrow": [0],
"gui": [3,8,7],
"proport": [2],
"regexp": [0],
"subtitl": [3],
"sentencecasemenuitem": [0],
"gotohistorybackmenuitem": [0],
"save": [5,8,3,2,0,7,[1,4,6]],
"v1.0": [3],
"articl": [0],
"relaunch": [7],
"goto": [[0,4]],
"editorcontextmenu": [0],
"top": [2,[6,8],[1,3,5]],
"too": [3,2],
"have": [3,0,8,1,5,2,7,6],
"powerpc": [3],
"mandatori": [0],
"optionssentsegmenuitem": [0],
"slowli": [3],
"avail": [8,3,[0,2],5,6,1],
"product": [[0,3]],
"robust": [[1,3]],
"question": [0],
"bought": [0],
"hyphen": [0],
"optionsaccessconfigdirmenuitem": [0],
"charact": [0,8,5,6,2,3,[1,4]],
"framework": [3],
"test.html": [3],
"php": [0],
"xxx": [7],
"instanc": [0,3,8,5],
"thousand": [0],
"smalltalk": [8],
"com": [0],
"instal": [3,2,0,[1,5],[4,6]],
"minor": [7],
"arrow": [8,[0,6]],
"almost": [5],
"profject": [7],
"cot": [0],
"remot": [3,7,[5,6,8]],
"manner": [1],
"upon": [[0,1,6,8]],
"whenev": [[1,3,7]],
"lag": [3],
"earlier": [0],
"pseudotranslatetmx": [3],
"whether": [2,[0,8],[3,6,7]],
"unabl": [[2,6]],
"function": [0,5,2,[3,8],1,[6,7],4],
"pipe": [0],
"start-up": [3],
"comparison": [8],
"targetlanguagecod": [0],
"platform-specif": [2],
"undock": [6,2],
"revert": [0],
"tri": [8,[2,3],0],
"editorprevsegmentnottab": [0],
"changeid": [2],
"less": [3],
"absolut": [2],
"translat": [3,0,8,1,6,5,2,7,4],
"eras": [[0,2]],
"uniqu": [8,[5,6],[0,2]],
"welcom": [1],
"bidirect": [[3,5],0],
"were": [[1,3],6],
"université": [2],
"basic": [3,8,0],
"disabl": [8,2,[0,5],3],
"footer": [0],
"cqt": [0],
"shorthand": [0],
"respons": [6],
"docs_devel": [3],
"lck": [6],
"twelv": [0],
"tsv": [0],
"extra": [[0,3,5,6]],
"design": [1],
"command-lin": [3],
"unpack": [3],
"semicolon": [3],
"encourag": [3],
"accord": [[2,8]],
"horizont": [0],
"doctor": [0],
"conduct": [8],
"mqxliff": [3],
"omegat.project.yyyymmddhhmm.bak": [3],
"appdata": [0],
"gotten": [0],
"configdir": [3],
"prev": [[0,1,2,3,5,6,7,8]],
"csv": [0,3],
"installdist": [3],
"a-z": [0],
"enhanc": [5],
"password": [3,2],
"ambigu": [0],
"caractèr": [3],
"fr-zb": [3],
"let": [8,[0,6]],
"state": [0,[3,8],[1,7]],
"editordeleteprevtoken": [0],
"les": [6],
"press": [8,0,5,6,[2,3]],
"eventu": [3,1],
"dock": [3],
"onlin": [8,[0,1,3,5]],
"coffe": [0],
"element": [0,8,1],
"caret": [0],
"want": [8,3,0,5,7,[1,2]],
"night": [3],
"processor": [8,1],
"each": [0,8,[2,3,5],1,[6,7]],
"javascript": [8],
"mediawiki": [[5,8],[0,1]],
"input": [[3,5],2],
"toolkit": [3],
"creativ": [0],
"must": [3,0,[2,7]],
"join.html": [0],
"suppli": [2],
"non-omegat": [2],
"installdictionari": [7],
"cur": [0],
"filenameon": [2],
"cut": [0,6],
"ctrl": [0,5],
"editorinsertlinebreak": [0],
"jumptoentryineditor": [0],
"document": [0,3,8,1,[5,6],4,7],
"omegat.kaptn": [3],
"mainten": [3],
"two": [3,8,0,[2,5],[1,4,7]],
"accident": [2],
"user-defin": [8],
"moment": [3],
"pop": [[0,5]],
"page_up": [0],
"found": [8,5,[0,3],[2,6],1],
"usernam": [3],
"scenario": [3],
"encrypt": [0],
"larg": [8,[0,3]],
"attach": [8,2],
"anoth": [3,[0,5,8]],
"freez": [3],
"advantag": [0],
"graphic": [3],
"creation": [5,[1,2,7]],
"resourc": [1,8,7,3,0],
"latest": [7],
"pend": [8],
"moodl": [0],
"team": [3,5,[0,7],[1,2,4,8],6],
"xx_yy": [0],
"side-by-sid": [3],
"docx": [3,8,[0,5]],
"project_stats_match_per_file.txt": [7],
"diagram": [0],
"txt": [3,0,6],
"googl": [2],
"quit": [5,[0,2]],
"re-ent": [2],
"chart": [0],
"thing": [[1,3]],
"fashion": [1],
"definit": [0,2],
"lib": [0],
"viewmarkfontfallbackcheckboxmenuitem": [0],
"tedious": [0],
"had": [0,[1,8]],
"prepar": [[0,3],4],
"align": [8,[3,5],1,[0,2,4]],
"adjac": [6],
"endnot": [0],
"insertcharsrlm": [0],
"sourceforg": [3,0],
"structur": [[0,8],[3,7],1],
"han": [0],
"viewdisplaymodificationinfoselectedradiobuttonmenuitem": [0],
"entir": [[0,5],[3,6]],
"index.html": [0,3],
"semeru-runtim": [3],
"has": [0,3,8,5,2,[6,7]],
"keyword": [8,1],
"given": [3,2,[0,7],8],
"doubl": [0,3],
"actual": [[0,1]],
"unlock": [6,0],
"autosav": [6],
"last": [5,0,8,2,6,3,[1,7]],
"editmultipledefault": [0],
"adapt": [2,8,5],
"mozilla": [[0,3]],
"doubt": [[7,8]],
"editfindinprojectmenuitem": [0],
"develop": [3,[0,8],[2,4]],
"reproduc": [[0,8]],
"pro": [2],
"diffrevers": [2],
"warn": [8,3,2,0,1,[5,7]],
"bookmark": [0],
"easiest": [0,3],
"inlin": [8],
"page": [0,[5,8],3,[1,2]],
"full": [8,[2,3],[0,6]],
"plural": [0],
"away": [[2,3]],
"becaus": [0,2],
"three-column": [0],
"parenthesi": [0],
"project.gettranslationinfo": [8],
"czt": [0],
"doctorat": [2],
"precis": [3],
"overview": [3],
"yes": [8],
"duckduckgo.com": [2],
"start": [0,8,3,1,[2,5]],
"yet": [[1,2,5],[3,7,8]],
"mymemori": [2],
"stylist": [0],
"generic": [2,[0,3]],
"pair": [0,[2,3,8]],
"regex101": [0],
"equal": [0,[2,3,5,6]],
"colour": [[0,8],2],
"chang": [8,[0,2,3],7,5,6,1],
"watson": [2],
"anywher": [[6,7],[0,8]],
"short": [8,[0,1,2,3]],
"pop-up": [6],
"time": [0,3,8,[1,7],5,[2,6]],
"tmxs": [[2,5]],
"kanji": [0],
"program": [3,0,[1,8],[2,6]],
"three": [0,3,[6,7],[1,5,8]],
"cyan": [5],
"put": [3,0,7,[1,2,8]],
"project_save.tmx.yyyymmddhhmm.bak": [3],
"viewmarkglossarymatchescheckboxmenuitem": [0],
"her": [0],
"enter": [0,8,5,2,3,6,1],
"prioriti": [[2,5],[1,3]],
"tran": [0],
"pale": [5],
"applic": [3,[0,5],8,[1,2,7],4],
"bidi": [8],
"projectteamnewmenuitem": [0],
"iraq": [0],
"dossier": [6],
"preced": [0,8],
"right-click": [8,[5,6],[3,7]],
"directorate-gener": [5],
"non-seg": [0],
"brunt": [0],
"memori": [3,[1,8],7,[0,6],5,2,4],
"autocompletertablelast": [0],
"authent": [3,2,6],
"no-match": [5],
"retransl": [3],
"indefinit": [0],
"recogn": [0,8,[6,7],[1,2,3,5]],
"tabl": [0,2,4,[3,5,6]],
"engin": [6,[2,5],8],
"post-process": [[2,8]],
"log": [0,5],
"four-step": [8],
"smart": [3],
"lot": [0,[3,4,8]],
"doc-license.txt": [0],
"永住権": [[2,8]],
"openjdk": [3],
"everytim": [[0,7]],
"consult": [[0,3]],
"theme": [2,8],
"toolscheckissuesmenuitem": [0],
"チューリッヒ": [2],
"pane": [6,8,5,2,1,7,3,0,4],
"undesir": [5],
"meant": [7],
"editor": [8,0,[2,6],5,7,[1,3],4],
"pseudotranslatetyp": [3],
"tutori": [0],
"orphan": [8],
"cycl": [5,0],
"autocompletertablepageup": [0],
"fetch": [5,2,0],
"www.deepl.com": [2],
"char": [8],
"small": [[0,5]],
"config-fil": [3],
"quick": [0,8,3],
"tell": [3,[0,1,8]],
"unavail": [0],
"projectclosemenuitem": [0],
"checker": [2,[0,5]],
"viewmarknonuniquesegmentscheckboxmenuitem": [0],
"hit": [8,5],
"major": [0,3],
"shown": [6,[5,8],[0,2,3]],
"titl": [5,[0,8]],
"consider": [3],
"inspir": [8],
"day": [3,0,1],
"lre": [[0,5]],
"group": [0,2,[6,8],[1,3]],
"obtain": [[1,2,3,8]],
"suppos": [0],
"system-user-nam": [0],
"findinprojectreuselastwindow": [0],
"lrm": [[0,5]],
"liter": [0,2],
"format": [3,0,1,8,[2,5],[6,7],4],
"tree": [7],
"particular": [[3,8]],
"readme.txt": [3,0],
"done": [3,[1,6]],
"languagetool": [5,2,[4,8]],
"console.println": [8],
"rainbow": [3],
"twice": [0],
"source.txt": [[0,2]],
"files.s": [8],
"autocompleterlistdown": [0],
"histori": [0,5,2],
"exchang": [0],
"auto-sync": [8],
"achiev": [3],
"launcher": [3],
"request": [[3,5]],
"procedur": [3],
"pars": [[0,6,8]],
"part": [0,2,8,5,[1,3,7]],
"currseg": [8],
"their": [[0,8],3,1,2,[5,6,7]],
"generat": [7,3,[0,1,2,5]],
"unexpect": [3],
"point": [0,2,3],
"general": [8,[0,3],2,6,4],
"browser": [2,[5,6]],
"activefilenam": [8],
"easi": [1,3],
"process": [0,[2,3],[1,8],5,4],
"project_files_show_on_load": [0,8],
"autocompletertrigg": [0],
"instance—a": [6],
"attribut": [0],
"clear": [[5,8]],
"ltr": [3,[0,4]],
"apostroph": [0],
"third": [3,[6,7],[0,1]],
"acquiert": [2],
"build": [3,[4,8]],
"mean": [0,8,[2,3,6]],
"neither": [0],
"further": [1,[0,7],[6,8]],
"account": [3,[2,5,7,8]],
"snippet": [8],
"been": [3,5,8,[1,2],[0,7]],
"stack": [8],
"dhttp.proxyhost": [3],
"japanes": [[2,3],0],
"ident": [8,[2,5,7],[0,3,6]],
"entries.s": [8],
"addit": [0,8,2,[1,3,4,5]],
"alphabet": [0,[6,8]],
"simplifi": [2,3],
"gotonextuntranslatedmenuitem": [0],
"subdirectori": [3],
"targetlocal": [0],
"systemwid": [3],
"editorprevseg": [0],
"path": [3,2,[6,8]],
"trip": [3],
"bind": [8],
"overwritten": [3,1,[0,2,8]],
"abbrevi": [0],
"record": [0,7],
"monospac": [2],
"a-za-z0": [0],
"strict": [3],
"you": [3,8,0,1,2,7,5,6,4],
"jump": [0,[6,8],5],
"reinsert": [1],
"happen": [3],
"www.apertium.org": [2],
"pass": [3],
"past": [5,[3,6]],
"impact": [1],
"mainstream": [0],
"percentag": [6,2,7],
"especi": [[3,8]],
"cours": [[1,7,8]],
"whose": [5],
"project_save.tmx.tmp": [3],
"configur": [3,0,2,5,8,6,4],
"nativ": [3,[0,2]],
"helpcontentsmenuitem": [0],
"resnam": [0],
"omegat-org": [3],
"descript": [[0,2,8],[1,4,5]],
"remote-project": [3],
"preserv": [0,3,2],
"initialcreationid": [2],
"ignore.txt": [7],
"organ": [0],
"mega": [0],
"projectaccessdictionarymenuitem": [0],
"zurich": [2],
"空白文字": [3],
"sentenc": [0,[1,8],2,[3,4,5]],
"alongsid": [3],
"optionsworkflowmenuitem": [0],
"consecut": [8,0],
"dgt": [3],
"how": [[0,1,3],2,6,[4,8]],
"releas": [3,[0,5]],
"term": [8,6,0,5,2,1,7],
"backslash": [0,3],
"files_order.txt": [7],
"mind": [[0,3]],
"projectrestartmenuitem": [0],
"editorskipnexttoken": [0],
"trans-unit": [0],
"right": [0,8,[3,6],5,[2,7]],
"insid": [3,0],
"qigong": [0],
"stage": [3,8],
"keybind": [8],
"maximum": [0,[3,8]],
"under": [3,[5,8],[0,2,6,7]],
"xhmtl": [0],
"submenus": [[3,8]],
"did": [8],
"represent": [8],
"imper": [8],
"reserv": [3],
"dir": [3],
"down": [0,8,2],
"hold": [8,1,6],
"linebreak": [0],
"trail": [0],
"subdir": [3],
"later": [3,0,7,[6,8]],
"legal": [0],
"bracket": [0],
"unrespons": [8],
"viewfilelistmenuitem": [0],
"info": [0,[2,5,6]],
"hyperlink": [6],
"brows": [8,6],
"autocompletertableleft": [0],
"non-break": [8,0,5,2],
"journey": [0],
"test": [3,0],
"count": [5,2],
"omegat": [3,0,8,1,2,5,7,4,6],
"forward-backward": [8],
"allemand": [2,8],
"deepl": [2],
"take": [0,8,3,[1,2,5]],
"month": [[0,3],1],
"thereof": [0],
"final": [0,1,[3,7]],
"editorlastseg": [0],
"file-source-encod": [0],
"occasion": [0],
"some": [3,0,2,[6,7],[1,5,8]],
"virtual": [8,3],
"blank": [0],
"rather": [0,8,3],
"session": [[0,3,6,8]],
"console-align": [[3,8]],
"back": [8,3,[0,6],1,5,7],
"projectopenrecentmenuitem": [0],
"miss": [5,0,1,[3,6]],
"thèse": [2],
"load": [3,8,[0,2],7,5],
"alpha": [3],
"大学院博士課程修了": [2],
"just": [0,[1,3],8,7],
"human": [2],
"divid": [0,8],
"primarili": [3],
"collabor": [[1,3]],
"custom": [0,3,2,5,1,[4,8]],
"editexportselectionmenuitem": [0],
"length": [0],
"issue_provider_sample.groovi": [8],
"home": [0,3,[1,2,5,6,7,8]],
"disable-location-sav": [3],
"print": [[0,3,8]],
"condit": [3],
"glyph": [5],
"unl": [6],
"although": [0,3],
"projectaccesstargetmenuitem": [0],
"interpret": [0],
"editoverwritemachinetranslationmenuitem": [0],
"iana": [0],
"relat": [[2,3,7],6],
"console-stat": [3],
"grant": [3],
"ingreek": [0],
"lunch": [0],
"f12": [8],
"visibl": [[0,7]],
"convers": [3,[2,4]],
"ignor": [0,7,[2,5,6,8],3],
"convert": [3,[1,2],[0,5,8]],
"hope": [1],
"attempt": [3,8,[0,2]],
"editorswitchorient": [0],
"soon": [[5,7]],
"influenc": [5],
"projectexitmenuitem": [0],
"aligndir": [3,8],
"system-host-nam": [0],
"action": [[5,8],3,0,6,[1,2]],
"lock": [6,3,0],
"adoptium": [3],
"text": [0,8,5,2,6,3,1,7,4],
"latin": [0],
"mymemory.translated.net": [2],
"en-to-fr": [3],
"fear": [3],
"editregisteruntranslatedmenuitem": [0],
"creat": [3,8,0,1,7,5,6,2,4],
"init": [3],
"python": [8],
"made": [[0,1,3],[5,8],[2,6,7]],
"block-level": [0],
"manag": [1,[3,5],4,[2,7]],
"manifest.mf": [3],
"maco": [0,3,5,6,4,[1,2],8],
"field": [8,6,5,[0,3],2],
"tarbal": [7],
"singl": [0,8,1],
"invalid": [3],
"doc": [8,0],
"doe": [0,3,8,1,[2,5]],
"output-fil": [3],
"notifi": [2],
"status": [6,5,3,[0,1,2,4]],
"server": [3,2,7,6],
"don": [1],
"dot": [[0,5]],
"paramet": [3,0,8,2,7],
"stamp": [[0,7]],
"run-on": [0],
"skip": [0,[7,8]],
"www2": [2],
"overrid": [[0,3]],
"file": [3,0,8,5,7,1,2,6,4],
"known": [[0,5]],
"member": [3,1],
"map": [3,8,4,7],
"may": [3,[0,8],6,[2,5,7]],
"within": [0,[3,7],[1,6]],
"forward": [[0,5]],
"could": [[0,1,3,7,8]],
"trigger": [[0,3,8]],
"menu": [0,6,8,5,2,1,4,[3,7]],
"url": [3,2,7,[0,5,8]],
"megabyt": [3],
"system-wid": [0],
"uppercasemenuitem": [0],
"explan": [8,[0,2]],
"viewmarkuntranslatedsegmentscheckboxmenuitem": [0],
"discrep": [5],
"probabl": [3],
"relev": [[0,3],2,[1,5]],
"needs-review-transl": [0],
"tagwip": [8],
"return": [8,0,[1,6]],
"nonsens": [0],
"invoke-item": [[2,8]],
"usb": [3],
"use": [3,0,8,2,1,5,6,7,4],
"usd": [8],
"feel": [[1,3],7],
"main": [[6,8],2,[3,5],0],
"newlin": [0],
"convent": [0],
"radio": [8],
"omegat.jar": [3,0],
"source-pattern": [3],
"strip": [8],
"conveni": [[0,3,8]],
"omegat.app": [3,0],
"fine": [0],
"usr": [[2,3]],
"find": [0,3,[1,2],7],
"host": [3,[0,2]],
"logo": [0],
"backward": [5],
"errat": [0],
"credit": [5],
"regardless": [3],
"alter": [8],
"workflow": [0,1,[4,6]],
"utf": [0,7],
"occur": [0],
"autocompletertablepagedown": [0],
"difficult": [3],
"sort": [6,[2,5,8]],
"fill": [7,3,5],
"feed": [0],
"servic": [3,2,6,5],
"forget": [2],
"task": [3,[0,8]],
"cleanup": [8],
"background": [7,[5,6]],
"xliff": [3,0],
"true": [0],
"header": [0,[5,8]],
"nonetheless": [[0,8]],
"present": [0,[3,8],[5,6],[1,2]],
"dsl": [7],
"mid-transl": [8],
"belong": [[0,2]],
"groovi": [8],
"pre-defin": [0],
"multi-paradigm": [8],
"best": [2,[0,1,6,7,8]],
"med": [5],
"transform": [2],
"fundament": [0],
"execut": [3,[0,8]],
"hour": [[1,3]],
"dtd": [[0,3]],
"repeat": [[0,3,8]],
"make": [3,0,7,[1,8],2,6,4],
"abov": [0,3,[2,8],[1,4,5]],
"sentence-level": [[0,8],1],
"projectcompilemenuitem": [0],
"classnam": [3],
"messageformat": [2,0],
"console-transl": [3],
"stern": [1],
"compound": [2],
"master": [3,2,0],
"optionsautocompletehistorycompletionmenuitem": [0],
"gotonextuniquemenuitem": [0],
"due": [1,3],
"conform": [3],
"underlin": [5,2],
"writer": [0],
"wordart": [0],
"merg": [8,1,[0,2]],
"dalloway": [2],
"rubi": [8],
"resource-bundl": [3],
"inform": [3,6,[0,2,5],8,[1,7]],
"depend": [[0,2,3,5],6,[1,8]],
"about": [0,[1,3,6,8],[5,7]],
"commit": [3,[0,5]],
"yyyi": [3],
"external_command": [7],
"targetlocalelcid": [0],
"annot": [1],
"project_stats_match.txt": [7],
"cover": [0,3],
"editorselectal": [0],
"character": [0],
"tab-separ": [0],
"reflect": [3,[0,1,5,7,8]],
"flexibl": [1],
"runner": [8,0],
"immedi": [0,8,[5,6]],
"simlp": [2],
"pointer": [6],
"condens": [2],
"omegat-default": [3],
"benefit": [8],
"user.languag": [3],
"regex": [0],
"highest": [6,2],
"meta": [0],
"declar": [0],
"except": [2,0,3,1],
"boost": [0],
"libreoffic": [1,0],
"autocompleterclos": [0],
"qualiti": [5],
"nevertheless": [1],
"scan": [8],
"global": [8,0,2,5,4,[1,3,6]],
"long": [0,[2,5]],
"into": [3,[0,8],6,5,2,[1,7]],
"unless": [3,[0,1,2]],
"defin": [0,2,3,8,[5,6],[1,7]],
"industri": [1],
"free": [3,1,[0,4]],
"mix": [3,4],
"evolut": [0],
"star": [0],
"though": [2],
"thorough": [0,3],
"stax": [3],
"everyday": [3],
"viewdisplaysegmentsourcecheckboxmenuitem": [0],
"appear": [2,[0,8],[1,6],[3,4,5]],
"editregisteremptymenuitem": [0],
"non-uniqu": [[2,5],[0,8]],
"face": [2],
"ibm": [[2,3]],
"stats-output-fil": [3],
"mismatch": [8],
"french—if": [2],
"progress": [6,3,2],
"reliabl": [7,3],
"oper": [8,[0,2,3,5],7],
"mani": [0,3],
"open": [8,5,0,3,2,6,1,7,4],
"treat": [0,[6,8],2],
"parsewis": [8],
"project": [3,8,7,0,5,1,[2,6],4],
"seven": [0],
"user-cr": [0],
"取得": [[2,8]],
"trustworthi": [3],
"xmx1024m": [3],
"whatev": [3],
"autotext": [2,0],
"sever": [8,[0,5,6],[3,7]],
"loop": [8],
"autocomplet": [0,4],
"enclos": [0,8,1],
"penalty-xxx": [[3,7]],
"gotonextsegmentmenuitem": [0],
"invert": [2],
"omegat-cod": [3],
"look": [1,0,2,3,[4,8]],
"repres": [0,[2,3]],
"abort": [3],
"left-to-right": [[0,5],3],
"guid": [0,5,1,4,3],
"idx": [7],
"internet": [2],
"conflict": [3,[0,1]],
"allow": [8,0,5,2,3,1,6],
"comma-separ": [0],
"squar": [0,2],
"commerci": [1],
"rule": [8,0,2,5,[3,4],1,7],
"proper": [3,[0,2,5],[6,8]],
"detect": [5,[2,3]],
"everi": [3,[0,7,8],5,[1,2]],
"speed": [3],
"printf": [0,2],
"peopl": [[1,3]],
"summari": [3,4],
"outsid": [0,[1,6,7]],
"autocompleterconfirmandclos": [0],
"common": [3,0,6,[1,4,8]],
"projectaccesscurrentsourcedocumentmenuitem": [0],
"interest": [0],
"appli": [2,8,3,0,[5,6,7]],
"linux": [0,3,5,6,[4,8],[1,2]],
"linux-install.sh": [3],
"again": [[0,2,3,6,8]],
"file.txt": [3],
"openxliff": [3],
"uncom": [3],
"writabl": [0,[5,7,8],[3,6]],
"layout": [1,2,0,[4,5,6],8],
"registri": [0],
"popup": [8],
"ifo": [7],
"popul": [[2,7]],
"comment": [0,6,[5,8],4],
"step": [8,[0,3],[1,7]],
"bash": [3],
"basi": [0],
"excit": [0],
"mark": [5,0,8,2,1,6,7,4],
"base": [0,[1,8],2,[3,4,5,6]],
"stem": [2,6],
"registr": [2],
"disconnect": [3],
"compulsori": [8],
"optionsmtautofetchcheckboxmenuitem": [0],
"xx.docx": [0],
"prefix": [7,2],
"whole": [[0,1,2,3,7,8]],
"consist": [0,8,[1,3,5,6,7]],
"loss": [3],
"大学": [2],
"lost": [3,[1,4,6]],
"editorshortcuts.properti": [0],
"insertcharslr": [0],
"grammat": [5],
"still": [[0,8],[5,6]],
"compress": [[0,7]],
"work": [3,0,8,1,7,[2,5]],
"lose": [3,1],
"suitabl": [[0,3,6]],
"fail": [3,1],
"itself": [0,3,8,7],
"sdlxliff": [3],
"among": [3,1],
"word": [0,8,[5,7],[2,6],1,3],
"variat": [0],
"love": [0],
"lingue": [2],
"thumb": [8],
"auto-propag": [[3,8]],
"requir": [3,[0,2],8,[5,6],[4,7]],
"across": [0],
"viewmarknotedsegmentscheckboxmenuitem": [0],
"non-vis": [0],
"event": [8,0,3],
"simplest": [0,3],
"vcs": [3],
"lingvo": [7],
"gotomatchsourceseg": [0],
"abstract": [4],
"appropri": [3,[0,8]],
"developer.ibm.com": [3],
"mrs": [2],
"opinion": [6],
"excel": [0],
"comma": [0],
"runn": [8],
"literari": [0],
"cannot": [3,[0,5,6]],
"runt": [0],
"averag": [8],
"stardict": [7],
"first": [0,8,[3,5],6,2,[1,7]],
"omegat.l4j.ini": [3],
"span": [0],
"prefer": [0,2,6,5,3,8,4,7],
"quotat": [[0,8]],
"threshold": [2],
"overridden": [3],
"float": [2],
"space": [0,8,1,[5,6],2],
"hard-and-fast": [8],
"ドイツ": [8,2],
"manipul": [6],
"simpl": [0,2,[1,3,8],4],
"from": [3,0,8,7,6,2,[1,5]],
"html": [0,3,2,1],
"spell": [[0,2],5,8],
"editselectfuzzy3menuitem": [0],
"you\'ll": [3],
"bottom": [8,2,6,5],
"insertcharsrl": [0],
"templat": [2,[4,8]],
"fals": [0,[3,8]],
"project.projectfil": [8],
"uncondit": [7],
"finit": [2],
"frequenc": [3],
"www.ibm.com": [2],
"jres": [3],
"frequent": [0,[1,3,8]],
"interact": [[3,8]],
"outright": [1],
"error": [3,6,5,[0,1,8]],
"egregi": [0],
"platform": [3,0,4],
"network": [3],
"shortcut": [0,5,[1,8],[4,6],3],
"public": [3],
"briefli": [[0,6]],
"track": [6],
"toolsalignfilesmenuitem": [0],
"tmx2sourc": [[3,7]],
"ini": [3],
"spent": [0],
"overal": [[0,5]],
"instead": [0,[2,3,5,6,8]],
"improv": [0,8],
"command": [8,3,2,0,5,7,6,[1,4]],
"project-specif": [7,5,[2,3,8]],
"unlik": [[0,1,2]],
"round": [3],
"dhttp.proxyport": [3],
"detach": [8],
"slash": [0],
"tag-fre": [8],
"www.microsoft.com": [2],
"negat": [0],
"notat": [0,[1,2]],
"viewmarkbidicheckboxmenuitem": [0],
"refus": [1],
"year": [[0,3]],
"subrip": [3],
"branch": [3],
"via": [3],
"describ": [0,3,[1,6,8]],
"score": [2,8,7],
"fileshortpath": [2],
"permiss": [8],
"poor": [8],
"double-click": [[3,8],[0,5,6]],
"visual": [[0,5]],
"absent": [3],
"near": [0],
"approxim": [8],
"日本語": [8],
"instruct": [3,0,[7,8]],
"appendix": [0,[6,8]],
"illustr": [0],
"raw": [3],
"version": [3,0,[5,7,8]],
"unassign": [5],
"folder": [3,8,7,0,5,2,1,6,4],
"stop": [2],
"handl": [0,[2,3]],
"detail": [5,[3,8],0,2,1,7,6],
"retriev": [3],
"projecteditmenuitem": [0],
"least": [3,8],
"manual": [[3,8],[0,5,7],1],
"dollar": [0],
"new_word": [8],
"recycl": [3],
"run\'n\'gun": [0],
"aspect": [1],
"appendic": [0,[2,4]],
"unbeliev": [0],
"measur": [3],
"nashorn": [8],
"machin": [2,5,6,[3,8],[0,4],7],
"behavior": [3,5],
"close": [0,8,3,5,[2,6]],
"unsung": [0],
"abc": [0],
"learn": [0],
"last_entry.properti": [7],
"abl": [3,0,[1,8]],
"textual": [8,0],
"toolbar.groovi": [8],
"newer": [5],
"uppercas": [0],
"invok": [8],
"iso": [0,3],
"eager": [1],
"isn": [[0,3]],
"supprim": [6],
"optionspreferencesmenuitem": [0],
"thorni": [0],
"autocompleternextview": [0],
"specif": [8,3,0,[2,5]],
"red": [[2,7],[0,8]],
"aggreg": [0],
"act": [7,0],
"soft-return": [0],
"post": [0],
"glossary.txt": [[3,7],[0,5]],
"finish": [8,0],
"dsun.java2d.noddraw": [3],
"placehold": [2,[0,3]],
"mispel": [0],
"add": [0,3,8,7,1,[2,5,6]],
"initi": [8,[0,3,7],2],
"multi-word": [0],
"chines": [2],
"ell": [2],
"need": [3,0,[1,2,7,8],[5,6]],
"equival": [8,[0,2,3],6],
"often": [1,[0,3],8],
"editorfirstseg": [0],
"x0b": [3],
"gather": [3],
"els": [6],
"respect": [[0,3,7]],
"rfe": [8],
"canada": [3],
"shell": [0],
"pre-configur": [2],
"port": [3],
"altern": [0,8,5,6,2,3],
"entry_activ": [8],
"http": [3,2,6],
"optionsautocompleteshowautomaticallyitem": [0],
"trust": [2,8],
"untar": [3],
"interfer": [5],
"lisenc": [0],
"consequ": [[1,3]],
"prevent": [3,1],
"undo": [5,0],
"glitch": [1],
"softwar": [3,0],
"ishan": [0],
"scope": [[0,1,8]],
"pasta": [0],
"projectsinglecompilemenuitem": [0],
"end": [0,8,[2,3]],
"lisens": [0],
"footnot": [0],
"modifi": [0,8,3,1,2,7,[4,5],6],
"espac": [3],
"otherwis": [3,[7,8]],
"myfil": [3],
"particip": [3],
"anyth": [[0,1,3]],
"label": [[2,5]],
"howev": [[0,3,8],1],
"fledg": [3],
"special": [0,[3,7,8]],
"okapi": [3],
"togeth": [1],
"page_down": [0],
"key-bas": [8],
"numer": [[0,3]],
"clone": [3],
"fine-tun": [6],
"targetlanguag": [[0,2]],
"directori": [3,0],
"sensit": [0,8],
"backup": [3,7,2,[4,8]],
"copyright": [5],
"properti": [3,6,0,[5,7,8],1,[2,4]],
"project_nam": [8],
"system-os-nam": [0],
"occurr": [8],
"insertcharspdf": [0],
"editselectfuzzyprevmenuitem": [0],
"number": [0,8,6,2,3,5,7,1],
"identifi": [0,2,5,[1,3,4,8]],
"specifi": [3,0,2,8,6,7],
"heapwis": [8],
"narrow": [1],
"faulti": [3],
"algorithm": [8,[0,5]],
"shorter": [8],
"troubleshoot": [3,[1,4,7]],
"newli": [7],
"similar": [0,[2,3],[1,6],[5,8]],
"tar.bz2": [7],
"paragraph-level": [0],
"forth": [3,6],
"bundle.properti": [3],
"script": [8,3,0,5,2,4,7],
"contributors.txt": [0],
"exit": [[3,8],5],
"system": [3,0,[2,5,8],7,6],
"default.th": [3],
"driver": [2],
"spellcheck": [2,7,8,3,[0,4,5]],
"www.regular-expressions.info": [0],
"characterist": [[0,2,8]],
"issu": [5,3,2,0,1,[4,7]],
"partial": [3],
"other": [[0,3],[2,7,8],[1,6],[4,5]],
"against": [[1,3,6]],
"retain": [3,0],
"savor": [0],
"parenthes": [[0,2]],
"login": [2,0,4],
"cell": [8,0],
"local": [3,8,2,0,5,[4,6,7]],
"optionsdictionaryfuzzymatchingcheckboxmenuitem": [0],
"resum": [0],
"remind": [[0,5,7]],
"valid": [0,[2,5],[3,7]],
"pictur": [0],
"assur": [5],
"interfac": [3,0,2,[1,6]],
"projet": [6],
"locat": [3,0,8,5,2,7,1,6,4],
"yield": [8],
"share": [3,8,7,[0,1,4],6],
"sourcelanguag": [2],
"rle": [[0,5]],
"gzip": [7],
"helpupdatecheckmenuitem": [0],
"duplic": [8,6,[0,1]],
"repo_for_all_omegat_team_project_sourc": [3],
"notic": [[3,8]],
"rlm": [[0,5]],
"esc": [6],
"exampl": [0,8,[2,3],6,5,[1,4],7],
"nostemscor": [2],
"first-third": [3],
"project_chang": [8],
"round-trip": [3],
"screen": [0],
"correspond": [8,0,[5,6],[2,7],3],
"c-x": [0],
"console-createpseudotranslatetmx": [3],
"mode": [3,8,6],
"etc": [2,[3,8],[1,7],[0,6]],
"fuzzyflag": [2],
"toolsshowstatisticsstandardmenuitem": [0],
"all": [8,3,[0,2],5,1,7,6],
"precaut": [3],
"border": [6],
"new": [3,8,0,1,5,2,7,[4,6]],
"escap": [0,3],
"took": [3],
"read": [8,3,0,2],
"simplic": [3],
"sequenti": [5],
"below": [0,3,6,[5,7,8]],
"c.t": [0],
"alt": [0,5],
"poisson": [8],
"runway": [0],
"choos": [0,[2,3,8]],
"rememb": [3,1,[0,5],8],
"half-width": [8],
"real": [3,[2,6]],
"tool": [3,8,[0,4,5,7],2,1],
"ll-cc.tmx": [3],
"unit": [0,8],
"alreadi": [3,0,7,[2,8],5],
"therefor": [0,8,3],
"collect": [[0,7]],
"two-lett": [3],
"redo": [[0,5]],
"media": [3],
"slot": [5],
"around": [[1,2]],
"simpler": [3],
"grunt": [0],
"reload": [8,5,[0,1,3,7]],
"tkit": [3],
"calcul": [[2,6],8],
"and": [0,3,8,5,6,2,1,7,4],
"synchron": [3,[6,8],[1,2,7]],
"predict": [[0,2]],
"row": [8,[0,5]],
"ani": [0,3,8,7,[1,5],6,2],
"render": [8],
"magento": [3],
"backs-up": [7],
"ant": [[3,8]],
"korean": [2],
"boundari": [0,1],
"dispar": [3],
"offlin": [3],
"ll_cc.tmx": [3],
"unnecessari": [7,2],
"u00a": [8],
"until": [3,[0,2,8]],
"helplastchangesmenuitem": [0],
"omegat.ex": [3],
"reason": [[0,2],[5,8]],
"thought": [3],
"shift": [0,5,8],
"sourcetext": [2],
"simultan": [2],
"java": [3,0,2,8],
"exe": [3],
"english": [3,2],
"xmxsize": [3],
"mistak": [2,[0,1,5,8]],
"jar": [3],
"api": [2],
"editselectfuzzy2menuitem": [0],
"project_save.tmx": [3,7,[1,8],5],
"encapsul": [8],
"dictionari": [2,7,6,[0,5],8,4,3],
"remain": [8,7,5,3],
"powershel": [[2,3,8]],
"eye": [[0,3]],
"letter": [0,5,8,[1,2,3]],
"grade": [1],
"editornextseg": [0],
"appl": [0],
"editselectfuzzynextmenuitem": [0],
"recommend": [3,[0,8]],
"worth": [[0,8]],
"read.m": [0],
"default": [0,[2,8],3,5,6,7,1,4],
"gray": [5,8,1],
"are": [[0,3],8,2,5,6,7,1],
"cloud.google.com": [2],
"taken": [[2,7],[3,5,6,8]],
"readme.bak": [3],
"where": [0,8,3,5,[2,6],1,7],
"sudo": [3],
"drop-down": [8,0,2],
"timestamp": [[0,3,4]],
"logogram": [0],
"broken": [[1,3]],
"vice": [8,3],
"projectaccessrootmenuitem": [0],
"nest": [0],
"rtl": [3,4,[0,5]],
"fulli": [7],
"call": [0,[3,8],5,[2,6,7]],
"facilit": [1],
"such": [0,3,[2,8],1,6,7,5],
"plugin": [3,2,0,4,1],
"autocompletertableup": [0],
"essenti": [3],
"ask": [3,8,[2,5],[0,1,6]],
"principl": [[1,4,6]],
"tabul": [3],
"understood": [8],
"through": [3,8,[0,6],[2,5]],
"toolsshowstatisticsmatchesperfilemenuitem": [0],
"strength": [8],
"projectcommitsourcefil": [0],
"editinsertsourcemenuitem": [0],
"run": [8,3,0,[2,5],[1,4]],
"viterbi": [8],
"microsoft": [0,[1,2,3]],
"reorgan": [0],
"projectnewmenuitem": [0],
"ecmascript": [8],
"worri": [1],
"either": [0,3,[2,7],1,8],
"view": [[0,2],[1,4],[5,6,8],7],
"lowercas": [0],
"white": [0,5],
"editorshortcuts.mac.properti": [0],
"segment": [8,0,5,6,2,7,1,3,4],
"changes.txt": [[0,3]],
"titlecasemenuitem": [0],
"yourself": [[1,3]],
"those": [[0,3],8,[1,7],[2,6]],
"glossari": [0,6,8,[5,7],2,[1,3],4],
"recurs": [8],
"editcreateglossaryentrymenuitem": [0],
"ignored_words.txt": [7],
"might": [3,1,0],
"configuration.properti": [3],
"github.com": [3],
"ital": [[0,1]],
"bold": [[2,6],[0,8],1],
"dure": [[3,8],[0,1,7]],
"autocompleterlistpageup": [0],
"effici": [1],
"longer": [3,[0,2,8]],
"introduc": [8],
"supersed": [8],
"多和田葉子": [8],
"occupi": [8],
"reopen": [3],
"name": [0,3,2,6,[1,8],7,5],
"physic": [3],
"recreat": [[0,1,3,7]],
"next": [0,5,8,[1,3],6,2,[4,7]],
"string": [8,0,[2,5],3,1],
"import": [[0,3],[1,6,7]],
"color": [2,5,7,4],
"hidden": [7],
"reli": [0,3],
"book": [[0,1,7]],
"show": [8,6,[2,3],0,[5,7]],
"cautious": [3],
"target-languag": [3],
"non": [0,[1,2]],
"button": [8,0,[1,2]],
"nor": [0],
"comput": [3,[1,2]],
"not": [0,3,8,2,1,5,7,6],
"now": [[0,3]],
"introduct": [1,4,3],
"trademark": [6],
"factor": [5],
"editortogglecursorlock": [0],
"enabl": [2,0,6,[3,8],1],
"greek": [0],
"green": [8,6,5],
"associ": [0,3,2,5,1,[4,6,8]],
"pseudotransl": [3],
"was": [2,[0,3,8],5,[1,7]],
"subfold": [0,3,[7,8]],
"greet": [0],
"new_fil": [8],
"selection.txt": [[0,5]],
"way": [0,3,8,5],
"target": [0,5,8,3,2,7,[1,6],4],
"xhtml": [0],
"grey": [5],
"what": [0,[1,3,8],2],
"itoken": [3],
"knowledg": [3],
"finder.xml": [[0,7,8]],
"refer": [0,[3,7],8,1,[4,6]],
"workfow": [0],
"colon": [0],
"window": [8,0,5,6,3,2,4,[1,7]],
"call-out": [5],
"config-dir": [3],
"editorskipprevtokenwithselect": [0],
"discard": [8],
"any—wil": [2],
"criteria": [8,[0,1]],
"disable-project-lock": [3],
"displac": [5],
"omegat.pref": [[0,2,8]],
"when": [8,0,2,3,6,7,[1,5]],
"termbas": [0],
"sequenc": [0],
"auto-popul": [[2,5],[0,7]],
"carriage-return": [0],
"far": [6,3],
"embed": [[0,5]],
"catch": [3],
"plan": [2],
"case": [0,5,8,3,2],
"give": [3,[0,5,8],[1,2,7]],
"item": [0,5,8,2,[1,3]],
"multipl": [0,6,3,[2,4]],
"violet": [5],
"unfriend": [1],
"matcher": [0],
"lowest": [6],
"explicit": [3],
"targettext": [2],
"consid": [0,[2,7],[1,5,8]],
"slide": [0],
"reset": [8,0,2],
"everyth": [[0,3]],
"style": [3,8],
"explor": [0],
"suit": [0,[3,6]],
"card": [8,0],
"care": [1],
"widget": [[4,6]],
"orang": [[0,5,7,8]],
"portion": [[0,8],5],
"mose": [2],
"guard": [3],
"pattern": [0,2,8,3],
"direct": [3,[0,5],[6,7,8]],
"compil": [8],
"caus": [[0,5]],
"mechan": [[0,3,5]],
"modern": [3],
"web": [2,8,3,[0,1,6]],
"edittagpaintermenuitem": [0],
"en-us_de_project": [3],
"you\'r": [1],
"temporarili": [2],
"symlink": [3],
"older": [3],
"protect": [2,1],
"nth": [8],
"editselectfuzzy4menuitem": [0],
"editregisteridenticalmenuitem": [0],
"more": [0,8,[1,2,3,7],6,[4,5]],
"display": [[2,8],5,6,0,3,7,1,4],
"hanja": [0],
"great": [1],
"unicod": [0,5,4],
"viewmarknbspcheckboxmenuitem": [0],
"availab": [0],
"fanci": [0],
"usag": [8,[3,4]],
"computer-assist": [[1,4]],
"left-hand": [8],
"certain": [3,[0,5,6]],
"advanc": [8,0,[2,5]],
"shut": [8],
"overwrit": [6,7,[0,3,5]],
"en-us": [2],
"path-to-omegat-project-fil": [3],
"fed": [3],
"whitespac": [0,[3,5]],
"credenti": [2,3,[4,6]],
"section": [0,3,6],
"auto-complet": [[0,2],[1,6],[4,5]],
"simpli": [0,[1,3],[7,8]],
"cloud": [3],
"protocol": [3,2],
"msgstr": [0],
"few": [0,1,3,[2,4,8]],
"dict": [2],
"untransl": [0,8,5,2,6,[1,3],7],
"orient": [0],
"nationalité": [2],
"kind": [[0,2]],
"daili": [0],
"resiz": [6],
"both": [8,3,0,2,6],
"most": [0,3,5,[6,8],[1,2,7]],
"delimit": [2,8,[1,5,6],0],
"phrase": [0,8],
"omegat.project": [3,7,[1,2,4,6,8]],
"marker": [[0,2,6]],
"keep": [8,3,0,[1,7],[2,6]],
"effect": [0,8,2],
"whi": [0],
"topic": [[0,3]],
"excludedfold": [3],
"targetcountrycod": [0],
"job": [0,[1,3]],
"fallback": [[0,5]],
"option": [0,8,3,2,5,4,1,7,6],
"who": [[2,8],[1,3]],
"overtyp": [0],
"continu": [0,[1,8]],
"insert": [5,[0,2],[6,7],8,[1,3]],
"everyon": [3],
"resid": [3],
"highlight": [8,6,7],
"along": [0,8],
"arrang": [2],
"sheet": [0],
"messag": [6,[0,3]],
"rest": [0,[1,3]],
"move": [5,8,6,1,[0,2]],
"amount": [3],
"also": [3,8,0,2,7,5,1,6],
"enough": [3],
"differ": [2,3,[5,8],0,6,1],
"conson": [0],
"situat": [3,0],
"consol": [3],
"mous": [8,[0,5,6]],
"vice-versa": [3],
"yandex": [2],
"various": [0,5,[1,3],8,2,[6,7]],
"archiv": [7],
"front": [3],
"visit": [5,7],
"user": [3,0,5,[2,4],[1,6,8]],
"itokenizertarget": [3],
"viewmarkwhitespacecheckboxmenuitem": [0],
"proxi": [3,2,4],
"extens": [0,3,7,[2,4,5,6,8]],
"back_spac": [0],
"potenti": [0,[5,7]],
"asterisk": [0],
"bring": [8,[0,3,6]],
"tooltip": [2,6],
"complet": [0,2,3,8],
"recalcul": [8],
"bak": [3,7],
"canon": [0],
"offer": [3,[0,1],6,8],
"robot": [0],
"fit": [1],
"bar": [6,0,4],
"claus": [0],
"fix": [3,[0,5,7]],
"built-in": [2],
"complex": [0,8],
"jre": [3],
"rang": [0,3],
"despit": [8],
"posit": [0,5,[6,8],[2,4]],
"eclips": [3],
"sure": [3,2,[0,6,8]],
"ad": [0,[3,8],2,[1,5,7]],
"reus": [3,[0,1,4,5]],
"diff": [2],
"automat": [8,3,7,2,5,0,1,[4,6]],
"an": [0,3,8,2,1,[5,6],7],
"editmultiplealtern": [0],
"secur": [2,8,4],
"panic": [1],
"extend": [[3,8]],
"as": [0,3,8,5,2,7,[1,6]],
"day-to-day": [0],
"at": [8,0,[2,5],3,6,1,7],
"predefin": [2,[0,3]],
"constitut": [0,[3,7]],
"hierarchi": [7,3],
"ordinarili": [0],
"drive": [3],
"alllemand": [8],
"non-gui": [3],
"deal": [[0,8]],
"be": [0,3,8,2,5,7,1,6],
"affect": [3],
"icon": [5,6],
"filters.xml": [0,[2,3,7,8]],
"delet": [3,0,[1,2,5],[4,7,8]],
"proven": [0],
"version-control": [3],
"br": [0],
"projectaccessglossarymenuitem": [0],
"see": [3,[5,8],0,1,2,7,6],
"search": [8,0,2,5,1,4,6,3,7],
"by": [0,3,8,2,7,1,[5,6]],
"whom": [2],
"segmentation.conf": [[0,3,7,8]],
"developp": [3],
"panel": [8,2],
"ca": [3],
"cc": [3],
"ce": [3],
"set": [2,3,8,0,5,7,1,4,6],
"contain": [[0,7],8,3,[1,2,6],5],
"incorrect": [7],
"balis": [6],
"column": [8,[0,5],2],
"freeli": [0],
"figur": [1,[4,6]],
"cs": [0],
"renam": [3,0],
"instantan": [3],
"partner": [3],
"somewhat": [0],
"project.sav": [3],
"apach": [3,8],
"adjustedscor": [2],
"font": [2,5,[0,4,6]],
"dd": [3],
"de": [[2,6]],
"justif": [3],
"featur": [[2,8],[1,3]],
"offic": [3,[0,1,8]],
"terminolog": [0,5],
"repositories.properti": [[0,3]],
"extern": [8,2,[0,5],6,[1,3,4,7]],
"forc": [0,[2,5,8]],
"do": [0,3,8,2,[1,5],6,7],
"f1": [[0,5,8]],
"f2": [[0,6],8],
"f3": [0,[5,6]],
"parti": [[1,3,6]],
"f5": [[0,1,5]],
"dz": [7],
"projectsavemenuitem": [0],
"editundomenuitem": [0],
"won": [3,4],
"rare": [3],
"contact": [6],
"ja-rv": [3],
"xmx6g": [3],
"autocompletertablefirstinrow": [0],
"digit": [0,3],
"which": [3,0,8,5,[1,2,7]],
"signific": [0],
"belazar": [2],
"en": [0,2],
"carri": [[5,8]],
"eu": [5],
"never": [0,[1,5,7]],
"she": [0],
"ex": [3],
"aggress": [5],
"adjust": [8,7],
"activ": [5,0,2,[6,8],7],
"first-class": [8],
"compat": [0,3,[2,8]],
"compar": [8,0],
"cursor": [6,5,0,8],
"prototype-bas": [8],
"indic": [6,[0,2,3,5]],
"insertcharslrm": [0],
"origin": [3,8,0,1,6,2],
"foo": [2],
"for": [0,3,8,2,5,1,7,6],
"exclud": [3,8,0],
"fr": [3,2],
"content": [0,[3,8],2,1,7,4,5,6],
"duckduckgo": [2],
"hover": [[2,5,6]],
"desktop": [3],
"decor": [1,0],
"applescript": [3],
"skill": [3],
"client": [3,[0,2,7]],
"json": [3],
"exclus": [8,3],
"gb": [3],
"class": [0,4],
"helplogmenuitem": [0],
"over": [5,[2,3,6,7]],
"six": [1],
"someth": [[2,3]],
"easy-to-us": [1],
"editoverwritetranslationmenuitem": [0],
"falso": [2],
"bound": [8],
"go": [0,1,[5,6],[3,4,8]],
"counter": [8],
"kept": [7,[0,3,8]],
"aeiou": [0],
"form": [0,8,3],
"publish": [3],
"setup": [3],
"restor": [5,3,2,6,[0,7,8]],
"avoid": [[0,3],[1,8]],
"foundat": [3],
"targetroot": [[2,8]],
"prompt": [3],
"subset": [[0,3]],
"assign": [0,5,8,7,[1,2,3,6]],
"typograph": [5],
"hh": [3],
"select": [5,8,0,2,[3,6],1,[4,7]],
"duser.languag": [3],
"viewmarkparagraphstartcheckboxmenuitem": [0],
"bin": [[2,3]],
"canadian": [2],
"degre": [0],
"easili": [1,[3,7]],
"apertium": [2],
"bit": [1],
"bis": [0],
"kaptain": [3],
"meta-inf": [3],
"clipboard": [5],
"repetit": [5,[0,8]],
"output": [3,2,4],
"veri": [1,8,[0,2],3],
"file-target-encod": [0],
"projectopenmenuitem": [0],
"autom": [3,[1,2,8]],
"corner": [6],
"four": [0,[3,5]],
"decim": [0],
"mainmenushortcuts.mac.properti": [0],
"context": [2,6,5,[0,1,3,7]],
"ordinari": [0],
"model": [[2,8]],
"id": [2,0,8],
"https": [3,2,0,7],
"join": [1],
"drag": [6,3],
"decis": [7],
"if": [3,8,5,0,2,1,7,6],
"french": [3,2,8],
"project_stats.txt": [7],
"non-ascii": [0],
"ocr": [8],
"oct": [2],
"projectaccesscurrenttargetdocumentmenuitem": [0],
"in": [0,8,3,5,2,6,7,1,4],
"lower": [0,5,[6,7,8]],
"termin": [3],
"ip": [3],
"index": [0,3],
"is": [0,3,8,[2,5,6],7,1,4],
"it": [0,3,8,7,6,5,2,1,4],
"vertic": [0],
"whitelist": [3],
"decid": [0,3],
"projectaccesstmmenuitem": [0],
"odf": [0,3],
"smoother": [[0,8]],
"contrast": [0],
"ja": [[2,3]],
"becam": [3],
"begin": [0,1],
"odt": [[0,8]],
"gotonexttranslatedmenuitem": [0],
"viewmarktranslatedsegmentscheckboxmenuitem": [0],
"paragraph": [0,[1,8],2,[5,6],[3,4]],
"charset": [0],
"viewer": [6],
"valu": [0,8,[2,3],5],
"librari": [0],
"standalon": [2],
"ilia": [3],
"toolscheckissuescurrentfilemenuitem": [0],
"libraries.txt": [0],
"learned_words.txt": [7],
"language—th": [0],
"world": [3],
"meantim": [3],
"uxxxx": [0],
"ftl": [[0,3]],
"side": [[3,6],[0,8]],
"break": [0,2,8],
"editselectfuzzy1menuitem": [0],
"themselv": [0,3,2],
"upgrad": [3,2,[4,8]],
"viewdisplaymodificationinfoallradiobuttonmenuitem": [0],
"tabular": [2],
"draw": [0],
"characters—known": [0],
"off": [5,[0,8]],
"comfort": [[3,8]],
"hide": [8,2],
"extran": [[0,2,8]],
"la": [2],
"report": [3,[0,1]],
"li": [0],
"autocompleterlistpagedown": [0],
"ll": [3],
"auto": [7,[0,5],[2,3,8]],
"receiv": [[2,6]],
"sign": [0,6],
"document.xx.docx": [0],
"lu": [0],
"editorskipnexttokenwithselect": [0],
"while": [0,[1,5,8],[3,6],2],
"second": [[2,5],[0,3,6,8]],
"that": [0,3,8,2,1,7,5,6,4],
"cycleswitchcasemenuitem": [0],
"download": [3,2,0,[5,7,8]],
"split": [8,0,[1,2,5,6]],
"mb": [3],
"oracl": [0],
"editortoggleovertyp": [0],
"than": [[0,2],[3,7],8,[1,6]],
"limit": [0,3,[1,7]],
"me": [3],
"non-translat": [0],
"picker": [3],
"mm": [3],
"gradlew": [3],
"administr": [3],
"entri": [8,0,5,6,2,[1,7]],
"applicaton": [0],
"level": [8,1],
"ms": [0],
"author": [8,[1,5]],
"toggl": [3,0,8],
"mt": [7,3],
"modif": [0,2,3,1,[5,6,8],7],
"my": [[0,3]],
"cascad": [2],
"plus": [0],
"disk": [8,3],
"viewmarklanguagecheckercheckboxmenuitem": [0],
"unseg": [1],
"updat": [[2,3],7,[0,8],[4,5]],
"ubiquit": [1],
"produc": [3,0,6],
"licenss": [0],
"no": [0,8,2,[3,5],[1,7]],
"code": [0,3,8,[2,4]],
"bridg": [3,[4,7]],
"underscor": [0],
"gotohistoryforwardmenuitem": [0],
"box": [8,0,2],
"switch": [0,8,[3,5],[2,6]],
"head": [0],
"dialog": [8,[0,2,5],[1,3],[4,7]],
"project_save.tmx.timestamp.bak": [7],
"total": [6,[5,8]],
"immut": [7],
"of": [0,8,3,2,1,6,7,5,4],
"bundl": [[2,3],0],
"possibl": [3,0,2,6,8],
"involv": [[3,8],7],
"applicationif": [5],
"ok": [8,5,1],
"dynam": [8],
"hear": [0],
"on": [3,0,8,2,5,6,1,4,7],
"keyboard": [0,[5,6]],
"macro": [8],
"purpos": [0,3,[2,8]],
"technic": [0,[3,5]],
"or": [0,8,3,[2,5],1,6,7,4],
"os": [0,6],
"src": [3],
"gigabyt": [3],
"control": [[0,5],3,1],
"encod": [0,[4,7,8]],
"no-team": [3],
"comprehens": [1],
"editinserttranslationmenuitem": [0],
"extrem": [0,[1,3]],
"lissens": [0],
"offici": [[0,4]],
"easier": [[0,3]],
"compliant": [3],
"pm": [[2,6]],
"po": [3,0,2,[6,7]],
"closest": [2],
"upper": [5,0],
"ssh": [3],
"environ": [3,0],
"qa": [8],
"autocompletertablefirst": [0],
"specialti": [3],
"necessari": [3,0,[1,2,8]],
"vari": [0,3],
"friend": [0],
"concurr": [5],
"recent": [5,3,[0,6]],
"they": [0,8,3,[6,7],[1,2],5],
"pinpoint": [8,3],
"streamlin": [0],
"github": [3],
"edit": [8,0,6,3,5,4,1,2],
"old": [3,1,[2,8]],
"subtract": [7],
"editselectfuzzy5menuitem": [0],
"them": [[0,3],1,8,5,[2,6],7],
"bilingu": [[7,8],3],
"then": [3,0,[1,8],[2,5,6,7]],
"kde": [3],
"accept": [8,[3,7],2],
"third-parti": [3,4],
"rc": [3],
"includ": [3,0,8,[6,7],2,1,5],
"readili": [3],
"adopt": [[0,1]],
"t0": [1],
"t1": [1],
"t2": [1],
"t3": [1],
"minut": [3,[1,2,5,7]],
"access": [0,[3,8],[2,5],1,7],
"currenc": [[0,8]],
"languag": [3,8,2,0,7,4,5,6,1],
"seen": [0],
"sa": [2],
"seem": [3],
"sc": [0],
"exept": [3],
"current": [5,8,0,[3,6],[2,7],1],
"sl": [3],
"optionsglossaryfuzzymatchingcheckboxmenuitem": [0],
"yandex.com": [2],
"so": [0,3,6,[7,8],[1,5]],
"caution": [[0,8]],
"email": [0],
"key": [0,8,2,6,3,5,4],
"apart": [8],
"communic": [6],
"intern": [3,[0,2,6]],
"starter": [0],
"onc": [[3,8],1,0,2],
"one": [0,8,5,3,6,[1,2],7],
"anymor": [3],
"msgid": [0],
"launch": [3,8,0,2,5,4],
"svn": [3,8,7],
"store": [0,3,[1,2,8],6,7,5,4],
"interv": [2,3,[5,7]],
"omegat-license.txt": [0],
"editoverwritesourcemenuitem": [0],
"stori": [0],
"closer": [2],
"confirm": [[0,2,5,7,8],3],
"omegat.autotext": [0],
"emerg": [3],
"kilobyt": [3],
"problemat": [1],
"enforc": [7,[1,2,3]],
"th": [5],
"bug": [[0,5]],
"remov": [8,0,3,7,[2,5,6]],
"tl": [3],
"tm": [7,3,2,8,5,[4,6],1],
"assist": [[1,6]],
"to": [3,0,8,2,5,1,6,7,4],
"v2": [3,2],
"v3": [3],
"typic": [[0,3]],
"editreplaceinprojectmenuitem": [0],
"but": [0,3,8,1,7,2,5],
"document.xx": [0],
"symbol": [0,6],
"editordeletenexttoken": [0],
"express": [0,8,2,4,3,1],
"multilingu": [0],
"viewmarkautopopulatedcheckboxmenuitem": [0],
"zero": [0,8],
"projectwikiimportmenuitem": [0],
"deactiv": [5],
"countri": [3,2],
"variant": [[0,3]],
"subsequ": [0],
"un": [3],
"up": [0,3,8,7,2,[1,4,6]],
"written": [3,[5,8]],
"us": [0],
"gotoprevioussegmentmenuitem": [0],
"partway": [[3,8]],
"newword": [8],
"usual": [3,5],
"this": [8,0,3,[2,5],7,[1,6],4],
"gotopreviousnotemenuitem": [0],
"editredomenuitem": [0],
"uilayout.xml": [[0,7]],
"verif": [2],
"substitut": [5],
"opt": [3,0],
"extract": [8,[0,2,7]],
"hint": [3],
"know": [0],
"projectʼ": [1],
"region": [0,[2,3]],
"support": [3,8,[0,1],7,2,[4,5]],
"vs": [2],
"sinc": [0,3,[1,8]],
"higher": [2,0],
"changed": [2],
"drop": [6,[3,7]],
"idea": [[3,8]],
"pure": [3],
"we": [0,[1,3]],
"unchang": [0],
"rearrang": [5],
"wavy-lin": [2],
"autocompleterlistup": [0],
"licenc": [0],
"repo_for_omegat_team_project": [3],
"choic": [8,[0,3]],
"normal": [0,[3,8],2,[5,7]],
"gradual": [[0,3,7]],
"adoptium.net": [3],
"slight": [[0,6]],
"previous": [0,5,[1,6],3,[7,8]],
"projectaccessexporttmmenuitem": [0],
"wide": [3],
"licens": [3,0,5],
"org": [3],
"distribut": [8,3,0,4],
"behav": [3,[0,4]],
"daunt": [0],
"example.email.org": [0],
"xx": [0],
"runtim": [3,0],
"sourc": [3,8,0,5,2,7,6,1,4],
"individu": [0,8,3],
"reach": [0,3],
"realiz": [0],
"none": [2,8,[0,5,7]],
"ressourc": [8],
"type": [3,8,6,[0,2],[1,4,7],5],
"beyond": [[0,1,6]],
"problem": [3,[5,6]],
"review": [1,[3,8],[0,4,6,7]],
"filenam": [0,8,[2,3,6]],
"optionsautocompletehistorypredictionmenuitem": [0],
"projectaccesssourcemenuitem": [0],
"roam": [0],
"between": [3,0,6,2,8,5,1],
"yy": [0],
"nbsp": [8],
"method": [3,[6,8]],
"contract": [0],
"scroll": [[0,2,6]],
"gotosegmentmenuitem": [0],
"come": [0,3,7,6,[1,8]],
"push": [3],
"exist": [3,[5,8],[0,1],[2,6,7]],
"readme_tr.txt": [3],
"penalti": [7,2],
"exact": [8,0,5,[1,7]],
"regist": [3,0,[2,5],[6,8]],
"initialcreationd": [2],
"references—in": [0],
"flag": [2,5,0,[1,6]],
"spacebar": [0],
"utf8": [0,[5,8]],
"helpaboutmenuitem": [0],
"copi": [3,8,6,[0,5],[2,7],1],
"out": [8,[0,5],[1,3]],
"weak": [8],
"induc": [3],
"get": [3,0],
"dark": [2],
"statist": [5,7,8,2,[0,3]],
"power": [0,8],
"place": [[0,8],3,[1,5,7],2],
"packag": [3,5],
"accur": [[7,8]],
"leav": [2,1,[0,5,6,7],3],
"regular": [0,3,8,2,[4,7],[1,6]],
"context_menu": [0],
"editsearchdictionarymenuitem": [0],
"restart": [[0,3],5,2],
"tag-valid": [3],
"ovr": [6],
"suggest": [6,2,0],
"alway": [0,[2,3,7],5,[1,8]],
"lead": [0],
"token": [0,[2,3,8],[6,7]],
"filter": [0,3,8,2,5,[1,4],7],
"expect": [0,3,[2,4]],
"help": [0,3,1,[4,5],8],
"site": [0,3,2,1],
"right-to-left": [[0,5],3],
"omegat.log": [0],
"carriag": [0],
"revis": [0],
"repositori": [3,7,[4,5,6,8],[0,2]],
"minimum": [7,[0,2]],
"autocompletertableright": [0],
"date": [2,[0,1,8],7],
"magic": [0],
"data": [3,2,5,8,7,1],
"lowercasemenuitem": [0],
"own": [3,0,8,[4,6]],
"wiki": [[3,7]],
"autocompleterconfirmwithoutclos": [0],
"separ": [0,[3,8],2,[1,6],[5,7]],
"breakabl": [1],
"tab": [0,5,[2,6]],
"filepath": [2],
"plain": [[0,3,8]],
"should": [0,2,3,8,[1,7],5],
"tag": [2,0,3,5,8,1,6,4],
"replac": [8,0,5,3,2,[1,6],[4,7]],
"agress": [0],
"versa": [8,3],
"tap": [0],
"like": [1,[3,8],[0,7],6],
"maxim": [6],
"onli": [0,8,2,3,5,[1,6],7],
"brace": [0],
"sent": [2],
"projectreloadmenuitem": [0],
"core": [7],
"person": [8,3],
"safe": [3],
"navig": [0,5,[1,6]],
"send": [3,2],
"here": [2,6,0,[3,8],7,[1,5]],
"note": [3,0,8,5,6,1,2,7,4],
"cross-platform": [3],
"line": [0,3,8,[2,6],[4,7]],
"noth": [[1,5]],
"link": [0,2,[6,7]],
"hero": [0],
"becom": [[1,7]],
"provis": [3],
"tbx": [0,2],
"wildcard": [[3,8]],
"can": [3,0,8,1,2,6,7,5],
"everybodi": [3],
"contributor": [[0,8]],
"git": [3,7],
"satisfi": [[3,8]],
"cat": [[0,1,3,8]],
"duser.countri": [3],
"provid": [3,0,2,8,6,5],
"realli": [5,8],
"xx-yy": [0],
"smooth": [0],
"reboot": [3],
"readm": [0],
"will": [8,[0,3],2,5,7,1,6],
"readi": [3],
"self-host": [3],
"match": [0,2,8,5,7,6,3,1,4],
"follow": [0,3,8,6,[1,2],[5,7]],
"categori": [0,4],
"intent": [0],
"fragment": [0,1],
"align.tmx": [3],
"file2": [3],
"arbitrari": [3],
"optionssetupfilefiltersmenuitem": [0],
"wild": [8,0],
"intend": [0,[3,7]]
};
