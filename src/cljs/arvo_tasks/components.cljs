(ns arvo-tasks.components
  (:require [re-frame.core :as rf]))


(defn upload-component []
  [:div
   [:form {:id "upload-form"
           :enc-type "multipart/form-data"
           :method "POST"}
    [:label "Upload Filename: "]
    [:input {:type "file"
             :name "upload-file"
             :id "upload-file"}]]])


(defn handle-file-upload [element-id]
  (let [el (.getElementById js/document element-id)
        name (.-name el)
        file (aget (.-files el) 0)
        form-data (doto
                   (js/FormData.)
                   (.append name file))]
    (rf/dispatch [:upload-file form-data])))

(defn upload-button []
  [:div
   [:hr]
   [:button {:class "btn btn-primary" :type "button"
             :on-click #(handle-file-upload "upload-file")}
    "Upload file" [:span {:class "fa fa-upload"}]]])

(defn vastaajatunnukset-button []
  [:button {:class "btn btn-primary" :type "button"
            :on-click #(rf/dispatch [:luo-tunnukset])}
   "Luo vastaajatunnukset" [:span {:class "fa fa-upload"}]])

(defn file-upload []
  [:div
   [upload-component]
   [upload-button]])