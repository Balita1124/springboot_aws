Source : https://github.com/awsdocs/aws-doc-sdk-examples/tree/main/javav2/usecases/creating_first_project

Vous pouvez développer une application Web dynamique que les utilisateurs peuvent utiliser pour soumettre des données à une table Amazon DynamoDB. En plus d'utiliser DynamoDB, cette application Web utilise également Amazon Simple Notification Service (Amazon SNS) et AWS Elastic Beanstalk. Cette application utilise DynamoDbEnhancedClient pour stocker des données dans la table DynamoDB. Une fois la table mise à jour, l'application utilise Amazon SNS pour envoyer un message texte afin d'avertir un utilisateur. Cette application utilise également les API Spring Boot pour créer un modèle, des vues et un contrôleur.

Le client amélioré DynamoDB mappe vos classes Java aux tables DynamoDB. Avec le client amélioré DynamoDB, vous pouvez effectuer les opérations suivantes :

 - Accédez à vos tableaux
 - Effectuer diverses opérations de création, lecture, mise à jour et suppression (CRUD)
 - Exécuter des requêtes

Remarque : Pour plus d'informations sur le client amélioré DynamoDB, consultez : https://docs.aws.amazon.com/sdk-for-java/v2/developer-guide/examples-dynamodb-enhanced.html

The following AWS services are used in this tutorial:

 - Amazon SNS
 - DynamoDB
 - Elastic Beanstalk

Conditions préalables

 - Un compte AWS
 - Un IDE Java (cet exemple utilise IntelliJ)
 - SDK Java 1.8 et Maven

Créer un projet IntelliJ nommé greetings
-----------------------------------------

La première étape consiste à créer un projet IntelliJ.

 + Dans l'IDE IntelliJ, choisissez Fichier, Nouveau, Projet.
 + Dans la boîte de dialogue Nouveau projet, choisissez Maven.
 + Choisissez Suivant.
 + Dans GroupId, saisissez spring-aws.
 + Dans ArtifactId, entrez Greetings.
 + Choisissez Suivant.
 + Choisissez Terminer.

Assurez-vous que le fichier pom.xml ressemble au code XML suivant.

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <groupId>spring-aws</groupId>
    <artifactId>greetings</artifactId>
    <version>1.0-SNAPSHOT</version>
    <packaging>jar</packaging>
    <description>Demo project for Spring Boot</description>
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>2.6.1</version>
        <relativePath/>
    </parent>
    <properties>
        <java.version>1.8</java.version>
    </properties>
    <dependencyManagement>
        <dependencies>
            <dependency>
                <groupId>software.amazon.awssdk</groupId>
                <artifactId>bom</artifactId>
                <version>2.17.136</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
        </dependencies>
    </dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-thymeleaf</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
            <exclusions>
                <exclusion>
                    <groupId>org.junit.vintage</groupId>
                    <artifactId>junit-vintage-engine</artifactId>
                </exclusion>
            </exclusions>
        </dependency>
        <dependency>
            <groupId>software.amazon.awssdk</groupId>
            <artifactId>dynamodb-enhanced</artifactId>
        </dependency>
        <dependency>
            <groupId>software.amazon.awssdk</groupId>
            <artifactId>dynamodb</artifactId>
        </dependency>
        <dependency>
            <groupId>software.amazon.awssdk</groupId>
            <artifactId>sns</artifactId>
        </dependency>
    </dependencies>
    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
                <version>${project.parent.version}</version>
            </plugin>
        </plugins>
    </build>
</project>
```

I - Creation des classes
----------------------------
1 - Classe principale
************************
```java
package com.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class GreetingApplication {

	public static void main(String[] args) {
		SpringApplication.run(GreetingApplication.class, args);
	}
}
```

2 - Controlleur
****************************
```java
package com.example.handlingformsubmission;

	import org.springframework.beans.factory.annotation.Autowired;
	import org.springframework.stereotype.Controller;
	import org.springframework.ui.Model;
	import org.springframework.web.bind.annotation.GetMapping;
	import org.springframework.web.bind.annotation.ModelAttribute;
	import org.springframework.web.bind.annotation.PostMapping;

	@Controller
	public class GreetingController {

    	@Autowired
    	private DynamoDBEnhanced dde;

    	@Autowired
    	private PublishTextSMS msg;

    	@GetMapping("/")
    	public String greetingForm(Model model) {
          model.addAttribute("greeting", new Greeting());
          return "greeting";
    	}

    	@PostMapping("/greeting")
    	public String greetingSubmit(@ModelAttribute Greeting greeting) {

          // Stores data in an Amazon DynamoDB table.
          dde.injectDynamoItem(greeting);

          // Sends a text notification.
          msg.sendMessage(greeting.getId());

          return "result";
    	}
      }
```
3 - La classe Greeting
**************************
```java
package com.example.handlingformsubmission;

	public class Greeting {

	private String id;
    	private String body;
    	private String name;
    	private String title;

    	public String getTitle() {
        	return this.title;
    	}

    	public void setTitle(String title) {
        	this.title = title;
    	}

    	public String getName() {
        	return this.name;
    	}

    	public void setName(String name) {
        	this.name = name;
    	}

    	public String getId() {
        	return id;
    	}

    	public void setId(String id) {
        	this.id = id;
    	}

    	public String getBody() {
        	return this.body;
    	}

    	public void setBody(String body) {
        	this.body = body;
    	}
       }
```
4 - la classe GreetingItems 
********************************

Cette classe représente contient l'annotation requise pour le client amélioré. Les membres de données de cette classe correspondent aux colonnes de la table DynamoDB Greeting.
```java
package com.example.handlingformsubmission;

  import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
  import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;

  @DynamoDbBean
  public class GreetingItems {

    private String id;
    private String name;
    private String message;
    private String title;

    public GreetingItems()
    {
    }

    public String getId() {
        return this.id;
    }

    @DynamoDbPartitionKey
    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMessage(){
        return this.message;
    }

    public void setMessage(String message){
        this.message = message;
    }

    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
  }
```
5 - La classe DynamoDBEnhanced
********************************
Cette classe utilise l'API DynamoDB qui injecte des données dans une table DynamoDB à l'aide du client amélioré
```java
package com.example.handlingformsubmission;

    import software.amazon.awssdk.auth.credentials.EnvironmentVariableCredentialsProvider;
    import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
    import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
    import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
    import software.amazon.awssdk.enhanced.dynamodb.model.PutItemEnhancedRequest;
    import software.amazon.awssdk.regions.Region;
    import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
    import org.springframework.stereotype.Component;
    import software.amazon.awssdk.services.dynamodb.model.DynamoDbException;

    @Component("DynamoDBEnhanced")
    public class DynamoDBEnhanced {

     public void injectDynamoItem(Greeting item){

        Region region = Region.US_EAST_1;
        DynamoDbClient ddb = DynamoDbClient.builder()
                .region(region)
                .credentialsProvider(EnvironmentVariableCredentialsProvider.create())
                .build();

        try {
            DynamoDbEnhancedClient enhancedClient = DynamoDbEnhancedClient.builder()
                    .dynamoDbClient(ddb)
                    .build();

            DynamoDbTable<GreetingItems> mappedTable = enhancedClient.table("Greeting", TableSchema.fromBean(GreetingItems.class));
            GreetingItems gi = new GreetingItems();
            gi.setName(item.getName());
            gi.setMessage(item.getBody());
            gi.setTitle(item.getTitle());
            gi.setId(item.getId());

            PutItemEnhancedRequest enReq = PutItemEnhancedRequest.builder(GreetingItems.class)
                    .item(gi)
                    .build();

            mappedTable.putItem(enReq);

        } catch (DynamoDbException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
     }
   }
```
6 - La classe PublishTextSMS 
*********************************
```java
package com.example.handlingformsubmission;

   import software.amazon.awssdk.auth.credentials.EnvironmentVariableCredentialsProvider;
   import software.amazon.awssdk.regions.Region;
   import software.amazon.awssdk.services.sns.SnsClient;
   import software.amazon.awssdk.services.sns.model.PublishRequest;
   import software.amazon.awssdk.services.sns.model.SnsException;
   import org.springframework.stereotype.Component;

   @Component("PublishTextSMS")
   public class PublishTextSMS {

    public void sendMessage(String id) {

        Region region = Region.US_EAST_1;
        SnsClient snsClient = SnsClient.builder()
                .region(region)
                .credentialsProvider(EnvironmentVariableCredentialsProvider.create())
                .build();
        
	String message = "A new item with ID value "+ id +" was added to the DynamoDB table";
        String phoneNumber="<Enter a valid mobile number>"; // Replace with a mobile phone number.

        try {
            PublishRequest request = PublishRequest.builder()
                    .message(message)
                    .phoneNumber(phoneNumber)
                    .build();

            snsClient.publish(request);

        } catch (SnsException e) {

            System.err.println(e.awsErrorDetails().errorMessage());
            System.exit(1);
        }
     }
   }
```
II - Creation des fichier HTML
----------------------------
1 - greeting.html
***********************
```html
<!DOCTYPE HTML>
   <html xmlns:th="https://www.thymeleaf.org">
   <head>
     <title>Getting started: Spring Boot and the DynamoDB Enhanced Client</title>
     <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
     <link rel="stylesheet" th:href="|https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/css/bootstrap.min.css|"/>
   </head>
   <body>
    <div class="container">
    <h1>Your first AWS MVC web application</h1>
    <p>You can submit data to an Amazon DynamoDB table by using the DynamoDB Enhanced Client. A mobile notification is sent, alerting a user that a new submission occurred.</p>
    <form action="#" th:action="@{/greeting}" th:object="${greeting}" method="post">
     <div class="form-group">
        <p>Id: <input type="text"  class="form-control" th:field="*{id}" /></p>
        </div>

        <div class="form-group">
            <p>Title: <input type="text" class="form-control" th:field="*{title}" /></p>
        </div>

        <div class="form-group">
            <p>Name: <input type="text" class="form-control" th:field="*{name}" /></p>
        </div>

        <div class="form-group">
            <p>Body: <input type="text" class="form-control" th:field="*{body}"/></p>
        </div>

        <p><input type="submit" value="Submit" /> <input type="reset" value="Reset" /></p>
    </form>
   </div>
   </body>
  </html>
```
2 - result.html
*********************
```html
	<!DOCTYPE HTML>
	<html xmlns:th="https://www.thymeleaf.org">
	<head>
    	 <title>Getting started: Handling form submission</title>
      	  <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
	 </head>
	<body>
	<h1>Result</h1>
	<p th:text="'id: ' + ${greeting.id}" />
	<p th:text="'content: ' + ${greeting.body}" />
	<a href="/">Submit another message</a>
	</body>
	</html>
```

III - Creation des fichiers Jar
-------------------------------------
mvn package

IV - Deployer l'application sur Elastic BeanStalk
----------------------------------------------------
Déployez l'application sur Elastic Beanstalk afin qu'elle soit disponible à partir d'une URL publique. 

Connectez-vous à AWS Management Console, 
puis ouvrez la console Elastic Beanstalk.
Une application est le conteneur de niveau supérieur dans Elastic Beanstalk qui contient un ou plusieurs environnements d'application. (Par exemple, prod, qa, dev, prod-web, prod-worker, qa-web ou qa-worker.)

Déployer l'application Greeting sur Elastic Beanstalk

 + Ouvrez la console Elastic Beanstalk à l'adresse https://console.aws.amazon.com/elasticbeanstalk/home.
 + Choisissez Créer une nouvelle application. Cela ouvre un assistant qui crée votre application et lance un environnement approprié.
 + Dans la boîte de dialogue Créer une nouvelle application, entrez les valeurs suivantes.
	- Nom de l'application - Greeting
	- Description - Une description de l'application

 + Choisissez Créer un maintenant.
 + Choisissez Environnement de serveur Web, puis sélectionnez Sélectionner.
 + Dans Plate-forme préconfigurée, choisissez Java.
 + Dans Télécharger votre code, accédez au fichier .jar que vous avez créé.
 + Choisissez Créer un environnement. Vous verrez l'application en cours de création.

 + Lorsque vous avez terminé, l'application indique que la santé est OK.

 + Pour modifier le port sur lequel Spring Boot écoute, ajoutez une variable d'environnement nommée SERVER_PORT avec la valeur 5000.
 + Ajoutez une variable nommée AWS_ACCESS_KEY_ID, puis spécifiez la valeur de votre clé d'accès.
 + Ajoutez une variable nommée AWS_SECRET_ACCESS_KEY, puis spécifiez la valeur de votre clé secrète.
	Remarque : Pour plus d'informations sur la définition des variables, voir Propriétés d'environnement et autres paramètres logiciels : https://docs.aws.amazon.com/elasticbeanstalk/latest/dg/environments-cfg-softwaresettings.html

 + Une fois les variables configurées, vous verrez l'URL d'accès à l'application.
