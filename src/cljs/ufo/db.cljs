(ns ufo.db)

(def default-db
  {
   :loading? false
   :error false
   :name "github profile"
   :user {
          :profile {}
          :repos []
          }
   :emps {:10010 nil
          :10011 nil}
   })

#_(def app-state
  {:search/results []
   :search/user []
   :list/trows []
   :list/tables
   [{:id   :salaries
     :sqlfn :salaries
     :tname "Salaries"
     :cols [:id :salary :abrev]}
    #_{:id   :users
       :sqlfn :users
       :tname "Users"
       :cols [:id :fname :lname]}]})
