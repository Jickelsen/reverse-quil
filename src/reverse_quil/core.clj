(ns reverse-quil.core
  (:require [quil.core :as q]
            [reverse-quil.update :as dynamic-update]
            [reverse-quil.setup :refer [tt]]
            ;; [reverse-quil.setup :as dynamic-setup]
            [brute.entity :as e]
            [reverse-quil.component :as c]
            ;; [reverse-quil.shot :as s]
            [brute.system :as s]
            [clojure.math.numeric-tower :as m])
  (:import [java.awt.event KeyEvent]
           [reverse_quil.component Ship Ship1 Ship Position Velocity Renderer]))

(def WIDTH 500)
(def HEIGHT 500)
(def FRAMERATE 60)

;; (defn classx [c & args] (class c))
;; (defmulti bounds classx)
;; (defmethod bounds Position [c & args]
  ;; {:x (min WIDTH (:x c) )
   ;; :y (min HEIGHT (:y c) )
   ;; :a (:a c)})




;; (defn time-travel []
  ;; (if (> (count @game-history) 1)
    ;; (do (swap! game-history pop)
        ;; (reset! game-state (last @game-history)))
    ;; (reset! tt false)))

;; (defn iteration []
  ;; "Inspired by https://gist.github.com/nbeloglazov/7920049"
  ;; (if @tt
    ;; (time-travel)
    ;; (swap! game-state update-state))
  ;; (draw-state @game-state @tt)
  ;; (:on-render main-screen))

(def game-state (atom 0))

(def game-history (atom [@game-state]))


;; (add-watch game-state :history
  ;; (fn [_ _ _ n]
    ;; (when-not (= (last @game-history) n)
      ;; (swap! game-history conj n))))

(defn render-ship []
  (q/fill 50 80 50)
  (q/rect -2 0 5 14)
  (q/fill 150 180 150)
  (q/triangle 0 -10 25 0 0  10)
  (q/fill 30 100 30)
  (q/ellipse 8 0 8 8))

(defn create-ship
    "Creates a ship entity"
    [state]
    (let [ship (e/create-entity)
          center-x (-> WIDTH (/ 2) (m/round))
          center-y (-> HEIGHT (/ 2) (m/round))
          ship-size 20
          ship-center-x (- center-x (/ 200 2))
          ship-center-y (- center-y (/ 200 2))
          angle 0]
        (-> state
            (e/add-entity ship)
            (e/add-component ship (c/->Ship))
            (e/add-component ship (c/->Ship1))
            (e/add-component ship (c/->Renderer render-ship))
            (e/add-component ship (c/->Position center-x center-y angle))
            (e/add-component ship (c/->Velocity 0 0 angle)))))

(defn- start
    "Create all the initial entities with their components"
    [system]
  ;; (s/create-shot game-state)
    (create-ship system))

(defn create-systems
    "register all the system functions"
    [system]
    (-> system
        (s/add-system-fn dynamic-update/update-state)
        (s/add-system-fn dynamic-update/draw-state)
    ))

(defn state-reset []
  (do
     (-> (e/create-system)
         (start)
         (create-systems)
         (as-> s (reset! game-state s)))))

(defn setup []
  (do
    (state-reset)
     (q/frame-rate FRAMERATE)))

(defn iteration []
  (do
     (q/background 155 165 55)
     (reset! game-state (s/process-one-game-tick @game-state (/ 1000 FRAMERATE)))
     ))

(defn key-pressed []
  (if (not @tt) 
    (let [ship1 (first (e/get-all-entities-with-component @game-state Ship1 ) )]
         (cond
          (= (q/key-code) KeyEvent/VK_W)
          (do (println "Up!")
              (reset! game-state (e/update-component @game-state ship1 Velocity dynamic-update/go-up)))
          (= (q/key-code) KeyEvent/VK_S)
          (reset! game-state (e/update-component @game-state ship1 Velocity dynamic-update/go-down))
          (= (q/key-code) KeyEvent/VK_A)
          (reset! game-state (e/update-component @game-state ship1 Velocity dynamic-update/go-left))
          (= (q/key-code) KeyEvent/VK_D)
          (reset! game-state (e/update-component @game-state ship1 Velocity dynamic-update/go-right))
          (= (q/key-code) KeyEvent/VK_F)
          (state-reset))))
  (if (= (q/key-code) KeyEvent/VK_SHIFT) 
    (swap! tt #(not %)))
  (if (= (q/key-code) KeyEvent/VK_ALT) 
    (do (println "Reset state")
        (state-reset))))

(q/defsketch reverse-quil
  :title "A game-like simulation built with Quil, with game-state undo"
  :size [WIDTH HEIGHT]
  :setup setup 
  :draw iteration
  :key-pressed key-pressed
  )