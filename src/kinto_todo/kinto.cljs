(ns kinto-todo.kinto
  (:require
   [cljs.core.match :refer [match]]
   [re-frame.core :as re-frame]))

(keyword 'create-task)
(keyword 'update-task)
(keyword 'list-tasks)
(keyword 'delete-completed-tasks)
(keyword 'sync-up)

(defonce kinto
  (new js/Kinto))

(defonce sync-settings {:remote "https://kinto.dev.mozaws.net/v1"
                        :headers {:Authorization (str "Basic " (js/btoa "user:pass"))}})

(defonce tasks (.collection kinto "tasks"))

(defn- kinto->data [js-col]
  (->> js-col
       (.-data)
       (map (fn [js-record]
              {:id (.-id js-record)
               :title (.-title js-record)
               :done (.-done js-record)}))))

(defn- fix-conflicts [js-res]
  (->> js-res
       (js->clj)
       (:conflicts)
       (map (fn [conflict]
              (.resolve tasks (clj->js conflict) (clj->js (:remote conflict)))))
       (js/Promise.all)))

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
        (.catch #(js/console.error "woops, couldn't list tasks: " %)))

    [[::update-task task on-success]]
    (-> tasks
        (.update (clj->js task))
        (.then #(re-frame/dispatch [::list-tasks on-success]))
        (.catch #(js/console.error "woops, couldn't update a task: " %)))

    [[::delete-completed-tasks on-success]]
    (-> tasks
        (.list)
        (.then (fn [js-data] (->> js-data
                                  (kinto->data)
                                  (filter #(:done %))
                                  (map #(.delete tasks (:id %)))
                                  (js/Promise.all))))
        (.then #(re-frame/dispatch [::list-tasks on-success]))
        (.catch #(js/console.error "woops, could not delete tasks")))

    [[::sync-up on-success]]
    (-> tasks
        (.sync (clj->js sync-settings))
        (.then #(fix-conflicts %))
        (.then #(re-frame/dispatch [::list-tasks on-success]))
        (.catch #(js/console.error "woops, could not synchronize")))))

(re-frame/reg-fx
 :kinto
 kinto-handler)

(re-frame/reg-event-fx
 ::list-tasks
 (fn [_ [_ on-success]]
   {:kinto [::list-tasks on-success]}))
