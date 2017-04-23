(ns ufo.db)

(def default-db
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
     :cols [:id :salary :abbrev]}

    :users
    {:sqlfn :users
     :name "Users"
     :cols [:id :fname :lname]}}})

