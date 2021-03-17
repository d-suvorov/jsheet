jsheet
======

_jsheet_ is a simplistic spreadsheet editor built merely for ~~educational purposes~~ fun.

Features
--------

_jsheet_ supports four basic types of values and building formulae over them.
The supported types are: boolean, double precision floating-point numbers,
strings and ranges. The formula starts with `=` by convention and consists of:
- A literal value, such as `false`, `true`, `1.46`, `"abracadabra"`. Numeric
  literals in exponential form are not supported;
- A reference in A1-format, such as `B52`. By default, references are shifted
  during copy-and-paste, however, absolute references are also supported and
  denoted with dollar-sign;
- A range, comprised of two references, as in `A1:A10`;
- A function call, such as `pow(2, A1)`;
- Common mathematical operators (`+`, `-`, `*`, `/`), relational operators
  (`==`, `!=`, `<`, `<=`, `>`, `>=`);
- Conditional expressions, such as `if (A1 == 42) then "yay!" else 1`.

Supported functions are:
- `pow(b, e)` takes `b` to the power of `e`;
- `sum(r)` calculates the sum of a range `r`;
- `length(s)` returns the length of a string `s`.

Build and Run
-------------

Prerequisites: install Java Runtime Environment (version 11 or higher) and Maven on your machine.
Then run:

```
mvn clean package
java -jar target/jsheet.jar &
```

.