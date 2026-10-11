<div align="center">

# LiveTranslate (Asssub)

**Ứng dụng dịch âm thanh thời gian thực cho Android**
Dịch trực tiếp qua Microphone · Thu âm thanh hệ thống · Phụ đề nổi (Overlay)

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)
![Android](https://img.shields.io/badge/Android-10%2B-3DDC84?logo=android&logoColor=white)
![Kotlin](https://img.shields.io/badge/Kotlin-2.0.21-7F52FF?logo=kotlin&logoColor=white)
![Version](https://img.shields.io/badge/version-2.6.2-0058BC)

[English](README.md) · [简体中文](README.zh-CN.md) · [Tải file APK](https://github.com/dangtrandang/asssub/releases/latest) · [Tài liệu kỹ thuật](docs/README.md)

</div>

---

LiveTranslate hỗ trợ dịch giọng nói trực tiếp từ microphone (chế độ Hội thoại / Đồng phiên) hoặc thu âm thanh phát ra từ các ứng dụng xem video/livestream (YouTube, TikTok, Netflix, trình duyệt...) và hiển thị phụ đề nổi trực tiếp trên màn hình theo thời gian thực.

Mục tiêu chính là **hỗ trợ nắm bắt nội dung với độ trễ cực thấp (low-latency comprehension)** bằng mô hình Gemini Live. Ứng dụng chạy hoàn toàn phía client: người dùng tự cấu hình Gemini API Key của mình, không cần máy chủ trung gian, không cần tài khoản, không mất phí thuê bao.

## Tính năng nổi bật

| Tính năng | Mô tả |
|---|---|
| **Hỗ trợ Tiếng Việt toàn diện** | Giao diện tiếng Việt hoàn chỉnh, hỗ trợ dịch nguồn và đích sang Tiếng Việt (`vi`). |
| **Dịch phụ đề video & Quick Settings Tile** | Bật/tắt dịch nhanh 1 chạm từ thanh thông báo hệ thống (Quick Settings) mà không cần mở giao diện app đè lên video. |
| **Chế độ phụ đề 1 dòng / 2 dòng** | Tắt mặc định dòng phụ đề xám câu trước để người đọc tập trung vào 1 dòng duy nhất, không bị phân tâm; có thể bật lại trong cài đặt. |
| **Chế độ hiển thị phụ đề** | Dịu mắt (Khung 2 dòng cố định + Chữ Soft Reveal êm dịu, mặc định), Chuẩn Chrome CC (Typewriter tức thì 0ms), hoặc Tinh gọn (Đơn dòng). |
| **Độ trễ tức thì (Zero-Delay) & Dwell Time** | Mặc định cập nhật phụ đề tức thì (`0s`), không trễ; cho phép kéo chỉnh thời gian giữ phụ đề (0s - 4.0s) để phù hợp nhịp đọc. |
| **Ngắt câu thông minh & Dải ký tự dài** | Hỗ trợ ngắt câu tự nhiên theo dấu chấm `.`, mở rộng độ dài dòng (20 - 120 ký tự, mặc định 70) giúp câu tiếng Việt liền mạch, không bị cắt vụn. |
| **Hộp phụ đề nổi thông minh** | Tự động ẩn nút điều khiển sau 2.5s để bạn tập trung xem video; chạm nhẹ để hiện lại. |
| **Tùy chỉnh độ mờ nền 1 chạm** | Đổi nhanh độ mờ ngay trên box: **80%** (mờ đậm) ➔ **40%** (mờ nhẹ) ➔ **0%** (trong suốt hoàn toàn, chỉ nổi chữ kèm bóng đổ rõ nét). |
| **Nút đóng ✕ nhanh** | Dừng hoàn toàn việc dịch và đóng box phụ đề trong 1 chạm. |
| **Dịch trực tiếp (Live Interpretation)** | Dành cho các cuộc họp, hội thảo, bài giảng, phỏng vấn, du lịch. |
| **Thư viện ngữ cảnh (Scene Library)** | Đã tích hợp sẵn 12 ngữ cảnh mẫu (Tổng quát, Game, Tin tức, Phim hoạt hình, Bài giảng...) bằng tiếng Việt. |
| **Bảo mật cục bộ** | API Key được mã hóa bằng Android Keystore AES-GCM; lịch sử dịch lưu trữ cục bộ trong bộ nhớ máy. |

## Sơ đồ luồng hoạt động

```text
Microphone AudioRecord ───────────┐
                                  ├─→ PCM16 / 16 kHz / mono / 100 ms
Âm thanh ứng dụng (Playback) ─────┘
                                      ↓
                            Gemini Live Translate
                                      ↓
                              SubtitleStabilizer
                         ┌────────────┼────────────┐
                         ↓            ↓            ↓
                   Dòng chữ trong App   Phụ đề nổi    Lịch sử lưu trữ
```

## Bắt đầu sử dụng

### 1. Cài đặt

Tải bản phát hành mới nhất từ [Releases](https://github.com/dangtrandang/asssub/releases/latest) hoặc tự biên dịch từ mã nguồn:

```bash
git clone https://github.com/dangtrandang/asssub.git
cd asssub/android
./gradlew :app:assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

*Yêu cầu môi trường build: JDK 17 và Android SDK 35.*

### 2. Thiết lập lần đầu

1. Vào **Cài đặt → Dịch thuật** và dán Gemini API Key của bạn (có thể nhập nhiều key cách nhau bằng dấu phẩy).
2. Chọn ngôn ngữ nguồn, ngôn ngữ đích (hỗ trợ Tiếng Việt, Tiếng Anh, Tiếng Trung, Tiếng Nhật, v.v.) và kịch bản dịch phù hợp.
3. Cấp quyền Microphone (cho chế độ Hội thoại) hoặc quyền Hiển thị trên ứng dụng khác (cho chế độ Video).
4. Bạn có thể thêm ô chức năng **"Dịch video"** vào bảng Cài đặt nhanh (Quick Settings) của Android để bật/tắt dịch bất cứ lúc nào khi đang xem phim.

## Giấy phép

Dự án được phân phối dưới giấy phép [MIT](LICENSE).
