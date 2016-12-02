;; squirrel_play.clj
;; an example problem for clojush, a Push/PushGP system written in Clojure
;; Nic McPhee, mcphee@morris.umn.edu, 2016

(ns clojush.problems.ec-ai-demos.squirrel-play
  (:use [clojush.pushgp.pushgp]
        [clojush.random]
        [clojush pushstate interpreter]
        clojush.instructions.common))

;;;;;;;;;;;;
;; The squirrels in Palo Alto spend most of the day playing. In particular,
;; they play if the temperature is between 60 and 90 (inclusive). Unless it
;; is summer, then the upper limit is 100 instead of 90.
;; Given an int temperature and a boolean is_summer, return true if the
;; squirrels play and false otherwise.
;; Taken from CodingBat: http://codingbat.com/prob/p135815

(def input-set
  [[70, false]
   [70, true]
   [95, false]
   [95, true]
   [90, false]
   [90, true]
   [50, false]
   [50, true]
   [100, false]
   [100, true]
   [105, true]
   [59, false]
   [59, true]
   [60, false]
   [60, true]
   [61, true]
   [61, false]
   [89, false]
   [89, true]
   [91, false]
   [91, true]
   [99, false]
   [99, true]
   [101, true]
   [101, false]
   [105, true]
   [105, false]
   [200, true]
   [200, false]])

(defn expected-output
  [inputs]
  (let [[temperature is-summer] inputs]
    (and (>= temperature 60)
         (if is-summer
           (<= temperature 100)
           (<= temperature 90)))))

; Make a new push state, and then add every
; input to the special `:input` stack.
; You shouldn't have to change this.
(defn make-start-state
  [inputs]
  (reduce (fn [state input]
            (push-item input :input state))
          (make-push-state)
          inputs))

; The only part of this you'd need to change is
; which stack(s) the return value(s) come from.
(defn actual-output
  [program inputs]
  (let [start-state (make-start-state inputs)
        end-state (run-push program start-state)
        result (top-item :boolean end-state)]
    result))

(defn all-errors
  [program]
  (doall
    (for [inputs input-set]
      (let [expected (expected-output inputs)
            actual (actual-output program inputs)]
        (if (= expected actual)
          0
          1)))))

(def atom-generators
  (concat
    ; Include all the instructions that act on integers and booleans
    ; Could have :exec here, but I just am limiting things to exec-if
    (registered-for-stacks [:integer :boolean])
    (list 'exec_if)
    ; A bunch of random numbers in case that's useful.
    ; (list (fn [] (lrand-int 100)))
    ; The three numeric constants that are specified in the problem statement
    (list 60 90 100)
    ; The two inputs
    (list 'in1 'in2)))

(def argmap
  {:error-function all-errors
   :atom-generators atom-generators
   :population-size 500
   })
