(ns processing
  (:require [cheshire.core :as json]
            [clojure.data.csv :as csv]
            [clojure.java.io :as io]))

(defonce prices-csv
  (with-open [reader (io/reader (io/file "scripts" "prices.csv"))]
    (doall
     (csv/read-csv reader))))

(defn process-csv [csv]
  (let [[[header-row] body] (split-at 1 csv)
        header-keys (map keyword header-row)]
    (map #(zipmap header-keys %) body)))


(defn parse-int [x]
  (Integer/parseInt x))

(defn ->json [csv]
  (->> csv
       process-csv
       (filter :price-ea)
       (map #(-> %
                 (update :price-ea parse-double)
                 (update :width parse-int)
                 (update :depth parse-int)
                 (update :basis parse-int)
                 ))))

(defn- ->keyed-prices
  [{:keys [type basis width depth price-ea]}]
  [[(format "%s-%s-%s-%s" type basis width depth) price-ea]
   [(format "%s-%s-%s-%s" type basis depth width) price-ea]])

(defn- ->maybe-upsized
  [{:as row :keys [basis width depth]}]
  (when (and (zero? (mod width 2))
             (zero? (mod depth 2)))
    (-> row
        (update :basis * 2)
        (update :width quot 2)
        (update :depth quot 2))))

(defn ->tree
  [coll]
  (->> coll
       (mapcat
        (fn [row]
          (concat (->keyed-prices row)
                  (when-let [upsized-row (->maybe-upsized row)]
                    (->keyed-prices upsized-row)))))
       (into (sorted-map))))

(defn process-prices [csv]
  (->> csv
       ->json
       ->tree))

(defn generate-price-map! [csv]
  (spit "./price-map.json" (cheshire.core/generate-string (process-prices csv) {:pretty true})))
