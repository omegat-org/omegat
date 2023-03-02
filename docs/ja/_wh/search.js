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
 "付録",
 "環境設定",
 "操作方法...",
 "Introduction to OmegaT",
 "メニュー",
 "ウィンドウ",
 "プロジェクトフォルダー",
 "ウィンドウとダイアログ",
 "OmegaT 5.8.0-取扱説明書"
];
wh.search_wordMap= {
"させることができます": [[2,4,7]],
"するための": [0,[2,4],[1,5]],
"以前": [[0,3],[2,5],[6,7]],
"スタンドアロンタグ": [1],
"数行離": [7],
"するために": [2,[1,3,7],0,6,4],
"するのでしょうか": [3],
"灰色": [4],
"先行": [0],
"することはできますが": [0,7],
"送信": [2,1],
"ではを": [7],
"きます": [4,2,7,[0,1],5,3,6],
"うこともできます": [[2,6]],
"アプローチ": [2],
"ローカルルール": [0],
"いつでもこの": [3],
"共有用語集": [2],
"info.plist": [2],
"うことで": [3],
"原文中": [7],
"むような": [0],
"むように": [[2,7]],
"しくに": [4],
"改行文字": [0],
"ただし": [0,7,[3,4]],
"fuzzi": [1],
"される": [0,7,1,6,2,3,[4,5],8],
"もできます": [7],
"添付": [[0,7]],
"size": [2],
"left": [0,5],
"ワークフロー": [3,[0,5,8]],
"ではに": [2],
"ではの": [1],
"にとって": [0],
"メジャー": [0],
"ファイルマネージャ": [[2,6]],
"がかりは": [0],
"バンドル": [2,1],
"ジャンプ": [7,[0,3,4,5]],
"メールアドレス": [0],
"result": [7],
"練習": [[0,2]],
"edittagnextmissedmenuitem": [0],
"があいまいな": [0],
"利用可能": [1,2,[0,3,5,7]],
"動的言語": [7],
"after": [0],
"quiet": [2],
"オブジェクトモデル": [7],
"されるまでに": [1],
"基礎": [[0,2,7]],
"言語属性": [1],
"このしきい": [1],
"各部分": [1],
"the": [0,2,7,6],
"フリーソフトウェア": [2],
"修正作業": [3],
"きします": [4,[0,6]],
"projectimportmenuitem": [0],
"選択範囲": [[0,4,5,7]],
"わります": [[4,6]],
"imag": [0],
"サービスリスト": [1],
"すべてがしきい": [1],
"作業用": [6],
"実行": [[2,7],4,[0,1],[3,6,8]],
"途中": [7,2],
"トークナイザー": [[0,2,7],5],
"あります": [0],
"言語": [2,7,1,0,3,6,[4,5]],
"omegat.project.lock": [2],
"しきい": [1],
"名構成例": [0],
"moodlephp": [2],
"currsegment.getsrctext": [7],
"箇条書": [7],
"practic": [7],
"ユーザー": [0,2,7,[4,5]],
"さらに": [7,3,[0,5,6]],
"にしたい": [[2,5]],
"をすぐに": [[4,7]],
"ターミナル": [2],
"フィール": [1],
"直接起動": [2],
"結合": [7,3,[0,1,2,6]],
"めたい": [2],
"変更例": [0],
"gotonotespanelmenuitem": [0],
"fr-fr": [3,1],
"きをしています": [7],
"でのみ": [1,7],
"サービスプロバイダ": [5],
"ではで": [2,1],
"のものか": [0],
"横断": [0],
"訳文候補": [[2,4,5,6]],
"機能名": [0],
"用語集入力": [3],
"された": [2,0,7,1,4,6,3,5],
"ソフトリターン": [0],
"されて": [2,0],
"全分節": [[1,7]],
"使用時": [2],
"をするには": [4],
"スライドレイアウト": [0],
"cjk": [7,0],
"複数形指定": [0],
"することがあります": [0,2],
"はまた": [[2,6]],
"変更調整": [3],
"文字列": [0,7,1,4,3,[2,5]],
"スクリプトウィンドウ": [[4,7]],
"現在書": [4],
"better": [7],
"windows10": [0],
"フィールド": [4,[0,7]],
"いより": [1],
"empti": [[2,4]],
"とする": [[1,7],[0,3]],
"たない": [0],
"はより": [7],
"詳細": [2,4,7,0,1,3,6,5],
"実装": [1],
"自動入力": [4],
"するものが": [7,2],
"全文": [5],
"スライド": [0],
"スコープ": [0],
"tmx": [2,7,6,1,[3,5]],
"様々": [[3,7],[0,1]],
"repo_for_all_omegat_team_project": [2],
"cli": [2],
"供給": [2],
"コンピューター": [[2,8]],
"application_startup": [7],
"無条件": [6],
"eventtyp": [7],
"分割": [7,0,1,3,[4,5]],
"ルール": [0,7,1,[2,4]],
"テキストエディタ": [0,[2,6]],
"ショートカットベース": [3,8],
"するものは": [0],
"ページ": [0,4,[2,3,7]],
"検索機能": [[0,2]],
"fr-ca": [1],
"文字全": [0],
"mainmenushortcuts.properti": [0],
"するものと": [7],
"識別子": [0,4,8],
"選択": [4,7,0,1,5,2,3,6],
"いっぱいに": [3],
"がでたほうを": [7],
"かれている": [5],
"ループ": [7],
"すこともできます": [7,6],
"かどうかで": [0],
"しておくこともできます": [2],
"subtitl": [2],
"ロック": [5,2,3],
"gotohistorybackmenuitem": [0],
"上下": [[1,3,7]],
"save": [7],
"v1.0": [2],
"わせについては": [0],
"ボタン": [7,3,[0,1]],
"するものを": [3],
"クエスチョンマーク": [0],
"個々": [0,[2,7]],
"top": [5],
"ウィンドウタブ": [5],
"have": [0],
"powerpc": [2],
"になることがあります": [[0,4,5]],
"キーバインディング": [7],
"カーソルセグメント": [4],
"question": [0],
"かどうかを": [6,[1,2,4]],
"経由": [2],
"editselectsourcemenuitem": [0],
"隣接": [5],
"れられる": [2],
"容易": [3,2],
"情報源": [0],
"りたい": [0],
"段階": [[2,7]],
"日本語入力": [4],
"徹底的": [3],
"ましくない": [4],
"検索領域": [7,1],
"まれていない": [7,2],
"com": [0],
"instal": [2],
"となります": [1],
"はここに": [5],
"はここで": [1],
"cot": [0],
"remot": [6],
"じことになります": [0],
"補完候補": [[0,1]],
"等間隔": [1],
"プロキシサーバー": [2],
"総数": [5,7],
"原文文書": [2],
"することはできません": [[0,5,7]],
"pipe": [0],
"赤色": [0],
"そのものですが": [[6,7]],
"てることが": [3],
"引用文記号": [0],
"レベル": [[3,7]],
"訳文": [0,7,4,3,1,2,6,5,8],
"数多": [2],
"はできないのです": [7],
"tri": [7],
"changeid": [1],
"translat": [0,1,2,7],
"個別設定": [1],
"訂正": [7],
"université": [1],
"除外構成例集": [7],
"再作成": [[0,2,3]],
"考慮": [1,[0,2,4,6]],
"cqt": [0],
"作業手順": [[3,5]],
"ルート": [0,2],
"有効化": [0],
"自動補完": [0,5],
"むようにします": [3],
"はそれぞれ": [2],
"docs_devel": [2],
"文字範囲": [0],
"第三外国語": [2],
"lck": [5],
"プロジェクト・フォルダー": [7],
"twelv": [0],
"tsv": [0],
"ありません": [[1,2,3]],
"ユーザーフォルダ": [2],
"にしない": [0],
"反対": [0],
"gnome": [1],
"理由": [0,[1,7],2],
"しているので": [3],
"とした": [[2,6]],
"として": [0,2,[5,7],1,4,[3,6]],
"表意文字": [0],
"注釈": [3],
"シェルスクリプト": [0],
"doctor": [0],
"参考訳文": [4,1,5,[0,6,7],3,2,8],
"しているのは": [2],
"sjis": [0],
"定義済": [1,[2,3]],
"appdata": [0],
"追加原文": [2],
"csv": [0,2],
"禁止": [1],
"グラフィカルインターフェース": [2],
"参照用用語集": [0],
"でもう": [7],
"検索結果領域": [7],
"であればどれでも": [2],
"データベース": [3],
"がうめられていきます": [6],
"caractèr": [2],
"させることを": [0],
"fr-zb": [2],
"したり": [7,0,2,[3,5],[1,6]],
"したら": [7,2,0],
"しており": [2,[1,6]],
"ダウンロードページ": [[1,2]],
"caret": [0],
"だけは": [0],
"自動変更": [0],
"だけの": [[0,2]],
"night": [2],
"スペルチェックシステム": [3],
"一連": [[1,2,6],3],
"リポジトリマッピング": [2,7],
"くするのに": [7],
"相違点": [1],
"だけで": [5],
"each": [7],
"報告": [0],
"見積": [7],
"再編成": [0],
"指摘": [4],
"わることがあるようです": [4],
"cur": [0],
"filenameon": [1,0],
"検索語": [7],
"cut": [0],
"ctrl": [0,4],
"editorinsertlinebreak": [0],
"jumptoentryineditor": [0],
"document": [[0,2]],
"文書形式": [[0,2]],
"単語": [0,7,[4,6],[1,5]],
"かれます": [4],
"小数点以下": [0],
"two": [7],
"つになります": [0],
"必要": [2,0,7,3,1,6,5,[4,8]],
"変更情報": [1,[4,5]],
"page_up": [0],
"追加": [0,7,2,3,6,[4,5],1],
"翻訳内容": [4,5],
"検査": [7],
"じてすばやく": [2],
"glossaryroot": [0],
"保障": [8],
"だけでなく": [7,[0,2,3,6]],
"まれていないため": [0],
"そのような": [7,[1,2]],
"場合": [2,0,7,4,1,3,5,6],
"resourc": [2],
"りです": [[0,7]],
"moodl": [0],
"オペレーティングシステム": [[0,2,6,7]],
"team": [2],
"されているので": [[3,4]],
"xx_yy": [0],
"でおこなったのでは": [6],
"docx": [[2,7],[0,4]],
"project_stats_match_per_file.txt": [[4,6]],
"txt": [2,0,5],
"になっていない": [4],
"機能": [0,3,4,1,2,7,[5,6]],
"うため": [0],
"をすばやく": [[3,7]],
"とまったく": [0],
"してから": [7,0,2],
"代替案": [1],
"だけを": [0,1],
"客様": [[1,7]],
"などなどです": [7],
"徐々": [[1,2,6]],
"lib": [0],
"パッケージマネージャ": [2],
"上位": [0,1],
"別々": [[1,7],[2,5]],
"source": [8],
"類似": [2,[0,3]],
"viewdisplaymodificationinfoselectedradiobuttonmenuitem": [0],
"index.html": [0,2],
"がなくなります": [[0,7]],
"標準手順": [2],
"編集領域": [5],
"とその": [[0,1,2,3,4,5]],
"develop": [2],
"diffrevers": [1],
"主要": [2],
"をします": [[0,2]],
"最小限": [6],
"資格情報": [1,2,[5,8]],
"でそのような": [0],
"宣言": [0],
"されないこともあります": [0],
"page": [0],
"下線付": [5],
"できないようです": [2],
"だけが": [0,7,2],
"しておく": [2],
"正規表現": [0,7,1,2,8,3],
"整合作業": [7],
"プロジェクトマッピング": [2],
"されるように": [1],
"プロジェクト": [2,7,3,0,6,4,1,5,8],
"フィルタ": [2,[0,1],[4,7]],
"parenthesi": [0],
"領域": [7,5,[0,3,4]],
"project.gettranslationinfo": [7],
"はありませんか": [3],
"機械翻訳結果": [1],
"doctorat": [1],
"予防措置": [2],
"アップデート": [[0,1,2]],
"メカニズム": [[0,2]],
"一部": [2,0,4,[1,3,6],[5,7]],
"現在": [4,7,2,[0,5],1,[3,6]],
"したい": [7,[0,2],[1,3,6]],
"mymemori": [1],
"構成設定": [2],
"れるまでは": [2],
"典型的": [2],
"regex101": [0],
"equal": [0,2],
"セグメント": [7,[0,3],[1,2]],
"訳文分節内": [5],
"omeat": [3,[2,8],5],
"watson": [1],
"画像": [0],
"グレー": [[3,4,7]],
"コンピュータ": [2,3,4],
"示唆": [5],
"をまだ": [4],
"しない": [0,2,7,1,4,5],
"これには": [[0,2]],
"project_save.tmx.yyyymmddhhmm.bak": [2],
"どおり": [0],
"一致項目": [7],
"viewmarkglossarymatchescheckboxmenuitem": [0],
"地域化": [2],
"回分保持": [2],
"通信": [5],
"することもできます": [2,7,0,[3,6],1,5],
"パーセンテージ": [5,1,6],
"されなくなったような": [1],
"がはいる": [6],
"標準的": [0],
"enter": [0,7,4,1],
"してこの": [[1,7]],
"属性設定": [0],
"られた": [0,3],
"applic": [2,[0,5]],
"bidi": [0,4,7],
"projectteamnewmenuitem": [0],
"gotoprevxenforcedmenuitem": [0],
"バージョン": [2,0,[4,6,7,8]],
"エクスポート": [[2,4,7]],
"該当": [[6,7],4],
"autocompletertablelast": [0],
"memori": [2],
"デザイン": [3],
"値全体": [0],
"indefinit": [0],
"omegt": [2],
"進行状況": [2],
"recogn": [0],
"んだり": [7],
"例外規則": [0,[1,3]],
"log": [0],
"openjdk": [1],
"永住権": [[1,7]],
"チーム": [2,[1,3,4],[0,7,8]],
"されていないか": [1],
"toolscheckissuesmenuitem": [0],
"めます": [7],
"予想": [[0,8]],
"することです": [2,0],
"エラー": [2,5,[4,7],[0,3]],
"翻訳自動反映": [7,2],
"代替言語": [[2,6]],
"orphan": [5],
"個別": [[0,2]],
"されなくなったり": [2],
"となる": [2,[1,7]],
"られる": [0],
"autocompletertablepageup": [0],
"リポジトリー": [0],
"www.deepl.com": [1],
"エンコーディグ": [0],
"config-fil": [2],
"quick": [0],
"形式自体": [0],
"文頭": [4],
"無意味": [0],
"対応": [7,0,2,4,[5,6],1,3],
"したすべての": [7,0],
"があると": [6],
"day": [0],
"lre": [[0,4]],
"記述方式": [1],
"編集機能": [0],
"system-user-nam": [0],
"lrm": [0,4],
"電子": [0],
"format": [0],
"とそれに": [0],
"要素": [0,7,3],
"console.println": [7],
"られるまで": [2],
"rainbow": [2],
"共有": [2,7,6,[0,3],5],
"要約": [1],
"ときには": [3],
"てるには": [4],
"autocompleterlistdown": [0],
"があるか": [4],
"利点": [7],
"どうでしょうか": [3,8],
"特有": [2],
"にするだけでなく": [3],
"のための": [2],
"各項目": [5],
"のために": [[3,7],[1,2]],
"メモリー": [2],
"part": [7],
"しても": [0,7,[1,2,3,8]],
"課題": [4,1],
"ローカル・マシン": [1],
"リセット": [7,0,1],
"簡略化": [1],
"たくさんの": [0],
"しては": [0],
"activefilenam": [7],
"展開": [6,0],
"introduction": [8],
"わないようにしてください": [7],
"それまでの": [[0,4]],
"project_files_show_on_load": [0],
"内部翻訳": [7],
"処理方法": [1],
"もする": [2],
"にそのように": [4],
"固定": [6],
"数字": [0,5,7,1],
"があれば": [[0,2],5],
"build": [2],
"ドロップダウンメニュー": [0,1],
"種類": [7,[0,1],[2,4,6]],
"とても": [7],
"以降": [0,2,7],
"チームプロジェクトリポジトリ": [4],
"entries.s": [7],
"翻訳者向": [3],
"相対": [1],
"グループ": [0,1,[5,7]],
"gotonextuntranslatedmenuitem": [0],
"ってくることができます": [3],
"targetlocal": [0],
"されるようにするには": [2],
"無償": [2],
"path": [2,0,[5,7]],
"するときには": [[2,3]],
"上部": [[1,2,3,7]],
"書式以外": [1],
"回復手順": [3],
"関数": [0,7,1],
"移動制限": [3],
"プレフィックス": [2],
"操作": [7,5,[1,2,3,4],6],
"をやり": [[2,4]],
"起動引数": [2],
"字体設定": [1,[3,5]],
"シーケンス": [0],
"到達": [3],
"したりすることで": [7],
"優先度": [[1,3,4,6]],
"コンバータ": [2],
"helpcontentsmenuitem": [0],
"resnam": [0],
"下部": [7,5,[1,4]],
"omegat-org": [2],
"remote-project": [2],
"熱心": [3],
"詳細設定": [7],
"descript": [7],
"プロトタイプ": [7],
"initialcreationid": [1],
"ignore.txt": [6],
"projectaccessdictionarymenuitem": [0],
"していたときのことを": [3],
"最大化": [5],
"をもって": [3],
"がなければ": [7],
"sentenc": [0],
"えることができない": [3],
"解釈": [0],
"あるいは": [0,7,[2,4]],
"解凍": [2],
"backslash": [0],
"焦点": [2],
"files_order.txt": [6],
"文節属性": [5,[3,8]],
"projectrestartmenuitem": [0],
"editorskipnexttoken": [0],
"むことができる": [7],
"編集画面": [7],
"trans-unit": [0],
"そのためには": [0],
"ランタイム": [2,0],
"right": [0],
"置換操作": [7],
"信頼": [1,[0,2,7]],
"完了": [7,[0,2,5,6]],
"qigong": [0],
"空訳文": [[0,4,5]],
"むけのものです": [0],
"文書中": [[0,3]],
"ビュー": [[0,1]],
"初期分節": [7],
"するときのみ": [0],
"とのみ": [0],
"といった": [0],
"参考": [[2,6]],
"dir": [2],
"down": [0],
"うには": [[0,1,2]],
"しようとします": [[1,2]],
"われていません": [7],
"何箇所": [7],
"bracket": [0],
"viewfilelistmenuitem": [0],
"させてくれるので": [3],
"virsion": [2],
"不十分": [2],
"info": [0],
"リモートロケーション": [2],
"non-break": [3],
"journey": [0],
"イベントメッセージ": [0],
"test": [2],
"分節属性": [5],
"をいかなる": [8],
"とすべての": [2],
"支援": [[2,3]],
"omegat": [2,0,7,3,1,4,6,8,5],
"検証": [0,1],
"用語集画面": [1],
"allemand": [1,7],
"ワードプロセッサ": [7],
"deepl": [1],
"トークン": [0],
"サンプル": [7],
"していれば": [[2,7]],
"しませんが": [0],
"訳文言語書式": [2],
"てるように": [2],
"ユーザーレベルコマンド": [[1,7]],
"にできます": [4],
"になりました": [3],
"修飾属性": [7],
"console-align": [[2,7]],
"back": [0],
"projectopenrecentmenuitem": [0],
"fr_fr": [3],
"miss": [0],
"りするよりも": [2],
"thèse": [1],
"オフィススイートファイル": [2],
"うのか": [2],
"もっと": [0],
"human": [1],
"キーワード": [7,3],
"していると": [2],
"したりすることができます": [3],
"にそれを": [6],
"はすべて": [0,2],
"ぐために": [2],
"げられます": [6],
"issue_provider_sample.groovi": [7],
"ぐための": [2],
"各主要": [2],
"unl": [5],
"しているか": [0],
"editoverwritemachinetranslationmenuitem": [0],
"マーカー": [[0,1,5]],
"くその": [0],
"console-stat": [2],
"ingreek": [0],
"底部": [5],
"地域設定": [2],
"lunch": [0],
"f12": [7],
"パニック": [3],
"していき": [7],
"しいことを": [[5,7]],
"従前": [0],
"については": [[0,2],7,4,1,3,5,6],
"projectexitmenuitem": [0],
"じずに": [0],
"参考訳文候補": [6],
"になりますが": [[2,6]],
"ります": [0,7,5,6,[1,2,3]],
"adoptium": [2],
"text": [2],
"ことにしました": [3],
"メインメニュー": [[4,5]],
"en-to-fr": [2],
"editregisteruntranslatedmenuitem": [0],
"init": [2],
"してさらに": [3],
"ロケール": [[0,2]],
"アドレス": [2,[5,6]],
"manag": [2],
"manifest.mf": [2],
"訳文言語": [0,2,[3,7],[1,6]],
"maco": [0,2,4,5,1],
"後半": [2],
"doc": [7,0],
"output-fil": [2],
"集計対象": [7],
"status": [0],
"参照文書": [6],
"本書": [3,8],
"桁数": [0],
"している": [2,0,7,[1,5],3],
"ペナルティフォルダー": [6],
"dot": [0],
"paramet": [2],
"run-on": [0],
"ネットワーク": [2],
"タイトル": [[4,7]],
"mac": [[3,4,8],5],
"まれていると": [0],
"独立": [[2,5]],
"ばれる": [0,2],
"優先": [7,2],
"アップグレード": [2,[1,7]],
"自動翻訳": [1,4],
"右端": [5],
"であっても": [0],
"状態": [[4,7],[5,6],[0,2,3]],
"map": [2,6],
"全角": [7],
"ボックス": [7,1],
"するはずです": [2],
"がそこに": [3],
"設計": [[2,3]],
"url": [2,1,[3,6],[0,4,7]],
"構文的": [7],
"方向": [0,4,5],
"uppercasemenuitem": [0],
"viewmarkuntranslatedsegmentscheckboxmenuitem": [0],
"反復": [0],
"のたびに": [2],
"クリック": [7,5,[3,4],1,0,2,6],
"needs-review-transl": [0],
"tagwip": [7,3],
"していた": [4,[3,5]],
"ダイアログボックス": [4],
"usb": [2],
"use": [1,2],
"そのまま": [7],
"usd": [7],
"作業内容": [7],
"さらには": [7],
"オンライン": [0],
"サービス": [1,2,[4,5]],
"堅牢性": [3],
"omegat.jar": [2,0],
"omegat.app": [2,0],
"usr": [[0,1,2]],
"になりません": [7],
"作業": [2,[3,7],0,[1,6]],
"もなく": [2],
"日付": [1,0,[3,6]],
"ローカル・ディレクトリー": [[1,4]],
"単一": [7],
"検索欄": [7],
"utf": [0,6],
"をより": [[2,7]],
"グローバル": [[0,7]],
"採用": [7,0],
"気付": [[3,7]],
"サーバー": [1],
"づくまでに": [3],
"なさまざまな": [5],
"質問": [0],
"無効": [4,7,1,0,[2,5]],
"したりすることもあります": [2],
"カウンター": [7],
"性能": [1],
"出発点": [1],
"dsl": [6],
"原文": [0,7,4,[1,2],3,5,6],
"ホーム": [[0,1,2,3,4,5,6,7]],
"エディタペイン": [4],
"にするために": [0,[2,7,8]],
"ユーザインタフェース": [[0,1]],
"提供": [2,1,3,7,0,5],
"母音": [0],
"気軽": [3],
"med": [4],
"自動改行": [4],
"dtd": [[0,2]],
"ポップアップメニュー": [5],
"注意点": [2],
"標準出力": [2],
"英語": [2,0,1],
"make": [2],
"検索結果画面": [7],
"をもとに": [3],
"方向書式設定文字": [4],
"projectcompilemenuitem": [0],
"console-transl": [2],
"えるはずです": [3],
"推奨場所": [0],
"ビルド": [2],
"アルゴリズム": [7,4,0],
"わせをすばやく": [7],
"optionsautocompletehistorycompletionmenuitem": [0],
"gotonextuniquemenuitem": [0],
"変更前": [2],
"リポジトリ": [2,5,[1,6,7]],
"管理者": [2,4],
"ハードドライブ": [2],
"オフラインユーティリティ": [2],
"のとおり": [0],
"区切": [0,[1,7],2,[3,4,5]],
"about": [0],
"commit": [2],
"targetlocalelcid": [0],
"チェッカー": [1,[0,4]],
"project_stats_match.txt": [[4,6]],
"興味深": [0],
"中身": [[3,6]],
"選択肢": [7,4,[1,5]],
"対訳集": [2],
"区別": [0,7,3],
"品質管理": [7],
"変換用": [7],
"文章": [0,[5,7],1,4,[2,3]],
"ディスク": [7,2],
"不変性": [6],
"索引": [7,0],
"libreoffic": [0],
"えられるはずです": [4],
"autocompleterclos": [0],
"ばれています": [0],
"することをお": [2,7],
"いている": [7,[0,2,4,5]],
"がもともと": [1],
"のときに": [3],
"プロジェクトメモリ": [6],
"経験": [3],
"編集内容表示": [5],
"文節化規則": [2],
"めることができます": [5,6],
"long": [0],
"配下": [7],
"ドロップ": [5,[2,6]],
"インストール・スクリプト": [2],
"のさらに": [0],
"デフォルトバージョン": [2],
"カナダフランス": [1],
"ローカルプロジェクトフォルダー": [2],
"アスキー": [0],
"マスター・パスワード": [1],
"背景色": [6,4],
"star": [0],
"するなどの": [2],
"しません": [0,2,[1,7],[3,5]],
"するなどで": [0],
"viewdisplaysegmentsourcecheckboxmenuitem": [0],
"editregisteremptymenuitem": [0],
"stats-output-fil": [2],
"プラグイン": [[1,2],0,[3,8]],
"しています": [2,0,7,[4,5,6],[1,3],8],
"open": [0,[1,2]],
"します": [0,[2,7],4,1,3,5,6],
"project": [2,6,5],
"取得": [[1,2],7,4,[0,5]],
"xmx1024m": [2],
"停止": [[1,7]],
"マウス": [7,[3,4,5]],
"はすでに": [1],
"いはじめるときは": [3],
"マッピングリポジトリ": [2],
"がうまくいけば": [3],
"固有名詞": [5],
"しました": [3,5],
"penalty-xxx": [[2,6]],
"影響": [[0,2],7,[1,3,4,5]],
"gotonextsegmentmenuitem": [0],
"カスタマイズ": [0,4,[1,2,3,5,7]],
"地域": [0,2],
"子音": [0],
"どんどん": [3],
"いくつかの": [0,[1,2]],
"ポップ": [0],
"されるので": [[3,6]],
"復元": [[2,5],1,[0,4,6,7]],
"ブランチ": [2],
"dropbox": [2],
"abort": [2],
"left-to-right": [0],
"ローカルチームプロジェクト": [2],
"わらない": [0],
"文字": [0,7,2,4,[1,3],8],
"置換領域": [7],
"くのを": [7],
"それだけでは": [0],
"訪問履歴": [4],
"printf": [0,1],
"抽出": [7,1],
"以外": [0,1],
"じたり": [2],
"修飾": [3,0,8,4],
"訳語": [[1,3,5]],
"存在": [7,4,2,[0,1]],
"じでない": [[0,7]],
"セット": [1,0],
"いていても": [0],
"番目以降": [1],
"準備作業": [2],
"になります": [2,0,[1,6],[3,5,7]],
"一般的": [2,[0,7],[1,5]],
"作成後": [2],
"しますが": [0,7,[2,3]],
"十分": [[2,3]],
"registri": [0],
"つすべての": [2],
"安全": [2],
"辞書検索": [4],
"段落内": [[3,5]],
"step": [0],
"bash": [[0,2]],
"tmroot": [0],
"アポストロフィ": [0],
"mark": [0],
"単位": [0],
"stem": [1],
"についてさらに": [0],
"準拠": [2,[3,7]],
"注目": [3],
"保存": [0,7,2,4,1,6,3,5,8],
"分類": [4,0],
"ケース": [2,[0,3]],
"スクリプト": [7,0,[2,4],1,8,[3,6]],
"現存": [1],
"大学": [1],
"されることは": [[1,2]],
"参考訳文表示": [1],
"直後": [0],
"訳文領域": [5],
"されたすべての": [[0,2]],
"できるように": [2,7],
"はそれらを": [3],
"ソースデータ": [4],
"ながら": [7],
"insertcharslr": [0],
"それぞれについて": [4],
"work": [[0,7]],
"できるようにします": [2],
"くことができなくなる": [3],
"メンバー": [2,3],
"コマンドパラメータ": [1],
"されているはずです": [2],
"配付": [7],
"文単位": [7,0,3],
"指定言語": [2],
"進捗情報": [5],
"word": [0,[3,7]],
"つけるには": [0],
"はそれらの": [3],
"平均": [7],
"lingue": [1],
"するのではなく": [3],
"前述": [0],
"フッタ": [0],
"相当": [[1,2]],
"各章": [3],
"完全": [0,2,4,[5,6]],
"順序": [1,[0,5],[6,7]],
"にあわせて": [3],
"ソースフォルダ": [2],
"スペースバー": [0],
"太字": [1,[5,7],0,3],
"つけるのに": [[0,3]],
"vcs": [2],
"lingvo": [6],
"のところ": [1],
"developer.ibm.com": [2],
"できるはずです": [2],
"保管": [1,[0,4]],
"これまでに": [2],
"mrs": [1],
"であるかどうかを": [5],
"しのある": [[1,4],0],
"だけではなく": [0],
"互換": [0,2],
"数分以上": [3],
"現在選択": [5],
"名称": [[0,1,4]],
"翻訳対象一覧": [7],
"各要素": [7],
"標準": [4,[1,5,7],2,3,[0,6,8]],
"じです": [[1,7],2],
"最大分節数": [1],
"るには": [[2,3,7]],
"みします": [[2,4,7]],
"きされる": [2],
"になるにしても": [6],
"html": [0,2,[1,3]],
"されるとき": [2],
"自動検出": [1],
"グレーアウト": [4],
"spell": [0],
"そうしないと": [2],
"簡易版": [7],
"insertcharsrl": [0],
"翻訳総局": [4],
"ダブルワード": [0],
"バックアップファイル": [2],
"一致候補": [0],
"プロジェクトマネージャ": [2],
"統計的分散": [7],
"一時的": [[1,5]],
"要素内": [0],
"除外定義": [7],
"外部変更": [4],
"finit": [1],
"をとても": [3],
"みやすくすることができます": [0],
"マクロ": [7],
"www.ibm.com": [1],
"実例": [5,2],
"書込": [4],
"てられた": [4,[0,6,7]],
"使用可能": [7,2,0,[1,5]],
"おそらく": [2],
"ウインドウ": [[4,7],[1,2,5],0],
"プロジェクトイベント": [2],
"toolsalignfilesmenuitem": [0],
"ナビゲート": [2],
"記載": [[2,7]],
"フィルター": [0,7,2,3],
"かないことがあります": [2],
"最終翻訳": [3],
"command": [4,0],
"されることが": [0],
"メモウィンドウ": [0],
"きなように": [8],
"ぶことができます": [0],
"まれています": [6,2,7],
"進捗状況": [5],
"接頭語": [6],
"onecloud": [2],
"再度解析": [7],
"viewmarkbidicheckboxmenuitem": [0],
"通知設定": [5],
"にすでに": [7],
"fileshortpath": [[0,1]],
"利用": [7,2,[0,1,3],6,[4,8]],
"日本語": [2,[1,7],0],
"一箇所翻訳": [7],
"翻訳状況": [4,1,[2,6,7],0],
"サブメニュー": [[2,7]],
"段組": [4],
"頒布": [8],
"問題箇所": [2],
"提出": [3],
"方向性": [0],
"のままにします": [2],
"まれません": [0],
"version": [2,8],
"えました": [3],
"folder": [5],
"きするかどうか": [5],
"特殊": [0],
"がどれくらいあるかを": [7],
"最低": [[2,5]],
"はなるべく": [7],
"ロード": [4,7,[2,6],[0,1]],
"projecteditmenuitem": [0],
"行末": [0],
"new_word": [7],
"run\'n\'gun": [0],
"ひとつ": [2],
"例外訳文": [0,5,[4,7],3],
"テンプレート": [1,0,8,7],
"nashorn": [7],
"machin": [1],
"unsung": [0],
"なしで": [6],
"をしようと": [2],
"整形": [7],
"last_entry.properti": [6],
"なしに": [7],
"えますが": [0],
"たとえば": [0,7,2,[1,6],[3,4]],
"けたいという": [2],
"しなかったことで": [2],
"もよく": [3],
"されるため": [6,7],
"選択履歴": [0],
"単語文字": [0],
"バックスラッシュ": [0,2],
"分節一覧": [7],
"されない": [0,[1,2,4,7]],
"低減": [3],
"最大限": [4,7],
"重複分節": [5],
"手動補正": [7],
"フィルタリング": [3],
"ないます": [7],
"互換性": [0,[2,7],1],
"autocompleternextview": [0],
"ある": [2],
"付属": [2,7,3],
"すぐに": [7,3],
"specif": [7],
"されています": [2,7,[0,1,3,4],5,8],
"ローカル・ファイル": [2],
"認証情報": [2,1],
"dsun.java2d.noddraw": [2],
"走査": [7],
"ひいては": [3],
"翻訳者": [2,3,0,1,5],
"いた": [7,[0,4],2,[1,5]],
"じであることを": [2],
"以上変更": [6],
"いに": [[3,7]],
"いと": [6],
"いて": [2,[0,3,6]],
"ell": [1],
"ではそれが": [0],
"されるその": [0],
"型付": [7],
"このような": [2,1,[0,3,5,6,7]],
"editorfirstseg": [0],
"このように": [1],
"x0b": [2],
"いの": [0],
"すぎる": [1],
"れるときに": [1],
"トピック": [[0,2]],
"メニュー": [0,4,5,7,3,1,8,2],
"http": [2,1],
"最大文字数": [0],
"いる": [2],
"されていなければ": [0],
"うか": [[0,2]],
"さまざまな": [0,[4,7],[2,3],[1,5]],
"マシン": [[2,7],1],
"装飾": [[0,3]],
"いを": [[3,7],1],
"記述": [[2,7]],
"lisenc": [0],
"すればすぐに": [0],
"できるものの": [0],
"ギリシャ": [0],
"projectsinglecompilemenuitem": [0],
"end": [0],
"lisens": [0],
"最近": [4,[0,2,5]],
"文書内": [[0,2,5]],
"コマンド": [7,1,[0,2],4,[3,8]],
"していない": [4,[2,3],0],
"うと": [2,[0,3]],
"大部分": [0],
"myfil": [2],
"env": [0],
"howev": [2],
"解除": [5,7,2,[0,1,3]],
"special": [0],
"引数": [2,[0,1]],
"okapi": [2],
"ターミナルプログラム": [2],
"するようにします": [0],
"page_down": [0],
"われます": [[4,7],5],
"環境変数": [0],
"になるのを": [3],
"各文字": [0],
"言語設定": [[1,7],3],
"マッチ": [[0,5]],
"されなくなります": [1],
"project_nam": [7],
"system-os-nam": [0],
"insertcharspdf": [0],
"てください": [[1,4]],
"最終的": [3,2],
"えた": [[0,1,6]],
"あなたはそれを": [2],
"専門領域": [2],
"えて": [[0,7]],
"specifi": [2],
"heapwis": [7],
"確認": [2,3,0,7,[1,4],5,6],
"えの": [5],
"nas": [2],
"をつける": [2],
"tar.bz2": [6],
"bundle.properti": [2],
"えば": [[0,2],[1,6],3],
"contributors.txt": [0],
"されていると": [1],
"進化": [0],
"対応関係": [7],
"整合方法": [7],
"でそれらの": [0],
"www.regular-expressions.info": [0],
"ファイルフィルター": [7,[0,2],4,[1,3,6,8]],
"していても": [2],
"されませんが": [4],
"sourcelang": [0],
"それぞれ": [2,0],
"える": [2,7,[0,1,5]],
"触発": [7],
"じます": [7,[0,2],4],
"併記": [6],
"login": [1],
"えを": [3],
"optionsdictionaryfuzzymatchingcheckboxmenuitem": [0],
"できますが": [[0,2,7]],
"いでしょう": [2],
"取扱": [3],
"行区切": [0],
"projet": [5],
"グラフィカルインターフェイス": [2],
"されてきます": [2],
"つかると": [7],
"かく": [0],
"sourcelanguag": [1],
"最適": [[3,5,7]],
"気功": [0],
"かが": [2],
"作成者": [3],
"キーベース": [7],
"gzip": [6],
"helpupdatecheckmenuitem": [0],
"ではでは": [2],
"基本単位": [7],
"マップ": [2],
"esc": [5],
"ダブルクリック": [2,7,[0,4,5]],
"るときに": [5],
"exampl": [0],
"標準用語集": [0],
"をすることができます": [0],
"nostemscor": [1],
"かつ": [2,[4,5,6]],
"project_chang": [7],
"翻訳": [2,3,7,6,5,0,4,1,8],
"画面": [0,[1,3]],
"構文解析": [0],
"console-createpseudotranslatetmx": [2],
"計算方法": [7],
"エスケープ": [0,2],
"プロトコル": [2,1],
"etc": [2],
"から": [[0,2],7,4,5,1,6,3],
"fuzzyflag": [1],
"下記参照": [2],
"予期": [2],
"escap": [0],
"new": [[1,2]],
"きく": [1],
"をふくむ": [[1,2,6]],
"できました": [3],
"かれ": [1],
"poisson": [7],
"runway": [0],
"できたか": [3],
"作成可能": [8],
"原文用語": [4,3],
"ll-cc.tmx": [2],
"められます": [3],
"分節規則": [1,0,7],
"てることができます": [[4,6]],
"きな": [[3,5,7]],
"更新履歴": [[0,4]],
"交換": [3],
"除去": [0],
"われるため": [2],
"きの": [[2,4,5,7]],
"初期設定": [0],
"フルパス": [1],
"アプリケーション": [2,0,4,3,7],
"サポートページ": [0],
"りました": [3],
"リアルタイム": [5],
"grunt": [0],
"くありますが": [2],
"するにつれ": [1],
"システム": [2,0,[1,7],[3,5,6]],
"magento": [2],
"えません": [0],
"マスターパスワード": [1],
"きを": [2],
"プレースホルダ": [1,0],
"心配": [3,2],
"をそのまま": [0],
"くし": [1],
"ここでの": [1],
"大幅": [2],
"ll_cc.tmx": [2],
"高速化": [2],
"くの": [0,[2,3],[1,7]],
"解説文書": [3],
"u00a": [7],
"ここでは": [0,1],
"くと": [5,[0,2,3,4]],
"ユーザーグループ": [[2,3]],
"世界": [2],
"shift": [0,4,7],
"すことでも": [7],
"複数存在": [[4,5]],
"自身": [0,[3,5,6]],
"エディター": [7],
"java": [2,0,1,7,3],
"端末": [2],
"のかわりに": [7],
"xmxsize": [2],
"エンコーディング": [0,[7,8]],
"使用例": [2],
"後処理": [[1,7],0],
"project_save.tmx": [2,6,[3,7],4],
"ナビゲーションコマンド": [5],
"コピー": [2,7,[1,5],[0,4],3,6],
"閲覧": [0],
"確認機能": [6],
"れないようにしてください": [1],
"けた": [3],
"したその": [0],
"powershel": [[0,2]],
"eye": [0],
"められるようにするには": [1],
"信頼性": [6,[2,8]],
"けて": [0],
"けは": [4],
"下回": [1],
"けの": [3],
"改行": [0,7],
"におす": [3],
"appl": [0],
"すでに": [[2,4,6]],
"循環": [0],
"可能用語集": [2],
"意見": [5],
"のものです": [2],
"ヘルプメニュー": [[0,8]],
"されたか": [3],
"sudo": [2],
"ける": [0,[1,2,4,5]],
"記入": [4],
"timestamp": [[0,8]],
"projectaccessrootmenuitem": [0],
"してください": [2,0,7,4,3,1,6,5],
"がされる": [3],
"けを": [4],
"分節": [7,4,0,5,1,3,6,2,8],
"プロジェクトツリー": [6],
"plugin": [0,2,1],
"コンテキスト": [[1,2,5]],
"autocompletertableup": [0],
"とします": [1,2],
"既存翻訳": [3],
"代替翻訳": [1,0],
"この": [7,0,2,4,1,6,5,3,8],
"役立": [0,4,[2,3],7],
"させようとします": [7],
"初期値": [0],
"希望": [2],
"もありません": [0],
"projectcommitsourcefil": [0],
"editinsertsourcemenuitem": [0],
"にあり": [[0,3]],
"したいことがあります": [2],
"報告書": [3],
"viterbi": [7],
"microsoft": [0,[3,7]],
"にある": [7,0,2,[1,4],3,5],
"projectnewmenuitem": [0],
"ecmascript": [7],
"こり": [2],
"最上部": [1],
"ごと": [[2,6]],
"辞書一覧": [3],
"segment": [5,7,[0,1,2]],
"changes.txt": [[0,2]],
"ファジーマッチ": [1],
"単独": [0],
"これ": [[0,3]],
"単語数": [4],
"させ": [7],
"用語集検索": [4],
"ignored_words.txt": [6],
"レビュー": [[2,3],[6,7,8]],
"github.com": [2],
"configuration.properti": [2],
"紫色": [4],
"削除済": [[3,7]],
"autocompleterlistpageup": [0],
"最下部": [[1,7]],
"同期中": [5],
"これにより": [2,[0,5]],
"さな": [6,4],
"コードスニペット": [7],
"くなります": [5],
"参考用語": [5],
"自体": [0,2,7],
"中間変換": [2],
"すように": [0],
"string": [2],
"個人設定": [2],
"プラットフォーム": [2,[0,1]],
"めないでしょう": [2],
"自動保存": [[4,5]],
"再読": [7,[3,4],[0,6]],
"しい": [2,7,0,3,4,1,6,5],
"垂直空白文字": [0],
"推奨": [2,0],
"ぐことはできませんが": [7],
"標準以外": [2,[0,6]],
"段落間": [4],
"制限": [0,3],
"され": [0,[2,7],5,1,4,3],
"not": [0],
"しか": [4],
"できません": [[2,5],0,4],
"文字自体": [0],
"用語集": [0,5,7,[1,4,6],3,2,8],
"ナビゲーションメニュー": [6,2],
"されずに": [[1,7]],
"しく": [2,[0,1],[4,5]],
"タイムスタンプ": [[0,2,6]],
"全体": [2,0,[1,3,4,6,7]],
"主要部分": [7],
"してみてください": [[2,3]],
"可能": [0,[4,7],[2,5,6],1,[3,8]],
"語入力": [4],
"was": [0],
"して": [2,7,0,3,5,1,4,6],
"selection.txt": [[0,4]],
"自動保存機能": [2],
"した": [2,7,4,0,5,[1,3],6,8],
"xhtml": [0],
"ください": [7,2,1,3,0,4,[5,6]],
"しの": [7],
"ローカルファイルマッピング": [2],
"what": [0],
"itoken": [2],
"finder.xml": [[0,6,7]],
"window": [0,2,4,5,3],
"コマンドラインベース": [7],
"接頭辞": [[1,6]],
"エディタ": [[5,7]],
"になっていることを": [3],
"じた": [[5,7]],
"ドライバ": [1],
"ちます": [0,7,[3,4],5],
"じて": [2,[0,1],[3,5,6],[4,7,8]],
"disable-project-lock": [2],
"辞書": [1,6,[3,5],7,[0,4],8,2],
"omegat.pref": [[0,1,7]],
"when": [1],
"なのは": [0],
"最初": [0,7,2,[5,6],1,[3,4]],
"すか": [[4,5]],
"びだす": [0],
"ローカルプログラム": [0],
"通知": [5,[1,2,7]],
"embed": [0],
"しを": [7,4,0],
"空白圧縮": [0],
"文芸的": [0],
"中国語": [1],
"取扱説明書": [[0,4],8,3,2],
"にすることもできます": [0],
"にしています": [0],
"すと": [7,[0,1,4,5]],
"じると": [7],
"まるすべての": [0],
"コントロール": [2],
"なものから": [0],
"ノンブレーク": [7],
"じる": [7,0,[2,4],3],
"ウィンドウ": [5,7,3,4,1,0,6,8,2],
"訳文地域": [0],
"level1": [7],
"直接": [2,[3,6]],
"アルファベット": [0,[5,7]],
"翻訳資材": [6],
"になった": [7],
"direct": [0],
"小数": [1],
"置換件数": [7],
"降順": [0],
"をすると": [0],
"されるすべての": [7],
"空白": [0,7,5],
"再利用": [2,3,[0,1,4]],
"ホームディレクト": [2],
"構築": [2],
"する": [2,0,7,1,4,6,5,3,8],
"ることはできます": [7],
"web": [0,7,2,3],
"がつけられます": [6],
"されていることを": [5,[1,2]],
"en-us_de_project": [2],
"フォルダーコンテンツ": [2],
"ホワイトリスト": [2],
"editselectfuzzy4menuitem": [0],
"editregisteridenticalmenuitem": [0],
"されてた": [6],
"二番目": [7],
"キャリッジリターン": [0],
"双方": [7],
"hanja": [0],
"文書化": [2],
"更新日時": [4],
"分節数": [7,4],
"テキストフィルター": [0],
"保存済": [1],
"キロバイト": [2],
"advanc": [1],
"にするという": [[0,7]],
"などを": [[3,6,7]],
"やその": [6],
"auto-complet": [0],
"辞書内": [4],
"せを": [1],
"とともに": [[0,7]],
"されたら": [[2,3]],
"されたり": [[0,2]],
"拒否": [[1,2,3]],
"dict": [1],
"開発版": [2],
"じることがあります": [4],
"チームプロジェクト": [2,4,6,0,[7,8]],
"えても": [3],
"効果的": [7],
"まれますが": [0],
"翻訳結果": [[0,4]],
"アプリ": [[0,2]],
"プロジェクトプロジェクト": [6],
"したのではないかと": [2],
"などで": [[3,7]],
"などの": [0,2,[3,4,5,7]],
"アスタリスク": [0],
"その": [0,2,7,3,[1,6],[4,5]],
"などに": [4],
"自動化": [[3,7]],
"なりますが": [2],
"わせされるように": [1],
"ではとして": [[1,4]],
"option": [2,[0,4]],
"罫線": [5],
"番号": [0,[1,4],[2,5,7],[3,6]],
"関連性": [[0,1]],
"みしません": [2],
"のあるものは": [1],
"リモートデスクトップ": [2],
"それ": [0,6],
"プログラムフォルダー": [0,[7,8]],
"語幹処理": [1,4],
"からすでに": [3],
"方向書式設定": [0],
"厄介": [3],
"どれか": [0],
"えることができます": [5,[3,6]],
"するさまざまな": [[2,3]],
"きすることを": [[5,6]],
"たち": [0],
"をつけたら": [3],
"形式": [0,2,3,7,[4,6],[1,5],8],
"圧縮": [6],
"たな": [7],
"数分前": [2],
"にすべての": [7],
"のあるすべての": [2],
"みしてください": [7],
"だけ": [2],
"変換": [2,[3,4],[0,7]],
"反映": [0,[2,3,4,6,7]],
"user": [1],
"識別時": [0],
"proxi": [2],
"ワイルドカード": [[0,2,7]],
"extens": [0],
"back_spac": [0],
"でもかまいません": [0],
"エキスポート": [6,[0,2,4,8]],
"効果": [0],
"記録": [6,0,[1,2,4]],
"だと": [7],
"robot": [0],
"集計": [4],
"新規作成": [3,4,[0,1,7,8]],
"登録": [2,1,[0,3,4],[5,7]],
"にして": [6,[0,2,3,5]],
"eclips": [2],
"けをしませんが": [0],
"しているのかを": [8],
"にした": [[0,1,2]],
"ホームディレクトリ": [0],
"diff": [1],
"an": [0],
"editmultiplealtern": [0],
"at": [1],
"するとともに": [3],
"各分節": [4],
"移動領域": [5],
"否定": [0],
"be": [0],
"してみて": [7],
"にする": [0,[1,7],[4,5,6]],
"アクション": [[4,5],[3,7],2],
"filters.xml": [0,[1,2,6,7]],
"自動的": [7,2,1,4,6,[0,3],5],
"br": [0],
"スラッシュ": [0],
"検出": [4,[1,2]],
"ポート": [2],
"けても": [0],
"一度翻訳": [2],
"by": [[0,1]],
"はいずれの": [0],
"けです": [3],
"segmentation.conf": [[0,2,6,7]],
"った": [[4,5,7]],
"はこうした": [3],
"するわけではありません": [2],
"ca": [2],
"cc": [2],
"ce": [2],
"プロジェクトメニュー": [[0,8]],
"って": [3,[2,5],0,[1,4,7]],
"れるとき": [6],
"スキップ": [0,6],
"にあるとみなします": [7],
"つけ": [3],
"文脈": [[1,4,5]],
"つが": [1],
"各機械翻訳": [5],
"cs": [0],
"独自": [[2,7],[1,4,5]],
"renam": [[0,2]],
"apach": [2,7],
"config": [2],
"adjustedscor": [1],
"dd": [2],
"de": [[1,5]],
"くことができる": [4],
"づけ": [4,1,0,2,[5,6,7]],
"管理": [3,2,4,8,[1,6,7]],
"f1": [[0,4,7]],
"つの": [0,2,7,1,[4,5,6],3],
"f2": [[3,5],[0,7]],
"つは": [0],
"f3": [[0,4],5],
"スタイル": [[0,2,7]],
"f5": [[0,3,4]],
"タイミング": [2],
"することによって": [7,[0,2,3,6]],
"dz": [6],
"editundomenuitem": [0],
"書式固有": [[1,2]],
"ja-rv": [2],
"あなたが": [[2,7]],
"which": [7],
"サイト": [0,2,1],
"することもできますし": [3],
"belazar": [1],
"つを": [5],
"en": [0,1],
"することもできますが": [2],
"eu": [4],
"つまたは": [[1,7]],
"にうまく": [2],
"サイズ": [1,7,5],
"としても": [6],
"関与": [2],
"てた": [3],
"でき": [5,[0,1,3]],
"カプセル": [7],
"フラグ": [1,4,0,[3,5]],
"共通": [[0,2,3]],
"てに": [0],
"origin": [5],
"for": [0,7,2],
"exclud": [2],
"fr": [2,[1,3]],
"再度開": [4,2],
"印刷": [0],
"無駄": [2],
"content": [[0,2],7],
"記号": [0,5,2,7],
"ての": [[2,3,7]],
"duckduckgo": [1],
"などのうち": [0],
"です": [0,7,2,1,5,6,4,3,8],
"めします": [2,7],
"applescript": [2],
"問題": [2,4,0,1,3,5,[6,7,8]],
"json": [2],
"有効": [[0,4],1,5,7,[2,6],3],
"helplogmenuitem": [0],
"での": [2,3,[1,4,5,8],[0,6]],
"てる": [[0,2]],
"オペレーティング・システム": [[0,1,2,4]],
"ディストリビューション": [2,7],
"editoverwritetranslationmenuitem": [0],
"でで": [7],
"とし": [0],
"ディレクトリ": [2],
"aeiou": [0],
"では": [0,7,2,4,1,5,3,6],
"なされます": [0,6],
"パーセント": [5],
"単語末": [7],
"になっています": [[1,7],[0,2,5]],
"構造的": [2],
"あなたは": [3],
"呼出": [4],
"としない": [7,1],
"したりすると": [2],
"機能性": [6],
"どう": [3],
"あなたの": [2],
"でも": [[0,7],[1,2,3],[4,5]],
"にいくつかの": [0],
"小数点": [0],
"との": [0,[1,2,5],[4,7]],
"hh": [2],
"とは": [0,1,[2,3,4,7]],
"させるには": [2],
"duser.languag": [2],
"viewmarkparagraphstartcheckboxmenuitem": [0],
"されていません": [[0,2]],
"方向制御用": [[0,4],8],
"されていない": [2,0,[1,4,7]],
"コミット": [2,[0,4]],
"file-target-encod": [0],
"mainmenushortcuts.mac.properti": [0],
"id": [1,0,7],
"https": [2,1,0,[5,6]],
"命令文章": [0],
"ない": [0],
"if": [7,2],
"project_stats.txt": [6,4],
"ocr": [7],
"規則集": [[1,7]],
"oct": [1],
"ポインタ": [5],
"境界": [[0,3]],
"projectaccesscurrenttargetdocumentmenuitem": [0],
"どの": [[0,1,5,7]],
"なく": [2,3],
"されているかどうかによって": [2],
"コロン": [0],
"in": [5,7],
"ip": [2],
"index": [0],
"is": [0,2],
"フランス": [2,1,7],
"なし": [1,[0,5],[2,4]],
"it": [2],
"ポイント": [1],
"projectaccesstmmenuitem": [0],
"odf": [0],
"間違": [[0,7],[1,3,4]],
"ずその": [1],
"中立的": [0],
"波線": [1],
"ja": [[1,2]],
"など": [1,0,7,2,[5,6],4],
"しないようにします": [0],
"橋渡": [2,[0,6]],
"過去": [2,3],
"入出力": [2],
"odt": [[0,7]],
"gotonexttranslatedmenuitem": [0],
"charset": [0],
"librari": [0],
"絶対": [0],
"自分": [[2,3],8],
"としていますが": [2],
"toolscheckissuescurrentfilemenuitem": [0],
"削除": [2,7,0,6,1,[3,4]],
"libraries.txt": [0],
"learned_words.txt": [6],
"にすぐに": [3],
"えます": [[2,7],4,5,1],
"ftl": [[0,2]],
"移動操作": [3],
"なうときに": [7],
"には": [0,7,2,5,6,1,3,4],
"抜粋": [0],
"なり": [[0,1]],
"くしたの": [2],
"なる": [2,1,[0,3,4,5,7]],
"ってください": [[3,4,5,7]],
"遭遇": [3],
"viewdisplaymodificationinfoallradiobuttonmenuitem": [0],
"手動修正": [7],
"にも": [7,2,0,[1,3],5],
"頻繁": [[0,7],3,4],
"la": [1],
"内部変更": [2],
"ではなくなります": [7],
"li": [0],
"ll": [2],
"テクニカル・リファレンス": [0],
"すことができます": [5,0],
"であると": [7],
"こうしておけば": [1],
"ネイティブ": [2,[0,1]],
"windows8.1": [0],
"lu": [0],
"にしないようにしましょう": [2],
"変数": [1,0,[7,8]],
"プロバイダ": [1,5],
"that": [0],
"cycleswitchcasemenuitem": [0],
"mb": [2],
"me": [2],
"リファレンス": [0],
"フルテキスト": [1],
"れます": [1,4],
"をすべて": [2],
"すべて": [4,[0,1,2],[6,7]],
"上限数": [7],
"であるか": [1],
"mm": [2],
"entri": [7],
"作業者": [4],
"プログラムメニュー": [2],
"ms": [0],
"mt": [6],
"本体": [0],
"右上隅": [5],
"my": [[0,2]],
"plus": [0],
"にすばやく": [4],
"license": [8],
"自前": [2],
"はい": [7],
"すべき": [[0,3]],
"回使用": [[3,4]],
"つかった": [[0,2,5,7]],
"licenss": [0],
"メモリデータファイル": [6],
"no": [[0,7],2],
"にそれ": [0],
"code": [0],
"余分": [0],
"のの": [1],
"装飾書式": [3],
"gotohistoryforwardmenuitem": [0],
"head": [0],
"project_save.tmx.timestamp.bak": [6],
"快適": [7],
"されていても": [[1,5]],
"のみ": [0,[4,7],[1,2]],
"of": [7,[0,6]],
"契約": [1],
"じようなことを": [1],
"頻度": [2],
"possibl": [2],
"入力": [7,4,[2,3],1,[0,5],6],
"分節合計": [4],
"ハイフン": [0],
"on": [1],
"or": [0,1,4],
"os": [4,0,5,[3,8],[2,7]],
"であれば": [4,1],
"にその": [7,[1,2]],
"発生": [[0,2],5,[1,4]],
"でこれを": [2],
"ばす": [3],
"editinserttranslationmenuitem": [0],
"シンプル": [[1,3]],
"pc": [1],
"fileextens": [0],
"されていますが": [[3,7]],
"プロキシサーバ": [2,1],
"ウィンドウナビゲーションコマンド": [4],
"オレンジ": [[5,7]],
"判定": [0],
"pm": [1],
"オフィス": [2],
"対訳": [4,5],
"po": [[0,2],1,[5,6]],
"されていますか": [2],
"メガバイト": [2],
"円滑": [7],
"単語分割": [7],
"になっていて": [5],
"qa": [7],
"autocompletertablefirst": [0],
"にどう": [3],
"辞書名": [3],
"翻訳済分節": [7],
"ユーザ": [2,1,[0,7,8]],
"they": [0],
"補完": [1],
"パラメータファイル": [2],
"github": [2],
"にするには": [7,[0,2,5]],
"訳文入力時": [2],
"edit": [7],
"old": [1],
"editselectfuzzy5menuitem": [0],
"them": [0],
"表示言語": [2],
"クリアマーク": [7],
"したくない": [[2,6]],
"表表示": [5],
"文字単位": [0],
"想像": [3],
"rc": [2],
"一覧表示": [5,[1,7]],
"includ": [2],
"対話": [2],
"自動": [2,[0,1,6],[4,5]],
"前回": [7],
"t0": [3],
"t1": [3],
"t2": [3],
"t3": [3],
"ブロックレベル": [0],
"にもどり": [3],
"sa": [1],
"既知": [5],
"sc": [0],
"生成時": [[0,7]],
"括弧内": [0],
"sl": [2],
"翻訳時": [0],
"現在開": [[4,5]],
"になっている": [1,4,7,[0,5]],
"one": [7],
"していたことに": [3],
"柔軟性": [3],
"各自": [2],
"確保": [2],
"パフォーマンス": [3],
"editoverwritesourcemenuitem": [0],
"変換対象": [4],
"omegat.autotext": [0],
"整数": [1],
"関係": [[1,2,3]],
"enforc": [6,4,[0,2],[1,3]],
"れると": [[5,7]],
"remov": [2],
"tl": [2],
"tm": [6,2,4,0,[1,7],[3,5,8]],
"基本": [2],
"to": [2,[0,3,7,8]],
"v2": [2,1],
"のなかの": [0],
"document.xx": [0],
"通常版": [0],
"コンマ": [0],
"ステップ": [7,6],
"のなかに": [0],
"インターネット": [1],
"viewmarkautopopulatedcheckboxmenuitem": [0],
"どちらも": [[1,2]],
"projectwikiimportmenuitem": [0],
"countri": [2],
"ui": [[2,4]],
"右括弧文字自体": [0],
"中括弧文字自体": [0],
"小文字設定": [0,4],
"接続": [2,1,5],
"un": [2],
"up": [0],
"複数単語": [0],
"にさまざまな": [3],
"どちらの": [[0,2]],
"表記": [0,[3,4],1],
"newword": [7],
"のものより": [1],
"テキストファイル": [0,4,1],
"しやすいような": [3],
"this": [0,2],
"りとみなす": [0],
"適切": [2,3,7,[0,1],4],
"opt": [2,0],
"ファイルシステム": [2],
"メモリフォルダー": [[6,7]],
"になければならない": [0],
"プレーンテキストファイル": [7],
"各所": [3],
"know": [0],
"複雑": [0,7,2],
"緊急演習": [2],
"われたすべての": [1],
"changed": [1],
"特別": [0,2,[6,7]],
"テスト": [2,0],
"キャンセル": [0,[4,7]],
"番目": [0,[4,5,7]],
"みやすくするために": [[0,1]],
"保持": [0,2,6,1],
"we": [0],
"られますが": [6],
"関連": [2,0,[3,6],[1,5,7]],
"autocompleterlistup": [0],
"licenc": [0],
"実際": [2,[0,3,7]],
"まれていた": [1],
"omegat.project.bak": [2],
"repo_for_omegat_team_project": [2],
"まれていて": [6],
"box.com": [2],
"きされることに": [2],
"セッション": [[5,7]],
"projectaccessexporttmmenuitem": [0],
"そこにある": [2],
"licens": [2,0],
"形成": [0],
"org": [2],
"マニュアル": [[2,3,4]],
"への": [0,2,3,[4,7],[1,6],5],
"分節編集": [4],
"したいとおもいます": [0],
"リモート・ファイル": [2],
"なされなくなります": [0],
"にしてください": [1,7],
"辞書項目": [0],
"変更": [0,7,2,1,3,6,4,5,8],
"xx": [0],
"sourc": [[2,7],6,4,[0,3]],
"事前定義": [0],
"それらはすべて": [7],
"べて": [3],
"type": [2,6],
"のなしに": [6],
"けます": [[0,2]],
"optionsautocompletehistorypredictionmenuitem": [0],
"projectaccesssourcemenuitem": [0],
"脚注": [0],
"そのために": [0],
"翻訳文": [5,[1,3]],
"yy": [0],
"除外": [[2,7],0,5],
"命令型": [7],
"しないときは": [7],
"段落": [0,7,1,[3,5]],
"をどれだけ": [3],
"push": [2],
"行上": [7],
"行下": [7],
"これらを": [0],
"readme_tr.txt": [2],
"有用": [0],
"追加書": [[0,6]],
"penalti": [6],
"ぎません": [3],
"出自": [1],
"まれている": [2,5,6,[0,4,7]],
"えると": [[0,3]],
"満足": [2],
"活用": [4,2],
"固有": [6,[0,2,4],[1,7]],
"メソッド・システム": [4],
"左側": [7,5,4],
"utf8": [0,[4,7]],
"分類別": [0],
"月間作業": [3],
"ダイアグラム": [0],
"はいずれかの": [0],
"クラウドサービス": [2],
"直接編集": [0],
"限定": [2,[0,6]],
"にします": [4,7,[0,5],1,[2,6]],
"ができるように": [6],
"英数字": [0],
"context_menu": [0],
"縦棒": [0],
"でおわる": [0],
"editsearchdictionarymenuitem": [0],
"tag-valid": [2],
"複数訳文": [5,0,[3,8]],
"ovr": [5],
"えられます": [0,1,6],
"ステータスバーメッセージ": [5],
"help": [2,0],
"イベント": [7,0],
"ツールツール": [2],
"各用語集": [1],
"訳文分節": [4,7,5,1,0],
"repositori": [2,6],
"magic": [0],
"検索対象": [7],
"後続": [0],
"属性": [0,5],
"するか": [7,2,3,[0,4],[1,6]],
"翻訳対象": [0,7,[3,4],[5,6,8]],
"プロジェクト・ディレクトリー": [0],
"内側": [5],
"lowercasemenuitem": [0],
"wiki": [[2,4]],
"おおび": [2],
"autocompleterconfirmwithoutclos": [0],
"ツール": [7,2,[1,4],[3,6,8],0],
"照合": [0,4],
"これらが": [2],
"フロント・ページ": [2],
"ブロック": [7,0,4,[1,8]],
"filepath": [1,0],
"まず": [[0,1,2,3]],
"それらもこの": [5],
"なると": [4],
"多数": [0,[2,5,6]],
"ます": [[2,5]],
"確認用": [0],
"まだ": [[2,3,4,7]],
"また": [2,7,0,[3,4],[1,5,6]],
"コマンドラインオプション": [2],
"brace": [0],
"完全一致検索": [7],
"カテゴリ": [0,8],
"むこともできます": [3],
"まで": [7,[2,4,6]],
"になり": [[0,2,7]],
"クロスプラットフォーム": [2],
"してのみ": [7],
"になる": [[2,5]],
"作成時": [[3,6,7]],
"それぞれが": [[0,2]],
"修飾子": [0],
"カラム": [7,0],
"プログラミング": [7],
"すれば": [[2,3]],
"にのみ": [7,1,[3,4]],
"みの": [[0,7],5,[1,3,4],2],
"しようとすると": [[0,1]],
"まり": [0],
"hero": [0],
"みに": [[2,3,4,8]],
"まる": [0],
"仮想": [7,2],
"許可": [1,7,[0,4]],
"いつでもすべての": [3],
"えられ": [7],
"未翻訳": [0,4,5,[1,2,7]],
"があったときに": [[1,3]],
"git": [2,6],
"ファイルサーバ": [2],
"一緒": [2],
"割合": [[1,5]],
"スライドマスター": [0],
"連携型": [2],
"xx-yy": [0],
"をすでに": [2],
"直接使用": [2],
"follow": [0],
"これらは": [0,[2,3],5],
"これらの": [0,6,[2,7],3,[4,5]],
"すると": [0,7,1,5,[2,4],3,6],
"などがあります": [3],
"むと": [7],
"先頭": [0,4,7,2],
"targetlang": [0],
"みを": [0,3],
"びます": [2],
"初期化": [2],
"めた": [2,0],
"optionssetupfilefiltersmenuitem": [0],
"コード": [0,2,[3,7],1],
"評価": [7],
"していることを": [2,3],
"することが": [2],
"altgraph": [0],
"情報": [[4,5],2,7,[0,1,3],6],
"いずれかを": [5],
"することも": [[3,4]],
"全般的": [7],
"もう": [0],
"stats-typ": [2],
"めて": [2,5],
"内部": [[0,7],[1,5]],
"トークナイザ": [6],
"するためにも": [7],
"these": [0],
"メディア": [2],
"検索条件": [7],
"することは": [3],
"のままにしておくと": [5],
"もし": [[2,4],[0,7]],
"ブラウザ": [[1,4,5]],
"商用製品以上": [3],
"xml": [0,2,1],
"めたばかりで": [3],
"ボタンベース": [4],
"検索範囲": [3],
"されなくなる": [5],
"することで": [[2,7],[3,5,6]],
"することと": [0],
"知識": [2],
"することに": [[0,2]],
"のいずれかを": [7,[0,1,2,4]],
"検出結果": [1],
"往復": [2],
"拡張子": [0,2,6,[1,4,5,7]],
"インライン": [7],
"対象": [0,2,[1,3],7],
"そのものに": [3],
"としています": [0,1],
"マーク": [7,0,1,4,5,[3,6]],
"める": [2,[0,1]],
"自動整合": [7],
"することを": [0,[2,5,7]],
"xdg-open": [0],
"befor": [0,2],
"概要": [[2,8]],
"チェック": [4,1,7,0,[2,3],5],
"のいずれかで": [4],
"のいずれかと": [0],
"システムツール": [2],
"集中": [3],
"簡素化": [2],
"一貫性問題": [2],
"のいずれかの": [[2,3,7]],
"tar.bz": [6],
"インスタンス": [0,2,4],
"それぞれの": [0,[4,5,6]],
"説明書": [0],
"品質": [1,4,[0,2,3]],
"チームメンバー": [[2,3]],
"shebang": [0],
"などにも": [4],
"パラメータ": [2,0,7,[1,6]],
"editorskipprevtoken": [0],
"せずに": [[2,7]],
"のすべての": [2,[0,7],[1,3]],
"ではないか": [3],
"分節識別子": [0],
"表示方法": [1,[3,5]],
"のすべては": [2],
"検索一致": [2],
"ファジー": [1],
"がどのように": [8],
"使用方法": [0,4],
"ラインフィード": [0],
"gnu": [2,8],
"反対方向": [0],
"訪問": [[4,6]],
"ではないと": [0],
"漢字": [0],
"のすべてが": [2],
"方式": [5],
"編集開始": [7],
"suzum": [1],
"target.txt": [[0,1]],
"小文字指定": [0],
"するためには": [2],
"temurin": [2],
"がないように": [3],
"やしていたときと": [3],
"複数言語": [7],
"やり": [0],
"d\'espac": [2],
"変換後": [2],
"stdout": [0],
"traduct": [5],
"場所": [2,0,7,3,[1,6],5,4],
"カンマ": [0],
"とみなして": [0],
"合理化": [3],
"省略形": [0],
"nameon": [0],
"自由": [8,0,5],
"いてもらいます": [2],
"gotonextnotemenuitem": [0],
"gpl": [0],
"newentri": [7],
"品質保証": [4],
"ソフト": [4,7,0],
"予測": [1],
"適合": [[0,7]],
"一度": [7,1,[4,6]],
"autocompleterprevview": [0],
"スペルチェッカー": [1,[6,7],[0,3,4,8]],
"リモートフォルダ": [[2,7]],
"リモートサーバ": [[2,6]],
"よく": [[2,3]],
"ワード": [4],
"ダウンロード": [2,[0,1,3,6],[4,7]],
"状況": [2],
"重複": [0,[2,5]],
"クローン": [2],
"法律文書": [0],
"projectcommittargetfil": [0],
"なければなりません": [1],
"pear": [0],
"po4a": [2],
"japonai": [7],
"omegat.org": [2],
"出現箇所": [0],
"属性値": [0],
"生成": [2,7,[0,3,6],1,[4,5],8],
"したりすることもできます": [7],
"いりません": [3],
"パートナー": [2],
"のそれぞれが": [1],
"用意": [2,4,[0,1]],
"などです": [[2,6]],
"必須": [[0,7]],
"maxprogram": [2],
"テキストパターン": [0],
"with": [[2,7]],
"説明": [0,[2,4],5,[1,3,7]],
"まったく": [0],
"pdf": [2,0,4,7],
"そうすることは": [7],
"らす": [7],
"らず": [2],
"取扱説明書入口": [2],
"autocompletertabledown": [0],
"editornextsegmentnottab": [0],
"より": [3],
"りが": [4],
"についてのみ": [0,2],
"toolsshowstatisticsmatchesmenuitem": [0],
"反転表示": [5],
"viewdisplaymodificationinfononeradiobuttonmenuitem": [0],
"出力形式": [2],
"つだけに": [0],
"タイプ": [[0,2],[1,5,6,7]],
"不具合": [3],
"されていないため": [3],
"にもう": [2],
"トラブルシューティング": [2,[3,6,8]],
"登場": [5],
"write": [0],
"著作権": [4],
"なっていきます": [7],
"gtk": [1],
"再入力": [1],
"ファイルフォルダー": [7],
"project_save.tmx.bak": [[2,6]],
"period": [0],
"りの": [[0,7],[3,5]],
"つけて": [3],
"下線": [4,[1,5]],
"りに": [[0,2,7]],
"学習": [[0,3,6]],
"わせると": [1],
"つけた": [3],
"注意": [[0,2],7,4],
"クリーンアップ": [7],
"後処理用外部": [[0,1,7]],
"projectaccesswriteableglossarymenuitem": [0],
"それらをすばやく": [7],
"めることができ": [6],
"application_shutdown": [7],
"なものまで": [0],
"autocompletertablelastinrow": [0],
"gui": [2,7,[0,6]],
"ると": [[1,4],3],
"定期的": [2,6],
"りや": [0],
"regexp": [0],
"参考訳文用": [7],
"重要": [0,2,[3,6,7]],
"sentencecasemenuitem": [0],
"するまでの": [0],
"権限": [7],
"articl": [0],
"りを": [[1,5],[0,4]],
"通常": [0,2,[1,7],[4,5],[3,6]],
"editorcontextmenu": [0],
"れた": [[1,3,8]],
"きするには": [5],
"減少": [7],
"じになります": [0],
"れて": [2],
"通貨表記": [7],
"選択領域": [[0,4]],
"optionssentsegmenuitem": [0],
"認証": [2,1,[5,8]],
"てのない": [4],
"実行画面": [2],
"bought": [0],
"自動表示": [0],
"ハイパーリンク": [5],
"optionsaccessconfigdirmenuitem": [0],
"けるため": [3],
"charact": [0,2],
"エントリ": [[0,1],[5,7]],
"framework": [2],
"test.html": [2],
"php": [0],
"れる": [7,[1,4,6]],
"xxx": [6],
"条件下": [2],
"任意": [0,7,6,4,2,5,[1,3]],
"smalltalk": [7],
"けられ": [[0,2,5]],
"連絡": [2],
"するだけです": [[3,7]],
"プロジェクトリソース": [6],
"公開": [[2,6]],
"pseudotranslatetmx": [2],
"中括弧": [0],
"セクション": [0,3],
"がすでに": [6,1],
"名前変更": [2],
"つきの": [3,2],
"ツールメニュー": [[0,8]],
"targetlanguagecod": [0],
"構成": [0,2,1,[3,6],[4,5,7]],
"するふたつの": [7],
"わせ": [0],
"editorprevsegmentnottab": [0],
"他方": [7,2],
"わず": [5],
"訳文言語用": [1],
"をつかって": [3],
"直接開": [[4,7]],
"それをもう": [2],
"てられます": [[0,5]],
"チェックボックス": [7,0,1,4],
"技術文書": [0],
"つから": [2],
"既存": [2,5,[3,7],[0,1,6]],
"構成要素": [0],
"連続": [0,7],
"メモリツール": [0],
"バイト": [2],
"メイン": [1,2],
"適用": [[1,7],2,0,5,[3,4,6]],
"からなり": [[3,6]],
"からなる": [0,[6,7]],
"omegat2.6": [0],
"るときにこの": [4],
"わる": [0,[2,7]],
"しないでください": [0],
"われ": [5],
"識別": [1,7,3],
"のおかげで": [[3,4]],
"スペルチェックフォルダー": [1],
"指向": [7],
"担当者": [[2,6]],
"コマンドライン": [2,0,1,7],
"定義内容": [0],
"既定": [[2,6]],
"けると": [0],
"projectnam": [0],
"にまだ": [4],
"参考資料": [0,7],
"omegat.project.yyyymmddhhmm.bak": [2],
"目次": [8],
"をお": [2],
"をご": [7,2,1,3,0,4,[5,6]],
"自動処理": [0],
"させるために": [2],
"configdir": [2],
"ステータス": [[2,5],3],
"サイセンス": [2],
"installdist": [2],
"されるべきではありません": [1],
"a-z": [0],
"スタート": [[2,7]],
"password": [1],
"ローカルプロジェクト": [2],
"このとき": [2],
"による": [[1,4],[0,5,7]],
"つずつ": [1],
"により": [[1,7],3],
"けされません": [5],
"単語境界": [0],
"gotonextxenforcedmenuitem": [0],
"editordeleteprevtoken": [0],
"するかどうかにかかわらず": [0],
"するかどうかを": [[1,4],7],
"置換": [7,4,0,1,3,5,[6,8]],
"オリジナル": [2],
"したりすることはできません": [0],
"するためだけに": [2],
"のとおりです": [[0,2],5],
"されてない": [3],
"んだ": [7,0],
"最大": [[0,2]],
"javascript": [7],
"mediawiki": [[4,7],[0,3]],
"セミコロン": [2],
"メインフォルダー": [2],
"んで": [7],
"基本訳文": [5,[0,4,7]],
"toolkit": [2],
"せずにそれを": [7],
"上記参照": [[0,2]],
"must": [0],
"join.html": [0],
"調整値": [7],
"メンテナンスタスク": [2],
"してこのような": [2],
"なくとも": [[2,6]],
"なります": [0,[2,4,5],1],
"させることもできます": [[0,2,6]],
"強調文字": [3],
"omegat.kaptn": [2],
"原文分節内": [1],
"しておきます": [6],
"失敗": [[2,7]],
"むことによって": [0],
"まれているわけではありません": [2],
"pop": [[0,4]],
"するとすぐに": [4],
"破損": [2],
"文書": [0,7,2,[3,4],[5,6,8]],
"開発": [2,7,[0,1]],
"しているかどうかに": [[1,6]],
"させたい": [[0,5]],
"翻訳不可能": [0],
"空白文字一": [0],
"ブックマーク": [0],
"プロジェクトファイル": [[1,2,5]],
"してさまざまな": [2],
"わくば": [3],
"googl": [[1,2]],
"opendocu": [0],
"認識": [0,3,[2,6,7],[1,4,5]],
"ができます": [[0,1,7]],
"gotoeditorpanelmenuitem": [0],
"ドロップダウンボタン": [7],
"てられません": [3],
"原則": [[3,5,8]],
"調整": [1,[2,3]],
"viewmarkfontfallbackcheckboxmenuitem": [0],
"煩雑": [3],
"レポート": [[2,4]],
"非常": [0,3,2],
"れているように": [3],
"ここを": [0],
"prepar": [0],
"insertcharsrlm": [0],
"通貨記号": [0],
"sourceforg": [2,0],
"han": [0],
"ヘッダー": [0,[4,7]],
"汎用": [1],
"semeru-runtim": [2],
"再配置中": [[4,5]],
"計算": [1,5,7],
"したりする": [2],
"フィルターパターン": [0],
"last": [1],
"用語集項目": [5],
"共通設定": [0],
"editmultipledefault": [0],
"mozilla": [[0,2]],
"参考用語集": [0],
"editfindinprojectmenuitem": [0],
"pro": [1],
"されたときのみ": [4],
"ここに": [[1,2]],
"warn": [2],
"がおかしいときは": [0],
"ここは": [7],
"ここの": [5],
"翻訳可能": [7,0],
"時点": [2,7],
"ここで": [0,1,2,7,[3,6]],
"自分用": [3],
"するように": [2,[0,1,6]],
"相談": [2],
"まかに": [7],
"語形変化": [5],
"セキュリティ": [[1,7],0],
"挙動": [2],
"昇順": [0],
"一番上": [5],
"けしたい": [4],
"ファイル": [2,0,7,3,6,4,1,5,8],
"てたものと": [7],
"創造的": [0],
"設定辞書": [1],
"がそれほど": [0],
"duckduckgo.com": [1],
"えたりできます": [7],
"りあてるために": [[5,7]],
"むために": [[4,7]],
"要求": [2,7],
"ファイルタイプ": [2],
"るために": [[2,7]],
"ユーザインターフェース": [2],
"サポート": [2,7,3,[0,1,6],[4,5,8]],
"テキストデータ": [7],
"カーソルキー": [3],
"わいながら": [3],
"特殊文字": [0],
"っています": [2],
"program": [[0,2]],
"python3": [0],
"名変数": [0],
"ユーザー・インターフェース": [5],
"置換後": [7],
"けられた": [1,0,[3,4,5]],
"エンジン": [5,4,1,7],
"tran": [0],
"構成例": [0,1,7],
"しておきたいときに": [5],
"わしい": [7],
"されているすべての": [1,4],
"iraq": [0],
"dossier": [5],
"ラテン": [0],
"ウィキ": [6],
"brunt": [0],
"きまたは": [2],
"することができます": [2,7,0,[3,5],6],
"各種設定": [2],
"ユーザーレベル": [7,0,1,4,8,[2,3,5]],
"レビュープロセス": [3],
"大文字": [0,4,7,1],
"リソースバンドル": [[0,2]],
"doc-license.txt": [0],
"とすると": [2],
"書式設定文字": [[0,4],8],
"しくないと": [6],
"外観": [1,8],
"チューリッヒ": [1],
"位置合": [[0,1,3,7]],
"pseudotranslatetyp": [2],
"各段落": [0],
"しやすいでしょう": [3],
"くしておきます": [1],
"過程": [6],
"をあまり": [1],
"間隔": [1],
"いたときに": [[0,7]],
"ましい": [2],
"資材": [[3,7],6,8],
"文字列内": [0],
"づいて": [[0,7],[3,4]],
"からその": [3],
"それらを": [7,[0,1,2]],
"アップロード": [2,4],
"括弧文字自体": [0],
"えていますでしょうか": [3],
"なすべての": [2,0],
"ユニコード": [0,4],
"ログイン": [1,0],
"projectclosemenuitem": [0],
"viewmarknonuniquesegmentscheckboxmenuitem": [0],
"半角文字": [7],
"できるようになります": [3,0,1],
"範囲": [0,7],
"するいくつかの": [4],
"いたままにしておき": [7],
"findinprojectreuselastwindow": [0],
"ランチャー": [2],
"したときは": [6],
"readme.txt": [2,0],
"それらの": [0,[2,7]],
"復帰": [0],
"それらは": [[2,5]],
"languagetool": [4,1,[7,8]],
"なもので": [2],
"可能性": [2,[1,3,5],[0,4,6,7]],
"source.txt": [[0,1]],
"files.s": [7],
"条件": [8],
"histori": [0],
"exchang": [0],
"同時": [7,[2,3],[1,4]],
"させることはできません": [0],
"わりません": [7],
"currseg": [7],
"general": [2,8],
"したときと": [7],
"したときに": [[0,6,7],2],
"ナビゲーション": [3,5],
"訳文分節以外": [5],
"カーソル": [5,4,[0,3,7],1],
"autocompletertrigg": [0],
"作業時": [1],
"カスタムタグ": [[0,1],[2,3,4]],
"づけたい": [0],
"選択中": [[0,4]],
"つだけ": [7],
"不一致": [7,4],
"たとえそれが": [0],
"うことができます": [[0,5],7],
"acquiert": [1],
"原文言語": [7,[2,6],[0,1]],
"はさらに": [0],
"現在使用": [2],
"一致": [0,7,6,1,4,3,2,5,8],
"ステータス・バー": [0],
"検索結果": [7,3,6],
"dhttp.proxyhost": [2],
"ソース": [[1,2,4,6]],
"のようになります": [[0,2]],
"手動調整": [7,6],
"からのものであることを": [6],
"しないが": [0],
"editorprevseg": [0],
"あらかじめ": [1],
"a-za-z0": [0],
"再現": [7],
"you": [7,[0,1]],
"制御": [0,2],
"www.apertium.org": [1],
"一般": [[1,2,5],[0,7,8]],
"個以上": [[0,7]],
"ノート": [[0,5]],
"れられたものとして": [7],
"project_save.tmx.tmp": [2],
"起動": [2,0,7,1,4],
"configur": [5,2],
"のようなさまざまな": [5],
"イラク": [0],
"ではまだ": [1],
"unicode": [8],
"千単位": [0],
"ユーザレベル": [7,[0,1]],
"るたびに": [4],
"mega": [0],
"zurich": [1],
"結果": [7,0,[4,5],2],
"単語全体": [0],
"空白文字": [0,7,3,[1,4],2],
"おおきく": [2],
"作成場所": [0],
"optionsworkflowmenuitem": [0],
"にするか": [7],
"ローカルコマンド": [1],
"または": [0,2,7,5,4,1,[3,6]],
"releas": [2],
"てられています": [[3,4]],
"わせだが": [0],
"てきたいささか": [3],
"セパレータ": [5],
"できます": [7,0,[1,2],4,3,5,6],
"キャレット": [0],
"になると": [0],
"しないで": [1],
"けいれる": [1],
"コマンドラインインターフェース": [2],
"交互": [5],
"そうでなければ": [7],
"がないと": [2],
"系列": [0],
"dictroot": [0],
"つには": [2],
"ソート": [0,[4,5]],
"くことができたことを": [2],
"代替": [4,[0,7],1],
"日本語版": [0],
"をつなぐ": [0],
"あとで": [3],
"翻訳支援": [[2,8]],
"xhmtl": [0],
"わせに": [0],
"されるか": [[1,3]],
"しないということは": [0],
"していません": [[0,1,3,7]],
"わせて": [[0,5],2],
"わせで": [0],
"ウェブ": [1,[4,5]],
"切断": [[0,2]],
"つです": [0],
"linebreak": [0],
"subdir": [2],
"をつづる": [0],
"するようになります": [7,[2,6]],
"でなければなりません": [0],
"同期": [2,7,[1,3,5,6]],
"ピリオド": [0,1,2],
"において": [[0,2,4]],
"わずに": [7],
"しなくなりました": [2],
"autocompletertableleft": [0],
"をそれぞれ": [6],
"タスク": [2,7],
"することにより": [1],
"価値": [3],
"forward-backward": [7],
"翻訳状態": [2,[3,6]],
"休憩": [3],
"ダークテーマ": [1],
"単純": [0,1,2],
"交代": [2],
"プラス": [0],
"アイコン": [5,[2,4,8]],
"editorlastseg": [0],
"file-source-encod": [0],
"反転": [[1,7]],
"some": [2],
"各行": [0],
"しているすべての": [2],
"準備": [[0,2]],
"本的": [6],
"しないといった": [4],
"ワードアート": [0],
"alpha": [2],
"文章装飾": [7],
"によっては": [[0,1,3,4,7]],
"わせを": [0,3],
"大学院博士課程修了": [1],
"just": [0],
"ねられます": [7],
"されると": [[2,7],[4,6]],
"のいずれか": [0],
"わせる": [0],
"するようにする": [4],
"editexportselectionmenuitem": [0],
"直接実行": [2],
"がないというまれな": [2],
"訳文言語以外": [[2,6]],
"home": [0,2],
"disable-location-sav": [2],
"レイアウト": [5,[1,3],8,0,7],
"projectaccesstargetmenuitem": [0],
"iana": [0],
"プラン": [1],
"構成物": [0,3,6,[4,7]],
"テキスト": [0,4,7,5,1,3,[2,6,8]],
"わせの": [0],
"最小": [[1,6],[0,8]],
"について": [0,1,[2,3,4,6]],
"用翻訳": [0],
"もることができます": [7],
"インストール": [2,1,0,3,[4,7],[5,6,8]],
"経験則": [7],
"利便性": [7],
"構文": [0,2,7],
"aligndir": [2,7],
"変数定義": [0],
"翻訳作業": [[2,3,6,7],4],
"system-host-nam": [0],
"できるようしてくれます": [8],
"mymemory.translated.net": [1],
"スクリプトファイル": [[2,7]],
"角括弧": [0],
"フリー・ソフトウェア": [2],
"creat": [[2,7]],
"python": [7],
"リスト": [0,2,1,[4,5,6]],
"事前": [[0,2,6,7]],
"まれます": [7,2,[0,4,5,6]],
"うことができ": [1],
"られます": [0],
"しましょう": [3,8],
"ウィンドウレイアウト": [[4,5]],
"かないようにすることができます": [7],
"にすることで": [4],
"しながら": [7],
"きされないようにするには": [3],
"文字数": [5,4,0],
"意味": [0,[1,5,7]],
"差異": [1],
"午後": [5],
"file": [2,7,0,5],
"していました": [2],
"わない": [7],
"までの": [0,7,1],
"かった": [1],
"逐次指定": [0],
"ここまでくれば": [2],
"でなければ": [2],
"における": [4,[0,7]],
"いやすい": [3],
"再適用": [4],
"文字集合内": [0],
"リソース": [3,[0,2,7]],
"回避": [0],
"はそれを": [[1,3,6]],
"オフ": [7,0,1],
"がある": [4,0,[2,7],[5,6],[1,3]],
"があり": [0,[2,7]],
"ステミング": [1],
"されるたびに": [2,[0,6]],
"のうち": [0],
"ライセンス": [2,0,[4,8]],
"ローカル": [2,[0,1,4,7],5],
"していますが": [[2,3]],
"マッチングツール": [0],
"一番簡単": [2],
"賢明": [2],
"invoke-item": [0],
"一文字": [3],
"使用": [0,[2,7],1,3,5,4,6,8],
"要件": [2],
"かその": [2],
"するなどして": [0],
"内容部分": [0],
"再始動": [0],
"オン": [7,[0,1],[2,4,6]],
"をしています": [0],
"source-pattern": [2],
"再開": [3,0],
"参照訳文": [2],
"わった": [3],
"にいっぱいになります": [2],
"名情報": [0],
"旧形式": [7],
"再計算": [7],
"対処": [2,3],
"autocompletertablepagedown": [0],
"っていると": [1],
"仕様": [[0,7]],
"淡色表示": [7],
"ワーキングディレクトリ": [2],
"開始": [0,7,3,2,5],
"task": [2],
"警告": [7,2,[0,1],3,4,6,5],
"にすると": [0,[1,2,4,7]],
"xliff": [2,0],
"うことです": [0],
"true": [0],
"駄目": [4],
"詳細用": [3],
"用語集用語": [4],
"未確定項目": [7],
"groovi": [7],
"共同作業": [3],
"リテラル": [0,1],
"のある": [2,0,3,[1,4,7],6],
"されており": [[0,4]],
"ショートカットキー": [7,0],
"つがより": [3],
"編集": [7,[0,5],3,1,4,[2,8],6],
"ファイル・": [0],
"内容表示": [[2,3,4]],
"プロジェクトレイアウト": [2],
"エラーメッセージ": [[2,5]],
"基準": [[2,4],[0,1,3]],
"インポート": [5],
"訳文生成": [7],
"したりすることがなくなります": [0],
"になったことで": [3],
"にすることも": [0],
"しやすくなります": [4],
"編集中": [0],
"ピンポイント": [7],
"messageformat": [1,0],
"づけた": [6],
"キー": [0,7,4,[1,5],3,[2,8]],
"参照": [2,4,0,7,3,6,5,1],
"master": [2],
"っていれば": [0],
"ウィジェット": [5],
"保証": [7],
"徹底": [0],
"パイプ": [0],
"writer": [0],
"dalloway": [1],
"rubi": [7],
"resource-bundl": [2],
"されます": [7,2,0,5,4,1,6,3],
"yyyi": [2],
"external_command": [6],
"挿入": [0,4,1,6,5,2,3,7],
"表内": [0],
"および": [0,[1,7],[2,3],[4,5],6,8],
"総称": [0],
"editorselectal": [0],
"分節化規則": [7,0,[1,4],[3,8],6],
"テーマ": [1,7],
"runner": [7,0],
"したまま": [5],
"かない": [2,0],
"外部検索設定": [7],
"スムーズ": [3],
"omegat-default": [2],
"するようにしてください": [0],
"user.languag": [2],
"数分後": [3],
"regex": [0],
"表記法": [0],
"meta": [0],
"対象分節": [7],
"確定済": [7],
"omegat.project.yyyymmdd.bak": [6],
"変数名": [0],
"実行時": [0],
"初心者": [3],
"先頭以外": [0],
"フリーズ": [2],
"ハウツー": [3,4,[6,7]],
"配布": [[0,2,7,8]],
"きされますが": [2],
"racin": [5],
"するとよいのか": [7],
"したりできます": [0],
"インストールフォルダ": [2],
"検討中": [3],
"およびその": [4],
"することができる": [[6,8]],
"翻訳中": [7,[3,4],[0,5]],
"thorough": [2],
"のついた": [1],
"ドッキング": [5,[1,3]],
"短縮形": [0],
"ibm": [[1,2]],
"サーバ": [2,[1,5]],
"アクセス・キー": [1],
"翻訳済": [7,4,0,5,[1,3]],
"分節化規則集": [7],
"parsewis": [7],
"すことはありません": [0],
"りあてることができます": [7],
"正規表現検索": [3],
"におすすめの": [2],
"スペルミス": [[1,4,7]],
"スペルチェッカーファイル": [2,1],
"理解": [[0,2,7]],
"されている": [7,2,0,[1,4],3,5,[6,8]],
"英字": [3],
"autocomplet": [0],
"分節化": [0,7,[1,3,6]],
"することがありません": [0],
"まない": [0,[1,2]],
"されていた": [7],
"まとめ": [2],
"かれています": [7],
"omegat-cod": [2],
"ではなく": [0,7,4],
"エンコード": [6],
"されていて": [2],
"直接作成": [2],
"にどのように": [1],
"置換文字列": [7],
"idx": [6],
"見付": [1],
"ではない": [0,[2,4]],
"squar": [0],
"損失": [2,3],
"ペナルティ": [6,1],
"autocompleterconfirmandclos": [0],
"大丈夫": [0],
"how-to": [0],
"projectaccesscurrentsourcedocumentmenuitem": [0],
"前後": [1,0],
"linux": [0,2,4,5,[1,3,7]],
"をいくつか": [0,2],
"linux-install.sh": [2],
"できない": [2,[0,1],[3,7]],
"受信": [1],
"されているような": [2],
"クレジット": [4],
"されているように": [2],
"file.txt": [2],
"設定変更": [1],
"openxliff": [2],
"ifo": [6],
"応答": [[5,7]],
"をここに": [5],
"excit": [0],
"したほぼ": [2],
"わないまでも": [4],
"えておくと": [[3,7]],
"でもあります": [0],
"双方向": [4,0],
"optionsmtautofetchcheckboxmenuitem": [0],
"初期位置": [[1,4,5]],
"ローカルコピー": [2,4],
"関数型": [7],
"基本的": [0],
"xx.docx": [0],
"まれていますが": [[1,3,7]],
"分節内": [5,[0,7]],
"尊重": [2],
"倍数": [2],
"履歴予測": [3],
"になったため": [2],
"参加": [[2,3]],
"editorshortcuts.properti": [0],
"書式": [2,[0,3,6]],
"設定": [0,7,2,4,1,3,6,5,8],
"導入": [1],
"破棄": [7],
"指示": [2,0,[4,6,7]],
"リモートファイル": [[2,6]],
"をかけて": [3],
"カスケード": [1],
"チュートリアル": [0],
"ドラッグアンドドロップ": [5],
"そこで": [[3,5],0],
"原因": [3],
"動作": [2,[0,4,8]],
"そこに": [7],
"スクロール": [[1,3,5]],
"保護": [1,3],
"あまり": [2],
"がいくつか": [2],
"消去": [1],
"同一訳文": [[0,4]],
"多少異": [0],
"にいずれかを": [2],
"定義": [0,1,2,7,5,4,[3,6]],
"したことになります": [2],
"同様": [[2,4],0,1,[3,5,7],6],
"二重化": [0],
"左括弧": [0],
"されていないことを": [1],
"tmotherlangroot": [0],
"直近": [7],
"スクリプトフォルダー": [7,[1,4]],
"viewmarknotedsegmentscheckboxmenuitem": [0],
"わせして": [3],
"訳文文書": [2],
"どおりに": [2],
"することにします": [3],
"ほとんどの": [0,4,[1,2,3]],
"積極的": [[0,4]],
"改善": [0,[3,4]],
"ドラッグ": [5,2],
"gotomatchsourceseg": [0],
"複数見": [7],
"プロジェクトフォルダー": [4,6,7,[0,3],2,[1,8]],
"されておらず": [5],
"excel": [0],
"comma": [0],
"むことができます": [7],
"整合内": [0],
"runn": [7],
"ターミナルウィンドウ": [2],
"支援翻訳": [3],
"runt": [0],
"がこの": [[0,6],[2,4,7]],
"stardict": [6],
"omegat.l4j.ini": [2],
"span": [0],
"のようなことができます": [3],
"prefer": [0],
"表示設定": [1],
"えないので": [0],
"つまり": [[0,2]],
"検索": [7,0,1,3,4,[2,5],8,6],
"検索式全体": [0],
"実行可能": [2,0],
"用辞書": [7],
"space": [0,7,[3,5]],
"ペイン": [5,1],
"ドイツ": [7,1],
"セル": [7,0],
"箇所": [[2,7]],
"アーカイブ": [6],
"もあります": [7,[0,2,3,6]],
"検索結果表示画面": [7],
"時間": [2,3,[0,1]],
"editselectfuzzy3menuitem": [0],
"マッピング": [2,7],
"後方": [0],
"解放": [0],
"fals": [0,2],
"project.projectfil": [7],
"まれない": [0],
"一方": [[0,2]],
"それでも": [[2,3]],
"プロジェクトパッケージ": [4],
"検索文字列": [7,4],
"のままになります": [[6,7]],
"コンテキストメニュー": [1,5,[0,3,6]],
"矢印": [7,5,[0,3]],
"元分節": [[0,4]],
"意図": [0],
"再起動": [2,0,[1,4,6]],
"くことができなくなることがあります": [2],
"のごく": [0],
"配置": [7,2,6,1,[0,3,4]],
"public": [2,8],
"タグ": [[0,1],3,7,4,2,5,8],
"変更作業": [3],
"競合": [2,[0,3]],
"されるようにします": [[0,1]],
"tmx2sourc": [[0,2,6]],
"オプション": [2,[0,4],7,1,[3,5]],
"ini": [2],
"一貫性": [4],
"しないことをお": [2],
"タブ": [0,5,1],
"ホスティングサービス": [2],
"dhttp.proxyport": [2],
"非表示": [0,5,7,1],
"判断": [[0,7]],
"subrip": [2],
"疑似翻訳": [2],
"句読点記号": [0],
"設定例": [[0,8]],
"往復可能": [2],
"共存": [2],
"score": [1],
"検索履歴": [7],
"によって": [2,0,4,5,7,[1,6],3],
"いのある": [0],
"えることなく": [2],
"文法": [[4,7]],
"のその": [0],
"なおかつ": [1],
"していないかもしれません": [3],
"raw": [2],
"現在表示": [4],
"行目": [2],
"選択的": [2],
"移動": [0,[4,5],3,7,1,6,[2,8]],
"コマンドプロンプト": [2],
"ソフトウエア": [[2,8]],
"てから": [5],
"がその": [0],
"これに": [0],
"うでしょう": [0],
"ソースフォルダー": [6],
"バージョンアップ": [1],
"これは": [0,7,2,[4,5,6],3],
"特定": [2,0,1,[3,5,7]],
"dollar": [0],
"めるようにしてください": [0],
"メモリ": [2,3,7,6,5,[0,4],1,8],
"これで": [2],
"unbeliev": [0],
"デフォルトエンコーディング": [0],
"由来": [[1,2,6],[0,4]],
"ユーザーレベルファイルフィルタ": [1,[0,7],[4,8]],
"close": [[0,7]],
"ありの": [1],
"表形式": [1,4],
"リモートサーバー": [6],
"にはこの": [4],
"abc": [0],
"連携": [3],
"これが": [0,6],
"けるだけでは": [0],
"toolbar.groovi": [7],
"現在翻訳中": [[5,7]],
"右下": [7],
"遷移": [7],
"拡張": [2,[0,7,8]],
"リモート": [2,4,5],
"右上": [5],
"最後": [0,4,1,7,[2,5],6],
"iso": [[0,2]],
"表現": [0,3,[1,7]],
"いまたは": [0],
"をさらに": [0],
"optionspreferencesmenuitem": [0],
"じることは": [2],
"ファーストクラス": [7],
"post": [0],
"glossary.txt": [[2,6],[0,4]],
"エスケープシーケンス": [0],
"トリガー": [[2,7]],
"ヘルプ": [[0,4],[2,8]],
"管理用": [2],
"があるかもしれません": [5],
"add": [2],
"についてさまざまな": [3],
"初期": [7],
"わせることができます": [2],
"rfe": [7],
"チーム・プロジェクト": [[1,5]],
"port": [2],
"そして": [3,0],
"entry_activ": [7],
"任意指定": [0],
"数値": [1,0,5,[6,7]],
"optionsautocompleteshowautomaticallyitem": [0],
"紹介": [[0,2]],
"gotoprevxautomenuitem": [0],
"くことを": [2,[0,1,7]],
"入力補完": [1,[3,4,5],[0,8]],
"できなかった": [7],
"するまで": [7],
"複合語": [1],
"豊富": [4],
"ishan": [0],
"pasta": [0],
"じですが": [[1,2]],
"そこであなたの": [2],
"ブリッジ": [2],
"方向指示制御文字": [4],
"コンテキストメニューアイコン": [4],
"ごとの": [[0,1,4]],
"modifi": [1],
"espac": [2],
"ぶには": [3],
"変換精度": [7],
"パターン": [[0,1],7,2],
"ローカルフォルダ": [[2,7]],
"水平方向": [0],
"迅速": [8],
"ごとに": [7,2,[4,5],[1,3]],
"分節番号": [[0,4]],
"ユーティリティ": [2],
"targetlanguag": [[0,1]],
"とあれば": [7],
"生産性": [0],
"properti": [2,0],
"onenot": [3],
"editselectfuzzyprevmenuitem": [0],
"number": [2],
"しなければなりません": [0],
"かのいずれかです": [0],
"するすべての": [[1,7],[0,2]],
"まれるため": [2],
"朝鮮語": [1],
"自動挿入": [5],
"になったときに": [2],
"れるかもしれません": [1],
"プロジェクトデータ": [2],
"script": [7,[0,2]],
"けることもできます": [2],
"system": [2],
"垂直方向": [0],
"本章": [5],
"修正": [2,7,4,3,[0,1,5,6]],
"シンボリック・リンク": [2],
"ドル": [0],
"わりに": [[2,3],[1,4,7]],
"キーボードショートカット": [4,3],
"プレゼンテーション": [0],
"local": [2,6],
"yield": [7],
"トラブル": [3],
"されていることがわかります": [6],
"分割位置": [1],
"rle": [0,4],
"実感": [3],
"専用": [7,1,0,4],
"cp932": [0],
"再挿入": [3],
"repo_for_all_omegat_team_project_sourc": [2],
"rlm": [0,4],
"業界": [3],
"わらず": [6],
"指定": [[0,2],1,7,[3,4,6],5],
"更新可能": [0],
"しないときには": [7],
"文中": [[0,7]],
"終了": [[4,7],0,2,1],
"c-x": [0],
"mode": [2,7],
"indesign": [3],
"toolsshowstatisticsstandardmenuitem": [0],
"tbx2": [1],
"即座": [[2,5]],
"引用符": [7,0],
"all": [0],
"これを": [2,0,7],
"read": [[0,7]],
"最大数": [7],
"c.t": [0],
"alt": [0,4],
"不明": [2],
"追記": [0],
"効率化": [3],
"unit": [0],
"デフォルト": [0,7,1,2,4,5,[3,6],8],
"フォールバック": [0],
"ローカルマッピング": [2],
"括弧": [0,1],
"正確": [[0,7]],
"されてないかのどちらかです": [3],
"されません": [0,6,7,[1,2,4],[3,5]],
"選択可能": [1],
"じにすることもできます": [5],
"ファイルフィルタ": [2,1,[0,7]],
"鍵認証": [2],
"最重要": [0],
"左端": [7],
"そのため": [7,[0,4]],
"ができるはずです": [6],
"段落区切": [[4,5],1,0],
"スペルチェック": [1,3,[4,7],[6,8]],
"原文分節": [5,7,6,[0,1,4],2],
"変換時": [1,3],
"できることを": [5],
"位置": [0,4,5,7,[2,3,8]],
"文字一覧": [1],
"まれるたびに": [6],
"新規": [[0,2,6]],
"検索式": [0],
"強制": [0,6,1],
"tkit": [2],
"and": [0,7],
"直前": [0],
"predict": [0],
"環境": [2],
"見出": [4,5],
"からすべての": [5],
"バグ": [[0,4]],
"手動": [7,2,[3,4,6],0],
"ant": [[2,7]],
"再配布": [8],
"強制的": [4],
"がかかる": [[1,3]],
"文字以上": [0],
"されていないものとして": [1],
"にあります": [2,[1,4],5,7],
"できるでしょう": [3],
"何度": [3],
"helplastchangesmenuitem": [0],
"omegat.ex": [2],
"だけをそのまま": [0],
"むけの": [2],
"ログファイル": [4,0],
"sourcetext": [1],
"ったことが": [3],
"モノリンガル": [0],
"習得": [0],
"自動補完機能": [[0,3]],
"保存間隔": [[1,2,4,6]],
"パス": [0,[1,2]],
"外側": [0],
"できるようにし": [0],
"翻訳領域": [5],
"jar": [2],
"api": [[1,7]],
"関係者全員": [2],
"解析": [5],
"させる": [0,[1,6]],
"editselectfuzzy2menuitem": [0],
"からも": [7,1],
"プロセス": [[2,3],[0,7]],
"上書": [2,5,4,[0,3],6,[1,7]],
"しているときにも": [7],
"からは": [4],
"からの": [6,0,4,2,[5,7],3,1],
"入力中": [5],
"したがって": [0,2,[3,7]],
"確定": [7,4,[1,3]],
"編集可能": [0,5],
"からと": [6],
"になることに": [0],
"letter": [0],
"従来": [0],
"するのに": [3,0,[2,7]],
"されますが": [2,[1,7]],
"しなくなることがあります": [7],
"するのと": [[1,5,7]],
"ることができます": [4,[2,3,6,7]],
"注記": [2,7,0,4,1,[3,5],6],
"未翻訳分節": [7,0,4,[1,5],[3,6],2],
"付与": [2,1],
"グラフィカルプログラム": [2],
"editornextseg": [0],
"便利": [7,2,[0,3],[1,5]],
"するという": [0],
"インターフェース": [2,[0,4]],
"editselectfuzzynextmenuitem": [0],
"れずにおきます": [1],
"修正済": [6],
"エントリー": [7],
"gotonextxautomenuitem": [0],
"されました": [5,6],
"強力": [0,7],
"read.m": [0],
"ステータスバー": [5,[3,8]],
"ドット": [0],
"are": [0,7],
"cloud.google.com": [1],
"しているときのみ": [7],
"readme.bak": [2],
"arg": [2],
"仕事": [3],
"かれた": [[0,2,4,5]],
"多言語": [0],
"ドック": [2],
"はあなたの": [4],
"しようとしている": [2],
"call": [0],
"時間間隔": [2],
"カスタムマッピング": [2],
"関連付": [0,2,[1,4],3,5],
"用語": [5,1,7,[0,3],4,6],
"機械翻訳": [1,[4,5],[0,8],[3,6]],
"tabul": [2],
"末尾": [0,4],
"するには": [7,2,0,[1,6],[3,4,5]],
"標準設定": [0],
"がついた": [[0,3]],
"toolsshowstatisticsmatchesperfilemenuitem": [0],
"リリース": [4],
"利用者": [1],
"企業": [2],
"規則": [1,0,7,3],
"設定済": [1],
"run": [7,0,2],
"併存": [2],
"パッケージ": [2,4],
"分節化設定": [2],
"矛盾": [7],
"記憶": [2,[4,6]],
"があるわけではありません": [1],
"技術的": [2],
"editorshortcuts.mac.properti": [0],
"外部": [[0,1],[2,3,5],7],
"オフライン": [2],
"titlecasemenuitem": [0],
"翻訳作業用": [2],
"くことができるように": [2],
"まりの": [0],
"われた": [[0,3]],
"editcreateglossaryentrymenuitem": [0],
"まりに": [3],
"パラメーター": [2],
"上半分": [1],
"わりを": [1],
"参照用": [3,[2,7]],
"アクセス": [0,2,4,7,3,6,1,8],
"のない": [3,7,[0,1,2]],
"introduc": [7],
"プロジェクトベース": [7],
"多和田葉子": [7],
"れてから": [7],
"色付": [4,0,7,[5,6]],
"name": [0,2],
"統計": [5,6],
"資源": [7],
"されたしきい": [[2,5]],
"四角形": [1],
"みとして": [[1,7]],
"まれる": [2,6,[1,5,7]],
"されたときに": [[2,7]],
"通常開発者": [0],
"ろうとし": [7],
"視覚化": [0],
"book": [0],
"制御文字": [0,4],
"させられました": [3],
"ルック": [1],
"がなく": [1],
"検索結果分節": [7],
"またはそれに": [0],
"リモートプロジェクト": [2],
"配色": [1,4],
"introduct": [3,[2,8]],
"できるようにする": [2],
"がない": [[4,7],[0,2,5,6]],
"内容": [7,2,3,1,6,4,[0,5]],
"editortogglecursorlock": [0],
"てています": [0],
"まれに": [2],
"例外": [1,0,2],
"new_fil": [7],
"右側": [7,5,0,3],
"がでる": [3],
"まれた": [0,2],
"target": [1,[4,7],6,[0,3],[2,8]],
"われる": [0],
"にもっとも": [5],
"パネル": [7],
"範囲外": [0],
"堅牢": [2,7,3],
"データ": [2,1,4,7,[3,8]],
"維持": [0,3],
"config-dir": [2],
"editorskipprevtokenwithselect": [0],
"絵文字": [4],
"大体": [2],
"くなるように": [7],
"アンダースコア": [0],
"termbas": [0],
"ペア": [1,[0,2,7]],
"用語定義": [7],
"いつき": [3],
"分節間": [4],
"いことが": [6],
"公式": [[0,8]],
"整列": [[4,7]],
"されないようにする": [6],
"targettext": [1],
"ポップアップ": [7],
"いつでも": [6],
"最大行数": [0],
"orang": [0],
"非分割": [1],
"compil": [7],
"くことで": [0],
"いことを": [6],
"edittagpaintermenuitem": [0],
"簡単": [0,3,2,[6,7],[1,8]],
"強調表示": [7,[0,5]],
"開始角括弧": [0],
"暗号化": [0],
"防止": [2],
"にあるすべての": [2],
"more": [5],
"display": [0],
"メモリファイル": [7,[2,3]],
"バイリンガル": [6],
"unicod": [0,4],
"viewmarknbspcheckboxmenuitem": [0],
"かないんですか": [2],
"するときか": [2],
"らかの": [1],
"らかな": [[2,6]],
"はこれらの": [[2,6]],
"自動訳文挿入": [6],
"くことが": [3],
"再起的": [7],
"説明上": [4],
"いません": [0],
"同一": [7,2,6,4],
"されるときに": [1],
"するときは": [[2,7]],
"できるため": [2],
"path-to-omegat-project-fil": [2],
"whitespac": [2],
"時間後": [3],
"であれ": [0],
"作業中": [[3,4,6]],
"である": [0,2,[4,7],[1,5,6,8]],
"後処理用": [7,[0,1],8],
"したりしないでください": [2],
"するときの": [7],
"打鍵": [0],
"文字列定義": [0],
"するときに": [7,0,2,[1,5,6]],
"であり": [0,7,[3,5,6]],
"msgstr": [0],
"小文字": [0,[4,7],1],
"するようには": [2],
"大多数": [0],
"履歴": [1,4],
"nationalité": [1],
"けるために": [[0,2,8]],
"できないように": [3],
"daili": [0],
"えるほうがよいでしょう": [1],
"あいにく": [3],
"一覧": [7,1,4,[0,2,3,8],[5,6]],
"にいつでもこれらの": [3],
"両方": [0,[2,7],1],
"delimit": [[0,1]],
"かれる": [6],
"omegat.project": [2,6,3,[1,5,7]],
"excludedfold": [2],
"targetcountrycod": [0],
"するものもあります": [0],
"ではありません": [[6,7]],
"ミス": [4],
"未使用": [4,[0,3]],
"insert": [7,0,[3,5]],
"以上": [0,[2,7],3],
"優先順位": [1,4,2],
"以下": [0,2,7,[5,6]],
"はできませんが": [0],
"起動時": [[1,2,4,7]],
"実行手順": [0],
"はすぐに": [1],
"回目": [4],
"ソフトウェア": [0],
"ハイライト": [7,[0,5]],
"ソース・ファイル": [2],
"候補": [[3,4,5]],
"also": [2],
"consol": [2],
"分節解除": [3],
"いているため": [0],
"整形用": [5],
"いたり": [[2,7]],
"いため": [0],
"こうとすると": [2],
"itokenizertarget": [2],
"サブフォルダー": [[0,2,6,7]],
"viewmarkwhitespacecheckboxmenuitem": [0],
"のみに": [3],
"のみの": [0],
"asterisk": [0],
"complet": [0],
"bak": [2,6],
"ソースコード": [2],
"分節更新情報": [1],
"bar": [0],
"向上": [0],
"のまま": [1],
"誤検出": [1],
"jre": [2,0],
"のみが": [[0,7],[1,5],[2,4]],
"メモ": [[3,5],4,0,7,8],
"ったりしないでください": [0],
"プログラム": [2,3,[0,1,7],[4,5]],
"原文挿入時": [1],
"コーヒー": [3],
"キーボード": [0,5],
"依頼": [2],
"マッピングパラメータ": [2],
"ノーブレークスペース": [7,4,0],
"リンク": [0,[1,3],[5,6]],
"drive": [2],
"されているため": [2],
"黄色": [4],
"できる": [0,[1,2],[5,7],[3,4,6,8]],
"むため": [0],
"alllemand": [7],
"がこのような": [1],
"フォルダ": [2,7,0,6,4,[1,3,5]],
"かないようにするには": [0],
"delet": [0],
"期待": [[1,2]],
"bcp": [[3,7]],
"するかどうかなどの": [7],
"projectaccessglossarymenuitem": [0],
"javadoc": [7],
"see": [7],
"特徴": [[0,7]],
"がまだ": [1],
"ライブラリー": [0],
"現在手動": [0],
"のみを": [[1,2],7,[0,3,4]],
"ユーザーインターフェース": [2,[0,1,3]],
"スロット": [4],
"格納": [0,6,3,[2,7],1],
"片方": [7],
"アクティブ": [0,[1,5,6]],
"アカウント": [2],
"するため": [[2,4,5,6]],
"特徴付": [0],
"ワープロソフト": [3],
"がさまざまな": [4],
"されているかどうか": [5],
"したように": [0],
"けられている": [[0,2,4]],
"サブフォルダ": [[0,2,6,7],4],
"スコア": [7],
"バックアップ": [2,6,1,7],
"project.sav": [2],
"メインウィンドウ": [5,7],
"してもらう": [2],
"エディション": [2],
"offic": [0,[3,7]],
"クリップボード": [4],
"大規模": [[2,7]],
"repositories.properti": [[0,2]],
"できるとは": [2],
"各々": [0],
"さない": [0],
"repositories": [8],
"projectsavemenuitem": [0],
"をおこなう": [7],
"xmx6g": [2],
"無視": [0,6,[1,7],[2,4,5]],
"いても": [2],
"autocompletertablefirstinrow": [0],
"powerpoint": [0],
"既定値": [4],
"貢献者": [[0,7]],
"収集": [2],
"tmautoroot": [0],
"水色": [4],
"ツールチップ": [1,5],
"めることもできます": [1],
"なってください": [3],
"であることを": [[2,5],1],
"じてこの": [2],
"をすべての": [1],
"派手": [0],
"特性": [1],
"insertcharslrm": [0],
"にしていなければ": [7],
"はそれ": [0],
"現在作業中": [0],
"つからなくなるまで": [1],
"ですぐに": [[2,3]],
"めるべきでない": [2],
"はその": [2,[0,5,6,7]],
"されるようになります": [[0,1]],
"スペース": [0,7,[4,5]],
"終了時": [[1,4]],
"になるように": [[3,5]],
"のほとんどの": [0,4],
"がこれらの": [2],
"ラグ": [2],
"みます": [[0,2,6,7]],
"できるようになり": [3],
"ドロップダウン": [7],
"商業": [2],
"日時": [7,1],
"させるか": [6],
"foundat": [2],
"targetroot": [0],
"のあたらしい": [3],
"ですが": [2,[0,3,6]],
"するとき": [[4,5]],
"select": [0],
"場所設定": [7,2],
"更新版": [3],
"前方": [0],
"bin": [0,[1,2]],
"カーソルロック": [5,0],
"apertium": [1],
"bis": [0],
"kaptain": [2],
"meta-inf": [2],
"欠落": [[0,5]],
"わせになります": [0],
"効率的": [3],
"二言語": [7],
"projectopenmenuitem": [0],
"autom": [2],
"定型文": [1],
"があるのは": [2],
"語句": [7,4],
"整合": [7,[2,4],0,[3,8]],
"検証対象": [0],
"model": [1],
"期待通": [2],
"空行": [0],
"不要": [2,1],
"整理": [0],
"遠慮": [2],
"きされます": [2,3,[0,1,7]],
"いものがあります": [0],
"中断": [2],
"vertic": [0],
"更新": [2,[1,6],[4,7],[0,3,8]],
"詳細情報": [0],
"解説": [4],
"斜体": [[0,7]],
"ショートカットファイル": [0],
"begin": [0],
"完全検索": [3],
"viewmarktranslatedsegmentscheckboxmenuitem": [0],
"paragraph": [0],
"valu": [7,2],
"フォント": [4,[0,1]],
"上記": [2,0,[1,4,7],[3,5,8]],
"機密": [1,8],
"ilia": [2],
"表記規則": [3,8],
"オブジェクト": [7,[0,2]],
"目的": [[0,2],1,[4,6,8]],
"複数": [2,[0,7],[1,4,5],[3,6]],
"クリア": [4],
"uxxxx": [0],
"ロゴ": [0],
"クラス": [0,2,8],
"ログ": [0,4],
"editselectfuzzy1menuitem": [0],
"数字変換": [1],
"ベース": [7,[0,1]],
"れている": [4],
"要因": [4],
"前回編集": [5],
"hide": [5],
"されないため": [7],
"挿入先": [1],
"autocompleterlistpagedown": [0],
"でさえ": [0],
"auto": [4,6,0,2,1],
"らして": [1],
"ウェブブラウザ": [1],
"sign": [0],
"document.xx.docx": [0],
"備忘録": [3],
"editorskipnexttokenwithselect": [0],
"入手": [[1,2]],
"フォルダー": [2,0,6,7,4,1,3,5,8],
"原文文章": [[2,6]],
"download": [2],
"追跡": [[2,5]],
"oracl": [0],
"editortoggleovertyp": [0],
"コミュニティ": [0],
"がどこにあるかわからくなっても": [3],
"でこの": [3],
"商標": [5],
"バンドルフィルター": [1],
"gradlew": [2],
"ファジーマッチング": [4],
"これによって": [7],
"比較": [7],
"操作方法": [2,[3,5,8]],
"新機能": [4],
"level": [7],
"てたいと": [0],
"文書情報": [7],
"右括弧": [0],
"タグチェック": [1],
"改節": [1],
"本当": [4],
"modif": [0],
"非分節化": [0],
"リモートリポジトリ": [2,6],
"れていて": [7],
"viewmarklanguagecheckercheckboxmenuitem": [0],
"原文文章内": [1],
"用語集機能": [7],
"種別": [0],
"めたりできます": [5],
"でしか": [7],
"わせされた": [7],
"表示": [7,5,1,4,0,3,2,6,8],
"内部構造": [0],
"前回使用": [7],
"訳文言語内": [2],
"自動置換": [0],
"をとおして": [0],
"全般": [2],
"switch": [7],
"プロキシ": [1,8],
"移動範囲": [5],
"衝突": [4],
"のあらゆる": [[0,3]],
"bundl": [1],
"むとき": [1],
"自動生成分節": [6,4,[1,2]],
"ショートカット": [0,4,3,7,5,2,8],
"干渉": [7],
"src": [2],
"検索方法": [7,3],
"ラジオボタン": [7],
"control": [4,0,[2,3,5]],
"日本": [2],
"no-team": [2],
"参照翻訳": [[2,6]],
"lissens": [0],
"実施": [3],
"ホスト": [[0,1,2]],
"困難": [2],
"続行": [[4,7]],
"ssh": [2],
"カウント": [4,1],
"部分的": [4],
"部分": [0,5,[3,4,7]],
"environ": [2,0],
"ローカルファイル": [2,5],
"friend": [0],
"それを": [2,[5,7]],
"認証資格情報": [1],
"数回使用": [7],
"更新情報": [0,[1,4,5]],
"するだけで": [3],
"実行中": [[0,4]],
"いほど": [7],
"一致結果": [0],
"kde": [2],
"するかどうか": [[1,7]],
"アラビア": [0],
"ギガバイト": [2],
"方法": [2,0,7,3,4,[5,8]],
"をこの": [[3,5,6]],
"オンラインユーティリティ": [2],
"てます": [[2,7]],
"autocompleter": [8],
"それは": [2],
"作成": [2,7,[0,6],3,4,1,5],
"同僚": [5],
"動的": [7],
"メッセージ": [[4,7],5],
"環境設定": [4,0,1,[3,6],[2,5],[7,8]],
"languag": [7,2],
"二種類": [0],
"にわずかに": [5],
"修正履歴": [4],
"という": [0,2,1,[3,6,7]],
"なときにいつでも": [6],
"current": [7,0],
"ドキュメント": [[0,4],2,7,3],
"視覚的": [0,[3,4]],
"ターゲットファイル": [[0,4]],
"なしになります": [0],
"optionsglossaryfuzzymatchingcheckboxmenuitem": [0],
"語用": [1,7],
"されるかどうかを": [1],
"key": [[0,1,7],2],
"いったん": [7],
"再整合": [7],
"います": [[2,3],0,[1,4,5,7]],
"されないようにします": [2],
"下半分": [1],
"msgid": [0],
"最小化": [5,2],
"svn": [2,7,6],
"launch": [2],
"決定": [[1,7],[0,6]],
"omegat-license.txt": [0],
"stori": [0],
"しやすくすることができます": [[6,7]],
"関心": [0],
"提案": [[0,1,5,7]],
"させます": [[0,2,6],[3,4,7]],
"厳密": [[0,2]],
"日常的": [0],
"するたびに": [0,[3,6],[1,2]],
"editreplaceinprojectmenuitem": [0],
"but": [0],
"symbol": [0],
"でこれらの": [2],
"editordeletenexttoken": [0],
"プロポーショナル": [1],
"完全一致": [7,[2,4]],
"明示的": [2],
"ローカルリポジトリ": [2],
"されることがあります": [2],
"gotoprevioussegmentmenuitem": [0],
"じように": [2,0],
"参照用語集": [0,6],
"gotopreviousnotemenuitem": [0],
"stderr": [0],
"editredomenuitem": [0],
"uilayout.xml": [[0,6]],
"できるほか": [[2,3,7]],
"sourceroot": [0],
"再度": [[1,3]],
"すべてに": [6],
"ターゲット": [[0,1,4]],
"することができない": [2],
"くことができます": [7,2],
"まれていませんが": [4],
"最新": [2,6,[0,3]],
"すべての": [7,0,2,1,4,[3,5,6]],
"むにつれて": [2],
"付加": [7],
"フレーズ": [0,7],
"それに": [0],
"マルチパラダイム": [7],
"緑色": [7,4],
"するかを": [0],
"っている": [0],
"ソースファイル": [0,[2,4],7],
"にならないでください": [3],
"したとはないが": [3],
"字体": [1,4,5],
"物理的": [2],
"高度": [0],
"normal": [7],
"最近使用": [4,0],
"でその": [[0,2]],
"サポートチャネル": [0],
"検索時": [0],
"それが": [[2,5,7]],
"解決": [[3,7]],
"ユーザーレベルファイル": [7,0],
"処理": [0,1,2,3,[4,7,8]],
"をいれます": [7],
"集約": [0],
"ホストサーバ": [2],
"段落単位": [7,0],
"ダイアログ": [7,1,3,0,4,2,8],
"バリエーション": [2],
"大文字小文字": [7],
"がより": [7],
"example.email.org": [0],
"水平空白文字": [0],
"通常色": [6],
"のような": [0,[3,5],[1,4,6]],
"のように": [0,[2,4],[3,5]],
"シート": [0],
"っていて": [2],
"runtim": [2,0],
"そこからの": [6],
"サードパーティ": [[2,3,5]],
"のままにするには": [0],
"またはその": [0,7,[3,5]],
"のいずれかです": [[0,2]],
"モード": [[2,7],5,[0,4]],
"したりするには": [7],
"ルート・フォルダー": [0],
"filenam": [0,5],
"手順": [2,7,3,[0,4]],
"操作履歴": [3],
"roam": [0],
"したあとも": [3],
"between": [7],
"クライアント": [2,[0,6]],
"はありません": [0,[2,7],[3,6],[1,4]],
"けるので": [0],
"文字集合": [0],
"nbsp": [7,4],
"一致率": [6,4,[0,5],[1,2]],
"文末脚注": [0],
"gotosegmentmenuitem": [0],
"があります": [0,2,7,1,[3,4],5,6],
"背景": [[5,6]],
"とおなじ": [5],
"カスタム": [2],
"外部検索": [7,1,4,0,[5,6,8]],
"したあとに": [[4,7]],
"えるかもしれません": [4],
"onedr": [2],
"ジョブ": [3],
"initialcreationd": [1],
"のさまざまな": [0,[3,5,6,7]],
"helpaboutmenuitem": [0],
"組合": [[0,1]],
"欧文": [3],
"付録": [[0,7],[1,4],[2,3,6],[5,8]],
"依存": [0,2,[1,7]],
"優先順": [1],
"構造単位": [0],
"するようにするには": [1],
"翻訳用": [0],
"数回": [2],
"パーツ": [1,4],
"びだすには": [7],
"とするかもしれません": [2],
"テキストエディター": [[0,7]],
"filter": [2],
"site": [1],
"projectroot": [0],
"コンソール": [2],
"訳文用語": [4],
"フォーマット": [[0,2],7,[4,8]],
"right-to-left": [0],
"訳語項目": [1],
"紛失": [[2,4]],
"でない": [2,[0,3]],
"omegat.log": [0],
"よりも": [1,[6,7],[0,2,4]],
"autocompletertableright": [0],
"つために": [4],
"しいと": [6],
"することはなく": [6],
"tab": [0,4,1,5],
"コンテンツ": [0,2,7,6,1,[5,8]],
"できるようになりました": [3],
"項目": [0,4,3,7,[1,5,6]],
"tag": [5],
"コメント": [5,0,[2,7],[3,4,8]],
"占有": [7],
"tar": [6],
"デイリーユース": [2],
"projectreloadmenuitem": [0],
"各取扱説明書": [0],
"構造": [[6,7],2,[0,3,8]],
"パスワード": [2,1],
"微調整": [5],
"通訳者側": [2],
"プロジェクトメンバー": [2],
"半角": [7,0],
"にされません": [4],
"tbx": [0,1],
"いもの": [5],
"するもっと": [3],
"can": [0,7],
"があるため": [2],
"cat": [[0,3,7]],
"標準版": [2],
"duser.countri": [2],
"provid": [2],
"readm": [0],
"ばして": [4],
"名前": [2,0,1,7,[3,5,6]],
"出力": [2,0,7,4,[1,3,5]],
"できませんが": [1],
"えてしまった": [3],
"align.tmx": [2],
"よりよい": [7],
"file2": [2]
};
