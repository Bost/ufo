(ns ufo.parser
  (:require
   [clojure.tools.reader.edn :as edn]
   [clojure.algo.monads :as m]))

(def max-cnt 120) ;; category-theory.org has about 60 expressions

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

(any-char "a")   ; (\a "")
(any-char "abc") ; (\a "bc")

(defn is-char [c]
  (char-test (partial = c)))

((is-char \a) "abc") ; ("a" "bc")
((is-char \x) "abc")

(def is-1? (partial = 1))
(is-1? 2) ; false
(is-1? 1) ; true

(defn regex-test [re]
  (m/domonad parser-m
             [matched-groups
              (fn [s]
                #_(println "s:" s)
                (if-let [match (re-find re s)]
                  (let [idx (.indexOf s match)]
                    #_(println "idx:" idx)
                    (let [head (.substring s 0 idx)
                          tail (.substring s (+ idx (count match)))]
                      #_(println "head:" head)
                      #_(println "tail:" tail)
                      #_(println "res:" (list (list head match) tail))
                      #_(println "--------")
                      (list (remove nil? [(if-not (empty? head)
                                            {:type :txt :val head})
                                          {:type :match :val match}])
                            tail)))
                  (list [{:type :txt :val s}]
                        nil)))]
             matched-groups))

(defn match-re [re]
  (regex-test re))

((match-re #"ab") "abc")
;; ([{:type :txt, :val ""} {:type :match, :val "ab"}] "c")
((match-re #"ab") "xabc")
;; ([{:type :txt, :val "x"} {:type :match, :val "ab"}] "c")
((match-re #"aa") "xx aa bb aa cc")
;; ([{:type :txt, :val "xx "} {:type :match, :val "aa"}] " bb aa cc")

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
(def whitespace (one-of " \n\r\t"))
(def text
  #_(one-of " abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ-_")
  (none-of
   (str double-quote left-bracket right-bracket)
   #_left-bracket))

(m/with-monad parser-m
  (defn match-string [target-strn]
    (if (= "" target-strn)
      (m-result "")
      (m/domonad parser-m
               [c (is-char (first target-strn))
                cs (match-string (. target-strn (substring 1)))]
               (str c cs) )))

  (defn match-all "(abcde...)" [& parsers]
    (m/m-fmap (partial apply str)
            (m/m-seq parsers)))

  (defn optional [parser]
    (m/m-plus parser (m-result nil)))

  (def match-one m/m-plus)

  (declare one-or-more)

  (defn none-or-more "(a*)" [parser]
    (optional (one-or-more parser)))

  (defn one-or-more "(a+) equals to (aa*)" [parser]
    (m/domonad
     [a parser
      as (none-or-more parser)]
     (str a as))))

(defn parse [ss monadic-parser]
  (loop [s ss
         acc []
         cnt 0]
    (if (> cnt max-cnt)
      (do
        (println "Stopping at (>" cnt "max-cnt)")
        acc)
      (if (empty? s)
        acc
        (let [[txt-exp rest] (monadic-parser s)]
          #_(do
              (println "s:" s)
              (println "txt-exp:" txt-exp)
              (println "rest:" rest)
              (println "---------"))
          (recur rest (into acc txt-exp) (inc cnt)))))))

(def exp-parser
  ;; Parsing a character means separating it from the rest
  (m/domonad parser-m
             [res (match-re
                   ;; #"\[e \"(.*?)\"\]"  ;; do not group inside
                   #"\[e \".*?\"\]"
                   )]
             res))

(defn exp-transformer
  "Get the expression content: [e \"1 + 2 + 3\"] -> \"1 + 2 + 3\""
  [hms]
  (->> hms
       (map (fn [hm]
              (if (= :match (:type hm))
                (update hm :val (fn [val]
                                  (second (re-find #"\[e \"(.*)\"\]" val))))
                hm)))
       (map (fn [hm] (update hm :type (fn [val] (condp = val
                                                :match :exp
                                                val)))))))

(def title-content-parser
  ;; Parsing a character means separating it from the rest
  ;; do not group anything inside the regexes
  (m/domonad parser-m
             [bef (match-re #"\n{0,}")
              res (->> (str
                        "\\*.{0,}?\\n"
                        "\\n{0,}?")
                       (re-pattern)
                       (match-re))]
             res))

(defn title-content-transformer
  [hms]
  (->> hms
       #_(map (fn [hm]
              (if (= :match (:type hm))
                (update hm :val
                        (fn [val] (second (re-find #"\[e \"(.*)\"\]" val))))
                hm)))
       #_(map (fn [hm]
              (update hm :val (fn [val]
                                (do
                                  (println "parsed" (parse val exp-parser))
                                  (exp-transformer (parse val exp-parser)))
                                #_val))))
       (map-indexed
        (fn [i hm]
          (if (= (:type hm) :match)
            {:title (:val hm) :content (some->> i (inc) (nth hms) :val)}
            ;; otherwise return nil, so it will be removed in the next step and
            ;; thus ignored
            )))
       (remove nil?)))
