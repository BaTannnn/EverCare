import { useCallback, useEffect, useMemo, useReducer } from "react";
import { Alert, Button, Modal } from "react-bootstrap";
import { useNavigate, useParams } from "react-router-dom";
import ConfirmModal from "../../components/common/ConfirmModal";
import ErrorState from "../../components/common/ErrorState";
import LoadingState from "../../components/common/LoadingState";
import ExamPatientBanner from "../../components/doctor/examination/ExamPatientBanner";
import MedicalRecordForm from "../../components/doctor/examination/MedicalRecordForm";
import MedicalServicesPanel from "../../components/doctor/examination/MedicalServicesPanel";
import PrescriptionPanel from "../../components/doctor/examination/PrescriptionPanel";
import TestResultsPanel from "../../components/doctor/examination/TestResultsPanel";
import { getAppointmentDetail } from "../../services/doctor/doctorAppointmentApi";
import {
  doctorExaminationActionTypes,
  doctorExaminationInitialState,
  doctorExaminationReducer,
} from "../../reducers/doctorExaminationReducer";
import {
  addMedicalRecordService,
  completeMedicalRecord,
  getDoctorTestResultFile,
  getMedicalRecordServices,
  updateMedicalRecord,
} from "../../services/doctor/doctorMedicalRecordApi";
import { searchMedicalServices } from "../../services/doctor/doctorMedicalServiceApi";
import { searchMedicines } from "../../services/doctor/doctorMedicineApi";
import {
  createDoctorPrescription,
  getDoctorPrescription,
  updateDoctorPrescription,
} from "../../services/doctor/doctorPrescriptionApi";
import {
  canStartExamination,
  editableStatuses,
  getErrorMessage,
  isDoctorVisibleAppointment,
} from "./doctorPageUtils";
import {
  createPrescriptionDraft,
  emptyPrescriptionLine,
  emptyServiceForm,
  getPrescriptionStockWarnings,
  isOrderableMedicalService,
  unwrapList,
} from "./examinationWorkspaceUtils";

function ExaminationWorkspacePage() {
  const { appointmentId } = useParams();
  const navigate = useNavigate();
  const [state, dispatch] = useReducer(doctorExaminationReducer, doctorExaminationInitialState);
  const { medicineSearch, serviceSearch, ui, workspace } = state;
  const { appointment, prescription, recordForm, services } = workspace;
  const {
    dropdownOpen: serviceDropdownOpen,
    form: serviceForm,
    keyword: serviceKeyword,
    options: serviceOptions,
    searching: serviceSearching,
    selectedIds: selectedServiceIds,
  } = serviceSearch;
  const {
    activePresetField,
    dropdownOpen: medicineDropdownOpen,
    error: medicineSearchError,
    keyword: medicineKeyword,
    loading: medicineLoading,
    results: medicineResults,
  } = medicineSearch;
  const {
    completeError,
    completeModal,
    completingRecord,
    error,
    loading,
    notice,
    openingResultFileId,
    resultFileViewer,
    savingPrescription,
    savingRecord,
    savingService,
  } = ui;

  const patchExamSlice = useCallback((slice, payload) => {
    dispatch({ type: doctorExaminationActionTypes.PATCH_SLICE, slice, payload });
  }, []);

  const setWorkspace = useCallback((payload) => patchExamSlice("workspace", payload), [patchExamSlice]);
  const setServiceSearch = useCallback((payload) => patchExamSlice("serviceSearch", payload), [patchExamSlice]);
  const setMedicineSearch = useCallback((payload) => patchExamSlice("medicineSearch", payload), [patchExamSlice]);
  const setUi = useCallback((payload) => patchExamSlice("ui", payload), [patchExamSlice]);

  const medicalRecord = appointment?.medicalRecord;
  const editable = editableStatuses.includes(appointment?.status);
  const prescriptionStatus = String(prescription?.status || "DRAFT").toUpperCase();
  const hasPrescription = Boolean(prescription?.id);
  const prescriptionDispensed = prescriptionStatus === "DISPENSED";
  const prescriptionEditable = editable && !prescriptionDispensed;
  const prescriptionStatusLabel = prescriptionDispensed
    ? "Đã cấp phát"
    : hasPrescription
      ? "Chờ cấp phát"
      : "Đơn mới";
  const prescriptionStatusVariant = prescriptionDispensed ? "success" : hasPrescription ? "warning" : "secondary";
  const prescriptionStockWarnings = getPrescriptionStockWarnings(prescription?.items || []);

  const loadWorkspace = useCallback(async () => {
    setUi({ loading: true, error: "", notice: "" });

    try {
      const response = await getAppointmentDetail(appointmentId);
      const detail = response.data;

      if (!isDoctorVisibleAppointment(detail)) {
        setWorkspace({ appointment: null });
        setUi({
          error: "Lịch hẹn này chưa sẵn sàng cho bác sĩ xử lý.",
        });
        return;
      }

      setWorkspace({
        appointment: detail,
        recordForm: {
          chiefComplaint: detail?.medicalRecord?.chiefComplaint || "",
          diagnosis: detail?.medicalRecord?.diagnosis || "",
          treatmentPlan: detail?.medicalRecord?.treatmentPlan || "",
          doctorNote: detail?.medicalRecord?.doctorNote || "",
        },
      });

      if (detail?.prescription?.id) {
        try {
          const prescriptionResponse = await getDoctorPrescription(detail.prescription.id);
          setWorkspace({ prescription: prescriptionResponse.data || detail.prescription });
        } catch {
          setWorkspace({ prescription: detail.prescription });
        }
      } else {
        setWorkspace({ prescription: null });
      }

      if (detail?.medicalRecord?.id) {
        const serviceResponse = await getMedicalRecordServices(detail.medicalRecord.id);
        setWorkspace({ services: serviceResponse.data || [] });
        setServiceSearch({ selectedIds: [] });
      } else {
        setWorkspace({ services: [] });
        setServiceSearch({ selectedIds: [] });
      }
    } catch (err) {
      if (err.response?.status === 401) {
        navigate("/login", { replace: true });
        return;
      }

      setUi({ error: getErrorMessage(err) });
    } finally {
      setUi({ loading: false });
    }
  }, [appointmentId, navigate, setServiceSearch, setUi, setWorkspace]);

  useEffect(() => {
    loadWorkspace();
  }, [loadWorkspace]);

  useEffect(() => () => {
    if (resultFileViewer.url) {
      URL.revokeObjectURL(resultFileViewer.url);
    }
  }, [resultFileViewer.url]);

  useEffect(() => {
    const keyword = serviceKeyword.trim();
    const timer = setTimeout(async () => {
      setServiceSearch({ searching: true });
      try {
        const response = await searchMedicalServices(keyword);
        setServiceSearch({ options: (response.data || []).filter(isOrderableMedicalService) });
      } catch {
        setServiceSearch({ options: [] });
      } finally {
        setServiceSearch({ searching: false });
      }
    }, 300);

    return () => clearTimeout(timer);
  }, [serviceKeyword, setServiceSearch]);

  useEffect(() => {
    const keyword = medicineKeyword.trim();

    if (!keyword) {
      setMedicineSearch({ results: [], error: "", dropdownOpen: false });
      return undefined;
    }

    setMedicineSearch({ dropdownOpen: true });
    const timer = setTimeout(async () => {
      setMedicineSearch({ loading: true, error: "" });
      try {
        const response = await searchMedicines(keyword);
        setMedicineSearch({ results: unwrapList(response.data) });
      } catch (err) {
        setMedicineSearch({ results: [], error: getErrorMessage(err) || "Không tải được danh sách thuốc." });
      } finally {
        setMedicineSearch({ loading: false });
      }
    }, 300);

    return () => clearTimeout(timer);
  }, [medicineKeyword, setMedicineSearch]);

  const updateRecordField = (field, value) => {
    setWorkspace({ recordForm: (current) => ({ ...current, [field]: value }) });
  };

  const handleSaveRecord = async (e) => {
    e.preventDefault();

    if (!medicalRecord?.id) {
      setUi({ notice: "Vui lòng bắt đầu khám trước khi lưu bệnh án." });
      return;
    }

    setUi({ savingRecord: true, notice: "" });

    try {
      const response = await updateMedicalRecord(medicalRecord.id, recordForm);
      setWorkspace({ appointment: (current) => ({
        ...current,
        medicalRecord: response.data,
      }) });
      setUi({ notice: "Đã lưu bệnh án." });
    } catch (err) {
      setUi({ notice: getErrorMessage(err) });
      if (err.response?.status === 409) {
        loadWorkspace();
      }
    } finally {
      setUi({ savingRecord: false });
    }
  };

  const handleCompleteRecord = async () => {
    if (!medicalRecord?.id) {
      setUi({ completeError: "Vui lòng bắt đầu khám trước khi hoàn tất bệnh án." });
      return;
    }

    if (!recordForm.diagnosis.trim()) {
      setUi({ completeError: "Vui lòng nhập chẩn đoán trước khi hoàn tất bệnh án." });
      return;
    }

    setUi({ completingRecord: true, notice: "", completeError: "" });

    try {
      await updateMedicalRecord(medicalRecord.id, recordForm);
      await completeMedicalRecord(medicalRecord.id);
      setUi({ completeModal: false, notice: "Đã hoàn tất bệnh án." });
      loadWorkspace();
    } catch (err) {
      setUi({ completeError: getErrorMessage(err) });
      if (err.response?.status === 401) {
        navigate("/login", { replace: true });
      }
    } finally {
      setUi({ completingRecord: false });
    }
  };

  const updateServiceKeyword = (value, options = {}) => {
    setServiceSearch({
      keyword: value,
      dropdownOpen: true,
      ...(options.keepSelection ? {} : { form: (current) => ({ ...current, serviceId: "" }) }),
    });
  };

  const updateServiceForm = (patch) => {
    setServiceSearch({ form: (current) => ({ ...current, ...patch }) });
  };

  const handleAddService = async (e) => {
    e.preventDefault();

    if (!medicalRecord?.id) {
      setUi({ notice: "Vui lòng bắt đầu khám trước khi thêm chỉ định." });
      return;
    }

    if (!serviceForm.serviceId) {
      setUi({ notice: "Vui lòng chọn dịch vụ từ danh sách gợi ý." });
      return;
    }

    const selectedServiceId = Number(serviceForm.serviceId);
    const alreadySelected = services.some((item) => Number(item.serviceId) === selectedServiceId);
    if (alreadySelected) {
      setUi({ notice: "Dịch vụ này đã có trong bệnh án." });
      return;
    }

    setWorkspace({
      services: (current) => [
        {
          id: `pending-${Date.now()}-${serviceForm.serviceId}`,
          serviceId: selectedServiceId,
          serviceCode: serviceForm.serviceCode,
          serviceName: serviceForm.serviceName || serviceKeyword,
          serviceType: serviceForm.serviceType,
          quantity: 1,
          unitPrice: serviceForm.unitPrice,
          resultSummary: serviceForm.resultSummary,
          testResults: [],
          pending: true,
        },
        ...current,
      ],
    });
    setServiceSearch({ selectedIds: [], form: emptyServiceForm, keyword: "", dropdownOpen: false });
    setUi({ notice: "Đã thêm dịch vụ vào danh sách chờ xác nhận." });
  };

  const handleConfirmServices = async () => {
    if (!medicalRecord?.id) {
      setUi({ notice: "Vui lòng bắt đầu khám trước khi xác nhận dịch vụ." });
      return;
    }

    const pendingServices = services.filter((item) => item.pending);
    if (pendingServices.length === 0) {
      setUi({ notice: "Không có dịch vụ mới cần xác nhận." });
      return;
    }

    setUi({ savingService: true, notice: "" });

    try {
      const confirmedServices = await Promise.all(
        pendingServices.map((item) =>
          addMedicalRecordService(medicalRecord.id, {
            serviceId: Number(item.serviceId),
            quantity: item.quantity || 1,
            resultSummary: item.resultSummary,
          }).then((response) => response.data)
        )
      );
      const confirmedByPendingId = new Map(
        pendingServices.map((item, index) => [item.id, confirmedServices[index]])
      );

      setWorkspace({ services: (current) =>
        current.map((item) => (item.pending ? confirmedByPendingId.get(item.id) || item : item))
      });
      setServiceSearch({ selectedIds: [] });
      setUi({ notice: "Đã xác nhận dịch vụ." });
    } catch (err) {
      setUi({ notice: getErrorMessage(err) });
      if (err.response?.status === 409) {
        loadWorkspace();
      }
    } finally {
      setUi({ savingService: false });
    }
  };

  const removePendingService = (serviceId) => {
    setWorkspace({ services: (current) => current.filter((item) => item.id !== serviceId || !item.pending) });
    setServiceSearch({ selectedIds: (current) => current.filter((id) => id !== serviceId) });
  };

  const removeSelectedPendingServices = () => {
    if (selectedServiceIds.length === 0) {
      setUi({ notice: "Vui lòng chọn dịch vụ cần xóa." });
      return;
    }

    const selectedIdSet = new Set(selectedServiceIds);
    setWorkspace({ services: (current) => current.filter((item) => !item.pending || !selectedIdSet.has(item.id)) });
    setServiceSearch({ selectedIds: [] });
    setUi({ notice: "Đã xóa dịch vụ chưa xác nhận." });
  };

  const toggleServiceSelection = (serviceId, checked) => {
    setServiceSearch({ selectedIds: (current) =>
      checked ? [...current, serviceId] : current.filter((id) => id !== serviceId)
    });
  };

  const toggleAllPendingServices = (checked) => {
    setServiceSearch({ selectedIds: checked ? services.filter((item) => item.pending).map((item) => item.id) : [] });
  };

  const selectMedicalService = (service) => {
    const alreadySelected = services.some((item) => Number(item.serviceId) === Number(service.id));
    if (alreadySelected) {
      setServiceSearch({ form: emptyServiceForm, keyword: "", dropdownOpen: false });
      setUi({ notice: "Dịch vụ này đã có trong bệnh án." });
      return;
    }

    setServiceSearch({ form: (current) => ({
      ...current,
      serviceId: service.id,
      serviceCode: service.code,
      serviceName: service.name,
      serviceType: service.serviceType,
      unitPrice: service.price,
    }), keyword: service.name || "", dropdownOpen: false });
  };

  const closeResultFileViewer = () => {
    setUi({ resultFileViewer: (current) => {
      if (current.url) {
        URL.revokeObjectURL(current.url);
      }

      return { show: false, url: "", title: "" };
    } });
  };

  const openResultFile = async (result) => {
    if (!result?.id) {
      return;
    }

    setUi({ openingResultFileId: result.id, notice: "" });

    try {
      const response = await getDoctorTestResultFile(result.id);
      const fileUrl = URL.createObjectURL(new Blob([response.data], { type: "application/pdf" }));

      setUi({ resultFileViewer: (current) => {
        if (current.url) {
          URL.revokeObjectURL(current.url);
        }

        return {
          show: true,
          url: fileUrl,
          title: result.resultTitle || result.serviceName || "Tệp kết quả",
        };
      } });
    } catch (err) {
      setUi({ notice: getErrorMessage(err) });
    } finally {
      setUi({ openingResultFileId: null });
    }
  };

  const addMedicineToPrescription = (medicine) => {
    const medicineId = medicine.medicineId ?? medicine.id;
    const medicineName = medicine.medicineName ?? medicine.name;

    if (!medicineId) {
      setUi({ notice: "Không xác định được thuốc cần thêm." });
      return;
    }

    const newItem = {
      ...emptyPrescriptionLine,
      medicineId,
      medicineName,
      unit: medicine.unit,
      unitPrice: medicine.unitPrice ?? medicine.price,
      availableQuantity: medicine.availableQuantity,
    };

    setWorkspace({
      prescription: (current) => createPrescriptionDraft({
        current,
        medicalRecord,
        patientName: appointment?.patient?.fullName,
        overrides: { items: [...(current?.items || []), newItem] },
      }),
    });
    setMedicineSearch({ keyword: "", results: [], dropdownOpen: false, error: "" });
    setUi({ notice: `Đã thêm ${medicineName || "thuốc"} vào đơn. Bấm lưu đơn thuốc để cập nhật.` });
  };

  const updatePrescriptionItem = (index, field, value) => {
    setWorkspace({ prescription: (current) => ({
      ...current,
      items: (current?.items || []).map((item, itemIndex) =>
        itemIndex === index ? { ...item, [field]: value } : item
      ),
    }) });
  };

  const applyPrescriptionPreset = (index, field, value) => {
    updatePrescriptionItem(index, field, value);
    setMedicineSearch({ activePresetField: null });
  };

  const removePrescriptionItem = (index) => {
    setWorkspace({ prescription: (current) => ({
      ...current,
      items: (current?.items || []).filter((_, itemIndex) => itemIndex !== index),
    }) });
  };

  const updatePrescriptionNote = (note) => {
    setWorkspace({
      prescription: (current) => createPrescriptionDraft({
        current,
        medicalRecord,
        patientName: appointment?.patient?.fullName,
        overrides: { note },
      }),
    });
  };

  const handleSavePrescription = async (e) => {
    e.preventDefault();

    if (!medicalRecord?.id) {
      setUi({ notice: "Vui lòng bắt đầu khám trước khi lưu đơn thuốc." });
      return;
    }

    if (!prescriptionEditable) {
      setUi({ notice: "Đơn thuốc hiện không thể chỉnh sửa." });
      return;
    }

    const items = prescription?.items || [];
    if (items.length === 0) {
      setUi({ notice: "Vui lòng thêm ít nhất một thuốc vào đơn." });
      return;
    }

    const invalidItem = items.find((item) => !item.medicineId || Number(item.quantity) <= 0);
    if (invalidItem) {
      setUi({ notice: "Vui lòng kiểm tra thuốc và số lượng trước khi lưu đơn." });
      return;
    }

    const payload = {
      note: prescription?.note || "",
      items: items.map((item) => ({
        medicineId: Number(item.medicineId),
        quantity: Number(item.quantity),
        dosage: item.dosage || "",
        frequency: item.frequency || "",
        duration: item.duration || "",
        instruction: item.instruction || "",
      })),
    };

    setUi({ savingPrescription: true, notice: "" });

    try {
      const response = prescription?.id
        ? await updateDoctorPrescription(prescription.id, payload)
        : await createDoctorPrescription(medicalRecord.id, payload);

      setWorkspace({
        prescription: response.data,
        appointment: (current) => ({
        ...current,
        prescription: response.data,
        }),
      });
      setUi({ notice: prescription?.id ? "Đã cập nhật đơn thuốc." : "Đã lưu đơn thuốc." });
    } catch (err) {
      setUi({ notice: getErrorMessage(err) });
      if (err.response?.status === 409) {
        loadWorkspace();
      }
    } finally {
      setUi({ savingPrescription: false });
    }
  };

  const allResults = useMemo(
    () => services.flatMap((item) => item.testResults || []),
    [services]
  );
  const pendingServices = useMemo(() => services.filter((item) => item.pending), [services]);
  const orderedServiceIds = useMemo(
    () => new Set(services.map((item) => Number(item.serviceId)).filter(Boolean)),
    [services]
  );
  const pendingServiceCount = pendingServices.length;
  const selectedPendingCount = selectedServiceIds.length;
  const allPendingSelected = pendingServiceCount > 0 && selectedPendingCount === pendingServiceCount;

  if (loading) {
    return <LoadingState />;
  }

  if (error) {
    return <ErrorState message={error} onRetry={loadWorkspace} />;
  }

  if (!appointment) {
    return <ErrorState message="Không tìm thấy ca khám." />;
  }

  return (
    <>
      <div className="exam-page-header">
        <div className="doctor-breadcrumb">
          Bác sĩ <span>/</span> <strong>Khám bệnh</strong>
        </div>
        <h1>Khám bệnh</h1>
        <p>Ghi nhận thông tin khám và tạo bệnh án điện tử.</p>
      </div>

      {notice && <Alert variant={notice.includes("Đã") ? "success" : "warning"}>{notice}</Alert>}

      {canStartExamination(appointment.status) && (
        <Alert variant="warning">
          Vui lòng bắt đầu khám trước từ trang chi tiết lịch hẹn để tạo bệnh án.
        </Alert>
      )}

      <ExamPatientBanner appointment={appointment} />

      <div className="exam-layout">
        <div className="exam-main-column">
          <MedicalRecordForm
            editable={editable}
            recordForm={recordForm}
            savingRecord={savingRecord}
            onSave={handleSaveRecord}
            onUpdateField={updateRecordField}
          />

          <MedicalServicesPanel
            allPendingSelected={allPendingSelected}
            editable={editable}
            pendingServiceCount={pendingServiceCount}
            savingService={savingService}
            selectedPendingCount={selectedPendingCount}
            selectedServiceIds={selectedServiceIds}
            orderedServiceIds={orderedServiceIds}
            serviceDropdownOpen={serviceDropdownOpen}
            serviceForm={serviceForm}
            serviceKeyword={serviceKeyword}
            serviceOptions={serviceOptions}
            serviceSearching={serviceSearching}
            services={services}
            onAddService={handleAddService}
            onConfirmServices={handleConfirmServices}
            onRemovePendingService={removePendingService}
            onRemoveSelectedPendingServices={removeSelectedPendingServices}
            onSelectMedicalService={selectMedicalService}
            onServiceFormChange={updateServiceForm}
            onServiceKeywordChange={updateServiceKeyword}
            onToggleAllPendingServices={toggleAllPendingServices}
            onToggleServiceSelection={toggleServiceSelection}
          />

          <TestResultsPanel
            results={allResults}
            openingResultFileId={openingResultFileId}
            onOpenResultFile={openResultFile}
          />

          <PrescriptionPanel
            activePresetField={activePresetField}
            editable={editable}
            medicineDropdownOpen={medicineDropdownOpen}
            medicineKeyword={medicineKeyword}
            medicineLoading={medicineLoading}
            medicineResults={medicineResults}
            medicineSearchError={medicineSearchError}
            prescription={prescription}
            prescriptionDispensed={prescriptionDispensed}
            prescriptionEditable={prescriptionEditable}
            prescriptionStatusLabel={prescriptionStatusLabel}
            prescriptionStatusVariant={prescriptionStatusVariant}
            prescriptionStockWarnings={prescriptionStockWarnings}
            savingPrescription={savingPrescription}
            onAddMedicine={addMedicineToPrescription}
            onApplyPreset={applyPrescriptionPreset}
            onMedicineKeywordChange={(value) => {
              setMedicineSearch({ keyword: value, dropdownOpen: Boolean(value.trim()) });
            }}
            onMedicineSearchBlur={() => {
              setTimeout(() => setMedicineSearch({ dropdownOpen: false }), 150);
            }}
            onMedicineSearchFocus={() => {
              if (medicineKeyword.trim()) {
                setMedicineSearch({ dropdownOpen: true });
              }
            }}
            onRemoveItem={removePrescriptionItem}
            onSavePrescription={handleSavePrescription}
            onSetActivePresetField={(value) => setMedicineSearch({ activePresetField: value })}
            onUpdateItem={updatePrescriptionItem}
            onUpdateNote={updatePrescriptionNote}
          />
        </div>
      </div>

      <div className="exam-action-bar">
        <Button
          type="button"
          disabled={!editable || completingRecord}
          onClick={() => {
            setUi({ completeError: "", completeModal: true });
          }}
        >
          {completingRecord ? "Đang hoàn tất..." : "Hoàn tất bệnh án"}
        </Button>
      </div>

      <Modal show={resultFileViewer.show} onHide={closeResultFileViewer} size="xl" centered>
        <Modal.Header closeButton>
          <Modal.Title>{resultFileViewer.title}</Modal.Title>
        </Modal.Header>
        <Modal.Body className="p-0">
          {resultFileViewer.url && (
            <iframe
              title={resultFileViewer.title}
              src={resultFileViewer.url}
              className="result-file-viewer"
            />
          )}
        </Modal.Body>
      </Modal>

      <ConfirmModal
        show={completeModal}
        title="Hoàn tất bệnh án"
        message={(
          <>
            <p className="mb-0">
              Bệnh án sẽ được lưu và lịch khám chuyển sang trạng thái đã khám xong. Sau khi hoàn tất, bạn không thể chỉnh sửa bệnh án này.
            </p>
            {completeError && (
              <Alert variant="danger" className="mt-3 mb-0">
                {completeError}
              </Alert>
            )}
          </>
        )}
        confirmText="Hoàn tất"
        confirmVariant="primary"
        loading={completingRecord}
        onConfirm={handleCompleteRecord}
        onHide={() => {
          setUi({ completeError: "", completeModal: false });
        }}
      />
    </>
  );
}

export default ExaminationWorkspacePage;
