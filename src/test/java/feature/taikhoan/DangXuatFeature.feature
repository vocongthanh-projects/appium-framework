Feature: Đăng xuất

  Scenario: Kiểm tra UI đăng xuất
    When Người dùng nhấp vào "Tài khoản"
    When Người dùng nhấp vào "Đăng xuất"
    Then Người dùng kiểm tra các văn bản sau hiển thị đúng:
    |Xác nhận đăng xuất|
    |Bạn có chắc muốn đăng xuất?|

  Scenario: Kiểm tra Nhấn nút hủy đăng xuất thành công
    When Người dùng nhấp vào "Tài khoản"
    When Người dùng nhấp vào "Đăng xuất"
    Then Người dùng kiểm tra văn bản "Xác nhận đăng xuất" không hiển thị
    Then Người dùng kiểm tra văn bản "Trang chủ" hiển thị

  Scenario: Kiểm tra Nhấn nút đồng ý đăng xuất thành công
    When Người dùng nhấp vào "Tài khoản"
    When Người dùng nhấp vào "Đăng xuất"
    When Người dùng nhấp vào "Đồng ý"
    Then Người dùng kiểm tra văn bản "Trang chủ" không hiển thị


