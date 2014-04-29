# Meajure

<a href="http://clojars.org/me.arrdem/meajure">
	<img src="http://clojars.org/me.arrdem/meajure/latest-version.svg" />
</a>

A Clojure library which provides rudimentary math with units
capabilities. The name is entirely [gtrak](http://github.com/gtrak)'s
fault. What makes meajure different from other units libraries?  There
are already several good ones:

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
user> (require 'meajure)
user> (+ #meajure/unit [3 :feet]
         #meajure/unit [2 :feet])
#meajure/unit [5, :feet]
```

Right, so as one would expect, addition between values with the same
units is meaningful and supported. Subtraction between values with the
same units is also supported. What happens if we try to do something
funky and mix units in simple arithmetic?

```Clojure
user> (+ #meajure/unit [3 :feet]
         #meajure/unit [2 :meters])
AssertionError Assert failed: (units-equal? x y)
```

Right. Adding feet and meters directly is potentially meaningful, in
that you can convert between them. This is what Frinj and minderbinder
do. Meajure does not support implicit conversions.


Okay, but what about multiplication? How are those operations defined?

```Clojure
user> (* #meajure/unit [3 :feet]
         #meajure/unit [2 :feet])
#meajure/unit [6, :feet 2]
```

Awesome! Meajure allows for unit exponentiation... but what about
negative exponents and other numeric cases?

```Clojure
user> (* #meajure/unit [1 :doge]
         #meajure/unit [(/ 127 10000000) :doge -1 :btc]
         #meajure/unit [600 :usd :btc -1])
#meajure/unit [381/50000, :usd]
```

Sweet... unit canceling and negaitve powers work... what about other
math operations that affect exponents?

```Clojure
user> (require '[clojure.algo.generic.math-functions
                 :refer [pow sqrt]])
nil

user> (sqrt #meajure/unit [3.3])
#meajure/unit [1.816590212458495]

user> (sqrt #meajure/unit [5000 :meters 2])
#meajure/unit [70.71067811865476, :meters 1N]

user> (sqrt #meajure/unit [16 :meters 3 :seconds 1])
#meajure/unit [4.0, :seconds 1/2, :meters 3/2]
```

```Clojure
user> (pow #meajure/unit [16 :meters 3 :seconds 1] 2)
#meajure/unit [256.0 :seconds 2 :meters 6]

user> (pow #meajure/unit [16 :meters 3 :seconds 1] 2.2)
#meajure/unit [445.7218884076158,
	           :seconds 2.2,
			   :meters 6.6000000000000005]
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

The reader literal syntax for meajure units also provides support for
common powers of ten. For example..

```Clojure
user> #meajure/unit [3 :kilo :doge]
#meajure/unit [3000.0, :doge]
user> #meajure/unit [3 :giga :ton :tnt]
#meajure/unit [3.0E9, :tnt, :ton]
```

This composes with normal units just like you'd expect

```Clojure
user> #meajure/unit [16 :tera :feet 2]
#meajure/unit [1.6E13, :feet 2]
```

Unfortunately this comes with the caviat that SI prefixes cannot be
used as name of the _first_ type. For instance, this is defined to be
invalid, because `:tera` is reserved to mean the SI prefix when it
occurs in the first sequential type exponent position.

```Clojure
user> #meajure/unit [16 :tera 2]
Exception Invalid parser state! Check unit format!
```

## License

Copyright © 2014 Reid "arrdem" McKenzie

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
