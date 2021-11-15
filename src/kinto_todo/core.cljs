(ns kinto-todo.core
  (:require
   [reagent.dom :as rdom]
   [re-frame.core :as re-frame]
   [kinto-todo.kinto :as kinto]
   [kinto-todo.events :as events]
   [kinto-todo.views :as views]
   [kinto-todo.config :as config]))

(defn dev-setup []
  (when config/debug?
    (println "dev mode")))

(defn ^:dev/after-load mount-root []
  (re-frame/clear-subscription-cache!)
  (let [root-el (.getElementById js/document "app")]
    (rdom/unmount-component-at-node root-el)
    (rdom/render [views/main-panel] root-el)))

(defn init []
  (kinto/init ::events/update-task-list)
  (re-frame/dispatch-sync [::events/initialize-db])
  (re-frame/dispatch-sync [::kinto/list-tasks ::events/update-task-list])
  (dev-setup)
  (mount-root))
