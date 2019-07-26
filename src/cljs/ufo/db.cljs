(ns ufo.db)

(def default-db
  (conj
   {:render-math true}
   {:loading? false
    :error false
    :name "github profile"
    :user {:profile {}
           :repos []}
    :emps {:10010 nil :10011 nil}
    :tables
    {:list [:salaries :users]

     :salaries
     {:sqlfn :salaries
      :name "Salaries"
      :cols [:id :salary]}

     :users
     {:sqlfn :users
      :name "Users"
      :cols [:id :fname :lname]}}}))

