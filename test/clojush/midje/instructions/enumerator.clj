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
;; some local helpers
;;

(defn is-valid-state? [state]
  (= (keys state) clojush.globals/push-types)
)


(defn safe-execute [instruction state]
  (let [result (execute-instruction instruction state)]
    (if (not (is-valid-state? result))
      (throw (Exception. "instruction returned invalid push-state"))
      result)))


;;
;; test contains-at-least? helper
;;

(def busy-state (push-state-from-stacks
  :integer '(9 9 -1) 
  :boolean '(false true) 
  :code '(7 [3 2 1] in1) 
  :exec '(integer_add))) 

(facts "contains-at-least? indicates that at least the specified number of items are present on the stacks of a state"
  (contains-at-least? (make-push-state) :integer 0) => truthy
  (contains-at-least? (make-push-state) :boolean 0) => truthy
  (contains-at-least? (make-push-state) :boolean 1912) => falsey

  (contains-at-least? busy-state :integer 3) => truthy
  (contains-at-least? busy-state :integer 4) => falsey
  (contains-at-least? busy-state :boolean 2) => truthy
  (contains-at-least? busy-state :boolean 3) => falsey
  (contains-at-least? busy-state :code 3) => truthy
  (contains-at-least? busy-state :code 4) => falsey
  (contains-at-least? busy-state :exec 1) => truthy
  (contains-at-least? busy-state :exec 2) => falsey
  (contains-at-least? busy-state :vector_foo 2) => falsey
)

;;
;; test apply-patch helper
;;

(facts "apply-patch adds new items to the top of the indicated stack"
  (:integer (apply-patch (make-push-state) :integer [1 10 2 11 3 12])) => (just 1 10 2 11 3 12)
  (:integer busy-state) => (just 9 9 -1)
  (:integer (safe-execute 'integer_pop busy-state)) => (just 9 -1) ;; just making top visible
  (:integer (safe-execute 66 busy-state)) => (just 66 9 9 -1) ;; just making top visible
  (:integer (apply-patch busy-state :integer [0 1 2])) => (just 0 1 2 9 9 -1)
  (:vector_integer (apply-patch (make-push-state) :vector_integer [[1 2] [3 4]])) => (just [1 2] [3 4])
  )

(facts "apply-patch can work with calculated values"
  (:integer (apply-patch busy-state :integer [(+ 2 3) (- 9 2)])) => (just 5 7 9 9 -1)
)
;;
;; test apply-patches helper
;;

(facts "apply-patches adds new items to the tops of all indicated stacks"
  (:integer (apply-patches (make-push-state))) => nil
  (:integer (apply-patches (make-push-state) :integer [1 2 3])) => (just 1 2 3)
  (:integer (apply-patches (make-push-state) :integer [1 2 3] :boolean [false true])) => (just 1 2 3)
  (:boolean (apply-patches (make-push-state) :integer [1 2 3] :boolean [false true])) => (just false true)

  (:integer (apply-patches busy-state)) => (just 9 9 -1)
  (:integer (apply-patches busy-state :integer [4 3 2])) => (just 4 3 2 9 9 -1)
  (:float (apply-patches busy-state :float [4.3 2.1])) => (just 4.3 2.1)
  )



;;
;; enumerator_from_vector_integer
;;

(facts "the instruction enumerator_from_vector_integer should remove a :vector_integer and add a new :enumerator item"
  (count (:vector_integer vi-state)) => 1
  (count (:enumerator vi-state)) => 0
  (count (:vector_integer (safe-execute 'enumerator_from_vector_integer vi-state))) => 0
  (count (:enumerator (safe-execute 
    'enumerator_from_vector_integer vi-state))) => 1
  )

(fact "the created item should be an Enumerator with the original vector_integer as its seq"
  (:collection (top-item :enumerator (safe-execute 'enumerator_from_vector_integer vi-state))) => (just 1 2 3 4 5)
  ) 

(fact "the created enumerator should have pointer set to 0"
  (:pointer (top-item :enumerator (safe-execute 'enumerator_from_vector_integer vi-state))) => 0
  )

(fact "no enumerator is created from a consumed empty vector" 
  (top-item :enumerator (safe-execute 'enumerator_from_vector_integer empty-on-vector-state)) => :no-stack-item 
  (top-item :vector_integer (safe-execute 'enumerator_from_vector_integer empty-on-vector-state)) => :no-stack-item 
  )

(fact "an actual push-state is returned"
  (is-valid-state? (safe-execute 'enumerator_from_vector_integer (make-push-state))) => truthy
)


;;
;; enumerator_unwrap
;;

(fact "enumerator_unwrap should push the :enumerator's collection onto the :exec stack"
  (top-item :exec (safe-execute 'enumerator_unwrap counter-on-enumerators-state)) =>  (:collection counter)
  (count (:enumerator (safe-execute 'enumerator_unwrap counter-on-enumerators-state))) =>  0
  )

(fact "the enumerator is destroyed if it is empty"
  (top-item :exec (safe-execute 'enumerator_unwrap empty-on-enumerators-state)) => :no-stack-item 
  (top-item :enumerator (safe-execute 'enumerator_unwrap empty-on-enumerators-state)) => :no-stack-item 
  )

(fact "an actual push-state is returned"
  (keys (safe-execute 'enumerator_unwrap (make-push-state))) => clojush.globals/push-types
)

;;
;; enumerator_rewind
;;

(fact "enumerator_rewind should reset the argument's pointer->0"
  (enum/enumerator? (top-item :enumerator (safe-execute 'enumerator_rewind counter-on-enumerators-state))) =>  truthy 
  (:pointer (top-item :enumerator (safe-execute 'enumerator_rewind counter-on-enumerators-state))) =>  0
  (count (:enumerator (safe-execute 'enumerator_rewind counter-on-enumerators-state))) =>  1
  )

(def advanced-counter-on-enumerators-state (push-item (enum/construct-enumerator [2 3 5 8 13] 3) :enumerator (make-push-state)))

(fact "enumerator_rewind should actively change the pointer"
  (:pointer (top-item :enumerator (safe-execute 'enumerator_rewind advanced-counter-on-enumerators-state))) =>  0
  )

(fact "the enumerator is destroyed if it is empty"
  (top-item :enumerator (safe-execute 'enumerator_rewind empty-on-enumerators-state)) => :no-stack-item 
  )

(fact "an actual push-state is returned"
  (keys (safe-execute 'enumerator_rewind (make-push-state))) => clojush.globals/push-types
)


;;
;; enumerator_ff
;;

(fact "enumerator_ff should move the top :enumerator item's pointer to its max value (length - 1)"
  (enum/enumerator? (top-item :enumerator (safe-execute 'enumerator_ff counter-on-enumerators-state))) =>  truthy 
  (:pointer (top-item :enumerator (safe-execute 'enumerator_ff counter-on-enumerators-state))) =>  4 
  )

(fact "the enumerator is destroyed if it is empty"
  (keys (safe-execute 'enumerator_unwrap empty-on-enumerators-state)) => clojush.globals/push-types
  (top-item :enumerator (safe-execute 'enumerator_ff empty-on-enumerators-state)) => :no-stack-item 
  )

(fact "an actual push-state is returned"
  (keys (safe-execute 'enumerator_ff (make-push-state))) => clojush.globals/push-types
)


;;
;; enumerator_first
;;

(facts "enumerator_first should set the pointer to 0, AND push the first item to the :exec stack"
  (enum/enumerator? (top-item :enumerator (safe-execute 'enumerator_first counter-on-enumerators-state))) =>  truthy 
  (count (:exec (safe-execute 'enumerator_first counter-on-enumerators-state))) =>  1
  (top-item :exec (safe-execute 'enumerator_first counter-on-enumerators-state)) =>  2
  (:pointer (top-item :enumerator (safe-execute 'enumerator_first counter-on-enumerators-state))) =>  0
  )

(fact "the enumerator is destroyed if it is empty" 
  (top-item :enumerator (safe-execute 'enumerator_first empty-on-enumerators-state)) => :no-stack-item
  (top-item :exec (safe-execute 'enumerator_first empty-on-enumerators-state)) => :no-stack-item 
  )

(fact "an actual push-state is returned"
  (keys (safe-execute 'enumerator_ff (make-push-state))) => clojush.globals/push-types
)

;;
;; enumerator_last
;;

(facts "enumerator_last should set the pointer to its max, AND push the last item to the :exec stack"
  (enum/enumerator? (top-item :enumerator (safe-execute 'enumerator_last counter-on-enumerators-state))) =>  truthy 
  (count (:exec (safe-execute 'enumerator_last counter-on-enumerators-state))) =>  1
  (top-item :exec (safe-execute 'enumerator_last counter-on-enumerators-state)) =>  13
  (:pointer (top-item :enumerator (safe-execute 'enumerator_last counter-on-enumerators-state))) =>  4
  )

(fact "the enumerator is destroyed if it is empty" 
  (top-item :enumerator (safe-execute 'enumerator_last empty-on-enumerators-state)) => :no-stack-item
  (top-item :exec (safe-execute 'enumerator_last empty-on-enumerators-state)) => :no-stack-item 
  )

;;
;; enumerator_forward
;;

(facts "enumerator_forward should advance the pointer by one"
  (enum/enumerator? (top-item :enumerator (safe-execute 'enumerator_forward counter-on-enumerators-state))) =>  truthy 
  (:pointer (top-item :enumerator (safe-execute 'enumerator_forward counter-on-enumerators-state))) =>  1
  )

(fact "the enumerator is destroyed if it is empty" 
  (top-item :enumerator (safe-execute 'enumerator_forward empty-on-enumerators-state)) => :no-stack-item
  (top-item :exec (safe-execute 'enumerator_forward empty-on-enumerators-state)) => :no-stack-item 
  )

(def maxed-counter-on-enumerators-state (push-item (enum/construct-enumerator [1 2 3 4 5] 4) :enumerator (make-push-state)))

(fact "the enumerator is destroyed if the pointer advances past the max" 
  (top-item :enumerator (safe-execute 'enumerator_forward maxed-counter-on-enumerators-state)) => :no-stack-item
  (top-item :exec (safe-execute 'enumerator_forward maxed-counter-on-enumerators-state)) => :no-stack-item 
  )


;;
;; enumerator_backward
;;

(facts "enumerator_backward should advance the pointer by one"
  (enum/enumerator? (top-item :enumerator (safe-execute 
    'enumerator_backward advanced-counter-on-enumerators-state))) => truthy 
  (:pointer (top-item :enumerator (safe-execute
    'enumerator_backward advanced-counter-on-enumerators-state))) => 2
  )

(fact "the enumerator is destroyed if it is empty" 
  (top-item :enumerator (safe-execute 'enumerator_backward empty-on-enumerators-state)) => :no-stack-item
  (top-item :exec (safe-execute 'enumerator_backward empty-on-enumerators-state)) => :no-stack-item 
  )

(fact "the enumerator is destroyed if the pointer advances past the max" 
  (top-item :enumerator (safe-execute 'enumerator_backward counter-on-enumerators-state)) => :no-stack-item
  (top-item :exec (safe-execute 'enumerator_backward counter-on-enumerators-state)) => :no-stack-item 
  )


;;
;; enumerator_next
;;

(facts "enumerator_next should advance the pointer by one, and push the CURRENT item"
  (enum/enumerator? (top-item :enumerator (safe-execute 'enumerator_next counter-on-enumerators-state))) =>  truthy 
  (:pointer (top-item :enumerator (safe-execute 'enumerator_next counter-on-enumerators-state))) =>  1
  (top-item :exec (safe-execute 'enumerator_next counter-on-enumerators-state)) =>  2
  )

(fact "the enumerator is destroyed if it is empty" 
  (top-item :enumerator (safe-execute 'enumerator_next empty-on-enumerators-state)) => :no-stack-item
  (top-item :exec (safe-execute 'enumerator_next empty-on-enumerators-state)) => :no-stack-item 
  )

(fact "the enumerator is destroyed if the pointer advances past the max, BUT pushes the last item" 
  (top-item :enumerator (safe-execute 'enumerator_next maxed-counter-on-enumerators-state)) => :no-stack-item
  (top-item :exec (safe-execute 'enumerator_next maxed-counter-on-enumerators-state)) => 5
  )

;;
;; enumerator_prev
;;

(facts "enumerator_prev should reduce the pointer by one, and push the CURRENT item"
  (enum/enumerator? (top-item :enumerator (safe-execute 
    'enumerator_prev advanced-counter-on-enumerators-state))) =>  truthy 
  (:pointer (top-item :enumerator (safe-execute 'enumerator_prev advanced-counter-on-enumerators-state))) =>  2
  (top-item :exec (safe-execute 'enumerator_prev advanced-counter-on-enumerators-state)) =>  8
  )

(fact "the enumerator is destroyed if it is empty" 
  (top-item :enumerator (safe-execute 'enumerator_prev empty-on-enumerators-state)) => :no-stack-item
  (top-item :exec (safe-execute 'enumerator_prev empty-on-enumerators-state)) => :no-stack-item 
  )

(fact "the enumerator is destroyed if the pointer advances below 0, BUT pushes the first item" 
  (top-item :enumerator (safe-execute 'enumerator_prev counter-on-enumerators-state)) => :no-stack-item
  (top-item :exec (safe-execute 'enumerator_prev counter-on-enumerators-state)) => 2
  )


;;
;; enumerator_set
;;

(def resetting-counter-state (apply-patch advanced-counter-on-enumerators-state :integer [2 -12 9812]))

(facts "enumerator_set should take an :integer, and change the pointer to that value, with bounds checking"
  (:integer resetting-counter-state) => (just 2 -12 9812)
  (:pointer (top-item :enumerator resetting-counter-state)) => 3
  (:pointer (top-item :enumerator (safe-execute 'enumerator_set resetting-counter-state))) => 2
  (count (:integer (run-push '(integer_pop enumerator_set) resetting-counter-state))) => 1
  (count (:enumerator (run-push '(integer_pop enumerator_set) resetting-counter-state))) => 0

  (count (:integer (run-push '(integer_pop integer_pop enumerator_set) resetting-counter-state))) => 0
  (count (:enumerator (run-push '(integer_pop integer_pop  enumerator_set) resetting-counter-state))) => 0
  )

;;
;; enumerator_map_code
;;

(def code-mapping-state (apply-patches advanced-counter-on-enumerators-state 
  :integer [0 0 0 0 0 0]
  :code ['(1 integer_stackdepth) 9 false]))

(fact "enumerator_map_code should advance the enumerator counter, with bounds checking"
  (:pointer (top-item :enumerator (safe-execute 'enumerator_map_code code-mapping-state))) => 4
  )

(fact "enumerator_map_code should put the expected three values onto the :exec stack"
  (:exec (safe-execute 'enumerator_map_code code-mapping-state)) => (just 8 '(1 integer_stackdepth) 'enumerator_map_code)
  )

(facts "enumerator_map_code should still fire when the counter is maxed out"
  (:exec (safe-execute 'enumerator_map_code (safe-execute 'enumerator_map_code code-mapping-state))) => 
    (just 13 '(1 integer_stackdepth) 'enumerator_map_code 8 '(1 integer_stackdepth) 'enumerator_map_code)

  (:integer (run-push '(enumerator_map_code) code-mapping-state)) => (just 11 1 13 8 1 8 0 0 0 0 0 0)
    ;; int:(0 0 0 0 0 0)   exec:(emc) ptr:(3)
    ;; int:(0 0 0 0 0 0)   exec:(8 '(1 integer_stackdepth) 'enumerator_map_code) ptr:(4)
    ;; int:(8 0 0 0 0 0 0) exec:('(1 integer_stackdepth) 'enumerator_map_code) ptr:(4)
    ;; int:(8 0 0 0 0 0 0) exec:(1 'integer_stackdepth 'enumerator_map_code) ptr:(4)
    ;; int:(1 8 0 0 0 0 0 0) exec:('integer_stackdepth 'enumerator_map_code) ptr:(4)
    ;; int:(8 1 8 0 0 0 0 0 0) exec:('enumerator_map_code) ptr:(4)
    ;; int:(8 1 8 0 0 0 0 0 0) exec:(13 '(1 integer_stackdepth) 'enumerator_map_code) ptr:(5)
    ;; int:(13 8 1 8 0 0 0 0 0 0) exec:('(1 integer_stackdepth) 'enumerator_map_code) ptr:(5)
    ;; int:(13 8 1 8 0 0 0 0 0 0) exec:(1 'integer_stackdepth 'enumerator_map_code) ptr:(5)
    ;; int:(1 13 8 1 8 0 0 0 0 0 0) exec:('integer_stackdepth 'enumerator_map_code) ptr:(5)
    ;; int:(11 1 13 8 1 8 0 0 0 0 0 0) exec:('enumerator_map_code) ptr:(5)
    ;; int:(11 1 13 8 1 8 0 0 0 0 0 0) exec:() ptr:()
  )

  (fact "enumerator_map_code should eliminate the top code after completing"
      (:code (run-push '(enumerator_map_code) code-mapping-state)) => (just 9 false)
  )

  (fact "enumerator_map_code should pop the top :code item if the enumerator is empty"
      (:code (safe-execute 'enumerator_map_code
        (push-item 88 :code empty-on-enumerators-state))) => '()
  )
