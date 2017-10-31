(ns arvo-tasks.events
  (:require [arvo-tasks.db :as db]
            [arvo-tasks.ajax :as ajax2]
            [day8.re-frame.http-fx]
            [re-frame.core :refer [dispatch reg-event-db reg-sub reg-event-fx]]
            [ajax.core :as ajax]))

;;dispatchers

(reg-event-db
  :initialize-db
  (fn [_ _]
    db/default-db))

(reg-event-db
  :set-active-page
  (fn [db [_ page]]
    (assoc db :page page)))

(reg-event-db
  :set-docs
  (fn [db [_ docs]]
    (assoc db :docs docs)))

(reg-event-db
  :set-selected-questionnaire
  (fn [db [_ selected]]
    (println "Dispatching selected questionnaire: " selected)
    (assoc db :selected-questionnaire selected)))

(reg-event-db :upload-complete
  (fn [db [_ resp]]
    (println "Upload Completed")
    (assoc-in db [:upload :status] :ok)))

(reg-event-db :upload-failed
  (fn [db [_ resp]]
    (println "Upload Failed")
    (assoc-in db [:upload :status] :failed)))

(reg-event-fx
  :upload-file
  (fn [{db :db} event]
    (let [[_ form-data] event]
      (ajax2/upload-file form-data :upload-complete :upload-failed))
    :db (assoc-in db [:upload :status] :in-progress)))

(reg-event-fx :luo-tunnukset
  (fn [event]
    {:http-xhrio {:method          :get
                  :uri             (str "/api/luotunnukset")
                  :response-format (ajax/json-response-format)
                  :on-success      [:process-questionnaire-response]
                  :on-failure      [:bad-response]}}))

;;subscriptions

(reg-sub
  :page
  (fn [db _]
    (:page db)))

(reg-sub
  :docs
  (fn [db _]
    (:docs db)))

(reg-sub
  :questionnaires
  (fn [db _]
    (:questionnaires db)))

(reg-sub
  :selected-questionnaire
  (fn [db _]
    (:selected-questionnaire db)))
