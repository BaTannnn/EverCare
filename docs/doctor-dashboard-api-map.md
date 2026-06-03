# Doctor Dashboard API Map

Base backend context: `http://localhost:8080/backend`

Frontend `src/configs/Apis.js` dùng base API `http://localhost:8080/backend/api/`, vì vậy endpoint trong frontend bỏ prefix `/api`.

## Dashboard APIs

| Feature | Method | Endpoint thật | Request body | Response body | Backend file | Ghi chú |
|---|---|---|---|---|---|---|
| Dashboard summary hôm nay | GET | `/api/doctor/dashboard/summary` | None | `DoctorDashboardSummaryResponse`: `todayAppointments`, `waitingAppointments`, `inProgressAppointments`, `completedAppointments`, `cancelledAppointments` | `backend/src/main/java/com/evercare/controllers/api/ApiDoctorDashboardController.java`, `backend/src/main/java/com/evercare/services/impl/DoctorDashboardServiceImpl.java`, `backend/src/main/java/com/evercare/dtos/response/DoctorDashboardSummaryResponse.java` | JWT. Service kiểm tra username hiện tại map được sang bác sĩ active. |
| Lịch hẹn hôm nay cho dashboard | GET | `/api/doctor/appointments/today` | None | `DoctorAppointmentResponse[]` | `backend/src/main/java/com/evercare/controllers/api/ApiDoctorAppointmentController.java`, `backend/src/main/java/com/evercare/services/impl/DoctorAppointmentServiceImpl.java` | JWT. Chỉ trả lịch thuộc bác sĩ đang đăng nhập. |

## Appointment APIs

| Feature | Method | Endpoint thật | Request body | Response body | Backend file | Ghi chú |
|---|---|---|---|---|---|---|
| Danh sách lịch làm việc hôm nay | GET | `/api/doctor/schedules/today` | None | `DoctorScheduleResponse[]`: `id`, `workDate`, `startTime`, `endTime`, `maxPatients`, `status`, `statusLabel`, `note`, `doctorId`, `doctorName`, `departmentId`, `departmentName` | `backend/src/main/java/com/evercare/controllers/api/ApiDoctorScheduleController.java`, `backend/src/main/java/com/evercare/dtos/response/DoctorScheduleResponse.java`, `backend/src/main/java/com/evercare/services/impl/DoctorScheduleServiceImpl.java` | JWT. Chỉ trả lịch làm việc của bác sĩ đang đăng nhập. |
| Danh sách lịch làm việc theo bộ lọc | GET | `/api/doctor/schedules?date=YYYY-MM-DD` hoặc `/api/doctor/schedules?fromDate=YYYY-MM-DD&toDate=YYYY-MM-DD&status=AVAILABLE` | Query optional `date`, `fromDate`, `toDate`, `status` | `DoctorScheduleResponse[]` | `backend/src/main/java/com/evercare/controllers/api/ApiDoctorScheduleController.java`, `backend/src/main/java/com/evercare/repositories/impl/DoctorScheduleRepositoryImpl.java` | Status thật: `AVAILABLE`, `FULL`, `CLOSED`, `CANCELLED`. |
| Danh sách lịch hôm nay | GET | `/api/doctor/appointments/today` | None | `DoctorAppointmentResponse[]` | `backend/src/main/java/com/evercare/controllers/api/ApiDoctorAppointmentController.java` | JWT. Chỉ bác sĩ phụ trách lịch hẹn được xem. |
| Danh sách lịch theo ngày | GET | `/api/doctor/appointments?date=YYYY-MM-DD` | Query `date`, format `yyyy-MM-dd` | `DoctorAppointmentResponse[]` | `backend/src/main/java/com/evercare/controllers/api/ApiDoctorAppointmentController.java` | JWT. Chưa có filter server-side theo status/search, frontend filter local. |
| Chi tiết lịch hẹn | GET | `/api/doctor/appointments/{appointmentId}` | None | `DoctorAppointmentResponse`: `id`, `appointmentCode`, `appointmentDate`, `startTime`, `endTime`, `reason`, `symptomNote`, `status`, `statusLabel`, `patient`, `service`, optional `medicalRecord`, optional `prescription` | `backend/src/main/java/com/evercare/controllers/api/ApiDoctorAppointmentController.java`, `backend/src/main/java/com/evercare/mappers/AppointmentMapper.java`, `backend/src/main/java/com/evercare/dtos/response/DoctorAppointmentResponse.java` | JWT. Chỉ bác sĩ phụ trách lịch hẹn được xem. |
| Bắt đầu khám | POST | `/api/doctor/appointments/{appointmentId}/start-examination` | Optional JSON `{ "chiefComplaint": string, "initialNote": string }` | `MedicalRecordResponse` | `backend/src/main/java/com/evercare/controllers/api/ApiDoctorAppointmentController.java`, `backend/src/main/java/com/evercare/dtos/request/StartExaminationRequest.java`, `backend/src/main/java/com/evercare/services/impl/DoctorAppointmentServiceImpl.java` | Chỉ status `BOOKED` hoặc `WAITING`; backend chuyển appointment sang `IN_PROGRESS` và tạo medical record nếu chưa có. |

## Medical Record APIs

| Feature | Method | Endpoint thật | Request body | Response body | Backend file | Ghi chú |
|---|---|---|---|---|---|---|
| Lấy bệnh án trong chi tiết lịch hẹn | GET | `/api/doctor/appointments/{appointmentId}` | None | `DoctorAppointmentResponse.medicalRecord` nếu có | `backend/src/main/java/com/evercare/controllers/api/ApiDoctorAppointmentController.java`, `backend/src/main/java/com/evercare/mappers/AppointmentMapper.java` | Backend chưa có GET medical record riêng cho bác sĩ; frontend lấy qua appointment detail. |
| Cập nhật bệnh án | PUT | `/api/doctor/medical-records/{recordId}` | Optional JSON `{ "chiefComplaint": string, "diagnosis": string, "treatmentPlan": string, "doctorNote": string }` | `MedicalRecordResponse` | `backend/src/main/java/com/evercare/controllers/api/ApiDoctorMedicalRecordController.java`, `backend/src/main/java/com/evercare/dtos/request/UpdateMedicalRecordRequest.java`, `backend/src/main/java/com/evercare/services/impl/DoctorMedicalRecordServiceImpl.java` | JWT. Backend chặn sửa nếu appointment `COMPLETED`. |

## Service/Test Result APIs

| Feature | Method | Endpoint thật | Request body | Response body | Backend file | Ghi chú |
|---|---|---|---|---|---|---|
| Tìm/lấy danh sách medical services | GET | `/api/medical-services?kw=...` | Query optional `kw`, `departmentId`, `serviceType` | `MedicalServiceResponse[]`: `id`, `code`, `name`, `description`, `price`, `serviceType`, `active`, `departmentId`, `departmentName` | `backend/src/main/java/com/evercare/controllers/api/ApiMedicalServiceController.java`, `backend/src/main/java/com/evercare/repositories/impl/MedicalServiceRepositoryImpl.java`, `backend/src/main/java/com/evercare/dtos/response/MedicalServiceResponse.java` | Dùng cho autocomplete chọn dịch vụ khi bác sĩ thêm chỉ định. |
| Thêm chỉ định dịch vụ/xét nghiệm | POST | `/api/doctor/medical-records/{recordId}/services` | JSON `{ "serviceId": number, "quantity": number, "resultSummary": string }` | `MedicalRecordServiceResponse` | `backend/src/main/java/com/evercare/controllers/api/ApiDoctorMedicalRecordController.java`, `backend/src/main/java/com/evercare/dtos/request/MedicalRecordServiceRequest.java`, `backend/src/main/java/com/evercare/services/impl/DoctorMedicalRecordServiceImpl.java` | Chỉ thêm khi appointment `IN_PROGRESS`. Không gửi `unitPrice` từ frontend. |
| Xem danh sách chỉ định và kết quả | GET | `/api/doctor/medical-records/{recordId}/services` | None | `MedicalRecordServiceResponse[]`, mỗi item có `testResults: TestResultResponse[]` | `backend/src/main/java/com/evercare/controllers/api/ApiDoctorMedicalRecordController.java`, `backend/src/main/java/com/evercare/mappers/MedicalRecordServiceMapper.java`, `backend/src/main/java/com/evercare/mappers/TestResultMapper.java` | Bác sĩ chỉ xem kết quả xét nghiệm qua response lồng trong service. |
| Nhân viên y tế tạo kết quả xét nghiệm | POST | `/api/staff/medical-records/{recordId}/test-results` | JSON `{ "serviceId": number, "resultTitle": string, "resultContent": string, "fileUrl": string, "conclusion": string }` | `TestResultResponse` | `backend/src/main/java/com/evercare/controllers/api/ApiStaffTestResultController.java`, `backend/src/main/java/com/evercare/dtos/request/TestResultRequest.java` | Không dùng trong Doctor Dashboard. |
| Nhân viên y tế cập nhật kết quả xét nghiệm | PUT | `/api/staff/test-results/{id}` | Optional JSON `{ "serviceId": number, "resultTitle": string, "resultContent": string, "fileUrl": string, "conclusion": string }` | `TestResultResponse` | `backend/src/main/java/com/evercare/controllers/api/ApiStaffTestResultController.java` | Không dùng trong Doctor Dashboard. |

## Medicine APIs

| Feature | Method | Endpoint thật | Request body | Response body | Backend file | Ghi chú |
|---|---|---|---|---|---|---|
| Tìm thuốc theo keyword | GET | `/api/medicines?keyword=...` | Query `keyword`; optional `active`, `unit` | `MedicineSearchResponse[]`: `medicineId`, `medicineName`, `unit`, `unitPrice`, `availableQuantity` | `backend/src/main/java/com/evercare/controllers/api/ApiMedicineController.java`, `backend/src/main/java/com/evercare/dtos/response/MedicineSearchResponse.java`, `backend/src/main/java/com/evercare/services/impl/MedicineServiceImpl.java` | Doctor Dashboard gọi bằng `authApis()`. Không trừ kho. |
| Lấy danh sách thuốc | GET | `/api/medicines` | Optional query `active`, `kw`, `keyword`, `unit` | Nếu không có `keyword`: `MedicineResponse[]` | `backend/src/main/java/com/evercare/controllers/api/ApiMedicineController.java`, `backend/src/main/java/com/evercare/dtos/response/MedicineResponse.java` | API hiện public theo security, frontend vẫn dùng `authApis()`. |
| Chi tiết thuốc | GET | `/api/medicines/{id}` | None | `MedicineResponse` | `backend/src/main/java/com/evercare/controllers/api/ApiMedicineController.java` | API thật có sẵn. |

## Prescription APIs

| Feature | Method | Endpoint thật | Request body | Response body | Backend file | Ghi chú |
|---|---|---|---|---|---|---|
| Xem đơn thuốc trong chi tiết lịch hẹn | GET | `/api/doctor/appointments/{appointmentId}` | None | `DoctorAppointmentResponse.prescription`, nếu medical record có prescription. `PrescriptionResponse` có `items: PrescriptionItemResponse[]` | `backend/src/main/java/com/evercare/controllers/api/ApiDoctorAppointmentController.java`, `backend/src/main/java/com/evercare/mappers/AppointmentMapper.java`, `backend/src/main/java/com/evercare/dtos/response/PrescriptionResponse.java`, `backend/src/main/java/com/evercare/dtos/response/PrescriptionItemResponse.java` | JWT doctor. Chỉ bác sĩ phụ trách appointment. |
| Dược sĩ xem danh sách đơn thuốc | GET | `/api/pharmacist/prescriptions` | Query params | `PrescriptionResponse[]` | `backend/src/main/java/com/evercare/controllers/api/ApiPharmacistPrescriptionController.java`, `backend/src/main/java/com/evercare/services/impl/PrescriptionServiceImpl.java` | Không dùng trong Doctor Dashboard. |
| Dược sĩ xem chi tiết đơn thuốc | GET | `/api/pharmacist/prescriptions/{id}` | None | `PrescriptionResponse` | `backend/src/main/java/com/evercare/controllers/api/ApiPharmacistPrescriptionController.java` | Không dùng trong Doctor Dashboard. |
| Dược sĩ cấp phát đơn thuốc | POST | `/api/pharmacist/prescriptions/{id}/dispense` | None | `PrescriptionResponse` | `backend/src/main/java/com/evercare/controllers/api/ApiPharmacistPrescriptionController.java`, `backend/src/main/java/com/evercare/services/impl/PrescriptionServiceImpl.java` | Không dùng trong Doctor Dashboard; đây là luồng trừ kho/cấp phát. |

## Missing APIs

| Feature | Method | Endpoint thật | Request body | Response body | Backend file | Ghi chú |
|---|---|---|---|---|---|---|
| Bác sĩ tạo đơn thuốc | Missing | Missing API như `POST /api/doctor/medical-records/{recordId}/prescriptions` | Nên nhận `note` và `items` gồm `medicineId`, `quantity`, `dosage`, `frequency`, `duration`, `instruction` | Nên trả `PrescriptionResponse` status `PRESCRIBED` | Không tìm thấy trong `backend/src/main/java/com/evercare/controllers/api` | Frontend không gọi endpoint giả và không hiển thị dữ liệu giả. |
| Bác sĩ cập nhật đơn thuốc | Missing | Missing API như `PUT /api/doctor/prescriptions/{prescriptionId}` | Nên nhận `note` và `items` | Nên trả `PrescriptionResponse` | Không tìm thấy trong `backend/src/main/java/com/evercare/controllers/api` | Frontend không gọi endpoint giả và không hiển thị dữ liệu giả. |
| Hoàn tất khám | Missing | Missing API như `POST /api/doctor/appointments/{appointmentId}/complete` | Optional JSON `{}` hoặc summary note | Nên trả updated `DoctorAppointmentResponse` hoặc `MedicalRecordResponse` | Không tìm thấy trong `ApiDoctorAppointmentController` | Frontend hiển thị nút và modal TODO, không bịa endpoint. |
| Lọc lịch hẹn server-side theo status/search | Missing | Current API chỉ có `GET /api/doctor/appointments?date=YYYY-MM-DD` | N/A | N/A | `backend/src/main/java/com/evercare/controllers/api/ApiDoctorAppointmentController.java` | Frontend filter/search local trên danh sách theo ngày. |
| API lấy medical record trực tiếp theo `recordId` | Missing | Missing `GET /api/doctor/medical-records/{recordId}` | None | `MedicalRecordResponse` | Không tìm thấy trong `ApiDoctorMedicalRecordController` | Frontend dùng appointment detail để lấy record. |
| API lấy test results trực tiếp theo record | Missing | Missing `GET /api/doctor/medical-records/{recordId}/test-results` | None | `TestResultResponse[]` | `backend/src/main/java/com/evercare/controllers/api/ApiDoctorMedicalRecordController.java` | Frontend xem kết quả qua `GET /api/doctor/medical-records/{recordId}/services`. |
