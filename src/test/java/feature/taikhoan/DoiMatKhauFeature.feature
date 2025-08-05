Feature: Đổi mật khẩu

  Scenario: Kiểm tra UI Option "Đổi mật khẩu"
    Given Người dùng nhấp vào "Tài khoản"
    Given Người dùng nhấp vào "Đổi mật khẩu"
    Then Người dùng kiểm tra các văn bản sau hiển thị đúng:
      | Mật khẩu cũ *               |
      | Mật khẩu mới *              |
      | Xác nhận mật khẩu *         |
      | Hủy                         |
      | Cập nhật                    |


  Scenario: Thay đổi mật khẩu thất bại do mật khẩu cũ không đúng
    Given Người dùng nhấp vào "Tài khoản"
    Given Người dùng nhấp vào "Đổi mật khẩu"
    When Người dùng nhập "MatKhauSai123@" vào trường có placeholder "Vui lòng nhập mật khẩu cũ"
    And Người dùng nhập "MatKhauMoi456@" vào trường có placeholder "Vui lòng nhập mật khẩu mới"
    And Người dùng nhập "MatKhauMoi456@" vào trường có placeholder "Vui lòng nhập xác nhận mật khẩu"
    And Người dùng nhấp vào "Cập nhật"
    Then Người dùng kiểm tra văn bản "Mật khẩu cũ không đúng" hiển thị

  Scenario: Thay đổi mật khẩu thất bại do mật khẩu mới và xác nhận không khớp
    Given Người dùng nhấp vào "Tài khoản"
    Given Người dùng nhấp vào "Đổi mật khẩu"
    When Người dùng nhập "MatKhauCu123@" vào trường có placeholder "Vui lòng nhập mật khẩu cũ"
    And Người dùng nhập "MatKhauMoi456@" vào trường có placeholder "Vui lòng nhập mật khẩu mới"
    And Người dùng nhập "MatKhauKhac789@" vào trường có placeholder "Vui lòng nhập xác nhận mật khẩu"
    And Người dùng nhấp vào "Cập nhật"
    Then Người dùng kiểm tra văn bản "Xác nhận mật khẩu không khớp với mật khẩu mới" hiển thị

  Scenario: Thay đổi mật khẩu thất bại do để trống mật khẩu cũ
    Given Người dùng nhấp vào "Tài khoản"
    Given Người dùng nhấp vào "Đổi mật khẩu"
    When Người dùng nhập "" vào trường có placeholder "Vui lòng nhập mật khẩu cũ"
    And Người dùng nhập "MatKhauMoi456@" vào trường có placeholder "Vui lòng nhập mật khẩu mới"
    And Người dùng nhập "MatKhauMoi456@" vào trường có placeholder "Vui lòng nhập xác nhận mật khẩu"
    And Người dùng nhấp vào "Cập nhật"
    Then Người dùng kiểm tra văn bản "Vui lòng nhập mật khẩu cũ" hiển thị

  Scenario: Thay đổi mật khẩu thất bại do để trống mật khẩu mới
    Given Người dùng nhấp vào "Tài khoản"
    Given Người dùng nhấp vào "Đổi mật khẩu"
    When Người dùng nhập "MatKhauCu123@" vào trường có placeholder "Vui lòng nhập mật khẩu cũ"
    And Người dùng nhập "" vào trường có placeholder "Vui lòng nhập mật khẩu mới"
    And Người dùng nhập "MatKhauMoi456@" vào trường có placeholder "Vui lòng nhập xác nhận mật khẩu"
    And Người dùng nhấp vào "Cập nhật"
    Then Người dùng kiểm tra văn bản "Vui lòng nhập mật khẩu mới" hiển thị

  Scenario: Thay đổi mật khẩu thất bại do để trống xác nhận mật khẩu
    Given Người dùng nhấp vào "Tài khoản"
    Given Người dùng nhấp vào "Đổi mật khẩu"
    When Người dùng nhập "MatKhauCu123@" vào trường có placeholder "Vui lòng nhập mật khẩu cũ"
    And Người dùng nhập "MatKhauMoi456@" vào trường có placeholder "Vui lòng nhập mật khẩu mới"
    And Người dùng nhập "" vào trường có placeholder "Vui lòng nhập xác nhận mật khẩu"
    And Người dùng nhấp vào "Cập nhật"
    Then Người dùng kiểm tra văn bản "Vui lòng nhập xác nhận mật khẩu" hiển thị

  Scenario: Thay đổi mật khẩu thất bại do hủy thao tác
    Given Người dùng nhấp vào "Tài khoản"
    Given Người dùng nhấp vào "Đổi mật khẩu"
    And Người dùng nhấp vào "Hủy"
    Then Người dùng kiểm tra văn bản "Thay đổi mật khẩu không hiển thị" không hiển thị

  Scenario: Thay đổi mật khẩu thất bại do mật khẩu mới dưới 8 ký tự
    Given Người dùng nhấp vào "Tài khoản"
    Given Người dùng nhấp vào "Đổi mật khẩu"
    When Người dùng nhập "MatKhauCu123@" vào trường có placeholder "Vui lòng nhập mật khẩu cũ"
    And Người dùng nhập "MATAA123" vào trường có placeholder "Vui lòng nhập mật khẩu mới"
    And Người dùng nhập "MATAA123" vào trường có placeholder "Vui lòng nhập xác nhận mật khẩu"
    And Người dùng nhấp vào "Cập nhật"
    Then Người dùng kiểm tra văn bản "Mật khẩu mới phải chứa ít nhất 1 chữ viết thường!" hiển thị

  Scenario: Thay đổi mật khẩu thất bại do mật khẩu mới thiếu ký tự đặc biệt
    Given Người dùng nhấp vào "Tài khoản"
    Given Người dùng nhấp vào "Đổi mật khẩu"
    When Người dùng nhập "MatKhauCu123@" vào trường có placeholder "Vui lòng nhập mật khẩu cũ"
    And Người dùng nhập "MatKhauMoi456" vào trường có placeholder "Vui lòng nhập mật khẩu mới"
    And Người dùng nhập "MatKhauMoi456" vào trường có placeholder "Vui lòng nhập xác nhận mật khẩu"
    And Người dùng nhấp vào "Cập nhật"
    Then Người dùng kiểm tra văn bản "Mật khẩu mới phải chứa ít nhất 1 ký tự đặc biệt!" hiển thị

  Scenario: Thay đổi mật khẩu thất bại do mật khẩu mới thiếu chữ hoa
    Given Người dùng nhấp vào "Tài khoản"
    Given Người dùng nhấp vào "Đổi mật khẩu"
    When Người dùng nhập "MatKhauCu123@" vào trường có placeholder "Vui lòng nhập mật khẩu cũ"
    And Người dùng nhập "matkhaumoi456@" vào trường có placeholder "Vui lòng nhập mật khẩu mới"
    And Người dùng nhập "matkhaumoi456@" vào trường có placeholder "Vui lòng nhập xác nhận mật khẩu"
    And Người dùng nhấp vào "Cập nhật"
    Then Người dùng kiểm tra văn bản "Mật khẩu mới phải chứa ít nhất 1 chữ viết hoa!" hiển thị

  Scenario: Thay đổi mật khẩu thất bại do mật khẩu mới thiếu số
    Given Người dùng nhấp vào "Tài khoản"
    Given Người dùng nhấp vào "Đổi mật khẩu"
    When Người dùng nhập "MatKhauCu123@" vào trường có placeholder "Vui lòng nhập mật khẩu cũ"
    And Người dùng nhập "MatKhauMoiABC@" vào trường có placeholder "Vui lòng nhập mật khẩu mới"
    And Người dùng nhập "MatKhauMoiABC@" vào trường có placeholder "Vui lòng nhập xác nhận mật khẩu"
    And Người dùng nhấp vào "Cập nhật"
    Then Người dùng kiểm tra văn bản "Mật khẩu mới phải chứa ít nhất 1 chữ số!" hiển thị

  Scenario: Thay đổi mật khẩu thất bại do mật khẩu mới trùng với mật khẩu cũ
    Given Người dùng nhấp vào "Tài khoản"
    Given Người dùng nhấp vào "Đổi mật khẩu"
    When Người dùng nhập "Pc07@1234567" vào trường có placeholder "Vui lòng nhập mật khẩu cũ"
    And Người dùng nhập "Pc07@1234567" vào trường có placeholder "Vui lòng nhập mật khẩu mới"
    And Người dùng nhập "Pc07@1234567" vào trường có placeholder "Vui lòng nhập xác nhận mật khẩu"
    And Người dùng nhấp vào "Cập nhật"
    Then Người dùng kiểm tra văn bản "Mật khẩu mới không được trùng với mật khẩu cũ" hiển thị

  Scenario: Thay đổi mật khẩu thất bại do xác nhận mật khẩu thiếu ký tự đặc biệt
    Given Người dùng nhấp vào "Tài khoản"
    Given Người dùng nhấp vào "Đổi mật khẩu"
    When Người dùng nhập "MatKhauCu123@" vào trường có placeholder "Vui lòng nhập mật khẩu cũ"
    And Người dùng nhập "MatKhauMoi456@" vào trường có placeholder "Vui lòng nhập mật khẩu mới"
    And Người dùng nhập "MatKhauMoi456" vào trường có placeholder "Vui lòng nhập xác nhận mật khẩu"
    And Người dùng nhấp vào "Cập nhật"
    Then Người dùng kiểm tra văn bản "Xác nhận mật khẩu phải chứa ít nhất 1 ký tự đặc biệt!" hiển thị






