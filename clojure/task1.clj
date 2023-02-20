(defn same_letter
  [lst a]
  (filter (fn [x] (let [[f s] x] (not= f s)))
          (map (fn [x] (str a x)) lst)))

(defn make_comb
  [el1 el2]
  (reduce (fn [acc x] (concat acc (same_letter el1 x))) () el2))

(defn search_dublicates
  [x l] 
  (reduce (fn [acc y] (if (= x y) true
                                  acc)) false l))

(defn clean [alphabet] (reduce (fn [acc x] (if (= (search_dublicates x acc) true) acc (cons x acc))) () alphabet))

(defn main_fun
  [alphabet N]
  (if (= N 0) ()
              (let [clean_alphabet (clean alphabet)]
                (reduce (fn [acc _] (make_comb acc clean_alphabet)) clean_alphabet (range (dec N))))))

(let [alphabet (list "c" "b" "a")
      N 4]
  (println (main_fun alphabet N)))