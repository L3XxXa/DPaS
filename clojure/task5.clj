(def restarts (atom 0 :validator #(>= % 0)))

(defn philosophers
  [count thinking-duration dining-duration periods]
  (let [philosopher
        (fn [i left-fork right-fork thinking-duration dining-duration periods]
          (let [think (fn [i duration] (do
                    (println "Philosopher " i " is thinking")
                    (Thread/sleep duration))), dine
                (fn [i left-fork right-fork duration]
                  (dosync
                    (swap! restarts inc)
                    (println "Philosopher " i " is hungry")
                    (alter left-fork inc)
                    (println "Philosopher " i " got left fork")
                    (alter right-fork inc)
                    (println "Philosopher " i " got right fork")
                    (println "Philosopher " i " is dining")
                    (Thread/sleep duration)
                    (println "Philosopher " i " finished dining")
                    (swap! restarts dec)))]
            (new Thread (fn [] (reduce (fn [_ _] (do (think i thinking-duration)
                                                     (dine i left-fork right-fork dining-duration))) '()
                                       (range periods)))))),
        forks (map (fn [_] (ref 0)) (range count)),
        philosophers (map (fn [i] (philosopher i (nth forks i) (nth forks (mod (+ i 1) count)) thinking-duration dining-duration periods)) (range count))]
    (do
      (doall (map (fn [philosopher] (.start philosopher)) philosophers))
      (doall (map (fn [philosopher] (.join philosopher)) philosophers)))))

(time (philosophers 5 1000 1000 1))
(println "Number of restarts: " @restarts)
