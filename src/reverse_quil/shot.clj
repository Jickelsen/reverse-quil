(ns ^{:doc "Shooty shots"}
    reverse-quil.shot
    (:require [reverse-quil.paddle :as p]
              [brute.entity :as e])
    (:import [reverse_quil.component Shot Velocity Position]))

(def speed 200)
(def shot-size 10)

(defn create-shot
    "Creates a shot entity"
    [state velocity]
    (let [shot (e/create-entity)
          center-x (-> WIDTH (/ 2) (m/round))
          center-y (-> HEIGHT (/ 2) (m/round))
          shot-size 20
          shot-center-x (- center-x (/ shot-size 2))
          shot-center-y (- center-y (/ shot-size 2))
          angle 0]
        (-> state
            (e/add-entity shot)
            (e/add-component shot (c/->Shot))
            (e/add-component shot (c/->Renderer ))
            (e/add-component shot (c/->Position center-x center-y angle))
            (e/add-component shot (c/->Velocity velocity angle)))))

(defn- move-shots!
    "move the shots according to their velocity"
    [system speed delta]
    (let [movement (* speed delta)]
        (doseq [shot (e/get-all-entities-with-component system Shot)]
          (let [{x :x y :y a :a}
                (merge-with + (e/get-component system shot Position)
                            (e/get-component system shot Velocity))]
              (q/push-matrix)
              (q/translate x y)
              (q/rotate a)
              (e/get-component system shot Renderer)
              (q/pop-matrix)))))

(defn process-one-game-tick
    "Render all the things"
    [system delta]
    (let [paddles (e/get-all-entities-with-component system CPUPaddle)
          ;; not very smart, always goes after the first ball
          ball (first (e/get-all-entities-with-component system Ball))
          b-center (-> (e/get-component system ball Rectangle) :rect (rectangle! :get-center (vector-2*)))]
        (doseq [paddle paddles]
            (let [p-center (-> (e/get-component system paddle Rectangle) :rect (rectangle! :get-center (vector-2*)))]
                (if (< (vector-2! p-center :x) (vector-2! b-center :x))
                    (p/move-paddle! system speed delta CPUPaddle)
                    (p/move-paddle! system (* -1 speed) delta CPUPaddle)))))
    system)
