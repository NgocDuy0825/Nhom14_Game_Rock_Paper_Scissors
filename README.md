Rock Paper Scissors – Network Programming (TCP Socket)
1. Tổng quan dự án
    Dự án Rock Paper Scissors – Network là một mini game oẳn tù tì được xây dựng theo mô hình Client – Server sử dụng Java TCP Socket. Toàn bộ logic xử lý trò     chơi được đặt tại phía Server, trong khi Client chỉ đảm nhiệm vai trò gửi dữ liệu đầu vào (tên người chơi, lựa chọn) và hiển thị kết quả.
  Dự án được thiết kế nhằm phục vụ mục đích học tập môn Lập trình mạng, giúp sinh viên hiểu rõ:
    •	Cơ chế kết nối TCP
    •	Mô hình xử lý đa client bằng Thread
    •	Luồng trao đổi dữ liệu Client ↔ Server
    •	Cách kiểm tra dữ liệu truyền nhận bằng Wireshark
   
2. Công nghệ & môi trường
  •	Ngôn ngữ: Java
  •	Mạng: TCP Socket
  •	Mô hình: Client – Server
  •	Xử lý đồng thời: Multi-thread (1 thread / client)
  •	IDE khuyến nghị: IntelliJ IDEA, Eclipse, VS Code
  •	Công cụ phân tích mạng: Wireshark

3. Cấu trúc source code
  RockPaperScissors-Network
  ├── src
  │ ├── server
  │ │ ├── GameServer.java # Khởi động server, lắng nghe kết nối
  │ │ ├── HandleClient.java # Xử lý giao tiếp với từng client (Thread)
  │ │ └── GameRoom.java # Quản lý phòng chơi và logic so sánh kết quả
  │ ├── client
  │ │ └── GameClient.java # Chương trình client kết nối server
  │ └── common
  │ └── Message.java # Định nghĩa cấu trúc dữ liệu trao đổi
  ├── README.md
  └── .gitignore

4. Phân tích chi tiết từng thành phần
  4.1. GameServer.java
    •	Khởi tạo ServerSocket với port cố định
    •	Lắng nghe kết nối từ client
    •	Mỗi khi có client kết nối:
      o	Tạo một đối tượng HandleClient
      o	Gán client vào GameRoom
      o	Khởi chạy thread xử lý riêng
    Vai trò chính:
      •	Quản lý vòng đời server
      •	Điều phối client vào các phòng chơi
   
  4.2. HandleClient.java
    •	Mỗi instance tương ứng 1 client
    •	Kế thừa Thread
    •	Thực hiện:
      o	Nhận tên người chơi
      o	Nhận lựa chọn Rock / Paper / Scissors
      o	Gửi dữ liệu về GameRoom
      o	Nhận kết quả từ server và gửi lại client
    Đây là lớp trung gian giao tiếp giữa client và logic game.
    
  4.3. GameRoom.java
    •	Quản lý 2 client trong 1 phòng chơi
    •	Lưu lựa chọn của từng người chơi
    •	Thực hiện so sánh kết quả theo luật:
      o	Rock > Scissors
      o	Scissors > Paper
      o	Paper > Rock
    •	Trả kết quả thắng / thua / hòa về cho từng client
    Lớp này chứa toàn bộ logic nghiệp vụ của trò chơi.
    
  4.4. GameClient.java
    •	Tạo Socket kết nối tới server (IP + Port)
    •	Gửi tên người chơi
    •	Nhận yêu cầu từ server
    •	Gửi lựa chọn của người chơi
    •	Nhận và hiển thị kết quả
    Client không xử lý logic game, chỉ thực hiện I/O.
    
  4.5. Message.java
    •	Định nghĩa cấu trúc dữ liệu dùng để trao đổi giữa client và server
    •	Giúp thống nhất format dữ liệu
    •	Tránh truyền dữ liệu rời rạc
   
6. Luồng hoạt động của chương trình
  1.	Server khởi động và mở cổng TCP
  2.	Client kết nối tới server
  3.	Server tạo thread HandleClient
  4.	Server ghép 2 client vào một GameRoom
  5.	Server yêu cầu client gửi tên
  6.	Server yêu cầu client gửi lựa chọn
  7.	GameRoom xử lý kết quả
  8.	Server gửi kết quả về cho từng client
  9.	Client hiển thị kết quả và kết thúc phiên chơi
  
6. Hướng dẫn cài đặt & chạy chương trình
   
  6.1. Yêu cầu
    •	Java JDK 8 hoặc cao hơn
    •	Đã cấu hình JAVA_HOME
    Kiểm tra Java:
      java -version
   
  6.2. Chạy Server
    cd src
    javac server/GameServer.java
    java server.GameServer
    Server hiển thị:
    Server started...
    Waiting for players...

  6.3. Chạy Client
    cd src
    javac client/GameClient.java
    java client.GameClient

7. Kiểm tra bằng Wireshark
  Filter TCP:
  tcp.port == 3000
  Có thể quan sát:
    •	TCP 3-way handshake
    •	Gói tin gửi tên người chơi
    •	Gói tin gửi lựa chọn
    •	Gói tin trả kết quả

8. Các lỗi thường gặp
  Không tìm thấy class main
    •	Sai package
    •	Chạy sai thư mục src
  Client không kết nối được server
    •	Server chưa chạy
    •	Sai IP / Port
    •	Firewall chặn cổng

9. Hướng phát triển
  •	Hỗ trợ nhiều người chơi
  •	Thêm chế độ chơi lại
  •	Tách protocol riêng cho UDP
  •	Thêm GUI (JavaFX)

10. Kết luận
Dự án giúp nắm vững cách xây dựng một ứng dụng mạng Client – Server cơ bản bằng TCP, từ việc thiết kế luồng xử lý, quản lý đa luồng cho đến phân tích dữ liệu truyền nhận trên mạng.

