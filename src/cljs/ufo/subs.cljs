(ns ufo.subs
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [re-frame.core :as re-frame]))

(re-frame/register-sub
 :tables
 (fn [db]
   (reaction (:tables @db))))

(re-frame/register-sub
 :emps
 (fn [db]
   (reaction (:emps @db))))

(re-frame/register-sub
 :loading?
 (fn [db]
   (reaction (:loading? @db))))

(re-frame/register-sub
 :error?
 (fn [db]
   (reaction (:error @db))))

