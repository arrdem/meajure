# Meajure

<a href="http://clojars.org/me.arrdem/meajure">
	<img src="http://clojars.org/me.arrdem/meajure/latest-version.svg" />
</a>

A Clojure library which provides rudimentary math with units
capabilities. What makes meajure different from other units libraries?
There are already several good ones:

 - [minderbinder](https://github.com/fogus/minderbinder)
 - [frinj](https://github.com/martintrojer/frinj/)

The answer is that Meajure operates on algebraic units. Frinj and
minderbinder do a good job at converting between units, but they
aren't really designed for mixing units in any sort of meaningful way.
Meajure on the other hand isn't designed for unit conversion, it's
designed for unit tracking.

```Clojure
user> (require '[clojure.algo.generic.arithmetic
                 :refer [+ - / *]])
user> (require '[meajure :refer [make-unit]])
user> (+ (make-unit 3 :feet 1)
         (make-unit 2 :feet 1))
#meajure.UnitValue{:val 5, :units {:feet 1}}
```

Right, so as one would expect, addition between values with the same
units is meaningful and supported. Subtraction between values with the
same units is also supported. What happens if we try to do something
funky and mix units in simple arithmetic?

```Clojure
user> (+ (make-unit 3 :feet 1)
         (make-unit 2 :meters 1))
AssertionError Assert failed: (units-equal? x y)
```

Right. Adding feet and meters directly is potentially meaningful, in
that you can convert between them. This is what Frinj and minderbinder
do. Meajure does not support implicit conversions.


Okay, but what about multiplication? How are those operations defined?

```Clojure
user> (* (make-unit 3 :feet 1)
         (make-unit 2 :feet 1))
#meajure.UnitValue{:val 6, :units {:feet 2}}
```

Awesome! Meajure allows for unit exponentiation... but what about
negative exponents and other numeric cases?

```Clojure
user> (* (make-unit 1 :doge 1)
         (make-unit (/ 127 10000000) :doge -1 :btc 1)
         (make-unit 600 :usd 1 :btc -1))
#meajure.UnitValue{:val 381/50000, :units {:usd 1}}
```

Sweet... unit canceling and negaitve powers work... what about other
math operations that affect exponents?

```Clojure
user> (require '[clojure.algo.generic.math-functions
                 :refer [pow sqrt]])
nil

user> (sqrt (make-unit 3.3))
#meajure.UnitValue{:val 1.816590212458495, :units {}}

user> (sqrt (make-unit 5000 :meters 2))
#meajure.UnitValue{:val 70.71067811865476, :units {:meters 1N}}

user> (sqrt (make-unit 16 :meters 3 :seconds 1))
#meajure.UnitValue{:val 4.0, :units {:seconds 1/2, :meters 3/2}}
```

```Clojure
user> (pow (make-unit 16 :meters 3 :seconds 1) 2)
#meajure.UnitValue{:val 256.0, :units {:seconds 2, :meters 6}}

user> (pow (make-unit 16 :meters 3 :seconds 1) 2.2)
#meajure.UnitValue{:val 445.7218884076158,
                   :units {:seconds 2.2,
				            :meters 6.6000000000000005}}
```

How on earth you'd contrive a power of 2.2 I have no idea, but if you
manage to, Meajure supports it `:D`.

Because the units are not affected by the `floor`, `ceil`, `abs` and
`round` operations, these are supported too as provided by
`clojure.algo.generic.math-functions`.

Other math operations such as logarithms and trig functions are not
well defined in terms of the units of the result, and not supported by
default. However thanks to the open nature of multimethods they are
trivial to implement.

## License

Copyright © 2014 Reid "arrdem" McKenzie

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
