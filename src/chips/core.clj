(ns chips.core
  (:use chips.parser
        chips.game
        chips.gui)
  (:gen-class))

(defn -main [& args]
  (let [chipdata (parse-file "CHIPS.DAT")]
    (loop [state (gui-init (game-init chipdata))]
      (draw-frame state)
      (if-not (time-to-quit?)
        (recur (game-tick (handle-input state))))))
  (gui-cleanup))