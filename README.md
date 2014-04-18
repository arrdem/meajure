# Meajure

A Clojure library which provides rudimentary math with units
capabilities. What makes meajure different from other units libraries?
There are already several good ones:

 - [minderbinder](https://github.com/fogus/minderbinder)
 - [frinj](https://github.com/martintrojer/frinj/)

The answer is that Meajure operates on algebraic units. Frinj and
minderbinder do a good job at converting units, but they aren't really
designed for mixing units in any sort of meaningful way.

## Demo

```Clojure
user> (use 'clojure.algo.generic.arithmetic)
WARNING: + already refers to: #'clojure.core/+ in namespace: user, being replaced by: #'clojure.algo.generic.arithmetic/+
WARNING: * already refers to: #'clojure.core/* in namespace: user, being replaced by: #'clojure.algo.generic.arithmetic/*
WARNING: - already refers to: #'clojure.core/- in namespace: user, being replaced by: #'clojure.algo.generic.arithmetic/-
WARNING: / already refers to: #'clojure.core// in namespace: user, being replaced by: #'clojure.algo.generic.arithmetic//
nil
user> (use 'meajure)
nil
user> (+ 1 1)
2
user> (+ (make-unit 3 :feet 1)
         (make-unit 2 :feet 1))
#meajure.UnitValue{:val 5, :units {:feet 1}}
user> (* (make-unit 3 :feet 1)
         (make-unit 2 :feet 1))
#meajure.UnitValue{:val 6, :units {:feet 2}}
user> (* (make-unit 1 :doge 1)
         (make-unit (/ 127 10000000) :doge -1 :btc 1)
         (make-unit 600 :usd 1 :btc -1))
#meajure.UnitValue{:val 381/50000, :units {:usd 1}}
```

Meajure can do more than this. As units are stored as numeric
exponents, other arithmetic operations such as exponentiation and
roots are well defined. Because the units are not affected by the
`floor`, `ceil`, `abs` and `round` operations, these are supported too
as provided by `clojure.algo.generic.math-functions`.

Other math operations such as logarithms and trig functions are not
well defined in terms of the units of the result, and not supported by
default. However thanks to the open nature of multimethods they are
trivial to implement.

## License

Copyright © 2014 Reid "arrdem" McKenzie

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
