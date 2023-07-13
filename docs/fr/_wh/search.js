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
 "Annexes",
 "Préférences",
 "Guides pratiques",
 "Introduction à OmegaT",
 "Menus",
 "Volets",
 "Racine du projet",
 "Fenêtres et dialogues",
 "OmegaT 6.1.0 - Manuel d’utilisation"
];
wh.search_wordMap= {
"au-dessus": [0,7,1,3],
"adopté": [0],
"césure": [1],
"répertori": [[1,5]],
"dépendent": [[0,2,4,7]],
"comprendr": [[0,5]],
"avanc": [[0,2,4]],
"directionnalité": [0],
"être": [0,2,7,1,4,3,5,6],
"entreposé": [2],
"changent": [0],
"accompagné": [2],
"tel": [0,7,2,1,[3,6]],
"avant": [2,0,1,7,[3,4,6],5],
"info.plist": [2],
"produit": [2,[1,5]],
"produis": [[2,7]],
"informatiqu": [[2,3]],
"produir": [[0,2]],
"réviseur": [6],
"pouvoir": [2,[0,7]],
"left": [0],
"quitter": [4,[1,2],0],
"quittez": [4,[1,3,6]],
"apparait": [[5,7],1],
"commencé": [[2,3,7]],
"étoil": [0],
"ces": [2,0,6,7,3,1,5],
"edittagnextmissedmenuitem": [0],
"cet": [4,7,0,2],
"quiet": [2],
"catégories": [8],
"recherch": [7,0,1,4,3,5,[2,6]],
"tutoriel": [0],
"essayez": [7,[0,1]],
"présentat": [5,3,0,[1,2]],
"the": [[2,7]],
"projectimportmenuitem": [0],
"régulier": [2],
"imag": [0],
"monolingu": [[0,7]],
"exclusiv": [2],
"répertorié": [[0,2,3]],
"omegat.project.lock": [2],
"ajouté": [[2,7],[0,1],[4,6]],
"moodlephp": [2],
"currsegment.getsrctext": [7],
"priorité": [[1,4],2,3],
"export": [0,4,[1,2,7]],
"mieux": [3,5],
"practic": [7],
"inopiné": [2],
"également": [7,2,0,1,3,6,5,4],
"projets": [8],
"avérer": [5],
"gotonotespanelmenuitem": [0],
"fr-fr": [[1,3]],
"incluent": [0,2],
"minim": [6,1],
"problématiqu": [3],
"proposé": [3],
"générer": [6,[3,4]],
"conçu": [3,[0,2,7]],
"offr": [3,[0,2,5]],
"démontrant": [0],
"croissant": [0],
"cjc": [7,0],
"pilot": [1],
"insérer": [0,4,5,1,3,7,6,2],
"désignerez": [2],
"insérez": [0],
"mêmes": [2,0,7],
"syntax": [0,2,7],
"avon": [0],
"empti": [2],
"apporté": [[0,2,4,5],1],
"blocs": [8],
"appuy": [7,5,1],
"variabl": [1,0,7],
"procédé": [[0,2]],
"propos": [0,[2,7],5],
"tmx": [2,7,6,1,[3,5]],
"capabl": [0],
"application_startup": [7],
"actuel": [4,7,[0,2],[5,6],1,3],
"détecter": [2],
"eventtyp": [7],
"traductions": [8],
"vérific": [[1,3],7,[2,4],6],
"distant": [2,6,4,[5,7]],
"mécanism": [[0,2,4]],
"fenêtr": [7,4,5,1,0,3,[2,6]],
"aligné": [[0,2,4,7]],
"fr-ca": [1],
"mainmenushortcuts.properti": [0],
"précédemment": [3,[0,2,6,7]],
"souci": [2],
"convertit": [[0,1,3]],
"savoir": [[2,4],[0,7],3,1,6,5],
"convertir": [2],
"glisser-déplac": [5],
"assistée": [8],
"gotohistorybackmenuitem": [0],
"save": [7],
"v1.0": [2],
"renommez-l": [[2,3]],
"top": [5],
"saut": [0,7],
"powerpc": [2],
"agissait": [2],
"question": [0,[1,4,7]],
"équival": [7,2,[0,1,5]],
"haut": [[0,1,7],4,[2,3,5]],
"premier": [0,7,4,[1,2,3,5]],
"présenteront": [5],
"sauf": [[0,2],[1,3]],
"incertain": [7],
"editselectsourcemenuitem": [0],
"éditez": [5],
"tandi": [5,[0,2,4]],
"supposon": [0],
"éditer": [7,3],
"évitons-n": [0],
"détail": [1,[5,7]],
"com": [0],
"instal": [2,1,[0,3],6],
"fichier2": [2],
"désactivez": [[0,7],1],
"proch": [1,2],
"colorera": [0],
"pipe": [0],
"cliquer": [7,4,5,[2,3]],
"clé": [2,7,1],
"au-delà": [[1,2,5]],
"énuméré": [2],
"précaution": [[2,7]],
"cliquez": [7,1,[0,3],5,[2,4]],
"tri": [4],
"changeid": [1],
"vérifié": [1],
"translat": [0,[1,2],7],
"voient": [6],
"université": [1],
"détach": [5],
"roug": [[1,6],[0,7]],
"immuabl": [6],
"écarté": [7],
"presqu": [[2,4]],
"environn": [2,0],
"fichiers": [8],
"respons": [4],
"docs_devel": [2],
"devrait": [2,6,[0,3,4]],
"tsv": [0],
"distanc": [[2,6]],
"automatiqu": [1,7,4,0,2,5,[3,6]],
"élément": [0,[4,7],3,5,[1,2],6],
"flux": [5],
"gnome": [1],
"leur": [0,7,2,3,1,[4,6]],
"maximal": [[0,2]],
"horizont": [0],
"analys": [[0,5,7]],
"celui": [[0,7],2,1,4],
"quel": [7,0,4,[3,5],[2,8]],
"appui": [[0,7]],
"appdata": [0],
"nombreux": [0,2],
"csv": [0,2],
"nombreus": [0],
"séparément": [1],
"apparit": [7,2],
"concern": [[0,2,3,6]],
"demandez": [2],
"paramétré": [2],
"caractèr": [0,7,4,1,5,[2,3]],
"téléchargera": [3],
"les": [0,7,2,1,4,3,5,6,8],
"press": [0],
"dock": [2],
"caret": [0],
"night": [2],
"listé": [[1,4]],
"équivalent": [1,[5,7]],
"fourniss": [1],
"forment": [0],
"paniqu": [3],
"filenameon": [1,0],
"ctrl": [0,4],
"editorinsertlinebreak": [0],
"jumptoentryineditor": [0],
"document": [0,2,7,3,4,[1,5,6],8],
"rapport": [2,[1,3],[0,7]],
"mainten": [2,7,0],
"moment": [3,[6,7],[1,2]],
"destiné": [6,0],
"page_up": [0],
"formatage": [8],
"glossaryroot": [0],
"supplémentair": [0,[2,7],1,5,[3,4]],
"resourc": [2],
"survol": [[1,5]],
"moodl": [0],
"team": [2],
"xx_yy": [0],
"docx": [[2,7],[0,4]],
"project_stats_match_per_file.txt": [[4,6]],
"txt": [2,0,5],
"disqu": [[2,7]],
"libertés": [8],
"projectmedopenmenuitem": [0],
"lib": [5,0],
"anni": [0],
"puissiez": [6],
"prendre": [8],
"source": [8],
"fréquent": [0,2],
"intimidant": [0],
"prendra": [0],
"totaux": [4],
"dégustant": [3],
"viewdisplaymodificationinfoselectedradiobuttonmenuitem": [0],
"lit": [7],
"index.html": [0,2],
"pris": [2,7,1,3,6,[0,4]],
"ressembl": [3],
"doubl": [0],
"personnalis": [0,[1,4],7],
"diffrevers": [1],
"téléchargent": [2],
"appliqu": [7,1,2,0,[5,6]],
"page": [0,[1,7],[3,4],2],
"initiaux": [0],
"concept": [3],
"quoi": [3,0],
"éditeur": [7,0,5,3,1,4,6,8,2],
"écouler": [0],
"project.gettranslationinfo": [7],
"doit": [0,2,1,[5,6,7]],
"attendu": [[0,8]],
"preuv": [2],
"brève": [[3,7]],
"doctorat": [1],
"lesquel": [7,[1,5]],
"mymemori": [1],
"incohér": [7],
"niveau": [7,0,3],
"pair": [2,[0,7],1],
"regex101": [0],
"equal": [2],
"chacun": [2,[0,1,5,7]],
"watson": [1],
"anglais": [2],
"impliqué": [2],
"fréquenc": [2],
"bouton": [7,3,0,1,[4,5],6],
"anné": [[0,2]],
"réalign": [7],
"mettez": [2],
"compris": [0],
"ondulé": [1],
"redimensionné": [5],
"lié": [3,[0,2,7]],
"ajout": [0,2,[3,7],6,5,[1,4]],
"côte": [2],
"viewmarkglossarymatchescheckboxmenuitem": [0],
"traduisez": [[1,3,5],2],
"enter": [0],
"bien": [2,0,7],
"applic": [2,0,[4,7],[1,3,6],5],
"imaginez": [3],
"bidi": [0,4],
"projectteamnewmenuitem": [0],
"gotoprevxenforcedmenuitem": [0],
"majeur": [2],
"évolut": [0],
"déplacez": [[3,5]],
"déplacer": [5,7,[0,3,4]],
"autocompletertablelast": [0],
"memori": [2],
"actualisé": [3],
"séjour": [0],
"revenir": [[3,5],7,[4,6]],
"ouvrez": [2,6,[0,3,4,7]],
"log": [0],
"lor": [7,2,1,[0,3],5,[4,6]],
"principes": [8],
"ramèn": [0],
"openjdk": [1],
"永住権": [[1,7]],
"interagir": [7],
"consult": [[0,2,7]],
"toolscheckissuesmenuitem": [0],
"dépend": [[0,1,4],2],
"aider": [8,[0,3]],
"affiché": [7,1,5,0,[4,6],2,3],
"présenc": [0],
"initial": [7,[1,6]],
"entrainez-v": [2],
"orphan": [5],
"clic": [5,7,4,2],
"avertiss": [7,[0,1,2],3,4,6,5],
"librairi": [0],
"présent": [0,7,2,[3,5],1],
"autocompletertablepageup": [0],
"entièr": [[0,2,6]],
"www.deepl.com": [1],
"config-fil": [2],
"apparié": [5],
"sélectionné": [4,7,5,[0,1],[2,3]],
"tell": [0,2,7,[3,4,5]],
"demandé": [[2,4],7,0],
"ajoutez": [0,[2,3,6],7],
"vérifier": [1,7,[0,3],[2,4,5]],
"dan": [7,0,2,4,1,5,3,6],
"correspondront": [0],
"commandes": [8],
"zone": [5],
"côté": [0,7],
"considér": [0],
"téléchargé": [2],
"orthographique": [8],
"lre": [0,4],
"renomm": [2],
"donn": [7,4,2],
"system-user-nam": [0],
"lrm": [0,4],
"décalag": [2],
"façon": [[0,2],1,[3,4,5]],
"absolu": [7,0],
"distribuer": [8],
"format": [2,0,3,1,7,[4,5,6],8],
"donc": [0,3,[2,7]],
"console.println": [7],
"produira": [0],
"rainbow": [2],
"vérifiez": [[2,3]],
"autocompleterlistdown": [0],
"utilisant": [8],
"dont": [[1,2,7],0,[3,4,5],6],
"très": [[3,7],4,[0,1,2]],
"projet_en-us_fr": [2],
"tent": [7,[1,2]],
"placerait": [6],
"endroit": [5,[0,7],[1,6]],
"accidentel": [1],
"kilooctet": [2],
"autonom": [1],
"part": [[2,7]],
"revenez": [[2,3,7]],
"principal": [7,5,1,[4,8]],
"ouvrir": [7,2,0,4,3,5,[1,6]],
"fra-epo": [1],
"activefilenam": [7],
"introduction": [8],
"temp": [[3,7],2,[1,5]],
"project_files_show_on_load": [0],
"souhaitez": [7,2,0,[4,6],1,8,[3,5]],
"apostroph": [0],
"build": [2],
"lue": [2],
"lui": [0,[6,7]],
"rassembl": [2],
"récit": [0],
"entries.s": [7],
"réparti": [[0,7]],
"gotonextuntranslatedmenuitem": [0],
"targetlocal": [0],
"path": [2,5],
"des": [0,7,2,1,4,3,5,6,8],
"flèche": [[3,7]],
"raccord": [2],
"nuag": [2],
"pass": [4,2,1,[0,7]],
"afin": [0,2,7,[4,6]],
"impact": [3],
"stade": [2],
"helpcontentsmenuitem": [0],
"trouvant": [[3,4]],
"resnam": [0],
"domain": [2],
"tâche": [2],
"omegat-org": [2],
"équipe": [8],
"mentionn": [4],
"descript": [4,[0,1,7],3],
"remote-project": [2],
"impératif": [7],
"initialcreationid": [1],
"ignore.txt": [6],
"projectaccessdictionarymenuitem": [0],
"dout": [6],
"pénalité": [6,1],
"journal": [0,4],
"crédit": [4],
"restauré": [2],
"term": [[5,7],[1,4],3,0,6,2],
"volets": [8],
"files_order.txt": [6],
"douz": [0],
"infobull": [1,5],
"projectrestartmenuitem": [0],
"bloc-note": [8],
"editorskipnexttoken": [0],
"trans-unit": [0],
"manquant": [[0,4],3,[2,5]],
"right": [0],
"possible": [8],
"caractéristiqu": [[0,1,7]],
"touches": [8],
"pourriez": [0],
"qigong": [0],
"accorde": [8],
"maximum": [0,7],
"paus": [3],
"délimit": [5,[4,7],[0,1]],
"multiples": [8],
"plusieur": [0,7,2,[3,5],[1,4,6]],
"accéder": [0,[4,7],3,1,[2,6]],
"appelé": [0,2,[1,6]],
"dir": [2,1],
"down": [0],
"dit": [0],
"dis": [0],
"combien": [0],
"dix": [[2,7],4],
"exemples": [8],
"séparateur": [1,0,5],
"viewfilelistmenuitem": [0],
"accédez": [3],
"signal": [3],
"encodag": [0,7],
"test": [2],
"varier": [2],
"omegat": [2,0,3,7,1,4,6,[5,8]],
"remplacez-la": [3],
"imprim": [7],
"présenté": [0,2,[3,5,7],1],
"allemand": [1,7],
"bleus": [7],
"deepl": [1],
"devra": [2],
"utilisiez": [2],
"fonction": [0,4,3,1,[5,7],2,6],
"final": [[0,3],[6,7]],
"celles-ci": [1],
"montr": [[4,7],0],
"reflèt": [[4,7]],
"console-align": [[2,7]],
"dissimul": [5,[3,4,7]],
"reçoiv": [1],
"projectopenrecentmenuitem": [0],
"fr_fr": [3],
"thèse": [1],
"load": [7],
"mise": [2,1,7,0,5,[3,4]],
"dépassant": [3],
"relay": [2],
"issue_provider_sample.groovi": [7],
"grand": [2,[3,7]],
"une": [0,2,7,1,4,3,5,6,8],
"insignifiant": [0],
"glyph": [4],
"austèr": [4],
"partir": [2,7,0,4,[1,3],6,5],
"editoverwritemachinetranslationmenuitem": [0],
"relat": [1,[5,7]],
"console-stat": [2],
"ingreek": [0],
"appell": [0],
"bureaux": [2],
"verrez": [6],
"cocher": [7],
"disposé": [4],
"f12": [7],
"verticaux": [0],
"commencerez": [0],
"convers": [2,[1,7]],
"ignor": [0,[6,7],[1,4,5]],
"décoré": [3],
"projectexitmenuitem": [0],
"contenus": [7,0,6],
"signet": [0],
"adoptium": [2],
"text": [0,7,1,[4,5],2,3,6],
"latin": [0],
"redémarré": [0],
"editregisteruntranslatedmenuitem": [0],
"init": [2],
"crées": [0],
"fermé": [5,2],
"utilisées": [8],
"créez": [2,3,[0,4],6],
"créer": [2,7,0,3,4,6,1,[5,8]],
"manag": [2],
"manifest.mf": [2],
"utilisez-la": [2],
"adapté": [[0,2]],
"maco": [0,2,4,5,3,1],
"réalisé": [2],
"éléments": [8],
"téléchargeabl": [[1,2,3]],
"équité": [0],
"doc": [7,0],
"output-fil": [2],
"statut": [[0,3]],
"perdez": [5],
"nomdeclass": [2],
"gardant": [3],
"paramet": [2],
"dos": [0],
"tenter": [1],
"réouvertur": [2],
"mai": [0,2,7,3,[1,6],4,5],
"maj": [4,7,1,0,3,[2,5,6]],
"tentez": [2],
"mal": [[4,7]],
"médecin": [0],
"libre": [8],
"map": [2,6],
"combiné": [0],
"arboresc": [6],
"url": [2,1,[3,6],[0,4,7]],
"uppercasemenuitem": [0],
"excluant": [2],
"viewmarkuntranslatedsegmentscheckboxmenuitem": [0],
"ultérieur": [[0,6,7],[2,3]],
"pourra": [2],
"cochez": [1,7],
"needs-review-transl": [0],
"tagwip": [7,3],
"directionnel": [4,0,8],
"usb": [2],
"peut-il": [3],
"use": [2],
"dqs": [0],
"usd": [7],
"vous-mêm": [3],
"main": [3],
"convent": [3,0],
"modificatric": [0],
"omegat.jar": [2,0],
"poursuit": [3],
"omegat.app": [2,0],
"peut-êtr": [0,7,[3,5]],
"usr": [[0,1,2]],
"logo": [0],
"commun": [[0,3]],
"seulement": [2,0,[3,4,7],1],
"liste": [8],
"utf": [0,6],
"répartit": [7],
"signif": [0,7],
"servic": [2,1,5,4],
"obtenir": [[2,5,7],3],
"synchronisé": [2,1],
"dsl": [6],
"servir": [2],
"longueur": [0],
"authentif": [2,[0,1,5]],
"prend": [2,7,[0,6]],
"répercut": [2],
"med": [4,0],
"grisé": [[4,7],3],
"dtd": [[0,2]],
"commencera": [3],
"projet_save.tmx.bak": [2],
"multiparadigm": [7],
"mes": [2,0],
"immédiat": [3,0,[2,7],[1,4,5]],
"met": [[1,6]],
"affichage": [8],
"fichier": [2,0,7,4,6,3,1,5],
"celui-ci": [7],
"projectcompilemenuitem": [0],
"classnam": [2],
"console-transl": [2],
"surlignag": [0],
"subséquem": [0],
"structure": [8],
"optionsautocompletehistorycompletionmenuitem": [0],
"gotonextuniquemenuitem": [0],
"conform": [2,[3,7]],
"insérerez": [0],
"prenn": [1],
"wordart": [0],
"confort": [7],
"princip": [[1,2,3,5,7]],
"dur": [2],
"inform": [2,5,1,[0,4],7,3,6],
"dus": [2],
"laisser": [[0,1,3,5]],
"commit": [2],
"targetlocalelcid": [0],
"laissez": [3,[2,5,6]],
"project_stats_match.txt": [[4,6]],
"banc": [0],
"grâce": [3,8],
"suscept": [2],
"précéder": [0],
"supporté": [0],
"sélectionnez-en": [5],
"libreoffic": [3,0],
"devriez": [2,[3,4]],
"autocompleterclos": [0],
"stocké": [[0,2,7],[1,3,5],[4,6]],
"conçus": [2],
"approprié": [2,0,[3,7]],
"précisent": [0],
"texte": [8],
"long": [0],
"signalera": [2],
"mis": [2,[6,7]],
"gestionnair": [4,2,[6,7]],
"énumérer": [0],
"variables": [8],
"puissant": [0,7],
"viewdisplaysegmentsourcecheckboxmenuitem": [0],
"editregisteremptymenuitem": [0],
"stats-output-fil": [2],
"polic": [1,4,5,[0,3]],
"progress": [5,[1,2]],
"open": [0,[1,2]],
"aliment": [1],
"entrant": [1],
"rapid": [7,[3,4],2],
"project": [0,[5,6,7]],
"取得": [[1,7]],
"xmx1024m": [2],
"canaux": [0],
"barr": [0,5,[2,3]],
"dzs": [0],
"penalty-xxx": [[2,6]],
"outil": [2,7,1,[0,4],6,3,8],
"gotonextsegmentmenuitem": [0],
"abord": [2,[0,3]],
"invers": [0,7],
"remplir": [[1,4,6]],
"réseau": [2],
"courriel": [0],
"modification": [8],
"ièm": [4],
"dropbox": [2],
"abort": [2],
"forcer": [0,[1,4]],
"internet": [1,4],
"comma-separ": [0],
"brut": [[0,7]],
"identité": [1],
"organis": [0],
"printf": [0,1],
"saisie": [8],
"remplit": [[2,6]],
"génériqu": [1,[0,7],2],
"affichag": [5,1,7,3,[0,4],6],
"appuyé": [3],
"majuscul": [0,4,2],
"saisir": [[3,7],[0,2,5]],
"arriverez": [0],
"préserver": [0],
"ci-dessus": [0,2,[1,4,7],[3,5,8]],
"bash": [[0,2]],
"tmroot": [0],
"base": [[0,2],7,[1,3,4,6]],
"sépare": [1],
"registr": [0],
"difficil": [[0,2]],
"téléchargez": [2,6],
"appel": [[0,1,2,5]],
"moi": [0,[2,3]],
"mon": [2],
"tentera": [7],
"mot": [0,7,[4,6],1,2,5],
"大学": [1],
"ferm": [[4,7]],
"corrigez": [2],
"thème": [1,7,0],
"télécharger": [2,0,[1,4]],
"insertcharslr": [0],
"ayant": [1],
"carnet": [6],
"permett": [[0,2],7,[3,4]],
"réussi": [2],
"word": [[0,3,7]],
"variat": [0],
"lingue": [1],
"langag": [7,0],
"sachant": [0],
"extension": [8],
"vôtre": [0],
"prunell": [0],
"réinitialis": [7,0,1,5,4],
"flèches": [7],
"propag": [[2,7]],
"général": [2,3],
"encapsulé": [7],
"convient": [5,[3,4]],
"ignoré": [0,7,[2,6]],
"terminé": [7,2,0],
"converti": [[2,7]],
"pâle": [4],
"gère": [2],
"arrêt": [1],
"détaillé": [2],
"lingvo": [6],
"developer.ibm.com": [2],
"mrs": [1],
"historiqu": [4,[0,1],3],
"agenc": [2],
"correspondances": [8],
"concerné": [7,3],
"basé": [7,[1,4],[0,5]],
"permet": [7,4,2,[0,1],3,5],
"voici": [2,[0,1,3]],
"aller": [0,5],
"feux": [7],
"repèr": [0],
"récemment": [[0,4,5]],
"méthode": [2,[5,7]],
"manipul": [7,[2,4,5]],
"ecr": [5],
"appar": [1],
"html": [0,2,[1,3]],
"guillemet": [[0,7]],
"stockage": [8],
"spell": [0],
"déverrouil": [5],
"ver": [2,0,[5,7],1,[3,4,6]],
"insertcharsrl": [0],
"indiquez": [[1,3]],
"remplac": [7,4,0,5,1,3,[2,6]],
"nièm": [7],
"identiqu": [4,7,1,2,6,[0,5]],
"finit": [1],
"avancé": [7,0],
"finir": [7],
"qualité": [4],
"conseil": [2],
"flexibilité": [3],
"privilégi": [2],
"configuration": [8],
"www.ibm.com": [1],
"contributeur": [[0,7]],
"dernièr": [1,[0,4,6,7],[2,5]],
"désigner": [2],
"glisser": [7,5,[1,2]],
"entrain": [1],
"développé": [[0,7]],
"toolsalignfilesmenuitem": [0],
"agir": [2],
"non-concord": [7],
"respectiv": [[0,2,6]],
"contenir": [[2,6],0,5,[1,7]],
"command": [7,2,1,0,4,5,3],
"professionnel": [3],
"inspiré": [7],
"respectif": [[0,6]],
"préparation": [8],
"agit": [7,6,[0,2,5]],
"retir": [7,3],
"requises": [8],
"onecloud": [2],
"notat": [0,1],
"viewmarkbidicheckboxmenuitem": [0],
"refus": [[2,3]],
"testé": [2],
"absenc": [4],
"faire": [8],
"préférenc": [4,0,5,[1,7],[2,3],6],
"branch": [2],
"via": [2,7],
"permettrait": [3],
"fileshortpath": [[0,1]],
"officiell": [0],
"compteur": [7],
"permiss": [7],
"absent": [2],
"approxim": [7],
"entour": [3],
"日本語": [7],
"instruct": [2,0,[3,7]],
"assigné": [4,7],
"version": [2,0,[4,7],[3,8]],
"folder": [5,0],
"longtemp": [0],
"même": [2,7,0,1,3,5,4,6],
"conflit": [2,[0,3]],
"projecteditmenuitem": [0],
"arrière-plan": [6],
"new_word": [7],
"séquenc": [0],
"rédigé": [3],
"appellera": [0],
"nashorn": [7],
"machin": [[1,7],[2,4]],
"bloc-not": [5,3,4,0],
"last_entry.properti": [6],
"ceci": [[2,3],5],
"reflété": [3],
"exploit": [[0,4],2,[1,5,6,7]],
"notam": [5],
"critèr": [7,[0,3]],
"répétition": [4,7,[0,5]],
"notez": [7],
"statistiqu": [4,6,7,1,[0,2]],
"autocompleternextview": [0],
"specif": [7],
"épineus": [3],
"utilisé": [0,7,2,3,1,6,4,5],
"dsun.java2d.noddraw": [2],
"apparaitr": [7,5],
"coché": [0,7,[1,2]],
"ell": [0,7,2,1,4,[3,6],5],
"résumé": [2,8],
"ceux-ci": [5],
"séparer": [0,2,7],
"editorfirstseg": [0],
"x0b": [2],
"canada": [2],
"altern": [0,5,[1,4],7,[2,3]],
"enregistrement": [8],
"abréviat": [0],
"http": [2],
"apparaiss": [1,[0,3]],
"plates-form": [1],
"lisenc": [0],
"octobr": [1],
"hôte": [2,0],
"empilé": [7],
"parfoi": [0,[2,3]],
"projectsinglecompilemenuitem": [0],
"end": [0],
"vos": [3,[0,2],[5,7],8,1,6],
"lisens": [0],
"modifiées": [8],
"pouvez": [2,[0,7],3,1,[5,6],4],
"visuel": [[0,4]],
"recherché": [7,0,2],
"env": [0],
"okapi": [2],
"page_down": [0],
"remplacez": [2,0],
"joker": [2],
"trier": [0,1],
"taper": [2],
"copyright": [4],
"officiel": [8],
"system-os-nam": [0],
"occurr": [0,7],
"insertcharspdf": [0],
"empêchent": [2],
"systèm": [2,0,[1,4],7,[3,5,6]],
"instantané": [2],
"reflètent": [[2,6]],
"rien": [4,[2,3,7]],
"traductair": [3,[0,2]],
"invis": [0],
"tar.bz2": [6],
"comport": [2,5,[1,7],[0,4,6]],
"invit": [2],
"bundle.properti": [2],
"typiqu": [2],
"contributors.txt": [0],
"livré": [1],
"xmxtaill": [2],
"pertinent": [[0,1,2]],
"www.regular-expressions.info": [0],
"sourcelang": [0],
"ruptur": [0,1],
"décroissant": [0],
"cell": [7,[0,1,2],[3,5]],
"optionsdictionaryfuzzymatchingcheckboxmenuitem": [0],
"valid": [[0,4],1,[2,6]],
"tâches": [2,7],
"assur": [[1,4],[0,2]],
"interfac": [2,0,4,[1,7],[3,5,6]],
"désormai": [[0,2]],
"projet": [2,7,6,[3,4],0,1,5,8],
"ordinateur": [2,[1,3,8]],
"sourcelanguag": [1],
"précédant": [0],
"crée": [2,4,0,6,[3,7]],
"appliqué": [[0,1,2]],
"gzip": [6],
"enregistré": [[0,1,4],[2,7],[3,5],6],
"helpupdatecheckmenuitem": [0],
"notif": [5],
"donnez": [2],
"esc": [5],
"ci-dess": [0,2,6,[3,7]],
"traduits": [8],
"cela": [0,[1,2],[5,7],[3,6]],
"séparé": [[0,7],[2,3],[4,5]],
"affectera": [7,0],
"nostemscor": [1],
"demi-largeur": [7],
"déconnectez-v": [2],
"cent": [0],
"est": [0,2,7,5,1,4,6,3,8],
"vue": [1],
"project_chang": [7],
"devienn": [2],
"donner": [0,2],
"console-createpseudotranslatetmx": [2],
"devient": [6],
"offrant": [3],
"etc": [1,[0,7],6,[3,4,5]],
"numéroté": [5,0],
"fuzzyflag": [1],
"dehor": [0,[3,6]],
"facultatif": [4],
"escap": [0],
"new": [1],
"manière": [8],
"traduire": [8],
"positionn": [0],
"poisson": [7],
"réel": [[2,5]],
"ensuit": [3,[2,4],[0,5,7]],
"supprimé": [1,2],
"glossair": [0,5,7,4,[1,3],6,2],
"intervall": [1,2,[4,6]],
"obsolèt": [0],
"cellul": [0],
"eux": [4,[2,5]],
"deux-point": [0],
"autres": [8],
"reçu": [5],
"rendez": [3],
"calcul": [7],
"chose": [2,1],
"juridiqu": [0],
"encadré": [0],
"magento": [2],
"créé": [2,6,[0,7],[1,3,4]],
"ll.tmx": [2],
"adjacent": [5],
"supprimez-l": [2],
"mémoires": [8],
"bidirectionnalité": [0],
"simultané": [[1,2,4]],
"u00a": [7],
"néanmoin": [[0,3]],
"localis": [2],
"surligné": [7,5],
"fenêtre": [8],
"shift": [0,7],
"tabulé": [0],
"totalité": [2],
"utiliserez": [[1,2,6]],
"java": [2,0,1,7,3],
"exe": [2],
"project_save.tmx": [2,6,[3,7],4],
"lancé": [1,2],
"jaun": [4],
"dictionari": [6,[1,5,7]],
"powershel": [[0,2]],
"exceptionnell": [3],
"dictionary": [8],
"ceux-là": [3],
"graphiqu": [2,0,[1,5,7],[3,6]],
"monétair": [0],
"besoin": [0,3,[2,6],1,7],
"envoi": [[2,4]],
"collaboratif": [2],
"presse-papi": [4],
"cett": [7,1,2,0,4,3,5,6],
"structurel": [2],
"traduction": [8],
"envoy": [2,[0,4],[1,5]],
"langu": [2,1,7,0,3,6,5,4],
"sudo": [2],
"timestamp": [0],
"raccourcis": [8],
"projectaccessrootmenuitem": [0],
"tout": [0,2,7,3,6,4,[1,5]],
"gras": [1,[5,7],0,3],
"prendr": [2,3,[4,7],1],
"tous": [7,2,1,0,[3,4],6,5],
"volet": [5,3,7,4,1,6,2,0],
"plugin": [0,[1,2]],
"état": [5,2,[0,6],[3,7],[1,4,8]],
"autocompletertableup": [0],
"simplicité": [2],
"aurez": [[2,6]],
"nécessitera": [2],
"découvrir": [[0,3]],
"pourrez": [7],
"chargement": [7,[0,2]],
"projectcommitsourcefil": [0],
"editinsertsourcemenuitem": [0],
"viterbi": [7],
"microsoft": [0,[3,7]],
"projectnewmenuitem": [0],
"ecmascript": [7],
"segment": [7,4,0,5,1,3,6,2,8],
"utilis": [2,0,7,3,[1,4],5,6,8],
"yeux": [3,[0,8]],
"publiez": [2],
"désarchivez": [2],
"changes.txt": [[0,2]],
"inférieur": [[1,7],5],
"publier": [2],
"glossari": [0,[6,7],[4,5]],
"imposs": [5],
"ignored_words.txt": [6],
"préféré": [0],
"github.com": [2],
"configuration.properti": [2],
"examin": [7,3],
"lesquell": [[0,2]],
"prototyp": [7],
"ceux": [[0,2,5,6,7]],
"autocompleterlistpageup": [0],
"expressions": [8],
"glossary": [8],
"composé": [[0,1,5]],
"anglai": [2,0,1],
"ayez": [[3,7]],
"import": [0,7,2,4,[5,6],3],
"color": [1,[0,4]],
"string": [2],
"déclaré": [0],
"classes": [8],
"gérées": [3],
"souvenez": [3],
"non": [0,7,[1,4],[2,3],5,6],
"nom": [0,[2,5,7],1,3,6],
"not": [0],
"protégé": [[1,3]],
"abordé": [0],
"parcourir": [3,[0,4,5]],
"ascii": [0],
"double-cliqu": [7,2,[4,5]],
"particulièr": [0,7],
"effet": [0,[1,7]],
"viewrestoreguimenuitem": [0],
"serait": [[0,7]],
"selection.txt": [[0,4]],
"xhtml": [0],
"empaqueté": [4],
"cliquant": [7,[5,6]],
"itoken": [2],
"situé": [7,0,2,4,5,[1,3],6],
"région": [0],
"finder.xml": [[0,6,7]],
"spécifier": [2,[0,7]],
"orthographiqu": [1,3,[6,7],4,[0,2]],
"proportionnell": [1],
"accélérer": [2],
"grec": [0],
"apparence": [8],
"window": [0,2,4,5,3],
"votre": [8],
"spécifiez": [2],
"plateform": [2,0],
"aprè": [0,2,[1,7],5,3,[4,6]],
"disable-project-lock": [2],
"autant": [2,3],
"donné": [2,1,[4,7],[0,6],3],
"omegat.pref": [[0,1,7]],
"inconnu": [0],
"documenté": [2],
"txml": [2],
"appliquera": [0],
"plan": [1],
"présentées": [8],
"multipl": [5,0,[2,3]],
"habitué": [4],
"autotraduit": [[0,6],4,[1,2]],
"explicit": [2],
"trouvent": [2,[0,6],1,[3,4]],
"droit": [0,5,7,4,2,[3,6]],
"effectué": [2,3,[0,1,7]],
"suit": [2,0,[4,5,6,7]],
"widget": [5],
"suffisam": [2],
"direct": [2,0,4,6,7],
"connexion": [1,2,[5,8]],
"d\'entr": [4],
"modern": [2],
"choisissez": [2,0,[1,4]],
"web": [1,7,2,[0,5]],
"récent": [2,4,[0,7]],
"passag": [0],
"provoqu": [[2,4]],
"souvenir": [4],
"récursif": [7],
"temporair": [[0,1,5]],
"editselectfuzzy4menuitem": [0],
"editregisteridenticalmenuitem": [0],
"gris": [4],
"hanja": [0],
"recherche": [8],
"récursiv": [7],
"usag": [2,[0,7]],
"japonais": [2],
"mettr": [7,2,[0,3],[1,5,6]],
"positif": [1],
"effac": [[0,4,7]],
"certain": [2,0,1,[4,5,7],[3,6]],
"advanc": [1],
"section": [2,3,0,7,1,[4,5]],
"remplaceront": [7],
"encodage": [8],
"protocol": [2,1],
"rattach": [5],
"authentifié": [1],
"ainsi": [0,2,[4,7],1,3],
"enchain": [0],
"orphelin": [7],
"feu": [7],
"écraser": [6,[2,5]],
"sous-menus": [[2,7]],
"dict": [1],
"contextuel": [[1,5],[0,3,4,7]],
"faisant": [[0,1,5]],
"omniprés": [4],
"intitulé": [0],
"licence": [8],
"dispon": [7,2,1,0,5,4,3],
"dispos": [7,[1,5]],
"géré": [2],
"effectu": [[0,2],4,[6,7],[1,3]],
"conventions": [8],
"équip": [2,4,[0,6],[1,3,7],5],
"option": [2,0,7,1,4,3,[5,6]],
"recourt": [2],
"inséré": [[4,6],1,2,7],
"lancera": [2],
"gamm": [0],
"rester": [6,[4,7]],
"contre-jour": [0],
"élevé": [1,5],
"diffèr": [7],
"héroïn": [0],
"donnant": [8],
"visuell": [[0,3]],
"fermiez": [7],
"spéciaux": [0,2],
"comptabilis": [4],
"paquet": [2,[0,4]],
"précédées": [0],
"archiv": [6],
"décrit": [0,5,2,7],
"atteindr": [0,5,3,[2,4],[6,7],1],
"proxi": [2,1],
"extens": [0,2,1,6,[3,4,5,7]],
"back_spac": [0],
"constitue": [8],
"fin": [0,7,1,[3,4]],
"recalcul": [7],
"encodé": [0],
"fond": [[4,5]],
"robot": [0],
"répétés": [7,[1,4],0],
"rendr": [6,[0,3,7]],
"eclips": [2],
"prochain": [0,4,2,[1,3]],
"globales": [8],
"ai": [0],
"diff": [1],
"carré": [1],
"fonctionnalité": [2,[1,3],[4,7]],
"an": [0],
"editmultiplealtern": [0],
"complèt": [[0,2]],
"former": [0],
"aq": [7],
"proxy": [8],
"perdr": [2],
"au": [0,7,2,4,[3,6],1,5],
"moyen": [7,0],
"formel": [2],
"déposé": [5],
"perdu": [2,4,3],
"définira": [7],
"parcourus": [4],
"productivité": [0],
"filters.xml": [0,[1,2,6,7]],
"pensez": [2],
"br": [0],
"search": [0],
"tierc": [2,3],
"identificateur": [4],
"segmentation.conf": [[0,2,6,7]],
"préexist": [[4,7]],
"ca": [2],
"minuscul": [0,4],
"ce": [0,2,7,6,4,[1,3,5],8],
"charge": [8],
"définiss": [0,2,1],
"soutient": [0],
"figur": [5,3],
"cs": [0],
"librement": [[0,5]],
"installera": [1],
"sélectionn": [7,4,[0,1],5,[2,3,6]],
"apach": [2,7],
"adjustedscor": [1],
"font": [0],
"présentation": [8],
"de": [0,2,7,1,4,3,5,6,8],
"détection": [1],
"ajustez": [7],
"extern": [7,1,[0,4],[3,5],[2,6]],
"forc": [0],
"f1": [[0,4,7]],
"f2": [[3,5],[0,7]],
"f3": [[0,4],5],
"f5": [[0,3,4]],
"du": [2,0,7,5,6,4,[1,3],8],
"contrôler": [0],
"dz": [6],
"editundomenuitem": [0],
"enregistrez": [[0,2],3],
"confianc": [1,7],
"virtuell": [7,2],
"doublé": [0],
"remarqu": [3],
"icôn": [4,5],
"belazar": [1],
"en": [2,7,0,4,1,3,5,6,8],
"actif": [1,[0,4,5]],
"copiant": [[3,6]],
"es": [0],
"et": [0,2,7,3,4,5,1,6,8],
"familiaris": [0],
"eu": [2],
"autorisé": [0],
"ex": [[0,2]],
"résoudre": [8],
"défaut": [0,7,1,4,2,5,6,3,8],
"inquiét": [3],
"activ": [4,0,[1,5,7],2],
"gard": [4],
"foi": [2,0,7,4,3,6,1,5],
"indic": [0],
"terminologi": [4],
"origin": [2,[3,7],[0,1],[5,6]],
"sélectionnez": [4,1,[3,7],5,2,0],
"for": [7,2],
"exclud": [2],
"fr": [2,1,3,0],
"morceaux": [3],
"contenu": [0,2,7,3,1,6,[4,5],8],
"content": [0,2,7],
"duckduckgo": [1],
"applescript": [2],
"exclut": [[0,2]],
"exclus": [7,2],
"json": [2],
"exclur": [5],
"class": [0,7],
"helplogmenuitem": [0],
"fusionnera": [7],
"editoverwritetranslationmenuitem": [0],
"go": [2],
"aeiou": [0],
"vérification": [8],
"form": [[0,7],[1,5],4,3],
"attaché": [7,1],
"fort": [0],
"essentiel": [2],
"assign": [4,7],
"hh": [2],
"duser.languag": [2],
"viewmarkparagraphstartcheckboxmenuitem": [0],
"sauvegard": [2,6,1,7],
"vert": [7,5,4],
"éviter": [2,0,3,[4,7]],
"spécifiqu": [7,2,[0,1],6,4,3],
"file-target-encod": [0],
"ravi": [0],
"dû": [[3,7]],
"mainmenushortcuts.mac.properti": [0],
"context": [[1,5],[2,4,6]],
"création": [7,4,[1,6],[0,2,3,5]],
"issus": [6],
"id": [1,0],
"https": [2,1,0,[5,6]],
"if": [7],
"project_stats.txt": [6,4],
"ocr": [7],
"permi": [3],
"entré": [7,4,1,[0,5],[2,3],6],
"sélectif": [2],
"projectaccesscurrenttargetdocumentmenuitem": [0],
"il": [0,2,7,6,4,3,5,1],
"contrôlent": [2],
"in": [5,7],
"publiqu": [2],
"termin": [2,0,7],
"ip": [2],
"verb": [0],
"index": [0],
"is": [0],
"projectaccesstmmenuitem": [0],
"odf": [0],
"complètement": [[0,2]],
"modèl": [1,0,7,2],
"ja": [[1,2]],
"odt": [[0,7]],
"gotonexttranslatedmenuitem": [0],
"jj": [2],
"charset": [0],
"librari": [0],
"provenc": [0],
"logogramm": [0],
"disparit": [2],
"déjà": [2,[4,7],6,[0,1,3]],
"toolscheckissuescurrentfilemenuitem": [0],
"libraries.txt": [0],
"learned_words.txt": [6],
"imposé": [6],
"aboutiss": [2],
"reprendr": [3,7],
"affich": [4,1,0,5,[2,7],3,6],
"ftl": [[0,2]],
"agréger": [0],
"portant": [[0,2,3,5,6]],
"actuell": [7,[4,5],[2,6]],
"viewdisplaymodificationinfoallradiobuttonmenuitem": [0],
"placez": [6,2],
"conservé": [2,[0,6],[1,7]],
"compressé": [6],
"canadien": [1],
"la": [0,7,2,4,1,5,3,6,8],
"placer": [2,0,7],
"lc": [2],
"automatique": [8],
"le": [0,2,7,5,4,1,3,6,8],
"li": [0],
"dictionnaires": [8],
"astérisqu": [0],
"ll": [2],
"fur": [2,[0,1,5]],
"consécutif": [7,0],
"ordr": [[1,7],[0,5],[4,6]],
"ls": [2],
"problèmes": [8],
"lu": [0],
"mot-clé": [7],
"ja-jp.tmx": [2],
"second": [4,[0,1,2]],
"survient": [2],
"cycleswitchcasemenuitem": [0],
"précis": [7,0],
"régulières": [8],
"limit": [0,2,3],
"mm": [2],
"entri": [7],
"mo": [2],
"masquer": [7,0],
"ms": [0],
"mt": [2,[3,7],6,[0,1,4]],
"essai": [[0,7]],
"ouvrant": [0],
"entro": [0],
"graphiques": [8],
"termes": [8],
"plus": [0,2,7,4,3,1,5,6,8],
"quotidien": [[0,2]],
"plaisir": [3,8],
"ne": [0,2,7,1,3,4,5,6],
"permettra": [3],
"essay": [[2,7]],
"constitué": [[0,3,7]],
"ni": [0,2],
"paramétrag": [2],
"opposé": [0],
"licenss": [0],
"no": [0],
"code": [0,7,2,3,1],
"aideront": [4],
"définit": [0,1,4,[2,7]],
"maintenir": [[3,5]],
"évite": [7],
"gotohistoryforwardmenuitem": [0],
"prennent": [1,[0,2]],
"contrôl": [0,4,3,2],
"head": [0],
"project_save.tmx.timestamp.bak": [6],
"preniez": [7],
"of": [7],
"définir": [1,0,7,5,[2,3,4]],
"possibl": [[0,2],[1,4,5],3,6],
"ok": [7,4,3],
"on": [0,[4,5],2],
"valeur": [0,7,2,1],
"os": [0],
"ou": [0,7,2,4,5,[1,3],6],
"insécabl": [7,[0,4],3],
"elle-mêm": [[2,3]],
"chargé": [6,[1,3,7]],
"editinserttranslationmenuitem": [0],
"là": [3],
"fileextens": [0],
"empêcher": [2,4,3],
"pi": [0],
"restent": [7],
"po": [[0,2],1,[5,6]],
"encor": [[1,4],[3,5],[2,6,7]],
"pp": [2],
"inclur": [2,[0,1],[3,5,6,7]],
"correspondra": [0,1],
"inclut": [2,7,[0,3]],
"formul": [3],
"inclus": [[2,7],0],
"pu": [3],
"récupérer": [4,2,[0,1]],
"exportées": [8],
"référer": [0],
"autocompletertablefirst": [0],
"calculé": [[1,5],7],
"faibl": [0,7],
"zéro": [0,7,2],
"rompra": [1],
"github": [2],
"qu": [0,[2,7],6,1,5,[3,4]],
"old": [1],
"ancienn": [2,7],
"editselectfuzzy5menuitem": [0],
"bilingu": [2,[6,7]],
"rc": [2],
"régional": [2],
"fonctionnement": [8],
"includ": [2],
"adopt": [2],
"apparaitra": [1],
"t0": [3],
"t1": [3],
"fenêtres": [8],
"t2": [3],
"t3": [3],
"minut": [2,[1,3,4,6]],
"échanger": [2],
"tombez": [3],
"grammatical": [7],
"aient": [[3,4]],
"sa": [2,0,[1,3],[4,5,6,7]],
"disposit": [1,[4,5],2],
"sc": [0],
"se": [2,0,4,6,7,5,3,1],
"bleu": [[5,7]],
"si": [2,7,0,4,1,3,5,6],
"quelqu": [0,2,3,4,1],
"codé": [6],
"intern": [2,4,[0,1,5]],
"mots-clé": [7,3],
"celle-ci": [[0,2,7]],
"où": [[0,2],[3,4],[5,7],6,1],
"quitt": [4],
"divisé": [[0,1,5,7]],
"editoverwritesourcemenuitem": [0],
"omegat.autotext": [0],
"dialogues": [8],
"ont": [0,6,2,[1,7],[3,4,5]],
"enforc": [[4,6],[0,2],[1,3]],
"remov": [2],
"tm": [6,2,4,[0,7],1,[3,5,8]],
"to": [2,7],
"v2": [2,1],
"correspondre": [8],
"document.xx": [0],
"tu": [0],
"commercial": [3],
"fermetur": [7,[0,1]],
"aide": [8],
"ancré": [[3,5]],
"dialogu": [[1,7],3,[0,4],2,6],
"brièvement": [[0,5]],
"trouv": [4,2,[0,7],[1,5]],
"corrig": [[1,2,4]],
"sûr": [2],
"viewmarkautopopulatedcheckboxmenuitem": [0],
"projectwikiimportmenuitem": [0],
"countri": [2],
"plein": [7],
"distribué": [7,[0,8]],
"tableaux": [0,8],
"un": [0,2,7,3,1,4,6,5,8],
"up": [0],
"jusqu": [2,7,[3,5],[0,1,6]],
"neutr": [0],
"bloqué": [5],
"newword": [7],
"désirez": [2],
"sélectionnez-l": [0],
"assurez-v": [2,[1,3,5]],
"devront": [[1,2,3]],
"solut": [2],
"this": [0],
"va": [2,4,[0,7]],
"modules": [8],
"redémarrag": [2],
"opt": [2,0],
"extract": [7,1],
"extérieur": [[4,5]],
"soient": [0,[2,3,6,7]],
"à-d": [0],
"désactiv": [4,7,5,2],
"voudrez": [0],
"renommé": [[0,3]],
"réfléchissez": [3],
"amélioré": [7],
"vs": [1],
"support": [2],
"libr": [2,[0,4,5]],
"changed": [1],
"vu": [2],
"coin": [5],
"changez": [1,[0,7]],
"sein": [0],
"deviez": [3],
"réorganis": [[0,4,5]],
"disposez": [[1,6]],
"recréé": [6],
"copiez-l": [2],
"changer": [7,3,[0,1,2,5]],
"vertical": [0],
"arbitrair": [2],
"lien": [[0,2],[1,3],[5,6]],
"autocompleterlistup": [0],
"licenc": [2,0,4],
"prenant": [2],
"omegat.project.bak": [2,6],
"préfère": [0],
"lieu": [0,[2,3,4]],
"présentant": [0],
"capacité": [2],
"exécuté": [7,[1,2]],
"projectaccessexporttmmenuitem": [0],
"licens": [[0,2]],
"org": [2],
"fractionn": [7,0],
"reconnus": [0,7],
"distribut": [2,0,7],
"récupérat": [2],
"requièr": [0],
"lentement": [2],
"choix": [7,2,0],
"poursuivez": [2],
"ancien": [2],
"xx": [0],
"hostil": [4],
"sourc": [0,2,7,4,1,5,6,3],
"extrait": [0,7],
"type": [2,7,0,1,[4,6],5],
"décochant": [0],
"lign": [0,7,2,1,5,4,6],
"optionsautocompletehistorypredictionmenuitem": [0],
"souri": [7,[4,5],[3,6]],
"avantag": [7],
"commentair": [0,5,7,[2,3,4]],
"projectaccesssourcemenuitem": [0],
"yy": [0],
"phase": [7],
"montrent": [0],
"comm": [0,2,7,6,[1,4,5],3],
"facult": [2],
"contract": [0],
"sombr": [1],
"balises": [8],
"quant": [7],
"dans": [8],
"push": [2],
"exist": [[2,4],[0,3,5,7],[1,6]],
"propr": [2,7,[0,5]],
"readme_tr.txt": [2],
"propo": [[0,4]],
"préciser": [0],
"penalti": [6],
"représent": [0,1,7],
"exact": [7,0,[2,4],[3,6]],
"organisé": [0],
"envisagez": [3],
"prédéfini": [1,2,[0,3]],
"confirmé": [[1,2]],
"oui": [7],
"caractéris": [0],
"retour": [0],
"cacher": [7],
"utf8": [0,[4,7]],
"continuez": [2],
"copi": [2,1,[4,7],[0,6]],
"coréen": [1],
"depui": [2,5,[0,3,4,7]],
"entretemp": [2],
"servent": [0],
"pourcentag": [5,1,6],
"power": [0],
"quand": [[1,2],[0,7]],
"longu": [[0,1]],
"context_menu": [0],
"emplacement": [8],
"editsearchdictionarymenuitem": [0],
"inversé": [0,[1,2]],
"tag-valid": [2],
"événement": [0],
"convertissez": [[2,3]],
"chemin": [2,0,1,7],
"help": [2],
"demandera": [[1,2,3]],
"décorat": [3,[0,7]],
"repositori": [2,6],
"minimum": [6,[0,1]],
"date": [[0,1],7,[2,3,6]],
"souhait": [0],
"lowercasemenuitem": [0],
"feuill": [0,7],
"wiki": [[2,6]],
"empêch": [[2,7]],
"autocompleterconfirmwithoutclos": [0],
"précède": [0,4],
"norm": [2],
"sélecteur": [2],
"quatr": [0,[2,4,7]],
"filepath": [1,0],
"mesur": [2,1,[0,4,5,7]],
"horodaté": [2],
"index.fra.html": [1],
"ja-jp": [2],
"doivent": [0,[1,2],7],
"défectueux": [2],
"post-traitement": [8],
"serveur": [2,1,6,5],
"bloc": [7,0,3],
"installé": [[1,2],[0,3,4],[5,7]],
"note": [2,0,4,7,5,1,3,6],
"redémarrez": [2],
"couleur": [1,4,[6,7],0],
"risquent": [3],
"ressemblera": [3],
"git": [2,6],
"ouvrira": [4],
"xx-yy": [0],
"connex": [6],
"provienn": [6,[2,5]],
"sept": [0],
"défini": [0,[2,7],[1,5],[4,6],3],
"potentiell": [[0,1,6]],
"virgul": [0],
"intens": [[0,4]],
"follow": [0],
"intent": [0],
"provient": [2],
"tableau": [0,1,4,5],
"targetlang": [0],
"exécuteur": [7],
"optionssetupfilefiltersmenuitem": [0],
"tabulair": [1,5],
"contrair": [0,[1,2,7]],
"altgraph": [0],
"stats-typ": [2],
"souhaité": [2,1,[0,4,7]],
"lui-mêm": [0,[6,7]],
"régulièr": [0,7,2,1,6,3],
"xml": [0,2,1],
"devrez": [7,0,[1,2,5]],
"italiqu": [[0,3,7]],
"anglais-japonai": [2],
"sert": [6],
"enlevez": [2],
"cour": [7,[0,4,5],2,6,1],
"nous": [0,2,3],
"grammaticaux": [4],
"apparent": [2],
"xdg-open": [0],
"befor": [2],
"sera": [0,2,[4,6,7],1,5,3],
"util": [2,7,0,4,3,[1,5]],
"paniquez": [3],
"abordon": [0],
"tar.bz": [6],
"numériqu": [0],
"rendent": [0],
"arab": [0],
"indiquent": [8],
"lire": [7,2,0],
"flottant": [1],
"écran": [[0,3]],
"remplacé": [2,0,7,[1,4]],
"shebang": [0],
"dépôt_de_toutes_les_sources_des_projets_omegat_en_équip": [2],
"editorskipprevtoken": [0],
"raccourci": [0,4,3,7,5,2],
"spécifi": [0,2],
"seul": [0,7,1,2,4,3],
"réglage": [2],
"dépôt_de_tous_les_projets_omegat_en_équip": [2],
"fiabilité": [2],
"heur": [7,[0,2,3,4]],
"nécessair": [2,[3,7],0,[1,6]],
"union": [0,4],
"feuillet": [7],
"paramètr": [2,0,7,4,5,1,[3,6]],
"aaaa": [2],
"gnu": [2,8],
"considérez": [7],
"autoris": [1,7,[0,4]],
"suzum": [1],
"target.txt": [[0,1]],
"temurin": [2],
"standard": [2,3,[0,1,5,7],4],
"traduct": [2,5,3,4,7,1,0,6],
"correct": [2,7,0,4,[1,5],[3,6]],
"stdout": [0],
"moment-là": [1],
"reconnaitr": [3],
"vérifi": [0,7,[2,4]],
"déplacement": [5],
"taill": [1,7,[2,5]],
"nameon": [0],
"spécialis": [2],
"entier": [2,[0,1,5]],
"renommez": [2],
"nettoyag": [7],
"lisibilité": [1],
"gotonextnotemenuitem": [0],
"par": [0,7,2,1,4,6,5,3,8],
"autour": [1],
"gpl": [0],
"pas": [0,2,7,1,3,4,5,6],
"newentri": [7],
"pay": [2,1],
"list": [0,[1,2],7,3,4,5,6],
"spécial": [0,6],
"autocompleterprevview": [0],
"veiller": [2],
"intégré": [[1,2]],
"veillez": [[2,7],[0,1]],
"synchronis": [2,7,[3,5],[4,6]],
"arrivez": [3],
"formats": [8],
"livr": [3],
"échéant": [5,[1,2,7]],
"projectcommittargetfil": [0],
"partiell": [1,4,[2,5,6],7],
"aussi": [2,7,0,[3,4],6],
"po4a": [2],
"combin": [2,[6,7]],
"japonai": [[1,2],7,0],
"rechargé": [[2,4,6]],
"omegat.org": [2],
"menus": [5,[0,4,7],[2,3,6],8],
"liaison": [7,6],
"générée": [2],
"impliqu": [2],
"perform": [[1,3]],
"générés": [[0,1]],
"fondat": [2],
"regroup": [5],
"maxprogram": [2],
"reconnu": [[0,5,6,7],[1,2,4]],
"pdf": [2,0,4,7],
"réglée": [4],
"chariot": [0],
"extrayez": [6],
"en-us_fr_project": [2],
"acheté": [0],
"étaient": [0],
"autocompletertabledown": [0],
"bouger": [0],
"volumineux": [7],
"editornextsegmentnottab": [0],
"toolsshowstatisticsmatchesmenuitem": [0],
"marquera": [4],
"viewdisplaymodificationinfononeradiobuttonmenuitem": [0],
"locaux": [2,[0,7],1,5],
"créées": [[2,7]],
"gtk": [1],
"peu": [[3,4,7]],
"project_save.tmx.bak": [6],
"period": [0],
"moteur": [5,4,1,7],
"contiendra": [[3,4,6]],
"projectaccesswriteableglossarymenuitem": [0],
"catégori": [0],
"install": [2],
"préparat": [2,[0,6],7],
"application_shutdown": [7],
"résoudr": [2,3,6],
"autocompletertablelastinrow": [0],
"enfoncé": [7],
"dépass": [0],
"unité": [0,7],
"regexp": [0],
"entrez": [[2,5],[1,4,7]],
"rappel": [[0,3,4,6]],
"sentencecasemenuitem": [0],
"emplac": [2,0,4,[3,7],1,6],
"choisi": [[2,5],4,7],
"filtrant": [3],
"entrer": [[2,5],3,[1,4]],
"indiqué": [0,[3,5],4],
"premièr": [7,[0,5],4,[1,2],[3,6]],
"articl": [0],
"instant": [0],
"editorcontextmenu": [0],
"surement": [2],
"optionssentsegmenuitem": [0],
"bascul": [0,[4,5]],
"fluidité": [3],
"robust": [2,3],
"françai": [2,1,7],
"suivant": [0,4,7,2,5,3,1,6,8],
"reconnaiss": [0,3],
"typag": [7],
"optionsaccessconfigdirmenuitem": [0],
"charact": [2],
"framework": [2],
"tôt": [3],
"test.html": [2],
"php": [0],
"xxx": [6],
"ll_pp.tmx": [2],
"instanc": [2,[4,5]],
"smalltalk": [7],
"curseur": [5,4,3,0,7],
"marqueur": [[0,1,5],2],
"varient": [[0,2]],
"voir": [2,4,7,0,3,1,[5,6]],
"pseudotranslatetmx": [2],
"soyez": [3],
"motif": [7,0,1],
"magiqu": [0],
"amélior": [0,[3,4]],
"targetlanguagecod": [0],
"editorprevsegmentnottab": [0],
"objet": [7,2],
"uniqu": [0,7,1,2,[4,5],3],
"suivi": [0,1,[2,3]],
"annul": [0,4,7],
"est-à-dir": [4,0],
"manuell": [7,[0,2]],
"approch": [2],
"deuxièm": [[1,5,7]],
"saisissez": [[3,7],[0,5],[1,2]],
"lancer": [2,[3,4],[0,7,8]],
"plaçant": [[2,7]],
"suivr": [[2,3]],
"lancez": [5],
"diaposit": [0],
"époqu": [3],
"cohérent": [4],
"départ": [[0,1]],
"tier": [[2,5]],
"identif": [2,1,[0,3,5]],
"lanc": [2],
"devant": [[0,2]],
"point-virgul": [2],
"conven": [2],
"renommag": [2],
"accord": [2],
"gérer": [3,8,2,1],
"masqu": [0,1,2,7],
"pein": [3],
"particuli": [0,2],
"précèdent": [0],
"projectnam": [0],
"changement": [[2,3],[0,4,6,7]],
"répété": [1],
"prête": [2],
"revanch": [0],
"automatis": [[2,3,7]],
"traduir": [2,3,7,0,5,4,6],
"recharg": [[4,7],0,3,2],
"cohérenc": [2],
"traduit": [7,0,4,2,3,5,1,6],
"traduis": [0,7,2],
"étudier": [8],
"doublon": [[0,5]],
"configdir": [2],
"vont": [0,[2,4]],
"fonctionn": [2,0,[1,4,7]],
"effacera": [1],
"installdist": [2],
"a-z": [0],
"affichera": [1,3],
"fusionné": [7,3],
"meilleur": [7,1,[3,5],[0,2,4,6]],
"fluidifier": [8],
"gotonextxenforcedmenuitem": [0],
"editordeleteprevtoken": [0],
"souviendra": [2],
"sous-titr": [2],
"devraient": [[0,1]],
"regroupez": [1],
"compatibilité": [0],
"expressé": [2],
"javascript": [7],
"marqu": [[0,7],4,[1,2,5]],
"mediawiki": [[4,7],[0,3]],
"sinon": [2],
"échouent": [7],
"toolkit": [2],
"omegat.project.aaaammjjhhmm.bak": [2],
"join.html": [0],
"fonctionnel": [7],
"initialis": [2],
"prévu": [2,1],
"omegat.kaptn": [2],
"davantag": [3,[0,1,7]],
"exporté": [[2,6,7],4,[0,3]],
"pop": [0],
"publique": [8],
"nombr": [[0,7],5,1,2,4,6,3],
"millier": [0],
"chaqu": [0,2,[4,7],1,6,3,5],
"dossier_exclu": [2],
"parvenez": [[1,2]],
"débutant": [3],
"charg": [2,7,[1,3],6,4],
"googl": [1],
"révision": [[2,3]],
"prêt": [2],
"gotoeditorpanelmenuitem": [0],
"continus": [0],
"attend": [0],
"viewmarkfontfallbackcheckboxmenuitem": [0],
"détect": [4,[1,2]],
"revient": [6],
"procéder": [[2,7],0],
"align": [7,[0,2,4],3,1],
"insertcharsrlm": [0],
"malgré": [7],
"classiqu": [[2,4]],
"sourceforg": [2,0],
"guides": [8],
"structur": [7,6,0,[2,3]],
"han": [0],
"fluidifi": [3],
"décrire": [2],
"semeru-runtim": [2],
"champ": [7,5,4,[0,3],2,1],
"aujourd": [0],
"exécutez": [[0,7]],
"last": [0],
"intérieur": [5,0,2],
"editmultipledefault": [0],
"adapt": [[1,7],[3,5]],
"mozilla": [[0,2]],
"editfindinprojectmenuitem": [0],
"déposant": [5],
"marqué": [1,7,[0,6]],
"pro": [1],
"début": [0,[2,4,7]],
"souvent": [3,[0,7],2],
"warn": [2],
"procédez": [2,0],
"attent": [7,0],
"pratiqu": [3,6,[0,2,7],4,5,1],
"préparer": [0],
"votr": [2,3,4,7,0,1,6,5],
"utilisez-l": [7],
"retenir": [0],
"pert": [[2,3]],
"commune": [0,2],
"plupart": [0,4,1],
"cherchez": [0],
"déverrouillé": [5],
"limité": [[0,2,6]],
"panneau": [[5,7]],
"prudenc": [2],
"retirez": [[2,6]],
"facilité": [3],
"chercher": [0],
"minimal": [8],
"décrite": [[0,2],[3,5]],
"coloré": [[1,5]],
"travaillez": [[2,7],1],
"bidirectionnell": [7],
"exceptionnel": [2],
"duckduckgo.com": [1],
"corrompr": [2],
"personnalisé": [0,2,1,4,3],
"équivaut": [0],
"chang": [7,4],
"clés": [7,[0,1]],
"pui": [[0,3],[2,7]],
"kanji": [0],
"cyan": [4],
"gardez": [[2,7]],
"python3": [0],
"options": [8],
"potentiel": [4],
"tran": [0],
"dégradé": [3],
"vous": [2,7,3,0,4,1,6,5,8],
"traduisiez": [3],
"dossier": [2,7,0,6,4,1,3,5,8],
"iraq": [0],
"importé": [5],
"mener": [7],
"utilisera": [1,7,2],
"entêt": [0,[4,7]],
"récupèr": [2],
"identifiez": [2],
"sagac": [3],
"écrit": [7,0,2],
"tabl": [1],
"appuyez": [4,3,7],
"doc-license.txt": [0],
"réciproqu": [2],
"traiter": [0,1,[2,4]],
"rempli": [6,[3,4]],
"チューリッヒ": [1],
"peut": [2,7,0,4,5,[1,6],3],
"pseudotranslatetyp": [2],
"chain": [7,0,[1,4],3,2],
"affecté": [5],
"garder": [0],
"cepend": [[0,7],[2,3]],
"toutefoi": [7,[0,2]],
"titr": [4,7],
"fournisseur": [1,5],
"projectclosemenuitem": [0],
"nouveaux": [[0,3,7],2,4],
"viewmarknonuniquesegmentscheckboxmenuitem": [0],
"européenn": [4],
"apportez": [2],
"déterminez": [1],
"group": [0,1,[2,7],3],
"dictionnair": [1,3,6,5,4,0,7,2],
"findinprojectreuselastwindow": [0],
"échouera": [2],
"visité": [6],
"développ": [2,[0,7],1],
"nommé": [2,[0,1,3,7]],
"readme.txt": [2,0],
"importe": [8],
"languagetool": [4,1,[7,8]],
"source.txt": [[0,1]],
"files.s": [7],
"nouvell": [2,4,0,7,[5,6]],
"exchang": [0],
"traitement": [[1,3],[4,8]],
"incluant": [0],
"currseg": [7],
"point": [0,1,3,[2,4,7]],
"colonn": [7,0,4,1],
"ensembl": [1,0,[2,7],4,6,3],
"surlignera": [0],
"tiré": [3],
"jugé": [2],
"facil": [3,2,6,[0,7]],
"autocompletertrigg": [0],
"attribut": [0],
"nécessit": [0,1,2,[4,5,7],3],
"dès": [2,[4,6,7]],
"processus": [3,2,0,[1,7]],
"acquiert": [1],
"lettr": [0,2,[3,4],7,1],
"opérat": [3,[2,7]],
"dhttp.proxyhost": [2],
"clôt": [0],
"connaitr": [2],
"alphabet": [0],
"simplifi": [1],
"editorprevseg": [0],
"suggèr": [5],
"bidirectionnel": [4],
"barre": [8],
"rencontrez": [[4,5]],
"a-za-z0": [0],
"www.apertium.org": [1],
"contient": [[0,6],2,7,5,[1,3]],
"nom_du_projet": [7],
"court": [7,[1,3]],
"corrompus": [2],
"configur": [2,0,1,7,4,3,5],
"rencontrerez": [2],
"nativ": [2,0],
"mettant": [0],
"unicode": [8],
"contienn": [0,4,[2,3,7],6],
"affin": [5],
"mega": [0],
"zurich": [1],
"空白文字": [2],
"étang": [0],
"identifié": [1,[2,3]],
"hor": [2],
"optionsworkflowmenuitem": [0],
"désactivé": [[1,4],7,0],
"how": [2],
"précédé": [0],
"releas": [2,0],
"étant": [[0,1,6,7],[2,4]],
"modifié": [[0,7],[1,2],4,[3,5],6],
"explic": [7,[0,1]],
"caractères": [8],
"identifiants": [8],
"modificateur": [0,3],
"voulez": [2,0],
"connaiss": [2],
"gestion": [2,3,4],
"dictroot": [0],
"atteindre": [8],
"approfondir": [0],
"séquentiel": [0],
"étape": [7,2,[0,3],6],
"inchangé": [3],
"subdir": [2],
"manièr": [0,[2,3],4,1],
"hésitez": [3,[0,2]],
"trait": [0,1,2],
"énumèr": [0],
"précise": [[2,5,6]],
"train": [7,5],
"trouvera": [0],
"autocompletertableleft": [0],
"autrement": [[0,6]],
"activé": [[1,5],4,0,3],
"simplement": [[0,7],3],
"trié": [[5,7]],
"ll-pp.tmx": [2],
"repli": [4,0],
"bucarest": [0],
"soit": [2,0,3,6,[4,7],1],
"dissimulé": [[5,7]],
"exécut": [2,7,[0,1],4,3],
"quitterez": [7],
"série": [0],
"editorlastseg": [0],
"file-source-encod": [0],
"tant": [0,[2,4,7],[1,5,6]],
"session": [[2,3,5]],
"entr": [0,2,1,[5,7],4,3,6],
"divis": [7,0,[3,4]],
"lequel": [0,[2,7],5,[1,3,4]],
"navigateur": [1,[4,5]],
"baudelair": [0],
"petit": [4,0],
"passer": [7,0,[3,4],[2,5]],
"alpha": [2],
"大学院博士課程修了": [1],
"ouvrez-l": [2],
"prévus": [0],
"just": [0,2],
"quelconqu": [0,6],
"collabor": [3],
"editexportselectionmenuitem": [0],
"trop": [[1,2]],
"recommandon": [[0,2]],
"troi": [0,2,[1,3,4,6,7],5],
"home": [0],
"protéger": [1],
"disable-location-sav": [2],
"condit": [[0,2,6]],
"interprét": [0],
"projectaccesstargetmenuitem": [0],
"fournit": [[0,2],3,[1,7]],
"surlign": [4,0,[5,6],[1,7]],
"fournir": [[0,2,7]],
"iana": [0],
"hui": [0],
"illustrations": [8],
"fusionn": [7,3,[0,1]],
"insèr": [4,7,5,1],
"fiabl": [6,[0,2]],
"nouveau": [3,2,7,0,1,4,6,[5,8]],
"visibl": [0,6],
"agrandi": [5],
"linguistiqu": [1,[0,4]],
"décision": [6],
"aligndir": [2],
"sont": [7,0,2,1,5,4,6,3],
"system-host-nam": [0],
"action": [[5,7],2,[1,3,4]],
"mymemory.translated.net": [1],
"creat": [0,[2,7]],
"python": [7],
"précisé": [0],
"créés": [[3,6],[4,5],[0,2,7]],
"accepté": [7,2,1],
"tarbal": [6],
"repos": [0],
"soustrait": [6],
"pluriel": [0],
"facteur": [4],
"rajout": [7],
"similitud": [1],
"adress": [0,2,[5,6]],
"figé": [6],
"diver": [[0,3,7]],
"réactiv": [7],
"éventuel": [2,0],
"file": [7,2,0,5],
"travail": [3,2,0,6,[5,7,8]],
"gauch": [0,4,7,5],
"prise": [[0,2,4,6],[1,7]],
"tard": [[2,3],5],
"édition": [7,[0,5],3,[1,4],8],
"menu": [0,4,5,1,7,3,8,2,6],
"transfor": [0],
"probabl": [[2,3,5]],
"globaux": [[0,7]],
"individuell": [0,4],
"invoke-item": [0],
"liberté": [8],
"éditabl": [5],
"radio": [7],
"source-pattern": [2],
"adéquat": [2],
"plutôt": [[0,7],[1,2,3,4]],
"flagrant": [0],
"occup": [7],
"autocompletertablepagedown": [0],
"caché": [6],
"rompr": [0],
"sort": [[0,2]],
"task": [2],
"prenez": [6],
"xliff": [[0,2]],
"true": [0],
"vérifiera": [1],
"orthograph": [1,0,[4,7],3],
"déposez": [6],
"groovi": [7],
"troisièm": [6,[0,2,5]],
"défiler": [[3,4,5]],
"sommair": [[0,1,2,3,4,5,6,7]],
"était": [[0,1]],
"mineur": [6],
"lectur": [[0,1]],
"défilez": [1],
"sous": [5,0,2,1,7,4,3,6],
"transform": [0,[1,4]],
"fléchées": [7,5,0],
"auparav": [[2,3]],
"créée": [[2,3,5]],
"sous-ensembl": [[0,2]],
"dépassent": [0],
"annoté": [3],
"décoratif": [3],
"messageformat": [1,0],
"préservé": [0],
"master": [2],
"collègu": [2,5],
"numéro": [[0,2,4],7,[5,6]],
"déposer": [[2,5]],
"correcteur": [6],
"writer": [0],
"recommandé": [2,[0,7]],
"dalloway": [1],
"rubi": [7],
"resource-bundl": [2],
"puiss": [[0,2]],
"habituel": [4],
"external_command": [6],
"reconnait": [[2,3,7]],
"étendr": [2],
"étendu": [[0,7]],
"editorselectal": [0],
"précédée": [0],
"runner": [7],
"mégaoctet": [2],
"précédés": [0],
"fractionnera": [7],
"omegat-default": [2],
"installez": [2],
"user.languag": [2],
"regex": [0,3],
"d\'un": [4],
"installer": [8],
"meta": [0],
"risqu": [[0,7]],
"except": [1,0,2,3],
"programm": [2,0,[3,7],[1,5]],
"extrémité": [[0,4]],
"global": [7,0,1,4,5],
"sentiez": [2],
"racin": [0,[3,4,6],[1,2,5,7]],
"ressources": [8],
"autohébergé": [2],
"modèle": [8],
"chiffr": [0,1,[5,7],[2,3]],
"navigu": [6,[3,4,5]],
"projet_save.tmx.aaaammjjhhmm.bak": [2],
"rubriqu": [2],
"préfix": [6,1,2],
"exclu": [2],
"cochant": [7],
"légèrement": [[0,5]],
"plug-in": [[0,1]],
"enregistrera": [4],
"vallé": [0],
"simplifié": [2],
"face": [3],
"ibm": [[1,2]],
"commenç": [0],
"comprenn": [3,0],
"scénario": [2],
"ici": [[0,1,5],7,6,[2,3,4]],
"interrog": [0],
"par-dessus": [2],
"quittant": [[1,4]],
"alimenté": [6],
"guis": [2],
"envoyé": [2,[1,4]],
"omegat-cod": [2],
"précision": [0],
"dépôt_du_projet_omegat_en_équip": [2],
"agrandit": [5],
"concentr": [3],
"guid": [3,[0,2,6],4,7,5,1],
"laquell": [[0,2],3],
"retraduir": [2],
"idx": [6],
"que": [[0,2],7,3,[1,6],5,4],
"arriv": [2],
"qui": [0,2,7,1,4,3,6,5,8],
"non-ruptur": [1],
"membr": [2,3],
"linus": [0],
"cherchera": [1],
"autocompleterconfirmandclos": [0],
"projectaccesscurrentsourcedocumentmenuitem": [0],
"linux": [0,2,4,5,[1,3,7]],
"abrégé": [0],
"travaux": [2],
"linux-install.sh": [2],
"compt": [[1,2],4,[0,7],6,3],
"fermant": [0],
"relancez": [6],
"file.txt": [2],
"existant": [2,7,[1,3,6]],
"commenc": [0,3,[2,4,7]],
"openxliff": [2],
"ifo": [6],
"lemmatis": [1,[0,4,5]],
"reprendrez": [0],
"venant": [[0,4]],
"segmenté": [0,3],
"comment": [0,[1,3,7]],
"comprend": [7,[2,5]],
"enfin": [[1,2]],
"optionsmtautofetchcheckboxmenuitem": [0],
"xx.docx": [0],
"fait": [7,2,3,0,[1,4,5,6]],
"définissez": [0,3],
"fair": [0,2,7,5,4,[1,3],6],
"consist": [0,[2,4],3],
"repris": [3],
"editorshortcuts.properti": [0],
"cibl": [0,4,7,2,1,3,6,5],
"compress": [0],
"craignez": [2],
"projet_save.tmx": [[2,6]],
"annexes": [8],
"idé": [7],
"décochez": [7,1],
"sdlxliff": [2],
"versions": [8],
"externes": [8],
"décocher": [7],
"englob": [0],
"conteneur": [0],
"travailliez": [3],
"requis": [1,2],
"précisant": [0],
"tmotherlangroot": [0],
"viewmarknotedsegmentscheckboxmenuitem": [0],
"aisément": [7],
"poir": [0],
"séparez": [0,1],
"arrêté": [3],
"supprimez": [[2,7],6,[0,1]],
"fonctionnera": [0],
"journé": [[0,4,7]],
"gotomatchsourceseg": [0],
"excel": [0],
"verrouillez": [3],
"comma": [0],
"interagissait": [2],
"maitris": [2],
"runn": [7],
"règles": [7,1,0,4,2,[6,8]],
"trouvé": [7,[0,3],[1,2,4]],
"stardict": [6],
"omegat.l4j.ini": [2],
"comme": [8],
"span": [0],
"prefer": [0],
"obligatoir": [[0,7]],
"souligné": [4,5],
"space": [0],
"quantité": [2],
"révisé": [3],
"ドイツ": [7,1],
"simpl": [0,[1,2],3,7],
"saisissez-l": [7],
"emploient": [2],
"ilc": [2],
"editselectfuzzy3menuitem": [0],
"entouré": [1],
"fals": [0,2],
"project.projectfil": [7],
"verrouillag": [5,0],
"quelques-un": [2],
"manuel": [4,[0,3],[2,7],8,6],
"trace": [6],
"public": [2],
"annex": [[0,7],1,4,[2,3,6],5],
"appartienn": [[0,1]],
"tmx2sourc": [2,[0,6]],
"sauvegardé": [0],
"ini": [2],
"pomm": [0],
"dessin": [0],
"rappelez-v": [0],
"désign": [0],
"emploi": [3],
"agira": [6],
"dhttp.proxyport": [2],
"stockera": [7],
"dernier": [0,4,7,5,[2,6]],
"sorti": [2,0],
"subrip": [2],
"journaux": [0],
"score": [1,7,6],
"agrégé": [0],
"tapant": [7],
"références": [8],
"appliqueront": [[1,2]],
"rouvrir": [2],
"courant": [2,5,[0,7]],
"décompress": [2],
"illustr": [3],
"raw": [2],
"conserv": [7,2,0,6],
"réutilis": [2,[3,4],[1,7]],
"diagramm": [0],
"charger": [2,7],
"personn": [2,1,7,4],
"parallèl": [2],
"cloné": [2],
"généraux": [1,7,0,[4,8],3],
"dollar": [0],
"recycl": [2],
"close": [7],
"décimal": [0],
"abc": [0],
"conten": [0,[2,3],[5,6,7],4],
"crochet": [0],
"toolbar.groovi": [7],
"donnera": [0],
"annulé": [[0,4]],
"signifi": [[0,2],[1,5]],
"dupliqué": [[2,7]],
"originel": [0],
"iso": [[0,2]],
"supprim": [[2,7],0,6,4,[1,3]],
"cinq": [1],
"optionspreferencesmenuitem": [0],
"enregistr": [[2,4],1,7,6,0,5],
"concord": [1,6],
"traitera": [[0,5]],
"glossary.txt": [[2,6],[0,4]],
"dessus": [[2,5,7]],
"finiss": [0],
"add": [2],
"initi": [7,2,1],
"accè": [2,1,[0,4],7],
"indépendam": [2],
"respect": [7],
"rfe": [7],
"récupéré": [1],
"faux": [1],
"faut": [[0,2],[1,4,7]],
"shell": [0],
"port": [2,1],
"entry_activ": [7],
"optionsautocompleteshowautomaticallyitem": [0],
"préférabl": [2,7],
"gotoprevxautomenuitem": [0],
"administrateur": [2],
"interfér": [4],
"rouvrez": [2],
"ishan": [0],
"automatiques": [8],
"chapitr": [2,[3,7],0,[4,5]],
"modifi": [0,7,2,[4,5],3,6,1],
"espac": [0,7,[3,4,5],1,2],
"pour": [2,0,7,1,3,4,5,6,8],
"erreur": [2,[1,4],[0,3],[5,7]],
"extrêmement": [0,2],
"rechargez": [6],
"textuel": [[0,1,7]],
"exempl": [0,7,2,1,[4,5],3,6],
"activez": [6,[0,1,4,7],[2,5]],
"intègr": [2],
"clone": [2],
"targetlanguag": [[0,1]],
"ciblez": [2],
"écrire": [7,[2,4]],
"post-trait": [[0,7],1],
"properti": [2],
"durant": [7],
"combinaison": [0,1,[3,4,7]],
"editselectfuzzyprevmenuitem": [0],
"préférez": [[0,7]],
"trouver": [0,2,7,[1,3],6],
"identifi": [0,1,2,[3,4],7],
"renforcé": [6],
"orienté": [7],
"onglet": [5,1],
"algorithm": [7,4],
"triée": [5],
"décidez": [3,2],
"journali": [0],
"effacé": [2],
"l\'un": [4],
"dépôts": [2,7,[0,5]],
"trouviez": [3],
"script": [7,0,2,4,1,[3,6]],
"spécific": [0,7],
"versionné": [6],
"poursuivr": [4],
"aid": [0,5,2,7,[3,4,6],1],
"exig": [[1,2,7]],
"remplissez": [2],
"partiel": [2],
"cadr": [[1,3,5,7]],
"déverrouillag": [5],
"robustess": [3],
"ajust": [7,[2,6]],
"gigaoctet": [2],
"lancement": [2,0,1,[4,7]],
"écrivez": [2],
"ais": [2],
"ait": [0],
"local": [2,7,1,0,4,[5,6]],
"segments": [8],
"accentué": [0],
"extravagant": [0],
"fermera": [4],
"souhaitiez": [0],
"commencez": [2],
"indiqu": [5,0,7,[1,2,3],6],
"générale": [7,0,2,4,[1,8]],
"trouvez": [[0,3]],
"rle": [0,4],
"généralement": [0,[1,7],[2,5]],
"écoul": [0],
"parenthès": [0,1],
"rlm": [0,4],
"créatif": [0],
"compté": [4],
"chinoi": [1],
"fréquemment": [[0,3,4,7]],
"modifier": [8],
"filtr": [0,7,2,1,4,3,6],
"pseudotraduit": [2],
"énumérant": [0],
"correspond": [0,2,4,1,7,6,5,3],
"c-x": [0],
"modifiez": [7,1,6],
"mode": [2,7,5,4],
"copier": [[2,7],[0,1,5,6]],
"puisqu": [3,[0,2]],
"copies": [8],
"copiez": [2],
"préférences": [8],
"toolsshowstatisticsstandardmenuitem": [0],
"été": [2,[6,7],[1,4],[3,5]],
"oblig": [7],
"s\'ouvr": [7],
"read": [0],
"fichier.txt": [2],
"alt": [0,4],
"touch": [0,7,4,5,3,1],
"spécifié": [2,1,[0,3,7]],
"unit": [0],
"déverrouillez": [3],
"outils": [8],
"illimité": [[3,6]],
"couper": [[0,5]],
"collect": [6],
"veniez": [3],
"amp": [1],
"simples": [8],
"tkit": [2],
"imbric": [[0,4]],
"bogu": [[0,4]],
"and": [7],
"génèrerait": [6],
"ant": [[2,7]],
"compétenc": [[2,3]],
"stocker": [6],
"traducteur": [1],
"saisi": [0,1,3,5,[4,7],2],
"application": [8],
"cité": [0],
"objectif": [[0,8]],
"helplastchangesmenuitem": [0],
"alternatif": [4,7],
"hébergement": [2],
"omegat.ex": [2],
"influencé": [4],
"sourcetext": [1],
"ouvertur": [[0,2,7]],
"apparaitront": [0,[1,7]],
"bloquer": [4,[1,2]],
"compos": [0,[6,7]],
"ordinair": [0],
"café": [3],
"horizontaux": [0],
"introduit": [0,1],
"stockag": [1,2],
"compter": [[1,4]],
"boit": [1,3,7,[0,2,4],6],
"jar": [2],
"api": [[1,7]],
"editselectfuzzy2menuitem": [0],
"introduis": [3],
"introduir": [4],
"textuelle": [8],
"multiplateform": [2],
"marquer": [7],
"demand": [2,1,[4,5,6,7]],
"démarrag": [2,[0,7]],
"personnell": [0],
"avant-arrièr": [7],
"progressiv": [[1,2,6]],
"devez": [2,0,3],
"logiciel": [2,[0,4]],
"editornextseg": [0],
"seuil": [1,[2,5]],
"vide": [0,2,6,5,[1,4],[3,7]],
"déplacé": [[1,5]],
"différenc": [7,1,[3,4]],
"réalis": [2],
"pratiques": [8],
"apprendr": [[0,3]],
"editselectfuzzynextmenuitem": [0],
"gotonextxautomenuitem": [0],
"devis": [7],
"read.m": [0],
"notion": [[0,5]],
"différent": [2,4,[1,3],5,[0,7],6],
"cloud.google.com": [1],
"readme.bak": [2],
"arg": [2],
"généré": [2],
"sécurisé": [1,8],
"similair": [2,[0,3],[1,7],5],
"précédent": [[0,4],3,5,[2,7],[1,6]],
"suppress": [0,[2,3]],
"déroulant": [7,0,1],
"peuvent": [0,2,7,1,3,[4,5],6],
"seraient": [0],
"facilit": [3,[0,2,4]],
"call": [0],
"moin": [2,[0,7]],
"nécessitait": [2],
"tabul": [0],
"sécurité": [[1,7],[0,2]],
"textuell": [7,0,3,4],
"toolsshowstatisticsmatchesperfilemenuitem": [0],
"verrouillé": [5,2],
"référenc": [0,2,7,6,3,[1,4],5],
"run": [7,2],
"considérera": [1],
"efficac": [0,7,3],
"editorshortcuts.mac.properti": [0],
"titlecasemenuitem": [0],
"jeu": [0],
"editcreateglossaryentrymenuitem": [0],
"bonne": [8],
"feuilleton": [7],
"paramètres": [8],
"conservera": [5],
"introduc": [7],
"多和田葉子": [7],
"lorsqu": [7,0,[1,2],5,3,[4,6]],
"recréez": [3],
"name": [0],
"compri": [[0,7],1,6,[3,4,5]],
"réviser": [3,[2,5,8]],
"esprit": [[2,3,7]],
"recréer": [[0,2]],
"pertin": [1],
"démarrez": [0],
"sensibilité": [0],
"aux": [7,2,4,[0,6],[1,3],[5,8]],
"déplace": [7],
"démarrer": [[2,7]],
"remplacez-l": [2],
"vidé": [4],
"pizza": [0],
"avi": [5],
"terminologiqu": [0],
"introduct": [3,[2,8]],
"suivantes": [8],
"suivez": [2,7],
"napl": [0],
"editortogglecursorlock": [0],
"matièr": [2],
"bonn": [2,[0,3]],
"associ": [0,2,3,6,[1,4,7]],
"new_fil": [7],
"mond": [2],
"physiqu": [2],
"target": [[1,4,7],6,3,[0,8]],
"essayé": [2],
"horodatag": [6],
"config-dir": [2],
"editorskipprevtokenwithselect": [0],
"matières": [8],
"copiera": [[3,5]],
"souvienn": [6,2],
"allant": [0],
"termbas": [0],
"cass": [0,7,4,1],
"rétablit": [4,[1,5,7]],
"bord": [5],
"possédant": [1],
"ouvr": [4,7,2,[1,5],3],
"règle": [0,1,7,3],
"rend": [0,[6,7],[2,4]],
"case": [7,[0,1]],
"rétablir": [0,[1,4,5]],
"propriété": [5,2,0,3,4,6,7,1],
"conséquent": [2,7,3],
"voyez": [3],
"duré": [1],
"violet": [4],
"modul": [1,0,2],
"auteur": [[3,4]],
"targettext": [1],
"réduit": [[2,5]],
"signalé": [1,4,0,[3,5]],
"style": [[2,7]],
"réduir": [7],
"explor": [0],
"consultez": [3,[0,2]],
"orang": [[0,5,7]],
"insens": [0],
"compil": [2,7],
"recommand": [2],
"doublant": [0],
"edittagpaintermenuitem": [0],
"superflu": [[0,1,7]],
"trouvait": [2],
"étiez": [3],
"créez-en": [3],
"hiérarchi": [6,2],
"saisiss": [1],
"sembl": [3],
"alor": [2,[5,7],[0,1,3,6]],
"more": [5],
"déclencher": [7],
"unicod": [0,4],
"viewmarknbspcheckboxmenuitem": [0],
"projectmedcreatemenuitem": [0],
"visualis": [0],
"copié": [[2,4,5],[0,1,7]],
"whitespac": [2],
"msgstr": [0],
"hyperlien": [5],
"nationalité": [1],
"important": [2,[0,3]],
"boug": [4],
"phrase": [0,7,3,1,[2,4]],
"omegat.project": [2,6,3,[1,5,7]],
"retourn": [[4,7]],
"targetcountrycod": [0],
"mauvais": [2],
"insert": [0,[1,2,4,5]],
"continu": [[3,5,7]],
"vrai": [2],
"inutil": [6,1],
"comparaison": [7],
"messag": [5,2,0],
"scripts": [8],
"profit": [0],
"san": [2,[0,7],1,6,3,5],
"rest": [0,3],
"original": [[0,7]],
"pourront": [2],
"typographiqu": [[4,7]],
"glossaires": [8],
"situat": [[2,3]],
"consol": [2],
"partag": [2,7,[3,6],0],
"itokenizertarget": [2],
"viewmarkwhitespacecheckboxmenuitem": [0],
"quelques": [8],
"répéter": [[2,3]],
"aucun": [1,0,[4,7],2,6],
"ouvert": [2,[1,4,7],[0,5]],
"utilison": [3],
"établit": [0],
"symboliqu": [2],
"établir": [4],
"complet": [0,1,5],
"retournez": [3],
"partagé": [2,7,[0,5,6]],
"répétez": [3],
"avoir": [2,3,[1,5,6,7],[0,4]],
"bak": [2,6],
"télécharg": [7],
"perçu": [0],
"niveaux": [3],
"bas": [7,5,0,1,[2,4]],
"seront": [0,2,1,7,6,3,[4,5]],
"complex": [0,7,2],
"jre": [2],
"posit": [0,[5,7],4],
"pourraient": [[2,6]],
"résultat": [7,0,3,[4,5],1],
"structurell": [0],
"plant": [2],
"chemin-vers-fichier-projet-omegat": [2],
"rapidement": [8],
"alllemand": [7],
"affect": [2,4],
"delet": [0],
"proven": [6,2,5,4],
"permettr": [4],
"bcp": [[3,7]],
"constitu": [0,1,[2,6]],
"projectaccessglossarymenuitem": [0],
"javadoc": [7],
"consulté": [7],
"relatif": [[0,1,2]],
"segmentation": [8],
"ça": [0],
"sen": [0,5],
"humain": [1],
"poser": [0],
"ses": [[2,7],5,[0,1,6]],
"readme_fr.txt": [2],
"incorrect": [[6,7]],
"balis": [1,0,3,[4,7],2,5],
"thèmes": [1],
"voyell": [0],
"aller-retour": [2],
"correspondr": [0,[1,7],[2,3]],
"project.sav": [2],
"offic": [0,[3,6,7]],
"repositories.properti": [[0,2]],
"ben": [0],
"parti": [0,[1,3],7,[4,5],6,2],
"ramené": [6],
"repositories": [8],
"projectsavemenuitem": [0],
"contact": [[2,5]],
"xmx6g": [2],
"procédur": [2],
"autocompletertablefirstinrow": [0],
"tirer": [4],
"fiable": [8],
"intéressant": [0],
"ouvrent": [5,4],
"exploiter": [8],
"répons": [5],
"reconvertir": [2],
"tmautoroot": [0],
"réutilisé": [0],
"compat": [2,[0,1,7]],
"compar": [7],
"insertcharslrm": [0],
"pendant": [3,[0,2],[4,6,7]],
"effectuez": [6,3],
"écritur": [[2,5],1],
"elles-mêm": [1],
"client": [2,[0,6]],
"propriétés": [8],
"utilisation": [8],
"six": [3],
"laiss": [0],
"principaux": [[2,3]],
"associez": [0],
"restant": [7,2],
"traité": [0,[1,2],[3,5]],
"targetroot": [0],
"optionnel": [0,2],
"fourni": [2,7,1,4],
"montrer": [2],
"intégrat": [0],
"bin": [0,[1,2]],
"apertium": [1],
"kaptain": [2],
"meta-inf": [2],
"obstacl": [2],
"projectopenmenuitem": [0],
"autom": [2],
"tabulation-separ": [0],
"présentera": [[2,3]],
"consonn": [0],
"vertic": [0],
"urgenc": [2],
"attribu": [7],
"exécution": [8],
"oublier": [1],
"oubliez": [2],
"selon": [0,7,1,[2,3,8]],
"viewmarktranslatedsegmentscheckboxmenuitem": [0],
"paragraph": [0,7,5,[1,4],3],
"valu": [0],
"eux-mêm": [0],
"accès": [8],
"ilia": [2],
"aura": [0,[1,7]],
"informé": [1],
"littéraux": [0],
"clavier": [0,4,[1,5]],
"sélection": [7,4,0,5,1],
"uxxxx": [0],
"table": [8],
"macos": [8],
"mémoir": [2,7,3,6,4,0,5,1],
"editselectfuzzy1menuitem": [0],
"d.s": [0],
"réinséré": [3],
"hide": [5],
"sensibl": [0,7],
"autocompleterlistpagedown": [0],
"redistribuer": [8],
"auto": [[4,6],0,2,1],
"sign": [0,5],
"document.xx.docx": [0],
"majorité": [0],
"redémarr": [[0,4],1],
"editorskipnexttokenwithselect": [0],
"soutien": [[2,3]],
"évidenc": [7,5,[0,3]],
"suffit": [2,3,[6,7]],
"suffis": [0],
"confèr": [6],
"download": [2],
"son": [[0,6,7],[2,3],5],
"oracl": [0],
"editortoggleovertyp": [0],
"ailleur": [[4,5]],
"travaill": [0],
"autr": [2,0,7,[1,3,5,6],4],
"gradlew": [2],
"parait": [4],
"descendr": [0],
"lemmatiseur": [[1,2,7],[5,6]],
"partant": [7],
"boucl": [7],
"modif": [0,7,1,4,2,6,5,3],
"cascad": [1],
"viewmarklanguagecheckercheckboxmenuitem": [0],
"rencontré": [3],
"recherchez": [[0,2]],
"bon": [1],
"multimot": [0],
"déclarat": [0],
"supérieur": [[1,5]],
"recherches": [8],
"anormal": [0],
"pouvant": [2],
"total": [[5,7],[2,4]],
"utiliser": [8],
"bundl": [1],
"décompressez": [2],
"dessous": [2],
"macro": [7],
"aidera": [[2,3]],
"src": [2],
"passiez": [3],
"échell": [2],
"double-cliquez": [2,0],
"utilisez": [7,0,3,2,1,5,6,4],
"no-team": [2],
"stylistiqu": [0],
"alphabétiqu": [[0,5,7]],
"lissens": [0],
"incorpor": [2],
"détacher": [[5,7]],
"correspondant": [0,7],
"glissé": [5],
"exerc": [0],
"ssh": [2],
"masqué": [1],
"spécifiiez": [2],
"environ": [2],
"vari": [5,0],
"formatag": [0,4,7],
"filtrer": [[2,3]],
"filtres": [8],
"resteront": [[6,7]],
"individuel": [0,7,2],
"vérificateur": [1,[0,4],[3,6,7,8]],
"condensé": [1],
"convertisseur": [2],
"kde": [2],
"téléchargement": [2,1],
"accept": [6,2],
"nouvel": [7,[1,6]],
"access": [7,[0,2,4,6]],
"trouverez": [2],
"languag": [7,2],
"sur": [2,7,4,0,1,3,5,6,8],
"current": [0],
"crypté": [0],
"utilisai": [7],
"optionsglossaryfuzzymatchingcheckboxmenuitem": [0],
"assisté": [3],
"communic": [5],
"accueil": [[0,2]],
"msgid": [0],
"rejeté": [1],
"svn": [2,7,6],
"omegat-license.txt": [0],
"quell": [0,2],
"suivent": [0],
"confirm": [[4,6,7],1],
"sous-dossi": [2,0,7,6,4],
"associé": [0,[1,4],2,[3,5,6,7]],
"sujet": [2],
"editreplaceinprojectmenuitem": [0],
"bureautiqu": [2],
"but": [[0,2,6,8]],
"symbol": [0,5,[2,7]],
"génère": [2],
"optionnell": [4],
"editordeletenexttoken": [0],
"express": [0,7,1,2,3],
"changeant": [7],
"multilingu": [0],
"jour": [[0,2],[1,7],6,[3,4,8]],
"renommez-la": [2],
"êtes": [7,[0,2,3,5]],
"échappé": [2],
"variant": [[0,2]],
"verrouil": [2],
"gotoprevioussegmentmenuitem": [0],
"toujour": [0,2,7,1,[4,6],3],
"bienvenu": [3],
"dépôt": [2,6,[4,5],1],
"gotopreviousnotemenuitem": [0],
"stderr": [0],
"editredomenuitem": [0],
"uilayout.xml": [[0,6]],
"recherchera": [1],
"substitut": [[0,4]],
"sourceroot": [0],
"fermer": [7,0,2,[3,4]],
"détecté": [4],
"fermez": [2,4],
"jamai": [0,[1,3,4,6]],
"vaut": [3],
"servira": [2,3],
"lisez-moi": [0],
"deux": [2,7,0,[1,3,4],[5,6]],
"apport": [7,6],
"porté": [0],
"prédiction": [1,[0,3]],
"normal": [0,2,7,[5,6]],
"projet_save.tmx.tmp": [2],
"littérair": [0],
"aligneur": [7,4],
"jumelé": [1],
"liée": [2],
"différem": [4],
"détermin": [[1,7],0],
"enregistrez-la": [6],
"remplacement": [8],
"example.email.org": [0],
"transmis": [[0,2]],
"additionnell": [0],
"atteignez": [3],
"littéral": [1],
"remarquez": [[0,7]],
"évènement": [7,0,2],
"restaur": [[2,6]],
"runtim": [2],
"tester": [0],
"ressourc": [7,3,6,[0,2]],
"aligner": [8],
"techniqu": [0,2],
"beaucoup": [[0,1,3]],
"accolad": [0],
"filenam": [0,5],
"ajoutera": [6],
"éventuell": [0],
"écrase": [5],
"roam": [0],
"œuvr": [[1,3]],
"raison": [0,[1,2,7],4],
"gotosegmentmenuitem": [0],
"permettront": [5],
"initialcreationd": [1],
"racine": [8],
"étiquett": [1],
"soulign": [4,5,0,[1,3]],
"helpaboutmenuitem": [0],
"impress": [0],
"normaux": [[0,7]],
"place": [0,2,[1,7]],
"regular": [0],
"rejoindr": [3],
"suggest": [0,[1,5]],
"filter": [2],
"pérennité": [6],
"site": [0,2,1],
"projectroot": [0],
"mises": [8],
"réellement": [4],
"omegat.log": [0],
"utilitair": [2],
"autocompletertableright": [0],
"argument": [0],
"avez": [2,3,0,7,4,1,[5,6]],
"diverg": [4],
"restreindr": [3],
"garantit": [7],
"tab": [0,4,1,5],
"divers": [1,[2,3,5,7]],
"tag": [[0,5]],
"écrasé": [3,1],
"faudra": [0],
"tao": [[3,7]],
"parmi": [0],
"satisfait": [7],
"coller": [[4,5]],
"intermédiair": [2],
"projectreloadmenuitem": [0],
"choisit": [2],
"choisir": [0,4,[1,2,7],5],
"obliqu": [0,2],
"navig": [3,[4,5],6,2],
"filtrag": [[2,7]],
"avec": [2,0,[3,7],1,4,[5,6]],
"distinct": [2,[4,5,6,7]],
"reproduir": [7],
"reproduit": [0],
"tbx": [0,1],
"lorsq": [5],
"dynamiqu": [7],
"remont": [0],
"cas": [2,[0,1,5,7],6,[3,4]],
"auquel": [[0,1,2]],
"car": [[0,2],7,1],
"placé": [0,[4,6],[2,3,7]],
"utilisatric": [[0,7]],
"annulez": [7],
"duser.countri": [2],
"ponctuat": [0],
"readm": [0],
"disparaitr": [3],
"avertir": [5],
"considéré": [0,6],
"présenter": [4],
"problèm": [2,4,0,1,3,5,6],
"alphanumériqu": [0],
"parfait": [4],
"fragment": [0],
"align.tmx": [2],
"file2": [2],
"commentaires": [8]
};
