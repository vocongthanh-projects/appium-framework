Feature: Nguồn lực

  Scenario: Kiểm tra UI combobox Nguồn lực
    Given Người dùng nhấn vào filter Nguồn lực Trang chủ
    Then Người dùng kiểm tra các văn bản sau hiển thị đúng:
      | Vui lòng chọn loại nguồn lực         |
      | Nguồn nước tự nhiên                  |
      | Trụ nước                             |
      | Đơn vị Điện lực hỗ trợ               |
      | Đơn vị Y tế hỗ trợ                   |
      | Đơn vị Công an cấp xã, huyện         |
      | Đơn vị phương tiện                   |
      | Bể nước                              |
      | Cơ sở                                |
      | Hủy                                  |
      | Chọn                                 |

  Scenario: Kiểm tra UI Thêm mới Nguồn lực
    Given Người dùng nhấn vào thêm mới Nguồn lực
    Then Người dùng kiểm tra các văn bản sau hiển thị đúng:
      | Thêm mới nguồn lực  |
      | Chọn loại nguồn lực |
      | Huỷ                 |
      | Xác nhận            |

  Scenario: Kiểm tra UI Option Thêm mới Nguồn lực
    Given Người dùng nhấn vào thêm mới Nguồn lực
    When Người dùng nhấp vào "Chọn loại nguồn lực"
    Then Người dùng kiểm tra các văn bản sau hiển thị đúng:
      | Vui lòng chọn                        |
      | Đơn vị Y tế hỗ trợ                  |
      | Đơn vị Điện lực hỗ trợ              |
      | Đơn vị hỗ trợ phương tiện           |
      | Nguồn nước                           |
      | Đơn vị Công an cấp xã, huyện        |
      | Hủy                                  |
      | Chọn                                 |

  Scenario: Kiểm tra UI Option "Đơn vị Y tế hỗ trợ"
    Given Người dùng nhấn vào thêm mới Nguồn lực
    When Người dùng nhấp vào "Chọn loại nguồn lực"
    And Người dùng nhấp vào "Đơn vị Y tế hỗ trợ"
    And Người dùng nhấp vào "Xác nhận"
    Then Người dùng kiểm tra các văn bản sau hiển thị đúng:
      | Thêm nguồn lực                        |
      | Tên đơn vị hỗ trợ *                   |
      | Nhập tên đơn vị hỗ trợ                |
      | Số điện thoại liên hệ (báo hỗ trợ y tế) * |
      | Nhập số điện thoại                    |
      | Lấy tọa độ hiện tại                   |
      | Kinh độ *                             |
      | Vĩ độ *                               |
      | Chọn tỉnh/thành                       |
      | Chọn xã/phường                        |
      | Nhập đường/phố                        |
      | Nhập số                               |
      | Nhập địa chỉ                          |
      | Trạng thái                            |
      | Kích hoạt                              |
      | Hủy                                   |
      | Lưu                                   |

  Scenario: Thêm mới "Đơn vị Y tế hỗ trợ" thành công (Happy Case)
    Given Người dùng nhấn vào thêm mới Nguồn lực
    When Người dùng nhấp vào "Chọn loại nguồn lực"
    And Người dùng nhấp vào "Đơn vị Y tế hỗ trợ"
    And Người dùng nhấp vào "Xác nhận"

  # Nhập thông tin vào các trường
    And Người dùng nhập "Trạm Y tế Phường 1" vào trường "Tên đơn vị hỗ trợ"
    And Người dùng nhập "0909123456" vào trường "Số điện thoại liên hệ"
    And Người dùng nhập "106.700" vào trường "Kinh độ"
    And Người dùng nhập "10.776" vào trường "Vĩ độ"
    And Người dùng nhấp vào "Tỉnh/Thành"
    And Người dùng nhấp vào "TP. Hồ Chí Minh"
    And Người dùng nhấp vào "Xã/Phường"
    And Người dùng nhấp vào "Phường 1"
    And Người dùng nhập "Nguyễn Văn Cừ" vào trường "Đường/Phố"
    And Người dùng nhập "123" vào trường "Số"
    And Người dùng nhập "123 Nguyễn Văn Cừ, P.1, Q.5" vào trường "Địa chỉ"
    And Người dùng nhấp vào "Kích hoạt"

  # Lưu dữ liệu
    And Người dùng nhấp vào "Lưu"

    Then Người dùng kiểm tra văn bản "Thêm mới nguồn lực thành công" hiển thị




  Scenario: Kiểm tra UI Option "Đơn vị Điện lực hỗ trợ"
    Given Người dùng nhấn vào thêm mới Nguồn lực
    When Người dùng nhấp vào "Chọn loại nguồn lực"
    And Người dùng nhấp vào "Đơn vị Điện lực hỗ trợ"
    And Người dùng nhấp vào "Xác nhận"
    Then Người dùng kiểm tra các văn bản sau hiển thị đúng:
      | Tên đơn vị hỗ trợ *                |
      | Nhập tên đơn vị hỗ trợ             |
      | Số điện thoại liên hệ (báo hỗ trợ cắt điện) * |
      | Nhập số điện thoại                 |
      | Lấy tọa độ hiện tại                |
      | Kinh độ *                          |
      | Vĩ độ *                            |
      | Tỉnh/Thành *                       |
      | Xã/Phường *                        |
      | Đường/Phố                          |
      | Nhập đường/phố                     |
      | Số                                 |
      | Nhập số                            |
      | Địa chỉ                            |
      | Nhập địa chỉ                       |
      | Trạng thái                         |
      | Kích hoạt                          |
      | Hủy                                |
      | Lưu                                |

  Scenario: Thêm mới "Đơn vị Điện lực hỗ trợ" thành công (Happy Case)
    Given Người dùng nhấn vào thêm mới Nguồn lực
    When Người dùng nhấp vào "Chọn loại nguồn lực"
    And Người dùng nhấp vào "Đơn vị Điện lực hỗ trợ"
    And Người dùng nhấp vào "Xác nhận"
    And Người dùng nhập "Công ty Điện lực TP HCM" vào trường "Tên đơn vị hỗ trợ"
    And Người dùng nhập "0909123456" vào trường "Số điện thoại liên hệ"
    And Người dùng nhấp vào "Lấy tọa độ hiện tại"
    And Người dùng nhấp vào "TP. Hồ Chí Minh"
    And Người dùng nhấp vào "Phường Bến Nghé"
    And Người dùng nhập "Lê Duẩn" vào trường "Đường/Phố"
    And Người dùng nhập "1" vào trường "Số"
    And Người dùng nhập "01 Lê Duẩn, Bến Nghé, Quận 1, TP. HCM" vào trường "Địa chỉ"
    And Người dùng nhấp vào "Kích hoạt"
    And Người dùng nhấp vào "Lưu"
    Then Người dùng kiểm tra văn bản "Thêm mới nguồn lực thành công" hiển thị


  Scenario: Kiểm tra UI Option "Đơn vị hỗ trợ phương tiện"
    Given Người dùng nhấn vào thêm mới Nguồn lực
    When Người dùng nhấp vào "Chọn loại nguồn lực"
    And Người dùng nhấp vào "Đơn vị hỗ trợ phương tiện"
    And Người dùng nhấp vào "Xác nhận"
    Then Người dùng kiểm tra các văn bản sau hiển thị đúng:
      | Tên đơn vị hỗ trợ *                              |
      | Nhập tên đơn vị hỗ trợ                           |
      | Số điện thoại liên hệ (báo hỗ trợ an ninh) *     |
      | Nhập số điện thoại                                |
      | Lấy tọa độ hiện tại                               |
      | Kinh độ *                                         |
      | Vĩ độ *                                           |
      | Tỉnh/Thành *                                     |
      | Xã/Phường *                                      |
      | Đường/Phố                                        |
      | Nhập đường/phố                                   |
      | Số                                               |
      | Nhập số                                          |
      | Địa chỉ                                          |
      | Nhập địa chỉ                                     |
      | Chủng loại phương tiện *                         |
      | Chọn loại phương tiện                            |
      | Số lượng *                                       |
      | Nhập số lượng                                    |
      | Trạng thái                                       |
      | Kích hoạt                                        |
      | Hủy                                              |
      | Lưu                                              |

  Scenario: Thêm mới "Đơn vị hỗ trợ phương tiện" thành công (Happy Case)
    Given Người dùng nhấn vào thêm mới Nguồn lực
    When Người dùng nhấp vào "Chọn loại nguồn lực"
    And Người dùng nhấp vào "Đơn vị hỗ trợ phương tiện"
    And Người dùng nhấp vào "Xác nhận"
    And Người dùng nhập "Đơn vị hỗ trợ xe chuyên dụng" vào trường "Tên đơn vị hỗ trợ"
    And Người dùng nhập "0987654321" vào trường "Số điện thoại liên hệ"
    And Người dùng nhấp vào "Lấy tọa độ hiện tại"
    And Người dùng nhấp vào "TP. Hồ Chí Minh"
    And Người dùng nhấp vào "Phường Bến Thành"
    And Người dùng nhập "Trần Hưng Đạo" vào trường "Đường/Phố"
    And Người dùng nhập "12" vào trường "Số"
    And Người dùng nhập "12 Trần Hưng Đạo, Bến Thành, Quận 1, TP. HCM" vào trường "Địa chỉ"
    And Người dùng nhấp vào "Chọn loại phương tiện"
    And Người dùng nhấp vào "Xe cứu hỏa"
    And Người dùng nhập "3" vào trường "Số lượng"
    And Người dùng nhấp vào "Kích hoạt"
    And Người dùng nhấp vào "Lưu"
    Then Người dùng kiểm tra văn bản "Thêm mới nguồn lực thành công" hiển thị

  Scenario: Kiểm tra UI Option "Đơn vị hỗ trợ phương tiện"
    Given Người dùng nhấn vào thêm mới Nguồn lực
    When Người dùng nhấp vào "Chọn loại nguồn lực"
    And Người dùng nhấp vào "Đơn vị hỗ trợ phương tiện"
    And Người dùng nhấp vào "Xác nhận"
    Then Người dùng kiểm tra các văn bản sau hiển thị đúng:
      | Tên đơn vị hỗ trợ *                        |
      | Nhập tên đơn vị hỗ trợ                     |
      | Số điện thoại liên hệ (báo hỗ trợ an ninh) * |
      | Nhập số điện thoại                         |
      | Lấy tọa độ hiện tại                        |
      | Kinh độ *                                  |
      | Vĩ độ *                                    |
      | Tỉnh/Thành *                               |
      | Xã/Phường *                                |
      | Đường/Phố                                  |
      | Nhập đường/phố                             |
      | Số                                         |
      | Nhập số                                    |
      | Địa chỉ                                    |
      | Nhập địa chỉ                               |
      | Chủng loại phương tiện *                  |
      | Chọn loại phương tiện                      |
      | Số lượng *                                 |
      | Nhập số lượng                              |
      | Trạng thái                                 |
      | Kích hoạt                                   |
      | Hủy                                        |
      | Lưu                                        |

  Scenario: Thêm mới "Đơn vị hỗ trợ phương tiện" thành công (Happy Case)
    Given Người dùng nhấn vào thêm mới Nguồn lực
    When Người dùng nhấp vào "Chọn loại nguồn lực"
    And Người dùng nhấp vào "Đơn vị hỗ trợ phương tiện"
    And Người dùng nhấp vào "Xác nhận"
    And Người dùng nhập "Đơn vị hỗ trợ xe chuyên dụng" vào trường "Tên đơn vị hỗ trợ"
    And Người dùng nhập "0987654321" vào trường "Số điện thoại liên hệ"
    And Người dùng nhấp vào "Lấy tọa độ hiện tại"
    And Người dùng nhấp vào "TP. Hồ Chí Minh"
    And Người dùng nhấp vào "Phường Bến Thành"
    And Người dùng nhập "Trần Hưng Đạo" vào trường "Đường/Phố"
    And Người dùng nhập "12" vào trường "Số"
    And Người dùng nhập "12 Trần Hưng Đạo, Bến Thành, Quận 1, TP. HCM" vào trường "Địa chỉ"
    And Người dùng nhấp vào "Chọn loại phương tiện"
    And Người dùng nhấp vào "Xe cứu hỏa"
    And Người dùng nhập "3" vào trường "Số lượng"
    And Người dùng nhấp vào "Kích hoạt"
    And Người dùng nhấp vào "Lưu"
    Then Người dùng kiểm tra văn bản "Thêm mới nguồn lực thành công" hiển thị


  Scenario: Kiểm tra UI Option "Đơn vị Công an cấp xã, huyện"
    Given Người dùng nhấn vào thêm mới Nguồn lực
    When Người dùng nhấp vào "Chọn loại nguồn lực"
    And Người dùng nhấp vào "Đơn vị Công an cấp xã, huyện"
    And Người dùng nhấp vào "Xác nhận"
    Then Người dùng kiểm tra các văn bản sau hiển thị đúng:
      | Tổ chức đơn vị *                                 |
      | Nhập tổ chức đơn vị                              |
      | Tên đơn vị hỗ trợ *                              |
      | Nhập tên đơn vị hỗ trợ                           |
      | Số điện thoại liên hệ (báo hỗ trợ an ninh) *     |
      | Nhập số điện thoại                                |
      | Lấy tọa độ hiện tại                               |
      | Kinh độ *                                         |
      | Vĩ độ *                                           |
      | Tỉnh/Thành *                                      |
      | Xã/Phường *                                       |
      | Đường/Phố                                         |
      | Nhập đường/phố                                    |
      | Số                                                |
      | Nhập số                                           |
      | Địa chỉ                                           |
      | Nhập địa chỉ                                      |
      | Trạng thái                                        |
      | Kích hoạt                                         |
      | Hủy                                               |
      | Lưu                                               |

  Scenario: Thêm mới "Đơn vị Công an cấp xã, huyện" thành công (Happy Case)
    Given Người dùng nhấn vào thêm mới Nguồn lực
    When Người dùng nhấp vào "Chọn loại nguồn lực"
    And Người dùng nhấp vào "Đơn vị Công an cấp xã, huyện"
    And Người dùng nhấp vào "Xác nhận"
    And Người dùng nhập "Công an Quận 1" vào trường "Tổ chức đơn vị"
    And Người dùng nhập "Đội Cảnh sát PCCC" vào trường "Tên đơn vị hỗ trợ"
    And Người dùng nhập "0934567890" vào trường "Số điện thoại liên hệ"
    And Người dùng nhấp vào "Lấy tọa độ hiện tại"
    And Người dùng nhấp vào "TP. Hồ Chí Minh"
    And Người dùng nhấp vào "Phường Bến Nghé"
    And Người dùng nhập "Lê Lợi" vào trường "Đường/Phố"
    And Người dùng nhập "15" vào trường "Số"
    And Người dùng nhập "15 Lê Lợi, Bến Nghé, Quận 1, TP. HCM" vào trường "Địa chỉ"
    And Người dùng nhấp vào "Kích hoạt"
    And Người dùng nhấp vào "Lưu"
    Then Người dùng kiểm tra văn bản "Thêm mới nguồn lực thành công" hiển thị






