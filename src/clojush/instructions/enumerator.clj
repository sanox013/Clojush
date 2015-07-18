;; enumerator.clj
;; define Enumerator type and accompanying instructions
;; Bill Tozier, bill@vagueinnovation.com, 2015

(ns clojush.instructions.enumerator
  (:require [clojush.types.enumerator :as enum])
  (:use [clojush.pushstate]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Instructions for Enumerators
;;
;; Rules of thumb:
;; 
;; There are (and should be) explicit instructions for converting each of the Push collection
;; types into Enumerators, but there is only one Enumerator type; the type of the enclosed
;; seq is maintained for its lifetime.
;; 
;; There are _not_ (and shouldn't be) any instructions which modify the _contents_ of the Push
;; collection inside an Enumerator; treat it as private and immutable for the lifetime of the
;; instance. The only state changes that should ever happen are changes in the pointer value
;; and the loop? state.
;;
;; No Enumerator instance should be made from an empty collection. Any instruction that encounters
;; such an 'empty' instance should destroy it rather than returning results. This includes `unwrap`.


(defn contains-at-least?
  "Returns true only when the number of items in all of the
  indicated prerequisite stacks meets or exceeds the number in state"
  [state & {:as prerequisites}]
  (reduce-kv 
    (fn [satisfied type requirement] 
      (and 
        satisfied 
        (>= (count (type state)) requirement)))
    true
    prerequisites)
  )

(defn apply-patch
  "Returns a new state with the indicated 'patch' pushed onto the top of the indicated stack"
  [state stack new-top]
  (assoc state stack (concat new-top (stack state)))
  )

(defn apply-patches
  "Returns a new state with all of the indicated 'patches' pushed onto the tops of the indicated stacks"
  [state & {:as patches}]
  (reduce-kv
    (fn [s stack new-top]
        (assoc s stack (concat new-top (stack s))))
    state
    patches
  ))

;; enumerator_from_vector_integer
;; consumes a vector_integer to make an enumerator with pointer 0
;; discards an empty vector
;;
(define-registered
  enumerator_from_vector_integer
  ^{:stack-types [:vector_integer :enumerator]}
  (fn [state]
    (if (contains-at-least? state :vector_integer 1)
      (let [collection (first (:vector_integer state))
            popped-state (pop-item :vector_integer state)]
        (if (not (empty? collection))
          (push-item (enum/new-enumerator collection) :enumerator popped-state)
          popped-state))
      state)))



;; enumerator_unwrap
;; consumes an enumerator to push its :collection onto the :exec stack
;; discards an empty enumerator (edge case)
;;
(define-registered
  enumerator_unwrap
  ^{:stack-types [:enumerator :exec]}
  (fn [state]
    (if (contains-at-least? state :enumerator 1)
      (let [old-seq (:collection (top-item :enumerator state))
            popped-state (pop-item :enumerator state)]
        (if (not (empty? old-seq))
          (push-item old-seq :exec popped-state)
          popped-state))
    state)))


;; enumerator_rewind
;; changes the pointer of the top enumerator to 0
;; discards an empty enumerator (edge case)
;;
(define-registered
  enumerator_rewind
  ^{:stack-types [:enumerator]}
  (fn [state]
    (if (contains-at-least? state :enumerator 1)
      (let [old-seq (:collection (top-item :enumerator state))
            popped-state (pop-item :enumerator state)]
        (if (not (empty? old-seq))
          (push-item (enum/new-enumerator old-seq) :enumerator popped-state)
          popped-state))
    state)))


;; enumerator_ff
;; changes the pointer of the top enumerator to the last position (length - 1)
;; discards an empty enumerator (edge case)
;;
(define-registered
  enumerator_ff
  ^{:stack-types [:enumerator :exec]}
  (fn [state]
    (if (contains-at-least? state :enumerator 1)
      (let [old-seq (:collection (top-item :enumerator state))
            popped-state (pop-item :enumerator state)]
        (if (not (empty? old-seq))
          (push-item (enum/construct-enumerator old-seq (dec (count old-seq))) :enumerator popped-state)
          popped-state))
    state)))


;; enumerator_first
;; changes the pointer of the top enumerator to 0 & pushes its first item to :exec
;; discards an empty enumerator (edge case)
;;
(define-registered
  enumerator_first
  ^{:stack-types [:enumerator :exec]}
  (fn [state]
    (if (contains-at-least? state :enumerator 1)
      (let [old-seq (:collection (top-item :enumerator state))
            popped-state (pop-item :enumerator state)]
        (if (not (empty? old-seq))
          (apply-patches popped-state
            :exec [(first old-seq)] 
            :enumerator [(enum/new-enumerator old-seq)])
          popped-state))
      state)))


;; enumerator_last
;; changes the pointer of the top enumerator to its max & pushes its last item to :exec
;; discards an empty enumerator (edge case)
;;
(define-registered
  enumerator_last
  ^{:stack-types [:enumerator :exec]}
  (fn [state]
    (if (contains-at-least? state :enumerator 1)
      (let [old-seq (:collection (top-item :enumerator state))
            popped-state (pop-item :enumerator state)]
        (if (not (empty? old-seq))
          (apply-patches 
            popped-state
            :enumerator [(enum/construct-enumerator old-seq (dec (count old-seq)))]
            :exec [(last old-seq)])
          popped-state))
      state)))


;; enumerator_forward
;; increments the top enumerator's pointer by 1
;; discards it if the pointer value exceeds the max (length - 1) 
;; discards an empty enumerator (edge case)
;;
(define-registered
  enumerator_forward
  ^{:stack-types [:enumerator]}
  (fn [state]
    (if (contains-at-least? state :enumerator 1)
      (let [old-state (top-item :enumerator state)
            old-seq (:collection old-state)
            old-ptr (:pointer old-state)
            done (>= (inc old-ptr) (count old-seq))
            popped-state (pop-item :enumerator state)]
        (if (and (not (empty? old-seq)) (not done))
          (push-item (enum/construct-enumerator old-seq (inc old-ptr))
                     :enumerator
                     popped-state)
          popped-state))
      state)))


;; enumerator_backward
;; decrements the top enumerator's pointer by 1
;; discards it if the pointer falls below 0 
;; discards an empty enumerator (edge case)
;;
(define-registered
  enumerator_backward
  ^{:stack-types [:enumerator]}
  (fn [state]
    (if (contains-at-least? state :enumerator 1)
      (let [old-state (top-item :enumerator state)
            old-seq (:collection old-state)
            old-ptr (:pointer old-state)
            done (neg? (dec old-ptr))
            popped-state (pop-item :enumerator state)]
        (if (and (not (empty? old-seq)) (not done))
          (push-item (enum/construct-enumerator old-seq (dec old-ptr))
                     :enumerator
                     popped-state)
          popped-state))
    state)))


;; enumerator_next
;; pushes the item at the pointer position onto :exec
;; increments the pointer by 1
;; discards the enumerator if the pointer value exceeds the max (length - 1) 
;; discards an empty enumerator (edge case)
;;
(define-registered
  enumerator_next
  ^{:stack-types [:enumerator :exec]}
  (fn [state]
    (if (contains-at-least? state :enumerator 1)
      (let [old-state (top-item :enumerator state)
            old-seq (:collection old-state)
            old-ptr (:pointer old-state)
            done (>= (inc old-ptr) (count old-seq))
            popped-state (pop-item :enumerator state)]
        (if (not (empty? old-seq))
          (let [state-with-item (push-item (nth old-seq old-ptr) :exec popped-state)]
            (if (not done)
              (push-item (enum/construct-enumerator old-seq (inc old-ptr))
                :enumerator
                state-with-item)
              state-with-item))
          popped-state))
    state)))


;; enumerator_prev
;; pushes the item at the pointer position onto :exec
;; decrements the pointer by 1
;; discards the enumerator if the pointer value falls below 0 
;; discards an empty enumerator (edge case)
;;
(define-registered
  enumerator_prev
  ^{:stack-types [:enumerator :exec]}
  (fn [state]
    (if (contains-at-least? state :enumerator 1)
      (let [old-state (top-item :enumerator state)
            old-seq (:collection old-state)
            old-ptr (:pointer old-state)
            done (< (dec old-ptr) 0)
            popped-state (pop-item :enumerator state)]
        (if (not (empty? old-seq))
          (let [state-with-item (push-item (nth old-seq old-ptr) :exec popped-state)]
            (if (not done)
              (push-item
                (enum/construct-enumerator old-seq (dec old-ptr))
                :enumerator
                state-with-item)
              state-with-item))
          popped-state))
    state)))


;; enumerator_set
;; takes an :integer and sets the top :enumerator pointer to that value
;; discards the enumerator if the pointer value falls below 0 
;; discards the enumerator if the pointer value exceeds the max 
;; discards an empty enumerator (edge case)
;;
(define-registered
  enumerator_set
  ^{:stack-types [:enumerator :integer]}
  (fn [state]
    (if (contains-at-least? state :enumerator 1 :integer 1)
      (let [old-seq (:collection (top-item :enumerator state))
            new-ptr (top-item :integer state)
            popped-state (pop-item :enumerator (pop-item :integer state))]
        (if (not (empty? old-seq))
          (if (and (not (neg? new-ptr)) 
                   (< new-ptr (count old-seq)))
            (push-item (enum/construct-enumerator old-seq new-ptr) :enumerator popped-state)
            popped-state)
          popped-state))
    state)))



;; enumerator_map_code
;; discards an empty enumerator (edge case)
;; pushes a copy of the instruction onto :exec
;; pushes a copy of the top :code item onto :exec
;; pushes the current item in the enumerator onto :exec
;; advances the enumerator pointer
;; discards the enumerator if the pointer value exceeds the max 
;; pops the top :code item when it expires (but not before)
;;
(define-registered
  enumerator_map_code
  ^{:stack-types [:code :enumerator :exec]}
  (fn [state]
    (if (contains-at-least? state :enumerator 1 :code 1)
      (let [old-enum (top-item :enumerator state)
            old-seq (:collection old-enum)
            old-ptr (:pointer old-enum)
            the-code (top-item :code state)
            popped-state (pop-item :code (pop-item :enumerator state))
            done (> (inc old-ptr) (count old-seq))]
        (if (and (not (empty? old-seq)) (not done))
            (apply-patches popped-state 
              :exec [(nth old-seq old-ptr) the-code 'enumerator_map_code]
              :enumerator [(enum/construct-enumerator old-seq (inc old-ptr))]
              :code [the-code])
            popped-state))
    state)))
