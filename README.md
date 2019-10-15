
#E-signature application
 
E-signature application - starting point for your electronic document flow with different document templates and e-sign providers. This demo include two template providers(local file and remote location by URL) and plugged-in e-sign document provider([DocuSign](https://developers.docusign.com)).<br/>
Application built on [Spring Boot](https://spring.io/projects/spring-boot) , [Spring MVC](https://docs.spring.io/spring/docs/current/spring-framework-reference/web.html) , [Spring Security](href="https://spring.io/projects/spring-security) , [JPA](https://spring.io/projects/spring-data-jpa) , [Thymeleaf](https://www.thymeleaf.org)([+Bootstrap](https://getbootstrap.com/)).

##How to start:


### Requirements:

- Java 1.8
- Maven 3
- MySQL DB
- DocuSign developer account

### Configuration:

Set `${DATASOURCE_USER}` and `${DATASOURCE_PASSWORD}` environment variables located in `application.properties` file according to your database setup <br/>
Set `${DS_CLIENT_ID}`, `${DS_IMPERSONATED_USER_GUID}`, `${RSA_PRIVATE_KEY}` environment variables located in `dsconfig.properties` from your DocuSign account <br/>
Authorize  your DocuSign application for impersonated signing with "Embedded Signing Ceremony" via [JWT Authentication](https://developers.docusign.com/esign-rest-api/guides/authentication/oauth2-jsonwebtoken)

### Build:

 `mvn clean package`
 
### Run:

`mvn spring-boot:run` 

To login into application use `user@m.com` with password `user` or `admin@m.com` with password `admin`. Then сreate a new document by clicking on the document icon in the middle of the screen. After that you can start signing document process clicking on the sign icon in the action column.
