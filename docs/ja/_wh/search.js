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
 "OmegaT 4.3.1 - 取扱説明書",
 "メニュー",
 "ウィンドウ",
 "プロジェクトフォルダー",
 "ウィンドウとダイアログ"
];
wh.search_wordMap= {
"情報": [5,6,8,11,[0,2,9]],
"altgraph": [3],
"させることができます": [[5,9]],
"することも": [[1,5]],
"するための": [[4,8]],
"以前": [6],
"もう": [[6,9]],
"めて": [[6,8,11]],
"いたままにして": [11],
"顧客": [6,[5,9]],
"内部": [[10,11]],
"めに": [4],
"するために": [5],
"灰色": [8],
"トークナイザ": [8],
"アプリケーションフォルダ": [5],
"うために": [6],
"メディア": [6],
"されなかった": [11],
"送信": [6],
"検索条件": [11],
"することは": [6],
"もし": [[5,6,11],[0,4,8,9]],
"きます": [8,10,6,5],
"ブラウザ": [[5,8]],
"キーストローク": [3],
"うこともできます": [6],
"けません": [6],
"不必要": [6],
"することで": [5,9],
"知識": [6],
"のいずれかを": [[8,11]],
"info.plist": [5],
"うことで": [11],
"xmx": [5],
"往復": [6],
"機能改善": [8],
"拡張子": [1,0,6,[5,9,10]],
"原文中": [3],
"インライン": [11],
"などとも": [11],
"マージ": [6],
"対象": [5,[2,6,11]],
"としています": [5],
"直接書": [1],
"マーク": [[1,8]],
"める": [5],
"しくは": [11,[6,9,10]],
"ただし": [11,[3,5,6,8]],
"される": [10,[5,6,9],11,[1,7,8]],
"もできます": [11],
"添付": [8,9],
"することを": [5],
"のものと": [6],
"概要": [7],
"チェック": [8,11,[4,6],5],
"util": [5],
"のいずれかで": [6],
"ホームページ": [6],
"いると": [5],
"スタートメニュー": [5],
"tar.bz": [0],
"にとって": [6],
"インスタンス": [5,8],
"それぞれの": [[5,6,8,9]],
"品質": [8,[6,10]],
"がこれにあたります": [5],
"文字色": [8],
"バンドル": [5],
"dgoogle.api.key": [5],
"パラメータ": [11,[5,6]],
"んでください": [9,4],
"result": [2],
"edittagnextmissedmenuitem": [3],
"利用可能": [[3,4,6]],
"せずに": [5],
"動的言語": [11],
"それぞれで": [6],
"のすべての": [6],
"つまりすべての": [8],
"quiet": [5],
"オブジェクトモデル": [11],
"逆変換": [6],
"がどのように": [6],
"es_es.d": [4],
"ラインフィード": [2],
"assembledist": [5],
"the": [[0,2,10]],
"できなくなってしまいます": [4],
"めることができません": [11],
"具体的": [5],
"projectimportmenuitem": [3],
"きします": [[8,10]],
"ばれます": [5],
"漢字": [11],
"選択範囲": [8,3,11],
"わります": [[6,8,10]],
"ぐことができます": [5],
"順次読": [10],
"imag": [5],
"するためには": [5],
"翻訳作業者": [6],
"やり": [[3,8]],
"作業用": [5],
"分野": [6,[10,11]],
"実行": [5,[8,11],6,7],
"場所": [5,10,6,9,[0,1,4,11]],
"途中": [[6,11]],
"トークナイザー": [11],
"カンマ": [2,1],
"言語": [5,[4,6],11,[0,7,8,9]],
"実行環境": [5],
"ることはできません": [11],
"からだと": [5],
"自由": [[0,7]],
"moodlephp": [5],
"すのはとても": [6],
"英文字": [8],
"currsegment.getsrctext": [11],
"optionsglossarytbxdisplaycontextcheckboxmenuitem": [3],
"みになっていることから": [9],
"gotonextnotemenuitem": [3],
"tar.gz": [5],
"gpl": [0],
"ユーザー": [5,7,[2,10,11]],
"専門用語": [[6,9]],
"原因調査": [5],
"というような": [6],
"品質保証": [8],
"にしたい": [6],
"さらに": [11],
"ソフト": [11],
"ターミナル": [5],
"一度": [8,9],
"改訂者": [11],
"xxxx9xxxx9xxxxxxxx9xxx99xxxxx9xx9xxxxxxxxxx": [5],
"したほうがより": [4],
"直接起動": [5],
"結合": [11],
"しかし": [6,[5,11],[1,4]],
"にかかわらず": [6],
"azur": [5],
"リモートフォルダ": [[6,11]],
"fr-fr": [4],
"されるとすぐに": [8],
"セパレーター": [9],
"よく": [5],
"こりえることです": [10],
"たいていはこの": [10],
"でのみ": [6],
"ダウンロード": [5,0,6,[3,4,7,8]],
"にもよりますが": [6],
"ロシア": [5],
"状況": [6,5],
"つけるために": [1],
"重複": [11,2],
"もちろん": [10,[4,9]],
"つけると": [6],
"減算": [2],
"された": [8,6,10,[5,11],9,1,[2,4]],
"いたいと": [3],
"そのものを": [[6,11]],
"んでいるかを": [9],
"うためには": [4],
"webster": [0,[7,9]],
"されず": [6],
"えてもかまいません": [6],
"全分節": [11,8],
"生成": [6,8,10,3,[5,9]],
"サブディレクトリ": [6],
"使用時": [4,7],
"cjk": [11],
"はまた": [6],
"などです": [[0,6,11]],
"であった": [6],
"文字列": [11,6,2],
"スクリプトウィンドウ": [8],
"はまず": [5],
"with": [5],
"説明": [6,[5,7,10,11]],
"pdf": [6,[7,8,11]],
"まったく": [6],
"させない": [2],
"自動更新": [5],
"用紙送": [2],
"フィールド": [[6,8,11]],
"がつかなくなった": [9],
"もそのまま": [10],
"empti": [5],
"とする": [[2,11]],
"そこから": [10,5],
"より": [[2,5,8]],
"りが": [8],
"toolsshowstatisticsmatchesmenuitem": [3],
"コンソールモード": [5],
"viewdisplaymodificationinfononeradiobuttonmenuitem": [3],
"蘭中翻訳": [6],
"出力形式": [6],
"詳細": [5,[6,8,11],[2,9,10]],
"実装": [5],
"全文": [9],
"するものが": [11],
"トラブルシューティング": [0,7],
"登場": [11],
"tmx": [6,10,5,[8,11],[3,9]],
"著作権": [8],
"境界正規表現": [[2,7]],
"右下角": [9],
"おめでとうございます": [11],
"ファイルフォルダー": [11,[4,6]],
"repo_for_all_omegat_team_project": [6],
"コンピューター": [5,4,[7,10]],
"無条件": [10],
"はじゅうぶんに": [10],
"するのはもちろんですが": [11],
"下線": [1,[4,9]],
"分割": [11],
"学習": [10],
"テキストエディタ": [1],
"つけた": [5],
"注意": [[6,11],5,[4,8,9,10]],
"ページ": [8,[5,6],3],
"intel": [5,7],
"後処理用外部": [11],
"検索機能": [11],
"mainmenushortcuts.properti": [3],
"するものと": [5],
"projectaccesswriteableglossarymenuitem": [3],
"選択": [8,11,5,9,3,[1,4,6]],
"かれている": [5],
"gui": [5,10],
"定期的": [6,10],
"cmd": [6],
"ると": [9],
"coach": [2],
"すこともできます": [11],
"regexp": [5],
"によってはそれ": [10],
"しておくこともできます": [[5,10]],
"するいかなる": [5],
"ロック": [5],
"しなおしたい": [9],
"sentencecasemenuitem": [3],
"gotohistorybackmenuitem": [3],
"重要": [[5,6,9,10]],
"れず": [3],
"権限": [5],
"語根": [8],
"ボタン": [11,4,5,9],
"りを": [[4,5,8]],
"通常": [5,11,8,[1,4,6,9,10]],
"するものを": [11],
"最短一致数量子": [2,7],
"uhhhh": [2],
"powerpc": [5],
"こりません": [9],
"になることがあります": [[8,9]],
"じになります": [6],
"選択領域": [8,[3,9]],
"optionssentsegmenuitem": [3],
"認証": [3],
"となっています": [6],
"カーソルセグメント": [8],
"つだけであることに": [6],
"経由": [5],
"かどうかを": [[8,10]],
"optionsaccessconfigdirmenuitem": [3],
"けるため": [6],
"わないような": [11],
"隣接": [9],
"エントリ": [[1,8]],
"容易": [11],
"うしかありません": [6],
"にはおなじみでしょうが": [11],
"加速": [6],
"test.html": [5],
"ろに": [2],
"xxx": [10],
"れる": [11],
"ましくない": [8],
"任意": [11,[8,9],[5,10],[1,2,3]],
"まれていない": [[6,11]],
"右上角": [9],
"smalltalk": [11],
"があるかを": [11],
"随時": [10],
"われることになります": [10],
"補完候補": [[3,8]],
"プロキシサーバー": [5],
"同名": [5],
"っていない": [8],
"連絡": [6],
"するだけです": [5],
"上記文書": [6],
"omegat.sourceforge.io": [5],
"pseudotranslatetmx": [5],
"原文文書": [6],
"することはできません": [[6,9]],
"がすでに": [1],
"セクション": [[5,6]],
"名前変更": [6],
"つきの": [6],
"レベル": [6],
"訳文": [6,8,11,9,3,10,[4,5],1],
"ツールメニュー": [3,7],
"はできないのです": [11],
"ディレクトリー": [6],
"のひとつにすぎません": [6],
"翻訳作業中": [10],
"構成": [6,[5,9]],
"数文字": [8],
"からでも": [10],
"わず": [9],
"訳文言語用": [4],
"ローカル・コピー": [6],
"translat": [5,[4,11]],
"直接開": [8],
"それをもう": [6],
"てられます": [5],
"技術文書": [3,2],
"チェックボックス": [11,4,[5,8]],
"こらないようにできるなら": [10],
"けるかもしれません": [6],
"じということになります": [9],
"除外構成例集": [11],
"既存": [6,[5,11],[1,9,10]],
"履歴補完": [8],
"はこのような": [6],
"たとえ": [11],
"するかどうかは": [4],
"たとえばその": [1],
"ルート": [6],
"有効化": [[3,9]],
"をひとつ": [6],
"自動補完": [3],
"メイン": [6],
"適用": [8,[10,11],[3,5,6]],
"ければ": [6],
"からなる": [1],
"してもかまいません": [5],
"それをまた": [6],
"docs_devel": [5],
"けになります": [6],
"tsv": [1],
"ありません": [4],
"わる": [[5,8,11]],
"unpack": [5],
"われ": [[6,8]],
"識別": [[10,11]],
"gnome": [5],
"原文分節中": [9],
"理由": [[4,10]],
"指向": [11],
"後述": [3],
"しているので": [6],
"コマンドライン": [5,6,7],
"とした": [6],
"として": [5,[6,11],9,3,4,[2,8]],
"じるとき": [8],
"参考訳文": [8,9,[3,10],6,11,7],
"にしておいてください": [9],
"encyclopedia": [0],
"既定": [6,[5,10,11]],
"後者": [[4,6]],
"にまだ": [8],
"目次": [7],
"定義済": [[2,5,7]],
"をお": [5],
"optionstagvalidationmenuitem": [3],
"させるために": [[3,6]],
"自動処理": [5,11],
"csv": [1,5],
"n.n_linux.tar.bz2": [5],
"pt_br": [4,5],
"a-z": [2],
"スタート": [5],
"ローカルプロジェクト": [6],
"このとき": [6],
"でわかる": [5],
"による": [[3,8],[5,6,9,11]],
"ときどき": [4],
"により": [[5,9]],
"耐性": [10],
"したり": [[2,3,9]],
"単語境界": [2],
"したら": [11,3],
"press": [3],
"極力抑": [5],
"置換": [11,8,3,9,7],
"だけの": [6],
"のとおりです": [8],
"リポジトリマッピング": [[6,11]],
"んだ": [[4,6,11]],
"最大": [5],
"dmicrosoft.api.client_secret": [5],
"javascript": [11],
"mediawiki": [11,[3,8]],
"input": [11],
"セミコロン": [6],
"んで": [11],
"見積": [11],
"基本訳文": [[3,8,9,11]],
"上記参照": [[5,6]],
"ヒント": [[3,4,6,9,11],7],
"大陸": [5],
"などはその": [6],
"わることがあるようです": [8],
"できると": [10],
"なくとも": [3],
"アイテム": [5,6],
"ctrl": [3,11,9,[6,8],1,[0,10]],
"なります": [5,[8,9]],
"させることもできます": [9],
"document": [5],
"しておきます": [5,10],
"していたりするはずの": [9],
"単語": [11,8,9,[1,2],[3,4],[5,10]],
"かれます": [8],
"しなければ": [11],
"必要": [6,5,[4,11],[1,10],[0,2,9],[3,8]],
"うだけでよいことになります": [5],
"追加": [6,11,5,[1,3,8,10],[4,9]],
"翻訳内容": [[8,9],6],
"found": [5],
"変更点": [5],
"するとすぐに": [8],
"破損": [6],
"のみならず": [6],
"デスクトップ": [5,11],
"バッチモード": [5],
"だけでなく": [[6,11]],
"げたような": [0],
"文書": [6,8,[0,3,5,7,11]],
"そのような": [6],
"場合": [5,8,6,11,10,9,4,1,2,3],
"resourc": [5],
"させたい": [11],
"りです": [[5,6,11],3],
"プロジェクトファイル": [9],
"セグメント・テキスト": [8],
"team": [6],
"りのすべての": [6],
"オペレーティングシステム": [10],
"回以上": [2],
"xx_yy": [6],
"docx": [6,[8,11]],
"えてください": [6],
"txt": [6,1,9],
"になっていない": [8],
"しておきましょう": [6],
"機能": [8,[6,11],[1,3,4]],
"googl": [5,11],
"うため": [11],
"蘭英": [6],
"してから": [8,[6,11]],
"認識": [1,[6,11]],
"だけを": [6],
"ができます": [[2,6,11]],
"徐々": [6],
"一致対象": [2],
"download.html": [5],
"ドロップダウンボタン": [11],
"つしか": [0],
"調整": [11],
"ローカリゼーションエンジニア": [6],
"非常": [6],
"みてください": [6],
"source": [7],
"通貨記号": [2],
"sourceforg": [3,5],
"trnsl": [5],
"viewdisplaymodificationinfoselectedradiobuttonmenuitem": [3],
"ヘッダー": [11,8],
"index.html": [5],
"omegat.tmx": [6],
"計算": [9],
"given": [7],
"編集領域": [9],
"とその": [2,[7,8]],
"editmultipledefault": [3],
"mozilla": [5],
"editfindinprojectmenuitem": [3],
"をします": [[5,6]],
"ここに": [[5,6,8]],
"それをより": [5],
"warn": [5],
"project_save.tmx.yyyymmddhhnn.bak": [6],
"technetwork": [5],
"翻訳可能": [11],
"つけやすくするために": [6],
"都合": [10],
"時点": [[5,6,10,11]],
"ここで": [[9,11],[5,10]],
"するように": [[5,6]],
"だけが": [11,[6,9]],
"しておく": [6,[3,5]],
"するような": [[5,6]],
"まかに": [11],
"正規表現": [2,7,11,5,[3,4]],
"語形変化": [9],
"左右": [6],
"設定時": [1],
"プロジェクトマッピング": [6],
"セキュリティ": [5],
"プロジェクト": [6,11,8,5,10,[3,9],1,4,0,7],
"されるような": [5],
"フィルタ": [6,5],
"挙動": [5,10],
"領域": [8],
"せです": [6],
"project.gettranslationinfo": [11],
"ファイル": [6,5,11,8,10,1,3,4,9,0,7],
"中止": [5],
"一部": [8,[6,10],[9,11]],
"現在": [8,9,11,[3,6,10],5,1],
"したい": [5,11,[6,10]],
"えたりできます": [11],
"start": [5,7],
"windows": [7],
"れるまでは": [[5,6]],
"典型的": [5],
"むために": [8],
"説明文": [3],
"equal": [5],
"セグメント": [11,[1,8,10]],
"n.n_windows.ex": [5],
"訳文分節内": [9],
"サポート": [5,[2,8,11]],
"リモート・リポジトリ": [6],
"コンピュータ": [8],
"ちですか": [5],
"optionsalwaysconfirmquitcheckboxmenuitem": [3],
"デフォルト・フィルタ": [6],
"しなくなった": [9],
"しない": [8,6,[3,4,11],[2,9]],
"program": [5],
"をそこに": [6],
"これには": [6,[4,5,8]],
"put": [10],
"ユーザー・インターフェース": [1],
"っていたとしても": [11],
"置換後": [11],
"することもできます": [5,9,11,[1,4,6]],
"パーセンテージ": [9],
"エンジン": [8,[2,7,11]],
"enter": [[3,8],[5,11]],
"現在行": [9],
"構成例": [11],
"られた": [5],
"しておきたいときに": [9],
"中段": [2],
"applic": [5],
"bidi": [6],
"projectteamnewmenuitem": [3],
"分節情報": [10],
"バージョン": [5,6,8,10],
"プライベート": [5],
"かもしれません": [4],
"がどこまで": [9],
"エクスポート": [6,10],
"該当": [8,[4,11]],
"することができます": [11,[5,6,9],4],
"n.n_mac.zip": [5],
"れておくのがよいでしょう": [11],
"omegt": [5],
"つのうちいずれかの": [5],
"進行状況": [10],
"んだら": [6],
"んだり": [11],
"ユーザーレベル": [11],
"国語": [11],
"大文字": [2,[3,8,11],5],
"まれると": [4],
"非単語境界": [2],
"リソースバンドル": [5],
"とすると": [5],
"omegat.jnlp": [5],
"されるのではなく": [11],
"普遍的": [9],
"チーム": [6,[3,5,7]],
"申請後": [5],
"ぶこともあります": [6],
"コマンドラインエディター": [5],
"n.n_windows_without_jre.ex": [5],
"pseudotranslatetyp": [5],
"予想": [[1,7]],
"なすべてを": [5],
"することです": [[5,6]],
"エラー": [6,[5,8]],
"翻訳自動反映": [11],
"代替言語": [6],
"ローカルリポジトリー": [6],
"べたように": [6],
"複数開": [11],
"個別": [[1,6,11]],
"となる": [11],
"間隔": [[6,8]],
"除外登録": [11],
"リポジトリー": [6],
"プロンプト": [5],
"dmicrosoft.api.client_id": [5],
"ましい": [10],
"づいた": [4],
"づいて": [[4,8]],
"ひとつは": [4],
"それらを": [6,5],
"config-fil": [5],
"アップロード": [8],
"ひとつの": [6],
"のものになります": [8],
"数字以外": [2],
"なすべての": [5],
"projectclosemenuitem": [3],
"viewmarknonuniquesegmentscheckboxmenuitem": [3],
"対応": [6,8,5,11,9,[0,2,4,10]],
"半角文字": [11],
"拡張機能": [11],
"きません": [10],
"すときに": [10],
"したすべての": [11,5],
"できるようになります": [5],
"範囲": [2],
"上側": [9],
"findinprojectreuselastwindow": [3],
"ったまま": [10],
"とされることもあります": [11],
"ランチャー": [5],
"じにしたいときは": [9],
"となるのは": [9],
"readme.txt": [6],
"それらの": [11,[2,6]],
"復帰": [8],
"要素": [6],
"languagetool": [8],
"console.println": [11],
"分節中": [6,9],
"可能性": [5,[2,6,11]],
"共有": [6,4],
"files.s": [11],
"条件": [[2,5]],
"利点": [5],
"exchang": [1],
"同時": [11,[6,8]],
"下側": [9],
"request": [5],
"のために": [11],
"メモリー": [5],
"することができ": [9],
"しても": [11,[5,9],6],
"わりません": [11],
"currseg": [11],
"じことを": [5],
"したときと": [11],
"げます": [5],
"しては": [[6,11]],
"訳文分節以外": [9],
"展開": [5,0],
"アイコンファイル": [5],
"わないようにしてください": [11],
"カーソル": [8,9,1],
"それまでの": [8],
"バッチファイル": [5],
"project_files_show_on_load": [11],
"内部翻訳": [9,[8,11]],
"カスタムタグ": [8],
"数字": [9,[2,6]],
"ltr": [6],
"選択中": [[3,8]],
"つだけ": [8],
"optionsexttmxmenuitem": [3],
"downloaded_file.tar.gz": [5],
"えるよう": [6],
"うことができます": [[4,5]],
"コマンドラインウィンドウ": [5],
"build": [5],
"があれば": [6],
"原文言語": [6,[9,11]],
"管理下": [6],
"きつづけます": [10],
"一致": [11,1,[2,8],3,[9,10],4],
"marketplac": [5],
"種類": [[6,8],10],
"検索結果": [11],
"とても": [11],
"になるか": [5],
"以降": [[1,5]],
"dhttp.proxyhost": [5],
"entries.s": [11],
"ソース": [6,5],
"翻訳者向": [9],
"のようになります": [5],
"グループ": [2],
"del": [9],
"各単語": [8],
"gotonextuntranslatedmenuitem": [3],
"path": [5],
"つあります": [5],
"上部": [9],
"のあいだで": [6],
"あらかじめ": [[5,6]],
"拡張子違": [0],
"再現": [[6,10]],
"いています": [[1,5]],
"関数": [11],
"書式設定": [6],
"制御": [[5,6]],
"語辞書": [4],
"操作": [[6,11]],
"改版": [10],
"画面出力": [5],
"一般": [9],
"次第": [1],
"個以上": [2,[3,11]],
"allsegments.tmx": [5],
"起動引数": [5],
"事態": [10],
"起動": [5,8,7],
"オープンソース": [[6,11]],
"優先度": [[8,10]],
"helpcontentsmenuitem": [3],
"サインアップ": [5],
"みするたびに": [6],
"下部": [[8,9,11]],
"omegat-org": [6],
"unicode": [7],
"欧州連合": [6],
"descript": [[3,5]],
"詳細設定": [11],
"プロトタイプ": [11],
"projectaccessdictionarymenuitem": [3],
"最大化": [9],
"結果": [[8,11],[2,5,9]],
"空白文字": [[2,3,8]],
"のどこかに": [4],
"optionsworkflowmenuitem": [3],
"または": [6,8,[5,11],[2,3],9,[0,1],[4,10]],
"releas": [6,3],
"あるいは": [5,6,[10,11]],
"解凍": [5],
"できます": [5,11,6,8,9,10,[0,3]],
"sparc": [5],
"projectrestartmenuitem": [3],
"しないと": [5],
"がそれです": [6],
"最上段": [2],
"ランタイム": [5],
"duden": [9],
"分節固有": [8],
"完了": [[6,9]],
"空訳文": [8,3],
"ソート": [9,8],
"代替": [8],
"かっている": [10],
"ぶという": [4],
"のままなのかどうかを": [10],
"あとで": [6],
"翻訳支援": [[6,7,10]],
"spotlight": [5],
"検討": [6],
"わせに": [5],
"していません": [6],
"わせて": [[5,6]],
"セットアッププログラム": [5],
"うときは": [6],
"判別": [4],
"参考": [10,5],
"dir": [5],
"切断": [6],
"にそれだけを": [6],
"うには": [5],
"subdir": [6],
"単純化": [6],
"人気": [11],
"何箇所": [11],
"するようになります": [[1,5]],
"でなければなりません": [[5,6]],
"同期": [6,[5,11]],
"じたとき": [6],
"ピリオド": [2],
"うときも": [6],
"されるほとんどの": [3],
"viewfilelistmenuitem": [3],
"ナビゲーションボタン": [5],
"において": [[5,8,11]],
"となるべく": [2],
"test": [5],
"をそれぞれ": [4],
"omegat": [5,6,[8,11],3,[7,10],4,1,[0,9],2],
"することにより": [8],
"forward-backward": [11],
"検証": [5,[3,6]],
"めてください": [9],
"休憩": [4],
"していれば": [[5,11]],
"をどうするかについては": [10],
"しませんが": [6],
"したかもしれない": [6],
"単純": [2,[1,4,11]],
"アイコン": [5,[7,9]],
"some": [6],
"表示位置": [9],
"うので": [11],
"console-align": [5],
"ms-dos": [5],
"二重": [5],
"projectopenrecentmenuitem": [3],
"けできるのは": [6],
"からなります": [9],
"くこともできます": [5],
"しないといった": [8],
"によっては": [11],
"わせを": [[0,3]],
"をかける": [11],
"キーワード": [11],
"ねられます": [5],
"うのが": [[10,11]],
"されると": [6],
"してもいいでしょう": [4],
"したならば": [5],
"わせる": [5],
"られていない": [1],
"editexportselectionmenuitem": [3],
"はすべて": [6],
"げられます": [10,6],
"und": [4],
"直接実行": [5],
"project_save.tmx.temporari": [6],
"下表左列": [9],
"別物": [11],
"home": [6,5],
"構文定義": [3],
"projectaccesstargetmenuitem": [3],
"editoverwritemachinetranslationmenuitem": [3],
"変換作業": [6],
"構成物": [3,8],
"テキスト": [8,9,6,11,4,[2,7,10]],
"良好": [11],
"ingreek": [2],
"底部": [9],
"いておくと": [6],
"それまで": [9],
"地域設定": [5],
"について": [6,[5,8],3],
"つからない": [11],
"指定例": [5],
"もることができます": [11],
"すこともあり": [11],
"インストール": [5,7,[0,8],9,[4,11]],
"es_es.aff": [4],
"ignor": [4],
"していき": [11],
"もしくはその": [0],
"構文": [2,11],
"顧客向": [6],
"については": [5,[2,8,10,11]],
"projectexitmenuitem": [3],
"参考訳文候補": [10],
"についての": [6,[9,11]],
"aligndir": [5],
"翻訳作業": [6,11,10,9],
"ります": [6,3,[1,5,10]],
"典型": [6],
"action": [8],
"text": [[2,5]],
"スクリプトファイル": [11],
"メインメニュー": [[3,9]],
"卓上": [9],
"editregisteruntranslatedmenuitem": [3],
"init": [6],
"についても": [11],
"python": [11],
"リスト": [11,[1,6,8]],
"es_mx.dic": [4],
"ロケール": [5],
"事前": [5,[6,11]],
"infix": [6],
"アドレス": [5],
"まれます": [10,6,[5,8],11],
"変更前後": [11],
"られます": [[1,5]],
"訳文言語": [[4,6],[5,9,11]],
"maco": [5,[1,8]],
"tarbal": [0],
"しましょう": [6],
"しなおす": [6],
"doc": [6],
"集計対象": [11],
"プロパティ": [8],
"している": [5,9,[0,2,6],11],
"しながら": [[6,9]],
"文字数": [[2,8],9],
"意味": [11],
"ネットワーク": [[5,6]],
"タイトル": [[8,11]],
"mac": [3,[5,6]],
"独立": [9],
"アクセサリ": [5],
"file": [11,6,5],
"優先": [5],
"アップグレード": [5,11],
"危険性": [6],
"わない": [11],
"一般設定": [1],
"までの": [10],
"までは": [[3,5]],
"であっても": [5,[1,6]],
"man": [5],
"状態": [8,9,10,11,6],
"map": [6],
"ここまでくれば": [5],
"全角": [11],
"でなければ": [6],
"ボックス": [11],
"ハードディスク": [[5,6,8]],
"における": [6,8,[9,11]],
"may": [11],
"こまめに": [6],
"わずすべて": [10],
"いやすい": [6],
"再適用": [6],
"リソース": [6,[5,11]],
"はそれを": [5],
"menu": [9],
"オフ": [11],
"がある": [8,5,[2,6],10,[4,11]],
"url": [6,[4,5,8,11]],
"今度作成": [6],
"ステミング": [1,[3,9]],
"分割表示": [9],
"構文的": [11],
"ライセンス": [[0,5,6,8]],
"のうち": [[1,9]],
"とほぼ": [11],
"uppercasemenuitem": [3],
"ローカル": [6,[5,8]],
"viewmarkuntranslatedsegmentscheckboxmenuitem": [3],
"クリック": [11,5,8,9,4,1,6],
"しいことと": [5],
"glossaries": [7],
"a-za-z": [2,11],
"していますが": [11],
"のうちの": [9],
"光学文字認識": [6],
"一番簡単": [5],
"賢明": [[4,6]],
"していますか": [0],
"平文": [1],
"していた": [8,4],
"使用": [6,5,8,9,11,[1,7],[4,10],[0,3],2],
"ダイアログボックス": [8,11],
"そのまま": [5],
"ともっとも": [6],
"作業内容": [5],
"オンライン": [4],
"オン": [8,[5,11]],
"サービス": [5,8],
"をしています": [1],
"omegat.jar": [5,[6,11]],
"source-pattern": [5],
"omegat.app": [5],
"usr": [5],
"ポルトガル": [5],
"になりません": [11],
"メキシコスペイン": [4],
"しておくと": [5],
"作業": [6,5,11],
"れています": [0],
"旧形式": [11],
"日付": [6],
"ローカル・ディレクトリー": [8],
"utf": [1],
"っていると": [8],
"グローバル": [11],
"仕様": [6],
"サーバー": [6,5],
"開始": [[6,11],[2,5]],
"のいずれでも": [10],
"警告": [5,[2,6]],
"にすると": [8,[5,6,10]],
"本来": [9],
"無効": [8],
"っているか": [4],
"true": [5],
"手修正": [11],
"dsl": [0],
"ホーム": [[0,1,2,3,4,5,6,8,9,10,11]],
"原文": [11,[6,8],[3,9],1,5,10],
"エディタペイン": [8],
"groovi": [11],
"提供": [5,[0,4]],
"母音": [2],
"のある": [5,6,11],
"n.n_windows_without_jre.zip": [5],
"されており": [8],
"med": [8],
"自動改行": [8],
"ショートカットキー": [11],
"編集": [11,8,9,[5,7],[1,3,6],10],
"en.wikipedia.org": [9],
"kmenueditor": [5],
"dtd": [5],
"ポップアップメニュー": [9],
"エラーメッセージ": [5,6],
"インポート": [6,[1,8,9,10]],
"基準": [[6,8]],
"しているかを": [4],
"訳文生成": [6],
"英語": [6,2,5],
"はじゅうぶんその": [6],
"するかについてのみ": [6],
"projectcompilemenuitem": [3],
"パブリッシュ": [6],
"ピンポイント": [11],
"console-transl": [5],
"キー": [3,5,11,[1,9]],
"ビルド": [5,7],
"えるという": [6],
"無効化": [1],
"アルゴリズム": [8,[3,11]],
"参照": [6,5,[2,9,10,11],[3,4],[0,8]],
"master": [6],
"kmenuedit": [5],
"とじゅうぶん": [10],
"だったら": [0],
"gotonextuniquemenuitem": [3],
"パイプ": [11],
"xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx": [5],
"リポジトリ": [6,8,5],
"writer": [6],
"rubi": [11],
"optionsviewoptionsmenuitem": [3],
"されます": [[8,11],6,9,10,5,1,4,3],
"区切": [1,5,[6,8,11]],
"無制限": [5],
"commit": [6],
"yyyi": [6],
"挿入": [8,3,9,11,[1,10]],
"project_stats_match.txt": [10],
"および": [6,[5,11],[0,1,8],[7,9]],
"行端寄": [6],
"しているなら": [2],
"分節化規則": [11,2,8,[3,6,10]],
"いていた": [9],
"dvd": [6],
"選択肢": [8,5],
"xmx2048m": [5],
"えています": [4],
"対訳集": [5],
"区別": [11,2,[5,10]],
"進値": [2],
"xxxxxxxxxxxxxxxx.xxxxxxxxxxxxxxxx.xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx": [5],
"作業方法": [6],
"スムーズ": [6],
"追加設定": [5],
"ひとつだけです": [6],
"user.languag": [5],
"regex": [2,7],
"文章": [6,[2,8,11]],
"meta": [3],
"keystrok": [3],
"だけです": [8,6],
"右寄": [6],
"アプリケーション・メニュー": [8],
"をいくつでも": [10],
"krunner": [5],
"libreoffic": [[4,6]],
"実行時": [5],
"されたりはしませんので": [11],
"とさずに": [11],
"いている": [8,11],
"配布": [5,6],
"確認用辞書": [4,7],
"付記": [9],
"きされますが": [5],
"めることができます": [3],
"配下": [11],
"したりできます": [3],
"ドロップ": [9,5,7],
"されていないものは": [9],
"のさらに": [1],
"対話式": [2],
"インストールフォルダ": [5],
"背景色": [10,8],
"翻訳中": [8,[9,10]],
"てられている": [3],
"しません": [5,8,11,9],
"はまったく": [10],
"viewdisplaysegmentsourcecheckboxmenuitem": [3],
"わったら": [[8,11]],
"editregisteremptymenuitem": [3],
"原文部分": [1],
"ibm": [5],
"サーバ": [6],
"両側": [6],
"りのいずれかに": [6],
"しています": [9,[5,6,8],4,[0,3,11]],
"open": [11],
"されていないすべての": [6],
"翻訳済": [11,9,8,3,10],
"www.oracle.com": [5],
"分節化規則集": [11],
"parsewis": [11],
"なるものです": [9],
"します": [[5,11],8,6,9,2,1,10,4,0,3],
"project": [5,11],
"xmx1024m": [5],
"取得": [5],
"マウス": [[8,9]],
"はすでに": [5],
"このあと": [6],
"してもよいでしょう": [6],
"理解": [6],
"マッピングリポジトリ": [6],
"されている": [8,5,[4,11],1],
"固有名詞": [9],
"にどのていど": [11],
"しました": [6],
"開始時": [10],
"分節化": [11,6],
"各種": [9],
"penalty-xxx": [10],
"gotonextsegmentmenuitem": [3],
"影響": [5,[6,8,9]],
"カスタマイズ": [3,7,[2,5,11]],
"子音": [2],
"いくつかの": [[1,5,9]],
"nnn.nnn.nnn.nnn": [5],
"のままであれば": [10],
"復元": [9,[3,6]],
"ではなく": [[4,5,8,9]],
"されていて": [6],
"翻訳単位": [[10,11]],
"しているはずの": [5],
"するよくある": [1,7],
"abort": [5],
"されるのは": [8,11],
"idx": [0],
"文字": [2,11,1,[5,7,8]],
"ではない": [[1,6,8,11]],
"をたどり": [8],
"非視覚的": [11],
"ペナルティ": [10],
"抽出": [0],
"大丈夫": [1],
"projectaccesscurrentsourcedocumentmenuitem": [3],
"linux": [5,[2,7]],
"以外": [2,[5,6,8]],
"じたり": [6],
"留意": [6],
"をいくつか": [[5,11]],
"されていく": [9],
"訳語": [[1,8]],
"われているものと": [4],
"存在": [11,[4,10],[8,9],[0,1,5],6],
"できない": [5],
"じでない": [11],
"セット": [1],
"いていても": [11],
"クレジット": [8],
"file.txt": [6],
"しますか": [0],
"になります": [11,[5,10],[4,9],[1,8]],
"一般的": [11,[6,8,9]],
"はいろいろあるでしょう": [10],
"作成後": [1],
"es-mx": [4],
"しますが": [[6,11]],
"十分": [11],
"単体": [2],
"ifo": [0],
"安全": [6],
"応答": [9],
"まれないことに": [6],
"行末記号": [2],
"くのに": [11],
"comment": [5],
"うかどうか": [5],
"分以内": [6],
"がかかります": [4],
"単位": [5],
"stem": [9],
"にのっとった": [11],
"双方向": [8,3],
"初期位置": [3],
"ローカルコピー": [8],
"関数型": [11],
"するものです": [5],
"基本的": [5,4],
"まれていますが": [6],
"準拠": [6],
"分節内": [[9,10]],
"らかなように": [9],
"合計": [11],
"履歴予測": [8],
"保存": [8,6,[5,11],1,3,10],
"テキストレイヤー": [6],
"ケース": [6,8],
"分類": [8],
"スクリプト": [11,[5,8],7],
"参加": [6],
"直後": [[2,9],[5,8]],
"ている": [6],
"optionsautocompleteautotextmenuitem": [3],
"書式": [6],
"できるように": [11],
"設定": [11,8,[5,6],4,3,[9,10],7,0,2],
"導入": [5],
"ながら": [6],
"候補単語": [8],
"されることになります": [11],
"指示": [5,[4,6]],
"zip": [5],
"リモートファイル": [[6,10]],
"それぞれについて": [8],
"製品名": [6],
"ることになります": [11],
"そこで": [5],
"原因": [[1,5]],
"動作": [5,[4,6]],
"そこに": [11,[9,10]],
"スクロール": [9],
"concis": [0],
"あまり": [5],
"されているはずです": [6],
"見当": [[0,9]],
"文単位": [11],
"word": [6,11],
"のままです": [10],
"がいくつか": [11],
"平均": [11],
"支障": [11],
"同一訳文": [[3,8]],
"定義": [3,8,11,[2,10]],
"同様": [6,8,11],
"前述": [6],
"前者": [6],
"直近": [[6,11]],
"スクリプトフォルダー": [8],
"viewmarknotedsegmentscheckboxmenuitem": [3],
"直接入力": [11],
"完全": [6,[5,8],[1,3,10]],
"してみましょう": [[0,6]],
"外部出力": [6],
"順序": [11,9],
"ソースフォルダ": [5],
"実用的": [5],
"スペースバー": [11],
"ほとんどの": [11],
"積極的": [8],
"太字": [9,[1,11]],
"ドラッグ": [9,5,7],
"vcs": [6],
"改善": [[8,11]],
"lingvo": [0],
"gotomatchsourceseg": [3],
"プロジェクトリーダー": [6],
"複数見": [11],
"プロジェクトフォルダー": [6,[10,11],[7,8,9]],
"保管": [6,8],
"optionssaveoptionsmenuitem": [3],
"電子版": [9],
"むことができます": [6,5],
"いいえ": [5],
"しのある": [9,8,3],
"runn": [11],
"ターミナルウィンドウ": [5],
"がこの": [[6,8]],
"stardict": [0],
"言語版": [5],
"omegat.l4j.ini": [5],
"互換": [5],
"階層": [5],
"表示設定": [6],
"名称": [5,[8,9]],
"検索": [11,8,2,3,[5,6,7]],
"つまり": [6,11,[9,10]],
"まることに": [5],
"標準": [[1,4,8,9,11]],
"じです": [11],
"pt_pt.aff": [4],
"語配列": [11],
"るには": [9],
"ペイン": [[1,9]],
"みします": [8],
"ドイツ": [11],
"箇所": [2,[6,11]],
"セル": [11],
"アーカイブ": [[0,6]],
"もあります": [11],
"html": [5],
"時間": [4],
"グレーアウト": [8],
"変換単位": [10],
"実体": [11],
"ソースセグメント": [9],
"フィードバック": [9],
"thunderbird": [4],
"したもの": [6],
"editselectfuzzy3menuitem": [3],
"マッピング": [6,11],
"けになる": [5],
"緑字": [9],
"させることもできますし": [4],
"翻訳総局": [8],
"artund": [4],
"したいとき": [6],
"fals": [[5,11]],
"わないために": [6,7],
"project.projectfil": [11],
"バックアップファイル": [6],
"ウェブサイト": [10],
"一方": [[0,11]],
"特定文書向": [6],
"プロジェクトマネージャ": [6],
"それでも": [6],
"一時的": [[6,10]],
"プロジェクトパッケージ": [8],
"外部変更": [8],
"はもちろん": [[4,9,11]],
"検索文字列": [11,8],
"しかもその": [6],
"しているため": [9],
"コンテキストメニュー": [1],
"矢印": [9],
"元分節": [[3,8]],
"実例": [2],
"www.ibm.com": [5],
"再起動": [3,8],
"てられた": [11],
"それには": [[5,11]],
"期間中": [6],
"実質的": [11],
"使用可能": [[5,6]],
"配置": [10,9,[1,5],[4,6]],
"shortcut": [3],
"つけられなくなるかもしれません": [6],
"タグ": [6,8,[3,5],[9,11]],
"ウインドウ": [3],
"pt_br.aff": [4],
"tmx2sourc": [6],
"記載": [[5,11]],
"フィルター": [11,6],
"lookup": [8],
"オプション": [8,11,5,[1,6,9,10]],
"ini": [5],
"じでも": [11],
"command": [[3,9],5],
"タブ": [1,[2,8,9]],
"n.n_without_jr": [5],
"できないことがあります": [5],
"されたかどうかを": [8],
"dhttp.proxyport": [5],
"があるかどうかが": [5],
"ぶことができます": [5],
"まれています": [5,[2,6,7,10,11]],
"判断": [11],
"viewmarkbidicheckboxmenuitem": [3],
"subrip": [5],
"招待": [6],
"共存": [5],
"できるほうが": [6],
"最上位": [10],
"にすでに": [11],
"利用": [[5,6,11],[0,3,4,9,10]],
"コマンドラインモード": [5],
"日本語": [5,6],
"混在": [6],
"検索履歴": [11],
"たらない": [9],
"にあるその": [9],
"によって": [8,6,[5,9],[3,10]],
"一箇所翻訳": [11],
"翻訳状況": [8,[3,10],[6,11]],
"サブメニュー": [5],
"raw": [6],
"version": [5],
"まれません": [6],
"現在表示": [8],
"選択的": [6],
"移動": [8,11,9,[3,7],5],
"みたい": [4],
"コマンドプロンプト": [5],
"てきます": [6],
"表記方向": [6],
"特殊": [6],
"がどれくらいあるかを": [11],
"aaa": [2],
"最低": [[9,10]],
"contemporari": [0],
"solari": [5],
"はなるべく": [11],
"専用形式": [10],
"がその": [[5,11]],
"ロード": [6,8],
"projecteditmenuitem": [3],
"のため": [4],
"これは": [[0,6,8,9,11],[4,5]],
"britannica": [0],
"行末": [[2,3]],
"特定": [6,[5,11]],
"られるものを": [11],
"メモリ": [6,[10,11],5,9,8,[2,7]],
"通常通": [5],
"例外訳文": [9,[3,8,11]],
"デフォルトエンコーディング": [1],
"由来": [8,3],
"wikipedia": [8],
"そもそもこのような": [10],
"論理演算子": [[2,7]],
"構文一覧": [2],
"リモートサーバー": [10],
"整形": [[6,11]],
"abc": [2],
"なしに": [11],
"スキャン": [6],
"これが": [5],
"たとえば": [6,11,4,5,2,[0,8,10],9],
"iceni": [6],
"けたいという": [5],
"翻訳途中": [11],
"されるため": [11],
"プレーンテキスト": [6],
"拡張": [[1,7]],
"選択履歴": [8,3],
"リモート": [8],
"最後": [8,[5,11],[3,9,10]],
"上級者": [[2,5]],
"いか": [6],
"バックスラッシュ": [2],
"いろいろあるやり": [6],
"iso": [1],
"台湾": [5],
"されない": [1],
"表現": [[9,11]],
"最大限": [11],
"するでしょう": [3],
"カレントフォルダー": [5],
"互換性": [5,11],
"あり": [2],
"付属": [0],
"ある": [6,10],
"ファーストクラス": [11],
"わかりません": [5],
"されています": [8,[6,11],[9,10]],
"するらしく": [5],
"ローカル・ファイル": [6],
"glossary.txt": [6,1],
"無料": [5],
"していきます": [6],
"ヘルプ": [8,[6,7]],
"dsun.java2d.noddraw": [5],
"走査": [11],
"翻訳者": [6,9,[10,11]],
"ａｂｃ": [11],
"いた": [11,9,[1,3,8,10]],
"があるかもしれません": [6,11],
"以上変更": [10],
"add": [4],
"れてください": [11],
"することはありません": [5],
"いて": [6,[3,11],[2,5]],
"いで": [0],
"じることを": [11],
"型付": [11],
"このような": [6,11],
"んでおく": [4],
"をそれぞれの": [6],
"x0b": [2],
"メニュー": [3,7,[5,8],11,10,[0,6]],
"チーム・プロジェクト": [6],
"同等": [0],
"そして": [[3,5,10]],
"ブラジルポルトガル": [4],
"引用": [2,7],
"数値": [10,[6,9,11]],
"いも": [11],
"http": [6,5,11],
"optionsautocompleteshowautomaticallyitem": [3],
"いま": [11],
"入力補完": [3,8],
"できなかった": [[4,11]],
"いる": [5],
"larouss": [9],
"うか": [4],
"いや": [10],
"マシン": [11],
"untar": [[0,5]],
"記述": [5,11,3],
"することになる": [4],
"冒頭": [5],
"filters.conf": [5],
"ギリシャ": [2],
"じですが": [6],
"projectsinglecompilemenuitem": [3],
"補助的": [10],
"最近": [[3,8]],
"文書内": [9],
"コマンド": [5,[8,11],9],
"ごとの": [8],
"依頼主": [10],
"していない": [8,[1,5]],
"うと": [5],
"翻訳者側": [6],
"大部分": [4],
"myfil": [6],
"変換精度": [11],
"ローカルフォルダ": [[6,11]],
"パターン": [6],
"キーイベント": [3],
"ごとに": [[8,11],6],
"分節番号": [9,8,3],
"水準": [6],
"引数": [5,6],
"解除": [11,[5,9]],
"大文字以外": [2],
"名称部分": [9],
"プロジェクトフォルダ": [1],
"ユーティリティ": [0],
"われます": [[6,11],[3,9]],
"clone": [6],
"とりわけ": [11],
"わせである": [5],
"収拾": [9],
"使用中": [5,[0,7]],
"family": [7],
"言語設定": [11,4],
"りません": [0],
"単語構成文字": [2],
"多種多様": [0],
"仕組": [5],
"翻訳後": [6],
"されなくなります": [5],
"をとってください": [5],
"properti": [5],
"想定": [5],
"editselectfuzzyprevmenuitem": [3],
"optionstabadvancecheckboxmenuitem": [3],
"最終的": [6],
"されていきます": [10],
"えた": [[5,6,11]],
"論外": [6],
"しなければなりません": [6],
"えて": [[4,6]],
"生成後": [11],
"するすべての": [11],
"heapwis": [11],
"optionsviewoptionsmenuloginitem": [3],
"確認": [4,6,8,5,[0,1,2,3,11],[7,9]],
"訂正候補": [4],
"自動挿入": [10,[6,8]],
"になったときに": [6],
"えは": [6],
"tar.bz2": [0],
"のようになっています": [3],
"れないでください": [11],
"解決策": [6],
"みです": [5],
"bundle.properti": [6],
"えば": [9,[5,8]],
"収録": [9],
"script": [11],
"オランダ": [6],
"スペイン": [4],
"spellcheck": [4],
"複数形": [1],
"x64": [5],
"ファイルフィルター": [11,8,3],
"リネーム": [6],
"翻訳作業全体": [6],
"修正": [11,3,[6,8],[5,9]],
"keyev": [3],
"そうではない": [10],
"える": [11,2,[5,6,7]],
"触発": [11],
"じます": [11,[8,9]],
"併記": [10],
"isn\'t": [2],
"わりに": [3,[6,8]],
"えを": [6],
"local": [6,5],
"できますが": [6,[1,9,11]],
"取扱": [6],
"いでしょう": [11],
"configuraiton.properti": [5],
"されていれば": [5],
"optionsteammenuitem": [3],
"gzip": [10],
"専用": [11,6,10],
"repo_for_all_omegat_team_project_sourc": [6],
"マップ": [6],
"表示状態": [9],
"置換用": [11],
"進捗": [9,7],
"esc": [11],
"結合前": [11],
"ダブルクリック": [5,[8,9,11]],
"x86": [5],
"かな": [11],
"またはすべて": [8],
"指定": [5,11,[3,6],[2,4,8],[0,10]],
"更新可能": [3],
"かつ": [[6,8]],
"かの": [6],
"es_mx.aff": [4],
"翻訳": [6,11,10,9,8,5,7,2,4],
"画面": [5,11],
"作業用翻訳": [10],
"文中": [11],
"終了": [8,6,3],
"いていたり": [9],
"上限": [11],
"console-createpseudotranslatetmx": [5],
"mode": [5],
"計算方法": [11],
"エスケープ": [2,5,1],
"るかどうかを": [5],
"longman": [0],
"から": [11,6,5,[9,10],4,[3,8],[1,7],2],
"java8jr": [5],
"下記参照": [5],
"toolsshowstatisticsstandardmenuitem": [3],
"all": [4],
"merriam": [0,[7,9]],
"これを": [6,5],
"きく": [5],
"alt": [[3,5,11]],
"不明": [[5,6]],
"これら": [6],
"projectname-omegat.tmx": [6],
"きし": [9],
"っているすべての": [6],
"デフォルト": [3,6,[1,8],5,[2,7,9,10,11]],
"するにあたって": [11],
"ローカルマッピング": [6],
"てることができます": [3],
"ファイルマネージャー": [4],
"てくる": [6],
"されません": [11,10,8,[1,6]],
"更新履歴": [[3,8]],
"いがある": [6],
"ファイルフィルタ": [[5,6,8,10]],
"左端": [11],
"変換工程": [6],
"しているうちに": [9],
"そのため": [[5,11],[4,8]],
"段落区切": [8],
"スペルチェック": [8],
"きの": [8],
"原文分節": [[8,10],9,11,6],
"位置": [8,[5,9],[1,6,11]],
"例外的": [11],
"全選択": [9],
"文字一覧": [3],
"アプリケーション": [5,6],
"リアルタイム": [9],
"n.n_without_jre.zip": [5],
"新規": [5,6],
"強制": [10],
"and": [[5,11]],
"直前": [8,5],
"くか": [6],
"システム": [5,6,[3,7]],
"環境": [5],
"くお": [6],
"magento": [5],
"えません": [[6,8,11]],
"たいていの": [5],
"からすべての": [11],
"手動": [[8,11],[4,6],1],
"しておいてください": [5],
"ant": [[6,11]],
"バグ": [8],
"をそのまま": [11],
"ここでの": [6],
"にあります": [11,5,6],
"くよう": [6],
"めったにないことですが": [11],
"何度": [[6,11]],
"くの": [[5,6]],
"u00a": [11],
"helplastchangesmenuitem": [3],
"くと": [[1,3,5,6,10]],
"omegat.ex": [5],
"ユーザーグループ": [6],
"ています": [11],
"けたほうが": [6],
"shift": [3,6,8,11,1],
"ログファイル": [8],
"複数存在": [[8,9,10]],
"パス": [5,6],
"てたい": [[3,5]],
"java": [5,3,11,2,[6,7]],
"端末": [5],
"english": [0],
"もありますし": [11],
"エンコーディング": [1,[7,11]],
"使用例": [2,7],
"jar": [5,6],
"api": [5],
"editselectfuzzy2menuitem": [3],
"project_save.tmx": [6,10,11],
"させる": [[6,10]],
"からも": [4],
"上図": [4,9],
"dictionari": [0,4],
"プロセス": [6],
"するのは": [6],
"確認機能": [4,[7,10]],
"コピー": [6,9,[4,8,11],5,10],
"上書": [5,[6,8,9,10]],
"閲覧": [5],
"バー": [5],
"からは": [11],
"からの": [5,[1,6],[7,9]],
"確実": [5],
"したがって": [[5,6,11]],
"プロジェクトパラメータ": [6],
"確定": [8,9],
"dictionary": [7],
"であったりする": [6],
"けは": [8],
"ることができます": [9,8],
"けの": [[2,3,9,11]],
"けに": [9,[3,4,5]],
"注記": [2,9],
"するので": [5],
"改行": [2],
"未翻訳分節": [11,8,3,[6,10]],
"クイック": [5],
"のどれかの": [11],
"下図": [9],
"便利": [5,2,[6,9]],
"翻訳前": [11],
"一行": [[3,10]],
"すでに": [[3,4,6,8,10]],
"投稿": [6],
"循環": [3],
"editselectfuzzynextmenuitem": [3],
"修正済": [10],
"いはわずかです": [11],
"プレーン・テキスト・ファイル": [6],
"強力": [11],
"意見": [9],
"ステータスバー": [9,[5,7]],
"ドット": [5],
"こみいった": [2],
"多言語法律文書": [6],
"文字列検索": [[2,11]],
"ヘルプメニュー": [3,7],
"readme.bak": [6],
"がるかもしれません": [6],
"結果的": [6],
"ける": [8],
"言語用": [4],
"art": [4],
"projectaccessrootmenuitem": [3],
"してください": [5,6,11,3,4,2,10,8,9,0],
"かれた": [9,[4,6,10]],
"dyandex.api.key": [5],
"してまわる": [9],
"ここ": [11],
"多言語": [6],
"けを": [8],
"ドック": [5],
"しようとしている": [9],
"えていたり": [5],
"rtl": [6],
"こそ": [10],
"不自然": [11],
"分節": [8,11,9,3,[6,10],1,5],
"jdk": [5],
"つのうちのどれかです": [10],
"プロジェクトツリー": [10],
"コンテキスト": [6],
"関連付": [8,6],
"つことができますが": [1],
"正規表現例": [[2,7]],
"用語": [1,9,3,[8,11]],
"機械翻訳": [8,9,[3,7]],
"この": [5,11,8,6,10,[2,9],[0,1,4,7]],
"末尾": [8,[2,6,10]],
"するには": [[5,11],[1,6],8,9],
"がついた": [6],
"初期値": [9],
"toolsshowstatisticsmatchesperfilemenuitem": [3],
"リリース": [8],
"もありません": [6],
"えかねないので": [6],
"editinsertsourcemenuitem": [3],
"にあり": [1],
"規則": [[5,11]],
"run": [11,[5,6]],
"パッケージ": [5,8],
"viterbi": [11],
"microsoft": [[5,6]],
"分節化設定": [11],
"われません": [[1,4]],
"にある": [5,11,[2,6,8,9],[1,4]],
"projectnewmenuitem": [3],
"記憶": [8],
"緑色背景": [9],
"ごと": [6],
"技術的": [8],
"optionstranstipsenablemenuitem": [3],
"左寄": [6],
"外部": [[6,11],[1,3,5]],
"オフライン": [6,5],
"titlecasemenuitem": [3],
"ｔｗ": [5],
"ファジーマッチ": [9],
"ルートフォルダー": [6],
"単独": [11],
"みには": [4],
"glossari": [1,6,[0,4,11]],
"まります": [6],
"単語数": [8],
"editcreateglossaryentrymenuitem": [3],
"クラウド": [6],
"言語間": [[6,11]],
"ignored_words.txt": [10],
"むすべての": [11],
"configuration.properti": [5],
"github.com": [6],
"紫色": [8],
"されたときも": [6],
"参照用": [1],
"わからない": [5],
"最下部": [11],
"アクセス": [3,[5,8,11],6],
"これにより": [10,[5,6,11]],
"さな": [[5,8]],
"コードスニペット": [11],
"れてから": [11],
"色付": [8,3],
"name": [5],
"自体": [2,[5,6]],
"たらなければ": [0],
"まれる": [11,[5,6]],
"string": [5],
"っておいたほうがよいでしょう": [4],
"個人設定": [[5,8]],
"プラットフォーム": [5,1],
"めないでしょう": [6],
"制御文字": [8,[2,3]],
"自動保存": [6,8],
"再読": [[8,11],[1,3,6]],
"しい": [6,5,8,[3,11],4,1,[2,10]],
"推奨": [11],
"段落間": [8],
"制限": [[5,6]],
"され": [9,5,[6,11],[1,8]],
"しか": [[4,8]],
"設定中": [4],
"not": [[5,11]],
"しが": [11],
"できません": [[8,9]],
"いずれの": [11],
"非単語文字": [2],
"配色": [3],
"ウィンドウタイトル": [11],
"用語集": [1,9,3,7,[8,10,11],6,[0,4]],
"するのか": [5],
"がない": [8,[1,5,9,10]],
"内容": [6,11,8,10,9,5,0],
"しく": [[6,8],5,1],
"全体": [8,[5,10]],
"してみてください": [[2,5,11]],
"可能": [1,[5,6],[3,8,10,11]],
"アドオン": [2],
"して": [6,5,11,8,9,4,[0,10]],
"まれた": [4],
"selection.txt": [8],
"した": [5,6,[8,11],9,4,[2,3,10],[0,1]],
"target": [8,[10,11],7],
"じく": [6],
"しの": [11],
"パネル": [5],
"いてもよいでしょう": [5],
"ローカルファイルマッピング": [6],
"われる": [8],
"しに": [11],
"堅牢": [6],
"データ": [6,7],
"くたびに": [6],
"無関係": [11],
"window": [5,[0,2,8]],
"config-dir": [5],
"接頭辞": [10],
"まれているだけの": [11],
"エディタ": [11,9],
"じた": [[1,8]],
"例示": [[5,9]],
"絵文字": [8],
"ちます": [11,5],
"じて": [6,[5,8,9,10,11]],
"disable-project-lock": [5],
"辞書": [4,0,9,[7,10],8,6,[1,3]],
"オートコンプリート": [1],
"omegat.pref": [11],
"termbas": [1],
"ペア": [6,9],
"わっていれば": [5],
"なのは": [5,10],
"普及": [11],
"最初": [11,[5,8],10,[1,6],[2,3,9]],
"すか": [8],
"通知": [6,5],
"分節間": [11],
"しを": [11,9,3],
"中国語": [6],
"そうすると": [10],
"いことが": [10],
"取扱説明書": [7,[6,8],[3,5]],
"にしています": [2],
"howto": [6],
"すと": [8,[6,9],[4,11]],
"公式": [7],
"んでいる": [6],
"pt_pt.dic": [4],
"されないようにする": [10],
"ベル": [2],
"ポップアップ": [11],
"じる": [[6,8,11],3],
"するよりも": [6],
"ウィンドウ": [9,11,8,6,[7,10],[1,4]],
"level1": [6],
"level2": [6],
"直接": [10],
"対訳形式": [0],
"アルファベット": [11,6],
"にあらかじめ": [9],
"置換件数": [11],
"aaabbb": [2],
"くために": [3],
"空白": [[2,11]],
"ずつ": [[10,11]],
"再利用": [6,7],
"きさを": [9],
"構築": [5],
"する": [6,5,11,8,9,10,1,4,7,2,0,3],
"ることはできます": [11],
"web": [5,[6,7]],
"edittagpaintermenuitem": [3],
"されていることを": [6],
"en-us_de_project": [6],
"簡単": [6,[4,10]],
"単一言語": [11],
"optionscolorsselectionmenuitem": [3],
"があるとします": [6],
"最良": [11],
"editselectfuzzy4menuitem": [3],
"editregisteridenticalmenuitem": [3],
"ここでいう": [11],
"えることによって": [5],
"にあるすべての": [6],
"キャリッジリターン": [2],
"そうすれば": [[4,10]],
"起動方法": [5],
"メモリファイル": [6,11],
"整形情報": [6,10],
"バイリンガル": [6],
"けたい": [6],
"unicod": [2],
"viewmarknbspcheckboxmenuitem": [3],
"更新日時": [8],
"らかの": [5],
"同上": [6],
"らかな": [10],
"具体例": [11],
"分節数": [11,9,8],
"補助用": [6],
"したとき": [6],
"pt_br.dic": [4],
"同一": [6,10,[4,5,11]],
"いません": [[5,8,11]],
"線上": [11],
"unabridg": [0],
"できるため": [6],
"するときは": [6],
"などを": [[5,6]],
"せをどうするかは": [6],
"するときと": [6],
"作業中": [[8,10]],
"である": [[1,8,11],[4,6]],
"翻訳作業向": [9],
"辞書内": [8],
"するときに": [[1,5,6]],
"はそのままです": [11],
"されていることが": [9],
"optionsglossaryexactmatchcheckboxmenuitem": [3],
"されたら": [5],
"とともに": [11],
"されたり": [11],
"小文字": [[3,11],2,[5,8]],
"チームプロジェクト": [6,8,7,[3,5,10,11]],
"効果的": [11],
"履歴": [8],
"があることが": [6],
"などは": [5],
"みされます": [11],
"一覧": [11,[7,8],3,[2,4,5,9]],
"があればその": [9],
"両方": [6,11,5],
"いたい": [4,[6,11]],
"nnnn": [9,5],
"などの": [[9,11],[0,5,6,8]],
"omegat.project": [6,5,10,[7,9]],
"前回保存": [6],
"その": [5,11,9,6,10,8,[0,4],[1,2,3,7]],
"などに": [[2,9]],
"excludedfold": [6],
"ではありません": [6,5],
"プロパティダイアログ": [1],
"があったとします": [10],
"未使用": [8,3],
"番号": [8,[5,9],1],
"関連性": [[3,8]],
"登場順": [11],
"リモートデスクトップ": [5],
"webstart": [5],
"それ": [[1,8,10]],
"かります": [9],
"語幹処理": [1,3],
"プログラムフォルダー": [5],
"以上": [[1,10]],
"myproject": [6],
"以下": [5,6,[2,11],[0,4,9,10]],
"えることができます": [6,5],
"zh_cn.tmx": [6],
"起動時": [5],
"はすぐに": [1],
"直線移動": [11],
"ハイライト": [8,9],
"しかもそれぞれの": [10],
"形式": [6,1,8,11,0,[5,9,10],[3,7]],
"ソース・ファイル": [6],
"候補": [[8,10]],
"圧縮": [[0,10]],
"孤立": [[6,9]],
"非空白文字": [2],
"にすべての": [11],
"整形用": [11],
"セットアップ": [6],
"みしてください": [11],
"いたり": [6],
"いておくことができます": [[10,11]],
"yandex": [5],
"申請": [5],
"だけ": [10],
"だけから": [11],
"メタ": [2],
"archiv": [5],
"反映": [10,[3,6,8]],
"変換": [8,6],
"repo_for_omegat_team_project.git": [6],
"user": [5],
"a123456789b123456789c123456789d12345678": [5],
"こうとすると": [5],
"サブフォルダー": [0],
"viewmarkwhitespacecheckboxmenuitem": [3],
"ワイルドカード": [11,6],
"のみの": [5],
"プライマリ": [5],
"効果": [5],
"記録": [10,[8,11]],
"すことをおすすめします": [6],
"ソースコード": [5,7],
"のみで": [[5,11]],
"bak": [6,10],
"集計": [8],
"されていることから": [11],
"階層構造": [10],
"新規作成": [[3,8]],
"bat": [5],
"のまま": [11],
"登録": [[5,8],[3,11],9],
"jre": [5],
"のみが": [11,6,[1,8]],
"optionsfontselectionmenuitem": [3],
"にして": [5],
"されたなら": [6],
"メモ": [[8,9],3,11,7],
"されるようにます": [11],
"にした": [[5,6]],
"プログラム": [[5,6],4,11,8],
"めたいという": [6],
"an": [2],
"editmultiplealtern": [3],
"まりとして": [9],
"git.code.sf.net": [5],
"キーボード": [9,[3,11]],
"マッピングパラメータ": [6],
"依頼": [6],
"するその": [[6,9]],
"ノーブレークスペース": [8,[3,11]],
"対象外": [[5,11]],
"リンク": [[5,10]],
"各分節": [8],
"黄色": [[8,9]],
"否定": [2],
"できる": [[6,11],5,[3,7,10]],
"be": [11],
"freebsd": [2],
"フォルダ": [5,6,[1,8,10],3,[0,9]],
"にする": [11,[3,4,8]],
"アクション": [8],
"のもう": [11],
"filters.xml": [6,[10,11]],
"自動的": [8,[5,6],[9,11],[1,3]],
"れないと": [11],
"br": [5],
"projectaccessglossarymenuitem": [3],
"であることに": [5],
"search": [2],
"であるのに": [4],
"検出": [[1,4,5,8]],
"ポート": [5],
"java8": [5],
"がまだ": [11],
"特徴": [11],
"一度翻訳": [6],
"になることもあるでしょう": [11],
"フリー": [4],
"のみを": [5,[8,11]],
"ユーザーインターフェース": [5],
"segmentation.conf": [6,[5,10,11]],
"った": [11,9],
"格納": [[4,9,11]],
"アカウント": [5],
"アクティブ": [8],
"するわけではありません": [6],
"developerwork": [5],
"cd": [5,6],
"条件欄": [11],
"ce": [5],
"するため": [6,8],
"öäüqwß": [11],
"set": [5],
"プロジェクトメニュー": [3,7],
"って": [11,[4,5],[0,6,9]],
"きたい": [9],
"cn": [5],
"いたとき": [6],
"文脈": [9,[8,11]],
"optionsrestoreguimenuitem": [3],
"cr": [2],
"けられている": [8],
"独自": [[2,11]],
"すもので": [9],
"スコア": [11],
"様子": [0,7],
"cx": [2],
"バックアップ": [[6,10]],
"構文例": [2],
"メインウィンドウ": [9,7],
"apach": [[4,6]],
"がいったん": [6],
"yyyymmddhhnn": [6],
"dd": [6],
"つに": [6],
"つもない": [1],
"クリップボード": [8],
"terminolog": [8],
"をなるべく": [6],
"づけ": [8,3,9],
"管理": [6,10],
"f1": [3],
"各々": [5],
"つの": [6,5,11,9,[4,8],1,[0,2,10]],
"f2": [9,[5,11]],
"f3": [[3,8]],
"つは": [9],
"f5": [3],
"スタイル": [[6,11]],
"タイミング": [10],
"さない": [11],
"repositories": [7],
"dz": [0],
"することによって": [5],
"projectsavemenuitem": [3],
"editundomenuitem": [3],
"xmx6g": [5],
"無視": [9,[3,4,5,8,10,11]],
"いても": [5],
"ったものを": [9],
"複数起動": [5],
"u000a": [2],
"していないものがありますが": [0],
"げられて": [10],
"手間": [6],
"既定値": [8,9],
"実用上": [9],
"つを": [9],
"en": [5],
"u000d": [2],
"u000c": [2],
"があるはずです": [0],
"eu": [8],
"つまたは": [[1,11]],
"みしていない": [1],
"収集": [6],
"水色": [8],
"注意事項": [[9,11]],
"チェックアウト": [6],
"でき": [9],
"フラグ": [[2,7,11]],
"u001b": [2],
"stats.txt": [10],
"翻訳会社": [9],
"共通": [11],
"てて": [5],
"exclud": [6],
"for": [11],
"fr": [5,4],
"再度開": [6],
"現在作業中": [3],
"記号": [5,1],
"content": [5],
"です": [6,[5,11],[8,9],10,1,4,2,[0,3,7]],
"applescript": [5],
"問題": [8,[1,6],[4,5,7]],
"はその": [6,1],
"gb": [5],
"有効": [8,5,1,[2,4,9,10,11]],
"のみです": [11],
"スペース": [11,[2,8],[1,6]],
"てはまるものです": [11],
"終了時": [[3,8,10]],
"helplogmenuitem": [3],
"での": [6,[2,9,11]],
"とが": [10],
"ディストリビューション": [5],
"きする": [6],
"オペレーティング・システム": [8],
"editoverwritetranslationmenuitem": [3],
"みます": [11,5],
"してはいませんが": [6],
"ディレクトリ": [8],
"aeiou": [2],
"では": [[5,6],11,9,8,[2,4],[0,3,10]],
"現在分節": [9,6],
"になっています": [11],
"日時": [11],
"させるか": [10],
"form": [5],
"現在編集中": [9],
"ホームフォルダー": [5],
"けであったり": [6],
"でも": [11,[5,6],[4,8,9,10]],
"hh": [6],
"との": [[9,11]],
"ですが": [9],
"とは": [11,9],
"するとか": [4],
"場所設定": [11],
"duser.languag": [5],
"させるには": [6],
"されていません": [6],
"カーソルロック": [9],
"れなくなり": [11],
"されていない": [[5,8],1],
"bis": [2],
"欠落": [[2,8]],
"コミット": [6,8],
"えておくべきでしょう": [6],
"projectopenmenuitem": [3],
"autom": [5],
"定型文": [3],
"があるのは": [6],
"上記以外": [6],
"語句": [11],
"整合": [11,8,5,7],
"context": [9],
"https": [6,5,9],
"id": [[5,6,11]],
"ない": [[2,6]],
"とも": [[5,6]],
"空行": [3],
"if": [11],
"project_stats.txt": [11],
"ocr": [[6,11]],
"規則集": [11],
"projectaccesscurrenttargetdocumentmenuitem": [3],
"toolsvalidatetagsmenuitem": [3],
"なく": [[1,6]],
"どの": [5,[4,9]],
"されているかどうかによって": [5],
"in": [11,10],
"ip": [5],
"にだけ": [11],
"is": [2],
"なし": [5],
"とを": [5],
"ウェブページ": [11],
"更新": [[8,11],1],
"odf": [6],
"odg": [6],
"ということがあらかじめはっきりと": [10],
"ja": [5],
"詳細情報": [5],
"など": [11,[6,10],[5,9]],
"過去": [6,9],
"容量": [5],
"けるようにしたければ": [3],
"すほうが": [4],
"odt": [6,11],
"gotonexttranslatedmenuitem": [3],
"viewmarktranslatedsegmentscheckboxmenuitem": [3],
"valu": [11,5],
"によってはいくつかの": [5],
"フォント": [8,1],
"jp": [5],
"上記": [5,6,[9,10],[0,8,11]],
"js": [11],
"ilia": [5],
"自分": [6],
"としていますが": [6],
"らかさないように": [11],
"削除": [11,6,[5,10],4,[8,9],3],
"learned_words.txt": [10],
"オブジェクト": [11],
"プライバシー": [5],
"えます": [8,11,6,[2,5]],
"目的": [6,[5,8,11]],
"都度": [11],
"複数": [11,5,[6,8],[1,10]],
"macos": [7],
"ftl": [5],
"には": [11,5,9,6,10,3,[0,8],1,4],
"クラス": [2,7],
"下記": [[5,6,9,11]],
"ログ": [[3,8]],
"editselectfuzzy1menuitem": [3],
"ってください": [[5,6]],
"なる": [8,[6,9],[10,11]],
"viewdisplaymodificationinfoallradiobuttonmenuitem": [3],
"draw": [6],
"ベース": [11],
"れている": [3],
"サイレントオプション": [5],
"にも": [11,[2,8,9,10]],
"修復": [6],
"頻繁": [6],
"観点": [9],
"入力用翻訳": [6],
"lf": [2],
"dswing.aatext": [5],
"していないことに": [4],
"下訳": [6],
"auto": [10,8,6,3],
"lu": [2],
"入手": [3,[4,5,6]],
"フォルダー": [5,10,11,6,8,4,0,[1,3,9]],
"みなすことができます": [6],
"プレースホルダー": [6],
"cycleswitchcasemenuitem": [3],
"きやすい": [6],
"mb": [5],
"oracl": [5,3],
"me": [6],
"商標": [9],
"たいてい": [2],
"れます": [[4,8]],
"はさておき": [4],
"をすべて": [11],
"すべて": [3,9,[4,6,8,11]],
"omegat.png": [5],
"ねて": [9],
"をかけることができます": [11],
"gradlew": [5],
"mm": [6],
"のもの": [6],
"entri": [11],
"これによって": [9],
"比較": [11],
"操作方法": [6,[0,7,10]],
"作業者": [8],
"相関": [11],
"新機能": [8],
"mt": [10],
"文書情報": [6],
"本当": [8],
"ツリー": [10],
"my": [6,5],
"リモートリポジトリ": [6],
"はい": [5],
"じてもよいかどうか": [8],
"つかった": [[9,10,11],6],
"にならなくなることがあるので": [11],
"nl": [6],
"辞書機能": [0],
"nn": [6],
"でしか": [11],
"できるのは": [0],
"表示": [8,11,9,3,[5,6],1,10,[4,7],2],
"一番望": [10],
"されるまでしばらく": [4],
"やはり": [5],
"code": [5],
"余分": [2],
"をかけ": [11],
"gotohistoryforwardmenuitem": [3],
"大切": [6],
"プロキシ": [[3,5]],
"衝突": [[3,8]],
"字幕": [5],
"of": [0],
"のみ": [[3,8]],
"契約": [5],
"ショートカット": [3,5,8,[7,11],[2,9]],
"入力": [11,5,6,8,9,2,10],
"ok": [[5,8]],
"優先的": [10],
"干渉": [11],
"ハイフン": [5],
"os": [5,1,[6,8]],
"であれば": [[6,8],5,[4,10,11]],
"見映": [6],
"src": [6],
"にその": [[6,9,11]],
"検索方法": [11],
"ラジオボタン": [11],
"control": [3],
"日本": [5],
"発生": [5,6],
"no-team": [[5,6]],
"参照翻訳": [6],
"editinserttranslationmenuitem": [3],
"実施": [[5,6]],
"ホスト": [5],
"プロキシサーバ": [5],
"オレンジ": [8],
"現在使用中": [8],
"一番書": [6],
"続行": [11],
"対訳": [9,8,5,6],
"po": [[5,9]],
"メガバイト": [5],
"optionsglossarystemmingcheckboxmenuitem": [3],
"pt": [5],
"部分的": [8,9],
"部分": [9,[1,6,8,11]],
"optionsautocompleteglossarymenuitem": [3],
"特典": [5],
"ローカルファイル": [6],
"それを": [[5,6]],
"ストレージ": [6],
"蓄積": [9,6],
"ばれ": [5],
"したものや": [9],
"あくまで": [11],
"更新情報": [3,8],
"コメントウィンドウ": [9],
"形態": [10],
"にするには": [[4,5]],
"edit": [8],
"たくさんあります": [2],
"editselectfuzzy5menuitem": [3],
"実行中": [[5,8,9]],
"いほど": [11],
"kde": [5],
"方法": [5,6,11,4,0,[7,10]],
"したものの": [1],
"をこの": [9,5],
"rc": [5],
"もっとも": [[4,5,9]],
"一覧表示": [8],
"includ": [6,11],
"順番": [[8,10,11]],
"していることが": [9],
"万字": [5],
"てます": [5],
"自動": [8,4],
"作成": [6,5,8,11,10,[1,4],2],
"したものと": [6],
"同僚": [9],
"動的": [11],
"メッセージ": [[5,8],9],
"としたい": [2],
"環境設定": [8,[3,5,6]],
"とほとんど": [2],
"にわずかに": [9],
"になっているか": [[6,9]],
"既知": [9],
"sc": [2],
"という": [5,10,[1,3,6,8,11]],
"生成時": [11],
"ドキュメント": [8],
"視覚的": [8],
"括弧内": [11],
"ターゲットファイル": [8],
"編集画面上": [11],
"青字": [9],
"わないためには": [6],
"されているかを": [6],
"文字数制限": [9],
"翻訳時": [6],
"訳文分節中": [8],
"key": [5,11],
"いったん": [[5,11]],
"現在開": [8],
"います": [9,[0,2,3,4,5,8]],
"svg": [5],
"になっている": [11,8],
"両方向": [6],
"はいくつも": [11],
"最小化": [9],
"づけられていることを": [5],
"svn": [6,10],
"各自": [6],
"確保": [5],
"決定": [10],
"editoverwritesourcemenuitem": [3],
"関係": [[1,9,11]],
"enforc": [10],
"れると": [11,10],
"させます": [11],
"中国": [5],
"tm": [10,6,8,[5,7,9,11]],
"厳密": [6],
"to": [5,[4,11]],
"日常的": [5],
"v2": [5],
"にない": [1],
"editreplaceinprojectmenuitem": [3],
"するたびに": [6],
"最長一致数量子": [2,7],
"でこれらの": [5],
"ステップ": [6,10],
"インターネット": [4,11],
"フォーカス": [11],
"同意": [5],
"全角文字": [11],
"express": [2],
"省略": [5],
"viewmarkautopopulatedcheckboxmenuitem": [3],
"projectwikiimportmenuitem": [3],
"ui": [6],
"リクエスト": [8],
"までのすべての": [2],
"小文字設定": [3,8],
"文章中": [6],
"接続": [[4,6]],
"されることがあります": [11],
"すべてを": [11],
"複数単語": [1],
"gotoprevioussegmentmenuitem": [3],
"どちらの": [6],
"表記": [6,7],
"じように": [11,9,[2,8]],
"テキストファイル": [[1,6,8]],
"しやすいように": [11],
"this": [2],
"gotopreviousnotemenuitem": [3],
"リモート・ロケーション": [6],
"editredomenuitem": [3],
"解決法": [6],
"uilayout.xml": [10],
"適切": [5,6,4,1,10],
"をとっておきましょう": [6],
"しているものです": [2],
"メモリフォルダー": [6],
"vi": [5],
"再度": [8],
"すべてに": [10],
"ぶと": [8,10],
"プレーンテキストファイル": [6],
"ターゲット": [1],
"くことができます": [11,5,8],
"最新": [6,5],
"いてください": [10],
"すべての": [11,5,6,8,[3,9]],
"がしっくりこない": [9],
"特別": [11],
"くなることがあるので": [6],
"図面": [6],
"それに": [[6,9]],
"テスト": [2],
"一致数": [9],
"番目": [9,1,[5,8]],
"マルチパラダイム": [11],
"緑色": [8],
"保持": [[10,11]],
"するかを": [5],
"っている": [[5,8,10,11]],
"ソースファイル": [6,8],
"関連": [5,6],
"実際": [6,[0,5,11]],
"字体": [8,3],
"見直": [10],
"groovy.codehaus.org": [11],
"repo_for_omegat_team_project": [6],
"物理的": [4],
"直接取得": [5],
"最近使用": [[3,8]],
"上段": [11],
"でその": [6],
"emac": [5],
"けるようになります": [5],
"org": [6],
"解決": [6,1],
"それが": [[10,11]],
"合致": [11],
"distribut": [5],
"処理": [5,6,9,3],
"マニュアル": [5],
"段落単位": [11],
"xf": [5],
"への": [5,6,8,[1,4,11]],
"してかまいません": [6],
"xi": [11],
"ダイアログ": [11,8,[4,10],[6,7,9]],
"けします": [8],
"リモート・ファイル": [6],
"テクノロジー": [5],
"があったとしても": [5],
"にしてください": [4],
"辞書項目": [8],
"すもの": [2],
"のような": [5,[6,11],[9,10]],
"のように": [5,[3,6,9,11]],
"っていて": [5],
"不正": [5,6],
"変更": [6,11,5,10,[4,8,9],1,3],
"べた": [5],
"xx": [5],
"xy": [2],
"sourc": [6,10,11,5,8,9],
"下段": [[2,11]],
"そこからの": [10],
"すために": [6],
"それらはすべて": [3],
"tester": [2,7],
"つことに": [11],
"type": [6,3],
"のいずれかです": [6],
"またはその": [6],
"らすために": [6],
"toolssinglevalidatetagsmenuitem": [3],
"モード": [6,[5,11],9],
"対策": [6],
"をするのであれば": [4],
"ったら": [3],
"翻訳入力行": [9,[3,10]],
"手順": [[6,11],5,[0,4,7,8]],
"けます": [8],
"していなかった": [8],
"projectaccesssourcemenuitem": [3],
"クライアント": [6,[5,10]],
"除外": [6,11],
"yy": [9],
"はありません": [5,[1,4,6]],
"にしたがって": [[2,5,6]],
"nbsp": [11],
"すればよいのです": [6],
"命令型": [11],
"段落": [6],
"一致率": [[9,10],8,3,6],
"gotosegmentmenuitem": [3],
"しないときに": [9],
"があります": [[5,6],4,[1,10,11],0,[3,8,9]],
"背景": [8],
"push": [6],
"わせてください": [6],
"めする": [6],
"用語集一致": [1],
"これらを": [6],
"readme_tr.txt": [6],
"べる": [5],
"外部検索": [[8,11]],
"どこであってもかまいません": [5],
"penalti": [10],
"分節化規則設定": [6],
"されないことに": [11],
"まれている": [[1,6]],
"定義構文": [3],
"xx_yy.tmx": [6],
"してしまう": [6],
"サブスクライブ": [5],
"としては": [5],
"固有": [[8,10],11],
"左側": [[8,11]],
"utf8": [1,8],
"ほど": [4],
"helpaboutmenuitem": [3],
"対応済": [6],
"されないようにすることができます": [11],
"付録": [[1,2,4],[0,3],6],
"依存": [1],
"直接編集": [6],
"限定": [11],
"にします": [8,[2,5]],
"regular": [2],
"tag-valid": [5],
"辞書用": [4],
"プロジェクト・マネージャー": [6],
"提示": [9],
"複数訳文": [9,7],
"テキストエディター": [5],
"えられます": [[5,6]],
"こりうる": [6],
"イベント": [3],
"コンソール": [5],
"フォーマット": [[1,6],7],
"でなく": [2],
"訳文分節": [8,11,6],
"文字列長": [5],
"u0009": [2],
"xhh": [2],
"訳語項目": [8],
"でない": [6],
"revis": [0],
"u0007": [2],
"よりも": [[5,8,10]],
"repositori": [6,10],
"えることが": [6],
"をその": [8],
"検索対象": [11,[2,9]],
"するが": [9],
"するか": [6,[5,11],4],
"翻訳対象": [11,8,[3,7,9,10]],
"lowercasemenuitem": [3],
"wiki": [[0,9]],
"firefox": [4,[2,11]],
"けるには": [1],
"ツール": [11,[2,8],6,7,10,[3,5]],
"照合": [8],
"されるはずです": [3],
"ブロック": [2,7],
"tab": [3,[1,8],9],
"taa": [8],
"できるという": [5],
"コンテンツ": [5],
"まず": [[4,6],5],
"項目": [3,8,1,11],
"コメント": [9,[1,11],[3,5,7,8]],
"tar": [5],
"まだ": [8],
"われている": [[9,11]],
"ポップアップメニューアイコン": [8],
"また": [11,[9,10],[5,6],[3,4,8]],
"再整合保留中": [11],
"完全一致検索": [11,1],
"カテゴリ": [2,7],
"projectreloadmenuitem": [3],
"まで": [11],
"独立型": [5],
"クロスプラットフォーム": [5],
"特化": [[9,11]],
"ブラジル": [5],
"構造": [10],
"safe": [11],
"作成時": [11],
"openoffic": [4,6],
"パスワード": [6],
"カラム": [1],
"修飾子": [3],
"プログラミング": [11],
"リードミー": [5],
"通訳者側": [6],
"印刷物": [9],
"optionsautocompletechartablemenuitem": [3],
"空欄": [9],
"すれば": [[4,5,6,8]],
"くときに": [6],
"にのみ": [11,[6,8]],
"みの": [11,[4,5,9],[2,7,8,10]],
"しようとすると": [6],
"みに": [5],
"winrar": [0],
"tbx": [1,3],
"まる": [2,3],
"いもの": [9],
"仮想": [11],
"みで": [5],
"許可": [[5,8]],
"ルートフォルダ": [3],
"未翻訳": [[8,11],9],
"git": [6,[5,10]],
"でしょう": [10],
"があるため": [11],
"正規表現関連": [2,7],
"することになります": [[6,11]],
"ウィキペディア": [8],
"ましいでしょう": [6],
"したいか": [6],
"duser.countri": [5],
"readm": [5],
"用途": [9],
"をすでに": [5],
"翻訳情報": [6],
"ばして": [8],
"びまで": [11],
"これより": [5],
"名前": [6,5,4,[1,10],[0,3,9,11]],
"出力": [6,[5,8],11,3],
"がれます": [[5,11]],
"これらは": [6],
"すると": [11,5,9,[6,8],[1,4,10]],
"これらの": [8,10,[4,11]],
"多少下": [6],
"むと": [11],
"先頭": [8,11,[2,3,5,9,10]],
"optionsspellcheckmenuitem": [3],
"えてしまった": [6],
"びます": [5,11],
"align.tmx": [5],
"がすべて": [[9,10]],
"めた": [5],
"file2": [6],
"optionssetupfilefiltersmenuitem": [3],
"ってから": [4],
"コード": [3,4,6,5],
"評価": [11]
};
