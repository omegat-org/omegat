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
 "Bijlage A. Woordenboeken",
 "Bijlage B. Woordenlijsten",
 "Bijlage D. Reguliere uitdrukkingen",
 "Bijlage E. Aanpassen van sneltoetsen toetsenbord",
 "Bijlage C. Spellingscontrole",
 "Installeren en uitvoeren van OmegaT",
 "How-To…",
 "OmegaT 4.2 - Gebruikershandleiding",
 "Menu&#39;s",
 "Vensters",
 "Projectmap",
 "Vensters en dialoogvensters"
];
wh.search_wordMap= {
"altgraph": [3],
"projectspecifiek": [11],
"inschakelen": [11,8,3],
"half": [11],
"opzicht": [6],
"scheelt": [11],
"eindigend": [8],
"vertalingen": [11,[6,9],8,10,7,[2,3,4]],
"beschrijfbar": [1],
"hoogst": [9,[3,11]],
"instelt": [11],
"start-versi": [5],
"without": [5],
"licentie-informati": [8],
"begint": [[5,6]],
"broncod": [11],
"xml": [11],
"totaal": [9],
"toepassingen": [[4,5,11]],
"ten": [6,[3,10,11]],
"instel": [4],
"automatisch": [11,8,5,6,[3,4],[1,9]],
"weergavemodi": [6],
"zwak": [11],
"info.plist": [5],
"weergav": [[3,11],[6,8],[1,9]],
"xmx": [5],
"vanaf": [5,11,[6,7,9,10]],
"lege": [11,3,8],
"gepubliceerd": [6],
"gecreëerd": [10],
"project-specifiek": [10,11],
"groot": [4],
"proxyserv": [5,11],
"softwaretoepassingen": [5],
"ltr-talen": [6],
"fuzzi": [11,[8,9],10],
"ctrl-shift-c": [11],
"auto-aanvullen": [11,3,8,1],
"befor": [5],
"adresseert": [5],
"util": [5],
"verzenden": [11],
"sneltoet": [3,8,11,6],
"tar.bz": [0],
"thui": [2],
"tmx-en": [6,[8,11]],
"object": [11],
"meervoud": [1],
"betekenen": [11],
"aangevraagd": [5],
"bat-bestand": [5],
"uitpakken": [5],
"reguliere": [7],
"waar": [6,8,[5,11],3,[4,9]],
"dgoogle.api.key": [5],
"onderzijd": [9,11,8],
"edittagnextmissedmenuitem": [3],
"geval": [6,11,5,[8,9],[4,10]],
"mogen": [[1,5,8]],
"laat": [11,[8,10]],
"quiet": [5],
"opmaakinformati": [6],
"xlsx": [11],
"notificeren": [11],
"laden": [11,6,8],
"es_es.d": [4],
"laad": [[5,6,11]],
"opdrachtprompt": [5],
"hoeveel": [[8,9]],
"assembledist": [5],
"toewijz": [5],
"the": [2,11,5,3],
"opzoeken": [8,11],
"helema": [2,11],
"extensies": [7],
"halv": [11],
"voorspellen": [8],
"reflecteert": [8],
"projectimportmenuitem": [3],
"snel": [[5,11]],
"deselect": [11],
"gesproken": [5,[8,11]],
"imag": [5],
"target.txt": [11],
"converteert": [8],
"correct": [6],
"rond": [11],
"doeldocumenten": [[8,11],6],
"kijken": [11],
"snelkoppel": [5],
"werkplek": [6],
"lijsten": [1],
"afhandelen": [[6,11]],
"onderdeel": [5],
"ltr-tekst": [6],
"pad": [5,6],
"doorgaat": [[9,11]],
"nameon": [11],
"optionel": [1],
"moodlephp": [5],
"na-verwerk": [11],
"currsegment.getsrctext": [11],
"optionsglossarytbxdisplaycontextcheckboxmenuitem": [3],
"thuispagina": [6],
"pak": [5],
"doorgaan": [11],
"meegeleverd": [11],
"export": [6],
"oorzaak": [5],
"gotonextnotemenuitem": [3],
"tar.gz": [5],
"projectbestand": [9],
"pas": [[9,11]],
"samenhangend": [11],
"start-technologi": [5],
"transtip": [[3,9]],
"check": [11,6],
"xxxx9xxxx9xxxxxxxx9xxx99xxxxx9xx9xxxxxxxxxx": [5],
"houdt": [10],
"aangevinkt": [8],
"terugkoppel": [9],
"positi": [11,9,[6,8]],
"gedeeltelijk": [[9,11]],
"azur": [5],
"fr-fr": [4],
"gedragen": [11],
"pictogrammen": [[5,7,9]],
"ongewenst": [8],
"gespecificeerd": [5,11,6],
"niet-teamproject": [6],
"enkel": [11,5,[6,8],2],
"onderscheid": [10],
"woordenlijst": [1,11,9,3,8,7,6],
"primari": [5],
"pijl": [11],
"startpunt": [11],
"gezet": [9],
"toepasbaarheid": [11],
"klinker": [2],
"verschil": [11],
"webster": [0,7],
"tab-teken": [[1,2]],
"hard": [[5,6,8]],
"gevallen": [6,11,[2,10]],
"eerder": [6,[8,11],9],
"voldoend": [11,[5,10]],
"sleutelwoord": [11],
"witt": [8],
"geïnspireerd": [11],
"drukt": [8,11],
"rood": [11,10],
"hierme": [5],
"exporteert": [[6,8,11]],
"weergegeven": [11,8,9,1,5,[6,10],2],
"segmentatie-opti": [11],
"uploadt": [8],
"toegewezen": [[3,5]],
"with": [5],
"overweg": [11],
"algemeen": [11,9,2],
"bevestigingsvenst": [11],
"sessi": [11,5],
"pdf": [6,[8,11]],
"chine": [6],
"welk": [11,[5,9],10,[2,4,6,8]],
"tweed": [9,[1,3,5]],
"tekstinvo": [6],
"productiviteit": [11],
"empti": [5],
"scripten": [11,8],
"machinevertalingen": [11],
"uitgegeven": [8],
"toolsshowstatisticsmatchesmenuitem": [3],
"bladeren": [[5,11],9],
"menu\'s": [7],
"focus": [11],
"viewdisplaymodificationinfononeradiobuttonmenuitem": [3],
"interactiev": [2],
"inhoud": [11,3,6,10,8,5,[0,9]],
"gebruikersnaam": [11],
"voer": [11,5,8],
"inclusief": [11,[6,9],[2,8]],
"variabl": [11],
"officiële": [7],
"schakelt": [[2,8,11]],
"per": [11,3,[5,8]],
"kenni": [6],
"bekijk": [11,5,[8,9,10],[2,6]],
"tmx": [6,10,5,11,[8,9]],
"woordgren": [2],
"period": [6],
"converteren": [6,11],
"uitgepakt": [5],
"nl-en": [6],
"integ": [11],
"voeg": [6,11,[3,5]],
"intel": [5,7],
"fr-ca": [11],
"mainmenushortcuts.properti": [3],
"creatiev": [11],
"typisch": [[5,6]],
"projectaccesswriteableglossarymenuitem": [3],
"even": [[4,5,11]],
"gui": [5,10],
"zoekcriteria": [11],
"cmd": [[6,11]],
"kolommen": [11,1],
"ge-ocrd": [11],
"coach": [2],
"subtitl": [5],
"genegeerd": [11,[3,5]],
"sentencecasemenuitem": [3],
"gotohistorybackmenuitem": [3],
"ingevoegd": [11,8,10],
"toe": [6,11,5,1,[3,4,10]],
"stemmen": [11,1,3],
"benedenhoek": [9],
"project-save.tmx": [6],
"cjk-teken": [11],
"uhhhh": [2],
"achtergrondkleur": [9],
"beveiligingswaarschuwingen": [5],
"tot": [11,5,[2,9],[0,6,8],3],
"powerpc": [5],
"console-modus": [5],
"forceringen": [10],
"opslagplaat": [6,[8,11],5],
"handleid": [8],
"optionssentsegmenuitem": [3],
"sterkst": [11],
"vooruit": [8],
"aangeboden": [[8,9]],
"gehouden": [11],
"optionsaccessconfigdirmenuitem": [3],
"groen": [9,8],
"groep": [2],
"charact": [8,[3,6]],
"eind": [11,2,10],
"installatie-instructi": [5],
"test.html": [5],
"namen": [11,[4,9]],
"keuzelijst": [[4,9]],
"xxx": [10],
"gevolgd": [11,2,3,6],
"smalltalk": [11],
"attributen": [11],
"remot": [5],
"standaardtoet": [11],
"gebruikt": [11,6,5,[3,4,8],[1,2,9,10]],
"verlaten": [[10,11]],
"proce": [11,6],
"scrollt": [11],
"omegat.sourceforge.io": [5],
"pseudotranslatetmx": [5],
"installatievenst": [4],
"pipe": [11],
"verhinderen": [11],
"verplaatst": [8,11,9],
"beperkingen": [11],
"hoewel": [11],
"momenteel": [[8,10,11]],
"targetlanguagecod": [11],
"bereik": [2],
"svgbestand": [5],
"blijft": [[10,11]],
"documentati": [3,[2,11]],
"rout": [6,11],
"lijnen": [11],
"translat": [11,5,8],
"pictogrambestanden": [5],
"werk": [[5,9]],
"afsluit": [8],
"bidirect": [8,3],
"opslagplaats_voor_alle_omegat_team_project_bronnen": [6],
"mappen": [6,11,[1,5,8,10]],
"werd": [8,[6,11],[1,2,5,9]],
"punten": [[6,11]],
"scriptnaam": [8],
"toepasselijk": [[5,6]],
"doelseg": [8,11,9],
"scheiden": [6],
"opkomen": [6],
"richtingen": [6],
"minimaliseert": [9],
"doorlopend": [11],
"locati": [5,[1,6],11,[4,8]],
"normaal": [5,11,[1,8,10]],
"volg": [[5,6],4],
"bureaublad": [5,11],
"websit": [10],
"maximaliseert": [9],
"docs_devel": [5],
"tsv": [1],
"paar": [6,9,[5,8,11]],
"extra": [3,[7,10,11],[5,8],[2,6]],
"correcti": [11,4],
"land": [5],
"opnieuw": [6,11,3,[8,9],[5,7]],
"alternatief": [11,[5,8,9]],
"lang": [11,5],
"gnome": [5],
"algemene": [7],
"ingesteld": [6,11,5,[1,8]],
"iso-standaard": [1],
"puntkomma": [6],
"opgegeven": [[5,11]],
"vóór": [11,[5,10],[6,9]],
"tekstfilt": [11],
"uitzonderingsregel": [11],
"tien": [8],
"alternatiev": [8,[9,11],6,3],
"vertaalsessi": [10],
"encyclopedia": [0],
"wisselend": [11],
"bron-doel": [0],
"exe-bestand": [5],
"med-pakket": [8],
"voorvoegsel": [11],
"verwacht": [[1,6]],
"optionstagvalidationmenuitem": [3],
"hoofdvenster": [7],
"japan": [[5,11]],
"pdf-bestand": [6,7],
"csv": [1,5],
"n.n_linux.tar.bz2": [5],
"besluit": [11],
"ondersteunt": [6,5],
"pt_br": [4,5],
"gpllicenti": [0],
"a-z": [2],
"verwijdert": [11,[4,8]],
"enhanc": [8],
"zoekterm": [11],
"zoal": [6,11,5,[9,10],0,4],
"let": [[5,11]],
"aanvullen": [8],
"press": [3],
"dock": [5],
"onlin": [4],
"git-cliënt": [5],
"verwijderd": [11,10,6],
"gecombineerd": [[5,11]],
"omegat-specifiek": [10],
"bovenst": [[9,11],[2,10]],
"functionaliteiten": [8],
"dmicrosoft.api.client_secret": [5],
"project_save.tmx.tijdelijk": [6],
"javascript": [11],
"mediawiki": [11,[3,8]],
"genaamd": [5,[4,10,11],[1,3]],
"beperkt": [11,6],
"herhalingen": [11,8,9],
"scheidingen": [[8,9]],
"merriamwebst": [9],
"ondersteund": [11,[2,5,6,8]],
"komt": [11,2,6],
"ouder": [[6,11]],
"geheugen": [5,11],
"opgenomen": [11,5,[1,6,9,10]],
"metateken": [2],
"docx-bestanden": [6],
"afbreekregel": [11],
"statusbalk": [9,[5,7]],
"teveel": [11],
"vertrouwd": [11],
"uitgeschakeld": [[8,11]],
"ctrl": [3,11,9,6,8,1,[0,10]],
"document": [11,6,8,9,[1,3,5,7]],
"ergen": [10,[6,9,11]],
"bevinden": [0],
"overschreven": [[5,11]],
"exporteerbaar": [6],
"besmet": [6],
"tijdelijk": [6],
"tmx-versi": [6],
"correspondeert": [8],
"standaard": [11,3,[6,8],1,[5,9],10,4,[2,7]],
"moment": [[9,11],6],
"geschiedeni": [8,3],
"venster": [11,9,8,5,[1,6],[4,7,10]],
"found": [5],
"doet": [11],
"geïmporteerd": [6,1],
"platformen": [5,1],
"tijd": [8,11],
"doen": [5,4,9],
"doel": [11,4,6,[5,9]],
"ervoor": [5],
"distributi": [5],
"resourc": [5,11],
"verbeteringen": [8],
"nauwkeurig": [10],
"dode": [6],
"contextmenu": [11,1],
"voor": [11,6,5,8,3,4,9,1,2,10,7,0],
"team": [[6,11],[3,7]],
"xx_yy": [[6,11]],
"kopi": [6,[8,10,11]],
"docx": [11,6,8],
"txt": [6,1,[9,11]],
"ondersteunen": [6],
"googl": [5,11],
"herladen": [[1,3,6,8,11]],
"personaliseren": [11],
"xhtml-filter": [11],
"hernoemd": [6],
"vervangen": [11,8,3,9,[6,7]],
"download.html": [5],
"voorbeeld": [6,[9,11],[2,5],[0,4]],
"scheidingsteken": [11],
"vorm": [11,6,[3,5,8,10]],
"bijwerken": [11],
"wijst": [5],
"praktijk": [5],
"spellingscontrole": [7],
"typ": [5],
"had": [6],
"source": [7],
"segement": [9],
"reden": [[4,5,6]],
"genereren": [[6,11],10],
"formaat": [6],
"oogpunt": [9],
"sourceforg": [3,5],
"trnsl": [5],
"goodi": [5],
"kort": [[4,11]],
"viewdisplaymodificationinfoselectedradiobuttonmenuitem": [3],
"configurati": [11,[3,8]],
"index.html": [5],
"omegat.tmx": [6],
"definiti": [3,11],
"zoek": [11,0],
"paneel": [8,[5,9]],
"editmultipledefault": [3],
"batch": [5],
"mozilla": [5],
"editfindinprojectmenuitem": [3],
"x-gebruik": [6],
"verwachtt": [11],
"voetteksten": [11],
"diffrevers": [11],
"nogmaal": [11],
"warn": [5],
"blijven": [[10,11],5],
"woordenlijsten": [1,11,[3,7,9,10],[0,4,6]],
"technetwork": [5],
"voortgang": [[6,9]],
"dingen": [6],
"werkelijk": [8],
"voert": [11],
"verwachte": [7],
"krachtig": [11],
"meest": [[5,9],[6,11],[3,10]],
"verkeerd": [4],
"opgelost": [8],
"plural": [11],
"omvat": [[5,11]],
"optioneel": [8,5],
"script-omgev": [5],
"uitgezonderd": [[6,11]],
"waarvan": [10,11],
"aangewezen": [6],
"plaatsaanduidingen": [6],
"java-implementati": [5],
"po-seg": [11],
"geleegd": [8],
"kleur": [[8,11]],
"project.gettranslationinfo": [11],
"regeleinden": [11,2],
"uitgevoerd": [11,[5,6],8,9],
"bekeken": [[5,6]],
"zichtbaar": [10],
"invoert": [11,5],
"gebruikelijk": [[5,8]],
"gereserveerd": [4],
"zowel": [6,11,[5,9]],
"wetgev": [6],
"tekentabel": [3],
"bovenstaand": [9,6,[1,2,5]],
"start": [5,11,[3,6,7,8]],
"samengevoegd": [11],
"niveau": [6,[8,11]],
"pair": [11],
"behulp": [11,[4,5,8,9]],
"equal": [5],
"klant": [6],
"kiezen": [11,[4,6]],
"colour": [11],
"breiden": [11],
"n.n_windows.ex": [5],
"opgeslagen": [11,6,5,[1,8,10],[4,9]],
"chang": [5],
"pop-up": [[8,11]],
"untarren": [5],
"sneltoetsen": [3,5,7,[2,11]],
"zoeken": [11,8,[1,3],[2,4,7]],
"opgemerkt": [6],
"optionsalwaysconfirmquitcheckboxmenuitem": [3],
"terugvallen": [8],
"sloveens": [9],
"besturingssysteem": [5,11,1,8],
"program": [[5,11]],
"opgemaakt": [[6,8]],
"hen": [2,[6,10]],
"hem": [[5,10]],
"tekst-bestandsformaten": [6],
"uitgeprobeerd": [6],
"keren": [2,[9,11],[4,8]],
"beëindigt": [2],
"het": [11,6,5,9,8,10,2,1,4,0,3,7],
"corresponderen": [4],
"enter": [11,[3,8],5],
"pagina": [8,11,5,[3,6]],
"toegang": [11,5,8,[0,3,6]],
"schakelen": [6,11,8,[4,5]],
"applic": [5],
"bidi": [6],
"teksten": [11,8],
"projectteamnewmenuitem": [3],
"tenslott": [[5,11]],
"woordenboek-bestand": [4],
"ms-dosvenst": [5],
"directorate-gener": [8],
"taalcombinati": [0],
"uniek": [11,9,3],
"verscheiden": [11,8],
"memori": [5],
"submenu": [5],
"n.n_mac.zip": [5],
"houden": [9],
"twijfelachtig": [11],
"domein": [11],
"quot": [2],
"voorgeschreven": [11],
"nadat": [11,[5,6]],
"resulterend": [5,11],
"toetsaanslagen": [[3,11]],
"dialoogvenst": [11,8,[4,6,10],[1,9]],
"log": [[3,8]],
"wanneer": [6,11,[8,10]],
"nader": [10],
"aanhalingen": [[2,7]],
"uitschakelen": [11],
"los": [9],
"computer": [7],
"uitvoert": [8],
"omegat.jnlp": [5],
"aided": [7],
"thema": [11],
"theme": [11],
"erond": [11],
"n.n_windows_without_jre.ex": [5],
"pseudotranslatetyp": [5],
"erop": [8],
"afhankelijk": [5,[8,9,11],[1,6]],
"opslaan": [11,[6,8],3,5],
"prof": [11],
"taal": [5,11,6,[4,9]],
"geüpgrad": [5],
"niet-afbreekbar": [8,[3,11]],
"gelezen": [[1,5,6,10]],
"regex-programma": [2],
"beschrijft": [6],
"dmicrosoft.api.client_id": [5],
"uitvoer": [[6,11],[3,8]],
"gulzig": [2],
"bieden": [11],
"config-fil": [5],
"door": [11,6,8,5,9,3,2,10,[0,1,4]],
"vlaggen": [[2,7]],
"tussenpozen": [6],
"hij": [10],
"projectclosemenuitem": [3],
"autotekst": [3],
"dag": [6],
"pictogramknop": [5],
"viewmarknonuniquesegmentscheckboxmenuitem": [3],
"odt-formaat": [6],
"eindnoten": [11],
"reparati": [6],
"algemen": [11,1,6],
"geeft": [8,11,9,5],
"dan": [11,5,[4,6,10],[0,2,3,8]],
"hit": [11,[2,6]],
"doelveld": [9],
"dat": [11,5,6,9,8,10,4,2,0],
"klass": [2],
"besli": [11],
"uitgesloten": [6,11],
"onder": [5,6,[0,11]],
"regelverzamel": [11],
"group": [11],
"minuten": [6,[8,11]],
"findinprojectreuselastwindow": [3],
"system-user-nam": [11],
"enig": [[8,11],[1,5,6]],
"positieven": [11],
"format": [11],
"teller": [9],
"readme.txt": [6,11],
"instellingen": [5,11,8,10],
"veilig": [11,6],
"bekend": [11,[6,9]],
"languagetool": [11,8],
"console.println": [11],
"vanweg": [10],
"translation-programma": [7],
"source.txt": [11],
"plaatst": [6],
"grote": [11,4],
"files.s": [11],
"instrueren": [4],
"analyseert": [2],
"tekenklassen": [[2,7]],
"herschrijven": [11],
"geëxporteerd": [11,8],
"alineasegmentati": [11],
"exchang": [1],
"duits": [11],
"request": [[5,8]],
"constructi": [2],
"nederlandstalig": [6],
"procedur": [6,[4,11]],
"startmap": [5],
"zoeksleutel": [2],
"engel": [[2,6],5],
"currseg": [11],
"koreaans": [11],
"uiterlijk": [11],
"voorgedefinieerd": [[2,11]],
"duren": [4],
"samenvoegen": [6],
"onthoud": [6,5,11,[4,10]],
"browser": [[5,8]],
"uitgav": [8],
"geven": [11,5,[6,8],[1,10]],
"brengen": [5],
"initiël": [5,11],
"gevuld": [[6,8]],
"fuzzy": [7],
"projectgeheugen": [11],
"voorkeursgereedschap": [11],
"noodzakelijk": [6],
"project_files_show_on_load": [11],
"voorkomt": [[4,9]],
"europes": [[6,11]],
"voeren": [5,11,6,8],
"objectmodel": [11],
"ltr": [6],
"optionsexttmxmenuitem": [3],
"downloaded_file.tar.gz": [5],
"kwaliteitsbeh": [8],
"gesorteerd": [[10,11]],
"geprefereerd": [[9,11]],
"uitsluiten": [[6,11]],
"build": [5],
"versturen": [6,11],
"gesynchroniseerd": [6,11],
"bijv": [11,5,10],
"bedoeld": [[2,6]],
"account": [5,11],
"marketplac": [5],
"tekstuel": [6],
"dhttp.proxyhost": [5],
"immuun": [10],
"entries.s": [11],
"del": [[9,11]],
"voegt": [8,[6,11]],
"doeltermen": [1],
"gotonextuntranslatedmenuitem": [3],
"targetlocal": [11],
"opdelen": [2],
"path": [5],
"toewijzen": [5],
"zorg": [5,[4,6,11]],
"gewenst": [5,[0,6,11]],
"hoge": [10],
"eracht": [1],
"preci": [11],
"beschikbaar": [5,[3,11],[6,8],4,[2,10]],
"valutasymbool": [2],
"toegangssleutel": [11],
"you": [11],
"synchroniseren": [[5,6,11]],
"hoofd-tm": [6],
"gewoonlijk": [[6,8,10]],
"past": [10,[3,9,11]],
"stelt": [11,8,5],
"allsegments.tmx": [5],
"impact": [11],
"schakel": [3,[6,8]],
"grati": [5,[4,11]],
"percentag": [9,11,10],
"projecttalen": [[6,8]],
"gebruikershandleiding": [7],
"configur": [[5,11]],
"helpcontentsmenuitem": [3],
"bronmap": [[6,11]],
"domain": [11],
"omegat-org": [6],
"weergeven": [11,8,3,[6,10],[5,9]],
"voordeel": [5],
"descript": [3],
"overeenkomt": [11,[2,4,6,10]],
"afgehandeld": [11,6],
"hoe": [11,[0,6],[7,8]],
"projectaccessdictionarymenuitem": [3],
"selecteren": [11,3,9,5,8,[4,10]],
"feitelijk": [8],
"java-start": [5],
"vertaalgeheugens": [7],
"resultaten": [11,8],
"extensi": [11,1,0,6,[9,10]],
"velden": [11,[5,8]],
"dianot": [11],
"hierond": [5,[2,6,9],[3,4,8,11]],
"optionsworkflowmenuitem": [3],
"voorkeur": [[6,9,11]],
"gesplitst": [[9,11]],
"releas": [6,3],
"po-filt": [11],
"onderstreept": [[1,4]],
"config-bestanden": [5],
"vervangt": [8],
"term": [1,9],
"overslaan": [11],
"backslash": [5,2],
"sparc": [5],
"geconverteerd": [[6,11]],
"geactiveerd": [[8,9]],
"getagd": [10],
"uit": [11,5,6,9,10,8,[0,1,4],3],
"pauz": [4],
"behoud": [11],
"zojuist": [8],
"segmentnumm": [8,3],
"niet-vertaald": [11,8,3,[6,9,10]],
"projectbeheerd": [6],
"boomstructuur": [10],
"duden": [9],
"bijgewerkt": [[1,6,11]],
"parse-gewijz": [11],
"vertaalgereedschap": [5],
"projecteigenschappen": [11,[1,4,6,7,8,10]],
"meer": [11,2,5,9,[6,10],[1,3,8]],
"verstandig": [4,10],
"bleekgrij": [8],
"benad": [11],
"instellen": [11,8,[4,6,7],[3,5,9]],
"spotlight": [5],
"visueel": [8],
"die": [11,[5,6],9,8,10,4,[1,3],2],
"ieder": [6],
"gespeld": [4],
"klikt": [11,9],
"uitvul": [6],
"keuzelijsten": [11],
"functi": [8,[4,11],9,1],
"maal": [2],
"dir": [5],
"bestandslocati": [11],
"maak": [6,10,[4,5]],
"selecteert": [8,5],
"overeen": [11,2,[1,6]],
"dit": [11,5,6,8,10,[2,4,9],7],
"opties": [7],
"div": [11],
"later": [11,[5,10],[6,8,9]],
"verwarren": [11],
"laten": [9,[3,5,6,11]],
"indienen": [6,8],
"viewfilelistmenuitem": [3],
"kwijt": [6],
"straf": [10],
"strategieën": [11],
"tmxen": [3],
"info": [3,8,11],
"deselecteren": [11],
"venstergroott": [11],
"test": [5],
"elektronisch": [9],
"omegat": [5,11,6,8,[3,7,10],4,1,[0,9],2],
"gehel": [11,8,[5,10]],
"spellingsproblemen": [8],
"handmatig": [11,[4,6,8],1],
"beid": [6,[5,11]],
"alinea-vert": [11],
"tijden": [[6,8,9,11]],
"manier": [11,6,5,[9,10]],
"probleem": [1,6],
"opvolgend": [5],
"meerdere": [7],
"internetzoekmachin": [11],
"file-source-encod": [11],
"zoekacti": [11,[2,8]],
"waarbij": [[6,9],[10,11]],
"some": [6],
"virtual": [11],
"gedeelt": [[9,11],[5,8],4],
"kwaliteit": [6,10],
"vereist": [5,[2,6,11]],
"projectinstellingen": [6],
"gerelateerd": [[6,11]],
"staan": [11,5,[6,9],[0,1,4,10]],
"console-align": [5],
"po-bestand": [[9,11]],
"projectopenrecentmenuitem": [3],
"passen": [11,5,[6,9]],
"bronfold": [5],
"allema": [[2,3,6,10]],
"grieks": [2],
"niet-gulzig": [2],
"hte": [5],
"staat": [11,[5,8],6,[4,9]],
"markeren": [8,3,11,1],
"vertellen": [11],
"eenmaal": [[5,6],11],
"eerstekla": [11],
"overeenkomen": [11,2,[1,9]],
"spelling": [7],
"hostnaam": [11],
"uitdrukk": [11,[2,5]],
"editexportselectionmenuitem": [3],
"afhandelt": [11],
"bestaan": [11,[1,4,6]],
"soortgelijk": [11,[0,5,9]],
"home": [6],
"tagvalidati": [11],
"tekst-bestanden": [11],
"filteropti": [11],
"varianten": [[2,11]],
"maar": [6,11,[1,2],[4,5,9],[8,10]],
"bestaat": [[6,10,11],1,9],
"projectaccesstargetmenuitem": [3],
"bruikbar": [5],
"waarnaar": [11],
"uitzonderlijk": [11],
"editoverwritemachinetranslationmenuitem": [3],
"opslag_voor_alle_omegat_teamprojecten": [6],
"gecomprimeerd": [10],
"menuitem": [3,8],
"ingreek": [2],
"hun": [11,6,10,[2,5,8,9]],
"hoger": [11,5],
"indexitem": [11],
"geassocieerd": [8],
"vorig": [8,3,6,[9,11]],
"corrigeren": [8],
"gekleurd": [9],
"es_es.aff": [4],
"med-projecten": [8],
"alternatieven": [9],
"raden": [11],
"sleutelwoorden": [11],
"accepteert": [10],
"vergroot": [11],
"projectexitmenuitem": [3],
"pt_pt.arr": [4],
"configuratiebestand": [5],
"strikt": [6],
"bouwen": [5,7],
"aligndir": [5],
"system-host-nam": [11],
"action": [8],
"uitlijn": [11],
"text": [[5,11]],
"misschien": [11,5,[2,6]],
"uiteraard": [9],
"gekopieerd": [9,11,6,8],
"editregisteruntranslatedmenuitem": [3],
"init": [6],
"creat": [11],
"python": [11],
"es_mx.dic": [4],
"alleen": [11,6,8,5,[1,4,9]],
"vastgezet": [9],
"geforceerd": [10],
"vertelt": [5,9],
"geauthenticeerd": [11],
"infix": [6],
"bak-bestanden": [6],
"maco": [5,1],
"tarbal": [0],
"zullen": [11,6,[8,9],5,10,[1,2,4]],
"negeer": [11],
"tijdstip": [10],
"doc": [6],
"opent": [8,11,[4,9]],
"zijden": [6],
"afbreken": [11],
"doe": [[0,11]],
"ltr-brontaal": [6],
"verzamelen": [6],
"status": [[8,11],[6,9],10],
"geheugenruimt": [5],
"structuur": [11,10],
"braziliaan": [[4,5]],
"server": [6,11,[5,10]],
"importeerd": [9],
"xml-bestanden": [11],
"verwachten": [5],
"betreft": [11],
"paramet": [6,5,11,2,10],
"mac": [3,[2,5,6]],
"mag": [11,[1,6],[3,8,10]],
"juist": [5,6,[1,10],[4,11],[0,8,9]],
"file": [11,5,6],
"eenzelfd": [11],
"maakt": [11,5,[6,8,9]],
"man": [5],
"map": [5,6,[10,11],8,4,[1,3,9],0],
"accentueren": [9],
"puntj": [8],
"klein": [3,8,10,[2,6,11]],
"hoeven": [11],
"nodig": [[5,6],11,4,2],
"within": [11],
"geparset": [6],
"bent": [6,[5,11],0],
"onbeperkt": [5],
"fijn": [2],
"individueel": [11],
"voorraadmap": [6],
"vlag": [11],
"menu": [3,7,5,11,8,9,[4,6]],
"url": [6,11,[4,5,8]],
"gebundeld": [5,11],
"megabyt": [5],
"uppercasemenuitem": [3],
"viewmarkuntranslatedsegmentscheckboxmenuitem": [3],
"waarschuw": [11,[6,9]],
"omstandigheden": [6],
"a-za-z": [2,11],
"mene": [9],
"relev": [11],
"tekstreek": [8],
"coder": [11,1],
"vermijden": [6,[10,11]],
"verzocht": [5],
"geldt": [[9,11]],
"use": [5],
"meerder": [11,[5,9],[1,6,8,10]],
"project_save.tmx.jaarmmdduumm.bak": [6],
"dienen": [[6,10,11]],
"vertaal": [11,6],
"geannuleerd": [8],
"doelsegmenten": [[5,6,11]],
"importeren": [[6,11],[8,10]],
"geïdentificeerd": [8],
"spati": [11,2,8,[1,3]],
"startopdracht": [5],
"rechts-klikken": [11],
"bevat": [10,6,11,5,9,8,[0,2,7]],
"tussen": [11,6,[8,10],[1,2,9]],
"bekijken": [[5,6,10]],
"omegat.jar": [5,[6,11]],
"source-pattern": [5],
"omegat.app": [5],
"usr": [5],
"oorzaken": [[1,5]],
"host": [5],
"invitati": [6],
"teruggaan": [11],
"invoegen": [11,[3,8],9],
"soort": [[6,11]],
"speciaal": [6,11],
"utf": [1],
"gelet": [11],
"tekstbestanden": [11,6],
"programmeerstijlen": [11],
"echter": [[6,11],5,4],
"linkerk": [8],
"problematisch": [6],
"html-tag": [11],
"bewerken": [11,9,8,[3,7],6,[1,5,10]],
"feed": [2],
"vallen": [[5,9]],
"projectsit": [11],
"woorden": [11,8,9,[2,3],[1,6,10]],
"servic": [5,11,8],
"paradigma": [11],
"true": [5],
"getypeerd": [11],
"dubbel": [5,2],
"dsl": [0],
"meestal": [11],
"tmx-basi": [6],
"submap": [10,6,11,5,4],
"groovi": [11],
"doorverwezen": [6],
"tekstgebi": [11],
"cliënt": [6,10,[9,11]],
"vertaalactiviteiten": [11],
"weggeschreven": [11],
"ontwikkelen": [2],
"fran": [11,5],
"robuust": [6],
"best": [11,10],
"codering": [7],
"mee": [10],
"med": [8],
"beginnend": [2],
"taalinstellingen": [4],
"panelen": [5],
"balk": [9],
"probeer": [11],
"tagbeh": [6],
"en.wikipedia.org": [9],
"oplossen": [6],
"kmenueditor": [5],
"dtd": [5],
"men": [[3,11]],
"sterk": [[6,11]],
"met": [11,6,5,[2,8],9,[1,4],10,3,0,7],
"startscript": [5],
"voorzichtig": [6],
"merk": [5,[0,4,11]],
"onmiddellijk": [1],
"segmenten": [11,8,9,3,10,6,5],
"nederland": [6],
"weergeeft": [11,8],
"voelt": [5],
"niet-zichtbar": [11],
"projectcompilemenuitem": [3],
"console-transl": [5],
"eigen": [6,11,[2,5,9]],
"maand": [6,5],
"tag-validati": [6],
"master": [6],
"betrekk": [[9,11],2],
"kmenuedit": [5],
"gotonextuniquemenuitem": [3],
"toegestaan": [11],
"form-fe": [2],
"geschreven": [8,[1,11]],
"invoervelden": [6],
"vensters": [7],
"aanbevolen": [11],
"blanco": [11],
"xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx": [5],
"writer": [6],
"gebaseerde-segmentati": [11],
"wordart": [11],
"dalloway": [11],
"rubi": [11],
"optionsviewoptionsmenuitem": [3],
"maximaliseren": [9],
"bron": [11,8,6,3,[5,9],7],
"dus": [11,6,5,10],
"feit": [6,11],
"gehost": [11],
"eenvoudig": [6,[2,11],[1,4]],
"binnen": [6,[5,8,10],[3,9]],
"commit": [6],
"targetlocalelcid": [11],
"quoten": [2],
"werkzaamheden": [9],
"dia-lay-out": [11],
"project_stats_match.txt": [10],
"dezelfd": [11,6,5,[0,1,2,8,9,10]],
"inloggen": [11,3],
"hoofdmap": [6],
"dvd": [6],
"talen": [11,6,[4,7]],
"gewerkt": [11],
"xmx2048m": [5],
"aanroepen": [11],
"huidig": [8,11,9,[3,10],[5,6],1],
"software-rel": [11],
"xxxxxxxxxxxxxxxx.xxxxxxxxxxxxxxxx.xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx": [5],
"stappen": [11],
"zal": [11,5,[6,8],9,10,4,[1,2],3],
"actiev": [[8,10]],
"besliss": [10],
"user.languag": [5],
"regex": [2,7],
"betekeni": [11],
"meta": [3],
"keystrok": [3],
"vergelijken": [5],
"krunner": [5],
"configureren": [11],
"libreoffic": [4,[6,11]],
"functionel": [11],
"gerepareerd": [[1,6]],
"global": [[1,11]],
"relatief": [6,11],
"regel": [11,2,3,[5,10],[1,9]],
"niet-gulzige": [7],
"bewerkt": [[8,9]],
"bevestigen": [[3,11]],
"twee": [11,5,6,4,[8,9],10],
"bevestig": [[5,8,10,11]],
"doeltalen": [11],
"spaan": [4],
"defin": [3],
"toegankelijk": [11,[3,6,8]],
"negerend": [8],
"sorteervolgord": [[8,9]],
"voordat": [11,[4,6]],
"plug-in": [11],
"aanpassingen": [11],
"verbeterd": [11],
"beschikbar": [11,5,4,9],
"gemak": [5],
"bewerkingsvenst": [11],
"doelgedeelt": [1],
"ip-adr": [5],
"stap": [6,11,10,9],
"pijltjestoetsen": [9],
"verifiëren": [1],
"afmetingen": [9],
"viewdisplaysegmentsourcecheckboxmenuitem": [3],
"klik": [11,5,8,[1,4]],
"appear": [11],
"negati": [2],
"editregisteremptymenuitem": [3],
"ibm": [5],
"moeten": [11,6,5,10,[2,3],[0,4],[1,8]],
"lettertypen": [8],
"doelbestandsnaam": [11],
"open": [11,[5,6,8],10],
"beheren": [6,[5,7,11]],
"www.oracle.com": [5],
"ongeluk": [11],
"project": [6,11,8,5,[3,10],9,1,4,7,0],
"onderst": [11,9],
"niet-uniek": [11,8,3],
"xmx1024m": [5],
"brondocumenten": [11],
"readme-bestanden": [11],
"verbind": [6],
"commando": [5],
"noemen": [11],
"lijkt": [[8,9,11]],
"geschikt": [4,5],
"windows-versi": [5],
"zeg": [6],
"penalty-xxx": [10],
"gotonextsegmentmenuitem": [3],
"vergrendeld": [5],
"definieert": [11],
"effecten": [11],
"versiebeh": [6],
"zoekopti": [11],
"zet": [[5,9]],
"tab-gescheiden": [1],
"identificeren": [11],
"nnn.nnn.nnn.nnn": [5],
"hierbij": [10],
"basal": [5],
"revisiestatus": [10],
"verificati": [11],
"zouden": [11,[3,6],[0,2,5]],
"beheerd": [11,6],
"abort": [5],
"diamodellen": [11],
"omdat": [11,[5,6],9],
"tsv-bestand": [1],
"distriberen": [4],
"luidt": [5],
"left-to-right": [6],
"gebeurt": [9],
"idx": [0],
"conversi": [6],
"internet": [11],
"object-attributen": [11],
"tellers": [7],
"nummer": [8,11,1],
"langer": [[5,11]],
"projectbestanden": [11,8,[3,7,9]],
"geëvalueerd": [11],
"how-to": [6,[0,7,10]],
"projectaccesscurrentsourcedocumentmenuitem": [3],
"linux": [5,[2,7,9]],
"iet": [[0,6,9]],
"proberen": [11,[5,6]],
"subtracti": [2],
"logbestand": [8],
"besturingsteken": [2],
"file.txt": [6],
"tussenst": [6],
"handleidingen": [6],
"es-mx": [4],
"odf-bestanden": [6],
"ontstaat": [11],
"ifo": [0],
"machinevert": [8,11,9,3],
"gelogd": [5],
"basi": [8],
"behandelen": [11],
"gebruiken": [11,5,6,4,[3,7],8,9,[0,1,10]],
"stel": [11],
"stem": [9,11],
"belangrijkst": [9],
"octal": [2],
"xx.docx": [11],
"blokniveau": [11],
"overschrijven": [[6,10,11]],
"consist": [8],
"api-sleutel": [5,11],
"titel": [11,8],
"bedden": [6],
"opgesomd": [3],
"zie": [6,[2,5,11],[4,9,10]],
"optionsautocompleteautotextmenuitem": [3],
"relati": [9],
"schrijven": [11,[5,6]],
"schuifbalk": [11],
"zij": [11,6,[3,10],[4,8],[1,2,5]],
"toekomstig": [6],
"zin": [11,6],
"mijnproject": [6],
"bureau-accessoir": [5],
"snelkoppelingen": [5],
"vak": [11],
"onderwerpen": [11],
"van": [11,6,5,8,9,10,4,3,2,1,7,0],
"vordert": [10],
"zit": [6],
"hints": [7],
"patroon": [11,2],
"poge": [[1,11]],
"tbx-woordenlijsten": [11],
"sturen": [6,11],
"midden": [[2,6]],
"concis": [0],
"customer-id": [5],
"middel": [11],
"daarin": [[5,6]],
"verloren": [6],
"word": [11,6],
"japans": [11],
"term.tilde.com": [11],
"vertaaleenheden": [10,11],
"eindigt": [5],
"eenvoudigst": [5],
"dankwoorden": [8],
"specifiek": [11,[6,8],5,[2,9]],
"steed": [6,[9,11]],
"regulier": [11,2,5,[3,4]],
"één": [11,[2,8],6,[5,10],[0,9],[1,3]],
"koptekst": [11],
"ftp-server": [11],
"portuge": [[4,5]],
"viewmarknotedsegmentscheckboxmenuitem": [3],
"ongev": [[5,6]],
"verkrijgen": [[5,11]],
"event": [3],
"imperatiev": [11],
"zorgt": [5,6],
"installatie": [7],
"afzonderlijk": [11,6,[1,3]],
"lingvo": [0],
"gevalideerd": [8,[9,11]],
"onderwerp": [[6,10],11],
"gotomatchsourceseg": [3],
"word-bestanden": [6],
"tekenreek": [11,9],
"aangeven": [[5,6]],
"gevonden": [11,[5,9],[1,2,6]],
"behandelt": [11],
"mrs": [11],
"optionssaveoptionsmenuitem": [3],
"vooruit-terug": [11],
"excel": [11],
"omegat-tag": [6],
"runn": [11],
"uitzien": [[5,11]],
"verbreek": [6],
"rechtstreek": [5],
"stardict": [0],
"omegat.l4j.ini": [5],
"onderk": [11],
"behandeld": [11,9],
"span": [11],
"aantal": [11,9,[1,8],[2,4,6,10]],
"beschrijven": [[3,6,8,9,11]],
"metatag": [11],
"denkt": [6],
"opgedeeld": [11],
"geabonneerd": [5],
"daarna": [11,[6,9]],
"helpteksten": [6],
"zoekresultaten": [11],
"teksteenheden": [11],
"ltr-tekenreeksen": [6],
"lijst": [11,[7,8],[2,4,5,6]],
"space": [11],
"indient": [6],
"uitmaken": [[4,10]],
"biedt": [11,5],
"open-sourc": [11],
"niet": [11,6,5,8,9,2,1,[4,10],3,0],
"jaar": [6],
"html": [5,11],
"voorkomend": [[6,11]],
"keuzevakken": [11],
"traag": [[5,11]],
"lager": [[6,9,11]],
"spell": [4,[8,11]],
"thunderbird": [4,11],
"ver": [6],
"editselectfuzzy3menuitem": [3],
"vet": [11,[1,9]],
"ontbrekend": [8,3,9],
"ontvangen": [5,11],
"tekstballon": [8],
"fals": [[5,11]],
"project.projectfil": [11],
"blauw": [[9,11]],
"daarme": [11],
"klikken": [11,5,9,[4,8],6],
"gekozen": [[4,8,10]],
"exporteren": [11,6,[3,8,10]],
"regelmatig": [6,11],
"hoofdlett": [11,[2,3],8,[5,9]],
"instanti": [5,[8,9]],
"probeert": [11,6],
"geluk": [11],
"mui": [[8,9]],
"vermeld": [8,[6,9]],
"zin-segmentati": [11],
"sjabloon": [11],
"compatibel": [5],
"vragen": [6,[5,9,11]],
"licentieovereenkomst": [5],
"rechten": [5],
"www.ibm.com": [5],
"woord": [11,8,4,[5,9]],
"rechter": [9],
"eén": [[1,9]],
"elder": [4],
"volgend": [11,8,3,[2,5],[6,9],0,[1,4,7,10]],
"sleutel": [11,5],
"toelaat": [6],
"platform": [5],
"een": [11,6,5,8,2,10,9,1,4,3,7,0],
"herlaadt": [8,11],
"bronbestand": [11,6,8],
"zoekt": [11],
"shortcut": [3],
"public": [6],
"projectpakket": [8],
"actief": [11],
"verschaffen": [4],
"doeltekst": [[9,11]],
"betrouwbar": [11],
"pt_br.aff": [4],
"veroorzaken": [5,8],
"tmx2sourc": [6],
"stijl": [6],
"zou": [11,6,5,[0,2,9]],
"lookup": [[8,11]],
"vertaalt": [11],
"andere": [7],
"zwevend": [11],
"daarom": [[4,5,8]],
"overeenkomend": [11,9,[1,5,8]],
"zonder": [11,5,[6,9]],
"daarop": [[8,9]],
"recht": [11,[5,6,9],8,[1,4]],
"kopiëren": [4,6,11,[3,5,8,9,10]],
"mogelijkheid": [11,[0,5,6]],
"bestandsformaten": [11,6],
"command": [[3,9],5],
"mogelijkheden": [11,6],
"raadpleeg": [2],
"vertaald": [11,6,9,[8,10],[3,5],4],
"n.n_without_jr": [5],
"indeling": [7],
"dient": [[6,11],[5,10]],
"dhttp.proxyport": [5],
"bovenk": [11],
"installatiemap": [11,5],
"indien": [11,8,5,[4,6],[9,10],3,0],
"categorieën": [[2,7]],
"haakj": [11],
"ingeschakeld": [11],
"viewmarkbidicheckboxmenuitem": [3],
"subrip": [5],
"doorverwijzingen": [6],
"sluiten": [11,8,3],
"configuratiemap": [[3,11]],
"duidelijk": [10],
"via": [[3,11],5,[0,6]],
"score": [11],
"springen": [9],
"nieuw": [11,6,[5,8],1,3,4,2],
"toch": [5,6],
"spatiebalk": [11],
"automatiseren": [5],
"volum": [11],
"lettertyp": [8,11,3],
"zoekmethoden": [11],
"koppelingen": [11],
"bestandsextensi": [[1,11]],
"alinea-eind": [11],
"opti": [11,8,5,9,3,[4,6],10,2],
"navigati": [11],
"submappen": [[0,10,11]],
"raw": [6],
"vooraf": [5,11,[6,10]],
"version": [5],
"ongevoelig": [11],
"toetsen": [3,11],
"uren": [6],
"breedt": [11],
"aanwijzingen": [6],
"posten": [6],
"klassen": [[2,7]],
"buiten": [[5,9]],
"flexibiliteit": [11],
"detail": [11,[6,8],[5,9]],
"levert": [5],
"gebruikers": [7],
"heeft": [11,5,6,[4,9],1,8,2],
"aaa": [2],
"specificati": [11],
"contemporari": [0],
"solari": [5],
"projecteditmenuitem": [3],
"rtl-segmenten": [6],
"britannica": [0],
"aan": [11,5,6,9,10,8,3,[1,2],[0,4]],
"begonnen": [[5,6,11]],
"sleep": [5],
"worden": [11,6,5,8,10,[1,9],3,[2,4]],
"hebben": [11,[5,9],[6,8],[1,10]],
"uitschakelt": [11],
"opdrachtscherm": [5],
"regex-programma\'s": [7],
"gescheiden": [11,1,9],
"machin": [11,5,8],
"wikipedia": [8],
"toet": [3,[1,9,11]],
"notities": [7],
"abc": [2],
"rcs": [6],
"tekstverwerk": [5],
"ingevuld": [5],
"bestaand": [11,[5,6],[1,10]],
"iceni": [6],
"controleert": [8],
"doelbestanden": [11,6,8,3],
"nabewerken": [11],
"originel": [6,11,9],
"voorzorgen": [6],
"kunt": [11,5,9,[4,6],[8,10],[0,3]],
"witruimt": [11,2,[3,8]],
"numeriek": [6],
"alinea": [11,[6,8]],
"rechterbovenhoek": [9],
"opstarten": [5],
"activeert": [8],
"post": [6],
"achterliggend": [11],
"glossary.txt": [6,1],
"beveilig": [11],
"neer": [[5,9]],
"dsun.java2d.noddraw": [5],
"herkennen": [[6,11]],
"taalpatroon": [11],
"placehold": [11],
"doc-bestand": [6],
"tegenstel": [11],
"add": [6],
"chines": [[5,6,11]],
"elk": [11,6,[2,8,9],[1,10],5,3],
"werken": [6,11,[4,5],3],
"canade": [11],
"scripttalen": [11],
"equival": [[5,8]],
"duplicaten": [11],
"x0b": [2],
"weglaten": [11],
"neem": [10],
"getypt": [8],
"doelmap": [11],
"canada": [5],
"segmentatiedoeleinden": [11],
"botsen": [[8,11]],
"gnome-gebruik": [5],
"http": [6,5,11],
"gelukkig": [11],
"optionsautocompleteshowautomaticallyitem": [3],
"bestandsindelingen": [[8,9]],
"vanuit": [11,[9,10],[5,8],[1,6,7]],
"uitdrukkingen": [[2,11],7,[3,4,5,9]],
"poortnumm": [5],
"hiërarchi": [10],
"larouss": [9],
"untar": [0],
"komma": [11,2,1],
"syntaxi": [11,3],
"invlo": [6],
"ruimt": [8],
"teken": [2,11,[5,8],[1,9],7],
"vol": [11],
"filters.conf": [5],
"zorgen": [11],
"beginmap": [5],
"softwar": [11],
"zodra": [8,1],
"heap-gewijz": [11],
"projectsinglecompilemenuitem": [3],
"gelijk": [[5,8,11]],
"gebruikersaccount": [11],
"ene": [11],
"zolang": [[8,11]],
"modifi": [3],
"globaal": [11],
"cursorpositi": [8,11,9],
"geversioneerd": [10],
"teamproject": [6,[7,8],[3,10,11]],
"wachtwoord": [11,6],
"myfil": [6],
"overlappen": [9],
"invoerbestand": [11],
"prioriteit": [11,8],
"verlaagd": [10],
"special": [11,9],
"krachtiger": [2],
"realiseert": [4],
"doelbestand": [11,6],
"voorgedefinieerde": [7],
"segmenteren": [11],
"geëxtraheerd": [11],
"versie-control": [6],
"opmerk": [6,[8,11],[2,10],9,1],
"woordteken": [2],
"alsof": [11],
"clone": [6],
"gulzige": [7],
"menu-item": [[3,8]],
"targetlanguag": [11],
"gedeelten": [9,11],
"tekstelementen": [6],
"afstand": [6,10,[8,11]],
"collega": [9],
"voorhanden": [6],
"system-os-nam": [11],
"teamverband": [5],
"editselectfuzzyprevmenuitem": [3],
"optionstabadvancecheckboxmenuitem": [3],
"defect": [6],
"rij": [8],
"naar": [11,6,8,5,3,9,[4,10],7,0,[1,2]],
"simpledateformat": [11],
"laatst": [8,3,[6,10,11],5],
"optionsviewoptionsmenuloginitem": [3],
"gegradeerd": [10],
"algorithm": [[3,8]],
"zetten": [11,9],
"daaraan": [5],
"med-project": [8],
"tar.bz2": [0],
"tekst-woordenlijsten": [1],
"brontalen": [6],
"twijfelt": [10],
"naam": [11,5,[6,9],10,[0,1]],
"bundle.properti": [6],
"script": [11,8,5],
"voorkeursmap": [8],
"verwijz": [[1,6],9],
"system": [11],
"krijgen": [11,6,5,0],
"reken": [[10,11]],
"gegeven": [[6,11]],
"identiek": [[6,10],[8,11],[2,3,5,9]],
"x64": [5],
"bronsegmenten": [8,11,[3,6]],
"controleren": [6,[8,11],[5,9],[4,10]],
"verdwijnen": [4],
"verplichten": [0],
"assisteert": [6],
"keyev": [3],
"netwerk": [[5,6]],
"mogelijk": [11,6,9,5,2,1,[3,8,10]],
"bewerk": [11,[1,4,5,6]],
"isn\'t": [2],
"login": [11],
"ander": [6,11,5,9,8,[1,4,10],2],
"local": [6,5,11],
"valid": [11,6],
"interfac": [5],
"negeert": [[9,10]],
"gesloten": [6,9],
"eruit": [11],
"verwijd": [11,[5,6,9,10]],
"patronen": [11,6],
"niet-gesegmenteerd": [11],
"machinevertaling": [7],
"optionsteammenuitem": [3],
"onthoudt": [[8,11]],
"onnodig": [[6,11]],
"zoiet": [11],
"bundle-filt": [11],
"tekst": [11,9,[6,8],10,7,[1,4]],
"genomen": [[3,11]],
"gzip": [10],
"immuniteit": [10],
"stellen": [11],
"gevorderd": [[2,5,11]],
"werkt": [8,[6,11],[4,5]],
"uitlijnen": [11,8,7],
"esc": [11],
"behelzen": [5],
"x86": [5],
"afgekort": [[2,11]],
"gebruik": [5,11,6,8,[1,9],2,7,[0,3,4]],
"zoektekst": [2],
"merendeel": [11],
"berichten": [9,5],
"nostemscor": [11],
"gedrukt": [9],
"kritisch": [11],
"back-upmedia": [6],
"es_mx.aff": [4],
"genoeg": [6],
"topt": [11],
"wijzigingen": [6,[5,10],[3,8,11],[1,9]],
"opmaakprofiel": [6],
"nee": [5],
"console-createpseudotranslatetmx": [5],
"mode": [5],
"witruimten": [11],
"vertaalgeheugen": [6,11,10,9,8,5,2],
"modi": [6],
"etc": [11,[5,6,9],[0,2,10]],
"gebi": [11],
"sommig": [11,[0,6],[1,8,9,10]],
"longman": [0],
"fuzzyflag": [11],
"engels": [6],
"links": [7],
"toolsshowstatisticsstandardmenuitem": [3],
"bureau": [9],
"net": [6],
"langzam": [6],
"identificeert": [11],
"all": [11,6,5,8,9,3,[4,10],2],
"merriam": [0,7],
"escap": [1],
"read": [11],
"config-bestand": [5],
"geminimaliseerd": [9],
"alt": [[3,5,11]],
"boven": [11,[6,8],[4,5]],
"weergavericht": [6],
"ééntalig": [11],
"tegen": [6,11],
"bestanden": [11,6,5,10,4,8,9,[0,1],3],
"po-bestanden": [11],
"alreadi": [11],
"foutbericht": [5,6],
"collect": [[9,11]],
"brontekst": [11,8,10,[1,3,6,9]],
"windows-gebruikers": [7],
"agressief": [8],
"geheel": [10,11],
"gestart": [11,[2,3,5,8]],
"zulk": [11,[6,10]],
"betreffend": [11],
"valideren": [5,[3,11]],
"hoofdvenst": [9,11,3],
"voorafgegaan": [[2,5]],
"bedoel": [11],
"per-project": [11],
"behouden": [11,10,[5,6]],
"n.n_without_jre.zip": [5],
"slaan": [9],
"vergelijk": [11],
"and": [5],
"opmaak": [[6,11],10],
"weigerachtige": [7],
"slaat": [8,6,5],
"magento": [5],
"moet": [5,11,6,[3,4],1,9],
"reproduceren": [6],
"ophalen": [11],
"ant": [[6,11]],
"entiteiten": [11],
"hetgeen": [11],
"diagrammen": [11],
"specific": [11],
"comprimeren": [11],
"pdf-bestanden": [6],
"dubbelklik": [5],
"waard": [2,11],
"bronnen": [6,11,5],
"opgehaald": [[5,11]],
"offlin": [6,5],
"aanvang": [10],
"leiden": [5],
"selectief": [6],
"codering-declarati": [11],
"u00a": [11],
"cyclus": [3],
"helplastchangesmenuitem": [3],
"filteritem": [11],
"verliet": [8],
"omegat.ex": [5],
"losgemaakt": [11],
"brongedeelt": [1],
"echt": [[5,11]],
"shift": [3,[6,8,11],1],
"sourcetext": [11],
"optreedt": [5],
"attribuut": [11],
"progressief": [11,10],
"auto-tekst": [11],
"taggen": [11],
"rechts": [7],
"java": [5,[3,11],2,[6,7]],
"zoekveld": [11],
"lokalisatie-ingenieur": [6],
"willekeurig": [11,[9,10]],
"tweetalig": [[6,11]],
"english": [0],
"hoofdletter-gevoelig": [2],
"voorgesteld": [9,8,11],
"werkbladnamen": [11],
"jar": [5,6],
"ondervindt": [6],
"api": [5],
"lang2": [6],
"lang1": [6],
"editselectfuzzy2menuitem": [3],
"project_save.tmx": [6,10,11],
"startparamet": [5],
"dictionari": [0],
"ongeacht": [[1,11]],
"oplevert": [[2,11]],
"gewijzigd": [6,11,8,5,[3,10]],
"reed": [11,[3,8,9,10]],
"rtl-tekst": [6],
"gevraagd": [8],
"malen": [6],
"zinsegmentati": [11],
"reek": [11],
"overeenkomst": [9,8,[3,11],10,1],
"dergelijk": [6,11],
"mixen": [6],
"geel": [[8,9]],
"geaccentueerd": [8,9,11],
"geen": [11,[5,8],6,1,9,10,[2,4]],
"letter": [8,3,[2,11],[5,6]],
"acti": [8,3,[0,5]],
"zult": [5,[6,10],11],
"vertaalbar": [11],
"nooit": [11],
"plaatsen": [[3,5,6,10]],
"opslag_voor_omegat_teamproject": [6],
"spellingscontrol": [4,11,10,[1,2,3]],
"bericht": [[5,6]],
"editselectfuzzynextmenuitem": [3],
"samen": [11,6],
"opnemen": [6,[9,11]],
"klembord": [8],
"read.m": [11],
"structurel": [11],
"default": [3],
"blokken": [11],
"knoppen": [11],
"readme.bak": [6],
"registreren": [8,3],
"tcl-gebaseerd": [11],
"gevolg": [11],
"mechanism": [8],
"functionaliteit": [11],
"timestamp": [11],
"vice": [[6,11]],
"projectaccessrootmenuitem": [3],
"betrouwbaar": [10],
"platform-specifiek": [11],
"dyandex.api.key": [5],
"vóórdat": [8,11],
"behoeven": [9],
"rtl": [6],
"doelen": [10],
"locale-cod": [11],
"benodigd": [5,0],
"gemaakt": [6,8,11,5,[1,9,10],4],
"jdk": [5],
"plugin": [11],
"bestand": [5,11,6,8,1,10,3,0,[7,9]],
"waarin": [11,5,6,[4,8,9]],
"essenti": [4],
"onvoorwaardelijk": [10],
"geopend": [11,6,9,8,[1,5]],
"grafieken": [11],
"toegevoegd": [6,[1,5,10],8],
"gekoppeld": [5,[10,11]],
"specificeren": [11,5,[3,6]],
"verlaat": [[6,11]],
"leveranci": [11],
"doeltaal": [11,[4,6],5],
"alinea-scheidingen": [8],
"toolsshowstatisticsmatchesperfilemenuitem": [3],
"stilistisch": [11],
"editinsertsourcemenuitem": [3],
"run": [11,[5,6]],
"viterbi": [11],
"rijen": [11],
"microsoft": [11,[5,6],9],
"doel-landcod": [11],
"projectnewmenuitem": [3],
"gegevensv": [6],
"verzonden": [11],
"intervallen": [11],
"ervan": [11,[1,8],[5,9]],
"optionstranstipsenablemenuitem": [3],
"ruw": [11],
"segment": [11,8,9,3,[1,10],[5,6]],
"vier": [8],
"titlecasemenuitem": [3],
"bronmappen": [6],
"glossari": [1,11,[6,10],9],
"totalen": [8],
"editcreateglossaryentrymenuitem": [3],
"ignored_words.txt": [10],
"betrokken": [6,11,4],
"instructies": [7],
"configuration.properti": [5],
"github.com": [6],
"cijfer": [11],
"zodat": [6,11,10,9],
"prototyp": [11],
"afbeeld": [4,[0,2,9]],
"significant": [11],
"bijvoorbeeld": [11,6,[4,5],9,[2,8],[0,10],[1,3]],
"verder": [11,8,[3,9,10]],
"hulpprogramma": [6],
"glossary": [7],
"altijd": [11,1,[3,8]],
"privat": [[5,11]],
"herhaald": [11],
"gaan": [11,6,8,5,[3,9]],
"verwezen": [6,11],
"segmentati": [11,[2,3,6,8]],
"name": [11],
"invoer": [6,11],
"downloaden": [5,[0,3,8],[4,6,7,11]],
"woordenboek": [4,11,[7,8],[0,9,10]],
"gaat": [9,[5,6,11]],
"parameters": [7],
"proxyhost": [5],
"gedownload": [5,11],
"wijzen": [5],
"string": [5],
"import": [6],
"nog": [11,[6,9],[1,8]],
"reserveert": [5],
"handig": [11,5,6,2,[9,10]],
"systemen": [5,[6,7]],
"comput": [5,11],
"not": [5],
"ingebouwd": [[4,11]],
"bestandsnaam": [11],
"logische": [7],
"verschillen": [11,5,6],
"bronbestanden": [6,11,8,3,5],
"lijken": [5],
"knippen": [9],
"gren": [2],
"onveranderlijk": [11],
"gebruikersbestanden": [11],
"was": [[6,9,11]],
"hoofd": [3,8,2],
"wat": [11,8,[2,5,6]],
"notiti": [11,9,8,3],
"selection.txt": [11,8],
"target": [[8,10,11],7],
"tezamen": [11],
"xhtml": [11],
"achter": [11],
"finder.xml": [11],
"omgev": [5],
"productnamen": [6],
"vermeldt": [11],
"window": [5,[0,2,8]],
"config-dir": [5],
"installatieprogramma": [5],
"misplaatst": [8],
"criteria": [11],
"disable-project-lock": [5],
"waarop": [[4,5,11]],
"algoritmen": [11],
"overeenkomsten": [11,8,9,10,[1,6],7,3,2],
"waarom": [11],
"omegat.pref": [11],
"termbas": [1],
"when": [11],
"plak": [8],
"modus": [5,6,11,9],
"carriage-return": [2],
"loggen": [5],
"gereedschap": [[8,11]],
"plan": [11],
"verschijnen": [11,3,[4,5,6,8,9,10]],
"onderdelen": [6],
"lokalisatie-bestanden": [6],
"item": [11,[1,8],5,3,[2,6,9]],
"voordelen": [11],
"specificeert": [5,11],
"howto": [6],
"geïnverteerd": [11],
"pakket": [5],
"rtl-weergav": [6],
"gegenereerd": [10],
"declarati": [11],
"escape-teken": [2],
"pt_pt.dic": [4],
"auteur": [[8,9,11]],
"meertalig": [6],
"tekstlaag": [6],
"targettext": [11],
"respectievelijk": [6],
"beëindig": [11],
"standaardregel": [11],
"specificeerd": [4],
"geconfigureerd": [11],
"bijzonder": [5],
"cellen": [11],
"level1": [6],
"uitvullen": [6],
"bronseg": [[9,11],[8,10]],
"level2": [6],
"onthouden": [[6,11]],
"onwaarschijnlijk": [11],
"scripttaal": [11],
"toevoegt": [11],
"direct": [5,11,8,[1,10]],
"wezen-segmenten": [11,9],
"gebruikershandleid": [[3,5,7,8]],
"aaabbb": [2],
"oranj": [8],
"grijz": [8],
"bewerkingsveld": [9,11],
"variëteiten": [10],
"reageren": [9],
"web": [5,[6,7]],
"edittagpaintermenuitem": [3],
"lokal": [6,[8,11],5],
"en-us_de_project": [6],
"wee": [6],
"vertegenwoordigen": [11],
"systeem": [5,[4,11],[3,8]],
"weg": [6],
"verwerk": [11,[5,8]],
"programmeren": [11],
"componenten": [11],
"wel": [5,[1,6,10,11]],
"optionscolorsselectionmenuitem": [3],
"hoofdmenu": [11,[3,9]],
"doelbestandsnaam-patronen": [11],
"editselectfuzzy4menuitem": [3],
"editregisteridenticalmenuitem": [3],
"display": [11],
"po-koptekst": [11],
"viewmarknbspcheckboxmenuitem": [3],
"verwerken": [11,[3,5,8]],
"gelaten": [11,5],
"geplaatst": [[8,11],1,6,[5,10]],
"associëren": [8],
"pt_br.dic": [4],
"grij": [[8,11]],
"volledig": [11,6,[3,5,9]],
"verbonden": [8],
"ontbreekt": [2],
"unabridg": [0],
"toestaan": [11],
"nul": [[2,11]],
"minder": [5,[6,10]],
"bronbestandsnaam": [11],
"presentati": [11],
"maakten": [11],
"nut": [11],
"heen-en-w": [6],
"letterlijk": [11],
"entiteit": [11],
"beveiligen": [11],
"doelterm": [1,8],
"optionsglossaryexactmatchcheckboxmenuitem": [3],
"softwareprogramma": [6],
"msgstr": [11],
"plek": [[6,9]],
"revisiecontrol": [6],
"overeenkomstig": [11,9,[8,10]],
"vereisen": [[8,9]],
"koppelen": [5],
"veel": [[6,11],[4,5,10]],
"sorteren": [11],
"werkmap": [5],
"voetnoten": [11],
"bevatten": [11,6,10,[5,9],[3,4,8]],
"standaardinstellingen": [11],
"openen": [8,[5,11],3,6,[4,9]],
"fouten": [6],
"statussen": [6],
"doorzoeken": [2],
"volgt": [6,[5,11],[3,10]],
"nnnn": [9,5],
"redenen": [11],
"omegat.project": [6,5,10,[7,9,11]],
"keer": [6,11,5],
"marker": [9,[8,11]],
"po-doelbestand": [11],
"effect": [11,9],
"excludedfold": [6],
"zoekitem": [11],
"option": [11],
"waarme": [11,5],
"webstart": [5],
"opendocument-bestanden": [11],
"niet-omegat": [11],
"hernoemen": [6,11,4],
"segmentatie-instellingen": [11,8],
"losmaken": [9],
"bronbestandsnamen": [11],
"zh_cn.tmx": [6],
"uitsluit": [11],
"wij": [11],
"getal": [[2,9],5],
"totdat": [[5,6,11]],
"toepass": [[5,6],8,4,[9,10,11]],
"wil": [[6,11]],
"scripts": [7],
"pakken": [0,5],
"plakken": [9],
"lengt": [9],
"komen": [2,1],
"kolomkop": [8],
"also": [11],
"zich": [[5,11],6,10,[0,4]],
"internetpagina": [11],
"gesegmenteerd": [11],
"kleiner": [11],
"consol": [5],
"yandex": [5],
"overgeslagen": [11],
"niet-opgemaakt": [11],
"archiv": [[5,6]],
"repo_for_omegat_team_project.git": [6],
"user": [5],
"a123456789b123456789c123456789d12345678": [5],
"vervangingen": [11],
"weinig": [[2,6]],
"viewmarkwhitespacecheckboxmenuitem": [3],
"voltooien": [11],
"proxi": [11,5,3],
"extens": [11],
"commentaar": [5],
"secti": [6],
"detecteert": [[1,5]],
"complet": [[5,6]],
"uitleg": [5],
"eerst": [11,8,5,6,[1,9,10],[2,3,4]],
"bak": [[6,10]],
"operatoren": [[2,7]],
"jokerteken": [11,6],
"bat": [5],
"berekend": [11,9],
"complex": [2],
"gescand": [6],
"decima": [11],
"neergezett": [9],
"jre": [5],
"doesn\'t": [11],
"waaruit": [[4,11]],
"terwijl": [6,9],
"optionsfontselectionmenuitem": [3],
"tekstbestand": [[6,8]],
"zien": [11,6,9],
"seconden": [11],
"fysiek": [4],
"werkend": [[5,10]],
"gang": [[8,11]],
"onderstrepen": [1],
"af": [11,8],
"cat-programma": [10],
"ziet": [11],
"diff": [11],
"al": [11,6,5,8,9,10,4,2,1,3,0],
"grafisch": [5],
"genieten": [11],
"an": [2],
"editmultiplealtern": [3],
"auteursrechten": [8],
"scrollen": [[9,11]],
"git.code.sf.net": [5],
"zinnen": [11],
"elementen": [11],
"logisch": [[2,11]],
"uitvoerbar": [5],
"technisch": [11,8],
"identificati": [11],
"schema": [11],
"karakteristieken": [11],
"taalcod": [4,11],
"tekeningen": [[6,11]],
"invoeren": [[2,11]],
"delen": [6,11,9],
"variabelen": [11],
"freebsd": [2],
"distribueren": [[4,6]],
"frans": [5],
"filters.xml": [6,[10,11]],
"delet": [11],
"uitgelijnd": [8,11],
"veld": [11,[5,8],4],
"br": [11,5],
"projectaccessglossarymenuitem": [3],
"vertaalwerk": [9],
"gestuurd": [11],
"kopieer": [6,[5,8]],
"segmentation.conf": [6,[5,10,11]],
"woordenboeken": [4,0,7,8,[9,11],[1,6,10],3],
"ca": [5],
"vele": [6],
"developerwork": [5],
"brontaal": [[6,9,10]],
"hoeft": [10,[1,6]],
"cd": [5,6],
"gecorrigeerd": [11],
"ce": [5],
"öäüqwß": [11],
"set": [11,5],
"gegevensverlies": [7],
"projectmappen": [[6,11]],
"besturingssysteemg": [1],
"documenten": [11,6,8,3,[5,9,10]],
"vertaalbureau": [9],
"wordt": [11,6,5,8,9,10,1,3,4],
"ofwel": [11,[4,8]],
"cn": [5],
"schade": [11],
"tabel": [2,3,11,9,[1,8]],
"beschrijv": [11,[3,5,6]],
"optionsrestoreguimenuitem": [3],
"gevaar": [6],
"cx": [2],
"wijzigt": [8,[5,11]],
"conflicteren": [3],
"opdrachtregel": [5,6,7],
"apach": [4,[6,11]],
"geërfd": [6],
"getoond": [11],
"adjustedscor": [11],
"woordenlijstbestanden": [1],
"dd": [6],
"de": [11,6,5,8,9,10,4,1,3,2,0,7],
"bladzijd": [3],
"terminolog": [11,8],
"offic": [11],
"bel": [2],
"beginnen": [[5,11],[3,6]],
"zijn": [11,6,5,8,1,9,3,10,4,0,2],
"extern": [11,8,[1,3,6]],
"f1": [3],
"kopieën": [6],
"do": [11],
"aangegeven": [[0,8,10]],
"toont": [[0,9,11]],
"f2": [9,[5,11]],
"hiervan": [5],
"f3": [[3,8]],
"gemeenschap": [6],
"dr": [11],
"f5": [3],
"zijd": [6],
"dialoogvensters": [7],
"repositories": [7],
"dz": [0],
"projectsavemenuitem": [3],
"editundomenuitem": [3],
"neerzetten": [5,7],
"zelfstandig": [[2,5,9]],
"xmx6g": [5],
"parst": [11],
"bijlag": [[1,2,4],[0,3],6],
"cursief": [11],
"resultaat": [[2,11]],
"powerpoint": [11],
"aansluiten": [9],
"u000a": [2],
"voegen": [11,6,1,[4,8,9,10]],
"bevindt": [5,6,[10,11]],
"ontbreken": [9],
"combinati": [[3,5]],
"pt_br-woordenboeken": [4],
"bestandsindeling": [7],
"geplakt": [8],
"en": [11,6,5,8,9,[4,10],1,2,3,7,0],
"lezen": [6],
"er": [11,5,6,[8,9],[1,10],[0,4],2],
"u000d": [2],
"u000c": [2],
"eu": [8],
"tekstbewerk": [1],
"ondersteunend": [6],
"voorkeuren": [8,11,5,[1,3,6,7]],
"tekstvak": [2],
"activ": [11,[5,10]],
"achtergrond": [[8,10]],
"frame": [5],
"cursor": [[9,11],8,1],
"u001b": [2],
"instructi": [[5,11]],
"stats.txt": [10],
"gebruikersinterfac": [5,[1,9,11]],
"terminologi": [[1,6,8,9,11]],
"onderaan": [[3,11]],
"deel": [[4,11],10],
"foo": [11],
"for": [[8,11],3],
"exclud": [6],
"segmentatieregel": [11,[2,6],10],
"fr": [5,[4,11]],
"brede": [11],
"content": [5,11],
"besluiten": [11],
"desktop": [5],
"weergave": [7],
"applescript": [5],
"ga": [3,8,[7,9,11]],
"gezocht": [11],
"gb": [5],
"verschijnt": [11,5],
"doelcod": [11],
"class": [11],
"helplogmenuitem": [3],
"methoden": [11,5],
"ongewijzigd": [11],
"over": [6,5,11,[3,8,9],10],
"terug": [8,[9,11],[3,5,6],[0,1,2,4,10]],
"gespecialiseerd": [9],
"vermeden": [11],
"editoverwritetranslationmenuitem": [3],
"outputfilenam": [5],
"go": [11,[1,6]],
"vrij": [[0,8]],
"aeiou": [2],
"bereikt": [[5,11]],
"toevoegen": [11,5,6,10,[3,9],[1,8]],
"rtl-documenten": [6],
"verschaft": [11,[6,8,9]],
"form": [5],
"zijnd": [2],
"aanmerken": [11],
"berekenen": [11],
"informati": [5,11,6,[0,2,8]],
"voltooid": [[6,8,9]],
"opeenvolgend": [11],
"bij": [11,6,5,10,9,[1,2]],
"select": [11,8,5,4,6,[1,9,10]],
"laadt": [11,6],
"duser.languag": [5],
"upgraden": [5],
"aangepast": [6,[3,8,11],[1,5]],
"weigeren": [[5,6]],
"bis": [2],
"begrijpt": [6,11],
"windows-platformen": [5],
"file-target-encod": [11],
"projectopenmenuitem": [3],
"autom": [5],
"begrenzingen": [[2,7]],
"context": [[9,11],[3,6,8]],
"fout": [6,[5,8]],
"geïmplementeerd": [11],
"https": [6,5,[9,11]],
"id": [11,6],
"basisopdracht": [5],
"if": [11],
"vooraf-gedefinieerd": [11],
"project_stats.txt": [11],
"ocr": [6],
"zeer": [6],
"projectaccesscurrenttargetdocumentmenuitem": [3],
"toolsvalidatetagsmenuitem": [3],
"ik": [5,11],
"vreemd": [11],
"in": [11,6,5,8,9,10,1,4,2,3,0,7],
"zichzelf": [11],
"termin": [5],
"sla": [6,[3,5]],
"effectueren": [11],
"is": [11,5,6,8,9,10,4,2,1,3,0,7],
"geselecteerd": [11,8,5,[4,6],9,3],
"it": [11],
"odf": [11],
"daaruit": [[5,6,11]],
"ondernomen": [8],
"projectmap": [1,10,[0,6,7,9]],
"ja": [5],
"alleen-lezen": [6],
"indel": [1,6,0,[8,11]],
"begin": [5,[2,10,11],6,[0,1,3,4,8,9]],
"odt": [[6,11]],
"tmx-bestanden": [6,10,[5,11]],
"hetzelfd": [11,6,[2,5],[4,8,9]],
"gotonexttranslatedmenuitem": [3],
"viewmarktranslatedsegmentscheckboxmenuitem": [3],
"daar": [11,[5,8]],
"windows-gebruik": [5],
"valu": [5],
"afwijkend": [[10,11]],
"vinden": [5,11,[3,6]],
"bewerkingen": [[6,9,11]],
"vals": [11],
"nplural": [11],
"js": [11],
"wijzig": [11,10,[4,5,8]],
"ilia": [5],
"keuz": [[5,11],8],
"learned_words.txt": [10],
"waarschuwingen": [5],
"weigerachtig": [2],
"vett": [9],
"programma": [5,6,11,[2,8],9],
"optic": [6],
"waarschuwingsteken": [2],
"d.i": [11,5,[4,6,8,9,10]],
"hier": [11,[6,8],[5,10],9],
"macos": [7],
"ftl": [5],
"betek": [11,6],
"editselectfuzzy1menuitem": [3],
"bepaalt": [4],
"alinea-segmentati": [11],
"expliciet": [11],
"upgrad": [[5,11]],
"viewdisplaymodificationinfoallradiobuttonmenuitem": [3],
"beter": [11],
"draw": [6],
"off": [8],
"aanvul": [[8,9]],
"kolom": [[1,11],8],
"hide": [11],
"bepaald": [11,6,10,[1,4,5,9]],
"samenvatting": [7],
"platt": [6,[1,11]],
"exporteerbar": [6],
"bronterm": [1,8],
"dswing.aatext": [5],
"xhtml-bestanden": [11],
"herkend": [1,11],
"auto": [10,[6,8],11],
"eenvoudigweg": [5,[1,4,9,11]],
"verzamel": [11],
"lu": [2],
"document.xx.docx": [11],
"som": [11,6,10],
"gedetecteerd": [8],
"onbruikbaar": [11],
"cycleswitchcasemenuitem": [3],
"download": [5,[0,6]],
"routinematig": [5],
"versnellen": [6],
"licenti": [6],
"mb": [5],
"oracl": [5,3,11],
"hoofdwachtwoord": [11],
"me": [6],
"hierboven": [11,6,5,[9,10],[0,2,4]],
"suggesti": [11,[3,4,8,9,10]],
"drempel": [11],
"doel-odt-bestand": [6],
"omegat.png": [5],
"getallen": [11,9,6],
"gradlew": [5],
"mm": [6],
"entri": [11],
"termen": [1,9,[3,11]],
"gedetailleerd": [[5,11]],
"mr": [11],
"ms": [11],
"mt": [10],
"aangebracht": [9],
"zeker": [[9,11]],
"schijf": [6,[5,8]],
"my": [6,5],
"kant": [11],
"definiëren": [11,[2,8]],
"relevant": [6,8,[3,5,11]],
"na": [11,5,[1,2,3,10]],
"genest": [1],
"nb": [11],
"ne": [8],
"updat": [11,8],
"bestandsnamen": [11,4],
"toegepast": [11,6],
"verzameld": [[9,10]],
"waarvoor": [11],
"nl": [6],
"vergeten": [0],
"datum": [11,8],
"no": [11],
"code": [3,5,11],
"gebaseerd": [11,4,[0,5]],
"volgord": [11,9],
"nu": [11,[3,4,6]],
"versiesystemen": [6],
"gotohistoryforwardmenuitem": [3],
"box": [11],
"switch": [11],
"unicode-blokken": [[2,7]],
"total": [11],
"óf": [2],
"naast": [11,[5,8],[3,4]],
"noch": [5],
"of": [11,6,5,2,8,9,3,[0,1,10],4],
"bundl": [5],
"kan": [11,6,[5,8],[3,9],[4,10],1,2],
"archief": [0],
"ok": [[5,8]],
"zelf": [11,6,8,[2,5,9]],
"om": [11,5,6,8,9,4,1,10,3,[0,2]],
"hexadecimal": [2],
"on": [6,11],
"sluit": [11,8,6],
"op": [11,5,8,6,9,4,10,3,[0,1]],
"macro": [11],
"or": [11],
"os": [[6,11]],
"src": [6],
"andersom": [11],
"gegaan": [[5,10]],
"niettegenstaand": [11],
"control": [6,[0,8],[3,4],2],
"gevaarlijk": [11],
"vertaalprojecten": [11],
"doeldocu": [8,[3,6]],
"no-team": [[5,6]],
"blijken": [6],
"vormen": [11,10],
"editinserttranslationmenuitem": [3],
"pc": [5],
"pdfs": [6],
"land-paar": [11],
"beperken": [11,5],
"projectnaam-omegat.tmx": [6],
"nakijken": [10],
"handelsmerken": [9],
"installeren": [5,4,7,0,8],
"beschouwd": [11],
"gewaarschuwd": [11],
"beschouwen": [6],
"po": [[5,9,11]],
"genoemd": [5,[6,11]],
"minst": [[10,11]],
"optionsglossarystemmingcheckboxmenuitem": [3],
"pt": [5],
"drie": [6,10,[0,9],1],
"vraagt": [11],
"ongeldig": [5,6],
"tekenset": [[1,11]],
"back-up": [[6,10]],
"html-opmerkingen": [11],
"environ": [5],
"verdeelt": [11],
"optionsautocompleteglossarymenuitem": [3],
"geprobeerd": [5],
"gesleept": [9],
"keuzevak": [11,4,5],
"verschillend": [11,6,[8,9],5],
"taalcontrol": [11],
"verwijzingen": [6,[1,2,11]],
"recent": [[5,8],[3,6]],
"cyaankleurig": [8],
"pinpoint": [11],
"gedurend": [11,5,6,10],
"sta": [11],
"edit": [8],
"editselectfuzzy5menuitem": [3],
"individuel": [11],
"beneden": [11],
"them": [11],
"collecti": [11,[2,9]],
"kde": [5],
"bestandsbeheerd": [4],
"verhogen": [11],
"rc": [5],
"uitvoeren": [5,7,[6,8]],
"slepen": [[5,9],7],
"ná": [5],
"laagst": [9],
"zin-niveau": [11],
"includ": [6],
"verschilt": [9],
"rapporteren": [6],
"volgen": [[2,6,8]],
"statistieken": [8,[3,10],6,11],
"gebeurtenissen": [3],
"stopt": [11],
"privaci": [5],
"vergroten": [11,8,3],
"dynamisch": [11],
"advi": [[5,6]],
"vertal": [[6,11],8,9,[3,10],5],
"inloggegeven": [11],
"hebt": [[5,6],11,4,[8,9],[3,10]],
"gratis": [7],
"configuratiebestanden": [5,8],
"omgezet": [11],
"languag": [5],
"gebeuren": [10],
"windows-systemen": [5],
"rtl-tekenreeksen": [6],
"lettertype-instellingen": [11],
"bestandsfilt": [11,8,[3,5,6,10]],
"bovenzijd": [9],
"sc": [2],
"tmx-bestand": [10],
"heel": [[10,11]],
"heen": [6],
"gedefinieerd": [[3,8,10,11],[4,5,6,9]],
"heet": [5],
"slecht": [11,6,[5,8,10],[0,1,4]],
"derd": [1,9],
"key": [5,3,11],
"starten": [5,11,7],
"intern": [[9,11],8],
"starter": [5],
"coderingen": [11],
"maker": [11],
"svn": [6,10],
"gekregen": [9],
"waarschijnlijk": [[5,6,9]],
"interv": [11,[6,8]],
"maken": [11,6,5,8,3,10,9,1],
"editoverwritesourcemenuitem": [3],
"bijzond": [11,6],
"te": [11,5,6,9,8,[4,10],1,0,3,2,7],
"alfabetisch": [11],
"enforc": [10],
"remov": [5],
"teruggedraaid": [6],
"stoppen": [5],
"tm": [10,6,11,8,[5,7,9]],
"behalen": [5],
"to": [[5,11]],
"vast": [9],
"v2": [5],
"editreplaceinprojectmenuitem": [3],
"voorloop": [11],
"document.xx": [11],
"traceren": [11],
"nemen": [11,[4,6]],
"inhoudsopgave": [7],
"tw": [5],
"plaat": [10,[4,5,11],[1,3,6,8,9]],
"negeren": [[4,8,9,11]],
"ook": [5,[6,11],9,8,4,[1,3,7,10]],
"express": [11,2],
"gevoelig": [11],
"afsluiten": [3,[8,11]],
"brondocu": [[3,11]],
"viewmarkautopopulatedcheckboxmenuitem": [3],
"beschreven": [11,5],
"deactiv": [1],
"projectwikiimportmenuitem": [3],
"countri": [5],
"gedrag": [5,[3,8,9,10,11]],
"ui": [6],
"afbeeldingen": [7],
"vullen": [[5,6]],
"lokaal": [[6,8]],
"verstrekt": [11],
"uitnodigen": [6],
"uitstekend": [6],
"vertaalproc": [11],
"gotoprevioussegmentmenuitem": [3],
"uu": [6],
"aangeraden": [[6,11]],
"geaccepteerd": [10,[3,5]],
"formaten": [6],
"uw": [5,6,4,9,11,3,8,10,[0,1]],
"this": [11],
"eenheden": [11],
"gotopreviousnotemenuitem": [3],
"editredomenuitem": [3],
"uilayout.xml": [10],
"installati": [5,[4,9]],
"verplaatsen": [[8,9,11]],
"vi": [5],
"voorbeelden": [[2,11],[5,7]],
"hoofdstuk": [[2,6,9,11]],
"knop": [11,4],
"traditionel": [5],
"hint": [11,[4,6]],
"kopieert": [[6,8,11]],
"pakketinhoud": [5],
"gemarkeerd": [8],
"geïnstrueerd": [11],
"vs": [11],
"dubbelklikken": [[5,8,9,11]],
"punt": [11,2,5],
"donker": [11],
"lay-out": [9],
"argumenten": [5],
"kie": [11],
"gedaan": [6,[9,11],[1,5]],
"zoveel": [[2,6]],
"teamprojecten": [[6,8],11],
"uitgevuld": [6],
"pure": [6],
"we": [11,6],
"natuurlijk": [[4,10],[5,6,11]],
"groovy.codehaus.org": [11],
"overschrijft": [8],
"recept": [6],
"backspac": [11],
"ord": [6],
"normal": [11,[5,6]],
"emac": [5],
"uitsluitingen": [6],
"org": [6],
"hernoem": [6],
"recognit": [6],
"projectleid": [6],
"distribut": [5],
"problemen": [8,6,[1,5],[0,7]],
"xf": [5],
"typen": [11,8,5],
"willen": [[6,11],2],
"betekenisvoll": [11],
"deactiveren": [8],
"ongedaan": [[3,8]],
"dichter": [11],
"werden": [[6,9]],
"overwegen": [[6,11]],
"xx": [5,11],
"xy": [2],
"runtim": [5],
"toepassingsmap": [5],
"sourc": [6,10,11,5,8,9],
"hele": [11],
"belangrijk": [6,[5,10]],
"tester": [2,7],
"linkerkolom": [9],
"selecti": [8,11,3,[0,9]],
"uitzond": [2,11],
"handelsnamen": [11],
"testen": [2],
"type": [6,11,[3,5]],
"wijzigen": [11,6,[5,9],3],
"toolssinglevalidatetagsmenuitem": [3],
"scant": [11],
"gebruikersgroep": [6,9],
"filenam": [11],
"geen-woord": [2],
"ligt": [10],
"geïnstalleerd": [5,[4,8],11],
"projectaccesssourcemenuitem": [3],
"uiteindelijk": [4],
"bronbestandsnaam-patroon": [11],
"afkort": [11],
"yy": [9,11],
"gemiddeld": [11],
"wachten": [11],
"nbsp": [11],
"method": [5,11],
"odg-bestand": [6],
"in-regelig": [11,10],
"gotosegmentmenuitem": [3],
"opdracht": [5,8,11],
"drukken": [11,9,6,8],
"projecten": [11,8,6,[1,5]],
"ze": [11,6,[5,9],[1,8]],
"manieren": [6,[5,11],4],
"push": [6],
"zh": [6],
"russisch": [5],
"readme_tr.txt": [6],
"vervang": [11,9],
"sporen": [5],
"doelgegeven": [5],
"intact": [[10,11]],
"teruggegeven": [5],
"object-georiënteerd": [11],
"zo": [[5,11],[2,4,6]],
"medeklink": [2],
"penalti": [10],
"ontbraken": [8],
"exact": [11,[1,8],4],
"xx_yy.tmx": [6],
"bouwt": [11],
"oud": [11,5],
"geladen": [6,[5,8,11]],
"deze": [11,5,8,10,6,[4,9],1],
"oploss": [6],
"opmerkingen": [11,9,[3,8],[1,7]],
"utf8": [1,[8,11]],
"helpaboutmenuitem": [3],
"landcod": [11],
"verouderd": [6],
"aantallen": [11],
"out": [[6,11]],
"besturingssystemen": [[5,10]],
"paren": [11],
"elkaar": [[5,9]],
"navigatieknop": [5],
"vindt": [2,[5,11]],
"regular": [2],
"toepassen": [11],
"mexicaan": [4],
"tag-valid": [5],
"kleuren": [[8,11],3],
"weergavemodus": [6],
"invoersystemen": [11],
"derhalv": [5],
"foutief": [11],
"token": [11],
"hoeveelheid": [[5,11]],
"filter": [11,6,5],
"help": [[3,7],8],
"toetsenbord": [3,7,[2,9,11]],
"omegat-project": [[8,10]],
"consequenti": [11,4],
"maplocati": [8],
"vetrouwelijheid": [11],
"right-to-left": [6],
"u0009": [2],
"xhh": [2],
"revis": [0],
"u0007": [2],
"repositori": [6,10],
"vertalen": [6,11,5,8,[7,9]],
"minimum": [11],
"zinvol": [4],
"gedraagt": [[10,11]],
"foutiev": [11],
"bestandsindel": [1,11],
"typt": [9],
"aanvullend": [11,10,[0,2,5,6,9]],
"argument": [5],
"ini-bestand": [5],
"corresponderend": [11,[4,10]],
"ovbereenkomsten": [11],
"lowercasemenuitem": [3],
"bedrijf": [9],
"wiki": [[0,9]],
"firefox": [[4,11]],
"blok": [2],
"druk": [11,[5,9],1],
"separ": [11],
"kop": [11],
"aanpassen": [11,3,[5,7],2],
"tab": [11,[1,3,8],9],
"taa": [11,8],
"wijz": [11],
"divers": [11],
"versi": [5,6,8,[2,4,9]],
"html-documenten": [11],
"tag": [11,[6,8],[3,5],9],
"ingevoerd": [11,8,9],
"bladwijz": [11],
"indelingen": [11,[5,8]],
"versa": [[6,11]],
"optieknoppen": [11],
"herstellen": [9,[3,6,11]],
"tar": [5],
"waarden": [11,1,5],
"naarto": [5],
"castilliaan": [4],
"opdrachten": [11,5,8],
"onli": [11],
"scherm": [5],
"aanloopt": [6],
"coderen": [11],
"bestandstypen": [11],
"projectreloadmenuitem": [3],
"java-properti": [11],
"omegat-interfac": [6],
"voorkomen": [11,6,[5,7,10]],
"pop-upmenu": [[9,11]],
"targetcoutrycod": [11],
"eigenschappen": [[6,8,11],[0,3,4,5]],
"openoffic": [4,11],
"navig": [5,[4,6]],
"verborgen": [[10,11]],
"populair": [11],
"sleutel-waardenparen": [11],
"vrijwel": [2],
"gid": [5],
"optionsautocompletechartablemenuitem": [3],
"hersteld": [10],
"line": [2],
"link": [[6,11],0],
"html-bestanden": [11,5],
"weet": [5,11,9],
"praktisch": [[5,9]],
"winrar": [0],
"tbx": [1,[3,11]],
"weer": [8,11,[5,9],6,[1,2]],
"verwijderen": [11,6,5,[3,4,8,9,10]],
"pictogram": [5,8],
"can": [11],
"git": [6,10],
"herstelt": [8,[9,11]],
"systeemvak": [5],
"tegelijkertijd": [11,8],
"tabellen": [[3,6,7]],
"initieel": [11],
"duser.countri": [5],
"zoekvenst": [6],
"verwijzen": [9],
"tck": [11],
"tegenkomt": [11],
"terminalvenst": [5],
"xx-yy": [11],
"passend": [5],
"odf-documenten": [11],
"readm": [5,11],
"internetverbind": [4],
"activeren": [8],
"will": [11],
"match": [2],
"kunnen": [11,6,5,[9,10],8,[1,2],[3,4]],
"leeg": [11,6,10,[8,9],[1,5]],
"ingedrukt": [[3,8,11]],
"follow": [2],
"categori": [2],
"wilt": [11,6,5,8,[3,4,9,10]],
"verwerkt": [5,[6,8]],
"effectief": [11],
"brontag": [8,3],
"optionsspellcheckmenuitem": [3],
"versiesysteem": [6],
"geldig": [11,5],
"terugkeren": [[6,11]],
"daarvan": [11,6,[0,4,10]],
"align.tmx": [5],
"frase": [11],
"navigeren": [11],
"file2": [6],
"optionssetupfilefiltersmenuitem": [3],
"aanwezig": [11,10,[0,5,6]],
"behalv": [6,11],
"overeenstem": [11]
};
