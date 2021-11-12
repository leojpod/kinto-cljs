(ns kinto-todo.kinto
  (:require
   [cljs.core.match :refer [match]]
   [re-frame.core :as re-frame]))

(keyword 'create-task)
(keyword 'list-tasks)

(defonce kinto
  (new js/Kinto))

(defonce tasks (.collection kinto "tasks"))

(defn- kinto-handler
  [action]
  (match [action]
    [[::create-task task-name]]
    (-> (.create tasks (clj->js {:title task-name, :done false}))
        (.then #(js/console.info "task created" %))
        (.catch #(js/console.error "woops task created failed" %)))

    [[::list-tasks]]
    (println "TODO list all the tasks on kinto")))

(re-frame/reg-fx
 :kinto
 kinto-handler)
