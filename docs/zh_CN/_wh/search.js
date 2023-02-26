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
 "附录",
 "首选项",
 "指南",
 "Introduction to OmegaT",
 "菜单",
 "窗格",
 "Project Folder",
 "Windows and Dialogs",
 "OmegaT 5.8.0 - User Manual"
];
wh.search_wordMap= {
"cancel": [4,7],
"所有用": [0],
"也可以使用": [0],
"half": [1],
"目前只有此首选项必须手动修改": [0],
"upload": [2,4],
"don\'t": [6],
"可写词汇表文件": [[0,7]],
"假设要查找": [0],
"would": [6,[0,2,7]],
"若不可用则": [0],
"ten": [[2,7],4],
"sake": [2],
"info.plist": [2],
"元素中的": [0],
"scratch": [2],
"click": [7,[3,5],1,4,2],
"fuzzi": [1,4,5,0,[2,3,6,7]],
"size": [1,7,2],
"left": [[0,5],[3,4,7]],
"这个参数文件包含自定义的自动文本参数": [0],
"单词字符": [0],
"object": [7,[0,2]],
"和下划线符号": [0],
"turn": [[2,3,6,7]],
"suffici": [2],
"result": [7,2,[3,4],[0,1,5]],
"项目菜单": [[0,8]],
"edittagnextmissedmenuitem": [0],
"same": [7,2,1,3,5,0,[4,6]],
"checkbox": [7],
"after": [0,2,7,1,3,5,[4,6]],
"quiet": [2],
"flip": [0],
"connect": [[1,2],5],
"hand": [3],
"address": [[0,2],[5,6]],
"这些控制符改变方向但本身不可见": [0],
"the": [7,2,4,1,5,3,0,6,8],
"straight": [3],
"projectimportmenuitem": [0],
"在加载时": [0],
"仅限未译片段": [1],
"是颠倒的": [1],
"obvious": [2],
"imag": [0],
"monolingu": [7],
"demonstr": [0],
"编辑菜单": [[0,8]],
"project-bas": [7],
"窗格": [5,[2,3,4,8]],
"advic": [2],
"good": [[2,3,8],1],
"omegat.project.lock": [2],
"字符集": [0],
"它们可用于": [0],
"moodlephp": [2],
"资源包": [0],
"currsegment.getsrctext": [7],
"implement": [1],
"uncheck": [7,0,1],
"export": [[0,2,4,7],6,[1,3]],
"others": [8],
"practic": [7,[0,2,6]],
"或者": [0],
"后处理命令": [0,[7,8]],
"reduc": [7],
"check": [[1,7],4,[2,3],0,5],
"onto": [2],
"rainbow-support": [2],
"resolut": [3],
"词汇表文件夹": [0],
"多语言用户手册的索引": [0],
"gotonotespanelmenuitem": [0],
"fr-fr": [3,1],
"存储库类型": [2],
"也十分有用": [0],
"规则": [0,[1,7]],
"ensur": [2,[3,7]],
"minim": [6,[1,2,5]],
"位于其中嵌套的文件夹下的词汇表也会被识别": [0],
"后模式": [0],
"分别是": [0],
"hard": [2],
"后面跟着零个或更多个": [0],
"cjk": [7,0],
"只有左方括号是特殊的": [0],
"prewritten": [6],
"下一个片段": [0],
"下一页": [[1,2,3,4,5,6,7,8]],
"better": [[3,7],1],
"你可以修改或添加文件名模式来将不同的文件关联给筛选器": [0],
"包括空格": [0],
"translation": [8],
"syntax": [[0,2],7],
"well": [2,7,[0,3],[1,4]],
"前后段落": [0],
"是匹配的": [0],
"仅以文件筛选器设置中的编码来保存译文文件": [0],
"empti": [2,6,[0,4,5],[3,7],1],
"可在词汇表中输入新词汇": [0],
"variabl": [1,[0,7]],
"全文": [1],
"保留": [0],
"block": [7,[0,4],[1,3,8]],
"tms": [2,6,[0,3,4],[1,7,8]],
"tmx": [2,7,6,[1,3,5]],
"propos": [7],
"order": [[1,7],[0,4,5,6]],
"e.g": [1,[2,7]],
"repo_for_all_omegat_team_project": [2],
"cli": [2],
"colleagu": [5],
"application_startup": [7],
"译文片段状态": [0],
"eventtyp": [7],
"integ": [1],
"fr-ca": [1],
"mainmenushortcuts.properti": [0],
"proport": [1],
"文件夹的完整路径": [0],
"subtitl": [2],
"gotohistorybackmenuitem": [0],
"save": [[4,7],2,1,6,0,[3,5]],
"v1.0": [2],
"relaunch": [6],
"top": [5,1,7,[2,3]],
"too": [2],
"have": [2,7,3,1,[0,4],6,5],
"powerpc": [2],
"它们后面没有跟着空格": [0],
"avail": [7,2,1,4,5,0,3],
"question": [0],
"hyphen": [0],
"如果你保留了开头和末尾处的标签": [0],
"editselectsourcemenuitem": [0],
"允许译文与原文相同": [[1,4]],
"是在搜索中该定义的分组编号": [0],
"com": [0],
"instal": [2,1,3,[0,4],[5,6,7]],
"诸如以下片语中开头的": [0],
"minor": [6],
"匹配统计数据": [[0,4,6]],
"所使用的库的列表": [0],
"cot": [0],
"remot": [2,6,4,[5,7]],
"translated": [8],
"以及": [0],
"upon": [[3,4],7],
"分割规则": [[0,7],[1,3,4,8]],
"并且在恢复为使用默认配置文件夹继续工作时不会出现": [0],
"whenev": [[3,6]],
"lag": [2],
"function": [0,4,3,7,[1,2],[5,6]],
"如上所述": [0],
"只有千位分隔符后面的部分会被匹配": [0],
"括号始终要左右成对使用": [0],
"在备注中": [7],
"垂直空格": [0],
"comparison": [7],
"tri": [7,[1,2]],
"changeid": [1],
"less": [2],
"translat": [2,3,7,5,[1,4],6,0],
"eras": [1],
"welcom": [3],
"此字符指定应该匹配前面那个字符或表达式的一个或多个实例": [0],
"université": [1],
"were": [3,4,[2,5]],
"十二个字符的其他几个则是普通的": [0],
"应用程序文件夹中包含": [0],
"原文文本": [0],
"对于某些文件格式": [0],
"原文文件和译文文件的格式可以不同": [4],
"其他平台": [0],
"cqt": [0],
"包含": [2],
"respons": [5],
"精确数字": [0],
"docs_devel": [2],
"lck": [5],
"tsv": [0],
"标记的文本": [[0,1],[3,5]],
"semicolon": [2],
"起始页": [[0,1,2,3,4,5,6,7]],
"gnome": [1],
"但不包括换行符": [0],
"原文术语可以是多个单词的语句": [0],
"退出": [0],
"创建一段和所在片段排列方向相反的文本": [0],
"注释": [5,0,[3,8]],
"片段中所有在": [0],
"doctor": [0],
"配置文件夹中存储了用户的大部分": [0],
"允许译文片段为空": [0],
"需要在前面加上反斜杠才能搜索花括号字符本身": [0],
"appdata": [0],
"地区": [0],
"重新加载项目": [4],
"gotten": [3],
"csv": [0,2],
"标记的标签都会被忽略": [0],
"删除非分割项目中开头和末尾的空白字符": [0],
"enhanc": [4],
"caractèr": [2],
"fr-zb": [2],
"let": [7,[0,3,5]],
"state": [2,7,[3,6]],
"les": [5],
"press": [7,[3,4],[0,5],1],
"dock": [2,[3,5]],
"element": [[3,7],0],
"勾选此框可使得仅有未翻译的片段会被发送至机器翻译服务": [1],
"night": [2],
"后处理命令的简单示例": [0],
"each": [7,2,[0,1,4,5],3,6],
"creativ": [0],
"但这并不会对表达式或结果产生任何影响": [0],
"suppli": [1],
"non-omegat": [1],
"中的第一个字符时": [0],
"片段": [7],
"filenameon": [1,0],
"cut": [[0,5]],
"ctrl": [0,4],
"editorinsertlinebreak": [0],
"jumptoentryineditor": [0],
"document": [2,7,0,3,5,4,[1,6,8]],
"标记自动填充片段": [0],
"mainten": [2],
"two": [2,7,0,[1,4],[3,5,6]],
"user-defin": [7],
"相比之下": [0],
"脱字符": [0],
"page_up": [0],
"glossaryroot": [0],
"scenario": [2],
"does": [8],
"attach": [7,1],
"distribute": [8],
"graphic": [2],
"creation": [4,[1,3]],
"resourc": [[3,7],6,2,0],
"moodl": [0],
"team": [2,4,[3,6,7],[0,8],[1,5]],
"xx_yy": [0],
"即使在文档中并未定义此选项也是如此": [0],
"docx": [[2,7],[0,4]],
"project_stats_match_per_file.txt": [[4,6]],
"txt": [2,0,5],
"空白原文片段有时充当原文语言中不存在但译文语言中必需的部分的占位符": [0],
"操作系统的名称": [0],
"就能匹配了": [0],
"quit": [4],
"文件夹中安装插件": [0],
"thing": [[2,3]],
"fashion": [3],
"自动与编辑器同步": [7],
"definit": [0,1],
"lib": [0],
"最新的是": [0],
"片语": [0],
"tedious": [3],
"source": [8],
"并且不会显示供翻译": [0],
"项目数据保存间隔": [[1,2,4,6]],
"这样的序列": [0],
"viewdisplaymodificationinfoselectedradiobuttonmenuitem": [0],
"index.html": [0,2],
"entir": [[2,3,4,5]],
"不会显示": [0],
"doubl": [[0,2]],
"actual": [3,2],
"要忽略的": [0],
"doubt": [[6,7]],
"develop": [2,7,[0,1]],
"diffrevers": [1],
"easiest": [[0,2]],
"可直访问它": [0],
"inlin": [7],
"通常是": [0],
"第一列是词汇原文": [0],
"page": [0,[4,7],[2,3],1],
"full": [7,2,1,5],
"按钮来进一步自定义其设置": [0],
"away": [[1,2,3]],
"becaus": [1],
"它正好匹配一个句点": [0],
"自动完成": [[0,1,4],[3,5,8]],
"它十分特别": [0],
"project.gettranslationinfo": [7],
"czt": [0],
"doctorat": [1],
"一些筛选器提供了": [0],
"start": [0,7,3,2,[1,4,5]],
"mymemori": [1],
"regex101": [0],
"pair": [[1,2],7,0],
"manage": [8],
"equal": [[0,2],5],
"幻灯片母版": [0],
"这样由四个数字": [0],
"环境变量和当前用户的": [0],
"watson": [1],
"anywher": [[5,6],7],
"short": [7,3,[1,4]],
"水平空白字符": [0],
"这样的代码": [0],
"可供探索超出本手册范围的高级或复杂用途": [0],
"tmxs": [1],
"three": [2,[1,4,6],[3,5,7]],
"required": [8],
"project_save.tmx.yyyymmddhhmm.bak": [2],
"项目词汇表": [0],
"单词": [0],
"viewmarkglossarymatchescheckboxmenuitem": [0],
"标记未译片段": [0],
"enter": [7,4,3,0,5,1,2],
"prioriti": [[1,4],[2,3]],
"pale": [4],
"文件夹中的文件时用的都是同样的文件名": [0],
"applic": [2,7,4,[0,3],[1,6],5],
"bidi": [0,4,7],
"projectteamnewmenuitem": [0],
"gotoprevxenforcedmenuitem": [0],
"preced": [0,7,2],
"单语": [0],
"memori": [2,3,7,6,5,[0,4],1],
"autocompletertablelast": [0],
"种词汇表文件": [0],
"no-match": [4],
"模板变量": [0,[1,8]],
"indefinit": [0],
"这个字符匹配一行的开头": [0],
"recogn": [7,3,[0,2,5,6],[1,4]],
"redistribute": [8],
"log": [[0,4]],
"完整的字符列表呈现在": [0],
"lot": [[0,3]],
"openjdk": [1],
"永住権": [[1,7]],
"分钟": [1],
"范例可参阅": [0],
"consult": [2,0],
"toolscheckissuesmenuitem": [0],
"pane": [5,7,3,[1,4],6,2,0],
"meant": [6],
"表示一个或多个": [0],
"原文文件的完整路径": [0],
"此字符指定应该匹配前面那个字符或表达式的零个或一个实例": [0],
"绘图和艺术字": [0],
"可用于创建较长的必须与所在片段反向排列的文本段": [0],
"即字母表中的字母": [0],
"tutori": [0],
"请查找": [0],
"orphan": [7,5],
"覆盖头部中的复数规范并使用译文语言的默认值": [0],
"autocompletertablepageup": [0],
"fetch": [[0,1,4]],
"www.deepl.com": [1],
"最大值": [0],
"美元": [0],
"的字母具有特殊含义": [0],
"config-fil": [2],
"quick": [7,[0,3],[2,4]],
"tell": [2,[3,7]],
"大小写": [0],
"checker": [[1,4]],
"对齐中的文本高亮": [0],
"shown": [5,7,4,[0,2]],
"和连字符": [0],
"day": [2,0,4],
"lre": [0,4],
"obtain": [7,[2,3,5]],
"以下模板变量始终可用": [0],
"system-user-nam": [0],
"lrm": [0,4],
"format": [2,3,0,7,[1,4,5],6],
"particular": [[2,7]],
"done": [2,3],
"你不能像项目词汇表那般从": [0],
"savour": [3],
"console.println": [7],
"rainbow": [2],
"autocompleterlistdown": [0],
"管道符号": [0],
"achiev": [[0,2,7]],
"包括括弧": [0],
"launcher": [2],
"片段属性": [5],
"pars": [[0,5,7]],
"part": [7,[1,4],[3,5],[2,6]],
"文本文件没有通用的段落标记": [0],
"unexpect": [2],
"分割规则和语言模式是用正则表达式定义的": [0],
"可以通过添加以下格式的": [0],
"browser": [1,5],
"activefilenam": [7],
"easi": [3,2],
"introduction": [8],
"project_files_show_on_load": [0],
"修改后的首选项存储在项目所使用的配置文件夹中": [0],
"数字": [0],
"切勿运行来自不受信任来源的后处理脚本": [0],
"其内容组存储在内存中": [0],
"它们必须手动进行修改": [0],
"third": [2,[5,6]],
"build": [2,7],
"further": [3,6,[0,5,7]],
"以全面了解": [0],
"stack": [7],
"ident": [7,2,[1,4,6],5],
"entries.s": [7],
"添加": [[0,1]],
"addit": [7,1,[0,2,3,4]],
"退出前确认": [1],
"gotonextuntranslatedmenuitem": [0],
"targetlocal": [0],
"path": [2,[0,1],[5,7]],
"算法": [7],
"请查看它": [0],
"bind": [7],
"十二个特殊字符是": [0],
"overwritten": [2,3,4,[1,7]],
"这就形成了表达式": [0],
"record": [6],
"monospac": [1],
"reinsert": [3],
"此选项可实现这点": [0],
"pass": [2],
"past": [4,[2,5]],
"impact": [3],
"需要审查翻译": [0],
"percentag": [5,1,6],
"especi": [[2,7]],
"whose": [[2,4]],
"的组合来定义表示特定文本模式的": [0],
"helpcontentsmenuitem": [0],
"resnam": [0],
"omegat-org": [2],
"设置为空译文": [[0,4]],
"descript": [4,7,[1,3]],
"remote-project": [2],
"initialcreationid": [1],
"ignore.txt": [6],
"projectaccessdictionarymenuitem": [0],
"计数器": [7],
"还有其他变体可用": [0],
"sentenc": [0,[3,7],1,[2,4]],
"此字符取消或激活后继字符的特殊含义": [0],
"alongsid": [2],
"转换大小写为": [0,4],
"consecut": [7],
"term": [7,5,[3,4],1,6],
"backslash": [2],
"files_order.txt": [6],
"mind": [[2,7]],
"projectrestartmenuitem": [0],
"editorskipnexttoken": [0],
"trans-unit": [0],
"新段落开始于": [0],
"right": [5,[0,7],2,3,[1,4,6]],
"possible": [8],
"qigong": [0],
"insid": [2,5],
"stage": [[2,7]],
"maximum": [[2,7]],
"under": [[2,4],[1,5,6,7,8]],
"submenus": [[2,7]],
"did": [7],
"imper": [7],
"意思是任意从": [0],
"reserv": [2],
"dir": [2],
"down": [0,7,[1,3]],
"可以用以下表达式做到这点": [0],
"later": [2,3,[6,7],5],
"检查当前文档问题": [0],
"legal": [0],
"unrespons": [7],
"viewfilelistmenuitem": [0],
"包含以": [0],
"info": [[0,1,4,5]],
"brows": [[5,7]],
"例如英语文本中的阿拉伯语摘录": [0],
"也可以在应用程序的": [0],
"non-break": [7,[0,4],[1,3]],
"journey": [0],
"test": [2,0],
"omegat": [2,0,3,7,1,4,6,8,5],
"allemand": [1,7],
"deepl": [1],
"month": [[0,2],3],
"final": [3,[2,6]],
"你可以创建多个参考词汇表文件夹": [0],
"occasion": [3],
"virtual": [7,2],
"的含义": [0],
"rather": [[0,7],[2,3,4]],
"在从左向右的片段中必须从右向左排列的弱方向性字符": [0],
"console-align": [[2,7]],
"dissimul": [5],
"此格式由欧盟翻译总局定义": [4],
"批注和工作表名称": [0],
"back": [[3,5,7],2,0,4,6],
"mach": [4],
"projectopenrecentmenuitem": [0],
"fr_fr": [3],
"在团队项目中": [6],
"miss": [4,[0,3],[2,5]],
"不自动显示原文文件列表": [0],
"thèse": [1],
"这个筛选器也无法理解": [0],
"对于连字符": [0],
"load": [[2,7],1,6],
"human": [1],
"状态栏": [5,[3,8]],
"标记空白字符": [0],
"custom": [[0,2],[1,4],[3,7]],
"issue_provider_sample.groovi": [7],
"glyph": [4],
"unl": [5],
"要记住": [0],
"editoverwritemachinetranslationmenuitem": [0],
"relat": [[1,6],[2,3,5]],
"grant": [2],
"console-stat": [2],
"如果需要处理": [0],
"ingreek": [0],
"lunch": [0],
"文档分发许可证": [0],
"f12": [7],
"convers": [2,1],
"ignor": [6,[0,1,5,7],[2,4]],
"convert": [2,3,[1,7]],
"elsewher": [4],
"attempt": [2,7,1],
"influenc": [4],
"projectexitmenuitem": [0],
"这个参数文件包含自定义的用户界面快捷键": [0],
"lock": [5,2,[0,3]],
"文件内容未加密": [0],
"adoptium": [2],
"text": [7,[4,5],1,0,2,3,6],
"排除": [2],
"en-to-fr": [2],
"类别": [0],
"fear": [2],
"editregisteruntranslatedmenuitem": [0],
"每个文件的匹配统计数据": [[0,4,6]],
"init": [2],
"made": [3,2,4,[1,5,6,7]],
"译文文件名和编码": [0],
"要忽略的标签": [0],
"block-level": [0],
"因此诸如": [0],
"manag": [3,4,2,[1,6,7]],
"不匹配": [0],
"manifest.mf": [2],
"中所列出的集合之一": [0],
"maco": [0,2,4,5,3,1],
"field": [7,5,4,[2,3],[0,1]],
"导出所选部分": [0],
"contents": [8],
"元素内声明": [0],
"invalid": [2],
"doc": [7,0],
"doe": [2,0,7,3,[1,4]],
"output-fil": [2],
"匹配": [0],
"status": [5,[2,3]],
"server": [2,1,[5,6]],
"don": [3],
"paramet": [2,7,1,6],
"dot": [4],
"对于基于": [0],
"stamp": [6],
"run-on": [0],
"skip": [0,6],
"overrid": [2],
"在确定匹配时": [0],
"如果在": [0],
"known": [4],
"回车字符": [0],
"map": [2,7,6],
"may": [2,7,0,5,[1,3,4,6]],
"forward": [4,0],
"保存和输出": [1,[2,8]],
"插件": [[0,1],[2,8]],
"允许标签编辑": [1],
"url": [2,1,[3,6],[0,4,7]],
"此字符必须与右大括号成对出现": [0],
"选择第二个匹配": [0],
"megabyt": [2],
"代码": [0],
"system-wid": [0],
"uppercasemenuitem": [0],
"viewmarkuntranslatedsegmentscheckboxmenuitem": [0],
"discrep": [4],
"请取消对此选项的勾选": [1],
"relev": [[1,2],[3,4]],
"needs-review-transl": [0],
"tagwip": [7,3],
"不限制": [0],
"如果每次创建": [0],
"usb": [2],
"use": [2,7,3,1,5,4,6,0,8],
"usd": [7],
"可以通过在前面加上反斜杠来搜索这四个类特殊字符本身": [0],
"feel": [[2,3],[1,6]],
"main": [7,5,2,1,[3,4,8]],
"convent": [3,0],
"粗体": [0],
"omegat.jar": [2,0],
"strip": [7,3],
"omegat.app": [2,0],
"conveni": [7,[2,3]],
"usr": [[0,1,2]],
"logo": [0],
"alter": [7],
"应用程序文件夹": [0,8],
"utf": [0,6],
"举例说": [0],
"下表列出了一些示例": [0],
"可以通过双写": [0],
"或创建": [0],
"然后在跟上一个不区分大小写的": [0],
"servic": [2,5,[1,4]],
"这些文件的内容就不会显示供翻译": [0],
"cleanup": [7],
"background": [6,[4,5]],
"的任意小写字符": [0],
"nonetheless": [0],
"dsl": [6],
"原文文件的完整文件名": [0],
"mid-transl": [7],
"时会变为一个": [0],
"隐藏开头和末尾的标签": [0],
"本地文件映射": [2],
"med": [4],
"dtd": [[0,2]],
"repeat": [7,[1,4],[0,2,3]],
"或紧跟在左方括号后面的脱字符之后的位置": [0],
"make": [2,3,6,7,[0,1],5,[4,8]],
"但前面加上": [0],
"定义": [0],
"sentence-level": [[0,7],3],
"projectcompilemenuitem": [0],
"classnam": [2],
"console-transl": [2],
"视图": [5,1,[3,4],8,[0,6]],
"地区代码": [0],
"structure": [8],
"optionsautocompletehistorycompletionmenuitem": [0],
"gotonextuniquemenuitem": [0],
"due": [[2,3]],
"conform": [[3,7]],
"properties": [8],
"所有的参数都应当用引号括起来": [0],
"inform": [5,2,[1,4],7,3,[0,6]],
"depend": [[1,2,4],5,[0,7],3],
"about": [0,[3,7],[2,5,6]],
"commit": [2],
"targetlocalelcid": [0],
"则这些空白字符在译文文档中也必须保留": [0],
"代理主机端口": [2],
"描述": [0],
"project_stats_match.txt": [[4,6]],
"above": [8],
"在此处": [0],
"正则表达式使用字母": [0],
"目录": [8],
"提供了用于": [2],
"benefit": [7],
"highest": [5,1],
"libreoffic": [3,0],
"如以下例子所示": [0],
"如果只需要匹配右方括号本身": [0],
"autocompleterclos": [0],
"qualiti": [4],
"此处仅涵盖对翻译人员最有用的基础知识": [0],
"选择第一个匹配": [0],
"scan": [7],
"常规命令和自定义脚本皆可使用模板变量": [0],
"long": [0],
"into": [2,7,[1,5],[3,4],6,0],
"浏览": [7],
"这意味着用正则表达式": [0],
"大量表达式": [0],
"defin": [1,[2,7],[0,5],[3,4,6]],
"industri": [3],
"variables": [8],
"though": [1],
"而第三列则是注释": [0],
"是任意": [0],
"为无限制": [0],
"加载时要忽略的段落": [0],
"它也会找到诸如": [0],
"everyday": [2],
"viewdisplaysegmentsourcecheckboxmenuitem": [0],
"appear": [1,7,5,[2,3]],
"editregisteremptymenuitem": [0],
"此字符起到了": [0],
"stats-output-fil": [2],
"mismatch": [7],
"progress": [[2,5],1],
"oper": [2,[1,4,7],[3,6]],
"它被称为": [0],
"mani": [2,3],
"open": [7,4,2,0,5,[1,3],6],
"会用它来识别编码": [0],
"treat": [[5,7],1],
"project": [2,7,6,3,4,5,1,0,8],
"取得": [[1,7]],
"trustworthi": [2],
"xmx1024m": [2],
"whatev": [2],
"autotext": [1],
"sever": [7,[4,5],[2,3,6]],
"loop": [7],
"提交原文文件": [2,[0,4]],
"如果勾选": [0],
"enclos": [7,[1,3]],
"penalty-xxx": [[2,6]],
"gotonextsegmentmenuitem": [0],
"与类别": [[0,8]],
"清理": [7],
"look": [3,1,[0,2],[4,7,8]],
"笔记窗格": [0],
"后面跟着区分大小写的": [0],
"撤消前一操作": [0],
"dropbox": [2],
"abort": [2],
"left-to-right": [0,4],
"字符来分隔各个标签定义": [0],
"中不包括连字符和撇号": [0],
"文字": [0],
"用户界面语言": [2],
"internet": [1],
"allow": [[4,7],[1,2,3],[0,5]],
"外部链接": [0],
"proper": [2,[1,4],[3,5,7]],
"speed": [2],
"printf": [0,1],
"peopl": [[2,4]],
"outsid": [[3,5,6]],
"访问项目内容": [0,3,6,[4,7]],
"common": [2,[0,5],[3,7]],
"interest": [0],
"appli": [7,1,2,[4,5,6]],
"以外": [0],
"自定义标签": [0,[1,2,3]],
"插图清单": [8],
"uncom": [2],
"writabl": [4,[2,6],[5,7],0],
"中的小写": [0],
"layout": [5,[1,3],8,4,[2,7]],
"registri": [0],
"format-specif": [1],
"step": [7,[0,2],3,6],
"bash": [[0,2]],
"tmroot": [0],
"mark": [7,0,1,4,[6,8]],
"创建文件时的系统时间": [0],
"stem": [1,5],
"base": [3,7,1,[2,4,5]],
"registr": [1],
"打开": [4,0],
"disconnect": [2],
"compulsori": [7],
"下一个未译片段": [0],
"文件夹或其子文件夹内": [0],
"那么它们会被覆盖": [0],
"whole": [2,[0,1,4,6,7]],
"它具有匹配": [0],
"如果需要匹配右括号字符本身": [0],
"保存": [0],
"loss": [2],
"reliable": [8],
"大学": [1],
"lost": [2,4,3],
"译文文件已创建": [5],
"按钮可将文件筛选器重置为其默认设置": [0],
"insertcharslr": [0],
"包括标签和数字": [1],
"still": [7,0,[4,5]],
"编码并且对此字符集之外的字符使用": [0],
"前进": [4,1],
"work": [2,0,[3,7],6,[1,4]],
"lose": [2,[3,5]],
"此字符开启一个": [0],
"suitabl": [[2,3,5]],
"备选译文所用的标识符": [0],
"among": [3],
"会检查其文件扩展名并尝试与筛选器中的原文文件名模式进行匹配": [0],
"word": [7,0,[4,6],1,[3,5]],
"标签处理": [1,[3,4,8]],
"love": [0],
"lingue": [1],
"词汇表可以是": [0],
"下表提供了各式其他示例": [0],
"auto-propag": [[2,7]],
"要么是全局且可用于共享配置文件夹的所有项目的": [0],
"任何给定表达式要么": [0],
"across": [3],
"位于标签之外的空白字符一般认为是不重要的": [0],
"功能依赖于正则表达式或将其作为一个选项提供": [0],
"任何以": [0],
"simplest": [2],
"在译文文件中压缩空白字符": [0],
"vcs": [2],
"lingvo": [6],
"developer.ibm.com": [2],
"mrs": [1],
"opinion": [5],
"literari": [0],
"译文文件的编码和原文文件相同": [0],
"averag": [7],
"threshold": [1,[2,5]],
"freedoms": [8],
"float": [1],
"只有反斜杠": [0],
"manipul": [[4,5]],
"不同": [0],
"即便将": [0],
"html": [0,2,[1,3]],
"spell": [1,[4,7],0],
"隐藏标签": [7],
"insertcharsrl": [0],
"finit": [1],
"移动到下一个已译片段": [4],
"句子式大小写": [0],
"www.ibm.com": [1],
"词汇表是存储在": [0],
"platform": [2,0,1],
"或者点击": [0],
"分割规则或例外规则定义了在片段中进行拆分或不能拆分的位置": [0],
"toolsalignfilesmenuitem": [0],
"如果原文文件中存在编码声明": [0],
"spent": [3],
"overal": [4],
"字符具有取消或激活其他字符的特殊含义的默认特殊含义": [0],
"那么在": [0],
"instead": [[0,1,2,3,4,7]],
"improv": [7],
"command": [7,2,1,4,0,5,3],
"project-specif": [6,7,[1,2]],
"unlik": [[1,3]],
"detach": [7],
"slash": [0],
"tag-fre": [7,3],
"onecloud": [2],
"notat": [0,1],
"viewmarkbidicheckboxmenuitem": [0],
"refus": [[2,3]],
"year": [[0,2]],
"branch": [2],
"via": [2],
"fileshortpath": [[0,1]],
"permiss": [7],
"visual": [[0,3,4]],
"double-click": [7,2,[4,5]],
"absent": [2],
"near": [3],
"approxim": [7],
"设置允许你定义": [0],
"日本語": [7],
"或者甚至诸如": [0],
"本附录面向对探索提高工作效率的强大方法感兴趣的用户": [0],
"比如": [0],
"instruct": [2,[6,7]],
"跳转到最后编辑的片段": [5],
"version": [2,[3,4,7,8]],
"恢复默认值": [0],
"的特殊含义": [0],
"folder": [2,7,6,4,3,1,0,5,8],
"stop": [[1,7]],
"handl": [[1,2],7],
"detail": [[2,4],7,1,3,6,5,0],
"retriev": [2],
"空白字符": [0],
"projecteditmenuitem": [0],
"least": [2,7],
"删除": [0],
"new_word": [7],
"和工作表名称": [0],
"run\'n\'gun": [0],
"measur": [2],
"nashorn": [7],
"后面字母的对应大写用于取反这个类": [0],
"machin": [1,4,[5,7],[0,2,6]],
"unsung": [0],
"通用": [1,8],
"分组的内容可以用在": [0],
"字符只匹配一个字符": [0],
"learn": [0,3],
"last_entry.properti": [6],
"选择第五个匹配": [0],
"级别": [7],
"那么此文件就会由": [0],
"此文件存储着当前选定的文本": [0],
"newer": [4],
"uppercas": [0],
"invok": [7],
"定义该位置需要两个正则表达式": [0],
"eager": [3],
"official": [8],
"thorni": [3],
"autocompleternextview": [0],
"系统上的主机名": [0],
"specif": [7,2,1,4,3],
"反斜杠": [0],
"是任意换行字符": [0],
"dsun.java2d.noddraw": [2],
"或者叫": [0],
"原文文件的名字": [0],
"的数字": [0],
"ell": [1],
"need": [2,3,6,7,1,[0,5]],
"editorfirstseg": [0],
"often": [3,[2,7]],
"x0b": [2],
"gather": [2],
"els": [5],
"如果勾选此选项": [0],
"canada": [2],
"altern": [7,4,[1,5],0,2],
"http": [2,1],
"它们覆盖了整个": [0],
"interfer": [4],
"lisenc": [0],
"下面用依赖于各个字符的正则表达式以及与其匹配或不匹配的文本来作为例子简要介绍每个字符": [0],
"softwar": [2,0],
"的所有内容映射到本地": [2],
"projectsinglecompilemenuitem": [0],
"end": [0,7,[1,2,3]],
"lisens": [0],
"字符来分隔各个表达式": [0],
"的任意字符": [0],
"otherwis": [2,[6,7]],
"大部分": [0],
"myfil": [2],
"particip": [2],
"原文语言": [0],
"env": [0],
"howev": [7,[0,2,3]],
"okapi": [2],
"special": [[2,6,7]],
"page_down": [0],
"key-bas": [7],
"numer": [0],
"关联给": [0],
"fine-tun": [5],
"也可以在各种支持渠道中提问": [0],
"花括号": [0],
"字符或其他会被解析为": [0],
"project_nam": [7],
"system-os-nam": [0],
"occurr": [7],
"这样小数点前面有任意多位但小数点后只有一位的数字": [0],
"insertcharspdf": [0],
"specifi": [2,1,7,[0,3,5,6]],
"heapwis": [7],
"narrow": [3],
"翻译以下属性": [0],
"模板列表中的其他条目是系统的环境变量": [0],
"newli": [[6,7]],
"similar": [2,3,1,[0,5,7],4],
"存储库": [2],
"tar.bz2": [6],
"在注释中": [7],
"paragraph-level": [0],
"forth": [[2,5]],
"bundle.properti": [2],
"contributors.txt": [0],
"变成": [0],
"选择下一个匹配": [0],
"driver": [1],
"www.regular-expressions.info": [0],
"characterist": [7],
"此文件包含当前片段的": [0],
"sourcelang": [0],
"脚本": [7,1,[0,4],8],
"omegat\'s": [8],
"这样无意义的组合": [0],
"against": [[2,3,5]],
"parenthes": [1,0],
"cell": [7,0],
"login": [1],
"文件夹": [0],
"optionsdictionaryfuzzymatchingcheckboxmenuitem": [0],
"remind": [[0,3,4,6]],
"valid": [4,[0,1],[2,6]],
"pictur": [3],
"assur": [4],
"interfac": [2,[1,4],[0,3,5]],
"projet": [5],
"安全存储": [1,8],
"share": [2,7,6,3,[0,5]],
"的远程桌面会话中运行缓慢": [2],
"上一个片段": [0],
"取消了句点的": [0],
"输入或插入从右向左的文本": [0],
"的结果": [1],
"sourcelanguag": [1],
"当使用了": [0],
"gzip": [6],
"helpupdatecheckmenuitem": [0],
"让修饰符右侧的表达式片段不区分大小写": [0],
"notic": [7],
"notif": [5],
"esc": [5],
"exampl": [0,2,7,1,4,[3,6],5],
"所选匹配的来源": [0],
"字符代表你的主文件夹": [0],
"应用程序和许多其他重要文件": [0],
"nostemscor": [1],
"头部供翻译": [0],
"的字母在正则表达式中具有特殊功能": [0],
"选项": [7,0,4,[1,3],[2,8]],
"first-third": [2],
"那么可能会被识别并视为": [0],
"project_chang": [7],
"screen": [3],
"关于这里未涵盖的组合的信息": [0],
"console-createpseudotranslatetmx": [2],
"etc": [1,[2,5,6]],
"这种特殊含义必须用另一个字符来取消才能匹配此字符本身": [0],
"fuzzyflag": [1],
"原文文件": [0],
"可以在其位置上显示可见的标记": [0],
"precaut": [2],
"new": [2,7,3,4,0,1,6,5,8],
"escap": [0,2],
"sequenti": [4],
"below": [[0,2],5,6,[1,3,4,7]],
"试图单独使用左括号或右括号会导致错误": [0],
"poisson": [7],
"runway": [0],
"choos": [[1,2,7]],
"half-width": [7],
"tool": [2,[4,7],[0,1,3,8]],
"ll-cc.tmx": [2],
"双向控制符可从": [0],
"therefor": [0,7,2],
"映射本地": [2],
"标签视为段落分隔标记": [0],
"译文文本": [0],
"slot": [4],
"around": [[1,3,5]],
"下的": [0],
"grunt": [0],
"和单个字符组成的具有单一含义的单元": [0],
"reload": [[4,7],[2,3,6],0],
"calcul": [[1,5],7],
"render": [7],
"magento": [2],
"要忽略此内容": [0],
"backs-up": [6],
"korean": [1],
"通常缩写为": [0],
"字符在前面加一个": [0],
"区间": [0],
"boundari": [[0,3]],
"dispar": [2],
"offlin": [2],
"ll_cc.tmx": [2],
"显示版权": [4],
"如上文所述": [0],
"标记词汇表匹配": [5,[0,3,4]],
"u00a": [7],
"参考词汇表": [0],
"分发的脚本位于此文件夹中": [0],
"shift": [0,4,7],
"记忆": [7],
"许多字符或字符组合在正则表达式中具有特殊含义": [0],
"java": [2,0,1,7,3],
"exe": [2],
"xmxsize": [2],
"project_save.tmx": [2,6,[3,7],4],
"dictionari": [1,3,6,5,[4,7],0,2],
"remain": [7,6,4,2],
"但是表达式会返回": [0],
"不要用文件名来标识多重译文": [0],
"powershel": [[0,2]],
"eye": [0],
"dictionary": [8],
"所使用的": [0],
"值设为": [0],
"appl": [0],
"插入原文": [0],
"大写和小写都算": [0],
"recommend": [2,7],
"各个已翻译的用户手册位于不同的语言文件夹中": [0],
"的简写": [0],
"default": [7,1,4,2,5,0,6,3,8],
"gray": [4,7,3],
"正则表达式只匹配文本": [0],
"作为": [0],
"字符匹配零个或更多个字符": [0],
"sudo": [2],
"drop-down": [7,1],
"timestamp": [[0,2,8]],
"projectaccessrootmenuitem": [0],
"并且会将其排除在验证目的之外": [0],
"fulli": [6],
"用户的登录名": [0],
"重新加载": [0],
"such": [2,7,1,[0,3],[5,6],4],
"plugin": [2,1,0,3],
"一些简单的例子": [[0,8]],
"autocompletertableup": [0],
"插入具有强方向性的不可见字符来强制弱方向性或中性方向性字符放在特定位置": [0],
"任意字符": [0],
"understood": [7],
"需要在前面加上反斜杠才能搜索方括号字符本身": [0],
"右方括号": [0],
"projectcommitsourcefil": [0],
"editinsertsourcemenuitem": [0],
"viterbi": [7],
"microsoft": [0,[3,7]],
"reorgan": [0],
"projectnewmenuitem": [0],
"ecmascript": [7],
"标准": [0],
"white": [4],
"它匹配": [0],
"segment": [7,4,5,1,0,3,6,2,8],
"很多": [0],
"changes.txt": [[0,2]],
"退出码和": [0],
"glossari": [5,7,0,[4,6],[1,3],2],
"recurs": [7],
"ignored_words.txt": [6],
"字符": [0,8],
"might": [2,3],
"github.com": [2],
"configuration.properti": [2],
"autocompleterlistpageup": [0],
"expressions": [8],
"glossary": [8],
"等修饰或其他": [0],
"supersed": [7],
"occupi": [7],
"reopen": [2],
"全部保留": [7],
"服务类型": [1],
"最后的": [0],
"正则表达式在默认情况下是区分大小写的": [0],
"它匹配不属于该类的所有字符": [0],
"physic": [2],
"recreat": [[2,3,6]],
"next": [4,0,7,[2,3],5,1],
"对于脚本": [0],
"import": [2,[0,3,5,6]],
"string": [7,4,1,2,3],
"hidden": [6,[5,7]],
"target-languag": [2],
"button": [7,3,1,[0,4]],
"通常定义为": [0],
"not": [2,0,7,1,3,4,6,5],
"now": [[0,2],3],
"trademark": [5],
"类的示例": [[0,8]],
"factor": [4],
"ascii": [0],
"包括数字和下划线": [0],
"green": [7,5,4],
"pseudotransl": [2],
"was": [1,2,7,[0,3,6]],
"旧版筛选器": [0],
"菜单": [[4,5],[3,7,8]],
"greet": [3],
"selection.txt": [[0,4]],
"way": [2,[0,3],[4,7],8],
"xhtml": [0],
"what": [0,[2,3,7]],
"itoken": [2],
"finder.xml": [[0,6,7]],
"refer": [0,[6,7],2,3,5],
"默认情况下禁用局部后处理命令": [0],
"window": [7,0,4,5,2,1,3,[6,8]],
"call-out": [4],
"discard": [7],
"criteria": [7,3],
"disable-project-lock": [2],
"omegat.pref": [[0,1,7]],
"when": [7,2,1,5,3,6,4,0],
"issues": [8],
"与项目的原文语言相匹配的语言模式将应用于该项目": [0],
"auto-popul": [[4,6],1,2],
"far": [5,2],
"最简单的正则表达式由单个字符或": [0],
"embed": [0,4],
"plan": [1],
"give": [4,2,[0,1,3,6,7]],
"multipl": [2,0,[1,5]],
"编码文字与": [0],
"unfriend": [4],
"lowest": [5],
"explicit": [2],
"后面跟的是逗号加上空格而非仅只一个空格": [0],
"除非你启用": [0],
"everyth": [[0,2]],
"suit": [[0,2,3,5]],
"study": [8],
"全局文件筛选器": [[0,1,7],[4,8]],
"widget": [5],
"portion": [7],
"direct": [2,4,0,[5,7]],
"打开项目": [2],
"删除此文件也会重置筛选器首选项并将项目恢复为使用全局文件筛选器": [0],
"地址中": [0],
"mechan": [[0,2,4]],
"modern": [2],
"web": [1,7,2,[0,4,5]],
"en-us_de_project": [2],
"区块": [0,8],
"you\'r": [3],
"symlink": [2],
"memories": [8],
"older": [2],
"本地文件夹或文件的名称": [2],
"编辑器": [7,[0,3,5],[1,4,6,8]],
"nth": [7],
"editselectfuzzy4menuitem": [0],
"editregisteridenticalmenuitem": [0],
"相对于": [2],
"hanja": [0],
"筛选器中注册的模式": [0],
"great": [3],
"关于": [[0,4]],
"usag": [2],
"left-hand": [7],
"其中": [0],
"advanc": [7,[0,1]],
"certain": [2,5,4],
"fed": [2],
"section": [3,2,7,0,1,[4,5]],
"auto-complet": [1,[0,3,5]],
"used": [8],
"方括号": [0],
"protocol": [2,1],
"few": [0,[3,4],2,7],
"dict": [1],
"左括号": [0],
"不带点号的原文文件扩展名": [0],
"该文件夹初始为空的": [6],
"记事本": [5,3,[4,8]],
"正则表达式用于查找文本": [0],
"marker": [[1,5],[0,2]],
"effect": [0,7,1],
"keep": [7,2,6,[1,3,5]],
"conventions": [8],
"whi": [0],
"topic": [2],
"请参阅下文": [0],
"fallback": [4],
"option": [2,7,1,4,3,6,5,0],
"who": [1,7,[2,4]],
"在文本文件中创建段落的方式": [0],
"如果要让文本筛选器处理": [0],
"shortcuts": [8],
"remark": [3],
"everyon": [2],
"的词汇表文件是包含三列列表的简单纯文本文件": [0],
"是分组的编号": [0],
"along": [0,7],
"用户定义的脚本": [0],
"正确的": [0],
"标记所有重复的片段": [1],
"其他选项": [0],
"作为原文容器并预期用翻译覆盖": [0],
"various": [3,[4,7],[2,5],1,[0,6]],
"建议": [2],
"archiv": [6],
"visit": [4,6],
"user": [2,1,[3,4,8],[5,7],0],
"这是默认的模式": [0],
"proxi": [2,1],
"extens": [2,0,6,[1,4,5,7]],
"back_spac": [0],
"bring": [7,[0,2,5]],
"tooltip": [1,5],
"recalcul": [7],
"为了将筛选器和文件关联起来": [0],
"robot": [0],
"fit": [[3,5]],
"搜索不区分大小写": [0],
"claus": [0],
"fix": [2,[4,6]],
"已译": [7],
"忽略未译片段": [4],
"此字符必须与右方括号成对出现": [0],
"rang": [2],
"文档文件夹": [0],
"despit": [7],
"eclips": [2],
"默认情况下会将其显示为红色": [0],
"ad": [7,[0,2],1,[3,4,6]],
"窗口": [0],
"sure": [2,1,7,[3,5]],
"diff": [1],
"automat": [7,2,1,6,4,3,5,0],
"an": [2,0,[3,7],1,4,5,6],
"editmultiplealtern": [0],
"panic": [3],
"extend": [[2,7]],
"as": [2,7,0,1,4,6,5,3,8],
"at": [7,1,5,[2,3],4,0,6],
"predefin": [1,[0,2]],
"constitut": [[0,2,6]],
"hierarchi": [6,2],
"be": [2,7,1,0,6,4,3,5],
"filters.xml": [0,[1,2,6,7]],
"version-control": [2],
"br": [0],
"在译文文件中包含未翻译的内容有时候会产生兼容性问题": [0],
"search": [7,1,4,3,0,5,[2,6,8]],
"by": [7,2,0,1,6,3,4,5,8],
"segmentation.conf": [[0,2,6,7]],
"并将": [0],
"panel": [7,1],
"ca": [2],
"cc": [2],
"ce": [2],
"freeli": [5],
"除了常规命令外": [0],
"figur": [5],
"cs": [0],
"renam": [2,3,0],
"partner": [2],
"下一个独特片段": [0],
"用匹配或所选内容替换译文": [0],
"此文件包含团队项目存储库的登录信息": [0],
"apach": [2,7],
"视觉提示有助于验证你的对齐是否正确": [0],
"config": [2],
"幻灯片批注": [0],
"对于脱字符": [0],
"adjustedscor": [1],
"font": [1,4,5],
"dd": [2],
"de": [[1,5]],
"提交译文文件": [2,[0,4]],
"extern": [7,1,4,3,[0,2,5,6]],
"forc": [[1,4,7]],
"do": [2,[1,7],[0,3,4],5,6],
"f1": [[0,4,7]],
"f2": [[3,5],[0,7]],
"f3": [[0,4],5],
"字符本身": [0],
"f5": [[0,3,4]],
"dz": [6],
"editundomenuitem": [0],
"won": [2],
"rare": [2],
"ja-rv": [2],
"诸如": [0],
"在分割时将": [0],
"which": [[2,7],4,[3,5],0,1],
"belazar": [1],
"en": [0,1],
"carri": [[4,7]],
"但即使是最简单的正则表达式": [0],
"never": [[3,4,6]],
"aggress": [4],
"还可以通过将其放在不会触发其特殊含义的位置来搜索其本身": [0],
"activ": [4,1,7,[0,5,6]],
"first-class": [7],
"indic": [5,4,[0,2],1],
"可以使用形如": [0],
"origin": [2,7,3,1,[0,5]],
"项目属性": [2,[0,4,7],[1,3,6,8]],
"for": [2,7,1,4,0,3,6,5,8],
"exclud": [2,7],
"可以创建": [0],
"出于安全原因": [0],
"fr": [2,[1,3]],
"content": [2,7,0,[1,3],6,[4,5]],
"的任意其他单词或字符序列": [0],
"duckduckgo": [1],
"hover": [[1,4,5]],
"applescript": [2],
"skill": [2],
"exclus": [7,2],
"json": [2],
"gb": [2],
"修改过的全局文件筛选器首选项保存在配置文件夹的": [0],
"helplogmenuitem": [0],
"easy-to-us": [3],
"editoverwritetranslationmenuitem": [0],
"go": [0,5,3,[2,4,7]],
"counter": [7],
"kept": [6,[2,7]],
"aeiou": [0],
"或从不": [0],
"本身": [0],
"form": [7,0],
"publish": [2],
"局部外部搜索": [7],
"restor": [2,[1,6],[4,5,7]],
"美元符号": [0],
"assign": [[0,4,7],[2,3,6],[1,5]],
"typograph": [[4,7]],
"hh": [2],
"duser.languag": [2],
"使用操作系统的默认编码": [0],
"viewmarkparagraphstartcheckboxmenuitem": [0],
"视图菜单": [[0,8]],
"原文文件名模式使用通配符": [0],
"canadian": [1],
"easili": [3,6],
"上双击或者点击其在": [2],
"repetit": [[4,5,7],0],
"使用此选项可以删除它们": [0],
"veri": [3,7,[1,4],[0,2]],
"file-target-encod": [0],
"four": [[2,4]],
"mainmenushortcuts.mac.properti": [0],
"context": [[1,5],4,[0,2,3,6]],
"定义让标签变为不可翻译的属性值": [0],
"id": [1,0,7],
"https": [2,1,0,[5,6]],
"drag": [5,2],
"if": [2,7,4,1,3,5,6,0],
"french": [2,1,7],
"project_stats.txt": [6,4],
"ocr": [7],
"oct": [1],
"projectaccesscurrenttargetdocumentmenuitem": [0],
"in": [7,2,4,1,5,0,3,6,8],
"lower": [4,0,[5,6,7]],
"termin": [2],
"ip": [2],
"可以通过诸如添加语言代码或时间戳的方式自动为你所创建的文件建立新的文件名": [0],
"index": [0],
"is": [2,7,5,4,1,6,0,3,8],
"it": [2,7,6,5,3,0,1,4,8],
"来指定字母数字区间以外的字符类": [0],
"多个值之间用英文逗号分隔": [0],
"projectaccesstmmenuitem": [0],
"这个参数文件包含自定义的文件筛选器": [0],
"odf": [0],
"ja": [[1,2]],
"becam": [2],
"带重命名的映射": [2],
"odt": [[0,7]],
"gotonexttranslatedmenuitem": [0],
"charset": [0],
"librari": [0],
"toolscheckissuescurrentfilemenuitem": [0],
"字母转变为匹配任意": [0],
"libraries.txt": [0],
"learned_words.txt": [6],
"所有和此正则表达式相匹配的段落都会被忽略": [0],
"类是通过将所需的字符括在方括号中来定义的": [0],
"和幻灯片版式": [0],
"总是": [0],
"在该参考词汇表文件夹中": [0],
"meantim": [2],
"前模式": [0],
"ftl": [[0,2]],
"不可见说明性文本": [0],
"themselv": [2,1],
"例如以下句子中的连续": [0],
"viewdisplaymodificationinfoallradiobuttonmenuitem": [0],
"之前": [0],
"off": [[3,4,7]],
"分组": [0],
"la": [1],
"li": [0],
"ll": [2],
"receiv": [[1,5]],
"项目": [2,3,7,4,6,[0,1],8],
"lu": [0],
"while": [4,[3,5],7,[1,2],0],
"second": [[1,4],[2,5,7]],
"that": [2,7,1,3,[0,6],4,5,8],
"cycleswitchcasemenuitem": [0],
"mb": [2],
"than": [1,[0,2,6,7],3,[4,5]],
"limit": [2,[0,3,6]],
"me": [[2,5]],
"picker": [2],
"mm": [2],
"entri": [7,4,5,[0,1],3,[2,6]],
"ms": [0],
"author": [7,[3,4]],
"toggl": [[0,5],7],
"mt": [6],
"修改过的局部文件筛选器首选项保存在位于项目文件夹内的": [0],
"或断行": [0],
"my": [[0,2]],
"翻译以下元素": [0],
"license": [8],
"disk": [7,2],
"updat": [[2,6],[1,7],3],
"three-lett": [[3,7]],
"licenss": [0],
"通常首选使用": [0],
"no": [7,2,[0,1],4,3,[5,6]],
"code": [0,2,7,3,1],
"格式的项目包": [4],
"大多数字符匹配其自身": [0],
"bridg": [2,[0,6]],
"gotohistoryforwardmenuitem": [0],
"head": [0],
"dialog": [[1,7],3,4,2,[0,6]],
"project_save.tmx.timestamp.bak": [6],
"of": [7,2,[1,3],5,4,6,0,8],
"possibl": [2,0,1,5],
"ok": [7,4,3],
"hear": [0],
"on": [2,7,[1,5],[3,4],0,6,8],
"keyboard": [0,[4,5]],
"purpos": [2,[1,7]],
"or": [7,2,0,4,3,5,1,6],
"os": [[4,5]],
"encod": [[6,7]],
"自动": [0],
"仅当该文件已经具有声明时": [0],
"方括号括起的字符类": [0],
"列表中每一行开头的序号": [0],
"editinserttranslationmenuitem": [0],
"fileextens": [0],
"easier": [2,[0,3],4],
"compliant": [2],
"pm": [[1,5]],
"中的": [0],
"po": [2,0,1,[5,6]],
"不带扩展名": [0],
"下面其中的": [0],
"查找其自身": [0],
"你可以使用此选项": [2],
"所创建的": [0],
"词汇表匹配": [5],
"qa": [7,4],
"拼写": [0],
"检查更新": [[0,4]],
"autocompletertablefirst": [0],
"necessari": [2,[1,3,7]],
"concurr": [4],
"recent": [[2,4],[0,5,7]],
"在所指定的最大字符数之后将行断开并略去剩下的部分": [0],
"they": [7,2,[0,3,5],[1,6],4],
"streamlin": [3],
"github": [2],
"edit": [7,2,5,4,3,0],
"old": [2,[1,7]],
"editselectfuzzy5menuitem": [0],
"them": [3,2,7,0,4,[5,6],1],
"bilingu": [[6,7],2],
"then": [2,3,[5,7],[1,4,6]],
"当启用了首选项": [0],
"third-parti": [2,3],
"用作默认译文": [[0,4,5]],
"详情请参阅指南": [0,[2,3,7],[1,4,6]],
"rc": [2],
"includ": [2,7,6,[3,5],1,[0,4]],
"参见": [0],
"readili": [2],
"adopt": [0],
"t0": [3],
"t1": [3],
"t2": [3],
"文本替换": [7,[0,3,4,8]],
"t3": [3],
"minut": [2,[3,4,6]],
"第二列是词汇译文": [0],
"sa": [1],
"seem": [2],
"sc": [0],
"数字的字符": [0],
"sl": [2],
"so": [2,[0,3,7],5,[4,6,8]],
"caution": [[2,7]],
"email": [0],
"apart": [7],
"exported": [8],
"帮助菜单": [[0,8]],
"intern": [2,[1,4,5]],
"机器翻译": [[4,5],[1,8],3],
"onc": [2,7,3,4,[0,1]],
"one": [7,4,[0,2],[3,5],1,6],
"可识别格式的术语文件": [0],
"它代表原文文件的完整文件名": [0],
"interv": [1,2],
"editoverwritesourcemenuitem": [0],
"则必须在其前面加上": [0],
"omegat.autotext": [0],
"kilobyt": [2],
"enforc": [6,4,[0,2],[1,3]],
"th": [4],
"链接": [0],
"remov": [7,2,6,1,[0,4]],
"tl": [2],
"tm": [6,2,4,0,[1,7],[3,5,8]],
"assist": [[3,5]],
"根据项目的特征更改默认间隔": [1],
"to": [2,7,3,1,4,0,5,6,8],
"v2": [2,1],
"文本搜索": [[3,7],[0,4,8]],
"document.xx": [0],
"括号": [0],
"回车符": [0],
"viewmarkautopopulatedcheckboxmenuitem": [0],
"dialogs": [8],
"有一些筛选器提供了选项": [0],
"projectwikiimportmenuitem": [0],
"插入原文文本时替换匹配项": [1],
"countri": [2,1],
"这只会匹配": [0],
"如果不想翻译某个筛选器所关联的文件": [0],
"un": [2],
"up": [[0,2],7,[3,6],1,[5,8]],
"us": [0],
"partway": [[2,7]],
"newword": [7],
"usual": [[2,4]],
"文件夹中找到这样的文件": [0],
"this": [7,2,4,1,6,5,3,0,8],
"详情请参阅附录": [0],
"opt": [2,0],
"extract": [7,[0,1,6]],
"know": [0],
"换行符": [0],
"region": [[1,2]],
"support": [2,7,3,6,1,[0,4,8]],
"vs": [1],
"changed": [1],
"drop": [5,[2,6]],
"为所有标签保留空格": [0],
"高亮": [[0,7]],
"将文件扩展名关联给筛选器并不足以让筛选器正确处理该文件": [0],
"垂直空白字符": [0],
"关闭后回到原本的片段": [7],
"we": [0,[2,3]],
"unchang": [3],
"autocompleterlistup": [0],
"licenc": [0],
"全局外部搜索": [7,1,[0,4,5,8]],
"omegat.project.bak": [2,6],
"repo_for_omegat_team_project": [2],
"要得到字面匹配更接近于": [1],
"choic": [7,2],
"slight": [[0,5]],
"previous": [4,0,3,5,2,[6,7]],
"projectaccessexporttmmenuitem": [0],
"wide": [2],
"licens": [2,0],
"请根据片段的方向性在此字符后面插入": [0],
"org": [2],
"当它是表达式的最后一个字符时": [0],
"distribut": [7,2],
"本地文件": [2],
"xx": [0],
"sourc": [7,2,4,1,[5,6],3,0],
"none": [1,[4,6,7]],
"type": [2,7,5,[1,3],[0,6],4],
"结尾的三字母组合": [0],
"beyond": [3],
"problem": [[2,5],4],
"terms": [8],
"optionsautocompletehistorypredictionmenuitem": [0],
"选择第四个匹配": [0],
"projectaccesssourcemenuitem": [0],
"脚注": [0],
"yy": [0],
"method": [2,[5,7]],
"contract": [0],
"scroll": [[1,3,5]],
"come": [2,[0,3],6,7,5],
"push": [2],
"exist": [2,[3,7],[4,5],[1,6]],
"readme_tr.txt": [2],
"examples": [8],
"penalti": [6,1],
"exact": [7,4,[2,3,6]],
"的原文文件": [0],
"regist": [2,1,[4,5],[3,7]],
"新建": [0],
"utf8": [0,[4,7]],
"有四条规则需要牢记": [0],
"使用此选项则可以根据关联的注释为这类片段提供翻译": [0],
"copi": [2,7,[1,4,5],0,[3,6]],
"但必须使用": [0],
"out": [7,4,[2,3,5]],
"中不可用的选项": [0],
"get": [2,[0,3,7]],
"dark": [1],
"statist": [4,[6,7],1,2],
"power": [7,0],
"packag": [2],
"制作人员和许可证信息": [4],
"accur": [[6,7]],
"context_menu": [0],
"修改编码声明": [0],
"editsearchdictionarymenuitem": [0],
"tag-valid": [2],
"示例映射": [2],
"ovr": [5],
"alway": [2,6,[1,4],[3,7]],
"readabl": [1],
"help": [[2,3],[4,8],[0,7]],
"或从": [0],
"翻译记忆库匹配": [1,[5,8]],
"数字的表达式": [0],
"之后可以在搜索表达式中用": [0],
"revis": [3],
"repositori": [2,6,4,[5,7]],
"minimum": [6,1],
"date": [[0,1,3,7],6],
"属性": [0],
"data": [2,1,[4,7],[3,6]],
"进入空片段时": [1],
"lowercasemenuitem": [0],
"own": [2,7,5],
"wiki": [[2,6]],
"autocompleterconfirmwithoutclos": [0],
"separ": [[2,7],[0,1,5],3,[4,6]],
"translate": [8],
"filepath": [1,0],
"create": [8],
"replac": [7,4,[1,2],[3,5],6],
"来取消此含义": [0],
"like": [2,[3,7],6,5],
"maxim": [5],
"会匹配任何具有": [0],
"sent": [1],
"之前的部分也是匹配项": [0],
"core": [6],
"前面有": [0],
"让对原文文件与项目设置的外部更改生效": [4],
"空格隔开的重复单词": [0],
"send": [2],
"文件格式": [0],
"如果你需要针对特定案例的帮助": [0],
"但可以在文本编辑器中做到这点": [0],
"here": [1,5,2,7,6,[3,4]],
"note": [[4,5],3,[0,7]],
"purpose": [8],
"line": [2,0,7,[1,5],6],
"noth": [[3,4]],
"link": [3,1,5],
"hero": [0],
"becom": [[3,6]],
"wildcard": [2],
"everybodi": [2],
"git": [2,6],
"contributor": [7],
"可以修改之": [0],
"xx-yy": [0],
"输入或插入从左向右的文本": [0],
"统计数据": [7,[0,1,2,4,6]],
"will": [2,1,7,6,[0,3],5,4],
"来重用它们": [0],
"self-host": [2],
"如果只需要匹配右花括号本身": [0],
"follow": [0,2,7,[3,5],1,[4,6]],
"匹配任何": [0],
"targetlang": [0],
"详情请参见": [0],
"arbitrari": [2],
"optionssetupfilefiltersmenuitem": [0],
"扩展名": [0],
"intend": [[2,6]],
"wild": [7],
"altgraph": [0],
"译文地区代码": [0],
"stats-typ": [2],
"以下句子中的": [0],
"your": [2,3,7,1,[0,4],5,[6,8]],
"并且可以通过罗列每一个需要包括的字符或者通过指定字符区间来指定": [0],
"without": [2,7,[0,1,6],5],
"these": [7,6,2,[0,3,5]],
"xml": [0,2,1],
"文件筛选器要么是局部且专属于给定项目的": [0],
"文本文件": [0],
"sometim": [2],
"serv": [2],
"thus": [[1,2,3]],
"会将": [0],
"摘要": [2,8],
"不换行空格": [7],
"noun": [5],
"存储库映射": [[2,7]],
"insensit": [7],
"按钮来访问修改对话框": [0],
"xdg-open": [0],
"befor": [2,1,7,0,[3,4,6],5],
"格式效果": [0],
"util": [2],
"创建": [4],
"工具": [1,7,2,6,4,[0,3,8]],
"tar.bz": [6],
"much": [[1,2,3]],
"可写项目词汇表位于": [0],
"chapter": [2,[3,7],[4,5]],
"搜索与替换": [7],
"yellow": [4],
"shebang": [0],
"editorskipprevtoken": [0],
"只有": [0],
"转到": [5,3,4,[2,6],[0,1,8]],
"每次调用此功能都会替换此文件中的文本": [0],
"远程文件夹或文件的名称": [2],
"gnu": [2,8],
"wipe": [3],
"可用选项包括": [0],
"blue": [7,5],
"前面有反斜杠": [0],
"suzum": [1],
"target.txt": [[0,1]],
"goe": [7],
"temurin": [2],
"standard": [2,3,[4,5,7],1],
"d\'espac": [2],
"stdout": [0],
"correct": [7,[1,5],[2,3,4,6]],
"traduct": [5],
"标题式大小写": [0],
"因此": [0],
"wish": [[7,8],[2,6]],
"可以根据名称或者是否启用来对筛选器进行排序": [0],
"nameon": [0],
"gotonextnotemenuitem": [0],
"area": [5,[2,7]],
"gpl": [0],
"这包括常规空格和不换行空格以及制表符": [0],
"newentri": [7],
"属性有哪些值不要翻译": [0],
"此选项强制维持": [0],
"请启用此选项": [0],
"list": [1,[0,2],7,3,4,5,6],
"autocompleterprevview": [0],
"externam": [1],
"vowel": [0],
"即空格和换行符": [0],
"的形式": [0],
"作为原文容器并预期将翻译放在": [0],
"最大行长度": [0],
"formats": [8],
"演讲备注": [0],
"medium": [2],
"projectcommittargetfil": [0],
"pear": [0],
"determin": [1,7],
"标准模板": [1],
"combin": [[0,1,2,7],[4,6]],
"po4a": [2],
"japonai": [7],
"omegat.org": [2],
"menus": [0,[2,5,6,7],[3,4]],
"realign": [7],
"object-ori": [7],
"perform": [2,[3,7],[0,1]],
"开头并以": [0],
"当它是": [0],
"alternatives—th": [5],
"这样的": [0],
"maxprogram": [2],
"所有词汇表必须位于": [0],
"with": [2,7,3,4,1,0,5,6],
"对于右方括号": [0],
"pdf": [2,0,4,7],
"there": [2,7,0,[4,5],[1,3,6]],
"右括号": [0],
"autocompletertabledown": [0],
"之间": [0],
"editornextsegmentnottab": [0],
"toolsshowstatisticsmatchesmenuitem": [0],
"focus": [3,2],
"viewdisplaymodificationinfononeradiobuttonmenuitem": [0],
"desir": [2,7,[1,5]],
"approach": [2],
"在单词": [0],
"你还是可以加": [0],
"per": [6,[1,5,7]],
"write": [7,2,[4,5],[0,1]],
"gtk": [1],
"图表": [0],
"如果未勾选": [0],
"project_save.tmx.bak": [[2,6]],
"period": [0,1,2],
"proceed": [2,[4,7]],
"局部后处理命令": [[0,1,7]],
"understand": [2],
"注意": [2,[0,7],4,1,[3,5],6],
"ever": [[1,3]],
"projectaccesswriteableglossarymenuitem": [0],
"even": [2,7,[1,3,4],[0,5,6]],
"aris": [2],
"application_shutdown": [7],
"autocompletertablelastinrow": [0],
"恢复主窗口": [5,[1,4]],
"gui": [2,7,6],
"tmx-standard": [2],
"指南": [2,[3,5,8]],
"在一个类中": [0],
"regexp": [0],
"sentencecasemenuitem": [0],
"语言集合": [1],
"会在对齐的文档中高亮显示的字符串": [0],
"articl": [0],
"editorcontextmenu": [0],
"此对话框提供了各种选项": [0],
"optionssentsegmenuitem": [0],
"robust": [[2,3]],
"bought": [0],
"大多数字符只是匹配其自身": [0],
"optionsaccessconfigdirmenuitem": [0],
"charact": [7,0,4,5,1,2,3],
"framework": [2],
"test.html": [2],
"php": [0],
"xxx": [6],
"原文文件编码和译文文件编码": [0],
"任意": [0],
"instanc": [2,7,[4,5]],
"smalltalk": [7],
"附录": [0,[6,8]],
"arrow": [7,5,[0,3]],
"almost": [[2,4]],
"manner": [3],
"递归": [7],
"访问配置文件夹": [0,[1,2,3,4]],
"earlier": [3],
"pseudotranslatetmx": [2],
"whether": [1,5,7,[0,2,6]],
"unabl": [[1,5]],
"标签简写": [0],
"无论是默认还是附加": [0],
"插入下一个缺失的标签": [0],
"start-up": [2],
"用于指定应匹配前一个字符或分组实例的数量": [0],
"选择上一个匹配": [0],
"targetlanguagecod": [0],
"undock": [5,1],
"editorprevsegmentnottab": [0],
"它们不会匹配诸如": [0],
"absolut": [1],
"uniqu": [7,[4,5]],
"的搜索包含一个": [0],
"bidirect": [4],
"星号": [0],
"详细信息请参阅章节": [0],
"basic": [2,7,0],
"译文文件": [0],
"对于": [0],
"选项和首选项": [0],
"disabl": [7,1,[4,5],2],
"后者属于": [0],
"单词边界": [0],
"extra": [[0,2,4,5]],
"design": [3,2],
"convey": [3],
"command-lin": [2],
"unpack": [2],
"单独出现时": [0],
"accord": [5,[1,7]],
"请在你的报告中附上此文件或其相关的部分": [0],
"conduct": [7],
"projectnam": [0],
"这样的句子中": [0],
"文件夹中": [0],
"omegat.project.yyyymmddhhmm.bak": [2],
"的字符": [0],
"根文件夹的完整路径": [0],
"符号匹配一行的末尾": [0],
"configdir": [2],
"正则表达式": [0,7,1,[2,8]],
"installdist": [2],
"表达式中的每一个字符都是有实际意义的": [0],
"a-z": [0],
"password": [2,1],
"那么": [0],
"gotonextxenforcedmenuitem": [0],
"editordeleteprevtoken": [0],
"eventu": [[2,3]],
"onlin": [7,[0,2,3,4]],
"coffe": [3],
"还有": [0],
"来取消其自身的特殊含义": [0],
"want": [7,2,[0,4],[1,3,6],5],
"processor": [7,3],
"javascript": [7],
"mediawiki": [[4,7],[0,3]],
"input": [4,[1,2]],
"代表单个空格": [0],
"toolkit": [2],
"must": [2,0,1,6],
"要查找普通的句点": [0],
"join.html": [0],
"中所做的所有修改都会存储在指定的配置文件夹中": [0],
"称为": [0],
"omegat.kaptn": [2],
"此文件夹提供了手动安装": [0],
"misplac": [4],
"multi-cel": [7],
"giving": [8],
"自动替换复数格式规范": [0],
"accident": [1],
"pop": [0,4],
"原则": [[3,5,8]],
"表格清单": [8],
"found": [7,4,2,[0,1,6],[3,5]],
"usernam": [2],
"larg": [7,[2,3]],
"范例之一是": [0],
"freez": [2],
"anoth": [2,[4,7]],
"latest": [6],
"pend": [7],
"条规则": [0],
"编辑模式": [0],
"简单的安装和运行说明": [0],
"区间是通过序列中的第一个字符后跟一个连字符再跟上序列中的最后一个字符来定义的": [0],
"片段分割": [0],
"side-by-sid": [2],
"googl": [1],
"总是会复制原文内容": [0],
"re-ent": [1],
"gotoeditorpanelmenuitem": [0],
"viewmarkfontfallbackcheckboxmenuitem": [0],
"had": [3,[0,7]],
"prepar": [2,0],
"词汇表中存在匹配的术语都会显示在": [0],
"可以利用这一点在搜索中对大小写区分进行精细控制": [0],
"align": [7,2,4,[1,3]],
"adjac": [5],
"insertcharsrlm": [0],
"可能是某种变体": [0],
"sourceforg": [2,0],
"structur": [7,6,[0,2],3],
"han": [0],
"semeru-runtim": [2],
"has": [[2,4],[5,7],1,[0,3,6]],
"内容并可用诸如": [0],
"keyword": [7,3],
"下一个备注": [0],
"given": [2,1,6],
"unlock": [5,3],
"autosav": [5],
"last": [4,[1,7],0,[2,5],[3,6]],
"editmultipledefault": [0],
"adapt": [[1,7],3],
"batch": [2],
"mozilla": [[0,2]],
"editfindinprojectmenuitem": [0],
"pro": [1],
"reproduc": [7],
"以及潜在的错误拼写": [0],
"这表示一个不区分大小写的": [0],
"warn": [2,3],
"部分中的站点": [0],
"激进式字体候补": [0],
"按钮": [0],
"获得": [0],
"关闭": [0],
"minimal": [8],
"创建不同译文": [0,[3,4,5,7]],
"overview": [2],
"yes": [7],
"duckduckgo.com": [1],
"yet": [1,[3,4],[2,6,7]],
"windows": [8],
"generic": [1,2],
"colour": [1,[0,4,6,7]],
"chang": [7,6,[1,2],3,0,[4,5]],
"进展的信息": [0],
"pop-up": [5],
"time": [3,2,7,6,4,0,[1,5]],
"远程文件": [2],
"kanji": [0],
"只是一个普通的字符": [0],
"文件位置": [7,2],
"program": [2,[3,7],[0,1,4,5]],
"区间内的单个字母": [0],
"cyan": [4],
"put": [2,6,0,[1,3]],
"python3": [0],
"译文语言": [0],
"就将其变为匹配": [0],
"tran": [0],
"其中句点前面没有反斜杠": [0],
"会使用某个强制性编码": [0],
"iraq": [0],
"dossier": [5],
"以及包含": [0],
"right-click": [7,[4,5],[2,6]],
"项目文件夹的完整路径": [0],
"brunt": [0],
"编辑": [[0,5],7,3,1,4,8],
"authent": [2,1,5],
"retransl": [2],
"tabl": [0,1,[4,5]],
"engin": [5,[1,4],7],
"post-process": [[1,7]],
"four-step": [7],
"smart": [2],
"选项菜单": [[0,8]],
"因为": [0],
"doc-license.txt": [0],
"紧跟在左方括号之后的脱字符将排除掉类中的其他字符": [0],
"中的一个字符": [0],
"theme": [1,7],
"チューリッヒ": [1],
"undesir": [4],
"editor": [7,5,[0,1,3],4,[2,6]],
"pseudotranslatetyp": [2],
"外观": [1,8],
"要插入和删除的内容": [1],
"语言检查器": [0],
"cycl": [4],
"char": [7],
"循环切换": [0],
"检查问题": [0],
"当它是表达式的第一个字符时": [0],
"small": [4],
"projectclosemenuitem": [0],
"分隔的值": [0],
"viewmarknonuniquesegmentscheckboxmenuitem": [0],
"检查规则": [7],
"匹配的序号": [1],
"hit": [7,4],
"如果需要搜索这些字符本身": [0],
"词汇表": [[0,5],7,4,8,[1,3,6],2],
"major": [0,2],
"搜索将匹配集合中的任何一个字符": [0],
"consider": [2],
"titl": [4,7],
"inspir": [7],
"group": [1,[5,7],[2,3]],
"就必须用": [0],
"findinprojectreuselastwindow": [0],
"readme.txt": [2,0],
"languagetool": [4,1,[7,8]],
"文档": [0],
"source.txt": [[0,1]],
"twice": [3],
"files.s": [7],
"histori": [4,[0,1],3],
"exchang": [0],
"request": [2],
"procedur": [2],
"credentials": [8],
"currseg": [7],
"文件的内容": [0],
"their": [7,2,3,0,1,[5,6]],
"generat": [6,2,[1,3,4]],
"这个参数文件包含自定义的外部搜索参数": [0],
"right-end": [5],
"point": [2,1,[0,7]],
"general": [7,2,1,5,0,[3,8]],
"项目文件夹中": [0],
"process": [3,2,7,[0,1],4],
"并不需要事先准备此文件": [0],
"autocompletertrigg": [0],
"instance—a": [5],
"attribut": [0],
"clear": [[4,7]],
"或者诸如": [0],
"acquiert": [1],
"mean": [[1,2,5,7]],
"这个参数文件包含自定义的编辑器快捷键": [0],
"表示零个或一个水平空格字符": [0],
"保存的": [0],
"account": [2,1,[4,6,7]],
"snippet": [7],
"修改它最简单的方法是使用": [0],
"been": [2,4,7,[1,3,6],0],
"dhttp.proxyhost": [2],
"japanes": [[1,2],0],
"常规的": [0],
"要求使用": [0],
"插入原文文本": [1],
"simplifi": [[1,2]],
"alphabet": [[5,7]],
"此筛选器是原始的": [0],
"你还可以调用脚本": [0],
"systemwid": [2],
"editorprevseg": [0],
"选择": [[0,1]],
"trip": [2],
"许多常用的集合都有一个反斜杠后跟一个字母组成的简写形式": [0],
"a-za-z0": [0],
"来查看文件的内容": [0],
"strict": [2],
"模糊匹配排序依据": [1],
"you": [2,7,3,1,4,6,0,5,8],
"jump": [[3,7],[0,4,5]],
"happen": [2],
"www.apertium.org": [1],
"mainstream": [0],
"cours": [[4,6,7]],
"project_save.tmx.tmp": [2],
"tags": [8],
"见上文": [0],
"正则表达式允许你创建字符集合": [0],
"的方式": [0],
"configur": [2,1,[0,7],[3,4,5]],
"nativ": [2,1],
"文档的编码通常在位于": [0],
"unicode": [8],
"此选项在处理仅包含不可翻译文本的": [0],
"preserv": [2,1],
"organ": [0],
"mega": [0],
"项目属性来设置参考词汇表文件夹的位置": [0],
"zurich": [1],
"mirror": [7],
"不在行首处的序列号": [0],
"空白文字": [2],
"不要添加引号": [0],
"是紧跟在左方括号之后": [0],
"optionsworkflowmenuitem": [0],
"用户手册": [0],
"how": [3,[0,1],[2,5],[7,8]],
"希腊字母区": [0],
"releas": [2,[0,4]],
"标签": [0],
"在从右向左的片段中必须从左向右排列的弱方向性字符": [0],
"聚合标签": [0],
"dictroot": [0],
"时使用": [0],
"keybind": [7],
"默认情况下": [0],
"如果软回车符是段落开头": [0],
"xhmtl": [0],
"represent": [7],
"统称为": [0],
"开放文档格式": [0],
"如果原文文档中包含用于控制排版布局的空白字符": [0],
"允许你在文件中进行极其强大的搜索": [0],
"hold": [7,3,5],
"linebreak": [0],
"subdir": [2],
"修复的清单": [0],
"文件夹中的术语文件": [0],
"hyperlink": [5],
"autocompletertableleft": [0],
"count": [4,1],
"forward-backward": [7],
"所使用的图像存储在此文件夹": [0],
"take": [0,[2,7],3,[1,4]],
"如果将其放在没有特殊含义的字符": [0],
"editorlastseg": [0],
"file-source-encod": [0],
"dictionaries": [8],
"some": [2,1,6,[0,3,4,5,7]],
"session": [[3,5,7]],
"但拆分成多行的三字母组合": [0],
"备注": [0],
"要添加筛选器模式": [0],
"alpha": [2],
"大学院博士課程修了": [1],
"just": [3,2,[0,6,7]],
"divid": [0,7],
"primarili": [2],
"grants": [8],
"collabor": [3,2],
"editexportselectionmenuitem": [0],
"home": [0,2],
"disable-location-sav": [2],
"condit": [2],
"print": [[2,7]],
"although": [2,[0,7]],
"projectaccesstargetmenuitem": [0],
"筛选器会检查": [0],
"要么": [0],
"iana": [0],
"忽略": [0],
"visibl": [6],
"匹配其前面或者后面表达式任一者": [0],
"hope": [3],
"soon": [[4,6,7]],
"有关专属于项目的命令": [0],
"aligndir": [2,7],
"system-host-nam": [0],
"action": [7,4,2,5,3,1],
"mymemory.translated.net": [1],
"creat": [2,7,3,6,4,1,5,0],
"python": [7],
"tarbal": [6],
"singl": [7,3,0],
"全局后处理命令": [[0,1,7]],
"括起来的任意数量的字符组成": [0],
"notifi": [5,1],
"请插入一个": [0],
"file": [2,7,6,4,3,0,1,5],
"让其匹配小数点": [0],
"会保留空白字符": [0],
"member": [2,3],
"用户创建的仅使用拉丁字符的文件如果不包含非": [0],
"加号": [0],
"within": [[5,6],[0,2,3]],
"could": [[2,6,7]],
"翻译记忆库": [3,[4,7]],
"trigger": [7,2],
"menu": [4,0,5,1,[3,7],2,6],
"exercis": [2],
"explan": [7,[0,1]],
"probabl": [2],
"文件结构还必须与此筛选器兼容": [0],
"return": [7,3,5],
"invoke-item": [0],
"使用": [0],
"之间的任何一位数字": [0],
"在希腊字母区中的字符": [0],
"尽管看起来令人生畏且复杂": [0],
"并不需要在前面加上反斜杠": [0],
"radio": [7],
"source-pattern": [2],
"find": [0,2,7,3,1,6],
"host": [2,1],
"backward": [4],
"regardless": [2],
"同样": [0],
"workflow": [3,5,8],
"occur": [0],
"autocompletertablepagedown": [0],
"difficult": [2],
"sort": [5,[4,7]],
"fill": [6,2,4],
"用户可以选择三个选项之一": [0],
"但差异": [1],
"forget": [1],
"task": [2,[3,7]],
"警告": [7,[0,1],2,3,4,6,5],
"xliff": [2,0],
"true": [0],
"header": [[4,7]],
"position": [8],
"如果正则表达式只是": [0],
"present": [[0,2],7,[1,3,4,5]],
"多重译文": [5,[3,8]],
"使用模式": [0],
"插入最佳模糊匹配": [[1,2,5,6]],
"belong": [1],
"groovi": [7],
"pre-defin": [3],
"multi-paradigm": [7],
"best": [[3,5],[1,4,7]],
"简单表达式": [0],
"transform": [1],
"execut": [2,7],
"hour": [[2,3]],
"并且脚本必须是可执行的": [0],
"已翻译": [0],
"的分发许可证": [0],
"abov": [2,[1,7],0,[3,5],4],
"messageformat": [1,0],
"stern": [4],
"默认内容": [0],
"compound": [1],
"master": [2,1],
"左花括号": [0],
"要改变具有弱方向性或中性方向性字符": [0],
"underlin": [4,5,1],
"writer": [0],
"如果您需要有关": [0],
"merg": [7,3,[0,1]],
"dalloway": [1],
"rubi": [7],
"resource-bundl": [2],
"会出现在状态栏上": [0],
"项目目录的名称": [0],
"可以通过取消对其勾选框的勾选来禁用它": [0],
"yyyi": [2],
"external_command": [6],
"annot": [3],
"脚本等本地程序来对其进行处理": [0],
"cover": [[0,2]],
"editorselectal": [0],
"集合": [1],
"reflect": [[2,3],[4,6]],
"flexibl": [3],
"有十二个字符具有特殊含义": [0],
"runner": [7,0],
"immedi": [[3,7],[4,5]],
"这个参数文件包含自定义分割参数": [0],
"based": [8],
"pointer": [5],
"distinguish": [3],
"虽然上面的代码中最后两个并不完全匹配": [0],
"condens": [1],
"在未提供片段内容时": [0],
"在项目中搜索": [0],
"omegat-default": [2],
"user.languag": [2],
"regex": [0,3],
"从命令行启动": [0],
"选项让": [0],
"meta": [0],
"它都不会有任何效果": [0],
"并且将其存储在": [0],
"except": [1,[0,2],3],
"此文件描述了": [0],
"nevertheless": [3],
"魔术": [0],
"global": [7,0,1,4,[2,3]],
"标签完全相同": [0],
"识别到并显示在": [0],
"racin": [5],
"ressources": [8],
"全局分割规则": [7,1,[0,4,8]],
"最近变更": [[0,4]],
"unless": [2,[1,3]],
"可以用": [0],
"free": [2,3,[0,7,8]],
"字符来分隔各个片段定义": [0],
"进行搜索是不会匹配": [0],
"左方括号": [0],
"配置文件夹": [0,2,1,[4,8]],
"disappar": [2],
"在所指定的最大字符数之后将长行断开": [0],
"thorough": [[0,2]],
"改进之处和": [0],
"它会在首次往词汇表中添加词条时创建": [0],
"译文文件名模式使用特殊的语法规则": [0],
"软回车处开始新段落": [0],
"face": [1],
"ibm": [[1,2]],
"以及文档属性": [0],
"替换为机器翻译": [0],
"french—if": [1],
"reliabl": [6,2],
"以下句子中的大写": [0],
"放置在序列外的连字符则就只是连字符": [0],
"parsewis": [7],
"注释将显示为标签": [0],
"autocomplet": [0],
"如果为每个词条所显示的上下文描述并非必要或者太长": [1],
"five": [1],
"日志": [0,4],
"下面这些变量变体会生成如下结果": [0],
"omegat-cod": [2],
"repres": [[0,1],2],
"只是告诉搜索功能要按此顺序精确匹配这些字母": [0],
"idx": [6],
"conflict": [2,[0,3]],
"squar": [1],
"因为它永远不能单独使用": [0],
"有关全局命令": [0],
"commerci": [3],
"rule": [7,1,0,4,[2,3],6],
"detect": [4,1,2],
"文件名变量": [0],
"是除了紧跟在左方括号的位置之外的任何位置": [0],
"everi": [2,6,7,[0,4],[1,3]],
"将用作后备": [0],
"右方括号和连字符": [0],
"以上两种对话框都允许你自定义此筛选器所关联的原文和译文文件名模式并可以选择各自的编码": [0],
"autocompleterconfirmandclos": [0],
"how-to": [3,6,4,[0,2,5,7]],
"projectaccesscurrentsourcedocumentmenuitem": [0],
"linux": [0,2,4,5,[1,3,7]],
"linux-install.sh": [2],
"again": [3,[1,2,7]],
"file.txt": [2],
"openxliff": [2],
"此文件夹包含你的拼写词典": [0],
"popup": [7],
"ifo": [6],
"popul": [[1,4,6]],
"为项目创建的可写词汇表将是以": [0],
"comment": [5,0,[4,7]],
"章节提供一些起点": [0],
"excit": [0],
"的前三个字符": [0],
"uncolor": [5],
"窗格部件": [[5,8]],
"intermedi": [2],
"optionsmtautofetchcheckboxmenuitem": [0],
"xx.docx": [0],
"prefix": [6,1,2],
"计算器": [7],
"一些应用程序需要保留某些": [0],
"consist": [4,[2,7],[3,5,6]],
"标记双向算法控制符": [0],
"自动获取翻译": [4,1],
"的作用": [0],
"editorshortcuts.properti": [0],
"扩展插件的标准位置": [0],
"grammat": [[4,7]],
"compress": [6],
"在此处选择": [0],
"水平和垂直空白": [0],
"另请参阅": [0],
"本附录末尾的": [0],
"fail": [[2,3],7],
"itself": [2,7,[0,3,6]],
"仅当该文件已经具有": [0],
"versions": [8],
"由单个单词组成但不以冒号结尾的行": [0],
"对齐文件": [7,4,[0,2,3,8]],
"整个": [0],
"请在列表中选择它并单击": [0],
"或制表符": [0],
"thumb": [7],
"spellchecking": [8],
"针对无日期片段的模板": [1],
"requir": [2,1,7,3,[4,5],6],
"tmotherlangroot": [0],
"viewmarknotedsegmentscheckboxmenuitem": [0],
"页脚": [0],
"event": [7,0,2],
"小写": [0],
"或其他文本序列": [0],
"表达式": [0],
"个字符": [0],
"基于以下条件自行选择编码": [0],
"gotomatchsourceseg": [0],
"appropri": [2,7,[0,3]],
"为所有片段显示": [0],
"excel": [0],
"comma": [0],
"分隔的格式进行记录并以": [0],
"大多数文件格式允许各种可能的编码": [0],
"runn": [7],
"cannot": [2,5],
"创建词条": [0],
"runt": [0],
"让修饰符右侧的表达式片段区分大小写": [0],
"stardict": [6],
"first": [7,5,[2,4],0,[1,3],6],
"omegat.l4j.ini": [2],
"span": [0],
"的表达式": [0],
"quotat": [7,0],
"prefer": [4,[2,5,7],1,6,3,0],
"overridden": [2],
"space": [7,0,[3,5],4,1],
"hard-and-fast": [7],
"ドイツ": [7,1],
"simpl": [1,3,2,[0,7]],
"下一个已译片段": [0],
"from": [2,7,6,5,4,3,[0,1]],
"的实例但不想要": [0],
"editselectfuzzy3menuitem": [0],
"的任意数字": [0],
"bottom": [7,1,5,4],
"templat": [1,7],
"fals": [0,[1,2]],
"project.projectfil": [7],
"uncondit": [6],
"frequenc": [2],
"存储库的同步": [5],
"这样的变体来指定哪一部分归属于文件名或扩展名": [0],
"保持片段为空": [[1,5]],
"结尾": [0],
"相对于给定根目录的原文文件名": [0],
"frequent": [[0,7],[2,3,4]],
"译文文件中的行长度": [0],
"interact": [[2,7]],
"而不是字母": [0],
"outright": [4],
"error": [2,5,4,[3,7]],
"network": [2],
"全局": [0],
"shortcut": [0,[3,4],7,5,2,8],
"public": [2,8],
"briefli": [5],
"则默认情况下": [0],
"首选项让你能定义一些字符串": [0],
"track": [[2,5]],
"tmx2sourc": [[0,2,6]],
"你应当只用它来避免包含使用该筛选器处理的文件的那些过往项目出现兼容性问题": [0],
"对话框中的相同选项则允许你将正则表达式应用于搜索文本和替换文本": [0],
"译文语言代码": [0],
"ini": [2],
"可以直接访问它": [0],
"首选项": [0,1,4,[3,6,7,8]],
"round": [2],
"dhttp.proxyport": [2],
"点击相关表头即可按升序或降序对它们进行排序": [0],
"文件中的注释通常是针对开发人员的": [0],
"按主题": [0],
"仅表示其自身": [0],
"允许在译文中改变标签顺序": [1],
"一个简单的类由": [0],
"subrip": [2],
"筛选器处理": [0],
"编码文字": [0],
"不必在前面加上反斜杠": [0],
"describ": [2,5,0,[3,7]],
"score": [1,7,6],
"的标签": [0],
"用户国家": [2],
"poor": [7],
"此文件夹中会包含最多三个文本文件": [0],
"appendix": [7,1,4,[0,2],[3,5,6]],
"例如标点符号": [0],
"读取参考词汇表时使用的编码取决于其文件扩展名": [0],
"illustr": [3],
"raw": [2],
"unassign": [4],
"指向支持页面的链接": [0],
"离线工作": [2],
"转到菜单": [[0,8]],
"manual": [7,[2,3,4],8,6,0],
"recycl": [2],
"可以在": [0],
"aspect": [3],
"下拉菜单来自定义译文文件名": [0],
"unbeliev": [0],
"appendic": [3],
"正则表达式还提供特殊修饰符来在表达式内指定区分大小写": [0],
"close": [7,2,0,4,5,[1,3]],
"尾注": [0],
"abc": [0],
"abl": [2,[3,4,7]],
"textual": [7,0],
"toolbar.groovi": [7],
"点击": [0],
"iso": [[0,2]],
"isn": [[0,2]],
"optionspreferencesmenuitem": [0],
"内容创建标签的方式": [0],
"当前译文文档": [0],
"red": [[1,6],7],
"act": [6],
"post": [0],
"glossary.txt": [[2,6],[0,4]],
"finish": [7,0],
"placehold": [1],
"add": [2,7,3,6,5,0,4,1],
"chines": [1],
"initi": [[2,7],[1,6]],
"来打开一个类似的对话框": [0],
"对话框": [0],
"equival": [7,1,2,[0,5]],
"表示有关特定含义而非此字符本身": [0],
"respect": [[2,6]],
"rfe": [7],
"shell": [0],
"port": [2],
"entry_activ": [7],
"不显示": [0],
"变量名": [0],
"optionsautocompleteshowautomaticallyitem": [0],
"gotoprevxautomenuitem": [0],
"exec": [0],
"trust": [1,7],
"而非": [0],
"untar": [2],
"其他内容": [0],
"consequ": [[3,7]],
"prevent": [2,[3,4,7]],
"undo": [4],
"glitch": [3],
"下文": [0],
"其中的": [0],
"ishan": [0],
"scope": [[0,3,7]],
"pasta": [0],
"modifi": [7,0,2,3,4,1,[5,6]],
"espac": [2],
"anyth": [[0,2]],
"label": [[1,4]],
"fledg": [2],
"登记全同译文": [0],
"类似地": [0],
"togeth": [3],
"clone": [2],
"targetlanguag": [[0,1]],
"这样的搜索只是在查找单词": [0],
"变量": [0],
"可以定义任意数量的区间": [0],
"匹配任何单个字符": [0],
"backup": [2,6,1,7],
"sensit": [7],
"模式": [[0,7]],
"properti": [5,2,3,[4,6],0,[1,7]],
"editselectfuzzyprevmenuitem": [0],
"number": [7,5,[1,2],4,6,3,0],
"所有新词条会以": [0],
"identifi": [1,3,[0,4],2,7],
"faulti": [2],
"algorithm": [7,4],
"shorter": [7],
"troubleshoot": [2,[3,6,8]],
"显示原文片段": [[0,4,5]],
"distributed": [8],
"script": [7,2,4,0,1,[3,6,8]],
"exit": [[2,7],4],
"system": [2,1,[4,7],[3,6],[0,5]],
"选择第三个匹配": [0],
"spellcheck": [[1,3],6,2,7],
"issu": [4,[1,2],3,6],
"partial": [2],
"要修改某个筛选器所关联的文件扩展名": [0],
"other": [2,7,[0,3],1,[5,6],[4,8]],
"retain": [2],
"local": [2,7,1,4,0,5,6],
"resum": [3],
"segments": [8],
"locat": [2,7,4,3,[0,1],6,5],
"yield": [7],
"允许局部外部搜索命令": [[1,7]],
"你可能需要使用绝对路径": [0],
"如果使用了适用的功能": [0],
"identifiers": [8],
"rle": [[0,4]],
"repo_for_all_omegat_team_project_sourc": [2],
"duplic": [[2,3,7]],
"rlm": [0,4],
"这类更严重的错误拼写": [0],
"会被写入": [0],
"句点": [0],
"强制让": [0],
"批注": [0],
"modifier": [8],
"round-trip": [2],
"映射存储库": [2],
"此文件夹包含直接连接到远程服务器的项目树结构的版本副本": [6],
"correspond": [7,[0,4,5],[1,2,6]],
"c-x": [0],
"computer-assisted": [8],
"mode": [2,7,5,4],
"copies": [8],
"modified": [8],
"toolsshowstatisticsstandardmenuitem": [0],
"all": [7,2,1,4,3,6,0,5],
"border": [5],
"每个项目都包含一个": [0],
"read": [7,2,0,1],
"simplic": [2],
"如果选择使用一个现有文件作为默认词汇表": [0],
"c.t": [0],
"alt": [0,4],
"字符体系": [[0,8]],
"rememb": [2,[3,4],6,7],
"忽略包含": [0],
"real": [[2,5]],
"标签中的": [0],
"unit": [0,7],
"alreadi": [2,[6,7],[1,3]],
"所有变量": [1],
"兼容": [0],
"上的位置": [2],
"collect": [6],
"two-lett": [2,[3,7]],
"例如阿拉伯语文本中的英语摘录": [0],
"位置": [0],
"不是": [0],
"redo": [4],
"链接命令在这里不起作用": [0],
"书签": [0],
"tkit": [2],
"and": [2,7,3,[4,5],1,0,6,8],
"synchron": [2,[3,7],[1,5,6]],
"predict": [1,[0,3]],
"row": [7,[0,4]],
"ani": [2,7,3,6,[0,4],5,1],
"你可以决定是否添加或者修改译文文件的声明": [0],
"应用程序文件夹的位置取决于您的平台和安装": [0],
"ant": [[2,7]],
"此选项会将这类多个连续空白字符在译文文档中转换为单个空格": [0],
"any": [8],
"书签引用": [0],
"第二列和第三列都是可选的": [0],
"unnecessari": [6],
"筛选器": [[0,7]],
"译文文件名": [0],
"helplastchangesmenuitem": [0],
"until": [2,[1,3,7]],
"对话框中更改其名称和位置": [0],
"这里有一些例子": [0],
"的特殊组合": [0],
"omegat.ex": [2],
"reason": [1,[0,2,4,7]],
"thought": [2],
"这些选项指定了": [0],
"sourcetext": [1],
"simultan": [[1,2]],
"表达式定义了需要应用该规则的位置之前的文本模式": [0],
"english": [2,1],
"mistak": [7,1,[3,4]],
"jar": [2],
"之间的任意数字": [0],
"api": [[1,7]],
"editselectfuzzy2menuitem": [0],
"encapsul": [7],
"这些类绝不会只表示用于形成简写的实际字母": [0],
"帮助": [4,[0,8]],
"数据图表": [0],
"打开之前创建的项目": [4],
"letter": [0,4,[3,7],[1,2]],
"grade": [3],
"自定义标签是使用正则表达式定义的标签": [0],
"editornextseg": [0],
"文件": [0,[2,5]],
"相对于存储库": [2],
"将当前项目转换为": [4],
"editselectfuzzynextmenuitem": [0],
"gotonextxautomenuitem": [0],
"worth": [[3,7]],
"read.m": [0],
"对于文本文件": [0],
"点号": [0],
"are": [7,2,1,4,5,6,3,0],
"cloud.google.com": [1],
"taken": [1,[4,6],[2,5,7]],
"readme.bak": [2],
"arg": [2],
"came": [6,2],
"where": [7,2,[0,5],4,[1,3],6],
"替换文本": [0],
"logogram": [0],
"broken": [3],
"vice": [7],
"正则表达式中的大多数字符只是在文本序列中": [0],
"即使术语单独出现也匹配术语组": [1],
"call": [0,[2,7],4,[1,3,5,6]],
"facilit": [3],
"essenti": [2],
"ask": [2,[4,7],1,[3,5]],
"tabul": [2],
"through": [2,7,5,[3,4]],
"此文件包含许多重要的用户首选项": [0],
"toolsshowstatisticsmatchesperfilemenuitem": [0],
"strength": [7],
"及更高版本要求使用": [0],
"run": [7,2,0,1,4,8,[3,5]],
"worri": [3],
"either": [2,[0,3,6,7],1],
"view": [[0,1,5,7]],
"editorshortcuts.mac.properti": [0],
"的可执行文件": [0],
"删除译文文件中的未翻译字符串": [0],
"titlecasemenuitem": [0],
"yourself": [3,2],
"those": [2,[3,7],6,[0,1,5]],
"editcreateglossaryentrymenuitem": [0],
"编辑器窗格": [[0,5]],
"ital": [[3,7]],
"bold": [1,[5,7],3],
"dure": [[2,3,7],6],
"effici": [3],
"的整体布局": [0],
"分组会被编号": [0],
"longer": [2,[0,1,5,7]],
"introduc": [7],
"多和田葉子": [7],
"name": [[1,3],[2,5],7,[0,6]],
"译文的状态更改为": [0],
"notabl": [[2,5]],
"每次进入一个新片段时都会替换此文件中的文本": [0],
"reli": [0,2],
"翻译空原文片段": [0],
"在很多其他应用程序中你也可能会天天用到": [0],
"book": [[0,3,6]],
"show": [7,5,1,2,[3,6]],
"cautious": [2],
"和左": [0],
"是原始的": [0],
"comput": [2,[1,3]],
"这三个文件提供了一种简便的办法来访问某些": [0],
"introduct": [3,[2,8]],
"行首": [0],
"如果出现了多个句点导致文件名和扩展名的划分变得不明确": [0],
"客户": [0],
"editortogglecursorlock": [0],
"enabl": [1,5,4,2,[3,6,7],0],
"associ": [2,[1,4],3,[0,5,7]],
"以七个字母拼出的单词": [0],
"它们是": [0],
"subfold": [2,7,6,4],
"new_fil": [7],
"target": [4,7,1,2,3,6,0,5,8],
"knowledg": [2],
"workfow": [3],
"项目的根文件夹": [2],
"config-dir": [2],
"禁用此选项可以让开头和末尾的空白字符在译文中可修改": [0],
"你可以随意修改它们以改进文档的片段分割或者添加其他通用规则": [0],
"editorskipprevtokenwithselect": [0],
"any—wil": [1],
"基于文件筛选器设置": [0],
"termbas": [0],
"sequenc": [3],
"文件筛选器": [0,7,[1,2,4,8]],
"使用此选项可将未翻译的片段留空": [0],
"因为第一个": [0],
"case": [4,7,2,1,[0,3,6]],
"例如空格": [0],
"item": [0,4,7,3,[1,5]],
"此文件夹可以设置为使用在项目之外的文件夹或其他项目的特定子文件夹之一": [0],
"运行不稳定": [0],
"violet": [4],
"下载团队项目": [2,[0,4]],
"matcher": [0],
"上一个备注": [0],
"文件进行对齐": [4],
"targettext": [1],
"consid": [[0,1,6],[2,3,4]],
"匹配显示模板": [1],
"reset": [7,1],
"详情请参阅": [0],
"style": [[2,7]],
"card": [7],
"care": [3],
"然后再插入": [0],
"任意单个字符": [0],
"orang": [[0,5,7]],
"guard": [2],
"pattern": [1,7,2,0],
"compil": [7],
"caus": [4],
"它们同时匹配大小写字符": [0],
"freedom": [8],
"edittagpaintermenuitem": [0],
"temporarili": [[1,5]],
"protect": [1,3],
"字母": [0],
"例如": [0,4],
"more": [0,7,3,6,[1,2,5],4],
"display": [1,7,4,5,3,[0,6],2],
"unicod": [0,4],
"viewmarknbspcheckboxmenuitem": [0],
"fanci": [0],
"索引条目": [0],
"同上": [4],
"团队": [1,8],
"将普通的小写": [0],
"computer-assist": [3],
"可能并不相同": [0],
"可写": [0],
"shut": [7],
"标记已译片段": [0],
"如果不使用默认配置文件夹": [0],
"overwrit": [5,[4,6],2],
"片段序号": [0],
"path-to-omegat-project-fil": [2],
"whitespac": [0,[2,4]],
"credenti": [1,2,5],
"simpli": [7,[2,3],[0,6]],
"cloud": [2],
"则只使用前后片段或片段标识符来表征备选翻译": [0],
"msgstr": [0],
"untransl": [7,4,5,[1,2,3],6],
"语言": [[1,7],3],
"编码": [0],
"nationalité": [1],
"kind": [1],
"daili": [0],
"resiz": [5],
"的任意大写字符": [0],
"both": [7,2,1,[0,5]],
"most": [2,0,4,[5,7],[1,6,8]],
"delimit": [1,5,[4,7],0],
"omegat.project": [2,6,3,[1,5,7]],
"phrase": [7,3],
"button-bas": [4],
"它们在编辑器中将呈现为单独的片段": [0],
"建议或默认位置如下": [0],
"excludedfold": [2],
"targetcountrycod": [0],
"不带扩展名的原文文件名": [0],
"job": [3,2],
"overtyp": [0],
"选择匹配": [[4,5]],
"名为": [0],
"标记含备注的片段": [0],
"insert": [4,6,5,0,1,[2,7],3],
"continu": [3,[2,7]],
"是特殊的": [0],
"resid": [2],
"highlight": [[4,7],5,[1,6]],
"自动显示相关建议": [0],
"reject": [1],
"原文文件的编码": [0],
"arrang": [1],
"替换为": [0],
"问号": [0],
"messag": [5,2],
"rest": [[2,3]],
"move": [4,7,5,3,[0,1]],
"amount": [2],
"会被复制到译文文档中": [0],
"之类的数字中最后的": [0],
"also": [2,7,[1,3],6,5,4,0],
"enough": [2],
"操作系统默认编码": [0],
"differ": [1,2,7,[3,5],4,0],
"这些片语中只有开头的": [0],
"conson": [0],
"situat": [[0,2]],
"consol": [2],
"mous": [7,[3,4,5]],
"才是匹配的": [0],
"通过使用预配置变量来更改模糊匹配的显示方式": [1],
"front": [2],
"itokenizertarget": [2],
"以粗体显示的筛选器是在当前项目中所使用的": [0],
"viewmarkwhitespacecheckboxmenuitem": [0],
"projects": [8],
"potenti": [[1,4,6]],
"complet": [1,2,[0,7]],
"包含多个单词和空格": [0],
"bak": [2,6],
"将其从可翻译内容中隐藏掉可确保你不会错误地删除或修改它们": [0],
"canon": [0],
"offer": [2,3,5,[0,7]],
"bar": [5],
"built-in": [1],
"或管道": [0],
"complex": [7,2],
"相同": [1],
"jre": [2],
"posit": [4,[0,5,7],1],
"插入匹配或所选内容": [0],
"reus": [2,[3,4],[1,7]],
"secur": [[1,7]],
"译文文件的编码": [0],
"文件中": [0],
"drive": [2],
"拼写检查": [7],
"file-shar": [2],
"alllemand": [7],
"non-gui": [2],
"行长度": [0],
"deal": [7],
"affect": [2],
"icon": [4,5],
"delet": [2,0,[3,4],[1,6,7]],
"proven": [0],
"bcp": [[3,7]],
"projectaccessglossarymenuitem": [0],
"javadoc": [7],
"see": [2,4,7,3,1,6,5,0],
"contain": [6,[2,7],5,3,[0,1],4],
"set": [[2,7],1,6,3,4,5,0,8],
"incorrect": [[6,7]],
"balis": [5],
"fastest": [8],
"column": [7,4,1],
"原文文件名是表征备选译文的元素之一": [0],
"instantan": [2],
"正则表达式并不限于字母数字字符": [0],
"通常都需要开头和末尾的标签才能正确地重新创建译文片段": [0],
"来查找数字": [0],
"project.sav": [2],
"somewhat": [3],
"进行处理": [0],
"详情请参阅章节": [0],
"删除译文": [0],
"最小值": [0],
"featur": [1,7,[2,3,5]],
"offic": [[0,2],[3,7]],
"terminolog": [4],
"repositories.properti": [[0,2]],
"搜索": [0],
"由浅入深": [0],
"parti": [[2,5]],
"双击可编辑字段来进行简单编辑": [0],
"repositories": [8],
"注释中的文本": [0],
"projectsavemenuitem": [0],
"contact": [5],
"xmx6g": [2],
"autocompletertablefirstinrow": [0],
"digit": [[2,7]],
"插入缺失的原文标签": [0],
"用法": [7],
"此字符结束一个分组": [0],
"tmautoroot": [0],
"adjust": [7,[2,6]],
"所选属性将作为可翻译片段呈现在": [0],
"公共首选项": [0],
"compat": [2,[1,7]],
"compar": [7,3],
"cursor": [5,4,3,0,7],
"prototype-bas": [7],
"insertcharslrm": [0],
"包括扩展名": [0],
"中打开": [0],
"上一页": [[0,1,2,3,4,5,6,7]],
"个人贡献者名单": [0],
"decor": [3,7],
"client": [2,6],
"over": [4,2,[1,5,6]],
"six": [3],
"someth": [[1,2]],
"bound": [7],
"以及换行符": [0],
"setup": [2],
"avoid": [2,0,[3,7]],
"foundat": [2],
"targetroot": [0],
"prompt": [2],
"subset": [2],
"select": [4,7,5,[1,2],[0,3],6],
"bin": [0,[1,2]],
"apertium": [1],
"bit": [4],
"bis": [0],
"kaptain": [2],
"meta-inf": [2],
"clipboard": [4],
"由单个单词组成并以冒号结尾的行": [0],
"output": [2],
"projectopenmenuitem": [0],
"autom": [2,[1,3,7]],
"corner": [5],
"这样做会让整个搜索表达式区分大小写": [0],
"model": [[1,7]],
"逗号分隔的值": [0],
"join": [3],
"decis": [6],
"默认项目映射": [2],
"拼写检查器": [1,[0,3,4,6,7,8]],
"whitelist": [2],
"decid": [3,2],
"更新": [1,[2,8]],
"制表符": [0],
"smoother": [[3,7]],
"这里只描述在": [0],
"搜索词典": [0],
"一个斜杠再加一到三个数字组成的代码": [0],
"斜体": [0],
"要避免在加载项目时自动打开原文文件列表窗口": [0],
"begin": [0,3],
"viewmarktranslatedsegmentscheckboxmenuitem": [0],
"template": [8],
"paragraph": [0,7,5,[1,4],3],
"其中每个字符都代表一个有效的潜在匹配项": [0],
"viewer": [5],
"这些文件记录了": [0],
"包括在屏幕上或打印输出时不可见的字符": [0],
"valu": [[0,7],[1,2],4],
"works": [8],
"颜色": [1,4],
"standalon": [1],
"原始文件扩展名": [0],
"ilia": [2],
"language—th": [0],
"world": [2],
"目的": [0],
"运行中的各种内部状态和生成的程序事件消息": [0],
"大写": [0],
"工具菜单": [[0,8]],
"uxxxx": [0],
"全部不保留": [7],
"macos": [8],
"reappli": [4],
"行尾": [0],
"side": [[2,5],[0,7]],
"字母中只有": [0],
"删除此文件也会重置筛选器首选项": [0],
"break": [1,0,[3,7]],
"editselectfuzzy1menuitem": [0],
"change": [8],
"upgrad": [2,1,7],
"tabular": [1],
"其间没有可翻译文本的多个标签会被聚合成单个标签": [0],
"non-default": [2],
"comfort": [[2,7]],
"hide": [7,5,[1,3,4]],
"extran": [[1,7]],
"report": [2,[3,4,7]],
"你可以选择要翻译的其他文档元素": [0],
"在其他文件中具备相同表征的片段将以相同方式翻译": [0],
"autocompleterlistpagedown": [0],
"auto": [4,6,0,2,[1,7]],
"sign": [[0,5]],
"notepad": [[3,5],4],
"document.xx.docx": [0],
"editorskipnexttokenwithselect": [0],
"download": [2,1,3,[0,6,7]],
"split": [7,3,[0,1,4,5]],
"数字和符号": [0],
"oracl": [0],
"editortoggleovertyp": [0],
"集合内容": [1],
"示例": [7],
"administr": [2],
"gradlew": [2],
"level": [7,3],
"modif": [1,4,[2,5],[3,7],[0,6]],
"cascad": [1],
"viewmarklanguagecheckercheckboxmenuitem": [0],
"unseg": [3],
"ubiquit": [4],
"produc": [2,[0,5,7]],
"本手册只介绍一小部分具有特殊含义的字母": [0],
"表示": [0],
"会从可翻译内容中移除开头和末尾的所有空白字符": [0],
"box": [7,0,1],
"此字符指定应该匹配前面那个字符或表达式的零个或更多个实例": [0],
"switch": [7,[1,3,4]],
"total": [5,[4,7]],
"immut": [6],
"bundl": [[1,2]],
"恢复前一操作": [0],
"involv": [[2,7],6],
"dynam": [7],
"macro": [7],
"technic": [0,2],
"src": [2],
"gigabyt": [2],
"标签时": [0],
"control": [4,0,2,3],
"版本": [0],
"在列表中选定筛选器并点击": [0],
"no-team": [2],
"extrem": [[2,3]],
"lissens": [0],
"offici": [0],
"的界面修改它们": [0],
"closest": [1],
"一些首选项在用户界面中没有对应项": [0],
"打开脚本窗口": [4],
"upper": [4],
"ssh": [2],
"用于存储可写词汇表和需要添加到项目中的所有参考词汇表": [0],
"部分": [0],
"environ": [2,0],
"specialti": [2],
"然后可以用文本编辑器打开它并进行修改": [0],
"vari": [5,2,0],
"friend": [0],
"是紧跟在左方括号之后或紧挨在右方括号之前的位置": [0],
"文件可以和": [4],
"的文件": [0],
"在其前面加上反斜杠时没有特殊含义": [0],
"pinpoint": [7,2],
"subtract": [6],
"kde": [2],
"accept": [7,[2,6],1],
"方法": [0],
"表达式这定义此位置之后的文本模式": [0],
"autocompleter": [8],
"access": [7,2,4,1,0,3,6,8],
"currenc": [7,0],
"在未分割的项目中": [0],
"languag": [2,7,1,3,6,0,[4,5]],
"如果": [[0,2]],
"exept": [2],
"current": [4,7,[2,5],[1,6],0,3],
"只有左花括号是特殊的": [0],
"映射成本地文件": [2],
"安装": [2,[3,8]],
"optionsglossaryfuzzymatchingcheckboxmenuitem": [0],
"key": [0,7,[1,5],4,[2,3],8],
"格式": [0,8],
"communic": [5],
"括起一个": [0],
"使用此模式可为译文文件分配和原文文件完全相同的名字": [0],
"此文件夹包含许多日志文件": [0],
"anymor": [2],
"msgid": [0],
"launch": [2,7,1,0,4,3],
"svn": [2,7,6],
"store": [2,7,3,[1,5,6],4,0],
"omegat-license.txt": [0],
"stori": [0],
"confirm": [[0,4,6,7],[1,2]],
"emerg": [2],
"problemat": [3],
"bug": [[0,4]],
"所使用的库存储在此文件夹": [0],
"typic": [2],
"editreplaceinprojectmenuitem": [0],
"but": [2,[0,7],3,6,1,4,5],
"保存自动填充状态": [6,[1,2,4]],
"symbol": [5,[0,2,7]],
"或最后一个": [0],
"定向格式符": [[0,4],8],
"的位置": [0],
"editordeletenexttoken": [0],
"当前原文文档": [0],
"扩展名的文件": [0],
"express": [0,7,1,[2,3]],
"deactiv": [4],
"zero": [7],
"subsequ": [[0,1]],
"variant": [2],
"该命令以带有展开的模板值的字符串的形式传递给": [0],
"written": [[2,4,7]],
"你应当使用": [0],
"gotoprevioussegmentmenuitem": [0],
"新段落创建于": [0],
"竖线": [0],
"gotopreviousnotemenuitem": [0],
"括起一组单个字符": [0],
"stderr": [0],
"editredomenuitem": [0],
"uilayout.xml": [[0,6]],
"verif": [1],
"字符是个非常特殊的字符": [0],
"substitut": [4],
"sourceroot": [0],
"hint": [3],
"意味着从": [0],
"projectʼ": [3],
"sinc": [[0,2,3],7],
"higher": [1,0],
"idea": [[2,7]],
"原文文件编码和译文文件编码字段用下拉菜单列出了所有支持的编码": [0],
"复制后备文本": [0],
"译文区域设置": [0],
"rearrang": [[4,5]],
"wavy-lin": [1],
"对任何一个词汇表做出的修改都会立刻被": [0],
"字体": [1,[3,5]],
"normal": [7,2,[1,6]],
"gradual": [[0,2,6]],
"代理服务器登录": [1,8],
"corrupt": [2],
"之后插入": [0],
"映射到本地文件": [2],
"behav": [2],
"example.email.org": [0],
"要在从左向右的片段中创建从右向左的嵌入文本": [0],
"不仅在": [0],
"当其可用时": [0],
"runtim": [2,0],
"individu": [7,[2,4]],
"reach": [3,2],
"realiz": [3],
"如果要指定不同的编码": [0],
"review": [3,[2,7],[5,6,8]],
"filenam": [[0,7],[1,2,5]],
"要在从右向左的片段中创建从左向右的嵌入文本": [0],
"roam": [0],
"between": [[2,5],[1,7],4,0,3],
"nbsp": [7],
"这是一组被视为单一单元的字符": [0],
"gotosegmentmenuitem": [0],
"可以另行指定非默认值的配置文件夹": [0],
"兼容性": [0],
"following": [8],
"部件时非常方便": [0],
"initialcreationd": [1],
"references—in": [0],
"对于名为": [0],
"flag": [4,1],
"spacebar": [0],
"的小写字符或连字符本身": [0],
"修订信息": [0],
"helpaboutmenuitem": [0],
"请确保译文中也包含它们": [0],
"weak": [7],
"词典": [5,[1,4],[6,8],[0,3,7]],
"place": [[2,7],[4,6],1],
"leav": [[1,3,4],[5,6],2],
"regular": [2,7,0,1,6,[3,5,8]],
"regulax": [0],
"原文和译文文件有时需要不同的编码": [0],
"restart": [2,0,4,1],
"扩展名和预期编码": [[0,8]],
"suggest": [5,1],
"token": [0,[1,2,7],[5,6]],
"filter": [2,7,1,4,3,[0,6]],
"expect": [2,1],
"site": [0,2,1],
"projectroot": [0],
"其处理方式和原生的": [0],
"选择语言检查器的位置": [1],
"right-to-left": [[0,4]],
"omegat.log": [0],
"behaviour": [2,4],
"autocompletertableright": [0],
"配置文件夹的默认位置因系统而异": [0],
"原文文件名模式": [0],
"窗格中": [0],
"tab": [0,[4,5],1],
"plain": [7],
"should": [[1,2],7,[0,3,4,6]],
"tag": [1,3,4,7,2,5,0],
"替换为原文": [0],
"在默认浏览器中打开本手册": [4],
"versa": [7],
"但是": [0],
"模糊匹配": [[4,5,7],3,[1,2],[6,8]],
"tap": [3],
"onli": [7,2,1,[3,4],5,0],
"projectreloadmenuitem": [0],
"区段": [0],
"类来在文本中查找这三个货币符号中的任何一个": [0],
"person": [7,2],
"safe": [2],
"files": [8],
"navig": [3,[4,5,6],2],
"或任何其他适合于工作流程的分类方式来组织": [0],
"简而言之": [0],
"与单独的字母": [0],
"cross-platform": [2],
"但仅针对当前显示在编辑器窗格中的文档": [4],
"本章节介绍各种正则表达式": [0],
"provis": [2],
"tbx": [0,1],
"can": [2,7,3,1,5,0,6,4,8],
"computing": [8],
"satisfi": [[2,7]],
"任意换行符": [0],
"cat": [[0,2,3,7]],
"可以根据所在片段的方向性创建两种不同的嵌入文本": [0],
"duser.countri": [2],
"provid": [2,1,7,5,[0,3],4],
"realli": [4],
"smooth": [3],
"reboot": [2],
"readm": [0],
"readi": [2],
"match": [7,[1,4],6,0,5,3,2,8],
"categori": [0],
"存储库凭据": [2,1],
"fragment": [3],
"align.tmx": [2],
"file2": [2],
"搜索并替换": [0],
"category": [8]
};
