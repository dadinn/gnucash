(ns com.besenczy.gnucash.specs.common
  (:require
   [com.besenczy.gnucash.specs.commodity :as cmdty]
   [clojure.edn :as edn]
   [clojure.string :as string]
   [java-time :as jt]
   [clojure.spec.gen.alpha :as gen]
   [clojure.spec.alpha :as spec])
  (:import
   [java.util UUID]))

(defn parse-guid [s]
  (->> (list 8 4 4 4 12)
    (reduce
      (fn [[slices acc] n]
        [(seq (drop n slices))
         (let [part (take n slices)
               part (apply str part)]
           (conj acc part))])
      [(seq s) []])
    second
    (string/join "-")
    (UUID/fromString)))

(spec/def ::guid
  (spec/with-gen
    (spec/and string?
      (partial re-matches #"[0-9a-f]{32}")
      (spec/conformer
        parse-guid
        (fn [id] (.replace (str id) "-" ""))))
    #(gen/fmap
       (fn [uuid] (-> uuid str (.replace "-" "")))
       (gen/uuid))))

(spec/def ::date
  (spec/and string?
    (partial re-matches #"[0-9]{4}-[0-9]{2}-[0-9]{2}")
    (let [fmt (jt/formatter "yyyy-MM-dd")]
      (spec/conformer
        (fn [s]
          (jt/local-date fmt s))
        (fn [dt]
          (jt/format fmt dt))))))

(spec/def ::datetime
  (spec/and string?
    (partial re-matches #"[0-9]{4}-[0-9]{2}-[0-9]{2} [0-9]{2}:[0-9]{2}:[0-9]{2}")
    (let [fmt (jt/formatter "yyyy-MM-dd HH:mm:ss")]
      (spec/conformer
        (fn [s]
          (jt/local-date-time format s))
        (fn [dt]
          (jt/format fmt dt))))))

(spec/def ::number
  (spec/and
    string?
    (spec/or
      :int (partial re-matches #"[0-9]+")
      :ratio (partial re-matches #"-?[0-9]+/[0-9]+"))
    (spec/conformer
      (fn [[t s]] (edn/read-string s))
      (fn [s] (println s)))))

(spec/def ::commodity
  (spec/keys
    :req-un
    [::cmdty/id
     ::cmdty/space]
    :opt-un
    [::cmdty/name
     ::cmdty/xcode
     ::cmdty/fraction
     ::cmdty/get-quotes
     ::cmdty/quote-source
     ::cmdty/quote-tz]))
