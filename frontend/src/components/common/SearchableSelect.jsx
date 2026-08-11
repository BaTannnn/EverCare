import { useEffect, useMemo, useState } from "react";
import { Form } from "react-bootstrap";

const normalizeValue = (value) => String(value ?? "");

function SearchableSelect({
  label,
  value,
  options,
  onChange,
  placeholder = "Chọn",
  searchPlaceholder = "Tìm kiếm...",
  emptyMessage = "Không tìm thấy kết quả",
  disabled = false,
  required = false,
  getOptionValue = (option) => option?.id ?? "",
  getOptionLabel = (option) => option?.name ?? "",
  getOptionDescription = null,
}) {
  const [searchText, setSearchText] = useState("");
  const [open, setOpen] = useState(false);

  const selectedOption = useMemo(
    () => options.find((option) => normalizeValue(getOptionValue(option)) === normalizeValue(value)) || null,
    [getOptionValue, options, value],
  );

  const selectedLabel = selectedOption ? getOptionLabel(selectedOption) : "";

  useEffect(() => {
    setSearchText(selectedLabel || "");
  }, [selectedLabel]);

  const filteredOptions = useMemo(() => {
    const keyword = searchText.trim().toLowerCase();
    if (!keyword) return options;

    return options.filter((option) => {
      const labelText = String(getOptionLabel(option) || "").toLowerCase();
      const descriptionText = String(getOptionDescription ? getOptionDescription(option) : "").toLowerCase();

      return labelText.includes(keyword) || descriptionText.includes(keyword);
    });
  }, [getOptionDescription, getOptionLabel, options, searchText]);

  const handleInputChange = (event) => {
    setSearchText(event.target.value);
    setOpen(true);
  };

  const handleOptionSelect = (option) => {
    onChange(normalizeValue(getOptionValue(option)));
    setSearchText(getOptionLabel(option) || "");
    setOpen(false);
  };

  const handleBlur = () => {
    window.setTimeout(() => {
      setOpen(false);
      setSearchText(selectedLabel || "");
    }, 120);
  };

  return (
    <Form.Group>
      <Form.Label>{label}</Form.Label>
      <div className="support-doctor-combobox">
        <Form.Control
          className="searchable-select-input"
          value={searchText}
          onChange={handleInputChange}
          onFocus={() => setOpen(true)}
          onBlur={handleBlur}
          placeholder={disabled ? placeholder : (open ? searchPlaceholder : placeholder)}
          autoComplete="off"
          disabled={disabled}
          required={required}
        />
        {!disabled && (selectedOption || searchText) && (
          <button
            type="button"
            className="support-doctor-clear"
            onMouseDown={(event) => event.preventDefault()}
            onClick={() => {
              setSearchText("");
              setOpen(false);
              onChange("");
            }}
            aria-label="Xóa lựa chọn"
          >
            ×
          </button>
        )}
        {open && !disabled && (
          <div className="support-doctor-menu">
            {filteredOptions.length > 0 ? (
              filteredOptions.map((option) => {
                const optionValue = normalizeValue(getOptionValue(option));
                const optionLabel = getOptionLabel(option) || "";
                const optionDescription = getOptionDescription ? getOptionDescription(option) : "";

                return (
                  <button
                    key={optionValue}
                    type="button"
                    className="support-doctor-option"
                    onMouseDown={(event) => event.preventDefault()}
                    onClick={() => handleOptionSelect(option)}
                  >
                    <span>{optionLabel}</span>
                    {optionDescription ? <small>{optionDescription}</small> : null}
                  </button>
                );
              })
            ) : (
              <div className="support-doctor-empty">{emptyMessage}</div>
            )}
          </div>
        )}
      </div>
    </Form.Group>
  );
}

export default SearchableSelect;
