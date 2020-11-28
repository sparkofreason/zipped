(ns sparkofreason.zipped-test
  (:require [clojure.test :refer :all]
            [sparkofreason.zipped :refer :all]))

(comment
 (require '[flow-storm.api :as fsa])
 (fsa/connect)
 
 (require '[vlaaad.reveal :as reveal])
 (add-tap (reveal/ui))
 )

(def reader-dispatch (handle-keypress empty-expr "#"))

(edit-loc (init-edit (edit-pointer empty-zipper)) \()

(-> empty-zipper
    init-edit
    (edit-loc \()
    (edit-loc \a)
    (edit-loc \b))

(handle-keypress empty-expr \()
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
(handle-keypress reader-dispatch \()
(handle-keypress reader-dispatch \[)
(handle-keypress reader-dispatch \{)
(handle-keypress reader-dispatch \")
