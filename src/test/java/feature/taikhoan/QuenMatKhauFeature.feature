Feature: Quên mật khẩu

  Scenario: Kiểm tra UI Quên mật khẩu
    When Người dùng nhấp vào "Tài khoản"
    When Người dùng nhấp vào "Đăng xuất"
    When Người dùng nhấp vào "Đồng ý"
    When Người dùng nhấp vào "Quên mật khẩu"
    Then Người dùng kiểm tra các văn bản sau hiển thị đúng:
      | Khôi phục mật khẩu                                          |
      | Vui lòng nhập tên đăng nhập tài khoản để khôi phục mật khẩu |
      | Hủy bỏ                                                      |

  Scenario: Kiểm tra nhập đúng tên tài khoản
    When Người dùng nhấp vào "Tài khoản"
    When Người dùng nhấp vào "Đăng xuất"
    When Người dùng nhấp vào "Đồng ý"
    When Người dùng nhấp vào "Quên mật khẩu"
    When Người dùng nhập "0354078931" vào trường có placeholder "Nhập tên tài khoản"
    When Người dùng nhấp vào "Gửi đi"
    Then Người dùng kiểm tra các văn bản sau hiển thị đúng:
      | Đã cấp lại mật khẩu mới!\nVui lòng kiểm tra email đăng ký tài khoản |
      | Quên mật khẩu                                                       |
      | Trở về                                                              |

  Scenario: Kiểm tra nhập sai tên tài khoản
    When Người dùng nhấp vào "Tài khoản"
    When Người dùng nhấp vào "Đăng xuất"
    When Người dùng nhấp vào "Đồng ý"
    When Người dùng nhấp vào "Quên mật khẩu"
    When Người dùng nhập "123" vào trường có placeholder "Nhập tên tài khoản"
    When Người dùng nhấp vào "Gửi đi"
    Then Người dùng kiểm tra các văn bản sau hiển thị đúng:
      | Tài khoản không tồn tại |
      | Quên mật khẩu                               |
      | Trở về                                      |



