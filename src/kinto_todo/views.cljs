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
    [:button {:class "flex space-x-2 items-center"
              :on-click #(re-frame/dispatch [::events/add-task])}
     [:i.gg-add]
     [:span  "Add"]]]
   [:ul.flex.flex-col.space-y-4.p-4.border-2.rounded-lg
    (map
     (fn [{:keys [id title done]}]
       [:li {:key id}
        [:input {:type "checkbox"
                 :readOnly true
                 :checked done}]
        [:span title]])
     @(re-frame/subscribe [::subs/task-list]))]])

;; main


(defn main-panel []
  (home-panel))
