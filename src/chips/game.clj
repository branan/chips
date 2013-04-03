(ns chips.game)

(defn build-cell [top bot]
  {:top top :bot bot})

(defn load-level [state lvl]
  (let [leveldata (nth (:levels (:data state)) lvl)
        bot (first (:layers leveldata))
        top (second (:layers leveldata))]
    (assoc state
      :world (map build-cell top bot))))

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
  (-> {:data chipdata}
      (load-level 0)
      (locate-player)))

(defn game-tick [state]
  state)
  