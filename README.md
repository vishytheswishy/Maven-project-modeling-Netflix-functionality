- # DEPLOYMENT AND INSTRUCTIONS

    - #### Video Demo Link: https://www.youtube.com/watch?v=8pmdW6Tm0KA

    - #### Instruction of deployment: 
    to run this program make sure you have tomcat 8.5.53 , mysql 5.7 installed on both your development machine and      
    instance. Afterwards run the following scripts(createtables.sql and movie-data.sql) on your development machine and 
    server instance in order to populate the mysql database.

-- Create a test user


       mysql> CREATE USER 'mytestuser'@'localhost' IDENTIFIED BY 'mypassword'; mysql> GRANT ALL PRIVILEGES ON * . * TO  
       'mytestuser'@'localhost';

-- clone the repository using git clone (URL) into a local repository


       once you have the repository cloned go to your terminal and navigate to your freshly cloned directory and type in the          following commands: mvn package now you should see a target directory with your desired war file.

-- deploy on tomcat manually or through your id

    
        grab the war file and either deploy it manually through the tomcat manager gui or through intellij by creating a new 
        configuration after importing the directory.

        refresh the tomcat manager page. You should see a new project (just deployed): project 1.

        click the project link, which goes to your website's landing page  (Movie List Page).

    - #### Explain how Connection Pooling is utilized in the Fabflix code.
    
    Connection pooling is utilized by our fablflix code to prevent an issue we where facing before in regards to query speed       and servlet time. By using connection pooling, our code reuses the same connections already instantiated in our code. The     context.xml defined as shown.
    
    -- Context
    <!-- Defines a Data Source Connecting to localhost moviedb-->
    Resource name="jdbc/moviedb"
              auth="Container"
              driverClassName="com.mysql.jdbc.Driver"
              type="javax.sql.DataSource"
              username="mytestuser"
              password="mypassword"
              url="jdbc:mysql://localhost:3306/moviedb"
    -- Context
    This demonstrates how read operations are done. After a connection being used in a servlet is done with its operations its 
    returned to the pool for other clients and itself to be reused.
    
    - #### Explain how Connection Pooling works with two backend SQL.
    Connection pooling with two MySQL backends works by using the same instance that the load balancer directs to for read         requests. For example the slave instance will use the slave mysql backend connection pool to make read requests and the  
    master instance will use the master mysql backend connection pool.
    
- # Master/Slave
    
    - #### How read/write requests were routed to Master/Slave SQL?
    Manually entered ip of the master instance for every place a write request would be generated to the backend. I.E   
    ActorInsertServlet.java
    String loginUser = "mytestuser";
    String loginPasswd = "mypassword";
    String serverName = request.getServerName();
    String loginUrl = "jdbc:mysql://18.222.116.172:3306/moviedb";
        
    line 58: Class.forName("com.mysql.jdbc.Driver").newInstance();
    line 59: Connection connection = DriverManager.getConnection(loginUrl, loginUser, loginPasswd);
    
