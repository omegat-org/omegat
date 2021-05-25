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
 "Appendice A. Dizionari",
 "Appendice B. Glossari",
 "Appendice D. Espressioni regolari",
 "Appendice E. Personalizzazione delle scorciatoie",
 "Appendice C. Correttore ortografico",
 "Installazione e avvio di OmegaT",
 "Guide all&#39;uso...",
 "OmegaT 4.2- Manuale dell&#39;Utente",
 "Menu",
 "Pannelli",
 "Cartella del progetto",
 "Finestre e finestre di dialogo"
];
wh.search_wordMap= {
"quantificatori": [2,7],
"popolazion": [[8,11]],
"coerent": [8],
"destinata": [9],
"avanz": [11],
"correttore": [7],
"predefinit": [11,3,10,[2,5]],
"evidenzia": [8,3,[1,11]],
"info.plist": [5],
"archiviarla": [6],
"allineatore": [7],
"stabil": [6],
"ricercata": [11],
"l\'attual": [[6,8,9]],
"preconfigur": [11],
"fuzzi": [[9,11],10],
"passati": [[5,6]],
"apparirà": [[8,10,11]],
"solit": [8],
"estratto": [11],
"precisament": [11],
"estratti": [11],
"lavora": [[6,11]],
"dgoogle.api.key": [5],
"possano": [6],
"immesso": [11],
"edittagnextmissedmenuitem": [3],
"esclusion": [11],
"modificar": [11,5,[3,6,9],[1,4,8]],
"quiet": [5],
"immessa": [11],
"raggiungibili": [8],
"l\'esatta": [11],
"es_es.d": [4],
"aggiunger": [6,11,5,[1,3],9],
"gestor": [[4,6,11]],
"the": [5,[0,2,3]],
"lavoro": [[6,11],[5,9]],
"projectimportmenuitem": [3],
"frances": [11,5],
"chiamar": [3],
"imag": [5],
"prescelt": [5],
"lavori": [9],
"monolingu": [11],
"l\'indirizzo": [5],
"priorità": [11,8],
"applica": [10],
"nozioni": [6],
"manuale": [7],
"l\'ingles": [6],
"nell\'ultima": [8],
"l\'aggiunta": [[5,11]],
"moodlephp": [5],
"accessibil": [6],
"currsegment.getsrctext": [11],
"sotto": [5,6,11,9,[0,2,4]],
"gestir": [6,11],
"che": [11,6,5,8,10,9,4,2,[1,3],0],
"gestit": [6],
"check": [6],
"distribuita": [5],
"tengono": [9],
"manuali": [11,6],
"xxxx9xxxx9xxxxxxxx9xxx99xxxxx9xx9xxxxxxxxxx": [5],
"indicherà": [6],
"coerenti": [11],
"motivo": [[5,8,11]],
"minimizza": [9],
"disco": [[5,6,8]],
"orientati": [11],
"termina": [[5,6,8,9]],
"offr": [11],
"webster": [0,[7,9]],
"esist": [11,[1,6],5],
"useranno": [[6,11]],
"produttività": [11],
"termini": [[1,9],[3,11]],
"comunica": [9],
"multiplo": [11],
"cjk": [11],
"multiple": [7],
"rendersi": [11],
"tutt": [11,6,[5,8,10],[2,3,4,9]],
"l\'anno": [6],
"contrario": [[6,11]],
"l\'attività": [11],
"ragion": [4],
"empti": [5],
"un\'ampia": [4],
"valida": [[9,11]],
"installazion": [5,4,11,[7,8]],
"ottico": [6],
"validi": [11,5],
"dall\'interfaccia": [1],
"un\'ulterior": [11],
"rilevanti": [11],
"valido": [5,11],
"tmx": [6,10,5,11,[3,8,9]],
"repo_for_all_omegat_team_project": [6],
"avanti": [8,2,[0,1,4,5,6,7,9,10,11],3],
"chiuderl": [11],
"nl-en": [6],
"inter": [11],
"dettagli": [[5,11]],
"integ": [11],
"intel": [5,7],
"fr-ca": [11],
"mainmenushortcuts.properti": [3],
"gradualment": [6],
"vedano": [11],
"un\'analisi": [11],
"convertir": [11,6],
"cmd": [[6,11]],
"coach": [2],
"complessivi": [11],
"provenient": [10],
"propri": [11,5,9,6,2],
"gotohistorybackmenuitem": [3],
"luogo": [[9,10]],
"parametro": [5],
"sarà": [11,8,[5,6],1,9],
"ricevut": [9],
"effettua": [11],
"sperimentano": [[6,11]],
"allineamento": [11],
"ciò": [[5,11],6,4],
"parametri": [6,11,5,10],
"project-save.tmx": [6],
"resteranno": [10],
"powerpc": [5],
"procurarsi": [5],
"propost": [8],
"opzion": [11,5,8,6],
"esporta": [11,8,[3,6]],
"elencati": [8,3],
"adottar": [[6,11]],
"sfondo": [[8,10],9],
"l\'autor": [[8,9]],
"qual": [5,11,8],
"anch": [11,5,6,9,8,4,1],
"col": [11,[2,9],5,[1,6,8],[3,4,10]],
"instal": [5,4],
"con": [11,5,6,8,9,1,3,0,10,4,2,7],
"minor": [2],
"elenco": [11,[4,5]],
"propagazion": [11],
"esadecimal": [2],
"remot": [11],
"lett": [5],
"avrà": [11,5],
"elenca": [4],
"omegat.sourceforge.io": [5],
"ordin": [11],
"pipe": [11],
"fondamentalment": [4],
"dell\'uscita": [11],
"tra": [11,10,[2,6,8],[0,1,9]],
"visualizzar": [11,[5,6],8,9],
"sull\'argomento": [10,11],
"all\'estension": [11],
"tre": [6,10,[0,9],[1,8]],
"piattaforma": [[5,11]],
"scompariranno": [4],
"sposti": [5],
"translat": [11,5,[4,6,8]],
"esportazion": [6,11],
"successiva": [8,3,[5,9,11]],
"sposta": [8,11,9],
"successivo": [8,11,3,[2,6,9]],
"successivi": [[5,11]],
"debolment": [11],
"chiudersi": [11],
"sull\'icona": [5],
"includendo": [6],
"correttament": [6,[1,5],[8,10]],
"docs_devel": [5],
"l\'origin": [9],
"tsv": [1],
"intendono": [11],
"archivio": [0],
"gnome": [5],
"probabil": [[5,6,9]],
"totali": [8],
"commento": [[1,3,5,8]],
"costrutto": [2],
"blocco": [2,9,[5,11]],
"corretta": [[1,5],[0,4]],
"corretto": [5,4],
"sincronizz": [6],
"corretti": [[6,11]],
"avanza": [8,[3,11]],
"commenti": [11,9,[1,7]],
"quei": [11],
"quel": [[6,11]],
"dipenderà": [6],
"csv": [1,5],
"stata": [8,6,[4,5,11]],
"n.n_linux.tar.bz2": [5],
"senza": [11,5,[6,9]],
"apparir": [[3,4,5]],
"stato": [[8,9],[6,11],5,10,[1,7]],
"seguir": [6,5,4],
"seguit": [11],
"state": [5],
"dock": [5],
"stati": [[8,11],9,[1,3,6,10]],
"segmentazion": [11,6,[2,8],[3,10]],
"dmicrosoft.api.client_secret": [5],
"secondaria": [[10,11]],
"importanti": [[5,6,11]],
"cui": [11,6,5,8,4,[2,9]],
"fisicament": [4],
"creativ": [11],
"utilizzando": [11,[5,6],[0,4,9,10]],
"ctrl": [3,11,9,6,8,1,[0,10]],
"logico": [11],
"document": [[5,11]],
"caric": [8],
"multinazional": [6],
"documentazion": [3,[2,11]],
"logici": [[2,7]],
"decommentar": [5],
"resourc": [5,11],
"preso": [2],
"ricopi": [5],
"richiesta": [5],
"team": [6],
"xx_yy": [[6,11]],
"docx": [11,[6,8]],
"txt": [6,1,[9,11]],
"prestar": [[5,6]],
"aggiornar": [11],
"possibilità": [11,[5,6]],
"l\'editor": [11,5],
"comunità": [6],
"offerto": [9],
"ordinar": [11],
"richiesto": [5,8],
"prendendo": [11],
"offerti": [8],
"caratteri": [11,8,[1,3],[2,5,6],9],
"definit": [11],
"anno": [6],
"definir": [11,[2,9]],
"carattere": [7],
"source": [7],
"attiva": [[8,11]],
"valgono": [11],
"rilevato": [2,6],
"trnsl": [5],
"viewdisplaymodificationinfoselectedradiobuttonmenuitem": [3],
"index.html": [5],
"configurati": [6],
"omegat.tmx": [6],
"prefisso": [11,10],
"configurato": [6],
"esposti": [5],
"ultimato": [11],
"diffrevers": [11],
"attivo": [8,9,11,10,[1,3]],
"fornitor": [11],
"l\'inizio": [2],
"giappones": [11,5],
"comandi": [11,5,8],
"ospitati": [11],
"principio": [10,6],
"project.gettranslationinfo": [11],
"comando": [5,11,[6,8],9,10,[1,4,7]],
"decompressa": [5],
"spostamento": [8],
"installazione": [7],
"n.b": [11],
"funzion": [11,8,4,[0,1,9]],
"diretto": [5],
"start": [5,7],
"equal": [5],
"esatt": [8,[1,11]],
"recupera": [11],
"diretta": [8],
"libero": [8],
"nascosti": [[10,11]],
"optionsalwaysconfirmquitcheckboxmenuitem": [3],
"prender": [11],
"reinserir": [11],
"renderla": [[10,11]],
"cartacei": [9],
"riconoscimento": [[1,11],[3,6]],
"dipendent": [1],
"applic": [11],
"avvisati": [11],
"projectteamnewmenuitem": [3],
"distribuir": [[5,6]],
"scorciatoi": [3,[2,5,11]],
"mantenendo": [10],
"directorate-gener": [8],
"applicazion": [[4,6]],
"sessioni": [5],
"tecnologia": [5],
"memori": [6,10,11,5,8,9],
"produrr": [6],
"realment": [6,8],
"incolla": [9],
"aspetto": [11],
"succeder": [10],
"selezionato": [8,11,[3,4,5]],
"aggirar": [6],
"eliminando": [10],
"computer": [7],
"omegat.jnlp": [5],
"istruzioni": [5,11,[4,6,7]],
"basandosi": [4],
"centro": [2],
"comporta": [11],
"globalment": [11],
"interessata": [11],
"n.n_windows_without_jre.ex": [5],
"incollarlo": [8],
"dell\'error": [5],
"clic": [11,5,[8,9],[1,4,6]],
"completament": [6,11],
"selezionata": [8,11,[5,9],[3,6]],
"dmicrosoft.api.client_id": [5],
"ricever": [5],
"prevalent": [11],
"selezionati": [11,[6,8],4],
"config-fil": [5],
"nell\'interfaccia": [6],
"dag": [[6,9,11]],
"premendo": [11,[4,6,8,9]],
"dai": [[5,6,11],7],
"dal": [6,11,[5,8],[1,9,10],[0,2,3]],
"alterata": [11],
"compres": [11],
"selezionarla": [6],
"dell\'impostazion": [9],
"system-user-nam": [11],
"format": [11],
"pausa": [4],
"trascinar": [5,9],
"console.println": [11],
"cambiat": [5],
"larghezza": [11],
"cambiar": [11,5,[4,6,9]],
"corso": [11,9],
"part": [9,11,5,8,[3,4,10]],
"autonom": [5],
"uscir": [[6,8]],
"principal": [9,11,6,3,10],
"contrassegno": [8],
"browser": [[5,8]],
"convertito": [6],
"chiedendo": [6],
"fuzzy": [7],
"project_files_show_on_load": [11],
"dopo": [11,[1,5,8],6,[2,9]],
"nello": [11],
"allineator": [11],
"protezion": [10],
"intorno": [11],
"ltr": [6],
"optionsexttmxmenuitem": [3],
"contrassegni": [8],
"nelle": [7],
"tema": [11],
"saranno": [11,8,[5,6],[1,10],9,3],
"build": [5],
"nella": [11,5,6,8,9,10,1,3,4,0],
"temi": [11],
"marketplac": [5],
"possibil": [11,5,6,9,[1,3,4,8,10],0],
"commutar": [6],
"entries.s": [11],
"deg": [11,[6,8],3],
"dei": [11,6,8,5,4,1,[9,10],7,3,2],
"del": [11,6,9,5,8,10,1,[3,4],7,0],
"gotonextuntranslatedmenuitem": [3],
"rispecchia": [8],
"targetlocal": [11],
"altra": [[4,5,6]],
"path": [5],
"ritien": [[6,11]],
"padroneggia": [6],
"interferir": [8],
"rilasciata": [8],
"relativi": [[9,11]],
"all\'apertura": [[6,11]],
"relativo": [6,[5,9,11]],
"allsegments.tmx": [5],
"altri": [6,11,[5,10],[1,9],[0,7,8]],
"impostazione": [7],
"rilasciati": [9],
"rivolg": [2],
"rilasciato": [[3,9]],
"impostazioni": [5,11,8,[4,6,10],7],
"altro": [9,[8,11]],
"equivalenti": [[8,9]],
"helpcontentsmenuitem": [3],
"omegat-org": [6],
"evidenziar": [9],
"relativa": [6],
"descript": [3],
"rinomina": [11],
"projectaccessdictionarymenuitem": [3],
"eseguit": [[6,11]],
"acceder": [11,[5,6,9]],
"all\'utent": [[5,8,9,11]],
"metacaratteri": [2],
"seguito": [11,[2,3,5],[0,6]],
"ricaricar": [11],
"dove": [9,[3,5],[6,11]],
"legislazion": [6],
"davanti": [11],
"eseguir": [11,5,[4,10],[2,8]],
"tradizionali": [5],
"dott": [11],
"rilasciare": [7],
"selezionarlo": [5],
"duden": [9],
"un\'espression": [11],
"seguiti": [2],
"operazioni": [[2,5,9]],
"spotlight": [5],
"did": [11],
"seguita": [11],
"tenterà": [[5,11]],
"dir": [5],
"impartir": [4],
"l\'apertura": [8],
"inizianti": [2],
"div": [11],
"esempi": [2,11,[6,7],5],
"considerati": [11],
"istanz": [5,8],
"alfabeticament": [11],
"considerata": [11],
"viewfilelistmenuitem": [3],
"lasciat": [10],
"lasciar": [11],
"adottata": [11],
"test": [5],
"originali": [8],
"raccolt": [11,9],
"allinear": [11,8],
"omegat": [5,6,11,8,10,[3,7],4,1,9,0,2],
"all\'interno": [11,6,9,8,[1,10],5,[3,4]],
"questi": [6,11,[4,5,10]],
"comportano": [11],
"assistita": [6,7],
"final": [[6,11]],
"questo": [11,5,8,6,9,10,[2,4,7]],
"configurazione": [7],
"influenza": [6],
"virtual": [11],
"messaggio": [[5,6]],
"rimuov": [11],
"ignora": [11],
"console-align": [5],
"questa": [11,10,8,5,[4,9],6,[1,2]],
"ms-dos": [5],
"disegni": [[6,11]],
"projectopenrecentmenuitem": [3],
"dipend": [8,[1,6,11]],
"dell\'intervallo": [[6,8]],
"attivata": [11,8,[4,9]],
"attivato": [8],
"legat": [6],
"inser": [11],
"aggiunti": [[5,6,8,10]],
"all\'altro": [8,11],
"lettera": [2,8,[3,6,11]],
"una": [11,6,8,5,2,10,4,3,9,1,0],
"ognuno": [1],
"und": [4],
"riscrivi": [11],
"considerato": [[9,10]],
"appena": [8,6],
"trattato": [[6,11]],
"uno": [11,5,2,8,9,[1,4,6],10],
"editoverwritemachinetranslationmenuitem": [3],
"relat": [[6,11],[5,9,10]],
"aggiunta": [[5,6],[0,8,9,10,11]],
"sfoglia": [5,11],
"trattati": [[6,11]],
"ingreek": [2],
"disattiva": [11],
"portoghes": [[4,5]],
"eccetto": [2],
"es_es.aff": [4],
"convert": [11,8],
"disattivo": [[8,9]],
"projectexitmenuitem": [3],
"ricercar": [[2,5,8,11]],
"contenut": [[10,11]],
"text": [5],
"editregisteruntranslatedmenuitem": [3],
"all\'indirizzo": [5],
"init": [6],
"preme": [11],
"punto": [11,2,[6,9],[5,10],[4,8]],
"scritta": [9],
"disponibil": [5,[4,6,11]],
"maco": [5,1],
"attraverso": [[2,5]],
"perder": [6],
"doc": [6],
"senso": [[4,10]],
"particolari": [11,4],
"server": [6,11,5,10],
"un\'altra": [[5,9],[2,6,11]],
"paramet": [5],
"siano": [11,6,[5,9,10],[0,4]],
"piacimento": [11],
"incluso": [11,9,[6,8]],
"mac": [3,[2,5,6]],
"bisogna": [6],
"leggi": [11],
"mantieni": [11],
"punta": [6],
"inizial": [5],
"varranno": [11],
"svuotato": [11],
"affatto": [11],
"man": [5],
"map": [6],
"punti": [6],
"may": [11],
"url": [6,[4,11]],
"desideri": [[10,11]],
"megabyt": [5],
"uppercasemenuitem": [3],
"calcolata": [11,9],
"viewmarkuntranslatedsegmentscheckboxmenuitem": [3],
"desidera": [11,[6,8],[2,9]],
"bisogno": [6],
"flessibilità": [11],
"usa": [11,3,[1,8],[4,5,6,9]],
"lungo": [11],
"use": [5],
"usi": [5],
"reperir": [[6,11]],
"operi": [11],
"dell\'interfaccia": [11],
"scarica": [[3,6,8]],
"lungh": [11],
"uso": [[7,11],6,[4,5],1,0],
"lavorando": [11],
"omegat.jar": [5,[6,11]],
"omegat.app": [5],
"usr": [5],
"assicurarsi": [5,[4,6]],
"creazion": [11,6],
"iniziar": [11,3],
"lista": [7],
"utf": [1],
"ogni": [11,6,9,8],
"deposito": [6,[8,11],5],
"ferma": [11],
"includer": [11,6],
"veder": [[2,6],[4,5,8,10,11]],
"feed": [2],
"inclusi": [[2,6]],
"lunga": [[5,11]],
"esci": [[3,8,11]],
"aggiungendo": [10],
"dsl": [0],
"servir": [6],
"tabulazion": [11,[1,2]],
"strumenti": [[6,11],[3,7],[2,8,10],9],
"vecchio": [6],
"vecchia": [5,11],
"n.n_windows_without_jre.zip": [5],
"med": [8],
"strumento": [11,[2,5,7,8]],
"en.wikipedia.org": [9],
"salvataggio": [6,11,[1,3,8]],
"dtd": [5],
"abilitato": [11],
"un\'installazion": [5],
"nuov": [[1,3,8],11],
"regolar": [11,2],
"make": [11],
"fanno": [[4,8,10]],
"sovrascritta": [5],
"localizzato": [5],
"comprimi": [11],
"projectcompilemenuitem": [3],
"console-transl": [5],
"banal": [6],
"sovrascritto": [11],
"entrambi": [6,11],
"gotonextuniquemenuitem": [3],
"due": [11,5,6,[4,8],9],
"messaggi": [[5,6,9]],
"conform": [11],
"wordart": [11],
"optionsviewoptionsmenuitem": [3],
"ignorerà": [11],
"attiv": [11],
"commit": [6],
"targetlocalelcid": [11],
"project_stats_match.txt": [10],
"freccia": [11,9],
"dvd": [6],
"xmx2048m": [5],
"revisor": [11],
"xxxxxxxxxxxxxxxx.xxxxxxxxxxxxxxxx.xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx": [5],
"miglioramenti": [8],
"consiglia": [11],
"esigenz": [[6,11]],
"esattament": [11],
"rallentar": [6],
"glifi": [8],
"punteggio": [11],
"krunner": [5],
"libreoffic": [4,[6,11]],
"modulo": [5],
"male": [11],
"opzioni": [11,8,[3,5,9],[4,7],6,[2,10]],
"eliminato": [11],
"pacchetto": [5,8,7],
"entramb": [5],
"protetti": [10],
"eliminati": [[6,11]],
"devono": [11,6,5,4,[0,1,2,3,9]],
"mio": [6],
"rifletter": [8],
"componenti": [11],
"defin": [[8,10]],
"supporto": [5],
"ottengono": [6],
"l\'analisi": [9],
"conterrebb": [11],
"viewdisplaysegmentsourcecheckboxmenuitem": [3],
"editregisteremptymenuitem": [3],
"appaiono": [11,3,9],
"dedicata": [6],
"aggiungi": [11,[5,8,9]],
"dedicati": [2],
"open": [11,6],
"www.oracle.com": [5],
"predefinite": [7],
"project": [[5,11]],
"predefinito": [11,1,[6,8],[5,7,9]],
"xmx1024m": [5],
"visualizzerà": [5],
"verificarn": [0],
"predefinita": [11,3,[6,8],9,[1,5],[2,10]],
"penalty-xxx": [10],
"gotonextsegmentmenuitem": [3],
"copiarlo": [6],
"finestra": [11,9,8,5,4,6,10,[1,3,7]],
"nnn.nnn.nnn.nnn": [5],
"finestre": [7],
"supporta": [[6,11]],
"perdit": [6],
"esistent": [11,[5,10],[1,6,8]],
"abort": [5],
"entrano": [3],
"l\'olandes": [6],
"internet": [11,4],
"descrizion": [11,[3,5,6]],
"valori": [[1,11],5],
"saltar": [8],
"nell\'editor": [11,[5,8]],
"sceg": [4],
"affidabili": [11,10],
"linguaggi": [11],
"verificato": [6],
"segnalerà": [[6,11]],
"uscita": [6],
"rilasciando": [5],
"controllar": [6,[5,11]],
"invitar": [6],
"basa": [11,4],
"assegnar": [11],
"inserirla": [9],
"percorso": [5,[6,8]],
"es-mx": [4],
"registro": [8,3],
"layout": [11],
"sincronizzato": [6],
"ridotta": [10],
"registra": [[3,8]],
"sincronizzati": [6,11],
"manualment": [11,6,[1,4,8]],
"base": [11,5,[6,8,9,10]],
"stem": [9],
"registr": [5],
"riserva": [5],
"esportarlo": [6],
"collegata": [10],
"allin": [11],
"automatica": [11,8,9,[3,4,7,10]],
"successivament": [5],
"facendo": [11,5,[6,8]],
"automatich": [11,8],
"vai": [[3,7,9],[8,11]],
"automatico": [11,3,6,8,[1,5]],
"notifich": [11],
"indica": [11,[2,9]],
"inserit": [[8,10,11]],
"inserir": [11,10,[5,8],9],
"indice": [7],
"esplicitament": [11],
"stile": [6,11],
"bidirezional": [6,[3,8]],
"stili": [[6,11]],
"risultano": [9],
"terminato": [9],
"loro": [11,6,9,[8,10]],
"attribuzion": [5],
"risultant": [11],
"permett": [11,8,5],
"indici": [11],
"quotidiano": [5],
"word": [6,11],
"desiderino": [6],
"lingue": [7],
"sottolinear": [1],
"spiegato": [11],
"lingua": [5,4,[6,11],9,[0,10]],
"limitazione": [7],
"utent": [5,11,[1,6]],
"limitazioni": [11],
"essa": [[6,10]],
"elettronici": [9],
"contenga": [8],
"europe": [11],
"duemila": [11],
"accettato": [5],
"includi": [11],
"contener": [[5,11],[6,9],[1,3,4,10]],
"sezioni": [6],
"lingvo": [0],
"includa": [5],
"variazion": [11],
"analizzati": [6],
"espander": [11],
"mrs": [11],
"istruir": [4],
"accettata": [10],
"coinvolti": [4],
"inserendo": [[3,8]],
"forzati": [10],
"specifico": [11,[1,4,5,6,8,9]],
"ecc": [11,[2,6,9]],
"specifich": [11,10,[2,9]],
"specifici": [11,[6,10],9],
"alleg": [0],
"specifica": [11,[5,6,8]],
"pt_pt.aff": [4],
"sufficienti": [11],
"appar": [11,5],
"html": [11,5],
"ammess": [2],
"esso": [11,[5,6],[1,7]],
"leggi.mi": [11],
"essi": [6,11,[5,8,9,10],[1,2,4]],
"artund": [4],
"revision": [[6,10]],
"nome_fil": [11],
"sulla": [11,[3,5],9,8,6],
"sposterà": [11],
"qualità": [6,[8,10]],
"alcun": [8,[5,9,11],[1,4,6]],
"l\'attributo": [11],
"avanzati": [[2,5]],
"minimo": [11],
"risultati": [11,[2,8],6],
"sullo": [6],
"www.ibm.com": [5],
"l\'elemento": [8],
"risultato": [11,6,[3,8]],
"risultass": [5],
"controllo": [6,11,[2,8],3],
"regolarment": [6],
"controlla": [4,[8,11]],
"agio": [5],
"controlli": [4],
"migliori": [11],
"command": [[3,9],5],
"consigli": [7],
"n.n_without_jr": [5],
"tipico": [6],
"convalidar": [6],
"miglior": [11,10],
"selezionabil": [5],
"quindi": [[5,9,11]],
"documento.xx.docx": [11],
"considerazion": [11],
"volut": [8],
"incollato": [8],
"viewmarkbidicheckboxmenuitem": [3],
"notar": [5,[6,10]],
"registrerà": [5],
"vorrebb": [11],
"dell\'oggetto": [11],
"contenent": [5,11,[4,6,9]],
"informazioni": [5,11,6,[3,8],[9,10],[0,2]],
"via": [11,5,[0,6,9,10]],
"l\'alternativa": [[8,11]],
"variabili": [11],
"volum": [11],
"combinando": [11],
"sinistro": [11],
"verificar": [6,[0,11],[4,9,10]],
"sinistra": [6,11,[7,8,9]],
"sotto-cartella": [10,6,5,[4,11]],
"version": [5,6,8,[4,10]],
"bozza": [11],
"volta": [11,6,5,2,[3,8]],
"espression": [[2,11]],
"avanzamento": [2],
"chiederà": [[5,11]],
"esportata": [11],
"vista": [[3,11],7,8,[1,9]],
"privata": [5],
"immess": [11],
"appropriati": [6],
"projecteditmenuitem": [3],
"collaborazion": [6,8,11,[3,5,10]],
"chiama": [11],
"appropriata": [6],
"britannica": [0],
"tradurlo": [6],
"configurar": [11],
"d\'una": [10],
"riconoscer": [11],
"modificato": [[5,6,8],[3,9,11]],
"appropriato": [[6,11]],
"wikipedia": [8],
"colleghi": [9],
"machin": [11],
"commutazion": [6],
"schermata": [9],
"apertura": [5],
"iceni": [6],
"dell\'avanzamento": [9],
"farci": [5],
"visualizzazion": [6,11,8],
"ripreso": [3],
"all\'url": [6],
"testi": [11],
"dovrà": [11,5,[9,10]],
"testo": [11,6,8,9,10,[1,3],7,[2,4,5]],
"specif": [11],
"l\'applicazion": [5],
"vantaggio": [5],
"dsun.java2d.noddraw": [5],
"chiamiamo": [11],
"categorico": [10],
"cambiata": [6],
"scorciatoia": [3,8,[6,11]],
"liberament": [0],
"sezion": [5],
"all\'uso": [6,[0,4,7,10]],
"riconosciuti": [[1,11]],
"scorciatoie": [7],
"x0b": [2],
"specificherà": [5],
"contrassegnar": [8],
"canada": [11,5],
"altern": [11,9,8],
"http": [6,5,11],
"decimal": [11],
"numerico": [2],
"riconosciuta": [11],
"nazion": [5,11],
"carrello": [2],
"significa": [6],
"rimuovendo": [5,[9,11]],
"linea": [6,[4,5]],
"dall\'utent": [11,[3,10]],
"vuot": [11,3],
"userà": [[4,5]],
"softwar": [[5,11]],
"projectsinglecompilemenuitem": [3],
"supportano": [6],
"leggono": [5],
"intasar": [11],
"progressivo": [11],
"vuol": [[3,6,11],[5,9]],
"prescelta": [5],
"fornit": [6],
"comuni": [1,[6,7]],
"special": [6,11],
"fornir": [11],
"project_save.tmx.annommgghhnn.bak": [6],
"utilità": [[5,11]],
"contempo": [10],
"diciamo": [6],
"nell\'esempio": [9,[4,6,11]],
"conosc": [6],
"copyright": [8],
"multi-piattaforma": [5],
"contenenti": [11,[6,8]],
"messicano": [4],
"trattino": [5],
"marchi": [11,9],
"controllerà": [11],
"system-os-nam": [11],
"optionstabadvancecheckboxmenuitem": [3],
"riconvert": [6],
"modificano": [11],
"traduzion": [6,11,8,9,10,3,5,2],
"heapwis": [11],
"optionsviewoptionsmenuloginitem": [3],
"servendosi": [11],
"dell\'angolo": [9],
"rifletterà": [10],
"lingu": [11,6,[4,8]],
"tar.bz2": [0],
"invio": [11,[3,8],[5,6]],
"bundle.properti": [6],
"invia": [6,8],
"coreani": [11],
"x64": [5],
"formattati": [6],
"keyev": [3],
"nell": [11,[1,2,3,6,8,9],[5,10]],
"parola": [11,8,[2,4],[5,9]],
"isn\'t": [2],
"cell": [11],
"esserci": [11],
"valid": [5],
"insiem": [[6,11]],
"velocizzar": [6],
"era": [6],
"immission": [6,11],
"eccezion": [11,6],
"utilizzerà": [5],
"riga": [5,11,2,6,[3,8,10],[1,7,9]],
"optionsteammenuitem": [3],
"chiavi": [11,5],
"gzip": [10],
"righ": [11,3],
"esc": [[8,11]],
"distribuzion": [5],
"x86": [5],
"nostemscor": [11],
"ess": [[3,11],10],
"traducibili": [11],
"scaricamento": [5],
"console-createpseudotranslatetmx": [5],
"ridefinir": [11],
"neg": [[6,8]],
"nei": [11,6,5,[8,9]],
"nel": [11,6,5,8,9,1,[2,3],[4,10]],
"suffisso": [5],
"longman": [0],
"fuzzyflag": [11],
"modificarn": [11],
"merriam": [0,[7,9]],
"escap": [5,[1,2]],
"comportar": [9],
"modificati": [11,[6,8],3],
"rappresenta": [5,9],
"ecco": [[8,11]],
"visualizz": [11,[8,10],5],
"projectname-omegat.tmx": [6],
"allineerà": [5],
"modificata": [[6,9,11]],
"cascata": [11],
"invec": [11],
"verranno": [11,[5,10],[4,6,8,9]],
"avvisi": [5],
"errato": [11],
"andar": [11],
"premer": [11,[5,8,9],[1,4,6]],
"forma": [11,[3,5,6,8,10]],
"n.n_without_jre.zip": [5],
"probabilment": [11],
"render": [11,6],
"medio": [11],
"magento": [5],
"crediti": [8],
"chiaro": [8],
"decider": [11,10],
"particolar": [[5,6,11]],
"rilascio": [8],
"correttor": [4,11,10,[1,2]],
"memorizz": [11],
"citazione": [7],
"u00a": [11],
"non-parola": [2],
"poterla": [9],
"molteplici": [10],
"shift": [3],
"cert": [6],
"autenticato": [11],
"java": [5,11,3,2,[6,7]],
"pieno": [6],
"lang2": [6],
"lang1": [6],
"project_save.tmx": [6,10,11],
"dictionari": [0,10],
"mantenuta": [[10,11]],
"rimuover": [11,6,10],
"applicazioni": [5,[4,6,11]],
"quattro": [8],
"dictionary": [7],
"indipendentement": [11],
"dell\'area": [11],
"numero": [11,9,8,1,5,[2,3,10]],
"all\'opzion": [[8,11]],
"ridimensionar": [9],
"numeri": [11,9,6],
"l\'avvio": [5],
"default": [3],
"alterna": [3],
"qualsiasi": [11,[9,10],[2,8],[5,6],[1,4],3],
"timestamp": [11],
"perso": [6],
"attributo": [11],
"sottotitoli": [5],
"projectaccessrootmenuitem": [3],
"mappato": [6],
"dyandex.api.key": [5],
"digitata": [[8,11]],
"correntement": [8],
"attributi": [11],
"mappati": [6],
"digitato": [[3,8]],
"correnti": [[3,8]],
"davvero": [8],
"fortunati": [11],
"voler": [[6,11]],
"plugin": [11],
"sospeso": [11],
"effetto": [[9,11]],
"incorporato": [11,4],
"pensata": [6],
"editinsertsourcemenuitem": [3],
"documento": [11,[6,8],3,[1,7,9]],
"apri": [[3,8]],
"appaiano": [6],
"viterbi": [11],
"microsoft": [11,[5,6],9],
"provenienti": [11,9],
"operator": [11],
"projectnewmenuitem": [3],
"assistenza": [6],
"incorporati": [11],
"pulsant": [11,5,9,4,[1,8]],
"documenti": [11,6,8,3,[5,9,10]],
"optionstranstipsenablemenuitem": [3],
"un\'abbreviazion": [11],
"ignorando": [[8,9]],
"estrarr": [[0,11]],
"glossari": [1,11,10,6,[3,7,9],[0,4]],
"ignored_words.txt": [10],
"verificarsi": [6],
"effettuar": [6],
"accanto": [3],
"impost": [6,5],
"configuration.properti": [5],
"github.com": [6],
"sostitu": [6],
"duplicati": [11],
"glossary": [7],
"contrassegnati": [10],
"segmentato": [11],
"incorporata": [[10,11]],
"segmentati": [11],
"aiuterà": [6],
"relazion": [9],
"string": [5],
"import": [[6,9]],
"color": [11,10],
"all\'avvio": [5],
"primaria": [5],
"non": [11,6,8,5,9,1,2,[3,4,10],[0,7]],
"dizionario": [4,11,0,8,[7,9]],
"tastiera": [11,[3,9]],
"not": [11,5],
"eseguono": [2],
"scritti": [11,8],
"primario": [11],
"applicabili": [5],
"scritto": [11],
"nessuna": [8,2,[3,11]],
"risp": [9],
"dall\'estension": [1],
"separatament": [[3,11]],
"was": [11],
"invertit": [11],
"rispecchi": [5],
"selection.txt": [11,8],
"xhtml": [11],
"hanno": [11,[0,2]],
"preferibil": [11],
"finder.xml": [11],
"selezionar": [11,5,[8,9],4,[1,6,10]],
"sequenza": [11],
"occupano": [[6,11]],
"window": [5,[0,2,8]],
"classi": [[2,7]],
"spuntar": [[4,11]],
"gestito": [6],
"decida": [11],
"disable-project-lock": [5],
"l\'icona": [5],
"omegat.pref": [11],
"farlo": [5],
"terminar": [11],
"carriage-return": [2],
"rigida": [6],
"far": [10],
"presentar": [6],
"ambivalent": [11],
"multipl": [9],
"rigido": [[5,6,8]],
"diventi": [11],
"greco": [2],
"howto": [6],
"diventa": [10],
"pt_pt.dic": [4],
"lavorar": [[6,11],9],
"otterranno": [6],
"vocali": [2],
"traduttori": [6],
"criterio": [11],
"italiana": [5],
"italiano": [5],
"disposizion": [9,[3,6,8]],
"iimportati": [6],
"avviator": [5],
"level1": [6],
"italiani": [5],
"level2": [6],
"ripristin": [6],
"automaticament": [11,8,5,[3,4],[1,6,9]],
"widget": [[7,9]],
"statistich": [8,[3,10],6,11],
"determinar": [4],
"navigazion": [11,5],
"modelli": [11,6],
"restar": [11],
"origine-destinazion": [0],
"restituiranno": [11],
"modello": [11,2],
"riportar": [11],
"individuerà": [11],
"sull": [11],
"web": [5,6,[7,10]],
"en-us_de_project": [6],
"proprietà": [11,[6,8],4,[0,1,3,7,10]],
"veda": [11,[5,6,9,10],[2,8]],
"editselectfuzzy4menuitem": [3],
"editregisteridenticalmenuitem": [3],
"evidenziata": [9,4],
"usat": [6],
"traducono": [[6,11]],
"aperta": [11,8],
"disattivando": [11],
"condizioni": [6],
"sorger": [6],
"aperti": [[6,8,9]],
"troppo": [11],
"personalizzazion": [3,[2,11]],
"troppi": [11],
"evidenziato": [11],
"aperto": [8,9,11,6,5],
"usar": [5,11,6,4,[2,8],[0,3,9,10]],
"caricherà": [[6,11]],
"pt_br.dic": [4],
"intestazion": [11],
"evidenziati": [8,11],
"indicator": [9],
"compongono": [11],
"unabridg": [0],
"dice": [5],
"suoi": [11],
"ovvero": [[9,11]],
"situazioni": [11],
"l\'elaborazion": [[9,11]],
"identificano": [11],
"optionsglossaryexactmatchcheckboxmenuitem": [3],
"dispon": [4],
"datata": [6],
"traducendo": [[9,10,11]],
"mostrerà": [11],
"datati": [11],
"vocabolari": [[4,9]],
"nnnn": [9,5],
"prioritario": [10],
"esportati": [11],
"confermarlo": [5],
"riprodurr": [6],
"contenuto": [11,3,8,[6,10],[5,9],0],
"conoscenza": [6],
"esportato": [8],
"dicitura": [9],
"piattaform": [5],
"visibil": [11,10],
"contenuti": [6,11,[1,5],[9,10]],
"zh_cn.tmx": [6],
"comunqu": [[5,11],[4,6]],
"deselezionar": [11],
"analizza": [[2,11]],
"contenuta": [10,6],
"oppur": [11,[2,4,5,9],6,[1,8]],
"gestisc": [11,6],
"occorrenz": [11,[4,9]],
"aggiornamento": [5,11],
"terminal": [5],
"critico": [11],
"processo": [11,6],
"archiv": [5],
"condizion": [11],
"gestiti": [11],
"aggiornamenti": [11,[5,8]],
"d\'uso": [11],
"repo_for_omegat_team_project.git": [6],
"user": [[5,9]],
"sostituisci": [11,[3,8]],
"proxi": [5,11,3],
"extens": [11],
"definizion": [3,11],
"disattivata": [11,8],
"allinea": [8,11],
"all\'original": [[8,11]],
"risolti": [1],
"resterà": [11],
"richiamo": [[8,11]],
"esista": [1],
"riavviar": [[3,11]],
"ad": [11,6,4,10,[0,1,5]],
"sure": [11],
"ag": [11,[2,3,8]],
"un\'opinion": [9],
"presenza": [11],
"ai": [5,11,6,1,[0,4,9,10]],
"suddivisi": [11],
"diff": [11],
"al": [11,6,5,10,8,2,[3,9],[1,4]],
"esclusi": [6],
"suddiviso": [9],
"an": [2],
"editmultiplealtern": [3],
"permetter": [10],
"aprirà": [11],
"git.code.sf.net": [5],
"predefin": [11,5],
"indipendent": [2],
"intervenir": [[9,11]],
"prerequis": [5],
"eseguibil": [5],
"be": [11],
"concedono": [5],
"importar": [6,[10,11]],
"salta": [11],
"elaborar": [[5,6]],
"filters.xml": [6,[10,11]],
"br": [11,5],
"l\'url": [[5,6,8,11]],
"necessita": [5],
"segmentation.conf": [6,[5,10,11]],
"combinazion": [11,9,8,[0,2,3,5]],
"file_scaricato.tar.gz": [5],
"ca": [5],
"cd": [5,6],
"ce": [5],
"öäüqwß": [11],
"ci": [5,[0,9],[4,6,8]],
"spazio": [11,2,[1,6]],
"cn": [5],
"familiar": [11,6],
"compiuta": [11],
"leggimi.txt": [11],
"cr": [2],
"indietro": [8,3,[0,1,2,4,5,6,9,10,11]],
"compiuto": [11],
"l\'uso": [11,[5,6]],
"ella": [6],
"memorizza": [[6,11]],
"cx": [2],
"terminologia": [[6,8,11]],
"sull\'installazion": [5],
"apach": [4,[6,11]],
"da": [11,6,5,8,[9,10],2,4,1,[3,7]],
"adjustedscor": [11],
"intervien": [11],
"di": [11,6,5,8,9,10,1,3,[2,4],7,0],
"aprirn": [8],
"f1": [3],
"f2": [9,[5,11]],
"f3": [[3,8]],
"bidirezionali": [8],
"f5": [3],
"ragioni": [11],
"copiano": [[4,6]],
"rari": [11],
"dz": [0],
"editundomenuitem": [3],
"possibili": [5,6,2,[1,3]],
"attender": [5],
"ed": [[2,6],[8,11],[1,4,5,7,9,10]],
"assiem": [11,8],
"u000a": [2],
"olandes": [6],
"avviati": [8],
"en": [5],
"preceder": [11],
"dovess": [[4,9,11]],
"u000d": [2],
"es": [11,5,[4,6,10]],
"u000c": [2],
"eu": [8],
"giusti": [5],
"dall\'interno": [11],
"minuscol": [[2,11]],
"fa": [[5,11],[6,9]],
"operazion": [[4,10],[8,11]],
"avviato": [5,11,8],
"aiutar": [6],
"rilievo": [6],
"fog": [11],
"aggiuntivo": [5],
"u001b": [2],
"stats.txt": [10],
"indic": [11],
"aggiuntivi": [11,[2,5,6]],
"origin": [11,8,[9,10],[3,6]],
"installeranno": [5],
"for": [11,[3,8]],
"exclud": [6],
"rosso": [11,10],
"sbloccarla": [9],
"fr": [5,11],
"necessitano": [10,[5,6,11]],
"content": [5],
"desktop": [5],
"spiegazion": [5],
"appunti": [8],
"applescript": [5],
"rossi": [11],
"necessità": [11,[2,6]],
"anche": [7],
"gb": [5],
"ingegner": [6],
"class": [11,2],
"helplogmenuitem": [3],
"gg": [6],
"presenta": [[0,5,8]],
"ausiliari": [6],
"licenza": [[0,5,6,8]],
"editoverwritetranslationmenuitem": [3],
"digitar": [5,[6,11]],
"outputfilenam": [5],
"lingua-nazion": [11],
"presenti": [11,5,[1,6],8,[0,9]],
"dalla": [5,[6,10,11],9,8],
"aeiou": [2],
"form": [11,[2,10]],
"ha": [11,6,[4,5,8,9,10]],
"reagirà": [9],
"fors": [2],
"dà": [[5,8]],
"aiuto": [[3,7],8,6],
"hh": [6],
"duser.languag": [5],
"completo": [11,[5,6,9]],
"convalida": [11,5,[3,8],6],
"file-target-encod": [11],
"fra": [11],
"verd": [9,8],
"context": [9],
"funzionerà": [4,5],
"scrivani": [9],
"drag": [5,7],
"https": [6,5,[9,11]],
"coincider": [5],
"id": [11,6],
"if": [11],
"project_stats.txt": [11],
"ocr": [[6,11]],
"projectaccesscurrenttargetdocumentmenuitem": [3],
"l\'indicazion": [11],
"scorrer": [11],
"il": [11,6,5,8,9,1,2,4,3,10,0],
"in": [11,6,5,8,9,10,3,[2,4],1,0,7],
"termin": [1,[8,11]],
"ip": [5],
"is": [2],
"it": [5,[4,11]],
"codici": [4],
"nell\'attribuirn": [5],
"confrontar": [4],
"odf": [6,11],
"odg": [6],
"esser": [11,6,5,[3,10],[1,8],[0,4,9],2],
"ja": [5],
"glossario": [1,11,9,3,8,7,6],
"odt": [6,11],
"gotonexttranslatedmenuitem": [3],
"ridimensionamento": [11],
"nplural": [11],
"js": [11],
"confonder": [11],
"rileverà": [2],
"precedenza": [[6,8]],
"sufficientement": [[6,10,11]],
"learned_words.txt": [10],
"sguardo": [5],
"esatta": [11],
"codifica": [11,1,7],
"dunqu": [11,5,6,4],
"codifich": [11],
"ftl": [5],
"abilita": [11,8,[3,9]],
"ritornarci": [9],
"ftp": [11],
"proporrà": [11],
"possied": [5,[1,6,11],[3,4,10]],
"viewdisplaymodificationinfoallradiobuttonmenuitem": [3],
"draw": [6],
"compless": [2],
"completa": [[3,5]],
"visivament": [8],
"la": [11,5,8,6,9,10,3,4,1,2,0,7],
"le": [11,6,5,8,10,9,1,2,3,4,7,0],
"lf": [2],
"dswing.aatext": [5],
"li": [11,[6,8]],
"coincidono": [1],
"genera": [[6,11]],
"nell\'ambient": [11],
"lo": [11,8,6,5,10,[0,2,4,7,9]],
"terzo": [1],
"lu": [2],
"salva": [8,[5,11],[3,6]],
"genere": [[5,9,11],[2,6]],
"cycleswitchcasemenuitem": [3],
"ma": [6,11,[1,2],9,5,[4,8,10]],
"conclud": [2],
"mb": [5],
"sufficient": [11,[1,9]],
"limit": [2],
"me": [6],
"precedenti": [11,[5,9]],
"terza": [1],
"singolo": [11,[2,9]],
"omegat.png": [5],
"esistenti": [11,6,10],
"mm": [6],
"stringh": [6,0],
"entri": [11],
"mr": [11],
"stringa": [11,8,9],
"ms": [11],
"mt": [10],
"riporterebb": [11],
"my": [5],
"responsabil": [6],
"ne": [5,[8,9]],
"nl": [6],
"nn": [6],
"no": [11,5,[9,10]],
"code": [5],
"ottal": [2],
"gotohistoryforwardmenuitem": [3],
"richiedono": [[9,11]],
"l\'uscita": [[3,11]],
"l\'equivalent": [5],
"od": [6],
"of": [0],
"copiato": [8,[9,11]],
"ok": [[5,8]],
"copiati": [9,[6,11]],
"concordanza": [8,[3,11],9,10,1],
"os": [[6,11]],
"concordanze": [7],
"l\'azion": [11],
"singola": [11],
"editinserttranslationmenuitem": [3],
"compressi": [10],
"determina": [5],
"aggiornarla": [5],
"collegamento": [5],
"collegamenti": [5,11,[0,6]],
"nulla": [2,8],
"po": [11,9,5],
"preferenx": [8],
"optionsglossarystemmingcheckboxmenuitem": [3],
"pt": [5],
"inclus": [[9,11]],
"fornisc": [5,[4,11]],
"necessari": [5,6,[0,11]],
"venga": [6,[5,10,11]],
"ciano": [8],
"maiuscola": [2,[3,8],11],
"recent": [8,[3,5,6]],
"fornito": [[5,11]],
"chiuderla": [11],
"edit": [8],
"occorrenza": [11],
"forniti": [[0,5,11]],
"editselectfuzzy5menuitem": [3],
"bilingu": [[6,11]],
"trasformati": [11],
"maiuscolo": [[3,8]],
"espressioni": [[2,11],7,[3,4,5,9]],
"rc": [5],
"agevolment": [6],
"includ": [6,11,[2,9]],
"allora": [10,[0,6,11]],
"né": [5],
"preferenz": [8,11,5,[1,3,6]],
"memorizzata": [5,[6,9,11]],
"tedesca": [11],
"accesso": [11,5,[0,3,8]],
"sa": [10],
"sc": [2],
"navigar": [5,[4,9]],
"se": [11,8,5,6,10,9,4,3,0,[1,2]],
"segno": [1],
"segni": [3],
"si": [11,6,5,8,9,4,10,3,2,0,1],
"so": [5],
"chiuder": [6],
"su": [11,5,8,6,4,1,[3,9]],
"intero": [11],
"creato": [6,1,11],
"creati": [10,8,[1,6]],
"elaborazion": [11,[3,8]],
"lilla": [8],
"ripristinar": [[6,9]],
"interv": [[6,11]],
"editoverwritesourcemenuitem": [3],
"enforc": [10],
"intera": [11],
"remov": [5],
"gestisca": [11],
"tm": [10,6,11,8,[5,7,9]],
"to": [5,11],
"v2": [5],
"interi": [11],
"scrivibil": [3],
"solament": [6],
"separati": [[1,11],6],
"copiatura": [4],
"dialogo": [11,8,[4,6,10],[1,7,9]],
"tw": [5],
"trova": [5,11,6,[1,2,9]],
"separato": [6,11],
"viewmarkautopopulatedcheckboxmenuitem": [3],
"dover": [11],
"projectwikiimportmenuitem": [3],
"countri": [5],
"dall": [11,[3,5,6,8]],
"memorizzano": [6],
"singol": [[9,11]],
"quali": [11,6,[4,5,9]],
"un": [11,6,5,8,1,9,10,2,4,[3,7],0],
"facendovi": [[5,9,11]],
"ottener": [5,11],
"l\'unica": [8],
"usual": [11],
"modificabil": [1,8,6],
"l\'argomento": [5],
"traducibil": [11],
"caricamento": [11],
"this": [2],
"sembrar": [5],
"va": [11],
"semplicement": [6],
"impegnar": [11],
"iniziano": [5],
"vi": [[5,6],11],
"doppio-cl": [5],
"considerar": [6,11],
"separata": [1],
"support": [11],
"vs": [11],
"drop": [5,7],
"accertarsi": [[9,11]],
"certezza": [[5,11]],
"pure": [[3,11],[2,4,5]],
"dettag": [11,[6,8],5],
"restituendo": [2],
"ortografici": [4,7,[8,11]],
"ortografico": [4,11,7,10,[1,2,8]],
"sé": [6],
"groovy.codehaus.org": [11],
"partenza": [11,6,8,[3,9],1,5,[0,2,4,10]],
"sì": [5,11],
"repo_for_omegat_team_project": [6],
"sotto-cartell": [[10,11]],
"ora": [6,11],
"backspac": [11],
"avvenga": [[10,11]],
"ore": [6],
"emac": [5],
"org": [6],
"divisa": [11],
"d\'interruzion": [11],
"distribut": [5],
"ortografica": [4,3],
"xf": [5],
"superior": [9,11],
"segu": [3,[5,6,9,11]],
"iniziali": [11],
"xx": [5,11],
"xy": [2],
"sourc": [6,10,11,[5,8],9],
"passaggio": [6,11,10,9],
"indirizza": [5],
"aprir": [5,11,6,8,10,[3,9]],
"indirizzo": [5],
"inizialment": [11],
"sostituir": [[9,11]],
"type": [6],
"raffigurato": [11],
"supportati": [[5,6,8]],
"toolssinglevalidatetagsmenuitem": [3],
"nell\'installazion": [9],
"projectaccesssourcemenuitem": [3],
"simbolo": [2],
"yy": [9,11],
"sovrascriver": [10],
"sensibil": [11],
"citazion": [2],
"creano": [6],
"come": [11,6,5,9,[0,8,10],[2,3,4],7],
"nome": [11,[5,6],10,9,[0,1,8]],
"quant": [[6,8]],
"usarla": [[5,11]],
"nomi": [11,4,9,6],
"push": [6],
"zh": [6],
"scompattarlo": [0],
"installa": [4,5],
"readme_tr.txt": [6],
"penalti": [10],
"un\'intestazion": [8],
"prevenzione": [7],
"attualment": [8,11],
"utf8": [1,[8,11]],
"copi": [[6,10]],
"tanto": [4],
"illimitato": [5],
"out": [6],
"precisa": [1],
"accada": [11],
"packag": [5],
"power": [11],
"virgol": [11,1],
"dare": [5],
"operatori": [2,7],
"tag-valid": [5],
"scriver": [9,11],
"ufficiale": [7],
"lunghi": [11],
"spedir": [6],
"dovrebb": [11,6,5],
"giorno": [6],
"u0009": [2],
"xhh": [2],
"dell\'host": [5],
"revis": [[0,6]],
"u0007": [2],
"repositori": [6,10],
"data": [11],
"lascia": [11,10],
"lowercasemenuitem": [3],
"wiki": [[0,9]],
"tabell": [[3,6]],
"firefox": [[4,11]],
"vecchi": [11],
"separ": [[1,11]],
"circa": [6],
"corrisponderebbero": [11],
"commutando": [6],
"cosa": [10],
"dato": [11,5,[6,9,10]],
"nostro": [6],
"dati": [6,11,[5,7]],
"creata": [11,[4,5,9]],
"richiamar": [1],
"sens": [11],
"crearn": [11],
"venir": [11],
"altrimenti": [6],
"openoffic": [4,11],
"quasi": [2],
"note": [11,9,8,[3,7]],
"concreta": [11],
"noti": [[5,6],11,4,10,0],
"iniziata": [2],
"optionsautocompletechartablemenuitem": [3],
"darà": [11],
"line": [2],
"memorizzati": [11,[5,8]],
"memorizzato": [11,[8,10]],
"git": [6,[5,10]],
"taglia": [9],
"estensioni": [[0,11],[1,7]],
"disabilitar": [11],
"continuar": [5],
"nota": [8,[6,11],9,[2,10],3,5],
"xx-yy": [11],
"estrarrà": [11],
"avviar": [5,11,[2,6]],
"confronto": [11],
"consideri": [2],
"optionsspellcheckmenuitem": [3],
"estension": [6,11,[0,1,9,10]],
"quella": [11,6,9,[4,5]],
"frase": [11,[2,3,6,8]],
"alcuna": [[6,11],[1,5,8,10]],
"iniziato": [[6,11]],
"frasi": [11],
"optionssetupfilefiltersmenuitem": [3],
"quelli": [11,[6,8],[5,9],4],
"quello": [11,8,[4,9],5,[0,1,3,6]],
"alcuni": [11,[5,6],[0,1,9,10]],
"altgraph": [3],
"remoti": [10],
"ultim": [11,[3,8,10]],
"esterna": [11,8],
"eccezioni": [2],
"valuta": [2],
"sottomenu": [5],
"remota": [6],
"esterni": [11,6],
"without": [5],
"pratico": [9],
"esterno": [11,1],
"basta": [5,[4,10,11]],
"inseriscono": [11],
"xml": [11],
"remoto": [6,[5,8,10]],
"perdina": [6,7],
"xmx": [5],
"serv": [10],
"gli": [11,5,8,6,1,[3,9],10],
"ultimi": [8],
"imprevist": [8],
"connession": [[4,6]],
"befor": [5],
"util": [11,[5,6,9,10]],
"tar.bz": [0],
"seri": [11,6],
"già": [11,5,[4,6,8,9,10],[1,3]],
"all\'esempio": [2],
"registrar": [11],
"terminatori": [2],
"chieder": [[6,9]],
"sopra": [11,6,9,5,[2,4,10],[0,1,8]],
"doppi": [2],
"differenti": [[6,11],[5,8]],
"giù": [11],
"ogniqualvolta": [6],
"alternar": [6,8],
"xlsx": [11],
"rinominata": [6],
"rovesciata": [[2,5]],
"regolari": [[2,11],7,6,[3,4,5]],
"così": [11,[6,9,10]],
"mioprogetto": [6],
"assembledist": [5],
"direzion": [6],
"fossero": [6],
"target.txt": [11],
"pratica": [5],
"divisibili": [8,3],
"ritornano": [6],
"livello": [11,6,8,10],
"standard": [4,[1,5,6,8,9,11]],
"livelli": [6],
"l\'immunità": [10],
"nell\'intestazion": [11],
"permettendo": [11],
"consentito": [11],
"nameon": [11],
"optionsglossarytbxdisplaycontextcheckboxmenuitem": [3],
"spostarla": [11],
"gotonextnotemenuitem": [3],
"azion": [8,11,3,0],
"tar.gz": [5],
"gpl": [0],
"suggerit": [8],
"accettar": [10],
"risied": [5],
"interattivi": [2],
"determinati": [8],
"aggiunt": [6,[1,10,11]],
"minori": [11],
"success": [10,[9,11]],
"aggiung": [[6,11],1],
"azur": [5],
"traduci": [11],
"riavviato": [3],
"formato": [1,6,11,[0,7],[5,8]],
"locali": [6,11],
"formati": [11,6,[8,9],5],
"determin": [[6,11]],
"consentirn": [11],
"meccanismo": [8],
"presentazion": [11],
"brasiliano": [[4,5]],
"basso": [11,3],
"pannelli": [9,[5,11],[6,7,10]],
"voci": [11,1,8,[3,5,9]],
"pannello": [9,8,11,1,7,[5,6,10]],
"ovviament": [[6,9]],
"voce": [11,[5,8],1,3,[2,9]],
"mobili": [11],
"it-it": [4],
"bassa": [9],
"with": [5,6],
"pdf": [6,[7,8,11]],
"riallinea": [11],
"sicurezza": [11,[5,6,10]],
"mentr": [9,11,6,[1,3,5,8]],
"personalizzazione": [7],
"frazionamento": [11],
"descritto": [11],
"toolsshowstatisticsmatchesmenuitem": [3],
"descritti": [[5,6,11]],
"traduc": [11,[5,6]],
"viewdisplaymodificationinfononeradiobuttonmenuitem": [3],
"viceversa": [11,6],
"per": [11,5,6,8,9,4,[2,10],[1,3],0,7],
"tutt\'": [10],
"riapplic": [6],
"nell\'elenco": [11],
"sortirà": [9],
"projectaccesswriteableglossarymenuitem": [3],
"potrebb": [11,6,[4,5]],
"assomiglierà": [5],
"riservatezza": [[5,11]],
"descritta": [11],
"regexp": [5],
"imperativi": [11],
"assumer": [10],
"costruir": [5,2],
"sentencecasemenuitem": [3],
"implementazioni": [5],
"troveranno": [11],
"tutti": [11,6,5,8,[2,9]],
"corrent": [[8,11],[3,5,10],9],
"uhhhh": [2],
"tutto": [[3,10,11],[4,5,9]],
"collaborazione": [7],
"buon": [11],
"optionssentsegmenuitem": [3],
"esegu": [11,8,5],
"rilasciar": [5,9],
"generazion": [11],
"assegnato": [11],
"optionsaccessconfigdirmenuitem": [3],
"sottil": [11],
"test.html": [5],
"unità": [11,10],
"xxx": [10],
"smalltalk": [11],
"risultar": [[2,9,10,11]],
"limitazion": [2],
"tempo": [[9,11],[4,6,8]],
"associata": [11],
"eliminandolo": [11],
"unirl": [6],
"recuper": [11],
"pseudotranslatetmx": [5],
"salvarlo": [6],
"caricar": [[6,11],[5,8]],
"abbass": [10],
"project_save.tmx.temporaneo": [6],
"attribuiscono": [5],
"targetlanguagecod": [11],
"personalizzati": [11,[3,8]],
"toglier": [11],
"attenzion": [[6,11],5],
"associati": [[5,8]],
"verificator": [11],
"tramit": [11,9,5,[6,8]],
"spaziatric": [11],
"riman": [11],
"associato": [11],
"inalter": [10],
"causa": [[5,10]],
"denominati": [4],
"applicabilità": [11],
"extra": [5],
"citato": [[5,9]],
"accorg": [4],
"citati": [[1,6]],
"potenzialità": [11],
"tien": [8],
"combinar": [11],
"consecutivi": [11],
"encyclopedia": [0],
"proceder": [6,[0,11]],
"applicar": [11,3],
"rapidament": [11],
"maiuscol": [11,2,5],
"optionstagvalidationmenuitem": [3],
"post-elaborazion": [11],
"preservati": [11],
"europea": [6],
"pt_br": [4,5],
"discussion": [6],
"piè": [11],
"a-z": [2],
"recuperar": [[6,11]],
"mobil": [11],
"evento": [3],
"password": [11,6],
"modificator": [3],
"seguono": [[2,6,8]],
"nuovament": [6],
"divisibil": [11],
"più": [11,5,9,[2,6],10,4,[1,8],3],
"dell\'utent": [5,[8,11],[3,7]],
"diagrammi": [11],
"un\'interfaccia": [5],
"personalizzar": [11],
"javascript": [11],
"mediawiki": [11,[3,8]],
"confermata": [9],
"input": [11,6],
"ossia": [11,[6,8],[5,9]],
"volt": [11,2,[6,8,10]],
"sommario": [7],
"limita": [6],
"stanno": [6],
"contengano": [[6,10]],
"poi": [11,5,[3,4,6,8]],
"found": [5],
"inalterati": [11],
"colori": [[8,11],3],
"fabbrica": [11,9],
"inalterato": [11],
"corrispond": [11,2,8,[4,9]],
"memorizzar": [[4,9]],
"espresso": [6],
"volessero": [11],
"pubblicati": [[0,6]],
"trascinato": [9],
"googl": [5,11],
"opendocu": [11],
"l\'opzion": [11,8,[1,5,6,9,10]],
"preservato": [11],
"dell\'attual": [6],
"download.html": [5],
"esegua": [[5,11]],
"lato": [6,11],
"ordina": [11],
"qualch": [[5,6],[4,11]],
"originario": [9],
"fatta": [1],
"adotta": [6],
"sourceforg": [3,5],
"continua": [11],
"esitar": [6],
"corrett": [5,[6,9,10]],
"precedut": [2],
"lati": [6],
"originaria": [9,11],
"continui": [11],
"definisc": [11,8],
"fatto": [6,5],
"editmultipledefault": [3],
"batch": [5],
"mozilla": [5],
"editfindinprojectmenuitem": [3],
"prendersi": [4],
"individua": [5],
"definito": [[4,8,10]],
"contemporaneament": [11],
"inoltr": [6],
"offert": [9],
"vuoti": [8,[3,5,11]],
"warn": [5],
"technetwork": [5],
"definita": [11,[3,6]],
"trattar": [6],
"vuoto": [11,6,[1,8,9]],
"prodotta": [5],
"semplici": [11,1],
"pers": [6],
"plural": [11],
"video": [5],
"miscellanea": [11],
"consultabili": [[0,9]],
"all\'inizio": [5,[2,10]],
"perd": [9],
"dell\'accesso": [11],
"tradurr": [11,6,9,10,8,5],
"prodotti": [11],
"numeral": [6],
"appariranno": [11],
"conto": [11],
"avviarlo": [5,11],
"prodotto": [6],
"gergo": [11],
"riutilizz": [6],
"windows": [7],
"configura": [11,8],
"valutati": [11],
"rilev": [9],
"colour": [11],
"n.n_windows.ex": [5],
"dimenticati": [0],
"riconosciut": [[2,11]],
"immediatament": [8,[1,2,11]],
"tipo": [11,6,[0,5]],
"nell\'intero": [11],
"parzial": [[8,11],9],
"nonostant": [11],
"program": [5],
"uguali": [[4,5,8,9]],
"notoriament": [6],
"tipi": [11,[5,8]],
"apportar": [11,10],
"pagina": [11,8,[3,5],2],
"univoci": [11,9,8,3],
"contaminar": [6],
"attesa": [[1,7]],
"strutturali": [11],
"mantenerlo": [11],
"cercar": [[0,4]],
"separa": [[9,11]],
"n.n_mac.zip": [5],
"mancant": [[2,3,8]],
"univoco": [11,[3,9]],
"riposizionar": [9],
"parziali": [[9,11],8,6,10,7],
"però": [6,[5,11]],
"sicura": [11],
"eliminarlo": [9],
"theme": [11],
"impostando": [11],
"atteso": [6],
"dall\'ordin": [11],
"editor": [11,[5,8,9],[1,6,7]],
"pseudotranslatetyp": [5],
"esecuzion": [[8,9]],
"eliminazion": [[6,11]],
"passaggi": [11],
"giallo": [[8,9]],
"un\'unica": [8],
"dizionari": [4,0,7,8,[6,9,10,11],[1,3]],
"scrivendo": [11],
"rispetto": [11],
"negazion": [2],
"flusso": [9],
"ricerca": [11,8,2,[6,7]],
"projectclosemenuitem": [3],
"tuttavia": [6,11,[4,5]],
"ulteriorment": [11],
"ricerch": [11,[2,8]],
"può": [11,6,9,5,[3,10],8,1,4],
"viewmarknonuniquesegmentscheckboxmenuitem": [3],
"pippo": [11],
"chiud": [8,11,6],
"importa": [[5,11]],
"group": [9],
"scuro": [11],
"dinamico": [11],
"cronologia": [8,3],
"gross": [11],
"findinprojectreuselastwindow": [3],
"campi": [11,[5,6,8]],
"bloccato": [5],
"deselezionando": [11],
"readme.txt": [6],
"marcata": [[10,11]],
"campo": [11,9,[5,8],4],
"languagetool": [11,8],
"ricaricato": [6,[1,11]],
"commuta": [3,8],
"annullata": [8],
"source.txt": [11],
"posseder": [[1,11]],
"files.s": [11],
"impostarn": [11],
"exchang": [1],
"indicando": [11],
"compilazion": [5],
"procedur": [11],
"currseg": [11],
"generat": [[6,10]],
"l\'appropriata": [11],
"inibir": [11],
"trascinando": [5,9],
"point": [11],
"colonn": [11,1],
"general": [8,11],
"identifica": [11],
"generar": [[6,10]],
"esister": [[6,9]],
"spostar": [[8,9]],
"torna": [5],
"dimension": [9],
"scaricar": [5,0,4],
"facil": [11],
"l\'inserimento": [[6,9]],
"impostata": [11],
"alternativa": [11,8,9,[5,6],3],
"riguardanti": [9],
"letti": [[1,10]],
"impostati": [11],
"contesto": [11,9,[6,8]],
"normalment": [5,11],
"condivider": [6],
"parentesi": [11],
"impostato": [11,4],
"unico": [11],
"attivar": [11,8,[3,10]],
"account": [5,11],
"dhttp.proxyhost": [5],
"diversa": [[4,6,11]],
"predizion": [8],
"comunement": [[6,8,11]],
"medesimo": [5],
"barra": [9,[5,11],[2,7]],
"diverso": [11,[6,10]],
"mantener": [6,[5,10,11]],
"ignorar": [[4,8,10,11]],
"rileva": [1],
"medesima": [11,8],
"diversi": [11,[5,6,8],[2,9]],
"prime": [8,[6,11]],
"lunghezza": [9],
"discesa": [4],
"tecnici": [8],
"prima": [11,8,6,[5,9],1,[3,10],[2,4]],
"specificar": [5,11,[3,6,10]],
"tecnico": [11],
"salvata": [[6,10]],
"configur": [5],
"trann": [6],
"contiene": [7],
"unicode": [7],
"importarla": [8],
"funzionalità": [11,[6,8]],
"dell\'editor": [11,9,8,10],
"documento.xx": [11],
"optionsworkflowmenuitem": [3],
"digitando": [11,8,5],
"releas": [6],
"sparc": [5],
"segmentar": [11],
"salvato": [[1,8],[6,11]],
"conterrà": [10],
"correzioni": [11,8],
"aggiungervi": [10],
"limitato": [11],
"gestion": [6,11],
"aggiungern": [3],
"riguardo": [10],
"selezionando": [11,8,[5,9,10]],
"vengono": [11,6,5,[1,10],8,9],
"destinazion": [11,4,8,[6,9],10],
"frequentement": [6],
"struttura": [10,11],
"legger": [6],
"tale": [11,[5,8],[6,9]],
"intervallo": [2],
"subdir": [6],
"localizzazion": [6],
"tali": [11,[6,10]],
"mostra": [8,11,3,9,[0,10]],
"eseguito": [5,8,[6,9]],
"corregger": [8],
"danni": [11],
"mostri": [11],
"passar": [11,[6,8,9]],
"forward-backward": [11],
"eseguita": [6,[5,8,11]],
"abbia": [1],
"file-source-encod": [11],
"ripetizion": [2],
"tant": [6],
"session": [11,10],
"dominio": [11],
"approssimativo": [11],
"bisognerà": [11],
"criteri": [11],
"nell\'altra": [11],
"primo": [11,[5,8],[6,10]],
"primi": [11],
"sola": [2,6],
"editexportselectionmenuitem": [3],
"solo": [11,6,[5,8],1,4,[0,9]],
"home": [6,5],
"eliminar": [11,6,5],
"projectaccesstargetmenuitem": [3],
"spostati": [[9,11]],
"sincronizza": [5],
"spostato": [9],
"attual": [11,9],
"varianti": [[2,11]],
"stess": [5,9],
"affianco": [[4,11]],
"bene": [6],
"aligndir": [5],
"servizio": [[8,11],5],
"quest\'operazion": [6],
"system-host-nam": [11],
"action": [8],
"sostituzioni": [8],
"sostituzione": [7],
"operativi": [[5,10]],
"creat": [6,11],
"quest\'ultimo": [11],
"python": [11],
"es_mx.dic": [4],
"pulsanti": [11],
"sono": [11,6,8,5,[1,9],3,10,0,2,4],
"operativa": [11],
"infix": [6],
"bell": [2],
"ugual": [11,8,9,3],
"crear": [6,11,5,[4,8]],
"lavorato": [11],
"funzioni": [11,9,5],
"quest\'ultima": [4],
"codic": [3,11,4,5],
"tarbal": [0],
"tratta": [11,9],
"funziona": [[8,11],[2,4,6]],
"migliorar": [11],
"operar": [11],
"infin": [[5,11]],
"periodo": [6],
"d\'altro": [11],
"vuota": [11,[8,10],[3,6]],
"ripristina": [[8,9],[3,11]],
"prototipo": [11],
"file": [11,6,5,8,10,1,4,3,9,0,7],
"gratuito": [5,7],
"dieci": [[6,8,11]],
"regola": [11],
"gratuiti": [[4,11]],
"spazi": [11,8,[2,3],1],
"operativo": [5,[1,11],8],
"unicament": [6],
"leggendon": [11],
"meno": [[5,6],10],
"delimitazioni": [8],
"menu": [3,[7,11],5,8,9,[1,4,6]],
"positivi": [11],
"rileveranno": [2],
"a-za-z": [2,11],
"l\'allineamento": [11,5],
"lasciato": [[8,11]],
"affinché": [[6,11],5,8],
"intermedio": [6],
"progetto": [6,11,8,10,[3,5],9,1,7,4,0],
"jolli": [11,6],
"lasciati": [11,5],
"sommità": [9],
"source-pattern": [5],
"trascinati": [9],
"fine": [11,[2,10],[5,6]],
"lasciata": [11],
"lavorarlo": [11],
"limitata": [11],
"host": [11],
"radic": [11,[1,3,9],[5,6,8]],
"problemi": [8,1,[0,6,7],5],
"modificabili": [6],
"problema": [1,6],
"aggiungerla": [5,4],
"manovrato": [11],
"dall\'esterno": [9],
"paradigma": [11],
"true": [5],
"inviar": [6,11],
"illustrato": [0],
"present": [11,5,[0,2,4,6,10]],
"farvi": [11],
"groovi": [11],
"utenti": [5,[6,7],11,2],
"sottrazion": [2],
"evitar": [6,11,10],
"l\'ora": [[8,11]],
"contestual": [11,9,[1,3]],
"unitament": [6],
"complicato": [6],
"aprirla": [11],
"l\'invio": [11],
"trascinare": [7],
"mese": [6,5],
"tipizzato": [11],
"aprirlo": [[5,6,11]],
"possono": [11,6,5,[8,10],[1,9]],
"visualizzarlo": [10],
"fino": [5,11,[2,4,6]],
"master": [6,11],
"spesso": [6,11],
"progetti": [11,8,6,[1,5,7]],
"kmenuedit": [5],
"percentual": [9,11],
"xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx": [5],
"writer": [6],
"rubi": [11],
"utili": [2,[5,11]],
"sfrutterà": [5],
"pubblicar": [6],
"riferimenti": [2],
"dell\'aggiunta": [11],
"globali": [1],
"riportato": [[6,9],4],
"tralasciando": [6],
"disconnettersi": [6],
"metodo": [5,[6,11]],
"metodi": [11,5],
"formattazion": [6,11,10],
"pertinenti": [[3,8,11]],
"riportata": [9],
"analizzerà": [5],
"sembra": [8],
"scegli": [8],
"user.languag": [5],
"regex": [2,7],
"meta": [3],
"keystrok": [3],
"avvia": [11,5,8],
"premuta": [[8,11]],
"un\'icona": [8],
"riutilizzar": [6],
"visualizzata": [8,9],
"premuto": [3],
"l\'intestazion": [11],
"visualizzati": [11,1,[6,8]],
"avvio": [5,7,11,8],
"global": [11],
"esclusioni": [[6,11]],
"visualizzato": [11,9,1,6,[5,8]],
"leggimi": [11,5],
"invito": [6],
"valori-chiav": [11],
"contatori": [9,7],
"valor": [2,11],
"digitazion": [8],
"pagin": [[6,11]],
"un\'operazion": [6],
"ibm": [5],
"chiav": [11,5],
"comun": [11],
"gamma": [[4,11]],
"spagnolo": [4],
"porzion": [[4,8,11]],
"parsewis": [11],
"scannerizz": [6],
"ancora": [11,[6,9],[2,8]],
"utilizzar": [11,5,6,1,4,[8,9]],
"chiamati": [[4,5,6]],
"attinger": [4],
"chiamato": [5,[1,11]],
"quest\'area": [11],
"rimossa": [10],
"autenticarsi": [5],
"dell\'argomento": [10],
"lavorarci": [6],
"conforma": [6],
"installar": [5,0,4],
"metà": [11],
"rimossi": [11],
"ignorata": [11],
"guid": [6,[0,10]],
"testual": [6],
"idx": [0],
"simil": [[6,11],9,[5,10]],
"ignorati": [[5,11]],
"conseguenza": [[9,11]],
"qui": [11,9,[5,6,8,10]],
"motori": [8],
"ignorato": [11,3],
"causar": [11],
"projectaccesscurrentsourcedocumentmenuitem": [3],
"linux": [5,[2,7,9]],
"entità": [11],
"rilasciarlo": [9],
"colorato": [8],
"rapido": [5],
"apportarvi": [11],
"piccola": [10],
"inferior": [9,11,[6,8]],
"file.txt": [6],
"elimina": [[4,8]],
"dall\'applicazion": [8],
"chiamata": [5,[10,11]],
"ifo": [0],
"rapida": [5],
"corsivo": [11],
"comment": [11],
"intatta": [10],
"icona": [5],
"sostituzion": [11,8],
"risulterà": [[6,8,11]],
"costoro": [6],
"regol": [11,6,[2,5,10]],
"opportuno": [[4,11]],
"importazion": [6],
"gruppo": [11,6,[1,2,5]],
"sistema": [5,11,[1,4],[6,8],3],
"xx.docx": [11],
"letterali": [11],
"semplic": [2,[5,6],4],
"svolto": [9],
"sistemi": [5,6,11,[1,3,7,10]],
"calcolo": [11],
"dall\'elenco": [11,8],
"agisc": [10],
"massimizza": [9],
"dell\'estension": [11],
"cartella": [5,6,10,11,8,1,[3,4,9],0,7],
"optionsautocompleteautotextmenuitem": [3],
"opportuni": [6],
"zip": [5],
"linguistica": [6],
"risieder": [1],
"concis": [0],
"customer-id": [5],
"richied": [[4,5,6,11]],
"versioni": [5,6,2],
"term.tilde.com": [11],
"all\'editor": [11],
"interagisc": [11],
"linguistico": [11],
"riveder": [10],
"linguistich": [4],
"incrementar": [11],
"viewmarknotedsegmentscheckboxmenuitem": [3],
"preferenze": [7],
"dell\'applicazion": [6,5],
"l\'esempio": [11,[2,5]],
"controllarla": [5],
"gotomatchsourceseg": [3],
"appropri": [[6,10]],
"eseguirà": [11,4],
"optionssaveoptionsmenuitem": [3],
"excel": [11],
"comma": [1],
"runn": [11],
"costruire": [7],
"restituito": [5],
"stardict": [0],
"ridott": [10],
"descriver": [11],
"omegat.l4j.ini": [5],
"impostazion": [11,6,[5,8],[2,9,10]],
"gruppi": [11],
"span": [11],
"seguent": [2,[3,5,11],6,0],
"metatag": [11],
"vicin": [11],
"l\'intero": [8,[5,11]],
"stessa": [11,6,[5,10]],
"sbaglio": [11],
"influenzerà": [11],
"visualizzi": [8],
"restituisc": [11],
"quantità": [5,11],
"cifr": [[5,6]],
"thunderbird": [4,11],
"eccesion": [6],
"editselectfuzzy3menuitem": [3],
"stesso": [11,6,2,0],
"adatto": [5],
"verifica": [8,11,[1,2,5]],
"costituirà": [11],
"stessi": [11,[1,5,8]],
"fals": [[5,11]],
"visualizza": [8,11,9],
"project.projectfil": [11],
"verifich": [[2,8]],
"periferich": [6],
"preferirsi": [11],
"aggregati": [11],
"trovat": [11],
"somiglianza": [11],
"adatta": [[8,9]],
"compatibil": [5],
"trovar": [11,5,3],
"relativament": [11,6],
"dovrebbero": [11,[3,4,5]],
"adatti": [4],
"sincronizzazion": [11],
"sebben": [11],
"arrivar": [5],
"error": [6,5],
"evidenza": [[3,8]],
"momento": [[10,11],[6,9]],
"imposta": [8,11,3],
"shortcut": [3],
"protegger": [11],
"rispettando": [10],
"pt_br.aff": [4],
"tmx2sourc": [6],
"precedentement": [[6,11]],
"ing": [11],
"ini": [5],
"etichettar": [11],
"proced": [6],
"rimozion": [11],
"riferimento": [6,1,9],
"sovrascr": [8],
"restring": [11],
"poiché": [5,[4,9,11]],
"dhttp.proxyport": [5],
"elenchi": [[1,11]],
"evidenzi": [11],
"fare": [11,5,8,[1,6,9,10]],
"subrip": [5],
"selezion": [[8,11],3,[0,4,9]],
"l\'accesso": [[5,8,11]],
"score": [11],
"aggiornati": [1],
"aggiornato": [[6,11]],
"usano": [[6,11]],
"utilizzati": [11,[2,4,5]],
"descriv": [6],
"passo": [6],
"impostar": [11,8],
"notano": [6],
"passa": [11],
"raw": [6],
"ripetut": [11],
"utilizzata": [[5,11],[8,9]],
"tornar": [[9,11],8],
"implementati": [11],
"fonti": [11],
"dovranno": [11,6],
"inviati": [11],
"conserv": [5],
"pont": [6],
"decomprimer": [5],
"discorrendo": [11,5,[0,6,10]],
"utilizzato": [11,[3,8,9]],
"copia": [[6,8,11],[3,9,10]],
"sviluppo": [2],
"spunta": [11],
"aaa": [2],
"raccolta": [[2,9,10]],
"specificati": [11],
"fate": [11],
"controllato": [11],
"contemporari": [0],
"solari": [5],
"ambient": [5],
"possiedono": [[4,11]],
"manual": [8,[3,4,5,7]],
"specificato": [5,[6,10]],
"carica": [[8,11],6],
"rimaner": [[9,11]],
"indicatori": [9],
"cima": [11],
"appendic": [[1,2,4],[0,3],6],
"aggressiva": [8],
"fase": [11],
"abc": [2],
"rcs": [6],
"abilitar": [[4,5,11]],
"permessi": [5],
"essendo": [11],
"concordanz": [11,9,8,10,[1,3],6],
"progressivament": [[10,11]],
"precedent": [8,[3,6],[9,11]],
"algoritmi": [11],
"specificata": [[5,11],[3,4]],
"caricando": [11],
"algoritmo": [[3,8]],
"l\'altro": [[8,11]],
"sintassi": [11,3],
"controllarn": [6],
"raccomanda": [6],
"iso": [1],
"giapponesi": [11],
"farà": [11],
"vengano": [[2,6]],
"cartell": [11,6,8,[0,1,5,10]],
"rilevatori": [[2,7]],
"scrive": [6],
"glossary.txt": [6,1],
"utilizza": [11,5,6],
"spuntata": [11],
"add": [6],
"cambio": [6],
"casella": [11,4,5],
"scritt": [11],
"gestione": [7],
"utilizzo": [2,[5,6,7]],
"patrimonio": [6],
"esclus": [6],
"consent": [11,[5,9],[2,6]],
"accedi": [3,8,11],
"utilizzi": [6],
"pertanto": [11],
"prescinder": [[1,11]],
"cambia": [[8,11]],
"buona": [2],
"subito": [[6,10]],
"riservata": [4],
"optionsautocompleteshowautomaticallyitem": [3],
"esclud": [6],
"larouss": [9],
"dall\'amministrator": [11],
"untar": [0,5],
"sarebb": [[4,6]],
"massimizzazion": [9],
"millimetrico": [11],
"risors": [6,11],
"filters.conf": [5],
"scopi": [11],
"caratter": [2,11,[3,5],8,1],
"scopo": [[4,5,11]],
"allineato": [8],
"milioni": [5],
"mappatura": [6,11],
"installti": [11],
"correzion": [4,[3,6]],
"corrisponder": [11,4],
"clone": [6],
"targetlanguag": [11],
"quanto": [[9,11]],
"estern": [8,3],
"indicar": [[5,6]],
"filtro": [11,6,5],
"memorie": [7],
"backup": [6],
"contratto": [5],
"memoria": [6,11,5,[9,10],8,2],
"properti": [[5,11]],
"filtri": [11,[6,8],[3,5,10]],
"corrispondenti": [11,[4,10]],
"durant": [11,5,6,[8,10],9],
"editselectfuzzyprevmenuitem": [3],
"cioè": [10],
"copiar": [6,4,[5,8],10],
"inserirlo": [1],
"lettura": [6],
"copiat": [[6,11]],
"cercando": [6],
"simpledateformat": [11],
"sempr": [11,1,[3,6,8]],
"svuotar": [9],
"conserverà": [[10,11]],
"script": [11,8,5,7],
"oltr": [11,5],
"system": [[6,11]],
"spellcheck": [4],
"ritorna": [10],
"basata": [[5,11]],
"distinguer": [10],
"analitico": [11],
"identico": [10,2],
"local": [6,5,[8,11]],
"segnal": [9],
"sostituito": [8],
"crea": [8,3,11,6,[1,5,9,10]],
"sostituisc": [8,11],
"interferisc": [5],
"repo_for_all_omegat_team_project_sourc": [6],
"lento": [[5,11]],
"futuri": [6],
"segmento": [11,8,9,3,10,1,[5,6]],
"identich": [6],
"utilizzano": [11,3],
"identici": [11],
"futuro": [6],
"suddividono": [9],
"segmenti": [11,8,9,6,[3,10],5],
"impatto": [11],
"dislocati": [6],
"identica": [11],
"attribuisc": [5],
"es_mx.aff": [4],
"riscontro": [1],
"inviando": [6],
"suggerimenti": [[3,9],11,[4,8,10]],
"mode": [5],
"corrispondenz": [11,6,10],
"modi": [6,5,[4,10,11]],
"usata": [6,[5,11]],
"suggerimento": [11,6],
"corrispondent": [2,11,[5,8,9]],
"usato": [11,[4,5,6,8]],
"segnalibri": [11],
"toolsshowstatisticsstandardmenuitem": [3],
"potrà": [11,[6,9]],
"modo": [11,6,5,9,[8,10],2,3],
"usati": [10,[6,8,11]],
"all": [11,[5,6],9,8],
"agenzia": [9],
"alt": [[3,5,11]],
"modalità": [5,6,11,9],
"real": [9],
"mostrino": [8],
"unir": [6],
"dell\'elenco": [11,8],
"finali": [8,6],
"osservar": [11],
"fondo": [11,3],
"registrata": [8],
"eu-direzion": [8],
"colonna": [11,8,1,9],
"registrati": [11],
"ritorno": [2],
"completato": [8],
"singolarment": [11],
"l\'interfaccia": [5,6],
"ispir": [11],
"and": [5,11,[6,7]],
"modifica": [11,9,8,3,10,6,7,1],
"modifich": [6,5,11,10,8,[1,3,9]],
"minuti": [6,11,8],
"attribuita": [5],
"ant": [[6,11]],
"ciascun": [11,8,[6,9]],
"bloccherà": [5],
"leggerment": [9],
"consistono": [11],
"considerando": [9],
"risparmiar": [11],
"argomento": [6],
"corrispondenza": [11,9,10,[1,8]],
"helplastchangesmenuitem": [3],
"traccia": [9,8],
"mobilità": [11],
"argomenti": [5],
"omegat.ex": [5],
"accurata": [10],
"interruzion": [11],
"grado": [11,6,[4,5,9]],
"perché": [6,[9,11]],
"sourcetext": [11],
"tasti": [11,[3,9],8],
"tradotto": [11,8,9,3,[5,6,10]],
"inevitabilment": [6],
"l\'avviator": [5],
"riparati": [6],
"english": [0],
"tabelle": [7],
"jar": [5,6],
"debbano": [11,5],
"api": [5,11],
"tabella": [2,3,11,9,1],
"editselectfuzzy2menuitem": [3],
"prossimo": [11,[3,8]],
"apr": [8,11,4,[1,6,9]],
"modificando": [5,6],
"tasto": [3,11,8,[1,9]],
"ciascuna": [11],
"letter": [[8,11],2],
"informazion": [11],
"editselectfuzzynextmenuitem": [3],
"completamento": [11,3,8,1],
"anziché": [[5,8,9]],
"potranno": [11,[4,8]],
"abbastanza": [6],
"readme.bak": [6],
"mancanti": [8,[3,9]],
"prevenzion": [6],
"parzialment": [9],
"facoltativa": [8],
"l\'espression": [11,2],
"art": [4],
"rinvenuti": [9],
"rtl": [6,7],
"scelto": [6],
"ulteriori": [11,5,[2,9,10],6],
"jdk": [5],
"significato": [11],
"tradotti": [11,8,6,9,[3,10],5],
"percentuali": [9,10],
"tradotta": [[6,8],[4,11]],
"sbloccati": [11],
"scelta": [[8,11]],
"verificheranno": [5],
"impraticabil": [5],
"toolsshowstatisticsmatchesperfilemenuitem": [3],
"grafica": [5],
"l\'accorpamento": [11],
"vicino": [11],
"run": [11,5],
"grafici": [11],
"molt": [[6,11]],
"titlecasemenuitem": [3],
"desiderata": [[5,6]],
"opzional": [1],
"permetterà": [11,[5,6]],
"editcreateglossaryentrymenuitem": [3],
"istanza": [[5,9]],
"all\'inserimento": [11],
"chieda": [11],
"precauzioni": [6],
"vien": [11,5,9,6,8,10,2,[1,3]],
"cancellando": [11],
"preferiranno": [6],
"privat": [11],
"innanzitutto": [6],
"name": [11],
"memorizzazion": [11,8],
"canc": [11,9],
"aggiorn": [5],
"allo": [11,[2,4,6,9]],
"doppio": [5,[8,9,11]],
"basati": [11,0],
"minuscola": [3],
"show": [5],
"consentono": [11,[5,9]],
"minuscolo": [[3,8]],
"l\'estension": [1,11,0],
"alla": [11,5,8,6,9,3,2,[4,10]],
"plurali": [1],
"basato": [11],
"assunto": [11],
"comput": [5,11],
"contien": [10,11,6,[5,8,9],0],
"situato": [4],
"un\'interruzion": [11],
"arrivo": [11,6,8,1,[3,5],[4,9,10]],
"avv": [11],
"avvieranno": [5],
"avvertimento": [9],
"occuperà": [11],
"quando": [11,6,9,5,8,[1,2],10],
"target": [[8,10,11],7],
"rinominarla": [6],
"grafico": [5],
"sorgent": [[5,11]],
"config-dir": [5],
"abbreviato": [11],
"blocchi": [[2,7,11]],
"all\'espression": [11],
"pression": [[3,9,11]],
"rimuovi": [11,[3,4,8]],
"sbagliat": [11],
"termbas": [1],
"evidentement": [9],
"finestr": [11,9,8],
"caso": [6,11,5,8,[9,10]],
"casi": [6,11,10],
"rappresentano": [11],
"rend": [6,11],
"esistono": [5,[1,4,11]],
"consig": [4,6],
"targettext": [11],
"perfettament": [[6,11]],
"includono": [[5,11]],
"identificativo": [11],
"presentano": [[9,11]],
"comportamento": [5,11,[3,8,9,10]],
"virgola": [2,6],
"identificativi": [11],
"condivision": [6],
"selettiva": [6],
"importati": [[1,6]],
"proprio": [5,4,[6,11],[1,9,10]],
"identificativa": [11],
"cifra": [2],
"aaabbb": [2],
"compil": [9],
"raggiungibil": [11,3],
"caus": [[1,5]],
"propria": [11,[5,9],8],
"edittagpaintermenuitem": [3],
"l\'immission": [6],
"mostrata": [[10,11]],
"adiacenti": [9],
"optionscolorsselectionmenuitem": [3],
"mostrato": [11,9],
"minimizzati": [9],
"intrapresa": [8],
"unicod": [2],
"mostrati": [9,8,[2,5]],
"viewmarknbspcheckboxmenuitem": [3],
"indicazioni": [6],
"causando": [8],
"scaricare": [7],
"dell\'installazion": [[4,5]],
"msgstr": [11],
"maiusc": [3,11,[6,8],1],
"solito": [[5,10]],
"sovrapporr": [9],
"potet": [10],
"poter": [4],
"individuati": [8],
"raccomandato": [11],
"scaricarl": [11],
"important": [5,[6,9,10]],
"sorgenti": [5,[7,11]],
"omegat.project": [6,5,10,[7,9,11]],
"excludedfold": [6],
"targetcountrycod": [11],
"inserisci": [[3,8,11]],
"riport": [6],
"d\'opera": [11],
"altr": [6,5,11,9,8],
"l\'utent": [11,[8,9]],
"webstart": [5],
"alto": [11,2],
"rete": [6,[5,11]],
"direttament": [5,11,[1,10]],
"consonanti": [2],
"inutil": [11],
"automazion": [5],
"coppi": [11],
"sincronizzar": [6],
"marcator": [9,11],
"intenzion": [5],
"programmazion": [11],
"l\'intervallo": [11],
"original": [6,[8,11],[9,10]],
"scaricato": [5],
"perciò": [6],
"consol": [5],
"mous": [9,11,[1,4,8]],
"yandex": [5],
"alta": [9,10],
"separatori": [9],
"consultar": [6,[5,11],2],
"a123456789b123456789c123456789d12345678": [5],
"viewmarkwhitespacecheckboxmenuitem": [3],
"equivalent": [0],
"oggetti": [11],
"configurazion": [11,5,[3,4,8]],
"avranno": [11],
"oggetto": [6,11],
"complet": [6],
"bak": [6,10],
"tradott": [11],
"paragrafo": [11],
"corrispondono": [11,[1,6,9]],
"regolando": [11],
"bat": [5],
"grigio": [8,11],
"paragrafi": [8,[6,11]],
"ricarica": [8,[3,6,11]],
"jre": [5],
"escludendo": [6],
"optionsfontselectionmenuitem": [3],
"indicato": [5,6],
"caricato": [[6,11]],
"attivando": [5],
"popolata": [6],
"caricati": [11],
"popolato": [[6,11]],
"popolati": [8,[3,11]],
"l\'ortografia": [4],
"caricata": [5],
"naturalment": [10,[4,5,11]],
"destro": [9,11,5,[1,4,8]],
"destra": [6,11,[5,7,8,9]],
"riporta": [9],
"l\'impostazion": [[6,11]],
"schema": [[2,11]],
"vogliono": [6],
"freebsd": [2],
"permetti": [11,8],
"icon": [5],
"digita": [[5,11]],
"dead": [6],
"dubbi": [11],
"projectaccessglossarymenuitem": [3],
"richieder": [[4,5,8]],
"indicata": [[0,8]],
"separazion": [8],
"indicati": [11,10],
"developerwork": [5],
"avvierà": [5,11],
"set": [5],
"categorie": [7],
"associar": [[8,11]],
"operano": [11],
"parol": [11,8,9,2,[1,6,10]],
"categoria": [2],
"optionsrestoreguimenuitem": [3],
"popolari": [6],
"rispettivament": [4],
"incorporar": [6],
"l\'occorrenza": [9],
"procedura": [6,11,[4,5,8,9]],
"l\'elenco": [[2,6,8,11]],
"tendina": [11],
"detto": [6],
"copierà": [11],
"offic": [11],
"frequenti": [5],
"coinvolgono": [8],
"parti": [[9,11],6],
"terminologica": [[8,11]],
"repositories": [7],
"projectsavemenuitem": [3],
"terminologich": [9],
"dell\'insiem": [11],
"terminologici": [1],
"xmx6g": [5],
"combinati": [5],
"dovessero": [6],
"esclusivament": [11],
"caldament": [6],
"chiuso": [6,8],
"l\'ampia": [[4,11]],
"compar": [11],
"cursor": [8,11,9,1],
"provar": [11],
"applicata": [10],
"un\'alternativa": [11],
"sia": [11,5,9,6,[2,4,8]],
"molti": [[6,11]],
"sig": [11],
"file-di-configurazion": [5],
"esempio": [11,6,5,4,[0,8],[2,9],10,[1,3]],
"ordinati": [10],
"client": [6,10,[5,9,11]],
"applicato": [11],
"disattiverà": [8],
"conversion": [6],
"restano": [5],
"dell\'ultima": [8],
"comparir": [9],
"indicano": [[9,11]],
"falsi": [11],
"attuali": [10],
"indicant": [11],
"correttezza": [11],
"capitolo": [10,[2,6,8,9,11]],
"quest\'opzion": [11],
"seguenti": [11,5,2],
"funzionant": [[5,10]],
"penalità": [10],
"l\'ultimo": [8,[5,10]],
"cerca": [11,8,3,2],
"elaborati": [[5,11]],
"bis": [2],
"ritenuti": [11],
"un\'implementazion": [5],
"un\'attribuzion": [5],
"projectopenmenuitem": [3],
"autom": [5],
"elaborato": [5],
"associarlo": [11],
"toolsvalidatetagsmenuitem": [3],
"necessario": [5,6,11,4,2],
"attribu": [5],
"scorretto": [8],
"decid": [11],
"autor": [11],
"familiarizzato": [6],
"molto": [11,6],
"inserisc": [8,11],
"necessaria": [11,6],
"squadra": [11,[3,6,7]],
"mappatur": [6],
"l\'ultima": [8,3,6],
"contengono": [11,6,[8,9]],
"viewmarktranslatedsegmentscheckboxmenuitem": [3],
"obbligatoria": [11],
"giustificazion": [6],
"valu": [1,5],
"ilia": [5],
"funzionali": [11],
"conferma": [11,[3,10]],
"vale": [[8,9]],
"simili": [11,9],
"programma": [5,11,6],
"cinesi": [11],
"disattivar": [11,[1,8]],
"evitata": [11],
"obbligatorio": [1],
"macos": [7],
"programmi": [[5,11]],
"saltata": [11],
"poc\'anzi": [6],
"editselectfuzzy1menuitem": [3],
"maniera": [8,11],
"blu": [[9,11]],
"scheda": [[8,9]],
"hide": [11],
"all\'interfaccia": [10],
"azioni": [11,8,5],
"piuttosto": [[4,5,6]],
"un\'applicazion": [6],
"auto": [10,[6,8],11],
"implicano": [5],
"combinazioni": [[2,3]],
"comparsa": [[8,11]],
"diffusi": [11],
"applicano": [8,6],
"posto": [3],
"oracl": [5,3,11],
"suggerita": [[8,9]],
"inserirà": [[9,11]],
"linguaggio": [11],
"castigliano": [4],
"differenz": [11,6],
"saltato": [11],
"gradlew": [5],
"gerarchia": [10],
"riesc": [11],
"dietro": [[9,11]],
"modif": [1],
"consenti": [11],
"preserva": [11],
"errori": [[6,8]],
"comprendono": [11],
"particolarment": [11,9],
"sceglier": [11,6,[4,9]],
"produc": [5],
"direzioni": [6],
"inizia": [[6,11]],
"gratuitament": [5],
"sottolineata": [4],
"trovino": [11],
"switch": [11],
"inizio": [11,[2,3]],
"inserita": [11,[8,10]],
"total": [9,11],
"l\'installazion": [5],
"macchina": [11,5,8],
"bundl": [[5,11]],
"tenta": [11,[5,6]],
"composit": [11],
"interruzioni": [11],
"traduttric": [6],
"altament": [11],
"sottolineato": [1],
"macro": [11],
"subir": [6],
"inserito": [11,9],
"src": [6],
"control": [[3,6]],
"pressioni": [11],
"grassetto": [11,9,1],
"no-team": [[5,6]],
"dell": [11,6,8,3,9,5,[2,10],1,0],
"incorpora": [5],
"sistemar": [11],
"assegn": [3],
"possa": [6,[8,11]],
"nuovo": [6,11,8,5,4,[1,3,9]],
"traduttor": [6,9,11,10],
"riutilizzo": [6,7],
"aumentar": [11],
"environ": [5],
"l\'operazion": [5,[8,11],6],
"chiudendo": [9],
"optionsautocompleteglossarymenuitem": [3],
"brevi": [11],
"esportar": [10],
"vari": [11,[9,10]],
"breve": [2],
"secondari": [10,[0,9]],
"sta": [11,[5,9,10]],
"comprensibili": [11],
"summenzionato": [5],
"assegna": [6],
"kde": [5],
"stampa": [2],
"principale": [7],
"mieifil": [6],
"sua": [[5,6],[8,11],[1,9]],
"motor": [11,8],
"sue": [[0,6]],
"sug": [5],
"sui": [11,[3,4,5,6,8]],
"languag": [5],
"preferito": [[5,11]],
"nessun": [11],
"sul": [11,5,[1,6,8],0],
"suo": [11,5,9,[0,8],6],
"distingu": [2],
"chiudi": [11,[3,8]],
"preferita": [9],
"porta": [5],
"key": [5,11],
"condividono": [6],
"riluttanti": [2,7],
"fuori": [6,5],
"svg": [5],
"svn": [6,10],
"quell": [11,[6,9],[2,3,5,10]],
"segnaposto": [[6,11]],
"credenziali": [11,6],
"nuova": [5,11,8,2,[3,4]],
"nuovi": [11,[1,6,8]],
"editreplaceinprojectmenuitem": [3],
"un\'esportazion": [6],
"scaric": [11],
"dell\'utente": [7],
"selezioni": [8],
"express": [[2,11]],
"installato": [5,8],
"quest": [5,[6,8,11],4],
"zero": [11,2],
"generati": [[10,11]],
"potrebbero": [11,5,[2,6,10]],
"l\'ordin": [11,[8,9]],
"conflitto": [3],
"rinominar": [6,4],
"lanciator": [5],
"generato": [8],
"annulla": [[3,8]],
"riquadro": [11,2,[5,6,9]],
"variano": [5],
"seleziona": [8,3,11,5],
"gotoprevioussegmentmenuitem": [3],
"possieda": [11],
"accett": [3],
"composta": [9],
"verrano": [[6,8,11]],
"ricevuti": [11],
"composto": [6],
"gotopreviousnotemenuitem": [3],
"dichiarazion": [11],
"editredomenuitem": [3],
"composti": [1],
"uilayout.xml": [10],
"installati": [[4,8]],
"normali": [11],
"risultanza": [11],
"quadro": [11],
"preferisc": [11],
"installata": [5],
"trovato": [11],
"trovati": [[5,9]],
"interfaccia": [5,[6,9]],
"titolo": [11,[8,9]],
"coppia": [11,6,9],
"allegato": [11],
"provocar": [11],
"cines": [6,5],
"apport": [5,[6,9]],
"nidif": [1],
"deve": [6,[5,11],3,1,[0,4]],
"scelt": [5],
"conosciuta": [9],
"normal": [[5,11],[1,6,8,10]],
"figure": [7],
"figura": [4,[0,2,9]],
"significativi": [11],
"seconda": [[9,11],[1,3,6,8]],
"runtim": [5],
"individu": [[8,11]],
"aggiungono": [[6,11]],
"autenticazion": [11],
"differenza": [11],
"potent": [11],
"tester": [2,7],
"bianco": [2],
"specificano": [11],
"secondo": [[9,10],[5,6]],
"filenam": [11],
"tener": [11],
"secondi": [11],
"guida": [[5,6,7]],
"riferiscono": [[5,9]],
"guide": [7],
"certa": [10],
"nbsp": [11],
"gotosegmentmenuitem": [3],
"interno": [11,[5,6,8,9]],
"eventualment": [[4,10]],
"generano": [6],
"opportun": [6],
"preceduta": [5],
"interna": [9,[8,11]],
"xx_yy.tmx": [6],
"verrà": [11,8,[1,9,10],[5,6]],
"usando": [8],
"flag": [[2,7]],
"soluzion": [6],
"ripetitivi": [[2,7]],
"helpaboutmenuitem": [3],
"posiziona": [[9,11]],
"eventuali": [11],
"salvar": [6,[3,5]],
"limitar": [11,5],
"apert": [11],
"creerà": [5,11],
"eccessivament": [11],
"regular": [2],
"risoluzion": [6],
"tabellar": [8],
"sito": [[6,10,11]],
"facoltativament": [5],
"certi": [11,10],
"generali": [11,7],
"certo": [11],
"elementi": [[6,11],3,1],
"token": [11],
"della": [11,6,9,8,5,10,4,1,3,[2,7]],
"filter": [11],
"maggior": [11,[3,5]],
"traduzione": [7],
"installare": [7],
"elemento": [3,11],
"posizioni": [11,[5,6]],
"delle": [7],
"traduzioni": [11,[6,9],8,10,7,2],
"right-to-left": [6,7],
"arancion": [8],
"localment": [8,6],
"dello": [[6,10,11]],
"avet": [4],
"aver": [11,6,[5,8],[0,1]],
"soglia": [11],
"ingles": [[2,6],[5,9]],
"tab": [1,3,[8,11],9],
"taa": [11,8],
"divers": [11,[6,9]],
"tag": [11,6,8,3,[5,9]],
"tal": [6],
"apportano": [6],
"individuar": [[1,5,6]],
"slovenian": [9],
"tar": [5],
"individual": [6],
"onli": [11],
"projectreloadmenuitem": [3],
"almeno": [[10,11]],
"ripetizione": [7],
"safe": [11],
"spostarsi": [11],
"disponibili": [11,5,3,[1,4,6],[2,8,9]],
"nell\'ordin": [[9,11]],
"ripetizioni": [11,[2,8],9],
"servizi": [5,11],
"immetter": [11,5],
"perdita": [6],
"winrar": [0],
"tbx": [1,11,3],
"albero": [10],
"consulti": [11],
"cat": [10],
"duser.countri": [5],
"tcl": [11],
"tck": [11],
"presenteranno": [6],
"ottieni": [11],
"ritenut": [10],
"readm": [5],
"categori": [2],
"ricorrono": [11],
"piac": [11],
"convertirlo": [6],
"trovano": [11,[5,6,8]],
"align.tmx": [5],
"posizion": [11,5,8,[6,9],1,4],
"file2": [6],
"orfani": [11,9],
"permettono": [8],
"l\'informazion": [8],
"efficaci": [2]
};
