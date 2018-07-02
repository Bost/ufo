(ns ufo.monad-test
  (:require [clojure.test :refer :all]
            [ufo.monad :refer :all]))

(defn f [x] (unit (+ x x)))
(defn g [n] (unit (+ 1 n)))
(defn h [x] (unit 5))

#_(comment
  (= (m-bind (m-result value) function)
     (function value)))

(deftest test-monad-laws-identity
  (testing "Identity law: μ ∘ Tη = μ ∘ ηT = idT"
    ;; left identity
    (is (= (bind (unit 3) f)
           (f 3)))
    ;; right identity
    (is (= (bind (unit 3) unit)
           (unit 3)))))


#_(comment
  (= (m-bind (m-bind monadic-expression
                     function1)
             function2)
     (m-bind monadic-expression
             (fn [x] (m-bind (function1 x)
                            function2)))))

(deftest test-monad-laws-assoc
  (testing "Associativity law: μ ∘ Tμ = μ ∘ μT"
    (is (= (bind (bind (unit 3) f) g)
           (bind (unit 3)
                 (fn [x] (bind (f x)
                              g)))))))

#_(comment
  (= (m-plus m-zero monadic-expression)
     (m-plus monadic-expression m-zero)
     monadic-expression))
