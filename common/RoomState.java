// package common;

// /**
// * RoomState - Enum định nghĩa các trạng thái của phòng chơi
// *
// * LUỒNG TRẠNG THÁI PHÒNG:
// * WAITING_FOR_PLAYER → FULL → SHOWING_RULES → READY_CHECK
// * → IN_PROGRESS → FINISHED → (WAITING_FOR_PLAYER hoặc DESTROYED)
// */
// public enum RoomState {
// // === CHỜ NGƯỜI CHƠI ===
// WAITING_FOR_PLAYER, // Phòng có 1 người, chờ người thứ 2

// // === ĐỦ NGƯỜI ===
// FULL, // Phòng đủ 2 người, chuẩn bị bắt đầu

// // === HIỂN THỊ LUẬT ===
// SHOWING_RULES, // Đang hiển thị luật chơi cho cả 2 người

// // === KIỂM TRA SẴN SÀNG ===
// READY_CHECK, // Đang kiểm tra sẵn sàng (30s timeout)

// // === ĐANG CHƠI ===
// IN_PROGRESS, // Đang chơi game (các lượt 1, 2, 3)

// // === KẾT THÚC ===
// FINISHED, // Đã kết thúc, hỏi chơi lại không

// // === HỦY ===
// DESTROYED // Phòng đã bị xóa (không còn ai)
// }