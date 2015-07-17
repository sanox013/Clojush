;; enumerator.clj
;; define Enumerator type and some associated functions
;; Bill Tozier, bill@vagueinnovation.com, 2015
;;
;;
;; an Enumerator is a simple Clojure record containing
;;   - an immutable seq (list, vector, map, string, etc)
;;   - a pointer variable, an integer, which indicates which item is "current"


(ns clojush.types.enumerator)

(defrecord Enumerator [collection pointer])

(defn construct-enumerator [contents pointer] 
  (Enumerator. contents pointer))

(defn new-enumerator [contents] 
  (Enumerator. contents 0))

(defn enumerator? [thing]
  (instance? Enumerator thing))