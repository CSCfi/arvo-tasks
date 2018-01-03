(ns arvo-tasks.integration.arvo
  (:require [buddy.sign.jwt :as jwt]
            [clj-http.client :as client]
            [arvo-tasks.config :refer [env]]))

(defn liita-vastaajatunnukset! [tunnukset]
  (let [auth-header (str "Bearer "
                         (jwt/sign {:caller "avopfi"} (:arvo-shared-secret env)))
        resp (client/post (str (:arvo-api-url env)"/public/uraseuranta/luotunnuksia")
                          {:headers {:Authorization auth-header}
                           :content-type :json
                           :as :json
                           :form-params tunnukset})]
    (:body resp)))

(defn get-uraseuranta-questionnaires []
  (let [auth-header (str "Bearer "
                         (jwt/sign {:caller "avopfi"} (:arvo-shared-secret env)))
        resp (client/get (str (:arvo-api-url env)"/public/uraseuranta/kyselyt")
                         {:headers {:Authorization auth-header}
                          :content-type :json
                          :as :json})]
    (:body resp)))

(defn hae-vastaajat [kyselykertaid]
  (let [auth-header (str "Bearer "
                         (jwt/sign {:caller "avopfi"} (:arvo-shared-secret env)))
        resp (client/get (str (:arvo-api-url env)"/public/uraseuranta/vastanneet/"kyselykertaid)
                         {:headers {:Authorization auth-header}
                          :content-type :json
                          :as :json})]
    (:body resp)))