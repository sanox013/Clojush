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
  [[70, 50, 6, 51]
   [87, 123, 3, 55]
   [93, 12, 3, 32]
   [95, 1, 3]
   [90, 92]
   [60, 10, 30]
   [50, 21, 16]
   [50, 2, 1, 9]
   [13, 12, 9, 8]
   [100, 1, 32]
   [105, 2, 3, 34, 53]
   [59, 20, 34, 32]
   [69, 12, 3]
   [62, 23, 4, 4]
   [33, 4, 2, 2, 9]])

; Our expected-output function, will generating correct result for this padding problem
(defn expected-output
  [inputs]
  (let [sorted-inputs (sort (fn [x y] (> (read-string (clojure.string/join "" [x y])) (read-string (clojure.string/join "" [y x])))) inputs)]
    (read-string (clojure.string/join "" sorted-inputs))))

(expected-output [50, 56, 5])


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
        result (top-item end-state)] ;removed ":boolean", since our result would be a number.
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
