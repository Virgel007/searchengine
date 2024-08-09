# The search engine

The program searches by words on websites.
The site is indexed and all pages of the site are saved to the database.
Search management is carried out through the browser.

![1-1](https://github.com/user-attachments/assets/f7b0a493-75ab-4786-887c-52abbb6e9c62)


The stack of technologies used: Java 17, MySQL, Spring, Maven.

Instructions for the local launch of the project — a sequence of commands and actions
1. Install MySQL and IDEA on your PC.
2. Add the project to IDEA (instructions for adding it can be found on the Internet)
3. Attention is important to work correctly in MySQL, use Create BD.sql script will create correct tables in a clean database.
4. In the application.yaml in the username and password fields, specify your username and password from your database.(MySQL port 3306 should be free or change the urk field to your port)
5. In the application.yaml specify the names of the sites and their address(URL)

![1-2](https://github.com/user-attachments/assets/2b8e6346-1545-4a53-b313-4e3404e994d5)


6. Update the dependencies in Maven
7. Launch the application and open your browser in the view (insert) line (http://localhost:8080 /) without brackets.
   You should see the Search Engine.

![1-3](https://github.com/user-attachments/assets/84d3e8f0-343f-430a-97c9-f2704074350a)

The status Failed means that there is no data on this site in the database.

In the Management tab, indexing of sites or pages belonging to the site is started.

![1-4](https://github.com/user-attachments/assets/4917cfa2-2aba-42ed-be56-b6cefd60be27)



Indicates an indexing error or the database is empty.

![2-1](https://github.com/user-attachments/assets/84ecfe68-d525-4926-860f-983b4517ab43)



Indicates that indexing is in progress, site crawling and saving is being performed.

![2-2](https://github.com/user-attachments/assets/91f7e557-82fe-4e79-8384-33c70e9d7630)


Indicates that indexing is complete.

![2-3](https://github.com/user-attachments/assets/d3c4bece-4ce5-4b32-8cb6-3e0a44f08974)



Use Russian in the search.

![1-5](https://github.com/user-attachments/assets/26ab1e4f-f8fb-48d8-9afb-9957913a0d8e)


The program works in multithreaded mode and uses all processor cores to quickly crawl the site through pages. A Russian-language lemotizer is used. To drop data from the database, use the SQL script. The scripts are located in the resource folder.

The final project in the course is a Java programmer from Skill Box.
The project was completed by Arkady Kruglov.
Contacts: arkadiy_88@bk.ru
Telegram: https://t.me/ArkadiyKruglov
Tel: +7 960 307 3132
