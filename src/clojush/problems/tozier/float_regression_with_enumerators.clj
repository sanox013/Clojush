;; testing enumerators
;; Differs from simple_regression by using floats and also by precomputing
;; the fitness cases.

(ns clojush.problems.tozier.float-regression-with-enumerators
  (:use [clojush.pushgp.pushgp]
        [clojush.random]
        [clojush.pushstate]
        [clojush.interpreter]))

;;;;;;;;;;;;
;; Float symbolic regression of x^3 - 2x^2 - x (problem 5 from the 
;; trivial geography chapter) with minimal float instructions and an 
;; input instruction that uses the auxiliary stack.

(def fitness-cases
  (for [input (map float (range 10))]
    [input
     (- (* input input input) 
        (* 2.0 input input) 
        input)]))

(def argmap
  {:error-function (fn [program]
                     (doall
                       (for [[input target] fitness-cases]
                         (let [state (run-push program 
                                               (push-item input :input 
                                                          (push-item input :float 
                                                                     (make-push-state))))
                               top-float (top-item :float state)]
                           (if (number? top-float)
                             (Math/abs (- top-float target))
                             1000)))))
   :atom-generators (list (fn [] (lrand 10))
                          'in1
                          @registered-instructions)
   :epigenetic-markers []
   :genetic-operator-probabilities {:alternation 0.8
                                    :uniform-mutation 0.2}
   :parent-selection :tournament
   :tournament-size 7
   :population-size 300
   :max-points 400
   :max-genome-size-in-initial-program 200
   :max-generations 0
   })