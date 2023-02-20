(ns task2
  (:require [clojure.test :as test])
)

(defn eratosthenes_sieve [seq]
  (cons (first seq) (lazy-seq (eratosthenes_sieve (filter #(not= 0 (mod % (first seq))) (rest seq) ) ) ) ) )

(test/deftest task2-test
  (test/testing "Testing eratosthenes_sieve"
    (test/is (= (nth (eratosthenes_sieve (take 20000 (iterate inc 2))) 0) 2))
    (test/is (= (nth (eratosthenes_sieve (take 20000 (iterate inc 2))) 1) 3))
    (test/is (= (nth (eratosthenes_sieve (take 20000 (iterate inc 2))) 1999) 17389))))
(test/run-tests)