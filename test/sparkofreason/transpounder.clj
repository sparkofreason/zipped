(ns sparkofreason.transpounder
  (:require [clojure.spec.alpha :as s]
            [clojure.pprint :as pp]
            [expound.alpha :as expound]))

(def ^:dynamic *test-mode* :throw)

(defmacro check!
  [spec v]
  `(let [v# ~v]
     (condp = *test-mode*
       :eval v#
       :explain (if-not (s/valid? ~spec v#)
                  (do
                    (pp/pprint ~(meta &form))
                    (expound/expound ~spec v#))
                  v#)
       :throw (if-not (s/valid? ~spec v#)
                (let [ed# (s/explain-data ~spec v#)]
                  (expound/expound ~spec v#)
                  (throw (ex-info "Assertion failed" ed#)))))))