(ns ufo.sql
  (:require
   [ufo
    [regexps :as re]]))

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

(defn users [{:keys [ids] :as prm}]
  (let [sql (str "
select
  emp_no " (name :id) "
 ,emp_no " (name :abrev) "
 ,first_name " (name :fname) "
 ,last_name " (name :lname) "
from employees where
emp_no in (" (re/inclause {:elems ids ; ids is a vector
                           :contract re/uid?}) ")
"
                 (postfix prm))]
    (assoc prm :f "users" :sql sql)))

(defn salaries [{:keys [] :as prm}]
  (let [sql (str "
select
  emp_no " (name :id) "
 ,emp_no " (name :abrev) "
 ,max(salary) " (name :salary) "
from salaries where
emp_no between 10010 and 10020
group by emp_no
"
                 (postfix prm))]
    (assoc prm :f "salaries" :sql sql)))
