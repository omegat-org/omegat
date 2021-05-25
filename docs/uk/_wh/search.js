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
 "Додаток A. Словники",
 "Додаток B. Глосарії",
 "Додаток D. Регулярні вирази",
 "Додаток E. Налаштування комбінацій клавіш",
 "Додаток C. Перевірка правопису",
 "Встановлення та запуск OmegaT",
 "Практичні рекомендації",
 "OmegaT 4.2 — Посібник користувача",
 "Меню",
 "Області вікна",
 "Тека проєкту",
 "Вікна та діалогові вікна"
];
wh.search_wordMap= {
"табличку": [11],
"якась": [11],
"повтори": [11,[2,8]],
"шмат": [4],
"виразу": [11],
"ідентифікатор": [11],
"форматування": [6,11,10],
"шлях": [5,6],
"імітуючи": [8],
"повтору": [9],
"змініть": [4,[3,5,11]],
"перейдіть": [5,[8,11]],
"таблички": [[6,11]],
"відповідати": [[4,10]],
"заважати": [11],
"еквівалент": [0],
"порожнього": [6],
"якості": [[6,8]],
"пуста": [6],
"info.plist": [5],
"діалект": [4],
"видання": [0],
"випадку": [6,5,[9,11],[8,10]],
"ріко": [11],
"гнучка": [11],
"віддаленої": [6],
"ftp-сервер": [11],
"мали": [6],
"fuzzi": [11],
"вирази": [2,7,11,[3,4]],
"оновити": [[5,11]],
"відредагуйте": [5],
"закінчуйте": [11],
"частенько": [6],
"ріка": [11],
"left": [11],
"причому": [6],
"створює": [[5,6,11]],
"відомо": [6],
"інструкцій": [5,11],
"областю": [11],
"торгових": [11],
"згенерована": [10],
"перейменовує": [11],
"область": [9,11,8,[6,7,10]],
"області": [9,11,1,[7,10],[6,8]],
"сигналу": [2],
"форматів": [[6,11],9],
"згенеровано": [8],
"командному": [[6,10]],
"dgoogle.api.key": [5],
"підписані": [11],
"запитом": [5],
"можливості": [5],
"edittagnextmissedmenuitem": [3],
"попереджала": [11],
"самому": [11],
"випадні": [11],
"містяться": [6,1],
"заголовок": [11,[8,9]],
"перше": [[3,8]],
"спільними": [6],
"випадок": [6,9],
"quiet": [5],
"пунктом": [[1,3]],
"перша": [[1,3,5,11]],
"перейменуйте": [6],
"помилковому": [5],
"країну": [5],
"шляхом": [11],
"es_es.d": [4],
"першу": [2],
"йде": [11,6],
"cat-інструменти": [10],
"перестрахуватися": [6],
"the": [5,[0,2]],
"projectimportmenuitem": [3],
"випадне": [4],
"миші": [11,[5,8],[1,4]],
"країна": [5],
"перші": [11],
"голосні": [2],
"уповільнюється": [11],
"imag": [5],
"виступати": [3],
"відбулося": [11],
"навпаки": [11,6],
"країни": [11],
"файлами": [11,3,[1,5],[6,8]],
"туди": [9,[4,5,11]],
"уповільнить": [6],
"вихід": [[3,11]],
"терміналу": [5],
"виглядати": [5],
"мишу": [8],
"детальніше": [5],
"проєктах": [11],
"moodlephp": [5],
"currsegment.getsrctext": [11],
"термінами": [1],
"терміналі": [5],
"останніх": [8],
"показувалися": [11],
"базі": [11,4],
"часу": [[4,9]],
"позначка": [8],
"виділеної": [[5,8]],
"використали": [6],
"переглядати": [6],
"xxxx9xxxx9xxxxxxxx9xxx99xxxxx9xx9xxxxxxxxxx": [5],
"система": [11,5],
"виразністю": [11],
"перераховані": [11,8,3],
"системи": [5,11,6,[7,8],[4,10]],
"fr-fr": [4],
"віддаленим": [6],
"використані": [[5,11]],
"віддалений": [6,11],
"конвертація": [6],
"заголовку": [11,8],
"заповнюється": [8],
"клацнути": [[5,9]],
"інструкцію": [7],
"називався": [11],
"цільового": [11],
"primari": [5],
"складніших": [11],
"інструкція": [5],
"виділення": [5,[8,11]],
"webster": [[0,9]],
"випадки": [11],
"посилається": [6],
"переконатися": [6],
"метою": [11],
"інструкції": [5],
"використане": [11],
"вводити": [11,6],
"орієнтуватися": [11],
"передбачена": [11],
"вважатися": [11,9],
"жорсткого": [6],
"сортувати": [11],
"cjk": [11],
"клас": [2],
"отриманий": [5],
"вводите": [11],
"синхронізувати": [11],
"копії": [6,4],
"програмами": [4],
"копія": [[6,10,11]],
"метод": [5],
"отриманих": [[6,11]],
"будьте": [6],
"передбачено": [11],
"копію": [6,8],
"empti": [5],
"враховуються": [11],
"кросплатформенних": [5],
"кольори": [11],
"ідентичну": [11],
"метасимвол": [2],
"синтаксисі": [11],
"кольору": [8],
"сервера": [6,5],
"ілюстрацій": [7],
"купу": [6],
"модифікатором": [3],
"власні": [11,[8,9]],
"одиницями": [11],
"tmx": [6,10,5,11,8,9],
"repo_for_all_omegat_team_project": [6],
"знаходитися": [10],
"підсвічено": [11],
"колонці": [[8,9,11]],
"слові": [11],
"секунд": [11],
"nl-en": [6],
"завантажити": [[0,8,11],[4,5,6,7]],
"integ": [11],
"intel": [5,7],
"шукати": [11,8,3,[1,2,4]],
"fr-ca": [11],
"mainmenushortcuts.properti": [3],
"діалогу": [8],
"інформацією": [10],
"імена": [[9,11]],
"дотримуйтесь": [[5,6]],
"сервери": [5],
"відкривати": [11],
"скриптові": [11],
"експортуються": [11],
"підсвічене": [9],
"cmd": [[6,11]],
"coach": [2],
"систему": [11,6,5],
"перейти": [8,11,5,9],
"подивитеся": [11],
"покажчика": [11],
"subtitl": [5],
"gotohistorybackmenuitem": [3],
"власних": [11],
"втратяться": [6],
"збережених": [10],
"знайдені": [[9,11],[1,8]],
"російськими": [5],
"будуть": [11,8,6,5,[9,10],[1,3,4]],
"слова": [11,2,8,9,10],
"пошуку": [11,8,2,[6,10]],
"project-save.tmx": [6],
"власний": [1],
"системі": [5,11,4],
"сервері": [6,[5,11]],
"останній": [8,10],
"powerpc": [5],
"слову": [11],
"слово": [11,8,[3,4,5],9],
"деякий": [4],
"пишете": [9],
"розповімо": [6],
"виставлені": [10],
"створюватиметься": [11],
"кінці": [2],
"бажанням": [6],
"записів": [11],
"структорою": [10],
"знайдено": [1,[5,6]],
"кінця": [11,2],
"ідентичні": [[6,9,11]],
"деяких": [11,[1,6,10]],
"власне": [5],
"наразі": [1],
"немає": [[1,4,5,8],[9,11]],
"перемикатися": [6,8],
"виставлено": [11],
"фільтр": [11,6],
"правилах": [11],
"збереженою": [10],
"правилам": [11],
"помістіть": [10,4],
"збереження": [11,6,[1,3,4,8]],
"зазначте": [11],
"omegat.sourceforge.io": [5],
"збереженні": [6],
"критеріям": [11],
"хорошим": [11],
"репозиторієм": [6],
"досвідчені": [5],
"технологія": [5],
"перекладачеві": [6],
"примітках": [11],
"підключення": [[4,5,6]],
"починаються": [[2,3,5,8]],
"першій": [11],
"обирати": [11],
"трохи": [9,4],
"translat": [11,5,[4,6]],
"валідацію": [5],
"змінюйте": [[9,11]],
"включені": [6],
"стаття": [11],
"локалізація": [5],
"називаємо": [11],
"значеннями": [11],
"зводить": [11],
"клавішу": [11],
"локалізації": [6],
"docs_devel": [5],
"вимкнута": [8],
"статус": [[8,11]],
"tsv": [1],
"увага": [[6,11]],
"призводити": [8],
"так": [6,5,[9,11],10,[2,3]],
"там": [11,[0,5],[4,6]],
"gnome": [5],
"уваги": [11,6],
"нового": [11,6,[2,5]],
"випадках": [11,6],
"оновлення": [[5,11]],
"послужити": [6],
"вимкнути": [11],
"розраховується": [11],
"поверне": [11],
"синтаксиси": [11],
"вирівняти": [11],
"увагу": [5,[6,11],[8,10],[0,4,9]],
"несправні": [6],
"чітко": [11],
"клавіша": [3],
"практичної": [9],
"вирівнювати": [[5,8,11]],
"розбиває": [11],
"щось": [[5,6]],
"подобається": [11],
"csv": [1,5],
"n.n_linux.tar.bz2": [5],
"пересунуті": [8],
"розпізнані": [[1,11]],
"перевіряється": [1],
"секундах": [11],
"резервних": [8],
"ютера": [11,5],
"враховувалися": [11],
"підключених": [1],
"press": [3],
"оригінального": [11,9],
"збігається": [11,6,4],
"конкретним": [11],
"сортування": [[8,9,11]],
"повідомленні": [6],
"програмуванням": [11],
"повідомлення": [[5,9],6],
"нічого": [2,8,9],
"dmicrosoft.api.client_secret": [5],
"системними": [3],
"розривів": [11],
"залишаються": [[5,11]],
"введіть": [11,8],
"проводитися": [5],
"яндекс.перекладач": [5],
"розмістити": [6,10],
"прив\'язку": [9],
"ctrl": [3,11,9,[6,8],1,[0,10]],
"імперативний": [11],
"document": [11,5],
"використовуються": [11,[2,4,10]],
"вирішите": [11],
"більшими": [11],
"розкладки": [11],
"скоріш": [[5,11]],
"ніби": [5],
"resourc": [5,11],
"рівня": [6],
"відмінної": [11],
"інструменту": [11],
"позаяк": [11],
"team": [6],
"xx_yy": [[6,11]],
"docx": [[6,11],8],
"txt": [6,1,[9,11]],
"атрибутів": [11],
"мається": [5],
"виглядом": [11],
"оголошення": [11],
"інструменти": [11,[3,7],8,[2,10],6],
"тек": [[8,11]],
"рівні": [11],
"перевірте": [6,4,[0,5]],
"ютері": [5,[8,11]],
"являється": [11],
"без": [11,[5,6],9,[2,8]],
"можливих": [6],
"можливий": [[1,11]],
"source": [7],
"сеансу": [11],
"кроку": [10,11],
"trnsl": [5],
"основне": [9],
"альтернатива": [11],
"viewdisplaymodificationinfoselectedradiobuttonmenuitem": [3],
"перетягнувши": [5],
"index.html": [5],
"omegat.tmx": [6],
"перевіркою": [11],
"виконається": [6],
"віддаленого": [6,[5,8]],
"перекладах": [[2,7]],
"перекладач": [11,6,[9,10]],
"обмінюватися": [6],
"двонаправлений": [6],
"позначатися": [8],
"непотрібний": [11],
"створюються": [10],
"diffrevers": [11],
"теґ": [11,8,3],
"видимих": [8],
"якими": [11],
"потужність": [11],
"синхронізується": [6],
"знайдете": [11,5],
"локалі": [11],
"визначених": [11,5],
"канаду": [5],
"станеться": [8],
"закриєте": [6],
"повну": [11],
"колонка": [1,11],
"експортувати": [11,[3,8,10]],
"текстів": [11,10],
"текстовими": [11],
"попередньо": [[2,5,7,8,10]],
"колонки": [8,[1,11]],
"визначений": [[6,8]],
"повна": [11],
"кількості": [11,5,[1,9,10]],
"тим": [[6,8,11]],
"project.gettranslationinfo": [11],
"тип": [11,6],
"конфіденційності": [11],
"ділитися": [11],
"типів": [11],
"локаль": [11],
"тих": [11,[6,8]],
"конкретної": [11],
"генеруються": [6],
"start": [5,7],
"віртуальної": [11],
"прапорцем": [8],
"equal": [5],
"optionsalwaysconfirmquitcheckboxmenuitem": [3],
"налаштоване": [11],
"прапорця": [11],
"зробили": [5,11],
"основами": [[1,11],3],
"найпростіший": [5],
"втратять": [11],
"категорія": [2],
"налаштовано": [6],
"збіг": [3,9,11,8,10],
"пропуск": [4],
"ліцензією": [0],
"останні": [[3,8,11]],
"основі": [8],
"фільтром": [11],
"enter": [[8,11],3,5],
"особливостей": [11],
"вмісту": [11],
"категорій": [[2,7]],
"завантажте": [5,6],
"applic": [5],
"розпакування": [5],
"покращень": [8],
"projectteamnewmenuitem": [3],
"примірники": [6],
"праве": [6],
"останню": [8,3],
"використовуйте": [11,[5,6,8]],
"остання": [10],
"тлі": [8],
"оригінальному": [9,6],
"memori": [5],
"пробільні": [11],
"тло": [8],
"тримати": [10],
"відбувається": [11,5,6],
"паралельно": [[5,8]],
"англійської": [5],
"пакет": [5,8],
"напишіть": [5],
"несумісні": [1],
"omegat.jnlp": [5],
"мати": [11,[3,4,6,9,10]],
"ятовує": [8],
"перекладацьку": [6],
"regexr.com": [2],
"тмх": [[3,11]],
"запишеться": [8],
"n.n_windows_without_jre.ex": [5],
"командного": [5,6,7],
"працюють": [[2,11]],
"засоби": [6],
"верхньому": [[3,9]],
"тож": [6],
"перекладацька": [6],
"необхідності": [[4,10,11]],
"той": [11,9,[5,8,10]],
"запам\'ятайте": [11],
"опис": [11,3,5],
"dmicrosoft.api.client_id": [5],
"зауваження": [9],
"прапорці": [[2,7,11]],
"наведені": [5],
"варіантами": [[9,11],6],
"просити": [5],
"перетягнете": [9],
"config-fil": [5],
"тою": [6],
"визначеною": [10],
"вкажуть": [11],
"рухається": [11],
"табличці": [11],
"спеціалізованих": [6],
"позначено": [8],
"тої": [11],
"спрацьовує": [11],
"перевіряє": [[8,11]],
"видимою": [10],
"глобальним": [8],
"скільки": [10],
"створилася": [11],
"параметрів": [11,5],
"наприклад": [11,[5,6],4,[2,8,9,10],[0,1,3]],
"визначенні": [[6,11]],
"урахуванням": [9],
"визначення": [3],
"сервіс": [[5,11]],
"system-user-nam": [11],
"обмежити": [[5,11]],
"format": [11],
"три": [6,0,9],
"вмикає": [[2,11]],
"посібник": [7,8,[3,5,6]],
"console.println": [11],
"реченнями": [11],
"потужний": [11],
"активні": [11],
"консольного": [5],
"випадному": [11],
"найближчого": [9],
"синхронізувала": [5],
"продовженням": [11],
"ознака": [11],
"подано": [1],
"єктами": [11],
"розбиття": [11],
"переглянути": [11,[5,10]],
"проглянути": [11],
"позначені": [[8,11]],
"project_files_show_on_load": [11],
"активне": [8],
"детального": [11],
"шістнадцятковим": [2],
"завантажує": [11],
"активна": [11],
"тепер": [[6,11]],
"optionsexttmxmenuitem": [3],
"іншій": [6],
"build": [5],
"виноски": [11],
"потім": [8,[3,4,5,6,11]],
"marketplac": [5],
"вставиться": [[8,9]],
"взаємодії": [11],
"залучити": [5],
"пробільний": [[2,11]],
"вилучаються": [11],
"entries.s": [11],
"можливу": [2],
"перекладіть": [6],
"актуального": [11],
"del": [[9,11]],
"підвантажування": [11],
"gotonextuntranslatedmenuitem": [3],
"захист": [6,7],
"ідентичної": [11],
"targetlocal": [11],
"можливі": [[1,3,6]],
"path": [5],
"захищене": [11],
"реченням": [2],
"проєктів": [11,8,6,[1,5,9]],
"пробільних": [11],
"спеціалізовані": [[6,9]],
"добре": [[6,11]],
"allsegments.tmx": [5],
"рікота": [11],
"параметри": [11,6,8,5,4,[7,10]],
"множина": [[1,11]],
"інструкціями": [5],
"використовуватися": [11],
"однаковими": [11,8],
"тут": [11,10,8,[5,6,9]],
"словам": [11],
"helpcontentsmenuitem": [3],
"автоматичному": [5],
"розбивати": [11],
"можливо": [11,6,[1,2]],
"omegat-org": [6],
"фільтрів": [11,6],
"значення": [11,5,[1,3],9],
"словенської": [9],
"projectaccessdictionarymenuitem": [3],
"блокування": [5],
"стрілками": [9],
"структуру": [10],
"посортувати": [11],
"видає": [2],
"досягнути": [11,6],
"був": [9,[6,8],11],
"структури": [[10,11]],
"параметра": [11],
"встановити": [5,[0,11],[4,7]],
"клік": [5],
"цільовим": [9],
"кнопка": [[4,11]],
"відкриває": [8,[9,11]],
"duden": [9],
"right": [11],
"відразу": [1,8],
"оброблятиметься": [11],
"перезавантаженні": [6],
"покаже": [11],
"дуже": [[6,11],5],
"перезавантаження": [8],
"пакетів": [11],
"номером": [8,3],
"проміжним": [6],
"spotlight": [5],
"аргумент": [5],
"верхній": [11,8],
"всього": [9,5],
"нечіткого": [[8,9]],
"перекладаєте": [11,[1,9]],
"dir": [5],
"натискаєте": [11],
"обсяг": [11],
"div": [11],
"кнопки": [11,[3,7,9]],
"утрачені": [6],
"однакові": [4],
"самій": [6,[2,5,11]],
"viewfilelistmenuitem": [3],
"закриває": [11],
"половині": [11],
"російська": [5],
"кнопку": [11,5,4],
"нього": [5,[8,11],6],
"test": [5],
"пароль": [11,6],
"файлом": [[1,5,6,8]],
"вашого": [6,11,[3,4,5,8,10]],
"паролю": [11],
"omegat": [5,6,11,8,10,[3,7],4,1,2,[0,9]],
"надається": [11],
"специфікацію": [11],
"меншої": [5],
"однакову": [11],
"запущена": [5],
"імпортувати": [[1,8]],
"меншою": [6],
"назвіть": [6],
"повертає": [11],
"підтеках": [1],
"особливо": [11,[5,6]],
"замовчуванням": [11,5],
"переписані": [10],
"console-align": [5],
"щоразу": [6,11,4],
"ms-dos": [5],
"особливе": [11],
"projectopenrecentmenuitem": [3],
"загальних": [1],
"акаунт": [5,11],
"перенести": [5],
"змінювали": [[8,11]],
"робочого": [[5,9]],
"префіксом": [11],
"способів": [6],
"помаранчевого": [8],
"und": [4],
"project_save.tmx.temporari": [6],
"особливі": [6,[2,5,10,11]],
"жорсткий": [8],
"примусове": [8],
"консольному": [5],
"editoverwritemachinetranslationmenuitem": [3],
"спільною": [6],
"ingreek": [2],
"клавіш": [3,11,8,7,2],
"їхнього": [1],
"іншою": [[6,11]],
"кожне": [11,[3,8]],
"типових": [6],
"es_es.aff": [4],
"типовим": [[9,11]],
"іншої": [11],
"projectexitmenuitem": [3],
"заміняти": [11],
"кнопок": [11],
"звикатимете": [5],
"кожну": [9],
"text": [5],
"японська": [11],
"спрацює": [6],
"віддалені": [[10,11]],
"editregisteruntranslatedmenuitem": [3],
"init": [6],
"нижній": [11,8],
"перебіг": [9],
"кожні": [8],
"вставляє": [8],
"точкове": [11],
"мінімум": [[10,11]],
"текстом": [11,8,3,10],
"запитів": [11],
"шрифта": [11],
"автоматизації": [5],
"maco": [5,1],
"контекст": [11,9],
"розрізняти": [11],
"збережені": [11],
"doc": [6],
"системним": [8],
"змінювати": [11,[5,6,9]],
"відповідними": [9],
"мовну": [[0,11]],
"пунктів": [[3,11]],
"ключ": [11,5],
"paramet": [5],
"некоректно": [[1,8]],
"ієрархічної": [10],
"mac": [3,[5,6]],
"радіокнопки": [11],
"надає": [[0,11]],
"стати": [3],
"властивостей": [[4,6]],
"фактично": [9],
"розміщення": [11],
"заберете": [11],
"областями": [9],
"налаштуваннями": [11,5,[3,8],[6,9]],
"автоматичного": [11,8],
"man": [5],
"шрифту": [8],
"map": [6],
"заповнювачі": [11],
"порожній": [11,6,8,1],
"код": [3,11,4,5,6],
"учасників": [8],
"розгортає": [9],
"надійшли": [11],
"рядок": [9,[3,8,11],[5,6,7]],
"пишіть": [6],
"мовою": [6,[1,5,9],[8,10]],
"режимами": [6],
"порожнім": [11,[3,8,9]],
"url": [6,[5,8,11]],
"стемінгу": [9],
"вставляються": [8],
"ідентичним": [10],
"uppercasemenuitem": [3],
"старі": [11],
"порожніх": [11],
"viewmarkuntranslatedsegmentscheckboxmenuitem": [3],
"шляхами": [[5,6]],
"ідентичний": [8,[3,10,11]],
"набору": [11],
"японські": [11],
"ідентичних": [11],
"фукції": [11],
"дасть": [9],
"віддалене": [6],
"т.п": [[5,9]],
"збігів": [8,[9,11],10,[3,6],[1,7]],
"use": [5],
"бажано": [6],
"початком": [11],
"букву": [8],
"закриється": [[8,9]],
"опцією": [5],
"заблоковано": [9],
"розпізнати": [11],
"зараз": [[8,10,11]],
"omegat.jar": [5,[6,11]],
"omegat.app": [5],
"usr": [5],
"буква": [6],
"додавали": [8],
"розміру": [11],
"сканування": [11],
"букви": [11,5],
"зеленому": [9],
"utf": [1,11],
"показу": [11],
"бразильська": [4],
"приклади": [2,[6,7],11],
"вам": [11,6,5,4,[2,9,10]],
"стара": [5],
"вирішити": [11],
"окремого": [11],
"колонтитули": [11],
"servic": [5],
"ятей": [6],
"лежить": [11],
"методи": [11],
"відкривається": [[6,11],1],
"короткі": [11],
"dsl": [0],
"робочому": [5],
"перших": [10],
"типовою": [11],
"системний": [11],
"першим": [11],
"язково": [10,[4,6,11]],
"прочитати": [[5,6]],
"прикладі": [9,6,[2,4]],
"med": [8],
"прикладу": [[9,11]],
"збережена": [6],
"вас": [5,6,11,4],
"побачите": [5,11],
"одиниці": [11],
"побачити": [11,9,[5,8]],
"пощастило": [11],
"dtd": [5],
"останнього": [8],
"стані": [11],
"ваш": [11,[5,9]],
"одиницю": [11],
"одиниць": [10],
"язкове": [11],
"всьому": [11],
"кореневу": [3],
"стану": [9,[5,6,7,10]],
"повністю": [11,6],
"паттерни": [6],
"сат-інструменти": [6],
"рядку": [[1,3,5,6,11]],
"projectcompilemenuitem": [3],
"треба": [[6,11],5,[2,9,10],4],
"console-transl": [5],
"рядки": [3],
"перший": [11,8,[1,9,10]],
"відповідника": [8],
"gotonextuniquemenuitem": [3],
"узагальнення": [11],
"відповідники": [[9,10]],
"додасте": [11],
"wordart": [11],
"здійснюватися": [1],
"завантажуються": [11],
"optionsviewoptionsmenuitem": [3],
"вплинути": [11],
"регістру": [[2,8]],
"аж": [11],
"ролі": [11],
"commit": [6],
"targetlocalelcid": [11],
"зустрічаються": [11],
"напряму": [5],
"рекомендується": [11],
"відкриє": [8],
"project_stats_match.txt": [10],
"регістра": [3],
"поведінкою": [5],
"самі": [8,11],
"подібності": [9],
"xmx2048m": [5],
"змінюєте": [11],
"керуючись": [11,5],
"повідомляти": [9],
"клавіші": [1],
"xxxxxxxxxxxxxxxx.xxxxxxxxxxxxxxxx.xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx": [5],
"правила": [11,10],
"різних": [11,[8,9]],
"основними": [4],
"саму": [[4,5]],
"елементів": [[2,3]],
"різним": [0],
"бо": [11],
"само": [9,[2,6,11]],
"бекап": [11],
"маєте": [5,4],
"правило": [11,9,2],
"останньому": [5],
"введете": [11,9],
"можете": [11,5,9,4,6,[8,10],3],
"обернену": [5],
"язковою": [1],
"регістрі": [3,11],
"саме": [11,[5,8,10],[2,6,9]],
"підставлений": [11],
"змінився": [11],
"krunner": [5],
"нормальним": [5],
"libreoffic": [4,[6,11]],
"елемент": [5,11],
"стандартних": [11,3],
"додавати": [[10,11],6,[5,9]],
"робочий": [[5,11]],
"ви": [11,5,9,6,[4,8],10,3,1,0],
"документами": [11],
"стандартним": [[6,8]],
"стандартний": [[4,6]],
"іншим": [6],
"довгі": [11],
"інший": [11,[8,9]],
"половинної": [11],
"проста": [2],
"інших": [1,[6,11],5],
"речі": [5],
"прибрати": [11,[6,10]],
"застаріла": [6],
"вже": [11,6,5,[1,4,9],[3,7,10]],
"квантифікатори": [2,7],
"просто": [[5,11],10,4,[1,6,9]],
"браузері": [[5,8]],
"відсотка": [9],
"го": [6],
"viewdisplaysegmentsourcecheckboxmenuitem": [3],
"екрануватись": [5],
"editregisteremptymenuitem": [3],
"розташованого": [8],
"англійській": [2],
"тій": [11,[4,5,6]],
"локальний": [6,11],
"чисел": [[9,11]],
"виклику": [5],
"перемикнути": [6],
"відкріпили": [11],
"прості": [6,1],
"open": [11],
"вебсайті": [10],
"де": [11,5,6,3,[1,8,9,10]],
"www.oracle.com": [5],
"мультипарадигмова": [11],
"називаються": [11],
"перед": [11,8,6,9,[1,2,5]],
"сайт": [11],
"правильно": [6,5,[1,9]],
"скриптах": [5],
"project": [5,11],
"відокремити": [[9,10]],
"до": [11,8,5,6,9,3,10,1,0,[2,4]],
"власної": [9],
"окремому": [[1,8]],
"xmx1024m": [5],
"копіюються": [[6,11]],
"визначіть": [10],
"файл": [5,6,11,10,1,8,3,[0,9]],
"методів": [11],
"правильні": [5,[4,6,11]],
"введена": [11],
"японським": [5],
"конструкції": [2],
"документі": [[6,8,11],3],
"першої": [[8,11]],
"найпростішій": [5],
"penalty-xxx": [10],
"французької": [11],
"gotonextsegmentmenuitem": [3],
"збігом": [8,3],
"діалозі": [8,6,11],
"числа": [11,9,8,6],
"розширень": [11],
"nnn.nnn.nnn.nnn": [5],
"особливого": [11],
"завантажиться": [6],
"видалення": [11,6],
"число": [8,[9,11]],
"обробляються": [11],
"звичайної": [[5,6]],
"введені": [11],
"abort": [5],
"визначеній": [1],
"версії": [5,6,4],
"ніяк": [5],
"же": [8],
"версію": [[5,8]],
"версія": [5,6,8],
"впевнені": [10],
"ієрархічну": [10],
"прокручування": [11],
"письмом": [6,7],
"зазначена": [11],
"документа": [6,9],
"впевнено": [6],
"запитати": [9],
"редагувати": [11,8,5],
"видаляє": [[4,8,11]],
"відносно": [9],
"за": [11,5,6,9,[4,8,10],[2,3],1,0],
"версій": [5,10],
"додатково": [11],
"зв": [11],
"опублікуйте": [6],
"документу": [11,8],
"язковий": [8],
"відтворити": [6],
"es-mx": [4],
"відсоток": [9,11],
"додаткові": [11,[8,9,10]],
"документи": [8,11,6,3,[5,10]],
"правильна": [6],
"прикладами": [5],
"знайдіть": [5],
"поводить": [11],
"зі": [11,8,6,[0,3,4,7],10],
"резервна": [10],
"пуск": [5],
"знову": [[8,11]],
"замінені": [11],
"ключових": [11],
"ввівши": [8,11],
"компонент": [6],
"високоякісні": [10],
"глосарієм": [1,7],
"відвідувати": [11],
"інтернет-сторінок": [11],
"поточна": [5],
"простих": [11],
"неунікальний": [9],
"резервні": [6],
"користування": [1,7],
"зазначені": [11],
"скриптовою": [11],
"додаткова": [11],
"простий": [2,6],
"інтернету": [5,4],
"загальна": [9],
"резервну": [6],
"оновлюються": [5],
"головній": [6],
"автоматичне": [11,6],
"двох": [[6,9],[5,10,11]],
"запрацює": [4],
"word": [[6,11]],
"слайдів": [11],
"зазначено": [[1,6,11]],
"проєктом": [[6,11],10],
"словники": [0,4,[6,7,9,10],[1,8,11]],
"місці": [[5,8,11],[4,6,9]],
"інтернеті": [[4,6]],
"місця": [11,[4,9]],
"щоденного": [5],
"локалізована": [5],
"перетягніть": [5],
"японською": [11],
"точному": [11],
"числі": [11],
"автоматичну": [11],
"змінитися": [11],
"призначений": [[1,2]],
"локальної": [6],
"обнуляє": [8],
"японської": [11],
"автоматично": [11,8,[6,10],5,[3,4],[1,9]],
"словнику": [11,8],
"обравши": [11,9],
"помічати": [6],
"змінитись": [11],
"автоматичні": [6],
"загальну": [11],
"місць": [11],
"закритті": [6],
"автоматизованого": [7],
"користувацького": [11],
"місце": [8,[6,9],11],
"пропущений": [[2,3,8]],
"цільової": [4],
"lingvo": [0],
"абзацами": [11,8],
"mrs": [11],
"диску": [[5,6]],
"вниз": [11],
"допомогою": [[5,11],9,[4,6,8],10],
"буває": [10],
"загальні": [11],
"стійкими": [10],
"словника": [4,0],
"змінивши": [4],
"плагінів": [11],
"стануть": [[4,11]],
"ми": [[6,11]],
"зафіксований": [6],
"накласти": [9],
"відкрито": [8],
"вашому": [[1,3,5,6]],
"звичайних": [0],
"відкрити": [8,11,3,5,6,9],
"усе": [3,11],
"зеленим": [[8,9]],
"ньому": [[4,5,6]],
"писати": [[8,11]],
"чисто": [6],
"напрямком": [6,8,3],
"речень": [11],
"pt_pt.aff": [4],
"на": [11,5,6,8,9,3,[1,4,10],[0,2]],
"не": [11,6,5,8,2,1,9,4,10,[0,3]],
"html": [11,5],
"скомпроментувати": [11],
"звичайний": [10],
"сховищ": [11,6],
"загальної": [[9,11]],
"онлайн": [4],
"звичайним": [[1,5,6,11]],
"проблем": [[1,5]],
"глосаріях": [1,[9,11]],
"сервер": [11],
"унікальність": [11],
"залишати": [11],
"усі": [6,11],
"відкриті": [[8,9]],
"artund": [4],
"якомога": [6],
"випущених": [8],
"сумісним": [5],
"об": [11,6],
"прогортати": [9],
"частиною": [2],
"ні": [5,11],
"третя": [1],
"позначає": [11],
"файлів": [11,6,10,8,4,[5,9],[1,7],3],
"робите": [11],
"корисно": [11,[5,8,9,10]],
"ос": [1,5],
"робити": [11,4],
"от": [[5,9]],
"переходить": [8],
"www.ibm.com": [5],
"відпущена": [3],
"видалених": [9],
"курсора": [8,11,9],
"все": [6,[5,9,11],4],
"корисні": [2],
"пл": [11],
"інше": [[0,11]],
"регулярними": [2,7],
"по": [11,5,[6,7,8]],
"сегментами": [11,9],
"вгорі": [[9,11]],
"наведіть": [8],
"пріоритетним": [10],
"резервує": [5],
"розблоковано": [9],
"принципом": [11],
"словниками": [0,[3,4,7]],
"переходите": [9],
"command": [[3,9]],
"вся": [6],
"n.n_without_jr": [5],
"всі": [11,6,5,[8,9],[2,3]],
"робить": [6],
"вгору": [11],
"надрукуйте": [11],
"видаляти": [11],
"інші": [6,11,5,9,[7,8,10]],
"перекладете": [11],
"стилі": [11],
"viewmarkbidicheckboxmenuitem": [3],
"зроблена": [11],
"який": [11,8,[5,6],[4,9],[1,2,10]],
"яким": [[2,6],[5,11]],
"переробити": [9],
"розділу": [11],
"бути": [[5,11],6,[1,2,10],[0,8,9]],
"запущений": [5],
"стан": [8],
"повернути": [9],
"певною": [11],
"поряд": [11,[5,8]],
"місцевих": [6],
"стиль": [6],
"вул": [11],
"яких": [11,1,[6,8],[9,10]],
"уникати": [11,10],
"розділі": [11,[5,9]],
"певної": [6],
"забрати": [11],
"теґів": [11,[3,5,9],6,8],
"зроблені": [[6,8]],
"самий": [9,[5,6,11]],
"version": [5],
"та": [11,6,[5,8],9,4,1,[3,7],2,0,10],
"цілого": [[2,11]],
"завантажений": [5],
"те": [11,9,[2,5,6,8]],
"паперових": [9],
"метатеґи": [11],
"нові": [11,[1,3],8],
"то": [11,5,8,9,6,[4,10],[0,1,3]],
"попереднього": [8,6,3],
"кодуванні": [1,11],
"напрямки": [6],
"ту": [[5,6],4],
"кодування": [11,1,7],
"projecteditmenuitem": [3],
"адреса": [5],
"britannica": [0],
"глосаріями": [3],
"запам": [[8,11]],
"нову": [5],
"несе": [6],
"ті": [[5,6,9,11],1],
"весь": [6,8],
"нове": [11,8],
"наповнення": [6],
"адресу": [11],
"нова": [5,3],
"знайдених": [9],
"підсвічуватися": [8],
"рушіїв": [8],
"циклічна": [3],
"поклавши": [3],
"iceni": [6],
"швидко": [11],
"періодично": [6],
"отримаєте": [5],
"резервного": [8],
"хмарне": [6],
"замовника": [6],
"очима": [9],
"перевіряться": [11],
"записуватися": [11],
"якийсь": [11,[2,5,6,9]],
"закінчує": [2],
"просп": [11],
"рівнів": [10],
"єктно-орієнтований": [11],
"порівнянні": [11],
"dsun.java2d.noddraw": [5],
"порівняння": [11],
"попередньої": [[3,8,9]],
"з\'явиться": [9],
"проте": [[1,11]],
"діалогові": [11,[7,8,9]],
"захочете": [11,6],
"x0b": [2],
"доступні": [11,5,[3,8],[4,6,9]],
"це": [11,5,6,9,8,[0,1,4,10],3,2],
"директорія": [10],
"скопіюються": [11],
"повторів": [[8,11],9],
"безкоштовно": [5],
"http": [6,5,11],
"підлягають": [5],
"зможуть": [6],
"напрямок": [6],
"експорт": [6],
"кожному": [1],
"визначає": [11,[5,6,8]],
"символу": [11],
"залежно": [11,5,[8,9]],
"речення": [11,6],
"мінімальну": [2],
"стадіях": [6],
"торгові": [[9,11]],
"цю": [11,8,6,4],
"ця": [11,8,5,10,6,4],
"напрямку": [8],
"екран": [5],
"ці": [11,8,6,[0,5,10]],
"глосарія": [1,9,[10,11],[3,6,8]],
"чи": [11,5,[2,6,8],[0,9],1,[3,4],10],
"відомим": [9],
"відкрийте": [[6,11],[5,10],[4,8]],
"зберігає": [[6,8]],
"символи": [11,8,[1,2],[3,7]],
"процесі": [[6,11]],
"функціональності": [8],
"глосарії": [1,11,[3,10],[0,4,6,7,8]],
"projectsinglecompilemenuitem": [3],
"нечіткі": [11,9,[6,7,8]],
"відсотком": [9],
"закривати": [11],
"відомих": [11],
"логічного": [11],
"якою": [[5,6,11]],
"символа": [5],
"натомість": [6],
"якої": [2,[9,11]],
"вказувати": [[4,6]],
"myfil": [6],
"повторами": [9],
"змінюється": [[6,10]],
"валідний": [5],
"часто": [11,[6,8]],
"правому": [9],
"було": [[6,11],[8,9]],
"перетягнути": [9],
"нема": [5,[6,9,11]],
"доступне": [11],
"неперекладеним": [11,8],
"були": [11,8,5,9,6,[1,4,10]],
"доступна": [5],
"типовими": [9],
"процесу": [[9,11]],
"глосарій": [[1,11],3,9,[7,8]],
"неперекладених": [11,9],
"найкращий": [11,9],
"діалогове": [11],
"сторони": [6],
"була": [[5,6],10],
"попередньому": [9,[2,6,8]],
"стягнути": [[3,8],[6,11]],
"розділений": [9],
"конфліктують": [3],
"скасовано": [8],
"ще": [11,[5,6],9,[2,8]],
"system-os-nam": [11],
"optionstabadvancecheckboxmenuitem": [3],
"потрібного": [5],
"номер": [[9,11]],
"букв": [[8,11]],
"через": [5,11,6,10,[1,2]],
"що": [11,6,5,8,9,10,1,4,2,7],
"комбінувати": [5],
"динамічного": [11],
"явитися": [[3,10]],
"optionsviewoptionsmenuloginitem": [3],
"зберегти": [8,5,[3,11]],
"збігах": [11],
"повторити": [[3,8]],
"команди": [5,11,8,6,9],
"налаштувати": [11,[3,4,6]],
"tar.bz2": [0],
"мексиканський": [4],
"перекладів": [6,11,10,9,8,5,7],
"застосування": [11],
"створіть": [6,[4,5]],
"команда": [5,11,7],
"додаток": [[1,2,4],[0,3,5],6],
"bundle.properti": [6],
"значно": [[6,11]],
"ширини": [11],
"являться": [[4,11]],
"закрийте": [6],
"ключовими": [11],
"x64": [5],
"команду": [5,[8,10,11]],
"ширину": [11],
"keyev": [3],
"завантаження": [5,[6,11]],
"сесіях": [5],
"збережіть": [6,[3,5]],
"дорівнювати": [5],
"обережно": [11],
"вимкніть": [1],
"вікна": [11,9,[7,8],5,[3,10]],
"вікно": [11,8,9,5,[4,7]],
"очиститься": [11],
"кожним": [11],
"обережні": [6],
"властивості": [11,[6,8],[0,3,4,7,10]],
"знаходяться": [11,10,5],
"додається": [[6,10]],
"чергу": [11],
"optionsteammenuitem": [3],
"винятків": [11],
"заберіть": [11,5],
"gzip": [10],
"символів": [11,2,7,[5,8],[3,9]],
"зберігається": [[10,11],[1,6,9]],
"esc": [11,2],
"причин": [[5,11]],
"x86": [5],
"облікові": [11],
"точні": [1],
"завантаженні": [11],
"відмінностями": [11],
"встановлення": [5,[4,7],11,8],
"nostemscor": [11],
"кодів": [4],
"залишить": [[10,11]],
"друкувати": [11],
"перекладені": [11,8,6,10,[3,5,9]],
"проігноровані": [5],
"єдину": [11],
"риску": [5],
"порожня": [10],
"застосувати": [11],
"console-createpseudotranslatetmx": [5],
"точно": [11],
"перекладену": [8],
"порожні": [3],
"значки": [9],
"etc": [11],
"longman": [0],
"fuzzyflag": [11],
"перекладено": [5],
"єктом": [11],
"escap": [1],
"merriam": [9],
"кожного": [11,8],
"зберігатися": [11],
"вікні": [11,4,8,[5,6,10]],
"готовими": [5],
"встановленим": [8],
"залишити": [[6,11]],
"корейські": [11],
"вікон": [11],
"поруч": [3],
"налаштувань": [11,6,[4,5,8],[3,9]],
"позначення": [3],
"плагін": [11],
"ігноровані": [11],
"назвапроєкту-omegat.tmx": [6],
"прототипно-орієнтована": [11],
"двома": [[6,11],[4,5]],
"знадобитися": [11],
"як": [11,6,5,9,[2,10],8,0,4,[1,7],3],
"буде": [11,5,[6,8],10,9,1,4],
"виставити": [11],
"n.n_without_jre.zip": [5],
"уникнути": [11,[6,10]],
"відсилати": [11],
"посередині": [2],
"magento": [5],
"великої": [[3,8,11]],
"рівень": [11],
"елемента": [5],
"перекладене": [11,6],
"контрольний": [2],
"стандартній": [8],
"елементи": [11,6,5],
"зекономити": [[6,11]],
"розділи": [6],
"акаунту": [5],
"точки": [11,9],
"португальська": [4],
"u00a": [11],
"французьким": [5],
"великою": [11],
"запасати": [1],
"командних": [[8,11],5],
"відсоткових": [10],
"багато": [11,2],
"загрози": [6],
"різниця": [11],
"установити": [4],
"різницю": [11],
"shift": [3,[6,11],8,1],
"командним": [6,11],
"складаються": [11],
"аналізує": [[2,11]],
"рядка": [5,2,11,[6,7,9]],
"java": [5,11,3,2,[6,7]],
"exe": [5],
"смаком": [10],
"командний": [6,[3,8]],
"опрацює": [11],
"перегляду": [6],
"кожен": [6,[1,8,9,10,11]],
"залишаться": [[10,11]],
"згадувалось": [6],
"відправлялись": [11],
"lang2": [6],
"lang1": [6],
"наведеного": [11],
"project_save.tmx": [6,10,11],
"dictionari": [0,10],
"властивостях": [1],
"відкритті": [6],
"піддаються": [11],
"наявне": [5],
"динамічною": [11],
"екранування": [[2,7]],
"відкриття": [[3,5],6],
"dictionary": [7],
"входять": [10],
"кожної": [[6,9,11]],
"зберігають": [6],
"наявну": [6],
"від": [11,[5,6],8,9,10,[1,2,3],[0,7]],
"переклад": [[8,11],9,6,3,1,[5,7,10]],
"виключень": [11],
"відображений": [8],
"він": [5,[9,11],10,6,[1,8],[2,4]],
"ніколи": [11],
"наявні": [11,[3,10]],
"повільно": [5],
"половинну": [11],
"китайські": [11],
"розрахованого": [9],
"підтвердите": [10],
"заархівованими": [10],
"чотири": [[8,9]],
"замість": [[3,5,8,11]],
"timestamp": [11],
"тихому": [5],
"спробує": [6],
"звертайтеся": [6],
"projectaccessrootmenuitem": [3],
"мовної": [6,11],
"dyandex.api.key": [5],
"знає": [6],
"спробувати": [6],
"plugin": [11],
"створюєте": [6],
"увесь": [8],
"знаю": [5,11],
"користувалися": [6],
"поводитись": [11],
"editinsertsourcemenuitem": [3],
"microsoft": [11,[5,6],9],
"projectnewmenuitem": [3],
"кнопкою": [11,9,[1,4,5,8]],
"наступного": [8,[3,11],9],
"optionstranstipsenablemenuitem": [3],
"наприкінці": [[10,11]],
"підтримуються": [11,[5,6]],
"блок": [2],
"glossari": [1,[6,10],[9,11]],
"авторизація": [11,3],
"із": [11,5,[2,8],9,6,3,4],
"будете": [[10,11]],
"ignored_words.txt": [10],
"перелічених": [11],
"користувачем": [11,[3,8]],
"configuration.properti": [5],
"github.com": [6],
"ім": [11],
"ін": [[5,11]],
"аркушів": [11],
"уникаючи": [2],
"glossary": [7],
"оригінальним": [11],
"прискорити": [6],
"оригінальний": [6,[9,11]],
"префікс": [11],
"окремі": [11],
"неправильне": [1],
"оригінальних": [6,[9,11]],
"заповнений": [11],
"неправильно": [11],
"string": [5],
"їм": [10,11],
"верхня": [9],
"відвідування": [8],
"потрібні": [6,[5,11],4],
"розриву": [11],
"їх": [11,6,[4,5,10],[8,9],1],
"покинуті": [9],
"визначте": [11],
"зручних": [5],
"іншого": [11,8,[5,9]],
"брати": [11],
"зручний": [[5,11]],
"її": [6,5,9,11,[0,10]],
"url-адреса": [4],
"потрібно": [11,6,[1,8],[0,2,5]],
"частина": [9,[5,11]],
"потрібне": [[5,11]],
"частини": [11,[6,9],8],
"selection.txt": [11,8],
"виберете": [9],
"xhtml": [11],
"потрібна": [6,11],
"частину": [8],
"скасовані": [6],
"типовий": [1,[3,6,7,8,9,11]],
"однаковою": [[0,6]],
"finder.xml": [11],
"визначені": [8,[2,7,10,11]],
"window": [5,[0,2,8]],
"url-адресу": [11],
"користувачам": [5,7,11],
"положення": [9],
"частині": [10,11],
"способом": [[5,6]],
"disable-project-lock": [5],
"omegat.pref": [11],
"фрази": [11],
"перекладають": [6],
"фон": [10],
"визначена": [11],
"керують": [11],
"воно": [11],
"цілому": [[2,8,11]],
"фраза": [11],
"кращий": [11],
"відмінності": [11,6],
"відображена": [11,[8,9]],
"вони": [11,6,[3,4],[0,2,5,8,9,10]],
"розділювальний": [11],
"пропозиціях": [8],
"howto": [6],
"вона": [11,5,9,8,[1,6,10]],
"доведеться": [[6,11],5],
"pt_pt.dic": [4],
"вкажіть": [5],
"ключа": [5],
"фразу": [11],
"level1": [6],
"текстовому": [11,[1,2]],
"level2": [6],
"відібрані": [10],
"однина": [1],
"виходите": [8],
"запит": [11],
"незалежно": [[5,11],1],
"самої": [5],
"пропущені": [8,3],
"ключі": [[5,11]],
"запис": [[8,11],[1,2,3,9]],
"вивід": [11,[3,6,8]],
"прикладів": [11,[2,5]],
"web": [5,7],
"мовам": [11],
"en-us_de_project": [6],
"найкраще": [[6,10]],
"яттю": [6],
"спробували": [6],
"необмеженого": [5],
"завантажуючи": [6],
"перевікою": [10],
"перекладних": [0],
"editselectfuzzy4menuitem": [3],
"editregisteridenticalmenuitem": [3],
"наведений": [[5,11]],
"перетворити": [6,8],
"створиться": [[10,11]],
"натисність": [8],
"шаблон": [11,2],
"має": [11,6,5,8,1,0,[3,4,9]],
"працюватиме": [11],
"вставлятися": [[6,10]],
"виходить": [8],
"виконання": [10,11],
"відкритим": [11],
"pt_br.dic": [4],
"використання": [[7,11],6,[2,5],[0,1,4,8]],
"відкритий": [11,8,[5,9]],
"атрибут": [11],
"залиште": [10],
"unabridg": [0],
"відкриється": [11,[4,9],8],
"таблиць": [7],
"таблиця": [2,3,[9,11],1],
"продуктивність": [11],
"клікніть": [5],
"союзу": [[6,8]],
"вітербі": [11],
"таблиці": [11,[3,8]],
"перейменувати": [6,4],
"всередині": [5],
"optionsglossaryexactmatchcheckboxmenuitem": [3],
"майте": [11,6],
"рамках": [11],
"шматок": [[6,8]],
"елементах": [11],
"улюблений": [5],
"операційні": [5,7],
"однаковий": [11],
"комбінаціями": [3],
"учасники": [6],
"наступному": [5,[6,8]],
"обробить": [5],
"nnnn": [5],
"однакових": [11],
"сегментацією": [11],
"операційна": [11],
"переписується": [11],
"пропозицією": [8],
"myproject": [6],
"заповнення": [11,8],
"раніше": [6,11],
"відповідності": [[1,4]],
"крайніх": [11],
"zh_cn.tmx": [6],
"реалізаціями": [5],
"потрібен": [5,6],
"підтек": [10],
"обробляти": [11],
"рухатися": [8],
"стандартними": [[6,11]],
"прибравши": [11],
"файлового": [4],
"передати": [5],
"додайте": [6,[3,5,11]],
"archiv": [5],
"кількістю": [11,6],
"покинули": [8],
"стає": [8],
"repo_for_omegat_team_project.git": [6],
"унікальні": [9],
"викликається": [1],
"user": [5],
"зазнають": [6],
"кількість": [11,9,8,2,[1,4,5,6]],
"вимикати": [8],
"показані": [11],
"extens": [11],
"перейшли": [11],
"результатом": [11],
"обрав": [6],
"вибору": [11,5],
"розділяються": [[1,11]],
"спеціального": [[2,11]],
"кліком": [11],
"португальської": [5],
"замінюються": [6],
"запуску": [5],
"якому": [11,5,1],
"показано": [[1,11]],
"diff": [11],
"an": [2],
"editmultiplealtern": [3],
"зробити": [11,6,[4,5],[3,8]],
"git.code.sf.net": [5],
"кому": [2],
"знаходиться": [10,[5,11],[6,8,9]],
"наступну": [11],
"популярних": [[6,11]],
"прогрес": [9],
"комп": [5,11,8],
"стрілку": [11],
"найнижчий": [9],
"персональних": [5],
"кома": [2],
"filters.xml": [6,[10,11]],
"іноді": [11,[4,5]],
"br": [11,5],
"схитрувати": [6],
"завантаженій": [5],
"новіша": [8],
"оригіналом": [11],
"segmentation.conf": [6,[5,10,11]],
"дозволить": [[5,11]],
"коли": [11,6,5,[8,9],[1,10],4],
"дистрибутива": [5],
"ca": [5],
"контекстного": [8],
"cd": [5],
"ресурси": [6,11],
"ce": [5],
"öäüqwß": [11],
"абсолютно": [[6,11]],
"зафіксувати": [[6,8]],
"cn": [5],
"важливих": [6],
"конкретну": [11],
"cx": [2],
"більшій": [11],
"дозволити": [11,8],
"конкретне": [8],
"діапазон": [2],
"apach": [4,[6,11]],
"adjustedscor": [11],
"dd": [6],
"показати": [8,11,[5,10]],
"вільні": [0],
"всіма": [9],
"f1": [3],
"f2": [9,[5,11]],
"f3": [[3,8]],
"стандартну": [11,4],
"f5": [3],
"починають": [11],
"архів": [[0,5]],
"розширення": [1,11,0,7],
"поточного": [9,11,10,[1,3,6,8]],
"платформі": [5],
"зчитує": [11],
"dz": [0],
"editundomenuitem": [3],
"достатньо": [6,[8,10]],
"стандартне": [11,6],
"керувати": [11],
"користувацькими": [11],
"u000a": [2],
"вносили": [5],
"кодом": [4],
"китайського": [6],
"теґах": [11],
"відображатися": [8,9],
"включатимуться": [6],
"u000d": [2],
"u000c": [2],
"керуючи": [9],
"областях": [6],
"стандартом": [1],
"основному": [11],
"другому": [6],
"різними": [6],
"стандартні": [3,11,5],
"параметром": [11,5],
"клацність": [5],
"u001b": [2],
"stats.txt": [10],
"панелей": [5],
"exclud": [6],
"кольоровим": [8],
"for": [11],
"перекладом": [[6,8,9],[3,10,11]],
"запустити": [5,11],
"замінити": [11,8,3,9],
"fr": [5,[4,11]],
"випуску": [8],
"content": [5],
"замінить": [8],
"пошуковий": [11],
"applescript": [5],
"запустите": [5],
"програм": [[4,6,11]],
"довільна": [6],
"gb": [5],
"такого": [6],
"сірому": [8],
"class": [11],
"нежадібні": [[2,7]],
"helplogmenuitem": [3],
"мов": [6,11,[1,4,7]],
"пошукових": [11],
"відображуються": [11,1],
"порядком": [10],
"editoverwritetranslationmenuitem": [3],
"outputfilenam": [5],
"вільне": [8],
"макети": [11],
"aeiou": [2],
"шрифт": [11,3],
"zip-файл": [5],
"основи": [11],
"лише": [11,6,8,5,[0,1],[3,4,9,10]],
"обраного": [8],
"вільно": [6],
"завершите": [8],
"деякі": [11,6,[1,5,9]],
"непробільний": [2],
"hh": [6],
"виявитись": [6],
"працює": [8,[5,6,11],[0,2,4]],
"duser.languag": [5],
"згори": [11],
"зображено": [0],
"мої": [5],
"показники": [11],
"збережеться": [5,[6,8,11]],
"окремих": [6,[5,8,11]],
"file-target-encod": [11],
"context": [9],
"drag": [5,7],
"https": [6,5,[9,11]],
"id": [[6,11]],
"намагатися": [11],
"if": [11],
"project_stats.txt": [11],
"ocr": [6],
"зовнішньому": [1],
"projectaccesscurrenttargetdocumentmenuitem": [3],
"міститься": [[1,6]],
"хоча": [11,[1,6,10]],
"результат": [11,[8,9]],
"заміну": [11],
"in": [11],
"хоче": [6],
"termin": [5],
"ip": [5],
"is": [2],
"фіксацію": [6],
"мишки": [9],
"коду": [[4,5,11]],
"odf": [6,11],
"odg": [6],
"заміни": [[9,11]],
"коди": [4],
"ja": [5],
"одночасно": [11],
"попередження": [[5,9]],
"вказує": [5,11],
"заміна": [11,7],
"проксі-сервера": [5],
"odt": [6,11],
"виведеться": [8],
"gotonexttranslatedmenuitem": [3],
"тематики": [6],
"іде": [2,11],
"nplural": [11],
"дає": [11,9],
"js": [11],
"впливає": [[6,9,11]],
"витратили": [11],
"learned_words.txt": [10],
"проксі-сервері": [11,3],
"очистивши": [11],
"правописом": [8],
"найбільше": [9],
"неважливо": [11],
"перезапустіть": [3],
"генеральним": [8],
"ftl": [5],
"стандартна": [3,11,5],
"два": [11,5,6,[4,8,9,10]],
"вимагає": [[4,6]],
"затвердження": [11],
"viewdisplaymodificationinfoallradiobuttonmenuitem": [3],
"конфліктуватиме": [5],
"draw": [6],
"випадків": [11,4],
"оригінал": [8,11,[6,9],[1,10]],
"списках": [11],
"напрямками": [6],
"варіантів": [[8,9,10,11],4],
"джерел": [[6,11]],
"посеред": [6],
"dswing.aatext": [5],
"написані": [6],
"доповнення": [8],
"видалити": [11,6,10,5],
"регістр": [3,8,11,2],
"дві": [5,11],
"lu": [2],
"доступу": [11,[5,6]],
"повтором": [11],
"cycleswitchcasemenuitem": [3],
"файлових": [11],
"mb": [5],
"російськомовній": [5],
"me": [6],
"основного": [[9,10]],
"файловим": [11],
"omegat.png": [5],
"файловий": [11],
"надійна": [6],
"mm": [6],
"направо": [6],
"унікального": [3],
"entri": [11],
"мишею": [5],
"викликатися": [11],
"записаних": [1],
"mr": [11],
"клієнт": [6],
"відповідатимуть": [11],
"ms": [11],
"mt": [10],
"надійне": [6],
"марок": [11],
"my": [6],
"перемикає": [6],
"копіювання": [4],
"двічі": [5,[8,9,11]],
"змін": [11,[8,10],[3,5,6]],
"перевірити": [11,9,[5,6,8,10]],
"nl": [6],
"модифікатор": [3],
"nn": [6],
"no": [11],
"розбиватися": [11],
"code": [5],
"синхронізуйте": [10],
"репозиторій": [6],
"стоїть": [[1,8,11]],
"gotohistoryforwardmenuitem": [3],
"запаковані": [5],
"основної": [9],
"нечітким": [8],
"записана": [5],
"нечіткий": [11,[9,10]],
"of": [0],
"потрібною": [5],
"відслідковувати": [9,11],
"нечітких": [9,8,11,6],
"валідність": [1],
"потрібної": [6,[4,5,11]],
"ok": [8],
"стискати": [11],
"відбуваються": [11],
"or": [3,1],
"крапки": [11,2],
"репозиторії": [6],
"os": [[6,11]],
"розглядатися": [11],
"крапку": [[2,11]],
"динамічна": [11],
"трьох": [[6,10]],
"актуальний": [6],
"ближче": [11],
"ються": [11],
"алгоритм": [11],
"editinserttranslationmenuitem": [3],
"репозиторію": [6],
"вирівнювання": [11,6,[5,8],7],
"відображення": [11,6,9],
"базова": [5],
"відсотків": [10,9],
"натиснули": [8],
"крапка": [11,[2,5]],
"репозиторія": [[5,6]],
"нової": [[5,8,11]],
"особа": [11],
"po": [11,9,5],
"параметрах": [8],
"optionsglossarystemmingcheckboxmenuitem": [3],
"натиснувши": [11,[8,9]],
"pt": [5],
"сат-інструментів": [6],
"відчуваєте": [[6,11]],
"вказані": [11],
"оброблено": [5],
"перезавантажите": [6],
"розпакуйте": [5],
"знаходять": [11],
"ідентифікатором": [11],
"призначена": [6],
"проєктний": [6],
"вказану": [5],
"конфігурації": [11],
"визначають": [11],
"вказано": [[4,10]],
"оброблені": [[5,6]],
"порядковий": [11],
"тоді": [11,[5,6]],
"вперше": [[5,6]],
"вимикає": [8],
"якщо": [11,8,6,5,9,10,4,[1,3],0,2],
"edit": [8],
"editselectfuzzy5menuitem": [3],
"червоним": [11],
"непогана": [2],
"rc": [5],
"червоний": [10],
"includ": [6],
"темну": [11],
"див": [5,[6,11],[9,10],[2,4,8]],
"робота": [11,[3,6]],
"критерій": [11],
"вісімковим": [2],
"винятком": [2],
"оцінюватися": [10],
"вільної": [7],
"відмічений": [11,4],
"небажаних": [8],
"застосовуючи": [6],
"зазвичай": [11,5,[6,9,10]],
"sc": [2],
"зупинятися": [11],
"сегмента": [8,11,9,3,[1,6]],
"свої": [6,[2,11]],
"обидва": [[6,11]],
"того": [11,5,6,8,[9,10]],
"своє": [11],
"обмежень": [5],
"сегменти": [11,8,9,3,10,5,6],
"свою": [11],
"сегменту": [[1,3,8,10,11]],
"змісту": [11],
"порада": [5],
"скорочень": [11],
"роботі": [6,5],
"синхронізованих": [11],
"пункту": [3],
"схожості": [[9,11]],
"editoverwritesourcemenuitem": [3],
"жодні": [6],
"роботу": [6,11],
"enforc": [10],
"зменшити": [11],
"складатися": [1],
"перезавантажити": [8,3,11],
"remov": [5],
"tk": [11],
"роботи": [6,10,11,[2,5],[7,9]],
"більшості": [11,[3,5]],
"tm": [10,6,8,[5,7,9,11]],
"to": [5],
"v2": [5],
"сегменті": [9,1,[6,11],[5,8]],
"document.xx": [11],
"tw": [5],
"потрібний": [11,[1,6,8,9]],
"перемістити": [10],
"числами": [11],
"доступний": [[2,5]],
"англійську": [6,5],
"виберіть": [8,11,5,1],
"вказати": [5,11,8],
"доступним": [6],
"viewmarkautopopulatedcheckboxmenuitem": [3],
"projectwikiimportmenuitem": [3],
"користувачів": [6,[2,9]],
"countri": [5],
"виділити": [8,[4,5,9,11]],
"єднати": [[6,11]],
"пункти": [8],
"англійські": [6],
"доступному": [11],
"робочу": [5],
"скористайтеся": [[1,6,11]],
"частин": [9],
"this": [2],
"обидві": [6],
"створеного": [10],
"підтверджуєте": [8],
"кліку": [11],
"доступних": [11,[4,5,6,9]],
"vi": [5],
"для": [11,6,5,8,3,9,10,[2,4],1,[0,7]],
"надсилає": [5],
"частих": [5],
"вкаже": [6],
"кліки": [5],
"drop": [5,7],
"словник": [4,11,[0,7,9],8],
"стають": [8],
"типові": [11,1,[3,7]],
"uk.wikipedia.org": [9],
"голандську": [6],
"символах": [11],
"типово": [11,1,[2,5,6]],
"показувалась": [5],
"картинки": [6],
"журналу": [8],
"groovy.codehaus.org": [11],
"repo_for_omegat_team_project": [6],
"міняти": [6],
"backspac": [11],
"док": [5],
"виділить": [8],
"автотексту": [11],
"emac": [5],
"org": [6],
"способи": [5,[6,11]],
"distribut": [5],
"паролів": [11],
"типове": [1,6],
"робоча": [10],
"залишиться": [11],
"xf": [5],
"наб": [11],
"тематиці": [10],
"ході": [10],
"розрив": [11],
"абзаців": [11,8],
"xx": [5,9,11],
"допомога": [6],
"xy": [2],
"sourc": [6,11,10,[5,8],9],
"тему": [[6,11]],
"починаючи": [[6,8]],
"обробка": [11,[3,5]],
"теґом": [11],
"підготовлені": [11],
"безкоштовних": [[4,11]],
"type": [6,3],
"контекстному": [11,9],
"притаманна": [11],
"обробки": [11,8],
"теми": [11],
"над": [11,[5,6,8,10]],
"інколи": [[10,11]],
"toolssinglevalidatetagsmenuitem": [3],
"projectaccesssourcemenuitem": [3],
"yy": [11],
"гарантує": [[5,11]],
"міняйте": [10],
"оператори": [[2,7]],
"танці": [6],
"керування": [6,8,[3,7,9,11]],
"звернутися": [6],
"push": [6],
"внесіть": [10],
"оператора": [11],
"примітки": [9,8,[3,11],7],
"readme_tr.txt": [6],
"поєднує": [11],
"безкоштовний": [5],
"penalti": [10],
"стоять": [11],
"примітка": [[2,11],[6,10],[8,9]],
"повернутися": [9,11,8],
"комплекти": [11],
"довідка": [[3,7],8],
"активний": [11,8,[3,9]],
"служби": [11],
"напишете": [5],
"певним": [[8,11]],
"теку": [5,3,[4,6,8],11,10,9],
"utf8": [1,8],
"використати": [11,6,[5,9],[1,3]],
"обох": [6],
"екранованих": [2],
"раптом": [[4,6]],
"певних": [[6,11]],
"теки": [5,6,8,11,1,[9,10],[0,3]],
"символами": [11],
"ведеться": [8],
"ужиті": [11],
"вигляд": [9,3,11,[7,8],[1,6]],
"тека": [10,5,6,4,[0,1,7,9,11]],
"ключ-значення": [11],
"директорії": [5,11],
"dark": [11],
"обов": [11,[4,6,10]],
"power": [11],
"переліку": [11],
"неперекладеними": [11],
"tag-valid": [5],
"голандської": [6],
"файлу": [11,6,[8,10],[1,9],5,[0,4]],
"спробуйте": [11],
"хочете": [11,5,[3,6,8,10],9],
"файла": [6],
"сегментується": [11],
"пошуковій": [11],
"рядків": [11],
"файли": [11,6,5,10,8,[1,4],[0,9],[3,7]],
"u0009": [2],
"xhh": [2],
"стіл": [[5,11]],
"продовжуйте": [11],
"revis": [0],
"u0007": [2],
"repositori": [6,10],
"голандською": [6],
"областей": [9],
"вибраного": [11],
"зважаючи": [11],
"lowercasemenuitem": [3],
"wiki": [[0,9]],
"firefox": [[4,11]],
"артикль": [2],
"клавіатури": [9],
"вибір": [[5,11]],
"записом": [1],
"виглядатиме": [6],
"пробілом": [11],
"набір": [11,6],
"файлі": [11,8,10,6,[3,9]],
"другого": [9],
"клавішою": [5,[3,8,11]],
"nl-zh": [6],
"вимкнений": [11],
"нею": [[5,11]],
"діяти": [11],
"клавіатурі": [[3,9]],
"якій": [5,8],
"неї": [5,11],
"попередніми": [11],
"пошук": [11,8,1,2,[6,7]],
"отримувати": [[6,11]],
"перекладатимуться": [11],
"різні": [11,6,[9,10]],
"граматики": [11],
"openoffic": [4,11],
"зберігати": [11,[8,9]],
"всюди": [11],
"обмежена": [11],
"взагалі": [10,[9,11]],
"захоче": [11],
"сесії": [11],
"зовнішнього": [8,11],
"перекладеними": [3],
"optionsautocompletechartablemenuitem": [3],
"мова-країна": [11],
"якесь": [11],
"наступні": [11,5],
"нескладні": [6],
"сесію": [11],
"між": [11,6,8,9,[1,2]],
"якого": [[1,9],[3,8,11]],
"тією": [6],
"структурних": [11],
"кастильську": [4],
"wildcard": [6],
"виділили": [8],
"відмічена": [8],
"відправлених": [11],
"мертві": [6],
"git": [6,[5,10]],
"язувати": [8],
"натискання": [[9,11]],
"тієї": [[4,6,9]],
"натисканні": [8],
"ситуації": [6],
"відмічено": [11],
"xx-yy": [11],
"спроби": [5],
"вказали": [[4,11]],
"ситуацій": [10],
"синхронізуються": [[6,11]],
"вікіпедії": [8],
"тихий": [5],
"вікіпедію": [8],
"здатна": [11],
"прослідкувати": [11],
"optionsspellcheckmenuitem": [3],
"інтерпретації": [2],
"відмічені": [11],
"проєктного": [6],
"такому": [[6,11],[4,9]],
"третьою": [9],
"рушія": [8],
"optionssetupfilefiltersmenuitem": [3],
"проєктної": [10],
"доступної": [5],
"зливатися": [11],
"altgraph": [3],
"змінюючи": [5],
"день": [6],
"попросити": [6],
"необхідні": [5,[0,6,11]],
"очікуване": [[1,7]],
"ним": [[8,11],3],
"вкладку": [8],
"завжди": [11,1,[3,8,9]],
"запуститься": [5,11],
"xml": [11],
"функціональний": [11],
"окремими": [11],
"блоків": [[2,7]],
"них": [[6,11],9,5,[2,3,8,10]],
"прихована": [[10,11]],
"розташована": [[5,6]],
"необхідну": [6],
"xmx": [5],
"новішої": [5],
"відключена": [8],
"необхідно": [[3,5,8,10,11]],
"відновити": [11,9,3],
"befor": [5],
"util": [5],
"необхідна": [6],
"пункт": [3,8,[9,11],[1,5]],
"заповнені": [[3,8,11]],
"імовірно": [6],
"tar.bz": [0],
"називатися": [4],
"відкривають": [8],
"ресурсів": [[0,6,11]],
"нерозривні": [8,3],
"офіційний": [7],
"іншими": [5,[4,8,11]],
"іконки": [5],
"помістити": [11],
"інструментів": [[2,6]],
"сканованих": [6],
"іконку": [5],
"вкладки": [9],
"xlsx": [11],
"сегментації": [11,6,2,[8,10]],
"спеціальних": [6],
"слабкою": [11],
"обрати": [11,5,9,4],
"буферу": [8],
"іконка": [8],
"увімкнути": [11,[3,4,8]],
"відповідної": [6],
"наявний": [11],
"тематичну": [11],
"окрім": [2,[5,11],6],
"assembledist": [5],
"регулярному": [11],
"наявних": [1],
"збереженням": [11],
"невидимий": [11],
"спеціальним": [9],
"увімкнуті": [11],
"спеціальний": [11,5],
"документах": [11],
"проблема": [1],
"режим": [11,5],
"зміниться": [10],
"юнікоду": [[2,7]],
"сегментація": [11,[2,3,8]],
"приховані": [10],
"наявність": [5],
"опрацьовано": [11],
"реальних": [9],
"target.txt": [11],
"сегментацію": [11],
"проблеми": [8,[0,1,6,7]],
"перегенеровуються": [6],
"цільову": [4,5],
"схожого": [11],
"відповідають": [11,9],
"легко": [[6,10]],
"перетини": [11],
"капіталізація": [11],
"вибраний": [8,11,3],
"зберігали": [8],
"вибраним": [8,[3,11]],
"nameon": [11],
"будь-якого": [[4,9,11]],
"відповідає": [11,[2,5,8]],
"optionsglossarytbxdisplaycontextcheckboxmenuitem": [3],
"зареєстровані": [5],
"вибраних": [11],
"пізніше": [5],
"gotonextnotemenuitem": [3],
"змінами": [9],
"tar.gz": [5],
"gpl": [0],
"сусідні": [9],
"колонках": [[1,11]],
"процесом": [11],
"жодному": [11],
"вибрати": [11,3,8,5],
"знайоме": [11],
"двомовній": [6],
"azur": [5],
"відмінність": [[10,11]],
"цільова": [4],
"вручну": [11,6,[1,4,5,8]],
"поставити": [8,11],
"виявити": [10],
"підказки": [[3,4],[6,7]],
"відмінністю": [6],
"поставите": [5],
"місяць": [[5,6]],
"зору": [11,9],
"старому": [11],
"ефективність": [11],
"термінів": [1],
"збереженого": [8],
"відновить": [10],
"братися": [11],
"систем": [6,[5,11]],
"групи": [[6,9,11]],
"подальших": [[8,11]],
"йдеться": [5],
"додаткових": [6,[5,10]],
"групу": [[2,6]],
"компіляція": [5,7],
"with": [6],
"pdf": [6,[7,8,11]],
"описаний": [[1,11]],
"записуватимуться": [1],
"повинен": [[1,3,6]],
"дивись": [11],
"повернення": [2],
"будь-де": [5],
"специфічні": [11,10],
"toolsshowstatisticsmatchesmenuitem": [3],
"безпосередньо": [5,11,8],
"назначити": [9],
"допоміжної": [10],
"viewdisplaymodificationinfononeradiobuttonmenuitem": [3],
"показуватися": [11,[6,8]],
"показуватись": [6],
"закінчують": [11],
"склеюючи": [11],
"правопису": [4,[7,11],10,[1,2,3]],
"необхідним": [5],
"діятиме": [11],
"необхідний": [[5,10]],
"передумови": [5],
"спіймає": [2],
"малюнки": [11],
"довжину": [[5,9]],
"копіюється": [8],
"наявними": [6],
"оновлювати": [1],
"вибрані": [8],
"текст": [11,8,9,3,6,10,2],
"projectaccesswriteableglossarymenuitem": [3],
"зміненим": [3],
"величезну": [[4,11]],
"особливий": [6,11],
"тексти": [11],
"їхніх": [11],
"подальшої": [11],
"gui": [5],
"їхній": [[6,11]],
"тексту": [11,9,6,8,7,[1,4,10]],
"тому": [11,[5,6],[1,8]],
"вирахувано": [11],
"regexp": [5],
"полягає": [[1,11]],
"regexr": [[2,7]],
"sentencecasemenuitem": [3],
"скасувати": [[3,8]],
"лишатися": [[9,11]],
"стосуються": [[6,9,11]],
"uhhhh": [2],
"жовтим": [[8,9]],
"вибране": [11],
"зауважте": [6],
"слідує": [6],
"ютер": [11],
"директоратом": [8],
"лівій": [9],
"optionssentsegmenuitem": [3],
"перезавантажили": [1],
"перезаписана": [5],
"зовнішні": [[3,11]],
"optionsaccessconfigdirmenuitem": [3],
"зовнішня": [11],
"вміст": [[3,11],[6,10],[5,8],[0,9]],
"charact": [6],
"test.html": [5],
"зрозуміло": [10],
"xxx": [10],
"цей": [11,5,[6,8],[2,7,9,10],1],
"пропустивши": [8],
"розширеннями": [0],
"зрозуміли": [4],
"smalltalk": [11],
"взято": [9,[3,8]],
"інтерфейсом": [5],
"відповідник": [1,5],
"біля": [[4,11]],
"відповідний": [11,[8,9],[1,5]],
"усім": [5],
"назвою": [11,5,[0,9,10]],
"столі": [5],
"усіх": [11,5],
"сегментам": [11],
"грецького": [2],
"столу": [5],
"pseudotranslatetmx": [5],
"посібники": [6],
"сегментах": [11,6,[3,8,9]],
"табуляцією": [1],
"зрозуміти": [5],
"разом": [11],
"відомості": [11],
"вставлявся": [11],
"активного": [8,10],
"використаєте": [11],
"перевіряються": [8],
"targetlanguagecod": [11],
"дозвіл": [5],
"звукового": [2],
"складні": [2],
"розіб": [11],
"відповідних": [[5,6,11]],
"краще": [11,10],
"сервісів": [11],
"теці": [11,5,6,1,10,8,0],
"покинутих": [11],
"цих": [11,4,[9,10]],
"дій": [[4,5,6]],
"приватні": [11],
"тексті": [11,[1,4],[2,3]],
"дім": [11],
"отримуватимете": [6],
"більший": [[5,11]],
"відмітити": [11],
"очистіть": [9],
"розташовані": [[0,8]],
"цим": [[5,8],11],
"інтерфейси": [6],
"жодного": [2],
"можуть": [11,5,10,[6,8],[1,2,3,9]],
"другий": [10],
"зміните": [11],
"оновлено": [11],
"змінити": [11,5,[3,6],8,[1,9,10]],
"діє": [10],
"дії": [[6,8]],
"інтерфейсу": [5,[10,11]],
"промотати": [11],
"encyclopedia": [0],
"оновлень": [[8,11]],
"інтерфейсі": [6],
"змінить": [11],
"символ": [2,11,[3,5]],
"будь-якому": [11,[1,9]],
"дію": [8,3],
"пересилати": [6],
"декілька": [11],
"зміненої": [11],
"шаблони": [11],
"optionstagvalidationmenuitem": [3],
"використовують": [11,0],
"презентацій": [11],
"pt_br": [4,5],
"функцій": [8],
"створювати": [[1,8]],
"a-z": [2],
"терміналом": [5],
"вносяться": [11],
"запускатися": [5],
"програмі": [6,[4,5,8]],
"клавіатурною": [6],
"шаблону": [11],
"орієнтовані": [10],
"ліниві": [2,7],
"вставте": [[5,8]],
"функція": [8,11,5,[0,4,6,7]],
"функцію": [[4,11],10],
"т.ін": [11],
"шаблоні": [11],
"кореневої": [6],
"марки": [[9,11]],
"функції": [11,9],
"javascript": [11],
"термінального": [5],
"mediawiki": [11,[3,8]],
"програми": [5,[6,11],8],
"вставити": [8,[3,11],9,[1,6]],
"заново": [11],
"програму": [6,[3,5,8]],
"повторюється": [11],
"виразами": [2,7],
"програма": [5,11,[2,6]],
"враховується": [11],
"головного": [9,[3,11]],
"ятовувати": [11],
"виключно": [[4,6]],
"розпочався": [2],
"комбінацій": [3,7,2],
"шрифтів": [11,8],
"полі": [11,5,2],
"додалось": [11],
"комбінацію": [3,6],
"комбінація": [3,6,[5,11]],
"поля": [11,9,4],
"діють": [8],
"комбінації": [3,11,8],
"googl": [5,11],
"opendocu": [11],
"працюйте": [6],
"перетвореннями": [6],
"лишати": [11],
"розширити": [11],
"download.html": [5],
"змінила": [11],
"ярликів": [5],
"виділяється": [9,5],
"мишкою": [9],
"показував": [11],
"поле": [11,9,8,[2,4,5]],
"прописані": [8],
"використовується": [11,3,5,[4,6]],
"натиснута": [3,11],
"механізмом": [8],
"розмістіть": [[1,6]],
"sourceforg": [3,5],
"утиліту": [0],
"натиснути": [11,[8,9],4,6],
"шаблонах": [11],
"цього": [11,[6,9],5,8,[0,10]],
"поки": [6],
"editmultipledefault": [3],
"mozilla": [5],
"editfindinprojectmenuitem": [3],
"доку": [5],
"третій": [[1,10]],
"дещо": [6],
"доки": [[5,11]],
"warn": [5],
"підтвердження": [[10,11]],
"project_save.tmx.yyyymmddhhnn.bak": [6],
"technetwork": [5],
"коментар": [[1,5,8]],
"визначенням": [11],
"показуватиме": [11],
"розділені": [11,1],
"новим": [2],
"вмикайте": [11],
"plural": [11],
"новий": [6,11,[5,8],4,[1,3]],
"комою": [6],
"нових": [[1,8]],
"змінених": [9],
"результатів": [11,[5,8]],
"наповнюється": [[6,9]],
"знаходилися": [1],
"наступної": [[3,8,9]],
"напевне": [11],
"клікнути": [5],
"коректно": [[5,6]],
"windows": [7],
"первісному": [6],
"нижньому": [[2,3,9]],
"colour": [11],
"n.n_windows.ex": [5],
"символові": [11],
"разів": [2,[6,11]],
"скопіювати": [[4,6,8],[3,9,10,11]],
"необов": [8,[1,10]],
"бюро": [9],
"поза": [9],
"редактор": [11,5],
"файлові": [11],
"питала": [11],
"program": [5],
"імпортуватися": [6],
"разі": [11],
"комами": [11,1],
"єкти": [11],
"залогінитись": [5],
"документацію": [[2,11]],
"межах": [8,9],
"досвідчених": [2],
"підкреслене": [4],
"допоміжні": [6],
"згортає": [9],
"зсередини": [5],
"помилку": [6,[5,8]],
"аргументи": [5],
"запрошення": [6],
"головному": [11],
"вийти": [8,[3,9]],
"n.n_mac.zip": [5],
"замін": [8],
"показує": [[9,11],8],
"розташовувати": [11],
"домашній": [5],
"терміні": [1],
"рисунок": [4,[0,2]],
"специфічний": [[5,11]],
"клієнта": [[5,9,10,11]],
"перебирання": [8],
"специфічних": [8],
"деталі": [8],
"недавній": [[3,8]],
"структурувати": [10],
"легше": [11],
"помилки": [[5,6]],
"theme": [11],
"помилка": [[5,6]],
"неправильного": [11],
"оскільки": [11,5,9,[6,10]],
"терміни": [1,[3,11],9],
"маркер": [9],
"pseudotranslatetyp": [5],
"цифру": [2,11],
"терміна": [1,8],
"новоствореної": [11],
"перекладачів": [6],
"форматовані": [6],
"протоколом": [6],
"обробляється": [5],
"оболонок": [11],
"команд": [11],
"допоміжна": [9],
"цифра": [11],
"цифри": [[10,11]],
"десяти": [8],
"означає": [[9,11]],
"цьому": [5,9,[6,10],11],
"бекслеш": [5],
"аналіз": [8],
"лишити": [11],
"обрана": [5],
"тонка": [11],
"projectclosemenuitem": [3],
"свій": [5,[10,11]],
"експортується": [11],
"ніж": [[2,10]],
"viewmarknonuniquesegmentscheckboxmenuitem": [3],
"ігнорувати": [11,8],
"розміщувати": [6],
"використовували": [5],
"валюти": [2],
"єднання": [6],
"циклічного": [8],
"використовуючи": [11,[0,4,5,6]],
"перш": [4],
"унікальних": [11,8,9],
"findinprojectreuselastwindow": [3],
"чинності": [11,3],
"підтеку": [6,10,11],
"readme.txt": [6,[5,11]],
"тощо": [11,[2,6,10]],
"languagetool": [11,8],
"ній": [[6,10,11]],
"source.txt": [11],
"навіть": [11,[6,8],[5,9]],
"підтека": [[1,10]],
"files.s": [11],
"валідатор": [5],
"exchang": [1],
"підтеки": [[10,11],5],
"обрали": [[4,11]],
"поточному": [9,1,11,[3,8,10]],
"запускати": [5,8],
"релевантні": [11],
"яку": [11,6,4],
"currseg": [11],
"неоднозначна": [11],
"списком": [4],
"застосовані": [10,6],
"point": [11],
"допоможуть": [6],
"застосовано": [11],
"виключає": [6],
"часткового": [11],
"скопіювали": [6],
"яка": [11,[4,6,8,10]],
"перевіряти": [4,[5,11]],
"помилок": [8,6],
"унікальний": [11],
"засновані": [11],
"яке": [11,8,[1,6]],
"продуктів": [[6,11]],
"питань": [8],
"документації": [3,6],
"вставляється": [11],
"обрані": [[4,11]],
"внизу": [11,9,[3,8]],
"якісь": [5,11,6],
"редагування": [11,9,8,7,[3,6],[1,10]],
"внутрішня": [9],
"внутрішню": [8],
"знайденого": [11],
"колір": [10],
"downloaded_file.tar.gz": [5],
"цілих": [11],
"обрано": [8],
"які": [11,5,6,9,8,[0,2,3,4]],
"account": [[5,11]],
"dhttp.proxyhost": [5],
"мільйонів": [5],
"полегшення": [11],
"відкриттям": [6],
"чином": [5,11,6,[9,10],[4,8]],
"двомовний": [11],
"вирівнюються": [11],
"підтеці": [10,6,[0,1]],
"двобуквенний": [5],
"робіть": [6],
"коментарями": [11],
"язаний": [6],
"районі": [11],
"діалоговому": [11,[4,10]],
"доступ": [5,11,8,[0,9]],
"своїй": [5],
"шматки": [9],
"вийшла": [8],
"вище": [5,[4,6],[1,11]],
"порушить": [10],
"дають": [11],
"починати": [11,[6,8]],
"приблизну": [11],
"буквальних": [11],
"configur": [5],
"порадою": [6],
"своїм": [10],
"значними": [11],
"набравши": [5],
"підходить": [[8,9]],
"посунути": [11],
"може": [11,6,5,9,[1,10],8,2,3],
"оберете": [8],
"optionsworkflowmenuitem": [3],
"трьома": [6],
"розташування": [[1,5,6,8,9,11]],
"сховані": [11],
"назв": [11],
"releas": [6,3],
"видані": [0],
"текстового": [[5,8]],
"sparc": [5],
"слугувати": [6],
"середовище": [5],
"події": [3],
"середовища": [5],
"першому": [[5,6]],
"явиться": [11,[5,8]],
"законодавчої": [6],
"записуються": [11],
"завершення": [11,6],
"виділеному": [8],
"подія": [3],
"крок": [6,11],
"встановіть": [11],
"знижені": [10],
"внутрішньої": [9],
"перекладами": [6,11],
"подивитися": [[5,8,11]],
"розпакованій": [5],
"час": [11,6,5,1,[4,7,8,10]],
"фільтри": [11,8,[3,5,6,10]],
"підставити": [11],
"здійснюється": [[1,11]],
"subdir": [6],
"мети": [6],
"подібний": [10],
"фільтра": [11],
"такій": [11],
"шукаєте": [11],
"мета": [11],
"мові": [2],
"потрібними": [5],
"скопіюйте": [6,8,5],
"маркером": [9],
"використовувати": [5,11,[4,6],3,8,10,[7,9]],
"фільтрі": [11],
"мову": [5,4,6],
"екрані": [10],
"клієнтом": [[6,10]],
"мови": [11,4,5,6,8],
"корейською": [11],
"фільтру": [11],
"автодоповнення": [11,[3,8],1],
"мова": [11,[5,6],4],
"file-source-encod": [11],
"some": [6],
"заголовком": [11],
"детальних": [5],
"втрати": [6,7],
"ять": [6,11,10,[5,8]],
"описують": [9],
"перейменуванням": [6],
"зрозуміють": [6],
"канадської": [11],
"загальними": [11],
"рядках": [11],
"розгортанням": [9],
"editexportselectionmenuitem": [3],
"конфліктувати": [[8,11]],
"отримати": [5,[6,8,11]],
"додавався": [11],
"інтернет": [[5,11]],
"home": [6],
"єктної": [11],
"яті": [6,11,10,5,[8,9],7],
"знижується": [10],
"кожній": [11],
"projectaccesstargetmenuitem": [3],
"їхню": [11],
"згорнуті": [9],
"зручності": [5],
"ось": [5,[6,11]],
"сегментувати": [11],
"детальнішої": [8],
"отримали": [5],
"програмою": [6],
"закладки": [11,9],
"схожі": [9],
"розділяє": [11],
"їхнє": [11],
"надіслати": [6],
"їхні": [11],
"aligndir": [5],
"меню": [3,11,7,5,8,9,1,[4,6]],
"system-host-nam": [11],
"action": [8],
"менш": [10],
"внутрішній": [10],
"відповідній": [[6,11]],
"creat": [11],
"python": [11],
"es_mx.dic": [4],
"інструкціям": [6],
"infix": [6],
"авто-текст": [3],
"сервісах": [5],
"визначати": [11],
"результаті": [5,11],
"розбиті": [11],
"перекладеному": [11,6,9],
"tarbal": [0],
"здається": [[6,11]],
"підпадають": [6],
"вставки": [9],
"даних": [6,11,5,7],
"китайською": [[6,11]],
"розуміє": [6],
"користувачу": [[8,9,11]],
"поверх": [5],
"термінал": [5],
"першого": [11,[5,8]],
"утім": [4],
"окрему": [4],
"шрифтом": [11,[1,9]],
"машинний": [11,[8,9],7],
"китайської": [5],
"машинним": [[3,8]],
"жорстокого": [5],
"регулярні": [2,7,11,[3,4,6]],
"окремо": [11,3],
"отримана": [9],
"назвами": [[4,10]],
"приголосних": [2],
"результати": [11,[2,6]],
"користувачі": [5],
"інженер": [6],
"file": [[6,11],5],
"окреме": [9,8],
"окрема": [2],
"забули": [0],
"отримані": [11],
"користувача": [5,7,[6,11],[1,3,8]],
"menu": [9],
"вибірку": [4],
"оперативної": [5],
"попереджень": [5],
"бразильської": [5],
"цій": [10,[1,8]],
"a-za-z": [2,11],
"поставлений": [11],
"сат-інструментах": [6],
"локально": [8,6],
"сигналізувати": [11],
"доволі": [11],
"запускаєте": [[5,8]],
"працюєте": [11],
"source-pattern": [5],
"локальну": [6,[8,11]],
"плагіни": [11],
"типізацією": [11],
"синтаксис": [11,3],
"стягування": [6],
"створено": [[6,10]],
"найближчі": [11],
"підтримки": [6],
"серверів": [6],
"ними": [[6,8,11]],
"підтримка": [6,7],
"поступово": [[6,10]],
"враховувати": [11],
"true": [5],
"створені": [[8,10],[6,11]],
"перетворення": [6,11],
"відокремлює": [9],
"локальна": [6],
"groovi": [11],
"слів": [11,[1,8],[2,3]],
"наступний": [8,3,[2,11]],
"середовищем": [5],
"виводиться": [5],
"детальну": [[9,11]],
"kmenueditor": [5],
"наступних": [9],
"порт": [5],
"локальні": [6],
"межі": [[8,9],[2,7]],
"сконвертовані": [11],
"згідно": [11,5],
"чіткій": [6],
"master": [6],
"kmenuedit": [5],
"графічний": [5],
"більше": [11,2,5,[3,6]],
"xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx": [5],
"одразу": [10],
"межу": [[2,11]],
"rubi": [11],
"своєму": [5],
"обійти": [6],
"іспанську": [4],
"незмінний": [11],
"відповідні": [4,[3,6,8,10,11]],
"yyyi": [6],
"показується": [1],
"відповідно": [11,10,[4,6]],
"іспанська": [4],
"запропонує": [4],
"відповідну": [11,[0,5]],
"колекції": [11,9],
"пошкодження": [11],
"підсвічуються": [9],
"колекція": [9],
"читання": [6],
"виправлень": [4],
"відповідна": [[5,11]],
"перекладених": [11,6,[5,9],[3,8,10]],
"виправлені": [1],
"user.languag": [5],
"regex": [2],
"ваші": [[5,6]],
"відфільтрувати": [11],
"орфографічних": [4,7,11],
"meta": [3],
"keystrok": [3],
"носії": [6],
"спільнокореневі": [11],
"відділяйте": [6],
"орфографічний": [4],
"орфографічним": [8],
"єднувати": [11],
"вашу": [5],
"оригіналові": [8,3],
"боці": [6],
"літери": [[3,8],11],
"завеликий": [11],
"кількох": [11,[1,6,9]],
"початковому": [11],
"літеру": [2],
"фільтрами": [[5,6]],
"ваша": [5,3],
"переваги": [11],
"допоміжний": [6],
"допоміжних": [10,6],
"літера": [2],
"менше": [[5,9]],
"ibm": [5],
"журнал": [[3,8]],
"підкреслювалися": [1],
"віднімання": [11],
"закінчили": [6],
"безпеку": [11],
"режимі": [5,[6,9,11]],
"найвищий": [9],
"великий": [4],
"останньої": [8,5],
"задач": [5],
"сюди": [10],
"безпеки": [5],
"усталені": [11],
"пам": [6,11,10,5,8,9,7],
"позначкою": [10,11],
"спосіб": [5],
"взяли": [9],
"пар": [11],
"скрипта": [[5,8]],
"версіями": [6],
"схожий": [[6,11]],
"підвантажує": [11],
"схемою": [[2,11]],
"огляд": [5,4],
"статистикою": [8],
"кутку": [9],
"функціями": [11],
"послідовності": [11],
"задовгий": [11],
"синхронізовані": [6],
"напівжирним": [11,9,1],
"великих": [2],
"контроль": [[5,11]],
"когерентнішої": [11],
"ручні": [11],
"закривається": [6],
"контролю": [[6,10,11]],
"посегментно": [11],
"idx": [0],
"зустрічається": [4],
"кінець": [2],
"пошуковому": [11],
"відредагувавши": [6],
"користувач": [8],
"визначаються": [11],
"перекладеним": [11],
"зліва": [6,11],
"перекладений": [[6,11],[3,8]],
"projectaccesscurrentsourcedocumentmenuitem": [3],
"linux": [5,[2,7,9]],
"скрипти": [11,8,[5,7]],
"значної": [10],
"checkout": [6],
"режиму": [[5,6,9]],
"запускає": [[5,8]],
"напрацьована": [11],
"file.txt": [6],
"шрифтах": [8],
"шрифтам": [11],
"ifo": [0],
"застосовується": [11],
"дозволяють": [9],
"залишатися": [[10,11]],
"завантажується": [5],
"чинних": [11],
"xx.docx": [11],
"приймете": [5],
"чинний": [11],
"процес": [[5,6,11]],
"термінології": [[8,9],[1,11]],
"інстальованим": [5],
"сірим": [[8,11]],
"optionsautocompleteautotextmenuitem": [3],
"форму": [5],
"виключення": [11,6,2],
"термінологію": [[6,11]],
"термінологія": [11],
"простіше": [4],
"параметрами": [5],
"найбільший": [11],
"неперекладене": [11],
"залишений": [11],
"сховища": [6,11],
"крапкою": [[2,6,8]],
"справді": [6,11],
"сховище": [8,6],
"вигляду": [[9,11]],
"concis": [0],
"підкресленому": [1],
"customer-id": [5],
"мовній": [11],
"машини": [11],
"визначити": [11,4],
"формі": [11,[5,6]],
"генеруйте": [6],
"неперекладені": [11,8,10,[3,6,9]],
"term.tilde.com": [11],
"вигляді": [[6,9,11]],
"містять": [11,[1,9]],
"платформах": [1],
"теках": [[5,6,10]],
"виконує": [11],
"якість": [6],
"позначають": [11],
"виділеного": [8],
"viewmarknotedsegmentscheckboxmenuitem": [3],
"набули": [11,3],
"встановлені": [5,4,8],
"відвіданих": [8,3],
"кольорами": [8],
"посунули": [11],
"запуск": [5,7,11,8],
"собою": [2],
"gotomatchsourceseg": [3],
"єднаються": [11],
"optionssaveoptionsmenuitem": [3],
"excel": [11],
"встановлено": [11],
"понять": [9],
"передувати": [11],
"зловиться": [11],
"контекстне": [[1,11]],
"stardict": [0],
"практичні": [6,[0,7,10]],
"встановлена": [5,3],
"omegat.l4j.ini": [5],
"span": [11],
"лівою": [11],
"space": [11],
"проставлений": [11],
"діалог": [[8,11]],
"перекладачам": [6],
"open-sourc": [11],
"виникли": [6],
"детальної": [[6,10],[9,11]],
"thunderbird": [4,11],
"editselectfuzzy3menuitem": [3],
"серед": [11,[8,10]],
"продовжити": [11],
"закриваєте": [8],
"застосовувати": [11],
"використовує": [11,6,4,5],
"fals": [[5,11]],
"project.projectfil": [11],
"шукатиме": [11],
"погано": [11],
"завгодно": [10],
"ліцензії": [[5,8]],
"кінцеві": [11],
"вами": [[10,11]],
"завершений": [9],
"самими": [11],
"імпортуйте": [6],
"виходом": [8],
"сховищі": [6],
"боку": [11],
"shortcut": [3],
"знаєте": [11],
"дізнатися": [11,2],
"опція": [8,9],
"колег": [9],
"тільки": [11,6,[4,9]],
"запускається": [5,11],
"опцію": [8,[1,6,10,11]],
"водночас": [5],
"ніякий": [8],
"ліцензій": [0],
"pt_br.aff": [4],
"tmx2sourc": [6],
"опції": [11],
"ini": [5],
"постійно": [6],
"поставте": [11,8],
"обере": [5],
"пошукового": [11],
"іконок": [5],
"покращити": [11],
"dhttp.proxyport": [5],
"сховали": [11],
"кінцева": [6],
"ключові": [11],
"нотатки": [9],
"закінчити": [11],
"вставленому": [11],
"працював": [11],
"поставлене": [11],
"зусилля": [6],
"subrip": [5],
"часткових": [10],
"маючи": [9],
"пропускати": [11],
"виконати": [5,[6,11]],
"правою": [11,[5,9],8,[1,4]],
"кольорів": [[3,11]],
"score": [11],
"найважливіший": [[5,10]],
"заперечення": [2],
"звичний": [6],
"дробів": [11],
"головну": [[6,10]],
"спочатку": [11,5],
"одну": [[2,3,5,6,9]],
"пов": [[9,11]],
"експортує": [[6,8]],
"вставлених": [11],
"нарешті": [5],
"таким": [11,5,6,[9,10],4],
"вписані": [5],
"raw": [6],
"ловлять": [2],
"одно": [5,4],
"фіксуєте": [6],
"звичних": [9],
"таких": [[6,11],[0,3,9,10]],
"середня": [11],
"вставлений": [8],
"одна": [[1,2,5,6,9]],
"початково": [8],
"вибравши": [10],
"відображується": [11],
"оберіть": [11,[5,8],[4,6]],
"вхідний": [11],
"такий": [[10,11],[5,6,9],[3,4]],
"очікується": [5],
"aaa": [2],
"відображаються": [9,10,[1,5]],
"розкоментуйте": [5],
"contemporari": [0],
"solari": [5],
"шар": [6],
"показувати": [11,8,3],
"максимальна": [11],
"бажаєте": [11],
"обраній": [11],
"abc": [2],
"при": [6,11,5,[8,9,10]],
"варто": [11,6],
"точного": [11],
"про": [11,6,[3,5],8,9,[2,10]],
"письма": [6,8,3],
"клацніть": [5,1],
"використовуєте": [[3,6]],
"максимальну": [2],
"єднайтесь": [6],
"задоволені": [11],
"абзацу": [11],
"альтернативних": [8],
"iso": [1],
"звичайні": [8],
"реального": [9],
"isn": [2],
"мають": [11,6,[5,9],[1,2,4,8]],
"альтернативним": [9],
"блоку": [2],
"свого": [8],
"блокується": [5],
"словосполучення": [11],
"glossary.txt": [6,1],
"йому": [11],
"абзаци": [11,6],
"нижчою": [6],
"блоки": [11],
"add": [6],
"словником": [8],
"довідкових": [1],
"несуттєве": [11],
"правильній": [0],
"інтерфейс": [[1,5]],
"правопис": [4],
"звичайну": [11],
"перелік": [[2,6,11]],
"літер": [2],
"optionsautocompleteshowautomaticallyitem": [3],
"теґу": [11],
"звичайно": [[4,5,9,10]],
"ніяку": [5],
"прокручуєте": [11],
"спеціальні": [11],
"галочками": [11],
"larouss": [9],
"глосаріїв": [1,11,9,3],
"теґи": [11,6,8,3],
"один": [[8,11],2,[5,6,9],[0,1,10]],
"всіх": [11,8,6,[3,4,10]],
"себе": [11,6],
"untar": [0],
"проєкті": [11,6,9,[1,10],[2,3,8]],
"діалекту": [11],
"кшталт": [[0,5]],
"експортування": [6],
"всім": [11],
"теґа": [11],
"скісну": [5],
"дужках": [11],
"несегментованих": [11],
"filters.conf": [5],
"допустимо": [3],
"проєкту": [6,11,8,[3,10],1,9,[5,7],4,0],
"графічного": [[5,10]],
"додати": [11,6,5,3,[1,4,8,9,10]],
"проєкти": [6,[7,8]],
"головне": [11,[7,9]],
"класи": [[2,7]],
"налаштуйте": [11],
"перекладацький": [5],
"старий": [11],
"clone": [6],
"безліч": [6],
"класу": [11],
"вашій": [5,[3,4,11]],
"скористатися": [6,11,4],
"старих": [11],
"targetlanguag": [11],
"конкретного": [11],
"характерних": [5],
"операції": [11,[6,9]],
"насправді": [11,[4,5]],
"системах": [[3,5,10]],
"ввели": [11],
"properti": [[5,11]],
"editselectfuzzyprevmenuitem": [3],
"змушує": [5],
"консольний": [5],
"будь-який": [11,2,1],
"уперед-назад": [11],
"виключаються": [6],
"simpledateformat": [11],
"розділ": [8,5,[2,6,11]],
"знизу": [11],
"внесення": [11],
"списку": [11,[4,8]],
"script": [11],
"курсивом": [11],
"пересуватися": [8],
"коментарях": [11],
"system": [11],
"мегабайтах": [5],
"spellcheck": [4],
"графічному": [6],
"виділіть": [9],
"зворотному": [11],
"контролювати": [6],
"розпізнає": [1],
"одним": [[6,11]],
"local": [6,5],
"підібрати": [11],
"вищевказаних": [1],
"нижня": [9],
"зовнішній": [11],
"програмування": [11],
"додані": [[6,8]],
"мерріама-вебстера": [0,7],
"наліво": [6,7],
"орфографічного": [4],
"такими": [[6,11]],
"repo_for_all_omegat_team_project_sourc": [6],
"вікном": [11],
"експортом": [6],
"пізніх": [6],
"підменю": [5],
"додано": [[1,8]],
"відображати": [5],
"автотекст": [11],
"слідуйте": [6],
"сонячна": [11],
"посилання": [[6,11],2],
"зображенні": [4],
"пробілу": [11],
"es_mx.aff": [4],
"обмеження": [11],
"значних": [11],
"mode": [5],
"крім": [[6,9]],
"рекомендацій": [[6,11]],
"нерозривним": [11],
"локальному": [6],
"toolsshowstatisticsstandardmenuitem": [3],
"нерозривний": [11],
"варіант": [11,9,8,3],
"перетягувати": [9],
"відкриваєте": [6],
"сегмент": [11,8,9,10],
"read": [11],
"показували": [[8,11]],
"варіанти": [11,9,[2,5,6,7]],
"командами": [9],
"alt": [[3,5,11]],
"його": [11,6,5,[1,8],9,[0,10],[2,4]],
"недоступна": [5],
"існують": [8],
"охоче": [11],
"машинного": [[8,11],9],
"вверх": [11],
"зовнішніх": [11],
"пробіли": [11,8,3,2],
"інтерактивних": [2],
"видалилось": [11],
"вибирає": [11],
"рекомендації": [6,[0,7,10]],
"старту": [5],
"вставлено": [[8,10]],
"назад": [8,9,[0,1,2,3,4,5,6,10,11]],
"список": [11,[7,8],5],
"знаком": [1],
"недоступні": [[4,8]],
"підібрано": [9],
"поверніться": [11],
"and": [5,[6,7,11]],
"функціонує": [11],
"художнього": [11],
"альтернативною": [[6,8]],
"неперекладеного": [11,[3,8]],
"назву": [11,[1,3,4,5,6,8,9]],
"операцій": [6],
"ant": [[6,11]],
"посиланнями": [0],
"назва": [11,[5,6],[1,8]],
"helplastchangesmenuitem": [3],
"назви": [11,[4,6,9],10],
"операцію": [11],
"доречніше": [4],
"підійде": [5],
"зручному": [[4,6]],
"omegat.ex": [5],
"посортовані": [10],
"інформацію": [[9,11],[5,8]],
"сторінка": [11],
"інформація": [[3,6,11],[5,8]],
"щойно": [4],
"усунути": [6],
"sourcetext": [11],
"таку": [[6,11]],
"словниках": [8],
"перекладається": [10,9],
"сторінки": [8,[2,3,11]],
"коментарі": [11,9,[1,3,7]],
"певну": [[5,11]],
"німецької": [11],
"командна": [11,3],
"вирізати": [9],
"інкаше": [6],
"таки": [5],
"іспанської": [4],
"таке": [11,[3,6]],
"відображає": [9,[8,11]],
"абзац": [11],
"сторінку": [8,11,[3,6]],
"перекладачі": [6],
"english": [0],
"спершу": [[4,6]],
"jar": [5,6],
"певні": [11,[8,9]],
"api": [5,11],
"така": [11,6],
"інформації": [[6,11],[5,10],[8,9]],
"editselectfuzzy2menuitem": [3],
"зверніть": [5,[6,11],[8,10],[0,4,9]],
"курсор": [9,11],
"набагато": [10],
"видавати": [6],
"розміщена": [6],
"пріоритет": [11,8],
"адресою": [5],
"повернуться": [6],
"можна": [11,6,5,8,9,10,[1,3],4],
"виразів": [2,[5,7,9]],
"копіювати": [[4,9]],
"менеджер": [6],
"міститися": [1],
"вбудоване": [10],
"однокореневі": [8],
"натисніть": [11,5,8,9,6,4],
"перетворювати": [11],
"відрізнятися": [5],
"будь-якої": [9],
"близький": [6],
"діаграми": [11],
"візуально": [8],
"схеми": [11],
"перекладі": [11,6],
"розміщеній": [11],
"перекладеного": [11,8,3],
"налаштовувати": [[6,11]],
"editselectfuzzynextmenuitem": [3],
"вбудована": [[4,11]],
"створить": [5,11],
"скорочують": [11],
"логін": [11],
"редакторі": [[1,11]],
"повний": [[5,11],3],
"загалом": [[2,11]],
"read.m": [11],
"повних": [8,9],
"вказаній": [11],
"раз": [6,11,2,9],
"readme.bak": [6],
"упорядковані": [10],
"врахування": [2],
"швидкий": [11],
"знань": [6],
"позицію": [11],
"нулеві": [11],
"редактора": [[3,5,11]],
"art": [4],
"оброблятися": [11],
"командні": [6,[7,8]],
"перекладачу": [6,[9,11]],
"деталізовані": [5],
"певного": [[6,11]],
"процедурою": [6],
"jdk": [5],
"значенням": [2],
"перекладача": [6,9],
"змістом": [6],
"звані": [6],
"командою": [8],
"створити": [11,8,[3,5],6,9,[1,10]],
"підтверджувати": [[3,11]],
"певне": [6],
"головної": [5],
"імпортована": [9],
"сторінок": [6],
"звісно": [11],
"toolsshowstatisticsmatchesperfilemenuitem": [3],
"системне": [[10,11]],
"процедурі": [6],
"структурна": [11],
"run": [5],
"імпортовану": [6],
"характеристики": [8],
"підтвердіть": [5],
"titlecasemenuitem": [3],
"хвилини": [6,[8,11]],
"editcreateglossaryentrymenuitem": [3],
"створили": [4],
"позиції": [11],
"завантажувати": [11],
"автоматичним": [11],
"перекладе": [5],
"засобом": [6],
"переклади": [11,6,[8,9,10]],
"конвеєри": [11],
"пров": [11],
"думку": [9],
"скрипт": [11,5,8],
"ідентифікаційні": [11],
"кілька": [11,5,8,[1,6,9,10]],
"вказують": [11],
"перекладу": [11,6,8,9,10,3,5,[1,7]],
"параметр": [11,5,8],
"автор": [[8,9,11]],
"повторюються": [11],
"збережуться": [[5,10,11]],
"використайте": [6,5,10],
"зберігаються": [11,8],
"розміщені": [11,6],
"загальновживану": [11],
"будь-якою": [6],
"внести": [[6,11]],
"правильнішої": [11],
"виділено": [11],
"обмежений": [11],
"виділені": [[8,9,11]],
"зберігатимуться": [11],
"оброблятиме": [11],
"автоматичний": [8,11],
"виділень": [11],
"собі": [5],
"хоста": [11],
"імпортовані": [6],
"коренем": [6],
"target": [[8,11],10,7],
"процедура": [6],
"включає": [6,11],
"операційній": [[5,11]],
"установіть": [11],
"виділене": [8],
"плутати": [11],
"міститиме": [11],
"хвилинах": [11],
"збору": [6],
"розташований": [5],
"config-dir": [5],
"вищенаведених": [11],
"розділятися": [11],
"ліве": [6],
"опрацьовує": [11],
"пропустити": [11],
"врешті": [11],
"будь-яке": [9],
"під": [11,6,5,1,[0,7,10]],
"termbas": [1],
"будь-яка": [3],
"скриптів": [11,8],
"закрити": [11,8,3],
"натисненням": [1],
"ігнорується": [3],
"будь-яку": [[1,2,11]],
"зможе": [6,[9,10,11]],
"функцією": [11,[4,8]],
"термін": [1,8],
"зайвого": [11],
"порядку": [11,9],
"діалогового": [11],
"будь-які": [5],
"можливість": [11,[4,5]],
"стрічку": [11],
"ключів": [11],
"клавіатурна": [6],
"підтримувані": [5],
"традиційних": [5],
"європейського": [[6,8]],
"targettext": [11],
"виявлена": [6],
"відрізняється": [9],
"вікнах": [5],
"екранує": [2],
"стрічка": [11],
"мовами": [[1,6,11]],
"правій": [11],
"ефекту": [11],
"вказуючи": [11],
"стрічки": [11,6],
"жадібні": [2,7],
"сенс": [[4,8]],
"файлах": [11,8,[6,10],[1,3]],
"aaabbb": [2],
"додаючи": [5],
"структурою": [6],
"поточній": [11],
"зареєструвати": [[3,8]],
"edittagpaintermenuitem": [3],
"даного": [[5,6,8]],
"структурні": [11],
"вираз": [2,11],
"потреби": [5,[4,6]],
"optionscolorsselectionmenuitem": [3],
"неформатовані": [11],
"бачити": [[5,6]],
"прив": [6,8,5,[0,11]],
"типу": [11,1],
"бачите": [[5,9]],
"повної": [[6,11]],
"показуються": [[1,11]],
"такою": [[2,6]],
"контекстним": [8],
"оригіналу": [11,8,[9,10],6,[1,3],5],
"підтримує": [11,[1,6]],
"viewmarknbspcheckboxmenuitem": [3],
"справа": [6,11,[5,7]],
"типи": [11,[8,9]],
"оригінали": [[6,8]],
"загубилися": [9],
"також": [11,5,6,[8,9],4,[1,3,7,10]],
"справу": [6],
"позитивно": [11],
"фіолетовим": [8],
"переклали": [4],
"платформ": [5],
"панелі": [11,9,[5,7,8]],
"перекладацької": [11],
"інтервали": [11],
"користувацьким": [5],
"головним": [11],
"формати": [6,[0,8]],
"msgstr": [11],
"помилкові": [5],
"котрі": [[6,11]],
"доступитися": [5],
"змінюються": [5],
"сторінці": [5,11],
"формату": [[1,6,11]],
"оригіналі": [11,6],
"йдуть": [8],
"відповідного": [5,[10,11]],
"помилково": [11],
"модифікації": [11],
"знаку": [1],
"фоні": [9],
"omegat.project": [6,5,10,[7,9,11]],
"французьку": [5],
"відображуватися": [6],
"панель": [5],
"excludedfold": [6],
"targetcountrycod": [11],
"нижче": [[5,11],[0,2,3,10]],
"дозволяє": [11,[5,8],6,9],
"кращими": [11],
"webstart": [5],
"підрахунку": [11],
"точніше": [11],
"перекладати": [11,6,[5,8]],
"сусідніми": [11],
"спитає": [5],
"підтримують": [6],
"блакитним": [8],
"десяткових": [11],
"користуватися": [6],
"продовжує": [11],
"активований": [8],
"перевантажувати": [11],
"користуються": [11],
"історію": [8],
"по-різному": [8],
"форматі": [[10,11],[0,6,8]],
"розпізнаються": [1,11],
"історії": [8],
"чотирьох": [8],
"розширенням": [11,[0,6,9]],
"yandex": [5],
"виконуються": [[5,9,11]],
"знаходження": [[5,9]],
"a123456789b123456789c123456789d12345678": [5],
"viewmarkwhitespacecheckboxmenuitem": [3],
"описано": [3],
"додаються": [6],
"зміна": [11,[3,6,8]],
"забезпечити": [5],
"перевіряйте": [6],
"bak": [6,10],
"зміни": [[6,11],10,[3,5,8],[1,2]],
"табуляція": [11],
"експорту": [11,6],
"табуляцію": [11],
"сумісне": [5],
"вибираєте": [11],
"bat": [5],
"табуляції": [2],
"регулярним": [11],
"сеансами": [11],
"технічної": [11],
"додавання": [5,[1,6,10,11]],
"описані": [6],
"регулярний": [11,2],
"менеджера": [4,6],
"jre": [5],
"сегментів": [11,8,9,6,10,3],
"синхронізації": [6],
"optionsfontselectionmenuitem": [3],
"початку": [10,5,6],
"відображатимуться": [[8,11]],
"прийняла": [10],
"перезапустити": [11],
"неунікальні": [11,8,3],
"використанням": [11,5,[8,9]],
"вставляйте": [11],
"корисний": [5],
"регулярних": [2,11,[5,7]],
"виключаючи": [6],
"проєкт": [6,8,5,11,3,10,[1,7,9],[0,4]],
"пробіл": [11,2,1],
"звичному": [5],
"freebsd": [2],
"маленької": [11],
"delet": [11],
"projectaccessglossarymenuitem": [3],
"видаліть": [[6,9,10,11]],
"маленькою": [8],
"відображався": [6],
"щодо": [6,8],
"рахує": [11],
"внутрішньому": [11],
"входить": [11],
"developerwork": [5],
"огляду": [11],
"set": [5],
"диск": [8],
"переміщено": [9],
"нулю": [11],
"виправлених": [8],
"нуля": [11],
"дивіться": [[2,9,11]],
"нуль": [2,6],
"безумовно": [[4,10]],
"закінчується": [[5,9]],
"optionsrestoreguimenuitem": [3],
"стандартному": [8],
"зручніше": [11,[6,9]],
"написанні": [4],
"тимчасово": [11],
"найбільш": [9,[6,11]],
"надійних": [11,10],
"явилася": [8],
"offic": [11],
"оцінку": [[10,11]],
"виправлення": [11,6],
"технічні": [8],
"зміст": [[6,7,11]],
"текстова": [6],
"початок": [[2,11],[0,1,3,4,5,6,8,9,10]],
"repositories": [7],
"оцінка": [[10,11]],
"projectsavemenuitem": [3],
"проксі": [5,11],
"описати": [11],
"xmx6g": [5],
"текстове": [11,[2,6]],
"закінчуватися": [8],
"точної": [1],
"порядок": [11,8,[4,6,9]],
"заміняє": [11],
"проігнорувала": [10],
"вставилася": [8],
"рух": [11],
"згодом": [[6,9]],
"анотація": [7],
"ярлики": [5],
"контексту": [[8,11],[3,9]],
"виникнуть": [6],
"запросити": [6],
"втратили": [[6,9]],
"використаного": [11],
"читати": [6],
"перезавантажиться": [11],
"серйозними": [11],
"контексті": [6],
"обміну": [8],
"опенсорсні": [6],
"вжиті": [[3,11]],
"включаючи": [6],
"виглядає": [[3,5,9]],
"востаннє": [6],
"замінює": [8],
"комбінацією": [8,[6,11]],
"частіше": [6],
"виправити": [[8,11]],
"знайде": [6],
"порожніми": [[5,11]],
"одного": [11,[1,6,8]],
"відмітьте": [11,4],
"створюють": [6],
"експортуйте": [6],
"збіри": [1],
"момент": [10,[1,9,11]],
"сполучення": [1],
"виставляє": [5],
"язка": [6],
"відкритими": [11],
"текстові": [[6,11],1],
"відповідність": [11],
"швидкого": [5],
"ліворуч": [[8,11]],
"язки": [6,11],
"згенеруйте": [6],
"кроці": [6],
"написаних": [6],
"правильним": [11],
"навігацію": [11],
"знайти": [[5,11],8,3,[0,6]],
"правильний": [5,10],
"шляху": [6],
"пояснення": [5],
"bis": [2],
"створений": [6,[1,8]],
"projectopenmenuitem": [3],
"autom": [5],
"попередніх": [[6,8,9]],
"стандартного": [9,11],
"відновлює": [8,9],
"попередній": [8,[3,11]],
"toolsvalidatetagsmenuitem": [3],
"створених": [11,6],
"записувалась": [11],
"прогнозовані": [8],
"типами": [6],
"працювали": [[6,11]],
"сервіси": [5],
"скинути": [11],
"комбінаціях": [3],
"зате": [6],
"документ": [6,[3,8],[7,11],9],
"працювало": [11],
"надані": [11],
"viewmarktranslatedsegmentscheckboxmenuitem": [3],
"зможете": [5,[4,11]],
"теґами": [11,[6,8]],
"токенізатори": [11],
"європейських": [11],
"valu": [5],
"призвести": [11],
"цією": [[5,8,11]],
"каретки": [2],
"ilia": [5],
"самостійно": [11,4],
"актуальна": [[5,10]],
"цієї": [11,[6,8],[1,5]],
"аргументів": [5],
"провести": [6],
"кореневій": [6],
"корисною": [9],
"налашувати": [2],
"прапорець": [11,8,[4,5]],
"optic": [6],
"macos": [7],
"точний": [[10,11]],
"змініюйте": [11],
"перевірку": [11,4,7],
"номеру": [1],
"editselectfuzzy1menuitem": [3],
"текстовий": [6,[5,11]],
"потребують": [11],
"стаються": [5],
"неактивних": [8],
"пропускатиме": [11],
"включно": [11,[2,9]],
"незначні": [10,11],
"надрукуються": [5],
"моменти": [6],
"являються": [11],
"збігу": [11,[8,9]],
"одномовних": [11],
"hide": [11],
"перевірки": [4,11,[7,10]],
"приєднує": [9],
"адміністратором": [11],
"збіги": [11,9,6,[1,10],[7,8]],
"перевірка": [4,11,3,8,[1,2,6,7,10]],
"протилежному": [8],
"оригінальними": [11],
"язок": [6,11],
"auto": [10,[6,8],11],
"включити": [11,8],
"словників": [4,[0,7],[9,11]],
"поведінку": [[5,8,11]],
"document.xx.docx": [11],
"перелічені": [[2,11]],
"містить": [6,11,10,5,[1,8],[0,7]],
"співвідноситься": [6],
"поточної": [5],
"наскільки": [[10,11]],
"послідовність": [6],
"oracl": [5,3,11],
"друкує": [11],
"запустіть": [[5,10]],
"залежати": [6],
"текстових": [11],
"gradlew": [5],
"фільтрування": [11],
"змінені": [11],
"містити": [[1,5,6,11],[3,9]],
"щоб": [11,5,6,8,9,1,[4,10],[0,2,3]],
"якоїсь": [4],
"увімкнена": [9],
"позначати": [8,3,11,[1,9]],
"внесли": [9],
"після": [11,5,[1,6],2,3,[8,9,10]],
"синім": [[9,11]],
"цілком": [11],
"технічний": [11],
"великі": [[5,11]],
"незмінними": [[9,11]],
"праворуч": [[8,11]],
"невидиме": [11],
"працювати": [11,6,4],
"складається": [9,[3,6]],
"switch": [11],
"подвійного": [5],
"велику": [2],
"bundl": [[5,11]],
"зловить": [2],
"екранувати": [5],
"правильної": [5],
"змінює": [6,5],
"поведінка": [[3,5,11]],
"src": [6],
"control": [3],
"будь-якій": [10,5],
"велика": [2],
"менеджеру": [6],
"вилучити": [11,[3,4,8]],
"зручністю": [11],
"no-team": [[5,6]],
"повторень": [11],
"замінювати": [11],
"форматах": [11,[8,10]],
"такі": [11,6,[0,10]],
"одному": [11,6,10],
"існує": [10,1,[2,6,11]],
"валідується": [9],
"описана": [5],
"документів": [6,[9,11]],
"умови": [[5,10]],
"натиснете": [9],
"смуга": [11],
"полів": [[5,8]],
"вашою": [6],
"запиту": [11,5],
"environ": [5],
"відредагувати": [[1,9,11]],
"optionsautocompleteglossarymenuitem": [3],
"враховує": [11,2],
"запити": [11],
"шаблонів": [11],
"буквально": [4],
"сам": [1],
"години": [6],
"статистиці": [11],
"введення": [9,11,6],
"переходу": [[8,11],[3,9]],
"згенерувати": [6],
"неактивною": [8],
"запису": [1,3,11],
"kde": [5],
"інструмент": [11,5],
"записи": [8,[1,11]],
"багатомовні": [6],
"вашої": [5,4,[9,11]],
"перехід": [8,[3,7,9],11],
"збереженнями": [[6,8]],
"створення": [6,11,[1,5],3],
"languag": [5],
"цілі": [11],
"розпакувати": [0,5],
"ввійдуть": [11],
"або": [11,6,8,9,[1,2,4],3,10],
"стемінг": [9],
"зручно": [6,[5,11]],
"однак": [6,11,[0,1]],
"постобробки": [11],
"переході": [11],
"дата": [11],
"парі": [11],
"створенні": [6],
"key": [5,11],
"проводить": [11,8],
"приклад": [6,[2,5,11]],
"перезаписувати": [11],
"svg": [5],
"граматичних": [1],
"памʼяті": [9],
"пару": [[0,11]],
"svn": [6,10],
"натискайте": [11],
"операційних": [[5,10]],
"сервісами": [9],
"перезавантажується": [6],
"куди": [11],
"памʼять": [9],
"пари": [11,6],
"запиті": [8],
"editreplaceinprojectmenuitem": [3],
"пара": [9],
"причини": [[1,4]],
"експортований": [8],
"закріпити": [8],
"увазі": [11,[5,6]],
"express": [11],
"причину": [5],
"підбірка": [2],
"введених": [8],
"збігатися": [11],
"введений": [3],
"подільшому": [11],
"редагуванням": [5],
"зупиниться": [5],
"результатами": [10],
"gotoprevioussegmentmenuitem": [3],
"модифікувати": [6],
"розпізнавання": [6],
"виразах": [11],
"досягти": [5],
"здійснюватиметься": [1],
"gotopreviousnotemenuitem": [3],
"поточний": [8,9,3,11],
"користуйтеся": [6],
"довідкою": [6],
"словами": [11],
"поточним": [11],
"editredomenuitem": [3],
"uilayout.xml": [10],
"реальності": [5],
"подвійний": [5],
"російським": [5],
"локального": [6],
"явилися": [10],
"полях": [6],
"стирається": [8],
"вказаних": [5],
"невідповідний": [11],
"дані": [11,8,6],
"розробки": [2],
"визначтесь": [10],
"запише": [5],
"recognit": [6],
"почали": [[6,11]],
"друга": [[1,3,5,9]],
"оформлення": [11],
"перекладатися": [11],
"переданий": [5],
"вказаний": [[5,10]],
"врахуйте": [4],
"рік": [11,6],
"зразки": [11],
"належать": [11],
"інтервал": [11,[6,8]],
"runtim": [5],
"вмикати": [8],
"неможливо": [11],
"налаштування": [11,5,3,[7,8],6,4,2,10,1],
"опублікувати": [5],
"виразом": [11],
"далі": [11,[0,1,2,4,5,6,7,8,9,10]],
"читаються": [[1,10]],
"filenam": [11],
"вказаної": [[3,5]],
"забирає": [11],
"вистачати": [11],
"усіма": [[5,11]],
"gotosegmentmenuitem": [3],
"перетворені": [11],
"язати": [8],
"мусять": [11],
"виникають": [1,7],
"встановлювати": [5],
"імпортування": [6],
"кольором": [8,[9,11]],
"логічні": [[2,7]],
"xx_yy.tmx": [6],
"сегментом": [[8,9]],
"ваших": [9,[5,6]],
"максимум": [11],
"наявної": [6,[5,7,11]],
"скопійовані": [9,11],
"покидаєте": [11],
"електронні": [9],
"скопіює": [11],
"helpaboutmenuitem": [3],
"атрибути": [11],
"позначаються": [[1,11]],
"прекрасно": [6],
"додав": [9],
"верхньої": [11],
"жодних": [[0,6,10]],
"правил": [11,[2,6]],
"скопійовано": [9,11],
"перемістяться": [11],
"примітками": [8,[3,9]],
"російською": [5],
"перезавантажте": [11],
"намагається": [11,5],
"тестування": [2],
"метатеґів": [11],
"перемістіть": [5],
"пропозиції": [[3,8,11]],
"змінних": [11],
"обирайте": [11],
"залежить": [6,[1,5,8,11]],
"мережі": [6],
"називають": [5,11],
"виділений": [8,11],
"взявшись": [9],
"більш": [10,[5,6,11],9,2],
"місцях": [5],
"тобто": [[6,11],5,9,[0,10]],
"корені": [5],
"статей": [9],
"язані": [5,[0,8,9,11]],
"стосується": [5,[6,8,11]],
"tab": [3,[1,8,11],9],
"taa": [11,8],
"запропоновані": [9],
"перекласти": [[5,6,8,9]],
"вставляти": [11,8],
"перевірятися": [11],
"але": [6,[5,11],2,[4,9],[1,8,10]],
"позначити": [8,3],
"доцільно": [6],
"tar": [5],
"змусить": [11],
"способами": [[4,6,11]],
"projectreloadmenuitem": [3],
"зверху": [2],
"обраний": [8,9,11],
"почати": [11],
"налаштуваннях": [[6,11],[1,8]],
"збігаються": [9],
"додає": [6],
"копірайт": [8],
"виконанням": [8],
"моделі": [11],
"закриттям": [11],
"формах": [[1,11]],
"формат": [1,[6,7],[8,10,11]],
"зчитуються": [[5,6]],
"спільні": [11],
"операційної": [5,11,8],
"мексиканська": [4],
"змішаний": [6],
"зайдіть": [[0,6]],
"збільшить": [5],
"повинно": [1],
"опрацьовуються": [6],
"правилами": [[5,11]],
"статистика": [[3,10],8,6,[7,9]],
"winrar": [0],
"tbx": [1,11,3],
"умов": [6],
"обраних": [8],
"статистики": [6],
"відновиться": [6],
"повинна": [[3,4]],
"редагуєте": [9],
"завдяки": [10],
"вбудований": [11],
"статистику": [8],
"duser.countri": [5],
"вводяться": [6],
"початкові": [11],
"tcl": [11],
"залежності": [9],
"спільна": [6],
"пакетах": [5],
"показують": [11],
"readm": [11,5],
"зміною": [11],
"ввести": [11,[2,5]],
"повинні": [[4,5]],
"пакету": [5],
"збільшити": [11],
"змінні": [11,6],
"align.tmx": [5],
"вилучати": [11],
"file2": [6],
"зокрема": [[8,11]]
};
