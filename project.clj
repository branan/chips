(defproject chips "0.0.1"
  :description "Chips Challenge in Clojure"
  :url "http://github.com/branan/chips"
  :license {:name "Eclipse Public License - v 1.0"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [gloss "0.2.2-beta4"]
                 [org.lwjgl.lwjgl/lwjgl "2.8.5"]
                 [org.lwjgl.lwjgl/lwjgl_util "2.8.5"]
                 [org.lwjgl.lwjgl/lwjgl-platform "2.8.5" :classifier "natives-osx" :native-prefix ""]]
  :main chips.core)
