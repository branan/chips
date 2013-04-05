(ns chips.game)

(defn build-cell [top bot]
  {:top top :bot bot})

(defn load-level [state lvl]
  (let [leveldata (nth (:levels (:data state)) lvl)
        bot (first (:layers leveldata))
        top (second (:layers leveldata))]
    (assoc state
      :world (vec (map build-cell top bot)))))

(defn idx-first-match [pred col]
  (first (keep-indexed #(if (pred %2) %1) col)))

(defn player-in-cell? [cell]
  (let [player-ids [108 109 110 111]]
    (or (some #(= (:top cell) %) player-ids)
        (some #(= (:bot cell) %) player-ids))))

(defn locate-player [state]
  (let [player-pos (idx-first-match #(player-in-cell? %) (:world state))]
    (-> state
        (assoc :player-y (quot player-pos 32))
        (assoc :player-x (mod player-pos 32)))))
        
(defn game-init [chipdata]
  (-> {:data chipdata, :tick -1, :last-move -9999} ; Our first real tick will be tick 0, and we want the last move time to be sufficiently old.
      (load-level 0)
      (locate-player)))

(defn assoc-cell [state pos tile]
  (let [cell (nth (:world state) pos)
        new-cell (assoc cell :bot tile)]
    (assoc state :world (assoc (:world state) pos new-cell))))

(defn next-tick [state]
  (assoc state :tick (inc (:tick state))))

(defn move-player-up [state]
  (if (:key-u state)
    (assoc state :player-y (dec (:player-y state)))
    state))

(defn move-player-down [state]
  (if (:key-d state)
    (assoc state :player-y (inc (:player-y state)))
    state))

(defn move-player-left [state]
  (if (:key-l state)
    (assoc state :player-x (dec (:player-x state)))
    state))

(defn move-player-right [state]
  (if (:key-r state)
    (assoc state :player-x (inc (:player-x state)))
    state))

(defn update-player-cell [state old-pos]
  (-> state
      (assoc-cell old-pos 0)
      (assoc-cell (+ (* (:player-y state) 32) (:player-x state)) 110)))

(defn update-player-pos [state]
  (let [old-pos (+ (* (:player-y state) 32) (:player-x state))]
    (-> state
        (move-player-up)
        (move-player-down)
        (move-player-left)
        (move-player-right)
        (update-player-cell old-pos))))

(defn move-player [state]
  (let [tick (:tick state)
        last-move (:last-move state)]
    (if (> (- tick last-move) 4) ; I'm not entirely sure this isn't an off by one error
      (update-player-pos state)
      state)))

(defn game-tick [state]
  (-> state
      (next-tick)
      (move-player)))
  