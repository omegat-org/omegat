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
 "appendix.LanguageToolPlugin.inOmegaT.html",
 "appendix.ScriptingPlugin.inOmegaT.html",
 "appendix.TeamProjects.html",
 "appendix.TokenizerPlugin.inOmegaT.html",
 "appendix.acknowledgements.html",
 "appendix.keyboard.html",
 "appendix.languages.html",
 "appendix.legal.notices.html",
 "appendix.shortcut.custom.html",
 "appendix.website.html",
 "chapter.about.OmegaT.html",
 "chapter.dictionaries.html",
 "chapter.file.filters.html",
 "chapter.files.and.folders.html",
 "chapter.files.to.translate.html",
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
 "Appendix E. Engadido de LanguageTool",
 "Appendix F. Engadido de Scripting",
 "Appendix C. Proxectos en equipo de OmegaT",
 "Appendix D. Engadido de Tokenizer",
 "Appendix J. Agradecementos",
 "Appendix B. Atallos de teclado no editor",
 "Appendix A. Idiomas - lista do código ISO 639",
 "Appendix I. Aviso legal",
 "Appendix H. Personalización de atallos de teclado",
 "Appendix G. OmegaT na rede",
 "Sobre OmegaT - Introdución",
 "Dicionarios",
 "Filtros de ficheiro",
 "Ficheiros e cartafoles de OmegaT",
 "Ficheiros a traducir",
 "Traballar con texto formatado",
 "Glosarios",
 "Instalar e executar OmegaT",
 "Aprenda a usar OmegaT en 5 minutos!",
 "Tradución automática",
 "Atallos de teclado e Menú",
 "Outros asuntos",
 "Traballar con texto plano",
 "Propiedades do proxecto",
 "Expresións regulares",
 "Buscas",
 "Segmentación do segmento orixe",
 "Verificación ortográfica",
 "Editar comportamento...",
 "Memorias de tradución",
 "A interface de usuario",
 "OmegaT 3.0 - Guía de usuario",
 "Index"
];
wh.search_wordMap= {
"eido": [1],
"comezan": [17],
"comezar": [21,[8,10,18,19,26,30]],
"definiron": [13],
"hall": [7],
"pide": [17],
"característica": [28],
"instalador": [13],
"emprego": [[21,32],[14,16,17],[10,12,31]],
"tel": [6],
"ten": [17,16,[14,27,30],[21,22,29],[1,2,10,12,15,20,26],[5,7,13,24,25]],
"empregu": [17,21,30,[11,12,13,19,20,23]],
"emprega": [17,[1,10,12,19],[2,14,16,20,21,22,27,29,30]],
"ter": [17,2,16,[15,21,25,29],[8,9,10,12,14,20,23,28,30]],
"info.plist": [17],
"vario": [[29,30],[2,10,14,17,25,28,32]],
"permitirall": [17],
"antonio": [7],
"coincida": [12,15],
"lea.m": [12],
"tex": [14],
"conseguirá": [29],
"fuzzi": [30],
"distinto": [[10,30],[5,12,14,21,29]],
"kua": [6],
"excluíndo": [29],
"esqueza": [16],
"nome_do_proxecto": [29],
"verán": [29],
"aragoné": [6],
"redor": [15],
"kur": [6],
"manteñen": [[16,17]],
"convertendo": [19],
"cen": [16],
"dgoogle.api.key": [17],
"ces": [6],
"edittagnextmissedmenuitem": [8],
"gikuyu": [6],
"tgl": [6],
"violeta": [20],
"modificar": [12,[17,23],30,[13,18,20,26]],
"quiet": [[17,21]],
"investigación": [19],
"xeito": [17,30,[2,14,15,22,29],[1,5,12,13,20,21,23,24,25,26]],
"enviándoll": [13],
"comunidad": [[2,29]],
"sami": [6],
"área": [23,[1,10,30]],
"porén": [[17,29],15,[4,14,18,19,21,22,23,26,30]],
"es_es.d": [27],
"imax": [32,[0,25,27,30]],
"bambara": [6],
"tha": [6],
"the": [[11,20,24]],
"download.htm": [17],
"preparar": [2,10],
"projectimportmenuitem": [8],
"resumo": [10,31],
"botón": [25,[17,23,30],[1,26]],
"imag": [17,13],
"maioría": [22,[20,26],[2,12,13,14,15]],
"traballan": [[2,10]],
"garda": [20,[23,29],[2,17,21]],
"tic": [16],
"extremadament": [15],
"currsegment.getsrctext": [1],
"traballar": [[15,22,32],31,[2,20,28,29]],
"substituír": [20,8,30,[12,19,29,32]],
"tir": [6],
"cha": [6],
"export": [28],
"che": [6],
"persoalment": [22],
"aplica": [17,[15,25,29]],
"reduc": [[3,14]],
"transtip": [20,8,30],
"corrixir": [15,[10,19,25,30]],
"tradutor": [29,30,14,[2,9,12,26]],
"checo": [[6,22]],
"chv": [6],
"coñecemento": [4],
"chu": [6],
"xxxx9xxxx9xxxxxxxx9xxx99xxxxx9xx9xxxxxxxxxx": [17],
"plataforma": [13,17,19,11],
"estabelec": [[2,26,28]],
"predefinido": [17],
"fr-fr": [27],
"ndonga": [6],
"madlon-kay": [7],
"xerai": [5],
"disco": [[2,17,20,29]],
"lapela": [30],
"directorio": [29,17,[2,21]],
"varia": [[29,30],[14,25],[13,17,18,20]],
"termina": [24],
"xmxzzm": [17],
"webster": [11,32,[30,31]],
"xeral": [13,7,23],
"xerar": [18,29,[23,31]],
"validar": [18,[8,20],[30,31]],
"duplicar": [15],
"lévase": [28],
"permit": [[17,20,28],[12,14],[1,15,16,19,21,24,29,30]],
"cargar": [[1,12,20]],
"contrario": [30],
"cargan": [29,16],
"empti": [29,[17,28]],
"irrelevant": [21],
"n.n_source.zip": [17],
"corrixa": [[15,30]],
"lepo": [3],
"spolski": [9],
"traducir": [12,[18,30],28,[10,13,14],29,[17,21,25,26,31],[9,19,20,23,32]],
"ofrec": [17,30,[9,10,12,13,19,23]],
"traduciu": [[10,12,21,27,28,29]],
"lepa": [3],
"telugú": [6],
"block": [21],
"orden": [25],
"rexistro": [[8,9]],
"tmx": [29,32,17,[20,30],[13,21],[8,10,25]],
"coidadosament": [13],
"realizado": [16],
"abrangu": [22],
"nl-en": [29],
"integ": [12],
"intel": [17,[31,32]],
"expandiu": [19],
"fr-ca": [26],
"mainmenushortcuts.properti": [8],
"gradualment": [29],
"angular": [16],
"empregarán": [12,[2,29]],
"realizada": [30],
"cmd": [[5,20],14],
"indonesio": [6],
"coach": [24],
"project_name-level1": [13],
"gotohistorybackmenuitem": [8],
"project_name-level2": [13],
"exportada": [29],
"estévez": [7],
"ton": [6],
"adxectivo": [3],
"unificación": [[14,15]],
"desancoralo": [30],
"exportado": [[16,20]],
"powerpc": [17],
"capacidad": [13],
"rota": [15],
"conxunto": [[23,26],25,[12,30]],
"tarefa": [[2,14]],
"rexistra": [13],
"coa": [24,30,[12,13],[17,22,29],20,[2,10,25],19,[7,11,15,16],[1,4,14,21,23]],
"solta": [[10,15]],
"amosaras": [12],
"necesitará": [17,[2,16,18,29]],
"instal": [17],
"con": [17,29,30,15,[10,20],24,[13,32],16,[1,31],[21,22,25],[8,12,14,18,23,28],[2,7,9,26],[19,27]],
"cirílico": [22],
"cos": [[10,32],[16,20,27],[2,11,12,14,17,25,29,31],[6,13,18,21,23]],
"cor": [14,[6,20,30]],
"haxa": [20,[10,17,23,29]],
"traducen": [19],
"mensax": [17,[2,21],[13,29,30]],
"sexan": [[4,20,29],[10,12,14,17,23,26]],
"lao": [6],
"sincroniza": [17],
"separador": [30],
"lat": [6],
"lav": [6],
"básica": [[15,17]],
"correcto": [17,[2,11,27]],
"esquema": [[1,12]],
"inútil": [23],
"translat": [19,32,[17,31],[10,12],[1,29]],
"plenament": [29],
"cargado": [29],
"aviso": [[7,32],[4,8,10,17,30,31]],
"incluírs": [2],
"búlgaro": [6],
"distinta": [19],
"tsn": [6],
"interesar": [19],
"correcta": [29,17,16,[3,11,18,19,27,28,30]],
"tso": [6],
"doar": [32],
"custo": [30],
"cre": [[6,29]],
"chmod": [13],
"recarga": [20,[16,21,29,30]],
"constrú": [1],
"gnome": [17],
"xustificación": [14],
"advertímosll": [26],
"inválido": [29],
"quer": [[8,12,30],[2,9,18,19,20,24,28]],
"convertido": [[14,20,23]],
"xestion": [12],
"almacena": [10],
"recargu": [25],
"significan": [17],
"significar": [30],
"sección": [[15,22],[14,17,19,23,25,26]],
"asistida": [10,[29,30,31]],
"ttx": [29,32],
"confidencialidad": [2],
"bielorruso": [6],
"appdata": [13],
"rexistrou": [19],
"definen": [30],
"colaboración": [2],
"comezo": [5,24,[13,17]],
"prev": [[0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30,32]],
"csv": [14,16],
"n.n_linux.tar.bz2": [17],
"traducila": [29],
"ofrecida": [[2,20]],
"lea": [[9,12,13,15]],
"tuk": [6],
"dunha": [24,13,[15,19,29],[3,16,20,21,23,25,30]],
"carga": [29,[12,17,18,21,30]],
"tur": [6],
"seguir": [[15,18,28,29,30]],
"tuv": [29],
"traducilo": [30],
"lep": [3],
"descrición": [[2,4,8,17,29,30]],
"lepš": [3],
"ler": [[12,14,29]],
"pada": [15],
"press": [8],
"dock": [17],
"leu": [13],
"ofrecido": [[2,19]],
"prevaleza": [29],
"ctr": [15],
"transferencia": [19],
"permitindo": [10],
"estruturada": [16],
"produza": [14],
"dmicrosoft.api.client_secret": [17],
"dobr": [17,13,30],
"traducimo": [14],
"estratexia": [[28,29]],
"fisicament": [27],
"directo": [2],
"cun": [[29,30],17,[16,19,20,22,25,28]],
"enumérans": [15],
"filenameon": [30],
"ctrl": [20,8,30,5,32,18,14,[15,29],[13,16,19,25,28],[1,11,23]],
"mykhalchuk": [7],
"twi": [6],
"document": [[12,13,14,15,17,21,29]],
"polaco": [6],
"fixes": [17,[9,29]],
"netrexx": [1],
"privacidad": [17],
"caixa": [23,[12,26],28,[25,27],[2,15,17,18,24]],
"superfici": [19],
"teñan": [[2,14,15,18,20,24]],
"descargu": [17,[11,13]],
"posición": [20,30,[14,26],[1,15,16,25,29]],
"lexislación": [29],
"resourc": [17],
"briac": [7],
"cargu": [29,[1,14,17,18]],
"resaltado": [30],
"desancorado": [20],
"xx_yy": [12,29],
"conveniencia": [30],
"docx": [14,[12,20,23]],
"txt": [22,[14,16],12],
"descarga": [17,[1,9,13,32]],
"coincident": [25,30],
"polaca": [0],
"definiu": [[0,19]],
"lituano": [6],
"traballando": [29,10],
"definir": [26,[10,12,16,21,23,24,30]],
"lib": [13],
"actualiza": [29],
"azerí": [6],
"asigna": [17],
"ojibwa": [6],
"lin": [6],
"lim": [6],
"seguen": [[15,24]],
"lit": [6],
"viewdisplaymodificationinfoselectedradiobuttonmenuitem": [8],
"marxinai": [23],
"omegat.tmx": [[13,29]],
"index.html": [[17,30]],
"manteras": [12],
"advertir": [13],
"destacado": [28],
"actual": [20,30,[8,13,21,25,28,29],[12,14,15,17,19,23,32]],
"actuai": [[2,13]],
"sobrescribirá": [20],
"destacada": [[20,25,30]],
"pago": [19],
"redacta": [[15,19]],
"formatado": [15,32,14,[18,20,29],23,[10,31],[22,26,30]],
"fixen": [0],
"fula": [6],
"colocada": [15],
"cym": [6],
"aspecto": [[10,18],12],
"chewa": [6],
"únase": [9,18],
"project.gettranslationinfo": [1],
"principio": [[2,16,20,26,29]],
"comando": [8],
"sobrescribirs": [13],
"destino": [12,20,29,14,32,27,[16,23,25,30],[15,17,19,28],[18,21],[0,1,3,10,22]],
"xavané": [6],
"substituiría": [19],
"restrición": [[11,17,28]],
"configurars": [14],
"lle": [[17,30],[13,16],[2,10,12,14,19,26,27]],
"casaco": [6],
"start": [17,32,31],
"decataras": [2],
"xérans": [13],
"smolej": [[7,31]],
"cursiva": [[12,15]],
"equal": [29,17],
"tiña": [26],
"arranxaron": [16],
"abreviar": [2],
"kulik": [7],
"comezou": [[23,29],30],
"anot": [2],
"optionsalwaysconfirmquitcheckboxmenuitem": [8],
"tmxs": [[29,30]],
"obvia": [16],
"ofici": [31],
"letón": [6],
"pali": [6],
"enter": [20,5,8,[15,17,28,30]],
"empregando": [25,[12,30,32],[1,16,19,20,21,27,28,31]],
"traducida": [12,[10,30]],
"applic": [13],
"bidi": [14],
"projectteamnewmenuitem": [8],
"brasileiro": [[17,27]],
"validou": [20],
"creativa": [26],
"preced": [13],
"traducido": [30,15,20,[14,18],[8,12],23,[13,25,28,32],[21,29],[10,16,17,19,31]],
"interactiva": [24],
"estaban": [30],
"creativo": [10],
"requiren": [13],
"mellor": [[26,30],[1,15,19,21,22,27,28,29]],
"pedindo": [2],
"realment": [20,[15,17,23,30]],
"afar": [6],
"omegt": [17],
"godfrey": [7],
"accidentai": [15],
"log": [[13,15]],
"especializado": [30],
"prezo": [[19,30]],
"eliminando": [15],
"crioulo": [6],
"visualización": [30],
"poñendo": [8],
"los": [24],
"emprégas": [30,20,[1,10,12,23]],
"necesita": [[17,29],[2,15],21,[13,19,27],[16,18]],
"comporta": [[13,28,30]],
"consult": [24],
"n.n_windows_without_jre.ex": [17],
"sueco": [6],
"cargars": [30],
"clic": [17,30,12,2,[1,20],[13,27],[9,16,25,26],[14,15,23,28]],
"especificando": [17],
"prof": [26],
"camproj": [14],
"empreguen": [[3,29]],
"arranxar": [9],
"produto": [[10,14]],
"fiábei": [29],
"dmicrosoft.api.client_id": [17],
"especializada": [30],
"validación": [15,32,30,21,[17,20],31,[8,12,14]],
"diríxas": [9],
"identificador": [17],
"erro": [15,9,[17,29],30,[0,10,18],[13,21,32],[4,14,20,23,28,31]],
"renome": [[21,29]],
"colorean": [18],
"premendo": [18,[20,30],[1,14,25,28]],
"dan": [6],
"dar": [15],
"conseguirs": [[17,21]],
"inclú": [[12,17,29],13,[0,7,19,24,25]],
"das": [32,30,12,29,20,[15,26],8,[13,14,23,31],[1,3],[10,17,25]],
"intro": [20],
"system-user-nam": [12],
"format": [[14,15]],
"wolof": [6],
"particular": [[7,21],[12,15,17,27,29]],
"fácil": [15,14,[10,18,22,30]],
"console.println": [1],
"cambiar": [[8,30],[14,22,23],[12,20],17,[13,21,25,26,32]],
"recargou": [16],
"requiran": [20],
"cambian": [15],
"cópiea": [[15,29]],
"cambien": [[14,26]],
"croata": [6],
"corso": [6],
"reducirá": [29],
"conéctans": [2],
"part": [30,20,[13,19],[9,15,17,26,27,29],[8,10,12,18,21,28]],
"amósas": [[12,30]],
"rexistrar": [8],
"amplo": [10],
"adecuada": [14],
"computador": [17,22,2,10,[12,20,21,31]],
"pare": [15,[12,19],32,29,21],
"ignórans": [15],
"leender": [7],
"para": [17,30,29,20,[12,26],2,19,14,15,[13,18,23],10,[8,21],25,27,[1,32],[7,16,28],[9,22],[24,31],[3,11],0,[5,6]],
"cópieo": [29],
"cargará": [29],
"adecuado": [27,2],
"teñen": [30,[2,12,25],[1,5,11,14,16,20,28]],
"apoiar": [9],
"bloqu": [24,26,31],
"ltr": [14,32],
"consoant": [24],
"optionsexttmxmenuitem": [8],
"ltz": [6],
"analic": [10],
"tema": [29,10],
"lub": [6],
"tonga": [6],
"marketplac": [17],
"lug": [6],
"subsecuent": [29],
"cambiou": [2],
"dea": [17],
"entries.s": [1],
"invariábel": [28],
"del": [[5,17,29,30]],
"desdobrábel": [[21,23,27]],
"reescribir": [12],
"gotonextuntranslatedmenuitem": [8],
"targetlocal": [12],
"gardada": [[13,29]],
"deu": [6],
"record": [[9,15]],
"interferir": [2],
"edición": [30,20,32,12,28,[25,31],[10,18],[5,16]],
"abranguendo": [22],
"relativo": [29],
"allsegments.tmx": [17],
"especi": [12,[1,4,5,14,25]],
"grati": [17],
"méxico": [27],
"helpcontentsmenuitem": [8],
"paso": [2,[9,26,29]],
"contrasin": [2,20],
"gardado": [[21,29],[2,8,12,17,20]],
"exactament": [[12,27,28]],
"descrita": [[8,17]],
"dous": [2,17,[10,26],[15,27,30],[1,14,21,28]],
"eliminada": [[15,20,23]],
"coincidencia": [30,32,20,29,8,28,31,[12,15],[3,13,16,23,24,26],[5,14,18,21,25]],
"uig": [6],
"eliminado": [15],
"outro": [32,20,30,[13,14,15],[21,29,31],17,[2,10,27,28],[3,4,6,9,11,16,18,25]],
"ortográfico": [27,31,[10,13,32]],
"term": [16,19],
"dotx": [14],
"instrución": [17,[10,11,12,21,31]],
"sobrescribiras": [28],
"ortográfica": [27,[20,32],0,[8,10,19,21,31]],
"duden": [30],
"insir": [20,[5,23]],
"panxabiano": [6],
"revisar": [29],
"actualizala": [17],
"existen": [[15,23,29]],
"spotlight": [17],
"tswana": [6],
"catlik": [3],
"murray": [[4,7]],
"seguint": [[20,24],8,[2,12],17,30,[13,28,29],25,[1,18,26],[10,11,14,19,21,22,23],[5,16,27]],
"empregan": [19,[8,10,12,27]],
"dir": [17],
"latex": [14],
"abrindo": [30],
"submenú": [[1,17]],
"empregar": [17,29,27,12,[2,16,31],[20,30,32],[8,19,21],[14,23],[1,10,15,26,28]],
"actualizado": [2,[16,32]],
"aceptábel": [[15,19]],
"div": [[6,12]],
"mainmanushortcuts.properti": [8],
"alfabeticament": [29],
"legal": [[7,32],[4,8,10,31]],
"viewfilelistmenuitem": [8],
"ukr": [6],
"empregad": [14],
"limpando": [28],
"gratuíta": [[2,17]],
"baleiro": [2,13,[28,29,30],[12,16,17,25,26]],
"test": [17,21],
"omegat": [17,32,[13,29],21,[10,31],30,[2,18],[9,14],20,12,[8,22],[15,28],[19,23],[1,25,27],[3,7,16],[4,26],0,[5,11,24]],
"baleira": [[12,26],28,[8,29,32]],
"imprim": [1],
"gratuíto": [2],
"kanuri": [6],
"final": [[18,25],[4,8,10,12,13,15,17,19,21,24,26]],
"requirir": [14],
"cargando": [2],
"proxecto": [29,20,32,2,13,30,23,18,[17,21],31,12,[8,16],[10,25,27],9,[1,3,26],[11,14],[0,5,19,28]],
"finai": [[15,23,29]],
"dispoñíbel": [17,2,21,[0,8,10,19,20,22,25,26,27,30]],
"virtual": [1],
"quebra": [26,[12,32]],
"widnow": [17],
"console-align": [17,21],
"coñec": [10],
"subcartafol": [13,32,29,[2,18],[10,23],[17,21,30],[11,27,28,31]],
"ms-dos": [17],
"desexa": [17,29,12,[9,19,22,30]],
"jean-christoph": [[4,7]],
"terá": [20,[17,19,25,26,29]],
"algún": [17,21,26,[15,29,30],[13,16],[0,11,14,18,22,23,31,32]],
"restaura": [20,30],
"feroé": [6],
"coincid": [24,12,[25,27],[26,30]],
"henri": [7],
"custom": [9],
"recoñecerans": [10],
"intraducíbei": [12],
"una": [[23,24,25,26,28,30]],
"engadindo": [[12,13]],
"und": [27],
"project_save.tmx.temporari": [[21,29]],
"grand": [[19,27],26],
"comprob": [19,[11,14,17],[2,27],[6,15,21,29]],
"opciónsvm": [17],
"kikuyu": [6],
"estabeleceu": [28],
"quechua": [6],
"interpret": [22],
"editoverwritemachinetranslationmenuitem": [8],
"estabelecen": [12],
"ingreek": [24],
"seguro": [[15,22,23,26,29]],
"editando": [[17,28]],
"aloxado": [[2,13,19]],
"es_es.aff": [27],
"vietnamita": [6],
"convert": [[1,28],[14,22,32]],
"ignor": [12,[13,27]],
"recoñecida": [[16,26]],
"necesit": [29,[17,25,26,30]],
"aproximación": [30],
"haber": [30,[11,20,26]],
"pojavnem": [16],
"implementación": [17],
"projectexitmenuitem": [8],
"inconsistencia": [4,[20,30]],
"diálogo": [32,12,[18,20],31,[23,30],[16,21],[1,2,22,27,28,29],26],
"desactivará": [20],
"integridad": [13],
"destácans": [15],
"preme": [5,[15,16,28,30]],
"provista": [30],
"punto": [26,24,[17,30],[1,9,13,14,28]],
"prema": [18,[2,25],[17,30],[12,15,16,19,20,29]],
"provisto": [30],
"útil": [[20,29],[7,10,12,17,23],[3,13,18,26,28,30]],
"aliñar": [[14,21]],
"especialment": [23,[3,21,26]],
"maco": [17],
"perdeu": [21],
"perder": [29],
"doc": [[14,30],13],
"consello": [32,[14,15,23,27,31],[24,30]],
"serven": [29,28],
"doi": [29],
"dos": [12,30,10,20,[2,17],[22,32],[14,19,25,29],[26,28],[8,16,27,31],[11,13,18,21,23],[1,4,15,24]],
"incluso": [[12,13,16,23,29,30]],
"mac": [17,5,8,[13,20],[2,10,14,18,31,32]],
"eliminala": [18],
"compoñent": [20],
"preprocesándoo": [14],
"mai": [29,[5,24],[15,16,17,25],[2,7,13,18,19,21],[0,1,4,10,14,20,23,26,27,28,30]],
"mah": [6],
"gaélico": [6],
"asociando": [14],
"adaptando": [26],
"lepša": [3],
"bosníaco": [6],
"numeración": [15,32],
"mal": [23,[6,15,20,27,30]],
"man": [[17,18,23]],
"especifica": [17,[2,19,26]],
"lepši": [3],
"banda": [26],
"mar": [6],
"may": [25],
"anteriorment": [17,[11,13,14,23,29]],
"urd": [6],
"personalizábei": [12],
"descargar": [32,[2,11],[8,13,17,27,31]],
"url": [2,[12,20,27,30]],
"especificament": [17],
"desaparecerán": [27],
"xeralment": [24,[15,23,30]],
"megabyt": [17],
"uppercasemenuitem": [8],
"viewmarkuntranslatedsegmentscheckboxmenuitem": [8],
"aconsellábel": [26],
"akán": [6],
"cláusula": [18],
"acaba": [2],
"etiquetado": [[29,32]],
"actualizada": [[2,6,10,14,19,29]],
"www.omegat.org": [9],
"usa": [[10,26]],
"válido": [[17,26],[19,21,23]],
"use": [30,18,[13,20]],
"comprobarán": [20,17],
"usd": [19],
"recomendábel": [2],
"engaden": [17],
"programador": [[1,20]],
"xestionars": [2],
"uso": [31,[19,32],[0,1,3,11,17,24],[2,10,16,20,26,27,30]],
"recomendámosll": [2],
"omegat.jar": [13,[1,21,29]],
"válida": [[17,23]],
"ofrecen": [[2,22]],
"usr": [17],
"logo": [13],
"eliminars": [15],
"intensiva": [30],
"ofreceu": [2],
"lista": [12,32,[6,30],[13,18,19,26,29,31],[0,1,5,10,14,16,17,20,21,22,24,25,27,28]],
"libro": [19],
"pseudotraducida": [29,[31,32]],
"créao": [23],
"utf": [22,16],
"comiña": [16],
"francesa": [0],
"nume": [16],
"adición": [[2,17,26]],
"aniñous": [15],
"servic": [19,2],
"borrar": [5],
"descrito": [[11,17,23]],
"necesitarán": [14],
"dsl": [11],
"derivado": [20],
"servir": [[10,16,29]],
"aparecerán": [12,[25,26,27]],
"n.n_windows_without_jre.zip": [17],
"ofreza": [16],
"resumirs": [2],
"nome_do_proxecto-omegat.tmx": [29],
"dtd": [14],
"desfai": [20],
"bald": [19],
"mes": [[21,29],17],
"meu": [4],
"declinación": [16],
"desfac": [20,8],
"recargar": [[8,20,29]],
"projectcompilemenuitem": [8],
"console-transl": [[17,21]],
"conseguir": [[9,26,28,29]],
"distribución": [17,7,13],
"conform": [[2,16,29]],
"materia": [[2,29]],
"masculino": [3],
"dun": [29,[2,13,15,20,24,26],[1,14,17,19,23,30],[0,10,21]],
"explícita": [[4,12]],
"wordart": [12],
"princip": [32,30,[20,31],29,[8,10,12,21,26],[1,13,17]],
"optionsviewoptionsmenuitem": [8],
"inform": [9,13],
"depend": [[13,14,19]],
"commit": [2],
"eliminará": [15],
"engadir": [17,12,2,30,[8,13,16,18],[1,9,14,19,20,26,29,32]],
"targetlocalelcid": [12],
"máis": [17,30,24,29,13,21,[14,19,23],[5,15,16,18,25,26],[2,10,12,27,28],[7,20,22],[1,6,8,9,31]],
"project_stats_match.txt": [[2,13,30]],
"valón": [6],
"lanzador": [17],
"dvd": [21],
"quarkxpress": [14],
"osetio": [6],
"meniju": [16],
"propia": [[17,24,26,30]],
"subscrición": [13],
"executa": [17,[13,22]],
"avanzado": [[10,17,21,24,29]],
"avanzada": [25,[15,32]],
"mala": [15],
"krunner": [17],
"controlar": [17],
"libreoffic": [27,18],
"prerrequisito": [21,17],
"escoll": [29],
"propiedad": [32,[13,23],20,[12,31],[21,29],[10,16,17,27,30],[3,8,11,26]],
"adapta": [26],
"progresa": [13],
"propio": [29,[2,10,28],[15,18,22,24,30]],
"común": [2,[16,29],[10,22,23,25,30,31,32]],
"gardará": [17,[2,21,23]],
"volver": [20,30,18,[8,12,15,29]],
"industri": [30],
"romané": [6],
"uzb": [6],
"constituínt": [24],
"exacta": [25,30,20,16,8],
"glosario.txt": [16],
"toman": [25],
"asegurará": [12],
"manx": [6],
"devolv": [17],
"texto": [14,[20,32],[12,22,30],28,[15,29],26,10,[16,19],[25,27],[18,23,24,31],[1,2,5,21],[3,13,17]],
"viewdisplaysegmentsourcecheckboxmenuitem": [8],
"permanec": [14],
"progreso": [30,[2,21]],
"acortar": [24],
"open": [25,[12,14,15],29],
"aliment": [17],
"www.oracle.com": [17],
"cargarán": [29],
"mkd": [6],
"project": [2,23,13],
"serán": [12,[17,29],[10,13,20,23,26]],
"xmx1024m": [17],
"entend": [29,[10,13]],
"afortunado": [30],
"ioruba": [6],
"único": [30,20,[8,23,25]],
"dzo": [6],
"arquivo": [13],
"pegar": [20,30,[5,31,32]],
"penalty-xxx": [29,32],
"permítell": [20,[12,29],[1,13,16,30]],
"gotonextsegmentmenuitem": [8],
"única": [[15,19,30]],
"mlg": [6],
"nnn.nnn.nnn.nnn": [17],
"descargou": [17],
"guía": [31,10,[9,17]],
"actualizará": [[2,29]],
"mlt": [6],
"abort": [[17,21]],
"precaución": [21],
"ficheiro": [12,32,29,17,13,14,30,20,22,18,16,21,27,23,2,[8,10,31],15,[25,28],11,26,1,9,[0,19]],
"segmentación": [26,23,32,10,12,30,[13,14,15,20,24,29],31,[8,25]],
"internet": [[2,30],[10,13,25,27]],
"lonx": [29],
"saltar": [30],
"residen": [2],
"explicitando": [9],
"printf": [15,20],
"interferirá": [17,23],
"interest": [19],
"cálculo": [30,[1,16]],
"gardara": [29],
"amosa": [30,20,25,[10,12,15,17,19,21,22,24]],
"escocé": [6],
"aparec": [[17,25],[8,19,20,23,27,29,30]],
"externo": [20,2],
"estabelecida": [12],
"debuxo": [12],
"repetición": [30,[10,20]],
"externa": [30,[8,13,14,20]],
"es-mx": [27],
"documentación": [7,[4,8,32],[10,12,13,24,30,31]],
"multinacion": [29],
"desprázao": [20],
"realizará": [[2,20,25]],
"bash": [13],
"manualment": [[16,27,29],[14,19,23,28]],
"considerado": [30],
"emerxent": [30,[20,28]],
"mark": [4],
"base": [3,[16,19,29,30]],
"stem": [3],
"realizar": [2,[14,17,20,25]],
"maldivio": [6],
"moi": [[10,22,25,26],[2,13,16,19,20,29,30]],
"lote": [17],
"mon": [6],
"escriba": [17,[2,13,18]],
"estabelecido": [29],
"facendo": [30,13,[1,27],[14,16,17,20,25]],
"pouco": [[17,26]],
"cortar": [20,[5,30,31]],
"vai": [15,[3,10,13,14,17,29,30]],
"indica": [2,[29,30]],
"internacion": [16],
"van": [17,[12,15]],
"inserir": [20,8,[14,28,32],[5,15,16],[12,23,24,25,26]],
"gedit": [16],
"manipulación": [15],
"nesta": [[4,17,29]],
"word": [14,[12,18,25]],
"dispoñíbei": [20,[17,30],[8,27],[12,19,29],[1,10,21,24,26]],
"xenitivo": [3],
"lingua": [19,[26,29,32],14,[3,17,21],[2,20,27],[0,4,13,16,22,23,30,31]],
"desactivada": [20],
"conexión": [[2,10,27]],
"recargalo": [26],
"luxemburgué": [6],
"servidor": [2,17,32,12],
"europa": [22,32],
"reabrir": [12],
"sendo": [[1,13,29,30]],
"mri": [6],
"vcs": [2],
"chamorro": [6],
"lingvo": [11],
"pouca": [18,31],
"cingalé": [6],
"zhuang": [6],
"exista": [13],
"canaré": [6],
"numeraba": [29],
"movido": [20],
"entrada": [16,25,20,31,[8,29,30],[1,2,12,15,17,19,24,28,32]],
"repág": [5],
"tortoisegit": [2],
"preparada": [2],
"preparado": [[2,10,30]],
"ligazón": [30,[15,17],[11,12,13,29]],
"msa": [6],
"incluída": [[12,13,17]],
"asegurars": [13,15],
"atopará": [17,[0,25,30]],
"n.n_sourc": [17],
"estilística": [26],
"incluído": [30,[0,3,13]],
"pt_pt.aff": [27],
"resumida": [30],
"tomar": [[13,30],[10,21,27,29]],
"html": [17,12,14,26,[1,13,15,18,21,29,30]],
"graza": [4,31],
"directorio-do-meu-proxecto": [21],
"ven": [6],
"estilístico": [10],
"ver": [20,8,32,[18,31],[10,15,17,25,26,29,30],[0,2,9,12,14,16,19,23,28]],
"redimensionalo": [30],
"omegat.bat": [13],
"deteña": [28],
"vez": [29,[17,20],2,[19,30],[13,18,24],[8,10,14,21,22,23,27,28]],
"artund": [27],
"captura": [9],
"vocabulario": [27,16],
"outra": [32,[17,30],20,[12,15,21,29,31],[0,10,24,26,27]],
"flexionada": [16],
"comprobas": [30],
"esta": [17,20,12,29,[15,28],[10,13,30],[14,19,23,26],[1,5,18,22,24,25,27]],
"garantindo": [10],
"preguntar": [30],
"comprobar": [15,29,27,[2,17,18,20,25,26,30]],
"velocidad": [19],
"aquela": [25],
"almacenar": [10,27],
"suprimir": [5,28],
"sawuła": [7],
"jres": [17],
"www.ibm.com": [17],
"achegar": [9],
"cabezallo": [12],
"renoméea": [[21,29]],
"dirix": [13],
"atopast": [4],
"seleccionado": [20,29,12,[17,27,30],[18,19]],
"disposición": [14,2],
"seleccionada": [20,[12,30],[1,21]],
"recib": [15],
"axudar": [[9,10,29]],
"grave": [23],
"financeirament": [9],
"poder": [[17,19],[2,13,26,27]],
"existent": [29,17,[1,16],[20,23,26],[13,14,18,19,28,31,32]],
"command": [20,[8,18,30]],
"poden": [29,10,[13,14],[2,16,30],17,23,[1,8,12,18,20,21,25,26,28]],
"sería": [17,[4,21,29]],
"atopars": [16],
"n.n_without_jr": [17],
"baleirará": [20],
"consegu": [26],
"existir": [[1,2,14,16]],
"poña": [[15,17],0],
"personalizar": [32,[1,5,12,15,20,30]],
"documento.xx.docx": [12],
"retir": [14],
"calidad": [[19,29],14,3],
"viewmarkbidicheckboxmenuitem": [8],
"subliñada": [[0,27]],
"branco": [[12,24],[8,14,16,26,28]],
"operación": [[15,32],2,[13,29,30,31]],
"fixándos": [25],
"especificación": [12],
"mazá": [20],
"idioma": [29,[17,27],32,[12,26],[6,14,22,23],[0,21],[10,19,30],[18,31],[1,5,13,15,16,25]],
"continuará": [21,17],
"dicionario": [27,32,11,30,31,10,[2,13],[16,20,24]],
"dividida": [[26,30]],
"toda": [25,29,[20,30],[12,15,19],[1,2,3,4,5,13,14,17,22,23,27]],
"recibiu": [30],
"vie": [6],
"subliñado": [16],
"fileshortpath": [30],
"compatibilidad": [1],
"recibir": [17],
"reutilizar": [29,32,31],
"permiso": [17],
"viu": [9],
"colabora": [2],
"posíbei": [[10,29],[2,8,14,16,17,20,28]],
"chuvach": [6],
"verificar": [16],
"asumindo": [14],
"version": [17],
"project-dir": [[17,21]],
"explican": [10],
"está": [17,[13,29],30,[12,19],[2,20,21],[10,14],[0,8,22,26],[5,7,15,16,18,23,24,27]],
"mya": [6],
"posíbel": [14,[15,23,24],[4,13,16,19,28,29,30]],
"de-fr": [29],
"vista": [14,[2,13,30]],
"atoparía": [[17,21]],
"urdú": [6],
"projecteditmenuitem": [8],
"britannica": [[11,32]],
"configurar": [20,12,[18,23,32],[15,21,26,27,30,31]],
"omitiras": [12],
"enriqu": [7],
"cambiará": [23],
"forza": [1],
"pasado": [29],
"duplicación": [15,32],
"minimiza": [30],
"contén": [13,30,29,[15,16,20],[1,9,10,17,21,23,31]],
"superlativo": [3],
"apertura": [15,10],
"modificará": [17],
"sintáctica": [20],
"detectar": [0,[15,29]],
"todo": [17,[29,30],20,[4,25],[8,15],[1,13,18],[2,3,5,9,19,21,22,23,24,26,28,31,32]],
"iceni": [14],
"fornec": [[19,25]],
"dividido": [[16,30]],
"definición": [8,[2,30]],
"riba": [26],
"faino": [13],
"tradicionai": [17],
"paxtún": [6],
"estrutura": [13,[26,29],32],
"estrutur": [19],
"ela": [15,[12,30]],
"ele": [[12,17,24,29,30]],
"recoñecendo": [3],
"segmentado": [12,29],
"decimai": [28],
"ell": [6],
"turco": [6],
"cambiars": [14,16],
"x0b": [24],
"visor": [[21,29],[16,20,30]],
"funcionalidad": [[1,2,26]],
"acostum": [30],
"http": [17,1,19],
"estrita": [15],
"léame": [[12,17]],
"detall": [[7,14,30],[4,17,19,20]],
"avisará": [15],
"luganda": [6],
"significa": [17,[5,14,29]],
"basicament": [[14,22]],
"occident": [[6,22,32]],
"xeorxiano": [6],
"vol": [6],
"cabeceira": [12],
"estrito": [29],
"softwar": [7,0,[1,2,17],[10,21,29]],
"projectsinglecompilemenuitem": [8],
"insira": [[20,28],17,2,25],
"ignorado": [17,[5,8]],
"de-en": [29],
"docbook": [[4,14,15]],
"helton": [4],
"abrir": [30,[8,17],20,[18,23],[2,5,13,22,32]],
"eng": [6],
"concederá": [13],
"iniciar": [17,13,21,12,[0,1,3,15,19,31,32]],
"aproximadament": [21],
"destacan": [30],
"destacar": [14],
"xeración": [[19,21]],
"adicionai": [13,[1,10,12,21,24,25,30]],
"puido": [[29,30]],
"compartido": [2],
"okapi": [32],
"creación": [[2,3,8,12,13,16,30]],
"numer": [14],
"minúscula": [20,[8,24,25],[17,30,32]],
"aínda": [29,23,[14,27],[2,4,15,17,20,26]],
"padrón": [12,26,32,[15,24],[22,29]],
"procesarán": [17,[12,29]],
"copyright": [7],
"moran": [7],
"coreano": [[6,20]],
"project_nam": [13],
"system-os-nam": [12],
"dereito": [2,[17,30],[7,16,20],[1,27,28]],
"pagar": [2],
"optionstabadvancecheckboxmenuitem": [8],
"perfectament": [29],
"solucionar": [29,15],
"optionsviewoptionsmenuloginitem": [8],
"dereita": [[14,32],5,30,[17,20,26,31]],
"similar": [30,2,[10,17,29],[12,18,23,25,28]],
"nas": [25,[12,15,19,21,26,29,30],[4,10,13,14,16,17,20,24,28]],
"nav": [6],
"alerta": [24],
"nau": [6],
"tar.bz2": [11],
"epo": [6],
"restaurar": [30,[8,20,21,32]],
"combinación": [8,5,[11,12,14,17,20,28]],
"canadá": [17],
"x64": [17],
"nbl": [6],
"busqu": [[11,24]],
"influencia": [14],
"tabulación": [16,26,24],
"isn\'t": [24],
"interesado": [[13,18]],
"valid": [14],
"modificalo": [7,14],
"interfac": [17,21,30,[10,32],[9,13,14,20,31]],
"navegu": [[17,27,29]],
"era": [14,[29,30]],
"manipulará": [18],
"abren": [[14,30]],
"nela": [[18,20,25,30]],
"optionsteammenuitem": [8],
"estea": [[2,10],[8,17,18,20,23]],
"incrementalment": [15],
"decidir": [[2,15,28]],
"gzip": [29],
"estará": [[2,17,25,29]],
"nde": [6],
"citación": [[24,31]],
"esa": [[19,27,28]],
"esc": [30,20],
"ese": [26,[14,17,20]],
"x86": [17],
"ndo": [6],
"logic": [25],
"nostemscor": [30],
"moita": [[10,18],[0,4,12,19,25,26,28]],
"est": [17,13,29,10,12,[14,15,21,30],16,[2,20,22,23,24,26,27,28],[4,5,6,19,31]],
"grupo": [15,32,[9,13],31,30,[18,21,24,29]],
"engadida": [20],
"actualizarán": [[12,13,16,29]],
"fijiano": [6],
"console-createpseudotranslatetmx": [17],
"modificars": [28],
"aloxamento": [2],
"nel": [[2,13,17],18],
"etc": [[14,29],[15,26],[2,16,17,20,23,25],[0,3,11,12,13,24,30]],
"engada": [[2,8,13,16,17,22,26]],
"longman": [[11,32]],
"nep": [6],
"fuzzyflag": [30],
"xanela": [32,30,20,31,17,25,[2,15,21],27,[12,23,29],[3,8,13,14,19,26]],
"engadido": [1,0,32,[3,31],2,[9,10,16,20,30]],
"merriam": [[11,30,31,32]],
"escap": [24],
"compons": [30],
"gardaras": [14,[12,23]],
"procesar": [17,29],
"cobizoso": [24,31],
"secundaria": [29,30],
"guión": [17],
"escribir": [[16,17,21,26]],
"intervalo": [[14,20,21]],
"posibilidad": [29,[2,10,17,23]],
"comportan": [[21,26]],
"eus": [6],
"project_sav": [30],
"premer": [[20,30],[16,25]],
"respectivo": [14],
"portapapei": [20],
"substitú": [20],
"interpretado": [22],
"máquina": [[1,17,21]],
"forma": [29,[12,13,16],[3,17,30],[2,8,26]],
"n.n_without_jre.zip": [17],
"xerada": [[13,19,29]],
"conflitivo": [[2,9]],
"devolverao": [23],
"navegación": [[17,25]],
"igbo": [6],
"abran": [14],
"medio": [23,[13,16,21,24]],
"interlingua": [6],
"ndebel": [6],
"magento": [14],
"vxd": [14],
"omitir": [12],
"xerado": [20],
"intenta": [12],
"necesitar": [24,[17,23,29]],
"estar": [[2,15,16,19,26],[8,12,22,23,29,30]],
"ewe": [6],
"necesitan": [[2,9,24,29]],
"moito": [14,[12,13,21,27,29]],
"navegando": [30],
"martin": [7],
"preferencia": [[17,32],13,[5,8,10,30,31]],
"traduciron": [29,[19,30]],
"xslt": [1],
"abortará": [21],
"carpeta": [1],
"segmentara": [26],
"shift": [[8,20],5,14,18,16,[28,32]],
"principiant": [[10,18]],
"refiren": [30],
"wunderlich": [7],
"nin": [[15,19,29],[14,17]],
"java": [17,13,[1,21],[8,32],[15,24,31],[12,14,20,29]],
"exe": [17],
"separábei": [20,8],
"comparación": [[15,30]],
"project_save.tmx": [29,21,[13,14],[2,20]],
"dictionari": [11,13,[2,31]],
"adaptars": [10],
"neutro": [3],
"agregar": [[12,30]],
"modelo": [[1,19],30],
"cero": [[15,24],25],
"usualment": [13],
"seleccionando": [[12,30],[13,17,22,26]],
"intentar": [[20,28]],
"intentan": [15],
"marcar": [[8,20],26,[2,28]],
"flash": [14],
"pasará": [29],
"appl": [20,[17,18]],
"traduciran": [17],
"modificado": [20],
"houber": [0],
"amosará": [20,25,[2,16,17,22,29,30]],
"diferenciala": [15],
"xestionado": [12],
"parámetro": [17,21,14,[4,13,19,22,29,32]],
"deberá": [26,21],
"dinamarqué": [6],
"atoparemo": [25],
"modificada": [[10,20]],
"timestamp": [12],
"gravaron": [29],
"propenso": [10],
"continuament": [29],
"nld": [6],
"gran": [24],
"segmentarán": [26],
"nest": [29,17,13,[10,15],[2,9,12,16,20,21,22,27,28,30]],
"cambiada": [23],
"espera": [17],
"engadila": [17,[21,27,29]],
"cambiado": [29],
"persa": [6],
"dando": [24],
"omgat": [13],
"plugin": [[0,1,3],13],
"aplicarán": [26],
"nesa": [25],
"merecen": [2],
"omeuproxecto": [29],
"omegat-l10n-request": [9],
"nese": [[1,10,11,23]],
"limitado": [[2,12]],
"eslavo": [6],
"orixinai": [25,[18,19,29]],
"improbábei": [25],
"absoluto": [29],
"estilo": [14,[0,1,2,15,29]],
"engadilo": [2],
"segmentará": [[14,26]],
"editinsertsourcemenuitem": [8],
"representan": [[15,30]],
"documento": [14,20,[15,29],[1,18],[12,30],[5,23],[8,10,32],[13,16,28,31],[2,7,9,17,26]],
"microsoft": [[12,14],[17,32],[10,16,18,30]],
"recoñecerán": [16],
"renomealo": [27],
"projectnewmenuitem": [8],
"coñecido": [1],
"optionstranstipsenablemenuitem": [8],
"inglés-catalán": [19],
"ordinario": [13],
"segment": [15,30],
"changes.txt": [13],
"estabelezan": [8],
"renomeala": [29],
"glossari": [16,13,[2,18,32]],
"ignored_words.txt": [[2,13]],
"grego": [24,6],
"supera": [28],
"holandé": [29],
"utilic": [29],
"nno": [6],
"next": [[0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30,31]],
"nob": [6],
"import": [14],
"color": [20],
"caxemiri": [6],
"compañeiro": [30],
"dividirs": [26],
"non": [20,17,16,29,23,[15,30],[12,21],19,24,[14,25],[26,28],[8,27],[5,18],[0,4],[1,2,13,22],10,[3,9,11,31]],
"prioridad": [26,32,[20,31]],
"nor": [6],
"pantalla": [9],
"not": [25],
"nos": [[14,29],25,[20,30],[17,32],[2,15,18],[0,3,8,10,12,19,23,24,26,28]],
"central": [[6,22,32]],
"emparellada": [[15,30]],
"ascii": [14],
"produtividad": [26],
"publica": [7],
"índice": [10,12],
"deliberado": [15],
"selection.txt": [28,20],
"xhtml": [12,[14,15,26,30]],
"resultar": [15],
"progresivament": [13],
"descomprima": [1],
"window": [17,13,[2,32],20,[10,11,14,16,21,24,31]],
"xestor": [[2,13,27]],
"gramat": [0],
"decida": [23,20],
"disable-project-lock": [17],
"previr": [[21,32],31],
"omegat.pref": [13],
"previo": [[5,26]],
"fai": [[17,19,21,30],[10,12,14,25,26,29]],
"inupiaq": [6],
"fan": [17,2],
"fao": [6],
"txml": [14],
"interpretará": [22],
"desempaquetalo": [17],
"fas": [6],
"previa": [[8,10]],
"clásica": [1],
"ambivalent": [12],
"bartko": [7],
"dispoñ": [19],
"omegat-script": [1],
"utiliza": [29,26],
"quirguiz": [6],
"candidata": [29],
"pt_pt.dic": [27],
"restrinxirá": [25],
"criterio": [25,7],
"bastant": [10],
"autopropagación": [23,32],
"italiano": [6],
"usuario": [17,13,[20,30,32],21,31,19,[9,10],23,[2,28,29],[8,12],18,[7,14,15,24]],
"xerarquía": [13],
"level1": [[18,29],20],
"amosamo": [2],
"level2": [29,18,20],
"automaticament": [17,[12,16,18,22,27,28,30],[0,2,3,10,13,14,20,21,23,25]],
"widget": [[30,31,32]],
"engadirs": [16],
"determinar": [27],
"útile": [[10,24],[18,29]],
"web": [17,9,[19,32],[1,2,13,14,18,30,31]],
"avéstico": [6],
"éuscaro": [6],
"remisión": [2],
"actualización": [[17,20]],
"compartirs": [18],
"confuso": [29],
"berlin": [7],
"enchendo": [29],
"identificábei": [29],
"aliñará": [17],
"editselectfuzzy4menuitem": [8],
"editregisteridenticalmenuitem": [8],
"gris": [20,18],
"vece": [24,12,[15,18,25,30],[1,10,13,14,19,27,29]],
"malaio": [6],
"indivisíbel": [24],
"dividilo": [30],
"usar": [18,[10,17,30],[4,7,19,25,29,31]],
"duplicars": [15],
"pt_br.dic": [27],
"usan": [10],
"omitirá": [12],
"crearans": [13],
"carácter": [24,5,26,12,[15,16,25,28,31]],
"estado": [30,20,[13,21],[4,12,14,17,23,29]],
"unabridg": [11],
"nun": [[17,30],[2,29],13,[14,15,16],[1,10,18,21,25],[11,22,23,28]],
"contabilidad": [30],
"section": [20],
"habela": [12],
"afrikaan": [6],
"moldavo": [6],
"pesar": [26],
"regularment": [29],
"orient": [[22,32]],
"abano": [27],
"producen": [29],
"rastreador": [4],
"petición": [[9,19]],
"darán": [12],
"fulah": [6],
"nnnn": [30,17],
"prever": [13],
"preferíbel": [26],
"crearas": [13],
"engadirá": [[16,20]],
"deformatado": [19],
"project_save.tmx.aaaammddhhnn.bak": [[21,29]],
"renomears": [29],
"correspóndes": [13],
"irlandé": [6],
"supr": [30],
"br.aff": [27],
"zh_cn.tmx": [29],
"escol": [[1,29]],
"wordfast": [14],
"suposto": [29,[19,22,27,30]],
"probar": [24],
"huriaux": [7],
"wix": [14],
"copiado": [[2,12,20]],
"paquet": [17,13,[1,2,14]],
"romanesa": [0],
"txt2": [22],
"fulano": [29],
"mantén": [2,[13,20,23]],
"visio": [14],
"nya": [6],
"txt1": [22],
"archiv": [17],
"dúas": [17,30,[2,13,15,16,21,23,26,28,29]],
"visit": [9],
"user": [13],
"estaba": [5,26],
"traduzan": [29],
"correspondan": [15],
"proxi": [17,[20,32],8],
"sitio": [9,[2,19],[1,17,18,31,32]],
"extraído": [17,13],
"extens": [12,1],
"perda": [21,[13,32],[10,23,31]],
"escapada": [17],
"fij": [6],
"fin": [5,[6,24,26]],
"hausa": [6],
"b0": [15],
"b1": [15],
"b2": [15],
"identificars": [19],
"permiten": [30,28],
"naruano": [6],
"armenio": [6],
"aa": [6],
"ab": [6],
"alfabeto": [[21,22]],
"desenvolv": [24],
"ae": [6],
"af": [6],
"declaración": [12],
"posterior": [7,[2,15,29]],
"ak": [6],
"diff": [30],
"am": [6],
"an": [24,6],
"editmultiplealtern": [8],
"ao": [17,30,29,20,2,18,[13,15],[9,21],[12,16,19,28],[1,14,22,23,25],[5,8,10,26],[24,27],[3,32]],
"extraída": [10],
"ar": [6],
"as": [15,30,20,17,29,[10,12],26,25,13,14,23,[2,16,18,19],[24,28],1,21,[3,8],[0,4,5,6,7,27],[9,11,22,31]],
"xurdir": [14],
"abreviado": [[1,2]],
"av": [6],
"ay": [6],
"wln": [6],
"az": [6],
"ba": [6],
"be": [[6,25]],
"simultaneament": [25,2],
"importas": [30],
"importar": [29,[2,20,30],[8,32]],
"bg": [6],
"bh": [6],
"bi": [6],
"inicialment": [13,[17,19,29]],
"filters.xml": [13,[2,23]],
"elaborar": [30],
"bm": [6],
"bn": [6],
"bo": [[4,26],[3,6,9]],
"anterior": [30,29,20,25,16,[0,8],[1,4,5,10,11,12,13,15,17,22,24,26,27]],
"br": [12,[6,17]],
"bs": [6],
"obrigatoria": [12],
"seguindo": [29],
"acepta": [30],
"vela": [[13,26]],
"samoano": [6],
"panei": [30,32,[14,20,31]],
"segmentation.conf": [[13,21],[17,23]],
"salto": [12,[5,14,15]],
"funcionará": [27,[0,17,21]],
"panel": [30,32,16,31,17,[1,18,19,29],[5,14,20]],
"iniciará": [17,[3,21]],
"ca": [[6,17,19,21,24]],
"cd": [17,21],
"resolvers": [2],
"ventá": [[2,25,28]],
"ce": [14,6],
"clave": [25,14],
"öäüqwß": [25],
"scrpting": [1],
"ch": [6],
"galé": [6],
"inglesa": [[0,4]],
"co": [17,[9,23,27,30,32],[5,12,18,20,25],[1,2,13,15,16,21,29],[4,6,11,14,19,26,28,31]],
"figur": [30,[13,16,27],[0,11,15,19,23,24,25,28,31]],
"cr": [6],
"cs": [6],
"traela": [20],
"corresponders": [27],
"grenlandé": [6],
"cu": [6],
"cv": [6],
"cx": [24],
"cy": [6],
"esperaba": [[9,20,29]],
"categoría": [[24,30],31],
"apach": [[2,27]],
"da": [[17,30],29,2,15,20,[5,21,25],[12,13],[10,19],[7,8,26],[9,32],[11,18,23,28,31],[1,3,4,6,14,16,24,27]],
"somalí": [6],
"adjustedscor": [30],
"font": [14,17,[20,32],21,[1,13,15,30,31]],
"diverxent": [19],
"dd": [[21,29]],
"de": [32,30,29,17,20,12,14,15,31,13,21,[23,26],10,8,19,2,25,[16,27],24,18,28,9,[1,22],0,5,3,4,7,6],
"duplicado": [[15,25,30]],
"identificará": [28],
"amosan": [16,30,[20,29]],
"fora": [5,15],
"separada": [30],
"chave-valor": [12],
"f0": [20],
"do": [30,20,[17,32],29,2,12,8,16,13,5,[14,23],[15,21,26,27],[18,31],10,25,[1,6,19],[3,28],[9,22],11,[4,24],0,7],
"f1": [20,30,[8,32]],
"pechala": [30],
"f2": [17],
"f3": [20,[8,32]],
"cumpran": [10],
"dr": [26],
"f5": [[8,20]],
"despoi": [26,17,15,[16,18],[2,5,12,13,14,20,21,23],[8,10,24,25,27]],
"visíbel": [[12,20,23]],
"incorporado": [27],
"dv": [6],
"uigur": [6],
"pintada": [15],
"wol": [6],
"permitir": [12,[2,21,23,28,29]],
"dz": [[6,11]],
"resposta": [19,9],
"editundomenuitem": [8],
"atopar": [17,[9,21],[10,13,24],[4,8,14,25,30]],
"ligalo": [17],
"separado": [12,16,[2,14,26,30]],
"ee": [6],
"which": [16],
"u000a": [24],
"selecciona": [20,17,[3,28]],
"pensado": [[10,24]],
"alemá": [0],
"el": [[2,18],[6,13]],
"visualsvn": [2],
"belazar": [19,32,31],
"en": [17,29,30,14,21,[2,12],26,[13,20],15,16,[1,10,22,23],[19,24],32,[25,27,31],[8,18],28,9,11,0,[3,5,7],4,6],
"eo": [6],
"aí": [17,[3,10,23]],
"es": [[6,19]],
"u000d": [24],
"et": [6],
"u000c": [24],
"eu": [[0,6]],
"carro": [24],
"verificación": [27,23,[0,12,20,26,28,32],[2,8,10,17,19,21,31]],
"probabelment": [[16,22]],
"duplicada": [24],
"mediant": [[2,30],[17,20],[12,16,32],[21,23,27,28,31]],
"activ": [[12,19]],
"fa": [6],
"tratalo": [29],
"personalización": [[8,32],31,[5,7,9,10,20]],
"gard": [14,[8,16,17]],
"ff": [6],
"foi": [15,4],
"stats.txt": [13],
"u001b": [24],
"fi": [6],
"fj": [6],
"for": [1],
"fo": [6],
"pensar": [[0,29]],
"fr": [17,21,[6,19,26,27]],
"content": [17,31],
"clase": [24,[1,31]],
"metad": [26,16],
"fy": [6],
"applescript": [17],
"divida": [29],
"ga": [[6,19]],
"conversión": [14,32],
"pequena": [[2,10,30]],
"gd": [6],
"class": [12],
"helplogmenuitem": [8],
"axeitalo": [15],
"rato": [0],
"copiala": [30],
"inuit": [6],
"resultado": [25,15,[12,24,29,30,31]],
"gl": [[6,30]],
"licenza": [[7,13],[11,17,20,29]],
"editoverwritetranslationmenuitem": [8],
"outputfilenam": [17],
"gn": [6],
"i0": [15],
"i2": [15],
"aeiou": [24],
"gu": [6],
"pequeno": [10,[5,14,26]],
"gv": [6],
"claro": [[20,29]],
"catalán": [6],
"arrastrando": [[17,30]],
"equipo": [2,[29,32],[8,31],[3,5,10,17,20]],
"rodean": [18],
"ha": [6],
"correspondent": [30,[0,3,12,13,14,15,17,21,24,26,27,28,29]],
"amosar": [[8,17],30,20,[14,16,22,25]],
"he": [6],
"inicialo": [17],
"hh": [[21,29]],
"hi": [6],
"duser.languag": [17],
"completo": [29,[5,10,12,19]],
"ho": [6],
"vers": [[13,29]],
"hr": [6],
"ht": [6],
"hu": [6],
"dependendo": [[17,30],20,[2,15,29]],
"enormement": [[14,15,30]],
"hy": [6],
"hz": [6],
"file-target-encod": [12],
"fra": [6],
"oci": [6],
"orixin": [[14,15],[20,30],12,[19,28,29],[8,25,32],[1,2,5,10,13,18,23]],
"verd": [[2,30],20],
"coinciden": [29,[16,30]],
"ia": [6],
"context": [30],
"briel": [[4,7]],
"id": [[6,30]],
"prefixo": [28,3],
"ie": [6],
"fri": [6],
"if": [1],
"estoniano": [6],
"project_stats.txt": [30,2],
"ig": [6],
"consecuencia": [23],
"ocr": [[20,23]],
"ii": [6],
"ik": [6],
"in": [1],
"io": [6],
"termin": [17,29],
"ip": [17,32],
"discusión": [13],
"index": [32,4],
"ir": [20,8,[30,31,32],28,[18,24,25]],
"traballo": [29,[10,17,30],13,[2,4,14,15,18,20,21,23]],
"is": [[2,6,16,24]],
"it": [6],
"iu": [6],
"odf": [14,[12,15,26]],
"ja": [[6,17,29]],
"propiament": [17,[13,14]],
"multiterm": [16,32,31],
"jc": [4],
"idoneidad": [7],
"linguax": [1],
"odp": [14],
"odt": [14,[20,23]],
"gotonexttranslatedmenuitem": [8],
"analiza": [[1,24,26]],
"librari": [13],
"jp": [22],
"asamé": [6],
"nplural": [12],
"aprendizax": [19],
"js": [1],
"jv": [6],
"montax": [19],
"learned_words.txt": [[2,13]],
"inserción": [[14,20]],
"comprimirs": [29],
"maxym": [7],
"ka": [6],
"kg": [6],
"robusta": [21],
"ki": [6],
"kj": [6],
"kk": [6],
"deberían": [26,20,[8,17,24]],
"amhárico": [6],
"kl": [6],
"km": [6],
"kn": [6],
"tamaño": [14,21,15],
"ko": [6],
"acced": [17,30,[10,20,21]],
"kr": [6],
"ks": [6],
"viewdisplaymodificationinfoallradiobuttonmenuitem": [8],
"ku": [6],
"kv": [6],
"kw": [6],
"táboa": [[8,14,26,30],[6,15]],
"ky": [6],
"completa": [30,[0,8,17,20,25]],
"la": [26,6],
"lb": [6],
"difiren": [17],
"desenvolvedor": [9,13],
"lg": [6],
"ful": [6],
"li": [6],
"dswing.aatext": [17],
"agora": [[2,15,29]],
"refac": [20,[8,30]],
"frecuent": [[9,10,17]],
"ln": [6],
"lo": [6],
"corresponden": [[2,29,30]],
"algunha": [29,[12,13,15,17,27],20],
"copiará": [[12,18]],
"enlac": [9],
"ls": [13],
"lt": [6],
"amósans": [[14,28]],
"lu": [24,6],
"dist": [17],
"lv": [6],
"refai": [20],
"ucraíno": [6],
"cycleswitchcasemenuitem": [8],
"cobr": [13],
"ma": [13],
"dada": [[26,29],[20,25]],
"mb": [17],
"tratará": [[25,29,30]],
"mg": [6],
"mh": [6],
"coincidan": [25,[16,20,24]],
"mi": [6],
"efecto": [[12,26],20],
"purament": [14],
"necesidad": [[2,19,20,28,29,30]],
"mk": [6],
"ml": [6],
"proxecto1": [2],
"mm": [[21,29]],
"entri": [1],
"mn": [6],
"proxecto2": [2],
"usbeco": [6],
"mr": [6],
"ms": [[6,15]],
"mt": [25,6],
"wxl": [14],
"escrib": [30,[17,18]],
"ruso-bielorruso": [19],
"ampliar": [26],
"my": [[6,13]],
"omitida": [12],
"respectivament": [29,[16,19]],
"na": [30,20,17,14,12,[2,15],26,[25,29],[8,16,18,19,27],[1,9,10,21,22],[7,13,23,32],[0,3,6,11,24,28,31]],
"nb": [[6,25]],
"nd": [6],
"editar": [32,20,[12,28],[8,29,31],30,5,[14,17,18,22,26]],
"ne": [6],
"updat": [2],
"ng": [6],
"marcada": [23,27],
"nl": [[6,29]],
"enderezo": [2,[17,32]],
"pechars": [[16,20]],
"nn": [[6,21,29]],
"navegador": [30,32,13,[17,20]],
"no": [30,[17,29],20,2,16,21,[12,15,25],13,18,[1,10],19,[5,8,27],28,23,22,[0,9,11,14,24,31],[3,6,32],26],
"desmarcar": [[25,29]],
"nr": [[6,16]],
"marcado": [20,[0,19,29]],
"córnico": [6],
"nv": [6],
"gotohistoryforwardmenuitem": [8],
"manteña": [29,14],
"ny": [6],
"húngaro": [6],
"abkhazo": [6],
"oc": [[6,19]],
"preparación": [17],
"conterá": [[13,25,29]],
"od": [14],
"of": [31,[2,11,19]],
"traballa": [13,[20,23]],
"orfo": [29,32,[26,30]],
"butané": [6],
"oj": [6],
"ok": [2],
"reserva": [[17,19]],
"iniciara": [17],
"om": [6],
"traducíbei": [[12,26,29,30]],
"traducíbel": [12],
"or": [6],
"os": [29,17,20,30,12,2,13,14,23,21,[16,27],10,15,25,28,18,22,26,[8,11],19,[24,32],[1,3],[4,6,9],[0,5,31]],
"opcion": [29],
"ot": [14],
"ou": [20,30,29,17,24,13,16,[12,15],[25,26],[1,2,10,14,21,27,28],9,[7,8,11,18,23],[3,4]],
"dado": [[21,29],[17,23],[13,14,30]],
"orfa": [29],
"vexa": [29,[13,20],30,24,[2,17,19,23,26],[7,12,14,18,25],[0,5,8,16,21,27,28]],
"oji": [6],
"dito": [25],
"pa": [6],
"editinserttranslationmenuitem": [8],
"pc": [17],
"descanso": [27],
"instalar": [17,32,27,11,[2,31],[10,18,20,30]],
"complexa": [[10,24]],
"pi": [6],
"complexo": [14],
"pl": [6],
"tradución": [29,30,10,19,20,32,13,28,14,23,[21,31],[15,18],[2,25],17,[8,26],[12,16],[24,27],[1,3]],
"propósito": [32,29,7,[16,17,21]],
"po": [12,30,14,[29,32],2],
"sesión": [[2,9,13,23]],
"ps": [6],
"soament": [[16,25]],
"pt": [[6,17,19,27]],
"startdict": [11],
"inclus": [24],
"automatizar": [12],
"iniciada": [24],
"subdirectorio": [29],
"dixo": [14],
"aplicar": [[2,12,25]],
"correo": [9,13],
"ciano": [20],
"aplican": [26],
"remitirs": [2],
"penalización": [29,15],
"recent": [[17,21,29]],
"qu": [6],
"edit": [27],
"editselectfuzzy5menuitem": [8],
"sinxeleza": [2],
"escondido": [20],
"efectúen": [12],
"singular": [3],
"rm": [6],
"redimensionamento": [20],
"rn": [6],
"ro": [6],
"duplicou": [15],
"precedida": [24],
"ru": [6],
"fornecida": [19],
"rw": [6],
"activado": [19,[12,20]],
"quedar": [30],
"optionstranstipsexactmatchmenuitem": [8],
"melloran": [3],
"sa": [6],
"avpág": [5],
"sc": [[6,24]],
"sd": [6],
"se": [17,29,20,30,12,21,16,25,15,13,26,[19,23],28,14,2,18,27,10,[1,5,8],22,[0,9],[11,24],[3,6]],
"nynorsk": [6],
"sg": [6],
"si": [17,[6,14]],
"exportación": [[16,28,29],14],
"sk": [6],
"sl": [[2,6]],
"samuel": [[4,7]],
"sm": [6],
"apropiado": [[12,17],[14,22,29]],
"súa": [17,30,[4,14,16,18],[2,10,13,15,26,29],[11,12,21]],
"sn": [6],
"so": [17,[2,6,16]],
"lectura": [[4,5]],
"sq": [6],
"sr": [26,6],
"creará": [17,[12,18],[16,21]],
"ss": [6],
"st": [6],
"su": [6],
"español": [27,19,6],
"ond": [17,30,8,[2,13,18,20,21,23,27,29]],
"apropiada": [[2,10]],
"sw": [6],
"orix": [29,30,20,[12,32],[14,16],28,[18,23],[22,26],25,8,[3,17,19,21,31],[0,1,2,10,15]],
"reinstal": [13],
"condición": [7,29],
"xestiónas": [14],
"inmediata": [21],
"remitirá": [2],
"ta": [6],
"editoverwritesourcemenuitem": [8],
"mengano": [29],
"alimentación": [24],
"te": [6],
"tg": [6],
"retorno": [24],
"th": [6],
"suficient": [[12,13,17,19,26]],
"ti": [6],
"tk": [6],
"tl": [6],
"créase": [29],
"tm": [29,[30,32],[2,25],18],
"pé": [12],
"tn": [6],
"to": [[6,25]],
"v2": [19,[17,32]],
"tr": [6],
"ts": [6],
"tt": [6],
"mesma": [[13,15,25],[12,14,17,21,28,29,30]],
"enviando": [9],
"sufixo": [3],
"tw": [[6,17]],
"rutina": [17],
"ty": [6],
"lugar": [29,2,[1,9,17,18,27,30]],
"seleccion": [[2,17],[12,18,21,25,27,28,30],[1,13,20,22,26,29]],
"hmxp": [14],
"oriá": [6],
"extraer": [[11,12,13,17,26]],
"projectwikiimportmenuitem": [8],
"mesmo": [29,2,17,[12,24,28,30],[7,10,16,20,21],[1,11,13,15,18,22,26,27]],
"ug": [6],
"uk": [6],
"yahoo": [[9,13]],
"coincidir": [[12,24]],
"un": [2,17,29,13,20,12,21,16,[10,15,18],[24,27],14,[19,30],26,25,1,[5,32],[8,22],[23,28],[3,4,9],[0,11],[7,31]],
"calcula": [30],
"triviai": [29],
"ur": [6],
"pareza": [17],
"pretradución": [32],
"uz": [6],
"this": [[16,24]],
"substitúa": [29],
"serbio": [6],
"bilingü": [29,[10,11,19,32]],
"tardar": [21],
"ve": [6],
"acelerar": [29],
"vi": [[6,17]],
"considerar": [[23,29]],
"vo": [6],
"operador": [[24,31]],
"libr": [7,[1,10,11,15,19,27,31]],
"ningunha": [[17,29],[7,14,20,30],[16,21]],
"escapars": [17],
"resolución": [[19,32],31],
"albané": [19],
"arrastrar": [[17,30]],
"creationd": [30],
"wa": [6],
"privada": [[17,19]],
"dano": [16],
"omegat.sourceforge.net": [17],
"identificar": [[3,15]],
"groovy.codehaus.org": [1],
"wo": [6],
"executan": [17],
"privado": [2],
"abreviación": [26],
"ord": [17,32,20,[13,21],15,[2,25,26],[29,30],[4,10,19,31]],
"só": [[12,17,25,29],2,30,[13,15,20,23],[27,28],[4,5,9,10,11,14,16,21,22,26]],
"executar": [17,32,21,18,[1,2,30,31]],
"licens": [7],
"aplicábel": [13],
"emac": [17],
"divisa": [24],
"ori": [6],
"xa": [30,[17,23,29],[10,16],[1,21],[2,15,26,27,28],[5,8,12,13,14,20,25]],
"orm": [6],
"créans": [14],
"xe": [15],
"xf": [17],
"xh": [6],
"damo": [14],
"venda": [6],
"superior": [30,[13,26],[9,17,18,19,20,32]],
"segu": [[2,29]],
"xn": [17],
"funcionan": [26],
"xp": [13],
"lido": [[20,23]],
"máximo": [8],
"xx": [17,12],
"xy": [24],
"xerará": [29],
"sourc": [13,17,29,[1,32],[16,18,20,21,30]],
"esloveno": [[3,6,16,19,25]],
"límite": [24,31],
"type": [8],
"oss": [6],
"volker": [7],
"yi": [6],
"termo": [16,30,[2,25],[5,18]],
"toolssinglevalidatetagsmenuitem": [8],
"yo": [6],
"alguén": [30],
"proveron": [10],
"extraia": [17],
"yu": [7],
"colaborativa": [2],
"como": [[17,29],30,10,[14,20,32],[13,15],[16,25,28],[19,23],1,[7,21,26,27],[11,22],[2,8,18,31],[0,5,9,12,24]],
"stylesheet": [1],
"construído": [[12,19]],
"yy": [12],
"za": [6],
"nome": [12,30,[29,32],17,[13,27],[2,28],[18,20],[1,6,11,14,15,16,19,21,22,23,25,26]],
"otp": [14],
"chichewa": [6],
"coma": [29,12,30,[2,14],[10,15,16,19,24,25],28,[3,13,22],[9,11,20,23,26]],
"zh": [6],
"ott": [14],
"exist": [[13,29],[1,14,16,30]],
"eslovena": [19],
"penalti": [29],
"zu": [6],
"zz": [17],
"yiddish": [[6,19]],
"parecida": [10],
"dalgún": [[12,15,23,29]],
"longa": [30],
"busca": [25,30,32,31,24,[10,20],[12,14,15,18,21,26]],
"extensíbel": [1],
"utf8": [16,22,[12,14]],
"excepto": [24,5],
"copi": [29,17,21],
"tanto": [29,30,13,[14,15,25],[7,17,24,28],[2,10,12,18,19,23,26,27]],
"albano": [6],
"vantax": [[2,17,29]],
"localiza": [[17,21]],
"columna": [16,30,15],
"separars": [15],
"longo": [[22,30]],
"redistribuílo": [7],
"representado": [15],
"funcion": [[1,13,18]],
"power": [12],
"aparecen": [[8,20,28,30]],
"selo": [30],
"xigabyt": [17],
"aquí": [15,2,[13,17],[14,21,23,25,30]],
"preciso": [12],
"tag-valid": [21,17],
"programación": [1,[15,20]],
"foran": [10],
"método": [[17,25],32,[14,31]],
"maratí": [6],
"oportunidad": [13],
"u0009": [24],
"xhh": [24],
"destacará": [30],
"maorí": [6],
"revis": [[11,29]],
"u0007": [24],
"repositori": [2],
"xho": [6],
"extraers": [13],
"instalou": [17],
"data": [[13,16,25,30]],
"xht": [14],
"lowercasemenuitem": [8],
"firefox": [27,1],
"lists.sourceforge.net": [9],
"cartafol": [17,29,13,2,32,20,30,[18,21],[16,23,27],[1,11],[0,3,10,12,25],[8,14,28,31]],
"garantía": [7],
"morfolóxica": [19],
"filepath": [30],
"dicir": [29,2,20,25,[15,16,22,23,27],[1,11,12,17,21,28,30]],
"dato": [21,[1,2,13,16,28,32],[10,14,17,23,30,31]],
"xerars": [29],
"automática": [19,20,30,32,[8,10,31],[16,23,27,29]],
"permitan": [[15,26]],
"oasi": [14],
"comprimido": [13],
"instalado": [17,[2,13]],
"nl-zh": [29],
"crears": [[13,29]],
"automático": [[21,32],[15,20,31]],
"xestionar": [14,19,[2,10,16,25]],
"comprimida": [32],
"core": [14,15],
"activada": [12,[10,30]],
"devandito": [[2,12,29]],
"canguro": [10],
"instalada": [17],
"openoffic": [27],
"note": [24,[21,25,29,30]],
"dará": [15],
"ensuciar": [25],
"engadiremo": [2],
"optionsautocompletechartablemenuitem": [8],
"saír": [20,8,29],
"axeitado": [17],
"concreto": [[10,12,19]],
"helari": [[4,7]],
"axeitada": [20],
"git": [2,29],
"reutilización": [29],
"inclusión": [30],
"exportar": [[20,29,32],28,[8,10,16]],
"estruturai": [26],
"abaixo": [15,[8,14,26]],
"léxica": [19],
"perdela": [13],
"continuar": [18],
"nota": [30,20,12,8,25,[2,14,16,17,18,19,21,26,29]],
"aplícas": [15],
"estruturar": [26],
"diferenza": [30],
"actualizando": [29,[31,32]],
"will": [4],
"pechado": [15],
"dependerán": [0],
"perdelo": [13],
"noso": [29],
"nort": [6],
"optionsspellcheckmenuitem": [8],
"considera": [[14,15]],
"xlf": [14],
"pechada": [20],
"frase": [26,[10,25],[23,32],[0,12,18,20,30],[19,24,29]],
"nomedoficheiro": [29],
"optionssetupfilefiltersmenuitem": [8],
"altgraph": [8],
"poderá": [[13,17,25,29]],
"novo": [[18,29],[12,17],8,[20,32],[1,16,27],[23,30],[2,10,13,14,15,19,22,26,28,31]],
"elimin": [[0,3,17,29,30]],
"moderno": [6],
"mencionou": [17],
"xml": [14,1,12,[13,15,16,19]],
"árbore": [13],
"menor": [[14,17,29]],
"persistent": [4],
"gla": [6],
"nova": [[17,26],[16,20],[24,32],[14,23,28,29,30,31]],
"resolvan": [2],
"gle": [6],
"pedir": [9],
"pediu": [17],
"glg": [6],
"serv": [30],
"semellant": [[0,29]],
"nome_do_proxecto-level1": [29],
"nome_do_proxecto-level2": [29],
"glv": [6],
"aplicará": [29],
"sincronizará": [2],
"maiúscula": [20,24,8,25,[17,26,30,32]],
"perigo": [29],
"seri": [[1,29]],
"tar.bz": [11],
"xltx": [14],
"engad": [[1,15,17,21,28]],
"independent": [13,[17,19,24]],
"seus": [29,22,30,[10,14],[2,13,17,20,21,25,26]],
"lóxico": [[24,31]],
"prazo": [22],
"desta": [[15,29]],
"despraza": [[5,20],30],
"asumirá": [23],
"lonxitud": [30],
"corrupción": [15],
"compilan": [19],
"xlsx": [14],
"castelá": [19],
"modificación": [8,[7,13,20],[10,12,14,15,16,30]],
"eslavónico": [6],
"baleirar": [30],
"ofrecemo": [10],
"poidan": [2],
"rápido": [17,[10,16]],
"creada": [[10,15,29,30]],
"aaaa": [[21,29]],
"gnu": [7,13],
"creado": [13,2,[12,14,16,17,18,20,27]],
"chiné": [[6,20,29]],
"target.txt": [28],
"facer": [17,30,[12,14,27],[10,13,16,29],[2,15,18,19,20,21,22,23,24,25,28]],
"sexa": [28,29,[2,7,14,20],[10,15,16,19,21,24,30]],
"ojibw": [6],
"nameon": [12],
"introducir": [15],
"pan": [6],
"canadens": [26],
"probábel": [[19,21,26,29,30]],
"gotonextnotemenuitem": [8],
"par": [15,29,19,2,[21,23,26,30]],
"un_espazo": [20],
"tar.gz": [17],
"gpl": [11],
"inglé": [29,[15,24],[3,6,17,19]],
"constitúen": [13],
"folla": [[12,30],[1,16,24]],
"coidado": [[2,4,15,21]],
"list": [31],
"omisión": [4],
"especiai": [26],
"conflito": [2,[8,29]],
"tagalo": [6],
"será": [17,12,[15,29],[2,5,8,14,19,20,21,25,30]],
"personalizada": [[15,17,20]],
"lisa": [16],
"azur": [17],
"text.html": [17],
"azul": [30,[0,15,25]],
"descargars": [[1,16]],
"formato": [14,32,16,12,30,10,[15,31],[11,18,19,29],[1,17,20,22,23,25,28]],
"numeroso": [[9,12]],
"rashid": [7],
"problemático": [13],
"asignars": [8],
"doutro": [10],
"navegar": [30,[17,19]],
"dividen": [30],
"diferencia": [30,25],
"distribúes": [7,13],
"preséntas": [15],
"grn": [6],
"nyanja": [6],
"estatística": [30,32,[13,20],8,31,19],
"xtg": [14],
"bindownload.cgi": [17],
"amosarán": [[21,25],[17,20],30,16],
"doutra": [[10,17,19]],
"aniñamento": [15,32,31],
"gardar": [20,[8,17,22,30]],
"lado": [27],
"obsoleto": [30],
"pdf": [[1,20,23]],
"gardan": [29],
"efectivo": [26],
"consultars": [10],
"mentr": [13,30,[10,12],[20,29]],
"implicitament": [[2,12]],
"proceda": [23],
"bislama": [6],
"toolsshowstatisticsmatchesmenuitem": [8],
"hexadecim": [24],
"manexo": [1],
"traduc": [21,30,[12,29]],
"viewdisplaymodificationinfononeradiobuttonmenuitem": [8],
"viceversa": [14],
"porción": [9],
"admitida": [12],
"exportábei": [29],
"plurilingü": [32],
"concisa": [4],
"per": [5],
"terán": [[12,23,28]],
"paí": [17,29,12,32],
"apreciaríamo": [9],
"traballaba": [19],
"cadro": [[24,29]],
"biari": [6],
"pedra": [16],
"aparencia": [[14,17,21,25]],
"colorearán": [30],
"público": [13],
"pech": [15,[21,29]],
"producida": [29],
"razón": [[2,13,16,17,27]],
"invisíbel": [13],
"esperanto": [6],
"guj": [6],
"autocompletado": [8],
"metaetiqueta": [12],
"regexp": [17,21],
"tahitiano": [6],
"volverá": [[17,21]],
"relación": [[23,30]],
"desprazamento": [20,[28,31,32]],
"stemmer": [3,32],
"declarativa": [1],
"uhhhh": [24],
"dalgunha": [10],
"dividan": [23],
"imperativa": [1],
"optionssentsegmenuitem": [8],
"pública": [[7,13],9],
"incompatibilidad": [[0,3]],
"medrar": [19],
"dokuwiki": [14],
"encher": [29],
"inconsistent": [29],
"aplicala": [15],
"palabra": [25,30,20,24,[3,10],[5,16],[17,18,27,31],[0,1,8,13,14,19,26,29,32]],
"test.html": [17],
"xxx": [29],
"accidentalment": [15],
"smalltalk": [1],
"ofrecerá": [27],
"ant.apache.org": [17],
"mínimo": [[20,24]],
"instalará": [17],
"introdución": [31,10,[0,1,2,3,18,19,32]],
"tempo": [30,19,[1,10,18,21,26,27]],
"dilixencia": [4],
"unirs": [[9,13]],
"pseudotranslatetmx": [[17,29]],
"listado": [20],
"insíreo": [16],
"reparado": [29],
"péchea": [25],
"verbo": [19],
"arno": [7],
"velasco": [7],
"estabelecers": [[16,26]],
"targetlanguagecod": [12],
"coincidirán": [24],
"pilpré": [7],
"aforroum": [4],
"nuosu": [6],
"innecesario": [2],
"presentarans": [15],
"empregaran": [23],
"contribú": [[14,15,30]],
"empregaras": [0],
"pega": [20],
"soluciona": [22],
"asegurándos": [16],
"reflectirán": [13],
"locativo": [16],
"expórtans": [28],
"aceptan": [8],
"causa": [17,16],
"aceptar": [2,17,[14,18,19]],
"entran": [8],
"artigo": [[4,9]],
"corrección": [[0,4,27]],
"extra": [17,21,[15,24]],
"deben": [17,[28,29],[2,8,15,26,27]],
"creationid": [30],
"reflictan": [13],
"xestión": [[20,32],10,[15,31],[2,13,14,16,30]],
"propón": [10],
"porqu": [29,[12,18,30]],
"combinar": [25],
"avanzar": [[5,8,20]],
"pli": [6],
"consecutivo": [[10,15,20]],
"encyclopedia": [11],
"desex": [12,[17,23,27,29]],
"cambi": [[14,22,26,27,29]],
"simplifica": [14],
"asegur": [18],
"arranxo": [29],
"considéras": [13],
"manexa": [25],
"rapidament": [[25,30]],
"optionstagvalidationmenuitem": [8],
"decat": [23],
"recentement": [[18,19]],
"europea": [29],
"aplicada": [0],
"suficientement": [29],
"pt_br": [27,17],
"cuantificador": [24,31],
"estimación": [30],
"asignación": [17,32],
"libraría": [1],
"a-z": [24],
"recuperar": [12],
"evento": [8],
"zoltan": [7],
"asunto": [[9,21],[6,27,31,32]],
"manterán": [12],
"poida": [[16,29],[17,30],[2,4,9,10,14,21,25]],
"validará": [30],
"tomando": [[3,27,30]],
"receita": [29],
"png": [17],
"javascript": [1],
"marqu": [[25,26],[12,23,27,28]],
"mediawiki": [[20,30],8],
"input": [25],
"comezando": [30],
"komi": [6],
"ofrecémosll": [10],
"volv": [13],
"escaso": [29],
"caracter": [[12,16,24],[17,22,25],[5,8,15,19,20,30,31],14],
"join.html": [13],
"gardou": [[20,21]],
"compilador": [14],
"procesamento": [14],
"pod": [14],
"retiran": [20],
"superposición": [15,32,31],
"facto": [30],
"poi": [15,[1,2,10,13,17,19]],
"apareza": [10],
"executalo": [[1,10]],
"pol": [6],
"aplicabilidad": [26],
"pens": [9],
"pon": [18],
"etiqueta": [15,32,20,30,12,14,31,18,[17,21],[8,29],23,2,[10,26]],
"por": [29,12,26,25,[16,30],17,[2,15,27],10,22,23,[8,14,20,24],19,[4,13,21],[9,11],[0,3,6,18,28,31,32]],
"dispositivo": [21],
"construír": [17,[19,32],31],
"checheno": [6],
"tomado": [8],
"pena": [2],
"voluntario": [9],
"díxito": [[20,24],[21,29]],
"agrupar": [15],
"seguridad": [29,21,32,17],
"coincidirá": [25,23],
"procedemento": [[21,29],[10,27]],
"googl": [19,[17,32],31,[10,25]],
"opendocu": [12],
"neerlandé": [6],
"travé": [[9,30],[10,11,13,20,23]],
"comproba": [[15,20]],
"actualizar": [17,[2,32],29,[10,16,23]],
"converterá": [18],
"támil": [6],
"hai": [17,16,[14,29,30],[20,28],26,[0,1,2,3,10,11,12,18,22,24,27]],
"sourceforg": [9,[8,13,32],[2,31]],
"buscando": [25,[21,29,32]],
"pegada": [15],
"chama": [[12,17]],
"permitirá": [[12,16]],
"actualizan": [17],
"soltar": [17],
"hat": [6],
"redución": [15],
"realiza": [17],
"hau": [6],
"executábel": [[13,17]],
"editmultipledefault": [8],
"batch": [13],
"editfindinprojectmenuitem": [8],
"bretón": [6],
"axudará": [29],
"reproduc": [15],
"peor": [15],
"determinado": [13],
"warn": [21,17],
"technetwork": [17],
"descargado": [[13,17]],
"plural": [[3,12]],
"detecta": [[15,16,17,30]],
"formada": [29],
"plurai": [[3,16]],
"conta": [17,30,29,[2,15,19,23],27,[9,13,24,25,28],[5,8,11,16,20,21,26]],
"dispara": [0],
"perd": [13],
"exporta": [20,[16,18,28,29]],
"tamén": [17,30,[20,29],2,[12,21,27],[0,8,9,10,14,16],[3,11,13,18,19,25,26,28,31]],
"jacob": [7],
"numerai": [30],
"comprobación": [[15,29]],
"ench": [2],
"confirmar": [[2,8,20,29]],
"país": [29],
"esperanza": [7],
"configura": [20],
"interlingu": [6],
"n.n_windows.ex": [17],
"tigrínia": [6],
"pop-up": [16],
"acord": [5],
"veñen": [30],
"totalment": [29,15],
"tipo": [[21,32],14,30,12,[15,26],[8,10,13,20,22,28,29,31]],
"heb": [6],
"brune": [7],
"localizada": [21],
"kanji": [22],
"reparar": [15],
"actuará": [29],
"desactualizada": [14],
"program": [17],
"pus": [6],
"keith": [7],
"her": [6],
"localizado": [20,17],
"inisir": [20],
"solicitar": [29],
"trae": [30],
"complexidad": [4],
"deseño": [30,12],
"executará": [17],
"listax": [10],
"campá": [24],
"resultant": [17,21],
"contaminar": [29],
"camtasia": [14],
"distribuír": [[13,29]],
"beneficio": [17],
"facelo": [4],
"n.n_mac.zip": [17],
"distribuída": [7],
"tabl": [20,24,8,30,31,[5,6]],
"noruegué": [6],
"pseudotradución": [[29,32]],
"doc-license.txt": [13],
"xestiona": [23],
"eclesiástico": [6],
"yid": [6],
"copyflowgold": [14],
"aprenda": [[10,18],[17,31]],
"editor": [25,[5,31],[10,14,17,20,21],[0,1,2,6,12,15,16,22,23,28]],
"pseudotranslatetyp": [[17,29]],
"hhc": [14],
"consistent": [[10,20,26]],
"hhk": [14],
"producirs": [[1,12]],
"tecnoloxía": [17],
"correctament": [[14,15],[22,29],[0,16,19]],
"intentará": [[16,17,28]],
"edíteo": [21],
"garden": [16],
"multiparadigmática": [1],
"javaapplicationstub": [17],
"posteriorment": [[17,30]],
"projectclosemenuitem": [8],
"executars": [17],
"hin": [6],
"viewmarknonuniquesegmentscheckboxmenuitem": [8],
"importa": [17,[20,21]],
"posibelment": [[3,13,24,29]],
"sobr": [17,[8,10,20],[29,30,31],[0,1,4,6,18,24,26]],
"tres": [29,15,[2,11,13,14],[9,16,18,20,22,30]],
"pegala": [30],
"consider": [22],
"group": [13],
"reservada": [1],
"readme.txt": [[7,13]],
"letra": [21,20,32,24,[29,30],8,[14,31]],
"campo": [30,12,[17,28],[16,20,25],[2,10,27],[14,21]],
"languagetool": [0,[10,31,32],[1,3]],
"occitano": [6],
"dispoñibilidad": [19],
"source.txt": [28],
"files.s": [1],
"siband": [7],
"histori": [20,8],
"exchang": [16],
"output-tag-valid": [17],
"dhinehi": [6],
"detallada": [30,13],
"sinta": [21,17],
"projectlock": [2],
"merram": [11],
"devolveu": [19],
"request": [19],
"obxecto": [1,26],
"currseg": [1],
"point": [12],
"accesíbei": [[13,30]],
"explica": [10],
"procesa": [19],
"reservado": [20],
"general": [7],
"l4j.ini": [17],
"identifica": [12],
"localización": [32,17,13,29,16,[14,21,25],[8,20,23]],
"mencionar": [4],
"agregarans": [12],
"abriras": [[2,20],[16,18,30]],
"proceso": [26,[2,10,13,15,21,23,28,29]],
"membro": [2,29,13],
"marcará": [15],
"colección": [30,[16,24,32]],
"alternativa": [30,20,[23,28],[2,13,29],[8,14,17,22,32]],
"andrzej": [7],
"substituído": [20],
"alternativo": [[29,32]],
"hábito": [13],
"normalment": [17,21,15,[13,29],[14,20]],
"teña": [17,29,23,[27,30],[15,19,21],[2,13,20,26],[5,9,11,16,25]],
"marcando": [[14,15]],
"teño": [4],
"traduza": [[19,20,26]],
"europeo": [26],
"acordo": [17],
"tomara": [20],
"antigo": [6,[20,21,23]],
"atopa": [29,[12,16],[15,19,21,24,25,30]],
"amosada": [20,[25,30]],
"dhttp.proxyhost": [17],
"especifican": [12,17,21],
"hmo": [6],
"especificar": [17,21,[8,12,13,14,22]],
"barra": [30,17,[20,21,24]],
"marca": [20,[23,30],[12,28],[14,17]],
"amosado": [30,[2,15,20]],
"acabar": [26],
"maximizar": [30],
"ignored_word": [32],
"yor": [6],
"responsabilidad": [2],
"producir": [26,[18,23,30]],
"corrector": [27,[10,31]],
"reinici": [8],
"activa": [[12,20,25]],
"cousa": [[9,18],[13,31]],
"situación": [[22,29]],
"configur": [16],
"oculto": [15],
"emprégans": [30,12],
"activo": [20,30,16,[5,17,28],[1,8,25,26,29]],
"documento.xx": [12],
"buscará": [18],
"acept": [13],
"importala": [29],
"buloichik": [7],
"noutra": [[17,18,29]],
"optionsworkflowmenuitem": [8],
"interior": [5],
"actualizou": [30],
"engadiron": [1],
"inclúan": [25],
"releas": [[8,17]],
"noutro": [17,29,1],
"peter": [7],
"comet": [[15,18]],
"segmentan": [26],
"cordonni": [7],
"sparc": [17],
"segmentar": [26,29],
"definido": [[12,22,30]],
"limitará": [16],
"nunca": [12,24,19],
"consisten": [15],
"suahili": [6],
"recuperalo": [13],
"indicador": [30],
"sandra": [7],
"antiga": [[17,23]],
"comec": [29],
"día": [[13,29],21],
"copiarán": [20],
"facturación": [19],
"nunha": [29,[20,30],13,[2,10,19,27]],
"acaben": [29],
"pacient": [4],
"tale": [[18,26]],
"amparado": [7],
"prior": [4],
"chinesa": [[17,29]],
"variábei": [15,12,30,20],
"nalgún": [29,[12,19,21,23,30]],
"poñer": [29],
"mostra": [[10,25],[11,21,31]],
"simplement": [[15,17],[9,13,16,22,26,29,30]],
"definida": [[8,14,27,29]],
"hrv": [6],
"coloca": [30],
"rexistrada": [28],
"había": [27],
"rexistrado": [10,[2,17,20]],
"probado": [14],
"directament": [17,12,[13,26]],
"file-source-encod": [12],
"subliñaras": [0],
"xusto": [15],
"cuestionario": [13],
"entr": [30,15,14,[20,29],[12,16,17,21,28],[0,1,2,13,19,24,25]],
"eliminan": [[15,23]],
"vilei": [7],
"tártaro": [6],
"piotr": [7],
"tang": [7],
"versionado": [29],
"divid": [[10,26,30]],
"pecha": [20,[15,29]],
"asumir": [22],
"asociado": [17,10],
"especificala": [21],
"editexportselectionmenuitem": [8],
"monitor": [[9,17,21]],
"transformación": [1],
"procesador": [14,1],
"aimará": [6],
"home": [13,[0,1,2,3,4,5,6,7,8,9,10,11,12,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30,32]],
"michael": [7],
"print": [15],
"tecnicament": [12],
"eliminar": [5,[23,28],[15,25],[16,17,20,27,29,32]],
"compatíbei": [[0,10,15,16,18,24,29,30]],
"build.xml": [17],
"compatíbel": [14,17,29,1,16,[2,10]],
"innegábei": [19],
"hun": [6],
"utilizada": [30],
"lector": [10],
"fusionala": [29],
"asegúras": [2],
"retírea": [15],
"millón": [[17,19]],
"malgax": [6],
"herero": [6],
"baixo": [[7,29],[1,2,11,17,30]],
"asociars": [14],
"aligndir": [[17,21]],
"system-host-nam": [12],
"latín": [6],
"creat": [2],
"garántell": [30],
"python": [1],
"es_mx.dic": [27],
"operativa": [2],
"infix": [14],
"crear": [32,29,20,[8,17],16,[2,23,26],[13,14,15,18,19,31],[28,30]],
"catro": [21],
"tarbal": [11],
"hindi": [6],
"proxecto_exemplo": [13],
"deixa": [[10,12]],
"omegat-development-request": [9],
"redactou": [14],
"executado": [[13,20]],
"similitud": [[15,28]],
"hora": [12,[4,20,21,25,29]],
"aparezan": [14],
"xent": [[4,10]],
"importada": [29],
"prototipo": [1],
"file": [25,1,17],
"creen": [12],
"experiencia": [[15,23]],
"operativo": [17,12,[2,20]],
"múltipl": [[30,32],31,16],
"bandeira": [[24,31]],
"ignorarán": [29],
"tard": [[13,18,20,23,27,28]],
"meno": [[13,29],[2,4,10,14,15,16,17,19,21,23,24,28]],
"catti": [3],
"menu": [16,30],
"unidad": [29,26,[10,20]],
"senxela": [25],
"hye": [6],
"importado": [29],
"a-za-z": [24,25],
"abrirs": [30],
"chamars": [[17,27]],
"negra": [30,15,[14,20,25]],
"apéndic": [[5,10,20,30]],
"asegúres": [17,[2,13,14,15,27]],
"enví": [[19,21]],
"etiquetarán": [29],
"source-pattern": [[17,21]],
"explicitament": [21],
"cunha": [[17,20,30],[14,15,16,27,32]],
"chua": [7],
"verificador": [13,27],
"definitivo": [10],
"marcarán": [20],
"problema": [32,16,22,[15,31],[11,19],[2,14,17,20,21,29,30]],
"asegura": [17],
"limitars": [25],
"tódala": [1],
"senón": [[1,15,29]],
"convenient": [17,29],
"xliff": [14],
"true": [17],
"present": [[17,30],[13,15,21,29]],
"adicion": [[15,16,29],17],
"islandé": [6],
"fágao": [13],
"groovi": [1],
"trate": [15],
"evitar": [29,2,[10,14,21,23,28]],
"distinguila": [18],
"trata": [[10,17]],
"fusionars": [2],
"desactivar": [20,12],
"ignóras": [15],
"especificado": [[13,17]],
"protexida": [18],
"transform": [1],
"kmenueditor": [17],
"menú": [8,31,20,30,32,17,[12,23],[1,10,18,21,22,26],[13,14,19,27,28]],
"sindi": [6],
"realidad": [14],
"xere": [[19,29]],
"miúdo": [[1,2]],
"tortoisesvn": [2],
"especificada": [25,[8,17]],
"debería": [26,[2,11,12,23],[3,8,9,13,15,20,21,29,30]],
"soto": [6],
"atención": [[15,17],[12,26]],
"abrirá": [27,[15,20,25]],
"prefixará": [29],
"messageformat": [15],
"kmenuedit": [17],
"chuang": [6],
"xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx": [17],
"writer": [18],
"dalloway": [26],
"rubi": [1],
"meus": [4],
"desmarqu": [23,[12,19,26]],
"programa": [17,21,[10,20],[1,2,16,18,28,30]],
"dependerá": [[14,29]],
"filtrado": [12,[10,31,32]],
"utilidad": [[17,25]],
"finé": [6],
"práctica": [17],
"preprocesalo": [29],
"sentido": [[26,27],[5,22,29]],
"conseguiron": [19],
"basea": [19],
"previsión": [21],
"xapones": [22],
"pulaar": [6],
"edita": [30],
"mestra": [29],
"capítulo": [10,30,13,29,[19,23],[4,7,16,24,25,26,31]],
"desprazar": [[5,26]],
"nomears": [8],
"locmanag": [14],
"simplifiqu": [15],
"regex": [24,31],
"preposición": [4],
"meta": [8,24],
"afectan": [15],
"regez": [[24,31]],
"abra": [[17,18],[21,29],[2,12,13,15,19,28,30]],
"porcentax": [30,29],
"remitir": [2],
"intacto": [[4,29]],
"globai": [32,23],
"sango": [6],
"global": [12],
"afectar": [[13,30]],
"expresión": [24,25,32,[12,15],[10,26,31],17,[11,21,30]],
"free": [7],
"práctico": [[17,30]],
"afecta": [15,17],
"personalizars": [[10,30]],
"modificador": [8],
"valor": [24,17,21,[29,30],[12,14]],
"indícall": [30],
"ibm": [17],
"fish": [3],
"ibo": [6],
"consiga": [17],
"incluír": [[2,13]],
"eslovenia": [30],
"incluíu": [30],
"baskir": [6],
"poda": [18],
"esquerda": [[14,32],5,[26,30,31]],
"exhaustivo": [10],
"corromperá": [15],
"sundané": [6],
"atoparán": [[12,25]],
"jean": [7],
"abrimo": [13],
"transmisión": [19],
"exhaustiva": [[6,18,24]],
"supresión": [15],
"fixeran": [29],
"copiando": [18],
"volapuqu": [6],
"alemán": [[6,25]],
"esencialment": [27],
"abrila": [30],
"ido": [6],
"mantemento": [2],
"abrilo": [30,[10,15]],
"idx": [11],
"que": [17,29,30,2,[15,20],[12,13],21,16,25,[10,19,23,26],14,18,27,28,[9,22],24,8,[4,5],1,[0,7,11],[3,6,31]],
"semella": [13],
"actualic": [[2,21]],
"fixa": [15],
"estándar": [30,[16,27],[1,6,10,13,19,21,29]],
"intacta": [13],
"inmediatament": [20],
"empeorará": [19],
"clasificarán": [30],
"fixo": [30,[17,19,20,28]],
"compartir": [2,29,32,31],
"linux": [17,32,[2,13],[10,16,21,24,30,31]],
"encomenda2": [29],
"actualment": [[20,30],[14,29]],
"checkout": [2],
"encomenda1": [29],
"dentro": [15,17,[1,2,10,11,16,25,29,30]],
"inferior": [30,[1,16,26,28]],
"financeiro": [32],
"elimina": [[20,25,27,29]],
"posibl": [3],
"zha": [6],
"instalarans": [27],
"ifo": [11],
"pode": [17,29,30,12,[2,14,25],15,18,[13,16],[21,27],[20,23,28],[10,26],[8,9],1,[0,22],[7,24],[4,5,11,19]],
"faga": [17,[2,12],1,[21,26],[9,13,15,16,20,23,27,28,30]],
"esquerdo": [1],
"corrompers": [15],
"icona": [17,[2,13],20],
"macedonio": [6],
"comprend": [29],
"zho": [6],
"octal": [24],
"segunda": [[16,17]],
"sistema": [17,12,[2,27],20,[16,32],[19,22],[8,21,29,31]],
"xx.docx": [12],
"gardando": [29],
"inicio": [17,32,5,21,13,[9,26]],
"consist": [13],
"borrador": [4,26],
"segundo": [[20,30],[7,10,12,15,25,29],[2,3,8,27,28,31]],
"especificars": [21],
"facerll": [22],
"price": [19],
"optionsautocompleteautotextmenuitem": [8],
"fernández": [7],
"zip": [[13,17]],
"inserirán": [20],
"ibai": [7],
"escollers": [13],
"yahoogroups.com": [9],
"concis": [11],
"elección": [[15,17]],
"sdlxliff": [14],
"chegada": [29],
"asignan": [17],
"inicia": [17,21,[28,29]],
"asignar": [[2,17]],
"cadea": [25,32,[20,30],[14,28],[3,8,16],[1,5,29]],
"almacenará": [30],
"extensión": [[12,16],22,11,14,[13,29]],
"iii": [6],
"copiou": [29],
"requir": [[19,27]],
"conectado": [19],
"incrementar": [[26,29]],
"fall": [17],
"kyle": [7],
"viewmarknotedsegmentscheckboxmenuitem": [8],
"encargars": [10],
"código": [[8,29],27,32,[6,12],17,[26,31],[1,10,19],[0,2,5,13,20,21]],
"subscrib": [9],
"abstract": [31],
"lingala": [6],
"optionssaveoptionsmenuitem": [8],
"excel": [12],
"debido": [26,[2,29]],
"comma": [16],
"procesará": [17,12],
"runn": [25],
"debaixo": [25],
"stardict": [11,32],
"omegat.l4j.ini": [17],
"span": [12],
"pola": [20,26,[8,30],13,[2,4,10]],
"intercambio": [[10,16]],
"diapositiva": [12],
"prefer": [13],
"hans-pet": [7],
"bloqueado": [17],
"crédito": [20],
"número": [30,[15,20],28,14,[1,17,19,29,32],[8,10,12,13,21,24,25]],
"limiar": [28],
"iku": [6],
"pijffer": [7],
"opinión": [30],
"creou": [[2,18,29]],
"tecla": [20,8,5,18,28],
"zakharov": [7],
"simpl": [15,[16,24],[0,10,12,14,19,28,32]],
"lexíbei": [20],
"detens": [28],
"hardwar": [21],
"thunderbird": [27],
"revisión": [29,2],
"ile": [6],
"editselectfuzzy3menuitem": [8],
"especificará": [17],
"verifica": [28],
"falt": [24],
"project.projectfil": [1],
"decatou": [27],
"phillip": [7],
"traba": [2],
"supostament": [15],
"regra": [26,32,23,10,15,13,[0,14,31],[12,17,24,30],19],
"iniciador": [17],
"relativament": [[13,30]],
"escolla": [17,[2,12,13]],
"protex": [23],
"ferramenta": [32,[19,20,29,30],[10,31],[8,24],1,[13,14,26],[17,21]],
"ruta": [17,21,30],
"corromp": [13],
"automatizarán": [17],
"error": [21,[9,17,29,30]],
"negación": [24],
"marcador": [[12,30],[14,32]],
"momento": [29,30,[1,10,12,15,18,19,23]],
"public": [7],
"maior": [[8,19,29]],
"complicado": [14],
"track": [9],
"ina": [6],
"ind": [6],
"contra": [[16,21]],
"pt_br.aff": [27],
"oromo": [6],
"renomear": [12],
"actúan": [5],
"kongo": [6],
"ini": [14,[17,32]],
"sinónimo": [10],
"acad": [29],
"compartan": [2],
"ruso": [17,[6,21,22]],
"acab": [[2,17,21]],
"fans": [[14,29]],
"escribindo": [[14,17]],
"polo": [30,29,26,[13,14],[2,15,17,19,20,28],[1,4,8,18,21,22,27]],
"dhttp.proxyport": [17],
"deixarans": [12],
"trado": [16,32,31],
"baseada": [[1,19],[10,20,26]],
"subrip": [14],
"describ": [10,[5,20,25,30]],
"score": [30],
"comerciai": [30],
"baseado": [[1,11,12,16,19,27,28]],
"escribirán": [20],
"cómodo": [21,17],
"navajo": [6],
"mestura": [32,14],
"versión": [17,2,29,19,7,[4,14],[0,3,9,13,16,21,24,27,31]],
"aparecerá": [2,17,[0,19,26,29,30]],
"appendix": [[0,1,2,3,5,7,8,9],[4,6],[21,26,29,32]],
"diagrama": [12],
"ipk": [6],
"suced": [[9,17,21,29]],
"liado": [15],
"editalo": [[20,30]],
"pons": [2],
"frecuentement": [[10,14,29,30]],
"setentrion": [6],
"copia": [29,2,21,20,32,[9,12,13,23,30]],
"aaa": [24],
"contemporari": [11],
"solari": [17,13],
"preferida": [[1,20,23,30]],
"manuai": [14,13],
"ambient": [[2,17,21],19],
"último": [[20,29],[4,8,17,25,26]],
"manual": [30,20,[4,7,8,9,32]],
"rcd": [29],
"aar": [6],
"amosaránsell": [29],
"kirundi": [6],
"funciona": [[13,19,20],[12,27]],
"comercialización": [7],
"alternativament": [14],
"axencia": [[10,30]],
"conter": [17,[21,28,30],[2,8,12,13,14,25,27,29]],
"malté": [6],
"significado": [[15,25]],
"causarán": [[15,30]],
"fase": [[19,29]],
"abc": [24],
"rcs": [29],
"conten": [[14,17,29]],
"navaho": [6],
"arriba": [29,[11,13,17,26,27]],
"recibido": [2],
"abk": [6],
"flexibilidad": [26],
"textuai": [10,26],
"textual": [29,22],
"abr": [20,[15,16,29],[1,2,13,21,28,30]],
"materi": [2,10],
"seleccionar": [8,[12,17],[20,25,27,30],[5,32],[3,13,18,21,23,26]],
"nomeado": [17],
"espazo": [24,26,[12,20],[8,30],[10,14,15,16,17]],
"preferido": [30],
"algoritmo": [[3,8]],
"referencia": [[10,29],17,[12,20,24,25,26,27,30]],
"isl": [6],
"demai": [[2,29]],
"iso": [6,32,22,31,[2,5,10,16,20,21,26,29]],
"contribuír": [32,9,[13,31]],
"fará": [15,[25,28,30]],
"moven": [[5,15]],
"taxico": [6],
"log.txt": [15],
"deterá": [17,21],
"prefiran": [29],
"asignarll": [2],
"bidireccion": [[8,14]],
"zul": [6],
"implícita": [7],
"controla": [23],
"modificábel": [16,32],
"ita": [6],
"finish": [2],
"ofrécens": [10],
"cambio": [[2,17],[26,29],[12,14,16,20],[1,5,8,13,15,21,23,25,30]],
"add": [2],
"acceda": [2],
"mencionada": [11],
"baseándos": [12,[10,17,20]],
"cambia": [12,[1,15,17,20,26]],
"xaponé": [26,[6,17,20,21,22,29]],
"tocará": [16],
"última": [20,[2,8,17,21],[9,13,29,30]],
"desactiva": [12],
"larouss": [30],
"acción": [20,8,[5,17,29]],
"reiniciars": [8],
"situado": [17,[15,30]],
"finalizador": [24],
"untar": [11],
"aliñador": [[21,32],31],
"farán": [2],
"benjamin": [7],
"benigno": [30],
"filters.conf": [21,17],
"enviar": [[19,29]],
"situada": [17],
"incluíndo": [30,[19,29]],
"invertida": [17,24],
"ofrécell": [28],
"porá": [20],
"sánscrito": [6],
"reducir": [[14,15,30]],
"usalo": [11],
"repositorio": [2,29,32,4],
"simplificación": [[14,15]],
"amarelo": [[20,30]],
"benigna": [15],
"afr": [6],
"indican": [15,30],
"saboga": [7],
"entidad": [25],
"targetlanguag": [12],
"directori": [2],
"indicar": [17,[14,29]],
"filtro": [12,23,32,[13,22],[25,30,31],[14,20],[2,8,18,26,29]],
"levara": [29],
"memoria": [29,10,30,32,17,20,[18,23,31],[2,21],[13,25,26],[14,19],[3,15,16,24]],
"cabo": [[2,17,20,21,28,29]],
"continuación": [9],
"properti": [[14,21]],
"título": [[14,25]],
"defecto": [[2,12]],
"durant": [[13,17],23,[10,16,18,21,26,29,30]],
"fisher": [3],
"editselectfuzzyprevmenuitem": [8],
"number": [12],
"traducindo": [30,[13,21,29]],
"isto": [17,29,30,[14,23,27,28],[2,19,20,21,22],[1,3,13,15,16,18,24,26]],
"copiar": [27,20,[30,32],[2,5,13,17,18,29,31]],
"procesado": [12,17],
"simpledateformat": [12],
"sempr": [[12,15,29],[13,16,26]],
"multiplataforma": [10],
"parágrafo": [12,26,[10,14,23],29],
"sinxela": [14,29],
"procesada": [20],
"gardarán": [23],
"quedará": [18],
"comentada": [8],
"script": [1,17,[13,32],28,[0,2,9,10,31]],
"comunment": [14],
"system": [29],
"seleccióneo": [17,[27,29]],
"credenciai": [2],
"atoparon": [30],
"nada": [24,[5,19],[0,13,16,30]],
"gramaticalment": [3],
"movelo": [26],
"khmer": [6],
"levaron": [2],
"locai": [2,29],
"querer": [26],
"local": [2,17,[9,14]],
"limburgué": [6],
"resum": [30],
"creador": [2],
"deixaras": [12],
"kwanyama": [6],
"escrito": [22,[1,9,19]],
"cree": [2,[21,29],[1,6,15,18,27,30]],
"segmenta": [30],
"escrita": [[21,27,30]],
"unha": [29,17,30,20,[2,26],[15,25],12,[1,10,14,18,19,21,24,28],[8,13,16,23],[0,5],27,[31,32],[6,22]],
"crea": [20,13,[1,2,12,14,15,26,29]],
"destacándoo": [16],
"resto": [[2,30],[12,21]],
"partida": [26],
"lento": [[20,21,29]],
"segmento": [20,30,29,28,25,5,26,[15,32],8,10,14,19,[12,23],18,[16,17],[1,21],[2,13,31]],
"inseriron": [25],
"cada": [20,12,2,[14,18,29],[19,30],[8,10,21,26,28]],
"aka": [6],
"futuro": [29,[2,13,18,20]],
"adxacent": [30],
"atop": [16,[25,29]],
"cita": [24],
"instalación": [17,[27,31],[1,32],[0,3,19,30],[10,13,21]],
"movers": [5],
"consecuentement": [[17,25]],
"tsonga": [6],
"levará": [[20,21]],
"tradúzao": [14],
"es_mx.aff": [27],
"configuración": [17,32,21,13,20,[23,27,31],30,[18,29],12,[0,1,3,16,25]],
"aprobous": [16],
"idéntico": [29,30,[17,25]],
"mode": [17,21],
"toolsshowstatisticsstandardmenuitem": [8],
"xunto": [23,12,[1,11,30]],
"modo": [17,21,14,32,20,29,[23,30],[2,10,12,18,26],[8,13,15,16,27,31]],
"usars": [17],
"umarov": [7],
"activou": [20],
"atopado": [17,[25,30]],
"alt": [5,[1,8,17]],
"persoal": [17],
"real": [[19,30]],
"típico": [17,29],
"persoai": [10],
"pregunta": [9,13],
"selecciónas": [30],
"unit": [29],
"guaraní": [6],
"etiquetar": [[20,28]],
"atopada": [30,[17,29]],
"atallo": [8,20,31,[10,32],[5,17,30],15,[2,6,7,9,14,16,23]],
"sobrescriba": [[12,30]],
"wildrich": [7],
"pinfo.list": [19],
"idéntica": [[8,15,24,28,30]],
"seleccionou": [20,[26,27]],
"amh": [6],
"completar": [13],
"unix": [20],
"rede": [[2,19],[9,10,17,27,32],[1,8,31]],
"fondo": [30,20],
"escritorio": [17,[25,30]],
"roh": [6],
"ron": [6],
"resolv": [2],
"and": [[7,9,13,17,19,21]],
"modifica": [[13,21,23,29]],
"bengalí": [6],
"basears": [10],
"ano": [[21,29]],
"minuto": [[2,10,18,20,21],[4,7,17,29,31]],
"ant": [17,[20,26],[15,29],[13,30],[0,2,3,5,8,14,18,19,21,24,27]],
"liars": [15],
"sintax": [26,[1,8],12],
"páxina": [9,[5,13,30],[1,8,14,17,20,26]],
"comentario": [30,16,12,32,[17,29,31]],
"sardo": [6],
"xunta": [15],
"sintan": [20],
"jnlp": [17],
"helplastchangesmenuitem": [8],
"omegat.ex": [17],
"xunten": [23],
"marcarans": [2],
"sourcetext": [30],
"confundir": [1],
"bloqueo": [17],
"recoñecerao": [2],
"atrá": [20,[8,30]],
"contraposición": [19],
"kuanyama": [6],
"comodín": [25,32,[12,31]],
"potenci": [15],
"moverá": [[23,30]],
"maximiza": [30],
"alá": [5,17],
"pechar": [30,20,[8,18]],
"english": [11],
"jar": [17,21,13,29],
"api": [19,17],
"jav": [6],
"editselectfuzzy2menuitem": [8],
"app": [17],
"marshalé": [6],
"implementada": [1],
"desambiguación": [19],
"aló": [5,14],
"capaz": [[12,15,20,21,29]],
"acto": [4],
"alex": [7],
"incluirán": [29,[18,25]],
"encima": [25],
"zulu": [6],
"introduzan": [9],
"nivel": [29,26,32,23],
"mellora": [9,[17,19]],
"ademai": [[11,16,17,25,29]],
"retirando": [30],
"gujarati": [6],
"feito": [[17,25]],
"editselectfuzzynextmenuitem": [8],
"seguida": [26],
"ara": [6],
"asistencia": [[9,14]],
"lingua-paí": [26],
"arg": [6],
"seguido": [24,[8,26],29],
"popular": [2,1],
"paypal": [9],
"malia": [25],
"portugué": [[6,17,27]],
"dispón": [30],
"art": [27],
"empregará": [17,27,[21,23]],
"obrigado": [19],
"comecen": [24],
"rtl": [14,32,31],
"presentado": [2],
"jdk": [17],
"call": [20],
"vergoña": [4],
"cale": [4],
"asm": [6],
"darll": [18],
"cargarans": [12],
"algo": [13,[10,17,23]],
"presentada": [29],
"dirixirs": [[13,28]],
"contida": [17],
"queira": [29,26,[2,13,16,18,25,27]],
"toolsshowstatisticsmatchesperfilemenuitem": [8],
"glosario": [16,32,30,20,31,[2,10],[8,18],13,[3,11,14,19,21]],
"run": [25,30,6],
"ata": [5,20,17,24,[10,18,23],[16,21,25,27,30]],
"resulta": [13],
"rus": [6],
"titlecasemenuitem": [8],
"editcreateglossaryentrymenuitem": [8],
"duro": [[17,20,29]],
"defectuoso": [29],
"simplificar": [[15,30]],
"ital": [15],
"mostrará": [15],
"secuencia": [15,26],
"represéntans": [12],
"bold": [15],
"nominativo": [3],
"messageform": [15],
"galego": [[6,30]],
"name": [12],
"adoptars": [29],
"inspirado": [1],
"leame.txt": [12],
"conteñan": [25,28,[15,16,26,29]],
"feita": [[10,28,30]],
"nominativa": [16],
"recurso": [14,[9,10]],
"completalo": [10],
"distinguir": [29],
"escribila": [14],
"android": [14],
"haitiano": [6],
"gustaría": [13],
"apoio": [[4,29,32]],
"ava": [6],
"ave": [6],
"corrixido": [[10,19]],
"xona": [6],
"capac": [12],
"reduciría": [29],
"sinxelo": [26,[4,17,27,31,32]],
"dinámica": [1],
"necesitaría": [22],
"estivesen": [22],
"empregara": [29],
"describir": [30],
"estean": [[5,29,30]],
"gárdeo": [[1,29]],
"target": [[1,13,18,20,32],[2,29]],
"elevado": [1],
"contido": [2,12,[29,30],[13,28],20,[16,17],[15,21],[1,9,10,11,14,18,22,25]],
"empregars": [14,[10,16],[1,8,13,17,18,20,25,28]],
"así": [29,[10,17,25,30],20,[2,12,15,16,26,27],[9,14,18,21,23,28]],
"aplicaras": [15],
"truco": [14],
"fabián": [7],
"config-dir": [[17,21]],
"envía": [19],
"ficheiro_descargado.tar.gz": [17],
"suxerirll": [10],
"crtl": [18],
"escribirá": [[17,20,29]],
"manter": [2,14,[4,12,15,17]],
"www.netrexx.org": [1],
"tibetano": [6],
"caso": [29,30,17,21,[2,10,15,23],24,[12,14,19,22,26],[3,5,16,25]],
"case": [[16,22,24]],
"primeira": [20,16,[17,24,30],[1,2,8,10]],
"cara": [20,29,26,8],
"obter": [[11,13,17,30]],
"deixará": [28],
"previament": [[2,20]],
"targettext": [30],
"aym": [6],
"conteñen": [[10,14,29],[13,22],[16,21,30]],
"estraño": [15],
"crearán": [20],
"comportamento": [32,28,[5,30],[17,20,29,31],[10,14],[8,12,13,15,22]],
"localizar": [[20,28]],
"mellorará": [19],
"temporalment": [30],
"cifra": [[17,30,32]],
"aaabbb": [24],
"implicación": [[2,13]],
"aze": [6],
"electrónico": [9,[13,30]],
"aforrando": [28],
"edittagpaintermenuitem": [8],
"relacionada": [30,[10,13,14,19]],
"relacionado": [10,[15,22,26,27,29,30]],
"conectar": [[2,29]],
"feminino": [3],
"bokmål": [6],
"curdo": [6],
"separan": [16],
"ábrese": [[20,30]],
"activan": [20],
"viewmarknbspcheckboxmenuitem": [8],
"unicod": [22,[16,24,31,32]],
"activar": [20,[12,23],[8,15,19,27,32]],
"procesaron": [15],
"tratan": [29],
"cuestión": [[2,15,23]],
"en-us": [29],
"especifiqu": [[17,29]],
"finalizada": [2],
"motu": [6],
"reproducida": [15],
"msgstr": [12],
"recoñec": [[10,28,29,30]],
"imaxin": [10],
"xuntan": [29],
"atributo": [12,[26,30]],
"katarn": [7],
"reciba": [21],
"delimitada": [16],
"faría": [[21,30]],
"turcomeno": [6],
"chamada": [[18,26],10],
"utilizar": [29,[27,30]],
"raíz": [29,17,[3,20,30]],
"codificación": [12,22,32,16,[10,14,19,30,31]],
"sequera": [15,14],
"utilizan": [12],
"proporcionando": [30],
"deberiamo": [14],
"important": [13,10,21,[1,4,14,17,18,19,29,30]],
"chamado": [17,29,[10,11,18,27]],
"ás": [[17,30],[12,13,29],[1,5,9,10,15,19]],
"omegat.project": [[2,13,17,32]],
"dirección": [14,17],
"targetcountrycod": [12],
"aceptará": [29],
"especificou": [25,27],
"solución": [[22,32],[29,31]],
"webstart": [17],
"flexíbel": [1],
"sae": [20],
"frisón": [6],
"ningún": [20,[8,12,16],[1,2,5,17,29,30]],
"sag": [6],
"inclúen": [[12,13,15,28,30]],
"subvers": [2,32],
"situalo": [16],
"primeiro": [20,[2,30],26,[9,15,17,25],[1,4,8,13,18,23,27,29]],
"inserida": [[19,25],[3,16,20,28]],
"san": [6],
"move": [[26,30],[20,28]],
"símbolo": [[16,24,30]],
"usada": [2],
"jpn": [6],
"reutilizala": [29],
"also": [32],
"neerlandesa": [0],
"resx": [14],
"comprobador": [24,31],
"inserido": [[14,19,20,30]],
"técnica": [[9,19]],
"alta": [19],
"detectada": [15],
"a123456789b123456789c123456789d12345678": [17],
"cuestionábei": [25],
"viewmarkwhitespacecheckboxmenuitem": [8],
"equivalent": [30,[11,13,17,20,29]],
"bad": [19],
"silenciosa": [21,17],
"circunstancia": [15],
"repiten": [30],
"lucen": [32],
"fóra": [2],
"bak": [[2,6,13]],
"bam": [6],
"bat": [17],
"indicará": [29],
"subtracción": [24],
"jre": [17],
"francé": [26,17,6],
"optionsfontselectionmenuitem": [8],
"enteira": [[17,26]],
"intención": [26],
"axustado": [30],
"aparición": [15],
"birmano": [6],
"translatedfil": [21],
"poderían": [4],
"aaron": [7],
"plano": [14,32,22,[20,28],[10,16],[1,2,12,15,30,31]],
"aprend": [[4,7]],
"terceira": [16],
"anális": [[19,20]],
"hebreo": [6],
"freebsd": [[13,24]],
"desviars": [15],
"rexx": [1],
"presentará": [18],
"amplament": [2],
"acabada": [30],
"terceiro": [30],
"enteiro": [[12,13,28]],
"see": [32],
"sei": [17],
"vito": [[7,31]],
"sen": [30,12,[28,29],[4,7],[1,13,16,17],[2,20,25,26],[18,19,23,32]],
"seq": [16],
"developerwork": [17],
"ser": [[10,29],26,[12,14],[8,15,16,17,20],[13,23],[11,18,28]],
"aberta": [25,20],
"seu": [17,29,[2,30],27,[12,20],26,[7,10,16,18,21,23,25],[1,4,9,13,14,15,19,22]],
"asígnas": [12],
"set": [[13,17,21]],
"contain": [16],
"n.n_windows_without_jr": [17],
"optionsrestoreguimenuitem": [8],
"castelán": [[6,27]],
"familiarizado": [2],
"igual": [29,28,[30,32]],
"silencioso": [[17,21]],
"fleurk": [7],
"empregado": [[2,12,13,14,17,30],[10,25,27]],
"explicación": [[0,2,17,21]],
"ordenada": [2],
"offic": [15,[12,14]],
"iguai": [17],
"corrixirs": [23],
"bel": [6],
"ben": [[16,26],[1,2,6,12,27]],
"reproducilo": [9],
"nepalí": [6],
"aberto": [19,[0,1,5,10,12,16,17,26,29,30]],
"revé": [23],
"projectsavemenuitem": [8],
"desprácea": [29],
"joel": [9],
"restauralo": [20],
"forzar": [2],
"simplificado": [10],
"participen": [2],
"debe": [17,[8,15],[21,27],[12,13,14,16,24],[2,29,30]],
"pptx": [14],
"ordenado": [29],
"empregada": [[13,14,23,24]],
"reflectir": [17,15],
"buscar": [25,20,8,[10,18,27,30,32]],
"autotexto": [8],
"informará": [29],
"restaurala": [30],
"adiant": [20,17,[2,24,29],[8,21,25,30]],
"cursor": [20,5,26,30,16],
"traducirá": [17,[18,21]],
"exclamación": [2],
"comprobará": [27],
"malabar": [6],
"xhosa": [6],
"inestimábel": [4],
"nomedoproxecto-omegat.tmx": [29],
"incorporen": [20],
"cambiando": [12],
"signo": [[2,30]],
"repetido": [30],
"axustar": [[23,30]],
"client": [2,29,[10,17,30]],
"sin": [6],
"desprazars": [[18,20],[26,30]],
"causará": [12],
"historia": [16],
"tiago": [7],
"mundo": [22],
"inserila": [30,[2,14,18,20]],
"combinars": [17],
"manterá": [[13,17]],
"desprazarao": [20],
"desprazaras": [5],
"conteña": [[25,29],[10,20,21]],
"falten": [[20,30]],
"confirmación": [20],
"gramaticai": [[0,10]],
"falta": [[15,30]],
"kalaallisut": [6],
"restant": [8],
"responsábel": [2],
"foundat": [7],
"bih": [6],
"descomprimilo": [11],
"bin": [13],
"terminolóxica": [16,30],
"gráfica": [21,17,12],
"apertium": [19,32,31],
"bit": [22],
"bis": [[6,24]],
"carón": [[8,12,17]],
"tabulador": [20,[8,16],30],
"projectopenmenuitem": [8],
"autom": [17],
"decim": [28],
"minimizalo": [30],
"obxectivo": [[10,29]],
"manters": [[9,15]],
"dálle": [30],
"toolsvalidatetagsmenuitem": [8],
"levan": [29],
"decid": [[23,29]],
"autor": [[7,30],[20,25]],
"levar": [[17,29,30]],
"traducius": [13],
"slk": [6],
"vaia": [[9,17,19,28,29]],
"agradecemento": [4,[7,10,31,32]],
"reducida": [29],
"john": [7],
"slv": [6],
"terminolóxico": [16],
"viewmarktranslatedsegmentscheckboxmenuitem": [8],
"presentación": [12],
"amba": [[17,19]],
"sme": [6],
"aliñación": [[17,21]],
"smo": [6],
"ambo": [21],
"contador": [30,[31,32]],
"divehi": [6],
"pojavni": [16],
"deix": [[2,26,30]],
"retroced": [5],
"editselectfuzzy1menuitem": [8],
"sna": [6],
"snd": [6],
"faltan": [30],
"evitars": [[26,30]],
"didier": [4,7],
"faltar": [4],
"molestars": [16],
"súas": [10,[2,5,13,17,20,24,26,29]],
"amosarans": [10],
"hide": [12],
"concordancia": [5],
"gráfico": [[13,17,21]],
"avaliación": [[19,30]],
"auto": [29,[22,32],12],
"teclado": [8,20,31,[5,10,32],30,[2,6,7,9,16,23,25]],
"soa": [[15,16,25]],
"siga": [17,[11,14,27,29]],
"vans": [13],
"notepad": [16],
"mesturar": [0],
"som": [6],
"posto": [16],
"podería": [30,[4,12,13,15,29]],
"son": [30,[13,29],[15,16,17,22],21,[2,12,14,23,28],[4,9,10,18,19,20,24,25,26,27]],
"axuda": [32,20,30,[4,8,31],[14,29],[18,21]],
"dele": [[13,19,28,30]],
"oracl": [17,8,12],
"empregamo": [25],
"inserirá": [[20,28,30]],
"gardarans": [2],
"dela": [15,30],
"sot": [6],
"spa": [6],
"configuralo": [20],
"codificado": [22,14],
"instantáneo": [30],
"lerán": [17],
"empregala": [29],
"relevant": [14,[10,13,15,17,20,21,30]],
"pedirá": [20],
"excepción": [26,32],
"boa": [30,29],
"bod": [6],
"mongol": [6],
"produc": [[17,29]],
"deixar": [[17,28,29]],
"entón": [29,17,[13,21,25],[12,18]],
"bos": [6],
"sqi": [6],
"vermello": [[2,12,15]],
"fouri": [7],
"total": [30],
"kal": [6],
"totai": [20,30],
"dend": [1,[2,10,17,19,30]],
"kan": [6],
"altament": [12],
"kas": [6],
"thoma": [7],
"kau": [6],
"kat": [6],
"opción": [12,20,[30,32],17,[28,31],[21,25],[8,15,19,26,27,29],[2,22,23],[0,13,14,16,24]],
"br1": [15],
"macro": [1],
"suxestión": [19,[20,27,28,29,30]],
"sra": [26],
"srd": [6],
"kaz": [6],
"consola": [21,17,32,31],
"chave": [[19,30],[10,17],[1,25]],
"illa": [6],
"control": [2,[8,20,29],[18,24,31]],
"informa": [17,[19,21]],
"no-team": [17],
"srp": [6],
"srt": [14],
"pretraducir": [29],
"srs": [26],
"asígneo": [29],
"cando": [29,[17,20,28],30,14,[15,21],[12,13,16],[10,23,26],[0,5,18],[1,19,24,25]],
"cancelara": [20],
"coherent": [26],
"específico": [[12,23],[17,21],14,[0,2,13,29,30,32]],
"herdado": [[14,29]],
"describirs": [26],
"environ": [17,13],
"optionsautocompleteglossarymenuitem": [8],
"bre": [6],
"específica": [[13,23],29,[10,12,14,24,26,28,30]],
"ssw": [6],
"titori": [10],
"desprazada": [20],
"herdada": [20],
"controlars": [[2,28]],
"desancora": [30],
"inclúa": [17,2],
"delimitando": [16],
"exemplo": [29,[17,30],12,[15,24],16,27,[2,21,22,25,32],[26,31],14,[11,19],[3,10,20,23,28],[0,1,8,13,18]],
"indicada": [8],
"deseñado": [14],
"formatar": [32],
"srª": [26],
"kde": [17,32],
"modificou": [8],
"indicado": [16],
"requirida": [17],
"execución": [19,[1,4,10]],
"sigan": [[4,26]],
"interes": [[10,21,29]],
"sub": [16],
"motor": [19,25],
"painless": [9],
"acceso": [17,[2,13],[1,11,30]],
"quedarán": [[13,23]],
"languag": [1],
"reformatado": [19],
"sun": [6],
"desenvolvemento": [[2,9],[17,21,32]],
"porto": [17,32],
"introdutorio": [4],
"sur": [6],
"estadística": [8],
"preguntars": [4],
"prefíres": [16],
"líen": [14],
"key": [[17,20]],
"necesaria": [19,[11,14,15]],
"decisión": [19],
"svg": [17],
"opcionalment": [[17,21,29]],
"svn": [2,32,[17,29,31]],
"escritura": [32,14,[10,31]],
"mostraras": [25],
"confirm": [[17,20]],
"necesario": [17,13,[14,30],[2,16],[11,15,20,24,25,27]],
"bug": [9],
"están": [20,17,11,[8,30],[12,13,15,16,18,29],[1,2,3,14,19,21,22,25,26,27]],
"fluxo": [[13,30]],
"reuniu": [13],
"bul": [6],
"swa": [6],
"deixen": [12],
"presentarall": [18],
"clasificar": [30],
"editreplaceinprojectmenuitem": [8],
"swe": [6],
"asignado": [8],
"aniñars": [15],
"desexado": [17,[15,19,20,29]],
"selección": [20,28,[8,32]],
"express": [12],
"asignada": [17],
"dest": [13,[18,30],[10,11,16,21,29]],
"baséas": [2],
"contexto": [30,[10,15,19,20]],
"reservándoo": [27],
"cinco": [29],
"tímido": [[24,31]],
"montón": [4],
"fábrica": [13],
"esqueceu": [[11,30]],
"variant": [29,[12,24]],
"declaro": [4],
"enviou": [19],
"gotoprevioussegmentmenuitem": [8],
"fragmento": [[1,15]],
"parec": [15],
"predeterminada": [12,[22,26,30],[13,14,20,29,32],[2,31],[8,16,23,24,25,28]],
"gotopreviousnotemenuitem": [8],
"aplicación": [17,[13,14,32],27,[7,10,21,31],[11,16,20,22,30]],
"editredomenuitem": [8],
"uilayout.xml": [13],
"inici": [17,[2,9,18,29]],
"khm": [6],
"predeterminado": [8,12,20,17,29,[13,14,21,22,24,25,28,30]],
"recorda": [20],
"cantidad": [17],
"desd": [17,13,29,[20,26,32],[2,8,30,31],[3,10,12,14,24,25]],
"learned_word": [32],
"errático": [13],
"idea": [30],
"orientada": [1],
"hiri": [6],
"ficheiros-de-configuración": [[17,21]],
"esquina": [30],
"kik": [6],
"kim": [7],
"kin": [6],
"sobrepor": [30],
"informado": [9],
"kir": [6],
"lakunza": [7],
"xestionarán": [29],
"normal": [17,[5,12,20,23,25]],
"guido": [7],
"sras": [26],
"ortografía": [27,[31,32]],
"comprobado": [29],
"tratarán": [20],
"cambiarán": [[23,28]],
"desexada": [30],
"license.txt": [13],
"animamo": [14],
"desprazala": [18],
"runtim": [17,13],
"potent": [25],
"luba-katanga": [6],
"diferent": [29,30,[12,14,20],[1,10,15],[2,3,13,17,23,26,27]],
"avar": [6],
"ruanda": [6],
"poderán": [10],
"comparativo": [3],
"enfrontars": [22],
"filenam": [12],
"pular": [6],
"activando": [14],
"saca": [17],
"parcialment": [[19,30]],
"española": [19],
"roam": [13],
"suceda": [26],
"modifiqu": [8,[25,30]],
"certa": [[13,15,17,29]],
"amor": [4],
"sabe": [29],
"suazi": [6],
"gotosegmentmenuitem": [8],
"interno": [[15,20,23,29]],
"eventualment": [18],
"deixou": [20],
"rango": [24],
"executaras": [1],
"pódese": [[15,16,21,30]],
"interna": [[20,30],23],
"xx_yy.tmx": [29],
"amos": [[14,25,30]],
"lembrar": [18,31],
"usando": [[8,30]],
"lembr": [18],
"helpaboutmenuitem": [8],
"concida": [25],
"instancia": [17,25],
"limitar": [[17,25]],
"mandelbaum": [7],
"impreso": [30],
"leam": [12],
"vosted": [28,[12,13,17,23,30]],
"regular": [24,25,32,[12,15],[10,21,26,31],17,[11,14]],
"función": [[20,30],[15,27],[10,28],1,[2,11,16,18,19,23,25]],
"sobrescrita": [17],
"certo": [29,[10,12,19,26]],
"indicación": [14],
"eslovaco": [6],
"token": [3,32,30,[0,2,10,31]],
"elemento": [8,17,[16,20,30],12,[13,14,21]],
"omegat.log": [13],
"dubid": [[13,21]],
"najlepših": [3],
"terminoloxía": [[10,29,30],[25,32]],
"pasarán": [[26,29]],
"rexístres": [[2,9]],
"árabe": [6],
"parcial": [[8,28],[20,30],29,32],
"estarán": [[16,19,29,30]],
"kom": [6],
"kon": [6],
"corresponda": [22],
"kor": [6],
"tab": [16],
"pretraducirá": [29],
"información": [[17,30],19,[13,14,29],[10,15],[8,20,21],[16,23],[2,6,9,11,12,22,24,25,28]],
"tah": [6],
"parciai": [32,[29,30],28,20,[14,15,18]],
"tag": [14],
"tai": [6],
"tal": [[7,13,17],[10,11,19,20,21,26,30]],
"tan": [14,[1,10,16,25,30]],
"rodapé": [12],
"tam": [6],
"administrador": [2],
"variedad": [[14,19]],
"tar": [13,17],
"tat": [6],
"pódens": [30],
"onli": [12],
"filtrar": [25,31],
"calquera": [29,17,24,[16,25,30],2,[7,12,13,18,21],[5,8,9,10,14,15,19,26,27,28]],
"suxerida": [30],
"persoa": [2],
"afectado": [15],
"individuai": [25],
"projectreloadmenuitem": [8],
"aproximada": [30],
"detrá": [5],
"safe": [25],
"acabado": [30],
"configúrans": [14],
"decidimo": [2],
"atractivo": [10,31],
"sail": [19],
"lingüística": [19,11],
"servizo": [19,17,2,20,[10,13,30]],
"winrar": [11],
"tbx": [16,32],
"vogal": [24],
"cal": [29,[2,12,19]],
"leen": [29],
"saia": [[20,29]],
"cat": [3,6],
"antidistorsión": [17,32],
"duser.countri": [17],
"tcl": [28],
"tck": [28],
"eliminación": [15,32],
"preguntará": [17],
"sitú": [[27,29]],
"canto": [[4,14,20,30]],
"colaborador": [4],
"comprobalo": [22],
"informar": [9,32,[22,31]],
"typo3": [14],
"align.tmx": [17,21],
"construción": [24,21],
"desexar": [[17,28]],
"argumento": [17,32,[19,29]],
"liña": [17,13,[12,21,24],[8,32],[5,26,29],[4,10,14,15,19,30,31]],
"comprobala": [15]
};
