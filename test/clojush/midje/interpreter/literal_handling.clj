; To run these tests with autotest use:
;
;    lein midje :autotest test
;
; This runs everything in the test sub-directory but
; _doesn't_ run all the stuff in src, which midje tries
; to run by default, which breaks the world.

(ns clojush.midje.interpreter.literal-handling
  (:use clojure.test
        midje.sweet
        clojush.interpreter
        clojush.pushstate))

(fact "Evaluating a null instruction returns the same state"
  (execute-instruction nil :test-state) => :test-state)


(facts "Evaluating a scalar constant adds that value to the appropriate stack"
      (let [test-state (make-push-state)]
        (:integer (execute-instruction 8 (make-push-state))) => '(8)
        (:boolean (execute-instruction false (make-push-state))) => '(false)
        (:float (execute-instruction -2.3 (make-push-state))) => '(-2.3)))


(fact "Evaluating a scalar constant pushes it onto the top of the appropriate stack"
      (let [test-state (make-push-state)]
        (:integer 
          (execute-instruction 7 (execute-instruction 8 (make-push-state)))) => '(7 8)))