(ns sparkofreason.keypress-test
  (:require [rewrite-cljc.node :as n]
            [rewrite-cljc.zip :as z]
            [sparkofreason.transpounder :refer [check!]]
            [sparkofreason.zipped :refer :all]))

(handle-keypress empty-expr \[)
(handle-keypress empty-expr \{)
(handle-keypress empty-expr \")
(handle-keypress empty-expr \a)

(handle-keypress empty-expr \:)
(-> empty-expr
    (handle-keypress \:)
    (handle-keypress \a))

(-> empty-expr
    (handle-keypress \:)
    (handle-keypress \a)
    (handle-keypress \/))
(-> empty-expr
    (handle-keypress \:)
    (handle-keypress \a)
    (handle-keypress \/)
    (handle-keypress \b))
(-> empty-expr
    (handle-keypress \:)
    (handle-keypress \a)
    (handle-keypress \/)
    (handle-keypress \b)
    (handle-keypress \/))

(-> empty-expr
    (handle-keypress \a)
    (handle-keypress \b)
    (handle-keypress \())

(-> empty-expr
    (handle-keypress \a)
    (handle-keypress \,))

(-> empty-expr
    (handle-keypress \@)
    (handle-keypress \a))

(-> empty-expr
    (handle-keypress \@)
    (handle-keypress \a))

#trace
 (-> empty-expr
     (handle-keypress \@)
     (handle-keypress \[)
     (handle-keypress \a)
     (.toString))

(handle-keypress empty-expr \])

(def reader-dispatch (handle-keypress empty-expr "#"))
(handle-keypress reader-dispatch \()
(handle-keypress reader-dispatch \[)
(handle-keypress reader-dispatch \{)
(handle-keypress reader-dispatch \")
