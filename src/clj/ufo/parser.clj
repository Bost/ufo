(ns ufo.parser
  (:require
   [clojure.tools.reader.edn :as edn]
   [clojure.algo.monads :as m]))

(m/defmonad parser-m
  ;; (fn [strn] ... ) is the monadic container
  [;; m-result is required. Type signature: m-result: a -> m a
   m-result (fn [x]
              (fn [strn]
                (list x strn)))

   ;; m-bind  is required. Type signature: m-bind: m a -> (a -> m b) -> m b
   m-bind (fn [parser func]
            (fn [strn]
              (let [result (parser strn)]
                (when (not= nil result)
                  ((func (first result)) (second result))))))

   ;; m-zero is optional
   m-zero (fn [strn]
            nil)

   ;; m-plus is optional
   m-plus (fn [& parsers]
            (fn [strn]
              (first
               (drop-while nil?
                           (map #(% strn) parsers)))))])

(defn any-char [strn]
  (if (= "" strn)
    nil
    (list (first strn) (. strn (substring 1)))))

(defn char-test [pred]
  (m/domonad parser-m
           [c any-char
            :when (pred c)]
           (str c)))

(defn is-char [c]
  (char-test (partial = c)))

(defn one-of [target-strn]
  (let [str-chars
        (set target-strn)
        #_(into #{} target-strn)]
    (char-test (fn [key] (contains? str-chars key)))))

(defn none-of [target-strn]
  (let [str-chars
        (set target-strn)
        #_(into #{} target-strn)]
    (char-test (fn [key] (not (contains? str-chars key))))))

(def double-quote "\"")
(def left-bracket "[")
(def right-bracket "]")
(def text
  #_(one-of " abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ-_")
  (none-of (str double-quote left-bracket right-bracket)))

(m/with-monad parser-m
  (defn match-string [target-strn]
    (if (= "" target-strn)
      (m-result "")
      (m/domonad parser-m
               [c (is-char (first target-strn))
                cs (match-string (. target-strn (substring 1)))]
               (str c cs) )))

  (defn match-all [& parsers]
    (m/m-fmap (partial apply str)
            (m/m-seq parsers)))

  (defn optional [parser]
    (m/m-plus parser (m-result nil)))

  (def match-one m/m-plus)

  (declare one-or-more)

  (defn none-or-more [parser]
    (optional (one-or-more parser)))

  (defn one-or-more [parser]
    (m/domonad
     [a parser
      as (none-or-more parser)]
     (str a as))))

(def exp-parser
  ;; Parsing a character means separating it from the rest
  (m/domonad parser-m
             [txt (none-or-more text)
              exp
              (none-or-more
               (match-all
                (match-string left-bracket)
                (none-or-more whitespace)
                (match-string "e")
                (none-or-more whitespace)
                (match-string double-quote)
                ;;
                (none-or-more text)
                ;;
                (match-string double-quote)
                (none-or-more whitespace)
                (match-string right-bracket)))]
             [{:type :txt :val txt} {:type :exp :val exp}]))

(defn search
  [file]
  (as->
      "aaa [e \"1 + 2 + 3\"] bbb" $
    ;; (slurp file) $
    (exp-parser $)))

(def max-cnt 100) ;; category-theory.org has about 48 [e "..."]

(def parse
  (fn [ss]
    (loop [s ss
           acc []
           cnt 0]
      (if (> cnt max-cnt)
        (do
          (println "Stopping at (>" cnt "max-cnt)")
          acc)
        (if (empty? s)
          acc
          (let [[txt-exp rest] (exp-parser s)]
            (recur rest (into acc txt-exp) (inc cnt))))))))
