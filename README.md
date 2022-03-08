# Project Title

Vidispine worktest by Gustav Kavtaradze
Task is to with the help of a Mandelbrot set generate a picture.


## Description

Project consist of two moduals one client and a server.
Client works as a commandline application. By the user with the help of a Java IDE input the arguments.

Arguments looks as follows
```
min_c_re min_c_im max_c_re max_c_im max_n x y divisions list-of-servers
```
Where the list-of-server is a list with space in between, and looks like example: localhost:8080 127.0.0.1:8081

## Getting Started

### Dependencies

* JDK (to run project in IDE)
* Java IDE (IntelliJ of any other modern Java IDE)

### Installing

* Clone project
* Open project in IDE and build

### Executing program

#### Server
* In IDE open run options for PoorManParallelismServerApplication 
* Add VM options add what port to run on with command
```
-Dserver.port=portnr
```
* Then run instances of the server application

#### Client
* In IDE open run options and enter arguments as list above.
* Run projekt
* When image generation is done a image comabine.png will be created in projekt folder. 


## Authors

Gustav Kavtaradze [@meckan]

## Acknowledgments

Inspiration, code snippets, etc.

The calculations needed to be made from [@joni]
* https://github.com/joni/fractals/blob/master/mandelbrot/MandelbrotColor.java
