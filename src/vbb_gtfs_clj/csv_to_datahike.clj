(ns vbb-gtfs-clj.csv-to-datahike
  (:require [datahike.api :as d]
            [tablecloth.api :as tc]))

(defn convert-ref-col-types [ds cols-info ref-cols]
  (let [coltypes (zipmap (:name cols-info) (:datatype cols-info))
        conversion-map (->> (filter (fn [[k v]] (not= (coltypes k) (coltypes v))) ref-cols)
                            (map (fn [[k v]] [k (coltypes v)]))
                            (into {}))]
    (if (not-empty conversion-map) (tc/convert-types ds conversion-map) ds)))

(defn get-column-info [ds]
  (tc/info ds :columns))

(defn add-tempid-col [ds]
  (tc/add-column ds :db/id (range -1 (- (+ (tc/row-count ds) 1)) -1)))

(defn map-refids-to-tempids [ds ref-cols]
  (reduce-kv (fn [m k v] (assoc m k (zipmap (v ds) (:db/id ds))))
             {}
             ref-cols))

(defn update-ref-cols [ds refid-tempid-maps]
  (let [refcols (keys refid-tempid-maps)]
    (tc/update-columns ds refcols (map (fn [k] (partial map #(or ((k refid-tempid-maps) %)
                                                                 %)))
                                       refcols))))

(defn map-col-attr-vals [cfg col-info]
  (let [{:keys [id-col ref-cols cardinality-many-cols]} cfg
        {col-name :name, col-datatype :datatype} col-info
        cardinality (if (col-name cardinality-many-cols)
                      :db.cardinality/many
                      :db.cardinality/one)
        db-datatype (if (col-name ref-cols)
                      :db.type/ref
                      (case col-datatype
                        :float64 :db.type/double
                        (:int16 :int32 :int64) :db.type/long
                        (keyword "db.type" (name col-datatype))))]
    (cond-> {:db/ident       col-name
             :db/cardinality cardinality
             :db/valueType   db-datatype}
      (= id-col col-name) (assoc :db/unique :db.unique/identity))))

(defn extract-schema [cols-cfg ds]
  (mapv #(map-col-attr-vals cols-cfg %)
        (tc/rows (get-column-info (tc/select-columns ds (complement #{:db/id}))) :as-maps)))

(defn write-schema [conn cols-cfg ds]
  (d/transact conn (extract-schema cols-cfg ds)))

(defn handle-ds-with-ref-cols [ds ref-cols]
  (let [ds (add-tempid-col (convert-ref-col-types ds (get-column-info ds) ref-cols))]
    (update-ref-cols ds (map-refids-to-tempids ds ref-cols))))

(defn csv-to-datahike [csv-cfg db-cfg]
  (let [cols-cfg (:cols csv-cfg)
        ref-cols (:ref-cols cols-cfg)
        ds (cond-> (tc/dataset (:csv csv-cfg) {:key-fn keyword})
             ref-cols (handle-ds-with-ref-cols ref-cols))
        conn (d/connect db-cfg)
        ds-without-nil-vals (mapv #(persistent! (reduce-kv
                                                 (fn [m k v] (if (some? v) (conj! m [k v]) m))
                                                 (transient {}) %))
                                  (tc/rows ds :as-maps))]
    (write-schema conn cols-cfg ds)
    (d/transact conn ds-without-nil-vals)))
