HTTPClientApplication

## Students

- William-Andrew Lussier (40033412)
- Anthony Le (40001837)

# How I run it (William)
1. **Maven Package**
---
Httpfs
1. mvn package (I package using an Intellij plugin)
2. cd target
3. `java -jar Httpfs.jar -v -d /home/wlussier/Projects/UDP-Http/sampleFiles`

    * Path directory: `-d $home/UDP-Http/sampleFiles`
    * Verbose: `-v`

---
Server
1. cd target
2. `java -jar UDPServer.jar --port 8007`

    * Port: `--port #`
---

Router (terminal)
1. `go build -o router`
2. `./router`


Client (Intellji)
1. cd target
2. `java -jar Httpc.jar {GET/POST Arguments}`

  Example: `java -jar Httpc.jar GET -v  http://httpbin.org/absolute-redirect/3`

## Tested Commands

  * Update paths with respect to your project location path-to-project/UDP-Http

HTTPC
---

#### HELP

`help`

`help post`

`help get`

#### GET
`GET -v  http://httpbin.org/get`

`GET -v  http://httpbin.org/absolute-redirect/3 -o /home/wlussier/Projects/UDP-Http/sampleFiles/output.txt `

`GET -v  http://httpbin.org/absolute-redirect/3`

`GET http://httpbin.org/get?course=networking&assignment=1 `

`GET -h "User-Agent: Hello" http://httpbin.org/status/418`

`GET -h "User-Agent: Hello" http://httpbin.org/status/418 -o /home/wlussier/Projects/UDP-Http/sampleFiles/teapot.txt `

#### POST
`POST -h Content-Type:application/json -d '{"Assignment": 1}' http://httpbin.org/post`

`POST -h Content-Type:application/json -d '{"Assignment": {"Page": 2, "Paragraph": 2}}' http://httpbin.org/post`

`POST -f /home/wlussier/Projects/UDP-Http/sampleFiles/example.txt http://httpbin.org/post`

HTTFS
---

`Get http://localhost:8080/`

`Get -v http://localhost:8080/`

`Get -v http://localhost:8080/teapot.txt`

`Post -v -d '{"Assignment": 1}' http://localhost:8080/output.txt`

`Post -v -f /home/wlussier/Projects/UDP-Http/sampleFiles/teapot.txt http://localhost:8080/output.txt`
