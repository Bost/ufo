(ns ufo.subs
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [re-frame.core :as re-frame]))

(re-frame/reg-sub
 :tables
 (fn [db]
   (:tables db)))

(re-frame/reg-sub
 :emps
 (fn [db]
   (:emps db)))

(re-frame/reg-sub
 :loading?
 (fn [db]
   (:loading? db)))

(re-frame/reg-sub
 :error?
 (fn [db]
   (:error db)))

