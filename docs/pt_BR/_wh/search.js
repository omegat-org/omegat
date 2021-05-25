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
 "Appendix E. Plugin LanguageTool",
 "Appendix F. Scripts",
 "Appendix C. Projetos de equipe no OmegaT",
 "Appendix D. Tokenizers",
 "Appendix J. Agradecimentos",
 "Appendix B. Atalhos de teclado no editor",
 "Appendix A. Línguas - lista de códigos ISO 639",
 "Appendix I. Avisos legais",
 "Appendix H. Configuração de atalhos",
 "Appendix G. OmegaT na web",
 "Usando TaaS no OmegaT",
 "Sobre o OmegaT - introdução",
 "Dicionários",
 "Filtros de Arquivos",
 "OmegaT Arquivos e pastas",
 "Arquivos para traduzir",
 "Trabalhar com texto formatado",
 "Glossários",
 "Como instalar e usar o OmegaT",
 "Aprenda a usar o OmegaT em 5 minutos!",
 "Tradução Automática",
 "Menu principal e atalhos de teclado",
 "Diversos assuntos",
 "Trabalhar com texto simples",
 "Propriedades do projeto",
 "Expressões regulares",
 "Buscar e substituir",
 "Buscas",
 "Segmentação do texto fonte",
 "Verificador ortográfico",
 "Modo de trabalho",
 "Memórias de tradução",
 "A interface do usuário",
 "OmegaT 3.1 - Manual do Usuário",
 "Index"
];
wh.search_wordMap= {
"coerent": [28],
"hall": [7],
"correçõ": [31],
"atualizando": [34],
"origem-destino": [12],
"exibir": [[8,21],[32,34],[18,27,33],[10,13,15,26]],
"instalador": [14],
"compartilhando": [34,[2,31],33],
"tel": [6],
"cheia": [31],
"tem": [18,15,16,[17,21,23],[1,2,7,14,22,28,29,31]],
"interativa": [25],
"desimpedida": [2],
"ter": [2,[18,27],[8,13,14,16,17,28,31,32],[12,15,23,24,29]],
"processador": [15],
"antonio": [7],
"tex": [15],
"conseguirá": [31],
"letzeburgesch": [6],
"fuzzi": [6],
"inicidado": [21],
"kua": [6],
"aragonê": [6],
"precisament": [13],
"mostrar": [21,[8,17,18,27]],
"guarda": [[2,11,18,22]],
"instruído": [22],
"guardar": [2,11],
"impresso": [[22,32]],
"kur": [6],
"disponibiliza": [18],
"convertendo": [20],
"maldívio": [6],
"dgoogle.api.key": [18],
"formulário": [[18,25]],
"ces": [6],
"edittagnextmissedmenuitem": [8],
"européia": [34],
"gikuyu": [6],
"tgl": [6],
"violeta": [21],
"tgk": [6],
"modificar": [24,[13,18],[15,32],21,[8,14,16,28,31]],
"quiet": [22,18],
"comunidad": [[2,31]],
"sami": [6],
"permitirão": [28],
"leia": [9,[13,14,16]],
"área": [21,18,[1,5,10,24,27,32]],
"usando-s": [24],
"acréscimo": [28],
"es_es.d": [29],
"porém": [19,[16,23,24,25,28,29]],
"bambara": [6],
"tha": [6],
"salv": [15,[8,17,18,23]],
"the": [23,21,31,25,18],
"ossétio": [6],
"download.htm": [18],
"aceitar": [[8,21]],
"taas_api_key_not_found": [10],
"projectimportmenuitem": [8],
"vbdx": [8],
"resumo": [11,33],
"imag": [18,14],
"estariam": [4],
"acrescent": [18,[2,8]],
"reduz": [15,[3,16,32]],
"predefinida": [28,21],
"eliminá-la": [19],
"incia": [18],
"corretor": [29],
"currsegment.getsrctext": [1],
"corrigir": [[11,16,20,27,31,32]],
"tir": [6],
"assistência": [19],
"cha": [6],
"export": [[15,17]],
"che": [6],
"casará": [25],
"aplica": [18,31],
"transtip": [21,8,32,17],
"tradutor": [31,32,15,[2,9,13,27,28]],
"checo": [23],
"chv": [6],
"chu": [6],
"xxxx9xxxx9xxxxxxxx9xxx99xxxxx9xx9xxxxxxxxxx": [18],
"projeto": [31,21,34,2,[14,32],24,19,[18,22,33],13,17,8,[11,29],[9,27,28],[1,3],[12,15],[0,5,10,20,30]],
"plataforma": [[14,18],20,[11,12]],
"inusitada": [23],
"estabelec": [2],
"predefinido": [[14,25,31,33]],
"fr-fr": [29],
"padrõ": [13,28,[16,24,32]],
"motivo": [[14,29]],
"ndonga": [6],
"madlon-kay": [7],
"disco": [[2,18,21,31]],
"restriçõ": [[12,30]],
"conexão": [[11,29]],
"xmxzzm": [18],
"webster": [12,34,[32,33]],
"chega": [30],
"corrija": [[16,32]],
"indicarem": [32],
"porçõ": [9],
"construção": [25,22],
"referenciada": [28],
"validar": [32],
"duplicar": [16],
"permit": [21,[10,13],[17,18],[1,30,31,32],[16,20,22,24]],
"serviço": [20,10,[18,21],2,[11,14,32,33]],
"minha": [[4,18,20]],
"empti": [31,[18,30]],
"redimensionar": [32],
"traduçõ": [31,32,11,21,[13,24,30,34],[28,33],[2,25,27]],
"irrelevant": [22],
"fiquem": [24],
"n.n_source.zip": [18],
"bloco": [25,28,33],
"lepo": [3],
"spolski": [9],
"lepa": [3],
"ordem": [[16,28],[31,32],[4,27]],
"block": [22],
"tms": [31,32,27],
"tmx": [31,34,18,[21,32],[14,22],[11,27]],
"nl-en": [31],
"expandir": [28],
"apertar": [21],
"colaboração": [2],
"integ": [13],
"coisa": [[9,14,19],33],
"intel": [18,[33,34]],
"fr-ca": [28],
"mainmenushortcuts.properti": [8],
"gradualment": [31],
"posiçõ": [[15,17]],
"srta": [28],
"cmd": [[5,21],15],
"romanch": [6],
"coach": [25],
"pós-fixada": [3],
"project_name-level1": [[14,31]],
"gotohistorybackmenuitem": [8],
"project_name-level2": [[14,31]],
"exportada": [[30,31]],
"estévez": [7],
"project-save.tmx": [31],
"compactar": [13],
"ton": [6],
"usarão": [13],
"básico": [18],
"exportado": [21,[17,30]],
"powerpc": [18],
"criativo": [11],
"avail": [23],
"quas": [17],
"embora": [[20,27]],
"tarefa": [[2,15]],
"criativa": [28],
"armênio": [6],
"pseudotradução": [34],
"quai": [[13,31],[0,11,19,28]],
"qual": [18,[11,13,19,22,24,31]],
"interessado": [19],
"com": [18,21,32,31,14,[15,34],13,16,11,17,22,2,[24,30],23,33,[8,20,25],[1,12],[9,27,29],[19,28],[4,5],[7,10]],
"instal": [18],
"cirílico": [23],
"cos": [6],
"cor": [15,[31,32],[6,30]],
"anex": [9],
"próprio": [31,[2,25,30],[16,18,19,32]],
"exibem": [32],
"chamar": [[14,19]],
"pipe": [24],
"lao": [6],
"própria": [[5,10,13,18,25,28,32]],
"telugu": [6],
"pressionou": [17],
"separador": [32],
"lat": [6],
"lav": [6],
"básica": [16,18],
"negrito": [32,16,[10,15,17,27]],
"esquema": [13],
"pior": [[16,20]],
"cpu": [32],
"revert": [22],
"translat": [20,34,[18,33],13,11,[1,21,29]],
"eram": [31,32],
"plenament": [31],
"aviso": [18,[7,34],[4,8,11,22,30,32,33]],
"acompanhar": [[9,16]],
"finalidad": [[17,22]],
"acompanham": [32],
"búlgaro": [6],
"respons": [20],
"tsn": [6],
"interesar": [20],
"ocupariam": [2],
"tso": [6],
"leva": [21],
"ordem2": [31],
"custo": [32],
"cre": [6],
"ordem1": [31],
"chmod": [14],
"lerá": [23],
"chihiro": [7],
"gnome": [18],
"alterada": [[18,24]],
"inválido": [[18,31]],
"quer": [[11,21]],
"convertido": [[13,15,24]],
"inválida": [18],
"crescent": [20,16],
"andamento": [32],
"deverá": [[18,28]],
"significar": [32],
"possibilita": [31],
"quem": [20],
"ttx": [31,34],
"bilíngu": [31,[11,20,34]],
"confidencialidad": [2],
"preencher": [[30,31]],
"alterado": [[17,21],[8,24,31]],
"appdata": [14],
"arquiv": [31],
"prev": [[0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30,31,32,34]],
"csv": [15,17],
"n.n_linux.tar.bz2": [18],
"tuk": [6],
"não-visível": [13],
"tur": [6],
"seguir": [[17,18,25,31,32]],
"tuv": [31],
"lep": [3],
"sotho": [6],
"lepš": [3],
"ler": [[13,15]],
"apresentação": [13],
"assegura": [18],
"prest": [16,[18,22]],
"dock": [18],
"ctr": [16],
"quantificador": [25,33],
"permitindo": [[5,11]],
"marcaçõ": [[24,25,33]],
"vazia": [[2,14],[13,21,30],[8,28,31,34]],
"próxima": [8,[21,32],[5,18,31,34]],
"dmicrosoft.api.client_secret": [18],
"desconectá-lo": [32],
"fisicament": [29],
"vazio": [[28,31,32],[2,13,17,18,21,30]],
"filenameon": [32],
"danificada": [16],
"ctrl": [21,8,5,32,34,19,[15,30],[16,31],[14,17,20,27],[1,12,24,26]],
"mykhalchuk": [7],
"twi": [6],
"document": [[13,14,15,16,18,22,31]],
"produzir": [[28,31]],
"desenho": [13],
"construiu": [32],
"privacidad": [18],
"construir": [20],
"caixa": [34,13,21,24,19,28,[17,22,27,29,33],[25,26,30,32],[2,5,10,16,18,23,31]],
"superfici": [20],
"resourc": [18],
"briac": [7],
"traduzindo": [32,[22,34]],
"removida": [24,[16,31]],
"xx_yy": [[13,31]],
"docx": [[13,15],24],
"txt": [23,17,15,13],
"restaurá-lo": [14],
"prestar": [16],
"adequação": [7],
"removido": [16],
"lituano": [6],
"definir": [[21,28],[13,22],[17,24,25,30,32]],
"lib": [14],
"trás": [[1,21]],
"gravada": [[22,31],13],
"ojibwa": [6],
"lin": [6],
"lim": [6],
"russo": [18,[6,10,22,23]],
"trnsl": [18],
"porção": [21],
"seguem": [25],
"segmentos-alvo": [31],
"lit": [6],
"viewdisplaymodificationinfoselectedradiobuttonmenuitem": [8],
"entregar": [31],
"omegat.tmx": [[14,31]],
"index.html": [[18,32]],
"gerenciada": [31],
"destacado": [[21,30]],
"consequência": [24],
"colocado": [32],
"gerenciado": [2],
"roberto": [31],
"destacada": [32,16,21],
"pago": [20],
"formatado": [34,15,[16,21],11,[19,23,30,31,33]],
"ojibua": [6],
"fecha": [21,26],
"fula": [6],
"colocada": [[17,31]],
"cym": [6],
"pescaria": [3],
"page": [15],
"córso": [6],
"p.ex": [[17,22,24,29],[18,31,32]],
"aspecto": [[11,19]],
"compartilhada": [19],
"chewa": [6],
"comanda": [22],
"montagem": [20],
"tradução": [31,32,20,21,11,34,14,24,[15,22,30],19,[16,33],18,8,[2,13,27,28],17,29,[1,3,10,25]],
"compartilhado": [2],
"projetado": [11],
"estruturado": [17],
"semelhant": [[2,21,32],[11,19,31]],
"computar": [23],
"project.gettranslationinfo": [1],
"salvamento": [[2,21]],
"disso": [[9,17,18,29]],
"comando": [18,34,21,20,[14,22],[2,24],[4,5,11,16,31,33]],
"destino": [13,21,15,34,17,29,16,[10,18,20,24,30],[19,22,26],[0,3,11,27,31,32]],
"traduzível": [13],
"traduzívei": [[13,28,31,32]],
"precis": [25],
"cliqu": [18,13,2,21,32,[26,27],[1,9,17,19,24,28,29],[10,14,16,20,30]],
"projetada": [11],
"irão": [[29,31]],
"start": [18,34,33],
"indonésio": [6],
"smolej": [[7,33]],
"reportar": [31],
"equal": [31,18],
"gravado": [31,[2,23,24,32]],
"short": [23],
"kulik": [7],
"repleta": [11],
"optionsalwaysconfirmquitcheckboxmenuitem": [8],
"tmxs": [21,[8,31,32]],
"possivelment": [31,[3,14,25]],
"macedônio": [6],
"declaração": [13],
"precisarão": [15],
"solução": [34,[20,23,33],31],
"estratégia": [[30,31]],
"incorretament": [32],
"selecioná-la": [30],
"ofici": [33],
"corresponderem": [31],
"enter": [21,5,8,[16,18,30,32]],
"amostra": [22],
"applic": [14],
"visualizado": [15],
"projectteamnewmenuitem": [8],
"distribuir": [[14,31]],
"brasileiro": [29],
"questõ": [[2,11]],
"fácei": [15],
"tecnologia": [18],
"memori": [18],
"submenu": [18],
"visualizada": [18],
"pedindo": [2],
"realment": [[18,21]],
"afar": [6],
"quot": [25],
"combinaçõ": [13],
"godfrey": [7],
"questão": [[2,11,24,28]],
"log": [[9,14]],
"especializado": [32],
"copie-o": [31],
"crioulo": [6],
"devidament": [[17,18,31]],
"comporta": [32],
"consult": [20,[15,19,21],[9,25,31,32]],
"n.n_windows_without_jre.ex": [18],
"deseja": [[29,31],[1,13,14,26,27]],
"sueco": [6],
"falando": [13],
"tutori": [11],
"oferecido": [[2,20]],
"especificando": [18],
"prof": [28],
"planilha": [[13,32],17],
"camproj": [15],
"novembro": [17],
"produto": [[11,15]],
"dmicrosoft.api.client_id": [18],
"especializada": [32],
"funçõ": [32,[11,21],1],
"oferecida": [[2,21,32]],
"clicou": [21],
"identificador": [13],
"config-fil": [[18,22]],
"cancelada": [21],
"erro": [16,9,[18,31],[22,32],0,[11,19,34],[4,14,15,21,24,33]],
"torne-o": [14],
"afetado": [16],
"exibida": [21,[26,27,31],[10,11,13,17,18,22,30,32]],
"experiência": [[16,24]],
"tela": [21,[1,9,18,22,28]],
"dan": [6],
"dar": [32],
"das": [32,34,31,[13,21],16,33,[5,14,25],[2,3,8,15,20,22,23,24,28],[11,17,18,26,27,30]],
"também": [18,32,21,31,17,[2,13],[0,11,15,19,22,29,30],[1,3,8,9,20,26,28,33]],
"navegação": [18,27],
"idêntico": [31,32,27],
"atualizarão": [2],
"imaginar": [4],
"system-user-nam": [13],
"autocomplear": [21],
"format": [[13,15,16]],
"wolof": [6],
"particular": [7,22],
"pausa": [29],
"fácil": [[11,16,19,27]],
"console.println": [1],
"instalá-la": [18],
"exibido": [32,[17,27],[13,21],18,[15,22,24],[23,30]],
"copie-a": [31],
"alertará": [[16,18]],
"microsoft.api.client_id": [20],
"atual": [21,32,30,[17,27],[1,8,14,18,31],[2,5,10,13,15,20,22,24,28,34]],
"atuam": [5],
"croata": [6],
"atuai": [[2,3,14]],
"part": [32,21,13,[14,16,27],[10,11,20,28],[5,9,18,22,26,29,31]],
"decorr": [14,20],
"computador": [18,2,[22,23],[11,13,19,20,21,33]],
"pare": [16,13,20,34,31,[2,22,30]],
"leender": [7],
"para": [18,32,21,31,15,20,28,13,34,19,2,16,22,24,14,[8,11],[27,30],[17,29],[5,9],33,7,[23,25],10,[0,12],[1,3,26],[4,6]],
"principai": [[11,20]],
"hiroshi": [7],
"próximo": [21,8,[19,27,30],[18,26,32]],
"apoiar": [9],
"project_files_show_on_load": [32],
"gostaria": [18],
"consoant": [25],
"idêntica": [21,[8,16,18,30,31,32]],
"gerenciar": [2],
"optionsexttmxmenuitem": [8],
"usá-lo": [20],
"três": [31,[2,12,14,15,21,32],[9,16,17,19,23]],
"ltz": [6],
"tema": [21],
"localizaçõ": [31],
"lub": [6],
"tonga": [6],
"segmentação": [28,24,34,11,[13,14,32],[15,16,21,25,31],33,[8,27]],
"marketplac": [20,18],
"lug": [6],
"sublinhada": [0,29],
"entries.s": [1],
"invisível": [14],
"linguístico": [20],
"del": [[30,32]],
"gotonextuntranslatedmenuitem": [8],
"inseri-lo": [17],
"targetlocal": [13],
"der": [19],
"daí": [14],
"sublinhado": [17],
"path": [18,22],
"deu": [6],
"apagado": [[16,18]],
"dez": [27],
"interferir": [[2,24]],
"teor": [20],
"relativo": [31],
"allsegments.tmx": [18],
"impact": [8],
"especi": [13,[4,5,15,18,22]],
"diferentement": [28],
"mídia": [22],
"protótipo": [1],
"filtragem": [11],
"helpcontentsmenuitem": [8],
"definiçõ": [[22,34],[8,21],[24,33]],
"descrita": [21],
"existem": [[23,31,32]],
"operação": [2,[14,31]],
"atravé": [32,[14,15]],
"visualizador": [[22,31],[17,21,32]],
"uig": [6],
"inserção": [[15,32,34]],
"pára": [30],
"ortográfico": [29,34,33,[0,14,21],[8,11,20,22]],
"outro": [34,18,32,[15,16,22,31],21,[2,11,14,29],[13,30,33],[3,4,5,9,12,17,19,28]],
"term": [17],
"dotx": [15],
"acervo": [20],
"especificaçõ": [21],
"ortográfica": [29,11],
"efeito": [32,[13,28]],
"permanecerá": [26],
"duden": [32],
"miura": [7],
"dia": [31,22],
"spotlight": [18],
"embutido": [[29,32]],
"murray": [[4,7]],
"seguint": [25,13,2,[8,18],14,31,[1,20,28,30,32],[10,11,12,15,21,22,27],[19,24,26]],
"datamarket.azure.com": [20],
"dir": [18],
"latex": [15],
"abrindo": [32],
"div": [13,6],
"contribuindo": [34],
"unificar": [16],
"diz": [[31,32]],
"legai": [[7,34],[4,8,11,33]],
"capitalização": [21],
"viewfilelistmenuitem": [8],
"ukr": [6],
"hierarquia": [14],
"limpando": [30],
"info": [10],
"test": [18,[15,22,31]],
"interferem": [16],
"mittmann": [7],
"omegat": [18,34,[14,31],22,[11,33],32,[2,15,19],9,21,20,13,[8,23],[16,24,30],10,[3,17,29],7,[0,1,4,27,28],[5,12,25,26]],
"imprim": [1],
"xxxxx": [10],
"útei": [18,[11,25],[19,31]],
"kanuri": [6],
"final": [25,[11,19],[4,16,18,20,22,26,27]],
"my-project-dir": [22],
"desmarcada": [28],
"naquela": [14],
"tabulação": [[17,28],25],
"virtual": [1],
"cadeia": [32],
"quebra": [28,[5,13,15,16,34]],
"console-align": [18,22],
"uma": [31,21,32,18,28,[2,27],14,20,25,[16,22,30],11,[13,15,17],[5,24,26,29],[0,1],[8,10,19],[12,23,34],[3,33]],
"encontra": [[13,17,22,25]],
"ms-dos": [18],
"senha": [2,21],
"sensível": [32],
"jean-christoph": [[4,7]],
"terá": [31,32,[14,17,18,19,20,22]],
"verificada": [21],
"talvez": [28],
"restaura": [21,32],
"inser": [21,5],
"henri": [7],
"exibi-la": [32],
"criou": [2],
"invocado": [32],
"und": [29],
"project_save.tmx.temporari": [[22,31]],
"grand": [[13,22,28,29]],
"une": [17],
"acionada": [0],
"kikuyu": [6],
"partir": [18,[14,21,34],[3,33],[2,31,32]],
"quechua": [6],
"interpret": [23],
"editoverwritemachinetranslationmenuitem": [8],
"existam": [24],
"ingreek": [25],
"seguro": [[24,27,31]],
"editando": [30],
"saber": [[2,16,20]],
"f12": [1],
"curta": [2],
"aplicável": [14],
"vontad": [[19,22]],
"es_es.aff": [29],
"vietnamita": [6],
"ignor": [[13,14]],
"convert": [30,23],
"aparecerão": [13,[18,27,29]],
"curto": [11,5],
"pojavnem": [17],
"projectexitmenuitem": [8],
"diálogo": [34,13,21,19,[32,33],[17,24,30],[2,22,29,31],[5,10,23,28]],
"cujo": [[21,29]],
"text": [23,15],
"latim": [6],
"integridad": [14],
"terminado": [32,2],
"imediatament": [[21,31]],
"oferecendo": [32],
"espaço": [13,25,[21,27,28],[8,15,32],[2,5,11,16,17]],
"útil": [31,13,[7,11,24],[3,14,18,19,21,28,30,32]],
"especialment": [[3,22,24]],
"maco": [18],
"perdeu": [22,31],
"doc": [[15,32],14],
"servem": [31,30],
"doi": [18,[2,11,28],[15,16,29,32],[1,14,17,31]],
"status": [32,[18,22]],
"acessá-lo": [[14,32]],
"paramet": [23],
"dos": [13,15,21,32,2,[18,20,27],[8,31],[11,24,29,34],[17,28,30,33],[4,14,16,19,23,26]],
"fech": [32,[22,31]],
"mac": [18,5,8,21,[2,11,14,15,19,33,34]],
"dinâmica": [1],
"painéi": [32,[18,34],[5,15]],
"permanecem": [18],
"mai": [32,18,[25,31],24,[14,19,20],[11,22,27],[13,17,28],[7,21,30],[2,6,8,9,15,23,29,33]],
"mah": [6],
"gaélico": [6],
"lepša": [3],
"dpe": [15,34,33],
"lexic": [20],
"mal": [[6,24]],
"man": [18],
"especifica": [18,[2,13,28]],
"mas": [31,17,5,[18,21],[2,7,16,19,22,25,27,29,32],[1,4,11,14,20,24,28,30]],
"lepši": [3],
"mar": [6],
"contemporâneo": [12],
"substituindo": [13],
"desabilitar": [13],
"anteriorment": [[11,21,31]],
"letão": [6],
"urd": [6],
"shona": [6],
"url": [2,[13,18,21,29,32]],
"especificament": [[18,21]],
"pesquisa": [27,[20,25,32]],
"megabyt": [18],
"uppercasemenuitem": [8],
"viewmarkuntranslatedsegmentscheckboxmenuitem": [8],
"combinação": [34,[5,15],[8,12,18,21]],
"acaba": [2],
"bielorrusso": [6],
"www.omegat.org": [9],
"baix": [18,12],
"válido": [28,[2,18,20,24]],
"usa": [[11,13,20],[3,18,23,28,31]],
"use": [18,32,21,[1,13,15,19],[5,12,24,31,33]],
"usd": [20],
"manfr": [7],
"programador": [21],
"uso": [34,[18,33],[17,22],2,[0,12,21,25],[11,20,24,27,28,29,31,32]],
"recebida": [[2,18,32]],
"omegat.jar": [14,[1,18,22,31]],
"válida": [24],
"usr": [18],
"alter": [2],
"lista": [[13,34],[6,14,28,32],[1,11,19,20,21,29,31,33],[0,5,15,18,22,25,27,30]],
"produzido": [31],
"progresso": [[2,32]],
"desfaz": [21,8],
"clicar": [32,[1,18,21,28],[14,15,29]],
"utf": [17,23],
"numa": [31,[21,29]],
"dizer": [29],
"servic": [18,2],
"produzida": [31],
"descrito": [18,[11,21,27]],
"dsl": [12],
"servir": [[31,32]],
"retrocesso": [5],
"dtaas.user.key": [10],
"certament": [16],
"moldávia": [6],
"lembrada": [19,33],
"dtd": [15],
"curinga": [27,34,[13,24,33]],
"divergência": [20],
"meu": [4],
"exibirá": [21,[11,15,23,27,30]],
"tentar": [[18,24,30]],
"make": [23],
"acima": [32,31,18,27,[12,13,14,15,17,25,29],[0,1,5,16,20,21,23,24]],
"voluntário": [9],
"palavra-chav": [27,11],
"subtração": [25],
"projectcompilemenuitem": [8],
"entretanto": [[15,18,22,24,29,31]],
"console-transl": [[18,22]],
"conseguir": [32],
"dua": [18,14,[2,22,32],[24,28,29,30,31]],
"truncado": [15],
"constantement": [20],
"conform": [14,[5,15,17,18,20,27]],
"masculino": [3],
"caminho": [[18,32],17],
"inseri-la": [32,[19,21]],
"wordart": [13],
"explícita": [22],
"princip": [32,34,21,33,31,[1,8,11,22,28],[13,14,18,24]],
"milhão": [20],
"descrevem": [32],
"expectativa": [7],
"optionsviewoptionsmenuitem": [8],
"depend": [[14,15,20,21]],
"commit": [2],
"targetlocalelcid": [13],
"escond": [[21,32]],
"project_stats_match.txt": [[2,14,32]],
"dvd": [22],
"quarkxpress": [15],
"observem": [15],
"xmx2048m": [18],
"aceita": [31,[8,32]],
"destinada": [20],
"meniju": [17],
"princípio": [[5,28]],
"xxxxxxxxxxxxxxxx.xxxxxxxxxxxxxxxx.xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx": [18],
"rastrear": [30],
"ocident": [[6,23,34]],
"reconhecerá": [[2,11]],
"krunner": [18],
"controlar": [18],
"libreoffic": [29,19],
"objeto": [1,28],
"escolh": [[16,28,29,31]],
"long": [31],
"suaíli": [6],
"defin": [21],
"uzb": [6],
"criação": [34,2,[14,15,28,31],[13,17,24,33]],
"extensivo": [11],
"aceito": [31],
"botão": [18,[2,27,32],1,[17,24,29],[26,28,30]],
"manx": [6],
"texto": [15,21,34,[30,32],13,[28,31],[16,23],11,[20,27],17,5,[8,19,24,29,33],[2,22,25,26],[1,14,18]],
"viewdisplaysegmentsourcecheckboxmenuitem": [8],
"permanec": [[15,24,31]],
"faça": [9,[1,14,20,22,28]],
"open": [13,[15,16,31],[11,20]],
"www.oracle.com": [18],
"linguagem": [1,23],
"linguagen": [1,33],
"mkd": [6],
"project": [[18,22],24,[2,14,21]],
"xmx1024m": [18],
"entend": [31,14],
"único": [32,27,[2,16,25],[11,13,18,24]],
"dzo": [6],
"arquivo": [13,34,31,18,14,15,32,21,19,17,[23,27],24,29,[2,22,33],[11,16],30,8,12,28,1,[0,9,20]],
"incluir": [27,[2,13,14,15,24]],
"acrescentá-lo": [2],
"incluiu": [32],
"estiverem": [[8,13,21,27]],
"gotonextsegmentmenuitem": [8],
"única": [16,25,[11,13,17,20,27,32,34]],
"metódo": [18],
"legislação": [31],
"mlg": [6],
"nnn.nnn.nnn.nnn": [18],
"mlt": [6],
"numerada": [16],
"armazenado": [[1,18,22,31]],
"sugerir": [11],
"abort": [[18,22]],
"armazenada": [18,22],
"internet": [[20,32],[2,14],[11,27,29]],
"convençõ": [31],
"vínculo": [18],
"terminológico": [17],
"serão": [31,[18,21],13,[2,17,22,24,27,28],23,[10,11,14,16,29,30,32]],
"conhecido": [1],
"printf": [16],
"interferirá": [18],
"ocorr": [[18,26]],
"correção": [[4,16,21,29]],
"interest": [20],
"interess": [[11,14]],
"cálculo": [32],
"requisitado": [18],
"escocê": [6],
"conhecida": [[25,31]],
"aparec": [[18,31,32],[2,8,16,20,21,24]],
"externo": [24,[21,32],[2,14]],
"estabelecida": [28],
"verificará": [[16,29]],
"codificação": [13,34,23,17,[11,32,33]],
"considerada": [16],
"externa": [32,21,[8,15,22]],
"es-mx": [29],
"registro": [21,[8,9,10,16]],
"portinhola": [3],
"layout": [[13,32]],
"termo-font": [21],
"multinacion": [31],
"constrangimento": [4],
"marc": [4],
"incluia": [20],
"intercâmbio": [17],
"registra": [14],
"bash": [14],
"dentr": [21],
"considerado": [[11,14,15,27,30]],
"manualment": [[17,29],[15,19,21,24,30,31]],
"base": [17,[1,3,18,21,22,31,32]],
"registr": [9],
"posicionada": [21],
"realizar": [26],
"lote": [18],
"incluem": [[30,31,32]],
"contendo": [27,[17,31],[26,32]],
"adaptá-la": [16],
"mon": [6],
"ossético": [6],
"estabelecido": [13],
"geração": [20],
"essencialment": [29],
"volapük": [6],
"pouco": [[18,29]],
"cortar": [32],
"vai": [5,[19,28,31]],
"indica": [18,[11,16,22,31]],
"internacion": [17],
"pré-definida": [18],
"inserir": [21,30,8,34,5,[13,15,16,25,28]],
"selecionando": [32,[13,14,18,34]],
"abortado": [22],
"iídich": [[6,20]],
"invé": [[17,23]],
"gedit": [17],
"pré-definido": [21],
"nesta": [14,[1,10,22,29,30,31]],
"comparação": [16,32],
"word": [15,[13,19,27]],
"uzbequ": [6],
"lingua": [20],
"retornado": [24],
"luxemburguê": [6],
"essa": [18,[11,14,17,31]],
"cingalê": [6],
"receb": [20,[16,18,30]],
"servidor": [2,18],
"europa": [23],
"sendo": [[11,18,21,31,32]],
"mri": [6],
"vcs": [2],
"chamorro": [6],
"lingvo": [12],
"zhuang": [6],
"estimativa": [32],
"dificuldad": [4],
"exista": [15],
"movido": [21],
"entrada": [17,27,21,33,[5,15,31,32],[2,10,13,18,20,30,34]],
"teste.html": [18],
"tortoisegit": [2],
"reter": [[15,16]],
"canará": [6],
"msa": [6],
"primeirament": [[2,18]],
"incluída": [[13,31],10],
"alimentado": [18],
"n.n_sourc": [18],
"alinhar": [[15,22]],
"constituída": [31],
"incluído": [[0,2,3,14,27,31]],
"resumida": [[2,12]],
"pt_pt.aff": [29],
"tomar": [22],
"baixado": [[14,18]],
"influência": [15],
"baixada": [[10,17]],
"html": [18,13,15,28,[14,16,19,22,31,32]],
"ven": [6],
"estilístico": [11],
"vem": [18,[0,13]],
"pacot": [18,14,[2,15,22]],
"conseguindo": [22],
"ver": [31,[19,21],[14,15,18,28],[5,9,13,16,17,20,30,32]],
"extensão": [[13,17],23,[12,15],[14,31]],
"achará": [13],
"omegat.bat": [14],
"vez": [2,31,[8,17,18,19,21,22,24,25,28,32],[9,15,29,30]],
"colá-la": [32],
"inestimável": [4],
"manuseio": [[11,32,34]],
"artund": [29],
"suporta": [31,[2,11,16,17]],
"captura": [9],
"outra": [34,18,32,21,31,[14,16,33],[0,11,13,19,22,25,27]],
"flexionada": [[3,17]],
"recomenda-s": [21],
"esta": [[21,30],[16,18],[13,14],24,[15,31,32],[11,20,27,28],[5,19]],
"diligência": [4],
"pachto": [6],
"sequenci": [16],
"àquela": [[28,32]],
"contenha": [19],
"velocidad": [20],
"visível": [24],
"aquela": [31,[13,16,18,25,28]],
"acerca": [11],
"sawuła": [7],
"jres": [18],
"www.ibm.com": [18],
"conversõ": [15],
"examinar": [11],
"retradução": [31],
"urdu": [6],
"recoloca": [32],
"grava": [21],
"financeirament": [9],
"poder": [13,20],
"existent": [31,[14,18,21,24,28],[10,17,30],[1,19,20,32,33,34]],
"command": [[8,32],[18,19]],
"mudado": [24],
"n.n_without_jr": [18],
"penalidade-xxx": [31,34],
"podem": [[15,31],11,32,18,14,[2,21],[17,24],[22,28],[13,16],[8,10,19,25,27,30]],
"consegu": [21],
"existir": [18,17,[1,2,31,32]],
"perigosa": [28],
"personalizar": [21,13,[1,16]],
"documento.xx.docx": [13],
"retir": [15],
"protegida": [[7,19]],
"viewmarkbidicheckboxmenuitem": [8],
"notar": [4],
"branco": [13,[21,25,30],[8,15,17,27,28]],
"preferir": [7,[14,24]],
"idioma": [31,18,34,29,13,[10,24,32],[11,22],[17,21,23,28,33]],
"revisado": [12],
"compatívei": [[11,13,15]],
"dividida": [32],
"assamê": [6],
"toda": [31,13,27,[16,32],[24,26],[1,3,4,5,8,10,14,15,20,21,23,29]],
"inicializar": [18],
"via": [32,9,[2,17,21,22,30]],
"compatível": [18,15],
"deparar": [23],
"colado": [21],
"vie": [6],
"fileshortpath": [32],
"colar": [21,32,[5,33,34]],
"reutilizar": [31],
"volum": [[10,29]],
"leia.m": [13],
"vir": [16,32],
"viu": [9],
"desligada": [13],
"colabora": [2],
"verificar": [16,[19,21,31],[8,28,32],[2,17,18,23,29,30,33]],
"visto": [27,31],
"version": [[2,18]],
"project-dir": [[18,22]],
"explicam": [11],
"volta": [21,[5,31]],
"está": [31,21,18,32,2,[11,14,15,20],23,[3,5,7,13,16,17,19,22,25,28,29]],
"mya": [6],
"de-fr": [31],
"vista": [[14,15,32]],
"estilizada": [28],
"traduzirá": [18],
"correndo": [27],
"projecteditmenuitem": [8],
"contêm": [15,23,[10,11,13,14,30,31,32]],
"itálico": [13,16],
"britannica": [34],
"configurar": [21,[13,32],[2,11,29]],
"bloqueio": [18],
"conjunto": [[24,28],[11,13,17,27,32]],
"enriqu": [7],
"apagar": [16,[13,17,30]],
"machin": [[20,21]],
"mudança": [31,[15,18,21,24,28]],
"japonê": [28,[6,18,22,31]],
"pressionando": [19,32,30],
"moldava": [6],
"minimiza": [32],
"contém": [14,31,32,18,[11,17,21],[9,12,13,22,33]],
"conhecem": [11],
"superlativo": [3],
"detectar": [0,[4,31]],
"todo": [18,32,31,[4,16,21,27],14,1,[3,8,10,13,15,19,20,22,24,25,28,29,33]],
"iceni": [15],
"suspenso": [[22,24,29]],
"digitada": [24],
"digitado": [[8,18,21]],
"fornec": [[21,32],[11,29]],
"dividido": [32,[24,28]],
"contribuir": [9,14],
"tradicionai": [18],
"preferência": [18,14,[5,8,11,21]],
"aloca": [31],
"estrutura": [[14,28],31],
"similaridad": [30],
"cobrança": [20],
"dsun.java2d.noddraw": [18],
"estrutur": [20],
"ela": [[18,32],[5,16,19,21,31],[2,13,14,20,24,25,30]],
"borda": [[25,33]],
"ele": [[14,32],15,[13,18],[27,29,31],[0,2,11,21,24,30],[1,8,10,12,16,17,19,22,25,33]],
"segmentado": [[13,28],[31,32]],
"decimai": [30],
"ell": [6],
"turco": [6],
"altera": [21],
"x0b": [25],
"livro": [20],
"funcionalidad": [[2,21,28,29]],
"http": [18,20,1],
"detalh": [32,[7,14,21],[4,9,13,15,18,24]],
"coloque-o": [[17,31]],
"ignorada": [[5,16,32]],
"interfer": [16],
"luganda": [6],
"significa": [[5,15,28,31]],
"basicament": [[15,23]],
"selecionou": [29],
"indispensável": [4],
"vol": [6],
"softwar": [7,[2,22,31]],
"projectsinglecompilemenuitem": [8],
"mexicano": [29],
"insira": [2,[17,21,24]],
"end": [5],
"ignorado": [13,[18,24],[8,14,31]],
"de-en": [31],
"docbook": [[4,15,16]],
"formatação": [16,31,15,34,[19,24],32,[11,28,33]],
"helton": [4],
"não-guloso": [[25,33]],
"abrir": [27,32,18,16,[8,21,24],29,[2,13,14,19,26,34]],
"eng": [6],
"adicionar": [[18,32],[1,13,14,17],[2,19,21,24,28,31,34]],
"iniciar": [18,22,13,[0,3,16,31,32,34]],
"aproximadament": [32],
"iniciai": [19],
"destacar": [32],
"adicionai": [14,[1,11,13,22,27,32]],
"iniciam": [18],
"okapi": [34],
"estiv": [18,[11,22],[8,13,14,21,30,32],[0,2,10,19,28,31]],
"eslavônico": [6],
"minúscula": [25,[8,21,27]],
"contribuem": [4],
"copyright": [7],
"moran": [7],
"coreano": [[6,21]],
"project_nam": [[14,31]],
"system-os-nam": [13],
"açõ": [21,[18,19]],
"optionstabadvancecheckboxmenuitem": [8],
"familiarizar": [22],
"epd": [15,34],
"optionsviewoptionsmenuloginitem": [8],
"pseudo-traduzida": [31,[33,34]],
"nas": [28,27,[21,31],[4,7],[8,11,13,15,16,17,20,22,24,30]],
"similar": [[11,32],[15,24,27,30]],
"nav": [6],
"alerta": [[22,25]],
"nau": [6],
"tar.bz2": [12],
"epo": [6],
"modificando-s": [32],
"restaurar": [[13,32],[8,21,22,34]],
"comentário": [32,[13,17],34,27,[8,18,31,33]],
"excluída": [16],
"canadá": [18],
"x64": [18],
"nbl": [6],
"comercialização": [7],
"ajudar": [9,[11,31]],
"exata": [27,[21,32],17,8],
"garant": [2],
"toni": [7],
"nele": [[2,11,18,19,32]],
"keyev": [8],
"baixando": [18,34],
"pré-selecionada": [2],
"login": [[8,9,20,21]],
"isn\'t": [25],
"esvaziado": [[21,26]],
"interfac": [18,22,32,[11,21,34],[2,9,15,31,33]],
"era": [32],
"confirmação": [[26,31]],
"nela": [2],
"optionsteammenuitem": [8],
"linha": [18,25,[14,20],22,[8,28,31,34],[5,13,24],[4,9,11,16,32,33]],
"decidir": [[2,21,24,30,31]],
"gzip": [31],
"verifiqu": [20,18,[2,12],[15,29],[6,14,22,31]],
"estará": [[2,31],14],
"nde": [6],
"começar": [[8,19,20]],
"esc": [32,[5,21]],
"ester": [29],
"x86": [18],
"ndo": [6],
"exampl": [[18,23,31]],
"nostemscor": [32],
"ess": [18,22,[15,28,31],[13,14,17,23,32]],
"datado": [32],
"est": [18,21,14,11,[31,32],[2,13,20,29,30],[24,25],[4,5,6,17,22,27,28,33]],
"grupo": [16,34,9,[14,33],32,[11,19,22,25,31]],
"preferem": [31],
"acessando": [10,33],
"topo": [[25,28]],
"fijiano": [6],
"console-createpseudotranslatetmx": [18],
"ms_client_id": [18],
"etc": [15,[16,31],[24,28],[2,13,17,18,27],[0,3,12,14,25,32]],
"longman": [[12,34]],
"nem": [31,18],
"nep": [6],
"fuzzyflag": [32],
"enumera": [13],
"inicializado": [30],
"new": [[8,23]],
"merriam": [12,[32,33,34]],
"escap": [18,25],
"conterão": [31],
"projectname-omegat.tmx": [31],
"coleçõ": [10,33],
"intervalo": [[15,21,22,25]],
"desordenada": [16],
"eus": [6],
"project_sav": [32],
"respectivo": [2,[14,15]],
"fazer": [[15,29],[2,14,18,22,23,26,30],[20,24]],
"ainda": [31,[2,4,24],[15,16,18,20,21,26,30]],
"máquina": [1],
"forma": [[15,17,31],[3,13,14,16,18,22,32],[21,27,28],[11,24,29]],
"fazem": [14],
"geralment": [16,31,[14,15,18,21,22,24,28,32]],
"pode-s": [[22,31],[0,8,16,24,27]],
"n.n_without_jre.zip": [18],
"defina": [[18,28]],
"respectiva": [13],
"proxy_host": [18],
"congolê": [6],
"igbo": [6],
"ndebel": [6],
"magento": [15],
"vxd": [15],
"toqu": [30],
"conclusão": [[11,28]],
"adjacent": [32],
"haver": [[24,28,31,32]],
"apesar": [[28,32]],
"estar": [18,22,[2,17,20,32]],
"ewe": [6],
"ms_client_secret": [18],
"offlin": [18],
"navegando": [32],
"martin": [7],
"u00a": [27],
"começando": [25],
"shift": [21,8,5,15,[16,19],[17,30],[32,34]],
"principiant": [11],
"wunderlich": [7],
"adicionado": [18,17,[1,14,22]],
"java": [18,14,22,[8,34],1,[16,25,33],[13,15,31]],
"exe": [18],
"eletrônico": [32],
"confiávei": [31],
"configurou": [22],
"eletrônica": [32],
"project_save.tmx": [31,22,[14,15],[2,21,27,30]],
"além": [31,[4,12,15,17,18,20,27,29]],
"dictionari": [[2,14],[12,34]],
"adicionada": [[16,17]],
"confiável": [17],
"neutro": [3],
"agregar": [13],
"desenvolvimento": [[2,9],34],
"modelo": [20,[1,10,21,32]],
"hospedado": [2],
"taitiano": [6],
"marcar": [21,8,30,[13,18]],
"bidirecionai": [21],
"flash": [15],
"prévio": [5],
"appl": [21,19],
"compactada": [34],
"acrescentando": [13],
"modificado": [[8,15],[18,22]],
"sabendo": [26],
"objetivo": [[18,30]],
"default": [23],
"prévia": [[4,11,22]],
"identificada": [21],
"mémoria": [34],
"dividirá": [11],
"pressioanando": [1],
"modificada": [[13,16]],
"timestamp": [13],
"ness": [31],
"dyandex.api.key": [18],
"continuament": [31],
"nld": [6],
"limitada": [5,[13,27]],
"nest": [18,31,[2,11,16,21,30],[3,9,13,15,32]],
"dinamarquê": [6],
"persa": [6],
"plugin": [0,3,34,[1,14,33]],
"compactado": [[14,31]],
"merecem": [2],
"faroê": [6],
"refaz": [21,[8,31,32]],
"omegat-l10n-request": [9],
"limitado": [5,[2,17]],
"duplo": [18,[14,32]],
"eslavo": [6],
"aproveitado": [31],
"absoluto": [31],
"estilo": [15,[1,2,16]],
"customização": [13],
"segmentará": [28],
"editinsertsourcemenuitem": [8],
"documento": [15,21,[13,31],16,[5,24,32],8,[7,9,11,14,19,27,28,30,34],[2,10,17,18,33]],
"representam": [32,16],
"microsoft": [[13,34],[15,20],18,[11,17,19,32,33]],
"projectnewmenuitem": [8],
"documentação": [7,[4,8,34],[11,13,14,25,32,33]],
"optionstranstipsenablemenuitem": [8],
"palavras-chav": [[27,32],11],
"segment": [21],
"changes.txt": [14],
"refletir": [18,16],
"restringir": [27],
"ignorando": [21],
"glossari": [17,[2,14],[19,21,27,34]],
"proxy_port": [18],
"ignored_words.txt": [[2,14]],
"grego": [25,6],
"holandê": [31,[0,6]],
"propriedad": [14,34,21,[13,22],[24,31],[11,17,18,29,32,33],[3,8,12,19,28]],
"nno": [6],
"registrando": [34],
"next": [[0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30,31,32,33]],
"nob": [6],
"string": [[3,32]],
"import": [[15,31]],
"intenção": [28],
"fizer": [[18,27]],
"nom": [8],
"caxemira": [6],
"prioridad": [28,34,[21,33]],
"nor": [6],
"not": [18],
"nos": [27,34,15,[21,31],[2,18],13,[24,32],[0,3,5,8,10,11,12,16,17,20,22,29]],
"central": [[6,23,34]],
"obtendo": [20],
"poderoso": [1],
"ascii": [15],
"produtividad": [28],
"índice": [11,13],
"traduzir": [13,15,11,[19,30],[9,14,18,20,28,31,32,33]],
"tenham": [21,[15,17]],
"castelhano": [[6,29]],
"nosso": [31],
"selection.txt": [30,21],
"tornar-s": [32],
"xhtml": [13,[15,16,28,32]],
"resultar": [16],
"posição": [21,32,28,[1,16,17,27,31]],
"compartilhar": [[2,31]],
"refer": [18],
"window": [18,14,[2,34],21,[11,12,15,17,22,25,33]],
"gramat": [[0,20]],
"traduziu": [[21,32]],
"decida": [24],
"envia": [30],
"disable-project-lock": [18],
"omegat.pref": [[14,32]],
"oomegat": [34],
"inupiaq": [6],
"fao": [6],
"txml": [15],
"terminar": [31],
"fas": [6],
"inesperada": [14],
"sugestõ": [21,[5,20,29,30,31,32]],
"faz": [[22,31],[15,16,18,25,29]],
"analisado": [31],
"contextuai": [5],
"ambivalent": [13],
"ativado": [21,[3,13,20]],
"bartko": [7],
"analisada": [32],
"processará": [[13,18]],
"utiliza": [[17,29]],
"quirguiz": [6],
"começou": [32],
"pt_pt.dic": [29],
"vantagem": [18],
"bastant": [[2,15,16,17,20,32]],
"italiano": [6],
"usuario": [21],
"pré-tradução": [[31,34]],
"level1": [[19,31],21],
"level2": [[19,31],21],
"repará-lo": [31],
"automaticament": [18,21,[13,17,19,30,32],[0,2,3,10,11,14,15,22,23,24,27,29]],
"pessoa": [2,[4,11]],
"widget": [[32,33,34]],
"determinar": [29],
"recém-adicionado": [17],
"certifique-s": [18,29],
"semelhança": [[31,32]],
"aproveita": [1],
"definição": [8,[2,29,32,33]],
"web": [18,[9,34],33,[1,8,11]],
"avéstico": [6],
"frísio": [6],
"confuso": [31],
"berlin": [7],
"editselectfuzzy4menuitem": [8],
"editregisteridenticalmenuitem": [8],
"malaio": [6],
"habilitá-lo": [18],
"tentativa": [17],
"usar": [18,[31,32],[11,20],[2,13,19,21,29],[8,17],[24,27,30,33],[4,7,12,14,15,16,22,28,34]],
"pt_br.dic": [29],
"usam": [13,[3,8,20]],
"omitirá": [13],
"finaliza": [25],
"estado": [21,[14,24,30,31,32]],
"preenchido": [10],
"contabilidad": [32],
"num": [18,[29,31,32]],
"section": [[21,23]],
"dica": [34,[15,16,24,29,33],[18,32]],
"recém-criado": [19],
"agradecimento": [4,[7,11,33,34]],
"raiz": [[3,31],18,[21,32]],
"aplicativo": [18,29,22,[14,17,21,32]],
"acessado": [18],
"saída": [[8,17,21],[13,15]],
"dict": [12],
"fez": [[28,30]],
"exatament": [[13,14,27,29,30]],
"são": [[31,32],18,14,[16,17,21],[15,24],[22,27,30],[2,13,28],[10,11,12,19,20],26,[1,4,8,25,29]],
"regularment": [[22,31]],
"faria": [[2,32]],
"orient": [[23,34]],
"pesca": [3],
"tratada": [[21,27,31]],
"acessada": [32,22],
"rastreador": [4],
"escreva": [13],
"árvore": [14],
"fulah": [6],
"tratado": [32,[15,30]],
"nnnn": [32,18],
"project_save.tmx.yearmmddhhnn.bak": [[22,31]],
"implementaçõ": [18],
"option": [32],
"irlandê": [6],
"processa": [[20,28]],
"juntar": [24],
"aceitável": [[16,20]],
"mantido": [2,[13,17,30,31]],
"vantagen": [2],
"errada": [21],
"myproject": [31],
"zh_cn.tmx": [31],
"copiada": [13],
"chuvash": [6],
"mantida": [[18,24]],
"gama": [29],
"wordfast": [15],
"huriaux": [7],
"wix": [15],
"copiado": [[2,13,21,24]],
"darão": [13],
"omissõ": [4],
"recarregado": [17,[22,31]],
"adicionando": [18],
"mantém": [[2,31]],
"txt2": [23],
"processo": [[21,28],[11,14,15,16,17,22,24,30,31]],
"visio": [15],
"nya": [6],
"txt1": [23],
"operacionai": [21,2],
"archiv": [18],
"procurar": [16,[10,11,19,27,29]],
"user": [14,[18,22]],
"detecção": [16],
"proxi": [18,[21,34],8],
"extraído": [14,[10,28]],
"perda": [22,[14,34],[11,24,33]],
"extens": [13],
"propagação": [24],
"fij": [6],
"correspondam": [[17,27]],
"fin": [13,[6,16,31]],
"fim": [5,16,25,[8,13,14,19,20,28]],
"kinyarwanda": [6],
"b0": [16],
"b1": [16],
"b2": [16],
"veja": [[14,31],[21,32],[24,25],[2,28],[0,7,13,20,27],[8,18,22,29,30]],
"permitem": [32,18],
"fiz": [0],
"marāṭhī": [6],
"leiam": [13,18],
"presumir": [23],
"aa": [6],
"ab": [6],
"alfabeto": [[22,23]],
"casarão": [3],
"desenvolv": [25],
"ae": [6],
"analisa": [[25,28]],
"af": [6],
"sugestão": [20,21],
"observaçõ": [32],
"posterior": [[7,31],[13,16,24]],
"ak": [6],
"diff": [32],
"automat": [23],
"am": [6],
"an": [25,[6,23]],
"editmultiplealtern": [8],
"ao": [[18,21],32,31,25,27,16,[2,30],[13,20],[14,22,24],[5,29],[19,23],[1,10,15,17,28],[4,8,11,34],[0,9,12]],
"ar": [6],
"as": [32,31,21,16,18,13,[11,24],27,[14,15,28],2,20,[17,22],30,[19,29],[1,3,10],[23,25,26,33],[5,6],[0,4,12]],
"abreviado": [2,1],
"av": [6],
"ay": [6],
"wln": [6],
"az": [6],
"ba": [6],
"be": [23,[6,31]],
"tsuana": [6],
"simultaneament": [[2,32]],
"múltipla": [34,32,[17,18,33]],
"importar": [32,31,[2,8]],
"bg": [6],
"bh": [6],
"bi": [6],
"sinônima": [11],
"inicialment": [14,[2,18,20,31]],
"lembre-s": [16,[9,18,19,24]],
"filters.xml": [14,[2,24]],
"bm": [6],
"bn": [6],
"pesquisado": [[14,27]],
"bo": [6],
"anterior": [21,31,8,[11,28,32],[5,13,14,22]],
"instalando": [18,34],
"br": [13,[6,18]],
"modificação": [[8,21],2,[14,17,18]],
"bs": [6],
"search": [21],
"demorada": [32],
"samoano": [6],
"necessita": [11],
"by": [23],
"explicação": [[0,2,18,19,22]],
"segmentation.conf": [[14,22],[18,24]],
"funcionará": [[0,3,18,22,29]],
"iniciará": [18],
"ca": [[6,18,20]],
"cd": [18,22],
"ce": [[6,15]],
"perca": [17],
"öäüqwß": [27],
"fixada": [16],
"ch": [6],
"inglesa": [4],
"galê": [6],
"cn": [18],
"familiar": [27],
"co": [6],
"figur": [32,[14,17,29],[0,12,16,20,25,30,33]],
"cr": [6],
"cs": [6],
"renam": [31],
"cu": [6],
"cv": [6],
"repetiçõ": [32,21,11],
"cx": [25],
"cy": [6],
"terminologia": [10,[11,17,31,32],[21,27,34]],
"utilizando-s": [20],
"apach": [[2,29]],
"da": [32,18,[2,14,21,31,34],20,15,27,[22,28],13,[16,19,29],5,[7,24],[1,3,8,17],[11,23,33],[0,4,6,12,25],30],
"confundido": [1],
"adjustedscor": [32],
"font": [31,21,34,13,32,15,[22,30],19,[24,28],[8,20],[16,27],33,[3,14],[1,5,10,17,18,23,26]],
"estável": [22],
"questionário": [14],
"dd": [[22,31]],
"de": [34,31,32,21,13,18,16,15,11,20,[14,28,33],24,2,[8,22],17,25,29,27,30,19,[9,23],5,[1,10],3,26,0,6,[4,7],12],
"executá-lo": [[1,11]],
"duplicado": [27],
"torna-s": [31],
"fora": [5,[2,16,18]],
"separada": [[21,28,32]],
"do": [[18,34],21,32,31,14,2,13,17,33,20,[15,29],5,[22,28],[8,19],9,24,11,30,16,[1,27],3,4,12,[6,10,23],[0,7,25],26],
"f1": [[21,32],[1,8,34]],
"f2": [[18,24]],
"f3": [21,[8,34]],
"dr": [28],
"f5": [[8,21]],
"encontrar": [18,[9,11,17],[8,14,22,30,32]],
"obrigatória": [13],
"dv": [6],
"uigur": [6],
"wol": [6],
"permitir": [30,[2,13,15,21,22,31]],
"dz": [[6,12]],
"resposta": [[9,20]],
"editundomenuitem": [8],
"raro": [[24,31]],
"separado": [17,13,2,[16,28,31,32]],
"torna-o": [14],
"ee": [6],
"intermediário": [31],
"u000a": [25],
"el": [6],
"em": [18,[31,32],21,13,28,[11,14,19,27],[15,16,22],[17,20],2,[10,23,24,25,30],1,26,8,9,[0,3,4,12,29,33],7],
"visualsvn": [2],
"belazar": [20,34,33],
"en": [[6,18,22,32]],
"eo": [6],
"es": [[6,20]],
"u000d": [25],
"et": [6],
"u000c": [25],
"eu": [[6,28]],
"ex": [31,[0,2,18,30]],
"operaçõ": [[16,31,32,34]],
"duplicada": [16,25],
"mediant": [18],
"fa": [6],
"esperado": [[21,31]],
"subpasta": [14,34,31,[2,19],[11,24],[17,18,22,32],[12,29,30,33]],
"alimentação": [25],
"ff": [6],
"foi": [20,31,[18,22],[16,17],[0,4,11,14,21,24,30]],
"stats.txt": [14],
"u001b": [25],
"fi": [6],
"fj": [6],
"origin": [[16,32],15,13,[2,11,14,20,24,27]],
"for": [18,31,[13,23],22,21,[1,15,16,19],[2,5,8,12,20,24,30,32]],
"fo": [6],
"pensar": [0],
"confirma": [21],
"exclua": [[31,32]],
"fr": [18,22,[6,20,28,29]],
"content": [18,33],
"metad": [28,[17,30]],
"marathi": [6],
"fy": [6],
"ativada": [[13,21,32]],
"exclui": [[16,29]],
"inuktitut": [6],
"applescript": [18],
"baixou": [[14,18]],
"ga": [[6,20]],
"processomento": [21],
"class": [[13,25],33,1],
"gd": [6],
"pequena": [[28,29,32]],
"helplogmenuitem": [8],
"mostrando": [[2,11,32]],
"macosx": [14],
"somali": [6],
"necessário": [18,[15,20],[2,32],31,[14,17],[12,16,21,22,25,27,29]],
"resultado": [27,31,[13,16,17,20,25,30,32,33]],
"gl": [6],
"editoverwritetranslationmenuitem": [8],
"digitar": [[17,19,24,27,30,32]],
"outputfilenam": [18],
"gn": [6],
"i0": [16],
"i2": [16],
"gt": [25],
"aeiou": [25],
"gu": [6],
"gv": [6],
"claro": [24,[4,20,21,23,29,31,32]],
"necessária": [14,[12,16,27,31]],
"virou": [2],
"ha": [6],
"correspondent": [13,32,[22,27,28],[0,1,14,16,18,21,24,25,26,29,31,33]],
"fort": [1],
"he": [6],
"dá": [32,18],
"hh": [[22,31]],
"hi": [6],
"fechada": [21],
"metacaracter": [25],
"duser.languag": [18],
"completo": [31,[5,11,12,13,18,20,21,32]],
"dê": [18],
"ho": [6],
"hr": [6],
"sobreposição": [16,34,33],
"tab-delimit": [17],
"ht": [6],
"hu": [6],
"dependendo": [[18,32],21,[13,16,31]],
"configuraçõ": [18,22,[14,32],[19,21,24,31,34],[3,11,13,29,33]],
"hy": [6],
"hz": [6],
"file-target-encod": [13],
"fra": [6],
"oci": [6],
"coincidem": [32],
"verd": [[2,32],21],
"ia": [6],
"sentença": [28,24,[32,34],[16,25]],
"context": [32],
"briel": [[4,7]],
"apêndic": [[5,11,21,24,32]],
"id": [[6,18,20,32]],
"https": [10],
"portal": [3],
"ie": [6],
"fri": [6],
"prefixo": [30],
"if": [1],
"estoniano": [6],
"associando": [15],
"project_stats.txt": [32,2],
"ig": [6],
"ocr": [24],
"ii": [6],
"ik": [6],
"edição": [21,32,30,34,31,11,[12,27,33]],
"in": [23,1,[21,31]],
"io": [6],
"k1": [19],
"termin": [18],
"ip": [18,34],
"index": [34,4],
"ir": [21,[8,32,33],[19,27,30,34],[5,9]],
"contrato": [18],
"is": [[6,18,25,31]],
"it": [[6,23]],
"iu": [6],
"irpara": [34],
"odf": [15,[13,16,28]],
"consequentement": [[27,29]],
"ja": [[6,18,31]],
"multiterm": [17,34,33],
"jc": [4],
"detalhada": [[14,32]],
"vasta": [29],
"odp": [15],
"odt": [15,24],
"gotonexttranslatedmenuitem": [8],
"librari": [14],
"melhorando": [20],
"precauçõ": [22],
"jp": [23],
"nplural": [13],
"iniciant": [19],
"origem": [[17,31],[18,32],10,[0,11,14,15,20,22,24,27]],
"js": [1],
"jv": [6],
"gerada": [31,[14,20,21]],
"learned_words.txt": [[2,14]],
"direto": [2],
"maxym": [7],
"ka": [6],
"kg": [6],
"temporariament": [32],
"ki": [6],
"kj": [6],
"gerado": [31,14],
"kk": [6],
"kl": [6],
"km": [6],
"uyghur": [6],
"kn": [6],
"ko": [6],
"kr": [6],
"ks": [6],
"multi-palavra": [[32,33]],
"viewdisplaymodificationinfoallradiobuttonmenuitem": [8],
"bem-vindo": [9],
"ku": [6],
"kv": [6],
"kw": [6],
"ky": [6],
"completa": [[8,18,19,27,32]],
"fechado": [[21,31]],
"lembra": [21],
"la": [6],
"lb": [6],
"há": [[17,18],[15,29,30],16,[2,14,19,23]],
"le": [[8,17]],
"desenvolvedor": [9,14],
"idioma-alvo": [31],
"lg": [6],
"ful": [6],
"li": [6],
"dswing.aatext": [18],
"agora": [[2,8,30,32]],
"ln": [6],
"lo": [6],
"disposição": [[2,21]],
"copiará": [19],
"ls": [14],
"lt": [[6,25]],
"lu": [25,6],
"dist": [18],
"lv": [6],
"correspondem": [[2,13,16,17,27,31,32]],
"salva": [15,[2,17,21,24,32]],
"exibição": [15,21,32,27,[2,8,22,33]],
"cycleswitchcasemenuitem": [8],
"that": [23],
"cobr": [14],
"dada": [28,18],
"selecion": [18,13,[19,22,27,29,30,32],[1,2,14,17,21,31]],
"salvo": [24,[14,21,31]],
"mb": [18],
"bidaux": [7],
"tratará": [16],
"limit": [25,[30,33]],
"mg": [6],
"tratamento": [17],
"mh": [6],
"mi": [6],
"sessão": [[14,24,26]],
"redireciona": [14],
"mk": [6],
"ml": [6],
"mm": [[22,31]],
"entri": [1],
"mn": [6],
"trabalha": [[14,19,24]],
"mr": [[6,28]],
"ms": [20,[6,16]],
"mt": [31,[6,20]],
"verá": [[20,31]],
"wxl": [15],
"my": [[6,14]],
"javanê": [6],
"respectivament": [17,31],
"na": [18,21,32,31,27,2,16,[17,22],13,[20,28],14,[1,15,24],[26,30],[8,9,19,29],[23,34],[0,3,7,11,12,25],[4,6,10,33]],
"nb": [[6,27]],
"itera": [32],
"trabalho": [34,18,30,[21,31],32,14,[2,15,16,22,33],[4,8,11,19,23,24,27]],
"nd": [6],
"já": [32,[18,31],24,17,[2,11,28],[1,8,13,14,15,16,22,29]],
"ne": [6],
"editar": [13,[21,34],[30,33],[8,32],[18,19,24,28,31]],
"updat": [2],
"ng": [6],
"marcada": [24,[13,30],[20,21,28,29,31]],
"nl": [31,6],
"nn": [6],
"navegador": [32,34,14,[18,21]],
"no": [18,32,21,31,2,17,13,16,[14,15,22,27],20,11,19,5,[8,33],[1,29,34],[24,30],9,[23,25],0,[3,28],[6,10,12,26]],
"code": [[8,20]],
"desmarcar": [[13,31]],
"nr": [[6,17]],
"marcado": [21,[27,31],[2,10,13]],
"córnico": [6],
"tinha": [28],
"parâmetro": [18,34,20,22,[14,15,31],4],
"colchet": [32],
"nv": [6],
"gotohistoryforwardmenuitem": [8],
"ny": [6],
"húngaro": [6],
"dialog": [[21,23]],
"escrev": [18],
"oc": [[6,20]],
"legado": [31],
"conterá": [14],
"od": [15],
"regravar": [13],
"of": [33,[18,21,23,31]],
"abre-s": [2],
"oj": [6],
"ok": [2,[18,19,31]],
"reserva": [18],
"om": [6],
"sinhala": [6],
"ausent": [21,34,[8,16,32]],
"fraca": [1],
"or": [6],
"corresponderá": [21],
"os": [18,31,32,13,21,2,14,15,17,29,22,27,16,[11,30],24,[8,10,19],[20,23,25,28,34],[1,12],[3,4,5],[6,9],[0,33]],
"opcion": [[20,31]],
"ot": [15],
"ou": [31,32,[18,21],[17,25],14,[13,30],[16,27],[2,28],[15,24],[7,8],[11,22,29],[9,12,19,26],20,[1,3,23],[5,10]],
"dado": [22,20,[10,17],[2,14,30,31,34],[3,9,11,15,18,24,26,32,33]],
"rígido": [[18,21,27,31]],
"encod": [23],
"oji": [6],
"pa": [6],
"editinserttranslationmenuitem": [8],
"unifiqu": [15],
"equip": [2,34,[8,21,31,33],[3,5,11,18,32]],
"instalar": [18,34,29,[2,12],[11,20,33],[19,21,32]],
"complexa": [[11,15]],
"predefinição": [21,[15,25]],
"lá": [0],
"lã": [0],
"pi": [6],
"easier": [23],
"sobrescrev": [[21,31]],
"complexo": [25],
"pl": [6],
"propósito": [34,[7,31]],
"po": [13,32,15,[31,34]],
"ps": [6],
"pt": [[6,18,20]],
"inclus": [14,[13,25]],
"reduzir": [16],
"inclui": [18,[0,2,7,13,25,31,32]],
"desaparec": [29],
"automatizar": [13],
"poderem": [18],
"automatizam": [18],
"devido": [[2,31]],
"corren": [27],
"aplicar": [13,[2,21,27,28]],
"taas_user_key": [18],
"ciano": [21],
"correr": [27],
"terem": [[28,32]],
"aplicam": [16],
"recent": [18,[21,22,31]],
"colocar": [[19,31]],
"pesada": [32],
"qu": [6],
"edit": [20,[18,22,29]],
"confirmará": [21],
"editselectfuzzy5menuitem": [8],
"citação": [[25,33]],
"deliberadament": [16],
"veze": [25,[13,14,19,32],[1,2,15,16,27,31]],
"inclua": [[16,18]],
"iniciado": [18,[24,25]],
"singular": [3],
"rm": [6],
"codificaçõ": [13],
"redimensionamento": [21],
"rn": [6],
"ro": [6],
"duplicou": [16],
"precedida": [25],
"renomea-lo": [29],
"fornecida": [11,[2,10,18,21,27]],
"ru": [6],
"rw": [6],
"optionstranstipsexactmatchmenuitem": [8],
"sa": [6],
"sc": [[6,25]],
"sd": [6],
"fornecido": [[11,32],[9,14,20]],
"óbvia": [23],
"se": [18,21,31,13,32,22,14,20,16,[28,29],[2,19],[17,27,30],[15,24],[0,8,11,12,23],[5,7,9],[1,3,6,25,26]],
"nynorsk": [6],
"sg": [6],
"si": [6],
"autônoma": [25],
"sk": [6],
"voltar": [21,[19,32],[8,13,27]],
"sl": [[2,6]],
"auxiliar": [31,[15,32]],
"samuel": [[4,7]],
"sm": [6],
"sn": [6],
"so": [[6,18]],
"sq": [6],
"sr": [28,6],
"tomando-s": [32],
"ss": [6],
"st": [6],
"lançador": [18],
"su": [6],
"sv": [6],
"ond": [18,32,21,[2,8,14,22],[15,16,19,24,29,30,31]],
"sw": [6],
"pesquisará": [27],
"tâmil": [6],
"ta": [20,[6,11,32]],
"editoverwritesourcemenuitem": [8],
"permissõ": [18],
"cuidar": [2],
"te": [6],
"tg": [6],
"th": [6],
"suficient": [[13,28,31]],
"enforc": [31],
"saṁskṛta": [6],
"ti": [[6,17]],
"remov": [[5,13],27,[21,24,30],[16,29,31]],
"tk": [6],
"tl": [6],
"deveriam": [25],
"tm": [31,[32,34],2,[19,21,27]],
"tn": [6],
"desativa": [21],
"to": [[18,22],23,[6,21,31]],
"selecionar": [8,[13,21],27,[10,29,32],[18,24],[3,5,14,19,22,23,28,30,34]],
"v2": [20,[18,34]],
"tr": [6],
"ts": [6],
"mesma": [[13,16,18,22,30,31],[2,12,14,15,21,23,25,27,32]],
"tt": [6],
"enviando": [9],
"provavelment": [[11,17,23,31]],
"importação": [[2,31,34]],
"tw": [[6,18]],
"solto": [8],
"ty": [6],
"lugar": [[2,31],18],
"lida": [[15,18,31]],
"lide": [17],
"hmxp": [15],
"oriá": [6],
"projectwikiimportmenuitem": [8],
"countri": [18],
"mesmo": [31,[21,27],[2,13,18],[7,17,25,30],[11,12,14,15,16,22,24,26,28,29,32]],
"ug": [6],
"uk": [6],
"yahoo": [[9,14]],
"um": [18,31,2,[14,21],32,16,17,[11,13],[22,25],19,20,[27,28],24,29,30,[9,15,34],5,[7,8],[0,3,4],[1,10,12,23,33],26],
"un": [17],
"esperava": [9],
"prático": [[18,32]],
"up": [21],
"triviai": [31],
"ur": [6],
"melhoria": [[9,21]],
"uz": [6],
"this": [[23,25]],
"ocorrência": [[26,27],29],
"adicionou": [14],
"vc": [2],
"ve": [6],
"acelerar": [31],
"vi": [[6,18]],
"traduzida": [13,[11,20,31,32]],
"siginificado": [27],
"considerar": [[24,31]],
"recomeçar": [28],
"vo": [6],
"traduzido": [13,21,32,16,[19,27,31],15,[8,14,24,30],18,[11,22],26,34,[28,29,33]],
"operador": [[25,33]],
"support": [31],
"vs": [[32,34]],
"albanê": [[6,20]],
"creationd": [32],
"wa": [6],
"microsoft.api.client_secret": [20],
"remoção": [16],
"ávar": [6],
"privada": [10,[18,33]],
"padrão": [13,8,21,31,[28,32],34,[14,17,18],[15,23,24],[2,25,29,30],[1,3,6,11,16,20,22,33]],
"dano": [[17,30]],
"omegat.sourceforge.net": [18],
"engano": [16],
"identificar": [[3,13]],
"groovy.codehaus.org": [1],
"wo": [6],
"lidar": [15,[2,11,20,30]],
"privado": [10,2],
"backspac": [5,30],
"só": [[4,12,23,27,32]],
"forem": [14,[16,27],[13,17,24,29,31]],
"ortografia": [[11,29]],
"executar": [18,32,[1,19]],
"emac": [18],
"rever": [[19,31]],
"ori": [6],
"assim": [18,31,2,[11,13,14,16,17,27,32],22],
"aqui": [21,[2,14,16],[3,11,15,18,22,24]],
"prática": [18],
"funcionai": [1],
"orm": [6],
"identificar-s": [20],
"xf": [18],
"novament": [18,[5,25,31]],
"xh": [6],
"venda": [6],
"superior": [[14,32],28,[9,20,21,26,27,34]],
"funcionar": [29,[14,22,23]],
"razoável": [11],
"segu": [[28,31,32]],
"funcionam": [[5,22]],
"xp": [14],
"lido": [[14,31]],
"máximo": [[8,23]],
"maori": [6],
"plena": [4],
"xx": [18,13],
"suport": [[1,9]],
"xy": [25],
"sourc": [14,[1,18,19,21,23,32,34],[2,8,11,13,20,22,31]],
"esloveno": [20,[3,6,17,32]],
"forneça": [10],
"extrair": [[12,13]],
"apagu": [18],
"oss": [6],
"volker": [7],
"yi": [6],
"termo": [17,[21,32],8,[2,5,10,20,27]],
"toolssinglevalidatetagsmenuitem": [8],
"quisermo": [30],
"manutenção": [2],
"yo": [6],
"yu": [7],
"colaborativa": [2],
"como": [32,[18,31],21,[11,16],20,15,[27,28,34],[2,24],[14,17],13,[1,22],[12,23,30],[19,25,29,33],[0,7,10],[8,9]],
"yy": [13],
"construído": [20],
"interlíngua": [6],
"apropriado": [[18,29],[13,31]],
"za": [6],
"nome": [13,32,31,34,18,[22,27,29],[2,14,17,19,21],[1,6,12,15,24,28,30]],
"otp": [15],
"resolução": [31],
"chichewa": [6],
"vá": [[18,20]],
"refletirá": [14],
"zh": [[6,31]],
"ott": [15],
"exist": [31],
"eslovena": [20],
"catalão": [6],
"hunspel": [3],
"apropriada": [[2,11]],
"zu": [6],
"seja": [31,25,13,[23,26,30,32],[2,15,16,17,18,21,22,27,28,29]],
"a-t-il": [8],
"renomei": [[24,31]],
"zz": [18],
"excluem": [31],
"longa": [32],
"busca": [27,[26,32],34,33,[21,25],11,[13,15,19,22,28]],
"utf8": [17,23,[13,15]],
"ória": [21],
"execução": [20,4],
"armazena": [[11,31]],
"copi": [31,18,22],
"tanto": [27,[11,32],[10,15,18,31]],
"canguru": [11],
"continuem": [4],
"parecido": [18],
"ponto": [[25,28],18,[1,9,11,14,15,21,30,31,32]],
"oferec": [[18,20],[10,24,30]],
"longo": [[23,32]],
"precisa": [31,[17,18],[9,14,20,23]],
"power": [13],
"funcion": [[18,19]],
"respeitando": [30],
"tanta": [32],
"aparecem": [[8,16,17,18,21,22,27,30]],
"tão": [[15,32]],
"manipulada": [15],
"foram": [31,[1,16],[8,14,20,26,32]],
"preciso": [18],
"tag-valid": [22,18],
"confortável": [18],
"casar": [25],
"método": [[18,27],34,[15,33]],
"oportunidad": [14],
"help": [15],
"cabeçalho": [13,32],
"dividindo-s": [32],
"informaçõ": [18,32,15,[16,20,31],[8,21],[11,14,24],[5,6,12,17,22,25,27,30]],
"u0009": [25],
"xhh": [25],
"ajustar": [[28,32]],
"u0007": [25],
"gerará": [31],
"xho": [6],
"retorna": [28],
"corr": [27],
"data": [[13,14,17,27,32]],
"xht": [15],
"lowercasemenuitem": [8],
"firefox": [29,1],
"lists.sourceforge.net": [9],
"utilizando": [[17,31]],
"melhoram": [3],
"tabela": [[8,15,21,28,32],[5,6,13,16]],
"filepath": [32],
"depoi": [18,28,[2,19],5,[8,15,16,20,27,29,30,32]],
"automática": [20,21,34,32,[8,11,24,29,33],[10,16]],
"tiver": [21,14,[18,20,29,31],[2,13,17,22,32]],
"oasi": [15],
"replac": [21],
"corretament": [[15,16,23,31],[0,17,18]],
"instalado": [18,[3,14,29]],
"desativando": [13],
"ligado": [20],
"representada": [16],
"dependerão": [0],
"automático": [[22,34],[21,33]],
"propõ": [11],
"core": [21,[15,16]],
"apresenta": [[13,14,20]],
"mestr": [13],
"instalada": [18,29],
"pré-processá-lo": [31],
"openoffic": [29],
"note": [25,[15,22,24,28,31,32]],
"excluir": [24,[5,16,31]],
"porcentagen": [31],
"optionsautocompletechartablemenuitem": [8],
"noth": [25],
"porcentagem": [32],
"link": [32,[9,16],[13,14]],
"muito": [31,[11,22,28],[2,4,9,14,15,17,23,24,25,29]],
"helari": [[4,7]],
"git": [2,31],
"programação": [1,[16,21]],
"exportar": [21,30,[8,11,17,31,34]],
"estruturai": [28],
"osseta": [6],
"muita": [19,[2,11,16,21,30]],
"abaixo": [18,2,[21,31,32],[5,8,16,20,22,25,27,29]],
"continuar": [19],
"nota": [21,[13,32],8,27,[2,17,19,22]],
"britânica": [12],
"xx-yy": [13],
"will": [4],
"acessívei": [[14,32]],
"virgul": [17],
"apaga": [21],
"quaisquer": [[4,17,25,28,31]],
"follow": [[23,25]],
"verificação": [34,16,32,22,[18,33],[21,29],[11,15,25]],
"nort": [6],
"optionsspellcheckmenuitem": [8],
"considera": [27],
"xlf": [15],
"frase": [27,19,11,[0,24],[13,20,26,28,31]],
"transferência": [21,20,5],
"optionssetupfilefiltersmenuitem": [8],
"abecásio": [6],
"altgraph": [8],
"poderá": [[9,13,14,18,23,24]],
"novo": [19,[18,31],[13,21,34],[8,17],29,[1,2,11,15,20,24,30,32,33]],
"seta": [32,27],
"procurando": [31],
"without": [18],
"alteraçõ": [18,28,[8,17,21,26,32]],
"these": [18],
"moderno": [6],
"basta": [[14,32],[16,18,28],[13,15,30]],
"menor": [[11,25]],
"xml": [15,13,[14,16,17,20]],
"muda": [[18,31]],
"sumário": [32],
"mude": [[22,23],[15,28,29]],
"remoto": [[18,21]],
"persistent": [4],
"gla": [6],
"nova": [[18,21,28],[24,25,34],[13,14,15,16,17,26,29,30,31,32,33]],
"digitá-la": [[2,15]],
"gle": [6],
"pedir": [32],
"glg": [6],
"serv": [17],
"alteração": [22],
"solicitada": [18],
"desviá-la": [16],
"glv": [6],
"junto": [[13,24],18],
"solicitado": [14],
"util": [[18,22]],
"têm": [27,[1,12,13,14,15,19,31,32]],
"atualizar": [18,[31,34],[2,17,22,24]],
"sincronizará": [2],
"maiúscula": [25,21,27,[8,18,28,32]],
"perigo": [31],
"subdiretório": [31],
"tar.bz": [12],
"reproduzida": [16],
"xltx": [15],
"maiúsculo": [25],
"encontrado": [[17,18,32],[4,8,13,21,22,27,31]],
"chinê": [[6,18,31]],
"registrar": [21,[2,8,9]],
"independent": [14,[18,20]],
"seus": [31,[18,23,32],[2,15,19],[0,12,13,17,21,28,30]],
"inalterado": [13,30],
"prazo": [23],
"executando": [34,18,[21,23]],
"desta": [[19,32],31],
"dessa": [[14,15,16,18,22,28]],
"encontrada": [31,[4,10,25,27,32]],
"redobrada": [16],
"prevenir": [30],
"laranja": [21],
"alternar": [15,21,[5,8]],
"compilar": [18],
"xlsx": [[13,15]],
"iniciá-lo": [20],
"histórico": [21],
"rápido": [[11,17,18]],
"aaaa": [[22,31]],
"gnu": [7,14],
"preservada": [16],
"component": [21],
"rápida": [[18,27]],
"desatualizada": [15],
"target.txt": [30],
"bislamá": [6],
"substituir": [[21,26],8,34,32,[13,20,25,27,31,33]],
"corrigido": [[11,17,20,24]],
"acontec": [31],
"ojibw": [6],
"mudou": [2],
"nameon": [13],
"maioria": [23,28,[2,8,13,14,15,21]],
"bretão": [6],
"inglê": [31,25,[0,3,6,12,18,20]],
"pan": [6],
"canadens": [28],
"gotonextnotemenuitem": [8],
"par": [16,31,20,[2,22,28,32]],
"excelent": [27],
"tar.gz": [18],
"gpl": [12],
"list": [33,23],
"especiai": [[27,28]],
"descompact": [18],
"personalizado": [32],
"contrário": [16,[17,20,24,27,32]],
"conflito": [2,[8,31]],
"substituiçõ": [26],
"envolv": [[28,32]],
"será": [18,13,[21,24],16,31,[15,27],[17,19,28],[0,2,20,32],[1,5,8,14,26,29]],
"in-lin": [16],
"personalizada": [[16,21]],
"lisa": [17],
"revisa": [32],
"azur": [20,18],
"azul": [32,[0,16,21,27]],
"formato": [34,15,17,13,11,[16,32],[19,33],[12,20],[8,14,18,23,27]],
"remova": [[0,3,18]],
"rashid": [7],
"ativar": [21,13,20,[1,16,24]],
"livr": [7,[12,20,29,33]],
"esvaziar": [32],
"navegar": [32,[20,27,29]],
"ocultado": [21],
"menus": [18,32,[19,21,24]],
"nauru": [6],
"reinstalar": [14],
"permitida": [30],
"grn": [6],
"nyanja": [6],
"estatística": [32,34,21,[8,14],33,[20,24]],
"endereço": [18,34],
"xtg": [15],
"bindownload.cgi": [18],
"vário": [[13,31],32,[2,9,11,14,21,27,30,33]],
"qualquer": [31,[18,32],[2,14,19,25,27],[17,21],[7,22],[5,8,9,13,28,29]],
"carregando": [2],
"requerido": [17],
"faltant": [5],
"with": [18,[21,23]],
"lado": [[5,18],[2,8,28,29,32]],
"permitido": [2],
"obsoleto": [32],
"pdf": [24],
"implicitament": [[2,13]],
"seção": [[9,16,18,20,23,24]],
"agência": [[11,32]],
"toolsshowstatisticsmatchesmenuitem": [8],
"hexadecim": [25],
"instantâneo": [32],
"viewdisplaymodificationinfononeradiobuttonmenuitem": [8],
"surja": [15],
"sincronizar": [18],
"concisa": [4],
"e-mail": [9,14],
"chamando": [14],
"paí": [18,31,13,34],
"move-s": [5],
"quão": [32],
"carrega": [[19,31,32]],
"insuficient": [20],
"público": [[9,10,14]],
"pesquisar": [[18,27]],
"esperanto": [6],
"gui": [14],
"guj": [6],
"carregu": [[15,18]],
"regexp": [18,22],
"imperativo": [1],
"amárico": [6],
"stemmer": [3,34],
"uhhhh": [25],
"sobretudo": [28],
"você": [31,18,32,21,19,[2,14,29],[13,20],[22,28,30],[11,24],16,[9,23],[15,17],[7,8,12,25],[1,5,27]],
"optionssentsegmenuitem": [8],
"pública": [[7,10],14,33],
"terão": [13],
"adjetivo": [3],
"utilização": [31,[0,18,34]],
"incompatibilidad": [[0,3]],
"dokuwiki": [15],
"inconsistent": [31],
"autorai": [21],
"charact": [25],
"pergunta": [[9,18]],
"perfeitament": [31],
"test.html": [18],
"xxx": [[14,31]],
"exclusõ": [24],
"smalltalk": [1],
"nenhum": [21,[17,32],[3,5,10,13,20,31]],
"instanc": [[23,31]],
"ant.apache.org": [18],
"mínimo": [[21,31]],
"almost": [23],
"sessõ": [18],
"tempo": [[20,27],[11,21,22,28,32]],
"usaram": [24],
"pseudotranslatetmx": [[18,31]],
"reduzida": [31],
"diretament": [18,13,[14,28]],
"verbo": [20],
"arno": [7],
"representa": [18,[16,28]],
"velasco": [7],
"panjabi": [6],
"abri-la": [32],
"targetlanguagecod": [13],
"pilpré": [7],
"nuosu": [6],
"evit": [2],
"abri-lo": [32,11],
"locativo": [17],
"websit": [14],
"causa": [[17,18]],
"carregada": [[18,22,31]],
"artigo": [[4,9]],
"recarrega": [21,[31,32]],
"extra": [18,22,[16,17,25]],
"creationid": [32],
"acostumar": [32],
"hebraico": [6],
"traduz": [22,[13,30,32]],
"sinalizado": [32],
"perdido": [14],
"atualizado": [[17,31],[2,11,13,14,32,34]],
"atualizada": [[2,31],[6,11,15,18]],
"acrescentar": [13,8,[2,9,15,16,20,21,30,32]],
"porqu": [[13,19,31,32]],
"procur": [[12,18]],
"pli": [6],
"encham": [27],
"combinam": [31],
"consecutivo": [[11,16,21]],
"conduct": [21],
"recarregu": [27],
"carregado": [31,[13,17,19,32]],
"simplifica": [15],
"seleção": [24,[21,28,30],13,[3,29],[8,12,16,18,32,33,34]],
"rapidament": [32],
"optionstagvalidationmenuitem": [8],
"questionávei": [27],
"economizando": [30],
"recentement": [20],
"arquivo-font": [23],
"aplicada": [28,[0,16]],
"suficientement": [31],
"agregada": [13],
"código-font": [[18,34],33],
"pt_br": [29,18],
"língua": [20,34,28,15,29,3,[0,6,22,31],[24,33],18,[2,11,14,19,23],[4,5,12,16,17,21,32]],
"pelo": [[21,31],20,[14,32],[2,8,15],[4,11,18,24,25,30],[0,7,13,16,22,27,28]],
"a-z": [25],
"calculada": [32],
"evento": [8],
"kanjii": [23],
"zoltan": [7],
"avançada": [[16,27]],
"resultará": [16],
"aplicado": [[24,28]],
"definitivament": [20],
"onlin": [20],
"pela": [18,20,[2,28],[7,17],[4,14,15,24]],
"tomando": [3],
"rodearão": [19],
"penalizada": [31],
"receita": [31],
"preservar": [13],
"europeus": [10],
"png": [18],
"javascript": [1],
"marqu": [27,[28,30],[2,13,26,29]],
"mediawiki": [[21,32],8],
"komi": [6],
"melhor": [[20,29,31],[2,16,17,22,28,30,32]],
"caracter": [25,5,21,[13,27],[17,18],[16,23,28,33],[8,20,32],[15,24,30]],
"reverterá": [14],
"obterá": [31],
"pessoal": [20],
"join.html": [14],
"must": [31],
"versão": [18,2,31,[7,20],22,[14,15],[0,3,4,9,21,33]],
"pessoai": [11],
"correspondendo": [13],
"compilador": [15],
"pod": [15],
"omegat.kaptn": [10],
"poi": [24,[2,11,15,16,18,22,31,32]],
"modificá-la": [[7,19]],
"necessidad": [[2,21,29,32]],
"pol": [6],
"aplicabilidad": [28],
"pens": [31],
"pop": [21],
"por": [31,13,21,27,[18,28,32],17,[16,24],[2,25],29,11,[15,30],[1,20],[3,8,14,19,23,26],[4,34],[5,6,9,12,22,33]],
"found": [18],
"dispositivo": [27],
"tomado": [24],
"preocupar": [17],
"houverem": [[0,28]],
"incorreta": [16,[0,30]],
"correspondência": [32,21,34,31,8,30,[16,33],25,[3,5,13,14,17,27],[15,19,22,24,28]],
"desejar": [[9,14,24,30,31,32]],
"agrupar": [16],
"project_name-omegat.tmx": [31],
"coloqu": [31,[9,28,29]],
"encontrará": [[18,27],0],
"desejam": [25],
"googl": [20,[18,34],33,[11,27]],
"opendocu": [13],
"implicaçõ": [[2,14]],
"mover-s": [[19,30]],
"modificá-lo": [7],
"envolvido": [[2,29,32]],
"correta": [[17,18,31],20,[3,12,19,29,30,32]],
"converterá": [19],
"ucraniano": [6],
"sourceforg": [9,[8,14,34],[2,33]],
"continua": [22,18],
"goodi": [18],
"cazaqu": [6],
"permitirá": [18],
"depuração": [18],
"coleção": [32,[17,25,34]],
"correto": [18,[2,29,31]],
"hat": [6],
"validador": [[13,18],21],
"has": [23],
"realiza": [26],
"baixar": [34,[12,21],[2,8,14,18,29,33]],
"hau": [6],
"pra": [[26,27]],
"escapa": [25],
"descompactar": [18,12],
"requerem": [21,18],
"determinada": [31,[16,27]],
"editmultipledefault": [8],
"batch": [14],
"desativada": [21],
"editfindinprojectmenuitem": [8],
"faltando": [[4,25,32]],
"combinada": [13],
"finalizar": [26],
"acabarem": [31],
"determinado": [31,[14,22,24],[15,18,20,32]],
"warn": [22,18],
"descompactação": [14],
"abertura": [11],
"technetwork": [18],
"check-out": [2],
"acrescentada": [28],
"desativado": [21],
"reutilização": [31],
"plural": [3,13],
"detecta": [[16,17,18,32]],
"formada": [[16,32]],
"plurai": [17],
"conta": [[18,20],[2,9,32]],
"expressão": [[13,16],25,[11,22,26,27]],
"atribuída": [18],
"gerenciamento": [21,34,33],
"introdução": [33,11,[0,1,2,3,20,34],19],
"expressõ": [25,27,[11,28,34],[18,26,33],[12,32]],
"implementação": [18],
"substituiria": [20],
"exporta": [21,[19,31]],
"mudar": [21,[13,14,16,18,23,32]],
"jacob": [7],
"atribuído": [[8,18]],
"procurará": [[11,19]],
"confirmar": [[2,8,21]],
"país": [31],
"medida": [32],
"avançado": [[11,18,22,25,31]],
"organizar": [2],
"interlingu": [6],
"n.n_windows.ex": [18],
"glossário": [17,34,[21,32],33,10,11,[8,19],[2,14,27],[3,5,12,15,22]],
"tigrínia": [6],
"pop-up": [32,17],
"intercambiável": [17],
"time": [31],
"calculado": [32],
"totalment": [31,16],
"tipo": [[13,15],34,[14,16,28,31,32],[20,23,30]],
"reconhecida": [[17,28]],
"heb": [6],
"brune": [7],
"listar": [11],
"localizada": [22,[18,21,27]],
"consideração": [24],
"qualidad": [20,[15,31],3],
"program": [18],
"pus": [6],
"keith": [7],
"reconhecido": [[11,17]],
"her": [6],
"localizado": [[18,21],32,[27,31]],
"projeto2": [2],
"projeto1": [2],
"vária": [32,[27,31],[11,15],[0,1,4,10,13,14,18,19,21]],
"resultant": [18,22],
"defeituoso": [31],
"razão": [18],
"explicita": [13],
"pré": [3],
"casinha": [3],
"pré-traduzir": [31],
"camtasia": [15],
"separa": [28],
"utilizará": [18],
"n.n_mac.zip": [18],
"distribuída": [7],
"consultada": [10],
"tabl": [21,25,8,32,33,[5,6]],
"acrescentado": [2],
"distribuído": [7],
"norueguê": [6],
"juntá-la": [31],
"esperar": [18],
"consultado": [11],
"descobrir": [32],
"doc-license.txt": [14],
"combinado": [[18,24]],
"mudem": [21],
"eclesiástico": [6],
"yid": [6],
"copyflowgold": [15],
"aprenda": [[11,19],[4,7,18,33]],
"provávei": [11],
"gerar": [19,31,[15,22,24]],
"provável": [[17,18,20,28,32]],
"editor": [27,32,5,15,[13,33,34],[11,17,18,19,21,22,24,30],[0,1,2,6,16,23,26,31]],
"pseudotranslatetyp": [[18,31]],
"órfãos": [31,[32,34]],
"hhc": [15],
"hhk": [15],
"consistent": [21],
"traduza-o": [15],
"testador": [25,33],
"trabalham": [[11,28]],
"trabalhar": [16,34,[23,33],[11,18,22],[2,15,21,30,31]],
"colocando": [8],
"posteriorment": [14],
"envolvida": [31],
"projectclosemenuitem": [8],
"hin": [6],
"viewmarknonuniquesegmentscheckboxmenuitem": [8],
"hio": [7],
"importa": [18,22],
"hit": [31,[24,25,32]],
"rolagem": [28],
"pareça": [15],
"movendo-s": [30],
"aceitação": [20],
"responsável": [2],
"pedido": [9],
"sobr": [32,[11,18,21],[24,31,33],[0,2,5,6,8,14,15,19,20,22,28,30]],
"consider": [[23,31]],
"ofereça": [17],
"fundo": [[21,31],32],
"group": [14],
"ocorrerem": [27],
"geral": [24,7,[10,14,33],25],
"prevenindo": [24],
"reservada": [29],
"gerai": [[5,14]],
"associado": [18,[11,13,15,21]],
"nº5": [32],
"readme.txt": [[7,14]],
"letra": [21,8,[25,31],[15,18,27,34]],
"contagen": [32],
"contagem": [32],
"campo": [32,[13,21],[17,18,30],[2,22,27,29],[11,15]],
"languagetool": [0,[11,33,34],[1,3,21]],
"sequência": [27,34,[15,16],21,17,28],
"source.txt": [30],
"estejam": [[2,3,11,18,21,27,31]],
"files.s": [1],
"página": [21,[5,18,20,32],[8,9,13,14,15,28]],
"siband": [7],
"exchang": [17],
"exclusão": [16,[24,30,34]],
"output-tag-valid": [18],
"tipagem": [1],
"indicando": [2],
"contínuo": [9],
"projectlock": [2],
"request": [20],
"currseg": [1],
"precisarem": [31],
"point": [13],
"explica": [11],
"reservado": [[7,15]],
"l4j.ini": [18],
"demo.taas-project.eu": [10],
"identifica": [13],
"europeia": [[28,31]],
"então": [[24,26,31],[10,29,30],[8,13,14,17,21,22,27,28,32]],
"corresponderão": [24],
"denominado": [18],
"torna": [31],
"vinogradov": [7],
"unido": [2],
"copiá-lo": [[13,16,18]],
"process": [13],
"iniciando": [18,[33,34]],
"membro": [2,[14,31]],
"marcará": [[16,21]],
"alternativa": [32,21,[2,13,30],[24,31],[8,15,23,34]],
"andrzej": [7],
"downloaded_file.tar.gz": [18],
"alternativo": [[31,34],14],
"copiá-la": [32],
"normalment": [18,22,[11,14,27]],
"traduza": [19,33],
"inscreva-s": [9],
"acordo": [[2,11,13],[21,27],[31,32,33]],
"citada": [2],
"account": [10],
"sejam": [17,[13,15],[4,24,29]],
"antigo": [6,21],
"especificam": [13],
"dhttp.proxyhost": [18],
"condiçõ": [7,31],
"diversa": [[11,14,21,22,25,27]],
"japanes": [23],
"desencaixado": [21],
"hmo": [6],
"especificar": [[18,31],[8,14,15]],
"período": [28],
"recortar": [21,[5,33]],
"quiser": [13,[8,20,32],[9,18,19,25,31]],
"diverso": [34,[11,15,22,32],[6,14,18,21,29,33]],
"barra": [32,18,[21,22,25]],
"acinzentada": [19],
"marca": [21,32,30],
"acrescentaremo": [2],
"preço": [20,32],
"ignorar": [13,29],
"dúvida": [[9,14]],
"ignored_word": [34],
"relatório": [9,[14,33,34]],
"yor": [6],
"you": [23],
"responsabilidad": [4],
"preferenci": [21],
"carregam": [31],
"contient": [17],
"comprometida": [14],
"revisão": [31,[0,2]],
"reinici": [8],
"ignoram": [25],
"mantendo": [[2,14]],
"configur": [18],
"encontraria": [27],
"chgt": [8],
"carregar": [[1,13],[2,31]],
"documento.xx": [13],
"buloichik": [7],
"descompactá-lo": [18],
"extenso": [11],
"optionsworkflowmenuitem": [8],
"fica": [31],
"digitando": [18],
"trabalharem": [2],
"releas": [18],
"peter": [7],
"comet": [16],
"cordonni": [7],
"sparc": [18],
"segmentar": [28,[24,31]],
"definido": [[13,14,21,29,31,32]],
"processar": [[13,27]],
"consistem": [28],
"nunca": [13],
"irrestrito": [18],
"sandra": [7],
"antiga": [24,[0,18]],
"procurá-lo": [19],
"vírgula": [17,[13,25]],
"frequentement": [13],
"arrast": [18],
"rtl-ltr": [5],
"georgiano": [6],
"diretório": [[2,14,18,22,31]],
"brasil": [18],
"meio": [24,[1,2,14,20,21,22,25,26,30]],
"pacient": [4],
"prior": [4],
"chinesa": [31],
"visualizar": [[15,30]],
"mostra": [32,21,[12,16,17,18,20]],
"definida": [15,[0,8,11,13,16,20,23,24,28,31,32]],
"passar": [32,0],
"hrv": [6],
"feche-a": [27],
"dhivehi": [6],
"coloca": [31],
"série": [[31,32]],
"confidenciai": [9],
"algun": [[18,21],[13,22,28,31,32],[0,2,12,16,17,20,24,30,33,34]],
"algum": [17,18,[13,14,15,16,20,22,24,29,31,32]],
"file-source-encod": [13],
"distribuição": [18,7,14],
"extensõ": [[12,17,23]],
"some": [23],
"entr": [32,[15,16,17,30,31],[5,13,21,25],[2,9,14,18,22,27]],
"vilei": [7],
"piotr": [7],
"tang": [7],
"validação": [[16,21]],
"preposiçõ": [4],
"alterar": [32,23,[18,22,28]],
"divid": [31],
"editexportselectionmenuitem": [8],
"abrang": [23],
"procurada": [27],
"home": [14,5,15,[0,1,2,3,4,6,7,8,9,10,11,12,13,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30,31,32,34]],
"reflet": [21],
"michael": [7],
"tecnicament": [13],
"eliminar": [30],
"build.xml": [18],
"critério": [27],
"hun": [6],
"utilizada": [[31,32]],
"aprendizagem": [20],
"memm": [21],
"malgax": [6],
"utilizado": [[15,18,20,29,31]],
"herero": [6],
"baixá-la": [10],
"baixo": [[28,32]],
"pescar": [3],
"aligndir": [[18,22]],
"system-host-nam": [13],
"mão": [20],
"ocasião": [20],
"creat": [10],
"python": [1],
"habitualment": [18],
"es_mx.dic": [29],
"alinhará": [18],
"infix": [15],
"envi": [9,[14,20]],
"tarbal": [12],
"perceba": [24],
"conveniência": [32],
"hindi": [6],
"idéia": [32],
"deixa": [[13,30]],
"omegat-development-request": [9],
"maçã": [21],
"executado": [18,21,1],
"google_api_key": [18],
"hora": [[13,21,22,31]],
"importada": [[31,32]],
"acontecerem": [31],
"desnecessário": [2],
"file": [23,[1,18],34],
"gratuito": [[2,7,18,20]],
"executada": [16],
"painel": [32,34,17,33,5,[21,31],[1,19,20,27],[15,18,24,26,30]],
"alinhamento": [15,[18,22]],
"meni": [17],
"gratuita": [18,[2,11]],
"começa": [11],
"opinião": [32],
"vantajosa": [28],
"tard": [[13,19,21,24,32]],
"meno": [14,[2,4,15,16,17,18,20,22,24,30,31]],
"aceit": [14],
"menu": [8,33,21,32,34,18,[1,11,13,17,23,24,28],[14,15,16,19,20,22,29,30]],
"ment": [24],
"unidad": [31,28,[11,21]],
"hye": [6],
"importado": [31],
"morfológica": [20],
"a-za-z": [25,27],
"condição": [[14,30]],
"return": [[20,21]],
"anular": [[18,22]],
"negação": [25],
"sinal": [17,[2,32]],
"tentará": [[18,30]],
"forçar": [2],
"aspa": [17],
"tamanho": [15,22,16],
"esquec": [[4,16]],
"source-pattern": [[18,22]],
"definitiva": [24],
"host": [[18,34],13],
"chua": [7],
"verificador": [29,[21,34],[0,33],14,[8,11,20,22]],
"consistência": [11],
"problema": [34,17,[16,33],[2,12,14,15,20,23,31],[18,21,22,32]],
"sort": [32],
"aproximação": [32],
"convenient": [31],
"xliff": [15],
"true": [18],
"envolvem": [23],
"present": [[14,16],[4,11,24,31,32]],
"assegure-s": [16],
"adicion": [31,[18,28]],
"islandê": [6],
"romeno": [[0,6]],
"groovi": [1],
"evitar": [[22,31],[2,34],[11,14,15,30,33]],
"trata": [11],
"pseudo-tradução": [31],
"especificado": [18,22,[14,29]],
"kmenueditor": [18],
"mesa": [32],
"rotular": [21],
"sindi": [6],
"sânscrito": [6],
"tortoisesvn": [2],
"especificada": [27,8],
"baixa": [[21,30]],
"abov": [31],
"abrirá": [[2,19,21]],
"checar": [27],
"atualment": [15,[5,21,31,32]],
"kmenuedit": [18],
"chuang": [6],
"recuperá-lo": [21],
"mês": [[22,31],18],
"akan": [6],
"xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx": [18],
"writer": [19],
"inseparávei": [21,8],
"dalloway": [28],
"rubi": [1],
"dzongkha": [6],
"desmarqu": [24,[13,20,28,30]],
"fiqu": [19],
"programa": [18,34,[15,21],[7,14,22],[0,2,11,33],[1,12,17,19,20,30,32]],
"dependerá": [[15,31]],
"alternância": [5],
"cover": [23],
"basco": [6],
"compilando": [[18,34],33],
"nomenclatura": [[16,34]],
"sentido": [[15,28,29,31]],
"pulaar": [6],
"edita": [32],
"basei": [20],
"independentement": [[31,32]],
"capítulo": [11,32,14,20,31,24,[4,7,17,25,27,28,33]],
"inconveniência": [14],
"locmanag": [15],
"simplifiqu": [16],
"escolha": [2,18,[14,21,28]],
"regex": [25,33,34],
"meta": [8],
"abra": [32,[18,19,22],[2,14,23,31]],
"mantê-lo": [[2,13]],
"intacto": [4],
"rodar": [18,22],
"adiel": [7],
"globai": [34],
"sango": [6],
"global": [[13,24]],
"redistribui-la": [7],
"colocá-la": [[21,31]],
"gramática": [0],
"modificador": [8],
"valor": [13,25,18,22,[31,32],16],
"reconhec": [[30,31,32]],
"atribuição": [[18,34]],
"gravou": [21],
"ibm": [18],
"ibo": [6],
"comun": [17,[11,21,31,32,33,34]],
"comum": [2,[21,27,31]],
"aconteça": [[28,31]],
"esquerda": [15,34,[5,13],[28,30,32,33]],
"dirigido": [30],
"identificável": [31],
"sundanê": [6],
"alemão": [[0,6,27]],
"conteria": [13],
"multiparadigma": [1],
"jean": [7],
"selecione-a": [31],
"desencaixa": [32],
"genitivo": [3],
"necessitam": [32],
"potenciai": [21],
"exceto": [25,[5,26]],
"funcionamento": [14,[13,15]],
"desmarcando-s": [21],
"ido": [6],
"guia": [32,[9,11,18]],
"opçõ": [21,13,34,[32,33],18,[26,27,30],[8,16,20,24,28,29],[10,23,31],[0,1,2,5,14,15,22,25]],
"idx": [12],
"que": [31,18,32,24,16,[2,13],14,[15,17,21],22,20,28,[11,19,27,30],29,[23,25],9,[8,26],[1,4,5],[3,7],[0,6,10,12]],
"qui": [17],
"observação": [32,[17,21,28]],
"requer": [[13,16,20,29]],
"início": [5,16,31,25,18,[14,17,22,28]],
"conflitant": [2],
"avança": [21],
"causar": [32],
"causam": [16],
"linux": [18,34,[2,14],[11,17,22,25,32,33]],
"checkout": [2],
"exigir": [[21,32]],
"dentro": [[2,31],16,[5,8,11,14,18,21,30,32]],
"vão": [32],
"processada": [16],
"inferior": [32,27,[18,20,28,31]],
"financeiro": [34],
"avaliação": [32],
"doação": [34],
"zha": [6],
"popup": [30],
"ifo": [12],
"pode": [[31,32],18,16,[2,21],[17,19],[29,30],[15,22,24,28],[11,13,14],8,27,[7,23],[0,1,5,12,25]],
"processado": [18,15,13,[24,31]],
"esquerdo": [1],
"zho": [6],
"octal": [25],
"segunda": [17],
"abreviatura": [28],
"sistema": [18,13,[2,21],[20,29,34],17,[5,8,22,23,31,33]],
"xx.docx": [13],
"adicioná-la": [[22,29]],
"alinhado": [15],
"consist": [32],
"segundo": [[21,32],[16,31],[3,8,11,18,30]],
"adicioná-lo": [18],
"optionsautocompleteautotextmenuitem": [8],
"demorar": [29],
"fernández": [7],
"dependent": [28],
"simplicidad": [2],
"zip": [[14,18]],
"gravação": [[8,21]],
"inicialização": [34,18,20],
"juntament": [32],
"pré-processado": [15],
"ibai": [7],
"yahoogroups.com": [9],
"cuidadosament": [14],
"sdlxliff": [15],
"restrito": [31],
"inicia": [[18,22],31],
"ação": [21,8,[5,12]],
"enquanto": [[11,13,32],[22,31]],
"pré-configurada": [21],
"iii": [6],
"copiou": [31],
"permanecerão": [14],
"kyle": [7],
"viewmarknotedsegmentscheckboxmenuitem": [8],
"espanhol": [[20,29],6],
"japones": [[21,23]],
"código": [[8,31],29,[6,13],34,28,[1,18,33],[0,2,5,11,14,21,22,23]],
"subscrib": [9],
"alfabética": [[31,32]],
"abstract": [33],
"lingala": [6],
"appropri": [23],
"yandex.api.key": [20],
"optionssaveoptionsmenuitem": [8],
"excel": [13],
"stardict": [12,34],
"omegat.l4j.ini": [18,10],
"first": [18],
"span": [13],
"atenção": [16,13,[18,22,28]],
"prefer": [14],
"hans-pet": [7],
"bloqueado": [18],
"crédito": [21],
"número": [32,21,16,30,15,[18,20,25,31,34],[1,8,11,26]],
"serem": [[13,14],[15,18,19,31],[8,11,22,28,33]],
"space": [[5,21]],
"domínio": [10],
"iku": [6],
"pijffer": [7],
"tecla": [21,[5,8],[19,32],[1,33]],
"zakharov": [7],
"russo-bielorrusso": [20],
"simpl": [15,34,[16,23],[17,30],[11,28,33],[0,2,13,18,20,21,25,29,31]],
"adição": [18],
"habilitar": [[8,13,20,24,29]],
"from": [23],
"editável": [[5,17],34],
"efetuada": [22],
"poluir": [31],
"publicado": [7],
"hardwar": [22],
"thunderbird": [29],
"identificaria": [0],
"ile": [6],
"editselectfuzzy3menuitem": [8],
"especificará": [18],
"acess": [32,[2,10,18,30,31]],
"verifica": [21,[1,16]],
"recarregar": [[8,13,21,28]],
"solicit": [31],
"project.projectfil": [1],
"fals": [18],
"phillip": [7],
"regra": [28,34,24,11,16,14,[0,15,33],[18,25,32],[13,20]],
"contanto": [21],
"vêm": [3],
"relativament": [[14,32]],
"inglês-catalão": [20],
"procura": [[25,27]],
"ferramenta": [34,[21,31,32],[20,33],[8,11,25],1,[14,15,28],[18,22]],
"frequent": [[9,18]],
"vincent": [7],
"senão": [21],
"marcador": [[13,32],34],
"momento": [19,[2,16,17,20,31,32]],
"proteg": [24],
"malayalam": [6],
"maior": [[11,13,16,24,25,28,32]],
"prefixada": [31],
"track": [9],
"ina": [6],
"ind": [6],
"papel": [11],
"contra": [22],
"pt_br.aff": [29],
"tmx2sourc": [31],
"oromo": [6],
"penalidad": [31,16],
"registrado": [[11,20,21]],
"distorcido": [16],
"quantidad": [18,[11,13,15,16,27,32]],
"renomear": [13],
"ilustrato": [28],
"ini": [15,[18,34]],
"kyrgyz": [6],
"registrada": [[30,32]],
"redistribuí-lo": [7],
"maximização": [32],
"dhttp.proxyport": [18],
"daquel": [13],
"contudo": [[4,20]],
"trado": [17,34,33],
"seria": [18,[4,9,11,17,22,31]],
"baseada": [[11,20,28],[1,2,34]],
"tampouco": [20],
"pronto": [[11,32]],
"subrip": [15],
"reconhecê-la": [19],
"operacion": [18,13],
"quanta": [[4,21]],
"score": [32],
"polonê": [[0,6]],
"comerciai": [[30,32]],
"pronta": [2],
"baseado": [[13,20],29,[11,12,17,18,30]],
"movê-la": [19],
"portanto": [28,[13,19],[2,9,15,16,18,23,27,31,32]],
"incluindo": [32,10],
"navajo": [6],
"aparecerá": [[0,2,15,18,20,21]],
"causarão": [16],
"appendix": [[0,1,2,3,5,7,8,9],[4,6],[22,28,31,34]],
"diagrama": [13],
"ipk": [6],
"passo": [2,[9,20,28]],
"passa": [[21,28,31]],
"copia": [21,32],
"aaa": [25],
"southern": [6],
"solari": [18,14],
"atribuir": [2,18],
"preferida": [[28,32]],
"ambient": [2,[18,22],20],
"último": [21,[4,18,31]],
"cinza": [21],
"manual": [32,[15,33],21,[4,7,8,9,11,14,34]],
"emparelhada": [32],
"aar": [6],
"azerbaijano": [6],
"seqüência": [27],
"cima": [[28,32]],
"kirundi": [6],
"funciona": [[20,21],[11,13,14]],
"bengali": [6],
"fato": [[20,32]],
"enviei": [22],
"orfão": [[28,31,34]],
"conter": [[18,31],[15,22,30,32],[2,8,29]],
"significado": [27],
"valão": [6],
"fase": [31],
"maltê": [6],
"abc": [25],
"rcs": [31],
"contem": [1],
"acessar": [10,[14,18],[11,13,21,27,32]],
"navaho": [6],
"abk": [6],
"flexibilidad": [28],
"textuai": [31],
"textual": [[23,31]],
"abr": [21,5,[1,2,32]],
"materi": [2,[11,31]],
"progressivament": [14],
"nomeado": [[8,29]],
"preferido": [32],
"algoritmo": [[3,8,21]],
"conteúdo": [2,13,[31,32],[14,21],30,[17,18],[16,22],[11,12,15,23,27]],
"isl": [6],
"iso": [6,34,23,[5,11,17,22,28,31,33]],
"fará": [[2,24,30,31,32]],
"referent": [32],
"movem": [5],
"tatar": [6],
"farão": [25,18],
"log.txt": [16],
"ativ": [24,[13,20]],
"zul": [6],
"implícita": [7],
"leiame.txt": [13],
"glossary.txt": [17],
"ita": [6],
"finish": [2],
"distinção": [[25,27]],
"incondicionalment": [31],
"add": [2,23],
"apagá-lo": [16],
"chines": [21],
"mencionada": [18],
"instruçõ": [18,[11,33]],
"última": [21,8,2,[9,14,22,27,31,32]],
"descrev": [11,[5,21,32]],
"mencionado": [[15,16,18]],
"deve-s": [16],
"larouss": [32],
"aprovado": [17],
"expandida": [20],
"untar": [[12,18]],
"substituição": [26,[21,31]],
"finlandê": [6],
"conectam-s": [2],
"benjamin": [7],
"benigno": [32],
"tailandê": [6],
"filters.conf": [22,18],
"enviar": [20],
"pasta": [[18,31],14,2,21,34,[24,32],22,19,[17,29],1,[12,27],[0,3,8,11,13],[10,15,30,33]],
"invertida": [18,25],
"irá": [18,29,[24,31],[13,21,23,26,27,28,30,32]],
"não": [18,21,[24,32],[13,16],[17,31],27,22,20,[15,25,28],[19,30],29,0,[8,26],[2,14,23],[3,4],[1,5,9,10,12,34]],
"contiv": [21],
"auto-completar": [8,21],
"aninhada": [16],
"amarelo": [[21,32]],
"afr": [6],
"trocar": [8,24],
"indicam": [16,[31,32]],
"inicializador": [18],
"filtra": [27,33],
"moeda": [25],
"mover": [[5,16,28,32]],
"pgdn": [5],
"saboga": [7],
"entidad": [27],
"efetivament": [24],
"targetlanguag": [13],
"directori": [2],
"quanto": [32,[2,11]],
"age": [31],
"indicar": [[15,16,18,31]],
"filtro": [13,24,34,[32,33],14,[15,21,23,27],[2,8,19,28,31]],
"escuro": [21],
"backup": [31,[22,34],14],
"contribuição": [34,9,33],
"properti": [15],
"título": [21,[8,27,32]],
"futura": [31],
"durant": [14,[18,24],32,[11,17,19,28,30,31]],
"editselectfuzzyprevmenuitem": [8],
"number": [13],
"manipulação": [16],
"isto": [2,[14,15,31],[18,21,22,27,30],[3,12,13,17,24,28,32]],
"editado": [[18,21]],
"copiar": [29,21,[5,19,32],[2,8,14,26,31,33,34]],
"iorubá": [6],
"utilizávei": [31],
"simpledateformat": [13],
"sempr": [31,[13,16,21],[8,14,15,17,19,24,25,28,30]],
"notará": [2],
"suportem": [11],
"habilitado": [24],
"multiplataforma": [18],
"exib": [21,32,[5,11,23]],
"parágrafo": [13,[24,28],[11,15],31],
"invariável": [30],
"referência": [[11,31],[13,21,25,32]],
"comentada": [13],
"script": [1,18,21,[14,34],[24,30,33],[0,2,9,11,22]],
"clicada": [5],
"system": [10,2],
"atalho": [8,21,33,34,18,11,5,32,[15,16,20],[2,6,7,9,10,17,24]],
"my_memory_email": [18],
"spellcheck": [29],
"isso": [18,31,32,29,[24,28],[15,16,17,19,20,21,23,25,26,30]],
"desambiguação": [20],
"credenciai": [2,20],
"avaliaçõ": [20],
"colega": [32],
"nada": [[5,21,25],0],
"dicionário": [29,[12,34],32,33,11,14,[3,17,25]],
"ajust": [24],
"gramaticalment": [3],
"khmer": [6],
"locai": [2,[24,31],[14,18,33]],
"querer": [31,[2,28]],
"local": [2,18,[14,21],[13,29,31],[8,15,17,22,32]],
"janela": [32,34,21,33,18,[26,27],[2,22],[1,16,29],[13,30,31],[3,8,14,15,19,20,24,28]],
"limburguê": [6],
"consertar": [[9,16]],
"locat": [18],
"kwanyama": [6],
"personalização": [[9,21]],
"escrito": [21,[15,23]],
"discussõ": [14],
"exclamação": [2],
"escrita": [21,[15,20,22]],
"mostrada": [[2,21,25,28,32]],
"reutilizando": [[31,34],33],
"indesejado": [16],
"resto": [2,32],
"partida": [[9,28,32]],
"lento": [[18,21,22,31]],
"segmento": [21,32,31,30,16,27,5,[28,34],8,13,11,15,[20,24],[19,26],[17,18],[1,10,22],[2,14]],
"mostrado": [32,21,[2,25,27]],
"deixado": [[13,18,21,24,30]],
"cada": [21,[2,13,15,19],[27,32],[20,22,24,28,30,31]],
"aka": [6],
"futuro": [[2,14,19,31]],
"função": [16,[29,30],[15,17,21]],
"tsonga": [6],
"acesso": [18,[2,14,32],[1,12,21]],
"variávei": [16,13,[21,32]],
"referem": [31],
"disponívei": [32,[18,21],[8,13,29],31,[1,2,9,11,22,25,27,28]],
"es_mx.aff": [29],
"requisição": [18],
"correspond": [25,13,27,[20,29],[14,16,21,23,24,28]],
"mode": [18,22],
"renomeá-la": [31],
"disponível": [18,[2,22],[8,10,11,21,29]],
"toolsshowstatisticsstandardmenuitem": [8],
"modo": [18,[22,34],15,30,21,[31,33],[2,8,11,23,32]],
"suportam": [31],
"all": [23],
"repositório": [2,31,34,4],
"percentagem": [32],
"umarov": [7],
"oferecem": [[2,23]],
"alt": [5,[1,8,18]],
"real": [[20,32]],
"típico": [18,31],
"recriar": [16],
"substantivo": [30,[20,32]],
"wildrich": [7],
"pinfo.list": [20],
"amh": [6],
"completar": [14],
"unix": [21],
"rede": [2,18],
"bósnio": [6],
"separadament": [15],
"roh": [6],
"especificação": [13],
"desejada": [[16,18,32]],
"ron": [6],
"resolv": [2],
"usará": [[11,15,18,31]],
"and": [23,[14,18,22,27]],
"modifica": [[14,24]],
"instrução": [13],
"desejado": [[18,20,31]],
"ano": [[22,31]],
"minuto": [[2,11,19,21,22],[4,7,18,31,33]],
"crie": [2,[21,31],[19,22,29]],
"ant": [18,[21,28],[14,16,31],[5,26,32],[0,2,3,13,15,17,19,20,22,24,29]],
"classificação": [32],
"cria": [21,[2,16,24,31]],
"sintax": [28,8,[13,24]],
"milhõ": [18],
"sardo": [6],
"considerando": [[17,18,32]],
"leitura": [21,[4,17,18]],
"possível": [[13,32],18,27,24,[14,15,16],[4,10,17,31],[1,3,5,9,19,20,26,30]],
"jnlp": [18],
"possívei": [31,25,[2,3,8,11,17,18,21,30]],
"helplastchangesmenuitem": [8],
"aplica-s": [18,[16,27,31]],
"cuanhama": [6],
"omegat.ex": [18],
"diferenciar": [16],
"cometeu": [19],
"reconhecendo": [3],
"sourcetext": [32],
"implementado": [20,18],
"moverá": [24],
"maximiza": [32],
"jar": [18,22,14,31],
"api": [20,18,10],
"jav": [6],
"editselectfuzzy2menuitem": [8],
"orientação": [20],
"app": [18],
"baseia-s": [2],
"implementada": [1],
"criar": [21,[18,19,31,34],8,[17,24],[14,16,33],10,[2,13,20,28,30,32]],
"verdad": [[2,15,16,24,32]],
"marshalê": [6],
"relação": [21,32],
"instalação": [18,29,[33,34],[0,20,32],[1,14,22]],
"capaz": [32,[11,29,30,31]],
"modificaçõ": [2,[7,13,14,21],[11,15,16,22,27,28,31]],
"exclusivo": [16],
"informação": [18,[11,21],[2,13,14,15,23,30,32]],
"alex": [7],
"zulu": [6],
"inconsistência": [4,[21,32]],
"modiqu": [32],
"omegat.sh": [18,10],
"feito": [18,[2,16,22,24,31,32]],
"editselectfuzzynextmenuitem": [8],
"mecanismo": [20],
"seguida": [28,[16,18,19,31]],
"ara": [6],
"portuguê": [[6,18,29,32]],
"are": [23],
"arg": [6],
"where": [31],
"memória": [31,11,32,[18,34],[19,21,24,33],[22,27],[14,28],[2,15,20],[3,16,17,25,26,30]],
"seguido": [25,[8,28],13],
"popular": [2,1],
"paypal": [9],
"art": [29],
"obrigado": [4,33],
"apont": [32],
"delimitador": [16],
"jdk": [18],
"call": [21],
"certeza": [17,[3,14,28,31]],
"reproduzir": [9],
"asm": [6],
"algo": [18],
"toolsshowstatisticsmatchesperfilemenuitem": [8],
"queira": [31],
"nível": [31,28,24],
"run": [6],
"rus": [6],
"titlecasemenuitem": [8],
"reiniciado": [8],
"ato": [4],
"extensivament": [11],
"arrastar": [32],
"editcreateglossaryentrymenuitem": [8],
"assistida": [[11,33]],
"simplificar": [16,[15,32]],
"apó": [28,16,14,[17,18,21,22],[13,25,27,31]],
"mostrará": [[2,27]],
"alic": [31],
"primária": [20],
"nominativo": [3],
"galego": [6],
"name": [14,13],
"auto-preenchido": [[21,30]],
"inspirado": [1],
"feita": [[15,18,21],[11,17,20,22,27,28,31]],
"nominativa": [17],
"vestígio": [16],
"precisará": [2],
"recurso": [21,[1,15,20],[9,11,12,13,28]],
"compilado": [20],
"encerrar": [21],
"distinguir": [31],
"meta-tag": [13],
"android": [15],
"acontecerá": [21],
"haitiano": [6],
"devem": [13,[18,28],2,30,[8,29],[15,16,21,22,31]],
"apoio": [[4,9,34]],
"ava": [6],
"enumerada": [8],
"assunto": [31,[9,22],[2,6,11,24,29,33,34]],
"ave": [6],
"comput": [23],
"ficará": [[13,16,18,19]],
"perceb": [29],
"usuário": [18,14,[32,34],21,33,22,[9,20],11,[2,24,31],[8,13,30],19,[7,10,15,25,27]],
"reutilizada": [31],
"pgup": [5],
"prossiga": [12],
"atualização": [21],
"quando": [31,[18,21],32,[15,30],[11,13,22],24,[5,14,17,28],[0,10,16,20,23,27,29]],
"target": [[14,21],[1,19,34],[2,23,24,31]],
"contido": [[18,31]],
"fabián": [7],
"config-dir": [[18,22]],
"combinarem": [31],
"casa": [3,29],
"pression": [[19,32],27,21,[2,17],[15,16,18,20,26,31]],
"manter": [2,[4,18]],
"evidentement": [[20,32]],
"africân": [6],
"tibetano": [6],
"caso": [31,[18,32],16,[11,13],[22,24],[2,3,5,15,23],[17,20,21,25,28],[14,27,29,30]],
"procura-s": [27],
"até": [[18,25],[5,11,17,24,32],[16,20,22,27,31]],
"auto-texto": [21,8],
"aymará": [6],
"primeira": [21,17,32,[1,2,13,18,31]],
"resolvida": [2],
"item": [8,18,[14,17,21,23]],
"sentir-s": [18],
"obter": [32,[7,14,18,20],[6,24,28,31]],
"iten": [32,[8,13],[17,21,25,27]],
"imagem": [0],
"atribui": [18],
"obedec": [31],
"targettext": [32],
"utualizar": [31,[33,34]],
"resolvido": [[2,31]],
"aym": [6],
"slide": [13],
"comportamento": [[5,18,31,32],11,[3,13,21,33,34]],
"localizar": [[21,22,27]],
"tratar": [20],
"guard": [2],
"comportam-s": [[5,28]],
"pattern": [23],
"pressionada": [8,5],
"leitor": [11],
"aaabbb": [25],
"aceitado": [18],
"aze": [6],
"desformatação": [20],
"edittagpaintermenuitem": [8],
"relacionada": [[11,14,16,19,20,32]],
"reformatação": [20],
"relacionado": [[2,9,11,31]],
"fechar": [21,[8,19,26,32]],
"inutilizar": [24],
"conectar": [2],
"feminino": [3],
"bokmål": [6],
"curdo": [6],
"display": [21],
"abordá-lo": [31],
"interrompido": [[18,22]],
"guloso": [25,33],
"efetiva": [31],
"viewmarknbspcheckboxmenuitem": [8],
"unicod": [23,[25,33,34]],
"corrêspondência": [34],
"possibilidad": [[2,11,16,24,31]],
"trabalhando": [31,11],
"pressionado": [8],
"cópia": [2,[9,13,21,22,24,31]],
"tratam": [24],
"habitu": [18,5],
"monitorar": [9],
"levado": [[24,32]],
"especifiqu": [[13,23]],
"delimitado": [16],
"jeito": [24],
"direita": [15,34,[5,13],[18,21,28,29,30,32,33]],
"motu": [6],
"holandesa": [31],
"às": [13,[18,27],[1,9,14,24,26,28,30,31,32]],
"msgstr": [13],
"atingido": [31],
"atributo": [13,[28,32]],
"katarn": [7],
"delimitada": [17],
"gere": [19,[15,33]],
"exceção": [28,34],
"estritament": [16],
"turcomeno": [6],
"chamada": [18,19,[11,28,31]],
"utilizar": [18,[20,22,29]],
"bashkir": [6],
"important": [14,11,22,[4,13,15,16,18,19,20,32]],
"chamado": [18,[10,19,21,29]],
"corrompido": [16,14],
"omegat.project": [[2,14,18,34]],
"imagin": [11],
"licença": [7,14,[12,18,21,31]],
"targetcountrycod": [13],
"cadastro": [10],
"procedimento": [31,22,[2,11,16,29]],
"direito": [2,32,18,[17,21],[1,7,30]],
"gera": [[14,25]],
"corrompida": [16],
"especificou": [27],
"renomeie-o": [31],
"webstart": [18],
"alto": [19],
"espanhola": [20],
"detectado": [16],
"situação": [[23,31]],
"pré-requisito": [22,18],
"criando": [[19,28]],
"sag": [6],
"sai": [21],
"recolhido": [31],
"subvers": [2,34],
"primeiro": [2,[21,28,32],[9,16,18],[4,8,14,17,19,20,24,25,27,29,31]],
"san": [6],
"inserida": [20,[16,21,27,30,31,32]],
"move": [21,[5,28]],
"símbolo": [2,[18,25]],
"resp": [3],
"usada": [[11,32],[15,18,22,24],[2,13,31],[14,16,17,19,20,25,28,29]],
"maneira": [[18,24],2,[5,21,23,27,28],[8,12,14,15,19,31,32]],
"técnico": [21],
"usado": [32,[13,14],[18,21],[11,15],[2,17,29,31],[0,8,20,22,24,30]],
"jpn": [6],
"also": [34],
"resx": [15],
"excluindo-s": [31],
"consol": [22,18,34,33],
"esteja": [[5,21],[2,19,20,24,30]],
"mous": [17,[0,18]],
"precisar": [[16,18],[14,19,20,24,28,30]],
"inserido": [[17,21],[27,32]],
"vice-versa": [15],
"técnica": [[13,20]],
"yandex": [20,[18,34],33],
"alta": [[20,31]],
"incluirão": [31],
"pāli": [6],
"consultar": [[10,32]],
"precisam": [2,16],
"a123456789b123456789c123456789d12345678": [18],
"viewmarkwhitespacecheckboxmenuitem": [8],
"equivalent": [32,[12,14,18,21,31]],
"bad": [20],
"alvo": [31,32,[21,24,27,29],[1,13,17,30]],
"lucen": [34],
"bak": [[2,6,14]],
"bam": [6],
"diferença": [32,30],
"bat": [18],
"indicará": [31],
"participar": [14],
"jre": [18],
"nepali": [6],
"optionsfontselectionmenuitem": [8],
"botõ": [32,26],
"francê": [28,18,[0,6]],
"minimizado": [32],
"duplicaçõ": [32],
"translatedfil": [22],
"customizando": [34,32],
"aaron": [7],
"plano": [21],
"terceira": [17],
"aprend": [20],
"anális": [[20,21]],
"deal": [23],
"freebsd": [[14,25]],
"digita": [32],
"delet": [5,30],
"terceiro": [32],
"see": [34],
"coluna": [[17,32],16],
"sei": [18],
"subsequent": [[2,31]],
"vito": [[7,33]],
"sem": [[7,13,32],[4,17],[10,14,18,31],[2,11,20,21,24,26,28,30]],
"seq": [17],
"palavra": [27,32,21,25,17,[3,5],11,[18,19,26,29,31],[0,14,15,16,20,28,33,34]],
"developerwork": [18],
"ser": [[18,31],17,[11,15],[14,28],32,21,13,22,[2,8,30],16,[19,29],[24,25],[7,10,20,27],[1,12]],
"aberta": [[16,27],[5,17,19,21,32]],
"seu": [18,[31,32],2,29,19,21,[13,20,22],[8,9,23,24,28],[4,11,12,16,27]],
"numeração": [[16,31,34]],
"set": [[14,18,22]],
"n.n_windows_without_jr": [18],
"apareçam": [[1,15]],
"categoria": [[25,32],[20,33]],
"validada": [32],
"optionsrestoreguimenuitem": [8],
"optar": [16],
"apresentará": [19],
"processamento": [21,[8,13]],
"prioritário": [17,34,33],
"validado": [21],
"igual": [[30,31],[21,32,34]],
"possam": [[16,31]],
"fleurk": [7],
"ordenada": [[2,21]],
"bihari": [6],
"classificado": [32,13],
"offic": [16,15],
"dígito": [[21,25],[18,22,31]],
"iguai": [[13,18,26,30]],
"bel": [6],
"ben": [6],
"bem": [32,28,[3,11,14,15,16,18,27,31]],
"começado": [24],
"resumem": [32],
"gerent": [2],
"aberto": [21,32,[17,30,31],[1,14,15,20,22,27],[0,5,11,13,18,28]],
"projectsavemenuitem": [8],
"joel": [9],
"digit": [18,[21,26],[2,14,19,27]],
"configurada": [[17,18]],
"groenlandê": [6],
"simplificado": [11],
"franco": [4],
"segurança": [18],
"ícone": [18,[2,14],32],
"configurado": [[11,13]],
"declinação": [17],
"destaqu": [11,33],
"pptx": [15],
"ordenado": [31],
"autotexto": [5],
"buscar": [21,[27,34],26,8,[10,25,33]],
"exportação": [31,[30,34],17],
"extinguindo": [23],
"example_project": [14],
"localização": [34,17,[15,22,31],[9,18,24]],
"cursor": [21,5,28,32,17],
"encaixa": [32],
"sentir": [22],
"enciclopédia": [12],
"xhosa": [6],
"logomarca": [14],
"abra-o": [27],
"erradament": [24],
"sim": [18],
"repetido": [21,[8,27]],
"client": [2,31,[11,16,32]],
"sin": [6],
"descrição": [[2,4,8,18,21,31,32]],
"codificar": [[15,20]],
"repetida": [[11,32]],
"falso": [32],
"punjabi": [6],
"tiago": [7],
"tajiqu": [6],
"manterá": [14],
"pescada": [3],
"tornarão": [28],
"gramaticai": [11],
"kalaallisut": [6],
"nenhuma": [[7,13,15,25],[17,18,20,21,23,26,31]],
"diferem": [18],
"bih": [6],
"obtida": [20],
"fundação": [7],
"armazenam": [31],
"select": [23],
"retornar": [32,[16,25]],
"bin": [14],
"armazenar": [[11,29,32]],
"direção": [15],
"gráfica": [22,18],
"apertium": [20,34,33],
"bit": [23],
"bis": [[6,25]],
"marcação": [[15,21,24]],
"hipótes": [16],
"projectopenmenuitem": [8],
"autom": [18],
"obtido": [21],
"decim": [30],
"sitema": [18],
"costuma": [31],
"toolsvalidatetagsmenuitem": [8],
"reutilizá-lo": [31],
"autor": [32,[7,21]],
"levar": [[22,31]],
"slk": [6],
"configuração": [34,33,[8,14],[18,29,32],[13,17,22,24],[0,5,7,9,10,21,23]],
"clicando": [[14,18,32]],
"john": [7],
"cenário": [32],
"slv": [6],
"vermelho": [13,[2,16]],
"viewmarktranslatedsegmentscheckboxmenuitem": [8],
"valu": [15],
"amba": [[16,20,22]],
"sme": [6],
"ilia": [7],
"frent": [21],
"vermelha": [31],
"world": [23],
"vale": [25],
"smo": [6],
"sido": [21,[14,32],[17,24,28,31]],
"ambo": [31],
"contador": [32,[33,34]],
"divehi": [6],
"recomendado": [[15,24,28]],
"planejar": [25],
"simplesment": [[9,29,31]],
"bidirecion": [15,[8,21]],
"pojavni": [17],
"deix": [28],
"aninhamento": [16,34,33],
"retroced": [21],
"editselectfuzzy1menuitem": [8],
"sna": [6],
"snd": [6],
"ajustada": [32],
"didier": [[4,7]],
"ilha": [6],
"podendo": [18],
"hide": [13],
"faltam": [[21,32]],
"gráfico": [[18,22]],
"desativar": [21],
"auto": [31,[21,23,24,34],13],
"teclado": [21,33,[11,32],[5,34],[2,6,8,24,27,30]],
"siga": [18,[15,20,22,29,31,32]],
"sob": [31,12],
"notepad": [17],
"obviament": [31],
"puder": [31],
"som": [6],
"posto": [2],
"hauçá": [6],
"download": [9],
"dele": [[5,18,21]],
"oracl": [18,8,13],
"inserirá": [21],
"dela": [32],
"sot": [6],
"evitará": [16],
"spa": [6],
"codificado": [23,15],
"identificação": [[13,15,16]],
"interpretar": [23],
"preserva": [13],
"adequado": [[18,29]],
"relevant": [21,[3,11,14,15,18,22,32]],
"dificultarem": [16],
"sublinha": [21],
"boa": [32,[4,25]],
"bod": [6],
"mongol": [6],
"mensagen": [22,[14,18,32]],
"bon": [20],
"bom": [9,[4,28]],
"percorr": [32],
"utilitário": [18],
"deixar": [30,[13,31],32],
"predominant": [24],
"mensagem": [18,[2,16],[10,14,22,31]],
"bos": [6],
"sqi": [6],
"multilíngu": [34],
"glossary.tbx": [17],
"criará": [18,13,22],
"customizar": [34],
"fouri": [7],
"total": [32,16],
"pressiona": [30],
"referem-s": [32],
"vê-las": [14],
"kal": [6],
"totai": [21,32],
"tenta": [13],
"kan": [6],
"altament": [28],
"kas": [6],
"thoma": [7],
"kau": [6],
"kat": [6],
"br1": [16],
"macro": [1],
"ficam": [[13,32]],
"sra": [28],
"srd": [6],
"kaz": [6],
"gigabyt": [18],
"chave": [[18,20],10,13,32,25],
"alinhador": [[22,34],33],
"control": [2,21,[8,11,16,33],[25,31,34]],
"aplicação": [11,[18,23]],
"informa": [20],
"no-team": [18],
"srp": [6],
"srt": [15],
"enquadr": [30],
"possa": [[7,20],[2,17,19,22,27,29,30,31]],
"esboço": [4,28],
"ficar": [16],
"possui": [[18,29]],
"alguma": [16,31,[13,14,19,30],[15,22,24,33]],
"específico": [13,24,18,[10,22,31],[0,11,14,15,29,32,34]],
"apresentada": [32],
"guarani": [6],
"back-up": [31],
"aumentar": [[28,31]],
"environ": [18,14],
"apresentado": [[2,32]],
"optionsautocompleteglossarymenuitem": [8],
"bre": [6],
"específica": [[14,15,24],[30,31],[2,11,13,18,21,22,25,28,32]],
"ssw": [6],
"intraduzívei": [13],
"pós-processamento": [24,21],
"removendo": [32],
"misturam": [17],
"exemplo": [31,13,18,[16,25,32],[2,17],[28,29,33,34],[15,22,23,27],[20,24,30],[1,3,11,12,21],[5,8,14,19]],
"indicada": [31],
"comunique-s": [14],
"passada": [20],
"on-lin": [29,11],
"kde": [18,34],
"indicado": [17],
"alimentar": [25],
"estão": [21,[18,32],[8,16,17],[12,14,20,22],[2,9,13,19,25,27,28,30,31]],
"conversão": [[15,34]],
"instância": [18,21],
"sua": [18,11,32,[14,28,31],[2,13,15],[4,16,19,21],[5,8,9,10,12,17,22,23,24,25,27]],
"sub": [17],
"ativo": [5,[21,28,31]],
"painless": [9],
"anexada": [21],
"languag": [18,[23,31]],
"extremament": [16],
"sul": [6],
"ativa": [[11,25]],
"sun": [6],
"sur": [8],
"executável": [[14,18]],
"respeito": [[25,31]],
"porta": [18,[3,34]],
"key": [10,15],
"seleciona": [21,[3,5]],
"svg": [18],
"opcionalment": [[18,31]],
"svn": [2,34,[18,31,33]],
"tagalog": [6],
"confirm": [[2,18]],
"bug": [9,[18,21]],
"fluxo": [32],
"bul": [6],
"visualização": [21,[15,22]],
"poupou-m": [4],
"swa": [6],
"recomendamo": [[2,28]],
"editreplaceinprojectmenuitem": [8],
"swe": [6],
"but": [25],
"substitua": [[31,32]],
"lógico": [[25,33]],
"express": [13],
"dest": [[3,5,11,14,15,16,17,20,29,31]],
"contexto": [[13,21,32],[11,16]],
"dess": [[15,18,28]],
"zero": [[16,25],[13,27]],
"cinco": [[21,31]],
"fábrica": [[14,34]],
"esqueceu": [12],
"variant": [[13,25,31]],
"lógica": [27],
"suportada": [[0,13,25,26]],
"digamo": [31],
"gotoprevioussegmentmenuitem": [8],
"legenda": [15],
"sino": [25],
"fragmento": [[1,16]],
"composta": [31],
"parec": [[14,16,17,18,19]],
"composto": [14],
"facilitar": [[16,21],[15,32]],
"gotopreviousnotemenuitem": [8],
"possivel": [13],
"prevalec": [31],
"editredomenuitem": [8],
"uilayout.xml": [14],
"suportado": [[17,32]],
"khm": [6],
"inici": [18,[11,31]],
"achar": [14,[20,22,31]],
"quadro": [18],
"predeterminado": [22],
"orientado": [25],
"substitui": [21],
"desd": [31,11,[15,16,17,18,20]],
"autocompletar": [5],
"learned_word": [34],
"algumnom": [14],
"deva": [18],
"etapa": [[14,20,32]],
"orientada": [1],
"hiri": [6],
"cautela": [16],
"gerenciador": [[14,29]],
"parar": [18],
"kik": [6],
"kim": [7],
"kin": [6],
"sobrepor": [32],
"avançar": [21,8],
"informado": [20],
"devo": [4],
"kir": [6],
"lakunza": [7],
"cuidado": [4],
"comprimento": [32],
"inscreveu": [[18,20]],
"deve": [18,[8,22,28],[16,17],2,[10,13,14,15,24,29],[3,9,12,20,30,32]],
"normal": [[14,18,27,31]],
"contenham": [[11,22,27,30,31]],
"guido": [7],
"significativo": [28],
"inteiro": [30,13],
"figura": [34,32,29],
"pt-br": [31],
"inscrev": [[14,18]],
"selecionado": [21,[13,31],18,[24,30],[0,1,14,19,20,29,32]],
"finalment": [18],
"seçõ": [[15,16]],
"inteira": [30,[21,28]],
"tudo": [[5,31],[2,8,26,32,34]],
"license.txt": [14],
"pescador": [3],
"selecionada": [21,32,[1,8,13,18,22,24,29]],
"runtim": [18,14],
"sérvio": [6],
"luba-katanga": [6],
"diferent": [31,32,[15,21],[2,13,16,18],[1,3,5,14,22,24,27,29]],
"comparativo": [3],
"feedback": [32],
"juntar-s": [19],
"filenam": [[13,23,31]],
"pular": [13,[6,26]],
"parcialment": [[20,32]],
"guzer": [6],
"roam": [14],
"modifiqu": [[8,13]],
"certa": [31],
"amor": [4],
"nbsp": [27],
"suazi": [6],
"mantenham": [15],
"elaborado": [20],
"interno": [5],
"gotosegmentmenuitem": [8],
"guardado": [2],
"enviado": [[2,20]],
"intervenção": [[13,31]],
"introdutório": [4],
"disponibilidad": [20],
"soment": [18,27,[31,32],2,[5,9,13,16,21,22,23,24,28]],
"enviada": [[20,22]],
"interna": [32,21,24],
"xx_yy.tmx": [31],
"key-valu": [13],
"usando": [[33,34],[18,29],10,[17,21,27],[2,13,28,32],[1,5,11,15,20,22,23,30]],
"tenha": [24,11,[2,3,17,18,21,28,31,32]],
"helpaboutmenuitem": [8],
"rode": [14],
"salvar": [21,30,[8,13,18]],
"apagá-la": [30],
"limitar": [18,10],
"permite-lh": [[13,14,15]],
"mandelbaum": [7],
"sugerida": [32],
"houver": [21,18,[31,32],[0,3,12,13]],
"regular": [25,27,[13,16],[11,28,34],[18,22,26,33],[12,15]],
"consumidor": [20],
"registre-s": [2,9],
"cient": [2],
"ajuda": [34,21,32,[4,8,33],31,[15,22]],
"c\'est": [17],
"sobrescrita": [[18,31]],
"redefinir": [5],
"certo": [[11,31],[2,13,28]],
"eslovaco": [6],
"versõ": [18,31,2,[4,20,25,29]],
"esqueça": [32],
"observ": [18,31,[20,32],29,[2,14],[5,9,12,16,17,22,24,27]],
"token": [3,34,[24,32],[0,2,11,33]],
"filter": [23],
"site": [9,2,[19,20,33,34]],
"sinalizador": [32],
"elemento": [13,15],
"sobrescrito": [30],
"frequência": [11],
"omegat.log": [14],
"criá-la": [20],
"localment": [2],
"tabulaçõ": [17,32],
"najlepših": [3],
"árabe": [6],
"influenciar": [21],
"parcial": [[8,30],32,21,31],
"kom": [6],
"kon": [6],
"influenciam": [16],
"corresponda": [27],
"kor": [6],
"garantia": [7],
"garantir": [[16,23]],
"birmanê": [6],
"manipulaçõ": [16],
"tab": [17,8,[5,21]],
"taa": [10,33,[17,20,21,34]],
"plain": [23],
"parciai": [34,[21,32],31,30,[5,15,16,19,24]],
"tah": [6],
"tag": [16,34,13,32,21,15,33,19,[18,22],[8,30,31],24,[5,11,28]],
"tai": [31,15,[13,28],[11,14,19,30]],
"tal": [[11,18,20,23]],
"rodapé": [13],
"tam": [6],
"administrador": [2],
"variedad": [[20,31]],
"carregá-lo": [31],
"tar": [14,18],
"criador": [2],
"tat": [6],
"onli": [13],
"filtrar": [27],
"individuai": [27],
"evitada": [[14,28]],
"projectreloadmenuitem": [8],
"aproximada": [32],
"anexado": [21],
"decidimo": [2],
"evitado": [32],
"haja": [13,[3,14,18]],
"estarão": [[8,16,17,27]],
"opção": [13,21,18,30,22,20,32,[10,17,27,31]],
"fitro": [23],
"pressionar": [32,[24,27]],
"apena": [13,17,[2,21,27,31],[15,18,24,29],[14,16,20,26,30,32]],
"sair": [21,31,8],
"sail": [20],
"vogai": [25],
"graham": [7],
"entanto": [[18,31],[15,16,32]],
"tbx": [17,21,[10,34]],
"winrar": [12],
"controlado": [[2,30]],
"duplicação": [16,34],
"cat": [11,[6,31,32]],
"duser.countri": [18],
"tcl": [30],
"consulta": [10,11],
"tck": [30],
"clarament": [23],
"criada": [[28,31],[11,14,29,32]],
"arrastando": [[18,32]],
"assegurará": [13],
"canto": [32],
"repetição": [32],
"descompactada": [18],
"movimentação": [21,[33,34]],
"informar": [23],
"typo3": [15],
"testar": [25],
"criado": [14,15,[17,19,21,31],[2,13,16,18,24,32]],
"align.tmx": [18,22]
};
