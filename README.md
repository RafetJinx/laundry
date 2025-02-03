## Laundry web site backend project


# Attention
#### To start this application, you need the `application.properties` file to be structured as follows:

**application.properties**

```application.properties

spring.main.allow-bean-definition-overriding=true

# springdoc
springdoc.api-docs.enabled=true
springdoc.api-docs.path=/v3/api-docs
springdoc.swagger-ui.enabled=true
springdoc.swagger-ui.path=/swagger-ui
springdoc.swagger-ui.url=/v3/api-docs

spring.application.name=

api.base.path=/api/v1

app.auth.username=
app.auth.password=

app.resetPassword.url=http://localhost:8080/view/auth/reset-password

# server forward
server.forward-headers-strategy=framework

# spring security
spring.security.user.name=admin
spring.security.user.password=admin
spring.security.user.roles=ADMIN

spring.mvc.view.prefix=classpath:/static/
spring.mvc.view.suffix=.html

# Frontend URL
#app.base.url=http://localhost:8080
app.base.url=http://localhost:8080

# Server Configuration
spring.datasource.url=jdbc:mysql://localhost:3306/laundry
spring.datasource.username=
spring.datasource.password=
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQL8Dialect
spring.jpa.database-platform=org.hibernate.dialect.MySQLDialect

jwt.secret=

spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=
spring.mail.password=
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true

app.tcmb.url=https://www.tcmb.gov.tr/kurlar/today.xml
app.company.name=
app.company.slogan=
```

> **Important Note:**  
> Your application.properties file must be located in ***src/main/resources***.
>
>
> You must fill in the following environment variables in your `application.properies` file:
>
> - `spring.datasource.url: yourdatasourceurl`
> - `spring.datasource.username: yourdatasourcename`
> - `spring.datasource.password: yourdatasourcepassword`
> - `mail.host: yourmailhost`
> - `mail.port: yourmailport`
> - `mail.username: yourmailusername`
> - `mail.password: yourmailpassword`
> - `app.tcmb.url: tcmburl`
> - `app.auth.username: youradminaccountusername`
> - `app.auth.password: youradminaccountpassword`

## if you want u can start datasource on docker container
1.  uncomment spring-boot-docker-compose
2.    your `compose.yaml`file must be like this

```yaml
services:
  mysql:
    image: 'mysql:latest'
    environment:
      - 'MYSQL_DATABASE='
      - 'MYSQL_PASSWORD='
      - 'MYSQL_ROOT_PASSWORD='
      - 'MYSQL_USER='
    ports:
      - '3306:3306'
    volumes:
      - /path/to/your/local/folder:/var/lib/mysql
```
> **Important Note:**  
> You must fill in the following environment variables in your `docker-compose.yaml` file:
> Your docker-compose.yaml file must be located in ***startapp/docker-compose.yaml***.
>
> - `MYSQL_DATABASE`
> - `MYSQL_PASSWORD`
> - `MYSQL_ROOT_PASSWORD`
> - `MYSQL_USER`
> - `/path/to/your/local/folder` with which folder you want save



## Easy start for linux server
 ```bash
   sh startapp/startapp.sh
 ```







## What l added 
  
**Version 1.0.0**
 
 - Readme added
 - Easy script added for Easy run app
 - Pom.xml editted now lombok working when build app
