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
 "Appendici",
 "Preferenze",
 "Guide all&#39;uso...",
 "Introduzione a OmegaT",
 "Menu",
 "Pannelli",
 "Cartella del progetto",
 "Finestre e finestre di dialogo",
 "OmegaT 6.0.0 - Manuale dell&#39;Utente"
];
wh.search_wordMap= {
"cancel": [7],
"popolazion": [6,[1,2,4]],
"diversament": [2],
"would": [7],
"avanz": [0],
"passata": [2],
"correttore": [8],
"predefinit": [0,2,[3,5],[1,6,7]],
"ten": [7],
"evidenzia": [4,0,[1,5],[3,6,7]],
"ricercati": [0],
"info.plist": [2],
"passato": [[0,2]],
"legg": [1],
"l\'attual": [2,4],
"preconfigur": [1],
"click": [7,2],
"tuoi": [3],
"fuzzi": [[1,4,5,7],3,2,6],
"apparirà": [[3,4]],
"size": [7,2],
"left": [0,7],
"mostrar": [5,[0,6]],
"estratti": [0],
"latini": [0],
"individuarlo": [2],
"ricercato": [0],
"mostrat": [7,5,6],
"object": [7],
"conteggio": [4],
"guardar": [0],
"lavora": [[1,2,7]],
"concede": [8],
"turn": [7],
"convertendo": [2],
"possano": [4],
"result": [7],
"edittagnextmissedmenuitem": [0],
"esclusion": [[2,7]],
"conteggia": [[1,4]],
"same": [7],
"un\'autenticazion": [2],
"modificar": [0,[2,4,5],3,1],
"quiet": [2],
"after": [7],
"ampio": [2],
"aggiunger": [2,0,3,[5,6],1,[4,7]],
"gestor": [4,2,6],
"the": [7,2,0,[1,5],[4,6]],
"quadrati": [1],
"riepilogo": [2],
"dell\'espression": [0],
"lavoro": [3,0,[2,5,7,8]],
"preparar": [0],
"chiamat": [0],
"projectimportmenuitem": [0],
"frances": [2,1],
"riferisc": [2],
"imag": [0],
"lavori": [0,2],
"monolingu": [[0,7]],
"prescelt": [2],
"l\'indirizzo": [2],
"l\'associazion": [0],
"edizion": [2],
"priorità": [[1,4],[2,3]],
"distribuisc": [2],
"applica": [1,[4,6]],
"manuale": [8],
"l\'ingles": [2],
"nell\'ultima": [[4,5]],
"omegat.project.lock": [2],
"moodlephp": [2],
"accessibil": [6],
"l\'aggiunta": [6],
"currsegment.getsrctext": [7],
"sotto": [0,[2,5],[1,6],[4,8]],
"comprender": [2],
"export": [6],
"uncheck": [7],
"gestir": [3,[1,2],[0,7]],
"che": [0,2,[1,3],6,7,5,4,8],
"practic": [7],
"notificar": [1],
"chi": [[0,1]],
"reduc": [7],
"check": [7],
"selezionandolo": [7],
"manuali": [[0,6]],
"freddo": [4],
"incorporazion": [[0,4]],
"gotonotespanelmenuitem": [0],
"distribuito": [2,[0,3,8]],
"l\'utente": [8],
"conservar": [7],
"motivo": [0,4],
"minimizza": [[2,5]],
"ensur": [7],
"disco": [2],
"posizionandoli": [0],
"initializza": [2],
"varia": [5],
"termina": [0],
"offr": [2,3,[4,5]],
"esist": [[2,4]],
"produttività": [0],
"termini": [[3,5],[0,1,4],6,7,8],
"multiplo": [2],
"cjk": [7,0],
"duplicar": [3],
"multiple": [8],
"multipli": [0],
"richiamata": [0],
"better": [7],
"tutt": [2,[0,1,3,4,7],6,5],
"l\'anno": [2],
"bilingui": [2,6],
"well": [7],
"contrario": [[0,2],3],
"empti": [[2,4]],
"valida": [0],
"installazion": [2,0],
"validi": [2],
"dall\'interfaccia": [2,4],
"un\'ulterior": [0],
"rilevanti": [1],
"valido": [6],
"conteggi": [4],
"block": [7],
"propon": [[5,7]],
"tms": [6,[7,8]],
"blocca": [2,4,1],
"tmx": [2,7,6,1,[3,5]],
"order": [7],
"repo_for_all_omegat_team_project": [2],
"cli": [2],
"application_startup": [7],
"avanti": [2,4,5,[0,1,3,6,7,8]],
"eventtyp": [7],
"dell\'esempio": [0],
"dettagli": [2],
"clonar": [2],
"fr-ca": [1],
"mainmenushortcuts.properti": [0],
"gradualment": [[2,6]],
"sganciarlo": [5],
"identificatori": [[0,8],[3,4]],
"vedano": [7],
"un\'analisi": [0],
"prevedono": [2],
"cmd": [4,0,3],
"convertir": [2],
"complessivi": [0],
"provenient": [[0,2,6]],
"propri": [5,[1,2],3,[0,7]],
"gotohistorybackmenuitem": [0],
"luogo": [5],
"parametro": [2,1],
"save": [7],
"sarà": [0,2,4,7,1,6,3],
"ricevut": [5],
"allineamento": [0],
"sperimentano": [2],
"v1.0": [2],
"conversioni": [2],
"parametri": [2,0,1,6],
"allineamenti": [0],
"ciò": [2,4,[0,1,7]],
"complessivo": [0],
"proporzional": [1],
"top": [7,5],
"resteranno": [6],
"have": [7,1,[0,5]],
"powerpc": [2],
"nell\'ambito": [2],
"propost": [1],
"avail": [7,5,2],
"opzion": [0,1,2,7,4],
"schermo": [0],
"esporta": [0,4,1],
"question": [0],
"distribuire": [8],
"nominata": [1],
"elencati": [[0,2]],
"editselectsourcemenuitem": [0],
"sfondo": [6,[4,5]],
"l\'autor": [4],
"contemporanea": [[2,4]],
"qual": [0,[2,4,5]],
"anch": [2,0,[1,3],6,4,[5,7]],
"com": [0],
"apporti": [3],
"col": [2,4,5,[0,3],7,6,1],
"instal": [2,0],
"sacco": [0,3],
"con": [0,2,1,4,3,6,5,7],
"minor": [8],
"elenco": [2,1,3,0,[5,6,7]],
"propagazion": [[2,7]],
"cot": [0],
"remot": [[2,4,6,7]],
"riaprir": [2],
"lett": [2],
"upon": [7],
"avrà": [0,7,6],
"elenca": [5,0],
"ordin": [1,0],
"function": [7],
"pipe": [0],
"dell\'uscita": [1],
"coerenza": [4,2],
"elencano": [0],
"tra": [0,2,5,4,1,3,[6,7]],
"visualizzar": [1,3,[2,4],[0,5,6]],
"comparison": [7],
"contator": [7],
"tre": [0,[2,6],[1,3,4],[5,7]],
"piattaforma": [0,2],
"tri": [7],
"changeid": [1],
"translat": [7,0,1,4,[2,5]],
"segmentazioni": [0],
"assegnerà": [2],
"successiva": [[0,4],[2,3],5],
"segmentazione": [8],
"sposta": [4,[0,1,3,5]],
"distinzion": [0],
"université": [1],
"mantenimento": [0],
"successivo": [4,0,3,[2,5,6]],
"successivi": [0,7],
"chiudersi": [2,1],
"sull\'icona": [4],
"cqt": [0],
"correttament": [2,1,[0,4,5,7]],
"l\'origin": [1,7],
"docs_devel": [2],
"tsv": [0],
"intendono": [1],
"archivio": [6],
"gnome": [1],
"probabil": [5],
"totali": [4],
"commento": [0,[2,4,5]],
"contenitor": [0],
"nell\'intervallo": [0],
"diventar": [3],
"passavi": [3],
"blocco": [5,0,3,4,8],
"corretta": [[0,2,3]],
"corretto": [2,0],
"doctor": [0],
"crescent": [0],
"dell\'affidabilità": [2],
"sincronizz": [2],
"corretti": [[2,7]],
"avanza": [4,1],
"commenti": [5,0,[3,7,8]],
"quei": [[3,4],2],
"quel": [[0,2],[1,3],[5,6,7]],
"appdata": [0],
"tua": [3],
"tue": [3,5],
"senza": [2,0,1,[3,5,6],[4,7]],
"csv": [0,2],
"stata": [2,[1,4],[0,6]],
"apparir": [[2,3]],
"unificatori": [0],
"analogament": [5],
"tuo": [3,7],
"stato": [5,2,[0,4],[3,6],[1,7],8],
"eventual": [2],
"seguir": [3,2,0],
"seguit": [[0,1]],
"caractèr": [2],
"let": [7],
"state": [[2,4],[0,1,3,7]],
"press": [7,[0,1]],
"dock": [2],
"stati": [2,[1,5,7],[0,3,6]],
"element": [7],
"segmentazion": [7,0,1,4,[2,3],6],
"chiavetta": [2],
"rispecchiano": [[2,6]],
"night": [2],
"each": [7],
"importanti": [0,2,3],
"cui": [0,2,4,3,1,5,7],
"utilizzando": [2,5,[0,6],[1,3]],
"filenameon": [1,0],
"cut": [0],
"ctrl": [0,4,3],
"editorinsertlinebreak": [0],
"jumptoentryineditor": [0],
"document": [7,[0,2]],
"distribu": [0],
"two": [7],
"dollaro": [0],
"mappar": [2],
"caric": [6],
"page_up": [0],
"attività": [2],
"documentazion": [[0,2],3],
"glossaryroot": [0],
"scenario": [2],
"stavi": [3],
"attach": [7],
"verrebbero": [6],
"resourc": [2,7,0],
"preso": [7],
"moodl": [0],
"richieste": [8],
"presi": [1],
"prese": [1,[2,4]],
"team": [2,7],
"xx_yy": [0],
"presa": [6],
"docx": [[2,7],[0,4]],
"project_stats_match_per_file.txt": [[4,6]],
"txt": [2,0,5],
"prestar": [2],
"aggiornar": [2,[3,7]],
"possibilità": [2],
"avvisar": [3],
"l\'editor": [0,7,[1,4]],
"ordinar": [0],
"richiesti": [[0,3]],
"caratteri": [0,4,[1,5],8,2],
"definit": [2,[0,5,7],[4,6]],
"projectmedopenmenuitem": [0],
"anno": [0],
"definir": [[0,1],7,[3,5],2],
"attivi": [[0,2]],
"lib": [0],
"ridimensionati": [5],
"source": [8],
"attiva": [0,4,1],
"configurata": [[0,6]],
"dall\'ultimo": [0],
"riutilizzati": [0],
"accetta": [2],
"viewdisplaymodificationinfoselectedradiobuttonmenuitem": [0],
"index.html": [0,2],
"prefisso": [6,1,2],
"configurato": [2],
"umana": [1],
"doubt": [7],
"develop": [7,2],
"ultimato": [0],
"diffrevers": [1],
"page": [7],
"attivo": [4,0,5,[1,6],7],
"full": [7],
"fornitor": [[1,5]],
"nascosto": [1],
"indefinito": [0],
"l\'inizio": [0],
"qualcosa": [[1,2]],
"disposizione": [8],
"giappones": [2,1,0],
"perduta": [2],
"comandi": [1,7,0,2,4,5,8],
"ospitati": [1],
"rivolti": [5],
"principio": [6],
"project.gettranslationinfo": [7],
"comando": [2,0,1,[4,7]],
"spostamento": [[0,4]],
"riconvertir": [2],
"czt": [0],
"disposizioni": [1],
"avviando": [0],
"paio": [2],
"funzion": [0,4,[1,3],2,7,5],
"diretto": [0,2],
"installazioni": [0],
"recupero": [2],
"start": [7,2],
"mymemori": [1],
"regex101": [0],
"pair": [7],
"equal": [0,2],
"watson": [1],
"esatt": [4,[2,6]],
"nascosta": [6],
"produrranno": [7],
"fung": [6],
"recupera": [4,[0,1,2]],
"short": [7],
"libero": [[0,2,4]],
"nascosti": [[6,7]],
"metter": [2],
"adeguata": [2],
"prender": [[2,4,7]],
"selezionarn": [5],
"aspetta": [1],
"renderla": [6],
"three": [7],
"elenc": [[1,2]],
"viewmarkglossarymatchescheckboxmenuitem": [0],
"concesso": [2],
"sottoinsiem": [[0,2]],
"renderlo": [0],
"riconoscimento": [1],
"enter": [7,0],
"applic": [[1,7],2,[0,5]],
"bidi": [0,4,7],
"verrebb": [2],
"projectteamnewmenuitem": [0],
"gotoprevxenforcedmenuitem": [0],
"scorciatoi": [0,3,4,5],
"preced": [0,7,2],
"directorate-gener": [4],
"applicazion": [2,3,1],
"sessioni": [2],
"autocompletertablelast": [0],
"memori": [2,7,3,6,4,5,1],
"produrr": [1],
"creativo": [0],
"incolla": [5],
"aspetto": [1,3,8],
"recogn": [7],
"selezionato": [4,5,[0,3]],
"log": [0],
"aggirar": [4],
"eliminando": [7],
"interessato": [4],
"correggerà": [2],
"interessati": [0],
"openjdk": [1],
"永住権": [[1,7]],
"computer": [8],
"istruzioni": [2,0],
"basandosi": [0],
"interagir": [7],
"comporta": [2,0],
"toolscheckissuesmenuitem": [0],
"pane": [7,5],
"assomiglia": [3],
"orphan": [7],
"clic": [5,4,[0,1,3],[2,7],6],
"completament": [2],
"autocompletertablepageup": [0],
"farebb": [2],
"selezionata": [4,5,[1,2],0],
"fetch": [1],
"www.deepl.com": [1],
"originariament": [2],
"selezionati": [4,[0,2,7]],
"config-fil": [2],
"quick": [7,0],
"nell\'interfaccia": [0],
"dag": [2],
"paura": [3],
"dai": [[2,7],[0,1,4]],
"convertirà": [[1,3]],
"premendo": [3,[5,7]],
"dal": [[0,2],1,[4,6],5,[3,7]],
"contrastar": [3],
"shown": [7],
"day": [0],
"lre": [0,4],
"inglese-giappones": [2],
"obtain": [7],
"cifrato": [0],
"system-user-nam": [0],
"lrm": [0,4],
"ottimal": [4],
"format": [7,0,4],
"formar": [0],
"pausa": [3],
"trascinar": [[2,5,6]],
"console.println": [7],
"rainbow": [2],
"cambiat": [3],
"autocompleterlistdown": [0],
"cambiar": [1,2,[3,5,6,7],0],
"ipertestuali": [5],
"concentrarsi": [2],
"corso": [[2,7],0],
"pars": [7],
"part": [0,[2,4],[5,7],1,3],
"uscir": [2],
"principal": [[1,5],2],
"trascinarlo": [5],
"contrassegno": [[0,4]],
"browser": [1,[4,5]],
"activefilenam": [7],
"fuzzy": [8],
"concentrarti": [3],
"project_files_show_on_load": [0],
"dopo": [0,2,1,3,5,7,4],
"nello": [[0,1]],
"project_save.tmx.aaaammgghhmm.bak": [2],
"allineator": [4],
"intorno": [1],
"generarlo": [6],
"contrassegni": [4],
"tema": [1],
"saranno": [2,4,1,0,6,[3,5,7]],
"build": [[2,7]],
"nella": [0,2,5,4,3,[1,7],6],
"facilment": [2,[3,6]],
"temi": [1],
"teme": [2],
"possibil": [2,0,1,5,4,[6,7],3],
"ident": [7],
"entries.s": [7],
"deg": [[0,4],[1,2]],
"dei": [0,1,2,7,4,3,5,6,8],
"addit": [7],
"del": [[0,2],4,6,5,1,3,7,8],
"gotonextuntranslatedmenuitem": [0],
"rispecchia": [[4,7]],
"targetlocal": [0],
"altra": [[0,2],[3,5]],
"path": [[0,2]],
"ritien": [2],
"bind": [7],
"abilitarl": [7],
"abbinarlo": [0],
"abbrevi": [[0,3]],
"overwritten": [7],
"relativi": [[1,5,6,7]],
"all\'apertura": [3],
"relativo": [1,2,[0,3,4,7]],
"altri": [0,2,5,[1,8],[3,6,7],4],
"rilasciati": [5],
"rivolg": [3],
"impostazioni": [2,7,0,3,4,[1,5,6]],
"altro": [[2,3],[0,1,4],[5,6]],
"ricerche": [8],
"helpcontentsmenuitem": [0],
"resnam": [0],
"omegat-org": [2],
"avanzato": [0],
"evidenziar": [[5,6]],
"relativa": [0],
"remote-project": [2],
"descript": [7],
"initialcreationid": [1],
"rinomina": [3],
"ignore.txt": [6],
"projectaccessdictionarymenuitem": [0],
"eseguit": [[2,4]],
"acceder": [7,3,[0,1,2],[4,5,6]],
"all\'utent": [[0,1,3,4]],
"sentenc": [7],
"seguito": [0,2,6,1],
"terz": [2,3],
"consecut": [7],
"ricaricar": [[0,3,4,7]],
"crearsi": [2],
"dove": [0,[2,5],[3,6],1],
"marcatori": [0,[1,2]],
"davanti": [0,3],
"term": [7],
"eseguir": [2,[1,3],[0,6,7],4],
"un\'inclusion": [0],
"files_order.txt": [6],
"projectrestartmenuitem": [0],
"editorskipnexttoken": [0],
"trans-unit": [0],
"right": [7,0],
"selezionarlo": [3],
"un\'espression": [0,1],
"qigong": [0],
"stage": [7],
"seguiti": [0],
"maximum": [7],
"operazioni": [2,[1,3]],
"under": [7],
"seguita": [0,3],
"imper": [7],
"abilitano": [3],
"dir": [2],
"tenterà": [7],
"down": [0,7],
"l\'apertura": [[2,7]],
"impartir": [2],
"speso": [3],
"fisico": [2],
"provengono": [6,2],
"riempito": [6],
"esempi": [0,2,8,5],
"considerati": [0,1],
"elabor": [0],
"istanz": [0,[2,4]],
"alfabeticament": [5],
"grossi": [2],
"considerata": [0,5],
"unrespons": [7],
"viewfilelistmenuitem": [0],
"lasciat": [3],
"lasciar": [[0,1],[3,4,6]],
"adottata": [0],
"non-break": [7],
"journey": [0],
"test": [2],
"originali": [4,5],
"allinear": [4],
"omegat": [2,0,[3,7],1,4,6,[5,8]],
"sblocchi": [3],
"allemand": [7],
"deepl": [1],
"tesi": [1],
"all\'interno": [0,2,5,4,[1,3],6,7],
"questi": [2,0,[3,5],1,6],
"assistita": [2,[3,8]],
"configurazioni": [1],
"cancellato": [4],
"cancellati": [0],
"final": [0,3,6],
"questo": [0,2,4,5,3,[6,7],1,8],
"racchiuder": [0],
"configurazione": [8],
"numerati": [5,0],
"succed": [2],
"sostituiranno": [7],
"virtual": [7,2],
"messaggio": [[2,5]],
"rimuov": [[0,6],7],
"ignora": [0,[1,6]],
"rather": [7],
"console-align": [[2,7]],
"back": [7,0],
"questa": [0,1,2,6,7,4,3,5],
"disegni": [0],
"projectopenrecentmenuitem": [0],
"dipend": [[0,1,4]],
"dell\'intervallo": [0],
"load": [7],
"attivata": [[0,1],[4,5,7]],
"attivato": [[2,4]],
"inser": [[1,6],2],
"coincid": [3],
"aggiunti": [[2,6],4],
"all\'altro": [[2,4]],
"custom": [7],
"lettera": [0,4,[1,3]],
"issue_provider_sample.groovi": [7],
"una": [0,2,1,4,3,6,5,7,8],
"aggiunto": [[2,7]],
"terminano": [0],
"considerato": [0],
"grand": [3],
"appena": [[2,4,5,6]],
"uno": [0,2,[1,4],5,[3,6,7]],
"editoverwritemachinetranslationmenuitem": [0],
"relat": [[0,1,2,5,6,7]],
"aggiunta": [0,[4,6]],
"console-stat": [2],
"trattati": [0],
"sfoglia": [7],
"ingreek": [0],
"restringer": [3],
"lunch": [0],
"disattiva": [0],
"crearlo": [2],
"f12": [7],
"eccetto": [0],
"convert": [[0,2,4,7]],
"disattivo": [4],
"attempt": [7],
"division": [0],
"projectexitmenuitem": [0],
"ricercar": [[0,1],[3,4,5]],
"contenut": [7,[2,5,6]],
"manterrà": [5],
"text": [7,2,1],
"editregisteruntranslatedmenuitem": [0],
"premi": [3],
"init": [2],
"all\'indirizzo": [[2,3]],
"punto": [0,2,1,6,[5,7]],
"misspel": [0],
"contattar": [5],
"made": [7],
"istantaneament": [6,2],
"manag": [[2,7]],
"manifest.mf": [2],
"disponibil": [2,1,0,4,[3,7]],
"maco": [0,2,4,5,3,1],
"field": [7],
"attraverso": [2],
"frattempo": [2],
"perder": [3],
"doc": [7,0],
"doe": [7],
"senso": [0,1],
"output-fil": [2],
"aiutare": [8],
"particolari": [[0,1]],
"successo": [2],
"status": [3],
"server": [2,1,6,5],
"un\'altra": [[0,2]],
"paramet": [7,2],
"siano": [2,1,[0,4],[3,5,6,7]],
"piacimento": [6],
"stamp": [6],
"run-on": [0],
"incluso": [2,[1,6],[0,4,7]],
"sottostant": [0,2],
"calcolati": [1],
"bisogna": [2],
"leggi": [[0,3]],
"dividerà": [[0,7]],
"mai": [0,3,[1,4,6]],
"mantieni": [[0,7]],
"affatto": [0],
"inizial": [2,7,[0,1]],
"man": [[0,2]],
"map": [2,6],
"punti": [0,[5,7]],
"may": [7],
"ripeti": [3],
"url": [2,1,6,[0,3,7]],
"megabyt": [2],
"uppercasemenuitem": [0],
"calcolata": [[1,5]],
"viewmarkuntranslatedsegmentscheckboxmenuitem": [0],
"contestuali": [5],
"direzional": [4,0],
"desidera": [0,7],
"needs-review-transl": [0],
"tagwip": [7,3],
"bisogno": [[4,7]],
"flessibilità": [3],
"usa": [3,0,1,[2,4],7,5,6],
"lungo": [0],
"usb": [2],
"use": [7,[1,2]],
"usd": [7],
"usi": [[0,3]],
"reperir": [2],
"main": [7],
"dell\'interfaccia": [2,[1,3]],
"scarica": [2,[0,4]],
"uso": [[2,4],7,[0,3,8],[1,5]],
"lavorando": [3],
"omegat.jar": [2,0],
"filtrandoli": [3],
"strip": [7,3],
"omegat.app": [2,0],
"conveni": [7],
"usr": [[0,1,2]],
"ottenerlo": [2],
"logo": [0],
"combina": [2],
"assicurarsi": [2,[0,5,7]],
"creazion": [4,[6,7],1,[0,3]],
"progressi": [[0,2]],
"alter": [7],
"iniziar": [3,[0,2]],
"lista": [8,2],
"libro": [3],
"utf": [0,6],
"domand": [0],
"ogni": [[0,2],6,4,[1,3],5],
"ferma": [1],
"deposito": [2,[4,6,7],[1,5]],
"includer": [[0,2],[3,5]],
"veder": [2,4,0,7,1,3,[5,6]],
"separator": [0],
"simbolici": [2],
"feed": [0],
"inclusi": [2,0,[3,5,6,7]],
"cleanup": [7],
"originata": [3],
"lunga": [0,1],
"descritt": [[0,2]],
"apposto": [2],
"aggiungendo": [0,[3,7]],
"esci": [[0,4]],
"inclusa": [0],
"dsl": [6],
"staccato": [1],
"tabulazion": [0],
"definibili": [7],
"strumenti": [2,1,7,[0,4,6],8,3],
"vecchio": [2,1],
"prend": [3],
"vecchia": [2],
"med": [4,0],
"strumento": [[2,3,4,7,8]],
"salvataggio": [1,2,4,[6,8]],
"dtd": [[0,2]],
"abilitato": [5,[1,3]],
"repeat": [7],
"nuov": [0,[3,4],[6,7]],
"regolar": [0,2,[1,5,6]],
"tentar": [2],
"make": [7],
"fanno": [2,0],
"sovrascritta": [2,4],
"comprimi": [0],
"projectcompilemenuitem": [0],
"classnam": [2],
"console-transl": [2],
"sovrascritti": [2,3,[0,1]],
"spaziatura": [1],
"recenti": [[2,4]],
"entrambi": [2,1],
"optionsautocompletehistorycompletionmenuitem": [0],
"due": [2,0,3,[1,4],[5,6,7]],
"gotonextuniquemenuitem": [0],
"messaggi": [5,0],
"conform": [[3,7]],
"grafi": [0],
"ricevono": [1],
"wordart": [0],
"istruito": [6],
"talvolta": [2],
"attiv": [0,[1,5]],
"inform": [7,2],
"ignorerà": [6],
"depend": [7],
"about": [0],
"commit": [2],
"targetlocalelcid": [0],
"project_stats_match.txt": [[4,6]],
"freccia": [[3,5],0],
"tab-separ": [0],
"provvist": [0],
"revisor": [[2,6]],
"miglioramenti": [[0,4]],
"permettendoti": [3],
"azzerar": [1],
"benefit": [7],
"esigenz": [1,[3,5]],
"esattament": [0,2],
"figurarsi": [0],
"ripeter": [2],
"glifi": [4],
"libreoffic": [3,0],
"male": [3],
"autocompleterclos": [0],
"qualiti": [4],
"opzioni": [2,0,7,4,3,1,[5,8]],
"eliminato": [[1,3]],
"pacchetto": [2,4],
"scan": [7],
"convertitori": [2],
"entramb": [0,[1,2,7]],
"protetti": [[1,3]],
"eliminati": [2,0],
"long": [0],
"devono": [0,2,7,1,[5,6]],
"into": [7],
"mio": [2],
"rifletter": [4],
"defin": [0,7,[1,6]],
"supporto": [2,0,[3,4,8]],
"sottolineatura": [0],
"progettato": [3],
"expression": [0],
"mano": [[0,7],[2,3,6]],
"appartengono": [[0,1]],
"l\'analisi": [5],
"viewdisplaysegmentsourcecheckboxmenuitem": [0],
"appear": [7],
"editregisteremptymenuitem": [0],
"appaiono": [1],
"aggiungi": [3,7,[0,4],[1,6],5],
"stats-output-fil": [2],
"mismatch": [7],
"progettati": [2],
"oper": [7],
"open": [7,0,[1,2]],
"pacchetti": [2],
"stai": [3],
"treat": [7],
"project": [7,6,[2,5]],
"predefinito": [1,4,[0,2],6,5],
"取得": [[1,7]],
"xmx1024m": [2],
"visualizzerà": [[1,3]],
"verificarn": [[0,3]],
"tenerla": [3],
"sever": [7],
"loop": [7],
"corredato": [2],
"predefinita": [0,1,5,7,2,4,[3,6],8],
"penalty-xxx": [[2,6]],
"enclos": [7],
"gotonextsegmentmenuitem": [0],
"copiarlo": [2],
"finestra": [4,1,5,[0,3],2,7,6,8],
"look": [7],
"calcolator": [7],
"finestre": [8],
"supporta": [2,[0,6]],
"difficili": [2],
"inseribili": [3],
"dropbox": [2],
"esistent": [2,3,5,[0,4]],
"abort": [2],
"internet": [1],
"descrizion": [4,[0,1]],
"valori": [0,[2,4]],
"comma-separ": [0],
"allow": [7,4],
"saltar": [6],
"affidabile": [8],
"rifletti": [3],
"nell\'editor": [[0,1],[3,4]],
"semplifica": [[1,2]],
"proper": [4,2],
"affidabili": [6],
"printf": [0,1],
"verificato": [2],
"common": [7],
"appli": [7,2],
"influenzati": [4],
"segnalerà": [2],
"uscita": [[0,2]],
"controllar": [0],
"basa": [0,2],
"assegnar": [[0,4],[1,6,7]],
"inserirla": [5,[2,4]],
"percorso": [2,0,1,[4,5,7]],
"writabl": [7],
"registro": [0,4,6],
"informatico": [2],
"layout": [0],
"andranno": [0],
"registri": [0],
"sincronizzato": [2],
"registra": [[0,1,2,4]],
"sincronizzati": [1],
"bash": [[0,2]],
"step": [7],
"tmroot": [0],
"manualment": [2,7,[0,3,4,6]],
"mark": [7],
"base": [0,5,2,7,[1,3,4,8]],
"registr": [0],
"riserva": [[0,2]],
"collegata": [6],
"distinguern": [4],
"nazionalità": [1],
"whole": [7],
"collegato": [[3,4,6]],
"collegati": [[2,6]],
"allin": [1],
"automatica": [1,4,[0,5,6,7],2,[3,8]],
"大学": [1],
"successivament": [3],
"facendo": [4,[1,5,6]],
"automatich": [5,2,[1,3]],
"automatici": [[0,4]],
"verificano": [5],
"insertcharslr": [0],
"vai": [0,5,3,4,[2,6,8],1],
"automatico": [0,1,3,[2,4,5],8,6],
"notifich": [5],
"indica": [5,2,[0,1]],
"inserit": [6,[1,2,3,5]],
"still": [7],
"inserir": [0,2,3,6,[1,4],5],
"indice": [8],
"work": [7,[0,2]],
"esplicitament": [2],
"notifica": [5],
"stile": [2],
"bidirezional": [0],
"terminato": [[1,3]],
"risultano": [5],
"loro": [0,2,7,3,[1,5],4],
"attribuzion": [2],
"sincronizzano": [2],
"risultant": [2,4],
"permett": [4,0,2,[1,3,7]],
"indici": [0],
"quotidiano": [0],
"variar": [2],
"word": [7,[0,3]],
"lingue": [1],
"sottolinear": [5],
"lingua": [2,0,1,3,7,6,5],
"utent": [2,0,1,[3,5]],
"essa": [0,2],
"contenga": [[2,3,6]],
"elettronica": [0],
"duemila": [1],
"converti": [3],
"spera": [3],
"includi": [[1,2,6]],
"contener": [6,[2,5],[0,1]],
"vcs": [2],
"sezioni": [[3,5]],
"lingvo": [6],
"developer.ibm.com": [2],
"includa": [2],
"accettati": [2],
"accettata": [2],
"analizzar": [5],
"averag": [7],
"inserendo": [4,3],
"forzati": [6],
"specifico": [2,0,[1,3]],
"ecc": [1,[0,5,6]],
"formular": [0],
"specifich": [7,6,[1,2]],
"specifici": [[0,2],[1,6,7]],
"specifica": [0,2,[1,4],[5,6,7]],
"anch\'essa": [0],
"gusti": [3],
"appar": [4],
"html": [0,2,[1,3]],
"adattarl": [3],
"spell": [7,0],
"variabil": [0],
"disabilit": [7],
"esso": [0,[1,4,6]],
"dell\'intero": [[3,4,6]],
"leggi.mi": [0],
"insertcharsrl": [0],
"essi": [0,[1,2]],
"revision": [[0,2,3]],
"danneggerebb": [1],
"nome_fil": [5],
"sulla": [2,5,[0,1],[3,4],7],
"qualità": [4],
"l\'attributo": [0],
"alcun": [0,4,2],
"avanzati": [0],
"minimo": [6,[0,1,8]],
"risultati": [0,[3,4,5,7],2],
"sullo": [2,0],
"l\'elemento": [[0,4]],
"www.ibm.com": [1],
"avanzata": [0],
"sulle": [8],
"agganciati": [[3,5]],
"risultato": [2,0,1,[3,7,8]],
"opposta": [0],
"minima": [1],
"risultass": [2],
"controllo": [0,2,[1,4],7],
"radunar": [2],
"regolarment": [2,6],
"toolsalignfilesmenuitem": [0],
"conseguentement": [0],
"controlla": [[0,1],4,2],
"agio": [2],
"gravi": [0],
"controlli": [[3,4]],
"migliori": [[1,3,7]],
"improv": [7],
"command": [7,0,4],
"ricordi": [6,[2,3]],
"project-specif": [7],
"convalidar": [4,1],
"miglior": [[1,3,5],[0,2,6]],
"detach": [7],
"tipici": [2],
"quindi": [[0,2,3,5],1],
"documento.xx.docx": [0],
"sottocartell": [2,[0,6],4],
"tag-fre": [7,3],
"considerazion": [1,[4,7],[2,6]],
"volut": [0],
"onecloud": [2],
"incollato": [4],
"viewmarkbidicheckboxmenuitem": [0],
"incollar": [4],
"preferir": [2],
"dell\'oggetto": [0],
"nasconder": [5],
"contenent": [2,4,0],
"punteggi": [6],
"agli": [8],
"informazioni": [5,[0,4],2,1,3,[6,7]],
"concorderà": [1],
"via": [7,6],
"segmentando": [3],
"l\'alternativa": [4],
"ignorino": [0],
"fileshortpath": [[0,1]],
"permiss": [7],
"variabili": [1,0,[7,8]],
"annotati": [3],
"double-click": [7,2],
"日本語": [7],
"combinando": [7],
"esaustiva": [2],
"instruct": [7],
"sinistro": [5],
"verificar": [3,5],
"sottolinea": [4],
"sinistra": [0,4,5],
"notazion": [1],
"version": [2,0,[3,4,6]],
"volta": [2,4,[0,6],3,1,[5,7]],
"folder": [7,5,6,8],
"stop": [7],
"espression": [0,1],
"avanzamento": [0],
"chiederà": [[1,3]],
"esportata": [2],
"detail": [7],
"riesca": [3],
"vista": [5,1,0,[3,4,8],6],
"verificherà": [0],
"projecteditmenuitem": [0],
"temporaneament": [[1,5]],
"least": [[5,7]],
"collaborazion": [2,4,6,[0,3],7,[1,5]],
"configurar": [1,2],
"considerandoli": [2],
"new_word": [7],
"run\'n\'gun": [0],
"riconoscer": [3],
"attivazion": [0],
"modificato": [[1,2,5],[0,3,6]],
"nashorn": [7],
"machin": [[1,7],[2,4,5]],
"colleghi": [[2,5]],
"compariranno": [0],
"unsung": [0],
"forza": [0],
"all\'azion": [3],
"puntator": [5],
"last_entry.properti": [6],
"commetti": [3],
"avvien": [2],
"apertura": [0],
"invok": [7],
"visualizzazion": [1,5,3],
"dovuto": [2],
"contribuir": [3],
"all\'url": [2],
"testi": [[0,7]],
"all\'ultimo": [[0,4,5]],
"bordo": [5],
"dovrà": [[0,1,2,7]],
"autocompleternextview": [0],
"testo": [0,4,1,5,3,7,2,6,8],
"specif": [7],
"provoca": [2],
"l\'applicazion": [2,0,1],
"all\'ultima": [0],
"dsun.java2d.noddraw": [2],
"categorico": [6],
"cambiarn": [0],
"scorciatoia": [0,4,3,5],
"riavvia": [4,[0,1,6]],
"sezion": [[2,3],0,7,1,4],
"liberament": [[0,2,5]],
"need": [7],
"all\'uso": [3,[2,6],[0,4],7,[1,5],8],
"riconosciuti": [0],
"scorciatoie": [8],
"riconosciuto": [[0,4,6]],
"editorfirstseg": [0],
"often": [7],
"x0b": [2],
"richiam": [0],
"contrassegnar": [1,4],
"un\'istanza": [0],
"canada": [[1,2]],
"altern": [7,0,1,[4,5]],
"http": [2,1],
"decimal": [0],
"riconosciuta": [[1,2,5,6]],
"problematici": [3],
"strano": [0],
"nazion": [2],
"carrello": [0],
"significa": [0,[1,2]],
"lisenc": [0],
"rimuovendo": [2],
"linea": [2,[0,3,4]],
"dall\'utent": [0,[5,7]],
"vuot": [0,5],
"softwar": [2],
"lieta": [0],
"projectsinglecompilemenuitem": [0],
"servono": [3],
"end": [[0,7]],
"supportano": [2,1],
"lisens": [0],
"l\'error": [4],
"corrispondano": [3],
"scomparir": [2],
"otherwis": [7],
"prescelti": [2],
"myfil": [2],
"vuol": [2,1,[0,7]],
"env": [0],
"prescelta": [2],
"aggiorna": [6],
"howev": [7],
"vuoi": [3],
"fornit": [2],
"comuni": [2,0,[3,5]],
"special": [0,[2,7]],
"okapi": [2],
"fornir": [2,[0,5,7]],
"stella": [0],
"page_down": [0],
"prestazioni": [3],
"key-bas": [7],
"utilità": [7],
"nell\'esempio": [0],
"prescelto": [2,4],
"copyright": [4],
"contenenti": [[0,6]],
"trattino": [0],
"marchi": [5],
"project_nam": [7],
"system-os-nam": [0],
"occurr": [7],
"insertcharspdf": [0],
"modificano": [2],
"traduzion": [2,3,5,4,0,1,7,6],
"turni": [2],
"specifi": [7],
"heapwis": [7],
"provengano": [6],
"riaperto": [2],
"lingu": [[1,2],[3,6,7],0,4],
"similar": [7],
"selettor": [2],
"tar.bz2": [6],
"invio": [4,1],
"bundle.properti": [2],
"contributors.txt": [0],
"invia": [2,[0,4]],
"driver": [1],
"pertinent": [0],
"coreani": [1],
"www.regular-expressions.info": [0],
"sourcelang": [0],
"nell": [2,[0,4,7],1,5,6],
"numerich": [0],
"formattato": [3],
"parola": [0,[4,6],[1,5]],
"cell": [7,0],
"optionsdictionaryfuzzymatchingcheckboxmenuitem": [0],
"esserci": [5],
"assur": [4],
"insiem": [[0,7],2],
"diffondessero": [2],
"interfac": [2],
"velocizzar": [2],
"assum": [0],
"era": [1],
"eccezion": [1,0,2,3],
"emergenza": [2],
"utilizzerà": [1,2,7],
"sourcelanguag": [1],
"riga": [0,2,1,5,[4,7],6],
"fornendo": [8],
"chiavi": [1],
"gzip": [6],
"helpupdatecheckmenuitem": [0],
"righ": [0,5],
"derivar": [2],
"notic": [7],
"esc": [[4,5],2],
"distribuzion": [2,0],
"durerà": [3],
"exampl": [7],
"l\'etichetta": [1],
"nostemscor": [1],
"ndt": [4,3],
"ess": [0,5,[1,3]],
"finement": [0],
"project_chang": [7],
"traducibili": [0],
"ufficial": [0],
"forniscono": [[0,1,2]],
"scaricamento": [[1,2]],
"osserverà": [2],
"console-createpseudotranslatetmx": [2],
"proposto": [0],
"neg": [4,0],
"nei": [0,7,[1,2],[4,5],6,3],
"nel": [2,4,0,1,5,3,7,6,8],
"suffisso": [2],
"fuzzyflag": [1],
"dottorato": [1],
"modificarl": [[0,3,7]],
"modificarn": [7],
"modificate": [8],
"escap": [0],
"new": [7],
"modificati": [4,[1,2],0],
"rappresenta": [0,1,[2,3]],
"ecco": [1],
"below": [7],
"poisson": [7],
"runway": [0],
"bianchi": [0],
"visualizz": [3,[1,5],[0,4,7]],
"half-width": [7],
"choos": [7],
"allineerà": [2],
"modificata": [3],
"ll-cc.tmx": [2],
"cascata": [1],
"canali": [0],
"invec": [[0,1]],
"therefor": [7],
"benvenut": [3],
"verranno": [0,2,[1,6],3,4],
"avvisi": [2],
"premer": [4,5],
"forma": [0,[1,3,4,7]],
"grunt": [0],
"reload": [7],
"calcul": [7],
"all\'attivazion": [1],
"probabilment": [2],
"sull\'avanzamento": [5],
"meglio": [3,5],
"render": [[0,3],[2,7]],
"potenzial": [0],
"magento": [2],
"crediti": [4],
"chiaro": [4],
"ll.tmx": [2],
"preferisce": [8],
"decider": [[0,6]],
"proposta": [2],
"nell\'uso": [3],
"particolar": [5],
"conservati": [2],
"proposti": [3],
"rilascio": [[4,5]],
"correttor": [[1,6],3,[0,4,7]],
"memorizz": [0,[2,5,7],[1,3,6]],
"sostituirla": [2],
"ll_cc.tmx": [2],
"u00a": [7],
"escluda": [2],
"shift": [0,4,7],
"cert": [2],
"fondamentali": [0],
"professional": [3],
"autenticato": [1],
"java": [2,0,1,7,3],
"pubblicazion": [2],
"xmxsize": [2],
"raggiungi": [3],
"project_save.tmx": [2,6,[3,7],4],
"dictionari": [6,[1,5,7]],
"remain": [7],
"mantenuta": [7],
"studiare": [8],
"rimuover": [[6,7],2,1,0],
"quattro": [0,2,4],
"applicazioni": [[0,2]],
"powershel": [[0,2]],
"eye": [0],
"dictionary": [8],
"contrazioni": [0],
"modificarlo": [0,[3,5,6]],
"clonato": [2],
"neutra": [0],
"dell\'area": [5],
"numero": [0,5,4,[1,2],[6,7],3],
"all\'opzion": [4],
"appl": [0],
"correggerla": [2],
"recommend": [[2,7]],
"numeri": [1,[0,5]],
"l\'avvio": [2],
"default": [7,2],
"alterna": [[0,4]],
"gray": [7],
"dodici": [0,4],
"mantenuto": [0],
"sudo": [2],
"drop-down": [7],
"qualsiasi": [0,2,6,[3,4],7,5,8],
"timestamp": [0],
"perso": [2],
"attributo": [0],
"sottotitoli": [2],
"projectaccessrootmenuitem": [0],
"mappato": [2],
"correntement": [4],
"attributi": [0],
"mappati": [2],
"digitato": [[4,5]],
"correnti": [7,[0,1,4,6]],
"digitati": [2],
"davvero": [3],
"such": [7],
"voler": [0],
"persa": [4],
"plugin": [[0,1],2,8],
"autocompletertableup": [0],
"principi": [[3,5,8]],
"effetti": [[0,2]],
"understood": [7],
"effetto": [0,7,[1,3]],
"incorporato": [2],
"sottolineerà": [1],
"pensata": [0],
"dell\'alfabeto": [0],
"projectcommitsourcefil": [0],
"editinsertsourcemenuitem": [0],
"documento": [0,2,[3,4],[1,5,7,8]],
"apri": [0,4],
"viterbi": [7],
"microsoft": [0,[3,7]],
"provenienti": [6,[2,3,5,7]],
"projectnewmenuitem": [0],
"assistenza": [[2,3,5]],
"ecmascript": [7],
"mappata": [2],
"pulsant": [7,[0,3],[1,5],4,[2,6]],
"documenti": [2,0,3,[4,5,7],[1,6]],
"segment": [7,[1,2,5]],
"un\'abbreviazion": [0],
"changes.txt": [[0,2]],
"imposterebb": [6],
"ignorando": [5,4],
"estrarr": [6],
"glossari": [0,7,5,4,6,[1,3],[2,8]],
"recurs": [7],
"ignored_words.txt": [6],
"accanto": [0,[2,7]],
"impost": [1,[5,7]],
"github.com": [2],
"configuration.properti": [2],
"duplicata": [2],
"contrassegnato": [1,0,[3,5]],
"richiederà": [2],
"autocompleterlistpageup": [0],
"sostitu": [7],
"giro": [3],
"duplicati": [7],
"glossary": [8],
"contrassegnati": [1,0],
"apra": [2],
"occupi": [7],
"duplicato": [0],
"incorporata": [7],
"segmentati": [0,3],
"formattazioni": [[3,5,7]],
"aiuterà": [2],
"occupa": [3],
"formattazione": [8],
"relazion": [[1,3]],
"next": [7],
"rigenera": [3],
"string": [7,2],
"import": [5],
"color": [6],
"personalizza": [[1,4]],
"non": [0,2,1,4,3,7,5,6],
"dizionario": [1,[3,6],4,5,0],
"tastiera": [[0,4,5]],
"button": [7],
"not": [7,4],
"eseguono": [1],
"scritti": [[0,6]],
"colpo": [4],
"legali": [0],
"applicabili": [6,0],
"scritto": [2],
"greek": [0],
"nessuna": [4,[0,1]],
"rinominando": [2],
"green": [7],
"significanti": [0],
"dall\'estension": [2],
"was": [7,0],
"invertit": [1],
"tastier": [0],
"impediranno": [2],
"viewrestoreguimenuitem": [0],
"rispecchi": [2],
"selection.txt": [[0,4]],
"nessuno": [7],
"way": [7],
"xhtml": [0],
"hanno": [2,[0,4],[3,5]],
"preferibil": [2],
"itoken": [2],
"what": [7],
"finder.xml": [[0,6,7]],
"refer": [7],
"selezionar": [4,1,[2,5],0,7],
"sequenza": [0,[3,4]],
"window": [7,0,2,4,5,3],
"classi": [0,8],
"spuntar": [1],
"discard": [7],
"utilizz": [0,3,7,[2,6]],
"gestito": [0],
"ampiament": [2],
"allineando": [3],
"criteria": [7],
"farla": [0],
"disable-project-lock": [2],
"omegat.pref": [[0,1,7]],
"when": [7,[1,5]],
"decidi": [3],
"fai": [3],
"farlo": [0,[1,4]],
"txml": [2],
"far": [2,0,6],
"carriage-return": [0],
"all\'elenco": [0],
"multipl": [5,0,3],
"rigido": [2],
"copiandol": [2],
"greco": [0],
"cambiano": [0],
"diventa": [6],
"lavorar": [2,0,[3,6]],
"traduttori": [2,3,[0,5]],
"confermar": [4],
"italiano": [0,[1,2]],
"disposizion": [5,3,1,4],
"contrassegn": [1],
"suit": [2],
"personalment": [3],
"automaticament": [[0,2,4,6],1,7,3,5],
"vedi": [3],
"widget": [5,8],
"determinar": [[0,1]],
"statistich": [4,6,1,[0,2],7],
"presenterà": [3],
"impedisca": [7],
"statistici": [6],
"navigazion": [[3,4,5],6,2],
"modelli": [0,[1,2,7]],
"portion": [7],
"direct": [0,[2,4,7]],
"manutenzion": [2],
"manipolazion": [5],
"scovar": [7],
"modello": [0,1,8,[2,7]],
"riportar": [0],
"statistica": [7],
"individuerà": [0],
"sull": [0,[3,5],7],
"web": [1,7,2,[0,5]],
"en-us_de_project": [2],
"proprietà": [5,2,[0,3],4,6,7,1,8],
"symlink": [2],
"approccio": [2],
"veda": [7,0,[1,4]],
"essersi": [2],
"nth": [7],
"editselectfuzzy4menuitem": [0],
"editregisteridenticalmenuitem": [0],
"evidenziata": [5],
"usat": [4],
"aperta": [[0,2,4,7]],
"traducono": [1],
"disattivando": [5],
"hanja": [0],
"condizioni": [2],
"contrassegnano": [[0,8]],
"aperti": [[1,4]],
"troppo": [2],
"personalizzazion": [0,4],
"risposta": [5],
"aperto": [2,4,[0,5]],
"respint": [1],
"usar": [0,[1,2],5,3,7,[4,6]],
"caricherà": [2],
"intestazion": [0],
"left-hand": [7],
"evidenziati": [4,0],
"advanc": [7,1],
"tentativo": [2],
"suoi": [2,5],
"situazioni": [2],
"promemoria": [[0,3,4]],
"derivanti": [6],
"l\'elaborazion": [1],
"prendono": [1],
"dict": [1],
"few": [7],
"disabilita": [[1,2]],
"giornaliero": [2],
"traducendo": [7,2,[1,3]],
"mostrerà": [[1,2,5]],
"cartella_config": [2],
"keep": [7],
"gestire": [8],
"risoluzione": [8],
"option": [7,1,[0,4]],
"who": [7],
"esportati": [7,6],
"difettoso": [2],
"riprodurr": [7],
"contenuto": [0,3,2,[1,6],7,[4,5],8],
"conoscenza": [2],
"esportato": [4],
"visibil": [[0,6]],
"piattaform": [2,[0,1]],
"verament": [3],
"contenuti": [2,5,6,0],
"sopraccitato": [1],
"comunqu": [0,[2,7]],
"deselezionar": [[1,7]],
"analizza": [0],
"decompression": [2],
"contenuta": [6],
"oppur": [0,3,[2,7],[5,6],[1,4]],
"gestisc": [1,2],
"puntini": [4],
"interpretati": [0],
"decomprimerlo": [2],
"aggiornamento": [1,2],
"cercherà": [1],
"mantien": [[1,6]],
"dimenticarlo": [1],
"ricrear": [[0,2]],
"terminal": [2],
"processo": [3,0,2,7],
"esplorar": [0],
"various": [7],
"singolar": [3],
"gestiti": [0,[2,3]],
"aggiornamenti": [1,2,[0,4,8]],
"visiv": [[0,3]],
"user": [7],
"sostituisci": [0,4,[1,7],5,3],
"proxi": [2,1],
"extens": [0,7],
"back_spac": [0],
"perda": [[2,3,5]],
"migliaia": [0],
"disattivata": [[1,4],0],
"definizion": [0],
"richiama": [0],
"bring": [7],
"allinea": [7,4,[0,2,3,8]],
"recalcul": [7],
"all\'utilizzo": [5],
"robot": [0],
"all\'original": [[1,4]],
"risolti": [[1,2]],
"riavviar": [2,0],
"despit": [7],
"eclips": [2],
"ad": [2,[0,7],3,[1,4,6]],
"diventino": [0],
"sure": [7],
"ramo": [2],
"ag": [[0,4],[1,2,5]],
"tabulazioni": [0],
"presenza": [0,[1,2]],
"ai": [2,0,4,[3,6],5,1,7],
"d\'ambient": [0],
"diff": [1],
"al": [0,2,4,3,5,6,1,7],
"automat": [7,1],
"esclusi": [2],
"an": [0,7],
"editmultiplealtern": [0],
"extend": [7],
"aprirà": [[2,5]],
"proxy": [8],
"ottenuto": [1],
"as": [7,2],
"predefin": [0,1,4],
"at": [7,5],
"esclusa": [2],
"indipendent": [0],
"stilistici": [0],
"convenzion": [0],
"eseguibil": [[0,2]],
"identificati": [3],
"trasferir": [2],
"be": [7],
"nell\'elemento": [0],
"simultaneament": [1],
"salta": [0],
"filters.xml": [0,[1,2,6,7]],
"elaborar": [2],
"br": [0],
"l\'url": [2,[1,4]],
"search": [7],
"necessita": [[0,1]],
"by": [7,[2,5]],
"potenziali": [[0,1,4,6]],
"segmentation.conf": [[0,2,6,7]],
"identificato": [[1,2]],
"combinazion": [0,4],
"panel": [7],
"nell\'espression": [0],
"ca": [2],
"cc": [2],
"l\'avanzar": [6],
"ce": [2],
"ci": [2,4,3,[0,1,6]],
"spazio": [0,3,[1,4,5],7],
"leggimi.txt": [0],
"cr": [0],
"cs": [0],
"associazion": [2],
"indietro": [4,[0,2,3,5],[1,6,7]],
"l\'uso": [0,[1,2],5],
"memorizza": [[0,2,3]],
"condivis": [7,0],
"apach": [2,7],
"da": [0,2,6,3,4,1,7,5,8],
"adjustedscor": [1],
"provi": [2],
"de": [1],
"di": [0,2,1,4,3,7,5,6,8],
"extern": [7],
"f1": [[0,4,7]],
"do": [7],
"f2": [[3,5],[0,7]],
"f3": [[0,4],5],
"principalment": [2],
"bidirezionali": [0],
"f5": [[0,3,4]],
"ragioni": [[0,1,2,7]],
"chiusura": [0,7],
"dz": [6],
"editundomenuitem": [0],
"possibile": [8],
"raro": [2],
"possibili": [0,2],
"ed": [4,[0,2,3],1,[6,7]],
"assiem": [3],
"which": [7],
"insecabili": [0],
"avviati": [1],
"belazar": [1],
"en": [0,2],
"preceder": [1,0],
"carri": [7],
"dovess": [0],
"es": [[0,1],2,7],
"avviata": [2],
"eu": [4],
"mediant": [[0,5]],
"dall\'interno": [[0,2]],
"minuscol": [0,1],
"ripetuti": [1,[4,7],0],
"activ": [4],
"first-class": [7],
"fa": [0,4,1],
"aiutar": [0],
"operazion": [2,[3,6]],
"cambiarla": [2],
"avviato": [2],
"fog": [0],
"aggiuntivo": [0],
"indic": [0],
"aggiuntivi": [0,1,2],
"vocal": [0],
"origin": [4,[1,7],[0,5],3],
"riconosc": [[2,3]],
"rosso": [6,[0,1]],
"for": [7,[0,2],[4,5]],
"exclud": [7,2],
"cambiarlo": [8],
"fr": [2,1],
"necessitano": [0,[2,3]],
"esport": [2,[0,3,4,7]],
"content": [7,[0,2],1],
"continueranno": [0],
"aggiuntiva": [2],
"duckduckgo": [1],
"spiegazion": [[0,1]],
"desktop": [2],
"appunti": [4],
"rossi": [1],
"applescript": [2],
"necessità": [[0,2,6],3],
"foss": [0],
"json": [2],
"gb": [2],
"class": [0],
"helplogmenuitem": [0],
"rossa": [1],
"gg": [2],
"presenta": [[0,3]],
"decorazioni": [0],
"editoverwritetranslationmenuitem": [0],
"licenza": [2,0,[4,8]],
"digitar": [7,[2,3]],
"conserva": [[2,6]],
"presenti": [2,7,[0,1,5],[3,4]],
"dalla": [2,0,4,6,3,5,1,7],
"elaborator": [3],
"counter": [7],
"aeiou": [0],
"anglosasson": [0],
"form": [7],
"sbloccarlo": [5],
"ha": [1,[3,5],4,[0,2,7]],
"restor": [7],
"fort": [0],
"dallo": [[0,2,3]],
"dà": [[1,4]],
"assign": [7],
"aiuto": [0,[2,4,8]],
"hh": [2],
"duser.languag": [2],
"viewmarkparagraphstartcheckboxmenuitem": [0],
"completo": [0,1,2],
"dichiarata": [0],
"convalida": [0,[1,4]],
"repetit": [7],
"commerciali": [3],
"veri": [7],
"file-target-encod": [0],
"fra": [7],
"verd": [5,4],
"malament": [7],
"collaboratori": [0],
"mainmenushortcuts.mac.properti": [0],
"funzionerà": [1],
"id": [1,0,7],
"https": [2,1,0,[5,6]],
"vero": [2],
"impedir": [2,4,[0,3]],
"if": [7,2,[1,5]],
"project_stats.txt": [6,4],
"french": [7],
"non-ascii": [0],
"ocr": [7],
"scorrer": [0,[1,3,5]],
"projectaccesscurrenttargetdocumentmenuitem": [0],
"il": [2,0,1,4,5,3,7,6,8],
"riconosca": [0,3],
"in": [2,0,7,4,3,1,5,6,8],
"termin": [[1,4],5,3,0,7],
"ip": [2],
"lower": [7],
"index": [0],
"is": [7,0,2],
"it": [7,2,[1,3]],
"riprendet": [7],
"codici": [0,7,3,2],
"racchiuso": [0,1],
"projectaccesstmmenuitem": [0],
"odf": [0],
"riprender": [3,[2,6]],
"esser": [0,2,1,4,7,[3,6],5],
"ja": [1],
"glossario": [0,5,4,[1,3,6,7],2],
"odt": [[0,7]],
"gotonexttranslatedmenuitem": [0],
"charset": [0],
"librari": [0],
"esatto": [[0,2]],
"precedenza": [3,0,[2,4,6],[1,7]],
"toolscheckissuescurrentfilemenuitem": [0],
"libraries.txt": [0],
"learned_words.txt": [6],
"robusto": [3],
"esatta": [3],
"codifica": [0,8],
"dunqu": [0,2,3],
"rintracciar": [2],
"robusta": [2],
"codifich": [0],
"ftl": [[0,2]],
"abilita": [1],
"acced": [[1,2,4,5]],
"incapsulati": [7],
"possied": [0,2,[4,6]],
"trasformano": [0],
"viewdisplaymodificationinfoallradiobuttonmenuitem": [0],
"compless": [0],
"off": [7],
"completa": [[0,2]],
"visivament": [4],
"la": [0,2,4,[1,3],5,7,6,8],
"registrazion": [[1,2]],
"le": [0,2,1,4,3,7,5,6,8],
"lf": [0],
"li": [[3,5],[0,4],2],
"ll": [2],
"coincidono": [6,2],
"lo": [0,2,1,[4,6],5,[3,7],8],
"ripristinerà": [2],
"lu": [0],
"salva": [4,2,6,[0,1],3],
"while": [7],
"genere": [[0,1,2],5,7],
"ja-jp.tmx": [2],
"second": [7],
"that": [7,0,[1,4]],
"cycleswitchcasemenuitem": [0],
"personalizz": [2,0],
"ma": [0,2,3,[1,6],[4,7],5],
"mb": [2],
"sufficient": [0],
"than": [7,5],
"limit": [0,[3,6]],
"me": [2],
"precedenti": [0,4,[3,5]],
"entra": [1],
"terza": [[0,2,6]],
"singolo": [0,[5,7]],
"esistenti": [2,[1,6,7]],
"porzioni": [0],
"mm": [2],
"stringh": [0,3,2],
"visibili": [0],
"entri": [7],
"ms": [0],
"stringa": [[1,4],0],
"author": [7],
"mt": [6],
"toggl": [7],
"entro": [3],
"esigenze": [8],
"my": [0,2],
"license": [8],
"disk": [7],
"appartenenti": [1],
"propongono": [2],
"responsabil": [4,2],
"ne": [2,3,[0,1,4]],
"updat": [7],
"licenss": [0],
"no": [7,0,[1,2,6]],
"code": [[0,7]],
"copiata": [4],
"richiedono": [7,[0,1,2,3,5]],
"gotohistoryforwardmenuitem": [0],
"produrrà": [0],
"head": [0],
"l\'uscita": [4,1],
"l\'equivalent": [2],
"project_save.tmx.timestamp.bak": [6],
"dialog": [7],
"racchiusi": [0],
"conterà": [1],
"of": [7,[0,6]],
"copiato": [[0,1,4],[5,7]],
"racchiusa": [0],
"ok": [7,4,3],
"copiati": [2,[4,7]],
"on": [7,2],
"abilità": [2],
"or": [7,0,[2,4]],
"concordanza": [4,0,1,5,6,2,3],
"ricreato": [6],
"singoli": [0,1],
"aggiornarlo": [6],
"concordanze": [8],
"l\'azion": [[3,7]],
"singola": [0,3],
"encod": [7],
"editinserttranslationmenuitem": [0],
"compressi": [6],
"fileextens": [0],
"determina": [[1,2]],
"collegamento": [1,[0,2]],
"collegamenti": [0,[2,3,5]],
"acquistano": [0],
"nulla": [[3,4,7]],
"po": [[0,2],1,[3,4,5,6]],
"lì": [[0,1,2]],
"invisibili": [0],
"inclus": [[0,7]],
"fornisc": [0,2,[1,3,7]],
"qa": [7,4],
"maiuscoli": [0],
"autocompletertablefirst": [0],
"necessari": [2,0,[3,6,7]],
"venga": [0,[2,3,6]],
"ciano": [4],
"maiuscola": [0,4],
"nascost": [5],
"recent": [4,[0,2],7],
"they": [7,0],
"fornito": [2,[1,4]],
"github": [2],
"edit": [7],
"forniti": [[1,2]],
"editselectfuzzy5menuitem": [0],
"bilingu": [7,[2,6]],
"them": [7],
"then": [7,1],
"maiuscolo": [4,0],
"espressioni": [0,[1,7],8,2,3],
"rc": [2],
"ritieni": [3],
"includ": [7,2,0,5],
"nell\'applicazion": [4,2,3],
"allora": [[2,7]],
"discrepanz": [4],
"revisionare": [8],
"né": [0,2],
"t0": [3],
"t1": [3],
"t2": [3],
"spiegazioni": [7],
"preferenz": [4,0,7,5,[1,2],6,3],
"memorizzata": [[3,6]],
"t3": [3],
"massimo": [0],
"revisionato": [3],
"tedesca": [1],
"abbassati": [6],
"accesso": [1,0,[2,8],[3,4,7]],
"grammatical": [7],
"definiscono": [0,1],
"sc": [0],
"adiacent": [5],
"navigar": [[3,5]],
"se": [2,0,4,1,5,[3,7],6],
"segno": [0,5],
"si": [2,0,4,1,7,5,6,3,8],
"sl": [2],
"posizionar": [7],
"so": [7],
"caution": [7],
"massima": [[0,2]],
"chiuder": [[0,2]],
"apart": [7],
"exported": [8],
"su": [2,0,4,1,3,[5,6],7],
"intero": [5,[0,2]],
"creato": [2,[6,7],0,[1,3]],
"intern": [[2,4]],
"quadr": [0],
"creati": [0,[3,6],[4,5,7],2],
"onc": [7],
"elaborazion": [1,4,3],
"norma": [2],
"one": [7,5,2],
"lilla": [4],
"ripristinar": [[1,6]],
"interv": [1,0],
"riapertura": [2],
"editoverwritesourcemenuitem": [0],
"trasformarla": [0],
"omegat.autotext": [0],
"kilobyt": [2],
"te": [3],
"revisionata": [3],
"intera": [0],
"enforc": [6,4,[0,2],[1,3]],
"ti": [3],
"remov": [7,2],
"gestisca": [[0,2]],
"tl": [2],
"tm": [6,2,4,1,[0,7],[5,8],3],
"to": [7,2,1,[0,4]],
"v2": [2,1],
"interi": [1],
"scrivibil": [4,6,[0,7],[2,5]],
"solament": [2,1,[0,7]],
"separati": [0,2],
"tu": [3],
"dialogo": [[1,3],[0,2,4],7,[6,8]],
"ostica": [3],
"trova": [0,2,5,[3,4],[1,7]],
"insensibil": [0],
"separato": [1,[0,6]],
"viewmarkautopopulatedcheckboxmenuitem": [0],
"dover": [[0,3,5]],
"incontrata": [3],
"projectwikiimportmenuitem": [0],
"countri": [2],
"dall": [[2,4],7,[1,5,6]],
"singol": [[0,3]],
"quali": [0,[2,3],[4,6,7]],
"un": [0,2,3,1,4,6,5,7,8],
"facendovi": [7],
"up": [7,0],
"ricordati": [3],
"ottener": [7,[2,3,5],[0,1]],
"partway": [7],
"newword": [7],
"modificabil": [0,5,[2,7]],
"traducibil": [0,7],
"caricamento": [0],
"this": [7,1,[0,2,5]],
"va": [[0,1]],
"puoi": [3],
"semplicement": [0,7,[2,3]],
"consegnar": [3],
"iniziano": [0],
"opt": [2,0],
"vi": [0],
"extract": [7,1],
"nascond": [[0,7]],
"considerar": [[0,1,2]],
"sostituirvi": [1],
"separata": [2,[4,5]],
"un\'ora": [3],
"know": [0],
"persino": [[0,2],[3,7]],
"allinterno": [0],
"region": [0],
"trovi": [3,2],
"vs": [1],
"support": [7],
"changed": [1],
"accertarsi": [2,1],
"l\'angolo": [5],
"vertical": [0],
"pure": [[0,2],6],
"dettag": [[2,4],0,7,1,3,6,5],
"we": [0],
"ortografici": [1,3,[0,4,8]],
"identificar": [0,[1,2]],
"autocompleterlistup": [0],
"ortografico": [1,6,[3,4],[0,7,8]],
"capacità": [1],
"riescano": [5],
"licenc": [0],
"sé": [0,[2,6,7]],
"partenza": [0,[1,2],4,5,7,3,6,8],
"sì": [[0,2]],
"omegat.project.bak": [2,6],
"repo_for_omegat_team_project": [2],
"ora": [[0,2,5],3],
"choic": [7],
"ortografia": [[1,4,7]],
"projectaccessexporttmmenuitem": [0],
"licens": [2,0],
"org": [2],
"d\'interruzion": [1],
"distribut": [7],
"ortografica": [1,3,2,[6,7]],
"divisi": [[0,3]],
"accessori": [2],
"sottratta": [6],
"superior": [[0,1,5]],
"segu": [2,0],
"iniziali": [0,7],
"appendici": [0,[3,6,8]],
"diviso": [5],
"xx": [0],
"sourc": [7,2,6,4,[0,3,5]],
"passaggio": [2,[0,6],7],
"sostituit": [1],
"supportato": [2,[3,6]],
"aprir": [2,0,4,[3,6],[5,7]],
"indirizzo": [0,2],
"inizialment": [1],
"sostituir": [2,[3,5],[0,1,6]],
"type": [7,2,[0,6]],
"indirizzi": [[1,5]],
"supportati": [2,7,[1,3,4]],
"optionsautocompletehistorypredictionmenuitem": [0],
"projectaccesssourcemenuitem": [0],
"yy": [0],
"simbolo": [0,2,5],
"adattar": [1],
"concepito": [3],
"sovrascriver": [6,[2,5]],
"sensibil": [0],
"method": [7],
"simboli": [0,5],
"creano": [4,7],
"nome": [0,2,1,[3,5],6],
"come": [0,2,1,4,[3,5,6,7],8],
"quant": [[0,2]],
"usarla": [2],
"nomi": [0,[5,6],[2,3]],
"ott": [1],
"push": [2],
"testuali": [7],
"installa": [1,3,2],
"concepita": [[2,6]],
"exist": [7],
"readme_tr.txt": [2],
"penalti": [6],
"abituati": [4],
"un\'intestazion": [4],
"exact": [7],
"attualment": [4,0,5,[1,2,7]],
"immagini": [0],
"tanti": [2],
"utf8": [0,[4,7]],
"copi": [2,7,[1,6]],
"facendoli": [0],
"out": [7],
"statist": [7],
"packag": [2],
"accur": [[6,7]],
"power": [7],
"virgol": [[0,1]],
"context_menu": [0],
"ulterior": [[2,6]],
"editsearchdictionarymenuitem": [0],
"intervento": [2],
"ospit": [2],
"tag-valid": [2],
"scriver": [[0,2,4],5],
"ufficiale": [8],
"lunghi": [1],
"trovarla": [3],
"help": [2,[0,7]],
"dovrebb": [[2,4],[0,1]],
"multipiattaforma": [2],
"giorno": [2,0],
"dell\'host": [2],
"repositori": [2,6],
"illimitata": [0],
"date": [[0,7]],
"depos": [[0,2,5]],
"data": [1,[0,2],[3,6,7]],
"ondulata": [1],
"lascia": [5,[1,6]],
"lowercasemenuitem": [0],
"tabell": [0],
"wiki": [[2,6]],
"own": [7],
"vecchi": [2,7],
"autocompleterconfirmwithoutclos": [0],
"separ": [7,3,[0,1,2,5]],
"definizioni": [[0,1]],
"cose": [3],
"ripiego": [4],
"cosa": [2,0],
"filepath": [1,0],
"dato": [2,[0,1,3]],
"arbitrario": [2],
"dati": [2,4,1,7,6,3],
"creata": [2,[0,1,3,5]],
"replac": [7],
"ja-jp": [2],
"richiamar": [4,[0,5],[2,3]],
"apriranno": [1],
"like": [7],
"venir": [2,[0,7]],
"tipicament": [0],
"altrimenti": [6],
"quasi": [[2,4]],
"paesi": [1],
"here": [7],
"note": [5,3,0,4,7,8],
"concreta": [7],
"noti": [0],
"line": [7,0],
"memorizzati": [[1,5],[0,2,4]],
"memorizzato": [4,0],
"hero": [0],
"organizz": [0],
"git": [2,6],
"taglia": [5],
"contributor": [7],
"rendendon": [6],
"estensioni": [2,0,[3,8]],
"confronta": [7],
"disabilitar": [7,0],
"continuar": [2,3],
"creare": [8],
"nota": [2,4,0,7,1,[3,5],6],
"xx-yy": [0],
"evitiamo": [0],
"avviar": [2,[0,3,5,7]],
"will": [7],
"consideri": [1],
"superiori": [3],
"opzionali": [2],
"follow": [7,0],
"racchiudendo": [0],
"estension": [0,2,6,4],
"quella": [0,2,7,5,[1,3,6]],
"targetlang": [0],
"frase": [0,3,7,1,[2,4]],
"alcuna": [2,4],
"iniziato": [3,2],
"alcuno": [[0,1]],
"frasi": [0,3,7],
"estender": [2],
"quelli": [3,[0,2],[1,4,5,7]],
"optionssetupfilefiltersmenuitem": [0],
"quello": [2,[0,1],3,5,4],
"intend": [0],
"inversa": [2],
"wild": [7],
"tradurre": [8],
"alcuni": [0,2,[1,3],[4,5,6],8],
"altgraph": [0],
"remoti": [6],
"ultim": [6,[0,4]],
"esterna": [[0,1,4,7]],
"stats-typ": [2],
"valuta": [0],
"eccezioni": [[1,2]],
"sottomenu": [[2,7]],
"remota": [2],
"esterni": [0,1],
"your": [7,2],
"moderni": [2],
"elimin": [2,1],
"garantita": [2],
"avevi": [3],
"without": [7,2],
"esterne": [8],
"esterno": [[1,2]],
"these": [7],
"basta": [[2,3],6],
"rubrica": [6],
"xml": [0,2,1],
"remoto": [2,6,[4,5]],
"popolar": [[1,2],6],
"immaginati": [3],
"ultimo": [2],
"serv": [2],
"gli": [0,2,1,4,5,3,[7,8]],
"ultimi": [4],
"neutral": [0],
"connession": [1,[2,5]],
"debol": [0],
"insensit": [7],
"ultima": [1],
"xdg-open": [0],
"befor": [7,2],
"util": [[0,2],4,[1,5,7]],
"ambigui": [0],
"seri": [1,0,[2,3]],
"tar.bz": [6],
"già": [2,[0,1,3],[4,6]],
"registrar": [[1,2,5]],
"l\'autenticazion": [2],
"chieder": [2,5],
"interessanti": [0],
"shebang": [0],
"doppi": [0],
"sopra": [2,0,[1,4],5,3,[7,8]],
"editorskipprevtoken": [0],
"differenti": [2,[1,4]],
"prevenir": [2],
"giù": [0],
"ogniqualvolta": [[2,3,6]],
"agiscono": [0],
"compilar": [4],
"alternar": [5],
"regolarn": [7],
"rovesciata": [0],
"sett": [0],
"regolari": [0,2,[1,7],8,3],
"rinominati": [[0,3]],
"così": [0,[1,3],2],
"aaaa": [2],
"gnu": [2,8],
"direzion": [0,5],
"blue": [7],
"associarn": [3],
"suzum": [1],
"target.txt": [[0,1]],
"goe": [7],
"pratica": [2],
"temurin": [2],
"trasforma": [0],
"livello": [7,0,3,1],
"divisibili": [4,0],
"standard": [2,[0,3],[1,4,5,7]],
"d\'espac": [2],
"nell\'intestazion": [0],
"livelli": [3],
"stdout": [0],
"correct": [7,2],
"wish": [7],
"permettendo": [1],
"nameon": [0],
"gotonextnotemenuitem": [0],
"determinato": [[0,1]],
"azion": [5,[2,4],[0,1,6,7]],
"area": [5,[2,7]],
"gpl": [0],
"accettar": [6],
"imbatti": [3],
"newentri": [7],
"risied": [2],
"edizioni": [2],
"list": [7],
"determinati": [[1,2,4,5]],
"aggiunt": [0,3,[2,4,5,7]],
"autocompleterprevview": [0],
"success": [6,[0,1,2]],
"aggiung": [2,6,[0,1]],
"traduci": [[0,3]],
"avess": [0],
"riavviato": [2,0],
"prosieguo": [2],
"interfacc": [[2,4]],
"regional": [0,2],
"formato": [2,0,[1,3,6],5,[4,7],8],
"locali": [7,1,[0,2],[4,5]],
"qualvolta": [3],
"meccanismi": [[0,2]],
"formati": [2,0,[1,3,4],[5,7,8]],
"projectcommittargetfil": [0],
"determin": [7,[0,1,2,4]],
"formata": [0],
"divider": [4],
"combin": [[0,1,7]],
"po4a": [2],
"presentazion": [0],
"japonai": [7],
"omegat.org": [2],
"menus": [7],
"realign": [7],
"object-ori": [7],
"basso": [[0,1,2]],
"direzionalità": [0],
"pannelli": [5,1,3,4,[2,8]],
"voci": [4,0,3,1,[2,5,7]],
"perform": [7],
"pannello": [5,3,4,1,6,7,0,2,8],
"voce": [0,4,5,[1,3,7],6],
"mobili": [1],
"l\'immutabilità": [6],
"maxprogram": [2],
"it-it": [3,1],
"bassa": [5],
"with": [7,2],
"obsoleto": [0],
"pdf": [2,0,4,7],
"there": [7,2],
"sicurezza": [2,6,1,7,0],
"mentr": [2,5,0,[1,3],[4,7]],
"autocompletertabledown": [0],
"descritto": [[0,5]],
"formano": [0],
"utilizziamo": [3],
"editornextsegmentnottab": [0],
"eseguirsi": [0],
"toolsshowstatisticsmatchesmenuitem": [0],
"descritti": [5,[2,3]],
"traduc": [2,[0,1]],
"viewdisplaymodificationinfononeradiobuttonmenuitem": [0],
"viceversa": [7],
"differenziar": [4],
"individueranno": [0],
"avent": [[0,1]],
"tradur": [[2,4]],
"per": [0,2,1,4,3,7,6,5,8],
"write": [7,0],
"gtk": [1],
"tutt\'": [6],
"decrescent": [0],
"project_save.tmx.bak": [[2,6]],
"proceed": [7],
"nell\'elenco": [0,1],
"disabilitati": [[0,1,7]],
"potrebb": [0,[2,5],1,[3,7]],
"projectaccesswriteableglossarymenuitem": [0],
"un\'impostazion": [7],
"even": [7],
"application_shutdown": [7],
"autocompletertablelastinrow": [0],
"unirà": [[0,7]],
"gui": [7],
"descritte": [8],
"descritta": [5],
"regexp": [0],
"sentencecasemenuitem": [0],
"preparazion": [2,0],
"tutti": [2,0,[1,7],4,3,[5,6]],
"corrent": [4,0,[2,3]],
"editorcontextmenu": [0],
"determinata": [6],
"tutto": [0,[6,7]],
"nativi": [0],
"collaborazione": [8],
"buon": [1],
"optionssentsegmenuitem": [0],
"esegu": [1,[0,2,4,7]],
"rilasciar": [5,[2,6]],
"nativo": [1],
"bought": [0],
"generazion": [[0,2]],
"assegnato": [[3,4,5]],
"optionsaccessconfigdirmenuitem": [0],
"charact": [7,2],
"assegnati": [7,6],
"framework": [2],
"test.html": [2],
"grammaticali": [4],
"l\'indic": [0],
"coincidano": [7],
"unità": [0],
"php": [0],
"assegnata": [[0,4,6]],
"xxx": [6],
"diritti": [2],
"instanc": [7],
"smalltalk": [7],
"arrow": [7],
"risultar": [[1,3]],
"aranc": [0],
"tempo": [2,3,[0,1,5,8]],
"associata": [4,[0,2,3]],
"l\'asterisco": [0],
"pseudotranslatetmx": [2],
"whether": [7],
"salvarlo": [6],
"armi": [3],
"personalizzata": [2],
"targetlanguagecod": [0],
"editorprevsegmentnottab": [0],
"personalizzati": [0,1,4,[2,3]],
"toglier": [2],
"debba": [1,[2,3]],
"uniqu": [7],
"interrogativo": [0],
"attenzion": [2,0],
"associati": [0,2],
"verificator": [1,0],
"personalizzato": [2],
"annul": [4],
"scorrett": [0],
"tramit": [2,[1,3,6,7],[4,5]],
"bidirect": [4],
"spaziatric": [[0,5]],
"associato": [0,1,5],
"un\'indicazion": [0],
"basic": [7],
"disabl": [1],
"quell\'ordin": [0],
"uniti": [3],
"scaricabil": [2],
"causa": [4],
"entrar": [4,[3,5]],
"diaposit": [0],
"includerlo": [1],
"command-lin": [2],
"assicura": [0],
"scarno": [4],
"chied": [4],
"consecutiva": [0],
"citato": [4],
"citati": [0],
"un\'unità": [0],
"accord": [7],
"tien": [4,0],
"combinar": [[2,6]],
"consecutivi": [0],
"direzionale": [8],
"invisibil": [0],
"proceder": [2,4],
"utilizzabil": [4,7],
"conduct": [7],
"projectnam": [0],
"omegat.project.yyyymmddhhmm.bak": [2],
"applicar": [[0,1,7]],
"concatenazion": [0],
"preservata": [2],
"rapidament": [3,[2,6]],
"maiuscol": [0,1,2],
"confermato": [4],
"post-elaborazion": [[0,7],1],
"eventi": [0],
"preservati": [1],
"installdist": [2],
"piè": [0],
"a-z": [0],
"recuperar": [4],
"evento": [0,2],
"password": [2,1],
"modificator": [0],
"seguono": [[0,2,3]],
"nell\'original": [5],
"compatibilità": [0],
"gotonextxenforcedmenuitem": [0],
"editordeleteprevtoken": [0],
"nuovament": [2],
"divisibil": [[3,7]],
"più": [0,2,5,1,3,4,6,7,8],
"dell\'utent": [0,2,[3,4,7,8]],
"diagrammi": [0],
"sostanzialment": [2],
"want": [7],
"un\'interfaccia": [2],
"impiegato": [0],
"processor": [7],
"personalizzar": [0,1],
"javascript": [7],
"mediawiki": [[4,7],[0,3]],
"input": [[1,2]],
"ossia": [0,[1,5]],
"toolkit": [2],
"volt": [0,3,[2,4]],
"join.html": [0],
"sommario": [8],
"stanno": [2,5],
"omegat.kaptn": [2],
"limiti": [3],
"poi": [3,2,0,6],
"multi-cel": [7],
"pop": [0,4],
"utilizzarla": [[1,2]],
"coloro": [2],
"found": [7],
"usernam": [2],
"colori": [1,4],
"larg": [7],
"fabbrica": [5],
"anoth": [7],
"corrispond": [0,4,[1,2,3,7]],
"pend": [7],
"colora": [0],
"memorizzar": [6,[0,5]],
"impossibil": [5],
"volessero": [6],
"consultino": [0],
"trascinato": [5],
"l\'amministrator": [2],
"googl": [1],
"l\'opzion": [[1,4],[2,6],3,0],
"preservato": [0],
"dell\'attual": [2],
"gotoeditorpanelmenuitem": [0],
"corrisponderebb": [0],
"ordina": [1],
"attend": [2],
"viewmarkfontfallbackcheckboxmenuitem": [0],
"qualch": [2,[1,3]],
"sull\'elemento": [5],
"had": [[0,7]],
"estremament": [0,3],
"align": [7],
"fatta": [[2,6]],
"insertcharsrlm": [0],
"sourceforg": [2,0],
"hai": [3],
"dell\'applicazione": [8],
"continua": [3],
"han": [0],
"corrett": [[0,1,2,3,5,6]],
"precedut": [0],
"originaria": [5],
"semeru-runtim": [2],
"continui": [[0,3]],
"definisc": [1,[0,2],4],
"has": [7],
"tabellari": [1],
"keyword": [7],
"fatto": [2],
"alfabetici": [0],
"last": [7],
"editmultipledefault": [0],
"adapt": [7,3],
"mozilla": [[0,2]],
"editfindinprojectmenuitem": [0],
"risorse": [8],
"implica": [[0,6]],
"pro": [1],
"individua": [0,3],
"definito": [0,[2,4,5]],
"offert": [3],
"inoltr": [3],
"vuoti": [0,4],
"scarichi": [2],
"warn": [2],
"togliendo": [0],
"definita": [0,[2,5]],
"l\'ausilio": [4],
"vuoto": [0,[1,2,4,5,6],3],
"trattat": [0],
"tagliar": [0],
"semplici": [0,1,3,[2,8]],
"plural": [0],
"conta": [4],
"all\'inizio": [0],
"tradurr": [2,3,0,4,5,[6,7]],
"l\'area": [5],
"percorso-a-un-file-di-progetto-omegat": [2],
"raccomand": [0],
"appariranno": [0,1],
"conto": [[0,3]],
"pere": [0],
"prodotto": [2],
"avviarlo": [2],
"nell\'area": [5],
"yes": [7],
"duckduckgo.com": [1],
"delimitazion": [[4,5],1],
"yet": [7],
"configura": [1],
"colour": [7,1],
"chang": [7],
"sull\'espression": [0],
"time": [7,6],
"riconosciut": [0,[5,7]],
"totalment": [6],
"immediatament": [0,3,[2,4,5]],
"tipo": [0,1,[2,3,5],[4,6]],
"parzial": [1,5,4,[2,6]],
"kanji": [0],
"nonostant": [[1,4]],
"program": [[2,7],0],
"python3": [0],
"uguali": [[2,4,5]],
"tipi": [0,[2,4],1],
"apportar": [0,6],
"tran": [0],
"pagina": [0,4,[1,2,7],3],
"univoci": [5,7],
"iraq": [0],
"imparar": [3],
"right-click": [7],
"attesa": [[0,8]],
"brunt": [0],
"separa": [1],
"mancant": [[0,4],3],
"univoco": [4,0],
"parziali": [4,1,5,7,3,2,0,[6,8]],
"però": [0],
"engin": [5,[4,7]],
"sicura": [1,8],
"aprendo": [5],
"eseguendo": [6],
"un\'estension": [0,[2,5]],
"four-step": [7],
"doc-license.txt": [0],
"theme": [7,1],
"impostando": [1],
"チューリッヒ": [1],
"all\'estrema": [5],
"editor": [7,0,5,3,[1,4,6],8,2],
"pseudotranslatetyp": [2],
"esecuzion": [0],
"eliminazion": [2],
"passaggi": [3,0,2],
"immutati": [6],
"giallo": [4],
"dizionari": [1,3,[5,6],[0,4,7],8,2],
"facent": [0],
"rispetto": [[0,3]],
"flusso": [3,8],
"char": [7],
"annullato": [0],
"apprender": [[0,3]],
"ricerca": [0,3,1,[4,7],8,[2,5]],
"projectclosemenuitem": [0],
"tuttavia": [7,[0,2,3]],
"ulteriorment": [3,7],
"ricerch": [7,1,0,4,[5,6]],
"può": [0,2,4,[3,5,6],[1,7]],
"viewmarknonuniquesegmentscheckboxmenuitem": [0],
"chiud": [4,0],
"fissa": [[1,6]],
"hit": [7],
"scura": [1],
"iscriversi": [3],
"consider": [[2,6]],
"titl": [7],
"inspir": [7],
"funzionamento": [8],
"group": [7],
"scuro": [1],
"cronologia": [4,[0,1],3],
"findinprojectreuselastwindow": [0],
"campi": [[0,4]],
"bloccato": [5,2],
"readme.txt": [2,0],
"campo": [4,[2,3,7],[0,1,5]],
"languagetool": [4,1,[7,8]],
"commuta": [0,4],
"ricaricato": [2],
"impostare": [8],
"source.txt": [[0,1]],
"files.s": [7],
"raddoppiandosi": [0],
"sicuro": [2],
"exchang": [0],
"alternanz": [4],
"chiarir": [3],
"currseg": [7],
"their": [7],
"trascinando": [5],
"general": [7,1,[0,2],[3,4,8]],
"colonn": [[0,1]],
"identifica": [1],
"generar": [2,3],
"soddisfacent": [2],
"spostar": [4,3],
"torna": [3],
"dimension": [1],
"frequenza": [2],
"facil": [3,4,0],
"scaricar": [2,[0,1,6]],
"l\'inserimento": [4,2],
"avviserà": [7],
"process": [7],
"magico": [0],
"autocompletertrigg": [0],
"membri": [2,3],
"membro": [2],
"clear": [7],
"impostata": [[2,4],6],
"alternativa": [[0,4],5,7,[1,2,3]],
"esercitazioni": [0],
"visiva": [0],
"alternativo": [[0,2]],
"contesto": [[1,5],4],
"normalment": [2,4],
"mean": [7],
"parentesi": [0,1],
"condivider": [2,3,[6,7]],
"impostato": [1,[0,2,3,6]],
"unico": [0],
"cancellerà": [1],
"attivar": [4,7,[0,1,5],[2,6]],
"account": [2],
"snippet": [7],
"sincronizzi": [2],
"been": [7],
"dhttp.proxyhost": [2],
"diversa": [2,0,[1,7],5],
"predizion": [[0,1]],
"alphabet": [7],
"systemwid": [2],
"barra": [0,5,[2,3,8]],
"editorprevseg": [0],
"diverso": [[1,4],[2,5]],
"marca": [[0,4]],
"mantener": [2,[0,4,7]],
"ignorar": [0,[1,4,6,7]],
"salvarl": [6],
"rileva": [4,[1,2]],
"a-za-z0": [0],
"diversi": [2,1,[0,3],[4,6]],
"you": [7,1,[0,2,4,5]],
"jump": [7],
"lunghezza": [0],
"prime": [[1,3],5],
"discesa": [0],
"tecnici": [0],
"www.apertium.org": [1],
"prima": [2,0,1,4,5,3,[6,7]],
"salvati": [[1,4],[0,6,7]],
"specificar": [2,0,[1,7]],
"tecnico": [[0,2]],
"salvata": [3],
"project_save.tmx.tmp": [2],
"configur": [7,5,[1,2,4]],
"trann": [3],
"un\'opzion": [0],
"ricav": [[4,5]],
"unicode": [8],
"funzionalità": [1,2,3,[0,4,5]],
"preserv": [2],
"dell\'editor": [5,[0,1,3],4,[2,7]],
"documento.xx": [0],
"mega": [0],
"zurich": [1],
"空白文字": [2],
"optionsworkflowmenuitem": [0],
"digitando": [[1,2]],
"how": [7],
"releas": [2,0],
"scaricarlo": [2],
"dotati": [0],
"segmentar": [[0,3,7]],
"salvato": [2,[0,4],5],
"conterrà": [6],
"correzioni": [[0,2,4]],
"effettivament": [3],
"supplementari": [0],
"limitato": [2],
"gestion": [3,4,[0,2]],
"dictroot": [0],
"riguardo": [[5,6]],
"approfondir": [0],
"selezionando": [4,6],
"keybind": [7],
"somiglianti": [0],
"xhmtl": [0],
"vengono": [0,2,1,7,5,[3,4],6],
"represent": [7],
"destinazion": [0,[1,4,7],2,[3,6]],
"frequentement": [0,4],
"questioni": [4],
"struttura": [[6,7],0,2,[3,8]],
"legger": [7,2,0],
"hold": [7],
"tale": [0,2,[6,7],4],
"intervallo": [0,2,[1,4,6]],
"subdir": [2],
"sfruttar": [0],
"tali": [2,1,[0,6],[3,4,7]],
"mostra": [4,5,1,0,2,3,7],
"eseguito": [2,3],
"corregger": [[1,2,4]],
"autocompletertableleft": [0],
"passar": [[1,3,5,6],[2,4]],
"l\'impaginazion": [1],
"forward-backward": [7],
"eseguita": [2,[0,4]],
"take": [7],
"l\'appendic": [7,[0,1],4,2,[3,5,6]],
"editorlastseg": [0],
"file-source-encod": [0],
"abbia": [[0,2,7]],
"confusion": [4],
"ripetizion": [5],
"some": [[2,7]],
"tant": [2],
"session": [[3,4,5,7]],
"approssimativo": [7],
"divis": [0],
"bisognerà": [[0,2]],
"criteri": [[0,3]],
"l\'identificazion": [3],
"primo": [0,4,[2,3,5,7]],
"attinenti": [2],
"alpha": [2],
"大学院博士課程修了": [1],
"primi": [0],
"just": [[0,7]],
"alterar": [2],
"divid": [7],
"elencando": [0],
"aggiungerà": [6],
"editexportselectionmenuitem": [0],
"eseguire": [8],
"solo": [0,[1,2],[3,4],7,5],
"home": [0,2],
"disable-location-sav": [2],
"print": [7],
"eliminar": [2,0,[3,6,7]],
"elaborarlo": [0],
"illustrano": [0],
"projectaccesstargetmenuitem": [0],
"sincronizza": [2,[3,4,5]],
"iana": [0],
"spostato": [[1,5]],
"attual": [2,5,6,[3,4,7]],
"creando": [2],
"varianti": [0],
"introduzion": [3,[2,8]],
"stess": [[0,2],1],
"soon": [7],
"bene": [[0,2],3,8],
"aligndir": [2,7],
"system-host-nam": [0],
"servizio": [2,5,4,1],
"action": [7],
"sostituzioni": [4],
"mymemory.translated.net": [1],
"sostituzione": [8],
"creat": [7,[2,3]],
"operativi": [6],
"quest\'ultimo": [0,2],
"mele": [0],
"python": [7],
"pulsanti": [4,[0,3]],
"conformement": [2],
"sono": [0,2,1,7,4,6,[3,5]],
"abil": [0],
"crean": [3],
"limitano": [2],
"massimizzato": [5],
"preimpostata": [2],
"crear": [2,[0,3],7,[4,6],1],
"ugual": [1,4,0],
"compatta": [1],
"aprirsi": [2,3],
"funzioni": [0,3,[5,6],[4,7]],
"quest\'ultima": [2],
"codic": [0,2,3,[1,7]],
"organizzar": [0,2],
"tarbal": [6],
"tratta": [5,2],
"singl": [7],
"funziona": [[0,2],[1,4,6]],
"migliorar": [[0,3]],
"graffa": [0],
"operar": [2],
"infin": [[1,2]],
"it_it": [3],
"periodo": [3],
"vuota": [2,6,[4,7],[0,1,3,5]],
"ripristina": [0,5,4,1],
"robustezza": [[2,3]],
"arrivarlo": [4],
"file": [2,0,7,4,6,3,1,5,8],
"gratuito": [8],
"dieci": [2,[4,6]],
"regola": [0,1,3],
"gratuiti": [2],
"leggibilità": [1],
"spazi": [0,4],
"operativo": [0,2,4,[1,5]],
"scaricherà": [3],
"regole": [8],
"l\'esecuzion": [2],
"ripristino": [2],
"notazioni": [0],
"could": [7],
"meno": [2,3,[0,1,4]],
"delimitazioni": [5,[0,1,4]],
"trigger": [7],
"menu": [0,4,5,1,3,7,8,2,6],
"ment": [[0,3]],
"consigliata": [2],
"positivi": [1],
"sfogliar": [3],
"distingua": [0],
"specifichi": [[0,2]],
"asterisco": [0],
"return": [7],
"invoke-item": [0],
"consonant": [0],
"lasciato": [4,3],
"affinché": [0,2,[1,3,4,7]],
"sovrascrittura": [5,4,[0,3]],
"progetto": [2,6,3,0,4,7,5,1,8],
"jolli": [[0,2]],
"radio": [7],
"fini": [0],
"salvano": [2],
"ottenendo": [5],
"source-pattern": [2],
"intermedia": [2],
"trascinati": [5],
"fine": [0,2,[1,3,7],6],
"find": [7],
"rilevamento": [1],
"host": [2,0],
"radic": [0,1,5,2],
"orizzontal": [0],
"armonizzarlo": [3],
"problemi": [4,1,2,0,3,[5,6,8]],
"scrittura": [2,5,1],
"modificabili": [0],
"autocompletertablepagedown": [0],
"dall\'url": [[1,2]],
"aggiungerla": [0],
"problema": [3,[2,4,5]],
"l\'impersonal": [3],
"sort": [7],
"possesso": [[1,2]],
"task": [[2,7]],
"dall\'esterno": [5],
"aggiungerlo": [2],
"xliff": [[0,2]],
"true": [0],
"header": [7],
"inviar": [2,1],
"present": [[0,2],7,[1,4],5],
"libertà": [8],
"groovi": [7],
"evitar": [0,[2,3,4]],
"utenti": [2,8,[0,1,3,4]],
"dall\'uso": [2],
"l\'ora": [[2,4]],
"contestual": [1,5,[0,3,4,6]],
"multi-paradigm": [7],
"strettament": [2],
"best": [7],
"reimpost": [1],
"mess": [[2,3]],
"aprirla": [2],
"execut": [7],
"mese": [0,2],
"aprirlo": [2,[0,5,6]],
"mesi": [3],
"possono": [0,2,3,5,1,4,6,7],
"abov": [7],
"messageformat": [1,0],
"fino": [2,[0,5],[1,3,6]],
"master": [2,0],
"spesso": [3,[0,2]],
"progetti": [2,[1,4],0,7,3,[5,6,8]],
"percentual": [5,1,6],
"prodott": [2],
"writer": [0],
"merg": [7],
"rubi": [7],
"utili": [0,3,[2,4,5,7]],
"sviluppatori": [[0,2]],
"resource-bundl": [2],
"mantenut": [6],
"pubblicar": [2],
"external_command": [6],
"riferimenti": [0,7],
"speciali": [0,[2,6]],
"editorselectal": [0],
"visualizzarn": [1],
"globali": [7,0,1,4,8,[3,5]],
"riportato": [[0,2]],
"convertono": [2],
"disconnettersi": [2],
"metodo": [[0,2],5],
"runner": [7,0],
"formattazion": [[0,4],3,7],
"metodi": [2],
"pertinenti": [[0,1,2,3]],
"immedi": [7],
"riportata": [0],
"sembra": [3,[2,8]],
"omegat-default": [2],
"riportati": [0,6],
"riportate": [8],
"scegli": [4],
"user.languag": [2],
"regex": [0,3],
"meta": [0],
"avvia": [2,0],
"premuta": [0],
"un\'icona": [4],
"riutilizzar": [2,3,[1,4]],
"visualizzata": [[1,4]],
"premuto": [5],
"naviga": [4],
"l\'intestazion": [0],
"visualizzati": [1,0,[2,3,5]],
"avvio": [2,0,1,[4,7]],
"global": [7,0,[1,2]],
"esclusioni": [7],
"all\'avviator": [2],
"visualizzato": [1,[0,4],[5,6]],
"leggimi": [0],
"risulta": [5],
"valor": [0,1,[2,4]],
"digitazion": [[2,5]],
"pagin": [3],
"ibm": [[1,2]],
"chiar": [2],
"sistemazion": [4],
"rispettiva": [0],
"chiav": [[1,3],2],
"comun": [[1,2,5]],
"delimitator": [1],
"porzion": [0],
"parsewis": [7],
"aprono": [5,[4,7]],
"ancora": [[1,3,4],0,[2,6,7]],
"conformi": [2],
"poch": [4],
"utilizzar": [2,0,[1,4],[5,7],[3,6]],
"andata": [2],
"assoluto": [0],
"alfanumerici": [0],
"chiamato": [0,2,1],
"poco": [2],
"copiando": [[3,6]],
"omegat-cod": [2],
"necessitar": [5],
"l\'accento": [0],
"ricordar": [[2,4]],
"lavorarci": [2],
"solidità": [3],
"riprenderà": [0],
"installar": [2,1,[0,3],6],
"metà": [[1,7]],
"rimossi": [1],
"racchius": [0],
"guid": [[2,3],5],
"idx": [6],
"simil": [0,2,[1,3],7],
"ignorati": [0],
"conseguenza": [3],
"qui": [1,[0,5],[6,7],[2,3]],
"motori": [4],
"detect": [4],
"rule": [7],
"ignorato": [0,[2,7]],
"everi": [7],
"colorata": [5],
"specificazion": [0],
"autocompleterconfirmandclos": [0],
"decorativa": [3],
"projectaccesscurrentsourcedocumentmenuitem": [0],
"desider": [4],
"basat": [[4,7]],
"linux": [0,2,4,5,[1,3,7]],
"rilasciarlo": [5],
"linux-install.sh": [2],
"rapido": [[2,3]],
"contezza": [0],
"assicurati": [3],
"dentro": [3],
"piccola": [6,4],
"inferior": [5,[1,4]],
"again": [[1,7]],
"file.txt": [2],
"elimina": [0,4],
"grandi": [1],
"openxliff": [2],
"dall\'applicazion": [4],
"chiamata": [[2,6]],
"piccolo": [0],
"popup": [7],
"ifo": [6],
"corsivo": [[0,3,7]],
"consentendo": [[0,3]],
"comment": [7],
"excit": [0],
"affidabil": [1,[2,7]],
"sostituzion": [4,0,7,3],
"risulterà": [[0,5]],
"optionsmtautofetchcheckboxmenuitem": [0],
"regol": [7,0,1,4,2,6],
"opportuno": [2,3],
"gruppo": [0,1,7,[4,6],[2,3,5]],
"sistema": [2,4,0,[1,3,5,7],6],
"xx.docx": [0],
"letterali": [0,1],
"raggruppar": [[1,5]],
"dell\'eseguibil": [2],
"semplic": [0,3,2,5],
"sistemi": [2,1,[0,4],6],
"semplif": [2],
"consist": [0,[6,7]],
"arabo": [0],
"dall\'elenco": [2,[0,4,7]],
"massimizza": [5],
"dell\'estension": [0],
"opportuna": [2],
"post-elaborazione": [8],
"cartella": [2,0,6,4,7,1,3,5,8],
"compresa": [0],
"editorshortcuts.properti": [0],
"pigiar": [5],
"sgancia": [5],
"versione": [8],
"pagsu": [0],
"linguistica": [0,[1,2,4]],
"inserimento": [2,[4,5]],
"itself": [7],
"restituirà": [0],
"sdlxliff": [2],
"richied": [2,[0,1],7],
"tipologi": [0],
"versioni": [2,[0,7,8]],
"all\'editor": [[3,5],7],
"l\'ha": [3],
"thumb": [7],
"linguistico": [1,0,6],
"regolazioni": [6],
"linguistici": [[0,1,3,7]],
"sostituiscilo": [3],
"riveder": [[2,3]],
"requir": [[2,7]],
"l\'aspetto": [0],
"requis": [2],
"linguistich": [1],
"tmotherlangroot": [0],
"viewmarknotedsegmentscheckboxmenuitem": [0],
"preferenze": [8],
"preferenza": [4,0,5,2,1,[3,6,7]],
"event": [7],
"dell\'applicazion": [0,2],
"un\'eccezion": [[0,1]],
"l\'esempio": [2],
"gotomatchsourceseg": [0],
"appropri": [7],
"sottocartella": [2],
"excel": [0],
"runn": [7],
"restituito": [[0,2]],
"runt": [0],
"descriver": [2],
"stardict": [6],
"omegat.l4j.ini": [2],
"first": [7],
"impostazion": [0,1,7,2,6,[4,5],3],
"span": [0],
"gruppi": [1,0,[2,3]],
"seguent": [0,[4,5],2],
"l\'intera": [[0,2]],
"prefer": [[0,7]],
"quotat": [7],
"nascondi": [[0,3,4,5,7]],
"vicin": [1],
"informatiche": [8],
"l\'intero": [[0,1,2,4]],
"space": [7,0],
"tipografici": [[4,7]],
"stessa": [2,0,1,[3,5,6,7]],
"fermati": [1],
"sbaglio": [1],
"hard-and-fast": [7],
"ドイツ": [7,1],
"veniss": [2],
"visualizzi": [[1,3]],
"simpl": [7],
"riscontrano": [4],
"from": [7,[1,5]],
"quantità": [2],
"cifr": [0,[2,5]],
"addirittura": [[2,7]],
"editselectfuzzy3menuitem": [0],
"stesso": [0,2,1,[3,4,5],7],
"adatto": [3],
"bottom": [7],
"verifica": [1,4,0,[2,3],[5,7]],
"l\'id": [0],
"costituirà": [0],
"stessi": [2,4],
"fals": [0,2],
"visualizza": [4,1],
"project.projectfil": [7],
"aggregati": [0],
"adatta": [[3,5],4],
"compatibil": [[0,7]],
"rendono": [0],
"trovar": [0,2,1,3],
"sincronizzazion": [2,7,5],
"sebben": [0,7,2],
"indirizzati": [0],
"frequent": [7],
"error": [2,5,0,7],
"momento": [3,[0,6],[2,5,7]],
"costituisc": [[0,2,6]],
"imposta": [0,[1,4],3],
"erano": [4],
"shortcut": [7,2],
"public": [2,8],
"considererà": [5],
"dell\'allineamento": [2],
"protegger": [1],
"tmx2sourc": [2,[0,6]],
"ini": [2],
"nell\'eseguirl": [2],
"proced": [2],
"rimozion": [2],
"disattiv": [4],
"riferimento": [0,2,6,3,[4,7]],
"dimostrano": [0],
"sovrascr": [5],
"poiché": [2,3],
"dhttp.proxyport": [2],
"elenchi": [0],
"introduzione": [8],
"negar": [0],
"superflua": [6],
"fare": [2,0,[1,5],7,[3,4],6],
"equivalgono": [1],
"pronto": [2],
"subrip": [2],
"copie": [8],
"selezion": [4,0,5,[1,2]],
"l\'altra": [2],
"l\'accesso": [4,2,0],
"aggiornata": [3],
"score": [1,7],
"describ": [7],
"pronta": [2],
"farn": [2],
"scorr": [0],
"aggiornato": [6],
"complessi": [0],
"usano": [0,7],
"utilizzati": [[0,2,7],[1,6]],
"persona": [[2,3]],
"inclusioni": [0],
"descriv": [0,2],
"impostar": [2,1,[0,6,7],[3,4,5]],
"passo": [0],
"illustr": [0],
"complesso": [2],
"utilizzate": [8],
"struttur": [[0,7]],
"passa": [[1,5],[0,4]],
"raw": [2],
"utilizzata": [0,2,[4,6],[1,3]],
"fonti": [[0,5]],
"tornar": [[3,5],2],
"dovranno": [1,2,7],
"comunicazion": [5],
"conserv": [2],
"pont": [2,[0,6]],
"decomprimer": [2],
"registrano": [0],
"discorrendo": [7,6],
"utilizzato": [0,2,3,4,1],
"copia": [2,6,1,[4,7],[0,5]],
"sviluppo": [2,[0,1]],
"spunta": [0],
"trascinamento": [5],
"specificati": [0,[1,7]],
"raccolta": [6],
"controllato": [[1,2]],
"controllati": [1],
"possiedono": [0,5],
"manual": [[0,3,4],[2,7],8],
"apribil": [2],
"specificato": [2,1],
"carica": [[2,4],[0,1]],
"rimaner": [5],
"indicatori": [5],
"cima": [0,[1,2,3,5]],
"fatt": [2],
"appendic": [0],
"unbeliev": [0],
"close": [7],
"compaiono": [3],
"aggressiva": [[0,4]],
"considerazioni": [2],
"fase": [[2,5],[3,7]],
"abc": [0],
"abilitar": [[0,2,6],[1,7]],
"abl": [7],
"permesso": [2,3],
"toolbar.groovi": [7],
"concordanz": [1,4,5,6,[2,7],3,0],
"progressivament": [1],
"precedent": [4,0,3,[2,5]],
"specificata": [[0,2],[1,3,6]],
"caricando": [1],
"spicco": [2],
"algoritmo": [[0,7]],
"l\'altro": [[1,4]],
"permessa": [0],
"sintassi": [0,2,7],
"raccomanda": [2],
"sembrano": [3],
"colorazioni": [0],
"iso": [[0,2]],
"isn": [[0,2]],
"giapponesi": [1],
"farà": [[0,2]],
"optionspreferencesmenuitem": [0],
"vengano": [2,0],
"laddov": [2],
"maggioranza": [0],
"cartell": [2,7,[3,6],[0,4]],
"coprono": [0],
"red": [7],
"scrive": [2],
"estranei": [0],
"post": [0],
"utilizza": [0,[1,2],6,7,[3,5]],
"glossary.txt": [[2,6],[0,4]],
"finish": [7],
"scartato": [6],
"spuntata": [2],
"scrivi": [3],
"add": [7,2],
"cambio": [2],
"casella": [[0,7],1],
"initi": [7],
"utilizzo": [2,3],
"equival": [7,1],
"consent": [[5,7],[1,2,4]],
"utilizzi": [[0,1]],
"accedi": [0,3,[4,6],[1,2,7]],
"pertanto": [2],
"prescinder": [2],
"rfe": [7],
"cambia": [0,[2,4,6]],
"danneggiati": [2],
"concep": [6],
"soddisfatti": [2],
"buona": [[2,8]],
"shell": [0],
"entry_activ": [7],
"subito": [[0,3,6],1],
"chiest": [2],
"esclud": [2,0],
"optionsautocompleteshowautomaticallyitem": [0],
"gotoprevxautomenuitem": [0],
"exec": [0],
"chiamandolo": [2],
"rimuoverlo": [2],
"dall\'amministrator": [2],
"sviluppar": [2],
"sarebb": [[0,2]],
"untar": [2],
"estesa": [3],
"muoverlo": [5],
"consequ": [7],
"prevent": [7],
"estesi": [0],
"risors": [3,7,6,[0,2]],
"ishan": [0],
"pasta": [0],
"pseudotradotta": [2],
"scope": [7],
"pseudotradotto": [2],
"scopi": [[0,2]],
"caratter": [0,1,4,3,[2,5],7],
"scopo": [0,8,[1,2,7]],
"esteso": [2],
"modifi": [7],
"allineato": [4],
"mappatura": [2,7],
"estraneo": [1],
"allineati": [[0,2]],
"l\'abbreviazion": [0],
"convenzioni": [3,8],
"quanti": [2],
"correzion": [1,3,2,[6,7]],
"corrisponder": [0,2],
"clone": [2],
"ovunqu": [4,0],
"targetlanguag": [[0,1]],
"distribuzioni": [2],
"virgolett": [0],
"quanto": [1,[0,2,3,5,6]],
"estern": [7,1,4,5,3,[0,6]],
"indicar": [3],
"filtro": [0,2,[1,7],[3,4]],
"memorie": [8],
"sensit": [7],
"backup": [7],
"memoria": [2,3,0,6,[4,5],7,1],
"collega": [2],
"concorda": [[0,1]],
"properti": [2,0],
"filtri": [[0,2],7,1,4,8,[3,6]],
"corrispondenti": [6,[2,4,5]],
"durant": [[2,7],3,[0,4],[1,5,6]],
"editselectfuzzyprevmenuitem": [0],
"number": [7],
"identifi": [7],
"copiar": [2,[0,1,6]],
"inserirlo": [5],
"cercando": [2,0],
"lettura": [0],
"sempr": [0,[1,2],[6,7],[3,4],5],
"letterario": [0],
"controllano": [2],
"algorithm": [7,4],
"shorter": [7],
"desiderano": [2],
"script": [7,0,2,4,1,8,[3,6]],
"oltr": [0],
"ritorni": [[0,3]],
"exit": [7],
"system": [7,2],
"ricorsiva": [7],
"spellcheck": [7],
"ritorna": [[3,6]],
"basata": [0,5],
"distinguer": [[0,3]],
"other": [7],
"c\'è": [4],
"cinqu": [1],
"identico": [0],
"local": [2,7,1,4,0,[5,6]],
"scaricabili": [[1,2]],
"locat": [7],
"yield": [7],
"sostituito": [0,1],
"crea": [0,2,7,4,6,3,5,1],
"richiederanno": [3],
"sostituisc": [4],
"dipendono": [[4,7]],
"affermato": [0],
"rle": [0,4],
"interferisc": [4],
"resto": [0,3],
"repo_for_all_omegat_team_project_sourc": [2],
"lento": [2],
"caratteristich": [[0,7]],
"memorizzarlo": [0],
"rlm": [0,4],
"segmento": [4,5,0,1,3,7,2,6,8],
"identich": [5],
"utilizzano": [2,0,1,[3,7,8]],
"identici": [4,[2,6]],
"segmenti": [4,0,[1,5],6,3,7,2,8],
"crei": [3],
"identica": [1],
"basano": [0,4],
"correspond": [7],
"c-x": [0],
"suggerimenti": [1,0],
"all\'avanzar": [2],
"mode": [7,2],
"corrispondenz": [[1,6],[0,4]],
"modi": [2,4],
"usata": [0],
"suggerimento": [[1,5]],
"corrispondent": [0,4,1,[2,5,6]],
"usato": [0,1],
"segnalibri": [0],
"specificando": [0],
"toolsshowstatisticsstandardmenuitem": [0],
"potrà": [[2,3,7]],
"modo": [0,[2,3],4,6,1,8],
"usati": [4],
"all": [7,1,4,0,3,2,6,5],
"read": [7],
"c.t": [0],
"alt": [0,4],
"modalità": [2,5,4],
"rememb": [7],
"real": [[0,2,5]],
"unit": [7,0],
"unir": [3],
"alreadi": [7],
"dell\'elenco": [0,4],
"finali": [4],
"attribuirg": [0],
"fondo": [1],
"registrata": [4],
"eu-direzion": [4],
"riguardano": [2],
"colonna": [4,0],
"registrati": [[2,7]],
"ritorno": [0,7,2],
"completato": [0],
"l\'interfaccia": [2,0,1],
"esternament": [3],
"tkit": [2],
"and": [7,2,[1,4,5]],
"synchron": [7],
"inviato": [1],
"modifica": [0,1,7,5,3,4,2,6,8],
"aspettano": [0],
"row": [7],
"ani": [7,2],
"l\'attivazion": [7],
"modifich": [[0,2],4,6,[1,3],5,7],
"completata": [[2,3]],
"minuti": [2,[1,4,6]],
"riutilizzarla": [2],
"ricorderà": [2],
"minuto": [[2,3]],
"ciascun": [0,2,4,1,[3,5,6]],
"nell\'estension": [2],
"ant": [[2,7]],
"leggerment": [[0,5]],
"consistono": [4],
"registrato": [[0,3]],
"considerando": [[0,3]],
"attribuito": [1],
"dischi": [7],
"ottimizzar": [3],
"argomento": [0],
"corrispondenza": [0,6],
"helplastchangesmenuitem": [0],
"until": [7],
"traccia": [4,[2,5]],
"redistribuirne": [8],
"argomenti": [[0,2]],
"omegat.ex": [2],
"interruzion": [[0,1]],
"grado": [2,0,[3,5,6]],
"perché": [0],
"sourcetext": [1],
"tradotto": [0,2,3,4,[1,5,7],6],
"tasti": [0,[3,4,5],8],
"compon": [[3,7]],
"fornitori": [1],
"tabelle": [8],
"jar": [2],
"mistak": [7],
"api": [[1,7]],
"tabella": [0,1,5,4],
"editselectfuzzy2menuitem": [0],
"prossimo": [0,[1,2,3,4,5]],
"scoraggianti": [0],
"apr": [4,[1,3],[2,5]],
"modificando": [2],
"tediosament": [3],
"scaricano": [2],
"ciascuna": [0,1,4],
"tasto": [0,4,[1,5],7],
"impararl": [0],
"chiesto": [4],
"letter": [0,2,7,[3,4]],
"editornextseg": [0],
"ciascuno": [[0,2],5],
"libreri": [0],
"completamenti": [1],
"editselectfuzzynextmenuitem": [0],
"gotonextxautomenuitem": [0],
"completamento": [0,1,[3,5],[4,8]],
"l\'identificator": [4],
"worth": [7],
"anziché": [[0,3],[4,7]],
"are": [7,[1,2],[4,5]],
"cloud.google.com": [1],
"potranno": [4],
"readme.bak": [2],
"arg": [2],
"mancanti": [4,0,[2,3,5]],
"where": [7],
"svariat": [6],
"parzialment": [2],
"l\'espression": [0,1],
"sblocca": [5],
"vice": [7],
"rinvenuti": [[2,5]],
"rivolgono": [2],
"circonflesso": [0],
"ulteriori": [2,[0,1,3,4,5]],
"significato": [0,5],
"call": [7,0],
"sblocco": [5],
"tradotti": [7,4,0,[2,3,5],6,1,8],
"percentuali": [[1,5,6]],
"sbloccato": [5],
"ask": [7],
"tradotta": [2],
"scelta": [4],
"through": [7],
"generica": [1],
"toolsshowstatisticsmatchesperfilemenuitem": [0],
"strength": [7],
"grafica": [2],
"vicino": [2],
"run": [7,0,2],
"grafici": [0],
"grafich": [2],
"generico": [2],
"linguetta": [5],
"desiderati": [0],
"either": [7],
"view": [7],
"entrarci": [3],
"editorshortcuts.mac.properti": [0],
"molt": [0],
"generich": [1],
"generici": [0],
"ricercando": [0],
"titlecasemenuitem": [0],
"desiderata": [[1,5]],
"opzional": [[0,4]],
"permetterà": [[1,2,3]],
"those": [7],
"l\'impianto": [3],
"editcreateglossaryentrymenuitem": [0],
"istanza": [2,[4,5]],
"precauzioni": [2],
"punteggiatura": [0],
"vien": [2,0,4,[5,6],1,7,3],
"bold": [7],
"introduc": [7],
"多和田葉子": [7],
"name": [7,0],
"memorizzazion": [1,[2,4]],
"protocollo": [2,1],
"allo": [2,[1,4],[0,3,7]],
"doppio": [2,[0,4,5,7]],
"basati": [[0,1,3,7]],
"reli": [2],
"minuscola": [0,4],
"book": [0],
"consentono": [0],
"show": [7,5],
"minuscolo": [[0,4]],
"alle": [8],
"l\'estension": [0,2,1],
"strutturalment": [2],
"alla": [0,[2,4],5,1,3,[6,7]],
"minuscoli": [0],
"lavoreranno": [2],
"basato": [3,8],
"comput": [2,[1,3]],
"arriva": [0],
"contien": [0,6,2,7,3,[1,5],4],
"situato": [7,2],
"un\'interruzion": [0],
"editortogglecursorlock": [0],
"arrivo": [0,4,2,3,1,5,6,7],
"enabl": [1,[4,5]],
"condivisa": [[2,6]],
"associ": [1,[0,2,4,7]],
"situata": [2],
"avvertimento": [7,[0,1],2,3,4,6,5],
"subfold": [7],
"new_fil": [7],
"situati": [[1,2]],
"quando": [2,0,1,7,5,3,[4,6]],
"abbreviata": [0],
"elaborazione": [8],
"target": [7,1,4,6,3,[0,8]],
"rinominarla": [2],
"capo": [0],
"sorgent": [2,0],
"condivisi": [[2,7],5],
"neanch": [1],
"condiviso": [2],
"config-dir": [2],
"restino": [6],
"blocchi": [[0,3],[7,8]],
"rinominarlo": [2],
"editorskipprevtokenwithselect": [0],
"visitato": [6],
"all\'espression": [0],
"rimuovi": [0,7,4],
"termbas": [0],
"sequenz": [0],
"finestr": [7,[0,1,3,4]],
"piano": [1],
"caso": [[0,2],4,[5,6],3],
"pulizia": [7],
"rappresentano": [1,0],
"casi": [2,4,0],
"rend": [[0,3],[2,5]],
"case": [7],
"visitati": [4],
"item": [7,4],
"frammenti": [[0,3]],
"nativament": [2],
"esistono": [0,2,4],
"targettext": [1],
"includono": [[0,3,4],[2,5]],
"reset": [7],
"style": [7],
"presentano": [[0,1]],
"comportamento": [2,4],
"card": [7],
"virgola": [0,2],
"all\'istant": [1],
"condivision": [2,[6,7]],
"selettiva": [2],
"individuano": [0,4],
"sarebbero": [2],
"proprio": [2,0,6,1,5,[3,7]],
"orang": [7],
"pattern": [7,2],
"cifra": [0],
"conteggiarl": [1],
"compil": [7],
"riproduc": [0],
"illustra": [0],
"propria": [2,0,1],
"mostrata": [[0,1]],
"edittagpaintermenuitem": [0],
"proprie": [8],
"ritardo": [2],
"adiacenti": [5],
"mostrato": [0,[2,4,5,7]],
"disattivarlo": [4],
"more": [7,5],
"display": [7],
"intrapresa": [4],
"unicod": [0,4],
"viewmarknbspcheckboxmenuitem": [0],
"mostrati": [[4,5,6],2],
"memorizzazione": [8],
"nient": [3],
"contrassegnerà": [[0,4]],
"projectmedcreatemenuitem": [0],
"indicazioni": [0],
"shut": [7],
"whitespac": [2],
"presentati": [[2,4,5]],
"utilizzabili": [[0,2,3]],
"conteggiati": [4,1],
"simpli": [[2,7]],
"presentata": [2],
"msgstr": [0],
"individuata": [0],
"automatiche": [8],
"maiusc": [4,0],
"solito": [2],
"individuato": [[3,4]],
"poter": [[6,7,8]],
"separar": [0,2,1],
"individuati": [[0,4],2],
"untransl": [7],
"evidenzierà": [0],
"daili": [0],
"facoltativo": [4,2],
"both": [7],
"important": [[0,6]],
"most": [7],
"sorgenti": [2],
"delimit": [7],
"inserisca": [4],
"omegat.project": [2,6,3,[1,5,7]],
"phrase": [7],
"excludedfold": [2],
"targetcountrycod": [0],
"inserisci": [0,4,[1,3],5,[2,6]],
"riport": [0,3],
"d\'origin": [3],
"altr": [2,0,3,[1,7],[5,6]],
"l\'utent": [0,4],
"escluder": [5],
"insert": [7,0,1],
"continu": [7],
"occasionalment": [3],
"alto": [1,5],
"rete": [2],
"highlight": [7],
"direttament": [2,4,3,6],
"automazion": [2],
"coppi": [2,0],
"sincronizzar": [[3,6]],
"marcator": [[1,5]],
"l\'intervallo": [1],
"move": [7],
"original": [[0,2],4,[3,5,7],[1,6]],
"also": [7,2],
"scaricato": [2],
"differ": [7],
"danneggiar": [2],
"consol": [2],
"allenarsi": [2],
"mous": [[4,5,7],[1,3,6]],
"resa": [0],
"alta": [5,1],
"sbc": [5],
"separatori": [[0,5]],
"consultar": [2,0],
"reso": [3],
"front": [3],
"resi": [3],
"annullar": [0],
"itokenizertarget": [2],
"viewmarkwhitespacecheckboxmenuitem": [0],
"l\'ordinaria": [0],
"equivalent": [5],
"oggetti": [2],
"configurazion": [2,0,1,4,3,7,5],
"avranno": [1],
"l\'eccezion": [2],
"complet": [[0,7]],
"bak": [2,6],
"tradott": [0,1,[4,7]],
"paragrafo": [0,7,[1,3,5],4],
"offer": [7],
"corrispondono": [7,[1,2,5],[0,3,6]],
"pulisci": [4],
"accedervi": [0,[2,4,6]],
"grigio": [4,3],
"paragrafi": [0,[4,5,7],1],
"distinguono": [0],
"complex": [7],
"ricarica": [4,6,[0,3,7]],
"segnalazion": [0],
"jre": [2],
"ufficio": [2],
"caricato": [2,6],
"posit": [7],
"popolata": [[2,6]],
"attivando": [4,5],
"reus": [7],
"caricati": [2,1],
"personal": [2],
"popolati": [[0,6],4,[1,2]],
"imposterà": [[2,7]],
"influenzar": [2],
"destro": [5,4,[2,6]],
"riporta": [0,5,[1,2,4]],
"destra": [0,5,4,[2,3]],
"l\'impostazion": [[0,5],[1,3]],
"alllemand": [7],
"schema": [1,[2,7]],
"vogliono": [2],
"permetti": [1,4],
"schemi": [1],
"icon": [5],
"delet": [0],
"bcp": [[3,7]],
"projectaccessglossarymenuitem": [0],
"javadoc": [7],
"see": [7],
"richieder": [[1,4]],
"sei": [3],
"indicata": [5,4],
"separazion": [4],
"indicati": [0,4],
"migliorato": [3],
"vada": [3,8],
"associar": [0,2],
"set": [7,[0,2],1],
"operano": [0],
"avvierà": [[0,1,2,4]],
"vist": [0],
"contain": [7],
"categorie": [8],
"parol": [0,[4,6],1,3],
"categoria": [0],
"column": [7],
"detta": [6],
"funzioneranno": [7],
"rispettivament": [6,[0,2]],
"procedura": [2,4],
"project.sav": [2],
"l\'elenco": [0,1,[3,4],[2,7]],
"tendina": [[0,1,7]],
"detto": [[0,2]],
"copierà": [5,3],
"featur": [7],
"offic": [0,[3,7]],
"chiusa": [7],
"frequenti": [[0,2]],
"repositories.properti": [[0,2]],
"genereranno": [4],
"ben": [3],
"consultarlo": [0],
"parti": [[1,2],[3,5],[0,4,6]],
"brevement": [0],
"repositories": [8],
"projectsavemenuitem": [0],
"terminologich": [0],
"terminologici": [0,4],
"xmx6g": [2],
"autocompletertablefirstinrow": [0],
"powerpoint": [0],
"digit": [7],
"forzar": [[0,1,4,7]],
"forzat": [6],
"nell\'operazion": [1],
"segnatament": [0],
"riparar": [2],
"trasmetter": [3],
"tmautoroot": [0],
"amministratori": [2],
"adjust": [7],
"chiuso": [5,[2,4]],
"l\'articolo": [0],
"compat": [2],
"compar": [5,7],
"cursor": [5,4,3,0,7],
"prototype-bas": [7],
"insertcharslrm": [0],
"abbina": [7],
"provar": [0],
"visualizzandolo": [2],
"applicata": [[0,2]],
"sia": [0,2,1,4,6,3,5],
"molti": [[0,2]],
"cambiando": [7],
"unisci": [1],
"validità": [0],
"sig": [1],
"dell\'elemento": [3],
"esempio": [0,[1,2],4,[3,5],7,6],
"richiest": [1],
"ordinati": [5],
"client": [2,[0,6]],
"applicato": [0],
"conversion": [2,1],
"capitoli": [3],
"corrisponda": [0],
"restano": [7],
"bound": [7],
"dell\'ultima": [[1,4]],
"comparir": [2],
"indicano": [0,4],
"falsi": [1],
"indicant": [1],
"attuali": [4],
"descrizioni": [[3,4]],
"correttezza": [[3,5]],
"capitolo": [2,7,3,0,[4,5]],
"racchiudono": [[0,3]],
"dell\'ultimo": [[1,6]],
"avoid": [7],
"targetroot": [0],
"prompt": [2],
"seguenti": [0,2,[4,5],[6,7,8]],
"select": [7],
"predizioni": [1],
"misur": [2],
"funzionant": [6],
"bin": [0,[1,2]],
"penalità": [6,1],
"dell\'altro": [2],
"l\'ultimo": [0,[4,5]],
"funzionano": [0],
"apertium": [1],
"cerca": [4,[0,1],7,3,5],
"bis": [0],
"kaptain": [2],
"meta-inf": [2],
"impedisc": [2],
"projectopenmenuitem": [0],
"autom": [[2,7]],
"offrono": [2],
"model": [7],
"inserirl": [7],
"necessario": [2,0,6,[1,4,5,7]],
"scorretti": [7],
"accento": [0],
"decid": [0,2],
"scorretto": [[2,4]],
"sistemati": [7],
"autor": [3],
"molto": [3,0,2],
"smoother": [7],
"inserisc": [4,5,2,[0,1]],
"necessaria": [[1,2]],
"squadra": [2,[1,3],[0,8]],
"mappatur": [2,7],
"ridurr": [7],
"l\'ultima": [[0,2,4]],
"contengono": [0,2,3,[4,5,7]],
"begin": [0],
"obbligatoria": [[0,7]],
"viewmarktranslatedsegmentscheckboxmenuitem": [0],
"paragraph": [7],
"valu": [7,0,2],
"utlizzar": [2],
"ilia": [2],
"conferma": [[0,1,6],[2,4]],
"vale": [5],
"simili": [2,[1,3,5]],
"blc": [5],
"programma": [3,[0,1],2,[5,6]],
"cinesi": [1],
"disattivar": [4,[5,7]],
"uxxxx": [0],
"semplicità": [2],
"obbligatorio": [7],
"macos": [8],
"programmi": [[0,2]],
"side": [7],
"break": [7],
"editselectfuzzy1menuitem": [0],
"rilevant": [2],
"posizionati": [2],
"maniera": [[3,4],0],
"blu": [5],
"scheda": [5,1],
"posizionato": [0],
"comfort": [7],
"hide": [7,5],
"all\'interfaccia": [[5,6]],
"azioni": [4,7,1,[0,2,5],8],
"extran": [7],
"piuttosto": [0,[2,4]],
"passando": [[4,5]],
"un\'applicazion": [2],
"interagivano": [2],
"l\'assegnazion": [3],
"autocompleterlistpagedown": [0],
"posizionata": [[2,3]],
"auto": [4,6,0,2,[1,7]],
"editorskipnexttokenwithselect": [0],
"combinazioni": [0,1],
"registrarsi": [2],
"applicano": [[3,7]],
"posto": [4,[2,7]],
"offra": [0],
"download": [2,7],
"split": [7],
"oracl": [0],
"editortoggleovertyp": [0],
"inserirà": [1],
"suggerita": [4],
"differenz": [1,[0,3]],
"different": [[0,1,2]],
"posta": [0],
"gradlew": [2],
"gerarchia": [6,2],
"ritornar": [5],
"dietro": [0],
"rifiuta": [2],
"modif": [0,7,2],
"interpretar": [0],
"consenti": [1,7,0],
"preserva": [0],
"errori": [4,[2,3],[1,7],0],
"viewmarklanguagecheckercheckboxmenuitem": [0],
"particolarment": [[5,7]],
"sceglier": [0,1,[2,7]],
"produc": [[2,5]],
"previsti": [2],
"inizia": [0,[2,4]],
"sottolineata": [5],
"box": [7],
"revisioni": [3],
"bloccar": [2],
"switch": [7,1],
"inizio": [0,4],
"inserita": [4,[0,1,6],5],
"total": [5,7,4],
"dettagliata": [0],
"tenta": [7,1,0,2],
"bundl": [1,[0,2]],
"interruzioni": [0,1],
"l\'installazion": [2,1],
"macchina": [1],
"involv": [7],
"sottolineato": [4],
"dynam": [7],
"logogramma": [0],
"scorretta": [6],
"macro": [7],
"inserito": [4,[0,1],[2,3,5,7]],
"src": [2],
"gigabyt": [2],
"control": [[0,4]],
"grassetto": [1,5,0,[3,7]],
"informa": [2],
"no-team": [2],
"dell": [0,2,4,7,1,3,5,6],
"l\'ambito": [[0,3]],
"incorpora": [1],
"sistemar": [7],
"lissens": [0],
"ospita": [5],
"assegn": [0],
"suggerisc": [[3,5]],
"possa": [2,3],
"ssc": [5],
"riutilizza": [4],
"nuovo": [[2,3],0,7,4,1,[6,8]],
"traduttor": [2,[0,1,3],6],
"ssh": [2],
"aumentar": [0],
"riutilizzo": [2],
"environ": [2,0],
"brevi": [1],
"esportar": [7],
"vari": [0,[2,3,4],7,[1,5],6],
"friend": [0],
"pinpoint": [7],
"secondari": [0],
"sta": [2,7,[1,5]],
"l\'apostrofo": [0],
"assegna": [0,3],
"generalment": [[0,5]],
"kde": [2],
"sto": [5],
"accept": [7],
"segnalarlo": [0],
"stampa": [[0,2]],
"principali": [2,[0,3]],
"principale": [8],
"mieifil": [2],
"sua": [2,6,[0,1,3,4,5],7],
"motor": [5,1,4],
"sue": [[0,2,6],5],
"access": [7],
"currenc": [7],
"sui": [2,0,3,[1,4]],
"languag": [7,2],
"nessun": [1,0],
"preferito": [7,[0,2]],
"sul": [2,[0,3,7],5,1,6],
"suo": [2,[5,6],[0,3]],
"chiudi": [0,[3,4]],
"apportata": [6],
"current": [7,[1,5]],
"sovrascritt": [2],
"preferita": [0],
"optionsglossaryfuzzymatchingcheckboxmenuitem": [0],
"porta": [2],
"key": [7,2],
"condividono": [[0,2,7]],
"fuori": [0,2,6],
"l\'approccio": [2],
"msgid": [0],
"svn": [2,7,6],
"launch": [7,2],
"store": [7],
"omegat-license.txt": [0],
"quell": [[1,6],[0,7],4,[2,3]],
"segnaposto": [1,0],
"stori": [0],
"confirm": [7],
"credenziali": [1,2,[5,8]],
"nuova": [2,[3,4],0,1,[5,6,7]],
"rapporti": [[2,7]],
"facoltà": [2],
"sovrascriva": [0],
"nuovi": [2,[0,3],[4,7]],
"editreplaceinprojectmenuitem": [0],
"un\'esportazion": [2],
"but": [[0,7],2],
"memorizzerà": [7],
"symbol": [7],
"dell\'utente": [8],
"editordeletenexttoken": [0],
"vanno": [[1,2]],
"installato": [2,1,[0,3,4,5]],
"express": [7],
"multilingu": [0],
"accoppiato": [0],
"quest": [7,[0,2],3,[1,6]],
"saper": [3],
"ripulir": [4],
"zero": [0,7,2],
"conflitti": [[0,2,3]],
"deactiv": [4],
"generati": [[0,1]],
"potrebbero": [2,0,3],
"l\'ordin": [[1,4,5,6]],
"rinominar": [2],
"conflitto": [2],
"generato": [2],
"variant": [2],
"generata": [2],
"soltanto": [0,[1,4],2],
"annulla": [0,4],
"written": [7],
"comparirà": [[0,1]],
"variano": [[0,2]],
"seleziona": [4,0,[1,3],[5,7]],
"gotoprevioussegmentmenuitem": [0],
"legenda": [4],
"accett": [1],
"composta": [[1,5]],
"facilitar": [3,[2,6,7]],
"dichiarazion": [0],
"gotopreviousnotemenuitem": [0],
"caffè": [3],
"stderr": [0],
"editredomenuitem": [0],
"verif": [0],
"composti": [0],
"uilayout.xml": [[0,6]],
"installati": [1,[3,4,7]],
"normali": [0,2],
"sourceroot": [0],
"quadro": [7],
"preferisc": [[0,6]],
"installata": [2],
"trovato": [0],
"ripristinati": [2],
"interromper": [0],
"inizierà": [3],
"sinc": [7],
"compatibili": [[1,2]],
"trovati": [3],
"interfaccia": [2],
"inatteso": [2],
"titolo": [[0,4]],
"coppia": [1,[0,2]],
"allegato": [1],
"quadra": [0],
"evidenziazion": [0],
"apport": [[0,1,2,5],[3,4]],
"seguendo": [5],
"conosciuti": [0],
"nidif": [0],
"assent": [2],
"devi": [3],
"deve": [0,2,6,[1,3]],
"restanti": [5,[2,4,7]],
"normal": [[0,7],[1,4,6]],
"figure": [8],
"figura": [[3,5]],
"implementa": [1],
"nome_utent": [2],
"example.email.org": [0],
"richieda": [[1,2]],
"accennato": [0],
"seconda": [[0,1,2],4,[3,5]],
"runtim": [2,0],
"individu": [7,0],
"aggiungono": [7],
"autenticazion": [2,1,5],
"differenza": [[0,1,7]],
"bianco": [0],
"specificano": [0],
"secondo": [5],
"review": [7],
"filenam": [7,0],
"tener": [2,0,[3,5]],
"secondi": [1],
"guida": [3,6,[0,4],[2,7],[1,5]],
"roam": [0],
"between": [7],
"guide": [8],
"certa": [2],
"interni": [[0,2]],
"nbsp": [7],
"comodo": [[3,7]],
"gotosegmentmenuitem": [0],
"interno": [2,[1,4,5]],
"eventualment": [[2,3]],
"preceduto": [0],
"generano": [2],
"preceduti": [0],
"l\'identificativo": [1],
"preceduta": [0],
"interna": [5],
"previsioni": [3],
"initialcreationd": [1],
"verrà": [0,2,[1,6],4,5],
"usando": [[3,6]],
"flag": [4,1],
"helpaboutmenuitem": [0],
"deriva": [[2,3]],
"modificatori": [0,[3,8],4],
"salvat": [[1,3,6]],
"eventuali": [[1,4]],
"salvar": [2,0],
"weak": [7],
"fattori": [4],
"tengano": [7],
"limitar": [0],
"apert": [1],
"parer": [5],
"creerà": [[2,5]],
"eccessivament": [1],
"place": [7],
"leav": [1],
"regular": [7],
"risoluzion": [[2,3],6],
"tabellar": [4],
"pochissimi": [4],
"sito": [2,[0,1]],
"certi": [[2,5,6]],
"generali": [[2,7],0],
"revisionar": [3],
"suggest": [5,1],
"elementi": [0,3,[1,4,5],[2,6]],
"identificator": [0,4],
"token": [0,[1,2,7],[5,6]],
"della": [2,0,1,5,4,3,6,7,8],
"filter": [7,2],
"maggior": [0,4,1],
"site": [1],
"traduzione": [8],
"installare": [8],
"elemento": [[0,3,4]],
"projectroot": [0],
"posizioni": [2,7,3,[0,1,6]],
"delle": [8],
"traduzioni": [5,6,3,2,1,[0,4],[7,8]],
"posizione": [8],
"arancion": [5],
"omegat.log": [0],
"localment": [2,4,1],
"dello": [[1,2],[0,6]],
"siti": [0],
"autocompletertableright": [0],
"ipotizzi": [0],
"aver": [2,3,[0,1],5,[4,7]],
"soglia": [1,[2,5]],
"garantir": [2],
"verso": [2,3],
"ingles": [2,[1,7]],
"tab": [0,4,1,5],
"generale": [8],
"divers": [[0,5],[3,6],[1,2,4]],
"l\'eliminazion": [[2,3]],
"plain": [7],
"should": [7,2],
"tag": [1,0,3,4,7,2,5,8],
"poterl": [2],
"spostarti": [3],
"tal": [[0,1,2]],
"versa": [7],
"all\'amministrator": [2],
"individuar": [0,2],
"onli": [7,1],
"filtrar": [[2,3]],
"projectreloadmenuitem": [0],
"almeno": [2],
"colleg": [3],
"tranquillament": [2],
"codificato": [6],
"person": [7],
"spostarsi": [5],
"disponibili": [0,2,1,[5,7],6,[3,4]],
"nell\'ordin": [5],
"ripetizioni": [4,[0,7]],
"servizi": [[1,2]],
"perdita": [2],
"tbx": [0,1],
"genererebb": [6],
"installano": [2],
"albero": [6],
"raggiunger": [3,2],
"can": [7,0,2],
"inclusion": [0],
"satisfi": [7],
"cat": [[0,3,7]],
"duser.countri": [2],
"provid": [7,[1,2]],
"sull\'uso": [2],
"match": [7,3],
"categori": [0],
"trovano": [1,[0,2],6,7,4],
"align.tmx": [2],
"posizion": [0,2,4,[1,3],[5,6],7],
"file2": [2],
"orfani": [5],
"permettono": [[0,4,7]],
"de-segmentati": [3]
};
