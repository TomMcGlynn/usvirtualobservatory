SELECT t.b, b.d FROM Tab t, Tab b GROUP BY t.b, b.d HAVING t.b <> d.b
