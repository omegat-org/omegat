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
 "付録A 辞書",
 "付録B 用語集 (Glossaries)",
 "付録D 正規表現",
 "付録E ショートカットのカスタマイズ",
 "付録C 綴り確認",
 "OmegaT のインストールと実行",
 "操作方法...",
 "OmegaT 4.2 - 取扱説明書",
 "メニュー",
 "ウィンドウ",
 "プロジェクトフォルダー",
 "ウィンドウとダイアログ"
];
wh.search_wordMap= {
"させることができます": [[5,9,11]],
"するための": [11,[4,8]],
"以前": [9,[6,11]],
"いたままにして": [11],
"するために": [11,5],
"灰色": [8],
"されなかった": [11],
"送信": [6,11],
"きます": [8,10,6,5],
"うこともできます": [6],
"けません": [6],
"不必要": [6],
"info.plist": [5],
"うことで": [11],
"機能改善": [8],
"原文中": [[3,11]],
"直接書": [1],
"しくは": [11,[6,9,10]],
"ただし": [11,[3,5,6,8]],
"fuzzi": [11],
"される": [11,10,[5,6,9],[1,7,8]],
"もできます": [11],
"添付": [8,9],
"のものと": [6],
"left": [11],
"いると": [5],
"にとって": [[6,11]],
"がこれにあたります": [5],
"バンドル": [5],
"dgoogle.api.key": [5],
"result": [2],
"edittagnextmissedmenuitem": [3],
"があいまいな": [11],
"利用可能": [[3,4,6]],
"動的言語": [11],
"quiet": [5],
"オブジェクトモデル": [11],
"プロキシー・サーバー": [11],
"es_es.d": [4],
"各部分": [11],
"the": [[0,2]],
"できなくなってしまいます": [4],
"めることができません": [11],
"具体的": [[5,11]],
"projectimportmenuitem": [3],
"きします": [[8,10]],
"選択範囲": [8,3,11],
"わります": [[6,8,10]],
"ぐことができます": [5],
"imag": [5],
"作業用": [5],
"分野": [6,[10,11]],
"実行": [5,11,8,6,7],
"途中": [[6,11]],
"トークナイザー": [11],
"言語": [11,5,[4,6],[0,7,8,9]],
"実行環境": [5],
"moodlephp": [5],
"名構成例": [11],
"currsegment.getsrctext": [11],
"ユーザー": [5,11,7,[2,10]],
"原因調査": [5],
"というような": [6],
"にしたい": [6],
"さらに": [11],
"ターミナル": [5],
"xxxx9xxxx9xxxxxxxx9xxx99xxxxx9xx9xxxxxxxxxx": [5],
"直接起動": [5],
"結合": [11],
"しかし": [6,11,5,[1,4]],
"めたい": [11],
"fr-fr": [4],
"されるとすぐに": [8],
"こりえることです": [10],
"たいていはこの": [10],
"でのみ": [[6,11]],
"にもよりますが": [6],
"ロシア": [5],
"つけるために": [1],
"もちろん": [10,[4,9]],
"つけると": [[6,11]],
"された": [11,8,6,10,5,9,1,[2,4]],
"いたいと": [3],
"うかを": [11],
"うためには": [4],
"webster": [0,[7,9]],
"されず": [6],
"えてもかまいません": [6],
"全分節": [11,8],
"サブディレクトリ": [6],
"使用時": [4,7],
"cjk": [11],
"重大": [11],
"複数形指定": [11],
"することがあります": [11],
"はまた": [6],
"であった": [6],
"手始": [11],
"文字列": [11,6,2],
"スクリプトウィンドウ": [8],
"はまず": [5],
"用紙送": [2],
"フィールド": [11,[6,8]],
"がつかなくなった": [9],
"いより": [11],
"empti": [5],
"とする": [11,2],
"コンソールモード": [5],
"詳細": [11,5,[6,8],[2,9,10]],
"実装": [5],
"全文": [9],
"するものが": [11],
"スライド": [11],
"tmx": [6,10,[5,11],8,[3,9]],
"右下角": [9],
"おめでとうございます": [11],
"repo_for_all_omegat_team_project": [6],
"変換中": [11],
"コンピューター": [5,4,[7,10,11]],
"無条件": [10],
"分割": [11],
"ルール": [11],
"テキストエディタ": [1],
"ページ": [8,[5,6],[3,11]],
"intel": [5,7],
"検索機能": [11],
"fr-ca": [11],
"mainmenushortcuts.properti": [3],
"するものと": [5],
"選択": [11,8,5,9,3,[1,4,6]],
"かれている": [5],
"cmd": [[6,11]],
"coach": [2],
"すこともできます": [11],
"しておくこともできます": [[5,10]],
"するいかなる": [5],
"ロック": [5],
"gotohistorybackmenuitem": [3],
"上下": [11],
"ボタン": [11,4,5,9],
"するものを": [11],
"powerpc": [5],
"になることがあります": [[8,9,11]],
"今回": [11],
"わせたもの": [11],
"られています": [11],
"こちらが": [11],
"カーソルセグメント": [8],
"つだけであることに": [6],
"経由": [5],
"かどうかを": [[8,10,11]],
"隣接": [9],
"容易": [11],
"うしかありません": [6],
"にはおなじみでしょうが": [11],
"ましくない": [8],
"とせず": [11],
"まれていない": [[6,11]],
"右上角": [9],
"があるかを": [11],
"うこと": [11],
"随時": [10],
"われることになります": [10],
"柔軟": [11],
"補完候補": [[3,8,11]],
"プロキシサーバー": [5],
"同名": [5],
"omegat.sourceforge.io": [5],
"原文文書": [6],
"することはできません": [[6,9]],
"レベル": [6],
"訳文": [11,6,8,9,3,10,[4,5],1],
"はできないのです": [11],
"ディレクトリー": [6],
"のひとつにすぎません": [6],
"翻訳作業中": [10],
"d.foo": [11],
"ローカル・コピー": [6],
"translat": [11,5,4],
"個別設定": [11],
"があるほうがよいでしょう": [11],
"められた": [11],
"けるかもしれません": [6],
"除外構成例集": [11],
"たとえ": [11],
"ルート": [6],
"有効化": [[3,9]],
"をひとつ": [6],
"自動補完": [3],
"docs_devel": [5],
"tsv": [1],
"ありません": [4],
"させておくことをおすすめします": [11],
"gnome": [5],
"しようとする": [11],
"理由": [[4,10,11]],
"後述": [3],
"しているので": [6],
"とした": [6],
"として": [11,5,6,9,3,4,[2,8]],
"じるとき": [8],
"参考訳文": [11,8,9,[3,10],6,7],
"にしておいてください": [9],
"定義済": [11,[2,5,7]],
"がついています": [11],
"識別情報": [11],
"csv": [1,5],
"n.n_linux.tar.bz2": [5],
"禁止": [11],
"でわかる": [5],
"ときどき": [4],
"すなわち": [11],
"耐性": [10],
"したり": [11,[2,3,9]],
"したら": [11,3],
"しており": [11],
"press": [3],
"極力抑": [5],
"だけの": [6],
"自動変更": [11],
"リポジトリマッピング": [[6,11]],
"一連": [11],
"用語分野": [11],
"相違点": [11],
"dmicrosoft.api.client_secret": [5],
"見積": [11],
"ヒント": [11,[3,4,6,9],7],
"大陸": [5],
"わることがあるようです": [8],
"ctrl": [3,11,9,[6,8],1,[0,10]],
"document": [[5,11]],
"していたりするはずの": [9],
"単語": [11,8,9,[1,2],[3,4],[5,10]],
"かれます": [8],
"しなければ": [11],
"必要": [6,5,11,4,[1,10],[0,2,9],[3,8]],
"うだけでよいことになります": [5],
"追加": [11,6,5,[1,3,8,10],[4,9]],
"翻訳内容": [[8,9],6],
"変更点": [5],
"のみならず": [6],
"バッチモード": [5],
"だけでなく": [[6,11]],
"そのような": [[6,11]],
"場合": [11,5,8,6,10,9,4,1,2,3],
"resourc": [5,11],
"りです": [11,[5,6],3],
"セグメント・テキスト": [[8,11]],
"team": [6],
"オペレーティングシステム": [10],
"xx_yy": [[6,11]],
"docx": [[6,11],8],
"えてください": [6],
"txt": [6,1,[9,11]],
"になっていない": [8],
"しておきましょう": [6],
"機能": [8,11,6,[1,3,4]],
"うため": [11],
"蘭英": [6],
"とまったく": [11],
"してから": [8,11,6],
"だけを": [11,6],
"徐々": [[6,11]],
"備考": [11],
"ローカリゼーションエンジニア": [6],
"別々": [11],
"source": [7],
"trnsl": [5],
"viewdisplaymodificationinfoselectedradiobuttonmenuitem": [3],
"index.html": [5],
"omegat.tmx": [6],
"編集領域": [9],
"とその": [11,2,[7,8]],
"ロケールコード": [11],
"diffrevers": [11],
"をします": [[5,6]],
"宣言": [11],
"つけやすくするために": [6],
"都合": [10],
"更新者名": [11],
"だけが": [11,[6,9]],
"しておく": [6,[3,5]],
"正規表現": [11,2,7,5,[3,4]],
"プロジェクトマッピング": [6],
"プロジェクト": [6,11,8,5,10,[3,9],1,4,[0,7]],
"されるような": [5],
"フィルタ": [6,[5,11]],
"領域": [8],
"project.gettranslationinfo": [11],
"中止": [5],
"アップデート": [9],
"一部": [8,[6,10,11],9],
"段落全体": [11],
"現在": [8,11,9,[3,6,10],5,1],
"したい": [11,5,[6,10]],
"start": [5,7],
"れるまでは": [[5,6]],
"典型的": [5],
"equal": [5],
"セグメント": [11,[1,8,10]],
"訳文分節内": [9],
"リモート・リポジトリ": [6],
"グレー": [11],
"コンピュータ": [[8,11]],
"optionsalwaysconfirmquitcheckboxmenuitem": [3],
"デフォルト・フィルタ": [6],
"しなくなった": [9],
"しない": [11,8,6,[3,4],[2,9]],
"これには": [6,[4,5,8]],
"地域化": [5],
"することもできます": [5,11,9,[1,4,6]],
"パーセンテージ": [9,11],
"プロジェクト・サイト": [11],
"されなくなったような": [11],
"enter": [11,[3,8],5],
"現在行": [9],
"られた": [5],
"中段": [2],
"applic": [5],
"bidi": [6],
"projectteamnewmenuitem": [3],
"分節情報": [10],
"バージョン": [5,6,8,10],
"かもしれません": [4],
"エクスポート": [6,10],
"該当": [11,8,4],
"れておくのがよいでしょう": [11],
"omegt": [5],
"進行状況": [10],
"んだら": [6],
"んだり": [11],
"例外規則": [11],
"omegat.jnlp": [5],
"されるのではなく": [11],
"チーム": [[6,11],[3,5,7]],
"申請後": [5],
"ぶこともあります": [6],
"コマンドラインエディター": [5],
"n.n_windows_without_jre.ex": [5],
"めます": [11],
"予想": [[1,7]],
"することです": [[5,6]],
"エラー": [6,[5,8]],
"翻訳自動反映": [11],
"代替言語": [6],
"べたように": [6],
"複数開": [11],
"prof": [11],
"個別": [11,[1,6]],
"となる": [11],
"リポジトリー": [6],
"dmicrosoft.api.client_id": [5],
"config-fil": [5],
"のものになります": [8],
"数字以外": [2],
"対応": [11,6,8,5,9,[0,2,4,10]],
"拡張機能": [11],
"についてはそれ": [11],
"したすべての": [11,5],
"があると": [11],
"上側": [9],
"system-user-nam": [11],
"ったまま": [10],
"format": [11],
"となるのは": [9],
"要素": [11,6],
"console.println": [11],
"ということです": [11],
"共有": [6,4],
"利点": [5],
"下側": [9],
"のために": [11],
"メモリー": [5],
"しても": [11,[5,9],6],
"ローカル・マシン": [11],
"リセット": [11],
"しては": [11,6],
"展開": [5,0],
"アイコンファイル": [5],
"わないようにしてください": [11],
"編集作業中": [11],
"それまでの": [[8,11]],
"project_files_show_on_load": [11],
"内部翻訳": [9,[8,11]],
"固定": [11],
"数字": [9,11,[2,6]],
"ltr": [6],
"optionsexttmxmenuitem": [3],
"えるよう": [6],
"コマンドラインウィンドウ": [5],
"build": [5],
"があれば": [[6,11]],
"marketplac": [5],
"ドロップダウンメニュー": [11],
"種類": [[6,8],[10,11]],
"とても": [11],
"以降": [[1,5]],
"entries.s": [11],
"翻訳者向": [9],
"だった": [11],
"グループ": [[2,11]],
"del": [[9,11]],
"各単語": [8],
"日本語訳注": [11],
"gotonextuntranslatedmenuitem": [3],
"targetlocal": [11],
"path": [5],
"上部": [11,9],
"拡張子違": [0],
"いています": [[1,5]],
"関数": [11],
"語辞書": [4],
"操作": [11,6],
"allsegments.tmx": [5],
"起動引数": [5],
"事態": [10],
"字体設定": [11],
"優先度": [[8,10,11]],
"helpcontentsmenuitem": [3],
"サインアップ": [5],
"みするたびに": [6],
"下部": [11,[8,9]],
"するものである": [11],
"omegat-org": [6],
"欧州連合": [6],
"descript": [[3,5]],
"詳細設定": [11],
"プロトタイプ": [11],
"projectaccessdictionarymenuitem": [3],
"最大化": [9],
"のどこかに": [4],
"あるいは": [5,6,[10,11]],
"解凍": [5],
"訳文項目": [11],
"最上段": [2],
"そのためには": [11],
"ランタイム": [5],
"duden": [9],
"right": [11],
"分節固有": [8],
"完了": [[6,9]],
"信頼": [11],
"空訳文": [11,8,3],
"文書中": [11],
"かっている": [10],
"ぶという": [4],
"のままなのかどうかを": [10],
"ビュー": [11],
"spotlight": [5],
"did": [11],
"検討": [6],
"判別": [[4,11]],
"参考": [[10,11],5],
"dir": [5],
"div": [11],
"うには": [5],
"しようとします": [11],
"何箇所": [11],
"じたとき": [6],
"viewfilelistmenuitem": [3],
"となるべく": [2],
"test": [5],
"支援": [11],
"omegat": [5,6,11,8,3,[7,10],4,9,1,0,2],
"検証": [11,5,[3,6]],
"めてください": [9],
"形式名": [11],
"していれば": [[5,11]],
"しませんが": [6],
"うので": [11],
"console-align": [5],
"ms-dos": [5],
"projectopenrecentmenuitem": [3],
"けできるのは": [6],
"からなります": [9],
"もっと": [11],
"キーワード": [11],
"うのが": [[10,11]],
"してもいいでしょう": [4],
"したならば": [5],
"られていない": [1],
"はすべて": [6],
"げられます": [10,[6,11]],
"und": [4],
"project_save.tmx.temporari": [6],
"別物": [11],
"構文定義": [3],
"editoverwritemachinetranslationmenuitem": [3],
"良好": [11],
"ingreek": [2],
"底部": [9],
"地域設定": [5],
"つからない": [11],
"指定例": [5],
"すこともあり": [11],
"es_es.aff": [4],
"ignor": [4],
"していき": [11],
"顧客向": [6],
"については": [5,11,[2,8,10]],
"projectexitmenuitem": [3],
"参考訳文候補": [10],
"についての": [[6,11],9],
"ります": [6,3,[1,5,10]],
"text": [[2,5]],
"メインメニュー": [11,[3,9]],
"卓上": [9],
"editregisteruntranslatedmenuitem": [3],
"init": [6],
"についても": [11],
"ロケール": [[5,11]],
"アドレス": [5],
"していること": [11],
"変更前後": [11],
"訳文言語": [[4,6],11,[5,9]],
"maco": [5,1],
"doc": [6],
"集計対象": [11],
"している": [5,11,9,[0,2,6]],
"ネットワーク": [[5,6]],
"タイトル": [[8,11]],
"mac": [3,[5,6]],
"独立": [9],
"優先": [5],
"ばれる": [11],
"アップグレード": [5,11],
"自動翻訳": [11],
"であっても": [5,[1,6]],
"man": [5],
"状態": [11,8,9,10,6],
"map": [6],
"全角": [11],
"ボックス": [11],
"may": [11],
"していて": [11],
"url": [6,11,[4,5,8]],
"認証付": [11],
"分割表示": [9],
"構文的": [11],
"とほぼ": [11],
"uppercasemenuitem": [3],
"viewmarkuntranslatedsegmentscheckboxmenuitem": [3],
"クリック": [11,5,8,9,4,1,6],
"しいことと": [5],
"glossaries": [7],
"既定値訳文": [9],
"のうちの": [9],
"していた": [8,4],
"ダイアログボックス": [8,11],
"そのまま": [5],
"作業内容": [5],
"オンライン": [4],
"サービス": [5,11,8],
"omegat.jar": [5,[6,11]],
"omegat.app": [5],
"usr": [5],
"になりません": [11],
"作業": [6,5,11],
"文法的": [11],
"日付": [6],
"ローカル・ディレクトリー": [8],
"utf": [1,11],
"したものとして": [11],
"グローバル": [11],
"サーバー": [6,11,5],
"本来": [9],
"無効": [11,8],
"dsl": [0],
"ホーム": [[0,1,2,3,4,5,6,8,9,10,11]],
"原文": [11,[6,8],[3,9],1,5,10],
"エディタペイン": [8],
"提供": [11,5,[0,4]],
"母音": [2],
"n.n_windows_without_jre.zip": [5],
"med": [8],
"自動改行": [8],
"en.wikipedia.org": [9],
"dtd": [5],
"ポップアップメニュー": [9,11],
"しているかを": [4],
"英語": [6,2,[5,11]],
"はじゅうぶんその": [6],
"make": [11],
"するかについてのみ": [6],
"projectcompilemenuitem": [3],
"パブリッシュ": [6],
"console-transl": [5],
"ビルド": [5,7],
"無効化": [1],
"アルゴリズム": [8,[3,11]],
"とじゅうぶん": [10],
"gotonextuniquemenuitem": [3],
"リポジトリ": [6,8,[5,11]],
"管理者": [11],
"optionsviewoptionsmenuitem": [3],
"区切": [11,1,5,[6,8]],
"commit": [6],
"targetlocalelcid": [11],
"チェッカー": [11],
"project_stats_match.txt": [10],
"行端寄": [6],
"しているなら": [2],
"いていた": [9],
"dvd": [6],
"選択肢": [11,8,5],
"xmx2048m": [5],
"えています": [[4,11]],
"対訳集": [5],
"区別": [11,2,[5,10]],
"自動通知": [11],
"進値": [2],
"xxxxxxxxxxxxxxxx.xxxxxxxxxxxxxxxx.xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx": [5],
"作業方法": [6],
"文学的": [11],
"文章": [6,[2,8,11]],
"右寄": [6],
"krunner": [5],
"libreoffic": [[4,6],11],
"されたりはしませんので": [11],
"とさずに": [11],
"いている": [8,11],
"めることができます": [3],
"配下": [11,0],
"ドロップ": [9,5,7],
"されていないものは": [9],
"のさらに": [1],
"カナダフランス": [11],
"背景色": [10,[8,11]],
"マスター・パスワード": [11],
"すことによって": [11],
"しません": [11,5,8,9],
"はまったく": [10],
"viewdisplaysegmentsourcecheckboxmenuitem": [3],
"editregisteremptymenuitem": [3],
"両側": [6],
"りのいずれかに": [6],
"プラグイン": [11],
"しています": [9,[5,6,8,11],4,[0,3]],
"open": [11],
"されていないすべての": [6],
"www.oracle.com": [5],
"します": [11,5,8,6,9,2,1,10,4,0,3],
"project": [5,11],
"xmx1024m": [5],
"取得": [[5,11]],
"マウス": [[8,9]],
"はすでに": [[5,11]],
"マッピングリポジトリ": [6],
"固有名詞": [11,9],
"しました": [6],
"開始時": [10],
"各種": [11,9],
"penalty-xxx": [10],
"gotonextsegmentmenuitem": [3],
"影響": [[5,11],[6,8,9]],
"カスタマイズ": [3,11,7,[2,5]],
"子音": [2],
"いくつかの": [11,[1,5,9]],
"nnn.nnn.nnn.nnn": [5],
"復元": [9,11,[3,6]],
"しているはずの": [5],
"abort": [5],
"されるのは": [8,11],
"文字": [2,11,1,[5,7,8]],
"をたどり": [8],
"非視覚的": [11],
"抽出": [11],
"以外": [2,[5,6,8]],
"じたり": [6],
"訳語": [[1,8]],
"われているものと": [[4,11]],
"存在": [11,[4,10],[8,9],[0,1,5],6],
"じでない": [11],
"セット": [[1,11]],
"いていても": [11],
"しますか": [0],
"になります": [11,[5,10],[4,9],[1,8]],
"一般的": [11,[6,8,9]],
"はいろいろあるでしょう": [10],
"作成後": [1],
"es-mx": [4],
"しますが": [[6,11]],
"十分": [11],
"単体": [2],
"安全": [6],
"行末記号": [2],
"くのに": [11],
"分以内": [6],
"単位": [[5,11]],
"stem": [9],
"をしているかを": [11],
"準拠": [6],
"保存": [11,[6,8],5,1,3,10],
"ケース": [6,[8,11]],
"分類": [8],
"スクリプト": [11,[5,8],7],
"できるような": [11],
"参考訳文表示": [11],
"直後": [[2,9],[5,8]],
"できるように": [11],
"ながら": [6],
"それぞれについて": [8],
"ることになります": [11],
"されているはずです": [6],
"見当": [[0,9]],
"文単位": [11],
"word": [[6,11]],
"平均": [11],
"前述": [6],
"直接入力": [11],
"完全": [6,[5,8,11],[1,3,10]],
"順序": [11,9],
"ソースフォルダ": [5],
"スペースバー": [11],
"太字": [[9,11],1],
"vcs": [6],
"lingvo": [0],
"のところ": [11],
"保管": [6,[8,11]],
"mrs": [11],
"電子版": [9],
"しのある": [9,8,[3,11]],
"言語版": [5],
"互換": [5],
"名称": [5,[8,9,11]],
"標準": [[1,4,8,9,11]],
"じです": [11],
"pt_pt.aff": [4],
"語配列": [11],
"るには": [9],
"みします": [8],
"html": [11,5],
"グレーアウト": [8],
"実体": [11],
"ソースセグメント": [9],
"フィードバック": [9],
"けになる": [5],
"させることもできますし": [4],
"翻訳総局": [8],
"artund": [4],
"わないために": [6,7],
"バックアップファイル": [6],
"ウェブサイト": [10],
"特定文書向": [6],
"プロジェクトマネージャ": [6],
"一時的": [[6,10]],
"外部変更": [8],
"しかもその": [6],
"しているため": [9],
"のしきい": [11],
"実例": [2],
"www.ibm.com": [5],
"てられた": [11],
"期間中": [6],
"使用可能": [11,[5,6]],
"ウインドウ": [[3,11]],
"記載": [[5,11]],
"フィルター": [11,6],
"じでも": [11],
"command": [[3,9],5],
"n.n_without_jr": [5],
"されたかどうかを": [8],
"機密保持": [11],
"ぶことができます": [[5,11]],
"まれています": [5,[2,6,7,10,11]],
"viewmarkbidicheckboxmenuitem": [3],
"できるほうが": [6],
"最上位": [10],
"にすでに": [11],
"利用": [11,[5,6],[0,3,4,9,10]],
"コマンドラインモード": [5],
"日本語": [[5,11],6],
"混在": [6],
"たらない": [9],
"一箇所翻訳": [11],
"翻訳状況": [8,[3,10],[6,11]],
"サブメニュー": [5],
"のままにします": [11],
"version": [5],
"まれません": [6],
"handl": [11],
"表記方向": [6],
"特殊": [6],
"がどれくらいあるかを": [11],
"最低": [[9,10]],
"はなるべく": [11],
"専用形式": [10],
"ロード": [6,8,11],
"projecteditmenuitem": [3],
"britannica": [0],
"行末": [[2,3]],
"通常通": [5],
"オプシ": [11],
"例外訳文": [9,11,[3,8]],
"テンプレート": [11],
"なしの": [11],
"wikipedia": [8],
"そもそもこのような": [10],
"構文一覧": [2],
"整形": [11,6],
"なしに": [11],
"スキャン": [6],
"たとえば": [11,6,4,5,[0,2],[8,10],9],
"iceni": [6],
"けたいという": [5],
"されるため": [11],
"選択履歴": [8,3],
"いか": [6],
"バックスラッシュ": [2],
"されない": [[1,11]],
"最大限": [11],
"送受信": [11],
"互換性": [5,11],
"あり": [2],
"付属": [[0,11]],
"ある": [6,10],
"されています": [[8,11],6,[9,10]],
"ローカル・ファイル": [6],
"認証情報": [11],
"していきます": [6],
"出力機能": [11],
"dsun.java2d.noddraw": [5],
"走査": [11],
"翻訳者": [6,[9,11],10],
"いた": [11,9,[1,3,8,10]],
"以上変更": [10],
"これらはすべて": [11],
"れてください": [11],
"することはありません": [5],
"いと": [11],
"いて": [6,[3,11],[2,5]],
"いで": [0],
"need": [11],
"型付": [11],
"このような": [11,6],
"んでおく": [4],
"をそれぞれの": [6],
"x0b": [2],
"すぎる": [11],
"メニュー": [3,7,[5,8],11,10,[0,6]],
"意味的": [11],
"いも": [11],
"http": [6,[5,11]],
"はこれが": [11],
"いま": [11],
"フェッチ": [11],
"いる": [5],
"うか": [4],
"いや": [10],
"マシン": [11],
"記述": [[5,11],3],
"することになる": [4],
"ギリシャ": [2],
"projectsinglecompilemenuitem": [3],
"最近": [[3,8]],
"文書内": [9],
"コマンド": [5,11,8,9,0],
"していない": [8,[1,5]],
"うと": [11,5],
"翻訳者側": [6],
"大部分": [4],
"myfil": [6],
"水準": [6],
"引数": [5,6],
"解除": [11,[5,9]],
"われます": [[6,11],[3,9]],
"とりわけ": [11],
"収拾": [9],
"使用中": [5,[0,7]],
"family": [7],
"識別基準": [11],
"言語設定": [11,4],
"りません": [0],
"訳注": [9],
"仕組": [[5,11]],
"翻訳後": [6],
"されなくなります": [5],
"system-os-nam": [11],
"ぐには": [11],
"optionstabadvancecheckboxmenuitem": [3],
"最終的": [6],
"えた": [[5,6,11]],
"論外": [6],
"えて": [[4,6]],
"生成後": [11],
"heapwis": [11],
"optionsviewoptionsmenuloginitem": [3],
"確認": [4,11,6,8,5,[0,1,2,3],[7,9]],
"しておくには": [11],
"えは": [6],
"tar.bz2": [0],
"のようになっています": [3],
"bundle.properti": [6],
"えば": [9,[5,8,11]],
"されていると": [11],
"スペイン": [4],
"複数形": [1],
"x64": [5],
"ファイルフィルター": [11,8,3],
"リネーム": [6],
"翻訳作業全体": [6],
"keyev": [3],
"える": [11,2,[5,6,7]],
"触発": [11],
"があがります": [11],
"じます": [11,[8,9]],
"併記": [10],
"isn\'t": [2],
"えを": [6],
"できますが": [6,[1,9,11]],
"取扱": [6],
"いでしょう": [11],
"されていれば": [5],
"展開先": [0],
"optionsteammenuitem": [3],
"gzip": [10],
"マップ": [6],
"表示状態": [9],
"置換用": [11],
"esc": [11],
"結合前": [11],
"ダブルクリック": [5,11,[8,9]],
"x86": [5],
"かな": [11],
"またはすべて": [8],
"nostemscor": [11],
"かつ": [[6,8]],
"かの": [6],
"かに": [11],
"翻訳": [6,11,10,9,8,5,7,2,4],
"画面": [5,11],
"けるべきです": [11],
"作業用翻訳": [10],
"いていたり": [9],
"上限": [11],
"console-createpseudotranslatetmx": [5],
"計算方法": [11],
"エスケープ": [2,5,1],
"longman": [0],
"から": [11,6,5,[9,10],4,[3,8],[1,7],2],
"下記参照": [5],
"fuzzyflag": [11],
"独自規則": [11],
"merriam": [0,[7,9]],
"きく": [11,5],
"projectname-omegat.tmx": [6],
"きし": [9],
"インストール・フォルダー": [11],
"っているすべての": [6],
"するにあたって": [11],
"てることができます": [3],
"分節規則": [11],
"きで": [11],
"更新履歴": [[3,8]],
"きの": [8],
"初期設定": [11],
"フルパス": [11],
"アプリケーション": [5,6],
"リアルタイム": [9],
"n.n_without_jre.zip": [5],
"くか": [6],
"するにつれ": [11],
"システム": [5,[6,11],[3,7]],
"くお": [6],
"magento": [5],
"えません": [[6,8,11]],
"しておいてください": [5,11],
"をそのまま": [11],
"くし": [11],
"ここでの": [6],
"みますが": [11],
"くよう": [6],
"めったにないことですが": [11],
"くの": [11,[5,6]],
"u00a": [11],
"ここでは": [11],
"くと": [[1,3,5,6,10]],
"ユーザーグループ": [6],
"shift": [3,[6,11],8,1],
"複数存在": [[8,9,10]],
"命令": [11],
"エディター": [11],
"java": [5,11,3,2,[6,7]],
"端末": [5],
"もありますし": [11],
"エンコーディング": [11,1,7],
"使用例": [2,[7,11]],
"project_save.tmx": [6,10,11],
"上図": [4,9],
"dictionari": [0,4],
"確認機能": [4,[7,10],11],
"コピー": [6,11,9,[4,8],5,10],
"閲覧": [5],
"プロジェクトパラメータ": [6],
"められるようにするには": [11],
"dictionary": [7],
"けは": [8],
"訳文生成時": [11],
"けの": [[2,3,9,11]],
"けに": [9,[3,4,5,11]],
"改行": [11,2],
"下図": [9],
"すでに": [[3,4,6,8,10]],
"投稿": [6],
"循環": [3],
"いはわずかです": [11],
"編集内容": [11],
"プレーン・テキスト・ファイル": [6],
"意見": [9],
"こみいった": [2],
"ヘルプメニュー": [3,7],
"がるかもしれません": [6],
"結果的": [6],
"ける": [8],
"言語用": [4],
"timestamp": [11],
"projectaccessrootmenuitem": [3],
"してください": [11,5,6,3,4,2,10,8,9,0],
"dyandex.api.key": [5],
"してまわる": [9],
"ここ": [11],
"けを": [8],
"えていたり": [5],
"こそ": [10],
"不自然": [11],
"分節": [11,8,9,3,[6,10],1,5],
"つのうちのどれかです": [10],
"プロジェクトツリー": [10],
"コンテキスト": [[6,11]],
"plugin": [11],
"つことができますが": [1],
"正規表現例": [[2,7]],
"とします": [11],
"したくなったら": [11],
"この": [11,5,[6,8],10,[2,9],[0,1,4,7]],
"初期値": [11,9],
"もありません": [6],
"editinsertsourcemenuitem": [3],
"にあり": [1],
"viterbi": [11],
"すことになります": [11],
"microsoft": [11,[5,6]],
"にある": [5,11,6,[2,8,9],[1,4]],
"projectnewmenuitem": [3],
"緑色背景": [9],
"ごと": [6],
"最上部": [11],
"optionstranstipsenablemenuitem": [3],
"ｔｗ": [5],
"ファジーマッチ": [[9,11]],
"単独": [11],
"glossari": [1,6,[0,4,11]],
"まります": [[6,11]],
"単語数": [8],
"言語間": [[6,11]],
"ignored_words.txt": [10],
"configuration.properti": [5],
"github.com": [6],
"紫色": [8],
"最下部": [11],
"これにより": [10,[5,6,11]],
"さな": [[5,8]],
"コードスニペット": [11],
"自体": [2,[5,6]],
"たらなければ": [0],
"string": [5],
"っておいたほうがよいでしょう": [4],
"個人設定": [[5,8,11]],
"プラットフォーム": [5,[1,11]],
"めないでしょう": [6],
"自動保存": [6,8],
"再読": [11,8,[1,3,6]],
"しい": [6,11,5,8,3,4,1,[2,9,10]],
"推奨": [11],
"段落間": [8],
"制限": [[5,6,11]],
"され": [[9,11],5,6,[1,8]],
"しか": [[4,8]],
"not": [11,5],
"しが": [11],
"できません": [[8,9]],
"用語集": [1,11,9,3,7,[8,10],6,[0,4]],
"名以外": [11],
"されずに": [11],
"しく": [[6,8],5,1],
"全体": [8,11,[5,10]],
"してみてください": [[2,5,11]],
"可能": [1,11,[5,6],[3,8,10]],
"was": [11],
"して": [11,6,5,8,9,4,10],
"selection.txt": [11,8],
"した": [11,5,6,8,9,4,[2,3,10],[0,1]],
"xhtml": [11],
"じく": [6],
"しの": [11],
"いてもよいでしょう": [5],
"ローカルファイルマッピング": [6],
"しに": [11],
"finder.xml": [11],
"じか": [11],
"くたびに": [6],
"window": [5,[0,2,8,11]],
"接頭辞": [10],
"まれているだけの": [11],
"エディタ": [11,9],
"じた": [[1,8,11]],
"ちます": [11,5],
"じて": [11,6,[5,8,9,10]],
"disable-project-lock": [5],
"辞書": [4,0,9,[7,10],[8,11],6,[1,3]],
"オートコンプリート": [1],
"omegat.pref": [11],
"なのは": [5,10],
"最初": [11,[5,8],10,[1,6],[2,3,9]],
"すか": [[8,11]],
"通知": [6,5],
"しを": [11,9,3],
"中国語": [6,11],
"そうすると": [10],
"空白圧縮": [11],
"取扱説明書": [7,[6,8],[3,5]],
"にしています": [2],
"howto": [6],
"すと": [[8,11],[6,9],4],
"んでいる": [11,6],
"pt_pt.dic": [4],
"暗黙": [11],
"有無": [11],
"じる": [11,[6,8],3],
"ウィンドウ": [11,9,8,6,7,10,[1,4]],
"level1": [6],
"level2": [6],
"直接": [10],
"非公開": [11],
"文字自身": [11],
"アルファベット": [11,6],
"置換件数": [11],
"小数": [11],
"くために": [3],
"をすると": [11],
"空白": [11,2],
"ずつ": [11,10],
"再利用": [6,[7,11]],
"構築": [5],
"する": [11,6,5,8,9,10,1,4,7,2,[0,3]],
"ることはできます": [11],
"web": [5,[6,7]],
"されていることを": [6],
"en-us_de_project": [6],
"editselectfuzzy4menuitem": [3],
"editregisteridenticalmenuitem": [3],
"ここでいう": [11],
"えることによって": [5],
"キャリッジリターン": [2],
"そうすれば": [[4,10]],
"双方": [11],
"けたい": [6],
"更新日時": [8],
"訳文分節欄": [11],
"分節数": [11,9,8],
"テキストフィルター": [11],
"したとき": [6],
"pt_br.dic": [4],
"線上": [11],
"unabridg": [0],
"などを": [[5,6]],
"やその": [5],
"辞書内": [8,11],
"されていることが": [9],
"optionsglossaryexactmatchcheckboxmenuitem": [3],
"されたら": [5],
"とともに": [11],
"されたり": [11],
"チームプロジェクト": [6,8,7,[3,5,10,11]],
"効果的": [11],
"などは": [5],
"nnnn": [9,5],
"などの": [[9,11],[0,5,6,8]],
"直接修正": [11],
"前回保存": [6],
"その": [11,5,9,6,10,8,[0,4],[1,2,3,7]],
"などに": [[2,9]],
"うとよいでしょう": [11],
"番号": [11,8,[5,9],1],
"関連性": [[3,8,11]],
"登場順": [11],
"リモートデスクトップ": [5],
"それ": [[1,8,10]],
"語幹処理": [1,3],
"プログラムフォルダー": [5],
"myproject": [6],
"えることができます": [[6,11],5],
"するさまざまな": [11],
"zh_cn.tmx": [6],
"直線移動": [11],
"しかもそれぞれの": [10],
"形式": [11,6,1,8,0,[5,9,10],[3,7]],
"圧縮": [[0,10]],
"孤立": [[6,9]],
"たな": [11],
"非空白文字": [2],
"にすべての": [11],
"セットアップ": [6],
"みしてください": [11],
"いておくことができます": [[10,11]],
"だけ": [[10,11]],
"だけから": [11],
"archiv": [5],
"反映": [10,[3,6,8]],
"変換": [8,6,11],
"repo_for_omegat_team_project.git": [6],
"user": [5],
"識別時": [11],
"ワイルドカード": [11,6],
"extens": [11],
"効果": [5],
"記録": [11,10,8],
"集計": [8],
"階層構造": [10],
"新規作成": [[3,8,11]],
"登録": [[5,8],[3,9,11]],
"にして": [5],
"sure": [11],
"されたなら": [6],
"最小一致率": [11],
"にした": [[5,6]],
"diff": [11],
"an": [2],
"editmultiplealtern": [3],
"まりとして": [9],
"git.code.sf.net": [5],
"サプライヤ": [11],
"対象外": [[5,11]],
"各分節": [8],
"否定": [2],
"be": [11],
"にする": [11,[3,4,8]],
"アクション": [8],
"filters.xml": [6,[10,11]],
"自動的": [11,8,[5,6],9,[1,3]],
"れないと": [11],
"br": [11,5],
"search": [2],
"検出": [[1,4,5,8]],
"ポート": [5],
"一度翻訳": [6],
"になることもあるでしょう": [11],
"フリー": [[4,11]],
"segmentation.conf": [6,[5,10,11]],
"った": [11,9],
"するわけではありません": [6],
"cd": [5,6],
"条件欄": [11],
"ce": [5],
"öäüqwß": [11],
"プロジェクトメニュー": [3,7],
"って": [11,[4,5],[0,6,9]],
"スキップ": [11],
"cn": [5],
"いたとき": [6],
"文脈": [[9,11],8],
"cr": [2],
"独自": [11,2],
"様子": [0,7],
"cx": [2],
"apach": [[4,6],11],
"がいったん": [6],
"yyyymmddhhnn": [6],
"adjustedscor": [11],
"dd": [6],
"つに": [[6,11]],
"づけ": [8,3,9],
"管理": [6,10],
"f1": [3],
"つの": [11,6,5,9,[4,8],1,[0,2,10]],
"f2": [9,[5,11]],
"f3": [[3,8]],
"つは": [9],
"dr": [11],
"f5": [3],
"スタイル": [[6,11]],
"タイミング": [11,10],
"dz": [0],
"することによって": [5],
"editundomenuitem": [3],
"ったものを": [9],
"u000a": [2],
"していないものがありますが": [0],
"げられて": [10],
"実用上": [9],
"つを": [9],
"en": [5],
"u000d": [2],
"u000c": [2],
"eu": [8],
"いられます": [11],
"つまたは": [[1,11]],
"芸術的": [11],
"注意事項": [[9,11]],
"サイズ": [11],
"チェックアウト": [6],
"でき": [9],
"フラグ": [[2,7,11]],
"u001b": [2],
"stats.txt": [10],
"翻訳会社": [9],
"共通": [11],
"てて": [5],
"foo": [11],
"exclud": [6],
"for": [11],
"fr": [5,[4,11]],
"再度開": [6],
"記号": [5,[1,11]],
"content": [5],
"です": [11,6,5,[8,9],10,1,4,[0,2],[3,7]],
"applescript": [5],
"めします": [11],
"問題": [8,[1,6],[4,5,7]],
"gb": [5],
"有効": [11,8,5,1,[2,4,9,10]],
"てはまるものです": [11],
"class": [11],
"helplogmenuitem": [3],
"での": [6,11,[2,9]],
"とが": [10],
"ディストリビューション": [5],
"オペレーティング・システム": [8],
"editoverwritetranslationmenuitem": [3],
"ディレクトリ": [8],
"aeiou": [2],
"では": [11,[5,6],9,8,[2,4],[3,10]],
"現在分節": [9,6],
"になっています": [11],
"form": [5],
"ホームフォルダー": [5],
"けであったり": [6],
"でも": [11,[5,6],[4,8,9,10]],
"小数点": [11],
"hh": [6],
"との": [11,9],
"とは": [11,9],
"duser.languag": [5],
"させるには": [6],
"されていません": [6],
"れなくなり": [11],
"されていない": [[5,8],1],
"コミット": [6,8],
"file-target-encod": [11],
"めるようにすることができます": [11],
"上記以外": [6],
"context": [9],
"https": [6,5,[9,11]],
"id": [11,[5,6]],
"ない": [[2,6]],
"とも": [[5,6,11]],
"if": [11],
"project_stats.txt": [11],
"ocr": [[6,11]],
"規則集": [11],
"projectaccesscurrenttargetdocumentmenuitem": [3],
"なく": [[1,6]],
"どの": [5,11,[4,9]],
"されているかどうかによって": [5],
"in": [11],
"ip": [5],
"にだけ": [11],
"したいときに": [11],
"index": [11],
"is": [2],
"フランス": [11],
"なし": [11,5],
"it": [11],
"とを": [5],
"ウェブページ": [11],
"odf": [6,11],
"ずその": [11],
"odg": [6],
"をあらわします": [11],
"したものではない": [11],
"ということがあらかじめはっきりと": [10],
"ja": [5],
"など": [11,[6,10],[5,9]],
"過去": [6,[9,11]],
"容量": [5],
"odt": [6,11],
"gotonexttranslatedmenuitem": [3],
"によってはいくつかの": [5],
"jp": [[5,11]],
"nplural": [11],
"js": [11],
"にし": [11],
"自分": [11,6],
"としていますが": [6],
"らかさないように": [11],
"削除": [11,6,[5,10],4,[8,9],3],
"learned_words.txt": [10],
"翻訳入力": [11],
"えます": [8,11,6,[2,5]],
"都度": [11],
"ftl": [5],
"には": [11,5,9,6,10,3,[0,8],1,4],
"ftp": [11],
"ってください": [[5,6]],
"なる": [11,8,[6,9],10],
"viewdisplaymodificationinfoallradiobuttonmenuitem": [3],
"draw": [6],
"にも": [11,[2,8,9,10]],
"修復": [6],
"頻繁": [6],
"入力用翻訳": [6],
"lf": [2],
"dswing.aatext": [5],
"していないことに": [4],
"すことができます": [11],
"こうしておけば": [11],
"lu": [2],
"変数": [11],
"みなすことができます": [6],
"cycleswitchcasemenuitem": [3],
"きやすい": [6],
"mb": [5],
"れなくても": [11],
"me": [6],
"フルテキスト": [11],
"たいてい": [2],
"れます": [[4,8,11]],
"はさておき": [4],
"をすべて": [11],
"すべて": [3,[9,11],[4,6,8]],
"omegat.png": [5],
"ねて": [9],
"mm": [6],
"であるか": [11],
"entri": [11],
"作業者": [8],
"mr": [11],
"ms": [11],
"mt": [10],
"ツリー": [10],
"my": [6,5],
"はい": [5],
"じてもよいかどうか": [8],
"すべき": [11],
"つかった": [11,[9,10],6],
"にならなくなることがあるので": [11],
"nl": [6],
"辞書機能": [0],
"nn": [6],
"no": [11],
"一番望": [10],
"されるまでしばらく": [4],
"やはり": [5],
"code": [5],
"余分": [2],
"gotohistoryforwardmenuitem": [3],
"探索": [11],
"字幕": [5],
"of": [0],
"のみ": [[3,8,11]],
"契約": [5],
"ファイルフィルターダイアログ": [11],
"入力": [11,5,6,8,9,2,10],
"ok": [[5,8]],
"ハイフン": [5],
"であれば": [[6,8],11,[0,4,5,10]],
"os": [5,11,1,[6,8]],
"見映": [6],
"にその": [[6,9,11]],
"発生": [5,6],
"のままになる": [11],
"editinserttranslationmenuitem": [3],
"プロキシサーバ": [5],
"オレンジ": [8],
"現在使用中": [8],
"判定": [11],
"一番書": [6],
"対訳": [9,8,5,6],
"po": [11,[5,9]],
"メガバイト": [5],
"optionsglossarystemmingcheckboxmenuitem": [3],
"pt": [5],
"マスター": [11],
"わないようにすることができます": [11],
"特典": [5],
"ストレージ": [6],
"蓄積": [9,6],
"ばれ": [5],
"したものや": [9],
"あくまで": [11],
"形態": [10],
"にするには": [11,[4,5]],
"edit": [8],
"たくさんあります": [2],
"editselectfuzzy5menuitem": [3],
"したくない": [11],
"したものの": [1],
"rc": [5],
"もっとも": [[4,5,9]],
"一覧表示": [8,11],
"includ": [6],
"していることが": [9],
"万字": [5],
"自動": [11,8,4],
"したものと": [6],
"ブロックレベル": [11],
"としたい": [2],
"になっているか": [[6,9]],
"既知": [9],
"sc": [2],
"生成時": [11],
"括弧内": [11],
"青字": [9],
"わないためには": [6],
"文字数制限": [9],
"翻訳時": [11,6],
"訳文分節中": [8],
"so": [11],
"現在開": [8],
"イメージ": [11],
"になっている": [11,8],
"両方向": [6],
"づけられていることを": [5],
"各自": [6],
"確保": [5],
"editoverwritesourcemenuitem": [3],
"整数": [11],
"関係": [[1,9,11]],
"enforc": [10],
"れると": [11,10],
"中国": [5],
"tm": [10,6,8,[5,7,9,11]],
"to": [5,[4,11]],
"v2": [5],
"にない": [1],
"document.xx": [11],
"ステップ": [6,10],
"インターネット": [[4,11]],
"同意": [5],
"ろでは": [11],
"viewmarkautopopulatedcheckboxmenuitem": [3],
"projectwikiimportmenuitem": [3],
"ui": [6],
"リクエスト": [8],
"までのすべての": [2],
"小文字設定": [3,8],
"文章中": [6],
"接続": [[4,6]],
"複数単語": [1],
"どちらの": [6],
"表記": [6,7],
"のものより": [11],
"テキストファイル": [11,[1,6,8]],
"しやすいように": [11],
"this": [[2,11]],
"解決法": [6],
"適切": [5,6,4,[1,11],10],
"をとっておきましょう": [6],
"しているものです": [2],
"メモリフォルダー": [6],
"vi": [5],
"ぶと": [8,10,11],
"プレーンテキストファイル": [6,11],
"特別": [11],
"くなることがあるので": [6],
"テスト": [[2,11]],
"一致数": [9],
"番目": [9,1,[5,8]],
"保持": [11,10],
"関連": [[5,11],6],
"実際": [[6,11],[0,5]],
"groovy.codehaus.org": [11],
"repo_for_omegat_team_project": [6],
"backspac": [11],
"セッション": [11],
"プロキシー": [11],
"emac": [5],
"org": [6],
"合致": [11],
"distribut": [5],
"マニュアル": [5],
"xf": [5],
"への": [5,6,[8,11],[1,4]],
"リモート・ファイル": [6],
"テクノロジー": [5],
"にしてください": [11,4],
"辞書項目": [[8,11]],
"すもの": [2],
"変更": [11,6,5,10,[4,8,9],1,3],
"べた": [5],
"xx": [5,11],
"xy": [2],
"sourc": [6,10,[5,11],8,9],
"それらはすべて": [3],
"つことに": [11],
"type": [6,[3,11]],
"らすために": [6],
"toolssinglevalidatetagsmenuitem": [3],
"ったら": [3],
"翻訳入力行": [9,11,[3,10]],
"けます": [8,11],
"していなかった": [8],
"projectaccesssourcemenuitem": [3],
"脚注": [11],
"除外": [6,11],
"yy": [9,11],
"すればよいのです": [6],
"命令型": [11],
"段落": [11,6],
"しないときに": [9],
"push": [6],
"用語集一致": [1],
"これらを": [6],
"readme_tr.txt": [6],
"有用": [11],
"べる": [5],
"参考訳文挿入": [11],
"penalti": [10],
"分節化規則設定": [6],
"されないことに": [11],
"まれている": [[1,6]],
"としては": [11,5],
"固有": [11,[8,10]],
"左側": [11,8],
"utf8": [1,8],
"ほど": [4],
"ダイアグラム": [11],
"でないすべての": [11],
"直接編集": [[6,11]],
"限定": [11],
"にします": [11,8,[2,5]],
"power": [11],
"のままにして": [11],
"tag-valid": [5],
"辞書用": [4],
"プロジェクト・マネージャー": [6],
"提示": [9],
"複数訳文": [11,9,7],
"えられます": [[5,6]],
"イベント": [3],
"各用語集": [11],
"訳文分節": [8,11,6],
"文字列長": [5],
"u0009": [2],
"xhh": [2],
"revis": [0],
"u0007": [2],
"repositori": [6,10],
"検索対象": [11,[2,9]],
"するが": [9],
"するか": [11,6,5,4],
"属性": [11],
"翻訳対象": [11,8,[3,7,9,10]],
"lowercasemenuitem": [3],
"wiki": [[0,9]],
"firefox": [[4,11],2],
"ツール": [11,[2,8],6,7,10,[3,5]],
"照合": [[8,11]],
"ブロック": [2,7],
"できるという": [5],
"まず": [11,[4,6],5],
"のひとつに": [11],
"空分節": [11],
"sens": [11],
"まだ": [8],
"また": [11,[9,10],[5,6],[3,4,8]],
"docs.oracle.com": [11],
"みか": [11],
"再整合保留中": [11],
"みが": [11],
"完全一致検索": [11,1],
"カテゴリ": [2,7],
"まで": [11],
"クロスプラットフォーム": [5],
"特化": [11,9],
"作成時": [11],
"openoffic": [4,[6,11]],
"カラム": [1],
"修飾子": [3],
"プログラミング": [11],
"リードミー": [5],
"optionsautocompletechartablemenuitem": [3],
"すれば": [[4,5,6,8]],
"くときに": [6],
"にのみ": [11,[6,8]],
"みの": [11,[4,5,9],[2,7,8,10]],
"しようとすると": [6],
"みに": [5],
"まる": [2,[3,11]],
"仮想": [11],
"みで": [[5,11]],
"許可": [11,[5,8]],
"ルートフォルダ": [3],
"未翻訳": [11,8,9],
"git": [6,[5,10]],
"正規表現関連": [2,7],
"ましいでしょう": [[6,11]],
"したいか": [6],
"xx-yy": [11],
"もせずに": [11],
"をすでに": [5],
"翻訳情報": [6],
"びまで": [11],
"これより": [5],
"これらは": [6],
"すると": [11,5,9,[6,8],[0,1,4,10]],
"これらの": [[8,11],10,4],
"多少下": [6],
"むと": [11],
"先頭": [11,8,[2,3,5,9,10]],
"optionsspellcheckmenuitem": [3],
"びます": [[5,11]],
"めた": [5],
"optionssetupfilefiltersmenuitem": [3],
"コード": [3,11,4,6,5],
"評価": [11],
"することが": [11],
"情報": [5,[6,11],8,[0,2,9]],
"altgraph": [3],
"することも": [[1,5]],
"もう": [11,[6,9]],
"めて": [[6,8,11]],
"顧客": [6,[5,9]],
"内部": [11,10],
"めに": [[4,11]],
"トークナイザ": [8],
"アプリケーションフォルダ": [5],
"うために": [[6,11]],
"メディア": [6],
"検索条件": [11],
"することは": [[6,11]],
"もし": [11,[5,6],[0,4,8,9]],
"ブラウザ": [[5,8]],
"キーストローク": [3],
"xml": [11],
"することで": [5,11,9],
"知識": [6],
"のいずれかを": [[8,11]],
"検出結果": [11],
"されたものであれ": [11],
"xmx": [5],
"往復": [6],
"拡張子": [11,1,0,6,[5,9,10]],
"インライン": [11],
"などとも": [11],
"マージ": [6],
"対象": [11,5,[2,6]],
"としています": [5],
"マーク": [11,[1,8]],
"める": [[5,11]],
"することを": [5],
"概要": [7],
"チェック": [11,8,[4,6],5],
"util": [[5,11]],
"のいずれかで": [6],
"ホームページ": [6],
"スタートメニュー": [5],
"tar.bz": [0],
"インスタンス": [5,8],
"それぞれの": [11,[5,6,8,9]],
"品質": [8,[6,10]],
"文字色": [8],
"パラメータ": [11,[5,6]],
"条件付": [11],
"んでください": [9,4],
"せずに": [[5,11]],
"それぞれで": [6],
"のすべての": [6,11],
"つまりすべての": [8],
"あえて": [11],
"表示方法": [11],
"逆変換": [6],
"ファジー": [11],
"がどのように": [6],
"xlsx": [11],
"使用方法": [11],
"ラインフィード": [2],
"しやすい": [11],
"assembledist": [5],
"とみなすが": [11],
"めるかどうかを": [11],
"ばれます": [5,11],
"漢字": [11],
"順次読": [10],
"target.txt": [11],
"するためには": [[5,11]],
"翻訳作業者": [6],
"やり": [[3,8]],
"場所": [5,[10,11],6,9,[0,1,4]],
"カンマ": [11,2,1],
"とみなして": [11],
"ることはできません": [11],
"nameon": [11],
"からだと": [5],
"自由": [[0,7]],
"すのはとても": [6],
"英文字": [8],
"optionsglossarytbxdisplaycontextcheckboxmenuitem": [3],
"みになっていることから": [9],
"gotonextnotemenuitem": [3],
"tar.gz": [5],
"gpl": [0],
"とみなされ": [11],
"専門用語": [[6,9]],
"品質保証": [8],
"ソフト": [11],
"一度": [8,9],
"改訂者": [11],
"したほうがより": [4],
"スペルチェッカー": [11],
"にかかわらず": [6],
"azur": [5],
"リモートフォルダ": [[6,11]],
"セパレーター": [9],
"よく": [5],
"ダウンロード": [5,[0,11],6,[3,4,7,8]],
"状況": [6,[5,11]],
"重複": [11,2],
"減算": [2],
"そのものを": [[6,11]],
"んでいるかを": [9],
"属性値": [11],
"生成": [6,[8,11],10,3,[5,9]],
"などです": [11,[0,6]],
"必須": [11],
"with": [5],
"説明": [6,11,[5,7,10]],
"pdf": [6,[7,8,11]],
"まったく": [[6,11]],
"させない": [2],
"自動更新": [5],
"もそのまま": [10],
"そこから": [10,5],
"より": [[2,5,8,11]],
"りが": [8],
"toolsshowstatisticsmatchesmenuitem": [3],
"viewdisplaymodificationinfononeradiobuttonmenuitem": [3],
"蘭中翻訳": [6],
"出力形式": [6],
"つだけに": [11],
"タイプ": [11],
"やすことができます": [11],
"トラブルシューティング": [0,7],
"登場": [11],
"りで": [11],
"著作権": [8],
"うものにしたいということは": [11],
"境界正規表現": [[2,7]],
"再入力": [11],
"ファイルフォルダー": [11,[4,6]],
"りの": [11],
"はじゅうぶんに": [10],
"するのはもちろんですが": [11],
"略称": [11],
"下線": [1,[4,9]],
"学習": [10],
"りに": [11],
"つけた": [5],
"注意": [11,6,5,[4,8,9,10]],
"後処理用外部": [11],
"projectaccesswriteableglossarymenuitem": [3],
"んでいるような": [11],
"があるからです": [11],
"gui": [5,10],
"定期的": [6,10],
"ると": [9],
"regexp": [5],
"によってはそれ": [10],
"しなおしたい": [9],
"sentencecasemenuitem": [3],
"重要": [[5,6,9,10]],
"れず": [3],
"権限": [5],
"語根": [11,8],
"りを": [[4,5,8]],
"通常": [5,11,8,[1,4,6,9,10]],
"最短一致数量子": [2,7],
"uhhhh": [2],
"こりません": [9],
"じになります": [11,6],
"選択領域": [[8,11],[3,9]],
"optionssentsegmenuitem": [3],
"認証": [11,3],
"となっています": [6],
"れは": [11],
"optionsaccessconfigdirmenuitem": [3],
"けるため": [6],
"わないような": [11],
"エントリ": [11,[1,8]],
"加速": [6],
"test.html": [5],
"ろに": [2,11],
"xxx": [10],
"れる": [11],
"ろで": [11],
"任意": [11,[8,9],[5,10],[1,2,3]],
"ろと": [11],
"smalltalk": [11],
"制約": [11],
"んでいること": [11],
"っていない": [8],
"連絡": [6],
"するだけです": [5],
"上記文書": [6],
"pseudotranslatetmx": [5],
"がすでに": [1],
"セクション": [[5,6]],
"名前変更": [6],
"つきの": [6],
"ツールメニュー": [3,7],
"targetlanguagecod": [11],
"構成": [6,[5,9,11]],
"数文字": [8],
"からでも": [10],
"わず": [9],
"訳文言語用": [4],
"直接開": [8],
"それをもう": [6],
"てられます": [[5,11]],
"技術文書": [3,[2,11]],
"チェックボックス": [11,4,[5,8]],
"こらないようにできるなら": [10],
"じということになります": [9],
"既存": [6,11,5,[1,9,10]],
"履歴補完": [8],
"構成要素": [11],
"はこのような": [6],
"フッター": [11],
"するかどうかの": [11],
"するかどうかは": [4],
"連続": [11],
"たとえばその": [1],
"メモリツール": [11],
"メイン": [6],
"適用": [11,8,10,[3,5,6]],
"ければ": [6],
"からなる": [1],
"してもかまいません": [5],
"それをまた": [6],
"けになります": [6],
"わる": [[5,8,11]],
"unpack": [5],
"われ": [[6,8]],
"しないでください": [11],
"識別": [11,10],
"原文分節中": [9],
"指向": [11],
"コマンドライン": [5,6,7],
"encyclopedia": [0],
"既定": [11,6,[5,10]],
"後者": [[4,6]],
"にまだ": [8],
"目次": [7],
"をお": [5,11],
"optionstagvalidationmenuitem": [3],
"させるために": [[3,6,11]],
"自動処理": [5,11],
"pt_br": [4,5],
"されるべきではありません": [11],
"a-z": [2],
"スタート": [5],
"ローカルプロジェクト": [6],
"このとき": [[6,11]],
"による": [[3,8,11],[5,6,9]],
"により": [[5,9,11]],
"単語境界": [2],
"置換": [11,8,3,9,7],
"のとおりです": [8],
"んだ": [11,[4,6]],
"最大": [5],
"javascript": [11],
"mediawiki": [11,[3,8]],
"input": [11],
"セミコロン": [6],
"んで": [11],
"基本訳文": [11,[3,8,9]],
"上記参照": [[5,6]],
"などはその": [6],
"未変更": [11],
"できると": [10],
"なくとも": [3],
"アイテム": [5,6],
"なります": [5,[8,9]],
"させることもできます": [9],
"限定的": [11],
"しておきます": [5,10],
"found": [5],
"するとすぐに": [8],
"破損": [[6,11]],
"デスクトップ": [5,11],
"げたような": [0],
"文書": [11,6,8,[0,3,5,7]],
"開発": [5],
"しているかどうかに": [11],
"させたい": [11],
"空白文字一": [11],
"ブックマーク": [11],
"プロジェクトファイル": [9],
"づけする": [11],
"アクセスキー": [11],
"りのすべての": [6],
"回以上": [2],
"googl": [5,11],
"opendocu": [11],
"認識": [1,11,6],
"ができます": [11,[2,6]],
"一致対象": [2],
"download.html": [5],
"ドロップダウンボタン": [11],
"つしか": [0],
"論理的": [11],
"調整": [11],
"非常": [[6,11]],
"みてください": [6],
"通貨記号": [2],
"sourceforg": [3,5],
"ヘッダー": [11,8],
"計算": [[9,11]],
"given": [7],
"しないようにするのに": [11],
"editmultipledefault": [3],
"mozilla": [5],
"editfindinprojectmenuitem": [3],
"ここに": [[5,6,8,11]],
"それをより": [5],
"warn": [5],
"project_save.tmx.yyyymmddhhnn.bak": [6],
"technetwork": [5],
"翻訳可能": [11],
"時点": [11,[5,6,10]],
"ここで": [11,9,[5,10]],
"するように": [[5,6],11],
"するような": [[5,6,11]],
"まかに": [11],
"plural": [11],
"語形変化": [9],
"左右": [6],
"設定時": [1],
"セキュリティ": [5],
"挙動": [5,10],
"せです": [6],
"みのため": [11],
"ファイル": [11,6,5,8,10,1,3,4,9,0,7],
"えたりできます": [11],
"windows": [7],
"むために": [8],
"説明文": [[3,11]],
"n.n_windows.ex": [5],
"サポート": [5,[2,8,11]],
"これらすべての": [11],
"ちですか": [5],
"一意": [11],
"program": [5],
"をそこに": [6],
"ユーザー・インターフェース": [1],
"っていたとしても": [11],
"置換後": [11],
"エンジン": [8,11,[2,7]],
"構成例": [11],
"しておきたいときに": [9],
"プライベート": [5],
"のあとに": [11],
"がどこまで": [9],
"することができます": [11,[5,6,9],4],
"n.n_mac.zip": [5],
"つのうちいずれかの": [5],
"ユーザーレベル": [11],
"国語": [11],
"大文字": [11,2,[3,8],5],
"まれると": [4],
"非単語境界": [2],
"リソースバンドル": [5],
"とすると": [5],
"外観": [11],
"普遍的": [9],
"のあるわかりやすい": [11],
"pseudotranslatetyp": [5],
"なすべてを": [5],
"くしておきます": [11],
"ローカルリポジトリー": [6],
"間隔": [11,[6,8]],
"除外登録": [11],
"プロンプト": [5],
"ましい": [10],
"づいた": [4],
"づいて": [[4,8]],
"ひとつは": [4],
"それらを": [6,5],
"アップロード": [8],
"ひとつの": [6],
"なすべての": [5],
"ログイン": [11],
"projectclosemenuitem": [3],
"viewmarknonuniquesegmentscheckboxmenuitem": [3],
"半角文字": [11],
"きません": [10],
"表示用": [11],
"すときに": [10],
"できるようになります": [[5,11]],
"範囲": [[2,11]],
"findinprojectreuselastwindow": [3],
"とされることもあります": [11],
"ランチャー": [5],
"じにしたいときは": [9],
"readme.txt": [6,11],
"それらの": [11,[2,6]],
"復帰": [8],
"languagetool": [11,8],
"分節中": [6,9],
"可能性": [5,11,[2,6]],
"source.txt": [11],
"files.s": [11],
"条件": [[2,5]],
"exchang": [1],
"同時": [11,[6,8]],
"request": [5],
"スクロールバー": [11],
"することができ": [9],
"わりません": [11],
"currseg": [11],
"point": [11],
"してみるのも": [11],
"じことを": [5],
"したときと": [11],
"げます": [5],
"訳文分節以外": [9],
"カーソル": [11,8,9,1],
"バッチファイル": [5],
"カスタムタグ": [[8,11]],
"にいかなる": [11],
"選択中": [[3,8]],
"つだけ": [8],
"downloaded_file.tar.gz": [5],
"うことができます": [[4,5]],
"たとえそれが": [11],
"原文言語": [6,11,9],
"はさらに": [11],
"管理下": [6],
"きつづけます": [10],
"一致": [11,1,[2,8],3,[9,10],4],
"account": [11],
"検索結果": [11],
"になるか": [5],
"dhttp.proxyhost": [5],
"ソース": [[6,11],5],
"のようになります": [5,11],
"しないか": [11],
"つあります": [[5,11]],
"のあいだで": [[6,11]],
"あらかじめ": [[5,6,11]],
"再現": [[6,10]],
"you": [11],
"書式設定": [6],
"制御": [[5,6,11]],
"改版": [10],
"画面出力": [5],
"一般": [11,9],
"次第": [1],
"個以上": [[2,11],3],
"ノート": [11],
"までのあいだ": [11],
"起動": [5,8,[7,11]],
"オープンソース": [[6,11]],
"unicode": [7],
"結果": [11,8,[2,5,9]],
"空白文字": [11,[2,3,8]],
"optionsworkflowmenuitem": [3],
"または": [11,6,8,5,[2,3],9,1,[0,4,10]],
"releas": [6,3],
"できます": [11,5,6,8,9,10,[0,3]],
"sparc": [5],
"しないと": [5],
"がそれです": [6],
"をどのように": [11],
"ソート": [9,8],
"代替": [8],
"日本語版": [11],
"あとで": [[6,11]],
"翻訳支援": [[6,7,10]],
"わせに": [5],
"されるか": [11],
"していません": [6],
"はさらにそれらを": [11],
"わせて": [[5,6,11]],
"セットアッププログラム": [5],
"うときは": [6],
"切断": [6],
"にそれだけを": [6],
"subdir": [6],
"単純化": [6],
"人気": [11],
"するようになります": [[1,5]],
"でなければなりません": [[5,6]],
"同期": [6,11,5],
"ピリオド": [[2,11]],
"うときも": [6],
"されるほとんどの": [3],
"ナビゲーションボタン": [5],
"において": [[5,8,11]],
"をそれぞれ": [4],
"ヨーロッパ": [11],
"することにより": [[8,11]],
"forward-backward": [11],
"休憩": [4],
"をどうするかについては": [10],
"したかもしれない": [6],
"単純": [2,11,[1,4]],
"アイコン": [5,[7,9]],
"file-source-encod": [11],
"わせたい": [11],
"反転": [11],
"some": [6],
"表示位置": [9],
"原文項目": [11],
"二重": [5],
"くこともできます": [5],
"しないといった": [8],
"ワードアート": [11],
"によっては": [11],
"わせを": [[0,3]],
"をかける": [11],
"ねられます": [5],
"されると": [6],
"わせる": [5],
"editexportselectionmenuitem": [3],
"直接実行": [5],
"下表左列": [9],
"home": [6,5],
"レイアウト": [11],
"projectaccesstargetmenuitem": [3],
"変換作業": [6],
"構成物": [3,8],
"えることがあるためです": [11],
"テキスト": [11,8,9,6,4,[2,7,10]],
"いておくと": [6],
"それまで": [9],
"最小": [11],
"について": [11,6,[5,8],3],
"もることができます": [11],
"インストール": [5,7,[0,8],[9,11],4],
"分節作成": [11],
"もしくはその": [0],
"構文": [2,11],
"aligndir": [5],
"翻訳作業": [6,11,10,9],
"system-host-nam": [11],
"典型": [6],
"action": [8],
"スクリプトファイル": [11],
"creat": [11],
"python": [11],
"リスト": [11,[1,6,8]],
"es_mx.dic": [4],
"事前": [5,[6,11]],
"infix": [6],
"まれます": [10,6,[5,8],11],
"られます": [[1,5,11]],
"されるゆになります": [11],
"しましょう": [6],
"しなおす": [6],
"プロパティ": [8],
"しながら": [[6,9]],
"文字数": [[2,8],9],
"意味": [11],
"差異": [11],
"アクセサリ": [5],
"file": [11,6,5],
"危険性": [6],
"わない": [11],
"一般設定": [11,[1,7]],
"までの": [[10,11]],
"までは": [[3,5]],
"ここまでくれば": [5],
"でなければ": [6],
"ハードディスク": [[5,6,8]],
"における": [6,8,[9,11]],
"こまめに": [6],
"わずすべて": [10],
"いやすい": [6],
"再適用": [6],
"リソース": [6,[5,11]],
"はそれを": [5],
"menu": [9],
"オフ": [11],
"がある": [8,11,5,[2,6],10,4],
"今度作成": [6],
"ステミング": [11,1,[3,9]],
"ライセンス": [[0,5,6,8]],
"のうち": [[1,9]],
"ローカル": [6,[5,8]],
"a-za-z": [2,11],
"していますが": [11],
"光学文字認識": [6],
"一番簡単": [5],
"賢明": [[4,6]],
"していますか": [0],
"平文": [1],
"使用": [11,6,5,8,9,[1,7],[4,10],3,[0,2]],
"ともっとも": [6],
"せます": [11],
"オン": [8,11,5],
"をしています": [1],
"source-pattern": [5],
"ポルトガル": [5],
"メキシコスペイン": [4],
"わった": [11],
"しておくと": [[5,11]],
"れています": [0],
"旧形式": [11],
"っていると": [[8,11]],
"仕様": [6],
"開始": [[6,11],[2,5]],
"のいずれでも": [10],
"警告": [11,5,[2,6]],
"にすると": [11,8,[5,6,10]],
"っているか": [4],
"true": [5],
"手修正": [11],
"groovi": [11],
"リテラル": [11],
"のある": [5,[6,11]],
"されており": [8,11],
"ショートカットキー": [11],
"編集": [11,8,9,[5,7],[1,3,6],10],
"ファイル・": [11],
"kmenueditor": [5],
"エラーメッセージ": [5,6],
"インポート": [6,[1,8,9,10]],
"基準": [[6,8,11]],
"訳文生成": [6],
"しやすくなります": [11],
"編集中": [11],
"ピンポイント": [11],
"キー": [11,3,5,[1,9]],
"によってさらに": [11],
"えるという": [6],
"参照": [11,6,5,[2,9,10],[3,4],[0,8]],
"master": [6],
"kmenuedit": [5],
"っていれば": [11],
"だったら": [0],
"パイプ": [11],
"xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx": [5],
"writer": [6],
"dalloway": [11],
"rubi": [11],
"されます": [11,8,6,9,10,5,1,4,3],
"無制限": [5],
"yyyi": [6],
"挿入": [8,11,3,9,[1,10]],
"および": [11,6,5,[1,8],[0,7,9]],
"分節化規則": [11,2,8,[3,6,10]],
"テーマ": [11],
"スムーズ": [6],
"追加設定": [5],
"ひとつだけです": [6],
"user.languag": [5],
"regex": [2,[7,11]],
"meta": [[3,11]],
"keystrok": [3],
"だけです": [8,6],
"をいくつでも": [10],
"実行時": [5],
"配布": [5,6],
"確認用辞書": [4,7,11],
"付記": [9],
"きされますが": [5],
"したりできます": [[3,11]],
"対話式": [2],
"インストールフォルダ": [5],
"翻訳中": [8,[9,10]],
"てられている": [3],
"のついた": [11],
"わったら": [[8,11]],
"原文部分": [1],
"ibm": [5],
"サーバ": [6,11],
"アクセス・キー": [11],
"翻訳済": [11,9,8,3,10],
"分節化規則集": [11],
"parsewis": [11],
"なるものです": [9],
"このあと": [6],
"してもよいでしょう": [[6,11]],
"理解": [6],
"されている": [8,11,5,4,1],
"にどのていど": [11],
"分節化": [11,6],
"まない": [11],
"のままであれば": [10],
"ではなく": [[4,5,8,9]],
"つかれば": [11],
"されていて": [6],
"翻訳単位": [[10,11]],
"するよくある": [1,7],
"idx": [0],
"ではない": [[1,6,8,11]],
"ペナルティ": [10],
"個数": [11],
"大丈夫": [1],
"projectaccesscurrentsourcedocumentmenuitem": [3],
"前後": [11],
"linux": [5,[2,7]],
"留意": [6],
"をいくつか": [[5,11]],
"されていく": [9],
"できない": [5],
"クレジット": [8],
"file.txt": [6],
"設定変更": [11],
"ドメイン": [11],
"ためです": [11],
"ifo": [0],
"応答": [9],
"まれないことに": [6],
"comment": [5],
"うかどうか": [5],
"がかかります": [4],
"にのっとった": [11],
"双方向": [8,3],
"初期位置": [11,3],
"ローカルコピー": [8],
"関数型": [11],
"するものです": [[5,11]],
"基本的": [5,4],
"まれていますが": [6],
"xx.docx": [11],
"分節内": [[9,10]],
"らかなように": [9],
"合計": [11],
"翻訳対象外": [11],
"履歴予測": [8],
"テキストレイヤー": [6],
"参加": [6],
"ている": [6],
"optionsautocompleteautotextmenuitem": [3],
"書式": [11,6],
"設定": [11,8,[5,6],4,3,[7,9,10],0,2],
"導入": [5],
"候補単語": [8],
"されることになります": [11],
"指示": [5,[4,6]],
"zip": [5],
"リモートファイル": [[6,10]],
"製品名": [6],
"そこで": [5],
"原因": [[1,5]],
"動作": [5,[4,6,11]],
"そこに": [11,[9,10]],
"スクロール": [11,9],
"保護": [11],
"concis": [0],
"あまり": [5],
"のままです": [10],
"がいくつか": [11],
"支障": [11],
"term.tilde.com": [11],
"同一訳文": [[3,8]],
"定義": [11,3,8,[2,10]],
"同様": [[6,11],8],
"されていないことを": [11],
"前者": [6],
"直近": [[6,11]],
"スクリプトフォルダー": [8],
"viewmarknotedsegmentscheckboxmenuitem": [3],
"してみましょう": [[0,6]],
"外部出力": [6],
"実用的": [5],
"ほとんどの": [11],
"積極的": [8],
"ドラッグ": [9,5,7],
"改善": [11,8],
"gotomatchsourceseg": [3],
"プロジェクトリーダー": [6],
"複数見": [11],
"プロジェクトフォルダー": [6,[10,11],[0,7,8,9]],
"optionssaveoptionsmenuitem": [3],
"excel": [11],
"むことができます": [6,5],
"いいえ": [5],
"runn": [11],
"ターミナルウィンドウ": [5],
"がこの": [[6,8]],
"stardict": [0],
"omegat.l4j.ini": [5],
"いているときに": [11],
"span": [11],
"階層": [5],
"設定欄": [11],
"表示設定": [6],
"検索": [11,8,2,3,[5,6,7]],
"つまり": [[6,11],[9,10]],
"まることに": [5],
"space": [11],
"のあいだ": [11],
"ペイン": [[1,9,11]],
"ドイツ": [11],
"箇所": [11,2,6],
"セル": [11],
"アーカイブ": [[0,6]],
"もあります": [11],
"時間": [4],
"変換単位": [10],
"thunderbird": [4,11],
"したもの": [6],
"editselectfuzzy3menuitem": [3],
"マッピング": [6,11],
"後方": [11],
"緑字": [9],
"したいとき": [6],
"fals": [[5,11]],
"project.projectfil": [11],
"複数指定": [11],
"一方": [11,0],
"それでも": [6],
"プロジェクトパッケージ": [8],
"はもちろん": [[4,9,11]],
"検索文字列": [11,8],
"があるでしょう": [11],
"コンテキストメニュー": [[1,11]],
"矢印": [9],
"元分節": [[3,8]],
"再起動": [3,11],
"意図": [11],
"それには": [[5,11]],
"実質的": [11],
"配置": [10,9,[1,5],[4,6]],
"shortcut": [3],
"つけられなくなるかもしれません": [6],
"タグ": [11,6,8,[3,5],9],
"pt_br.aff": [4],
"tmx2sourc": [6],
"されるようにします": [11],
"にしている": [11],
"lookup": [8],
"オプション": [11,8,5,[1,6,9,10]],
"ini": [5],
"タブ": [1,11,[2,8,9]],
"できないことがあります": [5],
"dhttp.proxyport": [5],
"非表示": [11],
"があるかどうかが": [5],
"判断": [11],
"subrip": [5],
"招待": [6],
"のそれ": [11],
"設定例": [11],
"共存": [5],
"score": [11],
"検索履歴": [11],
"にあるその": [9],
"によって": [8,6,[5,9],[3,10,11]],
"文法": [11],
"なおかつ": [11],
"raw": [6],
"現在表示": [8],
"えられる": [11],
"選択的": [6],
"移動": [11,8,9,[3,7],5],
"みたい": [4],
"コマンドプロンプト": [5],
"てきます": [6],
"文脈情報": [11],
"aaa": [2],
"contemporari": [0],
"solari": [5],
"がその": [5,11],
"のため": [[4,11]],
"バージョンアップ": [11],
"これは": [11,[0,6,8,9],[4,5]],
"特定": [11,6,5],
"られるものを": [11],
"メモリ": [6,11,10,5,9,8,[2,7]],
"デフォルトエンコーディング": [1],
"由来": [[8,11],3],
"論理演算子": [[2,7]],
"リモートサーバー": [10],
"abc": [2],
"これが": [5],
"翻訳途中": [11],
"プレーンテキスト": [6],
"拡張": [[1,7]],
"リモート": [8],
"最後": [8,11,5,[3,9,10]],
"上級者": [[2,5]],
"いろいろあるやり": [6],
"iso": [1],
"台湾": [5],
"プログレッシブ": [11],
"表現": [[9,11]],
"するでしょう": [3],
"カレントフォルダー": [5],
"ファーストクラス": [11],
"わかりません": [5],
"するらしく": [5],
"glossary.txt": [6,1],
"無料": [5],
"ヘルプ": [8,[6,7]],
"ａｂｃ": [11],
"があるかもしれません": [6,11],
"add": [4],
"再定義": [11],
"じることを": [11],
"初期": [11],
"わせることができます": [11],
"チーム・プロジェクト": [[6,11]],
"そして": [[3,5,10]],
"ブラジルポルトガル": [4],
"ですので": [11],
"引用": [2,7],
"数値": [11,10,[6,9]],
"コレクション": [11],
"optionsautocompleteshowautomaticallyitem": [3],
"入力補完": [11,3,8],
"できなかった": [[4,11]],
"larouss": [9],
"untar": [[0,5]],
"冒頭": [5],
"filters.conf": [5],
"じですが": [[6,11]],
"補助的": [10],
"ごとの": [8],
"依頼主": [10],
"変換精度": [11],
"ローカルフォルダ": [[6,11]],
"パターン": [6,11],
"キーイベント": [3],
"ごとに": [11,8,6],
"分節番号": [9,8,3],
"大文字以外": [2],
"名称部分": [9],
"プロジェクトフォルダ": [1],
"ユーティリティ": [0],
"clone": [6],
"わせである": [5],
"えられた": [11],
"targetlanguag": [11],
"単語構成文字": [2],
"多種多様": [0],
"生産性": [11],
"をとってください": [5],
"properti": [[5,11]],
"想定": [5],
"editselectfuzzyprevmenuitem": [3],
"されていきます": [10],
"しなければなりません": [6],
"するすべての": [11],
"simpledateformat": [11],
"朝鮮語": [11],
"訂正候補": [4],
"自動挿入": [11,10,[6,8]],
"になったときに": [6],
"とみなさない": [11],
"れないでください": [11],
"解決策": [6],
"みです": [5],
"収録": [9],
"script": [11],
"オランダ": [6],
"system": [11],
"としておいてください": [11],
"spellcheck": [4],
"シフト": [11],
"修正": [11,3,[6,8],[5,9]],
"other": [11],
"そうではない": [10],
"わりに": [3,[6,8,11]],
"local": [6,5],
"プレゼンテーション": [11],
"つことがあります": [11],
"configuraiton.properti": [5],
"専用": [11,6,10],
"repo_for_all_omegat_team_project_sourc": [6],
"進捗": [9,7],
"わらず": [11],
"句点": [11],
"指定": [11,5,[3,6],[2,4,8],[0,10]],
"更新可能": [3],
"スイッチカラーテーマ": [11],
"es_mx.aff": [4],
"文中": [11],
"終了": [[6,8,11],3],
"mode": [5],
"るかどうかを": [5],
"java8jr": [5],
"toolsshowstatisticsstandardmenuitem": [3],
"all": [4],
"これを": [11,6,5],
"read": [11],
"alt": [[3,5,11]],
"不明": [[5,6]],
"すべきでない": [11],
"これら": [6],
"デフォルト": [11,3,6,[1,8],5,[2,7,9,10]],
"alreadi": [11],
"ローカルマッピング": [6],
"ファイルマネージャー": [4],
"てくる": [6],
"されません": [11,10,8,[1,6]],
"いがある": [6],
"ファイルフィルタ": [[5,6,8,10]],
"左端": [11],
"変換工程": [6],
"しているうちに": [9],
"ができるはずです": [0],
"そのため": [11,5,[4,8]],
"段落区切": [8],
"スペルチェック": [8],
"原文分節": [11,[8,10],9,6],
"変換時": [11],
"位置": [11,8,[5,9],[1,6]],
"例外的": [11],
"全選択": [9],
"文字一覧": [11,3],
"新規": [5,6,11],
"強制": [10],
"and": [[5,11]],
"直前": [8,5],
"環境": [5],
"たいていは": [11],
"あいまい": [11],
"たいていの": [5],
"からすべての": [11],
"にちゃんと": [11],
"手動": [11,[4,6,8],1],
"ant": [[6,11]],
"バグ": [8],
"にあります": [11,5,6],
"何度": [[6,11]],
"helplastchangesmenuitem": [3],
"omegat.ex": [5],
"ています": [11],
"規則群": [11],
"けたほうが": [6],
"ログファイル": [8],
"sourcetext": [11],
"にあると": [11],
"自動挿入以外": [11],
"パス": [5,6],
"てたい": [[3,5]],
"english": [0],
"jar": [5,6],
"api": [5,11],
"解析": [11],
"editselectfuzzy2menuitem": [3],
"させる": [[6,10,11]],
"からも": [4],
"プロセス": [6,11],
"するのは": [6],
"上書": [5,[6,8,9,10,11]],
"バー": [5],
"からは": [11],
"からの": [5,[1,6,11],[7,9]],
"確実": [5],
"したがって": [11,[5,6]],
"確定": [8,9],
"セキュリティー": [11],
"慎重": [11],
"であったりする": [6],
"によるものであることを": [11],
"するのに": [11],
"ることができます": [9,8],
"注記": [2,9],
"するので": [5],
"未翻訳分節": [11,8,3,[6,10]],
"クイック": [5],
"のどれかの": [11],
"便利": [5,[2,11],[6,9]],
"翻訳前": [11],
"一行": [[3,10]],
"editselectfuzzynextmenuitem": [3],
"修正済": [10],
"れずにおきます": [11],
"目印": [11],
"強力": [11],
"けられるべきです": [11],
"read.m": [11],
"ステータスバー": [9,[5,7]],
"ドット": [5],
"多言語法律文書": [6],
"文字列検索": [[2,11]],
"readme.bak": [6],
"art": [4],
"かれた": [9,[4,6,10]],
"多言語": [6],
"ドック": [5],
"しようとしている": [9],
"rtl": [6],
"jdk": [5],
"関連付": [8,6],
"用語": [1,9,3,11,8],
"機械翻訳": [8,9,11,[3,7]],
"末尾": [11,8,[2,6,10]],
"するには": [11,5,[1,6],8,9],
"がついた": [6],
"toolsshowstatisticsmatchesperfilemenuitem": [3],
"リリース": [8],
"えかねないので": [6],
"規則": [11,5],
"設定済": [11],
"run": [11,[5,6]],
"パッケージ": [5,8],
"分節化設定": [11],
"われません": [[1,4]],
"記憶": [11,8],
"技術的": [11,8],
"左寄": [6],
"外部": [11,6,[1,3,5]],
"オフライン": [6,5],
"titlecasemenuitem": [3],
"ルートフォルダー": [6],
"みには": [4],
"editcreateglossaryentrymenuitem": [3],
"クラウド": [6],
"むすべての": [11],
"自動検索": [11],
"されたときも": [6],
"参照用": [1],
"わからない": [5],
"だったとしましょう": [11],
"アクセス": [11,3,[5,8],6],
"れてから": [11],
"色付": [8,3,11],
"name": [5],
"まれる": [11,[5,6]],
"制御文字": [8,[2,3]],
"設定中": [4],
"いずれの": [11],
"非単語文字": [2],
"配色": [[3,11]],
"ウィンドウタイトル": [11],
"するのか": [5],
"がない": [8,[1,5,9,10]],
"内容": [11,6,8,10,9,5],
"例外": [11],
"アドオン": [2],
"右側": [11],
"まれた": [4],
"target": [8,[10,11],7],
"パネル": [5],
"われる": [8],
"堅牢": [6],
"データ": [[6,11],7],
"無関係": [11],
"維持": [11],
"config-dir": [5],
"例示": [[5,9]],
"絵文字": [8],
"termbas": [1],
"ペア": [6,11,9],
"わっていれば": [5],
"普及": [11],
"をするための": [11],
"jis": [11],
"分節間": [11],
"いことが": [10],
"変化": [11],
"公式": [7],
"されないようにする": [10],
"targettext": [11],
"ベル": [2],
"ポップアップ": [11],
"するよりも": [6],
"対訳形式": [0],
"にあらかじめ": [9],
"aaabbb": [2],
"きさを": [9],
"edittagpaintermenuitem": [3],
"簡単": [6,11,[4,10]],
"単一言語": [11],
"optionscolorsselectionmenuitem": [3],
"があるとします": [6],
"最良": [11],
"にあるすべての": [6],
"起動方法": [5],
"メモリファイル": [6,11],
"整形情報": [6,[10,11]],
"バイリンガル": [6],
"unicod": [2],
"viewmarknbspcheckboxmenuitem": [3],
"らかの": [5,11],
"同上": [6],
"らかな": [10],
"具体例": [11],
"よくあります": [11],
"補助用": [6],
"同一": [6,[10,11],[4,5]],
"いません": [[5,8,11]],
"できるため": [6],
"するときは": [6],
"せをどうするかは": [6],
"するときと": [6],
"作業中": [[8,10]],
"である": [[1,8,11],[0,4,6]],
"翻訳作業向": [9],
"するときに": [[1,5,6,11]],
"はそのままです": [11],
"であり": [5],
"msgstr": [11],
"小文字": [11,3,2,[5,8]],
"履歴": [8],
"があることが": [6],
"えるほうがよいでしょう": [11],
"みされます": [11],
"一覧": [11,[7,8],3,[2,4,5,9]],
"があればその": [9],
"両方": [6,11,5],
"いたい": [[4,11],6],
"omegat.project": [6,5,10,[7,9,11]],
"excludedfold": [6],
"ではありません": [6,5],
"targetcountrycod": [11],
"プロパティダイアログ": [1],
"があったとします": [10],
"未使用": [8,3],
"webstart": [5],
"入力済": [11],
"かります": [9],
"以上": [[1,10]],
"優先順位": [11],
"以下": [11,5,6,2,[0,4,9,10]],
"起動時": [5],
"はすぐに": [1],
"ハイライト": [8,9],
"ソース・ファイル": [6],
"候補": [[8,10]],
"整形用": [11],
"真似": [11],
"いたり": [6],
"yandex": [5],
"申請": [5],
"メタ": [2],
"a123456789b123456789c123456789d12345678": [5],
"こうとすると": [5],
"サブフォルダー": [0],
"viewmarkwhitespacecheckboxmenuitem": [3],
"のみの": [5],
"プライマリ": [5],
"すことをおすすめします": [6],
"ソースコード": [5,7],
"のみで": [[5,11]],
"bak": [6,10],
"以上規則": [11],
"されていることから": [11],
"bat": [5],
"からやり": [11],
"のまま": [11],
"誤検出": [11],
"jre": [5],
"のみが": [11,6,[1,8]],
"optionsfontselectionmenuitem": [3],
"メモ": [[8,9],3,11,7],
"されるようにます": [11],
"プログラム": [[5,6],11,4,8],
"めたいという": [6],
"原文挿入時": [11],
"キーボード": [9,[3,11]],
"マッピングパラメータ": [6],
"依頼": [6],
"するその": [[6,9]],
"ノーブレークスペース": [8,[3,11]],
"リンク": [[5,10,11]],
"黄色": [[8,9]],
"できる": [11,6,5,[3,7,10]],
"freebsd": [2],
"フォルダ": [5,6,[1,8,10],3,9],
"のもう": [11],
"delet": [11],
"projectaccessglossarymenuitem": [3],
"であることに": [5],
"であるのに": [4],
"java8": [5],
"がまだ": [11],
"特徴": [11],
"のみを": [11,5,8],
"ユーザーインターフェース": [5,11],
"れることで": [11],
"格納": [[4,9,11]],
"アカウント": [5],
"アクティブ": [8],
"developerwork": [5],
"するため": [6,8],
"set": [5],
"きたい": [9],
"かどうか": [11],
"optionsrestoreguimenuitem": [3],
"けられている": [8],
"すもので": [9],
"スコア": [11],
"バックアップ": [[6,10]],
"構文例": [2],
"メインウィンドウ": [9,7],
"つもない": [1],
"クリップボード": [8],
"terminolog": [8],
"offic": [11],
"をなるべく": [6],
"各々": [5],
"javas": [11],
"さない": [11],
"repositories": [7],
"projectsavemenuitem": [3],
"xmx6g": [5],
"無視": [11,9,[3,4,5,8,10]],
"いても": [5],
"複数起動": [5],
"手間": [6,11],
"既定値": [8,9],
"みしていない": [1],
"収集": [6],
"水色": [8],
"であることを": [11],
"しておくことができます": [11],
"特性": [11],
"現在作業中": [3,11],
"はその": [6,[1,11]],
"のみです": [11],
"スペース": [11,[2,8],[1,6]],
"終了時": [[3,8,10,11]],
"きする": [6],
"みます": [11,5],
"してはいませんが": [6],
"ソーステキスト": [11],
"日時": [11],
"させるか": [10],
"現在編集中": [9],
"ですが": [9],
"するとか": [4],
"場所設定": [11],
"前方": [11],
"カーソルロック": [9],
"ユーザー・アカウント": [11],
"bis": [2],
"欠落": [[2,8]],
"えておくべきでしょう": [6],
"projectopenmenuitem": [3],
"autom": [5],
"定型文": [11,3],
"があるのは": [6],
"語句": [11],
"整合": [11,8,5,7],
"空行": [11,3],
"不要": [11],
"toolsvalidatetagsmenuitem": [3],
"きされます": [11],
"クリックメニュー": [11],
"更新": [11,8,1],
"かなければなりません": [11],
"詳細情報": [[5,11]],
"斜体": [11],
"けるようにしたければ": [3],
"すほうが": [4],
"viewmarktranslatedsegmentscheckboxmenuitem": [3],
"valu": [11,5],
"フォント": [8,1],
"上記": [5,6,[9,10],[0,8,11]],
"いがあれば": [11],
"ilia": [5],
"機密": [11],
"オブジェクト": [11],
"プライバシー": [5],
"目的": [11,6,[5,8]],
"複数": [11,5,[6,8],[1,10]],
"macos": [7],
"クラス": [2,7],
"下記": [11,[5,6,9]],
"ログ": [[3,8]],
"editselectfuzzy1menuitem": [3],
"ベース": [11],
"れている": [[3,11]],
"サイレントオプション": [5],
"hide": [11],
"観点": [9],
"挿入先": [11],
"下訳": [[6,11]],
"auto": [10,8,11,6,3],
"らして": [11],
"document.xx.docx": [11],
"入手": [3,[4,5,6]],
"フォルダー": [11,5,10,6,8,4,0,[1,3,9]],
"プレースホルダー": [[6,11]],
"oracl": [5,3,11],
"商標": [11,9],
"をかけることができます": [11],
"gradlew": [5],
"のもの": [6],
"これによって": [9],
"比較": [11],
"操作方法": [6,[0,7,10]],
"相関": [11],
"新機能": [8],
"文書情報": [6],
"本当": [8],
"改節": [11],
"リモートリポジトリ": [6],
"でしか": [11],
"できるのは": [0],
"表示": [11,8,9,3,[5,6],1,10,[4,7],2],
"れてしまった": [11],
"をかけ": [11],
"大切": [6],
"しないかのいずれかです": [11],
"プロキシ": [11,[3,5]],
"衝突": [[3,8,11]],
"のあらゆる": [11],
"bundl": [11],
"ショートカット": [3,5,[8,11],7,[2,9]],
"あまりないでしょう": [11],
"優先的": [10],
"干渉": [11],
"src": [6],
"検索方法": [11],
"ラジオボタン": [11],
"control": [3],
"日本": [5],
"no-team": [[5,6]],
"参照翻訳": [6],
"実施": [[5,6,11]],
"ホスト": [11,5],
"しなかった": [11],
"続行": [11],
"によってはある": [11],
"部分的": [8,[9,11]],
"部分": [9,11,[1,6,8]],
"optionsautocompleteglossarymenuitem": [3],
"ローカルファイル": [6],
"それを": [[5,6,11]],
"更新情報": [3,[8,11]],
"コメントウィンドウ": [9],
"すことになるかもしれません": [11],
"実行中": [[5,8,9]],
"いほど": [11],
"kde": [5],
"するかどうか": [11],
"方法": [11,5,6,4,0,[7,10]],
"をこの": [9,5],
"順番": [11,[8,10]],
"てます": [5],
"それは": [11],
"pattern.html": [11],
"作成": [6,[5,11],8,10,[1,4],2],
"同僚": [9],
"動的": [11],
"メッセージ": [5,[8,9]],
"環境設定": [8,11,[3,5,6]],
"とほとんど": [2],
"にわずかに": [9],
"という": [5,11,10,[1,3,6,8]],
"ドキュメント": [8,11],
"視覚的": [8],
"ターゲットファイル": [8],
"編集画面上": [11],
"されているかを": [6],
"語用": [11],
"key": [[5,11]],
"いったん": [[5,11]],
"います": [11,9,[0,2,3,4,5,8]],
"svg": [5],
"はいくつも": [11],
"下半分": [11],
"最小化": [9],
"svn": [6,10],
"決定": [[10,11]],
"提案": [11],
"させます": [11],
"厳密": [6],
"構成例編集": [11],
"日常的": [5],
"editreplaceinprojectmenuitem": [3],
"するたびに": [[6,11]],
"最長一致数量子": [2,7],
"でこれらの": [5],
"フォーカス": [11],
"全角文字": [11],
"express": [2],
"省略": [5],
"明示的": [11],
"されることがあります": [11],
"すべてを": [11],
"gotoprevioussegmentmenuitem": [3],
"じように": [11,9,[2,8]],
"gotopreviousnotemenuitem": [3],
"リモート・ロケーション": [6],
"editredomenuitem": [3],
"uilayout.xml": [10],
"再度": [[8,11]],
"すべてに": [10],
"ターゲット": [1],
"できるかどうか": [11],
"くことができます": [11,5,8],
"最新": [6,5],
"いてください": [10],
"すべての": [11,5,6,8,[3,9]],
"付加": [11],
"がしっくりこない": [9],
"図面": [6],
"それに": [11,[6,9]],
"マルチパラダイム": [11],
"緑色": [8],
"するかを": [5],
"っている": [11,[5,8,10]],
"ソースファイル": [6,8],
"にはそこを": [11],
"字体": [11,8,3],
"見直": [10],
"物理的": [4],
"高度": [11],
"直接取得": [5],
"最近使用": [[3,8]],
"上段": [11],
"でその": [6,11],
"けるようになります": [5],
"解決": [6,1],
"それが": [11,10],
"処理": [11,5,6,9,3],
"段落単位": [11],
"してかまいません": [6],
"ダイアログ": [11,8,[4,10],[6,7,9]],
"けします": [8],
"があったとしても": [5],
"のような": [11,5,6,[9,10]],
"のように": [5,11,[3,6,9]],
"っていて": [5],
"不正": [5,6],
"シート": [11],
"をまったく": [11],
"下段": [[2,11]],
"そこからの": [10],
"すために": [6],
"tester": [2,7],
"大半": [11],
"のいずれかです": [6],
"またはその": [6],
"モード": [6,[5,11],9],
"対策": [6],
"をするのであれば": [4],
"手順": [11,6,5,[0,4,7,8]],
"filenam": [11],
"ルールベース": [11],
"クライアント": [6,[5,10,11]],
"はありません": [5,[1,4,6]],
"にしたがって": [[2,5,6]],
"nbsp": [11],
"一致率": [[9,10],8,[3,11],6],
"gotosegmentmenuitem": [3],
"文末脚注": [11],
"があります": [[5,6],11,4,[1,10],0,[3,8,9]],
"背景": [8],
"わせてください": [6],
"めする": [6],
"外部検索": [11,8],
"どこであってもかまいません": [5],
"定義構文": [3],
"xx_yy.tmx": [6],
"してしまう": [6],
"サブスクライブ": [5],
"helpaboutmenuitem": [3],
"ダーク": [11],
"対応済": [6],
"されないようにすることができます": [11],
"付録": [[1,2,4],[0,3],6],
"依存": [1],
"優先順": [11],
"構造単位": [11],
"regular": [2],
"テキストエディター": [5],
"こりうる": [6],
"コンソール": [5],
"フォーマット": [[1,6],7],
"でなく": [2],
"訳語項目": [[8,11]],
"でない": [[6,11]],
"よりも": [11,[5,8,10]],
"えることが": [6],
"をその": [11,8],
"けるには": [1],
"されるはずです": [3],
"tab": [3,[1,8,11],9],
"taa": [11,8],
"コンテンツ": [5],
"項目": [3,[8,11],1],
"tag": [11],
"コメント": [11,9,1,[3,5,7,8]],
"tar": [[0,5]],
"われている": [[9,11]],
"しいか": [11],
"ポップアップメニューアイコン": [8],
"onli": [11],
"projectreloadmenuitem": [3],
"独立型": [5],
"ブラジル": [5],
"構造": [10,11],
"safe": [11],
"パスワード": [11,6],
"なはずです": [11],
"通訳者側": [6],
"印刷物": [9],
"半角": [11],
"空欄": [[9,11]],
"winrar": [0],
"tbx": [1,11,3],
"いもの": [9],
"でしょう": [10],
"があるため": [11],
"することになります": [[6,11]],
"ウィキペディア": [8],
"duser.countri": [5],
"tcl": [11],
"tck": [11],
"らない": [11],
"readm": [[5,11]],
"用途": [9],
"ばして": [8],
"名前": [6,5,[4,11],[1,10],[0,3,9]],
"出力": [11,6,[5,8],3],
"がれます": [[5,11]],
"えてしまった": [6],
"align.tmx": [5],
"がすべて": [[9,10]],
"file2": [6],
"ってから": [4]
};
