(ns kinto-todo.views
  (:require
   [re-frame.core :as re-frame]
   [kinto-todo.events :as events]
   [kinto-todo.subs :as subs]))


;; home


(defn home-panel []
  [:div.flex.flex-col.space-y-4.p-4
   [:h1
    "Tasks with Kinto and re-frame"]

   [:div.flex.space-x-2

    [:input {:value @(re-frame/subscribe [::subs/task-input])
             :on-change #(re-frame/dispatch-sync [::events/set-task-input (-> % .-target .-value)])
             :placeholder "enter your task here"}]
    [:button {:class "m-2 flex space-x-2 items-center"
              :on-click #(re-frame/dispatch [::events/add-task])}
     [:i.gg-add]
     [:span  "Add"]]]
   [:ul.flex.flex-col.space-y-4.p-4.border-2.rounded-lg
    (let [editing @(re-frame/subscribe [::subs/task-editing])]
      (doall (map
              (fn [{:as task :keys [id title done]}]
                (if (= id (:task-id editing))
                  [:li.flex.items-center {:key id}
                   [:input.flex-grow {:value (:task-name editing)
                                      :on-change #(re-frame/dispatch-sync
                                                   [::events/update-editing-task (-> % .-target .-value)])
                                      :placeholder "new task name here"}]
                   [:button {:class "bg-transparent border-0 text-gray-500 hover:text-red-700 hover:shadow-none"
                             :on-click #(re-frame/dispatch [::events/end-editing-task false])}
                    [:i.gg-close]]
                   [:button {:class "bg-transparent border-0 text-gray-500 hover:text-green-700 hover:shadow-none"
                             :on-click #(re-frame/dispatch [::events/end-editing-task true])}
                    [:i.gg-check-o]]]
                  [:li.flex.items-center {:key id}
                   [:label.flex-grow
                    [:input {:type "checkbox"
                             :on-change #(re-frame/dispatch [::events/update-task (assoc task :done (not done))])
                             :checked done}]

                    [:span title]]

                   [:button {:class "bg-transparent border-0 text-gray-700 hover:text-blue-700 hover:shadow-none" :on-click #(re-frame/dispatch [::events/start-editing-task id])}
                    [:i.gg-pen]]]))

              @(re-frame/subscribe [::subs/task-list]))))]
   [:nav.flex.space-x-4
    [:button {:class "flex space-x-2 items-center bg-gray-300"
              :on-click #(re-frame/dispatch [::events/clear-completed-tasks])}
     [:i.gg-trash]
     [:span "Clear completed"]]
    [:button {:class "flex space-x-2 items-center bg-green-400 border-green-600"
              :on-click #(re-frame/dispatch [::events/sync-with-server])}

     [:i.gg-sync]
     [:span "Synchronize"]]]])

;; main


(defn main-panel []
  (home-panel))
