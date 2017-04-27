(ns ufo.subs
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [re-frame.core :as re-frame]))

(re-frame/reg-sub-raw
 :tables
 (fn [db]
   (reaction (:tables @db))))

(re-frame/reg-sub-raw
 :emps
 (fn [db]
   (reaction (:emps @db))))

(re-frame/reg-sub-raw
 :loading?
 (fn [db]
   (reaction (:loading? @db))))

(re-frame/reg-sub-raw
 :error?
 (fn [db]
   (reaction (:error @db))))

