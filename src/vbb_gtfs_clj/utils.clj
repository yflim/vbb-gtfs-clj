(ns vbb-gtfs-clj.utils
  (:require [charred.api :as charred]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [inflections.core :as inflect]))

(defn prepend-entity-name [colname entity-name]
  (str entity-name "/" colname))

(defn xf-colname [colname entity-name]
  (-> (str/replace colname (re-pattern (str "^" entity-name "_" "(.*)")) "$1")
      (str/replace "_" "-")
      (prepend-entity-name entity-name)))

(defn read-edit-write-csv [from to]
  (with-open [reader (io/reader from)
              writer (io/writer to)]
    (let [data (charred/read-csv reader)
          entity-name (-> (re-matches #".*\/(.*)\.csv" from)
                          (nth 1)
                          inflect/singular)
          header (map #(xf-colname % entity-name) (first data))]
      (charred/write-csv writer (concat [header] (rest data))))))
