; To run these tests with autotest use:
;
;    lein midje :autotest test
;
; This runs everything in the test sub-directory but
; _doesn't_ run all the stuff in src, which midje tries
; to run by default, which breaks the world.

(ns clojush.midje.instructions.enumerator-creators
  (:require [clojush.types.enumerator :as enum])
  (:use midje.sweet
        [clojush pushstate interpreter]
        clojush.instructions.enumerator))


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
;; enumerator_from_vector_boolean
;;

(def vb-state (push-state-from-stacks :vector_boolean '([false true false])))

(facts "enumerator_from_vector_boolean should remove a :vector_boolean and add a new :enumerator item"
  (count (:vector_boolean vb-state)) => 1
  (count (:enumerator vb-state)) => 0
  (count (:vector_boolean (safe-execute 'enumerator_from_vector_boolean vb-state))) => 0
  (count (:enumerator (safe-execute 'enumerator_from_vector_boolean vb-state))) => 1
  )

(fact "the created item should be an Enumerator with the original vector_boolean as its seq"
  (:collection (top-item :enumerator (safe-execute 'enumerator_from_vector_boolean vb-state))) => (just false true false)
  ) 

(fact "the created enumerator should have pointer set to 0"
  (:pointer (top-item :enumerator (safe-execute 'enumerator_from_vector_boolean vb-state))) => 0
  )

(def empty-on-vb-state (push-state-from-stacks :vector_boolean '([])))

(fact "no enumerator is created from a consumed empty vector" 
  (top-item :enumerator (safe-execute 'enumerator_from_vector_boolean empty-on-vb-state)) => :no-stack-item 
  (top-item :vector_boolean (safe-execute 'enumerator_from_vector_boolean empty-on-vb-state)) => :no-stack-item 
  )

(fact "an actual push-state is returned"
  (is-valid-state? (safe-execute 'enumerator_from_vector_boolean (make-push-state))) => truthy
)


;;
;; enumerator_from_vector_float
;;

(def vf-state (push-state-from-stacks :vector_float '([1.2 -3.4 5.6 -7.8])))

(facts "enumerator_from_vector_float should remove a :vector_float and add a new :enumerator item"
  (count (:vector_float vf-state)) => 1
  (count (:enumerator vf-state)) => 0
  (count (:vector_float (safe-execute 'enumerator_from_vector_float vf-state))) => 0
  (count (:enumerator (safe-execute 'enumerator_from_vector_float vf-state))) => 1
  )

(fact "the created item should be an Enumerator with the original vector_float as its seq"
  (:collection (top-item :enumerator (safe-execute 'enumerator_from_vector_float vf-state))) => (just 1.2 -3.4 5.6 -7.8)
  ) 

(fact "the created enumerator should have pointer set to 0"
  (:pointer (top-item :enumerator (safe-execute 'enumerator_from_vector_float vf-state))) => 0
  )

(def empty-on-vf-state (push-state-from-stacks :vector_float '([])))

(fact "no enumerator is created from a consumed empty vector" 
  (top-item :enumerator (safe-execute 'enumerator_from_vector_float empty-on-vf-state)) => :no-stack-item 
  (top-item :vector_float (safe-execute 'enumerator_from_vector_float empty-on-vf-state)) => :no-stack-item 
  )

(fact "an actual push-state is returned"
  (is-valid-state? (safe-execute 'enumerator_from_vector_float (make-push-state))) => truthy
)


;;
;; enumerator_from_vector_string
;;

(def vs-state (push-state-from-stacks :vector_string '(["foo" "bar" "baz"])))

(facts "enumerator_from_vector_string should remove a :vector_string and add a new :enumerator item"
  (count (:vector_string vs-state)) => 1
  (count (:enumerator vs-state)) => 0
  (count (:vector_string (safe-execute 'enumerator_from_vector_string vs-state))) => 0
  (count (:enumerator (safe-execute 'enumerator_from_vector_string vs-state))) => 1
  )

(fact "the created item should be an Enumerator with the original vector_string as its seq"
  (:collection (top-item :enumerator (safe-execute 'enumerator_from_vector_string vs-state))) => (just "foo" "bar" "baz")
  ) 

(fact "the created enumerator should have pointer set to 0"
  (:pointer (top-item :enumerator (safe-execute 'enumerator_from_vector_string vs-state))) => 0
  )

(def empty-on-vs-state (push-state-from-stacks :vector_string '([])))

(fact "no enumerator is created from a consumed empty vector" 
  (top-item :enumerator (safe-execute 'enumerator_from_vector_string empty-on-vs-state)) => :no-stack-item 
  (top-item :vector_string (safe-execute 'enumerator_from_vector_string empty-on-vs-state)) => :no-stack-item 
  )

(fact "an actual push-state is returned"
  (is-valid-state? (safe-execute 'enumerator_from_vector_float (make-push-state))) => truthy
)


;;
;; enumerator_from_vector_integer
;;

(def vi-state (push-state-from-stacks :vector_integer '([1 2 3 4 5])))

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

(def empty-on-vi-state (push-item '[] :vector_integer (make-push-state)))

(fact "no enumerator is created from a consumed empty vector" 
  (top-item :enumerator (safe-execute 'enumerator_from_vector_integer empty-on-vi-state)) => :no-stack-item 
  (top-item :vector_integer (safe-execute 'enumerator_from_vector_integer empty-on-vi-state)) => :no-stack-item 
  )

(fact "an actual push-state is returned"
  (is-valid-state? (safe-execute 'enumerator_from_vector_integer (make-push-state))) => truthy
)


;;
;; enumerator_from_string
;;

(def string-state (push-state-from-stacks :string '("foo\nbar")))

(facts "the instruction enumerator_from_string should remove a :string and add a new :enumerator item"
  (count (:string string-state)) => 1
  (count (:enumerator string-state)) => 0
  (count (:string (safe-execute 'enumerator_from_string string-state))) => 0
  (count (:enumerator (safe-execute 'enumerator_from_string string-state))) => 1
  )

(fact "the created item should be an Enumerator with the original string as its seq"
  (:collection (top-item :enumerator (safe-execute 'enumerator_from_string string-state))) => "foo\nbar"
  ) 

(fact "the created enumerator should have pointer set to 0"
  (:pointer (top-item :enumerator (safe-execute 'enumerator_from_string string-state))) => 0
  )

(def empty-on-string-state (push-item "" :string (make-push-state)))

(fact "no enumerator is created from a consumed empty vector" 
  (top-item :enumerator (safe-execute 'enumerator_from_string empty-on-string-state)) => :no-stack-item 
  (top-item :string (safe-execute 'enumerator_from_string empty-on-string-state)) => :no-stack-item 
  )

(fact "an actual push-state is returned"
  (is-valid-state? (safe-execute 'enumerator_from_string (make-push-state))) => truthy
)

