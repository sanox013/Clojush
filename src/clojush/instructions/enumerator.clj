;; enumerator.clj
;; define Enumerator type and accompanying instructions
;; Bill Tozier, bill@vagueinnovation.com, 2015

(ns clojush.instructions.enumerator
  (:use [clojush.pushstate]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Instructions for Enumerators
;;
;; an Enumerator is a simple Clojure record containing
;;   - an immutable seq (list, vector, map, string, etc)
;;   - a pointer variable, an integer, which indicates which item is "current"
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

(defrecord Enumerator [collection pointer])

;; because defrecord trying to cross namespaces is awful
(defn make-enumerator [contents pointer] 
  (Enumerator. contents pointer))


(define-registered
  enumerator_from_vector_integer
  ^{:stack-types [:vector_integer :enumerator]}
  (fn [state]
    (if (not (empty? (:vector_integer state)))
      (let [collection (first (:vector_integer state)) pointer 0]
      (push-item (make-enumerator collection pointer)
                 :enumerator
                 (pop-item :vector_integer state)))
      state)))
