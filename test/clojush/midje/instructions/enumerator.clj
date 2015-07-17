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
(def counter (enum/construct-enumerator [2 3 5 8 13] 0))
(def counter-on-exec-state (push-item counter :exec (make-push-state)))
(def empty-enum (enum/construct-enumerator '[] 0))
(def empty-on-vector-state (push-item '[] :vector_integer (make-push-state)))
(def counter-on-enumerators-state (push-item counter :enumerator (make-push-state)))
(def vi-state (push-state-from-stacks :vector_integer '([1 2 3 4 5])))
(def empty-on-enumerators-state (push-item (enum/new-enumerator []) :enumerator (make-push-state)))


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
  (:collection (top-item :enumerator (execute-instruction 'enumerator_from_vector_integer vi-state))) => (just 1 2 3 4 5)
  ) 

(fact "the created enumerator should have pointer set to 0"
  (:pointer (top-item :enumerator (execute-instruction 'enumerator_from_vector_integer vi-state))) => 0
  )

(fact "no enumerator is created from an empty vector" 
  (top-item :enumerator (execute-instruction 'enumerator_from_vector_integer empty-on-vector-state)) => :no-stack-item 
  )

;;
;; enumerator_unwrap
;;

(fact "enumerator_unwrap should push the :enumerator's collection onto the :exec stack"
  (top-item :exec (execute-instruction 'enumerator_unwrap counter-on-enumerators-state)) =>  (:collection counter)
  (count (:enumerator (execute-instruction 'enumerator_unwrap counter-on-enumerators-state))) =>  0
  )

(fact "the enumerator is destroyed if it is empty" 
  (top-item :exec (execute-instruction 'enumerator_unwrap empty-on-enumerators-state)) => :no-stack-item 
  (top-item :enumerator (execute-instruction 'enumerator_unwrap empty-on-enumerators-state)) => :no-stack-item 
  )

;;
;; enumerator_rewind
;;

(fact "enumerator_rewind should reset the argument's pointer->0"
  (enum/enumerator? (top-item :enumerator (execute-instruction 'enumerator_rewind counter-on-enumerators-state))) =>  truthy 
  (:pointer (top-item :enumerator (execute-instruction 'enumerator_rewind counter-on-enumerators-state))) =>  0
  (count (:enumerator (execute-instruction 'enumerator_rewind counter-on-enumerators-state))) =>  1
  )

(def advanced-counter-on-enumerators-state (push-item (enum/construct-enumerator [1 2 3 4 5] 3) :enumerator (make-push-state)))

(fact "enumerator_rewind should actively change the pointer"
  (:pointer (top-item :enumerator (execute-instruction 'enumerator_rewind advanced-counter-on-enumerators-state))) =>  0
  )

(fact "the enumerator is destroyed if it is empty" 
  (top-item :enumerator (execute-instruction 'enumerator_rewind empty-on-enumerators-state)) => :no-stack-item 
  )


;;
;; enumerator_ff
;;

(fact "enumerator_ff should move the top :enumerator item's pointer to its max value (length - 1)"
  (enum/enumerator? (top-item :enumerator (execute-instruction 'enumerator_ff counter-on-enumerators-state))) =>  truthy 
  (:pointer (top-item :enumerator (execute-instruction 'enumerator_ff counter-on-enumerators-state))) =>  4 
  )

(fact "the enumerator is destroyed if it is empty" 
  (top-item :enumerator (execute-instruction 'enumerator_ff empty-on-enumerators-state)) => :no-stack-item 
  )


;;
;; enumerator_first
;;

(facts "enumerator_first should set the pointer to 0, AND push the first item to the :exec stack"
  (enum/enumerator? (top-item :enumerator (execute-instruction 'enumerator_first counter-on-enumerators-state))) =>  truthy 
  (count (:exec (execute-instruction 'enumerator_first counter-on-enumerators-state))) =>  1
  (top-item :exec (execute-instruction 'enumerator_first counter-on-enumerators-state)) =>  2
  (:pointer (top-item :enumerator (execute-instruction 'enumerator_first counter-on-enumerators-state))) =>  0
  )

(fact "the enumerator is destroyed if it is empty" 
  (top-item :enumerator (execute-instruction 'enumerator_first empty-on-enumerators-state)) => :no-stack-item
  (top-item :exec (execute-instruction 'enumerator_first empty-on-enumerators-state)) => :no-stack-item 
  )


;;
;; enumerator_last
;;

(facts "enumerator_last should set the pointer to its max, AND push the last item to the :exec stack"
  (enum/enumerator? (top-item :enumerator (execute-instruction 'enumerator_last counter-on-enumerators-state))) =>  truthy 
  (count (:exec (execute-instruction 'enumerator_last counter-on-enumerators-state))) =>  1
  (top-item :exec (execute-instruction 'enumerator_last counter-on-enumerators-state)) =>  13
  (:pointer (top-item :enumerator (execute-instruction 'enumerator_last counter-on-enumerators-state))) =>  4
  )

(fact "the enumerator is destroyed if it is empty" 
  (top-item :enumerator (execute-instruction 'enumerator_last empty-on-enumerators-state)) => :no-stack-item
  (top-item :exec (execute-instruction 'enumerator_last empty-on-enumerators-state)) => :no-stack-item 
  )

;;
;; enumerator_forward
;;

(facts "enumerator_forward should advance the pointer by one"
  (enum/enumerator? (top-item :enumerator (execute-instruction 'enumerator_forward counter-on-enumerators-state))) =>  truthy 
  (:pointer (top-item :enumerator (execute-instruction 'enumerator_forward counter-on-enumerators-state))) =>  1
  )

(fact "the enumerator is destroyed if it is empty" 
  (top-item :enumerator (execute-instruction 'enumerator_forward empty-on-enumerators-state)) => :no-stack-item
  (top-item :exec (execute-instruction 'enumerator_forward empty-on-enumerators-state)) => :no-stack-item 
  )

(def maxed-counter-on-enumerators-state (push-item (enum/construct-enumerator [1 2 3 4 5] 4) :enumerator (make-push-state)))

(fact "the enumerator is destroyed if the pointer advances past the max" 
  (top-item :enumerator (execute-instruction 'enumerator_forward maxed-counter-on-enumerators-state)) => :no-stack-item
  (top-item :exec (execute-instruction 'enumerator_forward maxed-counter-on-enumerators-state)) => :no-stack-item 
  )


;;
;; enumerator_backward
;;

(facts "enumerator_backward should advance the pointer by one"
  (enum/enumerator? (top-item :enumerator (execute-instruction 
    'enumerator_backward advanced-counter-on-enumerators-state))) => truthy 
  (:pointer (top-item :enumerator (execute-instruction
    'enumerator_backward advanced-counter-on-enumerators-state))) => 2
  )

(fact "the enumerator is destroyed if it is empty" 
  (top-item :enumerator (execute-instruction 'enumerator_backward empty-on-enumerators-state)) => :no-stack-item
  (top-item :exec (execute-instruction 'enumerator_backward empty-on-enumerators-state)) => :no-stack-item 
  )

(fact "the enumerator is destroyed if the pointer advances past the max" 
  (top-item :enumerator (execute-instruction 'enumerator_backward counter-on-enumerators-state)) => :no-stack-item
  (top-item :exec (execute-instruction 'enumerator_backward counter-on-enumerators-state)) => :no-stack-item 
  )


;;
;; enumerator_next
;;

(facts "enumerator_next should advance the pointer by one, and push the CURRENT item"
  (enum/enumerator? (top-item :enumerator (execute-instruction 'enumerator_next counter-on-enumerators-state))) =>  truthy 
  (:pointer (top-item :enumerator (execute-instruction 'enumerator_next counter-on-enumerators-state))) =>  1
  (top-item :exec (execute-instruction 'enumerator_next counter-on-enumerators-state)) =>  2
  )

(fact "the enumerator is destroyed if it is empty" 
  (top-item :enumerator (execute-instruction 'enumerator_next empty-on-enumerators-state)) => :no-stack-item
  (top-item :exec (execute-instruction 'enumerator_next empty-on-enumerators-state)) => :no-stack-item 
  )

(fact "the enumerator is destroyed if the pointer advances past the max, BUT pushes the last item" 
  (top-item :enumerator (execute-instruction 'enumerator_next maxed-counter-on-enumerators-state)) => :no-stack-item
  (top-item :exec (execute-instruction 'enumerator_next maxed-counter-on-enumerators-state)) => 5
  )

;;
;; enumerator_prev
;;

(facts "enumerator_prev should reduce the pointer by one, and push the CURRENT item"
  (enum/enumerator? (top-item :enumerator (execute-instruction 
    'enumerator_prev advanced-counter-on-enumerators-state))) =>  truthy 
  (:pointer (top-item :enumerator (execute-instruction 
    'enumerator_prev advanced-counter-on-enumerators-state))) =>  2
  (top-item :exec (execute-instruction 
    'enumerator_prev advanced-counter-on-enumerators-state)) =>  4
  )

(fact "the enumerator is destroyed if it is empty" 
  (top-item :enumerator (execute-instruction 'enumerator_prev empty-on-enumerators-state)) => :no-stack-item
  (top-item :exec (execute-instruction 'enumerator_prev empty-on-enumerators-state)) => :no-stack-item 
  )

(fact "the enumerator is destroyed if the pointer advances below 0, BUT pushes the first item" 
  (top-item :enumerator (execute-instruction 'enumerator_prev counter-on-enumerators-state)) => :no-stack-item
  (top-item :exec (execute-instruction 'enumerator_prev counter-on-enumerators-state)) => 2
  )
