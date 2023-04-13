(ns task4)

(declare supply-msg)
(declare notify-msg)

(defn storage
  [ware notify-step & consumers]
  (let [counter (atom 0 :validator #(>= % 0)),
        worker-state {:storage counter,
                      :ware ware,
                      :notify-step notify-step,
                      :consumers consumers}]
    {:storage counter,
     :ware ware,
     :worker (agent worker-state)}))

(defn factory
  [amount duration target-storage & ware-amounts]
  (let [bill (apply hash-map ware-amounts),
        buffer (reduce-kv (fn [acc k _] (assoc acc k 0))
                          {} bill),
        worker-state {:amount amount,
                      :duration duration,
                      :target-storage target-storage,
                      :bill bill,
                      :buffer buffer}]
    {:worker (agent worker-state)}))

(defn source
  [amount duration target-storage]
  (new Thread
       (fn []
         (Thread/sleep 300)
         (send (target-storage :worker) supply-msg amount)
         (recur))))

(defn supply-msg
  [state amount]
  (swap! (state :storage) #(+ % amount))      
  (let [ware (state :ware),
        cnt @(state :storage),
        notify-step (state :notify-step),
        consumers (state :consumers)]
    (when (and (> notify-step 0)
               (> (int (/ cnt notify-step))
                  (int (/ (- cnt amount) notify-step))))
      (println (.format (new java.text.SimpleDateFormat "hh.mm.ss.SSS") (new java.util.Date))
               "|" ware "amount: " cnt))
    (when consumers
      (doseq [consumer (shuffle consumers)]
        (send (consumer :worker) notify-msg ware (state :storage) amount))))
  state)                 

(defn notify-msg
  [state ware storage-atom amount]
  (locking storage-atom
  (let [bill (state :bill),
        buffer (state :buffer),
        needed  (min (- (bill ware) (buffer ware)) amount),
        internal-buffer
        (if (and (>= (bill ware) (buffer ware)) (> amount needed))
              (do
                (swap! storage-atom #(- % needed))
                (update buffer ware #(+ % needed)))
            buffer)]                 
        (if (= bill internal-buffer)
          (do
            (Thread/sleep (state :duration))
            (send ((state :target-storage) :worker) supply-msg (state :amount))
            (assoc state :buffer (reduce-kv (fn [acc k _] (assoc acc k 0)) {} bill)))
          (assoc state :buffer internal-buffer)))))

(def safe-storage (storage "Safe" 1))
(def safe-factory (factory 1 3000 safe-storage "Metal" 3))
(def cuckoo-clock-storage (storage "Cuckoo-clock" 1))
(def cuckoo-clock-factory (factory 1 2000 cuckoo-clock-storage "Lumber" 5 "Gears" 10))
(def gears-storage (storage "Gears" 20 cuckoo-clock-factory))
(def gears-factory (factory 4 1000 gears-storage "Ore" 4))
(def metal-storage (storage "Metal" 5 safe-factory))
(def metal-factory (factory 1 1000 metal-storage "Ore" 10))
(def lumber-storage (storage "Lumber" 20 cuckoo-clock-factory))
(def lumber-mill (source 5 4000 lumber-storage))
(def ore-storage (storage "Ore" 10 metal-factory gears-factory))
(def ore-mine (source 2 1000 ore-storage))

(defn start []
  (.start ore-mine)
  (.start lumber-mill))


(defn stop []
  (.stop ore-mine)
  (.stop lumber-mill))

(agent-error (gears-factory :worker))
(agent-error (metal-storage :worker))

(defn run []
(do
  (start)
  (Thread/sleep 60000)
  (stop)
  (println(agent-error(gears-factory :worker)))
  (println @(ore-storage :storage))
  (println @(safe-storage :storage))))

(run)
