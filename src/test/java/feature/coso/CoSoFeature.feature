Feature: Danh sách cơ sở

  Scenario: Kiểm tra UI Thêm mới cơ sở
    And Người dùng nhấp vào "Cơ sở"
    And Người dùng nhấn vào thêm mới cơ sở
    Then Người dùng kiểm tra các văn bản sau hiển thị đúng:
      | Số điện thoại cơ sở * |
      | Tên cơ sở *           |
      | Kinh độ *             |
      | Vĩ độ *               |
      | Tỉnh/TP *          |
      | Xã/Phường *           |
      | Đường/Phố/Thôn/Tổ     |
      | Số                    |
      | Kích hoạt             |


  Scenario: Thêm mới Cơ sở thành công (Happy Case)
    And Người dùng nhấp vào "Cơ sở"
    And Người dùng nhấn vào thêm mới cơ sở
    And Người dùng nhập số điện thoại ngẫu nhiên vào trường có placeholder "Nhập số điện thoại cơ sở"
    And Người dùng nhập "Cơ sở Mobile UI" vào trường có placeholder "Nhập tên cơ sở"
    And Người dùng nhập "Đường Phúc Xá" vào trường có placeholder "Nhập Đường/Phố/Thôn/Tổ"
    And Người dùng nhập "175" vào trường có placeholder "Nhập số"
    And Người dùng nhấp vào "Kích hoạt"
    And Người dùng nhấp vào "Lưu"
    Then Người dùng kiểm tra văn bản "Thông báo" hiển thị
    Then Người dùng kiểm tra văn bản "Đăng ký cơ sở thành công" hiển thị
    And Người dùng nhấp vào "Đóng"
    Then Người dùng kiểm tra các văn bản sau hiển thị đúng:
      | Tên cơ sở:                                        |
      | Cơ sở Mobile UI                                   |
      | Địa chỉ:                                          |
      | 175,  Đường Phúc Xá,  Phường Cầu Ông Lãnh, Thành phố Hồ Chí Minh |
      | Lĩnh vực:                                         |
      | Danh sách liên hệ                                 |
      | Tài liệu kèm theo                                 |
    And Người dùng nhấn vào Xem vị trí trên bản đồ
    Then Người dùng kiểm tra các văn bản sau hiển thị đúng:
      | Cơ sở Mobile UI                                   |
      | 175,  Đường Phúc Xá,  Phường Cầu Ông Lãnh, Thành phố Hồ Chí Minh |
      | Tiêu đề:                                          |
      | Cơ sở                                             |
      | Lĩnh vực:                                         |
      | Thông tin liên lạc:                               |


  Scenario: Validate Cơ sở
    And Người dùng nhấp vào "Cơ sở"
    And Người dùng nhấn vào thêm mới cơ sở
    And Người dùng nhấp vào "Lưu"
    Then Người dùng kiểm tra văn bản "Vui lòng nhập số điện thoại" hiển thị
    And Người dùng nhập số điện thoại ngẫu nhiên vào trường có placeholder "Nhập số điện thoại cơ sở"
    And Người dùng nhấp vào "Lưu"
    Then Người dùng kiểm tra văn bản "Vui lòng nhập tên cơ sở" hiển thị


  Scenario: Validate SDT đã tồn tại
    And Người dùng nhấp vào "Cơ sở"
    And Người dùng nhấn vào thêm mới cơ sở
    And Người dùng nhập "0354078999" vào trường có placeholder "Nhập số điện thoại cơ sở"
    And Người dùng nhập "Cơ sở Mobile UI" vào trường có placeholder "Nhập tên cơ sở"
    And Người dùng nhấp vào "Lấy tọa độ hiện tại"
    And Người dùng nhập "Đường Phúc Xá" vào trường có placeholder "Nhập Đường/Phố/Thôn/Tổ"
    And Người dùng nhập "175" vào trường có placeholder "Nhập số"
    And Người dùng nhấp vào "Kích hoạt"
    And Người dùng nhấp vào "Lưu"
    Then Người dùng kiểm tra văn bản "Số điện thoại đã tồn tại không được thêm mới! Vui lòng nhấn vào nút (Tìm kiếm) để biết thêm thông tin" hiển thị

  Scenario: Kiểm tra cơ sở đã tồn tại khi nhấn nút tìm kiếm
    And Người dùng nhấp vào "Cơ sở"
    And Người dùng nhấn vào thêm mới cơ sở
    And Người dùng nhập "0354078999" vào trường có placeholder "Nhập số điện thoại cơ sở"
    And Người dùng nhấp vào "Tìm kiếm"
    And Người dùng kiểm tra văn bản "Số điện thoại này đã đăng ký cho cơ sở Cơ sở Mobile UI. Bạn có muốn chỉnh sửa cơ sở này không?" hiển thị
    And Người dùng nhấp vào "Xác nhận"
    Then Người dùng kiểm tra các văn bản sau hiển thị đúng:
    |Cơ sở Mobile UI|
    |106.68534|
    |10.759247|
    |Thành phố Hồ Chí Minh|
    |Phường Cầu Ông Lãnh|
    |Đường Phúc Xá|
    |175|

  Scenario: Kiểm tra cơ sở chưa tồn tại khi nhấn nút tìm kiếm
    And Người dùng nhấp vào "Cơ sở"
    And Người dùng nhấn vào thêm mới cơ sở
    And Người dùng nhập số điện thoại ngẫu nhiên vào trường có placeholder "Nhập số điện thoại cơ sở"
    And Người dùng nhấp vào "Tìm kiếm"
    And Người dùng kiểm tra văn bản "Số điện thoại này chưa đăng ký cho cơ sở nào. Bạn có muốn thêm cơ sở mới không?" hiển thị


