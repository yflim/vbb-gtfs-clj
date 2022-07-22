(ns vbb-gtfs-clj.core
  (:require [environ.core :refer [env]]
            [clojure.java.shell :refer [sh]]
            [clojure.string :as str]
            [vbb-gtfs-clj.utils :as utils]))

(defn setup [opts]
  (doseq [from (->> (clojure.java.io/file (env :resource-dir))
                    file-seq
                    (filter #(.isFile %))
                    (map str)
                    (filter #(re-matches #".*\.csv" %)))]
    (let [to (str/replace from #"(.*)\.csv" "$1.cp.csv")]
      (utils/read-edit-write-csv from to)
      (sh "rm" from)
      (sh "mv" to from))))
