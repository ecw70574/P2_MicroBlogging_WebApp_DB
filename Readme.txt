Group Members and their contributions:

Ella Wileman - Commenting on posts functionality, ER diagram, follower functionality, bookmark functionality, trending page
Rebekah McLatcher - Bookmark functionality, liking posts functionality, ER diagram, hashtag search, Readme
Mariah Jaeck - trending page, creating posts, posts of people the user follows appears on home page
Amy Huang - Liking posts functionality, commenting on Posts functionality, hashtag search, Demo Video, assistance with debugging all functionalities 
Priya Sampat - follower functionality, comments and debugging 


New Feature Implemented: 
Description: The app contains a 'Trending Page' which displays the 10 most popular posts made by any users in descending order based on a combined score of like count, comment count, and bookmarks. Posts with the highest score appear at the top of the page. 

UI Access: 'Trending Page' on the top navigation bar. 

Files and Locations: 

1. Service - TrendingService.java
- path: P2_MicroBlogging_WebApp_DB/p2_starter_code/src/main/java/uga/menik/csx370/services/TrendingService.java

2. Controller - TrendingController.java
- path: P2_MicroBlogging_WebApp_DB/p2_starter_code/src/main/java/uga/menik/csx370/controllers/TrendingController.java

3. UI - trending_page.mustache
- path: P2_MicroBlogging_WebApp_DB/p2_starter_code/src/main/resources/templates/trending_page.mustache 


Instructions on Running the project: 
1. Start the docker container
2. Navigate to the folder that has the pom.xl file 
3. Run the command if unix: mvn spring-boot:run -Dspring-boot.run.jvmArguments='-Dserver.port=8081'
   Run command if windows: mvn spring-boot:run -D"spring-boot.run.arguments=--server.port=8081"
   Run command if power shell: mvn spring-boot:run --% -Dspring-boot.run.arguments="--server.port=8081"
4. Navigate to the following URL in a chrome browsers: http://localhost:8081/
5. Create an account and login to the application






