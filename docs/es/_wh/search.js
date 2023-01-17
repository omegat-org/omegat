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
 "appendix.acknowledgements.html",
 "appendix.keyboard.html",
 "appendix.languages.html",
 "appendix.legal.notices.html",
 "appendix.website.html",
 "chapter.about.OmegaT.html",
 "chapter.dictionaries.html",
 "chapter.file.filters.html",
 "chapter.files.and.directories.html",
 "chapter.files.to.translate.html",
 "chapter.font.html",
 "chapter.formatted.text.html",
 "chapter.glossaries.html",
 "chapter.installing.and.running.html",
 "chapter.instant.start.guide.html",
 "chapter.machine.translate.html",
 "chapter.menu.html",
 "chapter.misc.html",
 "chapter.plain.text.html",
 "chapter.project.properties.html",
 "chapter.regexp.html",
 "chapter.searches.html",
 "chapter.segmentation.html",
 "chapter.spellchecker.html",
 "chapter.translation.editing.html",
 "chapter.translation.memories.html",
 "chapter.user.interface.html",
 "index.html",
 "ix01.html"
];
wh.search_titleList = [
 "Apéndice E. Agradecimientos",
 "Apéndice C. Atajos de teclado en el editor",
 "Apéndice B. Idiomas ― Lista de códigos ISO 639",
 "Apéndice D. Aviso Legal",
 "Apéndice A. OmegaT en la web",
 "Sobre OmegaT - Introducción",
 "Diccionarios",
 "Filtros de archivo",
 "Archivos y directorios de OmegaT",
 "Archivos a traducir",
 "Configurando el tipo de letra",
 "Trabajando con texto con formato",
 "Glosarios",
 "Instalando y ejecutando OmegaT",
 "¡Aprenda a usar OmegaT en 5 minutos!",
 "Traducción automática",
 "Menú y atajos de teclado",
 "Temas diversos",
 "Trabajando con Texto sin Formato",
 "Propiedades del Proyecto",
 "Expresiones regulares",
 "Búsquedas",
 "Segmentación del texto fuente",
 "El Corrector Ortográfico",
 "Comportamiento del campo de edición",
 "Memorias de Traducción",
 "La interfaz de usuario",
 "OmegaT 2.3.0 - Guía de usuario",
 "Índice"
];
wh.search_wordMap= {
"cornuall": [2],
"mejora": [4],
"pretend": [5],
"característica": [[11,15,26]],
"avanc": [17],
"instalador": [8],
"valonia": [2],
"tel": [2],
"ten": [13,25,[8,23],[11,19,26],[12,15,17],[6,18,22]],
"info.plist": [13],
"vario": [[25,26],[5,8,9],[12,16,17]],
"antonio": [3],
"tex": [9],
"distinto": [[17,20,23,26]],
"kua": [2],
"mostrar": [13,16,[8,12,18,26]],
"guarda": [16,[9,12,13],[18,25]],
"verán": [5],
"ventaja": [13],
"aragoné": [2],
"ventana": [26,28,16,[13,21],27,[10,17],11,[7,8,23,25],[9,15,19,22]],
"guardar": [12,16,[7,13]],
"kur": [2],
"llevar": [[25,26]],
"ces": [2],
"omision": [0],
"gikuyu": [2],
"tgl": [2],
"violeta": [16],
"tgk": [2],
"modificar": [7,26,19,[3,8,13,16],[9,11,14,22]],
"haberlo": [0],
"quiet": [13,17],
"investigación": [15],
"quier": [[4,16,20]],
"comunidad": [25],
"sami": [2],
"presiona": [13,[24,26]],
"área": [19,26],
"modificacion": [3,[5,7,8,9,12,13,19,21]],
"bambara": [2],
"gestor": [[8,23]],
"tha": [2],
"desempaquetarlo": [6],
"inserta": [16,[24,26,28]],
"públicament": [4],
"download.htm": [13],
"sale": [16],
"llevan": [25],
"recargado": [12],
"imag": [13,8],
"botón": [13,26,[16,21,22,23]],
"monolingü": [9],
"descripción": [[0,13,25,26]],
"agrupado": [8],
"hablar": [0],
"predefinida": [[20,22]],
"pérdida": [17,28,[5,8,19,27]],
"archivos": [27],
"tir": [2],
"cha": [2],
"export": [[9,12,25]],
"che": [2],
"herramienta": [28,[15,16,25],20,[5,26],[9,22,27],[13,17]],
"aplica": [13,11],
"reduc": [[1,9]],
"diseñado": [9],
"checo": [[2,18]],
"donando": [28],
"chv": [2],
"chu": [2],
"agrupada": [[11,28]],
"plataforma": [8,13,[6,15]],
"respuesta": [4],
"predefinido": [7],
"fr-fr": [23],
"conservar": [11],
"motivo": [4],
"hacia": [16,1,[9,22]],
"ndonga": [2],
"apariencia": [[7,13]],
"lograr": [[13,23]],
"disco": [[13,16,25]],
"directorio": [13,25,8,23,[16,28],17,[19,26],[6,12],[5,21],[4,7,9,24]],
"varia": [26,[13,21]],
"termina": [[17,20]],
"xmxzzm": [13],
"webster": [6,28,[26,27]],
"anidación": [11],
"ocurren": [21],
"validar": [[14,16],27],
"duplicar": [11],
"permit": [24,[16,25,26],[7,9,13],[5,8,11,12,15,21,28]],
"cargar": [16,[7,12],[22,26]],
"contrario": [[11,16,26]],
"cargan": [25,12,7],
"empti": [[13,25]],
"porcion": [4],
"valida": [9],
"irrelevant": [17],
"corea": [2],
"derecho": [[3,26],[13,16],23],
"fallos": [27],
"adverbio": [0],
"presta": [11],
"spolski": [4],
"traducir": [[5,14,17],[8,9,22,24,25,26],[7,13,16,27],[4,15,21]],
"variabl": [16],
"ofrec": [[7,15,22,24,26]],
"derecha": [28,9,1,26,[12,13,16,22,27]],
"sustantivo": [15],
"mejor": [25,[10,11,15,17,22,26]],
"orden": [13,28,16,8,[17,21],22,11,[0,1,5,25,26]],
"propon": [5],
"tmx": [25,28,13,8,17,[5,16,21]],
"realizado": [13,[8,19,21]],
"añadiendo": [13,28,[7,26]],
"intel": [13,[27,28]],
"comúnment": [9],
"fr-ca": [22],
"gradualment": [25],
"lanzamiento": [28,13,17],
"cmd": [[1,16],9],
"indonesio": [2],
"convertir": [9],
"romanch": [2],
"razon": [12],
"legibilidad": [16],
"pestaña": [26],
"exportada": [8],
"procedimiento": [25,17,[5,23]],
"hacer": [13,[18,26],[7,8,9],[11,12,15,16,21,22,23,24]],
"ton": [2],
"negrita": [26,11,[9,21]],
"unificación": [9],
"explícitament": [[0,17]],
"exportado": [[12,16]],
"mongolia": [2],
"powerpc": [13],
"instalándola": [13],
"ortográficos": [27],
"capacidad": [8],
"rota": [11],
"poner": [25],
"ciérrala": [21],
"ponen": [15],
"minutos": [27],
"llamado": [13,[5,23,25]],
"con": [26,13,16,28,8,20,12,[5,25],9,11,[7,21],[14,23],[17,18],[22,27],6,19,[3,4,15],[10,24],0],
"cirílico": [18],
"mensaj": [[8,13,17],[25,26]],
"cos": [2],
"cor": [2],
"traducen": [[5,15]],
"lao": [2],
"telugu": [2],
"quita": [13],
"separador": [26],
"lat": [2],
"sientan": [16],
"las": [26,11,13,25,16,22,21,5,7,9,8,12,28,[19,20],14,[0,4,15,23],[1,17],[2,10,18,24,27]],
"lav": [2],
"básica": [[11,13]],
"correcto": [[13,23]],
"cpu": [26],
"translat": [15,28,[5,25,26]],
"empeorar": [15],
"eran": [25],
"cargado": [14],
"aviso": [28,3,[0,1,5,27]],
"lanzará": [13],
"interesan": [14],
"búlgaro": [2],
"tsn": [2],
"interesar": [15],
"tso": [2],
"correcta": [25,12,[13,14,23]],
"cre": [2],
"pasando": [24],
"chmod": [8],
"recarga": [25,[12,17]],
"gnome": [13,28],
"almacena": [[5,13,17],25],
"destinado": [20],
"haya": [26,[12,13,17,21,25],[7,14,15]],
"muestr": [[9,13]],
"significar": [26],
"sección": [18,[11,19,26]],
"orden2": [25],
"singalé": [2],
"asistida": [[5,27]],
"orden1": [25],
"avanza": [8],
"appdata": [8],
"definen": [26],
"asegúr": [13,23,[8,9,11,12]],
"csv": [12],
"directa": [7],
"lea": [11],
"ofrecida": [[16,26]],
"tuk": [2],
"lee": [[4,7]],
"carga": [13,[7,9,14,17,25,26]],
"estimacion": [26],
"tur": [2],
"modifícalo": [[10,17]],
"tus": [13,26,[5,18],[17,22,24,25]],
"tuv": [25],
"seguir": [26],
"integrado": [26],
"datos": [27],
"sotho": [2],
"sienta": [[13,17]],
"les": [20,[12,16,23,25]],
"prest": [13],
"dock": [13,28],
"ofrecido": [15],
"ctr": [11],
"transferencia": [15],
"ddhttp.proxyhost": [13],
"directo": [13,[9,16]],
"crecient": [15],
"dobl": [13,8,[12,20,26]],
"swahili": [2],
"māori": [2],
"ctrl": [16,26,1,28,14,9,11,[8,15,21],[6,19,24]],
"mykhalchuk": [3],
"document": [[0,8,11,13,17,25]],
"twi": [2],
"dibujo": [7],
"mejoran": [15],
"añaden": [13],
"mejorar": [15],
"polaco": [2],
"codicioso": [20],
"otras": [27],
"privacidad": [13],
"tamil": [2],
"construir": [15,13],
"superfici": [15],
"posición": [16,26,22,[8,9,11,21,25]],
"cosas": [27],
"resourc": [13],
"cargu": [25],
"resaltado": [16,26],
"descomprimir": [[8,13]],
"encuentra": [13,12,[7,25,26],[1,11,15,16,17,21]],
"xx_yy": [7],
"desear": [22],
"docx": [9],
"txt": [18,9,[7,12]],
"duplicarlo": [11],
"descarga": [13,[4,6,8,12,16]],
"coincident": [7,26],
"lituano": [2],
"definir": [22,[5,8,10,17,19,20]],
"lib": [8],
"actualiza": [[13,25]],
"asigna": [[7,13]],
"preprocesan": [9],
"ojibwa": [2],
"lin": [2],
"lim": [2],
"lit": [2],
"entregar": [25],
"atajos": [27],
"index.html": [[13,26]],
"omegat.tmx": [25],
"wörter": [12],
"pretraduc": [25],
"actual": [16,26,8,[12,24],[1,13,17,21,25],[0,22,28]],
"sobrescribirá": [13],
"terminador": [20],
"destacada": [26],
"breton": [2],
"fecha": [[8,12]],
"escribiendo": [13],
"fula": [2],
"cym": [2],
"actuar": [8],
"distribuida": [3],
"aspecto": [[5,7,9,14,26]],
"aprovechar": [25],
"estrategia": [[24,25]],
"chewa": [2],
"mientra": [[5,8,26],[7,12,15,17]],
"puerto": [13,28],
"precio": [26],
"retrasar": [25],
"principio": [1,25,[13,14]],
"comando": [16,13],
"destino": [16,7,28,[9,25],23,12,26,[8,11,21],[13,15],[5,17,19],[6,18,24]],
"introducirla": [14],
"cerrars": [16],
"expresiones": [27],
"servicio": [15,13,16,[5,8,26]],
"start": [13,28,27],
"abkhaz": [2],
"smolej": [[3,27]],
"cursiva": [11],
"equal": [[13,25]],
"alcanzado": [25],
"abreviar": [20],
"apartart": [11],
"bosnia": [2],
"conducir": [25],
"fácilment": [9],
"obvia": [12],
"ocasion": [9],
"enter": [13],
"comenzado": [[19,26]],
"asignó": [5],
"bien": [[16,20,25],[7,8,11,12,26]],
"traducida": [[14,25]],
"applic": [[8,28]],
"distribuir": [8],
"creativa": [22],
"preced": [8],
"traducido": [26,14,11,9,16,[7,25],8,[13,17,21,28],[5,24],[15,19,23,27]],
"memori": [13],
"interactiva": [20],
"creativo": [5],
"realment": [16],
"afar": [2],
"godfrey": [3],
"importancia": [22],
"avestan": [2],
"log": [8],
"dirigir": [24],
"eliminando": [[11,26,28]],
"visualización": [9],
"accidental": [11],
"los": [[25,26],13,16,8,7,9,12,23,5,14,[17,19],11,[15,18,22],[21,24],20,6,[0,28],[3,4],[1,2,10,27]],
"list.sourceforge.net": [4],
"introducido": [15],
"necesita": [[13,23],5,[4,8,11,12,14,15,17,20,25]],
"centro": [20],
"siguen": [20],
"comporta": [26],
"consult": [14],
"introducida": [21],
"dificultad": [8],
"sueco": [2],
"depósito": [15],
"clic": [13,26,7,[8,16],[4,14,22,23],[1,9,11,12,21]],
"especificando": [13],
"prof": [22],
"campana": [20],
"completament": [25],
"validación": [26,28,[11,16],27,[9,10,17]],
"cancelada": [16],
"otros": [27],
"erro": [21],
"desplazar": [26],
"convertirá": [14],
"colocarlo": [12],
"dan": [2],
"dar": [[11,16,25,26]],
"intro": [1,11],
"das": [12,[13,23]],
"resaltada": [[11,16,26]],
"cierta": [8,[11,25]],
"cierto": [5],
"wolof": [2],
"particular": [3,[7,11,13,17,23,25]],
"formar": [4],
"dond": [[13,26],8,[16,22,25]],
"fácil": [11,9],
"ademá": [[6,8,10,12,13]],
"cambiar": [26,[7,9],17,[13,16,18,19,22],[8,10,23,25]],
"lsa": [22],
"tradúcelo": [9],
"corso": [2],
"part": [26,16,[8,13,22,23],[4,12,15,21],[5,11,17]],
"acuerdo": [5,7],
"añadir": [[7,12,13,14],[4,9,17,22,25]],
"agencia": [[5,26]],
"adecuada": [[5,9]],
"instruccion": [13,[5,7]],
"pare": [15,11,17],
"principal": [27,15],
"para": [13,26,16,25,[8,22],7,14,15,11,9,23,[5,12],17,[4,21],[3,18],28,[19,20,24,27],[6,10],[0,2]],
"cargará": [25],
"adecuado": [13,23,7,18],
"próximo": [16],
"bloqu": [20,22],
"cierra": [26,16,25,17],
"arrancando": [13],
"ltr": [9,28],
"reportará": [24],
"ltz": [2],
"tema": [25,17,[4,5,23,28]],
"lub": [2],
"tonga": [2],
"lug": [2],
"subsecuent": [13],
"mezclando": [28,9],
"del": [28,26,16,25,13,8,12,1,7,22,[9,14,19,23],24,27,11,17,[5,15],[10,18,21],2,[4,6],[0,20]],
"reescribir": [7],
"targetlocal": [7],
"path": [13],
"des": [[12,13,19]],
"deu": [2],
"encuentr": [12],
"recorr": [16],
"edición": [26,16,24,28,27,25,[5,9,10,12,21]],
"relativo": [25],
"encuentren": [12],
"especi": [[7,11],[0,1,5,9,13,17,21,22]],
"importando": [25,28],
"cual": [[7,13,23,25],8,[4,5,12,15,17,24,26]],
"revisarán": [16],
"fueron": [25],
"paso": [[4,16,22]],
"habilita": [[5,20]],
"exactament": [[4,7,21,23]],
"descrita": [13],
"acceden": [26],
"coincidencia": [26,16,28,25,24,[12,20],[1,9,14,17,21,22,27]],
"asegurado": [14],
"transparent": [12],
"uig": [2],
"azerbaiyán": [2],
"sólo": [[13,25],26,[7,8],[11,18,21,23],[0,6,9,12,16,17,19,22]],
"ortográfico": [23,28,[5,16,27],8,[15,17]],
"habla": [12],
"term": [12],
"dotx": [9],
"gestión": [16,[5,28],[8,9]],
"ortográfica": [23],
"hacerl": [21],
"duden": [26],
"cubr": [8],
"refieren": [[25,26]],
"revisar": [25,5],
"involucrado": [26],
"comienc": [25],
"existen": [[9,25]],
"sirvió": [0],
"spotlight": [13],
"did": [22],
"dic": [23],
"tswana": [2],
"murray": [[0,3]],
"ahorra": [[24,25]],
"nuev": [1],
"dir": [13],
"latex": [9],
"actualizado": [[12,23]],
"submenú": [13],
"ubicado": [13,16],
"div": [2],
"unificar": [11],
"legal": [28,3,[0,1,5,27]],
"ukr": [2],
"omegat": [13,28,8,25,17,5,27,[4,14],9,26,16,7,18,24,[12,15,23],[0,11,21],[19,22],3,10,[2,6,20]],
"restableciendo": [28],
"excepcional": [25],
"sobre": [27],
"kanuri": [2],
"final": [1,7,[11,16,20],[0,5,14,15]],
"apoyo": [[0,4,28]],
"ubicada": [13],
"ubicación": [28,[8,13],25,[17,21],[4,12,14,16,19,26]],
"console-align": [13,17],
"alfabéticament": [25],
"ms-dos": [13],
"algún": [26,[0,7,12,17,22,25]],
"minú": [16],
"restaura": [16],
"analizan": [25],
"coincid": [20,7,21,[23,25]],
"insertar": [16,24,1,28,[7,9,22,27]],
"henri": [3],
"cerrada": [16],
"incluya": [13],
"una": [13,[25,26],16,22,12,20,[8,21],5,15,[9,11],23,[1,7,17],24,[10,14,18,19,28],[0,27]],
"und": [23],
"grand": [17],
"insertan": [16],
"dirigirlo": [25],
"kikuyu": [2],
"cuantificadores": [27],
"uno": [11,[12,17],[5,13],[0,6,16,20,25,26]],
"quechua": [2],
"interpret": [18],
"ingreek": [20],
"seguro": [[15,18,19,21]],
"editando": [16,28,24],
"saber": [5],
"x_windows_without_jr": [13],
"vietnamita": [2],
"ignor": [[8,23]],
"aproximación": [26],
"haber": [[6,8,12,17,19,22,25]],
"implementación": [13],
"pt_pt.arr": [23],
"inconsistencia": [0,[16,26]],
"diálogo": [28,7,[14,16],25,[10,17,19,26,27],[18,24],[8,22,23]],
"resalta": [26],
"latin": [2],
"quitando": [26],
"integridad": [8],
"terminado": [[12,26],25],
"provista": [15],
"punto": [20,22,[0,4,5,8,13,26]],
"x_source.zip": [13],
"cadena": [21,28,[9,26],12,[15,24,25]],
"provisto": [26],
"útil": [[3,5,13,25],[7,8,14,16,22,23,24,26]],
"maco": [13],
"especialment": [17],
"curso": [26],
"perder": [[17,26]],
"doc": [[9,26],8],
"dog": [12],
"reajustar": [26],
"publicar": [17],
"remuev": [23],
"dos": [13,[22,23,26],[5,21],[8,11,12,17,24,25],9],
"incluso": [3,[7,11,13,25,26]],
"mac": [13,1,[16,28],[5,8,9,14,27]],
"manej": [[7,9]],
"mah": [2],
"gaélico": [2],
"asociando": [9],
"permanecen": [13],
"numeración": [11],
"mal": [11,[2,23]],
"indicadores": [27],
"man": [13],
"libre": [27],
"especifica": [13,[7,17,18]],
"mar": [2],
"surgir": [9],
"anteriorment": [13,[9,12,22,25]],
"urd": [2],
"shona": [2],
"cometido": [19],
"descargar": [28,[6,23],[8,13,27]],
"estricto": [25],
"url": [[7,13,16]],
"varios": [27],
"megabyt": [13],
"pseudotraducción": [25,28],
"suministran": [23],
"faroes": [2],
"etiquetado": [28],
"actualizada": [[2,5,9]],
"reconozca": [23],
"www.omegat.org": [4],
"citando": [20],
"usa": [26,13,15,[6,16]],
"válido": [22,[13,17,25]],
"use": [[14,23]],
"comprobarán": [13],
"programador": [16],
"uso": [[17,28],[13,20,26,27],[5,6,22]],
"omegat.jar": [[17,25]],
"usr": [13],
"ofrecen": [18],
"ust": [17],
"logo": [8],
"combina": [8],
"lista": [28,8,27,[2,7,23,26],[5,15,16,22,25],[1,4,9,12,13,18,20,21]],
"libro": [15],
"pseudotraducida": [25,[27,28]],
"intensivo": [26],
"utf": [[12,18]],
"idear": [20],
"adición": [[13,22]],
"borrar": [11],
"listo": [21],
"descrito": [13],
"descomprimirlo": [[6,13]],
"servir": [[14,26]],
"aparecerán": [7,[22,23]],
"openoffice.org": [23,12,[7,9],[11,22]],
"emparejamiento": [11],
"galicia": [2],
"desarrollador": [4,8],
"dtd": [9],
"moviéndos": [16,28],
"merec": [0],
"mes": [[17,25]],
"make": [22],
"posibilita": [11],
"recargar": [25],
"console-transl": [[13,17]],
"bajo": [13,[3,17],[6,8,25]],
"podrá": [[7,8,11,23]],
"conseguir": [17],
"distribución": [13,3,8],
"conform": [[8,13]],
"wordart": [7],
"princip": [26,28,16,25,[5,7,8,17,22],10],
"inform": [4,[8,28]],
"depend": [[8,9,15,23]],
"osetia": [2],
"eliminará": [11],
"respalda": [17],
"project_stats_match.txt": [26],
"lanzador": [13],
"dvd": [17],
"entera": [22],
"quarkxpress": [9],
"osetio": [2],
"propia": [[13,15,20,22,26]],
"rastrear": [11],
"limburg": [2],
"divergent": [15],
"avanzado": [[5,13,17,20,25]],
"insertando": [28],
"preposicion": [0],
"avanzada": [[11,21,28]],
"mala": [11],
"krunner": [13],
"controlar": [[13,24]],
"libreoffic": [14],
"codiciosos": [27],
"sustituyendo": [7],
"propiedad": [[19,28],16,[13,17,25],[5,7,26],[6,8,14,22]],
"objeto": [22],
"uighur": [2],
"propio": [[24,25],[18,23,26]],
"expresion": [20,21,[22,28],5,[7,13,26]],
"común": [18],
"min": [[25,26]],
"volver": [26,16,[7,9,11,12,14,21,22,25]],
"uzb": [2],
"exacta": [21,26,16,12],
"mano": [[15,19]],
"manx": [2],
"texto": [9,28,16,24,18,[22,26],[7,11],5,[12,25],[21,27],[15,20],14,[10,17],[1,8,13,23]],
"exacto": [7],
"alojado": [8],
"terminando": [13],
"examina": [13],
"todos": [27],
"progreso": [26],
"open": [[7,11],[9,25]],
"www.oracle.com": [13],
"mkd": [2],
"errónea": [21],
"serán": [25,13,[5,7,8,12,16,22,23,24,26]],
"xmx1024m": [13],
"configurando": [28,[10,27],17,[8,11,13,16,23,24]],
"afortunado": [26],
"lenguaj": [[9,18,22]],
"único": [26,8,[7,11,21]],
"dzo": [2],
"incluir": [[8,11]],
"pegar": [16,26,[1,27,28]],
"automáticament": [13,[8,24],[12,14,18,23,26,28],[5,7,16,17,21]],
"observar": [5],
"manzana": [16],
"encontrarán": [7],
"única": [[8,11,12,26]],
"ingresa": [[13,16],19],
"mlg": [2],
"nnn.nnn.nnn.nnn": [13],
"guía": [5,27,[0,3,4,13,25]],
"actualizará": [[8,12,25]],
"mlt": [2],
"numerada": [11],
"involucrart": [22],
"precaución": [[7,11,22]],
"copiarla": [11],
"segmentación": [22,28,5,19,[20,26],[9,11,16,25],[7,27],8],
"internet": [[8,23,26],[5,15,21]],
"navega": [25,[13,23]],
"saltar": [[7,26]],
"terminológico": [12],
"también": [28,13,26,[12,25],16,[9,23],[5,7,17],[4,14,22,27]],
"interferirá": [13],
"predogl": [12],
"cálculo": [26,8],
"escocé": [2],
"aparec": [13,[25,26],[15,16]],
"patron": [7,22],
"externo": [26,[8,16]],
"previniendo": [[17,28],[19,27]],
"basa": [[15,26]],
"señalado": [26],
"repetición": [26],
"externa": [26,9],
"registro": [[4,11]],
"denominación": [11],
"documentación": [3,[0,28],[5,8,20,26,27]],
"multinacion": [25],
"aconsejamo": [22],
"búsqueda": [21,26,28,[16,20,27],5,[6,7,9,10,17]],
"registra": [[8,13,17]],
"realizará": [21],
"bash": [8],
"consistencia": [5],
"manualment": [[12,23,25],[9,14,16]],
"mark": [0],
"base": [[12,26]],
"realizar": [13,16],
"lote": [8],
"mon": [2],
"haciendo": [26,8,[9,12,13]],
"escriba": [[8,14]],
"volapük": [2],
"quiera": [[5,8]],
"cortar": [16,[1,26,27]],
"nombre_proyecto-level1": [[8,25]],
"indica": [[8,12,23]],
"internacion": [12],
"ejecutado": [16],
"van": [23],
"cuyo": [26],
"gedit": [12],
"wort": [12],
"consejo": [[16,28],[9,11,23,26]],
"manipulación": [11,[9,12]],
"subcarpeta": [14],
"incómoda": [18],
"estructura": [8,[22,25],[19,28]],
"word": [9,[7,12,14]],
"variat": [25],
"swati": [2],
"personalizando": [28,4],
"extension": [23,6,[9,18]],
"desactivada": [[7,16]],
"conexión": [[5,23]],
"luxemburgué": [2],
"agregarla": [23],
"contenga": [25,[17,23]],
"hasta": [[1,20],26,[5,8,12,13,15,17,19,21]],
"servidor": [13,28],
"europa": [18,28],
"acostumbrado": [16],
"mri": [2],
"chamorro": [2],
"zaslona": [12],
"cingalé": [2],
"zhuang": [2],
"exista": [8],
"mrs": [22],
"canaré": [2],
"ofensor": [5],
"movido": [16],
"repág": [1],
"entrada": [12,[9,27],[13,16,21,28],[7,11,15,20,25,26]],
"realizan": [21],
"sourceforge": [27],
"sirven": [8],
"msa": [2],
"reten": [9],
"uzbeko": [2],
"dirigirse": [27],
"asegurart": [8,11],
"alimentado": [13],
"evita": [18],
"esto": [13,[9,25],26,8,[23,24],[5,19],[11,17,18],[7,12,20,21,22]],
"mst": [25],
"x_sourc": [13],
"vea": [14],
"tomar": [17,[8,10,16,23]],
"requisito": [17,13],
"html": [13,[7,9],22,[8,11,14,17,25,26]],
"eludir": [11],
"ven": [2],
"independient": [8,[13,15,20,26]],
"ver": [28,16,14,22,[11,23,26,27],[4,9,13,15,25]],
"encuentran": [13,[16,26],[6,8,17,21]],
"ves": [20],
"estructurado": [12],
"informando": [28],
"vez": [25,13,[8,15,16,26],[5,20],[9,11,12,14,17,19,22,23]],
"artund": [23],
"captura": [4],
"vocabulario": [23,12],
"este": [27],
"esta": [13,16,25,26,[8,11,23],[5,9,22],[1,12,15,17,18,20,24]],
"preguntar": [[8,25]],
"velocidad": [15],
"comprobar": [23,[16,18,21,22,25,26]],
"almacenar": [[5,12,23,26]],
"acerca": [[5,13,16,26]],
"almacenan": [[8,25]],
"www.ibm.com": [13],
"ejecuta": [13,17],
"luego": [13,11],
"examinar": [13],
"muy": [5,[11,25],[4,8,9,12,15,16,18,21,22]],
"encapsula": [25],
"seleccionado": [16,24,[23,26],[7,10,12,14,15,17]],
"aunqu": [21],
"urdu": [2],
"disposición": [[15,16]],
"seleccionada": [16,26,[7,8]],
"conscient": [8],
"recib": [11],
"pista": [26],
"nombre_proyecto-level2": [[8,25]],
"cubren": [18],
"grave": [19],
"ordenador": [[5,13,18,27]],
"poder": [[15,23]],
"existent": [13,25,[8,12,15,16,19,22,24]],
"command": [14],
"sería": [13,[0,4,25]],
"archivos-de-configuración": [[13,17]],
"sustracción": [20],
"sucediendo": [4],
"existir": [9],
"personalizar": [7],
"anidar": [11],
"calidad": [15,[9,25]],
"caracteres": [27],
"protegida": [14],
"mayú": [16],
"operación": [8],
"elegir": [21,[8,23,25]],
"idioma": [[23,28],15,[13,25],[9,22],19,[17,18],[2,12],[5,26],7,14,[0,1,4,6,8,10,11,16,27]],
"esté": [13,26],
"toda": [21,[7,16],[0,1,8,9,12,18,22,23,25,26]],
"vie": [2],
"agrega": [[13,22]],
"recibir": [13],
"reutilizar": [25],
"permiso": [13],
"abajo": [[1,9,11,13,16,17,20,22,26]],
"mayor": [[5,11,15,22,23]],
"permite": [27],
"verificar": [14],
"google": [27],
"version": [13,25,[0,8,12,15,20]],
"project-dir": [17],
"explican": [5],
"está": [[16,25],13,8,[7,26],[5,11,12,17,22],[9,14,15,18,24],[1,4,19,20,23]],
"mya": [2],
"vista": [[8,10,22,26]],
"explicar": [26],
"itálica": [11],
"britannica": [[6,28]],
"configurar": [16,19,[7,14,28],[5,9,22,27]],
"conjunto": [22,16,[5,7,18,21,26]],
"recientement": [15],
"conteniendo": [8],
"japoné": [[2,13,17,22,25]],
"pasado": [[13,25]],
"minimiza": [26],
"detectan": [12],
"apertura": [11,5],
"modificará": [19],
"detectar": [[11,25]],
"todo": [[13,25,26],8,0,[16,19,21],[4,11,15],[1,7,9,14,17,18,20,24,28]],
"iceni": [9],
"freedict": [[6,28]],
"archivo_descargado.tar.gz": [13],
"nombre_proyecto": [[25,28]],
"dividido": [26],
"contribuir": [[4,8]],
"definición": [[8,26]],
"x_mac.zip": [13],
"elegist": [22],
"alinea": [13],
"tradicional": [13],
"haría": [26],
"provoca": [[9,13]],
"complemento": [26],
"asignando": [[13,28]],
"vacil": [8],
"emparejan": [26],
"lanza": [[12,13],17],
"segmentado": [22],
"cambiarl": [23],
"ell": [2],
"turco": [2],
"x0b": [20],
"visor": [[26,28],[16,17,25],[10,12]],
"fusión": [19],
"altern": [9],
"funcionalidad": [22],
"http": [13],
"atadura": [6],
"desacoplar": [26],
"accion": [[13,16]],
"detall": [[3,9,26],16],
"luganda": [2],
"significa": [22,[1,9,25]],
"hacerlo": [[23,25]],
"occident": [[18,28]],
"vol": [2],
"softwar": [3,13,17],
"directorio-de-configuración": [[13,17]],
"ignorado": [25],
"docbook": [[0,9,11]],
"helton": [0],
"eng": [2],
"abrir": [[8,26],16,[11,12,13,28]],
"iniciar": [13,[8,17]],
"aproximadament": [[12,17,26]],
"abarca": [18],
"adicional": [17,13,[5,8,20]],
"inicial": [[14,16]],
"instruir": [23],
"destacar": [26],
"inician": [13],
"éste": [16],
"creación": [[8,12]],
"minúscula": [[16,20,21],[26,28]],
"copyright": [3],
"básicament": [[9,18]],
"productividad": [22],
"coreano": [16],
"turno": [24],
"posnetka": [12],
"solucionar": [25],
"valga": [23],
"invis": [8],
"similar": [26,5,[16,21,25],[7,8,9,13,14,20]],
"nav": [2],
"alerta": [20],
"nau": [2],
"tar.bz2": [6],
"epo": [2],
"restaurar": [[7,16],[17,26]],
"stute": [12],
"idiomas": [27],
"combinación": [14,9,[1,6,13]],
"canadá": [[13,22]],
"pertinent": [[5,9]],
"africano": [2],
"x64": [13],
"nbl": [2],
"belarú": [2],
"interfaz": [13,26,17,[5,27],[4,8,9,16]],
"influencia": [9],
"cuanta": [12],
"tabulación": [12],
"interesado": [8],
"cuanto": [[25,26]],
"era": [9],
"malayo": [2],
"ere": [[6,26]],
"abren": [9],
"err": [21],
"dividiendo": [25],
"omitió": [26],
"búsquedas": [27],
"decidir": [[13,24]],
"nde": [2],
"estará": [7],
"cerrarla": [26],
"esa": [13],
"esc": [26,16],
"sustituirá": [15],
"jean-cristoph": [0],
"ese": [13,[11,14,16,21,22,23]],
"x86": [13],
"ndo": [2],
"toma": [25],
"logic": [21],
"reutilizarla": [25],
"eso": [[14,23]],
"est": [13,8,5,[7,11,16,25],[17,26],18,12,[20,23,24],[0,1,2,4,9,10,14,22]],
"grupo": [11,28,[4,8],[5,14,16,17,20,25,26]],
"screen": [12],
"configuracion": [17],
"actualizarán": [25],
"user.countri": [28],
"console-createpseudotranslatetmx": [13],
"etc": [9,[11,22],[12,13],[6,7,8,21,25,26]],
"longman": [[6,28]],
"nep": [2],
"merriam": [6,[26,27,28]],
"new": [12],
"escap": [20],
"descargando": [13,[23,28]],
"procesar": [7,[9,25]],
"procesan": [13],
"cámbial": [[17,25]],
"ventanas": [27],
"guión": [13,28],
"escribir": [9,[7,11,13,14,15,26]],
"invari": [24],
"línea": [13,[8,20],[12,17,28],[7,15,22],[1,5,11,25],[0,26,27]],
"intervalo": [[9,17]],
"interpreta": [9],
"posibilidad": [5],
"comportan": [[1,17,22]],
"año": [[17,25]],
"eus": [2],
"portapapel": [16],
"project_sav": [26],
"colocándot": [26],
"interpretado": [18],
"forma": [25,[7,11,12,26],[8,13],[1,5,9,15,18,19]],
"máquina": [[13,17],[10,15]],
"llevará": [26],
"respectiva": [9],
"navegación": [13],
"igbo": [2],
"recuerd": [14],
"project_save.tmx.tempor": [[17,25]],
"medio": [[12,17,19]],
"interlingua": [2],
"ndebel": [2],
"ejecutars": [13],
"groenlandia": [2],
"intenta": [7],
"ewe": [2],
"estar": [[13,15,17,18],[4,5,9,11,20,22,23]],
"ayuda": [28,16,26,0,[9,25,27],[14,17]],
"martin": [3],
"preferencia": [[8,13],26],
"amplia": [[5,23]],
"pferd": [12],
"carpeta": [14,[7,12,26]],
"principiant": [5],
"shift": [14],
"frisia": [2],
"piens": [8],
"wunderlich": [3],
"java": [13,8,17,28,[20,27],[7,9,21,25]],
"exe": [13],
"omitiendo": [26],
"comparación": [[11,26]],
"probador": [20,27],
"comodin": [21,28],
"project_save.tmx": [25,8,17,16],
"mayúscula": [[16,20],21,[1,13,22,26,28]],
"dictionari": [6],
"adaptars": [[11,26]],
"tahitian": [2],
"agregar": [[7,13],26,[4,8,12]],
"lanzando": [13,17],
"modelo": [15],
"ejecutarlo": [13,5],
"cero": [[11,20],[7,21,22]],
"seleccionando": [[18,22]],
"dictionary": [27],
"modificarlo": [9],
"infal": [12],
"casilla": [22,[7,23],[12,24]],
"subyacent": [11],
"marcar": [16,[7,11]],
"flash": [9],
"implican": [[13,18]],
"appl": [16,[13,14]],
"modificado": [16,17],
"grabado": [[17,19,25]],
"parámetro": [13,9,[8,17],[0,18,25,28]],
"deberá": [7],
"alterna": [8],
"br.arr": [23],
"cualquiera": [[12,13,15,20,21,25]],
"despleg": [19,[16,17,23,26]],
"mueven": [[1,11]],
"continuament": [25],
"nld": [2],
"gran": [[0,9,11,23,25,26]],
"omiten": [11],
"eslava": [2],
"clasificado": [26],
"cambiado": [25],
"persa": [2],
"segmentars": [[7,22]],
"lleva": [16,13],
"aplicarán": [22],
"omegat-l10n-request": [4],
"limitado": [7],
"eslavo": [2],
"duración": [26],
"absoluto": [20,[22,24,25]],
"estilo": [9,[11,22,25]],
"x_without_jre.zip": [13],
"fácile": [14],
"segmentará": [22],
"representan": [[7,11,26]],
"documento": [9,26,16,11,[1,7],[5,8,12,28],[3,14,22,24,25],[4,13,27]],
"microsoft": [9,7,14],
"amarillo": [[16,26]],
"regresa": [19],
"inglés-catalán": [15],
"changes.txt": [8],
"glossari": [14],
"comunes": [27],
"regreso": [15],
"holandé": [2],
"imagina": [5],
"mantendrá": [8],
"utilic": [[8,12,13]],
"nno": [2],
"corriendo": [[13,18]],
"recrear": [11],
"suel": [5],
"nob": [2],
"color": [9,26,16,11],
"propeti": [17],
"foco": [5],
"umbral": [24],
"dividirs": [22],
"poderosa": [21],
"prioridad": [22,28,27],
"utilización": [28,12,27],
"nor": [2],
"pantalla": [17,[4,9,10,13,26]],
"not": [22],
"central": [[2,18,28]],
"nos": [18],
"aún": [[11,16,25]],
"nov": [12],
"ascii": [9],
"índice": [28,[0,5,7]],
"memorias": [27],
"pierd": [8],
"deliberado": [11],
"was": [22],
"selection.txt": [24,16],
"xhtml": [7,[9,11,22]],
"resultar": [11],
"emitirá": [25],
"progresivament": [[8,11]],
"incluyen": [[8,16],[7,11,24]],
"window": [13,[8,28],[12,16],[5,6,17,20]],
"entonc": [[7,13,23,25]],
"decida": [19],
"personaliz": [7],
"mención": [0],
"omegat.pref": [8],
"previo": [16,17,13],
"tan-bueno-como-pueden-s": [26],
"inupiaq": [2],
"fao": [2],
"interpretará": [18],
"fas": [2],
"localizando": [[4,28]],
"previa": [10],
"presentar": [4],
"bartko": [3],
"presentan": [7],
"vuelv": [[8,16],[12,13,17,26]],
"contorno": [13,28],
"utiliza": [26,13,25,5,[7,16],[15,17,22],[8,9,18,19,23]],
"vuelva": [12],
"borra": [[16,25]],
"pt_pt.dic": [23],
"bastant": [[12,17]],
"italiano": [2],
"tercer": [26,12],
"usuario": [13,8,28,26,17,[4,16],[15,27],5,25,24,14,[3,7,9,20]],
"level1": [[14,25]],
"vuelo": [25],
"level2": [14,25],
"suma": [8],
"determinar": [23],
"alinear": [[9,17]],
"clases": [27],
"útile": [[5,20],[14,25]],
"web": [13,4,28,[15,27],[2,5,8,14,17,26]],
"subdirectorios": [27],
"actualización": [[10,12,16,25]],
"compartirs": [14],
"irlanda": [2],
"produciendo": [20],
"cuando": [25,[8,16],[12,26],[13,24],[5,9,11,17],7,[1,14,22],[18,19,20,21,23]],
"gris": [14],
"vece": [20,[11,26],[7,8]],
"entorno": [[13,17],8],
"vuelta": [9],
"samoa": [2],
"usar": [[13,14],16,[5,7,9,21],[12,15,19,22,25,26,27]],
"pt_br.dic": [23],
"usan": [19],
"omitirá": [7],
"golosina": [13],
"propiedades": [27],
"operaciones": [27],
"carácter": [20,[1,7],22,[11,12,13,16,21,27]],
"cobertura": [10],
"estado": [26,[11,16],17,[0,8,9,13,19]],
"unabridg": [6],
"contabilidad": [26],
"dice": [13,[17,26]],
"pesar": [[19,22]],
"dict": [6],
"ninguno": [16],
"dispon": [13,[7,16,17,23],26,5,[10,12,18,20,22]],
"orient": [[18,28]],
"tratada": [[5,21]],
"producen": [25],
"rastreador": [0],
"direccion": [13],
"traduzca": [[9,14,16]],
"petición": [4],
"codificacion": [7],
"fulah": [2],
"hund": [12],
"nnnn": [26,13],
"tratado": [26],
"mencionó": [9],
"supr": [1],
"gama": [[5,21,23]],
"probar": [[9,20]],
"huriaux": [3],
"wix": [9],
"pieza": [[5,7]],
"mantien": [12],
"copiado": [23],
"ninguna": [26,[3,9,13,20],[8,12,16]],
"paquet": [13,8,[9,17]],
"dónde": [[5,8,13,17,25]],
"txt2": [18],
"mantén": [12],
"nya": [2],
"txt1": [18],
"archiv": [13],
"añadirla": [13],
"olvidast": [6],
"user": [8],
"volverla": [25],
"sugerencia": [15,25,[12,16,23,24,26]],
"sitio": [4,[8,14,15,23,28]],
"extraído": [8],
"reflejar": [13,11],
"fij": [2],
"inflexión": [12],
"fin": [22,1,[2,7,16,21]],
"kinyarwanda": [2],
"hausa": [2],
"b0": [11],
"b1": [11],
"b2": [11],
"garantizando": [11],
"permiten": [[7,13,22]],
"límites": [27],
"tabuladora": [12],
"armenio": [2],
"aa": [2],
"ab": [2],
"alfabeto": [[17,18]],
"aparicion": [23],
"sure": [22],
"ae": [2],
"af": [2],
"declaración": [7],
"posterior": [[3,25],[9,11]],
"ak": [2],
"constituy": [7],
"al": [26,13,[16,25],11,28,[1,14],8,[9,24],[4,12,17],[15,20,21],[5,7,19,23],[18,22,27],[0,10]],
"am": [2],
"an": [20,2],
"ar": [2],
"as": [2],
"abreviado": [[5,11]],
"av": [2],
"ay": [2],
"wln": [2],
"az": [2],
"capítulos": [27],
"ba": [2],
"be": [2],
"bg": [2],
"ello": [14,[8,13],[5,12,15,17,20,23,25,26]],
"dicha": [9],
"importar": [26,[16,25]],
"bh": [2],
"bi": [2],
"inicialment": [[8,15]],
"complejo": [9],
"filters.xml": [8],
"sencillos": [27],
"bm": [2],
"bn": [2],
"proyectos": [27],
"bo": [2],
"anterior": [26,25,[5,12,16],23,[6,20],[0,1,8,11,13,15,17,18,22],[2,3,4,7,9,10,14,19,21,24,28]],
"separando": [5],
"instalando": [13,28,[23,27],[14,26]],
"br": [7,[2,13]],
"bs": [2],
"dependiendo": [26,[11,13,16,25]],
"acepta": [[25,26]],
"sitios": [27],
"segmentation.conf": [17,[8,13]],
"salto": [[7,12,22],[9,11,20]],
"panel": [26,28,12,27,13,[9,14,15],[1,16,24]],
"iniciará": [13],
"funcionará": [23,17],
"ca": [[2,13,15]],
"cd": [13,17],
"clave": [21,[1,5,26],[9,12,20,27]],
"ce": [2],
"ch": [2],
"galé": [2],
"cn": [13],
"coincidencias": [27],
"co": [2],
"cr": [2],
"cs": [2],
"cu": [2],
"cv": [2],
"maestra": [7],
"ella": [12,[7,13,16,18,22,26]],
"cx": [20],
"cy": [2],
"esperaba": [[4,16]],
"recién": [[12,14]],
"categoría": [[20,26]],
"da": [[2,13,17]],
"somalí": [2],
"siendo": [25],
"de": [26,28,13,25,8,16,9,7,11,22,5,17,27,12,15,23,21,20,18,14,4,24,19,10,3,0,1,2,6],
"constituyent": [20],
"duplicado": [11,26],
"explicacion": [12],
"separada": [16,26],
"f0": [16],
"f1": [16,26,28],
"reflejen": [8],
"f2": [13],
"f3": [16,28],
"dr": [22],
"f5": [16],
"encontrar": [[4,13],[5,8,12,17],[0,20,23,25,26]],
"incorporado": [23],
"dv": [2],
"wol": [2],
"dz": [[2,6]],
"permitir": [[7,17,24,25]],
"separado": [12,[7,8,9,22]],
"eb": [12],
"familiaric": [17],
"ee": [2],
"ejecutan": [13],
"u000a": [20],
"selecciona": [13,[7,16],[21,23,26],[12,17],[1,18,22,25,28]],
"el": [26,13,25,16,12,11,17,[7,8],23,9,20,22,24,14,[15,21],1,5,[18,28],4,27,10,[6,19],3,2,0],
"belazar": [15,28,27],
"en": [13,26,25,16,17,12,9,11,[8,21],22,23,14,5,7,15,18,20,28,[1,4,24],[19,27],6,[0,10],2,3],
"learned_word.txt": [8],
"eo": [2],
"es": [[13,26],25,8,[11,12],9,[15,16,17],[5,7,23],21,[18,22],24,20,[3,19],[2,4,6,10,14,27]],
"u000d": [20],
"et": [2],
"u000c": [20],
"eu": [2],
"pareja": [11,[15,26,28]],
"tendrán": [16,9],
"carro": [20],
"algunos": [27],
"aprendizaj": [15],
"verificación": [22,[7,23]],
"mediant": [[9,12,15]],
"fa": [2],
"cambiarla": [14],
"almacenarán": [23],
"ejecutar": [13,[17,26],14],
"ff": [2],
"stats.txt": [8],
"u001b": [20],
"indic": [5],
"fi": [2],
"fj": [2],
"origin": [24,26,[9,11,16],[14,25],19,[5,7,12,21,28]],
"vocal": [20],
"for": [14],
"fo": [2],
"confirma": [[12,13,16]],
"fr": [13,[17,23],[2,15,22]],
"content": [13],
"clase": [20],
"marathi": [2],
"fy": [2],
"inuktitut": [2],
"applescript": [13],
"ga": [[2,15]],
"conversión": [[9,15,18,28]],
"gb": [13],
"gd": [2],
"mostrando": [21,[12,16,27]],
"rato": [23],
"macosx": [8],
"presenta": [[1,8,11,13]],
"seguimiento": [[4,26]],
"resultado": [21,[11,15,20,25,26]],
"gl": [2],
"gn": [2],
"i0": [11],
"i2": [11],
"ayud": [4],
"aeiou": [20],
"gu": [2],
"gv": [2],
"claro": [25],
"catalán": [2],
"arrastrando": [13],
"sustituir": [16,26],
"generación": [15],
"equipo": [[13,18,25],17],
"rodean": [14],
"ha": [[11,13,16],[12,15,23,25],[0,2,5,7,8,17,19,24]],
"añad": [[12,18]],
"he": [[2,13,17]],
"hh": [17],
"hi": [2],
"duser.languag": [13],
"actualiment": [9],
"completo": [7,[1,5,15]],
"ho": [2],
"hr": [2],
"tab-delimit": [12],
"ht": [2],
"hu": [2],
"hy": [2],
"hz": [2],
"fra": [2],
"oci": [2],
"verd": [26,16],
"coinciden": [25,[12,19,20,22,26]],
"ia": [2],
"salvó": [[17,25]],
"briel": [[0,3]],
"id": [2],
"ie": [2],
"fri": [2],
"ig": [2],
"consecuencia": [[8,13,19,21,23]],
"project_stats.txt": [26],
"ii": [2],
"ik": [2],
"io": [2],
"termin": [13,25],
"ip": [13,28],
"ir": [[4,14,26]],
"is": [2],
"it": [[2,22]],
"iu": [2],
"entró": [1],
"perfil": [23],
"ja": [[2,13,25]],
"multiterm": [12,28,27],
"jc": [0],
"idoneidad": [3],
"odp": [9],
"odt": [9],
"montaj": [15],
"librari": [8],
"analiza": [[20,22]],
"jp": [18],
"origen": [12,[5,19]],
"jv": [2],
"pued": [13,26,25,[8,9],[12,17],23,21,[5,11],14,[7,22],16,[4,10,18,19,24],3,[0,6,15,20]],
"deshac": [16],
"inserción": [26],
"maxym": [3],
"ka": [2],
"un_espacio": [16],
"artículo": [4],
"omitido": [[13,20]],
"siguient": [20,16,13,[12,26],[8,14,22,25],[5,6,7,9,15,17,18,21],[1,10,23,24],[0,2,3,4,11,19,27]],
"kg": [2],
"robusta": [17],
"ki": [2],
"kj": [2],
"kk": [2],
"deberían": [16,22],
"kl": [2],
"km": [2],
"uyghur": [2],
"kn": [2],
"tamaño": [9,[10,17],[11,16,26]],
"ko": [2],
"acced": [26,13,8,[5,6,10,17]],
"kr": [2],
"ks": [2],
"ku": [2],
"acces": [[8,26]],
"kv": [2],
"kw": [2],
"ky": [2],
"completa": [13,[12,16,21,26]],
"físicament": [23],
"la": [26,13,16,8,11,25,9,15,12,22,[5,7],[21,28],17,23,[14,20],[3,18],19,[1,4,27],24,6,0,10,2],
"lb": [2],
"fue": [[0,26]],
"elementos": [27],
"le": [13,[5,14,16,17,22]],
"lg": [2],
"ful": [2],
"llena": [25],
"li": [2],
"dswing.aatext": [13],
"frecuent": [[4,5,13]],
"genera": [9],
"ln": [2],
"lo": [13,25,26,[8,12],[9,11,22],[3,4,7,15,18],[14,23],[5,16,17,20,21],[2,19,24]],
"corresponden": [[25,26]],
"copiará": [7,14],
"enlac": [26,[4,11],[7,8]],
"ls": [8],
"lt": [2],
"lu": [20,2],
"dist": [13],
"lv": [2],
"dada": [[21,25]],
"mb": [13],
"marshal": [2],
"me": [0],
"entra": [13],
"mg": [2],
"romper": [22],
"mh": [2],
"coincidan": [[12,20]],
"mi": [0,2],
"efecto": [[16,22,26],7],
"purament": [9],
"mk": [2],
"necesidad": [[15,23,24,25]],
"ml": [2],
"mn": [2],
"mr": [22,2],
"ms": [[2,11,22]],
"mt": [25,[2,8,21,26]],
"verá": [8],
"pashto": [2],
"wxl": [9],
"escrib": [13,12,[16,21,26]],
"ruso-bielorruso": [15],
"ampliar": [22],
"my": [2],
"respectivament": [12,[15,25]],
"javané": [2],
"na": [2],
"nb": [2],
"itera": [26],
"nd": [2],
"ne": [2],
"editar": [7,[16,28],25,[8,12,13,14,24,26,27]],
"ng": [2],
"editan": [5],
"marcada": [16,[12,22,23]],
"ni": [[15,25],13],
"nl": [2],
"nn": [[2,17]],
"navegador": [26,28,8,[13,16]],
"segmentador": [26],
"no": [13,[12,16],26,[11,25],[17,23],[9,14,19,21,22],8,[1,7,15,20],0,18,24,[2,4,6]],
"nr": [[2,12]],
"marcado": [25,9],
"nv": [2],
"sean": [[5,14,25]],
"ny": [2],
"húngaro": [2],
"oc": [[2,15]],
"od": [9],
"of": [6],
"oj": [2],
"consejos": [27],
"reserva": [13],
"ok": [14],
"om": [2],
"estrictament": [[11,26]],
"reformateo": [15],
"iniciast": [25],
"or": [2],
"sustituy": [16,25],
"os": [13,28,[2,5,9,12,27]],
"opcion": [7,[16,28],26,24,[13,17,22,23],[10,15,18,21,25],[9,20]],
"ot": [9],
"project_save.tmx.añomesdiahhnn.bak": [17],
"dado": [[11,13,15,17,21,22,25]],
"oji": [2],
"pa": [2],
"regulares": [27],
"pc": [13],
"descanso": [23],
"instalar": [28,[13,23],6,[5,8,16,27]],
"determina": [[8,13]],
"pi": [2],
"pl": [[2,12]],
"propósito": [28,25,3,[13,17,24]],
"traduccion": [25,5,26,22,[7,16,20,24]],
"po": [7,9],
"sesión": [19,[8,28]],
"mejorando": [13],
"ps": [2],
"pt": [[2,13,15,23]],
"startdict": [6],
"preferent": [16],
"inclus": [20],
"iglesia": [2],
"desaparec": [23],
"subdirectorio": [8,28,25,5,13,[6,12,17,23,24,26]],
"gracia": [0],
"obtendrá": [25],
"correo": [4,8],
"correr": [21],
"aplican": [22,13],
"incluy": [[13,21],[3,7,8,15,20,23,25,26]],
"colocar": [23],
"qu": [2],
"edit": [1,19],
"x_windows_without_jre.ex": [13],
"firmeza": [15],
"escenario": [[12,26]],
"siempr": [11,25,[9,12,22]],
"establecen": [22],
"estructur": [15],
"seguirán": [25],
"iniciado": [[19,20]],
"concepto": [26],
"rm": [2],
"rn": [2],
"ro": [2],
"precedida": [20],
"estructural": [22],
"ru": [2],
"rw": [2],
"activado": [16,[7,15]],
"sustituto": [13,28],
"estructurar": [22],
"desarrollar": [20],
"sa": [2],
"avpág": [1],
"sc": [[2,20]],
"faltarían": [0],
"sd": [2],
"se": [26,13,[16,25],[8,12],11,17,22,9,7,15,[21,24],[1,5],14,23,19,[3,6],[2,4]],
"nynorsk": [2],
"sg": [2],
"si": [13,25,16,7,[17,26],23,[11,12,14,15],22,[8,9,21],[1,5],[3,4,6,18,19,20],[2,10,24]],
"exportación": [25,12],
"sk": [2],
"sl": [2],
"auxiliar": [25,[9,26]],
"samuel": [[0,3]],
"sm": [2],
"despué": [22,12,11,14,[8,13,17],[1,7,16,20,21]],
"sn": [2],
"so": [2],
"lectura": [0],
"sq": [2],
"sr": [[2,22]],
"creará": [13,[8,17,19]],
"apart": [26],
"ss": [2],
"st": [2],
"su": [25,7,[0,16,26],[8,9,13],14,[6,12,22],[5,17,18],[1,2,4,15,19,21,23]],
"sobrescrib": [[16,24]],
"español": [15,[2,26]],
"sv": [2],
"sw": [2],
"constan": [11],
"norma": [25],
"inmediata": [[10,16]],
"ta": [15,[2,5]],
"alimentación": [20],
"te": [26,[13,16],[7,24],[5,8,22,25],[17,23],[2,9,11,12,19,20]],
"tg": [2],
"retorno": [[16,20]],
"ocurra": [22],
"th": [2],
"suficient": [[7,8,13,22]],
"ti": [[2,19]],
"pierda": [23],
"tk": [2],
"tl": [2],
"puls": [14,16],
"tm": [25,26,[21,28],14],
"tn": [2],
"to": [2],
"comilla": [12],
"tr": [2],
"ts": [2],
"solament": [13],
"tt": [2],
"tu": [13,23,25,26,8,5,[7,22],[4,10,11,16,17,18,19,21],[12,15]],
"enviando": [4,8],
"tw": [[2,13]],
"ty": [2],
"lugar": [[8,13,25],[14,16,23],[4,11,12,18,22]],
"corrig": [[11,26]],
"hmxp": [9],
"seleccion": [14,13],
"extraer": [[6,7,8,22,23]],
"puesto": [[8,9,13,17,18,19,23,26]],
"countri": [13],
"ug": [2],
"trivial": [25],
"uk": [2],
"yahoo": [[4,8]],
"coincidir": [[7,19]],
"dale": [8],
"un": [8,13,12,[16,25],[11,14,22],[17,26],23,20,7,[5,15],21,[4,9,19],[18,24,27,28],[0,1,3],6],
"calcula": [26],
"cuadro": [7,[10,16,17,18,19,20,21,24,25],[8,12,22,26,27]],
"ur": [2],
"aquello": [[12,25]],
"uz": [2],
"serbio": [2],
"aplicacion": [13,[5,16,23]],
"retroceso": [1],
"va": [1,11,[5,15,19]],
"ingresar": [[4,20,21]],
"bilingü": [[5,15,25]],
"validando": [28,11,27],
"tardar": [23],
"ve": [[13,20,25],[8,9,24],[2,3,12,15,16,17,21,26]],
"acelerar": [25],
"vi": [[2,13]],
"considerar": [[11,25],22],
"vo": [2],
"region": [7],
"operador": [20],
"trabajando": [[5,11,18,28],[25,27],[10,16]],
"libr": [3,[6,12,15,23]],
"preview": [12],
"escapars": [13],
"resolución": [15,[27,28]],
"albané": [15],
"alternando": [9],
"despliega": [10],
"arrastrar": [26],
"wa": [2],
"empezado": [17],
"omegat.sourceforge.net": [13],
"sé": [13],
"wo": [2],
"sí": [13,[1,12,16,20,25]],
"reconociendo": [5],
"distribuy": [3],
"licens": [[3,8]],
"emac": [13],
"ori": [2],
"orm": [2],
"xf": [13],
"xh": [2],
"venda": [2],
"superior": [26,22,[4,8,13,16,20,21]],
"mostrarán": [12],
"xp": [8],
"interé": [[5,15,17,25]],
"xx": [13,7],
"xy": [20],
"sourc": [14],
"esloveno": [[2,15]],
"límite": [20,1],
"salir": [16,[8,25]],
"tú": [[18,23]],
"ya": [26,25,[13,19],[11,22],[5,8,12,17,23,24],[7,15,16,21]],
"oss": [2],
"yi": [2],
"osx": [8],
"yo": [2],
"madagascar": [2],
"burmes": [2],
"noruego": [2],
"como": [[13,26],[5,25],8,9,[12,15,22],[7,18,21,23],[14,16,17,20],[6,11],[0,4,19,24]],
"yy": [7],
"empezast": [25],
"za": [2],
"otp": [9],
"chichewa": [2],
"zh": [2],
"ott": [9],
"coma": [20,12],
"exist": [[12,17,25,26]],
"eslovena": [15],
"ucrania": [2],
"vasco": [2],
"zu": [2],
"usarlo": [[6,12]],
"zz": [13],
"izquierda": [28,9,1,12,[22,26,27]],
"yiddish": [[2,15]],
"parecida": [24],
"busca": [21,20,[6,23,25]],
"utf8": [[12,18],9],
"reproducirlo": [4],
"excepto": [20,10],
"archivostraducido": [17],
"tanto": [25,[13,22,26],[5,8,9,11,12,21],[14,17,18,20]],
"columna": [12,26,11],
"separars": [11],
"dané": [2],
"representado": [11],
"darl": [14],
"funcion": [26,[14,16],[5,8,21]],
"power": [7],
"ayudar": [[4,5]],
"aparecen": [[8,14,16,21,24]],
"acumulado": [[8,26]],
"aquí": [16,11,[0,5,8,12,13,17,26]],
"programación": [[13,16,17]],
"método": [13,21,28,[5,9,12,26]],
"indicacion": [9],
"hengst": [12],
"hemo": [26],
"maratí": [2],
"u0009": [20],
"xhh": [20],
"ajustar": [26],
"revis": [6],
"u0007": [20],
"ctrl-cambio-c": [24],
"xho": [2],
"oración": [14],
"construcción": [20,17],
"corr": [21],
"data": [8],
"xht": [9],
"independientement": [[16,26]],
"firefox": [23],
"lists.sourceforge.net": [4],
"creast": [23],
"utilizando": [[5,6,12,13,15,18,24]],
"garantía": [3],
"translate": [27],
"cosa": [14,[4,8]],
"dato": [17,15,12,[8,24,25,28],[5,9,13,19,26]],
"automática": [15,16,[26,28],[12,27],[5,11,23]],
"permitan": [11],
"anidando": [[11,28],27],
"comprimido": [8],
"desplaza": [16],
"instalado": [13,23,8],
"apoyan": [12],
"sens": [22],
"venir": [11,26],
"ignore_words.txt": [8],
"oxt": [23],
"automático": [[17,28],[8,27]],
"apoyar": [4],
"activada": [[8,26]],
"canguro": [5],
"instalada": [13],
"dicho": [[9,25]],
"openoffic": [[9,23]],
"ejecut": [13,8],
"justificación": [9],
"ejemplo": [25,13,12,[11,20],17,[7,28],[18,22,26],[9,23],[6,15,21],[5,27],[10,14,16,24]],
"dará": [8],
"restablec": [7],
"concreto": [13],
"evaluación": [[15,26]],
"salida": [[9,21]],
"exportan": [24],
"exportar": [24,16,[12,25,28]],
"léxica": [15],
"continuar": [14],
"nota": [7,20,17,[10,12,14,15,16,21,22,25]],
"xx-yy": [7],
"actualizando": [28,[13,25],27],
"will": [0],
"costo": [26],
"nort": [2],
"considera": [[8,9,26]],
"xlf": [9],
"frase": [22,5,21,[19,28],[7,11,12,14,15,20,25]],
"saturar": [21],
"ocurrencia": [21],
"reconoc": [[5,14,24,25,26]],
"elimin": [9],
"incorrectament": [26],
"moderno": [2],
"buen": [[0,4],[8,22]],
"basta": [12],
"xml": [9,8,7,[11,12,15]],
"menor": [[9,11,12,13,22,25]],
"persistent": [0],
"gla": [2],
"beginn": [14],
"gle": [2],
"pedir": [25],
"glg": [2],
"abierta": [[16,21,26]],
"arrancar": [13],
"glv": [2],
"junto": [26,[13,23]],
"solicitado": [13],
"aplicará": [11],
"ofreciendo": [26],
"seri": [8,[5,25],20],
"tar.bz": [6],
"abierto": [21,12,[7,15,22]],
"xltx": [9],
"encontrado": [[9,21]],
"registrar": [4],
"encontrada": [13,0],
"enésima": [16],
"dividir": [22],
"compilan": [15],
"alternar": [9],
"xlsx": [9],
"modificación": [16,[7,8,12,25]],
"fuent": [28,26,25,7,16,[8,12],9,[15,22],[13,18],21,[17,19],[1,5,6,11,20]],
"aprobado": [12],
"muev": [16,[1,26],[12,22,24]],
"suelta": [[5,13]],
"rápido": [13,5,[0,3,25]],
"creada": [[5,26]],
"gnu": [3,8],
"financiero": [28],
"component": [16],
"creado": [[8,16],[9,12,14,23,25]],
"difieren": [13],
"target.txt": [24],
"boton": [26,21],
"ojibw": [2],
"extienden": [1],
"entrenador": [20],
"nameon": [7],
"introducir": [24,[12,19]],
"pan": [2],
"reproducir": [11],
"par": [11,15,14,[10,12,17,19,22,26,27]],
"tar.gz": [13],
"gpl": [6],
"trados": [27],
"inglé": [20,[0,2,12,13,15]],
"especial": [22],
"omisión": [16,[7,13]],
"tagalo": [2],
"será": [13,[25,26],[5,9,21]],
"lisa": [12],
"azul": [26,[11,21]],
"obsoleta": [9],
"chino": [[2,13,16]],
"numeroso": [[4,7]],
"formato": [9,11,28,12,[14,25],18,[5,7],[26,27],[15,16,21],[8,10,13,22,24]],
"rashid": [3],
"garantiza": [[5,11]],
"prefier": [3],
"detección": [9],
"numerosa": [[5,9]],
"navegar": [15,26],
"ocultado": [16],
"nauru": [2],
"diferencia": [26,[15,21]],
"descargast": [8],
"grn": [2],
"nyanja": [2],
"xtg": [9],
"bindownload.cgi": [13],
"requerido": [6],
"lado": [[11,13,15],[7,18,22,26]],
"obsoleto": [26],
"ruptura": [22,28],
"contienen": [[5,9,21],[18,26],[8,12,22,24,25]],
"bislama": [2],
"albania": [2],
"hexadecim": [20],
"traduc": [26,[7,17],25,[8,22,24]],
"viceversa": [9],
"agregarán": [7],
"leeme.txt": [7],
"concisa": [0],
"paí": [[13,25],7,22],
"reproducen": [[8,11]],
"manteniendo": [0],
"almohadilla": [13],
"descomprim": [13],
"insuficient": [15],
"público": [8],
"producida": [25],
"incorrecta": [11],
"razón": [[8,13,23]],
"recordatorio": [14],
"empiezan": [20],
"esperanto": [2],
"acostumbr": [26],
"guj": [2],
"instala": [12],
"instrucciones": [27],
"regexp": [[13,17]],
"volverá": [[7,8]],
"relación": [26],
"tailandia": [2],
"irregular": [8],
"amárico": [2],
"únicament": [21,7,[13,24,25]],
"uhhhh": [20],
"esclavo": [2],
"pública": [3],
"samskrta": [2],
"suavizado": [13],
"inconsistent": [25],
"palabra": [21,26,12,[16,20],5,[1,8],[13,14,23],[9,11,15,22,28]],
"ordenes": [27],
"guardando": [[12,27]],
"accidentalment": [11],
"ant.apache.org": [13],
"pie": [7],
"tarea": [9],
"pseudotranslatetmx": [25],
"listado": [14],
"desea": [25,13,7,26,[12,15],[4,18,21,23]],
"verbo": [[12,15]],
"término": [12,26,3,[1,8,14,21]],
"arno": [3],
"representa": [13],
"desde": [27],
"panjabi": [2],
"instruido": [7],
"targetlanguagecod": [7],
"nuosu": [2],
"gráficos": [27],
"pega": [16],
"asegurándot": [12],
"primero": [4,[12,13,14,16,22,23,26]],
"causa": [13,12],
"entrar": [16,26],
"aceptar": [[13,15]],
"podría": [14,[0,11,17,22,25]],
"diccionario": [23,28,6,26,5,[10,12,21]],
"corrección": [23],
"primera": [[12,13],[16,20]],
"agradezco": [0],
"extra": [[20,23]],
"diseño": [26],
"tradujo": [25],
"deben": [[13,22],[9,17],[7,20,23,24]],
"recuerda": [[4,11,16]],
"perdido": [[17,25]],
"deseada": [[16,26]],
"tien": [13,11,[9,12,18,26],8,[3,16,23,25],[5,17,22]],
"combinar": [[13,21]],
"porqu": [25,14],
"avanzar": [16],
"pli": [2],
"consecutivo": [[5,11]],
"seccion": [[11,21]],
"conservando": [8],
"encyclopedia": [6],
"jerarquía": [8],
"alicia": [25],
"deseado": [13,15],
"simplifica": [[9,11]],
"reflejando": [8],
"miproyecto": [25],
"agregado": [12],
"libertad": [11],
"europea": [25],
"suficientement": [25],
"agregada": [7],
"pt_br": [23,13],
"cuantificador": [20],
"reviert": [16],
"a-z": [20],
"zoltan": [3],
"asunto": [4],
"apoyando": [4],
"tomando": [26],
"contengan": [11],
"preservar": [7],
"png": [13],
"bueno": [15],
"intervienen": [23],
"manera": [13,[7,9],[12,22,25],[17,21],[1,4,6,8,14,16,18,20,23]],
"mediawiki": [[16,26]],
"komi": [2],
"caracter": [[1,12,20],[7,18],[8,11,21,26],[9,13,16]],
"utilizamo": [23],
"join.html": [8],
"instruy": [17],
"buena": [[20,26]],
"adelant": [[16,25],5],
"compilador": [9],
"incapaz": [17],
"pod": [9],
"superposición": [11],
"facto": [26],
"pol": [2],
"tomada": [16],
"posicion": [9],
"etiqueta": [11,28,26,16,9,14,7,25,[5,10,17,22,27]],
"por": [25,26,[12,13],22,8,[7,20],[9,15,16,21],[11,18],[5,17,23],0,[19,24],[2,3,6,14],[1,4,10,27]],
"voluntaria": [4],
"incluyendo": [[8,26],15],
"pena": [23],
"nombr": [7,[13,26,28],25,[8,14,23],[17,24],[2,6,9,12,16,18,19,21,22]],
"corrigiendo": [0],
"seguridad": [25,[17,28],[8,13]],
"agrupan": [11],
"coincidirá": [20],
"encontrará": [21],
"googl": [15,28,[5,13,21,26]],
"opendocu": [7,9],
"duplicando": [[11,28]],
"predefinidas": [27],
"muestran": [[12,21],26,16,[5,9,13,17]],
"continuo": [4],
"travé": [26,[4,16],[10,12,13,17,20,21,24]],
"actualizar": [25,[5,13,19]],
"sourceforg": [4,[8,28]],
"buscando": [17],
"han": [26,[15,25],[11,13,23]],
"permitirá": [13],
"actualizan": [13],
"hat": [2],
"realiza": [15],
"hau": [2],
"haz": [13,16,7,26,[23,25],[4,8,17,22],[11,12,19,21]],
"hay": [13,12,[23,26],[16,25],[5,6,9,14,17,18,22,24]],
"encuesta": [8],
"determinada": [25],
"peor": [11],
"cuatro": [[12,25]],
"correspondería": [21],
"finalizar": [[14,22]],
"determinado": [[7,8],25],
"agrupadas": [27],
"sindhi": [2],
"technetwork": [13],
"canadiens": [22],
"descargado": [13,8],
"retroalimentación": [26],
"pulsar": [[14,21,26]],
"plural": [12],
"detecta": [[11,12,13,26]],
"manipula": [24],
"pero": [[1,12,21,25],[11,20],[3,13,14,23,26],[0,5,8,15,16,19,22]],
"comienzan": [13],
"proporcionar": [22],
"desplazart": [22],
"proporcionan": [8],
"exporta": [24,16,[12,14,25,27,28]],
"técnicament": [7],
"comprobación": [20],
"confirmar": [16],
"medida": [26,[9,11,25]],
"constituyen": [8],
"windows": [27],
"esperanza": [3],
"cruzada": [13],
"interlingu": [2],
"embargo": [13,[11,14,23,25,26],[0,8,9,12,15,17,18,19,22]],
"calculado": [26],
"totalment": [25,11],
"tipo": [28,10,[9,17],[16,26],[7,13,27],[11,22],[8,12,18,24,25]],
"heb": [2],
"brune": [3],
"listar": [5],
"tras": [[13,14,23]],
"localizada": [17],
"kanji": [18],
"reparar": [[11,25]],
"coincidiría": [21],
"pus": [2],
"keith": [3],
"her": [2],
"readme_es.txt": [8],
"trae": [26],
"resultados": [27],
"abriéndolo": [5],
"resultant": [13,17],
"miscelánea": [28,5],
"lee.m": [7],
"hayan": [[14,25]],
"visita": [15],
"separa": [[12,22]],
"utilizará": [[13,23],17],
"desglosada": [26],
"anfitrión": [13],
"tecnología": [13],
"eliminarlo": [11],
"maneja": [9,[21,22]],
"esperar": [13],
"consultado": [5],
"combinado": [7],
"doc-license.txt": [8],
"eclesiástico": [2],
"yid": [2],
"copyflowgold": [9],
"aprenda": [14,[5,13,27]],
"manejo": [[5,26]],
"nombre_proyecto-omegat.tmx": [[8,25]],
"editor": [1,12,[7,9,13,14,17,21,27],[2,3,5,11,16,18,26]],
"pseudotranslatetyp": [25,13],
"análisi": [[15,16]],
"hhc": [9],
"hecho": [[0,9,12,13,16,24,25,26]],
"pulsacion": [24],
"reconocen": [12],
"hhk": [9],
"conoc": [14],
"correctament": [[9,25],[11,12,13,18],[8,15]],
"eliminarla": [14],
"intentará": [13],
"ejecutándolo": [28],
"sustituyen": [16],
"small": [12],
"javaapplicationstub": [13],
"hin": [2],
"deseará": [8],
"importa": [13,[9,16,17]],
"únete": [4],
"ofrezca": [12],
"hecha": [25],
"sobr": [5,13,[8,25],[9,22,26],[0,2,14,16,17,19,20,23,27]],
"tres": [25,[6,9],[4,8,11,12,14,16,18,21,26]],
"group": [8],
"archivo": [8,28,7,[9,13,25],26,12,[16,18],14,23,21,[5,17],[11,24],27,6,[4,22],19],
"reservada": [23],
"readme.txt": [3],
"letra": [[10,17],28,16,9,20,[25,26,27],[11,13],24],
"campo": [24,26,16,[7,12,13,28],[5,9,21,25,27],[10,17,19,23]],
"occitano": [2],
"source.txt": [24],
"cuál": [15],
"página": [[1,9,26],[4,7,8,15,16,22]],
"siband": [3],
"compleja": [[5,20]],
"tigrinya": [2],
"exchang": [12],
"detallada": [[8,14,26]],
"contribuyendo": [28,4,27],
"perderlo": [8],
"suavizando": [[13,28]],
"señalar": [9],
"denominada": [[5,22]],
"point": [7],
"explica": [5],
"procesa": [[13,15]],
"general": [8,3,11,[17,20,25,26]],
"l4j.ini": [13],
"identifica": [7],
"escapó": [13],
"localización": [9],
"generar": [14,25,[17,27]],
"temas": [27],
"iniciando": [13,[27,28]],
"proceso": [22,[5,8,11,17,24,25]],
"huérfana": [25],
"apunt": [26],
"prueba.html": [13],
"colección": [[20,23,26]],
"andrzej": [3],
"alternativa": [[18,24,26],[13,16]],
"proyecto_de_ejemplo": [8],
"project_save.tmx.añomesdíahoramin.bak": [25],
"huérfano": [25,28,[22,26]],
"fuera": [1,11],
"hábito": [8],
"citado": [[20,27]],
"normalment": [13,17,[9,11]],
"europeo": [22],
"dhttp.proxyhost": [13],
"diversa": [[14,25]],
"especifican": [7],
"hmo": [2],
"oculta": [7],
"especificar": [[13,17],8,[9,22]],
"período": [22],
"diverso": [17,[4,5,23,28]],
"barra": [26,13,[12,16,17,20]],
"marca": [24,[22,26],[7,23]],
"moldavan": [2],
"elimínalo": [26],
"maximizar": [26],
"yor": [2],
"tímidos": [27],
"producir": [[8,11,22,25]],
"corrector": [23,28,[16,27],[5,8],[15,17,21]],
"activa": [7,[5,15,16,21]],
"míos": [0],
"situación": [[18,25]],
"mucho": [9,[8,12,17,22,23,26]],
"contiene": [27],
"importarla": [25],
"unicode": [27],
"activo": [1],
"traductor": [[9,25],[4,22,28]],
"buscará": [14,[5,23]],
"acept": [[8,11,15]],
"buloichik": [3],
"integrar": [9],
"dejar": [24,9],
"extenso": [5],
"indoloro": [4],
"hoja": [7,[8,20,26]],
"releas": [13],
"griego": [20,2],
"peter": [3],
"comet": [[11,14]],
"segmentan": [22],
"desmarca": [22,15],
"segmentar": [22,[7,25]],
"sparc": [13],
"definido": [25],
"nunca": [7],
"consisten": [22],
"dirigirá": [8],
"indicador": [[19,20,26]],
"sandra": [3],
"financierament": [4],
"día": [25,[8,17],4],
"discurso": [15],
"rtl-ltr": [1],
"georgiano": [2],
"prefijada": [25],
"brasil": [[13,23]],
"pacient": [0],
"tale": [25,[5,17]],
"prior": [0],
"visualizar": [[12,16,17]],
"enlazarlo": [13],
"automáticamente": [27],
"abrirla": [26],
"traducciones": [27],
"simplement": [[4,8,12,13,22,26]],
"definida": [[9,13,18,25]],
"hrv": [2],
"dhivehi": [2],
"abrirlo": [[11,26]],
"pueden": [25,12,[5,26],[9,13,19],[8,22],[7,10,14,16,17,20,24]],
"coloca": [[23,25]],
"cargarlo": [12],
"directament": [13,[7,8,22]],
"tártara": [2],
"ubicacion": [[8,25]],
"dominio": [5],
"entr": [26,11,9,12,[20,21],[7,8,13,15,16,17,24,25]],
"relevancia": [26],
"temprana": [25],
"vilei": [3],
"asociada": [5],
"alterar": [13],
"más": [13,26,[17,20],[8,25],1,[9,12,14,21],[5,7,15,22],[3,16,18,23,24],[4,10,11,19,27]],
"divid": [26],
"asumir": [18],
"sola": [12,[11,21,25]],
"asociado": [13,[8,9]],
"solo": [[5,13,20]],
"procesador": [9],
"nueva": [13,[16,22],[20,28],[19,24,25,27]],
"tienen": [14,[6,7,9,11,16,20,21,23,26]],
"home": [8,13],
"encontraron": [26],
"nuevo": [14,13,[12,25],28,16,[7,8,22,26],[18,19,23,24,27]],
"eliminar": [11,[12,13,24,25]],
"recient": [[12,13,17,23,25]],
"árbol": [8],
"build.xml": [13],
"dirigirs": [16,28],
"mantenga": [25],
"hun": [2],
"creando": [28,[9,22],[16,27]],
"utilizada": [28,26,[10,13,19,20,21]],
"rehac": [16],
"lector": [5],
"fiabl": [22],
"moldavia": [2],
"visibl": [4],
"supuesto": [15,[1,9,12,13,18,19,23,25,26]],
"utilizado": [9,[8,11,17,23],[5,7,13,15,25,26]],
"x_windows.ex": [13],
"almacenada": [26],
"herero": [2],
"exposición": [26],
"división": [19],
"aligndir": [[13,17]],
"diligencia": [0],
"almacenado": [25],
"habitualment": [13],
"directorio-del-proyecto": [13],
"crean": [8,9],
"infix": [9],
"implementacion": [13],
"crear": [[12,25],13,8,14,16,[5,7,11,15,22,23,26,27,28]],
"puedan": [[12,25]],
"multipalabra": [26,[27,28]],
"tarbal": [6],
"hindi": [2],
"empieza": [0],
"omegat-development-request": [4],
"similitud": [[11,24,26]],
"hora": [25,[8,16,17,21,26]],
"producto": [[9,25]],
"importada": [25],
"anidado": [11],
"complejidad": [0],
"devuelv": [13],
"vistazo": [8],
"experiencia": [[11,19]],
"moneda": [20],
"operativo": [13,[7,16],12],
"múltipl": [[12,28]],
"herramientas": [27],
"despuésd": [22],
"tard": [8,[13,14,16]],
"meno": [28,[8,17,26],[0,5,9,13,15,19,25]],
"fiji": [2],
"quitarla": [11],
"unidad": [25,22,[5,16]],
"hye": [2],
"importado": [[25,26]],
"morfológico": [15],
"abrirs": [[14,26]],
"morfológica": [15],
"a-za-z": [20],
"probabl": [5,[13,15,17,25]],
"aparezca": [9],
"honorífica": [0],
"consonant": [20],
"apéndic": [[1,2,3],[0,4],[5,17,22,25,26,28]],
"enví": [15],
"radio": [[21,26]],
"errar": [11],
"automatizando": [7],
"source-pattern": [[13,17]],
"fine": [[7,26]],
"recogen": [25],
"descartar": [12],
"hogar": [17,13],
"chua": [3],
"turkmenistán": [2],
"definitivo": [19],
"problema": [28,12,[11,18],[6,15,25],[9,13,17,26]],
"asegura": [13],
"surtan": [[7,22]],
"convenient": [13,25],
"xliff": [9],
"hors": [12],
"true": [13],
"adicion": [[13,25],[2,12,23]],
"present": [[11,13],[5,6,7,17,25,26]],
"islandé": [2],
"identific": [25],
"evitar": [17,[5,9,22,25,26]],
"desempaca": [13],
"resuelven": [25],
"trata": [[6,7,8]],
"desactivar": [16,7],
"formateado": [28],
"especificado": [[13,21]],
"paneles": [27],
"apreciado": [4],
"hous": [12],
"kmenueditor": [13],
"menú": [16,[13,26],28,27,19,[5,14,18,22],[7,8,9,10,15,17,23,24]],
"realidad": [[11,13,15,26]],
"especificada": [21,[13,16]],
"debería": [[4,22]],
"leerlo": [8],
"segmenten": [9],
"atención": [11,13],
"desacopla": [26],
"abrirá": [[16,23,26]],
"kmenuedit": [13],
"cuidadosa": [12],
"chuang": [2],
"conocida": [[13,22,25]],
"blanco": [[7,20,22],[9,12,21,24]],
"akan": [2],
"writer": [12,9,14],
"editarlo": [26],
"dalloway": [22],
"insertarla": [[16,26]],
"dzongkha": [2],
"programa": [13,[14,16]],
"dependerá": [9],
"incluido": [8,[5,25]],
"utilidad": [[6,13,23]],
"computadora": [16],
"corregir": [[4,5,11,15,21,26]],
"trabajan": [[5,25]],
"práctica": [13],
"directorios": [27],
"ctrl-u": [14],
"nomenclatura": [28],
"sentido": [[18,23]],
"rutinariament": [13],
"porcentaj": [26],
"pulaar": [2],
"edita": [26],
"capítulo": [5,26,25,[0,3,8,20,21,22]],
"locmanag": [9],
"user.languag": [28],
"regex": [20,27,28],
"afectan": [11],
"meta": [20],
"abra": [14,[8,9,24]],
"incluida": [1],
"intacto": [0],
"proporcionada": [26],
"sango": [2],
"global": [7],
"afectar": [8],
"prueba": [13,[17,25]],
"expresión": [[20,21],[5,7,17,22]],
"alineador": [[17,28],27],
"free": [3],
"diversos": [27],
"práctico": [[8,13,26]],
"pretraducción": [28],
"cualquier": [25,8,[13,14,20,21],[12,26],[1,3,7,17],[5,9,22,23]],
"afecta": [11,13],
"valor": [20,13,25,[9,17,22,26]],
"ibm": [13],
"ibo": [2],
"homólogo": [7],
"introducción": [27,[5,14,15,28]],
"comun": [12,[26,28]],
"precaucion": [17],
"fuente": [27],
"numerando": [[25,28]],
"comodines": [27],
"jean": [3],
"tablas": [27],
"poco": [[8,11,13,20]],
"vaciar": [26],
"logro": [15],
"emparejada": [[11,16]],
"ajusta": [22],
"alemán": [[2,12]],
"ida": [9],
"presionando": [16],
"proporcionado": [16],
"ido": [2],
"idx": [6],
"dirigida": [15],
"que": [25,[13,26],8,12,23,[11,17],5,9,14,22,16,15,[7,21],19,18,24,4,20,[0,1,3,6],[2,27,28]],
"actualic": [17],
"estándar": [[12,26],[2,5,7,8,13,17,25]],
"intacta": [8],
"reconocido": [12],
"causar": [8],
"reemplazar": [16,[15,25,26]],
"inmediatament": [17],
"causan": [[11,26]],
"otro": [28,[8,9,13,26],25,[5,11,16,24],[4,6,14,15,17,22]],
"repeticion": [26,[5,16]],
"linux": [13,[8,28],12,[5,17,20,27]],
"rodea": [11],
"actualment": [[5,19,26]],
"exigir": [9],
"reconocida": [22],
"otra": [[13,25],16,[11,17],[7,12,26],[5,8,20,23]],
"icono": [13,8,[12,16]],
"dentro": [11,[13,14],[8,9,12,25]],
"inferior": [26,22,21],
"maldiva": [2],
"patrón": [7,22,28,20,18],
"macedonia": [2],
"elimina": [1,[11,23]],
"aterrizarán": [25],
"posibl": [9,25,[11,12,20],[0,5,8,13,15,16,19,23,24]],
"consigu": [13],
"zha": [2],
"ifo": [6],
"comprend": [25],
"zho": [2],
"octal": [20],
"segunda": [12,13],
"abreviatura": [22],
"sistema": [13,[12,16],[23,28],[7,15],[1,17,18]],
"inicio": [13,1,5,[0,3,9,25,28],[2,4,6,7,8,10,11,12,14,15,16,17,18,19,20,21,22,23,24,26]],
"consist": [[8,26]],
"borrador": [22],
"segundo": [[12,26],11,[5,25]],
"opciones": [27],
"transitivo": [12],
"antigua": [[13,16,19]],
"arranca": [17],
"antiguo": [2,19],
"cibl": [12],
"zip": [23,13],
"posiblement": [[9,13,22,23,25],[7,8,12,20,24,26]],
"refleja": [8],
"yahoogroups.com": [4],
"concis": [6],
"elección": [13,[7,8,11]],
"sdlxliff": [9],
"manipulacion": [11],
"asignan": [13],
"inicia": [13,17,24],
"asignar": [13],
"croacia": [2],
"qué": [7,[11,14,22]],
"extensión": [[7,12],18,23,[6,9],25],
"iii": [2],
"presionar": [26],
"pulsando": [26,[9,14]],
"conectado": [[13,15]],
"exportando": [25,28],
"tracker": [0],
"olvid": [11],
"japones": [18,[16,22]],
"manejado": [19],
"código": [25,23,28,2,7,[13,22],[1,4,5,8,15,18,27]],
"gestionar": [[11,27,28],[15,16]],
"subscrib": [4],
"hündin": [12],
"agregando": [28],
"grupos": [27],
"lingala": [2],
"excel": [7],
"debido": [[21,22]],
"tiempo": [26,[0,5,10,15,17,21,22]],
"regla": [22,28,5,11,9,[7,20,26,27],[8,13,15]],
"stardict": [[6,28]],
"omegat.l4j.ini": [13],
"intercambio": [12],
"diapositiva": [7],
"prefer": [[8,12,22]],
"crédito": [16],
"número": [26,[8,11],[9,16],[13,15],[7,10,21,25,28]],
"intercambia": [1],
"iku": [2],
"pijffer": [3],
"tecla": [16,1,[12,14,26],[9,13,24]],
"simpl": [12,[9,20],[5,15]],
"habilitar": [23],
"creol": [2],
"thunderbird": [23],
"ile": [2],
"verifica": [[2,16]],
"proporcionamo": [5],
"publicada": [3],
"inexistent": [19],
"desarrollo": [4,28],
"rápidament": [26],
"relativament": [[8,26]],
"mucha": [[8,12,21,24]],
"pone": [[14,26]],
"solucionando": [28],
"ruta": [13,17],
"corromp": [11],
"error": [11,26,25,13,14,[0,5,8,9,16,19]],
"negación": [20],
"marcador": [[7,26],[9,28]],
"momento": [14,[7,8,9,11,15,25,26]],
"proteg": [19],
"agradecimiento": [0,[3,28]],
"malayalam": [2],
"public": [[3,8]],
"ejemplos": [27],
"cuenta": [13,[19,23,25,26],8,[4,11],[14,15],[1,5,6,9,12,17,18,22,27]],
"complicado": [9],
"ina": [2],
"ind": [2],
"papel": [5],
"contra": [26,[7,12,23]],
"pt_br.aff": [23],
"oromo": [2],
"registrado": [[16,25]],
"actúan": [1],
"rusa": [13],
"kongo": [2],
"ini": [9,[13,28]],
"ubicar": [16],
"sinónimo": [5],
"establecido": [8],
"regresar": [16,14],
"ruso": [13,[2,17,18]],
"proced": [[5,6,9]],
"esfuerzo": [4],
"especificast": [23],
"restring": [21],
"dhttp.proxyport": [13],
"recomend": [9],
"trado": [12,28],
"clasificación": [26],
"subrip": [9],
"proporcionart": [8],
"vía": [4],
"operacion": [[11,28],[25,26]],
"docuwiki": [9],
"implementar": [13],
"comercial": [24],
"describ": [5,26,[1,16]],
"probablement": [[18,22,23]],
"recordar": [5],
"cómodo": [13],
"navajo": [2],
"espacio": [20,22,7,26,[5,8,9,11,12,13,16,21]],
"versión": [13,[3,25],15,[8,9],[0,4,17,23]],
"aparecerá": [[14,15,22]],
"persona": [0],
"diagrama": [7],
"ipk": [2],
"suced": [5],
"isla": [2],
"podrían": [[14,26]],
"frecuentement": [[26,28],[9,25]],
"copia": [25,[16,17],[13,28],[4,8,12,21,26]],
"efectivament": [[19,25]],
"aaa": [20],
"southern": [2],
"contemporari": [6],
"solari": [13,8],
"primer": [16,26,[11,22],[0,21,25]],
"preferida": [[16,26]],
"último": [16,[0,21,22,25]],
"manual": [26,[9,16],[0,3,4,8,28]],
"castellano": [2],
"aar": [2],
"kirundi": [2],
"funciona": [16,[12,13,23]],
"comercialización": [3],
"formatos": [27],
"oficial": [27],
"alternativament": [21],
"malté": [2],
"significado": [[11,21]],
"abc": [20],
"conten": [13,[9,17,25],24],
"arriba": [[1,8,13,21,23,25,26]],
"navaho": [2],
"generada": [[15,25]],
"abk": [2],
"recibido": [26],
"flexibilidad": [[21,22]],
"categorías": [27],
"textual": [25,5,22],
"abr": [16,26,25,17,[8,11,12,13,21],[1,7,14,15,18,24]],
"materi": [5],
"seleccionar": [13,[7,26],[8,10,14,16,17,22,23,24,28]],
"preferido": [26],
"referencia": [5,[25,26],[7,16,20,21]],
"adjuntará": [16],
"vacía": [[7,25]],
"isl": [2],
"iso": [2,28,18,27,[1,4,5,12,22,25]],
"encabezado": [7],
"log.txt": [11],
"contribuy": [[9,11,26]],
"bidireccion": [9],
"zul": [2],
"red": [[13,15]],
"asignarlo": [25],
"implícita": [3],
"vacío": [25,[12,22],[7,8,24,26]],
"ita": [2],
"traduciendo": [26,[8,14,17,25]],
"dir-de-mi-proyecto": [17],
"arrastra": [[13,26]],
"cambio": [16,[9,13],[1,22],[8,12,19,25],[5,7,11,17,21,24,26,28]],
"cambia": [18,[11,22,23],[16,25]],
"última": [16,[4,13,15,17,25,26]],
"mencionado": [13],
"construido": [15],
"larouss": [26],
"reglas": [27],
"acción": [16,[1,6,23,27]],
"solapar": [26],
"untar": [6],
"finlandé": [2],
"basada": [5,[15,22]],
"ahora": [[11,26]],
"benjamin": [3],
"benigno": [26],
"filters.conf": [17,13],
"enviar": [15],
"desinstalar": [23],
"invertida": [[13,20]],
"sánscrito": [2],
"basado": [15,[5,6,7,12,23,24]],
"reducir": [11,[9,26]],
"aff": [23],
"repositorio": [25,[0,8]],
"duda": [[8,15]],
"simplificación": [9],
"benigna": [11],
"advertencia": [13],
"afr": [2],
"indican": [11,[25,26]],
"mover": [[1,11,19]],
"saboga": [3],
"entidad": [21],
"legislación": [25],
"desacoplado": [16],
"targetlanguag": [7],
"dude": [[14,17]],
"indicar": [[9,23]],
"filtro": [7,28,18,[8,9,16,26],[5,12,14,19,22,25,27]],
"añadido": [[8,12]],
"memoria": [25,5,26,28,13,16,[8,14,19],[17,21],22,15,[12,18,20,27]],
"volverán": [22],
"cabo": [25],
"continuación": [13,[8,16,21,23]],
"properti": [9],
"título": [9],
"defecto": [13],
"durant": [13,19,[5,7,8,12,14,22,25,26,28]],
"cabe": [9],
"preprocesarlo": [25],
"copiar": [23,16,[14,26],[8,12,13,27,28]],
"procesado": [13,7],
"desplazándot": [26],
"pueda": [25,12,[9,13],[0,4,5,8,14,17,26]],
"multiplataforma": [5],
"refier": [[5,19,26]],
"selecciónalo": [[13,25]],
"script": [24],
"nada": [20,[1,15],[12,16]],
"ajust": [13,14,[8,17,19,25]],
"tabulacion": [12],
"khmer": [2],
"limburgué": [2],
"local": [13],
"resum": [26],
"ampliado": [15],
"kwanyama": [2],
"escrito": [18,25],
"mantendrán": [[7,13]],
"cree": [[2,14,17,22,25]],
"segmenta": [22],
"escrita": [16,[23,26]],
"aceptada": [25],
"crea": [8,[16,25],[11,13],[4,17,23,26]],
"mostrada": [16],
"reutilizando": [28,25,27],
"resto": [26],
"estén": [[12,14,16,25]],
"partida": [[0,4,22]],
"lento": [[16,17]],
"segmento": [16,26,25,11,1,24,[21,22],5,28,8,15,[9,12,14],7,[17,19],[10,27]],
"mostrado": [[10,20,26]],
"cada": [[7,9,25,26],[8,12,14,15,16],[5,17,22,24]],
"aka": [2],
"futuro": [[8,14]],
"filtros": [27],
"cita": [20],
"instalación": [13,23,[5,17,26,27]],
"movert": [16],
"consecuentement": [12],
"tsonga": [2],
"colócala": [25],
"configuración": [28,13,17,[16,26],[8,19,23],7,[12,21]],
"idéntico": [26,[13,21,25]],
"desplazaron": [16],
"chechenio": [2],
"usuarios": [27],
"correspond": [[8,18,21,23]],
"mode": [13,17],
"sugerirt": [5],
"cuánto": [[0,26]],
"modo": [13,17,9,28,26,[5,25,27]],
"umarov": [3],
"debajo": [25],
"deformatea": [15],
"ahí": [14],
"alt": [1,13],
"real": [[11,26]],
"típico": [[13,25]],
"sensato": [23],
"pregunta": [4,13],
"unit": [25],
"guaraní": [2],
"etiquetas": [27],
"registrart": [4],
"guion": [[13,24]],
"idéntica": [24,26],
"amh": [2],
"completar": [[5,8]],
"unix": [16],
"fondo": [[16,26]],
"suscribirs": [[8,14]],
"escritorio": [13,[21,23,26,28]],
"combinacion": [15],
"roh": [2],
"ron": [2],
"and": [[8,13,17]],
"modifica": [[8,19,23,25,26]],
"bengalí": [2],
"contar": [26,9],
"atajo": [16,28,5,[1,26],[2,3,13,19]],
"minuto": [[14,17],[0,5,13,16,25]],
"ant": [22,13,[8,11,25],[1,16],[9,12,14,18,23,26]],
"menudo": [14],
"comentario": [12,7,13],
"sardo": [2],
"facilitart": [[9,18]],
"párrafo": [7,22,5,[9,19,25]],
"afinando": [22],
"sundanes": [2],
"abriendo": [13,26],
"jnlp": [13],
"omegat.ex": [13],
"diferencian": [11],
"bloques": [27],
"pasar": [14],
"descomprimido": [13],
"invoca": [26],
"atrá": [16],
"kuanyama": [2],
"comodín": [[7,21]],
"compon": [[12,26]],
"llama": [13],
"implementado": [15],
"english": [6],
"lógicos": [27],
"jar": [13,[8,17],25],
"jav": [2],
"lanzarlo": [13],
"app": [13],
"desambiguación": [15],
"assames": [2],
"modificando": [28,19,27],
"capaz": [[5,26],[11,21,23,24]],
"sintaxi": [22,7],
"acto": [0],
"alex": [3],
"encima": [24],
"borrado": [11],
"establec": [16,22,[7,13,17]],
"manejar": [[5,7,9,12,21]],
"necesariament": [8],
"ejecutando": [13,28,8,[14,26,27]],
"zulu": [2],
"nivel": [25,22,[16,28],8],
"reacios": [27],
"oriya": [2],
"contadores": [27],
"fueran": [26],
"gujarati": [2],
"mitad": [22,8],
"seguida": [22,[20,25]],
"ara": [2],
"consideración": [[16,25]],
"arg": [2],
"niega": [11],
"seguido": [20],
"paypal": [4],
"portugué": [[2,13,23]],
"consta": [26],
"art": [23],
"subrayado": [12],
"razonablement": [[5,12]],
"rtl": [9,28],
"vmoption": [13],
"jdk": [13],
"asm": [2],
"algo": [13,[5,19,25]],
"cumplen": [14],
"glosario": [12,28,26,27,[5,14],16,[6,9,15,17]],
"run": [2],
"resulta": [8],
"rus": [2],
"tercera": [12],
"subrayada": [23],
"aymara": [2],
"duro": [[13,16,25]],
"defectuoso": [25],
"simplificar": [[11,26]],
"mostrará": [16,[5,13,18]],
"secuencia": [11,22],
"bold": [11],
"miembro": [25,8],
"introduc": [12,[13,21]],
"recurso": [9,[4,5]],
"contenido": [25,[8,16,26],[12,24],[7,17],[5,6,9,11,13,14,18,19,23]],
"encerrar": [12],
"facilidad": [5],
"android": [9],
"compresión": [23],
"haitiano": [2],
"ampliament": [5],
"suministra": [8],
"ava": [2],
"shot": [12],
"pushto": [2],
"contenida": [13],
"ave": [2],
"capac": [[7,9,13,25]],
"contien": [8,[25,26],5,12,17,[11,13],[4,6,10,16,19,21,23,24]],
"sustitución": [26],
"describir": [[22,26]],
"target": [14],
"superponiendo": [[11,28],27],
"así": [13,5,[7,9,14,16,18,21,25,26]],
"fabián": [3],
"config-dir": [[13,17]],
"muestra": [26,16,[5,13,21],[6,11,12,15,18]],
"envía": [[15,17]],
"manten": [[8,13,25]],
"evidentement": [26],
"tibetano": [2],
"reemplaza": [16,[26,28]],
"caso": [26,25,13,[5,23],[7,11,17,18],[9,15,22],[1,8,19,20,24]],
"casi": [[15,18]],
"obten": [[9,13,14,15,25,26]],
"operadores": [27],
"previament": [16,[1,5]],
"aym": [2],
"crearán": [14],
"localizar": [13],
"allí": [[8,13,16,19,25]],
"empezar": [[14,17,22]],
"temporalment": [26],
"procesamiento": [[7,16]],
"tratar": [16],
"comienza": [[5,22]],
"cifra": [13],
"aaabbb": [20],
"allá": [1,[9,13]],
"aze": [2],
"electrónico": [4,26],
"contabl": [26],
"ventajosa": [22],
"relacionada": [8],
"relacionado": [[4,5,11,15,25,26]],
"diccionarios": [27],
"inutilizar": [19],
"actualizacion": [13],
"bokmål": [2],
"vestigio": [11],
"cerrar": [16,14],
"ventajoso": [22],
"activan": [16,22],
"unicod": [18,12,[20,28]],
"activar": [16,[11,15]],
"mayoría": [18,[16,22],[7,8,9,26]],
"tratan": [11],
"comienzo": [1,20],
"innecesariament": [25],
"habitu": [1],
"cuestión": [[22,23]],
"en-us": [25],
"especifiqu": [25],
"sencilla": [13],
"motu": [2],
"reproducida": [11],
"restriccion": [13],
"códigos": [27],
"msgstr": [7],
"luce": [14],
"atributo": [7,22],
"separar": [22],
"delimitada": [12],
"utilizar": [25,13,[9,23],[7,12,26],[5,8,15,17,21],[0,11,16]],
"raíz": [25,[13,16]],
"codificación": [7,18,28,12,[5,9,16,27]],
"utilizan": [7,[5,8,15,26]],
"proporcionando": [26],
"bashkir": [2],
"peticion": [4],
"important": [8,[5,17],[13,26],[0,9,10,14,22]],
"omegat.project": [28,8],
"dirección": [9,[13,16,26,28]],
"facilitada": [16],
"obligatoria": [7],
"targetcountrycod": [7],
"sencillo": [22,[0,12,28]],
"x_without_jr": [13],
"aplicarla": [11],
"llegar": [9],
"intercambiar": [[5,16]],
"solución": [28,18,27],
"webstart": [13],
"traducirán": [13],
"resid": [[13,17]],
"rumania": [2],
"ningún": [26,[1,11,13,23]],
"sag": [2],
"san": [2],
"original": [14,16,[27,28]],
"símbolo": [[16,20]],
"técnico": [26],
"usado": [25],
"jpn": [2],
"automaticen": [13],
"resx": [9],
"favor": [[2,13]],
"modalidad": [[9,13]],
"técnica": [15],
"alta": [15],
"pāli": [2],
"equivalent": [26,[6,8,13,16,25]],
"indeciso": [5],
"cópiala": [25],
"formatear": [11],
"mismo": [25,[7,11,12,13,23,26],[17,18,20],[5,6,16,21,22,24]],
"circunstancia": [11],
"bak": [[2,8]],
"bam": [2],
"estonia": [2],
"bat": [13,8],
"indicará": [25],
"continúen": [0],
"misma": [[13,26],[1,7,8,9,11,20,25]],
"redistribuir": [3],
"participar": [8,4],
"jre": [13],
"francé": [22,13,2],
"nepali": [2],
"intención": [22],
"flujo": [[8,26]],
"lanzar": [13,8,11],
"potencialment": [11],
"declaracion": [26],
"aparición": [21],
"múltiples": [27],
"comprueba": [6,15,[9,17,23]],
"ejecución": [[0,5,8,13]],
"atractivos": [27],
"plano": [28,[9,16,18],5,[12,24]],
"cachemira": [2],
"gracias": [27],
"hebreo": [2],
"instalarlo": [8],
"freebsd": [[8,20]],
"proyecto": [25,28,16,8,26,14,[17,19],[12,13],27,[4,5,21,22],[7,23],[0,6,9],24],
"presentará": [14],
"sea": [24,25,[3,9,11,20],[8,12,14,15,16,17,22,26,27,28]],
"vito": [[3,27]],
"seq": [12],
"developerwork": [13],
"ser": [5,22,[9,12,13,16,17],[6,7,11,14,15,20,21,23,25]],
"vist": [4],
"set": [[8,13,17]],
"dejado": [16],
"validada": [26],
"megat": [3],
"dañado": [11],
"terminología": [[5,25,26],[12,21]],
"familiarizado": [17],
"validado": [16],
"igual": [[24,25,28],[8,13,17,27]],
"silencioso": [[13,17]],
"obligado": [15],
"fleurk": [3],
"explicación": [[12,13,17]],
"bihari": [2],
"offic": [11,[7,9]],
"dígito": [[16,20],[13,17,25]],
"bel": [2],
"ben": [2],
"resumen": [5,27],
"lucir": [14],
"consultarlo": [8],
"revé": [19],
"contada": [26],
"desmarcando": [7],
"joel": [4],
"él": [13,[1,8]],
"simplificado": [5],
"debe": [13,11,22,17,7,[4,6,8,9],[14,16,18,21,23,25,26]],
"configurado": [23,[7,17]],
"fohlen": [12],
"mantienen": [26],
"pptx": [9],
"licencia": [3,8,[6,16]],
"buscar": [21,[14,16,23],[5,26,28]],
"informará": [25],
"compat": [9,[13,25],12,[5,7,11,14,20,21,26]],
"pequeña": [5],
"cursor": [16,1,22,26],
"pierdan": [12],
"traducirá": [13],
"xhosa": [2],
"kurdo": [2],
"inestim": [0],
"cambiando": [19,28],
"variacion": [25],
"signo": [[12,26]],
"repetido": [26],
"sin": [13,26,[9,18],[0,3,7,25],[8,11,12,14,15,24],[22,23,28],[2,5,16,17,19,27]],
"client": [25,[5,26]],
"mostrarlo": [9],
"conversion": [9],
"historia": [16],
"vací": [26],
"codificar": [[12,15]],
"repetida": [26],
"punjabi": [2],
"tiago": [3],
"mundo": [18],
"confirmación": [[16,25]],
"falta": [11],
"kalaallisut": [2],
"restant": [8],
"foundat": [3],
"bih": [2],
"frecuencia": [28,26],
"bin": [8],
"gráfica": [17,13,[7,8]],
"apertium": [15,28,27],
"bit": [18],
"cerca": [25],
"bis": [2],
"tabulador": [[12,20,22,26]],
"autom": [13],
"yoruba": [2],
"decir": [25,[13,18,21,23],[6,7,9,16,17,26]],
"mueva": [22],
"larga": [26],
"discusion": [8],
"soporten": [5],
"decid": [[19,25]],
"autor": [3,16,21],
"slk": [2],
"slv": [2],
"reacio": [20],
"corregido": [[12,15]],
"presentación": [7],
"figuran": [9],
"amba": [[11,15,20,21]],
"sme": [2],
"frent": [[9,15,18,26]],
"correspondient": [[8,9,26],[7,11,17,22,23],[13,20,25]],
"smo": [2],
"sido": [[5,11,12,13,15]],
"ambo": [[9,17,21,25]],
"contador": [26,28],
"divehi": [2],
"deja": [[7,13,16,24]],
"agradecimientos": [27],
"häuschen": [12],
"figuras": [27],
"tabla": [16,20,26,[2,22],[1,9,11,27]],
"largo": [[18,22,26]],
"sna": [2],
"snd": [2],
"faltan": [26,16],
"nombrearchivo": [25],
"didier": [0,3],
"sensibl": [[4,21,26]],
"regularidad": [25],
"gráfico": [[8,13,17,26,28]],
"auto": [25,18,[7,28]],
"teclado": [16,[5,27,28],[1,26],[2,3,12,19]],
"plazo": [18],
"trabajo": [[25,26],13,[8,16],[0,5,9,14,17,19,23]],
"notepad": [12],
"comportamiento": [24,[26,28],[1,16,27],[5,8,9,13,25],[7,10]],
"som": [2],
"son": [26,[8,11,25],12,[9,13],[7,18,21,23],19,[0,4,14,17,20,24]],
"oracl": [13],
"sot": [2],
"fallo": [4,28,8],
"saltará": [7],
"spa": [2],
"contenidos": [27],
"codificado": [18,[9,12]],
"instantáneo": [26],
"interpretar": [18],
"relevant": [11],
"pedirá": [16],
"movimiento": [[16,27]],
"excepción": [22,28],
"bob": [25],
"bod": [2],
"trabaja": [8,[7,22]],
"produc": [[9,13]],
"propuesta": [15],
"bos": [2],
"sqi": [2],
"ponerla": [[16,25]],
"total": [26,[8,16]],
"kal": [2],
"monitorear": [4],
"kan": [2],
"altament": [[7,22]],
"prevista": [22],
"kas": [2],
"thoma": [3],
"kau": [2],
"kat": [2],
"opción": [16,[13,26],[7,12,21,22,23,24]],
"br1": [11],
"intervención": [25],
"subir": [22],
"srd": [2],
"kaz": [2],
"consola": [17,13,28,27],
"observará": [21],
"informe": [27],
"control": [16,[14,20]],
"sawula": [3],
"srp": [2],
"sigu": [[13,25],[9,11,12,23,26]],
"pulsa": [11,[1,12,15,21,26]],
"srt": [9],
"pretraducir": [25],
"sistemas": [27],
"traducción": [25,26,5,15,8,16,28,[14,19,27],17,[9,24],[11,12,21],[7,13],22,[18,20,23]],
"hazlo": [8],
"coherent": [[16,22]],
"específico": [[8,17,26],[5,7,9,13,22,25]],
"aumentar": [[22,25]],
"environ": [13,8],
"bre": [2],
"específica": [[9,25],[5,7,13,20,24,26]],
"ssw": [2],
"chuvashia": [2],
"breve": [5,[1,23]],
"entiend": [14],
"indicada": [25],
"generalment": [8,[11,13,20,22,26]],
"kde": [13,28],
"restaurarlo": [8],
"excluyendo": [25],
"lingüístico": [15],
"permitiéndot": [5],
"motor": [15,21],
"estadístico": [15],
"acceso": [13,8,[9,16]],
"languag": [13],
"multitérmino": [12],
"distingu": [20],
"sun": [2],
"sus": [[5,9,11,25],[6,8,12,15,20,26]],
"sur": [2],
"estadística": [[26,28],16,8],
"vergüenza": [0],
"preguntars": [0],
"generará": [25],
"necesaria": [[11,21]],
"decisión": [15],
"alguna": [25,[8,11],[13,17,19,26]],
"svg": [13],
"métodos": [27],
"viejo": [2],
"opcionalment": [[13,17]],
"svn": [13],
"escritura": [[17,22]],
"kazajstán": [2],
"condicion": [25],
"necesario": [13,8,9,[15,23,26],[11,25],[6,12,16,20,21]],
"alguno": [22,[8,16,17],13,[6,10,12,15,18,19,25,26,28]],
"están": [16,26,13,[6,8,11,12,17,25],[9,14,15,21,22,23]],
"llenar": [[24,25]],
"problemas": [27],
"glosarios": [27],
"bul": [2],
"demá": [[7,11,14]],
"swa": [2],
"recomendamo": [22],
"mezclan": [28],
"swe": [2],
"oest": [2],
"selección": [16,[1,24],[6,26,28]],
"lógico": [20],
"asignada": [13],
"contexto": [[5,11]],
"cinco": [7],
"requier": [13,[8,11,23,26]],
"tímido": [20],
"montón": [0],
"fábrica": [[8,28]],
"variant": [20],
"cierr": [11],
"parch": [25],
"cómo": [28,5,[6,8,9,11,12,23,25,27],[14,24]],
"sino": [[20,25]],
"basándos": [[7,16],23],
"limburgan": [2],
"parec": [[11,13]],
"predeterminada": [7,[18,28],9,[8,16,21,22,25,26],[13,20,27]],
"facilitar": [11,26],
"aplicación": [8,[13,28],9,[3,5,17,23,27],[10,18,22,26]],
"hillari": [0],
"uilayout.xml": [8],
"khm": [2],
"inici": [13,26],
"kirguis": [2],
"predeterminado": [25,[12,13,16,17,18,22,24]],
"cantidad": [[0,13,23,25]],
"desd": [13,8,[25,26],28,[20,22],[5,16],[7,11,12,15,21,24]],
"dese": [[7,14,25],[23,24]],
"errático": [8],
"etapa": [[9,15,25]],
"idea": [26],
"hiri": [2],
"provocar": [19],
"esquina": [26],
"kik": [2],
"kim": [3],
"asociar": [9],
"kin": [2],
"kir": [2],
"ordenará": [25],
"cuidado": [[0,11,17]],
"normal": [[8,13]],
"significativo": [22],
"conflicto": [25],
"figura": [28,26,[12,23],[8,21],[6,11,15,19,20,24]],
"ortografía": [23],
"finalment": [[13,14,23]],
"dañan": [8],
"dañar": [11],
"daño": [11],
"verla": [22],
"license.txt": [8],
"tipográfico": [5],
"runtim": [13,8],
"individu": [11],
"fundamentalment": [23],
"potent": [[20,21]],
"luba-katanga": [2],
"diferent": [26,25,9,11,[1,7,22]],
"avar": [2],
"hito": [25],
"tayikistán": [2],
"guardada": [8],
"filenam": [7],
"tener": [12,[13,14,26],[11,19],[4,5,9,10,21,25,27]],
"pular": [2],
"parcialment": [[15,26]],
"roam": [8],
"encerrado": [[11,16]],
"distorsion": [9],
"palabra_ignorada": [28],
"amor": [0],
"tenga": [22,[4,9,13,19,23,25]],
"guardado": [[8,16]],
"hace": [26,[0,1,16,17]],
"heredado": [[9,25]],
"sirv": [25],
"disponibilidad": [15],
"rango": [[12,20]],
"palabras": [27],
"interna": [26,16,19],
"actúa": [[8,25]],
"heredada": [16],
"usando": [28,27,[13,23],[7,12,21],[17,25]],
"servirá": [8],
"oracion": [26],
"rode": [11],
"según": [13],
"palabra_aprendida": [28],
"instancia": [[13,21]],
"limitar": [[13,21]],
"mandelbaum": [3],
"impreso": [26],
"exhibición": [9],
"sugerida": [[12,26]],
"algunaubicación": [8],
"regular": [20,21,22,[5,17,28],7,[9,13]],
"función": [[11,16],[12,13,23,24],[6,8]],
"tendrá": [[14,26],12,[1,13,15,22,23,25]],
"c\'est": [12],
"kirguistán": [2],
"anidarlo": [11],
"x_linux.tar.bz2": [13],
"elemento": [12,13,26,[16,20],[7,8,18,28]],
"omegat.log": [8],
"árabe": [2],
"proporciona": [13,[5,26],[4,15,21,23]],
"utilizarlo": [15],
"estarán": [[8,11,16,23]],
"parcial": [25,26,16,[24,28],9],
"kom": [2],
"kon": [2],
"kor": [2],
"tab": [[12,16]],
"información": [13,26,9,[5,8,11,16,25],[2,6,12,17,18,20,24]],
"eslovaca": [2],
"abordar": [18],
"tah": [2],
"tag": [9],
"empezó": [25],
"extraño": [11],
"tal": [22,[5,8,24],[1,6,26]],
"tan": [9,[5,20,26]],
"tam": [2],
"variedad": [[5,15,25]],
"administrador": [[8,23]],
"tao": [5,25],
"haga": [14],
"individual": [11,[7,21,28]],
"tar": [[8,13]],
"tat": [2],
"comenzar": [26],
"afectado": [11],
"aproximada": [[14,26]],
"detrá": [[12,23]],
"utilizarla": [5],
"todavía": [[19,25]],
"sospecha": [11],
"letonia": [2],
"atractivo": [5],
"lingüística": [15],
"winrar": [6],
"tbx": [12],
"leem": [7],
"leer": [[7,13,25,26]],
"cat": [2],
"duser.countri": [13],
"tcl": [24],
"consulta": [[16,25],26,[13,21,22],[4,9],[3,19,20]],
"tck": [24],
"clarament": [18],
"eliminación": [11],
"preguntará": [11],
"trabaj": [23],
"colaborador": [0],
"construyendo": [[13,28],27],
"informar": [18],
"typo3": [9],
"align.tmx": [13,17],
"actualizarla": [13],
"ahorrado": [0],
"argumento": [13,28,25]
};
