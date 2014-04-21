(ns ^{:author "Reid McKenzie"
      :doc "Math with units
            This library implements algo.generic math operations
            across values which have attached symbolic units. This
            library makes no attempt to provide transition functions
            or automatic conversion, it simply provides unit tracking
            through algebraic operations."}

  meajure
  (:require [clojure.algo.generic
             [arithmetic :refer :all]
             [comparison :as c]
             [math-functions :refer :all]])
  (:refer-clojure :exclude [- + / *]))


;; Math with units
;;------------------------------------------------------------------------------
(defrecord UnitValue [val units])

(defn make-unit
  "λ Numeric → [Object → Numeric]+ → UnitValue

  Helper for nicely building UnitValues"
  [v & kvs]
  {:pre [(even? (count kvs))]}
  (->UnitValue v (->> kvs
                      (partition 2)
                      (reduce (partial apply assoc) {}))))

(defn elimimate-zeros [{:keys [units] :as v}]
  (reduce (fn [x k]
            (if (zero? (get units k))
              (update-in x [:units] dissoc k) x))
          v (keys units)))

(defn scalar? [x]
  (and (number? (:val x))
       (empty? (:units x))))

(defn has-units? [x]
  (or (instance? UnitValue x)
      (and (map? x)
           (:val x))))

(defn to-scalar
  [n]
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
(defmethod + [meajure.UnitValue meajure.UnitValue]
  [x y]
  {:pre [(units-equal? x y)]}
  (-> x
      (update-in [:val] + (:val y))
      (simplify)))

(defmethod + [java.lang.Number meajure.UnitValue]
  [x y]
  (assert false "addition with scalars and units is illegal!"))

(defmethod + [meajure.UnitValue java.lang.Number]
  [x y]
  (assert false "addition with scalars and units is illegal!"))

(defmethod - [meajure.UnitValue meajure.UnitValue]
  [x y]
  {:pre [(units-equal? x y)]}
  (-> x
      (update-in [:val] - (:val y))
      (simplify)))

(defmethod - [java.lang.Number meajure.UnitValue]
  [x y]
  (assert false "subtraction with scalars and units is illegal!"))

(defmethod - [meajure.UnitValue java.lang.Number]
  [x y]
  (assert false "subtraction with scalars and units is illegal!"))


;; Multiplicative operations
;;--------------------------------------------------------------------
(defmethod * [meajure.UnitValue meajure.UnitValue]
  [x y]
  (-> x
      (assoc
          :val (* (:val x) (:val y))
          :units (merge-with +
                             (:units x)
                             (:units y)))
      simplify))

(defmethod * [java.lang.Number meajure.UnitValue]
  [x y]
  (assoc y
    :val (* x (:val y))))

(defmethod * [meajure.UnitValue java.lang.Number]
  [x y]
  (assoc x :val (* (:val x) y)))

(defmethod / [meajure.UnitValue meajure.UnitValue]
  [x y]
  (-> x
      (assoc
          :val (/ (:val x) (:val y))
          :units (merge-with + (:units x)
                             (reduce (fn [m [k v]]
                                       (assoc m k (* v -1)))
                                     {} (:units y))))
      simplify))

(defmethod / [java.lang.Number meajure.UnitValue]
  [x y]
  (assoc y :val (/ x (:val y))))

(defmethod / [meajure.UnitValue java.lang.Number]
  [x y]
  (assoc x :val (/ (:val x) y)))


;; Other math ops which are trivially meaningful
;;--------------------------------------------------------------------
(defmethod abs meajure.UnitValue [x]
  (update-in x [:val] abs))

(defmethod ceil meajure.UnitValue [x]
  (update-in x [:val] ceil))

(defmethod floor meajure.UnitValue [x]
  (update-in x [:val] floor))

(defmethod round meajure.UnitValue [x]
  (update-in x [:val] round))

;; TODO: ponder
;; (defmethod log meajure.UnitValue [x]
;;   (as-> x v
;;         (update-in v [:val] log)
;;         (update-in v [:units]
;;                    (fn [mapping]
;;                      (->> (for [[k v] mapping]
;;                             [k (/ v Math/E)])
;;                           (into {}))))))

(defmethod pow [meajure.UnitValue java.lang.Number] [x n]
  (-> x
      (update-in [:val] pow n)
      (update-in [:units]
                 (fn [mapping]
                   (->> (for [[k v] mapping]
                          [k (* v n)])
                        (into {}))))))

(defmethod sqrt meajure.UnitValue [x]
  (-> x
      (update-in [:val] sqrt)
      (update-in [:units]
                 (fn [mapping]
                   (->> (for [[k v] mapping]
                          [k (/ v 2)])
                        (into {}))))))

;; Implement comparisons
;;--------------------------------------------------------------------
(defmethod c/pos? meajure.UnitValue [x]
  (c/pos? (to-scalar x)))

(defmethod c/neg? meajure.UnitValue [x]
  (c/neg? (to-scalar x)))

(defmethod c/zero? meajure.UnitValue [x]
  (c/zero? (to-scalar x)))

(defmethod c/= [meajure.UnitValue meajure.UnitValue] [x y]
  {:pre [(units-equal? x y)]}
  (c/= (to-scalar x)
       (to-scalar y)))

(defmethod c/> [meajure.UnitValue meajure.UnitValue] [x y]
  {:pre [(units-equal? x y)]}
  (c/> (to-scalar x)
       (to-scalar y)))

(defmethod c/< [meajure.UnitValue meajure.UnitValue] [x y]
  {:pre [(units-equal? x y)]}
  (c/< (to-scalar x)
       (to-scalar y)))

(defmethod c/>= [meajure.UnitValue meajure.UnitValue] [x y]
  {:pre [(units-equal? x y)]}
  (c/>= (to-scalar x)
       (to-scalar y)))

(defmethod c/<= [meajure.UnitValue meajure.UnitValue] [x y]
  {:pre [(units-equal? x y)]}
  (c/<= (to-scalar x)
       (to-scalar y)))
