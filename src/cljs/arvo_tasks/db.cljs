(ns arvo-tasks.db)

(def default-db
  {:selected-questionnaire "1"
   :upload {:status :ok}
   :questionnaires [{:id "1"
                     :name "Urauseuranta - Oppilaitos 1"
                     :subjects 1500
                     :email-addresses 1200
                     :phone-numbers 400
                     :addresses 600}
                    {:id "2"
                     :name "Urauseuranta - Oppilaitos 2"
                     :subjects 500
                     :email-addresses 450
                     :phone-numbers 400
                     :addresses 400}]})
