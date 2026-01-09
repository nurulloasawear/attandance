Attendance Tizimi Loyihasi: To'liq Dokumentatsiya

Muallif: Nurullo Asatullayev & Shaxriyor Raximov

Sana: 09 Yanvar 2026

Versiya: 1.0

Maqsad: Ushbu hujjat attendance tizimi loyihasini to'liq hujjatlashtirish uchun yozilgan. U loyihaning barcha jihatlarini, shu jumladan arxitektura, komponentlar, ishlatilgan kutubxonalar, toollar, Java va Python qismlarini batafsil izohlaydi. Hujjat oddiy til bilan yozilgan, shuning uchun texnik bilimi kam odamlar ham tushunishi mumkin. Hujjat uzun va batafsil bo'lib, loyihani rivojlantirish, o'rnatish va ishlatish uchun qo'llanma sifatida foydalanilishi mumkin.
1. Kirish (Introduction)
   
Attendance tizimi – bu xodimlar yoki talabalar ishtirokini (ishtirokini) kuzatish uchun mo'ljallangan dasturiy ta'minot. Tizim xodimlarning kelish va ketish vaqtlarini qayd etadi, hisobotlar chiqaradi va bildirishnomalar yuboradi. Loyiha mikroservis arxitekturasi asosida qurilgan, ya'ni tizim mustaqil qismlarga bo'lingan, ular bir-biri bilan API orqali bog'lanadi. Bu arxitektura tizimni oson kengaytirish va xatolarni izolyatsiya qilish imkonini beradi.


Asosiy Funksiyalar:

Xodimlar ro'yxatdan o'tkazish va autentifikatsiya (login).
QR kod yoki yuz tanish (face recognition) orqali ishtirok qayd etish.
Real-time bildirishnomalar Telegram bot orqali.
Web dashboard orqali hisobotlar ko'rish (kunlik, oylik statistika).
Vazifalar (tasks) va maqsadlar (goals) boshqarish.
Xatolarni boshqarish (error handling) va logging.

Loyiha Qismlari:

Backend va Frontend: Java Spring Boot-da, serverda ishlaydi.
Lokal Scanner: Python-da, alohida Linux device da (masalan, kompyuter yoki Raspberry Pi), ngrok orqali bog'lanadi.
Telegram Bot: Python-da, lokal yoki serverda.

Loyiha SOLID prinsiblariga (Single Responsibility, Open-Closed, Liskov Substitution, Interface Segregation, Dependency Inversion) amal qilgan – ya'ni kod modulli va kengaytiriladigan.
Foydalanuvchilar:

Admin: Hisobotlar ko'radi, xodimlarni boshqaradi.
Xodim: QR/yuz orqali ro'yxatdan o'tadi, bot orqali bildirishnoma oladi.

Muammolar va Yechimlar: Tizim real vaqtda ishlashi kerak, shuning uchun Kafka message queue ishlatilgan. Xavfsizlik uchun JWT autentifikatsiya va OAuth.
2. Arxitektura (Architecture)
Loyiha mikroservis arxitekturasi asosida qurilgan. Mikroservislar – bu mustaqil xizmatlar, ular bir-biri bilan REST API, gRPC yoki message queue (Kafka) orqali bog'lanadi. Bu monolit tizimlardan farqli o'laroq, har bir qismni alohida rivojlantirish va deploy qilish imkonini beradi.
Asosiy Komponentlar:

API Gateway: Barcha so'rovlarni boshqaradi (Spring Cloud Gateway yoki Nginx).
Mikroservislar: User Service, Attendance Service, Tasks Service, Goals Service (Java-da).
Lokal Komponent: QR/Face scanner (Python OpenCV), alohida device da.
Message Queue: Kafka – real-time eventlar uchun (masalan, attendance log bo'lganda bildirishnoma yuborish).
Database: PostgreSQL – relational ma'lumotlar uchun (user, attendance, tasks, goals jadvallari).
Integratsiya: Ngrok – lokal scanner-ni public qilish uchun.

Hybrid Yondashuv: Tizimning bir qismi cloud/serverda (web va backend), boshqasi lokal device da (scanner). Bu resurslarni tejash va offline ishlatish imkonini beradi.
Quyida mikroservis arxitekturasining diagrammasi ko'rsatilgan. Bu diagramma loyihaning yuqori darajadagi ko'rinishini beradi, Java Spring Boot va Python integratsiyasini ko'rsatadi.
javaguides.netlinkedin.combacancytechnology.com


Deployment Arxitekturasi: Backend serverda Docker/Kubernetes bilan deploy qilinadi, scanner esa alohida Linux device da batch script orqali o'rnatiladi. Quyida hybrid deployment diagrammasi:
learn.microsoft.comlearn.microsoft.com

UML Diagrammalar:

Class Diagram: Tizim classlarini, atributlarini va metodlarini ko'rsatadi. Masalan, User classida id, username, faceEncoding bor.

stackoverflow.comt4tutorials.com


Sequence Diagram: Oqimlarni ko'rsatadi, masalan, QR scan → autentifikatsiya → log.

researchgate.netresearchgate.net

3. Ishlatilgan Kutubxonalar va Toollar (Libraries and Tools)
Loyiha turli kutubxonalar va toollarni ishlatadi. Quyida batafsil ro'yxat:
Umumiy Toollar:

Git: Versiya nazorati uchun.
Docker: Konteynerlashtirish uchun (mikroservislarni alohida ishlatish).
Ngrok: Lokal scanner-ni public qilish uchun (tunnel yaratish).
Postman: API testlash uchun.
VS Code yoki IntelliJ IDEA: Kod yozish uchun.
Draw.io yoki PlantUML: Diagrammalar chizish uchun.

Java Qismi Kutubxonalari (Spring Boot Dependencies):

spring-boot-starter-web: REST API uchun.
spring-boot-starter-data-jpa: DB bilan ishlash uchun (Hibernate bilan).
spring-boot-starter-security: Autentifikatsiya (JWT, OAuth).
spring-kafka: Kafka integratsiyasi uchun.
spring-cloud-starter-gateway: API Gateway uchun.
lombok: Kodni qisqartirish uchun (getters/setters avto).
postgresql: DB driver.
thymeleaf: Frontend HTML templatelar uchun (web dashboard).
junit: Testlash uchun.

Python Qismi Kutubxonalari:

opencv-python: Kamera va QR/face recognition uchun.
pyzbar: QR kod dekodlash uchun.
face_recognition: Yuz tanish uchun.
aiogram: Telegram bot uchun (async).
requests: API calls uchun.
fastapi va uvicorn: Lokal API uchun (scanner-da).
pydantic: Data modellar uchun.
logging: Error logging uchun.
backoff yoki retrying: Retry mechanism uchun (xatolarda qayta urinish).

Boshqa:

Kafka: Message queue.
PostgreSQL: DB.
Flyway yoki Liquibase: DB migration uchun (Java-da).

Bu kutubxonalar loyihani tez va ishonchli qiladi. Masalan, OpenCV yuz tanishni tez bajaradi, Spring Security xavfsizlikni ta'minlaydi.
4. Java-da Nima Qilinadi (What is Done in Java)
Java qismi loyihaning "og'ir" qismi – backend va web frontend. Spring Boot framework ishlatilgan, chunki u enterprise darajasida va oson konfiguratsiya qilinadi.
Asosiy Vazifalar:

User Service: Foydalanuvchilarni boshqarish – registratsiya, login (Spring Security bilan JWT), ma'lumotlar yangilash. Masalan, UserController-da @PostMapping("/register") endpoint.
Attendance Service: Ishtirok qayd etish va hisobotlar. logAttendance() metodi DB-ga saqlaydi va Kafka-ga event yuboradi.
Tasks va Goals Services: Vazifalar va maqsadlarni boshqarish – admin uchun.
API Gateway: Trafikni routing qiladi, masalan /users → User Service.
Web Dashboard: Thymeleaf bilan HTML sahifalar – dashboard.html-da statistika table va chartlar.
Error Handling: Custom exceptions (UserNotFoundException), @ExceptionHandler bilan.
Integratsiya: Kafka bilan real-time, PostgreSQL bilan DB.

Java kod struktura modulli: Controller (API), Service (logic), Repository (DB), Model (entities). SOLID bo'yicha interfacelar ishlatilgan (IUserService).
5. Python-da Nima Qilinadi (What is Done in Python)
Python qismi tez prototiplash va computer vision uchun ideal, shuning uchun lokal scanner va bot uchun ishlatilgan.
Asosiy Vazifalar:

QR/Face Scanner: OpenCV bilan kamera ochish, pyzbar bilan QR o'qish, face_recognition bilan yuz mosligini tekshirish. scan_qr() funksiyasi backend-ga requests.post orqali yuboradi.
Telegram Bot: Aiogram bilan handlers – /start, photo handling (QR/face from photo). services.py-da API calls.
Error Handling: try-except bilan (ConnectionError, ValueError), logging bilan yozish.
Integratsiya: Requests bilan Java API-ga ulanish, ngrok bilan public exposure.
Lokal Run: main.py-da loop, config.py-da URL va thresholdlar.

Python kod ham modulli: Funksiyalar va classlar (AttendanceLogger), Pydantic bilan modellar.
6. O'rnatish va Ishga Tushirish (Installation and Running)
Umumiy Qadamlar:

Git clone: git clone attendance-system.
Dependencies: Java uchun mvn install, Python uchun pip install -r requirements.txt.

Lokal Scanner (Linux Device):

install_scanner.sh ni ishga tushiring – avto o'rnatadi.
Ngrok: ngrok http 8000 – URL oling.
Run: python scanner/main.py.

Server (Backend/Frontend):

DB setup: PostgreSQL yaratish.
Run: mvn spring-boot:run har mikroservis uchun.
Dashboard: Browserda http://localhost:8080/dashboard.

Testlash: Postman bilan API, scanner-da QR ko'rsating.
7. Xavfsizlik va Monitoring

Xavfsizlik: JWT, HTTPS, input validation.
Monitoring: Prometheus + Grafana, logging bilan.
Scalability: Mikroservislar bilan, load balancing.

8. Kelajak Rivojlantirish

ML qo'shish (attendance prediction).
Mobile app integratsiyasi.
Cloud deploy (AWS).

Ushbu hujjat loyihani to'liq qamrab oladi. Savollar bo'lsa, qo'shimcha batafsillik uchun ayting!
