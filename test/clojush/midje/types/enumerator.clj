; To run these tests with autotest use:
;
;    lein midje :autotest test
;
; This runs everything in the test sub-directory but
; _doesn't_ run all the stuff in src, which midje tries
; to run by default, which breaks the world.

(ns clojush.midje.types.enumerator
  (:require [clojush.types.enumerator :as enum])
  (:use midje.sweet
        [clojush pushstate interpreter]
        clojush.instructions.enumerator))


;; checking initialization
(facts "enumerators can be created by assigning both components explicitly"
  (:collection (enum/construct-enumerator [1 2] 77)) => [1 2]
  (:pointer (enum/construct-enumerator [1 2] 77)) => 77

  (:collection (enum/construct-enumerator '(:a :list) 0)) => (just :a :list)
  (:pointer (enum/construct-enumerator '(:a :list) 0)) => 0

  (:collection (enum/construct-enumerator '(:a (:nested) :list) 0)) => (just :a '(:nested) :list)

  (:collection (enum/construct-enumerator "a string" 3)) => "a string"
  (:pointer (enum/construct-enumerator "a string" 3)) => 3
  )

(fact "new-enumerator assumes the pointer is 0"
  (:collection (enum/new-enumerator [1 2])) => [1 2]
  (:pointer (enum/new-enumerator [1 2])) => 0
  )

(fact "an enumerator can be constructed from an empty seq"
  (:collection (enum/new-enumerator '[])) => []
  (:pointer (enum/new-enumerator '[])) => 0
  )

;; some fixtures to use below
;;
(def counter (enum/construct-enumerator [1 2 3 4 5] 0))
(def counter-on-exec-state (push-item counter :exec (make-push-state)))

;; make sure the interpreter knows what Enumerators are
;; 
(fact "enumerator instances are recognized and pushed to the :enumerator stack from the :exec stack"
  (count (:enumerator (run-push '() counter-on-exec-state))) => 1
  (top-item :enumerator (run-push '() counter-on-exec-state)) => counter
  (count (:exec (run-push '() counter-on-exec-state))) => 0
  )
