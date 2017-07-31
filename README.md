mysql:
```
git clone https://github.com/datacharmer/test_db; and cd test_db

# install:
mysql --host=localhost --user=root --password=root < employees.sql

# test
mysql --host=localhost --user=root --password=root --table < test_employees_sha.sql

mysql --host=localhost --user=root --password=root employees
select * from employees where emp_no between 10001 and 10002 limit 5;
```

postgres:
```
sudo apt install postgresql postgresql-contrib
sudo -i -u postgres
psql
```

clj-time-ext:
```
mkdir ~/dev; and cd ~/dev
git clone https://github.com/Bost/clj-time-ext.git; and cd clj-time-ext
lein install
```

tools.nrepl:
```
mkdir ~dev; and cd ~/dev
git clone https://github.com/clojure/tools.nrepl.git; and cd tools.nrepl
sudo apt install --yes maven
mvn package
lein repl
lein garden auto
```

run ufo:
```
lein repl
```

then connect to REPL and run (in emacs M-x my/cider-figwheel-repl):
```
user> (require 'figwheel-sidecar.repl-api)
;; start-figwheel can be repeatedly called (is idempotent)
(figwheel-sidecar.repl-api/start-figwheel!)
(figwheel-sidecar.repl-api/cljs-repl)
```

sql commands in emacs:
```sql
(setq sql-user "root")
(setq sql-password "root")
```
then see emacs menu

## License

Copyright © 2016

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
