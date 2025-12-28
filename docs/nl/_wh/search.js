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
 "Appendices",
 "Voorkeuren",
 "How-to…",
 "Introductie voor OmegaT",
 "Menu&#39;s",
 "Vensters",
 "Projectmap",
 "Vensters en dialoogvensters",
 "OmegaT 6.0.2 - Gebruikershandleiding"
];
wh.search_wordMap= {
"upload": [4],
"eindigend": [0],
"beschrijfbar": [0,[4,6],[2,5],7],
"hoogst": [5,[1,3,7]],
"instelt": [3],
"licentie-informati": [4],
"stuurt": [2],
"totaal": [5],
"ten": [[2,6],5],
"instel": [[0,5],[1,3,7]],
"automatisch": [7,2,[1,6],4,0,3,5],
"zwak": [7],
"ter": [[0,2,6]],
"info.plist": [2],
"weergav": [5,[1,7],[0,3],4,6],
"lege": [2,0,[1,3,4,5,6]],
"fuzzi": [1,4,5,7,3,2,6],
"licht": [0],
"auto-aanvullen": [0,1,3,[4,5,8]],
"size": [2],
"left": [0],
"verandert": [0],
"tmx-en": [1],
"object": [7],
"mogelijke": [8],
"algoritm": [7],
"cel": [7],
"onderzijd": [7,5,1,4],
"authenticati": [2,1,5],
"edittagnextmissedmenuitem": [0],
"geval": [2,0,3],
"mogen": [0,[1,2,7],4],
"quiet": [2],
"invoegt": [[2,5]],
"projectstatistieken": [2],
"laden": [2,[0,3,7]],
"omhoog": [0],
"opdrachtprompt": [2],
"the": [0,[2,7]],
"halv": [7],
"extensies": [8],
"reflecteert": [[3,4]],
"projectimportmenuitem": [0],
"deselect": [7,1],
"gesproken": [2,[0,7]],
"imag": [0],
"converteert": [0,[2,4,6]],
"rond": [1],
"kijken": [[3,5]],
"tag-vrij": [7,3],
"doorgaat": [2],
"interacteerden": [2],
"moodlephp": [2],
"currsegment.getsrctext": [7],
"gerangschikt": [0],
"meegeleverd": [1],
"cijferig": [2],
"doorgaan": [[4,7]],
"export": [2],
"practic": [7],
"projectbestand": [5],
"termijn": [4],
"houdt": [[1,6,7]],
"aangevinkt": [4],
"gedeeltelijk": [2],
"gotonotespanelmenuitem": [0],
"fr-fr": [3,1],
"pictogrammen": [5,8],
"taalpatronen": [[0,1]],
"enkel": [0,[2,7],3,[1,6]],
"onderscheid": [[3,4]],
"pijl": [7],
"verschil": [[0,2,4,7]],
"root": [0],
"hard": [0,2],
"eerder": [3,0,[2,4],6,[5,7]],
"voldoend": [0],
"sleutelwoord": [3],
"drukt": [3,7],
"rood": [[0,1,6,7]],
"exporteert": [4],
"omschrijv": [4],
"weergegeven": [7,1,[0,5],4,2,3,6],
"xml-gebaseerd": [0],
"uploadt": [2],
"toegewezen": [4,7,[0,5,6]],
"algemeen": [0,1,2,7,5,8],
"tenminst": [[2,7]],
"geëxporteerde": [8],
"heeft—misschien": [2],
"welk": [7,[0,2],1,[5,6]],
"mechanismen": [[0,2]],
"tweed": [4,[0,1,2,5,7]],
"geproduceerd": [[2,5]],
"empti": [2],
"machinevertalingen": [1],
"vereenvoudigen": [1],
"mensen": [[2,4]],
"terugbetalen": [3],
"menu\'s": [8],
"eindigen": [0],
"presenteert": [0],
"kenni": [[1,2]],
"bekijk": [2,4,7,0,3,1,[5,6]],
"tmx": [2,7,6,[3,5]],
"gewoon": [3,4],
"partijen": [2,[3,5]],
"cli": [2],
"woordgren": [0],
"application_startup": [7],
"eventtyp": [7],
"integ": [1],
"e-mailadr": [0],
"fr-ca": [1],
"creatiev": [0],
"mainmenushortcuts.properti": [0],
"taalregio": [1],
"namelijk": [0],
"langzamerhand": [0],
"kolommen": [7,[0,1]],
"subtitl": [2],
"algehel": [0],
"genegeerd": [0,7,[2,6]],
"gotohistorybackmenuitem": [0],
"ingevoegd": [6,[1,2,4],3],
"beschrijfbaar": [7,0],
"save": [7],
"toe": [[0,2],7,3,[4,6],5,1],
"v1.0": [2],
"cjk-teken": [7],
"gebeurteni": [[0,2,7]],
"top": [5],
"tot": [0,7,2,4,[1,3],6,5,8],
"have": [0],
"powerpc": [2],
"opslagplaat": [2,6,[4,7],[1,5]],
"question": [0],
"versie": [8],
"editselectsourcemenuitem": [0],
"segmentparen": [7],
"eind": [0,7,[1,3]],
"regeleind": [0,7],
"werkstroom": [0],
"segmentatie": [8],
"gevolgd": [0,1,2,3],
"com": [0],
"instal": [2],
"cot": [0],
"remot": [2],
"bewezen": [0],
"gebruikt": [0,2,7,[1,3],6,4,[5,8]],
"verlaten": [4,1],
"proce": [3,7,[0,2],1],
"scrollt": [1],
"pipe": [0],
"letterig": [2],
"aanbiedingen": [3],
"verplaatst": [4,7,0,5],
"momenteel": [4,5,[0,1,2,7]],
"bereik": [0,3,7],
"rout": [2,7],
"changeid": [1],
"lijnen": [7,[1,3]],
"translat": [0,[1,2],[3,4,7]],
"breken": [2],
"werk": [7,[2,3],0],
"opslagplaats_voor_alle_omegat_team_project_bronnen": [2],
"mappen": [2,7,[3,6],4,0],
"werd": [[1,2],7,[0,3]],
"punten": [7],
"doelseg": [4,1,0],
"scheiden": [[0,7],2],
"paniek": [3],
"minimaliseert": [5],
"doorlopend": [0],
"geïnterpreteerd": [0],
"normaal": [[0,7],2,6],
"cqt": [0],
"taalpaar": [[0,1,2]],
"maximaliseert": [5],
"geldigheid": [[3,7]],
"docs_devel": [2],
"lck": [5],
"paar": [3,[0,4],[7,8]],
"tsv": [0],
"primair": [2],
"correcti": [7],
"opnieuw": [2,[3,4],0,7,1,6,[5,8]],
"gnome": [1],
"ingesteld": [1,2,[4,6,7],[0,5]],
"configuratie-bestanden": [2],
"onverwacht": [2],
"puntkomma": [2],
"opgegeven": [0,[1,2,6]],
"tekstfilt": [0],
"uitzonderingsregel": [0,1],
"maximal": [0,7],
"doctor": [0],
"taal—zou": [0],
"alfabet": [0],
"opslagplaatsen": [[0,2,5,6]],
"voorvoegsel": [6,1,2],
"gekloond": [2],
"appdata": [0],
"geautomatiseerd": [[1,2]],
"verwacht": [2,0],
"japan": [1,2],
"csv": [0,2],
"besluit": [3,2],
"scheidt": [0,7],
"verwijdert": [[2,6],[0,4,7]],
"zoekterm": [7],
"caractèr": [2],
"zoal": [0,7,2,5,6,3,[1,4]],
"aanvullen": [1,[0,5]],
"les": [5],
"press": [0],
"dock": [2],
"element": [0,7],
"verwijderd": [2,1],
"caret": [0],
"sorteer": [1],
"night": [2],
"bovenst": [1],
"functionaliteiten": [4],
"genaamd": [2,0,[1,6,7]],
"beperkt": [2,[0,6]],
"handleiding": [8],
"scheidingen": [[4,5]],
"herhalen": [2],
"basisprocedur": [2],
"ouder": [2],
"geïnteresseerd": [0],
"statusbalk": [5,[0,3,8]],
"filenameon": [1,0],
"cut": [0],
"uitgeschakeld": [1,[4,7],0],
"ctrl": [0,4,3],
"editorinsertlinebreak": [0],
"jumptoentryineditor": [0],
"document": [0,7,2,4,3,[1,5,6,8]],
"ergen": [6],
"rapport": [3],
"standaard": [0,1,7,4,2,5,3,6,8],
"moment": [3,[2,7],1],
"page_up": [0],
"glossaryroot": [0],
"doet": [1],
"scenario": [2],
"splitst": [7],
"geïmporteerd": [5],
"aanbiedt": [0],
"doen": [0,2,7,[3,4,5]],
"doel": [7,[2,8],[0,1,4]],
"resourc": [2,0],
"distributi": [2,7],
"moodl": [0],
"nauwkeurig": [[6,7]],
"verkennen": [0],
"contextmenu": [1,5,[0,3,4,6]],
"team": [2,1,[0,8]],
"xx_yy": [0],
"wereld": [2],
"docx": [2,[0,4,7]],
"bundel": [2],
"project_stats_match_per_file.txt": [[4,6]],
"txt": [2,0,5],
"zeven": [0],
"herladen": [[0,4,7],[3,6]],
"vervangen": [7,0,[1,4],3,5,[6,8]],
"voorbeeld": [0,7,[1,4],[2,3,5]],
"projectmedopenmenuitem": [0],
"lib": [0],
"bijwerken": [2,1],
"lid": [2],
"wijst": [0,3],
"spellingscontrole": [8],
"typ": [0,2],
"source": [8],
"reden": [[0,2]],
"harde-en-snell": [7],
"vervolgen": [[0,3,7]],
"viewdisplaymodificationinfoselectedradiobuttonmenuitem": [0],
"configurati": [2,1,0,[3,7],4],
"index.html": [0,2],
"zoek": [0,3],
"waarschuwen": [2,3],
"verwachtt": [1],
"diffrevers": [1],
"nogmaal": [1],
"respecteren": [2],
"samengesteld": [1],
"voortgang": [5,2],
"schroom": [2],
"dingen": [3],
"page": [[0,2]],
"verwachte": [8],
"verkeerd": [7],
"projectgegeven": [2,1,[4,6,7]],
"omvat": [[0,2]],
"n-e": [7],
"optioneel": [4],
"waarvan": [7,[2,3,4],6],
"project.gettranslationinfo": [7],
"regeleinden": [0],
"meerderheid": [0],
"czt": [0],
"mooie": [0],
"invoert": [3,[1,2,5,7]],
"doeltaalcod": [3],
"start": [2,[1,4,5,6]],
"inschakelt": [5],
"mymemori": [1],
"samengevoegd": [[0,3,7]],
"bereiden": [0],
"niveau": [7,0,3],
"regex101": [0],
"equal": [0,2],
"breiden": [7],
"vormt": [[0,6]],
"watson": [1],
"opgestart": [2],
"sneltoetsen": [0,3,[4,7],[2,8],5],
"zoeken": [7,0,1,4,3,[2,5,8]],
"systeem-bred": [0],
"erna": [0],
"viewmarkglossarymatchescheckboxmenuitem": [0],
"corresponderen": [7,[2,5],[1,6]],
"enter": [0,7,4,1],
"toegang": [2,4,7,[0,1],3,6,8],
"applic": [2,[0,5]],
"teksten": [7,0],
"projectteamnewmenuitem": [0],
"gotoprevxenforcedmenuitem": [0],
"niet-afbrek": [0],
"erme": [[0,2]],
"annuleren": [0],
"directorate-gener": [4],
"omega": [7],
"vraagteken": [0],
"autocompletertablelast": [0],
"memori": [2],
"submenu": [[2,7]],
"ooit": [[1,3]],
"resulterend": [2,[4,7]],
"wanneer": [2,1,[0,7],3,4,6,5],
"log": [0,4],
"principes": [8],
"vrijblijvend": [3],
"los": [[5,7]],
"openjdk": [1],
"永住権": [[1,7]],
"uploaden": [2],
"java-bestand": [2],
"computer": [8],
"aided": [8],
"potentiël": [[0,1,4,6]],
"inroept": [2],
"toolscheckissuesmenuitem": [0],
"terugbrengen": [7],
"tussenliggend": [2],
"erop": [[2,4],7],
"opslaat": [3,[2,7]],
"vereenvoudigd": [2],
"opslaan": [2,1,[4,7],6,0,8,5],
"grijpen": [3],
"niet-afbreekbar": [7,[0,4],[1,3]],
"beschrijft": [0,2],
"gelezen": [2],
"autocompletertablepageup": [0],
"twijfel": [6],
"www.deepl.com": [1],
"uitvoer": [2,1,[3,8]],
"wegen": [1],
"config-fil": [2],
"door": [0,2,7,4,3,1,5,6,8],
"interessant": [0],
"quick": [0],
"tell": [7],
"autotekst": [0],
"dag": [2,0,4],
"geeft": [4,5,1,7,0,3,2,[6,8]],
"telt": [[1,4]],
"dan": [2,1,3,[6,7],0,5,4],
"verwijst": [0],
"voortzet": [0],
"offline-programma": [2],
"dat": [2,0,7,3,1,5,4,6,8],
"klass": [0],
"halen": [4,1],
"uitgesloten": [7,2],
"day": [0],
"lre": [0,4],
"minuten": [2,[1,3,4,6]],
"system-user-nam": [0],
"lrm": [0,4],
"format": [0,4],
"positieven": [1],
"tellen": [4,1],
"console.println": [7],
"vanweg": [3],
"spelfout": [7],
"rainbow": [2],
"translation-programma": [8],
"oefen": [2],
"versleuteld": [0],
"autocompleterlistdown": [0],
"erto": [0],
"part": [7],
"zwakk": [0],
"koreaans": [1],
"conventies": [8],
"samenvoegen": [7,0,[1,2]],
"coördin": [2],
"mijn": [2],
"browser": [[1,4,5]],
"uitgav": [2,4],
"geven": [1,0,2,5,[3,4,7],6],
"gevuld": [6,0,[2,4]],
"activefilenam": [7],
"fuzzy": [8],
"project_files_show_on_load": [0],
"voorkomt": [[2,5,7]],
"voeren": [2,7,3,0,[1,4],[5,8]],
"uitsluiten": [2],
"essentieel": [2],
"build": [2],
"formuleren": [3],
"gesynchroniseerd": [2,1],
"bijv": [[0,1],[2,7]],
"bedoeld": [0,[2,6]],
"tekstuel": [7],
"samenstelt": [0],
"entries.s": [7],
"inloggegevens": [8],
"schrijfsystemen": [1],
"gotonextuntranslatedmenuitem": [0],
"doeltermen": [5],
"targetlocal": [0],
"geteld": [[1,4]],
"opdelen": [0],
"path": [2,0,7],
"zorg": [2,[1,7],[0,3,5]],
"bind": [7],
"monospac": [1],
"valutasymbool": [0],
"toegangssleutel": [1],
"frans—indien": [1],
"past": [5,[3,4,7]],
"percentag": [5,1,6],
"grati": [2],
"plaatsvervang": [1,0],
"variabel": [0],
"wensen": [0],
"gebruikershandleiding": [8],
"helpcontentsmenuitem": [0],
"resnam": [0],
"maken—bekend": [0],
"omegat-org": [2],
"verkort": [[3,7]],
"weergeven": [1,0,[5,7],[2,4],[3,6]],
"remote-project": [2],
"afgehandeld": [0,[2,7]],
"sdlxliff-bestanden": [2],
"initialcreationid": [1],
"ignore.txt": [6],
"selecteren": [0,[4,7],5,2,1,3],
"projectaccessdictionarymenuitem": [0],
"vertaalgeheugens": [8],
"hierond": [0,2,6,[3,5,7]],
"virtuel": [7],
"voorkeur": [4,0,2,5,[1,7],6,3],
"gesplitst": [[0,1,5]],
"onderstreept": [4,5],
"ingaat": [1],
"term": [1,[3,5],[0,4,7]],
"externe_opdracht": [6],
"backslash": [0,2],
"geactiveerd": [0,[2,4,7]],
"geconverteerd": [[2,7]],
"files_order.txt": [6],
"uit": [7,2,6,0,3,5,4,1,8],
"behoud": [0],
"projectrestartmenuitem": [0],
"pauz": [3],
"editorskipnexttoken": [0],
"standaarden": [0],
"niet-vertaald": [7,[0,4],1,5,[2,3,6]],
"trans-unit": [0],
"projectbeheerd": [2],
"twee-lett": [[3,7]],
"right": [0],
"parse-gewijz": [7],
"qigong": [0],
"vindt—in": [0],
"deelnemend": [2],
"projecteigenschappen": [2,[6,7],[3,4],[1,8]],
"script-bewerk": [7],
"directionele": [8],
"maximum": [[0,2]],
"dia": [0],
"detailweergav": [2],
"die": [0,2,7,1,3,6,4,5,8],
"zoekend": [0],
"klikt": [1],
"keuzelijsten": [0],
"functi": [0,4,3,[1,7],2,[5,6]],
"maal": [0],
"dir": [2],
"werkwijz": [3,5],
"down": [0],
"maak": [2,3,6,[1,7]],
"dit": [0,2,7,4,6,1,5,3,8],
"opties": [8],
"later": [2,3,[6,7],[0,5]],
"opmaak-specifiek": [1],
"laten": [0,2,1,4,[5,6],3],
"viewfilelistmenuitem": [0],
"kwijt": [[2,3]],
"straf": [6,1],
"info": [0,[1,4]],
"deselecteren": [[0,7]],
"journey": [0],
"test": [2],
"veranderen": [0],
"omegat": [2,0,3,7,1,4,6,8,5],
"tekstpatroon": [0],
"allemand": [1,7],
"handmatig": [7,2,0,6,[3,4]],
"deepl": [1],
"manier": [0,3,7,[2,8],4],
"zoals": [8],
"gepresenteerd": [5],
"meerdere": [8],
"gtk-look": [1],
"virtual": [[2,7]],
"projectinstellingen": [[2,3,4]],
"staan": [1,0,7,2,[3,4,6]],
"inbed": [0],
"console-align": [[2,7]],
"dissimul": [5],
"back": [0],
"kloond": [2],
"projectopenrecentmenuitem": [0],
"fr_fr": [3],
"load": [7],
"niet-standaard": [2],
"staat": [0,7,2,4,3,1,5,6],
"html-bestand": [0],
"vertellen": [0],
"berek": [7],
"eenmaal": [2,4,[0,3]],
"overeenkomen": [0,7,1,[2,3,6]],
"issue_provider_sample.groovi": [7],
"soortgelijk": [0,[1,2,3],5,[4,7]],
"uitdrukt": [3],
"unl": [5],
"maar": [0,2,3,7,[1,6],4,5],
"editoverwritemachinetranslationmenuitem": [0],
"gecomprimeerd": [[1,6]],
"console-stat": [2],
"ingreek": [0],
"voorrang": [7,2],
"indexitem": [0],
"lunch": [0],
"vorig": [0,4,[5,7],3],
"id-cod": [0],
"f12": [7],
"corrigeren": [[1,4,7]],
"convert": [2,3],
"projectexitmenuitem": [0],
"configuratiebestand": [7],
"vertaalservic": [5],
"uitlijn": [7,[0,2,4]],
"adoptium": [2],
"volgende": [8],
"text": [2,7],
"misschien": [[0,2],5],
"editregisteruntranslatedmenuitem": [0],
"init": [2],
"collaboratiev": [2],
"uitvoerbaar": [[0,2]],
"thuismap": [0],
"model-id": [1],
"vastgezet": [5,3],
"streep": [0],
"manag": [2],
"manifest.mf": [2],
"maco": [0,2,4,5,3,1],
"zullen": [2,0,7,1,[4,5,6],3],
"doc": [7,0],
"opent": [4,7,1,[2,3],[0,5]],
"zijden": [2],
"deelt": [2],
"output-fil": [2],
"status": [2,5,[0,6,7],[1,4]],
"structuur": [[6,7],[0,2,3,8]],
"server": [2,1,6,5],
"xml-bestanden": [0],
"klaar": [7],
"dot": [0],
"paramet": [2,7,[0,1],6],
"run-on": [0],
"gevoeligheid": [0],
"brongegeven": [4],
"mag": [0,[2,6,7],[1,3,4,5]],
"juist": [[2,4],[0,1,3,5,7]],
"doelgebi": [5],
"validatie-doeleinden": [0],
"map": [2,7,6,0,4,1,3,5,8],
"accentueren": [[0,7],4,5,[1,6]],
"gaandeweg": [6],
"url": [2,1,[3,6],[0,4,7]],
"overzicht": [2],
"megabyt": [2],
"uppercasemenuitem": [0],
"viewmarkuntranslatedsegmentscheckboxmenuitem": [0],
"synchroniseert": [2,7,[3,4,5]],
"relev": [[0,1,2]],
"needs-review-transl": [0],
"tekstreek": [4,0],
"varieert": [5,0],
"herinneringen": [3],
"html-document": [0],
"use": [2],
"usd": [7],
"omsloten": [[0,7],1],
"feel": [1],
"dienen": [2],
"vertaal": [2,0],
"gedistribueerd": [7,[0,8]],
"doelsegmenten": [7,1],
"tussen": [[0,2],5,4,[1,7],3,6],
"omegat.jar": [2,0],
"omegat.app": [2,0],
"usr": [[0,1,2]],
"markeert": [4],
"teruggaan": [[3,4]],
"invoegen": [0,4,[1,5],7,3,[2,6]],
"logo": [0],
"expressies": [8],
"utf": [0,6],
"echter": [7,[0,2,3]],
"html-tag": [[0,1]],
"bewerken": [7,5,0,3,1,4,[2,8],6],
"feed": [0],
"woorden": [0,4,6,7,[1,5]],
"servic": [1,2,4,5],
"getypeerd": [7],
"dsl": [6],
"beïnvloeden": [2],
"onveranderlijkheid": [6],
"doorverwezen": [2],
"cliënt": [[0,2,6]],
"mee": [1],
"med": [4],
"beginnend": [[0,3]],
"probeer": [7],
"uur": [3],
"dtd": [[0,2]],
"met": [0,2,7,1,3,5,4,6],
"voorzichtig": [2,7],
"onmiddellijk": [7,[0,2,5]],
"weergeeft": [0,[4,7],[1,2,3]],
"niet-zichtbar": [0],
"projectcompilemenuitem": [0],
"classnam": [2],
"console-transl": [2],
"geëscap": [2],
"voren": [3],
"voorzien": [2],
"optionsautocompletehistorycompletionmenuitem": [0],
"gotonextuniquemenuitem": [0],
"cloud-servic": [2],
"conform": [2,7],
"samenwerk": [3],
"vierkantj": [1],
"wordart": [0],
"princip": [[3,5]],
"bron": [7,1,0,2,4,6,5,3],
"dus": [3,2,[0,1]],
"feit": [[1,2]],
"about": [0],
"commit": [2],
"targetlocalelcid": [0],
"project_stats_match.txt": [[4,6]],
"dezelfd": [2,[0,1,7],[3,5],6],
"inloggen": [1,[0,8]],
"bang": [[2,3]],
"talen": [7,1,2,3,6,[0,4]],
"gewerkt": [3],
"evoluti": [0],
"aanroepen": [7,0],
"huidig": [4,7,[0,2],5,6,1,3],
"standaardta": [2],
"stappen": [3,0,[2,7]],
"symbool": [[0,2,5,7]],
"betekeni": [0,[5,7]],
"vergelijken": [2,7],
"configureren": [1],
"relatiev": [1],
"libreoffic": [3],
"autocompleterclos": [0],
"relatief": [2,0],
"minimalis": [2],
"long": [0],
"bewerkt": [[5,7],4],
"bevestigen": [[0,4,6],[1,7]],
"dienst": [2],
"twee": [2,7,0,[1,3,4],[5,6]],
"bevestig": [1],
"doeltalen": [[6,7],[1,2]],
"defin": [0,1],
"negerend": [4],
"aanpassingen": [0,4,2,[1,3,7],[5,6]],
"voordat": [[2,7],1,3],
"gemak": [[2,7]],
"afgebroken": [0],
"vertaalde": [8],
"stap": [7,2,6],
"pijltjestoetsen": [7,5,0],
"viewdisplaysegmentsourcecheckboxmenuitem": [0],
"klik": [7,3,[0,1],5,4,2],
"editregisteremptymenuitem": [0],
"uitbreid": [0],
"publiceren": [2],
"lettertypen": [4],
"stats-output-fil": [2],
"dag-tot-dag-basi": [0],
"beheren": [3,2,8,0,[1,4]],
"open": [7,2,0,4,6,1],
"project": [2,7,3,6,4,0,5,1,8],
"onderst": [7,1],
"取得": [[1,7]],
"xmx1024m": [2],
"moest": [[3,7]],
"lijkt": [4,[0,2]],
"doel-bestandsnaam": [0],
"geschikt": [[0,3,5]],
"kennen": [0],
"penalty-xxx": [[2,6]],
"gotonextsegmentmenuitem": [0],
"vergrendeld": [5,2],
"hoofdproject": [2],
"identificeren": [0,3,[1,7]],
"tab-gescheiden": [0],
"gebeurd": [2],
"verificati": [1],
"dropbox": [2],
"omdat": [2,0,7,[1,3]],
"tsv-bestand": [0],
"conversi": [2],
"internet": [1],
"vraag": [2],
"object-attributen": [0],
"suggereren": [5],
"betrokkenen": [2],
"brug": [2,[0,6]],
"nummer": [0,1,4,7,[3,5,6]],
"langer": [2,[0,1,5,7]],
"projectbestanden": [[1,2]],
"downloadt": [2],
"vergrendelt": [3],
"beschouw": [0],
"beurtel": [2],
"gebaseerde": [8],
"toevoeg": [0],
"geüpload": [2],
"logbestand": [4],
"bevrijdt": [7],
"registri": [0],
"bestandsstructuur": [0],
"gebonden": [7],
"machinevert": [[4,5],1,0,[3,6]],
"step": [0],
"bash": [0],
"basi": [2,[0,4]],
"tmroot": [0],
"stel": [3,2],
"stem": [1,5],
"registr": [2],
"belangrijkst": [3],
"omegat-filt": [0],
"blokniveau": [0],
"uitgegrijsd": [7,4],
"overschrijven": [5,[0,4,6],2],
"api-sleutel": [1],
"titel": [[4,7]],
"大学": [1],
"relati": [1],
"insertcharslr": [0],
"vak": [7,0,1],
"onderwerpen": [2],
"van": [2,0,7,1,4,3,5,6,8],
"work": [0],
"gemakkelijkst": [2],
"programmagebeurtenissen": [0],
"breekpunt": [1],
"word": [[0,3,7]],
"lingue": [1],
"japans": [2,[0,1]],
"eindigt": [0,2],
"eenvoudigst": [0,2],
"specifiek": [2,0,7,1,4,3],
"regulier": [0,7,1,2,3],
"één": [0,7,4,2,1,[5,6],3],
"koptekst": [0],
"opschoonacti": [7],
"langzaam": [3],
"recursief": [7],
"corrumperen": [2],
"haken": [0],
"zorgt": [[0,7]],
"bedekken": [0],
"afzonderlijk": [2,7,0,1,[3,5,6]],
"vcs": [2],
"lingvo": [6],
"developer.ibm.com": [2],
"aangeven": [[2,4]],
"gevonden": [7,4,2,[0,1,3,5]],
"dollar-teken": [0],
"mrs": [1],
"vooruit-terug": [7],
"uitzien": [3,[1,8]],
"onderk": [[1,7]],
"uitziet": [3],
"beschrijven": [[2,4]],
"opgedeeld": [[0,7]],
"denkt": [3],
"herinn": [0],
"daarna": [[0,1]],
"gecontroleerd": [1],
"teksteenheden": [0],
"float": [1],
"lijst": [0,[1,2],7,3,4,8,6],
"genummerd": [5,0],
"indient": [2],
"biedt": [2,[3,5],[0,7]],
"jaar": [[0,2]],
"html": [0,2],
"begrenz": [0],
"aanhalingsteken": [0,7],
"spell": [[0,3,4]],
"ver": [[2,3]],
"vet": [1,[0,5],[3,7]],
"ontvangen": [[1,5]],
"insertcharsrl": [0],
"regelmatig": [2,[0,6,7]],
"probeert": [7,[1,2],0],
"mui": [7,[4,5],[1,3]],
"vermeld": [0,2,[3,4,5,6,7]],
"www.ibm.com": [1],
"fijnmazig": [0],
"elder": [4],
"volgend": [0,4,7,2,5,3,1,6,8],
"tekenreeksen": [0,7,3,2],
"platform": [2,0,1],
"een": [0,2,7,3,1,4,6,5,8],
"bronbestand": [0,[4,7],3],
"projectpakket": [4],
"verschaffen": [0,2,[1,7,8]],
"aangeeft": [2],
"toolsalignfilesmenuitem": [0],
"stijl": [2],
"andere": [8],
"toegangsrechten": [2],
"daarom": [0,7,[2,4]],
"zonder": [2,0,7,1,6,[3,4,5]],
"overeenkomend": [[4,7],[0,1,6],5],
"daarop": [[4,7]],
"recht": [0,4,5,7,[2,6]],
"kopiëren": [[2,3],[0,1,5,6,7]],
"command": [4,0,3],
"software-documentati": [2],
"bovenk": [1],
"getest": [2],
"installatiemap": [2],
"aanwijz": [[2,5]],
"meedelen": [7],
"indien": [4,2,0,1,7,5,3],
"slash": [0],
"woordenlijstbestand": [7,0],
"onecloud": [2],
"viewmarkbidicheckboxmenuitem": [0],
"groepen": [1,[0,7]],
"via": [2,0],
"kwamen": [6,2],
"springen": [5,3],
"fileshortpath": [[0,1]],
"toch": [2],
"spatiebalk": [0],
"stukj": [1],
"automatiseren": [2,[3,7]],
"lettertyp": [1,4,5,[0,3]],
"minimale": [8],
"日本語": [7],
"alinea-eind": [0],
"opti": [[2,7],0,1,4,3,6,5],
"weigert": [[2,3]],
"version": [2],
"uren": [2],
"folder": [5,[2,6]],
"aanwijzingen": [0],
"stop": [1],
"buiten": [0,[3,5],6],
"klassen": [0,8],
"detail": [2,4,7,0,1,3,6,5],
"levert": [2],
"synchronisati": [2,5],
"projecteditmenuitem": [0],
"belang": [3],
"begonnen": [3,[2,7]],
"sleep": [2],
"new_word": [7],
"worden": [0,7,2,1,4,6,3,5,8],
"toen": [0],
"run\'n\'gun": [0],
"nashorn": [7],
"machin": [1,7,[2,4]],
"toet": [0,7,[1,4,5]],
"unsung": [0],
"last_entry.properti": [6],
"begroet": [3],
"loopt": [0],
"combineren": [[2,6,7]],
"stuk": [7],
"kunt": [2,0,7,3,[1,5,6],4,8],
"numeriek": [0],
"alinea": [0,7,[1,4],[3,5]],
"invullen": [4],
"gecorrumpeerd": [2],
"autocompleternextview": [0],
"omegat.project.vergrendeld": [2],
"specif": [7],
"spring": [0],
"activeert": [0,[4,7]],
"opstarten": [2,[0,1,4]],
"achterliggend": [0],
"neer": [[2,5,6]],
"dsun.java2d.noddraw": [2],
"herkennen": [[0,3],7],
"gedeclareerd": [0],
"tegenstel": [0],
"regiocod": [0],
"ell": [1],
"elk": [0,2,7,6,3,4,1,5,8],
"mappenstructuur": [7],
"canade": [1],
"editorfirstseg": [0],
"afdrukken": [0],
"x0b": [2],
"neem": [7,[0,3,6]],
"getypt": [[2,3]],
"canada": [2],
"altern": [0],
"http": [2],
"bestandsindelingen": [[0,1,2],[3,4,5]],
"uitdrukkingen": [0,7,[1,8],2],
"vanuit": [2,4,0,7,[1,5],3,6],
"decimal": [0],
"rapporten": [[2,4,7]],
"rondgang": [2],
"komma": [0],
"acteert": [6],
"lisenc": [0],
"syntaxi": [0,2,7],
"invlo": [3],
"teken": [0,7,4,5,1,2,3],
"vol": [3],
"softwar": [0],
"zodra": [[4,6,7]],
"projectsinglecompilemenuitem": [0],
"gelijk": [[1,4,5]],
"end": [0],
"lisens": [0],
"ene": [2],
"zolang": [0],
"globaal": [7,0,1,[4,5,8]],
"geversioneerd": [6],
"visuel": [0,3],
"myfil": [2],
"kader": [4],
"env": [0],
"verlaagd": [6],
"special": [0,[2,6,7]],
"okapi": [2],
"segmenteren": [[0,3]],
"realiseert": [3],
"opmerk": [2,0,4,7,1,[3,5],6],
"page_down": [0],
"menu-item": [0,4,3,7],
"gedeelten": [3],
"springt": [7,4],
"logingegeven": [2],
"project_nam": [7],
"system-os-nam": [0],
"gelijktijdig": [1],
"insertcharspdf": [0],
"decorati": [3,0],
"segmentation.srx": [[0,6,7]],
"bevestigd": [2],
"zetten": [[0,5]],
"med-project": [0,4],
"tar.bz2": [6],
"statusbericht": [5],
"forceert": [0],
"bundle.properti": [2],
"gemaximaliseerd": [5],
"contributors.txt": [0],
"https-protocol": [2],
"pad-naar-projectbestand-van-omegat": [2],
"gegeven": [2,1,4,[3,7]],
"reproduceert": [0],
"www.regular-expressions.info": [0],
"controleren": [1,[0,3,4],2,[5,7]],
"bronsegmenten": [0,[4,5],[1,6,7]],
"verdwijnen": [2],
"sourcelang": [0],
"mogelijk": [2,0,[1,7],5],
"dichtstbijzijnd": [1],
"ander": [2,0,1,7,5,3,6,4],
"optionsdictionaryfuzzymatchingcheckboxmenuitem": [0],
"beperk": [[0,3]],
"interfac": [2,[0,4]],
"gesloten": [5,2],
"projet": [5],
"soorten": [0],
"stuur": [2],
"niet-gesegmenteerd": [0,3],
"sourcelanguag": [1],
"onthoudt": [4],
"onnodig": [1],
"tekst": [0,7,5,4,1,3,2,8,6],
"gzip": [6],
"helpupdatecheckmenuitem": [0],
"stellen": [0,[1,7],2,[3,4]],
"werkt": [2,7,0,[1,4,6,8]],
"uitlijnen": [7,[0,4],[2,3,8]],
"esc": [5],
"exampl": [0],
"berichten": [[0,5]],
"nostemscor": [1],
"gedrukt": [3],
"project_chang": [7],
"wijzigingen": [0,2,7,[4,6],[1,3,5]],
"vul": [2],
"console-createpseudotranslatetmx": [2],
"achtereenvolgen": [4],
"witruimten": [0],
"lossen": [[2,3]],
"etc": [1,[0,5,6]],
"fuzzyflag": [1],
"net": [2,[0,6],[3,4,5,7]],
"grammatisch": [7],
"escap": [0],
"new": [1],
"voorspel": [0],
"projectinformati": [[3,5]],
"poisson": [7],
"runway": [0],
"verstoren": [1],
"bestanden": [2,7,0,3,6,4,5,1,8],
"groepeer": [1],
"tool": [2],
"ll-cc.tmx": [2],
"po-bestanden": [0,2,[1,6]],
"foutbericht": [2],
"gestart": [7,1,[0,2]],
"valideren": [[1,4]],
"verkleinen": [[2,7]],
"functioneert": [0],
"behouden": [7,0,2,6,[1,5]],
"grunt": [0],
"wissen": [[0,1]],
"calcul": [7],
"wissel": [5],
"defini": [0,1],
"leveringen": [2],
"magento": [2],
"plus-teken": [0],
"ophalen": [4,[0,1]],
"ll.tmx": [2],
"proefschrift": [1],
"vermeldenswaardig": [2],
"diagrammen": [0],
"specific": [2,[1,7]],
"comprimeren": [0],
"dubbelklik": [2,7,0],
"bronnen": [[3,7],6,0,[2,8]],
"offlin": [2],
"aanvang": [6],
"selectief": [2],
"ll_cc.tmx": [2],
"u00a": [7],
"cyclus": [[0,4]],
"logbestanden": [0],
"echt": [4,[2,7]],
"shift": [0,4,7],
"optreedt": [[2,5]],
"gegevensbestand": [6],
"attribuut": [0],
"auto-tekst": [1],
"progressief": [1],
"java": [2,0,1,7,3],
"inbedden": [[0,4]],
"zoekveld": [7,1],
"tweetalig": [2,[6,7]],
"xmxsize": [2],
"ondervindt": [2],
"project_save.tmx": [2,6,[3,7],4],
"dictionari": [0],
"ongeacht": [2],
"gevraagd": [[2,4],7],
"voorspellingen": [3],
"zinsegmentati": [7],
"powershel": [[0,2]],
"verplicht": [7],
"eye": [0],
"twaalf": [0],
"vertaalbar": [[0,7]],
"opslag_voor_omegat_teamproject": [2],
"comfortabel": [7],
"spellingscontrol": [1,3,[6,7],[0,2],4],
"appl": [0],
"samen": [3,7],
"opnemen": [2,5],
"gereedschappen": [2,4],
"structurel": [0],
"ontwikkelaar": [2,0],
"registreren": [[2,4,5],[0,1]],
"sudo": [2],
"mechanism": [2,4],
"netj": [2,1],
"timestamp": [0],
"ontgrendelt": [[3,5]],
"projectaccessrootmenuitem": [0],
"betrouwbaar": [6,2],
"afgetrokken": [6],
"behoeven": [5],
"back-upbestanden": [2],
"plugin": [2],
"autocompletertableup": [0],
"onvoorwaardelijk": [6],
"doelzin": [3],
"geopend": [2,7,1,4,[0,5]],
"toegevoegd": [2,7,[0,1],[4,6]],
"gekoppeld": [[1,3,6]],
"lijstweergav": [[2,5]],
"doorloopt": [7],
"roepen": [4,[0,2,3,7]],
"projectcommitsourcefil": [0],
"editinsertsourcemenuitem": [0],
"rijen": [7],
"viterbi": [7],
"microsoft": [0,[3,7]],
"sleuteltoetsen": [0],
"projectnewmenuitem": [0],
"gegevensv": [2],
"ecmascript": [7],
"verzonden": [[1,2]],
"neergezet": [5],
"functiecod": [0],
"segment": [4,7,5,0,1,3,2,6],
"changes.txt": [[0,2]],
"foutberichten": [5],
"glossari": [0,6,[5,7],4,2],
"ignored_words.txt": [6],
"leren": [0,3],
"absoluut": [[0,1]],
"herhaalt": [3],
"betrokken": [2,[1,7]],
"cijfer": [0],
"github.com": [2],
"configuration.properti": [2],
"zodat": [[0,3,8],[2,5,6]],
"prototyp": [7],
"afbeeld": [[3,5]],
"autocompleterlistpageup": [0],
"bijvoorbeeld": [0,2,7,[1,6],3,[4,5]],
"glossary": [8],
"overwogen": [[1,4]],
"jjjj": [2],
"herhaald": [[1,7],4,0],
"vervangend": [0],
"gaan": [3,5,4,[0,7],[1,2]],
"segmentati": [0,7,[1,2,3],[4,6]],
"downloaden": [2,[0,1],[3,4,7]],
"maanden": [3],
"krijgt": [4],
"gaat": [4,[0,1,5,7],[2,3]],
"wijzen": [[0,7],[2,3,4,6]],
"gedownload": [2],
"string": [2],
"nog": [7,0,[1,2,4],3,[5,6]],
"zacht": [0],
"reserveert": [2],
"segmentatieregels": [8],
"bestandsnaam": [0,[5,7],[1,2,6]],
"vervagen": [5],
"bronbestanden": [2,4,3,[0,7],6,[5,8]],
"knippen": [5],
"gren": [3],
"pseudotransl": [2],
"onderliggend": [7],
"regellengt": [0],
"was": [3,[0,1,6]],
"wat": [0,7,2,[1,3,4,6]],
"hoofd": [0,7,4],
"viewrestoreguimenuitem": [0],
"koppel": [0],
"selection.txt": [[0,4]],
"xhtml": [0],
"verlenen": [2],
"itoken": [2],
"finder.xml": [[0,6,7]],
"origineel": [2,1],
"window": [0,2,4,5,3],
"misplaatst": [4],
"criteria": [7,[0,3]],
"disable-project-lock": [2],
"overeenkomsten": [1,4,0,[5,6],7,[2,3],8],
"omegat.pref": [[0,1,7]],
"carriage-return": [0],
"gereedschap": [[2,4,7]],
"beheersteken": [0],
"specificeert": [0,2,[1,5,6]],
"pakket": [2],
"zoek-expressi": [0],
"behoeften": [1,3],
"declarati": [0],
"meertalig": [0],
"hoofd-klein": [0],
"cellen": [7,0],
"computersoftwar": [2],
"moeilijk": [[0,2]],
"widget": [5],
"scripttaal": [7],
"direct": [2,0,4,3,6,[1,7]],
"modern": [2],
"web": [[1,7],[0,2,4]],
"bestandskiez": [2],
"wee": [2,7],
"en-us_de_project": [2],
"systeem": [2,0,[3,7],[4,5,6]],
"weg": [2],
"startpagina": [2],
"symlink": [2],
"verwerk": [[1,2,4]],
"noodzakelijkerwijz": [2],
"bestemd": [6],
"wel": [[0,7],[1,2,3,5,6]],
"hulp": [[0,2,5]],
"geklikt": [5],
"editselectfuzzy4menuitem": [0],
"editregisteridenticalmenuitem": [0],
"prioriteiten": [1],
"hanja": [0],
"po-koptekst": [0],
"geplaatst": [2,0,4,7,6,[1,3,5]],
"positie": [8],
"alinea-scheid": [[1,4,5]],
"bidi-controleteken": [4,0],
"grij": [[3,4]],
"volledig": [0,[2,7],1,5,6],
"toestaan": [1,7,[0,3]],
"doorgegeven": [[0,2]],
"nul": [0,7,2],
"bronbestandsnaam": [0],
"begrijpen": [0],
"magisch": [0],
"nut": [7],
"beveiligen": [1],
"protocol": [2,1],
"doelterm": [4,[0,3]],
"softwareprogramma": [2],
"plek": [5],
"dict": [1],
"storingen": [3],
"vereisen": [[0,2],[1,7],[3,4]],
"spellen": [0],
"overeenkomstig": [[1,7]],
"koppelen": [0],
"veel": [0,2,3,1],
"openen": [0,2,7,[4,5],3,6],
"statussen": [[2,3]],
"volgt": [2,0,3],
"keer": [2,0,[4,6,7],3,5,1],
"marker": [[0,1,4],5,7],
"herinneren": [6],
"effect": [0,7,1],
"option": [[0,4]],
"beurt": [[1,2,3]],
"dien": [0],
"niet-omegat": [1],
"hernoemen": [2],
"losmaken": [[1,5]],
"bronbestandsnamen": [0],
"wie": [1],
"gemakkelijk": [3,4],
"toepass": [2,7,4,0,[3,6],1,5],
"wil": [2,0],
"pakken": [2],
"komen": [0,6,[2,3,5]],
"kolomkop": [4],
"zich": [2,3,0],
"kleiner": [7],
"overgeslagen": [6],
"xliff-filt": [0],
"user": [1],
"vervangingen": [7,4],
"weinig": [4],
"proxi": [2,1],
"extens": [0],
"back_spac": [0],
"uitleg": [7,[0,1]],
"eerst": [0,7,5,[2,4],3,1,6],
"robot": [0],
"kanalen": [0],
"tekstbestand": [[0,4,7],1],
"zien": [3,2,6,[0,7]],
"eclips": [2],
"vertaling": [8],
"pdf-document": [2],
"gang": [4],
"werkend": [6],
"af": [2,1,[4,7],5],
"ziet": [[3,5,7]],
"diff": [1],
"al": [2,0,7,1,4,6,3,5],
"an": [0],
"editmultiplealtern": [0],
"proxy": [8],
"elementen": [[0,3],7],
"uitvoerbar": [0,2],
"ondersteun": [2,[0,3]],
"technisch": [0,2],
"identificati": [[0,4],3],
"karakteristieken": [[0,1,7]],
"taalcod": [7,[2,3],[0,1]],
"tekeningen": [0],
"invoeren": [[3,4],[1,7],2],
"filters.xml": [0,[1,2,6,7]],
"br": [0],
"veld": [5,7,4,[2,3],0],
"segmentation.conf": [[0,2,6]],
"werkwijze": [8],
"ca": [2],
"vele": [0],
"cc": [2],
"hoeft": [0,3,2],
"betrouwbare": [8],
"ce": [2],
"letterbeeld": [1],
"projectmappen": [7],
"bewerkbar": [[0,5]],
"documenten": [7,2,3,0,5,6,[1,4,8]],
"voldoen": [[2,3]],
"gedocumenteerd": [2],
"cs": [0],
"antwoord": [5],
"partner": [2],
"aflopend": [0],
"opdrachtregel": [2,0,[1,7]],
"apach": [2,7],
"gezien": [0],
"config": [2],
"adjustedscor": [1],
"tmx-standaard": [2],
"dd": [2],
"de": [0,2,7,4,1,5,3,6,8],
"duplicaat-tekst": [0],
"projecteigenschap": [0,1,2],
"zijn": [0,7,2,1,6,3,4,5],
"extern": [7,1,4,0,[2,3,5],[6,8]],
"kopieën": [[2,8],0],
"f1": [[0,4,7]],
"f2": [[3,5],[0,7]],
"f3": [[0,4],5],
"f5": [[0,3,4]],
"zijd": [2],
"dz": [6],
"editundomenuitem": [0],
"niet-gebruikersinterfac": [2],
"neerzetten": [5],
"zelfstandig": [[1,5]],
"ingeleverd": [3],
"resultaat": [7,0,3],
"voegen": [2,3,5,0,[4,7],[1,6]],
"signific": [0],
"iedereen": [2],
"bevindt": [2],
"belazar": [1],
"en": [0,2,7,4,3,5,1,6,8],
"lezen": [7,[0,2]],
"er": [2,7,0,1,4,3,[5,6],8],
"eu": [4],
"voorkeuren": [4,0,7,5,1,[2,6],3,8],
"gewoonweg": [3],
"activ": [[6,7]],
"achtergrond": [6,[4,5]],
"terminologi": [0,4],
"origin": [5],
"for": [[0,7],4],
"exclud": [2],
"segmentatieregel": [7,0,[1,4],2,[3,6]],
"projectleden": [2],
"fr": [2,[1,3]],
"content": [[0,2],7],
"duckduckgo": [1],
"ding": [2],
"desktop": [2],
"weergave": [8],
"applescript": [2],
"json": [2],
"gezocht": [0],
"ga": [0,3,2,5,4,6,[1,8],7],
"gb": [2],
"helplogmenuitem": [0],
"toepasbar": [0],
"vergeet": [1],
"methoden": [2],
"terug": [7,3,[0,5],2,4,6,1],
"basiseenheid": [7],
"editoverwritetranslationmenuitem": [0],
"go": [2,3,0,1],
"aeiou": [0],
"bereikt": [3,7],
"toevoegen": [0,6,[2,3,7],[1,4],5],
"verschaft": [2,0,[1,7],[3,4]],
"zijnd": [[2,4,7]],
"he": [0],
"duser.languag": [2],
"viewmarkparagraphstartcheckboxmenuitem": [0],
"aangepast": [0,2,[1,4],3,[6,7]],
"weigeren": [2],
"file-target-encod": [0],
"fout": [2,0,[1,4,5]],
"mainmenushortcuts.mac.properti": [0],
"context": [[1,2,5],4],
"id": [1,0,7],
"https": [2,1,0,[5,6]],
"if": [7],
"project_stats.txt": [6,4],
"projectaccesscurrenttargetdocumentmenuitem": [0],
"vreemd": [0],
"in": [0,7,2,4,1,5,3,6,8],
"verweegt": [3],
"termin": [2],
"ip": [2],
"index": [0],
"is": [2,0,7,4,1,5,6,3,8],
"geselecteerd": [4,7,0,5,2,1,3],
"enzovoort": [7,[3,4,6]],
"projectaccesstmmenuitem": [0],
"odf": [0],
"installeert": [[1,2]],
"ondernomen": [4],
"ja": [7,[1,2]],
"indel": [2,0,[3,6],[1,5],[4,7]],
"odt": [[0,7]],
"tmx-bestanden": [2,7,6,1],
"eventueel": [[2,3]],
"daar": [2,[0,1,7]],
"gotonexttranslatedmenuitem": [0],
"charset": [0],
"librari": [0],
"vinden": [[0,2],[1,4,7],6],
"wijzig": [[1,6],[2,4],3],
"keuz": [[2,7],0],
"toolscheckissuescurrentfilemenuitem": [0],
"libraries.txt": [0],
"learned_words.txt": [6],
"historisch": [3],
"vett": [7,5],
"demonstreren": [0],
"overweeg": [2],
"ftl": [[0,2]],
"betek": [0,[1,2]],
"alinea-segmentati": [0],
"expliciet": [2],
"viewdisplaymodificationinfoallradiobuttonmenuitem": [0],
"beter": [3,7,1],
"off": [4],
"aanvul": [7,[0,4]],
"kolom": [7,0,4],
"la": [1],
"bepaald": [2,1,[0,5],[4,6]],
"samenvatting": [8],
"bronterm": [[0,3,4,5]],
"li": [0],
"xhtml-bestanden": [0],
"ll": [2],
"eenvoudigweg": [[0,2,7],3,6],
"neemt": [7],
"lu": [0],
"ja-jp.tmx": [2],
"schikk": [1],
"onbruikbaar": [7],
"that": [0],
"uittreksel": [0],
"cycleswitchcasemenuitem": [0],
"licenti": [2],
"mb": [2],
"hoofdwachtwoord": [1],
"me": [[2,5]],
"hierboven": [0,2,4,[1,7],[3,5,8]],
"suggesti": [[1,5],0],
"drempel": [1,[2,5]],
"getallen": [1,5,7,0],
"mm": [2],
"entri": [7],
"termen": [5,3,1,[4,6],7,[0,8]],
"gedetailleerd": [2],
"ms": [0],
"mt": [6],
"my": [[0,2]],
"schijf": [2,3,7],
"definiëren": [0,1,7,3,[2,4,5]],
"license": [8],
"helptip": [1,5],
"ondersteuning": [8],
"na": [0,1,7,2,[3,5],[4,6]],
"genest": [0],
"behoren": [[0,1]],
"updat": [1,2,[0,4]],
"waarvoor": [0,[1,2,5,7]],
"verzameld": [6],
"nl": [0],
"licenss": [0],
"datum": [1,[0,7],[3,4,6]],
"no": [0],
"code": [0,[2,3,7]],
"gebaseerd": [7,3,0,1,[2,4,5]],
"volgord": [1,[0,7],[5,6]],
"smaken": [3],
"nu": [0,[2,5],3],
"gotohistoryforwardmenuitem": [0],
"robuustheid": [3],
"spellingen": [0],
"head": [0],
"tegengesteld": [0],
"naast": [[0,2],[4,7]],
"vereiste": [8],
"noch": [0],
"beheer": [[0,3]],
"of": [0,7,2,4,[1,5],3,6],
"ok": [7,4,3],
"om": [2,7,0,3,1,4,5,6,8],
"on": [0],
"sluit": [7,[2,4],0],
"op": [2,7,0,4,1,3,5,6,8],
"or": [0,1],
"appendices": [8],
"niettegenstaand": [[0,3]],
"sjablonen": [1,0,[4,7]],
"doeldocu": [4,[0,2]],
"vormen": [0,7,1],
"editinserttranslationmenuitem": [0],
"beperken": [[0,3,7]],
"fileextens": [0],
"beschouwd": [0],
"pm": [1],
"latijns": [0],
"po": [2,1,5],
"minst": [6,5],
"en-naar-fr": [2],
"drie": [0,2,[1,4,6],[3,5,7]],
"vraagt": [[0,1,7]],
"ongeldig": [2],
"tekenset": [0],
"noodzakelijk—om": [2],
"voorkeurslocati": [0],
"verdeelt": [[1,7]],
"qa": [7,4],
"autocompletertablefirst": [0],
"bash-script": [2],
"doel-local": [0],
"gesleept": [5],
"recens": [[2,6]],
"verwijzingen": [0,7,[2,6]],
"recent": [[2,4],0,[5,7]],
"cyaankleurig": [4],
"they": [0],
"github": [2],
"edit": [7],
"old": [1],
"editselectfuzzy5menuitem": [0],
"teniet": [0],
"gecodeerd": [6],
"rc": [2],
"uitvoeren": [2,7,1,[0,3,8]],
"laagst": [5],
"includ": [2],
"vergroten": [6,[1,2,4],[0,7]],
"t0": [3],
"t1": [3],
"t2": [3],
"hebt": [2,3,[0,1],4,[5,6],7],
"t3": [3],
"gratis": [8],
"resulteren": [1],
"grammatical": [4],
"bestandsfilt": [[0,2,7],1,4,3,6],
"sc": [0],
"tmx-bestand": [[2,6],7],
"heel": [[0,3],2,4],
"fuzzy-overeenkomsten": [[0,4],1],
"heen": [[2,4]],
"sl": [2],
"opstart": [0],
"heet": [2],
"slecht": [0,2,[3,7],4],
"apart": [7],
"starten": [2,0,7,1,[3,4]],
"intern": [2,4,[0,1,5]],
"coderingen": [0],
"stapelend": [1],
"gekregen": [4],
"tenzij": [2,[0,1,3]],
"interv": [1,2,[4,6]],
"noodgevallen": [6],
"editoverwritesourcemenuitem": [0],
"omegat.autotext": [0],
"te": [2,7,0,3,1,4,5,6,8],
"kilobyt": [2],
"alfabetisch": [[0,5,7]],
"enforc": [6,4,[0,2],[1,3]],
"remov": [2],
"tl": [2],
"tm": [2,6,4,0,7,1,3,[5,8]],
"to": [0,[2,3,7]],
"schikken": [[4,5]],
"v2": [2,1],
"document.xx": [0],
"nemen": [[0,2],[1,3,7]],
"inhoudsopgave": [8],
"negeren": [0,1,[4,5,6]],
"omgevingsvariabelen": [0],
"ook": [2,7,0,3,1,6,4,5],
"allebei": [7],
"beschreven": [5,0,2,[3,7]],
"brondocu": [[0,1,2]],
"viewmarkautopopulatedcheckboxmenuitem": [0],
"projectwikiimportmenuitem": [0],
"countri": [2],
"gedrag": [2,4],
"hopelijk": [3],
"bijwerkt": [2],
"un": [2],
"up": [0,3],
"lokaal": [2,[1,7],0,4],
"vertaalproc": [[2,3]],
"uu": [2],
"opmerkt": [7],
"newword": [7],
"uw": [2,3,0,7,1,4,5,6,8],
"this": [0],
"kwam": [3],
"opt": [2,0],
"extract": [7],
"hoofdstuk": [2,7,3,0,[4,5]],
"knop": [7,3,0,1],
"kopieert": [7,4],
"gemarkeerd": [1,7,[0,4,6]],
"know": [0],
"geïnstrueerd": [[0,6]],
"vs": [1],
"punt": [0,2,1,4],
"changed": [1],
"lay-out": [5,[1,3],[0,8],4,[2,7]],
"zoveel": [2,3],
"gedaan": [2,3],
"vertical": [0],
"eenheid": [0],
"potentieel": [0],
"we": [0,[2,3]],
"autocompleterlistup": [0],
"licenc": [0],
"directionel": [4,0],
"omegat.project.bak": [2,6],
"gat": [2],
"ingekapseld": [7],
"projectaccessexporttmmenuitem": [0],
"licens": [2,0],
"org": [2],
"opmaaktekens": [8],
"filtert": [7],
"karakteriseert": [0],
"xx": [0],
"sourc": [2,7,6,4,[0,3,5]],
"hele": [1],
"belangrijk": [2,0,6],
"schrijfruimt": [5],
"type": [7,2,1,[0,6],[4,5]],
"wijzigen": [7,0,1,5,[2,3],[4,6,8]],
"tekstterm": [1],
"scant": [7],
"gebruikersgroep": [[2,3]],
"basishiërarchi": [6],
"optionsautocompletehistorypredictionmenuitem": [0],
"ligt": [[2,5]],
"geïnstalleerd": [[1,2],[0,3,4],[5,7]],
"schrijfrechten": [[2,7]],
"projectaccesssourcemenuitem": [0],
"uiteindelijk": [3,[0,6]],
"yy": [0],
"filteren": [[3,7],2],
"url-protocol": [2],
"gewon": [5],
"method": [7],
"segmenteigenschappen": [5,[3,8]],
"opdracht": [7,2,4,0,1],
"drukken": [7,5,[1,3]],
"ze": [3,2,[0,7],4,6,1],
"manieren": [2,4],
"push": [2],
"vervang": [2,7,[0,3]],
"readme_tr.txt": [2],
"object-georiënteerd": [7],
"zo": [[0,2,3,4,5,6]],
"medeklink": [0],
"ontwikkelingspagina": [0],
"penalti": [6],
"vierkant": [0],
"exact": [7,0,[2,4],[3,6]],
"aanvullingen": [1],
"oud": [2,0,7],
"geladen": [[6,7],[0,1,2,4]],
"opmerkingen": [5,0,[4,7],[3,8]],
"ontwikkel": [2,1],
"verbergt": [7],
"utf8": [0,[4,7]],
"bereiken": [0,[2,7]],
"versies": [8],
"elkaar": [[2,7]],
"power": [0],
"context_menu": [0],
"toepassen": [1,7],
"editsearchdictionarymenuitem": [0],
"parameterbestand": [0],
"tag-valid": [2],
"kleuren": [1,4,0],
"invoersystemen": [1],
"twee-letterig": [2],
"ovr": [5],
"hoeveelheid": [2],
"help": [0,[2,4,8]],
"gebruikten": [7],
"omegat-project": [4],
"typografisch": [[4,7]],
"repositori": [2,6],
"vertalen": [3,7,2,0,[4,5],1,8],
"minimum": [[0,1]],
"gedraagt": [2],
"bestandsindel": [0],
"typt": [[2,3,5]],
"date": [3],
"ini-bestand": [2],
"voorafgaand": [0,4],
"lowercasemenuitem": [0],
"vergrendelen": [2,[0,5]],
"wiki": [[2,6]],
"blok": [7,0],
"autocompleterconfirmwithoutclos": [0],
"druk": [7,4],
"distribueert": [2],
"filepath": [1,0],
"wijz": [2,[0,7]],
"omgekeerd": [2],
"indelingen": [2,7,[3,4,6,8]],
"ja-jp": [2],
"repareren": [2],
"herstellen": [7,[1,2],[0,5],[4,6]],
"frasen": [0],
"opdrachten": [1,7,0,[2,4],5,8],
"vergelijkt": [7],
"java-properti": [0],
"voorkomen": [0,2,7,4,[1,3]],
"dicht": [2],
"openoffic": [0],
"nuttig": [0,[2,7],4,3],
"eigenschappen": [4,[2,3,5],6,[0,7]],
"verborgen": [6,[5,7]],
"scala": [2],
"gevlagd": [1,4,0,[3,5]],
"line": [0],
"link": [0,4,[1,3],5],
"hero": [0],
"praktisch": [[6,7]],
"stuurprogramma": [1],
"git": [2,6],
"tegelijkertijd": [[2,7],[3,4]],
"initieel": [[1,2,6]],
"verwijzen": [[4,7]],
"xx-yy": [0],
"basisbeginselen": [0],
"internetverbind": [1],
"kunnen": [2,0,7,3,[1,5],4,6],
"ingedrukt": [7,0],
"follow": [0],
"lastig": [3],
"wilt": [7,2,0,4,[1,3,6],8,5],
"daarvan": [[0,2,7],[1,3]],
"targetlang": [0],
"frase": [[0,7]],
"perioden": [0],
"optionssetupfilefiltersmenuitem": [0],
"tabulair": [1],
"projectspecifiek": [7,[0,2]],
"altgraph": [0],
"inschakelen": [1,2],
"stats-typ": [2],
"valuta": [7,0],
"zoektermen": [7],
"opzicht": [2],
"vertalingen": [[2,6],5,4,[1,3],0,7,8],
"without": [2],
"begint": [0,2],
"login-naam": [0],
"broncod": [2],
"xml": [2,[0,1]],
"toepassingen": [[0,2]],
"vanaf": [2,0,7,1,[3,5]],
"project-specifiek": [6,[1,7]],
"neutral": [0],
"onvriendelijk": [4],
"groot": [7],
"proxyserv": [2,1],
"eigenschap": [5],
"clausul": [0],
"xdg-open": [0],
"befor": [2],
"weten": [5],
"util": [2],
"verzenden": [2],
"sneltoet": [0,4,7,3,5],
"verloor": [2],
"seri": [0],
"tar.bz": [6],
"leesbaarheid": [1],
"meervoud": [0],
"factoren": [4],
"uitpakken": [2],
"reguliere": [8],
"shebang": [0],
"waar": [0,2,7,5,[1,4],3,6],
"editorskipprevtoken": [0],
"omsluiten": [0,3],
"laat": [[2,3,7],5,4,[0,1],[6,8]],
"bijna": [4],
"soepel": [7],
"laad": [2],
"hoeveel": [[0,3,4]],
"gnu": [2,8],
"helema": [0],
"voorspellen": [1],
"hiervoor": [6],
"snel": [7,3,[2,4]],
"suzum": [1],
"target.txt": [[0,1]],
"temurin": [2],
"d\'espac": [2],
"afwezig": [2],
"correct": [2,0,7,[1,4,5,6]],
"stdout": [0],
"wist": [3],
"traduct": [5],
"doeldocumenten": [[2,4]],
"hoofdgeheugen": [7],
"lijsten": [0,2],
"afhandelen": [[0,2]],
"pad": [2,0,1,5],
"nameon": [0],
"optionel": [[0,2]],
"pak": [[2,6]],
"opgeroepen": [7],
"toetscombinati": [0],
"gotonextnotemenuitem": [0],
"gpl": [0],
"pas": [7,1,[0,5]],
"providerlijst": [1],
"newentri": [7],
"list": [7],
"autocompleterprevview": [0],
"verband": [2],
"positi": [0,7,5,4],
"cursortoetsen": [3],
"gedragen": [0],
"veelvoud": [2],
"ongewenst": [4],
"gespecificeerd": [2,0,1,[3,7]],
"regional": [2],
"medium": [2],
"woordenlijst": [0,5,7,1,4,3,6,2],
"startpunt": [1],
"projectcommittargetfil": [0],
"pear": [0],
"klinker": [0],
"communicatieprobleem": [5],
"po4a": [2],
"japonai": [7],
"omegat.org": [2],
"gevallen": [2,4,[0,7]],
"geïnspireerd": [7],
"hierme": [2],
"xliff-bestanden": [[0,2]],
"maxprogram": [2],
"overweg": [1],
"bevestigingsvenst": [7],
"pdf": [[0,2],4,7],
"sessi": [[2,3,5,7]],
"compatibiliteit": [0],
"productiviteit": [0],
"scripten": [7,4,1,0,8],
"autocompletertabledown": [0],
"editornextsegmentnottab": [0],
"toolsshowstatisticsmatchesmenuitem": [0],
"bladeren": [7,5],
"focus": [2],
"viewdisplaymodificationinfononeradiobuttonmenuitem": [0],
"inhoud": [0,2,7,3,[1,6],4,5,8],
"gebruikersnaam": [2],
"voer": [7,4,[0,1,2,3]],
"inclusief": [[0,2],6,1,[4,7],5],
"officiële": [8],
"schakelt": [5,[3,4,7]],
"per": [[0,6],4,[1,2,7],5],
"write": [0],
"project_save.tmx.bak": [[2,6]],
"converteren": [2,1,3],
"tekens": [8],
"voeg": [0,[2,3],7,6],
"projectaccesswriteableglossarymenuitem": [0],
"install": [3],
"application_shutdown": [7],
"reflecteren": [2],
"autocompletertablelastinrow": [0],
"gui": [7],
"ge-ocrd": [7],
"regexp": [0],
"begrepen": [7],
"dezefd": [0],
"sentencecasemenuitem": [0],
"stemmen": [5],
"editorcontextmenu": [0],
"handleid": [3,4,0,2],
"optionssentsegmenuitem": [0],
"gehouden": [1,[2,4,6,7]],
"bought": [0],
"optionsaccessconfigdirmenuitem": [0],
"inconsistent": [2],
"groen": [7,5,4],
"groep": [0,[1,5,7]],
"charact": [[0,2]],
"framework": [2],
"test.html": [2],
"namen": [[0,5,6]],
"keuzelijst": [7],
"php": [0],
"xxx": [6],
"kiest": [0],
"smalltalk": [7],
"attributen": [0],
"teamleden": [2,3],
"distributielicenti": [0],
"voorafgaan": [0],
"pseudotranslatetmx": [2],
"hoewel": [[0,2],7],
"targetlanguagecod": [0],
"documentati": [[0,2],[3,7]],
"editorprevsegmentnottab": [0],
"linkermarg": [5],
"afsluit": [7,4,1],
"associati": [2],
"bidirect": [4,0],
"toepasselijk": [7,2,3,0],
"hoofdgedeelt": [7],
"locati": [2,0,[3,7],1,[4,6]],
"volg": [2,[0,3,7]],
"vertaalproject": [3,[6,7]],
"leden": [2],
"extra": [1,7,2,0,[4,6],8,[3,5]],
"land": [2],
"alternatief": [7,[1,2,4]],
"lang": [0,[1,3]],
"afronden": [1],
"vóór": [0,1,2,6,7],
"tien": [[2,7],4],
"alternatiev": [[0,4],5,7,[1,2],3],
"projectnam": [0],
"standaardnamen": [6],
"exe-bestand": [2],
"med-pakket": [4],
"landen": [1],
"configdir": [2],
"unicode-blok": [0],
"ondersteunt": [2,7,[0,6]],
"installdist": [2],
"project_save.tmx.tijdstempel.bak": [6],
"a-z": [0],
"gotonextxenforcedmenuitem": [0],
"editordeleteprevtoken": [0],
"onlin": [[0,2,3]],
"gecombineerd": [[0,2]],
"plug-ins": [8],
"javascript": [7],
"mediawiki": [[4,7],[0,3]],
"herhalingen": [4,[0,7]],
"toolkit": [2],
"komt": [0,7,2,3,[4,5]],
"ondersteund": [2,7,3,6,[0,1,4]],
"geheugen": [2,6,[3,7],[0,1,5]],
"join.html": [0],
"opgenomen": [2,0,7,6,5],
"afbreekregel": [1],
"teveel": [2],
"vertrouwd": [0],
"pakketten": [2],
"initialis": [2],
"omegat.kaptn": [2],
"overschreven": [2,3,4,[0,1,7]],
"multi-cel": [7],
"tijdelijk": [5],
"correspondeert": [[0,2,4]],
"pop": [0,4],
"geschiedeni": [4,[0,1],3],
"venster": [7,5,4,1,3,0,6,2,8],
"validati": [4],
"platformen": [0],
"tijd": [3,2,7,[1,4]],
"aanpass": [0,7,[5,6]],
"ervoor": [2,0,[1,3,5,7]],
"verbeteringen": [[0,4]],
"aanpast": [7,[1,2,6]],
"ophield": [3],
"voor": [0,2,1,7,4,3,6,5,8],
"hernoemt": [2],
"kopi": [2,1,[4,6]],
"vertrouwt": [1,7],
"ondersteunen": [2,1],
"googl": [1],
"geweigerd": [1],
"hernoemd": [[0,3]],
"gotoeditorpanelmenuitem": [0],
"annuleert": [0,4],
"scheidingsteken": [1,0],
"vorm": [0,7],
"viewmarkfontfallbackcheckboxmenuitem": [0],
"had": [[0,3]],
"prepar": [0],
"insertcharsrlm": [0],
"sourceforg": [2,0],
"kort": [7,[0,1,3,4,5]],
"han": [0],
"veiligheidsredenen": [[1,7],0],
"semeru-runtim": [2],
"tekstcombinati": [1],
"aantreffen": [2],
"definiti": [[0,1]],
"bestandssysteem": [2],
"paneel": [7],
"editmultipledefault": [0],
"batch": [2],
"mozilla": [[0,2]],
"editfindinprojectmenuitem": [0],
"voetteksten": [0],
"pro": [1],
"falen": [7],
"blijven": [6,[4,7]],
"woordenlijsten": [0,5,7,[3,4],6,2,8,1],
"voert": [[1,2,4]],
"krachtig": [[0,7]],
"keuzemenu": [[0,1]],
"meest": [0,2,4,[5,7],[1,6,8]],
"opgelost": [[0,4]],
"tussentijd": [2],
"updates": [8],
"rechtsboven": [5],
"aangewezen": [[0,6]],
"minimal": [6,1],
"radioknoppen": [7],
"dupliceren": [3],
"kleur": [7,[0,1,6]],
"uitgevoerd": [2,7,0,[1,4]],
"zichtbaar": [[0,6]],
"zowel": [0,7,2,[1,5]],
"duckduckgo.com": [1],
"behulp": [[3,4]],
"kiezen": [[1,7],0,2],
"opgeslagen": [0,[1,2,4,7],5,[3,6]],
"untarren": [2],
"time": [5],
"serviceprovid": [5],
"terugvallen": [4,0],
"foutenrapport": [0],
"kanji": [0],
"besturingssysteem": [0,[2,4],[1,5,7]],
"program": [[0,2]],
"geacht": [6],
"python3": [0],
"hen": [2,[4,5]],
"hem": [6],
"keren": [3,[2,5,6,7]],
"het": [2,0,7,4,5,1,3,6,8],
"cjk-talen": [0],
"tran": [0],
"uitgenomen": [0],
"boekvert": [3],
"pagina": [0,[4,7],[2,3],1],
"schakelen": [7,[0,4],5,2],
"tenslott": [[1,2]],
"iraq": [0],
"dossier": [5],
"brunt": [0],
"verscheiden": [[0,4],6],
"uniek": [7,[4,5],0],
"hard-regeleind": [0],
"houden": [7,[2,3,4,5,6]],
"detecteren": [1],
"beletten": [2],
"voorgeschreven": [0],
"nadat": [2,3,7,0,[1,5]],
"gebracht": [5],
"uiteind": [5],
"dialoogvenst": [[1,7],3,2,[0,4],6],
"nader": [[3,5]],
"uitschakelen": [5],
"doc-license.txt": [0],
"uitvoert": [7,[0,8]],
"thema": [1,7],
"ontgrendelen": [5],
"tevreden": [[2,7]],
"チューリッヒ": [1],
"configuratiemappen": [2],
"taak": [3,2],
"pseudotranslatetyp": [2],
"uitvoerd": [2],
"afhankelijk": [[0,1,5],[2,4],7,3],
"taal": [2,7,1,[5,6],[0,3]],
"bieden": [2,0],
"hij": [6],
"projectclosemenuitem": [0],
"eindnoten": [0],
"viewmarknonuniquesegmentscheckboxmenuitem": [0],
"reparati": [2],
"algemen": [0,7,[1,2],3,5],
"eenvoudige": [8],
"canoniek": [0],
"onder": [2,[4,5],[0,1,6,8]],
"findinprojectreuselastwindow": [0],
"oktob": [1],
"enig": [0,7,[1,3],[2,5]],
"readme.txt": [2,0],
"instellingen": [7,2,0,1,[3,4,6],5],
"veilig": [[1,2],8],
"expressi": [0,7,1,[2,3]],
"bekend": [0],
"languagetool": [4,1,[7,8]],
"source.txt": [[0,1]],
"plaatst": [[0,1,2]],
"grote": [[2,7]],
"files.s": [7],
"tekenklassen": [0],
"geëxporteerd": [6,[4,7],[0,2]],
"alineasegmentati": [7],
"exchang": [0],
"menselijk": [1],
"lre-teken": [0],
"procedur": [2],
"engel": [2,[0,1]],
"currseg": [7],
"uiterlijk": [1,8],
"point": [0],
"general": [2,8],
"duren": [[0,1,3]],
"onthoud": [2,0,7],
"brengen": [0],
"initiël": [7,0,2,1],
"projectgeheugen": [[0,7]],
"noodzakelijk": [2,[3,7]],
"zoekfuncti": [0],
"waaraan": [3],
"autocompletertrigg": [0],
"objectmodel": [7],
"kwaliteitsbeh": [4],
"gesorteerd": [5,7],
"acquiert": [1],
"gepaard": [[0,1]],
"account": [2],
"opslagplaats_voor_alle_omegat_teamprojecten": [2],
"dhttp.proxyhost": [2],
"computerwerk": [8],
"uitgaven": [2],
"ontwikkeld": [7],
"faciliteren": [3],
"voegt": [4,7,2,[1,5,6]],
"editorprevseg": [0],
"toewijzen": [2,[0,1,4]],
"alineascheid": [5],
"gewenst": [2,5,[0,1]],
"eracht": [[0,5]],
"steld": [3],
"manipuleren": [5],
"beschikbaar": [2,7,0,1,4,5,3],
"opslag_voor_alle_omegat_bronnen_van_teamproject": [2],
"a-za-z0": [0],
"stroomlijnen": [3],
"you": [0],
"omvatten": [[2,3,5]],
"synchroniseren": [2,7,[3,6]],
"www.apertium.org": [1],
"verwijzingsbestanden": [6,7],
"gewoonlijk": [0,2],
"stelt": [[0,4,7],2,1,5],
"schakel": [0,[1,7],4,[2,6]],
"project_save.tmx.tmp": [2],
"tags": [8],
"configur": [5,2],
"bronmap": [2,0],
"voordeel": [[0,7]],
"overeenkomt": [0,3,1],
"hoe": [3,1,[0,7],[2,5],8],
"mega": [0],
"zurich": [1],
"feitelijk": [3,[0,2]],
"空白文字": [2],
"resultaten": [7,[0,3,4,5],[1,2]],
"velden": [[0,4],7],
"extensi": [0,2,6,[1,4,5,7]],
"optionsworkflowmenuitem": [0],
"releas": [2,0],
"vervangt": [4,7],
"conventi": [3,0],
"overslaan": [0],
"noodzaak": [[2,7]],
"goede": [8],
"segmentnumm": [[0,4]],
"boomstructuur": [6],
"bijgewerkt": [[1,2,3,6,7]],
"dictroot": [0],
"meer": [0,7,[1,3],[2,5],6,4],
"benad": [2,7],
"instellen": [2,6,[0,4,7],[1,8]],
"tekstblokken": [7],
"visueel": [4],
"ieder": [2],
"leggen": [0],
"bestandslocati": [7,2],
"selecteert": [4,7,[1,3]],
"overeen": [0,7],
"handelt": [1],
"zwakke-richt": [0],
"indienen": [2,[0,4]],
"hyperlink": [5],
"autocompletertableleft": [0],
"hostservic": [2],
"stijleffecten": [0],
"gehel": [0,[1,2],[3,4,5,6,7]],
"beid": [[0,7],[1,2]],
"tijden": [7,[0,1,4]],
"editorlastseg": [0],
"file-source-encod": [0],
"probleem": [4],
"zoekacti": [7,1,0,4,3,[5,6]],
"waarbij": [[0,3]],
"some": [2],
"vermijdt": [2],
"systeembre": [2],
"gedeelt": [0,3,2,7,[1,4]],
"vereist": [[1,2],0,[3,4,6,7]],
"gerelateerd": [[2,6],[3,5]],
"rle-teken": [0],
"vaalgrij": [4],
"anderen": [8],
"passen": [0,7,2,[1,3],5,4],
"allema": [[3,7]],
"grieks": [0],
"alpha": [2],
"eéntalig": [0],
"markeren": [0,[1,4],7,5,[3,6,8]],
"大学院博士課程修了": [1],
"just": [0],
"eerstekla": [7],
"hostnaam": [0],
"editexportselectionmenuitem": [0],
"afhandelt": [0],
"bestaan": [4,[0,7]],
"home": [0,2],
"disable-location-sav": [2],
"print": [2],
"varianten": [0],
"gedeeld": [2,7,[0,3,5,6]],
"bestaat": [0,2,[4,5,6,7],3],
"projectaccesstargetmenuitem": [0],
"nagenoeg": [2],
"waarnaar": [7],
"uitzonderlijk": [2],
"webadressen": [5],
"iana": [0],
"opslag_voor_alle_omegat_teamprojecten": [2],
"hun": [[0,2],7,[1,3],[4,5,6]],
"hoger": [1,0],
"geassocieerd": [0,[1,4],2,[3,5,7]],
"gekleurd": [5],
"med-projecten": [4],
"alternatieven": [[0,5]],
"raden": [2],
"sleutelwoorden": [7],
"accepteert": [[2,6]],
"strikt": [2],
"bouwen": [2],
"aligndir": [2,7],
"system-host-nam": [0],
"tekenklass": [0],
"stadium": [7],
"startpunten": [0],
"mymemory.translated.net": [1],
"gekopieerd": [5,4,[0,1,2,7]],
"eentalig": [7],
"creat": [[0,7]],
"python": [7],
"alleen": [0,7,[1,2],[3,4],5],
"vertelt": [2],
"uitgebreid": [2,0],
"negeer": [0],
"afbreken": [0,1,2],
"verzamelen": [2],
"opslagplaats_voor_omegat_teamproject": [2],
"verwachten": [0],
"betreft": [7],
"file": [7,2,[0,5]],
"collectief": [0],
"maakt": [2,3,0,[4,7],6,5,1],
"klein": [0,4,7,6],
"hoeven": [0,[2,3,7]],
"nodig": [2,[0,3],[6,7],1],
"voorbereidingen": [2],
"bent": [7,2,3,[1,5]],
"individueel": [[0,7]],
"fijn": [5],
"menu": [4,0,5,7,3,1,8,2,6],
"richt": [0,5],
"tegelijk": [6],
"gebundeld": [[1,2]],
"waarschuw": [7,0,[1,2],3,4,6,5],
"omstandigheden": [2],
"mene": [5],
"return": [0],
"bidirectionel": [4],
"invoke-item": [0],
"coder": [0,7],
"vermijden": [0,3],
"meerder": [0,5,[2,7],1,[3,4]],
"geannuleerd": [0,[4,7]],
"geïdentificeerd": [1,[2,3]],
"spati": [7,0,3,[1,4,5]],
"bevat": [6,0,2,7,5,[3,4],1],
"bekijken": [0,3],
"source-pattern": [2],
"host": [2],
"soort": [1],
"beheerstaken": [2],
"flagrant": [0],
"speciaal": [0,2],
"autocompletertablepagedown": [0],
"tekstbestanden": [0,6],
"modus-opti": [2],
"programmeerstijlen": [7],
"linkerk": [7,[4,5]],
"problematisch": [3],
"vallen": [0],
"task": [2],
"voorpagina": [2],
"paradigma": [7],
"xliff": [2,0],
"true": [0],
"dubbel": [0],
"submap": [2,7],
"identific": [2],
"groovi": [7],
"fran": [2,1,7],
"ontwikkelen": [2],
"best": [5,[1,3],[2,4,6,7]],
"robuust": [2,3],
"codering": [8],
"taalinstellingen": [1],
"panelen": [3],
"koffi": [3],
"oplossen": [2,[3,6,8]],
"sterk": [[0,7]],
"toewijst": [[3,7]],
"vuistregel": [7],
"optreden": [0],
"segmenten": [7,4,0,1,6,5,3,2,8],
"voelt": [2],
"processen": [2],
"messageformat": [1,0],
"nakijkt": [3],
"eigen": [2,7,0,[1,5]],
"maand": [[0,2]],
"master": [2],
"betrekk": [2],
"toegestaan": [0],
"groott": [1,7,5],
"geschreven": [7,[0,2,4,6]],
"blanco": [0],
"aanbevolen": [[0,2,7]],
"vensters": [8],
"writer": [0],
"dalloway": [1],
"rubi": [7],
"resource-bundl": [2],
"gehost": [[1,2]],
"eenvoudig": [0,3,2,1,6,7],
"binnen": [0,2,[3,7]],
"editorselectal": [0],
"hoofdmap": [2],
"tekstdecorati": [7],
"globale": [8],
"advanced-plan": [1],
"bijvoorbeeld—": [5],
"runner": [7,0],
"hoofdstukken": [3],
"zal": [[0,2],1,7,6,3,4,5],
"actiev": [[1,4,5,6]],
"omegat-default": [2],
"besliss": [6],
"user.languag": [2],
"regex": [0,3],
"meta": [0],
"beëindigen": [7],
"aanliggend": [5],
"functionel": [7],
"global": [7,0,1,4,[2,3]],
"racin": [5],
"regel": [0,1,7,5,2,[4,6]],
"sorteervolgord": [4],
"toegankelijk": [7,[4,6]],
"plug-in": [2,0,1,3],
"verbeterd": [7],
"beschikbar": [7,[1,2,5],0,3],
"ip-adr": [2],
"verifiëren": [0],
"geweest": [3],
"sneller": [3],
"ibm": [[1,2]],
"moeten": [0,2,1,[6,7],4,[3,5]],
"beheert": [[0,2]],
"duizendtalscheid": [0],
"bi-drectionel": [0],
"heropen": [2],
"ongeluk": [[0,1]],
"ontwikkelversi": [2],
"brondocumenten": [[0,7]],
"readme-bestanden": [0],
"verbind": [2,5],
"lopen": [0],
"verplaat": [[3,5]],
"aanpassingstoetsen": [3],
"accentueert": [7],
"broncontain": [0],
"definieert": [1,2,0],
"zet": [[0,2,6]],
"zes": [3],
"ide": [2],
"omegat-cod": [2],
"zouden": [2,1,0,[6,7],5],
"beheerd": [4,[2,3]],
"deugdelijk": [2],
"idx": [6],
"conflict": [2],
"filterinstellingen": [7],
"aanwezig—eerst": [1],
"liggen": [[1,3]],
"bestandsinhoud": [0],
"regio": [0],
"autocompleterconfirmandclos": [0],
"how-to": [3,[2,6],[0,4],7,5,[1,8]],
"symbolen": [0,5],
"projectaccesscurrentsourcedocumentmenuitem": [0],
"linux": [0,2,4,5,[1,3,7]],
"iet": [[0,1,2,5]],
"alfanumeriek": [0],
"brontermen": [[0,4]],
"zeggen": [2],
"linux-install.sh": [2],
"proberen": [[2,7],0],
"besturingsteken": [0,4],
"file.txt": [2],
"industrieel": [3],
"handleidingen": [0],
"openxliff": [2],
"vereisten": [2],
"transformati": [1],
"ifo": [6],
"behandelen": [0,[1,5]],
"excit": [0],
"gebruiken": [2,0,7,4,1,3,[5,6],8],
"ondank": [7],
"optionsmtautofetchcheckboxmenuitem": [0],
"herstel": [1],
"xx.docx": [0],
"verbeteren": [0],
"consist": [4],
"onzichtbaar": [0],
"schrijft": [7],
"zie": [2,[0,6]],
"editorshortcuts.properti": [0],
"schrijven": [7,2,4,5],
"zij": [0,7,2,1,[3,5,6],4],
"gerechtelijk": [0],
"golvend": [1],
"zin": [0,7,3,1,[2,4]],
"patroon": [0,1,7,2],
"poge": [1],
"tbx-woordenlijsten": [1],
"tegenovergesteld": [0],
"sturen": [2,[1,3]],
"verloren": [4,3],
"opneemt": [0],
"dankwoorden": [4],
"steed": [[0,7],[4,5]],
"tmotherlangroot": [0],
"viewmarknotedsegmentscheckboxmenuitem": [0],
"verkrijgen": [[2,7],[0,1,4]],
"event": [0],
"imperatiev": [7],
"rainbow-ondersteund": [2],
"onderwerp": [[0,2]],
"gotomatchsourceseg": [0],
"tekenreek": [7,1,[0,4]],
"behandelt": [7],
"excel": [0],
"comma": [0],
"systeemvariabelen": [[1,7]],
"runn": [7],
"verleden": [2],
"verbreek": [2],
"doelregio": [0],
"runt": [0],
"stardict": [6],
"omegat.l4j.ini": [2],
"span": [0],
"behandeld": [0,5],
"aantal": [7,0,5,2,4,[3,6],1],
"aanroept": [4],
"prefer": [0],
"proportioneel": [1],
"zoekresultaten": [7],
"wegschrijft": [2],
"ドイツ": [7,1],
"inbeddingen": [0],
"uitmaken": [0],
"niet": [0,2,7,3,1,4,5,6],
"voorkomend": [2,[5,7]],
"traag": [2],
"lager": [0,[5,7]],
"editselectfuzzy3menuitem": [0],
"ontbrekend": [4,0,3,[2,5]],
"fals": [0,2],
"project.projectfil": [7],
"blauw": [7,5],
"klikken": [7,4,5,2,[3,6]],
"exporteren": [0,[2,7],[1,3,4]],
"weergaven": [[2,7]],
"notificati": [5],
"hoofdlett": [0,4,[2,5]],
"instanti": [2,4],
"zin-segmentati": [[0,7]],
"sjabloon": [1,0,8],
"compatibel": [2,[0,1,7]],
"vragen": [2,0,[1,3,5]],
"rechten": [7],
"woord": [0,7,[4,6],[1,5]],
"frequent": [0,[4,7]],
"rechter": [[5,7]],
"sleutel": [7,[1,2]],
"introducti": [3,[2,8]],
"produceren": [[2,7]],
"herlaadt": [4,7],
"zoekt": [0,[1,7]],
"public": [2,8],
"actief": [[0,1]],
"doeltekst": [[1,4,7]],
"betrouwbar": [6,2],
"veroorzaken": [4,0],
"tmx2sourc": [2,[0,6]],
"zou": [0,2,7,[3,6],[4,5],1],
"vertaalt": [[2,3,5],[1,7]],
"mogelijkheid": [1,[2,5]],
"beetj": [4],
"mogelijkheden": [[2,3,7]],
"aanroep": [4],
"vertaald": [7,0,2,[3,4],5,[1,6]],
"indeling": [8],
"dient": [2,[0,3]],
"dhttp.proxyport": [2],
"repar": [2],
"categorieën": [0,8],
"haakj": [0,1],
"ingeschakeld": [[1,5],0,4,3],
"subrip": [2],
"sluiten": [0,7,[2,4],3],
"configuratiemap": [0,2,1,[4,7,8]],
"document-eigenschappen": [0],
"duidelijk": [2],
"score": [1,7,6],
"nieuw": [0,[2,7],3,4,6,1,5,8],
"verbinden": [2],
"koppelingen": [0],
"bestandsextensi": [0,2],
"appendix": [7,0,1,4,2,[3,5,6]],
"navigati": [[3,5],6,[2,4]],
"submappen": [[0,2],[6,7],4],
"raw": [2],
"vooraf": [0,1,[2,3,6]],
"ongevoelig": [0,7],
"decoratiev": [3],
"toetsen": [0,[7,8],[4,5]],
"breedt": [7],
"vrijheden": [8],
"flexibiliteit": [3],
"heeft": [2,7,0,1,4,[3,5],6],
"specificati": [0],
"tarball-archief": [6],
"bron-bestandsnaam": [0],
"standaardwaard": [7],
"aan": [7,2,0,5,3,1,[4,6],8],
"dollar": [0],
"hebben": [[0,7],2,1,4,5,3,6],
"maxima": [[2,6]],
"persoon": [7,2],
"aspect": [3],
"appendic": [0,[3,6]],
"unbeliev": [0],
"gescheiden": [0,[3,7]],
"close": [7],
"bepaal": [1],
"abc": [0],
"bestaand": [2,[0,3,5,7],6,1],
"toolbar.groovi": [7],
"controleert": [0,[2,3,4,7]],
"doelbestanden": [0,2,4,7,6],
"nabewerken": [7,[0,1],8],
"originel": [2,0,7,3,[1,5]],
"voorzorgen": [2],
"witruimt": [0,4],
"iso": [[0,2]],
"isn": [0],
"rechterbovenhoek": [5],
"optionspreferencesmenuitem": [0],
"helpen": [[3,8],[0,2,4]],
"binden": [7],
"behoudt": [[2,6]],
"soft-return": [0],
"post": [0],
"glossary.txt": [[2,6],[0,4]],
"beveilig": [2],
"geïllustreerd": [3],
"bron-tekstterm": [1],
"taalpatroon": [1,0],
"add": [2],
"chines": [1],
"werken": [2,0,7,6,[1,3,4]],
"apostrof": [0],
"equival": [7,1,[0,2,5]],
"geannoteerd": [3],
"rfc": [7],
"parsen": [[5,7]],
"aannemen": [7],
"rfe": [7],
"doelmap": [0],
"shell": [0],
"port": [2],
"segmentatiedoeleinden": [0],
"entry_activ": [7],
"botsen": [4],
"optionsautocompleteshowautomaticallyitem": [0],
"poortnumm": [2],
"gotoprevxautomenuitem": [0],
"hiërarchi": [[2,6]],
"docx-documenten": [7],
"resterend": [[5,7],[2,4]],
"meegeteld": [4],
"ruimt": [5],
"spelfouten": [[1,4,7]],
"zorgen": [2,0,[1,3]],
"ishan": [0],
"pasta": [0],
"heap-gewijz": [7],
"uitnemen": [[1,7]],
"notati": [0,1],
"cursorpositi": [4],
"teamproject": [2,4,6,[0,7,8],5],
"wachtwoord": [2,1],
"prioriteit": [4,1,[2,3]],
"produceert": [2],
"doelbestand": [0,4,7],
"versie-control": [2],
"alsof": [[2,7]],
"clone": [2],
"targetlanguag": [[0,1]],
"afstand": [2,6,4,[5,7]],
"collega": [5],
"valideert": [0],
"properti": [2],
"scriptbewerk": [0],
"editselectfuzzyprevmenuitem": [0],
"number": [2],
"rij": [7,4,0],
"defect": [2],
"standaardmethod": [5],
"naar": [0,2,7,4,3,5,1,6,8],
"laatst": [0,4,7,[1,2,5,6],3],
"vcs-cliënt": [2],
"algorithm": [4,0],
"daaraan": [0],
"twijfelt": [7],
"naam": [0,1,[2,3],5,7,6],
"script": [7,0,2,4,1,[3,6]],
"verwijz": [2,0,[3,7],6,4,5],
"krijgen": [7,[2,3],[0,6],[1,4,5]],
"reken": [[1,4,6],[2,7]],
"aanpassingstoet": [0,4],
"identiek": [4,7,2,[1,6],[0,5]],
"attribuutwaarden": [0],
"netwerk": [2],
"bewerk": [7,0,[2,3]],
"objecten": [2],
"local": [2,0,[6,7]],
"negeert": [[0,5,6]],
"tm\'s": [8],
"eruit": [[3,7]],
"verwijd": [0,7,2,[1,6]],
"patronen": [0,[1,7],2],
"lokaliseren": [2],
"machinevertaling": [8],
"zoiet": [7],
"rle": [[0,4]],
"genomen": [1],
"login-id": [1],
"gevorderd": [7],
"rlm": [0,4],
"onjuist": [6],
"introductie": [8],
"terminologie-bestanden": [0],
"brugtaal": [2],
"afgekort": [0],
"gebruik": [7,[0,2],3,1,5,4,6],
"samenvoegingen": [0],
"beïnvlo": [4],
"extreem": [2],
"resulteert": [2],
"c-x": [0],
"mode": [2,7],
"vertaalgeheugen": [2,3,7,6,4,5,0,1],
"modi": [5],
"gebi": [[5,7]],
"sommig": [2,0,1,[4,5,6,7],3],
"oefenen": [0],
"engels": [2,0],
"afbrekingen": [1],
"toolsshowstatisticsstandardmenuitem": [0],
"vrije": [2],
"identificeert": [1],
"all": [7,2,1,0,4,6,3,5],
"read": [0],
"geregistreerd": [[2,3,7]],
"c.t": [0],
"alt": [0,4],
"pijltj": [[3,7]],
"boven": [7,1,[2,4],[0,3,5]],
"real": [5],
"tegen": [[2,3,5]],
"unit": [0],
"brontekst": [1,5,4,7,0,3],
"agressief": [[0,4]],
"officiël": [0],
"zulk": [[5,6]],
"commerciël": [3],
"hoofdvenst": [5,7,4,[0,1]],
"registrati": [1],
"voorafgegaan": [0],
"bedoel": [0],
"geef": [[2,7]],
"slaan": [[6,7]],
"tkit": [2],
"and": [1],
"txml-bestanden": [2],
"opmaak": [3,[0,5,7]],
"aangeroepen": [0,7],
"slaat": [4,2,0],
"moet": [2,0,1,[3,7],6,4],
"reproduceren": [7],
"ant": [[2,7]],
"werkstromen": [2],
"daarboven": [0],
"waard": [0,7,[1,2,4]],
"opgehaald": [1],
"leiden": [2],
"helplastchangesmenuitem": [0],
"geavanceerd": [[0,7]],
"omegat.ex": [2],
"enigszin": [3],
"exitcod": [0],
"sourcetext": [1],
"dusverr": [2],
"willekeurig": [4,2],
"brengt": [5],
"voorgesteld": [0],
"werkbladnamen": [0],
"tm-bestand": [1],
"jar": [2],
"api": [7],
"editselectfuzzy2menuitem": [0],
"niet-ascii-teken": [0],
"oplevert": [7],
"gewijzigd": [1,[3,7],6],
"reed": [2,[3,6],[4,7]],
"reek": [0,3],
"overeenkomst": [0,4,1,5,6,7,3,[2,8]],
"dergelijk": [2,1,[3,7],0,4],
"markeringen": [0,[2,7]],
"geel": [4],
"geaccentueerd": [4,7,5],
"geen": [0,1,7,2,4,3,6],
"letter": [0,4,7,1,3,2],
"zult": [[2,7],6,[0,3,4]],
"acti": [7,4,2,5,3,1],
"nooit": [0,[3,4,6]],
"plaatsen": [[0,2,7],[1,4,5]],
"editornextseg": [0],
"alineablokken": [3],
"editselectfuzzynextmenuitem": [0],
"gotonextxautomenuitem": [0],
"klembord": [4],
"read.m": [0],
"uitdagend": [0],
"blokken": [[0,7]],
"knoppen": [7,4,[0,3]],
"cloud.google.com": [1],
"taken": [2,7],
"readme.bak": [2],
"arg": [2],
"functionaliteit": [2,[1,7]],
"logogram": [0],
"bezig": [1],
"vice": [7],
"vóórdat": [2,[0,4],[1,3,7]],
"forceren": [0,[1,4,7]],
"benodigd": [2],
"gemaakt": [2,3,7,[0,6],[1,4],5],
"call": [0],
"bestand": [2,0,7,6,4,3,[1,5]],
"waarin": [7,0,3,4],
"grafieken": [0],
"specificeren": [0,2,7],
"verlaat": [[1,3,4,6]],
"doeltaal": [[0,2],3,[1,7],[4,6]],
"alinea-scheidingen": [[0,1,4,5]],
"toolsshowstatisticsmatchesperfilemenuitem": [0],
"fragmenten": [[0,3]],
"run": [7,0,2],
"tweemaal": [3],
"intervallen": [1],
"ervan": [6,4,0,[3,7]],
"veroorzaakt": [0],
"editorshortcuts.mac.properti": [0],
"ruw": [7],
"vier": [0,[2,4,7]],
"bestandsfilters": [8],
"titlecasemenuitem": [0],
"graag": [0],
"totalen": [4],
"opmaakteken": [4,0],
"editcreateglossaryentrymenuitem": [0],
"verdel": [7],
"verder": [[0,3],6,[2,4]],
"introduc": [7],
"altijd": [0,2,[1,6],4,[3,7]],
"多和田葉子": [7],
"vertrouwen": [0,[2,4]],
"verwezen": [2],
"name": [0,[2,5]],
"invoer": [4,2],
"woordenboek": [1,6,[3,7],[4,5],[0,8]],
"proxyhost": [2],
"doelstatus": [0],
"book": [0],
"vermelde": [8],
"handig": [[2,7],[0,1,3,5]],
"systemen": [2,4,[0,1]],
"comput": [2,3],
"ingebouwd": [1],
"simpel": [0],
"editortogglecursorlock": [0],
"verschillen": [1,[0,3]],
"associ": [2],
"onveranderlijk": [6],
"new_fil": [7],
"notiti": [0,7,[3,4,5]],
"situati": [2],
"project-eigenschap": [2,3],
"target": [1,[4,7],6,3,[0,8]],
"achter": [0,5],
"bepalen": [[0,1,7]],
"vermeldt": [[1,5,7]],
"config-dir": [2],
"beschrijvingen": [[3,4]],
"editorskipprevtokenwithselect": [0],
"waarop": [0,[4,5]],
"waarom": [0],
"termbas": [0],
"modus": [[2,7],5,4],
"acteren": [[0,6]],
"afbreekstreepj": [0],
"vijf": [1],
"parameterbestanden": [2],
"verschijnen": [0,1,7,[2,3]],
"case": [0],
"item": [7,[4,5],0,1,3,6],
"voordelen": [2],
"verbergen": [7,[0,5],[1,2,3,4]],
"geïnverteerd": [1],
"definiëren—waarschijnlijk": [5],
"gegenereerd": [0],
"waren": [2],
"kijk": [[3,7]],
"auteur": [[3,4]],
"targettext": [1],
"respectievelijk": [[0,6],2],
"waarov": [3],
"geconfigureerd": [1,[4,5]],
"bronseg": [7,5,[1,2,4],6],
"onthouden": [2,[6,7],[0,3,4]],
"onwaarschijnlijk": [3],
"oproepen": [5,0],
"orang": [0],
"toevoegt": [7,[0,6]],
"wezen-segmenten": [7,5],
"gebruikershandleid": [0,4,[2,3,8]],
"oranj": [[5,7]],
"leest": [[1,7]],
"compil": [7],
"grijz": [4],
"lokal": [2,7,1,0,4,5],
"edittagpaintermenuitem": [0],
"schijven": [7,2],
"onderhoudstaken": [2],
"gewend": [4],
"unicod": [0,4],
"viewmarknbspcheckboxmenuitem": [0],
"verwerken": [[1,4],[0,2,3,8]],
"groeperen": [5],
"projectmedcreatemenuitem": [0],
"associëren": [0,[2,3]],
"computer-assist": [3],
"webinterfac": [2],
"minder": [2],
"presentati": [0],
"whitespac": [2],
"gericht": [0],
"letterlijk": [0,1],
"msgstr": [0],
"adresboek": [6],
"hoofdlettergevoelig": [7],
"vind": [7],
"sorteren": [0,1],
"gere": [2],
"voetnoten": [0],
"nationalité": [1],
"bevatten": [0,7,[2,3,6],5,[1,4]],
"focussen": [3],
"daili": [0],
"fouten": [[3,7],4],
"aangepaste": [8],
"omegat.project": [2,6,3,[1,5,7]],
"excludedfold": [2],
"targetcountrycod": [0],
"waarme": [2,0],
"insert": [0,5],
"belangrijkste": [8],
"arabisch": [0],
"uitsluit": [2,7],
"streng": [4],
"getal": [[0,1,2],[6,7]],
"totdat": [2,[1,3,7]],
"scripts": [8],
"plakken": [[4,5]],
"rest": [0,[2,3]],
"lengt": [1],
"noodmaatregelen": [2],
"gesegmenteerd": [0,[3,7]],
"consol": [2],
"toegestan": [2],
"haalt": [2],
"gesteld": [1],
"itokenizertarget": [2],
"viewmarkwhitespacecheckboxmenuitem": [0],
"voltooien": [2],
"equivalent": [2],
"secti": [0],
"overwegingen": [2],
"asterisk": [0],
"detecteert": [4,[1,2]],
"complet": [0],
"bak": [2,6],
"jokerteken": [7,[0,2]],
"herschikken": [0],
"berekend": [1,5,7],
"complex": [0,7,2],
"neergezett": [5],
"jre": [2],
"waaruit": [2],
"niveaus": [3],
"terwijl": [5,[0,4],[1,2,3,7]],
"seconden": [1],
"vaak": [3,[0,2,7]],
"fysiek": [2],
"identificatie": [8],
"onderstrepen": [[1,5]],
"grafisch": [2],
"project_save.tmx.jjjjmmdduumm.bak": [2],
"auteursrechten": [4],
"lidwoord": [0],
"scrollen": [[3,5]],
"zinnen": [0,3,7],
"nieuwer": [4],
"alllemand": [7],
"variabelen": [1,0,8,7],
"delen": [2,7,[0,1,6],[3,5],4],
"distribueren": [8],
"delet": [0],
"uitgelijnd": [[4,7],[0,1,2]],
"projectaccessglossarymenuitem": [0],
"javadoc": [7],
"kopieer": [2,7],
"snelst": [8],
"woordenboeken": [1,3,6,5,4,[0,8],[2,7]],
"indicati": [0],
"brontaal": [0,1,[2,5,6,7]],
"efficiënt": [3],
"herhal": [5,7],
"set": [1,0,7,[2,4,6],3],
"handelen": [[1,2,7]],
"kantoorpakketten": [2],
"incorrect": [7],
"wijzigd": [1],
"balis": [5],
"breid": [2],
"ofwel": [0,2,[1,3,6,7],4],
"wordt": [0,2,7,6,4,1,5,3,8],
"punctuati": [0],
"beschrijv": [4,[0,1,7]],
"tabel": [0,1,4,5],
"verliest": [2],
"wijzigt": [[0,7],4,[3,6]],
"gelden": [0],
"project.sav": [2],
"woordenlijstbestanden": [2],
"back-upbestand": [2],
"offic": [0,3],
"repositories.properti": [[0,2]],
"frequenti": [2],
"beginnen": [0,3,7,2],
"aangegeven": [4,0],
"hiervan": [2],
"dialoogvensters": [8],
"repositories": [8],
"projectsavemenuitem": [0],
"contact": [5],
"parst": [0],
"xmx6g": [2],
"cursief": [[0,3,7]],
"autocompletertablefirstinrow": [0],
"licentie": [8],
"associeert": [0],
"omlaag": [0],
"combinati": [0,[1,4,7]],
"zinnig": [0],
"geplakt": [4],
"omegat.project.jjjjmmddhhmm.bak": [2],
"vrijheid": [8],
"tmautoroot": [0],
"tekstbewerk": [[0,7],6,[2,3]],
"helpt": [3],
"cursor": [5,4,[0,3],7],
"instructi": [2,0,7],
"insertcharslrm": [0],
"gebruikersinterfac": [2,[0,1],[3,5,6,7]],
"deel": [[0,2,5],3],
"scheid": [[1,4]],
"jar-pakket": [2],
"gemakshalv": [2],
"vertaalprogramma": [5],
"verschijnt": [5],
"doelcod": [0],
"over": [0,1,7,[2,5],4,[3,6]],
"tabelweergav": [5],
"vastloopt": [2],
"vermeden": [7],
"zelf-gehost": [2],
"vrij": [0,[2,3,4,5]],
"breed": [2],
"begon": [7],
"informati": [5,2,[0,1,4,7],[3,6]],
"voltooid": [7,[0,2]],
"foundat": [2],
"statistisch": [7],
"targetroot": [0],
"subset": [[0,2]],
"opeenvolgend": [7,[0,1]],
"bij": [2,[0,4],[1,3],7,6,5],
"select": [7,1,4,[2,5],3,0],
"laadt": [[1,7],6],
"bin": [0,[1,2]],
"upgraden": [2,7],
"apertium": [1],
"bis": [0],
"kaptain": [2],
"meta-inf": [2],
"begrijpt": [2],
"output": [2],
"projectopenmenuitem": [0],
"autom": [2],
"registreert": [1,2],
"zeer": [7,0],
"accentu": [7],
"zichzelf": [0],
"vergeleken": [3],
"sla": [6,0,[3,7]],
"conflicten": [2,[0,3]],
"tekstinhoud": [[1,7]],
"variëren": [2,0],
"uitzonderingen": [1,0,2],
"projectmap": [6,[2,7],0,3,4,[1,8]],
"voorwaart": [0],
"begin": [0,[1,2,3,4,5,6,7]],
"alineascheidingen": [5],
"hetzelfd": [7,[0,2],[1,4],[3,5]],
"viewmarktranslatedsegmentscheckboxmenuitem": [0],
"valu": [2],
"bidi-markeringen": [7],
"vals": [1],
"startprogramma": [2],
"doorsturen": [2],
"ilia": [2],
"toegangstoken": [2],
"waarschuwingen": [2],
"programma": [[2,5],0,1,[3,7],4],
"uxxxx": [0],
"gereviseerd": [3],
"hier": [1,5,0,2,7,6,[3,4]],
"d.i": [[0,2]],
"macos": [8],
"editselectfuzzy1menuitem": [0],
"upgrad": [1],
"bibliotheken": [0],
"herkent": [[2,3,4,7]],
"tijdstempel": [0,[2,6]],
"hide": [5],
"uzelf": [3],
"platt": [0],
"comptabiliteit": [0],
"opgestapeld": [7],
"dag-gebruik": [2],
"herkend": [0,[5,6,7],[1,2]],
"autocompleterlistpagedown": [0],
"auto": [4,[0,6],2,1],
"karakteriseren": [0],
"notepad": [5,3,4,[0,8]],
"document.xx.docx": [0],
"editorskipnexttokenwithselect": [0],
"som": [0,2],
"bezocht": [4,6],
"gedetecteerd": [4],
"download": [2,[6,7]],
"versnellen": [2],
"oracl": [0],
"editortoggleovertyp": [0],
"universiteit": [1],
"gradlew": [2],
"afgedwongen": [6],
"zeker": [3],
"kant": [2],
"toetsenborden": [0],
"relevant": [0,[1,2,3,4]],
"ssh-authenticati": [2],
"viewmarklanguagecheckercheckboxmenuitem": [0],
"rechterk": [7,5,0,3],
"bestandsnamen": [0,7,3,2],
"toegepast": [1,2],
"underscor": [0],
"versiesystemen": [2],
"gedupliceerd": [[2,7]],
"afbrek": [0],
"switch": [0],
"denk": [2],
"unicode-blokken": [0,8],
"total": [7],
"óf": [0],
"bundl": [1,[0,2]],
"kan": [2,7,0,4,5,1,3,6],
"zelf": [[0,2],7,3,1,[4,6],5],
"macro": [7],
"src": [2],
"gigabyt": [2],
"control": [4,[0,1,2,7],3],
"vertaalprojecten": [[1,3]],
"no-team": [2],
"helft": [1],
"lissens": [0],
"nakijken": [3,2,8],
"srx": [[0,6]],
"handelsmerken": [5],
"installeren": [2,3,1,0,[6,8]],
"genoemd": [0,1,2],
"gedekt": [2],
"ssh": [2],
"teruggeven": [0],
"kantoorpaketten": [2],
"back-up": [2,6,1,7],
"environ": [2,0],
"beveelt": [2],
"usb-schrijf": [2],
"friend": [0],
"verschillend": [3,2,4,[0,7],5,1,6],
"keuzevak": [7],
"taalcontrol": [1,[0,4]],
"pinpoint": [7],
"afgesplitst": [3],
"sta": [4,1],
"gedurend": [2,[3,6,7]],
"engels-japans": [2],
"splitsen": [7,[0,3,4]],
"individuel": [0,[2,7]],
"beneden": [7,[0,1,3]],
"collecti": [6],
"vaardigheden": [2],
"kde": [2],
"bestandsbeheerd": [4,2,[6,7]],
"verhogen": [2],
"slepen": [5],
"rapporteren": [2],
"volgen": [3,[0,2]],
"statistieken": [4,6,2,1,0,7],
"gebeurtenissen": [7],
"stopt": [1],
"dynamisch": [7],
"advi": [2],
"vertal": [2,5,3,0,4,7,1,6],
"inloggegeven": [1,2,5],
"configuratiebestanden": [[1,2,4]],
"languag": [7,2],
"hieruit": [[0,4]],
"bovenzijd": [[1,2,3,5,7]],
"gedefinieerd": [0,2,7,5,1,[4,6],3],
"optionsglossaryfuzzymatchingcheckboxmenuitem": [0],
"derd": [2,[5,6],[0,3]],
"key": [[0,2]],
"msgid": [0],
"svn": [2,7,6],
"waarschijnlijk": [2],
"store": [3],
"maken": [2,0,7,3,4,6,1,[5,8]],
"omegat-license.txt": [0],
"beveiligd": [[1,3]],
"stori": [0],
"bijzond": [0,[2,3,7]],
"stoppen": [7],
"vast": [[0,5,6]],
"editreplaceinprojectmenuitem": [0],
"but": [0],
"plaat": [0,2,4,7,[3,6],[1,5]],
"editordeletenexttoken": [0],
"gevoelig": [0],
"express": [0],
"afsluiten": [4,[0,1,2]],
"horizontal": [0],
"spellingproblemen": [4],
"afbeeldingen": [[0,8]],
"vullen": [1,[4,6,7]],
"variant": [2],
"terughalen": [2],
"voorkeursinstellingen": [3],
"verdubbelen": [0],
"gotoprevioussegmentmenuitem": [0],
"geaccepteerd": [7,2,[1,6]],
"eenheden": [0],
"gotopreviousnotemenuitem": [0],
"stderr": [0],
"gezegd": [0],
"editredomenuitem": [0],
"uilayout.xml": [[0,6]],
"installati": [2,0],
"verplaatsen": [5,7,[1,3,4]],
"sourceroot": [0],
"voorbeelden": [0,8,2,5],
"vermelden": [0],
"hint": [[3,4]],
"vertel": [3],
"sind": [2],
"pakketinhoud": [2],
"dubbelklikken": [2,[4,5,7]],
"basisconfigurati": [2],
"donker": [1],
"argumenten": [0],
"kie": [[0,2]],
"teamprojecten": [[2,4],[0,1]],
"haal": [2],
"genoeg-mani": [2],
"ontworpen": [[2,3]],
"overschrijft": [5,[0,2]],
"haar": [1],
"normal": [0,[2,7],[1,4,6]],
"implementeert": [1],
"brugtalen": [2],
"hernoem": [2,3],
"uitsluitingen": [7],
"bestuderen": [8],
"problemen": [[2,4],1,0,3,[5,6,8]],
"typen": [2,5,[0,1,4,7]],
"willen": [2,[0,3],6],
"deactiveren": [4],
"ongedaan": [[0,4]],
"example.email.org": [0],
"dichter": [1],
"sterretj": [0],
"team-functionaliteit": [3],
"werden": [2,[4,5]],
"toepassingsmap": [0,8],
"runtim": [2,0],
"reeksen": [0],
"uitzond": [[0,1],[2,3]],
"selecti": [7,4,0,5,[1,2]],
"testen": [[0,2]],
"accolad": [0],
"filenam": [0],
"roam": [0],
"gemiddeld": [7],
"wachten": [7],
"nbsp": [7],
"in-regelig": [7],
"gotosegmentmenuitem": [0],
"variati": [0],
"projecten": [2,0,7,1,[3,4,5,6],8],
"afwijkingen": [4],
"webbrows": [1],
"bouwt": [7],
"initialcreationd": [1],
"gretig": [3],
"deze": [7,0,2,1,6,4,3,5,8],
"printf-variabelen": [0,1],
"helpaboutmenuitem": [0],
"rode": [[1,6]],
"besturingssystemen": [6],
"paren": [[0,2]],
"veronderstel": [0],
"vindt": [3],
"structureel": [2],
"regular": [0],
"token": [0,[1,2,7],[5,6]],
"filter": [0,2,7,1,4],
"toetsenbord": [[0,4,5]],
"site": [1,2],
"projectroot": [0],
"consequenti": [3],
"omegat.log": [0],
"autocompletertableright": [0],
"aanvullend": [0,[1,2,7],3],
"corresponderend": [7,[0,1,5,6]],
"kop": [[0,7]],
"aanpassen": [0,7,3,[2,4],[1,8],6],
"tab": [0,[4,5],1],
"versi": [2,0,[3,4,6,7]],
"tag": [1,0,3,[4,7],2,5,8],
"ingevoerd": [7,4,0,[1,5],[2,3]],
"bladwijz": [0],
"hostserv": [2],
"tak": [2],
"versa": [7],
"ontwerp": [3],
"deelnem": [[0,7]],
"waarden": [0,1,[2,4,7]],
"opschonen": [[4,7]],
"scherm": [[0,3]],
"coderen": [0],
"bestandstypen": [2],
"projectreloadmenuitem": [0],
"goed": [8],
"pop-upmenu": [[5,7]],
"welkom": [3],
"navig": [4],
"cross-platform": [2],
"hersteld": [[2,6]],
"kantoortoepassingen": [7],
"html-bestanden": [[0,2,3]],
"eenvoudig-t": [3],
"weet": [[3,5]],
"tbx": [0,1],
"weer": [4,1,5,0,[2,7],3,6],
"verwijderen": [7,2,0,[3,6],4,1],
"pictogram": [4],
"can": [0],
"regelt": [3],
"herstelt": [0,[1,4,5,7]],
"cat": [[0,3,7]],
"tabellen": [0,8],
"horen": [0],
"duser.countri": [2],
"zoekvenst": [7],
"provid": [1,5],
"ontwikkelingsteam": [7],
"readm": [0],
"activeren": [4,7,0],
"leeg": [6,[4,5],[1,7],[0,2,3]],
"categori": [0],
"versiesysteem": [2],
"brontag": [4],
"geldig": [0,[2,6]],
"align.tmx": [2],
"navigeren": [3,[4,6],[2,5]],
"file2": [2],
"raadplegen": [2],
"aanwezig": [[1,5,7],[0,2,4]],
"behalv": [[0,2]]
};
