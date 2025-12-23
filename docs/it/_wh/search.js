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
 "chapter.menus.html",
 "chapter.panes.html",
 "chapter.project.folder.html",
 "chapter.windows.and.dialogs.html",
 "index.html"
];
wh.search_titleList = [
 "Appendici",
 "Preferenze",
 "Guide all&#39;uso...",
 "Menu",
 "Pannelli",
 "Cartella del progetto",
 "Finestre e finestre di dialogo",
 "OmegaT 6.0.2 - User Manual"
];
wh.search_wordMap= {
"popolazion": [5,[1,2,3]],
"selezionabili": [6],
"upload": [2],
"diversament": [2],
"avanz": [6,0],
"passata": [2],
"correttore": [7],
"predefinit": [0,2,4,[1,5,6]],
"evidenzia": [3,0,6,[1,4],5],
"ricercati": [0],
"info.plist": [2],
"passato": [[0,2]],
"legg": [[1,6]],
"l\'attual": [[2,6],3],
"preconfigur": [1],
"click": [2],
"fuzzi": [[1,3,4,6],2,5],
"disabilitando": [1],
"apparirà": [3],
"size": [2],
"left": [0],
"mostrar": [4,6,[0,5]],
"estratti": [0],
"latini": [0],
"individuarlo": [2],
"ricercato": [[0,6]],
"mostrat": [6,4,5],
"conteggio": [3],
"guardar": [0],
"lavora": [6,[1,2]],
"concede": [7],
"convertendo": [2],
"possano": [3],
"result": [6],
"edittagnextmissedmenuitem": [0],
"esclusion": [[2,6]],
"nell\'elaborator": [6],
"conteggia": [[1,3]],
"same": [[2,6]],
"un\'autenticazion": [2],
"modificar": [6,0,[3,4],2,1],
"after": [2],
"quiet": [2],
"connect": [2],
"ampio": [2],
"raggiungibili": [6],
"aggiunger": [0,2,[4,5],[1,6],3],
"gestor": [3,2,[5,6]],
"the": [2,6,0,[1,5]],
"quadrati": [1],
"riepilogo": [2],
"dell\'espression": [0],
"lavoro": [[0,2,6],4],
"preparar": [0],
"chiamat": [0],
"projectimportmenuitem": [0],
"frances": [2,1,6],
"riferisc": [2],
"imag": [0],
"lavori": [0,2],
"monolingu": [[0,6]],
"prescelt": [2],
"l\'indirizzo": [2],
"l\'associazion": [0],
"edizion": [2],
"disponibilità": [4],
"priorità": [[1,3],2],
"distribuisc": [2],
"applica": [[1,6],[3,5]],
"sviluppati": [6],
"manuale": [7],
"l\'ingles": [2],
"nell\'ultima": [[3,4]],
"omegat.project.lock": [2],
"adegu": [2],
"moodlephp": [2],
"accessibil": [5],
"l\'aggiunta": [5],
"currsegment.getsrctext": [6],
"sotto": [0,[2,4],[1,5],[3,7]],
"uncheck": [0,1],
"comprender": [2],
"export": [5],
"gestir": [2,1,[0,6]],
"che": [0,2,6,1,5,[3,4],7],
"practic": [6],
"notificar": [1],
"chi": [[0,1]],
"reduc": [2],
"check": [[0,1,6],2],
"selezionandolo": [6],
"manuali": [[0,5,6]],
"freddo": [3],
"incorporazion": [[0,3]],
"gotonotespanelmenuitem": [0],
"distribuito": [2,[0,6,7]],
"rimanenti": [6],
"l\'utente": [7],
"conservar": [6],
"motivo": [0,3],
"minimizza": [[2,4]],
"disco": [2,6],
"posizionandoli": [[0,6]],
"orientati": [6],
"initializza": [2],
"varia": [4],
"termina": [0],
"offr": [2,[3,4,6]],
"esist": [[2,3,6]],
"produttività": [0],
"termini": [6,4,1,[0,3],5,7],
"multiplo": [[2,6]],
"cjk": [6,0],
"multiple": [7],
"multipli": [0],
"richiamata": [[0,6]],
"better": [6],
"tutt": [6,2,[0,1,3],5,4],
"l\'anno": [2],
"bilingui": [2,5],
"contrario": [[0,2],6],
"empti": [[2,3]],
"valida": [0],
"installazion": [2,0],
"validi": [2],
"dall\'interfaccia": [2,3],
"un\'ulterior": [0,6],
"rilevanti": [1],
"valido": [6,[2,5]],
"conteggi": [3],
"propon": [[4,6]],
"tms": [5,7],
"blocca": [2,3,1],
"tmx": [2,6,5,1,4],
"repo_for_all_omegat_team_project": [2],
"cli": [2],
"application_startup": [6],
"avanti": [2,3,4,[0,1,5,6,7]],
"eventtyp": [6],
"dell\'esempio": [0],
"dettagli": [2],
"clonar": [2],
"fr-ca": [1],
"mainmenushortcuts.properti": [0],
"gradualment": [[2,5]],
"sganciarlo": [4],
"identificatori": [[0,7],3],
"vedano": [6],
"un\'analisi": [[0,6]],
"prevedono": [2],
"sull\'attual": [6],
"cmd": [3,0],
"convertir": [2],
"complessivi": [0],
"provenient": [[0,2,5]],
"propri": [4,[1,2,6],0],
"gotohistorybackmenuitem": [0],
"luogo": [4],
"parametro": [2,[1,6]],
"save": [[2,6]],
"sarà": [6,0,2,3,1,5],
"ricevut": [4],
"allineamento": [6,0],
"sperimentano": [2],
"v1.0": [2],
"conversioni": [2],
"parametri": [2,0,6,1,5],
"allineamenti": [0],
"ciò": [2,[3,6],[0,1]],
"complessivo": [0],
"proporzional": [1],
"top": [4],
"resteranno": [[5,6]],
"too": [2],
"have": [2,0],
"powerpc": [2],
"nell\'ambito": [2],
"propost": [1],
"avail": [2],
"opzion": [0,1,2,6,3],
"schermo": [0],
"esporta": [0,3,1],
"question": [0],
"distribuire": [7],
"nominata": [1],
"elencati": [[0,2,6]],
"editselectsourcemenuitem": [0],
"sfondo": [5,[3,4]],
"l\'autor": [3],
"contemporanea": [[2,3]],
"qual": [[0,6],[2,3,4]],
"anch": [2,6,0,1,5,3,4],
"com": [0],
"col": [[2,6],3,4,0,5,1],
"instal": [2,[0,6]],
"sacco": [0],
"con": [0,2,6,1,3,5,4],
"minor": [7],
"elenco": [2,1,[0,6],[4,5]],
"propagazion": [[2,6]],
"cot": [0],
"remot": [2,[3,5,6]],
"riaprir": [2],
"lett": [2],
"avrà": [0,6,5],
"elenca": [4,0],
"possibilment": [2],
"ordin": [1,6,0],
"pipe": [0],
"dell\'uscita": [1],
"coerenza": [3,2],
"elencano": [0],
"tra": [0,[2,6],4,3,1,5],
"visualizzar": [1,[2,3,6],[0,4,5]],
"contator": [6],
"tre": [0,5,[1,2,3],[4,6]],
"piattaforma": [0,2],
"tri": [6],
"changeid": [1],
"translat": [2,0,1,6,3],
"segmentazioni": [0],
"assegnerà": [2],
"successiva": [[0,3],2,[4,6]],
"segmentazione": [7],
"sposta": [3,6,[0,1,4]],
"distinzion": [0],
"université": [1],
"were": [2],
"mantenimento": [0],
"successivo": [3,0,6,[2,4,5]],
"successivi": [0,6],
"debolment": [6],
"chiudersi": [2,1],
"sull\'icona": [3],
"cqt": [0],
"correttament": [2,3,1,[0,4,6]],
"l\'origin": [1,6],
"docs_devel": [2],
"tsv": [0],
"intendono": [1],
"archivio": [5],
"gnome": [1],
"probabil": [4],
"totali": [3],
"commento": [0,[2,3,4]],
"contenitor": [0],
"nell\'intervallo": [0],
"blocco": [4,[0,6],3,7],
"corretta": [[0,6]],
"riporterà": [6],
"corretto": [2,0,6],
"doctor": [0],
"crescent": [0],
"dell\'affidabilità": [2],
"sincronizz": [2],
"corretti": [[2,6]],
"avanza": [3,1],
"commenti": [4,0,6,7],
"quei": [3,2],
"quel": [[0,2],6,1,[4,5]],
"appdata": [0],
"tue": [4],
"senza": [2,[0,6],1,[4,5],3],
"csv": [0,2],
"stata": [2,[1,3],[0,5,6]],
"apparir": [2],
"unificatori": [0],
"analogament": [4],
"enhanc": [2],
"tuo": [6],
"stato": [4,2,[0,3],6,5,1,7],
"eventual": [2],
"seguir": [2,[0,6]],
"seguit": [[0,1]],
"caractèr": [2],
"state": [[2,3],[0,1,6]],
"press": [0],
"dock": [2],
"stati": [2,[1,4,6],[0,5]],
"segmentazion": [6,0,1,3,2,5],
"chiavetta": [2],
"rispecchiano": [[2,5]],
"night": [2],
"each": [2],
"importanti": [0,2],
"cui": [0,2,3,6,1,4],
"sull\'ultimo": [6],
"utilizzando": [2,4,[0,5],[1,6]],
"filenameon": [1,0],
"cut": [0],
"ctrl": [0,3],
"editorinsertlinebreak": [0],
"jumptoentryineditor": [0],
"document": [2,0],
"distribu": [6,0],
"two": [[2,6]],
"dollaro": [0],
"mappar": [2],
"caric": [5],
"page_up": [0],
"attività": [2],
"documentazion": [[0,2],6],
"glossaryroot": [0],
"scenario": [2],
"verrebbero": [5],
"resourc": [2,0],
"preso": [6],
"moodl": [0],
"richieste": [7],
"presi": [1],
"prese": [1,[2,3]],
"team": [2],
"xx_yy": [0],
"presa": [5],
"docx": [[2,6],[0,3]],
"project_stats_match_per_file.txt": [[3,5]],
"txt": [2,0,4],
"prestar": [2],
"aggiornar": [2,6],
"possibilità": [[2,6]],
"l\'editor": [6,0,[1,3]],
"ordinar": [0,6],
"richiesti": [0],
"caratteri": [0,6,3,[1,4],7,2],
"definit": [2,[0,4,6],[3,5]],
"projectmedopenmenuitem": [0],
"anno": [0],
"definir": [0,1,6,4,2],
"attivi": [[0,2]],
"lib": [0],
"ridimensionati": [4],
"source": [7],
"attiva": [0,3,1],
"valgono": [2],
"configurata": [[0,5]],
"dall\'ultimo": [0],
"riutilizzati": [0],
"accetta": [2],
"viewdisplaymodificationinfoselectedradiobuttonmenuitem": [0],
"index.html": [0,2],
"configurati": [[1,3]],
"prefisso": [5,1,2],
"configurato": [[2,4]],
"umana": [1],
"develop": [2],
"ultimato": [0],
"diffrevers": [1],
"attivo": [3,[0,4],6,1,5],
"fornitor": [[1,4]],
"nascosto": [1],
"indefinito": [0],
"l\'inizio": [0],
"qualcosa": [[1,2]],
"disposizione": [7],
"giappones": [2,[1,6],0],
"perduta": [2],
"comandi": [1,6,0,2,3,4,7],
"ospitati": [1],
"delimita": [6],
"rivolti": [4],
"principio": [5],
"project.gettranslationinfo": [6],
"comando": [6,2,0,1,3],
"spostamento": [[0,3,6]],
"riconvertir": [2],
"czt": [0],
"disposizioni": [1],
"avviando": [0],
"paio": [[2,6]],
"funzion": [0,3,1,2,6,4],
"diretto": [0,2],
"installazioni": [0],
"recupero": [2],
"start": [2],
"mymemori": [1],
"regex101": [0],
"equal": [0,2],
"watson": [1],
"esatt": [6,3,[2,5]],
"nascosta": [5],
"produrranno": [6],
"fung": [5],
"recupera": [3,[0,1,2]],
"short": [6],
"libero": [[0,2,3]],
"nascosti": [[5,6]],
"adeguata": [2],
"prender": [[2,3,6]],
"selezionarn": [4],
"aspetta": [1],
"renderla": [5],
"three": [2],
"elenc": [[1,2]],
"viewmarkglossarymatchescheckboxmenuitem": [0],
"concesso": [2],
"sottoinsiem": [[0,2]],
"renderlo": [0],
"riconoscimento": [1],
"enter": [0,6],
"applic": [1,2,[0,4]],
"bidi": [0,3,6],
"verrebb": [2],
"projectteamnewmenuitem": [0],
"gotoprevxenforcedmenuitem": [0],
"scorciatoi": [0,3,6,4],
"preced": [0,2],
"directorate-gener": [3],
"applicazion": [2,1],
"sessioni": [2],
"autocompletertablelast": [0],
"memori": [2,6,5,3,4,1],
"produrr": [1],
"creativo": [0],
"incolla": [4],
"aspetto": [1,7],
"selezionato": [3,6,4,0],
"log": [0],
"aggirar": [3],
"eliminando": [6],
"interessato": [3],
"correggerà": [2],
"interessati": [0],
"openjdk": [1],
"永住権": [[1,6]],
"computer": [7],
"istruzioni": [2,0,6],
"basandosi": [0],
"interagir": [6],
"comporta": [2,[0,6]],
"globalment": [6],
"toolscheckissuesmenuitem": [0],
"clic": [6,4,3,[0,1,2],5],
"completament": [2],
"autocompletertablepageup": [0],
"farebb": [2],
"selezionata": [3,4,[1,2],[0,6]],
"www.deepl.com": [1],
"originariament": [2],
"selezionati": [6,3,[0,2]],
"config-fil": [2],
"quick": [0],
"nell\'interfaccia": [0],
"dag": [2],
"dai": [6,2,1,[0,3,4]],
"convertirà": [1],
"premendo": [6,4],
"dal": [6,2,0,1,[3,5],4],
"day": [0],
"lre": [0,3],
"inglese-giappones": [2],
"cifrato": [0],
"system-user-nam": [0],
"lrm": [0,3],
"ottimal": [3],
"format": [0,[3,5]],
"formar": [0],
"particular": [2],
"done": [2],
"trascinar": [[2,4,5]],
"console.println": [6],
"rainbow": [2],
"autocompleterlistdown": [0],
"larghezza": [6],
"cambiar": [1,[2,6],[4,5],0],
"ipertestuali": [4],
"concentrarsi": [2],
"corso": [[2,6],0],
"part": [0,6,[2,3],4,1],
"uscir": [2],
"principal": [6,[1,4],2],
"trascinarlo": [4],
"contrassegno": [[0,3]],
"browser": [1,[3,4]],
"activefilenam": [6],
"fuzzy": [7],
"project_files_show_on_load": [0],
"dopo": [0,2,6,1,4,3],
"nello": [[0,1,6]],
"project_save.tmx.aaaammgghhmm.bak": [2],
"allineator": [3],
"intorno": [1],
"generarlo": [5],
"contrassegni": [3],
"tema": [1,6],
"saranno": [2,3,1,0,6,5,4],
"build": [2],
"nella": [0,2,6,4,3,1,5],
"facilment": [[2,5]],
"temi": [1],
"teme": [2],
"possibil": [2,0,6,1,4,3,5],
"ident": [6],
"entries.s": [6],
"deg": [6,[0,3],[1,2]],
"dei": [6,0,2,1,3,4,5,7],
"del": [0,2,6,3,1,5,4,7],
"gotonextuntranslatedmenuitem": [0],
"rispecchia": [[3,6]],
"targetlocal": [0],
"altra": [[0,2],4],
"path": [[0,2]],
"ritien": [2],
"bind": [6],
"abilitarl": [6],
"abbinarlo": [0],
"abbrevi": [0],
"relativi": [[1,4,5,6]],
"relativo": [1,2,[0,3]],
"altri": [0,2,[4,6],[1,7],5,3],
"rilasciati": [4],
"impostazioni": [6,2,0,3,[1,4,5]],
"altro": [1,[0,3],[2,4,5]],
"ricerche": [7],
"helpcontentsmenuitem": [0],
"resnam": [0],
"omegat-org": [2],
"avanzato": [0],
"evidenziar": [6,[4,5]],
"relativa": [[0,6]],
"remote-project": [2],
"initialcreationid": [1],
"ignore.txt": [5],
"projectaccessdictionarymenuitem": [0],
"eseguit": [3],
"acceder": [6,[0,1],[2,3,4,5]],
"all\'utent": [[0,1,3]],
"seguito": [0,2,5,1,6],
"terz": [2],
"ricaricar": [6,[0,3]],
"crearsi": [2],
"dove": [0,[2,4],[5,6],1],
"marcatori": [0,6,[1,2]],
"davanti": [0],
"eseguir": [2,6,1,[0,5],3],
"un\'inclusion": [0],
"files_order.txt": [5],
"projectrestartmenuitem": [0],
"editorskipnexttoken": [0],
"trans-unit": [0],
"right": [0],
"un\'espression": [0,6,1],
"qigong": [0],
"seguiti": [0],
"operazioni": [2,6,1],
"seguita": [0],
"dir": [2],
"tenterà": [6],
"down": [0],
"l\'apertura": [[2,6]],
"impartir": [2],
"fisico": [2],
"provengono": [5,2],
"riempito": [5],
"esempi": [0,2,7,4],
"considerati": [0,1],
"elabor": [0],
"istanz": [0,[2,3]],
"alfabeticament": [[4,6]],
"grossi": [2],
"considerata": [0,6,4],
"viewfilelistmenuitem": [0],
"lasciar": [0,[1,3,5]],
"adottata": [0],
"journey": [0],
"test": [2],
"originali": [3,4],
"allinear": [6,3],
"omegat": [2,0,6,1,3,5,4,7],
"deepl": [1],
"tesi": [1],
"all\'interno": [0,2,4,6,3,1,5],
"questi": [2,0,4,1,5],
"assistita": [2,7],
"configurazioni": [1],
"cancellato": [3],
"cancellati": [0],
"final": [0,[2,5]],
"questo": [6,0,2,3,4,5,1,7],
"racchiuder": [0],
"configurazione": [7],
"numerati": [4,0],
"sostituiranno": [6],
"occasion": [2],
"virtual": [6,2],
"messaggio": [[2,4]],
"rimuov": [6,[0,5]],
"ignora": [0,[1,5]],
"console-align": [[2,6]],
"back": [0],
"questa": [[0,1],6,2,5,3,4],
"disegni": [0],
"projectopenrecentmenuitem": [0],
"dipend": [[0,1,3]],
"dell\'intervallo": [0],
"load": [6],
"attivata": [1,0,[3,4,6]],
"attivato": [6,[2,3]],
"inser": [[1,5],2],
"leggibil": [6],
"aggiunti": [[2,5],[3,6]],
"all\'altro": [[2,3]],
"custom": [0],
"lettera": [0,3,1],
"issue_provider_sample.groovi": [6],
"una": [0,2,6,1,3,5,4,7],
"aggiunto": [[2,6]],
"terminano": [0],
"considerato": [0],
"grand": [6],
"appena": [[2,3,4,5,6]],
"uno": [0,6,2,1,3,4,5],
"editoverwritemachinetranslationmenuitem": [0],
"relat": [[0,1,2,4,5,6]],
"aggiunta": [0,[2,3,5,6]],
"richiamando": [6],
"console-stat": [2],
"trattati": [0],
"sfoglia": [6],
"ingreek": [0],
"lunch": [0],
"disattiva": [0],
"crearlo": [2],
"f12": [6],
"eccetto": [0],
"convert": [0,[2,3,5,6]],
"ricalcolato": [6],
"disattivo": [3],
"division": [0],
"projectexitmenuitem": [0],
"ricercar": [6,[0,1],[3,4]],
"contenut": [6,[2,4,5]],
"manterrà": [4],
"text": [[2,6],1],
"editregisteruntranslatedmenuitem": [0],
"init": [2],
"all\'indirizzo": [2],
"punto": [0,2,1,5,[4,6]],
"misspel": [0],
"contattar": [4],
"istantaneament": [5,2],
"manag": [2],
"manifest.mf": [2],
"disponibil": [6,2,1,0,3],
"maco": [0,2,3,4,1],
"field": [6],
"attraverso": [2,6],
"frattempo": [2],
"doc": [6,0],
"nell\'allineamento": [6],
"doe": [2,6],
"senso": [0,[1,6]],
"output-fil": [2],
"aiutare": [7],
"particolari": [0],
"successo": [2],
"server": [2,1,5,4],
"calcolato": [6],
"un\'altra": [[0,2]],
"paramet": [0,2],
"siano": [2,1,[0,3],[4,5,6]],
"piacimento": [5],
"stamp": [5],
"run-on": [0],
"incluso": [2,[1,5],[0,3,6]],
"sottostant": [0,2],
"calcolati": [1],
"bisogna": [2],
"leggi": [0],
"dividerà": [6,0],
"mai": [0,[1,3,5]],
"mantieni": [6,0],
"affatto": [0],
"inizial": [6,2,[0,1]],
"man": [[0,2]],
"map": [2,5],
"punti": [0,[4,6]],
"may": [2],
"url": [2,1,5,0],
"megabyt": [2],
"uppercasemenuitem": [0],
"calcolata": [[1,4]],
"viewmarkuntranslatedsegmentscheckboxmenuitem": [0],
"contestuali": [4],
"direzional": [3,0],
"desidera": [6,0],
"needs-review-transl": [0],
"bisogno": [[3,6]],
"tagwip": [6],
"usa": [0,1,6,[2,3],4,5],
"lungo": [0],
"usb": [2],
"use": [2,6,1],
"usd": [6],
"usi": [0],
"reperir": [2],
"dell\'interfaccia": [2,1],
"scarica": [2,[0,3,6]],
"uso": [[2,3],6,[0,7],[1,4]],
"lavorando": [6],
"omegat.jar": [2,0],
"omegat.app": [2,0],
"conveni": [6],
"usr": [[0,1,2]],
"ottenerlo": [2],
"logo": [0],
"combina": [2],
"assicurarsi": [2,[0,4]],
"creazion": [3,[5,6],1,0],
"progressi": [[0,2]],
"iniziar": [[0,2,6]],
"lista": [7,2],
"utf": [0,5],
"domand": [0],
"ogni": [0,[2,5,6],3,1,4],
"ferma": [1],
"deposito": [2,[3,5,6],[1,4]],
"includer": [[0,2],[4,6]],
"veder": [2,3,6,0,1,[4,5]],
"separator": [0],
"simbolici": [2],
"feed": [0],
"inclusi": [2,[0,6],[4,5]],
"lunga": [0,1],
"descritt": [[0,2],6],
"apposto": [2],
"aggiungendo": [0,6],
"esci": [[0,3]],
"inclusa": [0],
"dsl": [5],
"staccato": [1],
"tabulazion": [0],
"definibili": [6],
"strumenti": [2,1,6,[0,3,5],7],
"vecchio": [2,1],
"vecchia": [2],
"med": [3,0],
"strumento": [[2,3,6,7]],
"salvataggio": [1,2,3,[5,7]],
"dtd": [[0,2]],
"abilitato": [4,1],
"nuov": [0,3,[5,6]],
"regolar": [0,6,2,[1,4,5]],
"tentar": [2],
"make": [6],
"fanno": [[0,2]],
"abilitati": [[1,3]],
"sovrascritta": [2,3],
"comprimi": [0],
"projectcompilemenuitem": [0],
"classnam": [2],
"console-transl": [2],
"sovrascritti": [2,[0,1,6]],
"spaziatura": [1],
"recenti": [[2,3]],
"entrambi": [[2,6],1],
"optionsautocompletehistorycompletionmenuitem": [0],
"due": [2,0,6,[1,3],[4,5]],
"gotonextuniquemenuitem": [0],
"messaggi": [4,0],
"grafi": [0],
"ricevono": [1],
"wordart": [0],
"istruito": [5],
"attiv": [0,[1,4]],
"inform": [2],
"ignorerà": [5],
"about": [0],
"commit": [2],
"targetlocalelcid": [0],
"project_stats_match.txt": [[3,5]],
"freccia": [6,4,0],
"tab-separ": [0],
"provvist": [0],
"coordin": [2],
"revisor": [[2,5]],
"miglioramenti": [[0,3]],
"consiglia": [6],
"azzerar": [1],
"esigenz": [1,4],
"esattament": [0,6,2],
"figurarsi": [0],
"ripeter": [2],
"glifi": [3],
"punteggio": [6],
"libreoffic": [0],
"autocompleterclos": [0],
"qualiti": [3],
"opzioni": [6,2,0,3,1,[4,7]],
"eliminato": [1],
"pacchetto": [2,3],
"convertitori": [2],
"entramb": [0,[1,2,6]],
"protetti": [1],
"eliminati": [2,0],
"long": [0],
"devono": [0,2,6,1,[4,5]],
"into": [6],
"mio": [2],
"rifletter": [3],
"defin": [0,1,6,5],
"supporto": [2,0,[3,7]],
"sottolineatura": [0],
"expression": [0],
"mano": [6,0,[2,5]],
"appartengono": [[0,1]],
"l\'analisi": [4],
"viewdisplaysegmentsourcecheckboxmenuitem": [0],
"editregisteremptymenuitem": [0],
"appaiono": [1],
"aggiungi": [6,[0,3],[1,5],4],
"stats-output-fil": [2],
"progettati": [2],
"open": [6,0,[1,2]],
"pacchetti": [2],
"project": [2,6,5,4],
"predefinito": [1,3,[0,2,6],5,4],
"取得": [[1,6]],
"xmx1024m": [2],
"visualizzerà": [1],
"verificarn": [0],
"corredato": [2],
"predefinita": [0,6,1,4,[2,3],5,7],
"cella": [6],
"penalty-xxx": [[2,5]],
"gotonextsegmentmenuitem": [0],
"copiarlo": [2],
"supporti": [6],
"finestra": [6,3,1,4,0,2,5,7],
"calcolator": [6],
"finestre": [7],
"supporta": [2,6,[0,5]],
"difficili": [2],
"dropbox": [2],
"esistent": [2,4,[0,3,6]],
"abort": [2],
"internet": [1],
"descrizion": [3,[0,1,6],2],
"valori": [0,[2,3,6]],
"comma-separ": [0],
"saltar": [5],
"affidabile": [7],
"nell\'editor": [6,[0,1],3],
"semplifica": [[1,2]],
"affidabili": [5],
"linguaggi": [6],
"printf": [0,1],
"verificato": [2],
"influenzati": [3],
"segnalerà": [2],
"uscita": [[0,2]],
"controllar": [0],
"basa": [0,[2,6]],
"assegnar": [[0,3,6],[1,5]],
"inserirla": [4,[2,3]],
"percorso": [2,0,1,[3,4,6]],
"writabl": [2],
"registro": [0,3,5],
"informatico": [2],
"layout": [0],
"andranno": [0],
"registri": [0],
"sincronizzato": [2],
"teddesco": [6],
"registra": [[0,1,2,3]],
"sincronizzati": [1],
"bash": [[0,2]],
"tmroot": [0],
"manualment": [[2,6],[0,3,5]],
"base": [0,4,2,[1,6],[3,7]],
"registr": [0],
"riserva": [[0,2]],
"collegata": [5],
"distinguern": [3],
"nazionalità": [1],
"collegato": [[3,5]],
"collegati": [[2,5]],
"allin": [1],
"automatica": [[1,3],4,[0,5,6],2,7],
"大学": [1],
"facendo": [6,3,[1,4,5]],
"automatich": [[1,4],2],
"automatici": [[0,3]],
"verificano": [4],
"richiamato": [6],
"insertcharslr": [0],
"vai": [0,4,3,[2,5,7],1],
"automatico": [0,1,6,[2,3,4],7,5],
"notifich": [4],
"indica": [4,[0,1,2]],
"inserit": [5,[1,2,4]],
"inserir": [0,2,6,[1,5],3,4],
"indice": [7],
"work": [2,0],
"esplicitament": [2],
"notifica": [4],
"stile": [2],
"bidirezional": [0],
"stili": [6],
"terminato": [[1,6]],
"risultano": [4],
"loro": [0,6,2,[1,4],3],
"attribuzion": [2],
"sincronizzano": [2],
"risultant": [2,[3,6]],
"permett": [3,0,6,2,1],
"indici": [0],
"quotidiano": [0],
"variar": [2],
"word": [6],
"lingue": [1],
"sottolinear": [4],
"lingua": [2,0,1,6,5,4],
"utent": [2,0,1,4],
"essa": [0,2],
"contenga": [[2,5]],
"elettronica": [0],
"duemila": [1],
"include": [7],
"includi": [[1,2,5]],
"accettato": [6],
"contener": [5,[2,4],[0,1]],
"vcs": [2],
"sezioni": [4],
"lingvo": [5],
"developer.ibm.com": [2],
"includa": [2],
"accettati": [[2,6]],
"automatizzar": [[2,6]],
"dimensioni": [6],
"accettata": [2],
"analizzar": [[4,6]],
"inserendo": [3],
"forzati": [5],
"specifico": [2,0,1],
"ecc": [1,[0,4,5]],
"formular": [0],
"specifich": [6,[1,2,5]],
"specifici": [[0,2],[1,5,6]],
"alleg": [6],
"specifica": [0,2,[1,3],[4,5,6]],
"anch\'essa": [0],
"appar": [3],
"html": [0,2,1],
"spell": [[0,6]],
"variabil": [0],
"disabilit": [6],
"esso": [0,[1,3,5]],
"dell\'intero": [[3,5]],
"leggi.mi": [0],
"insertcharsrl": [0],
"essi": [[0,6],[1,2]],
"revision": [6,[0,2]],
"danneggerebb": [1],
"nome_fil": [4],
"sulla": [2,4,[0,1,6],3],
"qualità": [3,6],
"l\'attributo": [0],
"alcun": [0,[2,3],6],
"reattiva": [6],
"avanzati": [0],
"minimo": [5,[0,1,7]],
"risultati": [6,0,[3,4],2],
"sullo": [[0,2]],
"l\'elemento": [[0,3]],
"www.ibm.com": [1],
"avanzata": [0],
"agganciati": [4],
"risultato": [6,2,0,1,7],
"opposta": [0],
"minima": [1],
"risultass": [2],
"controllo": [0,[2,3],[1,6]],
"radunar": [2],
"regolarment": [2,5],
"toolsalignfilesmenuitem": [0],
"conseguentement": [0],
"controlla": [[0,1],3,2],
"agio": [2],
"gravi": [0],
"controlli": [3],
"migliori": [6,1],
"instead": [2],
"command": [0,[2,3]],
"ricordi": [5,2],
"project-specif": [5],
"convalidar": [3,1],
"miglior": [[1,4],[0,2,5,6]],
"tipici": [2],
"quindi": [[0,2,4],[1,6]],
"documento.xx.docx": [0],
"sottocartell": [2,[0,5,6],3],
"tag-fre": [6],
"considerazion": [1,[3,6],[2,5]],
"volut": [0],
"onecloud": [2],
"incollato": [3],
"viewmarkbidicheckboxmenuitem": [0],
"incollar": [3],
"notar": [6],
"preferir": [2],
"dell\'oggetto": [0],
"nasconder": [[4,6]],
"contenent": [2,3,0],
"punteggi": [5],
"agli": [7],
"informazioni": [4,[0,3],2,1,6,5],
"concorderà": [1],
"via": [6,5],
"l\'alternativa": [3],
"ignorino": [0],
"fileshortpath": [[0,1]],
"variabili": [1,0,[6,7]],
"日本語": [6],
"combinando": [6],
"esaustiva": [2],
"sinistro": [6,4],
"sottolinea": [3],
"verificar": [4],
"sinistra": [0,3,[4,6]],
"notazion": [1],
"version": [2,0,5,3],
"volta": [2,3,[0,5],6,1,4],
"folder": [4,[5,6],7],
"espression": [0,1],
"avanzamento": [0],
"chiederà": [1],
"esportata": [2],
"handl": [2],
"detail": [2],
"vista": [4,1,0,[3,7],6,5],
"verificherà": [0],
"immess": [6],
"voluta": [6],
"projecteditmenuitem": [0],
"temporaneament": [4],
"collaborazion": [2,3,5,0,6,[1,4]],
"tradurlo": [6],
"configurar": [1,2],
"considerandoli": [2],
"new_word": [6],
"run\'n\'gun": [0],
"riconoscer": [6],
"attivazion": [0],
"modificato": [[1,2,4,6],[0,5]],
"nashorn": [6],
"colleghi": [[2,4]],
"machin": [6,2],
"compariranno": [0],
"unsung": [0],
"refusi": [6],
"forza": [0],
"puntator": [4],
"last_entry.properti": [5],
"apertura": [0],
"visualizzazion": [6,1,4],
"dovuto": [2],
"all\'url": [2],
"testi": [6,0],
"all\'ultimo": [[0,3,4]],
"bordo": [4],
"dovrà": [[0,1,2,6]],
"autocompleternextview": [0],
"testo": [0,6,3,1,4,2,5,7],
"specif": [6],
"provoca": [2],
"l\'applicazion": [2,0,[1,6]],
"vantaggio": [6],
"all\'ultima": [0],
"dsun.java2d.noddraw": [2],
"categorico": [5],
"dovut": [2],
"cambiarn": [0],
"scorciatoia": [0,[3,6],4],
"riavvia": [3,[0,1,5]],
"sezion": [2,0,6,1,3],
"liberament": [[0,2,4]],
"need": [2],
"all\'uso": [[2,5],[0,3,6],[1,4,7]],
"riconosciuti": [0,6],
"scorciatoie": [7],
"riconosciuto": [[0,3,5]],
"editorfirstseg": [0],
"x0b": [2],
"richiam": [0],
"contrassegnar": [1,3],
"un\'istanza": [0],
"canada": [[1,2]],
"altern": [0,1,[3,4],2],
"http": [2,1],
"decimal": [0],
"riconosciuta": [[1,2,4,5]],
"strano": [0],
"nazion": [2],
"carrello": [0],
"significa": [0,[1,2,6]],
"lisenc": [0],
"rimuovendo": [2],
"linea": [2,[0,3]],
"dall\'utent": [0,[4,6]],
"vuot": [0,4],
"softwar": [2],
"lieta": [0],
"all\'union": [6],
"projectsinglecompilemenuitem": [0],
"end": [0],
"supportano": [2,1],
"lisens": [0],
"l\'error": [3],
"scomparir": [2],
"prescelti": [2],
"particip": [2],
"invierà": [2],
"vuol": [2,[1,6],0],
"env": [0],
"prescelta": [2],
"aggiorna": [5],
"fornit": [[2,6]],
"comuni": [2,0,[4,6]],
"special": [0,[2,6]],
"okapi": [2],
"fornir": [2,[0,4,6]],
"stella": [0],
"page_down": [0],
"utilità": [6],
"nell\'esempio": [0],
"prescelto": [2,3],
"copyright": [3],
"contenenti": [[0,5],6],
"trattino": [0],
"marchi": [4],
"project_nam": [6],
"system-os-nam": [0],
"insertcharspdf": [0],
"modificano": [6,2],
"traduzion": [2,4,[3,6],0,1,5],
"turni": [2],
"specifi": [6],
"heapwis": [6],
"provengano": [5],
"riaperto": [2],
"lingu": [6,[1,2],5,0,3],
"segmentation.srx": [[0,5,6]],
"selettor": [2],
"tar.bz2": [5],
"invio": [6,3,1],
"bundle.properti": [2],
"contributors.txt": [0],
"invia": [2,[0,3]],
"driver": [1],
"pertinent": [0],
"coreani": [1],
"www.regular-expressions.info": [0],
"sourcelang": [0],
"cassa": [6],
"nell": [6,2,[0,3],1,4,5],
"numerich": [0],
"parola": [0,6,[3,5],[1,4]],
"cell": [6,0],
"optionsdictionaryfuzzymatchingcheckboxmenuitem": [0],
"esserci": [4],
"valid": [6],
"assur": [3],
"insiem": [6,0,2],
"diffondessero": [2],
"interfac": [2],
"velocizzar": [2],
"assum": [0],
"era": [1],
"eccezion": [1,[0,2]],
"emergenza": [2],
"share": [2],
"utilizzerà": [1,[2,6]],
"sourcelanguag": [1],
"riga": [0,2,6,1,4,3,5],
"fornendo": [7],
"chiavi": [6,1],
"gzip": [5],
"helpupdatecheckmenuitem": [0],
"righ": [6,0,4],
"esc": [[3,4],2],
"distribuzion": [6,2,0],
"exampl": [6],
"l\'etichetta": [1],
"nostemscor": [1],
"ndt": [3],
"ess": [0,4,1],
"finement": [0],
"project_chang": [6],
"traducibili": [[0,6]],
"ufficial": [0],
"forniscono": [[0,1]],
"scaricamento": [[1,2]],
"osserverà": [2],
"console-createpseudotranslatetmx": [2],
"proposto": [0],
"neg": [3,0],
"nei": [6,0,[1,2],[3,4],5],
"nel": [6,[2,3],0,1,4,5,7],
"suffisso": [2],
"fuzzyflag": [1],
"dottorato": [1],
"modificarl": [[0,6]],
"modificarn": [6],
"modificate": [7],
"escap": [0],
"modificati": [3,[1,2],0],
"rappresenta": [0,1,2],
"ecco": [1],
"poisson": [6],
"runway": [0],
"bianchi": [0],
"visualizz": [6,[1,4],[0,3]],
"choos": [2],
"allineerà": [2],
"modificata": [6],
"ll-cc.tmx": [2],
"cascata": [1],
"canali": [0],
"invec": [[0,1]],
"verranno": [0,[1,2,5],6,3],
"avvisi": [2],
"premer": [6,3,[1,4]],
"forma": [0,[1,3,6]],
"grunt": [0],
"scarterà": [6],
"all\'attivazion": [1],
"probabilment": [2],
"sull\'avanzamento": [4],
"meglio": [4],
"render": [0,2],
"medio": [6],
"potenzial": [0],
"magento": [2],
"crediti": [3],
"chiaro": [3],
"ll.tmx": [2],
"preferisce": [7],
"decider": [[0,5]],
"proposta": [2],
"particolar": [4],
"conservati": [2],
"rilascio": [[3,4]],
"correttor": [[1,5],[0,3,6]],
"memorizz": [0,[2,4,6],[1,5]],
"sostituirla": [2],
"ll_cc.tmx": [2],
"u00a": [6],
"vincolo": [6],
"escluda": [2],
"shift": [0,3],
"cert": [2],
"fondamentali": [0],
"autenticato": [1],
"java": [2,0,1,6],
"pubblicazion": [2],
"xmxsize": [2],
"project_save.tmx": [2,5,6,3],
"dictionari": [5,[2,6],[1,4]],
"azzera": [6],
"mantenuta": [6],
"studiare": [7],
"rimuover": [6,5,2,1,0],
"comprensibil": [6],
"quattro": [0,2,[3,6]],
"applicazioni": [[0,2],6],
"powershel": [[0,2]],
"eye": [0],
"estrai": [6],
"dictionary": [7],
"contrazioni": [0],
"modificarlo": [0,[4,5]],
"clonato": [2],
"neutra": [0],
"necessarili": [2],
"dell\'area": [4],
"numero": [6,0,4,3,[1,2],5],
"all\'opzion": [3],
"appl": [0],
"correggerla": [2],
"numeri": [1,[0,4],6],
"l\'avvio": [2,6],
"default": [2],
"alterna": [[0,3,6]],
"dodici": [0,3],
"mantenuto": [0],
"sudo": [2],
"qualsiasi": [0,2,[5,6],3,4,7],
"timestamp": [0],
"perso": [2],
"attributo": [0],
"sottotitoli": [2],
"projectaccessrootmenuitem": [0],
"mappato": [2],
"digitata": [6],
"correntement": [3],
"attributi": [0],
"mappati": [2],
"digitato": [[3,4]],
"correnti": [6,[0,1,3,5]],
"digitati": [2],
"such": [2],
"voler": [0],
"persa": [3],
"plugin": [[0,1],2,7],
"autocompletertableup": [0],
"principi": [4,7],
"sospeso": [6],
"effetti": [[0,2]],
"effetto": [0,6,1],
"incorporato": [2],
"sottolineerà": [1],
"pensata": [0],
"dell\'alfabeto": [0],
"projectcommitsourcefil": [0],
"editinsertsourcemenuitem": [0],
"documento": [0,2,[3,6],[1,4,7]],
"apri": [0,3],
"viterbi": [6],
"microsoft": [0,6],
"provenienti": [5,[2,4,6]],
"projectnewmenuitem": [0],
"assistenza": [[2,4]],
"ecmascript": [6],
"mappata": [2],
"pulsant": [6,0,[1,4],3,[2,5]],
"documenti": [6,2,0,[3,4],[1,5]],
"segment": [2,6,[0,5]],
"un\'abbreviazion": [0],
"changes.txt": [[0,2]],
"imposterebb": [5],
"ignorando": [4,3],
"estrarr": [5],
"glossari": [0,4,6,[3,5],2,1,7],
"ignored_words.txt": [5],
"accanto": [0,[2,6]],
"impost": [1,[4,6]],
"github.com": [2],
"configuration.properti": [2],
"duplicata": [2],
"contrassegnato": [1,0,4],
"richiederà": [2],
"autocompleterlistpageup": [0],
"sostitu": [6],
"duplicati": [6],
"glossary": [7],
"contrassegnati": [1,[0,6]],
"apra": [[2,6]],
"duplicato": [0],
"incorporata": [6],
"segmentati": [0],
"formattazioni": [[4,6]],
"aiuterà": [2],
"formattazione": [7],
"relazion": [1],
"string": [2],
"import": [4],
"color": [5],
"personalizza": [[1,3]],
"non": [0,2,6,[1,3],4,5],
"dizionario": [1,5,3,4,0],
"tastiera": [6,[0,3,4]],
"not": [2,[1,6]],
"eseguono": [1],
"scritti": [[0,5,6]],
"colpo": [3],
"primario": [6],
"legali": [0],
"applicabili": [5,[0,6]],
"scritto": [[2,6]],
"greek": [0],
"nessuna": [3,6,[0,1]],
"rinominando": [2],
"significanti": [0],
"dall\'estension": [2],
"was": [[0,2]],
"invertit": [1],
"dell\'avvio": [6],
"tastier": [0],
"impediranno": [2],
"viewrestoreguimenuitem": [0],
"rispecchi": [2],
"selection.txt": [[0,3]],
"lontan": [6],
"nessuno": [6],
"xhtml": [0],
"hanno": [2,[0,3,6],4],
"preferibil": [2],
"itoken": [2],
"finder.xml": [[0,5,6]],
"refer": [2],
"selezionar": [6,3,1,4,2,0],
"sequenza": [0,3],
"occupano": [6],
"window": [0,2,3,4],
"classi": [0,7],
"spuntar": [1],
"utilizz": [0,6,[2,5]],
"gestito": [0],
"ampiament": [2],
"allineando": [6],
"farla": [0],
"disable-project-lock": [2],
"omegat.pref": [[0,1,6]],
"when": [2],
"farlo": [0,[1,3]],
"txml": [2],
"far": [2,0,[5,6]],
"carriage-return": [0],
"spuntat": [6],
"multipl": [4,0,2],
"rigido": [2],
"copiandol": [2],
"greco": [0],
"cambiano": [0],
"diventa": [5],
"lavorar": [2,0,5],
"traduttori": [2,[0,4]],
"confermar": [3,6],
"italiano": [0,[1,2]],
"disposizion": [4,1,3],
"contrassegn": [[1,6]],
"suit": [2],
"automaticament": [6,[0,2,3,5],1,4],
"widget": [4,7],
"determinar": [6,[0,1]],
"statistich": [3,2,5,1,0,6],
"impedisca": [6],
"statistici": [5],
"navigazion": [[3,4],5,2],
"modelli": [0,6,[1,2]],
"direct": [0,[2,3]],
"manutenzion": [2],
"manipolazion": [4],
"modello": [0,1,[6,7],2],
"riportar": [0],
"statistica": [6],
"individuerà": [0],
"sull": [0,[4,6]],
"mechan": [2],
"web": [1,6,2,[0,4]],
"en-us_de_project": [2],
"proprietà": [4,2,0,3,5,6,1,7],
"symlink": [2],
"approccio": [2],
"veda": [6,0,[1,3]],
"essersi": [2],
"editselectfuzzy4menuitem": [0],
"editregisteridenticalmenuitem": [0],
"evidenziata": [[4,6]],
"usat": [3],
"aperta": [6,[0,2,3]],
"traducono": [1],
"disattivando": [4],
"hanja": [0],
"sottraendo": [6],
"condizioni": [2],
"contrassegnano": [[0,7]],
"aperti": [[1,3]],
"troppo": [2],
"personalizzazion": [0,3],
"risposta": [4],
"evidenziato": [6],
"aperto": [2,6,3,[0,4]],
"respint": [1],
"usar": [6,0,[1,2],4,[3,5]],
"caricherà": [2],
"intestazion": [0],
"evidenziati": [3,0],
"advanc": [1],
"tentativo": [2],
"compongono": [6],
"suoi": [[2,6],4],
"situazioni": [2],
"promemoria": [[0,3]],
"derivanti": [5],
"l\'elaborazion": [1],
"prendono": [1],
"dict": [1],
"disabilita": [[1,2]],
"giornaliero": [2],
"traducendo": [6,2,1],
"mostrerà": [[1,2,4]],
"cartella_config": [2],
"keep": [2],
"risoluzione": [7],
"option": [[0,1,2,3]],
"esportati": [6,5],
"difettoso": [2],
"riprodurr": [6],
"contenuto": [0,6,2,[1,5],[3,4],7],
"conoscenza": [2],
"esportato": [3],
"visibil": [[0,5]],
"piattaform": [2,[0,1]],
"contenuti": [2,4,[5,6],0],
"sopraccitato": [1],
"comunqu": [0,[2,6]],
"deselezionar": [6,1],
"analizza": [0],
"decompression": [2],
"contenuta": [[2,5]],
"oppur": [6,0,[2,4,5],[1,3]],
"gestisc": [1],
"puntini": [3],
"occorrenz": [6],
"interpretati": [0],
"decomprimerlo": [2],
"aggiornamento": [1,2],
"cercherà": [1],
"mantien": [[1,5]],
"dimenticarlo": [1],
"ricrear": [[0,2]],
"terminal": [2],
"estra": [6],
"processo": [0,2,6],
"esplorar": [0],
"various": [2],
"gestiti": [0,2],
"aggiornamenti": [1,2,[0,3,7]],
"visiv": [0],
"user": [7,2],
"sostituisci": [6,0,3,1,4],
"proxi": [2,1],
"extens": [0],
"back_spac": [0],
"perda": [[2,4]],
"migliaia": [0],
"disattivata": [3,[0,1]],
"definizion": [0,1],
"richiama": [0,6],
"allinea": [6,3,[0,2,7]],
"all\'utilizzo": [4],
"robot": [0],
"all\'original": [[1,3]],
"disattivato": [6],
"risolti": [[1,2]],
"riavviar": [2,0],
"eclips": [2],
"ad": [2,0,6,[1,3,5]],
"diventino": [0],
"sure": [6],
"ramo": [2],
"ag": [[0,3,6],[1,2,4]],
"tabulazioni": [0],
"presenza": [0,[1,2]],
"ai": [2,[0,6],3,5,4,1],
"d\'ambient": [0],
"diff": [1],
"al": [0,2,6,3,4,5,1],
"automat": [[0,2,5]],
"esclusi": [2],
"an": [0,2,[1,5]],
"editmultiplealtern": [0],
"aprirà": [[2,4]],
"proxy": [7],
"ottenuto": [1],
"as": [2],
"predefin": [0,1,[3,6]],
"at": [2],
"esclusa": [2],
"indipendent": [0],
"stilistici": [0],
"convenzion": [0],
"eseguibil": [[0,2]],
"trasferir": [2],
"be": [[2,6]],
"nell\'elemento": [0],
"simultaneament": [1],
"salta": [0],
"filters.xml": [0,[1,2,5,6]],
"prontament": [2],
"elaborar": [2],
"br": [0],
"l\'url": [2,[1,3,6]],
"necessita": [6,[0,1]],
"by": [2,6],
"potenziali": [[0,1,3,5]],
"segmentation.conf": [[0,2,5]],
"identificato": [[1,2]],
"combinazion": [0,6,3,1],
"nell\'espression": [0],
"ca": [2],
"cc": [2],
"l\'avanzar": [5],
"ce": [2],
"ci": [2,3,[0,1,5,6]],
"spazio": [0,6,[1,3,4]],
"leggimi.txt": [0],
"scartat": [6],
"cr": [0],
"cs": [0],
"associazion": [2],
"indietro": [3,[0,2,4],[1,5,6]],
"l\'uso": [[0,2],1,4],
"memorizza": [[0,2]],
"condivis": [6,0],
"apach": [2,6],
"da": [0,2,6,5,3,1,4,7],
"adjustedscor": [1],
"provi": [2],
"de": [1],
"di": [0,2,6,1,3,4,5,7],
"extern": [[2,6]],
"f1": [[0,3,6]],
"do": [2,1],
"f2": [4,[0,6]],
"f3": [[0,3],4],
"principalment": [2],
"bidirezionali": [0],
"f5": [[0,3]],
"ragioni": [[0,1,2,6]],
"chiusura": [0,6],
"dz": [5],
"editundomenuitem": [0],
"possibile": [7],
"raro": [2],
"possibili": [0,2],
"ed": [3,0,[1,2,6],5],
"which": [2],
"insecabili": [0],
"avviati": [6,1],
"belazar": [1],
"en": [0,2],
"preceder": [1,0],
"dovess": [0],
"es": [[0,1],[2,6]],
"avviata": [2],
"eu": [3],
"mediant": [[0,4]],
"dall\'interno": [0],
"minuscol": [0,6,1],
"ripetuti": [[1,6],3,0],
"fa": [0,3,1],
"aiutar": [0],
"operazion": [2,5],
"cambiarla": [2],
"avviato": [2],
"fog": [0],
"aggiuntivo": [0],
"indic": [0,2],
"aggiuntivi": [0,[1,6],2],
"vocal": [0],
"origin": [3,1,[0,4],[2,6]],
"riconosc": [[2,6]],
"rosso": [5,[0,1,6]],
"for": [2,6,0,[1,3]],
"exclud": [2],
"cambiarlo": [7],
"fr": [2,1],
"necessitano": [0,2],
"esport": [2,[0,3,6]],
"content": [[0,2],6,1],
"continueranno": [0],
"aggiuntiva": [2],
"duckduckgo": [1],
"spiegazion": [[0,1]],
"desktop": [2],
"appunti": [3],
"rossi": [1],
"applescript": [2],
"necessità": [[0,5],2],
"foss": [0],
"json": [2],
"gb": [2],
"class": [0,6],
"helplogmenuitem": [0],
"rossa": [1],
"dividi": [6],
"gg": [2],
"presenta": [[0,6]],
"decorazioni": [0],
"editoverwritetranslationmenuitem": [0],
"licenza": [2,0,[3,7]],
"digitar": [[2,6]],
"conserva": [[2,5]],
"presenti": [2,6,1,[0,4],3],
"dalla": [2,0,[3,6],5,4,1],
"aeiou": [0],
"anglosasson": [0],
"form": [6],
"sbloccarlo": [4],
"publish": [2],
"ha": [6,1,4,3,0,2],
"fort": [0],
"dallo": [[0,2]],
"dà": [[1,3]],
"assign": [6],
"typograph": [6],
"aiuto": [0,[2,3,7]],
"hh": [2],
"duser.languag": [2],
"viewmarkparagraphstartcheckboxmenuitem": [0],
"completo": [0,1,2],
"dichiarata": [0],
"convalida": [0,[1,3]],
"file-target-encod": [0],
"fra": [6],
"verd": [6,4,3],
"malament": [6],
"collaboratori": [[0,6]],
"mainmenushortcuts.mac.properti": [0],
"funzionerà": [1],
"id": [1,0,6],
"https": [2,1,0,[4,5]],
"vero": [2],
"coincider": [6],
"impedir": [2,3,[0,6]],
"if": [2,[0,5,6]],
"project_stats.txt": [5,3],
"non-ascii": [0],
"ocr": [6],
"scorrer": [0,[1,4]],
"projectaccesscurrenttargetdocumentmenuitem": [0],
"il": [2,0,6,1,3,4,5,7],
"riconosca": [0],
"in": [2,0,6,3,1,4,5,7],
"lower": [0],
"termin": [1,3,[4,6],0],
"ip": [2],
"index": [0],
"is": [2,0,5],
"it": [2,1,[0,5]],
"riprendet": [6],
"codici": [0,6,2],
"racchiuso": [0,1],
"projectaccesstmmenuitem": [0],
"odf": [0],
"riprender": [[2,5]],
"esser": [0,2,6,1,3,5,4],
"ja": [1],
"glossario": [0,4,6,3,1,5,2],
"odt": [[0,6]],
"gotonexttranslatedmenuitem": [0],
"charset": [0],
"librari": [0],
"esatto": [[0,2]],
"precedenza": [0,[2,3,5],[1,6]],
"toolscheckissuescurrentfilemenuitem": [0],
"libraries.txt": [0],
"learned_words.txt": [5],
"esatta": [6],
"codifica": [0,[6,7]],
"dunqu": [0,2,6],
"rintracciar": [2],
"robusta": [2],
"codifich": [0],
"ftl": [[0,2]],
"abilita": [1],
"acced": [[1,2,3,4]],
"incapsulati": [6],
"possied": [[0,2],[3,5]],
"trasformano": [0],
"themselv": [2],
"viewdisplaymodificationinfoallradiobuttonmenuitem": [0],
"compless": [0],
"completa": [[0,2]],
"visivament": [3],
"la": [0,6,2,3,1,4,5,7],
"registrazion": [[1,2]],
"le": [0,6,2,1,3,4,5,7],
"lf": [0],
"mobilia": [6],
"li": [4,[0,1,3,6],2],
"ll": [2],
"coincidono": [5,2],
"lo": [[0,6],2,1,[3,5],4,7],
"ripristinerà": [2],
"lu": [0],
"salva": [3,2,[5,6],[0,1]],
"genere": [[0,1,2,6],4],
"ja-jp.tmx": [2],
"that": [6,0,2,1],
"cycleswitchcasemenuitem": [0],
"personalizz": [2,0],
"ma": [0,2,[1,5,6],3,4],
"mb": [2],
"sufficient": [0],
"limit": [0,5],
"me": [2],
"precedenti": [0,3,4],
"entra": [1],
"terza": [[0,2,5]],
"singolo": [0,6,4],
"esistenti": [6,[1,2,5]],
"porzioni": [0],
"mm": [2],
"stringh": [0,6,2],
"visibili": [0],
"entri": [6],
"ms": [0],
"stringa": [6,[1,3],0],
"mt": [5],
"ampliar": [6],
"esigenze": [7],
"my": [0],
"dell\'allineator": [6],
"license": [7],
"appartenenti": [1],
"propongono": [2],
"responsabil": [2,3],
"ne": [2,[0,1,3]],
"licenss": [0],
"no": [0,[1,2,5]],
"code": [6,0],
"copiata": [3],
"richiedono": [[0,2,4,6]],
"gotohistoryforwardmenuitem": [0],
"chiedono": [2],
"produrrà": [0],
"head": [0],
"l\'uscita": [3,1],
"dialog": [2],
"l\'equivalent": [2],
"project_save.tmx.timestamp.bak": [5],
"l\'unità": [6],
"racchiusi": [0],
"conterà": [1],
"of": [2,6,[0,1,5]],
"copiato": [[0,1,3],[4,6]],
"possibl": [2],
"racchiusa": [6,0],
"ok": [6,3],
"copiati": [2,[3,6]],
"on": [2],
"abilità": [2],
"or": [2,0],
"concordanza": [3,0,1,4,5,2,6],
"ricreato": [5],
"singoli": [0,[1,6]],
"vincolato": [6],
"aggiornarlo": [5],
"concordanze": [7],
"l\'azion": [6],
"singola": [0],
"editinserttranslationmenuitem": [0],
"compressi": [5],
"fileextens": [0],
"determina": [[1,2]],
"compliant": [6],
"collegamento": [1,[0,2]],
"collegamenti": [0,[2,4]],
"acquistano": [0],
"nulla": [[3,6]],
"po": [[0,2],1,[3,4,5]],
"lì": [[0,1,2]],
"invisibili": [0],
"inclus": [[0,6]],
"fornisc": [0,2,[1,6]],
"qa": [6,3],
"maiuscoli": [0],
"autocompletertablefirst": [0],
"necessari": [2,[0,6],5],
"venga": [0,[2,5,6]],
"ciano": [3],
"maiuscola": [0,3],
"nascost": [4],
"recent": [3,[0,2],6],
"they": [[0,2,6]],
"fornito": [2,[1,3]],
"github": [2],
"chiuderla": [6],
"forniti": [[1,2]],
"editselectfuzzy5menuitem": [0],
"bilingu": [6,[2,5]],
"them": [2],
"maiuscolo": [3,0],
"espressioni": [0,6,1,7,2],
"rc": [2],
"includ": [2,6,0,4],
"nell\'applicazion": [3,2],
"allora": [[2,6]],
"discrepanz": [3],
"né": [0,2],
"spiegazioni": [6],
"preferenz": [3,0,6,4,[1,2],5],
"memorizzata": [5],
"massimo": [0,6],
"tedesca": [1],
"abbassati": [5],
"accesso": [1,0,[2,6,7],3],
"segna": [6],
"definiscono": [0,1],
"sc": [0],
"adiacent": [4],
"navigar": [4],
"se": [2,0,6,3,1,4,5],
"segno": [0,4],
"si": [6,2,0,3,1,4,5,7],
"sl": [2],
"posizionar": [6],
"so": [2],
"massima": [[0,2]],
"chiuder": [[0,2],6],
"exported": [7],
"su": [6,2,0,3,1,[4,5]],
"intero": [4,[0,2]],
"creato": [2,[5,6],0,1],
"intern": [[2,3]],
"quadr": [0],
"creati": [0,5,[3,4,6],2],
"onc": [2],
"elaborazion": [1,3,2],
"norma": [2],
"one": [2,6],
"lilla": [3],
"ripristinar": [[1,5]],
"interv": [1,0],
"riapertura": [2],
"editoverwritesourcemenuitem": [0],
"trasformarla": [0],
"omegat.autotext": [0],
"tedesco": [6],
"kilobyt": [2],
"intera": [6,0],
"enforc": [5,3,[0,2],1],
"remov": [2],
"gestisca": [[0,2]],
"funzionar": [2],
"tl": [2],
"tm": [5,2,3,1,[0,6],[4,7]],
"to": [2,[1,6],0],
"v2": [2,1],
"interi": [1],
"scrivibil": [3,[5,6],0,4],
"solament": [2,1,[0,6]],
"separati": [0,[2,6]],
"dialogo": [[1,6],[0,2,3],[5,7]],
"trova": [0,6,2,4,3,1],
"insensibil": [0],
"separato": [1,[0,5]],
"viewmarkautopopulatedcheckboxmenuitem": [0],
"dover": [[0,2,4]],
"projectwikiimportmenuitem": [0],
"countri": [2],
"dall": [[2,3],[1,4,5,6]],
"singol": [0],
"quali": [0,6,2,[3,5]],
"un": [0,2,6,1,3,5,4,7],
"facendovi": [6],
"up": [0,2],
"ottener": [6,[2,4],[0,1]],
"newword": [6],
"modificabil": [0,4,[2,6]],
"traducibil": [0,6],
"caricamento": [0],
"this": [2,0,[1,5]],
"va": [[0,1]],
"l\'allineator": [6],
"semplicement": [0,6,2],
"iniziano": [0],
"opt": [2,0],
"vi": [0],
"extract": [6,1],
"nascond": [[0,6]],
"considerar": [6,[0,1,2]],
"sostituirvi": [1],
"separata": [2,[3,4]],
"know": [0],
"persino": [[0,2],6],
"allinterno": [0],
"region": [0],
"trovi": [2],
"vs": [1],
"changed": [1],
"accertarsi": [2,1,6],
"l\'angolo": [4],
"vertical": [0],
"pure": [[0,2],5],
"dettag": [2,3,6,0,1,5,4],
"we": [0],
"ortografici": [1,[0,3,6]],
"identificar": [0,[1,2,6]],
"autocompleterlistup": [0],
"ortografico": [1,5,3,[0,6,7]],
"capacità": [1],
"riescano": [4],
"marcatura": [6],
"licenc": [0],
"sé": [0,[5,6]],
"partenza": [0,6,1,2,3,4,5,7],
"sì": [6,[0,2]],
"omegat.project.bak": [2,5],
"repo_for_omegat_team_project": [2],
"ora": [[0,2,4]],
"choic": [2],
"ortografia": [[1,3]],
"projectaccessexporttmmenuitem": [0],
"licens": [2,0],
"org": [2],
"divisa": [6],
"ortografica": [1,[2,6],5],
"divisi": [0],
"accessori": [2],
"sottratta": [5],
"xi": [7],
"superior": [[0,1,4]],
"segu": [2,0,6],
"iniziali": [0,6],
"appendici": [0,[5,7]],
"diviso": [4],
"xx": [0],
"sourc": [2,6,5,3,[0,4]],
"passaggio": [6,2,[0,5]],
"sostituit": [[1,6]],
"supportato": [2,5],
"aprir": [2,6,0,3,5,4],
"indirizzo": [0,2],
"inizialment": [1],
"sostituir": [2,[4,6],[0,1,5]],
"type": [2,[0,5,6]],
"indirizzi": [[1,4]],
"supportati": [[2,6],[1,3]],
"optionsautocompletehistorypredictionmenuitem": [0],
"projectaccesssourcemenuitem": [0],
"yy": [0],
"simbolo": [0,2,[4,6]],
"adattar": [1],
"sovrascriver": [5,[2,4]],
"sensibil": [0],
"simboli": [0,4],
"creano": [3,6],
"nome": [0,2,1,6,4,5],
"come": [0,2,6,1,3,[4,5],7],
"quant": [[0,2]],
"l\'algoritmo": [6],
"nomi": [0,[4,5,6],2],
"ott": [1],
"push": [2],
"testuali": [6],
"exist": [[0,5]],
"installa": [1,2],
"concepita": [[2,5]],
"readme_tr.txt": [2],
"penalti": [5],
"abituati": [3],
"un\'intestazion": [3],
"regist": [2],
"attualment": [3,0,4,[1,2,6]],
"immagini": [0],
"tanti": [2],
"utf8": [0,[3,6]],
"copi": [2,[1,5]],
"facendoli": [0],
"precisa": [6],
"accur": [5],
"virgol": [[0,1]],
"context_menu": [0],
"tradurrà": [6],
"ulterior": [5],
"editsearchdictionarymenuitem": [0],
"intervento": [2],
"ospit": [2],
"tag-valid": [2],
"scriver": [[0,2,3,6],4],
"ufficiale": [7],
"lunghi": [1],
"help": [2,0],
"dovrebb": [[2,3],[0,1,6]],
"multipiattaforma": [2],
"giorno": [2,0],
"dell\'host": [2],
"repositori": [2,5],
"illimitata": [0],
"date": [0],
"depos": [[0,2,4]],
"data": [1,[0,2,6],5],
"ondulata": [1],
"lascia": [4,[1,5]],
"lowercasemenuitem": [0],
"tabell": [0],
"wiki": [[2,5]],
"vecchi": [2,6],
"autocompleterconfirmwithoutclos": [0],
"separ": [6,2,[0,1,4]],
"definizioni": [1,0],
"ripiego": [3],
"cosa": [2,0],
"filepath": [1,0],
"dato": [[0,1,2],6],
"arbitrario": [2],
"dati": [2,3,1,6,5],
"creata": [2,[0,1,4]],
"ja-jp": [2],
"advantageous—or": [2],
"richiamar": [3,6,[0,4],2],
"apriranno": [1],
"venir": [2,[0,6]],
"sent": [2],
"tipicament": [0],
"altrimenti": [5],
"quasi": [[2,3]],
"paesi": [1],
"send": [2],
"here": [6],
"note": [4,0,3,6,7],
"concreta": [6],
"noti": [0],
"line": [[0,2]],
"memorizzati": [[1,4],[0,2,3,6]],
"memorizzato": [3,0],
"hero": [0],
"becom": [2],
"organizz": [0],
"git": [2,5],
"taglia": [4],
"rendendon": [5],
"estensioni": [2,0,7],
"confronta": [6],
"disabilitar": [6,0],
"continuar": [2],
"creare": [7],
"nota": [2,[0,3],6,1,4,5],
"xx-yy": [0],
"evitiamo": [0],
"avviar": [2,6,[0,4]],
"will": [2],
"self-host": [2],
"confronto": [6],
"consideri": [1],
"opzionali": [2],
"follow": [0],
"racchiudendo": [0],
"estension": [0,2,5,3],
"quella": [0,[2,6],[1,4],5],
"targetlang": [0],
"frase": [0,6,1,[2,3]],
"alcuna": [2,[3,6]],
"iniziato": [[2,6]],
"alcuno": [[0,1]],
"frasi": [0,6],
"estender": [2],
"quelli": [[0,6],[1,2,3,4]],
"optionssetupfilefiltersmenuitem": [0],
"quello": [[2,6],[0,1],4,3],
"intend": [0],
"inversa": [2],
"alcuni": [0,2,1,[3,4,5],[6,7]],
"altgraph": [0],
"remoti": [5],
"ultim": [[5,6],[0,3]],
"esterna": [[0,1,3,6]],
"stats-typ": [2],
"valuta": [0],
"eccezioni": [[1,2]],
"sottomenu": [[2,6]],
"remota": [2],
"esterni": [0,1],
"your": [2,6],
"moderni": [2],
"elimin": [2,1],
"garantita": [2],
"esterne": [7],
"esterno": [[1,2]],
"basta": [2,5],
"rubrica": [5],
"xml": [0,2,1],
"remoto": [2,5,[3,4]],
"popolar": [[1,2],5],
"ultimo": [2],
"garantisc": [6],
"serv": [2],
"gli": [6,0,2,1,3,4,7],
"ultimi": [3],
"neutral": [0],
"connession": [1,[2,4]],
"debol": [0],
"ultima": [1],
"xdg-open": [0],
"befor": [2],
"util": [[0,2],6,3,[1,4]],
"ambigui": [0],
"seri": [1,0,2],
"tar.bz": [5],
"much": [1],
"già": [2,6,[0,1],[3,5]],
"registrar": [[1,2,4]],
"l\'autenticazion": [2],
"chieder": [2,4],
"interessanti": [0],
"shebang": [0],
"doppi": [0],
"sopra": [2,0,[1,3,6],4,7],
"editorskipprevtoken": [0],
"differenti": [2,[1,3]],
"prevenir": [2],
"giù": [0],
"ogniqualvolta": [[2,5]],
"agiscono": [0],
"compilar": [3],
"alternar": [4,6],
"regolarn": [6],
"rovesciata": [0],
"sett": [0],
"regolari": [0,6,2,1,7],
"rinominati": [0],
"così": [[0,6],[1,2]],
"aaaa": [2],
"gnu": [2,7],
"direzion": [0,4],
"concluder": [6],
"suzum": [1],
"target.txt": [[0,1]],
"pratica": [2],
"temurin": [2],
"trasforma": [0],
"livello": [6,0,[1,2]],
"divisibili": [3,[0,6]],
"standard": [2,0,[1,3,4,6]],
"d\'espac": [2],
"nell\'intestazion": [0],
"stdout": [0],
"l\'evidenziazion": [6],
"estrazion": [6],
"permettendo": [[1,6]],
"nameon": [0],
"gotonextnotemenuitem": [0],
"determinato": [[0,1]],
"azion": [4,[2,3],[0,1,5,6]],
"area": [4,2],
"gpl": [0],
"accettar": [5],
"newentri": [6],
"risied": [2],
"edizioni": [2],
"list": [0,[2,6]],
"determinati": [[1,2,3,4]],
"aggiunt": [0,[2,3,4,6]],
"autocompleterprevview": [0],
"success": [5,[0,1,2]],
"aggiung": [[2,6],5,[0,1]],
"traduci": [0],
"avess": [0],
"riavviato": [2,0],
"prosieguo": [2],
"interfacc": [[2,3]],
"regional": [0,2],
"formato": [2,0,[1,5,6],4,3,7],
"locali": [6,1,[0,2],[3,4]],
"meccanismi": [[0,2]],
"formati": [2,0,6,[1,3],[4,7]],
"projectcommittargetfil": [0],
"determin": [[0,1,2,3]],
"formata": [0],
"divider": [6,3],
"meccanismo": [2],
"combin": [0],
"po4a": [2],
"presentazion": [0],
"omegat.org": [2],
"basso": [6,[0,1,2]],
"direzionalità": [0],
"pannelli": [4,1,3,[2,7]],
"voci": [[3,6],0,1,[2,4]],
"perform": [2],
"pannello": [4,6,3,1,5,0,2,7],
"voce": [0,6,3,4,1,5],
"mobili": [6,1],
"l\'immutabilità": [5],
"maxprogram": [2],
"it-it": [1],
"bassa": [4],
"with": [2],
"obsoleto": [0],
"pdf": [2,0,3,6],
"riallinea": [6],
"there": [[0,2,5]],
"sicurezza": [2,5,1,6,0],
"mentr": [2,[4,6],0,1,3],
"autocompletertabledown": [0],
"descritto": [[0,4]],
"formano": [0],
"editornextsegmentnottab": [0],
"eseguirsi": [0],
"toolsshowstatisticsmatchesmenuitem": [0],
"descritti": [4,2],
"traduc": [2,[0,1,6]],
"viewdisplaymodificationinfononeradiobuttonmenuitem": [0],
"viceversa": [6],
"differenziar": [3],
"individueranno": [6,0],
"avent": [[0,1]],
"tradur": [[2,3]],
"per": [0,2,6,1,3,5,4,7],
"write": [[0,2]],
"gtk": [1],
"tutt\'": [5],
"decrescent": [0],
"project_save.tmx.bak": [[2,5]],
"nell\'elenco": [0,1,6],
"disabilitati": [[0,1,6]],
"potrebb": [0,6,[2,4],1],
"projectaccesswriteableglossarymenuitem": [0],
"un\'impostazion": [6],
"even": [2],
"disabilitato": [6],
"application_shutdown": [6],
"autocompletertablelastinrow": [0],
"unirà": [6,0],
"gui": [6],
"descritte": [7],
"descritta": [4],
"regexp": [0],
"imperativi": [6],
"assumer": [6],
"sentencecasemenuitem": [0],
"preparazion": [2,0],
"tutti": [[2,6],0,1,3,[4,5]],
"corrent": [6,3,0,2],
"editorcontextmenu": [0],
"determinata": [5],
"tutto": [[0,6],5],
"nativi": [0],
"collaborazione": [7],
"buon": [1],
"optionssentsegmenuitem": [0],
"esegu": [6,[1,2],[0,3]],
"rilasciar": [4,[2,5]],
"nativo": [1],
"bought": [0],
"generazion": [[0,2]],
"assegnato": [[3,4,6]],
"optionsaccessconfigdirmenuitem": [0],
"charact": [2],
"assegnati": [[5,6]],
"framework": [2],
"test.html": [2],
"grammaticali": [3],
"l\'indic": [0],
"unità": [0],
"php": [0],
"assegnata": [[0,3,5]],
"xxx": [5],
"diritti": [2],
"smalltalk": [6],
"risultar": [[1,6]],
"aranc": [0],
"tempo": [2,[0,1,4,7]],
"associata": [3,[0,2,6]],
"l\'asterisco": [0],
"recuper": [1],
"earlier": [[0,5]],
"pseudotranslatetmx": [2],
"salvarlo": [5],
"caricar": [6],
"personalizzata": [2],
"targetlanguagecod": [0],
"editorprevsegmentnottab": [0],
"personalizzati": [0,1,3,2,6],
"toglier": [2],
"debba": [1],
"interrogativo": [0],
"attenzion": [2,[0,6]],
"associati": [[0,2]],
"verificator": [1,0],
"personalizzato": [2],
"annul": [3],
"un\'azion": [6],
"scorrett": [0],
"tramit": [2,6,[1,5],[3,4]],
"bidirect": [3],
"spaziatric": [6,[0,4]],
"associato": [0,1,4],
"un\'indicazion": [0],
"quell\'ordin": [0],
"uniti": [6],
"causa": [[2,3]],
"scaricabil": [2],
"entrar": [3,4],
"diaposit": [0],
"includerlo": [1],
"command-lin": [2],
"assicura": [0],
"scarno": [3],
"chied": [[3,6]],
"consecutiva": [0],
"citato": [3],
"citati": [0],
"potenzialità": [6],
"un\'unità": [0],
"tien": [[3,6],0],
"combinar": [[2,5]],
"consecutivi": [6,0],
"direzionale": [7],
"invisibil": [0],
"proceder": [2,6,3],
"utilizzabil": [6,3],
"projectnam": [0],
"omegat.project.yyyymmddhhmm.bak": [2],
"applicar": [[0,1,6]],
"concatenazion": [0],
"preservata": [2],
"rapidament": [[2,5,6]],
"maiuscol": [0,6,1,2],
"confermato": [3],
"post-elaborazion": [[0,6],1],
"eventi": [6,0],
"preservati": [1],
"installdist": [2],
"piè": [0],
"a-z": [0],
"recuperar": [[1,3]],
"mobil": [6],
"evento": [0,[2,6]],
"password": [2,1],
"modificator": [0],
"seguono": [[0,2]],
"nell\'original": [4],
"compatibilità": [0],
"gotonextxenforcedmenuitem": [0],
"editordeleteprevtoken": [0],
"nuovament": [2],
"divisibil": [6],
"più": [0,2,6,4,1,3,5,7],
"dell\'utent": [0,2,[3,6]],
"diagrammi": [0],
"sostanzialment": [2],
"want": [2],
"un\'interfaccia": [2],
"impiegato": [0],
"personalizzar": [0,[1,6]],
"javascript": [6],
"mediawiki": [[3,6],0],
"input": [[1,2]],
"ossia": [0,[1,4]],
"toolkit": [2],
"volt": [0,[2,3],6],
"join.html": [0],
"must": [2,6],
"sommario": [7],
"stanno": [2,6,4],
"omegat.kaptn": [2],
"poi": [2,[0,6],[1,5]],
"pop": [0,3],
"utilizzarla": [[1,2]],
"coloro": [2],
"usernam": [2],
"colori": [1,3,6],
"fabbrica": [4],
"anoth": [2],
"corrispond": [0,6,3,[1,2]],
"colora": [0],
"memorizzar": [5,[0,4]],
"impossibil": [4],
"volessero": [5],
"soggiacent": [6],
"consultino": [0],
"trascinato": [4],
"l\'amministrator": [2],
"googl": [1],
"l\'opzion": [1,3,[2,5],[0,6]],
"preservato": [0],
"dell\'attual": [2],
"gotoeditorpanelmenuitem": [0],
"corrisponderebb": [0],
"lato": [6],
"ordina": [1],
"attend": [2],
"viewmarkfontfallbackcheckboxmenuitem": [0],
"qualch": [2,1],
"sull\'elemento": [4],
"had": [0],
"estremament": [0],
"fatta": [[2,5]],
"insertcharsrlm": [0],
"sourceforg": [2,0],
"dell\'applicazione": [7],
"continua": [6],
"han": [0],
"corrett": [[0,1,2,4,5]],
"precedut": [0],
"originaria": [4],
"semeru-runtim": [2],
"continui": [0],
"definisc": [1,[0,2],3],
"has": [2],
"tabellari": [1],
"fatto": [2],
"given": [2],
"alfabetici": [0],
"last": [[2,6]],
"editmultipledefault": [0],
"adapt": [6],
"mozilla": [[0,2]],
"editfindinprojectmenuitem": [0],
"risorse": [7],
"implica": [[0,5]],
"pro": [1],
"individua": [0,6],
"definito": [0,[2,3,4]],
"inoltr": [6],
"vuoti": [0,3],
"warn": [2],
"togliendo": [0],
"definita": [[0,6],[2,4]],
"l\'ausilio": [3],
"vuoto": [0,[1,2,3,4,5]],
"trattat": [0],
"tagliar": [0],
"semplici": [0,1,[2,7]],
"plural": [0],
"conta": [3],
"all\'inizio": [0],
"tradurr": [2,0,3,4,6,5],
"l\'area": [4],
"percorso-a-un-file-di-progetto-omegat": [2],
"prodotti": [4],
"raccomand": [0],
"appariranno": [0,1],
"conto": [0],
"pere": [0],
"prodotto": [2],
"avviarlo": [2,6],
"nell\'area": [4,6],
"duckduckgo.com": [1],
"delimitazion": [[3,4],1],
"yet": [2],
"configura": [1],
"colour": [[1,6]],
"chanc": [2],
"sull\'espression": [0],
"time": [2,5],
"riconosciut": [0,[4,6]],
"totalment": [5],
"immediatament": [0,[2,3,4,6]],
"tipo": [0,1,6,[2,4],[3,5]],
"nell\'intero": [6],
"parzial": [1,4,3,[2,5]],
"kanji": [0],
"nonostant": [[1,3,6]],
"program": [2,0],
"flussi": [2],
"python3": [0],
"uguali": [[2,3,4,6]],
"tipi": [0,[2,3],6,1],
"rendendo": [6],
"apportar": [6,0,5],
"tran": [0],
"pagina": [0,[3,6],[1,2]],
"univoci": [6,4],
"iraq": [0],
"attesa": [[0,7]],
"brunt": [0],
"separa": [1],
"mancant": [[0,3]],
"univoco": [3,0],
"parziali": [3,1,4,6,2,0,[5,7]],
"però": [0],
"sicura": [1,7],
"aprendo": [4],
"eseguendo": [[5,6]],
"un\'estension": [0,[2,4]],
"doc-license.txt": [0],
"theme": [[1,6]],
"impostando": [1],
"チューリッヒ": [1],
"all\'estrema": [4],
"editor": [6,0,4,[1,3,5],7,2],
"pseudotranslatetyp": [2],
"esecuzion": [0],
"eliminazion": [2],
"passaggi": [0,2],
"immutati": [5],
"giallo": [3],
"un\'unica": [6],
"dizionari": [1,[4,5],[0,3],6,7],
"facent": [0],
"rispetto": [0],
"annullato": [0],
"apprender": [0],
"ricerca": [6,0,1,3,7,[2,4]],
"projectclosemenuitem": [0],
"tuttavia": [6,0],
"ulteriorment": [6],
"ricerch": [6,1,0,3,[4,5]],
"può": [0,2,6,3,[4,5],1],
"viewmarknonuniquesegmentscheckboxmenuitem": [0],
"chiud": [6,3,0],
"fissa": [[1,5]],
"scura": [1],
"consider": [5,2],
"funzionamento": [7],
"scuro": [1],
"dinamico": [6],
"cronologia": [3,[0,1]],
"findinprojectreuselastwindow": [0],
"campi": [[0,3],6],
"bloccato": [4,2],
"readme.txt": [2,0],
"campo": [6,3,2,[0,1,4]],
"languagetool": [3,1,[6,7]],
"commuta": [0,3],
"ricaricato": [2],
"impostare": [7],
"source.txt": [[0,1]],
"files.s": [6],
"raddoppiandosi": [0],
"sicuro": [2],
"exchang": [0],
"request": [2],
"alternanz": [3],
"currseg": [6],
"their": [2],
"generat": [2],
"trascinando": [4],
"general": [1,[0,2],[3,7]],
"colonn": [6,[0,1]],
"identifica": [1],
"generar": [2],
"soddisfacent": [2],
"spostar": [3,6],
"dimension": [[1,6]],
"frequenza": [2],
"facil": [3,0],
"scaricar": [2,[0,1,5]],
"process": [2],
"l\'inserimento": [3,2],
"avviserà": [6],
"magico": [0],
"autocompletertrigg": [0],
"membri": [2],
"impostata": [[2,3],5],
"alternativa": [6,[0,3],4,[1,2]],
"esercitazioni": [0],
"visiva": [0],
"alternativo": [[0,2,6]],
"contesto": [[1,4],3],
"normalment": [[2,3]],
"parentesi": [0,1],
"condivider": [2,[5,6]],
"impostato": [1,6,[0,2,5]],
"unico": [0],
"cancellerà": [1],
"attivar": [3,6,[0,1,4],[2,5]],
"account": [2],
"sincronizzi": [2],
"been": [2],
"dhttp.proxyhost": [2],
"diversa": [[0,2],[1,6],4],
"predizion": [[0,1]],
"barra": [0,4,6,[2,7]],
"editorprevseg": [0],
"diverso": [[1,3,6],[2,4]],
"marca": [[0,3]],
"mantener": [2,[0,3,6]],
"ignorar": [0,[1,3,5,6]],
"salvarl": [5],
"rileva": [3,[1,2]],
"a-za-z0": [0],
"diversi": [2,1,0,[3,5,6]],
"you": [2,6,0],
"lunghezza": [0,1],
"prime": [1,4],
"discesa": [0],
"tecnici": [0],
"www.apertium.org": [1],
"prima": [2,0,1,6,3,4,5],
"salvati": [[1,3],[0,5,6]],
"specificar": [2,0,6,1],
"tecnico": [[0,2]],
"project_save.tmx.tmp": [2],
"configur": [[2,4]],
"dubbio": [6],
"un\'opzion": [0],
"ricav": [[3,4]],
"unicode": [7],
"funzionalità": [1,2,[0,3,4,6]],
"preserv": [2],
"dell\'editor": [4,[0,1,6],3,2],
"documento.xx": [0],
"mega": [0],
"zurich": [1],
"空白文字": [2],
"optionsworkflowmenuitem": [0],
"digitando": [[1,2,6]],
"releas": [2,0],
"dotati": [0],
"segmentar": [[0,6]],
"salvato": [2,[0,3],4],
"conterrà": [5],
"correzioni": [6,[0,2,3]],
"supplementari": [0],
"limitato": [2],
"gestion": [[2,3],0],
"aggiungern": [6],
"dictroot": [0],
"riguardo": [6,[4,5]],
"approfondir": [0],
"selezionando": [3,[5,6]],
"somiglianti": [0],
"xhmtl": [0],
"vengono": [0,6,2,1,4,3,5],
"destinazion": [0,6,[1,3],2,5],
"frequentement": [0,3],
"questioni": [3,2],
"struttura": [[5,6],0,2,7],
"legger": [6,2,0],
"tale": [0,2,[5,6],3],
"intervallo": [0,2,[1,3,5]],
"subdir": [2],
"sfruttar": [0],
"delimitar": [6],
"tali": [2,6,1,[0,5],3],
"mostra": [3,4,1,0,6,2],
"eseguito": [[2,6]],
"corregger": [[1,2,3]],
"autocompletertableleft": [0],
"passar": [[1,4,5],[2,3]],
"l\'impaginazion": [1],
"forward-backward": [6],
"eseguita": [6,2,[0,3]],
"l\'appendic": [6,[0,1],3,2,[4,5]],
"editorlastseg": [0],
"file-source-encod": [0],
"abbia": [[0,2,6]],
"confusion": [3],
"ripetizion": [4,6],
"some": [2],
"tant": [2],
"session": [[3,4,6]],
"approssimativo": [6],
"divis": [0],
"bisognerà": [[0,2]],
"criteri": [6,0],
"nell\'altra": [6],
"solv": [2],
"primo": [[0,6],3,[2,4]],
"attinenti": [2],
"alpha": [2],
"大学院博士課程修了": [1],
"primi": [0],
"just": [0],
"alterar": [[2,6]],
"divid": [6],
"elencando": [0],
"aggiungerà": [5],
"editexportselectionmenuitem": [0],
"eseguire": [7],
"solo": [0,1,[2,6],3,4],
"home": [0,2],
"disable-location-sav": [2],
"eliminar": [2,0,[5,6]],
"elaborarlo": [0],
"although": [2],
"illustrano": [0],
"projectaccesstargetmenuitem": [0],
"sincronizza": [[2,6],[3,4]],
"iana": [0],
"spostato": [[1,4]],
"attual": [2,6,4,5,3],
"creando": [2],
"varianti": [0],
"stess": [2,0,1],
"bene": [[0,2],6],
"aligndir": [2,6],
"system-host-nam": [0],
"servizio": [2,4,3,1],
"quest\'operazion": [2],
"sostituzioni": [[3,6]],
"mymemory.translated.net": [1],
"sostituzione": [7],
"creat": [2,6],
"operativi": [5],
"quest\'ultimo": [0,2],
"mele": [0],
"python": [6],
"pulsanti": [6,3,0],
"conformement": [2],
"sono": [6,0,2,1,3,5,4],
"abil": [0],
"limitano": [2],
"massimizzato": [4],
"preimpostata": [2],
"crear": [2,[0,6],5,3,1],
"ugual": [1,3,0],
"compatta": [1],
"aprirsi": [2],
"funzioni": [0,6,[4,5],3],
"quest\'ultima": [2],
"codic": [0,2,[1,6]],
"organizzar": [0,2],
"tarbal": [5],
"tratta": [4,[2,6]],
"funziona": [[0,2],[1,3,5,6]],
"migliorar": [[0,6]],
"graffa": [0],
"operar": [2],
"infin": [[1,2]],
"vuota": [2,5,[3,6],[0,1,4]],
"ripristina": [0,6,4,3,1],
"robustezza": [2],
"arrivarlo": [3],
"prototipo": [6],
"file": [2,0,6,3,5,1,4,7],
"gratuito": [7],
"dieci": [[2,6],[3,5]],
"regola": [0,[1,6]],
"member": [2],
"gratuiti": [2],
"leggibilità": [1],
"spazi": [0,3,6],
"operativo": [0,2,3,[1,4,6]],
"regole": [7],
"l\'esecuzion": [2],
"ripristino": [2],
"notazioni": [0],
"meno": [2,[0,1,3]],
"delimitazioni": [4,[0,1,3]],
"menu": [0,3,4,1,6,7,2,5],
"ment": [0],
"consigliata": [2],
"positivi": [1],
"distingua": [0],
"specifichi": [[0,2]],
"asterisco": [0],
"consigliato": [6],
"invoke-item": [0],
"l\'allineamento": [6],
"consonant": [0],
"lasciato": [3],
"affinché": [0,2,[1,3,6]],
"sovrascrittura": [4,3,0],
"progetto": [2,6,5,0,3,4,1,7],
"jolli": [6,[0,2]],
"fini": [0],
"salvano": [2],
"ottenendo": [4],
"source-pattern": [2],
"intermedia": [2],
"trascinati": [4],
"fine": [0,6,2,1,5],
"find": [6,2],
"rilevamento": [1],
"host": [2,0],
"radic": [0,1,4,2],
"orizzontal": [0],
"problemi": [3,1,2,0,[4,5,7]],
"scrittura": [[2,4,6],1],
"modificabili": [0,6],
"autocompletertablepagedown": [0],
"dall\'url": [[1,2]],
"aggiungerla": [0],
"problema": [[2,3,4]],
"possesso": [[1,2]],
"task": [2],
"dall\'esterno": [4],
"aggiungerlo": [2],
"paradigma": [6],
"xliff": [[0,2]],
"true": [0],
"inviar": [2,1],
"present": [[0,2],6,[1,3],4],
"libertà": [7],
"groovi": [6],
"evitar": [0,[2,3]],
"utenti": [2,7,[0,1,3,6]],
"dall\'uso": [2],
"l\'ora": [6,[2,3]],
"contestual": [1,4,[0,3,5]],
"strettament": [2],
"reimpost": [1],
"mess": [2],
"transform": [1],
"aprirla": [2],
"mese": [0,2],
"tipizzato": [6],
"aprirlo": [6,2,[0,4,5]],
"possono": [0,2,4,[1,6],3,5],
"necessary—to": [2],
"messageformat": [1,0],
"fino": [2,6,[0,4],[1,5]],
"master": [2,0],
"spesso": [6,[0,2]],
"progetti": [2,[1,3],0,6,[4,5],7],
"percentual": [4,1,5],
"prodott": [2],
"writer": [0],
"merg": [2],
"rubi": [6],
"utili": [0,6,[2,3,4]],
"sviluppatori": [[0,2]],
"resource-bundl": [2],
"mantenut": [5],
"pubblicar": [2],
"external_command": [5],
"riferimenti": [0,6],
"speciali": [0,[2,5]],
"editorselectal": [0],
"visualizzarn": [1],
"globali": [6,0,1,3,7,4],
"riportato": [[0,2]],
"convertono": [2],
"disconnettersi": [2],
"metodo": [6,[0,2],4],
"runner": [0],
"formattazion": [[0,3],6],
"metodi": [2],
"pertinenti": [[0,1,2,6]],
"riportata": [0],
"sembra": [2],
"omegat-default": [2],
"riportati": [0,5],
"riportate": [7],
"scegli": [3],
"user.languag": [2],
"regex": [0],
"meta": [0],
"avvia": [2,[0,6]],
"except": [0,1],
"premuta": [0],
"un\'icona": [3],
"riutilizzar": [2,[1,3,6]],
"visualizzata": [[1,3]],
"premuto": [6,4],
"naviga": [3],
"l\'intestazion": [[0,6]],
"visualizzati": [1,0,[2,4,6]],
"avvio": [2,0,1,[3,6]],
"global": [6,0,[1,2]],
"esclusioni": [6],
"all\'avviator": [2],
"visualizzato": [6,1,[0,3],[4,5]],
"leggimi": [0],
"risulta": [6,4],
"valor": [0,1,6,[2,3]],
"digitazion": [[2,4]],
"ibm": [[1,2]],
"chiar": [2],
"sistemazion": [3],
"rispettiva": [0],
"chiav": [6,1,2],
"comun": [[1,2,4]],
"delimitator": [1],
"porzion": [[0,6]],
"parsewis": [6],
"aprono": [4,[3,6]],
"ancora": [6,[1,3],0,5],
"conformi": [2],
"poch": [3],
"utilizzar": [2,6,0,[1,3],4,5],
"andata": [2],
"assoluto": [0],
"alfanumerici": [0],
"chiamato": [0,2,[1,6]],
"poco": [[2,6]],
"copiando": [5],
"omegat-cod": [2],
"necessitar": [4],
"l\'accento": [0],
"ricordar": [[2,3]],
"lavorarci": [2],
"riprenderà": [0],
"installar": [2,1,0,5],
"metà": [6,1],
"rimossi": [1],
"racchius": [0],
"guid": [2,[4,7]],
"idx": [5],
"simil": [0,2,1,6],
"conflict": [2],
"ignorati": [0],
"qui": [1,[0,4],6,5,2],
"motori": [3,4],
"rule": [0,1,[5,6]],
"ignorato": [0,[2,6]],
"colorata": [4],
"specificazion": [0],
"autocompleterconfirmandclos": [0],
"projectaccesscurrentsourcedocumentmenuitem": [0],
"desider": [3],
"basat": [[3,6]],
"linux": [0,2,3,4,[1,6]],
"rilasciarlo": [4],
"linux-install.sh": [2],
"rapido": [6,2],
"contezza": [0],
"piccola": [5,3],
"inferior": [6,4,[1,3]],
"file.txt": [2],
"elimina": [0,3],
"grandi": [1,6],
"openxliff": [2],
"dall\'applicazion": [3],
"chiamata": [[2,5]],
"piccolo": [0],
"ifo": [5],
"ricordarsi": [6],
"corsivo": [[0,6]],
"consentendo": [0],
"piccoli": [6],
"excit": [0],
"affidabil": [1,[2,6]],
"sostituzion": [6,3,0],
"comprend": [6],
"risulterà": [[0,4]],
"optionsmtautofetchcheckboxmenuitem": [0],
"regol": [6,0,1,3,2],
"opportuno": [2],
"gruppo": [0,1,6,[3,5],[2,4]],
"sistema": [2,3,0,6,[1,4],5],
"xx.docx": [0],
"letterali": [0,1],
"raggruppar": [[1,4]],
"dell\'eseguibil": [2],
"semplic": [0,[2,6],4],
"sistemi": [2,1,[0,3],5],
"semplif": [2],
"consist": [0,5],
"arabo": [0],
"dall\'elenco": [2,[0,3,6]],
"massimizza": [4],
"dell\'estension": [0],
"opportuna": [2],
"post-elaborazione": [7],
"cartella": [2,0,6,5,3,1,4,7],
"compresa": [0],
"editorshortcuts.properti": [0],
"pigiar": [4],
"grammat": [6],
"compresi": [6],
"sgancia": [4],
"versione": [7],
"valut": [6],
"pagsu": [0],
"linguistica": [0,[1,2,3]],
"inserimento": [2,[3,4]],
"itself": [2],
"restituirà": [0],
"sdlxliff": [2],
"richied": [2,[0,1],6],
"tipologi": [0],
"versioni": [2,[0,6,7]],
"all\'editor": [4,6],
"linguistico": [1,0,5],
"regolazioni": [[5,6]],
"linguistici": [[0,1]],
"riveder": [2],
"requir": [2,[1,6]],
"l\'aspetto": [0],
"requis": [2],
"linguistich": [1],
"tmotherlangroot": [0],
"viewmarknotedsegmentscheckboxmenuitem": [0],
"preferenze": [7],
"preferenza": [3,0,4,2,1,[5,6]],
"dell\'applicazion": [0,2],
"un\'eccezion": [[0,1]],
"l\'esempio": [2],
"gotomatchsourceseg": [0],
"appropri": [6,2],
"sottocartella": [2,6],
"excel": [0],
"cannot": [2],
"restituito": [[0,2]],
"runt": [0],
"stardict": [5],
"omegat.l4j.ini": [2],
"impostazion": [0,[1,6],2,5,[3,4]],
"span": [0],
"gruppi": [1,0,[2,6]],
"seguent": [0,[3,4],[2,6]],
"l\'intera": [[0,2]],
"prefer": [6,0],
"nascondi": [6,[0,3,4]],
"vicin": [1],
"tipografica": [6],
"informatiche": [7],
"l\'intero": [[0,1,2,3]],
"space": [0],
"tipografici": [3],
"stessa": [2,6,0,1,[4,5]],
"fermati": [1],
"sbaglio": [1],
"ドイツ": [6,1],
"veniss": [2],
"visualizzi": [1],
"riscontrano": [3],
"restituisc": [6],
"from": [2,[0,5]],
"quantità": [2],
"cifr": [0,[2,4]],
"addirittura": [[2,6]],
"ricavata": [6],
"editselectfuzzy3menuitem": [0],
"stesso": [0,[1,2],6,[3,4]],
"adatto": [6],
"verifica": [1,3,0,[2,6],4],
"l\'id": [0],
"costituirà": [0],
"stessi": [2,3],
"fals": [0,2],
"visualizza": [3,6,1],
"project.projectfil": [6],
"aggregati": [0],
"adatta": [4,[3,6]],
"compatibil": [2,[0,6]],
"rendono": [0],
"trovar": [0,2,1],
"sincronizzazion": [2,6,4],
"sebben": [0,6,2],
"indirizzati": [0],
"error": [2,4,0],
"momento": [[0,5,6],[2,4]],
"costituisc": [[0,2,5]],
"imposta": [0,[1,3,6]],
"erano": [3],
"shortcut": [2],
"public": [2,7],
"considererà": [4],
"dell\'allineamento": [[2,6]],
"protegger": [1],
"tmx2sourc": [2,[0,5]],
"ini": [2],
"nell\'eseguirl": [2],
"proced": [2],
"rimozion": [2],
"disattiv": [3],
"riferimento": [0,2,5,6,3],
"dimostrano": [0],
"sovrascr": [4],
"poiché": [2,6],
"dhttp.proxyport": [2],
"elenchi": [0],
"evidenzi": [6],
"negar": [0],
"superflua": [5],
"fare": [6,2,0,[1,4],3,5],
"equivalgono": [1],
"subrip": [2],
"copie": [7],
"selezion": [6,3,0,4,[1,2]],
"l\'altra": [2],
"l\'accesso": [3,2,0],
"marcatur": [6],
"score": [1],
"pronta": [2],
"farn": [2],
"scorr": [0,6],
"aggiornato": [[2,5,6]],
"complessi": [[0,6]],
"usano": [0,6],
"utilizzati": [6,[0,2],[1,5]],
"persona": [6,2],
"inclusioni": [0],
"allinearl": [6],
"descriv": [0,2],
"impostar": [2,1,6,[0,5],[3,4]],
"passo": [0],
"illustr": [0],
"complesso": [2],
"struttur": [[0,6]],
"passa": [6,[1,4],[0,3]],
"raw": [2],
"utilizzata": [0,2,6,[3,5],1],
"fonti": [[0,4]],
"tornar": [4,2],
"dovranno": [1,2,6],
"comunicazion": [4],
"conserv": [2],
"pont": [2,[0,5]],
"decomprimer": [2],
"registrano": [0],
"discorrendo": [6,5],
"utilizzato": [0,6,2,3,1],
"copia": [2,5,[1,6],3,[0,4]],
"sviluppo": [2,[0,1,6]],
"posseggono": [6],
"spunta": [0],
"trascinamento": [4],
"specificati": [0,[1,6]],
"raccolta": [5],
"controllato": [[1,2]],
"controllati": [1],
"ambient": [2],
"possiedono": [0,[4,6]],
"manual": [[0,3],2,[6,7]],
"apribil": [2],
"specificato": [2,1],
"carica": [[2,3],[0,1,6]],
"rimaner": [4],
"indicatori": [4],
"cima": [[1,2,4,6]],
"fatt": [2],
"appendic": [0],
"unbeliev": [0],
"close": [6],
"aggressiva": [[0,3]],
"considerazioni": [2],
"fase": [6,[2,4]],
"abc": [0],
"abilitar": [[0,2,5],[1,6]],
"permessi": [6],
"permesso": [2],
"toolbar.groovi": [6],
"concordanz": [1,3,4,5,[2,6],0],
"progressivament": [1],
"precedent": [3,0,[2,4,6]],
"algoritmi": [6],
"specificata": [[0,2],[1,5]],
"caricando": [1],
"spicco": [2],
"algoritmo": [6,0],
"l\'altro": [[1,3]],
"permessa": [0],
"fasi": [6],
"sintassi": [0,2,6],
"raccomanda": [2],
"colorazioni": [0],
"iso": [[0,2]],
"isn": [[0,2]],
"giapponesi": [1],
"farà": [[0,2]],
"optionspreferencesmenuitem": [0],
"vengano": [2,0],
"laddov": [[2,6]],
"maggioranza": [0],
"cartell": [2,6,5,[0,3]],
"coprono": [0],
"scrive": [[2,6]],
"act": [2],
"estranei": [0],
"post": [0],
"utilizza": [6,0,[1,2],5,4],
"glossary.txt": [[2,5],[0,3]],
"scartato": [5],
"spuntata": [2],
"add": [2],
"cambio": [2],
"casella": [6,1,0],
"utilizzo": [2,6],
"equival": [6,1],
"consent": [6,4,[1,2,3]],
"utilizzi": [[0,1]],
"accedi": [0,[3,5],[1,2,6]],
"pertanto": [6,2],
"rfc": [6],
"prescinder": [2],
"rfe": [6],
"cambia": [[0,6],[2,3,5]],
"danneggiati": [2],
"concep": [5],
"soddisfatti": [[2,6]],
"buona": [[2,7]],
"shell": [0],
"entry_activ": [6],
"subito": [[0,5,6],1],
"chiest": [2],
"esclud": [2,0],
"optionsautocompleteshowautomaticallyitem": [0],
"gotoprevxautomenuitem": [0],
"exec": [0],
"chiamandolo": [2],
"rimuoverlo": [2],
"sviluppar": [2],
"sarebb": [[0,2]],
"untar": [2],
"millimetrico": [6],
"muoverlo": [4],
"estesi": [0],
"risors": [6,5,[0,2]],
"ishan": [0],
"pasta": [0],
"pseudotradotta": [2],
"pseudotradotto": [2],
"scopi": [[0,2]],
"caratter": [0,1,[3,6],[2,4]],
"scopo": [0,7,[1,2,6]],
"esteso": [2],
"modifi": [2,6],
"allineato": [3],
"mappatura": [2,6],
"estraneo": [1],
"allineati": [[0,2,6]],
"l\'abbreviazion": [0],
"filtra": [6],
"quanti": [2],
"correzion": [[1,6],2,5],
"corrisponder": [0,2],
"clone": [2],
"ovunqu": [3,0],
"targetlanguag": [[0,1]],
"distribuzioni": [2],
"virgolett": [0,6],
"quanto": [[1,6],[0,2,4,5]],
"estern": [6,1,3,4,[0,5]],
"filtro": [0,6,2,1,3],
"memorie": [7],
"memoria": [2,6,0,5,[3,4],1],
"collega": [2],
"concorda": [[0,1]],
"properti": [2,0],
"filtri": [2,[0,6],1,3,7,5],
"corrispondenti": [6,5,[2,3,4]],
"durant": [[2,6],[0,3],[1,4,5]],
"editselectfuzzyprevmenuitem": [0],
"copiar": [2,6,[0,1,5]],
"inserirlo": [4],
"cercando": [2,0],
"lettura": [0],
"sempr": [0,[1,2],[5,6],3,4],
"letterario": [0],
"controllano": [2],
"algorithm": [3],
"desiderano": [2],
"script": [6,0,2,3,1,7,5],
"oltr": [[0,6]],
"ritorni": [0],
"ricorsiva": [6],
"spellcheck": [6],
"ritorna": [6,5],
"ricorsivo": [6],
"basata": [0,[4,6]],
"distinguer": [0],
"other": [2,6],
"c\'è": [3],
"cinqu": [1],
"identico": [[0,6]],
"local": [2,[1,6],3,0,[4,5]],
"scaricabili": [[1,2]],
"verificando": [2],
"locat": [2,6],
"yield": [6],
"sostituito": [0,1],
"crea": [0,[2,6],3,5,4,1],
"sostituisc": [6,3],
"dipendono": [[3,6]],
"affermato": [0],
"rle": [0,3],
"interferisc": [3],
"resto": [0],
"repo_for_all_omegat_team_project_sourc": [2],
"lento": [2],
"caratteristich": [[0,6]],
"memorizzarlo": [0],
"rlm": [0,3],
"segmento": [3,4,6,0,1,[2,5],7],
"identich": [4],
"utilizzano": [2,[0,6],1,7],
"identici": [3,[2,5,6]],
"segmenti": [6,3,0,[1,4],5,2],
"identica": [1],
"applicabil": [6],
"basano": [0,[2,3,6]],
"c-x": [0],
"suggerimenti": [[1,4],0],
"all\'avanzar": [2],
"mode": [2,6],
"corrispondenz": [6,[1,5],[0,3]],
"modi": [2,3],
"usata": [0],
"suggerimento": [1,4],
"corrispondent": [6,0,3,1,[2,4,5]],
"usato": [0,1],
"segnalibri": [0],
"specificando": [0],
"toolsshowstatisticsstandardmenuitem": [0],
"potrà": [[2,6]],
"modo": [0,2,3,6,5,1,7],
"usati": [[3,6]],
"all": [6,1,[2,3],0,5,4],
"dall\'allineamento": [6],
"c.t": [0],
"alt": [0,3],
"modalità": [2,4,[3,6]],
"real": [[0,2,4]],
"unit": [0],
"dell\'elenco": [[3,6]],
"finali": [3],
"attribuirg": [0],
"two-lett": [6],
"fondo": [1],
"registrata": [3],
"eu-direzion": [3],
"riguardano": [2],
"l\'elaborator": [6],
"colonna": [6,3,0],
"registrati": [[2,6]],
"ritorno": [0,6,2],
"completato": [[0,6]],
"singolarment": [6],
"l\'interfaccia": [2,0,1],
"ispir": [6],
"tkit": [2],
"nesimo": [6],
"synchron": [2],
"and": [2,6],
"inviato": [1],
"modifica": [6,0,1,4,3,2,5,7],
"aspettano": [0],
"ani": [2,6],
"l\'attivazion": [6],
"modifich": [[0,2,6],3,5,[1,4]],
"completata": [2],
"minuti": [2,[1,3,5]],
"riutilizzarla": [2],
"ricorderà": [2],
"minuto": [2],
"ciascun": [0,2,3,[1,6],[4,5]],
"nell\'estension": [2],
"ant": [[2,6]],
"leggerment": [[0,4]],
"consistono": [3,6],
"registrato": [0],
"considerando": [0],
"attribuito": [1],
"dischi": [6],
"argomento": [0],
"corrispondenza": [0,5],
"unnecessari": [2],
"helplastchangesmenuitem": [0],
"traccia": [3,[2,4]],
"mobilità": [6],
"redistribuirne": [7],
"argomenti": [[0,2]],
"omegat.ex": [2],
"interruzion": [[0,1]],
"grado": [2,[0,6],[4,5]],
"perché": [0],
"sourcetext": [1],
"tradotto": [0,2,6,3,[1,4],5],
"tasti": [6,0,[3,4],7],
"comodità": [6],
"accurato": [6],
"compon": [6],
"fornitori": [1],
"tabelle": [7],
"jar": [2],
"mistak": [6],
"debbano": [6],
"api": [[1,6]],
"tabella": [0,1,4,3],
"editselectfuzzy2menuitem": [0],
"prossimo": [0,[1,2,3,4]],
"scoraggianti": [0],
"apr": [3,6,1,[2,4]],
"modificando": [2],
"scaricano": [2],
"ciascuna": [0,1,[3,6]],
"tasto": [0,[3,6],[1,4]],
"impararl": [0],
"chiesto": [[3,6]],
"letter": [0,2,3],
"editornextseg": [0],
"ciascuno": [[0,2,6],4],
"libreri": [0],
"completamenti": [1],
"editselectfuzzynextmenuitem": [0],
"gotonextxautomenuitem": [0],
"completamento": [0,1,4,[3,7],6],
"l\'identificator": [3],
"anziché": [6,0,3],
"cloud.google.com": [1],
"are": [6,1],
"potranno": [6,3],
"readme.bak": [2],
"arg": [2],
"mancanti": [3,0,[2,4]],
"where": [2],
"svariat": [5],
"parzialment": [[2,6]],
"l\'espression": [0,6,1],
"sblocca": [4],
"rinvenuti": [[2,4]],
"rivolgono": [2],
"circonflesso": [0],
"ulteriori": [[2,6],[0,1,3,4]],
"significato": [0,[4,6]],
"call": [0],
"sblocco": [4],
"tradotti": [6,3,0,2,4,5,1],
"percentuali": [[1,4,5]],
"sbloccato": [4],
"ask": [2],
"tradotta": [2],
"scelta": [6,3],
"generica": [1],
"toolsshowstatisticsmatchesperfilemenuitem": [0],
"grafica": [2,6],
"vicino": [2],
"run": [0,2],
"grafici": [0],
"grafich": [2],
"generico": [2],
"linguetta": [4],
"desiderati": [0],
"view": [2],
"either": [2],
"editorshortcuts.mac.properti": [0],
"molt": [0],
"generich": [1],
"generici": [0],
"ricercando": [0],
"titlecasemenuitem": [0],
"desiderata": [[1,4]],
"yourself": [2],
"opzional": [[0,3]],
"permetterà": [[1,2]],
"those": [2],
"editcreateglossaryentrymenuitem": [0],
"istanza": [2,[3,4]],
"precauzioni": [2],
"punteggiatura": [0],
"vien": [2,6,0,3,[4,5],1],
"introduc": [6],
"多和田葉子": [6],
"name": [0],
"procedendo": [6],
"memorizzazion": [1,[2,3]],
"protocollo": [2,1],
"allo": [2,[1,3,6],0],
"doppio": [2,6,[0,3,4]],
"basati": [6,[0,1]],
"minuscola": [0,3],
"book": [0],
"consentono": [0],
"show": [6],
"minuscolo": [[0,3]],
"alle": [7],
"l\'estension": [0,2,[1,6]],
"strutturalment": [2],
"alla": [0,6,2,3,[1,4],5],
"minuscoli": [0],
"lavoreranno": [2],
"basato": [6],
"comput": [2,1],
"arriva": [0],
"contien": [0,5,2,6,1,4,3],
"situato": [6,2],
"un\'interruzion": [0,6],
"editortogglecursorlock": [0],
"arrivo": [0,3,2,6,1,4,5],
"condivisa": [[2,5]],
"associ": [1,[0,3]],
"situata": [2],
"avvertimento": [6,[0,1,2],3,5,4],
"new_fil": [6],
"situati": [[1,2]],
"quando": [6,2,0,1,4,[3,5]],
"abbreviata": [0],
"elaborazione": [7],
"target": [1,6,3,5,2,[0,7]],
"rinominarla": [2],
"capo": [0],
"sorgent": [2,0],
"condivisi": [[2,6],4],
"neanch": [1],
"condiviso": [2],
"config-dir": [2],
"restino": [5],
"blocchi": [[0,6],7],
"rinominarlo": [2],
"editorskipprevtokenwithselect": [0],
"visitato": [5],
"all\'espression": [0],
"rimuovi": [0,6,3],
"sbagliat": [6],
"termbas": [0],
"sequenz": [0],
"finestr": [6,[0,1,3]],
"piano": [1],
"caso": [0,2,3,[4,5],6],
"pulizia": [6],
"rappresentano": [1,[0,6]],
"casi": [2,3,[0,6]],
"rend": [0,6,[2,4]],
"comodament": [6],
"case": [2],
"visitati": [3],
"frammenti": [0],
"nativament": [2],
"esistono": [0,2,3],
"targettext": [1],
"includono": [[0,3],[2,4]],
"presentano": [[0,1]],
"comportamento": [2,3],
"virgola": [0,2],
"all\'istant": [1],
"condivision": [2,[5,6]],
"selettiva": [2],
"individuano": [0,3],
"sarebbero": [2],
"proprio": [2,0,5,[1,6],4],
"pattern": [2],
"cifra": [0],
"conteggiarl": [1],
"compil": [6],
"utilizzino": [2],
"riproduc": [0],
"illustra": [0],
"propria": [2,0,1],
"mostrata": [[0,1]],
"edittagpaintermenuitem": [0],
"proprie": [7],
"ritardo": [2],
"adiacenti": [4],
"mostrato": [[0,6],[2,3,4]],
"disattivarlo": [3],
"display": [2],
"intrapresa": [3],
"unicod": [0,3],
"viewmarknbspcheckboxmenuitem": [0],
"mostrati": [[3,4,5],2],
"memorizzazione": [7],
"contrassegnerà": [[0,3]],
"projectmedcreatemenuitem": [0],
"dall\'adoptium": [2],
"indicazioni": [0],
"whitespac": [2],
"presentati": [[2,3,4]],
"credenti": [2],
"utilizzabili": [[0,2,6]],
"conteggiati": [3,1],
"presentata": [2],
"msgstr": [0],
"individuata": [0],
"automatiche": [7],
"l\'original": [6],
"maiusc": [3,0,6],
"solito": [2],
"individuato": [3],
"poter": [6,[5,7]],
"separar": [0,[2,6],1],
"individuati": [[0,3],2],
"evidenzierà": [0],
"daili": [0],
"facoltativo": [3,2],
"both": [6],
"important": [[0,5]],
"sorgenti": [2],
"inserisca": [3],
"omegat.project": [2,5,[1,4,6]],
"excludedfold": [2],
"targetcountrycod": [0],
"inserisci": [0,3,1,[4,6],[2,5]],
"riport": [0,6],
"procedimento": [6],
"altr": [2,0,[1,6],[4,5]],
"l\'utent": [0,3],
"escluder": [6,4],
"insert": [0],
"alto": [[1,6],4],
"rete": [2],
"direttament": [2,3,[5,6]],
"automazion": [2],
"coppi": [2,[0,6]],
"sincronizzar": [5],
"marcator": [[1,4]],
"l\'intervallo": [1],
"programmazion": [6],
"original": [2,0,6,3,4,[1,5]],
"also": [2],
"scaricato": [2],
"differ": [2,6],
"danneggiar": [2],
"consol": [2],
"allenarsi": [2],
"mous": [6,[3,4],[1,5]],
"resa": [0],
"alta": [4,1],
"sbc": [4],
"separatori": [[0,4]],
"consultar": [2,0],
"annullar": [0],
"itokenizertarget": [2],
"viewmarkwhitespacecheckboxmenuitem": [0],
"l\'ordinaria": [0],
"equivalent": [4],
"oggetti": [6,2],
"configurazion": [2,0,1,6,3,4],
"avranno": [1],
"oggetto": [6],
"l\'eccezion": [2],
"complet": [0],
"bak": [2,5],
"tradott": [0,1,[3,6]],
"paragrafo": [0,6,[1,4],[2,3]],
"offer": [2],
"corrispondono": [6,[1,2,4],[0,5]],
"pulisci": [3],
"accedervi": [0,[2,3,5]],
"grigio": [3],
"paragrafi": [0,[3,4,6],1],
"distinguono": [0],
"ricarica": [3,6,5,0],
"segnalazion": [0],
"jre": [2],
"ufficio": [2,6],
"caricato": [6,[2,5]],
"popolata": [[2,5]],
"attivando": [3,4],
"caricati": [2,1],
"personal": [2],
"popolati": [[0,5],3,[1,2]],
"secur": [2],
"imposterà": [[2,6]],
"influenzar": [2],
"destro": [4,6,3,[2,5]],
"riporta": [0,4,[1,2,3]],
"destra": [0,6,4,3,2],
"l\'impostazion": [[0,4],1],
"schema": [1,6,2],
"vogliono": [2],
"permetti": [1,3],
"schemi": [1],
"icon": [4],
"delet": [[0,2]],
"projectaccessglossarymenuitem": [0],
"javadoc": [6],
"richieder": [[1,3]],
"indicata": [4,3],
"separazion": [[3,6]],
"indicati": [0,3],
"associar": [0,2],
"set": [[0,2],[1,6]],
"operano": [0],
"avvierà": [[0,1,2,3]],
"vist": [0],
"contain": [[0,5]],
"categorie": [7],
"parol": [0,6,[3,5],1],
"categoria": [0],
"rappresentazion": [6],
"detta": [5],
"ricontrollar": [6],
"funzioneranno": [6],
"rispettivament": [5,[0,2]],
"procedura": [2,[3,6]],
"project.sav": [2],
"l\'elenco": [0,1,3,[2,6]],
"tendina": [[0,1,6]],
"detto": [[0,2]],
"copierà": [4],
"offic": [0],
"chiusa": [6],
"frequenti": [[0,2]],
"repositories.properti": [[0,2]],
"genereranno": [3],
"consultarlo": [0],
"parti": [2,[1,4,6],[0,3,5]],
"brevement": [0],
"repositories": [7],
"projectsavemenuitem": [0],
"terminologich": [0],
"terminologici": [0,3],
"xmx6g": [2],
"autocompletertablefirstinrow": [0],
"powerpoint": [0],
"forzar": [[0,1,3,6]],
"forzat": [5],
"nell\'operazion": [1],
"segnatament": [0],
"riparar": [2],
"tmautoroot": [0],
"chiuso": [4,[2,3]],
"l\'articolo": [0],
"compar": [4],
"cursor": [4,3,0,6],
"insertcharslrm": [0],
"abbina": [6],
"provar": [6,0],
"visualizzandolo": [2],
"applicata": [[0,2]],
"sia": [0,2,[1,6],3,5,4],
"molti": [[0,2]],
"cambiando": [6],
"unisci": [[1,6]],
"validità": [0],
"sig": [1],
"esempio": [0,6,[1,2],3,4,5],
"richiest": [1],
"ordinati": [4],
"client": [2,[0,5]],
"applicato": [0],
"conversion": [2,1],
"corrisponda": [0],
"restano": [6],
"dell\'ultima": [[1,3]],
"comparir": [2],
"indicano": [0,3],
"falsi": [1],
"indicant": [[1,6]],
"attuali": [3],
"descrizioni": [3],
"correttezza": [4],
"capitolo": [2,6,0,[3,4]],
"racchiudono": [0],
"dell\'ultimo": [[1,5]],
"foundat": [2],
"targetroot": [0],
"prompt": [2],
"seguenti": [6,0,2,[3,4],[5,7]],
"select": [2],
"predizioni": [1],
"misur": [2],
"funzionant": [5],
"bin": [0,[1,2]],
"penalità": [5,1],
"dell\'altro": [2],
"l\'ultimo": [0,[3,4,6]],
"funzionano": [0],
"apertium": [1],
"cerca": [6,3,[0,1],4],
"bis": [0],
"kaptain": [2],
"meta-inf": [2],
"impedisc": [2],
"projectopenmenuitem": [0],
"autom": [2],
"offrono": [2],
"inserirl": [6],
"necessario": [[0,2],6,5,[1,3,4]],
"scorretti": [6],
"accento": [0],
"decid": [0,2],
"scorretto": [[2,3]],
"sistemati": [6],
"molto": [0,[2,6]],
"inserisc": [3,[4,6],2,[0,1]],
"necessaria": [[1,2]],
"squadra": [2,1,[0,6,7]],
"mappatur": [2,6],
"ridurr": [6],
"l\'ultima": [[0,2,3]],
"contengono": [0,[2,6],[3,4]],
"begin": [0],
"obbligatoria": [[0,6]],
"viewmarktranslatedsegmentscheckboxmenuitem": [0],
"valu": [0,2],
"ilia": [2],
"funzionali": [6],
"conferma": [[0,1,5],[2,3,6]],
"vale": [4],
"simili": [2,1,[4,6]],
"blc": [4],
"programma": [[0,1,6],2,[4,5]],
"cinesi": [1],
"disattivar": [3,6,4],
"rilevano": [6],
"uxxxx": [0],
"semplicità": [2],
"evitata": [6],
"obbligatorio": [6],
"macos": [7],
"programmi": [[0,2,6]],
"break": [0,1],
"editselectfuzzy1menuitem": [0],
"rilevant": [2],
"posizionati": [2],
"maniera": [3,0],
"blu": [6,4],
"scheda": [4,1],
"posizionato": [0],
"hide": [[2,4]],
"all\'interfaccia": [[4,5]],
"azioni": [6,3,1,[0,2,4],7],
"piuttosto": [0,[2,3]],
"consultazion": [2],
"passando": [[3,4]],
"un\'applicazion": [2],
"interagivano": [2],
"autocompleterlistpagedown": [0],
"posizionata": [2],
"auto": [3,5,0,2,[1,6]],
"implicano": [2],
"scrivern": [6],
"editorskipnexttokenwithselect": [0],
"combinazioni": [0,[1,6]],
"comparsa": [6],
"applicano": [6],
"posto": [3,[2,6]],
"offra": [0],
"download": [2],
"oracl": [0],
"editortoggleovertyp": [0],
"inserirà": [1],
"suggerita": [3],
"linguaggio": [6],
"differenz": [1,0],
"different": [[0,1,2]],
"posta": [0],
"gradlew": [2],
"gerarchia": [5,2],
"ritornar": [4],
"dietro": [0],
"rifiuta": [2],
"modif": [0,[2,6]],
"interpretar": [0],
"consenti": [1,6,0],
"preserva": [0],
"errori": [3,2,1,[0,6]],
"viewmarklanguagecheckercheckboxmenuitem": [0],
"particolarment": [6,4],
"sceglier": [6,0,1,2],
"produc": [2],
"previsti": [2],
"inizia": [0,[2,3]],
"sottolineata": [4],
"box": [0,1],
"bloccar": [2],
"switch": [[1,6]],
"inizio": [0,3],
"inserita": [6,3,[0,1,5],4],
"total": [4,6,3],
"dettagliata": [0],
"tenta": [6,1,0,2],
"bundl": [1,[0,2]],
"interruzioni": [0,1],
"l\'installazion": [2,1],
"macchina": [1],
"involv": [2],
"sottolineato": [3],
"logogramma": [0],
"scorretta": [5],
"macro": [6],
"inserito": [3,[0,1],[2,4]],
"src": [2],
"gigabyt": [2],
"control": [[0,3]],
"grassetto": [1,[4,6],0],
"informa": [2],
"no-team": [2],
"dell": [[0,6],2,3,1,4,5],
"l\'ambito": [[0,6]],
"incorpora": [1],
"sistemar": [6],
"lissens": [0],
"srx": [[0,5]],
"ospita": [4],
"assegn": [0],
"suggerisc": [4],
"possa": [2],
"ssc": [4],
"riutilizza": [3],
"nuovo": [[2,6],0,3,1,5],
"traduttor": [2,[0,1],5],
"ssh": [2],
"aumentar": [0],
"riutilizzo": [2],
"environ": [2,0],
"brevi": [[1,6]],
"esportar": [6],
"vari": [0,[2,3,6],[1,4],5],
"friend": [0],
"secondari": [0],
"sta": [[2,6],[1,4]],
"l\'apostrofo": [0],
"assegna": [0],
"generalment": [[0,4]],
"kde": [2],
"sto": [4],
"segnalarlo": [0],
"stampa": [[0,2]],
"principali": [2,0],
"principale": [7],
"mieifil": [2],
"sua": [2,6,5,[0,1,3,4]],
"motor": [4,1,[3,6]],
"sue": [[0,2,5],4],
"access": [2],
"currenc": [6],
"sui": [2,0,[1,3]],
"languag": [6,2],
"nessun": [1,0],
"preferito": [6,[0,2]],
"sul": [6,2,0,4,1,5],
"suo": [2,[4,5],6,0],
"chiudi": [6,0,3],
"apportata": [5],
"distingu": [6],
"current": [6],
"sovrascritt": [2],
"preferita": [0],
"optionsglossaryfuzzymatchingcheckboxmenuitem": [0],
"porta": [2],
"key": [[2,6]],
"condividono": [[0,2,6]],
"fuori": [0,2,5],
"l\'approccio": [2],
"msgid": [0],
"svn": [2,6,5],
"omegat-license.txt": [0],
"quell": [6,[1,5],0,3,2],
"segnaposto": [1,0],
"stori": [0],
"credenziali": [1,2,[4,7]],
"nuova": [2,6,3,0,1,[4,5]],
"rapporti": [[2,6]],
"facoltà": [2],
"sovrascriva": [0],
"nuovi": [6,2,0,3],
"editreplaceinprojectmenuitem": [0],
"un\'esportazion": [2],
"but": [0,6],
"memorizzerà": [6],
"dell\'utente": [7],
"editordeletenexttoken": [0],
"vanno": [[1,2]],
"installato": [2,1,[0,3,4]],
"multilingu": [0],
"accoppiato": [0],
"quest": [6,2,0,[1,5]],
"ripulir": [3],
"zero": [0,6,2],
"conflitti": [0],
"generati": [[0,1]],
"potrebbero": [2,0,6],
"l\'ordin": [[1,3,4,5,6]],
"rinominar": [2],
"conflitto": [2],
"generato": [2],
"variant": [2],
"generata": [2],
"soltanto": [6,0,[1,3],2],
"annulla": [0,3,6],
"comparirà": [[0,1]],
"variano": [[0,2]],
"riquadro": [6],
"seleziona": [3,0,1,6,4],
"gotoprevioussegmentmenuitem": [0],
"legenda": [3],
"accett": [[1,6]],
"composta": [[1,4]],
"facilitar": [[2,5,6]],
"dichiarazion": [0],
"gotopreviousnotemenuitem": [0],
"stderr": [0],
"editredomenuitem": [0],
"verif": [0],
"composti": [0],
"uilayout.xml": [[0,5]],
"installati": [1,3],
"normali": [0,2],
"sourceroot": [0],
"risultanza": [6],
"quadro": [6],
"preferisc": [[0,5]],
"installata": [2],
"trovato": [0],
"ripristinati": [2],
"interromper": [0],
"sinc": [2],
"higher": [0],
"compatibili": [[1,2]],
"interfaccia": [2,6],
"inatteso": [2],
"titolo": [[0,3,6]],
"coppia": [1,[0,2,6]],
"allegato": [1],
"quadra": [0],
"evidenziazion": [0],
"allegati": [6],
"apport": [[0,1,2,4],3],
"seguendo": [[4,6]],
"conosciuti": [0],
"nidif": [0],
"assent": [2],
"deve": [0,2,[1,5]],
"restanti": [4,[2,3,6]],
"normal": [[0,6],[1,2,3,5]],
"figure": [7],
"figura": [4],
"implementa": [1],
"nome_utent": [2],
"example.email.org": [0],
"richieda": [1],
"accennato": [0],
"seconda": [1,[0,2],3,[4,6]],
"runtim": [2,0],
"individu": [[0,6]],
"aggiungono": [6],
"autenticazion": [2,1,4],
"differenza": [[0,1,6]],
"bianco": [0],
"specificano": [0],
"secondo": [4],
"filenam": [0],
"tener": [2,0,4],
"secondi": [1],
"guida": [5,[0,3],[2,6],[1,4]],
"roam": [0],
"between": [2],
"guide": [7],
"certa": [2],
"interni": [[0,2]],
"nbsp": [6],
"gotosegmentmenuitem": [0],
"interno": [2,[1,3,4]],
"eventualment": [2],
"preceduto": [0],
"generano": [2],
"preceduti": [0],
"l\'identificativo": [1],
"preceduta": [0],
"interna": [4],
"initialcreationd": [1],
"verrà": [[0,6],2,[1,5],3,4],
"usando": [5],
"flag": [3,1],
"helpaboutmenuitem": [0],
"deriva": [2],
"modificatori": [0,7,3],
"salvat": [6,[1,5]],
"eventuali": [[1,3]],
"salvar": [[2,6],0],
"fattori": [3],
"tengano": [6],
"limitar": [0],
"apert": [1],
"parer": [4],
"creerà": [[2,4]],
"eccessivament": [1],
"leav": [1],
"risoluzion": [2,5],
"tabellar": [3],
"pochissimi": [3],
"sito": [2,[0,1]],
"certi": [[2,4,5]],
"generali": [6,2,0],
"elementi": [0,3,[1,4,6],[2,5]],
"identificator": [0,3],
"token": [0,2,[1,6],[4,5]],
"della": [2,6,0,1,4,3,5,7],
"filter": [2],
"maggior": [0,3,1],
"site": [1],
"traduzione": [7],
"installare": [7],
"elemento": [[0,3,6]],
"projectroot": [0],
"posizioni": [2,6,[0,1,5]],
"delle": [7],
"traduzioni": [4,5,1,2,[0,3],[6,7]],
"posizione": [7],
"arancion": [[4,6]],
"omegat.log": [0],
"localment": [2,3,1],
"dello": [[1,2],[0,5]],
"siti": [0],
"autocompletertableright": [0],
"ipotizzi": [0],
"aver": [2,6,[0,1],4,3],
"soglia": [1,[2,4]],
"garantir": [2],
"verso": [2],
"ingles": [2,[1,6]],
"tab": [0,3,1,4],
"generale": [7],
"divers": [[0,4],[5,6],[1,2,3]],
"l\'eliminazion": [2],
"should": [2],
"tag": [1,0,3,6,2,4,7],
"poterl": [2],
"tal": [[0,1,2,6]],
"individuar": [0,2],
"onli": [2],
"filtrar": [2],
"projectreloadmenuitem": [0],
"almeno": [[2,4,6]],
"colleg": [2],
"tranquillament": [2],
"codificato": [5],
"spostarsi": [4],
"disponibili": [6,[0,2],1,4,5,3],
"nell\'ordin": [4],
"ripetizioni": [3,[0,6]],
"servizi": [[1,2]],
"perdita": [2],
"provis": [2],
"tbx": [0,1],
"genererebb": [5],
"installano": [2],
"albero": [5],
"raggiunger": [2],
"can": [0,6],
"inclusion": [0],
"cat": [[0,6]],
"duser.countri": [2],
"provid": [[2,6]],
"sull\'uso": [2],
"readi": [2],
"match": [6,2],
"categori": [0],
"trovano": [6,1,[0,2],5,3],
"align.tmx": [2],
"posizion": [0,2,[3,6],1,[4,5]],
"file2": [2],
"orfani": [6,4],
"permettono": [[0,3,6]]
};
