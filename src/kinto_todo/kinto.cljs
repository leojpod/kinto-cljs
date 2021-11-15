(ns kinto-todo.kinto
  (:require
   [cljs.core.match :refer [match]]
   [oops.core :refer [oget]]
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
  (->>  js-res
        (#(oget % "conflicts"))
        (map (fn [conflict]
               (.resolve tasks conflict (oget conflict "remote"))))
        (js/Promise.all)))

(defn- kinto-handler
  [on-success action]
  (match [action]
    [[::create-task task-name]]
    (-> (.create tasks (clj->js {:title task-name, :done false}))
        (.then #(re-frame/dispatch [::list-tasks]))
        (.catch #(js/console.error "woops task created failed" %)))

    [[::list-tasks]]
    (-> tasks
        (.list)
        (.then #(re-frame/dispatch [on-success (kinto->data %)]))
        (.catch #(js/console.error "woops, couldn't list tasks: " %)))

    [[::update-task task]]
    (-> tasks
        (.update (clj->js task))
        (.then #(re-frame/dispatch [::list-tasks]))
        (.catch #(js/console.error "woops, couldn't update a task: " %)))

    [[::delete-completed-tasks]]
    (-> tasks
        (.list)
        (.then (fn [js-data] (->> js-data
                                  (kinto->data)
                                  (filter #(:done %))
                                  (map #(.delete tasks (:id %)))
                                  (js/Promise.all))))
        (.then #(re-frame/dispatch [::list-tasks]))
        (.catch #(js/console.error "woops, could not delete tasks")))

    [[::sync-up]]
    (-> tasks
        (.sync (clj->js sync-settings))
        (.then #(fix-conflicts %))
        (.then #(re-frame/dispatch [::list-tasks]))
        (.catch #(js/console.error "woops, could not synchronize")))))

(defn init [on-success]
  (re-frame/reg-fx
   :kinto
   (partial kinto-handler on-success)))

(re-frame/reg-event-fx
 ::list-tasks
 (fn [_ _]
   {:kinto [::list-tasks]}))
