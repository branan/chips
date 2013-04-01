(ns chips.core
  (:import org.lwjgl.LWJGLException
           [org.lwjgl.opengl Display DisplayMode GL11]
           [org.lwjgl.input Keyboard]
           [org.lwjgl.util.glu GLU])
  (:use chips.core.parser)
  (:gen-class))

(defn -main [& args]
  (let [datapath (first args)
        chipdata (parse-file datapath)
        level (nth (:levels chipdata) (Integer. (second args)))]
    (dump-layer (first (:layers level)))))
