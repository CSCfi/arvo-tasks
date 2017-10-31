(ns arvo-tasks.ajax
  (:require [ajax.core :as ajax]
            [re-frame.core :as rf]))

(defn local-uri? [{:keys [uri]}]
  (not (re-find #"^\w+?://" uri)))

(defn default-headers [request]
  (if (local-uri? request)
    (-> request
        (update :uri #(str js/context %))
        (update :headers #(merge {"x-csrf-token" js/csrfToken} %)))
    request))

(defn load-interceptors! []
  (swap! ajax/default-interceptors
         conj
         (ajax/to-interceptor {:name "default headers"
                               :request default-headers})))

(defn upload-file [form-data success failure]
  (println "Form data " form-data)
  (ajax/POST "/api/1/upload" {:body            form-data}
                            :response-format :detect
                            :keywords?       true
                            :handler         #(rf/dispatch [success])
                            :error-handler   #(rf/dispatch [failure])))