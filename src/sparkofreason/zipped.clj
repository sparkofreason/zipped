(ns sparkofreason.zipped
  (:require [clojure.string :as string]
            [rewrite-cljc.zip :as z]
            [rewrite-cljc.node :as n]
            [rewrite-cljc.parser :as p]
            [rewrite-cljc.reader :as r])
  (:gen-class))

(defrecord IncompleteNode [message string-value])
(defrecord IncompleteDerefNode [message string-value])
(defrecord IncompleteKeywordNode [message string-value])

(def incomplete? #{IncompleteNode
                   IncompleteDerefNode
                   IncompleteKeywordNode})

(defrecord EditPointer [loc edit-fn])
(defrecord Edit [edit-ptr node])

(def empty-expr #sparkofreason.zipped.IncompleteNode["", ""])
(def empty-zipper (z/of-string ""))

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
        (p/parse-string string-value')
        (catch clojure.lang.ExceptionInfo ex
          (let [message (.getMessage ^Throwable ex)
                error-type (error-message->type message)]
            (case error-type
              :eof (if-let [match (enclosure-matches k)]
                     (handle-keypress (->IncompleteNode message string-value') match)
                     (->IncompleteNode message string-value'))
              :keyword (->IncompleteKeywordNode message string-value')
              :deref (->IncompleteDerefNode message string-value')
              node)))))))

(defmethod handle-keypress IncompleteKeywordNode
  [node k]
  (let [{:keys [string-value]} node]
    (try
      (p/parse-string (str string-value k))
      (catch clojure.lang.ExceptionInfo ex
        node))))

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
      (try
        (p/parse-string (str string-value k))
        (catch clojure.lang.ExceptionInfo ex
          node)))))

(defmethod handle-keypress rewrite_cljc.node.whitespace.WhitespaceNode
  [node k]
  (cond
    (r/whitespace? k)
    (p/parse-string (str (:string-value node) k))
    
    :else
    (handle-keypress empty-expr k)))

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

(defmulti init-edit
  (fn [loc expr] (type (first loc))))

(defmethod init-edit rewrite_cljc.node.forms.FormsNode
  [loc _]
  (->Edit (->EditPointer loc z/append-child) empty-expr))

(defmethod init-edit rewrite_cljc.node.seq.SeqNode
  [loc _]
  (->Edit (->EditPointer loc z/append-child) empty-expr))

(defmethod init-edit rewrite_cljc.node.token.TokenNode
  [loc expr]
  (->Edit (->EditPointer loc z/replace) expr))

(defmethod init-edit rewrite_cljc.node.whitespace.WhitespaceNode
  [loc expr]
  (->Edit (->EditPointer loc z/insert-right) empty-expr))

;;; Need multimethod to determine the expr for Edit, e.g. SeqNode should be empty-expr, TokenNode expr

(defn update-node
  [edit k]
  (let [{:keys [edit-ptr node]} edit]
    (handle-keypress node k)))

(defn edit-loc
  [edit k]
  (let [{:keys [edit-ptr node]} edit
        node' (update-node edit k)]
    (cond 
      (incomplete? (type node'))
      (assoc edit :node node')

      (= rewrite_cljc.node.whitespace.WhitespaceNode (type node'))
      (->Edit (assoc edit-ptr :edit-fn z/insert-right) empty-expr)
      
      :else
      (let [{:keys [loc edit-fn]} edit-ptr
            edit-fn' (if (and (= edit-fn z/replace) (not= (type node) (type node')))  
                       z/insert-right
                       edit-fn)
            loc' (edit-fn' loc node')]
        (condp = edit-fn'
          z/append-child (-> loc' z/down z/rightmost (init-edit node'))
          z/replace (-> loc' (init-edit node'))
          z/insert-right (-> loc' (#(or (z/right %) %)) (init-edit node')))))))

(comment

  (r/whitespace-or-boundary? \/)
  (r/linebreak? \newline)

  (p/parse-string ".")
  (p/parse-string "@")
  (p/parse-string "#(")
  (p/parse-string "(")
  (pr-str (p/parse-string "foo"))
  (prn (p/parse-string "foo"))

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

  (z/insert-right (z/of-string "foo") (p/parse-string "x"))
  (z/sexpr (z/of-string "#inst \"2020-01-01\""))
  (z/find-next-value (-> "(foo #inst \"2020-01-01\")" z/of-string z/down) '(read-string "#inst \"2020-01-01\""))

  (-> (z/of-string "#_(foo)") z/print-root)
  (-> (z/of-string "(foo bar) (bar foo)"))
  (-> (z/of-string "#:foo/bar{:doink :norb}") z/sexpr)
  (p/parse-string "::z/a")
  (p/parse-string "#:foo.bar{:doink :norb}")
  (-> (z/of-string "#:foo{:doink 1}")
      z/down
      z/right
      (z/append-child (n/keyword-node :bar))
      (z/append-child (n/value 2))
      (z/print-root))
  (p/parse-string "{:foo 1 :bar}")
  (println :a/b)
  (identity {:foo :bar}))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World!"))

(comment
)