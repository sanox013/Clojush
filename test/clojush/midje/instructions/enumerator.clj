; To run these tests with autotest use:
;
;    lein midje :autotest test
;
; This runs everything in the test sub-directory but
; _doesn't_ run all the stuff in src, which midje tries
; to run by default, which breaks the world.

(ns clojush.midje.instructions.enumerator
  (:require [clojush.types.enumerator :as enum])
  (:use midje.sweet
        [clojush pushstate interpreter]
        clojush.instructions.enumerator))


;; some fixtures to use below
;;
(def counter (enum/construct-enumerator [1 2 3 4 5] 0))
(def counter-on-exec-state (push-item counter :exec (make-push-state)))
(def counter-on-enumerators-state (push-item counter :enumerator (make-push-state)))

;;
;; enumerator_from_vector_integer
;;

(facts "the instruction enumerator_from_vector_integer should remove a :vector_integer and add a new :enumerator item"
  (count (:vector_integer vi-state)) => 1
  (count (:enumerator vi-state)) => 0
  (count (:vector_integer (execute-instruction 'enumerator_from_vector_integer vi-state))) => 0
  (count (:enumerator (execute-instruction 'enumerator_from_vector_integer vi-state))) => 1
  )

(fact "the created item should be an Enumerator with the original vector_integer as its seq"
  (:collection (first (:enumerator (execute-instruction 'enumerator_from_vector_integer vi-state)))) => (just 1 2 3 4 5)
  ) 

(fact "the created enumerator should have pointer set to 0"
  (:pointer (first (:enumerator (execute-instruction 'enumerator_from_vector_integer vi-state)))) => 0
  )

;;
;; enumerator_unwrap
;;

(fact "enumerator_unwrap should push the :enumerator's collection onto the :exec stack"
  (first (:exec (execute-instruction 'enumerator_unwrap counter-on-enumerators-state))) =>  (:collection counter)
  (count (:enumerator (execute-instruction 'enumerator_unwrap counter-on-enumerators-state))) =>  0
  )

