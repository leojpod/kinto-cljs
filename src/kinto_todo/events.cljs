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

(re-frame/reg-event-db
 ::start-editing-task
 (fn [db [_ task-id]]
   (assoc
    db
    :editing
    {:task-id task-id
     :task-name (->> (:tasks db)
                     (some #(if (= task-id (:id %))
                              %
                              false))
                     (#(:title %)))})))
(re-frame/reg-event-db
 ::update-editing-task
 (fn [db [_ value]]
   (assoc-in db [:editing :task-name] value)))

(re-frame/reg-event-fx
 ::add-task
 (fn [{:keys [db]} _]
   (let [task-input (:task-input db)]
     {:db (assoc db :task-input "")
      :kinto [::kinto/create-task task-input]})))

(re-frame/reg-event-db
 ::update-task-list
 (fn [db [_ tasks]]
   (assoc db :tasks tasks)))

(re-frame/reg-event-fx
 ::update-task
 (fn [_ [_ task]]
   {:kinto [::kinto/update-task task]}))

(re-frame/reg-event-fx
 ::clear-completed-tasks
 (fn [_ _]
   {:kinto [::kinto/delete-completed-tasks]}))

(re-frame/reg-event-fx
 ::sync-with-server
 (fn [_ _]
   {:kinto [::kinto/sync-up]}))

(re-frame/reg-event-fx
 ::end-editing-task
 (fn [{:keys [db]} [_ should-save?]]
   (let [editing (:editing db)]
     (if should-save?
       {:db (assoc db :editing :done)
        :kinto [::kinto/update-task
                (->> db
                     (:tasks)
                     (some #(if (= (:id %) (:task-id editing))
                              %
                              false))
                     (#(assoc % :title (:task-name editing))))]}

       {:db (assoc db :editing nil)}))))
