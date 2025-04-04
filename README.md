# SelfAlarm Project (2rd Android Group Project)

## Nhóm thực hiện
- Phan Thị Mỹ Linh - 22110172
- Nguyễn Hữu Lộc - 22110179
- Trần Nguyễn Quốc Bảo - 22110112

## Tính năng chính

### 1. Trình phát nhạc
- Phát, tạm dừng và điều hướng qua các bài hát thông qua API ZingMP3
- Điều khiển phát nhạc
- Phát nhạc nền với các điều khiển thông báo
- Quản lý danh sách phát với các bảng xếp hạng thời gian thực (API Zing)

### 2. Quản lý SMS & Cuộc gọi
- Giám sát và phát hiện tin nhắn SMS đến
- Xử lý cuộc gọi đến với Telephony Manager
- Duy trì danh sách đen để tự động từ chối cuộc gọi hoặc xóa tin nhắn không mong muốn
- Theo dõi chi tiết nhật ký cuộc gọi và tin nhắn SMS

### 3. Lịch trình cá nhân
- Thêm, xóa và chỉnh sửa sự kiện lịch
- Nhập sự kiện từ lịch thiết bị
- Đặt lời nhắc và thông báo cho các sự kiện đã lên lịch
- Kích hoạt các hành động cụ thể vào thời điểm đã lên lịch (email, yêu cầu API, cảnh báo)

### 4. Tối ưu hóa pin
- Giám sát mức pin và trạng thái sạc
- Điều chỉnh cài đặt hệ thống (độ sáng, Wi-Fi, đồng bộ hóa) dựa trên mức pin
- Tối ưu hóa tài nguyên dựa trên trạng thái màn hình (bật/tắt)
- Thống kê sử dụng pin và các khuyến nghị

## Kiến trúc kỹ thuật

Ứng dụng tuân theo kiến trúc có cấu trúc với sự phân chia rõ ràng về trách nhiệm:

### Controller
- **Receivers**: Các broadcast receiver để xử lý sự kiện hệ thống và tương tác người dùng
    - `AlarmReceiver`: Quản lý các báo thức đã lên lịch
    - `BatteryMonitorReceiver` & `BatteryReceiver`: Giám sát trạng thái pin
    - `BootReceiver`: Khôi phục trạng thái ứng dụng sau khi khởi động lại thiết bị
    - `CallReceiver`: Xử lý cuộc gọi đến
    - `HeadphoneReceiver`: Phát hiện trạng thái kết nối tai nghe
    - `MediaButtonReceiver`: Bắt các lệnh từ nút media
    - `ScreenStateReceiver`: Giám sát trạng thái bật/tắt màn hình
    - `SMSReceiver`: Xử lý tin nhắn SMS đến

### Services 
- `BatteryOptimizationService`: Điều chỉnh cài đặt dựa trên trạng thái pin
- `BlacklistService`: Quản lý lọc cuộc gọi và SMS
- `MusicPlaybackService`: Xử lý phát nhạc nền
- `ReminderService`: Quản lý các lời nhắc đã lên lịch
- `ResourceOptimizationManager`: Tối ưu hóa sử dụng tài nguyên hệ thống
- `ScheduleService`: Xử lý các sự kiện đã lên lịch

### Data & Models
- Các model cho thành phần ứng dụng: Sự kiện, Cuộc gọi, SMS, Bài hát, Nghệ sĩ, Album, Danh sách phát,...
- Lưu trữ cục bộ cho tùy chọn người dùng và cài đặt ứng dụng

### Tiện ích (Utilities)
- `AlarmScheduler`: Lên lịch báo thức và thông báo
- `BatteryUtils`: Các hàm tiện ích liên quan đến pin
- `CallStateManager`: Quản lý trạng thái cuộc gọi
- `CryptoUtils`: Xử lý mã hóa cho dữ liệu nhạy cảm
- `JsonUtils`: Tiện ích phân tích cú pháp JSON
- `NetworkUtils`: Các chức năng liên quan đến mạng
- `NotificationHelper`: Tạo và quản lý thông báo
- `PreferenceManager`: Xử lý tùy chọn người dùng

### Giao diện người dùng (UI)
- Triển khai Material Design
- Nhiều fragment cho các phần khác nhau của ứng dụng
- Bố cục đáp ứng cho nhiều kích thước màn hình

## Chi tiết triển khai kỹ thuật

### BroadcastReceivers
Ứng dụng sử dụng rộng rãi hệ thống BroadcastReceiver của Android để:
- Lắng nghe tin nhắn SMS
- Xử lý cuộc gọi đến bằng Telephony Manager
- Giám sát mức pin và trạng thái sạc
- Phát hiện sự kiện bật/tắt màn hình
- Bắt các nút điều khiển media
- Phản hồi sự kiện kết nối/ngắt kết nối tai nghe
- Kích hoạt các sự kiện đã lên lịch bằng AlarmManager

### Services
Ứng dụng sử dụng các dịch vụ nền và tiền cảnh để:
- Duy trì danh sách đen cho cuộc gọi và tin nhắn
- Điều chỉnh cài đặt hệ thống dựa trên pin hoặc tùy chọn người dùng
- Quản lý phát nhạc trong nền với các điều khiển thông báo
- Xử lý logic sự kiện đã lên lịch


## Cài đặt

1. Clone repository về máy
2. Mở dự án trong Android Studio
3. Build và chạy ứng dụng trên thiết bị hoặc máy ảo của bạn

## Yêu cầu

- API 26+
- Quyền truy cập bộ nhớ để phát nhạc
- Quyền SMS và Cuộc gọi để xử lý tin nhắn và cuộc gọi
- Quyền truy cập lịch để quản lý lịch trình
- Ngoại lệ tối ưu hóa pin cho các dịch vụ nền
