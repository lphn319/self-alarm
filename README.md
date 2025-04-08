# SelfAlarm - 2rd Group Project

## Nhóm thực hiện
- Nguyễn Hữu Lộc - 22110179
- Phan Thị Mỹ Linh - 22110172
- Trần Nguyễn Quốc Bảo - 22110112

## Tính năng chính

### 1. Trình phát nhạc tích hợp
- **Tích hợp API ZingMP3**: Truy cập và phát nhạc từ thư viện ZingMP3 với đầy đủ thông tin bài hát, nghệ sĩ và album
- **Trình phát nền**: Phát nhạc trong nền với các điều khiển thông báo đầy đủ (phát/tạm dừng/bài tiếp theo/bài trước)
- **Điều khiển đa dạng**: Hỗ trợ các nút điều khiển media trên tai nghe, thông báo và màn hình khóa
- **Bảng xếp hạng thời gian thực**: Hiển thị bảng xếp hạng trending từ ZingMP3
- **Tự động tạm dừng**: Dừng nhạc khi ngắt kết nối tai nghe để tiết kiệm pin và dữ liệu

### 2. Quản lý SMS & Cuộc gọi thông minh
- **Danh sách đen linh hoạt**: Cho phép chặn cuộc gọi và tin nhắn từ các số điện thoại không mong muốn
- **Tùy chỉnh theo số**: Lựa chọn chặn chỉ cuộc gọi, chỉ tin nhắn hoặc cả hai cho từng số điện thoại
- **Nhật ký chi tiết**: Theo dõi lịch sử cuộc gọi và tin nhắn với thông tin thời gian, thời lượng và loại cuộc gọi
- **Thông báo thông minh**: Nhận thông báo khi có tin nhắn hoặc cuộc gọi bị chặn
- **Tự động nhận diện**: Hiển thị tên liên hệ nếu số điện thoại đã được lưu trong danh bạ

### 3. Lịch trình và Nhắc nhở
- **Quản lý sự kiện toàn diện**: Thêm, chỉnh sửa, xóa các sự kiện với tiêu đề, mô tả, địa điểm và thời gian
- **Lên lịch thông minh**: Đặt thời gian bắt đầu, kết thúc và lời nhắc cho mỗi sự kiện
- **Giao diện lịch trực quan**: Xem sự kiện theo ngày với giao diện lịch tháng cho phép điều hướng dễ dàng
- **Hệ thống nhắc nhở**: Đặt lời nhắc trước thời gian sự kiện với nhiều tùy chọn (phút, giờ, ngày)
- **Thông báo chi tiết**: Nhận thông báo đầy đủ thông tin về sự kiện sắp diễn ra

### 4. Tối ưu hóa pin thông minh
- **Giám sát pin thời gian thực**: Theo dõi liên tục mức pin, trạng thái sạc và nhiệt độ pin
- **Tùy chỉnh theo mức pin**: Điều chỉnh các cài đặt hệ thống tự động dựa trên mức pin hiện tại
- **Tối ưu hóa theo màn hình**: Thay đổi cài đặt khi màn hình tắt để tiết kiệm pin tối đa
- **Thông báo cảnh báo**: Cảnh báo khi pin yếu với các đề xuất tối ưu hóa cụ thể
- **Các cấp độ tối ưu**: Hỗ trợ nhiều cấp độ tối ưu hóa từ nhẹ đến mạnh tùy theo tình trạng pin

## Kiến trúc kỹ thuật

### 1. Controller Layer
- **Broadcast Receivers**: Lắng nghe và xử lý các sự kiện hệ thống Android
    - `AlarmReceiver`: Xử lý báo thức cho sự kiện đã lên lịch
    - `BatteryMonitorReceiver` & `BatteryReceiver`: Giám sát trạng thái pin
    - `BootReceiver`: Khôi phục dịch vụ sau khi thiết bị khởi động lại
    - `CallReceiver`: Đón nhận và xử lý cuộc gọi đến
    - `HeadphoneReceiver`: Phát hiện kết nối/ngắt kết nối tai nghe
    - `MediaButtonReceiver`: Bắt các lệnh từ nút media
    - `ScreenStateReceiver`: Giám sát trạng thái màn hình
    - `SMSReceiver`: Xử lý tin nhắn SMS đến

### 2. Service Layer
- `BatteryOptimizationService`: Dịch vụ nền để tối ưu hóa pin
- `BlacklistService`: Quản lý danh sách đen và chặn cuộc gọi/tin nhắn
- `MusicPlaybackService`: Dịch vụ phát nhạc nền với điều khiển thông báo
- `ReminderService`: Xử lý và hiển thị lời nhắc cho sự kiện
- `ResourceOptimizationManager`: Quản lý tối ưu hóa tài nguyên hệ thống
- `ScheduleService`: Xử lý logic cho các sự kiện đã lên lịch

### 3. Data Layer
- **Models**: Định nghĩa cấu trúc dữ liệu cho ứng dụng
    - `Event`: Mô tả sự kiện lịch
    - `BlacklistContact`: Thông tin liên hệ trong danh sách đen
    - `CallLogEntry`: Bản ghi cuộc gọi
    - `SMS`: Dữ liệu tin nhắn
    - `Music`, `Album`, `Artist`, `Playlist`: Mô hình dữ liệu cho trình phát nhạc
- **Data Source**:
    - `DatabaseHelper`: Quản lý cơ sở dữ liệu SQLite cho lịch trình
    - `AppDatabase`: Cơ sở dữ liệu Room cho dữ liệu nhạc và danh sách đen
    - `ZingMp3Api` & `ZingMp3Service`: Truy cập API ZingMP3 cho phát nhạc trực tuyến

### 4. Utilities
- `AlarmScheduler`: Lên lịch báo thức với AlarmManager
- `BatteryUtils`: Các tiện ích liên quan đến pin
- `CallStateManager`: Quản lý trạng thái cuộc gọi
- `CryptoUtils`: Mã hóa và xác thực cho API ZingMP3
- `JsonUtils`: Xử lý dữ liệu JSON
- `NetworkUtils`: Kiểm tra kết nối mạng
- `NotificationHelper`: Tạo và quản lý thông báo
- `PreferenceManager`: Lưu trữ và truy xuất cài đặt người dùng

### 5. View Layer
- **Fragments**: Các màn hình giao diện người dùng
    - `AddEditEventFragment`: Thêm/sửa sự kiện
    - `BatteryFragment`: Hiển thị thông tin và tối ưu pin
    - `BlacklistFragment`: Quản lý danh sách đen
    - `CallsFragment`: Hiển thị lịch sử cuộc gọi
    - `MusicChartFragment` & `MusicPlayerFragment`: Giao diện trình phát nhạc
    - `ScheduleFragment`: Hiển thị lịch và sự kiện
    - `SMSFragment`: Hiển thị tin nhắn
- **Adapters**: Kết nối dữ liệu với UI
    - `BlacklistAdapter`, `CallsAdapter`, `SMSAdapter`: Hiển thị danh sách
    - `ChartAdapter`, `EventAdapter`: Hiển thị nhạc và sự kiện

### 6. ViewModel Layer
- `MusicPlayerViewModel`: Quản lý trạng thái phát nhạc
- `HomeViewModel`: Quản lý dữ liệu trang chính

## Các nội dung đã làm được

### 1. Tích hợp API ZingMP3 hoàn chỉnh
- **Xác thực bảo mật**: Sử dụng HMAC-SHA512 và cơ chế Signature để xác thực với API
- **Truy xuất nội dung đa dạng**: Hỗ trợ lấy bài hát, album, nghệ sĩ, bảng xếp hạng
- **Cache thông minh**: Lưu trữ cục bộ để giảm thiểu yêu cầu mạng

### 2. Tối ưu hóa pin thông minh đa cấp độ
- **Tối ưu hóa tự động**: Áp dụng các cài đặt tối ưu dựa trên mức pin và trạng thái sạc
- **Tối ưu hóa theo màn hình**: Tối ưu hóa tài nguyên khi màn hình tắt
- **Kiểm soát đặc quyền**: Điều chỉnh Wi-Fi, độ sáng, đồng bộ hóa dựa trên cài đặt người dùng

### 3. Hệ thống danh sách đen linh hoạt
- **Lọc tùy chỉnh**: Lọc cuộc gọi, tin nhắn hoặc cả hai theo từng số
- **Xác nhận trực quan**: Thông báo khi thêm hoặc xóa số khỏi danh sách đen
- **Chặn tự động**: Chặn cuộc gọi và tin nhắn không mong muốn trong thời gian thực

### 4. Media Session và MediaButtonReceiver
- **Điều khiển đa nền tảng**: Hỗ trợ điều khiển phát nhạc từ tai nghe, thông báo và màn hình khóa
- **Quản lý trọng tâm âm thanh**: Tự động tạm dừng khi có cuộc gọi đến
- **Thông báo phương tiện phong phú**: Hiển thị thông tin bài hát và ảnh album

## Cài đặt và triển khai

### Yêu cầu hệ thống
- Android 8.0 (API level 26) trở lên
- Tối thiểu 50MB bộ nhớ trống
- Kết nối internet cho tính năng phát nhạc trực tuyến

### Xin cấp các quyền
- `READ_PHONE_STATE`, `PROCESS_OUTGOING_CALLS`, `READ_CALL_LOG`: Quản lý cuộc gọi
- `READ_SMS`, `RECEIVE_SMS`: Đọc và nhận tin nhắn SMS
- `INTERNET`, `ACCESS_NETWORK_STATE`: Truy cập internet và kiểm tra kết nối
- `FOREGROUND_SERVICE`: Chạy dịch vụ nền
- `WAKE_LOCK`: Giữ thiết bị hoạt động khi phát nhạc
- `WRITE_SETTINGS`: Tối ưu hóa đèn nền (tùy chọn)

### Hướng dẫn cài đặt
1. Clone repository
2. Mở dự án trong Android Studio
3. Cấu hình Gradle và tải các phụ thuộc
4. Build và cài đặt ứng dụng trên thiết bị

## Phân tích chi tiết mã nguồn

### Cấu trúc package và tổ chức mã nguồn
Tổ chức cấu trúc mã nguồn như sau: 
```
hcmute.edu.vn.linhvalocvabao.selfalarmproject
├── controller
│   ├── receivers - Các broadcast receivers xử lý sự kiện hệ thống
│   └── services - Các service xử lý tác vụ nền
├── data
│   ├── api - Giao tiếp với ZingMP3 API
│   ├── db - Cơ sở dữ liệu Room và SQLite
│   ├── model - Các lớp data model
│   └── repository - Tầng trung gian giữa ViewModel và data sources
├── di - Dependency Injection với Dagger Hilt
├── models - Các model cơ bản của ứng dụng
├── utils - Các lớp tiện ích
├── view
│   ├── adapter - Các adapter cho RecyclerView
│   ├── fragments - Các fragment UI
│   ├── home - Package chứa giao diện trang chính
│   └── viewmodels - ViewModel cho các màn hình
└── MainActivity.java - Điểm vào chính của ứng dụng
```
### Chi tiết
#### Receivers (Controller Layer)
- **AlarmReceiver**: Lắng nghe sự kiện báo thức, hiển thị thông báo và khởi động ReminderService để thực thi các hành động đã lên lịch.
- **BatteryReceiver & BatteryMonitorReceiver**: Giám sát trạng thái pin và kích hoạt tối ưu hóa khi pin xuống thấp, sử dụng cơ chế "cooling down" để tránh thông báo quá nhiều.
- **BootReceiver**: Khởi động lại các dịch vụ nền sau khi thiết bị khởi động, đảm bảo hoạt động liên tục.
- **CallReceiver**: Xử lý sự kiện cuộc gọi đến/đi, tích hợp với danh sách đen để lọc cuộc gọi.
- **SMSReceiver**: Lắng nghe tin nhắn SMS đến, tích hợp với danh sách đen, và chuyển thông tin đến UI.
- **HeadphoneReceiver**: Phát hiện sự kiện kết nối/ngắt kết nối tai nghe để điều chỉnh phát nhạc phù hợp.
- **MediaButtonReceiver**: Bắt sự kiện từ các nút điều khiển media (tai nghe, bluetooth) và chuyển đổi thành lệnh cho ứng dụng.
- **ScreenStateReceiver**: Phát hiện khi màn hình bật/tắt để điều chỉnh tài nguyên hệ thống phù hợp.

#### Services (Service Layer)
- **MusicPlaybackService**: Service foreground quản lý phát nhạc với đầy đủ tính năng:
    - Quản lý MediaSession để tương tác với hệ thống Android
    - Điều khiển trọng tâm âm thanh (AudioFocus)
    - Hiển thị thông báo phương tiện với điều khiển đầy đủ
    - Xử lý playlist và trạng thái phát nhạc
    - Tích hợp với ZingMP3API để lấy URL stream

  ```java
  // Quản lý MediaSession để tương tác với hệ thống Android
  private void updateMetadata() {
      MediaMetadataCompat.Builder metadataBuilder = new MediaMetadataCompat.Builder()
          .putString(MediaMetadataCompat.METADATA_KEY_TITLE, song.getTitle())
          .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, song.getArtists())
          .putLong(MediaMetadataCompat.METADATA_KEY_DURATION,
                  mediaPlayer != null ? mediaPlayer.getDuration() : 0);
      mediaSession.setMetadata(metadataBuilder.build());
  }
  ```

- **BlacklistService**: Quản lý danh sách đen với các chức năng:
    - Thêm/xóa số điện thoại vào danh sách đen
    - Kiểm tra số điện thoại có trong danh sách đen không
    - Lưu trữ và tải danh sách đen từ SharedPreferences
    - Từ chối cuộc gọi và chặn tin nhắn từ các số trong danh sách

  ```java
  // Kiểm tra số điện thoại có trong danh sách đen không
  private boolean isBlacklisted(String phoneNumber) {
      if (phoneNumber == null || phoneNumber.isEmpty()) {
          return false;
      }
      return blacklistMap.containsKey(phoneNumber);
  }
  ```

- **BatteryOptimizationService**: Dịch vụ tối ưu hóa pin với nhiều mức độ:
    - Điều chỉnh độ sáng màn hình dựa trên mức pin
    - Tắt/bật WiFi khi pin yếu
    - Quản lý đồng bộ hóa nền
    - Lưu trữ và khôi phục cài đặt ban đầu

  ```java
  // Tối ưu hóa khi màn hình tắt và pin yếu
  private void optimizeForScreenOff() {
      if (BatteryUtils.shouldOptimize(this)) {
          // Lưu cài đặt hiện tại
          editor.putInt(KEY_ORIGINAL_BRIGHTNESS, getCurrentBrightness());
          editor.apply();
          
          // Giảm độ sáng màn hình
          BatteryUtils.setBrightness(this, 50);
          
          // Tắt WiFi nếu không đang sạc
          if (wifiOptimizationEnabled && !BatteryUtils.isCharging(this)) {
              BatteryUtils.setWifiEnabled(this, false);
          }
      }
  }
  ```

- **ReminderService**: Xử lý lời nhắc cho sự kiện đã lên lịch với thông báo và hành động phù hợp.

#### API Layer (ZingMP3 Integration)
- **ZingMp3Api**: Tương tác với API của ZingMP3 để lấy dữ liệu nhạc:
    - Lấy URL stream cho bài hát
    - Lấy bảng xếp hạng trending
    - Lấy thông tin chi tiết về bài hát, album
    - Tìm kiếm nội dung

  ```java
  // Lấy URL stream cho bài hát từ API
  public LiveData<String> getSongStreamUrl(String id) {
      CryptoUtils.SignatureResult sigResult = CryptoUtils.hashParamWithCtime(Constants.SONG_PATH, id);
      Map<String, String> params = new HashMap<>();
      params.put("id", id);
      params.put("sig", sigResult.getSignature());
      params.put("ctime", sigResult.getCtime());
      
      service.getSong(params).enqueue(new Callback<BaseResponse>() {
          @Override
          public void onResponse(@NonNull Call<BaseResponse> call, @NonNull Response<BaseResponse> response) {
              // Xử lý phản hồi và trích xuất URL
          }
      });
      return result;
  }
  ```

- **CryptoUtils**: Xử lý xác thực cho ZingMP3 API:
    - Tạo chữ ký HMAC-SHA512 cho các yêu cầu API
    - Tạo hàm băm SHA-256
    - Quản lý tham số ctime (timestamp)

  ```java
  // Tạo chữ ký HMAC-SHA512 cho các yêu cầu API
  public static String hashParam(@NonNull String path, @NonNull String id) {
      String CTIME = String.valueOf(System.currentTimeMillis() / 1000);
      return getHmac512(
              path + getSha256(
                      "ctime=" + CTIME + "id=" + id + "version=" + Constants.API_VERSION
              ),
              SECRET_KEY
      );
  }
  ```

#### Utilities (Helper Classes)
- **AlarmScheduler**: Lên lịch báo thức sử dụng AlarmManager với hỗ trợ đa phiên bản Android.
- **BatteryUtils**: Tiện ích liên quan đến pin, cung cấp các phương thức để đọc thông tin pin và điều chỉnh cài đặt hệ thống.
- **CallStateManager**: Quản lý trạng thái cuộc gọi và tính toán thời lượng cuộc gọi.
- **NetworkUtils**: Kiểm tra trạng thái mạng và cung cấp LiveData để theo dõi thay đổi kết nối.
- **NotificationHelper**: Tạo và cập nhật thông báo với hỗ trợ kênh thông báo cho Android O trở lên.
- **PreferenceManager**: Quản lý cài đặt người dùng với hỗ trợ mã hóa EncryptedSharedPreferences.

#### Database và Repository
- **DatabaseHelper**: SQLiteOpenHelper quản lý cơ sở dữ liệu cho sự kiện và lịch trình.
- **AppDatabase**: Cấu hình Room Database cho lưu trữ cục bộ về nhạc, album, và playlist.
- **MusicRepository**: Tầng trung gian giữa ViewModel và data sources:
    - Kết hợp dữ liệu từ API và cơ sở dữ liệu cục bộ
    - Triển khai caching strategy
    - Xử lý kết nối mạng offline

### Thư viện và Framework
- Room Database: Lưu trữ cục bộ
- Glide: Tải và hiển thị hình ảnh
- Retrofit: Giao tiếp API
- Dagger Hilt: Dependency Injection
- LiveData & ViewModel: Quản lý trạng thái và dữ liệu
- MediaPlayer: Phát nhạc
- PapaParser: Xử lý tệp CSV
