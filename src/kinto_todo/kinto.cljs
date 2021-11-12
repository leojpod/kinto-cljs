(ns kinto-todo.kinto
  (:require
   [cljs.core.match :refer [match]]
   [re-frame.core :as re-frame]))

(keyword 'create-task)
(keyword 'watch-tasks)

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

    [[::watch-tasks on-success]]
    (-> tasks
        (.-events)
        (.on "change" (fn [res] (let [{:keys [targets]} (js->clj res :keywordize-keys true)] (re-frame/dispatch [on-success targets])))))))

(re-frame/reg-fx
 :kinto
 kinto-handler)

(re-frame/reg-event-fx
 ::watch-tasks
 (fn [_ [_ on-success]]
   {:kinto [::watch-tasks on-success]}))
