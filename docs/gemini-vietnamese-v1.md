# Hướng dẫn thử Gemini và tiếng Việt

## Tạo khóa API bằng tài khoản Google cá nhân

Không cần tài khoản Google Workspace hay EDU. Dùng tài khoản Google cá nhân để mở [Google AI Studio](https://aistudio.google.com/api-keys), chọn hoặc tạo một project rồi tạo API key cho Gemini API. Kiểm tra giới hạn sử dụng và billing của project trong AI Studio trước khi thử.

Không gửi khóa qua chat, email, issue hoặc log. Dán khóa trực tiếp vào Cicada:

- **Linux:** mở Cài đặt → Dịch vụ AI → Google Gemini, dán khóa vào ô API key và lưu. Cicada lưu khóa riêng trong `~/.config/cicada/ai-key-gemini`, với quyền tệp `0600`.
- **Android:** mở Cài đặt → Dịch vụ AI → Google Gemini, dán khóa và lưu. Khóa nằm trong vùng riêng của ứng dụng, được mã hóa bằng Android Keystore và loại khỏi bản sao lưu.

Cicada không đưa khóa vào settings JSON, log hay bản ghi cuộc họp. Chọn Google Gemini làm provider để thử; Ling vẫn là provider mặc định và có thể chọn lại trong cùng màn hình.

## Mô hình và dữ liệu gửi đi

Gemini dùng model riêng theo tác vụ: dịch văn bản và nhận dạng audio mặc định là `gemini-2.5-flash-lite`; Meeting Notes mặc định là `gemini-2.5-flash`. Có thể chọn `gemini-3.8-flash` hoặc model khác trong danh sách model của từng tác vụ. Các model này gọi Gemini `generateContent` trực tiếp, không qua giao thức tương thích OpenAI. Model 2.5 nhận văn bản/audio và trả văn bản; với bản dịch nhanh app gửi `thinkingBudget: 0`, còn Meeting Notes giữ cấu hình suy luận mặc định. WAV mono 16 kHz được gửi inline, tối đa 8 giây mỗi đoạn. Khi chọn Gemini, audio được gửi tới Google và app không tự chuyển sang provider khác khi lỗi. Android dùng TTS của thiết bị; Linux giữ đường phát giọng hiện có.

Google hiện đánh dấu `gemini-2.5-flash` là deprecated và công bố sẽ tự định tuyến request model này sang `gemini-3.5-flash-lite` từ 1/12/2026. Vì vậy cần chạy lại kiểm thử chất lượng Meeting Notes trước mốc đó; app vẫn giữ model tường minh trong cài đặt và không tự đổi provider.

## Kiểm thử trực tiếp

Sau khi khóa được lưu, chọn Gemini và chạy kiểm tra provider trong Cài đặt. Sau đó thử một cuộc hội thoại ngắn gồm:

1. Câu tiếng Việt có dấu, tên riêng và thuật ngữ.
2. Câu tiếng Anh cần dịch sang tiếng Việt, rồi đổi chiều Việt → Anh.
3. Câu dài và hai người nói ngắt lời hoặc nói liên tiếp.
4. Xem lại transcript để đối chiếu nguyên văn và timestamp; tạo biên bản tiếng Việt và so từng quyết định, công việc, người phụ trách, hạn chót với transcript.

Ghi nhận độ trễ từng request và token input/output trong log debug. Nội dung câu nói, audio và API key không được ghi vào log. Đối chiếu token với bảng giá hiện tại trong Google AI Studio để tính chi phí; bảng giá, tier và quota phụ thuộc model/project và có thể thay đổi. Không dùng số đo từ mock test để báo chất lượng nhận dạng hoặc dịch.

Mock tests kiểm tra schema request/response, thinking config, ánh xạ lỗi và retry; chúng không xác nhận chất lượng. Smoke live gần nhất: dịch Việt–Anh và Anh–Việt trên Flash-Lite trả HTTP 200; audio tiếng Anh từ clip mẫu và tiếng Việt từ WAV giọng tổng hợp được nhận dạng bằng Flash-Lite; Meeting Notes mẫu bằng Flash trả đúng hai công việc và thời hạn, không tạo quyết định. Đây chưa phải kiểm thử giọng người dùng hoặc micro thật. Bản build chưa được coi là sẵn sàng production cho đến khi kiểm tra audio trên thiết bị thật.
