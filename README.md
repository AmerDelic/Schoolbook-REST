# Schoolbook-REST
A Spring study project.


**Database Setup** 

1) Install docker
2) Get MySql image for docker: "sudo docker pull mysql"
3) Create a container: "sudo docker run --name mysql-test-server -p 3306:3306 -e "MYSQL_ROOT_PASSWORD=root" -d mysql"
4) Run the container: "sudo docker start mysql-server".
If it complains that something is already running at that port, do: "sudo service mysql stop", then try again.

You might want to access your DB via terminal using the following commands:
1) "sudo docker exec -it mysql-test-server bash"
2) "mysql -uroot -p"

You can check the IP address of a specific running container like so:
sudo docker inspect -f '{{range.NetworkSettings.Networks}}{{.IPAddress}}{{end}}' mysql-test-server

**Application Setup**

In "...resources/application.properties" you need to set the following variables:
* spring.datasource.url=${DB_URL}
* spring.datasource.username=${DB_USERNAME} 
* spring.datasource.password=${DB_PASSWORD}
* jwt.provider.secret==${JWT_PROV_SEC}

You could hardcode these, for example, replace '${DB_URL}' with 'jdbc:mysql://172.17.0.2/schoolbook', but what you 
should do is create these three environment variables in the run configuration,
so that 'DB_URL=jdbc:mysql://172.17.0.2/schoolbook'.

**Creating a user** 

* Endpoint: POST @ http://localhost:8081/sb/user/new
* Request: 
* {
  "username": "john4",
  "password": "psw",
  "firstName": "John",
  "lastName": "Elis",
  "email": "john4@mail.com"
  }

**Logging in** 

* Endpoint: POST @ http://localhost:8081/sb/user/login
* Request:
* {
  "username": "john4",
  "password": "psw"
  }

The response to a successful login request will contain a header 'Jwt-Token'. The content of this header
needs to be taken and put in a 'Authorization' header and prefixed with 'Bearer ' for each subsequent request.
The only other endpoint, at this time, is the 'get all users' endpoint: GET @ http://localhost:8081/sb/user.
