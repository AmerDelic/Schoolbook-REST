# Schoolbook-REST
A Spring study project.


** Database Setup ** 

1) Install docker
2) Get MySql image for docker: "sudo docker pull mysql"
3) Create a container: "sudo docker run --name mysql-test-server -p 3306:3306 -e "MYSQL_ROOT_PASSWORD=root" -d mysql"

You might want to access your DB via terminal using the following commands:
1) "sudo docker exec -it mysql-test-server bash"
2) "mysql -uroot -p"

You can check the IP address of a specific running container like so:
sudo docker inspect -f '{{range.NetworkSettings.Networks}}{{.IPAddress}}{{end}}' mysql-test-server
