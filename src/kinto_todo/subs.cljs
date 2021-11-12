(ns kinto-todo.subs
  (:require
   [re-frame.core :as re-frame]))

(re-frame/reg-sub
 ::task-input
 (fn [db]
   (:task-input db)))

(re-frame/reg-sub
 ::task-list
 (fn [db] (sort-by :id (:tasks db))))

