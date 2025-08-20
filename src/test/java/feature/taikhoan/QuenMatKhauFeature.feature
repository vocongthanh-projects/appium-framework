#Feature: Quên mật khẩu
#
#  Scenario: Kiểm tra UI Quên mật khẩu
#    When Người dùng nhấp vào "Tài khoản"
#    When Người dùng nhấp vào "Đăng xuất"
#    When Người dùng nhấp vào "Đồng ý"
#    When Người dùng nhấp vào "Quên mật khẩu"
#    Then Người dùng kiểm tra các văn bản sau hiển thị đúng:
#      | Khôi phục mật khẩu              |
#      | Vui lòng nhập số điện thoại đăng nhập tài khoản để khôi phục mật khẩu              |
#      | Hủy bỏ         |
#
#  Scenario: Kiểm tra nhập đúng số điện thoại
#    When Người dùng nhấp vào "Tài khoản"
#    When Người dùng nhấp vào "Đăng xuất"
#    When Người dùng nhấp vào "Đồng ý"
#    When Người dùng nhấp vào "Quên mật khẩu"
#    When Người dùng nhập "0354078931" vào trường có placeholder "Nhập số điện thoại"
#    When Người dùng nhấp vào "Gửi đi"
#    Then Người dùng kiểm tra các văn bản sau hiển thị đúng:
#      |Đã cấp lại mật khẩu mới!. Vui lòng kiểm tra email đăng ký tài khoản|
#      |Quên mật khẩu|
#      |Trở về|
#
#  Scenario: Kiểm tra nhập sai số điện thoại
#    When Người dùng nhấp vào "Tài khoản"
#    When Người dùng nhấp vào "Đăng xuất"
#    When Người dùng nhấp vào "Đồng ý"
#    When Người dùng nhấp vào "Quên mật khẩu"
#    When Người dùng nhập "123" vào trường có placeholder "Nhập số điện thoại"
#    When Người dùng nhấp vào "Gửi đi"
#    Then Người dùng kiểm tra các văn bản sau hiển thị đúng:
#    |Số điện thoại chưa đăng ký email." hiển thị|
#    |Quên mật khẩu|
#    |Trở về|
#
#
#
