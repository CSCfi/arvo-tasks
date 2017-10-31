(ns arvo-tasks.integration.arvo
  (:require [buddy.sign.jwt :as jwt]
            [clj-http.client :as client]))

(def test-data [{:id 1 :kyselykertaid 474 :kieli "fi"}
                {:id 2 :kyselykertaid 474 :kieli "sv"}
                {:id 3 :kyselykertaid 474 :kieli "en"}])

(def secret "secret")

(defn luo-vastaajatunnukset! []
  (let [auth-header (str "Bearer "
                         (jwt/sign {:caller "avopfi"} secret))
        resp (client/post "http://localhost:8082/api/public/uraseuranta/luotunnuksia"
                          {:headers {:Authorization auth-header}
                           :content-type :json
                           :as :json
                           :form-params test-data})]
    (println (:body resp))))

(defn get-uraseuranta-questionnaires []
  (let [auth-header (str "Bearer "
                         (jwt/sign {:caller "avopfi"} secret))
        resp (client/get "http://localhost:8082/api/public/uraseuranta/kyselyt"
                          {:headers {:Authorization auth-header}
                           :content-type :json
                           :as :json})]
    (:body resp)))