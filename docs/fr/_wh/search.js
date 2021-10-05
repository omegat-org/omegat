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
 "Annexe A. Dictionnaires",
 "Annexe B. Glossaires",
 "Annexe D. Expressions régulières",
 "Annexe E. Personnalisation des raccourcis",
 "Annexe C. Vérificateur orthographique",
 "Installation et exécution d&#39;OmegaT",
 "Guides pratiques",
 "OmegaT 4.2 – Guide de l&#39;utilisateur",
 "Menus",
 "Volets",
 "Dossier projet",
 "Fenêtres et dialogues"
];
wh.search_wordMap= {
"altgraph": [3],
"au-dessus": [11],
"résultant": [11],
"écrivant": [11],
"souhaité": [8],
"lui-mêm": [2],
"être": [11,6,5,1,3,8,[2,4,10],9,0],
"prévenu": [11],
"régulièr": [2,11,[5,6],[3,4]],
"l\'avoir": [8],
"changent": [6],
"xml": [11],
"devrez": [6,[5,11]],
"tel": [8,[4,5,10,11]],
"d\'éviter": [10,[6,11]],
"avant": [11,8,6,10,[1,4,5,9]],
"italiqu": [11],
"logiqu": [[2,11]],
"formulair": [5],
"info.plist": [5],
"relâché": [3],
"sert": [10],
"xmx": [5],
"produit": [6,[5,10],[8,9]],
"cour": [9,8,[10,11],5,1],
"produir": [6],
"nous": [[6,11]],
"fuzzi": [8],
"utiliseront": [11],
"befor": [5],
"réviseur": [11],
"pouvoir": [5,[3,4,11]],
"sera": [11,[6,8],5,1,9,[3,10]],
"util": [11,5,[2,6],[4,9]],
"l\'applic": [[5,6],8],
"quitter": [[3,8,11]],
"opérateurs": [7],
"tar.bz": [0],
"avid": [2],
"spécialisé": [9],
"quittez": [8,[6,11]],
"lire": [[5,6]],
"installeront": [5],
"remplacé": [6],
"commencé": [11],
"dgoogle.api.key": [5],
"formaté": [[6,11]],
"avait-il": [11],
"ces": [11,6,[4,8],5,10,9],
"edittagnextmissedmenuitem": [3],
"purement": [6],
"réticents": [7],
"cet": [11,[6,8]],
"raccourci": [3,5,8,11,[2,6,9]],
"spécifi": [[5,11]],
"quiet": [5],
"seul": [11,2,5,[6,8],[0,9]],
"catégories": [7],
"recherch": [11,8,2,[3,5],[4,6]],
"tutoriel": [5],
"xlsx": [11],
"dépôt_de_tous_les_projets_omegat_en_équip": [6],
"représenté": [11],
"essayez": [11],
"es_es.d": [4],
"heur": [[6,11]],
"d\'établir": [11],
"nécessair": [[5,6,11],4,10,[0,1,2]],
"paramètr": [5,11,6,8,10,4],
"assembledist": [5],
"gnu": [0],
"the": [5,0],
"présentat": [11],
"d\'orthograph": [[4,8,11]],
"trouvaient": [9],
"d\'assur": [8],
"projectimportmenuitem": [3],
"autoris": [11,5,8],
"régulier": [6],
"validez": [6],
"veuillez": [[10,11]],
"imag": [5],
"exclusiv": [6],
"monolingu": [11],
"target.txt": [11],
"l\'anné": [6],
"standard": [[4,11],[1,6,8,9]],
"d\'espac": [11,[2,8],3],
"délivré": [5],
"possèdent": [11],
"pré-exist": [8],
"correct": [[5,6,11],4,[1,8,10],9],
"traduct": [11,6,[8,9],10,5,3,2],
"vérifi": [[8,9]],
"analysé": [6],
"viennent": [8],
"métacaractèr": [2],
"ajouté": [[6,10],5,8],
"nameon": [11],
"fictif": [11],
"renommez": [6],
"entier": [11],
"moodlephp": [5],
"currsegment.getsrctext": [11],
"optionsglossarytbxdisplaycontextcheckboxmenuitem": [3],
"priorité": [11,8],
"export": [6,11,8,[3,10]],
"caractère": [7],
"mieux": [11],
"gotonextnotemenuitem": [3],
"par": [11,6,8,3,5,9,2,10,1,4,0,7],
"tar.gz": [5],
"autour": [11],
"pas": [11,6,5,8,[1,2],9,4,10,3],
"pay": [5,11],
"transtip": [[3,9]],
"d\'option": [11],
"list": [11,[4,8],[2,5,6,10]],
"relatives": [7],
"spécial": [[6,11]],
"corrigé": [11],
"xxxx9xxxx9xxxxxxxx9xxx99xxxxx9xx9xxxxxxxxxx": [5],
"veiller": [11],
"intégré": [11,[4,9,10]],
"également": [11,5,6,[4,9],8,[1,3,7]],
"azur": [5],
"fr-fr": [4],
"systématiqu": [5],
"synchronis": [[5,6,11]],
"formats": [7],
"incluent": [11],
"retrouv": [9,[8,11]],
"minim": [11],
"s\'assur": [11,[5,6]],
"échéant": [5],
"connaîtr": [11],
"lent": [11],
"prédéfinies": [7],
"proposé": [9,[5,8]],
"partiell": [11,8,9],
"apparaîtra": [10],
"générer": [6],
"conçu": [11,6],
"webster": [0,[7,9]],
"aussi": [[5,6],[2,9,10,11]],
"combin": [11],
"rechargé": [6,1],
"japonai": [11,5],
"milliard": [5],
"menus": [5,11,8,7],
"indiquera": [6],
"liaison": [10],
"orienté-objet": [11],
"citation": [7],
"cjk": [11],
"impliqu": [5],
"insérer": [8,[3,11]],
"spécialement": [[4,9]],
"reconnu": [11],
"mêmes": [[1,4,5,9]],
"pdf": [6,[7,8,11]],
"numérisé": [6],
"chariot": [2],
"syntax": [11,3],
"permis": [11],
"griser": [11],
"personnel": [5],
"passant": [11],
"étaient": [8],
"empti": [5],
"apporté": [5,11],
"blocs": [7],
"neutralisé": [5],
"d\'archiv": [0],
"vieill": [11],
"toolsshowstatisticsmatchesmenuitem": [3],
"focus": [11],
"viewdisplaymodificationinfononeradiobuttonmenuitem": [3],
"appuy": [11,9,6],
"répertoir": [10],
"locaux": [11],
"variabl": [11],
"l\'extens": [11,6,[1,10]],
"tmx": [6,10,5,11,8,[3,9]],
"propos": [[5,8,9,11]],
"créées": [9],
"d\'inform": [5,[6,11],10],
"peu": [[2,6,11]],
"moteur": [8,11],
"capabl": [[9,11]],
"actuel": [8,11,3,10,6,5],
"traductions": [7],
"contiendra": [10],
"d\'abréviat": [11],
"vérific": [[1,2,6,8,11]],
"distant": [6,10,[8,11]],
"nl-en": [6],
"mécanism": [8],
"fenêtr": [11,8,9,5,4,[3,6,10]],
"integ": [11],
"intel": [5,7],
"aligné": [[8,11]],
"mainmenushortcuts.properti": [3],
"prétraduct": [6],
"projectaccesswriteableglossarymenuitem": [3],
"chinois": [5],
"catégori": [2],
"préparat": [6],
"presser": [11],
"enfoncé": [3],
"c\'est-à-dir": [6,11,5,10],
"surtout": [11],
"glisser-déplac": [5,9],
"convertir": [[6,11]],
"cmd": [[6,11]],
"savoir": [8],
"assistée": [7],
"coach": [2],
"pressez": [11],
"unité": [11,10],
"entrez": [11,5],
"conservez": [10],
"sentencecasemenuitem": [3],
"gotohistorybackmenuitem": [3],
"emplac": [5,6,11,[8,10],[1,9]],
"choisi": [11,8,5,[6,9]],
"entrer": [11,[2,6]],
"restrict": [11],
"premièr": [11,8,[1,5],[6,9]],
"indiqué": [11,6],
"project-save.tmx": [6],
"sauvegardez": [5],
"renommez-l": [6],
"n\'execut": [11],
"uhhhh": [2],
"saut": [11,2],
"powerpc": [5],
"optionssentsegmenuitem": [3],
"bascul": [6,3,8],
"product": [11],
"équival": [2,[0,5,9,11]],
"question": [[6,11]],
"françai": [5],
"premier": [11,8,5,[2,3,6]],
"haut": [11,9,5,2],
"suivant": [11,8,3,5,[2,6,9],0,4,[1,7,10]],
"sauf": [[2,6]],
"reconnaiss": [[6,11]],
"typag": [11],
"optionsaccessconfigdirmenuitem": [3],
"résolut": [6],
"milieu": [[2,6,11]],
"tandi": [9,0],
"test.html": [5],
"incap": [11],
"regard": [11],
"xxx": [10],
"détail": [11,[6,9],[5,8]],
"instanc": [5,9],
"smalltalk": [11],
"résolus": [1],
"provoquera": [11],
"instal": [5,4,0,[7,8]],
"curseur": [8,[9,11],1],
"fichier2": [6],
"désactivez": [1],
"travaillé": [10],
"proch": [9,11,[6,10]],
"l\'environn": [5],
"marqueur": [9,[2,11]],
"voir": [11,6,5,[2,9],10,[4,8]],
"omegat.sourceforge.io": [5],
"pseudotranslatetmx": [5],
"soyez": [6,5],
"pipe": [11],
"motif": [11],
"amélior": [8],
"cliquer": [8,[5,11],4],
"clé": [11,5,2],
"targetlanguagecod": [11],
"déchochez": [11],
"d\'édition": [9,11,8,[3,10]],
"cliquez": [11,5,[4,8]],
"préfèrent": [6],
"d\'intégrer": [6],
"tri": [8],
"s\'affich": [[5,11]],
"objet": [11],
"translat": [11,5,4],
"l\'applicatif": [5],
"uniqu": [11,9,[3,5,8],[2,6]],
"suivi": [11,[2,6]],
"annul": [[3,8]],
"préalabl": [[4,6,9]],
"reconverti": [6],
"significatif": [11],
"manuell": [11],
"détach": [9],
"roug": [11,10],
"deuxièm": [[1,11]],
"qu\'omegat": [11,5],
"saisissez": [5,[8,11]],
"lancer": [5,11],
"environn": [5],
"fichiers": [7],
"dépôt_des_sources_de_tous_les_projets_omegat_en_équip": [6],
"répandus": [6],
"suivr": [6],
"applicabilité": [11],
"diaposit": [11],
"d\'express": [2,9],
"un_autr": [6],
"cohérent": [11],
"docs_devel": [5],
"l\'origin": [10],
"devrait": [0],
"départ": [11,[5,6]],
"d\'état": [9,[5,7]],
"tsv": [1],
"distanc": [5],
"l\'activ": [5],
"automatiqu": [11,8,3,5,[6,9],4,1],
"lanc": [[5,8,11]],
"élément": [3,11,5,[2,6],[1,8,9]],
"devant": [[5,11]],
"primair": [5],
"flux": [9],
"point-virgul": [6],
"arborescent": [10],
"gnome": [5],
"conven": [4],
"leur": [11,6,10,[1,2,5,8,9]],
"renommag": [6],
"terminaison": [2],
"masqu": [11,6],
"prérequi": [5],
"fr.wikipedia.org": [9],
"encyclopedia": [0],
"analys": [[2,11]],
"particuli": [6,[5,11]],
"l\'entré": [8,[1,2,11]],
"changement": [[6,10],[5,8,11]],
"répété": [11],
"gérée": [6],
"celui": [11,4,9],
"traduir": [11,6,10,[5,9],8],
"recharg": [8,11,3],
"conseils": [7],
"cohérenc": [8],
"quel": [11,[2,8,9],[4,5],1],
"traduit": [11,8,6,9,3,[5,10],4],
"appui": [[8,9]],
"traduis": [11],
"l\'installation": [7],
"optionstagvalidationmenuitem": [3],
"nombreux": [[2,6,10,11]],
"doublon": [11],
"csv": [1,5],
"n.n_linux.tar.bz2": [5],
"vont": [[6,11]],
"nombreus": [[6,11]],
"n\'a": [8,6,1],
"séparément": [11,[3,6]],
"pt_br": [4,5],
"fonctionn": [11,[5,6,8]],
"ingénieur": [6],
"apparit": [11],
"concern": [[0,4,6,9,11],5],
"a-z": [2],
"n\'i": [5,1,8],
"consitué": [6],
"affichera": [11,5],
"paramétré": [6],
"demandez": [6],
"caractèr": [11,2,8,1,[3,5,9],6],
"meilleur": [[9,11],10],
"les": [11,6,8,5,9,10,3,1,4,2,0,7],
"press": [3],
"dock": [5],
"souviendra": [11],
"lorsqu\'omegat": [11],
"sous-titr": [5],
"endommagé": [11],
"devraient": [[5,11]],
"listé": [[3,8]],
"dmicrosoft.api.client_secret": [5],
"javascript": [11],
"marqu": [11,[5,9]],
"mediawiki": [11,[3,8]],
"mappag": [6,11],
"input": [11],
"non-omegat": [11],
"fonctionnel": [11],
"ctrl": [3,11,9,6,8,1,[0,10]],
"privé": [[5,11]],
"droite": [7],
"document": [11,6,8,3,9,5,[1,2,7,10]],
"prévu": [11],
"rapport": [6,[9,11]],
"mainten": [[3,11]],
"exporté": [11,[6,8]],
"limite": [7],
"davantag": [11],
"possibilité": [[1,6,11]],
"moment": [[1,4,11],9],
"attribué": [[3,5],11],
"destiné": [6,[2,5,11]],
"construir": [5],
"vivement": [6],
"supplémentair": [11,5,[2,6,10]],
"nombr": [11,9,[1,6,8],[2,10]],
"n\'exist": [[4,6,9,11]],
"resourc": [5],
"chaqu": [11,6,8,9],
"team": [6],
"xx_yy": [[6,11]],
"effectueront": [2],
"docx": [[6,11],8],
"txt": [6,1,[9,11]],
"charg": [6,11,5,[2,8]],
"googl": [5,11],
"révision": [10],
"opendocu": [11],
"suffisa": [11],
"marchera": [5],
"commenté": [11],
"download.html": [5],
"glossaire": [7],
"disqu": [[5,6,8]],
"êtres": [6],
"remplacera": [8],
"détect": [[1,5,8]],
"revient": [[8,10]],
"source": [7],
"fréquent": [5],
"align": [11,8,5],
"procéder": [[6,11]],
"totaux": [[8,9]],
"malgré": [11],
"sourceforg": [3,5],
"trnsl": [5],
"guides": [7],
"structur": [[10,11]],
"viewdisplaymodificationinfoselectedradiobuttonmenuitem": [3],
"index.html": [5],
"omegat.tmx": [6],
"pris": [6,[5,11],8],
"ressembl": [[2,6]],
"compliqué": [6],
"champ": [11,[5,8],4,6],
"marqueurs": [7],
"doubl": [5,[2,11]],
"personnalis": [3,11,2],
"exécutez": [5],
"editmultipledefault": [3],
"l\'imag": [[4,9]],
"batch": [5],
"mozilla": [5],
"editfindinprojectmenuitem": [3],
"déposant": [5],
"marqué": [[10,11]],
"diffrevers": [11],
"début": [11,[2,10],[5,6]],
"warn": [5],
"procédez": [0],
"attent": [[5,6,11]],
"technetwork": [5],
"dépannag": [6],
"pratiqu": [[5,6],[0,9,10,11]],
"appliqu": [11,6],
"page": [11,8,[3,5,6],[2,9]],
"auxiliair": [6],
"votr": [5,6,4,9,11,3,8,1],
"initiaux": [11],
"pert": [6],
"plural": [11],
"plupart": [11,[3,5]],
"commune": [6],
"ralentir": [6],
"déverrouillé": [9],
"limité": [11,6],
"panneau": [11,5],
"retirez": [[5,11]],
"l\'intérieur-mêm": [3],
"vider": [9],
"traduisez-l": [6],
"éditeur": [[8,9,11],1,[5,6,7]],
"manqu": [2],
"d\'ouvrir": [5,11,[4,8,9]],
"décrite": [[5,11]],
"project.gettranslationinfo": [11],
"prudent": [6],
"doit": [11,6,[3,5],[1,4]],
"attendu": [[1,7]],
"lesquel": [11,6],
"travaillez": [[3,11]],
"bidirectionnell": [6],
"n.b": [11],
"l\'orthograph": [4],
"slélectionné": [8],
"corrompr": [6],
"personnalisé": [[3,8,11]],
"start": [5,7],
"windows": [7],
"niveau": [11,[5,8],6],
"pair": [11,6,5],
"equal": [5],
"l\'arboresc": [10],
"apparaîtront": [11],
"colour": [11],
"n.n_windows.ex": [5],
"posséder": [11],
"chacun": [[1,6,11]],
"chang": [8,[5,11]],
"impliqué": [6],
"possédez": [[4,6]],
"générales": [7],
"fournira": [6],
"clés": [11,5],
"optionsalwaysconfirmquitcheckboxmenuitem": [3],
"tmxs": [[6,11]],
"pui": [11,5,6,[2,4,8]],
"bouton": [11,5,4],
"java-vers": [5],
"anné": [6],
"d\'usag": [0],
"réalign": [11],
"mettez": [11],
"program": [5],
"ajout": [11,6,5,10,[1,3],[2,8]],
"cyan": [8],
"lié": [11],
"gardez": [11],
"éditant": [5],
"l\'utilitair": [0],
"prévenir": [6,7],
"côte": [5],
"intraduct": [11],
"étranger": [11],
"traduisez": [6,[9,11]],
"options": [7],
"enter": [5],
"bien": [11,[5,6,9,10],4],
"vous": [11,5,6,9,8,4,10,3,2,0],
"applic": [5,[4,6],11],
"projectteamnewmenuitem": [3],
"évolut": [6],
"dossier": [5,6,11,10,8,1,[3,4],[0,9],7],
"importé": [6,[1,9]],
"utilisera": [5,[4,6]],
"déplacer": [9,[8,11]],
"préexistant": [6],
"memori": [5],
"n.n_mac.zip": [5],
"déployé": [5],
"revenir": [9,11],
"écrit": [11,8,3],
"tabl": [11,[3,8]],
"ouvrez": [[5,6],[10,11],8],
"appuyez": [9,11,[1,5,6,8]],
"l\'ouvertur": [[6,11]],
"lor": [11,5,6],
"lot": [5],
"ramèn": [11],
"omegat.jnlp": [5],
"l\'attribut": [11],
"traiter": [11,8],
"interagir": [11],
"rempli": [8],
"theme": [11],
"dépend": [8,[1,5,6]],
"n.n_windows_without_jre.ex": [5],
"peut": [11,6,5,[3,8,9],10,1,4],
"pseudotranslatetyp": [5],
"aider": [6],
"éprouvent": [2],
"existerait": [11],
"affiché": [11,8,[1,9],5,[6,10]],
"initial": [[5,11]],
"confirmez": [5],
"configuré": [10],
"clic": [[5,11],9],
"prof": [11],
"connu": [9],
"ansi": [11],
"avertiss": [11,[5,6,9]],
"automatiseront": [5],
"présent": [11,5,10,[6,9],[0,1]],
"entièr": [11,6],
"dmicrosoft.api.client_id": [5],
"prédomin": [10],
"cepend": [6,[5,11],4],
"chaîn": [11,6,9],
"toutefoi": [5,[4,6]],
"config-fil": [5],
"sélectionné": [8,11,5,6,[4,9],3],
"titr": [11,8],
"tell": [6,[0,10,11]],
"d\'exécut": [5,8],
"demandé": [5,8],
"fournisseur": [11],
"projectclosemenuitem": [3],
"dépôt_du_projet_omegat_en_équipe.git": [6],
"nouveaux": [[1,3],[6,11],8],
"ajoutez": [[3,5,6]],
"vérifier": [11,6,4,[9,10]],
"viewmarknonuniquesegmentscheckboxmenuitem": [3],
"dépiot": [6],
"dan": [11,6,5,8,9,10,1,[3,4],2,0],
"européenn": [[6,8,11]],
"correspondront": [11],
"n\'exécut": [11],
"zone": [[2,11]],
"castillan": [4],
"sous-répertoir": [10],
"côté": [6,11,[3,4]],
"d\'omegat": [5,11,3,6,1,[7,8],[0,4,10]],
"considér": [[6,11]],
"téléchargé": [5,11],
"interrupt": [11],
"apportez": [6],
"orthographique": [7],
"avantageus": [11],
"déployer": [5],
"group": [11,6,[2,9]],
"renomm": [[4,11],6],
"dictionnair": [4,0,11,8,9,10,[1,3,6]],
"apparaît": [[5,11]],
"donn": [11,[8,9]],
"findinprojectreuselastwindow": [3],
"system-user-nam": [11],
"façon": [11,5,6,[3,4,8,9]],
"format": [6,11,1,8,0,[5,7,9]],
"développ": [2],
"parvienn": [6],
"nommé": [5,[1,10,11]],
"détaché": [11],
"readme.txt": [6,11],
"d\'icôn": [5],
"restreint": [11],
"donc": [[5,6,11],[4,10]],
"languagetool": [11,8],
"console.println": [11],
"source.txt": [11],
"vérifiez": [6,0,4],
"files.s": [11],
"histori": [8],
"nouvell": [11,8,5,[1,2]],
"exchang": [1],
"dont": [11,6,5],
"très": [[5,11],[6,10]],
"traitement": [11,3],
"projet_en-us_fr": [6],
"tent": [11,[5,6]],
"incluant": [11],
"endroit": [4,[6,8,9,11]],
"autonom": [[2,5]],
"part": [9],
"currseg": [11],
"point": [11,2,[5,6,8,9]],
"principal": [9,11,[3,4,6]],
"colonn": [11,1,8,9],
"ensembl": [11],
"parc": [6],
"ouvrir": [8,3,[6,11],5],
"lexiqu": [4],
"l\'intermédiair": [6],
"facil": [6,11],
"temp": [11,[4,9]],
"ligne": [7],
"project_files_show_on_load": [11],
"n\'étaient": [11],
"diffèrent": [5],
"attribut": [11],
"chef": [6],
"l\'exist": [5],
"souhaitez": [11,6,5,[2,4,9,10]],
"ltr": [6],
"d\'utilis": [11,[2,5],[4,6,10]],
"dès": [8,[1,10,11]],
"optionsexttmxmenuitem": [3],
"downloaded_file.tar.gz": [5],
"processus": [11,6],
"l\'extérieur": [9],
"build": [5],
"maîtr": [11],
"lettr": [8,2,11,[3,6]],
"lue": [[5,6]],
"marketplac": [5],
"account": [11],
"lui": [5,11,9],
"rassembl": [[2,6]],
"opérat": [[6,9,10,11]],
"d\'en": [[8,11]],
"dhttp.proxyhost": [5],
"rencontrés": [7],
"entries.s": [11],
"lus": [[1,10]],
"gotonextuntranslatedmenuitem": [3],
"targetlocal": [11],
"path": [5],
"suggèr": [9],
"des": [11,6,8,5,10,9,[1,4],3,7,2,0],
"bidirectionnel": [6],
"barre": [7],
"flèche": [11],
"double-cl": [8],
"strict": [6],
"soustray": [11],
"citat": [2],
"contient": [10,5,[6,11],9,[7,8]],
"pass": [[8,11],6,[5,10]],
"allsegments.tmx": [5],
"afin": [11,[5,6,10],[3,9]],
"impact": [11],
"stade": [10],
"court": [11],
"passez": [9,4],
"basculé": [6],
"configur": [11,5,[4,8],3,1],
"helpcontentsmenuitem": [3],
"trouvant": [8],
"domain": [11],
"tâche": [5],
"omegat-org": [6],
"équipe": [7],
"unicode": [7],
"comptoir": [0],
"descript": [[3,11],[5,6]],
"impératif": [11],
"contienn": [6,11,9],
"d\'outil": [2],
"projectaccessdictionarymenuitem": [3],
"contrôlé": [11],
"chifffr": [2],
"identifié": [8],
"hor": [6,5],
"optionsworkflowmenuitem": [3],
"pénalité": [10],
"désactivé": [8,11],
"journal": [8,3],
"l\'intérieur": [10,[0,1,2,4,5,11]],
"releas": [6,3],
"crédit": [8],
"étant": [11,8],
"sentent": [11],
"term": [1,11,9,8,[3,6]],
"volets": [7],
"modifié": [6,[5,8,11],3,1],
"sparc": [5],
"explic": [5],
"dépendr": [6],
"caractères": [7],
"logiques": [7],
"modificateur": [3],
"manquant": [8,3,9],
"project_save.tmx.annéemmjjhhmn.bak": [6],
"n\'insèr": [8],
"voulez": [11,[3,5,6,8,9]],
"duden": [9],
"connaiss": [6],
"caractéristiqu": [11],
"march": [8,4],
"côtés": [6],
"gestion": [6,7],
"d\'entrer": [11],
"atteindre": [7],
"maximum": [[3,6]],
"délimit": [[8,11]],
"n\'ont": [9,11],
"spotlight": [5],
"seriez": [6],
"multiples": [7],
"plusieur": [11,5,1,[4,6,8,9,10]],
"étape": [[6,11]],
"accéder": [3,11,[5,8]],
"appelé": [5,[4,11],[3,10]],
"inchangé": [11],
"avides": [7],
"dir": [5],
"d\'insérer": [11],
"dit": [5,9],
"div": [11],
"subdir": [6],
"dix": [8],
"vraiment": [11,5],
"manièr": [6,11,10],
"exemples": [7],
"séparateur": [9],
"trait": [11],
"viewfilelistmenuitem": [3],
"accédez": [5],
"précise": [5],
"signal": [2],
"train": [9],
"favorisé": [11],
"encodag": [11,1],
"test": [5],
"activé": [11,[8,9]],
"simplement": [11,[1,5]],
"trié": [[10,11]],
"omegat": [5,6,11,8,10,7,4,3,9,[0,1,2]],
"ietr": [[6,8]],
"imprim": [11],
"repli": [8],
"présenté": [1],
"allemand": [11],
"soit": [11,6,[4,9],[2,5],[1,10]],
"exécut": [5,11,[6,8],[3,7]],
"fonction": [11,8,9,4,[5,10],[0,1,6]],
"celles-ci": [11,6],
"file-source-encod": [11],
"montr": [[0,9,10,11]],
"reflèt": [8],
"orthographiques": [7],
"tant": [11,6,8,2],
"session": [11,[5,10]],
"console-align": [5],
"entr": [11,6,[1,2,9,10]],
"somm": [11],
"navigateur": [[5,8]],
"lequel": [11,5,[9,10]],
"ms-dos": [5],
"diminué": [10],
"projectopenrecentmenuitem": [3],
"sous-menu": [5],
"risquer": [6],
"petit": [8],
"s\'i": [0],
"passer": [11,8,5],
"mise": [6,[5,11],9,8],
"prévus": [5],
"quelconqu": [[10,11],9],
"collabor": [6],
"coupl": [[8,9]],
"d\'ignor": [[4,8]],
"requiert": [4],
"editexportselectionmenuitem": [3],
"custom": [11],
"trop": [11],
"und": [4],
"grand": [[2,4,6,11]],
"une": [11,6,8,5,2,3,[9,10],[1,4]],
"troi": [6,10,[0,9],1],
"condit": [[0,6]],
"glyph": [8],
"d\'interfac": [[5,10]],
"projectaccesstargetmenuitem": [3],
"partir": [5,11,[3,6,7,8],10],
"fournit": [5,11,4],
"surlign": [9],
"editoverwritemachinetranslationmenuitem": [3],
"relat": [11],
"ingreek": [2],
"appell": [11],
"bureaux": [5],
"illustrations": [7],
"comportera": [10],
"donnez-lui": [6],
"fusionn": [6],
"insèr": [[8,11]],
"cocher": [[8,11]],
"disposé": [8],
"fiabl": [11,10],
"n\'étant": [2],
"nouveau": [6,11,5,[4,8],3,[1,9]],
"es_es.aff": [4],
"visibl": [[6,9,10,11]],
"convers": [6],
"construct": [2],
"ignor": [11,[8,10]],
"mélang": [6],
"décision": [10],
"linguistiqu": [11],
"influenc": [6],
"familiarisé": [6],
"projectexitmenuitem": [3],
"aligndir": [5],
"contenus": [10,11],
"sont": [11,6,5,8,[1,9],10,3,0,[2,4]],
"system-host-nam": [11],
"action": [8,3,[0,5,9]],
"signet": [11],
"text": [11,6,8,9,10,1,[2,3],5,4],
"redémarré": [3],
"astuc": [11],
"editregisteruntranslatedmenuitem": [3],
"init": [6],
"creat": [11],
"fermé": [6],
"python": [11],
"es_mx.dic": [4],
"créez": [6,4],
"précisé": [5],
"infix": [6],
"créer": [11,6,[5,8],3,[9,10],1],
"créés": [10,8,[1,6]],
"accepté": [[3,5,10]],
"utilisez-la": [5],
"adapté": [5,11],
"maco": [5,3,[1,6,11]],
"envi": [11],
"éléments": [7],
"réalisé": [11],
"tarbal": [0],
"d\'except": [11],
"invalid": [5,6],
"doc": [6],
"pluriel": [1],
"statut": [11,[9,10]],
"portugai": [[4,5]],
"appliquez": [4],
"gardant": [10],
"paramet": [5],
"adress": [5],
"diver": [11],
"mac": [[3,5]],
"éventuel": [[5,6,9]],
"file": [11,6,[5,8]],
"mai": [6,11,[1,2],9,[4,5,8,10]],
"discrèt": [5],
"maj": [3,[6,11],8],
"travail": [5,[6,9],11],
"mal": [11,[4,8]],
"gauch": [6,11,[8,9]],
"man": [5],
"libre": [7],
"map": [6],
"combiné": [5],
"may": [11],
"prise": [11,[2,5,8]],
"tard": [[9,11]],
"édition": [1],
"menu": [3,7,11,[8,9],5,[1,4,6]],
"quantificateur": [2],
"l\'arrière-plan": [10,8],
"url": [6,[4,11]],
"incompat": [3],
"discret": [5],
"négation": [2],
"uppercasemenuitem": [3],
"excluant": [6],
"viewmarkuntranslatedsegmentscheckboxmenuitem": [3],
"ultérieur": [[5,6,10],11],
"a-za-z": [2,11],
"pourra": [1],
"probabl": [[5,9,11]],
"globaux": [11],
"cochez": [11,[4,5,8]],
"individuell": [11],
"bonus": [5],
"use": [[5,8]],
"s\'en": [11],
"main": [[5,11]],
"omegat.jar": [5,[6,11]],
"source-pattern": [5],
"adéquat": [[1,4]],
"omegat.app": [5],
"usr": [5],
"peut-êtr": [11,6],
"plutôt": [[5,6,9,11]],
"international": [6],
"marquag": [8],
"seulement": [11,6,8,1],
"liste": [7],
"utf": [1],
"caché": [[10,11]],
"sort": [[6,11],[2,5,9]],
"d\'interrog": [9],
"signif": [11],
"servic": [5,11,8],
"rencontr": [11],
"obtenir": [5,[6,9,11]],
"synchronisé": [6,11],
"prenez": [10],
"d\'identif": [11],
"true": [5],
"orthograph": [4,3],
"l\'espac": [11],
"dsl": [0],
"méta": [11,3],
"critiqu": [11],
"désiré": [5,[0,6]],
"longueur": [9],
"déposez": [5],
"l\'auteur": [[8,9]],
"d\'où": [11],
"groovi": [11],
"troisièm": [1,9],
"prend": [6,11],
"défiler": [11,9],
"sommair": [[0,1,2,3,4,5,6,8,9,10,11]],
"multi-paradigm": [11],
"était": [8,[6,9]],
"d\'équival": [8],
"devoir": [[10,11]],
"mineur": [11],
"n.n_windows_without_jre.zip": [5],
"med": [8],
"lectur": [6],
"électroniqu": [9],
"sous": [5,6,11,8,[1,3]],
"feront": [2],
"l\'avantag": [5],
"transform": [8],
"l\'altern": [[5,11]],
"grisé": [8,11],
"dtd": [5],
"fléchées": [[9,11]],
"auparav": [8],
"créée": [11,9],
"immédiat": [[1,8]],
"dictionair": [6],
"tentat": [5],
"affichage": [7],
"fichier": [11,6,5,8,10,1,4,9,3,0,7],
"celui-ci": [6,11,[4,9]],
"l\'exempl": [9,11,[0,2,4,5,6]],
"annoté": [8,3],
"expérimenté": [5],
"projectcompilemenuitem": [3],
"console-transl": [5],
"préservé": [11],
"master": [6],
"kmenuedit": [5],
"collègu": [9],
"gotonextuniquemenuitem": [3],
"gratuit": [5,[4,11]],
"conform": [[4,6,11]],
"n\'indiqu": [11],
"numéro": [8,[5,11],[3,6,9]],
"correcteur": [4],
"xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx": [5],
"fichiez": [6],
"déposer": [9],
"writer": [6],
"recommandé": [11],
"wordart": [11],
"princip": [11,[3,6,9]],
"dalloway": [11],
"rubi": [11],
"optionsviewoptionsmenuitem": [3],
"dur": [[5,6,8,11]],
"inform": [11,5,[3,8],6,[0,2]],
"s\'il": [8,11,[5,10],9,[0,3,4,6]],
"puiss": [11,10],
"habituel": [8],
"commit": [6],
"targetlocalelcid": [11],
"pourrait": [4],
"danger": [6],
"project_stats_match.txt": [10],
"archivez-l": [6],
"dvd": [6],
"xmx2048m": [5],
"revoir": [10],
"grâce": [5],
"mégaoctet": [5],
"abaissé": [10],
"précédés": [2],
"suscept": [6],
"xxxxxxxxxxxxxxxx.xxxxxxxxxxxxxxxx.xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx": [5],
"supporté": [11],
"répétées": [11],
"user.languag": [5],
"regex": [2],
"d\'un": [6,11,10,8,[2,5,9],[3,4,7]],
"qu\'un": [11,[5,6,8],10],
"installer": [7],
"meta": [3],
"keystrok": [3],
"risqu": [6],
"entraîn": [11],
"except": [11],
"krunner": [5],
"libreoffic": [4,[6,11]],
"programm": [5,11,6],
"stocké": [11,[5,8]],
"décompréssé": [5],
"conçus": [9],
"approprié": [6,5,[8,11]],
"lématis": [11],
"d\'instruct": [11],
"extrémité": [2,8],
"global": [8,11,1],
"précisent": [5],
"réparat": [6],
"texte": [7],
"racin": [6,11,[3,5,8,10]],
"l\'envoy": [6],
"long": [11],
"l\'argument": [5],
"chiffr": [11,[2,5,6,10]],
"signalera": [6],
"mis": [11,6,1],
"navigu": [11],
"gestionnair": [6,4,11],
"suppr": [11,9],
"préfix": [11,10],
"légèrement": [9],
"enregistrera": [8],
"commande": [7],
"l\'instal": [5,9],
"puissant": [11,[2,6]],
"viewdisplaysegmentsourcecheckboxmenuitem": [3],
"brésil": [5],
"d\'amélior": [[8,11]],
"editregisteremptymenuitem": [3],
"non-uniqu": [11],
"ibm": [5],
"commenç": [[2,11]],
"polic": [[8,11],3],
"progress": [9],
"décoché": [11],
"open": [11],
"ratio": [11],
"décompressé": [5],
"fichiers_exclus": [6],
"www.oracle.com": [5],
"entrant": [[8,11]],
"comprenn": [11],
"rapid": [[5,11]],
"project": [6,3],
"xmx1024m": [5],
"ici": [11,6,[5,8]],
"barr": [9,[5,11]],
"monprojet": [6],
"penalty-xxx": [10],
"quittant": [11],
"outil": [11,[6,8],3,[2,10],[5,9]],
"gotonextsegmentmenuitem": [3],
"invers": [5],
"nnn.nnn.nnn.nnn": [5],
"n\'avez": [6],
"remplir": [6],
"réseau": [[5,6,11]],
"envoyé": [[8,11]],
"dépôt_du_projet_omegat_en_équip": [6],
"agrandit": [9],
"ln-co": [11],
"abort": [5],
"guid": [6,[0,3,5,7,8,10,11]],
"encombr": [11],
"laquell": [[4,6,8]],
"idx": [0],
"retraduit": [6],
"internet": [11,4],
"confondr": [11],
"que": [11,6,5,[8,9,10],4,3,[0,1],2],
"jusqu\'à": [11,[2,5,6]],
"réparer": [6],
"langue-pay": [11],
"brut": [6,[1,11]],
"arriv": [11,[6,10]],
"qui": [11,6,5,8,9,2,10,4,[1,3]],
"s\'arrêter": [[4,11]],
"entité": [11],
"projectaccesscurrentsourcedocumentmenuitem": [3],
"remplit": [6],
"linux": [5,[2,7,9]],
"travaux": [[5,9]],
"abrégé": [11],
"traduirez": [11],
"génériqu": [11],
"n\'apparaiss": [11],
"checkout": [6],
"affichag": [11,3,8,1],
"appuyé": [11],
"majuscul": [2,[3,8],[5,11]],
"compt": [11,5,8,3],
"héritent": [6],
"fermant": [9],
"saisir": [11,[5,6]],
"vouloir": [11],
"existant": [11,6,5,10],
"commenc": [11,[3,5,6]],
"es-mx": [4],
"préserver": [11],
"ci-dessus": [6,[9,11],5,[2,4,8,10],[0,1]],
"ifo": [0],
"lemmatis": [[1,9,11],3],
"lorsqu\'ell": [11],
"venant": [6],
"segmenté": [11],
"comment": [0,[7,11]],
"comprend": [6,[5,11]],
"base": [[5,6,8,11]],
"sépare": [11],
"enfin": [[5,11]],
"octal": [2],
"fera": [[4,11]],
"téléchargez": [5,[0,6]],
"appel": [11],
"moi": [6,5],
"xx.docx": [11],
"aviez": [8],
"fait": [[6,11],5,[8,9],1],
"définissez": [11],
"fair": [11,6,9,[4,5,10]],
"mon": [6],
"consist": [11,6],
"mot": [11,8,2,9,[4,5,10],[1,3,6]],
"repris": [11],
"ferm": [[8,11]],
"optionsautocompleteautotextmenuitem": [3],
"thème": [11],
"télécharger": [[0,3,8],[4,5,6,7,11]],
"l\'inclus": [6],
"cibl": [11,[6,8],4,9,1,[5,10],3],
"apercevez": [4],
"ayant": [10,[2,4,9,11]],
"compress": [11],
"d\'échappement": [2],
"idé": [11],
"décochez": [11],
"concis": [0],
"décocher": [11],
"permett": [[2,5,8,9,11]],
"word": [6,11],
"d\'hôte": [11],
"langag": [11],
"term.tilde.com": [11],
"sachant": [3],
"extension": [7],
"autoamtiqu": [[8,11]],
"valeur-clé": [11],
"réinitialis": [9,[3,11]],
"propag": [11],
"publié": [6],
"validateur": [[5,11]],
"général": [9,11,6],
"viewmarknotedsegmentscheckboxmenuitem": [3],
"installation": [7],
"convient": [[4,6,9]],
"non-vis": [11],
"ignoré": [11,[3,5]],
"converti": [11,6],
"supprimez": [[6,10]],
"fonctionnera": [4],
"d\'api": [5,11],
"gère": [11,6],
"en-têt": [[8,11]],
"lingvo": [0],
"détaillé": [[5,11]],
"gotomatchsourceseg": [3],
"quantificateurs": [7],
"mrs": [11],
"optionssaveoptionsmenuitem": [3],
"excel": [11],
"comma": [1],
"agenc": [9],
"puissent": [6],
"correspondances": [7],
"s\'oppos": [11],
"concerné": [11,[4,5]],
"runn": [11],
"règles": [11,2,[5,6,10]],
"trouvé": [11,[2,5],1],
"basé": [11,[4,5]],
"permet": [11,8,9,5,6,[2,4,10]],
"stardict": [0],
"omegat.l4j.ini": [5],
"span": [11],
"voici": [[5,11]],
"désarchiv": [[0,5]],
"obligatoir": [11],
"d\'export": [11],
"aller": [9],
"souligné": [[1,4,9]],
"l\'espagnol": [4],
"d\'aid": [6],
"space": [11],
"repèr": [11],
"pt_pt.aff": [4],
"quantité": [5,[4,9]],
"méthode": [5,11,6,8],
"simpl": [[2,5],[4,6,11],1],
"appar": [11],
"html": [11,5],
"dérogat": [6],
"allez": [[6,11]],
"thunderbird": [4,11],
"ver": [11,8,[0,6]],
"editselectfuzzy3menuitem": [3],
"l\'id": [5],
"artund": [4],
"remplac": [11,8,3,9,10],
"fals": [[5,11]],
"project.projectfil": [11],
"nièm": [8],
"identiqu": [11,8,[6,10],[5,9],3],
"finit": [[2,9]],
"avancé": [11],
"d\'accè": [11,5],
"qualité": [6,[8,10]],
"conseil": [6,4,5],
"verrouillag": [5],
"n\'hésitez": [6],
"configuration": [7],
"www.ibm.com": [5],
"mesfichi": [6],
"manuel": [8,[4,6,11],1],
"contextuell": [11],
"shortcut": [3],
"dernièr": [8,3,[5,10,11],6],
"russ": [5],
"désigner": [3],
"glisser": [11,9,5],
"deviendront": [11],
"annex": [[1,2,4],[0,3],6],
"pt_br.aff": [4],
"tmx2sourc": [6],
"sauvegardé": [11,[6,10],9],
"répandu": [11],
"l\'ordinateur": [5,11],
"ini": [5],
"personnalisation": [7],
"contenir": [[5,6,9,10,11],[3,4]],
"command": [5,11,8,6,[3,9]],
"dessin": [11],
"désign": [11],
"n.n_without_jr": [5],
"inspiré": [11],
"défilement": [11],
"dhttp.proxyport": [5],
"concernant": [7],
"respectif": [6],
"préparation": [7],
"dernier": [8,[5,10]],
"retir": [6],
"qu\'en": [[0,11]],
"sorti": [6,11],
"viewmarkbidicheckboxmenuitem": [3],
"subrip": [5],
"testé": [6],
"d\'attribut": [11],
"préférenc": [8,11,5,[1,3,6]],
"via": [11,5],
"traitent": [6],
"editeur": [9],
"score": [11],
"qu\'ell": [6,[3,5,11]],
"compteur": [9],
"tapant": [[5,8,11]],
"absent": [9],
"volum": [11],
"donnent": [5],
"approxim": [11],
"entour": [9],
"savez": [11],
"séparat": [11],
"instruct": [5,[6,11]],
"l\'aid": [[5,6,11],[9,10]],
"d\'applic": [5],
"d\'appuy": [8],
"décompress": [5,0],
"courant": [[1,11]],
"supprimm": [11],
"raw": [6],
"version": [5,6,8,[2,4,7,11]],
"conserv": [11,[5,6]],
"réutilis": [6,11],
"diagramm": [11],
"d\'omegat.project": [6],
"charger": [11,8],
"l\'ais": [[5,6]],
"même": [11,6,5,[2,8,9],[0,10]],
"aaa": [2],
"contemporari": [0],
"solari": [5],
"n\'incluant": [6],
"projecteditmenuitem": [3],
"l\'historiqu": [8,3],
"chargez": [[6,11]],
"britannica": [0],
"communiqu": [6],
"n\'aura": [[5,9]],
"indéfini": [1],
"machin": [11,5,8],
"d\'une": [11,5,[2,8,10],9,[4,6,7]],
"élargir": [11],
"décimal": [11],
"périod": [6],
"abc": [2],
"rcs": [6],
"conten": [11,[1,4,5,8,9,10]],
"ceci": [2,11,5,6,[4,8,9]],
"l\'on": [[8,11]],
"l\'os": [1],
"iceni": [6],
"annulé": [[6,8]],
"stratégi": [11],
"signifi": [11,[0,6,9]],
"redonn": [11],
"mexicain": [4],
"notam": [11],
"instructions": [7],
"panneaux": [5],
"noter": [11,[6,10]],
"notes": [7],
"critèr": [11],
"iso": [1],
"répétition": [11,8,9],
"supprim": [11,[4,5,8],[3,6,9]],
"qu\'il": [11,[6,10],[2,5,8],4],
"notez": [5],
"statistiqu": [8,[3,10],6,11],
"enregistr": [8,11,3,[0,5,6]],
"rêgles": [6],
"traitera": [5],
"post": [6],
"glossary.txt": [6,1],
"dessus": [[5,11]],
"finiss": [5],
"contiendrait": [11],
"utilisé": [11,6,5,4,8,[2,3,9,10]],
"démarreront": [5],
"réutiliser": [7],
"dsun.java2d.noddraw": [5],
"antérieur": [11],
"rem": [[3,8]],
"wikipédia": [8],
"coché": [11,8,4],
"add": [6],
"passé": [11],
"initi": [6],
"ell": [11,[5,6,8,9]],
"ceux-ci": [[6,11]],
"résumé": [7],
"séparer": [6],
"altéré": [6],
"réécrire": [11],
"accè": [5,[8,11],[0,2,6]],
"s\'échanger": [6],
"inconditionnel": [10],
"x0b": [2],
"l\'option": [8,11,[6,9,10]],
"respect": [2,[6,11]],
"arrièr": [[8,11]],
"fusion": [11],
"récupéré": [[5,11]],
"faux": [11],
"canada": [5],
"port": [5,10],
"faut": [[6,10]],
"altern": [[9,11],6,[3,8]],
"http": [6,5,11],
"optionsautocompleteshowautomaticallyitem": [3],
"préférabl": [11],
"apparaiss": [11,3,6],
"larouss": [9],
"testeur": [2,7],
"untar": [0],
"d\'encodag": [11],
"interfér": [8],
"chevauch": [9],
"monsieur": [11],
"retiré": [11],
"filters.conf": [5],
"parfoi": [11,5],
"non-généré": [11],
"projectsinglecompilemenuitem": [3],
"vos": [5,9,[0,3,6,8,11]],
"lisent": [6],
"chapitr": [11,[2,6,9]],
"utilisateurs": [7],
"modifi": [11,5,[1,6],9,8,10,3],
"espac": [11,2,8,[1,3]],
"pouvez": [11,5,9,6,4,[0,3,8,10]],
"visuel": [8],
"s\'appell": [11],
"pour": [11,5,6,8,9,1,3,10,4,2,0,7],
"erreur": [8,6,11,5],
"particip": [6],
"n\'affect": [11],
"recherché": [11],
"rechargez": [11],
"textuel": [6],
"exempl": [11,6,2,5,4,[8,9,10],0,[1,3]],
"d\'accéder": [9],
"clone": [6],
"remplacez": [9],
"joker": [[6,11]],
"targetlanguag": [11],
"taper": [11],
"trier": [11],
"copyright": [8],
"officiel": [7],
"pertes": [7],
"tapez": [[5,9]],
"préférée": [[9,11]],
"post-trait": [11],
"durant": [11,5],
"system-os-nam": [11],
"occurr": [11,[4,9]],
"combinaison": [[0,3,5,8]],
"systèm": [5,11,6,[4,8],[3,10]],
"editselectfuzzyprevmenuitem": [3],
"optionstabadvancecheckboxmenuitem": [3],
"trouver": [11,[1,3,5,6],0],
"d\'enregistr": [9],
"identifi": [11,6],
"rien": [2,8,3],
"spécifiant": [5],
"simpledateformat": [11],
"optionsviewoptionsmenuloginitem": [3],
"onglet": [9],
"néerlandais": [6],
"algorithm": [11],
"décidez": [11],
"re-cochez": [11],
"tar.bz2": [0],
"effacé": [6],
"l\'un": [8,[0,5]],
"comport": [11,5,[0,8,9,10]],
"dépôts": [[6,11]],
"invit": [6],
"décider": [11],
"typiqu": [5],
"bundle.properti": [6],
"script": [11,8,5],
"spécific": [11],
"versionné": [10],
"system": [11],
"pertinent": [11,[3,8,10]],
"spellcheck": [4],
"x64": [5],
"poursuivr": [11],
"l\'éditeur": [11,[5,8,9,10]],
"n\'apparaît": [11],
"aid": [3,8],
"issu": [[8,11]],
"partiel": [[9,11]],
"cadr": [5],
"keyev": [3],
"d\'antislash": [2],
"gigaoctet": [5],
"ajust": [11],
"lancement": [5],
"ruptur": [11],
"cell": [11,[6,10],9,2],
"isn\'t": [2],
"ait": [11],
"local": [6,8,5,11],
"valid": [11,3,[5,8],6],
"assur": [[8,11]],
"interfac": [5,11],
"fermera": [8],
"projet": [6,11,8,10,5,3,[1,9],7,4,0],
"traductric": [6],
"locat": [5],
"plusier": [8],
"ordinateur": [[4,5,7,11]],
"commencez": [6],
"indiqu": [11,6],
"s\'appui": [11],
"optionsteammenuitem": [3],
"crée": [8,5,11],
"générale": [11,[6,8]],
"trouvez": [4],
"appliqué": [11,6],
"gzip": [10],
"enregistré": [1,11,[6,8,9]],
"n\'import": [[2,11],[8,9],[5,10],[3,4]],
"généralement": [[2,5,6,11],10],
"parenthès": [11],
"notif": [11],
"donnez": [5],
"ci-dess": [5,[2,9],[3,4,6,11]],
"x86": [5],
"créatif": [11],
"cela": [11,5,9,[6,10],[0,2,4]],
"cite": [2],
"évidem": [4],
"séparé": [11,1,[6,9],8],
"papier": [9],
"s\'arrêtera": [5],
"chinoi": [6,11],
"nostemscor": [11],
"demi-largeur": [11],
"ignorera": [11],
"déconnectez-v": [6],
"est": [11,6,5,[8,9],10,4,1,3,2,0],
"es_mx.aff": [4],
"l\'étape": [10,[6,9,11]],
"vue": [11,9],
"filtr": [11,6,8,5,[3,10]],
"donner": [8],
"correspond": [11,6,9,8,2,[3,10],1,4,5],
"modifiez": [[4,5,9],[1,3,11]],
"facultativ": [5],
"console-createpseudotranslatetmx": [5],
"mode": [5,6,[9,11]],
"copier": [[4,6],8,[3,5,9,10]],
"puisqu": [11,9],
"basant": [11,4],
"etc": [11,[5,6,9],[0,2,10]],
"l\'exécut": [5,11],
"longman": [0],
"fuzzyflag": [11],
"copiez": [6,5],
"préférences": [7],
"toolsshowstatisticsstandardmenuitem": [3],
"bureau": [5,[9,11]],
"été": [8,11,6,[1,5],9,10],
"oblig": [5],
"merriam": [0,[7,9]],
"s\'ouvr": [[8,11],9],
"l\'esprit": [11],
"read": [11],
"fichier.txt": [6],
"traduire": [7],
"laissé": [11,[5,6]],
"alt": [[3,5,11]],
"touch": [3,11,8,9,1],
"réel": [[9,11]],
"spécifié": [11,5,[3,6,8]],
"ensuit": [11,[3,5,6,8,10]],
"projectname-omegat.tmx": [6],
"supprimé": [11,10],
"lancez-l": [5],
"glossair": [1,11,9,3,8,[6,10],[0,4]],
"tool": [11],
"outils": [7],
"illimité": [[5,10]],
"invari": [11],
"couper": [9],
"intervall": [[6,11]],
"collect": [11,9],
"unix": [5],
"n\'apparait": [11],
"écrite": [11],
"roc": [6],
"cellul": [11],
"eux": [[6,8]],
"qu\'on": [10],
"autres": [7],
"n.n_without_jre.zip": [5],
"reçu": [[5,9,11]],
"bogu": [8],
"calcul": [11],
"and": [5],
"chose": [10,[9,11]],
"predict": [8],
"magento": [5],
"créé": [6,1,[4,5,11]],
"n\'affich": [[8,11]],
"ant": [[6,11]],
"désarchivag": [0],
"l\'utilis": [[5,11],6],
"mémoires": [7],
"supprimez-l": [9],
"n\'activez": [11],
"traducteur": [6,9,11,10],
"bidirectionnalité": [8,3],
"d\'utilisation": [7],
"saisi": [11,[3,8],6,[1,9]],
"cité": [10],
"simultané": [8],
"u00a": [11],
"helplastchangesmenuitem": [3],
"néanmoin": [11],
"localis": [[5,6]],
"surligné": [8],
"omegat.ex": [5],
"fenêtre": [7],
"largeur": [11],
"shift": [3,[1,8,11]],
"n\'interférera": [5,11],
"sourcetext": [11],
"ouvertur": [5],
"couch": [6],
"compos": [11,[5,6,9]],
"totalité": [6],
"utiliserez": [10],
"java": [5,11,3,2,[6,7]],
"exe": [5],
"l\'onglet": [8],
"stockag": [11,4],
"boit": [11,[4,8]],
"english": [0],
"jar": [5,6],
"l\'hôte": [5],
"api": [5],
"lang2": [6],
"lang1": [6],
"ambival": [11],
"editselectfuzzy2menuitem": [3],
"project_save.tmx": [6,10,11],
"textuelle": [7],
"subi": [8],
"lancé": [5],
"multiplateform": [5],
"jaun": [[8,9]],
"dictionari": [0,10,8],
"progressif": [11],
"marquer": [8,3,[1,11]],
"demand": [6,[5,11],[4,8]],
"démarrag": [5],
"dictionary": [7],
"avant-arrièr": [11],
"progressiv": [[10,11]],
"codag": [1],
"devez": [11,5],
"logiciel": [11,6],
"obtenez": [5],
"graphiqu": [5,[6,9,11]],
"vide": [11,6,[8,10],3,[1,5,9]],
"déplacé": [[9,11]],
"besoin": [6,[5,11],[2,9]],
"envoi": [8,11],
"différenc": [11,[1,6,9]],
"réalis": [4],
"pratiques": [7],
"editselectfuzzynextmenuitem": [3],
"presse-papi": [8],
"d\'affich": [8,11],
"surbril": [11],
"cett": [11,8,5,[6,9],10,3],
"recommenc": [11],
"devis": [2],
"read.m": [11],
"traduction": [7],
"différent": [11,[6,8],9,[4,10]],
"envoy": [6,[8,11]],
"visionn": [10],
"readme.bak": [6],
"langu": [11,[5,6],4,9,[0,8,10]],
"généré": [8],
"sécurisé": [11],
"raccourcis": [7],
"similair": [11],
"timestamp": [11],
"précédent": [8,3,6,9,11,[0,1,2,4,5,10]],
"art": [4],
"projectaccessrootmenuitem": [3],
"dyandex.api.key": [5],
"rtl": [6],
"suppress": [[10,11]],
"tout": [11,6,5,9,[2,8,10],[1,3,4]],
"gras": [11,9,1],
"prendr": [[3,4,8,10]],
"peuvent": [11,6,10,[5,8],1,9,2],
"déroulant": [11,[4,8]],
"tous": [11,5,6,8,9,3,2],
"volet": [9,11,8,[1,6],[7,10]],
"jdk": [5],
"mentionné": [5],
"facilit": [6],
"n\'arriv": [5],
"moin": [[5,6],[10,11]],
"plugin": [11],
"état": [6],
"tabul": [[1,2,11]],
"sécurité": [11,5],
"boît": [11,6],
"aurez": [5,10],
"pourrez": [[3,4,8,11]],
"textuell": [11,[6,8]],
"rigoureus": [11],
"chargement": [11],
"glossar": [1],
"toolsshowstatisticsmatchesperfilemenuitem": [3],
"editinsertsourcemenuitem": [3],
"verrouillé": [[5,9]],
"qu\'à": [11,[2,4]],
"référenc": [6,1,[2,9,11]],
"run": [11,5],
"viterbi": [11],
"microsoft": [11,[5,6],9],
"d\'instal": [5,[4,11]],
"projectnewmenuitem": [3],
"hexadécimal": [2],
"racinis": [9],
"efficac": [11],
"optionstranstipsenablemenuitem": [3],
"utilis": [11,5,6,4,[3,8],1,0,9],
"segment": [11,8,9,3,10,6,[1,5],2],
"publiez": [6],
"l\'outil": [[7,11]],
"désarchivez": [5],
"jet": [11],
"titlecasemenuitem": [3],
"d\'instanc": [8],
"jeu": [[1,11]],
"inférieur": [[9,11]],
"d\'appliqu": [11],
"publier": [6],
"glossari": [1,[6,10,11],9],
"editcreateglossaryentrymenuitem": [3],
"ignored_words.txt": [10],
"puisqu\'il": [11],
"suivit": [[2,3]],
"configuration.properti": [5],
"github.com": [6],
"examin": [11],
"lesquell": [5],
"essayera": [11],
"prototyp": [11],
"paramètres": [7],
"ceux": [[2,5,8,11]],
"conservera": [10],
"expressions": [7],
"glossary": [7],
"lorsqu": [11,6,9,5,8,[4,10]],
"gauche": [7],
"compri": [11,9,8],
"échap": [[1,11]],
"d\'abord": [[4,6,11]],
"anglai": [6,2,[5,11]],
"pertin": [11],
"démarrez": [[5,6]],
"ayez": [11,6],
"datant": [6],
"string": [5],
"import": [[5,6],11,10,9],
"aux": [11,5,6,8,[1,2,4,10]],
"classes": [7],
"déplace": [8,11],
"démarrer": [5,11],
"edition": [7],
"non": [11,8,[3,5,6,9],10,2],
"nom": [11,[5,9],6,[4,10],[0,1,8]],
"vidé": [[8,11]],
"outr": [11],
"lorsqu\'il": [11,[6,9]],
"s\'arrêt": [11],
"protégé": [10],
"not": [11],
"avi": [9],
"terminologiqu": [11],
"parcourir": [[3,4,5,11]],
"collé": [8],
"suivez": [6,5,11],
"l\'accè": [11],
"double-cliqu": [[5,9,11]],
"bonn": [[1,10,11]],
"associ": [8],
"l\'ouvrir": [11,9],
"l\'opérat": [11],
"particulièr": [11,[5,6]],
"physiqu": [4],
"mond": [6],
"effet": [11,[8,9]],
"serait": [5,[6,11]],
"selection.txt": [11,8],
"target": [10,[8,11],7],
"xhtml": [11],
"cliquant": [11,[5,9],6],
"empaqueté": [8],
"situé": [[5,11],1,[8,9]],
"soustract": [2],
"finder.xml": [11],
"effaç": [11],
"spécifier": [11,5,[3,10]],
"orthographiqu": [4,11,10,[1,2,8]],
"accélérer": [6],
"grec": [2],
"window": [5,[0,2,8]],
"config-dir": [5],
"insérant": [3],
"matières": [7],
"copiera": [11],
"contrat": [5],
"l\'utilisateur": [5,11,7,[3,8,9],10],
"l\'état": [[8,9,10,11]],
"plateform": [5,[1,11]],
"aprè": [11,[1,5],6,[2,3,4,8,9]],
"disable-project-lock": [5],
"autant": [6],
"donné": [[6,11],5,[0,1,10]],
"omegat.pref": [11],
"allant": [11],
"termbas": [1],
"logiciell": [5],
"souvient": [8],
"fai": [11],
"cass": [3,11,[2,8]],
"n\'ait": [8],
"possédant": [[8,11]],
"genr": [11,10],
"personalis": [11],
"non-avides": [7],
"l\'appel": [11],
"ouvr": [8,[4,11]],
"conséquenc": [11,9],
"d\'affichag": [6,11],
"singuli": [1],
"règle": [11],
"rétablir": [[3,8]],
"case": [11,4,5],
"d\'extens": [11,9],
"multipl": [9],
"propriété": [11,6,[4,5,8],[0,1,3,10]],
"conséquent": [5],
"source-c": [0],
"duré": [11],
"violet": [8],
"modul": [11],
"pt_pt.dic": [4],
"auteur": [11],
"explicit": [11],
"re-saisi": [11],
"targettext": [11],
"d\'utilisateur": [6,9],
"glisser-déplacer": [7],
"futur": [10,6],
"réduit": [9],
"trouvent": [[8,10]],
"signalé": [8],
"droit": [6,[9,11],5,8],
"remi": [6],
"style": [6],
"suit": [5,[0,9,10,11]],
"effectué": [6,11,[5,8],9],
"level1": [6],
"level2": [6],
"implicit": [11],
"suffisam": [[6,10]],
"consultez": [6,[2,8]],
"orang": [8],
"direct": [5,11,[6,8],10],
"connexion": [6,11,[3,4]],
"aaabbb": [2],
"n\'est": [5,11,8,6,[2,9,10],1,4],
"caus": [5,1],
"d\'entr": [[8,9]],
"récent": [8,[3,5,6]],
"web": [5,[6,7,10,11]],
"choisissez": [11,5,8],
"edittagpaintermenuitem": [3],
"enregistrez-l": [6],
"clé-valeur": [11],
"protect": [11],
"passag": [2],
"optionscolorsselectionmenuitem": [3],
"provoqu": [[6,8]],
"dossier_de_configur": [5],
"hiérarchi": [10],
"sembl": [[5,11]],
"saisiss": [11],
"alor": [11,6,[4,9,10]],
"editselectfuzzy4menuitem": [3],
"editregisteridenticalmenuitem": [3],
"gris": [8],
"more": [11],
"compteurs": [7],
"display": [11],
"lorsqu\'un": [11,8,[1,6,10]],
"unicod": [2],
"viewmarknbspcheckboxmenuitem": [3],
"recherche": [7],
"optiqu": [[6,11]],
"néerlandai": [6],
"mettr": [11,4],
"positif": [11],
"effac": [6],
"l\'insérer": [9,1],
"pt_br.dic": [4],
"lanceur": [5],
"certain": [11,6,10,9,[0,1,4,8]],
"dirigé": [11],
"légère": [10],
"bout": [11],
"unabridg": [0],
"copié": [9,11,6,8],
"en-us": [11],
"n\'écrivez": [11],
"section": [[5,6]],
"d\'info": [9],
"encodage": [7],
"refair": [9],
"authentifié": [11],
"ainsi": [11,5,8,6,[4,9,10]],
"optionsglossaryexactmatchcheckboxmenuitem": [3],
"msgstr": [11],
"orphelin": [11,9],
"qu\'au": [5],
"contextuel": [9,11,1],
"faisant": [[5,6,9,11]],
"dispon": [[5,11],8,[3,6],4,9],
"l\'anglai": [11,6],
"dispos": [11,9],
"s\'appliqu": [8,[5,11],[9,10]],
"important": [6],
"l\'exécutez": [5],
"nnnn": [9,5],
"phrase": [11,[2,3,6,8]],
"omegat.project": [6,5,10,[7,9,11]],
"effectu": [11,[5,8]],
"retourn": [[8,11],5],
"targetcountrycod": [11],
"équip": [6,[8,11],3,[5,10]],
"placez-l": [1],
"option": [11,[5,8],9,3,4,6,[2,10]],
"inséré": [11,8,10],
"d\'origin": [9],
"webstart": [5],
"lancera": [5],
"insert": [8],
"continu": [11],
"gamm": [[4,11]],
"rester": [11],
"dictionnaire": [7],
"omegat.projet": [6],
"br.aff": [4],
"project_save.tmx.temporair": [6],
"inutil": [[6,11]],
"zh_cn.tmx": [6],
"d\'exclus": [11],
"comparaison": [11],
"d\'effectu": [[5,8,11]],
"sai": [5],
"messag": [5,6,9],
"scripts": [7],
"l\'erreur": [5],
"san": [[5,11],9,0],
"rest": [11,[5,9]],
"pourront": [[2,4,5,11]],
"d\'exploit": [5,11,8,10],
"paquet": [5,[8,11]],
"glossaires": [7],
"américain": [11],
"consol": [5],
"l\'express": [11,2],
"partag": [6],
"vice-versa": [[6,11]],
"traditionnell": [5],
"yandex": [5],
"archiv": [5],
"décrit": [[5,6,9,11]],
"user": [5],
"a123456789b123456789c123456789d12345678": [5],
"atteindr": [[3,9,11],8],
"viewmarkwhitespacecheckboxmenuitem": [3],
"proxi": [5,11,3],
"extens": [11,1,0],
"aucun": [11,5,8,1,[6,9],[4,10]],
"ouvert": [11,6,9,8,[1,5]],
"constitue": [7],
"frapp": [11],
"fin": [11,2,10],
"complet": [[8,11],[5,6,9]],
"avoir": [11,6,9,[1,5],[0,2,4,8]],
"bak": [6,10],
"fond": [[8,9]],
"bat": [5],
"bas": [11,9,[3,8]],
"seront": [11,8,6,5,[1,9],10,4],
"complex": [2],
"d\'index": [11],
"jre": [5],
"d\'empêcher": [11],
"rendr": [11,8,[6,10]],
"optionsfontselectionmenuitem": [3],
"posit": [11,8,6],
"prochain": [8,3],
"pourraient": [5],
"lexicaux": [4],
"résultat": [11,8,[2,6]],
"l\'union": [8],
"possèd": [[1,11],4],
"diff": [11],
"fonctionnalité": [11,8],
"an": [2],
"editmultiplealtern": [3],
"complèt": [[3,5]],
"d\'ouvertur": [5],
"familièr": [11],
"git.code.sf.net": [5],
"au": [11,6,8,5,9,3,10,4,[1,2],0],
"moyen": [11,6],
"structurell": [11],
"déposé": [9],
"l\'insert": [11],
"perdu": [6],
"d\'entré": [11],
"parcourus": [8],
"be": [11],
"productivité": [11],
"freebsd": [2],
"affect": [[5,8]],
"relativ": [11],
"filters.xml": [6,[10,11]],
"proven": [9,1,[5,6,11]],
"permettr": [[6,8,11]],
"pensez": [6],
"br": [11,5],
"projectaccessglossarymenuitem": [3],
"l\'url": [6,[5,8,11]],
"l\'affichag": [6,11],
"pratiqué": [11],
"relatif": [[6,9]],
"segmentation.conf": [6,[5,10,11]],
"l\'écran": [5],
"préexist": [11],
"évité": [11],
"d\'argument": [5],
"sen": [11],
"l\'emplac": [9,[1,5,6,8,11]],
"ca": [5],
"contigus": [11],
"ses": [[0,5,9,11]],
"minuscul": [[3,8]],
"developerwork": [5],
"cd": [5,6],
"ce": [11,5,6,10,8,9,[0,4],[1,2,3,7]],
"öäüqwß": [11],
"set": [5],
"balis": [11,6,8,3,5,9],
"définiss": [11],
"restera": [11],
"portent": [11],
"cn": [5],
"adjoindr": [9],
"optionsrestoreguimenuitem": [3],
"figur": [4,[0,2]],
"thèmes": [11],
"cx": [2],
"voyell": [2],
"sélectionn": [11,9,8,5,4,10],
"aller-retour": [6],
"correspondr": [11,4],
"l\'ensembl": [11,[5,8,10]],
"apach": [[4,6,11]],
"adjustedscor": [11],
"font": [[4,10]],
"de": [11,6,5,8,9,10,[2,3],4,1,7,0],
"justif": [6],
"echap": [11],
"terminolog": [9],
"vieux": [11],
"offic": [11],
"ajustez": [11],
"d\'i": [[5,9,11]],
"extern": [11,8,[3,6]],
"f1": [3],
"do": [5],
"f2": [9,[5,11]],
"d\'autr": [6,5,9,[0,1,4,8]],
"f3": [[3,8]],
"parti": [11,9,8,[1,6],[4,10]],
"dr": [11],
"f5": [3],
"du": [11,6,5,8,3,9,10,1,4,7,2,0],
"tenir": [11],
"contrôler": [[5,11]],
"repositories": [7],
"dz": [0],
"duquel": [5],
"rattaché": [8],
"projectsavemenuitem": [3],
"editundomenuitem": [3],
"tiret": [5],
"enregistrez": [6,3],
"xmx6g": [5],
"procédur": [6,[4,11]],
"virtuell": [11],
"u000a": [2],
"remarqu": [[6,11]],
"icôn": [[5,8]],
"opérant": [5],
"spécificateurs": [7],
"en": [11,6,5,9,8,10,3,4,1,[0,2],7],
"actif": [[8,11]],
"u000d": [2],
"réticent": [2],
"et": [11,6,5,8,9,4,10,2,1,3,7,0],
"u000c": [2],
"ex": [5,11,[4,6,9]],
"réutilisé": [6],
"défaut": [11,3,8,6,1,[5,9],10,[2,7]],
"activ": [11,8,[3,10]],
"compat": [5],
"isolé": [11],
"u001b": [2],
"foi": [11,6,[2,5],[3,8,9]],
"stats.txt": [10],
"indic": [6],
"l\'intervall": [11,[6,8]],
"terminologi": [8,[1,6,11]],
"tient": [11],
"origin": [6,11],
"foo": [11],
"sélectionnez": [11,8,[4,5],6,[1,9,10]],
"exclud": [6],
"for": [11,8],
"fr": [5,4],
"pendant": [6,[5,10]],
"s\'effectu": [[8,10,11]],
"contenu": [11,[3,6],10,5,8,[0,9]],
"content": [5,11,3],
"effectuez": [10,[2,5,9]],
"vrir": [[3,8]],
"écritur": [[3,8]],
"alert": [5],
"applescript": [5],
"client": [6,10,[5,9,11]],
"exclus": [6,11],
"propriétés": [7],
"class": [11,2],
"d\'activ": [[8,11]],
"helplogmenuitem": [3],
"utilisation": [7],
"slovèn": [9],
"non-traduit": [11],
"editoverwritetranslationmenuitem": [3],
"outputfilenam": [5],
"go": [5],
"non-nécessair": [11],
"laiss": [11],
"aeiou": [2],
"intéress": [10,[4,11]],
"n\'appréciez": [11],
"form": [11,6,[5,8,10],3],
"courants": [7],
"defaut": [1],
"attaché": [11],
"d\'alert": [2],
"orthographié": [4],
"traité": [11,5,6],
"optionnel": [8,1],
"localisé": [6,9],
"rassemblé": [11],
"fourni": [11,[5,8]],
"hh": [6],
"duser.languag": [5],
"sauvegard": [6,[5,10]],
"vert": [9,8],
"éviter": [6,11],
"spécifiqu": [11,10,[6,8],5,[2,9]],
"bis": [2],
"d\'expressions": [7],
"file-target-encod": [11],
"projectopenmenuitem": [3],
"autom": [5],
"dû": [5],
"context": [[9,11],[3,6,8]],
"création": [[6,11]],
"issus": [11],
"https": [6,5,[9,11]],
"id": [11],
"if": [11],
"project_stats.txt": [11],
"vérifieront": [5],
"ocr": [6],
"entré": [11,8,3,1,[5,6,9]],
"sélectif": [6],
"projectaccesscurrenttargetdocumentmenuitem": [3],
"toolsvalidatetagsmenuitem": [3],
"il": [11,6,5,10,4,9,1,8,[2,3],7],
"in": [11],
"termin": [5,[8,9,11]],
"ip": [5],
"consonn": [2],
"is": [2],
"d\'avoir": [11,5,6],
"attribu": [5],
"matériel": [6],
"odf": [6,11],
"complètement": [6],
"modèl": [[2,11]],
"odg": [6],
"exécution": [7],
"ja": [5],
"accèd": [11],
"je": [5,11],
"selon": [[5,11]],
"odt": [6,11],
"gotonexttranslatedmenuitem": [3],
"viewmarktranslatedsegmentscheckboxmenuitem": [3],
"jj": [6],
"paragraph": [11,8,6],
"valu": [1],
"eux-mêm": [[6,11]],
"nplural": [11],
"s\'attendr": [5],
"js": [11],
"ilia": [5],
"n\'étai": [11],
"déjà": [11,[5,6],[8,9],[4,10],[1,3]],
"learned_words.txt": [10],
"clavier": [3,11,[8,9]],
"sélection": [8,11,3,[0,9]],
"redéfinir": [11],
"reprendr": [11],
"affich": [8,11,[3,9],5,6,[1,2,4,10]],
"table": [7],
"macos": [7],
"ftl": [5],
"mémoir": [6,11,10,5,9,8,2],
"ftp": [11],
"editselectfuzzy1menuitem": [3],
"portant": [[6,10]],
"agréger": [11],
"actuell": [[5,8,9,11]],
"viewdisplaymodificationinfoallradiobuttonmenuitem": [3],
"draw": [6],
"placez": [10,[4,6]],
"cherch": [11],
"conservé": [5,[10,11]],
"compressé": [10],
"hide": [11],
"opérateur": [2],
"la": [11,6,5,8,9,3,10,2,4,1,0,7],
"s\'agit": [[6,11],[0,9,10]],
"placer": [[3,6,8,10,11]],
"sensibl": [11],
"automatique": [7],
"le": [11,6,5,8,9,10,1,2,[3,4],0,7],
"dswing.aatext": [5],
"dictionnaires": [7],
"auto": [10,8,11,6,3],
"fur": [6],
"habituell": [[8,11]],
"consécutif": [11],
"l\'endroit": [8,9],
"ordr": [11],
"sign": [[1,9]],
"problèmes": [7],
"lu": [2],
"document.xx.docx": [11],
"majorité": [11],
"redémarr": [11],
"intégrer": [8],
"mot-clé": [11],
"l\'interfac": [5,6,[1,11]],
"second": [11,[3,5,6,8,9]],
"suffit": [[4,5,10],[9,11]],
"survient": [5],
"cycleswitchcasemenuitem": [3],
"son": [11,5,8,[1,6],9,0],
"l\'export": [6],
"précis": [[3,11]],
"oracl": [5,3,11],
"suffir": [11],
"spécificateur": [2],
"ailleur": [5],
"régulières": [7],
"limit": [11,[2,5]],
"survienn": [11],
"omegat.png": [5],
"d\'erreur": [5,6],
"autr": [6,5,11,[2,8,9],[1,4,10]],
"réservé": [4],
"gradlew": [5],
"mm": [6],
"interactif": [2],
"mn": [6],
"entri": [[8,11]],
"mo": [5],
"level": [6],
"mr": [11],
"ms": [11],
"mt": [10,11],
"lemmatiseur": [11],
"partant": [11],
"essai": [11],
"graphiques": [7],
"modif": [11,6,[3,5,10],8,1],
"my": [5],
"plus": [11,5,9,[2,6],10,[4,8],1,[0,3,7]],
"démarrage": [7],
"conseillé": [11],
"ne": [11,6,5,[1,8],4,[2,3,9],10],
"permettra": [11,8],
"essay": [11],
"rencontré": [0,[6,10]],
"veut": [6],
"ni": [9,5],
"paramétrag": [5],
"nl": [6],
"systèmes": [7],
"recherchez": [[0,5,6]],
"bon": [[0,4,11]],
"no": [11],
"supérieur": [[9,11]],
"déclarat": [11],
"code": [3,4,11,5,6],
"n\'apparaîtra": [8],
"reconnaîtr": [[6,11]],
"définit": [3,[8,11]],
"évite": [11],
"gotohistoryforwardmenuitem": [3],
"importez": [6],
"pouvant": [11],
"prennent": [[6,11]],
"d\'accueil": [6],
"contrôl": [6,8,[2,3]],
"switch": [11],
"total": [11,9,6],
"utiliser": [7],
"décidé": [11],
"of": [[0,11]],
"définir": [11,[2,4,8,9]],
"décompressez": [5],
"possibl": [11,5,[6,9],2,[8,10],[1,3,4]],
"apparaîtr": [[4,5]],
"présentent": [10],
"ok": [[5,8]],
"on": [[6,8,11],[3,5]],
"dessous": [[5,11]],
"macro": [11],
"valeur": [11,2,5,1],
"clair": [8],
"src": [6],
"ou": [11,6,5,8,2,9,3,1,[0,4],10],
"déplacera": [11],
"incluront": [6],
"control": [3],
"insécabl": [[8,11],3],
"double-cliquez": [5],
"utilisez": [6,[5,11],[1,8],[0,9]],
"démarrera": [5],
"no-team": [[5,6]],
"chargé": [[5,6,11]],
"stylistiqu": [11],
"alphabétiqu": [11],
"editinserttranslationmenuitem": [3],
"pc": [5],
"là": [11],
"connus": [6],
"empêcher": [11],
"pi": [11],
"l\'avez": [5],
"restent": [10,5],
"détacher": [9],
"correspondant": [11,[1,5]],
"encor": [[5,11],8],
"po": [11,9,5],
"déterminé": [11],
"correspondra": [[2,11]],
"inclur": [11,6],
"optionsglossarystemmingcheckboxmenuitem": [3],
"inclut": [[2,5,6,9]],
"pt": [[4,5]],
"inclus": [[6,11],2],
"finissez": [6],
"récupérer": [11],
"optionsautocompleteglossarymenuitem": [3],
"calculé": [11,9],
"faibl": [11],
"décriron": [6],
"zéro": [[2,11]],
"formatag": [11,6,10],
"d\'occurr": [11],
"montrant": [11],
"législat": [6],
"qu": [[6,11],5],
"filtrer": [11],
"edit": [11,8,3,9],
"d\'ajout": [11],
"ancienn": [[5,6]],
"resteront": [[10,11]],
"vérificateur": [4,11,7,10,[1,2]],
"editselectfuzzy5menuitem": [3],
"brésilien": [4],
"l\'appell": [5],
"bilingu": [[6,11]],
"receviez": [5],
"kde": [5],
"d\'agrandiss": [9],
"téléchargement": [5],
"rc": [5],
"redimensionn": [[9,11]],
"includ": [6],
"principale": [7],
"fenêtres": [7],
"l\'adress": [5],
"minut": [6,[8,11]],
"nouvel": [[5,11]],
"access": [3,[0,5,6,8,11]],
"trouverez": [5,6],
"languag": [[5,11]],
"décide": [11],
"distingu": [10],
"sa": [11,9,6,[5,8]],
"disposit": [9],
"sc": [2],
"sur": [11,5,[6,9],8,3,4,1,10],
"se": [8,11,9,6,[5,10],[3,4]],
"bleu": [[9,11]],
"si": [11,8,5,6,[4,10],9,[2,3],[0,1]],
"oublié": [0],
"quelqu": [[5,6,11]],
"key": [[5,11]],
"inscrir": [5],
"l\'élément": [3],
"obtiendrez": [[6,11]],
"intern": [[9,11],8],
"mots-clé": [11],
"svg": [5],
"celle-ci": [11,6,9],
"où": [8,11,[5,6],9],
"svn": [6,10],
"quitt": [10],
"divisé": [11],
"quell": [[4,11],[2,5,9,10]],
"suivent": [2],
"editoverwritesourcemenuitem": [3],
"inscrit": [[5,11]],
"dialogues": [7],
"données": [7],
"confirm": [11,[8,10]],
"l\'identif": [11],
"l\'objet": [11],
"sous-dossi": [10,6,11,5,[0,1,4]],
"ont": [11,8,6,[1,9,10]],
"enforc": [10],
"s\'avérer": [[6,10,11]],
"d\'être": [6,5,[9,11]],
"remov": [5],
"associé": [[5,8]],
"tm": [10,6,8,[7,9,11]],
"vast": [[4,11]],
"to": [[5,11]],
"v2": [5],
"sujet": [6,[10,11]],
"soumi": [0],
"editreplaceinprojectmenuitem": [3],
"but": [5],
"symbol": [2],
"document.xx": [11],
"tw": [5],
"aide": [7],
"dialogu": [11,8,10,[1,4,6,9]],
"validé": [8,[9,11]],
"express": [2,11,5,[3,4]],
"trouv": [8,[10,11],[2,3,5,6,9]],
"rédaction": [6],
"sûr": [[6,9,10],11],
"corrig": [8],
"viewmarkautopopulatedcheckboxmenuitem": [3],
"projectwikiimportmenuitem": [3],
"countri": [5],
"plein": [11],
"jour": [11,6,[1,5,8,10]],
"communauté": [6],
"distribué": [5,11],
"êtes": [[5,6],[9,10,11]],
"utilisateur": [5,11,6,[1,2,8,10]],
"tableaux": [[3,6,7]],
"variant": [11],
"un": [11,6,5,8,[1,2,9],4,10,3,0,7],
"l\'en-têt": [11],
"fichier_de_configur": [5],
"gotoprevioussegmentmenuitem": [3],
"désirez": [[5,6]],
"toujour": [11,[1,6],[3,8]],
"assurez-v": [5,4,6],
"sélectionnez-l": [5],
"l\'encodag": [11,1],
"devront": [[6,11]],
"dépôt": [6,8,[5,11]],
"solut": [[6,11]],
"d\'align": [11,8],
"this": [2],
"gotopreviousnotemenuitem": [3],
"va": [11,[4,5,6,8,10]],
"editredomenuitem": [3],
"uilayout.xml": [10],
"substitut": [8],
"extérieur": [[1,8]],
"vi": [5],
"fermer": [11,[3,8]],
"soient": [10,[4,6,11]],
"à-d": [11,8],
"désactiv": [[8,11]],
"renommé": [6],
"détecté": [6],
"amélioré": [11],
"fermez": [[6,8]],
"support": [6],
"vs": [9],
"jamai": [11],
"forcé": [10],
"libr": [[0,8,11]],
"coin": [9],
"changez": [11],
"deviez": [4],
"sein": [6],
"disposez": [[4,5,6,11]],
"copiez-l": [6],
"lisez-moi": [[5,11]],
"encouragé": [6],
"changer": [11,6],
"deux": [11,[5,6],8,4,9,10],
"appelon": [11],
"lien": [[0,5,11]],
"apport": [11],
"licenc": [[0,5,6,8]],
"groovy.codehaus.org": [11],
"auront": [11],
"lieu": [8],
"exécuté": [5,8],
"normal": [5,11,[1,10]],
"n\'arrivera": [6],
"emac": [5],
"org": [6],
"reconnus": [1,11],
"distribut": [5],
"évalué": [11],
"liée": [11],
"l\'icôn": [5],
"différem": [11],
"détermin": [4],
"xf": [5],
"quitté": [8],
"démarr": [5],
"remplacement": [7],
"lentement": [5],
"additionnell": [11],
"littéral": [11],
"remarquez": [6,5,[4,10,11],0],
"choix": [5,11,4],
"xx": [5,11],
"restaur": [8,[6,9,11]],
"xy": [2],
"runtim": [5],
"sourc": [11,6,8,9,[3,10],5,1],
"d\'absenc": [8],
"démarré": [[2,5]],
"tester": [2],
"extrait": [11],
"extrair": [0],
"ressourc": [[6,11],5],
"type": [11,6,[3,5,8,10]],
"aligner": [7],
"techniqu": [11,8],
"beaucoup": [2],
"toolssinglevalidatetagsmenuitem": [3],
"secondair": [10,9],
"décochant": [11],
"obligera": [11],
"lign": [5,11,[2,9],3,[6,8,10],4,1],
"filenam": [11],
"souri": [[8,9]],
"génération": [11],
"éventuell": [6],
"technologi": [5],
"créera": [11,5],
"l\'ajout": [5,11,[1,4,6,9]],
"commentair": [11,9,[1,5],[3,8]],
"projectaccesssourcemenuitem": [3],
"guide": [7],
"l\'except": [2,6],
"yy": [9,11],
"proposit": [11],
"comm": [11,6,5,9,8,[0,10],[3,4]],
"avez-v": [5],
"gotosegmentmenuitem": [3],
"raison": [[4,8,10]],
"sombr": [11],
"dans": [7],
"push": [6],
"zh": [6],
"exist": [11,[1,5],6,[4,8]],
"propr": [11,9,2],
"readme_tr.txt": [6],
"d\'oscil": [6],
"intact": [10],
"propo": [[3,8]],
"préciser": [5,4],
"penalti": [10],
"représent": [11,5],
"exact": [11,[1,8],4],
"xx_yy.tmx": [6],
"précisez": [5],
"l\'aller-retour": [6],
"prédéfini": [11,[2,5]],
"confirmé": [[2,3,11]],
"oui": [5],
"soulign": [1],
"retour": [2,[9,11]],
"utf8": [1,[8,11]],
"helpaboutmenuitem": [3],
"espagnol": [4],
"copi": [6,[8,11],10],
"valabl": [11],
"coréen": [11],
"depui": [[5,9,10,11],[4,6,8]],
"intérêt": [5],
"l\'invers": [11],
"implément": [5],
"pourcentag": [9,11,10],
"place": [11,[3,5,6]],
"power": [11],
"quand": [6,11,5],
"regular": [11],
"longu": [11],
"c\'est": [11,8,[2,5,9]],
"tag-valid": [5],
"inversé": [11],
"événement": [3],
"chemin": [5,6],
"suggest": [[8,11],[3,4,9,10]],
"pérennité": [10],
"site": [11,10],
"demandera": [11],
"exportez-l": [6],
"réellement": [8],
"u0009": [2],
"xhh": [2],
"entendu": [[5,11]],
"s\'écrivant": [6,7],
"qu\'import": [6],
"revis": [0],
"u0007": [2],
"utilitair": [5],
"repositori": [6,10],
"minimum": [11],
"date": [11,8],
"argument": [5],
"lowercasemenuitem": [3],
"wiki": [[0,9]],
"avez": [5,11,[4,6,9],8,[0,3,10]],
"firefox": [[4,11]],
"feuill": [11],
"separ": [1],
"tab": [1,3,[8,11],9],
"quatr": [[6,8]],
"taa": [11,8],
"mesur": [11,5,[4,6]],
"intéragit": [6],
"tag": [11],
"l\'align": [11,5],
"replac": [9],
"écrasé": [[5,11]],
"édité": [3],
"faudra": [11],
"tao": [10],
"doivent": [11,6,5,3,[0,1,2,4]],
"tar": [5],
"parmi": [[8,11]],
"défectueux": [6],
"coller": [8,9],
"s\'appliqueront": [11],
"clic-droit": [[1,4,8,11]],
"prendront": [11],
"intermédiair": [6],
"projectreloadmenuitem": [3],
"serveur": [6,5,[10,11]],
"n\'ajout": [6],
"suggéré": [9],
"bloc": [2,11],
"choisit": [5],
"choisir": [3,8,[5,11],6],
"obliqu": [5],
"tiendra": [5],
"contient-il": [0],
"installé": [5,[6,8],[4,11]],
"safe": [11],
"openoffic": [4,11],
"navig": [[5,11]],
"concevoir": [2],
"populair": [11],
"verra": [6],
"réagira": [9],
"filtrag": [11],
"avec": [5,11,6,9,8,[1,10],[0,3]],
"note": [11,9,8,6,2,[3,10]],
"redémarrez": [3],
"optionsautocompletechartablemenuitem": [3],
"couleur": [[8,11],3],
"reproduir": [6],
"l\'ordr": [11,9,8],
"l\'identiqu": [11],
"winrar": [0],
"tbx": [1,11,3],
"ressemblera": [5],
"notr": [6],
"excepté": [11],
"dynamiqu": [11],
"langues": [7],
"remont": [11],
"git": [6,[5,10]],
"cas": [6,11,5,10,[8,9],2],
"l\'une": [9,11],
"car": [11,[5,9]],
"placé": [8,[6,11]],
"ouvrira": [[8,11]],
"duser.countri": [5],
"tcl": [11],
"tck": [11],
"plage": [2],
"xx-yy": [11],
"non-avid": [2],
"readm": [5,11],
"défini": [[6,11],[5,10],[3,4,8,9]],
"disparaîtront": [4],
"virgul": [[2,11],1],
"match": [8],
"considéré": [11,9],
"intens": [8],
"categori": [2],
"intent": [11],
"présenter": [11],
"problèm": [1,[6,8],0,5],
"optionsspellcheckmenuitem": [3],
"passent": [11],
"parfait": [[6,8,11]],
"tableau": [2,3,11,9,1],
"l\'autr": [[8,11],[6,9]],
"accompli": [6],
"align.tmx": [5],
"confidentialité": [[5,11]],
"commentaires": [7],
"optionssetupfilefiltersmenuitem": [3],
"contrair": [6],
"auprè": [11]
};
