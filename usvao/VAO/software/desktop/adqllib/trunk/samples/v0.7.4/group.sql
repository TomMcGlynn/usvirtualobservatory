SELECT g.fieldID, avg(g.modelMag_u) as avg_modelMag_u, avg(g.modelMag_r) as avg_modelMag_r FROM  Galaxy g WHERE g.run = 1458 AND g.rerun = 40 AND g.camcol = 1 GROUP BY g.fieldID ORDER BY g.fieldID
