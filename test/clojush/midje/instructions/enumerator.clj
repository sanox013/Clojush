; To run these tests with autotest use:
;
;    lein midje :autotest test
;
; This runs everything in the test sub-directory but
; _doesn't_ run all the stuff in src, which midje tries
; to run by default, which breaks the world.

(ns clojush.midje.instructions.enumerator
  (:use clojure.test
        midje.sweet
        clojush.interpreter
        clojush.pushstate
        clojush.instructions.enumerator))

(fact "checking wiring"
  (+ 1 1) => 2)