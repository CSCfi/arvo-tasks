(ns arvo-tasks.excel
  (:require [dk.ative.docjure.spreadsheet :refer :all]
            [clojure.string :as s]
            [clojure.java.io :refer [output-stream]])
  (:import (java.io FileInputStream OutputStream File)
           (org.apache.poi.ss.usermodel WorkbookFactory)
           (org.apache.poi.poifs.filesystem POIFSFileSystem)
           (org.apache.poi.poifs.crypt EncryptionInfo EncryptionMode)
           (org.apache.poi.xssf.usermodel XSSFWorkbook)
           (org.apache.poi.openxml4j.opc OPCPackage PackageAccess)))

(defn open-workbook-from-stream [^FileInputStream stream ^String password]
  (WorkbookFactory/create stream (str password)))

(defn get-encryptor [^POIFSFileSystem fs ^String password]
  (let [encryptioninfo (EncryptionInfo. EncryptionMode/agile)
        encryptor (.getEncryptor encryptioninfo)]
    (do
      (.confirmPassword encryptor password)
      encryptor)))

(defn encrypt-workbook [^String filename ^String password]
  (let [fs (POIFSFileSystem.)
        enc (get-encryptor fs password)
        opc (OPCPackage/open (File. filename) PackageAccess/READ_WRITE)
        os (.getDataStream enc fs)
        _ (do
            (.save opc os)
            (.close opc))]
    (with-open [fos (output-stream filename)]
      (.writeFilesystem fs fos))))



(defn open-workbook [filename password]
  (with-open [stream (FileInputStream. filename)]
    (open-workbook-from-stream stream password)))

(defn load-sheet [file password]
  (->> (open-workbook file password)
       (select-sheet (fn [x] true))))

(defn int-to-column [val]
  (let [quot (quot val 26)
        char (char (+ 65 (mod val 26)))]
    (keyword (str (when (< 0 quot) (int-to-column (dec quot))) char))))

(def column-seq (map int-to-column (range)))

(def format-key
  (comp
    keyword
    #(s/replace % #"ä|ö|å" {"ä" "a"
                            "ö" "o"
                            "å" "a"})
    #(s/replace % "-" "")
    #(s/replace % "-_" "-")
    #(s/replace % " " "_")
    #(s/replace % #"\n" "_")
    #(.toLowerCase %)
    str))

(defn load-excel [file password]
  (let [sheet (load-sheet file password)
        title-row (first (row-seq sheet))
        keys (map format-key (iterator-seq (.iterator title-row)))
        columns (zipmap column-seq keys)
        ret (select-columns columns sheet)]
    (rest ret)))


(defn get-row [keys data]
  (map data keys))

(defn save-excel
  ([data data-keys filename password]
   (let [header-row (map name data-keys)
         rows (map #(get-row data-keys %) data)
         out-file (str filename".xlsx")
         wb (create-workbook "Tiedot"
              (cons header-row rows))]
    (save-workbook! out-file wb)
    (when password
      (encrypt-workbook out-file password))
    out-file))
  ([data data-keys filename]
   (save-excel data data-keys filename nil)))