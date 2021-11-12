(ns kinto-todo.subs
  (:require
   [re-frame.core :as re-frame]))

(re-frame/reg-sub
 ::task-input
 (fn [db]
   (:task-input db)))

(re-frame/reg-sub
 ::active-panel
 (fn [db _]
   (:active-panel db)))

