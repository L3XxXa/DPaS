(ns task3.clj
  (:require [clojure.test :as test]))

(defn handle-chunk
  [chunks, filter-f]
  (if (empty? chunks)
    (list)
  (concat
    (let [part-size (int (Math/ceil (/ (count (first chunks)) 8)))
           parts (partition-all part-size (first chunks))]
      (->> parts (map (fn [part] (future (doall (filter filter-f part)))))
           (doall) (map deref) (flatten)))
    (lazy-seq (handle-chunk (rest chunks) filter-f)))))

(defn parallel_filter
  [filter-f sequence]
    (handle-chunk (partition-all 1000 sequence) filter-f))

(defn heavy_even?
  [arg]
  (Thread/sleep 1) (even? arg))

(test/deftest test-adder
  (test/testing "Testing parallel filter"
    (test/is (=  80 (nth (parallel_filter heavy_even? (range 1000000)) 40)))
    )
    (time (=  8000 (nth (parallel_filter heavy_even? (range 1000000)) 4000)))
    (time (=  8000 (nth (filter heavy_even? (range 1000000)) 4000)))
  )

(test/run-tests)