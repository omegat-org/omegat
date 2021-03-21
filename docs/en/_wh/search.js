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
 "Appendix A. Dictionaries",
 "Appendix B. Glossaries",
 "Appendix D. Regular expressions",
 "Appendix E. Shortcuts customization",
 "Appendix C. Spell checker",
 "Installing and running OmegaT",
 "How To...",
 "OmegaT 4.2 - User&#39;s Guide",
 "Menus",
 "Panes",
 "Project folder",
 "Windows and dialogs"
];
wh.search_wordMap= {
"altgraph": [3],
"cancel": [8],
"german": [11],
"half": [11,1],
"don\'t": [5,[10,11]],
"upload": [8],
"your": [5,[4,6,9],11,3,8,[0,1,10]],
"without": [11,5,[1,6,9]],
"these": [[6,11],[5,8],4],
"would": [11,5,[1,2,6]],
"xml": [11],
"ten": [8],
"info.plist": [5],
"i.e": [11,6,[1,4,5],[0,8,9,10]],
"xmx": [5],
"sometim": [11,[6,10]],
"serv": [[1,6,10,11]],
"thus": [11,6,[1,5,10]],
"reluct": [[2,7]],
"noun": [11,9],
"scratch": [11],
"castillian": [4],
"click": [11,5,9,8,4,6],
"fuzzi": [11,8,9,[6,10],7],
"insensit": [11],
"glu": [11],
"befor": [11,[6,8],5,10,[1,4,9]],
"util": [[0,5]],
"re-appli": [6],
"left": [11,6,8,[5,9,10]],
"tar.bz": [0],
"much": [2,[4,9]],
"besid": [[4,5,11]],
"object": [11],
"chapter": [[1,2,6,9,11]],
"yellow": [[8,9]],
"turn": [11,[6,8]],
"suffici": [11,[5,6,10]],
"dgoogle.api.key": [5],
"result": [11,[5,8],[1,2]],
"edittagnextmissedmenuitem": [3],
"same": [11,6,5,2,[1,8,9],[0,4,10]],
"work-flow": [9],
"checkbox": [5],
"after": [11,5,1,[2,3,6,9]],
"quiet": [5],
"connect": [[4,6]],
"hand": [11,[6,9]],
"xlsx": [11],
"address": [5,11],
"es_es.d": [4],
"tradit": [5],
"assembledist": [5],
"the": [11,6,5,8,9,1,10,2,4,3,0,7],
"blue": [[9,11]],
"projectimportmenuitem": [3],
"imag": [5],
"monolingu": [11],
"target.txt": [11],
"standard": [[1,4,11],[5,6,8,9]],
"correct": [11,6,[1,4,5],10,[0,8,9]],
"advic": [[5,6]],
"good": [11],
"deploy": [5],
"wish": [11,5,6,[4,8,10]],
"nameon": [11],
"wherev": [6],
"moodlephp": [5],
"currsegment.getsrctext": [11],
"optionsglossarytbxdisplaycontextcheckboxmenuitem": [3],
"implement": [5,11],
"advis": [11],
"export": [6,11,8,[1,3,10]],
"uncheck": [11],
"gotonextnotemenuitem": [3],
"area": [11],
"tar.gz": [5],
"gpl": [0],
"practic": [[5,9]],
"mis-transl": [11],
"european": [[6,11]],
"pay": [5],
"check": [11,8,4,6,5,0,9,[2,3,10]],
"transtip": [[3,9],1],
"list": [11,8,[4,7],[1,2,3,5,6]],
"onto": [5],
"xxxx9xxxx9xxxxxxxx9xxx99xxxxx9xx9xxxxxxxxxx": [5],
"vowel": [2],
"in-lin": [10],
"lisa": [1],
"azur": [5],
"fr-fr": [4],
"synchronis": [6,11],
"ensur": [[1,5,11]],
"fortun": [11],
"minim": [9],
"primari": [5],
"determin": [[1,4,11]],
"root": [6,11,[3,5,8]],
"combin": [5,[0,3,11]],
"webster": [0,[7,9]],
"menus": [5,11,8,7],
"hard": [[5,6,8]],
"realign": [11],
"object-ori": [11],
"cjk": [11],
"perform": [[5,8,11]],
"better": [11,1],
"with": [11,5,6,8,9,10,1,[2,3],0,4,7],
"pdf": [6,[7,8,11]],
"there": [[5,11],6,[1,4],[8,9],0],
"syntax": [11,3],
"well": [5,6,[8,10,11]],
"empti": [11,6,8,10,[3,5,9],1],
"hexadecim": [2],
"toolsshowstatisticsmatchesmenuitem": [3],
"viewdisplaymodificationinfononeradiobuttonmenuitem": [3],
"desir": [5,11,[6,9]],
"upper-cas": [2],
"variabl": [11],
"block": [2,[7,11]],
"tms": [10,6,11],
"per": [3,[5,8,11]],
"write": [11,[5,6]],
"tmx": [6,10,5,11,[8,9]],
"propos": [9],
"e.g": [11,6,1,5,[4,10]],
"order": [11,[8,9],6],
"repo_for_all_omegat_team_project": [6],
"period": [11,2,[5,6]],
"colleagu": [9],
"proceed": [6,[0,11]],
"nl-en": [6],
"understand": [6],
"integ": [11],
"intel": [5,7],
"fr-ca": [11],
"mainmenushortcuts.properti": [3],
"projectaccesswriteableglossarymenuitem": [3],
"even": [11,[6,8],[1,5,9]],
"aris": [6],
"gui": [5,10],
"cmd": [[6,11]],
"coach": [2],
"regexp": [5],
"subtitl": [5],
"sentencecasemenuitem": [3],
"gotohistorybackmenuitem": [3],
"save": [6,8,11,[3,5],1,10],
"increas": [11],
"matter": [5,10],
"restrict": [11,6],
"entiti": [11],
"instant": [5],
"project-save.tmx": [6],
"relaunch": [3],
"goto": [[3,9],7],
"uhhhh": [2],
"top": [11,9,2],
"too": [11,4],
"have": [11,5,6,1,[8,9],[4,10],[0,3]],
"powerpc": [5],
"optionssentsegmenuitem": [3],
"slowli": [5],
"mandatori": [11],
"avail": [5,11,[3,4],[6,8],[2,9,10]],
"product": [[6,11]],
"laid": [8],
"robust": [6],
"question": [11,6],
"littl": [2],
"optionsaccessconfigdirmenuitem": [3],
"charact": [2,11,1,[5,8],7,[3,9],6],
"test.html": [5],
"regard": [[6,11]],
"xxx": [10],
"instanc": [11,5,9,4,[0,1,10],[6,8]],
"smalltalk": [11],
"instal": [5,4,7,[0,8,11],9],
"minor": [11],
"arrow": [[9,11]],
"remot": [6,10,[5,8,11]],
"upon": [11,[4,6,9]],
"whenev": [6],
"earlier": [9],
"omegat.sourceforge.io": [5],
"pseudotranslatetmx": [5],
"whether": [11,5,[8,10]],
"unabl": [[6,11]],
"function": [[8,11],4,9,1],
"plenti": [6],
"pipe": [11],
"start-up": [5],
"quantifi": [2,7],
"evid": [9],
"comparison": [11],
"targetlanguagecod": [11],
"platform-specif": [11],
"non-team": [6],
"undock": [9,11],
"revert": [[5,6,11]],
"tri": [11,[5,6]],
"less": [5,[1,6,10]],
"tick": [11,4],
"translat": [11,6,9,8,10,5,3,7,1,[2,4]],
"uniqu": [11,9,3],
"bidirect": [8,[3,6]],
"were": [9],
"basic": [5],
"disabl": [[8,11]],
"websit": [10],
"footer": [11],
"fullwidth": [11],
"docs_devel": [5],
"extra": [5,[2,6,10]],
"identif": [11],
"command-lin": [5],
"unpack": [5,0],
"writeabl": [3],
"semicolon": [6],
"gnome": [5],
"encourag": [6],
"accord": [11,[1,9,10]],
"confidenti": [11],
"encyclopedia": [0],
"analyz": [2],
"optionstagvalidationmenuitem": [3],
"prev": [[0,1,2,3,4,5,6,8,9,10,11]],
"csv": [[1,5]],
"n.n_linux.tar.bz2": [5],
"pt_br": [4,5],
"concern": [[2,9,11]],
"a-z": [2],
"enhanc": [8],
"password": [11,6],
"state": [6,[9,10,11]],
"press": [11,9,8,1,[3,6],5],
"eventu": [4],
"dock": [5],
"onlin": [4],
"element": [[6,11]],
"want": [11,[3,6,8],[4,9]],
"dmicrosoft.api.client_secret": [5],
"each": [11,6,8,9],
"dropdown": [11],
"javascript": [11],
"mediawiki": [11,[3,8]],
"input": [6,11],
"creativ": [11],
"must": [6,11,5,3,[1,4]],
"unnecessarili": [6],
"suppli": [11],
"non-omegat": [11],
"cut": [9],
"ctrl": [3,11,9,6,8,1,[0,10]],
"document": [11,6,8,3,9,5,[1,2,7,10]],
"two": [11,5,6,4,[8,9],10],
"anyway": [5],
"pop": [11],
"found": [11,5,1,[6,9],2],
"larg": [4],
"attach": [[0,8,11]],
"anoth": [[5,9],[2,8,11]],
"advantag": [11,5],
"graphic": [5],
"creation": [11],
"resourc": [5,6,11],
"latest": [[5,6,10]],
"pend": [11],
"inhibit": [11],
"team": [6,[8,11],7,3,[5,10]],
"xx_yy": [[6,11]],
"side-by-sid": [5],
"docx": [[6,11],8],
"diagram": [11],
"txt": [6,1,[9,11]],
"charg": [5],
"googl": [5,11],
"opendocu": [11],
"quit": [8,[3,11],1],
"re-ent": [11],
"thing": [[6,10]],
"chart": [11],
"download.html": [5],
"fashion": [11],
"definit": [3,11],
"won\'t": [6],
"align": [11,8,5,7],
"up-to-d": [6],
"adjac": [9],
"endnot": [11],
"sourceforg": [3,5],
"trnsl": [5],
"structur": [10,11,1],
"goodi": [5],
"viewdisplaymodificationinfoselectedradiobuttonmenuitem": [3],
"index.html": [5],
"omegat.tmx": [6],
"entir": [11,10],
"has": [11,8,6,5,1,4],
"keyword": [11],
"given": [11,5,6,10,[0,9]],
"doubl": [5,2],
"actual": [[5,6,8]],
"last": [8,[3,11],[5,10]],
"editmultipledefault": [3],
"batch": [5],
"mozilla": [5],
"doubt": [10],
"editfindinprojectmenuitem": [3],
"develop": [2],
"reproduc": [6],
"diffrevers": [11],
"warn": [5,11,[6,9]],
"bookmark": [11],
"nomin": [1],
"attent": [5],
"technetwork": [5],
"inlin": [11],
"page": [8,11,[3,5,6]],
"full": [11,[3,5,6,9]],
"plural": [[1,11]],
"away": [[1,6]],
"becaus": [11,6,[9,10]],
"three-column": [1],
"miscellanea": [11],
"project.gettranslationinfo": [11],
"precis": [11],
"yes": [5],
"start": [5,11,[2,6,7],[3,10]],
"yet": [11,[2,8]],
"stylist": [11],
"pair": [11,6,9],
"wiser": [4],
"equal": [5,[8,11]],
"colour": [[8,11],3],
"n.n_windows.ex": [5],
"chang": [11,6,5,10,8,[1,3,9],4],
"multin": [6],
"anywher": [[9,10],5],
"pop-up": [9,[1,8]],
"short": [[2,4,11]],
"time": [11,6,2,[4,8,9],5],
"optionsalwaysconfirmquitcheckboxmenuitem": [3],
"tmxs": [6,[3,8,11]],
"program": [5,11,6,1],
"three": [6,10,[0,9]],
"cyan": [8],
"put": [10,[3,6,9,11]],
"enter": [11,8,5,1,[3,6],[2,9]],
"prioriti": [1,11,[7,8]],
"pale": [8],
"applic": [5,6,4,11,[1,8]],
"bidi": [6],
"projectteamnewmenuitem": [3],
"russian": [5],
"right-click": [11,5,[1,4,8]],
"preced": [2],
"directorate-gener": [8],
"non-seg": [11],
"memori": [6,11,5,10,9,8,[1,2,7]],
"submenu": [5],
"n.n_mac.zip": [5],
"no-match": [8],
"authent": [11],
"retransl": [6],
"quot": [2,1],
"recogn": [11,1,6],
"tabl": [2,3,11,[7,9],[6,8]],
"engin": [11,[6,8]],
"post-process": [11],
"log": [8,[3,5]],
"layer": [6],
"lot": [10],
"omegat.jnlp": [5],
"consult": [2],
"theme": [11],
"pane": [9,[1,11],8,7,[6,10]],
"undesir": [8],
"n.n_windows_without_jre.ex": [5],
"editor": [11,9,8,5,[1,6,7,10]],
"pseudotranslatetyp": [5],
"orphan": [11,9],
"clic": [11],
"prof": [11],
"cycl": [[3,8]],
"dmicrosoft.api.client_id": [5],
"fetch": [11],
"char": [11],
"config-fil": [5],
"small": [8],
"quick": [[5,11]],
"tell": [5,9],
"projectclosemenuitem": [3],
"unavail": [5],
"checker": [4,11,10,[1,2,7]],
"viewmarknonuniquesegmentscheckboxmenuitem": [3],
"workplac": [6],
"hit": [11,[2,5,6]],
"shown": [11,9,8,[2,6]],
"major": [11],
"bear": [11],
"titl": [[8,11],3],
"inspir": [11],
"day": [6],
"group": [6,[2,9,11]],
"obtain": [5,[0,11]],
"findinprojectreuselastwindow": [3],
"system-user-nam": [11],
"liter": [11],
"format": [6,11,1,8,0,[5,7,9,10]],
"tree": [10],
"particular": [6,[4,5,11]],
"readme.txt": [6,11],
"done": [6,11,9,[1,5]],
"languagetool": [11,8],
"console.println": [11],
"source.txt": [11],
"files.s": [11],
"histori": [8,3],
"exchang": [1],
"auto-sync": [11],
"achiev": [[5,11]],
"launcher": [5],
"request": [5,8],
"procedur": [6,[4,11]],
"pars": [[6,11]],
"part": [9,11,[4,5,6,8,10]],
"currseg": [11],
"their": [11,6,[2,5,8,9,10]],
"generat": [[10,11],6,8],
"novemb": [1],
"point": [11,[6,9]],
"general": [11,9,2,7],
"browser": [[5,8]],
"process": [11,5,6,[3,8]],
"project_files_show_on_load": [11],
"built": [9],
"attribut": [11],
"clear": [11,10],
"ltr": [6],
"optionsexttmxmenuitem": [3],
"downloaded_file.tar.gz": [5],
"third": [[1,9]],
"build": [5,[7,11]],
"mean": [11,[1,6]],
"further": [[10,11]],
"account": [5,11,[3,8]],
"marketplac": [5],
"snippet": [11],
"been": [8,11,[1,6],5,[9,10]],
"dhttp.proxyhost": [5],
"ident": [[6,10],[8,11],[3,5,9]],
"japanes": [11,5],
"entries.s": [11],
"addit": [11,[1,5],[0,2,6,8,9,10]],
"del": [[9,11]],
"alphabet": [11],
"gotonextuntranslatedmenuitem": [3],
"subdirectori": [6],
"targetlocal": [11],
"path": [5,6],
"trip": [6],
"overwritten": [[5,11]],
"abbrevi": [11],
"record": [11],
"strict": [6],
"you": [11,5,6,9,4,8,10,1,3,2,0],
"jump": [9],
"happen": [10,11],
"contient": [1],
"pass": [5],
"allsegments.tmx": [5],
"past": [8,[6,9]],
"impact": [11],
"percentag": [9,11,10],
"especi": [11],
"cours": [[1,4,10],[5,6,9,11]],
"configur": [11,5,[3,8]],
"left-click": [11],
"helpcontentsmenuitem": [3],
"domain": [11],
"omegat-org": [6],
"descript": [[3,11],[5,6]],
"preserv": [11],
"projectaccessdictionarymenuitem": [3],
"sentenc": [11,[2,3,6,8]],
"optionsworkflowmenuitem": [3],
"pursu": [11],
"consecut": [11],
"how": [11,6,[0,7],1,[8,9,10]],
"releas": [[6,8],3],
"term": [1,[9,11],8,3],
"backslash": [[2,5]],
"sparc": [5],
"mind": [11],
"right": [9,5,[6,11],[1,8]],
"duden": [9],
"insid": [[1,5]],
"stage": [[6,10]],
"under": [5,6,[0,11]],
"spotlight": [5],
"did": [11],
"imper": [11],
"reserv": [[4,5]],
"dir": [5],
"down": [11,6],
"div": [11],
"subdir": [6],
"trail": [11],
"later": [11,[5,10],[6,8,9]],
"forgotten": [0],
"bracket": [11],
"viewfilelistmenuitem": [3],
"info": [3,[5,8]],
"brows": [[5,11],9],
"non-break": [8,3],
"test": [5,[2,6]],
"omegat": [5,6,11,8,[3,7,10],4,1,[0,9],2],
"forward-backward": [11],
"rule-bas": [11],
"take": [11,[4,6],[3,8,10]],
"month": [6,5],
"final": [5],
"file-source-encod": [11],
"some": [6,11,[5,8],1,[0,4,9,10]],
"untranslat": [11],
"virtual": [11],
"blank": [11],
"rather": [[1,4,5,6,9]],
"session": [11,[5,10]],
"console-align": [5],
"back": [6,9,8,[3,11]],
"ms-dos": [5],
"projectopenrecentmenuitem": [3],
"miss": [8,[1,3,9],2],
"load": [11,6,[5,8],1],
"just": [6,[1,8],[4,10,11]],
"inset": [1],
"custom": [[3,11],6,7,[2,8]],
"editexportselectionmenuitem": [3],
"length": [9],
"und": [4],
"project_save.tmx.temporari": [6],
"une": [1],
"home": [6,5,[0,1,2,3,4,8,9,10,11]],
"condit": [6],
"glyph": [8],
"print": [[9,11]],
"although": [11],
"projectaccesstargetmenuitem": [3],
"inherit": [6],
"editoverwritemachinetranslationmenuitem": [3],
"relat": [6,[9,11]],
"ingreek": [2],
"es_es.aff": [4],
"convers": [6],
"visibl": [10],
"construct": [2],
"ignor": [11,[8,9],[3,4,5,10]],
"convert": [[6,11],8],
"elsewher": [4],
"attempt": [11,[1,5,6]],
"soon": [8],
"influenc": [6],
"pojavnem": [1],
"projectexitmenuitem": [3],
"aligndir": [5],
"supplier": [11],
"system-host-nam": [11],
"action": [8,3,[0,5]],
"lock": [[5,9]],
"text": [11,6,8,9,10,1,[2,4],[5,7],3],
"en-to-fr": [5],
"editregisteruntranslatedmenuitem": [3],
"creat": [6,11,8,5,1,3,10,[4,9],7],
"init": [6],
"python": [11],
"es_mx.dic": [4],
"made": [5,[6,8]],
"infix": [6],
"block-level": [11],
"bell": [2],
"manag": [6,4,7],
"maco": [5,7],
"field": [11,9,[5,8],[1,4],6],
"tarbal": [0],
"singl": [11,[2,5]],
"invalid": [5,6],
"doc": [6],
"doe": [6,[1,8,11],5,[0,4,9]],
"status": [9,8,11,[5,7,10]],
"mis-spel": [4],
"server": [6,11,5,10],
"paramet": [[5,6],11,10],
"dot": [8],
"skip": [11],
"overrid": [[6,11]],
"mac": [3,[5,6]],
"mention": [5],
"file": [11,6,5,8,1,10,4,9,3,0,7],
"known": [[6,9]],
"man": [5],
"stand": [5,11],
"can\'t": [5],
"map": [6,11],
"meni": [1],
"may": [11,6,5,9,[1,10],4,[2,8]],
"within": [[5,6,9,10,11],[1,8],3],
"case-insensit": [2],
"forward": [8,3],
"could": [11],
"menu": [3,7,11,[8,9],5,1,[4,6]],
"url": [6,11,[4,5,8]],
"megabyt": [5],
"uppercasemenuitem": [3],
"viewmarkuntranslatedsegmentscheckboxmenuitem": [3],
"explan": [5],
"a-za-z": [2,11],
"probabl": [[1,9]],
"relev": [6,11,8,[3,5]],
"return": [[5,8,9,11]],
"use": [11,[5,6],4,8,9,[1,3],7,[2,10],0],
"subject": [[6,10],11],
"feel": [[5,6,11]],
"main": [9,11,[3,6],7],
"newlin": [[2,11]],
"radio": [11],
"omegat.jar": [5,[6,11]],
"source-pattern": [5],
"conveni": [5,6],
"omegat.app": [5],
"usr": [5],
"find": [5,1,[2,11],[3,6]],
"host": [[5,11]],
"backward": [8],
"credit": [8],
"alter": [5],
"unmodifi": [11],
"clutter": [11],
"utf": [1],
"occur": [[5,11],9],
"sort": [11,[8,9,10]],
"fill": [6],
"feed": [2],
"servic": [5,11,8],
"omegat-specif": [10],
"specialist": [9],
"background": [[8,10],9],
"rewrit": [11],
"true": [5],
"header": [11,8],
"dsl": [0],
"present": [11,10,[0,5]],
"mid-transl": [11],
"groovi": [11],
"pre-defin": [[5,11]],
"multi-paradigm": [11],
"cornerston": [1],
"best": [11,[9,10]],
"n.n_windows_without_jre.zip": [5],
"med": [8],
"en.wikipedia.org": [9],
"execut": [[5,11],8],
"hour": [6],
"kmenueditor": [5],
"dtd": [5],
"repeat": [[9,11]],
"make": [11,[5,10],6,4,9],
"capit": [11,5],
"source-target": [0],
"abov": [6,11,5,9,[1,2,4,10],[0,8]],
"sentence-level": [11],
"projectcompilemenuitem": [3],
"console-transl": [5],
"master": [6,11],
"kmenuedit": [5],
"gotonextuniquemenuitem": [3],
"form-fe": [2],
"conform": [11],
"underlin": [[1,4]],
"xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx": [5],
"writer": [6],
"wordart": [11],
"merg": [[6,11]],
"dalloway": [11],
"rubi": [11],
"optionsviewoptionsmenuitem": [3],
"inform": [5,11,6,8,[0,1,2]],
"depend": [[5,11],[8,9],6],
"about": [6,9,[3,5,8,10,11]],
"commit": [6,8],
"targetlocalelcid": [11],
"danger": [6],
"project_stats_match.txt": [10],
"tab-separ": [1],
"reflect": [5,[8,10]],
"dvd": [6],
"flexibl": [11],
"meaning": [11],
"xmx2048m": [5],
"meniju": [1],
"immedi": [8],
"software-rel": [11],
"xxxxxxxxxxxxxxxx.xxxxxxxxxxxxxxxx.xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx": [5],
"distinguish": [10],
"benefit": [5],
"user.languag": [5],
"regex": [2,7],
"highest": [9],
"meta": [3,2],
"keystrok": [3,11],
"encount": [11],
"declar": [11],
"except": [11,2,6],
"krunner": [5],
"predomin": [11],
"libreoffic": [4,[6,11]],
"qualiti": [6,8],
"fact": [6],
"scan": [[6,11]],
"global": [11],
"long": [11,[5,8]],
"into": [11,6,8,9,10,[0,3,5]],
"defin": [11,8,[9,10],[1,2,3,4,6]],
"free": [5,[0,4,7,8,11]],
"mix": [6,1],
"untick": [11],
"plug-in": [11],
"bother": [1],
"though": [11],
"stay": [10,11],
"viewdisplaysegmentsourcecheckboxmenuitem": [3],
"appear": [11,5,3,[1,4,6,8,9,10]],
"editregisteremptymenuitem": [3],
"non-uniqu": [11,[8,9],3],
"ibm": [5],
"reliabl": [[1,10]],
"progress": [[9,10,11],6],
"oper": [11,5,[6,8],[2,7,9,10]],
"mani": [11,6,8],
"open": [11,8,6,5,9,[1,3],[4,10]],
"pre-transl": [6],
"www.oracle.com": [5],
"treat": [11,9],
"parsewis": [11],
"project": [6,11,8,5,10,3,9,[1,7],4,0],
"xmx1024m": [5],
"whatev": [4],
"sever": [11,[5,6,8,9],10],
"evalu": [11],
"enclos": [1],
"penalty-xxx": [10],
"gotonextsegmentmenuitem": [3],
"invert": [11],
"greedi": [2,7],
"nnn.nnn.nnn.nnn": [5],
"look": [11,[4,5,6]],
"repres": [11],
"abort": [5],
"left-to-right": [6],
"guid": [7,5],
"idx": [0],
"unrestrict": [5],
"internet": [11,4],
"conflict": [3],
"allow": [11,8,5,[1,9],6],
"qui": [1],
"detect": [[1,5,8]],
"proper": [[1,5,11],[6,8,9,10]],
"rule": [11,[2,6],[5,10]],
"everi": [8,[6,11]],
"speed": [6],
"outsid": [9],
"common": [6,1,[7,8,11]],
"projectaccesscurrentsourcedocumentmenuitem": [3],
"appli": [11,8,5,[6,9,10]],
"linux": [5,[1,2,7,9]],
"middl": [[2,6]],
"unformat": [11],
"again": [11,[5,8,9]],
"file.txt": [6],
"writabl": [1,8],
"es-mx": [4],
"layout": [[9,11]],
"non-word": [2],
"popup": [11],
"ifo": [0],
"comment": [11,1,9,[3,5,7,8]],
"step": [6,11,10,[8,9]],
"mark": [8,3,11,9],
"base": [11,4,[0,1,5,6,8]],
"stem": [[9,11],3],
"octal": [2],
"disconnect": [6],
"prefix": [11,10],
"xx.docx": [11],
"whole": [[8,11],5],
"herebi": [10],
"consist": [[8,9,11]],
"loss": [6,7],
"critic": [11],
"lost": [6],
"optionsautocompleteautotextmenuitem": [3],
"zip": [5],
"still": [6,[9,11]],
"compress": [[10,11]],
"work": [5,6,11,[4,8],[9,10]],
"lose": [[6,9]],
"hesit": [6],
"gedit": [1],
"suitabl": [[4,5],6],
"fail": [[5,6]],
"itself": [2],
"concis": [0],
"customer-id": [5],
"structure-level": [11],
"among": [[8,9,10,11]],
"word": [11,8,[1,9],[2,6],[4,5],[7,10]],
"term.tilde.com": [11],
"verifi": [1],
"auto-propag": [11],
"requir": [6,[5,9],[0,1,2,4,8,11]],
"foreign": [11],
"across": [11],
"viewmarknotedsegmentscheckboxmenuitem": [3],
"non-digit": [2],
"non-vis": [11],
"steer": [11],
"event": [3],
"simplest": [5],
"single-word": [1],
"lingvo": [0],
"gotomatchsourceseg": [3],
"abstract": [7],
"appropri": [[6,11]],
"nice": [2],
"opinion": [9],
"mrs": [11],
"optionssaveoptionsmenuitem": [3],
"excel": [11],
"comma": [1,[2,11]],
"agenc": [9],
"runn": [11],
"cannot": [[6,11]],
"averag": [11],
"stardict": [0],
"first": [11,8,5,[1,6,9,10],[2,3,4]],
"omegat.l4j.ini": [5],
"span": [11],
"quotat": [[2,7]],
"prefer": [11,8,5,[6,9],[3,7]],
"perfect": [6],
"mistransl": [11],
"threshold": [11],
"float": [11],
"space": [11,2,8,[1,3]],
"pt_pt.aff": [4],
"hassl": [6],
"spite": [11],
"simpl": [[1,2],11,6],
"open-sourc": [11],
"from": [11,5,[6,9],10,3,4,8,1,7,2],
"html": [11,5],
"portugues": [[4,5]],
"spell": [4,[7,11],[8,10],[1,2,3]],
"thunderbird": [4,11],
"editselectfuzzy3menuitem": [3],
"bottom": [11,9,[3,8]],
"artund": [4],
"templat": [11],
"fals": [11,5],
"project.projectfil": [11],
"uncondit": [10],
"impli": [11],
"repair": [6],
"recip": [6],
"jres": [5],
"www.ibm.com": [5],
"frequent": [6,5],
"interact": [[2,11]],
"error": [6,5,8],
"platform": [5],
"network": [[5,6]],
"trace": [11],
"shortcut": [3,5,[8,11],7,[1,2,6]],
"track": [9],
"pt_br.aff": [4],
"tmx2sourc": [6],
"lookup": [8,11],
"ini": [5],
"instead": [[1,3,5,6,8,11]],
"improv": [11],
"command": [5,11,8,6,[3,9],7],
"project-specif": [11,10],
"n.n_without_jr": [5],
"pollut": [6],
"trade": [11,9],
"round": [6],
"dhttp.proxyport": [5],
"negat": [2],
"viewmarkbidicheckboxmenuitem": [3],
"subrip": [5],
"year": [6],
"via": [11,[1,6]],
"dutch": [6],
"describ": [11,[5,6],9],
"score": [11],
"permiss": [5],
"double-click": [5,[8,9,11]],
"visual": [8],
"volum": [11],
"near": [1],
"approxim": [[6,11]],
"communiti": [6],
"agreement": [5],
"appendix": [[1,2,4],[0,3],6],
"instruct": [[5,11],4,7],
"raw": [6],
"version": [5,6,8,[2,4,10]],
"folder": [5,[6,11],10,8,1,4,[3,9],0,7],
"stop": [11,5],
"handl": [11,6,1],
"detail": [11,[5,6,8],9],
"retriev": [5],
"aaa": [2],
"contemporari": [0],
"solari": [5],
"projecteditmenuitem": [3],
"least": [[10,11]],
"manual": [11,[6,8],[1,4],3],
"britannica": [0],
"machin": [11,8,9,5,[3,7]],
"wikipedia": [8],
"behavior": [5,[3,8,9,10]],
"close": [11,6,8,[3,9]],
"vocabulari": [4,1],
"abc": [2],
"rcs": [6],
"abl": [11,5,[4,9]],
"ancillari": [10,9],
"textual": [6,11],
"iceni": [6],
"uppercas": [2],
"iso": [1],
"specif": [11,[5,6,8],[2,9]],
"red": [11,10],
"aggreg": [11],
"act": [10],
"post": [6],
"glossary.txt": [[1,6]],
"dsun.java2d.noddraw": [5],
"finish": [[6,8,9,11]],
"placehold": [11],
"add": [11,6,5,3,[9,10],[1,4,8]],
"initi": [[5,10],11,6],
"chines": [6,[5,11]],
"need": [6,[5,11],10,[2,4],1],
"equival": [[0,5,8,9]],
"often": [11],
"x0b": [2],
"gather": [6],
"respect": [[1,6]],
"canada": [5],
"port": [5],
"pre-configur": [11],
"altern": [11,8,9,6,[3,5]],
"http": [6,5,11],
"optionsautocompleteshowautomaticallyitem": [3],
"emb": [6],
"trust": [11],
"larouss": [9],
"tcl-base": [11],
"untar": [[0,5]],
"interfer": [5,[8,11]],
"consequ": [11,[4,5]],
"prevent": [6,[7,11]],
"undo": [[3,8]],
"filters.conf": [5],
"softwar": [[5,6,11]],
"projectsinglecompilemenuitem": [3],
"end": [[2,11],[5,8,9,10]],
"footnot": [11],
"modifi": [11,[3,5,6],8,[9,10]],
"myfil": [6],
"anyth": [3],
"label": [8],
"howev": [6,11,5,4],
"special": [11,6,9],
"togeth": [11],
"numer": [6],
"clone": [6],
"targetlanguag": [11],
"directori": [8,6],
"backup": [6,10],
"copyright": [8],
"sensit": [11],
"properti": [11,[6,8],[1,4],[0,3,5,7,10]],
"system-os-nam": [11],
"occurr": [11,[4,9]],
"editselectfuzzyprevmenuitem": [3],
"optionstabadvancecheckboxmenuitem": [3],
"number": [11,9,8,[5,6],[2,3,10]],
"defect": [6],
"identifi": [11,8],
"specifi": [[5,11],[3,6],[4,10]],
"heapwis": [11],
"simpledateformat": [11],
"optionsviewoptionsmenuloginitem": [3],
"algorithm": [[3,8,11]],
"newli": [1],
"troubleshoot": [6],
"similar": [11,9,[5,6,10]],
"tar.bz2": [0],
"invit": [6],
"forth": [6],
"bundle.properti": [6],
"script": [11,8,5,7],
"exit": [[6,8]],
"system": [5,11,6,4,8,[1,3,7,10]],
"spellcheck": [4,[7,11]],
"x64": [5],
"characterist": [11],
"non-greedi": [2,7],
"aid": [7],
"issu": [8],
"partial": [[9,11]],
"other": [6,5,11,9,[8,10],[0,1,4,7]],
"keyev": [3],
"against": [11,[1,4,6]],
"retain": [[5,10,11]],
"isn\'t": [2],
"login": [11,3],
"cell": [11],
"local": [6,[5,8,11]],
"valid": [11,5,[3,6,8],9],
"assur": [8],
"pictur": [11],
"interfac": [5,6,[9,11]],
"assum": [11],
"locat": [5,11,[1,6],8,4],
"yield": [2],
"speak": [[1,11]],
"share": [6],
"optionsteammenuitem": [3],
"gzip": [10],
"high-qual": [10],
"repo_for_all_omegat_team_project_sourc": [6],
"duplic": [11],
"notif": [11],
"esc": [11],
"x86": [5],
"exampl": [11,6,2,5,9,[1,4,7,8],[0,3]],
"logic": [[2,7,11]],
"nostemscor": [11],
"es_mx.aff": [4],
"round-trip": [6],
"screen": [5],
"mexican": [4],
"correspond": [11,8,[4,9],[2,5,10]],
"console-createpseudotranslatetmx": [5],
"mode": [5,6,11,9],
"rightclick": [1],
"etc": [11,[5,6,9],[0,1,2,10]],
"longman": [0],
"fuzzyflag": [11],
"toolsshowstatisticsstandardmenuitem": [3],
"all": [11,6,[5,8],9,10,2,3,4],
"precaut": [6],
"merriam": [0,[7,9]],
"new": [11,6,8,5,3,1,4,2],
"escap": [5,2],
"read": [6,[5,10,11]],
"below": [5,6,[2,9],[0,3,4,8,11]],
"alt": [[3,5,11]],
"touch": [1],
"choos": [11,[4,5,6]],
"rememb": [[8,11]],
"projectname-omegat.tmx": [6],
"real": [9],
"tool": [11,[6,7],[2,3,8],10,[5,9]],
"unit": [11,10],
"alreadi": [11,5,6,[4,8,9,10],[1,3]],
"invari": [11],
"therefor": [5],
"bodi": [4],
"collect": [11,9,[1,2,10]],
"redo": [[3,8,9]],
"media": [6],
"around": [11],
"simpler": [4],
"per-project": [11],
"n.n_without_jre.zip": [5],
"reload": [6,[8,11],1,3],
"calcul": [11,9],
"and": [11,6,5,8,9,1,[4,10],[2,3],7,0],
"synchron": [[5,6]],
"predict": [8],
"row": [8,11],
"ani": [11,5,6,[1,2,10],9,8,3],
"render": [11],
"magento": [5],
"ant": [[6,11]],
"korean": [11],
"strategi": [11],
"dedic": [6],
"boundari": [2,7],
"offlin": [6,5],
"u00a": [11],
"unnecessari": [11],
"until": [[2,5,6,11]],
"helplastchangesmenuitem": [3],
"localis": [6],
"omegat.ex": [5],
"reason": [[4,5,8,11]],
"maintain": [10],
"shift": [3,11,[6,8],1],
"sourcetext": [11],
"compon": [11],
"java": [5,11,3,2,[6,7]],
"exe": [5],
"english": [6,2,[0,5]],
"jar": [5,6],
"mistak": [11],
"api": [5,11],
"lang2": [6],
"lang1": [6],
"ambival": [11],
"editselectfuzzy2menuitem": [3],
"project_save.tmx": [6,10,11],
"dictionari": [4,0,7,[8,11],9,10,6,[1,3]],
"remain": [11,[5,10]],
"slow": [[6,11]],
"myglossary.tbx": [1],
"letter": [2,8,11,6],
"recommend": [[1,11]],
"editselectfuzzynextmenuitem": [3],
"devis": [2],
"read.m": [11],
"default": [11,3,[6,8],[5,9],10,[1,2]],
"are": [11,6,5,8,1,9,10,3,[0,4],2],
"taken": [8,[3,11]],
"readme.bak": [6],
"spreadsheet": [1],
"where": [11,6,8,5,[3,9],4],
"drop-down": [[4,11]],
"popular": [11],
"timestamp": [11],
"broken": [11],
"art": [4],
"vice": [11,6],
"projectaccessrootmenuitem": [3],
"dyandex.api.key": [5],
"holder": [6],
"rtl": [6],
"fulli": [6],
"call": [11,5,[4,10]],
"jdk": [5],
"such": [6,11,[0,5,10]],
"plugin": [11],
"essenti": [4],
"ask": [6,[5,9,11]],
"through": [[2,11],9,[5,8]],
"oppos": [11],
"toolsshowstatisticsmatchesperfilemenuitem": [3],
"strength": [11],
"editinsertsourcemenuitem": [3],
"run": [5,11,8,[6,7]],
"viterbi": [11],
"microsoft": [11,[5,6],[1,9]],
"coher": [11],
"projectnewmenuitem": [3],
"technolog": [5],
"either": [11,6,[2,4,8]],
"view": [11,3,[6,7],8,[5,9,10]],
"white": [11,[1,8]],
"optionstranstipsenablemenuitem": [3],
"segment": [11,8,9,3,[6,10],[1,5],2],
"titlecasemenuitem": [3],
"huge": [[4,11]],
"those": [11,[6,10],[2,8]],
"glossari": [1,11,9,3,7,8,[6,10],[0,4]],
"editcreateglossaryentrymenuitem": [3],
"ignored_words.txt": [10],
"configuration.properti": [5],
"might": [[5,6,11]],
"github.com": [6],
"ital": [11],
"dure": [11,[5,6],[1,10]],
"bold": [11,9,1],
"longer": [11,5],
"overlap": [9],
"privat": [[5,11]],
"name": [11,5,[6,9],[4,10],1,[0,3,8]],
"physic": [4],
"next": [8,11,3,9,[1,5,6],[0,2,4,7,10]],
"string": [11,[6,8],[1,5],[0,9]],
"import": [6,11,[9,10],[5,8]],
"color": [9,11],
"hidden": [[10,11]],
"meta-tag": [11],
"show": [11,[1,5,8,9],[0,3,10]],
"disappear": [4],
"non": [1],
"button": [11,5,4],
"nor": [5],
"language-countri": [11],
"comput": [5,11,7],
"not": [11,6,5,1,8,[2,9],4,10,3],
"now": [[3,6,11]],
"enabl": [11,5,[2,3,4,6]],
"greek": [2],
"green": [9,8],
"associ": [8,5],
"was": [6,8,[9,11]],
"subfold": [10,[6,11],5,[0,4]],
"selection.txt": [11,8],
"way": [[6,11],5,[1,4,9,10]],
"target": [11,8,6,4,[1,9],10,5,3,7],
"grey": [8,11],
"xhtml": [11],
"what": [11,5],
"knowledg": [6],
"finder.xml": [11],
"refer": [6,[9,11],[2,5]],
"window": [11,5,8,9,[4,7],[0,1,2,3,6,10]],
"call-out": [8],
"config-dir": [5],
"leader": [6],
"criteria": [11],
"disable-project-lock": [5],
"displac": [8],
"omegat.pref": [11],
"when": [11,6,5,[8,9],1,10,2],
"sequenc": [11],
"personalis": [11],
"auto-popul": [[8,11],3],
"carriage-return": [2],
"far": [9],
"electron": [9],
"catch": [6],
"case": [6,11,3,8,5,[9,10],[1,2]],
"give": [11,[5,8],9],
"item": [3,5,[1,11],[2,6,8]],
"multipl": [[7,9],[1,5,11]],
"howto": [6],
"violet": [8],
"matcher": [[2,7]],
"lowest": [9],
"pt_pt.dic": [4],
"explicit": [11],
"targettext": [11],
"consid": [11,6],
"futur": [6],
"slide": [11],
"everyth": [[5,6]],
"prevail": [10],
"reset": [11],
"style": [6,11],
"level1": [6],
"card": [11],
"level2": [6],
"care": [[6,11]],
"widget": [[7,9]],
"orang": [8],
"portion": [8],
"pattern": [11,[2,6]],
"direct": [5,11,6,[8,10]],
"aaabbb": [2],
"caus": [5,11,[1,6,8]],
"mechan": [8],
"web": [5,[6,7]],
"edittagpaintermenuitem": [3],
"en-us_de_project": [6],
"you\'r": [6],
"seldom": [11],
"older": [6],
"protect": [11],
"optionscolorsselectionmenuitem": [3],
"nth": [8],
"editselectfuzzy4menuitem": [3],
"editregisteridenticalmenuitem": [3],
"more": [11,2,5,10,9,[1,6],[3,8]],
"display": [11,8,6,9,1,3,5,10],
"unicod": [[1,2,7]],
"viewmarknbspcheckboxmenuitem": [3],
"non-breack": [11],
"usag": [1,[5,7]],
"width": [11],
"pt_br.dic": [4],
"left-hand": [11],
"advanc": [[8,11],[2,3,5]],
"certain": [11,6],
"halfwidth": [11],
"unabridg": [0],
"overwrit": [[8,10]],
"fed": [5],
"whitespac": [11,[2,3,8]],
"credenti": [11],
"section": [[5,6]],
"auto-complet": [11,3,8],
"simpli": [5,[1,4,9,11]],
"optionsglossaryexactmatchcheckboxmenuitem": [3],
"msgstr": [11],
"dict": [0],
"few": [[5,6,11]],
"untransl": [11,8,[3,9],[6,10]],
"resiz": [[9,11]],
"both": [6,11,5,[1,9]],
"most": [9,[5,6,11],3,10],
"delimit": [[1,8]],
"nnnn": [9,5],
"project_save.tmx.yearmmddhhnn.bak": [6],
"omegat.project": [6,5,10,[7,9,11]],
"phrase": [11],
"referr": [6],
"marker": [9],
"keep": [11,10,[5,9]],
"effect": [11,9],
"excludedfold": [6],
"whi": [11],
"targetcountrycod": [11],
"job": [9],
"fallback": [8],
"omit": [11],
"who": [[2,11]],
"option": [11,8,5,9,3,[4,6,7],10,2],
"webstart": [5],
"insert": [11,8,3,9,[1,10]],
"continu": [11],
"myproject": [6],
"resid": [[5,6,10]],
"highlight": [9,[1,8,11]],
"along": [11],
"zh_cn.tmx": [6],
"arrang": [11],
"sheet": [11],
"messag": [5,6,9],
"prerequisit": [5],
"amount": [5,11],
"move": [[8,11],9],
"also": [5,11,6,9,1,[4,8],[3,7]],
"say": [6],
"enough": [6],
"differ": [11,6,9,8,5,[4,10]],
"conson": [2],
"consol": [5],
"mous": [[8,9]],
"yandex": [5],
"various": [11],
"archiv": [[0,5,6]],
"latter": [6],
"repo_for_omegat_team_project.git": [6],
"approv": [1],
"user": [5,11,7,[8,9],6,3,2],
"confus": [11],
"a123456789b123456789c123456789d12345678": [5],
"viewmarkwhitespacecheckboxmenuitem": [3],
"proxi": [5,11,3],
"extens": [11,[0,1],6,[9,10]],
"bad": [11],
"complet": [[6,11],[5,8]],
"bak": [6,10],
"offer": [[9,11],[1,5,8]],
"fit": [9],
"bar": [9,5,[7,11]],
"fix": [[1,6,8]],
"built-in": [[4,11]],
"bat": [5],
"complex": [2],
"draft": [11],
"jre": [5],
"doesn\'t": [6,11],
"rang": [[2,4,11]],
"optionsfontselectionmenuitem": [3],
"posit": [11,8,9,[1,6]],
"ad": [6,[1,5],10,11,8],
"sure": [5,11,[4,6]],
"reus": [6,7],
"diff": [11],
"automat": [11,[5,8],6,1,4,[3,9]],
"an": [6,11,5,2,1,4,[8,9],10],
"editmultiplealtern": [3],
"secur": [11,5],
"former": [6],
"as": [11,6,5,8,9,1,2,10,4,[0,3]],
"git.code.sf.net": [5],
"at": [11,5,[8,9],10,6,2,[1,3]],
"predefin": [[2,7,11]],
"constitut": [11],
"hierarchi": [10],
"drive": [[5,6]],
"xml-base": [1],
"strong": [6],
"deal": [[6,11]],
"be": [11,6,5,1,8,9,10,3,4,[0,2]],
"scheme": [11],
"freebsd": [2],
"top-level": [10],
"icon": [5,8],
"delet": [11,6,[5,8,9,10]],
"filters.xml": [6,[10,11]],
"dead": [6],
"version-control": [6],
"br": [11,5],
"projectaccessglossarymenuitem": [3],
"search": [11,8,2,3,[0,6,7]],
"see": [11,6,[2,9],[5,8,10],[1,4]],
"by": [11,6,8,5,9,[2,10],3,1,4,0],
"segmentation.conf": [6,[5,10,11]],
"panel": [5,11],
"ca": [5],
"developerwork": [5],
"cd": [5,6],
"ce": [5],
"öäüqwß": [11],
"contain": [11,10,6,5,[1,9],8,[0,3,4,7]],
"set": [11,5,6,8,1,4,[3,7,10]],
"column": [11,[1,8],9],
"cn": [5],
"familiar": [[6,11]],
"optionsrestoreguimenuitem": [3],
"figur": [4,1,[0,2,5,7,9]],
"renam": [6,11,4],
"cx": [2],
"somewhat": [[5,6]],
"apach": [4,[6,11]],
"adjustedscor": [11],
"font": [8,11,3],
"dd": [6],
"justif": [6],
"featur": [11,[0,6]],
"terminolog": [1,11,8,[6,9]],
"offic": [11],
"extern": [11,8,[3,6]],
"do": [11,5,[1,4,9],[3,6]],
"f1": [3],
"f2": [9,[5,11]],
"f3": [[3,8]],
"dr": [11],
"f5": [3],
"two-digit": [5],
"dz": [0],
"startup": [5],
"projectsavemenuitem": [3],
"editundomenuitem": [3],
"useless": [11],
"xmx6g": [5],
"digit": [[2,6]],
"which": [11,5,1,[6,9],[4,8]],
"u000a": [2],
"signific": [11],
"carri": [6,[5,8]],
"u000d": [2],
"u000c": [2],
"eu": [8],
"she": [6],
"never": [11],
"aggress": [8],
"adjust": [11],
"activ": [11,8,10,9],
"first-class": [11],
"aren\'t": [10],
"compat": [5,1],
"frame": [5],
"cursor": [8,[9,11],1],
"go-between": [6],
"prototype-bas": [11],
"u001b": [2],
"stats.txt": [10],
"indic": [6,11,[5,8,10]],
"origin": [6,11,9],
"foo": [11],
"for": [11,6,5,8,4,9,2,3,1,10,0,7],
"exclud": [6,11],
"fr": [5,[4,11]],
"content": [11,[3,5],6,10,[1,8],[0,7,9]],
"desktop": [5,11],
"alert": [2],
"applescript": [5],
"client": [6,10,[5,9,11]],
"exclus": [11,6],
"gb": [5],
"class": [[2,11],7],
"helplogmenuitem": [3],
"over": [5,8],
"spanish": [4],
"someth": [5],
"editoverwritetranslationmenuitem": [3],
"outputfilenam": [5],
"go": [[8,11],[6,7]],
"counter": [9,7],
"kept": [[1,10]],
"aeiou": [2],
"form": [11,[1,5,6,10],[3,8]],
"publish": [6],
"inflect": [1],
"setup": [11,4,[5,7],8],
"restor": [9,11,8,[3,6,10]],
"avoid": [[6,11],10],
"assign": [5,3,11],
"hh": [6],
"select": [11,8,5,3,9,[4,6],10,0],
"duser.languag": [5],
"redefin": [11],
"canadian": [11],
"easili": [6],
"tab-delimit": [1],
"bis": [2],
"clipboard": [8],
"repetit": [11,8],
"output": [6,11,1,[3,5,8]],
"veri": [11,[6,10]],
"read-on": [[1,6]],
"file-target-encod": [11],
"projectopenmenuitem": [3],
"autom": [5],
"corner": [9],
"four": [8],
"decim": [11],
"non-whitespac": [2],
"context": [11,9,[3,6,8]],
"model": [11],
"drag": [9,5,7],
"https": [6,5,[9,11]],
"id": [11,6],
"decis": [10],
"if": [11,8,5,6,10,[4,9],1,3,0,2],
"french": [11,5],
"project_stats.txt": [11],
"ocr": [[6,11]],
"projectaccesscurrenttargetdocumentmenuitem": [3],
"toolsvalidatetagsmenuitem": [3],
"in": [11,6,5,8,9,1,10,4,2,3,0,7],
"termin": [5,2],
"lower": [9,[10,11],[3,6,8]],
"ip": [5],
"index": [11],
"is": [11,5,6,9,8,10,1,[2,4],3,0,7],
"it": [11,5,6,9,1,10,8,0,4,7],
"decid": [11],
"odf": [6,11],
"odg": [6],
"ja": [5],
"multiterm": [1],
"begin": [[2,11],[1,5,6,10]],
"odt": [6,11],
"gotonexttranslatedmenuitem": [3],
"viewmarktranslatedsegmentscheckboxmenuitem": [3],
"viewer": [[1,6,9]],
"paragraph": [11,8,6],
"valu": [11,2,5],
"standalon": [5],
"nplural": [11],
"js": [11],
"ilia": [5],
"believ": [6],
"learned_words.txt": [10],
"pojavni": [1],
"optic": [6],
"ftl": [5],
"side": [6],
"ftp": [11],
"break": [11,4],
"editselectfuzzy1menuitem": [3],
"themselv": [[2,6,11]],
"upgrad": [5,11],
"viewdisplaymodificationinfoallradiobuttonmenuitem": [3],
"draw": [6,11],
"case-sensit": [[2,11]],
"comfort": [5],
"off": [[8,11],9],
"hide": [11],
"report": [[6,11]],
"le": [1],
"dswing.aatext": [5],
"ll": [5],
"auto": [10,[6,8,11]],
"receiv": [5,[9,11]],
"un-com": [5],
"sign": [[1,5,9]],
"notepad": [1],
"lu": [2],
"document.xx.docx": [11],
"while": [[6,9,10],[4,8]],
"second": [[9,11],[1,3,5]],
"that": [11,6,5,[8,9],10,4,1,0,3],
"cycleswitchcasemenuitem": [3],
"download": [5,0,[3,6,8,11],[1,4,7]],
"faster": [1],
"high": [11],
"split": [11,9],
"mb": [5],
"oracl": [5,3,11],
"than": [11,5,6,[2,4,9,10]],
"limit": [11,[1,5]],
"me": [6],
"outdat": [6],
"omegat.png": [5],
"gradlew": [5],
"mm": [6],
"administr": [11],
"entri": [11,1,8,3,[2,5,7,9]],
"level": [6,8,11],
"auxiliari": [6],
"mr": [11],
"author": [[8,9,11]],
"ms": [11],
"toggl": [6],
"mt": [10],
"modif": [[3,11],8,6],
"my": [6,5],
"establish": [11],
"chosen": [8],
"disk": [8],
"expand": [11],
"nb": [11],
"updat": [11,1,8],
"produc": [6,11],
"nl": [6],
"legisl": [6],
"nn": [6],
"no": [11,5,[1,8],9,[4,6,10],0],
"behind": [11],
"code": [3,4,11,5],
"gotohistoryforwardmenuitem": [3],
"box": [11,[2,4]],
"switch": [3,11,8,6],
"dialog": [11,8,1,[4,6,10],[7,9]],
"total": [9,[8,11]],
"of": [11,6,5,9,8,10,4,1,2,3,7,0],
"bundl": [5,11],
"immun": [10],
"possibl": [11,6,9,5,1,[2,3,10]],
"involv": [6,11,[4,5]],
"ok": [[5,8]],
"dynam": [11],
"on": [5,11,6,9,8,4,[0,3],[1,10]],
"keyboard": [9,[3,11]],
"macro": [11],
"purpos": [11,[1,5]],
"technic": [11,8],
"or": [11,6,5,2,9,8,1,3,4,0,10],
"os": [[5,6,11]],
"src": [6],
"control": [6,[3,8,11],[2,5]],
"encod": [11,1],
"no-team": [[5,6]],
"editinserttranslationmenuitem": [3],
"pc": [5],
"pdfs": [6],
"offici": [7],
"compliant": [6],
"easier": [[6,11]],
"po": [11,9,5],
"closest": [11],
"optionsglossarystemmingcheckboxmenuitem": [3],
"pt": [5],
"inclus": [[2,9]],
"upper": [[9,11],[3,8]],
"environ": [5],
"haven\'t": [8],
"optionsautocompleteglossarymenuitem": [3],
"necessari": [6,[5,11],[0,1,4]],
"damag": [[1,11]],
"concurr": [8],
"recent": [8,[3,5,6]],
"they": [11,6,[2,3],[4,5,8,10]],
"pinpoint": [11],
"edit": [11,9,8,3,[5,7],6,[1,4,10]],
"varieti": [10],
"old": [11,5],
"subtract": [2],
"editselectfuzzy5menuitem": [3],
"them": [[6,11],9,[4,5,8,10]],
"bilingu": [[6,11]],
"then": [11,5,4,[6,10],[1,3,8]],
"degrad": [10],
"kde": [5],
"accept": [10,[3,5]],
"rc": [5],
"includ": [11,6,9,5,[2,8]],
"refocus": [11],
"privaci": [5],
"minut": [6,[8,11]],
"sub": [1],
"access": [11,3,[5,8],6,[0,9]],
"currenc": [2],
"languag": [11,6,5,4,[1,9],[0,7,8,10]],
"seek": [11],
"seen": [6],
"seem": [[1,8]],
"sc": [2],
"current": [8,11,9,10,3,1,[5,6]],
"so": [11,6,1,[5,9],10],
"key": [11,5,3,[2,9]],
"impract": [5],
"intern": [[9,11],[1,8]],
"onc": [[5,6],[2,8],[1,3,11]],
"svg": [5],
"one": [11,8,9,[1,2,6],[5,10],3,0],
"launch": [5,[8,11]],
"svn": [6,10],
"store": [11,5,[6,9],[4,8,10]],
"interv": [11,6,8],
"stand-alon": [2],
"editoverwritesourcemenuitem": [3],
"stori": [1],
"closer": [11],
"confirm": [11,[3,5,8,10]],
"enforc": [10],
"bug": [8],
"remov": [11,[5,10],[4,6],[3,8,9]],
"tm": [10,6,11,8,[5,7,9]],
"assist": [6],
"to": [11,5,6,8,9,10,4,1,3,0,[2,7]],
"v2": [5],
"typic": [[5,6]],
"editreplaceinprojectmenuitem": [3],
"but": [6,11,[1,2],[5,9],[4,8,10]],
"symbol": [2],
"document.xx": [11],
"tw": [5],
"express": [11,2,7,[3,4,5,9]],
"viewmarkautopopulatedcheckboxmenuitem": [3],
"zero": [[2,11]],
"projectwikiimportmenuitem": [3],
"deactiv": [11,8],
"countri": [5,11],
"trivial": [6],
"ui": [6],
"variant": [[2,11]],
"subsequ": [5],
"un": [1],
"up": [6,11,5,[7,9]],
"written": [[8,11]],
"gotoprevioussegmentmenuitem": [3],
"usual": [[5,6,8],10],
"solut": [6],
"this": [11,5,8,6,10,9,[1,4],2,7],
"gotopreviousnotemenuitem": [3],
"editredomenuitem": [3],
"uilayout.xml": [10],
"verif": [11],
"thin": [11],
"substitut": [8],
"extract": [[0,11]],
"vi": [5],
"brazilian": [[4,5]],
"hint": [11,[4,6],7],
"desk": [9],
"know": [5,11],
"support": [6,11,[1,5],[2,8]],
"vs": [9,11],
"sinc": [5,11],
"higher": [11],
"drop": [9,5,[7,11]],
"scrollbar": [11],
"idea": [11],
"changer": [11],
"pure": [6],
"we": [11,6],
"unchang": [11],
"auto-text": [11,3],
"groovy.codehaus.org": [11],
"repo_for_omegat_team_project": [6],
"backspac": [11],
"choic": [[5,11]],
"normal": [5,11,10],
"gradual": [6],
"slight": [10,9],
"previous": [8,[3,6,11],9],
"licens": [[0,5,6,8]],
"emac": [5],
"org": [6],
"recognit": [6],
"recognis": [11],
"distribut": [5,6],
"xf": [5],
"behav": [11],
"deeper": [1],
"xx": [5,11],
"xy": [2],
"runtim": [5],
"sourc": [11,6,8,9,10,[3,5],1,7],
"individu": [11],
"tester": [2,7],
"realiz": [4],
"react": [9],
"none": [[3,8]],
"type": [11,6,5,8,[1,3,9]],
"beyond": [5],
"feedback": [9],
"toolssinglevalidatetagsmenuitem": [3],
"problem": [1,6,[0,7],5],
"review": [10],
"filenam": [11],
"routin": [5],
"projectaccesssourcemenuitem": [3],
"between": [11,6,[1,8,10],[2,9]],
"yy": [9,11],
"nbsp": [11],
"method": [[5,11]],
"gotosegmentmenuitem": [3],
"scroll": [11,9],
"come": [9],
"push": [6],
"zh": [6],
"exist": [11,[1,6],[5,10]],
"readme_tr.txt": [6],
"intact": [10],
"penalti": [10],
"exact": [11,[1,8],4],
"xx_yy.tmx": [6],
"key-valu": [11],
"regist": [8,3],
"sign-in": [5],
"flag": [[2,7,11]],
"utf8": [1,[8,11]],
"helpaboutmenuitem": [3],
"copi": [6,11,8,[4,9],5,10,3],
"our": [6],
"out": [6,8,11,5],
"weak": [11],
"get": [[6,11],10],
"dark": [11],
"statist": [8,[3,10],6,11],
"place": [6,[1,8],[4,5,10,11]],
"power": [11,2],
"packag": [5,8],
"accur": [10],
"leav": [11],
"regular": [11,2,6,7,[3,4,5]],
"c\'est": [1],
"tag-valid": [5],
"restart": [11],
"suggest": [[8,9,11],[3,4,10]],
"alway": [11,[1,3,8]],
"observ": [11],
"lead": [11],
"token": [11],
"filter": [11,6,8,5,[3,10]],
"help": [[3,7],[6,8]],
"expect": [[5,6,11]],
"site": [11],
"right-to-left": [6,7],
"u0009": [2],
"xhh": [2],
"behaviour": [[5,11]],
"revis": [[0,6,10]],
"u0007": [2],
"repositori": [6,[8,10,11],[5,7]],
"asset": [6],
"minimum": [11],
"date": [11],
"argument": [5],
"whatsoev": [6],
"data": [6,11,1,[5,7]],
"lowercasemenuitem": [3],
"wiki": [[0,9]],
"own": [11,[2,9]],
"firefox": [[4,11]],
"begun": [6],
"separ": [11,[1,6],9,[3,8]],
"tab": [1,11,[3,8],9,2],
"taa": [11,8],
"plain": [6,[1,11]],
"should": [11,6,3,[0,2],[5,9]],
"tag": [11,6,8,3,5,9,10],
"replac": [11,8,3,9,[6,7]],
"versa": [11,6],
"slovenian": [[1,9]],
"sens": [4,[10,11]],
"tar": [5],
"like": [5,11,6,[0,2,9,10]],
"maxim": [9],
"onli": [11,6,8,5,[0,1,4,9,10]],
"sent": [11],
"projectreloadmenuitem": [3],
"safe": [[6,11]],
"openoffic": [4,11],
"navig": [5,11,[4,6]],
"send": [6,11],
"irrespect": [11],
"here": [11,6,[5,10],8],
"note": [11,6,8,[5,9],10,[1,2,3,4],[0,7]],
"cross-platform": [5],
"optionsautocompletechartablemenuitem": [3],
"noth": [2,[1,8]],
"line": [5,11,2,3,6,10,[7,9]],
"link": [[0,5,10,11]],
"deliv": [11],
"becom": [6],
"winrar": [0],
"tbx": [1,11,3],
"tune": [11],
"wildcard": [[6,11]],
"can": [11,5,6,1,9,8,10,4,3,2,0],
"git": [6,[5,10]],
"cat": [10],
"duser.countri": [5],
"provid": [11,5,[4,6,8,9]],
"tck": [11],
"realli": [[8,11]],
"xx-yy": [11],
"readm": [[5,11]],
"will": [11,5,[6,8],10,9,4,1,2,3],
"virgul": [1],
"match": [11,[8,9],2,3,10,[1,6],7,4],
"follow": [11,2,5,[3,6],0,[4,8,9,10]],
"categori": [2,7],
"intent": [11],
"declens": [1],
"optionsspellcheckmenuitem": [3],
"align.tmx": [5],
"file2": [6],
"optionssetupfilefiltersmenuitem": [3],
"intend": [[2,6]],
"wild": [11]
};
