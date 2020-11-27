(ns sparkofreason.zipped
  (:require [clojure.string :as string]
            [rewrite-cljc.zip :as z]
            [rewrite-cljc.node :as n]
            [rewrite-cljc.parser :as p]
            [rewrite-cljc.reader :as r])
  (:gen-class))

(clojure.pprint/pprint (p/parse-string "@a"))
(defrecord IncompleteNode [message string-value])
(defrecord IncompleteDerefNode [message string-value])
(defrecord IncompleteKeywordNode [message string-value])

(def empty-expr #sparkofreason.zipped.IncompleteNode["", ""])

(def enclosure-matches
  {\" \"
   \( \)
   \[ \]
   \{ \}})

(defn error-message->type
  [msg]
  (cond
    (re-find #".*EOF.*" msg) :eof
    (re-find #".*Invalid keyword.*" msg) :keyword
    (re-find #".*:deref.*" msg) :deref
    :else :unknown))

(defmulti handle-keypress
  (fn [node k] (type node)))

(defmethod handle-keypress IncompleteNode
  [node k]
  (let [string-value (:string-value node)]
    (let [string-value' (str string-value k)]
      (try
        #trace (p/parse-string string-value')
        (catch clojure.lang.ExceptionInfo ex
          (let [message (.getMessage ^Throwable ex)
                error-type (error-message->type message)]
            (println string-value' error-type message)
            (case error-type
              :eof (if-let [match (enclosure-matches k)]
                     (handle-keypress (->IncompleteNode message string-value') match)
                     (->IncompleteNode message string-value'))
              :keyword (->IncompleteKeywordNode message string-value')
              :deref (->IncompleteDerefNode message string-value')
              node)))))))

(defmethod handle-keypress IncompleteDerefNode
  [node k]
  (let [{:keys [string-value]} node]
    (try
      (p/parse-string (str string-value k))
      (catch clojure.lang.ExceptionInfo ex
        node))))

(defmethod handle-keypress rewrite_cljc.node.token.TokenNode
  [node k]
  (cond 
    (r/whitespace-or-boundary? k)
    (handle-keypress empty-expr k)

    :else
    (let [string-value (:string-value node)]
      (println "tokenish" string-value)
      (try
        (p/parse-string (str string-value k))
        (catch clojure.lang.ExceptionInfo ex
          node)))))

(defmethod handle-keypress rewrite_cljc.node.keyword.KeywordNode
  [node k]
  (let [{keyword :k namespaced? :namespaced?} node
        string-value (str (:k node))
        has-namespace? (string/index-of string-value \/)]
    (cond 
      (r/whitespace-or-boundary? k)
      (handle-keypress empty-expr k)
      
      (= \/ k)
      (if (or namespaced? has-namespace?)
        node
        (handle-keypress (->IncompleteNode "" string-value) k))
      
      :else
      (try
        (p/parse-string (str string-value k))
        (catch clojure.lang.ExceptionInfo ex
          node)))))

(comment 
 
 (r/whitespace-or-boundary? \/)
 
 (p/parse-string "@")
 (p/parse-string "#(")
 (p/parse-string "(")
 (p/parse-string "foo")
 
 (def empty (z/of-string ""))
 (-> empty
   (z/insert-child (n/list-node []))
   z/down
   (z/insert-child (n/token-node 'foo)))
 
 (-> empty
   (z/insert-child (n/list-node []))
   z/down
   (z/insert-child (n/token-node 'foo))
   (z/append-child (n/token-node 'baz))
   z/down
   (z/replace (n/token-node 'bar))
   z/print)
 
 (with-out-str
   (-> (z/of-string "#_(foo)") z/print-root))
 
 (z/of-string "#f")
 (-> (z/of-string "#_(foo)") z/print-root)
 (-> (z/of-string "(foo bar)" ) )
 (-> (z/of-string "#:foo/bar{:doink :norb}"))
 (p/parse-string "::z/a")
 (println :a/b)
 )

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World!"))

(comment
  (require '[vlaaad.reveal :as reveal])
  (add-tap (reveal/ui))
)