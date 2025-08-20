Feature: Nguồn lực

  Scenario: Kiểm tra UI combobox Nguồn lực
    Given Người dùng nhấn vào filter Nguồn lực Trang chủ
    Then Người dùng kiểm tra các văn bản sau hiển thị đúng:
      | Vui lòng chọn loại nguồn lực |
      | Nguồn nước tự nhiên          |
      | Trụ nước                     |
      | Bể nước                      |
      | Cơ sở                        |
      | Hủy                          |
      | Chọn                         |

  Scenario: Kiểm tra UI Thêm mới Nguồn lực
    Given Người dùng nhấn vào thêm mới Nguồn lực
    Then Người dùng kiểm tra các văn bản sau hiển thị đúng:
      | Thêm mới nguồn lực  |
      | Chọn loại nguồn lực |
      | Hủy                 |
      | Xác nhận            |

  Scenario: Kiểm tra UI Option Thêm mới Nguồn lực
    Given Người dùng nhấn vào thêm mới Nguồn lực
    When Người dùng nhấp vào "Chọn loại nguồn lực"
    Then Người dùng kiểm tra các văn bản sau hiển thị đúng:
      | Vui lòng chọn |
      | Nguồn nước    |
      | Hủy           |
      | Chọn          |


  Scenario: Kiểm tra UI Option "Nguồn nước"
    Given Người dùng nhấn vào thêm mới Nguồn lực
    When Người dùng nhấp vào "Chọn loại nguồn lực"
    And Người dùng nhấp vào "Nguồn nước"
    And Người dùng nhấp vào "Xác nhận"
    Then Người dùng kiểm tra các văn bản sau hiển thị đúng:
      | Loại nguồn nước *        |
      | Phân loại chi tiết *     |
      | Loại đơn vị quản lý *   |
      | Tên đơn vị quản lý *      |
      | Tên nguồn nước           |
      | Trữ lượng/Lưu lượng (m3) |
      | Kinh độ *                |
      | Vĩ độ *                  |
      | Tỉnh/TP *             |
    When Người dùng cuộn xuống một lần
    Then Người dùng kiểm tra các văn bản sau hiển thị đúng:
      | Xã/Phường *       |
      | Đường/Phố/Thôn/Tổ |
      | Số                |
      | Địa chỉ           |
    When Người dùng cuộn xuống một lần
    Then Người dùng kiểm tra các văn bản sau hiển thị đúng:
      | Khả năng cấp nước * |
      | Có                |
      | Không             |
      | Ghi chú             |
      | Trạng thái        |
      | Kích hoạt         |
      | Hủy               |
      | Lưu               |

  Scenario: Validate nguồn nước thành công (Happy Case)
    Given Người dùng nhấn vào thêm mới Nguồn lực
    When Người dùng nhấp vào "Chọn loại nguồn lực"
    And Người dùng nhấp vào "Nguồn nước"
    And Người dùng nhấp vào "Xác nhận"
    And Người dùng nhấp vào "Lưu"
    And Người dùng nhấp vào "Trụ nước"
    Then Người dùng kiểm tra văn bản "Vui lòng chọn loại nguồn nước" hiển thị
    And Người dùng nhấp vào "Lưu"
    And Người dùng nhấp vào "Trụ nổi 3 họng"
    Then Người dùng kiểm tra văn bản "Vui lòng chọn phân loại chi tiết" hiển thị
    And Người dùng nhấp vào "Lưu"
    And Người dùng nhấp vào "Ủy ban nhân dân"
    Then Người dùng kiểm tra văn bản "Vui lòng chọn loại đơn vị quản lý" hiển thị
    And Người dùng nhập "Trụ nước UI Mobile" vào trường có placeholder "Nhập tên nguồn nước"
    And Người dùng nhấp vào "Lấy tọa độ hiện tại"
    And Người dùng cuộn xuống một lần
    And Người dùng nhập "Đường Phúc Xá" vào trường có placeholder "Nhập Đường/Phố/Thôn/Tổ"
    And Người dùng nhập "175" vào trường có placeholder "Nhập số"
    And Người dùng cuộn xuống một lần
    And Người dùng nhấp vào "Lưu"
    Then Người dùng kiểm tra văn bản "Vui lòng chọn khả năng cấp nước" hiển thị
    And Người dùng nhấp vào "Có"
    And Người dùng nhấp vào "Lưu"
    Then Người dùng kiểm tra văn bản "Vui lòng chọn phương tiện có thể cấp nước" hiển thị
    And Người dùng cuộn xuống một lần
    And Người dùng nhấp vào "Xe chữa cháy"
    And Người dùng nhấp vào "Lưu"
    Then Người dùng kiểm tra văn bản "Đã thêm mới nguồn lực thành công!" hiển thị
    And Người dùng nhấp vào "Đóng"

  Scenario: Thêm mới Trụ nước thành công (Happy Case)
    Given Người dùng nhấn vào thêm mới Nguồn lực
    When Người dùng nhấp vào "Chọn loại nguồn lực"
    And Người dùng nhấp vào "Nguồn nước"
    And Người dùng nhấp vào "Xác nhận"
    And Người dùng chọn "Trụ nước" từ dropdown có placeholder "Chọn loại nguồn nước"
    And Người dùng chọn "Trụ nổi 3 họng" từ dropdown có placeholder "Chọn phân loại chi tiết"
    And Người dùng chọn "Ủy ban nhân dân" từ dropdown có placeholder "Chọn đơn vị quản lý"
    And Người dùng nhập "Trụ nước UI Mobile" vào trường có placeholder "Nhập tên nguồn nước"
    And Người dùng nhấp vào "Lấy tọa độ hiện tại"
    And Người dùng cuộn xuống một lần
    And Người dùng nhập "Đường Phúc Xá" vào trường có placeholder "Nhập Đường/Phố/Thôn/Tổ"
    And Người dùng nhập "175" vào trường có placeholder "Nhập số"
    And Người dùng cuộn xuống một lần
    And Người dùng nhấp vào "Có"
    And Người dùng cuộn xuống một lần
    And Người dùng nhấp vào "Xe chữa cháy"
    And Người dùng nhập "Không có ghi chú" vào trường có placeholder "Nhập ghi chú"
    And Người dùng nhấp vào "Lưu"
    Then Người dùng kiểm tra văn bản "Đã thêm mới nguồn lực thành công!" hiển thị
    And Người dùng nhấp vào "Đóng"
    Given Người dùng nhấp vào "Nguồn lực"
    And Người dùng nhấp vào "Xem vị trí"
    Then Người dùng kiểm tra các văn bản sau hiển thị đúng:
      | Trụ nước UI Mobile                                      |
      | 175, Đường Phúc Xá, Phường Cầu Ông Lãnh, Thành phố Hồ Chí Minh |
      | Tiêu đề:                                                |
      | Thông tin liên lạc:                                     |

  Scenario: Kiểm tra dữ liệu sau khi thêm mới Trụ nước thành công (Happy Case)
    Given Người dùng nhấp vào "Nguồn lực"
    When Người dùng nhập "Trụ nước UI Mobile" vào trường có placeholder "Nhập thông tin tìm kiếm...."
    When Người dùng nhấp vào "Chọn Tỉnh/TP"
    When Người dùng cuộn đến phần tử có văn bản "Thành phố Hồ Chí Minh"
    When Người dùng nhấp vào "Thành phố Hồ Chí Minh"
    When Người dùng nhấp vào "Chọn xã/phường"
    When Người dùng cuộn đến phần tử có văn bản "Phường Cầu Ông Lãnh"
    When Người dùng nhấp vào "Phường Cầu Ông Lãnh"
    Then Người dùng kiểm tra các văn bản sau hiển thị đúng:
      | Địa chỉ:                                                |
      | 175, Đường Phúc Xá, Phường Cầu Ông Lãnh, Thành phố Hồ Chí Minh |
      | Loại nguồn nước:                                        |
      | Trụ nước                                                |
      | Phân loại chi tiết:                                     |
      | Trụ nổi 3 họng                                          |
      | Đã kích hoạt                                            |
    When Người dùng nhấp vào "Cập nhật"
    Then Người dùng kiểm tra các văn bản sau hiển thị đúng:
      | Trụ nước           |
      | Trụ nổi 3 họng     |
      | Ủy ban nhân dân    |
      | Trụ nước UI Mobile |
      | Thành phố Hồ Chí Minh   |
    And Người dùng cuộn xuống một lần
    Then Người dùng kiểm tra các văn bản sau hiển thị đúng:
      | Phường Cầu Ông Lãnh |
      | Đường Phúc Xá  |
      | 175            |
    And Người dùng cuộn xuống một lần
    Then Người dùng kiểm tra các văn bản sau hiển thị đúng:
      | Có               |
      | Xe chữa cháy     |
      | Không có ghi chú |
      | Kích hoạt        |

  Scenario: Kiểm tra cập nhật Trụ nước thành công (Happy Case)
    Given Người dùng nhấp vào "Nguồn lực"
    When Người dùng nhấp vào "Cập nhật"
    And Người dùng chọn "Nguồn nước tự nhiên" từ dropdown có placeholder "Trụ nước"
    And Người dùng chọn "Bến lấy nước" từ dropdown có placeholder "Chọn phân loại chi tiết"
    And Người dùng nhập "Trụ nước update UI Mobile" vào trường có placeholder "Trụ nước UI Mobile"
    And Người dùng nhập "123" vào trường có placeholder "0"
    And Người dùng nhấp vào "Lấy tọa độ hiện tại"
    And Người dùng cuộn xuống một lần
    And Người dùng nhập "Đường Phúc Xá" vào trường có placeholder "Nhập Đường/Phố/Thôn/Tổ"
    And Người dùng nhập "175" vào trường có placeholder "Nhập số"
    And Người dùng cuộn xuống một lần
    And Người dùng nhấp vào "Không"
    And Người dùng nhập "Không có nguyên nhân" vào trường có placeholder "Nhập nguyên nhân"
    And Người dùng cuộn xuống một lần
    And Người dùng nhấp vào "Lưu"
    Then Người dùng kiểm tra văn bản "Đã cập nhật nguồn lực thành công!" hiển thị
    And Người dùng nhấp vào "Đóng"
    When Người dùng nhấp vào "Cập nhật"
    Then Người dùng kiểm tra các văn bản sau hiển thị đúng:
      | Nguồn nước tự nhiên       |
      | Bến lấy nước              |
      | Ủy ban nhân dân           |
      | Trụ nước update UI Mobile |
      | 123                       |
    And Người dùng cuộn xuống một lần
    Then Người dùng kiểm tra các văn bản sau hiển thị đúng:
      | Thành phố Hồ Chí Minh |
      | Phường Cầu Ông Lãnh   |
      | Đường Phúc Xá    |
      | 175              |
    And Người dùng cuộn xuống một lần
    Then Người dùng kiểm tra các văn bản sau hiển thị đúng:
      | Không có nguyên nhân |
      | Không có ghi chú     |
      | Kích hoạt            |

  Scenario: Kiểm tra Xóa Trụ nước thành công (Happy Case)
    Given Người dùng nhấp vào "Nguồn lực"
    Then Người dùng kiểm tra văn bản "Trụ nước update UI Mobile" hiển thị
    When Người dùng nhấp vào "Xóa"
    Then Người dùng kiểm tra các văn bản sau hiển thị đúng:
      | Thông báo                              |
      | Xác nhận xóa nguồn lực \nTrụ nước update UI Mobile |
      | Không                                  |
    When Người dùng nhấp vào "Xác nhận"
    Then Người dùng kiểm tra văn bản "Trụ nước update UI Mobile" không hiển thị


  Scenario: Thêm mới Nguồn nước tự nhiên thành công (Happy Case)
    Given Người dùng nhấn vào thêm mới Nguồn lực
    When Người dùng nhấp vào "Chọn loại nguồn lực"
    And Người dùng nhấp vào "Nguồn nước"
    And Người dùng nhấp vào "Xác nhận"
    And Người dùng chọn "Nguồn nước tự nhiên" từ dropdown có placeholder "Chọn loại nguồn nước"
    And Người dùng nhấp vào "Ao, Hồ, Đầm"
    And Người dùng chọn "Bến lấy nước" từ dropdown có placeholder "Chọn phân loại chi tiết"
    And Người dùng chọn "Ủy ban nhân dân" từ dropdown có placeholder "Chọn đơn vị quản lý"
    And Người dùng nhập "Nguồn nước tự nhiên UI Mobile" vào trường có placeholder "Nhập tên nguồn nước"
    And Người dùng nhập "123" vào trường có placeholder "Nhập trữ lượng/lưu lượng (m3)"
    And Người dùng nhấp vào "Lấy tọa độ hiện tại"
    And Người dùng cuộn xuống một lần
    And Người dùng nhập "Đường Phúc Xá" vào trường có placeholder "Nhập Đường/Phố/Thôn/Tổ"
    And Người dùng nhập "175" vào trường có placeholder "Nhập số"
    And Người dùng cuộn xuống một lần
    And Người dùng nhấp vào "Có"
    And Người dùng cuộn xuống một lần
    And Người dùng nhấp vào "Xe chữa cháy"
    And Người dùng nhập "Không có ghi chú" vào trường có placeholder "Nhập ghi chú"
    And Người dùng nhấp vào "Lưu"
    Then Người dùng kiểm tra văn bản "Đã thêm mới nguồn lực thành công!" hiển thị
    And Người dùng nhấp vào "Đóng"

  Scenario: Kiểm tra dữ liệu sau khi thêm mới Nguồn nước tự nhiên thành công (Happy Case)
    Given Người dùng nhấp vào "Nguồn lực"
    When Người dùng nhập "Nguồn nước tự nhiên UI Mobile" vào trường có placeholder "Nhập thông tin tìm kiếm...."
    When Người dùng nhấp vào "Chọn Tỉnh/TP"
    When Người dùng cuộn đến phần tử có văn bản "Thành phố Hồ Chí Minh"
    When Người dùng nhấp vào "Thành phố Hồ Chí Minh"
    Then Người dùng kiểm tra các văn bản sau hiển thị đúng:
      | Địa chỉ:                                                |
      | 175, Đường Phúc Xá, Phường Cầu Ông Lãnh, Thành phố Hồ Chí Minh |
      | Loại nguồn nước:                                        |
      | Nguồn nước tự nhiên                                     |
      | Phân loại chi tiết:                                     |
      | Bến lấy nước                                              |
      | Đã kích hoạt                                            |
    When Người dùng nhấp vào "Cập nhật"
    Then Người dùng kiểm tra các văn bản sau hiển thị đúng:
      | Nguồn nước tự nhiên           |
      | Bến lấy nước                    |
      | Ủy ban nhân dân               |
      | Nguồn nước tự nhiên UI Mobile |
    And Người dùng cuộn xuống một lần
    Then Người dùng kiểm tra các văn bản sau hiển thị đúng:
      | Thành phố Hồ Chí Minh |
      | Phường Cầu Ông Lãnh   |
      | Đường Phúc Xá    |
      | 175              |
    And Người dùng cuộn xuống một lần
    Then Người dùng kiểm tra các văn bản sau hiển thị đúng:
      | Có           |
      | Xe chữa cháy |
      | Không có ghi chú |
      | Kích hoạt    |

  Scenario: Kiểm tra cập nhật Nguồn nước tự nhiên thành công (Happy Case)
    Given Người dùng nhấp vào "Nguồn lực"
    When Người dùng nhấp vào "Cập nhật"
    And Người dùng chọn "Bể nước" từ dropdown có placeholder "Nguồn nước tự nhiên"
    And Người dùng nhập "Nguồn nước tự nhiên update UI Mobile" vào trường có placeholder "Nguồn nước tự nhiên UI Mobile"
    And Người dùng nhấp vào "Lấy tọa độ hiện tại"
    And Người dùng cuộn xuống một lần
    And Người dùng nhập "Đường Phúc Xá" vào trường có placeholder "Nhập Đường/Phố/Thôn/Tổ"
    And Người dùng nhập "175" vào trường có placeholder "Nhập số"
    And Người dùng cuộn xuống một lần
    And Người dùng nhấp vào "Không"
    And Người dùng nhập "Không có nguyên nhân" vào trường có placeholder "Nhập nguyên nhân"
    And Người dùng cuộn xuống một lần
    And Người dùng nhấp vào "Lưu"
    Then Người dùng kiểm tra văn bản "Đã cập nhật nguồn lực thành công!" hiển thị
    And Người dùng nhấp vào "Đóng"
    When Người dùng nhấp vào "Cập nhật"
    Then Người dùng kiểm tra các văn bản sau hiển thị đúng:
      | Bể nước           |
      | Bể nước công cộng     |
      | Ủy ban nhân dân    |
      | Nguồn nước tự nhiên update UI Mobile |
      | 123 |
    And Người dùng cuộn xuống một lần
    Then Người dùng kiểm tra các văn bản sau hiển thị đúng:
      | Thành phố Hồ Chí Minh   |
      | Phường Cầu Ông Lãnh |
      | Đường Phúc Xá  |
      | 175            |
    And Người dùng cuộn xuống một lần
    Then Người dùng kiểm tra các văn bản sau hiển thị đúng:
      | Không có ghi chú |
      | Không có nguyên nhân|
      | Kích hoạt    |

  Scenario: Kiểm tra Xóa Nguồn nước tự nhiên thành công (Happy Case)
    Given Người dùng nhấp vào "Nguồn lực"
    Then Người dùng kiểm tra văn bản "Nguồn nước tự nhiên update UI Mobile" hiển thị
    When Người dùng nhấp vào "Xóa"
    Then Người dùng kiểm tra các văn bản sau hiển thị đúng:
      | Thông báo                                         |
      | Xác nhận xóa nguồn lực \nNguồn nước tự nhiên update UI Mobile |
      | Không                                             |
    When Người dùng nhấp vào "Xác nhận"
    Then Người dùng kiểm tra văn bản "Nguồn nước tự nhiên update UI Mobile" không hiển thị


  Scenario: Thêm mới Bể nước thành công (Happy Case)
    Given Người dùng nhấn vào thêm mới Nguồn lực
    When Người dùng nhấp vào "Chọn loại nguồn lực"
    And Người dùng nhấp vào "Nguồn nước"
    And Người dùng nhấp vào "Xác nhận"
    And Người dùng chọn "Bể nước" từ dropdown có placeholder "Chọn loại nguồn nước"
    And Người dùng chọn "Ủy ban nhân dân" từ dropdown có placeholder "Chọn đơn vị quản lý"
    And Người dùng nhập "Bể nước UI Mobile" vào trường có placeholder "Nhập tên nguồn nước"
    And Người dùng nhập "123" vào trường có placeholder "Nhập trữ lượng/lưu lượng (m3)"
    And Người dùng nhấp vào "Lấy tọa độ hiện tại"
    And Người dùng cuộn xuống một lần
    And Người dùng nhập "Đường Phúc Xá" vào trường có placeholder "Nhập Đường/Phố/Thôn/Tổ"
    And Người dùng nhập "175" vào trường có placeholder "Nhập số"
    And Người dùng cuộn xuống một lần
    And Người dùng nhấp vào "Có"
    And Người dùng cuộn xuống một lần
    And Người dùng nhấp vào "Xe chữa cháy"
    And Người dùng nhập "Không có ghi chú" vào trường có placeholder "Nhập ghi chú"
    And Người dùng nhấp vào "Lưu"
    Then Người dùng kiểm tra văn bản "Đã thêm mới nguồn lực thành công!" hiển thị
    And Người dùng nhấp vào "Đóng"


  Scenario: Kiểm tra dữ liệu sau khi thêm mới Bể nước thành công (Happy Case)
    Given Người dùng nhấp vào "Nguồn lực"
    When Người dùng nhập "Bể nước UI Mobile" vào trường có placeholder "Nhập thông tin tìm kiếm...."
    When Người dùng nhấp vào "Chọn Tỉnh/TP"
    When Người dùng cuộn đến phần tử có văn bản "Thành phố Hồ Chí Minh"
    When Người dùng nhấp vào "Thành phố Hồ Chí Minh"
    When Người dùng nhấp vào "Chọn xã/phường"
    When Người dùng cuộn đến phần tử có văn bản "Phường Cầu Ông Lãnh"
    When Người dùng nhấp vào "Phường Cầu Ông Lãnh"
    Then Người dùng kiểm tra các văn bản sau hiển thị đúng:
      | Địa chỉ:                                                |
      | 175, Đường Phúc Xá, Phường Cầu Ông Lãnh, Thành phố Hồ Chí Minh |
      | Loại nguồn nước:                                        |
      | Bể nước                                                 |
      | Phân loại chi tiết:                                     |
      | Bể nước công cộng                                       |
      | Đã kích hoạt                                            |
    When Người dùng nhấp vào "Cập nhật"
    Then Người dùng kiểm tra các văn bản sau hiển thị đúng:
      | Bể nước           |
      | Bể nước công cộng |
      | Ủy ban nhân dân   |
      | Bể nước UI Mobile |
    And Người dùng cuộn xuống một lần
    Then Người dùng kiểm tra các văn bản sau hiển thị đúng:
      | Thành phố Hồ Chí Minh |
      | Phường Cầu Ông Lãnh   |
      | Đường Phúc Xá    |
      | 175              |
    And Người dùng cuộn xuống một lần
    Then Người dùng kiểm tra các văn bản sau hiển thị đúng:
      | Có           |
      | Xe chữa cháy |
      | Không có ghi chú |
      | Kích hoạt    |

  Scenario: Kiểm tra cập nhật Bể nước thành công (Happy Case)
    Given Người dùng nhấp vào "Nguồn lực"
    When Người dùng nhấp vào "Cập nhật"
    And Người dùng chọn "Trụ nước" từ dropdown có placeholder "Bể nước"
    And Người dùng chọn "Trụ nổi 3 họng" từ dropdown có placeholder "Chọn phân loại chi tiết"
    And Người dùng nhập "Bể nước update UI Mobile" vào trường có placeholder "Bể nước UI Mobile"
    And Người dùng nhấp vào "Lấy tọa độ hiện tại"
    And Người dùng cuộn xuống một lần
    And Người dùng nhập "Đường Phúc Xá" vào trường có placeholder "Nhập Đường/Phố/Thôn/Tổ"
    And Người dùng nhập "175" vào trường có placeholder "Nhập số"
    And Người dùng cuộn xuống một lần
    And Người dùng nhấp vào "Không"
    And Người dùng nhập "Không có nguyên nhân" vào trường có placeholder "Nhập nguyên nhân"
    And Người dùng cuộn xuống một lần
    And Người dùng nhấp vào "Lưu"
    Then Người dùng kiểm tra văn bản "Đã cập nhật nguồn lực thành công!" hiển thị
    And Người dùng nhấp vào "Đóng"
    When Người dùng nhấp vào "Cập nhật"
    Then Người dùng kiểm tra các văn bản sau hiển thị đúng:
      | Trụ nước                 |
      | Trụ nổi 3 họng           |
      | Ủy ban nhân dân          |
      | Bể nước update UI Mobile |
    And Người dùng cuộn xuống một lần
    Then Người dùng kiểm tra các văn bản sau hiển thị đúng:
      | Thành phố Hồ Chí Minh   |
      | Phường Cầu Ông Lãnh |
      | Đường Phúc Xá |
      | 175           |
    And Người dùng cuộn xuống một lần
    Then Người dùng kiểm tra các văn bản sau hiển thị đúng:
      | Không có nguyên nhân     |
      | Không có ghi chú |
      | Kích hoạt |

  Scenario: Kiểm tra Xóa Bể nước thành công (Happy Case)
    Given Người dùng nhấp vào "Nguồn lực"
    Then Người dùng kiểm tra văn bản "Bể nước update UI Mobile" hiển thị
    When Người dùng nhấp vào "Xóa"
    Then Người dùng kiểm tra các văn bản sau hiển thị đúng:
      | Thông báo                             |
      | Xác nhận xóa nguồn lực \nBể nước update UI Mobile |
      | Không                                 |
    When Người dùng nhấp vào "Xác nhận"
    Then Người dùng kiểm tra văn bản "Bể nước update UI Mobile" không hiển thị







