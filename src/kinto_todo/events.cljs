(ns kinto-todo.events
  (:require
   [re-frame.core :as re-frame]
   [kinto-todo.kinto :as kinto]
   [kinto-todo.db :as db]))

(re-frame/reg-event-db
 ::initialize-db
 (fn [_ _]
   db/default-db))

(re-frame/reg-event-db
 ::set-task-input
 (fn [db [_ value]]
   (assoc db :task-input value)))

(re-frame/reg-event-fx
 ::add-task
 (fn [{:keys [db]} _]
   (let [task-input (:task-input db)]
     {:db (assoc db :task-input "")
      :kinto [::kinto/create-task task-input ::update-task-list]})))

(re-frame/reg-event-db
 ::update-task-list
 (fn [db [_ tasks]]
   (assoc db :tasks tasks)))

(re-frame/reg-event-fx
 ::update-task
 (fn [_ [_ task]]
   {:kinto [::kinto/update-task task ::update-task-list]}))

(re-frame/reg-event-fx
 ::clear-completed-tasks
 (fn [_ _]
   {:kinto [::kinto/delete-completed-tasks ::update-task-list]}))

(re-frame/reg-event-fx
 ::sync-with-server
 (fn [_ _]
   {:kinto [::kinto/sync-up ::update-task-list]}))
