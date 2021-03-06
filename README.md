mysql:
```fish
# install mysql
sudo apt install mysql-server mysql-client

# setup/change mysql root password
sudo service mysql stop
sudo killall mysqld_safe
sudo killall mysqld
sudo mkdir /var/run/mysqld; sudo chown mysql /var/run/mysqld
sudo mysqld_safe --skip-grant-tables &
mysql -u root
update mysql.user
    set authentication_string=PASSWORD("root"), plugin="mysql_native_password"
    where User='root' and Host='localhost';
flush privileges;
quit;
pgrep mysql
sudo kill # <pid1> ... <pidN>
sudo service mysql restart

# download and install test data:
git clone https://github.com/datacharmer/test_db; and \
cd test_db; and \
mysql --host=localhost --user=root --password=root < employees.sql

# test
mysql --host=localhost --user=root --password=root --table < test_employees_sha.sql

mysql --host=localhost --user=root --password=root employees
```
```sql
mysql> select * from employees where emp_no between 10001 and 10002 limit 5;
```

postgres:
```fish
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
```fish
mkdir ~dev; and cd ~/dev
git clone https://github.com/clojure/tools.nrepl.git; and cd tools.nrepl
sudo apt install --yes maven
mvn package
lein repl
lein garden auto
```

run ufo:
```fish
lein repl

# activate the dev profile in addition to the default profiles
lein with-profiles +dev repl

# activate only the dev profile
# lein with-profiles dev repl
```

then connect to REPL and run (in emacs M-x my/cider-figwheel-repl):
```clojure
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

Copyright © 2016, 2017, 2017, 2019, 2020

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
