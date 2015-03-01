(ns reverse-quil.core
  (:import [java.awt.event KeyEvent])
  (:require [quil.core :as q]
            [quil.middleware :as m]))

(def game-state (atom {:pos {:x 250 :y 250} 
                       :velocity {:x -10 :y -0.1}}))

(def game-history (atom [@game-state]))

(def tt (atom false))

(add-watch game-state :history
  (fn [_ _ _ n]
    (when-not (= (last @game-history) n)
      (swap! game-history conj n))))

(defn put! [m k v]
  (swap! m assoc k v))

(defn update-in! [m ks f & args]
  (swap! m #(apply (partial update-in % ks f) args)))

(def speed 10)
(def gravity 0.5)
(def char-width 50)
(def char-height 50)
(def WIDTH 500)
(def HEIGHT 500)

(defn setup []
  (q/rect-mode :center)
  (q/frame-rate 60))

(defn move [state]
  (update-in state [:pos] #(merge-with + state % (:velocity state))))

(defn apply-gravity [state]
  (update-in state [:velocity :y] #(+ % gravity)))

(defn bounds [state]
  (-> state
      (update-in
       [:pos] #(identity {:x (min WIDTH (:x %) )
                          :y (min HEIGHT (:y %) )}))
      (#(assoc % :velocity {
                            :x (if (>= (:x (:pos %)) WIDTH)
                                 0
                                 (if (<= (:x (:pos %)) 0)
                                   0
                                   (:x (:velocity %))))
                            :y (if (>= (:y (:pos %)) HEIGHT)
                                 0
                                 (if (<= (:y (:pos %)) 0)
                                   0
                                   (:y (:velocity %))))}))))

(defn go-up [y]
  (- y speed))
(defn go-down[y]
  (+ y speed))
(defn go-right [x]
  (+ x speed))
(defn go-left [x]
  (- x speed))

(defn key-pressed []
  (if (not @tt) 
    (cond
     (= (q/key-code) KeyEvent/VK_W)
     (update-in! game-state [:velocity :y] go-up)
     (= (q/key-code) KeyEvent/VK_S)
     (update-in! game-state [:velocity :y] go-down)
     (= (q/key-code) KeyEvent/VK_A)
     (update-in! game-state [:velocity :x] go-left)
     (= (q/key-code) KeyEvent/VK_D)
     (update-in! game-state [:velocity :x] go-right)))
  (if (= (q/key-code) KeyEvent/VK_SHIFT) 
    (swap! tt #(not %))))

(defn update-state [state]
  (-> state
      apply-gravity
      move
      bounds))

(defn draw-state [state tt]
  (if tt
    (q/background 155 165 55)
    (q/background 225 125 75))
  (q/stroke 65 35 31)
  (q/fill 133 70 63)
  (q/ellipse (:x (:pos state)) (:y (:pos state)) char-width char-height))

(defn time-travel []
  (if (> (count @game-history) 1)
    (do (swap! game-history pop)
        (reset! game-state (last @game-history)))
    (reset! tt false)))

(defn iteration []
  "Inspired by https://gist.github.com/nbeloglazov/7920049"
  (if @tt
    (time-travel)
    (swap! game-state update-state))
  (draw-state @game-state @tt))

(q/defsketch reverse-quil
  :title "A game-like simulation built with Quil, with game-state undo"
  :size [WIDTH HEIGHT]
  :setup setup
  :draw iteration
  :key-pressed key-pressed
  )
