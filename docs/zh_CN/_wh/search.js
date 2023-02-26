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
 "附录 A. 词典",
 "附录 B. 词汇表",
 "附录 D. 正则表达式",
 "附录 E. 快捷键自定义",
 "附录 C. 拼写检查",
 "安装和运行 OmegaT",
 "指南",
 "OmegaT 4.2 - 用户指南",
 "菜单",
 "窗格",
 "项目文件夹",
 "窗口与对话框"
];
wh.search_wordMap= {
"有时": [10],
"按下底部的": [11],
"或编辑器内的任何其他地方": [9],
"你可以在页面": [11],
"子菜单和": [5],
"拼写检查词典会正常运作": [4],
"info.plist": [5],
"当左键点击此数字时": [11],
"跳过": [11],
"如果译者试图修改它们": [6],
"要将脚本与数字关联": [8],
"fuzzi": [11],
"将需要翻译记忆的原文文档复制到项目的原文文件夹中": [6],
"是空的": [[6,11]],
"来禁止在打开时显示此窗口": [11],
"单词字符": [2],
"可以重用现有翻译": [6],
"因为所有单词都匹配上了": [9],
"主窗口由若干窗格": [9],
"电脑所使用的操作系统": [11],
"dgoogle.api.key": [5],
"当以控制台模式启动时": [5],
"项目菜单": [3,7],
"edittagnextmissedmenuitem": [3],
"菜单项代码": [3],
"告警": [2],
"匹配以下内容": [2],
"对话框的标题反映了实际使用的文件": [8],
"随后所输入的译文文本会视为此原文片段的一种替代译文": [9],
"quiet": [5],
"结果窗口以表格的形式布置": [8],
"如有必要": [11],
"还要根据设置窗口中的可用词汇表检查项目的译文语言代码": [4],
"安装到新的文件夹": [5],
"注意大小写": [5],
"处理为团队作业而设立的项目": [5],
"是一种强大的查找字符串实例的方法": [11],
"并在创建译文文档时不加修改地将它们复制到译文文件夹": [11],
"es_es.d": [4],
"勾选此选项可以用灰度显示所有非独特片段": [11],
"the": [5,[0,2]],
"将译文定义为空": [8],
"子文件夹": [5],
"projectimportmenuitem": [3],
"字符匹配零个或多个字符": [11],
"问题": [1,8],
"仅限未译片段": [11],
"imag": [5],
"以控制台模式启动": [5],
"你通常会在状态栏上看到的那些信息不会显示出来": [5],
"可以更容易发现问题": [6],
"恢复到取消上一个编辑操作之前的状态": [8],
"编辑菜单": [3,7],
"您可以将所选择的译文标记为默认译文": [8],
"实际使用的角度看": [9],
"在同一个弹出菜单中的": [11],
"窗格": [9,[7,10,11]],
"则对齐情况越好": [11],
"如果您系统中已经有合适的词典文件": [4],
"由语言定义": [6],
"用单选按钮选择方法": [11],
"翻译记忆库也可能包含参考译文": [6],
"向项目中添加文件": [11],
"moodlephp": [5],
"资源包": [5],
"currsegment.getsrctext": [11],
"文件依然会被导入": [6],
"实例中打开的项目会收到错误信息": [5],
"首选项文件夹": [3],
"您可能不想将所有片段发送给机器翻译引擎": [11],
"另一个控制台模式专用的命令行参数": [5],
"此类文件可以使用前文所述三种变体格式任意其一": [10],
"而只描述": [6],
"或者": [11],
"也可以通过双击": [5],
"check": [6],
"来保存更改": [5],
"在项目中途对": [6],
"xxxx9xxxx9xxxxxxxx9xxx99xxxxx9xx9xxxxxxxxxx": [5],
"只需解压": [5],
"原文文件名模式使用的通配符类似于": [11],
"告诉": [5],
"在接受了许可协议后": [5],
"可以创建一个包含命令行启动参数的脚本": [5],
"无论其是否已被翻译": [11],
"fr-fr": [4],
"存储库类型": [6],
"当插入原文文本时": [11],
"如果找到了合适的过滤器": [11],
"的最简单的方式是执行": [5],
"关闭查找窗口": [11],
"规则": [11],
"这对于包含许多商标或其他必须保持不变的专有名词的文本很有用": [11],
"那么其来源是内部项目翻译记忆库": [9],
"位于其中嵌套的文件夹下的词汇表也会被识别": [1],
"查找与替换只对翻译记忆库进行操作": [11],
"用户按下": [11],
"如上面的选择": [0],
"然后根据需要继续调整分割规则": [11],
"后模式": [11],
"文件则排除在外": [6],
"webster": [0,[7,9]],
"这将创建一个名为": [5],
"有两个菜单项允许直接打开当前的原文与译文文档以及可写词汇表": [8],
"如果要翻译的文件是树形结构的一部分": [10],
"当在从右向左和从左向右的语言间进行翻译时": [6],
"cjk": [11],
"左栏的数字指的是上图": [9],
"注意对于给定语言对只能使用一个": [6],
"然后点击确定来打开": [8],
"下一页": [[0,1,2,4,5,6,7,8,9,10,11]],
"下一个片段": [[3,8]],
"导出版本": [6],
"重复": [11],
"文件是不行的": [6],
"目录中在本地添加或修改的文件上传到存储库中": [8],
"您还可以在词汇表窗格中高亮标记想要用的条目并通过在选区上右键点击来将其插入到译文中": [9],
"empti": [5],
"恢复到执行上一个编辑操作之前的状态": [8],
"无论是显式定义还是隐含": [11],
"则亦可用": [8],
"或词汇表": [6],
"字典": [4],
"例如通过": [0],
"全文": [11],
"保留": [11],
"更新的自动通知": [11],
"tmx": [6,10,5,11,8,[3,9]],
"并且每条注释都单独占一行": [1],
"repo_for_all_omegat_team_project": [6],
"则仅当你在当前片段中按下": [11],
"查找并替换": [[3,8]],
"分割": [2],
"请参阅杂记中的字体设置部分": [11],
"如果勾选了此选项": [11],
"nl-en": [6],
"断句规则": [11],
"integ": [11],
"确保你有了免费的": [5],
"intel": [5,7],
"会按照": [10],
"我的密钥": [5],
"fr-ca": [11],
"过滤器已经可以处理": [11],
"mainmenushortcuts.properti": [3],
"边界匹配符": [[2,7]],
"在译文片段中查找": [11],
"左右的值会让命令显示在顶部": [11],
"无法重用此类信息来创建译文文件": [6],
"你需要用": [6],
"cmd": [[6,11]],
"coach": [2],
"即使其中的单词在原文文本中是分开出现的": [11],
"的官方用户指南": [7],
"或者如果您不喜欢渐进式加载过程中滚动条的行为": [11],
"使用通配符": [11],
"在查找字段输入字符串后": [11],
"gotohistorybackmenuitem": [3],
"它是动态": [11],
"安装和运行": [[5,7],8],
"如果在模糊匹配内选择了部分文本": [8],
"project-save.tmx": [6],
"以与常规的剪贴板行为一致": [8],
"项目是一个文件夹": [10],
"在片段的注释中查找": [11],
"以及是否要在桌面以及快速启动栏中创建快捷方式": [5],
"你可以用此文件夹和": [10],
"页面的内容": [8],
"光标解锁": [9],
"powerpc": [5],
"可以实现相同的效果": [11],
"换页": [2],
"完整的项目文件夹": [6],
"说明片段存在于默认项目翻译记忆库中但没有相对应的原文片段": [9],
"则该命令不起作用": [8],
"你可以通过点击": [11],
"恢复最大化之前的布局": [9],
"作为整体出现": [2],
"可选的还有": [5],
"允许译文与原文相同": [11],
"包含原文文本": [9],
"不过不再需要的时候请关掉它们": [11],
"文件的文件夹": [[5,11]],
"分割过程可描绘如下": [11],
"之后是词汇译文": [1],
"该方法对于找寻问题的原因有特别的意义": [5],
"则无需重新对其进行翻译": [6],
"你也可以用以下命令直接运行该应用程序": [5],
"匹配统计数据": [[3,8,10]],
"已打开或最小化": [9],
"以及": [[5,11]],
"就会显示俄语用户界面": [5],
"均由选定": [6],
"分割规则": [11],
"的对象模型": [11],
"要向参考词汇表中添加词汇": [1],
"omegat.sourceforge.io": [5],
"是此片段在项目中的编号": [9],
"来使用通配符": [11],
"在安装过程中": [5],
"如上所述": [6],
"光标锁定": [9],
"因此您可能需要从头开始翻译": [11],
"并不完全支持导入级别": [6],
"在备注中": [11],
"之后开始新片段": [11],
"以上半部分为参照": [9],
"例如在高度创造性或讲究文体的翻译中": [11],
"中特别标记的翻译可以用不同的颜色来显示": [8],
"参数会让": [5],
"选项已勾选就会显示颜色": [8],
"在翻译过程中维持原封不动": [11],
"的两个字典文件": [4],
"因此它在默认情况下是选中的": [11],
"用当前选中的模糊匹配": [8],
"扩展名与文件编码不一致": [1],
"translat": [11,5,4],
"的全局首选项中停用": [1],
"然后按需要导出为": [6],
"要自定义脚本功能": [11],
"并最终显示在拼写检查器窗口中": [4],
"请参考下文的": [5],
"的译文语言": [6],
"只考虑整数和简单的浮点数": [11],
"它们将出现在": [11],
"原文文件和译文文件的格式可以不同": [8],
"提供的语言检查器之外的语言检查器让你能够个性化验证规则": [11],
"它应当由项目经理": [6],
"某些情况下": [6],
"其中还包括安装说明": [7],
"请记住": [11],
"后向": [11],
"例如打算稍后再回到此片段重新做翻译": [9],
"包含": [[5,6,11]],
"您可以决定完全不翻译这类片段": [11],
"docs_devel": [5],
"名词集和斯洛文尼亚": [9],
"如果你取消此选项的勾选": [11],
"tsv": [1],
"并忽略标签和数字来计算所得到的百分比": [9],
"通过点击窗格并切换显示模式": [6],
"起始页": [[0,1,2,3,4,5,6,8,9,10,11]],
"gnome": [5],
"在上面的例子中": [[9,11]],
"当您为同一主题或者同一客户开始新项目时": [6],
"会将这些片段报告为未译": [11],
"退出": [[2,3,8]],
"则需要将它们全部合并到": [6],
"注释": [9,[7,8,11]],
"在俄语操作系统上": [5],
"按以下步骤操作": [4],
"扩展名为": [[0,10]],
"启用翻译提示": [3],
"将忽略它们": [5],
"另外": [[8,9]],
"地区": [5,11],
"因为其语言对可能与新语言不匹配": [11],
"重新加载项目": [8],
"的合适的翻译单元": [10],
"csv": [1,5],
"n.n_linux.tar.bz2": [5],
"删除非分割项目中开头和末尾的空白字符": [11],
"要翻译的文档也许包括了一些在译文文档中也要保持原封不动的商标": [11],
"在光标位置处只插入一个标签": [8],
"选中此复选框会让": [11],
"以便在各自的模式下查看原文并输入译文": [6],
"前也会自动保存": [8],
"文件后": [[3,6]],
"文件名": [11],
"press": [3],
"dock": [5],
"请遵循标签管理提示中给出的说明": [6],
"按空格键": [11],
"请按照所需安装包的说明进行操作": [5],
"后进行分割": [11],
"勾选此框可使得仅有未翻译的片段会被发送至机器翻译服务": [11],
"或者使用之前可能用过的术语": [6],
"可以在用户的主文件夹中找到": [5],
"dmicrosoft.api.client_secret": [5],
"对该命令的修改涉及到对其添加": [5],
"将其解压缩": [5],
"修复是微小的操作": [6],
"这对于包含商标": [9],
"功能": [9],
"在每次访问此项目时要求输入登录名和密码": [11],
"根据窗格的状态": [9],
"如果您只有标准西班牙语词典": [4],
"然后需要将其翻译为比如说中文": [6],
"你可以将这三个": [6],
"在您对新版本感到满意之前您可能希望这么做": [5],
"片段": [[8,9,11]],
"ctrl": [3,11,9,6,8,1,[0,10]],
"不翻译带有以下属性键值对的": [11],
"document": [[5,11]],
"标记自动填充片段": [[3,8,11]],
"复选框": [[4,11]],
"如果你对": [5],
"显示当前日志文件": [8],
"带图形界面": [5],
"你必须把它们复制过去": [5],
"中创建的译文文档的显示模式没有任何影响": [6],
"这也适用于": [9],
"resourc": [5],
"team": [6],
"xx_yy": [[6,11]],
"即使在文档中并未定义此选项也是如此": [11],
"docx": [[6,11],8],
"txt": [6,1,[9,11]],
"对于大多数项目而言": [11],
"服务器侧与译者侧": [6],
"来创建生成的翻译记忆库": [11],
"然后右键点击": [5],
"具体取决于你的系统": [5],
"可能会升级不使用语句分割的旧翻译记忆库": [11],
"进行手工修正": [11],
"对齐": [8],
"词库将下载为": [11],
"文件夹并重新载入项目以导入新文件": [11],
"这会以标准": [4],
"如果在编辑器窗格上右键单击": [9],
"如果点击": [11],
"都可": [6],
"理所当然": [5],
"source": [7],
"标记的": [11],
"您可以选择": [10],
"trnsl": [5],
"viewdisplaymodificationinfoselectedradiobuttonmenuitem": [3],
"index.html": [5],
"omegat.tmx": [6],
"文件夹下的文件": [10],
"有一个叫做": [11],
"使用团队项目": [6,[7,8]],
"在它们之间切换并插入最后一个选定的结果": [8],
"在后面跟着空格": [11],
"将新建立的": [6],
"的版本": [5],
"上的好事": [5],
"此文件夹一开始是空的": [10],
"导出的内容将被复制到位于用户偏好设置文件所在的文件夹内的": [8],
"diffrevers": [11],
"以下是一些限制选择的典型示例": [5],
"匹配度将根据文件夹的名称而降低": [10],
"否则": [6],
"第一列是词汇原文": [1],
"这和上面的示例大致上相同": [2],
"来直接启动它或者从命令行来直接启动它": [5],
"未翻译时": [11],
"字区": [2],
"机器翻译窗格在其打开时会包含机器翻译工具对当前片段的建议翻译": [9],
"除了首次出现外的所有非独特片段都会以灰度显示": [11],
"自动完成": [3,11,[1,8]],
"您可以选择要翻译的元素": [11],
"这样译者可以在后面检查带有此标签的片段是否已正确翻译": [10],
"打开终端": [5],
"光标沿着文本移动": [11],
"因此省掉了两次按键": [11],
"复制包含上述文档的翻译的翻译记忆库到新项目的": [6],
"等文件是可行的": [6],
"该子文件夹中还可能会创建其他文件": [10],
"project.gettranslationinfo": [11],
"下载": [5,[3,8]],
"取消勾选此选项可以显示所有标签": [11],
"允许项目专用过滤器": [11],
"或占位符": [11],
"安装拼写词典": [4,7],
"安装文件夹或平台特定的": [11],
"在加载翻译记忆库": [6],
"你需要用它才能获得并使用": [5],
"载入项目时会自动显示此窗口": [11],
"结束": [11],
"发送包含对网址的引用的": [6],
"start": [5,7],
"中所指定的命令": [11],
"而同时需要排除某些部分或者只要包含某些文件的翻译": [6],
"默认情况下会选中第一个匹配": [8],
"equal": [5],
"可以通过将": [5],
"值文本": [5],
"可连接": [6],
"这种修改对诸如": [6],
"上的跨平台版本": [5],
"会以日文用户界面启动": [5],
"通过": [11],
"菜单来访问": [3],
"optionsalwaysconfirmquitcheckboxmenuitem": [3],
"将原文文本分割为片段": [11],
"其显示方式将与原始文档相同": [6],
"例如改名为": [6],
"要分别修改一个或多个片段的位置": [11],
"如果没有显示文件名": [9],
"在创建新密码之前": [11],
"标记未译片段": [8,3],
"enter": [[3,8,11],5],
"拖进去的带有已知词汇表扩展名": [9],
"不过她精通英语": [6],
"结构": [2],
"有各种格式": [11],
"此页面让您可以为用户界面的各个部分选择不同的颜色": [11],
"applic": [5],
"前提是相似度高于在此对话框中设置的阈值": [11],
"projectteamnewmenuitem": [3],
"您可以编辑该文件来反映您的设置": [5],
"词汇表中只会显示一个条目": [11],
"的规则优先级应当设置为高于法语": [11],
"或者也可能是关键字表格": [11],
"将选中的文件复制到": [8],
"被设置为对某些文件格式使用强制编码": [11],
"默认情况下不显示此文件夹": [10],
"按钮来将编辑器窗口中显示的条目限制为当前查找所匹配的那些": [11],
"memori": [5],
"你可以更改每个不同的原文文件名模式的原文编码": [11],
"因此如果查找常见短语": [11],
"之后都会应用的外部后处理命令": [11],
"请检查": [6],
"这也能应用于": [8],
"不过": [5,6],
"omegat.jnlp": [5],
"分钟": [[6,11]],
"配置文件存放在哪个文件夹里": [5],
"温江": [5],
"每行最多包含一个快捷键的定义": [3],
"n.n_windows_without_jre.ex": [5],
"驾驭从右向左的语言": [6,7],
"在查找功能和片段分割规则中所使用的是": [2],
"保存或重新载入": [6],
"绘图和艺术字": [11],
"复制页面的网址": [8],
"字段中输入了": [11],
"prof": [11],
"现在还允许译文为空": [11],
"并按照规给定的优先级顺序应用": [11],
"未译片段将会标记为紫色": [8],
"选择第一个片段": [11],
"dmicrosoft.api.client_id": [5],
"所以只在要否决某些排除项的时候才需要指定本项内容": [6],
"希腊字母区中的字符": [2],
"来纠正": [11],
"一种基于原型的脚本语言": [11],
"config-fil": [5],
"因此依然不包含任何映射": [6],
"使用此选项能让": [5],
"进行若干次此类操作后": [11],
"快捷键设置": [3],
"国家对": [11],
"粘贴": [9],
"姓名或其他专有名词": [11],
"如果你需要更多详细信息": [2],
"然后导航至": [5],
"上面的流程是通常的情况": [6],
"启动器关联": [5],
"system-user-nam": [11],
"这可以通过两种方式实现": [5],
"检查未译片段": [6],
"章节": [[9,11]],
"如果未给出": [5],
"console.println": [11],
"仅为当前片段显示": [3],
"检查统计数据": [6],
"插入模糊匹配时尝试转换数字": [11],
"会先检查是否有更新": [5],
"作为译文文件的编码方案": [11],
"对齐器": [11,7],
"词汇表窗格": [9],
"平均得分": [11],
"但不匹配": [2],
"每当项目自动保存时": [6],
"通常应避免对分割规则进行重大更改": [11],
"中的文件": [5],
"等的出现之处": [11],
"平台": [5],
"所需的全部文件": [5],
"一开始似乎预期要": [5],
"进行压缩": [10],
"在此过程中": [11],
"可以访问项目的各个文件夹": [8],
"选择显示在模糊匹配窗格中的上一个": [8],
"project_files_show_on_load": [11],
"然后点击以下任一选项": [11],
"在翻译期间": [6],
"可以是键盘上的任何按键": [3],
"它们就会回复到原始状态": [6],
"要避免因为重用该项目而污染未来案例的可能": [6],
"数字": [2],
"ltr": [6],
"optionsexttmxmenuitem": [3],
"包含边界": [2],
"保存翻译并关闭项目": [8],
"错译片段之后可以手动用": [11],
"菜单中创建文件夹": [5],
"build": [5],
"的用法": [11],
"marketplac": [5],
"翻译的进行方式与非团队项目相同": [6],
"如果在项目开启状态下更改分割规则": [11],
"状态": [11],
"entries.s": [11],
"添加": [11],
"退出前确认": [[3,11]],
"del": [[9,11]],
"取消勾选此选项后": [11],
"gotonextuntranslatedmenuitem": [3],
"targetlocal": [11],
"path": [5],
"例如进行检查并在必要的情况下更正术语": [11],
"不显示词汇表词汇": [1],
"编辑行为": [[3,10]],
"该子文件夹一开始是空的": [10],
"它会根据项目的语言代码来使用正确的语言词典": [4],
"操作": [6],
"allsegments.tmx": [5],
"通常是最低的": [9],
"参见下面的": [5],
"标签是从左向右的字符串": [6],
"在断行": [11],
"会删除开头和末尾的空白字符": [11],
"helpcontentsmenuitem": [3],
"要选择分割的类型": [11],
"omegat-org": [6],
"词汇表文件为空": [1],
"设置为空译文": [[3,8,11]],
"此对话框让您能够指定要由过滤器处理的原文文件的文件名模式": [11],
"projectaccessdictionarymenuitem": [3],
"还有其他变体可用": [11],
"打开新窗口并在其中显示项目的统计数据": [8],
"在打开项目时会解析": [6],
"随时都可以用": [9],
"转换大小写为": [3,8],
"添加新词条": [11],
"不考虑": [11],
"启动": [5],
"并导航到": [5],
"尤其在完成初稿后": [11],
"词汇表必须位于": [1],
"新的规则集将与项目一起存储": [11],
"来达到相同结果": [5],
"要正确地读取原文中的标签并在译文中正确输入它们可能需要译者在从左向右和从右向左模式之间多次切换": [6],
"duden": [9],
"上面已经提到了基本的命令": [5],
"被拖到这个窗格中": [9],
"此数字显示在": [11],
"个模糊匹配来将其替换或插入到片段中": [8],
"比较模式下": [11],
"更具体地说": [5],
"spotlight": [5],
"did": [11],
"你一次可以退后一个片段": [8],
"标记": [11],
"已译文件的文件名和原文文件的文件名相同": [11],
"将把编辑字段留空": [11],
"按键快捷键": [3],
"dir": [5],
"这个包可以用在任何安装了": [5],
"子文件夹中的翻译记忆库的内容用于为待翻译的文本生成建议": [10],
"并定期生成译文文档": [6],
"该对话框允许您启用或禁用以下选项": [11],
"div": [11],
"则基于": [0],
"检查当前文档问题": [8],
"而非格式化信息": [6],
"除了选定上述方法之一": [11],
"viewfilelistmenuitem": [3],
"包含以": [5],
"命令之后需要执行的命令": [11],
"的文件夹": [5],
"的界面中尽可能少地显示标签": [6],
"通用首选项": [11,7],
"test": [5],
"每隔": [6],
"的文件夹中的任意翻译记忆库内匹配度为": [10],
"omegat": [5,6,11,8,10,[3,7],4,1,[0,9],2],
"文件来打开": [5],
"字区与分类类别": [[2,7]],
"除了": [[0,6]],
"没有必要从命令行启动": [5],
"安装新词典": [4],
"可以删除": [5],
"文件将被复制到": [9],
"脚本语言": [11],
"如下": [3],
"贪婪量词会尽可能多地匹配": [2],
"console-align": [5],
"如果有的话安装之": [5],
"此格式由欧盟翻译总局定义": [8],
"个文件": [5],
"ms-dos": [5],
"以下方案只是其中一种方法": [6],
"projectopenrecentmenuitem": [3],
"在团队项目中": [10],
"此命令最基本的形式是": [5],
"则可以用": [8],
"服务器上": [11],
"页面底部": [3],
"创建新项目并提交到存储库时": [6],
"如果您已安装的程序是": [5],
"以在输入第一个字母后就立即显示建议单词": [8],
"忽略大小写不同的命中": [11],
"状态栏": [9,7],
"上一备注": [9],
"可以在项目属性对话框中更改其名称和位置": [1],
"标记空白字符": [[3,8]],
"客户端对远程文件进行更改": [10],
"创建远程": [8],
"und": [4],
"参数是添加开头的": [5],
"例如包含原文文件的子文件夹": [11],
"之一来匹配其自身时就必须这样": [2],
"project_save.tmx.temporari": [6],
"正则表达式工具和使用实例": [2,7],
"右对齐": [6],
"要能识别其格式": [11],
"但它并不会将其包含在": [11],
"可以访问最后编辑的十个项目": [8],
"如果系统上有其他应用程序使用它们": [4],
"中的此文件并选择你要使用的文本编辑器": [5],
"editoverwritemachinetranslationmenuitem": [3],
"制作人员和授权信息": [8],
"ingreek": [2],
"从网站映射其他原文文件": [6],
"在每次离开片段时": [11],
"结尾的包": [5],
"创建的译文文档位于": [8],
"若已存在翻译则打印出片段的原文和译文": [11],
"不太走运": [11],
"es_es.aff": [4],
"的文本导出功能": [11],
"projectexitmenuitem": [3],
"已译或未译": [11],
"请勾选": [8],
"改进建议": [8],
"排除": [[6,11]],
"editregisteruntranslatedmenuitem": [3],
"每个文件的匹配统计数据": [[3,8]],
"init": [6],
"定义项目专用的外部搜索源": [11],
"标签中": [6],
"存储库中的所有更改都会被复制到本地项目中": [6],
"maco": [5,1],
"导出所选部分": [[3,8,11]],
"最顶上的匹配项是": [9],
"文件并不会自动更改": [6],
"doc": [6],
"通常需要输入两条命令": [5],
"创建翻译记忆库时": [11],
"匹配": [[1,11]],
"结构级分割": [11],
"任何已翻译并存储在这些文件中的文本只要与当前正在翻译的文本足够相似就会出现在模糊匹配中": [10],
"要做到这一点": [11],
"paramet": [5],
"因此如果要编辑此字段": [11],
"mac": [3,[5,6]],
"一旦项目在服务器上建立好之后": [6],
"自动文本": [[3,11]],
"如果在": [8],
"因此只显示第一个独特片段": [11],
"译文文档的显示方向需要手工在查看它的应用程序中改为从右向左": [6],
"对应于": [2],
"可以试着改变参数": [11],
"在译文中": [11],
"如果选中": [11],
"man": [5],
"对于只包含此类不可变文本的片段可以采取两种策略": [11],
"map": [6],
"将文件导入到": [6],
"上下文菜单": [1],
"may": [11],
"无需犹豫": [6],
"保存和输出": [11,[3,6,8]],
"插件": [11],
"要想以上面所举所有例子启动": [5],
"允许标签编辑": [11],
"url": [6,11],
"选择第二个匹配": [3],
"处理原文文件的方式": [11],
"到菜单中": [5],
"如果无法正常运行": [5],
"如果你确信某个": [10],
"uppercasemenuitem": [3],
"viewmarkuntranslatedsegmentscheckboxmenuitem": [3],
"您可以将这些翻译记忆库用作": [6],
"但没有": [4],
"显示包内容": [5],
"请取消对此选项的勾选": [11],
"中的词典基于": [0],
"这三个匹配度百分比按照以下顺序排列": [9],
"对于托管在外部服务器上的团队项目使用较长的间隔": [11],
"例如用": [5],
"use": [5],
"名称或其他专有名词": [9],
"方向键来选择文件": [11],
"非贪婪量词会尽可能少地匹配": [2],
"标准高": [6],
"omegat.jar": [5,[6,11]],
"请按与操作系统关联的热键": [1],
"omegat.app": [5],
"usr": [5],
"可以通过在": [11],
"应当分割为两个片段": [11],
"文件和": [11],
"如果要还原为过滤器的默认配置": [11],
"条目不是以制表符分隔的": [1],
"第一列": [11],
"的定义是区分大小写的": [11],
"编码保存的": [1],
"其译文文本在翻译过程中会自动用于后继命中": [11],
"这是字母": [6],
"依然可以通过使用": [11],
"前一种情况下": [6],
"utf": [1],
"模式将会匹配任何扩展名为": [11],
"启动命令参数": [5],
"此选项对某些中文": [11],
"举例说": [5],
"不会显示在编辑器窗格中": [11],
"有和没有字母是大写的": [11],
"仅为本次会话保存": [11],
"打开查找窗口": [11],
"将自动选择第一个模糊匹配": [9],
"根目录": [3],
"可以按": [11],
"词干分析": [11],
"见上图": [4],
"dsl": [0],
"当前文件": [9],
"原文": [[0,6]],
"完成此操作后": [5],
"要编辑或扩展现有的规则集": [11],
"该选项允许您直接输入翻译而无需删除原文文本": [11],
"的脚本来操纵控制": [11],
"类型的词典之外": [0],
"n.n_windows_without_jre.zip": [5],
"本地文件映射": [6],
"med": [8],
"在这里你可以选择不同的子文件夹": [11],
"en.wikipedia.org": [9],
"回复到原始布局": [9],
"dtd": [5],
"用命令行启动": [5],
"的话": [5],
"容易操作且没有危险性": [6],
"make": [11],
"中的标签": [11],
"将其重命名为": [6],
"所有有着相应词条的词汇都会被自动翻译": [11],
"编辑项目": [6],
"projectcompilemenuitem": [3],
"console-transl": [5],
"视图": [[8,11],[1,3,6,7]],
"则这些重复项不会出现在编辑器中": [11],
"顶上的条目": [2],
"程序": [5],
"大写字母": [2],
"和单词": [11],
"地区代码": [11],
"gotonextuniquemenuitem": [3],
"optionsviewoptionsmenuitem": [3],
"删除文件": [6],
"的功能": [9],
"commit": [6],
"targetlocalelcid": [11],
"这样项目经理或其他译者就可以查看并使用在此期间添加的翻译": [6],
"可以从主窗口的任何地方拖动文本并将其放到该片段中": [9],
"来组合这两个指令": [11],
"代理主机端口": [5],
"描述": [5],
"project_stats_match.txt": [10],
"请勾选此选项": [11,8],
"如果存在的话": [11],
"取消注释": [5],
"将插入与当前原文最相似的字符串的译文": [11],
"dvd": [6],
"并指定设置": [5],
"段落分割在某些情况下是有利的": [11],
"它将使用相同的设置": [5],
"一旦安装好": [5],
"xmx2048m": [5],
"地址": [5],
"文本会根据此处所选择的选项进行进一步分割": [11],
"显示原文和匹配之间差异的字符串": [11],
"标记格式为": [9],
"xxxxxxxxxxxxxxxx.xxxxxxxxxxxxxxxx.xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx": [5],
"控制台模式的目的是在脚本环境中将": [5],
"包括有": [11],
"且希望升级到更新版本": [5],
"作为译文语言": [4],
"目录": [7],
"就必须使用普通安装": [5],
"但项目经理并不会将": [6],
"第二个数字": [9],
"则该条目将为灰色": [8],
"提供了用于": [5],
"可以同时打开多个查找窗口": [11],
"原始文件的扩展名": [11],
"模糊匹配和词汇表": [6],
"查找方法和选项": [11],
"中的项目翻译记忆库所体现的那样": [10],
"因为你可以在启动时将其添加到命令行": [5],
"查找选项": [11],
"程序会先检测用户的操作系统的语言": [5],
"krunner": [5],
"libreoffic": [4,[6,11]],
"在初始安装结束后": [5],
"在可用格式中可能无法显示此文件图标": [5],
"选择第一个匹配": [3],
"文件夹插入的翻译会显示为橙色": [8],
"当前片段中没有词汇和与词汇表中的词汇相匹配": [1],
"将指示错误在问题文件中的位置": [6],
"每当完成一个项目": [6],
"开放文件格式": [11],
"稍停一会来下载正确版本的拼写检查器更为理智": [4],
"在关闭项目或退出": [8],
"而在片段内部拖来的文本则会移动过来": [9],
"建立团队项目需要一些关于服务器和": [6],
"浏览": [5,11],
"但其扩展名必须是": [1],
"如果译文语言是巴西葡萄牙语": [4],
"打开查找并替换窗口": [11],
"要访问此窗口": [11],
"如果您认为丢失了翻译数据": [6],
"必须重新启动": [3],
"个条目": [11],
"而第三列则是注释": [1],
"在本地存储库中建立一个新的": [6],
"已知一些工具会在某些条件下生成无效的": [6],
"viewdisplaysegmentsourcecheckboxmenuitem": [3],
"字段中输入": [5],
"editregisteremptymenuitem": [3],
"在下一个窗口中": [5],
"open": [11],
"www.oracle.com": [5],
"选择最有可能包含所需数据的翻译记忆库备份": [6],
"将其内容提取到项目文件夹": [0],
"语句级分割是分割原文的主要方式": [11],
"之外的": [6],
"project": [5,11],
"删除原文文件": [6],
"xmx1024m": [5],
"它们大致上遵循相同的模式": [2],
"使用分开出现在原文文本中的词条": [[3,11]],
"提交原文文件": [6,8],
"如果勾选": [8,11],
"代码片段将扫描当前项目中所有文件内的所有片段": [11],
"如果词汇表中显示了过多误报": [11],
"penalty-xxx": [10],
"请参阅": [[10,11],[2,9]],
"gotonextsegmentmenuitem": [3],
"当前版本的源码可以用": [5],
"逻辑": [11],
"文件过滤器类型": [11],
"则不会加载项目": [6],
"这样只有其名称显示在窗口底部": [9],
"无论是否已翻译": [10],
"nnn.nnn.nnn.nnn": [5],
"文件中的某个片段": [11],
"撤消前一操作": [[3,8]],
"请按照系统说明在你觉得方便的地方安装": [5],
"abort": [5],
"因此将手头各种翻译记忆库全放进此文件夹可能会不必要地拖慢": [6],
"用户界面语言": [5],
"去哪找要翻译的项目": [5],
"如果拼写检查器未能正常工作": [4],
"的原文语言当然应当是相同的": [6],
"字符类": [[2,7]],
"子文件夹内至少会包含一个文件": [10],
"访问项目内容": [3,8],
"启用": [11],
"新项目": [5],
"编辑过滤器对话框": [11],
"进入下一步": [11],
"自定义标签": [8],
"要以控制台模式启动": [5],
"插图清单": [7],
"双击某个片段可在编辑器中将其打开以进行修改": [11],
"es-mx": [4],
"但不会找到": [11],
"获取": [5,[3,11]],
"点击一列的标题会更改此列的排序顺序": [8],
"在非分割项目中": [11],
"stem": [9],
"打开": [8,3],
"但模糊匹配的质量会有所下降": [6],
"下一个未译片段": [[3,8]],
"将会显示原文片段并将其标记为绿色": [8],
"英语": [2],
"参数可以进行组合": [5],
"尽管": [11],
"保存": [[3,5,8,11]],
"译文文件名模式使用特殊的语法": [11],
"如果你忘记了对用户界面做过的更改": [9],
"无论是精确查找还是关键字查找": [11],
"通过就可以为译者提供要翻译片段的相关信息": [9],
"包括标签和数字": [11],
"过滤器用于处理扩展名为": [11],
"而不是": [2],
"前进": [8,[3,11]],
"以下示例展示了韦氏词典第十版的": [0],
"来自": [3],
"将显示修订选项设置为": [8],
"如果您想要让译文与原文相同": [9],
"项目会在本地被转换为团队项目并且会添加默认映射": [6],
"同步文件": [6],
"标记不换行空格": [[3,8]],
"标签处理": [11,3],
"word": [6,11],
"会将出现在项目的": [5],
"这样你就可以同时保留两个版本": [5],
"时会被覆盖": [11],
"在片段中混合从右向左和从左向右字符串": [6],
"对于上面这个例子": [11],
"才能使用": [5],
"更多细节请参阅": [9],
"不同模式之间用分号分隔": [6],
"原文和译文都以从右向左模式显示": [6],
"同时未翻译的文本以原始语言显示": [9],
"必须先安装适合的词典": [4],
"模式区分大小写": [2],
"任何不在希腊字母区中的字符": [2],
"然后在打开的窗口中激活": [11],
"窗格之间的分隔线可以拖动以调整窗格大小": [9],
"用导航按钮找到": [5],
"两种方式任选其一": [6],
"lingvo": [0],
"取决于操作系统": [8],
"你可以选择是否要让": [5],
"所有其他的文件都是只读的": [6],
"第一条是": [5],
"您也可以指示拼写检查器忽略这个拼写错误词汇所有出现之处": [4],
"mrs": [11],
"格式所必需的文件": [0],
"自动替换头中的": [11],
"的平台上": [5],
"只要在顶部的表格中点击它即可": [11],
"子文件夹包含要翻译的文件": [10],
"名称": [5],
"可以将查找应用到编辑器上": [11],
"你需要用它来登录": [5],
"创建并打开新项目": [8],
"pt_pt.aff": [4],
"重命名为": [4],
"的规则": [11],
"来添加子菜单": [5],
"或者添加映射来从其他位置同步文件": [6],
"参见以下的参考和范例": [2],
"html": [11,5],
"并且对于给定": [10],
"子文件夹中的翻译记忆库中包含多个原文文本完全相同但译文不同的片段": [10],
"如果原文文档包含用于控制布局的不可忽略的重要空格": [11],
"添加到菜单": [5],
"参见标签验证": [6],
"正如保存在": [10],
"您只需将对应的两个文件复制并重命名即可": [4],
"实例": [5],
"基于规则的分割": [11],
"artund": [4],
"直接启动它": [5],
"意味着任意空白字符": [11],
"使用操作系统的默认编码来处理文本文件": [11],
"匹配下列内容": [2],
"本地化文件中的占位符": [6],
"许多语言都已经有了可靠的分割规则": [11],
"新添加的词汇立即就会被识别生效": [1],
"不含": [5],
"未译片段的编辑区域可能为空": [9],
"片段分割规则": [11],
"以使译文文件继承此类参数": [6],
"移动到下一个已译片段": [8],
"句子式大小写": [3],
"你必须点击": [11],
"可能会有好处": [11],
"www.ibm.com": [5],
"请按以下步骤操作": [0],
"则可能系统上未安装": [5],
"贪婪量词": [[2,7]],
"来更新其他片段的对齐": [11],
"如果选中此选项": [11],
"它会尝试根据文件的扩展名选择过滤器": [11],
"可以确保原文文本中的": [11],
"中主账户密钥和客户": [5],
"默认": [[3,11]],
"如果只有一个可用译文": [8],
"空行处分割": [11],
"文件并点击之": [5],
"按文件细分": [11],
"非空白字符": [2],
"command": [[3,9],5],
"如果你选择了": [4],
"子文件夹中": [[5,6,10,11]],
"的导出不完全符合级别": [6],
"n.n_without_jr": [5],
"该文件不再需要": [5],
"进行确认": [10],
"此功能是团队项目特有的": [8],
"在译文文档中会得已保留": [11],
"用户可能需要生成一组所讨论项目专用的规则": [11],
"viewmarkbidicheckboxmenuitem": [3],
"year": [6],
"改用命令行让用户可以控制和修改程序的行为": [5],
"请用外部文本编辑器编辑之": [1],
"非贪婪": [[2,7]],
"如果项目是": [5],
"给定的项目可以拥有任意数量的参考词汇表": [1],
"表示自定义标签的正则表达式": [11],
"在翻译中": [10],
"您可以用文件管理器从系统中其他位置手动复制文件": [4],
"你可以修改": [5],
"比如": [9,[0,1,2]],
"对于中文情况下的": [5],
"version": [5],
"恢复默认值": [11],
"本身就可以找到很多很有用的正则表达式案例": [2],
"其中必须有一个映射包含": [6],
"的完整发行版": [5],
"会自动检测该目录下的术语表文件": [1],
"中的命令的顺序": [11],
"选项计算得到的百分比": [11],
"你可以直接从命令行启动它": [5],
"快捷键同样可从": [3],
"勾选此选项时": [11],
"虚拟机的一种动态语言": [11],
"空白字符": [[2,11]],
"不考虑标签和数字": [11],
"projecteditmenuitem": [3],
"键可以允许用键盘上的方向键将光标移入原文片段": [9],
"搜索窗口中": [6],
"译文区域设置代码": [11],
"britannica": [0],
"删除": [[5,9]],
"下面的例子中原文片段和上面模糊匹配的例子里一样是": [9],
"韦氏国际词典": [0],
"先决条件": [5],
"删除参数前的": [5],
"wikipedia": [8],
"但这些字符在编辑窗格中能正确显示": [1],
"通用": [11],
"共享翻译记忆库": [6],
"内存启动": [5],
"字符只匹配一个字符": [11],
"重复单词": [2],
"选择第五个匹配": [3],
"可用于通过网络部署独立的": [5],
"此选项允许以批处理模式进行标签验证": [5],
"级别": [6],
"iceni": [6],
"默认值是": [11],
"因此分配比这数字更少是没有任何好处的": [5],
"内包含有更详细的片段信息": [11],
"标签卡": [8],
"可以针对要翻译的文本调整分割规则来提高工作效率": [11],
"用户可以创建项目专用的文件过滤器": [11],
"例外规则": [11],
"如果用的是": [3],
"其中包含纠正或忽略该错误的操作": [8],
"不翻译带有以下属性键值对的标签的内容": [11],
"这个外部命令不能包含": [11],
"来选择编码": [11],
"已修正了若干以上问题": [1],
"要离线工作": [6],
"dsun.java2d.noddraw": [5],
"可以用外部工具将文件转换为支持的格式": [6],
"要以期望语言显示快速入门指南": [5],
"密钥请求表单": [5],
"翻译某种类型的文件": [11],
"您可以考虑为原文语言定义更多例外规则以获取语意更完整也更连贯的片段": [11],
"x0b": [2],
"该片段分为两部分显示": [9],
"统计": [3],
"在译文文件创建过程中不会被复制到": [11],
"如果你勾选了相应的复选框": [5],
"http": [6,5,11],
"翻译的日期": [11],
"代理主机": [5],
"和级别": [6],
"步之前就从": [10],
"总独特片段数和已译独特片段数显示在底部": [11],
"有三种切换状态": [6],
"项目参数必须由项目经理来更改": [6],
"则将处理文件": [5],
"的所有内容映射到本地": [6],
"中修改": [6],
"projectsinglecompilemenuitem": [3],
"对所有出现之处进行操作": [11],
"存储库网址": [6],
"管道": [11],
"起来": [6],
"myfil": [6],
"系统上又叫": [5],
"多个空格": [2],
"位于": [[5,10]],
"你可以在": [5],
"终端窗口": [5],
"如果涉及翻译团队": [6],
"匹配从给定单词的当前位置到结尾的零个或多个字符": [11],
"选项来打开项目": [6],
"或在其下面的子文件夹中": [0],
"则此功能会覆盖所选部分": [8],
"且只会应用这些映射": [6],
"即使此词汇在词汇表中存在有多种形式": [11],
"system-os-nam": [11],
"的语言使用右对齐": [6],
"历史完成": [8],
"optionstabadvancecheckboxmenuitem": [3],
"则包含项目专用的图形界面设置": [10],
"子文件夹中相同的层次结构": [10],
"此标记行为优先于": [8],
"heapwis": [11],
"optionsviewoptionsmenuloginitem": [3],
"翻译以下属性": [11],
"启用后": [11],
"包含项目参数在": [10],
"存储库": [6],
"维持着与": [10],
"tar.bz2": [0],
"它们在编辑器中显示为": [11],
"其中可以输入文件名片段": [11],
"在注释中": [11],
"bundle.properti": [6],
"此单词或短语会自动输入到": [11],
"等等": [[0,9,11]],
"内存的命令是": [5],
"选择下一个匹配": [3],
"标记非独特片段时包含首个非独特片段": [11],
"这两种规则的行为如下": [11],
"x64": [5],
"进阶用户可以通过在终端窗口中输入": [5],
"风格": [6],
"您可以使用它来自定义输出文件的名称": [11],
"脚本": [11,8,[5,7]],
"检查可能的标签错误": [6],
"在行尾位于句点后的额外空格": [2],
"在光标位置插入当前选中的模糊匹配": [8],
"来选择其他匹配项": [9],
"用户界面写入": [1],
"isn\'t": [2],
"文件夹": [5,10],
"遵循非常严格的流程": [6],
"安全存储": [11],
"保存项目并退出": [8],
"的远程桌面会话中运行缓慢": [5],
"上一个片段": [[3,8]],
"要安装拼写词典": [4],
"项目的内部翻译记忆库不会更改": [11],
"使用此命令": [8],
"的结果": [11],
"其中有运行": [5],
"optionsteammenuitem": [3],
"豁免后继更改": [10],
"gzip": [10],
"来输入供应商提供的详细认证信息": [11],
"吾不知": [5],
"查找选项与显示在": [11],
"如果前者不可用": [5],
"esc": [11,2],
"x86": [5],
"如果文件包含映射": [6],
"在片段的备注中查找": [11],
"您可以改变任何一个窗格的位置": [9],
"所选匹配的来源": [[3,8]],
"基本上有两种方法可以做到这一点": [4],
"搜索条目中的空格可以匹配普通的空格字符或不换行空格": [11],
"nostemscor": [11],
"它不适用于自动升级的": [5],
"选项": [[8,11],[4,9],[2,3,6,7,10]],
"定义为默认译文": [9],
"例如转到下一个符合当前查找约束条件的": [11],
"对于断句规则": [11],
"移动到下一个在翻译记忆库中无相同内容的片段": [8],
"文件中记录片段已被自动填充的信息": [11],
"console-createpseudotranslatetmx": [5],
"替换下一个": [11],
"要安装": [5,0],
"然后": [11,5],
"longman": [0],
"建议使用能直接查看其效果的工具": [11],
"fuzzyflag": [11],
"原文文件": [3],
"用通配符": [6],
"要记住这并不会将现有文件从旧文件夹移动到新位置": [11],
"重新载入": [[3,8]],
"如果你在": [11],
"查找窗口": [[8,11]],
"merriam": [[0,7,9]],
"escap": [1],
"但还未重新载入项目": [1],
"文件并不包含任何映射": [6],
"而注释行应以": [3],
"如果片段的当前翻译并不合适": [9],
"数字在匹配的原文和译文中必须完全相同": [11],
"projectname-omegat.tmx": [6],
"标签验证器会在第一个无效标签处停下": [5],
"来指示此": [6],
"所有匹配的片段": [11],
"时才会获取机器翻译": [11],
"则只会在完全按此顺序出现时才会被找出来": [11],
"第一个是": [3],
"来配置自动完成器词汇表视图": [11],
"映射本地": [6],
"按段": [11],
"会还原为默认值": [5],
"则默认会将选中的文本粘贴到": [8],
"删除某个文件": [10],
"你可以选择特定领域以限制发送与接受的数据量": [11],
"模糊匹配和不匹配的数量": [8],
"或反之": [6],
"下的": [8],
"将这些文件复制到另一个文件夹": [4],
"一次执行所有自动检查并将结果显示在窗口中的质量保证工具": [8],
"n.n_without_jre.zip": [5],
"会检查四种类型的问题": [8],
"使用来自辅助翻译记忆库的翻译并冠以": [10],
"不可见的格式信息": [11],
"当前所在片段的背景会变为红色": [10],
"资源": [5],
"中定义了执行外部搜索的命令": [8],
"magento": [5],
"仓库文件夹": [6],
"方法随发行版本而不同": [5],
"在存储库的子目录中映射项目": [6],
"参数": [5],
"未译": [11],
"同时后面要跟着空格和一个首字母大写的单词": [11],
"出于保密原因": [11],
"区间": [2],
"只在已译片段中查找": [11],
"显示版权": [8],
"标记词汇表匹配": [1],
"u00a": [11],
"一开始": [6],
"如果选择了此选项": [11],
"则可以配置标签检验器选项来检查编程变量": [11],
"上方表格的底部会出现一个空行": [11],
"shift": [3,11,[6,8],1],
"一些词典没有附加条件": [0],
"命令": [5],
"文件处于同一文件夹": [5],
"如果有所不同": [6],
"java": [5,11,3,2,[6,7]],
"exe": [5],
"用文件管理器": [4],
"已译文件的位置": [5],
"密钥可以使用": [5],
"由于很容易获得大量关于": [6],
"project_save.tmx": [6,10,11],
"在每次打开或者重新载入项目时也会将翻译记忆库备份到同一子目录下的": [6],
"dictionari": [0,10],
"使用此命令你可以前进到之前用": [8],
"西班牙语进行检查": [4],
"即项目创建时": [6],
"并使用编辑模式对话框": [11],
"标签问题": [8],
"会首先应用加拿大法语规则": [11],
"将下面这行添加到": [3],
"可能显示": [5],
"dictionary": [7],
"来配置自动文本选项以及添加或移除条目": [11],
"正常运行": [5],
"区分大小写": [11],
"则将当前原文片段写入该文件": [8],
"在下面的例子中": [9],
"如果遇到问题": [6],
"卡斯提尔": [4],
"他是项目的有效翻译记忆库": [10],
"插入原文": [[3,8,11]],
"技术上说": [11],
"如果你正在翻译的文本已更新": [11],
"原文文本将被复制到译文段落": [11],
"文件会自动完成许多更改": [6],
"只有一点不同": [11],
"会显示最后一个已打开的查找窗口": [8],
"将自动自动执行其中的命令": [5],
"设计的翻译记忆库和原文文件夹": [6],
"此类文件有时被称为": [6],
"timestamp": [11],
"位置不重要": [5],
"projectaccessrootmenuitem": [3],
"点击图标按钮": [5],
"dyandex.api.key": [5],
"对词典条目使用模糊匹配": [[8,11]],
"即导入您的新项目中": [6],
"原文词汇": [8],
"plugin": [11],
"一些简单的例子": [11],
"但是只有一个词汇表": [1],
"任意字符": [2],
"会真正删除选定的字典": [4],
"好些过滤器": [11],
"如果这么做": [9],
"您可以通过按下": [9],
"该文件与": [5],
"editinsertsourcemenuitem": [3],
"输入任何文本都会打开": [11],
"viterbi": [11],
"microsoft": [11,[5,6],9],
"projectnewmenuitem": [3],
"术语问题": [8],
"每种输出格式都有专门的方法来处理从右向左显示": [6],
"标准": [1],
"术语": [11],
"自动于编辑器同步": [11],
"optionstranstipsenablemenuitem": [3],
"规则的优先级": [11],
"在编辑器窗格中": [10],
"见下面的": [2],
"右键单击菜单": [11],
"glossari": [1,[6,10],11],
"ignored_words.txt": [10],
"到那然后在": [5],
"字符": [[2,11],[5,7]],
"configuration.properti": [5],
"github.com": [6],
"glossary": [7],
"窗格中当前选择的匹配相对应的片段": [8],
"这意味着如果原文语言是从左向右的而目标语言是从右向左的": [6],
"点击任何一个文件将打开它进行翻译": [11],
"服务类型": [11],
"术语查询": [8],
"要向其中添加内容": [10],
"在此处开始新段落": [11],
"因此你只需要对其他文件类型进行此操作": [11],
"这会在": [5],
"string": [5],
"它们可能意味着有较长的句子重复了很多次": [11],
"如果激活了语句分割规则": [11],
"not": [11,5],
"加拿大法语": [11],
"窗口显示以下信息": [11],
"该栏为用户提供了关于正在进行的特定操作的反馈": [9],
"等的句点": [11],
"第二个匹配项": [9],
"在文件中查找": [11],
"随着翻译进程": [10],
"不同的字符集编码": [11],
"was": [11],
"菜单": [[8,11],[5,7]],
"selection.txt": [11,8],
"使用此选项": [11],
"xhtml": [11],
"不区分全角": [11],
"如果您需要使用启动命令参数": [5],
"finder.xml": [11],
"来源既包括随着您对项目的翻译而实时创建的内部项目翻译记忆库": [9],
"用所选机器翻译服务提供的译文替换掉译文片段": [8],
"如果你想在": [6],
"按钮来向项目中添加原文文件": [11],
"以逗号分隔": [11],
"window": [5,[0,2,8]],
"你还可以在文件过滤器对话框的译文文件名模式字段中直接修改名称": [11],
"disable-project-lock": [5],
"设为译文语言": [4],
"并粘贴到": [8],
"omegat.pref": [11],
"如果你有一个由扫描页面组成的": [6],
"会在译文片段中查找包含疑似来自德语键盘的字符的单词": [11],
"会显示所有包含全部指定单词的片段的列表": [11],
"是此": [6],
"项目文件夹": [10,[6,7,9]],
"将在每个结果上方显示片段所在的文件名": [11],
"目前仅适用于来自带": [11],
"翻译": [6,[5,7]],
"并使用所选译文语言的默认值": [11],
"已译和未译片段中都进行查找": [11],
"产生命中": [2],
"新条目始终采用制表符分隔的格式": [1],
"仅当安装了拼写字典时才有效": [8],
"它是开源领域中参见且首选的编程工具": [11],
"编码和每个文件所包含的片段数": [11],
"pt_pt.dic": [4],
"中存储了登录详细信息的项目": [11],
"可以用如下形式的变量名": [11],
"当你在": [6],
"如果你的规则集处理的是语言": [11],
"如果勾选了": [[8,11]],
"level1": [6],
"当前片段的原文内容": [11],
"详细信息请查看相关手册": [6],
"这些文档是用操作系统所选择的应用程序打开的": [8],
"分类": [2],
"level2": [6],
"将从原文片段中移除所有格式化标签": [11],
"空的": [6],
"译文文件编码": [11],
"原文文件及其译文": [8],
"自动完成器会在输入已翻译术语表中条目的首字母或输入标签的": [11],
"打开项目": [10,[3,6]],
"所选定的模糊匹配以粗体突出显示": [9],
"就像使用任何其他应用程序一样": [6],
"并不需要指示": [4],
"插入当前片段的原文": [11],
"有关技术细节": [8],
"是此片段在项目中其余地方出现的次数": [9],
"web": [5,7],
"en-us_de_project": [6],
"句点后面跟着一个大写字母": [2],
"本地文件夹或文件的名称": [6],
"哪怕是在排除文件夹中": [6],
"编辑器": [[8,9,11],[6,7]],
"editselectfuzzy4menuitem": [3],
"editregisteridenticalmenuitem": [3],
"或使用": [4],
"有时候你可能希望自动重命名所翻译的文件": [11],
"相对于": [6],
"用于定义自己的分割规则变体或设计更复杂更强大的关键字查找内容": [2],
"只要做出新的选择并再次按下": [8],
"前向": [11],
"初始载入片段数量": [11],
"关于": [6,[3,8]],
"则不使用原文文件名": [11],
"就可以选择其他约束条件": [11],
"启用语句级别分割": [11],
"有以下查找方法": [11],
"pt_br.dic": [4],
"其中": [[3,6],[5,8,9]],
"快捷方式": [5],
"不会将任何信息保存到你所运行的计算机之外": [5],
"unabridg": [0],
"在项目中任意位置稍做改动": [10],
"有关": [11],
"当你加载项目时": [6],
"地执行替换": [11],
"如果在视图菜单中已勾选": [11],
"然后勾选或取消勾选所提供的复选框": [11],
"optionsglossaryexactmatchcheckboxmenuitem": [3],
"已译与总量": [9],
"工具可以输出": [10],
"可以在其中设置脚本的位置": [8],
"更改后的过滤器配置的副本会随同项目一起存储": [11],
"标记段落界定": [8],
"对于分割而言": [11],
"快捷键而": [6],
"翻译的作者或修改者": [11],
"nnnn": [9,5],
"project_save.tmx.yearmmddhhnn.bak": [6],
"指定了模式后": [5],
"俄语菜单等": [5],
"拖进去的": [9],
"要忽略的文件或文件夹": [11],
"查看": [6],
"myproject": [6],
"查找以任意顺序包含全部指定关键字的片段": [11],
"译者可能想要整个改变语句的顺序": [11],
"对所有项目生效": [8],
"使用其编码声明": [11],
"zh_cn.tmx": [6],
"个或更多": [3],
"实现": [5],
"中选择": [5],
"必须始终是开启段落的标签": [11],
"的快捷键": [3,11],
"分钟自动保存一次翻译": [8],
"窗口的组件恢复到默认状态": [11],
"示例和建议": [11],
"其他选项": [11],
"使用拼写词典": [4,7],
"在项目中查找": [[3,8,11]],
"选项来使其可见": [10],
"建议": [5],
"archiv": [5],
"repo_for_omegat_team_project.git": [6],
"则将文件分配给它进行处理": [11],
"user": [5],
"extens": [11],
"会以控制台模式启动并翻译给定项目": [5],
"它在默认情况下是关闭的": [8],
"中间的文本框": [2],
"已译": [[9,11]],
"它们可以存取计算机上的硬盘驱动器": [5],
"忽略未译片段": [8],
"如果同时勾选了此选项与": [11],
"原文片段和模糊匹配中包含的数字清单必须相同": [11],
"向译者发送邀请": [6],
"窗口": [8,[5,9,11]],
"sure": [11],
"弱类型的并且具有头等函数": [11],
"程序的启动参数将从": [5],
"可以重叠窗格": [9],
"先考虑避免出现这种情况会更有意义": [10],
"diff": [11],
"而后一种情况下": [6],
"an": [2],
"editmultiplealtern": [3],
"关键字查找": [11],
"按下": [9,[8,11]],
"只需修改此文件即可": [5],
"如果有缺失的": [8],
"git.code.sf.net": [5],
"创建一个与其他项目分开的项目": [6],
"词汇表窗格允许您使用在词汇表文件中建立的表达方式和专业术语的自有集": [9],
"则在块级": [11],
"你可以通过编辑": [5],
"因为它们和记录在翻译记忆库中的片段不再匹配": [11],
"而非数字": [6],
"或者完全不分割": [11],
"be": [11],
"命令所离开的拿个片段": [8],
"filters.xml": [6,[10,11]],
"两种情况下": [6],
"显示当前项目中每个文件的匹配统计数据": [8],
"项目中的片段总数": [9],
"转义": [[2,7]],
"则第一个被翻译的片段将假定为默认翻译": [11],
"创建所选文档的翻译记忆": [6],
"br": [11,5],
"译者可以给打开的片段添加备注": [9],
"翻译文本的纯文本版本并在稍后阶段在相关应用程序中添加必要的样式可能会不那么麻烦": [6],
"文件过滤器首选项": [11],
"但并不限制你将它用于原文文件之外": [6],
"segmentation.conf": [6,[5,10,11]],
"并将": [5],
"ca": [5],
"cd": [5,6],
"可能会好一些": [4],
"ce": [5],
"öäüqwß": [11],
"可以只查找词汇的精确匹配项": [1],
"为匹配的语言模式所定义的所有片段分割规则集都是活动的": [11],
"cn": [5],
"你可以从命令行用包含启动选项的脚本启动": [5],
"区别很小": [11],
"文件中新增或重写编码声明": [11],
"或类似的信息": [5],
"以正确显示从右向左": [6],
"本地项目将与远程存储库进行同步": [6],
"cx": [2],
"下一个独特片段": [3],
"用匹配或所选内容替换译文": [[3,8]],
"空格会显示一个小点": [8],
"在翻译中它们将显示为单独的片段": [11],
"词库": [11],
"apach": [4,[6,11]],
"会显示当前片段的最后一次更改的时间和作者": [8],
"辅助性的": [6],
"格式保存": [6],
"幻灯片批注": [11],
"adjustedscor": [11],
"的默认": [3],
"dd": [6],
"后跟着": [3,2],
"提交译文文件": [8],
"未指定编码的文件格式将使用您为其文件扩展名所设置的编码": [11],
"全部": [6],
"管理": [6],
"f1": [3],
"f2": [9,[5,11]],
"f3": [[3,8]],
"dr": [11],
"f5": [3],
"如果未选择任何文本": [8],
"查找窗口有它自己的菜单": [11],
"在单个文件或包含一组文件的文件夹中查找": [11],
"是带有完整的路径的": [5],
"将匹配单词": [11],
"dz": [0],
"editundomenuitem": [3],
"点击另一列中与第一个片段相对应的翻译": [11],
"标签的": [11],
"诸如": [6],
"以下列出了在翻译记忆库中搜索时可能会用到的表达式": [2],
"与统计数据": [6],
"启动与此数字相关联的脚本": [8],
"此选项允许": [11],
"u000a": [2],
"则需要在系统上运行有兼容的": [5],
"在大多数情况下": [11],
"有可能会出现这种情况": [10],
"en": [5],
"文件做出了更改": [5],
"如果未勾选自动传播复选框": [11],
"用原文文件名作为备选译文的识别信息的一部分": [11],
"u000d": [2],
"u000c": [2],
"会尝试通过组合现有的句子翻译来创建段落模糊匹配": [11],
"则此操作会手工确认您是否确实要退出": [8],
"u001b": [2],
"stats.txt": [10],
"如果希望词典中显示有着相同词根的词汇": [11],
"所有": [6],
"将文件复制到原文文件夹": [[3,8]],
"项目属性": [11,[7,8,10]],
"foo": [11],
"请务必记下所有这些详细信息": [11],
"exclud": [6],
"for": [11],
"窗口与对话框": [11,[7,8,9]],
"你可以将原文文本自动插入编辑区": [11],
"个片段": [11],
"fr": [5,[4,11]],
"content": [5,11],
"如果你输入的是": [5],
"或者将它添加到词典中": [4],
"applescript": [5],
"剪切": [9],
"gb": [5],
"class": [11],
"helplogmenuitem": [3],
"包括重复": [9],
"editoverwritetranslationmenuitem": [3],
"outputfilenam": [5],
"未找到命令": [5],
"删除译文并将片段设置为未译": [8],
"在将源文件分割为结构单元之后": [11],
"aeiou": [2],
"请从此列表中删除该项目": [11],
"这包括你对": [5],
"只会翻译直接位于原文文件夹下的": [5],
"更多内容参见": [6],
"hh": [6],
"最后": [5],
"duser.languag": [5],
"视图菜单": [3,7],
"使用该选项就不会发生锁定": [5],
"上双击或者点击其在": [5],
"目录下": [1],
"默认对齐": [6],
"过滤器选项": [11],
"file-target-encod": [11],
"注意那两个": [5],
"不会执行项目专有设置": [11],
"context": [9],
"清空": [11],
"已经正确地与": [5],
"https": [6,5,[9,11]],
"id": [11,[5,6]],
"这是两种常见的以开源许可提供的团队软件版本与修订控制系统": [6],
"if": [11],
"但如果选择了该文件夹": [5],
"project_stats.txt": [11],
"通过修改": [5],
"ocr": [[6,11]],
"或完整选择": [11],
"它将附加到您翻译的所有片段中": [11],
"projectaccesscurrenttargetdocumentmenuitem": [3],
"in": [11,5],
"项目的主翻译记忆库": [6],
"插入下一个缺失标签": [[3,8]],
"termin": [5],
"ip": [5],
"备选译文会在任意文件中生效": [11],
"is": [2],
"新添加的词汇会在变更保存后立即识别生效": [1],
"it": [11],
"目录中": [6],
"如果已经选择了译文片段的一部分": [8],
"或者在当前光标位置创建新的片段分割符": [11],
"odf": [6,11],
"odg": [6],
"工作表名称": [11],
"ja": [5],
"运行时环境": [5],
"服务": [5],
"上移": [11],
"带重命名的映射": [6],
"文件夹的翻译记忆库会在光标从一个片段移动到另一个片段时立即自动载入": [8],
"请按照项目经理所提供的流程进行操作": [6],
"时也可以用此命令来将译文登记为与原文完全相同": [8],
"相兼容的": [5],
"odt": [6,11],
"所有更改将在下次连接可用时进行同步": [6],
"不会干扰到通用的分割规则集": [11],
"gotonexttranslatedmenuitem": [3],
"要访问": [5],
"nplural": [11],
"以显示通常跟在刚输入的单词之后的建议单词": [8],
"有时缩写为": [11],
"js": [11],
"您可以将这些文件复制更名为": [4],
"命令行模式下的": [5],
"见下文的各个表格": [3],
"learned_words.txt": [10],
"货币符号": [2],
"所有译文部分已被自动填充": [8],
"片段数": [[9,11]],
"菜单项": [3],
"下移": [11],
"前模式": [11],
"ftl": [5],
"还可以通过点击": [11],
"不可见说明性文本": [11],
"ftp": [11],
"viewdisplaymodificationinfoallradiobuttonmenuitem": [3],
"来打开项目": [3],
"指定在": [11],
"之前": [8,6],
"draw": [6],
"则其内容会被忽略": [11],
"在这里我们不描述它们如何工作": [6],
"将分割规则设为项目专用": [11],
"此选项仅当可以进行此选择时才会出现": [11],
"请确保": [5],
"以及扩展名为": [6],
"如果您按": [8],
"标签然后删除它": [11],
"来识别原文文件的编码": [11],
"制表符分隔的值": [1],
"dswing.aatext": [5],
"即使它们在同一文档或不同文档中多次出现": [11],
"编辑模式对话框提供了以下选项": [11],
"则包含项目专用的文件过滤器": [10],
"查找全角形式": [11],
"页面查看": [5],
"并选择": [5],
"项目": [6,8,[5,11],[0,4,7,10]],
"是否有与": [5],
"lu": [2],
"按段落或按语句分割": [11],
"这些数字上并不能看出重复的相关程度": [11],
"cycleswitchcasemenuitem": [3],
"mb": [5],
"me": [6],
"上创建访问密钥": [11],
"omegat.png": [5],
"如果当前片段是文件中的最后一个片段": [8],
"页面": [[3,8]],
"你需要重新启动": [11],
"mm": [6],
"entri": [11],
"示例中的项目显然已经完成": [9],
"mr": [11],
"ms": [11],
"mt": [10],
"会创建之": [11],
"右键单击底部面板中的": [11],
"需要输入元字符": [2],
"即使你的机器上已经安装了包含相关字形的字体": [8],
"此文件夹将包含您添加入项目的词典": [10],
"my": [6,5],
"那么只有被选中的部分插入到光标位置处": [8],
"将想要使用的翻译记忆库放在新项目的": [6],
"查找字符串": [11],
"标签将不进行翻译": [11],
"将各种替代项之一": [9],
"右键单击文件名会打开一个弹出窗口": [11],
"首先解析文本以进行结构级分割": [11],
"nl": [6],
"nn": [6],
"用户指南": [7,5],
"no": [11],
"code": [5],
"格式的项目包": [8],
"在下面的示例中为": [4],
"gotohistoryforwardmenuitem": [3],
"注释前面会显示一个数字": [1],
"可将当前片段的译文替换为建议译文": [9],
"下载正确的安装包": [5],
"内容流": [6],
"实际使用": [0,7],
"字幕": [5],
"of": [0],
"之间的差距对文本中的重复情况提供了大致的概念": [11],
"将更改全局用户过滤器": [11],
"也可以通过点击文件名并按下各个": [11],
"请在浏览器中打开以下网址": [5],
"在大多数系统中": [5],
"os": [[6,11]],
"实例的数量": [8],
"的优势": [11],
"自动": [[10,11]],
"本文档是免费的计算机辅助翻译工具": [7],
"关于此主题的更多信息": [10],
"与对翻译记忆库进行查找相对": [11],
"editinserttranslationmenuitem": [3],
"如果译者需要与别人分享他们的": [6],
"中的": [[5,11],[0,2,6,9]],
"计数": [[7,9]],
"po": [11,9,5],
"使您可以更改上下文菜单": [11],
"optionsglossarystemmingcheckboxmenuitem": [3],
"修订状态等原因而不太可靠的翻译记忆库与高质量的翻译记忆库区分开来会很有用": [10],
"pt": [5],
"你可以使用此选项": [5],
"对于从左向右": [6],
"检查更新": [8],
"要翻译的文件的远程位置或目录": [6],
"提供了捆绑了": [5],
"现在退出项目": [6],
"所有语言都能访问": [11],
"可以在编辑器窗格": [11],
"上图中是": [4],
"当前片段原文和译文的字符数": [9],
"能够根据属性的值来将某些标签标记为不可翻译在某些时候会很有用": [11],
"对话框窗口中定义的行为进行操作": [10],
"edit": [8],
"也可以通过选定一行并按": [11],
"会匹配半角形式": [11],
"editselectfuzzy5menuitem": [3],
"用户很熟悉的含义": [11],
"点击此按钮可访问": [11],
"表示该匹配为模糊匹配": [11],
"此应用程序只运行在你的机器上": [5],
"这是它的工作原理": [11],
"用作默认译文": [[3,8,9]],
"rc": [5],
"其他在": [8],
"includ": [6],
"参见": [11,2,[6,8,9]],
"文本替换": [11,7],
"选中并用确定进行确认": [5],
"子文件夹的内容将反映当前的翻译状况": [10],
"亦称为": [11],
"第二列是词汇译文": [1],
"的命令": [5],
"你可以用以下参数来指定要创建的": [5],
"会打开下拉菜单": [4],
"sc": [2],
"免得把桌面搞得乱七八糟": [11],
"显示隐藏文件": [10],
"解压缩下载的文件": [5],
"从一个片段移动至另一个": [11],
"因为只移除了内联标签": [11],
"帮助菜单": [3,7],
"机器翻译": [[8,9,11],7],
"上半部分以原文语言显示": [9],
"请勾选断句": [11],
"即以双语形式导出的当前项目的内容": [6],
"关键字查找类似于诸如": [11],
"如果添加了前缀": [11],
"editoverwritesourcemenuitem": [3],
"与你授予本地版本的权限相同": [5],
"此功能旨在汇集原文文件": [6],
"enforc": [10],
"链接": [11],
"remov": [5],
"相对于存储库网址": [6],
"tm": [10,6,8,[5,7,9,11]],
"如果您已经有了希望拼写检查其忽略": [10],
"根据项目的特征更改默认间隔": [11],
"to": [5,11],
"v2": [5],
"允许每个项目自有命令": [11],
"如果文件不存在": [6],
"要执行此操作": [5],
"你还可以定义和标签验证相关的各种选项": [11],
"但转义后继": [2],
"document.xx": [11],
"其他平台的用户": [5],
"tw": [5],
"没有": [5],
"则会移动到上一个文件的最后一个片段": [8],
"因此此处所述的通配符查找仅适用于精确查找和关键字查找": [11],
"例如文件名为": [4],
"viewmarkautopopulatedcheckboxmenuitem": [3],
"也可以从其他项目中导入": [1],
"projectwikiimportmenuitem": [3],
"countri": [5],
"ui": [6],
"插入原文文本时替换命中的词条": [11],
"则会让": [11],
"是用户指定的按键组合": [3],
"文件并要求译者将其复制到专用的文件夹中然后用": [6],
"如果插入的匹配项来自名为": [10],
"此选项将指定要自动处理的文件": [5],
"必须重命名为": [6],
"也能输入": [10],
"日文或韩文字符输入系统很有用": [11],
"选择了适当的菜单后": [5],
"在熟悉": [6],
"this": [2],
"光学字符识别": [6],
"打开首选项对话框中的": [8],
"添加来自": [10],
"框中": [11],
"你可能需要向下滚动才能看到它": [11],
"主项目目录": [6],
"vi": [5],
"你可以通过按": [6],
"自定义已译文件的文件名和选择用什么编码来加载原文文件和保存翻译": [11],
"将其恢复为安装时定义的状态": [9],
"用作翻译工具": [5],
"为所有标签保留空格": [11],
"但会转义后继的字符": [2],
"并高于默认": [11],
"将窗格从主窗口中分离出来": [9],
"以后点击": [5],
"比如说巴西葡萄牙语情况下的": [5],
"命令字段的右边": [5],
"即可": [8],
"groovy.codehaus.org": [11],
"repo_for_omegat_team_project": [6],
"文件的标准行为": [11],
"墨西哥西班牙语": [4],
"文件会使用操作系统的默认编码加载": [11],
"backspac": [11],
"在术语表窗格中某些字符显示不正确": [1],
"要得到字面匹配更接近于": [11],
"用户主文件夹下的": [5],
"emac": [5],
"org": [6],
"处于从右向左模式时": [6],
"distribut": [5],
"本地文件": [6],
"已经对俄语进行了本地化": [5],
"尽管其中有个句点": [11],
"有许多交互式工具可用于开发和测试正则表达式": [2],
"是健壮的应用程序": [6],
"所有片段": [8],
"xf": [5],
"正常情况下译文文档应当都能打开": [11],
"译文语言和国家": [11],
"每月最多可以免费处理": [5],
"请将": [10],
"或者你可以点击": [5],
"xx": [5,11],
"强烈建议译者从原始文档中删除样式信息以在": [6],
"xy": [2],
"文本文件可能会在断行": [11],
"版本将被覆盖": [5],
"没有可用的图形环境": [5],
"sourc": [6,10,11,[5,8],9],
"type": [6,3],
"如果您在按": [8],
"文件需要这样来回": [6],
"从列表中选择供应商": [11],
"通常应当避免这么做": [11],
"toolssinglevalidatetagsmenuitem": [3],
"并使用": [5],
"离外复选框的勾选": [11],
"选择第四个匹配": [3],
"projectaccesssourcemenuitem": [3],
"专用参数": [6],
"文件必须由项目管理器删除": [6],
"该文件夹包含": [5],
"脚注": [11],
"无论它们位于项目中何处": [6],
"中检查": [0],
"非单词边界": [2],
"yy": [9,11],
"将其选作翻译记忆库文件夹": [6],
"或等效物": [0],
"段落": [[6,11]],
"要将一条规则定义为例外规则": [11],
"所支持的正则表达式": [2],
"之后": [5],
"从译文片段之外的地方拖来的文本会复制过来": [9],
"检查是否有任何": [11],
"等语言所启发的其他强大功能": [11],
"当前项目所使用的过滤器以粗体显示": [11],
"确保你有免费的": [5],
"push": [6],
"zh": [6],
"更改为所需的量": [5],
"readme_tr.txt": [6],
"也包括来自于您的过往工作或从客户或翻译代理那里得到的辅助翻译记忆库": [9],
"复数形式": [1],
"要启用拼写检查器": [4],
"penalti": [10],
"改变分割规则设置可能导致一些已经译好的片段被分拆或者合并": [11],
"来获取最新的辅助翻译工具列表": [6],
"仓库中的所有": [6],
"不要在第一个标签之前放置任何文本": [11],
"如果当前译文不适用": [8],
"和上述类似": [11],
"然后点击": [[5,11]],
"这个字段应当包含": [11],
"新建": [[3,5,6,8]],
"utf8": [1,[8,11]],
"完全匹配": [8],
"精确查找以短语方式查找": [11],
"out": [6],
"随同": [11],
"这种情况下需要额外的参数": [5],
"用户组": [6],
"编写的网页添加原文文件": [11],
"来设置规则": [11],
"都可以使用通配符": [11],
"也使用这个选定的语言": [5],
"来将译文区域替换为匹配内容": [9],
"power": [11],
"则只要项目被打开": [6],
"您的新快捷键现在应该会显示在已修改的菜单项旁边": [3],
"在光标位置插入原文": [8],
"版本的独立工具": [2],
"为单位": [5],
"tag-valid": [5],
"创建的": [6],
"已经完成了多少": [9],
"提示": [11,4,[6,7]],
"但不能用于选择模糊匹配": [9],
"示例映射": [6],
"以激活该查询": [8],
"添加到": [5],
"点击查找按钮会把项目中所有包含输入字符串的片段显示出来": [11],
"会在进行替换操作之前将原文片段复制到译文片段": [11],
"翻译记忆库匹配": [11],
"u0009": [2],
"xhh": [2],
"revis": [0],
"u0007": [2],
"会在模糊匹配查看器中看到来自于这些翻译记忆库的匹配": [6],
"repositori": [6,10],
"允许译文和原文相同": [8],
"属性": [11,[6,8],[0,3,4]],
"更改片段分割规则或文件过滤器": [6],
"lowercasemenuitem": [3],
"wiki": [[0,9]],
"firefox": [[4,11]],
"那么新的安装程序也必须是": [5],
"存放术语表的子文件夹必须包括名为": [4],
"页面上的": [8],
"在正翻译的片段中缺失的单词显示为蓝色而与缺失部分相邻的单词为绿色": [9],
"它会显示出现在当前片段中的词汇的翻译": [9],
"原文标签": [8],
"等系统": [5],
"或文件": [6],
"运行脚本": [8],
"sens": [11],
"文件会出现在": [11],
"本章内容是为高级用户准备的": [2],
"即使未选中": [8],
"我们建议您使用": [11],
"翻译为比如说英语": [6],
"则会显示文件并且可以选择": [5],
"再按一次": [9],
"让对原文文件与项目设置的外部更改生效": [8],
"openoffic": [4,11],
"其中提供了": [9],
"有一个或多个特定选项": [11],
"文件格式": [1,[7,8]],
"中完成": [6],
"如果在开始翻译后原文文件有所修改": [6],
"optionsautocompletechartablemenuitem": [3],
"git": [6,[5,10]],
"内存分配": [5],
"而其他词典": [0],
"xx-yy": [11],
"它将包含你在项目中使用的词汇表": [10],
"个查找条目": [11],
"统计数据": [[3,8,10]],
"必须用": [3],
"全部替换": [11],
"optionsspellcheckmenuitem": [3],
"如果修改后的片段已经确认": [8],
"它们全都使用": [3],
"右键点击带下划线的词汇并选择一个词汇译文可以将其插入片段的译文部分中当前光标位置处": [1],
"optionssetupfilefiltersmenuitem": [3],
"十六进制值为": [2],
"扩展名": [1],
"下文罗列了所有可能参数": [5],
"所有其他文件将被远程存储库中的文件替换": [6],
"altgraph": [3],
"项目导出数据到纯文本文件": [11],
"在某个空闲的数字上右键单击并选择": [8],
"在这里输入您的名字": [11],
"幻灯片母版和幻灯片版式": [11],
"而第四个匹配项相似但有所不同": [9],
"文件时才会出现此选项": [11],
"您需要同时指定语言和国家": [5],
"再次提醒注意反斜杠也要转义": [5],
"xml": [11],
"则它们也会从那些程序中消失": [4],
"用户文件文件夹中的": [11],
"快捷键定义语法如下": [3],
"文件或": [1],
"xmx": [5],
"文本文件": [11],
"摘要": [7],
"双向控制字符": [8],
"存储库映射": [[6,11]],
"文件会包含此名称": [6],
"逻辑运算符": [[2,7]],
"befor": [5],
"如果你的系统使用代理服务器": [5],
"在窗格底部会显示": [9],
"由拼写检查器创建和使用": [10],
"外部搜索": [11],
"来启动自动完成器": [11],
"创建": [[8,10]],
"工具": [11,[8,10],[0,3,7]],
"要删除项目专用的分割规则": [11],
"拥有最佳匹配度百分比的那个": [9],
"tar.bz": [0],
"有三种方法可供尝试": [6],
"译文文件也可以用任意编码编码": [11],
"查看相关的应用程序手册来了解详情": [6],
"会找到字符串": [11],
"的应用程序文件夹内的": [5],
"如果查找结果删除了重复项": [11],
"并选中之": [5],
"中定义的译文语言的的语言代码一致": [4],
"这种情况下你很走运": [11],
"如果要使用": [3],
"安装方便的快捷方式": [5],
"转到": [[8,9],7],
"在添加第一个词条时会自动创建之": [1],
"目录中的翻译记忆库": [11],
"xlsx": [11],
"一旦下载了代码": [5],
"要确保": [5],
"文件夹中创建": [5],
"中的句点需要按照正则表达式的规则进行转义": [5],
"远程文件夹或文件的名称": [6],
"就会使用之": [5],
"assembledist": [5],
"版本控制系统的知识": [6],
"详细信息请参阅下一章": [5],
"则此功能非常有用": [6],
"则条目会灰掉": [8],
"在编辑器中": [8],
"对于由一对或一组单词": [11],
"可用选项包括": [11],
"将保留为空": [11],
"不需要": [10],
"如果你已经注册了": [5],
"target.txt": [11],
"那么分享完整的": [6],
"按钮将其移到顶部": [11],
"下一备注": [9],
"打开新的": [8],
"标签视为段落分隔": [11],
"会保留": [11],
"标题式大小写": [3],
"包括": [[6,8]],
"因此": [[5,11],[6,10],[4,8]],
"接受的单词集": [10],
"选项后只需单击即可": [11],
"这会在工作文件夹中创建一个": [5],
"内的等价物是终端": [5],
"文件夹可以包含任意数量的辅助翻译记忆库": [10],
"nameon": [11],
"optionsglossarytbxdisplaycontextcheckboxmenuitem": [3],
"为了避免每次打开译文文件都要更改其显示参数": [6],
"能保存与原文完全相同的译文": [9],
"gotonextnotemenuitem": [3],
"分析要进行查找的文本": [2],
"tar.gz": [5],
"gpl": [0],
"就会出现": [5],
"外部搜索命令": [8],
"只会处理": [5],
"此子文件夹中会逐步添加此文件的备份": [10],
"在标签验证器中它会显示为红色": [11],
"请从主菜单中选择": [11],
"如果您经常使用过去生成的翻译记忆库": [6],
"框内输入新的单词或短语": [11],
"关闭和重新载入时": [6],
"创建的级别": [6],
"而将必须选择参数然后关闭之并刷新翻译活动的窗口称为": [11],
"快捷键": [3,8],
"即空格和换行符": [11],
"系统用户名": [11],
"语句分割是首选的选择": [11],
"azur": [5],
"如果标签与键值对列表匹配": [11],
"您可以稍后跟踪这些翻译检查它们是否正确": [11],
"图标": [5],
"随后可以切回查找窗口处理下一个结果片段": [11],
"制定一条例外规则": [11],
"它也能在": [6],
"演讲备注": [11],
"被设为从右向左": [6],
"查找字符串将被视为正则表达式": [11],
"检查确认替代译文是否正确或向同事征求意见": [9],
"左右的值会它们显示在底部": [11],
"只需一次点击": [5],
"可以用常规的导航操作进行跳转": [11],
"要添加其他资源": [6],
"项目设置等": [6],
"pdf": [6,[7,8,11]],
"有两种方式来分割文本": [11],
"预定义字符类": [[2,7]],
"不过足够简单": [6],
"转换不良的": [11],
"添加新原文文件": [6],
"将会在视觉上指示原文文档中各段落之间的分隔": [8],
"之间": [10],
"toolsshowstatisticsmatchesmenuitem": [3],
"可以将文件过滤器重置为默认设置": [11],
"viewdisplaymodificationinfononeradiobuttonmenuitem": [3],
"被忽略的文件或文件夹": [11],
"我们将可以维持开启放在编辑器旁边并与之交互的窗口称为": [11],
"手动添加相关的格式和项目": [6],
"如果用户系统的语言不可用": [5],
"在另一种语言上使用": [6],
"则可以将其放入": [10],
"文件夹位于项目的根文件夹下": [6],
"它们包含文本格式信息": [6],
"gti": [6],
"如果原文文档中包含非独特片段": [11],
"如果未勾选": [8],
"图表": [11],
"在此输入和编辑翻译": [9],
"要使用现有的术语表": [1],
"换行": [2],
"针对您的译文语言": [4],
"注意": [[10,11],[2,8],[6,9],[4,5]],
"如果你不想用": [11],
"要检查是否所有内容都符合预期": [6],
"两个文件的对齐情况通常需要手工修正": [11],
"projectaccesswriteableglossarymenuitem": [3],
"很显然": [4],
"例如绘图": [6],
"的文件夹中的": [10],
"恢复主窗口": [9,[3,11]],
"仅当两个文件都被识别为": [11],
"指南": [6,[0,7,10]],
"则此功能可让您在词典中查找所选单词": [8],
"regexp": [5],
"是被锁定的": [5],
"如果给定语句已被翻译过一次": [6],
"在每个光标位置处": [11],
"在这个文件夹里你会找到一个": [5],
"用此按钮打开对话框": [11],
"sentencecasemenuitem": [3],
"将其保存为": [6],
"使用这种方法您可以下载": [5],
"标题式大小写或句子式大小写": [8],
"通常": [5,8],
"即并非由": [11],
"uhhhh": [2],
"optionssentsegmenuitem": [3],
"之类的命令行编辑器中打开": [5],
"可以为": [3],
"即使离线也": [5],
"所需的所有东西": [5],
"如果您在翻译中途决定从语句翻译切换至段落翻译": [11],
"选项始终是选上的": [8],
"optionsaccessconfigdirmenuitem": [3],
"译文词汇": [8],
"你可能收到几个安全警告": [5],
"或面板": [5],
"这里面最重要的文件是": [10],
"项目中": [9],
"将此文件夹复制到合适的文件夹": [5],
"则它们在": [3],
"然后需要将译文文件转换回原始格式": [6],
"test.html": [5],
"的翻译": [6],
"包含以不同方式显示文本和修订信息的选项": [11],
"xxx": [10],
"菜单下的条目或按": [11],
"来浏览备注": [9],
"smalltalk": [11],
"如果您希望": [11],
"经常验证标签": [6],
"此功能在特殊情况下会非常有用": [11],
"附录": [[1,2,4],[0,3],6],
"上它被称为": [5],
"译文文件的格式质量取决于来回转换的质量": [6],
"独特片段数": [[9,11]],
"支持面向对象": [11],
"主页": [6],
"术语查询领域": [11],
"会分配": [5],
"访问配置文件夹": [3,[8,11]],
"那么手工复制文件是有意义的": [4],
"密钥": [[5,11]],
"pseudotranslatetmx": [5],
"可通过开始菜单中": [5],
"如果是在打开的片段上右键单击": [9],
"文件过滤器": [11,8,3],
"它还显示着针对当前片段的模糊匹配和词汇表匹配的数量": [9],
"选择上一个匹配": [3],
"中删除了": [10],
"targetlanguagecod": [11],
"最重要的是第二对数字": [9],
"要删除项目专用文件过滤器": [11],
"改进和修复的": [8],
"在现有安装上覆盖安装": [5],
"它可以使用大量可用于这些程序的免费拼写词典": [[4,11]],
"如果在使用程序过程中发生了错误": [5],
"你可以选择安装过程中使用的语言": [5],
"忽略匹配以下正则表达式的文本": [11],
"预定义的断句规则应当足够了": [11],
"通常是大约": [6],
"当您移动前往另一个片段时": [9],
"主题": [11],
"要删除过滤器": [11],
"译文文件": [[3,11]],
"如果您需要在翻译中复现文本格式": [6],
"对于": [6,[1,5]],
"项目文件": [11,[8,9],[3,7]],
"在这个案例中会降为": [10],
"译文文件通常需要具有与原文文件": [11],
"事件": [3],
"将它们复制到安全的地方备用": [6],
"下面显示译文的形式显示": [11],
"于任意窗格中": [8],
"独特片段数量的效果": [11],
"单词边界": [2],
"则不会有任何动作": [8],
"以兆字节": [5],
"则配置文件必须可用": [5],
"自动保存": [6],
"最后一屏让你可以查看所安装版本的自述文件和变更记录文件": [5],
"在原文中": [11],
"用户组词汇表": [9],
"文件夹中没找到词汇表文件": [1],
"文件夹下": [5,11],
"文件夹然后重新载入项目以载入新文件": [8],
"打开最近的项目": [[3,8]],
"实际上是双向": [6],
"这是反斜杠字符": [2],
"从左向右": [6],
"根据你的翻译创建译文文档": [8],
"来设置字符表自动完成器选项": [11],
"如果你拥有的是无法处理": [6],
"它可以单独用于所有三个窗格": [6],
"encyclopedia": [0],
"译者就能用它们来创建中文翻译": [6],
"自定义颜色": [3],
"文件夹中": [9,[8,11],6],
"的字符": [2],
"其它文件格式": [6],
"项目的翻译记忆库中之前的那些有效片段会变为孤立片段": [11],
"用户": [5,7,6],
"optionstagvalidationmenuitem": [3],
"这些文件是无格式的纯文本文件": [11],
"文件来从命令行启动": [5],
"默认为空": [11],
"这些设置是应用到": [8],
"正则表达式": [2,7,11,[3,4,5]],
"pt_br": [4,5],
"a-z": [2],
"这样能够保留片段中的内联格式": [10],
"此对话框可通过选择": [11],
"才能让新的快捷键生效": [3],
"确保将部署的是应用程序的最新版本": [5],
"将片段验证键设置为": [11],
"团队项目可以离线打开并翻译": [6],
"作为诸如": [11],
"允许用户在默认的词汇表文件中创建条目": [8],
"可能是整个段落": [11],
"可以向用户组咨询": [6],
"javascript": [11],
"防止数据丢失": [6,7],
"mediawiki": [11,[3,8]],
"input": [11],
"所选属性会作为片段出现在编辑器窗口中": [11],
"在每次翻译会话开始和结束时": [10],
"为了跟轻松地在查找结果集中导航": [11],
"修改分割规则可能会有改变片段": [11],
"的所有出现之处": [11],
"来标记通过模糊匹配插入的译文": [11],
"框中的下拉箭头可以访问最后": [11],
"复制到备份媒介上": [6],
"如果是": [0],
"的语言的语言使用左对齐": [6],
"框里": [11],
"后窗格未按预期显示时也可以使用它": [11],
"在翻译加拿大法语时": [11],
"不要与": [11],
"表格清单": [7],
"found": [5],
"请在此处输入代理服务器管理员所提供的详细信息": [11],
"要启动多个": [5],
"包含当前项目的当前统计数据": [10],
"你可以使用标准快捷键": [11],
"检查词典文件是否在正确的文件夹中": [0],
"会记住处理过的片段": [8],
"显示在": [11],
"就不会有效果": [9],
"无论项目中有多少词汇表": [1],
"有关此主题的更多信息": [11],
"不匹配任何内容": [2],
"要用法语界面启动": [5],
"片段分割": [11,[3,8]],
"将文件保存为": [6],
"当打开片段时其数据会被导出": [11],
"运行": [5],
"如果在此处添加了新脚本": [11],
"从而导致不理想的字体替换": [8],
"处理的纯文本或格式化文本文件格式": [6],
"googl": [5,11],
"opendocu": [11],
"如果未激活任何机器翻译服务": [8],
"的桌面图标上来打开项目": [5],
"在两侧": [6],
"download.html": [5],
"焦点重新转到查找字段": [11],
"中更改": [[6,8]],
"注意出现在通常的": [5],
"文本": [[6,11]],
"另一种方法是输入与原文相同的译文": [11],
"sourceforg": [3,5],
"所有未被排除的文件都会被包含": [6],
"或无需翻译的第三方语言片段的文档非常有用": [9],
"需要": [10],
"则词典文件必须命名为": [4],
"参见下文的": [5],
"右键点击带下划线的词汇": [4],
"上一个": [11],
"在句点和下一个句子开头之间是否少了个空格": [2],
"第二个则是": [3],
"如果在第": [10,6],
"以下变量相应的结果如下": [11],
"下一个备注": [[3,8]],
"editmultipledefault": [3],
"移动到上一个片段": [8],
"如果你用控制台模式启动": [5],
"mozilla": [5],
"并在上下滚动时渐进式地加载更多片段": [11],
"editfindinprojectmenuitem": [3],
"深色": [11],
"用以指定译文数据的位置": [5],
"warn": [5],
"这对于在服务器上运行": [5],
"之外的任何字符": [2],
"显然": [[9,10]],
"technetwork": [5],
"如果出于某种原因": [4],
"并且文件将会以": [1],
"在原文文本中相匹配的文本会显示为斜体": [11],
"plural": [11],
"标记非独特片段": [[3,8]],
"激进式字体候补": [8],
"词汇原文可以是多单词词汇": [1],
"按钮": [11],
"转到下一个未译片段的功能在有多重译文的片段处也会停下": [11],
"下一个片段或依赖于文件格式的某种片段标识": [11],
"对于绝大多数欧洲语言和日语来说": [11],
"要将一条规则定义为断句规则": [11],
"关闭": [11,[3,6,8]],
"其他图标": [5],
"创建不同译文": [[3,8,9,11]],
"设置为": [[3,11]],
"只要": [8],
"项目发布到服务器上": [6],
"更改会体现出来": [10],
"另一种选择": [11],
"并可以在": [5],
"一个典型的例子是多国立法": [6],
"windows": [7],
"不知道": [5],
"colour": [11],
"n.n_windows.ex": [5],
"标签验证器会处理所有片段并将关于所有带有无效标签的片段的警告信息写入指定文件": [5],
"所显示的文本被划分为片段": [9],
"在一行上双击将在编辑器窗格中激活相应的片段": [8],
"该对话框包含一个专门的译文文件名模式编辑器": [11],
"请双击位于": [5],
"以及为脚本分配快捷键": [8],
"字符表": [[3,11]],
"您可以通过选择": [9],
"此片段在译文文档中不会显示任何东西": [8],
"中来无条件地覆盖现有的默认翻译": [10],
"修改": [3],
"会仅基于原文文件来为整个项目创建一个": [5],
"远程文件": [6],
"文件位置": [11],
"关于细节": [11],
"program": [5],
"私钥": [5],
"文件所做的所有更改": [5],
"这是标准行为": [5],
"替换掉整个译文片段": [8],
"脚本存储在位于": [11],
"并用你的翻译替换或修改编辑区域的内容": [9],
"词汇表的词条和文档中的原文文本之间并不是": [1],
"在上述情况下": [9],
"译文语言": [11],
"越低": [11],
"给定项目的文件过滤器集保存为": [11],
"如果希望词汇表中显示有着相同词根的词汇": [11],
"的脚本提供了一套默认的": [11],
"那么您应该使用模式": [11],
"请使用它": [11],
"这将以": [5],
"窗格中的最接近匹配是用词干分析来确定的": [11],
"以及使用的是正确版本的": [5],
"例如把": [4],
"安装程序会询问你是否要在": [5],
"已实现以下脚本语言": [11],
"它们具有": [11],
"当手动或自动插入模糊匹配时": [11],
"如果需要更改设置": [5],
"解决方案是将已有的翻译记忆库复制到": [6],
"编辑": [11,8,9,[1,5,7]],
"n.n_mac.zip": [5],
"常见词汇表问题": [1,7],
"如果指定了": [5],
"客户端添加要翻译的文件": [6],
"项目的原文文件夹": [6],
"在输入片段的序号或编号后就会打开该片段": [8],
"必须指定有效的": [5],
"按键": [3],
"然后启动": [5],
"选项菜单": [3,7],
"在列表中选定此过滤器并点击": [11],
"添加脚本": [[8,11]],
"将会翻译所有": [5],
"并按": [11],
"theme": [11],
"命令行窗口也称为": [5],
"原文文件和译文文件编码": [11],
"同时": [11],
"pseudotranslatetyp": [5],
"在加载项目时会忽略相应的文件": [11],
"外观": [11],
"但是只能在团队项目的上下文环境中添加": [6],
"或更高版本的一部分": [5],
"将窗格放回主窗口内": [9],
"要插入和删除的内容": [11],
"文件来反映你的偏好设置": [5],
"中将": [11],
"循环切换": [3],
"下允许多个": [5],
"检查问题": [8],
"这里可以找到一些其他脚本": [11],
"它说明了目前您相对于总量": [9],
"精确": [1],
"将维护整个内容同时维持树形结构不变": [10],
"在某些操作系统中": [10],
"标题中会包含所使用的查找词": [11],
"projectclosemenuitem": [3],
"模糊匹配窗格": [9],
"自动检查文本的拼写": [4],
"viewmarknonuniquesegmentscheckboxmenuitem": [3],
"匹配的序号": [11],
"具体取决于上下文": [9],
"词汇表": [[3,11],[1,9],7,[0,4,8,10]],
"要启动": [5],
"将文件复制到": [6],
"的默认过滤器": [6],
"确定被强制更改的片段的豁免权": [10],
"findinprojectreuselastwindow": [3],
"某些专门用于翻译工作的文件格式": [9],
"readme.txt": [6,11],
"有两种方式可以完成这一点": [6],
"这种手动字体候补机制似乎会干扰标准字体候补": [8],
"languagetool": [11,8],
"文档": [11],
"source.txt": [11],
"传统": [5],
"files.s": [11],
"在一些少见情况下": [11],
"你还可以指定国家": [5],
"exchang": [1],
"示例参见": [11],
"每个文件条目包括其名称": [11],
"其间没有可翻译文本的多个标签将被聚合成单个标签": [11],
"译文": [0],
"currseg": [11],
"文本查找": [11,7],
"单类": [2],
"量词": [[2,7]],
"由于这是": [11],
"比如像网站中": [10],
"point": [11],
"对于内部服务器上的同步项目使用较短的间隔": [11],
"文件夹下的": [5],
"要创建项目专用的文件过滤器集合": [11],
"中的文本输入与显示都发生变化": [6],
"项目文件夹中": [1],
"需要使用要身份验证的代理服务器来访问因特网": [11],
"有两种方法": [5],
"请选择片段后按下": [11],
"转到片段": [9],
"在译文文档中": [11],
"放入": [10],
"个匹配的条目而非所有符合查找约束条件的条目": [11],
"检查的内容与上一条一样": [2],
"downloaded_file.tar.gz": [5],
"哪些文件会用来进行对齐取决于文件过滤器是否支持它": [5],
"使用拼写检查器": [[4,7]],
"项目文件夹下": [1],
"的信息": [6],
"且仅对当前项目有效": [11],
"则包含项目专用的片段分割规则": [10],
"自动在词典中查找": [8],
"只需在创建项目后将它放到": [1],
"如果你想再次使用此过滤器": [11],
"account": [11],
"将进一步把这些块分割为语句": [11],
"文件时": [[6,10]],
"此方法也会使用你在": [5],
"译文会显示为": [8],
"您需要不时地执行该操作": [4],
"窗格将作出相应的反应": [9],
"dhttp.proxyhost": [5],
"目录中的文件与在指定位置出现的内容对齐": [5],
"语言环境": [5],
"为避免丢失重要数据": [6],
"将其导入": [8],
"在任何时候": [11],
"插入原文文本": [11],
"原文与译文的编码字段使用的是包含所有支持的编码的下拉菜单": [11],
"已译文件的编码与原文文件编码相同": [11],
"都会在此处生成": [10],
"可以访问可用插件列表": [11],
"选择": [11,[4,5],8],
"匹配任意单个字符": [11],
"但已足够接近并且在其他支持": [6],
"如果此文件不包含映射": [6],
"移动或隐藏了一个或多个组件后无法恢复到所需的排列时可以使用此功能": [11],
"如果对齐情况看起来尚可改进": [11],
"模糊匹配排序依据": [11],
"百万个字符": [5],
"创建译文文档": [11,[3,8,10]],
"输入": [5,[6,11]],
"应该有一个断句规则": [11],
"等流行软件背后的语言": [11],
"也许会找不到您已翻译的片段": [6],
"对文件进行查找": [11],
"精确查找": [11],
"见上文": [[5,6]],
"configur": [5],
"已译片段会以上面显示原文": [11],
"一旦已经用其内容填充了": [6],
"unicode": [7],
"最小化窗格": [9],
"可写词汇表": [3],
"回车": [[2,11]],
"升级": [5],
"这实际上会让它们回到": [11],
"optionsworkflowmenuitem": [3],
"用户手册": [[3,8]],
"如果用键盘而不用鼠标是无法在原文片段中选择单词的": [9],
"之前的": [5],
"见下文": [[5,11]],
"releas": [6,3],
"还可以用来删除当前片段的现有译文": [11],
"请删除项目文件夹或将其或存档到工作区外的其他位置": [6],
"中应当可用": [3],
"框的那些行": [11],
"标签": [6,11],
"sparc": [5],
"然后输入替代译文": [8],
"只需将相应的两个文件复制到当前项目的": [10],
"如果给出": [5],
"虽然它们的原始翻译依然存在": [11],
"允许用户选择自动保存项目的间隔": [11],
"来将其插入到光标位置处": [9],
"文件现在包含你复制到原文文件夹中的文件的所选语言对的翻译": [6],
"聚合标签": [11],
"在自动处理后": [11],
"项目经理都必须发给译者他们用来访问的存储库的": [6],
"可以内含备注": [9],
"其中项目是顶层对象": [11],
"默认情况下": [11,6,5,[2,9]],
"替换为当前片段的原文": [11],
"定期将": [6],
"命令应当是": [5],
"如果此语言的用户界面可用": [5],
"原文与译文片段的对齐方式取决于项目的语言": [6],
"系统中": [3],
"可接受空行": [3],
"subdir": [6],
"删除过滤器": [11],
"由于在更改过滤器选项后文本将以不同的方式进行分割": [11],
"例如在上面的例子中是": [6],
"必须维持使用从左向右的原文语言的产品名称": [6],
"技术": [5],
"来更改选项": [5],
"除语言外": [5],
"此选项显示": [8],
"并勾选": [4],
"forward-backward": [11],
"也可以用主菜单中": [11],
"翻译编辑": [9],
"file-source-encod": [11],
"放到": [10],
"在左侧面板的列表中点击脚本名称将其加载到编辑器中": [11],
"some": [6],
"或导出新的所选部分": [11],
"都是": [6],
"当您移动至下一片段时": [9],
"编辑器一开始会显示": [11],
"备注": [[9,11],7],
"如果选择了多个翻译引擎": [8],
"为了加快此进程": [6],
"以控制台模式启动并自动执行以下服务之一": [5],
"在不同显示模式之间切换": [6],
"而非墨西哥西班牙语": [4],
"其位置并不重要": [5],
"通过清除译文片段并按下": [11],
"你也可以用": [5],
"打开存储着": [8],
"导入": [11],
"可以点击": [5],
"editexportselectionmenuitem": [3],
"项目无法访问你的工作或信息": [5],
"因为新项目与之前的项目类似": [6],
"参见正则表达式中": [11],
"减法": [2],
"其他拖进去的文件会被复制到": [9],
"语言文件": [5],
"home": [6],
"主菜单和状态栏组成": [9],
"来编辑": [11],
"projectaccesstargetmenuitem": [3],
"来自项目可写词汇表的词汇会使用粗体显示": [1],
"中所列的那些": [8],
"显示用于修改文本显示字体的对话框": [11],
"会匹配": [2],
"下载文件": [0],
"映射参数": [6],
"拼写问题": [8],
"在译文文档中删除": [11],
"可以用脚本设置预定义主题": [11],
"译文文件中的": [11],
"翻译中用到的正则表达式示例": [[2,7]],
"选择要进行对齐的两个文件": [8],
"如果用户的操作系统是俄语的且": [5],
"aligndir": [5],
"其中多个译者共享": [6],
"system-host-nam": [11],
"action": [8],
"接着是法语规则": [11],
"creat": [11],
"命令回到当前片段": [8],
"python": [11],
"隐私": [5],
"es_mx.dic": [4],
"分割规则通常适用于所有项目": [11],
"一个片段和另一个相同的片段可能会因为上下文而需要不同的译文": [8],
"infix": [6],
"可以手工创建和更新之": [1],
"来对尚未翻译的片段同样执行查找与替换": [11],
"会提醒原文和译文片段之间的标签差异": [11],
"可以通过查看其标题来快速确认其内容": [11],
"tarbal": [0],
"识别有替代翻译的片段时忽略文件上下文": [11],
"第二条命令是实际启动": [5],
"查找以下内容": [2],
"是一样的": [6],
"应用程序": [5],
"终止替换操作": [11],
"的行为": [5],
"你的文档和翻译记忆库都留在你的计算机上": [5],
"文档中列出了可能的按键事件": [3],
"正则表达式文档": [2],
"但又具有受": [11],
"项目的翻译记忆库将输出为": [6],
"模式也对光标右侧的文本有效": [11],
"file": [11,[5,6]],
"位于名为": [10],
"或包含与此待译字符串最相似的字符串的翻译": [9],
"状态栏在主窗口的底部显示有关工作流的信息": [9],
"键将其打开进行翻译": [11],
"的过时": [6],
"按钮来改变文件名的位置": [11],
"翻译记忆库": [11],
"并不要求相应文件已存在": [1],
"相关文件是": [5],
"menu": [9],
"在历史中前进": [8,3],
"与词典相关的问题": [0,7],
"要更改启动命令": [5],
"导出": [6],
"激活": [[10,11]],
"安装文件夹": [11],
"列出了所有在": [11],
"扩展名分别为": [0],
"a-za-z": [2,11],
"在正则表达式中有特殊含义": [11],
"上述两个命令可以包含到一个文件内": [5],
"如果我们想要避免在具有多个可能译文内容的片段上出现错译": [11],
"将其显示在结果文本框中": [2],
"词典在以": [4],
"使用": [6,11,5,[4,7]],
"会用相关错误信息协助进行错误排除": [6],
"会将查找限制在具有原文文件格式的文件内": [11],
"已正确设置": [5],
"要解压缩": [5],
"source-pattern": [5],
"生成时的系统日期时间": [11],
"若要为新的语言模式创建空的规则集": [11],
"内的": [5],
"验证标签": [3],
"然而": [[6,11]],
"也会找到": [11],
"用词干分析": [9],
"如果当前片段是文件中的第一个片段": [8],
"为译文的设置下将无作用": [4],
"显示非": [11],
"在默认": [3],
"但差异": [11],
"当你完成了翻译后": [8],
"在此之前会关闭已打开的项目": [9],
"警告": [11,6],
"即按段落分割": [11],
"匹配窗格设置": [[7,11]],
"true": [5],
"某个文件可能没有匹配的条目": [11],
"且其位置必须位于": [1],
"多重译文": [9,7],
"在下一个此类片段处停下": [11],
"这一行": [5],
"groovi": [11],
"在译文片段中可通过快捷键": [11],
"插入最佳模糊匹配": [11],
"右键点击面板": [5],
"用于重命名文件的脚本": [11],
"用位于": [5],
"如果文件名存在疑义": [11],
"kmenueditor": [5],
"子文件夹中的翻译记忆文件": [6],
"或用": [[5,9]],
"基于": [11],
"在这个例子中": [6],
"代表句点字符": [11],
"选择性共享": [6],
"在这里输入你的": [5],
"请按": [11],
"在静默模式下": [5],
"添加模式和编辑特定模式使用相同的对话框": [11],
"但以下几点除外": [6],
"您可以手动输入或者通过下拉菜单选择原文与译文语言": [11],
"master": [6],
"kmenuedit": [5],
"创建当前译文文档": [[3,8]],
"则不显示原文片段": [8],
"然后点击词典文件文件夹字段旁边的": [4],
"将三份": [6],
"标记双向算法控制字符": [[3,8]],
"在名称": [5],
"xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx": [5],
"仅当打开了项目后此菜单下才会显示条目": [8],
"writer": [6],
"模糊匹配窗格或词汇表窗格中选择单词或短语": [11],
"dalloway": [11],
"中将设置文件复制到特定文件夹": [5],
"不做任何改变": [11],
"rubi": [11],
"部分删除": [11],
"扩展名的": [6],
"而未译片段只显示原文": [11],
"的已安装文件": [4],
"片段集合是通过将所有片段去除重复片段计算得到的": [11],
"或选择了": [11],
"导入和导出翻译记忆库": [6],
"按堆": [11],
"对于放在名为": [10],
"一个接一个": [11],
"将其关闭": [11],
"如果辅助翻译记忆库中有与待译文本完全相同的原文片段": [10],
"的情况下": [6],
"获取默认": [3],
"建立团队项目": [6,7],
"可以使用默认的": [6],
"节和表被设为双向": [6],
"选择能提供最佳结果的那一个": [11],
"上述片段其中之一是当前片段": [9],
"按键事件说明": [3],
"则非独特片段会被标记为淡灰色": [8],
"user.languag": [5],
"regex": [2,7],
"从命令行启动": [5,7],
"meta": [[3,11]],
"中任何地方选择一个单词": [5],
"文本元素": [6],
"格式的示例": [11],
"查找可能会被限制为": [11],
"仅有少量文件格式指定强制编码": [11],
"自动传播翻译": [11],
"命令就应该是": [5],
"作为纯文本文件处理": [6],
"所支持的文件格式是": [8],
"的控制字符": [2],
"你可以通过从主菜单中选择": [11],
"词汇表是术语文件": [1],
"可以读取其他工具生成的翻译记忆库": [6],
"上下文菜单优先级": [11],
"在进行此类转换之前": [6],
"除了以下情况": [2],
"可通过": [[3,11]],
"因为全部独特片段均已翻译": [9],
"分钟的间隔可以在": [[6,8]],
"最近变更": [[3,8]],
"同时允许每个项目自有外部命令": [11],
"可以用": [11],
"前缀": [10],
"必要时可以编辑词典文件名或更改项目的语言设置": [4],
"对于例外规则": [11],
"名称为": [5],
"包含全部关键字": [11],
"但其译者并不懂荷兰语": [6],
"如果你使用了": [5],
"相混淆": [11],
"已译片段将会标记为黄色": [8],
"并且将按请求自动处理给定项目": [5],
"头并保持其不变": [11],
"ibm": [5],
"另一方面": [11],
"替换为机器翻译": [[3,8]],
"试图打开已经在另一个": [5],
"例如来自于": [4],
"必须转换为从右向左模式": [6],
"parsewis": [11],
"文件示例保留了": [5],
"命令式和函数式编程风格": [11],
"所在文件夹的名称": [5],
"主窗口": [[7,9]],
"浏览并选择您为词典创建的文件夹": [4],
"中来更改已分配的快捷键并添加新的快捷键": [3],
"请直接修改字段名或点击": [11],
"例外复选框": [11],
"如果为每个词条所显示的上下文描述并非必要或者太长": [11],
"日志": [[3,8]],
"因此会显示空白": [11],
"关闭项目": [6],
"如果文档不可用": [8],
"描述了配置对": [11],
"或者使用了": [5],
"然而在许多情况下": [6],
"要显示词汇表匹配项的": [1],
"文件中读取": [5],
"该规则集内的规则将显示在窗口的下半部分": [11],
"idx": [0],
"字段来在线查找词典会更简单": [4],
"因此应当为后面跟着句点的": [11],
"模糊": [10,11],
"译文文件的子文件夹等": [11],
"将当前选中内容导出至文本文件进行处理": [8],
"当前片段的译文内容": [11],
"文件名模式": [11],
"文件是按照名称顺序进行读取的": [10],
"projectaccesscurrentsourcedocumentmenuitem": [3],
"如果该文件已存在": [1],
"linux": [5,[2,7,9]],
"随后选中的语言将会安装": [4],
"的所有内容都映射到本地": [6],
"此处的信息仅适用于": [5],
"而非项目的过滤器": [11],
"模式对于光标左侧的文本有效同时相应的": [11],
"file.txt": [6],
"在这种模式下": [9],
"下半部分是编辑区域": [9],
"显示每个新版本的新功能": [8],
"ifo": [0],
"这是一条例外规则": [11],
"包含最新的项目匹配统计数据": [10],
"其右上角会显示不同的符号": [9],
"其他平台则是": [1],
"包里包含有运行": [5],
"将运行所选脚本": [11],
"之前的所有字符": [2],
"窗格部件": [[7,9]],
"来启动所选的宏": [11],
"就是当前工作文件夹": [5],
"xx.docx": [11],
"首次使用团队项目时": [6],
"以及包中的": [5],
"分割设置": [11,8],
"它会维持未译状态": [11],
"你可以为标签定义要维持不翻译的键值对": [11],
"会尝试根据原文内容转换模糊匹配中的数字": [11],
"编辑器窗格头部": [11],
"则会重新应用现存的本地映射": [6],
"修饰键": [3],
"自动获取翻译": [11],
"稍后可以用下面的": [8],
"optionsautocompleteautotextmenuitem": [3],
"而是由译者自行复制文件": [6],
"所有数字都会被视为标签": [11],
"zip": [5],
"可以用您所最熟悉的语言发帖": [6],
"打开生成的": [8],
"会导致": [6],
"创建此存储库的本地副本": [6],
"中的那些类似": [11],
"当你分离": [11],
"的统计数据是一致的": [6],
"在硬盘上方便的位置为特定客户或者主题的翻译记忆库创建一个文件夹": [6],
"如果规则匹配": [11],
"资源包过滤器已经能处理": [11],
"来代替": [3],
"你可能已经完成了一个项目": [6],
"这会将所选择的文件复制到": [11],
"如果返回了": [5],
"concis": [0],
"词汇表文件没有采用正确的编码": [1],
"查找": [11,8,2],
"对齐文件": [[8,11]],
"如果按默认值": [10],
"复制文本并将其粘贴到": [8],
"完全有能力处理": [11],
"term.tilde.com": [11],
"只需要勾选相应复选框即可": [11],
"但进行细微修正": [11],
"分割设置首选项": [11],
"但结束由": [2],
"重命名当前的": [6],
"如果你只需要文本信息": [6],
"viewmarknotedsegmentscheckboxmenuitem": [3],
"分割规则设置只影响": [11],
"页脚": [11],
"静默选项": [5],
"小写": [[3,8]],
"如果您的机器很强力": [11],
"但其中的所有设置都会被保留": [5],
"如果您已经在电脑上安装了某个版本的": [5],
"表达式": [11],
"个字符": [5],
"打开它": [6],
"gotomatchsourceseg": [3],
"为所有片段显示": [3],
"optionssaveoptionsmenuitem": [3],
"excel": [11],
"团队项目也可以以不同的方式建立": [6],
"这让重命名要翻译的文件成为可能": [6],
"runn": [11],
"读取词汇表时使用的编码取决于文件扩展名": [1],
"在这种情况下": [6,[5,11],9],
"所组成的条目": [11],
"创建词条": [3,[1,8,11]],
"stardict": [0],
"你可以通过将快捷键定义文件放在": [3],
"omegat.l4j.ini": [5],
"span": [11],
"是月份内的日期": [6],
"可以使用": [5],
"space": [1],
"但针对辅音": [2],
"下一个已译片段": [[3,8]],
"按钮来显示所有相应的出现之处": [11],
"插件安装在": [11],
"启动器正确关联": [5],
"以上信息仅适用于带有文本层的": [6],
"thunderbird": [4,11],
"editselectfuzzy3menuitem": [3],
"检查是否已经发布了更新版本的": [8],
"原文文件编码": [11],
"双击": [5],
"目录中本地创建的文件上传到存储库": [8],
"fals": [[5,11]],
"project.projectfil": [11],
"使用老电脑的用户如果觉得改变窗口大小非常慢可以尝试更改字体": [11],
"文件夹中的": [11,10],
"窗口的可用脚本列表中": [11],
"词汇表文件是简单的纯文本三列列表": [1],
"词汇表的上下文描述": [[3,11]],
"此对话框列出了可用的文件过滤器": [11],
"把你要查找的单词或短语输入": [11],
"功能需要因特网连接": [4],
"需要重新输入": [11],
"程序识别文本并将其转换为": [6],
"使用本地电脑上与": [11],
"保持片段为空": [11],
"如果它们与系统快捷键不冲突": [3],
"结尾": [8],
"关闭窗口": [11],
"和密码": [6],
"全局": [11],
"shortcut": [3],
"如果尚未保存项目": [8],
"允许您激活": [8],
"配置": [11],
"决定如何处理外来": [11],
"分钟以内": [6],
"pt_br.aff": [4],
"tmx2sourc": [6],
"ini": [5],
"的时候被高亮选定的文本": [11],
"片段按在项目中出现的顺序显示": [11],
"文件的名称必须与项目属性对话框": [4],
"您可以在此处重新定义用于保护登录详细信息和机器翻译服务访问密钥的主密码": [11],
"查找文件": [11],
"首选项": [8,[6,11]],
"dhttp.proxyport": [5],
"有一个内置的拼写检查器": [11],
"存档": [0],
"条目启动": [5],
"例如因为它们属于同一主题或者同一客户": [6],
"移动": [11],
"视为不同": [11],
"subrip": [5],
"私有词库显示为粗体": [11],
"发送项目网址并要求译者用": [6],
"如果碰到问题": [6],
"指定不应分开的文本部件": [11],
"score": [11],
"用户国家": [5],
"并选定其全部内容": [11],
"如果你希望词典中显示有着相同词根的词汇": [8],
"文件放到": [5],
"其中包含若干文件和子文件夹": [10],
"文件夹中且": [11],
"可以处理的格式": [6],
"raw": [6],
"当你下载项目时": [6],
"但反之则不然": [11],
"aaa": [2],
"搜索所需的语言组合": [0],
"contemporari": [0],
"solari": [5],
"如果文档允许": [6],
"在光标位置插入": [8],
"要向可写词汇表中添加新词汇": [1],
"离线工作": [6],
"项目的默认可写词汇表则总是以": [1],
"这取决于并发运行的": [8],
"转到菜单": [3,7],
"会打开一个弹出菜单": [9],
"请将此": [10],
"片段非独特时片段标记会显示": [9],
"如果项目使用专用过滤器和片段分割参数": [6],
"实际上只会同步两个文件": [6],
"见前文": [10],
"要求使用法语作为用户语言并且将国家设置为加拿大": [5],
"可以在": [6],
"中使用从右向左模式对于": [6],
"会添加所有": [6],
"要首次通过": [5],
"还会提供与": [9],
"程序将在关闭前要求确认": [11],
"您可以用": [9],
"不支持正则表达式": [11],
"尾注": [11],
"abc": [2],
"rcs": [6],
"针对": [11],
"并检查译文文件是否包含最新版本的翻译": [6],
"脚本名称": [8],
"匹配部分将以蓝色粗体显示": [11],
"整个项目的片段总数": [11],
"点击": [11,[4,8]],
"匹配查看器中显示了翻译记忆库中最相似的片段": [9],
"导出翻译记忆库": [6],
"位于项目的": [11],
"此命令将文件夹更改至包含可执行": [5],
"可以将文件拖放到各个窗格中": [9],
"那么意味着你忘记把它解压缩": [0],
"原文为比如说荷兰语": [6],
"未译片段会维持原文语言": [10],
"以一个标记结尾": [9],
"iso": [1],
"程序不会启动": [5],
"一条给定的原文片段可能需要多种不同翻译": [9],
"说明": [5],
"当前译文文档": [3],
"新行": [2],
"则此处会列出这些命令": [8],
"更改语言可能会导致当前使用的翻译记忆库变得无用": [11],
"排除孤立片段等": [11],
"的文件夹中的翻译记忆库": [10],
"glossary.txt": [6,1],
"重新对齐剩余部分": [11],
"原文片段是": [9],
"并可以定义自定义标签": [11],
"快捷键自定义": [3,7,2],
"编辑器会只显示这": [11],
"你还可以通过词典功能访问以下信息": [0],
"add": [6],
"对项目所做的其他本地更改都会被回退还原": [6],
"会在所有片段上显示此信息": [8],
"文件来修改": [5],
"均有": [6],
"对话框": [[8,11]],
"这样荷兰语的原文片段会以英文翻译的形式显示给译者": [6],
"如果有需要": [9],
"可从": [3],
"将源文件导入": [6],
"保留在": [10],
"文件是否受版本控制": [6],
"不显示": [[3,8]],
"optionsautocompleteshowautomaticallyitem": [3],
"标志": [[2,7]],
"则将跳过": [11],
"来创建服务或者脚本来自动化常用动作": [5],
"添加词条": [9],
"使用以下步骤": [11],
"如果已创建": [11],
"而非": [[2,5]],
"larouss": [9],
"可以识别出您已经翻译过了": [11],
"代表分配的内存量": [5],
"untar": [0],
"先确认勾选了拼写检查器对话框": [4],
"那么可以使用以下步骤将项目恢复到最近保存的状态": [6],
"高级选项": [11],
"因为它能更好地与之前的翻译相匹配": [11],
"filters.conf": [5],
"已翻译的文本会显示译文": [9],
"中出现的所有输入字段中使用": [6],
"建议翻译由词汇表提供": [9],
"有用的流程可以是这样": [6],
"其中列出了更正建议": [4],
"则不会添加任何其他翻译单元": [10],
"移除译文": [[3,8,11]],
"显示当前项目的匹配统计数据": [8],
"然后保存文件并重新启动": [3],
"要在译文中使用选定的匹配项": [9],
"如果希望重用过往项目的翻译记忆库": [6],
"在源代码文件夹中打开命令行并输入": [5],
"登记全同译文": [[3,8]],
"类似地": [11],
"文档中定义": [3],
"clone": [6],
"总量": [9],
"targetlanguag": [11],
"来添加模式": [6],
"在解压得到的文件夹中名为": [5],
"模式": [9],
"验证当前文档标签": [3],
"properti": [5],
"拼写检查器设置": [[4,7]],
"以插入建议": [11],
"editselectfuzzyprevmenuitem": [3],
"翻译记忆库文件之一从项目的根文件夹复制到仓库文件夹": [6],
"可以通过写入": [11],
"如果您希望文本过滤器处理": [11],
"simpledateformat": [11],
"用原文替换掉整个目标片段": [8],
"仅当你明白自己正在做什么且仅针对来自可信来源的项目时才激活此选项": [11],
"在处理团队项目时": [11],
"默认快捷键": [3],
"可以通过": [6],
"使用特定的拼写词典": [4],
"个或": [3],
"显示原文片段": [[3,8]],
"文件放入": [10],
"代表期望语言的两位代码": [5],
"script": [11],
"选择第三个匹配": [3],
"system": [11],
"spellcheck": [4],
"不会加载图形界面": [5],
"配置文件的本地目录": [8],
"有两个选择": [5],
"请注意": [6,5,[0,4,11]],
"如果禁用了": [8],
"您还可以再选择": [11],
"会显示所选语言所对应的分词器": [11],
"则光标会移动而不插入片段分割符": [11],
"local": [6,5],
"项目记忆库": [11],
"懒惰": [[2,7]],
"改变大小写": [8],
"如果输入的文件夹名称尚不存在": [11],
"在您的": [5],
"就可能必须在从右向左和从左向右模式之间来回切换": [6],
"更改此标志不会影响现有翻译记忆库中的片段": [11],
"它应当是个": [0],
"已翻译的片段以黄色显示": [9],
"在适当的位置创建新文件夹用来保存拼写词典": [4],
"拼写检查器根据译文语言代码来确定要使用的语言": [4],
"翻译提示": [[3,9]],
"原文文件类型": [11],
"则会关闭此选项": [8],
"上是": [11],
"repo_for_all_omegat_team_project_sourc": [6],
"才能使此更改生效": [11],
"分配更多内存": [5],
"在菜单": [11],
"在显示出现次数的确认窗口之后": [11],
"仅供": [4],
"从另一台机器上安装的其他": [5],
"的按钮": [11],
"非数字": [2],
"批注": [11],
"此处为": [6],
"es_mx.aff": [4],
"对话框内转到该仓库文件夹": [6],
"快捷键中": [3],
"映射存储库": [6],
"文本导出功能会从当前": [11],
"此文件夹包含直接连接到远程服务器的项目树结构的版本副本": [10],
"如果关闭了此选项": [11],
"它允许为所有翻译项目设置参数": [11],
"并分配": [5],
"mode": [5],
"则含有替代译文的片段会保持未译状态直到用户决定使用哪种译文": [11],
"八进制值为": [2],
"实用工具": [5],
"来代替默认的": [11],
"toolsshowstatisticsstandardmenuitem": [3],
"响铃": [2],
"单词和字符的重复": [8],
"所以在任意控制台上都可运行": [5],
"read": [11],
"alt": [[3,5,11]],
"默认的可写词汇表位于": [1],
"它是多范式语言": [11],
"此按钮让你能够浏览并下载已有的那些针对项目原文语言与译文语言的词库": [11],
"以及其他东西": [6],
"兼容": [5],
"默认词汇表": [1,7],
"在线词典的网址": [4],
"如果对于给定片段没有发生替换": [11],
"在译文文档中压缩空白字符": [11],
"上的位置": [5],
"即整个项目的总数据和项目中每个文件的总数据": [8],
"词汇表功能使用词干分析来查找匹配项": [1],
"作为新项目的名称": [5],
"将由于题材": [10],
"而对于从右向左": [6],
"账户": [5],
"更多信息关于分割规则的信息": [11],
"并且输出文件是纯文本文件": [6],
"对话框以编辑项目语言和文件夹位置": [8],
"这些过滤器将与项目一起存储": [11],
"必须包含项目的目标语言译文": [5],
"目录下的词汇表": [11],
"可能的原因": [1],
"请执行以下操作": [11],
"如果文件过滤器指定": [11],
"指定译文片段是要与原文片段相同还是留空": [5],
"位置": [5],
"来添加映射": [6],
"不是": [6],
"扩展数字": [11],
"书签": [11],
"例如词典过长的情况": [11],
"and": [[5,11]],
"可能需要一段时间": [4],
"但必须在自动保存之前以及项目被重新载入或关闭之前执行": [6],
"项目中所有可翻译文件的列表": [11],
"你也可以稍后通过将": [5],
"如果它们": [10],
"在安装开始时": [5],
"ant": [[6,11]],
"请在列表中选中此过滤器然后点击": [11],
"号之前显示的是词汇原文": [1],
"会采用最后一个": [10],
"对话框窗口下半部分的复选框用于以下目的": [11],
"可选": [8],
"创建本地副本": [6],
"将要使用的词典文件放到此文件夹中": [4],
"书签引用": [11],
"第二列和第三列都是可选的": [1],
"最好是使用": [10],
"文件的内容在打开新片段": [11],
"helplastchangesmenuitem": [3],
"译文文件名": [11],
"omegat.ex": [5],
"则根本不会保存任何强制修改": [10],
"窗格将在顶部显示标签卡": [9],
"sourcetext": [11],
"来指定": [3],
"不同步项目内容": [5],
"脚本窗口让您可以将现有脚本加载到文本区中并针对当前打开的项目运行它": [11],
"翻译记忆库可以作为一个中转来帮助生成": [6],
"如果未选择": [8],
"跳转到非独特片段的另一个实例": [9],
"如果原始文档是从左向右的": [6],
"如果从一开始就非常确定某些翻译记忆库中的翻译是完全正确的": [10],
"移动到": [8],
"english": [0],
"jar": [5,6],
"api": [5,11],
"创建译文文档时": [6],
"包括在片段开头和结尾处的标签": [11],
"editselectfuzzy2menuitem": [3],
"翻译文件时": [6],
"对话框中定义的项目参数": [10],
"查找词": [11],
"请右键单击": [5],
"在原文片段中查找": [11],
"匹配的原文文本": [11],
"无法正确显示某些字形": [8],
"算法是两种不同的计算方法": [11],
"此处": [11],
"将其打开": [11],
"会注意大小写": [11],
"帮助": [8,7],
"文件但不共享": [6],
"从灵活性的角度看": [11],
"数据图表": [11],
"打开之前创建的项目": [8],
"这在处理内联格式并不真正有用的文本": [11],
"开头": [3],
"此外": [4],
"请定期创建译文文件": [6],
"此窗口可通过选择": [11],
"文件": [6,11,5,10,[1,7],8],
"中所用的": [11],
"将当前项目转换为": [8],
"editselectfuzzynextmenuitem": [3],
"因为新的安装可以使用已有的": [5],
"此译文会进行验证并保存": [9],
"read.m": [11],
"来整个删除标签": [11],
"这也可以在": [6],
"readme.bak": [6],
"可以提高此数字": [11],
"因为它们都将被删除": [11],
"执行此文件时": [5],
"下一个或第": [8],
"art": [4],
"要翻译": [8],
"对于预定义的选项": [5],
"如果选用了语句分割": [11],
"rtl": [6],
"来查看它": [10],
"勾选此选项后": [11],
"对于变量": [11],
"出现一次或不出现": [2],
"此信息适用于诸如": [5],
"开始的转义": [2],
"jdk": [5],
"下载安装包": [5],
"文件自动提供给项目会非常有用": [5],
"内存": [5],
"中发帖": [6],
"实例打开同一个项目": [5],
"toolsshowstatisticsmatchesperfilemenuitem": [3],
"要修改这些选项": [11],
"每次一个字符": [11],
"因此您很可能并不需要参与编写自己的分割规则": [11],
"执行对齐操作可以从已经翻译过的单语言文档创建双语翻译记忆库": [11],
"run": [11,5],
"如果只有一个文件": [0],
"以分钟和秒为单位": [11],
"且要将": [3],
"使用指定的语言而非用户操作系统的语言": [5],
"因此如果涉及多个翻译记忆库": [6],
"语言模式的语法遵循正则表达式的语法规则": [11],
"的文件将被复制到": [9],
"则逐个片段地评估": [11],
"通常略低": [9],
"内置有拼写检查器": [4],
"外部": [3],
"titlecasemenuitem": [3],
"在终端窗口所显示的错误信息对查找原因提供了有用的信息": [5],
"在此模式下": [5],
"专用的": [10],
"也会显示此窗口": [11],
"editcreateglossaryentrymenuitem": [3],
"不换行空格将以灰色背景显示": [8],
"并下载": [5],
"编辑器窗格": [9],
"然后用服务来核查之": [5],
"启用不区分大小写的匹配": [2],
"显示": [11,[3,5,8]],
"离开此片段后背景恢复正常": [10],
"你只需要指定顶级子文件夹": [10],
"name": [11],
"团队项目必须先在服务器上": [6],
"取反": [2],
"可以在四个选项中循环切换": [8],
"开始": [5],
"复制": [9],
"进行关键字查找会找到字符串": [11],
"以运行命令": [5],
"对词条使用词干分析": [[1,3,11]],
"文件头中的规范": [11],
"或者用命令行使用": [6],
"即不会在": [11],
"账户页": [5],
"行首": [2],
"客户": [10],
"页面的翻译版本中": [8],
"则勾选本选项将始终确保此信息包含在已译文件中": [11],
"您的桌面上可能正摆着一本": [9],
"target": [[8,10,11],7],
"子文件夹的结构可以随您喜好采用任何形式": [10],
"允许您只插入原文文件的主文件名": [11],
"将报错并无法加载它们": [6],
"config-dir": [5],
"项目的根文件夹": [6],
"标签缺失或错位": [8],
"步是用命令行创建的项目": [6],
"然后按下": [11],
"大英简明百科": [0],
"你可以根据需要添加任意数量的映射": [6],
"在查找窗口中": [11],
"来自参考词汇表的词汇则用标准字体显示": [1],
"termbas": [1],
"所有片段都会一一显示": [11],
"将译文片段中高亮选定的文本的按所选选项": [8],
"孤立片段": [9],
"用包括标签和数字在内的全文进行计算得到的百分比": [9],
"建立": [6],
"的查找内": [11],
"这样就无需对大量": [10],
"整个过程可以通过基于": [11],
"让你可以检查确认数字在翻译过程中未被错误地更改": [11],
"接下来的两个匹配项亦是如此": [9],
"下载团队项目": [[3,6,8]],
"上一个备注": [[3,8]],
"文件进行对齐": [8],
"后自动启动": [[8,11]],
"项目负责人或本地化工程师来执行": [6],
"targettext": [11],
"来获取关于参数的更多信息": [5],
"字段": [8,11],
"匹配显示模板": [11],
"要修改文件过滤器模式": [11],
"转到下一个未译片段": [11],
"词典是印刷的词典的电子版本": [9],
"级别的": [8],
"取消勾选此选项可防止在编辑期间损坏标签": [11],
"aaabbb": [2],
"查找结果显示": [11],
"此命令中的": [5],
"从源代码构建": [5,7],
"将所有进度存储在项目的": [6],
"自动完成器会在输入已翻译术语表中条目的头几个字母或输入标签的": [8],
"删除开头和末尾的标签": [11],
"用户可以选择": [9],
"更靠谱": [10],
"项目中可翻译文件的总数": [11],
"edittagpaintermenuitem": [3],
"可定义": [11],
"在不同会话之间不会记住访问密钥": [11],
"词汇表中也会显示此条目": [11],
"例如": [11,[5,6],[4,8,10],[2,9],3],
"optionscolorsselectionmenuitem": [3],
"文本是整体评估的": [11],
"中加载译文文件": [6],
"添加新启动器": [5],
"支持导入": [6],
"移动到下一个片段": [8],
"项目的原文语言和": [6],
"会打开下一个带有备注的片段": [8],
"unicod": [2],
"viewmarknbspcheckboxmenuitem": [3],
"如果当前片段有多个备选译文": [8],
"将其设置为": [8],
"要将两个片段对齐到同一行上": [11],
"此处您可选择要安装的词典": [4],
"那么对于有着": [6],
"索引条目": [11],
"同上": [[6,8]],
"则必须重新加载项目才能使更改生效": [11],
"第三种可能的格式是": [1],
"团队": [11,[3,7]],
"拖放到": [5],
"即使只有一种可能的组合这也是必须的": [5],
"如果选择了": [5],
"来自放在": [8],
"标记已译片段": [8,3],
"片段序号": [[3,8]],
"维特比": [11],
"你授予此版本的权限": [5],
"则打开相应的项目": [9],
"一旦开始对项目进行翻译后": [11],
"您可以选择要翻译下列哪些项": [11],
"文件是逐行读取其中的片段的": [10],
"对于有着完全相同的原文文本的片段": [10],
"取消勾选此复选框": [11],
"勾选此选项可在": [11],
"甚至可以通过点击窗格的名称并拖动将其分离成独立的窗口": [9],
"msgstr": [11],
"将完全相同的片段视为单一实体": [11],
"拖拽到桌面上或开始菜单里来创建相应的快捷方式": [5],
"将内部的翻译记忆库保存到硬盘": [8],
"语言": [[6,11]],
"编码": [1],
"格式中": [11],
"稍后": [5],
"omegat.project": [6,5,10,[7,9,11]],
"全选再用": [9],
"excludedfold": [6],
"如果在其他文件夹内有名为": [5],
"这会修改": [10],
"targetcountrycod": [11],
"上获取": [5],
"还存储格式": [6],
"会选择紧挨着光标右边的那个字符所属的单词": [8],
"选择匹配": [8],
"通常还能支持全部三种格式": [10],
"移除": [4],
"分页符等": [11],
"名为": [[1,5]],
"随着翻译": [6],
"标记含备注的片段": [[3,8]],
"webstart": [5],
"字段中": [5,8],
"在这个例子中是": [6],
"会将尝试用可用的翻译记忆库翻译": [5],
"如果显示所有标签": [11],
"以下": [[5,11]],
"一个逗号或句点后面跟着若干空格然后是另一个逗号或句点": [2],
"格式为": [11],
"自动显示相关建议": [[3,8,11]],
"最大化窗格": [9],
"系统主机名": [11],
"此窗口允许您定义远程文件夹和本地文件夹之间的映射": [11],
"替换为": [11],
"选定一行或将鼠标移到这一行上会在最后一列中显示一个弹出菜单图标": [8],
"客户端": [6],
"文件的作者警告译者要注意译文的长度": [9],
"而只要其他上下文": [11],
"则可以在原文片段内高亮标记的单词上右键点击来打开带有建议翻译的弹出菜单": [9],
"文件会被修改为使用选定的语言": [5],
"翻译单元位于最后两列的单元格中": [11],
"这个包捆绑有": [5],
"操作系统默认编码": [1],
"请选择此菜单条目": [8],
"纯文本词汇表可以是": [1],
"更多信息": [5],
"不会干扰系统上可能已经安装的其他": [5],
"yandex": [5],
"选择了文本字符串": [8],
"还存在其他适合": [6],
"编辑器中显示为标签的那些": [11],
"有关的其他选项": [9],
"允许译文文件中存在空译文": [11],
"文件中的可翻译对象属性可以提取为单独的片段": [11],
"通过使用预配置变量来更改模糊匹配的显示方式": [11],
"下可以找到所需的两个条目": [5],
"一些已支持的过滤器有": [5],
"a123456789b123456789c123456789d12345678": [5],
"所生成的": [11],
"viewmarkwhitespacecheckboxmenuitem": [3],
"即可启用它": [5],
"后不要进行分割": [11],
"中使用的": [11],
"bak": [6,10],
"的匹配项其匹配度会降到": [10],
"删除标签": [11],
"流程是一样的": [6],
"当用户退出": [8],
"和映射的说明": [6],
"bat": [5],
"外部后处理命令": [11],
"如果原始文件名为": [11],
"多个连续的空白字符会被转换成单个空白字符": [11],
"然后跟着": [6],
"相同": [11],
"在编写纯从右向左文本时": [6],
"如何下载并安装词典": [0,7],
"jre": [5],
"或通过编辑": [6],
"optionsfontselectionmenuitem": [3],
"以在查找中包含位于": [11],
"过滤器": [11,6],
"插入匹配或所选内容": [[3,8]],
"根据首选的编辑行为": [9],
"可以通过对其名称旁边的复选框的勾选来关闭相应的过滤器": [11],
"不会被复制到译文文档中": [11],
"请选择": [[4,10,11]],
"特殊情况": [6],
"项目根目录下的三个文件": [6],
"存储库中的": [6],
"历史预测": [8],
"注释部分": [11],
"文件中": [8],
"显示匹配度百分比的那一行还包括有包含此匹配项的那个翻译记忆库的名称": [9],
"拼写检查": [4,[1,2,3,7]],
"软件应用程序": [5],
"你需要再按一次": [11],
"此区域允许输入一个在每次使用": [11],
"在排除模式对话框中": [11],
"在翻译过程中新放入": [8],
"freebsd": [2],
"当然": [[4,10,11]],
"delet": [11],
"要创建空译文": [11],
"替换": [11],
"projectaccessglossarymenuitem": [3],
"要检查其语法和适用性": [11],
"进行设置时": [1],
"创建新规则": [11],
"这样在编辑器中就可以用特定的颜色显示出来": [11],
"清除此选项可停用自动搜索": [11],
"developerwork": [5],
"例如欧盟的立法": [6],
"如下所示": [10],
"set": [5],
"详细的构建说明请参阅": [5],
"启动器": [5],
"跳过所有匹配此正则表达式的文本": [11],
"以元音开头的单词前面应当是": [2],
"时应当采取预防措施防止数据丢失": [6],
"文件的翻译": [11],
"optionsrestoreguimenuitem": [3],
"可以更改原文文件的显示参数": [6],
"译文的国家": [11],
"在窗口底部": [8],
"要注意": [[4,6]],
"应当有三个文件": [0],
"重用翻译记忆库": [6,7],
"内所设置的首选项与设置": [5],
"因为无论如何你都需要在启动时将其添加到命令行中": [5],
"这是默认的情况": [11],
"自由使用": [0],
"如果文件过滤器并不是开箱即用状态": [11],
"最小值": [11],
"offic": [11],
"如果无法看到所有窗格": [9],
"可以通过取消勾选此项选来保留之": [11],
"你可以在其中找到运行": [5],
"只修改两个文件": [6],
"搜索": [11],
"来创建译文文档并检查其内容": [6],
"文件都必须添加到版本控制系统中并发布到服务器上": [6],
"例如在文件名后添加语言代码": [11],
"在其他平台上": [5],
"repositories": [7],
"projectsavemenuitem": [3],
"因此为特定语言定义的规则优先级应当高于默认的那些": [11],
"xmx6g": [5],
"则本地对映射进行的更改都会丢失": [6],
"的片段的背景将以彩色显示": [8],
"项目中创建译文文件时": [6],
"默认为英语": [5],
"插入缺失的原文标签": [[3,8]],
"用法": [1,[5,7]],
"此对话框可以通过选择": [11],
"反之亦然": [11],
"在片段中点击右键并选择": [11],
"最重要的是": [5],
"将会是纯文本文件": [6],
"之间的部分": [11],
"其它系统": [5,7],
"给定项目的分割规则集保存为": [11],
"词汇表并存放在当前词汇表文件夹中": [11],
"你的启动脚本": [5],
"勾选": [11,8],
"也可以单击": [5],
"空格可匹配不换行空格": [11],
"会排除路径中包含有": [6],
"可以使用前缀": [11],
"你可以按": [6],
"现有的规则永远是个良好的起点": [11],
"要想返回标准的": [9],
"上一页": [[0,1,2,3,4,5,6,8,9,10,11]],
"用过滤器从远程存储库映射其他资源": [6],
"应当注意": [6],
"不会纳入统计": [11],
"或重新载入项目": [11],
"只需要在安装新版本时选择与现有安装相同的安装文件夹": [5],
"不应当分割为两个片段": [11],
"启动脚本": [5],
"因此建议调用一个脚本": [11],
"您可以更改或添加由每个文件过滤器处理的文件的文件名模式": [11],
"继续": [11],
"如果存在": [10,11],
"但如果你愿意也可以在项目的属性对话框中选择其他文件夹": [6],
"只需删除编辑区域中的所有文本将其清空": [9],
"不使用词干分析但仍然忽略标签和数字": [9],
"功能来提供可选择的词典列表": [4],
"的翻译记忆工具中会生成正确的匹配": [6],
"设置是正确的": [5],
"查找词典": [8],
"按钮并提供相应的网址来从用": [11],
"除大写字母外的所有字母": [2],
"若要在片段的原文部分中给匹配的词汇加上下划线": [1],
"其在译文文件中的翻译将为空": [11],
"独特": [11],
"该项目的默认词汇表": [1],
"在使用检查拼写功能前": [4],
"可以修改现有的原文文件": [6],
"是保存上一份翻译记忆库时的小时和分钟": [6],
"bis": [2],
"空格": [11],
"项目经理可以邀请译者在其上工作": [6],
"要添加新的文件过滤器模式": [11],
"projectopenmenuitem": [3],
"autom": [5],
"要编辑过滤器所针对的文件和编码": [11],
"这些翻译记忆库将用于对原文文本进行预翻译": [6],
"逗号分隔的值": [1],
"打开命令行窗口": [5],
"停用提供的机器翻译工具": [8],
"之后您可以将文件添加到其中": [10],
"toolsvalidatetagsmenuitem": [3],
"而显示的词汇条目则是出现在可用词汇表": [9],
"拖放": [5,7],
"视为": [6],
"非单词字符": [2],
"默认项目映射": [6],
"拼写检查器": [11],
"不仅存储翻译": [6],
"框中的": [11],
"按钮将打开词典安装器窗口": [4],
"更新": [11],
"制表符": [2],
"请选择此选项": [11],
"创建从右向左的译文文档": [6],
"此命令为": [5],
"尝试将每个过滤器的原文文件名模式与文件名进行匹配": [11],
"如果你决定修改项目的文件夹": [11],
"也不适用于安装在": [5],
"viewmarktranslatedsegmentscheckboxmenuitem": [3],
"参见上文描述": [6],
"颜色": [11],
"如果已经译了大量文本后才意识到项目的译文语言代码与拼写检查器的语言代码不一致": [4],
"注意所有参数都必须以两个": [5],
"可以直接从": [1],
"ilia": [5],
"当前": [10],
"目的": [11],
"大写": [[3,8]],
"只需双击已下载的程序": [5],
"工具菜单": [3,7],
"macos": [7],
"如果没有第五个匹配": [9],
"你甚至可以考虑删除那些不再需要的文件": [6],
"这种方式也有效": [11],
"必须在从右向左文本中嵌入从左向右文本": [6],
"行尾": [2],
"editselectfuzzy1menuitem": [3],
"更多关于它们的信息": [9],
"匹配的译文文本": [11],
"可能会更好": [11],
"所以正常情况下应该选中该复选框": [11],
"hide": [11],
"使用哪个配置文件": [5],
"出现一次或多次": [2],
"注意其他": [10],
"这将在工作文件夹中创建一个": [5],
"auto": [10,[6,8]],
"属性等": [11],
"document.xx.docx": [11],
"oracl": [5,3,11],
"项目网站并创建用户帐户": [11],
"中使用这类文件作为参考译文": [6],
"可以修改三个百分比的排列顺序": [9],
"必须在启动时将一些额外的参数传递给它": [5],
"示例": [2],
"gradlew": [5],
"其中项目经理希望完全控制项目": [6],
"要添加新的原文文件": [6],
"添加要翻译的文件和其他资源": [6],
"当前片段": [8],
"比较模式来对齐": [11],
"这个翻译记忆库逐渐得以填充": [6],
"文件可以用": [10],
"从左侧列表中选择所需的脚本": [8],
"只会包含勾选了第一列中的": [11],
"你可以这样来将添加": [5],
"换句话说": [6],
"在服务器上创建一个译者可访问的": [6],
"最后为缺省规则": [11],
"可以添加或删除模式": [11],
"switch": [11],
"这里有若干限制": [11],
"项目经理应当与译者核实确认两侧": [6],
"请使用": [6,1],
"恢复前一操作": [[3,8]],
"要以命令行模式运行": [5],
"两列充分对齐后": [11],
"src": [6],
"查找转选区": [11],
"control": [3],
"版本": [5,6],
"文件已经与": [5],
"no-team": [[5,6]],
"片段中出现的在任一词汇表内有匹配匹配项的术语都会显示在": [1],
"子文件夹中并将其重命名为": [6],
"服务的访问的过程": [11],
"它基于": [11,4],
"请取消对断句": [11],
"自动传播翻译复选框为用户提供了下面两种自动翻译的可能性": [11],
"词汇表文件没有正确的扩展名": [1],
"打开脚本窗口": [8],
"覆盖": [11],
"离开片段时验证标签": [11],
"这些文件位于": [11],
"输入翻译记忆库": [6],
"environ": [5],
"生成的": [5],
"optionsautocompleteglossarymenuitem": [3],
"之类因特网搜索引擎的": [11],
"上面的方法用于例行地启动程序多少有些不切实际": [5],
"使用句点作为小数点": [11],
"一开始是空的": [10],
"在命令行": [6],
"更确切地说": [11],
"文件可以和": [8],
"的文件": [[5,11],6],
"译者们会首选共享公共翻译记忆库而不是发布他们的本地版本": [6],
"中删除": [10],
"请检查其扩展名": [0],
"或直接在": [5],
"基于此原因": [5],
"不包括扩展名": [11],
"例如添加公认的缩写": [11],
"从右向左片段中的": [6],
"实际上此命令应该是类似于这样的": [5],
"带有扩展名的原文文件的完整文件名": [11],
"kde": [5],
"更多信息请参阅": [11],
"文件即可": [5],
"行终止符除外": [2],
"空行处进行段落分割或不分割": [11],
"在打开项目时": [1],
"languag": [5],
"因为它们可能会对机器的安全性产生重大影响": [11],
"以在查找中包含项目记忆库": [11],
"只使用原文文件的结构来生成片段": [11],
"如果": [[5,8,9,11]],
"为带绿色背景色的粗体字符": [9],
"选项后": [11],
"映射成本地文件": [6],
"并且": [11],
"安装": [5,4],
"点击对话框上半部分的": [11],
"key": [[5,11]],
"格式": [1,[0,6,7]],
"存放在": [5],
"svg": [5],
"包括带有项目设置的": [6],
"用于": [11],
"svn": [6,10],
"如果输入了多个单词": [11],
"在编辑器中根据查找来过滤条目": [11],
"中的文件进行更改要特别小心": [6],
"并可以编写": [8],
"以及打开": [6],
"必须是如下格式": [3],
"bug": [8],
"为你的系统弄个": [5],
"字符开头": [5],
"editreplaceinprojectmenuitem": [3],
"保存自动填充状态": [[8,11]],
"不带参数启动": [5],
"文档等": [11],
"当前原文文档": [3],
"express": [[2,11]],
"中能看到其文本内容": [6],
"选择其中之一会将其插入到译文片段中当前光标位置处": [9],
"即使两个文件中键的顺序不同或两个文件包含的信息数量不等": [11],
"在打开项目前断开网络连接": [6],
"快捷键定义文件必须命名为": [3],
"右下角的计数跟踪着翻译的进度": [9],
"语法": [11],
"包含格式的文件": [11],
"来进入": [11],
"当翻译软件相关的文件时": [11],
"调整对齐参数": [11],
"时特别有用": [11],
"的完整语法在来自": [3],
"gotoprevioussegmentmenuitem": [3],
"含备注的片段将标记为青色": [8],
"您可以在文档中滚动并双击任何一个片段将其打开并进行编辑": [9],
"后的所有内容将被忽略": [3],
"gotopreviousnotemenuitem": [3],
"对它进行翻译并创建译文": [6],
"翻译服务": [5],
"语句级分割": [11],
"editredomenuitem": [3],
"uilayout.xml": [10],
"你的系统用于访问代理服务器的端口号": [5],
"译文文档的显示模式必须在通常用于显示或修改它的应用程序": [6],
"所有缺失标签之一": [8],
"匹配度": [10],
"详细信息请参考": [8],
"朗文当代英语词典": [0],
"将片段导出到文本文件": [11],
"字体": [[3,11]],
"查找与替换窗口": [8],
"用户首选项文件夹下的": [11],
"标题旁边的括号内": [11],
"查找将会对指定字符串进行精确匹配": [11],
"主菜单中的大多数菜单项都可以指定新的快捷键": [3],
"代理服务器登录": [11,3],
"以外的操作系统中": [5],
"查找包含指定的确切字符串的片段": [11],
"是月份": [6],
"映射到本地文件": [6],
"如果你激活了翻译提示选项": [9],
"在解压缩得到的文件夹中名为": [5],
"一旦项目已经打开": [6],
"它是个技术性标签": [11],
"在服务器上创建一个空项目": [6],
"译文片段会被": [11],
"会打开上一个带有备注的片段": [8],
"runtim": [5],
"为当前正在翻译的文档创建对应的译文文档": [8],
"tester": [2,7],
"这可以是": [6],
"可以通过点击表头来按字母排序": [11],
"编辑器窗格显示部分翻译的文档的文本": [9],
"分割规则等": [6],
"开始精确对齐": [11],
"导入原文文件": [11],
"filenam": [11],
"中你可以修改": [5],
"你可能会同时打开多个查找窗口": [11],
"将规则集的名称和语言模式更改为相关语言及其代码": [11],
"且翻译是正确的": [6],
"然后用": [11],
"许可": [0],
"gotosegmentmenuitem": [3],
"点击其中之一会保存并关闭当前项目然后打开另一个项目": [8],
"是反向的": [11],
"注意密钥长度为": [5],
"只在未译片段中查找": [11],
"翻译记忆工具使用称为片段的文本单元": [11],
"可能还有若干其他文件": [10],
"要改变可用内存量": [5],
"具体方法根据发行版本而有所不同": [5],
"其中最可能用到的那条": [9],
"xx_yy.tmx": [6],
"该文件夹是否包含三个带不同扩展名的同名文件": [0],
"则必须对其进行修复": [6],
"按钮之一并选择": [11],
"那么只有被选中的部分替换掉整个译文片段": [8],
"把你要替换掉的单词或表达式输入到": [11],
"韦氏词典": [[0,7]],
"修订信息": [3,8],
"helpaboutmenuitem": [3],
"是否要勾选这些规则取决于它们与您要翻译的文本的类型是否相关": [11],
"如果未指定此参数": [5],
"因此此规则的复选框必须取消勾选": [11],
"之后不要进行分割": [11],
"如果在这类文件中发现错误": [6],
"文件是一种特殊情况": [6],
"通常是数值最高的": [9],
"此文件并不会被清空": [8],
"词典": [9,[0,6,7,11],[1,3,8,10]],
"请务必测试所有选项": [6],
"你也可用直接从命令行": [5],
"regular": [2],
"如何使用它们": [6],
"在其原文文件夹中遇到文件时": [11],
"出现零次或多次": [2],
"项目添加到版本控制系统": [6],
"参见下文和菜单": [6],
"扩展名和预期编码": [[1,7]],
"可以打开原文文件和": [11],
"由于": [11],
"选择关键字查找来以任意顺序查找任意数量的单个完整单词": [11],
"目标区域设置": [11],
"选择语言检查器的位置": [11],
"客户端从存储库": [5],
"开启项目": [6],
"项目的本地副本": [8],
"中使用的拼写检查器": [4],
"对网址的引用如下所示": [6],
"默认状态下": [11],
"窗格中": [1],
"tab": [11,3,[1,8],[2,9]],
"taa": [11,8],
"创建译文文件": [6],
"编码保存": [1],
"位数年份": [6],
"半角字符": [11],
"替换为原文": [[3,8,11]],
"借助这些帮助文字来起步": [6],
"在默认浏览器中打开本手册": [8],
"但是": [11,[4,6]],
"模糊匹配": [9,[7,8,11]],
"使用此功能的示例之一是自动将译文文档发送到客户的": [11],
"中的第一个": [2],
"tar": [5],
"使用所需的语言对和合适的名称": [6],
"onli": [11],
"日文句号": [11],
"projectreloadmenuitem": [3],
"在这类情况下": [6],
"左对齐": [6],
"所有其他字符代表其自身": [11],
"这使得": [6],
"给出的词典链接": [0],
"safe": [11],
"屏幕上记录的信息较少": [5],
"标签上分割": [11],
"但是要注意": [11],
"要允许这一点": [11],
"但仅针对当前显示在编辑器窗格中的文档": [8],
"但需要确保语言代码完全一致": [4],
"winrar": [0],
"tbx": [1,11,3],
"而不是新打开一个": [8],
"人们可以找到": [3],
"cat": [10],
"duser.countri": [5],
"tcl": [11],
"tck": [11],
"这里是代理服务器的": [5],
"三种匹配度百分比都会施加罚分": [10],
"readm": [11,5],
"文件添加到版本控制的项目中": [6],
"则会移动到下一个文件的第一个片段": [8],
"自动在词典中查找片段文本": [11],
"重要提示": [6],
"以及文本中的数字": [6],
"则该步骤已由程序完成": [6],
"存储库凭据": [11],
"应用程序文件夹中的": [5],
"在历史中后退": [8,3],
"译者可以指定译文文件是否应当包含编码声明": [11],
"模式组成的规则将按给定顺序应用": [11],
"align.tmx": [5],
"不会对其格式或字符集进行检查": [1],
"file2": [6],
"对计算机的无限制访问": [5],
"为了使其成为可能": [11]
};
