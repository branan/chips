(ns chips.core.parser
  (:use gloss.core
        gloss.io
        [clojure.java.io]))

;; Everything from here through "(defcodec option..." is busted and
;; unused. Options are currently treated as an opaque blob.
(defcodec leveltime {:type :time, :data (ordered-map :size :byte :time :uint16-le)})
(defcodec levelchips {:type :time, :data (ordered-map :size :byte, :chips :uint16-le)})
(defcodec levelname {:type :title, :data (finite-frame :byte (string :ascii :delimiters [ \z ]))})
(defcodec trapbutton
  {:type :trap,
   :data (repeated
          (ordered-map
           :button-x :uint16-le
           :button-y :uint16-le
           :trap-x :uint16-le
           :trap-y :uint16-le
           :unused :uint16-le)
          :prefix (prefix :byte #(/ % 10) #(* % 10)))})
(defcodec clonebutton
  {:type :clone,
   :data (repeated
          (ordered-map
           :button-x :uint16-le
           :button-y :uint16-le
           :machine-x :uint16-le
           :machine-y :uint16-le)
          :prefix (prefix :byte #(/ % 8) #(* % 8)))})
(defcodec levelpass {:type :pass, :data (finite-block :byte)})
(defcodec levelhint {:type :hint, :data (finite-frame :byte (string :ascii :delimiters [ \z ]))})
(defcodec plainpass {:type :plainpass, :data (finite-frame :byte (string :ascii :delimiters [ \z ]))})
(defcodec monsters
  {:type :monsters,
   :data (repeated
          (ordered-map
           :monster-x :byte
           :monster-y :byte)
          :prefix (prefix :byte #(/ % 2) #(* % 2)))})

(defcodec option
  (header
   (enum :byte
         {:time 1,
          :chips 2,
          :title 3,
          :trap 4,
          :clone 5,
          :pass 6,
          :hint 7,
          :plainpass 8,
          :monster 10})
   {:time leveltime,
    :chips levelchips,
    :title levelname,
    :trap trapbutton,
    :clone clonebutton,
    :pass levelpass,
    :hint levelhint,
    :plainpass plainpass,
    :monster monsters}
   :type))
;; Everything above here is broken and unused

(defcodec level
  (ordered-map
   :levelnum :uint16-le
   :timelimit :uint16-le
   :numchips :uint16-le
   :compression (enum :uint16-le {:compressed 1, :uncompressed 0})
   :layers [(finite-block :uint16-le)
            (finite-block :uint16-le)]
   :options (finite-block :uint16-le)))
   ;; :options (finite-frame :uint16-le
   ;;           (repeated option :prefix :none))))


;; top-level gloss codec parsing a Chip's Challenege data file
(defcodec chips-dat-codec
  (ordered-map
   :magic (enum :uint32-le {:ccmagic 174764})
   :levels(repeated
            (finite-frame :uint16-le level)
            :prefix :uint16-le)))

(defn transform-element [coll key trans]
  (assoc coll key (trans (key coll))))

(defn third [seq]
  (nth seq 2))

;; Everything about this function makes me sad
(defn rle-decode [buf]
  (loop [result () data buf]
    (if (empty? data)
      result ; When the buf is empty, just return our result
      (let [the-byte (bit-and (first data) 255)] ; Otherwise, take the next byte from buf and decode it
        (if (= the-byte 255)
          (let [count (bit-and (second data) 255) ; If we are RLE, the next two bytes are count and value
                value (bit-and (third data) 255)]
            (recur
             (concat result (repeat count value))
             (nthnext data 3))) ; and continue with the rest of the data
          (recur ; non-rle case
           (concat result [the-byte]) ; append byte to our data
           (rest data))))))) ; and continue with the rest of the buffer

(defn decode-layer [layer]
  (let [layer (first layer)
        buf (byte-array (.limit layer))]
    (.rewind layer)
    (.get layer buf)
    (rle-decode (vec buf))))

(defn decode-level [level]
  (->
   (transform-element level :layers #(map decode-layer %))
   (transform-element ,,,,, :options #(first %))))

(defn parse-buffer [buffer]
  (let [dat (decode chips-dat-codec buffer)]
    (transform-element dat :levels #(map decode-level %))))

(defn parse-file [^String filename]
  (let [file-size (.length (file filename))
        buffer (byte-array file-size)]
    (.read (input-stream filename) buffer)
    (parse-buffer buffer)))

(defn dump-line [line]
  (dorun (map #(if (= 1 %)
                 (print "#")
                 (print "."))
              line))
  (println ""))

(defn dump-layer [layer]
  (dorun (map dump-line (partition 32 layer))))
