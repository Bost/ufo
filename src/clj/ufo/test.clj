(ns ufo.test)

;; m-result is required. Type signature: m-result: a -> m a
(defn m-result [x] (vector x))

;; m-bind is required. Type signature: m-bind: m a -> (a -> m b) -> m b
(defn m-bind [m-val m-func] (mapv m-func m-val))

;; m-zero is optional; Type signature: m-zero: a -> m a
(defn m-zero [_] (m-result 0))

;; m-plus is optional; Type signature: m-plus: m a -> m a -> m a
(defn m-plus [m-val1 m-val2] (mapv (fn [v1 v2] (+ v1 v2)) m-val1 m-val2))

(defn f [x] (+ x x))
(defn g [n] (+ 1 n))
(defn h [x] 5)

(defn mf [x] (m-result (f x)))
(defn mg [n] (m-result (g n)))
(defn mh [x] (m-result (h x)))

(defn test-monad-laws-identity
  "Identity law: μ ∘ Tη = μ ∘ ηT = idT"
  []
  (and (= (m-bind (m-result 3) mf)
          (mf 3))
       (= (m-bind (m-result 3) m-result)
          (m-result 3))
       (= (m-bind (m-result 3) m-zero)
          (m-zero 3))
       ))

(defn test-monad-laws-assoc
  "Associativity law: μ ∘ Tμ = μ ∘ μT"
  []
  (= (m-bind (m-bind (m-result 3) mf) mg)
     (m-bind (m-result 3) (fn [x] (m-bind (mf x) mg)))))

(defn test-monad-laws-identity-zero-plus
  "Identity law: μ ∘ Tη = μ ∘ ηT = idT"
  []
  (and (= (m-plus (m-result 3) (m-zero 3))
          (m-plus (m-zero 3) (m-result 3))
          (m-result 3))
       (= (m-plus (m-result 3) (m-result 6))
          (m-plus (m-result 6) (m-result 3)))))
