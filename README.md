HTTPClientApplication

## Students

- William-Andrew Lussier (40033412)
- Anthony Le (40001837)


#TODO

## HTTPC

### Handle the Response
1. Send Response packets to Client (Packet are already separated into packets of the right size)
2. Make sure Client receives all the packets and is able to recreate the Response Object.

### Update the Request Payload
1. Update the code so that if the request object sent as a payload to the Server does not exceed the Max size.
2. Break down the packet like in the Server if the payload is too large.

Once all this is complete, the basic httpc should be functional.

## HTTPFS

### Update Server
1. Update server to handle the request if the user uses the httpfs interface.



# UDP Example in Java

## Requirement
1. [Oracle JDK 8](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html)
2. [Apache Maven](https://maven.apache.org/) 

## Usage

1. Compile and package jar 
   mvn package

2. Run the router (see router's README)

3. Run the echo server
   java -cp .:target/udp-http-1.0-SNAPSHOT-jar-with-dependencies.jar ca.concordia.UDPServer --port 8007

4. Run the echo client
   java -cp .:target/udp-http-1.0-SNAPSHOT-jar-with-dependencies.jar ca.concordia.UDPClient \
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