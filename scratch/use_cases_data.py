# -*- coding: utf-8 -*-

USE_CASES = [
    {
        'num': '3.2.1',
        'code': 'UC1',
        'name': 'Đăng ký',
        'title': '3.2.1 Mô tả use case Đăng ký',
        'actor': 'User',
        'description': 'Use Case này cho phép khách hàng mới tạo lập tài khoản cá nhân trên ứng dụng Thunder Book bằng Email và Mật khẩu để thực hiện mua sắm, quản lý giỏ hàng và theo dõi đơn hàng.',
        'trigger': 'Khách hàng nhấn chọn "Đăng ký ngay" trên màn hình Đăng nhập của ứng dụng.',
        'precondition': 'Khách hàng chưa đăng nhập vào ứng dụng và thiết bị có kết nối Internet.',
        'main_flow': [
            (1, 'Người dùng', 'Nhấn chọn "Đăng ký ngay" tại màn hình Đăng nhập.'),
            (2, 'Hệ thống', 'Hiển thị màn hình Đăng ký (RegisterActivity) với các trường: Họ và tên, Email, Số điện thoại, Mật khẩu, Xác nhận mật khẩu.'),
            (3, 'Người dùng', 'Nhập đầy đủ thông tin hợp lệ và nhấn nút "Đăng ký".'),
            (4, 'Hệ thống', 'Kiểm tra tính hợp lệ của dữ liệu: định dạng email, số điện thoại đủ 10 chữ số, mật khẩu từ 6-12 ký tự gồm chữ hoa và ký tự đặc biệt, mật khẩu xác nhận trùng khớp.'),
            (5, 'Hệ thống', 'Gọi Firebase Authentication để tạo tài khoản người dùng với Email và Mật khẩu.'),
            (6, 'Hệ thống', 'Tạo bản ghi thông tin người dùng trong Cloud Firestore (collection "users") với phân quyền mặc định là "user".'),
            (7, 'Hệ thống', 'Hiển thị thông báo "Đăng ký thành công!" và chuyển hướng người dùng vào màn hình chính (HomeActivity).')
        ],
        'alt_flow': [
            ('1a', 'Người dùng', 'Để trống một hoặc nhiều trường thông tin bắt buộc hoặc mật khẩu xác nhận không khớp.'),
            ('', 'Hệ thống', 'Hiển thị thông báo lỗi cụ thể (ví dụ: "Vui lòng nhập họ tên", "Mật khẩu xác nhận không khớp") và giữ nguyên dữ liệu đã nhập.'),
            ('2a', 'Người dùng', 'Nhập địa chỉ Email hoặc Số điện thoại đã được đăng ký trước đó trên hệ thống.'),
            ('', 'Hệ thống', 'Hiển thị thông báo lỗi: "Email hoặc số điện thoại đã được sử dụng bởi tài khoản khác."'),
            ('3a', 'Người dùng', 'Nhập mật khẩu không đáp ứng yêu cầu bảo mật (dưới 6 ký tự hoặc không có chữ hoa/ký tự đặc biệt).'),
            ('', 'Hệ thống', 'Hiển thị thông báo yêu cầu mật khẩu phải từ 6-12 ký tự, chứa ít nhất 1 chữ hoa và 1 ký tự đặc biệt.'),
            ('4a', 'Hệ thống', 'Mất kết nối Internet hoặc dịch vụ Firebase không phản hồi.'),
            ('', 'Hệ thống', 'Hiển thị thông báo lỗi: "Không thể kết nối với máy chủ, vui lòng kiểm tra lại kết nối mạng."')
        ],
        'postcondition': 'Tài khoản mới được tạo thành công trên Firebase Authentication và Firestore, người dùng đăng nhập vào hệ thống.'
    },
    {
        'num': '3.2.2',
        'code': 'UC2',
        'name': 'Mua hàng',
        'title': '3.2.2 Mô tả use case Mua hàng',
        'actor': 'User',
        'description': 'Use Case này cho phép khách hàng đặt mua các cuốn sách đã chọn trong giỏ hàng, xác nhận địa chỉ nhận hàng, áp dụng mã voucher giảm giá và tạo đơn hàng trên hệ thống Thunder Book.',
        'trigger': 'Khách hàng nhấn nút "Thanh toán" trong Giỏ hàng hoặc nút "Mua ngay" tại trang Chi tiết sách.',
        'precondition': 'Khách hàng đã đăng nhập tài khoản và giỏ hàng có ít nhất một sản phẩm sách còn hàng trong kho.',
        'main_flow': [
            (1, 'Người dùng', 'Chọn các sản phẩm sách cần mua trong Giỏ hàng và nhấn nút "Thanh toán".'),
            (2, 'Hệ thống', 'Chuyển sang màn hình Thanh toán (CheckoutActivity), tải thông tin danh sách sách đã chọn, địa chỉ giao hàng mặc định, phí vận chuyển và tính toán tổng tiền tạm tính.'),
            (3, 'Người dùng', 'Xác nhận hoặc chọn lại địa chỉ nhận hàng từ sổ địa chỉ; nhấn chọn mã Voucher giảm giá (nếu có).'),
            (4, 'Hệ thống', 'Áp dụng giảm giá voucher (theo %, tiền mặt hoặc freeship) và tính toán lại tổng tiền thanh toán cuối cùng.'),
            (5, 'Người dùng', 'Nhập ghi chú cho đơn hàng (nếu cần) và nhấn nút "Đặt hàng".'),
            (6, 'Hệ thống', 'Thực thi Firestore Transaction: kiểm tra số lượng tồn kho của từng cuốn sách, trừ số lượng tồn kho tương ứng và tạo tài liệu đơn hàng mới trong collection "orders".'),
            (7, 'Hệ thống', 'Tự động xóa các sản phẩm sách đã mua khỏi giỏ hàng của người dùng trên Firestore.'),
            (8, 'Hệ thống', 'Hiển thị thông báo "Đặt hàng thành công!" và chuyển hướng người dùng đến màn hình Chi tiết đơn hàng (OrderDetailActivity).')
        ],
        'alt_flow': [
            ('1a', 'Người dùng', 'Chưa có địa chỉ giao hàng nào trong sổ địa chỉ.'),
            ('', 'Hệ thống', 'Hiển thị thông báo "Vui lòng chọn hoặc thêm địa chỉ nhận hàng" và điều hướng sang màn hình Thêm địa chỉ.'),
            ('2a', 'Hệ thống', 'Số lượng sách tồn kho không đủ so với số lượng khách đặt mua tại thời điểm xác nhận đơn.'),
            ('', 'Hệ thống', 'Hủy giao dịch đặt hàng, thông báo "Số lượng sách trong kho không đủ đáp ứng" và cập nhật lại giỏ hàng.'),
            ('3a', 'Người dùng', 'Mã voucher đã hết hạn sử dụng hoặc không thỏa mãn điều kiện áp dụng.'),
            ('', 'Hệ thống', 'Hiển thị thông báo "Voucher không khả dụng" và giữ nguyên mức giá gốc.')
        ],
        'postcondition': 'Đơn hàng mới được tạo với trạng thái PENDING, số lượng tồn kho sách được cập nhật trừ đi tương ứng, giỏ hàng được dọn sạch.'
    },
    {
        'num': '3.2.3',
        'code': 'UC3',
        'name': 'Thêm địa chỉ giao hàng',
        'title': '3.2.3 Mô tả use case Thêm địa chỉ giao hàng',
        'actor': 'User',
        'description': 'Use Case này cho phép khách hàng tạo mới một địa chỉ nhận hàng bao gồm họ tên người nhận, số điện thoại và địa chỉ chi tiết theo cấp Tỉnh/Huyện/Xã để phục vụ quá trình giao nhận sách.',
        'trigger': 'Khách hàng nhấn nút "Thêm địa chỉ mới" tại màn hình Sổ địa chỉ hoặc màn hình Thanh toán.',
        'precondition': 'Khách hàng đã đăng nhập tài khoản trên ứng dụng.',
        'main_flow': [
            (1, 'Người dùng', 'Nhấn nút "Thêm địa chỉ mới" (+).'),
            (2, 'Hệ thống', 'Hiển thị màn hình Thêm địa chỉ (AddEditAddressActivity), nạp danh sách Tỉnh/Thành phố từ API hành chính.'),
            (3, 'Người dùng', 'Nhập Họ tên người nhận, Số điện thoại; chọn Tỉnh/Thành phố từ danh sách dropdown.'),
            (4, 'Hệ thống', 'Tự động lọc và hiển thị danh sách Quận/Huyện tương ứng với Tỉnh/Thành phố đã chọn.'),
            (5, 'Người dùng', 'Chọn Quận/Huyện, chọn Phường/Xã và nhập địa chỉ chi tiết (số nhà, tên đường/ngõ).'),
            (6, 'Người dùng', 'Tùy chọn tích chọn "Đặt làm địa chỉ mặc định" và nhấn nút "Lưu địa chỉ".'),
            (7, 'Hệ thống', 'Kiểm tra tính hợp lệ của thông tin (các trường không để trống, số điện thoại đúng định dạng 10 chữ số).'),
            (8, 'Hệ thống', 'Tạo bản ghi địa chỉ mới trong subcollection "users/{uid}/addresses" trên Cloud Firestore.'),
            (9, 'Hệ thống', 'Hiển thị thông báo "Đã lưu địa chỉ" và quay lại màn hình trước đó.')
        ],
        'alt_flow': [
            ('1a', 'Người dùng', 'Để trống một hoặc nhiều trường thông tin bắt buộc (họ tên, SĐT, tỉnh/huyện/xã, địa chỉ cụ thể).'),
            ('', 'Hệ thống', 'Hiển thị thông báo lỗi "Vui lòng nhập đầy đủ thông tin" và đánh dấu các trường còn thiếu.'),
            ('2a', 'Người dùng', 'Nhập số điện thoại người nhận không đúng 10 chữ số.'),
            ('', 'Hệ thống', 'Hiển thị thông báo lỗi: "Số điện thoại phải gồm 10 chữ số."')
        ],
        'postcondition': 'Địa chỉ nhận hàng mới được thêm vào sổ địa chỉ cá nhân của người dùng trên Firestore.'
    },
    {
        'num': '3.2.4',
        'code': 'UC4',
        'name': 'Thêm sách',
        'title': '3.2.4 Mô tả use case Thêm sách',
        'actor': 'Admin',
        'description': 'Use Case này cho phép Quản trị viên nhập thông tin, tải ảnh bìa và tạo mới một đầu sách vào kho dữ liệu của hệ thống Thunder Book.',
        'trigger': 'Admin nhấn nút (+) Thêm sách trên thanh công cụ của màn hình Quản lý sách.',
        'precondition': 'Người dùng đã đăng nhập hệ thống với quyền Quản trị viên (Admin).',
        'main_flow': [
            (1, 'Admin', 'Nhấn nút (+) Thêm sách trên màn hình Quản lý sách.'),
            (2, 'Hệ thống', 'Tải danh sách Tác giả, Nhà xuất bản, Thể loại từ Firestore và hiển thị form Thêm sách (AddEditBookActivity).'),
            (3, 'Admin', 'Nhập Tên sách, Mô tả, chọn Tác giả, Nhà xuất bản, các Thể loại tương ứng, nhập Giá gốc, Giá khuyến mãi, Số lượng tồn kho và chọn ảnh bìa từ thư viện máy.'),
            (4, 'Admin', 'Bật/tắt công tắc "Sách nổi bật" và "Kích hoạt bán", sau đó nhấn nút "Lưu sách".'),
            (5, 'Hệ thống', 'Kiểm tra tính hợp lệ của dữ liệu nhập (tên sách không rỗng, giá gốc > 0, giá khuyến mãi < giá gốc, tồn kho >= 0, đã chọn tác giả, NXB và ít nhất 1 thể loại).'),
            (6, 'Hệ thống', 'Tải tệp ảnh bìa lên Firebase Storage (thư mục "book_covers/") và nhận lại URL tải xuống công khai.'),
            (7, 'Hệ thống', 'Tạo mã định danh sách mới (bookId), khởi tạo các trường thống kê (soldCount=0, viewCount=0, rating=0, ratingCount=0) và lưu tài liệu vào collection "books" trên Firestore.'),
            (8, 'Hệ thống', 'Hiển thị thông báo "Đã lưu sách", đóng màn hình và cập nhật danh sách sách trên giao diện quản trị.')
        ],
        'alt_flow': [
            ('1a', 'Admin', 'Để trống tên sách hoặc chưa chọn Tác giả / Nhà xuất bản / Thể loại.'),
            ('', 'Hệ thống', 'Hiển thị thông báo lỗi tương ứng (ví dụ: "Vui lòng nhập tên sách", "Vui lòng chọn tác giả").'),
            ('2a', 'Admin', 'Nhập giá khuyến mãi lớn hơn hoặc bằng giá gốc hoặc nhập giá gốc <= 0.'),
            ('', 'Hệ thống', 'Hiển thị thông báo lỗi: "Giá khuyến mãi phải nhỏ hơn giá gốc."'),
            ('3a', 'Admin', 'Nhập số lượng tồn kho nhỏ hơn 0.'),
            ('', 'Hệ thống', 'Hiển thị thông báo lỗi: "Số lượng tồn kho không hợp lệ."'),
            ('4a', 'Hệ thống', 'Quá trình upload ảnh bìa lên Firebase Storage gặp sự cố mạng.'),
            ('', 'Hệ thống', 'Hiển thị thông báo "Tải ảnh thất bại, vui lòng thử lại" và giữ nguyên form nhập.')
        ],
        'postcondition': 'Đầu sách mới được lưu trữ trên Firestore và Firebase Storage, sẵn sàng hiển thị trên danh mục sách.'
    },
    {
        'num': '3.2.5',
        'code': 'UC5',
        'name': 'Sửa thông tin sách',
        'title': '3.2.5 Mô tả use case Sửa thông tin sách',
        'actor': 'Admin',
        'description': 'Use Case này cho phép Quản trị viên cập nhật các thông tin chi tiết (tiêu đề, giá bán, tồn kho, ảnh bìa, trạng thái hiển thị) hoặc thực hiện xóa mềm một cuốn sách đã có.',
        'trigger': 'Admin nhấn chọn một cuốn sách trong danh sách quản lý để mở form chỉnh sửa.',
        'precondition': 'Người dùng đã đăng nhập với quyền Admin và cuốn sách cần sửa đang tồn tại trên Firestore.',
        'main_flow': [
            (1, 'Admin', 'Nhấn chọn cuốn sách cần sửa từ danh sách trong ManageBookActivity.'),
            (2, 'Hệ thống', 'Tải toàn bộ thông tin chi tiết cuốn sách từ Firestore và đổ dữ liệu lên form Chỉnh sửa (AddEditBookActivity).'),
            (3, 'Admin', 'Chỉnh sửa các trường thông tin mong muốn (giá, tồn kho, mô tả, thể loại) và/hoặc chọn ảnh bìa mới từ thiết bị.'),
            (4, 'Admin', 'Nhấn nút "Lưu sách".'),
            (5, 'Hệ thống', 'Kiểm tra tính hợp lệ của dữ liệu chỉnh sửa.'),
            (6, 'Hệ thống', 'Nếu có ảnh bìa mới: tải ảnh lên Firebase Storage để lấy URL mới; nếu không: giữ nguyên URL ảnh cũ.'),
            (7, 'Hệ thống', 'Đóng gói các trường cập nhật và thực hiện cập nhật tài liệu sách trên Cloud Firestore.'),
            (8, 'Hệ thống', 'Hiển thị thông báo "Đã lưu sách", đóng Activity và cập nhật giao diện danh sách theo thời gian thực.')
        ],
        'alt_flow': [
            ('1a', 'Admin', 'Nhập dữ liệu không hợp lệ (tên để trống, giá âm, tồn kho âm, giá sale >= giá gốc).'),
            ('', 'Hệ thống', 'Hiển thị thông báo lỗi cụ thể và không cho phép thực hiện lưu.'),
            ('2a', 'Admin', 'Nhấn nút icon "Xóa" trên thanh công cụ để gỡ bỏ sách.'),
            ('', 'Hệ thống', 'Hiển thị hộp thoại cảnh báo xác nhận xóa sách.'),
            ('', 'Admin', 'Xác nhận xóa.'),
            ('', 'Hệ thống', 'Thực hiện xóa mềm bằng cách cập nhật trường "isActive = false" trên Firestore để bảo đảm toàn vẹn dữ liệu đơn hàng cũ, thông báo "Đã xóa sách" và đóng màn hình.')
        ],
        'postcondition': 'Thông tin cuốn sách được cập nhật chính xác trên Firestore và Firebase Storage.'
    },
    {
        'num': '3.2.6',
        'code': 'UC6',
        'name': 'Thêm thể loại',
        'title': '3.2.6 Mô tả use case Thêm thể loại',
        'actor': 'Admin',
        'description': 'Use Case này cho phép Quản trị viên tạo mới một danh mục / thể loại sách kèm ảnh đại diện để phân loại sách trên hệ thống Thunder Book.',
        'trigger': 'Admin nhấn nút (+) Thêm thể loại trên màn hình Quản lý thể loại.',
        'precondition': 'Người dùng đã đăng nhập hệ thống với quyền Quản trị viên (Admin).',
        'main_flow': [
            (1, 'Admin', 'Nhấn nút (+) FAB trên màn hình Quản lý thể loại (ManageCategoryActivity).'),
            (2, 'Hệ thống', 'Hiển thị hộp thoại form nhập liệu (AdminEntityFormDialog) gồm ô nhập Tên thể loại và khu vực chọn ảnh icon.'),
            (3, 'Admin', 'Nhập Tên thể loại và chọn ảnh minh họa từ thư viện máy (tùy chọn).'),
            (4, 'Admin', 'Nhấn nút "Lưu" trên hộp thoại.'),
            (5, 'Hệ thống', 'Kiểm tra tên thể loại không được để trống hoặc chỉ chứa khoảng trắng.'),
            (6, 'Hệ thống', 'Nếu có chọn ảnh: tải ảnh lên Firebase Storage mục "category_images/" và lấy URL ảnh.'),
            (7, 'Hệ thống', 'Tạo document ID mới, gán "isActive = true" và lưu thể loại vào collection "categories" trên Firestore.'),
            (8, 'Hệ thống', 'Hiển thị thông báo "Đã lưu", bộ lắng nghe Realtime Listener tự động cập nhật danh sách thể loại trên RecyclerView.')
        ],
        'alt_flow': [
            ('1a', 'Admin', 'Để trống tên thể loại khi bấm nút "Lưu".'),
            ('', 'Hệ thống', 'Hiển thị thông báo lỗi "Vui lòng nhập tên thể loại" và giữ nguyên hộp thoại.')
        ],
        'postcondition': 'Thể loại sách mới được tạo trên Firestore, sẵn sàng để gán cho các đầu sách.'
    },
    {
        'num': '3.2.7',
        'code': 'UC7',
        'name': 'Sửa thể loại',
        'title': '3.2.7 Mô tả use case Sửa thể loại',
        'actor': 'Admin',
        'description': 'Use Case này cho phép Quản trị viên chỉnh sửa tên, thay đổi ảnh đại diện hoặc xóa bỏ một thể loại sách đã tồn tại.',
        'trigger': 'Admin nhấn nút icon "Sửa" trên một mục thể loại trong danh sách.',
        'precondition': 'Người dùng đã đăng nhập với quyền Admin và thể loại cần sửa đang tồn tại trên Firestore.',
        'main_flow': [
            (1, 'Admin', 'Nhấn icon "Sửa" (Edit) bên cạnh thể loại muốn chỉnh sửa.'),
            (2, 'Hệ thống', 'Hiển thị form dialog với đầy đủ thông tin hiện tại (Tên thể loại cũ, Ảnh đại diện cũ).'),
            (3, 'Admin', 'Chỉnh sửa tên thể loại và/hoặc chọn ảnh đại diện mới từ thiết bị.'),
            (4, 'Admin', 'Nhấn nút "Lưu".'),
            (5, 'Hệ thống', 'Kiểm tra tên thể loại không được rỗng.'),
            (6, 'Hệ thống', 'Nếu có ảnh mới: tải lên Storage; nếu không: giữ nguyên ảnh cũ.'),
            (7, 'Hệ thống', 'Cập nhật tài liệu thể loại trong collection "categories" trên Cloud Firestore.'),
            (8, 'Hệ thống', 'Hiển thị thông báo "Đã lưu", giao diện tự động cập nhật lại thông tin qua Realtime Listener.')
        ],
        'alt_flow': [
            ('1a', 'Admin', 'Xóa trắng tên thể loại rồi nhấn nút "Lưu".'),
            ('', 'Hệ thống', 'Báo lỗi "Vui lòng nhập tên thể loại" và giữ nguyên form.'),
            ('2a', 'Admin', 'Nhấn nút "Xóa" thể loại nhưng thể loại này đang có sản phẩm sách liên kết.'),
            ('', 'Hệ thống', 'Kiểm tra ràng buộc trên Firestore và báo lỗi "Không thể xóa vì đang có sản phẩm thuộc thể loại này."')
        ],
        'postcondition': 'Thông tin thể loại sách được cập nhật thành công trên hệ thống.'
    },
    {
        'num': '3.2.8',
        'code': 'UC8',
        'name': 'Thêm voucher',
        'title': '3.2.8 Mô tả use case Thêm voucher',
        'actor': 'Admin',
        'description': 'Use Case này cho phép Quản trị viên cấu hình tạo mã khuyến mại / voucher giảm giá (theo phần trăm, số tiền cố định hoặc miễn phí vận chuyển) với thời hạn áp dụng cụ thể.',
        'trigger': 'Admin nhấn nút (+) Thêm voucher trên màn hình Quản lý voucher.',
        'precondition': 'Người dùng đã đăng nhập hệ thống với quyền Quản trị viên (Admin).',
        'main_flow': [
            (1, 'Admin', 'Nhấn nút (+) trên màn hình Quản lý voucher (ManageVoucherActivity).'),
            (2, 'Hệ thống', 'Mở màn hình Thêm voucher (AddEditVoucherActivity) với các trường cấu hình.'),
            (3, 'Admin', 'Nhập Mã voucher (VD: SUMMER2026), chọn Loại voucher qua Chip (Giảm %, Tiền cố định, Freeship), nhập Giá trị giảm, chọn Ngày bắt đầu và Ngày kết thúc qua DatePickerDialog, gạt công tắc "Kích hoạt ngay".'),
            (4, 'Admin', 'Nhấn nút "Lưu Voucher".'),
            (5, 'Hệ thống', 'Kiểm tra tính hợp lệ: Mã không rỗng, ngày kết thúc phải sau ngày bắt đầu, giá trị giảm > 0, nếu loại % thì giá trị <= 100%.'),
            (6, 'Hệ thống', 'Truy vấn Firestore kiểm tra mã voucher đã tồn tại trong collection "vouchers" hay chưa.'),
            (7, 'Hệ thống', 'Chuẩn hóa mã in hoa, sinh ID tài liệu và lưu dữ liệu voucher vào Cloud Firestore.'),
            (8, 'Hệ thống', 'Hiển thị thông báo "Đã lưu voucher" và quay trở lại màn hình danh sách voucher.')
        ],
        'alt_flow': [
            ('1a', 'Admin', 'Để trống mã voucher hoặc chưa chọn ngày áp dụng.'),
            ('', 'Hệ thống', 'Báo lỗi yêu cầu nhập đầy đủ mã voucher và khoảng thời gian áp dụng.'),
            ('2a', 'Admin', 'Chọn ngày kết thúc xảy ra trước hoặc trùng với ngày bắt đầu.'),
            ('', 'Hệ thống', 'Báo lỗi "Ngày kết thúc phải sau ngày bắt đầu."'),
            ('3a', 'Admin', 'Chọn voucher theo % nhưng nhập giá trị giảm lớn hơn 100% hoặc nhỏ hơn bằng 0.'),
            ('', 'Hệ thống', 'Báo lỗi "Giá trị giảm theo % phải từ 1 đến 100."'),
            ('4a', 'Admin', 'Nhập mã voucher đã tồn tại trên Firestore.'),
            ('', 'Hệ thống', 'Báo lỗi "Mã voucher đã tồn tại, vui lòng chọn mã khác."')
        ],
        'postcondition': 'Voucher mới được lưu trên Firestore và sẵn sàng cho khách hàng áp dụng khi thanh toán đơn hàng.'
    },
    {
        'num': '3.2.9',
        'code': 'UC9',
        'name': 'Trò chuyện với AI (Chatbot)',
        'title': '3.2.9 Mô tả use case Trò chuyện với AI (Chatbot)',
        'actor': 'User',
        'description': 'Use Case này cho phép khách hàng trò chuyện tương tác với Trợ lý ảo thông minh tích hợp mô hình ngôn ngữ lớn Google Gemini AI để được tư vấn chọn sách, tra cứu nội dung, kiểm tra tồn kho và nhận các thẻ sách gợi ý trực quan.',
        'trigger': 'Khách hàng nhấn vào nút biểu tượng Chatbot nổi (FAB) trên màn hình chính của ứng dụng.',
        'precondition': 'Khách hàng đã đăng nhập tài khoản và thiết bị có kết nối mạng Internet.',
        'main_flow': [
            (1, 'Người dùng', 'Nhấn vào biểu tượng Trợ lý AI trên màn hình chính.'),
            (2, 'Hệ thống', 'Mở màn hình ChatBotActivity, kết nối Firestore nạp lịch sử trò chuyện gần nhất của tài khoản (subcollection "users/{uid}/chat_history") hoặc hiển thị lời chào mừng kèm các gợi ý câu hỏi nhanh (Prompt Chips).'),
            (3, 'Người dùng', 'Nhập câu hỏi vào ô tin nhắn (ví dụ: "Gợi ý cho tôi vài cuốn sách tâm lý học hay") hoặc nhấn chọn một Prompt Chip gợi ý.'),
            (4, 'Hệ thống', 'Hiển thị bong bóng tin nhắn của người dùng, lưu tin nhắn vào Firestore, đồng thời hiển thị bong bóng AI đang phản hồi (Typing indicator).'),
            (5, 'Hệ thống', 'Chuyển ngữ cảnh cuộc trò chuyện sang Google Gemini API. Mô hình AI phân tích câu hỏi và kích hoạt cơ chế Multi-turn Tool Calling (gọi hàm tìm kiếm sách "search_books" hoặc kiểm tra kho "check_stock").'),
            (6, 'Hệ thống', 'Thực thi truy vấn Firestore bằng thuật toán Hybrid Search: tính toán Cosine Similarity vector embedding kết hợp khớp từ khóa Tiếng Việt và lọc theo giá bán, đánh giá để tìm các cuốn sách phù hợp nhất.'),
            (7, 'Hệ thống', 'Gửi dữ liệu sách tìm được về Gemini API để tổng hợp thành câu trả lời văn phong tự nhiên, mạch lạc.'),
            (8, 'Hệ thống', 'Hiển thị câu trả lời của AI kèm các thẻ sách đề xuất (ảnh bìa, tiêu đề, giá, rating) bên dưới bong bóng chat và lưu câu trả lời vào Firestore.'),
            (9, 'Người dùng', 'Có thể chạm trực tiếp vào thẻ sách đề xuất để mở màn hình Chi tiết sách (BookDetailActivity).')
        ],
        'alt_flow': [
            ('1a', 'Người dùng', 'Gửi câu hỏi khi mất kết nối Internet hoặc Gemini API quá tải/lỗi dịch vụ.'),
            ('', 'Hệ thống', 'Ẩn bong bóng đang gõ và hiển thị thông báo lỗi "Không thể kết nối với Trợ lý AI, vui lòng thử lại sau".'),
            ('2a', 'Người dùng', 'Nhấn vào nút "Xóa lịch sử chat" trên thanh tiêu đề.'),
            ('', 'Hệ thống', 'Hiển thị dialog xác nhận xóa toàn bộ tin nhắn; khi người dùng đồng ý, hệ thống xóa dữ liệu trong subcollection "chat_history" trên Firestore và đặt lại lời chào ban đầu.')
        ],
        'postcondition': 'Nội dung cuộc trò chuyện được lưu trữ trên Firestore, người dùng nhận được thông tin tư vấn sách hữu ích.'
    },
    {
        'num': '3.2.10',
        'code': 'UC10',
        'name': 'Đánh giá sản phẩm',
        'title': '3.2.10 Mô tả use case Đánh giá sản phẩm',
        'actor': 'User',
        'description': 'Use Case này cho phép khách hàng chấm điểm số sao (1-5 sao) và gửi nhận xét, bình luận đánh giá cho cuốn sách đã mua sau khi đơn hàng được giao thành công.',
        'trigger': 'Khách hàng nhấn nút "Đánh giá" bên cạnh sản phẩm sách trong màn hình Chi tiết đơn hàng đã hoàn thành.',
        'precondition': 'Khách hàng đã đăng nhập và đã mua cuốn sách đó với đơn hàng ở trạng thái "Đã giao" (DELIVERED).',
        'main_flow': [
            (1, 'Người dùng', 'Nhấn nút "Đánh giá" bên cạnh cuốn sách đã nhận tại màn hình Đơn hàng của tôi / Chi tiết đơn hàng.'),
            (2, 'Hệ thống', 'Mở màn hình Viết đánh giá (WriteReviewActivity), hiển thị ảnh bìa sách, tiêu đề sách, thanh chọn số sao và ô nhập nhận xét.'),
            (3, 'Người dùng', 'Chạm chọn mức điểm số sao (từ 1 đến 5 sao) và nhập lời nhận xét, cảm nhận về cuốn sách.'),
            (4, 'Người dùng', 'Nhấn nút "Gửi đánh giá".'),
            (5, 'Hệ thống', 'Kiểm tra dữ liệu: Đã chọn số sao hợp lệ (>= 1) và nội dung nhận xét không được để trống.'),
            (6, 'Hệ thống', 'Lấy thông tin snapshot của người dùng (họ tên, ảnh đại diện) và thực thi Firestore Transaction: kiểm tra trùng lặp theo ID "${userId}_${bookId}_${orderId}", đọc rating và ratingCount hiện tại của sách, tính toán điểm trung bình mới, cập nhật lại sách và ghi bản ghi vào collection "reviews".'),
            (7, 'Hệ thống', 'Hiển thị thông báo "Cảm ơn bạn đã đánh giá sản phẩm!", đóng màn hình đánh giá và cập nhật trạng thái đơn hàng.')
        ],
        'alt_flow': [
            ('1a', 'Người dùng', 'Chưa chọn số sao hoặc để trống khung nhận xét khi nhấn "Gửi đánh giá".'),
            ('', 'Hệ thống', 'Hiển thị thông báo cảnh báo "Vui lòng chọn số sao và nhập nhận xét của bạn".'),
            ('2a', 'Hệ thống', 'Phát hiện người dùng đã gửi đánh giá cho cuốn sách này trên đơn hàng tương ứng trước đó.'),
            ('', 'Hệ thống', 'Báo lỗi "Bạn đã đánh giá cuốn sách này rồi" và không cho phép gửi lại.')
        ],
        'postcondition': 'Bản ghi đánh giá mới được lưu trong collection "reviews", điểm đánh giá trung bình và số lượt đánh giá của cuốn sách được cập nhật lại chính xác.'
    },
    {
        'num': '3.2.11',
        'code': 'UC11',
        'name': 'Thống kê doanh thu',
        'title': '3.2.11 Mô tả use case Thống kê doanh thu',
        'actor': 'Admin',
        'description': 'Use Case này cho phép Quản trị viên theo dõi các chỉ số tài chính quan trọng của cửa hàng: Tổng doanh thu, Tổng số đơn hàng, Giá trị trung bình mỗi đơn (AOV), Biểu đồ biến động doanh thu theo thời gian và Bảng xếp hạng Top sách bán chạy, Top khách hàng tiềm năng.',
        'trigger': 'Admin nhấn chọn mục "Thống kê" từ thanh điều hướng hoặc menu Bảng điều khiển quản trị.',
        'precondition': 'Người dùng đã đăng nhập hệ thống với tư cách Quản trị viên (Admin).',
        'main_flow': [
            (1, 'Admin', 'Nhấn chọn mục "Thống kê" trên thanh menu quản trị.'),
            (2, 'Hệ thống', 'Mở màn hình Thống kê (StatisticActivity), mặc định chọn mốc thời gian là "Theo Tuần" (WEEK).'),
            (3, 'Hệ thống', 'Truy vấn Firestore lấy tất cả các đơn hàng có trạng thái "Đã giao" (DELIVERED) trong khoảng thời gian tương ứng, đồng thời lấy danh sách Top 10 sách bán chạy nhất và Top 10 khách hàng mua nhiều nhất.'),
            (4, 'Hệ thống', 'Tính toán tổng doanh thu, tổng số đơn, giá trị đơn trung bình (AOV = Doanh thu / Số đơn) và gom nhóm doanh thu theo từng mốc thời gian (theo giờ trong ngày / ngày trong tuần / ngày trong tháng / tháng trong năm).'),
            (5, 'Hệ thống', 'Hiển thị 3 thẻ số liệu tổng quan (Doanh thu, Số đơn, AOV), vẽ biểu đồ đường LineChart trực quan và cập nhật 2 danh sách bảng xếp hạng Top sách, Top khách hàng.'),
            (6, 'Admin', 'Nhấn chọn chuyển đổi giữa các chip thời gian: Ngày / Tuần / Tháng / Năm.'),
            (7, 'Hệ thống', 'Tự động tính toán lại dữ liệu và cập nhật mượt mà biểu đồ cùng các chỉ số tài chính tương ứng theo thời gian thực.')
        ],
        'alt_flow': [
            ('1a', 'Hệ thống', 'Không có đơn hàng nào hoàn tất trong khoảng thời gian được chọn.'),
            ('', 'Hệ thống', 'Hiển thị doanh thu là 0 đ, số đơn là 0 và biểu đồ đường phẳng ở mức 0.')
        ],
        'postcondition': 'Các số liệu thống kê kinh doanh và biểu đồ tăng trưởng được kết xuất chính xác phục vụ công tác điều hành.'
    },
    {
        'num': '3.2.12',
        'code': 'UC12',
        'name': 'Quản lý tác giả và Nhà xuất bản',
        'title': '3.2.12 Mô tả use case Quản lý tác giả và Nhà xuất bản',
        'actor': 'Admin',
        'description': 'Use Case này cho phép Quản trị viên theo dõi danh sách, tìm kiếm theo tên, thêm mới, cập nhật thông tin và thực hiện xóa an toàn (kiểm tra ràng buộc toàn vẹn dữ liệu với các đầu sách) đối với Tác giả và Nhà xuất bản.',
        'trigger': 'Admin nhấn chọn mục "Quản lý tác giả" hoặc "Quản lý nhà xuất bản" trên menu quản trị.',
        'precondition': 'Người dùng đã đăng nhập hệ thống với quyền Quản trị viên (Admin).',
        'main_flow': [
            (1, 'Admin', 'Nhấn chọn mục "Quản lý tác giả" (hoặc "Quản lý NXB") trên menu quản trị.'),
            (2, 'Hệ thống', 'Mở màn hình quản lý tương ứng (ManageAuthorActivity / ManagePublisherActivity), kết nối Firestore Realtime hiển thị danh sách mục hiện có kèm ảnh đại diện/logo.'),
            (3, 'Admin', 'Có thể nhập từ khóa vào ô tìm kiếm để lọc nhanh danh sách theo tên ngay trong bộ nhớ cache.'),
            (4, 'Admin', 'Nhấn nút (+) để thêm mới hoặc nhấn icon "Sửa" trên một mục để cập nhật.'),
            (5, 'Hệ thống', 'Hiển thị form dialog (AdminEntityFormDialog) nhập Tên, Mô tả/Tiểu sử và chọn ảnh.'),
            (6, 'Admin', 'Nhập thông tin hợp lệ, chọn ảnh đại diện từ thiết bị và nhấn "Lưu".'),
            (7, 'Hệ thống', 'Kiểm tra hợp lệ tên không để trống, tải ảnh lên Firebase Storage (thư mục "author_avatars/" hoặc "publisher_logos/") và lưu bản ghi vào Firestore.'),
            (8, 'Hệ thống', 'Hiển thị thông báo "Đã lưu", danh sách trên giao diện tự động cập nhật lại.')
        ],
        'alt_flow': [
            ('1a', 'Admin', 'Nhấn nút "Xóa" một tác giả (hoặc nhà xuất bản).'),
            ('', 'Hệ thống', 'Truy vấn Firestore kiểm tra ràng buộc khóa ngoại trong collection "books" xem có cuốn sách nào đang gắn với tác giả/NXB này không.'),
            ('', 'Hệ thống', 'Nếu phát hiện có sách đang liên kết: Báo lỗi "Không thể xóa vì đang có sản phẩm thuộc Tác giả/Nhà xuất bản này" và hủy thao tác xóa.'),
            ('', 'Hệ thống', 'Nếu không có sách nào sử dụng: Hiển thị dialog xác nhận xóa; khi Admin đồng ý, hệ thống xóa tài liệu khỏi Firestore và cập nhật lại danh sách.')
        ],
        'postcondition': 'Thông tin Tác giả và Nhà xuất bản được cập nhật đầy đủ, đảm bảo tính toàn vẹn dữ liệu hệ thống.'
    }
]

print(f"Loaded {len(USE_CASES)} use cases successfully.")
