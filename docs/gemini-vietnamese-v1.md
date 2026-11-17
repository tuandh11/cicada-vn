# Hướng dẫn thử Gemini và tiếng Việt

## Tạo khóa API bằng tài khoản Google cá nhân

Không cần tài khoản Google Workspace hay EDU. Dùng tài khoản Google cá nhân để mở [Google AI Studio](https://aistudio.google.com/api-keys), chọn hoặc tạo một project rồi tạo API key cho Gemini API. Kiểm tra giới hạn sử dụng và billing của project trong AI Studio trước khi thử.

Không gửi khóa qua chat, email, issue hoặc log. Dán khóa trực tiếp vào Cicada:

- **Linux:** mở Cài đặt → Dịch vụ AI → Google Gemini, dán khóa vào ô API key và lưu. Cicada lưu khóa riêng trong `~/.config/cicada/ai-key-gemini`, với quyền tệp `0600`.
- **Android:** mở Cài đặt → Dịch vụ AI → Google Gemini, dán khóa và lưu. Khóa nằm trong vùng riêng của ứng dụng, được mã hóa bằng Android Keystore và loại khỏi bản sao lưu.

Cicada không đưa khóa vào settings JSON, log hay bản ghi cuộc họp. Chọn Google Gemini làm provider để thử; Ling vẫn là provider mặc định và có thể chọn lại trong cùng màn hình.

## Mô hình và dữ liệu gửi đi

Model mặc định là `gemini-3.8-flash`, gọi trực tiếp qua Gemini `generateContent` API. Model nhận văn bản và audio, trả văn bản. Cicada gửi đoạn WAV mono 16 kHz inline; mỗi đoạn nghe tối đa 8 giây, thấp hơn giới hạn 20 MB cho toàn bộ inline request. Âm thanh được gửi tới Google khi chọn Gemini. Google không xuất giọng nói từ model này; Android dùng giọng TTS của thiết bị, Linux giữ đường phát giọng hiện có.

## Kiểm thử trực tiếp

Sau khi khóa được lưu, chọn Gemini và chạy kiểm tra provider trong Cài đặt. Sau đó thử một cuộc hội thoại ngắn gồm:

1. Câu tiếng Việt có dấu, tên riêng và thuật ngữ.
2. Câu tiếng Anh cần dịch sang tiếng Việt, rồi đổi chiều Việt → Anh.
3. Câu dài và hai người nói ngắt lời hoặc nói liên tiếp.
4. Xem lại transcript để đối chiếu nguyên văn và timestamp; tạo biên bản tiếng Việt và so từng quyết định, công việc, người phụ trách, hạn chót với transcript.

Ghi nhận độ trễ từng request và token input/output trong log debug. Nội dung câu nói, audio và API key không được ghi vào log. Đối chiếu token với bảng giá hiện tại trong Google AI Studio để tính chi phí; bảng giá, tier và quota phụ thuộc model/project và có thể thay đổi. Không dùng số đo từ mock test để báo chất lượng nhận dạng hoặc dịch.

Mock tests chỉ kiểm tra schema request/response, ánh xạ lỗi và retry. Chúng không gọi Google và không xác nhận chất lượng tiếng Việt. Bản build chưa được coi là sẵn sàng production cho đến khi kiểm tra trên thiết bị thật với Gemini API và audio thật.
