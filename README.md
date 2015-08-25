## Availability
[![Build Status](https://travis-ci.org/candrews/availability.svg?branch=master)](https://travis-ci.org/candrews/availability)
[![Dependency Status](https://david-dm.org/candrews/availability.svg)](https://david-dm.org/candrews/availability)

Availability shows availability for a given person or room. It indicates current availability, availability at a specific date/time, and shows a calendar view.

## Building from Source
Availability uses a Maven build system. 

### Prerequisites

Git, JDK 8, and Maven.

Be sure that your `JAVA_HOME` environment variable points to the `jdk1.8.0` folder
extracted from the JDK download.

### Check out sources
`git clone git@github.com:candrews/availability.git`

### Compile and test
`mvn package`

### Run the project
```shell
export EXCHANGE_CREDENTIALS_PASSWORD=<PASSWORD>
export EXCHANGE_CREDENTIALS_USERNAME=<USERNAME>
export EXCHANGE_URI=<URI> # usually ends in /ews/Exchange.asmx
mvn spring-boot:run -Dspring.profiles.active=development`
```
Use your browser to hit http://localhost:8080/
Some interesting URLs include:
* http://localhost:8080/user/`email`/availability to view the user with the given email address's availability
* http://localhost:8080/user/`email`/availability.json?start=&end= to get a JSON representation of the user with the given email address's availability. `start` and `end` must be provided in [ISO combined date time format](https://en.wikipedia.org/wiki/ISO_8601#Combined_date_and_time_representations) with the 'Z' timezone.
* http://localhost:8080/user/`email`/availability/redirect to view the user with the given email address's availability as an image (will return a different image depending on if the user is currently availability, busy, or tentative). This endpoint also accepts 3 optional parameters:
 * date: [ISO combined date time format](https://en.wikipedia.org/wiki/ISO_8601#Combined_date_and_time_representations) with the 'Z' timezone of the date to use when determining availability
 * free: URL to redirect to if the user is free
 * busy: URL to redirect to if the user is busy
 * tentative: URL to redirect to if the user is tentative


## Import into IDE
This project uses [Lombok](https://projectlombok.org/) so special instructions have to be followed when using most IDE.
Make sure that the environment variables EXCHANGE_CREDENTIALS_PASSWORD, EXCHANGE_CREDENTIALS_USERNAME, and EXCHANGE_URI are set appropriately and the JVM argument spring.profiles.active=development is provided when launching the spring-boot:run goal.
