(ns ^{:author "Reid McKenzie"
      :doc "Math with units
            This library implements algo.generic math operations
            across values which have attached symbolic units. This
            library makes no attempt to provide transition functions
            or automatic conversion, it simply provides unit tracking
            through algebraic operations."}

  meajure.core
  (:require [clojure.algo.generic
             [arithmetic :refer :all]
             [comparison :as c]
             [math-functions :refer :all]])
  (:refer-clojure :exclude [- + / *]))


;; Exponent helpers
;;--------------------------------------------------------------------
(def exponent-map
  {:yotta   1.0E24
   :Y       1.0E24
   :zetta   1.0E21
   :Z       1.0E21
   :exa     1.0E18
   :E       1.0E18
   :peta    1.0E15
   :P       1.0E15
   :tera    1.0E12
   :T       1.0E12
   :giga    1.0E9
   :G       1.0E9
   :mega    1.0E6
   :M       1.0E6
   :kilo    1.0E3
   :k       1.0E3
   :hecto   1.0E2
   :h       1.0E2
   :deca    1.0E1
   :da      1.0E1
   ;; --------------
   :deci    1.0E-1
   :d       1.0E-1
   :centi   1.0E-2
   :c       1.0E-2
   :milli   1.0E-3
   :m       1.0E-3
   :micro   1.0E-6
   :μ       1.0E-6
   :u       1.0E-6
   :satoshi 1.0E-8
   :s       1.0E-8
   :nano    1.0E-9
   :n       1.0E-9
   :pico    1.0E-12
   :p       1.0E-12
   :femto   1.0E-15
   :f       1.0E-15
   :atto    1.0E-18
   :a       1.0E-18
   :zepto   1.0E-21
   :z       1.0E-21
   :yocto   1.0E-24
   :y       1.0E-24
   })

;; Math with units
;;--------------------------------------------------------------------
(defrecord UnitValue [val units]
  Object
  (toString [this]
    (->> (:units this)
         (map (fn [[k v]]
                (str ", " k
                     (if (contains? #{1, 1.0} v)
                       "" (str " " v)))))
         (apply str)
         (format "#meajure/unit [%s%s]"
                 (:val this)))))

(defmethod print-method UnitValue [o ^java.io.Writer w]
  (.write w (.toString o)))

(defn make-unit
  "λ Numeric → [Object → Numeric]* → UnitValue

  Helper for nicely building UnitValues"
  [v & kvs]
  {:pre [(or (empty? kvs)
             (even? (count kvs)))]}
  (->> kvs
       (partition 2)
       (reduce (partial apply assoc) {})
       (->UnitValue v)))

(defn parse-unit
  "Parses a unit vector, being a sequence matching this pattern
  [numeric-base power-of-ten? ((keyword expr)|keyword)+]

  The single arity case is the entry point, the double arity case is an
  implementation detail used to implement the ((keyword expr)|keyword)+
  pattern.

  Warning:
    This parser will misbehave if a unit type in the first position
    collides with a power of 10 term. For instance, this will misbehave:

    [1 :T 4]

    The parser reads this as 1 Terra, and then fails to match the 4 as a
    unit to a power.

  Examples:
    [1 :doge]                     → [1 :doge 1]
    [100 :feet 2]                 → [100 :feet 2]
    [100 :kilo :metre :second -1] → [100000 :metre 1 :second -1]
  "

  ([[base & opts]]
     (if (contains? exponent-map (first opts))
       (parse-unit (make-unit (* (get exponent-map (first opts))
                                 base))
                   (rest opts))
       (parse-unit base opts)))

  ([acc opts]
     (let [[k v & more] opts]
       (cond (empty? opts)
             acc

             (and (keyword? k)
                  (or (number? v)
                      (list? v)))
             (recur (* acc
                       (make-unit 1 k (eval v)))
                    more)
             
             (keyword? k)
             (recur (* acc
                       (make-unit 1 k 1))
                    (rest opts))

             true
             (throw (Exception.
                     (str "Invalid parser state! Check unit format! "
                          [acc k v more])))))))

(defn elimimate-zeros [{:keys [units] :as v}]
  (reduce (fn [x k]
            (if (zero? (get units k))
              (update-in x [:units] dissoc k) x))
          v (keys units)))

(defn scalar? [x]
  (or (number? x)
      (and (number? (:val x))
           (empty? (:units x)))))

(defn has-units? [x]
  (or (instance? UnitValue x)
      (and (map? x)
           (:val x))))

(defn to-scalar [n]
  {:pre [(has-units? n)]}
  (:val n))

(defn simplify [x]
  {:pre [(has-units? x)]}
  (-> x elimimate-zeros))

(defn units-equal?
  [x y]
  {:pre [(has-units? x)
         (has-units? y)]}
  (= (:units x)
     (:units y)))


;; Addition operations... units must be equal or illegal
;;--------------------------------------------------------------------
(defmethod + [UnitValue UnitValue]
  [x y]
  {:pre [(units-equal? x y)]}
  (-> x
      (update-in [:val] + (:val y))
      (simplify)))

(defmethod + [java.lang.Number UnitValue]
  [x y]
  (assert false "addition with scalars and units is illegal!"))

(defmethod + [UnitValue java.lang.Number]
  [x y]
  (assert false "addition with scalars and units is illegal!"))

(defmethod - [UnitValue UnitValue]
  [x y]
  {:pre [(units-equal? x y)]}
  (-> x
      (update-in [:val] - (:val y))
      (simplify)))

(defmethod - [java.lang.Number UnitValue]
  [x y]
  (assert false "subtraction with scalars and units is illegal!"))

(defmethod - [UnitValue java.lang.Number]
  [x y]
  (assert false "subtraction with scalars and units is illegal!"))


;; Multiplicative operations
;;--------------------------------------------------------------------
(defmethod * [UnitValue UnitValue]
  [x y]
  (-> x
      (assoc
          :val (* (:val x) (:val y))
          :units (merge-with +
                             (:units x)
                             (:units y)))
      simplify))

(defmethod * [java.lang.Number UnitValue]
  [x y]
  (assoc y
    :val (* x (:val y))))

(defmethod * [UnitValue java.lang.Number]
  [x y]
  (assoc x
    :val (* (:val x) y)))

(defmethod / [UnitValue UnitValue]
  [x y]
  (-> x
      (assoc
          :val (/ (:val x) (:val y))
          :units (merge-with + (:units x)
                             (reduce (fn [m [k v]]
                                       (assoc m k (* v -1)))
                                     {} (:units y))))
      simplify))

(defmethod / [java.lang.Number UnitValue]
  [x y]
  (assoc y
    :val (/ x (:val y))))

(defmethod / [UnitValue java.lang.Number]
  [x y]
  (assoc x
    :val (/ (:val x) y)))


;; Other math ops which are trivially meaningful
;;--------------------------------------------------------------------
(defmethod abs UnitValue [x]
  (update-in x [:val] abs))

(defmethod ceil UnitValue [x]
  (update-in x [:val] ceil))

(defmethod floor UnitValue [x]
  (update-in x [:val] floor))

(defmethod round UnitValue [x]
  (update-in x [:val] round))

(defmethod pow [UnitValue java.lang.Number] [x n]
  (-> x
      (update-in [:val] pow n)
      (update-in [:units]
                 (fn [mapping]
                   (->> (for [[k v] mapping]
                          [k (* v n)])
                        (into {}))))))

(defmethod sqrt UnitValue [x]
  (-> x
      (update-in [:val] sqrt)
      (update-in [:units]
                 (fn [mapping]
                   (->> (for [[k v] mapping]
                          [k (/ v 2)])
                        (into {}))))))


;; Implement comparisons
;;--------------------------------------------------------------------
(defmethod c/pos? UnitValue [x]
  (c/pos? (to-scalar x)))

(defmethod c/neg? UnitValue [x]
  (c/neg? (to-scalar x)))

(defmethod c/zero? UnitValue [x]
  (c/zero? (to-scalar x)))

(defmethod c/= [UnitValue UnitValue] [x y]
  {:pre [(units-equal? x y)]}
  (c/= (to-scalar x)
       (to-scalar y)))

(defmethod c/> [UnitValue UnitValue] [x y]
  {:pre [(units-equal? x y)]}
  (c/> (to-scalar x)
       (to-scalar y)))

(defmethod c/< [UnitValue UnitValue] [x y]
  {:pre [(units-equal? x y)]}
  (c/< (to-scalar x)
       (to-scalar y)))

(defmethod c/>= [UnitValue UnitValue] [x y]
  {:pre [(units-equal? x y)]}
  (c/>= (to-scalar x)
       (to-scalar y)))

(defmethod c/<= [UnitValue UnitValue] [x y]
  {:pre [(units-equal? x y)]}
  (c/<= (to-scalar x)
       (to-scalar y)))
