;; padding_problem.clj
;; Yuting, Shinny, & Ai, Fall 2016


(ns clojush.problems.padding_problem.padding_problem
  (:use [clojush.pushgp.pushgp]
        [clojush.random]
        [clojush pushstate interpreter]
        clojush.instructions.common
        clojush.instructions.vectors))

;;;;;;;;;;;;
;; Our padding problem is defined as :
;; Write a function that given a list of non negative integers,
;; arranges them such that they form the largest possible number.
;;For example, given [50, 2, 1, 9], the largest formed number is 95021.
;; Taken from http://www.shiftedup.com/2015/05/07/five-programming-problems-every-software-engineer-should-be-able-to-solve-in-less-than-1-hour


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
   [33, 4, 2, 2, 9]
   [32, 5, 6]
   [77, 9, 12, 3]
   [56, 7]
   [11, 8, 19, 5, 10]
   [78, 5]
   [54, 80, 67, 98]
   [34, 80]
   [37, 83, 19, 53]
   [70, 12, 40]
   [44, 8]])

; Our expected-output function, will generating correct result for this padding problem
; code modified from http://www.shiftedup.com/2015/05/08/solution-to-problem-4
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
        top-int (top-item :integer end-state)]
    top-int))


(defn abs [n] (max n (- n)))


(defn all-errors
  [program]
  (doall
    (for [inputs input-set]
      (let [expected (expected-output inputs)
            actual (actual-output program inputs)]
        (if (= actual :no-stack-item)
          1000
          (abs (- expected actual)))))))


(def atom-generators
  (concat
    (registered-for-stacks [:integer :boolean :string :exec :vector])

    ; have to find a way to pass into the right number of vector's elements
    (list 'in1 'in2)))

(def argmap
  {:error-function all-errors
   :atom-generators atom-generators
   :population-size 500
   })
