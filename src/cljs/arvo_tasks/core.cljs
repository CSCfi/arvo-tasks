(ns arvo-tasks.core
  (:require [reagent.core :as r]
            [re-frame.core :as rf]
            [secretary.core :as secretary]
            [goog.events :as events]
            [goog.history.EventType :as HistoryEventType]
            [markdown.core :refer [md->html]]
            [ajax.core :refer [GET POST]]
            [arvo-tasks.ajax :refer [load-interceptors!]]
            [arvo-tasks.events]
            [arvo-tasks.components :refer [file-upload vastaajatunnukset-button]])
  (:import goog.History))

(defn nav-link [uri title page collapsed?]
  (let [selected-page (rf/subscribe [:page])]
    [:li.nav-item
     {:class (when (= page @selected-page) "active")}
     [:a.nav-link
      {:href uri
       :on-click #(reset! collapsed? true)} title]]))

(defn navbar []
  (r/with-let [collapsed? (r/atom true)]
    [:nav.navbar.navbar-dark.bg-primary
     [:button.navbar-toggler.hidden-sm-up
      {:on-click #(swap! collapsed? not)} "☰"]
     [:div.collapse.navbar-toggleable-xs
      (when-not @collapsed? {:class "in"})
      [:a.navbar-brand {:href "#/"} "arvo-tasks"]
      [:ul.nav.navbar-nav
       [nav-link "#/" "Uraseuranta" :home collapsed?]
       [nav-link "#/about" "About" :about collapsed?]]]]))

(defn questionnaire-selector [questionnaires]
  [:select {:id :questionnaire :on-change #(rf/dispatch [:set-selected-questionnaire (-> % (.-target) (.-value))])}
   (for [q @questionnaires]
     [:option {:key (:id q) :value (:id q)} (:name q)])])

(defn about-page []
  [:div.container
   [:div.phases
    [:div.col-md-12
     [:img {:src (str js/context "/img/warning_clojure.png")}]]]])

(defn home-page []
  [:div.container
   (when-let [docs @(rf/subscribe [:docs])]
     [:div.row>div.col-sm-12
      [:div {:dangerouslySetInnerHTML
             {:__html (md->html docs)}}]])])

(def pages
  {:home #'home-page
   :about #'about-page})

(defn get-selected-questionnaire [selected questionnaires]
  (println "getting selected from: " @questionnaires)
  (println "getting selected with id: " @selected)
  (println "Result: " (first (filter #(= @selected (:id %)) @questionnaires)))
  ;(some #(if (= @selected (:id %)) % ) @questionnaires)
  (first (filter #(= @selected (:id %)) @questionnaires)))

(defn page []
  (let [questionnaires (rf/subscribe [:questionnaires])
        selected-questionnaire (rf/subscribe [:selected-questionnaire])
        current-questionnaire (get-selected-questionnaire selected-questionnaire questionnaires)]
    [:div
     [navbar]
     [questionnaire-selector questionnaires]
     [:div.phases
      [file-upload]]
      ;[vastaajatunnukset-button]
     [:div.stats
      [:h1 "Tilastot"]
      [:p "Tähän statistiikkaa kyselystä?"]]]))


;; -------------------------
;; Routes
(secretary/set-config! :prefix "#")

(secretary/defroute "/" []
  (rf/dispatch [:set-active-page :home]))

(secretary/defroute "/about" []
  (rf/dispatch [:set-active-page :about]))

;; -------------------------
;; History
;; must be called after routes have been defined
(defn hook-browser-navigation! []
  (doto (History.)
    (events/listen
      HistoryEventType/NAVIGATE
      (fn [event]
        (secretary/dispatch! (.-token event))))
    (.setEnabled true)))

;; -------------------------
;; Initialize app
(defn fetch-docs! []
  (GET "/docs" {:handler #(rf/dispatch [:set-docs %])}))

(defn mount-components []
  (rf/clear-subscription-cache!)
  (r/render [#'page] (.getElementById js/document "app")))

(defn init! []
  (rf/dispatch-sync [:initialize-db])
  (load-interceptors!)
  (fetch-docs!)
  (hook-browser-navigation!)
  (mount-components))
