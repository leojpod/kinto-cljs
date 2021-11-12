(ns kinto-todo.db)

(def default-db
  {:task-input ""
   :tasks (.collection (new js/Kinto) "tasks")})
