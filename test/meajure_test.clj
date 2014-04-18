(ns meajure-test
  (:require [meajure :refer :all]
            [clojure.algo.generic
             [arithmetic :refer :all]
             [comparison :as c]
             [math-functions :as f]]
            [clojure.test :refer :all])
  (:refer-clojure :exclude [+ - / *]))


(defmacro u [& more]
  `(make-unit ~@ more))


(deftest arithmetic-tests
  (testing "Addition"
    (are [expected got]  (c/= expected got)
         (+ (u 3) (u 4))   (u 7)

         (+ (u -1) (u 0))  (u -1)

         (+ (u 1 :doge 1)
              (u 1 :doge 1)) (u 2 :doge 1)))

  (testing "Subtraction"
    (are [expected got]  (c/= expected got)
         (- (u 3) (u 4))   (u -1)

         (- (u -1) (u 0))  (u -1)

         (- (u 1 :doge 1)
              (u 1 :doge 1)) (u 0 :doge 1)))

  (testing "Multiplication"
    (are [expected got] (c/= expected got)
         (* (u 3) (u 4))   (u 12)

         (* (u 3 :feet 1)
              (u 2 :feet 1)) (u 6 :feet 2)

              (* (u 3
                      :feet 1
                      :second 1)
                   (u (/ 3.2808)
                      :feet -1
                      :meter 1)) (u (/ 3 3.2808)
                                    :second 1
                                    :meter 1)))

  (testing "Division"
    (are [expected got] (c/= expected got)
         (/ (u 16) (u 2)) (u 8)

         (/ (u 1 :doge 1)
            (u 1 :doge 1)) (u 1)

         (/ (u 2 :doge 2)
            (u 1 :doge 1)) (u 2 :doge 1))))


(deftest function-tests
  (testing "abs"
    (is (c/= (f/abs (u 1 :doge 1))
             (f/abs (u -1 :doge 1))))
    
    (is (c/= (f/abs (u -1.0))
             (u 1.0))))

  (testing "ceil"
    (is (c/= (u 3.0)
             (f/ceil (u 2.9))))

    (is (c/= (u 2.0 :foo 1)
             (f/ceil (u 1.999999 :foo 1)))))

  (testing "floor"
    (is (c/= (u 2.0 :foo 1)
             (f/floor (u 2.1 :foo 1))))

    (is (c/= (u 2.0)
             (f/floor (u 2.1)))))

  (testing "round"
    (is (c/= (u 2)
             (f/round (u 2.1))))

    (is (c/= (u 5 :feet 1)
             (f/round (u 4.618123 :feet 1))))))


(deftest comparison-tests
  (testing "Equality"
    (is     (c/= (u 1 :doge 1)
                 (u 1 :doge 1))))

  (testing "Comparison >"
    (is     (c/> (u 1) (u 0)))
    (is     (c/> (u -1) (u -2)))
    (is     (c/> (u Integer/MAX_VALUE) (u Integer/MIN_VALUE))))

  (testing "Comparison <"
    (is     (c/< (u 0) (u 1)))
    (is     (c/< (u -1) (u Integer/MAX_VALUE)))
    (is     (c/< (u Integer/MIN_VALUE) (u Integer/MAX_VALUE))))

  (testing "zero?"
    (is (c/zero? (u 0)))
    (is (c/zero? (u 0 :feet 1)))
    (is (c/zero? (u 0.0 :feet 5))))

  (testing "neg?"
    (is (c/neg? (u -1)))
    (is (c/neg? (u -1 :meter 1 :second 1))))

  (testing "pos?"
    (is (c/pos? (u 1)))
    (is (c/pos? (u 1 :foot 1 :second 1)))))
