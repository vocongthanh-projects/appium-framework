Feature: Danh sách nguồn lực

  Scenario: Kiểm tra UI Nguồn lực
    Given Người dùng nhấp vào "Nguồn lực"
    Then Người dùng kiểm tra các văn bản sau hiển thị đúng:
      | Danh sách nguồn lực         |
      | Nhập thông tin tìm kiếm.... |


  Scenario: Kiểm tra UI combobox Nguồn lực
    Given Người dùng nhấp vào "Nguồn lực"
    When Người dùng nhấn vào filter Nguồn lực
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
