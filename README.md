# Globalize
 A fullstack hotel booking project
 Language: Java, html, css, js, MySQL, Dart
 Framework: Spring boot, Nextjs, Flutter
# How to run
# Backend
 Compile the project first if there is no target folder <br /><br />
 <code>mvn compile</code>

 Run the jar file in target folder<br /><br />
 <code>java -jar target/booking_platform-0.0.1-SNAPSHOT.jar</code>

# Frontend
 Install dependency<br /><br />
 <code>npm install</code>

 Build the project<br /><br />
 <code>npm run build</code>

 Backend only work when there is a MySQL working. Check the application.properties file and replace database config (and other configs)
 ![image](https://github.com/user-attachments/assets/c2f264f8-1ade-4bcf-a47b-5d9ebc53b767)<br />
 Create a database name 'booking' or anything you put on config file<br />
 Seed the database with these 2 files<br />
 ![image](https://github.com/user-attachments/assets/81ba5880-e0b3-4c89-9822-8cd32cc32ac9)<br />

 Run<br /><br />
 <code>npm run start</code>

 or run it in dev mode<br /><br />
 <code>npm run dev</code>

 # Mobile app
 Mobile app need to run in a virtual machine or an android phone in dev mode

 # Docker
 Use the docker compose to make an multi-container by using compose.yaml file<br /><br />
 <code>docker compose up</code>

 # Test account
 There are some seed account for testing purpose
 - Guest:<br />
   julia_roberts<br />
   alice_johnson<br />
   bob_smith<br />
   charlie_davis<br />
   diana_miller<br />
   evan_wilson<br />
   fiona_brown<br />
   george_clark<br />
   hannah_lewis<br />
   ian_walker<br />
   Password for all: pass123<br />
 - Owner:<br />
   barbara_jackson<br />
   john_doe<br />
   jane_doe<br />
   mike_smith<br />
   sarah_brown<br />
   james_wilson<br />
   linda_martinez<br />
   robert_garcia<br />
   patricia_anderson<br />
   david_thomas<br />
   Password for all: password123<br />
