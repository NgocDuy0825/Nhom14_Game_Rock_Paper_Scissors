# Rock Paper Scissors - Network Programming (TCP Socket)

## 1. Tong quan du an

Du an Rock Paper Scissors la mot mini game oan tu ti duoc xay dung theo mo hinh Client - Server su dung Java TCP Socket. Toan bo logic xu ly tro choi nam o phia Server, Client chi thuc hien gui du lieu va hien thi ket qua.

Muc tieu chinh:

* Hieu mo hinh Client - Server
* Thuc hanh lap trinh mang TCP
* Xu ly da client bang Thread
* Kiem tra du lieu truyen nhan bang Wireshark

---

## 2. Cong nghe su dung

* Ngon ngu: Java
* Mang: TCP Socket
* Mo hinh: Client - Server
* Xu ly dong thoi: Multi-thread
* Cong cu ho tro: Wireshark

---

## 3. Cau truc thu muc

```text
RockPaperScissors-Network
├── src
│   ├── server
│   │   ├── GameServer.java      # Khoi dong server
│   │   ├── HandleClient.java    # Xu ly moi client (Thread)
│   │   └── GameRoom.java        # Logic tro choi
│   ├── client
│   │   └── GameClient.java      # Chuong trinh client
│   └── common
│       └── Message.java         # Dinh nghia du lieu trao doi
├── README.md
└── .gitignore
```

---

## 4. Mo ta chuc nang cac thanh phan

### 4.1. GameServer.java

* Tao ServerSocket voi port co dinh
* Lang nghe ket noi tu client
* Moi client duoc xu ly boi mot Thread HandleClient
* Gheps cap client vao GameRoom

---

### 4.2. HandleClient.java

* Dai dien cho 1 client
* Ke thua Thread
* Nhan ten nguoi choi
* Nhan lua chon Rock / Paper / Scissors
* Gui va nhan ket qua tu GameRoom

---

### 4.3. GameRoom.java

* Quan ly 2 client trong 1 phong choi
* Luu lua chon cua tung nguoi choi
* So sanh ket qua theo luat:

  * Rock thang Scissors
  * Scissors thang Paper
  * Paper thang Rock

---

### 4.4. GameClient.java

* Ket noi toi server thong qua IP va Port
* Gui ten nguoi choi
* Gui lua chon
* Nhan va hien thi ket qua

---

### 4.5. Message.java

* Dinh nghia cau truc du lieu gui giua Client va Server
* Giup thong nhat format truyen tin

---

## 5. Luong hoat dong chuong trinh

1. Server khoi dong va mo cong TCP
2. Client ket noi toi server
3. Server tao Thread xu ly client
4. Hai client duoc ghep vao GameRoom
5. Client gui ten
6. Client gui lua chon
7. Server xu ly ket qua
8. Server gui ket qua ve client
9. Client hien thi ket qua

---

## 6. Huong dan chay chuong trinh

### 6.1. Yeu cau

* Java JDK 8 hoac cao hon
* Da cau hinh JAVA_HOME

Kiem tra Java:

```bash
java -version
```

---

### 6.2. Chay Server

```bash
cd src
javac server/GameServer.java
java server.GameServer
```

---

### 6.3. Chay Client

```bash
cd src
javac client/GameClient.java
java client.GameClient
```

---

## 7. Kiem tra bang Wireshark

Filter:

```text
tcp.port == 3000
```

Co the quan sat:

* TCP 3-way handshake
* Du lieu gui lua chon
* Du lieu tra ve ket qua

---

## 8. Loi thuong gap

* Khong tim thay main class do sai package
* Client khong ket noi duoc do server chua chay
* Sai IP hoac Port

---

## 9. Huong phat trien

* Ho tro nhieu phong choi
* Them che do choi lai
* Phien ban UDP de so sanh voi TCP
* Them giao dien GUI

