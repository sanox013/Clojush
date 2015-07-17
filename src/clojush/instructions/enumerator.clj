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


(define-registered
  enumerator_from_vector_integer
  ^{:stack-types [:vector_integer :enumerator]}
  (fn [state]
    (if (not (empty? (:vector_integer state)))
      (let [collection (first (:vector_integer state))]
      (push-item (enum/new-enumerator collection)
        :enumerator
        (pop-item :vector_integer state)))
      state)))

(define-registered
  enumerator_unwrap
  ^{:stack-types [:enumerator :exec]}
  (fn [state]
    (if (not (empty? (:enumerator state)))
      (let [old-seq (:collection (top-item :enumerator state))
            popped-state (pop-item :enumerator state)]
      (push-item  old-seq :exec popped-state))
    state)))

(define-registered
  enumerator_rewind
  ^{:stack-types [:enumerator :exec]}
  (fn [state]
    (if (not (empty? (:enumerator state)))
      (let [old-seq (:collection (top-item :enumerator state))
            popped-state (pop-item :enumerator state)]
      (push-item (enum/new-enumerator old-seq) :exec popped-state))
    state)))

(define-registered
  enumerator_ff
  ^{:stack-types [:enumerator :exec]}
  (fn [state]
    (if (not (empty? (:enumerator state)))
      (let [old-seq (:collection (top-item :enumerator state))
            popped-state (pop-item :enumerator state)]
      (push-item (enum/construct-enumerator old-seq (- (count old-seq) 1)) :exec popped-state))
    state)))

(define-registered
  enumerator_first
  ^{:stack-types [:enumerator :exec]}
  (fn [state]
    (if (not (empty? (:enumerator state)))
      (let [old-seq (:collection (top-item :enumerator state))
            popped-state (pop-item :enumerator state)]
      (push-item 
        (enum/new-enumerator old-seq)
        :exec
        (push-item 
          (first old-seq)
          :exec
          popped-state)))
    state)))
