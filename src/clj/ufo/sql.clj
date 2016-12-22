(ns ufo.sql
  (:require
   [ufo
    [regexps :as re]
    ]))

(def rowlimdefault 512)
(def rowlimmax 1024)
(def rowlim-cache (* 5 rowlimmax))

(defn postfix-db2
  "(= rowlim 0) means 'no rowlim'"
  [{:keys [rowlim cache] :or {rowlim rowlimdefault}}]
  #_[(and (>= rowlim 0) ; avoid neagive rowlims; rowlim 0 means 'no rowlim'
          (<= rowlim rowlimmax))]
  (str
   (if (zero? rowlim)
     ""
     (str "fetch first " (cond
                           cache rowlim-cache
                           :else rowlim) " rows only"))
   " with ur"))

(defn postfix-mysql
  "(= rowlim 0) means 'no rowlim'"
  [{:keys [rowlim cache] :or {rowlim rowlimdefault}}]
  #_[(and (>= rowlim 0) ; avoid neagive rowlims; rowlim 0 means 'no rowlim'
          (<= rowlim rowlimmax))]
  (str
   (if (zero? rowlim)
     ""
     (str "limit 0," (cond
                     cache rowlim-cache
                     :else rowlim)))))

(def postfix postfix-mysql)

(defn get-xyz
  "Select SCHEMA.TABLE COLUMN_X, COLUMN_Y, COLUMN_Z according to xs, ys"
  [{:keys [xs ys] :as prm}]
  (let [sql (str "
select
   t.COLUMN_X " (name :colx) "
  ,t.COLUMN_Y " (name :coly) "
  ,t.COLUMN_Z " (name :colz) "
from SCHEMA.TABLE t
where 1=1
and t.COLUMN_X in (" (re/inclause {:elems xs :contract re/colx?}) ")
and t.COLUMN_Y in (" (re/inclause {:elems ys :contract re/coly?}) ")
" (postfix prm))]
    (assoc prm :f "cols-xyz" :sql sql)))

(defn users
  [{:keys [xs ys] :as prm}]
  (let [sql (str "
select
  emp_no " (name :id) "
 ,first_name " (name :fname) "
 ,last_name " (name :lname) "
from employees where emp_no between 10001 and 10002
"
              (postfix prm))]
    (assoc prm :f "cols-xyz" :sql sql)))
