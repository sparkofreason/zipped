(ns sparkofreason.run-tests
  (:require [cognitect.transcriptor :as xr]))

#_(doseq [f (xr/repl-files "test/sparkofreason")]
  (xr/run f))

(xr/run "test/sparkofreason/zipped_test.clj")