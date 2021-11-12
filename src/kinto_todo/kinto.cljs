(ns kinto-todo.kinto
  (:require
   [cljs.core.match :refer [match]]
   [re-frame.core :as re-frame]))

(keyword 'create-task)
(keyword 'list-tasks)

(defonce kinto
  (new js/Kinto))

(defonce tasks (.collection kinto "tasks"))

(defn- kinto->data [js-col]
  (->> js-col
       (.-data)
       (map (fn [js-record]
              {:id (.-id js-record)
               :title (.-title js-record)
               :done (.-done js-record)}))))

(defn- kinto-handler
  [action]
  (match [action]
    [[::create-task task-name on-success]]
    (-> (.create tasks (clj->js {:title task-name, :done false}))
        (.then #(re-frame/dispatch [::list-tasks on-success]))
        (.catch #(js/console.error "woops task created failed" %)))

    [[::list-tasks on-success]]
    (-> tasks
        (.list)
        (.then #(re-frame/dispatch [on-success (kinto->data %)]))
        (.catch #(js/console.error "woops, couldn't list tasks: " %)))))

(re-frame/reg-fx
 :kinto
 kinto-handler)

(re-frame/reg-event-fx
 ::list-tasks
 (fn [_ [_ on-success]]
   {:kinto [::list-tasks on-success]}))
