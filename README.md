
WebApp:
```fish
$ rlwrap lein figwheel
```

or
```fish
$ rlwrap lein repl
# TODO write down the command
```

Test mysql
```mysql
mysql --host=localhost --user=root --password=root employees
select * from employees where emp_no between 10001 and 10002 limit 5;
```
## License

Copyright © 2015 FIXME

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
