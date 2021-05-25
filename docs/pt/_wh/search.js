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
 "Apêndice A. Dicionários",
 "Apêndice B. Glossários",
 "Apêndice D. Expressões regulares",
 "Apêndice E. Personalização dos atalhos",
 "Apêndice C. Verificação ortográfica",
 "Instalar e executar o OmegaT",
 "Guias práticos...",
 "OmegaT 4.2 - Guia do utilizador",
 "Menus",
 "Painéis",
 "Project folder",
 "Janelas e diálogos"
];
wh.search_wordMap= {
"altgraph": [3],
"actualização": [11],
"coerent": [11],
"poderá": [[5,11],6,[2,4,9]],
"não-único": [11,8,[3,9]],
"remota": [[6,11]],
"novo": [[6,11],3,[1,5],8,4],
"seta": [[9,11]],
"elimin": [6],
"característica": [11],
"alteraçõ": [[5,6],10,11,8,[1,3,9]],
"exibir": [5,11],
"instalador": [4],
"basta": [[5,10,11],[1,9]],
"muda": [5],
"menor": [[6,11]],
"xml": [11],
"texto-alvo": [11],
"remoto": [6,10,[5,8]],
"mude": [[6,8]],
"tem": [5,11,6,[1,8],4],
"nova": [8,11,5,2,[1,4,10]],
"ter": [11,6,8,[1,3],[5,9]],
"info.plist": [5],
"n.n.n_without_jre.zip": [5],
"pedir": [[6,9]],
"xmx": [5],
"serv": [10],
"alteração": [11,10,[6,8]],
"crítico": [11],
"solicitada": [5],
"aplicar-s": [8],
"fuzzi": [[10,11]],
"oferece-lh": [5],
"befor": [5],
"junto": [11],
"solicitado": [5],
"util": [6,11,[5,9]],
"têm": [11,[0,5,9]],
"maiúscula": [[2,11],8,[3,5]],
"perigo": [6],
"precisament": [11],
"tar.bz": [0],
"mostrar": [11,[3,8],5,[1,6,10]],
"guarda": [5],
"instruído": [11],
"garantirá": [11],
"encontrado": [[5,11],[6,9],1],
"chinê": [6,5],
"independent": [2],
"dedicada": [6],
"adiciona": [6],
"seus": [5,11,6,9,[0,3,10]],
"mostram": [11,9],
"impresso": [9],
"dados-alvo": [5],
"inalterado": [11],
"dgoogle.api.key": [5],
"versionamento": [6],
"desta": [11,[6,9,10]],
"formulário": [11],
"dessa": [4],
"encontrada": [[2,11]],
"edittagnextmissedmenuitem": [3],
"prevenir": [6,7],
"repositório_para_projecto_omegat.git": [6],
"violeta": [8],
"modificar": [11,5,[6,9]],
"laranja": [8],
"quiet": [5],
"comunidad": [6],
"compilar": [5,7],
"convit": [6],
"alternar": [6,8],
"texto-font": [11,[1,3,6]],
"xlsx": [11],
"enorm": [[4,11]],
"área": [11,8],
"iniciá-lo": [5],
"preservado": [11],
"histórico": [8,3],
"es_es.d": [4],
"visualment": [8],
"gestor": [6,4],
"n.n.n_without_jr": [5],
"rápido": [5],
"rolamento": [11],
"assembledist": [5],
"the": [5,[0,2]],
"preservada": [11],
"component": [11],
"aceitar": [5,8],
"projectimportmenuitem": [3],
"resumo": [7],
"imag": [5],
"monolingu": [11],
"target.txt": [11],
"substituir": [11,8,3,9,7],
"corrigido": [11],
"acontec": [10],
"nameon": [11],
"usavam": [11],
"moodlephp": [5],
"maioria": [11,[3,5]],
"currsegment.getsrctext": [11],
"optionsglossarytbxdisplaycontextcheckboxmenuitem": [3],
"inglê": [6,2,5],
"corrigir": [[8,11]],
"fonte-alvo": [0],
"gotonextnotemenuitem": [3],
"atrasar": [6],
"par": [6,[9,11]],
"tar.gz": [5],
"gpl": [0],
"aplica": [5],
"check": [6],
"especiai": [11],
"tradutor": [6,9,11,10],
"conflito": [3],
"xxxx9xxxx9xxxxxxxx9xxx99xxxxx9xx9xxxxxxxxxx": [5],
"envolv": [11,6],
"será": [11,5,8,[1,6],9,[3,10]],
"plataforma": [5,[1,11]],
"personalizada": [11,[3,8]],
"cole-o": [8],
"azur": [5],
"fr-fr": [4],
"azul": [[9,11]],
"padrõ": [11,6],
"motivo": [8],
"formato": [6,11,1,8,[0,7],[5,9]],
"disco": [[5,6,8]],
"restriçõ": [[0,5,11]],
"remova": [[5,10,11]],
"livr": [[0,8]],
"arranqu": [5],
"incómodo": [6],
"primari": [5],
"termina": [2],
"esvaziar": [9],
"navegar": [5,[9,11]],
"webster": [0,[7,9]],
"soletrada": [4],
"ocultado": [11],
"menus": [5,11,8,7],
"construção": [2],
"definindo": [11],
"permitida": [11],
"estatística": [8,[3,10],6,11],
"validar": [3,[6,11]],
"cjk": [11],
"carregamento": [11],
"endereço": [5],
"qualquer": [11,9,10,2,[5,6],8,[1,3,4]],
"vário": [11,[5,6,8,9,10]],
"lado": [6,11,5,[3,4]],
"pdf": [6,[7,8,11]],
"ordenar": [11],
"permit": [11,5,[2,6,8,9]],
"serviço": [5,11,8],
"requerida": [5],
"chefe": [6],
"empti": [[5,8]],
"redimensionar": [9],
"proceda": [[0,11]],
"traduçõ": [11,6,9,8,10,2],
"agência": [9],
"hexadecim": [2],
"bloco": [2,11],
"toolsshowstatisticsmatchesmenuitem": [3],
"instantâneo": [5],
"viewdisplaymodificationinfononeradiobuttonmenuitem": [3],
"sincronizar": [[5,6]],
"reinserido": [11],
"ordem": [11,9,8],
"tms": [10,6,11],
"paí": [5,11],
"tmx": [6,10,5,11,[8,9]],
"e.g": [[6,11]],
"repo_for_all_omegat_team_project": [6],
"move-s": [11],
"realizado": [6],
"quão": [11],
"nl-en": [6],
"expandir": [11],
"integ": [11],
"carrega": [[6,11]],
"coisa": [[3,6]],
"folha": [11,2],
"intel": [5,7],
"fr-ca": [11],
"mainmenushortcuts.properti": [3],
"gradualment": [6],
"projectaccesswriteableglossarymenuitem": [3],
"realizada": [6],
"premida": [3],
"conhecimento": [6],
"gui": [5,10],
"cmd": [[6,11]],
"peça": [11],
"coach": [2],
"carregu": [[5,11]],
"subtitl": [5],
"produzam": [11],
"sentencecasemenuitem": [3],
"gotohistorybackmenuitem": [3],
"imperativo": [11],
"exportada": [11],
"save": [5],
"subscreveu": [5],
"widgets": [7],
"premido": [[3,8,11]],
"project-save.tmx": [6],
"básico": [5],
"uhhhh": [2],
"exportado": [[8,11]],
"powerpc": [5],
"guloseima": [5],
"optionssentsegmenuitem": [3],
"ortográficos": [7],
"terão": [11,6],
"embora": [11],
"utilizam-s": [5],
"utilização": [11,4,[2,6,7],5],
"pré-definidas": [7],
"optionsaccessconfigdirmenuitem": [3],
"substituirá": [8],
"criativa": [11],
"perfeitament": [6],
"test.html": [5],
"descendent": [11],
"quai": [[5,6,11]],
"xxx": [10],
"qual": [5,11,[6,8]],
"exclusõ": [[6,11]],
"nenhum": [8,[1,5,9]],
"smalltalk": [11],
"com": [11,5,6,9,8,10,[1,2,4],[0,3],7],
"mínimo": [[2,11]],
"sub-menu": [5],
"sessõ": [[5,11]],
"cor": [11],
"tempo": [[4,11],9],
"próprio": [11,[2,6,9]],
"solicita": [5],
"mínima": [11],
"omegat.sourceforge.io": [5],
"pseudotranslatetmx": [5],
"listado": [8,3],
"reparado": [6],
"reduzida": [10],
"pipe": [11],
"própria": [[2,9,11]],
"separador": [9,8],
"básica": [5],
"representa": [[5,11]],
"correcto": [4],
"negrito": [11,9,1],
"esquema": [11],
"targetlanguagecod": [11],
"desligue-s": [6],
"translat": [11,5,[4,8]],
"abri-lo": [11],
"aviso": [[5,11],9],
"registada": [11,5],
"causa": [5,1],
"entrar": [[5,6]],
"exterior": [9],
"assumir": [10],
"efectuada": [11],
"carregada": [8],
"edite-o": [1],
"acompanham": [9],
"largar": [5,[7,9]],
"correcta": [[5,6],1,[0,4,9,10,11]],
"docs_devel": [5],
"recarrega": [8,11],
"tsv": [1],
"ponha": [10],
"extra": [5,[2,6,10]],
"perdida": [6],
"gnome": [5],
"registado": [11],
"traduz": [[6,8,9,11]],
"alterada": [11,[5,6]],
"relutant": [2],
"inválido": [[5,6]],
"quer": [6,11],
"perdido": [6],
"convertido": [[6,11]],
"acrescentar": [6,[9,11]],
"porqu": [11,6,9],
"procur": [11],
"inválida": [5],
"consecutivo": [11],
"encyclopedia": [0],
"deverá": [0,[5,11]],
"significar": [11],
"tido": [11],
"gravador": [6],
"recarregu": [11],
"carregado": [[5,11]],
"confidencialidad": [11],
"preencher": [[6,8]],
"alterado": [6,11,[8,10]],
"iniciarão": [5],
"rapidament": [11],
"optionstagvalidationmenuitem": [3],
"questionávei": [11],
"doca": [5],
"csv": [1,5],
"n.n_linux.tar.bz2": [5],
"suficientement": [6,[10,11]],
"aplicada": [11],
"pt_br": [5],
"agregada": [11],
"pelo": [11,6,[8,10],[5,9],[2,3,4]],
"língua": [6],
"premindo": [11,9,6],
"a-z": [2],
"calculada": [[9,11]],
"evento": [3],
"seguir": [6],
"integrado": [11],
"avançada": [11],
"gravaçõ": [11],
"ligar": [5],
"inseri-la-á": [9],
"aplicado": [11],
"apresentação": [[6,11]],
"assegura": [5],
"prest": [5],
"onlin": [4],
"pela": [8,11,[5,6],[0,3,9]],
"quantificador": [2],
"receita": [6],
"preservar": [11],
"europeus": [11],
"retraduzida": [6],
"vazia": [11,10,[3,8],6],
"próxima": [[6,11]],
"dmicrosoft.api.client_secret": [5],
"javascript": [11],
"marqu": [11,8,4],
"mediawiki": [11,[3,8]],
"transformar-se-ão": [11],
"input": [11],
"caracter": [11,8,[1,2],[3,5,6,9]],
"melhor": [11,[9,10]],
"acção": [8,3,0],
"fisicament": [4],
"obterá": [6],
"vazio": [11,6,[1,5,9]],
"disto": [11],
"versão": [5,6,8,10],
"limita": [6],
"ctrl": [3,11,9,6,8,1,[0,10]],
"document": [11,5],
"diário": [8,3],
"facto": [6],
"produzir": [6,11],
"poi": [11],
"necessidad": [[4,6,11]],
"limite": [7],
"tomada": [8],
"aplicabilidad": [11],
"conseguido": [5],
"desenho": [[6,11]],
"construiu": [9],
"privacidad": [5],
"etiqueta": [11,6,8,[3,5],9],
"por": [11,6,8,5,2,[4,9],10,[1,3],0,7],
"caixa": [11,[2,4],5],
"escrevendo": [8,[5,11]],
"resourc": [5,11],
"correspondência": [11,8,9,3,10,1,6,2],
"desejar": [6,11,[2,5,9,10]],
"correspondências": [7],
"lhe": [11,10],
"descomprimir": [5],
"removida": [[10,11]],
"team": [6],
"xx_yy": [[6,11]],
"coloqu": [[4,6,10,11]],
"encontrará": [5,11],
"docx": [[6,11],8],
"txt": [6,1,[9,11]],
"googl": [5,11],
"reflectirá": [10],
"opendocu": [11],
"união": [11],
"modificá-lo": [6],
"envolvido": [4],
"download.html": [5],
"definir": [11,8,[2,3,9]],
"actualizar": [5,11],
"armazenamento": [11],
"gravada": [[1,10]],
"documento-alvo": [[3,8]],
"source": [7],
"imun": [10],
"align": [11],
"sourceforg": [3,5],
"russo": [5],
"trnsl": [5],
"continua": [6],
"seguem": [[2,8]],
"segmentos-alvo": [[5,11]],
"viewdisplaymodificationinfoselectedradiobuttonmenuitem": [3],
"index.html": [5],
"omegat.tmx": [6],
"validador": [5],
"destacado": [8],
"actual": [8,11,9,10,3,[5,6],1],
"actuai": [[3,8,10]],
"gerir": [6,7],
"determinada": [[6,10,11]],
"quatro": [8],
"colocado": [[8,11]],
"editmultipledefault": [3],
"janelas": [7],
"corresponderia": [11],
"mozilla": [5],
"editfindinprojectmenuitem": [3],
"terminador": [2],
"diffrevers": [11],
"combinada": [11],
"formatado": [6],
"determinado": [11,6,[1,9,10]],
"warn": [5],
"fecha": [8,11],
"dactilografado": [3],
"colocada": [8],
"technetwork": [5],
"acrescentada": [10],
"dactilografada": [11],
"simultâneo": [8],
"plural": [11],
"detecta": [[1,5]],
"plurai": [1],
"expressão": [11,2,5],
"conta": [[5,11],[3,8]],
"atribuída": [5],
"introdução": [[6,11]],
"documento-font": [[3,8]],
"tradução": [11,6,9,8,10,3,5,7,2],
"permitir-lhe-á": [5],
"expressõ": [11,2,[3,4,5,9]],
"implementação": [5],
"exporta": [[6,8,11]],
"semelhant": [11,9,[5,6,10]],
"project.gettranslationinfo": [11],
"disso": [9,[6,8]],
"comando": [5,11,8,6,9,[3,7]],
"mudar": [3,11,6],
"destino": [6,11,[8,9]],
"traduzível": [11],
"atribuído": [3,[5,11]],
"procurará": [11],
"traduzívei": [11],
"cliqu": [11,5,8,9,[1,4]],
"confirmar": [[3,10,11]],
"n.b": [11],
"medida": [[9,11]],
"avançado": [[2,5]],
"start": [5,7],
"windows": [7],
"reportar": [6],
"equal": [5],
"colour": [11],
"n.n_windows.ex": [5],
"glossário": [1,11,9,3,8,[6,7],[0,4,10]],
"gravado": [[1,6],5],
"aplicaçõ": [5,[4,11]],
"abreviar": [2],
"progrid": [10],
"calculado": [11],
"totalment": [6],
"optionsalwaysconfirmquitcheckboxmenuitem": [3],
"tmxs": [6,[3,8]],
"tipo": [11,6,8],
"reconhecida": [11],
"localizada": [11,5],
"possivelment": [[2,6,10]],
"carregará": [6],
"desactualizada": [6],
"qualidad": [6,[8,10]],
"program": [5],
"declaração": [11],
"solução": [6],
"estratégia": [11],
"reconhecido": [1,11],
"localizado": [5,1,[8,11]],
"país-alvo": [11],
"colecçõ": [11],
"vária": [11,[1,2,5,6,8,9]],
"enter": [11,[3,8],5],
"resultant": [5,11],
"defeituoso": [6],
"bidi": [6],
"projectteamnewmenuitem": [3],
"razão": [[4,5]],
"distribuir": [6],
"brasileiro": [5],
"surjam": [6],
"desact": [1],
"pré-traduzir": [6],
"directorate-gener": [8],
"tecnologia": [5],
"separa": [11],
"memori": [5],
"interactiva": [2],
"utilizará": [5,4],
"n.n_mac.zip": [5],
"pedindo": [6],
"realment": [[8,11]],
"questão": [11,6],
"acrescentado": [6],
"especializado": [9],
"prevalecerá": [10],
"copie-o": [6],
"combinado": [5],
"mudem": [6],
"célula": [11],
"omegat.jnlp": [5],
"idioma-paí": [11],
"gerida": [6],
"esqueceu-s": [0],
"devidament": [5],
"comporta": [11],
"consult": [[2,6]],
"dicionários": [7],
"globalment": [11],
"theme": [11],
"gerar": [6,[10,11]],
"note-s": [5,[4,6,11],10],
"n.n_windows_without_jre.ex": [5],
"provável": [[5,9,11]],
"editor": [11,9,8,5,[1,6,7,10]],
"pseudotranslatetyp": [5],
"órfãos": [11,9],
"blocos": [7],
"deseja": [5,[4,11],[3,6,8,10]],
"deslocada": [8],
"falando": [11],
"consistent": [8],
"especificando": [5],
"oferecido": [9],
"prof": [11],
"completament": [11],
"correctament": [[1,6],[8,10]],
"produto": [6],
"dmicrosoft.api.client_id": [5],
"especializada": [9],
"funçõ": [9,11],
"oferecida": [8],
"identificador": [11],
"config-fil": [5],
"cancelada": [8],
"erro": [6,5,8,11],
"trabalhar": [6,11],
"colocando": [3],
"envolvida": [6],
"projectclosemenuitem": [3],
"viewmarknonuniquesegmentscheckboxmenuitem": [3],
"importa": [5],
"dar": [8],
"traduções": [7],
"das": [11,9,6,10,[3,8],5],
"pedido": [[6,8]],
"sobr": [5,[6,9,11],[8,10],1],
"também": [5,11,6,9,4,8,[1,3,7,10]],
"navegação": [[5,11]],
"idêntico": [10,6,[2,11]],
"aparência": [11],
"fundo": [[8,9,10,11]],
"group": [9],
"corpo": [4],
"auto-povoado": [11,8],
"ocorrerem": [11],
"canadiano": [11],
"geral": [11],
"findinprojectreuselastwindow": [3],
"system-user-nam": [11],
"reservada": [4],
"gerai": [11],
"associado": [[5,8]],
"format": [11],
"pausa": [4],
"particular": [5],
"letra": [8,3,[2,11],6],
"readme.txt": [6,11],
"campo": [11,9,[5,8],4,6],
"fácil": [11],
"flutuant": [11],
"languagetool": [11,8],
"console.println": [11],
"sequência": [11],
"source.txt": [11],
"estejam": [11,[8,9]],
"files.s": [11],
"página": [8,11,[3,6],5],
"exibido": [6,[8,11]],
"exchang": [1],
"exclusão": [11],
"contínuo": [11],
"request": [5],
"tent": [11],
"part": [9,11,[8,10],[1,4],[3,5,6]],
"currseg": [11],
"precisarem": [10],
"mostrem": [[8,11]],
"inibir": [11],
"point": [11],
"torno": [11],
"computador": [5,11,7],
"principal": [7],
"pare": [11],
"europeia": [6],
"identifica": [11],
"para": [11,6,5,8,9,3,4,[1,10],2,[0,7]],
"então": [11,[6,10],[4,8]],
"corresponderão": [11],
"rotineira": [5],
"torna": [6],
"próximo": [11,8,[0,1,2,4,5,6,7,9,10]],
"descarregar": [4],
"copiá-lo": [5],
"project_files_show_on_load": [11],
"extensões": [7],
"defini-lo": [1],
"ltr": [6],
"consoant": [2],
"alternativa": [11,9,8,[3,5]],
"escolhida": [8],
"idêntica": [8,[3,5,6,9,11]],
"substituído": [[6,11]],
"optionsexttmxmenuitem": [3],
"downloaded_file.tar.gz": [5],
"pesquis": [0],
"três": [6,10,[0,9],1],
"comparar": [11],
"alternativo": [[6,8]],
"tema": [11],
"build": [5],
"normalment": [5,[6,8]],
"facilment": [6],
"acordo": [11,[5,10]],
"segmentação": [11,[2,6],8,[3,10]],
"marketplac": [5],
"account": [[5,11]],
"sejam": [6,11],
"antigo": [11],
"sublinhada": [4],
"dhttp.proxyhost": [5],
"acessória": [[9,10]],
"condiçõ": [6],
"entries.s": [11],
"ser-lh": [5],
"linguístico": [11,4],
"del": [[9,11]],
"especificar": [11,5,[3,6,10]],
"gotonextuntranslatedmenuitem": [3],
"período": [6],
"quiser": [11,[3,5,8,9]],
"targetlocal": [11],
"barra": [9,5,11,[2,7]],
"sublinhado": [1],
"marca": [11,[2,9]],
"daí": [5],
"path": [5],
"apagado": [6],
"reconhecimento": [6],
"ignorar": [11,[4,8]],
"dez": [8],
"dúvida": [10],
"interferir": [8],
"preferirão": [6],
"surgem": [11],
"formiga": [6],
"acerto": [11],
"prima": [11,9,[1,5]],
"susceptível": [6],
"relativo": [6],
"allsegments.tmx": [5],
"verificaçõ": [8],
"revisão": [[6,10]],
"corrector": [4,[7,10,11]],
"especi": [6,11],
"reinici": [3],
"activa": [[8,11]],
"mantendo": [10],
"protótipo": [11],
"configur": [5],
"helpcontentsmenuitem": [3],
"oculto": [10],
"omegat-org": [6],
"definiçõ": [[5,11],8,6,4],
"afix": [6],
"unicode": [7],
"relativa": [[2,9]],
"descript": [3],
"carregar": [11,6,8],
"lematizar": [11],
"activo": [[8,10,11]],
"projectaccessdictionarymenuitem": [3],
"oposição": [11],
"exactament": [11,4],
"descrita": [11],
"existem": [11,[1,4]],
"noutra": [5,6],
"atravé": [11,5,[2,6,9]],
"operação": [[6,11]],
"optionsworkflowmenuitem": [3],
"fica": [5],
"interior": [9],
"visualizador": [9],
"digitando": [11],
"autorização": [6],
"releas": [6],
"inserção": [11],
"eliminado": [11],
"pára": [5],
"outro": [6,11,[5,9],8,[0,1,10]],
"ortográfico": [4,11,[7,8,10]],
"term": [9],
"sparc": [5],
"segmentar": [11],
"definido": [[8,10,11],[4,6,9]],
"processar": [11,5],
"consistem": [11],
"nunca": [2,11],
"povoado": [[3,8]],
"ortográfica": [4,[1,2,7]],
"efeito": [11,9],
"permanecerá": [11],
"duden": [9],
"salvaguarda": [[6,10]],
"linguística": [[0,4]],
"antiga": [11,5],
"vírgula": [[2,11],[1,6]],
"dia": [6],
"spotlight": [5],
"arrast": [5],
"frequentement": [6,11],
"segmento-font": [9,11,[8,10]],
"meia": [11],
"seguint": [11,2,3,8,5,9,6,[0,10]],
"desempacotar": [[0,5]],
"dir": [5],
"meio": [[2,6,11]],
"actualizado": [[1,11]],
"div": [11],
"subdir": [6],
"diz": [5],
"etiquetas-font": [[3,8]],
"âmbito": [6],
"viewfilelistmenuitem": [3],
"hierarquia": [10],
"mostra": [8,[9,11],0],
"test": [[2,5]],
"recomenda": [11],
"definida": [6,[3,5,8,11]],
"omegat": [5,6,11,8,10,[3,7],4,1,[0,9],2],
"execuçõ": [[6,10]],
"imprim": [11],
"forward-backward": [11],
"útei": [2,5],
"degradada": [10],
"feche-a": [11],
"desaparecerão": [4],
"coloca": [9],
"ideia": [11],
"altura": [11],
"final": [[2,10,11]],
"série": [[6,11]],
"nalgun": [11],
"algun": [11,[5,8],[0,1,9,10]],
"directament": [5,11,[1,8,10]],
"algum": [11,[4,5,6]],
"finai": [11],
"file-source-encod": [11],
"conselho": [[5,6]],
"desmarcada": [11],
"distribuição": [5],
"verificado": [[8,11]],
"extensõ": [11,0,[1,9]],
"tabulação": [[1,11],2],
"some": [6],
"cadeia": [11,[6,8],9],
"virtual": [11],
"estrangeiro": [11],
"quebra": [11],
"console-align": [5],
"uma": [11,5,6,8,10,2,[4,9],[1,3]],
"entr": [11,6,[8,10],9,[1,2]],
"encontra": [[2,6,11]],
"pormenor": [6],
"ms-dos": [5],
"impraticável": [5],
"projectopenrecentmenuitem": [3],
"diálogos": [7],
"senha": [11,6],
"sensível": [[2,11]],
"validação": [11,[5,6]],
"segmento-alvo": [8,11,9],
"bela": [2],
"terá": [[5,9,11]],
"chamamo": [11],
"alterar": [11,5,6,[3,9]],
"restaura": [[8,9,11]],
"mantenha": [10],
"inser": [8,11],
"editexportselectionmenuitem": [3],
"criou": [[4,6]],
"und": [4],
"project_save.tmx.temporari": [6],
"procurada": [11],
"grand": [[4,11]],
"home": [6,5],
"actualizaçõ": [11,[5,8]],
"tecnicament": [11],
"eliminar": [[6,11],5],
"projectaccesstargetmenuitem": [3],
"partir": [5,11,[6,8],[1,3,7,10]],
"editoverwritemachinetranslationmenuitem": [3],
"existam": [11],
"critério": [11],
"ingreek": [2],
"seguro": [11,6],
"editando": [5],
"utilizada": [[6,11],[2,4,5,9]],
"procura-chav": [2],
"revertida": [6],
"anfitrião": [11],
"es_es.aff": [4],
"convert": [11,[6,8]],
"ignor": [10],
"desbloqueado": [11],
"aparecerão": [11,4],
"utilizado": [11,5,6,[4,8],[3,10]],
"curto": [11],
"projectexitmenuitem": [3],
"baixo": [11,9],
"diálogo": [11,8,[6,10],[1,4,9]],
"aligndir": [5],
"mão": [6],
"system-host-nam": [11],
"action": [8],
"text": [5],
"editregisteruntranslatedmenuitem": [3],
"init": [6],
"terminado": [9,8],
"creat": [11],
"python": [11],
"imediatament": [[1,8,11]],
"oferecendo": [9],
"es_mx.dic": [4],
"espaço": [11,2,8,3,1],
"alinhará": [5],
"infix": [6],
"bell": [2],
"útil": [11,6,[5,9,10]],
"maco": [5,1],
"especialment": [11],
"tarbal": [0],
"repor": [11,6],
"curso": [9],
"insensível": [11],
"apareça": [6],
"doc": [6],
"operar": [11],
"servem": [11],
"doi": [11,5,6,4,[8,10]],
"publicar": [6],
"submet": [6],
"deixa": [11],
"executado": [[5,8,11]],
"hora": [[6,8,11]],
"paramet": [5],
"auto-propagação": [11],
"dos": [11,6,5,3,9,4,7,[0,1,2,8],10],
"importada": [6],
"fech": [6],
"mac": [3,6],
"dinâmica": [11],
"painéi": [9,[5,11],[6,10]],
"permanecem": [5],
"gratuito": [[4,5,11]],
"file": [11,5,6,8],
"mai": [11,5,9,[2,10],6,[4,8],[1,3]],
"yyy": [9],
"mal": [11,4],
"painel": [9,11,8,1,6,[4,5,7,10]],
"operativo": [5,11,8,10],
"man": [5],
"alinhamento": [11,6,5],
"especifica": [11,5],
"map": [6],
"bandeira": [11],
"mas": [6,11,[1,2],[5,9],[4,8,10]],
"gratuita": [5,7],
"começa": [5],
"recuperada": [5],
"opinião": [9],
"may": [11],
"anteriorment": [[6,8]],
"vantajosa": [11],
"desnecessária": [11],
"subtracção": [2],
"tard": [[5,11],[8,9,10]],
"meno": [[5,10],[6,11]],
"aceit": [10,3],
"menu": [3,7,11,[8,9],5,[1,6]],
"ment": [11],
"url": [6,11,[4,5,8]],
"especificament": [[5,8]],
"pesquisa": [11],
"unidad": [11,10],
"megabyt": [5],
"uppercasemenuitem": [3],
"importado": [6,1],
"viewmarkuntranslatedsegmentscheckboxmenuitem": [3],
"ligeira": [10],
"contiverem": [11],
"não-omegat": [11],
"combinação": [[0,3,5]],
"a-za-z": [2,11],
"actualizada": [[5,6]],
"anular": [6],
"restaurá-los-á": [9],
"parêntes": [11],
"válido": [11,5],
"usa": [11],
"negação": [2],
"opera": [11],
"oracle\"": [5],
"sinai": [9],
"use": [11,[5,8],9,[0,1]],
"sinal": [1],
"cinzento": [8,11],
"tentará": [[5,11]],
"re-aplicado": [6],
"uso": [[1,5,7,11],0],
"recebida": [5],
"clique-o": [5],
"omegat.jar": [5,[6,11]],
"source-pattern": [5],
"omegat.app": [5],
"válida": [11],
"usr": [5],
"explicitament": [11],
"alter": [[4,5]],
"lista": [11,[4,7,8],[1,2,5,6]],
"progresso": [[6,9]],
"verificador": [11],
"desfaz": [[3,8]],
"assumido": [11],
"utf": [1],
"clicar": [11,5,9,[4,8]],
"numa": [[10,11],[1,6,8],[0,4,5]],
"problema": [[1,8],6,0,5],
"exemplos": [7],
"sort": [11],
"recebido": [11],
"produzida": [6],
"convenient": [5,6],
"descrito": [5,11],
"true": [5],
"fechando": [9],
"envolvem": [5],
"dsl": [0],
"assegure-s": [[0,4]],
"present": [11,10,[0,5]],
"adicion": [[3,5]],
"servir": [6],
"groovi": [11],
"trate": [11],
"evitar": [6,[10,11]],
"trata": [11],
"desactivar": [11,8],
"devolvê-lo": [11],
"n.n_windows_without_jre.zip": [5],
"hífen": [5],
"med": [8],
"especificado": [5,11],
"en.wikipedia.org": [9],
"kmenueditor": [5],
"dtd": [5],
"rotular": [8],
"especificadores": [7],
"especificada": [11,[3,6]],
"tentar": [6,11],
"acima": [11,6,[5,9],[2,4,10],[0,1,8]],
"lançamento": [5],
"palavra-chav": [11],
"projectcompilemenuitem": [3],
"abrirá": [[4,11]],
"console-transl": [5],
"conseguir": [[5,6,9,11]],
"master": [6],
"dua": [11,5,[6,8,9],4],
"kmenuedit": [5],
"imunidad": [10],
"gotonextuniquemenuitem": [3],
"conform": [[5,6,11]],
"mês": [6,5],
"utilizadores": [7],
"inseparável": [11],
"xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx": [5],
"caminho": [5,6],
"writer": [6],
"inseri-la": [9],
"inseparávei": [8,3],
"wordart": [11],
"princip": [9,11,[3,6]],
"descrevem": [9],
"dalloway": [11],
"rubi": [11],
"optionsviewoptionsmenuitem": [3],
"depend": [8,[1,5,6]],
"ficheiros-alvo": [11,6,[3,8]],
"desmarqu": [11],
"commit": [6],
"programa": [5,[6,11]],
"targetlocalelcid": [11],
"dependerá": [6],
"apresentar": [11],
"project_stats_match.txt": [10],
"reflect": [8],
"dvd": [6],
"sentido": [4,[10,11]],
"xmx2048m": [5],
"recebeu": [9],
"resident": [10],
"xxxxxxxxxxxxxxxx.xxxxxxxxxxxxxxxx.xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx": [5],
"rastrear": [11],
"edita": [9],
"executa": [11],
"retida": [[5,11]],
"independentement": [[1,11]],
"capítulo": [[2,6,9,11]],
"ficheiros-font": [6,[8,11],[3,5]],
"user.languag": [5],
"regex": [2,7],
"escolha": [5,11],
"glifo": [8],
"abri": [3],
"meta": [3],
"keystrok": [3],
"automatizarão": [5],
"krunner": [5],
"colocá-lo": [1],
"libreoffic": [4,[6,11]],
"abra": [6,[5,8,10,11]],
"controlar": [[5,11]],
"mantê-lo": [11],
"desligar": [11],
"globai": [[1,11]],
"escolh": [11,[4,6]],
"global": [11],
"colocá-la": [10],
"defin": [11,8],
"exacta": [11,[1,8]],
"correcção": [4],
"fiávei": [[10,11]],
"gestão": [6],
"criação": [6,11,1],
"modificador": [3],
"valor": [2,11,[1,5]],
"botão": [11,5,9,[1,4,8]],
"reconhec": [11,6],
"atribuição": [5],
"texto": [11,6,8,9,10,[1,2,4],7,[3,5]],
"viewdisplaysegmentsourcecheckboxmenuitem": [3],
"aconselhamo": [11],
"gravou": [8],
"editregisteremptymenuitem": [3],
"alojado": [11],
"permanec": [11,10],
"ibm": [5],
"terminando": [8],
"faça": [5,10,[6,11]],
"comun": [[1,6]],
"open": [11],
"comum": [11],
"www.oracle.com": [5],
"linguagem": [11],
"aconteça": [[10,11]],
"habituai": [8],
"linguagen": [11],
"termo-alvo": [1,8],
"acrescentá-la": [4],
"project": [[10,11],[5,6,7,9]],
"esquerda": [6,11,[2,5,7,8,9]],
"xmx1024m": [5],
"média": [11],
"entend": [6],
"dirigido": [11],
"cria-o": [11],
"positivo": [11],
"único": [11,9,[3,5]],
"uni-la": [6],
"alemão": [11],
"conteria": [11],
"arquivo": [[0,5]],
"incluir": [11,6],
"incluiu": [9],
"selecção": [8,11,3,9,0],
"estiverem": [8,11],
"propriedades": [7],
"penalty-xxx": [10],
"gotonextsegmentmenuitem": [3],
"ciclo": [3],
"única": [11],
"legislação": [6],
"ida": [6],
"interrupção": [11],
"nnn.nnn.nnn.nnn": [5],
"introduzida": [8,11],
"melhorado": [11],
"acrescentar-lh": [10],
"armazenado": [11,[5,8,10]],
"abort": [5],
"ficheiro": [11,6,5,8,10,1,4,9,3,0,7],
"guia": [7,[5,6],[0,10]],
"opçõ": [11,8,5,[3,9],4,6,[2,10]],
"fazendo": [5],
"armazenada": [11,[5,9]],
"idx": [0],
"internet": [11,4],
"privilegiar": [11],
"que": [11,6,5,9,8,10,4,[1,3],[0,2]],
"saltar": [11,9],
"existirem": [11,5],
"souber": [11],
"serão": [11,8,[5,6],[1,9],10,4],
"requer": [[4,6,8,9,11]],
"lançar": [5],
"início": [11,[2,10],5,[0,1,3,4,6,8,9]],
"documentos-alvo": [11,8,6],
"intacta": [10],
"avança": [[8,11]],
"interferirá": [5,11],
"engenheiro": [6],
"causar": [11],
"ocorr": [5,[9,11]],
"terminológica": [11],
"projectaccesscurrentsourcedocumentmenuitem": [3],
"renomeação": [6],
"linux": [5,[2,7,9]],
"nessa": [11],
"cálculo": [11],
"autónoma": [5],
"actualment": [8,11,10],
"conhecida": [[6,9]],
"exigir": [6],
"aparec": [11,5,[3,9]],
"externo": [11,1],
"dentro": [[10,11],[5,6,8,9]],
"estabelecida": [11],
"inferior": [11,9,[3,6,8]],
"desliga": [[8,9]],
"file.txt": [6],
"codificação": [11,1,7],
"elimina": [8],
"externa": [11,8,[3,6]],
"es-mx": [4],
"termo-font": [1,8],
"multinacion": [6],
"ifo": [0],
"pode": [11,5,6,9,8,4,10,3,1,[0,2]],
"ajustando": [11],
"pré-definição": [11,8,[5,6],[1,10],9],
"processado": [5],
"manualment": [[6,11],[1,4,8]],
"considerado": [11],
"esquerdo": [11],
"base": [11,[4,5,6,8]],
"stem": [9],
"octal": [2],
"segunda": [1],
"abreviatura": [11],
"sistema": [5,11,6,4,8,[3,10]],
"lote": [5],
"incluem": [11],
"xx.docx": [11],
"adicioná-la": [5],
"contendo": [11,[6,8],[5,10]],
"excluindo": [[6,11]],
"alinhado": [8],
"guardá-lo": [6],
"pálido": [8],
"produzindo": [2],
"segundo": [[9,11],[3,5,6]],
"optionsautocompleteautotextmenuitem": [3],
"essencialment": [4],
"geração": [11],
"alinhada": [11],
"demorar": [4],
"pouco": [[6,11]],
"dependent": [1],
"cortar": [9],
"vai": [5,[6,11]],
"relata": [11],
"zip": [5],
"pré-definida": [11,9,8,[2,3,5,6,10]],
"gravação": [6,11,[3,8]],
"indica": [9],
"inserir": [11,8,3,[1,5,9]],
"juntament": [11],
"hesit": [6],
"sub-pasta": [10,6,11,5,[0,4]],
"pré-definido": [3,1,6,[5,7,8,11]],
"concis": [0],
"nesta": [[1,4,6,10]],
"verificarão": [5],
"customer-id": [5],
"necessitem": [9],
"inicia": [5,8],
"comparação": [11],
"word": [6,11],
"term.tilde.com": [11],
"melhoramento": [8],
"enquanto": [[8,9],[6,10]],
"pré-configurada": [11],
"desactivada": [8],
"anexa": [8],
"permanecerão": [10],
"essa": [11],
"viewmarknotedsegmentscheckboxmenuitem": [3],
"receb": [[5,9]],
"espanhol": [4],
"documentos-font": [11],
"correcçõ": [11,[6,8]],
"japones": [11],
"servidor": [6,[5,11],10],
"código": [3,4,11,5,6],
"sendo": [11],
"lingvo": [0],
"gotomatchsourceseg": [3],
"alfabética": [11],
"é-lhe": [11],
"exista": [11],
"optionssaveoptionsmenuitem": [3],
"excel": [11],
"movido": [[9,11]],
"entrada": [11,8,1,6,3,[2,5,9]],
"runn": [11],
"stardict": [0],
"traduzi-lo": [6],
"omegat.l4j.ini": [5],
"span": [11],
"incluída": [11],
"lembrará": [11],
"alimentado": [5],
"atenção": [[5,6]],
"bloqueado": [5],
"crédito": [8],
"número": [11,9,8,1,[5,6],[3,10]],
"alinhar": [[8,11]],
"diapositivo": [11],
"serem": [11],
"estilística": [11],
"limiar": [11],
"space": [11],
"domínio": [11],
"constituída": [9],
"pré-definiçõ": [11],
"pt_pt.aff": [4],
"incluído": [6],
"tecla": [3,11,1],
"tomar": [[6,11]],
"simpl": [6,11,[1,2],[4,5]],
"influência": [6],
"adição": [[5,11]],
"libertado": [3],
"html": [11,5],
"editável": [1,[3,8]],
"poluir": [6],
"publicado": [6],
"pacot": [5,8],
"thunderbird": [4,11],
"extensão": [11,1,[0,6],[9,10]],
"ver": [11,6,[3,5,8],[2,7,10],9,[1,4]],
"editselectfuzzy3menuitem": [3],
"especificará": [5],
"inúmera": [6],
"recarregar": [[3,8,11]],
"fecha-o": [8],
"verifica": [8],
"comprimir": [11],
"vez": [[5,11],6,8,[2,9],3],
"artund": [4],
"suporta": [6,5],
"fals": [[5,11]],
"captura": [6],
"mts": [11],
"project.projectfil": [11],
"outra": [6,5,11,[4,9],[1,2,8,10]],
"este": [7],
"defeito": [11,6,2],
"esta": [11,8,5,10,6,[4,9]],
"regra": [11,[2,6],[5,10]],
"iniciador": [5],
"relativament": [11],
"focar": [11],
"àquela": [11],
"visível": [11,10],
"morto": [6],
"aquela": [[9,10,11]],
"procura": [11,8,[2,6]],
"instruções": [7],
"ferramenta": [11,6,[2,3,8],10,[5,7,9]],
"acerca": [[3,8]],
"jres": [5],
"www.ibm.com": [5],
"frequent": [5],
"conversõ": [6],
"velho": [6],
"marcador": [[9,11]],
"momento": [9],
"proteg": [11],
"seleccionado": [11,8,[5,6],4,3],
"shortcut": [3,11],
"prefixada": [10],
"seleccionada": [8,[9,11],[3,5]],
"auto-povoamento": [11],
"tmx2sourc": [6],
"contra": [[6,11]],
"grava": [8,6],
"lookup": [8],
"quantidad": [5,11],
"renomear": [11,6],
"grave": [[3,5]],
"ini": [5],
"poder": [11,[4,5]],
"proced": [6],
"existent": [11,[5,6],[1,10]],
"command": [3],
"podem": [11,6,5,[1,10],8,9],
"restring": [11],
"maximização": [9],
"dhttp.proxyport": [5],
"vocabulário": [4],
"existir": [10,[1,6,11]],
"daquel": [[6,11]],
"contudo": [[5,6,11],4],
"seria": [5,[6,11]],
"personalizar": [11],
"caracteres": [7],
"movê-lo": [[9,11]],
"baseada": [11],
"objecto": [11],
"viewmarkbidicheckboxmenuitem": [3],
"subrip": [5],
"branco": [11,2,8],
"notar": [6],
"notas": [7],
"realçada": [9],
"preferir": [11],
"idioma": [5,[6,11],4,9,8],
"quanta": [8],
"toda": [11,6,[4,8,9,10]],
"implementar": [5],
"excepção": [11],
"compatível": [5,6],
"score": [11],
"colado": [8],
"estatuto": [11],
"colar": [9],
"baseado": [11,[0,4]],
"reutilizar": [6,7],
"tiverem": [8],
"portanto": [5],
"volum": [11],
"introduza": [8],
"incluindo": [11,[6,9],8],
"aparecerá": [[8,10,11]],
"desligada": [11],
"diagrama": [11],
"passo": [6,11,10,[0,8]],
"fonte": [7],
"verificar": [[6,11],4,9,[0,1,5,10]],
"passa": [8,9],
"raw": [6],
"visto": [6],
"tornar": [11,10],
"version": [5],
"aproximadas": [7],
"pontuação": [11],
"utilizarão": [11],
"volta": [6,[5,9]],
"folder": [10,[6,7,9]],
"está": [11,6,[5,8],[9,10],1,4],
"desligado": [[8,9]],
"rolar": [11],
"copia": [[8,11]],
"vista": [[5,9,11]],
"enquadrar": [9],
"efectivament": [[5,8,11]],
"aaa": [2],
"conceb": [2],
"traduzirá": [5],
"contemporari": [0],
"solari": [5],
"instalá-las-ão": [5],
"atribuir": [5],
"projecteditmenuitem": [3],
"preferida": [11,9],
"manuai": [11,6],
"representam-s": [11],
"ambient": [5,11,[1,9]],
"contêm": [[6,11]],
"último": [8,[10,11]],
"itálico": [11],
"manual": [8,[3,4,11]],
"britannica": [0],
"configurar": [11],
"bloqueio": [9,5],
"cima": [11],
"conjunto": [11,1],
"funciona": [8,11,5],
"apagar": [[10,11]],
"preferências": [7],
"oficial": [7],
"wikipedia": [8],
"mudança": [11],
"japonê": [11,5],
"conter": [[5,6],[9,11],[3,4,10]],
"pt.aff": [4],
"significado": [11],
"fase": [[6,10]],
"abc": [2],
"rcs": [6],
"minimiza": [9],
"mapeado": [6],
"contém": [10,11,[5,6,9],[0,7,8]],
"aced": [3,11,5,[6,8,9]],
"copiá-los-á": [11],
"flexibilidad": [11],
"eventos-chav": [3],
"textual": [6],
"todo": [11,6,8,5,9,10,[2,3]],
"abr": [8,11,4],
"iceni": [6],
"seleccionar": [11,3,[5,8,9],4,10],
"mandar": [11],
"progressivament": [[10,11]],
"pasta-font": [[3,5,8]],
"n-ésima": [8],
"nomeado": [[3,4]],
"preferido": [9],
"algoritmo": [[3,8,11]],
"fornec": [11,5,4],
"dividido": [11,9],
"compilação": [5],
"teste": [7],
"aconselhável": [11],
"conteúdo": [11,3,6,[5,10],8,[0,9]],
"iso": [1],
"fará": [11],
"controlo": [6,8,[2,3]],
"tradicionai": [5],
"farão": [2],
"preferência": [8,11,[4,5],[1,3,6]],
"destina-s": [[2,5,6]],
"bidireccion": [8,[3,6]],
"recuar": [8,3],
"estrutura": [10,11],
"similaridad": [11],
"realçar": [9],
"glossary.txt": [6,1],
"mapeamento": [6,11],
"dsun.java2d.noddraw": [5],
"estrutur": [11],
"ela": [[6,9,11]],
"ajustamento": [11],
"ele": [11,6,[5,9],[1,2,3,8]],
"incondicionalment": [10],
"implícito": [11],
"add": [6],
"recolha": [6],
"chines": [11],
"segmentado": [11],
"altera": [8,11],
"gravar": [11,[6,8],3],
"instruçõ": [5,11],
"x0b": [2],
"funcionalidad": [11,[0,6,8]],
"última": [8,3,[5,11],6],
"http": [6,5,11],
"descrev": [6,11],
"optionsautocompleteshowautomaticallyitem": [3],
"mencionado": [5],
"acçõ": [[5,8]],
"deve-s": [6],
"detalh": [11,8,[5,6,9]],
"larouss": [9],
"tcl-base": [11],
"emo": [3],
"untar": [0],
"ignorada": [3],
"substituição": [11,8],
"significa": [[5,6,11]],
"filters.conf": [5],
"enviar": [6,[8,11]],
"pasta": [5,6,11,10,8,1,4,[0,3,9]],
"invertida": [11],
"projectsinglecompilemenuitem": [3],
"mexicano": [4],
"irá": [[4,6,11]],
"avaliado": [11],
"insira": [11,5],
"ignorado": [11,5],
"não": [11,[5,6],8,9,1,10,[2,4],3,[0,7]],
"formatação": [6,11,10],
"abrir": [11,5,8,3,9,6],
"contiv": [6],
"constituirá": [11],
"adicionar": [11,6,5,1,3,[8,9,10]],
"iniciar": [5,11,[6,7]],
"auto-completar": [11],
"aninhada": [1],
"amarelo": [[8,9]],
"myfil": [6],
"comuns": [7],
"aproximadament": [6],
"categorias": [7],
"progressivo": [11],
"adicionam": [6],
"iniciai": [11],
"instruir": [4],
"deveria": [11],
"adicionai": [11,[2,6,10]],
"indicam": [11],
"moeda": [2],
"mover": [11,9],
"consegue-s": [11],
"estiv": [11,5,8,[0,3,4,6,10]],
"entidad": [11],
"clone": [6],
"minúscula": [2,[3,8,11]],
"notem": [6],
"reescrev": [11],
"registar": [8,[3,11]],
"targetlanguag": [11],
"quanto": [[6,9,11]],
"indicar": [[5,6,11]],
"filtro": [11,6,8,5,[3,10]],
"reiniciar": [[4,11]],
"backup": [6],
"properti": [[5,11]],
"título": [[8,11],3],
"futura": [6],
"coreano": [11],
"durant": [11,[5,6],10],
"system-os-nam": [11],
"editselectfuzzyprevmenuitem": [3],
"optionstabadvancecheckboxmenuitem": [3],
"copiam": [6],
"partilha": [6],
"isto": [[5,11],6,9,[4,10],[2,8]],
"editado": [8],
"copiar": [4,6,[3,8,9,10]],
"simpledateformat": [11],
"sempr": [11,6,1,[3,8]],
"optionsviewoptionsmenuloginitem": [3],
"nas": [11,[0,1,2,8,10]],
"excluído": [6],
"parágrafo": [11,8,6],
"exib": [8],
"tar.bz2": [0],
"voltarão": [6],
"invariável": [11],
"restaurar": [[9,11],3],
"eliminação": [11],
"referência": [6,1,[2,9,11]],
"comentário": [11,9,[1,5],[3,8]],
"entrarem": [3],
"comentada": [11],
"excluída": [6],
"bundle.properti": [6],
"pergunta-lh": [5],
"script": [11,8,5],
"impacto": [11],
"gostar": [11],
"idiomas": [7],
"introduzir": [11,2],
"conformidad": [[9,11]],
"atalho": [3,5,[8,11],[2,6]],
"system": [11],
"canadá": [5],
"spellcheck": [4],
"x64": [5],
"isso": [11,6],
"credenciai": [11],
"ajudar": [6],
"colega": [9],
"nada": [2,[3,8]],
"keyev": [3],
"diapositivos-mestr": [11],
"demasiado": [11],
"dicionário": [4,0,[8,11],9,[6,7,10],[1,3]],
"premir": [11,8,[6,9]],
"isn\'t": [2],
"individualment": [11],
"querer": [11,5],
"locai": [6,[5,11]],
"local": [6,5,[8,11],4],
"janela": [11,[8,9],5,4,[3,6,7,10]],
"esvaziado": [[8,11]],
"navegu": [[4,5,6]],
"pontual": [11],
"era": [6],
"atalhos": [7],
"personalização": [3,7,2],
"desnecessariament": [6],
"escrito": [[8,11]],
"eliminando-a": [11],
"confirmação": [11],
"segmenta": [11],
"ecap": [1],
"escrita": [11],
"multi-paradigma": [11],
"optionsteammenuitem": [3],
"tome": [10],
"mostrada": [11,8,10,[5,6]],
"deixada": [11],
"linha": [5,11,2,3,[6,10],8,[1,7,9]],
"gzip": [10],
"decidir": [11],
"começam": [5],
"divisão": [11],
"verifiqu": [6,[0,4]],
"estará": [[6,8,11]],
"repo_for_all_omegat_team_project_sourc": [6],
"partida": [[9,11]],
"começar": [[3,6,11]],
"lento": [11],
"segmento": [11,8,9,3,10,6,1,5],
"mostrado": [11,9,1,8,5],
"deixado": [11,[5,6,8]],
"esc": [11],
"cada": [11,8,6,[1,9]],
"x86": [5],
"futuro": [6],
"quantificadores": [7],
"cita": [2],
"nostemscor": [11],
"função": [8,[4,11],5,1],
"acesso": [11,[5,8],0],
"renomeá-lo": [4],
"ess": [11,[6,8]],
"variávei": [11],
"disponívei": [11,5,[3,4],8,[2,6,9,10]],
"est": [11,5,8,6,[2,4,10]],
"es_mx.aff": [4],
"grupo": [6,[2,11]],
"topo": [11,9],
"deixarão": [11],
"correspond": [11,2,4,8],
"console-createpseudotranslatetmx": [5],
"mode": [5],
"renomeá-la": [6],
"etc": [11,[5,6,9],[0,2,10]],
"longman": [0],
"nem": [5],
"fuzzyflag": [11],
"disponível": [5,6,[3,4,8]],
"toolsshowstatisticsstandardmenuitem": [3],
"modo": [5,6,11,9,[1,8,10]],
"suportam": [6],
"merriam": [0,[7,9]],
"escap": [2],
"new": [5],
"repositório": [6,[8,11],5],
"percentagem": [9,11],
"read": [11],
"percentagen": [9,10],
"alt": [[3,5,11]],
"projectname-omegat.tmx": [6],
"real": [9],
"sensato": [4],
"típico": [[5,6]],
"etiquetar": [11],
"intervalo": [11,6,[2,8]],
"renomeado": [6],
"substantivo": [[9,11]],
"seleccionou": [[4,11]],
"glossários": [7],
"collect": [9],
"indesejável": [8],
"rede": [[5,6]],
"respectivo": [6],
"separadament": [11,[3,6]],
"fazer": [[5,11],4,[6,9,10]],
"ainda": [11,[2,6,8,9]],
"forma": [11,6,5,10,[3,8,9]],
"máquina": [11,5,8],
"geralment": [[9,11],2,[6,10]],
"especificação": [11],
"desejada": [[5,11]],
"pode-s": [[3,10,11]],
"n.n_without_jre.zip": [5],
"and": [5],
"respectiva": [11],
"modifica": [10],
"instrução": [11],
"não-limit": [2],
"desejado": [5,[6,8,11]],
"universai": [11,6],
"magento": [5],
"ano": [6],
"crie": [6,4],
"minuto": [6,[8,11]],
"ant": [11,8,6,[5,10],[1,4,9]],
"proposta": [9],
"cria": [8,5,6],
"toqu": [11],
"conclusão": [11,3,8,1],
"adjacent": [9],
"sintax": [11,3],
"haver": [[0,6,11]],
"apesar": [11],
"rigoroso": [6],
"estar": [5,1,[3,6,9,11]],
"offlin": [6,5],
"leitura": [6],
"possível": [11,[6,9],[2,5],1],
"u00a": [11],
"possívei": [6,[1,3,5,11]],
"helplastchangesmenuitem": [3],
"aplica-s": [5,[6,9,10,11]],
"realçado": [11],
"omegat.ex": [5],
"descomprimida": [5],
"efectua": [8],
"começando": [2],
"webster\"": [0],
"shift": [3,11,[6,8],1],
"sourcetext": [11],
"confundir": [11],
"permitindo-lh": [11],
"pseudotradlatetyp": [5],
"dupla": [2],
"expressões": [7],
"adicionado": [[1,6],[5,8]],
"java": [5,11,3,2,[6,7]],
"exe": [5],
"maximiza": [9],
"movendo": [8],
"english": [0],
"lógicos": [7],
"jar": [5,6],
"api": [5,11],
"lang2": [6],
"lang1": [6],
"ficheiro-font": [11,6,8],
"editselectfuzzy2menuitem": [3],
"project_save.tmx": [6,10,11],
"não-espaço": [2],
"além": [5,[0,8,9,11]],
"baseia-s": [11],
"dictionari": [0,10],
"criar": [11,6,[5,8],3,[1,7,9,10]],
"implementada": [[5,11]],
"adicionada": [[5,6,10]],
"deixando": [5],
"cair": [5],
"relação": [[4,9]],
"instalação": [5,4,[7,11],9],
"capaz": [11,[4,9]],
"modelo": [11],
"criam": [6],
"modificaçõ": [[6,11]],
"informação": [[6,11],5,3,8],
"penalização": [10],
"seleccionando": [11,[5,8,9,10]],
"dictionary": [7],
"marcar": [8,3,11,[1,5]],
"marcas": [7],
"obtenha": [5],
"contadores": [7],
"não-palavra": [2],
"acrescentando": [11],
"feito": [6,5,9],
"modificado": [6,8,[1,3,5]],
"editselectfuzzynextmenuitem": [3],
"mecanismo": [[6,8]],
"literai": [11],
"seguida": [11],
"read.m": [11],
"portuguê": [[4,5]],
"default": [3],
"readme.bak": [6],
"limpar": [11],
"identificada": [8],
"seguido": [2,3,[6,11]],
"memória": [6,11,5,10,9,8,2],
"popular": [11],
"timestamp": [11],
"lançado": [11],
"art": [4],
"projectaccessrootmenuitem": [3],
"ficheiro-alvo": [11,6],
"transfira": [5,0],
"efectuar": [8],
"ness": [[9,10,11]],
"dyandex.api.key": [5],
"limitada": [11],
"colando": [11],
"nest": [5,6,11,9,[8,10]],
"rtl": [6],
"lançada": [8,11],
"espera": [5],
"jdk": [5],
"plugin": [11],
"certeza": [11],
"reproduzir": [6],
"refaz": [[3,8,9]],
"utiliza-o": [5],
"duplo": [5,[9,11]],
"precisem": [6],
"limitado": [11],
"algo": [5],
"estilo": [6,11],
"contida": [[1,10]],
"toolsshowstatisticsmatchesperfilemenuitem": [3],
"dá-lhe": [11],
"nível": [6,11,8,10],
"editinsertsourcemenuitem": [3],
"inspirada": [11],
"documento": [6,11,8,[3,9],[1,5,7,10]],
"run": [11,5],
"viterbi": [11],
"microsoft": [11,[5,6],9],
"projectnewmenuitem": [3],
"documentação": [3,[2,11]],
"optionstranstipsenablemenuitem": [3],
"palavras-chav": [11],
"segment": [9],
"importou": [9],
"usá-las-á": [6],
"lematização": [[1,9,11],3],
"titlecasemenuitem": [3],
"reiniciado": [3],
"ignorando": [9,8],
"arrastar": [[5,9],7],
"transferida": [11],
"glossari": [1,10,6,[9,11]],
"editcreateglossaryentrymenuitem": [3],
"grego": [2],
"ignored_words.txt": [10],
"assistida": [7],
"apó": [11,5,[1,6],2],
"configuration.properti": [5],
"github.com": [6],
"mostrará": [11],
"holandê": [6],
"propriedad": [11,[6,8],4,[0,1,3,10]],
"transferido": [5],
"glossary": [7],
"name": [11],
"avisado": [11],
"feita": [11,[1,6,8]],
"recurso": [6,[5,11]],
"string": [5],
"encerrar": [11],
"intenção": [11],
"distinguir": [10],
"classes": [7],
"fizer": [11],
"poderosa": [11],
"devem": [11,6,5,3,4,[1,2]],
"prioridad": [11,8],
"assunto": [[6,10],11],
"not": [11],
"nos": [11,8,[5,6],[3,9],4],
"digitalizada": [6],
"poderoso": [2],
"produtividad": [11],
"perceb": [4],
"índice": [[7,11]],
"tenham": [11,[1,6,8]],
"traduzir": [11,6,10,[5,9],[7,8]],
"reutilizada": [6],
"castelhano": [4],
"nosso": [6],
"selection.txt": [11,8],
"quando": [11,6,[5,8,9],10,1],
"target": [[8,10,11],7],
"xhtml": [11],
"finder.xml": [11],
"posição": [11,8,9,[1,6]],
"contido": [5],
"window": [5,[0,2,8]],
"config-dir": [5],
"envia": [8],
"disable-project-lock": [5],
"omegat.pref": [11],
"termbas": [1],
"manter": [[5,10,11]],
"envio": [[6,11]],
"evidentement": [[6,9]],
"caso": [6,11,5,9,[8,10],2],
"terminar": [[6,11]],
"até": [2,[5,6,11],[4,9]],
"sugestõ": [[9,11],[3,4,8,10]],
"faz": [6,[4,5,10]],
"analisado": [6],
"primeira": [11,8,1,[5,6,9]],
"largado": [9],
"ambivalent": [11],
"item": [3,5,[8,11]],
"operadores": [7],
"obter": [11,5,[0,6]],
"iten": [[3,11],[1,2,6]],
"atribui": [5],
"apoiando": [11],
"utiliza": [11,[1,4,5,6]],
"processará": [5],
"previament": [8],
"pt_pt.dic": [4],
"targettext": [11],
"vantagem": [5],
"resolvido": [1],
"bastant": [11],
"comportamento": [5,[3,8,9,10,11]],
"level1": [6],
"level2": [6],
"automaticament": [11,5,8,6,[3,4],[1,9]],
"tabelas": [7],
"widget": [9],
"determinar": [4],
"certifique-s": [5,4],
"comportam-s": [11],
"aaabbb": [2],
"electrónico": [9],
"definição": [3,11,4],
"web": [5,[6,7,10]],
"edittagpaintermenuitem": [3],
"en-us_de_project": [6],
"relacionada": [[6,9,11]],
"utilizador": [5,11,8,[6,7,9],3,[1,2]],
"relacionado": [11],
"fechar": [11,[3,8]],
"optionscolorsselectionmenuitem": [3],
"editselectfuzzy4menuitem": [3],
"editregisteridenticalmenuitem": [3],
"more": [9],
"unicod": [2],
"viewmarknbspcheckboxmenuitem": [3],
"tentativa": [[1,11]],
"activar": [11,8,[3,4,5]],
"possibilidad": [[5,6,11]],
"usar": [11,[3,5,6],9,[4,7],[0,8]],
"cópia": [6,[4,8,10,11]],
"usam": [3],
"habitu": [[5,6,8]],
"omitirá": [11],
"carácter": [2,11,[1,7]],
"estado": [9,[6,8],[10,11],[5,7]],
"unabridg": [0],
"num": [11,6,5,[1,8],[9,10],[3,4]],
"revelar-s": [6],
"fornecendo": [11],
"xx-yyi": [11],
"dica": [11,[3,4,6,9]],
"causando": [8],
"direita": [6,11,[5,7,8]],
"holandesa": [6],
"às": [[5,11],[2,8,9,10]],
"utilizam": [11],
"raiz": [6,11,[3,5,8]],
"optionsglossaryexactmatchcheckboxmenuitem": [3],
"msgstr": [11],
"saída": [11,6,3,8],
"fez": [11,9],
"atributo": [11],
"são": [11,6,[1,5],8,10,9,0,[2,3,4]],
"separar": [11,6],
"gere": [11],
"tratada": [11],
"realinhar": [11],
"chamada": [10,[5,8,11]],
"utilizar": [5,[6,11],4,[1,8],10,[3,7]],
"escreva": [5],
"árvore": [10],
"important": [6,[5,9,10]],
"chamado": [5,11,4],
"deslocar": [11],
"nnnn": [9,5],
"transfer": [6],
"project_save.tmx.yearmmddhhnn.bak": [6],
"tratado": [11,6,9],
"omegat.project": [6,5,10,[7,9,11]],
"prever": [8],
"implementaçõ": [5],
"preenchida": [6],
"renomeie-a": [6],
"sublinhar": [1],
"licença": [[0,5,6,8]],
"targetcountrycod": [11],
"procedimento": [6,[4,11]],
"direito": [9,11,5,8,[1,4]],
"especificou": [[4,11]],
"webstart": [5],
"alto": [9],
"resid": [[5,6]],
"detectado": [8],
"pré-requisito": [5],
"acabou": [8],
"viagem": [6],
"zh_cn.tmx": [6],
"copiada": [[6,11]],
"saiba": [9],
"outros": [7],
"múltiplas": [7],
"sai": [8,10],
"scripts": [7],
"mantida": [10],
"ecrã": [5],
"selectiva": [6],
"autenticado": [11],
"primeiro": [11,5,[6,8,10],[2,3,4,9]],
"inserida": [11,8,10],
"move": [8],
"símbolo": [2],
"maneira": [[5,6],[4,11]],
"técnico": [8],
"usado": [11,6],
"copiado": [9,11,[6,8]],
"delimitaçõ": [8],
"darão": [11],
"usem": [6],
"recarregado": [6,1],
"mapear": [6],
"esteja": [11,[5,8],6],
"adicionando": [10],
"precisar": [5,6,[2,4,11]],
"inserido": [11,[8,9]],
"vice-versa": [11,6],
"yandex": [5],
"incluirão": [6],
"alta": [11,10],
"técnica": [11],
"processo": [11,6],
"noção": [9],
"archiv": [5],
"procurar": [11,8,[2,3],[4,7]],
"consultar": [6],
"user": [9],
"precisam": [[2,10,11]],
"a123456789b123456789c123456789d12345678": [5],
"viewmarkwhitespacecheckboxmenuitem": [3],
"equivalent": [[0,5,8,9,11]],
"proxi": [5,11,3],
"extraído": [11],
"pt.dic": [4],
"perda": [6,7],
"propagação": [11],
"extens": [11],
"escapada": [5],
"silenciosa": [5],
"ligaçõ": [[0,11]],
"correspondam": [1],
"separaçõ": [8],
"remove-o": [11],
"alvo": [11,6,9,8,[1,4,10]],
"fin": [11],
"fim": [11,[2,6]],
"bak": [6,10],
"projecto": [6,11,8,10,5,3,[1,9],7,4,0],
"êxito": [11],
"diferença": [11,6],
"veja": [5,[2,6,9,11]],
"permitem": [[5,8,9]],
"bat": [5],
"indicará": [6],
"camada": [6],
"jre": [5],
"optionsfontselectionmenuitem": [3],
"aquel": [6],
"botõ": [11],
"francê": [11,5],
"minimizado": [9],
"desenvolv": [2],
"analisa": [11,2],
"sugestão": [11],
"posterior": [10,[6,11]],
"diff": [11],
"an": [2],
"editmultiplealtern": [3],
"ao": [11,6,5,2,3,8,9,10,4,1,0],
"as": [11,6,8,5,10,9,1,[2,4],3,[0,7]],
"naturalment": [[4,9,10]],
"git.code.sf.net": [5],
"abreviado": [11],
"terceira": [1],
"sentem": [11],
"be": [11],
"múltipla": [9,5],
"freebsd": [2],
"importar": [11,6,10],
"inicialment": [10,[5,6,11]],
"filters.xml": [6,[10,11]],
"múltiplo": [11],
"delet": [11],
"terceiro": [[1,9]],
"anterior": [8,[3,6,11],9,[0,1,2,4,5,10]],
"br": [11,5],
"projectaccessglossarymenuitem": [3],
"modificação": [11,3,8,5],
"vê-lo": [10],
"coluna": [11,1,8,9],
"sei": [5],
"necessita": [5],
"restaurado": [10],
"subsequent": [5],
"sem": [11,5,[6,8],9],
"segmentation.conf": [6,[5,10,11]],
"explicação": [5],
"funcionará": [4,5],
"iniciará": [5],
"ca": [5],
"palavra": [11,8,[2,9],[4,5],[1,6,10]],
"developerwork": [5],
"ser": [11,6,5,8,3,[1,10],4,2,[0,9]],
"cd": [5,6],
"aberta": [11,8],
"seu": [5,11,6,4,9,[1,3,10]],
"ce": [5],
"perca": [9],
"öäüqwß": [11],
"set": [[5,6]],
"associar": [8],
"categoria": [2],
"apareçam": [11],
"cn": [5],
"validada": [9],
"familiar": [11],
"optionsrestoreguimenuitem": [3],
"repetiçõ": [11,8,9],
"cx": [2],
"familiarizado": [6],
"incorporar": [6],
"escapado": [5],
"terminologia": [11,[1,6,8,9]],
"processamento": [11,[3,6,8]],
"validado": [8],
"igual": [[8,11]],
"possam": [5,6],
"silencioso": [5],
"apach": [4,[6,11]],
"da": [11,5,6,8,9,10,3,4,7,[1,2]],
"adjustedscor": [11],
"font": [11,8,6,[5,9],3,[1,10]],
"dd": [6],
"de": [11,6,5,8,9,[2,3],10,4,1,7,0],
"selecçõ": [8],
"dicas": [7],
"executá-lo": [11],
"terminolog": [8],
"duplicado": [11],
"offic": [11],
"dígito": [[5,6]],
"torna-s": [6],
"iguai": [5],
"separada": [[1,9,11]],
"fora": [6],
"chave-valor": [11],
"f0": [1],
"do": [11,5,6,8,9,1,3,[4,10],7,[0,2]],
"f1": [3],
"bem": [6,5],
"começado": [6],
"f2": [9,[5,11]],
"f3": [[3,8]],
"tradutora": [6],
"dr": [11],
"f5": [3],
"justificação": [6],
"encontrar": [5,1,[3,6]],
"incorporado": [[4,11]],
"obrigatória": [11],
"aberto": [6,[8,9,11],[1,5]],
"repositories": [7],
"dz": [0],
"permitir": [11,[6,8]],
"projectsavemenuitem": [3],
"editundomenuitem": [3],
"raro": [11],
"especifiquem": [11],
"separado": [1,11,6],
"xmx6g": [5],
"encontram": [0],
"desmarcando": [11],
"intermediário": [6],
"u000a": [2],
"selecciona": [8],
"práticos": [7],
"acredita": [6],
"importe-a": [8],
"em": [11,5,6,8,10,9,3,2,1,[0,4],7],
"segurança": [11,5],
"esperada": [[1,7]],
"en": [5],
"ícone": [5,8],
"configurado": [11],
"u000d": [2],
"u000c": [2],
"eu": [8],
"destaqu": [9],
"carro": [2],
"ordenado": [[10,11]],
"reflectir": [5],
"operaçõ": [[6,9,11]],
"exportação": [6,11],
"activ": [11,10],
"esperado": [[6,11]],
"localização": [5,[6,11],[1,8]],
"lentament": [5],
"cursor": [8,[9,11],1],
"foi": [6,8,11,[1,5,9]],
"u001b": [2],
"sentir": [5],
"stats.txt": [10],
"origin": [11,6,9],
"foo": [11],
"for": [11,5,6,10,8,[0,3,4,9]],
"exclud": [6],
"confirma": [8],
"fr": [5,[4,11]],
"content": [5],
"arquive-a": [6],
"metad": [11],
"exclui": [6],
"sim": [5],
"applescript": [5],
"client": [6,10,[5,9,11]],
"gb": [5],
"class": [11,2],
"pequena": [[4,11]],
"helplogmenuitem": [3],
"rato": [[9,11],5,8,1],
"secção": [5],
"mostrando": [11],
"necessário": [5,6,11,[0,2,4]],
"descrição": [11,[3,5,6]],
"resultado": [11,8,[2,6,10]],
"repetida": [11],
"editoverwritetranslationmenuitem": [3],
"outputfilenam": [5],
"falso": [11],
"manterá": [10],
"aeiou": [2],
"pequeno": [8],
"necessária": [[0,11]],
"claro": [10,[4,5]],
"gerais": [7],
"form": [5],
"falta": [8,3,[2,9]],
"mistura": [6],
"reagirá": [9],
"equipa": [6,[8,11],7,3,[5,10]],
"correspondent": [11,9,8,[1,2,4,5,10]],
"fort": [11],
"desloca": [11],
"dá": [[5,8],[9,11]],
"nenhuma": [[1,5,8,10]],
"diferem": [5],
"hh": [6],
"painéis": [7],
"armazenam": [6],
"duser.languag": [5],
"poupando": [11],
"completo": [11,[5,6,9]],
"armazenar": [[4,9,11]],
"bis": [2],
"dependendo": [9,11,[5,8]],
"configuraçõ": [5,[3,10,11]],
"marcação": [8],
"exiba": [11],
"file-target-encod": [11],
"projectopenmenuitem": [3],
"autom": [5],
"coincidem": [[1,9]],
"algarismo": [2,6],
"verd": [9,8],
"decim": [11],
"apêndic": [[1,2,4],[0,3],6],
"https": [6,5,[9,11]],
"id": [11,6],
"chama-s": [[1,5]],
"prefixo": [11],
"if": [11],
"project_stats.txt": [11],
"ocr": [[6,11]],
"projectaccesscurrenttargetdocumentmenuitem": [3],
"toolsvalidatetagsmenuitem": [3],
"edição": [9,11,8,[1,3,6,10]],
"in": [11],
"termin": [5,11],
"ip": [5],
"encontrem": [6],
"ir": [[3,7,9,11],8],
"povoamento": [8],
"is": [2],
"fidedigna": [11],
"autor": [8,[9,11]],
"idioma-font": [6,[10,11]],
"odf": [6,11],
"consequentement": [11,[4,5]],
"odg": [6],
"ja": [5],
"configuração": [5,11,[4,8],7,3],
"clicando": [11,[8,9],[4,5,6]],
"detalhada": [[5,11]],
"inclusão": [9],
"odt": [6,11],
"gotonexttranslatedmenuitem": [3],
"viewmarktranslatedsegmentscheckboxmenuitem": [3],
"vermelho": [11,10],
"sítio": [[10,11]],
"valu": [11,5],
"amba": [5],
"precauçõ": [6],
"decidido": [11],
"nplural": [11],
"origem": [6,11,[9,10],5],
"js": [11],
"ilia": [5],
"frent": [8],
"gerada": [10],
"learned_words.txt": [10],
"fá-lo": [11],
"sido": [8,11,[1,5,6]],
"segmentos-font": [11,8,3],
"largu": [5],
"ambo": [6,11],
"contador": [9],
"figuras": [7],
"simplesment": [4],
"robusta": [6],
"macos": [7],
"ftl": [5],
"gerado": [[8,10,11]],
"ftp": [11],
"editselectfuzzy1menuitem": [3],
"viewdisplaymodificationinfoallradiobuttonmenuitem": [3],
"draw": [6],
"completa": [[3,5,6,11]],
"fechado": [6],
"hide": [11],
"faltam": [[8,9]],
"forçado": [10],
"há": [5,[4,6,11]],
"gráfico": [[5,11]],
"fortement": [6],
"idioma-alvo": [4,11,6,5],
"dswing.aatext": [5],
"utilize-o": [5],
"agora": [[3,6,9,11]],
"auto": [10,[6,8],11],
"teclado": [9,[3,11]],
"disposição": [11,9],
"siga": [5,[4,6]],
"copiará": [11],
"sob": [[5,6],[0,8,11]],
"meta-caracter": [2],
"lu": [2],
"document.xx.docx": [11],
"correspondem": [11],
"convidar": [6],
"largá-lo": [9],
"som": [2],
"cycleswitchcasemenuitem": [3],
"adequada": [[5,10]],
"exibição": [11,6],
"dada": [11,[0,5,6]],
"dele": [5],
"mb": [5],
"oracl": [5,3,11],
"dela": [5,[0,9,11]],
"limit": [2],
"me": [6],
"sessão": [11,[3,10]],
"omegat.png": [5],
"purament": [6],
"gradlew": [5],
"mm": [6],
"entri": [11],
"trabalha": [6],
"mt": [10],
"verá": [5],
"ficheiros": [7],
"decisõ": [10],
"my": [6,5],
"identificação": [11],
"adequado": [4,5,6],
"relevant": [6,11,8,[3,5]],
"na": [11,5,8,6,9,1,10,3,4,[0,2]],
"relutantes": [7],
"trabalho": [5,9,[6,10,11]],
"já": [11,5,6,[4,8,9,10],[1,3]],
"editar": [11,8,[3,7,9],[1,5,6]],
"vamo": [6],
"marcada": [11,8,4],
"nl": [6],
"navegador": [[5,8]],
"mensagen": [[5,9]],
"nn": [6],
"no": [11,6,5,8,9,4,3,1,10,2,0],
"percorr": [[8,9]],
"bom": [11],
"utilitário": [[0,5]],
"code": [5],
"desmarcar": [11],
"deixar": [11],
"marcado": [8,[10,11]],
"predominant": [11],
"colecção": [[2,9]],
"tinha": [11],
"parâmetro": [6,5,11,10],
"mensagem": [5,6],
"gotohistoryforwardmenuitem": [3],
"switch": [11],
"criará": [5],
"total": [[9,11],6],
"escrev": [[6,9,11]],
"olhar": [11],
"referem-s": [9],
"conterá": [10,11],
"abre-o": [11],
"of": [0],
"totai": [8],
"bundl": [[5,11]],
"tenta": [11,5],
"altament": [11],
"abre-s": [9],
"reserva": [[5,6]],
"macro": [11],
"ficam": [5],
"corresponderá": [2],
"or": [3],
"sra": [11],
"os": [11,6,8,5,9,1,4,10,3,0,2,7],
"opcion": [8,1],
"consola": [5],
"src": [6],
"ou": [11,6,5,2,[8,9],[1,4],0,[3,10]],
"dado": [[6,11],5],
"chave": [11,5],
"aplicam-s": [8],
"control": [3],
"aplicação": [6,5,4,8],
"rígido": [[5,6,8]],
"no-team": [[5,6]],
"dito": [6],
"pendent": [11,4],
"editinserttranslationmenuitem": [3],
"regulares": [7],
"pc": [5],
"instalar": [5,4,[0,7],8],
"óptico": [6],
"lá": [11,5],
"pdfs": [6],
"sistemas": [7],
"complexo": [2],
"sobrescrev": [10],
"possa": [6,[10,11]],
"propósito": [11],
"po": [11,9,5],
"optionsglossarystemmingcheckboxmenuitem": [3],
"pt": [4,5],
"inclus": [2],
"alguma": [6,[4,5]],
"dados": [7],
"específico": [11,[6,10],[4,5]],
"apresentada": [5],
"inclui": [5,[6,11],[2,9]],
"herdado": [6],
"nº": [9],
"aumentar": [11],
"iniciada": [2],
"poderem": [5],
"environ": [5],
"bra": [5],
"optionsautocompleteglossarymenuitem": [3],
"apresentado": [11],
"devido": [10],
"específica": [11,[8,10],[2,5,6,9]],
"aplicar": [11,8],
"intraduzívei": [11],
"correr": [11,[5,8]],
"ciano": [8],
"pós-processamento": [11],
"removendo": [[5,9,11]],
"terem": [11],
"recent": [8,[3,5,6,10]],
"colocar": [6],
"razõ": [11],
"exemplo": [11,6,5,9,2,4,[0,8,10],[1,3]],
"edit": [[4,5,8]],
"indicada": [[8,10]],
"citação": [2,7],
"editselectfuzzy5menuitem": [3],
"bilingu": [[6,11]],
"srª": [11],
"memórias": [7],
"veze": [11,2,6,[5,8,10]],
"kde": [5],
"inútei": [11],
"modificou": [3],
"passado": [[5,6]],
"rc": [5],
"inclua": [5],
"includ": [6],
"partilhar": [6],
"estão": [5,[9,11],[3,8],6,[0,1,2,10]],
"iniciado": [5,11],
"conversão": [6],
"redimensionamento": [11],
"codificaçõ": [11],
"instância": [5,[8,11]],
"precedida": [2],
"sua": [5,11,6,9,8,1,[0,2,3,4]],
"arrastado": [9],
"motor": [[8,11]],
"ligeirament": [9],
"fornecida": [[8,11]],
"ferramentas": [7],
"ávido": [2],
"activado": [8],
"languag": [5],
"sincronizada": [6],
"sc": [2],
"fornecido": [11,6],
"alterador": [11],
"se": [11,8,5,6,10,9,4,0,3,2,1],
"executável": [5],
"lançá-lo": [5],
"respeita": [11],
"si": [11,2],
"voltar": [9,11,8],
"auxiliar": [10,6],
"sincronizado": [6,11],
"so": [1],
"porta": [5],
"key": [5,11],
"sr": [11],
"lançador": [5],
"seleccionando-a": [11],
"ond": [11,[5,8],6,3,[4,9]],
"svg": [5],
"opcionalment": [5],
"norma": [6],
"svn": [6,10],
"editoverwritesourcemenuitem": [3],
"permissõ": [5],
"confirm": [5],
"retorno": [[2,9]],
"devolvido": [5],
"suficient": [[5,11]],
"enforc": [10],
"objectivo": [[4,5]],
"fluxo": [9],
"reuniu": [10],
"remov": [11,[4,5],[3,6,8]],
"problemas": [7],
"escreverá": [5],
"tm": [10,6,8,[5,7,9,11]],
"visualização": [6],
"to": [5,11],
"secçõ": [6],
"v2": [5],
"meta-etiqueta": [11],
"editreplaceinprojectmenuitem": [3],
"mesma": [11,5,6,9,[2,8,10]],
"enviando": [6],
"tu": [10],
"document.xx": [11],
"tw": [5],
"importação": [6],
"substitua": [[9,11]],
"secretária": [9],
"lugar": [10],
"lida": [[5,6]],
"contextu": [[9,11],[1,3,8]],
"lógico": [[2,11]],
"express": [11],
"viewmarkautopopulatedcheckboxmenuitem": [3],
"dest": [[6,10]],
"lide": [6],
"seleccion": [11,8,5,4,[1,6,9]],
"contexto": [[9,11],[6,8]],
"zero": [11,2,6],
"projectwikiimportmenuitem": [3],
"countri": [5],
"mesmo": [11,6,[5,8],2,[0,1,4,9]],
"ui": [6],
"um": [11,6,5,8,2,1,9,[4,10],3,7,0],
"variant": [[2,11]],
"prático": [6,[0,9,10]],
"up": [6],
"suportada": [11,2],
"triviai": [6],
"digamo": [6],
"gotoprevioussegmentmenuitem": [3],
"melhoria": [8],
"parec": [[5,8,11]],
"this": [2],
"facilitar": [6],
"gotopreviousnotemenuitem": [3],
"ocorrência": [11,[4,9]],
"adicionou": [10],
"editredomenuitem": [3],
"suportado": [[5,6,8]],
"uilayout.xml": [10],
"inici": [[5,6]],
"achar": [2],
"acelerar": [6],
"restabelec": [8],
"vi": [5],
"traduzida": [11,[6,8],9],
"ligação": [[4,6]],
"considerar": [6,11],
"interag": [11],
"orientado": [11],
"substitui": [8],
"desd": [11,10],
"operador": [2],
"traduzido": [11,[6,8],9,10,3,5,4],
"vs": [9,11],
"máxima": [9],
"alternando": [6],
"etapa": [9],
"referida": [11,5],
"partilham": [6],
"remoção": [[6,11]],
"privada": [[5,11]],
"padrão": [11,[2,4,8],[1,5,6,9]],
"dano": [11],
"sobrepor": [9],
"avançar": [8,3,11],
"engano": [11],
"identificar": [11],
"comparada": [11],
"groovy.codehaus.org": [11],
"cuidado": [[6,11]],
"referido": [[1,6,11]],
"repo_for_omegat_team_project": [6],
"lidar": [11,6],
"deve": [11,6,5,3,[1,4]],
"convertê-lo": [6],
"backspac": [11],
"só": [11,8,6,[0,1,2]],
"forem": [11,8,[4,10]],
"normal": [11,[1,5,10]],
"ortografia": [4,3],
"contenham": [[9,10,11]],
"executar": [5,7,8],
"significativo": [11],
"emac": [5],
"org": [6],
"inteiro": [11],
"figura": [4,[0,2,9]],
"rever": [10],
"assim": [11,[5,6,8],[1,3,9,10]],
"prática": [5],
"aqui": [11,6,[5,9,10],8],
"distribut": [5],
"funcionai": [11],
"finalment": [5],
"xf": [5],
"novament": [[6,8,9,11]],
"superior": [11,9,[2,5,10]],
"funcionar": [[4,5]],
"segu": [5,[0,3,6]],
"funcionam": [[6,11]],
"inteira": [11],
"lido": [[1,10]],
"tudo": [[3,5,6,9,11]],
"máximo": [[2,3]],
"xx": [5,11],
"xy": [2],
"runtim": [5],
"seleccione-a": [6],
"suport": [6],
"sourc": [6,[10,11],[5,8],9],
"potent": [11],
"extrair": [0],
"diferent": [11,6,9,8,[4,5,10]],
"apagu": [9],
"type": [6],
"aligner": [7],
"termo": [1,11,9,3],
"toolssinglevalidatetagsmenuitem": [3],
"quisermo": [11],
"guardada": [6],
"filenam": [11],
"parcialment": [9],
"actua": [10],
"projectaccesssourcemenuitem": [3],
"cont": [5],
"modifiqu": [[3,9,11]],
"encerrado": [6],
"certa": [11,[5,6]],
"como": [11,6,5,9,8,0,[4,10],2,3,7],
"emitida": [5],
"yy": [[9,11]],
"nbsp": [11],
"apropriado": [5,[6,11]],
"gotosegmentmenuitem": [3],
"sincronização": [11],
"interno": [11],
"nome": [11,5,[6,9],[4,10],[0,1,8]],
"eventualment": [4],
"resolução": [6],
"enviado": [11],
"guardado": [11],
"abri-lo-á": [11],
"push": [6],
"zh": [6],
"exist": [1],
"clicando-o": [11],
"readme_tr.txt": [6],
"penalti": [10],
"seleccione-o": [5],
"interna": [9,[8,11]],
"avaliador": [11],
"xx_yy.tmx": [6],
"seja": [11,6,[8,9,10],[0,4,5]],
"usando": [11,6,[4,5],[8,9,10]],
"multi-plataforma": [5],
"renomei": [6],
"parecida": [9],
"longa": [11],
"tenha": [11,6,[5,8],1],
"busca": [11],
"utf8": [1,[8,11]],
"execução": [5],
"helpaboutmenuitem": [3],
"meuprojecto": [6],
"excepto": [2,[6,11]],
"copi": [6,[5,8]],
"tanto": [6,11,[5,9]],
"repartida": [11],
"lembra-s": [8],
"out": [6],
"guias": [7],
"limitar": [11,5],
"permite-lh": [11,8,9],
"ponto": [11,2,6,[5,8,9]],
"oferec": [11],
"longo": [11],
"representado": [11],
"precisa": [6,[1,10,11]],
"dark": [11],
"notificação": [11],
"funcion": [11],
"power": [11],
"sugerida": [8,9],
"transferiu": [5],
"transferir": [5,[0,3,8],[6,7,11]],
"regular": [11,2,[5,6],[3,4]],
"houver": [8,9,[5,6]],
"tanta": [11],
"ajuda": [[3,7],[6,8]],
"aparecem": [11,3],
"foram": [[8,11],9,10],
"desistir": [8],
"sobrescrita": [5],
"tag-valid": [5],
"confortável": [5],
"pegando": [11],
"redefinir": [11],
"método": [[5,11]],
"certo": [11,6],
"especifica-s": [5],
"versõ": [[5,6],4],
"esqueça": [6],
"token": [11],
"filter": [11],
"cabeçalho": [11,8],
"elemento": [[6,11]],
"informaçõ": [5,11,10,[0,2,8]],
"u0009": [2],
"xhh": [2],
"ajustar": [11],
"revis": [0],
"u0007": [2],
"fornecedor": [11],
"repositori": [6,10],
"localment": [8,6],
"opções": [7],
"abordam": [11],
"data": [11],
"proporciona": [11],
"excluirpasta": [6],
"lowercasemenuitem": [3],
"wiki": [[0,9]],
"firefox": [[4,11]],
"parcial": [11],
"comentários": [7],
"utilizando": [11,[0,8,9]],
"corresponda": [11],
"saltado": [11],
"garantia": [8],
"garantir": [11],
"tabela": [2,3,11,9,[1,6,8]],
"tab": [[1,3],[8,11],9],
"taa": [11,8],
"permitam": [11],
"ligada": [10],
"depoi": [11,5,[1,3,4,6,9]],
"automática": [11,8,3,9,6,[1,7]],
"tiver": [6,[8,11],[4,5],[9,10]],
"encorajado": [6],
"tai": [6,11,[0,5,10]],
"comprimido": [10],
"tal": [[6,11],10,[4,9]],
"instalado": [5,4,[6,8,11]],
"rodapé": [11],
"ligado": [9],
"variedad": [[4,10,11]],
"slovenian": [9],
"administrador": [11],
"tar": [5],
"largura": [11],
"filtrar": [11],
"onli": [11],
"automático": [11,[3,8]],
"individuai": [11],
"sent": [6],
"evitada": [11],
"desorganizem": [11],
"projectreloadmenuitem": [3],
"aproximada": [11,8,9,6,10],
"core": [8,11,3],
"detrá": [11],
"activada": [[9,11]],
"anexado": [11],
"instalada": [5,8],
"safe": [11],
"openoffic": [4,11],
"especificador": [2],
"evitado": [11],
"opção": [11,8,5,[6,9,10]],
"haja": [5],
"estarão": [[8,11]],
"note": [[5,6],[0,10,11]],
"apena": [11,6,5,8,4,[1,9]],
"sair": [[3,6,8]],
"vogai": [2],
"optionsautocompletechartablemenuitem": [3],
"entanto": [6,[4,11]],
"muito": [11,6,[2,4,10]],
"agressiva": [8],
"winrar": [0],
"tbx": [1,11,3],
"controlado": [[6,11]],
"git": [6,[5,10]],
"indicaçõ": [6],
"programação": [11],
"exportar": [[6,11],[3,8,10]],
"ávidos": [7],
"estruturai": [11],
"cat": [10],
"abaixo": [5,[6,11],[2,9],[3,4,8]],
"muita": [[6,11]],
"continuar": [11],
"duser.countri": [5],
"nota": [11,8,9,6,[2,3,10]],
"tck": [11],
"readm": [[5,11]],
"criada": [11,[6,9]],
"acessível": [11,[3,6]],
"arrastando": [[5,9]],
"direcção": [6],
"acessívei": [8],
"retirado": [3],
"quaisquer": [5,[0,1,6,11]],
"canto": [9],
"verificação": [4,11,2,[1,6,7]],
"optionsspellcheckmenuitem": [3],
"testar": [[2,6]],
"criado": [6,[8,10],[1,5],11],
"frase": [11,[2,3,6,8]],
"align.tmx": [5],
"transferência": [8],
"file2": [6],
"tendo": [11],
"optionssetupfilefiltersmenuitem": [3],
"argumento": [5]
};
