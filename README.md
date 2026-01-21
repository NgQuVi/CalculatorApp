#Ultimate Calculator App - NgQuVi_1410
## Project Roadmap (5 Phase)

### Giai đoạn 1: Khởi tạo & Cấu trúc (Project Setup)
**Mục tiêu:** Tạo khung dự án và thiết lập môi trường phát triển.
- Tạo project mới (Empty Activity).
- Cấu hình `build.gradle`: ViewModel, LiveData, Material Design.
- Thiết lập `.gitignore` chuẩn Android.
- Tạo cấu trúc package: `model`, `view`, `viewmodel`, `utils`.
- GitHub: `chore: initial project setup`.

### Giai đoạn 2: Xây dựng Core Logic (Backend Engine)
**Mục tiêu:** Máy tính tính đúng trước khi tối ưu UI.
- Tạo `ExpressionEvaluator` xử lý chuỗi biểu thức.
- Áp dụng thuật toán Shunting-yard (hoặc parser) để xử lý ưu tiên toán tử và ngoặc.
- Dùng `BigDecimal` cho số lớn và độ chính xác cao.
- Hỗ trợ hàm Scientific: `sin`, `cos`, `tan`, `log`, `pow`, `sqrt`.
- Viết unit test (ví dụ `1+1=2`, chia cho 0 không crash).
- GitHub: `feat: implement core math logic and unit tests`.

### Giai đoạn 3: Thiết kế Giao diện (UI/UX Implementation)
**Mục tiêu:** Tạo UI hiện đại, fresh, responsive.
- Thiết kế `colors.xml`, `styles.xml` (Neumorphism hoặc Minimalist Dark Mode).
- Layout `activity_main.xml` dùng `ConstraintLayout`.
- Chia bàn phím: cơ bản và scientific (ẩn/hiện hoặc vuốt).
- Custom button với drawable shadow/bo góc.
- GitHub: `ui: design calculator layout and themes`.

### Giai đoạn 4: Kết nối & MVVM (Integration)
**Mục tiêu:** Kết nối UI với core logic qua MVVM.
- Tạo `CalculatorViewModel`.
- Dùng `LiveData` để update UI khi nhấn phím.
- Xử lý Backspace, AC, và cập nhật biểu thức/kết quả.
- GitHub: `feat: connect UI with ViewModel and implement calculation logic`.

### Giai đoạn 5: Tinh chỉnh & Nâng cao (Polish & Advanced Features)
**Mục tiêu:** Hoàn thiện UX và tính ổn định.
