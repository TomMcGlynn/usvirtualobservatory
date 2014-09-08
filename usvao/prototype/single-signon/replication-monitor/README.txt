Database Replication Monitoring

Goal: Detect and report database replication failures.

Design:
  1. Make a change on one peer
  2. Detect whether it is propagated to the other peer
  3. Report any failures (by email)
  4. Status is visible as a webpage
  4. Scheduling a check is handled externally -- for example, by cron

Configuration & Installation:
  * replicmon.properties
  * build.properties
  * ant deploy

Dependencies & Limitations:
  * currently only works for MySQL, but the architecture is basically neutral
  * requires a Java web application container (Tomcat etc)