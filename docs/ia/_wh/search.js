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
 "appendix.Scripts.inOmegaT.html",
 "appendix.TeamProjects.html",
 "appendix.Tokenizers.inOmegaT.html",
 "appendix.acknowledgements.html",
 "appendix.keyboard.html",
 "appendix.languages.html",
 "appendix.legal.notices.html",
 "appendix.shortcut.custom.html",
 "appendix.website.html",
 "chapter.TaaS.html",
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
 "chapter.searchandreplace.html",
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
 "Appendix E. LanguageTool plugin",
 "Appendix F. Scripts",
 "Appendix C. Projectos de equipa OmegaT",
 "Appendix D. Tokenizers",
 "Appendix J. Recognitiones",
 "Appendix B. Vias breve del Claviero in le editor",
 "Appendix A. Linguas - lista del codices ISO 639",
 "Appendix I. Advertentias legal",
 "Appendix H. Personalisation del vias breve",
 "Appendix G. OmegaT sur le web",
 "Usar TaaS in OmegaT",
 "Re OmegaT - introduction",
 "Dictionarios",
 "Filtros del file",
 "OmegaT Files e Plicas",
 "Files a render",
 "Operar con le texto formattate",
 "Glossarios",
 "Installar e facer fluer OmegaT",
 "Apprender a usar OmegaT in 5 minutas!",
 "Traduction automatic",
 "Menu e vias breve del claviero",
 "Subjectos miscellanee",
 "Operar con le texto simple",
 "Proprietates del projecto",
 "Expressiones regular",
 "Cerca e replacia",
 "Recercas",
 "Segmentation del texto fonte",
 "Corrector orthographic",
 "Comportamento del Modifica",
 "Memorias de traduction:",
 "Le interfacie del usator",
 "OmegaT 3.5 - Guida del usator",
 "Index"
];
wh.search_wordMap= {
"cancel": [21],
"fenestra": [34,32,21,33,13,27,19,18,[2,26],22,[16,29,31],[1,17,24,30],[23,28],[3,8,10,14,15,20]],
"don\'t": [14],
"prepara": [[17,30]],
"hall": [7],
"percurso": [[18,32]],
"tel": [6],
"predefinit": [21,13,[17,20,30,32]],
"disponibilit": [20],
"avant": [21,[8,15,32]],
"info.plist": [18],
"passato": [31],
"antonio": [7],
"lege": [[9,14,16,18]],
"tex": [15],
"letzeburgesch": [6],
"fuzzi": [31,[30,32,34]],
"persian": [6],
"reman": [31,18],
"latino": [6],
"left": [30],
"kua": [6],
"confidentialit": [[2,21]],
"avid": [25,33],
"maldivian": [6],
"rola": [30],
"kur": [6],
"titulo": [[21,32],[15,27]],
"reusat": [[15,31]],
"reusar": [31,34,33],
"dgoogle.api.key": [18],
"elect": [14],
"ces": [6],
"edittagnextmissedmenuitem": [8],
"automatisar": [13],
"gikuyu": [6],
"tgl": [6],
"tgk": [6],
"modificar": [21,34,32,[13,18],24,[5,11,20,30],[15,16,26,28,33]],
"quiet": [[18,22]],
"after": [28],
"connect": [2,20],
"sami": [6],
"penalit": [31,16],
"es_es.d": [29],
"bambara": [6],
"quadrato": [24,28,[13,30],[16,18,21]],
"tha": [6],
"rolamento": [30],
"the": [7,14,[17,18],[31,32],34,[1,21,24,33]],
"laborant": [[18,31]],
"incit": [15],
"preparar": [[13,19,34],[2,11,33]],
"projectimportmenuitem": [8],
"frances": [28,18,[0,6]],
"information": [18,[11,15,21],[2,5,6,12,22,30]],
"imag": [18,14],
"scriber": [30,[13,16,18,21,24]],
"amabil": [25],
"applica": [32],
"consignar": [2],
"accessibil": [32,[14,18]],
"preposition": [4],
"currsegment.getsrctext": [1],
"implement": [[17,20],1],
"tir": [6],
"cha": [6],
"export": [31,[17,21,30],15],
"che": [6],
"practic": [32],
"reduc": [[15,16],[3,32]],
"transtip": [21,[8,32],17],
"check": [13],
"checo": [23],
"chv": [6],
"notabil": [11,33],
"chu": [6],
"xxxx9xxxx9xxxxxxxx9xxx99xxxxx9xx9xxxxxxxxxx": [18],
"resolut": [2,31],
"rolo": [11],
"prendera": [13],
"fr-fr": [29],
"claudent": [32],
"deveni": [31,[15,32]],
"conservar": [16],
"besonia": [15,2],
"ensur": [14],
"fortun": [32],
"ndonga": [6],
"madlon-kay": [7],
"dexter": [32],
"disco": [[2,18,21,31]],
"disactivar": [21],
"lent": [[21,22]],
"primari": [18],
"besonio": [19],
"termina": [[25,31]],
"xmxzzm": [18],
"milliari": [31],
"webster": [12,34,[32,33]],
"termino": [17,21,[2,27,32],[5,8,10,13,19]],
"effortio": [9],
"validar": [[19,32]],
"cjk": [27],
"duplicar": [16],
"monstra": [21,32,15,27,[11,22],[8,12,16,17,20,23,30,33]],
"cargant": [2],
"continerea": [13],
"sape": [18],
"cargar": [[1,13,20,21,30,31]],
"syntax": [28,8,[13,24]],
"cargat": [31,[13,17,18,19,22,32]],
"contrario": [[20,27]],
"essayar": [[15,25]],
"empti": [31,[18,30]],
"valida": [21,[8,16,19],[15,30,33]],
"monstrara": [21,2,[23,27]],
"irrelevant": [22],
"n.n_source.zip": [18],
"bloco": [25,28,[9,33]],
"presta": [[16,22]],
"lepo": [3],
"spolski": [9],
"financiari": [34],
"longitud": [32],
"reveni": [[18,22]],
"mal-traduction": [30],
"scandit": [15],
"implication": [2],
"traducit": [13,[18,21],32],
"lepa": [3],
"presso": [20],
"block": [22],
"propon": [11],
"tms": [31,32,[20,27]],
"tmx": [31,34,18,32,[20,21],[14,22],[11,27]],
"e.g": [17,31,[0,18,29,32]],
"avanti": [[11,27]],
"inter": [32,16,[15,21],[30,31],[8,13],[2,5,10,14,17,18,20,22,25]],
"nl-en": [31],
"securit": [18],
"integ": [13],
"intel": [18,[33,34]],
"expandit": [20],
"fr-ca": [28],
"mainmenushortcuts.properti": [8],
"angular": [17],
"blocat": [32,18],
"convertit": [[13,15,24]],
"cmd": [21,5,15],
"propri": [31,[30,32],[2,4,11,18,21,23,25,27,28]],
"project_name-level1": [[14,31]],
"gotohistorybackmenuitem": [8],
"project_name-level2": [[14,31]],
"quanquam": [24],
"parametro": [18,[20,22],15,[4,14,23,31,34]],
"meridion": [6],
"save": [18],
"estévez": [7],
"project-save.tmx": [31],
"changedd": [32],
"ton": [6],
"technica": [20],
"powerpc": [18],
"have": [32],
"cambiamento": [31,[2,26]],
"disveloppar": [25],
"schermo": [[9,18,22]],
"product": [11],
"poner": [[20,21]],
"question": [2,[9,24,31]],
"quam": [[15,28],25,[16,20,30]],
"sati": [31],
"cetera": [15,28,[16,17,27,32],[2,12,13,18,24]],
"qual": [13,[11,18],31,32,[20,22,24],[14,16,17,19,28],[0,4,10,12,21,27]],
"expon": [32,[11,21]],
"order2": [31],
"order1": [31],
"instal": [18,14,29,[0,33],[1,2,3,21,22,32,34]],
"con": [21,32,18,31,11,14,13,[15,17,34],[2,27],[16,24,30],20,[8,22,23,25],[29,33],[1,9,12],[19,26,28],3,5],
"minor": [[15,28,32]],
"cos": [6],
"auto-synchronisar": [27],
"cor": [6],
"ossetian": [6],
"remot": [[18,21]],
"adopta": [18],
"omegat.sourceforge.io": [18],
"function": [21,[16,32],[1,9,29,30],[11,12,17,19]],
"ordin": [28,31,[16,27,32],[2,4,21]],
"pipe": [24],
"lao": [6],
"telugu": [6],
"quita": [21],
"lat": [6],
"las": [[11,19]],
"lav": [6],
"tra": [21],
"subscript": [14],
"contator": [32,[33,34]],
"detenera": [19],
"cpu": [32],
"revert": [14],
"monstrant": [32],
"revers": [18,25],
"translat": [20,34,[13,18,33],11,[1,14,15,29,31,32]],
"condition": [31],
"mantenimento": [2],
"aviso": [24,[21,32]],
"dispacchettar": [18],
"gratia": [4,[3,15,16,30,31,33]],
"occurrent": [9],
"tsn": [6],
"interesar": [20],
"nihil": [25,5,[17,19,20,21]],
"tso": [6],
"leva": [34,[15,24],[3,20,28,32,33]],
"cre": [6],
"chmod": [14],
"chihiro": [7],
"recarga": [21,[27,31,32]],
"gnome": [18],
"probabil": [[20,22,31,32]],
"consiliabil": [28],
"quer": [[16,22,27,31]],
"flue": [18],
"commento": [[17,32],13,34,27,[8,18,31,33]],
"accopul": [16],
"scribit": [[21,23],8],
"crescent": [20],
"proba": [[18,31]],
"devenira": [20],
"analys": [[20,21,25,31]],
"significar": [32],
"ttx": [31,34],
"detalio": [32,[15,21,31],[4,18,20,24]],
"ligamin": [32,9,16,[12,13,14]],
"appdata": [14],
"prev": [[0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30,31,32,34]],
"supplit": [32,[2,11,14,28]],
"csv": [15,17],
"quia": [31,[13,19]],
"tuk": [6],
"valor-clav": [13],
"carga": [31,[1,15,18,19,30,32]],
"apparit": [27],
"concern": [23,32],
"displicar": [[1,21]],
"simultaneement": [[2,32]],
"tur": [6],
"stato": [32,21,22,[14,15,16,18,24,30,31]],
"tuv": [31],
"lep": [3],
"sotho": [6],
"lepš": [3],
"prest": [[2,11,32]],
"articulo": [[9,17,18,27]],
"les": [[21,27]],
"lev": [7],
"dock": [18],
"agentia": [[11,32]],
"element": [21],
"ctr": [16],
"totevia": [[15,31]],
"dmicrosoft.api.client_secret": [18],
"creativ": [[11,28]],
"unix-simil": [21],
"exequit": [18,[1,2]],
"swahili": [6],
"dextra": [34,5,15,[28,29,33]],
"producera": [[13,16],32],
"filenameon": [32],
"māori": [6],
"ctrl": [21,8,5,32,34,19,27,[15,30],[16,31],[14,17,20],[1,12,24,26]],
"mykhalchuk": [7],
"document": [7,[13,14],[4,8,34],[11,15,16,18,22,25,31,32,33]],
"twi": [6],
"clamat": [18],
"distribu": [31],
"comenciamento": [5],
"polaco": [0],
"construit": [20],
"frontiera": [25],
"tamil": [6],
"superfici": [20],
"scenario": [32],
"dextro": [32],
"graphic": [[14,22],18],
"creation": [[17,28,34],[2,14,24,33]],
"attacc": [12],
"initiara": [18],
"resourc": [18],
"briac": [7],
"erga": [22],
"xx_yy": [[13,31]],
"reportara": [[30,31]],
"docx": [[13,15],24],
"txt": [23,17,15,[13,32]],
"prestar": [3],
"clar": [31],
"ergo": [[18,28,31],[14,29,30,32]],
"deliber": [16],
"thing": [14],
"definit": [8,[31,32],[0,2,11,13,14,15,20,23,29,30]],
"indonesiano": [6],
"lituano": [6],
"anno": [[22,31]],
"definir": [28,[13,21,24],[22,25,32]],
"lib": [14],
"non-cifra": [25],
"ojibwa": [6],
"lin": [6],
"lim": [6],
"russo": [[6,10,18,22,23]],
"trnsl": [18],
"entit": [27],
"lit": [6],
"viewdisplaymodificationinfoselectedradiobuttonmenuitem": [8],
"omegat.tmx": [[14,31]],
"index.html": [[18,32]],
"entir": [30],
"actual": [21,32,[27,30],[8,13,14,15,22,31],[4,16,17,18,20,28,34]],
"erig": [20],
"doubt": [14],
"renunciar": [4],
"verbal": [21],
"diffrevers": [32],
"sumer": [[14,22]],
"fula": [6],
"cym": [6],
"si-appel": [31],
"page": [[14,18]],
"convertimento": [[20,34]],
"romaniano": [0],
"paga": [[16,18]],
"aspecto": [11,19,[13,16,33]],
"fixat": [[15,17],[16,18,21,32]],
"fixar": [21,[8,16]],
"previ": [21,[8,31],28,[4,11,22,32]],
"chewa": [6],
"quod": [9],
"construer": [18,[20,34],33],
"precio": [[20,32]],
"hesitar": [22],
"project.gettranslationinfo": [1],
"salvamento": [21,[2,8]],
"principio": [[5,25]],
"tamen": [[4,15,16,18,22,23,24,31,32]],
"initiava": [[18,24,31,32]],
"convention": [31],
"desirar": [31,[2,18,21]],
"servicio": [20,18,10,[2,21],[11,14,32,33]],
"start": [18,34,33],
"mymemori": [20,34,33],
"abkhaz": [6],
"smolej": [[7,33]],
"equal": [31,[18,30],[21,34]],
"replacia": [26,[21,27,34],[25,31,33]],
"grammaticament": [3],
"recipient": [15],
"cliccat": [21,5],
"cliccar": [21,[18,32],[1,13,14,19,20,29]],
"kulik": [7],
"faller": [[16,18,19]],
"demanda": [[9,18],2],
"comprim": [13],
"optionsalwaysconfirmquitcheckboxmenuitem": [8],
"tmxs": [21,[8,31,32]],
"prender": [29,[8,31,32]],
"prevalera": [31],
"preter": [25],
"rescrib": [13],
"nigrato": [32,[10,16,17]],
"claudera": [21],
"disfac": [21,8],
"necun": [[8,21,31]],
"redig": [27,21,[13,18,22,32]],
"enter": [21,5,8,[16,18,30]],
"exponit": [17],
"applic": [[14,15],18,[11,34],[20,28,33],[7,21,22,29],[0,16,17,23,24,25,31,32]],
"bidi": [15],
"projectteamnewmenuitem": [8],
"offerit": [[2,20,21,32]],
"initialment": [30,[2,14,18,20,31]],
"memori": [18],
"nigrat": [[13,15,32]],
"afar": [6],
"revenir": [[19,32]],
"anqu": [[21,29,32]],
"quot": [25],
"traina": [18],
"cessat": [[8,21]],
"godfrey": [7],
"domo": [[18,22]],
"avestan": [6],
"log": [[8,14,16,21]],
"kannada": [6],
"providit": [12],
"configurava": [22],
"lor": [31,15,32,[2,28],[11,13,20,24,27],[1,12,16,18,21,25]],
"lot": [4],
"servitor": [2],
"los": [31,32,[13,14,16,30],[2,11,15,19,21,24,27,28,29],[10,18,20,26]],
"arrangiamento": [13,21,[16,18,30,32],[2,15,23,31]],
"comporta": [[5,30,32]],
"consult": [11],
"contradiction": [4,[21,32]],
"necessitara": [18,[15,17,28,31,32]],
"n.n_windows_without_jre.ex": [18],
"amhar": [6],
"initiar": [18,[8,11,20,22,28,33,34]],
"orphan": [31,34,[27,28,32]],
"clic": [32,[14,18,21,27]],
"prof": [28],
"camproj": [15],
"campana": [25],
"discaten": [0],
"re-assignar": [5],
"dmicrosoft.api.client_id": [18],
"disimballar": [18],
"prevalent": [24],
"ubi": [[18,31,32],2,[8,21,29],[10,11,13,14,15,16,19,22,24,26,27,28]],
"config-fil": [18,22],
"interessant": [[11,22,31]],
"suppler": [28],
"non-segment": [13],
"dan": [6],
"interferentia": [2],
"derecto": [[2,21]],
"dar": [32],
"cognosc": [[2,11]],
"derecta": [[18,21]],
"puncta": [32],
"intra": [[2,16,17,31],18,[5,11,14,15,21,32]],
"consuet": [5],
"system-user-nam": [13],
"permittent": [11],
"recognoscera": [2],
"format": [11,[13,16]],
"wolof": [6],
"particular": [[7,22],[13,15,16,18,28,29,31]],
"pausa": [29],
"ajorn": [32],
"actualisa": [2],
"console.println": [1],
"plure": [32,31,11,[18,27],[14,21,30],[13,15,33],[0,1,2,4,17,20,22,25,34]],
"elevar": [31],
"cambiat": [24,[18,31],[15,16,17,22,27,28]],
"kurdish": [6],
"puncto": [[1,9,11,14,15,21,28,32]],
"cambiar": [32,24,[13,15,18,21],[5,8,14,20,22,23,27,28]],
"microsoft.api.client_id": [20],
"parv": [11,21],
"part": [32,21,14,[13,20,23],[11,16,28],[8,10,15,22,29,31]],
"autonom": [[18,25]],
"permittera": [10],
"pare": [13,[16,20],34,31,[2,22,32]],
"executabil": [[14,18]],
"tend": [16],
"leender": [7],
"synonym": [11],
"hiroshi": [7],
"imponit": [[13,27]],
"suffixo": [3],
"tene": [[14,32]],
"project_files_show_on_load": [32],
"cancellant": [16],
"ltr": [15,34],
"optionsexttmxmenuitem": [8],
"de-formatt": [20],
"ltz": [6],
"build": [22],
"lub": [6],
"tonga": [6],
"marketplac": [20,18],
"lug": [6],
"possibil": [32,15,[16,24],[18,31],[3,10,17,20,30],[2,4,5,8,13,14,19,21,26]],
"commutar": [15,[8,21]],
"finalis": [20],
"entries.s": [1],
"ident": [[30,31],32,13,[14,16,21],[2,8,12,15,17,18,22,25,27]],
"addit": [31,14,18,[1,17],[22,32],[2,11,13,16,21,25,27,28]],
"del": [32,34,21,13,31,18,2,33,14,8,11,16,17,24,15,[22,28],5,[20,29],27,30,3,23,[9,19],25,[4,10],[1,6],[0,12],26,7],
"pauc": [[18,19],[15,25,33]],
"gotonextuntranslatedmenuitem": [8],
"targetlocal": [13],
"path": [18,22],
"deu": [6],
"abbrevi": [[1,2,28]],
"consigna": [2],
"allsegments.tmx": [18],
"includit": [13,31,[14,32],[0,2,3,16,20]],
"grati": [19],
"percentag": [32],
"sugger": [11],
"helpcontentsmenuitem": [8],
"descript": [[2,4,8,18,21,31,32]],
"initialcreationid": [32],
"projectaccessdictionarymenuitem": [8],
"habilita": [13,[24,31],[22,25,30,32]],
"maltes": [6],
"discargamento": [[9,34]],
"cacographia": [29],
"consecut": [[11,16,21]],
"uig": [6],
"non-frangibil": [21,8],
"proxim": [32,[8,30]],
"contrasigno": [2,21],
"term": [[7,17,32],20],
"celat": [21],
"dotx": [15],
"pre-processar": [31],
"right": [30],
"duden": [32],
"changedid": [32],
"stage": [20],
"miura": [7],
"under": [7],
"accordo": [[18,32]],
"spotlight": [18],
"did": [28],
"non-spatio": [25],
"die": [[20,31],22],
"tswana": [6],
"imper": [1],
"reserv": [29],
"catlik": [3],
"signat": [18],
"murray": [[4,7]],
"datamarket.azure.com": [20],
"adder": [18,32,[2,13],[8,9,14,15,17,19,21],[1,20,22,24,29,31,34]],
"habil": [13,[15,16,18,20,21,29,30,32]],
"insula": [6],
"dir": [18],
"latex": [15],
"strictement": [16],
"div": [13,6],
"later": [[2,7],[13,16,18,20,23,28,29]],
"unificar": [[15,16]],
"assignar": [2,[8,18]],
"grosso": [32],
"legal": [34,7,[4,8,11,33]],
"viewfilelistmenuitem": [8],
"recognosc": [11,[15,19,30,31,32]],
"ukr": [6],
"info": [8,10],
"fallera": [31],
"test": [18,22],
"mittmann": [7],
"omegat": [18,34,[14,31],22,11,33,15,32,[2,19],9,[20,21],13,[8,23],[24,30],16,[3,17,29],[1,7,10],[0,4,27,28],12,[5,25,26]],
"xxxxx": [10],
"ull": [31,[0,5,15,18]],
"kanuri": [6],
"final": [[13,21],[29,34],32,31,[16,19,20,24,30],[11,17,18],[4,15,27]],
"my-project-dir": [22],
"variet": [[15,31]],
"requirit": [18,14,[12,20]],
"virtual": [1],
"servar": [[2,15]],
"ignora": [[13,14]],
"console-align": [18,22],
"esseva": [31,[18,21],[14,16,17,32],[1,5,20,22,24,28],[0,4,15,30]],
"affligit": [16],
"ms-dos": [18],
"servat": [[18,22]],
"projectopenrecentmenuitem": [8],
"reguardant": [21],
"jean-christoph": [[4,7]],
"postquam": [19,22,[14,18,28]],
"human": [20],
"restaura": [[21,32]],
"inser": [21,[8,30,32,34],[5,27],[2,17],[13,15,20,26,28]],
"henri": [7],
"relax": [[8,15]],
"retro": [5,21,[8,15,27,32]],
"recommenda": [2],
"una": [27],
"permission": [18],
"applicabilit": [28],
"und": [29],
"project_save.tmx.temporari": [[22,31]],
"stirp": [3,34],
"grand": [[22,29]],
"une": [[17,19,20]],
"kikuyu": [6],
"partit": [32],
"uno": [16,[21,31,32],[26,27],[11,18,22],[3,10,13,17,20,24,25,30]],
"faction": [[5,24]],
"quechua": [6],
"partir": [[11,32],[14,18,31]],
"interpret": [23],
"editoverwritemachinetranslationmenuitem": [8],
"relat": [21,[11,20,31,32]],
"ingreek": [25],
"es_es.aff": [29],
"convers": [34],
"ignor": [13,[16,18,24],[5,8,31,32]],
"limburgish": [6],
"necess": [[15,16,21,27,29,32]],
"convert": [[15,30],23],
"construct": [25],
"disparera": [29],
"pseudo-rendit": [31,[33,34]],
"cadent": [24],
"haber": [27,[2,13,17,32],[9,16,18,22,24,26,30,31]],
"pojavnem": [17],
"projectexitmenuitem": [8],
"pt_pt.arr": [29],
"loca": [19],
"scribera": [18],
"lock": [18],
"leger": [13],
"text": [13],
"loco": [2,18,[9,21,31],[14,17,19,20,28,29]],
"editregisteruntranslatedmenuitem": [8],
"reguardo": [[18,31,32]],
"reimplaciamento": [13],
"inadvertent": [14],
"scribibil": [17,[8,21,34]],
"mitt": [29],
"disponibil": [18,21,32,2,[8,13,22,29],31,[1,11,23,25,27,28]],
"maco": [18],
"perder": [[14,32]],
"mitter": [[21,31]],
"invalid": [18,31],
"contento": [2,[8,13],[18,31,32],21,14,[17,30],[16,22,27],[11,12,15,20,23]],
"projecto-specif": [14,[2,24]],
"doc": [[15,32],14],
"doe": [18],
"synchronisa": [18],
"senso": [[29,31]],
"emittit": [[13,15,18,31]],
"server": [18,2],
"magi": [19,[9,20,28,33]],
"paramet": [18],
"mac": [18,5,8,[14,21],[2,11,15,19,33,34]],
"mention": [[2,18]],
"desinentia": [[18,22]],
"ponent": [[8,31]],
"mah": [6],
"senti": [[21,22]],
"lepša": [3],
"lexic": [20],
"mal": [16,24,6],
"man": [18],
"lepši": [3],
"mar": [6],
"superposit": [16,34,33],
"may": [27],
"anteriorment": [21],
"urd": [6],
"shona": [6],
"url": [2,[13,18,21,29,32]],
"megabyt": [18],
"uppercasemenuitem": [8],
"viewmarkuntranslatedsegmentscheckboxmenuitem": [8],
"desidera": [[13,18],[14,20,21,26,31,32]],
"faroes": [6],
"www.omegat.org": [9],
"usa": [18,32,21,[13,20,31],8,[3,11,22,24],[2,5,12,14,17,19,23,28,29]],
"opera": [26,[2,4,5,11,13,14,21,23,24]],
"mail": [14],
"use": [7,32,[18,21,30]],
"anglese-catalano": [20],
"usd": [20],
"recognition": [4,[7,11,33,34]],
"manfr": [7],
"completement": [16],
"uso": [[22,33],[18,34],[0,1,12,17,25],[2,11,20,21,28,29,32]],
"omegat.jar": [14,[1,22,31]],
"commut": [15],
"omegat.app": [18],
"conveni": [32],
"usr": [18],
"legit": [31,18],
"logo": [14],
"commun": [2,[17,31],[11,19,27,32,33,34]],
"alter": [34,18,32,31,21,16,[13,15],[11,22],33,14,[2,30],[9,17,19,27],[0,3,4,5,6,12,20,25,28,29]],
"lista": [13,[11,21,34],[6,32],[1,14,19,20,28,31,33],[0,5,15,17,18,22,23,25,27,29,30]],
"progresso": [32,[2,22,30]],
"libro": [20],
"macedon": [6],
"ideal": [[1,21,32]],
"manes": [6],
"utf": [23,17],
"immagazinar": [29],
"signif": [[13,28]],
"servic": [20],
"avantiar": [[8,21]],
"null": [21,17,[22,31],[18,32],[3,19,20,23,27,29]],
"obtenit": [20],
"portuges": [[6,18,29]],
"deposita": [9],
"dsl": [12],
"servir": [[17,31,32]],
"intraducibil": [13],
"symbolo": [[21,25]],
"prend": [2],
"qualifica": [9],
"dtaas.user.key": [10],
"openoffice.org": [29],
"nivello": [31,28,[24,34],[2,14]],
"capitalis": [27],
"dtd": [15],
"malagasi": [6],
"make": [28],
"recargar": [[8,13,21,28]],
"projectcompilemenuitem": [8],
"console-transl": [[18,22]],
"structura": [14,28,31],
"clicca": [18,13,2,26,[1,9,17,24,27,28,32],[10,16,19,21,29,30]],
"constantement": [20],
"gratuit": [18,[2,20],[11,12,29,33]],
"gotonextuniquemenuitem": [8],
"conform": [27,[31,33]],
"bosnian": [6],
"materia": [31],
"dum": [[11,14,21],[13,20,24]],
"wordart": [13],
"princip": [[32,34],[21,33],31,22,[8,11,13,28],[1,14]],
"scribent": [18],
"duo": [18,[2,32],[28,31],[11,16,22,29],[14,15,30],[1,17,21,24]],
"dur": [[2,18,21,31]],
"optionsviewoptionsmenuitem": [8],
"inform": [32,15,18,[16,31],[11,14,21],24,[13,17,20,23,25]],
"depend": [[14,15,20,21]],
"commit": [2],
"targetlocalelcid": [13],
"project_stats_match.txt": [[2,14,32]],
"fortiar": [21],
"cura": [[2,4,22]],
"character": [[21,27],[5,13,32],[16,17,18,20,23,34],[8,15,24,25,31,33]],
"essentialment": [29],
"dvd": [22],
"quarkxpress": [15],
"coperi": [23],
"xmx2048m": [18],
"curt": [23],
"curr": [6],
"meniju": [17],
"resident": [31],
"xxxxxxxxxxxxxxxx.xxxxxxxxxxxxxxxx.xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx": [18],
"limburg": [6],
"divergent": [20],
"fluent": [21],
"labor": [[18,32],[2,31],[14,15,16,17,22]],
"quantificator": [25,33],
"belarusian": [6],
"krunner": [18],
"controlar": [16,31,[18,32],[2,6,21,23,26,27]],
"libreoffic": [[15,19]],
"modulo": [18],
"ubica": [31],
"pacchetto": [18,14,[2,15]],
"uighur": [6],
"long": [32,[18,23]],
"into": [13],
"vetul": [[21,24],[0,11,15,18]],
"deberea": [28,31,[2,8,12,13,14,22,24,25,32],[3,9,16,17,18]],
"benign": [[16,32]],
"uzb": [6],
"supporto": [[9,34]],
"expression": [25,27,34,[11,18,26,28,33],[12,32]],
"mano": [[19,20,24,31]],
"talia": [21,33],
"numerario": [25],
"necessariment": [17],
"texto": [15,21,34,32,30,[13,23],[28,31],16,11,17,[5,20],[19,27,29,33],[2,22,24,25],[1,8,14,18]],
"viewdisplaysegmentsourcecheckboxmenuitem": [8],
"editregisteremptymenuitem": [8],
"lassa": [30,[2,13,18,28]],
"inusabil": [24],
"examina": [11],
"oper": [18,[2,13,21],[11,14],26],
"precisement": [13],
"open": [27,13,[15,16,31],[0,11,20]],
"fabrica": [[14,34]],
"funder": [31],
"www.oracle.com": [18],
"mkd": [6],
"rapid": [11,17],
"project": [[18,22],[14,24],[2,15,21]],
"xmx1024m": [18],
"consistentia": [11],
"imprimit": [22,18,32],
"dzo": [6],
"renomina": [[22,31]],
"dismarca": [[21,30]],
"penalty-xxx": [31,34],
"gotonextsegmentmenuitem": [8],
"committera": [2],
"mlg": [6],
"nnn.nnn.nnn.nnn": [18],
"modification": [[11,13,14,15,21,27,31]],
"supporta": [31,[11,15],[1,2]],
"perdit": [[21,34],[4,14,16,22,31]],
"mlt": [6],
"abort": [[18,22]],
"committ": [[2,31]],
"left-to-right": [15],
"internet": [[20,29],[2,32],27],
"allow": [14],
"saltar": [32],
"successivement": [19,5],
"superscribit": [[14,18,30]],
"saltat": [13],
"printf": [16],
"interest": [[14,20]],
"interess": [19],
"regrupp": [16],
"usator": [18,34,33,[11,21],[2,9,19,20],[13,14,15,22,25,27,31]],
"commod": [18,[15,31]],
"majuscul": [[8,25],[21,28]],
"externo": [32],
"reassum": [32],
"maximis": [32],
"es-mx": [29],
"scribe": [18,32,[14,15,17]],
"registro": [21],
"bass": [1],
"nstip": [21],
"traher": [18,[32,34],33],
"marc": [4],
"registra": [31,[2,9,11,22]],
"bash": [14],
"concernent": [32],
"manualment": [[15,17,29,31],[20,21,24,30]],
"base": [13,8,21,32,[14,31],[15,18,23,28],[2,9,10,17,20,25,29,30]],
"stem": [3],
"registr": [24,31,[2,11,21],[18,20,32]],
"insimul": [[13,16,21,24]],
"excambia": [21],
"difficil": [23],
"tempor": [[20,32],[13,15,19,27],[11,16,21,22,28]],
"romansh": [6],
"mon": [6],
"nonobstant": [28],
"admittit": [[17,28]],
"impedimento": [2],
"volapük": [6],
"stilo": [15,[0,1,2,16]],
"indica": [[16,32],[11,18,22,31]],
"inserit": [21,32,[17,27,30,31]],
"profito": [2],
"gedit": [17],
"necessit": [20,[2,31],[19,21,30]],
"word": [15,[13,19,27]],
"swati": [6],
"extension": [12,[17,23,32]],
"fortia": [2],
"lingua": [31,34,29,18,20,28,15,[3,24],[1,22],[0,6,11,23,33],[13,32],[10,17],[2,14,19,21],[4,5,12,16]],
"auto-propag": [24],
"essera": [21,[13,31],18,24,[17,32],16,19,[2,15,27],28,[0,14],[1,5,8,10,11,26,29,30]],
"slave": [6],
"impon": [28],
"installation": [18],
"europe": [[10,28,31,34]],
"europa": [23],
"revisio": [31],
"mri": [6],
"vcs": [2],
"chamorro": [6],
"assecurar": [16,14],
"lingvo": [12],
"zhuang": [6],
"discargar": [18,34,[12,21],[2,8,10,17,29,33]],
"pre-processo": [15],
"mrs": [28],
"opinion": [32],
"excambio": [17],
"verment": [[21,24]],
"tortoisegit": [2],
"trahit": [32],
"msa": [6],
"provident": [32],
"reten": [13,[2,18,31,32]],
"esqu": [18,12],
"sovent": [29],
"ecc": [18,[11,15,22]],
"n.n_sourc": [18],
"specifica": [[13,18],[2,23,28,30,31]],
"manipul": [16],
"appar": [[8,18],[14,21],[2,15,17,20,24,27,30,31,32]],
"html": [18,13,15,28,[14,16,19,22,31,32]],
"vel": [[22,25],[2,14]],
"ven": [6],
"computator": [2,21],
"variabil": [16,13,[21,32]],
"ver": [18],
"productivit": [28],
"omegat.bat": [14],
"artund": [29],
"vocabulario": [29,17],
"impli": [7],
"finit": [2,[31,32]],
"alcun": [21,[5,14,31,32]],
"javanes": [6],
"excambiar": [11],
"recip": [[16,18]],
"minimo": [21],
"configuration": [[14,18],22,[21,24],[13,19,31,32,33],[3,29,34]],
"trahent": [18],
"accedit": [32,22],
"proposito": [34,31,[17,22,30]],
"sawuła": [7],
"inscribit": [20,[15,21,27,32]],
"jres": [18],
"www.ibm.com": [18],
"displicara": [19],
"mus": [32],
"examinar": [[11,14,32]],
"liber": [[16,18]],
"celer": [18],
"urdu": [6],
"dirig": [14],
"propriet": [14,34,21,[13,24],[22,31],[11,17,18,29,32,33],[3,8,12,15,19,28]],
"adjutar": [[11,31]],
"inutilement": [31],
"selectionar": [[29,34]],
"laborara": [[0,18,29]],
"existent": [31,[14,17],[18,24],[10,21,28,30],[1,15,19,20,33,34]],
"command": [[18,21],[2,8,19]],
"n.n_without_jr": [18],
"allig": [21],
"viewmarkbidicheckboxmenuitem": [8],
"year": [[22,31]],
"notar": [17],
"preferit": [24],
"via": [8,21,32,33,34,[11,18],5,2,9,[6,7,13,15,17,22,24,27]],
"vie": [6],
"fileshortpath": [32],
"approxim": [32],
"strato": [15],
"errara": [16],
"aborto": [22],
"coercit": [20],
"instruct": [11,13],
"verificar": [[17,28,31]],
"sinistra": [5,15,28],
"merchant": [7],
"version": [18,2,31,20,7,[4,14,15],[0,3,9,11,22,25,29,33]],
"project-dir": [[18,22]],
"folder": [17],
"mya": [6],
"stop": [21],
"permitt": [21,30,[13,18],[17,31,32],[1,15,16],[2,10,20,22,24,28]],
"de-fr": [31],
"rolar": [[28,32]],
"detail": [7],
"vista": [8,[15,21,33,34],[2,14,22,32]],
"assecur": [14],
"dmymemory.api.email": [20],
"projecteditmenuitem": [8],
"britannica": [34],
"configurar": [21,[13,32]],
"chechen": [6],
"cambiava": [2],
"annidamento": [16,34,33],
"hardit": [27],
"conflig": [[2,8]],
"groenlandes": [6],
"enriqu": [7],
"habeva": [[22,31]],
"learn": [7],
"assati": [[20,32],[15,16,17,22,31]],
"instruer": [29],
"apertura": [16,21,[11,32]],
"iceni": [15],
"constituera": [13],
"invoc": [32],
"repetition": [32,21,11],
"specif": [18,[13,22,24],14,[15,27,31],[10,11,32],[0,8,21,25,28,29,30,34]],
"obligatori": [13],
"esserea": [[4,18],[9,22]],
"dsun.java2d.noddraw": [18],
"testa": [13,26],
"ell": [6],
"turco": [6],
"processara": [18],
"x0b": [25],
"assigna": [18],
"canada": [18],
"abandona": [30],
"altern": [32,31,[13,21,34],24,[2,8,14,30]],
"http": [18,20,1],
"detali": [[14,32]],
"jaln": [[21,32]],
"interfer": [21],
"luganda": [6],
"significa": [[5,15,28,31]],
"basicament": [15],
"linea": [18,34,27,20,14,[8,22,25],32,[13,15,24,28,31],17,[3,4,5,9,11,16,30,33]],
"occident": [[6,23,34]],
"vol": [6],
"softwar": [7,[0,2],[1,18,21,22,31]],
"habera": [[19,32],[18,20]],
"ingresso": [31],
"projectsinglecompilemenuitem": [8],
"mexicano": [29],
"de-en": [31],
"vos": [4,[13,18,33]],
"docbook": [[4,15,16]],
"cambiara": [13],
"helton": [4],
"eng": [6],
"instruit": [[13,22]],
"proponera": [19],
"fornit": [[11,32],[9,14,18,20,21]],
"estim": [32],
"special": [13,[3,4,5,15,22,27,28]],
"okapi": [34],
"numer": [16,[15,31,32,34]],
"violetto": [21],
"damno": [[17,30]],
"donar": [34],
"copyright": [7],
"vacua": [32],
"moran": [7],
"coreano": [[6,21]],
"project_nam": [[14,31]],
"remanera": [14],
"system-os-nam": [13],
"occurr": [31,[18,26,27,32]],
"optionstabadvancecheckboxmenuitem": [8],
"abbordar": [23],
"immagazin": [[18,24],[13,22]],
"modificant": [30],
"optionsviewoptionsmenuloginitem": [8],
"islandes": [6],
"nav": [6],
"alerta": [25],
"nau": [6],
"tar.bz2": [12],
"epo": [6],
"restaurar": [21,[8,14,22]],
"besoniara": [2],
"alimenta": [18],
"chassa": [[31,34]],
"majorit": [[2,28]],
"pertinent": [15,32,21,[3,8,11,14,16,18,22,31]],
"x64": [18],
"hollandes": [6],
"nbl": [6],
"toni": [7],
"cassa": [21,[5,25,26,29],[2,27,31]],
"parola": [27,32,21,17,11,[5,25],3,[18,19,26,29,31,33],[14,15,16,20,28,34]],
"parenthes": [32],
"functionamento": [[4,11,18]],
"cell": [[15,27,29,30,31]],
"isn\'t": [25],
"valid": [34,[16,32],[21,22],18,33,[13,24,28],[15,20]],
"interfac": [14],
"era": [[21,32]],
"assum": [23],
"mongolico": [6],
"probabilement": [[11,17,23]],
"accostum": [32],
"riga": [5],
"entrata": [21,17,27,[32,33],8,[5,20],[1,2,10,13,14,15,16,18,25,30,31,34]],
"optionsteammenuitem": [8],
"poterea": [[31,32],[4,19]],
"uzbek": [6],
"gzip": [31],
"nde": [6],
"notic": [34],
"quandocunqu": [31],
"esc": [32,[5,21,27]],
"x86": [18],
"ndo": [6],
"logic": [[25,27,33]],
"nostemscor": [32],
"remitt": [20],
"ubicunqu": [[31,32]],
"est": [6],
"mexican": [29],
"console-createpseudotranslatetmx": [18],
"littera": [21,25,31,[8,15]],
"influentia": [[15,21]],
"etc": [31,[16,24],[0,2,3,13,14,15,18,25]],
"forni": [20],
"longman": [[12,34]],
"nep": [6],
"fuzzyflag": [32],
"instantane": [32],
"merriam": [12,[32,33,34]],
"comportar": [[14,22]],
"didn\'t": [0],
"preferentia": [18,14,[5,8,11,21,32]],
"projectname-omegat.tmx": [31],
"finnes": [6],
"interpreta": [23],
"exactement": [29,[13,16]],
"eus": [6],
"project_sav": [32],
"angles": [31,25,[0,3,4,6,12,18,20,21,32]],
"fonte-destin": [12],
"media": [22],
"forma": [[14,31],[3,13],[17,27],[8,16,18,32]],
"lanceat": [18,20,21],
"lancear": [18,34,14,22,20,[1,16]],
"debera": [[8,20,31]],
"n.n_without_jre.zip": [18],
"calcul": [32],
"credito": [21],
"igbo": [6],
"defini": [[17,21]],
"render": [[13,19],15,[11,22,30,31,32,33],[2,14,20,24,26,28,34]],
"medio": [24,[14,17,22,25,31]],
"interlingua": [[6,7,33]],
"ndebel": [6],
"magento": [15],
"vxd": [15],
"continera": [14,27],
"adjacent": [32],
"haver": [[16,20]],
"super": [18,[1,12,20,28,29,32]],
"martio": [33],
"ewe": [6],
"martin": [7],
"u00a": [27],
"non-parola": [25],
"discassar": [12],
"localis": [[15,18,22],[2,9,21,31,34]],
"toto": [16,[25,30],[2,5,26,28,31,32,34]],
"segmentara": [[15,28]],
"shift": [21,8,5,15,[16,19,27],[17,30,34],32],
"cert": [31,13,[11,14,16,19,20,28]],
"wunderlich": [7],
"deind": [[15,19,24]],
"java": [18,14,22,[8,34],1,[15,16,25,33],[13,31]],
"exe": [18],
"positura": [27],
"tote": [31,27,[13,18,32],15,21,[2,14,16,24],[1,5,10,17,20,22,26],[3,11,23,29]],
"ukrainiano": [6],
"project_save.tmx": [31,22,[14,15],[2,21,27,30]],
"dictionari": [2,12],
"addera": [[2,17]],
"remain": [30],
"nequ": [18],
"importun": [16],
"usualment": [21],
"marcat": [21,[27,32],13,[24,30],28,[0,2,11,16,20,29,31,34]],
"myglossary.tbx": [17],
"simplicit": [2],
"marcar": [27,[2,13,32]],
"flash": [15],
"continent": [27,14,[11,32],[16,17,31],[18,21,22,28,30]],
"numero": [32,[21,30],16,[13,15],[18,20,34],[1,8,17,26,27,31]],
"email-address.com": [20],
"appl": [21,19],
"recommend": [[17,24]],
"pulsata": [21],
"effecto": [[13,28],32],
"implicar": [31],
"traduction": [31,32,11,21,[13,24,30,34],[20,28,33],[2,19,25,27]],
"danes": [6],
"default": [13],
"br.arr": [29],
"perqu": [18],
"rendit": [21,32,[13,27],19,16,[15,31],24,8,[22,30],14,[11,18,20,26,34],33,[7,29]],
"timestamp": [13],
"attributo": [13,[28,32]],
"projectaccessrootmenuitem": [8],
"castiliano": [6],
"dyandex.api.key": [18],
"nld": [6],
"incompatibilit": [[0,3]],
"plugin": [0,3,34,[1,14,33]],
"classif": [32],
"omegat-l10n-request": [9],
"x_without_jre.zip": [18],
"differentia": [32,30],
"editinsertsourcemenuitem": [8],
"documento": [21,15,13,31,[8,16],[5,24,32],[19,34],[11,14,27,28,30],[2,9,10,17,18,33]],
"microsoft": [[13,34],[15,20],18,[11,17,19,32,33]],
"operator": [[25,33]],
"projectnewmenuitem": [8],
"supportar": [9],
"nostr": [31],
"pulsant": [32,19,[1,15,24,27]],
"charta": [13],
"optionstranstipsenablemenuitem": [8],
"utilit": [[12,18]],
"utilis": [32,2,[8,17]],
"segment": [28,24,[16,34],11,32,13,31,[14,15,21,25],33,[8,27]],
"changes.txt": [14],
"glossari": [17,[2,32],[21,27]],
"ignored_words.txt": [[2,14]],
"examin": [2],
"configuration.properti": [18,10],
"imagina": [25],
"appropriatement": [31],
"nno": [6],
"giro": [15],
"superl": [3],
"duplicato": [27],
"recrear": [16],
"judeogermano": [[6,20]],
"designo": [[13,15]],
"next": [[0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30,31,32,33]],
"nob": [6],
"adjutara": [9],
"color": [21,15,[16,32],[8,30]],
"string": [18],
"import": [31,32],
"non": [21,32,[18,24],13,31,17,16,27,[15,20],[22,28,30],25,[5,26,29],[4,8],[0,14,19],[2,23],[1,3,9,12,34]],
"summit": [32,28,[9,19,20,21]],
"button": [[27,32],18,[24,26],[1,13,28,29]],
"nor": [6],
"not": [18,[17,27,28]],
"nos": [2,[28,30]],
"polones": [6],
"central": [[6,23,34]],
"expectar": [18],
"now": [0],
"colpo": [21],
"sequito": [18,[16,25,31],[2,15,21,22,27]],
"ascii": [15],
"remarca": [21],
"piscar": [15],
"permittit": [[2,30]],
"was": [28],
"invertit": [32],
"selection.txt": [30,21],
"xhtml": [13,[15,16,28,32]],
"resultar": [16],
"refer": [18,[16,31],[9,15,21,27,32]],
"grec": [25,6],
"window": [18,14,[2,34],21,[11,12,15,17,20,22,25,33]],
"armeni": [6],
"call-out": [21],
"pre-definit": [[18,21]],
"disable-project-lock": [18],
"meta-character": [25],
"omegat.pref": [[14,32]],
"supernumerari": [16],
"inupiaq": [6],
"novicio": [[11,19]],
"personalis": [[8,34],[9,11,21,33],[5,7,32]],
"fao": [6],
"auto-popul": [[21,30],8],
"txml": [15],
"far": [29],
"instrumento": [34,32,[8,21,31,33],[1,25],[14,28],[20,22]],
"faq": [9],
"electron": [32],
"fas": [6],
"estrani": [21],
"plan": [11],
"exhibi": [5],
"ambivalent": [13],
"bartko": [7],
"multipl": [[32,34],13],
"pt_pt.dic": [29],
"explicit": [4],
"calcat": [4],
"purment": [15],
"habilit": [24],
"bastant": [[13,27,28]],
"criterio": [27],
"futur": [31,[2,14,19]],
"amontar": [18],
"italiano": [6],
"level1": [[19,31],21],
"discarg": [[2,14,18],10],
"level2": [[19,31],21],
"minimisa": [32],
"implicit": [13],
"automaticament": [18,21,17,[19,23,29,30,32],[0,2,3,8,10,11,13,14,15,22,24,27]],
"determinar": [29],
"alinear": [[15,22]],
"portion": [[9,21]],
"direct": [15,[2,14]],
"restar": [13,[15,24,30]],
"connexion": [16,[8,11,29]],
"sume": [[13,28,31]],
"modello": [13,28,34,[16,20,24],[1,23,32]],
"statistica": [32,34,21,[8,14],33,24],
"colla": [21,[33,34]],
"modern": [6],
"web": [18,9,[20,34],14,33,[1,8,19,32]],
"alora": [[18,22,29],[14,31],[10,13,16,19,20,21,26,27]],
"berlin": [7],
"editselectfuzzy4menuitem": [8],
"editregisteridenticalmenuitem": [8],
"perquir": [27],
"gris": [21,19],
"vece": [31,[14,15,21,28]],
"usat": [18,[14,21,32],11,24,13,[15,20,34],31,22,33,[9,30],[2,8],[16,17,29],[0,10,19,25,27,28]],
"etsi": [27],
"tentativa": [[17,21]],
"inflar": [2],
"capitulo": [11,32,14,20,31,24,[4,17,25,27,28,33]],
"usar": [18,31,[29,33],[15,20],[2,13,32,34],[11,17,21,27],19,8,[10,24],[4,16,22,28,30]],
"chronologia": [21,8],
"bulgaro": [6],
"pt_br.dic": [29],
"tertia": [32],
"certain": [14],
"tabula": [21,[5,32]],
"unabridg": [12],
"dice": [18,32],
"section": [[16,23],[15,18,20,21,24]],
"auto-complet": [21],
"afrikaan": [6],
"plen": [[18,27,31]],
"alquanto": [31],
"optionsglossaryexactmatchcheckboxmenuitem": [8],
"interpretara": [23],
"renominar": [[13,24,29,31]],
"dict": [12],
"sententia": [[21,24]],
"regularment": [31],
"possibilit": [[2,11,20,24,31]],
"orient": [[1,23,34]],
"habitud": [14],
"adressar": [31],
"vacuar": [32],
"fulah": [6],
"vacuat": [[21,26]],
"nnnn": [32,18],
"project_save.tmx.yearmmddhhnn.bak": [[22,31]],
"effectu": [27],
"summ": [32],
"effect": [21],
"fallback": [21],
"option": [21,13,[18,32],30,34,33,27,[20,22],[26,31],[8,16,24,28,29],[10,14,17,23],[0,2,5,11,15,25]],
"appreci": [9],
"displica": [8,2,[17,19,21,24,29]],
"processa": [20],
"avantiamento": [32],
"myproject": [31],
"visibil": [9],
"probat": [25,33],
"melio": [[16,17,22,31]],
"zh_cn.tmx": [31],
"chuvash": [6],
"wordfast": [15],
"probar": [[15,21]],
"pejor": [[16,20]],
"approb": [17],
"huriaux": [7],
"copula": [[11,16,20]],
"carmelo": [[7,33]],
"wix": [15],
"collant": [24],
"linguist": [20],
"txt2": [23],
"synchronisara": [2],
"confund": [[0,1]],
"processo": [[24,28],[8,11,13,14,15,16,21,22,30,31]],
"visio": [15],
"nya": [6],
"txt1": [23],
"archiv": [18],
"procurar": [20],
"user": [14,[18,22],[7,32]],
"confus": [16,31],
"proxi": [18,[8,21,34]],
"claud": [21,27,[22,26,31]],
"extens": [13,[17,23],15,[11,12],[14,29,31]],
"dign": [[17,31]],
"legier": [31],
"copiara": [[13,19,26]],
"fij": [6],
"avantia": [21],
"fin": [5,27,[20,25],[4,6,8,14,15,19,28,31]],
"kinyarwanda": [6],
"hausa": [6],
"b0": [16],
"vietnames": [6],
"b1": [16],
"fit": [7],
"b2": [16],
"claus": [[21,31]],
"fix": [16],
"solment": [[21,27],[16,20,31],[5,14,15,17,18,19,24,28,29,30,32]],
"marāṭhī": [6],
"ajornamento": [11],
"cyclar": [21],
"dicer": [17],
"gaelico": [6],
"aa": [6],
"ab": [14,[15,18,34],32,[29,31],[20,21],[5,13,17,27],[0,1,6,11,16,23,24,28,33]],
"surg": [15],
"ad": [17],
"sure": [28],
"ae": [6],
"af": [6],
"elig": [[8,21],32,[10,18,27],[2,5,29,31]],
"ak": [6],
"diff": [32],
"al": [21,32,18,[2,27],31,5,20,8,9,[14,24,28,34],[11,16,17],[19,22,30],[13,15,33],[10,25,29],[4,26],1,[3,7,12,23]],
"automat": [20,21,34,32,33,[11,22],[5,8,10,16,24,29]],
"am": [6],
"an": [25,[14,16],[6,30]],
"editmultiplealtern": [8],
"ap": [18],
"ar": [6],
"as": [7,[6,21]],
"av": [6],
"ay": [6],
"wln": [6],
"az": [6],
"novement": [18],
"approximativement": [22],
"ba": [6],
"virgula": [17,[13,25],30],
"be": [17,7,[6,13,27]],
"importar": [31,2,34,[15,20,32]],
"bg": [6],
"bh": [6],
"bi": [6],
"salta": [13,26],
"filters.xml": [14,[2,24]],
"scriptorio": [18,[27,32]],
"bm": [6],
"bn": [6],
"bo": [6],
"traducera": [18],
"anterior": [21,15],
"br": [13,[6,18]],
"bs": [6],
"ubic": [34,18,[17,21],[22,27,31,32],[8,14,19,24]],
"samoano": [6],
"necessita": [18,31,20,16,[2,29],[9,11,14,22,25,30,32]],
"by": [[7,14,30],[9,32]],
"segmentation.conf": [[14,22],[18,24]],
"ca": [[6,18,20]],
"minuscul": [8,21],
"cd": [18,22],
"ce": [[4,6,15,28,32]],
"clave": [8,[5,20,21,27],[18,32],[10,13],[11,19,33],[17,25,30]],
"öäüqwß": [27],
"systema": [18,13,21,2,[29,34],[11,17,20],[5,8,10,22,23,31,33]],
"norvegian": [6],
"ch": [6],
"ci": [16,[2,21],14,[1,3,13,18,24,27]],
"familiar": [22,[1,27]],
"cn": [18],
"co": [6],
"figur": [32,[14,17,29],[0,12,16,20,25,30,33]],
"cr": [6],
"apprend": [[19,20],[4,11,18,33]],
"cs": [6],
"cu": [6],
"cv": [6],
"cx": [25],
"cy": [6],
"terminologia": [10,[11,17,31,32],[21,27]],
"graphica": [14],
"sanskrit": [6],
"apach": [2],
"etiquettar": [[21,30]],
"da": [[18,21],[6,19,32]],
"maestro": [31],
"adjustedscor": [32],
"font": [21,31,13,34,32,15,19,27,20,[8,24,30],[23,28],17,[16,18],[10,14,33],[3,22],[0,1,2,5,11,26]],
"pre-requisito": [22,18],
"dd": [[22,31]],
"de": [34,31,18,32,13,21,15,16,28,20,11,[14,24],[22,27],[2,33],8,[17,19],30,23,[9,25],1,29,26,[4,5],10,3,0,6,12],
"justif": [15],
"di": [22],
"slovaco": [6],
"fora": [5,31],
"extern": [32,[21,24],[2,8,14,15,16]],
"observa": [15],
"do": [14,0],
"f1": [[21,32],[8,34]],
"f2": [32,[18,24]],
"f3": [21,[8,34]],
"personalisa": [13],
"f4": [11],
"dr": [28],
"f5": [[8,11,21]],
"dv": [6],
"wol": [6],
"dz": [[6,12]],
"editundomenuitem": [8],
"commutant": [15],
"veni": [21],
"ee": [6],
"which": [18],
"u000a": [25],
"pallid": [21],
"el": [6],
"visualsvn": [2],
"belazar": [20,34,33],
"en": [[6,18,22,32]],
"eo": [[6,18,19,24]],
"es": [32,31,21,18,[13,17],16,2,22,14,15,20,[11,24],30,27,[8,28],[1,29],23,12,0,[3,9,10,19,25,26],5,[6,33]],
"u000d": [25],
"et": [15,28,[16,17,27,32],[2,6,12,13,18,24]],
"u000c": [25],
"eu": [6],
"integr": [21,[13,14,18,28,30,32]],
"ex": [32,31,[8,18],[2,13,15],[3,11,16,17],[0,21,24,28,34],[10,20,22,25,29,30,33]],
"aggress": [21],
"essit": [32,21,26,30],
"activ": [21,13,[5,20],[3,11,19,28,31,32]],
"fa": [6],
"ff": [6],
"stats.txt": [14],
"u001b": [25],
"indic": [11,[13,31]],
"fi": [6],
"fj": [6],
"terti": [[17,32]],
"origin": [32,30,16,17,[15,20],13,[2,11,21,24,27,31]],
"vocal": [25],
"for": [7,[1,18,34],[2,32,33]],
"exclud": [31],
"fo": [6],
"depost": [18],
"pensar": [0],
"confirma": [21,[8,18]],
"fr": [18,22,[6,20,28,29]],
"content": [[18,30],[17,33]],
"precaution": [22],
"marathi": [6],
"fy": [6],
"desktop": [18],
"inuktitut": [6],
"applescript": [18],
"exclus": [[24,27]],
"accid": [28],
"ga": [[6,20]],
"gb": [18],
"class": [[13,25],33,1],
"gd": [6],
"helplogmenuitem": [8],
"somali": [6],
"presenta": [[13,16,32]],
"carta": [[13,27]],
"spanish": [29],
"gl": [6],
"editoverwritetranslationmenuitem": [8],
"outputfilenam": [18],
"gn": [6],
"i0": [16],
"fijian": [6],
"i2": [16],
"aeiou": [25],
"gu": [6],
"gv": [6],
"duple-cl": [[14,18,27]],
"placiat": [31],
"publish": [7],
"ha": [18,[31,32],21,15,29,[20,30],[2,19],17,[11,16,23,27],[1,12,14,22],[4,13,24,26],[6,8,28]],
"equipa": [2,34,31,[8,21,33],[3,5,11,18]],
"correspondent": [32,[16,22],[0,2,13,14,15,20,21,24,25,26,28,29,31]],
"fort": [1],
"adressa": [[18,24]],
"he": [6],
"assign": [18,[8,13,34]],
"hh": [[22,31]],
"hi": [6],
"duser.languag": [18],
"ho": [6],
"canadian": [28],
"generalit": [[10,24,33]],
"hr": [6],
"amarea": [[9,25]],
"ht": [6],
"hu": [6],
"prefixa": [[13,28]],
"practica": [18],
"repetit": [32],
"hy": [6],
"hz": [6],
"file-target-encod": [13],
"fra": [6],
"oci": [6],
"verd": [[2,32],21],
"ia": [6],
"briel": [[4,7]],
"id": [31,2,[13,17,18,21,23,24,27],[6,12,16,20,22,26,32]],
"https": [10,18],
"prefixo": [30,3],
"ie": [6],
"fri": [6],
"impedir": [[22,34],[14,24,33]],
"if": [[7,13,14],[1,17,21]],
"estoniano": [6],
"manipulation": [16],
"project_stats.txt": [32,2],
"ig": [6],
"ocr": [[15,24]],
"ii": [6],
"ik": [6],
"projectaccesscurrenttargetdocumentmenuitem": [8],
"il": [18,32,[15,31],17,[20,24],[13,30],[2,14,16,21,28,29],[10,11],[3,5,9,22,23,27]],
"in": [31,32,18,21,2,27,15,16,17,13,22,14,20,19,11,23,[28,30],24,29,1,33,25,5,[8,34],3,12,[0,10],9,[4,7,26],6],
"io": [[4,18],6],
"termin": [18,32],
"ip": [18,34],
"briton": [6],
"k3": [19],
"linguag": [1,[23,33]],
"index": [34,4],
"ir": [21,[32,33,34]],
"is": [7,32,[13,14,25],[6,18,24,27]],
"it": [7,17,[14,30],[6,18,28]],
"iu": [6],
"odf": [15,[13,16,28]],
"odg": [15],
"alterement": [[18,21]],
"esser": [18,28,[17,31],15,[11,13,22,32],[16,21],[2,8],[14,30],[20,24],[10,12,25,27],[19,23,29]],
"ja": [18,[11,21,32],[2,6,17,19,31]],
"multiterm": [17,34,33],
"jc": [4],
"odp": [15],
"glossario": [17,21,34,32,8,33,[10,11,27],[14,19],2,[3,5,12,15,22]],
"odt": [15,24],
"gotonexttranslatedmenuitem": [8],
"librari": [14],
"jp": [23],
"nplural": [13],
"js": [1],
"antea": [[2,28,31,32]],
"jv": [6],
"learned_words.txt": [[2,14]],
"similabil": [32,[11,13,19,24,30]],
"maxym": [7],
"ka": [6],
"fte": [[8,21]],
"codifica": [21],
"elementob": [21],
"dunqu": [[14,29]],
"elementoa": [21],
"retardar": [31],
"kg": [6],
"ki": [6],
"montag": [20],
"kj": [6],
"kk": [6],
"kl": [6],
"km": [6],
"uyghur": [6],
"kn": [6],
"ko": [6],
"dextr": [32,[15,18],[1,21]],
"acced": [8,32,[14,18],[2,10,11,21,27]],
"equala": [13,[27,29]],
"suggestion": [[5,8,21,29,30,31,32]],
"kr": [6],
"ks": [6],
"viewdisplaymodificationinfoallradiobuttonmenuitem": [8],
"ku": [6],
"kv": [6],
"draw": [15],
"kw": [6],
"ky": [6],
"usqu": [[5,11,18],[8,16,22,24,25]],
"la": [34,32,33,[2,3,6,25,29]],
"lb": [6],
"le": [21,32,18,31,13,16,2,15,17,27,20,14,24,22,[11,30],28,8,34,25,29,19,23,5,1,33,26,[3,10,12],9,4,0,6,7],
"fortement": [15],
"lg": [6],
"ful": [6],
"li": [6],
"dswing.aatext": [18],
"refac": [21,[8,32]],
"genera": [[15,19,20]],
"ln": [6],
"lo": [18,32,31,21,15,17,2,[14,16,29],[19,22,27,28],[1,6,11,12,13,20,26]],
"personalisar": [34,[1,13,16,21,32]],
"ls": [14],
"lt": [6],
"placia": [17],
"lu": [25,6],
"dist": [18],
"lv": [6],
"salva": [21,[15,18],[8,17,19,23]],
"genere": [[13,20,23]],
"erraticament": [14],
"that": [7,[18,30]],
"cycleswitchcasemenuitem": [8],
"ma": [31,[16,17,18,32],[2,21,22],[1,4,11,14,15,19,20,24,25,27,29,30]],
"salvo": [5],
"mb": [18],
"bidaux": [7],
"limin": [30],
"sufficient": [[14,18]],
"limit": [[2,13,17,27]],
"me": [[4,31]],
"inestimabil": [4],
"entra": [18],
"mg": [6],
"denomin": [19,[10,13,16,21,34]],
"mh": [6],
"sparniar": [23],
"mi": [4,6],
"sparniat": [4],
"mk": [6],
"ml": [6],
"genit": [3],
"mm": [[22,31]],
"entri": [1],
"mn": [6],
"mr": [28,6],
"ms": [20,[6,16,28]],
"author": [7],
"mt": [32,[31,34],[6,20]],
"pashto": [6],
"thai": [6],
"wxl": [15],
"my": [[6,14,18,20]],
"plus": [32,18,31,25,[14,24],17,[13,22,27],[20,21,29],[11,15,23],[2,8,10,12,16,19,28,30]],
"expand": [28],
"na": [6],
"renov": [2,[11,13,14,31,34]],
"nb": [[6,27]],
"itera": [32],
"responsabil": [2],
"nd": [6],
"ne": [[6,16]],
"editar": [21,34,[17,18,33]],
"updat": [2],
"ng": [[6,15]],
"ni": [[20,31],[2,30]],
"nl": [31,6],
"legisl": [31],
"nn": [[6,22,31]],
"displaci": [21],
"no": [[13,18],[0,6,32]],
"nr": [6],
"nv": [6],
"gotohistoryforwardmenuitem": [8],
"ny": [6],
"seligent": [[23,32]],
"blau": [[16,21,32]],
"obtenera": [31],
"oc": [[6,20]],
"od": [15],
"of": [7,33,18,[2,17,20,32]],
"possibl": [30],
"oj": [6],
"ok": [2,[18,19,31]],
"reserva": [31,22,[21,34],[14,18]],
"om": [6],
"sinhala": [6],
"on": [17,[16,18],[15,31,32],11,[0,2,8,14,22,29],[1,4,5,9,13,19,23,27,30]],
"purpos": [7],
"or": [7,[8,14,30],[6,12,17,29]],
"os": [18,34,14,[2,6,11,15,21,33]],
"ot": [15],
"oji": [6],
"pa": [6],
"editinserttranslationmenuitem": [8],
"ipso": [[18,25]],
"pc": [18],
"pdfs": [15],
"pi": [6],
"septentrion": [6],
"pl": [6],
"po": [13,32,15,[31,34]],
"ps": [6],
"optionsglossarystemmingcheckboxmenuitem": [8],
"pt": [[6,18,20,29]],
"inclus": [[16,17,25,32]],
"augusto": [7],
"nondum": [19],
"inserera": [[21,32]],
"necessari": [[14,15,18],[2,12,16,17,20,27,29,31,32]],
"recent": [[8,18,21,31]],
"inscript": [21],
"comparera": [0],
"evalutation": [20],
"qu": [6],
"edit": [32,34,21],
"old": [6],
"editselectfuzzy5menuitem": [8],
"quotidian": [14],
"bilingu": [31,[11,20,34]],
"them": [[13,14]],
"then": [30],
"re": [32,[11,31],14,[19,30,33],[2,6,9,15,16,18,21,23,24,25,28]],
"includ": [27,[14,18,31],[16,32],[0,2,7,13,20,24,25,29,30]],
"privaci": [18],
"strategia": [[30,31]],
"singular": [[3,16]],
"rm": [6],
"rn": [6],
"ro": [6],
"minut": [7],
"minus": [[14,18,20,31],[2,4,11,16,17,22,24,30]],
"ru": [6],
"accesso": [[10,18],[2,14,21],[1,12,32,33]],
"rw": [6],
"culmin": [1],
"sa": [6],
"deviar": [16],
"disposit": [21],
"sc": [[6,25]],
"omn": [18,31,27,[4,32],2,[14,21],[13,16,25],[8,19,22,30],[1,3,17,20,23,28,33]],
"renovar": [[2,22]],
"sd": [6],
"navigar": [[10,32]],
"se": [21,2,[5,19,32],[15,16,18,28],[3,13,22,27,30,33],[4,6,9,14,17,20,24,29,34]],
"creava": [29],
"nynorsk": [6],
"sg": [6],
"si": [21,18,31,13,32,22,14,[27,29,30],16,[15,19],[17,20,28],[11,24],[0,2,5,8,25],[1,9,12,23],[3,6,26]],
"trovarea": [[18,22]],
"sk": [6],
"sl": [[2,6]],
"auxiliar": [31,[15,32]],
"samuel": [[4,7]],
"sm": [6],
"sn": [6],
"so": [6],
"caution": [16],
"lectura": [[4,17]],
"sq": [6],
"email": [9,20],
"sr": [6],
"ss": [6],
"constat": [15],
"st": [6],
"su": [32,21,13,[2,4,14,17,31],[11,12,15,23,27],[16,18],[0,1,5,6,19,20,22,24,28]],
"impract": [18],
"sv": [6],
"pesantement": [11],
"intern": [32,21,[17,18,24]],
"sw": [6],
"monstrar": [18,[21,32],[8,27],[10,13,15,17,23,26]],
"cento": [20],
"norma": [21],
"monstrat": [21,32,27,[13,17],[15,31],[2,18,24,26,30],[11,16,22,25]],
"vostr": [18],
"ta": [6],
"editoverwritesourcemenuitem": [8],
"te": [18,[14,20,30],[2,9,13,19,21,23,24],[15,16,17,28,31,32],[1,3,6,11,22]],
"querent": [27],
"tg": [6],
"retorno": [32],
"th": [6],
"ont": [[8,21]],
"enforc": [31],
"saṁskṛta": [6],
"ti": [6],
"remov": [[5,13],[16,30],[18,21,24,27,31],[0,8,15,29,32]],
"tk": [6],
"tl": [6],
"tm": [31,32,34,[2,20],[19,21,27]],
"assist": [[31,33]],
"tn": [6],
"melior": [20,[28,30,31,32]],
"to": [18,22,14,32,[6,7,21,27]],
"v2": [20,[18,34]],
"tr": [6],
"ts": [6],
"tt": [6],
"tu": [18,31,32,19,[14,20],[2,21],29,[13,22],[27,30],11,[9,16,28],[8,23],17,[15,24],[1,12,25],5,[0,3,4,26]],
"document.xx": [13],
"dialogo": [34,13,21,19,[31,32,33],[17,22,24,30],[2,5,23,29],[1,10,28]],
"tw": [[6,18]],
"trova": [17,[16,20,25]],
"ty": [6],
"insensibil": [[21,27]],
"revid": [[30,31]],
"corrig": [16,32,[4,11,20,27]],
"viewmarkautopopulatedcheckboxmenuitem": [8],
"hmxp": [15],
"describent": [11],
"projectwikiimportmenuitem": [8],
"countri": [18],
"re-focalisa": [27],
"ug": [6],
"mesmo": [21,[17,18]],
"prioritari": [17],
"trivial": [31],
"uk": [6],
"yahoo": [[9,14]],
"sinhales": [6],
"disambigu": [20],
"un": [18,21,31,32,2,14,[25,27,28],[13,16],20,22,[15,17],[11,24,29],19,8,30,34,[1,5],[0,9,23],[3,10],33,[12,26],4],
"reluctant": [[25,33]],
"ur": [6],
"ut": [32,[15,31],23,[8,13,14,24,27,28]],
"dynamicament": [20],
"usual": [18,[16,21]],
"modificabil": [5],
"uz": [6],
"traducibil": [[13,32],[28,31]],
"solut": [34,23,[31,33]],
"this": [14,25],
"specificament": [[18,21]],
"exclam": [2],
"va": [[16,19,30],[20,34]],
"vc": [2],
"ve": [6],
"vi": [[6,18]],
"considerar": [31,[24,28]],
"vo": [6],
"support": [15,17,[0,13,16,25,26,32]],
"wa": [6],
"identificar": [[3,13,16,20]],
"exito": [16],
"groovy.codehaus.org": [1],
"wo": [6],
"claviero": [21,32,11,[33,34],5,[2,6,8,24,27]],
"expecta": [9],
"ora": [[2,8,16,30,31]],
"backspac": [30],
"licens": [7,14],
"emac": [18],
"ori": [6],
"querera": [19],
"distribut": [7,18,14],
"orm": [6],
"calculo": [32,17],
"xf": [18],
"xh": [6],
"venda": [6],
"superior": [28,32],
"lentement": [18],
"xp": [14],
"location": [24,[14,21,31,33]],
"deeper": [17],
"lapsus": [11],
"xx": [18,13],
"xy": [25],
"sourc": [31,14,18,32,[1,21,34],[0,8,11,19,20,22]],
"scand": [1],
"beyond": [18],
"oss": [6],
"justo": [17,[2,31],[9,13,14,16,22,23,27]],
"volker": [7],
"yi": [6],
"toolssinglevalidatetagsmenuitem": [8],
"yo": [6],
"concordar": [27,[13,21,24,25]],
"avantag": [2],
"yu": [7],
"projectaccesssourcemenuitem": [8],
"cont": [32],
"burmes": [6],
"como": [32,18,[15,31],2,[14,34],20,[16,17,21],11,[12,13,24],[22,27,28,29,30],[1,23],[3,9,25,33],[0,8,19]],
"yy": [32,13],
"sensibil": [[9,32]],
"facult": [14],
"lanceara": [18,3],
"za": [6],
"voluntari": [9],
"otp": [15],
"chichewa": [6],
"quant": [[4,21]],
"zh": [[6,31]],
"ott": [15],
"exist": [31,[15,17,32],[1,2,3,14,16,24]],
"installa": [29],
"meraviliar": [4],
"regred": [21],
"hunspel": [3],
"penalti": [31],
"exact": [27,[21,32],17],
"zu": [6],
"prope": [31],
"zz": [18],
"dara": [13],
"south": [6],
"north": [6],
"utf8": [17,23,[13,15]],
"galleciano": [6],
"tanto": [25],
"inconditionatement": [31],
"columna": [17,32,[16,31]],
"statist": [20],
"angulo": [32],
"power": [13],
"accur": [31],
"singul": [[16,27],[2,11,13,25]],
"ulterior": [[2,17]],
"tag-valid": [22,18],
"suspect": [16],
"tokenizer": [11],
"rotund": [15],
"help": [15],
"typo": [[15,22],34,21,[13,16],32,28,[11,14,31,33]],
"kirghiz": [6],
"u0009": [25],
"xhh": [25],
"revis": [[2,9,12,16,31]],
"u0007": [25],
"repositori": [2],
"xho": [6],
"date": [31,[18,22],[15,24,27,28],[2,10,20,32]],
"cors": [6],
"data": [32,[13,14,17,22,31]],
"xht": [15],
"lowercasemenuitem": [8],
"firefox": [[1,29]],
"wiki": [12],
"lists.sourceforge.net": [9],
"separ": [17,[13,32],[2,28],[8,15,16,21,31]],
"circa": [21,[8,31,33,34]],
"neglig": [16],
"cosa": [[9,19,31],[2,8,15,16,33]],
"filepath": [32],
"dato": [22,20,[10,17],[2,14,30,34],[3,11,15,18,24,31,33]],
"depoi": [31],
"preferera": [31],
"oasi": [15],
"dicit": [21],
"depon": [18,[32,34],33],
"sens": [28],
"maxim": [[14,23,30,32]],
"venir": [16,32],
"rejectar": [16],
"openoffic": [[17,29]],
"quasi": [[17,23]],
"renomin": [31],
"send": [14],
"note": [[25,31],32,[13,18,22,28]],
"consequentia": [18,[2,17,22,24,27,31]],
"sequ": [18,31,[2,8,12,14,15,25,28,29,32]],
"optionsautocompletechartablemenuitem": [8],
"noth": [0],
"proqu": [32],
"helari": [[4,7]],
"git": [2,31],
"contributor": [4],
"exportar": [[21,31,34],[8,11,15,20,30]],
"viaweb": [9],
"continuar": [19],
"nota": [32,[18,21,31],20,2,[8,13,27,29],[14,15,17,22,24],[5,9,12,16,19,28]],
"creara": [18,[13,19,22,24]],
"xx-yy": [13],
"frisian": [6],
"auxilio": [4],
"will": [7,[4,13,17]],
"costo": [32],
"respectivement": [17,[20,31]],
"virgul": [17],
"intens": [32],
"intent": [28],
"sparnia": [30],
"optionsspellcheckmenuitem": [8],
"considera": [[16,23]],
"xlf": [15],
"optionssetupfilefiltersmenuitem": [8],
"intertanto": [32],
"altgraph": [8],
"german": [27],
"ultim": [21,[18,31,32],[2,8,20],[5,9,14,22,27]],
"nove": [21,18,19,31,[17,34],[8,13,28],[24,25,29],[2,30,32,33],[1,11,14,15,20,23,26,27]],
"govern": [30],
"your": [[18,21]],
"without": [7,[13,18]],
"analysa": [28],
"functiona": [[20,21],[11,14,18,19,28,31]],
"xml": [15,13,[14,16,17,20]],
"gla": [6],
"colpat": [8],
"gle": [6],
"i.e": [29,30],
"glg": [6],
"serv": [31],
"neutral": [3],
"insensit": [25],
"glv": [6],
"assecurara": [13],
"befor": [[14,18]],
"util": [[11,31],13,[18,19,24,25],[3,14,21,28,30,32]],
"tar.bz": [12],
"seri": [24,[12,17,32]],
"rendent": [28],
"arab": [6],
"xltx": [15],
"chapter": [7],
"minuscula": [21,[8,18,27,32]],
"registrar": [21,11,[8,20,30,32]],
"independent": [14,20],
"dubita": [31],
"reimplaciar": [26,32,[20,21,31,34]],
"lassat": [13,[15,18,21,24,31]],
"expedit": [20],
"prevenir": [30],
"dividit": [32],
"lassar": [30],
"xlsx": [[13,15]],
"enorm": [29],
"costum": [34,17,[22,33]],
"rondo": [15],
"aliquanto": [18],
"tradit": [18],
"gnu": [7,14],
"component": [21],
"alicun": [[16,31],13,22,[28,32],[18,24],[9,15,17,20,23,29],[0,1,2,8,12,14,19,21,25,33,34]],
"target.txt": [30],
"facer": [18,31,[2,15,19,20,23,32],[16,21,26,29,33,34]],
"standard": [32,[17,29,31],[6,11,13,14,15,18,20,21,27]],
"traduct": [31,32,21,20,11,34,14,15,24,30,[16,19,22,33],8,27,[2,13,18],[17,28],1,[3,10,25,29]],
"correct": [18,29,31,[17,20],[2,3,11,12,19,24,30,32]],
"reporto": [9,34,[14,33]],
"travalio": [31],
"alineara": [18],
"ojibw": [6],
"supplera": [14],
"reporta": [20],
"nameon": [13],
"rendera": [21],
"pai": [[18,31],13,34],
"optionsglossarytbxdisplaycontextcheckboxmenuitem": [8],
"pan": [6],
"gotonextnotemenuitem": [8],
"area": [[1,21,24,32]],
"par": [16,31,[20,32],[2,22,24,28]],
"tar.gz": [18],
"gpl": [12],
"suggerit": [32],
"concordator": [[25,33]],
"advertimento": [[13,18],[28,32]],
"basqu": [6],
"list": [33],
"assistit": [[11,33]],
"success": [[21,27],[2,13,26,31,34]],
"superpon": [32],
"in-lin": [16],
"lisa": [17],
"azur": [20,[0,18,27,32]],
"plenment": [31],
"qualqu": [[14,17,19,24,31]],
"formatt": [16,15,34,31,[21,24],[19,23,28,30,32,33]],
"pascer": [32],
"formato": [15,34,17,[13,19],[11,32],[16,31,33],[12,20],[18,21,23,24,27]],
"interfaci": [18,[32,34],[11,21,22],[2,9,14,15,33]],
"contribu": [34,9,[14,15,16,32,33]],
"rashid": [7],
"determin": [[14,17]],
"navegar": [20],
"combin": [[5,18],[8,12,13]],
"menus": [18,32,[19,24,27]],
"germano": [[0,6]],
"nauru": [6],
"basso": [32,[18,28],[2,5,27,29,30]],
"grn": [6],
"nyanja": [6],
"computo": [32],
"pannello": [18],
"xtg": [15],
"bindownload.cgi": [18],
"acceptabil": [[16,20]],
"slavon": [6],
"with": [18,[7,14,15]],
"pdf": [15,34,24],
"there": [0],
"folio": [32,[13,17,25]],
"functionalit": [[2,28]],
"pede": [13,32],
"bislama": [6],
"mult": [[11,15,21,22,30,31]],
"toolsshowstatisticsmatchesmenuitem": [8],
"hexadecim": [25],
"traduc": [22,[11,13,14,15,18,32],[9,28,30,31]],
"viewdisplaymodificationinfononeradiobuttonmenuitem": [8],
"auto-completamento": [8,21],
"russo-bielorusso": [20],
"remarcar": [4],
"ultra": [5,[1,18,31]],
"e-mail": [[14,20]],
"si-nomin": [11],
"per": [18,[21,31],13,32,20,[2,25],27,[15,17,28],24,[14,23],[8,10,11],[16,30],[22,29],[5,33],[12,34],[1,3,6,7,19,26]],
"comocunqu": [[15,18,19,29,31],[11,16,20,28,32]],
"understand": [14],
"glypho": [21],
"projectaccesswriteableglossarymenuitem": [8],
"million": [20],
"even": [7],
"restriction": [30],
"esperanto": [6],
"gui": [22,[14,18]],
"indication": [15],
"guj": [6],
"non-concordantia": [21],
"renovation": [18],
"regexp": [18,22],
"tahitiano": [6],
"sentencecasemenuitem": [8],
"immediatement": [[17,21]],
"verisimil": [[18,28]],
"instant": [31],
"stemmer": [3],
"uhhhh": [25],
"ambira": [19],
"plica": [[14,31],18,2,34,32,21,22,24,[19,29],17,[11,12],[1,8,27],[0,3,13,30,33],[10,15,20]],
"moldovan": [6],
"agradamento": [18],
"optionssentsegmenuitem": [8],
"robust": [22],
"typat": [1],
"actualisara": [2],
"optionsaccessconfigdirmenuitem": [8],
"dokuwiki": [15],
"inconsistent": [31],
"charact": [25,27,34,22,15,[17,21],5,[8,13,32,33],[16,28],18,[3,23,30]],
"prefixar": [21,[13,15,24,28]],
"test.html": [18],
"xxx": [31],
"smalltalk": [1],
"ant.apache.org": [18],
"confortabil": [18],
"correctement": [15,[23,31],[16,17,20]],
"assecurant": [17],
"reinstalla": [14],
"verosimilant": [11],
"pseudotranslatetmx": [[18,31]],
"personalisabil": [[13,21]],
"verbo": [20],
"arno": [7],
"decideva": [2],
"representa": [[13,16,32]],
"velasco": [7],
"panjabi": [6],
"targetlanguagecod": [13],
"codification": [13],
"extrahit": [14,[10,28]],
"pilpré": [7],
"repetitor": [25],
"nuosu": [6],
"absolut": [31],
"bidirect": [21,[8,15]],
"evit": [[28,32]],
"luxembourgish": [6],
"quitar": [21,8],
"basic": [[16,18]],
"locativo": [17],
"assumit": [24],
"complication": [4],
"entrat": [27,16],
"secundo": [[2,11,21,31],[3,13,14,16,28,30,32]],
"vole": [[8,17,19,29,32],[9,13,14,20,23,30,31]],
"causa": [18,[15,17,31]],
"qualit": [31,20,15,3],
"volo": [31],
"reimplaci": [26],
"lancea": [22,18,[0,3,20,31]],
"minimis": [32],
"design": [11,25],
"extra": [[16,18,22,25],[15,31]],
"identif": [[11,13,21]],
"unpack": [18],
"deber": [28,11],
"procur": [21],
"pli": [6],
"encyclopedia": [12],
"invisibil": [14],
"applicar": [[2,13,16,27]],
"simplifica": [16,15],
"relativement": [[14,32]],
"optionstagvalidationmenuitem": [8],
"recentement": [[21,22]],
"discussion": [14],
"pt_br": [29,18],
"a-z": [25],
"conduc": [28],
"accresc": [[28,31]],
"mobil": [30],
"evento": [8,21],
"zoltan": [7],
"ligar": [18],
"consilio": [34,[15,16,29,33],[18,31]],
"eventu": [31,[11,17]],
"preservar": [13],
"muta": [18],
"png": [18],
"diagramma": [13],
"javascript": [1],
"mediawiki": [[21,32],8],
"input": [27],
"komi": [6],
"mediet": [28,[17,27,30]],
"walloon": [6],
"join.html": [14],
"must": [17],
"selectionant": [32],
"pod": [15],
"deteg": [0],
"omegat.kaptn": [10],
"facto": [[15,16]],
"poi": [18,[24,31],[2,17],[11,22,26],[1,13,16,20,21,27,28,32]],
"pol": [6],
"deten": [31],
"vidit": [31],
"accident": [16],
"escapp": [18],
"por": [13,[6,14,21,22,23]],
"found": [18],
"preliminar": [22],
"faceva": [18],
"etiquett": [31],
"cliccant": [32,[1,15,29]],
"project_name-omegat.tmx": [31],
"inhibir": [32],
"mesura": [[22,32],[15,17]],
"realisa": [29],
"googl": [20,[18,34],33,[11,27]],
"opendocu": [13],
"hungaro": [6],
"arbor": [14],
"largessa": [27],
"download.html": [18],
"etiam": [11,[0,2,13,16,18,24,31,32]],
"prepar": [32,[18,29,34],[2,33],21,[3,17,22,31]],
"redistribut": [7],
"sourceforg": [9,[8,14,34],[2,33]],
"continua": [22,[4,18]],
"structur": [[17,20,28]],
"goodi": [18],
"hat": [6],
"hau": [6],
"industria": [32],
"last": [[8,21]],
"garanti": [11],
"quatro": [21],
"incapsula": [31],
"editmultipledefault": [8],
"batch": [[14,18]],
"insufficient": [20],
"editfindinprojectmenuitem": [8],
"incapac": [22],
"pro": [18,[21,32],31,[13,20,28],2,27,[15,16],11,[19,22],24,[14,29,34],[8,17],[23,30],9,[4,25],3,[10,12,26,33],5,[0,1],7,6],
"reproduc": [[9,15]],
"implica": [21,[18,23]],
"warn": [22,18],
"nomin": [13,32,[18,31],34,29,[2,17,30],[14,21,27,28],[1,3,6,8,11,12,15,19,20,22,23,24]],
"attent": [16,18],
"sindhi": [6],
"technetwork": [18],
"ininterrumpibil": [27],
"adject": [3],
"pulsat": [[16,30]],
"pulsar": [21,32,[27,30,31]],
"plural": [3,[13,17]],
"retenit": [[15,18,24,31]],
"vider": [21,[19,28],[16,20],[9,11,13,14,15,24,27,30,31,32,34]],
"miscellanea": [34,[11,14,21]],
"perd": [17],
"resultato": [27,[13,16,17,20,25,31,32,33]],
"suggerera": [16],
"exporta": [30,21,17,[19,31]],
"confidibil": [28],
"prudent": [29],
"recerca": [27,32,26,[21,34],33,25,[8,10,11],[13,15,20,22,28]],
"gerer": [[2,20]],
"conto": [18,[20,32],[2,8,9,21,24]],
"jacob": [7],
"lanceamento": [18,[20,22],34],
"confirmar": [[2,31]],
"generic": [24],
"interlingu": [6],
"n.n_windows.ex": [18],
"chang": [[8,18,21]],
"multin": [31],
"pop-up": [[17,30,32]],
"cercator": [24,[3,32]],
"haitian": [6],
"totalment": [[16,31]],
"secund": [32,16,[8,17,18,21,31]],
"circum": [19],
"tractamento": [15,[2,11,16,17,21,24,32,34]],
"heb": [6],
"pseudo-traduct": [[31,34]],
"embarasso": [4],
"brune": [7],
"kanji": [23],
"reparar": [[9,16]],
"convertera": [19],
"multo": [[11,28],[2,13,14,16,21,23,25,29,31]],
"program": [18],
"pus": [6],
"keith": [7],
"numeros": [[9,15]],
"her": [6],
"listat": [8],
"rendeva": [20],
"apportar": [21],
"tran": [[24,27]],
"pagina": [[13,15,21],[5,8,9,18,32],[20,28]],
"resultant": [18,22],
"opportunit": [14],
"lapso": [29],
"precedit": [25],
"camtasia": [15],
"majuscula": [21,25,[8,18,27,32]],
"cercar": [27,21,26,[8,11,29,34]],
"cercat": [[3,34]],
"beneficio": [18],
"separa": [28],
"n.n_mac.zip": [18],
"mancant": [[21,32],8,[5,17,25]],
"decomprim": [18],
"tabl": [21,25,8,32,33,[5,6]],
"platteforma": [14,18,20],
"doc-license.txt": [14],
"yid": [6],
"copyflowgold": [15],
"thema": [21],
"paulatim": [31],
"methodo": [18,27,34,[15,33]],
"editor": [27,5,[32,33],[18,21,30],[0,1,2,6,8,11,13,15,16,17,22,23,24,34]],
"pseudotranslatetyp": [[18,31]],
"hhc": [15],
"reverso": [15],
"marcara": [31],
"hhk": [15],
"consistent": [[21,28]],
"desira": [31,[13,18],[16,27]],
"obra": [[11,14,15,16,18,19,31,32]],
"progred": [14],
"evoca": [20],
"velocit": [20],
"hic": [14],
"projectclosemenuitem": [8],
"ulteriorment": [[13,28]],
"hin": [6],
"clarment": [23],
"viewmarknonuniquesegmentscheckboxmenuitem": [8],
"hio": [7],
"importa": [32,15],
"major": [23,28,[8,11,13,14,15,16,20,24]],
"interrupt": [28,[5,16,34]],
"tres": [31,15,[2,12,14,16],[9,17,19,23,32]],
"inspir": [1],
"consider": [[14,15,30]],
"assecura": [2,[15,18]],
"fundo": [32,[21,31],[8,23,27,28]],
"group": [[14,32]],
"apparentia": [[15,21,32]],
"archivo": [14,[4,12,19]],
"findinprojectreuselastwindow": [8],
"discoperi": [[17,22]],
"readme.txt": [[7,13,14]],
"campo": [32,[13,21],[17,18,30],[2,27,29],[11,15,20,22,25]],
"languagetool": [0,[11,33,34],[1,3,21]],
"commuta": [15],
"discopert": [25],
"source.txt": [30],
"apparera": [13,28,[1,2,20,21,29,31]],
"files.s": [1],
"merita": [2],
"siband": [7],
"croat": [6],
"tigrinya": [6],
"exchang": [17],
"output-tag-valid": [18],
"projectlock": [2],
"request": [20],
"tirant": [32],
"currseg": [1],
"their": [0],
"generat": [[14,20,31],[11,13,21]],
"implicitement": [2],
"novemb": [17],
"point": [13],
"incontra": [13],
"torno": [30],
"explica": [11],
"general": [7,14,24],
"l4j.ini": [18],
"grammatica": [0],
"demo.taas-project.eu": [10],
"identifica": [13],
"archiva": [[11,31]],
"generar": [19,31,[24,28,33]],
"dimension": [15,16],
"vinogradov": [7],
"facil": [16,15,[19,23,27,32]],
"process": [18,[13,16]],
"membro": [2,31],
"alternativa": [[21,30,32],[2,14,15,18,23,24,27]],
"andrzej": [7],
"alibi": [[29,31]],
"chec": [6],
"downloaded_file.tar.gz": [18],
"normalment": [18,22,[1,16]],
"signa-libro": [13],
"reflectera": [14],
"facit": [[31,32],[18,21,22,30],[2,15,16,17,24]],
"miscellane": [22,[6,29,33,34]],
"account": [[10,20]],
"dhttp.proxyhost": [18],
"cyano": [21],
"tentara": [18],
"hmo": [6],
"facient": [3],
"barra": [32,18,[21,22,25,30]],
"marca": [21,8,30,[28,29],[24,32],[2,13,16,17,18,20,31]],
"fortiamento": [31],
"ignorar": [29],
"promovit": [31,18],
"ignored_word": [34],
"strict": [31],
"yor": [6],
"you": [7,14,[18,21]],
"prime": [21,[2,17,32],18,[9,16,28,31],[4,5,13,14,19,20,24,25,27,29]],
"aliquot": [32],
"prima": [1],
"removent": [32],
"contient": [17],
"citat": [25,33],
"specificar": [20,18,[8,13,14,15,22]],
"corrector": [29,[0,21,34],[14,33],[11,20,22]],
"infra": [2],
"re-formatt": [20],
"activa": [[13,20,24]],
"replaciar": [21,8,27],
"configur": [18,[13,22],[0,8,10,14,24,32]],
"producit": [31,15],
"affic": [16],
"traductor": [31,[20,32],[15,18,34],[2,9,13,21,28,33]],
"buloichik": [7],
"traducton": [18],
"corpor": [29],
"mentionar": [4],
"optionsworkflowmenuitem": [8],
"interior": [16],
"releas": [18],
"peter": [7],
"cordonni": [7],
"sparc": [18],
"segmentar": [28,31],
"tracia": [32,[9,16]],
"processar": [[13,21],31],
"explic": [[0,2,18,22]],
"subjecto": [31,[9,14,22],[2,6,11,24,29,33,34]],
"gestion": [[21,34],[11,16,33],[14,15]],
"sandra": [7],
"adhuc": [31,[15,21,24]],
"dactylographar": [[15,17]],
"represent": [16],
"debilement": [1],
"frequentement": [19,[2,11,13,15,31,32]],
"configuration.proprieti": [18],
"discurso": [20],
"rtl-ltr": [5],
"superscrib": [[21,31]],
"brasil": [18],
"cancellar": [16],
"mantenit": [2,31],
"intervallo": [[15,21,22]],
"prior": [4],
"aperit": [[18,21,30,31]],
"aperir": [32,18,[8,21],[14,16,24],[2,11,19]],
"simplement": [18,[9,14,17,28,29,31,32]],
"passar": [20],
"fide": [[2,17,31]],
"hrv": [6],
"schematron": [15],
"passat": [18],
"dhivehi": [6],
"conjunctement": [24,32],
"file-source-encod": [13],
"session": [[14,18,24,26]],
"dominio": [10,[1,11]],
"univers": [27,34,33],
"vilei": [7],
"piotr": [7],
"tang": [7],
"primo": [[8,21,32]],
"blanc": [13,[21,25],[8,15,17,28,30]],
"respecto": [[24,30,31]],
"removera": [16],
"divid": [[11,31,32]],
"collabor": [2],
"retraduc": [31],
"fontal": [15],
"editexportselectionmenuitem": [8],
"solo": [[2,4,11,13,21]],
"monitor": [9],
"home": [14,5,18,[0,1,2,3,4,6,7,8,9,10,11,12,13,15,16,17,19,20,21,22,23,24,25,26,27,28,29,30,31,32,34]],
"michael": [7],
"videra": [[20,21]],
"condit": [[16,17]],
"reimplaciarea": [20],
"eliminar": [13],
"projectaccesstargetmenuitem": [8],
"build.xml": [18],
"alphabeto": [[22,23]],
"hun": [6],
"lector": [32,[11,34]],
"paragrapho": [13,[24,28],[11,15],31],
"hope": [7],
"calca": [17,32],
"x_windows.ex": [18],
"herero": [6],
"aligndir": [[18,22]],
"desiderar": [[13,28,30]],
"system-host-nam": [13],
"action": [21,8,[5,12,18,30]],
"georgian": [6],
"creat": [14,32,31,15,[2,13,17,19,21],[16,28]],
"python": [1],
"habitualment": [14,[16,22],[11,18,31]],
"es_mx.dic": [29],
"conformement": [21],
"infix": [15],
"crear": [[21,31],34,18,[2,10,15],[8,24],[14,16,17,19,20,27,28,30,33]],
"codif": [13,23,34,17,15,[11,32,33]],
"codic": [[8,31],29,6,34,[13,28],[18,33],[1,2,5,11,14,20,22,23]],
"tarbal": [12],
"hindi": [6],
"singl": [13],
"comencia": [13],
"operar": [[16,23,34],33,[2,21,30,31]],
"inscribent": [15],
"redactor": [21],
"omegat-development-request": [9],
"natur": [16],
"periodo": [[25,28],18,30],
"hora": [[22,31]],
"adress": [20,18,[2,34]],
"producto": [[11,15]],
"separatement": [21],
"file": [13,34,18,15,31,14,32,21,23,19,17,27,24,[22,29],11,33,16,[2,8],30,[12,20],28,1,9,0],
"fila": [21,[13,27,31]],
"member": [14],
"meni": [17],
"communment": [15],
"experientia": [[16,24]],
"impacchett": [18],
"within": [17],
"cadita": [27],
"tard": [18,[2,13,14,24,30,32]],
"catti": [3],
"menu": [8,33,21,32,34,18,[1,11,13,14,17,22,23,24,28,30],[15,19,20,29]],
"ment": [16,24],
"quomodo": [[14,21]],
"mens": [[22,31],18],
"concernit": [28],
"exercit": [16],
"machina": [[18,20,22],[1,21,30]],
"continit": [[1,18,31]],
"cyril": [23],
"hye": [6],
"similantia": [[16,30]],
"a-za-z": [25,27],
"return": [21],
"crede": [[22,31]],
"consonant": [25],
"serva": [16],
"radio": [27],
"fini": [26,31],
"source-pattern": [[18,22]],
"fine": [13,[16,31]],
"find": [31],
"adher": [9],
"accepta": [[14,32]],
"servi": [30],
"radic": [[21,31],[3,8,18,24,32]],
"errat": [32],
"chua": [7],
"stilist": [[11,28]],
"occur": [18],
"problema": [[17,34],[16,23],[12,33],[2,15,18,21,22,31,32]],
"difficult": [14],
"osset": [6],
"convenient": [2],
"specialist": [32],
"xliff": [15],
"true": [18],
"orthograph": [29,34,33,[11,21],[0,14],[8,20,22]],
"position": [15],
"inviar": [20],
"present": [16,[13,18,32],[2,11,12,14,22,27,31]],
"mesm": [31,[18,27],21,[2,11,16,23,25],[3,13,15,17,22,28,29,32]],
"groovi": [1],
"rotara": [28],
"evitar": [[2,31],[11,15,22,30]],
"flexibilit": [28],
"diligentia": [4],
"accordant": [28],
"execut": [21,[1,14]],
"kmenueditor": [18],
"petra": [[17,31]],
"tortoisesvn": [2],
"salvara": [21],
"rubr": [13,[2,16,31]],
"mantenera": [14],
"messageformat": [16],
"tracta": [11,[14,15,16,27,28]],
"kmenuedit": [18],
"chuang": [6],
"lsps": [20],
"akan": [6],
"kashmiri": [6],
"xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx": [18],
"writer": [19],
"dalloway": [28],
"rubi": [1],
"dzongkha": [6],
"virguletta": [[17,25]],
"annot": [32],
"cover": [14],
"reflect": [[14,16,18,21]],
"taxo": [25],
"ignorant": [21],
"immedi": [22],
"financiariment": [9],
"largement": [2],
"assi": [18,32,31,[27,28],15,2,[16,17],[11,13,19,22],[9,14,20,23,25,30]],
"pulaar": [6],
"serbo": [6],
"edita": [[29,32]],
"britann": [12],
"locmanag": [15],
"regex": [25,33,34],
"interferera": [18,24],
"meta": [8],
"declar": [13],
"except": [28,25,[26,34]],
"programm": [1,[16,21]],
"suppl": [[10,21]],
"naviga": [18,[29,31]],
"obscur": [21],
"adiel": [7],
"functionar": [29],
"sango": [6],
"incremento": [9],
"global": [34,[13,24]],
"fiss": [28,[24,32]],
"occitan": [6],
"tags-omegat": [16],
"listant": [29],
"technicament": [13],
"free": [7],
"valor": [13,25,22,[18,32],31,20],
"immunit": [31],
"unregula": [28],
"ibi": [[1,12,18,28,32]],
"face": [18,22,[1,2,3,14,16,19,27,29,31]],
"ibm": [18],
"supra": [[14,18,31],[0,12,30]],
"fish": [3],
"ibo": [6],
"directement": [18,13,[14,21,28]],
"transverso": [32,5,1],
"operation": [[16,34],[31,32,33]],
"calculation": [32],
"monera": [28],
"dependera": [[0,15,31]],
"ancora": [31,[2,4],[5,16,21,24,25,32]],
"sublin": [0,[17,29]],
"commando": [18,8,34,21,20,22,14,24,32,2,[4,5,11,23,30,31,33]],
"jean": [7],
"romanian": [6],
"poco": [14],
"off-lin": [18],
"collection": [10,[11,28]],
"temporaneement": [32],
"necessitar": [[24,25,32]],
"ido": [6],
"conforma": [28],
"installar": [18,34,29,20,[12,33],[2,11],[19,21,32]],
"recercar": [27,[19,26]],
"evidenti": [21],
"idx": [12],
"qua": [20],
"simil": [32,[2,21,31],[11,18,26,27]],
"que": [18,31,32,[2,13],16,19,14,[20,24],[15,17,21],22,28,[11,29],27,[23,30],[1,5],[3,4,8,9,25,26],0,[6,12]],
"empleo": [29],
"occultar": [21],
"arriv": [31],
"qui": [[17,21,31]],
"commerci": [[30,32]],
"detect": [16],
"quo": [29],
"mutar": [[8,18]],
"delivra": [28],
"causar": [14],
"actualis": [32,[2,6]],
"projectaccesscurrentsourcedocumentmenuitem": [8],
"desider": [18,[20,21,24,31,32]],
"basat": [20,11,[1,3,13],[2,12,17,18,21,28,29,30]],
"compartir": [[2,31]],
"linux": [18,34,[2,14],[11,17,20,22,25,32,33]],
"sinistr": [15,[1,32]],
"actualment": [21,31,[5,11,15,24,32]],
"checkout": [2],
"releva": [[16,17,18,32]],
"masculin": [3],
"hierarchia": [14],
"sembla": [21],
"inferior": [32,[30,31]],
"elimina": [[27,29]],
"zha": [6],
"popup": [32],
"ifo": [12],
"popul": [10],
"comment": [13],
"sparniant": [31],
"characteristica": [1,[20,21]],
"brasilian": [29],
"comprend": [31,[2,14]],
"zho": [6],
"accelerar": [31],
"octal": [25],
"serraino": [[7,33]],
"vigent": [31],
"gruppo": [16,34,[9,14],33,[11,19,22,25,31,32]],
"xx.docx": [13],
"prefix": [[23,31],[21,25,33,34],[15,22,24,28,30]],
"consist": [32,[14,16,18]],
"preparation": [34,22,[24,33]],
"cargamento": [[13,30]],
"price": [20],
"optionsautocompleteautotextmenuitem": [8],
"fernández": [7],
"grammat": [11],
"dependent": [18,[21,32],[13,16,31]],
"zip": [14],
"profession": [20],
"versiona": [18],
"hesit": [14],
"vader": [9],
"declin": [17,3],
"ibai": [7],
"yahoogroups.com": [9],
"hereditag": [31],
"concis": [[4,12]],
"sdlxliff": [15],
"customer-id": [18],
"annid": [16],
"patrono": [[10,13]],
"remarcant": [15],
"requesta": [20,[18,21]],
"invariabil": [30],
"term.tilde.com": [10],
"initia": [18,31,[19,28]],
"church": [6],
"iii": [6],
"requir": [32,[15,21]],
"resimilara": [18],
"predisponit": [2],
"incrementar": [30],
"cader": [18],
"kyle": [7],
"viewmarknotedsegmentscheckboxmenuitem": [8],
"integrit": [14],
"initio": [[5,18],14,[11,17,25,28,31]],
"japones": [[23,28],[6,18,21,22,31]],
"subscrib": [9],
"gotomatchsourceseg": [8],
"abstract": [33],
"appropri": [13,[2,11,23,31]],
"lingala": [6],
"yandex.api.key": [20],
"optionssaveoptionsmenuitem": [8],
"excel": [13],
"runn": [27],
"stardict": [12,34],
"omegat.l4j.ini": [18,[10,20]],
"quantit": [[10,18]],
"span": [13],
"prefer": [7,[14,28]],
"perfect": [31],
"hans-pet": [7],
"arrangi": [32],
"remarcara": [2],
"space": [5,21],
"iku": [6],
"pijffer": [7],
"zakharov": [7],
"commercio": [30],
"simpl": [15,34,23,30,17,16,[2,11,13,18,20,21,29,33]],
"open-sourc": [1],
"from": [14],
"habilitar": [[18,20,29]],
"creol": [6],
"hardwar": [22],
"desirabil": [21],
"thunderbird": [29],
"ile": [6],
"editselectfuzzy3menuitem": [8],
"ubuntu": [11],
"comprimit": [[14,31,34]],
"ill": [2,[21,27,31],[4,14,20,32]],
"alicubi": [[18,31]],
"project.projectfil": [1],
"fals": [[18,32]],
"rupt": [16],
"trovat": [21,[17,32],[18,31],27,[0,3,4,13,14,22,25]],
"phillip": [7],
"compatibil": [18,17],
"trovar": [18,[9,11,14,22],[1,4,8,15,23,25,27,32]],
"collabora": [2],
"disveloppator": [9,14],
"pone": [31,21,[15,32]],
"explicitement": [[13,22]],
"frequent": [18],
"interact": [25],
"sentira": [18],
"vincent": [7],
"error": [[16,18],[22,31],32,0,[11,19],[4,14,15,21,24]],
"momento": [[2,19,31]],
"debitement": [[16,17,18],[0,14,15]],
"malayalam": [6],
"public": [[7,14],10],
"pomo": [21],
"russ": [18],
"ina": [6],
"reiniti": [8],
"ind": [6],
"contra": [[32,34],[17,29]],
"pt_br.aff": [29],
"tmx2sourc": [31],
"oromo": [6],
"precedentement": [18,[15,24]],
"kongo": [6],
"ini": [15,[18,34]],
"ubicar": [21],
"proced": [[11,12,15,24]],
"kyrgyz": [6],
"flecha": [[27,32]],
"pollut": [31],
"restring": [27],
"facerea": [27,[2,32]],
"dhttp.proxyport": [18],
"repar": [31],
"trado": [17,34,33],
"appellar": [24],
"negat": [25],
"rememora": [24],
"objecto": [1,28],
"subrip": [15],
"kangaroo": [11],
"sensat": [23],
"sorta": [[31,32]],
"implementar": [17],
"describ": [11,32,[5,21]],
"score": [32],
"percussion": [8,30],
"dextero": [2,[17,18,30]],
"navajo": [6],
"rar": [[24,31]],
"persona": [2,11],
"appendix": [[0,1,2,3,5,7,8,9],[4,6],[22,28,31,34]],
"alineamento": [[18,22]],
"ipk": [6],
"passo": [[9,21]],
"illustr": [28],
"passa": [0],
"ips": [[2,13,16,19,23,25,31]],
"conserv": [[2,13,16,17]],
"usant": [27,[13,17,20,21,28,29,31,32]],
"facilement": [15],
"copia": [31,21,2,[22,34],18,[9,13,14,16,19,24,29,32,33]],
"specificava": [27],
"aaa": [25],
"solari": [18,14],
"ambient": [2,22,[14,18,20]],
"priorit": [17,[28,34],33,21],
"manual": [32,[15,21],[4,7,8,9,14,34]],
"postea": [[5,13]],
"referentia": [11,31,[13,21,25,32]],
"aar": [6],
"usara": [18,[13,14,19,22,29]],
"kirundi": [6],
"bengali": [6],
"reimplacia": [21,26,[13,32]],
"physicament": [29],
"appendic": [[5,11,21,24,32]],
"azerbaijani": [6],
"scoto": [6],
"marshalles": [6],
"close": [32],
"abc": [25],
"rcs": [31],
"navaho": [6],
"maximo": [[8,20]],
"abk": [6],
"textual": [31,11,[23,28]],
"materi": [2],
"illac": [[18,19]],
"precedent": [32,31,13,[5,14,17,27],[0,11,16,20,21,22,23,25]],
"concordant": [27],
"isl": [6],
"pote": [18,31,32,15,13,[2,11,21],[14,17],[22,27],[16,24],28,[19,30],29,8,9,25,[0,1,23],[4,5,10,12,20]],
"iso": [6,34,23,33,[5,11,17,22,28,31]],
"controlo": [[2,24],28,[13,21,29],[8,16,25,30,33],[0,11,18,31,34]],
"tatar": [6],
"ist": [18,21,14,31,13,32,[11,30],16,[20,22,24],17,[2,29],[19,23,28],15,[3,5,9,10,25,27,33]],
"log.txt": [16],
"concord": [32,21],
"zul": [6],
"controla": [20,29,[12,15,16,18,21],2,[22,31]],
"aggreg": [13],
"inactiv": [21],
"post": [28,16,18,17,[2,5,8,13,14,21,25,26,27,32]],
"glossary.txt": [17],
"ita": [31,[6,24]],
"add": [2,[13,18,28],[1,8,23,27,30]],
"cambio": [2,[18,28],21,[5,13,14,15,16,22,24,31,32]],
"initi": [13,[8,31],[18,21],[15,22,23,30,34],[2,3,19,24,25,32,33]],
"chines": [31,[6,18,21]],
"respect": [15],
"cambia": [23,8,[28,31],[13,15,16,18,21,29]],
"pre-configur": [21],
"inact": [21],
"decomprimit": [18],
"optionsautocompleteshowautomaticallyitem": [8],
"reimpler": [31],
"larouss": [32],
"contemporane": [12],
"untar": [12,18],
"benjamin": [7],
"filters.conf": [22,18],
"retenera": [2],
"possibilement": [31,[3,14,25]],
"repositorio": [2,31,34],
"modifi": [[7,14],32],
"auto-completar": [5],
"tractar": [[15,20],[13,17,19,23,27]],
"tractat": [15,[13,31,32],[2,11,21,24,27,30]],
"contin": [[14,31],15,32,[18,22],21,30,[11,17],[1,2,8,9,10,12,13,23,29,33]],
"anyth": [14],
"afr": [6],
"mover": [5,21,19,32,[27,28,33,34]],
"pgdn": [5],
"saboga": [7],
"non-visibil": [[13,24]],
"reservoir": [20],
"targetlanguag": [13],
"directori": [2],
"age": [[5,14,31]],
"quanto": [32],
"indicar": [[15,18,31]],
"filtro": [13,24,34,[27,33],[14,15,23,32],21,[2,8,19,28,31]],
"sensit": [[25,27]],
"memoria": [31,11,34,32,18,21,[19,24,33],[22,27],[14,28],[2,15,20],[3,16,17,25,26]],
"movent": [30],
"collega": [32],
"concorda": [25,13,27,[31,32],[17,28],16],
"omittera": [13],
"referit": [[15,18,28]],
"relancea": [8],
"properti": [[15,22]],
"defecto": [9,34,[20,33],[14,21,31]],
"movit": [[21,32]],
"durant": [14,[18,24],[21,28,30,32],[2,11,13,17,19,22,31]],
"fisher": [3],
"utilisabil": [[8,20]],
"editselectfuzzyprevmenuitem": [8],
"defect": [31],
"isto": [18,31,32,14,15,[13,30],[22,23,25],[2,16,21,24,28],[3,5,12,17,20,29]],
"necesstara": [18],
"copiar": [29,32,[5,21],[8,14,18,19,31]],
"post-processo": [24,21],
"copiat": [32,[2,13],[16,21,24,31]],
"simpledateformat": [13],
"sempr": [31,[13,16]],
"script": [1,[18,21],34,14,[20,22,24,30,33],[0,2,9,11,15,16,28,31,32]],
"system": [2],
"exir": [31],
"maximisa": [32],
"arrestara": [[18,30]],
"spellcheck": [29],
"partial": [34,21,32,30,31,[5,15,16,19,24,27]],
"aid": [11],
"contrariment": [32],
"algorithmo": [[3,8,21]],
"unic": [32,[8,21],[16,20,24,27]],
"cinqu": [[21,31]],
"khmer": [6],
"exclusion": [24],
"querer": [10],
"local": [2,[13,18],[15,21,31]],
"incommodit": [15],
"locat": [18,21,[1,5,14,17]],
"replaciamento": [26],
"kwanyama": [6],
"afflig": [14],
"specialis": [32],
"cree": [6],
"segmenta": [31],
"prototypo": [1],
"miscer": [34,15,17],
"crea": [21,[2,14],[8,19,24,31],[16,18,22,29,32,34]],
"resto": [[2,32]],
"duplic": [16,14,[32,34]],
"segmento": [21,32,31,30,27,5,16,28,8,[11,13],34,[15,20,24],19,[17,18],[1,26],[10,22],[2,14]],
"aka": [6],
"resta": [2],
"applicabil": [14],
"nunquam": [13],
"cortina": [29],
"tsonga": [6],
"es_mx.aff": [29],
"eligit": [21],
"correspond": [20,[2,14,21,23,29,31,32]],
"ingenio": [20],
"mode": [18,22],
"factura": [20],
"movera": [24],
"t13": [2],
"navigant": [32],
"toolsshowstatisticsstandardmenuitem": [8],
"modo": [18,22,15,34,32,16,[3,23,28,31,33]],
"oblid": [12],
"postar": [22],
"simplic": [[16,25],[17,28],[0,11,15,31,33,34]],
"aperira": [21,32,[2,29]],
"read": [[13,14]],
"umarov": [7],
"alt": [5,[1,8,18,20,31]],
"rng": [15],
"real": [20],
"unit": [31,28,21],
"guaraní": [6],
"amb": [16,[15,18,27,31],[17,20]],
"modalit": [11],
"wildrich": [7],
"pinfo.list": [20],
"atqu": [32],
"veter": [11],
"amh": [6],
"collect": [28,32,[13,17,23,25,27,34]],
"unix": [21],
"adaptar": [[11,16]],
"roh": [6],
"querit": [10],
"ron": [6],
"devenir": [16,[9,14,20,32]],
"resolv": [[2,21]],
"convenibil": [29,18],
"and": [7,14,[2,15,18,20,22,27,29,32]],
"ration": [21,[2,14,18,29]],
"modifica": [32,34,30,13,31,21,33,8,19,15,[11,18,22,23,24,27]],
"obsolet": [32],
"ani": [7,18],
"automatisara": [18],
"colligit": [[14,31]],
"ant": [[18,31],[21,28],[16,29],5,[0,2,3,13,14,15,17,19,20,22,24,25,26,27,32]],
"escappa": [25],
"minuta": [4,[2,19,21,22],[11,18,28,31,33]],
"sardo": [6],
"previement": [21],
"dextera": [15,17],
"rememorar": [19,33],
"application": [11,21,[15,18,20],31,[13,29,32]],
"sundanes": [6],
"removit": [[16,24,31]],
"jnlp": [18],
"helplastchangesmenuitem": [8],
"omegat.ex": [18],
"grado": [31,2,[20,28,32]],
"displic": [[31,32],[10,13,18,22,26]],
"sourcetext": [32],
"multo-paradigma": [1],
"kuanyama": [6],
"legent": [15],
"jam": [32,[18,24],[11,21,22,29,31],[1,2,8,14,15,17,20,26,28,30]],
"jar": [18,22,14,31],
"api": [20,18],
"replaciara": [21],
"tabella": [[15,21,28,32],[5,6,8,16]],
"jav": [6],
"editselectfuzzy2menuitem": [8],
"app": [18],
"morpholog": [20],
"assames": [6],
"acto": [4],
"alex": [7],
"zulu": [6],
"oriya": [6],
"melioration": [21],
"omegat.sh": [18],
"vide": [31,14,[21,32],25,[2,15,18,20,24,28],[0,13,19,27,30],[5,8,9,17,22,29]],
"gujarati": [6],
"editselectfuzzynextmenuitem": [8],
"completamento": [[11,28]],
"read.m": [13],
"ara": [6],
"are": [[14,18]],
"arg": [6],
"popular": [2,[1,30]],
"paypal": [9],
"substantia": [18,22],
"vice": [32,18,[2,25],31,[13,15,19,20,21,24],[8,16,17,27],[1,3,5,9,14,22,23,30]],
"dupl": [18,25],
"art": [29],
"rtl": [15,34],
"percip": [24],
"facilit": [11],
"significato": [27,16],
"jdk": [18],
"call": [21],
"clauder": [32,21,[8,19,26,27]],
"ask": [14],
"taggat": [[31,34]],
"asm": [6],
"calc": [17],
"tabul": [17,[28,32],25],
"pagabil": [20],
"toolsshowstatisticsmatchesperfilemenuitem": [8],
"run": [27,32,[6,18]],
"resulta": [14],
"linguetta": [32],
"rus": [6],
"either": [7,14],
"differentement": [[5,15,28]],
"aymara": [6],
"titlecasemenuitem": [8],
"dictionario": [29,[12,34],32,33,14,11,[3,8,17,25]],
"editcreateglossaryentrymenuitem": [8],
"simplificar": [[15,16,32]],
"ital": [13,16],
"alic": [31],
"bold": [16],
"alia": [[2,16]],
"introduc": [21],
"privat": [10,20,[2,18]],
"name": [14,[13,18]],
"canc": [5],
"alin": [[22,34],33],
"moldavian": [6],
"meta-tag": [13],
"android": [15],
"lingua-pai": [28],
"ava": [6],
"pushto": [6],
"comput": [[18,23],32,[11,33],[2,13]],
"ave": [6],
"capac": [[14,18,27]],
"introduct": [33,11,[0,1,2,3,19,20,34],4],
"enabl": [8],
"spatio": [13,25,[5,21,27,28],[8,32],[11,15,16,17]],
"describit": [18,[21,27]],
"associ": [15,18,[11,21]],
"pgup": [5],
"quando": [31,32,21,[18,30],22,[13,15,16,27],[14,17],[0,5,11,19,20,24,28],[1,2,10,23,25]],
"target": [14,[1,19,21,31,34],[2,24]],
"fabián": [7],
"config-dir": [[18,22]],
"sequent": [21,25,8,13,32,[2,18],31,[14,30],[1,19],[11,15,20,22,23,24,27],[5,10,12,16,17,26,28]],
"casa": [5],
"empleat": [21],
"indolor": [9],
"manten": [2,[9,24,31]],
"evidentement": [32],
"tibetano": [6],
"singulo": [16,34],
"caso": [31,32,18,[2,24],[11,13,15,16],[3,21,22],[20,23,28],[5,25,27,30]],
"gent": [4],
"auto-texto": [21],
"effectivement": [24],
"rend": [[13,14,32],[15,22],[11,21,25,30,31]],
"semper": [[8,13,14,16,17,20,21,24,28]],
"case": [30],
"obten": [18,20,[12,14,29]],
"destin": [13,[15,31],21,17,[16,32,34],[10,26],[20,22,24],[0,3,8,11,18,23,27,29,30]],
"capabil": [[13,31,32]],
"presentant": [21],
"targettext": [32],
"aym": [6],
"style": [31],
"explor": [21],
"comportamento": [34,30,21,[5,18,31,32,33],[11,15],[3,8,13,14,16,23]],
"evalut": [32],
"care": [14],
"orang": [21],
"letton": [6],
"increment": [16],
"cifra": [18,[21,22,25,31]],
"aaabbb": [25],
"aze": [6],
"compil": [[15,20]],
"disveloppamento": [[2,9],34],
"edittagpaintermenuitem": [8],
"mort": [15],
"protect": [[19,24]],
"bokmål": [6],
"optionscolorsselectionmenuitem": [8],
"cata": [32,[13,21],[15,19],[25,27,30,31],[2,8,9,11,16,17,20,22,24,28]],
"more": [7],
"display": [32],
"viewmarknbspcheckboxmenuitem": [8],
"unicod": [23,[17,25,33,34]],
"activar": [21,[13,16],[20,24]],
"profund": [17],
"pensa": [9],
"partita": [9],
"en-us": [31],
"concordara": [25,[13,24,27]],
"motu": [6],
"msgstr": [13],
"espaniol": [20,29,6],
"poter": [[2,14]],
"katarn": [7],
"gere": [2],
"discarga": [18,2,[8,12,14,21]],
"ducer": [[21,31]],
"establit": [[2,28]],
"bashkir": [6],
"important": [14,11,22,[15,18,19,20,31,32]],
"delimit": [17],
"phrase": [28,[11,27],[19,24],[0,26,32,34],[8,16,20,25,31]],
"omegat.project": [18,[2,14,32,34]],
"formattar": [34,16,33],
"imagin": [0],
"revelar": [15],
"periculo": [31],
"identificabil": [31],
"targetcountrycod": [13],
"retalio": [1],
"webstart": [18],
"continu": [[9,13]],
"presentara": [19],
"insert": [21],
"alto": [[5,27,28,30,32]],
"perseverant": [4],
"resid": [[18,22]],
"publicament": [9],
"rete": [[2,11],18],
"administrator": [2],
"incombra": [27],
"patient": [4],
"sag": [6],
"curar": [13],
"messag": [18,[2,22,31],[14,32]],
"regula": [28,34,24,11,16,[0,14,15,33],[18,25,32],[13,20]],
"subvers": [2,34],
"san": [6],
"labora": [31],
"move": [21,[5,28],32,16],
"similant": [11],
"resp": [3],
"jpn": [6],
"also": [34],
"alsi": [18,32,21,31,17,[13,15],[11,20,22,29,30],[0,2,3,8,9,14,19,26,28,33]],
"technologia": [[17,18]],
"resx": [15],
"differ": [18],
"situat": [[23,31]],
"mous": [0],
"compli": [30],
"favor": [[6,16,18]],
"yandex": [20,[18,34],33],
"habilitara": [18],
"pāli": [6],
"interruption": [[13,15,28]],
"a123456789b123456789c123456789d12345678": [18],
"viewmarkwhitespacecheckboxmenuitem": [8],
"equivalent": [32,[12,14,18,31]],
"bad": [20],
"potenti": [21],
"complet": [31,32,[5,8,11,13,18,20,21,27,30]],
"lucen": [34],
"bak": [[2,6,14]],
"compler": [14],
"projecto": [31,21,34,32,2,14,[19,24],[8,18],33,[13,22],17,[9,11,29],[27,28],[1,3],[12,15,20],[0,5,10,30]],
"bam": [6],
"indisponibil": [[18,22]],
"offer": [[2,20],[13,17,18,23,24,30,32]],
"sublinea": [21],
"bat": [18],
"optionalment": [[18,22,31]],
"summario": [11,[32,33]],
"complex": [[11,15,25]],
"sch": [15],
"tenent": [32],
"participar": [[31,34],2,[14,33]],
"jre": [18,14],
"nepali": [6],
"optionsfontselectionmenuitem": [8],
"posit": [21,32,[28,31],[1,14,16,17,27,29]],
"qualcunqu": [32,[21,31],19,[2,5,8,14,17,18,22,28,29]],
"laborar": [11],
"partialment": [[20,32]],
"translatedfil": [22],
"secur": [18,[3,23,24,29,31]],
"naturalment": [31,[17,24,29],[18,20,23,32]],
"aaron": [7],
"plano": [21],
"rapidement": [[11,27,32]],
"questionario": [14],
"project2": [2],
"permult": [9],
"schema": [25,[13,15,28]],
"project1": [2],
"hebreo": [6],
"freebsd": [[14,25]],
"icon": [18,[2,14],21],
"deler": [[16,24,30],[17,18,19]],
"delet": [30,16,34],
"projectaccessglossarymenuitem": [8],
"see": [34,7],
"incastrar": [15],
"sed": [25,[21,24,27,28]],
"vito": [[7,33]],
"subsequent": [[15,18,31]],
"vade": [8,[32,33],[30,34]],
"omission": [4],
"licentia": [[12,18,21,31]],
"seq": [17],
"developerwork": [18],
"ser": [[15,24,31],[2,13,14,28,32]],
"bulgar": [6],
"causara": [[16,30]],
"set": [[14,18,22]],
"vist": [28,18],
"orthographia": [29],
"categoria": [[25,32],33],
"vacu": [[28,30],[2,13,14,21,31],8,[17,18,27,32,34]],
"optionsrestoreguimenuitem": [8],
"depositar": [9],
"procedura": [31,22,[2,11,29]],
"selig": [13,18,21,[2,14,29],[19,22,27,32],[3,8,28,30,31],[1,5,10,23,24,34]],
"mergit": [[2,24]],
"fleurk": [7],
"functionara": [29],
"bihari": [6],
"offic": [16,[13,15]],
"terminolog": [[17,34]],
"bel": [6],
"pre-traduc": [31],
"ben": [[6,18,19,26,27]],
"fluer": [[18,20],[2,19,21,32,33,34]],
"extremement": [16],
"sequentia": [16,[27,28]],
"restara": [[13,26]],
"projectsavemenuitem": [8],
"tenit": [[24,32]],
"joel": [9],
"adir": [27],
"adjuta": [34,[21,32],[8,33],[4,9,15,19,22,31]],
"respic": [18],
"recollig": [21],
"debe": [18,13,2,[16,22],[8,17],[20,29,30],[14,15,28],[10,11,21,31,32],[23,25]],
"pptx": [15],
"adjust": [32],
"example_project": [14],
"compar": [[3,16,18,32]],
"projectoject": [14],
"cursor": [21,5,[28,32],17],
"signa": [2],
"xhosa": [6],
"sia": [[11,32],[2,21],[4,10,13,15,20,27,31]],
"sloven": [20],
"pendant": [13],
"sic": [22],
"candid": [[4,31]],
"signo": [[17,32],2,30],
"sin": [[2,13,17,30,31,32],[4,14,18],[6,11,19,20,21,26,28]],
"client": [2,31,[11,16,20,32]],
"promot": [[18,34],21],
"promov": [31,34,[18,24,33]],
"conversion": [15],
"historia": [21],
"codificar": [20],
"someth": [14],
"svedo": [6],
"punjabi": [6],
"tiago": [7],
"mundo": [23],
"sloveno": [[3,6,17]],
"functionant": [14,[2,18,23,29]],
"foundat": [7],
"bih": [6],
"expert": [[16,18,22,25,31]],
"select": [21,[8,30],[3,18,27],[5,12,13,28,32,33,34]],
"pre-traduct": [34],
"retornar": [32,[13,16,19,21]],
"bin": [14],
"warranti": [7],
"apertium": [20,34,[11,33]],
"cerca": [27,34,[25,26],[20,33],[3,12,19,21]],
"bit": [23],
"bis": [[6,25]],
"kaptain": [20],
"output": [17,[8,21]],
"projectopenmenuitem": [8],
"autom": [18],
"yoruba": [6],
"decim": [30],
"ordinari": [14],
"decis": [[20,31]],
"toolsvalidatetagsmenuitem": [8],
"sequer": [[18,20,31,32]],
"concordantia": [32,21,34,31,8,30,[16,33],27,[3,5,13,14,17],[15,19,22,24,25,28]],
"vertic": [14,[25,34]],
"decid": [24,[2,16,30,31]],
"meliora": [3],
"attribu": [31],
"autor": [32,21,27],
"levar": [[31,32]],
"levat": [21],
"molto": [32],
"slk": [6],
"privilegio": [2],
"john": [7],
"plurim": [28],
"kazakh": [6],
"slv": [6],
"viewmarktranslatedsegmentscheckboxmenuitem": [8],
"corrump": [16],
"valu": [15],
"optim": [2],
"arresta": [[18,22,30]],
"sme": [6],
"ilia": [7],
"penar": [17],
"smo": [6],
"percentu": [31],
"divehi": [6],
"disactiv": [13,21],
"programma": [18,22,[2,15,17,19,21,32]],
"aragones": [6],
"lanceator": [10],
"pojavni": [17],
"optic": [15],
"simila": [17],
"creator": [2],
"retroced": [21],
"editselectfuzzy1menuitem": [8],
"sna": [6],
"snd": [6],
"maniera": [[15,31,32],[2,18,24],[17,21,22,27],[11,16,19,20,23,28,29]],
"scheda": [20],
"didier": [4,7],
"turkmen": [6],
"extrah": [[12,13,14]],
"hide": [13],
"alphabeticament": [[31,32]],
"instruction": [18,[11,33]],
"report": [21],
"reagera": [32],
"nederlandes": [31,0],
"auto": [31,[13,21,23,34],20],
"notepad": [17],
"document.xx.docx": [13],
"postt": [28],
"complit": [[18,20,22]],
"som": [6],
"sol": [18,[13,21,31],[27,32],[2,14],[17,24],[11,12,15,16,20,22,23,26,29,30]],
"posto": [31],
"son": [31,18,16,27,15,22,14,[19,28],[13,20,23,24,32],[21,30],[2,17,25],[3,11]],
"dele": [[5,31],[21,32]],
"oracl": [18,8,13],
"sot": [6],
"different": [32,31,21,15,[2,13,16],[3,14,22,27,29]],
"specialment": [24],
"posta": [22],
"administr": [2,[14,29]],
"sequit": [28,25,8,[16,31]],
"spa": [6],
"recipit": [[2,18,32]],
"modif": [8,21,7,[14,15,17,18,22,24,30,31]],
"consenti": [21,32,20],
"relevant": [16],
"bob": [31],
"bod": [6],
"comenciar": [[13,32]],
"produc": [[17,34],[11,24,31,33]],
"malay": [6],
"bon": [[4,9,32],[3,20,28,31]],
"avantagios": [28],
"bos": [6],
"cancella": [21],
"adjustar": [[22,32]],
"sqi": [6],
"aliqu": [11],
"dispacchetta": [18],
"fouri": [7],
"total": [32,21],
"illo": [32,14,31,[2,13,15],[21,27],18,[1,11],[16,17,19,25,29,30],[0,8,22],[5,12,20,23,26,28],[3,10,24,33]],
"kal": [6],
"tenta": [[13,30],[22,24]],
"immun": [31],
"kan": [6],
"involv": [32],
"dynam": [1],
"kas": [6],
"thoma": [7],
"kau": [6],
"adjung": [19],
"causant": [21],
"kat": [6],
"br1": [16],
"macro": [1],
"technic": [13,21],
"srd": [6],
"kaz": [6],
"consola": [22,18,34,33],
"br4": [20],
"gigabyt": [18],
"illa": [[19,20,31]],
"demandar": [[31,32]],
"control": [21,[2,8,10,19,30]],
"no-team": [18],
"srp": [6],
"comprehens": [19],
"pulsa": [19,[2,18,27],[5,16,17,20,21,32]],
"srt": [15],
"irlandes": [6],
"operara": [22],
"offici": [33],
"incorpor": [18],
"restaurara": [32],
"proponit": [32],
"hospit": [34,[2,13,18]],
"proclama": [4],
"coherent": [28],
"environ": [18,14],
"inscrib": [[2,19,21,24],[15,17,25,26]],
"optionsautocompleteglossarymenuitem": [8],
"includera": [31],
"exhibir": [21],
"bre": [6],
"vari": [11,31,[10,13,19,21,24,32,33]],
"ssw": [6],
"breve": [8,21,33,[11,34],5,18,[2,32],[6,7,9,13,15,17,24,25,27,29]],
"sta": [[18,28]],
"exemplo": [31,13,18,32,25,16,17,[2,22,23],[27,28,29,33,34],[12,15,20,24],[1,11,21,30],[3,5,8,14,19]],
"progressivement": [[14,30]],
"subtract": [25],
"poteva": [[4,31]],
"generalment": [[25,32],[16,24,28,31]],
"degrad": [31],
"controlara": [[16,18,29]],
"kde": [18,34],
"on-lin": [20],
"exempli": [[3,15,16,30,31]],
"accept": [31,[8,18]],
"stampa": [[1,16]],
"affront": [23],
"occurrentia": [[26,27],[16,18,24,25,29,31,32]],
"silentios": [22,18],
"sub": [14,18,31,34,2,22,[12,19,24,32],17,[29,30,33]],
"motor": [27],
"access": [[21,32]],
"languag": [18],
"distingu": [31],
"sun": [6],
"sur": [18,21,2,[20,32],13,1,[11,22],[14,27,28],[8,9,31],[17,19,24,29,30],[3,10,12,15,16,26,34],[5,23,33]],
"utilisar": [18],
"current": [21,32,[1,2,14,17,30],[3,5,10,24,27,31]],
"implic": [31,[2,9,14,28,29,32]],
"key": [21,[10,15,18]],
"porta": [18,[31,32,34]],
"trovara": [[18,27],[0,13]],
"svg": [18],
"svn": [2,34,[18,31,33]],
"tagalog": [6],
"confirm": [[21,26]],
"exequ": [18,34,[11,20,22],[1,19]],
"problemat": [11],
"fluxo": [[14,32]],
"bul": [6],
"discoperir": [[0,9,16,18,31]],
"swa": [6],
"typic": [18,31],
"editreplaceinprojectmenuitem": [8],
"swe": [6],
"but": [7,17],
"symbol": [34],
"contextu": [21,[5,8]],
"express": [13,16,25,[11,22,27,28]],
"ubication": [14],
"multilingu": [[20,34]],
"clausura": [16],
"saper": [20],
"amplement": [11],
"communit": [[2,31]],
"contexto": [32,16,13,[11,21]],
"zero": [[16,25],[13,27,28]],
"variant": [[13,25,31]],
"terminator": [25],
"gotoprevioussegmentmenuitem": [8],
"verit": [[20,32]],
"fragmento": [21,[5,16]],
"limburgan": [6],
"albanes": [[6,20]],
"includent": [32,16],
"gotopreviousnotemenuitem": [8],
"editredomenuitem": [8],
"uilayout.xml": [14],
"verif": [21],
"substitut": [21],
"khm": [6],
"quadro": [32,34,[17,21],[5,33],[27,31],22,[1,15,18,19,20,30],[11,13,24,26]],
"recorda": [[9,14,16,19,21]],
"interag": [20],
"glissa": [13],
"desd": [[7,16]],
"appello": [21],
"secar": [[5,21,32]],
"learned_word": [34],
"idea": [32],
"hiri": [6],
"susten": [9],
"heredit": [15],
"dismarcar": [13],
"kik": [6],
"habe": [18,17],
"kim": [7],
"propriement": [21],
"auto-text": [8],
"sub-plica": [34,11,19,31],
"dimensionamento": [21],
"commodit": [32],
"kin": [6],
"kir": [6],
"lakunza": [7],
"generara": [31],
"normal": [[24,27],[18,31]],
"corrupt": [16,14],
"guido": [7],
"conflicto": [2,31],
"recognit": [[3,15]],
"figura": [34,32,[11,29]],
"finalment": [18],
"non-standard": [22],
"license.txt": [14],
"restaur": [[31,34]],
"runtim": [18,14,20],
"individu": [27],
"ampl": [5],
"instantia": [18,[21,32]],
"potent": [[1,25,27,30]],
"luba-katanga": [6],
"avar": [6],
"ressourc": [15,[9,11,18]],
"realit": [2],
"specificant": [18],
"rationabilement": [11],
"credential": [2,20],
"filenam": [31,13],
"tener": [4],
"pular": [6],
"advertentia": [[7,34],[4,8,33]],
"guida": [[11,33],[9,15,18]],
"routin": [18],
"roam": [14],
"between": [13],
"collat": [21],
"amor": [4],
"generant": [22],
"nbsp": [27],
"collar": [32,[5,21]],
"interno": [[5,32]],
"gotosegmentmenuitem": [8],
"eventualment": [29],
"intact": [[4,14]],
"acceptar": [20],
"cornish": [6],
"xx_yy.tmx": [31],
"initialcreationd": [32],
"sign-in": [18],
"flag": [[24,25,33]],
"galles": [6],
"confinio": [[25,33]],
"tosto": [[18,29,31,32]],
"aperi": [21,[16,31,32],[5,18,19,27],[2,8,13,14,17,22,23,26,34]],
"helpaboutmenuitem": [8],
"salvat": [[15,22,31],[2,11,14,17,21]],
"salvar": [21,[8,13,15,30]],
"apert": [32,21,[5,15,17,20,22,27,31],[1,13,14,28,30]],
"limitar": [18,[10,27]],
"mandelbaum": [7],
"parer": [19],
"languit": [32],
"regular": [25,27,[13,16],[11,22,28,34],[18,26,33],[12,15]],
"placa": [2],
"sito": [[9,20],2,[10,14,19,33,34]],
"c\'est": [17],
"dubitabil": [27],
"suggest": [20,21],
"observ": [27],
"token": [3,34,[0,2,24,32,33]],
"etiquetta": [21,[32,33,34]],
"catalan": [6],
"retorna-carro": [25],
"expect": [31],
"elemento": [[13,15,32],[11,25,27]],
"agrada": [30],
"right-to-left": [15],
"omegat.log": [14],
"abashkin": [7],
"localment": [2],
"installara": [18],
"deponit": [32],
"recarg": [17,[22,31]],
"najlepših": [3],
"incipient": [25],
"kom": [6],
"responsa": [20,9],
"kon": [6],
"kor": [6],
"verso": [[1,9,13,18,22]],
"mechanismo": [21],
"garantit": [16],
"tab": [17,[8,21],5,32],
"taa": [10,33,[17,20,21,34]],
"tah": [6],
"non-un": [32],
"tag": [16,34,13,[21,32],15,33,19,18,22,[8,30,31],24,[5,11,28]],
"tal": [[15,31],30,[13,23]],
"versa": [[15,24,27]],
"tan": [15],
"tam": [[6,15]],
"slovenian": [32],
"tar": [14,18],
"tat": [6],
"onli": [13],
"filtrat": [11],
"potera": [[16,18,19,32]],
"reparation": [[21,31]],
"projectreloadmenuitem": [8],
"legibilit": [21],
"person": [21,[5,8,11,20]],
"safe": [27],
"salvag": [13,27],
"navig": [[18,32,34],[14,27],10],
"specificara": [18],
"tunc": [[15,19]],
"cross-platform": [18],
"sail": [20],
"delit": [16,32],
"perdita": [22,14,34,[11,24,33]],
"graham": [7],
"distinct": [[16,21,31]],
"seligit": [21,18,31,[13,32],29,[1,8,30],[19,20,22,24,28]],
"provis": [21],
"tbx": [17,21,[8,10,34]],
"winrar": [12],
"diminuit": [31],
"can": [7,[17,18]],
"cat": [11,3,[6,31,32,33]],
"duser.countri": [18],
"tcl": [30],
"provid": [[18,32],[11,17,20]],
"consulta": [25],
"tck": [30],
"feminin": [3],
"eligent": [24],
"avertit": [[14,30]],
"readm": [13,18],
"non-avid": [25,33],
"match": [32],
"tajik": [6],
"appoio": [4],
"informar": [23],
"typo3": [15],
"informat": [17],
"align.tmx": [18,22],
"argumento": [34,18,[20,31]]
};
