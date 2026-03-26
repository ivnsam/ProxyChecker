# ProxyChecker

My small project for learning Java.

It can check is proxy host or hosts from list accessible by trying to create connection through proxy and getting country of this proxy.

## Compile

```sh
javac src/* -d out
```

## Run

- Print help

```sh
java cp out App --help
```

- Check single proxy with defaults

```sh
java -cp out App http://185.241.5.57:3128
```

- Check proxies from list (Be careful, by default **ALL** proxies will be checked)

```sh
java -cp out App https://cdn.jsdelivr.net/gh/proxifly/free-proxy-list@main/proxies/protocols/http/data.txt
```
