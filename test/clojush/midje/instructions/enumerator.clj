; To run these tests with autotest use:
;
;    lein midje :autotest test
;
; This runs everything in the test sub-directory but
; _doesn't_ run all the stuff in src, which midje tries
; to run by default, which breaks the world.

(ns clojush.midje.instructions.enumerator
  (:use midje.sweet
        [clojush pushstate interpreter]
        clojush.instructions.enumerator))

;; make sure the interpreter knows what Enumerators are
;;
(def counter (make-enumerator [1 2 3 4 5] 0))
(def recognizer (push-item counter :enumerator (make-push-state)))

(fact "enumerator instances are recognized and pushed to the :enumerator stack"
  (count (:enumerator (run-push '() recognizer))) => 1
  )

(def vi-state (push-state-from-stacks :vector_integer '([1 2 3 4 5])))

;; enumerator_from_vector_integer
;;
(facts "the instruction enumerator_from_vector_integer should remove a :vector_integer and add a new :enumerator item"
  (count (:vector_integer vi-state)) => 1
  (count (:enumerator vi-state)) => 0
  (count (:vector_integer (run-push '(enumerator_from_vector_integer) vi-state))) => 0
  (count (:enumerator (run-push '(enumerator_from_vector_integer) vi-state))) => 1
  )

(fact "the created item should be an Enumerator with the original vector_integer as its seq"
  (:collection (first (:enumerator (run-push '(enumerator_from_vector_integer) vi-state)))) => (just 1 2 3 4 5)
  ) 

(fact "the created enumerator should have pointer set to 0"
  (:pointer (first (:enumerator (run-push '(enumerator_from_vector_integer) vi-state)))) => 0
  )

;; enumerator_first
;;
