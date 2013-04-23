(ns chips.gui
  (:import [javax.imageio ImageIO]
           org.lwjgl.LWJGLException
           org.lwjgl.BufferUtils
           [org.lwjgl.opengl Display DisplayMode GL11 GL12 Util]
           [org.lwjgl.input Keyboard]
           [org.lwjgl.util.glu GLU])
  (:require [clojure.java.io :refer [file]] ))

(defn load-texture [filename]
  (let [image (ImageIO/read (file filename))
        width (.getWidth image)
        height (.getHeight image)
        size (* width height)
        pixels (int-array size)
        buffer (BufferUtils/createIntBuffer size)]
    (.getRGB image 0 0 width height pixels 0 width)
    (.put buffer pixels)
    (.rewind buffer)
    (GL11/glTexImage2D
     GL11/GL_TEXTURE_2D
     0
     GL11/GL_RGB
     width height
     0
     GL12/GL_BGRA
     GL12/GL_UNSIGNED_INT_8_8_8_8_REV
     buffer)
    (Util/checkGLError)
    {:width width :height height}))

(defn gui-init [state]
  (Display/setDisplayMode (new DisplayMode 512 512))
  (Display/setTitle "Chips")
  (Display/setFullscreen false)
  (Display/setVSyncEnabled true)
  (Display/create)

  (GL11/glMatrixMode GL11/GL_PROJECTION)
  (GLU/gluOrtho2D 0.0 9.0 9.0 0.0) ;; OpenGL has 0 at the bottom, but we want it at the top. So we flip it in our projection matrix.

  (GL11/glEnable GL11/GL_TEXTURE_2D)
  (Util/checkGLError)
  (GL11/glClearColor 0.0 0.0 0.0 0.0)
  (Util/checkGLError)
  (GL11/glTexParameteri GL11/GL_TEXTURE_2D GL11/GL_TEXTURE_MIN_FILTER GL11/GL_NEAREST)
  (Util/checkGLError)
  (GL11/glTexParameteri GL11/GL_TEXTURE_2D GL11/GL_TEXTURE_MAG_FILTER GL11/GL_NEAREST)

  (let [texinfo (load-texture "TILES.GIF")
        tile-size (/ (:height texinfo) 16)
        win-size (* tile-size 9)]
    (Display/setDisplayMode (new DisplayMode win-size win-size)))
  (Display/releaseContext)
  state)

(defn draw-cell [row col id]
  (let [row-step 0.0625
        col-step 0.07692307692
        tex-row (* (mod id 16) row-step)
        tex-col (* (quot id 16) col-step)]
    (GL11/glTexCoord2f tex-col tex-row)
    (GL11/glVertex2i col row)
    (GL11/glTexCoord2f tex-col (+ tex-row row-step))
    (GL11/glVertex2i col (+ row 1))
    (GL11/glTexCoord2f (+ tex-col col-step) (+ tex-row row-step))
    (GL11/glVertex2i (+ col 1) (+ row 1))
    (GL11/glTexCoord2f (+ tex-col col-step) tex-row)
    (GL11/glVertex2i (+ col 1) row)))

(defn get-cell [state row col]
  (let [cell (first (nth (:world state) (+ col (* row 32))))]
    (if (nil? cell)
      0
      cell)))

; The makeCurrent/releaseContext pair here isn't very efficient, but
; makes it possible to call this function from the REPL, which likes
; to toss things into different threads. OpenGL doesn't play nice with
; threads
(defn draw-frame [state]
  (Display/makeCurrent)
  (GL11/glClear GL11/GL_COLOR_BUFFER_BIT)
  (GL11/glBegin GL11/GL_QUADS)
  (let [first-row (- (:player-y state) 4)
        first-col (- (:player-x state) 4)]
    (doseq [row (range 0 9)
            col (range 0 9)]
      (draw-cell row col (get-cell state (+ first-row row) (+ first-col col)))))
  (GL11/glEnd)
  (Display/update)
  (Display/sync 20)
  (Display/releaseContext))

(defn handle-input [state]
  (-> state
      (assoc :key-u (Keyboard/isKeyDown Keyboard/KEY_UP))
      (assoc :key-d (Keyboard/isKeyDown Keyboard/KEY_DOWN))
      (assoc :key-l (Keyboard/isKeyDown Keyboard/KEY_LEFT))
      (assoc :key-r (Keyboard/isKeyDown Keyboard/KEY_RIGHT))))

(defn time-to-quit? []
  (Display/isCloseRequested))

(defn gui-cleanup []
  (Display/destroy))