HTTPClientApplication

## Students

- William-Andrew Lussier (40033412)
- Anthony Le (40001837)


# UDP Example in Java

## Requirement
1. [Oracle JDK 8](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html)
2. [Apache Maven](https://maven.apache.org/) 

## Usage

1. Compile and package jar 
   mvn package

2. Run the router (see router's README)

3. Run the echo server
   java -cp .:target/udp-http-1.0-SNAPSHOT-jar-with-dependencies.jar ca.UDPServer --port 8007

4. Run the echo client
   java -cp .:target/udp-http-1.0-SNAPSHOT-jar-with-dependencies.jar ca.UDPClient \
   --router-host localhost \
   --router-port 3000 \
   --server-host localhost \
   --server-port 8007 \


## Tested Commands

#### HELP

`help`

`help post`

`help get`

#### GET
`
GET -v  http://httpbin.org/get
`

`
GET -v  http://httpbin.org/absolute-redirect/3 -o /home/wlussier/Projects/HTTPClientApplication/sampleFiles/output.txt 
`

`
GET -v  http://httpbin.org/absolute-redirect/3
`

`
GET http://httpbin.org/get?course=networking&assignment=1 
`

`
GET -h "User-Agent: Hello" http://httpbin.org/status/418
`

`
GET -h "User-Agent: Hello" http://httpbin.org/status/418 -o /home/wlussier/Projects/HTTPClientApplication/sampleFiles/teapot.txt 
`

#### POST
`
POST -h Content-Type:application/json -d '{"Assignment": 1}' http://httpbin.org/post
`

`
POST -h Content-Type:application/json -d '{"Assignment": {"Page": 2, "Paragraph": 2}}' http://httpbin.org/post
`

`
POST -f /home/wlussier/Projects/HTTPClientApplication/sampleFiles/example.txt http://httpbin.org/post
`