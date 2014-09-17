;; sine_regression.clj
;; An example problem for clojush, a Push/PushGP system written in Clojure
;; Nic McPhee, mcphee@morris.umn.edu, 2014

(ns clojush.problems.demos.sine-regression
  (:use [clojush.pushgp.pushgp]
        [clojush.random]
        [clojush.pushstate]
        [clojush.interpreter]))

;;;;;;;;;;;;
;; Floating-point symbolic regression of sin(x) based on the example
;; from Appendix B of _A field guide to genetic programming_) with
;; minimal float instructions and an input instruction that uses the
;; auxiliary stack.

;; Besides the differences that come from using Push instead of Koza-style
;; tree-based GP (e.g., differences in XO and mutation), this is generational
;; instead of steady state; I assume there's support for steady-state in
;; Clojush, but I haven't chased that down yet.

;; I didn't try to directly mimic the specification of the number of random
;; constants since Clojush handles those quite differently. I did, however,
;; change the range of the constants to [-5, 5].

;; For now I've left the pop size and generations at their defaults instead of
;; switching to 100K and 100 respectively, just because I'm not sure how Clojush
;; responds to these kind of paramters.

;; The fitness cases are (x, sin(x)) for x in {0.0, 0.1, ..., 6.2}, so essentially
;; 0 to 2*pi.
(def fitness-cases
  (for [input (map #(/ % 10.0) (range 63))]
    [input (Math/sin input)]))

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
   :atom-generators (list (fn [] (- (lrand 10) 5))
                          'in1
                          'float_div
                          'float_mult
                          'float_add
                          'float_sub)
   :epigenetic-markers []
   :genetic-operator-probabilities {:alternation 0.8
                                    :uniform-mutation 0.2}
   :parent-selection :tournament
   :tournament-size 2
   :population-size 100000
   :max-generations 100
   })
