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
        clojush.instructions.enumerator
        clojush.instructions.vectors))


;; some fixtures to use below
;;
(def counter (enum/construct-enumerator [1 2 3 4 5] 0))
(def empty-enum (enum/construct-enumerator [] 0))
(def counter-on-exec-state (push-item counter :exec (make-push-state)))
(def counter-on-enumerators-state (push-item counter :enumerator (make-push-state)))
(def vi-state (push-state-from-stacks :vector_integer '([1 2 3 4 5])))



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

;;
;; enumerator_rewind
;;

(fact "enumerator_rewind should move the :enumerator with pointer->0 onto the :exec stack"
  (enum/enumerator? (top-item :exec (execute-instruction 'enumerator_rewind counter-on-enumerators-state))) =>  truthy 
  (:pointer (top-item :exec (execute-instruction 'enumerator_rewind counter-on-enumerators-state))) =>  0
  (count (:enumerator (execute-instruction 'enumerator_rewind counter-on-enumerators-state))) =>  0 
  )

(def advanced-counter-on-enumerators-state (push-item (enum/construct-enumerator [1 2 3 4 5] 3) :enumerator (make-push-state)))

(fact "enumerator_rewind should actively change the pointer"
  (:pointer (top-item :exec (execute-instruction 'enumerator_rewind advanced-counter-on-enumerators-state))) =>  0
  )

;;
;; enumerator_ff
;;

(fact "enumerator_ff should move the :enumerator with pointer->max onto the :exec stack"
  (enum/enumerator? (top-item :exec (execute-instruction 'enumerator_ff counter-on-enumerators-state))) =>  truthy 
  (:pointer (top-item :exec (execute-instruction 'enumerator_ff counter-on-enumerators-state))) =>  4 
  (count (:enumerator (execute-instruction 'enumerator_ff counter-on-enumerators-state))) =>  0 
  )

;;
;; enumerator_first
;;

(facts "enumerator_first should move the :enumerator with pointer->0 onto the :exec stack, above its first item"
  (enum/enumerator? (top-item :exec (execute-instruction 'enumerator_first counter-on-enumerators-state))) =>  truthy 
  (count (:exec (execute-instruction 'enumerator_first counter-on-enumerators-state))) =>  2
  (stack-ref :exec 1 (execute-instruction 'enumerator_first counter-on-enumerators-state)) =>  1
  (:pointer (top-item :exec (execute-instruction 'enumerator_first counter-on-enumerators-state))) =>  0
  (count (:enumerator (execute-instruction 'enumerator_first counter-on-enumerators-state))) =>  0 
  )
